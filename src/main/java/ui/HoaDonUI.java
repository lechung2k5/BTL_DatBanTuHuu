package ui;

import dao.HoaDonDAO;
import entity.ChiTietHoaDon;
import entity.HoaDon;
import entity.PTTThanhToan;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime; // Import LocalDateTime
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.VerticalAlignment; // Import đúng package
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Lớp Controller cho giao diện Quản lý Hóa Đơn (HoaDon.fxml) Đã cập nhật để
 * đồng bộ với entity HoaDon mới.
 */
public class HoaDonUI {

	// === CÁC THÀNH PHẦN GIAO DIỆN (FXML) ===
	@FXML
	private ComboBox<String> comboFilter;
	@FXML
	private TextField txtSearch;
	@FXML
	private Button btnSearch;

	@FXML
	private TableView<HoaDon> tableHoaDon;
	@FXML
	private TableColumn<HoaDon, String> colMaHD;
	@FXML
	private TableColumn<HoaDon, String> colNgay; // Kiểu String vì hiển thị đã format
	@FXML
	private TableColumn<HoaDon, PTTThanhToan> colHinhThucThanhToan;
	@FXML
	private TableColumn<HoaDon, String> colSoDienThoaiKH; // Dùng getter mới
	@FXML
	private TableColumn<HoaDon, Double> colTongTien;
	@FXML
	private TableColumn<HoaDon, Void> colXemChiTiet;

	@FXML
	private Button btnXuatExcel;
	@FXML
	private Button btnInHoaDon;

	@FXML
	private TableView<ChiTietHoaDon> tableChiTietHD;

	// Các Label hiển thị thông tin chi tiết
	@FXML
	private Label lblMaHDValue;
	@FXML
	private Label lblMaKHValue; // Sẽ hiển thị SĐT
	@FXML
	private Label lblNgayValue;
	@FXML
	private Label lblThuNganValue;
	@FXML
	private Label lblBanValue;
	@FXML
	private Label lblGioVaoValue;
	@FXML
	private Label lblGioRaValue;
	@FXML
	private Label lblTongCongMonAnValue;
	@FXML
	private Label lblPhiDichVuValue;
	@FXML
	private Label lblThueVATValue;
	@FXML
	private Label lblTienDatCocValue;
	@FXML
	private Label lblKhuyenMaiValue;
	@FXML
	private Label lblTongTienThanhToanValue;

	// === BIẾN QUẢN LÝ DỮ LIỆU ===
	private HoaDonDAO hoaDonDAO;
	private ObservableList<HoaDon> allHoaDonList;
	private ObservableList<HoaDon> danhSachHoaDon; // Danh sách hiển thị
	private ObservableList<ChiTietHoaDon> danhSachChiTietHD;

	// Định dạng ngày giờ
	private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
	private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"); // Format cho
																											// LocalDateTime

	// Hằng số cho ComboBox
	private final String ALL_METHODS = "Tất cả PT";
	private final String VI_DIEN_TU = PTTThanhToan.VI_DIEN_TU.getDisplayName();
	private final String NGAN_HANG = PTTThanhToan.NGAN_HANG.getDisplayName();
	private final String TIEN_MAT = PTTThanhToan.TIEN_MAT.getDisplayName();

	@FXML
	private void initialize() {
		hoaDonDAO = new HoaDonDAO();
		allHoaDonList = FXCollections.observableArrayList();
		danhSachHoaDon = FXCollections.observableArrayList();
		danhSachChiTietHD = FXCollections.observableArrayList();

		setupTableHoaDonColumns();
		setupTableChiTietHoaDon();

		comboFilter.getItems().addAll(ALL_METHODS, VI_DIEN_TU, NGAN_HANG, TIEN_MAT);
		comboFilter.setValue(ALL_METHODS);

		comboFilter.setOnAction(event -> filterData());
		btnSearch.setOnAction(event -> filterData());
		txtSearch.setOnAction(event -> filterData()); // Thêm: Lọc khi nhấn Enter trên TextField

		loadAndFilterData();

		btnXuatExcel.setOnAction(e -> xuatExcel());
		btnInHoaDon.setOnAction(e -> inHoaDon());

		tableHoaDon.setItems(danhSachHoaDon);

		// Listener cho việc chọn dòng trong bảng hóa đơn chính
		tableHoaDon.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
			if (newSelection != null) {
				hienThiThongTinHoaDon(newSelection);
			} else {
				hienThiThongTinHoaDon(null); // Xóa thông tin nếu không có dòng nào được chọn
			}
		});
	}

	/**
	 * Cấu hình các cột cho bảng Hóa Đơn chính (tableHoaDon)
	 */
	private void setupTableHoaDonColumns() {
		colMaHD.setCellValueFactory(new PropertyValueFactory<>("maHD")); // Sửa: Dùng maHD

		// SỬA: Định dạng LocalDateTime cho cột Ngày Lập
		colNgay.setCellValueFactory(cellData -> {
			LocalDateTime ngayLap = cellData.getValue().getNgayLap();
			String formattedDate = (ngayLap != null) ? ngayLap.format(dateFormatter) : "N/A";
			return new javafx.beans.property.SimpleStringProperty(formattedDate);
		});

		colHinhThucThanhToan.setCellValueFactory(new PropertyValueFactory<>("hinhThucTT"));
		colHinhThucThanhToan.setCellFactory(column -> new TableCell<HoaDon, PTTThanhToan>() {
			@Override
			protected void updateItem(PTTThanhToan item, boolean empty) {
				super.updateItem(item, empty);
				setText((empty || item == null) ? null : item.getDisplayName());
			}
		});

		// SỬA: Dùng getter mới getSoDienThoaiKH()
		colSoDienThoaiKH.setCellValueFactory(new PropertyValueFactory<>("soDienThoaiKH"));

		colTongTien.setCellValueFactory(new PropertyValueFactory<>("tongTienThanhToan"));
		colTongTien.setCellFactory(column -> new TableCell<>() {
			@Override
			protected void updateItem(Double item, boolean empty) {
				super.updateItem(item, empty);
				setText(empty || item == null ? null : String.format("%,.0f VNĐ", item));
			}
		});

		colXemChiTiet.setCellFactory(param -> new TableCell<>() {
			private final Button btnXem = new Button("Xem");
			private final HBox pane = new HBox(btnXem);
			{
				pane.setAlignment(Pos.CENTER);
				btnXem.getStyleClass().add("view-button");
				btnXem.setOnAction(event -> {
					HoaDon hoaDon = getTableView().getItems().get(getIndex());
					if (hoaDon != null) {
						hienThiThongTinHoaDon(hoaDon);
						// Có thể thêm: tableHoaDon.getSelectionModel().select(hoaDon); // Tự động
						// select dòng khi nhấn Xem
					}
				});
			}

			@Override
			protected void updateItem(Void item, boolean empty) {
				super.updateItem(item, empty);
				setGraphic(empty ? null : pane);
			}
		});
	}

	private void loadAndFilterData() {
		allHoaDonList.setAll(hoaDonDAO.getAllHoaDon());
		filterData(); // Áp dụng bộ lọc ban đầu
		// Hiển thị chi tiết HD đầu tiên sau khi load và lọc xong
		if (!danhSachHoaDon.isEmpty()) {
			tableHoaDon.getSelectionModel().selectFirst();
			// Listener ở initialize sẽ tự gọi hienThiThongTinHoaDon
		} else {
			hienThiThongTinHoaDon(null); // Xóa nếu không có HD nào
		}
	}

	private void filterData() {
		String selectedPaymentMethodDisplay = comboFilter.getValue();
		String searchText = txtSearch.getText().toLowerCase().trim();

		List<HoaDon> filteredList = allHoaDonList.stream().filter(hd -> {
			boolean paymentMatch = ALL_METHODS.equals(selectedPaymentMethodDisplay) || (hd.getHinhThucTT() != null
					&& selectedPaymentMethodDisplay.equalsIgnoreCase(hd.getHinhThucTT().getDisplayName()));

			// SỬA: Dùng getter mới getSoDienThoaiKH() để lọc
			boolean searchMatch = searchText.isEmpty()
					|| (hd.getSoDienThoaiKH() != null && hd.getSoDienThoaiKH().toLowerCase().contains(searchText));

			return paymentMatch && searchMatch;
		}).collect(Collectors.toList());

		danhSachHoaDon.setAll(filteredList);

		// Không cần gọi hienThiThongTinHoaDon ở đây nữa,
		// vì listener của tableHoaDon.getSelectionModel() sẽ xử lý
		if (danhSachHoaDon.isEmpty()) {
			hienThiThongTinHoaDon(null); // Xóa thông tin chi tiết nếu kq lọc rỗng
		} else {
			tableHoaDon.getSelectionModel().selectFirst(); // Chọn dòng đầu nếu có kq
		}
	}

	private void setupTableChiTietHoaDon() {
		// (Giữ nguyên như trước)
		tableChiTietHD.getColumns().clear();
		TableColumn<ChiTietHoaDon, Void> colSTT = new TableColumn<>("STT");
		colSTT.setPrefWidth(40);
		colSTT.setSortable(false);
		colSTT.setCellFactory(col -> new TableCell<>() {
			@Override
			public void updateIndex(int index) {
				super.updateIndex(index);
				setText(isEmpty() || index < 0 ? null : Integer.toString(index + 1));
			}
		});
		TableColumn<ChiTietHoaDon, String> colTenMon = new TableColumn<>("Tên món");
		colTenMon.setCellValueFactory(new PropertyValueFactory<>("tenMon"));
		colTenMon.setPrefWidth(150);
		TableColumn<ChiTietHoaDon, Integer> colSoLuong = new TableColumn<>("Số lượng");
		colSoLuong.setCellValueFactory(new PropertyValueFactory<>("soLuong"));
		colSoLuong.setPrefWidth(70);
		TableColumn<ChiTietHoaDon, Double> colDonGia = new TableColumn<>("Đơn giá");
		colDonGia.setCellValueFactory(new PropertyValueFactory<>("donGia"));
		colDonGia.setPrefWidth(90);
		colDonGia.setCellFactory(column -> new TableCell<>() {
			@Override
			protected void updateItem(Double item, boolean empty) {
				super.updateItem(item, empty);
				setText(empty || item == null ? null : String.format("%,.0f", item));
			}
		});
		TableColumn<ChiTietHoaDon, Double> colThanhTien = new TableColumn<>("Thành tiền");
		colThanhTien.setCellValueFactory(new PropertyValueFactory<>("thanhTien"));
		colThanhTien.setPrefWidth(100);
		colThanhTien.setCellFactory(column -> new TableCell<>() {
			@Override
			protected void updateItem(Double item, boolean empty) {
				super.updateItem(item, empty);
				setText(empty || item == null ? null : String.format("%,.0f", item));
			}
		});
		tableChiTietHD.getColumns().addAll(colSTT, colTenMon, colSoLuong, colDonGia, colThanhTien);
		tableChiTietHD.setItems(danhSachChiTietHD);
	}

	/**
	 * Hiển thị thông tin chi tiết của một hóa đơn lên panel bên phải. Đã cập nhật
	 * để dùng getters mới và định dạng LocalDateTime.
	 */
	private void hienThiThongTinHoaDon(HoaDon hoaDon) {
		if (hoaDon == null) {
			lblMaHDValue.setText("...");
			lblMaKHValue.setText("...");
			lblNgayValue.setText("...");
			lblThuNganValue.setText("...");
			lblBanValue.setText("...");
			lblGioVaoValue.setText("...");
			lblGioRaValue.setText("...");
			lblTongCongMonAnValue.setText("0 VNĐ");
			lblPhiDichVuValue.setText("0 VNĐ");
			lblThueVATValue.setText("0 VNĐ");
			lblTienDatCocValue.setText("0 VNĐ");
			lblKhuyenMaiValue.setText("0 VNĐ");
			lblTongTienThanhToanValue.setText("0 VNĐ");
			danhSachChiTietHD.clear();
			return;
		}

		// SỬA: Dùng các getters mới và định dạng LocalDateTime
		lblMaHDValue.setText(hoaDon.getMaHD());
		lblMaKHValue.setText(hoaDon.getSoDienThoaiKH() != null ? hoaDon.getSoDienThoaiKH() : "N/A");
		lblNgayValue.setText(hoaDon.getNgayLap() != null ? hoaDon.getNgayLap().format(dateFormatter) : "N/A");
		lblThuNganValue.setText(hoaDon.getTenNhanVien() != null ? hoaDon.getTenNhanVien() : "N/A");
		lblBanValue.setText(hoaDon.getMaBan() != null ? hoaDon.getMaBan() : "N/A"); // Dùng getter tiện ích
		lblGioVaoValue.setText(hoaDon.getGioVao() != null ? hoaDon.getGioVao().format(timeFormatter) : "N/A"); // Chỉ
																												// hiển
																												// thị
																												// giờ:phút
		lblGioRaValue.setText(hoaDon.getGioRa() != null ? hoaDon.getGioRa().format(timeFormatter) : "N/A"); // Chỉ hiển
																											// thị
																											// giờ:phút

		lblTongCongMonAnValue.setText(String.format("%,.0f VNĐ", hoaDon.getTongCongMonAn()));
		lblPhiDichVuValue.setText(String.format("%,.0f VNĐ", hoaDon.getPhiDichVu()));
		lblThueVATValue.setText(String.format("%,.0f VNĐ", hoaDon.getThueVAT()));
		lblTienDatCocValue.setText(String.format("%,.0f VNĐ", hoaDon.getTienCoc()));
		lblKhuyenMaiValue.setText(String.format("%,.0f VNĐ", hoaDon.getKhuyenMai()));
		lblTongTienThanhToanValue.setText(String.format("%,.0f VNĐ", hoaDon.getTongTienThanhToan()));

		// Tải chi tiết món ăn (giữ nguyên)
		danhSachChiTietHD.setAll(hoaDonDAO.getChiTietHoaDon(hoaDon.getMaHD()));
	}

	private void xuatExcel() {
		ObservableList<HoaDon> dataToExport = tableHoaDon.getItems();
		if (dataToExport == null || dataToExport.isEmpty()) {
			showAlert("Không có dữ liệu hóa đơn để xuất.");
			return;
		}

		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Lưu file Excel");
		fileChooser.setInitialFileName("DanhSachHoaDon_" + LocalDate.now() + ".xlsx");
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Excel files (*.xlsx)", "*.xlsx");
		fileChooser.getExtensionFilters().add(extFilter);

		Stage stage = (Stage) tableHoaDon.getScene().getWindow();
		File file = fileChooser.showSaveDialog(stage);

		if (file != null) {
			try (Workbook workbook = new XSSFWorkbook(); FileOutputStream fileOut = new FileOutputStream(file)) {
				Sheet sheet = workbook.createSheet("DanhSachHoaDon");

				// --- Styles ---
				CellStyle headerStyle = createHeaderStyle(workbook);
				CellStyle currencyStyle = createCurrencyStyle(workbook);
				CellStyle dateTimeStyle = createDateTimeStyle(workbook);
				CellStyle basicStyle = createBasicCellStyle(workbook);

				// --- Header Row ---
				String[] columns = { "Mã HĐ", "Ngày Lập", "PT Thanh Toán", "Trạng Thái", "SĐT Khách", "Thu Ngân", "Bàn",
						"Giờ Vào", "Giờ Ra", "Tiền Cọc", "Tổng Tiền Món", "Tổng Thanh Toán" };
				Row headerRow = sheet.createRow(0);
				for (int i = 0; i < columns.length; i++) {
					Cell cell = headerRow.createCell(i);
					cell.setCellValue(columns[i]);
					cell.setCellStyle(headerStyle);
				}

				// --- Data Rows ---
				int rowNum = 1;
				for (HoaDon hd : dataToExport) {
					Row row = sheet.createRow(rowNum++);
					createCell(row, 0, hd.getMaHD(), basicStyle);
					createCell(row, 1, hd.getNgayLap(), dateTimeStyle); // Truyền LocalDateTime
					createCell(row, 2, (hd.getHinhThucTT() != null ? hd.getHinhThucTT().getDisplayName() : ""),
							basicStyle);
					createCell(row, 3, (hd.getTrangThai() != null ? hd.getTrangThai().getDisplayName() : ""),
							basicStyle);
					createCell(row, 4, hd.getSoDienThoaiKH(), basicStyle);
					createCell(row, 5, hd.getTenNhanVien(), basicStyle);
					createCell(row, 6, hd.getMaBan(), basicStyle);
					createCell(row, 7, hd.getGioVao(), dateTimeStyle); // Truyền LocalDateTime
					createCell(row, 8, hd.getGioRa(), dateTimeStyle); // Truyền LocalDateTime
					createCell(row, 9, hd.getTienCoc(), currencyStyle);
					createCell(row, 10, hd.getTongCongMonAn(), currencyStyle);
					createCell(row, 11, hd.getTongTienThanhToan(), currencyStyle);
				}

				// --- Auto Size Columns ---
				for (int i = 0; i < columns.length; i++) {
					sheet.autoSizeColumn(i);
				}

				workbook.write(fileOut);
				showAlert("Xuất file Excel thành công!\nĐã lưu tại: " + file.getAbsolutePath());

			} catch (IOException e) {
				showAlert("Lỗi khi ghi file Excel: " + e.getMessage());
				e.printStackTrace();
			} catch (Exception e) {
				showAlert("Đã xảy ra lỗi không mong muốn: " + e.getMessage());
				e.printStackTrace();
			}
		} else {
			System.out.println("Hủy thao tác lưu file Excel.");
		}
	}

	// --- Hàm tiện ích tạo CellStyle (giúp code `xuatExcel` gọn hơn) ---
	private CellStyle createHeaderStyle(Workbook workbook) {
		CellStyle style = workbook.createCellStyle();
		Font font = workbook.createFont();
		font.setBold(true);
		font.setFontHeightInPoints((short) 12);
		style.setFont(font);
		style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		style.setAlignment(HorizontalAlignment.CENTER);
		style.setVerticalAlignment(VerticalAlignment.CENTER);
		style.setBorderBottom(BorderStyle.THIN);
		style.setBorderTop(BorderStyle.THIN);
		style.setBorderLeft(BorderStyle.THIN);
		style.setBorderRight(BorderStyle.THIN);
		return style;
	}

	private CellStyle createCurrencyStyle(Workbook workbook) {
		CellStyle style = createBasicCellStyle(workbook); // Kế thừa border
		DataFormat format = workbook.createDataFormat();
		style.setDataFormat(format.getFormat("#,##0\" VNĐ\""));
		return style;
	}

	private CellStyle createDateTimeStyle(Workbook workbook) {
		CellStyle style = createBasicCellStyle(workbook); // Kế thừa border
		CreationHelper createHelper = workbook.getCreationHelper();
		style.setDataFormat(createHelper.createDataFormat().getFormat("dd/MM/yyyy HH:mm")); // Dùng CreationHelper
		return style;
	}

	private CellStyle createBasicCellStyle(Workbook workbook) {
		CellStyle style = workbook.createCellStyle();
		style.setBorderBottom(BorderStyle.THIN);
		style.setBorderTop(BorderStyle.THIN);
		style.setBorderLeft(BorderStyle.THIN);
		style.setBorderRight(BorderStyle.THIN);
		style.setVerticalAlignment(VerticalAlignment.CENTER); // Căn giữa theo chiều dọc
		return style;
	}

	// --- Hàm tiện ích tạo Cell (xử lý kiểu dữ liệu khác nhau) ---
	private void createCell(Row row, int column, Object value, CellStyle style) {
		Cell cell = row.createCell(column);
		if (value instanceof String) {
			cell.setCellValue((String) value);
		} else if (value instanceof Double) {
			cell.setCellValue((Double) value);
		} else if (value instanceof Integer) { // Thêm kiểu Integer nếu cần
			cell.setCellValue((Integer) value);
		} else if (value instanceof LocalDateTime) { // Xử lý LocalDateTime
			cell.setCellValue((LocalDateTime) value);
		} else if (value instanceof LocalDate) { // Xử lý LocalDate (nếu có)
			cell.setCellValue((LocalDate) value);
		} else if (value == null) {
			cell.setBlank();
		}
		// Luôn áp dụng style
		if (style != null) {
			cell.setCellStyle(style);
		}
	}

	private void inHoaDon() {
		HoaDon selectedHoaDon = tableHoaDon.getSelectionModel().getSelectedItem();
		if (selectedHoaDon != null) {
			showAlert("Chuẩn bị in hóa đơn: " + selectedHoaDon.getMaHD()); // Sửa: Dùng getMaHD()
		} else {
			showAlert("Vui lòng chọn một hóa đơn để in.");
		}
	}

	private void showAlert(String message) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}
}
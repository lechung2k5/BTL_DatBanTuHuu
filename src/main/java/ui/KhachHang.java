package ui;

import java.time.LocalDate;
import java.time.LocalDateTime; // Import LocalDateTime
import java.time.format.DateTimeFormatter;
import java.util.List; // Import List
import java.util.Optional; // Import Optional

import dao.KhachHangDAO;
import dao.HoaDonDAO; // THÊM IMPORT HoaDonDAO
import entity.ChiTietHoaDon;
import entity.HoaDon; // THÊM IMPORT HoaDon
import entity.PTTThanhToan; // THÊM IMPORT PTTThanhToan
import javafx.beans.property.SimpleObjectProperty; // THÊM IMPORT SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets; // THÊM IMPORT Insets
import javafx.geometry.Pos;
import javafx.scene.control.*; // Import tất cả control
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority; // THÊM IMPORT Priority
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox; // THÊM IMPORT VBox

public class KhachHang {

	// === Khai báo FXML ===
	@FXML
	private TableView<entity.KhachHang> tblKhachHang;
	@FXML
	private TableColumn<entity.KhachHang, String> colMaKH;
	@FXML
	private TableColumn<entity.KhachHang, String> colHoTen;
	@FXML
	private TableColumn<entity.KhachHang, String> colSDT;
	@FXML
	private TableColumn<entity.KhachHang, String> colDiaChi;
	@FXML
	private TableColumn<entity.KhachHang, String> colEmail;
	@FXML
	private TableColumn<entity.KhachHang, String> colNgayDangKy;
	@FXML
	private TableColumn<entity.KhachHang, String> colLoaiKH;
	@FXML
	private TableColumn<entity.KhachHang, String> colTongTien; // Kiểu String vì sẽ hiển thị text
	@FXML
	private TableColumn<entity.KhachHang, Void> colXemLichSu;

	@FXML
	private TextField txtHoTen, txtSDT, txtDiaChi, txtEmail, txtSearch;
	@FXML
	private DatePicker datePickerNgayDangKy;
	@FXML
	private ComboBox<String> filterComboBox;

	@FXML
	private Button btnThem, btnXoa, btnSua, btnXoaTrang;
	@FXML
	private Button btnTim; // Nút tìm kiếm

	// === Thuộc tính khác ===
	// ✅ Đảm bảo các formatter này được khởi tạo đúng và là final
	private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
	private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
	private KhachHangDAO khachHangDAO;
	private HoaDonDAO hoaDonDAO; // DAO để lấy lịch sử hóa đơn

	private ObservableList<entity.KhachHang> masterCustomerList; // Danh sách gốc
	private FilteredList<entity.KhachHang> filteredCustomerList; // Danh sách hiển thị sau lọc

	// ==================================
	// KHỞI TẠO (INITIALIZE)
	// ==================================
	public void initialize() {
		khachHangDAO = new KhachHangDAO();
		hoaDonDAO = new HoaDonDAO(); // Khởi tạo HoaDonDAO

		setupTableColumns(); // Cài đặt các cột bảng
		loadDatabaseData(); // Tải dữ liệu ban đầu
		setupFiltersAndSearch(); // Cài đặt bộ lọc và tìm kiếm
		setupActionButtons(); // Gán sự kiện cho các nút Thêm/Sửa/Xóa
		setupSelectionListener(); // Lắng nghe sự kiện chọn dòng
		clearForm(); // Xóa trắng form ban đầu
	}

	// ==================================
	// CÀI ĐẶT GIAO DIỆN
	// ==================================

	/** Cài đặt bộ lọc ComboBox và sự kiện nút Tìm */
	private void setupFiltersAndSearch() {
		filterComboBox.setItems(FXCollections.observableArrayList("Tất cả", "Member", "Gold", "Diamond", "Guest" // Thêm
																													// Guest
																													// nếu
																													// có
		));
		filterComboBox.setValue("Tất cả"); // Giá trị mặc định

		// Lắng nghe thay đổi ComboBox và ô tìm kiếm, gọi updateFilter
		filterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> updateFilter());
		txtSearch.textProperty().addListener((obs, oldVal, newVal) -> updateFilter()); // Lọc ngay khi gõ
		// Nút Tìm có thể không cần thiết nếu lọc live, nhưng vẫn giữ lại
		btnTim.setOnAction(e -> updateFilter());
	}

	/** Gán sự kiện cho các nút Thêm, Sửa, Xóa, Xóa trắng */
	private void setupActionButtons() {
		btnThem.setOnAction(e -> handleThem());
		btnSua.setOnAction(e -> handleSua());
		btnXoa.setOnAction(e -> handleXoa());
		btnXoaTrang.setOnAction(e -> clearForm());
	}

	/** Lắng nghe sự kiện chọn dòng trên TableView */
	private void setupSelectionListener() {
		tblKhachHang.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
			if (newSelection != null) {
				// Hiển thị thông tin lên form
				txtHoTen.setText(newSelection.getTenKH());
				txtSDT.setText(newSelection.getSoDT());
				txtDiaChi.setText(newSelection.getDiaChi());
				txtEmail.setText(newSelection.getEmail());
				datePickerNgayDangKy.setValue(newSelection.getNgayDangKy());
			} else {
				clearForm(); // Xóa form nếu không có dòng nào được chọn
			}
		});
	}

	/** Cấu hình các cột cho TableView Khách Hàng */
	private void setupTableColumns() {
		tblKhachHang.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

		// ✅ SỬA LỖI: Đảm bảo tên thuộc tính khớp với getter trong entity.KhachHang
		colMaKH.setCellValueFactory(new PropertyValueFactory<>("maKH")); // Cần getMaKH()
		colHoTen.setCellValueFactory(new PropertyValueFactory<>("tenKH")); // Cần getTenKH()
		colSDT.setCellValueFactory(new PropertyValueFactory<>("soDT")); // Cần getSoDT()
		colDiaChi.setCellValueFactory(new PropertyValueFactory<>("diaChi")); // Cần getDiaChi()
		colEmail.setCellValueFactory(new PropertyValueFactory<>("email")); // Cần getEmail()
		colLoaiKH.setCellValueFactory(new PropertyValueFactory<>("thanhVien")); // Cần getThanhVien()

		// Cột Ngày đăng ký (Định dạng Date) - Cần getNgayDangKy() trả về LocalDate
		colNgayDangKy.setCellValueFactory(cellData -> {
			LocalDate date = cellData.getValue().getNgayDangKy();
			String formattedDate = (date != null) ? date.format(dateFormatter) : "";
			return new SimpleStringProperty(formattedDate);
		});

		// Cột Tổng tiền HĐ (Hiện đang ẩn và hiển thị "N/A")
		// Cần getTongTienHoaDon() hoặc tính toán riêng nếu muốn hiển thị giá trị
		colTongTien.setCellValueFactory(cellData -> new SimpleStringProperty("N/A")); // Giá trị tạm
		colTongTien.setVisible(false); // Ẩn cột này đi

		// Cột nút Xem Lịch Sử
		colXemLichSu.setCellFactory(param -> new TableCell<entity.KhachHang, Void>() {
			private final Button viewButton = new Button("Xem");
			private final HBox pane = new HBox(viewButton);
			{
				viewButton.getStyleClass().add("view-button");
				pane.setAlignment(Pos.CENTER);
				viewButton.setOnAction(event -> {
					entity.KhachHang customer = getTableView().getItems().get(getIndex());
					if (customer != null && customer.getMaKH() != null) {
						showInvoiceHistoryDialog(customer); // Gọi hàm hiển thị lịch sử
					} else {
						showAlert(Alert.AlertType.WARNING, "Lỗi", "Không thể lấy thông tin khách hàng.");
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

	// ==================================
	// XỬ LÝ DỮ LIỆU VÀ SỰ KIỆN
	// ==================================

	/** Tải dữ liệu từ CSDL vào danh sách gốc và áp dụng bộ lọc */
	private void loadDatabaseData() {
		try {
			masterCustomerList = khachHangDAO.getAllKhachHang();
			if (masterCustomerList == null) {
				masterCustomerList = FXCollections.observableArrayList();
				showAlert(Alert.AlertType.ERROR, "Lỗi dữ liệu", "Không thể tải danh sách khách hàng từ CSDL.");
			}
		} catch (Exception e) {
			masterCustomerList = FXCollections.observableArrayList();
			showAlert(Alert.AlertType.ERROR, "Lỗi tải dữ liệu", "Đã xảy ra lỗi: " + e.getMessage());
			e.printStackTrace();
		}
		// Luôn khởi tạo FilteredList dù có lỗi hay không
		filteredCustomerList = new FilteredList<>(masterCustomerList, p -> true);
		tblKhachHang.setItems(filteredCustomerList);
		updateFilter(); // Áp dụng bộ lọc ban đầu (hiển thị tất cả)
		System.out.println("LOG: Đã tải " + masterCustomerList.size() + " khách hàng.");
	}

	/** Cập nhật bộ lọc dựa trên ComboBox và ô tìm kiếm */
	private void updateFilter() {
		String selectedTier = filterComboBox.getValue();
		String searchText = txtSearch.getText() != null ? txtSearch.getText().toLowerCase().trim() : "";

		if (filteredCustomerList == null)
			return;

		filteredCustomerList.setPredicate(customer -> {
			boolean tierMatch = selectedTier == null || selectedTier.equals("Tất cả")
					|| (customer.getThanhVien() != null && customer.getThanhVien().equalsIgnoreCase(selectedTier));

			// Tìm kiếm tương đối theo SĐT hoặc Tên
			boolean searchMatch = searchText.isEmpty()
					|| (customer.getSoDT() != null && customer.getSoDT().toLowerCase().contains(searchText))
					|| (customer.getTenKH() != null && customer.getTenKH().toLowerCase().contains(searchText));

			return tierMatch && searchMatch;
		});

		// Xử lý việc chọn dòng sau khi lọc
		if (filteredCustomerList.isEmpty()) {
			clearForm();
		} else {
			// Chỉ chọn lại dòng đầu tiên nếu dòng đang chọn không còn trong danh sách lọc
			entity.KhachHang currentSelection = tblKhachHang.getSelectionModel().getSelectedItem();
			if (currentSelection == null || !filteredCustomerList.contains(currentSelection)) {
				tblKhachHang.getSelectionModel().selectFirst();
			}
			// Nếu dòng đang chọn vẫn hợp lệ, không cần làm gì, listener sẽ giữ form được
			// cập nhật
		}
	}

	/** Xử lý nút Thêm */
	private void handleThem() {
		String hoTen = txtHoTen.getText().trim();
		String sdt = txtSDT.getText().trim();
		String diaChi = txtDiaChi.getText().trim();
		String email = txtEmail.getText().trim();
		LocalDate ngayDangKy = datePickerNgayDangKy.getValue();

		// Kiểm tra thông tin bắt buộc
		if (hoTen.isEmpty() || sdt.isEmpty() || ngayDangKy == null) {
			showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng nhập Họ tên, SĐT và Ngày đăng ký.");
			return;
		}
		// TODO: Thêm kiểm tra định dạng SĐT, Email nếu cần

		String newId = khachHangDAO.getNewMaKH();
		String loaiKH = "Member"; // Mặc định khi thêm mới

		entity.KhachHang newCustomer = new entity.KhachHang(newId, hoTen, sdt, email, ngayDangKy, diaChi, loaiKH);

		if (khachHangDAO.themKhachHang(newCustomer)) {
			masterCustomerList.add(newCustomer); // Thêm vào danh sách gốc
			// FilteredList sẽ tự cập nhật nếu khách hàng mới khớp bộ lọc hiện tại
			tblKhachHang.getSelectionModel().select(newCustomer); // Tự động chọn khách hàng mới thêm
			showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã thêm khách hàng mới.");
		} else {
			showAlert(Alert.AlertType.ERROR, "Lỗi CSDL", "Thêm khách hàng thất bại.");
		}
	}

	/** Xử lý nút Sửa */
	private void handleSua() {
		entity.KhachHang selectedCustomer = tblKhachHang.getSelectionModel().getSelectedItem();
		if (selectedCustomer == null) {
			showAlert(Alert.AlertType.WARNING, "Chưa chọn", "Vui lòng chọn khách hàng cần sửa.");
			return;
		}

		String hoTen = txtHoTen.getText().trim();
		String sdt = txtSDT.getText().trim();
		String diaChi = txtDiaChi.getText().trim();
		String email = txtEmail.getText().trim();
		LocalDate ngayDangKy = datePickerNgayDangKy.getValue();

		if (hoTen.isEmpty() || sdt.isEmpty() || ngayDangKy == null) {
			showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng nhập Họ tên, SĐT và Ngày đăng ký.");
			return;
		}
		// TODO: Thêm kiểm tra định dạng SĐT, Email

		// Cập nhật đối tượng trong bộ nhớ
		selectedCustomer.setTenKH(hoTen);
		selectedCustomer.setSoDT(sdt);
		selectedCustomer.setDiaChi(diaChi);
		selectedCustomer.setEmail(email);
		selectedCustomer.setNgayDangKy(ngayDangKy);
		// Không cho sửa loại KH ở đây (Giữ nguyên loại KH cũ:
		// selectedCustomer.getThanhVien())

		if (khachHangDAO.suaKhachHang(selectedCustomer)) {
			tblKhachHang.refresh(); // Cập nhật hiển thị dòng đã sửa
			showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã cập nhật thông tin khách hàng.");
			// Giữ nguyên dòng đang chọn
		} else {
			showAlert(Alert.AlertType.ERROR, "Lỗi CSDL", "Cập nhật thất bại.");
			// Cân nhắc tải lại dữ liệu nếu lỗi nghiêm trọng: loadDatabaseData();
		}
	}

	/** Xử lý nút Xóa */
	private void handleXoa() {
		entity.KhachHang selectedCustomer = tblKhachHang.getSelectionModel().getSelectedItem();
		if (selectedCustomer == null) {
			showAlert(Alert.AlertType.WARNING, "Chưa chọn", "Vui lòng chọn khách hàng cần xóa.");
			return;
		}

		// Xác nhận trước khi xóa
		Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Bạn có chắc chắn muốn xóa khách hàng '"
				+ selectedCustomer.getTenKH() + "' (Mã: " + selectedCustomer.getMaKH() + ") không?", ButtonType.YES,
				ButtonType.NO);
		confirm.setTitle("Xác nhận xóa");
		confirm.setHeaderText(null);
		Optional<ButtonType> result = confirm.showAndWait();

		if (result.isPresent() && result.get() == ButtonType.YES) {
			if (khachHangDAO.xoaKhachHang(selectedCustomer.getMaKH())) {
				masterCustomerList.remove(selectedCustomer); // Xóa khỏi danh sách gốc
				// FilteredList tự cập nhật
				clearForm(); // Xóa form sau khi xóa thành công
				showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã xóa khách hàng.");
			} else {
				showAlert(Alert.AlertType.ERROR, "Lỗi CSDL",
						"Xóa thất bại. Khách hàng có thể đang có hóa đơn liên quan hoặc lỗi khác.");
			}
		}
	}

	/** Xóa trắng các trường nhập liệu */
	private void clearForm() {
		txtHoTen.clear();
		txtSDT.clear();
		txtDiaChi.clear();
		txtEmail.clear();
		datePickerNgayDangKy.setValue(null);
		tblKhachHang.getSelectionModel().clearSelection(); // Bỏ chọn dòng
		txtHoTen.requestFocus(); // Focus vào ô Họ tên
	}

	/** Hiển thị Dialog lịch sử hóa đơn */
	/**
	 * Hiển thị Dialog lịch sử hóa đơn CÓ THÊM SỰ KIỆN DOUBLE-CLICK
	 */
	private void showInvoiceHistoryDialog(entity.KhachHang customer) {
		List<HoaDon> hoaDonList = hoaDonDAO.getHoaDonByMaKH(customer.getMaKH());
		Dialog<Void> dialog = new Dialog<>();
		dialog.setTitle("Lịch sử hóa đơn - " + customer.getTenKH());
		dialog.setHeaderText("Danh sách hóa đơn của: " + customer.getTenKH() + " (SĐT: " + customer.getSoDT() + ")");

		TableView<HoaDon> historyTable = new TableView<>();
		historyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

		TableColumn<HoaDon, String> colHistMaHD = new TableColumn<>("Mã HĐ");
		colHistMaHD.setCellValueFactory(new PropertyValueFactory<>("maHD"));
		colHistMaHD.setPrefWidth(80);
		TableColumn<HoaDon, String> colHistNgayLap = new TableColumn<>("Ngày Lập");
		colHistNgayLap.setCellValueFactory(cellData -> {
			LocalDateTime dt = cellData.getValue().getNgayLap();
			return new SimpleStringProperty(dt != null ? dt.format(dateTimeFormatter) : "N/A");
		});
		colHistNgayLap.setPrefWidth(150);
		TableColumn<HoaDon, String> colHistPTTT = new TableColumn<>("PTTT");
		colHistPTTT.setCellValueFactory(cellData -> {
			PTTThanhToan pt = cellData.getValue().getHinhThucTT();
			return new SimpleStringProperty(pt != null ? pt.getDisplayName() : "Chưa TT");
		});
		colHistPTTT.setPrefWidth(100);
		TableColumn<HoaDon, Double> colHistTongTien = new TableColumn<>("Tổng Tiền");
		colHistTongTien.setCellValueFactory(new PropertyValueFactory<>("tongTienThanhToan"));
		colHistTongTien.setCellFactory(tc -> new TableCell<>() {
			@Override
			protected void updateItem(Double item, boolean empty) {
				super.updateItem(item, empty);
				setText(empty || item == null ? null : String.format("%,.0f Đ", item));
				setAlignment(Pos.CENTER_RIGHT);
			}
		});
		colHistTongTien.setPrefWidth(120);

		historyTable.getColumns().addAll(colHistMaHD, colHistNgayLap, colHistPTTT, colHistTongTien);
		if (hoaDonList.isEmpty()) {
			historyTable.setPlaceholder(new Label("Khách hàng chưa có hóa đơn."));
		} else {
			historyTable.setItems(FXCollections.observableArrayList(hoaDonList));
		}

		// --- THÊM SỰ KIỆN DOUBLE-CLICK ---
		historyTable.setOnMouseClicked(event -> {
			if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
				HoaDon selectedHoaDon = historyTable.getSelectionModel().getSelectedItem();
				if (selectedHoaDon != null && selectedHoaDon.getMaHD() != null) {
					System.out.println("Double clicked on HD: " + selectedHoaDon.getMaHD());
					// Gọi hàm hiển thị chi tiết hóa đơn
					showInvoiceDetailDialog(selectedHoaDon.getMaHD());
				}
			}
		});
		// --- KẾT THÚC THÊM SỰ KIỆN ---

		VBox dialogLayout = new VBox(10, historyTable);
		dialogLayout.setPadding(new Insets(10));
		VBox.setVgrow(historyTable, Priority.ALWAYS);
		dialog.getDialogPane().setContent(dialogLayout);
		dialog.getDialogPane().setPrefSize(550, 400);
		dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
		dialog.showAndWait();
	}

	// --- HÀM MỚI ĐỂ HIỂN THỊ CHI TIẾT HÓA ĐƠN ---
	/**
	 * Hiển thị Dialog chi tiết hóa đơn dựa vào mã hóa đơn.
	 * 
	 * @param maHD Mã hóa đơn cần hiển thị chi tiết.
	 */
	/**
	 * Hiển thị Dialog chi tiết hóa đơn dựa vào mã hóa đơn. Đã sửa lỗi định dạng
	 * thời gian.
	 */
	private void showInvoiceDetailDialog(String maHD) {
		HoaDon hoaDon = hoaDonDAO.getHoaDonChiTietByMaHD(maHD);
		if (hoaDon == null) {
			showAlert(Alert.AlertType.ERROR, "Lỗi", "Không tìm thấy hóa đơn " + maHD);
			return;
		}
		List<ChiTietHoaDon> chiTietList = hoaDonDAO.getChiTietHoaDon(maHD);

		Dialog<Void> detailDialog = new Dialog<>();
		detailDialog.setTitle("Chi tiết hóa đơn - " + maHD);
		detailDialog.setHeaderText(null);

		// --- Layout chính ---
		VBox mainLayout = new VBox(15);
		mainLayout.setPadding(new Insets(15));
		// ✅ ÁP DỤNG STYLE CSS VÀ STYLE CLASS
		String cssPath = getClass().getResource("/css/HoaDon.css") != null
				? getClass().getResource("/css/HoaDon.css").toExternalForm()
				: null;
		if (cssPath != null) {
			// Nên lấy DialogPane để áp dụng CSS cho toàn bộ cửa sổ (bao gồm cả nền)
			detailDialog.getDialogPane().getStylesheets().add(cssPath);
			// Áp dụng style class cho nội dung bên trong nếu cần (VBox đã có padding)
			mainLayout.getStyleClass().add("right-panel"); // Tái sử dụng style panel
		} else {
			System.err.println("Không tìm thấy file HoaDon.css");
			// Có thể thêm style nền mặc định nếu CSS không load được
			mainLayout.setStyle("-fx-background-color: #FFF3E0; -fx-background-radius: 10;");
		}

		// --- Tiêu đề ---
		Label title = new Label("CHI TIẾT HÓA ĐƠN " + maHD);
		title.getStyleClass().add("screen-title"); // Sử dụng style title
		// ✅ CĂN GIỮA TIÊU ĐỀ
		title.setMaxWidth(Double.MAX_VALUE);
		title.setAlignment(Pos.CENTER);

		// --- Thông tin chung (GridPane) ---
		GridPane infoGrid = new GridPane();
		infoGrid.setHgap(10);
		infoGrid.setVgap(8);
		// ... (Cài đặt ColumnConstraints - giữ nguyên) ...
		ColumnConstraints col1 = new ColumnConstraints();
		col1.setHgrow(Priority.NEVER);
		ColumnConstraints col2 = new ColumnConstraints();
		col2.setHgrow(Priority.ALWAYS);
		ColumnConstraints col3 = new ColumnConstraints();
		col3.setHgrow(Priority.NEVER);
		ColumnConstraints col4 = new ColumnConstraints();
		col4.setHgrow(Priority.ALWAYS);
		infoGrid.getColumnConstraints().addAll(col1, col2, col3, col4);

		Label titleLabel;
		Label valueLabel;
		int rowIndex = 0;
		// ... (Thêm các Label Ngày, SĐT, Bàn, Thu ngân, Giờ vào/ra vào infoGrid - giữ
		// nguyên code) ...
		// Ngày - SĐT Khách
		titleLabel = new Label("Ngày:");
		titleLabel.getStyleClass().add("info-title");
		valueLabel = new Label(hoaDon.getNgayLap() != null ? hoaDon.getNgayLap().format(dateFormatter) : "N/A");
		valueLabel.getStyleClass().add("info-value");
		infoGrid.add(titleLabel, 0, rowIndex);
		infoGrid.add(valueLabel, 1, rowIndex);
		titleLabel = new Label("SĐT Khách:");
		titleLabel.getStyleClass().add("info-title");
		valueLabel = new Label(hoaDon.getSoDienThoaiKH() != null ? hoaDon.getSoDienThoaiKH() : "N/A");
		valueLabel.getStyleClass().add("info-value");
		infoGrid.add(titleLabel, 2, rowIndex);
		infoGrid.add(valueLabel, 3, rowIndex);
		rowIndex++;
		// Bàn - Thu ngân
		titleLabel = new Label("Bàn:");
		titleLabel.getStyleClass().add("info-title");
		valueLabel = new Label(hoaDon.getMaBan() != null ? hoaDon.getMaBan() : "N/A");
		valueLabel.getStyleClass().add("info-value");
		infoGrid.add(titleLabel, 0, rowIndex);
		infoGrid.add(valueLabel, 1, rowIndex);
		titleLabel = new Label("Thu ngân:");
		titleLabel.getStyleClass().add("info-title");
		valueLabel = new Label(hoaDon.getTenNhanVien() != null ? hoaDon.getTenNhanVien() : "N/A");
		valueLabel.getStyleClass().add("info-value");
		infoGrid.add(titleLabel, 2, rowIndex);
		infoGrid.add(valueLabel, 3, rowIndex);
		rowIndex++;
		// Giờ vào - Giờ ra
		titleLabel = new Label("Giờ vào:");
		titleLabel.getStyleClass().add("info-title");
		valueLabel = new Label(
				hoaDon.getGioVao() != null ? hoaDon.getGioVao().toLocalTime().format(timeFormatter) : "N/A");
		valueLabel.getStyleClass().add("info-value");
		infoGrid.add(titleLabel, 0, rowIndex);
		infoGrid.add(valueLabel, 1, rowIndex);
		titleLabel = new Label("Giờ ra:");
		titleLabel.getStyleClass().add("info-title");
		valueLabel = new Label(
				hoaDon.getGioRa() != null ? hoaDon.getGioRa().toLocalTime().format(timeFormatter) : "N/A");
		valueLabel.getStyleClass().add("info-value");
		infoGrid.add(titleLabel, 2, rowIndex);
		infoGrid.add(valueLabel, 3, rowIndex);

		// --- Bảng chi tiết món ăn --- (Giữ nguyên)
		TableView<ChiTietHoaDon> detailTable = new TableView<>();
		// ... (Cài đặt cột và dữ liệu cho detailTable) ...
		detailTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		detailTable.setPrefHeight(180);
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
				setAlignment(Pos.CENTER_RIGHT);
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
				setAlignment(Pos.CENTER_RIGHT);
			}
		});
		detailTable.getColumns().addAll(colSTT, colTenMon, colSoLuong, colDonGia, colThanhTien);
		detailTable.setItems(FXCollections.observableArrayList(chiTietList));
		if (chiTietList.isEmpty())
			detailTable.setPlaceholder(new Label("Không có chi tiết món ăn."));

		// --- Phần Tổng tiền --- (Giữ nguyên)
		VBox totalBox = new VBox(5);
		totalBox.setPadding(new Insets(5, 0, 5, 0));
		double tongMonAn = hoaDon.getTongCongMonAn();
		double phiDichVu = tongMonAn * 0.05;
		double thueVAT = tongMonAn * 0.08;
		double khuyenMai = 0.0;
		totalBox.getChildren().addAll(createTotalRow("Tổng cộng món ăn:", String.format("%,.0f VNĐ", tongMonAn)),
				createTotalRow("Phí dịch vụ (5%):", String.format("%,.0f VNĐ", phiDichVu)),
				createTotalRow("Thuế VAT (8%):", String.format("%,.0f VNĐ", thueVAT)),
				createTotalRow("Tiền đặt cọc bàn:", String.format("%,.0f VNĐ", hoaDon.getTienCoc())),
				createTotalRow("Khuyến mãi:", String.format("%,.0f VNĐ", khuyenMai)));

		// --- Phần Tổng Thanh Toán --- (Giữ nguyên)
		HBox finalTotalBox = new HBox();
		finalTotalBox.setAlignment(Pos.CENTER_RIGHT);
		Label finalTotalLabel = new Label("Tổng tiền thanh toán:");
		finalTotalLabel.getStyleClass().add("total-title");
		Label finalTotalValue = new Label(String.format("%,.0f VNĐ", hoaDon.getTongTienThanhToan()));
		finalTotalValue.getStyleClass().add("total-value");
		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);
		finalTotalBox.getChildren().addAll(finalTotalLabel, spacer, finalTotalValue);

		// --- Thêm vào layout chính ---
		mainLayout.getChildren().addAll(title, infoGrid, detailTable, totalBox, finalTotalBox);

		// --- Cài đặt Dialog ---
		detailDialog.getDialogPane().setContent(mainLayout);
		detailDialog.getDialogPane().setPrefWidth(550);
		detailDialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

		// ✅ Xóa nền mặc định của DialogPane để thấy nền của mainLayout
		detailDialog.getDialogPane().setStyle("-fx-background-color: transparent;");

		detailDialog.showAndWait();
	}

	/** Hàm tiện ích tạo một dòng HBox cho phần tổng tiền */
	private HBox createTotalRow(String labelText, String valueText) {
		HBox hbox = new HBox();
		Label label = new Label(labelText);
		label.getStyleClass().add("info-title");
		Label value = new Label(valueText);
		value.getStyleClass().add("info-value");
		value.setAlignment(Pos.CENTER_RIGHT);
		Region spacer = new Region();
		HBox.setHgrow(spacer, Priority.ALWAYS);
		hbox.getChildren().addAll(label, spacer, value);
		return hbox;
	}

	/** Hiển thị Alert đơn giản */
	private void showAlert(Alert.AlertType type, String title, String content) {
		Alert alert = new Alert(type);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(content);
		alert.showAndWait();
	}
} // End class KhachHang
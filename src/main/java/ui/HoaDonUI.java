package ui;

import dao.HoaDonDAO;
import entity.ChiTietHoaDon;
import entity.HoaDon;
import entity.KhachHang;
import entity.PTTThanhToan;
// === IMPORT MỚI CHO IN TRỰC TIẾP ===
import entity.TaiKhoan; // << MỚI: Để lấy thông tin NV đăng nhập
import javafx.application.Platform; // << MỚI: Để chạy alert từ thread
import org.apache.pdfbox.pdmodel.font.PDFont; // << MỚI
import org.apache.pdfbox.pdmodel.font.PDType1Font; // << MỚI
import org.apache.pdfbox.printing.PDFPrintable; // << MỚI
import org.apache.pdfbox.util.Matrix; // << MỚI
import ui.MainApp; // << MỚI: Để lấy thông tin NV đăng nhập

import javax.print.attribute.HashPrintRequestAttributeSet; // << MỚI
import javax.print.attribute.PrintRequestAttributeSet; // << MỚI
import java.awt.print.PrinterException; // << MỚI
import java.awt.print.PrinterJob; // << MỚI
import java.io.InputStream; // << MỚI
// ======================================

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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat; // Giữ lại cho createReceiptPdf
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.VerticalAlignment;
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

// === CÁC IMPORT CHO Apache PDFBox (Giữ nguyên) ===
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
// =====================================


/**
 * Lớp Controller cho giao diện Quản lý Hóa Đơn (HoaDon.fxml)
 * 🔥 ĐÃ CẬP NHẬT:
 * - Thay thế hàm 'inHoaDonPDF' (lưu file) bằng 'handleInHoaDon' (in trực tiếp).
 * - Logic in mới (createReceiptPdf) tự động lấy tên NV đang đăng nhập từ MainApp.
 * - Đã xóa các hàm helper PDF cũ không còn sử dụng.
 */
public class HoaDonUI {

    // === CÁC THÀNH PHẦN GIAO DIỆN (FXML) ===
    @FXML private DatePicker datePickerFilter;
    @FXML private Button btnClearDate;
    @FXML private ComboBox<String> comboFilter;
    @FXML private TextField txtSearch;
    @FXML private Button btnSearch;

    @FXML private TableView<HoaDon> tableHoaDon;
    @FXML private TableColumn<HoaDon, String> colMaHD;
    @FXML private TableColumn<HoaDon, String> colNgay;
    @FXML private TableColumn<HoaDon, PTTThanhToan> colHinhThucThanhToan;
    @FXML private TableColumn<HoaDon, String> colSoDienThoaiKH;
    @FXML private TableColumn<HoaDon, Double> colTongTien;
    @FXML private TableColumn<HoaDon, Void> colXemChiTiet;

    @FXML private Button btnXuatExcel;
    @FXML private Button btnInHoaDon;

    @FXML private TableView<ChiTietHoaDon> tableChiTietHD;

    // Các Label hiển thị thông tin chi tiết
    @FXML private Label lblMaHDValue;
    @FXML private Label lblMaKHValue;
    @FXML private Label lblNgayValue;
    @FXML private Label lblThuNganValue;
    @FXML private Label lblBanValue;
    @FXML private Label lblGioVaoValue;
    @FXML private Label lblGioRaValue;
    @FXML private Label lblTongCongMonAnValue;
    @FXML private Label lblPhiDichVuValue;
    @FXML private Label lblThueVATValue;
    @FXML private Label lblTienDatCocValue;
    @FXML private Label lblKhuyenMaiValue;
    @FXML private Label lblTongTienThanhToanValue;

    // === BIẾN QUẢN LÝ DỮ LIỆU ===
    private HoaDonDAO hoaDonDAO;
    private ObservableList<HoaDon> allHoaDonList;
    private ObservableList<HoaDon> danhSachHoaDon;
    private ObservableList<ChiTietHoaDon> danhSachChiTietHD;

    // Định dạng ngày giờ
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // Hằng số cho ComboBox
    private final String ALL_METHODS = "Tất cả PT";
    private final String VI_DIEN_TU = PTTThanhToan.VI_DIEN_TU.getDisplayName();
    private final String NGAN_HANG = PTTThanhToan.NGAN_HANG.getDisplayName();
    private final String TIEN_MAT = PTTThanhToan.TIEN_MAT.getDisplayName();

    // === FONT CHO PDF (Đã xóa FONT_PATH cũ) ===
    // (Font sẽ được load bên trong createReceiptPdf)

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

        datePickerFilter.setOnAction(event -> filterData());
        btnClearDate.setOnAction(event -> {
            datePickerFilter.setValue(null);
            filterData();
        });
        comboFilter.setOnAction(event -> filterData());
        btnSearch.setOnAction(event -> filterData());
        txtSearch.setOnAction(event -> filterData());

        loadAndFilterData();

        btnXuatExcel.setOnAction(e -> xuatExcel());
        
        // 🔥 THAY ĐỔI LOGIC NÚT IN
        btnInHoaDon.setOnAction(e -> {
            HoaDon selectedHoaDon = tableHoaDon.getSelectionModel().getSelectedItem();
            if (selectedHoaDon == null) {
                showAlert("Vui lòng chọn một hóa đơn để in.");
                return;
            }
            // Gọi hàm in trực tiếp (lấy từ TraCuu.java)
            handleInHoaDon(selectedHoaDon);
        });

        tableHoaDon.setItems(danhSachHoaDon);

        tableHoaDon.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                hienThiThongTinHoaDon(newSelection);
            } else {
                 hienThiThongTinHoaDon(null);
            }
        });
    }

    // ... (Các hàm setupTableHoaDonColumns, loadAndFilterData, filterData, setupTableChiTietHoaDon, hienThiThongTinHoaDon giữ nguyên) ...
    
    private void setupTableHoaDonColumns() {
        colMaHD.setCellValueFactory(new PropertyValueFactory<>("maHD"));

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
                setText( (empty || item == null) ? null : item.getDisplayName() );
            }
        });

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
        datePickerFilter.setValue(null); // << Reset DatePicker khi tải lại
        filterData();
        if (!danhSachHoaDon.isEmpty()) {
            tableHoaDon.getSelectionModel().selectFirst();
        } else {
             hienThiThongTinHoaDon(null);
        }
    }

    private void filterData() {
        String selectedPaymentMethodDisplay = comboFilter.getValue();
        String searchText = txtSearch.getText().toLowerCase().trim();
        LocalDate selectedDate = datePickerFilter.getValue(); // << Lấy giá trị từ DatePicker

        List<HoaDon> filteredList = allHoaDonList.stream()
            .filter(hd -> {
                boolean paymentMatch = ALL_METHODS.equals(selectedPaymentMethodDisplay) ||
                                       (hd.getHinhThucTT() != null &&
                                        selectedPaymentMethodDisplay.equalsIgnoreCase(hd.getHinhThucTT().getDisplayName()));

                boolean searchMatch = searchText.isEmpty() ||
                                      (hd.getSoDienThoaiKH() != null &&
                                       hd.getSoDienThoaiKH().toLowerCase().contains(searchText));

                // << Logic lọc theo ngày >>
                boolean dateMatch = (selectedDate == null) ||
                                    (hd.getNgayLap() != null &&
                                     hd.getNgayLap().toLocalDate().equals(selectedDate));

                return paymentMatch && searchMatch && dateMatch ; // << Thêm dateMatch vào điều kiện
            })
            .collect(Collectors.toList());

        danhSachHoaDon.setAll(filteredList);

         if (danhSachHoaDon.isEmpty()) {
             hienThiThongTinHoaDon(null);
         } else {
            // Cố gắng giữ lại lựa chọn cũ nếu có thể, nếu không thì chọn dòng đầu
             HoaDon selected = tableHoaDon.getSelectionModel().getSelectedItem();
             if (selected != null && danhSachHoaDon.contains(selected)) {
                 tableHoaDon.getSelectionModel().select(selected);
             } else {
                 tableHoaDon.getSelectionModel().selectFirst();
             }
         }
    }

    private void setupTableChiTietHoaDon() {
        tableChiTietHD.getColumns().clear();
        TableColumn<ChiTietHoaDon, Void> colSTT = new TableColumn<>("STT");
        colSTT.setPrefWidth(40); colSTT.setSortable(false);
        colSTT.setCellFactory(col -> new TableCell<>() {
            @Override public void updateIndex(int index) { super.updateIndex(index); setText(isEmpty() || index < 0 ? null : Integer.toString(index + 1)); }
        });
        TableColumn<ChiTietHoaDon, String> colTenMon = new TableColumn<>("Tên món");
        colTenMon.setCellValueFactory(new PropertyValueFactory<>("tenMon")); colTenMon.setPrefWidth(150);
        TableColumn<ChiTietHoaDon, Integer> colSoLuong = new TableColumn<>("Số lượng");
        colSoLuong.setCellValueFactory(new PropertyValueFactory<>("soLuong")); colSoLuong.setPrefWidth(70);
        TableColumn<ChiTietHoaDon, Double> colDonGia = new TableColumn<>("Đơn giá");
        colDonGia.setCellValueFactory(new PropertyValueFactory<>("donGia")); colDonGia.setPrefWidth(90);
        colDonGia.setCellFactory(column -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) { super.updateItem(item, empty); setText(empty || item == null ? null : String.format("%,.0f", item)); }
        });
        TableColumn<ChiTietHoaDon, Double> colThanhTien = new TableColumn<>("Thành tiền");
        colThanhTien.setCellValueFactory(new PropertyValueFactory<>("thanhTien")); colThanhTien.setPrefWidth(100);
        colThanhTien.setCellFactory(column -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) { super.updateItem(item, empty); setText(empty || item == null ? null : String.format("%,.0f", item)); }
        });
        tableChiTietHD.getColumns().addAll(colSTT, colTenMon, colSoLuong, colDonGia, colThanhTien);
        tableChiTietHD.setItems(danhSachChiTietHD);
    }

    private void hienThiThongTinHoaDon(HoaDon hoaDon) {
         if (hoaDon == null) {
            lblMaHDValue.setText("..."); lblMaKHValue.setText("..."); lblNgayValue.setText("...");
            lblThuNganValue.setText("..."); lblBanValue.setText("..."); lblGioVaoValue.setText("...");
            lblGioRaValue.setText("..."); lblTongCongMonAnValue.setText("0 VNĐ"); lblPhiDichVuValue.setText("0 VNĐ");
            lblThueVATValue.setText("0 VNĐ"); lblTienDatCocValue.setText("0 VNĐ"); lblKhuyenMaiValue.setText("0 VNĐ");
            lblTongTienThanhToanValue.setText("0 VNĐ");
            danhSachChiTietHD.clear();
            return;
        }

        lblMaHDValue.setText(hoaDon.getMaHD());
        lblMaKHValue.setText(hoaDon.getSoDienThoaiKH() != null ? hoaDon.getSoDienThoaiKH() : "N/A");
        lblNgayValue.setText(hoaDon.getNgayLap() != null ? hoaDon.getNgayLap().format(dateFormatter) : "N/A");
        lblThuNganValue.setText(hoaDon.getTenNhanVien() != null ? hoaDon.getTenNhanVien() : "N/A");
        lblBanValue.setText(hoaDon.getMaBan() != null ? hoaDon.getMaBan() : "N/A");
        lblGioVaoValue.setText(hoaDon.getGioVao() != null ? hoaDon.getGioVao().format(timeFormatter) : "N/A");
        lblGioRaValue.setText(hoaDon.getGioRa() != null ? hoaDon.getGioRa().format(timeFormatter) : "N/A");

        lblTongCongMonAnValue.setText(String.format("%,.0f VNĐ", hoaDon.getTongCongMonAn()));
        lblPhiDichVuValue.setText(String.format("%,.0f VNĐ", hoaDon.getPhiDichVu()));
        lblThueVATValue.setText(String.format("%,.0f VNĐ", hoaDon.getThueVAT()));
        lblTienDatCocValue.setText(String.format("%,.0f VNĐ", hoaDon.getTienCoc()));
        lblKhuyenMaiValue.setText(String.format("%,.0f VNĐ", hoaDon.getKhuyenMai()));
        lblTongTienThanhToanValue.setText(String.format("%,.0f VNĐ", hoaDon.getTongTienThanhToan()));

        danhSachChiTietHD.setAll(hoaDonDAO.getChiTietHoaDon(hoaDon.getMaHD()));
    }
    

    // ... (Hàm xuatExcel và các hàm helper của nó (createHeaderStyle, createCurrencyStyle, v.v.) giữ nguyên) ...
    
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

                CellStyle headerStyle = createHeaderStyle(workbook);
                CellStyle currencyStyle = createCurrencyStyle(workbook);
                CellStyle dateTimeStyle = createDateTimeStyle(workbook);
                CellStyle basicStyle = createBasicCellStyle(workbook);

                String[] columns = {"Mã HĐ", "Ngày Lập", "PT Thanh Toán", "Trạng Thái", "SĐT Khách", "Thu Ngân", "Bàn", "Giờ Vào", "Giờ Ra", "Tiền Cọc", "Tổng Tiền Món", "Tổng Thanh Toán"};
                Row headerRow = sheet.createRow(0);
                for (int i = 0; i < columns.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(columns[i]);
                    cell.setCellStyle(headerStyle);
                }

                int rowNum = 1;
                for (HoaDon hd : dataToExport) {
                    Row row = sheet.createRow(rowNum++);
                    createCell(row, 0, hd.getMaHD(), basicStyle);
                    createCell(row, 1, hd.getNgayLap(), dateTimeStyle);
                    createCell(row, 2, (hd.getHinhThucTT() != null ? hd.getHinhThucTT().getDisplayName() : ""), basicStyle);
                    createCell(row, 3, (hd.getTrangThai() != null ? hd.getTrangThai().getDisplayName() : ""), basicStyle);
                    createCell(row, 4, hd.getSoDienThoaiKH(), basicStyle);
                    createCell(row, 5, hd.getTenNhanVien(), basicStyle);
                    createCell(row, 6, hd.getMaBan(), basicStyle);
                    createCell(row, 7, hd.getGioVao(), dateTimeStyle);
                    createCell(row, 8, hd.getGioRa(), dateTimeStyle);
                    createCell(row, 9, hd.getTienCoc(), currencyStyle);
                    createCell(row, 10, hd.getTongCongMonAn(), currencyStyle);
                    createCell(row, 11, hd.getTongTienThanhToan(), currencyStyle);
                }

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

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont(); font.setBold(true); font.setFontHeightInPoints((short) 12); style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex()); style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER); style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN); style.setBorderTop(BorderStyle.THIN); style.setBorderLeft(BorderStyle.THIN); style.setBorderRight(BorderStyle.THIN);
        return style;
    }
    private CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = createBasicCellStyle(workbook);
        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0\" VNĐ\""));
        return style;
    }
    private CellStyle createDateTimeStyle(Workbook workbook) {
        CellStyle style = createBasicCellStyle(workbook);
        CreationHelper createHelper = workbook.getCreationHelper();
        style.setDataFormat(createHelper.createDataFormat().getFormat("dd/MM/yyyy HH:mm"));
        return style;
    }
    private CellStyle createBasicCellStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN); style.setBorderTop(BorderStyle.THIN); style.setBorderLeft(BorderStyle.THIN); style.setBorderRight(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private void createCell(Row row, int column, Object value, CellStyle style) {
        Cell cell = row.createCell(column);
        if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof Double) {
            cell.setCellValue((Double) value);
        } else if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        } else if (value instanceof LocalDateTime) {
             cell.setCellValue((LocalDateTime) value);
        } else if (value instanceof LocalDate) {
             cell.setCellValue((LocalDate) value);
        } else if (value == null) {
            cell.setBlank();
        }
        if (style != null) {
            cell.setCellStyle(style);
        }
    }

    // === CÁC HÀM TIỆN ÍCH CHO PDFBox (ĐÃ XÓA) ===
    // ... (Đã xóa các hàm drawTextLeft, drawTextRight, drawLine, v.v. cũ) ...


    // === HÀM IN HÓA ĐƠN (PDF) - (ĐÃ XÓA HÀM CŨ) ===
    
    // === 🔥 HÀM MỚI: HELPER CLASS CHO VỊ TRÍ Y (Copy từ TraCuu) ===
    private static class YPosition {
        public float y;
        public YPosition(float initialY) {
            this.y = initialY;
        }
    }

    // === 🔥 HÀM MỚI: XỬ LÝ IN (Copy từ TraCuu) ===
    private void handleInHoaDon(HoaDon hdDisplay) {
        if (hdDisplay == null || hdDisplay.getMaHD() == null) {
            showAlert("Lỗi", "Không có hóa đơn hợp lệ để in.");
            return;
        }
        
        // QUAN TRỌNG: Lấy lại chi tiết đầy đủ từ DAO
        HoaDon hd = hoaDonDAO.getHoaDonChiTietByMaHD(hdDisplay.getMaHD());
        if (hd == null) {
            // Dùng Platform.runLater nếu hàm này được gọi từ thread khác
             Platform.runLater(() -> showAlert("Lỗi", "Không thể tải chi tiết hóa đơn từ database."));
            return;
        }
        
        // Chạy trong Thread riêng để tránh treo UI
        new Thread(() -> {
            PDDocument document = null;
            try {
                // Tạo document PDF với dữ liệu đầy đủ
                document = createReceiptPdf(hd);
                
                // Lấy Job in từ AWT/Swing
                PrinterJob job = PrinterJob.getPrinterJob();
                PrintRequestAttributeSet attr = new HashPrintRequestAttributeSet();
                
                // Mở hộp thoại chọn máy in
                if (job.printDialog(attr)) {
                    java.awt.print.PageFormat pageFormat = job.getPageFormat(attr);
                    
                    // Cài đặt nội dung in (PDFPrintable)
                    org.apache.pdfbox.printing.PDFPrintable printableData = 
                        new org.apache.pdfbox.printing.PDFPrintable(document);
                    
                    job.setPrintable(printableData, pageFormat);
                    
                    // Gửi lệnh in
                    job.print(attr); 
                    
                    Platform.runLater(() -> {
                        showAlert("Đã gửi lệnh in cho Hóa đơn " + hd.getMaHD() + " thành công.");
                    });
                } else {
                    Platform.runLater(() -> showAlert("Đã hủy thao tác in."));
                }

            } catch (java.awt.print.PrinterException e) {
                // Sửa lỗi: dùng showAlert
                Platform.runLater(() -> showAlert("Lỗi trong quá trình in ấn: " + e.getMessage()));
            } catch (Exception e) {
                e.printStackTrace();
                // Sửa lỗi: dùng showAlert
                Platform.runLater(() -> showAlert("Lỗi khi tạo PDF: " + e.getMessage()));
            } finally {
                if (document != null) {
                    try {
                        document.close();
                    } catch (IOException ignored) {}
                }
            }
        }).start();
    }

    // === 🔥 HÀM MỚI: TẠO PDF (Copy từ TraCuu) ===
    // Hàm này lấy tên NV từ MainApp.getLoggedInUser()
    private PDDocument createReceiptPdf(HoaDon hd) throws IOException {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        final float PAGE_WIDTH = page.getMediaBox().getWidth();
        final float MARGIN = 72; 
        final float Y_START = page.getMediaBox().getHeight() - MARGIN;
        final float LINE_HEIGHT = 16; 
        
        final YPosition pos = new YPosition(Y_START); 

        // SỬA PHẦN LOAD FONT (Copy từ TraCuu.java - đã fix lỗi)
        PDFont font = PDType1Font.HELVETICA;
        PDFont fontBold = PDType1Font.HELVETICA_BOLD;

        try {
            // Sử dụng getClass().getResourceAsStream để lấy font từ resources
            InputStream fontStream = getClass().getResourceAsStream("/fonts/UTM Avo.ttf");
            if (fontStream != null) {
                try {
                    font = PDType0Font.load(document, fontStream);
                } finally {
                    fontStream.close();
                }
            }
            
            InputStream fontBoldStream = getClass().getResourceAsStream("/fonts/UTM AvoBold.ttf");
            if (fontBoldStream != null) {
                try {
                    fontBold = PDType0Font.load(document, fontBoldStream);
                } finally {
                    fontBoldStream.close();
                }
            }
        } catch (Exception e) {
            // Nếu lỗi, dùng font mặc định Helvetica
            System.err.println("Sử dụng font mặc định do không load được font tùy chỉnh: " + e.getMessage());
            font = PDType1Font.HELVETICA;
            fontBold = PDType1Font.HELVETICA_BOLD;
        }
        
        // Lấy chi tiết hóa đơn
        List<ChiTietHoaDon> monAnList = hoaDonDAO.getChiTietHoaDon(hd.getMaHD());
        
        final DecimalFormat currencyFormatter = new DecimalFormat("###,###");
        final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy"); // Đổi tên biến để tránh trùng lặp
        final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm"); // Đổi tên biến

        // Chuẩn bị dữ liệu hiển thị
        String banStr = hd.getMaBan() != null ? hd.getMaBan() : "N/A";
        String ngayStr = hd.getNgayLap() != null ? hd.getNgayLap().toLocalDate().format(dateFormatter) : "N/A";
        String gioVaoStr = hd.getGioVao() != null ? hd.getGioVao().toLocalTime().format(timeFormatter) : "N/A";
        String gioRaStr = hd.getGioRa() != null ? hd.getGioRa().toLocalTime().format(timeFormatter) : "N/A";
        
        // === 🔥 LOGIC LẤY TÊN NV ĐĂNG NHẬP ===
        String tenThuNgan = "N/A";
        try {
            TaiKhoan tk = MainApp.getLoggedInUser();
            if (tk != null && tk.getNhanVien() != null && tk.getNhanVien().getHoTen() != null) {
                tenThuNgan = tk.getNhanVien().getHoTen(); 
            } else if (hd.getTenNhanVien() != null) {
                 // Dự phòng: Lấy tên nhân viên từ hóa đơn nếu không lấy được từ MainApp
                 tenThuNgan = hd.getTenNhanVien();
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy tên nhân viên đăng nhập: " + e.getMessage());
            if (hd.getTenNhanVien() != null) {
                 tenThuNgan = hd.getTenNhanVien(); // Dự phòng
            }
        }
        String thuNganStr = tenThuNgan;
        // ======================================
        
        String khachHangStr = hd.getSoDienThoaiKH() != null ? hd.getSoDienThoaiKH() : "N/A";
        String hinhThucTTStr = hd.getHinhThucTT() != null ? hd.getHinhThucTT().getDisplayName() : "N/A";

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
        
            // === 1. HEADER - TÊN NHÀ HÀNG ===
            contentStream.beginText();
            contentStream.setFont(fontBold, 14); 
            float titleWidth = fontBold.getStringWidth("NHÀ HÀNG TỨ HỮU") / 1000 * 14;
            contentStream.newLineAtOffset((PAGE_WIDTH - titleWidth) / 2, pos.y);
            contentStream.showText("NHÀ HÀNG TỨ HỮU");
            contentStream.endText();
            pos.y -= LINE_HEIGHT * 1.5f;
            
            // 1.2 Địa chỉ
            contentStream.beginText();
            contentStream.setFont(font, 10);
            String address = "Địa chỉ: 77 Hồ Tùng Mậu, Phường Châu Đốc, An Giang";
            float addressWidth = font.getStringWidth(address) / 1000 * 10;
            contentStream.newLineAtOffset((PAGE_WIDTH - addressWidth) / 2, pos.y);
            contentStream.showText(address);
            contentStream.endText();
            pos.y -= LINE_HEIGHT;

            // 1.3 SĐT
            contentStream.beginText();
            contentStream.setFont(font, 10);
            String sdt = "SĐT: 0909 123 456";
            float sdtWidth = font.getStringWidth(sdt) / 1000 * 10;
            contentStream.newLineAtOffset((PAGE_WIDTH - sdtWidth) / 2, pos.y);
            contentStream.showText(sdt);
            contentStream.endText();
            pos.y -= LINE_HEIGHT * 1.5f;

            // === 2. TIÊU ĐỀ HÓA ĐƠN ===
            contentStream.beginText();
            contentStream.setFont(fontBold, 16);
            titleWidth = fontBold.getStringWidth("HÓA ĐƠN THANH TOÁN") / 1000 * 16;
            contentStream.newLineAtOffset((PAGE_WIDTH - titleWidth) / 2, pos.y);
            contentStream.showText("HÓA ĐƠN THANH TOÁN");
            contentStream.endText();
            pos.y -= LINE_HEIGHT * 1.2f;

            contentStream.beginText();
            contentStream.setFont(fontBold, 12);
            String maHDLabel = "Số HĐ: " + (hd.getMaHD() != null ? hd.getMaHD() : "N/A");
            float maHDWidth = fontBold.getStringWidth(maHDLabel) / 1000 * 12;
            contentStream.newLineAtOffset((PAGE_WIDTH - maHDWidth) / 2, pos.y);
            contentStream.showText(maHDLabel);
            contentStream.endText();
            pos.y -= LINE_HEIGHT * 1.8f;

            // === 3. THÔNG TIN CHUNG (2 CỘT) ===
            final float COL_SEP = (PAGE_WIDTH - 2 * MARGIN) / 2;
            final float FONT_SIZE_INFO = 10;
            final float COL_1_START = MARGIN;
            final float COL_2_START = MARGIN + COL_SEP;

            contentStream.setFont(font, FONT_SIZE_INFO);
            
            // Dòng 1: Hiển thị tên thu ngân đã lấy
            contentStream.beginText();
            contentStream.newLineAtOffset(COL_1_START, pos.y);
            contentStream.showText("Bàn: " + banStr);
            contentStream.setTextMatrix(org.apache.pdfbox.util.Matrix.getTranslateInstance(COL_2_START, pos.y));
            contentStream.showText("Thu ngân: " + thuNganStr); // << SỬ DỤNG TÊN NV MỚI LẤY
            contentStream.endText();
            pos.y -= LINE_HEIGHT * 0.9f;

            // Dòng 2
            contentStream.beginText();
            contentStream.newLineAtOffset(COL_1_START, pos.y);
            contentStream.showText("Ngày: " + ngayStr);
            contentStream.setTextMatrix(org.apache.pdfbox.util.Matrix.getTranslateInstance(COL_2_START, pos.y));
            contentStream.showText("Khách hàng: " + khachHangStr);
            contentStream.endText();
            pos.y -= LINE_HEIGHT * 0.9f;

            // Dòng 3
            contentStream.beginText();
            contentStream.newLineAtOffset(COL_1_START, pos.y);
            contentStream.showText("Giờ vào: " + gioVaoStr);
            contentStream.setTextMatrix(org.apache.pdfbox.util.Matrix.getTranslateInstance(COL_2_START, pos.y));
            contentStream.showText("Giờ ra: " + gioRaStr);
            contentStream.endText();
            pos.y -= LINE_HEIGHT * 1.5f;

            // === 4. BẢNG MÓN ĂN ===
            final float FONT_SIZE_TABLE = 9; 
            float colSTT = MARGIN;                    
            float colTenMon = MARGIN + 30;           
            float colSL = PAGE_WIDTH - MARGIN - 180;  
            float colDonGia = PAGE_WIDTH - MARGIN - 110; 
            float colThanhTien = PAGE_WIDTH - MARGIN - 30; 

            // Tiêu đề cột
            contentStream.beginText();
            contentStream.setFont(fontBold, FONT_SIZE_TABLE);
            contentStream.newLineAtOffset(colSTT, pos.y);
            contentStream.showText("STT");
            contentStream.setTextMatrix(org.apache.pdfbox.util.Matrix.getTranslateInstance(colTenMon, pos.y));
            contentStream.showText("Tên món");
            contentStream.setTextMatrix(org.apache.pdfbox.util.Matrix.getTranslateInstance(colSL, pos.y));
            contentStream.showText("SL");
            contentStream.setTextMatrix(org.apache.pdfbox.util.Matrix.getTranslateInstance(colDonGia, pos.y));
            contentStream.showText("Đơn giá (VNĐ)");
            contentStream.setTextMatrix(org.apache.pdfbox.util.Matrix.getTranslateInstance(colThanhTien, pos.y));
            contentStream.showText("Thành tiền");
            contentStream.endText();
            
            // Dữ liệu món ăn
            contentStream.setFont(font, FONT_SIZE_TABLE);
            
            float currentY = pos.y - LINE_HEIGHT * 1.2f; 
            
            for (int i = 0; i < monAnList.size(); i++) {
                ChiTietHoaDon mon = monAnList.get(i);
                
                contentStream.beginText();
                
                // Cột STT
                contentStream.newLineAtOffset(colSTT, currentY);
                contentStream.showText(String.valueOf(i + 1));
                
                // Cột Tên món
                contentStream.setTextMatrix(org.apache.pdfbox.util.Matrix.getTranslateInstance(colTenMon, currentY));
                contentStream.showText(mon.getTenMon());
                
                // Cột SL (Căn phải)
                String slStr = String.valueOf(mon.getSoLuong());
                float slWidth = font.getStringWidth(slStr) / 1000 * FONT_SIZE_TABLE;
                contentStream.setTextMatrix(org.apache.pdfbox.util.Matrix.getTranslateInstance(colSL - slWidth + 15, currentY)); 
                contentStream.showText(slStr);

                // Cột Đơn giá (Căn phải)
                String dgStr = currencyFormatter.format(mon.getDonGia());
                float dgWidth = font.getStringWidth(dgStr) / 1000 * FONT_SIZE_TABLE;
                contentStream.setTextMatrix(org.apache.pdfbox.util.Matrix.getTranslateInstance(colDonGia - dgWidth + 15, currentY)); 
                contentStream.showText(dgStr);
                
                // Cột Thành tiền (Căn phải)
                String ttStr = currencyFormatter.format(mon.getThanhTien());
                float ttWidth = font.getStringWidth(ttStr) / 1000 * FONT_SIZE_TABLE;
                contentStream.setTextMatrix(org.apache.pdfbox.util.Matrix.getTranslateInstance(colThanhTien - ttWidth + 15, currentY)); 
                contentStream.showText(ttStr);
                
                contentStream.endText();
                
                currentY -= LINE_HEIGHT * 1.6f; 
            }
            
            pos.y = currentY + LINE_HEIGHT * 1.6f; 
            pos.y -= LINE_HEIGHT * 1.0f;

            // === 5. TỔNG KẾT CHI TIẾT ===
            final float FONT_SIZE_SUMMARY = 10;
            final float SUMMARY_INDENT = MARGIN;
            final float SUMMARY_VALUE_COL = PAGE_WIDTH - MARGIN;
            
            class SummaryDrawer {
                private final PDFont regularFont;
                private final PDFont boldFont;
                
                SummaryDrawer(final PDFont regularFont, final PDFont boldFont) {
                    this.regularFont = regularFont;
                    this.boldFont = boldFont;
                }
                
                void draw(String label, String value, boolean isBold, boolean isTotal) throws IOException {
                    float currentFontSize = isTotal ? 12 : FONT_SIZE_SUMMARY; 
                    PDFont currentFont = isBold ? boldFont : regularFont;
                    
                    // 1. Vẽ Label
                    contentStream.beginText();
                    contentStream.setFont(currentFont, currentFontSize);
                    contentStream.newLineAtOffset(SUMMARY_INDENT, pos.y);
                    contentStream.showText(label);
                    contentStream.endText();
                    
                    // 2. Vẽ Value (Căn phải)
                    float valueWidth = currentFont.getStringWidth(value) / 1000 * currentFontSize;
                    contentStream.beginText();
                    contentStream.setFont(currentFont, currentFontSize);
                    contentStream.newLineAtOffset(SUMMARY_VALUE_COL - valueWidth, pos.y); 
                    contentStream.showText(value);
                    contentStream.endText();
                    
                    pos.y -= LINE_HEIGHT * (isTotal ? 1.4f : 1.1f); 
                }
            }

            SummaryDrawer drawer = new SummaryDrawer(font, fontBold);
            
            // 5.1. Các dòng tính toán
            drawer.draw("Tổng cộng món ăn:", currencyFormatter.format(hd.getTongCongMonAn()) + " VNĐ", true, false); 
            drawer.draw("Phí dịch vụ (5%):", currencyFormatter.format(hd.getPhiDichVu()) + " VNĐ", false, false);
            drawer.draw("Thuế VAT (8%):", currencyFormatter.format(hd.getThueVAT()) + " VNĐ", false, false);
            drawer.draw("Tiền đặt cọc bàn:", "-" + currencyFormatter.format(hd.getTienCoc()) + " VNĐ", false, false);
            
            // 5.2. Đường kẻ phân chia
            pos.y += LINE_HEIGHT * 0.5f;
            contentStream.setLineWidth(0.5f);
            contentStream.moveTo(SUMMARY_INDENT, pos.y);
            contentStream.lineTo(SUMMARY_VALUE_COL, pos.y);
            contentStream.stroke();
            pos.y -= LINE_HEIGHT * 1.2f;

            // 5.3. Vẽ "Tổng thanh toán"
            drawer.draw("Tổng thanh toán:", currencyFormatter.format(hd.getTongTienThanhToan()) + " VNĐ", true, true);

            pos.y -= LINE_HEIGHT * 0.5f;

            // Đường kẻ dưới tổng thanh toán
            contentStream.moveTo(SUMMARY_INDENT, pos.y);
            contentStream.lineTo(SUMMARY_VALUE_COL, pos.y);
            contentStream.stroke();
            pos.y -= LINE_HEIGHT * 1.2f;
            
            // 5.4. Chi tiết Thanh toán, ưu đãi, Khách trả
            String khachHangMemberDetails = "Khách vãng lai"; 
            if (hd.getKhachHang() != null && hd.getKhachHang().getThanhVien() != null) {
                khachHangMemberDetails = hd.getKhachHang().getThanhVien();
            }
            
            String uuDaiStr = (hd.getKhuyenMai() > 0) 
                ? ("-" + currencyFormatter.format(hd.getKhuyenMai()) + " VNĐ") 
                : "0 VNĐ";
            
            double soTienKhachTra = hd.getTongTienThanhToan() + hd.getKhuyenMai(); 

            drawer.draw("Hình thức thanh toán:", hinhThucTTStr, false, false);
            drawer.draw("Khách hàng thành viên:", khachHangMemberDetails, false, false);
            drawer.draw("Ưu đãi áp dụng:", uuDaiStr, false, false);
            
            // Số tiền khách trả
            drawer.draw("Số tiền khách trả:", currencyFormatter.format(soTienKhachTra) + " VNĐ", true, false); 
            
            // Đường kẻ cuối cùng
            pos.y -= LINE_HEIGHT * 0.2f;
            contentStream.moveTo(SUMMARY_INDENT, pos.y);
            contentStream.lineTo(SUMMARY_VALUE_COL, pos.y);
            contentStream.stroke();
            pos.y -= LINE_HEIGHT * 2.5f;

            // === 6. FOOTER ===
            contentStream.beginText();
            contentStream.setFont(font, 10);
            String footer = "Nhà hàng Tứ Hữu xin cám ơn và hẹn gặp lại!";
            float footerWidth = font.getStringWidth(footer) / 1000 * 10;
            contentStream.newLineAtOffset((PAGE_WIDTH - footerWidth) / 2, pos.y);
            contentStream.showText(footer);
            contentStream.endText();
        }

        return document;
    }

 // === HÀM HIỂN THỊ THÔNG BÁO CHUNG ===
    private void showAlert(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Thông báo");
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    // === HÀM HIỂN THỊ THÔNG BÁO LỖI CÓ TIÊU ĐỀ RIÊNG ===
    private void showAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

}
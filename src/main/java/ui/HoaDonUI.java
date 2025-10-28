package ui;

import dao.HoaDonDAO;
import entity.ChiTietHoaDon;
import entity.HoaDon;
import entity.PTTThanhToan;
// Import thêm entity KhachHang nếu cần lấy thông tin thành viên
import entity.KhachHang;
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
import java.text.DecimalFormat; // Import để format tiền tệ
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

// === CÁC IMPORT CHO Apache PDFBox ===
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
// =====================================


/**
 * Lớp Controller cho giao diện Quản lý Hóa Đơn (HoaDon.fxml)
 * Đã áp dụng layout PDF mới từ hình ảnh.
 * Sử dụng font NotoSans-Bold.ttf.
 * Đã sửa lỗi căn chỉnh PDF và loại bỏ các lớp helper phức tạp.
 * Đã chỉnh summary label sang căn trái.
 * Đã thêm lại DatePicker và nút X.
 */
public class HoaDonUI {

    // === CÁC THÀNH PHẦN GIAO DIỆN (FXML) ===
    @FXML private DatePicker datePickerFilter; // << Thêm lại @FXML
    @FXML private Button btnClearDate;         // << Thêm lại @FXML
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

    // === FONT CHO PDF ===
    public static final String FONT_PATH = "src/main/resources/fonts/NotoSans-Bold.ttf";

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

        // << Logic cho bộ lọc (Kích hoạt lại DatePicker và nút X) >>
        datePickerFilter.setOnAction(event -> filterData());
        btnClearDate.setOnAction(event -> { // << Thêm lại logic nút X
            datePickerFilter.setValue(null);
            filterData();
        });
        comboFilter.setOnAction(event -> filterData());
        btnSearch.setOnAction(event -> filterData());
        txtSearch.setOnAction(event -> filterData());

        loadAndFilterData();

        btnXuatExcel.setOnAction(e -> xuatExcel());
        btnInHoaDon.setOnAction(e -> inHoaDonPDF()); // Gọi hàm in PDF mới

        tableHoaDon.setItems(danhSachHoaDon);

        // Listener cho việc chọn dòng
        tableHoaDon.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                hienThiThongTinHoaDon(newSelection);
            } else {
                 hienThiThongTinHoaDon(null);
            }
        });
    }

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


    // === CÁC HÀM TIỆN ÍCH CHO PDFBox (Giữ nguyên) ===

    private String formatCurrencyVND(double amount) {
        DecimalFormat formatter = new DecimalFormat("###,###");
        return formatter.format(amount) + " VNĐ";
    }

    private float drawTextLeft(PDPageContentStream stream, PDType0Font font, int fontSize,
                               float x, float y, String text) throws IOException {
        stream.beginText();
        stream.setFont(font, fontSize);
        stream.newLineAtOffset(x, y);
        stream.showText(text != null ? text : "");
        stream.endText();
        return y;
    }

    private float drawTextRight(PDPageContentStream stream, PDType0Font font, int fontSize,
                                float x_right, float y, String text) throws IOException {
        if (text == null) text = "";
        float textWidth = (font.getStringWidth(text) / 1000.0f) * fontSize;
        float x = x_right - textWidth;

        stream.beginText();
        stream.setFont(font, fontSize);
        stream.newLineAtOffset(x, y);
        stream.showText(text);
        stream.endText();
        return y;
    }

    private float drawTextCenter(PDPageContentStream stream, PDType0Font font, int fontSize,
                                 float x_center, float y, String text) throws IOException {
        if (text == null) text = "";
        float textWidth = (font.getStringWidth(text) / 1000.0f) * fontSize;
        float x = x_center - (textWidth / 2.0f);

        stream.beginText();
        stream.setFont(font, fontSize);
        stream.newLineAtOffset(x, y);
        stream.showText(text);
        stream.endText();
        return y;
    }

    private float drawCenteredTextPage(PDPageContentStream stream, PDType0Font font, int fontSize,
                                       float y, String text, float pageWidth, float lineHeight) throws IOException {
        if (text == null) text = "";
        float textWidth = (font.getStringWidth(text) / 1000.0f) * fontSize;
        float x = (pageWidth - textWidth) / 2.0f;

        stream.beginText();
        stream.setFont(font, fontSize);
        stream.newLineAtOffset(x, y);
        stream.showText(text);
        stream.endText();
        return y - lineHeight;
    }

    private float drawSummaryRow(PDPageContentStream stream, PDType0Font fontLabel, PDType0Font fontValue, int fontSize,
                                 float y, String label, String value,
                                 float labelAlignLeftX, float valueAlignRightX, float lineHeight) throws IOException {
        drawTextLeft(stream, fontLabel, fontSize, labelAlignLeftX, y, label);
        drawTextRight(stream, fontValue, fontSize, valueAlignRightX, y, value);
        return y - lineHeight;
    }

    private void drawLine(PDPageContentStream stream, float y, float startX, float endX, float gapBefore) throws IOException {
        float lineY = y + gapBefore;
        stream.moveTo(startX, lineY);
        stream.lineTo(endX, lineY);
        stream.setStrokingColor(0.7f, 0.7f, 0.7f);
        stream.setLineWidth(0.5f);
        stream.stroke();
        stream.setStrokingColor(0, 0, 0);
    }


    // === HÀM IN HÓA ĐƠN (PDF) - Giữ nguyên ===

    private void inHoaDonPDF() {
        HoaDon selectedHoaDon = tableHoaDon.getSelectionModel().getSelectedItem();
        if (selectedHoaDon == null) {
            showAlert("Vui lòng chọn một hóa đơn để in.");
            return;
        }

        ObservableList<ChiTietHoaDon> chiTietList = danhSachChiTietHD;
        if (chiTietList == null || chiTietList.isEmpty()) {
             List<ChiTietHoaDon> fallbackList = hoaDonDAO.getChiTietHoaDon(selectedHoaDon.getMaHD());
             if (fallbackList.isEmpty()) {
                showAlert("Hóa đơn này không có chi tiết món ăn để in.");
                return;
             }
             chiTietList = FXCollections.observableArrayList(fallbackList);
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Lưu file PDF Hóa Đơn");
        fileChooser.setInitialFileName("HoaDon_" + selectedHoaDon.getMaHD() + ".pdf");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("PDF files (*.pdf)", "*.pdf");
        fileChooser.getExtensionFilters().add(extFilter);
        Stage stage = (Stage) tableHoaDon.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);
        if (file == null) {
            System.out.println("Hủy thao tác lưu file PDF.");
            return;
        }

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDType0Font font;
            try {
                 font = PDType0Font.load(document, new File(FONT_PATH));
            } catch (IOException e) {
                showAlert("Lỗi: Không thể tải file font tại:\n" + FONT_PATH + "\nVui lòng kiểm tra lại.");
                e.printStackTrace();
                return;
            }
            PDType0Font fontBold = font;

            final float PAGE_WIDTH = page.getMediaBox().getWidth();
            final float MARGIN = 72;
            final float CONTENT_WIDTH = PAGE_WIDTH - 2 * MARGIN;
            final float Y_START = page.getMediaBox().getHeight() - MARGIN;
            float y = Y_START;
            final float LINE_HEIGHT_S = 12f;
            final float LINE_HEIGHT_M = 14f;
            final float LINE_HEIGHT_L = 18f;
            final float LINE_HEIGHT_XL = 22f;
            final float GAP = 6f;

            final DecimalFormat currencyFormatter = new DecimalFormat("###,###");
            final DateTimeFormatter pdfDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            final DateTimeFormatter pdfTimeFormatter = DateTimeFormatter.ofPattern("HH:mm");

            String banStr = selectedHoaDon.getMaBan() != null ? selectedHoaDon.getMaBan() : "N/A";
            String ngayStr = selectedHoaDon.getNgayLap() != null ? selectedHoaDon.getNgayLap().toLocalDate().format(pdfDateFormatter) : "N/A";
            String gioVaoStr = selectedHoaDon.getGioVao() != null ? selectedHoaDon.getGioVao().toLocalTime().format(pdfTimeFormatter) : "N/A";
            String gioRaStr = selectedHoaDon.getGioRa() != null ? selectedHoaDon.getGioRa().toLocalTime().format(pdfTimeFormatter) : "N/A";
            String thuNganStr = selectedHoaDon.getTenNhanVien() != null ? selectedHoaDon.getTenNhanVien() : "N/A";
            String khachHangStr = selectedHoaDon.getSoDienThoaiKH() != null ? selectedHoaDon.getSoDienThoaiKH() : "Khách vãng lai";
            String hinhThucTTStr = selectedHoaDon.getHinhThucTT() != null ? selectedHoaDon.getHinhThucTT().getDisplayName() : "N/A";
            String khachHangMemberDetails = "Khách vãng lai";
             if (selectedHoaDon.getKhachHang() != null && selectedHoaDon.getKhachHang().getThanhVien() != null) {
                 khachHangMemberDetails = selectedHoaDon.getKhachHang().getThanhVien();
             }
            String uuDaiStr = (selectedHoaDon.getKhuyenMai() > 0) ? ("-" + formatCurrencyVND(selectedHoaDon.getKhuyenMai())) : "0 VNĐ";
            double soTienKhachTra = selectedHoaDon.getTongTienThanhToan() + selectedHoaDon.getKhuyenMai();

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {

                y = drawCenteredTextPage(contentStream, fontBold, 14, y, "NHÀ HÀNG XYZ", PAGE_WIDTH, LINE_HEIGHT_L);
                y = drawCenteredTextPage(contentStream, font, 10, y, "Địa chỉ: 123 Đường ABC, Phường X, Quận Y, TP. Z", PAGE_WIDTH, LINE_HEIGHT_M);
                y = drawCenteredTextPage(contentStream, font, 10, y, "SĐT: 0123.456.789", PAGE_WIDTH, LINE_HEIGHT_M);
                y -= GAP;

                y = drawCenteredTextPage(contentStream, fontBold, 16, y, "HÓA ĐƠN THANH TOÁN", PAGE_WIDTH, LINE_HEIGHT_XL);
                y = drawCenteredTextPage(contentStream, fontBold, 12, y, "Số HĐ: " + (selectedHoaDon.getMaHD() != null ? selectedHoaDon.getMaHD() : "N/A"), PAGE_WIDTH, LINE_HEIGHT_L);
                y -= GAP * 2;

                final float COL_1_START = MARGIN;
                final float COL_2_START = MARGIN + CONTENT_WIDTH / 2 + 10;
                final int FONT_SIZE_INFO = 10;

                drawTextLeft(contentStream, font, FONT_SIZE_INFO, COL_1_START, y, "Bàn: " + banStr);
                drawTextLeft(contentStream, font, FONT_SIZE_INFO, COL_2_START, y, "Thu ngân: " + thuNganStr);
                y -= LINE_HEIGHT_M;
                drawTextLeft(contentStream, font, FONT_SIZE_INFO, COL_1_START, y, "Ngày: " + ngayStr);
                drawTextLeft(contentStream, font, FONT_SIZE_INFO, COL_2_START, y, "Khách hàng: " + khachHangStr);
                y -= LINE_HEIGHT_M;
                drawTextLeft(contentStream, font, FONT_SIZE_INFO, COL_1_START, y, "Giờ vào: " + gioVaoStr);
                drawTextLeft(contentStream, font, FONT_SIZE_INFO, COL_2_START, y, "Giờ ra: " + gioRaStr);
                y -= LINE_HEIGHT_M;

                drawLine(contentStream, y, MARGIN, PAGE_WIDTH - MARGIN, GAP);

                final int FONT_SIZE_TABLE_HEADER = 10;
                final int FONT_SIZE_TABLE_DATA = 9;
                float colSTT_X = MARGIN + 5;
                float colTenMon_X = MARGIN + 40;
                float colSL_X_Center = MARGIN + CONTENT_WIDTH * 0.68f;
                float colDonGia_X_Right = MARGIN + CONTENT_WIDTH * 0.84f;
                float colThanhTien_X_Right = PAGE_WIDTH - MARGIN - 5;

                y -= LINE_HEIGHT_M;
                drawTextLeft(contentStream, fontBold, FONT_SIZE_TABLE_HEADER, colSTT_X, y, "STT");
                drawTextLeft(contentStream, fontBold, FONT_SIZE_TABLE_HEADER, colTenMon_X, y, "Tên món");
                drawTextCenter(contentStream, fontBold, FONT_SIZE_TABLE_HEADER, colSL_X_Center, y, "SL");
                drawTextRight(contentStream, fontBold, FONT_SIZE_TABLE_HEADER, colDonGia_X_Right, y, "Đơn giá");
                drawTextRight(contentStream, fontBold, FONT_SIZE_TABLE_HEADER, colThanhTien_X_Right, y, "Thành tiền");
                y -= LINE_HEIGHT_M;

                drawLine(contentStream, y, MARGIN, PAGE_WIDTH - MARGIN, GAP/2);
                y -= GAP;

                for (int i = 0; i < chiTietList.size(); i++) {
                    ChiTietHoaDon mon = chiTietList.get(i);
                    drawTextLeft(contentStream, font, FONT_SIZE_TABLE_DATA, colSTT_X, y, String.valueOf(i + 1));
                    drawTextLeft(contentStream, font, FONT_SIZE_TABLE_DATA, colTenMon_X, y, mon.getTenMon());
                    drawTextCenter(contentStream, font, FONT_SIZE_TABLE_DATA, colSL_X_Center, y, String.valueOf(mon.getSoLuong()));
                    drawTextRight(contentStream, font, FONT_SIZE_TABLE_DATA, colDonGia_X_Right, y, currencyFormatter.format(mon.getDonGia()));
                    drawTextRight(contentStream, font, FONT_SIZE_TABLE_DATA, colThanhTien_X_Right, y, currencyFormatter.format(mon.getThanhTien()));
                    y -= LINE_HEIGHT_M;
                }

                drawLine(contentStream, y, MARGIN, PAGE_WIDTH - MARGIN, GAP);

                final int FONT_SIZE_SUMMARY = 10;
                final float LABEL_ALIGN_LEFT_X = MARGIN; // *** Sửa lại thành căn trái ***
                final float VALUE_ALIGN_RIGHT_X = PAGE_WIDTH - MARGIN - 5;

                y -= LINE_HEIGHT_L;

                y = drawSummaryRow(contentStream, fontBold, fontBold, FONT_SIZE_SUMMARY, y, "Tổng cộng món ăn:", formatCurrencyVND(selectedHoaDon.getTongCongMonAn()), LABEL_ALIGN_LEFT_X, VALUE_ALIGN_RIGHT_X, LINE_HEIGHT_M);
                y = drawSummaryRow(contentStream, font, font, FONT_SIZE_SUMMARY, y, "Phí dịch vụ (5%):", formatCurrencyVND(selectedHoaDon.getPhiDichVu()), LABEL_ALIGN_LEFT_X, VALUE_ALIGN_RIGHT_X, LINE_HEIGHT_M);
                y = drawSummaryRow(contentStream, font, font, FONT_SIZE_SUMMARY, y, "Thuế VAT (8%):", formatCurrencyVND(selectedHoaDon.getThueVAT()), LABEL_ALIGN_LEFT_X, VALUE_ALIGN_RIGHT_X, LINE_HEIGHT_M);
                y = drawSummaryRow(contentStream, font, font, FONT_SIZE_SUMMARY, y, "Tiền đặt cọc bàn:", "-" + formatCurrencyVND(selectedHoaDon.getTienCoc()), LABEL_ALIGN_LEFT_X, VALUE_ALIGN_RIGHT_X, LINE_HEIGHT_M);

                drawLine(contentStream, y, MARGIN , VALUE_ALIGN_RIGHT_X, GAP); // *** Sửa lại điểm bắt đầu kẻ ***
                y -= GAP;

                y = drawSummaryRow(contentStream, fontBold, fontBold, 12, y, "Tổng thanh toán:", formatCurrencyVND(selectedHoaDon.getTongTienThanhToan()), LABEL_ALIGN_LEFT_X, VALUE_ALIGN_RIGHT_X, LINE_HEIGHT_L);

                y -= GAP;
                y = drawSummaryRow(contentStream, font, font, FONT_SIZE_SUMMARY, y, "Hình thức thanh toán:", hinhThucTTStr, LABEL_ALIGN_LEFT_X, VALUE_ALIGN_RIGHT_X, LINE_HEIGHT_M);
                y = drawSummaryRow(contentStream, font, font, FONT_SIZE_SUMMARY, y, "Khách hàng thành viên:", khachHangMemberDetails, LABEL_ALIGN_LEFT_X, VALUE_ALIGN_RIGHT_X, LINE_HEIGHT_M);
                y = drawSummaryRow(contentStream, font, font, FONT_SIZE_SUMMARY, y, "Ưu đãi áp dụng:", uuDaiStr, LABEL_ALIGN_LEFT_X, VALUE_ALIGN_RIGHT_X, LINE_HEIGHT_M);
                y = drawSummaryRow(contentStream, fontBold, fontBold, FONT_SIZE_SUMMARY, y, "Số tiền khách trả:", formatCurrencyVND(soTienKhachTra), LABEL_ALIGN_LEFT_X, VALUE_ALIGN_RIGHT_X, LINE_HEIGHT_M);

                drawLine(contentStream, y, MARGIN, VALUE_ALIGN_RIGHT_X, GAP); // *** Sửa lại điểm bắt đầu kẻ ***
                y -= GAP * 3;

                y = drawCenteredTextPage(contentStream, font, 10, y, "Nhà hàng XYZ xin cám ơn và hẹn gặp lại!", PAGE_WIDTH, LINE_HEIGHT_M);
            }

            document.save(file);
            showAlert("In hóa đơn PDF thành công!\nĐã lưu tại: " + file.getAbsolutePath());

        } catch (FileNotFoundException e) {
            showAlert("Lỗi: Không thể ghi file PDF.\nFile có thể đang được mở bởi một chương trình khác.\nChi tiết: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            showAlert("Lỗi I/O khi tạo file PDF: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            showAlert("Đã xảy ra lỗi không mong muốn khi in PDF: " + e.getMessage() +
                      "\nVui lòng kiểm tra lại 'FONT_PATH' và file 'NotoSans-Bold.ttf'.");
            e.printStackTrace();
        }
    }


    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null); alert.setContentText(message); alert.showAndWait();
    }
}
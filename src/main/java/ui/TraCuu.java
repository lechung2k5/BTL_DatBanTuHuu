package ui;

import dao.HoaDonDAO;
import dao.KhachHangDAO;
import entity.ChiTietHoaDon;
import entity.HoaDon;
import entity.KhachHang;
import entity.TaiKhoan;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

// 🔥 THÊM IMPORT CHO IN HÓA ĐƠN
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TraCuu {
    @FXML private ComboBox<String> cboSapXepHD, cboSapXepKH;
    @FXML private TextField txtTimKiemHD, txtTimKiemKH;
    @FXML private TableView<HoaDonDisplay> tblHoaDon;
    @FXML private TableColumn<HoaDonDisplay, String> colMaHD, colNgayHD, colHinhThuc, colSDTKH, colTongTien;
    @FXML private TableColumn<HoaDonDisplay, Void> colXemChiTiet;
    @FXML private TableView<KhachHangDisplay> tblKhachHang;
    @FXML private TableColumn<KhachHangDisplay, String> colMaKH, colHoTen, colSDT, colDiaChi, colEmail, colNgayDK, colLoaiKH, colTongTienHD;
    @FXML private TableColumn<KhachHangDisplay, Void> colLichSu;

    private ObservableList<HoaDonDisplay> danhSachHoaDon, tatCaHoaDon;
    private ObservableList<KhachHangDisplay> danhSachKhachHang, tatCaKhachHang;
    private HoaDonDAO hoaDonDAO;
    private KhachHangDAO khachHangDAO;
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm");
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private List<HoaDon> cachedHD;

    @FXML
    public void initialize() {
        hoaDonDAO = new HoaDonDAO();
        khachHangDAO = new KhachHangDAO();
        tatCaHoaDon = FXCollections.observableArrayList();
        danhSachHoaDon = FXCollections.observableArrayList();
        tatCaKhachHang = FXCollections.observableArrayList();
        danhSachKhachHang = FXCollections.observableArrayList();
        cachedHD = hoaDonDAO.getAllHoaDon();

        setupTables();
        loadData();
        Platform.runLater(() -> {
            Scene scene = tblHoaDon.getScene();
            if (scene == null) return;

            scene.setOnKeyPressed(e -> {
                if (e.isControlDown() && !e.isShiftDown() && e.getCode() == KeyCode.H) {
                    tblHoaDon.requestFocus();
                }
                if (e.isControlDown() && !e.isShiftDown() && e.getCode() == KeyCode.K) {
                    tblKhachHang.requestFocus();
                }
                if (e.isControlDown() && !e.isAltDown() && e.getCode() == KeyCode.F) {
                    txtTimKiemHD.requestFocus();
                    txtTimKiemHD.selectAll();
                }
                if (e.isControlDown() && e.isAltDown() && e.getCode() == KeyCode.F) {
                    txtTimKiemKH.requestFocus();
                    txtTimKiemKH.selectAll();
                }
                if (e.isControlDown() && e.isShiftDown() && e.getCode() == KeyCode.E) {
                    if (tblHoaDon.isFocused()) handleXuatExcelHoaDon();
                    if (tblKhachHang.isFocused()) handleXuatExcelKhachHang();
                }
                if (e.isControlDown() && e.getCode() == KeyCode.P && tblHoaDon.isFocused()) {
                    HoaDonDisplay hd = tblHoaDon.getSelectionModel().getSelectedItem();
                    if (hd != null) showFullDetailHD(hd.getMaHD());
                }
                if (e.getCode() == KeyCode.F5) {
                    loadData();
                }
            });
        });

        
        cboSapXepHD.setItems(FXCollections.observableArrayList("Theo ngày", "Theo tháng", "Theo năm"));
        cboSapXepKH.setItems(FXCollections.observableArrayList("Theo ngày", "Theo tháng", "Theo năm"));
        cboSapXepKH.valueProperty().addListener((o, old, n) -> { if (n != null) sapXepKH(n); });
        cboSapXepHD.valueProperty().addListener((o, old, n) -> { if (n != null) sapXepHD(n); });
        txtTimKiemHD.textProperty().addListener((o, old, n) -> timHD(n));
        txtTimKiemKH.textProperty().addListener((o, old, n) -> timKH(n));
    }
    

    private void setupTables() {
        // Setup bảng Hóa đơn
        colMaHD.setCellValueFactory(c -> c.getValue().maHDProperty());
        colNgayHD.setCellValueFactory(c -> c.getValue().ngayProperty());
        colHinhThuc.setCellValueFactory(c -> c.getValue().hinhThucProperty());
        colSDTKH.setCellValueFactory(c -> c.getValue().sdtKHProperty());
        colTongTien.setCellValueFactory(c -> c.getValue().tongTienProperty());
        colTongTien.setCellFactory(col -> new TableCell<>() {
            protected void updateItem(String i, boolean e) {
                super.updateItem(i, e);
                setText(e || i == null ? "" : i);
                setAlignment(Pos.CENTER_RIGHT);
            }
        });
        colXemChiTiet.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Xem");
            { 
                btn.setOnAction(e -> showFullDetailHD(getTableView().getItems().get(getIndex()).getMaHD())); 
                btn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 10;");
            }
            protected void updateItem(Void i, boolean e) { 
                super.updateItem(i, e); 
                setGraphic(e ? null : btn); 
                setAlignment(Pos.CENTER);
            }
        });

        // Setup bảng Khách hàng
        colMaKH.setCellValueFactory(c -> c.getValue().maKHProperty());
        colHoTen.setCellValueFactory(c -> c.getValue().hoTenProperty());
        colSDT.setCellValueFactory(c -> c.getValue().sdtProperty());
        colDiaChi.setCellValueFactory(c -> c.getValue().diaChiProperty());
        colEmail.setCellValueFactory(c -> c.getValue().emailProperty());
        colNgayDK.setCellValueFactory(c -> c.getValue().ngayDKProperty());
        colLoaiKH.setCellValueFactory(c -> c.getValue().loaiKHProperty());
        colTongTienHD.setCellValueFactory(c -> c.getValue().tongTienHDProperty());
        colTongTienHD.setCellFactory(col -> new TableCell<>() {
            protected void updateItem(String i, boolean e) {
                super.updateItem(i, e);
                setText(e || i == null ? "" : i);
                setAlignment(Pos.CENTER_RIGHT);
            }
        });
        colLichSu.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Xem");
            { 
                btn.setOnAction(e -> showFullHistoryKH(getTableView().getItems().get(getIndex()).getMaKH())); 
                btn.setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 10;");
            }
            protected void updateItem(Void i, boolean e) { 
                super.updateItem(i, e); 
                setGraphic(e ? null : btn); 
                setAlignment(Pos.CENTER);
            }
        });
        
        tblHoaDon.setItems(danhSachHoaDon);
        tblKhachHang.setItems(danhSachKhachHang);
    }

    private void loadData() {
        // Load Hóa đơn - Lấy chi tiết đầy đủ từ DAO
        for (HoaDon hd : cachedHD) {
            HoaDon hdChiTiet = hoaDonDAO.getHoaDonChiTietByMaHD(hd.getMaHD());
            
            String sdt = "N/A";
            double tongTien = 0;
            
            if (hdChiTiet != null) {
                sdt = hdChiTiet.getSoDienThoaiKH() != null ? hdChiTiet.getSoDienThoaiKH() : "N/A";
                tongTien = hdChiTiet.getTongTienThanhToan();
            }
            
            tatCaHoaDon.add(new HoaDonDisplay(
                hd.getMaHD() != null ? hd.getMaHD() : "",
                hd.getNgayLap() != null ? hd.getNgayLap().format(fmt) : "",
                hd.getHinhThucTT() != null ? hd.getHinhThucTT().getDisplayName() : "",
                sdt, 
                String.format("%,.0f VNĐ", tongTien)
            ));
        }
        danhSachHoaDon.setAll(tatCaHoaDon);

        // Load Khách hàng
        for (KhachHang kh : khachHangDAO.getAllKhachHang()) {
            List<HoaDon> hoaDonKH = hoaDonDAO.getHoaDonByMaKH(kh.getMaKH());
            double total = hoaDonKH.stream().mapToDouble(HoaDon::getTongTienThanhToan).sum();
            
            tatCaKhachHang.add(new KhachHangDisplay(
                kh.getMaKH(), 
                kh.getTenKH(), 
                kh.getSoDT(), 
                kh.getDiaChi() != null ? kh.getDiaChi() : "", 
                kh.getEmail() != null ? kh.getEmail() : "",
                kh.getNgayDangKy() != null ? kh.getNgayDangKy().format(fmt) : "", 
                kh.getThanhVien() != null ? kh.getThanhVien() : "", 
                String.format("%,.0f VNĐ", total)
            ));
        }
        danhSachKhachHang.setAll(tatCaKhachHang);
    }

    private void sapXepHD(String k) {
        List<HoaDonDisplay> l = new ArrayList<>(danhSachHoaDon);
        l.sort((a, b) -> {
            String d1 = a.getNgay();
            String d2 = b.getNgay();
            
            if (d1 == null || d1.isEmpty() || d1.length() < 10) return 1;
            if (d2 == null || d2.isEmpty() || d2.length() < 10) return -1;
            
            try {
                String[] parts1 = d1.split("/");
                String[] parts2 = d2.split("/");
                
                if (parts1.length != 3 || parts2.length != 3) {
                    return d1.compareTo(d2);
                }
                
                int ngay1 = Integer.parseInt(parts1[0]);
                int thang1 = Integer.parseInt(parts1[1]);
                int nam1 = Integer.parseInt(parts1[2]);
                
                int ngay2 = Integer.parseInt(parts2[0]);
                int thang2 = Integer.parseInt(parts2[1]);
                int nam2 = Integer.parseInt(parts2[2]);
                
                if (k.equals("Theo năm")) {
                    if (nam1 != nam2) return nam1 - nam2;
                    if (thang1 != thang2) return thang1 - thang2;
                    return ngay1 - ngay2;
                } else if (k.equals("Theo tháng")) {
                    if (nam1 != nam2) return nam1 - nam2;
                    if (thang1 != thang2) return thang1 - thang2;
                    return ngay1 - ngay2;
                } else {
                    if (nam1 != nam2) return nam1 - nam2;
                    if (thang1 != thang2) return thang1 - thang2;
                    return ngay1 - ngay2;
                }
            } catch (Exception e) {
                return d1.compareTo(d2);
            }
        });
        danhSachHoaDon.setAll(l);
    }

    private void sapXepKH(String k) {
        List<KhachHangDisplay> l = new ArrayList<>(danhSachKhachHang);
        l.sort((a, b) -> {
            String d1 = a.getNgayDK();
            String d2 = b.getNgayDK();
            
            if (d1 == null || d1.isEmpty() || d1.length() < 10) return 1;
            if (d2 == null || d2.isEmpty() || d2.length() < 10) return -1;
            
            try {
                String[] parts1 = d1.split("/");
                String[] parts2 = d2.split("/");
                
                if (parts1.length != 3 || parts2.length != 3) {
                    return d1.compareTo(d2);
                }
                
                int ngay1 = Integer.parseInt(parts1[0]);
                int thang1 = Integer.parseInt(parts1[1]);
                int nam1 = Integer.parseInt(parts1[2]);
                
                int ngay2 = Integer.parseInt(parts2[0]);
                int thang2 = Integer.parseInt(parts2[1]);
                int nam2 = Integer.parseInt(parts2[2]);
                
                if (k.equals("Theo năm")) {
                    if (nam1 != nam2) return nam1 - nam2;
                    if (thang1 != thang2) return thang1 - thang2;
                    return ngay1 - ngay2;
                } else if (k.equals("Theo tháng")) {
                    if (nam1 != nam2) return nam1 - nam2;
                    if (thang1 != thang2) return thang1 - thang2;
                    return ngay1 - ngay2;
                } else {
                    if (nam1 != nam2) return nam1 - nam2;
                    if (thang1 != thang2) return thang1 - thang2;
                    return ngay1 - ngay2;
                }
            } catch (Exception e) {
                return d1.compareTo(d2);
            }
        });
        danhSachKhachHang.setAll(l);
    }

    private void timHD(String t) {
        if (t == null || t.trim().isEmpty()) { 
            danhSachHoaDon.setAll(tatCaHoaDon); 
            return; 
        }
        String s = t.toLowerCase().trim();
        danhSachHoaDon.setAll(tatCaHoaDon.stream()
            .filter(h -> (h.getSdtKH() != null && h.getSdtKH().toLowerCase().contains(s)) ||
                        (h.getMaHD() != null && h.getMaHD().toLowerCase().contains(s)))
            .collect(Collectors.toList()));
    }

    private void timKH(String t) {
        if (t == null || t.trim().isEmpty()) { 
            danhSachKhachHang.setAll(tatCaKhachHang); 
            return; 
        }
        String s = t.toLowerCase().trim();
        danhSachKhachHang.setAll(tatCaKhachHang.stream()
            .filter(k -> (k.getSdt() != null && k.getSdt().toLowerCase().contains(s)) ||
                        (k.getHoTen() != null && k.getHoTen().toLowerCase().contains(s)))
            .collect(Collectors.toList()));
    }

    // 🔥 SỬA HÀM NÀY: THÊM NÚT IN HÓA ĐƠN
    private void showFullDetailHD(String id) {
        HoaDon hd = hoaDonDAO.getHoaDonChiTietByMaHD(id);
        if (hd == null) { 
            alert("Lỗi", "Không tìm thấy hóa đơn"); 
            return; 
        }
        List<ChiTietHoaDon> ct = hoaDonDAO.getChiTietHoaDon(id);
        
        Dialog<Void> d = new Dialog<>();
        d.setTitle("Chi tiết hóa đơn - " + id);
        
        VBox main = new VBox(15); 
        main.setPadding(new Insets(15));
        main.setStyle("-fx-background-color: #FFF3E0; -fx-background-radius: 10;");
        
        Label title = new Label("CHI TIẾT HÓA ĐƠN " + id);
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #d35400;");
        title.setMaxWidth(Double.MAX_VALUE);
        title.setAlignment(Pos.CENTER);
        
        GridPane info = new GridPane(); 
        info.setHgap(15); 
        info.setVgap(8);
        int row = 0;
        addInfo(info, row++, "Ngày:", hd.getNgayLap() != null ? hd.getNgayLap().format(fmt) : "", 
                "SĐT:", hd.getSoDienThoaiKH() != null ? hd.getSoDienThoaiKH() : "");
        addInfo(info, row++, "Bàn:", hd.getMaBan() != null ? hd.getMaBan() : "", 
                "Thu ngân:", hd.getTenNhanVien() != null ? hd.getTenNhanVien() : "");
        addInfo(info, row++, "Giờ vào:", hd.getGioVao() != null ? hd.getGioVao().toLocalTime().format(timeFmt) : "", 
                "Giờ ra:", hd.getGioRa() != null ? hd.getGioRa().toLocalTime().format(timeFmt) : "");
        
        TableView<ChiTietHoaDon> tbl = new TableView<>(); 
        tbl.setPrefHeight(180);
        TableColumn<ChiTietHoaDon, Void> c0 = new TableColumn<>("STT"); 
        c0.setPrefWidth(40);
        c0.setCellFactory(col -> new TableCell<>() { 
            public void updateIndex(int i) { 
                super.updateIndex(i); 
                setText(isEmpty() || i < 0 ? null : String.valueOf(i + 1)); 
            }
        });
        TableColumn<ChiTietHoaDon, String> c1 = new TableColumn<>("Tên món"); 
        c1.setCellValueFactory(new PropertyValueFactory<>("tenMon")); 
        c1.setPrefWidth(150);
        TableColumn<ChiTietHoaDon, Integer> c2 = new TableColumn<>("SL"); 
        c2.setCellValueFactory(new PropertyValueFactory<>("soLuong")); 
        c2.setPrefWidth(50);
        TableColumn<ChiTietHoaDon, Double> c3 = new TableColumn<>("Đơn giá"); 
        c3.setCellValueFactory(new PropertyValueFactory<>("donGia")); 
        c3.setPrefWidth(90);
        c3.setCellFactory(col -> new TableCell<>() { 
            protected void updateItem(Double i, boolean e) { 
                super.updateItem(i, e); 
                setText(e || i == null ? null : String.format("%,.0f", i)); 
                setAlignment(Pos.CENTER_RIGHT); 
            }
        });
        TableColumn<ChiTietHoaDon, Double> c4 = new TableColumn<>("Thành tiền"); 
        c4.setCellValueFactory(new PropertyValueFactory<>("thanhTien")); 
        c4.setPrefWidth(100);
        c4.setCellFactory(col -> new TableCell<>() { 
            protected void updateItem(Double i, boolean e) { 
                super.updateItem(i, e); 
                setText(e || i == null ? null : String.format("%,.0f", i)); 
                setAlignment(Pos.CENTER_RIGHT); 
            }
        });
        tbl.getColumns().addAll(c0, c1, c2, c3, c4); 
        tbl.setItems(FXCollections.observableArrayList(ct));
        
        VBox sum = new VBox(5);
        sum.getChildren().addAll(
            row("Tổng món ăn:", String.format("%,.0f VNĐ", hd.getTongCongMonAn())),
            row("Phí DV (5%):", String.format("%,.0f VNĐ", hd.getPhiDichVu())), 
            row("VAT (8%):", String.format("%,.0f VNĐ", hd.getThueVAT())),
            row("Tiền cọc:", String.format("%,.0f VNĐ", hd.getTienCoc())), 
            row("Khuyến mãi:", String.format("%,.0f VNĐ", hd.getKhuyenMai()))
        );
        
        HBox tot = new HBox(); 
        tot.setAlignment(Pos.CENTER_RIGHT);
        Label lbl = new Label("Tổng thanh toán: "); 
        lbl.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        Label val = new Label(String.format("%,.0f VNĐ", hd.getTongTienThanhToan())); 
        val.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: red;");
        Region sp = new Region(); 
        HBox.setHgrow(sp, Priority.ALWAYS); 
        tot.getChildren().addAll(lbl, sp, val);
        
        main.getChildren().addAll(title, info, tbl, sum, tot);
        d.getDialogPane().setContent(main); 
        d.getDialogPane().setPrefWidth(600); 
        
        // 🔥 THÊM NÚT IN HÓA ĐƠN KẾ NÚT CLOSE
        ButtonType btnPrint = new ButtonType("🖨️ In hóa đơn", ButtonBar.ButtonData.LEFT);
        d.getDialogPane().getButtonTypes().addAll(btnPrint, ButtonType.CLOSE);
        
        // 🔥 XỬ LÝ SỰ KIỆN NÚT IN
        Button printButton = (Button) d.getDialogPane().lookupButton(btnPrint);
        printButton.setOnAction(e -> {
            e.consume(); // Ngăn dialog đóng
            handleInHoaDon(hd);
        });
        
        d.getDialogPane().setStyle("-fx-background-color: transparent;");
        d.show();
    }

    // 🔥 HÀM XỬ LÝ IN HÓA ĐƠN (LẤY ĐẦY ĐỦ DỮ LIỆU TỪ DAO GIỐNG NÚT XEM)
    private void handleInHoaDon(HoaDon hdDisplay) {
        if (hdDisplay == null || hdDisplay.getMaHD() == null) {
            alert("Lỗi", "Không có hóa đơn hợp lệ để in.");
            return;
        }
        
        // 🔥 QUAN TRỌNG: Lấy lại chi tiết đầy đủ từ DAO (giống hàm showFullDetailHD)
        HoaDon hd = hoaDonDAO.getHoaDonChiTietByMaHD(hdDisplay.getMaHD());
        if (hd == null) {
            Platform.runLater(() -> alert("Lỗi", "Không thể tải chi tiết hóa đơn từ database."));
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
                        alert("In Hóa đơn", "Đã gửi lệnh in cho Hóa đơn " + hd.getMaHD() + " thành công.");
                    });
                } else {
                    Platform.runLater(() -> alert("Hủy In", "Đã hủy thao tác in."));
                }

            } catch (java.awt.print.PrinterException e) {
                Platform.runLater(() -> alert("Lỗi In", "Lỗi trong quá trình in ấn: " + e.getMessage()));
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> alert("Lỗi Hệ thống", "Lỗi khi tạo PDF: " + e.getMessage()));
            } finally {
                if (document != null) {
                    try {
                        document.close();
                    } catch (IOException ignored) {}
                }
            }
        }).start();
    }

    // 🔥 CLASS HELPER CHO VỊ TRÍ Y
    private static class YPosition {
        public float y;
        public YPosition(float initialY) {
            this.y = initialY;
        }
    }

    // 🔥 HÀM TẠO PDF HÓA ĐƠN (DỰA TRÊN ẢNH CẤU TRÚC)
    private PDDocument createReceiptPdf(HoaDon hd) throws IOException {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        final float PAGE_WIDTH = page.getMediaBox().getWidth();
        final float MARGIN = 72; 
        final float Y_START = page.getMediaBox().getHeight() - MARGIN;
        final float LINE_HEIGHT = 16; 
        
        final YPosition pos = new YPosition(Y_START); 

     // 🔥 SỬA PHẦN LOAD FONT (Copy từ DatBan.java - đã fix lỗi)
        PDFont font = PDType1Font.HELVETICA;
        PDFont fontBold = PDType1Font.HELVETICA_BOLD;

        try {
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
            System.err.println("Sử dụng font mặc định do không load được font tùy chỉnh");
            font = PDType1Font.HELVETICA;
            fontBold = PDType1Font.HELVETICA_BOLD;
        }
        // Lấy chi tiết hóa đơn
        List<ChiTietHoaDon> monAnList = hoaDonDAO.getChiTietHoaDon(hd.getMaHD());
        
        final DecimalFormat currencyFormatter = new DecimalFormat("###,###");
        final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        // Chuẩn bị dữ liệu hiển thị
        String banStr = hd.getMaBan() != null ? hd.getMaBan() : "N/A";
        String ngayStr = hd.getNgayLap() != null ? hd.getNgayLap().toLocalDate().format(dateFormatter) : "N/A";
        String gioVaoStr = hd.getGioVao() != null ? hd.getGioVao().toLocalTime().format(timeFormatter) : "N/A";
        String gioRaStr = hd.getGioRa() != null ? hd.getGioRa().toLocalTime().format(timeFormatter) : "N/A";
        String tenThuNgan = "N/A";
        try {
            TaiKhoan tk = MainApp.getLoggedInUser();
            if (tk != null && tk.getNhanVien() != null) {
                tenThuNgan = tk.getNhanVien().getHoTen(); 
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy tên nhân viên đăng nhập: " + e.getMessage());
        }
        String thuNganStr = tenThuNgan;
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
            
            // Dòng 1
            contentStream.beginText();
            contentStream.newLineAtOffset(COL_1_START, pos.y);
            contentStream.showText("Bàn: " + banStr);
            contentStream.setTextMatrix(org.apache.pdfbox.util.Matrix.getTranslateInstance(COL_2_START, pos.y));
            contentStream.showText("Thu ngân: " + thuNganStr);
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
            // 🔥 LẤY THÔNG TIN KHÁCH HÀNG TỪ HoaDon (nếu có)
            String khachHangMemberDetails = "Khách vãng lai"; 
            if (hd.getKhachHang() != null && hd.getKhachHang().getThanhVien() != null) {
                khachHangMemberDetails = hd.getKhachHang().getThanhVien();
            }
            
            // 🔥 TÍNH ƯU ĐÃI TỪ KHUYẾN MÃI (đã có sẵn trong HoaDon)
            String uuDaiStr = (hd.getKhuyenMai() > 0) 
                ? ("-" + currencyFormatter.format(hd.getKhuyenMai()) + " VNĐ") 
                : "0 VNĐ";
            
            // 🔥 TÍNH SỐ TIỀN KHÁCH TRẢ = Tổng thanh toán + Khuyến mãi
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

    private void showFullHistoryKH(String id) {
        List<HoaDon> ls = hoaDonDAO.getHoaDonByMaKH(id);
        KhachHang kh = khachHangDAO.getAllKhachHang().stream()
            .filter(k -> id.equals(k.getMaKH()))
            .findFirst()
            .orElse(null);
        
        Dialog<Void> d = new Dialog<>(); 
        d.setTitle("Khách hàng - " + id);
        
        VBox main = new VBox(15); 
        main.setPadding(new Insets(15));
        main.setStyle("-fx-background-color: #FFF3E0; -fx-background-radius: 10;");
        
        if (kh != null) {
            Label t = new Label("THÔNG TIN KHÁCH HÀNG"); 
            t.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #d35400;");
            t.setMaxWidth(Double.MAX_VALUE);
            t.setAlignment(Pos.CENTER);
            
            GridPane g = new GridPane(); 
            g.setHgap(15); 
            g.setVgap(8);
            int row = 0;
            addInfo(g, row++, "Mã:", kh.getMaKH(), "Tên:", kh.getTenKH());
            addInfo(g, row++, "SĐT:", kh.getSoDT(), "Email:", kh.getEmail() != null ? kh.getEmail() : "");
            addInfo(g, row++, "Địa chỉ:", kh.getDiaChi() != null ? kh.getDiaChi() : "", "Loại:", kh.getThanhVien() != null ? kh.getThanhVien() : "");
            addInfo(g, row++, "Ngày ĐK:", kh.getNgayDangKy() != null ? kh.getNgayDangKy().format(fmt) : "", "", "");
            
            double tot = ls.stream().mapToDouble(HoaDon::getTongTienThanhToan).sum();
            HBox s = new HBox(); 
            s.setAlignment(Pos.CENTER_RIGHT);
            Label sl = new Label("Tổng chi tiêu: "); 
            sl.setStyle("-fx-font-weight: bold;");
            Label sv = new Label(String.format("%,.0f VNĐ", tot)); 
            sv.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: green;");
            Region sp = new Region(); 
            HBox.setHgrow(sp, Priority.ALWAYS); 
            s.getChildren().addAll(sl, sp, sv);
            main.getChildren().addAll(t, g, s);
        }
        
        Label ht = new Label("LỊCH SỬ (" + ls.size() + " hóa đơn)"); 
        ht.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #d35400;");
        
        TableView<HoaDon> tb = new TableView<>(); 
        tb.setPrefHeight(250);
        tb.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        TableColumn<HoaDon, String> c1 = new TableColumn<>("Mã"); 
        c1.setCellValueFactory(new PropertyValueFactory<>("maHD")); 
        c1.setPrefWidth(100);
        
        TableColumn<HoaDon, String> c2 = new TableColumn<>("Ngày"); 
        c2.setCellValueFactory(cellData -> {
            HoaDon h = cellData.getValue();
            return new SimpleStringProperty(h.getNgayLap() != null ? h.getNgayLap().format(dateTimeFormatter) : "N/A");
        });
        c2.setPrefWidth(150);
        
        TableColumn<HoaDon, String> c3 = new TableColumn<>("PTTT"); 
        c3.setCellValueFactory(cellData -> {
            HoaDon h = cellData.getValue();
            return new SimpleStringProperty(h.getHinhThucTT() != null ? h.getHinhThucTT().getDisplayName() : "Chưa TT");
        });
        c3.setPrefWidth(120);
        
        TableColumn<HoaDon, Double> c4 = new TableColumn<>("Tổng"); 
        c4.setCellValueFactory(new PropertyValueFactory<>("tongTienThanhToan")); 
        c4.setPrefWidth(130);
        c4.setCellFactory(col -> new TableCell<>() { 
            protected void updateItem(Double i, boolean e) { 
                super.updateItem(i, e); 
                setText(e || i == null ? null : String.format("%,.0f VNĐ", i)); 
                setAlignment(Pos.CENTER_RIGHT); 
            }
        });
        
        tb.getColumns().addAll(c1, c2, c3, c4); 
        tb.setItems(FXCollections.observableArrayList(ls));
        
        // Thêm sự kiện double-click để xem chi tiết hóa đơn
        tb.setOnMouseClicked(event -> {
            if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
                HoaDon selectedHD = tb.getSelectionModel().getSelectedItem();
                if (selectedHD != null && selectedHD.getMaHD() != null) {
                    showFullDetailHD(selectedHD.getMaHD());
                }
            }
        });
        
        if (ls.isEmpty()) {
            tb.setPlaceholder(new Label("Khách hàng chưa có hóa đơn."));
        }
        
        main.getChildren().addAll(ht, tb);
        d.getDialogPane().setContent(main); 
        d.getDialogPane().setPrefSize(600, 650); 
        d.getDialogPane().getButtonTypes().add(ButtonType.CLOSE); 
        d.getDialogPane().setStyle("-fx-background-color: transparent;");
        d.show();
    }

    private void addInfo(GridPane g, int r, String l1, String v1, String l2, String v2) {
        Label lb1 = new Label(l1); 
        lb1.setStyle("-fx-font-weight: bold;");
        g.add(lb1, 0, r); 
        g.add(new Label(v1), 1, r);
        
        if (!l2.isEmpty()) {
            Label lb2 = new Label(l2); 
            lb2.setStyle("-fx-font-weight: bold;");
            g.add(lb2, 2, r); 
            g.add(new Label(v2), 3, r);
        }
    }

    private HBox row(String l, String v) {
        HBox h = new HBox(); 
        Label lb = new Label(l); 
        lb.setStyle("-fx-font-weight: bold;");
        Label vl = new Label(v); 
        Region s = new Region(); 
        HBox.setHgrow(s, Priority.ALWAYS);
        h.getChildren().addAll(lb, s, vl); 
        return h;
    }

    @FXML
    private void handleXuatExcelHoaDon() {
        FileChooser fc = new FileChooser(); 
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel", "*.xlsx"));
        File f = fc.showSaveDialog(tblHoaDon.getScene().getWindow());
        if (f != null) {
            try (Workbook wb = new XSSFWorkbook(); FileOutputStream out = new FileOutputStream(f)) {
                Sheet s = wb.createSheet("Hóa đơn"); 
                Row h = s.createRow(0);
                String[] cols = {"Mã", "Ngày", "PTTT", "SĐT", "Tổng"};
                for (int i = 0; i < cols.length; i++) h.createCell(i).setCellValue(cols[i]);
                int r = 1;
                for (HoaDonDisplay hd : danhSachHoaDon) {
                    Row rw = s.createRow(r++); 
                    rw.createCell(0).setCellValue(hd.getMaHD());
                    rw.createCell(1).setCellValue(hd.getNgay()); 
                    rw.createCell(2).setCellValue(hd.getHinhThuc());
                    rw.createCell(3).setCellValue(hd.getSdtKH()); 
                    rw.createCell(4).setCellValue(hd.getTongTien());
                }
                wb.write(out); 
                alert("Thành công", "Đã xuất Excel");
            } catch (Exception e) { 
                alert("Lỗi", e.getMessage()); 
            }
        }
    }

    @FXML
    private void handleXuatExcelKhachHang() {
        FileChooser fc = new FileChooser(); 
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel", "*.xlsx"));
        File f = fc.showSaveDialog(tblKhachHang.getScene().getWindow());
        if (f != null) {
            try (Workbook wb = new XSSFWorkbook(); FileOutputStream out = new FileOutputStream(f)) {
                Sheet s = wb.createSheet("Khách hàng"); 
                Row h = s.createRow(0);
                String[] cols = {"Mã", "Tên", "SĐT", "Địa chỉ", "Email", "Ngày", "Loại", "Tổng"};
                for (int i = 0; i < cols.length; i++) h.createCell(i).setCellValue(cols[i]);
                int r = 1;
                for (KhachHangDisplay k : danhSachKhachHang) {
                    Row rw = s.createRow(r++); 
                    rw.createCell(0).setCellValue(k.getMaKH());
                    rw.createCell(1).setCellValue(k.getHoTen()); 
                    rw.createCell(2).setCellValue(k.getSdt());
                    rw.createCell(3).setCellValue(k.getDiaChi()); 
                    rw.createCell(4).setCellValue(k.getEmail());
                    rw.createCell(5).setCellValue(k.getNgayDK()); 
                    rw.createCell(6).setCellValue(k.getLoaiKH());
                    rw.createCell(7).setCellValue(k.getTongTienHD());
                }
                wb.write(out); 
                alert("Thành công", "Đã xuất Excel");
            } catch (Exception e) { 
                alert("Lỗi", e.getMessage()); 
            }
        }
    }

    private void alert(String t, String c) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(t); 
        a.setHeaderText(null); 
        a.setContentText(c); 
        a.showAndWait();
    }

    // === INNER CLASSES ===
    public static class HoaDonDisplay {
        private final SimpleStringProperty maHD, ngay, hinhThuc, sdtKH, tongTien;
        public HoaDonDisplay(String m, String n, String h, String s, String t) {
            maHD = new SimpleStringProperty(m); 
            ngay = new SimpleStringProperty(n);
            hinhThuc = new SimpleStringProperty(h); 
            sdtKH = new SimpleStringProperty(s);
            tongTien = new SimpleStringProperty(t);
        }
        public String getMaHD() { return maHD.get(); }
        public SimpleStringProperty maHDProperty() { return maHD; }
        public String getNgay() { return ngay.get(); }
        public SimpleStringProperty ngayProperty() { return ngay; }
        public String getHinhThuc() { return hinhThuc.get(); }
        public SimpleStringProperty hinhThucProperty() { return hinhThuc; }
        public String getSdtKH() { return sdtKH.get(); }
        public SimpleStringProperty sdtKHProperty() { return sdtKH; }
        public String getTongTien() { return tongTien.get(); }
        public SimpleStringProperty tongTienProperty() { return tongTien; }
    }

    public static class KhachHangDisplay {
        private final SimpleStringProperty maKH, hoTen, sdt, diaChi, email, ngayDK, loaiKH, tongTienHD;
        public KhachHangDisplay(String m, String h, String s, String d, String e, String n, String l, String t) {
            maKH = new SimpleStringProperty(m); 
            hoTen = new SimpleStringProperty(h);
            sdt = new SimpleStringProperty(s); 
            diaChi = new SimpleStringProperty(d);
            email = new SimpleStringProperty(e); 
            ngayDK = new SimpleStringProperty(n);
            loaiKH = new SimpleStringProperty(l); 
            tongTienHD = new SimpleStringProperty(t);
        }
        public String getMaKH() { return maKH.get(); }
        public SimpleStringProperty maKHProperty() { return maKH; }
        public String getHoTen() { return hoTen.get(); }
        public SimpleStringProperty hoTenProperty() { return hoTen; }
        public String getSdt() { return sdt.get(); }
        public SimpleStringProperty sdtProperty() { return sdt; }
        public String getDiaChi() { return diaChi.get(); }
        public SimpleStringProperty diaChiProperty() { return diaChi; }
        public String getEmail() { return email.get(); }
        public SimpleStringProperty emailProperty() { return email; }
        public String getNgayDK() { return ngayDK.get(); }
        public SimpleStringProperty ngayDKProperty() { return ngayDK; }
        public String getLoaiKH() { return loaiKH.get(); }
        public SimpleStringProperty loaiKHProperty() { return loaiKH; }
        public String getTongTienHD() { return tongTienHD.get(); }
        public SimpleStringProperty tongTienHDProperty() { return tongTienHD; }
    }
}
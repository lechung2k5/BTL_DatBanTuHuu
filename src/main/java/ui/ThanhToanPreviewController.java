package ui;

import dao.DatBanDAO;
import entity.HoaDon;
import entity.PTTThanhToan;
import entity.TaiKhoan;
import entity.TrangThaiHoaDon;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import javafx.util.Callback; // Cần thiết cho CellValueFactory

import java.awt.print.PrinterJob;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Optional;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;

public class ThanhToanPreviewController {

    // === KHAI BÁO CŨ (Giữ nguyên) ===
    @FXML private Label lblMaHD;
    @FXML private Label lblBan;
    @FXML private Label lblTongMonAn;
    @FXML private Label lblPhiDV;
    @FXML private Label lblThueVAT;
    @FXML private Label lblTienCoc;
    @FXML private Label lblTongThanhToan;
    @FXML private ToggleButton btnTienMat;
    @FXML private ToggleButton btnNganHang;
    @FXML private ToggleButton btnMoMo;
    @FXML private Button btnHuy;
    @FXML private Button btnXacNhan;
    @FXML private Button btnInHoaDon; 
    @FXML private ToggleGroup paymentGroup; 
    
    // === KHAI BÁO MỚI (Từ FXML Preview) ===
    @FXML private Label lblTongTruocKM;
    @FXML private Label lblThanhVien;
    @FXML private Label lblUuDaiApDung;
    @FXML private Label lblSoTienKhachTra;
    // Thông tin chung
    @FXML private Label lblNgay;
    @FXML private Label lblGioVao;
    @FXML private Label lblGioRa;
    @FXML private Label lblThuNgan;
    @FXML private Label lblKhachHang;
    
    // BẢNG MÓN ĂN (Sẽ dùng MyReceiptItem)
    @FXML private TableView<MyReceiptItem> tblMonAnThanhToan;
    @FXML private TableColumn<MyReceiptItem, Integer> colStt;
    @FXML private TableColumn<MyReceiptItem, String> colTenMon;
    @FXML private TableColumn<MyReceiptItem, Integer> colSL;
    @FXML private TableColumn<MyReceiptItem, Double> colDonGia;
    @FXML private TableColumn<MyReceiptItem, Double> colThanhTien;
    // ===============================================

    private HoaDon hoaDonToPay; 
    private DatBanDAO datBanDAO;
    private DatBan mainController; 
    
    private PTTThanhToan selectedPTTT = PTTThanhToan.TIEN_MAT;
    private final DecimalFormat currencyFormatter = new DecimalFormat("###,### VNĐ");
    private boolean isNewInvoice; 
    
    // DỮ LIỆU TÁCH MÓN
    private ObservableList<TachBanPopupController.MonTach> monTachListSnapshot;
    private String maHDGocSnapshot;

    @FXML
    private void initialize() {
        if (paymentGroup != null) {
            paymentGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal instanceof ToggleButton toggle) {
                    if (toggle == btnTienMat) selectedPTTT = PTTThanhToan.TIEN_MAT;
                    else if (toggle == btnNganHang) selectedPTTT = PTTThanhToan.NGAN_HANG;
                    else if (toggle == btnMoMo) selectedPTTT = PTTThanhToan.VI_DIEN_TU;
                }
            });
        }
        
        btnXacNhan.setOnAction(e -> handleFinalThanhToan());
        btnHuy.setOnAction(e -> closePopup());
        
        if (btnInHoaDon != null) {
             btnInHoaDon.setOnAction(e -> handleInHoaDon());
             btnInHoaDon.setDisable(true); // Disable ban đầu
        }
        
        // 🔥 FIX BINDING LỖI: Cấu hình cột TableView sử dụng Getter truyền thống
        if (tblMonAnThanhToan != null) {
             // STT (dùng index)
             colStt.setCellValueFactory(data -> new SimpleIntegerProperty(tblMonAnThanhToan.getItems().indexOf(data.getValue()) + 1).asObject());
             // Tên món
             colTenMon.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTenMon()));
             // SL
             colSL.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getSoLuong()).asObject());
             // Đơn giá
             colDonGia.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getDonGia()).asObject());
             // Thành tiền (Tính toán)
             colThanhTien.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getThanhTien()).asObject());
        }
    }

    /**
     * 🔥 SỬA: Cập nhật hàm để tính toán, đổ dữ liệu vào các Labels mới và BẢNG
     * ĐÃ FIX: Đảm bảo TableView được cấu hình và đổ dữ liệu MonOrder từ MonTachSnapshot.
     */
    public void setInitialData(HoaDon hd, DatBanDAO dao, DatBan controller, double tongMonAn, double tienCoc, boolean isNewInvoice) {
        this.hoaDonToPay = hd;
        this.datBanDAO = dao;
        this.mainController = controller;
        this.isNewInvoice = isNewInvoice; 
        
        // --- TÍNH TOÁN CÁC GIÁ TRỊ ---
        double phiDV = tongMonAn * 0.05;
        double vat = (tongMonAn + phiDV) * 0.08;
        double tienCocApDung = isNewInvoice ? 0.0 : tienCoc;
        
        double tienKhuyenMai = hd.getKhuyenMai(); 
        
        double tongTruocKM = tongMonAn + phiDV + vat - tienCocApDung;
        double tongThanhToan = tongTruocKM - tienKhuyenMai;
        double soTienKhachTra = tongThanhToan; 

        // --- ĐỔ DỮ LIỆU CHUNG (Lấy thông tin từ HĐ Gốc nếu đang preview) ---
        HoaDon hdGocDeLayThongTin = hd;
        if (isNewInvoice && maHDGocSnapshot != null) {
            try {
                HoaDon fetchedHdGoc = datBanDAO.getHoaDonByMaHD(maHDGocSnapshot);
                if (fetchedHdGoc != null) hdGocDeLayThongTin = fetchedHdGoc;
            } catch (Exception e) {
                System.err.println("Lỗi khi fetch HĐ gốc cho preview: " + e.getMessage());
            }
        }

        String tenThuNgan = "N/A";
        String khachHangSdt = hdGocDeLayThongTin.getKhachHang() != null ? hdGocDeLayThongTin.getKhachHang().getSoDT() : "N/A";
        
        try {
            TaiKhoan tk = MainApp.getLoggedInUser();
            if (tk != null && tk.getNhanVien() != null) {
                tenThuNgan = tk.getNhanVien().getHoTen(); 
            }
        } catch (Exception ignored) {}
        
        // Phần Header (GridPane)
        if (lblMaHD != null) lblMaHD.setText("SỐ HĐ: " + hd.getMaHD());
        if (lblNgay != null) lblNgay.setText("Ngày: " + (hdGocDeLayThongTin.getNgayLap() != null ? hdGocDeLayThongTin.getNgayLap().toLocalDate().toString() : "N/A"));
        if (lblGioVao != null) lblGioVao.setText("Giờ vào: " + (hdGocDeLayThongTin.getGioVao() != null ? hdGocDeLayThongTin.getGioVao().toLocalTime().toString().substring(0, 5) : "N/A"));
        if (lblGioRa != null) lblGioRa.setText("Giờ ra: " + java.time.LocalTime.now().toString().substring(0, 5)); // Giờ hệ thống
        if (lblThuNgan != null) lblThuNgan.setText("Thu ngân: " + tenThuNgan);
        if (lblKhachHang != null) lblKhachHang.setText("Khách hàng: " + khachHangSdt);
        if (lblBan != null) lblBan.setText("Bàn: " + (hd.getBan() != null ? hd.getBan().getMaBan() : "Chưa gán"));

        // Phần Summary
        if (lblTongMonAn != null) lblTongMonAn.setText(currencyFormatter.format(tongMonAn));
        if (lblPhiDV != null) lblPhiDV.setText(currencyFormatter.format(phiDV));
        if (lblThueVAT != null) lblThueVAT.setText(currencyFormatter.format(vat));
        if (lblTienCoc != null) lblTienCoc.setText("-" + currencyFormatter.format(tienCocApDung));
        if (lblTongTruocKM != null) lblTongTruocKM.setText(currencyFormatter.format(tongTruocKM));
        
        if (lblThanhVien != null) {
            String memberStatus = (hdGocDeLayThongTin.getKhachHang() != null && hdGocDeLayThongTin.getKhachHang().getThanhVien().equals("VIP")) ? "Gold (giảm 10%)" : "N/A";
            lblThanhVien.setText(memberStatus);
        }
        
        if (lblUuDaiApDung != null) lblUuDaiApDung.setText("-" + currencyFormatter.format(tienKhuyenMai));
        
        if (lblTongThanhToan != null) lblTongThanhToan.setText(currencyFormatter.format(Math.max(0, tongThanhToan)));
        if (lblSoTienKhachTra != null) lblSoTienKhachTra.setText(currencyFormatter.format(Math.max(0, soTienKhachTra)));
        
        
        // 🔥 FIX QUAN TRỌNG: ĐỔ DỮ LIỆU VÀO BẢNG MÓN ĂN (Sử dụng logic tính toán từ MonTachSnapshot)
        if (tblMonAnThanhToan != null && monTachListSnapshot != null) {
            ObservableList<MyReceiptItem> receiptList = FXCollections.observableArrayList();
            
            for (TachBanPopupController.MonTach mon : monTachListSnapshot) {
                int sl;
                if (isNewInvoice) {
                    sl = mon.getSoLuongTach(); // SL đã tách
                } else {
                    sl = mon.getSoLuongGoc() - mon.getSoLuongTach(); // SL còn lại
                }
                
                if (sl > 0) {
                    double tt = sl * mon.getDonGia();
                    // Tạo MyReceiptItem (tenMon, soLuong, donGia, thanhTien)
                    receiptList.add(new MyReceiptItem(mon.getTenMon(), sl, mon.getDonGia(), tt));
                }
            }
            tblMonAnThanhToan.setItems(receiptList);
        }
    }
    
    public void setMonTachList(ObservableList<TachBanPopupController.MonTach> monTachListSnapshot, String maHDGoc) {
        this.monTachListSnapshot = monTachListSnapshot;
        this.maHDGocSnapshot = maHDGoc;
    }
    
    /**
     * Xử lý xác nhận thanh toán cuối cùng.
     */
    private void handleFinalThanhToan() {
        Optional<ButtonType> result = showAlertConfirm("Xác nhận thanh toán", 
                                                      "Xác nhận thanh toán Hóa đơn " + hoaDonToPay.getMaHD() + " bằng " + selectedPTTT.getDisplayName() + "?");
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String maNV = "N/A";
            try {
                TaiKhoan tk = MainApp.getLoggedInUser();
                if (tk != null && tk.getNhanVien() != null) {
                    maNV = tk.getNhanVien().getMaNV(); 
                }
            } catch (Exception e) {
                 System.err.println("Lỗi khi lấy mã NV đăng nhập: " + e.getMessage());
            }
            
            try {
                
                // 1. GỌI DAO TRANSACTION: Thực hiện Tách món + Tạo HĐ mới + Thanh toán
                if (datBanDAO.thucHienTachBanVaThanhToan(
                    maHDGocSnapshot, 
                    monTachListSnapshot, 
                    hoaDonToPay.getMaHD(), 
                    selectedPTTT, 
                    maNV)) 
                {
                    
                    showAlert(AlertType.INFORMATION, "Thành công", "Hóa đơn " + hoaDonToPay.getMaHD() + " đã thanh toán.");
                    
                    // 2. Cập nhật giao diện chính
                    if (mainController != null) {
                        mainController.loadTableGrids();
                        mainController.loadBookingCards();
                        
                        hoaDonToPay = datBanDAO.getHoaDonByMaHD(hoaDonToPay.getMaHD());
                    }
                    
                    // 3. Kích hoạt nút In và vô hiệu hóa nút Thanh toán
                    btnXacNhan.setDisable(true);
                    if (btnInHoaDon != null) btnInHoaDon.setDisable(false);
                    
                    // 4. Đóng popup sau khi in (Nếu không có nút In, đóng luôn)
                    if (btnInHoaDon == null) closePopup(); 

                } else {
                    showAlert(AlertType.ERROR, "Lỗi CSDL", "Không thể hoàn tất Transaction Thanh Toán/Tách. Đã rollback.");
                }
            } catch (Exception e) {
                showAlert(AlertType.ERROR, "Lỗi hệ thống", "Lỗi khi thanh toán/tách: " + e.getMessage());
            }
        }
    }
    
    /**
     * Xử lý In Hóa đơn (Chỉ được gọi sau khi thanh toán thành công).
     */
    private void handleInHoaDon() {
        if (hoaDonToPay == null || !hoaDonToPay.getTrangThai().getDbValue().equals(TrangThaiHoaDon.DA_THANH_TOAN.getDbValue())) {
            showAlert(AlertType.WARNING, "Lỗi", "Hóa đơn chưa được thanh toán hoặc không hợp lệ.");
            return;
        }

        new Thread(() -> {
            org.apache.pdfbox.pdmodel.PDDocument document = null;
            try {
                document = mainController.getReceiptDocument(hoaDonToPay); 
                
                java.awt.print.PrinterJob job = PrinterJob.getPrinterJob();
                PrintRequestAttributeSet attr = new HashPrintRequestAttributeSet();
                
                if (job.printDialog(attr)) {
                    java.awt.print.PageFormat pageFormat = job.getPageFormat(attr);
                    org.apache.pdfbox.printing.PDFPrintable printableData = new org.apache.pdfbox.printing.PDFPrintable(document);
                    
                    job.setPrintable(printableData, pageFormat);
                    job.print(attr); 
                    
                    Platform.runLater(() -> {
                        showAlert(AlertType.INFORMATION, "In Hóa đơn", "Đã gửi lệnh in thành công.");
                        closePopup(); // Đóng Popup sau khi in
                    });
                } else {
                     Platform.runLater(() -> showAlert(AlertType.INFORMATION, "Hủy In", "Đã hủy thao tác in."));
                }

            } catch (Exception e) {
                Platform.runLater(() -> showAlert(AlertType.ERROR, "Lỗi In", "Lỗi trong quá trình in ấn: " + e.getMessage()));
            } finally {
                if (document != null) {
                    try {
                        document.close();
                    } catch (IOException ignored) {}
                }
            }
        }).start();
    }
    
    private void closePopup() {
        Stage stage = (Stage) btnHuy.getScene().getWindow();
        stage.close();
    }
    
    private Optional<ButtonType> showAlertConfirm(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        return alert.showAndWait();
    }
    
    private void showAlert(AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * 🔥 HÀM VIEW MODEL MỚI: Dùng để ánh xạ dữ liệu trực tiếp lên bảng tblMonAnThanhToan.
     */
    public static class MyReceiptItem {
        private final String tenMon;
        private final int soLuong;
        private final double donGia;
        private final double thanhTien;

        public MyReceiptItem(String tenMon, int soLuong, double donGia, double thanhTien) {
            this.tenMon = tenMon; // Giữ String thay vì Property phức tạp
            this.soLuong = soLuong;
            this.donGia = donGia;
            this.thanhTien = thanhTien;
        }

        // Getter truyền thống (sử dụng trong cấu hình cột trên)
        public String getTenMon() { return tenMon; }
        public StringProperty tenMonProperty() { return new SimpleStringProperty(tenMon); }
        public int getSoLuong() { return soLuong; }
        public double getDonGia() { return donGia; }
        public double getThanhTien() { return thanhTien; }
    }
}
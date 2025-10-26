package ui;

import dao.DatBanDAO;
import entity.HoaDon;
import entity.PTTThanhToan;
import entity.TaiKhoan;
import entity.TrangThaiHoaDon;
import dao.UuDaiDAO; // 🔥 Thêm import
import entity.UuDai;   // 🔥 Thêm import
import java.util.ArrayList; // 🔥 Thêm import
import java.util.List;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import javafx.util.Callback; // Cần thiết cho CellValueFactory
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.awt.print.PrinterJob;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.time.LocalTime;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;

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
    @FXML private ComboBox<String> promoComboBoxPreview;

    // 🔥 THÊM CÁC BIẾN ĐỂ QUẢN LÝ KHUYẾN MÃI VÀ TÍNH TOÁN LẠI
    private final UuDaiDAO uuDaiDAO = new UuDaiDAO();
    private List<UuDai> dsUuDaiDangApDung = new ArrayList<>();
    private UuDai selectedUuDai = null;
    
    // 🔥 THÊM BIẾN LƯU TRỮ GIÁ TRỊ TÍNH TOÁN
    private double currentTongMonAn;
    private double currentTienCoc;
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
    	// 🔥 THÊM SETONACTION CHO CÁC NÚT THANH TOÁN
        if (btnTienMat != null) {
            btnTienMat.setOnAction(e -> moPopupThanhToanTienMat());
        }
        if (btnNganHang != null) {
            btnNganHang.setOnAction(e -> openNganHangQrPopup());
        }
        if (btnMoMo != null) {
            btnMoMo.setOnAction(e -> openMoMoQrPopup());
        }
        
       
        btnHuy.setOnAction(e -> closePopup());
        
        if (btnInHoaDon != null) {
             btnInHoaDon.setOnAction(e -> handleInHoaDon());
             btnInHoaDon.setDisable(false); // Disable ban đầu
             btnInHoaDon.setStyle(
                     "-fx-background-color: #007bff; " +
                     "-fx-text-fill: white; " +
                     "-fx-font-size: 1.1em; " +
                     "-fx-font-weight: bold; " +
                     "-fx-padding: 10px 25px; " +
                     "-fx-background-radius: 5px; " +
                     "-fx-border-radius: 5px; " +
                     "-fx-cursor: hand; " +
                     "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 5, 0, 1, 2);"
                 );
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
     // 🔥 THÊM MỚI: Tải và lắng nghe ComboBox khuyến mãi
        loadPromoComboBox();
        if (promoComboBoxPreview != null) {
            promoComboBoxPreview.valueProperty().addListener((obs, oldVal, newVal) -> {
                handlePromoSelection(newVal);
            });
        }
    }

    /**
     * 🔥 HÀM MỚI: Xử lý khi người dùng chọn Khuyến mãi
     */
    private void handlePromoSelection(String selectedName) {
        if (selectedName == null || selectedName.equals("Không áp dụng")) {
            selectedUuDai = null;
        } else {
            String simpleName = selectedName.substring(0, selectedName.indexOf(" (Giảm")).trim();
            
            selectedUuDai = dsUuDaiDangApDung.stream()
                .filter(ud -> ud.getTenUuDai().equals(simpleName))
                .findFirst()
                .orElse(null);
        }
        // Tính toán và hiển thị lại tổng tiền ngay lập tức
        calculateAndDisplayTotals();
    }
    /**
     * 🔥 HÀM MỚI: Tính toán và cập nhật tất cả Label tổng tiền
     */
    private void calculateAndDisplayTotals() {
        // Lấy giá trị đã lưu
        double tongMonAn = this.currentTongMonAn;
        double tienCocApDung = this.currentTienCoc;

        // Tính toán
        double phiDV = tongMonAn * 0.05;
        double vat = (tongMonAn + phiDV) * 0.08;
        
        double tienKhuyenMai = 0.0;
        if (selectedUuDai != null) {
            // Giả định giảm trên tổng tiền món ăn
            tienKhuyenMai = tongMonAn * (selectedUuDai.getGiaTri() / 100.0);
        }
        
        double tongTruocKM = tongMonAn + phiDV + vat - tienCocApDung;
        double tongThanhToan = tongTruocKM - tienKhuyenMai;
        double soTienKhachTra = tongThanhToan; 

        // Cập nhật giao diện
        if (lblTongMonAn != null) lblTongMonAn.setText(currencyFormatter.format(tongMonAn));
        if (lblPhiDV != null) lblPhiDV.setText(currencyFormatter.format(phiDV));
        if (lblThueVAT != null) lblThueVAT.setText(currencyFormatter.format(vat));
        if (lblTienCoc != null) lblTienCoc.setText("-" + currencyFormatter.format(tienCocApDung));
        if (lblTongTruocKM != null) lblTongTruocKM.setText(currencyFormatter.format(tongTruocKM));
        
        if (lblUuDaiApDung != null) lblUuDaiApDung.setText("-" + currencyFormatter.format(tienKhuyenMai));
        if (lblTongThanhToan != null) lblTongThanhToan.setText(currencyFormatter.format(Math.max(0, tongThanhToan)));
        if (lblSoTienKhachTra != null) lblSoTienKhachTra.setText(currencyFormatter.format(Math.max(0, soTienKhachTra)));
    }
	/**
     * 🔥 HÀM MỚI: Tải danh sách Khuyến mãi 'Đang áp dụng'
     */
    private void loadPromoComboBox() {
        List<UuDai> allUuDai = uuDaiDAO.getAllUuDai();
        
        dsUuDaiDangApDung = allUuDai.stream()
            .filter(ud -> ud.getTrangThai().equals("Đang áp dụng"))
            .collect(Collectors.toList());
            
        ObservableList<String> promoNames = FXCollections.observableArrayList();
        promoNames.add("Không áp dụng"); 
        
        for (UuDai ud : dsUuDaiDangApDung) {
            promoNames.add(String.format("%s (Giảm %.0f%%)", ud.getTenUuDai(), ud.getGiaTri()));
        }
        
        if (promoComboBoxPreview != null) {
            promoComboBoxPreview.setItems(promoNames);
            promoComboBoxPreview.getSelectionModel().selectFirst();
        }
    }

	/**
     * 🔥 SỬA: Cập nhật hàm để tính toán, đổ dữ liệu vào các Labels mới và BẢNG
     * ĐÃ FIX: Đảm bảo TableView được cấu hình và đổ dữ liệu MonOrder từ MonTachSnapshot.
     */
 // [Trong file ThanhToanPreviewController.java]

    public void setInitialData(HoaDon hd, DatBanDAO dao, DatBan controller, double tongMonAn, double tienCoc, boolean isNewInvoice) {
        this.hoaDonToPay = hd;
        this.datBanDAO = dao;
        this.mainController = controller;
        this.isNewInvoice = isNewInvoice; 
        
        // --- 1. LƯU GIÁ TRỊ TÍNH TOÁN (Đã đúng) ---
        this.currentTongMonAn = tongMonAn;
        this.currentTienCoc = isNewInvoice ? 0.0 : tienCoc;

        // --- 🔥 ĐÃ XÓA KHỐI TÍNH TOÁN CŨ (từ phiDV đến soTienKhachTra) ---
        // (Logic này đã được chuyển vào hàm calculateAndDisplayTotals())

        // --- 2. ĐỔ DỮ LIỆU CHUNG (HEADER) (Giữ nguyên) ---
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
        // ... (Giữ nguyên code set text cho lblNgay, lblGioVao, lblGioRa, lblThuNgan, lblKhachHang, lblBan) ...
        if (lblNgay != null) lblNgay.setText("Ngày: " + (hdGocDeLayThongTin.getNgayLap() != null ? hdGocDeLayThongTin.getNgayLap().toLocalDate().toString() : "N/A"));
        if (lblGioVao != null) lblGioVao.setText("Giờ vào: " + (hdGocDeLayThongTin.getGioVao() != null ? hdGocDeLayThongTin.getGioVao().toLocalTime().toString().substring(0, 5) : "N/A"));
        if (lblGioRa != null) lblGioRa.setText("Giờ ra: " + java.time.LocalTime.now().toString().substring(0, 5));
        if (lblThuNgan != null) lblThuNgan.setText("Thu ngân: " + tenThuNgan);
        if (lblKhachHang != null) lblKhachHang.setText("Khách hàng: " + khachHangSdt);
        if (lblBan != null) lblBan.setText("Bàn: " + (hd.getBan() != null ? hd.getBan().getMaBan() : "Chưa gán"));

        // --- 🔥 ĐÃ XÓA CÁC LỆNH .setText CŨ CHO PHẦN SUMMARY ---
        
        if (lblThanhVien != null) {
            String memberStatus = (hdGocDeLayThongTin.getKhachHang() != null && hdGocDeLayThongTin.getKhachHang().getThanhVien().equals("VIP")) ? "Gold (giảm 10%)" : "N/A";
            lblThanhVien.setText(memberStatus);
        }
        
        // --- 3. ĐỔ DỮ LIỆU BẢNG MÓN ĂN (Giữ nguyên) ---
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
                    receiptList.add(new MyReceiptItem(mon.getTenMon(), sl, mon.getDonGia(), tt));
                }
            }
            tblMonAnThanhToan.setItems(receiptList);
        }
        
        // --- 4. GỌI HÀM TÍNH TOÁN TỔNG THỂ (Đã đúng) ---
        calculateAndDisplayTotals();
    }
    
    public void setMonTachList(ObservableList<TachBanPopupController.MonTach> monTachListSnapshot, String maHDGoc) {
        this.monTachListSnapshot = monTachListSnapshot;
        this.maHDGocSnapshot = maHDGoc;
    }
    /**
     * Xử lý xác nhận thanh toán cuối cùng (Lưu vào CSDL).
     * Hàm này sẽ được gọi TỪ BÊN TRONG các popup thanh toán (Tiền mặt, QR...).
     */
    private void handleFinalThanhToan() {
        // Không cần hiển thị confirm dialog ở đây nữa, vì đã confirm trong popup con

        // Lấy maNV (Giữ nguyên)
        String maNV = "N/A";
        try {
            TaiKhoan tk = MainApp.getLoggedInUser();
            if (tk != null && tk.getNhanVien() != null) maNV = tk.getNhanVien().getMaNV();
        } catch (Exception e) { System.err.println("Lỗi lấy mã NV: " + e.getMessage()); }

        try {
            String maUuDaiDaChon = (selectedUuDai != null) ? selectedUuDai.getMaUuDai() : null;

            // 1. GỌI DAO TRANSACTION (sử dụng selectedPTTT đã được set bởi popup con)
            if (datBanDAO.thucHienTachBanVaThanhToan(
                maHDGocSnapshot,
                monTachListSnapshot,
                hoaDonToPay.getMaHD(),
                selectedPTTT, // <<< Sử dụng PTTT đã được set
                maNV,
                maUuDaiDaChon
            ))
            {
                showAlert(AlertType.INFORMATION, "Thành công", "Hóa đơn đã thanh toán thành công.");

                // 2. Cập nhật giao diện chính (qua mainController)
                if (mainController != null) {
                    mainController.loadTableGrids();
                    mainController.loadBookingCards();
                    // Có thể gọi thêm hàm để clear form chính nếu cần
                    // mainController.clearFormDatBan();
                }

                // 3. Vô hiệu hóa các nút trong POPUP NÀY
                disablePopupPaymentButtons(); // Gọi hàm mới để disable nút

                 // 4. Xử lý nút In Hóa đơn sau khi thanh toán
                 if (btnInHoaDon != null) {
                    btnInHoaDon.setDisable(false); // Luôn bật nút in sau TT
                    if (isNewInvoice) {
                        btnInHoaDon.setText("Đóng (HĐ Mới)"); // Đổi text nếu là HĐ mới
                        btnInHoaDon.setOnAction(e -> closePopup());
                    } else {
                        // Cập nhật trạng thái HĐ để hàm in biết
                        this.hoaDonToPay.setTrangThai(TrangThaiHoaDon.DA_THANH_TOAN);
                        btnInHoaDon.setText("In Hóa Đơn (Final)");
                        btnInHoaDon.setOnAction(e -> handleInHoaDonDaThanhToan()); // Gọi hàm in final
                    }
                 }


            } else {
                showAlert(AlertType.ERROR, "Lỗi CSDL", "Không thể hoàn tất Transaction. Đã rollback.");
                 // Có thể bật lại các nút nếu rollback
                 // enablePopupPaymentButtons();
            }
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi hệ thống", "Lỗi khi thanh toán/tách: " + e.getMessage());
            e.printStackTrace();
             // Có thể bật lại các nút nếu lỗi
             // enablePopupPaymentButtons();
        }
    }
 // [Thêm hàm mới này vào cuối file ThanhToanPreviewController.java]

    /**
     * 🔥 HÀM MỚI: Chỉ dùng để in sau khi đã bấm "Thanh toán" thành công.
     * Tải lại HĐ từ CSDL để đảm bảo in bản final.
     */
    private void handleInHoaDonDaThanhToan() {
        // 1. Tải lại HĐ từ CSDL để đảm bảo 100% là bản final
        // Giả định hoaDonToPay.getMaHD() chứa mã HĐ đúng (kể cả HĐ mới sau khi lưu)
        // LƯU Ý: Hàm thucHienTachBanVaThanhToan cần trả về mã HĐ mới nếu tạo thành công
        // Hoặc chúng ta cần gọi getHoaDonByMaHD với mã HĐ đã lưu.
        // Tạm thời dùng mã HĐ hiện tại:
        String maHDCanIn = this.hoaDonToPay.getMaHD();
        if (maHDCanIn == null || maHDCanIn.startsWith("TMP")) {
             showAlert(AlertType.ERROR, "Lỗi", "Không thể xác định mã Hóa đơn đã thanh toán để in.");
             return;
        }

        HoaDon hoaDonDeIn = datBanDAO.getHoaDonByMaHD(maHDCanIn);

        if (hoaDonDeIn == null || !hoaDonDeIn.getTrangThai().getDbValue().equals(TrangThaiHoaDon.DA_THANH_TOAN.getDbValue())) {
            showAlert(AlertType.WARNING, "Lỗi", "Không tìm thấy hóa đơn đã thanh toán (ID: " + maHDCanIn + ") để in.");
            return;
        }

        // 2. Lấy danh sách món ăn từ CSDL cho bản in final
        ObservableList<DatBan.MonOrder> monAnDaLuu = datBanDAO.getChiTietHoaDon(maHDCanIn);
        // Chuyển đổi sang MyReceiptItem để dùng hàm createReceiptPdf
        ObservableList<MyReceiptItem> receiptItemsDaLuu = FXCollections.observableArrayList();
        for(DatBan.MonOrder mon : monAnDaLuu) {
            receiptItemsDaLuu.add(new MyReceiptItem(mon.getTenMon(), mon.getSoLuong(), mon.getDonGia(), mon.getSoLuong() * mon.getDonGia()));
        }


        // 3. Chạy in trong Thread riêng
        new Thread(() -> {
            PDDocument document = null;
            try {
                // Gọi hàm createReceiptPdf với dữ liệu đã lưu từ CSDL
                document = createReceiptPdf(hoaDonDeIn, receiptItemsDaLuu, null); // Truyền null cho hdGocInfo vì đây là bản final

                java.awt.print.PrinterJob job = PrinterJob.getPrinterJob();
                PrintRequestAttributeSet attr = new HashPrintRequestAttributeSet();

                if (job.printDialog(attr)) {
                    java.awt.print.PageFormat pageFormat = job.getPageFormat(attr);
                    org.apache.pdfbox.printing.PDFPrintable printableData = new org.apache.pdfbox.printing.PDFPrintable(document);

                    job.setPrintable(printableData, pageFormat);
                    job.print(attr);

                    Platform.runLater(() -> {
                        showAlert(AlertType.INFORMATION, "In Hóa đơn", "Đã gửi lệnh in (Final) thành công.");
                        closePopup(); // Đóng Popup sau khi in final
                    });
                } else {
                     Platform.runLater(() -> {
                        showAlert(AlertType.INFORMATION, "Hủy In", "Đã hủy thao tác in.");
                        closePopup(); // Vẫn đóng popup
                     });
                }

            } catch (Exception e) {
                Platform.runLater(() -> showAlert(AlertType.ERROR, "Lỗi In", "Lỗi trong quá trình in ấn: " + e.getMessage()));
                Platform.runLater(this::closePopup); // Đóng dù có lỗi
            } finally {
                if (document != null) {
                    try {
                        document.close();
                    } catch (IOException ignored) {}
                }
            }
        }).start();
    }

	/**
     * 🔥 HÀM ĐÃ SỬA: Xử lý nút In Hóa đơn (In Hóa đơn TẠM TÍNH).
     * Sẽ lấy thông tin từ HĐ Gốc nếu đang in HĐ Mới (TMP...).
     */
    @FXML
    private void handleInHoaDon() {
        if (hoaDonToPay == null || hoaDonToPay.getMaHD() == null) {
            showAlert(AlertType.WARNING, "Lỗi", "Không có Hóa đơn để in.");
            return;
        }
        
        // 1. Chuẩn bị dữ liệu từ UI
        HoaDon hdTamDeIn = this.hoaDonToPay; // Đây là HĐ TMP... hoặc HĐ Gốc
        ObservableList<MyReceiptItem> monAnList = tblMonAnThanhToan.getItems();
        
        // --- 🔥 LẤY THÔNG TIN TỪ HĐ GỐC NẾU LÀ HĐ MỚI ---
        HoaDon hdGocDeLayThongTin = null;
        if (isNewInvoice && maHDGocSnapshot != null) {
            try {
                hdGocDeLayThongTin = datBanDAO.getHoaDonByMaHD(maHDGocSnapshot);
            } catch (Exception e) {
                System.err.println("Lỗi khi fetch HĐ gốc cho preview print: " + e.getMessage());
                // Không cần dừng, chỉ là thông tin header sẽ thiếu
            }
        }
        // Nếu không phải HĐ mới, thì HĐ gốc chính là HĐ đang xem
        if (!isNewInvoice) {
             hdGocDeLayThongTin = hdTamDeIn;
        }
        // ---------------------------------------------

        // 2. Cập nhật HĐ tạm thời với các giá trị tính toán mới nhất
        double tongMonAn = this.currentTongMonAn;
        double tienCoc = this.currentTienCoc; // Tiền cọc gốc chỉ áp dụng nếu in HĐ Gốc
        double phiDV = tongMonAn * 0.05;
        double vat = (tongMonAn + phiDV) * 0.08;
        double tienKM = 0.0;
        if (selectedUuDai != null) {
            tienKM = tongMonAn * (selectedUuDai.getGiaTri() / 100.0);
        }

        hdTamDeIn.setHinhThucTT(this.selectedPTTT);
        hdTamDeIn.setMaUuDai(selectedUuDai != null ? selectedUuDai.getMaUuDai() : null);
        hdTamDeIn.setTongCongMonAn(tongMonAn); // Tính lại phiDV, vat
        hdTamDeIn.setTienCoc(tienCoc);
        hdTamDeIn.setKhuyenMai(tienKM); // Tính lại tổng thanh toán

        // 3. Chạy in trong Thread riêng
        final HoaDon finalHdGocInfo = hdGocDeLayThongTin; // Biến final để dùng trong lambda
        new Thread(() -> {
            PDDocument document = null;
            try {
                // 4. TẠO DOCUMENT (Truyền cả HĐ tạm và HĐ gốc nếu có)
                document = createReceiptPdf(hdTamDeIn, monAnList, finalHdGocInfo);

                // ... (Phần code PrinterJob, dialog, print... giữ nguyên) ...
                java.awt.print.PrinterJob job = java.awt.print.PrinterJob.getPrinterJob();
                PrintRequestAttributeSet attr = new HashPrintRequestAttributeSet();

                if (job.printDialog(attr)) {
                    // ... (code set printable, print) ...
                    java.awt.print.PageFormat pageFormat = job.getPageFormat(attr);
                    org.apache.pdfbox.printing.PDFPrintable printableData = new org.apache.pdfbox.printing.PDFPrintable(document);
                    job.setPrintable(printableData, pageFormat);
                    job.print(attr);

                    Platform.runLater(() -> {
                        showAlert(AlertType.INFORMATION, "In Hóa đơn", "Đã gửi lệnh in (Tạm tính) thành công.");
                    });
                } else {
                     Platform.runLater(() -> showAlert(AlertType.INFORMATION, "Hủy In", "Đã hủy thao tác in."));
                }

            } catch (java.awt.print.PrinterException e) {
                 Platform.runLater(() -> showAlert(AlertType.ERROR, "Lỗi In", "Lỗi trong quá trình in ấn: " + e.getMessage()));
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showAlert(AlertType.ERROR, "Lỗi Hệ thống", "Lỗi khi tạo PDF: " + e.getMessage()));
            } finally {
                // ... (code đóng document) ...
                if (document != null) {
                    try {
                        document.close();
                    } catch (IOException ignored) {}
                }
            }
        }).start();
    }
 // Lớp YPosition (copy từ DatBan.java)
    private static class YPosition {
        public float y;
        public YPosition(float initialY) {
            this.y = initialY;
        }
    }
 // [Trong file ThanhToanPreviewController.java]

 // [Trong file ThanhToanPreviewController.java]

 // [Trong file ThanhToanPreviewController.java]

    /**
     * 🔥 HÀM ĐÃ SỬA LẦN CUỐI: Tạo PDF dựa trên dữ liệu TẠM THỜI và thông tin HĐ Gốc.
     * @param hd Hóa đơn tạm/gốc (đã được cập nhật totals)
     * @param monAnList Danh sách món ăn (từ tblMonAnThanhToan)
     * @param hdGocInfo Hóa đơn gốc (để lấy Ngày, Giờ vào, KH), có thể null nếu đang in HĐ gốc.
     * @return PDDocument
     */
    private PDDocument createReceiptPdf(HoaDon hd,
                                        ObservableList<MyReceiptItem> monAnList,
                                        HoaDon hdGocInfo) throws IOException { // <<< THÊM THAM SỐ hdGocInfo
        PDDocument document = new PDDocument();
        PDPage page = new PDPage(org.apache.pdfbox.pdmodel.common.PDRectangle.A4);
        document.addPage(page);

        // Kích thước trang & margin
        final float PAGE_WIDTH = page.getMediaBox().getWidth();
        final float MARGIN = 72;
        final float Y_START = page.getMediaBox().getHeight() - MARGIN;
        final float LINE_HEIGHT = 16;
        final YPosition pos = new YPosition(Y_START);

        // ====== 🔹 LOAD FONT HỖ TRỢ TIẾNG VIỆT ======
        org.apache.pdfbox.pdmodel.font.PDFont font = null;
        org.apache.pdfbox.pdmodel.font.PDFont fontBold = null;
        try {
            // Đảm bảo đường dẫn font chính xác trong resources
            InputStream fontStream = getClass().getResourceAsStream("/fonts/UTM Avo.ttf");
            InputStream fontBoldStream = getClass().getResourceAsStream("/fonts/UTM AvoBold.ttf");
            if (fontStream != null) {
                font = org.apache.pdfbox.pdmodel.font.PDType0Font.load(document, fontStream, true);
            }
            if (fontBoldStream != null) {
                fontBold = org.apache.pdfbox.pdmodel.font.PDType0Font.load(document, fontBoldStream, true);
            }
        } catch (Exception e) {
            System.err.println("⚠️ Lỗi load font: " + e.getMessage());
        }

        // Fallback fonts
        if (font == null) {
            font = org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA;
        }
        if (fontBold == null) {
            fontBold = org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA_BOLD;
        }
        // ============================================================

        // ====== DỮ LIỆU CHUẨN BỊ (SỬA LẠI ĐỂ DÙNG hdGocInfo) ======
        final java.text.DecimalFormat currencyFormatter = new java.text.DecimalFormat("###,###");
        final java.time.format.DateTimeFormatter dateFormatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
        final java.time.format.DateTimeFormatter timeFormatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm");

        // --- Lấy thông tin từ HĐ Gốc (hdGocInfo) nếu có, nếu không thì dùng HĐ hiện tại (hd) ---
        HoaDon sourceForHeader = (hdGocInfo != null) ? hdGocInfo : hd; // Chọn nguồn dữ liệu header

        String banStr = (sourceForHeader != null && sourceForHeader.getBan() != null && sourceForHeader.getBan().getMaBan() != null)
                ? sourceForHeader.getBan().getMaBan()
                : "N/A";
        // Lấy từ sourceForHeader
        String ngayStr = sourceForHeader.getNgayLap() != null ? sourceForHeader.getNgayLap().toLocalDate().format(dateFormatter) : "N/A";
        String gioVaoStr = sourceForHeader.getGioVao() != null ? sourceForHeader.getGioVao().toLocalTime().format(timeFormatter) : "N/A";
        String khachHangStr = (sourceForHeader.getKhachHang() != null && sourceForHeader.getKhachHang().getSoDT() != null) ? sourceForHeader.getKhachHang().getSoDT() : "N/A";
        // ----------------------------------------------------------------------------------

        String gioRaStr = LocalTime.now().format(timeFormatter); // Luôn là giờ hiện tại
        String tenThuNgan = "N/A";
        try {
            TaiKhoan tk = MainApp.getLoggedInUser();
            if (tk != null && tk.getNhanVien() != null) {
                tenThuNgan = tk.getNhanVien().getHoTen();
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy tên nhân viên đăng nhập: " + e.getMessage());
        }
        String hinhThucTTStr = hd.getHinhThucTT() != null ? hd.getHinhThucTT().getDisplayName() : "Chưa chọn";
        // ===========================================

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {

            // === 1, 2, 3: HEADER, TIÊU ĐỀ, THÔNG TIN CHUNG ===
            // Header (Tên quán, địa chỉ, SĐT)
            contentStream.beginText();
            contentStream.setFont(fontBold, 14);
            float titleWidth = fontBold.getStringWidth("NHÀ HÀNG TỨ HỮU") / 1000 * 14;
            contentStream.newLineAtOffset((PAGE_WIDTH - titleWidth) / 2, pos.y);
            contentStream.showText("NHÀ HÀNG TỨ HỮU");
            contentStream.endText();
            pos.y -= LINE_HEIGHT * 1.5;

            contentStream.beginText();
            contentStream.setFont(font, 10);
            String address = "Địa chỉ: 77 Hồ Tùng Mậu, Phường Châu Đốc, An Giang";
            float addressWidth = font.getStringWidth(address) / 1000 * 10;
            contentStream.newLineAtOffset((PAGE_WIDTH - addressWidth) / 2, pos.y);
            contentStream.showText(address);
            contentStream.endText();
            pos.y -= LINE_HEIGHT;

            contentStream.beginText();
            contentStream.setFont(font, 10);
            String sdt = "SĐT: 0909 123 456"; // Thay SĐT thật
            float sdtWidth = font.getStringWidth(sdt) / 1000 * 10;
            contentStream.newLineAtOffset((PAGE_WIDTH - sdtWidth) / 2, pos.y);
            contentStream.showText(sdt);
            contentStream.endText();
            pos.y -= LINE_HEIGHT * 1.5;

            // Tiêu đề hóa đơn
            contentStream.beginText();
            contentStream.setFont(fontBold, 16);
            titleWidth = fontBold.getStringWidth("HÓA ĐƠN THANH TOÁN") / 1000 * 16;
            contentStream.newLineAtOffset((PAGE_WIDTH - titleWidth) / 2, pos.y);
            contentStream.showText("HÓA ĐƠN THANH TOÁN");
            contentStream.endText();
            pos.y -= LINE_HEIGHT * 1.2;

            contentStream.beginText();
            contentStream.setFont(fontBold, 12);
            String maHDLabel = "SỐ HĐ: " + (hd.getMaHD() != null ? hd.getMaHD() : "N/A");
            float maHDWidth = fontBold.getStringWidth(maHDLabel) / 1000 * 12;
            contentStream.newLineAtOffset((PAGE_WIDTH - maHDWidth) / 2, pos.y);
            contentStream.showText(maHDLabel);
            contentStream.endText();
            pos.y -= LINE_HEIGHT * 1.8;

            // Thông tin chung
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
            contentStream.showText("Thu ngân: " + tenThuNgan);
            contentStream.endText();
            pos.y -= LINE_HEIGHT * 0.9;
            // Dòng 2
            contentStream.beginText();
            contentStream.newLineAtOffset(COL_1_START, pos.y);
            contentStream.showText("Ngày: " + ngayStr);
            contentStream.setTextMatrix(org.apache.pdfbox.util.Matrix.getTranslateInstance(COL_2_START, pos.y));
            contentStream.showText("Khách hàng: " + khachHangStr);
            contentStream.endText();
            pos.y -= LINE_HEIGHT * 0.9;
            // Dòng 3
            contentStream.beginText();
            contentStream.newLineAtOffset(COL_1_START, pos.y);
            contentStream.showText("Giờ vào: " + gioVaoStr);
            contentStream.setTextMatrix(org.apache.pdfbox.util.Matrix.getTranslateInstance(COL_2_START, pos.y));
            contentStream.showText("Giờ ra: " + gioRaStr);
            contentStream.endText();
            pos.y -= LINE_HEIGHT * 1.5;


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
                MyReceiptItem mon = monAnList.get(i);
                contentStream.beginText();
                contentStream.newLineAtOffset(colSTT, currentY);
                contentStream.showText(String.valueOf(i + 1));
                contentStream.setTextMatrix(org.apache.pdfbox.util.Matrix.getTranslateInstance(colTenMon, currentY));
                contentStream.showText(mon.getTenMon());
                String slStr = String.valueOf(mon.getSoLuong());
                float slWidth = font.getStringWidth(slStr) / 1000 * FONT_SIZE_TABLE;
                contentStream.setTextMatrix(org.apache.pdfbox.util.Matrix.getTranslateInstance(colSL - slWidth + 15, currentY));
                contentStream.showText(slStr);
                String dgStr = currencyFormatter.format(mon.getDonGia());
                float dgWidth = font.getStringWidth(dgStr) / 1000 * FONT_SIZE_TABLE;
                contentStream.setTextMatrix(org.apache.pdfbox.util.Matrix.getTranslateInstance(colDonGia - dgWidth + 15, currentY));
                contentStream.showText(dgStr);
                String ttStr = currencyFormatter.format(mon.getThanhTien());
                float ttWidth = fontBold.getStringWidth(ttStr) / 1000 * FONT_SIZE_TABLE;
                contentStream.setTextMatrix(org.apache.pdfbox.util.Matrix.getTranslateInstance(colThanhTien - ttWidth + 15, currentY));
                contentStream.showText(ttStr);
                contentStream.endText();
                currentY -= LINE_HEIGHT * 1.6f;
            }
            pos.y = currentY + LINE_HEIGHT * 1.6f;
            pos.y -= LINE_HEIGHT * 1.0;

            // === 5. TỔNG KẾT CHI TIẾT ===
            final float FONT_SIZE_SUMMARY = 10;
            final float SUMMARY_INDENT = MARGIN;
            final float SUMMARY_VALUE_COL = PAGE_WIDTH - MARGIN;

            // Lớp helper SummaryDrawer
            class SummaryDrawer {
                private final PDFont regularFont;
                private final PDFont boldFont;

                SummaryDrawer(final PDFont rf, final PDFont bf) {
                    this.regularFont = rf;
                    this.boldFont = bf;
                }

                void draw(String label, String value, boolean isBold, boolean isTotal) throws IOException {
                    float cfs = isTotal ? 12 : FONT_SIZE_SUMMARY;
                    PDFont cf = isBold ? boldFont : regularFont;
                    contentStream.beginText();
                    contentStream.setFont(cf, cfs);
                    contentStream.newLineAtOffset(SUMMARY_INDENT, pos.y);
                    contentStream.showText(label);
                    contentStream.endText();
                    float vw = cf.getStringWidth(value) / 1000 * cfs;
                    contentStream.beginText();
                    contentStream.setFont(cf, cfs);
                    contentStream.newLineAtOffset(SUMMARY_VALUE_COL - vw, pos.y);
                    contentStream.showText(value);
                    contentStream.endText();
                    pos.y -= LINE_HEIGHT * (isTotal ? 1.4f : 1.1f);
                }
            }
            SummaryDrawer drawer = new SummaryDrawer(font, fontBold);

            // Vẽ các dòng summary
            drawer.draw("Tổng cộng món ăn:", currencyFormatter.format(hd.getTongCongMonAn()) + " VNĐ", true, false);
            drawer.draw("Phí dịch vụ (5%):", currencyFormatter.format(hd.getPhiDichVu()) + " VNĐ", false, false);
            drawer.draw("Thuế VAT (8%):", currencyFormatter.format(hd.getThueVAT()) + " VNĐ", false, false);
            drawer.draw("Tiền đặt cọc bàn:", "-" + currencyFormatter.format(hd.getTienCoc()) + " VNĐ", false, false);

            // Vẽ đường kẻ và Tổng thanh toán
            pos.y += LINE_HEIGHT * 0.5f;
            contentStream.setLineWidth(0.5f);
            contentStream.moveTo(SUMMARY_INDENT, pos.y);
            contentStream.lineTo(SUMMARY_VALUE_COL, pos.y);
            contentStream.stroke();
            pos.y -= LINE_HEIGHT * 1.2f;
            drawer.draw("Tổng thanh toán:", currencyFormatter.format(hd.getTongTienThanhToan()) + " VNĐ", true, true);
            pos.y -= LINE_HEIGHT * 0.5f;
            contentStream.moveTo(SUMMARY_INDENT, pos.y);
            contentStream.lineTo(SUMMARY_VALUE_COL, pos.y);
            contentStream.stroke();
            pos.y -= LINE_HEIGHT * 1.2f;

            // Chi tiết khác
            String khachHangMemberDetails = "N/A"; // Cần logic thực tế
            if (sourceForHeader.getKhachHang() != null && "VIP".equals(sourceForHeader.getKhachHang().getThanhVien())) {
                khachHangMemberDetails = "Gold (giảm 10%)"; // Ví dụ
            }
            String uuDaiStr = (hd.getKhuyenMai() > 0) ? ("-" + currencyFormatter.format(hd.getKhuyenMai()) + " VNĐ") : "0 VNĐ";
            double soTienKhachTra = hd.getTongTienThanhToan() + hd.getKhuyenMai(); // Giả định KM đã bao gồm trong tổng

            drawer.draw("Hình thức thanh toán:", hinhThucTTStr, false, false);
            drawer.draw("Khách hàng thành viên:", khachHangMemberDetails, false, false);
            drawer.draw("Ưu đãi áp dụng:", uuDaiStr, false, false);
            drawer.draw("Số tiền khách trả:", currencyFormatter.format(Math.max(0, soTienKhachTra)) + " VNĐ", true, false);

            // === 6. FOOTER ===
            pos.y -= LINE_HEIGHT * 0.2;
            contentStream.moveTo(SUMMARY_INDENT, pos.y);
            contentStream.lineTo(SUMMARY_VALUE_COL, pos.y);
            contentStream.stroke();
            pos.y -= LINE_HEIGHT * 2.5;
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

	private void closePopup() {
        Stage stage = (Stage) btnHuy.getScene().getWindow();
        stage.close();
    }
	// [Thêm vào cuối file ThanhToanPreviewController.java]

    // --- CÁC HÀM MỞ POPUP THANH TOÁN (ĐÃ SAO CHÉP VÀ CHỈNH SỬA) ---

    /**
     * Mở Popup Thanh toán Tiền mặt.
     */
    private void moPopupThanhToanTienMat() {
        if (hoaDonToPay == null) { // Sử dụng hoaDonToPay
            showAlert(AlertType.WARNING, "Lỗi", "Vui lòng chọn Hóa đơn trước khi thanh toán.");
            return;
        }

        calculateAndDisplayTotals(); // Gọi hàm tính tổng của Controller này
        double tongTienThanhToan = 0;
        try {
            // Lấy tổng tiền từ Label của Controller này
            String amountStr = lblTongThanhToan.getText().replaceAll("[^0-9]", "");
            tongTienThanhToan = Double.parseDouble(amountStr.isEmpty() ? "0" : amountStr);
        } catch (NumberFormatException e) {
            showAlert(AlertType.ERROR, "Lỗi", "Không thể xác định tổng tiền thanh toán.");
            return;
        }

        final double finalTotal = tongTienThanhToan;
        final DecimalFormat currencyFormatter = new DecimalFormat("###,###");

        // ... (Code tạo giao diện FlowPane, TextFields, Button btnXacNhan giữ nguyên) ...
        FlowPane flowButtonContainer = new FlowPane(10, 10);
        double[] presetValues = {100000, 200000, 500000, 1000000, 1500000, 2000000, 2500000, 3000000, 5000000};
        ToggleGroup presetGroup = new ToggleGroup();
        flowButtonContainer.setPrefWrapLength(420);

        TextField txtTienKhachDua = new TextField("0");
        TextField txtTienTraLai = new TextField("0");
        Button btnXacNhan = new Button("Xác nhận");
        btnXacNhan.setMaxWidth(Double.MAX_VALUE);
        btnXacNhan.setStyle("-fx-background-color: #ff9900; -fx-text-fill: white; -fx-font-size: 1.2em; -fx-font-weight: bold; -fx-padding: 12px 0; -fx-background-radius: 5px; -fx-border-radius: 5px;");
        txtTienTraLai.setEditable(false);
        txtTienKhachDua.setAlignment(Pos.CENTER_RIGHT);
        txtTienTraLai.setAlignment(Pos.CENTER_RIGHT);


        // ... (Code Runnable capNhatTienThoi giữ nguyên) ...
         Runnable capNhatTienThoi = () -> {
            String cleanedAmount = txtTienKhachDua.getText().replaceAll("[^0-9]", "");
            double tienKhachDua = 0;
            try {
                tienKhachDua = Double.parseDouble(cleanedAmount.isEmpty() ? "0" : cleanedAmount);
            } catch (NumberFormatException ignored) {}
            double tienTraLai = tienKhachDua - finalTotal;
            txtTienTraLai.setText(currencyFormatter.format(Math.max(0, tienTraLai)));
            if (tienKhachDua < finalTotal) {
                btnXacNhan.setStyle("-fx-background-color: #e0e0e0; -fx-text-fill: #999999; -fx-font-size: 1.2em; -fx-font-weight: bold; -fx-padding: 12px 0; -fx-background-radius: 5px; -fx-border-radius: 5px;");
                txtTienKhachDua.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
                btnXacNhan.setDisable(true);
            } else {
                btnXacNhan.setStyle("-fx-background-color: #ff9900; -fx-text-fill: white; -fx-font-size: 1.2em; -fx-font-weight: bold; -fx-padding: 12px 0; -fx-background-radius: 5px; -fx-border-radius: 5px;");
                txtTienKhachDua.setStyle(null);
                btnXacNhan.setDisable(false);
            }
        };


        // ... (Code xử lý nút preset và nhập tay giữ nguyên) ...
        for (double value : presetValues) {
            ToggleButton btn = new ToggleButton(currencyFormatter.format(value) + " VNĐ");
            btn.setUserData(value); btn.setToggleGroup(presetGroup);
            btn.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #0d6efd; -fx-font-weight: 500; -fx-font-size: 0.95em; -fx-padding: 10px 15px; -fx-border-radius: 5px; -fx-background-radius: 5px; -fx-border-color: #0d6efd; -fx-border-width: 1px;");
            btn.setPrefWidth(125); btn.setPrefHeight(40);
            btn.setOnAction(e -> { txtTienKhachDua.setText(currencyFormatter.format(value)); capNhatTienThoi.run(); });
            flowButtonContainer.getChildren().add(btn);
        }
        txtTienKhachDua.textProperty().addListener((obs, oldVal, newVal) -> {
             String filtered = newVal.replaceAll("[^0-9]", "");
             try {
                 if (!filtered.isEmpty()) { double value = Double.parseDouble(filtered); txtTienKhachDua.setText(currencyFormatter.format(value)); Platform.runLater(txtTienKhachDua::end); } else { txtTienKhachDua.setText(""); }
             } catch (NumberFormatException ignored) {}
             capNhatTienThoi.run();
        });


        // ... (Code thiết lập Layout GridPane root giữ nguyên) ...
        Label lblTitle = new Label("Tiền mặt"); lblTitle.setStyle("-fx-font-size: 1.5em; -fx-font-weight: bold;");
        VBox inputContainer = new VBox(10); inputContainer.getChildren().addAll( createInputGridRow("Tiền khách đưa:", txtTienKhachDua, true), createInputGridRow("Tiền trả lại khách:", txtTienTraLai, false) );
        GridPane root = new GridPane(); root.setVgap(20); root.setPadding(new Insets(20)); root.setPrefWidth(450); root.setPrefHeight(480); root.setStyle("-fx-background-color: white;");
        ColumnConstraints column = new ColumnConstraints(); column.setPercentWidth(100); root.getColumnConstraints().add(column);
        root.add(lblTitle, 0, 0); root.add(flowButtonContainer, 0, 1); root.add(inputContainer, 0, 2); root.add(btnXacNhan, 0, 3);
        GridPane.setHalignment(lblTitle, HPos.CENTER); GridPane.setHalignment(flowButtonContainer, HPos.CENTER); GridPane.setHalignment(btnXacNhan, HPos.CENTER);
        GridPane.setFillWidth(btnXacNhan, true); GridPane.setFillWidth(inputContainer, true);


        // Thiết lập Stage
        Stage popupStage = new Stage();
        popupStage.setTitle("Thanh toán tiền mặt");
        Scene scene = new Scene(root);
        popupStage.setScene(scene);

        // 🔥 SỬA LẠI XỬ LÝ NÚT XÁC NHẬN
        btnXacNhan.setOnAction(e -> {
            // 1. Set PTTT tương ứng
            this.selectedPTTT = PTTThanhToan.TIEN_MAT;
            // 2. Đóng popup này TRƯỚC khi gọi hàm lưu
            popupStage.close();
            // 3. Gọi hàm lưu/thanh toán chính của ThanhToanPreviewController
            handleFinalThanhToan();
        });

        popupStage.show();
    }

    /**
     * Mở Popup hiển thị QR Thanh toán Ngân hàng.
     */
    private void openNganHangQrPopup() {
        if (hoaDonToPay == null || hoaDonToPay.getMaHD() == null) { // Sử dụng hoaDonToPay
            showAlert(AlertType.WARNING, "Lỗi", "Không có mã hóa đơn hợp lệ để tạo QR.");
            return;
        }

        calculateAndDisplayTotals(); // Gọi hàm tính tổng của Controller này
        double tongTienThanhToan = 0;
        try {
            // Lấy tổng tiền từ Label của Controller này
            String amountStr = lblTongThanhToan.getText().replaceAll("[^0-9]", "");
            tongTienThanhToan = Double.parseDouble(amountStr.isEmpty() ? "0" : amountStr);
        } catch (NumberFormatException e) {
            showAlert(AlertType.ERROR, "Lỗi", "Không thể xác định tổng tiền thanh toán.");
            return;
        }

        if (tongTienThanhToan <= 0) {
             showAlert(AlertType.WARNING, "Lỗi", "Tổng tiền thanh toán phải lớn hơn 0.");
            return;
        }

        // ... (Code chuẩn bị thông tin BANK_CODE, ACCOUNT_NUMBER, qrUrl giữ nguyên) ...
        final String YOUR_BANK_CODE = "970422"; // MB Bank BIN
        final String YOUR_ACCOUNT_NUMBER = "0927432020905"; // Số tài khoản của bạn.
        String maHD = hoaDonToPay.getMaHD(); // Sử dụng hoaDonToPay
        String rawContent = "TT" + maHD.toUpperCase().replace(" ", "_");
        String encodedContent; try { encodedContent = java.net.URLEncoder.encode(rawContent, "UTF-8"); } catch (java.io.UnsupportedEncodingException e) { encodedContent = rawContent; }
        String qrUrl = String.format( "https://img.vietqr.io/image/%s-%s-compact.png?amount=%d&addInfo=%s", YOUR_BANK_CODE, YOUR_ACCOUNT_NUMBER, (int) Math.ceil(tongTienThanhToan), encodedContent );

        // ... (Code tải ảnh QR giữ nguyên, gọi generateQrCodeImageFromUrl) ...
        Image qrImage = generateQrCodeImageFromUrl(qrUrl, 250);
        if (qrImage == null || qrImage.isError()) { showAlert(AlertType.ERROR, "Lỗi kết nối", "..."); return; }


        // ... (Code tạo giao diện ImageView, Labels, Buttons btnHuy, btnXacNhanThanhToan giữ nguyên) ...
        ImageView qrView = new ImageView(qrImage); qrView.setFitWidth(250); qrView.setFitHeight(250);
        Label lblTitle = new Label("Quét Mã Thanh Toán VietQR"); lblTitle.setStyle("-fx-font-size: 1.5em; -fx-font-weight: bold;");
        Label lblAmount = new Label("Số tiền: " + String.format("%,.0f Đ", tongTienThanhToan)); lblAmount.setStyle("-fx-font-size: 1.2em; -fx-font-weight: 500; -fx-text-fill: red;");
        Label lblContent = new Label("Nội dung: " + rawContent); lblContent.setStyle("-fx-font-size: 1.0em; -fx-font-weight: 400;");
        Button btnHuy = new Button("Hủy"); Button btnXacNhanThanhToan = new Button("Xác nhận đã thanh toán");
        btnHuy.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-padding: 10px 20px; -fx-font-weight: bold;"); btnHuy.getStyleClass().add("action-button-cancel");
        btnXacNhanThanhToan.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-padding: 10px 20px; -fx-font-weight: bold;"); btnXacNhanThanhToan.getStyleClass().add("confirm-button");
        HBox buttonBox = new HBox(15, btnHuy, btnXacNhanThanhToan); buttonBox.setAlignment(Pos.CENTER);
        VBox root = new VBox(20, lblTitle, qrView, lblAmount, lblContent, buttonBox); root.setPadding(new Insets(20)); root.setAlignment(Pos.CENTER); root.setPrefSize(400, 550);


        // Thiết lập Stage
        Stage popupStage = new Stage();
        popupStage.setTitle("Thanh toán Ngân hàng");
        popupStage.setScene(new Scene(root));

        // GÁN SỰ KIỆN CHO NÚT
        btnHuy.setOnAction(e -> popupStage.close());

        // 🔥 SỬA LẠI XỬ LÝ NÚT XÁC NHẬN
        btnXacNhanThanhToan.setOnAction(e -> {
            // 1. Set PTTT tương ứng
            this.selectedPTTT = PTTThanhToan.NGAN_HANG;
            // 2. Đóng popup này TRƯỚC khi gọi hàm lưu
            popupStage.close();
            // 3. Gọi hàm lưu/thanh toán chính của ThanhToanPreviewController
            handleFinalThanhToan();
        });

        popupStage.show();
    }

     /**
     * Mở Popup hiển thị QR Thanh toán MoMo.
     */
    private void openMoMoQrPopup() {
        if (hoaDonToPay == null || hoaDonToPay.getMaHD() == null) { // Sử dụng hoaDonToPay
            showAlert(AlertType.WARNING, "Lỗi", "Không có mã hóa đơn hợp lệ để tạo QR.");
            return;
        }

        calculateAndDisplayTotals(); // Gọi hàm tính tổng của Controller này
        double tongTienThanhToan = 0;
        try {
            // Lấy tổng tiền từ Label của Controller này
            String amountStr = lblTongThanhToan.getText().replaceAll("[^0-9]", "");
            tongTienThanhToan = Double.parseDouble(amountStr.isEmpty() ? "0" : amountStr);
        } catch (NumberFormatException e) {
            showAlert(AlertType.ERROR, "Lỗi", "Không thể xác định tổng tiền thanh toán.");
            return;
        }
         if (tongTienThanhToan <= 0) {
             showAlert(AlertType.WARNING, "Lỗi", "Tổng tiền thanh toán phải lớn hơn 0.");
            return;
        }

        // ... (Code chuẩn bị thông tin BANK_CODE BVBank, ACCOUNT_NUMBER, qrUrl giữ nguyên) ...
         final String YOUR_BANK_CODE = "970454"; final String YOUR_ACCOUNT_NUMBER = "99MM24030M69605648"; String maHD = hoaDonToPay.getMaHD(); // Sử dụng hoaDonToPay
         String rawContent = "TT" + maHD.toUpperCase().replace(" ", "_"); String encodedContent; try { encodedContent = java.net.URLEncoder.encode(rawContent, "UTF-8"); } catch (java.io.UnsupportedEncodingException e) { encodedContent = rawContent; }
         String qrUrl = String.format( "https://img.vietqr.io/image/%s-%s-compact.png?amount=%d&addInfo=%s", YOUR_BANK_CODE, YOUR_ACCOUNT_NUMBER, (int) Math.ceil(tongTienThanhToan), encodedContent );


        // ... (Code tải ảnh QR giữ nguyên) ...
        Image qrImage = generateQrCodeImageFromUrl(qrUrl, 250);
         if (qrImage == null || qrImage.isError()) { showAlert(AlertType.ERROR, "Lỗi kết nối", "..."); return; }


        // ... (Code tạo giao diện ImageView logoView, qrView, Labels, Buttons btnHuy, btnXacNhanThanhToan giữ nguyên) ...
         ImageView qrView = new ImageView(qrImage); qrView.setFitWidth(250); qrView.setFitHeight(250);
        ImageView logoView = new ImageView(); try { Image logoMomo = new Image(getClass().getResourceAsStream("/images/MoMo_Logo.png")); logoView.setImage(logoMomo); logoView.setFitHeight(40); logoView.setPreserveRatio(true); } catch (Exception e) { System.err.println("Lỗi tải logo MoMo."); return; }
        Label lblAmount = new Label("Số tiền: " + String.format("%,.0f Đ", tongTienThanhToan)); lblAmount.setStyle("-fx-font-size: 1.2em; -fx-font-weight: 500; -fx-text-fill: red;");
        Label lblContent = new Label("Nội dung: " + rawContent); lblContent.setStyle("-fx-font-size: 1.0em; -fx-font-weight: 400;");
        Button btnHuy = new Button("Hủy"); Button btnXacNhanThanhToan = new Button("Xác nhận đã thanh toán");
        btnHuy.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-padding: 10px 20px; -fx-font-weight: bold;");
        btnXacNhanThanhToan.setStyle("-fx-background-color: #b0006d; -fx-text-fill: white; -fx-padding: 10px 20px; -fx-font-weight: bold;");
        HBox buttonBox = new HBox(15, btnHuy, btnXacNhanThanhToan); buttonBox.setAlignment(Pos.CENTER);
        VBox root = new VBox(20, logoView, qrView, lblAmount, lblContent, buttonBox); root.setPadding(new Insets(20)); root.setAlignment(Pos.CENTER); root.setPrefSize(400, 550);


        // Thiết lập Stage
        Stage popupStage = new Stage();
        popupStage.setTitle("Thanh toán MoMo/VietQR");
        popupStage.setScene(new Scene(root));

        // GÁN SỰ KIỆN CHO NÚT
        btnHuy.setOnAction(e -> popupStage.close());

        // 🔥 SỬA LẠI XỬ LÝ NÚT XÁC NHẬN
        btnXacNhanThanhToan.setOnAction(e -> {
            // 1. Set PTTT tương ứng
            this.selectedPTTT = PTTThanhToan.VI_DIEN_TU; // MoMo
            // 2. Đóng popup này TRƯỚC khi gọi hàm lưu
            popupStage.close();
            // 3. Gọi hàm lưu/thanh toán chính của ThanhToanPreviewController
            handleFinalThanhToan();
        });

        popupStage.show();
    }

    // --- CÁC HÀM PHỤ TRỢ (ĐÃ SAO CHÉP VÀ CHỈNH SỬA) ---

    /**
     * Hàm helper tạo một hàng trong GridPane cho popup tiền mặt.
     */
    private GridPane createInputGridRow(String labelText, TextField textField, boolean showInputLabel) {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(5);
        grid.setAlignment(Pos.CENTER_LEFT);

        Label label = new Label(labelText);
        label.setStyle("-fx-font-size: 1.05em; -fx-text-fill: #333333; -fx-font-weight: 500;");
        grid.add(label, 0, 1);

        textField.setEditable(showInputLabel);
        textField.setPrefWidth(150);
        textField.setStyle("-fx-border-color: #ced4da; -fx-border-width: 1px; -fx-border-radius: 5px; -fx-padding: 8px 10px;");
        grid.add(textField, 1, 1);

        if (showInputLabel) {
            Label inputLabel = new Label("Input");
            inputLabel.setStyle("-fx-font-size: 0.75em; -fx-text-fill: #999999;");
            GridPane.setHalignment(inputLabel, HPos.LEFT);
            grid.add(inputLabel, 1, 0);
        }

        ColumnConstraints col1 = new ColumnConstraints(); col1.setHgrow(Priority.NEVER); col1.setPrefWidth(120);
        ColumnConstraints col2 = new ColumnConstraints(); col2.setHgrow(Priority.ALWAYS); col2.setMinWidth(150);
        grid.getColumnConstraints().addAll(col1, col2);

        return grid;
    }

     /**
     * Tải ảnh QR Code từ một URL.
     */
    private Image generateQrCodeImageFromUrl(String url, int size) {
        try {
            Image image = new Image(url, size, size, true, true);
            if (image.isError()) {
                System.err.println("Lỗi tải ảnh QR từ URL: " + image.getException().getMessage());
                return null;
            }
            return image;
        } catch (Exception e) {
            System.err.println("Lỗi không xác định khi tải ảnh QR: " + e.getMessage());
            return null;
        }
    }

     /**
      * Vô hiệu hóa các nút thanh toán trong popup preview sau khi đã thanh toán thành công.
      */
     private void disablePopupPaymentButtons() {
         if (btnTienMat != null) btnTienMat.setDisable(true);
         if (btnNganHang != null) btnNganHang.setDisable(true);
         if (btnMoMo != null) btnMoMo.setDisable(true);
         if (btnXacNhan != null) btnXacNhan.setDisable(true);
         // Nút In Hóa đơn sẽ được xử lý riêng trong handleFinalThanhToan
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
package ui;

// === IMPORTS CẦN THIẾT CHO LOGIC ĐẶT BÀN VÀ DAO ===
import dao.DatBanDAO; 
import ui.MainApp;
import dao.MonAnDAO;       
import dao.DanhMucMonDAO;  
import entity.Ban; 
import entity.HoaDon; 
import entity.KhachHang; 
import entity.LoaiBan; 
import entity.DanhMucMon;  
import entity.TrangThaiBan; 
import entity.TrangThaiHoaDon;
import entity.PTTThanhToan; // Import PTTThanhToan
import entity.TaiKhoan;
import dao.UuDaiDAO; // Đảm bảo đã import
import entity.UuDai; // Đảm bảo đã import

// Import JavaFX
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.awt.print.PrinterJob;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.printing.PDFPrintable;

import javafx.application.Platform; 
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader; 
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene; 
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.beans.value.ObservableValue; 
import javax.print.attribute.HashPrintRequestAttributeSet; 
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.Doc; 
import javax.print.SimpleDoc;


public class DatBan implements Initializable {
    
    // DAO
    private final DatBanDAO datBanDAO = new DatBanDAO(); 
    private final MonAnDAO monAnDAO = new MonAnDAO();           
    private final DanhMucMonDAO danhMucMonDAO = new DanhMucMonDAO(); 
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private final UuDaiDAO uuDaiDAO = new UuDaiDAO();
    
    // Data
    private ObservableList<MonOrder> monOrderList = FXCollections.observableArrayList();
    private List<DanhMucMon> dsDanhMuc; 
    
    private ObservableList<Ban> selectedBanList = FXCollections.observableArrayList();
    private ObservableList<Button> selectedButtonList = FXCollections.observableArrayList();
    
    private List<HoaDon> dsHoaDonDatTrongNgay = new ArrayList<>(); 
    
    
    private boolean isBookingConfirmed = false; 
    
    private HoaDon currentHoaDon = null;
    private List<HoaDon> currentHoaDonGocVaPhu = new ArrayList<>();
    private Map<String, Button> tableButtonMap = new HashMap<>();// <<< THÊM DÒNG NÀY
    private ToggleGroup menuGroup;
    private ToggleGroup paymentGroup;
    private List<UuDai> dsUuDaiDangApDung = new ArrayList<>(); // 🔥 Danh sách ưu đãi đang áp dụng
    private UuDai selectedUuDai = null; // 🔥 Ưu đãi đang được chọn
    
    
    // === CỘT 1 (Left Panel) ===
    @FXML private ComboBox<String> comboFilter;
    @FXML private TextField txtSearch;
    @FXML private VBox vboxBookingCards;
    
    @FXML private ScrollPane middleScrollPane; 
    @FXML private ScrollPane vboxReceipt;
    
    // === CỘT 2 (Middle Panel) ===
    @FXML private TextField txtThoiGian;
    @FXML private DatePicker datePickerThoiGianDen;
    @FXML private Button btnTim;
    @FXML private GridPane gridTangTret;
    @FXML private GridPane gridTang1;
    @FXML private GridPane gridPhongRieng;
    @FXML private TextField txtTenKhachHang;
    @FXML private TextField txtSoDienThoai;
    @FXML private TextField txtSoLuongKhach;
    @FXML private TextField txtYeuCau;
    @FXML private TextField txtTienCoc;
    @FXML private Button btnThanhToanCoc;
    @FXML private Label lblTrangThaiBan;
    @FXML private Label lblBanDangChon;
    @FXML private Button btnTimKhach;
    @FXML private Button btnXacNhanBan;
    
    // === CỘT 3 (Right Panel) - GỌI MÓN & HÓA ĐƠN ===
    @FXML private ToggleButton toggleKhaiVi, toggleNuong, toggleLau, toggleXaoHap, toggleChien, toggleDacSan, toggleDoUong;
    @FXML private Button btnLuuDatHang; 
    @FXML private Button btnThanhToan;
    @FXML private TextField txtTimMon;
    @FXML private Button btnSuaMon;
    @FXML private Button btnDoiBan;
    @FXML private Button btnTachBan;
    @FXML private Button btnGopBan;
    @FXML private Button btnHuyBan;
    // Table Món Ăn (Menu)
    @FXML private TableView<MonAn> tblMonAn;
    @FXML private TableColumn<MonAn, String> colTenMon;
    @FXML private TableColumn<MonAn, String> colHinhAnh;
    @FXML private TableColumn<MonAn, Number> colGia;
    @FXML private TableColumn<MonAn, Void> colChon;
    
    // Table Món Đã Chọn (Order)
    @FXML private TableView<MonOrder> tblMonDaChon;
    @FXML private TableColumn<MonOrder, String> colOrderTenMon;
    @FXML private TableColumn<MonOrder, Number> colOrderDonGia;
    @FXML private TableColumn<MonOrder, Integer> colOrderSoLuong;
    @FXML private TableColumn<MonOrder, Void> colOrderTangGiam;
    @FXML private TableColumn<MonOrder, Void> colOrderHuy;

    // Hóa đơn Summary
    @FXML private TableView<MonOrder> tblHoaDon;
    @FXML private ComboBox<String> promoComboBox;
    @FXML private Label lblTongTienMonAn;
    @FXML private Label lblPhiDichVu;
    @FXML private Label lblThueVAT;
    @FXML private Label lblKhuyenMai;
    @FXML private Label lblTienCocSummary;
    @FXML private Label lblTongTienThanhToan;
    @FXML private Label lblSoHD;
    @FXML private Label lblBanHD;
    @FXML private Label lblThuNgan;
    @FXML private Label lblGioVao;
    @FXML private Label lblGioRa;

    // Phương thức thanh toán
    @FXML private ToggleButton btnTienMat; 
    @FXML private ToggleButton btnNganHang;
    @FXML private ToggleButton btnMoMo;
    @FXML private Button btnInHoaDon;

    // =========================================================
    // HÀM KHỞI TẠO (INITIALIZE)
    // =========================================================
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        // ----------------------------------------------------
        // BƯỚC 1: TẢI DỮ LIỆU VÀ KHỞI TẠO CẤU TRÚC (DAO calls)
        // ----------------------------------------------------
        
        dsDanhMuc = danhMucMonDAO.getAllDanhMuc();
        
        menuGroup = new ToggleGroup();
        ToggleButton[] toggles = {toggleKhaiVi, toggleNuong, toggleLau, toggleXaoHap, toggleChien, toggleDacSan, toggleDoUong};
        
        for (int i = 0; i < dsDanhMuc.size() && i < toggles.length; i++) {
            ToggleButton toggle = toggles[i];
            DanhMucMon dm = dsDanhMuc.get(i);
            
            toggle.setText(dm.getTenDM());
            toggle.setUserData(dm.getMaDM());
            toggle.setToggleGroup(menuGroup);
        }
        
        setupMonAnTable();
        setupMonOrderTable();
        
        List<entity.MonAn> dsEntityMonAn = monAnDAO.getAllMonAn();
        loadMonAnTable(dsEntityMonAn.stream().map(MonAn::fromEntity).collect(Collectors.toList()));
        
        paymentGroup = new ToggleGroup();
        ToggleButton[] paymentToggles = {btnTienMat, btnNganHang, btnMoMo};
        for (ToggleButton toggle : paymentToggles) {
            toggle.setToggleGroup(paymentGroup);
        }

        loadBookingCards(); 
        loadTableGrids(); 


        // ----------------------------------------------------
        // BƯỚC 2: GÁN SỰ KIỆN VÀ CẬP NHẬT UI (Sử dụng @FXML fields an toàn)
        // ----------------------------------------------------
        
        promoComboBox.setItems(FXCollections.observableArrayList("Giảm 15% hóa đơn cho khách hàng VIP", "Không áp dụng"));
        promoComboBox.getSelectionModel().selectFirst();

        datePickerThoiGianDen.setValue(LocalDate.now());
        txtThoiGian.setText(LocalTime.now().format(timeFormatter));

        btnXacNhanBan.setOnAction(e -> handleXacNhanBan()); 
        btnTim.setOnAction(e -> handleTimBanTrong());
        btnLuuDatHang.setOnAction(e -> handleLuuDatHang());
        btnThanhToan.setOnAction(e -> handleThanhToan());
        
        ToggleButton[] togglesFinal = {toggleKhaiVi, toggleNuong, toggleLau, toggleXaoHap, toggleChien, toggleDacSan, toggleDoUong};
        for(ToggleButton toggle : togglesFinal) {
            if(toggle != null && toggle.getUserData() != null) {
                toggle.setOnAction(e -> handleMenuToggle((String)toggle.getUserData()));
            }
        }
        
        if (txtTimMon != null) {
            txtTimMon.textProperty().addListener((obs, oldVal, newVal) -> handleTimMon(newVal));
        } else {
             System.err.println("LỖI CẤU HÌNH FXML: Không thể inject txtTimMon.");
        }

        txtSoDienThoai.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) { 
                handleTimTenKhachHang(txtSoDienThoai.getText());
            }
        });
        
        updateSelectionLabels();
        
        calculateTotal();
        
        if (vboxReceipt != null) {
            vboxReceipt.setVisible(false);
        }
		// === THÊM MỚI: CÀI ĐẶT BỘ LỌC DANH SÁCH ĐẶT BÀN ===
        
        // 1. Thêm các lựa chọn vào ComboBox lọc
        if (comboFilter != null) {
            comboFilter.setItems(FXCollections.observableArrayList(
                "Tất cả", 
                "Đang phục vụ", 
                "Đã đặt"
            ));
            comboFilter.setValue("Tất cả"); // Đặt giá trị mặc định

            // 2. Gán sự kiện: Khi thay đổi filter, tải lại danh sách
            comboFilter.valueProperty().addListener((obs, oldVal, newVal) -> loadBookingCards());
        }

        // 3. Gán sự kiện: Khi gõ vào ô tìm kiếm, tải lại danh sách
        if (txtSearch != null) {
            txtSearch.textProperty().addListener((obs, oldVal, newVal) -> loadBookingCards());
        }
        loadPromoComboBox(); 
        
        if (promoComboBox != null) {
            // Sự kiện: Khi chọn khuyến mãi, tính toán lại tổng tiền
            promoComboBox.valueProperty().addListener((obs, oldVal, newVal) -> handlePromoSelection(newVal));
        }
        // === THÊM MỚI: GÁN SỰ KIỆN CHO CÁC NÚT CHỨC NĂNG MỚI ===
        btnSuaMon.setOnAction(e -> handleSuaHoaDon());
        btnDoiBan.setOnAction(e -> openDoiBanPopup());
        btnHuyBan.setOnAction(e -> handleHuyBan());
        
     // === GÁN SỰ KIỆN CHO CÁC NÚT CHỨC NĂNG MỚI ===
        btnSuaMon.setOnAction(e -> handleSuaHoaDon());
        btnDoiBan.setOnAction(e -> openDoiBanPopup());
        btnTachBan.setOnAction(e -> openTachBanPopup()); // << SỰ KIỆN MỚI CHO TÁCH BÀN
        btnGopBan.setOnAction(e -> openGopBanPopup());
        btnHuyBan.setOnAction(e -> handleHuyBan());
        btnInHoaDon.setOnAction(e -> handleInHoaDon());
        if (btnTienMat != null) {
            // 🔥 SỬA TÊN HÀM
            btnTienMat.setOnAction(e -> moPopupThanhToanTienMat()); 
        }
        if (btnNganHang != null) {
            // 🔥 SỬA TÊN HÀM
            btnNganHang.setOnAction(e -> openNganHangQrPopup()); 
        }
        if (btnMoMo != null) {
            btnMoMo.setOnAction(e -> openMoMoQrPopup()); 
        }
        // === THÊM MỚI: ĐẶT TRẠNG THÁI NÚT BAN ĐẦU ===
        updateButtonVisibility(false); // Ban đầu là trạng thái TẠO MỚI
    }
    
    // =========================================================
    // HÀM NGHIỆP VỤ TÁCH BÀN (CHÍNH)
    // =========================================================

 // ui.DatBan.java

    /**
     * 🔥 HÀM SỬA CUỐI CÙNG: Xử lý nút In Hóa đơn (In Hóa đơn TẠM TÍNH).
     * Cho phép in nếu HĐ đang ở trạng thái ĐANG SỬ DỤNG hoặc ĐÃ ĐẶT (Chưa thanh toán).
     */
    @FXML
    private void handleInHoaDon() {
        if (currentHoaDon == null || currentHoaDon.getMaHD() == null) {
            showAlert(Alert.AlertType.WARNING, "Lỗi", "Vui lòng chọn Hóa đơn để in.");
            return;
        }
        
        String trangThaiDb = currentHoaDon.getTrangThai().getDbValue();

        // 1. Nếu HĐ CHƯA thanh toán (Đang sử dụng, Đã đặt, Hóa đơn tạm)
        if (!trangThaiDb.equals(TrangThaiHoaDon.DA_THANH_TOAN.getDbValue())) {
            // 🔥 BẮT BUỘC: Lưu lại danh sách món đã sửa đổi trước khi in (Tạo bản nháp)
            handleSuaHoaDon(); 
            showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Đã lưu thay đổi và tiến hành in Hóa đơn TẠM TÍNH.");
        }
        
        // 2. Chạy trong Thread riêng để tránh treo UI
        new Thread(() -> {
            PDDocument document = null;
            try {
                // 3. TẠO DOCUMENT TRỰC TIẾP TỪ BỘ NHỚ
                document = createReceiptPdf(currentHoaDon);
                
                // 4. Lấy Job in từ AWT/Swing
                java.awt.print.PrinterJob job = java.awt.print.PrinterJob.getPrinterJob();
                PrintRequestAttributeSet attr = new HashPrintRequestAttributeSet();
                
                // 5. Mở hộp thoại chọn máy in
                if (job.printDialog(attr)) {
                    
                    java.awt.print.PageFormat pageFormat = job.getPageFormat(attr);
                    
                    // Cài đặt nội dung in (PDFPrintable)
                    org.apache.pdfbox.printing.PDFPrintable printableData = new org.apache.pdfbox.printing.PDFPrintable(document);
                    
                    job.setPrintable(printableData, pageFormat);
                    
                    // 6. GỬI LỆNH IN
                    job.print(attr); 
                    
                    Platform.runLater(() -> {
                        showAlert(Alert.AlertType.INFORMATION, "In Hóa đơn", "Đã gửi lệnh in cho Hóa đơn " + currentHoaDon.getMaHD() + " thành công.");
                    });
                } else {
                     Platform.runLater(() -> showAlert(Alert.AlertType.INFORMATION, "Hủy In", "Đã hủy thao tác in."));
                }

            } catch (java.awt.print.PrinterException e) {
                 Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Lỗi In", "Lỗi trong quá trình in ấn: " + e.getMessage()));
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Lỗi Hệ thống", "Lỗi khi tạo PDF: " + e.getMessage()));
            } finally {
                if (document != null) {
                    try {
                        document.close(); // Đóng document sau khi in
                    } catch (IOException ignored) {}
                }
            }
        }).start();
    }

 // Lớp YPosition của bạn (đặt ngoài phương thức, trong class DatBan)
    private static class YPosition {
        public float y;
        public YPosition(float initialY) {
            this.y = initialY;
        }
    }

    /**
     * 🔥 HÀM CUỐI CÙNG: Tạo đối tượng PDDocument theo MẪU ẢNH HÓA ĐƠN.
     * ĐÃ FIX: Điều chỉnh căn chỉnh cột Tên món (làm hẹp) và các cột giá trị (thêm padding) để tối ưu khoảng cách.
     * @param hd Hóa đơn đã thanh toán.
     * @return PDDocument chứa nội dung hóa đơn.
     */
    private PDDocument createReceiptPdf(HoaDon hd) throws IOException {
        PDDocument document = new PDDocument();
        // SỬ DỤNG A4
        PDPage page = new PDPage(org.apache.pdfbox.pdmodel.common.PDRectangle.A4);
        document.addPage(page);

        // Kích thước trang & margin
        final float PAGE_WIDTH = page.getMediaBox().getWidth();
        final float MARGIN = 72; 
        final float Y_START = page.getMediaBox().getHeight() - MARGIN;
        final float LINE_HEIGHT = 16; 
        
        final YPosition pos = new YPosition(Y_START); 

        // ====== 🔹 LOAD FONT HỖ TRỢ TIẾNG VIỆT (Giữ nguyên logic) ======
        org.apache.pdfbox.pdmodel.font.PDFont font = null;
        org.apache.pdfbox.pdmodel.font.PDFont fontBold = null; 
        
        try {
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

        if (font == null) font = org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA;
        if (fontBold == null) fontBold = org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA_BOLD;
        // ============================================================

        // ====== DỮ LIỆU CHUẨN BỊ VÀ TÍNH TOÁN LẠI TỔNG TIỀN ======
        ObservableList<MonOrder> monAnList = datBanDAO.getChiTietHoaDon(hd.getMaHD());
        
        double tongTienMonAn = monAnList.stream()
                .mapToDouble(order -> order.getDonGia() * order.getSoLuong())
                .sum();
        
        hd.setTongCongMonAn(tongTienMonAn); // Kích hoạt calculateTotals()

        final java.text.DecimalFormat currencyFormatter = new java.text.DecimalFormat("###,###");
        final java.time.format.DateTimeFormatter dateFormatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
        final java.time.format.DateTimeFormatter timeFormatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm");

        // --- Chuẩn bị dữ liệu hiển thị (Giữ nguyên) ---
        String banStr = hd.getBan() != null ? hd.getBan().getMaBan() : "N/A";
        String ngayStr = hd.getNgayLap() != null ? hd.getNgayLap().toLocalDate().format(dateFormatter) : "N/A";
        String gioVaoStr = hd.getGioVao() != null ? hd.getGioVao().toLocalTime().format(timeFormatter) : "N/A";
        String gioRaStr = LocalTime.now().format(timeFormatter);
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
        String khachHangStr = (hd.getKhachHang() != null && hd.getKhachHang().getSoDT() != null) ? hd.getKhachHang().getSoDT() : "N/A";
        String hinhThucTTStr = hd.getHinhThucTT() != null ? hd.getHinhThucTT().getDisplayName() : "N/A";
        // ============================================================

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
        
            // === 1, 2, 3: HEADER, TIÊU ĐỀ, THÔNG TIN CHUNG (Giữ nguyên) ===
            // 1.1 Tên Quán
            contentStream.beginText();
            contentStream.setFont(fontBold, 14); 
            float titleWidth = fontBold.getStringWidth("NHÀ HÀNG TỨ HỮU") / 1000 * 14;
            contentStream.newLineAtOffset((PAGE_WIDTH - titleWidth) / 2, pos.y);
            contentStream.showText("NHÀ HÀNG TỨ HỮU");
            contentStream.endText();
            pos.y -= LINE_HEIGHT * 1.5;
            
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
            pos.y -= LINE_HEIGHT * 1.5;

            // 2. TIÊU ĐỀ HÓA ĐƠN
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

            // 3. THÔNG TIN CHUNG
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

            // === 4. BẢNG MÓN ĂN (Giữ nguyên) ===
            final float FONT_SIZE_TABLE = 9; 
            float colSTT = MARGIN;                    // 72
            float colTenMon = MARGIN + 30;           // 102
            float colSL = PAGE_WIDTH - MARGIN - 180;  // 414
            float colDonGia = PAGE_WIDTH - MARGIN - 110; // 484
            float colThanhTien = PAGE_WIDTH - MARGIN - 30; // 564

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
                MonOrder mon = monAnList.get(i);
                
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
                String ttStr = currencyFormatter.format(mon.getDonGia() * mon.getSoLuong());
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
            pos.y -= LINE_HEIGHT * 1.2f; // Tăng khoảng cách xuống thêm (từ 0.8f lên 1.2f)

            // 5.3. Vẽ "Tổng thanh toán" ngay dưới đường kẻ
            drawer.draw("Tổng thanh toán:", currencyFormatter.format(hd.getTongTienThanhToan()) + " VNĐ", true, true);

            pos.y -= LINE_HEIGHT * 0.5f;

            // Đường kẻ dưới tổng thanh toán
            contentStream.moveTo(SUMMARY_INDENT, pos.y);
            contentStream.lineTo(SUMMARY_VALUE_COL, pos.y);
            contentStream.stroke();
            pos.y -= LINE_HEIGHT * 1.2f;
            
            // 5.4. Chi tiết Thanh toán, Ưu đãi, Khách trả
            String khachHangMemberDetails = "Gold (giảm 10%)"; 
            String uuDaiStr = (hd.getKhuyenMai() > 0) ? ("-" + currencyFormatter.format(hd.getKhuyenMai()) + " VNĐ") : "0 VNĐ";
            double soTienKhachTra = hd.getTongTienThanhToan() + hd.getKhuyenMai(); 

            drawer.draw("Hình thức thanh toán:", hinhThucTTStr, false, false);
            drawer.draw("Khách hàng thành viên:", khachHangMemberDetails, false, false);
            drawer.draw("Ưu đãi áp dụng:", uuDaiStr, false, false);
            
            // Số tiền khách trả
            drawer.draw("Số tiền khách trả:", currencyFormatter.format(soTienKhachTra) + " VNĐ", true, false); 
            
            // Đường kẻ cuối cùng
            pos.y -= LINE_HEIGHT * 0.2;
            contentStream.moveTo(SUMMARY_INDENT, pos.y);
            contentStream.lineTo(SUMMARY_VALUE_COL, pos.y);
            contentStream.stroke();
            pos.y -= LINE_HEIGHT * 2.5;

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
    /**
     * 🔥 HÀM SỬA CUỐI CÙNG CHO MOMO: Mở Popup hiển thị QR Thanh toán (Sử dụng LOGO và BIN BVBank).
     * * SẼ CẦN KẾT NỐI INTERNET ĐỂ TẢI ẢNH QR.
     */
    private void openMoMoQrPopup() {
        if (currentHoaDon == null || currentHoaDon.getMaHD() == null) {
            showAlert(Alert.AlertType.WARNING, "Lỗi", "Không có mã hóa đơn hợp lệ để tạo QR.");
            return;
        }
        
        // 1. Chuẩn bị dữ liệu thanh toán
        calculateTotal(); 
        double tongTienThanhToan = 0;
        try {
            String amountStr = lblTongTienThanhToan.getText().replaceAll("[^0-9]", "");
            tongTienThanhToan = Double.parseDouble(amountStr.isEmpty() ? "0" : amountStr);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xác định tổng tiền thanh toán.");
            return;
        }
        
        if (tongTienThanhToan <= 0) {
            showAlert(Alert.AlertType.WARNING, "Lỗi", "Tổng tiền thanh toán phải lớn hơn 0.");
            return;
        }

        // 2. Thông tin Tĩnh (Cập nhật BIN BVBank)
        final String YOUR_BANK_CODE = "970454"; // 🔥 BIN BVBank ĐÃ SỬA
        final String YOUR_ACCOUNT_NUMBER = "99MM24030M69605648"; // Số tài khoản ảo MoMo/BVBank
        String maHD = currentHoaDon.getMaHD();
        
        String rawContent = "TT" + maHD.toUpperCase().replace(" ", "_");
        String encodedContent;
        try {
            encodedContent = java.net.URLEncoder.encode(rawContent, "UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            encodedContent = rawContent; 
        }
        
        // 3. Tạo URL Quicklink (Dùng Quicklink VietQR)
        String qrUrl = String.format(
            "https://img.vietqr.io/image/%s-%s-compact.png?amount=%d&addInfo=%s",
            YOUR_BANK_CODE, 
            YOUR_ACCOUNT_NUMBER, 
            (int) Math.ceil(tongTienThanhToan), 
            encodedContent
        );
        
        // 4. Tải ảnh QR từ Internet
        Image qrImage = generateQrCodeImageFromUrl(qrUrl, 250); 

        if (qrImage == null || qrImage.isError()) {
            showAlert(Alert.AlertType.ERROR, "Lỗi kết nối", "Không thể tải mã QR từ Quicklink VietQR. Vui lòng kiểm tra lại kết nối.");
            return; 
        }

        // 5. Tạo giao diện Popup
        ImageView qrView = new ImageView(qrImage);
        qrView.setFitWidth(250);
        qrView.setFitHeight(250);
        
        // 🔥 TẢI VÀ THAY THẾ LABEL BẰNG LOGO MOMO 🔥
        ImageView logoView = new ImageView();
        try {
            // Giả định file ảnh logo MoMo nằm trong /images/momo_logo.png
            Image logoMomo = new Image(getClass().getResourceAsStream("/images/MoMo_Logo.png"));
            logoView.setImage(logoMomo);
            logoView.setFitHeight(40); 
            logoView.setPreserveRatio(true);
        } catch (Exception e) {
            // Fallback nếu không tìm thấy ảnh
            System.err.println("Lỗi tải logo MoMo. Dùng Label thay thế.");
            return; // Dừng lại ở đây hoặc hiển thị lỗi
        }

        Label lblAmount = new Label("Số tiền: " + String.format("%,.0f Đ", tongTienThanhToan));
        lblAmount.setStyle("-fx-font-size: 1.2em; -fx-font-weight: 500; -fx-text-fill: red;");
        
        Label lblContent = new Label("Nội dung: " + rawContent);
        lblContent.setStyle("-fx-font-size: 1.0em; -fx-font-weight: 400;");

        // Tạo nút Hủy và Xác nhận (Màu sắc MoMo)
        Button btnHuy = new Button("Hủy");
        Button btnXacNhanThanhToan = new Button("Xác nhận đã thanh toán");
        
        btnHuy.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-padding: 10px 20px; -fx-font-weight: bold;");
        btnXacNhanThanhToan.setStyle("-fx-background-color: #b0006d; -fx-text-fill: white; -fx-padding: 10px 20px; -fx-font-weight: bold;"); 

        HBox buttonBox = new HBox(15, btnHuy, btnXacNhanThanhToan);
        buttonBox.setAlignment(Pos.CENTER);

        // Thay lblTitle bằng logoView
        VBox root = new VBox(20, logoView, qrView, lblAmount, lblContent, buttonBox);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);
        root.setPrefSize(400, 550);

        Stage popupStage = new Stage();
        popupStage.setTitle("Thanh toán MoMo/VietQR");
        popupStage.setScene(new Scene(root));
        
        // GÁN SỰ KIỆN CHO NÚT
        btnHuy.setOnAction(e -> popupStage.close());

     // ui.DatBan.java - trong hàm openMoMoQrPopup()

     // ...
        btnXacNhanThanhToan.setOnAction(e -> {
            // 1. Chuẩn bị dữ liệu (pttt, maNV)
            PTTThanhToan pttt = PTTThanhToan.VI_DIEN_TU; // Hoặc NGAN_HANG, MOMO
            String maHDCanThanhToan = currentHoaDon.getMaHD();
            String maNV = "NV001"; // Giả định đã có logic lấy mã NV từ MainApp
            // ... (logic lấy mã NV) ...
            
            if (maHDCanThanhToan != null) {
                // 2. GỌI DAO ĐỂ CẬP NHẬT TRẠNG THÁI HÓA ĐƠN VÀ GIẢI PHÓNG BÀN
                if (datBanDAO.thanhToanHoaDon(maHDCanThanhToan, pttt, maNV)) { 
                    
                    // 🔥 BƯỚC FIX LỖI QUAN TRỌNG: TẢI LẠI ĐỐI TƯỢNG HOÁ ĐƠN TỪ CSDL
                    HoaDon hdDaThanhToan = datBanDAO.getHoaDonByMaHD(maHDCanThanhToan);
                    
                    if (hdDaThanhToan != null) {
                        // Gán HĐ mới nhất (với trạng thái ĐÃ THANH TOÁN) vào currentHoaDon
                        currentHoaDon = hdDaThanhToan;
                        
                        showAlert(Alert.AlertType.INFORMATION, "Thành công", 
                                  "Hóa đơn " + maHDCanThanhToan + " đã được thanh toán bằng " + pttt.getDisplayName());
                        
                        // 3. Cập nhật giao diện
                        loadDsBan(); 
                        disableMiddleActionButtons();
                        disablePaymentButtons(); // 🔥 Kích hoạt nút In Hóa đơn
                        loadBookingCards(); 
                        
                        // Cần gọi lại handleThanhToan để cập nhật các Labels trên Receipt với Giờ Ra mới
                        handleThanhToan(); 

                    } else {
                         showAlert(Alert.AlertType.ERROR, "Lỗi", "Đã thanh toán, nhưng không tải lại được hóa đơn mới.");
                    }

                } else {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể cập nhật trạng thái hóa đơn.");
                }
            } else {
                showAlert(Alert.AlertType.WARNING, "Lỗi", "Không có hóa đơn đang chọn.");
            }
            
            popupStage.close();
        });
  

        popupStage.show();
    }
 // ui.DatBan.java - Thêm vào class DatBan

    /**
     * 🔥 HÀM SỬA: Chỉ vô hiệu hóa các nút Phương thức Thanh toán (trên Receipt Panel).
     * Giữ nguyên các thông tin HĐ và kích hoạt nút In Hóa đơn.
     */
    private void disablePaymentButtons() {
        // 1. Vô hiệu hóa nút Thanh Toán (chính) và các nút PTTT (toggle buttons)
        if (btnThanhToan != null) btnThanhToan.setDisable(true);
        if (btnTienMat != null) btnTienMat.setDisable(true);
        if (btnNganHang != null) btnNganHang.setDisable(true);
        if (btnMoMo != null) btnMoMo.setDisable(true);
        
        // 2. Kích hoạt nút In Hóa đơn 🔥
        if (btnInHoaDon != null) {
            btnInHoaDon.setDisable(false); 
        }
    }

    /**
     * 🔥 HÀM MỚI: Vô hiệu hóa các nút thao tác nghiệp vụ (Panel Giữa).
     * Đảm bảo các nút Sửa/Đổi/Gộp/Tách/Hủy không dùng được sau khi thanh toán.
     */
    private void disableMiddleActionButtons() {
        // 🔥 GIẢ ĐỊNH CÁC BIẾN FXML NÀY ĐÃ ĐƯỢC KHAI BÁO
        // @FXML private Button btnSuaMon; 
        // @FXML private Button btnDoiBan; 
        // @FXML private Button btnTachBan; 
        // @FXML private Button btnGopBan; 
        // @FXML private Button btnHuyBan; 
        
        if (btnSuaMon != null) btnSuaMon.setDisable(true);
        if (btnDoiBan != null) btnDoiBan.setDisable(true);
        if (btnTachBan != null) btnTachBan.setDisable(true);
        if (btnGopBan != null) btnGopBan.setDisable(true);
        if (btnHuyBan != null) btnHuyBan.setDisable(true);
    }

    // 🔥 HÀM loadDsBan() TẠI VỊ TRÍ CŨ (giữ nguyên nếu bạn có)

    public void loadDsBan() {
        // Gọi lại hàm vẽ lưới bàn của bạn
        loadTableGrids();
        System.out.println("LOG UI: Đã tải lại danh sách Bàn (Cập nhật trạng thái bàn).");
    }
	/**
     * 🔥 HÀM SỬA CUỐI CÙNG: Mở Popup hiển thị QR Thanh toán Ngân hàng (Thêm màu cho nút Hủy/Xác nhận).
     * * SẼ CẦN KẾT NỐI INTERNET ĐỂ TẢI ẢNH QR.
     */
    private void openNganHangQrPopup() {
        if (currentHoaDon == null || currentHoaDon.getMaHD() == null) {
            showAlert(Alert.AlertType.WARNING, "Lỗi", "Không có mã hóa đơn hợp lệ để tạo QR.");
            return;
        }
        
        // 1. Chuẩn bị dữ liệu thanh toán
        calculateTotal(); 
        double tongTienThanhToan = 0;
        try {
            String amountStr = lblTongTienThanhToan.getText().replaceAll("[^0-9]", "");
            tongTienThanhToan = Double.parseDouble(amountStr.isEmpty() ? "0" : amountStr);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xác định tổng tiền thanh toán.");
            return;
        }
        
        if (tongTienThanhToan <= 0) {
            showAlert(Alert.AlertType.WARNING, "Lỗi", "Tổng tiền thanh toán phải lớn hơn 0.");
            return;
        }

        // 2. Thông tin tĩnh và động
        final String YOUR_BANK_CODE = "970422"; // MB Bank BIN
        final String YOUR_ACCOUNT_NUMBER = "0927432020905"; // Số tài khoản của bạn.
        String maHD = currentHoaDon.getMaHD();
        
        String rawContent = "TT" + maHD.toUpperCase().replace(" ", "_");
        String encodedContent;
        try {
            encodedContent = java.net.URLEncoder.encode(rawContent, "UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            encodedContent = rawContent; 
        }
        
        // 3. Tạo URL Quicklink
        String qrUrl = String.format(
            "https://img.vietqr.io/image/%s-%s-compact.png?amount=%d&addInfo=%s",
            YOUR_BANK_CODE, 
            YOUR_ACCOUNT_NUMBER, 
            (int) Math.ceil(tongTienThanhToan), 
            encodedContent
        );
        
        // 4. Tải ảnh QR từ Internet
        Image qrImage = generateQrCodeImageFromUrl(qrUrl, 250); 

        if (qrImage == null || qrImage.isError()) {
            showAlert(Alert.AlertType.ERROR, "Lỗi kết nối", "Không thể tải mã QR từ Quicklink VietQR. Vui lòng kiểm tra lại kết nối.");
            return; 
        }

        // 5. Tạo giao diện Popup
        ImageView qrView = new ImageView(qrImage);
        qrView.setFitWidth(250);
        qrView.setFitHeight(250);
        
        Label lblTitle = new Label("Quét Mã Thanh Toán VietQR");
        lblTitle.setStyle("-fx-font-size: 1.5em; -fx-font-weight: bold;");
        
        Label lblAmount = new Label("Số tiền: " + String.format("%,.0f Đ", tongTienThanhToan));
        lblAmount.setStyle("-fx-font-size: 1.2em; -fx-font-weight: 500; -fx-text-fill: red;");
        
        Label lblContent = new Label("Nội dung: " + rawContent);
        lblContent.setStyle("-fx-font-size: 1.0em; -fx-font-weight: 400;");

        // 🔥 TẠO NÚT HỦY VÀ XÁC NHẬN VÀ ÁP DỤNG MÀU SẮC
        Button btnHuy = new Button("Hủy");
        Button btnXacNhanThanhToan = new Button("Xác nhận đã thanh toán");
        
        // Style cho nút Hủy (Màu xám/đỏ)
        btnHuy.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-padding: 10px 20px; -fx-font-weight: bold;");
        btnHuy.getStyleClass().add("action-button-cancel"); 
        
        // Style cho nút Xác nhận (Màu xanh lá cây/cam)
        btnXacNhanThanhToan.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-padding: 10px 20px; -fx-font-weight: bold;");
        btnXacNhanThanhToan.getStyleClass().add("confirm-button"); 

        HBox buttonBox = new HBox(15, btnHuy, btnXacNhanThanhToan);
        buttonBox.setAlignment(Pos.CENTER);

        VBox root = new VBox(20, lblTitle, qrView, lblAmount, lblContent, buttonBox);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);
        root.setPrefSize(400, 550);

        Stage popupStage = new Stage();
        popupStage.setTitle("Thanh toán Ngân hàng");
        popupStage.setScene(new Scene(root));
        
        // 6. GÁN SỰ KIỆN CHO NÚT
        btnHuy.setOnAction(e -> popupStage.close());

        btnXacNhanThanhToan.setOnAction(e -> {
            // 1. Chuẩn bị dữ liệu (pttt, maNV)
            PTTThanhToan pttt = PTTThanhToan.NGAN_HANG; // Hoặc NGAN_HANG, MOMO
            String maHDCanThanhToan = currentHoaDon.getMaHD();
            String maNV = "NV001"; // Giả định đã có logic lấy mã NV từ MainApp
            // ... (logic lấy mã NV) ...
            
            if (maHDCanThanhToan != null) {
                // 2. GỌI DAO ĐỂ CẬP NHẬT TRẠNG THÁI HÓA ĐƠN VÀ GIẢI PHÓNG BÀN
                if (datBanDAO.thanhToanHoaDon(maHDCanThanhToan, pttt, maNV)) { 
                    
                    // 🔥 BƯỚC FIX LỖI QUAN TRỌNG: TẢI LẠI ĐỐI TƯỢNG HOÁ ĐƠN TỪ CSDL
                    HoaDon hdDaThanhToan = datBanDAO.getHoaDonByMaHD(maHDCanThanhToan);
                    
                    if (hdDaThanhToan != null) {
                        // Gán HĐ mới nhất (với trạng thái ĐÃ THANH TOÁN) vào currentHoaDon
                        currentHoaDon = hdDaThanhToan;
                        
                        showAlert(Alert.AlertType.INFORMATION, "Thành công", 
                                  "Hóa đơn " + maHDCanThanhToan + " đã được thanh toán bằng " + pttt.getDisplayName());
                        
                        // 3. Cập nhật giao diện
                        loadDsBan(); 
                        disableMiddleActionButtons();
                        disablePaymentButtons(); // 🔥 Kích hoạt nút In Hóa đơn
                        loadBookingCards(); 
                        
                        // Cần gọi lại handleThanhToan để cập nhật các Labels trên Receipt với Giờ Ra mới
                        handleThanhToan(); 

                    } else {
                         showAlert(Alert.AlertType.ERROR, "Lỗi", "Đã thanh toán, nhưng không tải lại được hóa đơn mới.");
                    }

                } else {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể cập nhật trạng thái hóa đơn.");
                }
            } else {
                showAlert(Alert.AlertType.WARNING, "Lỗi", "Không có hóa đơn đang chọn.");
            }
            
            popupStage.close();
        });

        popupStage.show();
    }
 // ui.DatBan.java

 // ui.DatBan.java

 // ui.DatBan.java

   

	/**
     * 🔥 HÀM SỬA CUỐI CÙNG: Tạo URL Quicklink của MoMo (ĐÃ FIX LỖI BIÊN DỊCH ENCODE).
     * Sử dụng cú pháp rút gọn cho MoMo để đảm bảo tính ổn định.
     */
    private String createMomoQrUrl(String maHD, double amount) {
        // 🔥 THÔNG TIN TĨNH CỦA BẠN (CẦN ĐIỀN CHÍNH XÁC)
        final String YOUR_MOMO_PHONE = "09xxyyyzzzz"; // <<< THAY THẾ BẰNG SĐT MOMO CỦA BẠN
        
        // Nội dung chuyển khoản: Tối đa khoảng 30 ký tự, không dấu.
        String rawContent = "TT" + maHD; 
        
        // CÚ PHÁP MOMO THANH TOÁN (PAYMENT LINK)
        String momoPayLink = String.format(
            "https://payment.momo.vn/pay?phone=%s&amount=%d&note=%s",
            YOUR_MOMO_PHONE, 
            (int) Math.ceil(amount), 
            rawContent // Nội dung chưa mã hóa URL
        );
        
        String encodedMomoPayLink = momoPayLink;
        String encodedUrl;

        try {
            // Mã hóa URL của link MoMo (bắt buộc phải có try-catch)
            encodedMomoPayLink = java.net.URLEncoder.encode(momoPayLink, "UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            System.err.println("Lỗi mã hóa URL MoMo: " + e.getMessage());
        }
        
        // SỬ DỤNG DỊCH VỤ BÊN THỨ 3 (GoQR) ĐỂ CHUYỂN LINK NÀY THÀNH ẢNH QR CODE
        try {
            // Mã hóa toàn bộ chuỗi data (link MoMo đã mã hóa) trước khi gửi đến GoQR
            encodedUrl = "https://api.qrserver.com/v1/create-qr-code/?data=" + 
                         java.net.URLEncoder.encode(encodedMomoPayLink, "UTF-8") + 
                         "&size=250x250";
        } catch (java.io.UnsupportedEncodingException e) {
            // Fallback nếu có lỗi mã hóa lần cuối
            encodedUrl = "Lỗi mã hóa";
        }
        
        return encodedUrl;
    }
 // ui.DatBan.java

    /**
     * 🔥 HÀM MỚI: Tải ảnh QR Code từ một URL.
     * KHÔNG CẦN THƯ VIỆN ZXING NỮA.
     *
     * @param url Chuỗi URL của API tạo QR Code.
     * @param size Kích thước (chỉ dùng để đặt kích thước Image, không ảnh hưởng đến tải ảnh).
     * @return Ảnh JavaFX Image.
     */
    private Image generateQrCodeImageFromUrl(String url, int size) {
        try {
            // Tải ảnh trực tiếp từ URL
            Image image = new Image(url, size, size, true, true);
            
            // Kiểm tra lỗi tải ảnh (ví dụ: URL sai, mất mạng)
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
     * Chuyển chuỗi dữ liệu (qrData) thành ảnh QR Code bằng thư viện ZXing.
     *
     * @param qrData Chuỗi dữ liệu (Ví dụ: Chuỗi EMV/NAPAS)
     * @param size Kích thước ảnh (ví dụ: 250)
     * @return Ảnh JavaFX Image.
     */
    private Image generateQrCodeImage(String qrData, int size) {
        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H); // Mức sửa lỗi cao

            // 1. Mã hóa chuỗi dữ liệu thành BitMatrix
            BitMatrix bitMatrix = new MultiFormatWriter().encode(
                qrData,
                BarcodeFormat.QR_CODE,
                size,
                size,
                hints
            );

            // 2. Chuyển BitMatrix thành byte array (PNG format)
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            // Dùng lớp MatrixToImageWriter của zxing-javase
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", os);
            
            // 3. Tạo JavaFX Image từ byte array
            return new Image(new ByteArrayInputStream(os.toByteArray()));

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi tạo QR", "Không thể tạo mã QR. Vui lòng kiểm tra thư viện tạo QR (ZXing) và dữ liệu QR Code.");
            System.err.println("Lỗi tạo mã QR bằng ZXing: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

	/**
     * 🔥 HÀM CUỐI CÙNG: Mở Popup Thanh toán Tiền mặt (FIX KÍCH THƯỚC NÚT BẰNG MAX_WIDTH TRỰC TIẾP).
     */
    private void moPopupThanhToanTienMat() {
        if (currentHoaDon == null) {
            showAlert(Alert.AlertType.WARNING, "Lỗi", "Vui lòng chọn Hóa đơn trước khi thanh toán.");
            return;
        }
        
        // 1. Chuẩn bị dữ liệu tổng tiền
        calculateTotal(); 
        double tongTienThanhToan = 0;
        try {
            String amountStr = lblTongTienThanhToan.getText().replaceAll("[^0-9]", "");
            tongTienThanhToan = Double.parseDouble(amountStr.isEmpty() ? "0" : amountStr);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xác định tổng tiền thanh toán.");
            return;
        }

        final double finalTotal = tongTienThanhToan;
        final DecimalFormat currencyFormatter = new DecimalFormat("###,###");
        
        // 2. Thiết lập các Controls
        
        FlowPane flowButtonContainer = new FlowPane(10, 10);
        double[] presetValues = {100000, 200000, 500000, 1000000, 1500000, 2000000, 2500000, 3000000, 5000000};
        ToggleGroup presetGroup = new ToggleGroup();
        flowButtonContainer.setPrefWrapLength(420);

        // TextFields
        TextField txtTienKhachDua = new TextField("0"); 
        TextField txtTienTraLai = new TextField("0"); 
        
        Button btnXacNhan = new Button("Xác nhận");
        
        // 🔥 FIX KÍCH THƯỚC NÚT: Buộc nút chiếm tối đa chiều rộng có thể
        btnXacNhan.setMaxWidth(Double.MAX_VALUE); 
        
        // INLINE STYLE cho nút Xác nhận
        btnXacNhan.setStyle("-fx-background-color: #ff9900; -fx-text-fill: white; -fx-font-size: 1.2em; -fx-font-weight: bold; -fx-padding: 12px 0; -fx-background-radius: 5px; -fx-border-radius: 5px;");
        
        txtTienTraLai.setEditable(false);
        txtTienKhachDua.setAlignment(Pos.CENTER_RIGHT);
        txtTienTraLai.setAlignment(Pos.CENTER_RIGHT);


        // 3. Logic Tính toán Tiền trả lại (Giữ nguyên)
        
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
        
        // Xử lý nút preset (Giữ nguyên logic)
        for (double value : presetValues) {
            ToggleButton btn = new ToggleButton(currencyFormatter.format(value) + " VNĐ");
            btn.setUserData(value);
            btn.setToggleGroup(presetGroup);
            // Style cơ bản cho nút preset
            btn.setStyle("-fx-background-color: #ffffff; -fx-text-fill: #0d6efd; -fx-font-weight: 500; -fx-font-size: 0.95em; -fx-padding: 10px 15px; -fx-border-radius: 5px; -fx-background-radius: 5px; -fx-border-color: #0d6efd; -fx-border-width: 1px;");
            btn.setPrefWidth(125); 
            btn.setPrefHeight(40);

            btn.setOnAction(e -> {
                txtTienKhachDua.setText(currencyFormatter.format(value));
                capNhatTienThoi.run();
            });
            flowButtonContainer.getChildren().add(btn);
        }

        // Xử lý nhập tay (Giữ nguyên logic)
        txtTienKhachDua.textProperty().addListener((obs, oldVal, newVal) -> {
             String filtered = newVal.replaceAll("[^0-9]", "");
             
             try {
                 if (!filtered.isEmpty()) {
                     double value = Double.parseDouble(filtered);
                     txtTienKhachDua.setText(currencyFormatter.format(value));
                     Platform.runLater(txtTienKhachDua::end); 
                 } else {
                     txtTienKhachDua.setText("");
                 }
             } catch (NumberFormatException ignored) {}

             capNhatTienThoi.run();
        });
        
        // 4. Thiết lập Layout và Stage
        
        Label lblTitle = new Label("Tiền mặt");
        lblTitle.setStyle("-fx-font-size: 1.5em; -fx-font-weight: bold;");

        VBox inputContainer = new VBox(10);
        inputContainer.getChildren().addAll(
            createInputGridRow("Tiền khách đưa:", txtTienKhachDua, true),
            createInputGridRow("Tiền trả lại khách:", txtTienTraLai, false)
        );
        
        
        GridPane root = new GridPane();
        root.setVgap(20);
        root.setPadding(new Insets(20));
        root.setPrefWidth(450); 
        root.setPrefHeight(480); 
        root.setStyle("-fx-background-color: white;"); 

        ColumnConstraints column = new ColumnConstraints();
        column.setPercentWidth(100); 
        root.getColumnConstraints().add(column);
        
        root.add(lblTitle, 0, 0); 
        root.add(flowButtonContainer, 0, 1);
        root.add(inputContainer, 0, 2); 
        root.add(btnXacNhan, 0, 3); 

        GridPane.setHalignment(lblTitle, HPos.CENTER);
        GridPane.setHalignment(flowButtonContainer, HPos.CENTER);
        
        GridPane.setHalignment(btnXacNhan, HPos.CENTER);
        
        // 🔥 QUAN TRỌNG: FillWidth buộc nút giãn nở 100%
        GridPane.setFillWidth(btnXacNhan, true); 
        GridPane.setFillWidth(inputContainer, true);
        
        // 5. Thiết lập Stage (Cửa sổ Popup)
        Stage popupStage = new Stage();
        popupStage.setTitle("Thanh toán tiền mặt");
        
        Scene scene = new Scene(root);
        popupStage.setScene(scene);

        // 6. Xử lý nút Xác nhận
        btnXacNhan.setOnAction(e -> {
            // 1. Chuẩn bị dữ liệu (pttt, maNV)
            PTTThanhToan pttt = PTTThanhToan.TIEN_MAT; // Hoặc NGAN_HANG, MOMO
            String maHDCanThanhToan = currentHoaDon.getMaHD();
            String maNV = "NV001"; // Giả định đã có logic lấy mã NV từ MainApp
            // ... (logic lấy mã NV) ...
            
            if (maHDCanThanhToan != null) {
                // 2. GỌI DAO ĐỂ CẬP NHẬT TRẠNG THÁI HÓA ĐƠN VÀ GIẢI PHÓNG BÀN
                if (datBanDAO.thanhToanHoaDon(maHDCanThanhToan, pttt, maNV)) { 
                    
                    // 🔥 BƯỚC FIX LỖI QUAN TRỌNG: TẢI LẠI ĐỐI TƯỢNG HOÁ ĐƠN TỪ CSDL
                    HoaDon hdDaThanhToan = datBanDAO.getHoaDonByMaHD(maHDCanThanhToan);
                    
                    if (hdDaThanhToan != null) {
                        // Gán HĐ mới nhất (với trạng thái ĐÃ THANH TOÁN) vào currentHoaDon
                        currentHoaDon = hdDaThanhToan;
                        
                        showAlert(Alert.AlertType.INFORMATION, "Thành công", 
                                  "Hóa đơn " + maHDCanThanhToan + " đã được thanh toán bằng " + pttt.getDisplayName());
                        
                        // 3. Cập nhật giao diện
                        loadDsBan(); 
                        disableMiddleActionButtons();
                        disablePaymentButtons(); // 🔥 Kích hoạt nút In Hóa đơn
                        loadBookingCards(); 
                        
                        // Cần gọi lại handleThanhToan để cập nhật các Labels trên Receipt với Giờ Ra mới
                        handleThanhToan(); 

                    } else {
                         showAlert(Alert.AlertType.ERROR, "Lỗi", "Đã thanh toán, nhưng không tải lại được hóa đơn mới.");
                    }

                } else {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể cập nhật trạng thái hóa đơn.");
                }
            } else {
                showAlert(Alert.AlertType.WARNING, "Lỗi", "Không có hóa đơn đang chọn.");
            }
            
            popupStage.close();
        });
        popupStage.show();
    }

    /**
     * Hàm helper createInputGridRow (Đã sửa ở bước trước)
     * Vui lòng đảm bảo bạn có hàm này trong class DatBan.java
     */
    private GridPane createInputGridRow(String labelText, TextField textField, boolean showInputLabel) {
        GridPane grid = new GridPane();
        grid.setHgap(10); 
        grid.setVgap(5);  
        grid.setAlignment(Pos.CENTER_LEFT); 

        // Cột 1: Label
        Label label = new Label(labelText);
        label.setStyle("-fx-font-size: 1.05em; -fx-text-fill: #333333; -fx-font-weight: 500;");
        grid.add(label, 0, 1); 

        // Cột 2: TextField
        textField.setEditable(showInputLabel);
        textField.setPrefWidth(150); 
        textField.setStyle("-fx-border-color: #ced4da; -fx-border-width: 1px; -fx-border-radius: 5px; -fx-padding: 8px 10px;");
        grid.add(textField, 1, 1); 

        // === XỬ LÝ LABEL "INPUT" ===
        if (showInputLabel) {
            Label inputLabel = new Label("Input"); 
            inputLabel.setStyle("-fx-font-size: 0.75em; -fx-text-fill: #999999;");
            GridPane.setHalignment(inputLabel, HPos.LEFT);
            grid.add(inputLabel, 1, 0); 
        }

        // Đặt ràng buộc chiều rộng cho các cột
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setHgrow(Priority.NEVER); 
        col1.setPrefWidth(120); 

        ColumnConstraints col2 = new ColumnConstraints();
        col2.setHgrow(Priority.ALWAYS); 
        col2.setMinWidth(150); 

        grid.getColumnConstraints().addAll(col1, col2);

        return grid;
    }


 
   
	/**
     * 🔥 HÀM MỚI: Xử lý khi người dùng chọn Khuyến mãi
     */
    private void handlePromoSelection(String selectedName) {
        if (selectedName == null || selectedName.equals("Không áp dụng")) {
            selectedUuDai = null;
        } else {
            // Trích xuất tên KM từ chuỗi hiển thị
            String simpleName = selectedName.substring(0, selectedName.indexOf(" (Giảm")).trim();
            
            // Tìm đối tượng UuDai tương ứng
            selectedUuDai = dsUuDaiDangApDung.stream()
                .filter(ud -> ud.getTenUuDai().equals(simpleName))
                .findFirst()
                .orElse(null);
        }
        calculateTotal(); // Luôn tính lại tổng tiền khi khuyến mãi thay đổi
    }

	/**
     * 🔥 HÀM MỚI: Tải danh sách Khuyến mãi 'Đang áp dụng' vào ComboBox
     */
    private void loadPromoComboBox() {
        // Lấy tất cả ưu đãi và lọc những ưu đãi 'Đang áp dụng'
        List<UuDai> allUuDai = uuDaiDAO.getAllUuDai();
        
        // Lọc các ưu đãi đang áp dụng (hoặc Sắp diễn ra)
        dsUuDaiDangApDung = allUuDai.stream()
            .filter(ud -> ud.getTrangThai().equals("Đang áp dụng"))
            .collect(Collectors.toList());
            
        ObservableList<String> promoNames = FXCollections.observableArrayList();
        promoNames.add("Không áp dụng"); // Thêm lựa chọn mặc định
        
        for (UuDai ud : dsUuDaiDangApDung) {
            promoNames.add(String.format("%s (Giảm %.0f%%)", ud.getTenUuDai(), ud.getGiaTri()));
        }
        
        if (promoComboBox != null) {
            promoComboBox.setItems(promoNames);
            promoComboBox.getSelectionModel().selectFirst();
        }
    }

	/**
     * 🔥 HÀM MỚI: Mở Popup Gộp Bàn (GopBan_Popup.fxml).
     * @requires currentHoaDon != null (Phải chọn HĐ Gốc)
     */
    private void openGopBanPopup() {
        // 1. Kiểm tra HĐ gốc đã được chọn chưa
        if (currentHoaDon == null || currentHoaDon.getMaHDGoc() != null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn một Hóa đơn Gốc đang hoạt động để gộp.");
            return;
        }

        try {
            // 2. Tải FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/GopBan_Popup.fxml"));
            VBox root = loader.load();
            GopBanPopupController popupController = loader.getController();

            // 3. Truyền dữ liệu: HĐ gốc (Master) và reference đến controller cha
            popupController.setInitialData(currentHoaDon, datBanDAO, this); 

            // 4. Cấu hình Stage
            Stage popupStage = new Stage();
            popupStage.setTitle("Gộp Bàn Cho HĐ: " + currentHoaDon.getMaHD());
            Scene scene = new Scene(root);
            popupStage.setScene(scene);
            popupStage.showAndWait(); 

            // 5. Sau khi Popup đóng, refresh giao diện chính
            loadBookingCards();
            loadTableGrids();
            clearFormDatBan();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi UI", "Không thể tải giao diện Gộp Bàn Popup.\nKiểm tra file FXML và Controller đã đúng chưa.");
        } catch (Exception e) {
             e.printStackTrace();
             showAlert(Alert.AlertType.ERROR, "Lỗi không xác định", "Đã xảy ra lỗi khi mở Popup Gộp Bàn: " + e.getMessage());
        }
    }

	/**
     * 🔥 HÀM MỚI: Mở Popup Tách Bàn (TachBanPopup.fxml).
     * * Đảm bảo:
     * 1. Hóa đơn gốc (currentHoaDon) đã được chọn.
     * 2. Hóa đơn phải đang ở trạng thái 'Đang sử dụng' (DANG_SU_DUNG) hoặc 'HoaDonTam'.
     */
    private void openTachBanPopup() {
        // 1. Kiểm tra Hóa đơn gốc đã được chọn chưa
        if (currentHoaDon == null || currentHoaDon.getMaHDGoc() != null) {
            showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn một Hóa đơn Gốc đang hoạt động để tách.");
            return;
        }
        
        // 2. Kiểm tra trạng thái Hóa đơn
        String trangThaiHd = currentHoaDon.getTrangThai().getDbValue();
        if (!(trangThaiHd.equals(TrangThaiHoaDon.DANG_SU_DUNG.getDbValue()) || trangThaiHd.equals(TrangThaiHoaDon.HOA_DON_TAM.getDbValue()))) {
             showAlert(Alert.AlertType.WARNING, "Không thể tách", "Chỉ có thể tách Hóa đơn đang phục vụ/tạm (trạng thái hiện tại: " + currentHoaDon.getTrangThai().getDisplayName() + ").");
             return;
        }

        try {
            // Tải FXML và Controller
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/TachBan_Popup.fxml"));
            VBox root = loader.load();
            TachBanPopupController popupController = loader.getController(); // << Tên Controller chính xác

            // Truyền dữ liệu: Sử dụng currentHoaDon (Hóa đơn gốc)
            popupController.setInitialData(currentHoaDon, datBanDAO, this); 

            // Cấu hình Stage (cửa sổ popup)
            Stage popupStage = new Stage();
            popupStage.setTitle("Tách Bàn Cho Hóa Đơn: " + currentHoaDon.getMaHD());
            Scene scene = new Scene(root);
            
            // (Optional) Thêm CSS cho Popup
            URL cssUrl = getClass().getResource("/css/TachBanPopup.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }
            
            popupStage.setScene(scene);
            popupStage.showAndWait(); 

            // 3. Sau khi Popup đóng, refresh giao diện chính
            loadBookingCards();
            loadTableGrids();
            clearFormDatBan(); // Dọn dẹp form sau thao tác

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi Tải Giao Diện", "Không thể tải giao diện Tách Bàn.\nKiểm tra file FXML và Controller đã đúng chưa.");
        } catch (Exception e) {
             e.printStackTrace();
             showAlert(Alert.AlertType.ERROR, "Lỗi không xác định", "Đã xảy ra lỗi khi mở Popup Tách Bàn: " + e.getMessage());
        }
    }
 // ui.DatBan.java

    
	/**
     * 🔥 HÀM MỚI: Mở Popup để thực hiện đổi bàn.
     */
    private void openDoiBanPopup() {
        // 1. Kiểm tra xem có hóa đơn nào đang được chọn không
        if (currentHoaDon == null || currentHoaDonGocVaPhu.isEmpty()) { //
            showAlert(Alert.AlertType.WARNING, "Chưa chọn Hóa đơn", "Vui lòng chọn một Hóa đơn Gốc từ danh sách bên trái để thực hiện đổi bàn.");
            return;
        }

        try {
            // 2. Tải FXML của Popup
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DoiBan_Popup.fxml")); // <<< Tên file FXML mới
            VBox root = loader.load();

            // 3. Lấy Controller của Popup và truyền dữ liệu
            DoiBanPopupController popupController = loader.getController(); // <<< Controller mới
            popupController.setInitialData(currentHoaDonGocVaPhu, datBanDAO, this); // Truyền list HĐ, DAO và controller chính

            // 4. Tạo và hiển thị cửa sổ Popup
            Stage popupStage = new Stage();
            popupStage.setTitle("Đổi Bàn cho HĐ: " + currentHoaDon.getMaHD()); //
            Scene scene = new Scene(root);

            // (Optional) Thêm CSS cho Popup nếu muốn
            URL cssUrl = getClass().getResource("/css/DoiBanPopup.css"); // <<< File CSS mới (tùy chọn)
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            popupStage.setScene(scene);
            popupStage.showAndWait(); // Hiển thị và chờ Popup đóng lại

            // 5. Sau khi Popup đóng, refresh lại màn hình chính (nếu cần)
            // Controller Popup sẽ gọi hàm refresh của DatBan nếu đổi bàn thành công.

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi UI", "Không thể mở giao diện Đổi Bàn Popup: " + e.getMessage());
        } catch (Exception e) {
             e.printStackTrace();
             showAlert(Alert.AlertType.ERROR, "Lỗi không xác định", "Đã xảy ra lỗi khi mở Popup Đổi Bàn: " + e.getMessage());
        }
    }

	/**
     * 🔥 HÀM XỬ LÝ NGHIỆP VỤ TÁCH BÀN
     * Tạo một hóa đơn mới (với trạng thái HOADONTAM) dựa trên hóa đơn cũ, 
     * và gán cho một bàn TẠM THỜI (NULL).
     * @param maHDCu Mã hóa đơn gốc cần tách.
     * @return Mã hóa đơn mới được tạo.
     * @throws Exception nếu có lỗi CSDL hoặc lỗi nghiệp vụ.
     */
    public String tachBan(String maHDCu) throws Exception {
        
        // 1. Lấy thông tin hóa đơn cũ
        HoaDon hdCu = datBanDAO.getHoaDonByMaHD(maHDCu); 
        if (hdCu == null) {
            throw new Exception("Không tìm thấy Hóa đơn gốc.");
        }
        
        // 2. TẠO HÓA ĐƠN MỚI (Copy thông tin)
        HoaDon hdMoi = new HoaDon();
        hdMoi.setNgayLap(java.time.LocalDateTime.now());
        hdMoi.setGioVao(hdCu.getGioVao()); 
        hdMoi.setKhachHang(hdCu.getKhachHang());
        hdMoi.setTienCoc(0.0); 
        hdMoi.setMaUuDai(null);
        hdMoi.setHinhThucTT(null); 
        hdMoi.setTrangThai("HoaDonTam"); // Trạng thái Tạm thời

        // 3. LƯU HÓA ĐƠN MỚI (chưa có món, chưa có bàn)
        datBanDAO.luuHoaDonVaChiTiet(hdMoi, FXCollections.observableArrayList());
        String maHDMoi = hdMoi.getMaHD(); 
        
        // 4. CẬP NHẬT GIAO DIỆN MÀN HÌNH CHÍNH
        Platform.runLater(() -> {
            loadBookingCards();
            loadTableGrids();
        });
        
        return maHDMoi;
    }

    // =========================================================
    // LOGIC ĐẶT BÀN VÀ XÁC NHẬN
    // =========================================================
    
    /**
     * 🔥 XỬ LÝ LƯU THÔNG TIN ĐẶT HÀNG / KHÁCH ĐẾN QUÁN
     * === ĐÃ SỬA LOGIC (Gốc - Phụ) ĐỂ XỬ LÝ NHIỀU BÀN (Dùng maHDGoc) ===
     * Chỉ tạo 1 Hóa Đơn GỐC (có món, có cọc) và N Hóa Đơn PHỤ (không món, không cọc).
     */
    private void handleLuuDatHang() {
        if (selectedBanList.isEmpty()) { //
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng chọn ít nhất một bàn.");
            return;
        }
        
        if (!isBookingConfirmed) {
            showAlert(Alert.AlertType.WARNING, "Thiếu bước Xác nhận", "Vui lòng nhấn nút 'Xác nhận bàn' trước khi lưu đặt hàng.");
            return;
        }

        try {
            // 1. LẤY THÔNG TIN CHUNG
            String tenKH = txtTenKhachHang.getText();
            String sdt = txtSoDienThoai.getText();
            
            if (sdt.isEmpty()) {
                 showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng nhập Số điện thoại khách hàng.");
                 return;
            }
            
            KhachHang khachHang = datBanDAO.timHoacTaoKhachHang(sdt, tenKH);

            LocalDate ngayDen = datePickerThoiGianDen.getValue();
            LocalTime gioDen = LocalTime.parse(txtThoiGian.getText(), timeFormatter);
            java.time.LocalDateTime thoiGianDen = ngayDen.atTime(gioDen);
            
            String trangThaiBanDau;
            if (thoiGianDen.isBefore(java.time.LocalDateTime.now().plusMinutes(15))) {
                trangThaiBanDau = TrangThaiHoaDon.DANG_SU_DUNG.getDbValue(); //
            } else {
                trangThaiBanDau = TrangThaiHoaDon.DAT.getDbValue();
            }
            
            double tongTienCoc = 0;
            try {
                 String tienCocRaw = txtTienCoc.getText().replaceAll("[^0-9.]", ""); 
                 tongTienCoc = Double.parseDouble(tienCocRaw.isEmpty() ? "0" : tienCocRaw);
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Tiền cọc không hợp lệ.");
                return;
            }

            // 2. TÁCH BÀN GỐC VÀ BÀN PHỤ
            Ban banGoc = selectedBanList.get(0); //
            List<Ban> banPhuList = new ArrayList<>(selectedBanList.subList(1, selectedBanList.size())); //

            // 3. TẠO HÓA ĐƠN GỐC (Chứa tất cả món và tiền cọc)
            HoaDon hoaDonGoc = new HoaDon();
            hoaDonGoc.setNgayLap(java.time.LocalDateTime.now());
            hoaDonGoc.setGioVao(thoiGianDen);
            hoaDonGoc.setKhachHang(khachHang); 
            hoaDonGoc.setBan(banGoc); //
            hoaDonGoc.setTrangThai(trangThaiBanDau); //
            hoaDonGoc.setTienCoc(tongTienCoc); //
            hoaDonGoc.setMaHDGoc(null); // Gốc thì là NULL
            
            // Lưu HĐ Gốc VỚI danh sách món
            datBanDAO.luuHoaDonVaChiTiet(hoaDonGoc, monOrderList); //
            datBanDAO.capNhatTrangThaiBan(banGoc.getMaBan(), trangThaiBanDau); //
            
            System.out.println("LOG: Đã lưu HD GỐC " + hoaDonGoc.getMaHD() + " cho bàn " + banGoc.getMaBan());

            // 4. TẠO CÁC HÓA ĐƠN PHỤ (Không món, không cọc, trạng thái "HoaDonTam")
            for (Ban banPhu : banPhuList) {
                HoaDon hoaDonPhu = new HoaDon();
                hoaDonPhu.setNgayLap(java.time.LocalDateTime.now());
                hoaDonPhu.setGioVao(thoiGianDen);
                hoaDonPhu.setKhachHang(khachHang);
                hoaDonPhu.setBan(banPhu);
                hoaDonPhu.setTienCoc(0); // KHÔNG cọc
                hoaDonPhu.setMaHDGoc(hoaDonGoc.getMaHD()); // <<< LIÊN KẾT VỚI HĐ GỐC
                hoaDonPhu.setTrangThai(TrangThaiHoaDon.HOA_DON_TAM.getDbValue()); //

                // Lưu HĐ Phụ KHÔNG CÓ món
                datBanDAO.luuHoaDonVaChiTiet(hoaDonPhu, FXCollections.observableArrayList());
                datBanDAO.capNhatTrangThaiBan(banPhu.getMaBan(), trangThaiBanDau); //
                
                System.out.println("LOG: Đã lưu HD PHỤ " + hoaDonPhu.getMaHD() + " cho bàn " + banPhu.getMaBan());
            }

            // 5. HOÀN TẤT
            showAlert(Alert.AlertType.INFORMATION, "Thành công", 
                String.format("Đã lưu đặt hàng thành công!\n- HĐ Gốc: %s (Bàn %s)\n- HĐ Phụ: %d bàn",
                              hoaDonGoc.getMaHD(), banGoc.getMaBan(), banPhuList.size()));
            
            isBookingConfirmed = false;
            clearFormDatBan();
            loadBookingCards();
            loadTableGrids(); 
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi hệ thống", "Lỗi khi lưu đặt hàng: " + e.getMessage());
        }
    }

    /**
     * HÀM XỬ LÝ NÚT XÁC NHẬN (Cập nhật UI và tính tiền cọc)
     */
    private void handleXacNhanBan() {
        if (selectedBanList.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng chọn ít nhất một bàn để xác nhận.");
            return;
        }

        if (currentHoaDon == null) {
            for (Ban selectedBan : selectedBanList) {
                TrangThaiBan trangThaiHienThi = getTrangThaiHienThi(selectedBan, LocalTime.now());
                if (trangThaiHienThi == TrangThaiBan.DA_DAT || trangThaiHienThi == TrangThaiBan.DANG_SU_DUNG) {
                     showAlert(Alert.AlertType.WARNING, "Bàn đang bận", "Bàn " + selectedBan.getMaBan() + " đang bận hoặc đã được đặt trước (trong vòng 4 tiếng trước giờ vào). Vui lòng chọn bàn khác.");
                     return;
                }
            }
        }
        
        for (int i = 0; i < selectedBanList.size(); i++) {
            Button button = selectedButtonList.get(i);
            
            button.getStyleClass().remove("table-button-selected"); 
            
            applyTableStyle(button, TrangThaiBan.DA_DAT); 
        }
        
        double tienCoc = calculateTienCoc();
        txtTienCoc.setText(String.format("%.0f", tienCoc)); 

        isBookingConfirmed = true; 
        
        showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã xác nhận bàn. Tiền cọc đã được tính và áp dụng. Vui lòng nhập thông tin.");

        updateSelectionLabels();
        
        loadBookingCards();
    }
    
    // Hàm tính tiền cọc dựa trên chính sách
    private double calculateTienCoc() {
        if (selectedBanList.isEmpty()) {
            return 0.0;
        }

        double tongTienCoc = 0;
        final double COC_MAC_DINH = 150000.0;
        final double PHI_PHONG_RIENG = 100000.0;
        

        for (Ban ban : selectedBanList) {
            tongTienCoc += COC_MAC_DINH;
            
            if (ban.getLoaiBan() == LoaiBan.PHONG) {
                tongTienCoc += PHI_PHONG_RIENG;
            }
        }
        
        return tongTienCoc;
    }
    
    // =========================================================
    // LOGIC TÍNH TOÁN TRẠNG THÁI HIỂN THỊ MỚI
    // =========================================================

    /**
     * Tính toán trạng thái hiển thị của bàn dựa trên MỘT thời điểm kiểm tra.
     * Logic: Ưu tiên trạng thái ĐANG PHỤC VỤ (CAM), còn lại áp dụng logic ĐÃ ĐẶT (ĐỎ).
     * === ĐÃ SỬA: Loại bỏ chuyển trạng thái DAT sang DANG_SU_DUNG trong logic màu ===
     */
    public TrangThaiBan getTrangThaiHienThi(Ban banGoc, LocalTime thoiGianKiemTra) {
        // [Copy-paste toàn bộ nội dung của hàm getTrangThaiHienThi đã được sửa ở bước trước vào đây]
        // (Để giữ cho phản hồi này ngắn gọn, hãy sử dụng lại mã của hàm getTrangThaiHienThi đã gửi trước đó)
        
        // 1. Tìm HĐ đang hoạt động (Dat, DangSuDung, HoaDonTam) sớm nhất cho bàn này
        Optional<HoaDon> hdDangCho = dsHoaDonDatTrongNgay.stream() 
            .filter(hd -> hd.getBan() != null && hd.getBan().getMaBan().equals(banGoc.getMaBan()) && hd.getGioVao() != null)
            .filter(hd -> hd.getTrangThai() != null && 
                          (hd.getTrangThai().getDbValue().equals("Dat") || 
                           hd.getTrangThai().getDbValue().equals("DangSuDung") || 
                           hd.getTrangThai().getDbValue().equals("HoaDonTam")))
            .min(Comparator.comparing(hd -> hd.getGioVao())); 

        if (hdDangCho.isPresent()) {
            String trangThaiDb = hdDangCho.get().getTrangThai().getDbValue();
            
            // 2. Ưu tiên CAM (DANG_SU_DUNG / HOA_DON_TAM)
            if (trangThaiDb.equals("DangSuDung") || trangThaiDb.equals("HoaDonTam")) {
                return TrangThaiBan.DANG_SU_DUNG;
            }
            
            // 3. Xử lý ĐỎ (ĐÃ ĐẶT - DAT)
            if (trangThaiDb.equals("Dat")) {
                LocalTime gioVao = hdDangCho.get().getGioVao().toLocalTime(); 
                LocalTime gioCanhBaoSom = gioVao.minusHours(4); // Mốc 4 tiếng
                
                // Case 1: Thời gian tìm kiếm ĐÃ ĐẾN hoặc QUA giờ đặt
                if (thoiGianKiemTra.isAfter(gioVao) || thoiGianKiemTra.equals(gioVao)) {
                     return TrangThaiBan.DANG_SU_DUNG; // Chuyển ngay sang trạng thái Đang Sử Dụng (Cam)
                } 
                
                // Case 2: Thời gian tìm kiếm TRƯỚC giờ đặt (Kiểm tra 4 tiếng cứng)
                if (thoiGianKiemTra.isAfter(gioCanhBaoSom) || thoiGianKiemTra.equals(gioCanhBaoSom)) { 
                    // Nếu nằm trong vòng [4 tiếng trước, Giờ vào) -> Đỏ
                    return TrangThaiBan.DA_DAT; 
                }
            }
        }

        // 4. Fallback: Nếu không rơi vào các trường hợp bận -> Luôn là Trống (Trắng)
        return TrangThaiBan.TRONG; 
    }
    // =========================================================
    // LOGIC TẢI BẢNG VÀ CHỌN BÀN
    // =========================================================
    
    // Cần phải là public để Controller khác có thể gọi
    public void loadTableGrids() {
        System.out.println("\n*** LOAD SƠ ĐỒ BÀN BAN ĐẦU (DỰ TRÊN THỜI GIAN HIỆN TẠI) ***");
        
        // 1. Tải lại ds HĐ Đang Chờ (sử dụng ngày hiện tại)
        LocalDate dateToLoadForGrid = datePickerThoiGianDen.getValue() != null ? datePickerThoiGianDen.getValue() : LocalDate.now();
        this.dsHoaDonDatTrongNgay = datBanDAO.getDsDatBanHomNay(dateToLoadForGrid);
        System.out.println("LOG loadTableGrids: Đã tải lại " + dsHoaDonDatTrongNgay.size() + " HĐ đang chờ cho logic tô màu.");


        List<Ban> tatCaBan = datBanDAO.getAllBan();
        List<Ban> banHienThi = new ArrayList<>();
        LocalTime thoiGianHienTai = LocalTime.now();

        // 2. Tính toán trạng thái hiển thị
        for (Ban ban : tatCaBan) {
            Ban banMoi = new Ban(ban.getMaBan(), ban.getViTri(), ban.getSucChua(), ban.getLoaiBan(), ban.getTrangThai());
            
            TrangThaiBan trangThaiHienThi = getTrangThaiHienThi(banMoi, thoiGianHienTai);
            banMoi.setTrangThai(trangThaiHienThi);
            
            banHienThi.add(banMoi);
        }
        
        loadTableGridsBase(banHienThi);
    }

    private void loadTableGridsBase(List<Ban> dsBan) {
        try {
            // === THÊM MỚI: Clear Map trước khi tạo lại nút ===
            tableButtonMap.clear();
            // ===========================================

            List<Ban> tangTret = dsBan.stream().filter(b -> b.getLoaiBan() == LoaiBan.TANG_TRET).collect(Collectors.toList()); //
            // ... (code lấy tangMot, phongRieng như cũ) ...
            List<Ban> tangMot = dsBan.stream().filter(b -> b.getLoaiBan() == LoaiBan.TANG_1).collect(Collectors.toList());
            List<Ban> phongRieng = dsBan.stream().filter(b -> b.getLoaiBan() == LoaiBan.PHONG).collect(Collectors.toList());

            populateTableGrid(gridTangTret, tangTret); //
            populateTableGrid(gridTang1, tangMot); //
            populateTableGrid(gridPhongRieng, phongRieng); //

        } catch (Exception e) { //
            // ... (xử lý lỗi như cũ) ...
            showAlert(Alert.AlertType.ERROR, "Lỗi tải bàn", "Không thể tải danh sách bàn. Kiểm tra kết nối CSDL và DatBanDAO.");
            tableButtonMap.clear(); // Clear map nếu có lỗi
            populateTableGrid(gridTangTret, createMockTables(20, LoaiBan.TANG_TRET));
            populateTableGrid(gridTang1, createMockTables(10, LoaiBan.TANG_1));
            populateTableGrid(gridPhongRieng, createMockTables(5, LoaiBan.PHONG));
        }
    }

    private List<Ban> createMockTables(int count, LoaiBan loaiBan) {
        List<Ban> mockList = new ArrayList<>();
        TrangThaiBan[] statuses = TrangThaiBan.values();
        for (int i = 1; i <= count; i++) {
            String maBan = loaiBan.toString() + String.format("%02d", i); 
            TrangThaiBan status = statuses[i % statuses.length];
            mockList.add(new Ban(maBan, "Vị trí " + i, 4, loaiBan, status)); 
        }
        return mockList;
    }
    
    private void populateTableGrid(GridPane grid, List<Ban> dsBan) {
        // Không clear map ở đây, clear ở loadTableGridsBase
        grid.getChildren().clear();
        int col = 0;
        int row = 0;
        int maxCols = 5;

        for (Ban ban : dsBan) {
            Button btn = createTableButton(ban); //
            btn.setOnAction(e -> handleChonBan(ban, btn)); //

            // === THÊM MỚI: Lưu nút vào Map ===
            tableButtonMap.put(ban.getMaBan(), btn);
            // ================================

            grid.add(btn, col, row); //
            GridPane.setMargin(btn, new Insets(8));

            col++;
            if (col >= maxCols) { //
                col = 0;
                row++;
            }
        }
    }

    private Button createTableButton(Ban ban) {
        String soBan = ban.getMaBan().replaceAll("[^0-9]", "");
        
        Button btn = new Button(soBan);
        btn.getStyleClass().add("table-button");
        btn.setPrefSize(70, 70); 
        btn.setAlignment(Pos.CENTER); 

        TrangThaiBan trangThaiHienThi = ban.getTrangThai(); 

        applyTableStyle(btn, trangThaiHienThi);

        return btn;
    }
    
    private void applyTableStyle(Button button, TrangThaiBan trangThai) {
        button.getStyleClass().removeAll("table-button-booked", "table-button-serving", "table-button-available", "table-button-selected");
        
        if (trangThai == TrangThaiBan.DA_DAT) {
             button.getStyleClass().add("table-button-booked"); 
        } else if (trangThai == TrangThaiBan.DANG_SU_DUNG) {
             button.getStyleClass().add("table-button-serving"); 
        } else {
             button.getStyleClass().add("table-button-available"); 
        }
    }
    
    /**
     * Xử lý khi người dùng bấm chọn/bỏ chọn một bàn trên sơ đồ.
     * === ĐÃ SỬA: Logic Cảnh báo 8 tiếng khi chọn bàn ===
     */
    private void handleChonBan(Ban ban, Button currentButton) {

        // 1. Lấy thời gian kiểm tra từ UI
        LocalTime thoiGianKiemTra;
        LocalDate ngayKiemTra;
        try {
            ngayKiemTra = datePickerThoiGianDen.getValue();
            String gioStr = txtThoiGian.getText();
            if (ngayKiemTra == null || gioStr == null || gioStr.trim().isEmpty()) {
                thoiGianKiemTra = LocalTime.now();
            } else {
                 thoiGianKiemTra = LocalTime.parse(gioStr, timeFormatter);
            }
        } catch (Exception e) {
             showAlert(Alert.AlertType.ERROR, "Lỗi định dạng giờ", "Giờ nhập không hợp lệ (cần HH:mm). Vui lòng sửa lại trước khi chọn bàn.");
             return;
        }

        // 2. Kiểm tra Trạng thái BẬN CỨNG (4 tiếng - Màu Đỏ)
        TrangThaiBan trangThaiHienThi = getTrangThaiHienThi(ban, thoiGianKiemTra);

        if (trangThaiHienThi == TrangThaiBan.DA_DAT || trangThaiHienThi == TrangThaiBan.DANG_SU_DUNG) {
            showAlert(Alert.AlertType.INFORMATION, "Bàn bận",
                String.format("Bàn %s hiện không thể chọn vào lúc %s do đang bận (vùng 4 tiếng cứng).",
                              ban.getMaBan(), thoiGianKiemTra.format(timeFormatter)));
            return;
        }
        
        // 3. Xử lý Cảnh báo MỀM (4 - 8 tiếng)
         Optional<HoaDon> datGanNhat = dsHoaDonDatTrongNgay.stream()
            .filter(hd -> hd.getBan() != null && hd.getBan().getMaBan().equals(ban.getMaBan()) && hd.getGioVao() != null)
            .filter(hd -> hd.getTrangThai() != null && hd.getTrangThai().getDbValue().equals("Dat"))
            .min(Comparator.comparing(hd -> hd.getGioVao()));

        if (datGanNhat.isPresent()) {
            LocalTime gioVao = datGanNhat.get().getGioVao().toLocalTime();
            LocalTime gioCanhBaoMem = gioVao.minusHours(8); // Mốc 8 tiếng
            LocalTime gioCanhBaoCung = gioVao.minusHours(4); // Mốc 4 tiếng (đã kiểm tra ở bước 2)

            // Kiểm tra: nằm trong khoảng [8 tiếng trước, 4 tiếng trước)
            if (thoiGianKiemTra.isAfter(gioCanhBaoMem) && thoiGianKiemTra.isBefore(gioCanhBaoCung)) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Cảnh báo bàn sắp có khách");
                confirm.setHeaderText("Bàn " + ban.getMaBan() + " đã được đặt lúc " + gioVao.format(timeFormatter));
                confirm.setContentText("Bàn này có đơn đặt trước trong vòng 8 tiếng tới (vùng cảnh báo mềm). Bạn có chắc chắn muốn chọn không?");

                Optional<ButtonType> result = confirm.showAndWait();

                if (result.isPresent() && (result.get() == ButtonType.CANCEL || result.get() == ButtonType.CLOSE)) {
                    return; // Ngăn chọn nếu người dùng hủy
                }
            }
        }


        // 4. Xử lý Chọn/Bỏ chọn (Giữ nguyên)
         boolean alreadySelected = selectedBanList.contains(ban);

        if (alreadySelected) {
            isBookingConfirmed = false;
            txtTienCoc.setText("0");

            selectedBanList.remove(ban);
            selectedButtonList.remove(currentButton);

            currentButton.getStyleClass().remove("table-button-selected");
            // Trả lại màu TRẮNG/TRỐNG sau khi bỏ chọn (vì nó đã vượt qua bước 2)
            applyTableStyle(currentButton, TrangThaiBan.TRONG); 
        }
        else {
            isBookingConfirmed = false;
            txtTienCoc.setText("0");

            selectedBanList.add(ban);
            selectedButtonList.add(currentButton);

            currentButton.getStyleClass().removeAll("table-button-booked", "table-button-serving", "table-button-available");
            currentButton.getStyleClass().add("table-button-selected"); // Màu xanh lá cây (chọn)
        }

        updateSelectionLabels();
    }
    
    private void updateSelectionLabels() {
        if (selectedBanList.isEmpty()) {
            if (lblBanDangChon != null) lblBanDangChon.setText("Chưa chọn");
            if (lblTrangThaiBan != null) lblTrangThaiBan.setText("---");
            return;
        }

        String banNames = selectedBanList.stream()
                .map(Ban::getMaBan)
                .collect(Collectors.joining(", "));
        
        String statusText = selectedBanList.size() + " bàn đã chọn";

        if (lblBanDangChon != null) lblBanDangChon.setText(banNames);
        if (lblTrangThaiBan != null) lblTrangThaiBan.setText(statusText);
    }
    
    // Cần phải là public để Controller khác có thể gọi
 // Cần phải là public để Controller khác có thể gọi
 // Cần phải là public để Controller khác có thể gọi
    public void loadBookingCards() {
        vboxBookingCards.getChildren().clear();
        
        LocalDate dateToLoadForGrid = datePickerThoiGianDen.getValue() != null ? datePickerThoiGianDen.getValue() : LocalDate.now();
        
        try {
            // 1. Lấy TẤT CẢ đơn đang chờ (master list)
            List<HoaDon> allPendingBookings = datBanDAO.getDsHoaDonDangCho(); 

            // 2. Lấy giá trị filter và search từ UI
            String selectedStatus = comboFilter.getValue();
            String searchQuery = txtSearch.getText();

            // 3. ÁP DỤNG FILTER VÀ SEARCH
            List<HoaDon> filteredList = allPendingBookings.stream()
                // === LỌC BỎ HOÁ ĐƠN TẠM BẰNG CÁCH NÀY ===
                .filter(hd -> hd.getTrangThai() != null && !hd.getTrangThai().getDbValue().equals(TrangThaiHoaDon.HOA_DON_TAM.getDbValue()))
                // =======================================
                .filter(hd -> {
                    // Lọc theo Trạng thái
                    boolean statusMatch = true;
                    if (selectedStatus != null && !selectedStatus.equals("Tất cả")) {
                        String dbStatusValue = "";
                        
                        if (selectedStatus.equals("Đang phục vụ")) {
                            dbStatusValue = TrangThaiHoaDon.DANG_SU_DUNG.getDbValue();
                        } else if (selectedStatus.equals("Đã đặt")) {
                            dbStatusValue = TrangThaiHoaDon.DAT.getDbValue();
                        }
                        
                        // Thêm logic lọc nếu là "Đang phục vụ" thì bao gồm cả HoaDonTam
                        if (selectedStatus.equals("Đang phục vụ")) {
                            statusMatch = hd.getTrangThai() != null && 
                                          (hd.getTrangThai().getDbValue().equals(TrangThaiHoaDon.DANG_SU_DUNG.getDbValue()) || 
                                           hd.getTrangThai().getDbValue().equals(TrangThaiHoaDon.HOA_DON_TAM.getDbValue()));
                        } else {
                            statusMatch = hd.getTrangThai() != null && hd.getTrangThai().getDbValue().equals(dbStatusValue);
                        }
                    }
                    return statusMatch;
                })
                .filter(hd -> {
                    // Lọc theo SĐT
                    boolean searchMatch = true;
                    if (searchQuery != null && !searchQuery.trim().isEmpty()) {
                        // Phải kiểm tra null cho khachHang và soDT để tránh lỗi
                        searchMatch = hd.getKhachHang() != null && 
                                      hd.getKhachHang().getSoDT() != null && 
                                      hd.getKhachHang().getSoDT().contains(searchQuery.trim());
                    }
                    return searchMatch;
                })
                .collect(Collectors.toList()); // Thu thập kết quả đã lọc

            // 4. Tải các đơn trong ngày (CHO LOGIC SƠ ĐỒ BÀN)
            this.dsHoaDonDatTrongNgay = datBanDAO.getDsDatBanHomNay(dateToLoadForGrid); 
            
            // 5. Hiển thị danh sách ĐÃ LỌC
            System.out.println("\n*** LOG: Tải thành công " + filteredList.size() + " đơn ĐANG CHỜ (đã lọc) ***");

            if (filteredList.isEmpty()) {
                Label lbl = new Label("Không tìm thấy đơn nào khớp.");
                lbl.setPadding(new Insets(10));
                vboxBookingCards.getChildren().add(lbl);
            } else {
                // Chỉ hiển thị các HĐ GỐC trong danh sách filter (HĐ Phụ sẽ được hiển thị qua HĐ Gốc)
                List<HoaDon> hdGocFilter = filteredList.stream()
                                            .filter(hd -> hd.getMaHDGoc() == null)
                                            .collect(Collectors.toList());
                for (HoaDon hd : hdGocFilter) { // Dùng 'hdGocFilter'
                    VBox card = createBookingCard(hd);
                    card.setOnMouseClicked(e -> loadHoaDonToMainInterface(hd)); 
                    vboxBookingCards.getChildren().add(card);
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi tải danh sách đặt bàn: " + e.getMessage());
            Label lbl = new Label("LỖI TẢI DỮ LIỆU: " + e.getMessage());
            lbl.setPadding(new Insets(10));
            vboxBookingCards.getChildren().add(lbl);
        }
    }

 // (Bên dưới hàm handleSelectBookingCard)

    /**
     * 🔥 HÀM MỚI: Load thông tin của Hóa đơn lên giao diện chính
     * === ĐÃ NÂNG CẤP (Gốc - Phụ) + TÔ MÀU BÀN TRÊN SƠ ĐỒ ===
     * Sẽ tìm và hiển thị tất cả các bàn (Gốc và Phụ) liên quan đến HĐ này.
     * Sẽ tô màu đỏ các bàn tương ứng trên sơ đồ.
     */
    private void loadHoaDonToMainInterface(HoaDon hd) {
        System.out.println("LOG: Đang tải Hóa đơn " + (hd.getMaHD() != null ? hd.getMaHD() : "Mới") + " lên giao diện chính.");

        // === BƯỚC 1: RESET TẤT CẢ MÀU CỦA CỤM HÓA ĐƠN TRƯỚC ĐÓ ===
        if (!currentHoaDonGocVaPhu.isEmpty()) {
            System.out.println("LOG: Reset màu cho cụm HĐ cũ trước khi tải HĐ mới.");
            for (HoaDon hdCu : currentHoaDonGocVaPhu) {
                if (hdCu.getBan() != null) {
                    Button btn = tableButtonMap.get(hdCu.getBan().getMaBan());
                    if (btn != null) {
                        TrangThaiBan trangThaiThuc = getTrangThaiHienThi(hdCu.getBan(), LocalTime.now());
                        applyTableStyle(btn, trangThaiThuc);
                    }
                }
            }
        }
        // ==========================================================

        // 1. Set hóa đơn hiện tại (HĐ được click)
        this.currentHoaDon = hd; 

        // 2. KIỂM TRA GỐC/PHỤ
        if (hd.getMaHDGoc() != null) { 
             showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Bạn đã chọn Hóa đơn Phụ. Đang tải Hóa đơn Gốc liên quan...");
            HoaDon hdGoc = datBanDAO.getHoaDonByMaHD(hd.getMaHDGoc());
            if (hdGoc == null) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không tìm thấy HĐ Gốc của HĐ Phụ này!");
                clearFormDatBan();
                return;
            }
            this.currentHoaDon = hdGoc; // Chuyển sang làm việc với HĐ Gốc
        }

        // 3. TÌM TẤT CẢ HĐ PHỤ (Để tô màu bàn)
        currentHoaDonGocVaPhu.clear(); 
        currentHoaDonGocVaPhu.add(this.currentHoaDon); 
        List<HoaDon> hoaDonPhu = datBanDAO.getHoaDonPhuByMaHDGoc(this.currentHoaDon.getMaHD()); 
        currentHoaDonGocVaPhu.addAll(hoaDonPhu); 
        
        
        // -----------------------------------------------------------------
        // 🔥 BƯỚC KHẮC PHỤC LỖI: TẢI CHI TIẾT DANH SÁCH MÓN ĂN TỪ CSDL
        // -----------------------------------------------------------------
        monOrderList.clear(); // Xóa món ăn hiện tại (đang trống hoặc của HĐ cũ)
        if (this.currentHoaDon.getMaHD() != null) {
            // Gọi DAO để lấy danh sách chi tiết hóa đơn (MonOrder)
            System.out.println("LOG: Tải món ăn cho HD Gốc: " + this.currentHoaDon.getMaHD());
            ObservableList<MonOrder> chiTiet = datBanDAO.getChiTietHoaDon(this.currentHoaDon.getMaHD());
            monOrderList.addAll(chiTiet);
        }
        tblMonDaChon.refresh(); // Cập nhật lại giao diện danh sách món ăn
        calculateTotal();       // Tính toán lại tổng tiền hiển thị
        // -----------------------------------------------------------------

        // 5. Populate Middle Panel (Thông tin khách hàng, bàn, thời gian)
         if (currentHoaDon.getKhachHang() != null) {
            txtTenKhachHang.setText(currentHoaDon.getKhachHang().getTenKH());
            txtSoDienThoai.setText(currentHoaDon.getKhachHang().getSoDT());
        } else {
            txtTenKhachHang.clear();
            txtSoDienThoai.clear();
        }
        if (currentHoaDon.getGioVao() != null) {
            datePickerThoiGianDen.setValue(currentHoaDon.getGioVao().toLocalDate());
            txtThoiGian.setText(currentHoaDon.getGioVao().toLocalTime().format(timeFormatter));
        } else {
            datePickerThoiGianDen.setValue(LocalDate.now());
            txtThoiGian.setText(LocalTime.now().format(timeFormatter));
        }
        txtTienCoc.setText(String.format("%,.0f", currentHoaDon.getTienCoc()));
        
        // 6. Populate Label Bàn đã chọn (Hiển thị TẤT CẢ bàn)
        String tatCaBan = currentHoaDonGocVaPhu.stream() 
            .map(h -> {
                String tenBan = (h.getBan() != null) ? h.getBan().getMaBan() : "N/A";
                if (h.getMaHDGoc() == null) return tenBan + " (Gốc)"; 
                return tenBan;
            })
            .collect(Collectors.joining(", "));

        lblBanDangChon.setText(tatCaBan); 
        lblTrangThaiBan.setText(String.format("Đang xem %d bàn", currentHoaDonGocVaPhu.size())); 

        // Lấy sức chứa của bàn GỐC
        if (currentHoaDon.getBan() != null) { 
            txtSoLuongKhach.setText(String.valueOf(currentHoaDon.getBan().getSucChua()));
        } else {
            txtSoLuongKhach.clear();
        }

        txtYeuCau.clear(); //

        // 7. TÔ MÀU CÁC NÚT BÀN TRÊN SƠ ĐỒ
        TrangThaiHoaDon trangThaiHdGoc = TrangThaiHoaDon.fromDbValue(currentHoaDon.getTrangThai().getDbValue());
        TrangThaiBan trangThaiCanTo = trangThaiHdGoc == TrangThaiHoaDon.DANG_SU_DUNG
                                       ? TrangThaiBan.DANG_SU_DUNG // Màu cam nếu đang phục vụ
                                       : TrangThaiBan.DA_DAT;    // Màu đỏ nếu đã đặt

        for (HoaDon hoadon : currentHoaDonGocVaPhu) {
            if (hoadon.getBan() != null) {
                Button btn = tableButtonMap.get(hoadon.getBan().getMaBan()); 
                if (btn != null) {
                    applyTableStyle(btn, trangThaiCanTo); 
                }
            }
        }
        // ==============================================

        // 8. Cập nhật trạng thái và nút
        isBookingConfirmed = true; 
        updateButtonVisibility(true); 

        // 9. Tắt panel thanh toán nếu đang mở
        if (vboxReceipt != null) {
            vboxReceipt.setVisible(false);
        }
    }
    /**
     * Tạo một VBox card hiển thị thông tin tóm tắt của Hóa đơn.
     * === ĐÃ SỬA: Hiển thị danh sách bàn duy nhất, bỏ chữ "(Gốc)" ===
     */
    private VBox createBookingCard(HoaDon hd) {
        VBox card = new VBox(8); //
        card.getStyleClass().add("booking-card"); //
        card.setPadding(new Insets(15)); //

        String maGiaoDich = hd.getMaHD(); //
        String trangThaiDb = hd.getTrangThai() != null ? hd.getTrangThai().getDbValue() : "Unknown"; //
        String trangThaiViet = switch (trangThaiDb) { //
            case "Dat" -> "Đã đặt";
            case "DangSuDung" -> "Đang phục vụ";
            case "HoaDonTam" -> "Hóa đơn tạm";
            default -> trangThaiDb;
        };
        String gioVao = (hd.getGioVao() != null)
                           ? hd.getGioVao().toLocalTime().format(timeFormatter) : "N/A"; //
        String sdtKhach = (hd.getKhachHang() != null && hd.getKhachHang().getSoDT() != null)
                           ? hd.getKhachHang().getSoDT() : "N/A"; //


        // === LOGIC MỚI: LẤY DANH SÁCH BÀN DUY NHẤT ===
        String danhSachBanDayDu = "N/A";
        try {
            List<HoaDon> allRelatedHDs = new ArrayList<>(); //
            String maHDGocDeTim; //

            if (hd.getMaHDGoc() == null) { //
                maHDGocDeTim = hd.getMaHD(); //
                allRelatedHDs.add(hd); //
            } else {
                maHDGocDeTim = hd.getMaHDGoc(); //
                HoaDon hdGocTimDuoc = datBanDAO.getHoaDonByMaHD(maHDGocDeTim); //
                if (hdGocTimDuoc != null) { //
                    allRelatedHDs.add(hdGocTimDuoc); //
                }
                // (Bỏ log lỗi)
            }

            // Chỉ lấy HĐ Phụ đang ở trạng thái HoaDonTam
            List<HoaDon> hdPhuList = datBanDAO.getHoaDonPhuByMaHDGoc(maHDGocDeTim); //
            for(HoaDon hp : hdPhuList){ //
                if(!allRelatedHDs.stream().anyMatch(h -> h.getMaHD().equals(hp.getMaHD()))){ //
                    allRelatedHDs.add(hp); //
                }
            }

            // Sử dụng Set để lấy mã bàn duy nhất
            Set<String> uniqueBanSet = allRelatedHDs.stream()
                .filter(h -> h.getBan() != null) // Lọc bỏ HĐ không có bàn
                .map(h -> h.getBan().getMaBan()) // Lấy mã bàn
                .collect(Collectors.toSet()); // Thu thập vào Set (tự động loại trùng)

            // Tạo chuỗi hiển thị từ Set (có thể sắp xếp nếu muốn)
            danhSachBanDayDu = uniqueBanSet.isEmpty() ? "N/A" : String.join(", ", new TreeSet<>(uniqueBanSet)); // Sắp xếp A-Z

        } catch (Exception e) { //
             System.err.println("Lỗi khi lấy danh sách bàn đầy đủ cho card HD " + hd.getMaHD() + ": " + e.getMessage());
             e.printStackTrace();
             Ban banHienTai = hd.getBan();
             danhSachBanDayDu = (banHienTai != null ? banHienTai.getMaBan() : "Lỗi");
        }
        // ===========================================


        Label lblMaHD = new Label(maGiaoDich != null ? maGiaoDich : "Mã: N/A"); //
        lblMaHD.getStyleClass().add("booking-card-id"); //
        Label lblSDT = new Label("SĐT: " + sdtKhach); //
        Label lblTrangThai = new Label("Trạng thái: " + trangThaiViet); //
        lblTrangThai.getStyleClass().add("booking-status-" + trangThaiDb.toLowerCase()); //
        Label lblThoiGian = new Label("Thời gian đặt: " + gioVao); //

        // === SỬA LABEL BÀN ===
        Label lblBan = new Label("Bàn: " + danhSachBanDayDu); // <<< Sử dụng chuỗi mới
        // ====================

        Button btnXemChiTiet = new Button("Xem chi tiết"); //
        // ... (style và sự kiện của btnXemChiTiet giữ nguyên) ...
         btnXemChiTiet.setMaxWidth(Double.MAX_VALUE);
        btnXemChiTiet.getStyleClass().add("view-details-button");
        btnXemChiTiet.setPrefHeight(45);
        btnXemChiTiet.setOnAction(e -> {
            // 🔥 LOG VÀ GỌI HÀM: Mở Popup Chi Tiết Đặt Bàn
            System.out.println("LOG CLICK: Button Xem Chi Tiết clicked for HD: " + hd.getMaHD());
            openChiTietDatBanPopup(hd); // <<< GỌI HÀM MỞ POPUP MỚI
            e.consume(); //
        });

        card.getChildren().addAll(lblMaHD, lblSDT, lblTrangThai, lblThoiGian, lblBan, btnXemChiTiet); //

        return card; //
    }

 // ui.DatBan.java

    /**
     * 🔥 HÀM MỚI: Mở Popup Chi Tiết Đặt Bàn.
     */
    private void openChiTietDatBanPopup(HoaDon hd) {
        System.out.println("\nLOG OPEN POPUP: Attempting to open ChiTietDatBan Popup for HD: " + hd.getMaHD());
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ChiTietDatBan_Popup.fxml"));
            VBox root = loader.load();
            
            System.out.println("LOG OPEN POPUP: FXML loaded successfully.");
            
            ChiTietDatBanController controller = loader.getController();

            // Tạo Stage (Cửa sổ Popup)
            Stage popupStage = new Stage();
            popupStage.setTitle("Chi tiết đặt bàn " + (hd.getMaHD() != null ? hd.getMaHD() : "Mới"));
            
            // 🔥 TRUYỀN DỮ LIỆU ĐẾN CONTROLLER (Kích hoạt load data)
            controller.setHoaDonData(hd, datBanDAO); 
            
            System.out.println("LOG OPEN POPUP: Data transmitted to Controller.");

            // Cấu hình Stage
            Scene scene = new Scene(root);
            
            // Gán Controller cha (this) vào UserData để các nút trong Popup có thể gọi lại DatBan.java
            root.setUserData(this); 
            
            // Cấu hình CSS (giữ nguyên)
            // ... (Logic CSS) ...
            
            popupStage.setScene(scene);
            popupStage.show();

            System.out.println("LOG OPEN POPUP: Popup displayed successfully. Waiting for interaction...");

        } catch (IOException e) {
            System.err.println("❌ ERROR POPUP: IO/FXML Loading Failed. Check file path / ChiTietDatBan_Popup.fxml.");
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi UI", "Không thể tải giao diện chi tiết đặt bàn: " + e.getMessage());
        } catch (Exception e) {
             System.err.println("❌ ERROR POPUP: Unspecified error occurred during popup opening.");
             e.printStackTrace();
             showAlert(Alert.AlertType.ERROR, "Lỗi Hệ thống", "Đã xảy ra lỗi không xác định khi mở Popup: " + e.getMessage());
        }
    }

	// TRIỂN KHAI LOGIC TẢI THÔNG TIN LÊN FORM KHI CHỌN CARD
    private void handleSelectBookingCard(HoaDon hd) {
        try {
            // 1. Tải FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ChiTietDatBan_Popup.fxml"));
            VBox root = loader.load();
            
            // 2. Lấy Controller
            ChiTietDatBanController controller = loader.getController();

            // 3. Tạo Stage (Cửa sổ Popup)
            Stage popupStage = new Stage();
            popupStage.setTitle("Chi tiết đặt bàn " + (hd.getMaHD() != null ? hd.getMaHD() : "Mới"));
            
            // 4. TRUYỀN DỮ LIỆU ĐẾN CONTROLLER
            controller.setHoaDonData(hd, datBanDAO); // Truyền Hóa đơn và DAO
            
            // 5. Cấu hình Stage
            Scene scene = new Scene(root);
            
            // 🔥 Cấu hình quan trọng: Gán Controller cha (this) vào UserData của Scene Root
            // Dùng để các hàm trong Popup có thể gọi lại các hàm PUBLIC của DatBan.java
            root.setUserData(this); 
            
            // Đường dẫn CSS (Giữ nguyên logic cũ nếu cần)
            URL cssUrl = getClass().getResource("/css/DatBan.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            } else {
                 System.err.println("Không tìm thấy file css. Đảm bảo nó nằm trong src/main/resources/css/");
            }
            
            popupStage.setScene(scene);
            popupStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi UI", "Không thể tải giao diện chi tiết đặt bàn: " + e.getMessage());
        } catch (Exception e) {
             e.printStackTrace();
             showAlert(Alert.AlertType.ERROR, "Lỗi Hệ thống", "Đã xảy ra lỗi không xác định khi mở Popup: " + e.getMessage());
        }
    }
    
    // =========================================================
    // LOGIC TẢI MENU VÀ GỌI MÓN (ĐÃ SỬA)
    // =========================================================
    
    private void handleTimMon(String keyword) {
        if (keyword.trim().isEmpty()) {
            List<entity.MonAn> dsEntityMonAn = monAnDAO.getAllMonAn();
            loadMonAnTable(dsEntityMonAn.stream().map(MonAn::fromEntity).collect(Collectors.toList()));
        } else {
            List<entity.MonAn> dsEntityMonAn = monAnDAO.searchMonAnByName(keyword);
            loadMonAnTable(dsEntityMonAn.stream().map(MonAn::fromEntity).collect(Collectors.toList()));
        }
    }
    
    private void handleMenuToggle(String maDanhMuc) {
        if (maDanhMuc == null) return;
        
        List<entity.MonAn> dsEntityMonAn = monAnDAO.getMonAnByDanhMuc(maDanhMuc);
        loadMonAnTable(dsEntityMonAn.stream().map(MonAn::fromEntity).collect(Collectors.toList()));
        
        txtTimMon.clear();
    }
    
    private void loadMonAnTable(List<MonAn> dsMon) {
        ObservableList<MonAn> monAnObservableList = FXCollections.observableArrayList(dsMon);
        tblMonAn.setItems(monAnObservableList);
        tblMonAn.refresh();
    }
    
    private void setupMonAnTable() {
        colTenMon.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTenMon()));
        
        colGia.setCellValueFactory(cellData -> {
            return cellData.getValue().giaProperty(); 
        });
        
        colHinhAnh.setCellFactory(param -> new TableCell<MonAn, String>() {
            private final ImageView imageView = new ImageView();
            private final Label lblNoImage = new Label("N/A"); 

            { 
                imageView.setFitWidth(50); 
                imageView.setFitHeight(50);
                imageView.setPreserveRatio(true); 
                setAlignment(Pos.CENTER);
            }
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getItem() == null) {
                    setGraphic(null);
                } else {
                    byte[] hinhAnhBytes = getTableView().getItems().get(getIndex()).getHinhAnhBytes(); 
                    
                    if (hinhAnhBytes != null && hinhAnhBytes.length > 0) {
                        try {
                            Image image = new Image(new ByteArrayInputStream(hinhAnhBytes));
                            imageView.setImage(image);
                            setGraphic(imageView);
                            // SỬA LỖI: Cần gán giá trị item cho cell để không bị null pointer
                            setText(null); 
                        } catch (Exception e) {
                            System.err.println("❌ LỖI RUNTIME LOAD ẢNH: " + getTableView().getItems().get(getIndex()).getTenMon() + " - " + e.getMessage());
                            setGraphic(lblNoImage); 
                        }
                    } else {
                        setGraphic(lblNoImage); 
                    }
                }
            }
        });

        colChon.setCellFactory(tc -> new TableCell<MonAn, Void>() {
            final Button btn = new Button("Chọn");
            {
                btn.setOnAction(event -> {
                    MonAn mon = getTableView().getItems().get(getIndex());
                    handleChonMon(mon);
                    calculateTotal(); 
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btn);
                    btn.getStyleClass().add("btn-chon-mon");
                }
            }
        });
    }
    
    private void setupMonOrderTable() {
        colOrderTenMon.setCellValueFactory(cellData -> cellData.getValue().tenMonProperty());
        colOrderDonGia.setCellValueFactory(cellData -> cellData.getValue().donGiaProperty());
        colOrderSoLuong.setCellValueFactory(cellData -> cellData.getValue().soLuongProperty().asObject());

        colOrderTangGiam.setCellFactory(tc -> new TableCell<MonOrder, Void>() {
            final HBox box = new HBox(5);
            final Button btnMinus = new Button("-");
            final Button btnPlus = new Button("+");
            
            {
                btnMinus.getStyleClass().add("btn-quantity-control");
                btnPlus.getStyleClass().add("btn-quantity-control");
                box.setAlignment(Pos.CENTER);
                box.getChildren().addAll(btnMinus, btnPlus);

                btnPlus.setOnAction(event -> {
                    MonOrder order = getTableView().getItems().get(getIndex());
                    order.setSoLuong(order.getSoLuong() + 1);
                    tblMonDaChon.refresh();
                    calculateTotal(); 
                });

                btnMinus.setOnAction(event -> {
                    MonOrder order = getTableView().getItems().get(getIndex());
                    if (order.getSoLuong() > 1) {
                        order.setSoLuong(order.getSoLuong() - 1);
                        tblMonDaChon.refresh();
                        calculateTotal(); 
                    }
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        colOrderHuy.setCellFactory(tc -> new TableCell<MonOrder, Void>() {
            final Button btnHuy = new Button("X");
            {
                btnHuy.getStyleClass().add("btn-huy-mon");
                btnHuy.setOnAction(event -> {
                    MonOrder order = getTableView().getItems().get(getIndex());
                    monOrderList.remove(order);
                    tblMonDaChon.refresh();
                    calculateTotal(); 
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnHuy);
            }
        });
        
        tblMonDaChon.setItems(monOrderList);
    }

    private void handleChonMon(MonAn mon) {
        Optional<MonOrder> existingOrder = monOrderList.stream()
            .filter(o -> o.getMaMon().equals(mon.getMaMon()))
            .findFirst();

        if (existingOrder.isPresent()) {
            MonOrder order = existingOrder.get();
            order.setSoLuong(order.getSoLuong() + 1);
        } else {
            monOrderList.add(new MonOrder(mon.getMaMon(), mon.getTenMon(), mon.getGiaBan(), 1)); 
        }
        tblMonDaChon.refresh();
    }
    
    /**
     * TÍNH TOÁN VÀ HIỂN THỊ TỔNG TIỀN
     */
    private void calculateTotal() {
        double tongTienMonAn = monOrderList.stream()
                .mapToDouble(order -> order.getDonGia() * order.getSoLuong())
                .sum();
        
        // Khai báo tỷ lệ
        final double SERVICE_FEE_RATE = 0.05; // 5%
        final double VAT_RATE = 0.08;         // 8%
        
        // 🔥 SỬA LỖI: TÍNH PHÍ DỊCH VỤ DỰA TRÊN TỔNG TIỀN MÓN ĂN
        double phiDichVu = tongTienMonAn * SERVICE_FEE_RATE; 
        
        double thueVAT = (tongTienMonAn + phiDichVu) * VAT_RATE; // VAT tính trên (Tổng món + Phí dịch vụ)
        
        double tienKhuyenMai = 0.0; 
        if (selectedUuDai != null) {
            // Giảm giá trị * trên tổng tiền món ăn (Giả định GiaTri là % giảm)
            tienKhuyenMai = tongTienMonAn * (selectedUuDai.getGiaTri() / 100.0);
        }
        
        double tienCocDaThanhToan = 0.0;
        try {
            String tienCocRaw = txtTienCoc.getText().replaceAll("[^0-9.]", "");
            tienCocDaThanhToan = Double.parseDouble(tienCocRaw.isEmpty() ? "0" : tienCocRaw);
        } catch (NumberFormatException e) {
            tienCocDaThanhToan = 0.0;
        }
        
        double tongTienThanhToan = tongTienMonAn + phiDichVu + thueVAT - tienKhuyenMai - tienCocDaThanhToan;

        if (lblTongTienMonAn != null) lblTongTienMonAn.setText(String.format("%,.0f Đ", tongTienMonAn));
        // 🔥 CẬP NHẬT PHÍ DỊCH VỤ
        if (lblPhiDichVu != null) lblPhiDichVu.setText(String.format("%,.0f Đ", phiDichVu)); 
        
        if (lblThueVAT != null) lblThueVAT.setText(String.format("%,.0f Đ", thueVAT));
        if (lblKhuyenMai != null) lblKhuyenMai.setText(String.format("%,.0f Đ", tienKhuyenMai)); 
        if (lblTienCocSummary != null) lblTienCocSummary.setText(String.format("%,.0f Đ", tienCocDaThanhToan));
        if (lblTongTienThanhToan != null) lblTongTienThanhToan.setText(String.format("%,.0f Đ", Math.max(0, tongTienThanhToan))); 
    }

    
    public void clearFormDatBan() {
        // 1. Clear các trường nhập liệu
        txtThoiGian.clear(); //
        datePickerThoiGianDen.setValue(LocalDate.now()); //
        txtTenKhachHang.clear(); //
        txtSoDienThoai.clear(); //
        txtSoLuongKhach.clear(); //
        txtYeuCau.clear(); //
        txtTienCoc.clear(); //
        if (lblBanDangChon != null) lblBanDangChon.setText("Chưa chọn"); //
        if (lblTrangThaiBan != null) lblTrangThaiBan.setText("Trống"); //
        monOrderList.clear(); //
        calculateTotal(); //
        if (vboxReceipt != null) { //
            vboxReceipt.setVisible(false);
        }

        // === BƯỚC MỚI 2: RESET MÀU CỦA CỤM HÓA ĐƠN ĐANG XEM VỀ TRẠNG THÁI THỰC TẾ ===
        // (Dùng list HĐ gốc/phụ để reset màu, sau đó clear list này)
        if (currentHoaDonGocVaPhu != null && !currentHoaDonGocVaPhu.isEmpty()) {
            System.out.println("LOG clearFormDatBan: Reset màu cho " + currentHoaDonGocVaPhu.size() + " bàn từ HĐ cũ.");
            for (HoaDon hdCu : currentHoaDonGocVaPhu) {
                if (hdCu.getBan() != null) {
                    Button btn = tableButtonMap.get(hdCu.getBan().getMaBan());
                    if (btn != null) {
                        // Trả bàn về trạng thái thực tế (tính toán lại theo giờ hiện tại)
                        Ban banHienTai = hdCu.getBan();
                        TrangThaiBan trangThaiThuc = getTrangThaiHienThi(banHienTai, LocalTime.now());
                        applyTableStyle(btn, trangThaiThuc);
                    }
                }
            }
        }
        currentHoaDonGocVaPhu.clear(); // Reset list HĐ gốc/phụ
        // =========================================================================

        // 2. Reset màu của các bàn ĐANG CHỌN (màu xanh lá) về màu gốc
        for (Button btn : selectedButtonList) { //
             // Tìm đối tượng Ban tương ứng (cần để gọi getTrangThaiHienThi)
             Ban banGoc = selectedBanList.stream()
                            .filter(b -> selectedButtonList.indexOf(btn) == selectedBanList.indexOf(b))
                            .findFirst().orElse(null);
             if (banGoc != null) {
                 TrangThaiBan trangThaiGoc = getTrangThaiHienThi(banGoc, LocalTime.now()); //
                 applyTableStyle(btn, trangThaiGoc); // Trả lại màu trắng/đỏ tùy trạng thái gốc
             } else {
                  // Fallback: Nếu không tìm thấy Ban, cứ trả về màu trắng
                  applyTableStyle(btn, TrangThaiBan.TRONG);
             }
        }
        selectedBanList.clear(); // Xóa danh sách bàn đang chọn
        selectedButtonList.clear(); // Xóa danh sách nút đang chọn

        // 3. Reset trạng thái logic
        isBookingConfirmed = false; //
        currentHoaDon = null; // Reset hóa đơn đang xem

        // 4. Cập nhật hiển thị nút về trạng thái TẠO MỚI
        updateButtonVisibility(false); //
    }
    
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    // === THÊM MỚI ===
    private Optional<ButtonType> showAlertConfirm(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        return alert.showAndWait();
    }

    private void handleTimTenKhachHang(String sdt) {
        if (sdt.trim().isEmpty() || sdt.trim().length() < 9) { 
            return;
        }

        try {
            KhachHang khachTimDuoc = datBanDAO.timHoacTaoKhachHang(sdt, ""); 
            
            if (khachTimDuoc != null && khachTimDuoc.getMaKH() != null && khachTimDuoc.getTenKH() != null && !khachTimDuoc.getTenKH().equals("Khách vãng lai")) { 
                 txtTenKhachHang.setText(khachTimDuoc.getTenKH());
            } else if (khachTimDuoc != null && khachTimDuoc.getTenKH().equals("Khách vãng lai")) {
                 txtTenKhachHang.clear();
            }

        } catch (Exception e) {
            System.err.println("Lỗi khi tìm tên khách hàng qua SĐT: " + e.getMessage());
        }
    }

 // =========================================================
    // XỬ LÝ TÌM BÀN TRỐNG THEO THỜI GIAN
    // === ĐÃ SỬA: SỬ DỤNG GIỜ TÌM KIẾM ĐỂ TÍNH MÀU SẮC (KHẮC PHỤC LỖI MẤT MÀU) ===
    // =========================================================
    private void handleTimBanTrong() {

        LocalDate ngay; //
        String gioStr; //
        LocalTime gio; // // Giờ tìm kiếm
        java.sql.Timestamp ts; //

        // Lưu trạng thái xem HĐ cũ (vì clearFormDatBan sẽ xóa nó)
        boolean dangXemHoaDonCu = (this.currentHoaDon != null); 
        
        // Cần lưu lại cụm HĐ cũ để hiển thị màu trở lại nếu cần
        List<HoaDon> cumHoaDonDangXemSnapshot = new ArrayList<>(this.currentHoaDonGocVaPhu);


        try {
            // 1. ĐỌC GIÁ TRỊ TỪ UI VÀ PARSE
            ngay = datePickerThoiGianDen.getValue(); 
            gioStr = txtThoiGian.getText(); 

            if (ngay == null || gioStr == null || gioStr.trim().isEmpty()) { 
                showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng nhập Ngày và Giờ để tìm bàn.");
                return;
            }

            gio = LocalTime.parse(gioStr, timeFormatter); 
            ts = java.sql.Timestamp.valueOf(ngay.atTime(gio)); 

            // 2. LUÔN LUÔN RESET FORM VÀ CÁC TRẠNG THÁI
            System.out.println("LOG handleTimBanTrong: Nhấn Tìm -> Reset form và trạng thái.");
            clearFormDatBan(); 
            this.currentHoaDon = null; 
            this.isBookingConfirmed = false; 

            // 3. Đặt lại giá trị Ngày/Giờ sau khi clear
            datePickerThoiGianDen.setValue(ngay); 
            txtThoiGian.setText(gioStr); 


            System.out.println("\n*** LOG: THỰC HIỆN TÌM BÀN TRỐNG TẠI THỜI GIAN NGƯỜI DÙNG NHẬP: " + gioStr + " ***");

            // 4. LẤY TRẠNG THÁI KHẢ DỤNG TỪ DAO (Bảng tất cả bàn + isAvailable)
            List<Map<String, Object>> allBanInfo = datBanDAO.getAllBanWithAvailability(ts); 

            // 5. CẬP NHẬT DS HĐ ĐANG CHỜ (cho logic tô màu)
            // Phải tải lại danh sách HĐ cho ngày đang tìm kiếm
            this.dsHoaDonDatTrongNgay = datBanDAO.getDsDatBanHomNay(ngay); 
            System.out.println("  DEBUG: Đã cập nhật dsHoaDonDatTrongNgay cho ngày " + ngay + ". Số lượng: " + dsHoaDonDatTrongNgay.size()); 

            // 6. XÁC ĐỊNH MÀU HIỂN THỊ (ƯU TIÊN LOGIC 4/8 TIẾNG TỪ CONTROLLER)
            List<Ban> banHienThi = new ArrayList<>(); 
            
            System.out.println("  DEBUG: Bắt đầu vòng lặp tô màu:"); 
            for (Map<String, Object> banInfo : allBanInfo) { 
                Ban ban = (Ban) banInfo.get("ban");
                Ban banMoi = new Ban(ban.getMaBan(), ban.getViTri(), ban.getSucChua(), ban.getLoaiBan(), ban.getTrangThai());

                TrangThaiBan finalStatus;
                
                // Trạng thái theo logic 4/8 tiếng (ĐỎ/CAM)
                TrangThaiBan trangThaiTheoLogic48 = getTrangThaiHienThi(banMoi, gio); 
                
                // Trạng thái khả dụng (từ DAO)
                boolean isAvailable = (Boolean) banInfo.get("isAvailable");
                
                System.out.println("    - Processing Table: " + ban.getMaBan() + " | Is Available (DAO): " + isAvailable + " | 4/8h Logic Status: " + trangThaiTheoLogic48); 

                // === LOGIC ƯU TIÊN MỚI ===
                // Nếu logic 4/8 tiếng cho thấy ĐỎ/CAM -> Ưu tiên giữ màu đó, bất kể DAO nói gì.
                if (trangThaiTheoLogic48 != TrangThaiBan.TRONG) {
                     finalStatus = trangThaiTheoLogic48;
                     System.out.println("      -> Priority 1: Assigning status based on 4/8h logic (BUSY): " + finalStatus);
                }
                // Nếu không có HĐ nào ràng buộc (theo logic 4/8h) -> Set TRỐNG
                else {
                     finalStatus = TrangThaiBan.TRONG;
                     System.out.println("      -> Priority 2: Assigning status: TRONG.");
                }

                banMoi.setTrangThai(finalStatus);
                System.out.println("      ===> Final Assigned Status for " + banMoi.getMaBan() + ": " + finalStatus);
                banHienThi.add(banMoi); 
            }
            
            // 7. TẢI LẠI GIAO DIỆN
            loadTableGridsBase(banHienThi); 

        } catch (java.time.format.DateTimeParseException e) { 
            showAlert(Alert.AlertType.ERROR, "Lỗi định dạng", "Giờ nhập không hợp lệ. Vui lòng nhập theo định dạng HH:mm (ví dụ: 14:30).");
        } catch (Exception ex) { 
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tìm bàn trống. Kiểm tra dữ liệu đầu vào hoặc kết nối DB.");
            // Nếu có lỗi, tải lại sơ đồ bàn với giờ hiện tại để không bị trống
            loadTableGrids(); 
        }
    }
    /**
     * 🔥 HÀM HELPER MỚI: Tải lại sơ đồ bàn dựa trên trạng thái của cụm HĐ đang xem
     * (Dùng khi có lỗi trong handleTimBanTrong)
     */
    private void loadTableGridsBaseBasedOnCurrentBooking(List<HoaDon> cumHoaDonDangXem) {
         List<Ban> tatCaBan = datBanDAO.getAllBan(); // Lấy tất cả bàn
         Set<String> maBanDangXemSet = cumHoaDonDangXem.stream()
                                            .filter(hd -> hd.getBan() != null)
                                            .map(hd -> hd.getBan().getMaBan())
                                            .collect(Collectors.toSet());
         HoaDon hoaDonGoc = cumHoaDonDangXem.stream().filter(hd->hd.getMaHDGoc() == null).findFirst().orElse(null);
         TrangThaiBan trangThaiCanTo = (hoaDonGoc != null && TrangThaiHoaDon.fromDbValue(hoaDonGoc.getTrangThai().getDbValue()) == TrangThaiHoaDon.DANG_SU_DUNG)
                                                  ? TrangThaiBan.DANG_SU_DUNG
                                                  : TrangThaiBan.DA_DAT;

         List<Ban> banHienThi = new ArrayList<>();
         LocalTime thoiGianHienTai = LocalTime.now();
         for (Ban ban : tatCaBan) {
              Ban banMoi = new Ban(ban.getMaBan(), ban.getViTri(), ban.getSucChua(), ban.getLoaiBan(), ban.getTrangThai());
              if (maBanDangXemSet.contains(ban.getMaBan())) {
                  banMoi.setTrangThai(trangThaiCanTo); // Tô màu cho bàn đang xem
              } else {
                   // Với các bàn khác, lấy trạng thái theo giờ hiện tại (logic cũ của loadTableGrids)
                   TrangThaiBan ttHienThi = getTrangThaiHienThi(banMoi, thoiGianHienTai);
                   banMoi.setTrangThai(ttHienThi);
              }
              banHienThi.add(banMoi);
         }
         loadTableGridsBase(banHienThi); // Vẽ lại lưới
    }
    /**
     * 🔥 HÀM MỚI: Cập nhật trạng thái hiển thị của các nút chức năng
     * @param isViewingOldHoaDon true nếu đang xem HĐ cũ, false nếu đang tạo HĐ mới.
     */
    private void updateButtonVisibility(boolean isViewingOldHoaDon) {
        // Nút cho HĐ MỚI
        btnLuuDatHang.setVisible(!isViewingOldHoaDon);
        btnXacNhanBan.setVisible(!isViewingOldHoaDon); // Ẩn nút xác nhận bàn luôn

        // Nút cho HĐ CŨ
        btnSuaMon.setVisible(isViewingOldHoaDon);
        btnDoiBan.setVisible(isViewingOldHoaDon);
        btnTachBan.setVisible(isViewingOldHoaDon);
        btnGopBan.setVisible(isViewingOldHoaDon);
        btnHuyBan.setVisible(isViewingOldHoaDon);
        
        // Nút Thanh Toán luôn hiển thị khi click
        btnThanhToan.setVisible(true); 
    }
    
    // === THÊM MỚI: CÁC HÀM XỬ LÝ SỰ KIỆN CHO NÚT MỚI ===
    // =========================================================
    // HÀNH ĐỘNG CHO HÓA ĐƠN CŨ (SỬA, HỦY, ĐỔI)
    // =========================================================

    /**
     * 🔥 XỬ LÝ SỬA HÓA ĐƠN (Thông tin KH, Tiền cọc, Danh sách món)
     */
 // ui.DatBan.java

    /**
     * 🔥 XỬ LÝ SỬA HÓA ĐƠN (Thông tin KH, Tiền cọc, Danh sách món)
     * ĐÃ SỬA: Loại bỏ hoàn toàn hộp thoại xác nhận. Việc lưu được thực hiện tự động.
     */
    private void handleSuaHoaDon() {
        if (currentHoaDon == null) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không có hóa đơn nào được chọn để sửa.");
            return;
        }
        
        // 🔥 BỎ QUA BƯỚC XÁC NHẬN (CONFIRMATION MODAL) VÀ THỰC HIỆN LƯU TRỰC TIẾP
        
        try {
            // 1. Cập nhật Khách hàng
            // Sử dụng dữ liệu từ UI để tìm hoặc tạo Khách hàng
            KhachHang kh = datBanDAO.timHoacTaoKhachHang(txtSoDienThoai.getText(), txtTenKhachHang.getText());
            
            // 2. Cập nhật Tiền cọc
            double tienCoc = 0;
            try {
                 String tienCocRaw = txtTienCoc.getText().replaceAll("[^0-9.]", ""); 
                 tienCoc = Double.parseDouble(tienCocRaw.isEmpty() ? "0" : tienCocRaw);
            } catch (NumberFormatException e) {
                 showAlert(Alert.AlertType.ERROR, "Lỗi", "Tiền cọc không hợp lệ.");
                 return;
            }

            // 3. Gọi DAO cập nhật thông tin chính (Sử dụng MaHD Gốc)
            datBanDAO.capNhatThongTinHoaDon(currentHoaDon.getMaHD(), kh.getMaKH(), tienCoc);

            // 4. Gọi DAO cập nhật chi tiết món ăn (Xóa cũ, thêm mới)
            datBanDAO.capNhatChiTietHoaDon(currentHoaDon.getMaHD(), monOrderList);
            
            // Chỉ hiện thông báo thành công nếu người dùng nhấn nút Sửa Món
            if (btnSuaMon.isFocused()) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã cập nhật Hóa đơn " + currentHoaDon.getMaHD() + " thành công.");
            }
            
            // Tải lại danh sách bên trái (cần thiết cho cả Sửa và Thanh toán)
            loadBookingCards(); 

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi CSDL", "Không thể cập nhật hóa đơn: " + e.getMessage());
        }
    }
   
    /**
     * 🔥 XỬ LÝ HỦY BÀN
     */
    private void handleHuyBan() {
        if (currentHoaDon == null) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không có hóa đơn nào được chọn để hủy.");
            return;
        }
        // Kiểm tra HĐ Gốc
        if (currentHoaDon.getTrangThai() != null && currentHoaDon.getTrangThai().getDbValue().equals("DaThanhToan")) { // === SỬA === (Kiểm tra null)
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể hủy Hóa đơn đã thanh toán.");
            return;
        }

        // 2. Xác nhận (Quan trọng)
        Optional<ButtonType> result = showAlertConfirm("XÁC NHẬN HỦY", 
            "Bạn có chắc muốn HỦY toàn bộ cụm Hóa đơn Gốc " + currentHoaDon.getMaHD() + " và các Hóa đơn Phụ liên quan?\nBàn liên quan sẽ được trả trống. KHÔNG THỂ HOÀN TÁC!");
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
             try {
                // Lấy tất cả Hóa đơn liên quan (Gốc và Phụ)
                List<HoaDon> allRelatedHDs = new ArrayList<>(currentHoaDonGocVaPhu); // Lấy từ list đã load sẵn

                // 3. Xử lý TẤT CẢ Hóa đơn liên quan
                for (HoaDon hdToHuy : allRelatedHDs) {
                    // Hủy Hóa đơn (Gốc hoặc Phụ)
                    datBanDAO.capNhatTrangThaiHoaDon(hdToHuy.getMaHD(), TrangThaiHoaDon.DA_HUY.getDbValue(), true); // true = set giờ ra

                    // Trả Bàn liên quan về trạng thái "Trong"
                    if (hdToHuy.getBan() != null) {
                        datBanDAO.capNhatTrangThaiBan(hdToHuy.getBan().getMaBan(), "Trong");
                        System.out.println("LOG: Đã hủy HD " + hdToHuy.getMaHD() + " và trả bàn " + hdToHuy.getBan().getMaBan());
                    }
                }
                
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã hủy toàn bộ cụm Hóa đơn gốc " + currentHoaDon.getMaHD() + " và các hóa đơn phụ liên quan.");
                
                // 4. Reset
                clearFormDatBan();
                loadBookingCards();
                loadTableGrids();

             } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Lỗi CSDL", "Không thể hủy hóa đơn: " + e.getMessage());
             }
        }
    }
    // =========================================================
    // XỬ LÝ THANH TOÁN (MỚI)
    // =========================================================
    private void handleThanhToan() {
        if (currentHoaDon == null) {
            showAlert(Alert.AlertType.WARNING, "Không có hóa đơn", "Vui lòng chọn đơn đặt/đang phục vụ bên trái để xem hóa đơn.");
            return;
        }
        
        if (currentHoaDon.getTrangThai().getDbValue().equals("DaThanhToan")) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Hóa đơn này đã được thanh toán.");
            return;
        }

        // 1. Cập nhật và lưu thay đổi vào CSDL
        handleSuaHoaDon(); 
        
        // 2. Tải lại chi tiết món ăn (monOrderList) từ CSDL mới nhất
        monOrderList.clear();
        if (currentHoaDon.getMaHD() != null) {
            ObservableList<MonOrder> chiTiet = datBanDAO.getChiTietHoaDon(currentHoaDon.getMaHD());
            monOrderList.addAll(chiTiet);
        }
        
        // 3. Tính toán lại tổng tiền
        calculateTotal(); 

        // 4. HIỂN THỊ PANEL THANH TOÁN (vboxReceipt)
        if (vboxReceipt != null) {
            vboxReceipt.setVisible(true);

            // -------------------------------------------------------------
            // CẬP NHẬT THÔNG TIN CHUNG HÓA ĐƠN
            // -------------------------------------------------------------
            String maHD = currentHoaDon.getMaHD() != null ? currentHoaDon.getMaHD() : "N/A";
            
            // Bàn: Lấy từ HĐ Gốc
            String tenBan = currentHoaDon.getBan() != null ? currentHoaDon.getBan().getMaBan() : "N/A";
            
            // Giờ vào: Lấy từ trường GioVao của HĐ
            String gioVaoStr = currentHoaDon.getGioVao() != null ? 
                               currentHoaDon.getGioVao().toLocalTime().format(timeFormatter) : "N/A";
            
            // Giờ ra: Lấy giờ hệ thống hiện tại
            String gioRaStr = LocalTime.now().format(timeFormatter); 
            
            // Thu ngân: Lấy tên nhân viên đang đăng nhập (Cần có biến NhanVien đang đăng nhập)
            String tenThuNgan = "N/A";
            try {
                // Gọi hàm static từ MainApp để lấy đối tượng TaiKhoan
                TaiKhoan tk = MainApp.getLoggedInUser();
                if (tk != null && tk.getNhanVien() != null) {
                    // Giả định tên nhân viên được lấy bằng getHoTen()
                    tenThuNgan = tk.getNhanVien().getHoTen(); 
                }
            } catch (Exception e) {
                // Xử lý lỗi nếu việc lấy thông tin thất bại
                System.err.println("Lỗi khi lấy tên nhân viên đăng nhập: " + e.getMessage());
            } 
            
            // Đổ dữ liệu vào Labels
            if (lblSoHD != null) lblSoHD.setText("Số HĐ: " + maHD); 
            if (lblBanHD != null) lblBanHD.setText("Bàn: " + tenBan);
            if (lblThuNgan != null) lblThuNgan.setText("Thu ngân: " + tenThuNgan);
            if (lblGioVao != null) lblGioVao.setText("Giờ vào: " + gioVaoStr);
            if (lblGioRa != null) lblGioRa.setText("Giờ ra: " + gioRaStr);
            // -------------------------------------------------------------
            
            // 5. CẬP NHẬT TABLE VIEW CHO THANH TOÁN (Danh sách món ăn)
            if (tblHoaDon != null) {
                ObservableList<TableColumn<MonOrder, ?>> columns = tblHoaDon.getColumns();

                if (columns.size() >= 5) {
                     TableColumn<MonOrder, Integer> colStt = (TableColumn<MonOrder, Integer>) columns.get(0);
                     colStt.setCellValueFactory(data -> new SimpleIntegerProperty(tblHoaDon.getItems().indexOf(data.getValue()) + 1).asObject());
                     
                     TableColumn<MonOrder, String> colTenMon = (TableColumn<MonOrder, String>) columns.get(1);
                     colTenMon.setCellValueFactory(data -> data.getValue().tenMonProperty());
                     
                     TableColumn<MonOrder, Integer> colSL = (TableColumn<MonOrder, Integer>) columns.get(2);
                     colSL.setCellValueFactory(data -> data.getValue().soLuongProperty().asObject());
                     
                     TableColumn<MonOrder, Double> colDonGia = (TableColumn<MonOrder, Double>) columns.get(3);
                     colDonGia.setCellValueFactory(data -> data.getValue().donGiaProperty().asObject());
                     
                     TableColumn<MonOrder, Double> colTong = (TableColumn<MonOrder, Double>) columns.get(4);
                     colTong.setCellValueFactory(data -> {
                         double total = data.getValue().getSoLuong() * data.getValue().getDonGia();
                         return new SimpleDoubleProperty(total).asObject();
                     });
                     
                     tblHoaDon.setItems(monOrderList);
                     tblHoaDon.refresh();
                } else {
                    System.err.println("Lỗi: tblHoaDon không có đủ 5 cột để ánh xạ.");
                }
            }
            
            // 6. ĐỔ DỮ LIỆU TỔNG TIỀN VÀO CÁC LABEL
            currentHoaDon.setTongCongMonAn(monOrderList.stream()
                .mapToDouble(order -> order.getDonGia() * order.getSoLuong()).sum());

            if (lblTongTienMonAn != null) lblTongTienMonAn.setText(String.format("%,.0f Đ", currentHoaDon.getTongCongMonAn()));
            if (lblPhiDichVu != null) lblPhiDichVu.setText(String.format("%,.0f Đ", currentHoaDon.getPhiDichVu()));
            if (lblThueVAT != null) lblThueVAT.setText(String.format("%,.0f Đ", currentHoaDon.getThueVAT()));
            if (lblTienCocSummary != null) lblTienCocSummary.setText(String.format("%,.0f Đ", currentHoaDon.getTienCoc()));
            if (lblKhuyenMai != null) lblKhuyenMai.setText(String.format("%,.0f Đ", currentHoaDon.getKhuyenMai()));
            if (lblTongTienThanhToan != null) lblTongTienThanhToan.setText(String.format("%,.0f Đ", currentHoaDon.getTongTienThanhToan()));
        }
    }
    
    // =========================================================
    // LỚP VIEWMODEL (SỬA LỖI KIỂU DỮ LIỆU)
    // =========================================================
    
    public static class MonAn {
        private final SimpleStringProperty maMon;
        private final SimpleStringProperty tenMon;
        private final byte[] hinhAnhBytes; 
        private final SimpleDoubleProperty giaBan;

        public MonAn(String maMon, String tenMon, double giaBan, byte[] hinhAnhBytes) {
            this.maMon = new SimpleStringProperty(maMon);
            this.tenMon = new SimpleStringProperty(tenMon);
            this.giaBan = new SimpleDoubleProperty(giaBan);
            this.hinhAnhBytes = hinhAnhBytes; 
        }
        public String getMaMon() { return maMon.get(); }
        public String getTenMon() { return tenMon.get(); }
        public SimpleStringProperty tenMonProperty() { return tenMon; }
        public double getGiaBan() { return giaBan.get(); }
        public SimpleDoubleProperty giaProperty() { return giaBan; }
        public byte[] getHinhAnhBytes() { return hinhAnhBytes; }
        public static MonAn fromEntity(entity.MonAn entity) {
            return new MonAn(entity.getMaMon(), entity.getTenMon(), entity.getGiaBan(), entity.getHinhAnh());
        }
    }
    
    public static class MonOrder {
        private final SimpleStringProperty maMon;
        private final SimpleStringProperty tenMon;
        private final SimpleDoubleProperty donGia;
        private final SimpleIntegerProperty soLuong;

        public MonOrder(String maMon, String tenMon, double donGia, int soLuong) {
            this.maMon = new SimpleStringProperty(maMon);
            this.tenMon = new SimpleStringProperty(tenMon);
            this.donGia = new SimpleDoubleProperty(donGia);
            this.soLuong = new SimpleIntegerProperty(soLuong);
        }
        
        public String getMaMon() { return maMon.get(); }
        public String getTenMon() { return tenMon.get(); }
        public SimpleStringProperty tenMonProperty() { return tenMon; }
        public double getDonGia() { return donGia.get(); }
        public SimpleDoubleProperty donGiaProperty() { return donGia; }
        public int getSoLuong() { return soLuong.get(); }
        public void setSoLuong(int soLuong) { this.soLuong.set(soLuong); }
        public SimpleIntegerProperty soLuongProperty() { return soLuong; }
    }

    /**
     * 🔥 HELPER MỚI: Tải lại danh sách HĐ đang chờ cho logic tô màu.
     * Cần phải PUBLIC để DoiBanPopupController có thể gọi.
     */
    public void loadDsHoaDonDatTrongNgay(LocalDate date) {
        this.dsHoaDonDatTrongNgay = datBanDAO.getDsDatBanHomNay(date);
    }
    
    /**
     * HELPER: Kiểm tra xem bàn có nằm trong cụm HĐ đang xem (HĐ Gốc/Phụ) không.
     * Cần phải PUBLIC để TachBanPopupController và DoiBanPopupController có thể gọi.
     */
    public boolean isBanInCurrentBooking(Ban ban) {
         if (ban == null || currentHoaDonGocVaPhu.isEmpty()) return false;
         return currentHoaDonGocVaPhu.stream()
            .filter(hd -> hd.getBan() != null)
            .anyMatch(hd -> hd.getBan().getMaBan().equals(ban.getMaBan()));
    }

   
   
}
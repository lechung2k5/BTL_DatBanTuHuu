package ui;

// === IMPORTS CẦN THIẾT CHO LOGIC ĐẶT BÀN VÀ DAO ===
import dao.DatBanDAO; 
import dao.MonAnDAO;       
import dao.DanhMucMonDAO;  
import entity.Ban; 
import entity.HoaDon; 
import entity.KhachHang; 
import entity.LoaiBan; 
import entity.DanhMucMon;  
import entity.TrangThaiBan; 
import entity.TrangThaiHoaDon;
import entity.PTTThanhToan; 
import entity.TaiKhoan;
import dao.UuDaiDAO; 
import entity.UuDai; 
// Import JavaFX
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.Comparator;
import java.util.HashMap;
import java.io.ByteArrayInputStream;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
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
import javafx.scene.Scene; 
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.DialogPane;
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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javax.print.attribute.HashPrintRequestAttributeSet; 
import javax.print.attribute.PrintRequestAttributeSet;



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
    private boolean daThanhToanCoc = false;
    
    
    // === CỘT 1 (Left Panel) ===
    @FXML private ComboBox<String> comboFilter;
    @FXML private TextField txtSearch;
    @FXML private VBox vboxBookingCards;
    
    @FXML private ScrollPane middleScrollPane; 
    @FXML private ScrollPane vboxReceipt;
    
    // === CỘT 2 (Middle Panel) ===
    @FXML private ComboBox<String> comboThoiGian;
    @FXML private DatePicker datePickerThoiGianDen;
    @FXML private ComboBox<String> comboKhuVuc;
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
    @FXML private ComboBox<String> comboTrangThaiHienTai; // 🔥 MỚI
    @FXML private Button btnCapNhatTrangThai; // 🔥 MỚI

    
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
    	// --- 1. KHỞI TẠO COMBOBOX TRẠNG THÁI ---
    	// --- 1. KHỞI TẠO COMBOBOX TRẠNG THÁI (ĐÃ SỬA THEO YÊU CẦU GIẢNG VIÊN) ---
        if (comboTrangThaiHienTai != null) {
            comboTrangThaiHienTai.setItems(FXCollections.observableArrayList(
                "Chờ xác nhận",  // CHO_XAC_NHAN
                "Đã đặt",        // DAT
                "Đã nhận bàn"    // DANG_SU_DUNG (Tên mới thay cho "Đang phục vụ")
                // 🔥 ĐÃ ẨN: "Hóa đơn tạm", "Đã thanh toán", "Đã hủy"
            ));
        }

        // --- 2. GÁN SỰ KIỆN CẬP NHẬT ---
        if (btnCapNhatTrangThai != null) {
            btnCapNhatTrangThai.setOnAction(e -> handleCapNhatTrangThaiNhanh());
        }
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
    

        btnXacNhanBan.setOnAction(e -> handleXacNhanBan()); 
        if (btnThanhToanCoc != null) {
            btnThanhToanCoc.setOnAction(e -> openThanhToanCocPopup());
            btnThanhToanCoc.setDisable(true); // Vô hiệu hóa ban đầu
        }
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

        txtSoDienThoai.focusedProperty().addListener((obs, oldVal, isNowFocused) -> {
            
            // Chỉ chạy khi *mất focus* (!isNowFocused)
            if (!isNowFocused) { 
                String sdt = txtSoDienThoai.getText().trim();
                if (sdt.isEmpty() || sdt.length() < 9) {
                    return; // Không làm gì nếu SĐT không hợp lệ
                }
                
                try {
                    // 🔥 Chỉ TÌM KIẾM, không TẠO MỚI (gọi hàm mới ở Bước 1)
                    KhachHang khachTimDuoc = datBanDAO.timKhachHangBySDT(sdt); 
                    
                    if (khachTimDuoc != null) {
                        // Nếu tìm thấy, điền tên khách hàng
                        // (Tên có thể là null nếu khách cũ không có tên)
                        txtTenKhachHang.setText(khachTimDuoc.getTenKH()); 
                    } else {
                        // Nếu không tìm thấy, XÓA TRẮNG ô tên
                        // để người dùng tự nhập tên cho KHÁCH HÀNG MỚI.
                        txtTenKhachHang.clear();
                    }
                } catch (Exception e) {
                     System.err.println("Lỗi khi tìm tên khách hàng (sự kiện focus): " + e.getMessage());
                }
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
     // --- KHỞI TẠO COMBOBOX GIỜ (Cách nhau 30 phút) ---
        ObservableList<String> timeSlots = FXCollections.observableArrayList();
        LocalTime startTime = LocalTime.of(8, 0); // Mở cửa lúc 8h sáng
        LocalTime endTime = LocalTime.of(22, 0);  // Đóng cửa lúc 10h tối

        while (!startTime.isAfter(endTime)) {
            timeSlots.add(startTime.format(timeFormatter));
            startTime = startTime.plusMinutes(30);
        }

        if (comboThoiGian != null) {
            comboThoiGian.setItems(timeSlots);
            // Set giờ hiện tại (làm tròn lên 30p tiếp theo)
            LocalTime now = LocalTime.now();
            if (now.getMinute() > 30) {
                comboThoiGian.setValue(now.plusHours(1).withMinute(0).format(timeFormatter));
            } else {
                comboThoiGian.setValue(now.withMinute(30).format(timeFormatter));
            }
        }
     // --- 🔥 THÊM MỚI: Khởi tạo ComboBox Khu Vực ---
        if (comboKhuVuc != null) {
            comboKhuVuc.setItems(FXCollections.observableArrayList(
                "Tự động",    // Máy tự tính theo số người
                "Tầng trệt", 
                "Tầng 1", 
                "Phòng riêng",
                "Tất cả"      // Hiện hết không lọc
            ));
            comboKhuVuc.setValue("Tự động"); // Mặc định để máy tính
        }
    }
    
    // =========================================================
    // HÀM NGHIỆP VỤ TÁCH BÀN (CHÍNH)
    // =========================================================

 // ui.DatBan.java

    /**
     * 🔥 HÀM MỚI: Cập nhật trạng thái từ ComboBox (Có Validate Logic)
     */
    private void handleCapNhatTrangThaiNhanh() {
        if (currentHoaDon == null || currentHoaDon.getMaHD() == null) {
            showAlert(Alert.AlertType.WARNING, "Lỗi", "Vui lòng chọn một hóa đơn để cập nhật.");
            return;
        }

        String trangThaiMoiDisplay = comboTrangThaiHienTai.getValue();
        if (trangThaiMoiDisplay == null) return;

        // 1. Map từ tên hiển thị Tiếng Việt về DB Value
        String trangThaiMoiDb = "";
        if (trangThaiMoiDisplay.equals("Đã nhận bàn")) trangThaiMoiDb = TrangThaiHoaDon.DANG_SU_DUNG.getDbValue();
        else if (trangThaiMoiDisplay.equals("Đã đặt")) trangThaiMoiDb = TrangThaiHoaDon.DAT.getDbValue();
        else if (trangThaiMoiDisplay.equals("Chờ xác nhận")) trangThaiMoiDb = TrangThaiHoaDon.CHO_XAC_NHAN.getDbValue();
        else {
             showAlert(Alert.AlertType.ERROR, "Lỗi", "Trạng thái không hợp lệ.");
             return;
        }

        // 2. 🔥 LOGIC CHẶN LÙI TRẠNG THÁI (Quan trọng)
        String trangThaiHienTaiDb = currentHoaDon.getTrangThai().getDbValue();
        
        // Nếu đang là ĐÃ ĐẶT hoặc ĐÃ NHẬN BÀN -> Không được về CHỜ XÁC NHẬN
        if ((trangThaiHienTaiDb.equals(TrangThaiHoaDon.DAT.getDbValue()) || 
             trangThaiHienTaiDb.equals(TrangThaiHoaDon.DANG_SU_DUNG.getDbValue())) 
             && trangThaiMoiDb.equals(TrangThaiHoaDon.CHO_XAC_NHAN.getDbValue())) {
            
            showAlert(Alert.AlertType.WARNING, "Sai quy trình", 
                      "Hóa đơn đã được xác nhận hoặc đang phục vụ.\nKhông thể quay lại trạng thái 'Chờ xác nhận'.");
            // Reset lại combobox về cũ
            loadHoaDonToMainInterface(currentHoaDon); 
            return;
        }

        try {
            // 3. Cập nhật Hóa đơn
            datBanDAO.capNhatTrangThaiHoaDon(currentHoaDon.getMaHD(), trangThaiMoiDb, false);

            // 4. Cập nhật Bàn (Gốc và Phụ)
            String banStatusUpdate = getTrangThaiBanFromHoaDon(trangThaiMoiDb);
            for (HoaDon hd : currentHoaDonGocVaPhu) {
                if (hd.getBan() != null) {
                    datBanDAO.capNhatTrangThaiBan(hd.getBan().getMaBan(), banStatusUpdate);
                }
            }
            
            // Cập nhật lại đối tượng hiện tại để đồng bộ
            currentHoaDon.setTrangThai(trangThaiMoiDb);

            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã cập nhật trạng thái thành: " + trangThaiMoiDisplay);
            
            // 5. Refresh giao diện
            loadBookingCards();
            loadHoaDonToMainInterface(currentHoaDon); // Load lại để tô màu bàn

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể cập nhật trạng thái: " + e.getMessage());
        }
    }

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
     * 🔥 HÀM TẠO PDF HÓA ĐƠN (FULL)
     * ĐÃ CẬP NHẬT: VAT = 8% * Tổng Tiền Món Ăn
     */
    private PDDocument createReceiptPdf(HoaDon hd) throws IOException {
        PDDocument document = new PDDocument();
        // SỬ DỤNG KHỔ GIẤY A4
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

        // Fallback font nếu không load được font tiếng Việt
        if (font == null) font = org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA;
        if (fontBold == null) fontBold = org.apache.pdfbox.pdmodel.font.PDType1Font.HELVETICA_BOLD;
        // ============================================================

        // ====== TÍNH TOÁN LẠI SỐ LIỆU (ĐỒNG BỘ VỚI UI) ======
        ObservableList<MonOrder> monAnList = datBanDAO.getChiTietHoaDon(hd.getMaHD());
        
        // 1. Tổng tiền món
        double tongTienMonAn = monAnList.stream()
                .mapToDouble(order -> order.getDonGia() * order.getSoLuong())
                .sum();
        
        // 2. Phí dịch vụ (5% trên tổng món)
        double phiDichVu = tongTienMonAn * 0.05; 
        
        // 3. 🔥 Thuế VAT (8% TRÊN TỔNG MÓN ĂN - THEO YÊU CẦU)
        double thueVAT = tongTienMonAn * 0.08; 
        
        double tienKhuyenMai = hd.getKhuyenMai(); // Lấy từ Model (đã được tính ở UI)
        double tienCoc = hd.getTienCoc();
        
        // 4. Tổng thanh toán
        double tongThanhToan = tongTienMonAn + phiDichVu + thueVAT - tienCoc - tienKhuyenMai;
        if (tongThanhToan < 0) tongThanhToan = 0;

        // Cập nhật ngược lại vào object HD để đồng bộ dữ liệu nếu cần dùng sau này
        hd.setTongCongMonAn(tongTienMonAn);
        hd.setPhiDichVu(phiDichVu);
        hd.setThueVAT(thueVAT);
        // (KhuyenMai và TienCoc đã có sẵn)

        final java.text.DecimalFormat currencyFormatter = new java.text.DecimalFormat("###,###");
        final java.time.format.DateTimeFormatter dateFormatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
        final java.time.format.DateTimeFormatter timeFormatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm");

        // --- Chuẩn bị dữ liệu hiển thị ---
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
        } catch (Exception e) {}
        String khachHangStr = (hd.getKhachHang() != null && hd.getKhachHang().getSoDT() != null) ? hd.getKhachHang().getSoDT() : "N/A";
        String hinhThucTTStr = hd.getHinhThucTT() != null ? hd.getHinhThucTT().getDisplayName() : "N/A";
        // ============================================================

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
        
            // === 1. HEADER & THÔNG TIN QUÁN ===
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
            String sdt = "SĐT: 0909 123 456";
            float sdtWidth = font.getStringWidth(sdt) / 1000 * 10;
            contentStream.newLineAtOffset((PAGE_WIDTH - sdtWidth) / 2, pos.y);
            contentStream.showText(sdt);
            contentStream.endText();
            pos.y -= LINE_HEIGHT * 1.5;

            // === 2. TIÊU ĐỀ HÓA ĐƠN ===
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

            // Tiêu đề bảng
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
            
            // Nội dung bảng
            contentStream.setFont(font, FONT_SIZE_TABLE);
            float currentY = pos.y - LINE_HEIGHT * 1.2f; 
            
            for (int i = 0; i < monAnList.size(); i++) {
                MonOrder mon = monAnList.get(i);
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
                
                String ttStr = currencyFormatter.format(mon.getDonGia() * mon.getSoLuong());
                float ttWidth = fontBold.getStringWidth(ttStr) / 1000 * FONT_SIZE_TABLE;
                contentStream.setTextMatrix(org.apache.pdfbox.util.Matrix.getTranslateInstance(colThanhTien - ttWidth + 15, currentY)); 
                contentStream.showText(ttStr);
                
                contentStream.endText();
                currentY -= LINE_HEIGHT * 1.6f; 
            }
            pos.y = currentY + LINE_HEIGHT * 1.6f; 
            pos.y -= LINE_HEIGHT * 1.0;

            // === 5. TỔNG KẾT CHI TIẾT (Footer Logic) ===
            final float FONT_SIZE_SUMMARY = 10;
            final float SUMMARY_INDENT = MARGIN;
            final float SUMMARY_VALUE_COL = PAGE_WIDTH - MARGIN;
            
            // Helper class để vẽ dòng tổng kết
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
                    contentStream.beginText();
                    contentStream.setFont(currentFont, currentFontSize);
                    contentStream.newLineAtOffset(SUMMARY_INDENT, pos.y);
                    contentStream.showText(label);
                    contentStream.endText();
                    
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
            
            // 5.1. Vẽ các dòng chi tiết
            drawer.draw("Tổng cộng món ăn:", currencyFormatter.format(tongTienMonAn) + " VNĐ", true, false); 
            drawer.draw("Phí dịch vụ (5%):", currencyFormatter.format(phiDichVu) + " VNĐ", false, false);
            drawer.draw("Thuế VAT (8%):", currencyFormatter.format(thueVAT) + " VNĐ", false, false);
            drawer.draw("Tiền đặt cọc bàn:", "-" + currencyFormatter.format(tienCoc) + " VNĐ", false, false);
            
            // 🔥 In dòng khuyến mãi nếu có
            if (tienKhuyenMai > 0) {
                drawer.draw("Khuyến mãi/Ưu đãi:", "-" + currencyFormatter.format(tienKhuyenMai) + " VNĐ", false, false);
            }

            // 5.2. Đường kẻ phân chia
            pos.y += LINE_HEIGHT * 0.5f;
            contentStream.setLineWidth(0.5f);
            contentStream.moveTo(SUMMARY_INDENT, pos.y);
            contentStream.lineTo(SUMMARY_VALUE_COL, pos.y);
            contentStream.stroke();
            pos.y -= LINE_HEIGHT * 1.2f;

            // 5.3. Tổng thanh toán (Dùng biến đã tính chính xác)
            drawer.draw("Tổng thanh toán:", currencyFormatter.format(tongThanhToan) + " VNĐ", true, true);

            // Đường kẻ dưới tổng thanh toán
            pos.y -= LINE_HEIGHT * 0.5f;
            contentStream.moveTo(SUMMARY_INDENT, pos.y);
            contentStream.lineTo(SUMMARY_VALUE_COL, pos.y);
            contentStream.stroke();
            pos.y -= LINE_HEIGHT * 1.2f;
            
            // 5.4. Thông tin bổ sung & Số tiền khách trả
            String uuDaiStr = (tienKhuyenMai > 0) ? ("-" + currencyFormatter.format(tienKhuyenMai) + " VNĐ") : "0 VNĐ";
            String memberInfo = "Gold (giảm 10%)"; // Ví dụ tĩnh hoặc lấy từ object KhachHang nếu có logic thành viên

            drawer.draw("Hình thức thanh toán:", hinhThucTTStr, false, false);
            // drawer.draw("Khách hàng thành viên:", memberInfo, false, false); // Bỏ comment nếu muốn hiện
            drawer.draw("Ưu đãi áp dụng:", uuDaiStr, false, false);
            
            // Số tiền khách trả chính là Tổng thanh toán
            drawer.draw("Số tiền khách trả:", currencyFormatter.format(tongThanhToan) + " VNĐ", true, false); 
            
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
     * 🔥 HÀM PUBLIC HELPER: Dùng để cho phép các Controller Popup gọi hàm in private.
     */
    public PDDocument getReceiptDocument(HoaDon hd) throws IOException {
        // Gọi hàm private của DatBan
        return createReceiptPdf(hd); 
    }
    /**
     * 🔥 HÀM SỬA CUỐI CÙNG CHO MOMO: Mở Popup hiển thị QR Thanh toán (Sử dụng LOGO và BIN BVBank).
     * === ĐÃ THÊM: Tên tài khoản và Số tài khoản ===
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
        // === THÊM MỚI: TÊN TÀI KHOẢN ===
        final String YOUR_ACCOUNT_NAME = "MOMO_LECONGCHUNG"; // <<< THAY TÊN CHỦ TK CỦA BẠN VÀO ĐÂY

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
        
        // TẢI VÀ THAY THẾ LABEL BẰNG LOGO MOMO
        ImageView logoView = new ImageView();
        try {
            Image logoMomo = new Image(getClass().getResourceAsStream("/images/MoMo_Logo.png"));
            logoView.setImage(logoMomo);
            logoView.setFitHeight(40); 
            logoView.setPreserveRatio(true);
        } catch (Exception e) {
            System.err.println("Lỗi tải logo MoMo. Dùng Label thay thế.");
            return; 
        }

        Label lblAmount = new Label("Số tiền: " + String.format("%,.0f Đ", tongTienThanhToan));
        lblAmount.setStyle("-fx-font-size: 1.2em; -fx-font-weight: 500; -fx-text-fill: red;");
        
        // === THÊM MỚI: TÊN VÀ SỐ TÀI KHOẢN ===
        Label lblAccountName = new Label("Tên tài khoản: " + YOUR_ACCOUNT_NAME);
        lblAccountName.setStyle("-fx-font-size: 1.1em; -fx-font-weight: 500; -fx-text-fill: #333;");
        
        Label lblAccountNumber = new Label("Số tài khoản: " + YOUR_ACCOUNT_NUMBER);
        lblAccountNumber.setStyle("-fx-font-size: 1.1em; -fx-font-weight: 500; -fx-text-fill: #333;");
        // === KẾT THÚC THÊM MỚI ===
        
        Label lblContent = new Label("Nội dung: " + rawContent);
        lblContent.setStyle("-fx-font-size: 1.0em; -fx-font-weight: 400;");

        // Tạo nút Hủy và Xác nhận (Màu sắc MoMo)
        Button btnHuy = new Button("Hủy");
        Button btnXacNhanThanhToan = new Button("Xác nhận đã thanh toán");
        
        btnHuy.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-padding: 10px 20px; -fx-font-weight: bold;");
        btnXacNhanThanhToan.setStyle("-fx-background-color: #b0006d; -fx-text-fill: white; -fx-padding: 10px 20px; -fx-font-weight: bold;"); 

        HBox buttonBox = new HBox(15, btnHuy, btnXacNhanThanhToan);
        buttonBox.setAlignment(Pos.CENTER);

        // === SỬA LẠI LAYOUT: Thêm 2 label mới vào infoBox ===
        VBox infoBox = new VBox(8, lblAmount, lblAccountName, lblAccountNumber, lblContent);
        infoBox.setAlignment(Pos.CENTER);

        VBox root = new VBox(15, logoView, qrView, infoBox, buttonBox); // Giảm spacing
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);
        root.setPrefSize(400, 600); // Tăng chiều cao

        Stage popupStage = new Stage();
        popupStage.setTitle("Thanh toán MoMo/VietQR");
        popupStage.setScene(new Scene(root));
        
        // GÁN SỰ KIỆN CHO NÚT (Giữ nguyên)
        btnHuy.setOnAction(e -> popupStage.close());

        btnXacNhanThanhToan.setOnAction(e -> {
            // (Logic xác nhận thanh toán giữ nguyên)
            PTTThanhToan pttt = PTTThanhToan.VI_DIEN_TU; 
            String maHDCanThanhToan = currentHoaDon.getMaHD();
            String maNV = "NV_LOI"; // Mã mặc định nếu lỗi
            try {
                // Lấy tài khoản đang đăng nhập từ MainApp
                TaiKhoan tk = MainApp.getLoggedInUser();
                
                // Kiểm tra và lấy maNV (Giả định NhanVien entity có getMaNV())
                if (tk != null && tk.getNhanVien() != null && tk.getNhanVien().getMaNV() != null) { 
                    maNV = tk.getNhanVien().getMaNV(); 
                } else {
                     System.err.println("Lỗi thanh toán: Không tìm thấy maNV từ MainApp.getLoggedInUser()");
                     // Gán một mã NV mặc định hoặc báo lỗi tùy nghiệp vụ
                     maNV = "NV_DEFAULT"; 
                }
            } catch (Exception ex) {
                System.err.println("Lỗi nghiêm trọng khi lấy maNV đăng nhập: " + ex.getMessage());
                ex.printStackTrace();
            } 
            
            if (maHDCanThanhToan != null) {
                if (datBanDAO.thanhToanHoaDon(maHDCanThanhToan, pttt, maNV)) { 
                    HoaDon hdDaThanhToan = datBanDAO.getHoaDonByMaHD(maHDCanThanhToan);
                    if (hdDaThanhToan != null) {
                        currentHoaDon = hdDaThanhToan;
                        showAlert(Alert.AlertType.INFORMATION, "Thành công", 
                                  "Hóa đơn " + maHDCanThanhToan + " đã được thanh toán bằng " + pttt.getDisplayName());
                        
                        loadDsBan(); 
                        disableMiddleActionButtons();
                        disablePaymentButtons(); 
                        loadBookingCards(); 
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
 // [Thêm hàm mới này vào file DatBan.java]

    /**
     * 🔥 HÀM MỚI: Mở Popup hiển thị QR Thanh toán Tiền Cọc.
     * Chỉ hoạt động khi Hóa đơn đang ở trạng thái "Chờ xác nhận".
     */
    private void openThanhToanCocPopup() {
        // 1. Kiểm tra hóa đơn và trạng thái
        if (currentHoaDon == null || currentHoaDon.getMaHD() == null) {
            showAlert(Alert.AlertType.WARNING, "Lỗi", "Vui lòng chọn một Hóa đơn để thanh toán cọc.");
            return;
        }
        if (currentHoaDon.getTrangThai() != TrangThaiHoaDon.CHO_XAC_NHAN) {
             showAlert(Alert.AlertType.WARNING, "Lỗi", "Chỉ có thể thanh toán cọc cho Hóa đơn đang ở trạng thái 'Chờ xác nhận'.");
             return;
        }

        // 2. Lấy số tiền cọc từ TextField
        double tienCoc = 0;
        try {
            String tienCocRaw = txtTienCoc.getText().replaceAll("[^0-9.]", "");
            tienCoc = Double.parseDouble(tienCocRaw.isEmpty() ? "0" : tienCocRaw);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Số tiền cọc không hợp lệ.");
            return;
        }

        if (tienCoc <= 0) {
            showAlert(Alert.AlertType.WARNING, "Lỗi", "Số tiền cọc phải lớn hơn 0.");
            return;
        }

        // 3. Thông tin QR (Giống Ngân hàng, chỉ khác nội dung)
        final String YOUR_BANK_CODE = "970422"; // MB Bank BIN (Ví dụ)
        final String YOUR_ACCOUNT_NUMBER = "0927432020905"; // Số tài khoản của bạn
        String maHD = currentHoaDon.getMaHD();

        // Nội dung: Rõ ràng là thanh toán cọc
        String rawContent = "Coc" + maHD.toUpperCase().replace(" ", "_"); // VD: CocHD079
        String encodedContent;
        try {
            encodedContent = java.net.URLEncoder.encode(rawContent, "UTF-8");
        } catch (java.io.UnsupportedEncodingException e) {
            encodedContent = rawContent;
        }

        // 4. Tạo URL Quicklink
        String qrUrl = String.format(
            "https://img.vietqr.io/image/%s-%s-compact.png?amount=%d&addInfo=%s",
            YOUR_BANK_CODE,
            YOUR_ACCOUNT_NUMBER,
            (int) Math.ceil(tienCoc), // Số tiền cọc
            encodedContent
        );

        // 5. Tải ảnh QR
        Image qrImage = generateQrCodeImageFromUrl(qrUrl, 250);
        if (qrImage == null || qrImage.isError()) {
            showAlert(Alert.AlertType.ERROR, "Lỗi kết nối", "Không thể tải mã QR VietQR. Vui lòng kiểm tra lại kết nối.");
            return;
        }

        // 6. Tạo giao diện Popup (Tương tự Ngân hàng)
        ImageView qrView = new ImageView(qrImage);
        qrView.setFitWidth(250); qrView.setFitHeight(250);

        Label lblTitle = new Label("Quét Mã Để Thanh Toán Tiền Cọc"); // Sửa tiêu đề
        lblTitle.setStyle("-fx-font-size: 1.5em; -fx-font-weight: bold;");

        Label lblAmount = new Label("Số tiền cọc: " + String.format("%,.0f Đ", tienCoc)); // Sửa text
        lblAmount.setStyle("-fx-font-size: 1.2em; -fx-font-weight: 500; -fx-text-fill: red;");

        Label lblContent = new Label("Nội dung: " + rawContent);
        lblContent.setStyle("-fx-font-size: 1.0em; -fx-font-weight: 400;");

        Button btnHuy = new Button("Hủy");
        Button btnXacNhanCoc = new Button("Xác nhận đã cọc"); // Sửa text nút

        btnHuy.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-padding: 10px 20px; -fx-font-weight: bold;");
        btnXacNhanCoc.setStyle("-fx-background-color: #ffc107; -fx-text-fill: black; -fx-padding: 10px 20px; -fx-font-weight: bold;"); // Màu vàng cho cọc

        HBox buttonBox = new HBox(15, btnHuy, btnXacNhanCoc);
        buttonBox.setAlignment(Pos.CENTER);

        VBox root = new VBox(20, lblTitle, qrView, lblAmount, lblContent, buttonBox);
        root.setPadding(new Insets(20)); root.setAlignment(Pos.CENTER); root.setPrefSize(400, 550);

        Stage popupStage = new Stage();
        popupStage.setTitle("Thanh toán tiền cọc cho HD: " + maHD); // Sửa tiêu đề cửa sổ
        popupStage.setScene(new Scene(root));

        // 7. GÁN SỰ KIỆN CHO NÚT
        btnHuy.setOnAction(e -> popupStage.close());

        btnXacNhanCoc.setOnAction(e -> {
            String maHDCanXacNhan = currentHoaDon.getMaHD();

            try {
                // 🔥 GỌI DAO ĐỂ CẬP NHẬT TRẠNG THÁI TỪ "ChoXacNhan" -> "Dat"
                if (datBanDAO.xacNhanTienCoc(maHDCanXacNhan)) { // <<< Gọi hàm DAO mới

                    // Tải lại HĐ để lấy trạng thái mới
                    HoaDon hdDaDat = datBanDAO.getHoaDonByMaHD(maHDCanXacNhan);
                    if (hdDaDat != null) {
                        currentHoaDon = hdDaDat; // Cập nhật HĐ hiện tại
                        this.daThanhToanCoc = true;
                        showAlert(Alert.AlertType.INFORMATION, "Thành công",
                                  "Đã xác nhận thanh toán cọc cho Hóa đơn " + maHDCanXacNhan + ".\nTrạng thái chuyển thành: Đã đặt.");

                        // Cập nhật giao diện chính
                        loadBookingCards(); // Cập nhật card list
                        loadTableGrids();   // Cập nhật màu bàn
                        // Cập nhật lại form nếu cần (trạng thái HĐ)
                        loadHoaDonToMainInterface(currentHoaDon); // Tải lại để cập nhật nút

                    } else {
                         showAlert(Alert.AlertType.ERROR, "Lỗi", "Đã xác nhận cọc, nhưng không tải lại được hóa đơn.");
                    }
                } else {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể cập nhật trạng thái hóa đơn (có thể HĐ không ở trạng thái 'Chờ xác nhận').");
                }
            } catch(SQLException sqlEx) {
                 showAlert(Alert.AlertType.ERROR, "Lỗi CSDL", "Lỗi khi xác nhận tiền cọc: " + sqlEx.getMessage());
                 sqlEx.printStackTrace();
            }

            popupStage.close();
        });

        popupStage.show();
    }
    // 🔥 HÀM loadDsBan() TẠI VỊ TRÍ CŨ (giữ nguyên nếu bạn có)

    public void loadDsBan() {
        // Gọi lại hàm vẽ lưới bàn của bạn
        loadTableGrids();
        System.out.println("LOG UI: Đã tải lại danh sách Bàn (Cập nhật trạng thái bàn).");
    }
    /**
     * 🔥 HÀM SỬA CUỐI CÙNG: Mở Popup hiển thị QR Thanh toán Ngân hàng.
     * === ĐÃ THÊM: Tên tài khoản và Số tài khoản ===
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
        // === THÊM MỚI: TÊN TÀI KHOẢN ===
        final String YOUR_ACCOUNT_NAME = "LÊ CÔNG CHUNG"; // <<< THAY TÊN CHỦ TK CỦA BẠN VÀO ĐÂY
        
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
        
        // === THÊM MỚI: TÊN VÀ SỐ TÀI KHOẢN ===
        Label lblAccountName = new Label("Tên tài khoản: " + YOUR_ACCOUNT_NAME);
        lblAccountName.setStyle("-fx-font-size: 1.1em; -fx-font-weight: 500; -fx-text-fill: #333;");
        
        Label lblAccountNumber = new Label("Số tài khoản: " + YOUR_ACCOUNT_NUMBER);
        lblAccountNumber.setStyle("-fx-font-size: 1.1em; -fx-font-weight: 500; -fx-text-fill: #333;");
        // === KẾT THÚC THÊM MỚI ===
        
        Label lblContent = new Label("Nội dung: " + rawContent);
        lblContent.setStyle("-fx-font-size: 1.0em; -fx-font-weight: 400;");

        // TẠO NÚT HỦY VÀ XÁC NHẬN VÀ ÁP DỤNG MÀU SẮC
        Button btnHuy = new Button("Hủy");
        Button btnXacNhanThanhToan = new Button("Xác nhận đã thanh toán");
        
        btnHuy.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-padding: 10px 20px; -fx-font-weight: bold;");
        btnXacNhanThanhToan.setStyle("-fx-background-color: #28a745; -fx-text-fill: white; -fx-padding: 10px 20px; -fx-font-weight: bold;");

        HBox buttonBox = new HBox(15, btnHuy, btnXacNhanThanhToan);
        buttonBox.setAlignment(Pos.CENTER);

        // === SỬA LẠI LAYOUT: Thêm 2 label mới vào infoBox ===
        VBox infoBox = new VBox(8, lblAmount, lblAccountName, lblAccountNumber, lblContent);
        infoBox.setAlignment(Pos.CENTER);

        VBox root = new VBox(15, lblTitle, qrView, infoBox, buttonBox); // Giảm spacing
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);
        root.setPrefSize(400, 600); // Tăng chiều cao 1 chút

        Stage popupStage = new Stage();
        popupStage.setTitle("Thanh toán Ngân hàng");
        popupStage.setScene(new Scene(root));
        
        // 6. GÁN SỰ KIỆN CHO NÚT (Giữ nguyên)
        btnHuy.setOnAction(e -> popupStage.close());

        btnXacNhanThanhToan.setOnAction(e -> {
            // (Logic xác nhận thanh toán giữ nguyên)
            PTTThanhToan pttt = PTTThanhToan.NGAN_HANG; 
            String maHDCanThanhToan = currentHoaDon.getMaHD();
            String maNV = "NV_LOI"; // Mã mặc định nếu lỗi
            try {
                // Lấy tài khoản đang đăng nhập từ MainApp
                TaiKhoan tk = MainApp.getLoggedInUser();
                
                // Kiểm tra và lấy maNV (Giả định NhanVien entity có getMaNV())
                if (tk != null && tk.getNhanVien() != null && tk.getNhanVien().getMaNV() != null) { 
                    maNV = tk.getNhanVien().getMaNV(); 
                } else {
                     System.err.println("Lỗi thanh toán: Không tìm thấy maNV từ MainApp.getLoggedInUser()");
                     // Gán một mã NV mặc định hoặc báo lỗi tùy nghiệp vụ
                     maNV = "NV_DEFAULT"; 
                }
            } catch (Exception ex) {
                System.err.println("Lỗi nghiêm trọng khi lấy maNV đăng nhập: " + ex.getMessage());
                ex.printStackTrace();
            } 
            
            if (maHDCanThanhToan != null) {
                if (datBanDAO.thanhToanHoaDon(maHDCanThanhToan, pttt, maNV)) { 
                    HoaDon hdDaThanhToan = datBanDAO.getHoaDonByMaHD(maHDCanThanhToan);
                    if (hdDaThanhToan != null) {
                        currentHoaDon = hdDaThanhToan;
                        showAlert(Alert.AlertType.INFORMATION, "Thành công", 
                                  "Hóa đơn " + maHDCanThanhToan + " đã được thanh toán bằng " + pttt.getDisplayName());
                        
                        loadDsBan(); 
                        disableMiddleActionButtons();
                        disablePaymentButtons(); 
                        loadBookingCards(); 
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
        btnXacNhan.setMaxWidth(Double.MAX_VALUE); 
        btnXacNhan.setStyle("-fx-background-color: #ff9900; -fx-text-fill: white; -fx-font-size: 1.2em; -fx-font-weight: bold; -fx-padding: 12px 0; -fx-background-radius: 5px; -fx-border-radius: 5px;");
        
        txtTienTraLai.setEditable(false);
        txtTienKhachDua.setAlignment(Pos.CENTER_RIGHT);
        txtTienTraLai.setAlignment(Pos.CENTER_RIGHT);

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
        
        // Xử lý nút preset 
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

        // Xử lý nhập tay 
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
            String maNV = "NV_LOI"; // Mã mặc định nếu lỗi
            try {
                // Lấy tài khoản đang đăng nhập từ MainApp
                TaiKhoan tk = MainApp.getLoggedInUser();
                
                // Kiểm tra và lấy maNV (Giả định NhanVien entity có getMaNV())
                if (tk != null && tk.getNhanVien() != null && tk.getNhanVien().getMaNV() != null) { 
                    maNV = tk.getNhanVien().getMaNV(); 
                } else {
                     System.err.println("Lỗi thanh toán: Không tìm thấy maNV từ MainApp.getLoggedInUser()");
                     // Gán một mã NV mặc định hoặc báo lỗi tùy nghiệp vụ
                     maNV = "NV_DEFAULT"; 
                }
            } catch (Exception ex) {
                System.err.println("Lỗi nghiêm trọng khi lấy maNV đăng nhập: " + ex.getMessage());
                ex.printStackTrace();
            }
            
            
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
     */
    private void handleLuuDatHang() {
        if (selectedBanList.isEmpty()) {
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
                showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng nhập Số điện thoại.");
                return; 
            }
            KhachHang khachHang = datBanDAO.timHoacTaoKhachHang(sdt, tenKH);
            LocalDate ngayDen = datePickerThoiGianDen.getValue(); 
            
            // 🔥 SỬA: Lấy giờ từ ComboBox
            String gioDenStr = comboThoiGian.getValue();
            if (gioDenStr == null || gioDenStr.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng chọn Giờ đến.");
                return; 
            }
            LocalTime gioDen = LocalTime.parse(gioDenStr, timeFormatter);
            
            java.time.LocalDateTime thoiGianDen = ngayDen.atTime(gioDen);

            double tongTienCoc = 0;
            try {
                 String tienCocRaw = txtTienCoc.getText().replaceAll("[^0-9.]", "");
                 tongTienCoc = Double.parseDouble(tienCocRaw.isEmpty() ? "0" : tienCocRaw);
            } catch (NumberFormatException e) { }

            // --- XÁC ĐỊNH TRẠNG THÁI BAN ĐẦU ---
            String trangThaiBanDau;
            boolean coCocVaChuaThanhToan = (tongTienCoc > 0 && !this.daThanhToanCoc);

            if (coCocVaChuaThanhToan) {
                trangThaiBanDau = TrangThaiHoaDon.CHO_XAC_NHAN.getDbValue();
            } else {
                if (thoiGianDen.isBefore(java.time.LocalDateTime.now().plusMinutes(15))) {
                    trangThaiBanDau = TrangThaiHoaDon.DANG_SU_DUNG.getDbValue();
                } else {
                    trangThaiBanDau = TrangThaiHoaDon.DAT.getDbValue();
                }
            }

            // 2. TÁCH BÀN GỐC VÀ BÀN PHỤ
            Ban banGoc = selectedBanList.get(0);
            List<Ban> banPhuList = new ArrayList<>(selectedBanList.subList(1, selectedBanList.size()));

            // 3. TẠO HÓA ĐƠN GỐC
            HoaDon hoaDonGoc = new HoaDon();
            hoaDonGoc.setNgayLap(java.time.LocalDateTime.now());
            hoaDonGoc.setGioVao(thoiGianDen);
            hoaDonGoc.setKhachHang(khachHang);
            hoaDonGoc.setBan(banGoc);
            hoaDonGoc.setTrangThai(trangThaiBanDau);
            hoaDonGoc.setTienCoc(tongTienCoc);
            hoaDonGoc.setMaHDGoc(null);

            datBanDAO.luuHoaDonVaChiTiet(hoaDonGoc, monOrderList);
            datBanDAO.capNhatTrangThaiBan(banGoc.getMaBan(), getTrangThaiBanFromHoaDon(trangThaiBanDau));

            // 4. TẠO CÁC HÓA ĐƠN PHỤ
            for (Ban banPhu : banPhuList) {
                HoaDon hoaDonPhu = new HoaDon();
                hoaDonPhu.setNgayLap(java.time.LocalDateTime.now());
                hoaDonPhu.setGioVao(thoiGianDen);
                hoaDonPhu.setKhachHang(khachHang);
                hoaDonPhu.setBan(banPhu);
                hoaDonPhu.setTienCoc(0);
                hoaDonPhu.setMaHDGoc(hoaDonGoc.getMaHD());
                hoaDonPhu.setTrangThai(TrangThaiHoaDon.HOA_DON_TAM.getDbValue());

                datBanDAO.luuHoaDonVaChiTiet(hoaDonPhu, FXCollections.observableArrayList());
                datBanDAO.capNhatTrangThaiBan(banPhu.getMaBan(), getTrangThaiBanFromHoaDon(trangThaiBanDau));
            }

            // 5. HOÀN TẤT
            showAlert(Alert.AlertType.INFORMATION, "Thành công",
                String.format("Đã lưu đặt hàng thành công!\nTrạng thái: %s\n- HĐ Gốc: %s",
                              TrangThaiHoaDon.fromDbValue(trangThaiBanDau).getDisplayName(),
                              hoaDonGoc.getMaHD()));

            isBookingConfirmed = false;
            this.daThanhToanCoc = false;
            clearFormDatBan();
            loadBookingCards();
            loadTableGrids();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi hệ thống", "Lỗi khi lưu đặt hàng: " + e.getMessage());
        }
    }
    private String getTrangThaiBanFromHoaDon(String trangThaiHoaDonDbValue) {
        TrangThaiHoaDon ttHD = TrangThaiHoaDon.fromDbValue(trangThaiHoaDonDbValue);
        if (ttHD == TrangThaiHoaDon.DANG_SU_DUNG) {
            return TrangThaiBan.DANG_SU_DUNG.getDbValue(); // Bàn đang sử dụng
        } else {
            // Bao gồm DAT, CHO_XAC_NHAN (và cả HOA_DON_TAM nếu có bàn)
            return TrangThaiBan.DA_DAT.getDbValue(); // Bàn đã đặt (hoặc chờ cọc)
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
     * 🔥 ĐÃ SỬA: Coi trạng thái CHO_XAC_NHAN tương đương như DAT (Đã đặt - màu đỏ).
     */
    public TrangThaiBan getTrangThaiHienThi(Ban banGoc, LocalTime thoiGianKiemTra) {
        // 1. Tìm HĐ đang hoạt động sớm nhất cho bàn này
        Optional<HoaDon> hdDangCho = dsHoaDonDatTrongNgay.stream()
            .filter(hd -> hd.getBan() != null && hd.getBan().getMaBan().equals(banGoc.getMaBan()) && hd.getGioVao() != null)
            // Lọc các trạng thái giữ bàn
            .filter(hd -> hd.getTrangThai() != null &&
                          (hd.getTrangThai() == TrangThaiHoaDon.DAT ||
                           hd.getTrangThai() == TrangThaiHoaDon.DANG_SU_DUNG ||
                           hd.getTrangThai() == TrangThaiHoaDon.HOA_DON_TAM ||
                           hd.getTrangThai() == TrangThaiHoaDon.CHO_XAC_NHAN)) // <<< THÊM MỚI
            .min(Comparator.comparing(HoaDon::getGioVao));

        if (hdDangCho.isPresent()) {
            TrangThaiHoaDon trangThaiHD = hdDangCho.get().getTrangThai();

            // 2. Ưu tiên CAM (DANG_SU_DUNG / HOA_DON_TAM)
            if (trangThaiHD == TrangThaiHoaDon.DANG_SU_DUNG || trangThaiHD == TrangThaiHoaDon.HOA_DON_TAM) {
                return TrangThaiBan.DANG_SU_DUNG; // Cam
            }

            // 3. Xử lý ĐỎ (ĐÃ ĐẶT - DAT hoặc Chờ xác nhận - CHO_XAC_NHAN)
            if (trangThaiHD == TrangThaiHoaDon.DAT || trangThaiHD == TrangThaiHoaDon.CHO_XAC_NHAN) { // <<< THÊM MỚI
                LocalTime gioVao = hdDangCho.get().getGioVao().toLocalTime();
                LocalTime gioCanhBaoSom = gioVao.minusHours(4); // Mốc 4 tiếng

                // Case 1: Thời gian tìm kiếm ĐÃ ĐẾN hoặc QUA giờ vào -> Cam
                if (thoiGianKiemTra.isAfter(gioVao) || thoiGianKiemTra.equals(gioVao)) {
                     return TrangThaiBan.DANG_SU_DUNG;
                }

                // Case 2: Thời gian tìm kiếm TRƯỚC giờ vào (Kiểm tra 4 tiếng cứng) -> Đỏ
                if (thoiGianKiemTra.isAfter(gioCanhBaoSom) || thoiGianKiemTra.equals(gioCanhBaoSom)) {
                    return TrangThaiBan.DA_DAT; // Đỏ
                }
                // Nếu trước 4 tiếng -> Trống (sẽ rơi vào fallback)
            }
        }

        // 4. Fallback: Trống (Trắng)
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
            String gioStr = comboThoiGian.getValue();
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
            String searchQuery = txtSearch.getText().trim().toLowerCase(); // Lấy và chuẩn hóa query

            // 3. ÁP DỤNG FILTER VÀ SEARCH
            List<HoaDon> filteredList = allPendingBookings.stream()
                // Lọc bỏ Hóa đơn Tạm (Giữ nguyên)
                .filter(hd -> hd.getTrangThai() != null && !hd.getTrangThai().getDbValue().equals(TrangThaiHoaDon.HOA_DON_TAM.getDbValue()))
                // Lọc theo Trạng thái (Giữ nguyên)
                .filter(hd -> {
                    boolean statusMatch = true;
                    if (selectedStatus != null && !selectedStatus.equals("Tất cả")) {
                        // ... (logic lọc status giữ nguyên) ...
                        String dbStatusValue = "";
                        TrangThaiHoaDon selectedEnum = TrangThaiHoaDon.fromDisplayName(selectedStatus); // Chuyển đổi display name sang enum
                        if (selectedEnum != null) {
                            dbStatusValue = selectedEnum.getDbValue();
                        }

                        // Bao gồm cả HĐ Tạm nếu filter là "Đang phục vụ"
                        if (selectedStatus.equals(TrangThaiHoaDon.DANG_SU_DUNG.getDisplayName())) {
                             statusMatch = hd.getTrangThai() != null &&
                                           (hd.getTrangThai() == TrangThaiHoaDon.DANG_SU_DUNG ||
                                            hd.getTrangThai() == TrangThaiHoaDon.HOA_DON_TAM);
                        } else if (!dbStatusValue.isEmpty()) {
                             statusMatch = hd.getTrangThai() != null && hd.getTrangThai().getDbValue().equals(dbStatusValue);
                        } else {
                            statusMatch = false; // Không tìm thấy trạng thái hợp lệ
                        }
                    }
                    return statusMatch;
                })
                // --- 🔥 LỌC THEO TÌM KIẾM (SĐT, Mã HĐ, Mã Bàn) ---
                .filter(hd -> {
                    if (searchQuery.isEmpty()) {
                        return true; // Nếu ô tìm kiếm trống thì không lọc gì cả
                    }

                    // Kiểm tra SĐT (khách hàng có thể null)
                    boolean sdtMatch = hd.getKhachHang() != null &&
                                       hd.getKhachHang().getSoDT() != null &&
                                       hd.getKhachHang().getSoDT().toLowerCase().contains(searchQuery);

                    // Kiểm tra Mã HĐ
                    boolean maHdMatch = hd.getMaHD() != null &&
                                        hd.getMaHD().toLowerCase().contains(searchQuery);

                    // Kiểm tra Mã Bàn (bàn có thể null)
                    // Cần kiểm tra cả các bàn phụ nếu là HĐ Gốc
                    boolean maBanMatch = false;
                    List<HoaDon> relatedHDs = new ArrayList<>();
                    relatedHDs.add(hd); // Add chính nó
                    if (hd.getMaHDGoc() == null) { // Nếu là HĐ Gốc, tìm HĐ Phụ
                        relatedHDs.addAll(datBanDAO.getHoaDonPhuByMaHDGoc(hd.getMaHD()));
                    }
                    // Kiểm tra mã bàn trên tất cả HĐ liên quan
                    for (HoaDon relatedHd : relatedHDs) {
                        if (relatedHd.getBan() != null && relatedHd.getBan().getMaBan() != null &&
                            relatedHd.getBan().getMaBan().toLowerCase().contains(searchQuery)) {
                            maBanMatch = true;
                            break; // Chỉ cần tìm thấy 1 bàn khớp là đủ
                        }
                    }

                    // Trả về true nếu khớp bất kỳ trường nào
                    return sdtMatch || maHdMatch || maBanMatch;
                })
                // ---------------------------------------------
                .collect(Collectors.toList());

            // 4. Tải các đơn trong ngày (CHO LOGIC SƠ ĐỒ BÀN) - Giữ nguyên
            this.dsHoaDonDatTrongNgay = datBanDAO.getDsDatBanHomNay(dateToLoadForGrid);

            // 5. Hiển thị danh sách ĐÃ LỌC (Giữ nguyên)
            System.out.println("\n*** LOG: Tải thành công " + filteredList.size() + " đơn ĐANG CHỜ (đã lọc) ***");
            if (filteredList.isEmpty()) {
                // ... (hiển thị thông báo không tìm thấy) ...
                 Label lbl = new Label("Không tìm thấy đơn nào khớp."); lbl.setPadding(new Insets(10)); vboxBookingCards.getChildren().add(lbl);
            } else {
                // Chỉ hiển thị HĐ Gốc (Giữ nguyên)
                List<HoaDon> hdGocFilter = filteredList.stream()
                                            .filter(hd -> hd.getMaHDGoc() == null)
                                            .collect(Collectors.toList());
                for (HoaDon hd : hdGocFilter) {
                    VBox card = createBookingCard(hd);
                    card.setOnMouseClicked(e -> loadHoaDonToMainInterface(hd));
                    vboxBookingCards.getChildren().add(card);
                }
            }
        } catch (Exception e) {
            // ... (xử lý lỗi) ...
             System.err.println("Lỗi tải danh sách đặt bàn: " + e.getMessage()); Label lbl = new Label("LỖI TẢI DỮ LIỆU: " + e.getMessage()); lbl.setPadding(new Insets(10)); vboxBookingCards.getChildren().add(lbl);
        }
    }

 // (Bên dưới hàm handleSelectBookingCard)

    /**
     * 🔥 HÀM LOAD HÓA ĐƠN: Đã cập nhật để set giờ vào ComboBox
     */
    public void loadHoaDonToMainInterface(HoaDon hd) {
        System.out.println("LOG: Đang tải Hóa đơn " + (hd.getMaHD() != null ? hd.getMaHD() : "Mới"));

        // === BƯỚC 1: RESET MÀU CŨ ===
        if (!currentHoaDonGocVaPhu.isEmpty()) {
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

        // 1. Set hóa đơn hiện tại
        this.currentHoaDon = hd; 

        // 2. KIỂM TRA GỐC/PHỤ
        if (hd.getMaHDGoc() != null) { 
            HoaDon hdGoc = datBanDAO.getHoaDonByMaHD(hd.getMaHDGoc());
            if (hdGoc != null) this.currentHoaDon = hdGoc;
        }

        // 3. TÌM TẤT CẢ HĐ PHỤ
        currentHoaDonGocVaPhu.clear(); 
        currentHoaDonGocVaPhu.add(this.currentHoaDon); 
        List<HoaDon> hoaDonPhu = datBanDAO.getHoaDonPhuByMaHDGoc(this.currentHoaDon.getMaHD()); 
        currentHoaDonGocVaPhu.addAll(hoaDonPhu); 
        
        // 4. TẢI MÓN ĂN
        monOrderList.clear(); 
        if (this.currentHoaDon.getMaHD() != null) {
            ObservableList<MonOrder> chiTiet = datBanDAO.getChiTietHoaDon(this.currentHoaDon.getMaHD());
            monOrderList.addAll(chiTiet);
        }
     // ---------------------------------------------------------
        // 🔥 ĐOẠN LOGIC MỚI: LOAD ƯU ĐÃI TỪ HÓA ĐƠN LÊN COMBOBOX
        // ---------------------------------------------------------
        // Mặc định là null
        this.selectedUuDai = null; 
        
        if (currentHoaDon.getMaUuDai() != null) {
            // Tìm ưu đãi trong danh sách đang load (dsUuDaiDangApDung) khớp với Mã Ưu Đãi của HĐ
            UuDai uuDaiDaChon = dsUuDaiDangApDung.stream()
                .filter(ud -> ud.getMaUuDai().equals(currentHoaDon.getMaUuDai()))
                .findFirst()
                .orElse(null);

            if (uuDaiDaChon != null) {
                this.selectedUuDai = uuDaiDaChon;
                // Tạo chuỗi hiển thị giống format trong loadPromoComboBox
                String displayString = String.format("%s (Giảm %.0f%%)", uuDaiDaChon.getTenUuDai(), uuDaiDaChon.getGiaTri());
                promoComboBox.setValue(displayString);
            } else {
                // Trường hợp ưu đãi cũ đã hết hạn hoặc bị xóa -> Reset
                promoComboBox.getSelectionModel().selectFirst(); 
            }
        } else {
            // Nếu HĐ chưa có ưu đãi -> Chọn "Không áp dụng"
            promoComboBox.getSelectionModel().selectFirst();
        }
        tblMonDaChon.refresh(); 
        calculateTotal();       

        // 5. HIỂN THỊ THÔNG TIN (MIDDLE PANEL)
         if (currentHoaDon.getKhachHang() != null) {
            txtTenKhachHang.setText(currentHoaDon.getKhachHang().getTenKH());
            txtSoDienThoai.setText(currentHoaDon.getKhachHang().getSoDT());
        } else {
            txtTenKhachHang.clear();
            txtSoDienThoai.clear();
        }
        
        // 🔥 SỬA: Hiển thị thời gian lên ComboBox và DatePicker
        if (currentHoaDon.getGioVao() != null) {
            datePickerThoiGianDen.setValue(currentHoaDon.getGioVao().toLocalDate());
            
            // Format giờ từ DB thành chuỗi (ví dụ "18:00")
            String gioDB = currentHoaDon.getGioVao().toLocalTime().format(timeFormatter);
            // Set giá trị cho ComboBox (nó sẽ hiển thị ngay cả khi không nằm trong list options)
            comboThoiGian.setValue(gioDB); 
        } else {
            datePickerThoiGianDen.setValue(LocalDate.now());
            // Mặc định chọn giờ đầu tiên hoặc giờ hiện tại
            comboThoiGian.getSelectionModel().selectFirst();
        }
        
        txtTienCoc.setText(String.format("%,.0f", currentHoaDon.getTienCoc()));
        
        // 6. Populate Label Bàn
        String tatCaBan = currentHoaDonGocVaPhu.stream() 
            .map(h -> (h.getBan() != null) ? h.getBan().getMaBan() : "N/A")
            .collect(Collectors.joining(", "));

        lblBanDangChon.setText(tatCaBan); 
        lblTrangThaiBan.setText(String.format("Đang xem %d bàn", currentHoaDonGocVaPhu.size())); 

        if (currentHoaDon.getBan() != null) { 
            txtSoLuongKhach.setText(String.valueOf(currentHoaDon.getBan().getSucChua()));
        }

        txtYeuCau.clear(); 
        
        // Update ComboBox Trạng Thái HĐ
        if (comboTrangThaiHienTai != null) {
            String dbVal = currentHoaDon.getTrangThai().getDbValue();
            String displayStatus;
            if (dbVal.equals(TrangThaiHoaDon.DANG_SU_DUNG.getDbValue())) displayStatus = "Đã nhận bàn";
            else if (dbVal.equals(TrangThaiHoaDon.DAT.getDbValue())) displayStatus = "Đã đặt";
            else if (dbVal.equals(TrangThaiHoaDon.CHO_XAC_NHAN.getDbValue())) displayStatus = "Chờ xác nhận";
            else displayStatus = "";
            comboTrangThaiHienTai.setValue(displayStatus);
        }

        // 7. TÔ MÀU BÀN TRÊN SƠ ĐỒ
        TrangThaiHoaDon trangThaiHdGoc = TrangThaiHoaDon.fromDbValue(currentHoaDon.getTrangThai().getDbValue());
        TrangThaiBan trangThaiCanTo = (trangThaiHdGoc == TrangThaiHoaDon.DANG_SU_DUNG) 
                                      ? TrangThaiBan.DANG_SU_DUNG : TrangThaiBan.DA_DAT;

        for (HoaDon hoadon : currentHoaDonGocVaPhu) {
            if (hoadon.getBan() != null) {
                Button btn = tableButtonMap.get(hoadon.getBan().getMaBan()); 
                if (btn != null) applyTableStyle(btn, trangThaiCanTo); 
            }
        }

        // 8. Cập nhật nút
        isBookingConfirmed = true; 
        updateButtonVisibility(true); 
        if (btnThanhToanCoc != null) {
            boolean enableCocButton = (currentHoaDon != null && currentHoaDon.getTrangThai() == TrangThaiHoaDon.CHO_XAC_NHAN);
            btnThanhToanCoc.setDisable(!enableCocButton);
        }

        if (vboxReceipt != null) vboxReceipt.setVisible(false);
    }
    /**
     * Tạo thẻ booking hiển thị bên trái với nút chức năng thông minh.
     */
    private VBox createBookingCard(HoaDon hd) {
        // 1. Khởi tạo Card
        VBox card = new VBox(8);
        card.getStyleClass().add("booking-card");
        card.setPadding(new Insets(15));

        // 2. Lấy dữ liệu từ Hóa đơn
        String maGiaoDich = hd.getMaHD();
        
        // Xử lý trạng thái hiển thị tiếng Việt
        String trangThaiDb = hd.getTrangThai() != null ? hd.getTrangThai().getDbValue() : "Unknown";
        String trangThaiViet = switch (trangThaiDb) {
            case "Dat" -> "Đã đặt";
            case "DangSuDung" -> "Đã nhận bàn"; // Tên mới theo yêu cầu
            case "HoaDonTam" -> "Đang tạo...";
            case "ChoXacNhan" -> "Chờ xác nhận";
            default -> trangThaiDb;
        };
        
        String gioVao = (hd.getGioVao() != null) ? hd.getGioVao().toLocalTime().format(timeFormatter) : "N/A";
        String sdtKhach = (hd.getKhachHang() != null && hd.getKhachHang().getSoDT() != null) ? hd.getKhachHang().getSoDT() : "N/A";

        // 3. Logic tìm tên bàn (Bao gồm cả bàn của HĐ Phụ)
        String danhSachBanDayDu = "N/A";
        try {
             List<HoaDon> allRelatedHDs = new ArrayList<>(); 
             String maHDGocDeTim;
             
             // Xác định HĐ Gốc
             if (hd.getMaHDGoc() == null) { 
                 maHDGocDeTim = hd.getMaHD(); 
                 allRelatedHDs.add(hd); 
             } else { 
                 maHDGocDeTim = hd.getMaHDGoc(); 
                 HoaDon hdGocTimDuoc = datBanDAO.getHoaDonByMaHD(maHDGocDeTim); 
                 if (hdGocTimDuoc != null) { allRelatedHDs.add(hdGocTimDuoc); } 
             }
             
             // Tìm các HĐ Phụ
             List<HoaDon> hdPhuList = datBanDAO.getHoaDonPhuByMaHDGoc(maHDGocDeTim); 
             for(HoaDon hp : hdPhuList){ 
                 if(!allRelatedHDs.stream().anyMatch(h -> h.getMaHD().equals(hp.getMaHD()))){ 
                     allRelatedHDs.add(hp); 
                 } 
             }
             
             // Gom danh sách mã bàn
             Set<String> uniqueBanSet = allRelatedHDs.stream()
                 .filter(h -> h.getBan() != null)
                 .map(h -> h.getBan().getMaBan())
                 .collect(Collectors.toSet());
                 
             danhSachBanDayDu = uniqueBanSet.isEmpty() ? "N/A" : String.join(", ", new TreeSet<>(uniqueBanSet));
             
        } catch (Exception e) { 
            Ban banHienTai = hd.getBan(); 
            danhSachBanDayDu = (banHienTai != null ? banHienTai.getMaBan() : "Lỗi"); 
        }

        // 4. Tạo các Label hiển thị
        Label lblMaHD = new Label(maGiaoDich != null ? maGiaoDich : "Mã: N/A");
        lblMaHD.getStyleClass().add("booking-card-id");
        
        Label lblSDT = new Label("SĐT: " + sdtKhach);
        
        Label lblTrangThai = new Label("Trạng thái: " + trangThaiViet);
        lblTrangThai.getStyleClass().add("booking-status-" + trangThaiDb.toLowerCase()); // CSS class theo trạng thái
        
        Label lblThoiGian = new Label("Giờ: " + gioVao);
        Label lblBan = new Label("Bàn: " + danhSachBanDayDu);

        // 5. 🔥 TẠO NÚT "SMART ACTION" 🔥
        Button btnSmartAction = new Button();
        btnSmartAction.setMaxWidth(Double.MAX_VALUE);
        btnSmartAction.setPrefHeight(40);
        btnSmartAction.setStyle("-fx-font-weight: bold; -fx-cursor: hand;");

        // --- CẤU HÌNH NÚT THEO TRẠNG THÁI ---
        TrangThaiHoaDon ttEnum = hd.getTrangThai();
        
        if (ttEnum == TrangThaiHoaDon.DAT) {
            // Trường hợp 1: ĐÃ ĐẶT -> Hành động: NHẬN BÀN
            btnSmartAction.setText("▶ Nhận bàn ngay");
            btnSmartAction.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
            btnSmartAction.setOnAction(e -> {
                e.consume(); 
                handleNhanBanNhanh(hd); // Gọi hàm nhận bàn
            });
            
        } else if (ttEnum == TrangThaiHoaDon.CHO_XAC_NHAN) {
            // Trường hợp 2: CHỜ CỌC -> Hành động: XÁC NHẬN CỌC
            btnSmartAction.setText("💰 Xác nhận cọc");
            btnSmartAction.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-weight: bold;");
            btnSmartAction.setOnAction(e -> {
                e.consume();
                loadHoaDonToMainInterface(hd); 
                openThanhToanCocPopup();       
            });
            
        } else if (ttEnum == TrangThaiHoaDon.DANG_SU_DUNG || ttEnum == TrangThaiHoaDon.HOA_DON_TAM) {
            // Trường hợp 3: ĐANG PHỤC VỤ / ĐÃ NHẬN BÀN -> Hành động: THANH TOÁN
            btnSmartAction.setText("💲 Thanh toán");
            btnSmartAction.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-weight: bold;");
            btnSmartAction.setOnAction(e -> {
                e.consume();
                loadHoaDonToMainInterface(hd); 
                handleThanhToan();             
            });
            
        } else {
            // Trường hợp khác: Chỉ xem
            btnSmartAction.setText("Xem chi tiết");
            btnSmartAction.setOnAction(e -> {
                e.consume();
                loadHoaDonToMainInterface(hd);
            });
        }

        // 6. Thêm tất cả vào Card
        card.getChildren().addAll(lblMaHD, lblSDT, lblTrangThai, lblThoiGian, lblBan, btnSmartAction);
        
        return card;
    }
 // ui.DatBan.java

    /**
     * 🔥 HÀM THÔNG MINH: Chuyển nhanh từ "Đã đặt" sang "Đang phục vụ" (Check-in).
     * Cập nhật cả HĐ Gốc và các HĐ Phụ, cập nhật màu bàn.
     */
    private void handleNhanBanNhanh(HoaDon hd) {
        // 1. Xác định HĐ Gốc
        String maHDGoc = (hd.getMaHDGoc() != null) ? hd.getMaHDGoc() : hd.getMaHD();
        
        try {
            // 2. Tìm tất cả HĐ liên quan (Gốc + Phụ)
            List<HoaDon> allRelated = new ArrayList<>();
            HoaDon hdGocObj = datBanDAO.getHoaDonByMaHD(maHDGoc);
            if (hdGocObj != null) allRelated.add(hdGocObj);
            allRelated.addAll(datBanDAO.getHoaDonPhuByMaHDGoc(maHDGoc));

            // 3. Cập nhật DB: Chuyển tất cả sang DANG_SU_DUNG
            String newStatusDb = TrangThaiHoaDon.DANG_SU_DUNG.getDbValue();
            String newBanStatus = TrangThaiBan.DANG_SU_DUNG.getDbValue();

            for (HoaDon item : allRelated) {
                // Update Hóa đơn
                datBanDAO.capNhatTrangThaiHoaDon(item.getMaHD(), newStatusDb, false);
                
                // Update Bàn
                if (item.getBan() != null) {
                    datBanDAO.capNhatTrangThaiBan(item.getBan().getMaBan(), newBanStatus);
                }
            }

            // 4. Thông báo và Refresh
            showAlert(Alert.AlertType.INFORMATION, "Nhận bàn thành công", 
                      "Đã nhận bàn cho HĐ " + maHDGoc + ". Khách bắt đầu sử dụng.");
            
         // 5. 🔥 TỰ ĐỘNG LOAD VÀ CẬP NHẬT COMBOBOX
            loadBookingCards();
            loadTableGrids();
            
            // Tìm lại HĐ mới nhất từ DB để có trạng thái đúng
            HoaDon hdMoi = datBanDAO.getHoaDonByMaHD(maHDGoc);
            if (hdMoi != null) {
                loadHoaDonToMainInterface(hdMoi); // Hàm này sẽ tự set ComboBox thành "Đã nhận bàn"
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể nhận bàn: " + e.getMessage());
        }
    }

	/**
     * 🔥 HÀM MỚI: Mở Popup Chi Tiết Đặt Bàn.
     */
    private void openChiTietDatBanPopup(HoaDon hd) {
    	loadHoaDonToMainInterface(hd);
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
        
        // Format giá tiền hiển thị cho đẹp (VD: 120,000)
        colGia.setCellValueFactory(cellData -> cellData.getValue().giaProperty());
        colGia.setCellFactory(tc -> new TableCell<MonAn, Number>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%,.0f", item.doubleValue()));
                }
            }
        });
        
        colHinhAnh.setCellFactory(param -> new TableCell<MonAn, String>() {
            private final ImageView imageView = new ImageView();
            { 
                imageView.setFitWidth(40); 
                imageView.setFitHeight(40);
                imageView.setPreserveRatio(true); 
                setAlignment(Pos.CENTER);
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    byte[] hinhAnhBytes = getTableView().getItems().get(getIndex()).getHinhAnhBytes(); 
                    if (hinhAnhBytes != null && hinhAnhBytes.length > 0) {
                        try {
                            imageView.setImage(new Image(new ByteArrayInputStream(hinhAnhBytes)));
                            setGraphic(imageView);
                        } catch (Exception e) { setGraphic(null); }
                    } else { setGraphic(null); }
                }
            }
        });

        // 🔥 NÚT CHỌN: MÀU XANH LÁ
        colChon.setCellFactory(tc -> new TableCell<MonAn, Void>() {
            final Button btn = new Button("Chọn");
            {
                // Style cho nút Chọn
                btn.setStyle("-fx-background-color: #2f9e44; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
                btn.setPrefWidth(60);
                
                btn.setOnAction(event -> {
                    MonAn mon = getTableView().getItems().get(getIndex());
                    handleChonMon(mon);
                    calculateTotal(); 
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
    }
    
    private void setupMonOrderTable() {
        colOrderTenMon.setCellValueFactory(cellData -> cellData.getValue().tenMonProperty());
        
        // Format đơn giá trong bảng Order
        colOrderDonGia.setCellValueFactory(cellData -> cellData.getValue().donGiaProperty());
        colOrderDonGia.setCellFactory(tc -> new TableCell<MonOrder, Number>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : String.format("%,.0f", item.doubleValue()));
            }
        });
        
        colOrderSoLuong.setCellValueFactory(cellData -> cellData.getValue().soLuongProperty().asObject());

        // 🔥 NÚT TĂNG GIẢM: MÀU CAM VÀ XANH DƯƠNG
        colOrderTangGiam.setCellFactory(tc -> new TableCell<MonOrder, Void>() {
            final HBox box = new HBox(5);
            final Button btnMinus = new Button("-");
            final Button btnPlus = new Button("+");
            
            {
                // Style nút Trừ (-) : Màu Cam/Đỏ nhạt
                btnMinus.setStyle("-fx-background-color: #f08c00; -fx-text-fill: white; -fx-font-weight: bold; -fx-min-width: 30px; -fx-cursor: hand;");
                
                // Style nút Cộng (+) : Màu Xanh dương
                btnPlus.setStyle("-fx-background-color: #1971c2; -fx-text-fill: white; -fx-font-weight: bold; -fx-min-width: 30px; -fx-cursor: hand;");
                
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

        // 🔥 NÚT HỦY: MÀU ĐỎ
        colOrderHuy.setCellFactory(tc -> new TableCell<MonOrder, Void>() {
            final Button btnHuy = new Button("X");
            {
                // Style nút Hủy : Màu Đỏ đậm
                btnHuy.setStyle("-fx-background-color: #e03131; -fx-text-fill: white; -fx-font-weight: bold; -fx-min-width: 30px; -fx-cursor: hand;");
                
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
    /**
     * TÍNH TOÁN VÀ HIỂN THỊ TỔNG TIỀN (ĐÃ SỬA LOGIC VAT)
     */
    private void calculateTotal() {
        double tongTienMonAn = monOrderList.stream()
                .mapToDouble(order -> order.getDonGia() * order.getSoLuong())
                .sum();
        
        // 1. Phí dịch vụ (5% trên tổng món)
        final double SERVICE_FEE_RATE = 0.05; 
        double phiDichVu = tongTienMonAn * SERVICE_FEE_RATE; 
        
        // 2. 🔥 SỬA: Thuế VAT (8% TRÊN TỔNG MÓN ĂN - Theo yêu cầu)
        final double VAT_RATE = 0.08;         
        // Code cũ (sai): double thueVAT = (tongTienMonAn + phiDichVu) * VAT_RATE;
        double thueVAT = tongTienMonAn * VAT_RATE; // Code mới (Đúng)
        
        // 3. Khuyến mãi
        double tienKhuyenMai = 0.0; 
        if (selectedUuDai != null) {
            tienKhuyenMai = tongTienMonAn * (selectedUuDai.getGiaTri() / 100.0);
        }
        
        // Cập nhật model
        if (currentHoaDon != null) {
            currentHoaDon.setKhuyenMai(tienKhuyenMai);
            currentHoaDon.setPhiDichVu(phiDichVu);
            currentHoaDon.setThueVAT(thueVAT);
        }

        // 4. Tiền cọc
        double tienCocDaThanhToan = 0.0;
        try {
            String tienCocRaw = txtTienCoc.getText().replaceAll("[^0-9.]", "");
            tienCocDaThanhToan = Double.parseDouble(tienCocRaw.isEmpty() ? "0" : tienCocRaw);
        } catch (NumberFormatException e) {
            tienCocDaThanhToan = 0.0;
        }
        
        // 5. Tổng thanh toán
        double tongTienThanhToan = tongTienMonAn + phiDichVu + thueVAT - tienKhuyenMai - tienCocDaThanhToan;

        // 6. Hiển thị lên UI
        if (lblTongTienMonAn != null) lblTongTienMonAn.setText(String.format("%,.0f Đ", tongTienMonAn));
        if (lblPhiDichVu != null) lblPhiDichVu.setText(String.format("%,.0f Đ", phiDichVu)); 
        if (lblThueVAT != null) lblThueVAT.setText(String.format("%,.0f Đ", thueVAT));
        if (lblKhuyenMai != null) lblKhuyenMai.setText(String.format("%,.0f Đ", tienKhuyenMai)); 
        if (lblTienCocSummary != null) lblTienCocSummary.setText(String.format("%,.0f Đ", tienCocDaThanhToan));
        if (lblTongTienThanhToan != null) lblTongTienThanhToan.setText(String.format("%,.0f Đ", Math.max(0, tongTienThanhToan))); 
    }

    
    public void clearFormDatBan() {
        // 1. Clear các trường nhập liệu
    	comboThoiGian.getSelectionModel().selectFirst();
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
     // 🔥 THÊM MỚI: Vô hiệu hóa nút Thanh toán cọc
        if (btnThanhToanCoc != null) {
            btnThanhToanCoc.setDisable(true);
        }
        // 3. Reset trạng thái logic
        isBookingConfirmed = false; //
        currentHoaDon = null; // Reset hóa đơn đang xem
        this.daThanhToanCoc = false;

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
    
 // =========================================================
    // XỬ LÝ TÌM BÀN TRỐNG - LOGIC TỐI ƯU (SMART FIT)
    // =========================================================
    private void handleTimBanTrong() {
        LocalDate ngay; String gioStr; LocalTime gio; java.sql.Timestamp ts; 
        int soLuongKhach = 0; 

        try {
            ngay = datePickerThoiGianDen.getValue(); 
            gioStr = comboThoiGian.getValue(); 
            String khuVucChon = comboKhuVuc.getValue(); 

            // Parse số lượng khách
            String slKhachStr = txtSoLuongKhach.getText().trim();
            if (!slKhachStr.isEmpty()) {
                try {
                    soLuongKhach = Integer.parseInt(slKhachStr);
                    if (soLuongKhach < 0) throw new NumberFormatException();
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.WARNING, "Lỗi nhập liệu", "Số lượng khách phải là số nguyên dương.");
                    return;
                }
            }

            if (ngay == null || gioStr == null || gioStr.trim().isEmpty()) { 
                showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng chọn Ngày và Giờ.");
                return;
            }

            gio = LocalTime.parse(gioStr, timeFormatter); 
            ts = java.sql.Timestamp.valueOf(ngay.atTime(gio)); 

            // Reset Form & Restore UI
            clearFormDatBan(); 
            this.currentHoaDon = null; 
            this.isBookingConfirmed = false; 
            datePickerThoiGianDen.setValue(ngay); 
            comboThoiGian.setValue(gioStr); 
            if (soLuongKhach > 0) txtSoLuongKhach.setText(String.valueOf(soLuongKhach));
            comboKhuVuc.setValue(khuVucChon); 

            // Lấy dữ liệu từ DB
            List<Map<String, Object>> allBanInfo = datBanDAO.getAllBanWithAvailability(ts); 
            this.dsHoaDonDatTrongNgay = datBanDAO.getDsDatBanHomNay(ngay); 

            List<Ban> banHienThi = new ArrayList<>(); 
            
            for (Map<String, Object> banInfo : allBanInfo) { 
                Ban ban = (Ban) banInfo.get("ban");
                
                // 1. ĐIỀU KIỆN TIÊN QUYẾT: BÀN PHẢI ĐỦ CHỖ
                if (soLuongKhach > 0 && ban.getSucChua() < soLuongKhach) {
                    continue; 
                }

                boolean passKhuVuc = true;
                
                // 2. LOGIC LỌC
                if (khuVucChon != null && soLuongKhach > 0) {
                    
                    if (khuVucChon.equals("Tự động")) {
                        // --- A. PHÂN LOẠI KHU VỰC ---
                        // Nhóm < 9 người: Ẩn Phòng riêng (để dành phòng cho nhóm 9-10 trở lên)
                        if (soLuongKhach < 9 && ban.getLoaiBan() == LoaiBan.PHONG) {
                            passKhuVuc = false;
                        }
                        // Nhóm >= 10 người: Ẩn bàn Sảnh (thường bàn sảnh chỉ max 8)
                        else if (soLuongKhach >= 10 && ban.getLoaiBan() != LoaiBan.PHONG) {
                             passKhuVuc = false;
                        }

                        // --- B. ĐỘ VỪA VẶN (FIT LOGIC) ---
                        if (passKhuVuc) {
                            int gheDu = ban.getSucChua() - soLuongKhach;

                            if (soLuongKhach < 10) {
                                // Với nhóm nhỏ (< 10): Áp dụng "Vừa khít" (Strict Fit)
                                // Chỉ cho phép dư tối đa 1 ghế
                                // VD: Khách 3 -> Bàn 4 (Dư 1) OK. Bàn 6 (Dư 3) Ẩn.
                                if (gheDu > 1) passKhuVuc = false;
                            } else {
                                // Với nhóm lớn (>= 10): Áp dụng "Linh hoạt" (Loose Fit)
                                // Cho phép dư tối đa 5 ghế (vì bàn lớn hiếm)
                                // VD: Khách 13 -> Phòng 15 (Dư 2) OK.
                                if (gheDu > 5) passKhuVuc = false;
                            }
                        }
                    }
                    // Logic lọc cứng nếu user chọn cụ thể Khu vực (Tầng trệt/1/Phòng)
                    else if (!khuVucChon.equals("Tất cả")) {
                         if (khuVucChon.equals("Tầng trệt") && ban.getLoaiBan() != LoaiBan.TANG_TRET) passKhuVuc = false;
                         else if (khuVucChon.equals("Tầng 1") && ban.getLoaiBan() != LoaiBan.TANG_1) passKhuVuc = false;
                         else if (khuVucChon.equals("Phòng riêng") && ban.getLoaiBan() != LoaiBan.PHONG) passKhuVuc = false;
                    }
                }
                
                if (!passKhuVuc) continue; 

                // 3. XÁC ĐỊNH TRẠNG THÁI MÀU SẮC
                Ban banMoi = new Ban(ban.getMaBan(), ban.getViTri(), ban.getSucChua(), ban.getLoaiBan(), ban.getTrangThai());
                TrangThaiBan trangThaiHienThi = getTrangThaiHienThi(banMoi, gio); 
                
                if (trangThaiHienThi != TrangThaiBan.TRONG) {
                     banMoi.setTrangThai(trangThaiHienThi);
                } else {
                     banMoi.setTrangThai(TrangThaiBan.TRONG);
                }
                
                banHienThi.add(banMoi); 
            }
            
            loadTableGridsBase(banHienThi); 
            
            if (banHienThi.isEmpty()) {
                String msg = "Không tìm thấy bàn trống phù hợp.";
                if (soLuongKhach > 0 && "Tự động".equals(khuVucChon)) {
                    msg += "\n(Hệ thống đang ẩn các bàn quá rộng hoặc quá chật để tối ưu. Hãy thử chọn khu vực 'Tất cả' để xem toàn bộ bàn).";
                }
                showAlert(Alert.AlertType.INFORMATION, "Thông báo", msg);
            }

        } catch (java.time.format.DateTimeParseException e) { 
            showAlert(Alert.AlertType.ERROR, "Lỗi định dạng", "Giờ không hợp lệ.");
        } catch (Exception ex) { 
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tìm bàn: " + ex.getMessage());
            loadTableGrids(); 
        }
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
     * 🔥 XỬ LÝ SỬA HÓA ĐƠN (Thông tin KH, Tiền cọc, Danh sách món, VÀ KHUYẾN MÃI)
     */
    private void handleSuaHoaDon() {
        if (currentHoaDon == null) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không có hóa đơn nào được chọn để sửa.");
            return;
        }
        
        try {
            // 1. Cập nhật Khách hàng
            KhachHang kh = datBanDAO.timHoacTaoKhachHang(txtSoDienThoai.getText(), txtTenKhachHang.getText());
            
            // 2. Cập nhật Tiền cọc
            double tienCoc = 0;
            try {
                 String tienCocRaw = txtTienCoc.getText().replaceAll("[^0-9.]", ""); 
                 tienCoc = Double.parseDouble(tienCocRaw.isEmpty() ? "0" : tienCocRaw);
            } catch (NumberFormatException e) { }

            // 3. Gọi DAO cập nhật thông tin chính (MaKH, TienCoc)
            datBanDAO.capNhatThongTinHoaDon(currentHoaDon.getMaHD(), kh.getMaKH(), tienCoc);

            // 4. Gọi DAO cập nhật chi tiết món ăn
            datBanDAO.capNhatChiTietHoaDon(currentHoaDon.getMaHD(), monOrderList);
            
            // ---------------------------------------------------------
            // 🔥 ĐOẠN LOGIC MỚI BỔ SUNG: CẬP NHẬT KHUYẾN MÃI
            // ---------------------------------------------------------
            // Tính toán tổng tiền món để tính % giảm giá
            double tongTienMon = monOrderList.stream().mapToDouble(m -> m.getDonGia() * m.getSoLuong()).sum();
            double tienKhuyenMai = 0;
            String maUuDai = null;

            if (selectedUuDai != null) {
                maUuDai = selectedUuDai.getMaUuDai();
                // Tính tiền giảm giá dựa trên % giá trị ưu đãi
                tienKhuyenMai = tongTienMon * (selectedUuDai.getGiaTri() / 100.0);
            }

            // Gọi hàm DAO vừa thêm ở Bước 1
            datBanDAO.capNhatKhuyenMaiHoaDon(currentHoaDon.getMaHD(), maUuDai, tienKhuyenMai);

            // Cập nhật lại object hiện tại trên RAM để đồng bộ
            currentHoaDon.setMaUuDai(maUuDai);
            currentHoaDon.setKhuyenMai(tienKhuyenMai);
            // ---------------------------------------------------------
            
            // Chỉ hiện thông báo thành công nếu người dùng nhấn nút Sửa Món
            if (btnSuaMon.isFocused()) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã cập nhật Hóa đơn " + currentHoaDon.getMaHD() + " thành công.");
            }
            
            // Tải lại danh sách bên trái
            loadBookingCards(); 

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi CSDL", "Không thể cập nhật hóa đơn: " + e.getMessage());
        }
    }
   
 // [Trong file DatBan.java]

    /**
     * 🔥 XỬ LÝ HỦY BÀN (ĐÃ SỬA: Thêm Popup hiển thị tiền hoàn cọc + Inline CSS)
     */
    private void handleHuyBan() {
        if (currentHoaDon == null) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không có hóa đơn nào được chọn để hủy.");
            return;
        }
        if (currentHoaDon.getTrangThai() != null && currentHoaDon.getTrangThai().getDbValue().equals("DaThanhToan")) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể hủy Hóa đơn đã thanh toán.");
            return;
        }
        if (currentHoaDon.getGioVao() == null) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xác định giờ vào của hóa đơn để tính hoàn cọc.");
            return;
        }

        // --- 1. Tính toán thời gian còn lại và tiền hoàn cọc ---
        LocalDateTime gioVao = currentHoaDon.getGioVao();
        LocalDateTime now = LocalDateTime.now();
        long minutesToArrival = java.time.Duration.between(now, gioVao).toMinutes();

        double tienCoc = currentHoaDon.getTienCoc();
        double refundPercentage = 0.0;
        String policyApplied;

        if (minutesToArrival >= 120) { // Hủy trước >= 2 giờ
            refundPercentage = 0.8;
            policyApplied = "Hủy trước ≥2 giờ: hoàn 80% tiền cọc.";
        } else if (minutesToArrival >= 60) { // Hủy trong 1–2 giờ
            refundPercentage = 0.5;
            policyApplied = "Hủy trong 1–2 giờ: hoàn 50% tiền cọc.";
        } else { // Hủy < 1 giờ hoặc đã qua giờ vào
            refundPercentage = 0.0;
            if (now.isAfter(gioVao.plusMinutes(30))) {
                 policyApplied = "Đến muộn trên 30 phút: không hoàn tiền cọc.";
            } else {
                 policyApplied = "Hủy sát giờ (<1 giờ): không hoàn tiền cọc.";
            }
        }

        double refundAmount = tienCoc * refundPercentage;
        DecimalFormat currencyFormatter = new DecimalFormat("###,### VNĐ");

        // --- 2. Tạo Alert và ÁP DỤNG CSS ---
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("XÁC NHẬN HỦY BÀN");
        confirmationAlert.setHeaderText("Hủy Hóa đơn " + currentHoaDon.getMaHD() + "?");

        String contentText = String.format(
            "Chính sách áp dụng: %s\n" +
            "Tiền cọc đã nhận: %s\n" +
            "Số tiền hoàn lại cho khách: %s (%.0f%%)\n\n" +
            "Bạn có chắc chắn muốn hủy toàn bộ cụm Hóa đơn này và trả bàn về trống không?",
            policyApplied,
            currencyFormatter.format(tienCoc),
            currencyFormatter.format(refundAmount),
            refundPercentage * 100
        );
        confirmationAlert.setContentText(contentText);

        // Đổi tên nút
        ButtonType confirmButton = new ButtonType("Xác nhận Hủy");
        ButtonType cancelButton = ButtonType.CANCEL;
        confirmationAlert.getButtonTypes().setAll(confirmButton, cancelButton);

        // --- 🔥 THÊM CSS TRỰC TIẾP ---
        DialogPane dialogPane = confirmationAlert.getDialogPane();
        dialogPane.setStyle(
            "-fx-background-color: #f8f9fa; " + // Nền sáng
            "-fx-border-color: #dee2e6; " +     // Viền xám nhạt
            "-fx-border-width: 1px;"
        );
        // Style Header Text
        dialogPane.lookup(".header-panel .label").setStyle(
            "-fx-font-size: 1.2em; " +
            "-fx-font-weight: bold; " +
            "-fx-text-fill: #dc3545;" // Màu đỏ cảnh báo
        );
        // Style Content Text
        dialogPane.lookup(".content.label").setStyle(
            "-fx-font-size: 1.0em; " +
            "-fx-line-spacing: 5px;"
        );
        // Style Buttons
        Button confirmBtnNode = (Button) dialogPane.lookupButton(confirmButton);
        confirmBtnNode.setStyle(
            "-fx-background-color: #dc3545; " + // Nút xác nhận màu đỏ
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-padding: 8px 15px;"
        );
        Button cancelBtnNode = (Button) dialogPane.lookupButton(cancelButton);
        cancelBtnNode.setStyle(
             "-fx-background-color: #6c757d; " + // Nút hủy màu xám
             "-fx-text-fill: white; " +
             "-fx-padding: 8px 15px;"
        );
        // -----------------------------

        Optional<ButtonType> result = confirmationAlert.showAndWait();

        // --- 3. Xử lý kết quả từ Popup ---
        if (result.isPresent() && result.get() == confirmButton) {
             try {
                List<HoaDon> allRelatedHDs = new ArrayList<>(currentHoaDonGocVaPhu);

                for (HoaDon hdToHuy : allRelatedHDs) {
                    datBanDAO.capNhatTrangThaiHoaDon(hdToHuy.getMaHD(), TrangThaiHoaDon.DA_HUY.getDbValue(), true);
                    if (hdToHuy.getBan() != null) {
                        datBanDAO.capNhatTrangThaiBan(hdToHuy.getBan().getMaBan(), TrangThaiBan.TRONG.getDbValue());
                        System.out.println("LOG: Đã hủy HD " + hdToHuy.getMaHD() + " và trả bàn " + hdToHuy.getBan().getMaBan());
                    }
                }

                showAlert(Alert.AlertType.INFORMATION, "Thành công",
                    "Đã hủy toàn bộ cụm Hóa đơn gốc " + currentHoaDon.getMaHD() +
                    ".\nSố tiền cần hoàn lại cho khách: " + currencyFormatter.format(refundAmount));

                clearFormDatBan();
                loadBookingCards();
                loadTableGrids();

             } catch (Exception e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Lỗi CSDL", "Không thể hủy hóa đơn: " + e.getMessage());
             }
        } else {
            showAlert(Alert.AlertType.INFORMATION, "Đã hủy", "Thao tác hủy bàn đã được hủy bỏ.");
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
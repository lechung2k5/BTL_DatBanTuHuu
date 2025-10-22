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

// Import JavaFX
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Comparator; 
import java.io.ByteArrayInputStream; 

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.beans.value.ObservableValue; // Import cần thiết cho các kiểu trả về

public class DatBan implements Initializable {
    
    // DAO
    private final DatBanDAO datBanDAO = new DatBanDAO(); 
    private final MonAnDAO monAnDAO = new MonAnDAO();           
    private final DanhMucMonDAO danhMucMonDAO = new DanhMucMonDAO(); 
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    
    // Data
    private ObservableList<MonOrder> monOrderList = FXCollections.observableArrayList();
    private List<DanhMucMon> dsDanhMuc; 
    
    // NEW: Hỗ trợ chọn nhiều bàn (Dùng cho logic chọn/xác nhận)
    private ObservableList<Ban> selectedBanList = FXCollections.observableArrayList();
    private ObservableList<Button> selectedButtonList = FXCollections.observableArrayList();
    
    // Danh sách các đơn đặt bàn/đang sử dụng trong ngày (dùng cho logic cảnh báo)
    private List<HoaDon> dsHoaDonDatTrongNgay = new ArrayList<>(); 
    
    // Biến trạng thái để kiểm soát luồng Xác nhận -> Lưu
    private boolean isBookingConfirmed = false; 
    
    private HoaDon currentHoaDon = null;
    private ToggleGroup menuGroup;
    private ToggleGroup paymentGroup;
    
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
    
    // Table Món Ăn (Menu)
    @FXML private TableView<MonAn> tblMonAn;
    // SỬA: Sử dụng ViewModel MonAn cho TableView
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
    @FXML private ComboBox<String> promoComboBox;
    @FXML private Label lblTongTienMonAn;
    @FXML private Label lblPhiDichVu;
    @FXML private Label lblThueVAT;
    @FXML private Label lblKhuyenMai;
    @FXML private Label lblTienCocSummary;
    @FXML private Label lblTongTienThanhToan;

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
        
        // 1. Tải danh mục
        dsDanhMuc = danhMucMonDAO.getAllDanhMuc();
        
        // 2. Cài đặt ToggleGroup (dùng dsDanhMuc)
        menuGroup = new ToggleGroup();
        ToggleButton[] toggles = {toggleKhaiVi, toggleNuong, toggleLau, toggleXaoHap, toggleChien, toggleDacSan, toggleDoUong};
        
        for (int i = 0; i < dsDanhMuc.size() && i < toggles.length; i++) {
            ToggleButton toggle = toggles[i];
            DanhMucMon dm = dsDanhMuc.get(i);
            
            toggle.setText(dm.getTenDM());
            toggle.setUserData(dm.getMaDM());
            toggle.setToggleGroup(menuGroup);
            // Sự kiện sẽ được gán ở Bước 2 (sau khi tất cả @FXML fields đã được inject)
        }
        
        // 3. Khởi tạo các bảng
        setupMonAnTable();
        setupMonOrderTable();
        
        // 4. Tải và load món ăn mặc định
        List<entity.MonAn> dsEntityMonAn = monAnDAO.getAllMonAn();
        loadMonAnTable(dsEntityMonAn.stream().map(MonAn::fromEntity).collect(Collectors.toList()));
        
        // 5. Cài đặt ToggleGroup Phương thức thanh toán
        paymentGroup = new ToggleGroup();
        ToggleButton[] paymentToggles = {btnTienMat, btnNganHang, btnMoMo};
        for (ToggleButton toggle : paymentToggles) {
            toggle.setToggleGroup(paymentGroup);
        }

        // 6. Tải dữ liệu đặt bàn và sơ đồ bàn (Phụ thuộc vào các fields UI, nên đặt sau FXML Load)
        loadBookingCards(); 
        loadTableGrids(); 


        // ----------------------------------------------------
        // BƯỚC 2: GÁN SỰ KIỆN VÀ CẬP NHẬT UI (Sử dụng @FXML fields an toàn)
        // ----------------------------------------------------
        
        // Cài đặt ComboBox khuyến mãi
        promoComboBox.setItems(FXCollections.observableArrayList("Giảm 15% hóa đơn cho khách hàng VIP", "Không áp dụng"));
        promoComboBox.getSelectionModel().selectFirst();

        // Cài đặt ngày giờ mặc định cho đơn mới
        datePickerThoiGianDen.setValue(LocalDate.now());
        txtThoiGian.setText(LocalTime.now().format(timeFormatter));

        // Gắn sự kiện cho các nút
        btnXacNhanBan.setOnAction(e -> handleXacNhanBan()); 
        btnTim.setOnAction(e -> handleTimBanTrong());
        btnLuuDatHang.setOnAction(e -> handleLuuDatHang());
        btnThanhToan.setOnAction(e -> handleThanhToan());
        
        // Gán sự kiện cho các ToggleButton Danh mục (Sử dụng fields đã được khởi tạo)
        ToggleButton[] togglesFinal = {toggleKhaiVi, toggleNuong, toggleLau, toggleXaoHap, toggleChien, toggleDacSan, toggleDoUong};
        for(ToggleButton toggle : togglesFinal) {
            if(toggle != null && toggle.getUserData() != null) {
                toggle.setOnAction(e -> handleMenuToggle((String)toggle.getUserData()));
            }
        }
        
        // 1. Gắn listener cho ô tìm kiếm món ăn 
        if (txtTimMon != null) {
            txtTimMon.textProperty().addListener((obs, oldVal, newVal) -> handleTimMon(newVal));
        } else {
             System.err.println("LỖI CẤU HÌNH FXML: Không thể inject txtTimMon.");
        }

        // 🔥 NEW: Gắn listener cho txtSoDienThoai để tìm tên KH
        txtSoDienThoai.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) { // Mất focus
                handleTimTenKhachHang(txtSoDienThoai.getText());
            }
        });
        
        // 2. Cập nhật nhãn khi khởi tạo 
        updateSelectionLabels();
        
        // 3. Tính tổng tiền lần đầu
        calculateTotal();
        
        // 🔥 ẨN PANEL HÓA ĐƠN KHI KHỞI TẠO
        if (vboxReceipt != null) {
            vboxReceipt.setVisible(false);
        }
    }
    
    /**
     * 🔥 XỬ LÝ LƯU THÔNG TIN ĐẶT HÀNG / KHÁCH ĐẾN QUÁN
     */
    private void handleLuuDatHang() {
        if (selectedBanList.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng chọn ít nhất một bàn.");
            return;
        }
        
        // KIỂM TRA TRẠNG THÁI CONFIRMED
        if (!isBookingConfirmed) {
            showAlert(Alert.AlertType.WARNING, "Thiếu bước Xác nhận", "Vui lòng nhấn nút 'Xác nhận bàn' trước khi lưu đặt hàng.");
            return;
        }

        try {
            // 1. Thu thập thông tin Khách hàng
            String tenKH = txtTenKhachHang.getText();
            String sdt = txtSoDienThoai.getText();
            
            if (sdt.isEmpty()) {
                 showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng nhập Số điện thoại khách hàng.");
                 return;
            }
            
            // Tìm hoặc tạo khách hàng (Giả định DAO có hàm này)
            KhachHang khachHang = datBanDAO.timHoacTaoKhachHang(sdt, tenKH);

            // 2. Thu thập thông tin thời gian
            LocalDate ngayDen = datePickerThoiGianDen.getValue();
            LocalTime gioDen = LocalTime.parse(txtThoiGian.getText(), timeFormatter);
            
            if (ngayDen == null) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng chọn ngày đến.");
                return;
            }
            
            java.time.LocalDateTime thoiGianDen = ngayDen.atTime(gioDen);
            
            // Xác định trạng thái ban đầu: Đặt trước (Dat) hay Dùng ngay (DangSuDung)
            String trangThaiBanDau;
            // Nếu Giờ đến <= Giờ hiện tại (cộng thêm 15 phút buffer) -> Dùng ngay
            if (thoiGianDen.isBefore(java.time.LocalDateTime.now().plusMinutes(15))) {
                trangThaiBanDau = "DangSuDung";
            } else {
                trangThaiBanDau = "Dat";
            }
            
            // 3. Xử lý từng bàn đã chọn
            for (Ban banDuocChon : selectedBanList) {
                
                // 3a. Kiểm tra lại trạng thái chặn cứng lần cuối
                TrangThaiBan trangThaiHienThi = getTrangThaiHienThi(banDuocChon, LocalTime.now());
                if (trangThaiHienThi == TrangThaiBan.DA_DAT || trangThaiHienThi == TrangThaiBan.DANG_SU_DUNG) {
                    showAlert(Alert.AlertType.WARNING, "Bàn bận", "Bàn " + banDuocChon.getMaBan() + " vừa bị chiếm. Vui lòng chọn bàn khác.");
                    return;
                }
                
                // 3b. Tạo đối tượng HoaDon mới
                HoaDon newHoaDon = new HoaDon();
                newHoaDon.setNgayLap(java.time.LocalDateTime.now());
                newHoaDon.setGioVao(thoiGianDen);
                
                // Dùng setKhachHang(KhachHang)
                newHoaDon.setKhachHang(khachHang); 
                
                newHoaDon.setBan(banDuocChon);
                newHoaDon.setTrangThai(trangThaiBanDau);
                
                double tienCoc = 0;
                try {
                     // SỬA: Đọc tiền cọc an toàn: loại bỏ định dạng dấu phẩy trước khi parse
                     String tienCocRaw = txtTienCoc.getText().replaceAll("[^0-9.]", ""); 
                     tienCoc = Double.parseDouble(tienCocRaw.isEmpty() ? "0" : tienCocRaw);
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Tiền cọc không hợp lệ.");
                    return;
                }
                newHoaDon.setTienCoc(tienCoc);
                
                // 3c. Lưu Hóa đơn và chi tiết CSDL
                datBanDAO.luuHoaDonVaChiTiet(newHoaDon, monOrderList);
                
                // 3d. Cập nhật trạng thái bàn trong DB
                datBanDAO.capNhatTrangThaiBan(banDuocChon.getMaBan(), trangThaiBanDau);
                
                System.out.println("LOG: Đã lưu HD mới cho bàn " + banDuocChon.getMaBan() + " với trạng thái: " + trangThaiBanDau);
            }
            
            // 4. Hoàn tất và Reset
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã lưu đặt hàng thành công! Trạng thái: " + (trangThaiBanDau.equals("Dat") ? "Đã đặt" : "Đang sử dụng"));
            
            // Reset trạng thái confirmed sau khi lưu
            isBookingConfirmed = false;
            
            clearFormDatBan();
            loadBookingCards();
            loadTableGrids(); // Cập nhật lại sơ đồ bàn
            
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

        // Kiểm tra: Nếu là đơn mới và bàn đã bận, không cho xác nhận
        if (currentHoaDon == null) {
            for (Ban selectedBan : selectedBanList) {
                // Sử dụng logic trạng thái hiển thị mới để kiểm tra chặn cứng (thời điểm hiện tại)
                TrangThaiBan trangThaiHienThi = getTrangThaiHienThi(selectedBan, LocalTime.now());
                if (trangThaiHienThi == TrangThaiBan.DA_DAT || trangThaiHienThi == TrangThaiBan.DANG_SU_DUNG) {
                     showAlert(Alert.AlertType.WARNING, "Bàn đang bận", "Bàn " + selectedBan.getMaBan() + " đang bận hoặc đã được đặt trước (trong vòng 4 tiếng trước giờ vào). Vui lòng chọn bàn khác.");
                     return;
                }
            }
        }
        
        // ------------------------------------------------------------------
        // LOGIC CHUYỂN TRẠNG THÁI TẠM THỜI VÀ TÍNH TIỀN CỌC
        // ------------------------------------------------------------------
        
        // 1. Cập nhật trạng thái bàn trong UI thành màu xác nhận (ĐỎ tạm thời)
        for (int i = 0; i < selectedBanList.size(); i++) {
            Button button = selectedButtonList.get(i);
            
            // Bỏ màu xanh lá (selected)
            button.getStyleClass().remove("table-button-selected"); 
            
            // Áp dụng màu Đỏ (booked) cho button để hiển thị trạng thái đã xác nhận
            applyTableStyle(button, TrangThaiBan.DA_DAT); 
        }
        
        // 2. TÍNH TIỀN CỌC VÀ CẬP NHẬT TRƯỜNG TEXT
        double tienCoc = calculateTienCoc();
        // SỬA: ĐIỀN VÀO TXT DƯỚI DẠNG SỐ KHÔNG ĐỊNH DẠNG (BỎ DẤU PHẨY)
        txtTienCoc.setText(String.format("%.0f", tienCoc)); 

        // 3. Đánh dấu đã xác nhận
        isBookingConfirmed = true; 
        
        showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã xác nhận bàn. Tiền cọc đã được tính và áp dụng. Vui lòng nhấn 'Lưu đặt hàng' để hoàn tất.");

        // 4. Cập nhật nhãn
        updateSelectionLabels();
        
        // 5. Tải lại các thành phần khác (nếu cần)
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
        
        // Ở bước này (Xác nhận bàn), chúng ta giả định KHÔNG có món cụ thể
        // nên chỉ áp dụng quy tắc 150.000đ/bàn và phí dịch vụ 100.000đ/phòng.

        for (Ban ban : selectedBanList) {
            // 1. Áp dụng phí cọc cơ bản cho mỗi bàn
            tongTienCoc += COC_MAC_DINH;
            
            // 2. Thêm phí dịch vụ nếu là phòng riêng (Loại bàn PHONG/TANG_1)
            if (ban.getLoaiBan() == LoaiBan.PHONG) {
                tongTienCoc += PHI_PHONG_RIENG;
            }
        }
        
        return tongTienCoc;
    }
    
    // =========================================================
    // LOGIC TÍNH TOÁN TRẠNG THÁI HIỂN THỊ MỚI
    // =========================================================

    // Hàm tính toán trạng thái hiển thị của bàn dựa trên MỘT thời điểm kiểm tra
    private TrangThaiBan getTrangThaiHienThi(Ban banGoc, LocalTime thoiGianKiemTra) {
        // 1. Nếu trạng thái DB gốc là DANG_SU_DUNG (Cam) thì luôn hiển thị Đang Sử Dụng.
        if (banGoc.getTrangThai() == TrangThaiBan.DANG_SU_DUNG) {
            return TrangThaiBan.DANG_SU_DUNG;
        }

        // 2. Lấy đơn đặt bàn "Dat" (Đã đặt) gần nhất cho bàn này
        Optional<HoaDon> datGanNhat = dsHoaDonDatTrongNgay.stream()
            .filter(hd -> hd.getBan() != null && hd.getBan().getMaBan().equals(banGoc.getMaBan()) && hd.getGioVao() != null)
            .filter(hd -> hd.getTrangThai().equals("Dat")) 
            .min(Comparator.comparing(hd -> hd.getGioVao())); 

        if (datGanNhat.isPresent()) {
            LocalTime gioVao = datGanNhat.get().getGioVao().toLocalTime();
            
            // Tính toán các mốc thời gian
            LocalTime gioCanhBaoMem = gioVao.minusHours(8); // Giờ vào - 8 tiếng (Bắt đầu cảnh báo mềm)
            LocalTime gioCanhBaoCung = gioVao.minusHours(4); // Giờ vào - 4 tiếng (Bắt đầu chặn/ĐỎ)
            
            // QUY TẮC 1: Chặn hoàn toàn (DA_DAT / ĐỎ) - Bắt đầu TẠI 4 tiếng trước giờ vào.
            if ((thoiGianKiemTra.isAfter(gioCanhBaoCung) || thoiGianKiemTra.equals(gioCanhBaoCung))
                 && (thoiGianKiemTra.isBefore(gioVao) || thoiGianKiemTra.equals(gioVao))) {
                return TrangThaiBan.DA_DAT; 
            }
            
            // Quy tắc 2: Cảnh báo mềm (TRONG/TRẮNG) - Từ 8 tiếng đến 4 tiếng trước giờ vào
            // gioCanhBaoMem <= thoiGianKiemTra < gioCanhBaoCung
            if (thoiGianKiemTra.isAfter(gioCanhBaoMem) && thoiGianKiemTra.isBefore(gioCanhBaoCung)) {
                 return TrangThaiBan.TRONG; 
            }
        }
        
        // Quy tắc 3: Bình thường (TRONG) - Hơn 8 tiếng hoặc không có đơn đặt nào sắp tới.
        return TrangThaiBan.TRONG;
    }
    
    // =========================================================
    // LOGIC TẢI BẢNG VÀ CHỌN BÀN
    // =========================================================
    
    private void loadTableGrids() {
        System.out.println("\n*** LOAD SƠ ĐỒ BÀN BAN ĐẦU (DỰ TRÊN THỜI GIAN HIỆN TẠI) ***");
        
        List<Ban> tatCaBan = datBanDAO.getAllBan();
        List<Ban> banHienThi = new ArrayList<>();
        LocalTime thoiGianHienTai = LocalTime.now();

        for (Ban ban : tatCaBan) {
            // TẠO BẢN SAO VÀ GÁN TRẠNG THÁI HIỂN THỊ MONG MUỐN
            Ban banMoi = new Ban(ban.getMaBan(), ban.getViTri(), ban.getSucChua(), ban.getLoaiBan(), ban.getTrangThai());
            
            // Chỉ tính lại trạng thái hiển thị 4h/8h nếu trạng thái DB không phải đang sử dụng
            if (banMoi.getTrangThai() != TrangThaiBan.DANG_SU_DUNG) {
                 TrangThaiBan trangThaiHienThi = getTrangThaiHienThi(banMoi, thoiGianHienTai);
                 // Ghi đè trạng thái DB bằng trạng thái LIVE tính toán
                 banMoi.setTrangThai(trangThaiHienThi);
            }
            banHienThi.add(banMoi);
        }
        
        loadTableGridsBase(banHienThi);
    }

    // Hàm load sơ đồ bàn dựa trên danh sách bàn đã được lọc/chuẩn bị
    private void loadTableGridsBase(List<Ban> dsBan) {
        try {
            List<Ban> tangTret = dsBan.stream().filter(b -> b.getLoaiBan() == LoaiBan.TANG_TRET).collect(Collectors.toList());
            List<Ban> tangMot = dsBan.stream().filter(b -> b.getLoaiBan() == LoaiBan.TANG_1).collect(Collectors.toList()); 
            List<Ban> phongRieng = dsBan.stream().filter(b -> b.getLoaiBan() == LoaiBan.PHONG).collect(Collectors.toList()); 

            populateTableGrid(gridTangTret, tangTret);
            populateTableGrid(gridTang1, tangMot);
            populateTableGrid(gridPhongRieng, phongRieng);
            
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi tải bàn", "Không thể tải danh sách bàn. Kiểm tra kết nối CSDL và DatBanDAO.");
            
            // Fallback (giữ nguyên logic Mock Tables)
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
        grid.getChildren().clear();
        int col = 0;
        int row = 0;
        int maxCols = 5; 

        for (Ban ban : dsBan) {
            Button btn = createTableButton(ban);
            btn.setOnAction(e -> handleChonBan(ban, btn)); 

            grid.add(btn, col, row);
            
            GridPane.setMargin(btn, new Insets(8)); 

            col++;
            if (col >= maxCols) {
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

        // SỬ DỤNG TRẠNG THÁI ĐÃ GÁN (từ loadTableGridsBase)
        TrangThaiBan trangThaiHienThi = ban.getTrangThai(); 

        applyTableStyle(btn, trangThaiHienThi);

        return btn;
    }
    
    // Hàm áp dụng style (màu sắc) dựa trên trạng thái
    private void applyTableStyle(Button button, TrangThaiBan trangThai) {
        button.getStyleClass().removeAll("table-button-booked", "table-button-serving", "table-button-available", "table-button-selected");
        
        if (trangThai == TrangThaiBan.DA_DAT) {
             button.getStyleClass().add("table-button-booked"); // Màu Đỏ (Chặn cứng)
        } else if (trangThai == TrangThaiBan.DANG_SU_DUNG) {
             button.getStyleClass().add("table-button-serving"); // Màu Vàng/Cam (Đang sử dụng)
        } else {
             // TRONG (bao gồm cả trường hợp Cảnh báo mềm 8 tiếng -> 4 tiếng)
             button.getStyleClass().add("table-button-available"); // Màu Trắng
        }
    }
    
    // CẬP NHẬT LOGIC: Thêm kiểm tra CẢNH BÁO MỀM 8 TIẾNG -> 4 TIẾNG VÀ CHẶN CỨNG 4 TIẾNG
    private void handleChonBan(Ban ban, Button currentButton) {
        
        LocalTime thoiGianHienTai = LocalTime.now(); 
        
        // Lấy trạng thái hiển thị của bàn DỰA TRÊN THỜI GIAN HIỆN TẠI
        TrangThaiBan trangThaiHienThi = getTrangThaiHienThi(ban, thoiGianHienTai);

        // 1. CHẶN CỨNG (4 TIẾNG TRƯỚC GIỜ VÀO HOẶC ĐANG SỬ DỤNG)
        if (trangThaiHienThi == TrangThaiBan.DA_DAT || trangThaiHienThi == TrangThaiBan.DANG_SU_DUNG) {
            showAlert(Alert.AlertType.INFORMATION, "Bàn bận", "Bàn này hiện không thể chọn do đang được sử dụng hoặc đã được đặt (trong vòng 4 tiếng trước giờ vào).");
            return;
        }

        // 2. LOGIC CẢNH BÁO MỀM (8 TIẾNG -> 4 TIẾNG)
        Optional<HoaDon> datGanNhat = dsHoaDonDatTrongNgay.stream()
            .filter(hd -> hd.getBan() != null && hd.getBan().getMaBan().equals(ban.getMaBan()) && hd.getGioVao() != null)
            .filter(hd -> hd.getTrangThai().equals("Dat"))
            .min(Comparator.comparing(hd -> hd.getGioVao()));

        if (datGanNhat.isPresent()) {
            LocalTime gioVao = datGanNhat.get().getGioVao().toLocalTime();
            LocalTime gioCanhBaoMem = gioVao.minusHours(8); 
            LocalTime gioCanhBaoCung = gioVao.minusHours(4); 
            
            // Kiểm tra: Nếu thời gian hiện tại nằm trong khoảng [Giờ vào - 8 tiếng, Giờ vào - 4 tiếng)
            if (thoiGianHienTai.isAfter(gioCanhBaoMem) && thoiGianHienTai.isBefore(gioCanhBaoCung)) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Cảnh báo bàn sắp có khách");
                confirm.setHeaderText("Bàn " + ban.getMaBan() + " đã được đặt lúc " + gioVao.format(timeFormatter));
                confirm.setContentText("Bàn này có đơn đặt trước trong vòng 8 tiếng tới (vùng cảnh báo mềm). Bạn có chắc chắn muốn chọn không?");
                
                Optional<ButtonType> result = confirm.showAndWait();
                
                if (result.isPresent() && (result.get() == ButtonType.CANCEL || result.get() == ButtonType.CLOSE)) {
                    return; // Hủy thao tác chọn bàn
                }
            }
        }
        // END LOGIC CẢNH BÁO MỀM

        boolean alreadySelected = selectedBanList.contains(ban);

        if (alreadySelected) {
            // Xử lý thao tác BỎ CHỌN
            isBookingConfirmed = false; 
            txtTienCoc.setText("0"); // Clear tiền cọc
            
            selectedBanList.remove(ban);
            selectedButtonList.remove(currentButton);
            
            currentButton.getStyleClass().remove("table-button-selected");
            // Khôi phục màu gốc
            applyTableStyle(currentButton, trangThaiHienThi); 
        } 
        else {
            // Xử lý thao tác CHỌN bàn mới
            isBookingConfirmed = false; 
            txtTienCoc.setText("0"); // Clear tiền cọc
            
            selectedBanList.add(ban);
            selectedButtonList.add(currentButton);
            
            // Áp dụng màu 'selected' (xanh lá)
            currentButton.getStyleClass().removeAll("table-button-booked", "table-button-serving", "table-button-available");
            currentButton.getStyleClass().add("table-button-selected"); 
        }
        
        // 3. CẬP NHẬT NHÃN HIỂN THỊ
        updateSelectionLabels(); 
    }
    
    // Cập nhật nhãn thông tin bàn (FIX LỖI NULL POINTER)
    private void updateSelectionLabels() {
        if (selectedBanList.isEmpty()) {
            if (lblBanDangChon != null) lblBanDangChon.setText("Chưa chọn");
            if (lblTrangThaiBan != null) lblTrangThaiBan.setText("---");
            return;
        }

        // Tạo chuỗi hiển thị cho nhiều bàn
        String banNames = selectedBanList.stream()
                .map(Ban::getMaBan)
                .collect(Collectors.joining(", "));
        
        // Cập nhật trạng thái hiển thị
        String statusText = selectedBanList.size() + " bàn đã chọn";

        if (lblBanDangChon != null) lblBanDangChon.setText(banNames);
        if (lblTrangThaiBan != null) lblTrangThaiBan.setText(statusText);
    }
    
    private void loadBookingCards() {
        vboxBookingCards.getChildren().clear();
        
        // Lấy ngày từ DatePicker (hoặc ngày hiện tại nếu DatePicker rỗng)
        LocalDate dateToLoad = datePickerThoiGianDen.getValue() != null ? datePickerThoiGianDen.getValue() : LocalDate.now();
        
        try {
            // Lấy dữ liệu thực tế từ DAO và LƯU vào biến toàn cục
            List<HoaDon> dsDatBan = datBanDAO.getDsDatBanHomNay(dateToLoad); 
            this.dsHoaDonDatTrongNgay = dsDatBan; // <--- CẬP NHẬT BIẾN TOÀN CỤC

            System.out.println("\n*** LOG: Tải thành công " + dsDatBan.size() + " đơn đặt/sử dụng cho ngày " + dateToLoad + " ***");

            if (dsDatBan == null || dsDatBan.isEmpty()) {
                Label lbl = new Label("Chưa có đơn đặt/đang sử dụng nào trong ngày " + dateToLoad.toString() + ".");
                lbl.setPadding(new Insets(10));
                vboxBookingCards.getChildren().add(lbl);
            } else {
                for (HoaDon hd : dsDatBan) {
                    VBox card = createBookingCard(hd);
                    card.setOnMouseClicked(e -> handleSelectBookingCard(hd));
                    vboxBookingCards.getChildren().add(card);
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi tải danh sách đặt bàn: " + e.getMessage());
            // Hiển thị thông báo lỗi thay vì Mock Data
            Label lbl = new Label("LỖI TẢI DỮ LIỆU: " + e.getMessage());
            lbl.setPadding(new Insets(10));
            vboxBookingCards.getChildren().add(lbl);
        }
    }

    // THẺ ĐẶT BÀN HIỂN THỊ CHI TIẾT VÀ NÚT XEM CHI TIẾT LỚN
    private VBox createBookingCard(HoaDon hd) {
        VBox card = new VBox(8); // Tăng spacing
        card.getStyleClass().add("booking-card"); // Sử dụng style chung cho card
        card.setPadding(new Insets(15));

        // --- Chuẩn bị dữ liệu hiển thị ---
        String maGiaoDich = hd.getMaHD();
        
        String trangThai = hd.getTrangThai();
        String trangThaiViet = switch (trangThai != null ? trangThai : "Unknown") {
            case "Dat" -> "Đã đặt"; 
            case "DangSuDung" -> "Đang phục vụ";
            case "HoaDonTam" -> "Hóa đơn tạm";
            default -> "Chờ";
        };
        
        // Kiểm tra gioVao có bị null không trước khi gọi toLocalTime()
        String gioVao = (hd.getGioVao() != null) 
                           ? hd.getGioVao().toLocalTime().format(timeFormatter) : "N/A";
        
        // LƯU Ý: selectedBan đã bị xóa, sử dụng Ban từ HoaDon
        Ban ban = hd.getBan(); 
        String maBan = ban != null ? ban.getMaBan() : "N/A";
        String soBan = maBan.replaceAll("[^0-9]", "");
        soBan = soBan.substring(Math.max(soBan.length() - 2, 0));
        
        String sdtKhach = (hd.getKhachHang() != null && hd.getKhachHang().getSoDT() != null) 
                           ? hd.getKhachHang().getSoDT() : "N/A";

        // --- 1. Mã Hóa đơn (ID) ---
        Label lblMaHD = new Label(maGiaoDich != null ? maGiaoDich : "Mã: N/A");
        lblMaHD.getStyleClass().add("booking-card-id"); 
        
        // --- 2. SĐT ---
        Label lblSDT = new Label("SĐT: " + sdtKhach);
        
        // --- 3. Trạng thái ---
        Label lblTrangThai = new Label("Trạng thái: " + trangThaiViet);
        lblTrangThai.getStyleClass().add("booking-status-" + (trangThai != null ? trangThai.toLowerCase() : "default")); 

        // --- 4. Thời gian đặt ---
        Label lblThoiGian = new Label("Thời gian đặt: " + gioVao);
        
        // --- 5. Số bàn ---
        Label lblBan = new Label("Bàn: " + soBan);

        // --- 6. Nút Xem Chi Tiết (Lớn) ---
        Button btnXemChiTiet = new Button("Xem chi tiết");
        btnXemChiTiet.setMaxWidth(Double.MAX_VALUE);
        btnXemChiTiet.getStyleClass().add("view-details-button"); 
        btnXemChiTiet.setPrefHeight(45); 
        btnXemChiTiet.setOnAction(e -> handleSelectBookingCard(hd)); // Gán action

        // --- Sắp xếp các thành phần ---
        card.getChildren().addAll(lblMaHD, lblSDT, lblTrangThai, lblThoiGian, lblBan, btnXemChiTiet);
        
        return card;
    }

    // TRIỂN KHAI LOGIC TẢI THÔNG TIN LÊN FORM KHI CHỌN CARD
    private void handleSelectBookingCard(HoaDon hd) {
        currentHoaDon = hd; // Lưu hóa đơn đang chỉnh sửa
        
        // 1. Cập nhật thông tin khách hàng và bàn
        if (hd.getKhachHang() != null) {
            txtSoDienThoai.setText(hd.getKhachHang().getSoDT());
            txtTenKhachHang.setText(hd.getKhachHang().getTenKH()); 
        } else {
            txtSoDienThoai.clear();
            txtTenKhachHang.clear();
        }

        // 2. Cập nhật thời gian
        if (hd.getGioVao() != null) {
            datePickerThoiGianDen.setValue(hd.getGioVao().toLocalDate());
            txtThoiGian.setText(hd.getGioVao().toLocalTime().format(timeFormatter));
        }

        // 3. Cập nhật trạng thái bàn hiển thị
        if (hd.getBan() != null) {
            Ban tempBan = hd.getBan(); 
            if (lblBanDangChon != null) lblBanDangChon.setText(tempBan.getMaBan());
            // SỬA: Dùng trạng thái hiển thị tại thời điểm hiện tại
            if (lblTrangThaiBan != null) lblTrangThaiBan.setText(getTrangThaiHienThi(tempBan, LocalTime.now()).toString());
        }

        // 4. Cập nhật Tiền Cọc
        if (hd.getTienCoc() != null) {
            // Định dạng tiền cọc (loại bỏ phần thập phân nếu bằng .0)
            String tienCocStr = String.format("%,.0f", hd.getTienCoc());
            txtTienCoc.setText(tienCocStr);
        } else {
            txtTienCoc.setText("0"); 
        }
        
        // 🔥 NEW LOGIC: Tải số lượng khách (Giả định HoaDon có trường soLuongKhach)
        if (hd.getBan() != null) {
            txtSoLuongKhach.setText(String.valueOf(hd.getBan().getSucChua())); // Dùng sức chứa bàn tạm thời
        } else {
            txtSoLuongKhach.clear();
        }

        // 🔥 NEW LOGIC: Tải danh sách gọi món (Chi tiết hóa đơn)
        monOrderList.clear();
        if (hd.getMaHD() != null) {
            ObservableList<MonOrder> chiTiet = datBanDAO.getChiTietHoaDon(hd.getMaHD());
            monOrderList.addAll(chiTiet);
        }
        tblMonDaChon.refresh();
        
        // Cập nhật lại tổng tiền trên hóa đơn (sau khi load chi tiết món)
        calculateTotal(); 
        
        // Đánh dấu confirmed là true nếu đây là đơn đang tồn tại
        isBookingConfirmed = true; 
        
        // 🔥 NEW: ẨN PANEL HÓA ĐƠN KHI CHỌN CARD (Chỉ hiện khi bấm Thanh toán)
        if (vboxReceipt != null) {
            vboxReceipt.setVisible(false);
        }
    }
    
    // =========================================================
    // LOGIC TẢI MENU VÀ GỌI MÓN (ĐÃ SỬA)
    // =========================================================
    
    /**
     * XỬ LÝ TÌM KIẾM MÓN ĂN THEO TÊN
     */
    private void handleTimMon(String keyword) {
        if (keyword.trim().isEmpty()) {
            // Nếu rỗng, hiển thị lại tất cả món ăn
            List<entity.MonAn> dsEntityMonAn = monAnDAO.getAllMonAn();
            // FIX LỖI: THÊM CHUYỂN ĐỔI KIỂU DỮ LIỆU
            loadMonAnTable(dsEntityMonAn.stream().map(MonAn::fromEntity).collect(Collectors.toList()));
        } else {
            // Gọi hàm tìm kiếm từ DAO
            List<entity.MonAn> dsEntityMonAn = monAnDAO.searchMonAnByName(keyword);
            // FIX LỖI: THÊM CHUYỂN ĐỔI KIỂU DỮ LIỆU
            loadMonAnTable(dsEntityMonAn.stream().map(MonAn::fromEntity).collect(Collectors.toList()));
        }
    }
    
    /**
     * XỬ LÝ CHỌN DANH MỤC (dùng mã danh mục)
     */
    private void handleMenuToggle(String maDanhMuc) {
        if (maDanhMuc == null) return;
        
        // Tải món ăn theo mã danh mục
        List<entity.MonAn> dsEntityMonAn = monAnDAO.getMonAnByDanhMuc(maDanhMuc);
            // FIX LỖI: THÊM CHUYỂN ĐỔI KIỂU DỮ LIỆU
        loadMonAnTable(dsEntityMonAn.stream().map(MonAn::fromEntity).collect(Collectors.toList()));
        
        // Xóa nội dung tìm kiếm
        txtTimMon.clear();
    }
    
    /**
     * Hàm tải danh sách món ăn lên bảng Menu (tblMonAn)
     */
    // SỬA THAM SỐ: Đảm bảo tham số là ViewModel (DatBan.MonAn)
    private void loadMonAnTable(List<MonAn> dsMon) {
        ObservableList<MonAn> monAnObservableList = FXCollections.observableArrayList(dsMon);
        tblMonAn.setItems(monAnObservableList);
        tblMonAn.refresh();
    }
    
    private void setupMonAnTable() {
        // Ánh xạ các cột
        // SỬA: Sử dụng thuộc tính thực tế của entity.MonAn (getTenMon, getGiaBan)
        colTenMon.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTenMon()));
        
        // FIX DÒNG 835: Đảm bảo kiểu trả về là ObservableValue<Number>
        colGia.setCellValueFactory(cellData -> {
            return cellData.getValue().giaProperty(); // SimpleDoubleProperty là ObservableValue<Number>
        });
        
        // SỬA CÁCH HIỂN THỊ HÌNH ẢNH
        colHinhAnh.setCellFactory(param -> new TableCell<MonAn, String>() {
            private final ImageView imageView = new ImageView();
            private final Label lblNoImage = new Label("N/A"); // Thêm label báo không có ảnh

            { 
                imageView.setFitWidth(50); 
                imageView.setFitHeight(50);
                imageView.setPreserveRatio(true); // Hiển thị theo tỷ lệ
                setAlignment(Pos.CENTER);
            }
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getItem() == null) {
                    setGraphic(null);
                } else {
                    // Lấy byte[] từ ViewModel (đã được tải từ Entity)
                    byte[] hinhAnhBytes = getTableView().getItems().get(getIndex()).getHinhAnhBytes(); 
                    
                    if (hinhAnhBytes != null && hinhAnhBytes.length > 0) {
                        try {
                            // Chuyển byte[] sang Image và hiển thị
                            Image image = new Image(new ByteArrayInputStream(hinhAnhBytes));
                            imageView.setImage(image);
                            setGraphic(imageView);
                        } catch (Exception e) {
                            // Xử lý lỗi tải ảnh (nếu byte[] bị hỏng)
                            System.err.println("❌ LỖI RUNTIME LOAD ẢNH: " + getTableView().getItems().get(getIndex()).getTenMon() + " - " + e.getMessage());
                            setGraphic(lblNoImage); 
                        }
                    } else {
                        setGraphic(lblNoImage); // Hiển thị N/A nếu không có dữ liệu ảnh
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
                    calculateTotal(); // TÍNH LẠI TỔNG TIỀN
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
        
        // XÓA: Xóa dữ liệu mock
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
                    calculateTotal(); // TÍNH LẠI TỔNG TIỀN
                });

                btnMinus.setOnAction(event -> {
                    MonOrder order = getTableView().getItems().get(getIndex());
                    if (order.getSoLuong() > 1) {
                        order.setSoLuong(order.getSoLuong() - 1);
                        tblMonDaChon.refresh();
                        calculateTotal(); // TÍNH LẠI TỔNG TIỀN
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
                    calculateTotal(); // TÍNH LẠI TỔNG TIỀN
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnHuy);
            }
        });
        
        tblMonDaChon.setItems(monOrderList);
        
        // XÓA: Xóa dữ liệu mock
    }

    private void handleChonMon(MonAn mon) {
        // Lấy mã món, tên món, và giá từ entity.MonAn thực tế
        Optional<MonOrder> existingOrder = monOrderList.stream()
            .filter(o -> o.getMaMon().equals(mon.getMaMon()))
            .findFirst();

        if (existingOrder.isPresent()) {
            MonOrder order = existingOrder.get();
            order.setSoLuong(order.getSoLuong() + 1);
        } else {
            monOrderList.add(new MonOrder(mon.getMaMon(), mon.getTenMon(), mon.getGiaBan(), 1)); // Dùng getGiaBan
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
        
        final double VAT_RATE = 0.08; 
        
        double phiDichVu = 0.0; // Giả định 0.0
        double thueVAT = tongTienMonAn * VAT_RATE;
        double tienKhuyenMai = 0.0; // Logic khuyến mãi phức tạp hơn, tạm thời 0
        
        double tienCocDaThanhToan = 0.0;
        try {
            // Lấy tiền cọc từ text field (có thể là tiền cọc đã nhập hoặc đã tính)
            // Loại bỏ định dạng (dấu phẩy) để đảm bảo parse Double thành công.
            String tienCocRaw = txtTienCoc.getText().replaceAll("[^0-9.]", "");
            tienCocDaThanhToan = Double.parseDouble(tienCocRaw.isEmpty() ? "0" : tienCocRaw);
        } catch (NumberFormatException e) {
            tienCocDaThanhToan = 0.0;
        }
        
        double tongTienThanhToan = tongTienMonAn + phiDichVu + thueVAT - tienKhuyenMai - tienCocDaThanhToan;

        // Cập nhật nhãn
        if (lblTongTienMonAn != null) lblTongTienMonAn.setText(String.format("%,.0f Đ", tongTienMonAn));
        if (lblPhiDichVu != null) lblPhiDichVu.setText(String.format("%,.0f Đ", phiDichVu));
        if (lblThueVAT != null) lblThueVAT.setText(String.format("%,.0f Đ", thueVAT));
        if (lblKhuyenMai != null) lblKhuyenMai.setText(String.format("%,.0f Đ", tienKhuyenMai));
        if (lblTienCocSummary != null) lblTienCocSummary.setText(String.format("%,.0f Đ", tienCocDaThanhToan));
        if (lblTongTienThanhToan != null) lblTongTienThanhToan.setText(String.format("%,.0f Đ", Math.max(0, tongTienThanhToan))); // Tổng tiền không âm
    }

    
    private void clearFormDatBan() {
        txtThoiGian.clear();
        datePickerThoiGianDen.setValue(LocalDate.now());
        txtTenKhachHang.clear();
        txtSoDienThoai.clear();
        txtSoLuongKhach.clear();
        txtYeuCau.clear();
        txtTienCoc.clear();
        if (lblBanDangChon != null) lblBanDangChon.setText("Chưa chọn");
        if (lblTrangThaiBan != null) lblTrangThaiBan.setText("Trống");
        
        // Reset màu bàn
        for (Button btn : selectedButtonList) {
             // Lấy trạng thái hiển thị gốc dựa trên thời gian hiện tại
             Ban banGoc = selectedBanList.get(selectedButtonList.indexOf(btn));
             TrangThaiBan trangThaiGoc = getTrangThaiHienThi(banGoc, LocalTime.now());
             applyTableStyle(btn, trangThaiGoc); 
        }
        
        // Clear danh sách chọn
        selectedBanList.clear();
        selectedButtonList.clear(); 
        
        // Clear món đã chọn và tính tiền
        monOrderList.clear(); 
        calculateTotal();
        
        // Reset trạng thái confirmed
        isBookingConfirmed = false;
        
        // ẨN PANEL HÓA ĐƠN KHI KHỞI TẠO
        if (vboxReceipt != null) {
            vboxReceipt.setVisible(false);
        }
    }
    
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * 🔥 NEW: Xử lý tìm kiếm Tên khách hàng dựa trên SĐT khi mất focus
     */
    private void handleTimTenKhachHang(String sdt) {
        if (sdt.trim().isEmpty() || sdt.trim().length() < 9) { 
            return;
        }

        try {
            // Gọi DAO để tìm hoặc tạo Khách hàng (logic đã được sửa để SELECT trước)
            KhachHang khachTimDuoc = datBanDAO.timHoacTaoKhachHang(sdt, ""); 
            
            // Nếu khách hàng đã tồn tại (không phải là Guest mới tạo, hoặc tên không rỗng)
            if (khachTimDuoc != null && khachTimDuoc.getMaKH() != null && khachTimDuoc.getTenKH() != null && !khachTimDuoc.getTenKH().equals("Khách vãng lai")) { 
                 // Nếu là khách hàng thành viên có tên, tự động điền
                 txtTenKhachHang.setText(khachTimDuoc.getTenKH());
            } else if (khachTimDuoc != null && khachTimDuoc.getTenKH().equals("Khách vãng lai")) {
                 // Nếu là KH Guest (mới tạo tối thiểu), xóa trường tên để người dùng nhập tên nếu muốn
                 txtTenKhachHang.clear();
            }

        } catch (Exception e) {
            System.err.println("Lỗi khi tìm tên khách hàng qua SĐT: " + e.getMessage());
        }
    }

 // =========================================================
    // XỬ LÝ TÌM BÀN TRỐNG THEO THỜI GIAN
    // =========================================================
    private void handleTimBanTrong() {
        // Đảm bảo không có bàn nào được chọn khi thực hiện tìm kiếm mới
        for (Button btn : selectedButtonList) {
            // Lấy trạng thái hiển thị gốc dựa trên thời gian hiện tại
            Ban banGoc = selectedBanList.get(selectedButtonList.indexOf(btn));
            TrangThaiBan trangThaiGoc = getTrangThaiHienThi(banGoc, LocalTime.now());
            applyTableStyle(btn, trangThaiGoc); 
        }
        selectedBanList.clear();
        selectedButtonList.clear();
        updateSelectionLabels();
        
        // Reset trạng thái confirmed
        isBookingConfirmed = false;
        txtTienCoc.setText("0");


        try {
            LocalDate ngay = datePickerThoiGianDen.getValue();
            String gioStr = txtThoiGian.getText();
            
            if (ngay == null || gioStr.isEmpty()) {
                loadTableGrids(); 
                return;
            }
            
            LocalTime gio = LocalTime.parse(gioStr, timeFormatter);
            
            System.out.println("\n*** LOG: THỰC HIỆN TÌM BÀN TRỐNG TẠI THỜI GIAN NGƯỜI DÙNG NHẬP: " + gioStr + " ***");

            // 2. Lấy danh sách TẤT CẢ BÀN và Bàn trống/bận tại giờ tìm kiếm (DAO)
            java.sql.Timestamp ts = java.sql.Timestamp.valueOf(ngay.atTime(gio));
            List<Ban> tatCaBan = datBanDAO.getAllBan(); 
            List<Ban> banTrongTaiGioTim = datBanDAO.getBanTrongTheoGio(ts);
            Set<String> maBanTrong = banTrongTaiGioTim.stream().map(Ban::getMaBan).collect(Collectors.toSet());
            
            List<Ban> banHienThi = new ArrayList<>();
            
            for (Ban ban : tatCaBan) {
                
                Ban banMoi = new Ban(ban.getMaBan(), ban.getViTri(), ban.getSucChua(), ban.getLoaiBan(), ban.getTrangThai());
                
                // LOGIC TÌM KIẾM
                if (!maBanTrong.contains(ban.getMaBan())) {
                    // 1. Nếu DAO báo KHÔNG TRỐNG tại giờ tìm kiếm, GÁN DA_DAT (ĐỎ).
                    banMoi.setTrangThai(TrangThaiBan.DA_DAT); 
                } else {
                    // 2. Nếu DAO báo TRỐNG tại giờ tìm kiếm, BẮT ĐẦU DÙNG LOGIC UI để xử lý VÙNG CẢNH BÁO (4h-8h).
                    TrangThaiBan trangThaiHienThiTaiGioTim = getTrangThaiHienThi(ban, gio);
                    
                    if (trangThaiHienThiTaiGioTim == TrangThaiBan.DA_DAT || trangThaiHienThiTaiGioTim == TrangThaiBan.DANG_SU_DUNG) {
                         banMoi.setTrangThai(trangThaiHienThiTaiGioTim); // ĐỎ/CAM (Vùng chặn cứng)
                    } else {
                         // Bao gồm cả vùng cảnh báo 4h-8h và vùng TRỐNG tuyệt đối.
                         banMoi.setTrangThai(TrangThaiBan.TRONG); // TRẮNG
                    }
                }
                
                banHienThi.add(banMoi);
            }
            
            // Cập nhật lại giao diện
            loadBookingCards(); 
            loadTableGridsBase(banHienThi); 

        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tìm bàn trống. Kiểm tra dữ liệu đầu vào hoặc kết nối DB.");
            loadTableGrids(); // Fallback về trạng thái ban đầu
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
        
        // Kiểm tra trạng thái: Không thể thanh toán đơn Đã Thanh toán
        if (currentHoaDon.getTrangThai().equals("DaThanhToan")) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Hóa đơn này đã được thanh toán.");
            return;
        }

        // 🔥 LOGIC MỚI: Tải chi tiết món ăn và hiển thị panel hóa đơn
        
        // 1. Load chi tiết món ăn (từ CSDL) vào bảng Order (Bảng tblMonDaChon)
        // Dữ liệu này được sử dụng để tính tổng tiền Hóa đơn bên phải
        monOrderList.clear();
        if (currentHoaDon.getMaHD() != null) {
            ObservableList<MonOrder> chiTiet = datBanDAO.getChiTietHoaDon(currentHoaDon.getMaHD());
            monOrderList.addAll(chiTiet);
        }
        tblMonDaChon.refresh();
        
        // 2. Cập nhật tổng tiền
        calculateTotal(); 

        // 3. Hiển thị Panel Hóa đơn (Panel bên phải)
        if (vboxReceipt != null) {
            vboxReceipt.setVisible(true);
        }
        
        // **********************************************
        // * ĐIỂM DỪNG: Logic thanh toán cuối cùng sẽ nằm *
        // * trong một nút khác (VD: In/Xác nhận TT)    *
        // **********************************************
    }

    // =========================================================
    // LỚP VIEWMODEL (SỬA LỖI KIỂU DỮ LIỆU)
    // =========================================================
    
    // SỬA: Lớp ViewModel MonAn
    public static class MonAn {
        // Sử dụng thuộc tính thực tế của entity.MonAn
        private final SimpleStringProperty maMon;
        private final SimpleStringProperty tenMon;
        // NEW: Thêm trường byte[] để giữ dữ liệu ảnh
        private final byte[] hinhAnhBytes; 
        private final SimpleDoubleProperty giaBan;

        // Constructor mới chỉ dùng 4 thuộc tính cần thiết
        public MonAn(String maMon, String tenMon, double giaBan, byte[] hinhAnhBytes) {
            this.maMon = new SimpleStringProperty(maMon);
            this.tenMon = new SimpleStringProperty(tenMon);
            this.giaBan = new SimpleDoubleProperty(giaBan);
            this.hinhAnhBytes = hinhAnhBytes; // Lưu byte[]
        }
        // Getter/Setter cho Entity MonAn
        public String getMaMon() { return maMon.get(); }
        public String getTenMon() { return tenMon.get(); }
        public SimpleStringProperty tenMonProperty() { return tenMon; }
        public double getGiaBan() { return giaBan.get(); }
        public SimpleDoubleProperty giaProperty() { return giaBan; }
        // NEW: Getter cho byte[]
        public byte[] getHinhAnhBytes() { return hinhAnhBytes; }
        // FIX: Bổ sung method ánh xạ từ entity.MonAn sang MonAn ViewModel
        public static MonAn fromEntity(entity.MonAn entity) {
            // Cập nhật để truyền byte[]
            return new MonAn(entity.getMaMon(), entity.getTenMon(), entity.getGiaBan(), entity.getHinhAnh());
        }
    }
    
    // Lớp MonOrder giữ nguyên
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
}
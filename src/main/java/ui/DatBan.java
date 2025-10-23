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
import entity.PTTThanhToan; // Import PTTThanhToan

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

import javafx.application.Platform; 
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader; 
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.beans.value.ObservableValue; 

public class DatBan implements Initializable {
    
    // DAO
    private final DatBanDAO datBanDAO = new DatBanDAO(); 
    private final MonAnDAO monAnDAO = new MonAnDAO();           
    private final DanhMucMonDAO danhMucMonDAO = new DanhMucMonDAO(); 
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    
    // Data
    private ObservableList<MonOrder> monOrderList = FXCollections.observableArrayList();
    private List<DanhMucMon> dsDanhMuc; 
    
    private ObservableList<Ban> selectedBanList = FXCollections.observableArrayList();
    private ObservableList<Button> selectedButtonList = FXCollections.observableArrayList();
    
    private List<HoaDon> dsHoaDonDatTrongNgay = new ArrayList<>(); 
    
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
    }
    
    // =========================================================
    // HÀM NGHIỆP VỤ TÁCH BÀN (CHÍNH)
    // =========================================================

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
            String tenKH = txtTenKhachHang.getText();
            String sdt = txtSoDienThoai.getText();
            
            if (sdt.isEmpty()) {
                 showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng nhập Số điện thoại khách hàng.");
                 return;
            }
            
            KhachHang khachHang = datBanDAO.timHoacTaoKhachHang(sdt, tenKH);

            LocalDate ngayDen = datePickerThoiGianDen.getValue();
            LocalTime gioDen = LocalTime.parse(txtThoiGian.getText(), timeFormatter);
            
            if (ngayDen == null) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng chọn ngày đến.");
                return;
            }
            
            java.time.LocalDateTime thoiGianDen = ngayDen.atTime(gioDen);
            
            String trangThaiBanDau;
            if (thoiGianDen.isBefore(java.time.LocalDateTime.now().plusMinutes(15))) {
                trangThaiBanDau = "DangSuDung";
            } else {
                trangThaiBanDau = "Dat";
            }
            
            for (Ban banDuocChon : selectedBanList) {
                
                TrangThaiBan trangThaiHienThi = getTrangThaiHienThi(banDuocChon, LocalTime.now());
                if (trangThaiHienThi == TrangThaiBan.DA_DAT || trangThaiHienThi == TrangThaiBan.DANG_SU_DUNG) {
                    showAlert(Alert.AlertType.WARNING, "Bàn bận", "Bàn " + banDuocChon.getMaBan() + " vừa bị chiếm. Vui lòng chọn bàn khác.");
                    return;
                }
                
                HoaDon newHoaDon = new HoaDon();
                newHoaDon.setNgayLap(java.time.LocalDateTime.now());
                newHoaDon.setGioVao(thoiGianDen);
                
                newHoaDon.setKhachHang(khachHang); 
                
                newHoaDon.setBan(banDuocChon);
                newHoaDon.setTrangThai(trangThaiBanDau);
                
                double tienCoc = 0;
                try {
                     String tienCocRaw = txtTienCoc.getText().replaceAll("[^0-9.]", ""); 
                     tienCoc = Double.parseDouble(tienCocRaw.isEmpty() ? "0" : tienCocRaw);
                } catch (NumberFormatException e) {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Tiền cọc không hợp lệ.");
                    return;
                }
                newHoaDon.setTienCoc(tienCoc);
                
                datBanDAO.luuHoaDonVaChiTiet(newHoaDon, monOrderList);
                
                datBanDAO.capNhatTrangThaiBan(banDuocChon.getMaBan(), trangThaiBanDau);
                
                System.out.println("LOG: Đã lưu HD mới cho bàn " + banDuocChon.getMaBan() + " với trạng thái: " + trangThaiBanDau);
            }
            
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã lưu đặt hàng thành công! Trạng thái: " + (trangThaiBanDau.equals("Dat") ? "Đã đặt" : "Đang sử dụng"));
            
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
        
        showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã xác nhận bàn. Tiền cọc đã được tính và áp dụng. Vui lòng nhấn 'Lưu đặt hàng' để hoàn tất.");

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

    // Hàm tính toán trạng thái hiển thị của bàn dựa trên MỘT thời điểm kiểm tra
    private TrangThaiBan getTrangThaiHienThi(Ban banGoc, LocalTime thoiGianKiemTra) {
        if (banGoc.getTrangThai() == TrangThaiBan.DANG_SU_DUNG) {
            return TrangThaiBan.DANG_SU_DUNG;
        }

        Optional<HoaDon> datGanNhat = dsHoaDonDatTrongNgay.stream()
            .filter(hd -> hd.getBan() != null && hd.getBan().getMaBan().equals(banGoc.getMaBan()) && hd.getGioVao() != null)
            .filter(hd -> hd.getTrangThai() != null && hd.getTrangThai().getDbValue().equals("Dat")) 
            .min(Comparator.comparing(hd -> hd.getGioVao())); 

        if (datGanNhat.isPresent()) {
            LocalTime gioVao = datGanNhat.get().getGioVao().toLocalTime();
            
            LocalTime gioCanhBaoCung = gioVao.minusHours(4); 
            LocalTime gioCanhBaoMem = gioVao.minusHours(8); 
            
            if ((thoiGianKiemTra.isAfter(gioCanhBaoCung) || thoiGianKiemTra.equals(gioCanhBaoCung))
                 && thoiGianKiemTra.isBefore(gioVao)) {
                return TrangThaiBan.DA_DAT; 
            }
            
            if (thoiGianKiemTra.isAfter(gioCanhBaoMem) && thoiGianKiemTra.isBefore(gioCanhBaoCung)) {
                 return TrangThaiBan.TRONG; 
            }
        }
        
        return TrangThaiBan.TRONG;
    }
    
    // =========================================================
    // LOGIC TẢI BẢNG VÀ CHỌN BÀN
    // =========================================================
    
    // Cần phải là public để Controller khác có thể gọi
    public void loadTableGrids() {
        System.out.println("\n*** LOAD SƠ ĐỒ BÀN BAN ĐẦU (DỰ TRÊN THỜI GIAN HIỆN TẠI) ***");
        
        List<Ban> tatCaBan = datBanDAO.getAllBan();
        List<Ban> banHienThi = new ArrayList<>();
        LocalTime thoiGianHienTai = LocalTime.now();

        for (Ban ban : tatCaBan) {
            Ban banMoi = new Ban(ban.getMaBan(), ban.getViTri(), ban.getSucChua(), ban.getLoaiBan(), ban.getTrangThai());
            
            if (banMoi.getTrangThai() != TrangThaiBan.DANG_SU_DUNG) {
                 TrangThaiBan trangThaiHienThi = getTrangThaiHienThi(banMoi, thoiGianHienTai);
                 banMoi.setTrangThai(trangThaiHienThi);
            }
            banHienThi.add(banMoi);
        }
        
        loadTableGridsBase(banHienThi);
    }

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
    
    private void handleChonBan(Ban ban, Button currentButton) {
        
        LocalTime thoiGianHienTai = LocalTime.now(); 
        
        TrangThaiBan trangThaiHienThi = getTrangThaiHienThi(ban, thoiGianHienTai);

        if (trangThaiHienThi == TrangThaiBan.DA_DAT || trangThaiHienThi == TrangThaiBan.DANG_SU_DUNG) {
            showAlert(Alert.AlertType.INFORMATION, "Bàn bận", "Bàn này hiện không thể chọn do đang được sử dụng hoặc đã được đặt (trong vòng 4 tiếng trước giờ vào).");
            return;
        }

        Optional<HoaDon> datGanNhat = dsHoaDonDatTrongNgay.stream()
            .filter(hd -> hd.getBan() != null && hd.getBan().getMaBan().equals(ban.getMaBan()) && hd.getGioVao() != null)
            .filter(hd -> hd.getTrangThai() != null && hd.getTrangThai().getDbValue().equals("Dat"))
            .min(Comparator.comparing(hd -> hd.getGioVao()));

        if (datGanNhat.isPresent()) {
            LocalTime gioVao = datGanNhat.get().getGioVao().toLocalTime();
            LocalTime gioCanhBaoMem = gioVao.minusHours(8); 
            LocalTime gioCanhBaoCung = gioVao.minusHours(4); 
            
            if (thoiGianHienTai.isAfter(gioCanhBaoMem) && thoiGianHienTai.isBefore(gioCanhBaoCung)) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Cảnh báo bàn sắp có khách");
                confirm.setHeaderText("Bàn " + ban.getMaBan() + " đã được đặt lúc " + gioVao.format(timeFormatter));
                confirm.setContentText("Bàn này có đơn đặt trước trong vòng 8 tiếng tới (vùng cảnh báo mềm). Bạn có chắc chắn muốn chọn không?");
                
                Optional<ButtonType> result = confirm.showAndWait();
                
                if (result.isPresent() && (result.get() == ButtonType.CANCEL || result.get() == ButtonType.CLOSE)) {
                    return; 
                }
            }
        }

        boolean alreadySelected = selectedBanList.contains(ban);

        if (alreadySelected) {
            isBookingConfirmed = false; 
            txtTienCoc.setText("0"); 
            
            selectedBanList.remove(ban);
            selectedButtonList.remove(currentButton);
            
            currentButton.getStyleClass().remove("table-button-selected");
            applyTableStyle(currentButton, trangThaiHienThi); 
        } 
        else {
            isBookingConfirmed = false; 
            txtTienCoc.setText("0"); 
            
            selectedBanList.add(ban);
            selectedButtonList.add(currentButton);
            
            currentButton.getStyleClass().removeAll("table-button-booked", "table-button-serving", "table-button-available");
            currentButton.getStyleClass().add("table-button-selected"); 
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
    public void loadBookingCards() {
        vboxBookingCards.getChildren().clear();
        
        LocalDate dateToLoad = datePickerThoiGianDen.getValue() != null ? datePickerThoiGianDen.getValue() : LocalDate.now();
        
        try {
            List<HoaDon> dsDatBan = datBanDAO.getDsDatBanHomNay(dateToLoad); 
            this.dsHoaDonDatTrongNgay = dsDatBan; 

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
            Label lbl = new Label("LỖI TẢI DỮ LIỆU: " + e.getMessage());
            lbl.setPadding(new Insets(10));
            vboxBookingCards.getChildren().add(lbl);
        }
    }

    private VBox createBookingCard(HoaDon hd) {
        VBox card = new VBox(8); 
        card.getStyleClass().add("booking-card"); 
        card.setPadding(new Insets(15));

        String maGiaoDich = hd.getMaHD();
        
        String trangThai = hd.getTrangThai() != null ? hd.getTrangThai().getDbValue() : "Unknown"; 
        
        String trangThaiViet = switch (trangThai) {
            case "Dat" -> "Đã đặt"; 
            case "DangSuDung" -> "Đang phục vụ";
            case "HoaDonTam" -> "Hóa đơn tạm";
            case "DaThanhToan" -> "Đã thanh toán";
            case "DaHuy" -> "Đã hủy";
            default -> "Chờ";
        };
        
        String gioVao = (hd.getGioVao() != null) 
                           ? hd.getGioVao().toLocalTime().format(timeFormatter) : "N/A";
        
        Ban ban = hd.getBan(); 
        String maBan = ban != null ? ban.getMaBan() : "N/A";
        String soBan = maBan.replaceAll("[^0-9]", "");
        soBan = soBan.substring(Math.max(soBan.length() - 2, 0));
        
        String sdtKhach = (hd.getKhachHang() != null && hd.getKhachHang().getSoDT() != null) 
                           ? hd.getKhachHang().getSoDT() : "N/A";

        Label lblMaHD = new Label(maGiaoDich != null ? maGiaoDich : "Mã: N/A");
        lblMaHD.getStyleClass().add("booking-card-id"); 
        
        Label lblSDT = new Label("SĐT: " + sdtKhach);
        
        Label lblTrangThai = new Label("Trạng thái: " + trangThaiViet);
        lblTrangThai.getStyleClass().add("booking-status-" + (trangThai != null ? trangThai.toLowerCase() : "default")); 

        Label lblThoiGian = new Label("Thời gian đặt: " + gioVao);
        
        Label lblBan = new Label("Bàn: " + soBan);

        Button btnXemChiTiet = new Button("Xem chi tiết");
        btnXemChiTiet.setMaxWidth(Double.MAX_VALUE);
        btnXemChiTiet.getStyleClass().add("view-details-button"); 
        btnXemChiTiet.setPrefHeight(45); 
        btnXemChiTiet.setOnAction(e -> handleSelectBookingCard(hd)); 

        card.getChildren().addAll(lblMaHD, lblSDT, lblTrangThai, lblThoiGian, lblBan, btnXemChiTiet);
        
        return card;
    }

    // TRIỂN KHAI LOGIC TẢI THÔNG TIN LÊN FORM KHI CHỌN CARD
    private void handleSelectBookingCard(HoaDon hd) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ChiTietDatBan_Popup.fxml"));
            VBox root = loader.load();
            
            // 🔥 GÁN CONTROLLER MÀN HÌNH CHÍNH (this) VÀO USER DATA
            root.setUserData(this); 
            
            ChiTietDatBanController controller = loader.getController();

            Stage popupStage = new Stage();
            popupStage.setTitle("Chi tiết đặt bàn " + (hd.getMaHD() != null ? hd.getMaHD() : "Mới"));
            Scene scene = new Scene(root);
            
            // Đường dẫn CSS
            URL cssUrl = getClass().getResource("/css/ChiTietDatBan.css");
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            } else {
                 System.err.println("Không tìm thấy file css. Đảm bảo nó nằm trong src/main/resources/css/");
            }
            
            popupStage.setScene(scene);
            
            // TRUYỀN DỮ LIỆU VÀO CONTROLLER MỚI
            controller.setHoaDonData(hd, datBanDAO);
            
            popupStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi UI", "Không thể tải giao diện chi tiết đặt bàn: " + e.getMessage());
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
        
        final double VAT_RATE = 0.08; 
        
        double phiDichVu = 0.0; 
        double thueVAT = tongTienMonAn * VAT_RATE;
        double tienKhuyenMai = 0.0; 
        
        double tienCocDaThanhToan = 0.0;
        try {
            String tienCocRaw = txtTienCoc.getText().replaceAll("[^0-9.]", "");
            tienCocDaThanhToan = Double.parseDouble(tienCocRaw.isEmpty() ? "0" : tienCocRaw);
        } catch (NumberFormatException e) {
            tienCocDaThanhToan = 0.0;
        }
        
        double tongTienThanhToan = tongTienMonAn + phiDichVu + thueVAT - tienKhuyenMai - tienCocDaThanhToan;

        if (lblTongTienMonAn != null) lblTongTienMonAn.setText(String.format("%,.0f Đ", tongTienMonAn));
        if (lblPhiDichVu != null) lblPhiDichVu.setText(String.format("%,.0f Đ", phiDichVu));
        if (lblThueVAT != null) lblThueVAT.setText(String.format("%,.0f Đ", thueVAT));
        if (lblKhuyenMai != null) lblKhuyenMai.setText(String.format("%,.0f Đ", tienKhuyenMai));
        if (lblTienCocSummary != null) lblTienCocSummary.setText(String.format("%,.0f Đ", tienCocDaThanhToan));
        if (lblTongTienThanhToan != null) lblTongTienThanhToan.setText(String.format("%,.0f Đ", Math.max(0, tongTienThanhToan))); 
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
        
        for (Button btn : selectedButtonList) {
             Ban banGoc = selectedBanList.get(selectedButtonList.indexOf(btn));
             TrangThaiBan trangThaiGoc = getTrangThaiHienThi(banGoc, LocalTime.now());
             applyTableStyle(btn, trangThaiGoc); 
        }
        
        selectedBanList.clear();
        selectedButtonList.clear(); 
        
        monOrderList.clear(); 
        calculateTotal();
        
        isBookingConfirmed = false;
        
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
    // =========================================================
    private void handleTimBanTrong() {
        for (Button btn : selectedButtonList) {
            Ban banGoc = selectedBanList.get(selectedButtonList.indexOf(btn));
            TrangThaiBan trangThaiGoc = getTrangThaiHienThi(banGoc, LocalTime.now());
            applyTableStyle(btn, trangThaiGoc); 
        }
        selectedBanList.clear();
        selectedButtonList.clear();
        updateSelectionLabels();
        
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

            java.sql.Timestamp ts = java.sql.Timestamp.valueOf(ngay.atTime(gio));
            List<Ban> tatCaBan = datBanDAO.getAllBan(); 
            List<Ban> banTrongTaiGioTim = datBanDAO.getBanTrongTheoGio(ts);
            Set<String> maBanTrong = banTrongTaiGioTim.stream().map(Ban::getMaBan).collect(Collectors.toSet());
            
            List<Ban> banHienThi = new ArrayList<>();
            
            for (Ban ban : tatCaBan) {
                
                Ban banMoi = new Ban(ban.getMaBan(), ban.getViTri(), ban.getSucChua(), ban.getLoaiBan(), ban.getTrangThai());
                
                if (!maBanTrong.contains(ban.getMaBan())) {
                    banMoi.setTrangThai(TrangThaiBan.DA_DAT); 
                } else {
                    TrangThaiBan trangThaiHienThiTaiGioTim = getTrangThaiHienThi(ban, gio);
                    
                    if (trangThaiHienThiTaiGioTim == TrangThaiBan.DA_DAT || trangThaiHienThiTaiGioTim == TrangThaiBan.DANG_SU_DUNG) {
                         banMoi.setTrangThai(trangThaiHienThiTaiGioTim); 
                    } else {
                         banMoi.setTrangThai(TrangThaiBan.TRONG); 
                    }
                }
                
                banHienThi.add(banMoi);
            }
            
            loadBookingCards(); 
            loadTableGridsBase(banHienThi); 

        } catch (Exception ex) {
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tìm bàn trống. Kiểm tra dữ liệu đầu vào hoặc kết nối DB.");
            loadTableGrids(); 
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
        
        if (currentHoaDon.getTrangThai().equals("DaThanhToan")) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Hóa đơn này đã được thanh toán.");
            return;
        }

        monOrderList.clear();
        if (currentHoaDon.getMaHD() != null) {
            ObservableList<MonOrder> chiTiet = datBanDAO.getChiTietHoaDon(currentHoaDon.getMaHD());
            monOrderList.addAll(chiTiet);
        }
        tblMonDaChon.refresh();
        
        calculateTotal(); 

        if (vboxReceipt != null) {
            vboxReceipt.setVisible(true);
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
}
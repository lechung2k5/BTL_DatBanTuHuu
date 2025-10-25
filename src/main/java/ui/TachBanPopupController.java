package ui;

import dao.DatBanDAO;
import entity.Ban;
import entity.HoaDon;
import entity.TrangThaiBan;
import entity.TrangThaiHoaDon;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TachBanPopupController {

    // ==========================================================
    // FXML FIELDS (ĐÃ DỌN DẸP)
    // ==========================================================
    @FXML private Label lblMaHDGoc;
    
    @FXML private TableView<MonTach> tblMonAnGoc;
    @FXML private TableColumn<MonTach, String> colTenMonGoc;
    @FXML private TableColumn<MonTach, Number> colSLHienTai;
    @FXML private TableColumn<MonTach, Number> colSLTach;
    
    @FXML private TableView<MonTach> tblMonAnMoi;
    @FXML private TableColumn<MonTach, String> colTenMonMoi;
    @FXML private TableColumn<MonTach, Number> colSLMoi;
    
    // NÚT CHUYỂN MÓN (TRUNG TÂM)
    @FXML private Button btnChuyenChon; // Nút chuyển 1 món (>)
    @FXML private Button btnHuyChuyen; // Nút hủy chuyển 1 món (<)
    
    // NÚT THANH TOÁN
    @FXML private Button btnThanhToanGoc;
    @FXML private Button btnThanhToanMoi;
    
    @FXML private Button btnHuy;

    @FXML private Label lblTongTienMoi;
    

    // ==========================================================
    // DATA & CONTROLLERS
    // ==========================================================
    private HoaDon hoaDonGoc;
    private DatBanDAO datBanDAO;
    private DatBan mainController; 
    
    private ObservableList<MonTach> allMonTach = FXCollections.observableArrayList();
    private ObservableList<MonTach> monTachList = FXCollections.observableArrayList();
    private ObservableList<MonTach> monMoiList = FXCollections.observableArrayList(); 

    // ==========================================================
    // INITIALIZATION & SETUP
    // ==========================================================
    @FXML
    private void initialize() {
        setupTableViews();
        setupNutActions();
        
        btnHuy.setOnAction(e -> closePopup());
    }

    /**
     * Nhận dữ liệu ban đầu từ màn hình DatBan.
     */
    public void setInitialData(HoaDon hdGoc, DatBanDAO dao, DatBan mainCtrl) {
        this.hoaDonGoc = hdGoc;
        this.datBanDAO = dao;
        this.mainController = mainCtrl;

        lblMaHDGoc.setText(hdGoc.getMaHD() + " (Bàn " + hdGoc.getBan().getMaBan() + ")");
        
        loadMonTachList(hdGoc.getMaHD());
        
        updateButtonState(); 
    }
    
    /**
     * Tải danh sách món ăn từ HĐ gốc và chuyển thành ViewModel MonTach.
     */
    private void loadMonTachList(String maHD) {
        // Giả định DatBanDAO có hàm getChiTietHoaDon(String maHD) trả về MonOrder
        ObservableList<DatBan.MonOrder> initialOrder = datBanDAO.getChiTietHoaDon(maHD);
        
        allMonTach.clear();
        for (DatBan.MonOrder order : initialOrder) {
            MonTach mt = MonTach.fromMonOrder(order);
            allMonTach.add(mt);
        }
        
        updateLists();
        tblMonAnGoc.refresh();
    }
    
    /**
     * Thiết lập cấu trúc cho TableView.
     */
    private void setupTableViews() {
        // Bảng Gốc
        colTenMonGoc.setCellValueFactory(cell -> cell.getValue().tenMonProperty());
        colSLHienTai.setCellValueFactory(cell -> (ObservableValue<Number>) cell.getValue().soLuongConLaiProperty());
        colSLTach.setCellValueFactory(cell -> (ObservableValue<Number>) cell.getValue().soLuongTachProperty());
        
        tblMonAnGoc.setItems(monTachList);

        // Bảng Mới
        colTenMonMoi.setCellValueFactory(cell -> cell.getValue().tenMonProperty());
        colSLMoi.setCellValueFactory(cell -> (ObservableValue<Number>) cell.getValue().soLuongTachProperty());
        
        tblMonAnMoi.setItems(monMoiList);
    }

    // ==========================================================
    // ACTION HANDLERS (TÁCH MÓN)
    // ==========================================================

    /**
     * Xử lý tăng giảm số lượng món muốn tách.
     */
    private void handleTangGiamSL(MonTach mon, int delta) {
        int currentTach = mon.getSoLuongTach();
        int maxGoc = mon.getSoLuongGoc();
        
        int newTach = currentTach + delta;
        
        if (newTach >= 0 && newTach <= maxGoc) {
            
            mon.setSoLuongTach(newTach);
            
            updateLists();
            calculateTotalMoi();
            tblMonAnGoc.refresh(); 
            tblMonAnMoi.refresh(); 
        }
        updateButtonState();
    }
    
    /**
     * Xử lý hành động chuyển món giữa hai bảng.
     */
    private void setupNutActions() {
        // Nút Chuyển Chọn (>) - Tăng 1 đơn vị
        btnChuyenChon.setOnAction(e -> {
            MonTach selectedMon = tblMonAnGoc.getSelectionModel().getSelectedItem();
            if (selectedMon != null) {
                int slConLaiTrongGoc = selectedMon.getSoLuongConLai();
                
                if (slConLaiTrongGoc > 0) {
                    // Chuyển 1 món sang (delta = 1)
                    handleTangGiamSL(selectedMon, 1); 
                } else {
                    showAlert(AlertType.WARNING, "Lỗi", "Món này đã được chuyển hết.");
                }
            } else {
                 showAlert(AlertType.WARNING, "Chọn món", "Vui lòng chọn một món trong bảng gốc có số lượng còn lại.");
            }
        });
        
        // Nút Hủy Chuyển (<) - Giảm 1 đơn vị
        btnHuyChuyen.setOnAction(e -> {
            MonTach selectedMon = tblMonAnMoi.getSelectionModel().getSelectedItem(); 
            if (selectedMon != null) {
                // Giảm 1 món (delta = -1)
                handleTangGiamSL(selectedMon, -1);
            } else {
                 showAlert(AlertType.WARNING, "Chọn món", "Vui lòng chọn một món trong bảng mới để hủy chuyển.");
            }
        });
        
        // NÚT THANH TOÁN (Logic giữ nguyên, giả định FXML fields tồn tại)
        if (btnThanhToanGoc != null) {
            // FIX: Nút Thanh toán Gốc chỉ cần kiểm tra còn món trong HĐ gốc hay không
            btnThanhToanGoc.setOnAction(e -> handleThanhToanGopTach(false));
        }
        if (btnThanhToanMoi != null) {
            // FIX: Nút Thanh toán Mới chỉ cần kiểm tra có món được tách ra hay không
            btnThanhToanMoi.setOnAction(e -> handleThanhToanGopTach(true));
        }
    }


    /**
     * Cập nhật danh sách hiển thị cho hai bảng từ allMonTach.
     */
    private void updateLists() {
        // Lọc cho bảng gốc (chỉ hiển thị món có SL còn lại > 0)
        monTachList.setAll(allMonTach.stream()
            .filter(m -> m.getSoLuongConLai() > 0)
            .sorted(Comparator.comparing(MonTach::getTenMon))
            .collect(Collectors.toList()));
            
        // Lọc cho bảng mới (chỉ hiển thị món có SL tách > 0)
        monMoiList.setAll(allMonTach.stream()
            .filter(m -> m.getSoLuongTach() > 0)
            .sorted(Comparator.comparing(MonTach::getTenMon))
            .collect(Collectors.toList()));
    }


    /**
     * Tính toán tổng tiền của các món đã tách.
     */
    private void calculateTotalMoi() {
        double total = monMoiList.stream()
            .mapToDouble(m -> m.getDonGia() * m.getSoLuongTach())
            .sum();
        lblTongTienMoi.setText(String.format("Tổng tiền món mới: %,.0f Đ", total));
    }

    /**
     * Cập nhật trạng thái nút Xác nhận Tách.
     */
    private void updateButtonState() {
        boolean monDuocChon = monMoiList.stream().anyMatch(m -> m.getSoLuongTach() > 0);
        
        // Nút Thanh toán chỉ được enable sau khi có món được tách
        if (btnThanhToanMoi != null) btnThanhToanMoi.setDisable(!monDuocChon);
        
        // Nút Thanh toán gốc luôn được enable nếu HĐ gốc có món
        if (btnThanhToanGoc != null) {
            boolean monConLai = monTachList.stream().anyMatch(m -> m.getSoLuongConLai() > 0);
            btnThanhToanGoc.setDisable(!monConLai);
        }
    }

    // ==========================================================
    // XỬ LÝ THANH TOÁN GỘP TÁCH (GOM LOGIC TÁCH VÀO THANH TOÁN)
    // ==========================================================
    
    /**
     * 🔥 HÀM MỚI: Xử lý TÁCH MÓN VÀ THANH TOÁN GỘP (ĐƯỢC GỌI BỞI NÚT THANH TOÁN)
     */
    private void handleThanhToanGopTach(boolean thanhToanMoi) {
        if (monMoiList.isEmpty() && thanhToanMoi) {
             showAlert(AlertType.ERROR, "Lỗi", "Không có món nào được tách để thanh toán Hóa đơn mới.");
             return;
        }

        // Kiểm tra xem HĐ gốc còn món không (nếu người dùng đã chuyển hết món)
        if (monTachList.isEmpty() && !thanhToanMoi) {
             showAlert(AlertType.ERROR, "Lỗi", "Không còn món nào trong Hóa đơn gốc để thanh toán.");
             return;
        }

        Optional<ButtonType> result = showAlertConfirm("Xác nhận Thanh toán", 
            "Bạn có chắc muốn TÁCH MÓN và Thanh toán " + (thanhToanMoi ? "Hóa đơn PHỤ" : "Hóa đơn GỐC") + " bằng Tiền Mặt?");

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // 1. Thực hiện Transaction Tách Món (Tạo HD phụ, cập nhật CTHD gốc)
                // Hóa đơn mới được tạo
                HoaDon hdMoiDuocTao = thucHienTransactionTachBan(); 
                
                // 2. Lựa chọn HĐ cần thanh toán
                String maHDToPay = thanhToanMoi ? hdMoiDuocTao.getMaHD() : hoaDonGoc.getMaHD(); 
                
                // 3. Thanh toán HĐ đã chọn
                datBanDAO.capNhatTrangThaiHoaDon(maHDToPay, TrangThaiHoaDon.DA_THANH_TOAN.getDbValue(), true); // set gioRa = true
                
                // 4. Cleanup (Giải phóng bàn gốc nếu HĐ gốc được thanh toán)
                if (maHDToPay.equals(hoaDonGoc.getMaHD()) && hoaDonGoc.getBan() != null) {
                    datBanDAO.capNhatTrangThaiBan(hoaDonGoc.getBan().getMaBan(), TrangThaiBan.TRONG.getDbValue());
                }

                // 5. Refresh UI và Đóng
                mainController.loadBookingCards();
                mainController.loadTableGrids();
                closePopup();
                
                showAlert(AlertType.INFORMATION, "Thành công", "Đã Tách Món và Thanh toán Hóa đơn " + maHDToPay + ".");

            } catch (Exception e) {
                e.printStackTrace();
                showAlert(AlertType.ERROR, "Lỗi CSDL", "Không thể hoàn tất giao dịch Thanh toán/Tách: " + e.getMessage());
            }
        }
    }
    
    /**
     * Thực hiện giao dịch Tách Bàn (Tạo HĐ mới, cập nhật HĐ cũ).
     */
    private HoaDon thucHienTransactionTachBan() throws SQLException {
        // 1. TẠO HÓA ĐƠN PHỤ MỚI (Trạng thái HoaDonTam, KHÔNG CÓ BÀN)
        HoaDon hdMoi = new HoaDon();
        hdMoi.setNgayLap(LocalDateTime.now());
        hdMoi.setGioVao(hoaDonGoc.getGioVao());
        hdMoi.setKhachHang(hoaDonGoc.getKhachHang());
        hdMoi.setBan(null); // KHÔNG GÁN BÀN MỚI
        hdMoi.setTienCoc(0); 
        hdMoi.setMaHDGoc(hoaDonGoc.getMaHD());
        hdMoi.setTrangThai(TrangThaiHoaDon.HOA_DON_TAM.getDbValue());

        // Lưu Hóa đơn Mới (chưa có CTHD)
        datBanDAO.luuHoaDonVaChiTiet(hdMoi, FXCollections.observableArrayList()); 

        // 2. CẬP NHẬT/CHÈN MÓN QUA DAO
        for (MonTach mon : allMonTach) {
            if (mon.getSoLuongTach() > 0) {
                int slMoiConLai = mon.getSoLuongGoc() - mon.getSoLuongTach();
                double donGia = mon.getDonGia(); // Lấy đơn giá để tính thành tiền

                // Cập nhật CTHD Gốc (Giảm số lượng món)
                if (slMoiConLai == 0) {
                    datBanDAO.xoaChiTietHoaDon(hoaDonGoc.getMaHD(), mon.getMaMon());
                } else {
                    datBanDAO.capNhatSoLuongCTHD(hoaDonGoc.getMaHD(), mon.getMaMon(), slMoiConLai, donGia);
                }
                
                // Thêm CTHD Mới (Chèn số lượng món đã tách vào HĐ Mới)
                datBanDAO.themChiTietHoaDon(hdMoi.getMaHD(), mon.getMaMon(), mon.getSoLuongTach(), donGia);
            }
        }
        
        // 3. CẬP NHẬT TRẠNG THÁI HÀNH CHÍNH (Nếu HĐ Gốc không còn món)
        boolean conMonConLai = allMonTach.stream()
                                          .anyMatch(m -> m.getSoLuongConLai() > 0);
        
        if (!conMonConLai) {
            // Nếu không còn món nào trong HĐ Gốc, chuyển HĐ Gốc sang trạng thái Phụ
            datBanDAO.capNhatTrangThaiHoaDon(hoaDonGoc.getMaHD(), TrangThaiHoaDon.HOA_DON_TAM.getDbValue(), false);
        }
        
        return hdMoi; // Trả về Hóa đơn mới được tạo
    }


    // ==========================================================
    // UTILITIES
    // ==========================================================
    private void closePopup() {
        Stage stage = (Stage) btnHuy.getScene().getWindow();
        stage.close();
    }
    private Optional<ButtonType> showAlert(AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        return alert.showAndWait();
    }
    private Optional<ButtonType> showAlertConfirm(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        return alert.showAndWait();
    }
    
    // ==========================================================
    // INNER STATIC CLASS: VIEW MODEL MON TÁCH
    // ==========================================================
    public static class MonTach {
        private final SimpleStringProperty maMon;
        private final SimpleStringProperty tenMon;
        private final SimpleDoubleProperty donGia;
        private final SimpleIntegerProperty soLuongGoc;      // SL ban đầu trong HĐ cũ (Tổng SL)
        private final SimpleIntegerProperty soLuongTach;     // SL muốn tách sang HĐ mới
        private final SimpleIntegerProperty soLuongConLai;   // SL còn lại (soLuongGoc - soLuongTach)

        public MonTach(String maMon, String tenMon, double donGia, int soLuongGoc) {
            this.maMon = new SimpleStringProperty(maMon);
            this.tenMon = new SimpleStringProperty(tenMon);
            this.donGia = new SimpleDoubleProperty(donGia);
            this.soLuongGoc = new SimpleIntegerProperty(soLuongGoc);
            this.soLuongTach = new SimpleIntegerProperty(0);
            this.soLuongConLai = new SimpleIntegerProperty(soLuongGoc);
            
            // Lắng nghe sự kiện thay đổi soLuongTach để cập nhật soLuongConLai
            this.soLuongTach.addListener((obs, oldVal, newVal) -> {
                this.soLuongConLai.set(this.soLuongGoc.get() - newVal.intValue());
            });
        }
        
        // --- Getters and Setters for Properties ---
        public String getMaMon() { return maMon.get(); }
        public String getTenMon() { return tenMon.get(); }
        public SimpleStringProperty tenMonProperty() { return tenMon; }
        public double getDonGia() { return donGia.get(); }
        public SimpleDoubleProperty donGiaProperty() { return donGia; }
        
        public int getSoLuongGoc() { return soLuongGoc.get(); }
        public SimpleIntegerProperty soLuongGocProperty() { return soLuongGoc; }
        
        public int getSoLuongTach() { return soLuongTach.get(); }
        public void setSoLuongTach(int soLuongTach) { this.soLuongTach.set(soLuongTach); }
        public SimpleIntegerProperty soLuongTachProperty() { return soLuongTach; }
        
        // <<< THUỘC TÍNH MỚI CHO HIỂN THỊ SỐ LƯỢNG CÒN LẠI >>>
        public int getSoLuongConLai() { return soLuongConLai.get(); }
        public SimpleIntegerProperty soLuongConLaiProperty() { return soLuongConLai; }
        // ---------------------------------------------------
        
        public static MonTach fromMonOrder(DatBan.MonOrder order) {
            return new MonTach(order.getMaMon(), order.getTenMon(), order.getDonGia(), order.getSoLuong());
        }
    }
}
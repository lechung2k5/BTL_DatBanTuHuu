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
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
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
            btnThanhToanGoc.setOnAction(e -> handleThanhToanGopTach(false));
        }
        if (btnThanhToanMoi != null) {
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
     * 🔥 HÀM ĐÃ SỬA: Xử lý TÁCH MÓN VÀ MỞ POPUP PREVIEW THANH TOÁN
     * LƯU Ý: KHÔNG GỌI CSDL CHO ĐẾN KHI NÚT XÁC NHẬN CUỐI CÙNG TRONG PREVIEW ĐƯỢC NHẤN.
     */
    private void handleThanhToanGopTach(boolean thanhToanMoi) {
        if (monMoiList.isEmpty() && thanhToanMoi) {
             showAlert(AlertType.ERROR, "Lỗi", "Không có món nào được tách để thanh toán Hóa đơn mới.");
             return;
        }

        if (monTachList.isEmpty() && !thanhToanMoi) {
             showAlert(AlertType.ERROR, "Lỗi", "Không còn món nào trong Hóa đơn gốc để thanh toán.");
             return;
        }
        
        Optional<ButtonType> result = showAlertConfirm("Xác nhận Tách Món", 
            "Bạn có chắc muốn thực hiện TÁCH MÓN VÀ thanh toán? Thao tác này sẽ chuẩn bị giao dịch.");

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // 1. TÍNH TOÁN DỮ LIỆU TÁCH MỚI (chỉ trong bộ nhớ)
                // Hóa đơn Mới (tạm thời) sẽ được tạo mã HD ngẫu nhiên để Preview
                HoaDon hdMoiTam = new HoaDon();
                // 🔥 Sửa: Không cần gọi getNextMaHD() ở đây, chỉ cần tạo mã tạm.
                hdMoiTam.setMaHD("TMP" + "001"); // Mã tạm
                
                // 2. Lựa chọn HĐ cần thanh toán và thông tin tiền cọc
                HoaDon hdToPreview = thanhToanMoi ? hdMoiTam : hoaDonGoc;
                
                // 3. Tính toán tổng tiền món ăn (cho Preview)
                double tongMonAn;
                if (thanhToanMoi) {
                     tongMonAn = monMoiList.stream().mapToDouble(m -> m.getDonGia() * m.getSoLuongTach()).sum();
                } else {
                     // Nếu thanh toán HĐ gốc: tính tổng số lượng món còn lại
                     tongMonAn = monTachList.stream().mapToDouble(m -> m.getDonGia() * m.getSoLuongConLai()).sum();
                }
                
                // 4. MỞ POPUP XÁC NHẬN THANH TOÁN CUỐI CÙNG
                // Tiền cọc gốc chỉ được áp dụng nếu HĐ gốc được thanh toán (thanhToanMoi = false)
                openThanhToanPreviewPopup(hdToPreview, tongMonAn, hoaDonGoc.getTienCoc(), thanhToanMoi); 
                
                // Sau khi Popup Thanh toán đóng, cần đóng Popup Tách Bàn (vì giao dịch đã hoàn tất)
                closePopup(); 

            } catch (Exception e) {
                e.printStackTrace();
                showAlert(AlertType.ERROR, "Lỗi CSDL", "Không thể chuẩn bị giao dịch Tách: " + e.getMessage());
            }
        }
    }

    /**
     * 🔥 HÀM MỚI: Mở Popup Thanh toán Preview.
     */
    private void openThanhToanPreviewPopup(HoaDon hdToPay, double tongMonAn, double tienCocGoc, boolean isNewInvoice) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ThanhToanPreview_Popup.fxml"));
        VBox root = loader.load();
        
        ThanhToanPreviewController controller = loader.getController();

     // 🔥 BƯỚC 1: TRUYỀN DỮ LIỆU TÁCH (SNAPSHOT) TRƯỚC
	     ObservableList<MonTach> monTachListSnapshot = FXCollections.observableArrayList(allMonTach);
	     controller.setMonTachList(monTachListSnapshot, hoaDonGoc.getMaHD()); //
	
	     // 🔥 BƯỚC 2: GỌI SETINITIALDATA SAU (Để nó có thể sử dụng dữ liệu vừa truyền)
	     controller.setInitialData(hdToPay, datBanDAO, mainController, tongMonAn, tienCocGoc, isNewInvoice); 
	
	     Stage popupStage = new Stage(); 


      
        popupStage.setTitle("Xác nhận Thanh toán: " + hdToPay.getMaHD());
        popupStage.setScene(new Scene(root));
        popupStage.showAndWait();
    }
	/**
     * Thực hiện giao dịch Tách Bàn (Tạo HĐ mới, cập nhật HĐ cũ).
     * 🔥 HÀM NÀY ĐÃ BỊ XÓA KHỎI CONTROLLER, CẦN CHUYỂN LOGIC NÀY VÀO DAO!
     */
    /*
    private HoaDon thucHienTransactionTachBan() throws SQLException {
       // ... (Logic cũ bị loại bỏ)
    }
    */


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
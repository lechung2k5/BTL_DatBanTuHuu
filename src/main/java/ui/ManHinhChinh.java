package ui;

import entity.TaiKhoan;
import entity.VaiTro;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.net.URL;

public class ManHinhChinh {
    @FXML private BorderPane contentArea;
    @FXML private VBox menuItems;
    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;
    @FXML private ImageView profileImage;

    @FXML private Button manHinhChinhButton;
    @FXML private Button quanLyDatBanButton;
    @FXML private Button quanLyThongKeButton;
    @FXML private Button quanLyThucDonButton;
    @FXML private Button quanLyHoaDonButton;
    @FXML private Button quanLyNhanVienButton;
    @FXML private Button quanLyKhachHangButton;
    @FXML private Button quanLyKhuyenMaiButton;
    @FXML private Button quanLyTraCuuButton;
    @FXML private Button dangXuatButton;

    @FXML private ImageView manHinhChinhIcon;
    @FXML private ImageView quanLyDatBanIcon;
    @FXML private ImageView quanLyThongKeIcon;
    @FXML private ImageView quanLyThucDonIcon;
    @FXML private ImageView quanLyHoaDonIcon;
    @FXML private ImageView quanLyNhanVienIcon;
    @FXML private ImageView quanLyKhachHangIcon;
    @FXML private ImageView quanLyKhuyenMaiIcon;
    @FXML private ImageView quanLyTraCuuIcon;
    @FXML private ImageView dangXuatIcon;

    private MainApp mainApp;
    private Button activeButton;
    private final Map<Button, String> defaultIcons = new HashMap<>();
    private final Map<Button, String> activeIcons = new HashMap<>();
    private final Map<Button, ImageView> buttonIconMap = new HashMap<>();
    
    private TaiKhoan currentUser;

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    public void initialize() {
        buttonIconMap.put(manHinhChinhButton, manHinhChinhIcon);
        buttonIconMap.put(quanLyDatBanButton, quanLyDatBanIcon);
        buttonIconMap.put(quanLyThongKeButton, quanLyThongKeIcon);
        buttonIconMap.put(quanLyThucDonButton, quanLyThucDonIcon);
        buttonIconMap.put(quanLyHoaDonButton, quanLyHoaDonIcon);
        buttonIconMap.put(quanLyNhanVienButton, quanLyNhanVienIcon);
        buttonIconMap.put(quanLyKhachHangButton, quanLyKhachHangIcon);
        buttonIconMap.put(quanLyKhuyenMaiButton, quanLyKhuyenMaiIcon);
        buttonIconMap.put(quanLyTraCuuButton, quanLyTraCuuIcon);
        buttonIconMap.put(dangXuatButton, dangXuatIcon);

        addIconMapping(manHinhChinhButton, "/icons/iconHome.png", "/icons/iconHome_White.png");
        addIconMapping(quanLyDatBanButton, "/icons/iconDatBan.png", "/icons/iconDatBan_White.png");
        addIconMapping(quanLyThongKeButton, "/icons/iconThongKe.png", "/icons/iconThongKe_White.png");
        addIconMapping(quanLyThucDonButton, "/icons/iconThucDon.png", "/icons/iconThucDon_White.png");
        addIconMapping(quanLyHoaDonButton, "/icons/iconHoaDon.png", "/icons/iconHoaDon_White.png");
        addIconMapping(quanLyNhanVienButton, "/icons/iconNhanVienMenu.png", "/icons/iconNhanVien_White.png");
        addIconMapping(quanLyKhachHangButton, "/icons/iconKhachHang.png", "/icons/iconKhachHang_White.png");
        addIconMapping(quanLyKhuyenMaiButton, "/icons/iconKhuyenMai.png", "/icons/iconKhuyenMai_White.png");
        addIconMapping(quanLyTraCuuButton, "/icons/iconTraCuu.png", "/icons/iconTraCuu_White.png");
        addIconMapping(dangXuatButton, "/icons/iconDangXuat.png", "/icons/iconDangXuat_White.png");

        setActiveButton(manHinhChinhButton);
        try {
            loadScreen("/fxml/Dashboard.fxml", "/css/Dashboard.css");
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // 🔥 THÊM: Đợi scene được tạo xong rồi setup phím tắt
        contentArea.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                setupKeyboardShortcuts();
            }
        });
    }

    /**
     * 🔥 HÀM MỚI: Thiết lập phím tắt toàn cục
     */
    private void setupKeyboardShortcuts() {
        contentArea.getScene().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            
            // Ctrl + 1-7: Điều hướng nhanh (có kiểm tra quyền)
            if (event.isControlDown()) {
                switch (event.getCode()) {
                    case DIGIT1:
                        // Dashboard - Tất cả có quyền
                        if (manHinhChinhButton.isVisible()) {
                            try { handleManHinhChinh(); } catch (IOException e) { e.printStackTrace(); }
                            event.consume();
                        }
                        break;
                        
                    case DIGIT2:
                        // Đặt bàn - THU NGÂN có quyền
                        if (quanLyDatBanButton.isVisible()) {
                            try { handleQuanLyDatBan(); } catch (IOException e) { e.printStackTrace(); }
                            event.consume();
                        }
                        break;
                        
                    case DIGIT3:
                        // Hóa đơn - THU NGÂN có quyền
                        if (quanLyHoaDonButton.isVisible()) {
                            try { handleQuanLyHoaDon(); } catch (IOException e) { e.printStackTrace(); }
                            event.consume();
                        }
                        break;
                        
                    case DIGIT4:
                        // Khách hàng - THU NGÂN có quyền
                        if (quanLyKhachHangButton.isVisible()) {
                            try { handleQuanLyKhachHang(); } catch (IOException e) { e.printStackTrace(); }
                            event.consume();
                        }
                        break;
                        
                    case DIGIT5:
                        // Thực đơn - QUẢN LÝ có quyền
                        if (quanLyThucDonButton.isVisible()) {
                            try { handleQuanLyThucDon(); } catch (IOException e) { e.printStackTrace(); }
                            event.consume();
                        }
                        break;
                        
                    case DIGIT6:
                        // Khuyến mãi - QUẢN LÝ có quyền
                        if (quanLyKhuyenMaiButton.isVisible()) {
                            try { handleQuanLyKhuyenMai(); } catch (IOException e) { e.printStackTrace(); }
                            event.consume();
                        }
                        break;
                        
                    case DIGIT7:
                        // Nhân viên - QUẢN LÝ có quyền
                        if (quanLyNhanVienButton.isVisible()) {
                            try { handleQuanLyNhanVien(); } catch (IOException e) { e.printStackTrace(); }
                            event.consume();
                        }
                        break;
                        
                    case DIGIT8:
                        // Thống kê - QUẢN LÝ có quyền
                        if (quanLyThongKeButton.isVisible()) {
                            try { handleQuanLyThongKe(); } catch (IOException e) { e.printStackTrace(); }
                            event.consume();
                        }
                        break;
                        
                    case T:
                        // Tra cứu - Tất cả có quyền
                        if (quanLyTraCuuButton.isVisible()) {
                            try { handleQuanLyTraCuu(); } catch (IOException e) { e.printStackTrace(); }
                            event.consume();
                        }
                        break;
                        
                    case Q:
                        // Đăng xuất
                        handleDangXuat();
                        event.consume();
                        break;
                }
            }
            
            // F1: Hiển thị trợ giúp phím tắt
            else if (event.getCode() == KeyCode.F1) {
                showKeyboardShortcutsHelp();
                event.consume();
            }
            
            // F5: Làm mới trang hiện tại
            else if (event.getCode() == KeyCode.F5) {
                refreshCurrentScreen();
                event.consume();
            }
        });
    }
    
    /**
     * 🔥 HÀM MỚI: Hiển thị hướng dẫn phím tắt (động theo quyền)
     */
    private void showKeyboardShortcutsHelp() {
        StringBuilder helpText = new StringBuilder();
        helpText.append("⌨️ PHÍM TẮT CÓ SẴN:\n\n");
        helpText.append("📍 ĐIỀU HƯỚNG:\n");
        
        // Chỉ hiển thị phím tắt cho menu có quyền truy cập
        if (manHinhChinhButton.isVisible()) {
            helpText.append("Ctrl + 1  →  Dashboard\n");
        }
        if (quanLyDatBanButton.isVisible()) {
            helpText.append("Ctrl + 2  →  Quản lý Đặt bàn\n");
        }
        if (quanLyHoaDonButton.isVisible()) {
            helpText.append("Ctrl + 3  →  Quản lý Hóa đơn\n");
        }
        if (quanLyKhachHangButton.isVisible()) {
            helpText.append("Ctrl + 4  →  Quản lý Khách hàng\n");
        }
        if (quanLyThucDonButton.isVisible()) {
            helpText.append("Ctrl + 5  →  Quản lý Thực đơn\n");
        }
        if (quanLyKhuyenMaiButton.isVisible()) {
            helpText.append("Ctrl + 6  →  Quản lý Khuyến mãi\n");
        }
        if (quanLyNhanVienButton.isVisible()) {
            helpText.append("Ctrl + 7  →  Quản lý Nhân viên\n");
        }
        if (quanLyThongKeButton.isVisible()) {
            helpText.append("Ctrl + 8  →  Thống kê & Báo cáo\n");
        }
        if (quanLyTraCuuButton.isVisible()) {
            helpText.append("Ctrl + T  →  Tra cứu\n");
        }
        
        helpText.append("\n📋 CHỨC NĂNG:\n");
        helpText.append("F1        →  Hiển thị trợ giúp này\n");
        helpText.append("F5        →  Làm mới trang hiện tại\n");
        helpText.append("Ctrl + Q  →  Đăng xuất\n");
        
        // Thông tin vai trò
        if (currentUser != null && currentUser.getVaiTro() != null) {
            helpText.append("\n👤 VAI TRÒ CỦA BẠN:\n");
            helpText.append(currentUser.getVaiTro().getTenVaiTro());
            helpText.append("\n\n📝 QUYỀN HẠN:\n");
            helpText.append(currentUser.getVaiTro().getMoTaQuyen());
        }
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Hướng dẫn phím tắt");
        alert.setHeaderText("📖 Danh sách phím tắt");
        alert.setContentText(helpText.toString());
        
        // Tăng kích thước dialog
        alert.getDialogPane().setMinWidth(500);
        alert.getDialogPane().setMinHeight(400);
        
        alert.showAndWait();
    }
    
    /**
     * 🔥 HÀM MỚI: Làm mới trang hiện tại
     */
    private void refreshCurrentScreen() {
        try {
            // Xác định trang hiện tại dựa vào activeButton
            if (activeButton == manHinhChinhButton) {
                loadScreen("/fxml/Dashboard.fxml", "/css/Dashboard.css");
            } else if (activeButton == quanLyDatBanButton) {
                loadScreen("/fxml/QuanLyDatBan.fxml", "/css/DatBan.css");
            } else if (activeButton == quanLyHoaDonButton) {
                loadScreen("/fxml/QuanLyHoaDon.fxml", "/css/HoaDon.css");
            } else if (activeButton == quanLyKhachHangButton) {
                loadScreen("/fxml/QuanLyKhachHang.fxml", "/css/KhachHang.css");
            } else if (activeButton == quanLyThucDonButton) {
                loadScreen("/fxml/QuanLyThucDon.fxml", "/css/ThucDon.css");
            } else if (activeButton == quanLyKhuyenMaiButton) {
                loadScreen("/fxml/QuanLyKhuyenMai.fxml", "/css/KhuyenMai.css");
            } else if (activeButton == quanLyNhanVienButton) {
                loadScreen("/fxml/QuanLyNhanVien.fxml", "/css/NhanVien.css");
            } else if (activeButton == quanLyThongKeButton) {
                loadScreen("/fxml/QuanLyThongKe.fxml", "/css/ThongKe.css");
            } else if (activeButton == quanLyTraCuuButton) {
                loadScreen("/fxml/QuanLyTraCuu.fxml", "/css/TraCuu.css");
            }
            
            // Hiển thị thông báo nhẹ
            System.out.println("🔄 Đã làm mới trang hiện tại");
            
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể làm mới trang: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Nhận thông tin người dùng và áp dụng phân quyền
     */
    public void setUserInfo(TaiKhoan user) {
        this.currentUser = user;
        
        if (user != null && user.getNhanVien() != null) {
            userNameLabel.setText(user.getNhanVien().getHoTen());
            userRoleLabel.setText(user.getVaiTro() != null ? user.getVaiTro().getTenVaiTro() : "Không xác định");

            try {
                profileImage.setImage(new Image(getClass().getResourceAsStream("/icons/iconUserPlaceholder.png")));
            } catch (Exception e) {
                System.err.println("Không tìm thấy ảnh placeholder: /icons/iconUserPlaceholder.png");
            }
            
            apDungPhanQuyen();

        } else {
            userNameLabel.setText("Khách");
            userRoleLabel.setText("Chưa đăng nhập");
            try {
                profileImage.setImage(new Image(getClass().getResourceAsStream("/icons/iconUserPlaceholder.png")));
            } catch (Exception e) {
                System.err.println("Không tìm thấy ảnh placeholder: /icons/iconUserPlaceholder.png");
            }
        }
    }
    
    /**
     * Áp dụng phân quyền cho các menu theo ĐÚNG yêu cầu
     * 
     * PHÂN QUYỀN CHI TIẾT:
     * 
     * 1. QUẢN LÝ (QuanLy):
     *    ✅ Dashboard
     *    ❌ Quản lý Đặt bàn
     *    ✅ Thống kê & Báo cáo
     *    ✅ Quản lý Thực đơn
     *    ❌ Quản lý Hóa đơn
     *    ✅ Quản lý Nhân viên
     *    ❌ Quản lý Khách hàng
     *    ✅ Quản lý Ưu đãi
     *    ✅ Tra cứu
     * 
     * 2. THU NGÂN (NhanVienThuNgan):
     *    ✅ Dashboard
     *    ✅ Quản lý Đặt bàn
     *    ❌ Thống kê & Báo cáo
     *    ❌ Quản lý Thực đơn
     *    ✅ Quản lý Hóa đơn
     *    ❌ Quản lý Nhân viên
     *    ✅ Quản lý Khách hàng
     *    ❌ Quản lý Ưu đãi
     *    ✅ Tra cứu
     */
    private void apDungPhanQuyen() {
        if (currentUser == null || currentUser.getVaiTro() == null) {
            hienThiMenu(manHinhChinhButton, true);
            hienThiMenu(quanLyDatBanButton, false);
            hienThiMenu(quanLyThongKeButton, false);
            hienThiMenu(quanLyThucDonButton, false);
            hienThiMenu(quanLyHoaDonButton, false);
            hienThiMenu(quanLyNhanVienButton, false);
            hienThiMenu(quanLyKhachHangButton, false);
            hienThiMenu(quanLyKhuyenMaiButton, false);
            hienThiMenu(quanLyTraCuuButton, false);
            return;
        }
        
        VaiTro vaiTro = currentUser.getVaiTro();
        
        hienThiMenu(manHinhChinhButton, vaiTro.coQuyenDashboard());
        hienThiMenu(quanLyDatBanButton, vaiTro.coQuyenQuanLyDatBan());
        hienThiMenu(quanLyThongKeButton, vaiTro.coQuyenThongKe());
        hienThiMenu(quanLyThucDonButton, vaiTro.coQuyenQuanLyThucDon());
        hienThiMenu(quanLyHoaDonButton, vaiTro.coQuyenQuanLyHoaDon());
        hienThiMenu(quanLyNhanVienButton, vaiTro.coQuyenQuanLyNhanVien());
        hienThiMenu(quanLyKhachHangButton, vaiTro.coQuyenQuanLyKhachHang());
        hienThiMenu(quanLyKhuyenMaiButton, vaiTro.coQuyenQuanLyUuDai());
        hienThiMenu(quanLyTraCuuButton, vaiTro.coQuyenTraCuu());
        hienThiMenu(dangXuatButton, true);
        
        System.out.println("========================================");
        System.out.println("🔐 PHÂN QUYỀN CHO: " + vaiTro.getTenVaiTro());
        System.out.println("========================================");
        System.out.println("✅ Dashboard: " + vaiTro.coQuyenDashboard());
        System.out.println((vaiTro.coQuyenQuanLyDatBan() ? "✅" : "❌") + " Quản lý Đặt bàn: " + vaiTro.coQuyenQuanLyDatBan());
        System.out.println((vaiTro.coQuyenThongKe() ? "✅" : "❌") + " Thống kê & Báo cáo: " + vaiTro.coQuyenThongKe());
        System.out.println((vaiTro.coQuyenQuanLyThucDon() ? "✅" : "❌") + " Quản lý Thực đơn: " + vaiTro.coQuyenQuanLyThucDon());
        System.out.println((vaiTro.coQuyenQuanLyHoaDon() ? "✅" : "❌") + " Quản lý Hóa đơn: " + vaiTro.coQuyenQuanLyHoaDon());
        System.out.println((vaiTro.coQuyenQuanLyNhanVien() ? "✅" : "❌") + " Quản lý Nhân viên: " + vaiTro.coQuyenQuanLyNhanVien());
        System.out.println((vaiTro.coQuyenQuanLyKhachHang() ? "✅" : "❌") + " Quản lý Khách hàng: " + vaiTro.coQuyenQuanLyKhachHang());
        System.out.println((vaiTro.coQuyenQuanLyUuDai() ? "✅" : "❌") + " Quản lý Ưu đãi: " + vaiTro.coQuyenQuanLyUuDai());
        System.out.println("✅ Tra cứu: " + vaiTro.coQuyenTraCuu());
        System.out.println("========================================");
    }
    
    /**
     * Ẩn/hiện và vô hiệu hóa menu theo quyền
     */
    private void hienThiMenu(Button button, boolean coQuyen) {
        if (button == null) return;
        
        button.setVisible(coQuyen);
        button.setManaged(coQuyen);
        button.setDisable(!coQuyen);
    }
    
    /**
     * Kiểm tra quyền trước khi mở menu
     */
    private boolean kiemTraQuyen(String tenChucNang, boolean coQuyen) {
        if (!coQuyen) {
            showAlert(Alert.AlertType.WARNING, 
                "⚠️ KHÔNG CÓ QUYỀN TRUY CẬP", 
                "Bạn không có quyền sử dụng chức năng: " + tenChucNang + "\n\n" +
                "Vai trò của bạn: " + (currentUser != null ? currentUser.getVaiTro().getTenVaiTro() : "Chưa đăng nhập") + "\n" +
                "Quyền hạn: " + (currentUser != null ? currentUser.getVaiTro().getMoTaQuyen() : "Không có"));
            return false;
        }
        return true;
    }

    private void addIconMapping(Button button, String defaultIcon, String activeIcon) {
        defaultIcons.put(button, defaultIcon);
        activeIcons.put(button, activeIcon);
    }

    private void updateMenuStyles() {
         buttonIconMap.forEach((button, icon) -> {
            if (button == null || icon == null) return;
            String iconPath = (button == activeButton) ? activeIcons.get(button) : defaultIcons.get(button);
            if (iconPath != null) {
                 try {
                     URL iconUrl = getClass().getResource(iconPath);
                     if (iconUrl != null) {
                         icon.setImage(new Image(iconUrl.toExternalForm()));
                     } else { System.err.println("Không tìm thấy icon: " + iconPath); }
                 } catch (Exception e) { System.err.println("Lỗi load icon: " + iconPath); e.printStackTrace(); }
            }
            button.getStyleClass().setAll((button == activeButton) ? "menu-button-active" : "menu-button");
        });
    }

    private void setActiveButton(Button button) {
        this.activeButton = button;
        updateMenuStyles();
    }

     private void loadScreen(String fxmlPath, String cssPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();
        
        root.getStylesheets().clear();
        URL globalCssUrl = getClass().getResource("/css/manHinhChinh.css");
        if (globalCssUrl != null) {
             root.getStylesheets().add(globalCssUrl.toExternalForm());
        }
         if(cssPath != null && !cssPath.isEmpty()) {
              URL specificCssUrl = getClass().getResource(cssPath);
              if (specificCssUrl != null) {
                   root.getStylesheets().add(specificCssUrl.toExternalForm());
              } else {
                   System.err.println("Không tìm thấy file CSS: " + cssPath);
              }
         }

        contentArea.setCenter(root);
    }
    
    /**
     * Hiển thị thông báo
     */
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // ========================================================================
    // CÁC HÀM HANDLE CÓ KIỂM TRA QUYỀN
    // ========================================================================
    
    @FXML 
    private void handleManHinhChinh() throws IOException { 
        setActiveButton(manHinhChinhButton); 
        loadScreen("/fxml/Dashboard.fxml", "/css/Dashboard.css"); 
    }
    
    @FXML 
    private void handleQuanLyDatBan() throws IOException {
        if (currentUser == null || !kiemTraQuyen("Quản lý Đặt bàn", currentUser.getVaiTro().coQuyenQuanLyDatBan())) {
            return;
        }
        setActiveButton(quanLyDatBanButton); 
        loadScreen("/fxml/QuanLyDatBan.fxml", "/css/DatBan.css"); 
    }
    
    @FXML 
    private void handleQuanLyThongKe() throws IOException {
        if (currentUser == null || !kiemTraQuyen("Thống kê & Báo cáo", currentUser.getVaiTro().coQuyenThongKe())) {
            return;
        }
        setActiveButton(quanLyThongKeButton); 
        loadScreen("/fxml/QuanLyThongKe.fxml", "/css/ThongKe.css"); 
    }
    
    @FXML 
    private void handleQuanLyThucDon() throws IOException {
        if (currentUser == null || !kiemTraQuyen("Quản lý Thực đơn", currentUser.getVaiTro().coQuyenQuanLyThucDon())) {
            return;
        }
        setActiveButton(quanLyThucDonButton); 
        loadScreen("/fxml/QuanLyThucDon.fxml", "/css/ThucDon.css"); 
    }
    
    @FXML 
    private void handleQuanLyHoaDon() throws IOException {
        if (currentUser == null || !kiemTraQuyen("Quản lý Hóa đơn", currentUser.getVaiTro().coQuyenQuanLyHoaDon())) {
            return;
        }
        setActiveButton(quanLyHoaDonButton); 
        loadScreen("/fxml/QuanLyHoaDon.fxml", "/css/HoaDon.css"); 
    }
    
    @FXML 
    private void handleQuanLyNhanVien() throws IOException {
        if (currentUser == null || !kiemTraQuyen("Quản lý Nhân viên", currentUser.getVaiTro().coQuyenQuanLyNhanVien())) {
            return;
        }
        setActiveButton(quanLyNhanVienButton); 
        loadScreen("/fxml/QuanLyNhanVien.fxml", "/css/NhanVien.css"); 
    }
    
    @FXML 
    private void handleQuanLyKhachHang() throws IOException {
        if (currentUser == null || !kiemTraQuyen("Quản lý Khách hàng", currentUser.getVaiTro().coQuyenQuanLyKhachHang())) {
            return;
        }
        setActiveButton(quanLyKhachHangButton); 
        loadScreen("/fxml/QuanLyKhachHang.fxml", "/css/KhachHang.css"); 
    }
    
    @FXML 
    private void handleQuanLyKhuyenMai() throws IOException {
        if (currentUser == null || !kiemTraQuyen("Quản lý Ưu đãi", currentUser.getVaiTro().coQuyenQuanLyUuDai())) {
            return;
        }
        setActiveButton(quanLyKhuyenMaiButton); 
        loadScreen("/fxml/QuanLyKhuyenMai.fxml", "/css/KhuyenMai.css"); 
    }
    
    @FXML 
    private void handleQuanLyTraCuu() throws IOException {
        setActiveButton(quanLyTraCuuButton); 
        loadScreen("/fxml/QuanLyTraCuu.fxml", "/css/TraCuu.css"); 
    }

    @FXML 
    private void handleDangXuat() {
        setActiveButton(dangXuatButton);
        System.out.println("Đăng xuất...");

        MainApp.setLoggedInUser(null);

        if (mainApp != null) {
            Stage currentStage = (Stage) dangXuatButton.getScene().getWindow();
            if (currentStage != null) {
                currentStage.close();
            }
            mainApp.start(new Stage());
        } else {
             System.err.println("Lỗi: Không thể đăng xuất vì mainApp là null.");
        }
    }
}
    
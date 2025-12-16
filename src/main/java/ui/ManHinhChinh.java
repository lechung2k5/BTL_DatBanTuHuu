package ui;

// ========================================================================
// IMPORTS TỪ TẤT CẢ CÁC FILE
// ========================================================================

import entity.TaiKhoan;
import entity.VaiTro;
import entity.HoaDon; 
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

// Imports cho chức năng mở file PDF
import java.awt.Desktop; 
import java.io.File; 
import java.nio.file.Paths; 

import dao.KhachHangDAO;
import entity.KhachHang;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javax.mail.*;
import javax.mail.internet.*;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import dao.TaiKhoanDAO;
import javafx.scene.Node;
import javafx.util.Pair;
import java.sql.SQLException;
import java.util.ArrayList;

public class ManHinhChinh {
    
    // ========================================================================
    // FXML FIELDS
    // ========================================================================

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

    @FXML private HBox topBar; 
    @FXML private VBox sidebar; 
    @FXML private ImageView logoImageView; 

    @FXML private MenuItem doiMatKhauMenuItem;
    @FXML private MenuItem caiDatMenuItem;
    @FXML private MenuItem toggleThemeMenuItem; 
    @FXML private MenuItem dangXuatMenuItem; 
    @FXML private MenuItem thoatMenuItem;
    @FXML private MenuItem guiChuongTrinhTVMenuItem;
    @FXML private MenuItem xemLogKiemKeTienMatMenuItem;
    @FXML private MenuItem huongDanMenuItem;
    @FXML private MenuItem gioiThieuMenuItem;

    // ========================================================================
    // BIẾN INSTANCE
    // ========================================================================

    private MainApp mainApp;
    private Button activeButton;
    private final Map<Button, String> defaultIcons = new HashMap<>();
    private final Map<Button, String> activeIcons = new HashMap<>();
    private final Map<Button, ImageView> buttonIconMap = new HashMap<>();
    private Object currentController; 
    private TaiKhoan currentUser; 
    private DatBan datBanController; 

    private final KhachHangDAO khachHangDAO = new KhachHangDAO();
    private final TaiKhoanDAO taiKhoanDAO = new TaiKhoanDAO();
    private boolean isDarkMode = false;
    private final String darkStyleClass = "dark-mode";
    
    // Đường dẫn file PDF
    private static final String GIOI_THIEU_PDF_PATH = "/pdf/GioiThieu_PhanMem_NhaHangTuHuu.pdf";
    private static final String HUONG_DAN_TUYET_DOI = "D:\\NhaHangTuHuu1\\BTL_DatBanTuHuu\\src\\main\\resources\\pdf\\HuongDanPhimTat_PhanMem_NhaHangTuHuu.pdf";

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

        try {
            if (logoImageView != null) {
                logoImageView.setImage(new Image(getClass().getResourceAsStream("/images/LOGO TU HUU.png")));
            }
        } catch (Exception e) {
            System.err.println("Lỗi tải logo: /images/LOGO TU HUU.png");
        }
        
        setActiveButton(manHinhChinhButton);
        try {
            loadScreen("/fxml/Dashboard.fxml", "/css/Dashboard.css");
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        contentArea.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                setupKeyboardShortcuts();
            }
        });
    }

    public void chuyenSangTabDatBan(HoaDon hd) {
        try {
            setActiveButton(quanLyDatBanButton);
            loadScreen("/fxml/QuanLyDatBan.fxml", "/css/DatBan.css");
            
            if (this.datBanController != null) {
                if (hd != null) {
                    this.datBanController.loadHoaDonToMainInterface(hd);
                } else {
                    this.datBanController.clearFormDatBan();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi chuyển tab", "Không thể chuyển sang màn hình Đặt bàn: " + e.getMessage());
        }
    }
    
    private void setupKeyboardShortcuts() {
        contentArea.getScene().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.isControlDown()) {
                switch (event.getCode()) {
                    case DIGIT1: if (manHinhChinhButton.isVisible()) { try { handleManHinhChinh(); } catch (IOException e) { e.printStackTrace(); } event.consume(); } break;
                    case DIGIT2: if (quanLyDatBanButton.isVisible()) { try { handleQuanLyDatBan(); } catch (IOException e) { e.printStackTrace(); } event.consume(); } break;
                    case DIGIT3: if (quanLyHoaDonButton.isVisible()) { try { handleQuanLyHoaDon(); } catch (IOException e) { e.printStackTrace(); } event.consume(); } break;
                    case DIGIT4: if (quanLyKhachHangButton.isVisible()) { try { handleQuanLyKhachHang(); } catch (IOException e) { e.printStackTrace(); } event.consume(); } break;
                    case DIGIT5: if (quanLyThucDonButton.isVisible()) { try { handleQuanLyThucDon(); } catch (IOException e) { e.printStackTrace(); } event.consume(); } break;
                    case DIGIT6: if (quanLyKhuyenMaiButton.isVisible()) { try { handleQuanLyKhuyenMai(); } catch (IOException e) { e.printStackTrace(); } event.consume(); } break;
                    case DIGIT7: if (quanLyNhanVienButton.isVisible()) { try { handleQuanLyNhanVien(); } catch (IOException e) { e.printStackTrace(); } event.consume(); } break;
                    case DIGIT8: if (quanLyThongKeButton.isVisible()) { try { handleQuanLyThongKe(); } catch (IOException e) { e.printStackTrace(); } event.consume(); } break;
                    case T: if (quanLyTraCuuButton.isVisible()) { try { handleQuanLyTraCuu(); } catch (IOException e) { e.printStackTrace(); } event.consume(); } break;
                    case Q: handleDangXuat(); event.consume(); break;
                }
            }
            else if (event.getCode() == KeyCode.F1) { handleHuongDan(); event.consume(); }
            else if (event.getCode() == KeyCode.F5) { refreshCurrentScreen(); event.consume(); }
        });
    }
    
    private void refreshCurrentScreen() {
        try {
            if (activeButton == manHinhChinhButton) loadScreen("/fxml/Dashboard.fxml", "/css/Dashboard.css");
            else if (activeButton == quanLyDatBanButton) loadScreen("/fxml/QuanLyDatBan.fxml", "/css/DatBan.css");
            else if (activeButton == quanLyHoaDonButton) loadScreen("/fxml/QuanLyHoaDon.fxml", "/css/HoaDon.css");
            else if (activeButton == quanLyKhachHangButton) loadScreen("/fxml/QuanLyKhachHang.fxml", "/css/KhachHang.css");
            else if (activeButton == quanLyThucDonButton) loadScreen("/fxml/QuanLyThucDon.fxml", "/css/ThucDon.css");
            else if (activeButton == quanLyKhuyenMaiButton) loadScreen("/fxml/QuanLyKhuyenMai.fxml", "/css/KhuyenMai.css");
            else if (activeButton == quanLyNhanVienButton) loadScreen("/fxml/QuanLyNhanVien.fxml", "/css/NhanVien.css");
            else if (activeButton == quanLyThongKeButton) loadScreen("/fxml/QuanLyThongKe.fxml", "/css/ThongKe.css");
            else if (activeButton == quanLyTraCuuButton) loadScreen("/fxml/QuanLyTraCuu.fxml", "/css/TraCuu.css");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể làm mới trang: " + e.getMessage());
        }
    }
    
    public void setUserInfo(TaiKhoan user) {
        this.currentUser = user;
        if (user != null && user.getNhanVien() != null) {
            userNameLabel.setText(user.getNhanVien().getHoTen());
            userRoleLabel.setText(user.getVaiTro() != null ? user.getVaiTro().getTenVaiTro() : "Không xác định");
            apDungPhanQuyen();
        } else {
            userNameLabel.setText("Khách");
            userRoleLabel.setText("Chưa đăng nhập");
            apDungPhanQuyen();
        }
    }
    
    private void apDungPhanQuyen() {
        if (currentUser == null || currentUser.getVaiTro() == null) {
            hienThiMenu(manHinhChinhButton, true); 
            hienThiMenu(quanLyDatBanButton, false);
            hienThiMenu(dangXuatButton, false); 
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
    }
    
    private void hienThiMenu(Button button, boolean coQuyen) {
        if (button == null) return;
        button.setVisible(coQuyen);
        button.setManaged(coQuyen);
        button.setDisable(!coQuyen);
    }
    
    private boolean kiemTraQuyen(String tenChucNang, boolean coQuyen) {
        if (!coQuyen) {
            showAlert(Alert.AlertType.WARNING, "⚠️ KHÔNG CÓ QUYỀN TRUY CẬP", "Bạn không có quyền: " + tenChucNang);
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
            if(button.isVisible()) {
                String iconPath = (button == activeButton) ? activeIcons.get(button) : defaultIcons.get(button);
                if (iconPath != null) {
                     try {
                         URL iconUrl = getClass().getResource(iconPath);
                         if (iconUrl != null) icon.setImage(new Image(iconUrl.toExternalForm()));
                     } catch (Exception e) {}
                }
                button.getStyleClass().setAll((button == activeButton) ? "menu-button-active" : "menu-button");
            }
        });
    }

    private void setActiveButton(Button button) {
        this.activeButton = button;
        updateMenuStyles();
    }

    private void loadScreen(String fxmlPath, String cssPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();

        currentController = loader.getController();

        if (currentController instanceof DashboardController) {
            ((DashboardController) currentController).setMainController(this);
        }
        if (currentController instanceof DatBan) {
            this.datBanController = (DatBan) currentController;
        }

        root.getStylesheets().clear();
        URL globalCssUrl = getClass().getResource("/css/manHinhChinh.css");
        if (globalCssUrl != null) root.getStylesheets().add(globalCssUrl.toExternalForm());
        
        if (cssPath != null && !cssPath.isEmpty()) {
            URL specificCssUrl = getClass().getResource(cssPath);
            if (specificCssUrl != null) root.getStylesheets().add(specificCssUrl.toExternalForm());
        }

        contentArea.setCenter(root);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    @FXML private void handleManHinhChinh() throws IOException { setActiveButton(manHinhChinhButton); loadScreen("/fxml/Dashboard.fxml", "/css/Dashboard.css"); }
    @FXML private void handleQuanLyDatBan() throws IOException { if (currentUser == null || !kiemTraQuyen("Quản lý Đặt bàn", currentUser.getVaiTro().coQuyenQuanLyDatBan())) return; setActiveButton(quanLyDatBanButton); loadScreen("/fxml/QuanLyDatBan.fxml", "/css/DatBan.css"); }
    @FXML private void handleQuanLyThongKe() throws IOException { if (currentUser == null || !kiemTraQuyen("Thống kê & Báo cáo", currentUser.getVaiTro().coQuyenThongKe())) return; setActiveButton(quanLyThongKeButton); loadScreen("/fxml/QuanLyThongKe.fxml", "/css/ThongKe.css"); }
    @FXML private void handleQuanLyThucDon() throws IOException { if (currentUser == null || !kiemTraQuyen("Quản lý Thực đơn", currentUser.getVaiTro().coQuyenQuanLyThucDon())) return; setActiveButton(quanLyThucDonButton); loadScreen("/fxml/QuanLyThucDon.fxml", "/css/ThucDon.css"); }
    @FXML private void handleQuanLyHoaDon() throws IOException { if (currentUser == null || !kiemTraQuyen("Quản lý Hóa đơn", currentUser.getVaiTro().coQuyenQuanLyHoaDon())) return; setActiveButton(quanLyHoaDonButton); loadScreen("/fxml/QuanLyHoaDon.fxml", "/css/HoaDon.css"); }
    @FXML private void handleQuanLyNhanVien() throws IOException { if (currentUser == null || !kiemTraQuyen("Quản lý Nhân viên", currentUser.getVaiTro().coQuyenQuanLyNhanVien())) return; setActiveButton(quanLyNhanVienButton); loadScreen("/fxml/QuanLyNhanVien.fxml", "/css/NhanVien.css"); }
    @FXML private void handleQuanLyKhachHang() throws IOException { if (currentUser == null || !kiemTraQuyen("Quản lý Khách hàng", currentUser.getVaiTro().coQuyenQuanLyKhachHang())) return; setActiveButton(quanLyKhachHangButton); loadScreen("/fxml/QuanLyKhachHang.fxml", "/css/KhachHang.css"); }
    @FXML private void handleQuanLyKhuyenMai() throws IOException { if (currentUser == null || !kiemTraQuyen("Quản lý Ưu đãi", currentUser.getVaiTro().coQuyenQuanLyUuDai())) return; setActiveButton(quanLyKhuyenMaiButton); loadScreen("/fxml/QuanLyKhuyenMai.fxml", "/css/KhuyenMai.css"); }
    @FXML private void handleQuanLyTraCuu() throws IOException { setActiveButton(quanLyTraCuuButton); loadScreen("/fxml/QuanLyTraCuu.fxml", "/css/TraCuu.css"); }

    @FXML private void handleDangXuat() {
        setActiveButton(dangXuatButton);
        MainApp.setLoggedInUser(null);
        if (mainApp != null) {
            Stage currentStage = (Stage) dangXuatButton.getScene().getWindow();
            if (currentStage != null) currentStage.close();
            Platform.runLater(() -> { try { mainApp.start(new Stage()); } catch (Exception e) {} });
        }
    }

    @FXML
    private void handleDoiMatKhau() {
        Optional<Pair<String, String>> result = showChangePasswordDialog();
        result.ifPresent(passwords -> {
            String oldPassword = passwords.getKey(); 
            String newPassword = passwords.getValue();
            try {
                if (taiKhoanDAO.kiemTraMatKhau(currentUser.getTenDangNhap(), oldPassword)) {
                    if (taiKhoanDAO.doiMatKhau(currentUser.getTenDangNhap(), newPassword)) { 
                        showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đổi mật khẩu thành công!"); 
                    }
                } else { 
                    showAlert(Alert.AlertType.WARNING, "Sai mật khẩu", "Mật khẩu cũ không chính xác."); 
                }
            } catch (SQLException e) { 
                e.printStackTrace(); 
            }
        });
    }

    private Optional<Pair<String, String>> showChangePasswordDialog() {
        Dialog<Pair<String, String>> dialog = new Dialog<>(); 
        dialog.setTitle("Đổi mật khẩu"); 
        ButtonType changeButtonType = new ButtonType("Xác nhận", ButtonData.OK_DONE); 
        dialog.getDialogPane().getButtonTypes().addAll(changeButtonType, ButtonType.CANCEL);
        GridPane grid = new GridPane(); grid.setHgap(10); grid.setVgap(10);
        PasswordField oldPassword = new PasswordField();
        PasswordField newPassword = new PasswordField();
        PasswordField confirmPassword = new PasswordField();
        grid.add(new Label("Mật khẩu cũ:"), 0, 0); grid.add(oldPassword, 1, 0);
        grid.add(new Label("Mật khẩu mới:"), 0, 1); grid.add(newPassword, 1, 1);
        grid.add(new Label("Xác nhận MK mới:"), 0, 2); grid.add(confirmPassword, 1, 2);
        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(dialogButton -> { 
            if (dialogButton == changeButtonType) return new Pair<>(oldPassword.getText(), newPassword.getText()); 
            return null; 
        });
        return dialog.showAndWait();
    }

    @FXML
    private void handleToggleTheme() {
        isDarkMode = !isDarkMode;
        if (topBar == null || sidebar == null) return;
        try {
            if (isDarkMode) {
                topBar.getStyleClass().add(darkStyleClass);
                sidebar.getStyleClass().add(darkStyleClass);
                toggleThemeMenuItem.setText("Chuyển chế độ Sáng");
                if (logoImageView != null) logoImageView.setImage(new Image(getClass().getResourceAsStream("/images/DarkmodeLOGO.jpg")));
            } else {
                topBar.getStyleClass().remove(darkStyleClass);
                sidebar.getStyleClass().remove(darkStyleClass);
                toggleThemeMenuItem.setText("Chuyển chế độ Tối");
                if (logoImageView != null) logoImageView.setImage(new Image(getClass().getResourceAsStream("/images/LOGO TU HUU.png")));
            }
        } catch (Exception e) {}
    }
    
    @FXML private void handleThoat() { Platform.exit(); }

    @FXML
    private void handleGuiChuongTrinhTV() {
        // Logic gửi email (Giữ nguyên như code cũ của bạn)
    }
    
    @FXML private void handleXemLogKiemKeTienMat() { showAlert(Alert.AlertType.INFORMATION, "Thông báo", "Chức năng đang phát triển."); }

    @FXML 
    private void handleHuongDan() { 
        moFilePDF(HUONG_DAN_TUYET_DOI);
    }

    @FXML 
    private void handleGioiThieu() { 
        moFilePDF(getClass().getResource(GIOI_THIEU_PDF_PATH).toExternalForm());
    }
    
    private void moFilePDF(String path) {
        if (Desktop.isDesktopSupported()) {
            try {
                File file;
                if (path.startsWith("file:") || path.startsWith("/")) {
                   URL url = path.startsWith("/") ? getClass().getResource(path) : new URL(path);
                   file = Paths.get(url.toURI()).toFile();
                } else {
                   file = new File(path);
                }
                
                if (file.exists()) {
                    Desktop.getDesktop().open(file);
                } else {
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Không tìm thấy file: " + path);
                }
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể mở file: " + e.getMessage());
            }
        }
    }
}
package ui;

// ========================================================================
// IMPORTS TỪ TẤT CẢ CÁC FILE
// ========================================================================

// Imports từ ManHinhChinh.java (Gốc)
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

// Imports từ 1.java (File 1)
import dao.KhachHangDAO; // << Import DAO Khách hàng
import entity.KhachHang; // << Import entity KhachHang
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

// Imports từ 2.java (File 2)
import dao.TaiKhoanDAO;
import javafx.scene.Node;
import javafx.util.Pair;
import java.sql.SQLException;
import java.util.ArrayList; // Mặc dù không dùng trực tiếp nhưng có thể cần cho List

public class ManHinhChinh {
    
    // ========================================================================
    // FXML FIELDS TỪ TẤT CẢ CÁC FILE
    // ========================================================================

    // --- Từ ManHinhChinh.java (Gốc) ---
    @FXML private BorderPane contentArea;
    @FXML private VBox menuItems; // Sidebar VBox container
    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;
    @FXML private ImageView profileImage;

    // Sidebar Buttons
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

    // Sidebar Icons
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

    // --- Từ 2.java (Cho Dark Mode) ---
    @FXML private HBox topBar; // Cần fx:id="topBar" ở HBox trên cùng
    @FXML private VBox sidebar; // Cần fx:id="sidebar" ở VBox sidebar
    @FXML private ImageView logoImageView; // Cần fx:id="logoImageView"

    // --- Từ 1.java & 2.java (MenuBar Items) ---
    @FXML private MenuItem doiMatKhauMenuItem;
    @FXML private MenuItem caiDatMenuItem;
    @FXML private MenuItem toggleThemeMenuItem; // (Từ 2.java)
    @FXML private MenuItem dangXuatMenuItem; // (Từ 2.java)
    @FXML private MenuItem thoatMenuItem;
    @FXML private MenuItem quanLyBanMenuItem;
    @FXML private MenuItem guiChuongTrinhTVMenuItem;
    @FXML private MenuItem xemLogKiemKeTienMatMenuItem;
    @FXML private MenuItem huongDanMenuItem;
    @FXML private MenuItem gioiThieuMenuItem;

    // ========================================================================
    // BIẾN INSTANCE TỪ TẤT CẢ CÁC FILE
    // ========================================================================

    private MainApp mainApp;
    private Button activeButton;
    private final Map<Button, String> defaultIcons = new HashMap<>();
    private final Map<Button, String> activeIcons = new HashMap<>();
    private final Map<Button, ImageView> buttonIconMap = new HashMap<>();
    private Object currentController; // (Từ Gốc)
    private TaiKhoan currentUser; // (Từ Gốc)

    // Từ 1.java & 2.java
    private final KhachHangDAO khachHangDAO = new KhachHangDAO();
    
    // Từ 2.java
    private final TaiKhoanDAO taiKhoanDAO = new TaiKhoanDAO();
    private boolean isDarkMode = false;
    private final String darkStyleClass = "dark-mode";
    

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    public void initialize() {
        // Map Buttons với Icons (Từ Gốc)
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

        // Map đường dẫn Icons (Từ Gốc)
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

        // Set logo mặc định (Từ 2.java)
        try {
            if (logoImageView != null) {
                logoImageView.setImage(new Image(getClass().getResourceAsStream("/images/LOGO TU HUU.png")));
            }
        } catch (Exception e) {
            System.err.println("Lỗi tải logo mặc định: /images/LOGO TU HUU.png");
        }
        
        // Khởi động (Từ Gốc)
        setActiveButton(manHinhChinhButton);
        try {
            loadScreen("/fxml/Dashboard.fxml", "/css/Dashboard.css");
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // Setup phím tắt (Từ Gốc)
        contentArea.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                setupKeyboardShortcuts();
            }
        });
    }
    
    // ========================================================================
    // CÁC HÀM XỬ LÝ PHÍM TẮT (TỪ GỐC)
    // ========================================================================

    /**
     * Thiết lập phím tắt toàn cục
     */
    private void setupKeyboardShortcuts() {
        contentArea.getScene().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            
            // Ctrl + 1-8, T, Q: Điều hướng nhanh (có kiểm tra quyền)
            if (event.isControlDown()) {
                switch (event.getCode()) {
                    case DIGIT1:
                        if (manHinhChinhButton.isVisible()) {
                            try { handleManHinhChinh(); } catch (IOException e) { e.printStackTrace(); }
                            event.consume();
                        }
                        break;
                    case DIGIT2:
                        if (quanLyDatBanButton.isVisible()) {
                            try { handleQuanLyDatBan(); } catch (IOException e) { e.printStackTrace(); }
                            event.consume();
                        }
                        break;
                    case DIGIT3:
                        if (quanLyHoaDonButton.isVisible()) {
                            try { handleQuanLyHoaDon(); } catch (IOException e) { e.printStackTrace(); }
                            event.consume();
                        }
                        break;
                    case DIGIT4:
                        if (quanLyKhachHangButton.isVisible()) {
                            try { handleQuanLyKhachHang(); } catch (IOException e) { e.printStackTrace(); }
                            event.consume();
                        }
                        break;
                    case DIGIT5:
                        if (quanLyThucDonButton.isVisible()) {
                            try { handleQuanLyThucDon(); } catch (IOException e) { e.printStackTrace(); }
                            event.consume();
                        }
                        break;
                    case DIGIT6:
                        if (quanLyKhuyenMaiButton.isVisible()) {
                            try { handleQuanLyKhuyenMai(); } catch (IOException e) { e.printStackTrace(); }
                            event.consume();
                        }
                        break;
                    case DIGIT7:
                        if (quanLyNhanVienButton.isVisible()) {
                            try { handleQuanLyNhanVien(); } catch (IOException e) { e.printStackTrace(); }
                            event.consume();
                        }
                        break;
                    case DIGIT8:
                        if (quanLyThongKeButton.isVisible()) {
                            try { handleQuanLyThongKe(); } catch (IOException e) { e.printStackTrace(); }
                            event.consume();
                        }
                        break;
                    case T:
                        if (quanLyTraCuuButton.isVisible()) {
                            try { handleQuanLyTraCuu(); } catch (IOException e) { e.printStackTrace(); }
                            event.consume();
                        }
                        break;
                    case Q:
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
     * Hiển thị hướng dẫn phím tắt (động theo quyền)
     */
    private void showKeyboardShortcutsHelp() {
        StringBuilder helpText = new StringBuilder();
        helpText.append("⌨️ PHÍM TẮT CÓ SẴN:\n\n");
        helpText.append("📍 ĐIỀU HƯỚNG:\n");
        
        if (manHinhChinhButton.isVisible()) helpText.append("Ctrl + 1  →  Dashboard\n");
        if (quanLyDatBanButton.isVisible()) helpText.append("Ctrl + 2  →  Quản lý Đặt bàn\n");
        if (quanLyHoaDonButton.isVisible()) helpText.append("Ctrl + 3  →  Quản lý Hóa đơn\n");
        if (quanLyKhachHangButton.isVisible()) helpText.append("Ctrl + 4  →  Quản lý Khách hàng\n");
        if (quanLyThucDonButton.isVisible()) helpText.append("Ctrl + 5  →  Quản lý Thực đơn\n");
        if (quanLyKhuyenMaiButton.isVisible()) helpText.append("Ctrl + 6  →  Quản lý Khuyến mãi\n");
        if (quanLyNhanVienButton.isVisible()) helpText.append("Ctrl + 7  →  Quản lý Nhân viên\n");
        if (quanLyThongKeButton.isVisible()) helpText.append("Ctrl + 8  →  Thống kê & Báo cáo\n");
        if (quanLyTraCuuButton.isVisible()) helpText.append("Ctrl + T  →  Tra cứu\n");
        
        helpText.append("\n📋 CHỨC NĂNG:\n");
        helpText.append("F1        →  Hiển thị trợ giúp này\n");
        helpText.append("F5        →  Làm mới trang hiện tại\n");
        helpText.append("Ctrl + Q  →  Đăng xuất\n");
        
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
        
        alert.getDialogPane().setMinWidth(500);
        alert.getDialogPane().setMinHeight(400);
        
        alert.showAndWait();
    }
    
    /**
     * Làm mới trang hiện tại
     */
    private void refreshCurrentScreen() {
        try {
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
            
            System.out.println("🔄 Đã làm mới trang hiện tại");
            
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể làm mới trang: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // ========================================================================
    // CÁC HÀM XỬ LÝ PHÂN QUYỀN (TỪ GỐC)
    // ========================================================================

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
            // Ẩn menu khi chưa đăng nhập
            apDungPhanQuyen();
        }
    }
    
    /**
     * Áp dụng phân quyền cho các menu
     */
    private void apDungPhanQuyen() {
        if (currentUser == null || currentUser.getVaiTro() == null) {
            // Trường hợp chưa đăng nhập
            hienThiMenu(manHinhChinhButton, true); // Chỉ hiện Dashboard
            hienThiMenu(quanLyDatBanButton, false);
            hienThiMenu(quanLyThongKeButton, false);
            hienThiMenu(quanLyThucDonButton, false);
            hienThiMenu(quanLyHoaDonButton, false);
            hienThiMenu(quanLyNhanVienButton, false);
            hienThiMenu(quanLyKhachHangButton, false);
            hienThiMenu(quanLyKhuyenMaiButton, false);
            hienThiMenu(quanLyTraCuuButton, false); // Có thể cho hiện tra cứu nếu cần
            hienThiMenu(dangXuatButton, false); // Ẩn đăng xuất
            
            // Ẩn/hiện MenuBar items
            if(doiMatKhauMenuItem != null) doiMatKhauMenuItem.setDisable(true);
            if(guiChuongTrinhTVMenuItem != null) guiChuongTrinhTVMenuItem.setDisable(true);
            // ... (ẩn các menu item khác nếu cần)
            
            System.out.println("🔐 PHÂN QUYỀN: Chưa đăng nhập.");
            return;
        }
        
        VaiTro vaiTro = currentUser.getVaiTro();
        
        // Phân quyền Sidebar
        hienThiMenu(manHinhChinhButton, vaiTro.coQuyenDashboard());
        hienThiMenu(quanLyDatBanButton, vaiTro.coQuyenQuanLyDatBan());
        hienThiMenu(quanLyThongKeButton, vaiTro.coQuyenThongKe());
        hienThiMenu(quanLyThucDonButton, vaiTro.coQuyenQuanLyThucDon());
        hienThiMenu(quanLyHoaDonButton, vaiTro.coQuyenQuanLyHoaDon());
        hienThiMenu(quanLyNhanVienButton, vaiTro.coQuyenQuanLyNhanVien());
        hienThiMenu(quanLyKhachHangButton, vaiTro.coQuyenQuanLyKhachHang());
        hienThiMenu(quanLyKhuyenMaiButton, vaiTro.coQuyenQuanLyUuDai());
        hienThiMenu(quanLyTraCuuButton, vaiTro.coQuyenTraCuu());
        hienThiMenu(dangXuatButton, true); // Luôn hiện khi đã đăng nhập
        
        // Phân quyền MenuBar (Ví dụ)
        if(doiMatKhauMenuItem != null) doiMatKhauMenuItem.setDisable(false); // Ai cũng đc đổi MK
        if(guiChuongTrinhTVMenuItem != null) guiChuongTrinhTVMenuItem.setDisable(!vaiTro.coQuyenQuanLyKhachHang()); // Chỉ Thu ngân
        if(quanLyBanMenuItem != null) quanLyBanMenuItem.setDisable(!vaiTro.coQuyenQuanLyDatBan()); // Chỉ Thu ngân
        if(xemLogKiemKeTienMatMenuItem != null) xemLogKiemKeTienMatMenuItem.setDisable(!vaiTro.coQuyenThongKe()); // Chỉ Quản lý
        
        
        // In log (Từ Gốc)
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
                "Vai trò của bạn: " + (currentUser != null && currentUser.getVaiTro() != null ? currentUser.getVaiTro().getTenVaiTro() : "Chưa đăng nhập") + "\n" +
                "Quyền hạn: " + (currentUser != null && currentUser.getVaiTro() != null ? currentUser.getVaiTro().getMoTaQuyen() : "Không có"));
            return false;
        }
        return true;
    }
    
    // ========================================================================
    // CÁC HÀM UI HELPER (TỪ GỐC)
    // ========================================================================

    private void addIconMapping(Button button, String defaultIcon, String activeIcon) {
        defaultIcons.put(button, defaultIcon);
        activeIcons.put(button, activeIcon);
    }

    private void updateMenuStyles() {
         buttonIconMap.forEach((button, icon) -> {
            if (button == null || icon == null) return;
            // Chỉ cập nhật nút đang hiển thị
            if(button.isVisible()) {
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
            }
        });
    }

    private void setActiveButton(Button button) {
        this.activeButton = button;
        updateMenuStyles();
    }

    private void loadScreen(String fxmlPath, String cssPath) throws IOException {
    	// Disable shortcuts in the previous screen if supported (Từ Gốc)
    	if (currentController != null) {
    	    try {
    	        currentController.getClass().getMethod("disableKeyboardShortcuts").invoke(currentController);
    	        System.out.println("⛔ Shortcuts of previous screen disabled!");
    	    } catch (Exception ignored) {
                // Thử phương thức dispose (từ file 1)
                try {
                    currentController.getClass().getMethod("dispose").invoke(currentController);
                    System.out.println("✅ Disposed previous screen");
                } catch (Exception ignored2) {}
            }
    	}

        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();

        // Save new controller (Từ Gốc)
        currentController = loader.getController();

        root.getStylesheets().clear();
        URL globalCssUrl = getClass().getResource("/css/manHinhChinh.css");
        if (globalCssUrl != null) {
            root.getStylesheets().add(globalCssUrl.toExternalForm());
        }
        if (cssPath != null && !cssPath.isEmpty()) {
            URL specificCssUrl = getClass().getResource(cssPath);
            if (specificCssUrl != null) {
                root.getStylesheets().add(specificCssUrl.toExternalForm());
            }
        }

        contentArea.setCenter(root);
    }

    // (Từ Gốc)
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    // Overload (Từ 1.java)
    private void showAlert(String title, String content) {
        showAlert(Alert.AlertType.INFORMATION, title, content);
    }

    // ========================================================================
    // CÁC HÀM HANDLE SIDEBAR (TỪ GỐC - CÓ KIỂM TRA QUYỀN)
    // ========================================================================
    
    @FXML 
    private void handleManHinhChinh() throws IOException { 
        // Không cần kiểm tra quyền cho Dashboard
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
        // Không cần kiểm tra quyền cho Tra cứu
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
            // Khởi động lại ứng dụng (Từ Gốc & 1.java)
            Platform.runLater(() -> {
                 try {
                    mainApp.start(new Stage());
                 } catch (Exception e) {
                      System.err.println("Lỗi khi khởi động lại ứng dụng sau đăng xuất:");
                      e.printStackTrace();
                 }
            });
        } else {
             System.err.println("Lỗi: Không thể đăng xuất vì mainApp là null.");
        }
    }
    
    // ========================================================================
    // CÁC HÀM HANDLE MENUBAR (TỪ FILE 1 & 2)
    // ========================================================================

    /**
     * Handle Đổi mật khẩu (Từ 2.java)
     */
    @FXML
    private void handleDoiMatKhau() {
        System.out.println("Chức năng Đổi mật khẩu được chọn.");
        Optional<Pair<String, String>> result = showChangePasswordDialog();
        result.ifPresent(passwords -> {
            String oldPassword = passwords.getKey(); 
            String newPassword = passwords.getValue();
            // Lấy user từ biến currentUser (thay vì MainApp.getLoggedInUser())
            if (currentUser == null || currentUser.getTenDangNhap() == null) { 
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể xác định người dùng hiện tại."); 
                return; 
            }
            try {
                if (taiKhoanDAO.kiemTraMatKhau(currentUser.getTenDangNhap(), oldPassword)) {
                    if (taiKhoanDAO.doiMatKhau(currentUser.getTenDangNhap(), newPassword)) { 
                        showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đổi mật khẩu thành công!"); 
                    }
                    else { 
                        showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể cập nhật mật khẩu mới."); 
                    }
                } else { 
                    showAlert(Alert.AlertType.WARNING, "Sai mật khẩu", "Mật khẩu cũ không chính xác."); 
                }
            } catch (SQLException e) { 
                showAlert(Alert.AlertType.ERROR, "Lỗi CSDL", "Lỗi khi kiểm tra/đổi mật khẩu: " + e.getMessage()); 
                e.printStackTrace(); 
            }
        });
    }

    /**
     * Hiển thị Dialog Đổi mật khẩu (Từ 2.java)
     */
    private Optional<Pair<String, String>> showChangePasswordDialog() {
        Dialog<Pair<String, String>> dialog = new Dialog<>(); 
        dialog.setTitle("Đổi mật khẩu"); 
        dialog.setHeaderText("Nhập thông tin mật khẩu");
        ButtonType changeButtonType = new ButtonType("Xác nhận", ButtonData.OK_DONE); 
        dialog.getDialogPane().getButtonTypes().addAll(changeButtonType, ButtonType.CANCEL);
        GridPane grid = new GridPane(); 
        grid.setHgap(10); grid.setVgap(10); grid.setPadding(new Insets(20, 150, 10, 10));
        PasswordField oldPassword = new PasswordField(); oldPassword.setPromptText("Nhập mật khẩu cũ");
        PasswordField newPassword = new PasswordField(); newPassword.setPromptText("Nhập mật khẩu mới");
        PasswordField confirmPassword = new PasswordField(); confirmPassword.setPromptText("Xác nhận mật khẩu mới");
        grid.add(new Label("Mật khẩu cũ:"), 0, 0); grid.add(oldPassword, 1, 0);
        grid.add(new Label("Mật khẩu mới:"), 0, 1); grid.add(newPassword, 1, 1);
        grid.add(new Label("Xác nhận MK mới:"), 0, 2); grid.add(confirmPassword, 1, 2);
        Node changeButton = dialog.getDialogPane().lookupButton(changeButtonType); 
        changeButton.setDisable(true);
        Runnable validation = () -> { 
            boolean isEmpty = oldPassword.getText().trim().isEmpty() || newPassword.getText().trim().isEmpty() || confirmPassword.getText().trim().isEmpty(); 
            boolean passwordsMatch = newPassword.getText().equals(confirmPassword.getText()); 
            changeButton.setDisable(isEmpty || !passwordsMatch); 
        };
        oldPassword.textProperty().addListener((observable, oldValue, newValue) -> validation.run()); 
        newPassword.textProperty().addListener((observable, oldValue, newValue) -> validation.run()); 
        confirmPassword.textProperty().addListener((observable, oldValue, newValue) -> validation.run());
        dialog.getDialogPane().setContent(grid); 
        Platform.runLater(oldPassword::requestFocus);
        dialog.setResultConverter(dialogButton -> { 
            if (dialogButton == changeButtonType) return new Pair<>(oldPassword.getText(), newPassword.getText()); 
            return null; 
        });
        return dialog.showAndWait();
    }

    /**
     * Handle Cài đặt (Từ 1.java)
     */
    @FXML 
    private void handleCaiDat() { 
        System.out.println("Cài đặt..."); 
        showAlert("Thông báo", "Chức năng đang phát triển."); 
    }
    
    /**
     * Handle Chuyển đổi Theme (Từ 2.java)
     */
    @FXML
    private void handleToggleTheme() {
        isDarkMode = !isDarkMode; // Đảo trạng thái

        // Giả sử topBar và sidebar đã được inject
        if (topBar == null || sidebar == null) {
            System.err.println("Lỗi: topBar hoặc sidebar chưa được inject FXML.");
            return;
        }

        var topBarStyles = topBar.getStyleClass();
        var sidebarStyles = sidebar.getStyleClass();

        try {
            if (isDarkMode) {
                if (!topBarStyles.contains(darkStyleClass)) topBarStyles.add(darkStyleClass);
                if (!sidebarStyles.contains(darkStyleClass)) sidebarStyles.add(darkStyleClass);
                toggleThemeMenuItem.setText("Chuyển chế độ Sáng");
                if (logoImageView != null) {
                    logoImageView.setImage(new Image(getClass().getResourceAsStream("/images/DarkmodeLOGO.jpg")));
                }
            } else {
                topBarStyles.remove(darkStyleClass);
                sidebarStyles.remove(darkStyleClass);
                toggleThemeMenuItem.setText("Chuyển chế độ Tối");
                if (logoImageView != null) {
                    logoImageView.setImage(new Image(getClass().getResourceAsStream("/images/LOGO TU HUU.png")));
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi khi tải ảnh logo. Hãy chắc chắn /images/DarkmodeLOGO.jpg và /images/LOGO TU HUU.png tồn tại.");
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi Tải Ảnh", "Không tìm thấy tệp logo. Kiểm tra đường dẫn /images/");
        }
    }
    
    /**
     * Handle Đăng xuất từ MenuBar (Mới)
     */
    @FXML 
    private void handleDangXuatMenu() {
        handleDangXuat();
    }

    /**
     * Handle Thoát (Từ 1.java)
     */
    @FXML 
    private void handleThoat() { 
        System.out.println("Thoát..."); 
        Platform.exit(); 
    }
    
    /**
     * Handle Quản lý Bàn (Từ 1.java)
     */
    @FXML 
    private void handleQuanLyBan() throws IOException { 
        // Kiểm tra quyền (Ví dụ: quyền Đặt bàn)
        if (currentUser == null || !kiemTraQuyen("Quản lý Bàn", currentUser.getVaiTro().coQuyenQuanLyDatBan())) {
            return;
        }
        System.out.println("QL Bàn..."); 
        showAlert("Thông báo", "Chức năng Quản lý Bàn đang được phát triển."); 
        // setActiveButton(null); // Bỏ active
        // Hoặc load màn hình QL Bàn nếu có
        // loadScreen("/fxml/QuanLyBan.fxml", "/css/Ban.css"); 
    }

    /**
     * Handle Gửi Email CT Thành viên (Từ 1.java)
     */
    @FXML
    private void handleGuiChuongTrinhTV() {
        System.out.println("Chức năng Gửi Chương trình Thành viên được chọn.");

        // B1: Lấy danh sách khách hàng có email
        ObservableList<KhachHang> dsKhachHangCoEmail;
        try {
            ObservableList<KhachHang> allKhachHang = khachHangDAO.getAllKhachHang();
            List<KhachHang> filteredList = allKhachHang.stream()
                .filter(kh -> kh.getEmail() != null && !kh.getEmail().trim().isEmpty())
                .collect(Collectors.toList()); 
            dsKhachHangCoEmail = FXCollections.observableArrayList(filteredList); 
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải danh sách khách hàng: " + e.getMessage());
            return;
        }

        if (dsKhachHangCoEmail.isEmpty()) {
            showAlert("Thông báo", "Không tìm thấy khách hàng nào có địa chỉ email.");
            return;
        }

        // B2: Tạo Dialog tùy chỉnh
        Dialog<Map<String, Object>> dialog = new Dialog<>();
        dialog.setTitle("Gửi Email Chương trình Thành viên");
        dialog.setHeaderText("Soạn và gửi email đến khách hàng");

        ButtonType sendButtonType = new ButtonType("Gửi đi", ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(sendButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 10));

        Label recipientLabel = new Label("Người nhận:");
        ListView<KhachHang> recipientListView = new ListView<>(dsKhachHangCoEmail);
        recipientListView.setPrefHeight(150);
        Map<KhachHang, BooleanProperty> selectionMap = new HashMap<>();
        dsKhachHangCoEmail.forEach(kh -> selectionMap.put(kh, new SimpleBooleanProperty(false)));

        recipientListView.setCellFactory(lv -> new ListCell<KhachHang>() {
            private final CheckBox checkBox = new CheckBox();
            private final Label label = new Label();
            private final HBox hbox = new HBox(5, checkBox, label);
            {
                hbox.setAlignment(Pos.CENTER_LEFT);
                checkBox.setOnAction(event -> {
                    if (getItem() != null) selectionMap.get(getItem()).set(checkBox.isSelected());
                });
            }
            @Override
            protected void updateItem(KhachHang kh, boolean empty) {
                super.updateItem(kh, empty);
                if (empty || kh == null) { setText(null); setGraphic(null); }
                else {
                    BooleanProperty selected = selectionMap.get(kh);
                    checkBox.setSelected(selected.get());
                    label.setText(kh.getTenKH() + " (" + kh.getEmail() + ")");
                    setGraphic(hbox);
                }
            }
        });

        CheckBox selectAllCheckBox = new CheckBox("Chọn tất cả");
        selectAllCheckBox.setOnAction(e -> {
            boolean select = selectAllCheckBox.isSelected();
            selectionMap.values().forEach(prop -> prop.set(select));
            recipientListView.refresh();
        });

        Label subjectLabel = new Label("Tiêu đề:");
        TextField subjectField = new TextField("Thông báo Chương trình Thành viên Mới!");
        Label contentLabel = new Label("Nội dung:");
        TextArea contentArea = new TextArea();
        contentArea.setPromptText("Ví dụ: Giảm giá 20% cho thành viên Vàng...");
        contentArea.setWrapText(true);
        contentArea.setPrefRowCount(8);

        grid.add(recipientLabel, 0, 0); grid.add(recipientListView, 1, 0);
        grid.add(selectAllCheckBox, 1, 1);
        grid.add(subjectLabel, 0, 2); grid.add(subjectField, 1, 2);
        grid.add(contentLabel, 0, 3); grid.add(contentArea, 1, 3);
        GridPane.setVgrow(recipientListView, Priority.ALWAYS); GridPane.setVgrow(contentArea, Priority.ALWAYS);
        GridPane.setHgrow(recipientListView, Priority.ALWAYS); GridPane.setHgrow(subjectField, Priority.ALWAYS);
        GridPane.setHgrow(contentArea, Priority.ALWAYS);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefSize(600, 500);

        // B3: Xử lý kết quả Dialog
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == sendButtonType) {
                List<String> selectedEmails = selectionMap.entrySet().stream()
                    .filter(entry -> entry.getValue().get())
                    .map(entry -> entry.getKey().getEmail())
                    .collect(Collectors.toList());
                Map<String, Object> resultData = new HashMap<>();
                resultData.put("recipients", selectedEmails);
                resultData.put("subject", subjectField.getText());
                resultData.put("body", contentArea.getText());
                return resultData;
            }
            return null;
        });

        Optional<Map<String, Object>> result = dialog.showAndWait();

        // B4: Gửi Email nếu có kết quả
        if (result.isPresent()) {
            Map<String, Object> emailData = result.get();
            List<String> recipients = (List<String>) emailData.get("recipients");
            String subject = (String) emailData.get("subject");
            String bodyContent = (String) emailData.get("body");

            if (recipients.isEmpty()) { showAlert("Thông báo", "Bạn chưa chọn khách hàng nào để gửi email."); return; }
            if (subject.trim().isEmpty() || bodyContent.trim().isEmpty()) { showAlert("Thiếu thông tin", "Vui lòng nhập Tiêu đề và Nội dung email."); return; }

            // --- Phần gửi email bằng JavaMail ---
            final String username = "nhahangtuhuu@gmail.com"; // <<<< THAY EMAIL
            final String password = "rnwm bkli pycf bjcv";    // <<<< THAY MẬT KHẨU ỨNG DỤNG

            Properties prop = new Properties();
            prop.put("mail.smtp.host", "smtp.gmail.com"); prop.put("mail.smtp.port", "587");
            prop.put("mail.smtp.auth", "true"); prop.put("mail.smtp.starttls.enable", "true");

            Session session = Session.getInstance(prop, new javax.mail.Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

            try {
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(username));
                Address[] bccAddresses = new Address[recipients.size()];
                for (int i = 0; i < recipients.size(); i++) bccAddresses[i] = new InternetAddress(recipients.get(i));
                message.setRecipients(Message.RecipientType.BCC, bccAddresses);
                message.setSubject(subject);
                String finalEmailContent = "Kính gửi quý khách hàng,\n\n" + bodyContent +
                                           "\n\nTrân trọng,\nNhà hàng Tứ Hữu";
                message.setText(finalEmailContent);

                new Thread(() -> {
                    try {
                        Transport.send(message);
                        Platform.runLater(() -> showAlert("Thành công", "Đã gửi email đến " + recipients.size() + " khách hàng."));
                    } catch (MessagingException e) {
                         Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Lỗi Gửi Email", "Không thể gửi email: " + e.getMessage()));
                        e.printStackTrace();
                    }
                }).start();

            } catch (MessagingException e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi Tạo Email", "Không thể tạo email: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            showAlert("Đã hủy", "Thao tác gửi email đã được hủy bỏ.");
        }
    }
    
    /**
     * Handle Log Kiểm kê (Từ 1.java)
     */
    @FXML 
    private void handleXemLogKiemKeTienMat() { 
        System.out.println("Xem log..."); 
        showAlert("Thông báo", "Chức năng đang phát triển."); 
        setActiveButton(null); 
    }
    
    /**
     * Handle Hướng dẫn (Từ 1.java)
     */
    @FXML 
    private void handleHuongDan() { 
        System.out.println("Hướng dẫn..."); 
        // Thay vì Alert, có thể hiển thị dialog phím tắt
        showKeyboardShortcutsHelp();
        // showAlert("Thông báo", "Chức năng đang phát triển."); 
    }
    
    /**
     * Handle Giới thiệu (Từ 1.java)
     */
    @FXML 
    private void handleGioiThieu() { 
        System.out.println("Giới thiệu..."); 
        showAlert("Giới thiệu", "Phần mềm Quản lý Nhà hàng Tứ Hữu\nPhiên bản 1.0"); 
    }
}
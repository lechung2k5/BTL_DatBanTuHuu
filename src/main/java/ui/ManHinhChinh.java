package ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ManHinhChinh {
    @FXML
    private BorderPane contentArea;
    @FXML
    private VBox menuItems;
    @FXML
    private Label userNameLabel;
    @FXML
    private Label userRoleLabel;

    // FXML IDs for all buttons and their corresponding ImageViews
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
    private Map<Button, String> defaultIcons = new HashMap<>();
    private Map<Button, String> activeIcons = new HashMap<>();
    private Map<Button, ImageView> buttonIconMap = new HashMap<>();

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    public void initialize() throws IOException {
        // Map buttons to their ImageViews
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
        
        // Map button IDs to their respective icon file paths
        defaultIcons.put(manHinhChinhButton, "/icons/iconHome.png");
        activeIcons.put(manHinhChinhButton, "/icons/iconHome_White.png");

        defaultIcons.put(quanLyDatBanButton, "/icons/iconDatBan.png");
        activeIcons.put(quanLyDatBanButton, "/icons/iconDatBan_White.png");

        defaultIcons.put(quanLyThongKeButton, "/icons/iconThongKe.png");
        activeIcons.put(quanLyThongKeButton, "/icons/iconThongKe_White.png");

        defaultIcons.put(quanLyThucDonButton, "/icons/iconThucDon.png");
        activeIcons.put(quanLyThucDonButton, "/icons/iconThucDon_White.png");

        defaultIcons.put(quanLyHoaDonButton, "/icons/iconHoaDon.png");
        activeIcons.put(quanLyHoaDonButton, "/icons/iconHoaDon_White.png");
        
        defaultIcons.put(quanLyNhanVienButton, "/icons/iconNhanVienMenu.png");
        activeIcons.put(quanLyNhanVienButton, "/icons/iconNhanVien_White.png");

        defaultIcons.put(quanLyKhachHangButton, "/icons/iconKhachHang.png");
        activeIcons.put(quanLyKhachHangButton, "/icons/iconKhachHang_White.png");

        defaultIcons.put(quanLyKhuyenMaiButton, "/icons/iconKhuyenMai.png");
        activeIcons.put(quanLyKhuyenMaiButton, "/icons/iconKhuyenMai_White.png");

        defaultIcons.put(quanLyTraCuuButton, "/icons/iconTraCuu.png");
        activeIcons.put(quanLyTraCuuButton, "/icons/iconTraCuu_White.png");

        defaultIcons.put(dangXuatButton, "/icons/iconDangXuat.png");
        activeIcons.put(dangXuatButton, "/icons/iconDangXuat_White.png");
        
        activeButton = manHinhChinhButton;
        updateMenuStyles();

        // Load the main dashboard content
        handleManHinhChinh();
    }

    private void updateMenuStyles() {
        for (Button button : buttonIconMap.keySet()) {
            button.getStyleClass().remove("menu-button-active");
            button.getStyleClass().add("menu-button");
            
            ImageView icon = buttonIconMap.get(button);
            if (icon != null && defaultIcons.containsKey(button)) {
                icon.setImage(new Image(defaultIcons.get(button)));
            }
        }
        
        if (activeButton != null) {
            activeButton.getStyleClass().remove("menu-button");
            activeButton.getStyleClass().add("menu-button-active");
            
            ImageView activeIcon = buttonIconMap.get(activeButton);
            if (activeIcon != null && activeIcons.containsKey(activeButton)) {
                activeIcon.setImage(new Image(activeIcons.get(activeButton)));
            }
        }
    }

    @FXML
    private void handleManHinhChinh() throws IOException {
        activeButton = manHinhChinhButton;
        updateMenuStyles();
        
        Label welcomeLabel = new Label("Chào mừng đến với hệ thống quản lý nhà hàng TỪ HỮU!");
        welcomeLabel.setStyle("-fx-font-size: 24px; -fx-padding: 20px;");
        
        VBox container = new VBox(welcomeLabel);
        container.setAlignment(javafx.geometry.Pos.CENTER);
        
        contentArea.setCenter(container);
    }
    
    private void loadScreen(String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();
        contentArea.setCenter(root);
    }

    @FXML
    private void handleQuanLyDatBan() throws IOException {
        activeButton = quanLyDatBanButton;
        updateMenuStyles();
        loadScreen("/fxml/QuanLyDatBan.fxml");
    }

    @FXML
    private void handleQuanLyThongKe() throws IOException {
        activeButton = quanLyThongKeButton;
        updateMenuStyles();
        loadScreen("/fxml/QuanLyThongKe.fxml");
    }

    @FXML
    private void handleQuanLyThucDon() throws IOException {
        activeButton = quanLyThucDonButton;
        updateMenuStyles();
        loadScreen("/fxml/QuanLyThucDon.fxml");
    }

    @FXML
    private void handleQuanLyHoaDon() throws IOException {
        activeButton = quanLyHoaDonButton;
        updateMenuStyles();
        loadScreen("/fxml/QuanLyHoaDon.fxml");
    }
    
    @FXML
    private void handleQuanLyNhanVien() throws IOException {
        activeButton = quanLyNhanVienButton;
        updateMenuStyles();
        loadScreen("/fxml/QuanLyNhanVien.fxml");
    }

    @FXML
    private void handleQuanLyKhachHang() throws IOException {
        activeButton = quanLyKhachHangButton;
        updateMenuStyles();
        loadScreen("/fxml/QuanLyKhachHang.fxml");
    }

    @FXML
    private void handleQuanLyKhuyenMai() throws IOException {
        activeButton = quanLyKhuyenMaiButton;
        updateMenuStyles();
        loadScreen("/fxml/QuanLyKhuyenMai.fxml");
    }

    @FXML
    private void handleQuanLyTraCuu() throws IOException {
        activeButton = quanLyTraCuuButton;
        updateMenuStyles();
        loadScreen("/fxml/QuanLyTraCuu.fxml");
    }

    @FXML
    private void handleDangXuat() {
        activeButton = dangXuatButton;
        updateMenuStyles();
    }
}
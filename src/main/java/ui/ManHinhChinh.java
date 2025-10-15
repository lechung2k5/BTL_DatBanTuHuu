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
import java.net.URL;

public class ManHinhChinh {
    @FXML private BorderPane contentArea;
    @FXML private VBox menuItems;
    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;

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

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    public void initialize() {
        // Ánh xạ các nút và icon
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

        // Ánh xạ các đường dẫn icon
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
        
        // Đặt nút mặc định
        setActiveButton(manHinhChinhButton);
        try {
            // Load màn hình Dashboard mặc định khi khởi chạy
            loadScreen("/fxml/Dashboard.fxml", "/css/Dashboard.css");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addIconMapping(Button button, String defaultIcon, String activeIcon) {
        defaultIcons.put(button, defaultIcon);
        activeIcons.put(button, activeIcon);
    }
    
    private void updateMenuStyles() {
        buttonIconMap.forEach((button, icon) -> {
            if (button == null || icon == null) return;
            if (button == activeButton) {
                button.getStyleClass().setAll("menu-button-active");
                URL activeIconUrl = getClass().getResource(activeIcons.get(button));
                if (activeIconUrl != null) {
                    icon.setImage(new Image(activeIconUrl.toExternalForm()));
                }
            } else {
                button.getStyleClass().setAll("menu-button");
                URL defaultIconUrl = getClass().getResource(defaultIcons.get(button));
                if (defaultIconUrl != null) {
                    icon.setImage(new Image(defaultIconUrl.toExternalForm()));
                }
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
        
        // Đảm bảo BorderPane đã có Scene trước khi thêm CSS
        if (contentArea.getScene() != null) {
            // Xóa tất cả các style cũ
            contentArea.getScene().getStylesheets().clear();
            
            // Thêm các style mới
            contentArea.getScene().getStylesheets().add(getClass().getResource("/css/manHinhChinh.css").toExternalForm());
            contentArea.getScene().getStylesheets().add(getClass().getResource(cssPath).toExternalForm());
        }

        contentArea.setCenter(root);
    }

    @FXML private void handleManHinhChinh() throws IOException {
        setActiveButton(manHinhChinhButton);
        loadScreen("/fxml/Dashboard.fxml", "/css/Dashboard.css");
    }

    @FXML private void handleQuanLyDatBan() throws IOException {
        setActiveButton(quanLyDatBanButton);
        loadScreen("/fxml/QuanLyDatBan.fxml", "/css/DatBan.css");
    }

    @FXML private void handleQuanLyThongKe() throws IOException {
        setActiveButton(quanLyThongKeButton);
        loadScreen("/fxml/QuanLyThongKe.fxml", "/css/ThongKe.css");
    }

    @FXML private void handleQuanLyThucDon() throws IOException {
        setActiveButton(quanLyThucDonButton);
        loadScreen("/fxml/QuanLyThucDon.fxml", "/css/ThucDon.css");
    }

    @FXML private void handleQuanLyHoaDon() throws IOException {
        setActiveButton(quanLyHoaDonButton);
        loadScreen("/fxml/QuanLyHoaDon.fxml", "/css/HoaDon.css");
    }

    @FXML private void handleQuanLyNhanVien() throws IOException {
        setActiveButton(quanLyNhanVienButton);
        loadScreen("/fxml/QuanLyNhanVien.fxml", "/css/NhanVien.css");
    }

    @FXML private void handleQuanLyKhachHang() throws IOException {
        setActiveButton(quanLyKhachHangButton);
        loadScreen("/fxml/QuanLyKhachHang.fxml", "/css/KhachHang.css");
    }

    @FXML private void handleQuanLyKhuyenMai() throws IOException {
        setActiveButton(quanLyKhuyenMaiButton);
        loadScreen("/fxml/QuanLyKhuyenMai.fxml", "/css/KhuyenMai.css");
    }

    @FXML private void handleQuanLyTraCuu() throws IOException {
        setActiveButton(quanLyTraCuuButton);
        loadScreen("/fxml/QuanLyTraCuu.fxml", "/css/TraCuu.css");
    }

    @FXML private void handleDangXuat() {
        setActiveButton(dangXuatButton);
        System.out.println("Đăng xuất thành công!");
    }
}
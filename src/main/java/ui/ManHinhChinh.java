package ui;

import entity.TaiKhoan; // 🔥 Thêm import
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage; // 🔥 Thêm import Stage

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.net.URL;

public class ManHinhChinh {
    @FXML private BorderPane contentArea;
    @FXML private VBox menuItems;
    @FXML private Label userNameLabel; // Đã có fx:id
    @FXML private Label userRoleLabel; // Đã có fx:id
    @FXML private ImageView profileImage; // Đã có fx:id

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
    }

    /**
     * 🔥 HÀM MỚI: Nhận thông tin người dùng và cập nhật giao diện
     */
    public void setUserInfo(TaiKhoan user) {
        if (user != null && user.getNhanVien() != null) {
            userNameLabel.setText(user.getNhanVien().getHoTen());
            userRoleLabel.setText(user.getVaiTro() != null ? user.getVaiTro().getTenVaiTro() : "Không xác định");

            // Đặt ảnh đại diện mặc định
            try {
                 // Bạn cần có file ảnh này trong resources/icons
                profileImage.setImage(new Image(getClass().getResourceAsStream("/icons/iconUserPlaceholder.png")));
            } catch (Exception e) {
                 System.err.println("Không tìm thấy ảnh placeholder: /icons/iconUserPlaceholder.png");
                 // Giữ nguyên ảnh mặc định trong FXML nếu có lỗi
            }

        } else {
            // Xử lý trường hợp không có user (ví dụ: chạy trực tiếp màn hình chính)
            userNameLabel.setText("Khách");
            userRoleLabel.setText("Chưa đăng nhập");
             try {
                profileImage.setImage(new Image(getClass().getResourceAsStream("/icons/iconUserPlaceholder.png")));
            } catch (Exception e) {
                 System.err.println("Không tìm thấy ảnh placeholder: /icons/iconUserPlaceholder.png");
            }
        }
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
                 } catch (Exception e) { System.err.println("Lỗi load icon: " + iconPath); e.printStackTrace(); } // In stack trace để debug
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
        
        // Cập nhật CSS cho contentArea (nếu cần thiết cho từng màn hình con)
        // Lưu ý: Cách quản lý CSS này có thể cần xem lại nếu màn hình con phức tạp
        root.getStylesheets().clear(); // Xóa style cũ của root con
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

    // ... (Các hàm handle... khác giữ nguyên)
    @FXML private void handleManHinhChinh() throws IOException { /*...*/ setActiveButton(manHinhChinhButton); loadScreen("/fxml/Dashboard.fxml", "/css/Dashboard.css"); }
    @FXML private void handleQuanLyDatBan() throws IOException { /*...*/ setActiveButton(quanLyDatBanButton); loadScreen("/fxml/QuanLyDatBan.fxml", "/css/DatBan.css"); }
    @FXML private void handleQuanLyThongKe() throws IOException { /*...*/ setActiveButton(quanLyThongKeButton); loadScreen("/fxml/QuanLyThongKe.fxml", "/css/ThongKe.css"); }
    @FXML private void handleQuanLyThucDon() throws IOException { /*...*/ setActiveButton(quanLyThucDonButton); loadScreen("/fxml/QuanLyThucDon.fxml", "/css/ThucDon.css"); }
    @FXML private void handleQuanLyHoaDon() throws IOException { /*...*/ setActiveButton(quanLyHoaDonButton); loadScreen("/fxml/QuanLyHoaDon.fxml", "/css/HoaDon.css"); }
    @FXML private void handleQuanLyNhanVien() throws IOException { /*...*/ setActiveButton(quanLyNhanVienButton); loadScreen("/fxml/QuanLyNhanVien.fxml", "/css/NhanVien.css"); }
    @FXML private void handleQuanLyKhachHang() throws IOException { /*...*/ setActiveButton(quanLyKhachHangButton); loadScreen("/fxml/QuanLyKhachHang.fxml", "/css/KhachHang.css"); }
    @FXML private void handleQuanLyKhuyenMai() throws IOException { /*...*/ setActiveButton(quanLyKhuyenMaiButton); loadScreen("/fxml/QuanLyKhuyenMai.fxml", "/css/KhuyenMai.css"); }
    @FXML private void handleQuanLyTraCuu() throws IOException { /*...*/ setActiveButton(quanLyTraCuuButton); loadScreen("/fxml/QuanLyTraCuu.fxml", "/css/TraCuu.css"); }


    @FXML private void handleDangXuat() {
        setActiveButton(dangXuatButton);
        System.out.println("Đăng xuất..."); // Thay đổi thông báo

        // 🔥 ĐÃ SỬA: Logic đăng xuất hoàn chỉnh
        MainApp.setLoggedInUser(null); // Xóa thông tin người dùng đã đăng nhập

        if (mainApp != null) {
            // Lấy Stage hiện tại (cửa sổ màn hình chính) và đóng nó
            Stage currentStage = (Stage) dangXuatButton.getScene().getWindow();
            if (currentStage != null) {
                currentStage.close();
            }

            // Tạo và hiển thị lại màn hình đăng nhập
            // Cách 1: Gọi lại start của MainApp (đơn giản nhất)
            mainApp.start(new Stage());

            // Cách 2: Gọi trực tiếp gotoLogin (nếu bạn muốn giữ lại instance MainApp cũ)
            // Cần sửa lại MainApp.start và MainApp.gotoLogin để xử lý primaryStage đúng cách
            // mainApp.gotoLogin(); // Cần điều chỉnh MainApp để cách này hoạt động
        } else {
             System.err.println("Lỗi: Không thể đăng xuất vì mainApp là null.");
        }
    }
}
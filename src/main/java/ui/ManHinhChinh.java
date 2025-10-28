package ui;

import dao.KhachHangDAO; // << Import DAO Khách hàng
import entity.KhachHang; // << Import entity KhachHang
import entity.TaiKhoan;
import entity.VaiTro; // << Import VaiTro nếu chưa có

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty; // << Import BooleanProperty
import javafx.beans.property.SimpleBooleanProperty; // << Import SimpleBooleanProperty
import javafx.collections.FXCollections; // << Import FXCollections
import javafx.collections.ObservableList; // << Import ObservableList
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets; // << Import Insets
import javafx.geometry.Pos;   // << Import Pos
import javafx.scene.Parent;
import javafx.scene.Scene; // << Import Scene
import javafx.scene.control.*; // << Import các control chung
import javafx.scene.control.ButtonBar.ButtonData; // << Import ButtonData
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane; // << Import GridPane
import javafx.scene.layout.HBox;     // << Import HBox
import javafx.scene.layout.Priority; // << Import Priority
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javax.mail.*; // << Import JavaMail
import javax.mail.internet.*; // << Import JavaMail internet
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList; // << Import ArrayList
import java.util.HashMap;
import java.util.List; // << Import List
import java.util.Map;
import java.util.Optional;
import java.util.Properties; // << Import Properties
import java.util.stream.Collectors;


public class ManHinhChinh {
    // --- FXML Fields cũ ---
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

    // --- FXML Fields MỚI cho MenuItems ---
    @FXML private MenuItem doiMatKhauMenuItem;
    @FXML private MenuItem caiDatMenuItem;
    // MenuItem đăng xuất dùng chung handler handleDangXuat
    @FXML private MenuItem thoatMenuItem;
    @FXML private MenuItem quanLyBanMenuItem;
    // MenuItem quản lý khác dùng chung handler handleQuanLy...
    // MenuItem xem thống kê dùng chung handler handleQuanLyThongKe
    // MenuItem mở tra cứu dùng chung handler handleQuanLyTraCuu
    @FXML private MenuItem guiChuongTrinhTVMenuItem;
    @FXML private MenuItem xemLogKiemKeTienMatMenuItem; // <<< Đổi tên biến FXML nếu cần
    @FXML private MenuItem huongDanMenuItem;
    @FXML private MenuItem gioiThieuMenuItem;
    // ------------------------------------

    private MainApp mainApp;
    private Button activeButton;
    private final Map<Button, String> defaultIcons = new HashMap<>();
    private final Map<Button, String> activeIcons = new HashMap<>();
    private final Map<Button, ImageView> buttonIconMap = new HashMap<>();
    private final KhachHangDAO khachHangDAO = new KhachHangDAO(); // << Khởi tạo DAO

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    public void initialize() {
        // ... (Code initialize cũ gán icon và mappings giữ nguyên) ...
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
            // Cân nhắc hiển thị lỗi cho người dùng
        }
    }

    /**
     * Nhận thông tin người dùng và cập nhật giao diện
     */
    public void setUserInfo(TaiKhoan user) {
        // ... (Code setUserInfo cũ giữ nguyên) ...
        if (user != null && user.getNhanVien() != null) {
            userNameLabel.setText(user.getNhanVien().getHoTen());
            // Sửa lại cách lấy Vai trò nếu bạn dùng Enum hoặc đối tượng riêng
            userRoleLabel.setText(user.getVaiTro() != null ? user.getVaiTro().getTenVaiTro() : "Không xác định"); // << Sử dụng getTenVaiTro()

            // Đặt ảnh đại diện (giữ nguyên logic)
            try {
                // Bạn cần có file ảnh này trong resources/icons
                profileImage.setImage(new Image(getClass().getResourceAsStream("/icons/iconUserPlaceholder.png")));
            } catch (Exception e) {
                 System.err.println("Không tìm thấy ảnh placeholder: /icons/iconUserPlaceholder.png");
                 // Giữ nguyên ảnh mặc định trong FXML nếu có lỗi
            }

        } else {
            // Xử lý trường hợp không có user (giữ nguyên)
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
        // ... (Code cũ giữ nguyên) ...
        defaultIcons.put(button, defaultIcon);
        activeIcons.put(button, activeIcon);
    }

    private void updateMenuStyles() {
        // ... (Code cũ giữ nguyên) ...
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
        // ... (Code cũ giữ nguyên) ...
        this.activeButton = button;
        updateMenuStyles();
    }

     private void loadScreen(String fxmlPath, String cssPath) throws IOException {
        // ... (Code cũ giữ nguyên) ...
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent root = loader.load();
        root.getStylesheets().clear();
        URL globalCssUrl = getClass().getResource("/css/manHinhChinh.css"); // Giả sử có file global
        if (globalCssUrl != null) root.getStylesheets().add(globalCssUrl.toExternalForm());
         if(cssPath != null && !cssPath.isEmpty()) {
              URL specificCssUrl = getClass().getResource(cssPath);
              if (specificCssUrl != null) root.getStylesheets().add(specificCssUrl.toExternalForm());
              else System.err.println("Không tìm thấy file CSS: " + cssPath);
         }
        contentArea.setCenter(root);
    }

    // --- Các hàm handle... cũ cho các nút bên trái (Giữ nguyên) ---
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
        // ... (Code cũ đăng xuất giữ nguyên) ...
        setActiveButton(dangXuatButton);
        System.out.println("Đăng xuất...");
        MainApp.setLoggedInUser(null);
        if (mainApp != null) {
            Stage currentStage = (Stage) dangXuatButton.getScene().getWindow();
            if (currentStage != null) currentStage.close();
            mainApp.start(new Stage()); // Gọi lại start để hiện màn hình đăng nhập
        } else {
             System.err.println("Lỗi: Không thể đăng xuất vì mainApp là null.");
        }
    }

    // --- HÀM HANDLE MỚI cho MenuItems ---

    @FXML
    private void handleDoiMatKhau() {
        System.out.println("Chức năng Đổi mật khẩu được chọn.");
        showAlert("Thông báo", "Chức năng đang được phát triển.");
    }

    @FXML
    private void handleCaiDat() {
        System.out.println("Chức năng Cài đặt được chọn.");
        showAlert("Thông báo", "Chức năng đang được phát triển.");
    }

    @FXML
    private void handleThoat() {
        System.out.println("Thoát ứng dụng.");
        Platform.exit(); // Đóng ứng dụng JavaFX
    }

    @FXML
    private void handleQuanLyBan() throws IOException {
        System.out.println("Chức năng Quản lý Bàn được chọn.");
        // loadScreen("/fxml/QuanLyBan.fxml", "/css/QuanLyBan.css"); // Uncomment when ready
        setActiveButton(null);
        showAlert("Thông báo", "Chức năng Quản lý Bàn đang được phát triển.");
    }

    // --- 🔥 HÀM GỬI EMAIL ĐÃ CẬP NHẬT ---
    @FXML
    private void handleGuiChuongTrinhTV() {
        System.out.println("Chức năng Gửi Chương trình Thành viên được chọn.");

        // --- B1: Lấy danh sách khách hàng có email ---
        ObservableList<KhachHang> dsKhachHangCoEmail;
        try {
            ObservableList<KhachHang> allKhachHang = khachHangDAO.getAllKhachHang();
         // Lọc ra những KH có email
            List<KhachHang> filteredList = allKhachHang.stream() // <<< Thu thập vào List thường trước
                .filter(kh -> kh.getEmail() != null && !kh.getEmail().trim().isEmpty())
                .collect(Collectors.toList()); // <<< Dùng Collectors.toList()
            dsKhachHangCoEmail = FXCollections.observableArrayList(filteredList); // <<< Tạo ObservableList từ List

        } catch (Exception e) {
            showAlert("Lỗi", "Không thể tải danh sách khách hàng: " + e.getMessage());
            return;
        }

        if (dsKhachHangCoEmail.isEmpty()) {
            showAlert("Thông báo", "Không tìm thấy khách hàng nào có địa chỉ email.");
            return;
        }

        // --- B2: Tạo Dialog tùy chỉnh ---
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

        // --- B3: Xử lý kết quả Dialog ---
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

        // --- B4: Gửi Email nếu có kết quả ---
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
                         Platform.runLater(() -> showAlert("Lỗi Gửi Email", "Không thể gửi email: " + e.getMessage()));
                        e.printStackTrace();
                    }
                }).start();

            } catch (MessagingException e) {
                showAlert("Lỗi Tạo Email", "Không thể tạo email: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            showAlert("Đã hủy", "Thao tác gửi email đã được hủy bỏ.");
        }
    }
    // ------------------------------------

    // <<< THÊM HÀM MỚI >>>
    @FXML
    private void handleXemLogKiemKeTienMat() {
        System.out.println("Chức năng Xem log kiểm kê tiền mặt được chọn.");
        // TODO: Load màn hình xem log kiểm kê
        showAlert("Thông báo", "Chức năng Xem log kiểm kê tiền mặt đang được phát triển.");
        setActiveButton(null); // Bỏ highlight sidebar
    }
    // <<< KẾT THÚC HÀM MỚI >>>

    @FXML
    private void handleHuongDan() {
        System.out.println("Chức năng Hướng dẫn sử dụng được chọn.");
        showAlert("Thông báo", "Chức năng đang được phát triển.");
    }

    @FXML
    private void handleGioiThieu() {
        System.out.println("Chức năng Giới thiệu phần mềm được chọn.");
        showAlert("Thông báo", "Giới thiệu Phần mềm Quản lý Nhà hàng Tứ Hữu\nPhiên bản 1.0\nNhóm phát triển: ...");
    }

    // --- HÀM HELPER SHOW ALERT ---
    private void showAlert(String title, String content) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    // ------------------------------------
}
package ui;

import dao.TaiKhoanDAO; // Import DAO
import entity.TaiKhoan; // Import Entity
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert; // Import Alert
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
// 🔥 THÊM IMPORTS CHO PHÍM TẮT
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
// ------------------------------

public class DangNhap {
    @FXML
    private TextField txtMaNhanVien;

    @FXML
    private PasswordField txtPassword;

    private MainApp mainApp;
    private final TaiKhoanDAO taiKhoanDAO = new TaiKhoanDAO();

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    /**
     * 🔥 THÊM HÀM NÀY: Khởi tạo phím tắt khi scene đã load
     */
    @FXML
    private void initialize() {
        // Đợi scene được tạo xong để gán phím tắt
        txtMaNhanVien.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                setupKeyboardShortcuts();
            }
        });
    }

    /**
     * 🔥 HÀM MỚI: Thiết lập phím tắt
     */
    private void setupKeyboardShortcuts() {
        // Lấy scene hiện tại để thêm bộ lọc sự kiện phím
        txtMaNhanVien.getScene().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            // Enter hoặc Ctrl+S: Đăng nhập
            if (event.getCode() == KeyCode.ENTER ||
                (event.isControlDown() && event.getCode() == KeyCode.S)) {
                handleDangNhapButtonAction(null); // Gọi hàm đăng nhập
                event.consume(); // Ngăn sự kiện tiếp tục xử lý
            }
            // Esc: Xóa form
            else if (event.getCode() == KeyCode.ESCAPE) {
                clearForm(); // Gọi hàm xóa form
                event.consume();
            }
            // Ctrl+F: Focus vào ô mã nhân viên
            else if (event.isControlDown() && event.getCode() == KeyCode.F) {
                txtMaNhanVien.requestFocus();
                txtMaNhanVien.selectAll();
                event.consume();
            }
            // F1: Hiển thị trợ giúp phím tắt
            else if (event.getCode() == KeyCode.F1) {
                showKeyboardShortcutsHelp(); // Gọi hàm hiển thị trợ giúp
                event.consume();
            }
        });
    }

    @FXML
    void handleDangNhapButtonAction(ActionEvent event) {
        String maNhanVien = txtMaNhanVien.getText().trim();
        String password = txtPassword.getText();

        if (maNhanVien.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng nhập Mã nhân viên / Tên đăng nhập.");
            txtMaNhanVien.requestFocus();
            return;
        }
        if (password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng nhập Mật khẩu.");
            txtPassword.requestFocus();
            return;
        }

        TaiKhoan taiKhoan = taiKhoanDAO.kiemTraDangNhap(maNhanVien, password);

        if (taiKhoan != null) {
            // 🔥 THÊM THÔNG BÁO THÀNH CÔNG 🔥


            // Lưu thông tin người dùng vào MainApp
            MainApp.setLoggedInUser(taiKhoan);

            // Chuyển sang màn hình chính (sử dụng MainApp)
            if (mainApp != null) {
                mainApp.gotoMainScreen();
            }
            // MainApp sẽ tự đóng cửa sổ này

        } else {
            // Đăng nhập thất bại
            showErrorAlert("Đăng nhập thất bại", "Mã nhân viên hoặc mật khẩu không chính xác.");
            txtPassword.clear(); // Xóa ô mật khẩu
            txtPassword.requestFocus();
        }
    }

    @FXML
    private void handleQuenMatKhau(ActionEvent event) {
        if (mainApp != null) {
            mainApp.gotoQuenMatKhau();
        }
    }

    /**
     * 🔥 HÀM MỚI: Xóa toàn bộ form
     */
    private void clearForm() {
        txtMaNhanVien.clear();
        txtPassword.clear();
        txtMaNhanVien.requestFocus(); // Đưa con trỏ về ô mã nhân viên
    }

    /**
     * 🔥 HÀM MỚI: Hiển thị hướng dẫn phím tắt
     */
    private void showKeyboardShortcutsHelp() {
        String helpText = """
                ⌨️ PHÍM TẮT ĐĂNG NHẬP:
                \s
                Enter hoặc Ctrl+S  →  Đăng nhập
                Esc                →  Xóa toàn bộ form
                Ctrl+F             →  Focus vào ô mã nhân viên
                F1                 →  Hiển thị trợ giúp này
                """;

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Hướng dẫn phím tắt");
        alert.setHeaderText("📖 Phím tắt có sẵn");
        alert.setContentText(helpText);
        alert.showAndWait();
    }

    // --- Hàm tiện ích hiển thị thông báo ---
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
     private void showErrorAlert(String title, String content) {
        showAlert(Alert.AlertType.ERROR, title, content);
    }

    private void showInfoAlert(String title, String content) {
        showAlert(Alert.AlertType.INFORMATION, title, content);
    }
    // -----------------------------------------
}
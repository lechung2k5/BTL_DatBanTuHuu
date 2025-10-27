package ui;

import dao.TaiKhoanDAO;
import entity.TaiKhoan;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

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
        // Đợi scene được tạo xong
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
        txtMaNhanVien.getScene().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            // Enter hoặc Ctrl+S: Đăng nhập
            if (event.getCode() == KeyCode.ENTER || 
                (event.isControlDown() && event.getCode() == KeyCode.S)) {
                handleDangNhapButtonAction(null);
                event.consume();
            }
            
            // Esc: Xóa form
            else if (event.getCode() == KeyCode.ESCAPE) {
                clearForm();
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
                showKeyboardShortcutsHelp();
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
            showInfoAlert("Đăng nhập thành công", "Xin chào " + taiKhoan.getNhanVien().getHoTen() + "!");

            // Lưu thông tin người dùng vào MainApp
            MainApp.setLoggedInUser(taiKhoan);

            // Chuyển sang màn hình chính
            if (mainApp != null) {
                mainApp.gotoMainScreen();
            }

        } else {
            // Đăng nhập thất bại
            showErrorAlert("Đăng nhập thất bại", "Mã nhân viên hoặc mật khẩu không chính xác.");
            txtPassword.clear();
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
        txtMaNhanVien.requestFocus();
    }

    /**
     * 🔥 HÀM MỚI: Hiển thị hướng dẫn phím tắt
     */
    private void showKeyboardShortcutsHelp() {
        String helpText = """
            ⌨️ PHÍM TẮT ĐĂNG NHẬP:
            
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
}
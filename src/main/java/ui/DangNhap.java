package ui;

import dao.TaiKhoanDAO; // Import DAO
import entity.TaiKhoan; // Import Entity
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert; // Import Alert
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.Node; // Import Node

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
           

            // 🔥 THÊM DÒNG NÀY: Lưu thông tin người dùng vào MainApp
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
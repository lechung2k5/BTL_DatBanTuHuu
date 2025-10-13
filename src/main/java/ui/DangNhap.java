package ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class DangNhap {
    @FXML
    private TextField txtMaNhanVien;

    @FXML
    private PasswordField txtPassword;

    @FXML
    void handleDangNhapButtonAction(ActionEvent event) {
        String maNhanVien = txtMaNhanVien.getText();
        String password = txtPassword.getText();

        // Logic xác thực đơn giản
        if ("admin".equals(maNhanVien) && "123".equals(password)) {
            try {
                // Đóng cửa sổ đăng nhập
                Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
                stage.close();

                // Mở màn hình chính
                Parent root = FXMLLoader.load(getClass().getResource("/fxml/ManHinhChinh.fxml"));
                Stage mainStage = new Stage();
                mainStage.setTitle("Quản lý nhà hàng");
                mainStage.setScene(new Scene(root));
                mainStage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Mã nhân viên hoặc mật khẩu không đúng.");
        }
    }
}
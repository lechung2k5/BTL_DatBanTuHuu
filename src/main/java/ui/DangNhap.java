package ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.Node;

public class DangNhap {
    @FXML
    private TextField txtMaNhanVien;

    @FXML
    private PasswordField txtPassword;

    private MainApp mainApp;

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    void handleDangNhapButtonAction(ActionEvent event) {
        String maNhanVien = txtMaNhanVien.getText();
        String password = txtPassword.getText();

        if ("admin".equals(maNhanVien) && "123".equals(password)) {
            if (mainApp != null) {
                mainApp.gotoMainScreen();
            }
        } else {
            System.out.println("Mã nhân viên hoặc mật khẩu không đúng.");
        }
    }
}
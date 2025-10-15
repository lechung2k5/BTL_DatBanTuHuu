package ui;

import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.io.IOException;

public class QuenMatKhau {
    @FXML private TextField txtMaNhanVien;
    @FXML private Button btnXacNhan;

    private MainApp mainApp;

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void handleXacNhan(ActionEvent event) {
        System.out.println("Xác nhận đổi mật khẩu cho mã nhân viên: " + txtMaNhanVien.getText());
    }

    @FXML
    private void handleQuayLai(ActionEvent event) throws IOException {
        if (mainApp != null) {
            mainApp.gotoLogin();
        }
    }
}
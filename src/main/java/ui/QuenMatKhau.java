package ui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.Node;

import java.io.IOException;

public class QuenMatKhau {
    @FXML
    private TextField txtMaNhanVien;

    private MainApp mainApp;

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void handleXacNhan(ActionEvent event) {
        String maNhanVien = txtMaNhanVien.getText();
        
        if (!maNhanVien.isEmpty()) {
            System.out.println("Xác nhận đổi mật khẩu cho mã nhân viên: " + maNhanVien);
            // Xử lý logic khôi phục mật khẩu ở đây
        } else {
            System.out.println("Vui lòng nhập mã nhân viên.");
        }
    }

    @FXML
    private void handleQuayLai(ActionEvent event) throws IOException {
        if (mainApp != null) {
            mainApp.gotoLogin();
        }
    }
}
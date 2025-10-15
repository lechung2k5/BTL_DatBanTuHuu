package ui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;

public class DatBan {

    @FXML private TextField txtTenKhachHang;
    @FXML private TextField txtSoDienThoai;
    @FXML private TextField txtSoLuongKhach;
    @FXML private TextField txtNgayDat;
    @FXML private TextField txtGioDat;
    @FXML private TextField txtTienCoc;
    @FXML private TableView tblBan;

    @FXML
    private void handleDatBan(ActionEvent event) {
        // Lấy dữ liệu từ các trường nhập liệu
        String tenKhachHang = txtTenKhachHang.getText();
        String soDienThoai = txtSoDienThoai.getText();
        // ... (lấy các trường khác)

        // Hiển thị thông báo hoặc xử lý logic đặt bàn
        System.out.println("Đã đặt bàn cho khách hàng: " + tenKhachHang);
    }
}
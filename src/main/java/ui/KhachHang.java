package ui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;

public class KhachHang {

    @FXML private TextField txtMaKH;
    @FXML private TextField txtHoTen;
    @FXML private TextField txtSDT;
    @FXML private TableView tblKhachHang;

    @FXML
    private void initialize() {
        // Khởi tạo bảng dữ liệu và load dữ liệu từ CSDL
    }

    @FXML
    private void handleThemKH(ActionEvent event) {
        // Lấy dữ liệu từ các trường nhập liệu
        String maKH = txtMaKH.getText();
        String hoTen = txtHoTen.getText();
        String sdt = txtSDT.getText();

        // Xử lý logic thêm khách hàng vào CSDL và cập nhật bảng
        System.out.println("Đã thêm khách hàng: " + hoTen);
    }

    @FXML
    private void handleSuaKH(ActionEvent event) {
        // Xử lý logic sửa thông tin khách hàng
    }

    @FXML
    private void handleXoaKH(ActionEvent event) {
        // Xử lý logic xóa khách hàng
    }
}
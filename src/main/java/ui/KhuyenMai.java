package ui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;

public class KhuyenMai {

    @FXML private TextField txtMaKM;
    @FXML private TextField txtTenChuongTrinh;
    @FXML private TextField txtGiaTriGiam;
    @FXML private TableView tblKhuyenMai;

    @FXML
    private void initialize() {
        // Khởi tạo bảng dữ liệu và load dữ liệu từ CSDL
    }

    @FXML
    private void handleThemKM(ActionEvent event) {
        // Lấy dữ liệu từ các trường nhập liệu
        String maKM = txtMaKM.getText();
        String tenChuongTrinh = txtTenChuongTrinh.getText();
        String giaTriGiam = txtGiaTriGiam.getText();

        // Xử lý logic thêm khuyến mãi vào CSDL và cập nhật bảng
        System.out.println("Đã thêm chương trình khuyến mãi: " + tenChuongTrinh);
    }

    @FXML
    private void handleSuaKM(ActionEvent event) {
        // Xử lý logic sửa thông tin khuyến mãi
    }

    @FXML
    private void handleXoaKM(ActionEvent event) {
        // Xử lý logic xóa khuyến mãi
    }
}
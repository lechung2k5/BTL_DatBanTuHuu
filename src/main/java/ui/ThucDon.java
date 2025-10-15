package ui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;

public class ThucDon {

    @FXML private TextField txtMaMon;
    @FXML private TextField txtTenMon;
    @FXML private TextField txtGiaBan;
    @FXML private TableView tblThucDon;

    @FXML
    private void initialize() {
        // Khởi tạo bảng dữ liệu và load dữ liệu từ CSDL
    }

    @FXML
    private void handleThemMon(ActionEvent event) {
        // Lấy dữ liệu từ các trường nhập liệu
        String maMon = txtMaMon.getText();
        String tenMon = txtTenMon.getText();
        String giaBan = txtGiaBan.getText();

        // Xử lý logic thêm món ăn vào CSDL và cập nhật bảng
        System.out.println("Đã thêm món: " + tenMon);
    }

    @FXML
    private void handleSuaMon(ActionEvent event) {
        // Xử lý logic sửa món ăn
    }

    @FXML
    private void handleXoaMon(ActionEvent event) {
        // Xử lý logic xóa món ăn
    }
}
package ui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;

public class TraCuu {

    @FXML private TextField txtTimKiem;
    @FXML private TableView tblKetQua;
    @FXML private TableColumn colMa;
    @FXML private TableColumn colTen;
    @FXML private TableColumn colLoai;
    @FXML private TableColumn colNgay;

    @FXML
    private void initialize() {
        // Khởi tạo bảng dữ liệu
    }

    @FXML
    private void handleTraCuu(ActionEvent event) {
        // Lấy từ khóa từ trường nhập liệu
        String tuKhoa = txtTimKiem.getText();

        // Xử lý logic tra cứu trong CSDL và cập nhật bảng
        System.out.println("Đã tra cứu với từ khóa: " + tuKhoa);
    }
}
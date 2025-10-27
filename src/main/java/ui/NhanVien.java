package ui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;

public class NhanVien {

    @FXML private TextField txtMaNV;
    @FXML private TextField txtHoTen;
    @FXML private TextField txtChucVu;
    @FXML private TableView tblNhanVien;

    @FXML
    private void initialize() {
        // Khởi tạo bảng dữ liệu và load dữ liệu từ CSDL
    }

    @FXML
    private void handleThemNV(ActionEvent event) {
        // Lấy dữ liệu từ các trường nhập liệu
        String maNV = txtMaNV.getText();
        String hoTen = txtHoTen.getText();
        String chucVu = txtChucVu.getText();

        // Xử lý logic thêm nhân viên vào CSDL và cập nhật bảng
        System.out.println("Đã thêm nhân viên: " + hoTen);
    }

    @FXML
    private void handleSuaNV(ActionEvent event) {
        // Xử lý logic sửa thông tin nhân viên
    }

    @FXML
    private void handleXoaNV(ActionEvent event) {
        // Xử lý logic xóa nhân viên
    }
}
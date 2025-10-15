package ui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.event.ActionEvent;

public class HoaDon {

    @FXML private TableView tblHoaDon;
    @FXML private TableColumn colMaHD;
    @FXML private TableColumn colNgayTao;
    @FXML private TableColumn colNhanVien;
    @FXML private TableColumn colTongTien;

    @FXML
    private void initialize() {
        // Khởi tạo bảng dữ liệu và load dữ liệu từ CSDL
    }

    @FXML
    private void handleChiTietHoaDon(ActionEvent event) {
        // Xử lý logic xem chi tiết hóa đơn
        System.out.println("Đã xem chi tiết hóa đơn.");
    }

    @FXML
    private void handleInHoaDon(ActionEvent event) {
        // Xử lý logic in hóa đơn
        System.out.println("Đã in hóa đơn.");
    }
}
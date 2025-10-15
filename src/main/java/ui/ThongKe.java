package ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class ThongKe {

    @FXML
    private Label lblTongDoanhThu;

    @FXML
    private Label lblSoLuongDon;

    @FXML
    public void initialize() {
        // Khởi tạo dữ liệu thống kê
        loadData();
    }

    private void loadData() {
        // Load dữ liệu từ cơ sở dữ liệu và hiển thị lên giao diện
        // Ví dụ: lblTongDoanhThu.setText("250,000,000 VNĐ");
        // ...
    }
}
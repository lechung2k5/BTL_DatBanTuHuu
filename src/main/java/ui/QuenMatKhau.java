package ui;

import dao.TaiKhoanDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox; // Import HBox

import java.io.IOException;

public class QuenMatKhau {
    @FXML private TextField txtMaNhanVien;
    @FXML private PasswordField pfMatKhauMoi; // Thêm khai báo
    @FXML private PasswordField pfXacNhanMatKhau; // Thêm khai báo
    @FXML private Button btnXacNhanMaNV; // Nút kiểm tra mã
    @FXML private Button btnDoiMatKhau; // Nút đổi mật khẩu
    @FXML private HBox hboxMatKhauMoi; // HBox chứa ô MK mới
    @FXML private HBox hboxXacNhanMK; // HBox chứa ô xác nhận MK

    private MainApp mainApp;
    private final TaiKhoanDAO taiKhoanDAO = new TaiKhoanDAO();

    @FXML
    private void initialize() {
        // Ban đầu ẩn các trường mật khẩu và nút Đổi MK
        setPasswordFieldVisibility(false);
    }

    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void handleXacNhan(ActionEvent event) { // Nút "Xác nhận Mã NV"
        String maNhanVien = txtMaNhanVien.getText().trim();

        if (maNhanVien.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng nhập mã nhân viên.");
            txtMaNhanVien.requestFocus();
            return;
        }

        boolean taiKhoanTonTai = taiKhoanDAO.kiemTraTonTaiTaiKhoan(maNhanVien);

        if (taiKhoanTonTai) {
            // 🔥 ĐÃ SỬA: Hiển thị phần nhập mật khẩu mới
            showAlert(Alert.AlertType.INFORMATION, "Tìm thấy tài khoản",
                      "Tìm thấy tài khoản '" + maNhanVien + "'. Vui lòng nhập mật khẩu mới.");
            setPasswordFieldVisibility(true); // Hiện các ô mật khẩu và nút Đổi MK
            txtMaNhanVien.setEditable(false); // Khóa ô mã NV lại
            pfMatKhauMoi.requestFocus();

        } else {
            // Xử lý khi không tìm thấy
            showAlert(Alert.AlertType.ERROR, "Không tìm thấy",
                      "Không tìm thấy tài khoản nào ứng với mã nhân viên '" + maNhanVien + "'.");
            txtMaNhanVien.requestFocus();
            txtMaNhanVien.selectAll();
        }
    }

    @FXML
    private void handleDoiMatKhau(ActionEvent event) { // Nút "Đổi Mật Khẩu"
        String maNhanVien = txtMaNhanVien.getText().trim(); // Lấy lại mã NV (đã bị khóa)
        String matKhauMoi = pfMatKhauMoi.getText();
        String xacNhanMK = pfXacNhanMatKhau.getText();

        // Validate mật khẩu mới
        if (matKhauMoi.isEmpty()) {
             showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng nhập mật khẩu mới.");
             pfMatKhauMoi.requestFocus();
             return;
        }
         if (xacNhanMK.isEmpty()) {
             showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng xác nhận mật khẩu mới.");
             pfXacNhanMatKhau.requestFocus();
             return;
        }
        if (!matKhauMoi.equals(xacNhanMK)) {
            showAlert(Alert.AlertType.WARNING, "Lỗi Nhập Liệu", "Mật khẩu xác nhận không khớp.");
            pfXacNhanMatKhau.requestFocus();
            pfXacNhanMatKhau.selectAll();
            return;
        }
        // Có thể thêm validate độ phức tạp mật khẩu ở đây

        // Gọi DAO để cập nhật mật khẩu
        if (taiKhoanDAO.doiMatKhau(maNhanVien, matKhauMoi)) {
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đổi mật khẩu thành công!");
            if (mainApp != null) {
                mainApp.gotoLogin(); // Quay về đăng nhập
            }
        } else {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Đã xảy ra lỗi khi đổi mật khẩu. Vui lòng thử lại.");
        }
    }


    @FXML
    private void handleQuayLai(ActionEvent event) throws IOException {
        if (mainApp != null) {
            // Reset lại giao diện trước khi quay lại (ẩn các ô MK)
            setPasswordFieldVisibility(false);
            txtMaNhanVien.clear();
            txtMaNhanVien.setEditable(true);
            mainApp.gotoLogin();
        }
    }

    /**
     * 🔥 HÀM MỚI: Ẩn/hiện các thành phần liên quan đến đổi mật khẩu
     */
    private void setPasswordFieldVisibility(boolean visible) {
        hboxMatKhauMoi.setVisible(visible);
        hboxMatKhauMoi.setManaged(visible); // Quan trọng: Bỏ quản lý layout khi ẩn
        hboxXacNhanMK.setVisible(visible);
        hboxXacNhanMK.setManaged(visible);
        btnDoiMatKhau.setVisible(visible);
        btnDoiMatKhau.setManaged(visible);

        // Ẩn/hiện nút Xác nhận mã NV tương ứng
        btnXacNhanMaNV.setVisible(!visible);
        btnXacNhanMaNV.setManaged(!visible);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
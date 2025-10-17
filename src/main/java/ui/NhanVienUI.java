package ui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import entity.NhanVien;

public class NhanVienUI {

    @FXML private TextField txtMaNV;
    @FXML private TextField txtHoTen;
    @FXML private TextField txtSDT;
    @FXML private ComboBox<String> cbxChucVu;
    @FXML private DatePicker dpNgayVaoLam;
    @FXML private ComboBox<String> cbxCaLam;
    @FXML private PasswordField pfMatKhau;
    @FXML private ComboBox<String> cbxTrangThai;
    @FXML private TextField txtTimKiem; // Thêm ô tìm kiếm

    @FXML private TableView<NhanVien> tblNhanVien;
    @FXML private TableColumn<NhanVien, String> colMaNV;
    @FXML private TableColumn<NhanVien, String> colHoTen;
    @FXML private TableColumn<NhanVien, String> colSDT;
    @FXML private TableColumn<NhanVien, String> colChucVu;
    @FXML private TableColumn<NhanVien, String> colMatKhau;
    @FXML private TableColumn<NhanVien, String> colNgayVaoLam;
    @FXML private TableColumn<NhanVien, String> colCaLam;
    @FXML private TableColumn<NhanVien, String> colTrangThai;

    private ObservableList<NhanVien> nhanVienList;
    private FilteredList<NhanVien> filteredList;
    private SortedList<NhanVien> sortedList;
    private boolean isEditing = false;

    @FXML
    private void initialize() {
        // Initialize TableView columns
        colMaNV.setCellValueFactory(new PropertyValueFactory<>("maNV"));
        colHoTen.setCellValueFactory(new PropertyValueFactory<>("hoTen"));
        colSDT.setCellValueFactory(new PropertyValueFactory<>("sdt"));
        colChucVu.setCellValueFactory(new PropertyValueFactory<>("chucVu"));
        colMatKhau.setCellValueFactory(new PropertyValueFactory<>("matKhau"));
        colNgayVaoLam.setCellValueFactory(new PropertyValueFactory<>("ngayVaoLam"));
        colCaLam.setCellValueFactory(new PropertyValueFactory<>("caLam"));
        colTrangThai.setCellValueFactory(new PropertyValueFactory<>("trangThai"));

        // Initialize ComboBox options
        cbxChucVu.setItems(FXCollections.observableArrayList("Quản lý", "Thư ký", "Nhân viên"));
        cbxCaLam.setItems(FXCollections.observableArrayList("7h-13h", "13h-19h", "19h-1h"));
        cbxTrangThai.setItems(FXCollections.observableArrayList("Đang làm", "Nghỉ việc"));

        // Initialize employee list with sample data
        nhanVienList = FXCollections.observableArrayList(
            new NhanVien("NV01", "Nguyễn Văn A", "0901234567", "Quản lý", "password123", "29/03/2025", "7h-13h", "Đang làm"),
            new NhanVien("NV02", "Nguyễn Văn B", "0901234568", "Thư ký", "password456", "29/03/2025", "7h-13h", "Đang làm")
        );
        
        // Khởi tạo FilteredList với danh sách ban đầu
        filteredList = new FilteredList<>(nhanVienList, p -> true);

        // Lắng nghe sự thay đổi trong TextField tìm kiếm
        txtTimKiem.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredList.setPredicate(nhanVien -> {
                // Nếu ô tìm kiếm trống, hiển thị toàn bộ danh sách
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                // Chuyển đổi từ khóa tìm kiếm thành chữ thường
                String lowerCaseFilter = newValue.toLowerCase();

                // Kiểm tra xem từ khóa có khớp với bất kỳ thuộc tính nào của nhân viên không
                if (nhanVien.getMaNV().toLowerCase().contains(lowerCaseFilter)) {
                    return true; // Khớp với mã nhân viên
                } else if (nhanVien.getHoTen().toLowerCase().contains(lowerCaseFilter)) {
                    return true; // Khớp với họ và tên
                } else if (nhanVien.getChucVu().toLowerCase().contains(lowerCaseFilter)) {
                    return true; // Khớp với chức vụ
                }
                return false; // Không khớp
            });
        });

        // Gói FilteredList trong SortedList.
        // SortedList sẽ tự động cập nhật khi FilteredList thay đổi và cho phép sắp xếp theo cột.
        sortedList = new SortedList<>(filteredList);

        // Gắn bộ so sánh của SortedList với bộ so sánh của TableView.
        // Điều này đảm bảo rằng việc sắp xếp sẽ hoạt động trên dữ liệu đã được lọc.
        sortedList.comparatorProperty().bind(tblNhanVien.comparatorProperty());

        // Gán SortedList đã được lọc và sắp xếp cho TableView
        tblNhanVien.setItems(sortedList);

        // Add TableView selection listener
        tblNhanVien.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> showNhanVienDetails(newSelection)
        );
    }
    
    private void showNhanVienDetails(NhanVien nhanVien) {
        // Clear all fields first
        txtMaNV.setText("");
        txtHoTen.setText("");
        txtSDT.setText("");
        cbxChucVu.setValue(null);
        dpNgayVaoLam.setValue(null);
        cbxCaLam.setValue(null);
        pfMatKhau.setText("");
        cbxTrangThai.setValue(null);

        if (nhanVien != null) {
            try {
                // Set employee details
                txtMaNV.setText(nhanVien.getMaNV() != null ? nhanVien.getMaNV() : "");
                txtHoTen.setText(nhanVien.getHoTen() != null ? nhanVien.getHoTen() : "");
                txtSDT.setText(nhanVien.getSdt() != null ? nhanVien.getSdt() : "");
                cbxChucVu.setValue(nhanVien.getChucVu());
                cbxCaLam.setValue(nhanVien.getCaLam());
                pfMatKhau.setText(nhanVien.getMatKhau() != null ? nhanVien.getMatKhau() : "");
                cbxTrangThai.setValue(nhanVien.getTrangThai());

                // Parse and set date
                if (nhanVien.getNgayVaoLam() != null && !nhanVien.getNgayVaoLam().isEmpty()) {
                    try {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                        dpNgayVaoLam.setValue(LocalDate.parse(nhanVien.getNgayVaoLam(), formatter));
                    } catch (DateTimeParseException e) {
                        System.err.println("Invalid date format for employee " + nhanVien.getMaNV() + ": " + nhanVien.getNgayVaoLam());
                        dpNgayVaoLam.setValue(null);
                    }
                }
            } catch (Exception e) {
                showErrorAlert("Lỗi", "Không thể hiển thị thông tin nhân viên: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleThemNV(ActionEvent event) {
        // Clear form for adding new employee
        txtMaNV.setText(generateNewMaNV());
        txtHoTen.setText("");
        txtSDT.setText("");
        cbxChucVu.setValue(null);
        dpNgayVaoLam.setValue(LocalDate.now());
        cbxCaLam.setValue(null);
        pfMatKhau.setText("");
        cbxTrangThai.setValue("Đang làm");
        isEditing = false;
    }

    @FXML
    private void handleXoaNV(ActionEvent event) {
        NhanVien selected = tblNhanVien.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showErrorAlert("Lỗi", "Vui lòng chọn một nhân viên để xóa.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Bạn có chắc muốn xóa nhân viên " + selected.getHoTen() + "?");
        if (confirm.showAndWait().get() == ButtonType.OK) {
            nhanVienList.remove(selected);
            clearForm();
        }
    }

    @FXML
    private void handleSuaNV(ActionEvent event) {
        NhanVien selected = tblNhanVien.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showErrorAlert("Lỗi", "Vui lòng chọn một nhân viên để sửa.");
            return;
        }
        isEditing = true;
        showNhanVienDetails(selected);
        txtMaNV.setEditable(false); // MaNV should not be editable
    }

    @FXML
    private void handleLuuNV(ActionEvent event) {
        if (!validateInput()) {
            return;
        }

        NhanVien nhanVien = new NhanVien(
            txtMaNV.getText(),
            txtHoTen.getText(),
            txtSDT.getText(),
            cbxChucVu.getValue(),
            pfMatKhau.getText(),
            dpNgayVaoLam.getValue() != null ? dpNgayVaoLam.getValue().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "",
            cbxCaLam.getValue(),
            cbxTrangThai.getValue()
        );

        if (isEditing) {
            NhanVien selected = tblNhanVien.getSelectionModel().getSelectedItem();
            if (selected != null) {
                int index = nhanVienList.indexOf(selected);
                nhanVienList.set(index, nhanVien);
                showInfoAlert("Thành công", "Đã cập nhật thông tin nhân viên.");
            }
        } else {
            nhanVienList.add(nhanVien);
            showInfoAlert("Thành công", "Đã thêm nhân viên mới.");
        }

        isEditing = false;
        tblNhanVien.getSelectionModel().clearSelection();
        clearForm();
    }

    private boolean validateInput() {
        if (txtHoTen.getText().isEmpty()) {
            showErrorAlert("Lỗi", "Họ và tên không được để trống.");
            return false;
        }
        if (!txtSDT.getText().matches("\\d{10}")) {
            showErrorAlert("Lỗi", "Số điện thoại phải có đúng 10 chữ số.");
            return false;
        }
        if (cbxChucVu.getValue() == null) {
            showErrorAlert("Lỗi", "Vui lòng chọn chức vụ.");
            return false;
        }
        if (dpNgayVaoLam.getValue() == null) {
            showErrorAlert("Lỗi", "Vui lòng chọn ngày vào làm.");
            return false;
        }
        if (cbxCaLam.getValue() == null) {
            showErrorAlert("Lỗi", "Vui lòng chọn ca làm.");
            return false;
        }
        if (pfMatKhau.getText().isEmpty()) {
            showErrorAlert("Lỗi", "Mật khẩu không được để trống.");
            return false;
        }
        if (cbxTrangThai.getValue() == null) {
            showErrorAlert("Lỗi", "Vui lòng chọn trạng thái.");
            return false;
        }
        return true;
    }

    private String generateNewMaNV() {
        int maxId = nhanVienList.stream()
            .map(nv -> Integer.parseInt(nv.getMaNV().substring(2)))
            .max(Integer::compareTo)
            .orElse(0);
        return String.format("NV%02d", maxId + 1);
    }

    private void clearForm() {
        txtMaNV.setText("");
        txtHoTen.setText("");
        txtSDT.setText("");
        cbxChucVu.setValue(null);
        dpNgayVaoLam.setValue(null);
        cbxCaLam.setValue(null);
        pfMatKhau.setText("");
        cbxTrangThai.setValue(null);
    }

    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showInfoAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
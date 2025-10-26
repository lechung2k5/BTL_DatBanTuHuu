package ui;

// import dao.CaTrucDAO; // 🔥 BỎ CaTrucDAO
import dao.NhanVienDAO;
// import entity.CaTruc; // 🔥 BỎ CaTruc
import entity.NhanVien;
import entity.VaiTro;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

// import java.sql.Time; // 🔥 BỎ
import java.time.LocalDate;
// import java.time.LocalTime; // 🔥 BỎ
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern; // Import Pattern

public class NhanVienUI {

    //<editor-fold desc="FXML Declarations">
    @FXML private TextField txtMaNV;
    @FXML private TextField txtHoTen;
    @FXML private TextField txtSDT;
    @FXML private ComboBox<String> cbxChucVu;
    @FXML private DatePicker dpNgaySinh;
    @FXML private ComboBox<String> cbxCaLam; // Sẽ dùng cho ca YÊU THÍCH
    @FXML private PasswordField pfMatKhau;
    @FXML private ComboBox<String> cbxTrangThai;
    @FXML private TextField txtTimKiem;

    @FXML private TableView<NhanVien> tblNhanVien;
    @FXML private TableColumn<NhanVien, String> colMaNV;
    @FXML private TableColumn<NhanVien, String> colHoTen;
    @FXML private TableColumn<NhanVien, String> colSDT;
    @FXML private TableColumn<NhanVien, String> colChucVu;
    @FXML private TableColumn<NhanVien, String> colNgaySinh;
    @FXML private TableColumn<NhanVien, String> colCaLam; // (FXML id vẫn là colCaLam)
    @FXML private TableColumn<NhanVien, String> colTrangThai;

    // Khai báo đúng 6 nút
    @FXML private Button btnThem; // Nút "Thêm" (Lưu mới)
    @FXML private Button btnSua;  // Nút "Sửa" (Cập nhật ngay)
    @FXML private Button btnXoa;
    @FXML private Button btnLuu;  // Nút "Lưu" (Không dùng)
    @FXML private Button btnHuy;
    @FXML private Button btnXoaRong; // Nút "Xóa rỗng" (Bắt đầu thêm)
    //</editor-fold>

    private ObservableList<NhanVien> nhanVienList;
    private final NhanVienDAO nhanVienDAO = new NhanVienDAO();
    // private final CaTrucDAO caTrucDAO = new CaTrucDAO(); // 🔥 BỎ
    private NhanVien currentSelectedNhanVien = null;

    // Trạng thái giao diện: VIEWING (đang xem/sửa) hoặc ADDING (đang thêm mới)
    private enum EditState { VIEWING, ADDING }
    private EditState currentState = EditState.VIEWING;

    @FXML
    private void initialize() {
        nhanVienList = FXCollections.observableArrayList();

        // Cấu hình cột
        colMaNV.setCellValueFactory(new PropertyValueFactory<>("maNV"));
        colHoTen.setCellValueFactory(new PropertyValueFactory<>("hoTen"));
        colSDT.setCellValueFactory(new PropertyValueFactory<>("sdt"));
        colChucVu.setCellValueFactory(new PropertyValueFactory<>("chucVu"));
        colNgaySinh.setCellValueFactory(new PropertyValueFactory<>("ngaySinh"));
        
        // 🔥 THAY ĐỔI QUAN TRỌNG: Bind cột vào "caLamYeuThich"
        colCaLam.setCellValueFactory(new PropertyValueFactory<>("caLamYeuThich")); 
        
        colTrangThai.setCellValueFactory(new PropertyValueFactory<>("trangThai"));

        // Cấu hình ComboBox
        ObservableList<String> vaiTroStrings = FXCollections.observableArrayList();
        for (VaiTro vaiTro : VaiTro.values()) { vaiTroStrings.add(vaiTro.getTenVaiTro()); }
        cbxChucVu.setItems(vaiTroStrings);
        cbxTrangThai.setItems(FXCollections.observableArrayList("Đang làm", "Nghỉ việc"));
        
        // Dùng danh sách code cứng cho Ca làm YÊU THÍCH
        cbxCaLam.setItems(FXCollections.observableArrayList("Sáng", "Chiều", "Tối", "Nguyên ngày"));
        cbxCaLam.setPromptText("Chọn ca yêu thích...");


        // Cấu hình tìm kiếm
        FilteredList<NhanVien> filteredList = new FilteredList<>(nhanVienList, p -> true);
        txtTimKiem.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredList.setPredicate(nv -> {
                if (newVal == null || newVal.isEmpty()) return true;
                String lowerCaseFilter = newVal.toLowerCase();
                return nv.getMaNV().toLowerCase().contains(lowerCaseFilter) ||
                       nv.getHoTen().toLowerCase().contains(lowerCaseFilter);
            });
        });
        SortedList<NhanVien> sortedList = new SortedList<>(filteredList);
        sortedList.comparatorProperty().bind(tblNhanVien.comparatorProperty());
        tblNhanVien.setItems(sortedList);

        // Listener cho bảng: Khi chọn dòng -> VIEWING, hiển thị data, mở khóa form
        tblNhanVien.getSelectionModel().selectedItemProperty().addListener(
            (obs, oldSelection, newSelection) -> {
                if (currentState == EditState.ADDING) return; // Không làm gì nếu đang thêm
                currentSelectedNhanVien = newSelection;
                if (newSelection != null) {
                    showNhanVienDetails(newSelection);
                    setFormEditable(true); // Mở khóa form ngay khi chọn
                } else {
                    clearForm();
                    setFormEditable(false); // Khóa form nếu không chọn
                }
                updateUIState(EditState.VIEWING); // Luôn quay về VIEWING khi chọn dòng
            }
        );

        loadNhanVienData(); // Tải dữ liệu lần đầu
    }

    //<editor-fold desc="State Management & UI Control">
    private void setFormEditable(boolean editable) {
        txtMaNV.setEditable(false);
        txtHoTen.setEditable(editable);
        txtSDT.setEditable(editable);
        pfMatKhau.setEditable(editable);
        cbxChucVu.setDisable(!editable);
        dpNgaySinh.setDisable(!editable);
        cbxCaLam.setDisable(!editable); // ComboBox đã được mở
        cbxTrangThai.setDisable(!editable);
    }

    private void updateUIState(EditState state) {
        this.currentState = state;
        boolean rowIsSelected = (currentSelectedNhanVien != null);

        if (state == EditState.ADDING) {
            // Khi đang thêm mới (sau khi bấm Xóa rỗng)
            setFormEditable(true);      // Mở khóa form
            btnXoaRong.setDisable(true);  // Tắt "Xóa rỗng"
            btnThem.setDisable(false);     // Bật "Thêm" (Lưu mới)
            btnSua.setDisable(true);
            btnLuu.setDisable(true);      // Tắt nút "Lưu"
            btnXoa.setDisable(true);
            btnHuy.setDisable(false);
            tblNhanVien.setDisable(true);  // Khóa bảng
        } else { // VIEWING state (Đang xem hoặc đang sửa trên dòng đã chọn)
            setFormEditable(rowIsSelected); // Form sửa được nếu có dòng được chọn
            btnXoaRong.setDisable(false); // Bật "Xóa rỗng"
            btnThem.setDisable(true);      // Tắt "Thêm" (Lưu mới)
            btnSua.setDisable(!rowIsSelected); // Bật Sửa/Xóa/Hủy nếu có dòng được chọn
            btnLuu.setDisable(true);      // Tắt nút "Lưu"
            btnXoa.setDisable(!rowIsSelected);
            btnHuy.setDisable(!rowIsSelected); // Hủy chỉ bật khi có dòng đang chọn (để revert)
            tblNhanVien.setDisable(false); // Mở khóa bảng
        }
    }
    //</editor-fold>

    //<editor-fold desc="Event Handlers">
    @FXML
    private void handleXoaRong(ActionEvent event) { // Nút "Xóa rỗng" -> Bắt đầu thêm
        tblNhanVien.getSelectionModel().clearSelection();
        clearForm();
        txtMaNV.setText(generateNewMaNV());
        pfMatKhau.setPromptText("Bắt buộc nhập");
        updateUIState(EditState.ADDING); // Chuyển sang trạng thái ADDING
        txtHoTen.requestFocus();
    }

    @FXML
    private void handleThem(ActionEvent event) { // Nút "Thêm" -> Lưu nhân viên mới
        if (currentState != EditState.ADDING || !validateInput()) return;

        NhanVien nv = createNhanVienFromForm();
        if (nhanVienDAO.themNhanVien(nv)) {
            showInfoAlert("Thành công", "Đã thêm nhân viên mới!");
            loadNhanVienData(); // Tải lại và quay về VIEWING
        } else {
            showErrorAlert("Thất bại", "Thêm mới không thành công.");
        }
    }

    @FXML
    private void handleSua(ActionEvent event) { // Nút "Sửa" -> Cập nhật ngay dòng đang chọn
        if (currentSelectedNhanVien == null || currentState != EditState.VIEWING || !validateInput()) return;

        NhanVien nv = createNhanVienFromForm(); // Lấy dữ liệu từ form
        if (nhanVienDAO.capNhatNhanVien(nv)) {
            showInfoAlert("Thành công", "Đã cập nhật thông tin nhân viên!");
            loadNhanVienData(); // Tải lại và quay về VIEWING
        } else {
            showErrorAlert("Thất bại", "Cập nhật không thành công.");
        }
    }

    @FXML
    private void handleLuu(ActionEvent event) { // Nút "Lưu" -> Không làm gì cả
        System.out.println("Nút Lưu không có chức năng trong quy trình này.");
    }

    @FXML
    private void handleXoa(ActionEvent event) { // Nút "Xóa"
        if (currentSelectedNhanVien == null || currentState != EditState.VIEWING) return;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setContentText("Bạn có chắc chắn muốn xóa nhân viên '" + currentSelectedNhanVien.getHoTen() + "'?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (nhanVienDAO.xoaNhanVien(currentSelectedNhanVien.getMaNV())) {
                showInfoAlert("Thành công", "Đã xóa nhân viên!");
                loadNhanVienData();
            } else {
                showErrorAlert("Thất bại", "Xóa nhân viên không thành công.");
            }
        }
    }

    @FXML
    private void handleHuy(ActionEvent event) { // Nút "Hủy"
        if (currentState == EditState.ADDING) {
            loadNhanVienData(); // Quay về trạng thái xem ban đầu
        } else if (currentSelectedNhanVien != null) {
            showNhanVienDetails(currentSelectedNhanVien); // Khôi phục dữ liệu gốc của dòng đang chọn
        }
    }
    //</border-fold>

    //<editor-fold desc="Data & Utility Methods">
    private void loadNhanVienData() {
        int selectedIndex = tblNhanVien.getSelectionModel().getSelectedIndex();
        NhanVien selectedNVBeforeLoad = currentSelectedNhanVien;

        try {
            List<NhanVien> listFromDB = nhanVienDAO.layTatCaNhanVien();
            nhanVienList.setAll(listFromDB);

            if (selectedNVBeforeLoad != null) {
                for (int i = 0; i < nhanVienList.size(); i++) {
                    if (nhanVienList.get(i).getMaNV().equals(selectedNVBeforeLoad.getMaNV())) {
                        tblNhanVien.getSelectionModel().select(i);
                        break;
                    }
                }
             }
        } catch (Exception e) {
            showErrorAlert("Lỗi tải dữ liệu", e.getMessage());
            currentSelectedNhanVien = null;
        }

        currentSelectedNhanVien = tblNhanVien.getSelectionModel().getSelectedItem();

        if (currentSelectedNhanVien == null) {
            clearForm();
        }

        updateUIState(EditState.VIEWING);
    }


    private void showNhanVienDetails(NhanVien nhanVien) {
        if (nhanVien == null) {
             clearForm();
             return;
        }
        txtMaNV.setText(nhanVien.getMaNV());
        txtHoTen.setText(nhanVien.getHoTen());
        txtSDT.setText(nhanVien.getSdt());
        cbxChucVu.setValue(nhanVien.getChucVu());
        cbxTrangThai.setValue(nhanVien.getTrangThai());
        pfMatKhau.setText("");
        pfMatKhau.setPromptText("Để trống nếu không muốn đổi mật khẩu");

        // Hiển thị ca làm YÊU THÍCH
        cbxCaLam.setValue(nhanVien.getCaLamYeuThich());
        
        try {
            dpNgaySinh.setValue(LocalDate.parse(nhanVien.getNgaySinh(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        } catch (Exception e) {
            dpNgaySinh.setValue(null);
        }
    }

    private NhanVien createNhanVienFromForm() {
        NhanVien nv;
        if (currentState == EditState.VIEWING && currentSelectedNhanVien != null) {
             // Lấy nhân viên hiện tại để cập nhật
             nv = nhanVienDAO.getNhanVienTheoMa(currentSelectedNhanVien.getMaNV());
        }
        else {
             nv = new NhanVien();
             nv.setMaNV(txtMaNV.getText().trim());
        }

        nv.setHoTen(txtHoTen.getText().trim());
        nv.setSdt(txtSDT.getText().trim());
        nv.setChucVu(cbxChucVu.getValue());
        if (dpNgaySinh.getValue() != null) {
            nv.setNgaySinh(dpNgaySinh.getValue().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        } else {
             nv.setNgaySinh(null);
        }
        nv.setTrangThai(cbxTrangThai.getValue());
        
        // Lấy ca làm yêu thích từ ComboBox
        nv.setCaLamYeuThich(cbxCaLam.getValue());

        String matKhau = pfMatKhau.getText();
        if (!matKhau.isEmpty()) {
             nv.setMatKhau(matKhau);
        } else if (currentState == EditState.ADDING) {
            nv.setMatKhau(""); // Bắt buộc mật khẩu khi thêm mới (logic validate)
        } else if (currentSelectedNhanVien != null) {
            // Khi Sửa, nếu mật khẩu trống -> giữ mật khẩu cũ (lấy từ DAO)
             nv.setMatKhau(currentSelectedNhanVien.getMatKhau());
        }
        
        return nv;
    }

    private String generateNewMaNV() {
        String maCuoi = nhanVienDAO.layMaNVCuoiCung();
        if (maCuoi == null) return "NV001";
        try {
            int soMoi = Integer.parseInt(maCuoi.substring(2)) + 1;
            return String.format("NV%03d", soMoi);
        } catch (Exception e) { return "NV001"; }
    }

    private boolean validateInput() {
        if (txtHoTen.getText().trim().isEmpty()) {
            showErrorAlert("Lỗi", "Họ tên không được để trống.");
            txtHoTen.requestFocus();
            return false;
        }
        if (!txtSDT.getText().trim().matches("^0\\d{9}$")) {
            showErrorAlert("Lỗi", "Số điện thoại phải bắt đầu bằng 0 và có 10 chữ số.");
            txtSDT.requestFocus();
            return false;
        }
        if (cbxChucVu.getValue() == null) {
             showErrorAlert("Lỗi", "Vui lòng chọn chức vụ.");
             cbxChucVu.requestFocus();
             return false;
        }
        if (dpNgaySinh.getValue() == null) {
             showErrorAlert("Lỗi", "Vui lòng chọn ngày sinh.");
             dpNgaySinh.requestFocus();
             return false;
        }
        if (currentState == EditState.ADDING && pfMatKhau.getText().isEmpty()) {
            showErrorAlert("Lỗi", "Mật khẩu là bắt buộc khi thêm mới.");
            pfMatKhau.requestFocus();
            return false;
        }
        if (cbxTrangThai.getValue() == null) {
             showErrorAlert("Lỗi", "Vui lòng chọn trạng thái.");
             cbxTrangThai.requestFocus();
             return false;
        }
        // Không cần validate cbxCaLam (vì có thể để trống - không yêu thích)
        return true;
    }

    private void clearForm() {
        txtMaNV.setText("");
        txtHoTen.setText("");
        txtSDT.setText("");
        cbxChucVu.setValue(null);
        dpNgaySinh.setValue(null);
        cbxCaLam.setValue(null); // Xóa ca yêu thích
        cbxCaLam.setPromptText("Chọn ca yêu thích..."); // Đặt lại prompt
        pfMatKhau.setText("");
        pfMatKhau.setPromptText("");
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
    //</editor-fold>
}
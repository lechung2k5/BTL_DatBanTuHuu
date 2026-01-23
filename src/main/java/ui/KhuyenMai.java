package ui;

import dao.UuDaiDAO;
import entity.UuDai;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class KhuyenMai {

    @FXML private TableView<Promotion> tblKhuyenMai;
    @FXML private TableColumn<Promotion, String> colTenKM;
    @FXML private TableColumn<Promotion, String> colMaKM;
    @FXML private TableColumn<Promotion, String> colThoiGian;
    @FXML private TableColumn<Promotion, String> colTrangThai;
    @FXML private TableColumn<Promotion, String> colGiaTri;
    @FXML private TableColumn<Promotion, Void> colHanhDong;
    
    @FXML private TextField txtTenKM;
    @FXML private TextField txtGiaTri;
    @FXML private DatePicker datePickerStart;
    @FXML private DatePicker datePickerEnd;
    @FXML private ComboBox<String> filterComboBox;

    @FXML private Button btnThem, btnSua, btnLuu;
    
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private ObservableList<Promotion> promotionList = FXCollections.observableArrayList();
	private final UuDaiDAO uuDaiDAO = new UuDaiDAO();
    private Promotion selectedPromotion = null;
    private boolean isEditMode = false;
    private String currentMaKM = null;

    @FXML
    public void initialize() {
        setupTableColumns();
        loadDataFromDatabase();
        setupFilterComboBox();
        setupButtonEvents();
        setupInputValidation();
        setupTableSelectionEvent();
        resetFormState();
    }

    // ==================== SETUP CÁC THÀNH PHẦN ====================
    
    private void setupTableColumns() {
        tblKhuyenMai.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        colTenKM.setCellValueFactory(new PropertyValueFactory<>("name"));
        colMaKM.setCellValueFactory(new PropertyValueFactory<>("code"));
        colThoiGian.setCellValueFactory(new PropertyValueFactory<>("duration"));
        colTrangThai.setCellValueFactory(new PropertyValueFactory<>("status"));
        colGiaTri.setCellValueFactory(new PropertyValueFactory<>("value"));
        
        colHanhDong.setCellFactory(param -> new TableCell<Promotion, Void>() {
            private final Button deleteButton = new Button("Xóa");
            private final HBox pane = new HBox(deleteButton);
            
            {
                deleteButton.getStyleClass().add("delete-button");
                pane.setAlignment(Pos.CENTER);
                
                deleteButton.setOnAction(event -> {
                    Promotion promo = getTableRow().getItem();
                    if (promo != null) {
                        xoaKhuyenMai(promo);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
                
                if (deleteButton != null) {
                    deleteButton.setDisable(isEditMode);
                }
            }
        });
    }

    private void setupFilterComboBox() {
        filterComboBox.setItems(FXCollections.observableArrayList(
            "Tất cả", "Đang áp dụng", "Sắp diễn ra", "Đã hết hạn"
        ));
        filterComboBox.setValue("Tất cả");
        filterComboBox.setOnAction(e -> filterPromotions(filterComboBox.getValue()));
    }

    private void setupButtonEvents() {
        btnThem.setOnAction(e -> themKhuyenMai());
        btnSua.setOnAction(e -> chinhSuaKhuyenMai());
        btnLuu.setOnAction(e -> luuKhuyenMai());
    }

    private void setupInputValidation() {
        // RÀNG BUỘC: Tên khuyến mãi giới hạn độ dài
        txtTenKM.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.length() > 100) {
                txtTenKM.setText(oldVal);
            }
        });
        
        // RÀNG BUỘC: Giá trị giới hạn độ dài (cho phép nhập tự do, validate khi Lưu)
        txtGiaTri.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.length() > 10) {
                txtGiaTri.setText(oldVal);
            }
        });
    }

    private void setupTableSelectionEvent() {
        tblKhuyenMai.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !isEditMode) {
                selectedPromotion = newVal;
                loadPromotionToForm(newVal);
            }
        });
        
        tblKhuyenMai.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && selectedPromotion != null && !isEditMode) {
                chinhSuaKhuyenMai();
            }
        });
    }

    // ==================== XỬ LÝ DATABASE ====================
    
    private void loadDataFromDatabase() {
        try {
            List<UuDai> uuDaiList = uuDaiDAO.getAllUuDai();
            promotionList.clear();
            
            for (UuDai ud : uuDaiList) {
                Promotion promo = new Promotion(
                    ud.getTenUuDai(),
                    ud.getMaUuDai(),
                    ud.getNgayBatDau(),
                    ud.getNgayKetThuc(),
                    ud.getTrangThai(),
                    String.format("%.0f%%", ud.getGiaTri())
                );
                promotionList.add(promo);
            }
            
            tblKhuyenMai.setItems(promotionList);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải dữ liệu từ database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==================== XỬ LÝ CÁC NÚT ====================
    
    private void themKhuyenMai() {
        // RÀNG BUỘC: Chỉ quản lý mới có quyền thực hiện
        if (isEditMode) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Xác nhận");
            confirm.setHeaderText("Bạn đang trong chế độ chỉnh sửa");
            confirm.setContentText("Dữ liệu chưa lưu sẽ bị mất. Bạn có muốn tiếp tục?");
            
            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.OK) {
                return;
            }
        }
        
        clearForm();
        setFormEditable(true);
        isEditMode = true;
        selectedPromotion = null;
        
        // RÀNG BUỘC: Mã ưu đãi tự động phát sinh, định dạng {id}, NOT NULL
        currentMaKM = uuDaiDAO.taoMaUuDaiMoi();
        
        txtTenKM.requestFocus();
        updateButtonStates(true, true, false);
        updateTableState(true);
        
        showAlert(Alert.AlertType.INFORMATION, "Thêm mới khuyến mãi", 
                 "Mã khuyến mãi tự động: " + currentMaKM + "\nVui lòng nhập đầy đủ thông tin và nhấn Lưu.");
    }

    private void chinhSuaKhuyenMai() {
        // RÀNG BUỘC: Phải chọn voucher trước khi sửa
        if (selectedPromotion == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn", "Vui lòng chọn khuyến mãi cần sửa từ bảng.");
            return;
        }
        
        isEditMode = true;
        currentMaKM = selectedPromotion.getCode();
        setFormEditable(true);
        
        updateButtonStates(true, true, false);
        updateTableState(true);
        
        txtTenKM.requestFocus();
        txtTenKM.selectAll();
    }

    private void luuKhuyenMai() {
        // RÀNG BUỘC: Kiểm tra tất cả ràng buộc trước khi lưu
        if (!validateAllInput()) {
            return;
        }
        
        String tenKM = txtTenKM.getText().trim();
        double giaTri = Double.parseDouble(txtGiaTri.getText().trim());
        LocalDate ngayBatDau = datePickerStart.getValue();
        LocalDate ngayKetThuc = datePickerEnd.getValue();
        
        // RÀNG BUỘC: Kiểm tra trùng mã voucher (đã xử lý trong DAO)
        UuDai uuDai = new UuDai();
        uuDai.setMaUuDai(currentMaKM);
        uuDai.setTenUuDai(tenKM);
        uuDai.setMoTa(tenKM);
        uuDai.setGiaTri(giaTri);
        uuDai.setNgayBatDau(ngayBatDau);
        uuDai.setNgayKetThuc(ngayKetThuc);
        
        boolean success;
        if (selectedPromotion == null) {
            success = uuDaiDAO.themUuDai(uuDai);
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Thêm thành công", 
                         "Đã thêm khuyến mãi mới: " + tenKM);
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi thêm", "Không thể thêm khuyến mãi vào database.");
                return;
            }
        } else {
            success = uuDaiDAO.capNhatUuDai(uuDai);
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Cập nhật thành công", 
                         "Đã cập nhật khuyến mãi: " + tenKM);
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi cập nhật", "Không thể cập nhật khuyến mãi trong database.");
                return;
            }
        }
        
        // RÀNG BUỘC: Ghi log lịch sử thao tác (đã xử lý trong DAO)
        loadDataFromDatabase();
        resetFormState();
    }

    private void xoaKhuyenMai(Promotion promo) {
        // RÀNG BUỘC: Chỉ quản lý mới có quyền xóa
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Xác nhận xóa");
        confirmDialog.setHeaderText("Bạn chắc chắn muốn xóa khuyến mãi này?");
        confirmDialog.setContentText("Khuyến mãi: " + promo.getName() + "\nMã: " + promo.getCode() + 
                                     "\n\nHành động này không thể hoàn tác.");
        
        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                if (uuDaiDAO.xoaUuDai(promo.getCode())) {
                    showAlert(Alert.AlertType.INFORMATION, "Xóa thành công", 
                             "Đã xóa khuyến mãi \"" + promo.getName() + "\"");
                    
                    if (selectedPromotion != null && 
                        selectedPromotion.getCode().equals(promo.getCode())) {
                        selectedPromotion = null;
                    }
                    
                    loadDataFromDatabase();
                    resetFormState();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Lỗi xóa", "Không thể xóa khuyến mãi từ database.");
                }
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Lỗi", 
                         "Không thể xóa khuyến mãi: " + e.getMessage() + 
                         "\n\nKhuyến mãi có thể đang được sử dụng trong hóa đơn hoặc bị ràng buộc dữ liệu.");
                e.printStackTrace();
            }
        }
    }

    // ==================== XỬ LÝ LỌC ====================
    
    private void filterPromotions(String filter) {
        if (filter.equals("Tất cả")) {
            loadDataFromDatabase();
        } else {
            List<UuDai> filteredList = uuDaiDAO.locUuDaiTheoTrangThai(filter);
            promotionList.clear();
            
            for (UuDai ud : filteredList) {
                Promotion promo = new Promotion(
                    ud.getTenUuDai(),
                    ud.getMaUuDai(),
                    ud.getNgayBatDau(),
                    ud.getNgayKetThuc(),
                    ud.getTrangThai(),
                    String.format("%.0f%%", ud.getGiaTri())
                );
                promotionList.add(promo);
            }
            
            tblKhuyenMai.setItems(promotionList);
        }
    }

    // ==================== XỬ LÝ FORM ====================
    
    private void loadPromotionToForm(Promotion promo) {
        if (promo == null) return;
        
        UuDai uuDai = uuDaiDAO.timUuDaiTheoMa(promo.getCode());
        if (uuDai != null) {
            txtTenKM.setText(uuDai.getTenUuDai());
            txtGiaTri.setText(String.valueOf(uuDai.getGiaTri()));
            datePickerStart.setValue(uuDai.getNgayBatDau());
            datePickerEnd.setValue(uuDai.getNgayKetThuc());
        }
    }

    private void clearForm() {
        txtTenKM.clear();
        txtGiaTri.clear();
        datePickerStart.setValue(null);
        datePickerEnd.setValue(null);
        datePickerStart.getEditor().clear();
        datePickerEnd.getEditor().clear();
    }

    private void resetFormState() {
        clearForm();
        setFormEditable(false);
        selectedPromotion = null;
        currentMaKM = null;
        isEditMode = false;
        
        updateButtonStates(false, false, true);
        updateTableState(false);
        tblKhuyenMai.getSelectionModel().clearSelection();
    }

    private void setFormEditable(boolean editable) {
        txtTenKM.setEditable(editable);
        txtGiaTri.setEditable(editable);
        datePickerStart.setDisable(!editable);
        datePickerEnd.setDisable(!editable);
    }

    private void updateButtonStates(boolean disableThem, boolean disableSua, boolean disableLuu) {
        btnThem.setDisable(disableThem);
        btnSua.setDisable(disableSua);
        btnLuu.setDisable(disableLuu);
    }
    
    private void updateTableState(boolean disable) {
        tblKhuyenMai.setDisable(disable);
        filterComboBox.setDisable(disable);
        tblKhuyenMai.refresh();
    }

    // ==================== VALIDATION - RÀNG BUỘC ====================
    
    private boolean validateAllInput() {
        return validateMaUuDai() && 
               validateTenKhuyenMai() && 
               validateGiaTri() && 
               validateNgayBatDau() &&
               validateNgayKetThuc() &&
               validateDateRange();
    }

    // RÀNG BUỘC 1: Mã ưu đãi (maUuDai)
    // - Không được null
    // - Không được rỗng ("")
    // - Định dạng: {id}, NOT NULL
    private boolean validateMaUuDai() {
        if (currentMaKM == null || currentMaKM.trim().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Lỗi hệ thống", "Mã ưu đãi không được tạo. Vui lòng thử lại.");
            return false;
        }
        return true;
    }

    // RÀNG BUỘC 2: Tên ưu đãi (tenUuDai)
    // - Không được null
    // - Không được rỗng ("")
    // - Bắt buộc nhập
    private boolean validateTenKhuyenMai() {
        String tenKM = txtTenKM.getText().trim();
        
        if (tenKM.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", 
                     "Tên khuyến mãi không được để trống.\nVui lòng nhập tên khuyến mãi.");
            txtTenKM.requestFocus();
            return false;
        }
        
        if (tenKM.length() < 5) {
            showAlert(Alert.AlertType.WARNING, "Tên quá ngắn", 
                     "Tên khuyến mãi phải có ít nhất 5 ký tự để đảm bảo đầy đủ thông tin.");
            txtTenKM.requestFocus();
            return false;
        }
        
        return true;
    }

    // RÀNG BUỘC 3: Giá trị ưu đãi (giaTri)
    // - Phải >= 0
    // - Kiểu dữ liệu: double
    // - Có thể là % hoặc số tiền cụ thể
    // - Giá trị từ 0 đến 100 (%)
    private boolean validateGiaTri() {
        String giaTriStr = txtGiaTri.getText().trim();
        
        if (giaTriStr.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", 
                     "Giá trị khuyến mãi không được để trống.\nVui lòng nhập giá trị khuyến mãi (%).");
            txtGiaTri.requestFocus();
            return false;
        }
        
        try {
            double giaTri = Double.parseDouble(giaTriStr);
            
            // RÀNG BUỘC: giaTri >= 0
            if (giaTri < 0) {
                showAlert(Alert.AlertType.WARNING, "Giá trị không hợp lệ", 
                         "Giá trị khuyến mãi không được âm.\nVui lòng nhập giá trị từ 0 đến 100.");
                txtGiaTri.requestFocus();
                return false;
            }
            
            // RÀNG BUỘC: giaTri <= 100 (%)
            if (giaTri > 100) {
                showAlert(Alert.AlertType.WARNING, "Giá trị không hợp lệ", 
                         "Giá trị khuyến mãi không được vượt quá 100%.\nVui lòng nhập giá trị từ 0 đến 100.");
                txtGiaTri.requestFocus();
                return false;
            }
            
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Giá trị không hợp lệ", 
                     "Giá trị khuyến mãi phải là số.\nVui lòng nhập số hợp lệ (ví dụ: 10, 15.5).");
            txtGiaTri.requestFocus();
            return false;
        }
        
        return true;
    }

    // RÀNG BUỘC 4: Ngày bắt đầu (ngayBatDau)
    // - NOT NULL
    // - Kiểu dữ liệu: Date
    private boolean validateNgayBatDau() {
        if (datePickerStart.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Thiếu ngày bắt đầu", 
                     "Ngày bắt đầu không được để trống.\nVui lòng chọn ngày bắt đầu áp dụng khuyến mãi.");
            datePickerStart.requestFocus();
            return false;
        }
        return true;
    }

    // RÀNG BUỘC 5: Ngày kết thúc (ngayKetThuc)
    // - NOT NULL
    // - Phải >= ngayBatDau
    // - Kiểu dữ liệu: Date
    private boolean validateNgayKetThuc() {
        if (datePickerEnd.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Thiếu ngày kết thúc", 
                     "Ngày kết thúc không được để trống.\nVui lòng chọn ngày kết thúc áp dụng khuyến mãi.");
            datePickerEnd.requestFocus();
            return false;
        }
        return true;
    }

    // RÀNG BUỘC 6: Khoảng thời gian
    // - ngayKetThuc >= ngayBatDau
    private boolean validateDateRange() {
        LocalDate ngayBatDau = datePickerStart.getValue();
        LocalDate ngayKetThuc = datePickerEnd.getValue();
        
        if (ngayBatDau != null && ngayKetThuc != null) {
            if (ngayKetThuc.isBefore(ngayBatDau)) {
                showAlert(Alert.AlertType.WARNING, "Ngày không hợp lệ", 
                         "Ngày kết thúc phải sau hoặc bằng ngày bắt đầu.\n" +
                         "Ngày bắt đầu: " + ngayBatDau.format(formatter) + "\n" +
                         "Ngày kết thúc: " + ngayKetThuc.format(formatter));
                datePickerEnd.requestFocus();
                return false;
            }
        }
        
        return true;
    }

    // ==================== TIỆN ÍCH ====================
    
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    // ==================== INNER CLASS ====================
    
    public static class Promotion {
        private final SimpleStringProperty name;
        private final SimpleStringProperty code;
        private final SimpleStringProperty duration;
        private final SimpleStringProperty status;
        private final SimpleStringProperty value;

        public Promotion(String name, String code, LocalDate startDate, LocalDate endDate, String status, String value) {
            this.name = new SimpleStringProperty(name);
            this.code = new SimpleStringProperty(code);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            this.duration = new SimpleStringProperty(startDate.format(formatter) + " - " + endDate.format(formatter));
            this.status = new SimpleStringProperty(status);
            this.value = new SimpleStringProperty(value);
        }

        public String getName() { return name.get(); }
        public String getCode() { return code.get(); }
        public String getDuration() { return duration.get(); }
        public String getStatus() { return status.get(); }
        public String getValue() { return value.get(); }
    }
}
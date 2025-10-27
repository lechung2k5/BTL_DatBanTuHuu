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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    @FXML private TextField txtTimKiem;
    @FXML private DatePicker datePickerStart;
    @FXML private DatePicker datePickerEnd;
    @FXML private ComboBox<String> filterComboBox;

    @FXML private Button btnThem, btnSua, btnLuu, btnXoa, btnTimKiem;
    
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private ObservableList<Promotion> promotionList = FXCollections.observableArrayList();
    private ObservableList<Promotion> allPromotions = FXCollections.observableArrayList();
    private final UuDaiDAO uuDaiDAO = new UuDaiDAO();
    private Promotion selectedPromotion = null;
    private boolean isEditMode = false;
    private String currentMaKM = null;

    // Định nghĩa phím tắt
    private final KeyCombination keyCtrlN = new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN);
    private final KeyCombination keyCtrlE = new KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN);
    private final KeyCombination keyCtrlD = new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN);
    private final KeyCombination keyCtrlF = new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN);
    private final KeyCombination keyCtrlS = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN);
    private final KeyCombination keyAltA = new KeyCodeCombination(KeyCode.A, KeyCombination.ALT_DOWN);
    private final KeyCombination keyAltE = new KeyCodeCombination(KeyCode.E, KeyCombination.ALT_DOWN);

    @FXML
    public void initialize() {
        setupTableColumns();
        loadDataFromDatabase();
        setupFilterComboBox();
        setupButtonEvents();
        setupInputValidation();
        setupTableSelectionEvent();
        
        // Đợi scene được gán trước khi setup phím tắt
        tblKhuyenMai.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                setupKeyboardShortcuts();
            }
        });
        
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
        
        if (btnXoa != null) {
            btnXoa.setOnAction(e -> xoaKhuyenMaiDaChon());
        }
        
        if (btnTimKiem != null) {
            btnTimKiem.setOnAction(e -> timKiemKhuyenMai());
        }
        
        if (txtTimKiem != null) {
            txtTimKiem.setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.ENTER) {
                    timKiemKhuyenMai();
                }
            });
        }
    }

    private void setupInputValidation() {
        txtTenKM.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.length() > 100) {
                txtTenKM.setText(oldVal);
            }
        });
        txtGiaTri.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.matches("\\d*(\\.\\d*)?")) {
                txtGiaTri.setText(oldVal);
            }
        });
    }

    private void setupTableSelectionEvent() {
        tblKhuyenMai.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !isEditMode) {
                selectedPromotion = newVal;
                loadPromotionToForm(newVal);
                if (btnXoa != null) {
                    btnXoa.setDisable(false);
                }
            } else {
                if (btnXoa != null && !isEditMode) {
                    btnXoa.setDisable(true);
                }
            }
        });
        
        tblKhuyenMai.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && selectedPromotion != null && !isEditMode) {
                chinhSuaKhuyenMai();
            }
        });
    }

    private void setupKeyboardShortcuts() {
        tblKhuyenMai.getScene().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            // Ctrl + N: Thêm voucher mới
            if (keyCtrlN.match(event)) {
                event.consume();
                themKhuyenMai();
            }
            // Ctrl + E: Sửa voucher
            else if (keyCtrlE.match(event)) {
                event.consume();
                chinhSuaKhuyenMai();
            }
            // Ctrl + D: Xóa voucher
            else if (keyCtrlD.match(event)) {
                event.consume();
                xoaKhuyenMaiDaChon();
            }
            // Ctrl + F: Tìm voucher
            else if (keyCtrlF.match(event)) {
                event.consume();
                focusTimKiem();
            }
            // Ctrl + S: Lưu
            else if (keyCtrlS.match(event)) {
                event.consume();
                if (isEditMode) {
                    luuKhuyenMai();
                }
            }
            // Alt + A: Hiển thị voucher đang áp dụng
            else if (keyAltA.match(event)) {
                event.consume();
                filterComboBox.setValue("Đang áp dụng");
                filterPromotions("Đang áp dụng");
            }
            // Alt + E: Hiển thị voucher hết hạn
            else if (keyAltE.match(event)) {
                event.consume();
                filterComboBox.setValue("Đã hết hạn");
                filterPromotions("Đã hết hạn");
            }
        });
    }

    // ==================== XỬ LÝ DATABASE ====================
    
    private void loadDataFromDatabase() {
        try {
            List<UuDai> uuDaiList = uuDaiDAO.getAllUuDai();
            promotionList.clear();
            allPromotions.clear();
            
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
                allPromotions.add(promo);
            }
            
            tblKhuyenMai.setItems(promotionList);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tải dữ liệu từ database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==================== XỬ LÝ CÁC NÚT ====================
    
    private void themKhuyenMai() {
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
        currentMaKM = uuDaiDAO.taoMaUuDaiMoi();
        
        txtTenKM.requestFocus();
        updateButtonStates(true, true, false);
        updateTableState(true);
        
        showAlert(Alert.AlertType.INFORMATION, "Thêm mới khuyến mãi", 
                 "Mã khuyến mãi tự động: " + currentMaKM + "\nVui lòng nhập đầy đủ thông tin và nhấn Lưu (Ctrl+S).");
    }

    private void chinhSuaKhuyenMai() {
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
        if (!validateAllInput()) {
            return;
        }
        
        String tenKM = txtTenKM.getText().trim();
        double giaTri = Double.parseDouble(txtGiaTri.getText().trim());
        LocalDate ngayBatDau = datePickerStart.getValue();
        LocalDate ngayKetThuc = datePickerEnd.getValue();
        
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
        
        loadDataFromDatabase();
        resetFormState();
    }

    private void xoaKhuyenMaiDaChon() {
        if (isEditMode) {
            showAlert(Alert.AlertType.WARNING, "Không thể xóa", 
                     "Vui lòng thoát chế độ chỉnh sửa trước khi xóa.");
            return;
        }
        
        if (selectedPromotion == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn", "Vui lòng chọn khuyến mãi cần xóa từ bảng.");
            return;
        }
        
        xoaKhuyenMai(selectedPromotion);
    }

    private void xoaKhuyenMai(Promotion promo) {
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

    // ==================== XỬ LÝ TÌM KIẾM ====================
    
    private void focusTimKiem() {
        if (txtTimKiem != null) {
            txtTimKiem.requestFocus();
            txtTimKiem.selectAll();
        }
    }

    private void timKiemKhuyenMai() {
        if (txtTimKiem == null) {
            return;
        }
        
        String keyword = txtTimKiem.getText().trim().toLowerCase();
        
        if (keyword.isEmpty()) {
            tblKhuyenMai.setItems(allPromotions);
            return;
        }
        
        ObservableList<Promotion> searchResults = allPromotions.stream()
            .filter(p -> 
                p.getName().toLowerCase().contains(keyword) ||
                p.getCode().toLowerCase().contains(keyword) ||
                p.getStatus().toLowerCase().contains(keyword)
            )
            .collect(Collectors.toCollection(FXCollections::observableArrayList));
        
        tblKhuyenMai.setItems(searchResults);
        
        if (searchResults.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Tìm kiếm", 
                     "Không tìm thấy khuyến mãi nào phù hợp với từ khóa: " + keyword);
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
        
        if (txtTimKiem != null) {
            txtTimKiem.clear();
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
        
        if (btnXoa != null) {
            btnXoa.setDisable(isEditMode || selectedPromotion == null);
        }
    }
    
    private void updateTableState(boolean disable) {
        tblKhuyenMai.setDisable(disable);
        filterComboBox.setDisable(disable);
        
        if (txtTimKiem != null) {
            txtTimKiem.setDisable(disable);
        }
        if (btnTimKiem != null) {
            btnTimKiem.setDisable(disable);
        }
        
        tblKhuyenMai.refresh();
    }

    // ==================== VALIDATION ====================
    
    private boolean validateAllInput() {
        return validateTenKhuyenMai() && validateGiaTri() && validateDateRange();
    }

    private boolean validateTenKhuyenMai() {
        String tenKM = txtTenKM.getText().trim();
        
        if (tenKM.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng nhập tên khuyến mãi.");
            txtTenKM.requestFocus();
            return false;
        }
        
        if (tenKM.length() < 5) {
            showAlert(Alert.AlertType.WARNING, "Tên quá ngắn", "Tên khuyến mãi phải có ít nhất 5 ký tự.");
            txtTenKM.requestFocus();
            return false;
        }
        
        return true;
    }

    private boolean validateGiaTri() {
        String giaTriStr = txtGiaTri.getText().trim();
        
        if (giaTriStr.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng nhập giá trị khuyến mãi.");
            txtGiaTri.requestFocus();
            return false;
        }
        
        try {
            double giaTri = Double.parseDouble(giaTriStr);
            if (giaTri < 0 || giaTri > 100) {
                showAlert(Alert.AlertType.WARNING, "Giá trị không hợp lệ", "Giá trị phải từ 0 đến 100.");
                txtGiaTri.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.WARNING, "Giá trị không hợp lệ", "Vui lòng nhập số hợp lệ.");
            txtGiaTri.requestFocus();
            return false;
        }
        
        return true;
    }

    private boolean validateDateRange() {
        if (datePickerStart.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Thiếu ngày bắt đầu", "Vui lòng chọn ngày bắt đầu.");
            datePickerStart.requestFocus();
            return false;
        }
        
        if (datePickerEnd.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "Thiếu ngày kết thúc", "Vui lòng chọn ngày kết thúc.");
            datePickerEnd.requestFocus();
            return false;
        }
        
        if (datePickerEnd.getValue().isBefore(datePickerStart.getValue())) {
            showAlert(Alert.AlertType.WARNING, "Ngày không hợp lệ", 
                     "Ngày kết thúc phải sau hoặc bằng ngày bắt đầu.");
            datePickerEnd.requestFocus();
            return false;
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
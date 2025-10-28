package ui;

import dao.DanhMucMonDAO;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

import dao.MonAnDAO;
import entity.MonAn; 

import javafx.scene.layout.AnchorPane;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser; 

import java.io.ByteArrayInputStream; 
import java.io.File; 
import java.io.IOException; 
import java.nio.file.Files; 
import java.util.ArrayList;

public class ThucDon {

    @FXML private TableView<MenuItem> tableThucDon;
    @FXML private TableColumn<MenuItem, String> colTenMon;
    @FXML private TableColumn<MenuItem, Image> colHinhAnh;
    @FXML private TableColumn<MenuItem, Number> colDonGia;

    @FXML private TextField txtTenMon, txtDonGia, txtSearch;
    @FXML private Label uploadLabel;
    @FXML private ImageView previewImg;
    
    @FXML private TextField txtDanhMuc; 

    @FXML private Button btnThem, btnXoa, btnSua, btnXoaTrang;
    @FXML private Button btnLuuForm; 
    @FXML private Button btnTim; // Nút này giờ sẽ có chức năng

    @FXML private HBox categoryButtonBox;
    @FXML private Button btnKhaiVi, btnNuong, btnLau, btnXaoHap, btnChien, btnDacSan, btnDoUong;

    @FXML private AnchorPane formInputArea;

    private MonAnDAO monAnDAO;
    private DanhMucMonDAO danhMucMonDAO;
    private ObservableList<MenuItem> dsMonAnUI;
    private File selectedImageFile; 

    public void initialize() {
        monAnDAO = new MonAnDAO();
        danhMucMonDAO = new DanhMucMonDAO();
        dsMonAnUI = FXCollections.observableArrayList();
        tableThucDon.setItems(dsMonAnUI);

        setupTableColumns();
        addCategoryButtonListeners();
        
        clearForm(); 
        formInputArea.setVisible(false);
        btnLuuForm.setText("Lưu");

        // Tải danh mục đầu tiên (Khai vị) làm mặc định
        if (btnKhaiVi != null) {
            btnKhaiVi.getStyleClass().add("active");
            filterTableData("DM001"); 
        } else {
            loadDataFromDatabase(); 
        }

        // === LOGIC CÁC NÚT ===
        uploadLabel.setOnMouseClicked(e -> onUploadClicked());
        btnThem.setOnAction(e -> {
            if (formInputArea.isVisible()) {
                formInputArea.setVisible(false);
            } else {
                clearForm(); 
                for (javafx.scene.Node node : categoryButtonBox.getChildren()) {
                    if (node.getStyleClass().contains("active") && node instanceof Button) {
                        txtDanhMuc.setText(((Button) node).getText());
                        break;
                    }
                }
                formInputArea.setVisible(true);
            }
        });
        btnXoa.setOnAction(e -> handleXoaMonAn());
        btnSua.setOnAction(e -> {
            if (tableThucDon.getSelectionModel().getSelectedItem() != null) {
                formInputArea.setVisible(true); 
            } else {
                showAlert(Alert.AlertType.WARNING, "Chưa chọn", "Vui lòng chọn một món ăn để sửa.");
            }
        });
        btnXoaTrang.setOnAction(e -> clearForm()); 
        btnLuuForm.setOnAction(e -> handleLuuForm());
        
        // ===================================
        // === LOGIC MỚI: TÌM KIẾM BẰNG NÚT ===
        // ===================================
        btnTim.setOnAction(e -> {
            handleSearch(txtSearch.getText());
        });
        
        // === ĐÃ XÓA LISTENER TÌM KIẾM LIVE ===
        // txtSearch.textProperty().addListener(...) đã bị xóa
        // ===================================

        // Listener khi chọn 1 hàng: Chỉ điền data
     // ==================================================
     // === PHÍM TẮT CRUD TRONG TRANG THỰC ĐƠN ==========
     // ==================================================
     txtSearch.sceneProperty().addListener((obsScene, oldScene, newScene) -> {
         if (newScene != null) {

             // Ctrl + F -> focus tìm kiếm
             newScene.getAccelerators().put(
                 new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN),
                 () -> {
                     txtSearch.requestFocus();
                     txtSearch.selectAll();
                 }
             );

             // Ctrl + N -> Thêm món
             newScene.getAccelerators().put(
                 new KeyCodeCombination(KeyCode.N, KeyCombination.CONTROL_DOWN),
                 () -> btnThem.fire()
             );

             // Ctrl + S -> Lưu / Sửa món
             newScene.getAccelerators().put(
                 new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN),
                 () -> btnLuuForm.fire()
             );

             // Ctrl + D -> Xóa món
             newScene.getAccelerators().put(
                 new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN),
                 () -> btnXoa.fire()
             );

             // ESC -> Xóa trắng form
             newScene.getAccelerators().put(
                 new KeyCodeCombination(KeyCode.ESCAPE),
                 () -> btnXoaTrang.fire()
             );

             // Ctrl + → -> Chọn món tiếp theo
             newScene.getAccelerators().put(
                 new KeyCodeCombination(KeyCode.RIGHT, KeyCombination.CONTROL_DOWN),
                 () -> {
                     int i = tableThucDon.getSelectionModel().getSelectedIndex();
                     if (i < tableThucDon.getItems().size() - 1) {
                         tableThucDon.getSelectionModel().select(i + 1);
                     }
                 }
             );

             // Ctrl + ← -> Món phía trên
             newScene.getAccelerators().put(
                 new KeyCodeCombination(KeyCode.LEFT, KeyCombination.CONTROL_DOWN),
                 () -> {
                     int i = tableThucDon.getSelectionModel().getSelectedIndex();
                     if (i > 0) {
                         tableThucDon.getSelectionModel().select(i - 1);
                     }
                 }
             );

             // Enter trong bảng -> load dữ liệu sang form
             tableThucDon.setOnKeyPressed(e -> {
                 if (e.getCode() == KeyCode.ENTER) {
                     MenuItem item = tableThucDon.getSelectionModel().getSelectedItem();
                     if (item != null) {
                         txtTenMon.setText(item.getName());
                         txtDonGia.setText(String.valueOf(item.getPrice()));
                         previewImg.setImage(item.getImage());
                         txtDanhMuc.setText(item.getCategory());
                         formInputArea.setVisible(true);
                     }
                 }
             });
         }
     });

        tableThucDon.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                txtTenMon.setText(newSelection.getName());
                txtDonGia.setText(String.valueOf(newSelection.getPrice())); 
                previewImg.setImage(newSelection.getImage());
                txtDanhMuc.setText(newSelection.getCategory());
                selectedImageFile = null; 
            }
        });
    }

    /**
     * HÀM MỚI: Xử lý logic tìm kiếm
     */
    private void handleSearch(String searchTerm) {
        String term = searchTerm.trim();
        if (term.isEmpty()) {
            // Nếu ô tìm kiếm rỗng, tải lại danh mục đang active
            String activeMaDM = getActiveCategoryMaDM();
            if (activeMaDM != null) {
                filterTableData(activeMaDM);
            } else {
                // Nếu không có gì active, tải "Khai vị" (DM001) làm mặc định
                filterTableData("DM001");
                if (btnKhaiVi != null) btnKhaiVi.getStyleClass().add("active");
            }
        } else {
            // Nếu có từ khóa, gọi DAO để tìm kiếm
            ArrayList<MonAn> searchResults = monAnDAO.searchMonAnByName(term);
            populateTable(searchResults);
            
            // Khi tìm kiếm, bỏ "active" tất cả các nút danh mục
            deactivateAllCategoryButtons();
        }
    }
    
    // (Hàm loadDataFromDatabase, filterTableData giữ nguyên)
    private void loadDataFromDatabase() {
        ArrayList<MonAn> listFromDB = monAnDAO.getAllMonAn();
        populateTable(listFromDB);
    }
    private void filterTableData(String maDM) {
        ArrayList<MonAn> listFromDB = monAnDAO.getMonAnByDanhMuc(maDM);
        populateTable(listFromDB);
    }
    
    /**
     * CẬP NHẬT: populateTable để hiển thị thông báo tìm kiếm
     */
    private void populateTable(ArrayList<MonAn> listFromDB) {
        dsMonAnUI.clear();
        for (MonAn monAn : listFromDB) {
            Image fxImage = null;
            byte[] imgBytes = monAn.getHinhAnh();
            if (imgBytes != null && imgBytes.length > 0) {
                fxImage = new Image(new ByteArrayInputStream(imgBytes));
            }
            MenuItem itemUI = new MenuItem(
                    monAn.getMaMon(), monAn.getTenMon(), fxImage, 
                    monAn.getGiaBan(), monAn.getTenDanhMuc(), monAn.getMaDM()
            );
            dsMonAnUI.add(itemUI);
        }
        
        // Cập nhật thông báo
        if (txtSearch.getText().trim().isEmpty()) {
            System.out.println("✅ Đã tải " + dsMonAnUI.size() + " món ăn (theo danh mục).");
        } else {
             System.out.println("✅ Tìm thấy " + dsMonAnUI.size() + " kết quả cho '" + txtSearch.getText() + "'.");
        }
    }
    
    // (Hàm handleLuuForm, handleXoaMonAn, onUploadClicked, clearForm, showAlert, setupTableColumns giữ nguyên)
    
    private void handleLuuForm() {
        String tenMon = txtTenMon.getText().trim();
        String donGiaStr = txtDonGia.getText().replace(",", "").trim();
        String tenDM = txtDanhMuc.getText().trim();
        if (tenMon.isEmpty() || donGiaStr.isEmpty() || tenDM.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Tên món, đơn giá và danh mục không được rỗng.");
            return;
        }
        double donGia;
        try {
            donGia = Double.parseDouble(donGiaStr);
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Đơn giá phải là một con số.");
            return;
        }
        String maDM = danhMucMonDAO.getMaDMByTenDM(tenDM);
        if (maDM == null) {
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Tên danh mục không hợp lệ: '" + tenDM + "'.\n" +
                    "Các danh mục hợp lệ là: Khai vị, Nướng, Lẩu, Xào/Hấp, Chiên, Đặc sản, Đồ uống.");
            return;
        }
        byte[] hinhAnhBytes = null;
        if (selectedImageFile != null) {
            try {
                hinhAnhBytes = Files.readAllBytes(selectedImageFile.toPath());
            } catch (IOException e) {
                System.err.println("❌ Lỗi khi đọc file ảnh: " + e.getMessage());
                e.printStackTrace();
            }
        }
        MenuItem selectedItem = tableThucDon.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            String newMaMon = monAnDAO.getNextMaMon();
            MonAn newMon = new MonAn(newMaMon, tenMon, hinhAnhBytes, donGia, maDM);
            if (monAnDAO.addMonAn(newMon)) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã thêm món mới: " + tenMon);
                filterTableData(maDM); 
                updateActiveCategoryButton(tenDM);
                clearForm();
                formInputArea.setVisible(false);
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Thêm món ăn thất bại!");
            }
        } else {
            String maMon = selectedItem.getMaMon();
            MonAn updatedMon = new MonAn(maMon, tenMon, hinhAnhBytes, donGia, maDM);
            if (monAnDAO.updateMonAn(updatedMon)) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã cập nhật món: " + tenMon);
                filterTableData(maDM); 
                updateActiveCategoryButton(tenDM);
                clearForm();
                formInputArea.setVisible(false);
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Cập nhật món ăn thất bại!");
            }
        }
    }
    private void handleXoaMonAn() {
        MenuItem selectedItem = tableThucDon.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn", "Vui lòng chọn món ăn để xóa.");
            return;
        }
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Xác nhận xóa");
        confirmAlert.setHeaderText("Bạn có chắc muốn xóa món: " + selectedItem.getName() + "?");
        confirmAlert.setContentText("Hành động này không thể hoàn tác.");
        if (confirmAlert.showAndWait().get() == ButtonType.OK) {
            if (monAnDAO.deleteMonAn(selectedItem.getMaMon())) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã xóa món ăn.");
                filterTableData(selectedItem.getMaDM()); 
                clearForm();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Xóa thất bại! (Món ăn có thể đã có trong Hóa đơn).");
            }
        }
    }
    private void onUploadClicked() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn ảnh món ăn");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        selectedImageFile = fileChooser.showOpenDialog(uploadLabel.getScene().getWindow());
        if (selectedImageFile != null) {
            try {
                Image image = new Image(selectedImageFile.toURI().toString());
                previewImg.setImage(image);
            } catch (Exception e) {
                System.err.println("❌ Lỗi khi tải ảnh preview: " + e.getMessage());
                selectedImageFile = null;
            }
        }
    }
    private void clearForm() {
        txtTenMon.clear();
        txtDonGia.clear();
        previewImg.setImage(null);
        txtDanhMuc.clear(); 
        tableThucDon.getSelectionModel().clearSelection(); 
        selectedImageFile = null; 
    }
    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    private void setupTableColumns() {
        tableThucDon.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        colTenMon.setCellValueFactory(new PropertyValueFactory<>("name"));
        colTenMon.setCellFactory(column -> new TableCell<MenuItem, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                getStyleClass().add("name-cell"); 
            }
        });
        colHinhAnh.setCellValueFactory(new PropertyValueFactory<>("image"));
        colHinhAnh.setCellFactory(column -> new TableCell<MenuItem, Image>() {
            private final ImageView imageView = new ImageView();
            {
                imageView.setFitHeight(50); 
                imageView.setFitWidth(80);
                imageView.setPreserveRatio(false); 
                getStyleClass().add("image-cell");
            }
            @Override
            protected void updateItem(Image item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText("No Image"); 
                } else {
                    imageView.setImage(item);
                    setGraphic(imageView);
                    setText(null);
                }
            }
        });
        colDonGia.setCellValueFactory(new PropertyValueFactory<>("price"));
        colDonGia.setCellFactory(column -> new TableCell<MenuItem, Number>() {
            {
                getStyleClass().add("price-cell"); 
            }
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%,.0f Đ", item.doubleValue()));
                }
            }
        });
    }

    // (Hàm addCategoryButtonListeners giữ nguyên)
    private void addCategoryButtonListeners() {
        for (javafx.scene.Node node : categoryButtonBox.getChildren()) {
            if (node instanceof Button) {
                node.setOnMouseClicked(this::onCategoryClicked);
            }
        }
    }

    /**
     * CẬP NHẬT: onCategoryClicked để xóa ô tìm kiếm
     */
    private void onCategoryClicked(MouseEvent evt) {
        Button clickedButton = (Button) evt.getSource();
        String tenDM = clickedButton.getText();
        String maDM = danhMucMonDAO.getMaDMByTenDM(tenDM);
        
        if (maDM != null) {
            setActiveButton(clickedButton); 
            filterTableData(maDM); 
            
            txtDanhMuc.setText(tenDM); 
            formInputArea.setVisible(false); 
            clearForm();
            
            // === THÊM DÒNG NÀY ===
            // Xóa nội dung tìm kiếm khi chọn danh mục
            txtSearch.clear(); 
            // ======================
        } else {
            System.err.println("❌ Không tìm thấy mã DM cho: " + tenDM);
        }
    }
    
    // (Hàm setActiveButton, updateActiveCategoryButton giữ nguyên)
    private void setActiveButton(Button activeButton) {
        deactivateAllCategoryButtons(); // Gọi hàm hỗ trợ mới
        activeButton.getStyleClass().add("active");
    }
    private void updateActiveCategoryButton(String tenDM) {
         for (javafx.scene.Node node : categoryButtonBox.getChildren()) {
            if (node instanceof Button && ((Button) node).getText().equals(tenDM)) {
                setActiveButton((Button) node);
                break;
            }
        }
    }

    /**
     * HÀM MỚI: Bỏ "active" tất cả các nút danh mục
     */
    private void deactivateAllCategoryButtons() {
        for (javafx.scene.Node node : categoryButtonBox.getChildren()) {
            node.getStyleClass().remove("active");
        }
    }
    
    /**
     * HÀM MỚI: Lấy mã DM của nút đang "active"
     * @return maDM hoặc null nếu không có nút nào active
     */
    private String getActiveCategoryMaDM() {
        for (javafx.scene.Node node : categoryButtonBox.getChildren()) {
            if (node instanceof Button && node.getStyleClass().contains("active")) {
                String tenDM = ((Button) node).getText();
                // Dùng DAO để lấy mã từ tên
                return danhMucMonDAO.getMaDMByTenDM(tenDM);
            }
        }
        return null; // Không có nút nào active (ví dụ: khi đang tìm kiếm)
    }

    
    // (Lớp MenuItem giữ nguyên)
    public static class MenuItem {
        private final SimpleStringProperty maMon; 
        private final SimpleStringProperty name;
        private final SimpleObjectProperty<Image> image;
        private final SimpleDoubleProperty price;
        private final SimpleStringProperty categoryName; 
        private final SimpleStringProperty maDM; 
        
        public MenuItem(String maMon, String name, Image image, double price, String categoryName, String maDM) { 
            this.maMon = new SimpleStringProperty(maMon);
            this.name = new SimpleStringProperty(name);
            this.price = new SimpleDoubleProperty(price);
            this.categoryName = new SimpleStringProperty(categoryName); 
            this.maDM = new SimpleStringProperty(maDM);
            this.image = new SimpleObjectProperty<>(image);
        }
        public String getMaMon() { return maMon.get(); } 
        public String getName() { return name.get(); } 
        public Image getImage() { return image.get(); }
        public double getPrice() { return price.get(); }
        public String getCategory() { return categoryName.get(); } 
        public String getMaDM() { return maDM.get(); } 
        public SimpleStringProperty maMonProperty() { return maMon; }
        public SimpleStringProperty nameProperty() { return name; }
        public SimpleObjectProperty<Image> imageProperty() { return image; }
        public SimpleDoubleProperty priceProperty() { return price; }
        public SimpleStringProperty categoryProperty() { return categoryName; }
        public SimpleStringProperty maDMProperty() { return maDM; }
    }
}
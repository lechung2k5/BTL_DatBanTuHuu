package ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class KhuyenMai {

    @FXML private TableView<Promotion> tblKhuyenMai;
    @FXML private TableColumn<Promotion, String> colTenKM;
    @FXML private TableColumn<Promotion, String> colMaKM;
    @FXML private TableColumn<Promotion, String> colThoiGian;
    @FXML private TableColumn<Promotion, String> colTrangThai;
    @FXML private TableColumn<Promotion, Void> colHanhDong;
    
    @FXML private TextField txtTenKM;
    @FXML private DatePicker datePickerStart;
    @FXML private DatePicker datePickerEnd;
    @FXML private ComboBox<String> filterComboBox;

    @FXML private Button btnThem, btnSua, btnLuu;
    
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public void initialize() {
        setupTableColumns();
        loadSampleData();
        
        // Set initial form values from the image
        txtTenKM.setText("Giảm 10% cho hóa đơn 2,000,000");
        datePickerStart.setValue(LocalDate.of(2022, 12, 12));
        datePickerEnd.setValue(LocalDate.of(2023, 1, 12));
        
        filterComboBox.setItems(FXCollections.observableArrayList("Tất cả", "Đang áp dụng", "Tạm ngưng", "Không áp dụng"));
        filterComboBox.setValue("Đang áp dụng...");

        btnThem.setOnAction(e -> System.out.println("Thêm clicked"));
        btnSua.setOnAction(e -> System.out.println("Sửa clicked"));
        btnLuu.setOnAction(e -> System.out.println("Lưu clicked"));
    }

    private void setupTableColumns() {
        tblKhuyenMai.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        colTenKM.setCellValueFactory(new PropertyValueFactory<>("name"));
        colMaKM.setCellValueFactory(new PropertyValueFactory<>("code"));
        colThoiGian.setCellValueFactory(new PropertyValueFactory<>("duration"));
        colTrangThai.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        // Add delete buttons to the action column
        colHanhDong.setCellFactory(param -> new TableCell<Promotion, Void>() {
            private final Button deleteButton = new Button("Xóa");
            private final HBox pane = new HBox(deleteButton);
            {
                deleteButton.getStyleClass().add("delete-button");
                pane.setAlignment(Pos.CENTER);
                deleteButton.setOnAction(event -> {
                    Promotion promo = getTableView().getItems().get(getIndex());
                    System.out.println("Deleting: " + promo.getName());
                    // Add deletion logic here
                    getTableView().getItems().remove(promo);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void loadSampleData() {
        ObservableList<Promotion> promotions = FXCollections.observableArrayList();
        promotions.add(new Promotion(
            "Giảm 10% cho đơn từ 200k", "KM10", 
            LocalDate.of(2024, 4, 1), LocalDate.of(2024, 4, 30), 
            "Đang áp dụng (combobox)"
        ));
        promotions.add(new Promotion(
            "Giảm 15% cho khách hàng có thẻ thành viên đã thanh toán trên 1.000.000 VNĐ", "TV15", 
            LocalDate.of(2024, 5, 1), LocalDate.of(2024, 5, 15), 
            "Tạm ngưng"
        ));
        promotions.add(new Promotion(
            "Giảm 5% cho khách hàng có thẻ thành viên đã thanh toán trên 500.000 VNĐ", "TV05", 
            LocalDate.of(2024, 3, 1), LocalDate.of(2024, 3, 31), 
            "Không áp dụng"
        ));
        // *** ADDED MORE DATA TO SHOW SCROLLBAR ***
        promotions.add(new Promotion(
            "Khuyến mãi 20/10", "PNVN",
            LocalDate.of(2024, 10, 15), LocalDate.of(2024, 10, 21),
            "Đang áp dụng"
        ));
        promotions.add(new Promotion(
            "Mừng Quốc Khánh 2/9", "QK0209",
            LocalDate.of(2024, 8, 30), LocalDate.of(2024, 9, 3),
            "Đã hết hạn"
        ));
        promotions.add(new Promotion(
            "Chào hè rực rỡ", "SUMMER24",
            LocalDate.of(2024, 6, 1), LocalDate.of(2024, 8, 31),
            "Tạm ngưng"
        ));
        promotions.add(new Promotion(
            "Black Friday Sale", "BF2024",
            LocalDate.of(2024, 11, 29), LocalDate.of(2024, 11, 29),
            "Sắp diễn ra"
        ));
        tblKhuyenMai.setItems(promotions);
    }
    
    public class Promotion {
        private final SimpleStringProperty name;
        private final SimpleStringProperty code;
        private final SimpleStringProperty duration;
        private final SimpleStringProperty status;

        public Promotion(String name, String code, LocalDate startDate, LocalDate endDate, String status) {
            this.name = new SimpleStringProperty(name);
            this.code = new SimpleStringProperty(code);
            this.duration = new SimpleStringProperty(startDate.format(formatter) + " - " + endDate.format(formatter));
            this.status = new SimpleStringProperty(status);
        }

        public String getName() { return name.get(); }
        public String getCode() { return code.get(); }
        public String getDuration() { return duration.get(); }
        public String getStatus() { return status.get(); }
    }
}
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

public class KhachHang {

    @FXML private TableView<Customer> tblKhachHang;
    @FXML private TableColumn<Customer, String> colMaKH;
    @FXML private TableColumn<Customer, String> colHoTen;
    @FXML private TableColumn<Customer, String> colSDT;
    @FXML private TableColumn<Customer, String> colDiaChi;
    @FXML private TableColumn<Customer, String> colEmail;
    @FXML private TableColumn<Customer, String> colNgayDangKy;
    @FXML private TableColumn<Customer, String> colLoaiKH;
    @FXML private TableColumn<Customer, String> colTongTien;
    @FXML private TableColumn<Customer, Void> colXemLichSu;

    @FXML private TextField txtHoTen, txtSDT, txtDiaChi, txtEmail, txtSearch;
    @FXML private DatePicker datePickerNgayDangKy;
    @FXML private ComboBox<String> filterComboBox;

    @FXML private Button btnThem, btnXoa, btnSua, btnLuu;
    
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public void initialize() {
        setupTableColumns();
        loadSampleData();
        
        // Set initial form values from the image
        setInitialFormValues();
        
        filterComboBox.setItems(FXCollections.observableArrayList("Tất cả", "VIP", "Thành viên"));

        // Button action placeholders
        btnThem.setOnAction(e -> System.out.println("Thêm clicked"));
        btnXoa.setOnAction(e -> System.out.println("Xóa clicked"));
        btnSua.setOnAction(e -> System.out.println("Sửa clicked"));
        btnLuu.setOnAction(e -> System.out.println("Lưu clicked"));
    }

    private void setupTableColumns() {
        tblKhachHang.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        colMaKH.setCellValueFactory(new PropertyValueFactory<>("id"));
        colHoTen.setCellValueFactory(new PropertyValueFactory<>("name"));
        colSDT.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colDiaChi.setCellValueFactory(new PropertyValueFactory<>("address"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colNgayDangKy.setCellValueFactory(new PropertyValueFactory<>("joinDate"));
        colLoaiKH.setCellValueFactory(new PropertyValueFactory<>("type"));
        colTongTien.setCellValueFactory(new PropertyValueFactory<>("totalSpent"));
        
        // Add "Xem" buttons to the action column
        colXemLichSu.setCellFactory(param -> new TableCell<Customer, Void>() {
            private final Button viewButton = new Button("Xem");
            private final HBox pane = new HBox(viewButton);
            {
                viewButton.getStyleClass().add("view-button");
                pane.setAlignment(Pos.CENTER);
                viewButton.setOnAction(event -> {
                    Customer customer = getTableView().getItems().get(getIndex());
                    System.out.println("Viewing history for: " + customer.getName());
                    // Add logic to show history
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
        ObservableList<Customer> customers = FXCollections.observableArrayList();
        customers.add(new Customer("KH01", "Nguyễn Văn A", "0363636363", "Phường Hưng Thạnh, TPHCM", "abc@gmail.com", LocalDate.of(2025, 2, 28), "VIP", "1,000,000 Đ"));
        customers.add(new Customer("KH02", "Nguyễn Văn B", "0363636363", "123 Đường ABC, Quận 1", "def@gmail.com", LocalDate.of(2025, 2, 28), "Thành viên", "1,000,000 Đ"));
        customers.add(new Customer("KH03", "Nguyễn Văn C", "0363636363", "456 Đường XYZ, Quận 2", "ghi@gmail.com", LocalDate.of(2025, 2, 28), "Thành viên", "1,000,000 Đ"));
        // Add more data to show scrollbar
        for (int i = 4; i <= 15; i++) {
             customers.add(new Customer("KH" + String.format("%02d", i), "Khách hàng " + i, "0987654321", "Địa chỉ " + i, "email"+i+"@example.com", LocalDate.now(), "Thành viên", i*100000 + " Đ"));
        }
        tblKhachHang.setItems(customers);
    }
    
    private void setInitialFormValues() {
        txtHoTen.setText("Nguyễn Văn A");
        txtSDT.setText("0325454123");
        datePickerNgayDangKy.setValue(LocalDate.of(2022, 12, 12));
        txtDiaChi.setText("Phường Hưng Thạnh, TPHCM");
        txtEmail.setText("abc@gmail.com");
    }
    
    public class Customer {
        private final SimpleStringProperty id;
        private final SimpleStringProperty name;
        private final SimpleStringProperty phone;
        private final SimpleStringProperty address;
        private final SimpleStringProperty email;
        private final SimpleStringProperty joinDate;
        private final SimpleStringProperty type;
        private final SimpleStringProperty totalSpent;

        public Customer(String id, String name, String phone, String address, String email, LocalDate joinDate, String type, String totalSpent) {
            this.id = new SimpleStringProperty(id);
            this.name = new SimpleStringProperty(name);
            this.phone = new SimpleStringProperty(phone);
            this.address = new SimpleStringProperty(address);
            this.email = new SimpleStringProperty(email);
            this.joinDate = new SimpleStringProperty(joinDate.format(formatter));
            this.type = new SimpleStringProperty(type);
            this.totalSpent = new SimpleStringProperty(totalSpent);
        }

        // Getters
        public String getId() { return id.get(); }
        public String getName() { return name.get(); }
        public String getPhone() { return phone.get(); }
        public String getAddress() { return address.get(); }
        public String getEmail() { return email.get(); }
        public String getJoinDate() { return joinDate.get(); }
        public String getType() { return type.get(); }
        public String getTotalSpent() { return totalSpent.get(); }
    }
}
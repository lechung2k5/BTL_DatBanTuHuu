package ui;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class DatBan {

    // === CỘT 1 ===
    @FXML private ComboBox<String> comboFilter;
    @FXML private TextField txtSearch;
    @FXML private VBox vboxBookingCards;
    @FXML private Button btnTaoDatBan;

    // === CỘT 2 ===
    @FXML private TextField txtThoiGian;
    @FXML private DatePicker datePickerThoiGianDen;
    @FXML private Button btnTim;
    @FXML private GridPane gridTangTret;
    @FXML private GridPane gridTang1;
    @FXML private GridPane gridPhongRieng;
    @FXML private Button btnXacNhanBan;
    @FXML private TextField txtTenKhachHang;
    @FXML private TextField txtSoDienThoai;
    @FXML private TextField txtSoLuongKhach;
    @FXML private TextField txtYeuCau;
    @FXML private TextField txtTienCoc;
    @FXML private Button btnThanhToanCoc;
    @FXML private ToggleButton toggleKhaiVi;
    @FXML private ToggleButton toggleNuong;
    @FXML private ToggleButton toggleLau;
    @FXML private ToggleButton toggleXaoHap;
    @FXML private ToggleButton toggleChien;
    @FXML private ToggleButton toggleDacSan;
    @FXML private ToggleButton toggleDoUong;
    @FXML private Button btnLuuDatHang;
    @FXML private Button btnThanhToan;
    
    // Bảng món ăn
    @FXML private TableView<MonAn> tblMonAn;
    @FXML private TableColumn<MonAn, String> colTenMon;
    @FXML private TableColumn<MonAn, String> colHinhAnh;
    @FXML private TableColumn<MonAn, Number> colGia;
    @FXML private TableColumn<MonAn, Void> colChon;

    // Bảng món đã chọn
    @FXML private TableView<MonOrder> tblMonDaChon;
    @FXML private TableColumn<MonOrder, String> colOrderTenMon;
    @FXML private TableColumn<MonOrder, Number> colOrderDonGia;
    @FXML private TableColumn<MonOrder, Integer> colOrderSoLuong;
    @FXML private TableColumn<MonOrder, Void> colOrderTangGiam;
    @FXML private TableColumn<MonOrder, Void> colOrderHuy;

    // === CỘT 3 ===
    @FXML private TableView<?> tblHoaDon;
    @FXML private ComboBox<String> promoComboBox;
    @FXML private Label promoLabel;
    @FXML private Button btnTienMat;
    @FXML private Button btnNganHang;
    @FXML private Button btnMoMo;
    @FXML private Button btnInHoaDon;

    private ToggleGroup menuGroup;

    @FXML
    private void initialize() {
        populateTableGrid(gridTangTret, 1, 20, 5);
        populateTableGrid(gridTang1, 21, 30, 5);  
        populatePhongRiengGrid();
        loadBookingCards();
        
        menuGroup = new ToggleGroup();
        toggleKhaiVi.setToggleGroup(menuGroup);
        toggleNuong.setToggleGroup(menuGroup);
        toggleLau.setToggleGroup(menuGroup);
        toggleXaoHap.setToggleGroup(menuGroup);
        toggleChien.setToggleGroup(menuGroup);
        toggleDacSan.setToggleGroup(menuGroup);
        toggleDoUong.setToggleGroup(menuGroup);
        toggleKhaiVi.setSelected(true);
        
        promoComboBox.setItems(FXCollections.observableArrayList("Giảm 15% hóa đơn cho khách hàng VIP", "Không áp dụng"));
        promoComboBox.getSelectionModel().selectFirst();
        
        setupMonAnTable();
        setupMonOrderTable();
    }
    
    private void setupMonAnTable() {
        colTenMon.setCellValueFactory(cellData -> cellData.getValue().tenMonProperty());
        colGia.setCellValueFactory(cellData -> cellData.getValue().giaProperty());
        colHinhAnh.setCellFactory(param -> {
            ImageView imageView = new ImageView();
            imageView.setFitHeight(40);
            imageView.setFitWidth(40);
            TableCell<MonAn, String> cell = new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    if (empty || item == null) {
                        setGraphic(null);
                    } else {
                        // imageView.setImage(new Image(item));
                        setGraphic(imageView);
                    }
                }
            };
            cell.setAlignment(Pos.CENTER);
            return cell;
        });
        colChon.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Chọn");
            {
                btn.getStyleClass().add("select-button");
                btn.setOnAction(event -> { /* Logic thêm món */ });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                setGraphic(empty ? null : btn);
                setAlignment(Pos.CENTER);
            }
        });
        tblMonAn.setItems(FXCollections.observableArrayList(
            new MonAn("Đậu phộng rang muối", "path/to/image.jpg", 20000),
            new MonAn("Khô mực nướng", "path/to/image.jpg", 20000),
            new MonAn("Khô cá chỉ vàng", "path/to/image.jpg", 20000)
        ));
    }
    
    private void setupMonOrderTable() {
        colOrderTenMon.setCellValueFactory(cellData -> cellData.getValue().tenMonProperty());
        colOrderDonGia.setCellValueFactory(cellData -> cellData.getValue().donGiaProperty());
        colOrderSoLuong.setCellValueFactory(cellData -> cellData.getValue().soLuongProperty().asObject());
        colOrderTangGiam.setCellFactory(param -> new TableCell<>() {
            private final Button btnPlus = new Button("+");
            private final Button btnMinus = new Button("-");
            private final HBox pane = new HBox(5, btnMinus, btnPlus);
            {
                btnPlus.getStyleClass().add("quantity-button");
                btnMinus.getStyleClass().add("quantity-button");
                pane.setAlignment(Pos.CENTER);
            }
             @Override
            protected void updateItem(Void item, boolean empty) {
                setGraphic(empty ? null : pane);
            }
        });
        colOrderHuy.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Hủy");
            {
                btn.getStyleClass().add("cancel-button");
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                setGraphic(empty ? null : btn);
                setAlignment(Pos.CENTER);
            }
        });
        tblMonDaChon.setItems(FXCollections.observableArrayList(
            new MonOrder("Lẩu cua đồng", 250000, 1),
            new MonOrder("Cơm chiên hải sản", 80000, 2),
            new MonOrder("Ghẹ rang muối ớt", 150000, 1)
        ));
    }

    private void populateTableGrid(GridPane grid, int start, int end, int numCols) {
        int row = 0, col = 0;
        for (int i = start; i <= end; i++) {
            Button tableButton = new Button(String.format("%02d", i));
            tableButton.getStyleClass().add("table-button");
            if (i % 4 == 0) tableButton.getStyleClass().add("table-button-booked");
            else if (i % 7 == 0) tableButton.getStyleClass().add("table-button-selected");
            else tableButton.getStyleClass().add("table-button-available");
            grid.add(tableButton, col, row);
            col++;
            if (col == numCols) { col = 0; row++; }
        }
    }
    
    private void populatePhongRiengGrid() {
        int[] phongRiengNumbers = {31, 32, 33, 34, 35, 36};
        int row = 0, col = 0;
        for (int tableNum : phongRiengNumbers) {
            Button tableButton = new Button(String.valueOf(tableNum));
            tableButton.getStyleClass().addAll("table-button", "table-button-wide");
            if (tableNum == 32 || tableNum == 35) tableButton.getStyleClass().add("table-button-booked");
            else tableButton.getStyleClass().add("table-button-available");
            gridPhongRieng.add(tableButton, col, row);
            col++;
            if (col == 3) { col = 0; row++; }
        }
    }
    
    private void loadBookingCards() {
        vboxBookingCards.getChildren().add(createBookingCard("DB00023", "0377019956", "Đã xác nhận", "12:30 PM", "01"));
        vboxBookingCards.getChildren().add(createBookingCard("DB00024", "0377019956", "Đang phục vụ", "10:30 PM", "23"));
        vboxBookingCards.getChildren().add(createBookingCard("DB00025", "0377019956", "Đang phục vụ", "06:30 PM", "26"));
    }

    private VBox createBookingCard(String id, String phone, String status, String time, String table) {
        VBox card = new VBox(5);
        card.getStyleClass().add("booking-card");
        Label lblId = new Label(id);
        lblId.getStyleClass().add("booking-card-id");
        Label lblPhone = new Label("SĐT: " + phone);
        Label lblStatus = new Label("Trạng thái: " + status);
        Label lblTime = new Label("Thời gian đặt: " + time);
        Label lblTable = new Label("Bàn: " + table);
        lblPhone.getStyleClass().add("booking-card-info");
        lblStatus.getStyleClass().add("booking-card-info");
        lblTime.getStyleClass().add("booking-card-info");
        lblTable.getStyleClass().add("booking-card-info");
        Button btnDetails = new Button("Xem chi tiết");
        btnDetails.getStyleClass().add("view-details-button");
        VBox.setMargin(btnDetails, new Insets(8, 0, 0, 0));
        card.getChildren().addAll(lblId, lblPhone, lblStatus, lblTime, lblTable, btnDetails);
        return card;
    }

    // Lớp dữ liệu cho bảng Món Ăn
    public static class MonAn {
        private final SimpleStringProperty tenMon;
        private final SimpleStringProperty hinhAnh;
        private final SimpleDoubleProperty gia;

        public MonAn(String tenMon, String hinhAnh, double gia) {
            this.tenMon = new SimpleStringProperty(tenMon);
            this.hinhAnh = new SimpleStringProperty(hinhAnh);
            this.gia = new SimpleDoubleProperty(gia);
        }
        public String getTenMon() { return tenMon.get(); }
        public SimpleStringProperty tenMonProperty() { return tenMon; }
        public String getHinhAnh() { return hinhAnh.get(); }
        public SimpleStringProperty hinhAnhProperty() { return hinhAnh; }
        public double getGia() { return gia.get(); }
        public SimpleDoubleProperty giaProperty() { return gia; }
    }

    // Lớp dữ liệu cho bảng Món Order
    public static class MonOrder {
        private final SimpleStringProperty tenMon;
        private final SimpleDoubleProperty donGia;
        private final SimpleIntegerProperty soLuong;

        public MonOrder(String tenMon, double donGia, int soLuong) {
            this.tenMon = new SimpleStringProperty(tenMon);
            this.donGia = new SimpleDoubleProperty(donGia);
            this.soLuong = new SimpleIntegerProperty(soLuong);
        }
        public String getTenMon() { return tenMon.get(); }
        public SimpleStringProperty tenMonProperty() { return tenMon; }
        public double getDonGia() { return donGia.get(); }
        public SimpleDoubleProperty donGiaProperty() { return donGia; }
        public int getSoLuong() { return soLuong.get(); }
        public SimpleIntegerProperty soLuongProperty() { return soLuong; }
    }
}
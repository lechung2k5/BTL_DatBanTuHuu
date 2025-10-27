package ui;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

public class ThongKe {

    // Invoice Table
    @FXML private ComboBox<String> hoaDonFilterCombo;
    @FXML private TableView<HoaDonThongKe> tblHoaDon;
    @FXML private TableColumn<HoaDonThongKe, String> colMaHoaDon;
    @FXML private TableColumn<HoaDonThongKe, String> colThoiGianVao;
    @FXML private TableColumn<HoaDonThongKe, Number> colTongTien;
    @FXML private TableColumn<HoaDonThongKe, Void> colXemChiTiet;
    @FXML private Button btnExportHoaDon;

    // Best Sellers Table
    @FXML private ComboBox<String> monAnFilterCombo;
    @FXML private TableView<MonBanChay> tblMonBanChay;
    @FXML private TableColumn<MonBanChay, Integer> colSTT;
    @FXML private TableColumn<MonBanChay, String> colTenMon;
    @FXML private TableColumn<MonBanChay, Integer> colSoLuong;
    @FXML private TableColumn<MonBanChay, Number> colDoanhThuMon;
    @FXML private Button btnExportMonAn;

    // Charts
    @FXML private ComboBox<String> doanhThuFilterCombo;
    @FXML private BarChart<String, Number> barChartDoanhThu;
    @FXML private LineChart<String, Number> lineChartKhuVuc;
    
    @FXML private Label lblTangTretTotal;
    @FXML private Label lblTang1Total;


    @FXML
    public void initialize() {
        setupFilters();
        setupInvoiceTable();
        setupBestSellersTable();
        setupRevenueChart();
        setupAreaChart();
        
        lblTangTretTotal.setText("$3,004");
        lblTang1Total.setText("$4,504");
     // === PHÍM TẮT THAO TÁC BẢNG ===
        tblHoaDon.sceneProperty().addListener((obsScene, oldScene, newScene) -> {
            if (newScene == null) return;

            newScene.getAccelerators().clear();

            // Ctrl + K → Focus bảng Hóa đơn
            newScene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.K, KeyCombination.CONTROL_DOWN),
                () -> {
                    tblHoaDon.requestFocus();
                    tblHoaDon.getSelectionModel().selectFirst();
                }
            );

            // Ctrl + C → Focus bảng Món bán chạy
            newScene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN),
                () -> {
                    tblMonBanChay.requestFocus();
                    tblMonBanChay.getSelectionModel().selectFirst();
                }
            );

            // Enter → Xem chi tiết hóa đơn
            tblHoaDon.setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.ENTER) {
                    var item = tblHoaDon.getSelectionModel().getSelectedItem();
                    if (item != null) {
                        System.out.println("📌 Chi tiết hóa đơn: " + item.getMaHD());
                    }
                }
            });

            // Enter → Xem thông tin món bán chạy
            tblMonBanChay.setOnKeyPressed(e -> {
                if (e.getCode() == KeyCode.ENTER) {
                    var item = tblMonBanChay.getSelectionModel().getSelectedItem();
                    if (item != null) {
                        System.out.println("🍽 Chi tiết món bán chạy: " + item.getTenMon());
                    }
                }
            });

            // Ctrl + E → Xuất Excel (tùy bảng đang focus)
            newScene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.E, KeyCombination.CONTROL_DOWN),
                () -> {
                    if (tblHoaDon.isFocused()) {
                        System.out.println("📊 Xuất Excel Hóa đơn!");
                        // btnExportHoaDon.fire();
                    } else if (tblMonBanChay.isFocused()) {
                        System.out.println("📈 Xuất Excel Món bán chạy!");
                        // btnExportMonAn.fire();
                    }
                }
            );

            // ESC → Bỏ focus cả 2 bảng
            newScene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.ESCAPE),
                () -> {
                    tblHoaDon.getSelectionModel().clearSelection();
                    tblMonBanChay.getSelectionModel().clearSelection();
                }
            );
        });

    }
    
    private void setupFilters() {
        hoaDonFilterCombo.setItems(FXCollections.observableArrayList("Theo ngày", "Theo tuần", "Theo tháng"));
        hoaDonFilterCombo.setValue("Theo ngày");
        
        monAnFilterCombo.setItems(FXCollections.observableArrayList("Theo tháng", "Theo tuần", "Theo ngày"));
        monAnFilterCombo.setValue("Theo tháng");

        doanhThuFilterCombo.setItems(FXCollections.observableArrayList("Theo ngày", "Theo tuần", "Theo tháng"));
        doanhThuFilterCombo.setValue("Theo ngày");
    }

    private void setupInvoiceTable() {
        tblHoaDon.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        colMaHoaDon.setCellValueFactory(new PropertyValueFactory<>("maHD"));
        colThoiGianVao.setCellValueFactory(new PropertyValueFactory<>("thoiGianVao"));
        colTongTien.setCellValueFactory(new PropertyValueFactory<>("tongTien"));
        
        colTongTien.setCellFactory(column -> new TableCell<HoaDonThongKe, Number>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%,.0f VND", item.doubleValue()));
                }
            }
        });

        colXemChiTiet.setCellFactory(param -> new TableCell<HoaDonThongKe, Void>() {
            private final Button btn = new Button("Xem");
            {
                btn.getStyleClass().add("view-button");
                btn.setOnAction(event -> {
                    HoaDonThongKe data = getTableView().getItems().get(getIndex());
                    System.out.println("Xem chi tiết cho hóa đơn: " + data.getMaHD());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
                setAlignment(Pos.CENTER);
            }
        });

        tblHoaDon.setItems(getInvoiceSampleData());
    }
    
    private ObservableList<HoaDonThongKe> getInvoiceSampleData() {
        return FXCollections.observableArrayList(
            new HoaDonThongKe("HD01", "09:05 AM", 150000),
            new HoaDonThongKe("HD02", "09:05 AM", 150000),
            new HoaDonThongKe("HD03", "09:05 AM", 150000),
            new HoaDonThongKe("HD04", "09:05 AM", 150000)
        );
    }

    private void setupBestSellersTable() {
        tblMonBanChay.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        colSTT.setCellValueFactory(new PropertyValueFactory<>("stt"));
        colTenMon.setCellValueFactory(new PropertyValueFactory<>("tenMon"));
        colSoLuong.setCellValueFactory(new PropertyValueFactory<>("soLuongBan"));
        colDoanhThuMon.setCellValueFactory(new PropertyValueFactory<>("doanhThu"));
        
        colDoanhThuMon.setCellFactory(column -> new TableCell<MonBanChay, Number>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : String.format("%,.0f", item.doubleValue()));
            }
        });

        tblMonBanChay.setItems(getBestSellersSampleData());
    }
    
    private ObservableList<MonBanChay> getBestSellersSampleData() {
        return FXCollections.observableArrayList(
            new MonBanChay(1, "Bia Sài Gòn Xanh", 1359, 36321000),
            new MonBanChay(2, "Đậu phộng rang muối", 1359, 36321000),
            new MonBanChay(3, "Lẩu thập cẩm", 1359, 36321000),
            new MonBanChay(4, "Hàu nướng phô mai", 1359, 36321000),
            new MonBanChay(5, "Coca Cola", 1359, 36321000)
        );
    }

    private void setupRevenueChart() {
        barChartDoanhThu.setAnimated(false);
        XYChart.Series<String, Number> series1 = new XYChart.Series<>();
        series1.setName("Khung giờ Ngày/giờ");
        series1.getData().add(new XYChart.Data<>("T2", 10000));
        series1.getData().add(new XYChart.Data<>("T3", 15000));
        series1.getData().add(new XYChart.Data<>("T4", 8000));

        XYChart.Series<String, Number> series2 = new XYChart.Series<>();
        series2.setName("Khách thân quen");
        series2.getData().add(new XYChart.Data<>("T2", 12000));
        series2.getData().add(new XYChart.Data<>("T3", 18000));
        series2.getData().add(new XYChart.Data<>("T4", 7000));

        barChartDoanhThu.getData().addAll(series1, series2);
        barChartDoanhThu.setLegendVisible(true);
    }

    private void setupAreaChart() {
        lineChartKhuVuc.setAnimated(false);
        XYChart.Series<String, Number> series1 = new XYChart.Series<>();
        series1.setName("Tầng trệt");
        series1.getData().add(new XYChart.Data<>("Jan", 3000));
        series1.getData().add(new XYChart.Data<>("Feb", 3200));
        series1.getData().add(new XYChart.Data<>("Mar", 4000));
        series1.getData().add(new XYChart.Data<>("Apr", 3500));
        
        XYChart.Series<String, Number> series2 = new XYChart.Series<>();
        series2.setName("Tầng 1");
        series2.getData().add(new XYChart.Data<>("Jan", 4500));
        series2.getData().add(new XYChart.Data<>("Feb", 4200));
        series2.getData().add(new XYChart.Data<>("Mar", 5000));
        series2.getData().add(new XYChart.Data<>("Apr", 4800));

        lineChartKhuVuc.getData().addAll(series1, series2);
        lineChartKhuVuc.setLegendVisible(false);
    }

    // --- Inner classes for TableView data models ---
    public static class HoaDonThongKe {
        private final SimpleStringProperty maHD;
        private final SimpleStringProperty thoiGianVao;
        private final SimpleDoubleProperty tongTien;

        public HoaDonThongKe(String maHD, String thoiGianVao, double tongTien) {
            this.maHD = new SimpleStringProperty(maHD);
            this.thoiGianVao = new SimpleStringProperty(thoiGianVao);
            this.tongTien = new SimpleDoubleProperty(tongTien);
        }
        public String getMaHD() { return maHD.get(); }
        public String getThoiGianVao() { return thoiGianVao.get(); }
        public double getTongTien() { return tongTien.get(); }
    }

    public static class MonBanChay {
        private final SimpleIntegerProperty stt;
        private final SimpleStringProperty tenMon;
        private final SimpleIntegerProperty soLuongBan;
        private final SimpleDoubleProperty doanhThu;
        
        public MonBanChay(int stt, String tenMon, int soLuongBan, double doanhThu) {
            this.stt = new SimpleIntegerProperty(stt);
            this.tenMon = new SimpleStringProperty(tenMon);
            this.soLuongBan = new SimpleIntegerProperty(soLuongBan);
            this.doanhThu = new SimpleDoubleProperty(doanhThu);
        }
        public int getStt() { return stt.get(); }
        public String getTenMon() { return tenMon.get(); }
        public int getSoLuongBan() { return soLuongBan.get(); }
        public double getDoanhThu() { return doanhThu.get(); }
    }
}
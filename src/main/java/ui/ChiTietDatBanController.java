package ui;

import dao.DatBanDAO;
import entity.HoaDon;
import entity.PTTThanhToan;
import javafx.beans.property.SimpleDoubleProperty; 
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos; 
import javafx.scene.Node; 
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType; // Import AlertType
import javafx.scene.layout.GridPane; 
import javafx.scene.layout.HBox; 
import javafx.scene.layout.StackPane; 
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import ui.DatBan.MonOrder; 

import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

/**
 * Phiên bản Controller ĐƠN GIẢN:
 * Chỉ hiển thị Panel_ChiTietOrder, không chuyển đổi panel.
 * Ẩn các nút chức năng (Gọi món, Đổi bàn, Tách bàn).
 * Chỉ giữ lại nút "Thanh Toán".
 */
public class ChiTietDatBanController implements Initializable {
    
    // =========================================================
    // HEADER FIELDS (Dùng để hiển thị thông tin hóa đơn)
    // =========================================================
    @FXML private Label lblMaHD;
    @FXML private TextField txtTenKhachHang;
    @FXML private TextField txtSoDienThoai;
    @FXML private TextField txtSoLuongKhach;
    @FXML private TextField txtYeuCau;
    @FXML private TextField txtMaBan;
    @FXML private ComboBox<String> comboTrangThai;
    @FXML private TextField txtThoiGian;
    @FXML private DatePicker datePickerThoiGianDen;
    
    // NÚT HÀNH ĐỘNG CHÍNH (Sẽ bị ẩn đi)
    @FXML private Button btnCapNhat;
    @FXML private Button btnDoiBan;
    @FXML private Button btnTachBan;
    @FXML private Button btnGoiMon;
    @FXML private Button btnHuyBan;
    @FXML private Button btnBack; 

    // =========================================================
    // ORDER PANEL FIELDS (Ánh xạ từ Panel_ChiTietOrder.fxml qua lookup)
    // =========================================================
    @FXML private TableView<MonOrder> tblChiTietOrder;
    @FXML private TableColumn<MonOrder, String> colTenMon;
    @FXML private TableColumn<MonOrder, Number> colDonGia;
    @FXML private TableColumn<MonOrder, Number> colSoLuong;
    @FXML private TableColumn<MonOrder, Number> colThanhTien;
    @FXML private TableColumn<MonOrder, Void> colTangGiam;
    @FXML private TableColumn<MonOrder, Void> colHuy;
    
    @FXML private Label lblTongTienMonAn;
    @FXML private Label lblTienCoc;
    @FXML private Label lblTongThanhToan;
    @FXML private Button btnThanhToan; // Nút này được giữ lại

    // =NOTO: ĐÃ XÓA CÁC FIELDS TỪ GOIMON_POPUP.FXML
    // =NOTO: ĐÃ XÓA CÁC FIELDS TỪ DOIBAN_POPUP.FXML

    // =========================================================
    // CONTAINER VÀ LOGIC FIELDS
    // =========================================================
    @FXML private StackPane contentContainer; 
    
    private HoaDon hoaDon;
    private DatBanDAO datBanDAO;
    private ObservableList<MonOrder> monOrderList = FXCollections.observableArrayList();
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    
    // Cache panel duy nhất
    private Node orderPanel;
    
    // =NOTO: ĐÃ XÓA CÁC DAO VÀ LIST PHỤ


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        
        comboTrangThai.setItems(FXCollections.observableArrayList("Đã đặt", "Đang phục vụ", "Đã thanh toán", "Đã hủy", "Hóa đơn tạm"));
        
        // Chỉ gán sự kiện cho nút Back (để đóng popup)
        btnBack.setOnAction(e -> handleClosePopup());
        
        // Các nút khác không cần gán sự kiện vì sẽ bị ẩn
        // btnCapNhat.setOnAction(...);
        // btnTachBan.setOnAction(...); 
        // btnGoiMon.setOnAction(...);
        // btnDoiBan.setOnAction(...);
    }
    
    public void setHoaDonData(HoaDon hd, DatBanDAO dao) {
        this.hoaDon = hd;
        this.datBanDAO = dao;
        
        // Tải panel chi tiết order duy nhất
        loadOrderPanel();
        loadDataToUI(); 
    }

    private void loadDataToUI() {
        if (hoaDon == null) return;
        
        lblMaHD.setText("Chi tiết đặt bàn " + hoaDon.getMaHD());
        
        if (hoaDon.getKhachHang() != null) {
            txtTenKhachHang.setText(hoaDon.getKhachHang().getTenKH());
            txtSoDienThoai.setText(hoaDon.getKhachHang().getSoDT());
        } else {
             txtTenKhachHang.setText("Khách vãng lai");
             txtSoDienThoai.setText("");
        }
        
        if (hoaDon.getBan() != null) {
            txtMaBan.setText(hoaDon.getBan().getMaBan());
            txtSoLuongKhach.setText(String.valueOf(hoaDon.getBan().getSucChua()));
        } else {
             txtMaBan.setText("Chưa chọn");
             txtSoLuongKhach.setText("");
        }
        
        if (hoaDon.getGioVao() != null) {
            txtThoiGian.setText(hoaDon.getGioVao().toLocalTime().format(timeFormatter));
            datePickerThoiGianDen.setValue(hoaDon.getGioVao().toLocalDate());
        }
        
        if(lblTienCoc != null) {
            lblTienCoc.setText(String.format("%,.0f Đ", hoaDon.getTienCoc()));
        }
        
        String trangThaiHienThi = switch (hoaDon.getTrangThai().getDbValue()) {
            case "Dat" -> "Đã đặt"; 
            case "DangSuDung" -> "Đang phục vụ";
            case "DaThanhToan" -> "Đã thanh toán";
            case "DaHuy" -> "Đã hủy";
            case "HoaDonTam" -> "Hóa đơn tạm";
            default -> "Chờ";
        };
        comboTrangThai.getSelectionModel().select(trangThaiHienThi);
        
        txtYeuCau.setText("phòng riêng/bàn tầng trệt");
    }
    
    // Phương thức tải danh sách món từ DAO
    public void loadOrderDetail() {
        if (hoaDon.getMaHD() != null && tblChiTietOrder != null) { 
            monOrderList.clear();
            ObservableList<MonOrder> chiTiet = datBanDAO.getChiTietHoaDon(hoaDon.getMaHD()); 
            monOrderList.addAll(chiTiet);
            tblChiTietOrder.setItems(monOrderList);
            calculateTotal(); // Tính tổng tiền sau khi load
        }
    }
    
    // Phương thức cài đặt TableView
    private void setupOrderTable() {
        if (tblChiTietOrder == null) return;

        colTenMon.setCellValueFactory(cellData -> cellData.getValue().tenMonProperty());
        colDonGia.setCellValueFactory(cellData -> cellData.getValue().donGiaProperty());
        colSoLuong.setCellValueFactory(cellData -> cellData.getValue().soLuongProperty()); 
        
        colThanhTien.setCellValueFactory(cellData -> new SimpleDoubleProperty(
            cellData.getValue().getDonGia() * cellData.getValue().getSoLuong()
        ));

        // Logic tăng giảm / hủy
        colTangGiam.setCellFactory(tc -> new TableCell<MonOrder, Void>() {
            final HBox box = new HBox(5);
            final Button btnMinus = new Button("-");
            final Button btnPlus = new Button(" +");
            
            {
                box.setAlignment(Pos.CENTER);
                box.getChildren().addAll(btnMinus, btnPlus);

                btnPlus.setOnAction(event -> {
                    MonOrder order = getTableView().getItems().get(getIndex());
                    order.setSoLuong(order.getSoLuong() + 1);
                    tblChiTietOrder.refresh();
                    calculateTotal(); 
                });

                btnMinus.setOnAction(event -> {
                    MonOrder order = getTableView().getItems().get(getIndex());
                    if (order.getSoLuong() > 1) {
                        order.setSoLuong(order.getSoLuong() - 1);
                    } else {
                        monOrderList.remove(order);
                    }
                    tblChiTietOrder.refresh();
                    calculateTotal(); 
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        colHuy.setCellFactory(tc -> new TableCell<MonOrder, Void>() {
            final Button btnHuy = new Button("X"); 
            {
                btnHuy.setOnAction(event -> {
                    MonOrder order = getTableView().getItems().get(getIndex());
                    monOrderList.remove(order);
                    tblChiTietOrder.refresh();
                    calculateTotal(); 
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnHuy);
            }
        });
        
        tblChiTietOrder.setItems(monOrderList);
    }
    
    // Phương thức tính tổng tiền
    public void calculateTotal() {
        double tongTienMonAn = monOrderList.stream()
                .mapToDouble(order -> order.getDonGia() * order.getSoLuong())
                .sum();
        
        final double VAT_RATE = 0.08; 
        double thueVAT = tongTienMonAn * VAT_RATE;
        
        double tienCocDaThanhToan = (hoaDon != null) ? hoaDon.getTienCoc() : 0.0;
        
        double tongTienThanhToan = tongTienMonAn + thueVAT - tienCocDaThanhToan;
        
        if (lblTongTienMonAn != null) lblTongTienMonAn.setText(String.format("%,.0f Đ", tongTienMonAn));
        if (lblTienCoc != null) lblTienCoc.setText(String.format("%,.0f Đ", tienCocDaThanhToan)); // Cập nhật tiền cọc ở đây
        if (lblTongThanhToan != null) lblTongThanhToan.setText(String.format("%,.0f Đ", Math.max(0, tongTienThanhToan))); 
    }
    
    // =========================================================
    // CÁC PHƯƠNG THỨC XỬ LÝ SỰ KIỆN (Đã đơn giản hóa)
    // =========================================================

    /**
     * Nút này đã bị ẩn, nhưng giữ lại hàm
     */
    @FXML
    private void handleCapNhatDatBan() {
        showAlert(AlertType.INFORMATION, "Thông báo", "Chức năng cập nhật thông tin đang được triển khai.");
    }

    /**
     * Xử lý thanh toán (Nút này được giữ lại)
     */
    @FXML
    private void handleThanhToan() {
        PTTThanhToan ptThanhToan = PTTThanhToan.TIEN_MAT; 

        try {
            datBanDAO.capNhatKhiThanhToan(hoaDon.getMaHD(), ptThanhToan);
            
            if (hoaDon.getBan() != null) {
                 datBanDAO.capNhatTrangThaiBan(hoaDon.getBan().getMaBan(), "Trong");
            }

            showAlert(AlertType.INFORMATION, "Thành công", "Hóa đơn " + hoaDon.getMaHD() + " đã được thanh toán thành công. Bàn đã được giải phóng.");
            
            Stage stage = (Stage) btnThanhToan.getScene().getWindow();
            
            // Cập nhật lại màn hình DatBan chính
            if (stage.getScene().getRoot().getUserData() instanceof DatBan) {
                 ((DatBan) stage.getScene().getRoot().getUserData()).loadBookingCards();
                 ((DatBan) stage.getScene().getRoot().getUserData()).loadTableGrids();
            }
            
            stage.close();

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Lỗi Thanh Toán", "Không thể hoàn tất thanh toán: " + e.getMessage());
        }
    }
    
    /**
     * Nút này đã bị ẩn, nhưng giữ lại hàm
     */
    @FXML
    private void handleTachBan() {
        if (hoaDon == null || hoaDon.getBan() == null) {
            showAlert(AlertType.WARNING, "Lỗi Tách Bàn", "Không thể tách bàn khi chưa có thông tin Hóa đơn hoặc Bàn.");
            return;
        }
        
        // ... (logic tách bàn) ...
        showAlert(AlertType.INFORMATION, "Thông báo", "Chức năng Tách bàn (đã bị vô hiệu hóa).");
    }

    // =NOTO: ĐÃ XÓA CÁC HÀM: handleClose, handleCapNhatOrder, handleXacNhanDoiBan

    // =========================================================
    // LOGIC TẢI PANEL DUY NHẤT
    // =========================================================

    /**
     * Chỉ tải Panel_ChiTietOrder và ẩn các nút chức năng.
     */
    private void loadOrderPanel() {
        try {
            if (orderPanel == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Panel_ChiTietOrder.fxml"));
                orderPanel = loader.load();
                
                // Gán lại các FXML fields từ Panel_ChiTietOrder (Sử dụng lookup)
                tblChiTietOrder = (TableView) orderPanel.lookup("#tblChiTietOrder");
                colTenMon = (TableColumn) tblChiTietOrder.getColumns().get(0);
                colDonGia = (TableColumn) tblChiTietOrder.getColumns().get(1);
                colSoLuong = (TableColumn) tblChiTietOrder.getColumns().get(2);
                colThanhTien = (TableColumn) tblChiTietOrder.getColumns().get(3);
                colTangGiam = (TableColumn) tblChiTietOrder.getColumns().get(4);
                colHuy = (TableColumn) tblChiTietOrder.getColumns().get(5);
                
                lblTongTienMonAn = (Label) orderPanel.lookup("#lblTongTienMonAn");
                lblTienCoc = (Label) orderPanel.lookup("#lblTienCoc");
                lblTongThanhToan = (Label) orderPanel.lookup("#lblTongThanhToan");
                
                btnThanhToan = (Button) orderPanel.lookup("#btnThanhToan");
                btnThanhToan.setOnAction(e -> handleThanhToan());
                
                setupOrderTable(); 
            }
            
            loadOrderDetail(); // Load/refresh dữ liệu món ăn
            loadDataToUI(); // Tải lại thông tin header
            
            // Đặt panel vào container
            contentContainer.getChildren().clear();
            contentContainer.getChildren().add(orderPanel);
            
            // === THAY ĐỔI THEO YÊU CẦU MỚI ===
            // Ẩn tất cả các nút chức năng
            btnBack.setVisible(true); // Giữ lại nút Back để đóng
            btnCapNhat.setVisible(false);
            btnDoiBan.setVisible(false);
            btnGoiMon.setVisible(false);
            btnHuyBan.setVisible(false);
            btnTachBan.setVisible(false);


        } catch (IOException e) {
            showAlert(AlertType.ERROR, "Lỗi UI", "Không thể tải panel chi tiết order.");
            e.printStackTrace();
        }
    }
    
    // =NOTO: ĐÃ XÓA: handleSwitchToGoiMonPanel, handleSwitchToDoiBanPanel
    // =NOTO: ĐÃ XÓA: Tất cả các hàm helper cho 2 panel phụ

    // =========================================================
    // HÀM UTILITY
    // =========================================================

    /**
     * Đóng cửa sổ popup
     */
    @FXML
    private void handleClosePopup() {
        Stage stage = (Stage) btnBack.getScene().getWindow();
        if (stage != null) {
            stage.close();
        }
    }

    private void showAlert(AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
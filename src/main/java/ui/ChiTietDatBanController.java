package ui;

import dao.DatBanDAO;
import entity.HoaDon;
import entity.PTTThanhToan;
import entity.TrangThaiBan;
import entity.TrangThaiHoaDon;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import ui.DatBan.MonOrder;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller Chi Tiết Đặt Bàn: Xử lý hiển thị thông tin và CẬP NHẬT TRẠNG THÁI.
 * Đã được đơn giản hóa: Ẩn các nút chức năng phức tạp (Đổi/Tách/Gọi món).
 * =========================================================
 * FIX: Đã thêm lại hàm setHoaDonData và sử dụng các @FXML fields đúng.
 * =========================================================
 */
public class ChiTietDatBanController implements Initializable {

    // =========================================================
    // FXML FIELDS (LƯỢC BỎ CÁC NÚT KHÔNG CẦN THIẾT)
    // =========================================================
    @FXML private Label lblMaHD;
    @FXML private TextField txtTenKhachHang;
    @FXML private TextField txtSoDienThoai;
    @FXML private TextField txtSoLuongKhach;
    @FXML private TextField txtYeuCau;
    @FXML private TextField txtMaBan;
    @FXML private ComboBox<String> comboTrangThai; // Chứa tên hiển thị trạng thái
    @FXML private TextField txtThoiGian;
    @FXML private DatePicker datePickerThoiGianDen;

    // NÚT CHỨC NĂNG CẦN GIỮ LẠI (Dựa trên ChiTietDatBan_Popup.fxml đã sửa)
    @FXML private Button btnCapNhat;
    @FXML private Button btnBack;
    
    // FXML fields từ Panel_ChiTietOrder.fxml (Cần được gán lại qua lookup nếu không dùng Controller con)
    @FXML private StackPane contentContainer; 
    private Label lblTienCoc;
    private Label lblTongTienMonAn;
    private Label lblTongThanhToan;
    private TableView<MonOrder> tblChiTietOrder;

    // =========================================================
    // LOGIC & DATA
    // =========================================================
    private HoaDon hoaDon;
    private DatBanDAO datBanDAO;
    private ObservableList<MonOrder> monOrderList = FXCollections.observableArrayList();
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private Node orderPanel; // Cache panel con

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Khởi tạo ComboBox (Sử dụng tên hiển thị)
        comboTrangThai.setItems(FXCollections.observableArrayList("Đã đặt", "Đang phục vụ", "Đã thanh toán", "Đã hủy", "Hóa đơn tạm"));

        // Gán sự kiện
        btnBack.setOnAction(e -> handleClosePopup());
        if (btnCapNhat != null) {
            btnCapNhat.setOnAction(e -> handleCapNhatDatBan());
        }
    }

    /**
     * 🔥 Phương thức khởi tạo dữ liệu và nhận tham chiếu DAO.
     * Đây là hàm kích hoạt việc tải dữ liệu và giao diện.
     */
    public void setHoaDonData(HoaDon hd, DatBanDAO dao) {
        this.hoaDon = hd;
        this.datBanDAO = dao;

        // Tải Panel Chi tiết Order
        loadOrderPanel();

        // Tải dữ liệu vào UI (Header + Panel Order)
        loadDataToUI();
        
        // Cần tải lại Panel Order để hiển thị món
        loadOrderDetail(); 
    }

    /**
     * Tải dữ liệu chính (Header) lên UI.
     */
    private void loadDataToUI() {
        if (hoaDon == null) return;

        lblMaHD.setText("Chi tiết đặt bàn " + hoaDon.getMaHD());
        
        // Lấy giá trị trạng thái DB
        String trangThaiHdDb = hoaDon.getTrangThai().getDbValue();

        if (hoaDon.getKhachHang() != null) {
            txtTenKhachHang.setText(hoaDon.getKhachHang().getTenKH());
            txtSoDienThoai.setText(hoaDon.getKhachHang().getSoDT());
        } 

        if (hoaDon.getBan() != null) {
            txtMaBan.setText(hoaDon.getBan().getMaBan());
            txtSoLuongKhach.setText(String.valueOf(hoaDon.getBan().getSucChua()));
        } 

        if (hoaDon.getGioVao() != null) {
            txtThoiGian.setText(hoaDon.getGioVao().toLocalTime().format(timeFormatter));
            datePickerThoiGianDen.setValue(hoaDon.getGioVao().toLocalDate());
        }

        // === CẬP NHẬT COMBO BOX TRẠNG THÁI (LẤY TÊN HIỂN THỊ) ===
        String trangThaiHienThi = TrangThaiHoaDon.fromDbValue(trangThaiHdDb).getDisplayName();
        comboTrangThai.getSelectionModel().select(trangThaiHienThi);
        // ===================================
    }
    
    /**
     * Tải Panel Chi Tiết Order (Món ăn) và gán các fields.
     */
    private void loadOrderPanel() {
        if (contentContainer == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Panel_ChiTietOrder.fxml"));
            orderPanel = loader.load(); 

            // === GÁN CÁC FIELDS BỊ THIẾU TỪ PANEL PHỤ QUA LOOKUP ===
            tblChiTietOrder = (TableView) orderPanel.lookup("#tblChiTietOrder");
            lblTongTienMonAn = (Label) orderPanel.lookup("#lblTongTienMonAn");
            lblTienCoc = (Label) orderPanel.lookup("#lblTienCoc");
            lblTongThanhToan = (Label) orderPanel.lookup("#lblTongThanhToan");
            Button btnThanhToan = (Button) orderPanel.lookup("#btnThanhToan"); // Nút thanh toán trong panel

            // Gán sự kiện thanh toán (tùy chọn)
            if (btnThanhToan != null) {
                // btnThanhToan.setOnAction(e -> handleThanhToan());
                btnThanhToan.setVisible(false); // Ẩn nút Thanh Toán theo yêu cầu
            }
            // ========================================================

            setupOrderTable(); // Cài đặt TableView
            contentContainer.getChildren().clear();
            contentContainer.getChildren().add(orderPanel);

        } catch (IOException e) {
             showAlert(AlertType.ERROR, "Lỗi UI", "Không thể tải file /fxml/Panel_ChiTietOrder.fxml.");
        }
    }
    
    /**
     * Tải dữ liệu món ăn vào TableView.
     */
    public void loadOrderDetail() {
        if (hoaDon != null && hoaDon.getMaHD() != null && tblChiTietOrder != null) {
            monOrderList.clear();
            // Hàm getChiTietHoaDon trả về ObservableList<DatBan.MonOrder>
            ObservableList<DatBan.MonOrder> chiTiet = datBanDAO.getChiTietHoaDon(hoaDon.getMaHD());
            monOrderList.addAll(chiTiet);
            tblChiTietOrder.setItems(monOrderList);
            calculateTotal(); 
        }
    }

    /**
     * Cài đặt cấu trúc cột cho TableView (Dùng các fields đã lookup).
     */
    private void setupOrderTable() {
        if (tblChiTietOrder == null) return;

        // Giả sử các cột đã được định nghĩa đúng thứ tự và kiểu trong FXML
        // Dùng Index để lấy cột (không an toàn, nhưng cần thiết nếu không dùng fx:id)
        if (tblChiTietOrder.getColumns().size() >= 6) {
             // Cần định nghĩa lại các cột theo kiểu đúng
             TableColumn<MonOrder, String> colTenMon = (TableColumn<MonOrder, String>) tblChiTietOrder.getColumns().get(0);
             TableColumn<MonOrder, Number> colDonGia = (TableColumn<MonOrder, Number>) tblChiTietOrder.getColumns().get(1);
             TableColumn<MonOrder, Integer> colSoLuong = (TableColumn<MonOrder, Integer>) tblChiTietOrder.getColumns().get(2);
             TableColumn<MonOrder, Number> colThanhTien = (TableColumn<MonOrder, Number>) tblChiTietOrder.getColumns().get(3);
             TableColumn<MonOrder, Void> colTangGiam = (TableColumn<MonOrder, Void>) tblChiTietOrder.getColumns().get(4);
             TableColumn<MonOrder, Void> colHuy = (TableColumn<MonOrder, Void>) tblChiTietOrder.getColumns().get(5);

             colTenMon.setCellValueFactory(cellData -> cellData.getValue().tenMonProperty());
             colDonGia.setCellValueFactory(cellData -> cellData.getValue().donGiaProperty());
             colSoLuong.setCellValueFactory(cellData -> cellData.getValue().soLuongProperty().asObject());
             colThanhTien.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getDonGia() * cellData.getValue().getSoLuong()));
             
             // Logic tăng giảm / hủy (Giữ nguyên logic phức tạp)
             // ... (cần thêm logic CellFactory cho TangGiam và Huy)
        }
    }


    /**
     * Tính tổng tiền và cập nhật labels.
     */
    public void calculateTotal() {
        double tongTienMonAn = monOrderList.stream()
                .mapToDouble(order -> order.getDonGia() * order.getSoLuong())
                .sum();

        final double VAT_RATE = 0.08;
        double thueVAT = tongTienMonAn * VAT_RATE;

        // Giả sử getTienCoc() trả về double (kiểu nguyên thủy)
        double tienCocDaThanhToan = (hoaDon != null) ? hoaDon.getTienCoc() : 0.0; 

        double tongTienThanhToan = tongTienMonAn + thueVAT - tienCocDaThanhToan;

        // Luôn kiểm tra null cho các UI component được gán động
        if (lblTongTienMonAn != null) lblTongTienMonAn.setText(String.format("%,.0f Đ", tongTienMonAn));
        if (lblTienCoc != null) lblTienCoc.setText(String.format("%,.0f Đ", tienCocDaThanhToan)); 
        if (lblTongThanhToan != null) lblTongThanhToan.setText(String.format("%,.0f Đ", Math.max(0, tongTienThanhToan)));
    }


    /**
     * 🔥 Xử lý Cập nhật thông tin đặt bàn và TRẠNG THÁI (FINAL FIX)
     */
    @FXML
    private void handleCapNhatDatBan() {
        if (hoaDon == null) return;
        
        String trangThaiMoiDisplay = comboTrangThai.getSelectionModel().getSelectedItem();
        
        // --- FIX LỖI: KIỂM TRA NULL TỪ fromDisplayName ---
        // Convert display name to Enum (đảm bảo không bị NullPointerException khi gọi .getDbValue())
        TrangThaiHoaDon newStatusEnum = TrangThaiHoaDon.fromDisplayName(trangThaiMoiDisplay);
        if (newStatusEnum == null) {
            showAlert(AlertType.ERROR, "Lỗi Chuyển Đổi", "Trạng thái được chọn không hợp lệ. Vui lòng kiểm tra lại.");
            return;
        }
        String trangThaiMoiDb = newStatusEnum.getDbValue(); 
        // ----------------------------------------------------

        try {
            // Xác định xem có cần set gioRa (Chỉ khi DaThanhToan hoặc DaHuy)
            boolean setGioRa = (trangThaiMoiDb.equals(TrangThaiHoaDon.DA_THANH_TOAN.getDbValue()) || trangThaiMoiDb.equals(TrangThaiHoaDon.DA_HUY.getDbValue()));
            
            // 1. Cập nhật trạng thái Hóa đơn
            datBanDAO.capNhatTrangThaiHoaDon(hoaDon.getMaHD(), trangThaiMoiDb, setGioRa);
            
            // 2. Cập nhật trạng thái Bàn
            if(hoaDon.getBan() != null) {
                // Nếu trạng thái mới là Đã Thanh Toán hoặc Đã Hủy, Bàn phải chuyển về TRỐNG.
                // Ngược lại, Bàn giữ trạng thái mới (Dat, DangSuDung, HoaDonTam).
                String banStatusUpdate = (setGioRa) ? TrangThaiBan.TRONG.getDbValue() : trangThaiMoiDb;
                datBanDAO.capNhatTrangThaiBan(hoaDon.getBan().getMaBan(), banStatusUpdate);
            }
            
            // 3. Tải lại dữ liệu chính và đóng popup
            Stage stage = (Stage) btnCapNhat.getScene().getWindow();
            if (stage.getScene().getRoot().getUserData() instanceof DatBan) {
                DatBan parentCtrl = (DatBan) stage.getScene().getRoot().getUserData();
                parentCtrl.loadBookingCards();
                parentCtrl.loadTableGrids();
            }
            
            showAlert(AlertType.INFORMATION, "Thành công", "Đã cập nhật trạng thái Hóa đơn thành: " + trangThaiMoiDisplay);
            
            handleClosePopup(); 
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Lỗi Cập Nhật", "Không thể cập nhật trạng thái: " + e.getMessage());
        }
    }
    
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
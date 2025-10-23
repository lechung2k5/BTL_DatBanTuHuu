package ui;

// === IMPORTS ===
import dao.DatBanDAO;
import dao.MonAnDAO;
import dao.DanhMucMonDAO;
import entity.*; // Import hết entity
// Import JavaFX và Java Util
import java.io.ByteArrayInputStream;
import java.net.URL;
import java.sql.Timestamp; // Import Timestamp cho getBanTrongTheoGio
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

/**
 * Controller cho màn hình Đặt Bàn.
 * Đã cập nhật để đồng bộ với entity HoaDon mới.
 */
public class DatBan implements Initializable {

    // DAO
    private final DatBanDAO datBanDAO = new DatBanDAO();
    private final MonAnDAO monAnDAO = new MonAnDAO();
    private final DanhMucMonDAO danhMucMonDAO = new DanhMucMonDAO();
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy"); // Thêm date formatter

    // Data
    private ObservableList<MonOrder> monOrderList = FXCollections.observableArrayList();
    private List<DanhMucMon> dsDanhMuc;
    private ObservableList<Ban> selectedBanList = FXCollections.observableArrayList();
    private ObservableList<Button> selectedButtonList = FXCollections.observableArrayList();
    private List<HoaDon> dsHoaDonDatTrongNgay = new ArrayList<>();
    private boolean isBookingConfirmed = false;
    private HoaDon currentHoaDon = null;
    private ToggleGroup menuGroup;
    private ToggleGroup paymentGroup;

    // === FXML Components ===
    @FXML private ComboBox<String> comboFilter; @FXML private TextField txtSearch; @FXML private VBox vboxBookingCards;
    @FXML private ScrollPane middleScrollPane; @FXML private TextField txtThoiGian; @FXML private DatePicker datePickerThoiGianDen;
    @FXML private Button btnTim; @FXML private GridPane gridTangTret; @FXML private GridPane gridTang1; @FXML private GridPane gridPhongRieng;
    @FXML private TextField txtTenKhachHang; @FXML private TextField txtSoDienThoai; @FXML private TextField txtSoLuongKhach;
    @FXML private TextField txtYeuCau; @FXML private TextField txtTienCoc; @FXML private Button btnThanhToanCoc;
    @FXML private Label lblTrangThaiBan; @FXML private Label lblBanDangChon; @FXML private Button btnTimKhach; @FXML private Button btnXacNhanBan;
    @FXML private ScrollPane rightScrollPane; @FXML private VBox vboxRightPanel; @FXML private VBox vboxMenuGoiMon; @FXML private ScrollPane vboxReceipt;
    @FXML private HBox hboxDanhMucToggles; @FXML private ToggleButton toggleKhaiVi, toggleNuong, toggleLau, toggleXaoHap, toggleChien, toggleDacSan, toggleDoUong;
    @FXML private TextField txtTimMon; @FXML private TableView<MonAnViewModel> tblMonAn; // Sử dụng ViewModel
    @FXML private TableColumn<MonAnViewModel, String> colTenMon; @FXML private TableColumn<MonAnViewModel, Image> colHinhAnh; // Sửa kiểu thành Image
    @FXML private TableColumn<MonAnViewModel, Number> colGia; @FXML private TableColumn<MonAnViewModel, Void> colChon;
    @FXML private TableView<MonOrder> tblMonDaChon; @FXML private TableColumn<MonOrder, String> colOrderTenMon;
    @FXML private TableColumn<MonOrder, Number> colOrderDonGia; @FXML private TableColumn<MonOrder, Integer> colOrderSoLuong;
    @FXML private TableColumn<MonOrder, Void> colOrderTangGiam; @FXML private TableColumn<MonOrder, Void> colOrderHuy;
    @FXML private Button btnLuuDatHang; @FXML private ComboBox<String> promoComboBox; @FXML private Label lblTongTienMonAn;
    @FXML private Label lblPhiDichVu; @FXML private Label lblThueVAT; @FXML private Label lblKhuyenMai; @FXML private Label lblTienCocSummary;
    @FXML private Label lblTongTienThanhToan; @FXML private ToggleButton btnTienMat; @FXML private ToggleButton btnNganHang;
    @FXML private ToggleButton btnMoMo; @FXML private Button btnXacNhanThanhToan; @FXML private Button btnInHoaDon;

    // =========================================================
    // INITIALIZE
    // =========================================================
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        dsDanhMuc = danhMucMonDAO.getAllDanhMuc();
        setupToggleGroups();
        setupMonAnTable(); // Setup ViewModel table
        setupMonOrderTable();

        // Load default menu items
        List<entity.MonAn> dsEntityMonAn = monAnDAO.getAllMonAn();
        loadMonAnTable(dsEntityMonAn); // Truyền trực tiếp list entity

        loadBookingCards();
        loadTableGrids();

        promoComboBox.setItems(FXCollections.observableArrayList("Không áp dụng", "Giảm 10% (VIP)", "Tặng món KM"));
        promoComboBox.getSelectionModel().selectFirst();
        datePickerThoiGianDen.setValue(LocalDate.now());
        txtThoiGian.setText(LocalTime.now().format(timeFormatter));

        // Event Handlers
        btnXacNhanBan.setOnAction(e -> handleXacNhanBan());
        btnTim.setOnAction(e -> handleTimBanTrong());
        btnLuuDatHang.setOnAction(e -> handleLuuDatHang());
        btnXacNhanThanhToan.setOnAction(e -> handleXacNhanThanhToan());
        if(btnInHoaDon != null) btnInHoaDon.setOnAction(e -> handleInHoaDon()); // Gán sự kiện nút In

        // Menu Toggles Event
        if (hboxDanhMucToggles != null) {
            hboxDanhMucToggles.getChildren().filtered(n -> n instanceof ToggleButton).forEach(n -> {
                ToggleButton t = (ToggleButton) n;
                if (t.getUserData() instanceof String) { // Kiểm tra kiểu UserData
                    t.setOnAction(e -> handleMenuToggle((String) t.getUserData()));
                }
            });
        }

        // Search Listeners
        if (txtTimMon != null) txtTimMon.textProperty().addListener((o, ov, nv) -> handleTimMon(nv));
        if (txtSoDienThoai != null) txtSoDienThoai.focusedProperty().addListener((o, ov, nv) -> { if (!nv) handleTimTenKhachHang(txtSoDienThoai.getText()); });

        updateSelectionLabels();
        calculateTotal();

        // Hide receipt panel initially
        showOrHideReceiptPanel(false);

        // Listeners for total calculation
        monOrderList.addListener((javafx.collections.ListChangeListener.Change<? extends MonOrder> c) -> calculateTotal());
        txtTienCoc.textProperty().addListener((o, ov, nv) -> calculateTotal());
    }

    // =========================================================
    // SETUP UI COMPONENTS
    // =========================================================
    private void setupToggleGroups() { /* ... Giữ nguyên ... */ }

    /** Cấu hình bảng hiển thị Món Ăn (dùng ViewModel) */
    private void setupMonAnTable() {
        if (tblMonAn == null) return;
        colTenMon.setCellValueFactory(cellData -> cellData.getValue().tenMonProperty());
        colGia.setCellValueFactory(cellData -> cellData.getValue().giaBanProperty());

        // Cell factory cho hình ảnh
        colHinhAnh.setCellValueFactory(cellData -> cellData.getValue().hinhAnhProperty());
        colHinhAnh.setCellFactory(param -> new TableCell<MonAnViewModel, Image>() {
            private final ImageView imageView = new ImageView();
            {
                imageView.setFitHeight(50); // Kích thước ảnh
                imageView.setFitWidth(50);
                imageView.setPreserveRatio(true);
                setAlignment(Pos.CENTER); // Căn giữa ảnh
            }
            @Override
            protected void updateItem(Image item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    imageView.setImage(item);
                    setGraphic(imageView);
                }
            }
        });

        // Cell factory cho nút Chọn
        colChon.setCellFactory(param -> new TableCell<MonAnViewModel, Void>() {
            private final Button btnChon = new Button("Chọn");
            {
                btnChon.setOnAction(event -> {
                    MonAnViewModel monVM = getTableView().getItems().get(getIndex());
                    if (monVM != null) {
                        handleChonMon(monVM.getMaMon(), monVM.getTenMon(), monVM.getGiaBan()); // Truyền thông tin cần thiết
                    }
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnChon);
            }
        });
    }

     /** Cấu hình bảng Món Đã Chọn (MonOrder) */
    private void setupMonOrderTable() {
        if (tblMonDaChon == null) return;
        colOrderTenMon.setCellValueFactory(cellData -> cellData.getValue().tenMonProperty());
        colOrderDonGia.setCellValueFactory(cellData -> cellData.getValue().donGiaProperty());
        colOrderSoLuong.setCellValueFactory(cellData -> cellData.getValue().soLuongProperty().asObject());

        // Cell factory cho nút Tăng/Giảm
        colOrderTangGiam.setCellFactory(param -> new TableCell<MonOrder, Void>() {
             private final Button btnPlus = new Button("+");
             private final Button btnMinus = new Button("-");
             private final HBox pane = new HBox(5, btnMinus, btnPlus);
             {
                 pane.setAlignment(Pos.CENTER);
                 btnPlus.setOnAction(event -> getTableView().getItems().get(getIndex()).increaseQuantity());
                 btnMinus.setOnAction(event -> getTableView().getItems().get(getIndex()).decreaseQuantity());
             }
             @Override protected void updateItem(Void item, boolean empty) {
                 super.updateItem(item, empty); setGraphic(empty ? null : pane);
             }
        });

        // Cell factory cho nút Hủy
        colOrderHuy.setCellFactory(param -> new TableCell<MonOrder, Void>() {
            private final Button btnHuy = new Button("X");
            {
                btnHuy.setOnAction(event -> monOrderList.remove(getIndex()));
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty); setGraphic(empty ? null : btnHuy);
            }
        });
        tblMonDaChon.setItems(monOrderList);
    }


    // =========================================================
    // EVENT HANDLERS & LOGIC
    // =========================================================

    /** Xử lý lưu đơn đặt hàng/đến quán */
    private void handleLuuDatHang() {
        // ... (Kiểm tra điều kiện: chọn bàn, xác nhận) ...
        if (selectedBanList.isEmpty() || !isBookingConfirmed) { /* ... show alert ... */ return; }

        try {
            // Lấy và kiểm tra thông tin KH, Thời gian
            String sdt = txtSoDienThoai.getText().trim(); String tenKH = txtTenKhachHang.getText().trim();
            LocalDate ngayDen = datePickerThoiGianDen.getValue(); String gioStr = txtThoiGian.getText();
            if (sdt.isEmpty() || ngayDen == null || gioStr.isEmpty()) { /* ... show alert ... */ return; }
            entity.KhachHang khachHang = datBanDAO.timHoacTaoKhachHang(sdt, tenKH);
            LocalDateTime thoiGianDen = ngayDen.atTime(LocalTime.parse(gioStr, timeFormatter));

            // Xác định trạng thái ban đầu
            TrangThaiHoaDon trangThaiBanDau = thoiGianDen.isBefore(LocalDateTime.now().plusMinutes(15))
                                                ? TrangThaiHoaDon.DANG_SU_DUNG : TrangThaiHoaDon.DAT;

            // Xử lý từng bàn được chọn
            for (Ban banDuocChon : selectedBanList) {
                 // Kiểm tra lại trạng thái bàn ngay trước khi lưu
                TrangThaiBan trangThaiHienTai = getTrangThaiHienThi(banDuocChon, LocalTime.now());
                if (trangThaiHienTai != TrangThaiBan.TRONG) {
                     showAlert(Alert.AlertType.WARNING, "Bàn bận", "Bàn " + banDuocChon.getMaBan() + " vừa bị chiếm. Vui lòng chọn lại.");
                     resetSelectionAndConfirmation(); loadTableGrids(); return;
                }

                // Tạo đối tượng HoaDon mới
                HoaDon newHoaDon = new HoaDon();
                newHoaDon.setNgayLap(LocalDateTime.now());
                newHoaDon.setGioVao(thoiGianDen);
                newHoaDon.setKhachHang(khachHang);
                newHoaDon.setBan(banDuocChon);
                newHoaDon.setTrangThai(trangThaiBanDau);
                // Tiền cọc (đã parse và kiểm tra)
                double tienCoc = 0; try { String raw = txtTienCoc.getText().replaceAll("[^0-9.]", ""); tienCoc = Double.parseDouble(raw.isEmpty() ? "0" : raw); } catch (Exception e) {}
                newHoaDon.setTienCoc(tienCoc);
                 // Các trường khác như maUuDai, ptThanhToan, tenNV sẽ là null ban đầu

                // Lưu vào DB
                datBanDAO.luuHoaDonVaChiTiet(newHoaDon, monOrderList);

                // Cập nhật trạng thái bàn trong DB
                String trangThaiBanDb = (trangThaiBanDau == TrangThaiHoaDon.DAT) ? TrangThaiBan.DA_DAT.getDbValue() : TrangThaiBan.DANG_SU_DUNG.getDbValue();
                datBanDAO.capNhatTrangThaiBan(banDuocChon.getMaBan(), trangThaiBanDb);
            }

            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã lưu đặt hàng! Trạng thái: " + trangThaiBanDau.getDisplayName());
            resetSelectionAndConfirmation(); loadBookingCards(); loadTableGrids();

        } catch (Exception e) {
            e.printStackTrace(); showAlert(Alert.AlertType.ERROR, "Lỗi hệ thống", "Lỗi khi lưu đặt hàng: " + e.getMessage());
        }
    }

    private TrangThaiBan getTrangThaiHienThi(Ban banDuocChon, LocalTime now) {
		// TODO Auto-generated method stub
		return null;
	}

	/** Xử lý nút xác nhận bàn */
    private void handleXacNhanBan() { /* ... Giữ nguyên logic tính cọc, đổi màu, set flag isBookingConfirmed ... */ }

    /** Tính tiền cọc */
//   private double calculateTienCoc() { /* ... Giữ nguyên ... */ }

    /** Tính trạng thái hiển thị của bàn */
//    private TrangThaiBan getTrangThaiHienThi(Ban banGoc, LocalTime thoiGianKiemTra) { /* ... Giữ nguyên ... */ }

    /** Tải lại sơ đồ bàn */
    private void loadTableGrids() { /* ... Giữ nguyên ... */ }
    private void loadTableGridsBase(List<Ban> dsBan) { /* ... Giữ nguyên ... */ }
    private void populateTableGrid(GridPane grid, List<Ban> dsBan) {
         if (grid == null) return; grid.getChildren().clear(); int col = 0, row = 0, maxCols = 5;
         for (Ban ban : dsBan) {
             Button btn = createTableButton(ban);
             btn.setOnAction(e -> handleChonBan(ban, btn));
             grid.add(btn, col, row); GridPane.setMargin(btn, new Insets(8)); col++; if (col >= maxCols) { col = 0; row++; }
         }
    }
    /** Tạo nút bàn và GÁN USERDATA */
    private Button createTableButton(Ban ban) {
        String soBan = ban.getMaBan().replaceAll("[^0-9]", "");
        Button btn = new Button(soBan);
        btn.setUserData(ban.getMaBan()); // QUAN TRỌNG: Lưu mã bàn vào UserData
        btn.getStyleClass().add("table-button"); btn.setPrefSize(70, 70); btn.setAlignment(Pos.CENTER);
        applyTableStyle(btn, ban.getTrangThai());
        return btn;
    }
    private void applyTableStyle(Button button, TrangThaiBan trangThai) { /* ... Giữ nguyên ... */ }

    /** Xử lý chọn/bỏ chọn bàn */
    private void handleChonBan(Ban ban, Button currentButton) { /* ... Giữ nguyên logic cảnh báo và chọn/bỏ chọn ... */ }

    /** Reset tiền cọc và flag xác nhận */
    private void resetConfirmationStatus() { /* ... Giữ nguyên ... */ }
    /** Reset cả trạng thái chọn và xác nhận */
    private void resetSelectionAndConfirmation() {
         resetConfirmationStatus(); // Reset cọc và flag
         LocalTime now = LocalTime.now();
         List<Ban> allBans = datBanDAO.getAllBan(); // Lấy lại danh sách bàn gốc

         for(Button btn : selectedButtonList) {
             String maBan = getMaBanFromButton(btn); // Lấy mã bàn từ UserData
             if (maBan != null) {
                  allBans.stream().filter(b -> b.getMaBan().equals(maBan)).findFirst()
                         .ifPresent(banGoc -> {
                             TrangThaiBan trangThaiGoc = getTrangThaiHienThi(banGoc, now);
                             applyTableStyle(btn, trangThaiGoc); // Reset màu nút về trạng thái đúng
                         });
             }
         }
         selectedButtonList.clear(); selectedBanList.clear();
         updateSelectionLabels(); clearFormDatBan();
    }
    /** Lấy mã bàn từ UserData của Button */
    private String getMaBanFromButton(Button btn) {
        return (btn != null && btn.getUserData() instanceof String) ? (String) btn.getUserData() : null;
    }

    /** Cập nhật nhãn thông tin bàn đang chọn */
    private void updateSelectionLabels() { /* ... Giữ nguyên ... */ }

    /** Tải danh sách các card đặt bàn/đang sử dụng (cột trái) */
    private void loadBookingCards() { /* ... Giữ nguyên ... */ }
    /** Tạo card hiển thị thông tin đặt bàn (cột trái) */
//    private VBox createBookingCard(HoaDon hd) { /* ... Giữ nguyên ... */ }

    /** Tải thông tin từ card đã chọn lên form */
    private void handleSelectBookingCard(HoaDon hd) { /* ... Giữ nguyên logic load dữ liệu lên form, tải món, ẩn/hiện panel ... */ }

    /** Reset màu các nút trên sơ đồ bàn */
    private void resetUIMapSelection() { /* ... Giữ nguyên ... */ }
    /** Tô màu nút bàn được chọn từ card */
    private void highlightSelectedTableOnMap(Ban selectedBan) { /* ... Giữ nguyên ... */ }

    /** Tìm món ăn theo keyword */
    private void handleTimMon(String keyword) {
        List<entity.MonAn> dsEntityMonAn = (keyword == null || keyword.trim().isEmpty())
                                            ? monAnDAO.getAllMonAn()
                                            : monAnDAO.searchMonAnByName(keyword);
        loadMonAnTable(dsEntityMonAn);
    }
    /** Lọc món ăn theo danh mục */
    private void handleMenuToggle(String maDanhMuc) {
        if (maDanhMuc == null) return;
        List<entity.MonAn> dsEntityMonAn = monAnDAO.getMonAnByDanhMuc(maDanhMuc);
        loadMonAnTable(dsEntityMonAn);
        if (txtTimMon != null) txtTimMon.clear();
    }
    /** Tải danh sách món ăn lên bảng (chuyển đổi sang ViewModel) */
    private void loadMonAnTable(List<entity.MonAn> dsEntityMonAn) {
        List<MonAnViewModel> dsViewModel = dsEntityMonAn.stream()
                                                    .map(MonAnViewModel::fromEntity)
                                                    .collect(Collectors.toList());
        ObservableList<MonAnViewModel> monAnObservableList = FXCollections.observableArrayList(dsViewModel);
        if (tblMonAn != null) {
            tblMonAn.setItems(monAnObservableList);
            tblMonAn.refresh();
        }
    }

    /** Xử lý khi nhấn nút Chọn món */
    private void handleChonMon(String maMon, String tenMon, double giaBan) {
         Optional<MonOrder> existingOrder = monOrderList.stream().filter(o -> o.getMaMon().equals(maMon)).findFirst();
         if (existingOrder.isPresent()) { existingOrder.get().increaseQuantity(); }
         else { monOrderList.add(new MonOrder(maMon, tenMon, giaBan, 1)); }
         tblMonDaChon.refresh();
         // calculateTotal() tự gọi qua listener
    }

    /** Tính toán và hiển thị tổng tiền */
    private void calculateTotal() { /* ... Giữ nguyên ... */ }
    /** Xóa form đặt bàn */
    private void clearFormDatBan() { /* ... Giữ nguyên ... */ }
    /** Hiển thị thông báo */
    private void showAlert(Alert.AlertType type, String title, String content) { /* ... Giữ nguyên ... */ }
    /** Tìm tên khách hàng theo SĐT */
    private void handleTimTenKhachHang(String sdt) { /* ... Giữ nguyên ... */ }
    /** Tìm bàn trống theo thời gian */
    private void handleTimBanTrong() { /* ... Giữ nguyên ... */ }

    /** Hiển thị hoặc ẩn panel hóa đơn */
    private void showOrHideReceiptPanel(boolean show) { /* ... Giữ nguyên ... */ }

    /** Xử lý khi nhấn nút Thanh Toán (giờ là hiển thị panel hóa đơn) */
    private void handleDisplayReceipt() { /* ... Giữ nguyên logic kiểm tra trạng thái và hiển thị panel ... */ }

    /** Xử lý khi nhấn nút Xác Nhận Thanh Toán (cuối cùng) */
    private void handleXacNhanThanhToan() { /* ... Giữ nguyên logic lấy PTTT, gọi DAO cập nhật, reset UI ... */ }
    
    /** THÊM: Xử lý nút In Hóa Đơn */
    private void handleInHoaDon() {
        if (currentHoaDon == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn hóa đơn", "Vui lòng chọn hóa đơn cần in.");
            return;
        }
         // TODO: Thêm logic in hóa đơn ở đây (sử dụng JasperReports hoặc thư viện khác)
         showAlert(Alert.AlertType.INFORMATION, "In hóa đơn", "Chuẩn bị in hóa đơn: " + currentHoaDon.getMaHD());
    }


    // =========================================================
    // LỚP VIEWMODEL NỘI TUYẾN
    // =========================================================

    /** ViewModel cho bảng Món Ăn (tblMonAn) */
    public static class MonAnViewModel {
        private final SimpleStringProperty maMon;
        private final SimpleStringProperty tenMon;
        private final SimpleDoubleProperty giaBan;
        private final SimpleObjectProperty<Image> hinhAnh; // Dùng Image

        public MonAnViewModel(String maMon, String tenMon, double giaBan, byte[] hinhAnhBytes) {
            this.maMon = new SimpleStringProperty(maMon);
            this.tenMon = new SimpleStringProperty(tenMon);
            this.giaBan = new SimpleDoubleProperty(giaBan);
            // Chuyển byte[] thành Image
            Image img = null;
            if (hinhAnhBytes != null && hinhAnhBytes.length > 0) {
                try (ByteArrayInputStream bis = new ByteArrayInputStream(hinhAnhBytes)) {
                    img = new Image(bis);
                } catch (Exception e) { System.err.println("Lỗi load ảnh món ăn: " + e.getMessage()); }
            }
            this.hinhAnh = new SimpleObjectProperty<>(img);
        }

        // Chuyển từ Entity sang ViewModel
        public static MonAnViewModel fromEntity(entity.MonAn entity) {
            if (entity == null) return null;
            return new MonAnViewModel(entity.getMaMon(), entity.getTenMon(), entity.getGiaBan(), entity.getHinhAnh());
        }

        // Getters JavaFX Properties
        public String getMaMon() { return maMon.get(); }
        public String getTenMon() { return tenMon.get(); }
        public double getGiaBan() { return giaBan.get(); }
        public Image getHinhAnh() { return hinhAnh.get(); }
        public SimpleStringProperty maMonProperty() { return maMon; }
        public SimpleStringProperty tenMonProperty() { return tenMon; }
        public SimpleDoubleProperty giaBanProperty() { return giaBan; }
        public SimpleObjectProperty<Image> hinhAnhProperty() { return hinhAnh; }
    }


    /** ViewModel cho bảng Món Đã Chọn (tblMonDaChon) */
    public static class MonOrder {
        private final SimpleStringProperty maMon;
        private final SimpleStringProperty tenMon;
        private final SimpleDoubleProperty donGia;
        private final SimpleIntegerProperty soLuong;
        private final SimpleDoubleProperty thanhTien; // Thêm thành tiền

        public MonOrder(String maMon, String tenMon, double donGia, int soLuong) {
            this.maMon = new SimpleStringProperty(maMon);
            this.tenMon = new SimpleStringProperty(tenMon);
            this.donGia = new SimpleDoubleProperty(donGia);
            this.soLuong = new SimpleIntegerProperty(soLuong);
            this.thanhTien = new SimpleDoubleProperty(donGia * soLuong); // Tính ban đầu

            // Listener để tự động cập nhật thành tiền khi số lượng thay đổi
            this.soLuong.addListener((obs, oldV, newV) ->
                thanhTien.set(donGiaProperty().get() * newV.intValue())
            );
        }

        // Getters JavaFX Properties
        public String getMaMon() { return maMon.get(); }
        public String getTenMon() { return tenMon.get(); }
        public double getDonGia() { return donGia.get(); }
        public int getSoLuong() { return soLuong.get(); }
        public double getThanhTien() { return thanhTien.get(); } // Getter cho thành tiền
        public SimpleStringProperty maMonProperty() { return maMon; }
        public SimpleStringProperty tenMonProperty() { return tenMon; }
        public SimpleDoubleProperty donGiaProperty() { return donGia; }
        public SimpleIntegerProperty soLuongProperty() { return soLuong; }
        public SimpleDoubleProperty thanhTienProperty() { return thanhTien; } // Property cho thành tiền

        // Hàm tăng/giảm số lượng
        public void increaseQuantity() { setSoLuong(getSoLuong() + 1); }
        public void decreaseQuantity() { if (getSoLuong() > 1) setSoLuong(getSoLuong() - 1); }
        public void setSoLuong(int quantity) { if (quantity > 0) this.soLuong.set(quantity); }
    }

} // End class DatBan
package ui;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import dao.HoaDonDAO;
import dao.KhachHangDAO;
import entity.ChiTietHoaDon;
import entity.HoaDon;
import entity.PTTThanhToan;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;


public class KhachHang {

    // === Khai báo FXML ===
    @FXML private TableView<entity.KhachHang> tblKhachHang;
    @FXML private TableColumn<entity.KhachHang, String> colMaKH;
    @FXML private TableColumn<entity.KhachHang, String> colHoTen;
    @FXML private TableColumn<entity.KhachHang, String> colSDT;
    @FXML private TableColumn<entity.KhachHang, String> colDiaChi;
    @FXML private TableColumn<entity.KhachHang, String> colEmail;
    @FXML private TableColumn<entity.KhachHang, String> colNgayDangKy;
    @FXML private TableColumn<entity.KhachHang, String> colLoaiKH;
    @FXML private TableColumn<entity.KhachHang, String> colTongTien;
    @FXML private TableColumn<entity.KhachHang, Void> colXemLichSu;

    @FXML private TextField txtHoTen, txtSDT, txtDiaChi, txtEmail, txtSearch;
    @FXML private DatePicker datePickerNgayDangKy;
    @FXML private ComboBox<String> filterComboBox;

    @FXML private Button btnThem, btnXoa, btnSua, btnXoaTrang;
    @FXML private Button btnTim;

    // === Thuộc tính khác ===
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private KhachHangDAO khachHangDAO;
    private HoaDonDAO hoaDonDAO;
    private EventHandler<KeyEvent> khShortcutHandler;
    private boolean isDeletingInProgress = false; // ✅ Thêm flag để tránh xóa trùng lặp

    private ObservableList<entity.KhachHang> masterCustomerList;
    private FilteredList<entity.KhachHang> filteredCustomerList;


    // ==================================
    // KHỞI TẠO (INITIALIZE)
    // ==================================
    public void initialize() {
        khachHangDAO = new KhachHangDAO();
        hoaDonDAO = new HoaDonDAO();

        setupTableColumns();
        loadDatabaseData();
        setupFiltersAndSearch();
        setupActionButtons();
        setupSelectionListener();
        clearForm();
        
        tblKhachHang.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) setupKeyboardShortcuts();
        });
    }

    // ==================================
    // CÀI ĐẶT GIAO DIỆN
    // ==================================

    private void setupFiltersAndSearch() {
        filterComboBox.setItems(FXCollections.observableArrayList(
            "Tất cả", "Member", "Gold", "Diamond", "Guest"
        ));
        filterComboBox.setValue("Tất cả");

        filterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> updateFilter());
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> updateFilter());
        btnTim.setOnAction(e -> updateFilter());
    }

    private void setupActionButtons() {
        btnThem.setOnAction(e -> handleThem());
        btnSua.setOnAction(e -> handleSua());
        btnXoa.setOnAction(e -> handleXoa());
        btnXoaTrang.setOnAction(e -> clearForm());
    }

    private void setupSelectionListener() {
        tblKhachHang.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                txtHoTen.setText(newSelection.getTenKH());
                txtSDT.setText(newSelection.getSoDT());
                txtDiaChi.setText(newSelection.getDiaChi());
                txtEmail.setText(newSelection.getEmail());
                datePickerNgayDangKy.setValue(newSelection.getNgayDangKy());
            } else {
                clearForm();
            }
        });
    }

    private void setupTableColumns() {
        tblKhachHang.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        colMaKH.setCellValueFactory(new PropertyValueFactory<>("maKH"));
        colHoTen.setCellValueFactory(new PropertyValueFactory<>("tenKH"));
        colSDT.setCellValueFactory(new PropertyValueFactory<>("soDT"));
        colDiaChi.setCellValueFactory(new PropertyValueFactory<>("diaChi"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colLoaiKH.setCellValueFactory(new PropertyValueFactory<>("thanhVien"));

        colNgayDangKy.setCellValueFactory(cellData -> {
            LocalDate date = cellData.getValue().getNgayDangKy();
            String formattedDate = (date != null) ? date.format(dateFormatter) : "";
            return new SimpleStringProperty(formattedDate);
        });

        colTongTien.setCellValueFactory(cellData -> new SimpleStringProperty("N/A"));
        colTongTien.setVisible(false);

        colXemLichSu.setCellFactory(param -> new TableCell<entity.KhachHang, Void>() {
            private final Button viewButton = new Button("Xem");
            private final HBox pane = new HBox(viewButton);
            {
                viewButton.getStyleClass().add("view-button");
                pane.setAlignment(Pos.CENTER);
                viewButton.setOnAction(event -> {
                    entity.KhachHang customer = getTableView().getItems().get(getIndex());
                    if (customer != null && customer.getMaKH() != null) {
                        showInvoiceHistoryDialog(customer);
                    } else {
                        showAlert(Alert.AlertType.WARNING, "Lỗi", "Không thể lấy thông tin khách hàng.");
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    // ==================================
    // XỬ LÝ DỮ LIỆU VÀ SỰ KIỆN
    // ==================================

    private void loadDatabaseData() {
        try {
            masterCustomerList = khachHangDAO.getAllKhachHang();
            if (masterCustomerList == null) {
                 masterCustomerList = FXCollections.observableArrayList();
                 showAlert(Alert.AlertType.ERROR, "Lỗi dữ liệu", "Không thể tải danh sách khách hàng từ CSDL.");
            }
        } catch (Exception e) {
             masterCustomerList = FXCollections.observableArrayList();
             showAlert(Alert.AlertType.ERROR, "Lỗi tải dữ liệu", "Đã xảy ra lỗi: " + e.getMessage());
             e.printStackTrace();
        }
        filteredCustomerList = new FilteredList<>(masterCustomerList, p -> true);
        tblKhachHang.setItems(filteredCustomerList);
        updateFilter();
        System.out.println("LOG: Đã tải " + masterCustomerList.size() + " khách hàng.");
    }

    private void updateFilter() {
        String selectedTier = filterComboBox.getValue();
        String searchText = txtSearch.getText() != null ? txtSearch.getText().toLowerCase().trim() : "";

        if (filteredCustomerList == null) return;

        filteredCustomerList.setPredicate(customer -> {
            boolean tierMatch = selectedTier == null || selectedTier.equals("Tất cả") ||
                                (customer.getThanhVien() != null && customer.getThanhVien().equalsIgnoreCase(selectedTier));

            boolean searchMatch = searchText.isEmpty() ||
                                  (customer.getSoDT() != null && customer.getSoDT().toLowerCase().contains(searchText)) ||
                                  (customer.getTenKH() != null && customer.getTenKH().toLowerCase().contains(searchText));

            return tierMatch && searchMatch;
        });

        if (filteredCustomerList.isEmpty()) {
            clearForm();
        } else {
             entity.KhachHang currentSelection = tblKhachHang.getSelectionModel().getSelectedItem();
            if (currentSelection == null || !filteredCustomerList.contains(currentSelection)) {
                 tblKhachHang.getSelectionModel().selectFirst();
            }
        }
    }

    private void handleThem() {
        String hoTen = txtHoTen.getText().trim(); 
        String sdt = txtSDT.getText().trim();
        String diaChi = txtDiaChi.getText().trim(); 
        String email = txtEmail.getText().trim();
        LocalDate ngayDangKy = datePickerNgayDangKy.getValue();

        if (hoTen.isEmpty() || sdt.isEmpty() || ngayDangKy == null) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng nhập Họ tên, SĐT và Ngày đăng ký.");
            return;
        }

        String newId = khachHangDAO.getNewMaKH();
        String loaiKH = "Member";

        entity.KhachHang newCustomer = new entity.KhachHang(newId, hoTen, sdt, email, ngayDangKy, diaChi, loaiKH);

        if (khachHangDAO.themKhachHang(newCustomer)) {
            masterCustomerList.add(newCustomer);
            tblKhachHang.getSelectionModel().select(newCustomer);
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã thêm khách hàng mới.");
        } else {
            showAlert(Alert.AlertType.ERROR, "Lỗi CSDL", "Thêm khách hàng thất bại.");
        }
    }

    private void handleSua() {
        entity.KhachHang selectedCustomer = tblKhachHang.getSelectionModel().getSelectedItem();
        if (selectedCustomer == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn", "Vui lòng chọn khách hàng cần sửa.");
            return;
        }

        String hoTen = txtHoTen.getText().trim(); 
        String sdt = txtSDT.getText().trim();
        String diaChi = txtDiaChi.getText().trim(); 
        String email = txtEmail.getText().trim();
        LocalDate ngayDangKy = datePickerNgayDangKy.getValue();

        if (hoTen.isEmpty() || sdt.isEmpty() || ngayDangKy == null) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng nhập Họ tên, SĐT và Ngày đăng ký.");
            return;
        }

        selectedCustomer.setTenKH(hoTen);
        selectedCustomer.setSoDT(sdt);
        selectedCustomer.setDiaChi(diaChi);
        selectedCustomer.setEmail(email);
        selectedCustomer.setNgayDangKy(ngayDangKy);

        if (khachHangDAO.suaKhachHang(selectedCustomer)) {
            tblKhachHang.refresh();
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã cập nhật thông tin khách hàng.");
        } else {
            showAlert(Alert.AlertType.ERROR, "Lỗi CSDL", "Cập nhật thất bại.");
        }
    }

    private void handleXoa() {
        // ✅ Ngăn chặn xóa trùng lặp
        if (isDeletingInProgress) {
            System.out.println("⚠️ Đang xử lý xóa, bỏ qua yêu cầu trùng lặp");
            return;
        }
        
        entity.KhachHang selectedCustomer = tblKhachHang.getSelectionModel().getSelectedItem();
        if (selectedCustomer == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn", "Vui lòng chọn khách hàng cần xóa.");
            return;
        }

        isDeletingInProgress = true; // ✅ Đánh dấu đang xóa
        
        try {
            // ✅ Lưu thông tin để debug
            String maKH = selectedCustomer.getMaKH();
            String tenKH = selectedCustomer.getTenKH();
            System.out.println("🗑️ Chuẩn bị xóa: " + maKH + " - " + tenKH);
            
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Bạn có chắc chắn muốn xóa khách hàng '" + tenKH + "' (Mã: " + maKH + ") không?",
                    ButtonType.YES, ButtonType.NO);
            confirm.setTitle("Xác nhận xóa");
            confirm.setHeaderText(null);
            Optional<ButtonType> result = confirm.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.YES) {
                System.out.println("✅ Người dùng xác nhận xóa, đang gọi DAO...");
                
                // ✅ Gọi DAO để xóa trong CSDL
                boolean isDeleted = khachHangDAO.xoaKhachHang(maKH);
                
                System.out.println("📊 Kết quả từ DAO: " + (isDeleted ? "THÀNH CÔNG" : "THẤT BẠI"));
                
                if (isDeleted) {
                    // ✅ CHỈ xóa khỏi danh sách KHI DAO trả về true
                    masterCustomerList.remove(selectedCustomer);
                    clearForm();
                    System.out.println("✅ Đã xóa khỏi ObservableList");
                    showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã xóa khách hàng " + tenKH + ".");
                } else {
                    // ✅ KHÔNG xóa khỏi danh sách nếu DAO thất bại
                    System.err.println("❌ DAO trả về false, KHÔNG xóa khỏi danh sách");
                    showAlert(Alert.AlertType.ERROR, "Lỗi CSDL", 
                        "Xóa thất bại. Khách hàng có thể đang có hóa đơn liên quan hoặc lỗi khác.\n" +
                        "Dữ liệu trên giao diện KHÔNG thay đổi.");
                }
            } else {
                System.out.println("❌ Người dùng hủy xóa");
            }
        } catch (Exception e) {
            System.err.println("❌ Lỗi ngoại lệ khi xóa: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi hệ thống", "Đã xảy ra lỗi: " + e.getMessage());
        } finally {
            isDeletingInProgress = false; // ✅ Reset flag
            System.out.println("🔓 Reset flag isDeletingInProgress");
        }
    }

    private void clearForm() {
        txtHoTen.clear(); 
        txtSDT.clear(); 
        txtDiaChi.clear(); 
        txtEmail.clear();
        datePickerNgayDangKy.setValue(null);
        tblKhachHang.getSelectionModel().clearSelection();
        txtHoTen.requestFocus();
    }

    private void showInvoiceHistoryDialog(entity.KhachHang customer) {
        List<HoaDon> hoaDonList = hoaDonDAO.getHoaDonByMaKH(customer.getMaKH());
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Lịch sử hóa đơn - " + customer.getTenKH());
        dialog.setHeaderText("Danh sách hóa đơn của: " + customer.getTenKH() + " (SĐT: " + customer.getSoDT() + ")");

        TableView<HoaDon> historyTable = new TableView<>();
        historyTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<HoaDon, String> colHistMaHD = new TableColumn<>("Mã HĐ");
        colHistMaHD.setCellValueFactory(new PropertyValueFactory<>("maHD")); 
        colHistMaHD.setPrefWidth(80);
        
        TableColumn<HoaDon, String> colHistNgayLap = new TableColumn<>("Ngày Lập");
        colHistNgayLap.setCellValueFactory(cellData -> { 
            LocalDateTime dt = cellData.getValue().getNgayLap(); 
            return new SimpleStringProperty(dt != null ? dt.format(dateTimeFormatter) : "N/A"); 
        });
        colHistNgayLap.setPrefWidth(150);
        
        TableColumn<HoaDon, String> colHistPTTT = new TableColumn<>("PTTT");
        colHistPTTT.setCellValueFactory(cellData -> { 
            PTTThanhToan pt = cellData.getValue().getHinhThucTT(); 
            return new SimpleStringProperty(pt != null ? pt.getDisplayName() : "Chưa TT"); 
        });
        colHistPTTT.setPrefWidth(100);
        
        TableColumn<HoaDon, Double> colHistTongTien = new TableColumn<>("Tổng Tiền");
        colHistTongTien.setCellValueFactory(new PropertyValueFactory<>("tongTienThanhToan"));
        colHistTongTien.setCellFactory(tc -> new TableCell<>() { 
            @Override 
            protected void updateItem(Double item, boolean empty) { 
                super.updateItem(item, empty); 
                setText(empty || item == null ? null : String.format("%,.0f Đ", item)); 
                setAlignment(Pos.CENTER_RIGHT); 
            } 
        });
        colHistTongTien.setPrefWidth(120);

        historyTable.getColumns().addAll(colHistMaHD, colHistNgayLap, colHistPTTT, colHistTongTien);
        
        if (hoaDonList.isEmpty()) { 
            historyTable.setPlaceholder(new Label("Khách hàng chưa có hóa đơn.")); 
        } else { 
            historyTable.setItems(FXCollections.observableArrayList(hoaDonList)); 
        }

        historyTable.setOnMouseClicked(event -> {
            if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
                HoaDon selectedHoaDon = historyTable.getSelectionModel().getSelectedItem();
                if (selectedHoaDon != null && selectedHoaDon.getMaHD() != null) {
                    System.out.println("Double clicked on HD: " + selectedHoaDon.getMaHD());
                    showInvoiceDetailDialog(selectedHoaDon.getMaHD());
                }
            }
        });

        VBox dialogLayout = new VBox(10, historyTable);
        dialogLayout.setPadding(new Insets(10)); 
        VBox.setVgrow(historyTable, Priority.ALWAYS);
        dialog.getDialogPane().setContent(dialogLayout);
        dialog.getDialogPane().setPrefSize(550, 400);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    private void showInvoiceDetailDialog(String maHD) {
        HoaDon hoaDon = hoaDonDAO.getHoaDonChiTietByMaHD(maHD);
        if (hoaDon == null) { 
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không tìm thấy hóa đơn " + maHD); 
            return; 
        }
        List<ChiTietHoaDon> chiTietList = hoaDonDAO.getChiTietHoaDon(maHD);

        Dialog<Void> detailDialog = new Dialog<>();
        detailDialog.setTitle("Chi tiết hóa đơn - " + maHD);
        detailDialog.setHeaderText(null);

        VBox mainLayout = new VBox(15);
        mainLayout.setPadding(new Insets(15));
        
        String cssPath = getClass().getResource("/css/HoaDon.css") != null ? getClass().getResource("/css/HoaDon.css").toExternalForm() : null;
        if (cssPath != null) {
            detailDialog.getDialogPane().getStylesheets().add(cssPath);
            mainLayout.getStyleClass().add("right-panel");
        } else {
            System.err.println("Không tìm thấy file HoaDon.css");
            mainLayout.setStyle("-fx-background-color: #FFF3E0; -fx-background-radius: 10;");
        }

        Label title = new Label("CHI TIẾT HÓA ĐƠN " + maHD);
        title.getStyleClass().add("screen-title");
        title.setMaxWidth(Double.MAX_VALUE);
        title.setAlignment(Pos.CENTER);

        GridPane infoGrid = new GridPane(); 
        infoGrid.setHgap(10); 
        infoGrid.setVgap(8);
        
        ColumnConstraints col1=new ColumnConstraints(); 
        col1.setHgrow(Priority.NEVER); 
        ColumnConstraints col2=new ColumnConstraints(); 
        col2.setHgrow(Priority.ALWAYS);
        ColumnConstraints col3=new ColumnConstraints(); 
        col3.setHgrow(Priority.NEVER); 
        ColumnConstraints col4=new ColumnConstraints(); 
        col4.setHgrow(Priority.ALWAYS);
        infoGrid.getColumnConstraints().addAll(col1, col2, col3, col4);

        Label titleLabel; 
        Label valueLabel; 
        int rowIndex = 0;
        
        titleLabel = new Label("Ngày:"); 
        titleLabel.getStyleClass().add("info-title");
        valueLabel = new Label(hoaDon.getNgayLap() != null ? hoaDon.getNgayLap().format(dateFormatter) : "N/A");
        valueLabel.getStyleClass().add("info-value"); 
        infoGrid.add(titleLabel, 0, rowIndex); 
        infoGrid.add(valueLabel, 1, rowIndex);
        
        titleLabel = new Label("SĐT Khách:"); 
        titleLabel.getStyleClass().add("info-title");
        valueLabel = new Label(hoaDon.getSoDienThoaiKH() != null ? hoaDon.getSoDienThoaiKH() : "N/A");
        valueLabel.getStyleClass().add("info-value"); 
        infoGrid.add(titleLabel, 2, rowIndex); 
        infoGrid.add(valueLabel, 3, rowIndex);
        rowIndex++;
        
        titleLabel = new Label("Bàn:"); 
        titleLabel.getStyleClass().add("info-title");
        valueLabel = new Label(hoaDon.getMaBan() != null ? hoaDon.getMaBan() : "N/A");
        valueLabel.getStyleClass().add("info-value"); 
        infoGrid.add(titleLabel, 0, rowIndex); 
        infoGrid.add(valueLabel, 1, rowIndex);
        
        titleLabel = new Label("Thu ngân:"); 
        titleLabel.getStyleClass().add("info-title");
        valueLabel = new Label(hoaDon.getTenNhanVien() != null ? hoaDon.getTenNhanVien() : "N/A");
        valueLabel.getStyleClass().add("info-value"); 
        infoGrid.add(titleLabel, 2, rowIndex); 
        infoGrid.add(valueLabel, 3, rowIndex);
        rowIndex++;
        
        titleLabel = new Label("Giờ vào:"); 
        titleLabel.getStyleClass().add("info-title");
        valueLabel = new Label(hoaDon.getGioVao() != null ? hoaDon.getGioVao().toLocalTime().format(timeFormatter) : "N/A");
        valueLabel.getStyleClass().add("info-value"); 
        infoGrid.add(titleLabel, 0, rowIndex); 
        infoGrid.add(valueLabel, 1, rowIndex);
        
        titleLabel = new Label("Giờ ra:"); 
        titleLabel.getStyleClass().add("info-title");
        valueLabel = new Label(hoaDon.getGioRa() != null ? hoaDon.getGioRa().toLocalTime().format(timeFormatter) : "N/A");
        valueLabel.getStyleClass().add("info-value"); 
        infoGrid.add(titleLabel, 2, rowIndex); 
        infoGrid.add(valueLabel, 3, rowIndex);

        TableView<ChiTietHoaDon> detailTable = new TableView<>();
        detailTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); 
        detailTable.setPrefHeight(180);
        
        TableColumn<ChiTietHoaDon, Void> colSTT = new TableColumn<>("STT"); 
        colSTT.setPrefWidth(40); 
        colSTT.setSortable(false); 
        colSTT.setCellFactory(col -> new TableCell<>() { 
            @Override 
            public void updateIndex(int index) { 
                super.updateIndex(index); 
                setText(isEmpty() || index < 0 ? null : Integer.toString(index + 1)); 
            } 
        });
        
        TableColumn<ChiTietHoaDon, String> colTenMon = new TableColumn<>("Tên món"); 
        colTenMon.setCellValueFactory(new PropertyValueFactory<>("tenMon")); 
        colTenMon.setPrefWidth(150);
        
        TableColumn<ChiTietHoaDon, Integer> colSoLuong = new TableColumn<>("Số lượng"); 
        colSoLuong.setCellValueFactory(new PropertyValueFactory<>("soLuong")); 
        colSoLuong.setPrefWidth(70);
        
        TableColumn<ChiTietHoaDon, Double> colDonGia = new TableColumn<>("Đơn giá"); 
        colDonGia.setCellValueFactory(new PropertyValueFactory<>("donGia")); 
        colDonGia.setPrefWidth(90); 
        colDonGia.setCellFactory(column -> new TableCell<>() { 
            @Override 
            protected void updateItem(Double item, boolean empty) { 
                super.updateItem(item, empty); 
                setText(empty || item == null ? null : String.format("%,.0f", item)); 
                setAlignment(Pos.CENTER_RIGHT); 
            } 
        });
        
        TableColumn<ChiTietHoaDon, Double> colThanhTien = new TableColumn<>("Thành tiền"); 
        colThanhTien.setCellValueFactory(new PropertyValueFactory<>("thanhTien")); 
        colThanhTien.setPrefWidth(100); 
        colThanhTien.setCellFactory(column -> new TableCell<>() { 
            @Override 
            protected void updateItem(Double item, boolean empty) { 
                super.updateItem(item, empty); 
                setText(empty || item == null ? null : String.format("%,.0f", item)); 
                setAlignment(Pos.CENTER_RIGHT); 
            } 
        });
        
        detailTable.getColumns().addAll(colSTT, colTenMon, colSoLuong, colDonGia, colThanhTien);
        detailTable.setItems(FXCollections.observableArrayList(chiTietList));
        if(chiTietList.isEmpty()) detailTable.setPlaceholder(new Label("Không có chi tiết món ăn."));

        VBox totalBox = new VBox(5); 
        totalBox.setPadding(new Insets(5, 0, 5, 0));
        double tongMonAn = hoaDon.getTongCongMonAn(); 
        double phiDichVu = tongMonAn * 0.05; 
        double thueVAT = tongMonAn * 0.08; 
        double khuyenMai = 0.0;
        totalBox.getChildren().addAll(
            createTotalRow("Tổng cộng món ăn:", String.format("%,.0f VNĐ", tongMonAn)), 
            createTotalRow("Phí dịch vụ (5%):", String.format("%,.0f VNĐ", phiDichVu)), 
            createTotalRow("Thuế VAT (8%):", String.format("%,.0f VNĐ", thueVAT)), 
            createTotalRow("Tiền đặt cọc bàn:", String.format("%,.0f VNĐ", hoaDon.getTienCoc())), 
            createTotalRow("Khuyến mãi:", String.format("%,.0f VNĐ", khuyenMai))
        );

        HBox finalTotalBox = new HBox(); 
        finalTotalBox.setAlignment(Pos.CENTER_RIGHT);
        Label finalTotalLabel = new Label("Tổng tiền thanh toán:"); 
        finalTotalLabel.getStyleClass().add("total-title");
        Label finalTotalValue = new Label(String.format("%,.0f VNĐ", hoaDon.getTongTienThanhToan())); 
        finalTotalValue.getStyleClass().add("total-value");
        Region spacer = new Region(); 
        HBox.setHgrow(spacer, Priority.ALWAYS);
        finalTotalBox.getChildren().addAll(finalTotalLabel, spacer, finalTotalValue);

        mainLayout.getChildren().addAll(title, infoGrid, detailTable, totalBox, finalTotalBox);

        detailDialog.getDialogPane().setContent(mainLayout);
        detailDialog.getDialogPane().setPrefWidth(550);
        detailDialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        detailDialog.getDialogPane().setStyle("-fx-background-color: transparent;");

        detailDialog.showAndWait();
    }

    private HBox createTotalRow(String labelText, String valueText) {
        HBox hbox = new HBox();
        Label label = new Label(labelText); 
        label.getStyleClass().add("info-title");
        Label value = new Label(valueText); 
        value.getStyleClass().add("info-value"); 
        value.setAlignment(Pos.CENTER_RIGHT);
        Region spacer = new Region(); 
        HBox.setHgrow(spacer, Priority.ALWAYS);
        hbox.getChildren().addAll(label, spacer, value);
        return hbox;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title); 
        alert.setHeaderText(null); 
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void setupKeyboardShortcuts() {
        Scene scene = tblKhachHang.getScene();
        if (scene == null) return;

        // ✅ Gỡ handler cũ nếu có
        if (khShortcutHandler != null) {
            scene.removeEventFilter(KeyEvent.KEY_PRESSED, khShortcutHandler);
        }

        khShortcutHandler = event -> {
            // ✅ KIỂM TRA NGHIÊM NGẶT: Chỉ xử lý khi TableView đang hiển thị VÀ trong Scene graph
            if (!tblKhachHang.isVisible() || tblKhachHang.getScene() == null || tblKhachHang.getParent() == null) {
                System.out.println("⛔ Bỏ qua phím tắt Khách Hàng - Màn hình không active");
                return;
            }

            // ✅ QUAN TRỌNG: Consume event NGAY khi phát hiện phím tắt hợp lệ
            boolean handled = false;

            if (event.isControlDown()) {
                switch (event.getCode()) {
                    case N: // Thêm mới
                        btnThem.fire();
                        handled = true;
                        break;
                        
                    case E: // Edit/Sửa
                    case S: // Save/Sửa (trùng với Edit)
                        btnSua.fire();
                        handled = true;
                        break;
                        
                    case D: // Delete/Xóa
                        // ✅ Kiểm tra TRƯỚC KHI gọi fire() để tránh duplicate
                        entity.KhachHang selected = tblKhachHang.getSelectionModel().getSelectedItem();
                        if (selected != null) {
                            btnXoa.fire();
                            handled = true;
                        }
                        break;
                        
                    case F: // Focus vào tìm kiếm
                        txtSearch.requestFocus();
                        txtSearch.selectAll();
                        handled = true;
                        break;
                        
                    case R: // Reset/Xóa trắng
                        btnXoaTrang.fire();
                        handled = true;
                        break;
                }
            } else if (event.getCode() == KeyCode.F5) {
                loadDatabaseData();
                handled = true;
            } else if (event.getCode() == KeyCode.ENTER) {
                if (txtSearch.isFocused()) {
                    updateFilter();
                    handled = true;
                } else {
                    entity.KhachHang kh = tblKhachHang.getSelectionModel().getSelectedItem();
                    if (kh != null) {
                        showInvoiceHistoryDialog(kh);
                        handled = true;
                    }
                }
            }

            // ✅ Consume event SAU KHI xử lý xong
            if (handled) {
                event.consume();
            }
        };

        scene.addEventFilter(KeyEvent.KEY_PRESSED, khShortcutHandler);
        System.out.println("✅ Đã kích hoạt phím tắt Khách Hàng");

        // ✅ GỠ BỎ KHI TRANG BỊ ẨN HOẶC REMOVED KHỎI SCENE
        tblKhachHang.visibleProperty().addListener((obs, oldVal, isNowVisible) -> {
            if (!isNowVisible && khShortcutHandler != null) {
                Scene s = tblKhachHang.getScene();
                if (s != null) {
                    s.removeEventFilter(KeyEvent.KEY_PRESSED, khShortcutHandler);
                    System.out.println("⛔ Đã GỠ BỎ phím tắt Khách Hàng (visible = false)");
                }
            }
        });

        // ✅ GỠ BỎ KHI NODE BỊ REMOVED KHỎI SCENE GRAPH
        tblKhachHang.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene == null && khShortcutHandler != null && oldScene != null) {
                oldScene.removeEventFilter(KeyEvent.KEY_PRESSED, khShortcutHandler);
                System.out.println("⛔ Đã GỠ BỎ phím tắt Khách Hàng (removed from scene)");
            }
        });

        // ✅ GỠ BỎ KHI PARENT BỊ THAY ĐỔI (Chuyển trang)
        tblKhachHang.parentProperty().addListener((obs, oldParent, newParent) -> {
            if (newParent == null && khShortcutHandler != null) {
                Scene s = tblKhachHang.getScene();
                if (s != null) {
                    s.removeEventFilter(KeyEvent.KEY_PRESSED, khShortcutHandler);
                    System.out.println("⛔ Đã GỠ BỎ phím tắt Khách Hàng (parent changed)");
                }
            }
        });
    }

}
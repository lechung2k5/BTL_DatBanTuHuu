package ui;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import dao.KhachHangDAO;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList; 
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

public class KhachHang {

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
    
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private KhachHangDAO khachHangDAO;
    
    private ObservableList<entity.KhachHang> masterCustomerList;
    private FilteredList<entity.KhachHang> filteredCustomerList;
    
    public void initialize() {
        khachHangDAO = new KhachHangDAO();
        setupTableColumns();
        loadDatabaseData(); 
        clearForm();
        
        // 1. Cài đặt ComboBox (Tất cả, Member, Gold, Diamond)
        filterComboBox.setItems(FXCollections.observableArrayList(
            "Tất cả", "Member", "Gold", "Diamond"
        ));
        filterComboBox.setValue("Tất cả"); 

        // 2. Thêm listener cho ComboBox (Vẫn lọc trực tiếp khi chọn)
        filterComboBox.valueProperty().addListener((obs, oldVal, newVal) -> updateFilter());
        
        // 4. Gán sự kiện cho Nút Tìm (Sẽ gọi hàm lọc)
        btnTim.setOnAction(e -> updateFilter());
        
        // --- Các sự kiện nút Thêm, Sửa, Xóa ---
        
        btnThem.setOnAction(e -> {
            String hoTen = txtHoTen.getText();
            String sdt = txtSDT.getText();
            String diaChi = txtDiaChi.getText();
            String email = txtEmail.getText();
            LocalDate ngayDangKy = datePickerNgayDangKy.getValue();

            if (hoTen.isEmpty() || sdt.isEmpty() || ngayDangKy == null) {
                System.out.println("LỖI: Vui lòng nhập đầy đủ Họ tên, SĐT và Ngày đăng ký.");
                return;
            }

            String newId = khachHangDAO.getNewMaKH();
            String loaiKH = "Member"; 

            // 🔥 SỬA: Sử dụng constructor 7 tham số mới của entity.KhachHang:
            // (maKH, tenKH, soDT, email, ngayDangKy, diaChi, thanhVien)
            entity.KhachHang newCustomer = new entity.KhachHang(
                newId, 
                hoTen, 
                sdt, 
                email, 
                ngayDangKy, // 🔥 Tham số 5: ngayDangKy
                diaChi, 
                loaiKH
            );
            
            if (khachHangDAO.themKhachHang(newCustomer)) {
                masterCustomerList.add(newCustomer); 
                clearForm();
            } else {
                System.out.println("LỖI: Thêm khách hàng vào CSDL thất bại.");
            }
        });

        btnSua.setOnAction(e -> {
            entity.KhachHang selectedCustomer = tblKhachHang.getSelectionModel().getSelectedItem();

            if (selectedCustomer == null) {
                System.out.println("LỖI: Vui lòng chọn một khách hàng để sửa.");
                return;
            }

            String hoTen = txtHoTen.getText();
            String sdt = txtSDT.getText();
            String diaChi = txtDiaChi.getText();
            String email = txtEmail.getText();
            LocalDate ngayDangKy = datePickerNgayDangKy.getValue();
            
             if (hoTen.isEmpty() || sdt.isEmpty() || ngayDangKy == null) {
                System.out.println("LỖI: Vui lòng nhập đầy đủ Họ tên, SĐT và Ngày đăng ký.");
                return;
            }

            selectedCustomer.setTenKH(hoTen);
            selectedCustomer.setSoDT(sdt);
            selectedCustomer.setDiaChi(diaChi);
            selectedCustomer.setEmail(email);
            
            // Cập nhật trường ngày đăng ký
            selectedCustomer.setNgayDangKy(ngayDangKy);
            
            if (khachHangDAO.suaKhachHang(selectedCustomer)) {
                tblKhachHang.refresh(); 
                clearForm();
            } else {
                 System.out.println("LỖI: Cập nhật CSDL thất bại.");
                 loadDatabaseData(); 
            }
        });

        btnXoa.setOnAction(e -> {
            entity.KhachHang selectedCustomer = tblKhachHang.getSelectionModel().getSelectedItem();
            
            if (selectedCustomer != null) {
                String maKH = selectedCustomer.getMaKH();
                if (khachHangDAO.xoaKhachHang(maKH)) {
                    masterCustomerList.remove(selectedCustomer); 
                    clearForm();
                } else {
                    System.out.println("LỖI: Xóa khỏi CSDL thất bại. (Có thể do ràng buộc khóa ngoại)");
                }
            } else {
                System.out.println("LỖI: Vui lòng chọn khách hàng để xóa.");
            }
        });

        btnXoaTrang.setOnAction(e -> clearForm());
        
        tblKhachHang.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                txtHoTen.setText(newSelection.getTenKH());
                txtSDT.setText(newSelection.getSoDT());
                txtDiaChi.setText(newSelection.getDiaChi());
                txtEmail.setText(newSelection.getEmail());
                
                // Sử dụng getNgayDangKy
                datePickerNgayDangKy.setValue(newSelection.getNgayDangKy()); 
            }
        });
    }
    
    /**
     * Hàm này được gọi bởi ComboBox (lọc live) VÀ nút Tìm (lọc SĐT)
     */
    private void updateFilter() {
        String selectedTier = filterComboBox.getValue();
        String searchText = txtSearch.getText().toLowerCase();

        filteredCustomerList.setPredicate(customer -> {
            // Điều kiện 1: Lọc theo Hạng thành viên (ComboBox)
            boolean tierMatch = false;
            if (selectedTier == null || selectedTier.equals("Tất cả")) {
                tierMatch = true;
            } else {
                // Giả định getThanhVien() trả về string khớp với "Member", "Gold", "Diamond"
                tierMatch = customer.getThanhVien().equals(selectedTier);
            }

            // Điều kiện 2: Lọc theo SĐT (Ô tìm kiếm - Tương đối)
            boolean searchMatch = false;
            if (searchText == null || searchText.isEmpty()) {
                searchMatch = true;
            } else {
                searchMatch = customer.getSoDT().toLowerCase().contains(searchText);
            }

            // Kết quả: Chỉ hiển thị nếu khớp CẢ HAI điều kiện
            return tierMatch && searchMatch;
        });
    }

    private void clearForm() {
        txtHoTen.clear();
        txtSDT.clear();
        txtDiaChi.clear();
        txtEmail.clear();
        datePickerNgayDangKy.setValue(null);
        tblKhachHang.getSelectionModel().clearSelection();
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
            // Lấy getNgayDangKy từ Entity (ngayDangKy)
            LocalDate date = cellData.getValue().getNgayDangKy();
            return new SimpleStringProperty(date != null ? date.format(formatter) : "");
        });
        
        colTongTien.setCellValueFactory(cellData -> {
            return new SimpleStringProperty("0 Đ");
        });
        
        colXemLichSu.setCellFactory(param -> new TableCell<entity.KhachHang, Void>() {
            private final Button viewButton = new Button("Xem");
            private final HBox pane = new HBox(viewButton);
            {
                viewButton.getStyleClass().add("view-button");
                pane.setAlignment(Pos.CENTER);
                viewButton.setOnAction(event -> {
                    entity.KhachHang customer = getTableView().getItems().get(getIndex());
                    System.out.println("Viewing history for: " + customer.getTenKH());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void loadDatabaseData() {
        masterCustomerList = khachHangDAO.getAllKhachHang();
        
        if (masterCustomerList != null) {
            filteredCustomerList = new FilteredList<>(masterCustomerList, p -> true);
            tblKhachHang.setItems(filteredCustomerList);
            System.out.println("Đã tải dữ liệu CSDL và cài đặt bộ lọc.");
        } else {
            System.out.println("LỖI: Không thể tải dữ liệu CSDL.");
            masterCustomerList = FXCollections.observableArrayList();
            filteredCustomerList = new FilteredList<>(masterCustomerList, p -> true);
            tblKhachHang.setItems(filteredCustomerList);
        }
    }
}
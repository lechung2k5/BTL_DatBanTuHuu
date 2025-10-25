package ui;

import dao.DatBanDAO;
import entity.Ban;
import entity.HoaDon;
import entity.TrangThaiBan;
import entity.TrangThaiHoaDon;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class DoiBanPopupController {

    @FXML private Label lblMaHDGoc;
    @FXML private Label lblSoBanHienTai;
    @FXML private Label lblDanhSachBanCu;
    @FXML private DatePicker datePickerThoiGianMoi;
    @FXML private TextField txtThoiGianMoi;
    @FXML private Button btnTimBanTrong;
    @FXML private Label lblThongTinChonBan;
    @FXML private ListView<Ban> listViewBanTrong;
    @FXML private Button btnHuy;
    @FXML private Button btnXacNhanDoi;

    private List<HoaDon> hoaDonGocVaPhu;
    private DatBanDAO datBanDAO;
    private DatBan mainController; // Tham chiếu đến Controller màn hình chính để refresh

    private ObservableList<Ban> banTrongList = FXCollections.observableArrayList();
    private Map<Ban, BooleanProperty> selectionMap = new HashMap<>(); // Map lưu trạng thái chọn
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private int soBanCanChon = 0;
    
    // === HELPER CẦN THIẾT TỪ DATBAN (ĐƯỢC GỌI QUA mainController) ===
    // Để Controller này có thể gọi các hàm phức tạp trong DatBan
    
    // (Giả định mainController có các hàm sau, nếu không, ta phải tạo interface hoặc dùng Reflection)
    // CÁCH TỐT NHẤT: Bổ sung thêm các phương thức public cần thiết vào DatBan.java
    
    // Cần có một Set các mã bàn cũ để kiểm tra
    private Set<String> maBanCuSet; 
    
    @FXML
    private void initialize() {
        listViewBanTrong.setItems(banTrongList);
        listViewBanTrong.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE); 
        
        // === KHÔI PHỤC LIST CELL SỬ DỤNG CHECKBOX VÀ BINDING VỚI selectionMap ===
        listViewBanTrong.setCellFactory(new Callback<ListView<Ban>, ListCell<Ban>>() {
            @Override
            public ListCell<Ban> call(ListView<Ban> lv) {
                ListCell<Ban> cell = new ListCell<Ban>() {
                    private final CheckBox checkBox = new CheckBox();
                    private final Label label = new Label();
                    private final HBox hbox = new HBox(5, checkBox, label);
                    
                    { 
                        hbox.setAlignment(Pos.CENTER_LEFT);
                        // Khi Checkbox được click, cập nhật trạng thái trong Map
                        checkBox.setOnAction(event -> {
                            if (getItem() != null) {
                                // Cập nhật BooleanProperty trong Map
                                selectionMap.get(getItem()).set(checkBox.isSelected());
                                updateXacNhanButtonState(); // Cập nhật trạng thái nút
                            }
                        });
                    }

                    @Override
                    protected void updateItem(Ban item, boolean empty) {
                        super.updateItem(item, empty);
                        
                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            // Lấy/Tạo BooleanProperty và cập nhật CheckBox
                            BooleanProperty selected = selectionMap.computeIfAbsent(item, k -> new SimpleBooleanProperty(false));
                            
                            // SỬA LỖI CÚ PHÁP: Dùng getDisplayName()
                            label.setText(item.getMaBan() + " (" + item.getSucChua() + " chỗ) - " + item.getTrangThai().getDisplayName());
                            
                            // Set trạng thái của Checkbox từ Map
                            checkBox.setSelected(selected.get()); 
                            
                            // === LOGIC VÔ HIỆU HÓA CHECKBOX VÀ BÀN BẬN (FIX LỖI B010) ===
                            boolean laBanCu = maBanCuSet != null && maBanCuSet.contains(item.getMaBan());
                            
                            // Bàn bị coi là BẬN nếu trạng thái logic (logic 4/8 tiếng) không phải là TRONG
                            boolean isCurrentlyBusy = item.getTrangThai() != TrangThaiBan.TRONG;
                            
                            // Chỉ cho phép chọn nếu (Nó là Bàn cũ) HOẶC (Nó hoàn toàn TRỐNG)
                            // Nếu isCurrentlyBusy=true, và nó KHÔNG phải bàn cũ -> DISABLE
                            checkBox.setDisable(isCurrentlyBusy && !laBanCu); 
                            
                            // Set màu nền (tùy chọn)
                            if (isCurrentlyBusy) {
                                setStyle("-fx-background-color: #fce4e4; -fx-opacity: 0.8;"); // Màu nhạt cho bàn bận
                            } else {
                                setStyle("");
                            }
                            
                            setGraphic(hbox);
                        }
                    } 
                };
                return cell;
            } 
        });
        // ===============================================

        // Gán sự kiện cho các nút
        btnTimBanTrong.setOnAction(e -> handleTimBanTrong()); 
        btnXacNhanDoi.setOnAction(e -> handleXacNhanDoi()); 
        btnHuy.setOnAction(e -> closePopup()); 

        btnXacNhanDoi.setDisable(true); 
    }

    /**
     * Nhận dữ liệu ban đầu từ màn hình DatBan
     */
    public void setInitialData(List<HoaDon> hoaDonGocVaPhu, DatBanDAO dao, DatBan mainController) {
        this.hoaDonGocVaPhu = hoaDonGocVaPhu;
        this.datBanDAO = dao;
        this.mainController = mainController;
        
        // Lấy mã bàn cũ một lần duy nhất
        this.maBanCuSet = hoaDonGocVaPhu.stream()
                            .filter(hd -> hd.getBan() != null)
                            .map(hd -> hd.getBan().getMaBan())
                            .collect(Collectors.toSet());

        // Hiển thị thông tin HĐ hiện tại
        HoaDon hdGoc = hoaDonGocVaPhu.stream().filter(h -> h.getMaHDGoc() == null).findFirst().orElse(null);
        if (hdGoc != null) {
            lblMaHDGoc.setText(hdGoc.getMaHD());
            soBanCanChon = hoaDonGocVaPhu.size(); // Số bàn cần chọn = số bàn hiện tại
            lblSoBanHienTai.setText(String.valueOf(soBanCanChon));
            String dsBanCu = hoaDonGocVaPhu.stream()
                                .map(h -> h.getBan() != null ? h.getBan().getMaBan() : "?")
                                .collect(Collectors.joining(", "));
            lblDanhSachBanCu.setText(dsBanCu);
            lblThongTinChonBan.setText(String.format("Chọn đúng %d bàn trống dưới đây:", soBanCanChon));

            // Đặt thời gian mặc định là giờ vào hiện tại của HĐ Gốc
            if (hdGoc.getGioVao() != null) {
                datePickerThoiGianMoi.setValue(hdGoc.getGioVao().toLocalDate());
                txtThoiGianMoi.setText(hdGoc.getGioVao().toLocalTime().format(timeFormatter));
            } else {
                 // Hoặc giờ hiện tại nếu HĐ không có giờ vào
                 datePickerThoiGianMoi.setValue(LocalDate.now());
                 txtThoiGianMoi.setText(LocalTime.now().format(timeFormatter));
            }
            // Tự động tìm bàn trống lần đầu
            handleTimBanTrong();

        } else {
            // Xử lý lỗi nếu không tìm thấy HĐ Gốc
            showAlert(AlertType.ERROR, "Lỗi Dữ liệu", "Không tìm thấy Hóa đơn Gốc trong danh sách truyền vào.");
            closePopup();
        }
    }

    /**
     * Xử lý khi nhấn nút "Tìm bàn trống"
     */
    private void handleTimBanTrong() {
        LocalDate ngayMoi = datePickerThoiGianMoi.getValue(); //
        String gioMoiStr = txtThoiGianMoi.getText(); //
        Timestamp thoiGianMoiTs; //
        LocalTime gioMoi; // <<< KHAI BÁO BIẾN CỤC BỘ

        try {
            if (ngayMoi == null || gioMoiStr == null || gioMoiStr.trim().isEmpty()) { //
                showAlert(AlertType.WARNING, "Thiếu thời gian", "Vui lòng nhập Ngày và Giờ mới.");
                return;
            }
            gioMoi = LocalTime.parse(gioMoiStr, timeFormatter); // <<< GÁN GIÁ TRỊ
            thoiGianMoiTs = Timestamp.valueOf(ngayMoi.atTime(gioMoi)); //
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi định dạng", "Giờ nhập không hợp lệ (cần HH:mm).");
            return;
        }

        // 1. Lấy TẤT CẢ bàn kèm trạng thái availability
        List<Map<String, Object>> allBanInfo = datBanDAO.getAllBanWithAvailability(thoiGianMoiTs); //

        banTrongList.clear(); //
        selectionMap.clear(); // Clear map cũ
        
        // --- 2. Lấy mã bàn cũ ---
        Set<String> maBanCuSet = hoaDonGocVaPhu.stream() //
                                  .filter(hd -> hd.getBan() != null)
                                  .map(hd -> hd.getBan().getMaBan())
                                  .collect(Collectors.toSet());
        // -----------------------
        
        // Cần tải lại ds HĐ đang chờ cho logic hiển thị trạng thái chính xác (Sử dụng HELPER MỚI)
        mainController.loadDsHoaDonDatTrongNgay(ngayMoi); 

        for (Map<String, Object> banInfo : allBanInfo) {
            Ban ban = (Ban) banInfo.get("ban"); //
            boolean isAvailableDAO = (Boolean) banInfo.get("isAvailable"); //
            
            // Xử lý trạng thái hiển thị (dùng logic màu của DatBan)
            Ban banHienThi = new Ban(ban.getMaBan(), ban.getViTri(), ban.getSucChua(), ban.getLoaiBan(), ban.getTrangThai());
            
            // Lấy trạng thái màu (ĐỎ/CAM) dựa trên logic 4/8 tiếng VÀ thời điểm tìm kiếm (Sử dụng HELPER MỚI)
            TrangThaiBan trangThaiTheoLogic48 = mainController.getTrangThaiHienThi(banHienThi, gioMoi); 
            
            // Logic ưu tiên:
            if (maBanCuSet.contains(banHienThi.getMaBan())) {
                 // Nếu là bàn cũ -> set trạng thái là TRỐNG (để cho phép chọn lại)
                 banHienThi.setTrangThai(TrangThaiBan.TRONG);
            } else if (isAvailableDAO) {
                 // Nếu là bàn mới và available -> TRỐNG
                 banHienThi.setTrangThai(TrangThaiBan.TRONG);
            } else {
                 // Nếu NOT available -> Dùng trạng thái ĐỎ/CAM từ logic 4/8 tiếng để hiển thị BẬN
                 banHienThi.setTrangThai(trangThaiTheoLogic48);
            }
            
            banTrongList.add(banHienThi); 
            
            // Khởi tạo trạng thái chọn: TỰ ĐỘNG TICK BÀN CŨ
            boolean isSelected = maBanCuSet.contains(banHienThi.getMaBan());
            BooleanProperty prop = new SimpleBooleanProperty(isSelected);
            selectionMap.put(banHienThi, prop);
        }
        
        // Cần refresh ListView để ListCellFactory cập nhật Checkbox
        listViewBanTrong.refresh(); 

        updateXacNhanButtonState(); 
        lblThongTinChonBan.setText(String.format("Chọn đúng %d bàn trống muốn đổi đến:", soBanCanChon));
    }


    /**
     * Xử lý khi nhấn nút "Xác nhận đổi"
     */
    private void handleXacNhanDoi() {
        // === LẤY DANH SÁCH BÀN ĐƯỢC CHỌN TỪ MAP (DÙNG CHECKBOX) ===
        List<Ban> selectedBanMoi = selectionMap.entrySet().stream()
                                      .filter(entry -> entry.getValue().get())
                                      .map(Map.Entry::getKey)
                                      .collect(Collectors.toList());

        if (selectedBanMoi.isEmpty()) {
            showAlert(AlertType.ERROR, "Chưa chọn bàn", "Vui lòng chọn ít nhất một bàn mới để đổi.");
            return;
        }
        // Kiểm tra bàn không được bận
        for (Ban ban : selectedBanMoi) {
             if (ban.getTrangThai() != TrangThaiBan.TRONG) {
                 showAlert(AlertType.ERROR, "Lỗi", "Bàn " + ban.getMaBan() + " đang bận. Vui lòng bỏ chọn bàn này.");
                 return;
             }
        }
        

        // Lấy lại thời gian mới
        LocalDate ngayMoi = datePickerThoiGianMoi.getValue();
        String gioMoiStr = txtThoiGianMoi.getText();
        Timestamp thoiGianDoiMoi;
         try {
            LocalTime gioMoi = LocalTime.parse(gioMoiStr, timeFormatter);
            thoiGianDoiMoi = Timestamp.valueOf(ngayMoi.atTime(gioMoi));
         } catch (Exception e) {
             showAlert(AlertType.ERROR, "Lỗi thời gian", "Thời gian mới không hợp lệ.");
             return;
         }

        Optional<ButtonType> result = showAlertConfirm("Xác nhận Đổi Bàn",
            String.format("Bạn có chắc muốn đổi %d bàn cũ sang %d bàn mới đã chọn vào lúc %s không?",
                          hoaDonGocVaPhu.size(),
                          selectedBanMoi.size(),
                          gioMoiStr));

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // 6. Lấy danh sách Bàn Cũ và Set mã bàn MỚI
                List<Ban> banCuList = hoaDonGocVaPhu.stream()
                                      .map(HoaDon::getBan)
                                      .filter(b -> b != null)
                                      .collect(Collectors.toList());
                Set<String> maBanMoiSetThucSu = selectedBanMoi.stream()
                                            .map(Ban::getMaBan)
                                            .collect(Collectors.toSet());

                System.out.println("--- Starting Change Table ---");
                System.out.println("  Old Tables: " + banCuList.stream().map(Ban::getMaBan).collect(Collectors.toList()));
                System.out.println("  New Tables Selected: " + maBanMoiSetThucSu);

                // === 7. LOGIC DAO (Sắp xếp lại) ===
                int soBanCu = hoaDonGocVaPhu.size();
                int soBanMoiThucSu = selectedBanMoi.size();
                int soLuongXuLy = Math.min(soBanCu, soBanMoiThucSu);

                // 7.1. Cập nhật HĐ hiện có (Gán bàn mới + giờ mới)
                System.out.println("  Step 7.1: Updating existing Invoices...");
                for (int i = 0; i < soLuongXuLy; i++) {
                    HoaDon hd = hoaDonGocVaPhu.get(i);
                    Ban banMoi = selectedBanMoi.get(i);
                    System.out.println("    - Updating HD " + hd.getMaHD() + " to use Table " + banMoi.getMaBan() + " at " + thoiGianDoiMoi);
                    datBanDAO.capNhatBanVaGioVaoChoHoaDon(hd.getMaHD(), banMoi.getMaBan(), thoiGianDoiMoi);
                }

                // 7.2. Tạo HĐ Phụ mới nếu cần (GIỮ NGUYÊN)
                System.out.println("  Step 7.2: Creating new Sub-Invoices if needed...");
                if (soBanMoiThucSu > soBanCu) {
                    HoaDon hdGoc = hoaDonGocVaPhu.get(0);
                    for (int i = soBanCu; i < soBanMoiThucSu; i++) {
                        Ban banMoiThem = selectedBanMoi.get(i);
                         System.out.println("    - Creating new Sub-Invoice for Table " + banMoiThem.getMaBan());
                        HoaDon hoaDonPhuMoi = new HoaDon();
                        // ... (set thông tin hoaDonPhuMoi) ...
                        hoaDonPhuMoi.setNgayLap(java.time.LocalDateTime.now());
                        hoaDonPhuMoi.setGioVao(thoiGianDoiMoi.toLocalDateTime());
                        hoaDonPhuMoi.setKhachHang(hdGoc.getKhachHang());
                        hoaDonPhuMoi.setBan(banMoiThem);
                        hoaDonPhuMoi.setTienCoc(0);
                        hoaDonPhuMoi.setMaHDGoc(hdGoc.getMaHD());
                        hoaDonPhuMoi.setTrangThai(TrangThaiHoaDon.HOA_DON_TAM.getDbValue());

                        datBanDAO.luuHoaDonVaChiTiet(hoaDonPhuMoi, FXCollections.observableArrayList());
                    }
                }

                // 7.3. Hủy HĐ Phụ cũ nếu cần (KHẮC PHỤ LỖI DÍNH BÀN: Hủy HĐ và trả bàn ngay)
                System.out.println("  Step 7.3: Cancelling old Sub-Invoices if needed (and releasing tables)...");
                if (soBanCu > soBanMoiThucSu) {
                    for (int i = soBanMoiThucSu; i < soBanCu; i++) {
                        HoaDon hdPhuCanHuy = hoaDonGocVaPhu.get(i);
                        if (hdPhuCanHuy.getMaHDGoc() != null) {
                             System.out.println("    - Cancelling old Sub-Invoice " + hdPhuCanHuy.getMaHD() + " (Table " + hdPhuCanHuy.getBan().getMaBan() + ")");
                            
                            // 1. Cập nhật trạng thái HĐ thành DA_HUY
                            datBanDAO.capNhatTrangThaiHoaDon(hdPhuCanHuy.getMaHD(), TrangThaiHoaDon.DA_HUY.getDbValue(), true); 
                            
                            // 2. Cập nhật trạng thái BÀN thành TRONG NGAY LẬP TỨC
                            if (hdPhuCanHuy.getBan() != null) {
                                datBanDAO.capNhatTrangThaiBan(hdPhuCanHuy.getBan().getMaBan(), "Trong");
                                System.out.println("    -> Table " + hdPhuCanHuy.getBan().getMaBan() + " set to 'Trong' immediately.");
                            }
                        }
                    }
                }

                // === 8. CẬP NHẬT TRẠNG THÁI BÀN MỚI (GIỮ NGUYÊN) ===
                System.out.println("  Step 8: Updating status for NEW tables...");
                 for (int i = 0; i < soBanMoiThucSu; i++) {
                     Ban banMoi = selectedBanMoi.get(i);
                     HoaDon hdTuongUng;
                     // Tìm HĐ tương ứng (hoặc HĐ Gốc nếu i=0, hoặc HĐ Phụ mới nếu i >= soBanCu)
                     if (i < soLuongXuLy) { // HĐ cũ được cập nhật
                         hdTuongUng = hoaDonGocVaPhu.get(i);
                     } else { // HĐ Phụ mới được tạo (lấy trạng thái mặc định)
                         hdTuongUng = null; // Hoặc tạo HĐ tạm để lấy trạng thái DANG_SU_DUNG
                     }

                    String trangThaiBanMoi;
                    if (hdTuongUng != null && hdTuongUng.getMaHDGoc() == null) { // Nếu là HĐ Gốc
                        TrangThaiHoaDon tt = hdTuongUng.getTrangThai();
                        if (tt != null) {
                            trangThaiBanMoi = tt.getDbValue();
                        } else {
                            trangThaiBanMoi = TrangThaiHoaDon.DANG_SU_DUNG.getDbValue();
                        }
                    } else { // HĐ Phụ (cũ hoặc mới)
                        trangThaiBanMoi = TrangThaiHoaDon.DANG_SU_DUNG.getDbValue(); // Luôn là Đang Sử Dụng
                    }
                    System.out.println("    - Setting Table " + banMoi.getMaBan() + " to status: " + trangThaiBanMoi);
                    datBanDAO.capNhatTrangThaiBan(banMoi.getMaBan(), trangThaiBanMoi);
                 }

                // === 9. TRẢ BÀN CŨ KHÔNG CÒN ĐƯỢC DÙNG (CLEANUP CÒN LẠI) ===
                System.out.println("  Step 9: Releasing old tables NOT used by the new set...");
                // Lấy tất cả mã bàn cũ
                Set<String> maBanCuSet = banCuList.stream()
                                                  .map(Ban::getMaBan)
                                                  .collect(Collectors.toSet());
                
                // Các bàn CŨ cần được trả về trạng thái 'Trong'
                Set<String> maBanCanTra = new HashSet<>(maBanCuSet);
                // Loại bỏ những bàn CŨ mà đang được TÁI SỬ DỤNG
                maBanCanTra.removeAll(maBanMoiSetThucSu);
                
                // Chỉ duyệt qua các bàn cần trả và set trạng thái (Đây là những bàn CŨ không phải HĐ Phụ bị hủy và không được tái sử dụng)
                for (String maBanTra : maBanCanTra) {
                    datBanDAO.capNhatTrangThaiBan(maBanTra, "Trong");
                }
                // =============================

                showAlert(AlertType.INFORMATION, "Thành công", "Đã đổi " + soBanMoiThucSu + " bàn thành công!");

                if (mainController != null) {
                    mainController.loadBookingCards();
                    mainController.loadTableGrids();
                    mainController.clearFormDatBan();
                }
                closePopup();
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(AlertType.ERROR, "Lỗi CSDL", "Không thể đổi bàn: " + e.getMessage());
            }
        }
    }

    /**
     * Cập nhật trạng thái enable/disable của nút Xác nhận
     */
    private void updateXacNhanButtonState() {
        // Lấy danh sách bàn đang được chọn từ Checkbox Map
        long countSelected = selectionMap.values().stream()
                                  .filter(BooleanProperty::get)
                                  .count();
        
        // Chỉ enable khi số lượng chọn BẰNG số bàn cũ
        btnXacNhanDoi.setDisable(countSelected != soBanCanChon); 
    }

    /**
     * Đóng cửa sổ Popup
     */
    private void closePopup() {
        Stage stage = (Stage) btnHuy.getScene().getWindow();
        stage.close();
    }

    // --- Hàm tiện ích ---
    private void showAlert(AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
     private Optional<ButtonType> showAlertConfirm(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        return alert.showAndWait();
    }
}
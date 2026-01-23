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
import java.util.Objects;
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

 // DoiBanPopupController.java (Chỉ phần hàm handleTimBanTrong được sửa)

    /**
     * Xử lý khi nhấn nút "Tìm bàn trống"
     */
    private void handleTimBanTrong() {
        LocalDate ngayMoi = datePickerThoiGianMoi.getValue(); 
        String gioMoiStr = txtThoiGianMoi.getText(); 
        Timestamp thoiGianMoiTs; 
        LocalTime gioMoi; 

        try {
            if (ngayMoi == null || gioMoiStr == null || gioMoiStr.trim().isEmpty()) { 
                showAlert(AlertType.WARNING, "Thiếu thời gian", "Vui lòng nhập Ngày và Giờ mới.");
                return;
            }
            gioMoi = LocalTime.parse(gioMoiStr, timeFormatter); 
            thoiGianMoiTs = Timestamp.valueOf(ngayMoi.atTime(gioMoi)); 
        } catch (Exception e) {
            showAlert(AlertType.ERROR, "Lỗi định dạng", "Giờ nhập không hợp lệ (cần HH:mm).");
            return;
        }

        // Cần tải lại ds HĐ đang chờ cho logic hiển thị trạng thái chính xác
        mainController.loadDsHoaDonDatTrongNgay(ngayMoi); 

        banTrongList.clear(); 
        selectionMap.clear(); 
        
        // --- Lấy mã bàn cũ ---
        Set<String> maBanCuSet = hoaDonGocVaPhu.stream() 
                                  .filter(hd -> hd.getBan() != null)
                                  .map(hd -> hd.getBan().getMaBan())
                                  .collect(Collectors.toSet());
        // -----------------------

        // 🔥 FIX: Lấy TẤT CẢ các bàn và kiểm tra trạng thái BẬN CỨNG (DAO đã xử lý loại trừ HĐ hiện tại)
        // GIẢ ĐỊNH: DAO.getAllBanWithAvailability ĐÃ ĐƯỢC FIX ĐỂ LOẠI TRỪ CỤM HĐ ĐANG XEM. 
        // DO KHÔNG CÓ CODE DAO ĐẦY ĐỦ Ở ĐÂY, CHÚNG TA PHẢI GỌI DAO ĐỂ LẤY TẤT CẢ BÀN
        // VÀ DÙNG LOGIC MÀU CỦA CONTROLLER CHÍNH
        
        List<Ban> tatCaBan = datBanDAO.getAllBan(); // Lấy TẤT CẢ bàn (trạng thái TRỐNG/ĐANG SỬ DỤNG)
        
        for (Ban ban : tatCaBan) {
            
            // 1. Lấy trạng thái BẬN (logic 4/8 tiếng) tại thời điểm mới
            TrangThaiBan trangThaiLogic = mainController.getTrangThaiHienThi(ban, gioMoi); 
            
            // 2. Tạo đối tượng Ban mới để hiển thị trong ListView
            Ban banHienThi = new Ban(ban.getMaBan(), ban.getViTri(), ban.getSucChua(), ban.getLoaiBan(), ban.getTrangThai());
            
            // 🔥 LOGIC MỚI: Chỉ loại trừ bàn cũ khỏi danh sách BẬN.
            if (maBanCuSet.contains(ban.getMaBan())) {
                 // Nếu là bàn cũ -> luôn set là TRỐNG để cho phép chọn (dù logic 4/8 tiếng báo ĐỎ/CAM)
                 banHienThi.setTrangThai(TrangThaiBan.TRONG);
            } else if (trangThaiLogic != TrangThaiBan.TRONG) {
                 // Nếu là bàn mới và Bận theo Logic 4/8 tiếng -> giữ trạng thái BẬN
                 banHienThi.setTrangThai(trangThaiLogic);
            } else {
                 // Ngược lại -> TRỐNG
                 banHienThi.setTrangThai(TrangThaiBan.TRONG);
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
     * 🔥 HÀM ĐÃ SỬA: Xử lý xác nhận đổi bàn (Bao gồm các trường hợp: 1-1, N-N, N-M, N-1)
     * ĐÃ TỐI ƯU: Loại bỏ vòng lặp cập nhật HĐ cũ và thay bằng các bước Cập nhật/Xóa rõ ràng.
     */
    /**
     * 🔥 HÀM ĐÃ SỬA: Xử lý xác nhận đổi bàn (Bao gồm các trường hợp: 1-1, N-N, N-M, N-1)
     * ĐÃ SỬA LỖI: Gọi hàm DAO mới để cập nhật trạng thái NHIỀU bàn cùng lúc.
     */
    private void handleXacNhanDoi() {
        // ... (phần code lấy selectedBanMoi, hoaDonGoc, maBanCuList, hoaDonPhuCuList, maBanMoiSet, maBanMoiGoc) ...
        List<Ban> selectedBanMoi = selectionMap.entrySet().stream()
                                    .filter(entry -> entry.getValue().get())
                                    .map(Map.Entry::getKey)
                                    .collect(Collectors.toList());

        if (selectedBanMoi.isEmpty()) {
            showAlert(AlertType.ERROR, "Chưa chọn bàn", "Vui lòng chọn ít nhất một bàn mới.");
            return;
        }
        // Kiểm tra bàn không được bận (chỉ kiểm tra các bàn mới không phải là bàn cũ)
        for (Ban ban : selectedBanMoi) {
             if (ban.getTrangThai() != TrangThaiBan.TRONG && !maBanCuSet.contains(ban.getMaBan())) {
                 showAlert(AlertType.ERROR, "Lỗi", "Bàn " + ban.getMaBan() + " đang bận. Vui lòng bỏ chọn bàn này.");
                 return;
             }
        }

        HoaDon hoaDonGoc = hoaDonGocVaPhu.stream().filter(h -> h.getMaHDGoc() == null).findFirst().orElse(null);
        if (hoaDonGoc == null) return; 

        // Lấy mã bàn cũ và các HĐ phụ
        List<String> maBanCuList = hoaDonGocVaPhu.stream().map(h -> h.getBan() != null ? h.getBan().getMaBan() : null)
                                            .filter(Objects::nonNull).collect(Collectors.toList());
        List<HoaDon> hoaDonPhuCuList = hoaDonGocVaPhu.stream().filter(h -> h.getMaHDGoc() != null).collect(Collectors.toList());
        Set<String> maBanMoiSet = selectedBanMoi.stream().map(Ban::getMaBan).collect(Collectors.toSet());
        
        // Bàn mới gốc là bàn đầu tiên được chọn
        String maBanMoiGoc = selectedBanMoi.get(0).getMaBan();
        String maHDGoc = hoaDonGoc.getMaHD();

        // --- Kiểm tra trùng lặp (Giữ nguyên) ---
        if (new HashSet<>(maBanCuList).equals(maBanMoiSet) && maBanCuList.size() == maBanMoiSet.size()) {
             showAlert(AlertType.WARNING, "Không đổi", "Bàn cũ và bàn mới giống nhau.");
             return;
        }
        // ----------------------------------------

        Optional<ButtonType> result = showAlertConfirm("Xác nhận đổi bàn", 
            String.format("Bạn có chắc chắn muốn đổi các bàn %s sang các bàn %s không?\n\n"
                        + "Lưu ý: Các hóa đơn phụ/bàn cũ không còn được liên kết sẽ bị hủy/giải phóng.",
                        String.join(", ", maBanCuList), String.join(", ", maBanMoiSet)));
        
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                Timestamp thoiGianDoiMoi = Timestamp.valueOf(datePickerThoiGianMoi.getValue().atTime(LocalTime.parse(txtThoiGianMoi.getText(), timeFormatter)));
                
                // 1. XÓA TẤT CẢ HÓA ĐƠN PHỤ CŨ
                List<String> maHDPhuCanXoa = hoaDonPhuCuList.stream().map(HoaDon::getMaHD).collect(Collectors.toList());
                if (!maHDPhuCanXoa.isEmpty()) {
                    datBanDAO.xoaHoaDonVaChiTiet(maHDPhuCanXoa);
                    System.out.println("LOG: Đã xóa " + maHDPhuCanXoa.size() + " Hóa đơn Phụ cũ.");
                }

                // 2. CẬP NHẬT HÓA ĐƠN GỐC
                datBanDAO.capNhatBanVaGioVaoChoHoaDon(maHDGoc, maBanMoiGoc, thoiGianDoiMoi); 
                
                // 3. TẠO HÓA ĐƠN PHỤ MỚI (Nếu có nhiều hơn 1 bàn mới)
                if (selectedBanMoi.size() > 1) {
                    for (int i = 1; i < selectedBanMoi.size(); i++) {
                        Ban banMoi = selectedBanMoi.get(i);
                        HoaDon hoaDonPhuMoi = new HoaDon();
                        hoaDonPhuMoi.setNgayLap(java.time.LocalDateTime.now());
                        hoaDonPhuMoi.setGioVao(thoiGianDoiMoi.toLocalDateTime());
                        hoaDonPhuMoi.setKhachHang(hoaDonGoc.getKhachHang());
                        hoaDonPhuMoi.setBan(banMoi);
                        hoaDonPhuMoi.setTienCoc(0);
                        hoaDonPhuMoi.setMaHDGoc(maHDGoc);
                        hoaDonPhuMoi.setTrangThai(TrangThaiHoaDon.HOA_DON_TAM.getDbValue());

                        datBanDAO.luuHoaDonVaChiTiet(hoaDonPhuMoi, FXCollections.emptyObservableList());
                    }
                    System.out.println("LOG: Đã tạo " + (selectedBanMoi.size() - 1) + " Hóa đơn Phụ mới.");
                }
                
                // 4. GIẢI PHÓNG VÀ KHÓA BÀN
                
                // 4.1. Giải phóng các bàn cũ KHÔNG còn được sử dụng
                Set<String> maBanCuKhongDuocChonLai = new HashSet<>(maBanCuList);
                maBanCuKhongDuocChonLai.removeAll(maBanMoiSet); 
                
                if (!maBanCuKhongDuocChonLai.isEmpty()) {
                    // 🔥 GỌI HÀM DAO MỚI ĐỂ CẬP NHẬT NHIỀU BÀN
                    datBanDAO.capNhatTrangThaiNhieuBan(maBanCuKhongDuocChonLai, TrangThaiBan.TRONG.getDbValue()); 
                    System.out.println("LOG: Đã giải phóng các bàn cũ: " + maBanCuKhongDuocChonLai);
                }

                // 4.2. Cập nhật trạng thái BẬN cho TẤT CẢ các bàn mới được chọn
                String trangThaiBanMoi = (hoaDonGoc.getTrangThai() == TrangThaiHoaDon.DAT) ? 
                                          TrangThaiHoaDon.DAT.getDbValue() : TrangThaiHoaDon.DANG_SU_DUNG.getDbValue();
                
                // 🔥 GỌI HÀM DAO MỚI ĐỂ CẬP NHẬT NHIỀU BÀN
                datBanDAO.capNhatTrangThaiNhieuBan(maBanMoiSet, trangThaiBanMoi); 
                System.out.println("LOG: Đã khóa " + maBanMoiSet.size() + " bàn mới với trạng thái: " + trangThaiBanMoi);

                
                // 5. KẾT THÚC
                showAlert(AlertType.INFORMATION, "Thành công", 
                          String.format("Đã đổi/gộp bàn thành công!\nTừ: %s\nSang: %s", 
                                        String.join(", ", maBanCuList), String.join(", ", maBanMoiSet)));

                if (mainController != null) {
                    mainController.loadTableGrids();
                    mainController.loadBookingCards();
                    HoaDon hdMoi = datBanDAO.getHoaDonByMaHD(maHDGoc);
                    mainController.loadHoaDonToMainInterface(hdMoi); 
                }
                closePopup();
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(AlertType.ERROR, "Lỗi CSDL", "Không thể đổi/gộp bàn: " + e.getMessage());
            }
        }
    }
    /**
     * Cập nhật trạng thái enable/disable của nút Xác nhận
     * 🔥 ĐÃ SỬA: CHỈ CẦN CHỌN ÍT NHẤT 1 BÀN (cho phép gộp bàn)
     */
    private void updateXacNhanButtonState() {
        // Lấy danh sách bàn đang được chọn từ Checkbox Map
        long countSelected = selectionMap.values().stream()
                                  .filter(BooleanProperty::get)
                                  .count();
        
        // Chỉ enable khi số lượng chọn LỚN HƠN 0
        btnXacNhanDoi.setDisable(countSelected == 0); 
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
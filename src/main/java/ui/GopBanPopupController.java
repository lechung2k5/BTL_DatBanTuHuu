package ui;

import dao.DatBanDAO;
import entity.HoaDon;
import entity.TrangThaiBan;
import entity.TrangThaiHoaDon;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class GopBanPopupController implements Initializable {

    @FXML private Label lblHDMaster;
    @FXML private ListView<HoaDon> listViewHDCanGop;
    @FXML private Button btnHuy;
    @FXML private Button btnXacNhanGop;

    private HoaDon hoaDonMaster;
    private DatBanDAO datBanDAO;
    private DatBan mainController;

    private ObservableList<HoaDon> hdCanGopList = FXCollections.observableArrayList();
    private Map<HoaDon, BooleanProperty> selectionMap = new HashMap<>(); // Map lưu trạng thái chọn

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        listViewHDCanGop.setItems(hdCanGopList);
        // Bỏ qua SelectionMode.MULTIPLE vì ta dùng Checkbox

        btnHuy.setOnAction(e -> closePopup());
        btnXacNhanGop.setOnAction(e -> handleXacNhanGop());
        btnXacNhanGop.setDisable(true); // Disable ban đầu
        
        // === FIX: Cài đặt CellFactory với Checkbox ===
        listViewHDCanGop.setCellFactory(lv -> new ListCell<HoaDon>() {
            private final CheckBox checkBox = new CheckBox();
            private final Label label = new Label();
            private final HBox hbox = new HBox(5, checkBox, label);
            
            {
                hbox.setAlignment(Pos.CENTER_LEFT);
                // Listener cho Checkbox
                checkBox.setOnAction(event -> {
                    if (getItem() != null) {
                        selectionMap.get(getItem()).set(checkBox.isSelected());
                        updateButtonState();
                    }
                });
            }

            @Override
            protected void updateItem(HoaDon hd, boolean empty) {
                super.updateItem(hd, empty);
                
                if (empty || hd == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // Lấy/Tạo BooleanProperty
                    BooleanProperty selected = selectionMap.computeIfAbsent(hd, k -> new SimpleBooleanProperty(false));
                    
                    // Cập nhật Checkbox và Label
                    checkBox.setSelected(selected.get());
                    checkBox.setDisable(false); // Luôn cho phép chọn/bỏ chọn

                    String ban = (hd.getBan() != null) ? hd.getBan().getMaBan() : "Chưa Gán Bàn";
                    String trangThai = hd.getTrangThai().getDisplayName();
                    label.setText(String.format("%s (Bàn %s, %s)", hd.getMaHD(), ban, trangThai));
                    
                    setGraphic(hbox);
                }
            }
        });
        // ===========================================
    }

    /**
     * Nhận Hóa đơn Master (HĐ được chọn từ DatBan) và tải danh sách HĐ có thể gộp.
     * 🔥 ĐÃ SỬA: Chỉ lọc Hóa đơn có trạng thái "Đang phục vụ".
     */
    public void setInitialData(HoaDon masterHD, DatBanDAO dao, DatBan parentCtrl) {
        this.hoaDonMaster = masterHD;
        this.datBanDAO = dao;
        this.mainController = parentCtrl;

        lblHDMaster.setText(masterHD.getMaHD() + " (Bàn " + masterHD.getBan().getMaBan() + ")");

        // 1. Tải TẤT CẢ HĐ đang hoạt động
        List<HoaDon> allActiveHDs = datBanDAO.getDsHoaDonDangCho();

        // 2. Lọc: Chỉ lấy HĐ đang DANG_SU_DUNG, và không phải HĐ Master
        List<HoaDon> canGop = allActiveHDs.stream()
                .filter(hd -> !hd.getMaHD().equals(masterHD.getMaHD()))
                // 🔥 SỬA DÒNG NÀY: Bỏ điều kiện || HOA_DON_TAM
                .filter(hd -> hd.getTrangThai() == TrangThaiHoaDon.DANG_SU_DUNG)
                .collect(Collectors.toList());

        hdCanGopList.setAll(canGop);

        // Khởi tạo selectionMap cho danh sách mới
        selectionMap.clear();
        for (HoaDon hd : hdCanGopList) {
            selectionMap.put(hd, new SimpleBooleanProperty(false));
        }
        updateButtonState();
    }

    private void handleXacNhanGop() {
        // Lấy danh sách Hóa đơn được chọn từ Map
        List<HoaDon> selectedHDs = selectionMap.entrySet().stream()
                                  .filter(entry -> entry.getValue().get())
                                  .map(Map.Entry::getKey)
                                  .collect(Collectors.toList());
        
        if (selectedHDs.isEmpty()) return;

        Optional<ButtonType> result = showAlertConfirm("Xác nhận Gộp", 
            String.format("Bạn có chắc chắn muốn gộp %d Hóa đơn đã chọn vào Hóa đơn %s không?", selectedHDs.size(), hoaDonMaster.getMaHD()));

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                // Duyệt qua từng HĐ nguồn và thực hiện giao dịch gộp
                for (HoaDon sourceHD : selectedHDs) {
                    // 1. Chuyển món từ HĐ nguồn sang HĐ đích (Sử dụng DAO mới)
                    datBanDAO.congDonChiTietHoaDon(hoaDonMaster.getMaHD(), sourceHD.getMaHD());

                    // 2. Xóa HĐ nguồn và giải phóng bàn nguồn
                    datBanDAO.xoaHoaDonGop(sourceHD.getMaHD(), sourceHD.getBan().getMaBan());
                }

                showAlert(AlertType.INFORMATION, "Thành công", "Đã gộp thành công " + selectedHDs.size() + " Hóa đơn vào HD " + hoaDonMaster.getMaHD());
                
                // 3. Refresh UI chính và đóng popup
                mainController.loadBookingCards();
                mainController.loadTableGrids();
                closePopup();

            } catch (SQLException e) {
                showAlert(AlertType.ERROR, "Lỗi CSDL", "Không thể hoàn tất giao dịch gộp: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Cập nhật trạng thái nút Xác nhận Gộp.
     */
    private void updateButtonState() {
        long countSelected = selectionMap.values().stream()
                                  .filter(BooleanProperty::get)
                                  .count();
        
        btnXacNhanGop.setDisable(countSelected == 0);
    }

    private void closePopup() {
        Stage stage = (Stage) btnHuy.getScene().getWindow();
        stage.close();
    }

    private void showAlert(AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private Optional<ButtonType> showAlertConfirm(String title, String content) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        return alert.showAndWait();
    }
}
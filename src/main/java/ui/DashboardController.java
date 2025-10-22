package ui;

import dao.CaTrucDAO; // 🔥 Import DAO
import entity.CaTruc;
import entity.NhanVien; // 🔥 Import NhanVien
import entity.TaiKhoan;
import javafx.application.Platform; // 🔥 Import Platform
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets; // 🔥 Import Insets
import javafx.geometry.Pos;   // 🔥 Import Pos
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.control.Tooltip;
import javafx.geometry.Point2D;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime; // 🔥 Import LocalTime
import java.time.Duration; // <--- 🔥 ĐÃ THÊM IMPORT NÀY
import java.time.format.DateTimeFormatter;
import java.util.List;      // 🔥 Import List
import java.util.Map;       // 🔥 Import Map
import java.util.stream.Collectors; // 🔥 Import Collectors

public class DashboardController {

    // Giữ nguyên các @FXML
    @FXML private GridPane weeklyCalendarGrid; // Đảm bảo fx:id khớp với FXML
    @FXML private DatePicker datePicker;
    @FXML private TextField txtSoTienKiemKe;
    @FXML private GridPane tableMapGrid;
    @FXML private Canvas hoursCanvas;
    @FXML private Canvas revenueCanvas;
    @FXML private Label hoursDetailsLabel;
    @FXML private Label revenueDetailsLabel;

    private LocalDate startOfWeek;
    private Tooltip tooltip = new Tooltip();

    // 🔥 Khởi tạo DAO
    private final CaTrucDAO caTrucDAO = new CaTrucDAO();
    // private final NhanVienDAO nhanVienDAO = new NhanVienDAO(); // Không cần thiết nếu CaTrucDAO đã lấy tên NV

    @FXML
    private void initialize() {
        startOfWeek = LocalDate.now().with(DayOfWeek.MONDAY);
        datePicker.setValue(startOfWeek);
        updateWeeklyCalendar(); // Gọi hàm cập nhật lịch
        setupTableMap();
        drawHoursChart();
        drawRevenueChart();
        setupTooltips();
        updateKpiDetails();
    }

    /**
     * 🔥 HÀM ĐÃ SỬA: Cập nhật lịch làm việc hàng tuần với dữ liệu từ CSDL
     */
    private void updateWeeklyCalendar() {
        weeklyCalendarGrid.getChildren().clear();

        // 🔥 Lấy thông tin người dùng đang đăng nhập
        TaiKhoan currentUser = MainApp.getLoggedInUser();
        if (currentUser == null || currentUser.getNhanVien() == null) {
            // Xử lý trường hợp không có người dùng đăng nhập (hiển thị lịch trống hoặc thông báo)
             Label errorLabel = new Label("Không thể tải lịch làm việc.\nVui lòng đăng nhập lại.");
             errorLabel.setStyle("-fx-text-fill: red;");
             weeklyCalendarGrid.add(errorLabel, 1, 1, 7, 3); // Hiển thị lỗi giữa grid
             // Hoặc không làm gì cả để grid trống trơn
            return; // Dừng hàm nếu không có thông tin
        }
        String loggedInMaNV = currentUser.getNhanVien().getMaNV();


        // 🔥 ĐÃ SỬA: Gọi hàm DAO mới với mã NV của người đăng nhập
        List<CaTruc> caTrucTrongTuan = caTrucDAO.layCaTrucTrongTuanCuaNV(startOfWeek, loggedInMaNV);

        String[] daysOfWeek = {"Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ nhật"};
        String[] rowHeaders = {"Ca làm", "Sáng", "Chiều", "Tối"};

        // --- Tạo Header (Giữ nguyên) ---
        LocalDate currentDayHeader = startOfWeek;
        for (int i = 0; i < daysOfWeek.length; i++) {
             Label dayLabel = new Label(daysOfWeek[i] + "\n" + currentDayHeader.format(DateTimeFormatter.ofPattern("dd/MM")));
             dayLabel.getStyleClass().add("day-of-week-label");
             dayLabel.setAlignment(Pos.CENTER);
             dayLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
             dayLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
             weeklyCalendarGrid.add(dayLabel, i + 1, 0);
             currentDayHeader = currentDayHeader.plusDays(1);
         }
         for (int i = 0; i < rowHeaders.length; i++) {
             Label timeLabel = new Label(rowHeaders[i]);
             timeLabel.getStyleClass().add(i == 0 ? "time-slot-label-header" : "time-slot-label");
             timeLabel.setAlignment(Pos.CENTER);
             timeLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
             weeklyCalendarGrid.add(timeLabel, 0, i);
         }
        // --- Kết thúc Header ---

        // --- Điền dữ liệu vào các ô (Logic hiển thị giữ nguyên) ---
        for (int col = 0; col < daysOfWeek.length; col++) {
            LocalDate dateOfCell = startOfWeek.plusDays(col);
            for (int row = 1; row < rowHeaders.length; row++) {
                String timeSlotOfCell = rowHeaders[row];
                LocalTime startTimeSlot = getTimeSlotStartTime(timeSlotOfCell);

                 List<CaTruc> shiftsInCell = caTrucTrongTuan.stream()
                        .filter(ca -> ca.getNgay().equals(dateOfCell) &&
                                      ca.getGioBatDau().equals(startTimeSlot))
                        .collect(Collectors.toList());

                VBox cellContainer = new VBox(5);
                cellContainer.setAlignment(Pos.TOP_LEFT);
                cellContainer.getStyleClass().add("day-cell");
                cellContainer.setPadding(new Insets(8));

                if (!shiftsInCell.isEmpty()) {
                    Map<String, List<CaTruc>> shiftsGroupedByTime = shiftsInCell.stream()
                            .collect(Collectors.groupingBy(ca ->
                                ca.getGioBatDau().format(DateTimeFormatter.ofPattern("HH:mm")) + " - " +
                                ca.getGioKetThuc().format(DateTimeFormatter.ofPattern("HH:mm"))
                            ));

                    // --- 🔥 PHẦN ĐÃ SỬA ĐỂ HIỂN THỊ TỔNG GIỜ ---
                    shiftsGroupedByTime.forEach((timeString, shiftsGroup) -> {
                        // Lấy 1 ca làm mẫu để tính giờ (vì đã group theo giờ BĐ-KT)
                        CaTruc mauCa = shiftsGroup.get(0);
                        LocalTime batDau = mauCa.getGioBatDau();
                        LocalTime ketThuc = mauCa.getGioKetThuc();

                        // --- Tính toán thời lượng ---
                        Duration duration = Duration.between(batDau, ketThuc);
                        // Xử lý ca qua đêm (ví dụ: 18:00 - 02:00)
                        if (ketThuc.isBefore(batDau)) {
                            duration = duration.plusHours(24);
                        }

                        long hours = duration.toHours();
                        long minutes = duration.toMinutes() % 60;

                        // Tạo chuỗi hiển thị tổng giờ
                        String durationString;
                        if (minutes == 0) {
                            durationString = String.format("Tổng: %d giờ", hours);
                        } else {
                            durationString = String.format("Tổng: %d giờ %d phút", hours, minutes);
                        }
                        // --- Kết thúc tính toán ---


                        // Tạo các Label
                        Label timeLabel = new Label(timeString);
                        timeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #1a1a1a;");
                        
                        Label durationLabel = new Label(durationString);
                        durationLabel.getStyleClass().add("shift-duration-label"); // Thêm class CSS để style

                        // Thêm vào VBox
                        VBox shiftDisplayBox = new VBox(2); // Giữ nguyên spacing
                        shiftDisplayBox.getChildren().addAll(timeLabel, durationLabel); // Thêm cả 2 label
                        cellContainer.getChildren().add(shiftDisplayBox);
                    });
                     // --- 🔥 KẾT THÚC PHẦN SỬA ---
                }
                weeklyCalendarGrid.add(cellContainer, col + 1, row);
            }
        }
    }


    // --- Các hàm tiện ích (Cần có) ---
    private LocalTime getTimeSlotStartTime(String timeSlot) {
        switch (timeSlot.toLowerCase()) {
            case "sáng": return LocalTime.of(8, 0);
            case "chiều": return LocalTime.of(14, 0);
            case "tối": return LocalTime.of(18, 0);
            default: return LocalTime.MIDNIGHT;
        }
    }
    // Hàm này có thể không cần thiết nữa
    private LocalTime getTimeSlotEndTime(String timeSlot) {
         switch (timeSlot.toLowerCase()) {
            case "sáng": return LocalTime.of(14, 0);
            case "chiều": return LocalTime.of(22, 0);
            case "tối": return LocalTime.of(2, 0); // Ca tối có thể qua ngày hôm sau
            default: return LocalTime.MAX;
        }
    }
     private void showErrorAlert(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
     private void showInfoAlert(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }


    // --- Các hàm xử lý sự kiện còn lại (giữ nguyên) ---
    @FXML private void handleCurrentWeek() {
        startOfWeek = LocalDate.now().with(DayOfWeek.MONDAY);
        datePicker.setValue(startOfWeek);
        updateWeeklyCalendar();
    }

    @FXML private void handlePreviousWeek() {
        startOfWeek = startOfWeek.minusWeeks(1);
        datePicker.setValue(startOfWeek);
        updateWeeklyCalendar();
    }

    @FXML private void handleNextWeek() {
        startOfWeek = startOfWeek.plusWeeks(1);
        datePicker.setValue(startOfWeek);
        updateWeeklyCalendar();
    }

    @FXML private void handleCheckCash() {
         showInfoAlert("Thông báo", "Chức năng kiểm kê tiền mặt đang được phát triển.");
    }

    @FXML private void updateCalendarFromDatePicker() {
        LocalDate selectedDate = datePicker.getValue();
        if (selectedDate != null) {
            startOfWeek = selectedDate.with(DayOfWeek.MONDAY);
            updateWeeklyCalendar();
        }
    }

    // --- Các hàm vẽ biểu đồ, tooltip, bàn ăn (giữ nguyên) ---
     private void setupTableMap() { /* ... Giữ nguyên code cũ ... */
        tableMapGrid.getChildren().clear();
        // Dữ liệu bàn ăn nên lấy từ CSDL (BanDAO)
        ObservableList<Table> tables = FXCollections.observableArrayList(
            new Table("Bàn 1", "Trống"), new Table("Bàn 2", "Đang phục vụ"),
            new Table("Bàn 3", "Trống"), new Table("Bàn 4", "Đã đặt"),
            new Table("Bàn 5", "Đang phục vụ")
            // ... Thêm bàn khác
        );
        int col = 0, row = 0;
        int maxCols = 3; // Số cột tối đa bạn muốn hiển thị bàn
        for (Table table : tables) {
            Hyperlink tableLink = new Hyperlink(table.name);
            tableLink.getStyleClass().add("table-link");
            // Thêm style dựa trên trạng thái (cần CSS tương ứng)
            switch (table.status.toLowerCase()) {
                case "đang phục vụ": tableLink.setStyle("-fx-background-color: #e74c3c;"); break; // Đỏ
                case "đã đặt": tableLink.setStyle("-fx-background-color: #f39c12;"); break; // Cam
                default: tableLink.setStyle("-fx-background-color: #2ecc71;"); break; // Xanh lá (Trống)
            }
            tableLink.setOnAction(e -> {
                 Alert alert = new Alert(Alert.AlertType.INFORMATION);
                 alert.setTitle("Chi tiết bàn");
                 alert.setHeaderText(table.name);
                 alert.setContentText("Trạng thái: " + table.status + "\n(Thêm logic xem chi tiết hóa đơn...)");
                 alert.showAndWait();
             });
            tableMapGrid.add(tableLink, col, row);
            col++;
            if (col >= maxCols) { // >= thay vì >
                col = 0;
                row++;
            }
        }
     }
     private static class Table { String name, status; Table(String n, String s){name=n; status=s;} }
     private void drawHoursChart() { /* ... Giữ nguyên code cũ ... */
        GraphicsContext gc = hoursCanvas.getGraphicsContext2D(); double w = hoursCanvas.getWidth(), h = hoursCanvas.getHeight(), cx=w/2, cy=h/2, or=Math.min(w,h)/2.5, ir=or*0.7; gc.clearRect(0,0,w,h); double ratio = 160.0/180.0; gc.setFill(Color.web("#e0e0e0")); gc.fillOval(cx-or,cy-or,2*or,2*or); gc.setFill(Color.web("#27ae60")); gc.fillArc(cx-ir,cy-ir,2*ir,2*ir, 90,-ratio*360, javafx.scene.shape.ArcType.ROUND);
     }
     private void drawRevenueChart() { /* ... Giữ nguyên code cũ ... */
        GraphicsContext gc = revenueCanvas.getGraphicsContext2D(); double w = revenueCanvas.getWidth(), h = revenueCanvas.getHeight(), cx=w/2, cy=h/2, or=Math.min(w,h)/2.5, ir=or*0.7; gc.clearRect(0,0,w,h); double ratio = 5000000.0/6000000.0; gc.setFill(Color.web("#e0e0e0")); gc.fillOval(cx-or,cy-or,2*or,2*or); gc.setFill(Color.web("#f39c12")); gc.fillArc(cx-ir,cy-ir,2*ir,2*ir, 90,-ratio*360, javafx.scene.shape.ArcType.ROUND);
     }
     private void setupTooltips() { /* ... Giữ nguyên code cũ ... */
        hoursCanvas.setOnMouseMoved(e -> { tooltip.setText("Số giờ làm: 160/180h"); tooltip.show(hoursCanvas, e.getScreenX()+10, e.getScreenY()+10); }); hoursCanvas.setOnMouseExited(e -> tooltip.hide());
        revenueCanvas.setOnMouseMoved(e -> { tooltip.setText("Doanh thu: 5M/6M"); tooltip.show(revenueCanvas, e.getScreenX()+10, e.getScreenY()+10); }); revenueCanvas.setOnMouseExited(e -> tooltip.hide());
     }
     private void updateKpiDetails() { /* ... Giữ nguyên code cũ ... */
        // Dữ liệu KPI này cũng nên lấy từ CSDL (tính toán doanh thu, giờ làm...)
        hoursDetailsLabel.setText("160/180 giờ");
        revenueDetailsLabel.setText("5.000.000 / 6.000.000 đ");
     }

}
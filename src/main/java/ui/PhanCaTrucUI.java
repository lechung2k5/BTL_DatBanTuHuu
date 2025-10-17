package ui;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.Scene;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class PhanCaTrucUI {

    @FXML private GridPane gridPhanCa;
    @FXML private DatePicker datePicker;

    private LocalDate startOfWeek;
    private Map<String, Shift> shiftData;  // Sử dụng Shift class để dễ mở rộng backend sau

    @FXML
    private void initialize() {
        startOfWeek = LocalDate.now().with(DayOfWeek.MONDAY);
        datePicker.setValue(startOfWeek);
        shiftData = loadShiftData();  // Phương thức riêng để load data, dễ thay bằng API call backend sau
        updateSchedule();
    }

    // Phương thức load data mẫu, sau này thay bằng call backend (ví dụ: API fetch shifts)
    private Map<String, Shift> loadShiftData() {
        Map<String, Shift> data = new HashMap<>();
        // Dữ liệu mẫu cho các ngày khác nhau
        data.put("Thứ 2-Sáng", new Shift("08:00-15:00", "7h", "TC (+6)", "Nhân viên: A, B, C\nGhi chú: Ca sáng thứ 2"));
        data.put("Thứ 3-Sáng", new Shift("08:00-15:00", "7h", "TC (+6)", "Nhân viên: D, E\nGhi chú: Ca sáng thứ 3"));
        data.put("Thứ 4-Chiều", new Shift("15:00-23:00", "8h", "TC (+5)", "Nhân viên: F, G\nGhi chú: Ca chiều thứ 4"));
        data.put("Thứ 5-Tối", new Shift("18:00-02:00", "8h", "TC (+7)", "Nhân viên: H, I, J\nGhi chú: Ca tối thứ 5"));
        data.put("Thứ 6-Sáng", new Shift("08:00-15:00", "7h", "TC (+6)", "Nhân viên: K, L\nGhi chú: Ca sáng thứ 6"));
        data.put("Thứ 7-Chiều", new Shift("15:00-23:00", "8h", "TC (+5)", "Nhân viên: M, N\nGhi chú: Ca chiều thứ 7"));
        data.put("Chủ nhật-Tối", new Shift("18:00-02:00", "8h", "TC (+7)", "Nhân viên: O, P\nGhi chú: Ca tối chủ nhật"));
        return data;
    }

    private void updateSchedule() {
        gridPhanCa.getChildren().clear();

        String[] daysOfWeek = {"Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ nhật"};
        String[] timeSlots = {"Sáng", "Chiều", "Tối"};

        // Label "Ca làm"
        Label caLamLabel = new Label("Ca làm");
        caLamLabel.getStyleClass().add("ca-lam-label");
        // Thêm căn giữa cho label này
        caLamLabel.setAlignment(javafx.geometry.Pos.CENTER);
        gridPhanCa.add(caLamLabel, 0, 0);

        LocalDate currentDay = startOfWeek;
        for (int i = 0; i < daysOfWeek.length; i++) {
            // Chèn ký tự xuống dòng (\n) để nội dung xuống hàng
            Label dayLabel = new Label(daysOfWeek[i] + "\n" + currentDay.format(DateTimeFormatter.ofPattern("dd/MM")));
            dayLabel.getStyleClass().add("day-of-week-label");
            // Căn giữa nội dung của label
            dayLabel.setAlignment(javafx.geometry.Pos.CENTER);
            gridPhanCa.add(dayLabel, i + 1, 0);
            currentDay = currentDay.plusDays(1);
        }

        for (int i = 0; i < timeSlots.length; i++) {
            Label timeLabel = new Label(timeSlots[i]);
            timeLabel.getStyleClass().add("time-slot-label");
            // Căn giữa nội dung của label
            timeLabel.setAlignment(javafx.geometry.Pos.CENTER);
            gridPhanCa.add(timeLabel, 0, i + 1);
        }
        

        for (int col = 1; col <= 7; col++) {
            for (int row = 1; row <= 3; row++) {
                String day = daysOfWeek[col - 1];
                String time = timeSlots[row - 1];
                String key = day + "-" + time;
                VBox cell = new VBox();
                cell.getStyleClass().add("day-cell");
                Shift shift = shiftData.get(key);
                if (shift != null) {
                    Label dataLabel = new Label(shift.getSummary());  // Hiển thị tóm tắt trong ô
                    dataLabel.getStyleClass().add("shift-info");
                    dataLabel.setOnMouseClicked(e -> showShiftDetails(shift));  // Click hiển thị chi tiết
                    cell.getChildren().add(dataLabel);
                } else {
                    Button registerButton = new Button("Đăng ký");
                    registerButton.getStyleClass().add("register-button");
                    registerButton.setOnAction(e -> showRegisterModal(key));  // Click hiển thị modal đăng ký
                    cell.getChildren().add(registerButton);
                }
                gridPhanCa.add(cell, col, row);
            }
        }
    }

    // Hiển thị modal chi tiết ca trực
    private void showShiftDetails(Shift shift) {
        Stage modalStage = new Stage();
        modalStage.initModality(Modality.APPLICATION_MODAL);
        VBox modalContent = new VBox(10);
        modalContent.setPadding(new Insets(20));
        modalContent.getChildren().addAll(
            new Label("Thời gian: " + shift.time),
            new Label("Số giờ: " + shift.hours),
            new Label("Mã ca: " + shift.code),
            new Label("Chi tiết: " + shift.details),
            new Button("Đóng") {{
                setOnAction(e -> modalStage.close());
            }}
        );
        modalStage.setScene(new Scene(modalContent, 300, 200));
        modalStage.setTitle("Chi tiết ca trực");
        modalStage.showAndWait();
    }

    // Hiển thị modal đăng ký cho ca trống (sau này có thể thêm form đăng ký backend)
    private void showRegisterModal(String key) {
        Stage modalStage = new Stage();
        modalStage.initModality(Modality.APPLICATION_MODAL);
        VBox modalContent = new VBox(10);
        modalContent.setPadding(new Insets(20));
        modalContent.getChildren().addAll(
            new Label("Ca: " + key),
            new Label("Ca này đang trống. Bạn muốn đăng ký?"),
            new Button("Đăng ký") {{
                setOnAction(e -> {
                    // Sau này thêm logic backend đăng ký ở đây
                    System.out.println("Đăng ký ca: " + key);
                    modalStage.close();
                });
            }},
            new Button("Đóng") {{
                setOnAction(e -> modalStage.close());
            }}
        );
        modalStage.setScene(new Scene(modalContent, 300, 150));
        modalStage.setTitle("Đăng ký ca trực");
        modalStage.showAndWait();
    }

    @FXML
    private void previousWeek() {
        startOfWeek = startOfWeek.minusWeeks(1);
        datePicker.setValue(startOfWeek);
        shiftData = loadShiftData();  // Reload data khi chuyển tuần (sau này fetch từ backend)
        updateSchedule();
    }

    @FXML
    private void nextWeek() {
        startOfWeek = startOfWeek.plusWeeks(1);
        datePicker.setValue(startOfWeek);
        shiftData = loadShiftData();  // Reload data
        updateSchedule();
    }

    @FXML
    private void handleCurrentWeek() {
        startOfWeek = LocalDate.now().with(DayOfWeek.MONDAY); // This line is correct
        datePicker.setValue(LocalDate.now()); // Set the DatePicker to today's date
        shiftData = loadShiftData();
        updateSchedule();
    }

    @FXML
    private void updateCalendarFromDatePicker() {
        LocalDate selectedDate = datePicker.getValue();
        if (selectedDate != null) {
            startOfWeek = selectedDate.with(DayOfWeek.MONDAY);
            shiftData = loadShiftData();  // Reload data
            updateSchedule();
        }
    }

    // Class Shift để lưu dữ liệu ca trực, dễ mở rộng và kết nối backend
    private static class Shift {
        String time;
        String hours;
        String code;
        String details;

        Shift(String time, String hours, String code, String details) {
            this.time = time;
            this.hours = hours;
            this.code = code;
            this.details = details;
        }

        String getSummary() {
            return time + "\n" + hours + "\n" + code;
        }
    }
}
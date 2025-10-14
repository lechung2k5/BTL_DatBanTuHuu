package ui;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

public class DashboardController {

    @FXML
    private GridPane calendarGrid;

    @FXML
    private DatePicker datePicker;

    @FXML
    private Button prevMonthButton;

    @FXML
    private Button nextMonthButton;

    private YearMonth currentMonth;

    @FXML
    private void initialize() {
        // Đặt ngày hiện tại làm giá trị mặc định
        LocalDate today = LocalDate.now(); // 14/10/2025 11:11 PM +07
        datePicker.setValue(today);
        currentMonth = YearMonth.from(today);
        updateCalendar();

        // Xử lý lỗi nếu calendarGrid không được khởi tạo
        if (calendarGrid == null || datePicker == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Lỗi khởi tạo");
            alert.setHeaderText(null);
            alert.setContentText("Không thể khởi tạo lịch từ FXML.");
            alert.showAndWait();
        }
    }

    private void updateCalendar() {
        // Xóa nội dung cũ
        calendarGrid.getChildren().clear();

        // Thêm tiêu đề các ngày trong tuần
        String[] days = {"CN", "T2", "T3", "T4", "T5", "T6", "T7"};
        for (int i = 0; i < days.length; i++) {
            Label dayLabel = new Label(days[i]);
            dayLabel.getStyleClass().add("day-label");
            calendarGrid.add(dayLabel, i, 0);
        }

        // Lấy ngày đầu tiên của tháng
        LocalDate firstOfMonth = currentMonth.atDay(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7; // Chuyển sang 0-6 (CN=0)

        // Lấy số ngày trong tháng
        int daysInMonth = currentMonth.lengthOfMonth();

        // Điền ngày vào GridPane
        int row = 1;
        int col = dayOfWeek;
        for (int day = 1; day <= daysInMonth; day++) {
            Label dayLabel = new Label(String.valueOf(day));
            dayLabel.getStyleClass().add("day-number");
            if (LocalDate.of(currentMonth.getYear(), currentMonth.getMonth(), day).isEqual(LocalDate.now())) {
                dayLabel.getStyleClass().add("current-day"); // Đánh dấu ngày hiện tại
            }
            calendarGrid.add(dayLabel, col, row);

            col++;
            if (col > 6) {
                col = 0;
                row++;
            }
        }

        // Đảm bảo GridPane có đủ hàng
        calendarGrid.setMinHeight((row + 1) * 100); // Chiều cao 100px mỗi ô
    }

    @FXML
    private void updateCalendarFromDatePicker() {
        LocalDate selectedDate = datePicker.getValue();
        if (selectedDate != null) {
            currentMonth = YearMonth.from(selectedDate);
            updateCalendar();
        }
    }

    @FXML
    private void previousMonth() {
        currentMonth = currentMonth.minusMonths(1);
        datePicker.setValue(currentMonth.atDay(1)); // Đặt ngày 1 của tháng trước
        updateCalendar();
    }

    @FXML
    private void nextMonth() {
        currentMonth = currentMonth.plusMonths(1);
        datePicker.setValue(currentMonth.atDay(1)); // Đặt ngày 1 của tháng sau
        updateCalendar();
    }
}
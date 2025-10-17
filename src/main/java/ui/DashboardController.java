package ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.control.Tooltip;
import javafx.geometry.Point2D;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class DashboardController {

    @FXML private GridPane weeklyCalendarGrid;
    @FXML private DatePicker datePicker;
    @FXML private TextField txtSoTienKiemKe;
    @FXML private Label dateRangeLabel;
    @FXML private Label endOfDayCash;
    @FXML private Label startOfDayCash;
    @FXML private GridPane tableMapGrid; // Bản đồ bàn ăn
    @FXML private Canvas hoursCanvas; // Canvas cho Số giờ làm
    @FXML private Canvas revenueCanvas; // Canvas cho Doanh thu
    @FXML private Label hoursDetailsLabel; // Label chi tiết Số giờ làm
    @FXML private Label revenueDetailsLabel; // Label chi tiết Doanh thu

    private LocalDate startOfWeek;
    private Tooltip tooltip = new Tooltip();

    @FXML
    private void initialize() {
        startOfWeek = LocalDate.now().with(DayOfWeek.MONDAY);
        datePicker.setValue(startOfWeek);
        updateWeeklyCalendar();
        setupTableMap();
        drawHoursChart();
        drawRevenueChart();
        setupTooltips();
        updateKpiDetails();
    }

    private void updateWeeklyCalendar() {
        weeklyCalendarGrid.getChildren().clear();

        // Thêm nhãn cho các ngày trong tuần
        String[] days = {"Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ nhật"};
        LocalDate currentDay = startOfWeek;

        for (int i = 0; i < days.length; i++) {
            Label dayLabel = new Label(days[i] + "\n" + currentDay.format(DateTimeFormatter.ofPattern("dd/MM")));
            dayLabel.getStyleClass().add("day-of-week-label");
            weeklyCalendarGrid.add(dayLabel, i, 0);
            VBox dayCell = new VBox();
            dayCell.getStyleClass().add("day-cell");
            Label shiftInfo = new Label("Ca: 8h-16h\nNhân viên: Nguyễn Văn A");
            shiftInfo.getStyleClass().add("shift-info");
            dayCell.getChildren().add(shiftInfo);
            weeklyCalendarGrid.add(dayCell, i, 1);
            currentDay = currentDay.plusDays(1);
        }
    }
    
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
        // Xử lý logic kiểm kê tiền mặt
    }
    
    @FXML private void updateCalendarFromDatePicker() {
        LocalDate selectedDate = datePicker.getValue();
        if (selectedDate != null) {
            startOfWeek = selectedDate.with(DayOfWeek.MONDAY);
            updateWeeklyCalendar();
        }
    }

    private void setupTableMap() {
        tableMapGrid.getChildren().clear();
        
        // Dữ liệu mẫu cho bàn ăn đang phục vụ
        ObservableList<Table> tables = FXCollections.observableArrayList(
            new Table("Bàn 1", "Đang phục vụ"),
            new Table("Bàn 4", "Đang phục vụ"),
            new Table("Bàn 4", "Đang phục vụ"),
            new Table("Bàn 4", "Đang phục vụ"),
            new Table("Bàn 4", "Đang phục vụ")
        );

        int col = 0, row = 0;
        for (Table table : tables) {
            Hyperlink tableLink = new Hyperlink(table.name);
            tableLink.getStyleClass().add("table-link");
            tableLink.setOnAction(e -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Chi tiết bàn");
                alert.setHeaderText(table.name);
                alert.setContentText("Trạng thái: " + table.status + "\nChi tiết: [Thêm logic database ở đây]");
                alert.showAndWait();
            });
            tableMapGrid.add(tableLink, col, row);
            col++;
            if (col > 2) {
                col = 0;
                row++;
            }
        }
    }

    private static class Table {
        String name, status;

        Table(String name, String status) {
            this.name = name;
            this.status = status;
        }
    }

    private void drawHoursChart() {
        GraphicsContext gc = hoursCanvas.getGraphicsContext2D();
        double width = hoursCanvas.getWidth();
        double height = hoursCanvas.getHeight();
        double centerX = width / 2;
        double centerY = height / 2;
        double outerRadius = Math.min(width, height) / 2.5;
        double innerRadius = outerRadius * 0.7;

        // Xóa canvas
        gc.clearRect(0, 0, width, height);

        // Dữ liệu mẫu cho Số giờ làm
        double hoursRatio = 160.0 / 180.0; // 88.89%

        // Vẽ hình tròn ngoài (tổng, màu xám)
        gc.setFill(Color.web("#3498db"));
        gc.fillOval(centerX - outerRadius, centerY - outerRadius, 2 * outerRadius, 2 * outerRadius);

        // Vẽ hình tròn trong (phần đạt được, màu xanh lá)
        gc.setFill(Color.web("#27ae60"));
        gc.fillArc(centerX - innerRadius, centerY - innerRadius, 2 * innerRadius, 2 * innerRadius, 0, -hoursRatio * 360, javafx.scene.shape.ArcType.ROUND);

        // Đường viền
        gc.setStroke(Color.BLACK);
        gc.strokeOval(centerX - outerRadius, centerY - outerRadius, 2 * outerRadius, 2 * outerRadius);
        gc.strokeOval(centerX - innerRadius, centerY - innerRadius, 2 * innerRadius, 2 * innerRadius);
    }

    private void drawRevenueChart() {
        GraphicsContext gc = revenueCanvas.getGraphicsContext2D();
        double width = revenueCanvas.getWidth();
        double height = revenueCanvas.getHeight();
        double centerX = width / 2;
        double centerY = height / 2;
        double outerRadius = Math.min(width, height) / 2.5;
        double innerRadius = outerRadius * 0.7;

        // Xóa canvas
        gc.clearRect(0, 0, width, height);

        // Dữ liệu mẫu cho Doanh thu
        double revenueRatio = 5000000.0 / 6000000.0; // 83.33%

        // Vẽ hình tròn ngoài (tổng, màu xám)
        gc.setFill(Color.web("#3498db"));
        gc.fillOval(centerX - outerRadius, centerY - outerRadius, 2 * outerRadius, 2 * outerRadius);

        // Vẽ hình tròn trong (phần đạt được, màu xanh lá)
        gc.setFill(Color.web("#27ae60"));
        gc.fillArc(centerX - innerRadius, centerY - innerRadius, 2 * innerRadius, 2 * innerRadius, 0, -revenueRatio * 360, javafx.scene.shape.ArcType.ROUND);

        // Đường viền
        gc.setStroke(Color.BLACK);
        gc.strokeOval(centerX - outerRadius, centerY - outerRadius, 2 * outerRadius, 2 * outerRadius);
        gc.strokeOval(centerX - innerRadius, centerY - innerRadius, 2 * innerRadius, 2 * innerRadius);
    }

    private void setupTooltips() {
        // Tooltip cho Số giờ làm
        hoursCanvas.setOnMouseMoved(e -> {
            double width = hoursCanvas.getWidth();
            double height = hoursCanvas.getHeight();
            double centerX = width / 2;
            double centerY = height / 2;
            double outerRadius = Math.min(width, height) / 2.5;
            double innerRadius = outerRadius * 0.7;
            double distance = Math.sqrt(Math.pow(e.getX() - centerX, 2) + Math.pow(e.getY() - centerY, 2));

            if (distance <= outerRadius && distance >= innerRadius) {
                tooltip.setText("Tổng: 100%");
            } else if (distance <= innerRadius) {
                tooltip.setText("Số giờ làm: 160/180h (88.89%)");
            } else {
                tooltip.setText("");
            }
            Point2D mousePoint = hoursCanvas.localToScreen(e.getX(), e.getY());
            tooltip.show(hoursCanvas, mousePoint.getX(), mousePoint.getY());
        });
        hoursCanvas.setOnMouseExited(e -> tooltip.hide());

        // Tooltip cho Doanh thu
        revenueCanvas.setOnMouseMoved(e -> {
            double width = revenueCanvas.getWidth();
            double height = revenueCanvas.getHeight();
            double centerX = width / 2;
            double centerY = height / 2;
            double outerRadius = Math.min(width, height) / 2.5;
            double innerRadius = outerRadius * 0.7;
            double distance = Math.sqrt(Math.pow(e.getX() - centerX, 2) + Math.pow(e.getY() - centerY, 2));

            if (distance <= outerRadius && distance >= innerRadius) {
                tooltip.setText("Tổng: 100%");
            } else if (distance <= innerRadius) {
                tooltip.setText("Doanh thu: 5M/6M VNĐ (83.33%)");
            } else {
                tooltip.setText("");
            }
            Point2D mousePoint = revenueCanvas.localToScreen(e.getX(), e.getY());
            tooltip.show(revenueCanvas, mousePoint.getX(), mousePoint.getY());
        });
        revenueCanvas.setOnMouseExited(e -> tooltip.hide());
    }

    private void updateKpiDetails() {
        hoursDetailsLabel.setText("Số giờ làm: 160/180h (88.89%)");
        revenueDetailsLabel.setText("Doanh thu: 5M/6M VNĐ (83.33%)");
    }
}
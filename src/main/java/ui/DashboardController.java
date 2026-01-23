package ui;

import dao.CaTrucDAO;
import dao.DatBanDAO;
import dao.HoaDonDAO;
import entity.Ban;
import entity.CaTruc;
import entity.HoaDon;
import entity.TaiKhoan;
import javafx.application.Platform;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import javafx.stage.Stage;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;

import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DashboardController {

    // --- FXML UI Components ---
    @FXML private GridPane weeklyCalendarGrid;
    @FXML private DatePicker datePicker;
    
    // Phần Kiểm kê & Tiền mặt
    @FXML private TextField txtSoTienKiemKe;
    @FXML private VBox vboxKiemKeDauCa;
    @FXML private VBox vboxTienMatTongKet;
    @FXML private Button btnXacNhanKiemKe;
    @FXML private Label lblTienMatDauCa;
    @FXML private Label lblTienMatThuDuoc;
    @FXML private Label lblTienMatDuKien;

    // Phần Thống kê & KPI
    @FXML private Canvas hoursCanvas;
    @FXML private Canvas revenueCanvas;
    @FXML private Label hoursDetailsLabel;
    @FXML private Label revenueDetailsLabel;
    
    @FXML private Label lblSoNgayNghi;
    @FXML private Label lblBanPhucVu;
    @FXML private Label lblBanDatTruoc;
    @FXML private Label lblTongGioLam;

    // Phần Header & Sơ đồ bàn
    @FXML private VBox rootDashboardVBox;
    @FXML private GridPane tableMapGrid;
    @FXML private Label lblDateTime;

    // --- Variables & DAO ---
    private LocalDate startOfWeek;
    private final CaTrucDAO caTrucDAO = new CaTrucDAO();
    private final HoaDonDAO hoaDonDAO = new HoaDonDAO();
    private final DatBanDAO datBanDAO = new DatBanDAO();
    
    private boolean daKiemKeHoacHoatDong = false;
    private Timeline tienMatPollingTimeline;

    // 🔥 LIÊN KẾT VỚI MÀN HÌNH CHÍNH
    private ManHinhChinh mainController; 

    public void setMainController(ManHinhChinh main) {
        this.mainController = main;
    }

    @FXML
    private void initialize() {
        startOfWeek = LocalDate.now().with(DayOfWeek.MONDAY);
        datePicker.setValue(startOfWeek);

        startClock(); 
        refreshData(); 
        setupTooltips();
        kiemTraVaCapNhatTrangThaiKiemKe();

        if (rootDashboardVBox != null) {
            rootDashboardVBox.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene == null) {
                    stopTienMatPolling();
                } else {
                    kiemTraVaCapNhatTrangThaiKiemKe();
                }
            });
        }
        
        // Tự động format tiền khi nhập
        txtSoTienKiemKe.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) return;
            String plainText = newVal.replaceAll("[^0-9]", "");
            try {
                long value = Long.parseLong(plainText);
                String formatted = String.format("%,d", value);
                if (!newVal.equals(formatted)) {
                    txtSoTienKiemKe.setText(formatted);
                    txtSoTienKiemKe.positionCaret(formatted.length());
                }
            } catch (NumberFormatException e) {}
        });
    }

    @FXML
    private void refreshData() {
        updateWeeklyCalendar();
        setupTableMap(); 
        updateKpiGioLam();
        updateSoNgayNghi();
        updateTheBanPhucVu();
        updateTheBanDatTruoc();
        updateTheTongGioLam();
        updateKpiDoanhThu();
        updateTienMatLabelsRealtime();
    }

    private void startClock() {
        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss - dd/MM/yyyy");
            if (lblDateTime != null) {
                lblDateTime.setText(LocalDateTime.now().format(formatter));
            }
        }));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
    }

    // ==========================================================
    // PHẦN SƠ ĐỒ BÀN THÔNG MINH (CHUYỂN TAB KHI CLICK)
    // ==========================================================

    /**
     * 🔥 HÀM SETUP TABLE MAP (FULL LOGIC): 
     * 3 Màu (Đỏ, Tím, Xanh) + Chuyển Tab DatBan khi click
     */
    private void setupTableMap() {
        tableMapGrid.getChildren().clear();

        try {
            // 1. Lấy danh sách HĐ
            List<HoaDon> listDangPhucVu = datBanDAO.getHoaDonDangPhucVu();
            List<HoaDon> listDaDat = datBanDAO.getHoaDonDaDat();
            
            // 2. Lấy toàn bộ bàn từ DB
            List<Ban> listAllBan = datBanDAO.getListBan(); 

            int col = 0, row = 0;
            int maxCols = 4;

            for (Ban ban : listAllBan) {
                String maBan = ban.getMaBan();
                
                // Tìm HĐ tương ứng
                HoaDon hdServing = listDangPhucVu.stream()
                        .filter(h -> h.getBan() != null && h.getBan().getMaBan().equals(maBan))
                        .findFirst().orElse(null);
                
                HoaDon hdBooking = listDaDat.stream()
                        .filter(h -> h.getBan() != null && h.getBan().getMaBan().equals(maBan))
                        .findFirst().orElse(null);

                Button btnBan = new Button();
                btnBan.setPrefSize(85, 55);
                btnBan.setWrapText(true);
                btnBan.setStyle("-fx-font-weight: bold; -fx-background-radius: 5; -fx-font-size: 11px;");

                // --- ƯU TIÊN 1: ĐANG PHỤC VỤ (ĐỎ) ---
                if (hdServing != null) {
                    long phutDaNgoi = 0;
                    if (hdServing.getGioVao() != null) {
                        phutDaNgoi = java.time.Duration.between(hdServing.getGioVao(), LocalDateTime.now()).toMinutes();
                    }
                    String thoiGianText = (phutDaNgoi >= 60) 
                            ? String.format("%dh %02dp", phutDaNgoi/60, phutDaNgoi%60) 
                            : String.format("%d phút", phutDaNgoi);
                            
                    btnBan.setText(maBan + "\n" + thoiGianText);
                    
                    if (phutDaNgoi > 120) {
                        btnBan.setStyle(btnBan.getStyle() + "-fx-background-color: #c0392b; -fx-text-fill: white;");
                        btnBan.setTooltip(new Tooltip("CẢNH BÁO: Khách ngồi quá 2 tiếng!"));
                    } else {
                        btnBan.setStyle(btnBan.getStyle() + "-fx-background-color: #e74c3c; -fx-text-fill: white;");
                        btnBan.setTooltip(new Tooltip("Đang phục vụ. Click để quản lý."));
                    }
                    
                    // 🔥 SỰ KIỆN CLICK: Chuyển sang tab Đặt Bàn
                    btnBan.setOnAction(e -> {
                        if (mainController != null) mainController.chuyenSangTabDatBan(hdServing);
                    });
                } 
                // --- ƯU TIÊN 2: ĐÃ ĐẶT (TÍM) --- 
                else if (hdBooking != null) {
                    String gioDen = "---";
                    if (hdBooking.getGioVao() != null) {
                        gioDen = hdBooking.getGioVao().format(DateTimeFormatter.ofPattern("HH:mm"));
                    }
                    btnBan.setText(maBan + "\n" + gioDen);
                    btnBan.setStyle(btnBan.getStyle() + "-fx-background-color: #9b59b6; -fx-text-fill: white;");
                    btnBan.setTooltip(new Tooltip("Khách đặt lúc: " + gioDen));
                    
                    // 🔥 SỰ KIỆN CLICK: Chuyển sang tab Đặt Bàn
                    btnBan.setOnAction(e -> {
                        if (mainController != null) mainController.chuyenSangTabDatBan(hdBooking);
                    });
                }
                // --- ƯU TIÊN 3: TRỐNG (XANH) ---
                else {
                    btnBan.setText(maBan);
                    btnBan.setStyle(btnBan.getStyle() + "-fx-background-color: #2ecc71; -fx-text-fill: white;");
                    btnBan.setTooltip(new Tooltip("Bàn trống"));
                    // Click bàn trống -> Có thể chuyển sang đặt bàn để tạo mới (tùy chọn)
                    btnBan.setOnAction(e -> {
                         if (mainController != null) mainController.chuyenSangTabDatBan(null); 
                    });
                }

                tableMapGrid.add(btnBan, col, row);
                GridPane.setMargin(btnBan, new Insets(4));

                col++;
                if (col >= maxCols) { col = 0; row++; }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ==========================================================
    // CÁC HÀM KHÁC (KPI, TIỀN MẶT, LỊCH...) - GIỮ NGUYÊN
    // ==========================================================

    private void setupTooltips() {
        if (hoursDetailsLabel != null) {
            Tooltip tt = new Tooltip("Chi tiết giờ làm:\nThực tế: Giờ đã làm\nMục tiêu: Tổng giờ lịch");
            tt.setShowDelay(Duration.millis(200));
            hoursDetailsLabel.setTooltip(tt);
            Tooltip.install(hoursCanvas, tt); 
        }
        if (revenueDetailsLabel != null) {
            Tooltip tt = new Tooltip("Chi tiết doanh thu:\nThực tế: Doanh thu cá nhân\nMục tiêu: 10,000,000 đ");
            tt.setShowDelay(Duration.millis(200));
            revenueDetailsLabel.setTooltip(tt);
            Tooltip.install(revenueCanvas, tt);
        }
        if (lblBanDatTruoc != null) lblBanDatTruoc.setTooltip(new Tooltip("Tổng số bàn khách đã đặt trước"));
        if (lblBanPhucVu != null) lblBanPhucVu.setTooltip(new Tooltip("Số lượng bàn đang có khách"));
    }

    private void kiemTraVaCapNhatTrangThaiKiemKe() {
        TaiKhoan currentUser = MainApp.getLoggedInUser();
        if (currentUser == null || currentUser.getNhanVien() == null) {
            toggleKiemKeView(false, false);
            return;
        }
        String maNV = currentUser.getNhanVien().getMaNV();
        double savedInitialCash = MainApp.getInitialCashCount(maNV);
        try {
            boolean hasSales = hoaDonDAO.kiemTraTienMatTrongNgay(maNV, LocalDate.now());
            if (savedInitialCash > 0 || (savedInitialCash == 0.0 && hasSales)) {
                this.daKiemKeHoacHoatDong = true;
                toggleKiemKeView(false, true); 
                updateTienMatCaHienTai();
            } else {
                this.daKiemKeHoacHoatDong = false;
                toggleKiemKeView(true, false); 
                stopTienMatPolling();
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void toggleKiemKeView(boolean showInput, boolean showSummary) {
        if (vboxKiemKeDauCa != null) {
            vboxKiemKeDauCa.setVisible(showInput);
            vboxKiemKeDauCa.setManaged(showInput);
        }
        if (vboxTienMatTongKet != null) {
            vboxTienMatTongKet.setVisible(showSummary);
            vboxTienMatTongKet.setManaged(showSummary);
        }
    }

    private void startTienMatPolling() {
        if (tienMatPollingTimeline == null) {
            tienMatPollingTimeline = new Timeline(new KeyFrame(Duration.seconds(2.0), event -> {
                Platform.runLater(this::updateTienMatLabelsRealtime);
            }));
            tienMatPollingTimeline.setCycleCount(Animation.INDEFINITE);
        }
        if (this.daKiemKeHoacHoatDong && tienMatPollingTimeline.getStatus() != Animation.Status.RUNNING) {
            tienMatPollingTimeline.play();
        }
    }

    private void stopTienMatPolling() {
        if (tienMatPollingTimeline != null) tienMatPollingTimeline.stop();
    }

    private void updateTienMatLabelsRealtime() {
        TaiKhoan currentUser = MainApp.getLoggedInUser();
        if (currentUser == null) return;
        String maNV = currentUser.getNhanVien().getMaNV();
        double dauCa = MainApp.getInitialCashCount(maNV);
        if (dauCa < 0) return;
        try {
            double tienMatThuDuoc = hoaDonDAO.getTongTienMatTrongNgay(maNV, LocalDate.now());
            double tienMatDuKien = dauCa + tienMatThuDuoc;
            if (lblTienMatThuDuoc != null) lblTienMatThuDuoc.setText(String.format("%,.0f đ", tienMatThuDuoc));
            if (lblTienMatDuKien != null) lblTienMatDuKien.setText(String.format("%,.0f đ", tienMatDuKien));
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void handleCheckCash() {
        try {
            double amount = Double.parseDouble(txtSoTienKiemKe.getText().replaceAll("[^0-9]", ""));
            TaiKhoan user = MainApp.getLoggedInUser();
            if (user != null) {
                MainApp.setInitialCashCount(amount, user.getNhanVien().getMaNV());
                this.daKiemKeHoacHoatDong = true;
                updateTienMatCaHienTai();
            }
        } catch (NumberFormatException e) { }
    }

    private void updateTienMatCaHienTai() {
        TaiKhoan currentUser = MainApp.getLoggedInUser();
        if (currentUser == null) return;
        String maNV = currentUser.getNhanVien().getMaNV();
        double dauCa = MainApp.getInitialCashCount(maNV);
        toggleKiemKeView(false, true); 
        if (lblTienMatDauCa != null) lblTienMatDauCa.setText(String.format("%,.0f đ", dauCa));
        updateTienMatLabelsRealtime();
        startTienMatPolling();
    }

    private void updateTheTongGioLam() {
         TaiKhoan currentUser = MainApp.getLoggedInUser();
         if (currentUser == null) return;
         String maNV = currentUser.getNhanVien().getMaNV();
         long totalMinutes = 0;
         try {
             List<CaTruc> list = caTrucDAO.getCaTrucTrongThang(maNV, LocalDate.now().withDayOfMonth(1), LocalDate.now());
             for (CaTruc ca : list) {
                 if (!ca.getNgay().isAfter(LocalDate.now()) && hoaDonDAO.kiemTraHoatDongNVTrongNgay(maNV, ca.getNgay())) {
                     java.time.Duration d = java.time.Duration.between(ca.getGioBatDau(), ca.getGioKetThuc());
                     if (d.isNegative()) d = d.plusHours(24);
                     totalMinutes += d.toMinutes();
                 }
             }
             if (lblTongGioLam != null) lblTongGioLam.setText((totalMinutes / 60) + " giờ");
         } catch (Exception e) { e.printStackTrace(); }
    }

    private void updateTheBanDatTruoc() {
        try {
            int booked = datBanDAO.demSoBanDatTruoc();
            int total = datBanDAO.demTongSoBan(); 
            if (total == 0) total = 20; 
            if (lblBanDatTruoc != null) {
                lblBanDatTruoc.setText(booked + "/" + total + " bàn");
                if (booked > 0) {
                	lblBanDatTruoc.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #f1c40f;");
                } else {
                    lblBanDatTruoc.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #3498db;");
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void updateTheBanPhucVu() {
        try {
            int serving = datBanDAO.getMaBanDangPhucVu().size();
            if (lblBanPhucVu != null) lblBanPhucVu.setText(serving + " bàn");
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void updateSoNgayNghi() {
        if (lblSoNgayNghi != null) lblSoNgayNghi.setText("0/5 ngày");
    }

    private void updateWeeklyCalendar() {
        weeklyCalendarGrid.getChildren().clear();
        TaiKhoan currentUser = MainApp.getLoggedInUser();
        if (currentUser == null || currentUser.getNhanVien() == null) return;
        String loggedInMaNV = currentUser.getNhanVien().getMaNV();

        List<CaTruc> caTrucTrongTuan = caTrucDAO.layCaTrucTrongTuanCuaNV(startOfWeek, loggedInMaNV);
        String[] daysOfWeek = {"Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ nhật"};
        String[] rowHeaders = {"Ca làm", "Sáng", "Chiều", "Tối"};

        LocalDate currentDayHeader = startOfWeek;
        for (int i = 0; i < daysOfWeek.length; i++) {
            String dateStr = currentDayHeader.format(DateTimeFormatter.ofPattern("dd/MM"));
            Label dayLabel = new Label(daysOfWeek[i] + "\n" + dateStr);
            dayLabel.getStyleClass().add("day-of-week-label");
            dayLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            weeklyCalendarGrid.add(dayLabel, i + 1, 0); 
            currentDayHeader = currentDayHeader.plusDays(1);
        }

        for (int i = 0; i < rowHeaders.length; i++) {
            Label timeLabel = new Label(rowHeaders[i]);
            timeLabel.getStyleClass().add(i == 0 ? "time-slot-label-header" : "time-slot-label");
            timeLabel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            weeklyCalendarGrid.add(timeLabel, 0, i);
        }

        for (int col = 0; col < daysOfWeek.length; col++) {
            LocalDate dateOfCell = startOfWeek.plusDays(col);
            for (int row = 1; row < rowHeaders.length; row++) {
                String timeSlotOfCell = rowHeaders[row];
                LocalTime startTimeSlot = getTimeSlotStartTime(timeSlotOfCell);
                List<CaTruc> shiftsInCell = caTrucTrongTuan.stream()
                        .filter(ca -> ca.getNgay().equals(dateOfCell) && ca.getGioBatDau().equals(startTimeSlot))
                        .collect(Collectors.toList());

                VBox cellContainer = new VBox(5);
                cellContainer.getStyleClass().add("day-cell"); 
                if (!shiftsInCell.isEmpty()) {
                    Map<String, List<CaTruc>> shiftsGroupedByTime = shiftsInCell.stream()
                            .collect(Collectors.groupingBy(ca -> ca.getGioBatDau().format(DateTimeFormatter.ofPattern("HH:mm")) + " - " + ca.getGioKetThuc().format(DateTimeFormatter.ofPattern("HH:mm"))));
                    shiftsGroupedByTime.forEach((timeString, shiftsGroup) -> {
                        CaTruc mauCa = shiftsGroup.get(0);
                        java.time.Duration thoiLuong = java.time.Duration.between(mauCa.getGioBatDau(), mauCa.getGioKetThuc());
                        if (thoiLuong.isNegative()) thoiLuong = thoiLuong.plusHours(24);
                        Label timeLabel = new Label(timeString);
                        timeLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
                        Label durationLabel = new Label("Tổng: " + thoiLuong.toHours() + "h");
                        durationLabel.getStyleClass().add("shift-duration-label");
                        cellContainer.getChildren().addAll(timeLabel, durationLabel);
                        cellContainer.setStyle("-fx-background-color: #dbf6e6; -fx-border-color: #27ae60;");
                    });
                }
                weeklyCalendarGrid.add(cellContainer, col + 1, row);
            }
        }
    }
    
    private LocalTime getTimeSlotStartTime(String timeSlot) {
        switch (timeSlot.toLowerCase()) {
            case "sáng": return LocalTime.of(8, 0);
            case "chiều": return LocalTime.of(14, 0);
            case "tối": return LocalTime.of(18, 0);
            default: return LocalTime.MIDNIGHT;
        }
    }

    private void updateKpiGioLam() {
        TaiKhoan currentUser = MainApp.getLoggedInUser();
        if (currentUser == null || currentUser.getNhanVien() == null) {
            drawDonutChart(hoursCanvas.getGraphicsContext2D(), 0, 1, Color.web("#2ecc71"));
            if (hoursDetailsLabel != null) hoursDetailsLabel.setText("N/A");
            return;
        }
        String maNV = currentUser.getNhanVien().getMaNV();
        LocalDate now = LocalDate.now();
        LocalDate dauThang = now.withDayOfMonth(1);
        LocalDate cuoiThang = now.withDayOfMonth(now.lengthOfMonth());
        double gioMucTieu = 0;
        double gioThucTe = 0;
        try {
             gioMucTieu = caTrucDAO.getTongGioLamTheoLich(maNV, dauThang, cuoiThang);
             List<CaTruc> caTrucThang = caTrucDAO.getCaTrucTrongThang(maNV, dauThang, cuoiThang);
             long tongPhutThucTe = 0;
             for (CaTruc ca : caTrucThang) {
                 if (!ca.getNgay().isAfter(now)) {
                     boolean coDiLam = hoaDonDAO.kiemTraHoatDongNVTrongNgay(maNV, ca.getNgay());
                     if (coDiLam) {
                         java.time.Duration thoiLuong = java.time.Duration.between(ca.getGioBatDau(), ca.getGioKetThuc());
                         if (thoiLuong.isNegative()) thoiLuong = thoiLuong.plusHours(24);
                         tongPhutThucTe += thoiLuong.toMinutes();
                     }
                 }
             }
             gioThucTe = tongPhutThucTe / 60.0;
        } catch (Exception e) { e.printStackTrace(); }
        drawDonutChart(hoursCanvas.getGraphicsContext2D(), gioThucTe, gioMucTieu, Color.web("#2ecc71"));
        if (hoursDetailsLabel != null) hoursDetailsLabel.setText(String.format("%.1f/%.0f giờ", gioThucTe, gioMucTieu));
    }

    private void updateKpiDoanhThu() {
        TaiKhoan currentUser = MainApp.getLoggedInUser();
        if (currentUser == null || currentUser.getNhanVien() == null) {
             drawDonutChart(revenueCanvas.getGraphicsContext2D(), 0, 1, Color.web("#f39c12"));
             if (revenueDetailsLabel != null) revenueDetailsLabel.setText("N/A");
             return;
        }
        String maNV = currentUser.getNhanVien().getMaNV();
        LocalDate now = LocalDate.now();
        LocalDate dauThang = now.withDayOfMonth(1);
        LocalDate cuoiThang = now.withDayOfMonth(now.lengthOfMonth());
        double doanhThuThucTe = 0;
        double doanhThuMucTieu = 10_000_000.0; 
        try {
            doanhThuThucTe = hoaDonDAO.getDoanhThuNhanVienTrongThang(maNV, dauThang, cuoiThang);
        } catch (Exception e) { e.printStackTrace(); }
        drawDonutChart(revenueCanvas.getGraphicsContext2D(), doanhThuThucTe, doanhThuMucTieu, Color.web("#f39c12"));
        if (revenueDetailsLabel != null) {
            double trieuThucTe = doanhThuThucTe / 1_000_000.0;
            double trieuMucTieu = doanhThuMucTieu / 1_000_000.0;
            revenueDetailsLabel.setText(String.format("%.1f/%.0f Tr", trieuThucTe, trieuMucTieu));
        }
    }

    private void drawDonutChart(GraphicsContext gc, double actual, double target, Color color) {
        double w = gc.getCanvas().getWidth();
        double h = gc.getCanvas().getHeight();
        double size = Math.min(w, h);
        double cx = w / 2;
        double cy = h / 2;
        double strokeWidth = size * 0.15; 
        double radius = (size - strokeWidth) / 2;
        gc.clearRect(0, 0, w, h);
        gc.setLineWidth(strokeWidth);
        gc.setStroke(Color.web("#e0e0e0"));
        gc.setLineCap(StrokeLineCap.BUTT); 
        gc.strokeOval(cx - radius, cy - radius, radius * 2, radius * 2);
        double ratio = (target == 0) ? 0 : (actual / target);
        ratio = Math.max(0, Math.min(1.0, ratio));
        gc.setStroke(color);
        gc.setLineCap(StrokeLineCap.ROUND); 
        gc.strokeArc(cx - radius, cy - radius, radius * 2, radius * 2, 90, -ratio * 360, javafx.scene.shape.ArcType.OPEN);
    }

    @FXML private void handleCurrentWeek() { startOfWeek = LocalDate.now().with(DayOfWeek.MONDAY); datePicker.setValue(startOfWeek); updateWeeklyCalendar(); }
    @FXML private void handlePreviousWeek() { startOfWeek = startOfWeek.minusWeeks(1); datePicker.setValue(startOfWeek); updateWeeklyCalendar(); }
    @FXML private void handleNextWeek() { startOfWeek = startOfWeek.plusWeeks(1); datePicker.setValue(startOfWeek); updateWeeklyCalendar(); }
    @FXML private void updateCalendarFromDatePicker() { if(datePicker.getValue()!=null) { startOfWeek = datePicker.getValue().with(DayOfWeek.MONDAY); updateWeeklyCalendar(); } }
}
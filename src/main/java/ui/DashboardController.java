package ui;

import dao.CaTrucDAO; // 🔥 Import DAO
import dao.DatBanDAO;
import dao.HoaDonDAO;
import entity.CaTruc;
import entity.HoaDon;
import entity.NhanVien; // 🔥 Import NhanVien
import entity.TaiKhoan;
import entity.TrangThaiBan;
import ui.MainApp; //
import javafx.application.Platform; // 🔥 Import Platform
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets; // 🔥 Import Insets
import javafx.geometry.Pos;   // 🔥 Import Pos
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.control.Tooltip;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime; // 🔥 Import LocalTime
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.List;      // 🔥 Import List
import java.util.Map;       // 🔥 Import Map
import java.util.stream.Collectors; // 🔥 Import Collectors
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;

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
    @FXML private Label lblSoNgayNghi;
    @FXML private Label lblBanPhucVu; // <-- THÊM DÒNG NÀY
    @FXML private Label lblBanDatTruoc; // <-- THÊM DÒNG NÀY
    @FXML private Label lblTongGioLam; // <-- THÊM DÒNG NÀY
    @FXML private Label lblTienMatDauCa;      // Label mới
    @FXML private Label lblTienMatThuDuoc;   // Đã có
    @FXML private Label lblTienMatDuKien;     // Label mới
    @FXML private VBox vboxTienMatTongKet;    // Sửa fx:id VBox
    @FXML private VBox vboxKiemKeDauCa;       // VBox chứa phần nhập liệu đầu ca
    @FXML private Button btnXacNhanKiemKe;   // Đã có
    @FXML private VBox rootDashboardVBox;
    private LocalDate startOfWeek;
    private Tooltip tooltip = new Tooltip();

    // 🔥 Khởi tạo DAO
    private final CaTrucDAO caTrucDAO = new CaTrucDAO();
    private final HoaDonDAO hoaDonDAO = new HoaDonDAO(); // <-- THÊM DÒNG NÀY
    private final DatBanDAO datBanDAO = new DatBanDAO();
    private boolean daKiemKeHoacHoatDong = false;
    private Timeline tienMatPollingTimeline;
    // private final NhanVienDAO nhanVienDAO = new NhanVienDAO(); // Không cần thiết nếu CaTrucDAO đã lấy tên NV

    @FXML
    private void initialize() {
        startOfWeek = LocalDate.now().with(DayOfWeek.MONDAY);
        datePicker.setValue(startOfWeek);
        updateWeeklyCalendar(); // Gọi hàm cập nhật lịch
        setupTableMap();
        setupTooltips();
        updateKpiGioLam();
        updateSoNgayNghi();
        updateTheBanPhucVu();
        updateTheBanDatTruoc();
        updateTheTongGioLam();
        updateKpiDoanhThu();
        updateTienMatCaHienTai();
        kiemTraVaCapNhatTrangThaiKiemKe();
        if (rootDashboardVBox != null) {
            rootDashboardVBox.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene == null) {
                    stopTienMatPolling(); // Dừng khi rời khỏi màn hình Dashboard
                } else {
                    // Khi quay lại, kiểm tra lại trạng thái và có thể start polling nếu cần
                    kiemTraVaCapNhatTrangThaiKiemKe();
                }
            });
        } // <-- 🔥 THÊM DẤU NGOẶC NHỌN ĐÓNG NÀY VÀO 🔥
    } //
    /**
     * 🔥 HÀM ĐÃ SỬA LOGIC: Xử lý trường hợp đặc biệt khi Prefs lưu 0.0
     * nhưng chưa có hoạt động tiền mặt.
     */
    private void kiemTraVaCapNhatTrangThaiKiemKe() {
        TaiKhoan currentUser = MainApp.getLoggedInUser();
        if (currentUser == null || currentUser.getNhanVien() == null) {
            // Ẩn cả hai VBox nếu không có user
            if (vboxKiemKeDauCa != null) {vboxKiemKeDauCa.setVisible(false); vboxKiemKeDauCa.setManaged(false);}
            if (vboxTienMatTongKet != null) {vboxTienMatTongKet.setVisible(false); vboxTienMatTongKet.setManaged(false);}
            this.daKiemKeHoacHoatDong = false;
            stopTienMatPolling();
            return;
        }

        String maNV = currentUser.getNhanVien().getMaNV();
        LocalDate homNay = LocalDate.now();
        this.daKiemKeHoacHoatDong = false; // Reset cờ

        // --- KIỂM TRA PREFERENCES TRƯỚC ---
        double savedInitialCash = MainApp.getInitialCashCount(maNV);

        // Biến tạm để quyết định hiển thị
        boolean showInput = false;
        boolean showSummary = false;

        try {
            if (savedInitialCash > 0) {
                // TRƯỜNG HỢP 1: ĐÃ KIỂM KÊ VỚI SỐ TIỀN > 0 => Hiện tổng kết
                System.out.println("Kiểm tra ban đầu: Đã kiểm kê hôm nay (Prefs > 0: " + savedInitialCash + ").");
                this.daKiemKeHoacHoatDong = true;
                showInput = false;
                showSummary = true;
            } else if (savedInitialCash == 0.0) {
                // TRƯỜNG HỢP 2: PREFS LƯU 0.0 => Kiểm tra xem có hoạt động chưa
                System.out.println("Kiểm tra ban đầu: Prefs đang lưu 0.0. Kiểm tra hoạt động...");
                boolean daCoTienMat = hoaDonDAO.kiemTraTienMatTrongNgay(maNV, homNay);
                if (daCoTienMat) {
                    // 2a: Prefs=0 VÀ Đã hoạt động => Hiện tổng kết (0.0 là hợp lệ)
                    System.out.println("    --> Đã có HĐ tiền mặt. Coi 0.0 là đúng.");
                    this.daKiemKeHoacHoatDong = true;
                    showInput = false;
                    showSummary = true;
                } else {
                    // 2b: Prefs=0 NHƯNG CHƯA hoạt động => Lỗi lưu trữ? Hiện ô nhập
                    System.out.println("    --> Chưa có HĐ tiền mặt. Giá trị 0.0 có thể sai. Hiện ô nhập.");
                    this.daKiemKeHoacHoatDong = false;
                    showInput = true;
                    showSummary = false;
                    // Không cần xóa Preferences, lần nhập tiếp theo sẽ ghi đè
                }
            } else { // savedInitialCash < 0 (là -1.0)
                // TRƯỜNG HỢP 3: CHƯA KIỂM KÊ HÔM NAY
                System.out.println("Kiểm tra ban đầu: Chưa kiểm kê hôm nay (Prefs: -1).");
                this.daKiemKeHoacHoatDong = false;
                showInput = true; // Luôn hiện ô nhập
                showSummary = false;
            }

            // --- Cập nhật giao diện dựa trên quyết định ---
            if (vboxKiemKeDauCa != null) {
                vboxKiemKeDauCa.setVisible(showInput);
                vboxKiemKeDauCa.setManaged(showInput);
                // Reset trạng thái ô nhập nếu hiển thị lại
                if (showInput) {
                    if (txtSoTienKiemKe != null) txtSoTienKiemKe.setDisable(false);
                    if (btnXacNhanKiemKe != null) btnXacNhanKiemKe.setDisable(false);
                }
            }
            if (vboxTienMatTongKet != null) {
                vboxTienMatTongKet.setVisible(showSummary);
                vboxTienMatTongKet.setManaged(showSummary);
            }

            // Gọi cập nhật chi tiết nếu hiển thị phần tổng kết
            if (showSummary) {
                updateTienMatCaHienTai(); // Sẽ đọc lại giá trị từ Prefs và bắt đầu polling
            } else {
                stopTienMatPolling(); // Dừng polling nếu chỉ hiển thị ô nhập
            }

        } catch (SQLException e) {
            System.err.println("Lỗi DB khi kiểm tra tiền mặt ban đầu: " + e.getMessage());
            // Xử lý lỗi: Ẩn cả hai
            if (vboxKiemKeDauCa != null) {vboxKiemKeDauCa.setVisible(false); vboxKiemKeDauCa.setManaged(false);}
            if (vboxTienMatTongKet != null) {vboxTienMatTongKet.setVisible(false); vboxTienMatTongKet.setManaged(false);}
            this.daKiemKeHoacHoatDong = false;
            stopTienMatPolling();
            e.printStackTrace();
        }
    }
    /**
     * 🔥 HÀM MỚI: Bắt đầu Timeline để cập nhật tiền mặt thu được định kỳ.
     */
    private void startTienMatPolling() {
        if (tienMatPollingTimeline == null) {
            tienMatPollingTimeline = new Timeline(
                // 👇 SỬ DỤNG TÊN ĐẦY ĐỦ Ở ĐÂY 👇
                new KeyFrame(javafx.util.Duration.seconds(20.0), event -> {
                    Platform.runLater(this::updateTienMatLabelsRealtime);
                })
            );
            tienMatPollingTimeline.setCycleCount(Animation.INDEFINITE);
        }
        // ... (phần if kiểm tra và play() giữ nguyên) ...
        if (this.daKiemKeHoacHoatDong && tienMatPollingTimeline.getStatus() != Animation.Status.RUNNING) {
            System.out.println("Bắt đầu polling tiền mặt...");
            tienMatPollingTimeline.play();
        }
    }

    /**
     * 🔥 HÀM MỚI: Dừng Timeline cập nhật tiền mặt.
     */
    private void stopTienMatPolling() {
        if (tienMatPollingTimeline != null && tienMatPollingTimeline.getStatus() == Animation.Status.RUNNING) {
            System.out.println("Dừng polling tiền mặt.");
            tienMatPollingTimeline.stop();
        }
    }

    /**
     * 🔥 HÀM MỚI: Chỉ cập nhật các label tiền mặt (Thu được, Dự kiến) cho polling.
     * Không ẩn/hiện VBox hay bắt đầu/dừng polling ở đây.
     */
    private void updateTienMatLabelsRealtime() {
        // ... (lấy user/nv) ...
    	TaiKhoan currentUser = MainApp.getLoggedInUser();
        if (currentUser == null || currentUser.getNhanVien() == null) return;
        String maNV = currentUser.getNhanVien().getMaNV();
        double dauCa = MainApp.getInitialCashCount(maNV); // Lấy đầu ca
        if (dauCa < 0) return; // Chưa kiểm kê, không cập nhật realtime

        LocalDate homNay = LocalDate.now();
        double tienMatThuDuoc = 0;
        try {
            tienMatThuDuoc = hoaDonDAO.getTongTienMatTrongNgay(maNV, homNay);
            double tienMatDuKien = dauCa + tienMatThuDuoc; // Tính lại dự kiến

            if (lblTienMatThuDuoc != null) lblTienMatThuDuoc.setText(String.format("%,.0f đ", tienMatThuDuoc));
            if (lblTienMatDuKien != null) lblTienMatDuKien.setText(String.format("%,.0f đ", tienMatDuKien));
        } catch (Exception e) { /* ... xử lý lỗi ... */ }
    }
	/**
     * 🔥 HÀM MỚI: Tính toán và cập nhật thẻ "Tổng giờ làm" trong tháng.
     * Chỉ tính giờ cho những ngày có lịch làm VÀ có hoạt động (có hóa đơn).
     */
    private void updateTheTongGioLam() {
        TaiKhoan currentUser = MainApp.getLoggedInUser();
        if (currentUser == null || currentUser.getNhanVien() == null) {
            if (lblTongGioLam != null) lblTongGioLam.setText("N/A");
            System.err.println("updateTheTongGioLam: Không tìm thấy thông tin người dùng/nhân viên.");
            return;
        }

        // Không cần kiểm tra chức vụ ở đây, tính cho tất cả

        String maNV = currentUser.getNhanVien().getMaNV();
        long tongSoPhutLam = 0; // Dùng long để lưu tổng số phút

        LocalDate homNay = LocalDate.now();
        LocalDate dauThang = homNay.withDayOfMonth(1);
        LocalDate cuoiThang = homNay.withDayOfMonth(homNay.lengthOfMonth());

        try {
            // 1. Lấy tất cả ca trực của NV trong tháng
            List<CaTruc> caTrucThang = caTrucDAO.getCaTrucTrongThang(maNV, dauThang, cuoiThang);
            System.out.println("updateTheTongGioLam: Tìm thấy " + caTrucThang.size() + " ca trực cho NV " + maNV + " trong tháng.");

            // 2. Duyệt qua từng ca trực
            for (CaTruc ca : caTrucThang) {
                LocalDate ngayLam = ca.getNgay();

                // Chỉ tính những ngày đã qua hoặc ngày hôm nay
                if (!ngayLam.isAfter(homNay)) {
                    // 3. Kiểm tra xem có hoạt động (hóa đơn) vào ngày đó không
                    boolean coHoatDong = hoaDonDAO.kiemTraHoatDongNVTrongNgay(maNV, ngayLam);

                    if (coHoatDong) {
                        // 4. Nếu có hoạt động, tính thời lượng ca và cộng dồn
                        LocalTime batDau = ca.getGioBatDau();
                        LocalTime ketThuc = ca.getGioKetThuc();
                        Duration thoiLuongCa = Duration.between(batDau, ketThuc);

                        // Xử lý ca qua đêm
                        if (ketThuc.isBefore(batDau)) {
                            thoiLuongCa = thoiLuongCa.plusHours(24);
                        }

                        tongSoPhutLam += thoiLuongCa.toMinutes(); // Cộng dồn số phút
                        System.out.println(" -> Ngày " + ngayLam + " có làm việc. Cộng thêm: " + thoiLuongCa.toMinutes() + " phút.");
                    } else {
                         System.out.println(" -> Ngày " + ngayLam + " không có HĐ (coi như nghỉ). Không cộng giờ.");
                    }
                }
            }

            // 5. Chuyển đổi tổng số phút thành giờ và định dạng
            long tongSoGio = tongSoPhutLam / 60;
            // long soPhutLe = tongSoPhutLam % 60; // Nếu muốn hiển thị cả phút lẻ

            String hienThiGioLam = String.format("%d giờ", tongSoGio);
            // Hoặc: String hienThiGioLam = String.format("%d giờ %d phút", tongSoGio, soPhutLe);

            // 6. Cập nhật Label
            if (lblTongGioLam != null) {
                lblTongGioLam.setText(hienThiGioLam);
            } else {
                System.err.println("updateTheTongGioLam: Lỗi - lblTongGioLam chưa được inject.");
            }

        } catch (SQLException sqlEx) { // Bắt lỗi từ DAO
             if (lblTongGioLam != null) lblTongGioLam.setText("Lỗi DB");
             System.err.println("Lỗi CSDL khi tính tổng giờ làm: " + sqlEx.getMessage());
             sqlEx.printStackTrace();
        } catch (Exception e) { // Bắt các lỗi khác
            if (lblTongGioLam != null) lblTongGioLam.setText("Lỗi");
            System.err.println("Lỗi khi tính toán tổng giờ làm: " + e.getMessage());
            e.printStackTrace();
        }
    }
	/**
     * 🔥 HÀM MỚI: Cập nhật thẻ thống kê "Bàn đặt trước".
     * Lấy số bàn có HĐ 'Dat' / Tổng số bàn.
     */
    private void updateTheBanDatTruoc() {
        try {
            // 1. Gọi DAO để đếm số bàn đã đặt
            int soBanDat = datBanDAO.demSoBanDatTruoc();

            // 2. Gọi DAO để đếm tổng số bàn
            int tongSoBan = datBanDAO.demTongSoBan();

            // 3. Cập nhật Label (Kiểm tra null)
            if (lblBanDatTruoc != null) {
                // Hiển thị dạng "X/Y bàn"
                lblBanDatTruoc.setText(soBanDat + "/" + tongSoBan + " bàn");
            } else {
                System.err.println("updateTheBanDatTruoc: Lỗi - lblBanDatTruoc chưa được inject.");
            }
        } catch (Exception e) {
            if (lblBanDatTruoc != null) {
                lblBanDatTruoc.setText("Lỗi");
            }
            System.err.println("Lỗi khi cập nhật thẻ bàn đặt trước: " + e.getMessage());
            e.printStackTrace();
        }
    }
	/**
     * 🔥 HÀM MỚI: Cập nhật thẻ thống kê "Bàn đang phục vụ".
     */
    /**
     * 🔥 HÀM ĐÃ SỬA: Cập nhật thẻ thống kê "Bàn đang phục vụ"
     * Lấy số lượng từ danh sách mã bàn có hóa đơn 'DangSuDung'.
     */
    private void updateTheBanPhucVu() {
        try {
            // 1. Gọi hàm DAO lấy danh sách mã bàn đang phục vụ thực tế (từ HoaDon)
            List<String> maBanDangPhucVuList = datBanDAO.getMaBanDangPhucVu();

            // 2. Lấy số lượng bàn từ kích thước danh sách
            int soBanPV = maBanDangPhucVuList.size();

            // 3. Cập nhật Label (Kiểm tra null)
            if (lblBanPhucVu != null) {
                lblBanPhucVu.setText(soBanPV + " bàn");
            } else {
                System.err.println("updateTheBanPhucVu: Lỗi - lblBanPhucVu chưa được inject.");
            }
        } catch (Exception e) {
            if (lblBanPhucVu != null) {
                lblBanPhucVu.setText("Lỗi");
            }
            System.err.println("Lỗi khi cập nhật thẻ bàn phục vụ: " + e.getMessage());
            e.printStackTrace();
        }
    }
	/**
     * 🔥 HÀM ĐÃ SỬA: Tính toán và cập nhật số ngày nghỉ đã sử dụng.
     * Dùng lịch làm việc và kiểm tra hoạt động qua HoaDonDAO.
     */
    private void updateSoNgayNghi() {
        TaiKhoan currentUser = MainApp.getLoggedInUser();
        if (currentUser == null || currentUser.getNhanVien() == null) {
            if (lblSoNgayNghi != null) lblSoNgayNghi.setText("N/A");
            System.err.println("updateSoNgayNghi: Không tìm thấy thông tin người dùng/nhân viên.");
            return;
        }

        String chucVu = currentUser.getVaiTro() != null ? currentUser.getVaiTro().toString() : "";
        
        // <<< THÊM DÒNG NÀY ĐỂ DEBUG >>>
        System.out.println("DEBUG updateSoNgayNghi: Chức vụ thực tế = [" + chucVu + "]"); 
        // <<< KẾT THÚC THÊM DEBUG >>>

        if ("QuanLy".equalsIgnoreCase(chucVu)) {
             if (lblSoNgayNghi != null) lblSoNgayNghi.setText("N/A");
             System.out.println("updateSoNgayNghi: Bỏ qua tính ngày nghỉ cho Quản lý.");
            return;
        }

        // 🔥 GIỚI HẠN CHỈ CHO THU NGÂN (VÌ DÙNG HOA DON)
        // Check if the role string IS NOT "ThuNgan" (case-insensitive)
        if (!"Nhân viên thu ngân".equalsIgnoreCase(chucVu)) { 
            if (lblSoNgayNghi != null) {
                lblSoNgayNghi.setText("N/A"); // Set N/A nếu không phải chức vụ này
            }
            System.out.println("updateSoNgayNghi: Bị chặn vì chức vụ không phải 'Nhân viên thu ngân'. Chức vụ là: [" + chucVu + "]"); 
            return; // Thoát hàm
       }
        // ===========================================

        // ... (phần còn lại của hàm giữ nguyên) ...
        String maNV = currentUser.getNhanVien().getMaNV();
        int soNgayNghiDaDung = 0;
        int gioiHanNgayNghi = 5;

        LocalDate homNay = LocalDate.now();
        LocalDate dauThang = homNay.withDayOfMonth(1);
        LocalDate cuoiThang = homNay.withDayOfMonth(homNay.lengthOfMonth());

        try {
            List<LocalDate> ngayCoLich = caTrucDAO.getNgayCoLichLam(maNV, dauThang, cuoiThang);
            System.out.println("updateSoNgayNghi: Tìm thấy " + ngayCoLich.size() + " ngày có lịch làm cho NV " + maNV + " trong tháng.");

            for (LocalDate ngayLam : ngayCoLich) {
                if (!ngayLam.isAfter(homNay)) {
                    boolean coHoatDong = hoaDonDAO.kiemTraHoatDongNVTrongNgay(maNV, ngayLam);
                    if (!coHoatDong) {
                        System.out.println(" -> Phát hiện nghỉ (không có HĐ) ngày: " + ngayLam);
                        soNgayNghiDaDung++;
                    }
                }
            }

            System.out.println("updateSoNgayNghi: Đã kiểm tra hoạt động qua Hóa Đơn, số ngày nghỉ = " + soNgayNghiDaDung);

             if (lblSoNgayNghi != null) {
                lblSoNgayNghi.setText(soNgayNghiDaDung + "/" + gioiHanNgayNghi + " ngày");
             } else {
                 System.err.println("updateSoNgayNghi: Lỗi - lblSoNgayNghi chưa được inject từ FXML.");
             }

        } catch (SQLException sqlEx) {
             if (lblSoNgayNghi != null) lblSoNgayNghi.setText("Lỗi DB");
             System.err.println("Lỗi CSDL khi kiểm tra hoạt động NV: " + sqlEx.getMessage());
             sqlEx.printStackTrace();
        } catch (Exception e) {
             if (lblSoNgayNghi != null) lblSoNgayNghi.setText("Lỗi");
            System.err.println("Lỗi khi tính toán số ngày nghỉ: " + e.getMessage());
            e.printStackTrace();
        }
    }

	/**
     * 🔥 HÀM ĐÃ SỬA: Cập nhật lịch làm việc hàng tuần với dữ liệu từ CSDL
     */
    /**
     * 🔥 HÀM ĐÃ SỬA: Cập nhật lịch làm việc hàng tuần với dữ liệu từ CSDL
     * Đã sửa lỗi biến `duration` sau khi xóa import `javafx.util.Duration`.
     */
    private void updateWeeklyCalendar() {
        weeklyCalendarGrid.getChildren().clear();

        // Lấy thông tin người dùng đang đăng nhập
        TaiKhoan currentUser = MainApp.getLoggedInUser();
        if (currentUser == null || currentUser.getNhanVien() == null) {
            Label errorLabel = new Label("Không thể tải lịch làm việc.\nVui lòng đăng nhập lại.");
            errorLabel.setStyle("-fx-text-fill: red;");
            weeklyCalendarGrid.add(errorLabel, 1, 1, 7, 3);
            return;
        }
        String loggedInMaNV = currentUser.getNhanVien().getMaNV();

        // Gọi hàm DAO với mã NV
        List<CaTruc> caTrucTrongTuan = caTrucDAO.layCaTrucTrongTuanCuaNV(startOfWeek, loggedInMaNV);

        String[] daysOfWeek = {"Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ nhật"};
        String[] rowHeaders = {"Ca làm", "Sáng", "Chiều", "Tối"};

        // --- Tạo Header ---
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

        // --- Điền dữ liệu vào các ô ---
        for (int col = 0; col < daysOfWeek.length; col++) {
            LocalDate dateOfCell = startOfWeek.plusDays(col);
            for (int row = 1; row < rowHeaders.length; row++) {
                String timeSlotOfCell = rowHeaders[row];
                LocalTime startTimeSlot = getTimeSlotStartTime(timeSlotOfCell);

                // Lọc ca trực cho ô hiện tại
                List<CaTruc> shiftsInCell = caTrucTrongTuan.stream()
                        .filter(ca -> ca.getNgay().equals(dateOfCell) &&
                                      ca.getGioBatDau().equals(startTimeSlot))
                        .collect(Collectors.toList());

                VBox cellContainer = new VBox(5);
                cellContainer.setAlignment(Pos.TOP_LEFT);
                cellContainer.getStyleClass().add("day-cell");
                cellContainer.setPadding(new Insets(8));

                if (!shiftsInCell.isEmpty()) {
                    // Group ca theo giờ (nếu có nhiều NV cùng 1 ca)
                    Map<String, List<CaTruc>> shiftsGroupedByTime = shiftsInCell.stream()
                            .collect(Collectors.groupingBy(ca ->
                                ca.getGioBatDau().format(DateTimeFormatter.ofPattern("HH:mm")) + " - " +
                                ca.getGioKetThuc().format(DateTimeFormatter.ofPattern("HH:mm"))
                            ));

                    // Hiển thị thông tin từng group ca
                    shiftsGroupedByTime.forEach((timeString, shiftsGroup) -> {
                        CaTruc mauCa = shiftsGroup.get(0); // Lấy 1 ca làm mẫu
                        LocalTime batDau = mauCa.getGioBatDau();
                        LocalTime ketThuc = mauCa.getGioKetThuc();

                        // --- Tính toán thời lượng (Sử dụng java.time.Duration) ---
                        // Đảm bảo dùng java.time.Duration
                        java.time.Duration thoiLuongCa = java.time.Duration.between(batDau, ketThuc); 
                        
                        // Xử lý ca qua đêm
                        if (ketThuc.isBefore(batDau)) {
                            // 🔥 SỬA LỖI: Dùng biến thoiLuongCa thay vì duration 🔥
                            thoiLuongCa = thoiLuongCa.plusHours(24); 
                        }

                        // 🔥 SỬA LỖI: Dùng biến thoiLuongCa thay vì duration 🔥
                        long hours = thoiLuongCa.toHours(); 
                        long minutes = thoiLuongCa.toMinutes() % 60; 

                        // Tạo chuỗi hiển thị tổng giờ
                        String durationString;
                        if (minutes == 0) {
                            durationString = String.format("Tổng: %d giờ", hours);
                        } else {
                            durationString = String.format("Tổng: %d giờ %d phút", hours, minutes);
                        }
                        // --- Kết thúc tính toán ---

                        // Tạo Labels
                        Label timeLabel = new Label(timeString);
                        timeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #1a1a1a;");

                        Label durationLabel = new Label(durationString);
                        durationLabel.getStyleClass().add("shift-duration-label");

                        // Thêm vào VBox con
                        VBox shiftDisplayBox = new VBox(2);
                        shiftDisplayBox.getChildren().addAll(timeLabel, durationLabel);
                        cellContainer.getChildren().add(shiftDisplayBox);
                    });
                }
                // Thêm VBox (cellContainer) vào lưới lịch
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

    /**
     * 🔥 HÀM ĐÃ SỬA: Xử lý nút Xác nhận kiểm kê đầu ca.
     * Thêm log chi tiết để kiểm tra việc lưu vào MainApp.
     */
    @FXML
    private void handleCheckCash() {
        String tienKiemKeStr = txtSoTienKiemKe.getText().replaceAll("[^0-9]", "");
        double soTienDaNhap = 0;
        try {
             if (!tienKiemKeStr.isEmpty()){
                 soTienDaNhap = Double.parseDouble(tienKiemKeStr);
                 System.out.println("handleCheckCash: Đã parse số tiền nhập = " + soTienDaNhap);
             } else { /* báo lỗi thiếu thông tin */ return; }
        } catch (NumberFormatException e) { /* báo lỗi số không hợp lệ */ return; }

        TaiKhoan currentUser = MainApp.getLoggedInUser();
        if (currentUser == null || currentUser.getNhanVien() == null) { /* báo lỗi user */ return; }
        String maNV = currentUser.getNhanVien().getMaNV();

        System.out.println("handleCheckCash: Chuẩn bị gọi MainApp.setInitialCashCount với số tiền: " + soTienDaNhap + " cho NV: " + maNV);
        MainApp.setInitialCashCount(soTienDaNhap, maNV); // Lưu vào Preferences
        double kiemTraLai = MainApp.getInitialCashCount(maNV);
        System.out.println("handleCheckCash: Đã gọi set xong. Giá trị get lại được từ MainApp là: " + kiemTraLai);

        showInfoAlert("Xác nhận", "Đã ghi nhận số tiền kiểm kê đầu ca.");

        // Ẩn phần nhập liệu
        if (vboxKiemKeDauCa != null) {
             vboxKiemKeDauCa.setVisible(false);
             vboxKiemKeDauCa.setManaged(false);
        }

        this.daKiemKeHoacHoatDong = true; // Đánh dấu đã kiểm kê
        updateTienMatCaHienTai(); // Gọi cập nhật và bắt đầu polling
    }
    /**
     * 🔥 HÀM MỚI/SỬA: Cập nhật các Label liên quan đến tiền mặt trong ca.
     * Được gọi sau khi xác nhận kiểm kê đầu ca.
     */
    /**
     * 🔥 HÀM ĐÃ SỬA: Cập nhật các Label tiền mặt. Thêm log chi tiết.
     */
    private void updateTienMatCaHienTai() {
        System.out.println("--- Bắt đầu updateTienMatCaHienTai ---");

        TaiKhoan currentUser = MainApp.getLoggedInUser();
        // ... (kiểm tra null user/nv) ...
        if (currentUser == null || currentUser.getNhanVien() == null) { /* ẩn VBox, dừng polling, return */ return; }
        String maNV = currentUser.getNhanVien().getMaNV();

        double dauCa = MainApp.getInitialCashCount(maNV);
        System.out.println("updateTienMatCaHienTai: Lấy dauCa từ MainApp = " + dauCa);

        // Chỉ tiếp tục nếu đã kiểm kê (dauCa >= 0)
        if (dauCa < 0) {
             System.out.println("updateTienMatCaHienTai: dauCa < 0 (Lỗi logic?), ẩn tổng kết và dừng.");
             if (vboxTienMatTongKet != null) { /* ẩn VBox */ }
             stopTienMatPolling();
             return;
        }

        // --- HIỂN THỊ VBOX TỔNG KẾT ---
        if (vboxTienMatTongKet != null) {
             System.out.println("updateTienMatCaHienTai: Chuẩn bị set VBox tổng kết visible.");
             vboxTienMatTongKet.setVisible(true);
             vboxTienMatTongKet.setManaged(true);
             System.out.println("updateTienMatCaHienTai: Đã set VBox tổng kết visible.");
        } else { /* báo lỗi VBox null */ return; }
        // --- KẾT THÚC HIỂN THỊ VBOX ---

        LocalDate homNay = LocalDate.now();
        double tienMatThuDuoc = 0;

        try {
            System.out.println("updateTienMatCaHienTai: Chuẩn bị gọi hoaDonDAO.getTongTienMatTrongNgay...");
            tienMatThuDuoc = hoaDonDAO.getTongTienMatTrongNgay(maNV, homNay);
            System.out.println("updateTienMatCaHienTai: Tiền mặt thu được từ DAO = " + tienMatThuDuoc);

            double tienMatDuKien = dauCa + tienMatThuDuoc;
            System.out.println("updateTienMatCaHienTai: Tiền mặt dự kiến = " + tienMatDuKien);

            // --- 🔥 BỎ TEXT "(Đã hoạt động)" 🔥 ---
            String dauCaText = String.format("%,.0f đ", dauCa); // Luôn hiển thị số tiền

            System.out.println("updateTienMatCaHienTai: Chuẩn bị cập nhật Labels...");
            if (lblTienMatDauCa != null) lblTienMatDauCa.setText(dauCaText); else System.err.println("lblTienMatDauCa is null");
            if (lblTienMatThuDuoc != null) lblTienMatThuDuoc.setText(String.format("%,.0f đ", tienMatThuDuoc)); else System.err.println("lblTienMatThuDuoc is null");
            if (lblTienMatDuKien != null) lblTienMatDuKien.setText(String.format("%,.0f đ", tienMatDuKien)); else System.err.println("lblTienMatDuKien is null");
            System.out.println("updateTienMatCaHienTai: Đã cập nhật Labels.");

            startTienMatPolling();

        } catch (Exception e) { /* ... xử lý lỗi ... */ }
        System.out.println("--- Kết thúc updateTienMatCaHienTai ---");
    }
	@FXML private void updateCalendarFromDatePicker() {
        LocalDate selectedDate = datePicker.getValue();
        if (selectedDate != null) {
            startOfWeek = selectedDate.with(DayOfWeek.MONDAY);
            updateWeeklyCalendar();
        }
    }

    /**
     * 🔥 HÀM ĐÃ SỬA: Lấy danh sách bàn từ HÓA ĐƠN "Đang phục vụ"
     * và hiển thị lên sơ đồ, thêm sự kiện mở popup chi tiết.
     */
    private void setupTableMap() {
        tableMapGrid.getChildren().clear(); // Xóa các bàn cũ

        try {
            // 1. Lấy danh sách Hóa đơn đang phục vụ (chứa maHD và maBan)
            List<HoaDon> hoaDonDangPhucVuList = datBanDAO.getHoaDonDangPhucVu(); // Gọi hàm mới

            // Nếu không có bàn nào đang phục vụ, hiển thị thông báo và thoát
            if (hoaDonDangPhucVuList.isEmpty()) {
                Label lblTrong = new Label("Không có bàn nào\nđang phục vụ.");
                lblTrong.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
                lblTrong.setPadding(new Insets(10));
                tableMapGrid.add(lblTrong, 0, 0, 3, 1); // Hiển thị thông báo (giả sử maxCols=3)
                return; // Dừng lại ở đây
            }

            // 2. Hiển thị các bàn lên GridPane
            int col = 0, row = 0;
            int maxCols = 3; // Số cột tối đa

            for (HoaDon hd : hoaDonDangPhucVuList) {
                // Lấy mã bàn từ hóa đơn
                String maBan = (hd.getBan() != null) ? hd.getBan().getMaBan() : "Lỗi";

                Hyperlink tableLink = new Hyperlink(maBan);
                tableLink.getStyleClass().add("table-link");

                // Đặt màu đỏ cho bàn đang phục vụ
                tableLink.setStyle("-fx-background-color: #e74c3c;"); // Màu đỏ

                // 🔥 GÁN SỰ KIỆN MỞ POPUP CHI TIẾT 🔥
                // Truyền đối tượng HoaDon (hd) vào hàm xử lý
                tableLink.setOnAction(e -> openChiTietPopup(hd));

                tableMapGrid.add(tableLink, col, row);
                GridPane.setMargin(tableLink, new Insets(5)); // Thêm khoảng cách nhỏ

                col++;
                if (col >= maxCols) {
                    col = 0;
                    row++;
                }
            }

        } catch (Exception e) {
            // Xử lý lỗi nếu không lấy được danh sách
            System.err.println("Lỗi khi tải sơ đồ bàn đang phục vụ: " + e.getMessage());
            Label lblLoi = new Label("Lỗi tải danh sách bàn.");
            tableMapGrid.add(lblLoi, 0, 0);
            e.printStackTrace();
        }
    }
    /**
     * 🔥 HÀM MỚI: Mở Popup Chi Tiết Đặt Bàn cho một Hóa đơn cụ thể.
     * @param hd Hóa đơn cần hiển thị chi tiết.
     */
    private void openChiTietPopup(HoaDon hd) {
        if (hd == null || hd.getMaHD() == null) {
            showErrorAlert("Lỗi", "Không có thông tin hóa đơn để hiển thị.");
            return;
        }

        try {
            // 1. Tải FXML của Popup
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ChiTietDatBan_Popup.fxml"));
            VBox root = loader.load();

            // 2. Lấy Controller của Popup
            ChiTietDatBanController popupController = loader.getController();

            // 3. Tải dữ liệu đầy đủ cho Hóa đơn (vì hd hiện tại chỉ có maHD, maBan)
            HoaDon hdDayDu = datBanDAO.getHoaDonByMaHD(hd.getMaHD());
            if (hdDayDu == null) {
                 showErrorAlert("Lỗi", "Không tìm thấy chi tiết đầy đủ cho hóa đơn " + hd.getMaHD());
                 return;
            }


            // 4. Truyền dữ liệu đầy đủ vào Controller Popup
            // Hàm setHoaDonData cần nhận DatBanDAO nữa
            popupController.setHoaDonData(hdDayDu, datBanDAO);

            // 5. Tạo và hiển thị cửa sổ Popup
            Stage popupStage = new Stage();
            popupStage.setTitle("Chi tiết bàn " + hdDayDu.getBan().getMaBan() + " - HĐ: " + hdDayDu.getMaHD());
            Scene scene = new Scene(root);

            // (Optional) Thêm CSS cho Popup nếu muốn
            URL cssUrl = getClass().getResource("/css/ChiTietDatBan.css"); // Giả sử có file CSS này
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            }

            popupStage.setScene(scene);
            popupStage.show(); // Dùng show() thay vì showAndWait() để không chặn Dashboard

        } catch (IOException e) {
            e.printStackTrace();
            showErrorAlert("Lỗi UI", "Không thể mở giao diện Chi Tiết Đặt Bàn Popup: " + e.getMessage());
        } catch (Exception e) {
             e.printStackTrace();
             showErrorAlert("Lỗi không xác định", "Đã xảy ra lỗi khi mở Popup Chi Tiết Bàn: " + e.getMessage());
        }
    }
	private static class Table { String name, status; Table(String n, String s){name=n; status=s;} }
	/**
     * 🔥 HÀM ĐÃ SỬA LỖI VẼ: Vẽ biểu đồ tròn số giờ làm dựa trên giờ thực tế và mục tiêu.
     * @param gioThucTe Số giờ làm thực tế.
     * @param gioMucTieu Tổng số giờ làm theo lịch (mục tiêu).
     */
    private void drawHoursChart(double gioThucTe, double gioMucTieu) {
        GraphicsContext gc = hoursCanvas.getGraphicsContext2D();
        double w = hoursCanvas.getWidth(), h = hoursCanvas.getHeight();
        double cx = w / 2, cy = h / 2;
        // or: Bán kính vòng ngoài, ir: Bán kính vòng trong (lỗ donut)
        double or = Math.min(w, h) / 2.5, ir = or * 0.7; 

        gc.clearRect(0, 0, w, h); // Xóa canvas cũ

        // 1. Vẽ vòng tròn nền màu xám nhạt (kích thước ngoài)
        gc.setFill(Color.web("#e0e0e0")); // Màu xám nền
        gc.fillOval(cx - or, cy - or, 2 * or, 2 * or);

        // Tính tỷ lệ hoàn thành (giữ nguyên)
        double ratio = (gioMucTieu == 0) ? 0 : (gioThucTe / gioMucTieu);
        ratio = Math.max(0, Math.min(1, ratio)); // Giới hạn từ 0 đến 1

        // 2. Vẽ CUNG MÀU XANH LÁ (kích thước ngoài)
        gc.setFill(Color.web("#27ae60")); // Màu xanh lá
        // Vẽ cung từ góc 12h (90 độ), ngược chiều kim đồng hồ, dựa trên tỷ lệ
        // Kích thước bounding box phải là của vòng ngoài (or)
        gc.fillArc(cx - or, cy - or, 2 * or, 2 * or, 90, -ratio * 360, javafx.scene.shape.ArcType.ROUND);

        // 3. Vẽ VÒNG TRÒN TRẮNG Ở GIỮA (kích thước trong) để tạo lỗ donut
        // Màu trắng hoặc màu nền của VBox (ví dụ: #f4f4f4 nếu là màu nền dashboard)
        gc.setFill(Color.web("#f4f4f4")); // <-- Đổi thành màu nền nếu cần
        gc.fillOval(cx - ir, cy - ir, 2 * ir, 2 * ir); // Dùng bán kính trong (ir)

    }
    /**
     * 🔥 HÀM ĐÃ SỬA: Vẽ biểu đồ tròn doanh thu dựa trên doanh thu thực tế và mục tiêu.
     * @param doanhThuThucTe Doanh thu thực tế của nhân viên.
     * @param doanhThuMucTieu Mục tiêu doanh thu (vd: 10 triệu).
     */
    private void drawRevenueChart(double doanhThuThucTe, double doanhThuMucTieu) {
        GraphicsContext gc = revenueCanvas.getGraphicsContext2D();
        double w = revenueCanvas.getWidth(), h = revenueCanvas.getHeight();
        double cx = w / 2, cy = h / 2;
        double or = Math.min(w, h) / 2.5, ir = or * 0.7;

        gc.clearRect(0, 0, w, h); // Xóa canvas cũ

        // Vẽ nền xám
        gc.setFill(Color.web("#e0e0e0"));
        gc.fillOval(cx - or, cy - or, 2 * or, 2 * or);

        // Tính tỷ lệ
        double ratio = (doanhThuMucTieu == 0) ? 0 : (doanhThuThucTe / doanhThuMucTieu);
        ratio = Math.max(0, Math.min(1, ratio)); // Giới hạn 0-1

        // Vẽ cung màu cam
        gc.setFill(Color.web("#f39c12")); // Màu cam
        gc.fillArc(cx - or, cy - or, 2 * or, 2 * or, 90, -ratio * 360, javafx.scene.shape.ArcType.ROUND);

        // Vẽ lỗ donut
        gc.setFill(Color.web("#f4f4f4")); // Màu nền dashboard
        gc.fillOval(cx - ir, cy - ir, 2 * ir, 2 * ir);
    }
     /**
      * 🔥 HÀM ĐÃ SỬA: Cài đặt Tooltip (văn bản chú giải khi di chuột)
      * Lấy text trực tiếp từ các label chi tiết (hoursDetailsLabel, revenueDetailsLabel).
      */
     private void setupTooltips() {
         // Tooltip cho biểu đồ giờ làm
         if (hoursCanvas != null && hoursDetailsLabel != null) {
             hoursCanvas.setOnMouseMoved(e -> {
                 // Lấy text hiện tại từ label chi tiết
                 tooltip.setText("Số giờ làm: " + hoursDetailsLabel.getText());
                 // Hiển thị tooltip gần con trỏ chuột
                 tooltip.show(hoursCanvas, e.getScreenX() + 10, e.getScreenY() + 10);
             });
             // Ẩn tooltip khi chuột rời khỏi canvas
             hoursCanvas.setOnMouseExited(e -> tooltip.hide());
         }

         // Tooltip cho biểu đồ doanh thu
         if (revenueCanvas != null && revenueDetailsLabel != null) {
             revenueCanvas.setOnMouseMoved(e -> {
                 // Lấy text hiện tại từ label chi tiết
                 tooltip.setText("Doanh thu: " + revenueDetailsLabel.getText());
                 // Hiển thị tooltip gần con trỏ chuột
                 tooltip.show(revenueCanvas, e.getScreenX() + 10, e.getScreenY() + 10);
             });
             // Ẩn tooltip khi chuột rời khỏi canvas
             revenueCanvas.setOnMouseExited(e -> tooltip.hide());
         }
     }
     /**
      * 🔥 HÀM ĐÃ SỬA: Cập nhật các Label chi tiết KPI.
      * @param gioLamThucTe Giờ làm thực tế.
      * @param gioLamMucTieu Giờ làm mục tiêu (theo lịch).
      * @param doanhThuThucTe Doanh thu thực tế (Tạm thời là số giả).
      * @param doanhThuMucTieu Doanh thu mục tiêu (Tạm thời là số giả).
      */
     /**
      * 🔥 HÀM ĐÃ SỬA: Cập nhật các Label chi tiết KPI với giá trị thực tế.
      */
     private void updateKpiDetails(double gioLamThucTe, double gioLamMucTieu, double doanhThuThucTe, double doanhThuMucTieu) {
         if (hoursDetailsLabel != null) {
             hoursDetailsLabel.setText(String.format("%.1f / %.1f giờ", gioLamThucTe, gioLamMucTieu));
         }
         if (revenueDetailsLabel != null) {
             // Định dạng tiền tệ không có VNĐ để ngắn gọn
             revenueDetailsLabel.setText(String.format("%,.0f / %,.0f đ", doanhThuThucTe, doanhThuMucTieu));
         }
     }
     /**
      * 🔥 HÀM MỚI: Tính toán doanh thu thực tế và mục tiêu, sau đó cập nhật biểu đồ và label KPI doanh thu.
      */
     private void updateKpiDoanhThu() {
         TaiKhoan currentUser = MainApp.getLoggedInUser();
         // Giữ lại giá trị giờ đã tính trước đó để cập nhật label
         double gioLamThucTeDaTinh = 0;
         double gioLamMucTieuDaTinh = 0;
         // Lấy giá trị giờ từ label nếu có thể, nếu không thì dùng giá trị mặc định/tính lại
         if(hoursDetailsLabel != null && hoursDetailsLabel.getText().contains("/")) {
             try {
                 String[] parts = hoursDetailsLabel.getText().split(" / ");
                 gioLamThucTeDaTinh = Double.parseDouble(parts[0].replace(",", ".")); // Thay thế dấu phẩy nếu cần
                 gioLamMucTieuDaTinh = Double.parseDouble(parts[1].split(" ")[0].replace(",", "."));
             } catch (Exception e) {
                 System.err.println("Lỗi parse giờ từ label: " + e.getMessage());
             }
         }


         if (currentUser == null || currentUser.getNhanVien() == null) {
             System.err.println("updateKpiDoanhThu: Không tìm thấy thông tin người dùng/nhân viên.");
             drawRevenueChart(0, 10000000.0); // Vẽ biểu đồ rỗng với mục tiêu
             // Cập nhật Label thành N/A (giữ lại giờ đã tính)
              updateKpiDetails(gioLamThucTeDaTinh, gioLamMucTieuDaTinh, 0, 10000000.0);
             return;
         }

         String maNV = currentUser.getNhanVien().getMaNV();
         double doanhThuThucTe = 0;
         final double doanhThuMucTieu = 10000000.0; // Mục tiêu 10 triệu

         LocalDate homNay = LocalDate.now();
         LocalDate dauThang = homNay.withDayOfMonth(1);
         LocalDate cuoiThang = homNay.withDayOfMonth(homNay.lengthOfMonth());

         try {
             // 1. Tính Doanh Thu Thực Tế
             doanhThuThucTe = hoaDonDAO.getDoanhThuNhanVienTrongThang(maNV, dauThang, cuoiThang);
             System.out.println("updateKpiDoanhThu: Tổng doanh thu thực tế tháng này: " + doanhThuThucTe);

             // 2. Cập nhật biểu đồ và Label
             drawRevenueChart(doanhThuThucTe, doanhThuMucTieu);
             // Gọi updateKpiDetails với giá trị giờ đã tính và doanh thu mới tính
             updateKpiDetails(gioLamThucTeDaTinh, gioLamMucTieuDaTinh, doanhThuThucTe, doanhThuMucTieu);


         } catch (Exception e) { // Bắt lỗi chung, bao gồm cả SQLException tiềm ẩn
             System.err.println("Lỗi khi cập nhật KPI doanh thu: " + e.getMessage());
             drawRevenueChart(0, doanhThuMucTieu); // Vẽ biểu đồ rỗng
             // Cập nhật label với giá trị giờ và doanh thu lỗi
             updateKpiDetails(gioLamThucTeDaTinh, gioLamMucTieuDaTinh, 0, doanhThuMucTieu);
             if(revenueDetailsLabel != null) revenueDetailsLabel.setText("Lỗi");
             e.printStackTrace();
         }
     }
     /**
      * 🔥 HÀM MỚI: Tính toán giờ làm thực tế và theo lịch, sau đó cập nhật biểu đồ và label KPI giờ.
      */
     private void updateKpiGioLam() {
         TaiKhoan currentUser = MainApp.getLoggedInUser();
         if (currentUser == null || currentUser.getNhanVien() == null) {
              System.err.println("updateKpiGioLam: Không tìm thấy thông tin người dùng/nhân viên.");
             // Vẽ biểu đồ trống hoặc không làm gì
             drawHoursChart(0, 0); // Vẽ biểu đồ rỗng
             // Cập nhật Label thành N/A (giữ nguyên doanh thu giả)
             if (hoursDetailsLabel != null) hoursDetailsLabel.setText("N/A");
             // updateKpiDetails(0, 0, 5000000.0, 6000000.0); // Gọi với giờ = 0
             return;
         }

         String maNV = currentUser.getNhanVien().getMaNV();
         long tongSoPhutLamThucTe = 0; // Tổng phút thực tế làm (có đi làm)
         double gioLamTheoLich = 0;   // Tổng giờ theo lịch (mục tiêu)

         LocalDate homNay = LocalDate.now();
         LocalDate dauThang = homNay.withDayOfMonth(1);
         LocalDate cuoiThang = homNay.withDayOfMonth(homNay.lengthOfMonth());

         try {
             // 1. Tính Giờ Mục Tiêu (Theo Lịch)
             gioLamTheoLich = caTrucDAO.getTongGioLamTheoLich(maNV, dauThang, cuoiThang);
             System.out.println("updateKpiGioLam: Tổng giờ theo lịch tháng này: " + gioLamTheoLich + " giờ.");

             // 2. Tính Giờ Thực Tế (Lặp lại logic của updateTheTongGioLam)
             List<CaTruc> caTrucThang = caTrucDAO.getCaTrucTrongThang(maNV, dauThang, cuoiThang);
             for (CaTruc ca : caTrucThang) {
                 LocalDate ngayLam = ca.getNgay();
                 if (!ngayLam.isAfter(homNay)) {
                     boolean coHoatDong = hoaDonDAO.kiemTraHoatDongNVTrongNgay(maNV, ngayLam);
                     if (coHoatDong) {
                         LocalTime batDau = ca.getGioBatDau();
                         LocalTime ketThuc = ca.getGioKetThuc();
                         Duration thoiLuongCa = Duration.between(batDau, ketThuc);
                         if (ketThuc.isBefore(batDau)) {
                             thoiLuongCa = thoiLuongCa.plusHours(24);
                         }
                         tongSoPhutLamThucTe += thoiLuongCa.toMinutes();
                     }
                 }
             }
             double gioLamThucTe = (double) tongSoPhutLamThucTe / 60.0;
             System.out.println("updateKpiGioLam: Tổng giờ làm thực tế (có HĐ): " + gioLamThucTe + " giờ.");

             // 3. Cập nhật biểu đồ và Label
             drawHoursChart(gioLamThucTe, gioLamTheoLich);
             // Giữ nguyên doanh thu giả khi gọi updateKpiDetails
             updateKpiDetails(gioLamThucTe, gioLamTheoLich, 5000000.0, 6000000.0);


         } catch (SQLException sqlEx) {
             System.err.println("Lỗi CSDL khi cập nhật KPI giờ làm: " + sqlEx.getMessage());
             drawHoursChart(0, 0); // Vẽ biểu đồ rỗng
             if (hoursDetailsLabel != null) hoursDetailsLabel.setText("Lỗi DB");
             sqlEx.printStackTrace();
         } catch (Exception e) {
             System.err.println("Lỗi khi cập nhật KPI giờ làm: " + e.getMessage());
             drawHoursChart(0, 0); // Vẽ biểu đồ rỗng
             if (hoursDetailsLabel != null) hoursDetailsLabel.setText("Lỗi");
             e.printStackTrace();
         }
     }

}
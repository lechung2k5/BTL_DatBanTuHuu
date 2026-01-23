package dao;

// ... imports (giữ nguyên) ...
import connect.ConnectDB;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.XYChart;
import ui.ThongKe.MonBanChay;
import ui.ThongKe.HoaDonThongKe;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ThongKeDAO {

	private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm a");
    private final DateTimeFormatter chartDateFormatter = DateTimeFormatter.ofPattern("E dd/MM");

    /** Lấy dữ liệu KPI cho một ngày cụ thể (Giữ nguyên) */
    public Map<String, Double> getKpisForDate(LocalDate date) {
        Map<String, Double> kpis = new HashMap<>(); kpis.put("totalInvoices", 0.0); kpis.put("totalRevenue", 0.0);
        String sql = """
            SELECT COUNT(DISTINCT h.maHD) AS TotalInvoices, ISNULL(SUM(ct.thanhTien), 0) AS TotalRevenue
            FROM HoaDon h LEFT JOIN ChiTietHoaDon ct ON h.maHD = ct.maHD
            WHERE h.trangThai = N'DaThanhToan' AND CAST(h.ngayLap AS DATE) = ? """;
        try (Connection conn = ConnectDB.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setObject(1, date); ResultSet rs = pstmt.executeQuery();
            if (rs.next()) { kpis.put("totalInvoices", rs.getDouble("TotalInvoices")); kpis.put("totalRevenue", rs.getDouble("TotalRevenue")); }
        } catch (SQLException e) { e.printStackTrace(); System.err.println("Lỗi khi lấy dữ liệu KPI cho ngày " + date + ": " + e.getMessage()); }
        return kpis;
    }

    /** Lấy danh sách hóa đơn và tổng (Giữ nguyên) */
    public Map<String, Object> getHoaDonThongKe(Integer ngay, Integer thang, Integer nam) {
        ObservableList<HoaDonThongKe> list = FXCollections.observableArrayList(); Map<String, Object> result = new HashMap<>(); result.put("list", list); result.put("totalInvoices", 0); result.put("totalRevenue", 0.0);
        String baseSql = """
            WITH FilteredData AS ( SELECT h.maHD, h.gioVao, ISNULL((SELECT SUM(ct.thanhTien) FROM ChiTietHoaDon ct WHERE ct.maHD = h.maHD), 0) AS TongTien
            FROM HoaDon h WHERE h.trangThai = N'DaThanhToan' """;
        StringBuilder whereClause = new StringBuilder(); List<Object> params = new ArrayList<>();
        if (nam != null) { whereClause.append(" AND YEAR(h.ngayLap) = ? "); params.add(nam); } if (thang != null) { whereClause.append(" AND MONTH(h.ngayLap) = ? "); params.add(thang); } if (ngay != null) { whereClause.append(" AND DAY(h.ngayLap) = ? "); params.add(ngay); }
        String listSql = baseSql + whereClause.toString() + ") SELECT * FROM FilteredData ORDER BY gioVao DESC"; String totalsSql = baseSql + whereClause.toString() + ") SELECT COUNT(*) AS TotalInvoices, ISNULL(SUM(TongTien), 0) AS TotalRevenue FROM FilteredData";
        try (Connection conn = ConnectDB.getConnection()) {
            try (PreparedStatement pstmtList = conn.prepareStatement(listSql)) { for (int i = 0; i < params.size(); i++) { pstmtList.setObject(i + 1, params.get(i)); } ResultSet rsList = pstmtList.executeQuery(); while (rsList.next()) { String maHD = rsList.getString("maHD"); java.sql.Timestamp tsGioVao = rsList.getTimestamp("gioVao"); String thoiGianVao = (tsGioVao != null) ? tsGioVao.toLocalDateTime().format(timeFormatter) : "N/A"; double tongTien = rsList.getDouble("TongTien"); list.add(new HoaDonThongKe(maHD, thoiGianVao, tongTien)); } }
            try (PreparedStatement pstmtTotals = conn.prepareStatement(totalsSql)) { for (int i = 0; i < params.size(); i++) { pstmtTotals.setObject(i + 1, params.get(i)); } ResultSet rsTotals = pstmtTotals.executeQuery(); if (rsTotals.next()) { result.put("totalInvoices", rsTotals.getInt("TotalInvoices")); result.put("totalRevenue", rsTotals.getDouble("TotalRevenue")); } }
        } catch (SQLException e) { e.printStackTrace(); System.err.println("Lỗi khi lấy dữ liệu hóa đơn thống kê: " + e.getMessage()); }
        result.put("list", list); return result;
    }

    /** Lấy Top 5 món bán chạy nhất (Giữ nguyên) */
    public ObservableList<MonBanChay> getMonBanChay(Integer ngay, Integer thang, Integer nam) {
        ObservableList<MonBanChay> list = FXCollections.observableArrayList();
        StringBuilder sql = new StringBuilder("""
            SELECT TOP 5 m.tenMon, SUM(ct.soLuong) AS TongSoLuong, SUM(ct.thanhTien) AS TongDoanhThu
            FROM ChiTietHoaDon ct JOIN MonAn m ON ct.maMon = m.maMon JOIN HoaDon h ON ct.maHD = h.maHD
            WHERE h.trangThai = N'DaThanhToan' """);
        List<Object> params = new ArrayList<>();
        if (nam != null) { sql.append(" AND YEAR(h.ngayLap) = ? "); params.add(nam); } if (thang != null) { sql.append(" AND MONTH(h.ngayLap) = ? "); params.add(thang); } if (ngay != null) { sql.append(" AND DAY(h.ngayLap) = ? "); params.add(ngay); }
        sql.append(" GROUP BY m.tenMon ORDER BY TongDoanhThu DESC ");
        try (Connection conn = ConnectDB.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) { pstmt.setObject(i + 1, params.get(i)); } ResultSet rs = pstmt.executeQuery(); int stt = 1;
            while (rs.next()) { String tenMon = rs.getString("tenMon"); int soLuong = rs.getInt("TongSoLuong"); double doanhThu = rs.getDouble("TongDoanhThu"); list.add(new MonBanChay(stt++, tenMon, soLuong, doanhThu)); }
        } catch (SQLException e) { e.printStackTrace(); System.err.println("Lỗi khi lấy dữ liệu món bán chạy: " + e.getMessage()); } return list;
    }

    /** Lấy dữ liệu doanh thu khu vực theo ngày trong tuần (Giữ nguyên) */
    public Map<String, XYChart.Series<String, Number>> getDoanhThuKhuVucTheoNgayTrongTuan(LocalDate startDate, LocalDate endDate) {
        Map<String, XYChart.Series<String, Number>> result = new LinkedHashMap<>(); result.put("Tầng trệt", new XYChart.Series<>()); result.put("Tầng 1", new XYChart.Series<>()); result.put("Phòng", new XYChart.Series<>());
        result.get("Tầng trệt").setName("Tầng trệt"); result.get("Tầng 1").setName("Tầng 1"); result.get("Phòng").setName("Phòng");
        Map<LocalDate, Map<String, Double>> dailyRevenue = new LinkedHashMap<>();
        for (int i = 0; i < 7; i++) { LocalDate currentDate = startDate.plusDays(i); Map<String, Double> areaRevenue = new HashMap<>(); areaRevenue.put("Tầng trệt", 0.0); areaRevenue.put("Tầng 1", 0.0); areaRevenue.put("Phòng", 0.0); dailyRevenue.put(currentDate, areaRevenue); }
        String sql = """
            SELECT CAST(h.ngayLap AS DATE) AS Ngay, b.viTri AS KhuVuc, ISNULL(SUM(ct.thanhTien), 0) AS DoanhThuNgay
            FROM HoaDon h JOIN Ban b ON h.maBan = b.maBan LEFT JOIN ChiTietHoaDon ct ON h.maHD = ct.maHD
            WHERE h.trangThai = N'DaThanhToan' AND h.ngayLap >= ? AND h.ngayLap < ? AND b.viTri IN (N'Tầng trệt', N'Tầng 1', N'Phòng riêng')
            GROUP BY CAST(h.ngayLap AS DATE), b.viTri ORDER BY Ngay """;
        try (Connection conn = ConnectDB.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setObject(1, startDate.atStartOfDay()); pstmt.setObject(2, endDate.plusDays(1).atStartOfDay());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) { LocalDate ngay = rs.getDate("Ngay").toLocalDate(); String khuVuc = rs.getString("KhuVuc"); double doanhThu = rs.getDouble("DoanhThuNgay"); if ("Phòng riêng".equals(khuVuc)) { khuVuc = "Phòng"; } if (dailyRevenue.containsKey(ngay) && dailyRevenue.get(ngay).containsKey(khuVuc)) { dailyRevenue.get(ngay).put(khuVuc, doanhThu); } }
        } catch (SQLException e) { e.printStackTrace(); System.err.println("Lỗi khi lấy doanh thu khu vực theo ngày: " + e.getMessage()); }
        dailyRevenue.forEach((date, areaRevenueMap) -> {
            String category = date.format(chartDateFormatter);
            result.get("Tầng trệt").getData().add(new XYChart.Data<>(category, areaRevenueMap.getOrDefault("Tầng trệt", 0.0))); result.get("Tầng 1").getData().add(new XYChart.Data<>(category, areaRevenueMap.getOrDefault("Tầng 1", 0.0))); result.get("Phòng").getData().add(new XYChart.Data<>(category, areaRevenueMap.getOrDefault("Phòng", 0.0)));
        });
        return result;
    }
    /**
     * HÀM MỚI: Lấy dữ liệu Tổng Doanh Thu theo từng ngày trong tuần
     * @param startDate Ngày bắt đầu tuần (Thứ 2)
     * @param endDate Ngày kết thúc tuần (Chủ nhật)
     * @return XYChart.Series chứa doanh thu mỗi ngày
     */
    public XYChart.Series<String, Number> getDoanhThuTheoNgayTrongTuan(LocalDate startDate, LocalDate endDate) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        // Khởi tạo Map với 7 ngày và doanh thu 0
        Map<LocalDate, Double> dailyRevenue = new LinkedHashMap<>();
        for (int i = 0; i < 7; i++) {
            dailyRevenue.put(startDate.plusDays(i), 0.0);
        }

        String sql = """
            SELECT
                CAST(h.ngayLap AS DATE) AS Ngay,
                ISNULL(SUM(ct.thanhTien), 0) AS DoanhThuNgay
            FROM HoaDon h
            LEFT JOIN ChiTietHoaDon ct ON h.maHD = ct.maHD
            WHERE h.trangThai = N'DaThanhToan'
              AND h.ngayLap >= ? AND h.ngayLap < ? -- Từ startDate đến trước endDate + 1
            GROUP BY CAST(h.ngayLap AS DATE)
            ORDER BY Ngay
            """;

        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setObject(1, startDate.atStartOfDay());
            pstmt.setObject(2, endDate.plusDays(1).atStartOfDay());

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                LocalDate ngay = rs.getDate("Ngay").toLocalDate();
                double doanhThu = rs.getDouble("DoanhThuNgay");
                // Cập nhật giá trị trong Map nếu ngày đó có trong tuần
                if (dailyRevenue.containsKey(ngay)) {
                    dailyRevenue.put(ngay, doanhThu);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Lỗi khi lấy doanh thu theo ngày trong tuần: " + e.getMessage());
        }

        // Đổ dữ liệu từ Map vào Series với định dạng trục X mong muốn
        dailyRevenue.forEach((date, revenue) -> {
            String category = date.format(chartDateFormatter); // Format "E dd/MM"
            series.getData().add(new XYChart.Data<>(category, revenue));
        });

        return series;
    }

} // End class ThongKeDAO
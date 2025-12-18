package dao;

import connect.ConnectDB;
import entity.*; // Import tất cả entity cho tiện

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HoaDonDAO {

    /**
     * Lấy tất cả hóa đơn từ CSDL cho màn hình quản lý hóa đơn.
     * === ĐÃ SỬA: JOIN với bảng UuDai để lấy % giảm giá và tính tổng tiền đúng ===
     */
    public List<HoaDon> getAllHoaDon() {
        List<HoaDon> danhSachHoaDon = new ArrayList<>();
        String sql = """
            SELECT
                h.maHD, h.ngayLap, h.ptThanhToan, h.trangThai, h.maUuDai, h.gioVao, h.gioRa, h.tienCoc,
                h.maKH, kh.soDT, h.maBan, h.maNV, n.tenNV,
                u.giaTri AS phanTramGiam, -- 🔥 LẤY % GIẢM GIÁ TỪ DB
                ISNULL((SELECT SUM(ct.thanhTien) FROM ChiTietHoaDon ct WHERE ct.maHD = h.maHD), 0) AS tongCongMonAn
            FROM HoaDon h
            LEFT JOIN KhachHang kh ON h.maKH = kh.maKH
            LEFT JOIN NhanVien n ON h.maNV = n.maNV
            LEFT JOIN UuDai u ON h.maUuDai = u.maUuDai -- 🔥 JOIN BẢNG ƯU ĐÃI
            WHERE h.trangThai = ?
            ORDER BY h.ngayLap DESC
        """;
        
        try (Connection conn = ConnectDB.getConnection(); 
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, TrangThaiHoaDon.DA_THANH_TOAN.getDbValue());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    HoaDon hoaDon = new HoaDon(); 
                    hoaDon.setMaHD(rs.getString("maHD")); 
                    
                    hoaDon.setNgayLap(rs.getTimestamp("ngayLap") != null ? rs.getTimestamp("ngayLap").toLocalDateTime() : null); 
                    
                    String ptttStr = rs.getString("ptThanhToan");
                    hoaDon.setHinhThucTT(ptttStr != null ? PTTThanhToan.fromDbValue(ptttStr) : null);
                    
                    String trangThaiStr = rs.getString("trangThai");
                    hoaDon.setTrangThai(trangThaiStr);
                    
                    hoaDon.setMaUuDai(rs.getString("maUuDai"));
                    hoaDon.setGioVao(rs.getTimestamp("gioVao") != null ? rs.getTimestamp("gioVao").toLocalDateTime() : null);
                    hoaDon.setGioRa(rs.getTimestamp("gioRa") != null ? rs.getTimestamp("gioRa").toLocalDateTime() : null);
                    hoaDon.setTienCoc(rs.getDouble("tienCoc"));
                    hoaDon.setTenNhanVien(rs.getString("tenNV"));
                    
                    // Lấy tổng tiền món ăn
                    double tongMon = rs.getDouble("tongCongMonAn");
                    hoaDon.setTongCongMonAn(tongMon);
                    
                    // 🔥 TÍNH TIỀN KHUYẾN MÃI DỰA TRÊN %
                    double phanTramGiam = rs.getDouble("phanTramGiam"); // Nếu null thì JDBC trả về 0
                    double tienKhuyenMai = tongMon * (phanTramGiam / 100.0);
                    hoaDon.setKhuyenMai(tienKhuyenMai);

                    // Tải Khách Hàng
                    if (rs.getString("maKH") != null) {
                        KhachHang kh = new KhachHang();
                        kh.setSoDT(rs.getString("soDT"));
                        hoaDon.setKhachHang(kh);
                    }
                    
                    // Tải Bàn
                    if (rs.getString("maBan") != null) {
                        Ban ban = new Ban();
                        ban.setMaBan(rs.getString("maBan"));
                        hoaDon.setBan(ban);
                    }

                    // 🔥 Gọi hàm tính toán lại để cập nhật tongTienThanhToan, VAT, Phí DV
                    hoaDon.calculateTotals(); 
                    
                    danhSachHoaDon.add(hoaDon);
                }
            }
        } catch (SQLException e) { 
            System.err.println("Lỗi khi lấy danh sách hóa đơn ĐÃ THANH TOÁN: " + e.getMessage()); 
            e.printStackTrace(); 
        }
        return danhSachHoaDon;
    }

    /**
     * Lấy chi tiết món ăn (entity ChiTietHoaDon) của một hóa đơn.
     */
    public List<ChiTietHoaDon> getChiTietHoaDon(String maHD) {
        List<ChiTietHoaDon> chiTietList = new ArrayList<>();
        String sql = """
            SELECT m.tenMon, ct.soLuong, m.giaBan, ct.thanhTien
            FROM ChiTietHoaDon ct JOIN MonAn m ON ct.maMon = m.maMon WHERE ct.maHD = ?
        """;
        try (Connection conn = ConnectDB.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, maHD);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    chiTietList.add(new ChiTietHoaDon(rs.getString("tenMon"), rs.getInt("soLuong"), rs.getDouble("giaBan"), rs.getDouble("thanhTien")));
                }
            }
        } catch (SQLException e) { System.err.println("Lỗi khi lấy chi tiết hóa đơn: " + e.getMessage()); e.printStackTrace(); }
        return chiTietList;
    }

    /**
     * Lấy danh sách hóa đơn của một khách hàng cụ thể.
     */
    public List<HoaDon> getHoaDonByMaKH(String maKH) {
        List<HoaDon> danhSachHoaDon = new ArrayList<>();
        if (maKH == null || maKH.isEmpty()) {
            return danhSachHoaDon; 
        }

        // 🔥 CŨNG CẦN JOIN ƯU ĐÃI ĐỂ LỊCH SỬ HIỆN ĐÚNG TIỀN
        String sql = """
            SELECT
                h.maHD, h.ngayLap, h.ptThanhToan, h.trangThai, h.tienCoc,
                u.giaTri AS phanTramGiam,
                ISNULL((SELECT SUM(ct.thanhTien) FROM ChiTietHoaDon ct WHERE ct.maHD = h.maHD), 0) AS tongCongMonAn
            FROM HoaDon h
            LEFT JOIN UuDai u ON h.maUuDai = u.maUuDai
            WHERE h.maKH = ?
            ORDER BY h.ngayLap DESC
        """;

        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, maKH);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                HoaDon hd = new HoaDon(); 

                hd.setMaHD(rs.getString("maHD"));
                String ptttStr = rs.getString("ptThanhToan");
                hd.setHinhThucTT(ptttStr != null ? PTTThanhToan.fromDbValue(ptttStr) : null);
                
                hd.setTrangThai(rs.getString("trangThai"));
                hd.setTienCoc(rs.getDouble("tienCoc"));

                Timestamp tsNgayLap = rs.getTimestamp("ngayLap");
                hd.setNgayLap( (tsNgayLap != null) ? tsNgayLap.toLocalDateTime() : null );

                double tongMon = rs.getDouble("tongCongMonAn");
                hd.setTongCongMonAn(tongMon);
                
                // Tính khuyến mãi
                double phanTram = rs.getDouble("phanTramGiam");
                hd.setKhuyenMai(tongMon * (phanTram / 100.0));
                
                hd.calculateTotals(); // Tính lại tổng

                danhSachHoaDon.add(hd);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy lịch sử hóa đơn cho KH " + maKH + ": " + e.getMessage());
            e.printStackTrace();
        }
        return danhSachHoaDon;
    }

    /**
     * Lấy thông tin chi tiết đầy đủ của một hóa đơn dựa vào mã hóa đơn.
     * 🔥 ĐÃ SỬA: JOIN VỚI BẢNG UUDAI
     */
    public HoaDon getHoaDonChiTietByMaHD(String maHD) {
        HoaDon hoaDon = null;
        if (maHD == null || maHD.isEmpty()) {
            return null;
        }

        String sql = """
            SELECT
                hd.maHD, hd.ngayLap, hd.maUuDai, hd.ptThanhToan, hd.trangThai, hd.gioVao, hd.gioRa, hd.tienCoc,
                hd.maKH, kh.tenKH, kh.soDT, kh.email AS khEmail, kh.ngayDangKy AS khNgayDK, kh.thanhVien AS khThanhVien, kh.diaChi AS khDiaChi,
                hd.maBan, b.viTri AS banViTri, b.sucChua AS banSucChua, b.loaiBan AS banLoaiBan, b.trangThai AS banTrangThai,
                hd.maNV, n.tenNV,
                u.giaTri AS phanTramGiam, -- 🔥 CỘT QUAN TRỌNG
                ISNULL((SELECT SUM(ct.thanhTien) FROM ChiTietHoaDon ct WHERE ct.maHD = hd.maHD), 0) AS tongCongMonAn
            FROM HoaDon hd
            LEFT JOIN KhachHang kh ON hd.maKH = kh.maKH
            LEFT JOIN Ban b ON hd.maBan = b.maBan
            LEFT JOIN NhanVien n ON hd.maNV = n.maNV
            LEFT JOIN UuDai u ON hd.maUuDai = u.maUuDai -- 🔥 JOIN ƯU ĐÃI
            WHERE hd.maHD = ?
        """;

        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maHD);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                hoaDon = new HoaDon();

                hoaDon.setMaHD(rs.getString("maHD"));
                hoaDon.setMaUuDai(rs.getString("maUuDai"));
                hoaDon.setTienCoc(rs.getDouble("tienCoc"));
                hoaDon.setTenNhanVien(rs.getString("tenNV"));
                
                String ptttStr = rs.getString("ptThanhToan");
                hoaDon.setHinhThucTT(ptttStr != null ? PTTThanhToan.fromDbValue(ptttStr) : null);
                
                hoaDon.setTrangThai(rs.getString("trangThai"));
                
                Timestamp tsNgayLap = rs.getTimestamp("ngayLap"); hoaDon.setNgayLap( (tsNgayLap != null) ? tsNgayLap.toLocalDateTime() : null );
                Timestamp tsGioVao = rs.getTimestamp("gioVao"); hoaDon.setGioVao( (tsGioVao != null) ? tsGioVao.toLocalDateTime() : null );
                Timestamp tsGioRa = rs.getTimestamp("gioRa"); hoaDon.setGioRa( (tsGioRa != null) ? tsGioRa.toLocalDateTime() : null );

                // Khách hàng
                if (rs.getString("maKH") != null) {
                    LocalDate ngayDK = rs.getObject("khNgayDK", LocalDate.class);
                    KhachHang kh = new KhachHang(
                        rs.getString("maKH"), rs.getString("tenKH"), rs.getString("soDT"),
                        rs.getString("khEmail"), ngayDK, rs.getString("khDiaChi"),
                        rs.getString("khThanhVien")
                    );
                    hoaDon.setKhachHang(kh);
                }

                // Bàn
                if (rs.getString("maBan") != null) {
                    Ban ban = new Ban(
                        rs.getString("maBan"), rs.getString("banViTri"), rs.getInt("banSucChua"),
                        LoaiBan.fromString(rs.getString("banLoaiBan")),
                        TrangThaiBan.fromDbValue(rs.getString("banTrangThai"))
                    );
                    hoaDon.setBan(ban);
                }

                // Tổng tiền & Khuyến mãi
                double tongMon = rs.getDouble("tongCongMonAn");
                hoaDon.setTongCongMonAn(tongMon);
                
                // 🔥 TÍNH KHUYẾN MÃI
                double phanTram = rs.getDouble("phanTramGiam");
                hoaDon.setKhuyenMai(tongMon * (phanTram / 100.0));
                
                hoaDon.calculateTotals(); // Tính tổng cuối
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy chi tiết hóa đơn " + maHD + ": " + e.getMessage());
            e.printStackTrace();
        }
        return hoaDon;
    }

    /**
     * Kiểm tra hoạt động NV trong ngày.
     */
    public boolean kiemTraHoatDongNVTrongNgay(String maNV, LocalDate ngay) throws SQLException {
        String sql = "SELECT COUNT(*) FROM HoaDon WHERE maNV = ? AND CAST(ngayLap AS DATE) = ?"; 
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maNV);
            ps.setDate(2, Date.valueOf(ngay)); 
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) return true;
            }
        } 
        return false; 
    }

    /**
     * Tính tổng doanh thu NV trong tháng.
     */
    public double getDoanhThuNhanVienTrongThang(String maNV, LocalDate dauThang, LocalDate cuoiThang) {
        double tongDoanhThu = 0.0;
        String sql = "SELECT SUM(cthd.thanhTien) " +
                     "FROM ChiTietHoaDon cthd " +
                     "JOIN HoaDon hd ON cthd.maHD = hd.maHD " +
                     "WHERE hd.maNV = ? " +
                     "AND hd.trangThai = ? " + 
                     "AND hd.ngayLap BETWEEN ? AND ?"; 

        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maNV);
            ps.setString(2, TrangThaiHoaDon.DA_THANH_TOAN.getDbValue()); 
            ps.setTimestamp(3, Timestamp.valueOf(dauThang.atStartOfDay()));
            ps.setTimestamp(4, Timestamp.valueOf(cuoiThang.atTime(23, 59, 59)));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    tongDoanhThu = rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tongDoanhThu;
    }

    /**
     * 🔥 HÀM ĐÃ SỬA: Tính tổng tiền mặt thu được (Khớp với HoaDon.java)
     * Đã kiểm tra tên cột 'giaTri' (chữ thường) trong bảng UuDai.
     */
    public double getTongTienMatTrongNgay(String maNV, LocalDate ngay) {
        double tongTienMatThucTe = 0.0;
        // Công thức SQL: (Món * 1.05 * 1.08) - Cọc - (Món * %KM)
        // Lưu ý: Logic này tương đương với: Món + (Món*0.05) + (Món*0.08) - Cọc - KM
        String sql = """
            SELECT SUM(
                       ISNULL(cthd_sum.TongMonAn, 0) 
                       + (ISNULL(cthd_sum.TongMonAn, 0) * 0.05) -- + Phí DV
                       + (ISNULL(cthd_sum.TongMonAn, 0) * 0.08) -- + VAT (8% trên món)
                       - hd.tienCoc 
                       - ISNULL(ud.giaTri / 100.0 * ISNULL(cthd_sum.TongMonAn, 0), 0) -- - Khuyến mãi (giaTri thường)
                   ) AS TongTienMatTinhLai
            FROM HoaDon hd
            LEFT JOIN (
                SELECT maHD, SUM(thanhTien) as TongMonAn
                FROM ChiTietHoaDon
                GROUP BY maHD
            ) cthd_sum ON hd.maHD = cthd_sum.maHD
            LEFT JOIN UuDai ud ON hd.maUuDai = ud.maUuDai 
            WHERE hd.maNV = ?                
              AND hd.trangThai = ?           
              AND hd.ptThanhToan = ?         
              AND CAST(hd.ngayLap AS DATE) = ? 
        """;

        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maNV);
            ps.setString(2, TrangThaiHoaDon.DA_THANH_TOAN.getDbValue());
            ps.setString(3, PTTThanhToan.TIEN_MAT.getDbValue());
            ps.setDate(4, Date.valueOf(ngay));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    tongTienMatThucTe = rs.getDouble(1);
                    if (tongTienMatThucTe < 0) tongTienMatThucTe = 0;
                }
            }
        } catch (SQLException e) {
             System.err.println("Lỗi khi tính tổng tiền mặt: " + e.getMessage());
             e.printStackTrace();
        }
        return tongTienMatThucTe;
    }

    /**
     * Kiểm tra xem NV có xử lý hóa đơn TIỀN MẶT nào không.
     */
    public boolean kiemTraTienMatTrongNgay(String maNV, LocalDate ngay) throws SQLException {
        String sql = "SELECT COUNT(*) FROM HoaDon " +
                     "WHERE maNV = ? " +
                     "AND trangThai = ? " + 
                     "AND ptThanhToan = ? " + 
                     "AND CAST(ngayLap AS DATE) = ?";

        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maNV);
            ps.setString(2, TrangThaiHoaDon.DA_THANH_TOAN.getDbValue());
            ps.setString(3, PTTThanhToan.TIEN_MAT.getDbValue());
            ps.setDate(4, Date.valueOf(ngay));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return true;
                }
            }
        }
        return false;
    }
}
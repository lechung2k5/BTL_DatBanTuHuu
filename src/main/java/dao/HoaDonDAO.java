package dao;

import connect.ConnectDB;
import entity.*; // Import tất cả entity cho tiện

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HoaDonDAO {

    // (Giữ nguyên hàm getAllHoaDon() và getChiTietHoaDon(String maHD))
    /**
     * Lấy tất cả hóa đơn từ CSDL cho màn hình quản lý hóa đơn.
     */
    public List<HoaDon> getAllHoaDon() {
        // ... code của hàm getAllHoaDon() ...
         List<HoaDon> danhSachHoaDon = new ArrayList<>();
        String sql = """
            SELECT
                h.maHD, h.ngayLap, h.ptThanhToan, h.trangThai, h.maUuDai, h.gioVao, h.gioRa, h.tienCoc,
                h.maKH, kh.soDT, h.maBan, h.maNV, n.tenNV,
                ISNULL((SELECT SUM(ct.thanhTien) FROM ChiTietHoaDon ct WHERE ct.maHD = h.maHD), 0) AS tongCongMonAn
            FROM HoaDon h
            LEFT JOIN KhachHang kh ON h.maKH = kh.maKH
            LEFT JOIN NhanVien n ON h.maNV = n.maNV
            ORDER BY h.ngayLap DESC
        """;
        try (Connection conn = ConnectDB.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql); ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                HoaDon hd = new HoaDon();
                hd.setMaHD(rs.getString("maHD"));
                hd.setHinhThucTT(PTTThanhToan.fromDbValue(rs.getString("ptThanhToan")));
                hd.setTrangThai(TrangThaiHoaDon.fromDbValue(rs.getString("trangThai")));
                hd.setMaUuDai(rs.getString("maUuDai"));
                Timestamp tsNgayLap = rs.getTimestamp("ngayLap"); hd.setNgayLap( (tsNgayLap != null) ? tsNgayLap.toLocalDateTime() : null );
                Timestamp tsGioVao = rs.getTimestamp("gioVao"); hd.setGioVao( (tsGioVao != null) ? tsGioVao.toLocalDateTime() : null );
                Timestamp tsGioRa = rs.getTimestamp("gioRa"); hd.setGioRa( (tsGioRa != null) ? tsGioRa.toLocalDateTime() : null );
                String maKH = rs.getString("maKH");
                if (maKH != null) { KhachHang kh = new KhachHang(); kh.setMaKH(maKH); kh.setSoDT(rs.getString("soDT")); hd.setKhachHang(kh); }
                else { hd.setKhachHang(null); }
                 String maBan = rs.getString("maBan");
                if (maBan != null) { Ban ban = new Ban(); ban.setMaBan(maBan); hd.setBan(ban); }
                else { hd.setBan(null); }
                hd.setTenNhanVien(rs.getString("tenNV"));
                hd.setTienCoc(rs.getDouble("tienCoc"));
                hd.setTongCongMonAn(rs.getDouble("tongCongMonAn")); // Trigger calculation
                danhSachHoaDon.add(hd);
            }
        } catch (SQLException e) { System.err.println("Lỗi khi lấy danh sách hóa đơn: " + e.getMessage()); e.printStackTrace(); }
        return danhSachHoaDon;
    }

    /**
     * Lấy chi tiết món ăn (entity ChiTietHoaDon) của một hóa đơn.
     */
    public List<ChiTietHoaDon> getChiTietHoaDon(String maHD) {
        // ... code của hàm getChiTietHoaDon(String maHD) ...
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

    // --- HÀM MỚI ---
    /**
     * Lấy danh sách hóa đơn của một khách hàng cụ thể.
     * @param maKH Mã khách hàng.
     * @return List các đối tượng HoaDon.
     */
    public List<HoaDon> getHoaDonByMaKH(String maKH) {
        List<HoaDon> danhSachHoaDon = new ArrayList<>();
        if (maKH == null || maKH.isEmpty()) {
            return danhSachHoaDon; // Trả về rỗng nếu không có mã KH
        }

        // Lấy các thông tin cần thiết để hiển thị lịch sử
        // Bao gồm cả subquery tính tổng tiền món
        String sql = """
            SELECT
                h.maHD, h.ngayLap, h.ptThanhToan, h.trangThai, h.tienCoc,
                ISNULL((SELECT SUM(ct.thanhTien) FROM ChiTietHoaDon ct WHERE ct.maHD = h.maHD), 0) AS tongCongMonAn
            FROM HoaDon h
            WHERE h.maKH = ?
            ORDER BY h.ngayLap DESC
        """;

        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, maKH);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                HoaDon hd = new HoaDon(); // Dùng constructor rỗng

                hd.setMaHD(rs.getString("maHD"));
                hd.setHinhThucTT(PTTThanhToan.fromDbValue(rs.getString("ptThanhToan")));
                hd.setTrangThai(TrangThaiHoaDon.fromDbValue(rs.getString("trangThai")));
                hd.setTienCoc(rs.getDouble("tienCoc"));

                Timestamp tsNgayLap = rs.getTimestamp("ngayLap");
                hd.setNgayLap( (tsNgayLap != null) ? tsNgayLap.toLocalDateTime() : null );

                // Gán tổng tiền món ăn để entity tự tính tổng cuối
                hd.setTongCongMonAn(rs.getDouble("tongCongMonAn"));

                // Không cần set KhachHang, Ban, NhanVien cho view lịch sử này
                danhSachHoaDon.add(hd);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy lịch sử hóa đơn cho KH " + maKH + ": " + e.getMessage());
            e.printStackTrace();
        }
        return danhSachHoaDon;
    }
 // --- HÀM MỚI ---
    /**
     * Lấy thông tin chi tiết đầy đủ của một hóa đơn dựa vào mã hóa đơn.
     * Join với KhachHang, Ban, NhanVien.
     * @param maHD Mã hóa đơn cần lấy chi tiết.
     * @return Đối tượng HoaDon đầy đủ thông tin, hoặc null nếu không tìm thấy.
     */
    public HoaDon getHoaDonChiTietByMaHD(String maHD) {
        HoaDon hoaDon = null;
        if (maHD == null || maHD.isEmpty()) {
            return null;
        }

        // Đảm bảo các alias (AS ...) là chính xác
        String sql = """
            SELECT
                hd.maHD, hd.ngayLap, hd.maUuDai, hd.ptThanhToan, hd.trangThai, hd.gioVao, hd.gioRa, hd.tienCoc,
                hd.maKH, kh.tenKH, kh.soDT, kh.email AS khEmail, kh.ngayDangKy AS khNgayDK, kh.thanhVien AS khThanhVien, kh.diaChi AS khDiaChi,
                hd.maBan, b.viTri AS banViTri, b.sucChua AS banSucChua, b.loaiBan AS banLoaiBan, b.trangThai AS banTrangThai,
                hd.maNV, n.tenNV,
                ISNULL((SELECT SUM(ct.thanhTien) FROM ChiTietHoaDon ct WHERE ct.maHD = hd.maHD), 0) AS tongCongMonAn
            FROM HoaDon hd
            LEFT JOIN KhachHang kh ON hd.maKH = kh.maKH
            LEFT JOIN Ban b ON hd.maBan = b.maBan
            LEFT JOIN NhanVien n ON hd.maNV = n.maNV
            WHERE hd.maHD = ?
        """;

        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maHD);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                hoaDon = new HoaDon();

                // Thông tin Hóa đơn
                hoaDon.setMaHD(rs.getString("maHD"));
                hoaDon.setMaUuDai(rs.getString("maUuDai"));
                hoaDon.setTienCoc(rs.getDouble("tienCoc"));
                hoaDon.setTenNhanVien(rs.getString("tenNV"));
                hoaDon.setHinhThucTT(PTTThanhToan.fromDbValue(rs.getString("ptThanhToan")));
                hoaDon.setTrangThai(TrangThaiHoaDon.fromDbValue(rs.getString("trangThai")));
                Timestamp tsNgayLap = rs.getTimestamp("ngayLap"); hoaDon.setNgayLap( (tsNgayLap != null) ? tsNgayLap.toLocalDateTime() : null );
                Timestamp tsGioVao = rs.getTimestamp("gioVao"); hoaDon.setGioVao( (tsGioVao != null) ? tsGioVao.toLocalDateTime() : null );
                Timestamp tsGioRa = rs.getTimestamp("gioRa"); hoaDon.setGioRa( (tsGioRa != null) ? tsGioRa.toLocalDateTime() : null );

                // Thông tin Khách hàng
                if (rs.getString("maKH") != null) {
                    // ✅ SỬA LỖI Ở ĐÂY: Dùng getObject thay vì getDate().toLocalDate()
                    LocalDate ngayDK = rs.getObject("khNgayDK", LocalDate.class);
                    KhachHang kh = new KhachHang(
                        rs.getString("maKH"), rs.getString("tenKH"), rs.getString("soDT"),
                        rs.getString("khEmail"), ngayDK, rs.getString("khDiaChi"),
                        rs.getString("khThanhVien")
                    );
                    hoaDon.setKhachHang(kh);
                } else { hoaDon.setKhachHang(null); }

                // Thông tin Bàn
                if (rs.getString("maBan") != null) {
                    Ban ban = new Ban(
                        rs.getString("maBan"), rs.getString("banViTri"), rs.getInt("banSucChua"),
                        LoaiBan.fromString(rs.getString("banLoaiBan")),
                        TrangThaiBan.fromDbValue(rs.getString("banTrangThai"))
                    );
                    hoaDon.setBan(ban);
                } else { hoaDon.setBan(null); }

                // Tổng tiền món ăn
                hoaDon.setTongCongMonAn(rs.getDouble("tongCongMonAn"));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy chi tiết hóa đơn " + maHD + ": " + e.getMessage());
            e.printStackTrace();
        }
        return hoaDon;
    }
}
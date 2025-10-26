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
     * === ĐÃ SỬA: Chỉ lấy các hóa đơn có trạng thái "Đã thanh toán" ===
     */
    public List<HoaDon> getAllHoaDon() {
        List<HoaDon> danhSachHoaDon = new ArrayList<>();
        String sql = """
            SELECT
                h.maHD, h.ngayLap, h.ptThanhToan, h.trangThai, h.maUuDai, h.gioVao, h.gioRa, h.tienCoc,
                h.maKH, kh.soDT, h.maBan, h.maNV, n.tenNV,
                ISNULL((SELECT SUM(ct.thanhTien) FROM ChiTietHoaDon ct WHERE ct.maHD = h.maHD), 0) AS tongCongMonAn
            FROM HoaDon h
            LEFT JOIN KhachHang kh ON h.maKH = kh.maKH
            LEFT JOIN NhanVien n ON h.maNV = n.maNV
            WHERE h.trangThai = ? -- <<< THÊM ĐIỀU KIỆN LỌC
            ORDER BY h.ngayLap DESC
        """;
        
        // Sửa lại try-with-resources để gán tham số
        try (Connection conn = ConnectDB.getConnection(); 
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Gán giá trị cho điều kiện WHERE
            pstmt.setString(1, TrangThaiHoaDon.DA_THANH_TOAN.getDbValue()); //

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    HoaDon hoaDon = new HoaDon(); 
                    hoaDon.setMaHD(rs.getString("maHD")); 
                    
                    hoaDon.setNgayLap(rs.getTimestamp("ngayLap") != null ? rs.getTimestamp("ngayLap").toLocalDateTime() : null); 
                    
                    String ptttStr = rs.getString("ptThanhToan");
                    if (ptttStr != null) {
                        hoaDon.setHinhThucTT(PTTThanhToan.fromDbValue(ptttStr)); 
                    } else {
                        hoaDon.setHinhThucTT((PTTThanhToan) null); 
                    }
                    
                    String trangThaiStr = rs.getString("trangThai");
                    if (trangThaiStr != null) {
                        hoaDon.setTrangThai(trangThaiStr); 
                    } else {
                        hoaDon.setTrangThai((String) null); 
                    }
                    
                    hoaDon.setMaUuDai(rs.getString("maUuDai"));
                    hoaDon.setGioVao(rs.getTimestamp("gioVao") != null ? rs.getTimestamp("gioVao").toLocalDateTime() : null);
                    hoaDon.setGioRa(rs.getTimestamp("gioRa") != null ? rs.getTimestamp("gioRa").toLocalDateTime() : null);
                    hoaDon.setTienCoc(rs.getDouble("tienCoc"));
                    hoaDon.setTenNhanVien(rs.getString("tenNV"));
                    hoaDon.setTongCongMonAn(rs.getDouble("tongCongMonAn"));
                    
                    // ... (logic load KhachHang, Ban sẽ là null nếu không join, 
                    // nhưng logic trong entity HoaDon.java đã xử lý việc này)
                    
                    // Tải Khách Hàng (chỉ cần SĐT cho UI này)
                    if (rs.getString("maKH") != null) {
                        KhachHang kh = new KhachHang();
                        kh.setSoDT(rs.getString("soDT"));
                        hoaDon.setKhachHang(kh);
                    }
                    
                    // Tải Bàn (chỉ cần mã bàn)
                    if (rs.getString("maBan") != null) {
                        Ban ban = new Ban();
                        ban.setMaBan(rs.getString("maBan"));
                        hoaDon.setBan(ban);
                    }

                    hoaDon.calculateTotals(); // Tính toán tổng tiền
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
                // Sửa lỗi mơ hồ PTTThanhToan
                String ptttStr = rs.getString("ptThanhToan");
                hd.setHinhThucTT(ptttStr != null ? PTTThanhToan.fromDbValue(ptttStr) : (PTTThanhToan) null);
                
                // Sửa lỗi mơ hồ TrangThai
                String trangThaiStr = rs.getString("trangThai");
                hd.setTrangThai(trangThaiStr); // Sử dụng setter String
                
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
                
                // Sửa lỗi mơ hồ PTTThanhToan
                String ptttStr = rs.getString("ptThanhToan");
                hoaDon.setHinhThucTT(ptttStr != null ? PTTThanhToan.fromDbValue(ptttStr) : (PTTThanhToan) null);
                
                // Sửa lỗi mơ hồ TrangThai
                String trangThaiStr = rs.getString("trangThai");
                hoaDon.setTrangThai(trangThaiStr);
                
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
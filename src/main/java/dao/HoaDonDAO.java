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
// Trong HoaDonDAO.java
    
    /**
     * 🔥 HÀM MỚI (Thay thế LSĐN): Kiểm tra xem một nhân viên có thực hiện
     * thanh toán hóa đơn nào trong một ngày cụ thể hay không.
     * @param maNV Mã nhân viên thu ngân cần kiểm tra.
     * @param ngay Ngày cần kiểm tra (LocalDate).
     * @return true nếu có ít nhất một hóa đơn được xử lý bởi NV đó trong ngày, false nếu không.
     * @throws SQLException Nếu có lỗi truy vấn CSDL.
     */
    public boolean kiemTraHoatDongNVTrongNgay(String maNV, LocalDate ngay) throws SQLException {
        // Kiểm tra cột maNV trong bảng HoaDon, lọc theo ngày (CAST ngày)
        // Chỉ cần COUNT(*) > 0 là đủ
        String sql = "SELECT COUNT(*) FROM HoaDon WHERE maNV = ? AND CAST(ngayLap AS DATE) = ?"; 
        // Lưu ý: Có thể dùng gioRa thay cho ngayLap nếu logic của bạn phù hợp hơn

        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
             
            ps.setString(1, maNV);
            ps.setDate(2, Date.valueOf(ngay)); // Chuyển LocalDate sang java.sql.Date
            
            try (ResultSet rs = ps.executeQuery()) {
                // Nếu có kết quả và COUNT(*) > 0 thì tức là có hoạt động
                if (rs.next() && rs.getInt(1) > 0) {
                    return true;
                }
            }
        } 
        // Nếu không tìm thấy hoặc có lỗi, coi như không hoạt động
        return false; 
    }
    /**
     * 🔥 HÀM MỚI: Tính tổng doanh thu (từ ChiTietHoaDon) của các hóa đơn
     * mà một nhân viên đã xử lý (thanh toán) trong một khoảng thời gian (tháng).
     * @param maNV Mã nhân viên thu ngân.
     * @param dauThang Ngày đầu tiên của tháng.
     * @param cuoiThang Ngày cuối cùng của tháng.
     * @return Tổng doanh thu (double).
     */
    public double getDoanhThuNhanVienTrongThang(String maNV, LocalDate dauThang, LocalDate cuoiThang) {
        double tongDoanhThu = 0.0;
        // Sum thanhTien từ CTHD, join với HoaDon để lọc theo maNV và ngày
        // Chỉ tính những hóa đơn đã thanh toán (trangThai = 'DaThanhToan')
        String sql = "SELECT SUM(cthd.thanhTien) " +
                     "FROM ChiTietHoaDon cthd " +
                     "JOIN HoaDon hd ON cthd.maHD = hd.maHD " +
                     "WHERE hd.maNV = ? " +
                     "AND hd.trangThai = ? " + // Chỉ tính HĐ đã thanh toán
                     "AND hd.ngayLap BETWEEN ? AND ?"; // Lọc theo ngày lập hóa đơn

        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maNV);
            ps.setString(2, TrangThaiHoaDon.DA_THANH_TOAN.getDbValue()); // Lọc HĐ đã thanh toán
            // Chuyển LocalDate sang Timestamp (hoặc Date nếu cột ngayLap là DATE)
            // Lấy thời điểm đầu ngày và cuối ngày để bao trọn cả tháng
            ps.setTimestamp(3, Timestamp.valueOf(dauThang.atStartOfDay()));
            ps.setTimestamp(4, Timestamp.valueOf(cuoiThang.atTime(23, 59, 59)));

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Lấy kết quả SUM, nếu là NULL (không có HĐ nào) thì trả về 0
                    tongDoanhThu = rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
             System.err.println("Lỗi khi tính tổng doanh thu của NV " + maNV + ": " + e.getMessage());
            e.printStackTrace();
        }
        return tongDoanhThu;
    }
 // Trong HoaDonDAO.java

    /**
     * 🔥 HÀM ĐÃ SỬA: Tính tổng tiền mặt thu được dựa trên công thức tính
     * TONGTIENTHANHTOAN của entity HoaDon (bao gồm phí, VAT như entity tính).
     * @param maNV Mã nhân viên thu ngân.
     * @param ngay Ngày cần tính toán.
     * @return Tổng số tiền mặt thực tế thu được (tính theo logic entity).
     */
    public double getTongTienMatTrongNgay(String maNV, LocalDate ngay) {
        double tongTienMatThucTe = 0.0;
        // Tái tạo lại phép tính tongTienThanhToan từ HoaDon.java trong SQL:
        // tongTienThanhToan = tongCongMonAn + phiDichVu + thueVAT - tienCoc - khuyenMai;
        // trong đó:
        // phiDichVu = tongCongMonAn * 0.05;
        // thueVAT = tongCongMonAn * 0.08; (Theo HoaDon.java)
        String sql = """
            SELECT SUM(
                       ISNULL(cthd_sum.TongMonAn, 0) -- tongCongMonAn
                       + (ISNULL(cthd_sum.TongMonAn, 0) * 0.05) -- + phiDichVu
                       + (ISNULL(cthd_sum.TongMonAn, 0) * 0.08) -- + thueVAT (Theo cách tính của HoaDon.java)
                       - hd.tienCoc -- - tienCoc
                       - ISNULL(ud.GiaTri / 100.0 * ISNULL(cthd_sum.TongMonAn, 0), 0) -- - khuyenMai (% trên TongMonAn)
                   ) AS TongTienMatTinhLai
            FROM HoaDon hd
            LEFT JOIN (
                -- Tính tổng tiền món cho mỗi hóa đơn
                SELECT maHD, SUM(thanhTien) as TongMonAn
                FROM ChiTietHoaDon
                GROUP BY maHD
            ) cthd_sum ON hd.maHD = cthd_sum.maHD
            LEFT JOIN UuDai ud ON hd.maUuDai = ud.MaUuDai -- Join để lấy giá trị khuyến mãi
            WHERE hd.maNV = ?                -- Lọc theo nhân viên
              AND hd.trangThai = ?           -- Lọc HĐ đã thanh toán
              AND hd.ptThanhToan = ?         -- Lọc thanh toán tiền mặt
              AND CAST(hd.ngayLap AS DATE) = ? -- Lọc theo ngày
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
                    // Đảm bảo không âm
                    if (tongTienMatThucTe < 0) tongTienMatThucTe = 0;
                }
            }
        } catch (SQLException e) {
             System.err.println("Lỗi khi tính tổng tiền mặt (theo entity) trong ngày của NV " + maNV + ": " + e.getMessage());
             e.printStackTrace();
        }
        return tongTienMatThucTe;
    }
 // Trong HoaDonDAO.java

    /**
     * 🔥 HÀM MỚI: Kiểm tra xem NV có xử lý hóa đơn TIỀN MẶT nào trong ngày không.
     * Dùng để xác định xem có cần hiển thị ô nhập kiểm kê đầu ca hay không.
     * @param maNV Mã nhân viên.
     * @param ngay Ngày cần kiểm tra.
     * @return true nếu có ít nhất 1 hóa đơn tiền mặt, false nếu không.
     * @throws SQLException Lỗi CSDL.
     */
    public boolean kiemTraTienMatTrongNgay(String maNV, LocalDate ngay) throws SQLException {
        // Chỉ cần kiểm tra sự tồn tại (COUNT > 0)
        String sql = "SELECT COUNT(*) FROM HoaDon " +
                     "WHERE maNV = ? " +
                     "AND trangThai = ? " + // Đã thanh toán
                     "AND ptThanhToan = ? " + // Bằng tiền mặt
                     "AND CAST(ngayLap AS DATE) = ?";

        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maNV);
            ps.setString(2, TrangThaiHoaDon.DA_THANH_TOAN.getDbValue());
            ps.setString(3, PTTThanhToan.TIEN_MAT.getDbValue());
            ps.setDate(4, Date.valueOf(ngay));

            try (ResultSet rs = ps.executeQuery()) {
                // Nếu có kết quả và COUNT(*) > 0 thì return true
                if (rs.next() && rs.getInt(1) > 0) {
                    return true;
                }
            }
        }
        // Mặc định false nếu không tìm thấy hoặc lỗi
        return false;
    }
    
}
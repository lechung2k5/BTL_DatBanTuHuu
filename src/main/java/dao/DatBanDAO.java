package dao;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import entity.Ban;
import entity.LoaiBan;
import entity.TrangThaiBan;
import connect.ConnectDB;
import entity.HoaDon; 
import entity.KhachHang; 
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ui.DatBan.MonOrder;

public class DatBanDAO {

    /**
     * Lấy mã Hóa đơn tiếp theo để tự động tăng (HDxxx)
     */
    public String getNextMaHD() {
        String maHD = "HD001";
        String sql = "SELECT MAX(maHD) FROM HoaDon";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                String maxMaHD = rs.getString(1);
                if (maxMaHD != null) {
                    int num = Integer.parseInt(maxMaHD.substring(2)) + 1;
                    maHD = String.format("HD%03d", num);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return maHD;
    }
    
    /**
     * Lấy mã Khách hàng tiếp theo để tự động tăng (KHxxx)
     */
    public String getNextMaKH() {
        String maKH = "KH001";
        String sql = "SELECT MAX(maKH) FROM KhachHang";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                String maxMaKH = rs.getString(1);
                if (maxMaKH != null) {
                    int num = Integer.parseInt(maxMaKH.substring(2)) + 1;
                    maKH = String.format("KH%03d", num);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return maKH;
    }

    /**
     * Tìm hoặc tạo khách hàng dựa trên SĐT.
     * Nếu tìm thấy -> Lấy thông tin đầy đủ của Khách hàng đó.
     * Nếu không tìm thấy -> Tạo bản ghi tối thiểu (Guest) và trả về.
     */
    public KhachHang timHoacTaoKhachHang(String sdt, String tenKH) throws SQLException {
        // 🔥 SỬA: Truy vấn TẤT CẢ các cột để dùng Constructor 7 tham số.
        String selectSql = "SELECT maKH, tenKH, soDT, email, ngayDangKy, thanhVien, diaChi FROM KhachHang WHERE soDT = ?";
        
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement psSelect = con.prepareStatement(selectSql)) {

            psSelect.setString(1, sdt);
            
            try (ResultSet rs = psSelect.executeQuery()) {
                if (rs.next()) {
                    // TRƯỜNG HỢP 1: Khách hàng ĐÃ TỒN TẠI (Tự động link tên)
                    LocalDate ngayDangKyFromDB = rs.getDate("ngayDangKy") != null ? rs.getDate("ngayDangKy").toLocalDate() : null;
                    
                    // Sử dụng constructor 7 tham số: (maKH, tenKH, soDT, email, ngayDangKy, diaChi, thanhVien)
                    return new KhachHang(
                        rs.getString("maKH"),
                        rs.getString("tenKH"),
                        rs.getString("soDT"),
                        rs.getString("email"),
                        ngayDangKyFromDB,
                        rs.getString("diaChi"),
                        rs.getString("thanhVien")
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e; 
        }
        
        // TRƯỜNG HỢP 2: Khách hàng CHƯA TỒN TẠI -> Tạo mới tối thiểu (Guest)
        try (Connection con = ConnectDB.getConnection()) {
            String newMaKH = getNextMaKH();
            String tenKhach = tenKH.isEmpty() ? "Khách vãng lai" : tenKH;
            LocalDate ngayHienTai = LocalDate.now();
            
            // Chèn bản ghi tối thiểu: maKH, tenKH, soDT, ngayDangKy (ngày tạo), thanhVien='Guest'
            // Chỉ chèn 5 cột bắt buộc/cơ bản. email và diaChi là NULL.
            String insertSql = "INSERT INTO KhachHang (maKH, tenKH, soDT, ngayDangKy, thanhVien) " +
                               "VALUES (?, ?, ?, ?, ?)";

            try (PreparedStatement psInsert = con.prepareStatement(insertSql)) {
                psInsert.setString(1, newMaKH);
                psInsert.setNString(2, tenKhach);
                psInsert.setString(3, sdt);
                psInsert.setDate(4, Date.valueOf(ngayHienTai)); 
                psInsert.setNString(5, "Guest"); 
                psInsert.executeUpdate();
                
                // Trả về đối tượng KhachHang mới tạo
                // Sử dụng constructor 7 tham số: (maKH, tenKH, soDT, email, ngayDangKy, diaChi, thanhVien)
                return new KhachHang(newMaKH, tenKhach, sdt, null, ngayHienTai, null, "Guest");
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tạo khách hàng mới: " + e.getMessage());
            throw e; 
        }
    }
    
    /**
     * Lưu Hóa đơn và Chi tiết Hóa đơn (Đơn đặt hàng/đến quán)
     */
    public void luuHoaDonVaChiTiet(HoaDon hoaDon, ObservableList<MonOrder> monOrderList) throws SQLException {
        String maHD = getNextMaHD();
        
        // 1. Lưu Hóa đơn chính
        String hdSql = "INSERT INTO HoaDon (maHD, ngayLap, trangThai, gioVao, maBan, maKH, tienCoc) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(hdSql)) {
            
            ps.setString(1, maHD);
            ps.setTimestamp(2, Timestamp.valueOf(hoaDon.getNgayLap()));
            ps.setString(3, hoaDon.getTrangThai());
            ps.setTimestamp(4, Timestamp.valueOf(hoaDon.getGioVao()));
            ps.setString(5, hoaDon.getBan().getMaBan());
            
            String maKH = null;
            if (hoaDon.getKhachHang() != null) {
                maKH = hoaDon.getKhachHang().getMaKH();
            }
            ps.setString(6, maKH);
            
            ps.setDouble(7, hoaDon.getTienCoc());
            
            ps.executeUpdate();
            
            // 2. Lưu Chi tiết Hóa đơn (Nếu có món ăn)
            if (!monOrderList.isEmpty()) {
                 String cthdSql = "INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, thanhTien) VALUES (?, ?, ?, ?)";
                 try (PreparedStatement psCt = con.prepareStatement(cthdSql)) {
                     for (MonOrder order : monOrderList) {
                         psCt.setString(1, maHD);
                         psCt.setString(2, order.getMaMon());
                         psCt.setInt(3, order.getSoLuong());
                         double thanhTien = order.getDonGia() * order.getSoLuong(); 
                         psCt.setDouble(4, thanhTien);
                         psCt.addBatch();
                     }
                     psCt.executeBatch();
                 }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            throw e; // Ném ngoại lệ để rollback nếu cần thiết
        }
    }

    /**
     * 🔥 NEW: Lấy chi tiết món ăn (MonOrder ViewModel) của một hóa đơn cụ thể.
     * @param maHD Mã hóa đơn
     * @return ObservableList<MonOrder>
     */
    public ObservableList<ui.DatBan.MonOrder> getChiTietHoaDon(String maHD) {
        ObservableList<ui.DatBan.MonOrder> list = FXCollections.observableArrayList();
        Connection con = connect.ConnectDB.getConnection();
        if (con == null) return list;

        String sql = """
            SELECT cthd.maMon, cthd.soLuong, m.tenMon, m.giaBan
            FROM ChiTietHoaDon cthd
            JOIN MonAn m ON cthd.maMon = m.maMon
            WHERE cthd.maHD = ?
        """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maHD);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String maMon = rs.getString("maMon");
                    String tenMon = rs.getString("tenMon");
                    int soLuong = rs.getInt("soLuong");
                    double giaBan = rs.getDouble("giaBan");
                    
                    // Sử dụng giá bán của món ăn làm đơn giá cho order
                    list.add(new ui.DatBan.MonOrder(maMon, tenMon, giaBan, soLuong)); 
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi tải chi tiết hóa đơn " + maHD + ": " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Bổ sung logic NOT IN cho cả trạng thái "Dat" và "DangSuDung"
     */
    public List<Ban> getBanTrongTheoGio(Timestamp gioDen) {
        List<Ban> list = new ArrayList<>();
        String sql = """
            SELECT b.*
            FROM Ban b
            WHERE b.maBan NOT IN (
                SELECT h.maBan
                FROM HoaDon h
                WHERE 
                    (h.trangThai IN ('DangSuDung', 'Dat')) AND
                    (h.gioVao <= ? AND (h.gioRa IS NULL OR h.gioRa > ?))
            )
        """;
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTimestamp(1, gioDen);
            ps.setTimestamp(2, gioDen);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                LoaiBan loaiBan = LoaiBan.fromString(rs.getString("loaiBan"));
                TrangThaiBan trangThai = TrangThaiBan.fromString(rs.getString("trangThai")); 

                Ban ban = new Ban(
                        rs.getString("maBan"),
                        rs.getString("viTri"),
                        rs.getInt("sucChua"),
                        loaiBan,
                        trangThai
                );
                list.add(ban);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Lấy tất cả bàn (dùng để load sơ đồ ban đầu)
     */
    public List<Ban> getAllBan() {
        List<Ban> list = new ArrayList<>();
        String sql = "SELECT * FROM Ban";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                LoaiBan loaiBan = LoaiBan.fromString(rs.getString("loaiBan"));
                TrangThaiBan trangThai = TrangThaiBan.fromString(rs.getString("trangThai")); 

                Ban ban = new Ban(
                        rs.getString("maBan"),
                        rs.getString("viTri"),
                        rs.getInt("sucChua"),
                        loaiBan,
                        trangThai
                );
                list.add(ban);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Trả về danh sách Hóa đơn Đã Đặt/Đang Sử Dụng trong ngày HOẶC đang hoạt động từ ngày hôm trước (Logic xuyên đêm).
     */
    public List<HoaDon> getDsDatBanHomNay(LocalDate date) {
        List<HoaDon> list = new ArrayList<>();
        
        String sql = """
            SELECT 
                hd.maHD, hd.trangThai, hd.gioVao, hd.gioRa, hd.maKH, hd.ngayLap, hd.maUuDai, hd.ptThanhToan, hd.tienCoc,
                b.maBan, b.viTri, b.sucChua, b.loaiBan, b.trangThai AS trangThaiBan,
                kh.tenKH, kh.soDT, kh.email, kh.ngayDangKy, kh.thanhVien, kh.diaChi
            FROM HoaDon hd
            JOIN Ban b ON hd.maBan = b.maBan
            LEFT JOIN KhachHang kh ON hd.maKH = kh.maKH
            WHERE 
                hd.trangThai IN ('DangSuDung', 'Dat')
                AND (
                    -- Điều kiện 1: Đơn hàng bắt đầu trong ngày được chọn
                    CAST(hd.gioVao AS DATE) = ? 
                    
                    -- Điều kiện 2: HOẶC là đơn Đang Sử Dụng bắt đầu từ ngày hôm trước (Xuyên đêm) 
                    -- Tức là, tìm những đơn có trạng thái DangSuDung và gioVao < ngày được chọn
                    OR (hd.trangThai = 'DangSuDung' AND CAST(hd.gioVao AS DATE) < ?)
                )
        """;
        
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setDate(1, java.sql.Date.valueOf(date));
            ps.setDate(2, java.sql.Date.valueOf(date)); 

            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                // 1. Tạo entity Ban
                LoaiBan loaiBan = LoaiBan.fromString(rs.getString("loaiBan"));
                TrangThaiBan trangThaiBan = TrangThaiBan.fromString(rs.getString("trangThaiBan"));
                Ban ban = new Ban(
                    rs.getString("maBan"),
                    rs.getString("viTri"),
                    rs.getInt("sucChua"),
                    loaiBan,
                    trangThaiBan
                );
                
                // 2. Tạo entity KhachHang
                KhachHang khachHang = null;
                if (rs.getString("maKH") != null) {
                    LocalDate ngayDangKyFromDB = rs.getDate("ngayDangKy") != null ? rs.getDate("ngayDangKy").toLocalDate() : null;

                    // SỬA: Dùng constructor 7 tham số mới: (maKH, tenKH, soDT, email, ngayDangKy, diaChi, thanhVien)
                    khachHang = new KhachHang(
                        rs.getString("maKH"),
                        rs.getString("tenKH"),
                        rs.getString("soDT"),
                        rs.getString("email"),
                        ngayDangKyFromDB, 
                        rs.getString("diaChi"),
                        rs.getString("thanhVien")
                    );
                }
                
                // 3. Tạo và ánh xạ entity HoaDon 
                HoaDon hoaDon = new HoaDon();
                hoaDon.setMaHD(rs.getString("maHD"));
                hoaDon.setTrangThai(rs.getString("trangThai"));
                hoaDon.setMaUuDai(rs.getString("maUuDai"));
                hoaDon.setPtThanhToan(rs.getString("ptThanhToan"));
                hoaDon.setKhachHang(khachHang);
                hoaDon.setBan(ban);
                
                // Ánh xạ các trường thời gian
                Timestamp ngayLapTimestamp = rs.getTimestamp("ngayLap");
                if (ngayLapTimestamp != null) {
                    hoaDon.setNgayLap(ngayLapTimestamp.toLocalDateTime());
                }

                Timestamp gioVaoTimestamp = rs.getTimestamp("gioVao");
                if (gioVaoTimestamp != null) {
                    hoaDon.setGioVao(gioVaoTimestamp.toLocalDateTime());
                }
                
                Timestamp gioRaTimestamp = rs.getTimestamp("gioRa");
                if (gioRaTimestamp != null) {
                    hoaDon.setGioRa(gioRaTimestamp.toLocalDateTime());
                }
                
                // Ánh xạ tiền cọc
                hoaDon.setTienCoc(rs.getDouble("tienCoc")); 

                list.add(hoaDon);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>(); 
        }
        return list;
    }

    /**
     * Cập nhật trạng thái bàn (Dat, DangSuDung, Trong, ...)
     */
    public void capNhatTrangThaiBan(String maBan, String trangThai) {
        String sql = "UPDATE Ban SET trangThai = ? WHERE maBan = ?";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, trangThai);
            ps.setString(2, maBan);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Khi thanh toán xong: cập nhật giờ ra và trạng thái hóa đơn
     */
    public void capNhatGioRaVaTrangThaiHoaDon(String maHD) {
        String sql = "UPDATE HoaDon SET gioRa = GETDATE(), trangThai = 'DaThanhToan' WHERE maHD = ?";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maHD);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Khi tạo hóa đơn mới (đặt bàn hoặc khách đến)
     */
    public void capNhatTrangThaiBanKhiTaoHoaDon(String maBan, boolean datTruoc) {
        String trangThai = datTruoc ? "Dat" : "DangSuDung";
        capNhatTrangThaiBan(maBan, trangThai);
    }
}
package dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

import connect.ConnectDB;
import entity.KhachHang; 
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class KhachHangDAO {

    /**
     * Lấy tất cả khách hàng từ CSDL.
     */
    public ObservableList<KhachHang> getAllKhachHang() {
        ObservableList<KhachHang> customerList = FXCollections.observableArrayList();
        String sql = "SELECT maKH, tenKH, soDT, email, ngayDangKy, diaChi, thanhVien FROM KhachHang";

        // Mở kết nối 1 lần
        try (Connection conn = ConnectDB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // Đọc dữ liệu cơ bản từ ResultSet
                String maKH = rs.getString("maKH");
                String tenKH = rs.getNString("tenKH");
                String soDT = rs.getString("soDT");
                String email = rs.getNString("email");
                
                LocalDate ngayDangKy = rs.getDate("ngayDangKy") != null ? rs.getDate("ngayDangKy").toLocalDate() : null;
                
                String diaChi = rs.getNString("diaChi");
                String storedTier = rs.getNString("thanhVien"); // Hạng đang lưu trong DB

                // === LOGIC MỚI THEO YÊU CẦU ===
                // 1. Tính tổng chi tiêu (dùng chung Connection)
                double tongChiTieu = getTongChiTieuByMaKH(maKH, conn);

                // 2. Quy đổi ra hạng mới
                String calculatedTier = calculateTierFromSpending(tongChiTieu);
                
                // 3. So sánh và cập nhật CSDL nếu hạng bị thay đổi
                if (storedTier == null || !storedTier.equalsIgnoreCase(calculatedTier)) {
                    updateTierInDB(maKH, calculatedTier, conn);
                    storedTier = calculatedTier; // Cập nhật biến để dùng cho đối tượng bên dưới
                }
                // ===============================

                // 4. Tạo đối tượng KhachHang với hạng ĐÚNG (storedTier đã được cập nhật)
                KhachHang kh = new KhachHang(maKH, tenKH, soDT, email, ngayDangKy, diaChi, storedTier);
                customerList.add(kh);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tải danh sách khách hàng: " + e.getMessage());
        }
        return customerList;
    }

    /**
     * Thêm một khách hàng mới vào CSDL
     */
    public boolean themKhachHang(KhachHang kh) { 
        String sql = "INSERT INTO KhachHang (maKH, tenKH, soDT, email, ngayDangKy, diaChi, thanhVien) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, kh.getMaKH());
            ps.setNString(2, kh.getTenKH());
            ps.setString(3, kh.getSoDT());
            ps.setNString(4, kh.getEmail());
            ps.setDate(5, kh.getNgayDangKy() != null ? Date.valueOf(kh.getNgayDangKy()) : null);
            ps.setNString(6, kh.getDiaChi());
            ps.setNString(7, kh.getThanhVien() != null ? kh.getThanhVien() : "Member");

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm khách hàng: " + e.getMessage());
            return false;
        }
    }

    /**
     * Cập nhật thông tin khách hàng trong CSDL
     */
    public boolean suaKhachHang(KhachHang kh) {
        String sql = "UPDATE KhachHang SET tenKH = ?, soDT = ?, email = ?, ngayDangKy = ?, diaChi = ?, thanhVien = ? " +
                     "WHERE maKH = ?";

        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setNString(1, kh.getTenKH());
            ps.setString(2, kh.getSoDT());
            ps.setNString(3, kh.getEmail());
            ps.setDate(4, kh.getNgayDangKy() != null ? Date.valueOf(kh.getNgayDangKy()) : null); 
            ps.setNString(5, kh.getDiaChi());
            ps.setNString(6, kh.getThanhVien());
            ps.setString(7, kh.getMaKH()); // Điều kiện WHERE

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi sửa khách hàng: " + e.getMessage());
            return false;
        }
    }

    /**
     * Xóa khách hàng khỏi CSDL bằng mã KH
     */
    public boolean xoaKhachHang(String maKH) {
        String sql = "DELETE FROM KhachHang WHERE maKH = ?";

        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, maKH);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa khách hàng: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Tạo mã khách hàng mới tự động (ví dụ: KH003 -> KH004)
     */
    public String getNewMaKH() {
        String newId = "KH001"; // Mặc định nếu bảng rỗng
        String sql = "SELECT MAX(maKH) FROM KhachHang";
        
        try (Connection conn = ConnectDB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                String maxMaKH = rs.getString(1);
                if (maxMaKH != null) {
                    int num = Integer.parseInt(maxMaKH.substring(2));
                    num++;
                    newId = "KH" + String.format("%03d", num);
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy mã KH mới: " + e.getMessage());
        }
        return newId;
    }
    
    // =================================================================
    // CÁC HÀM HELPER MỚI ĐỂ TÍNH TOÁN HẠNG THÀNH VIÊN
    // =================================================================

    /**
     * (Helper) Tính tổng chi tiêu của một khách hàng từ tất cả hóa đơn đã thanh toán.
     * @param maKH Mã khách hàng
     * @param conn Connection (để tái sử dụng trong vòng lặp)
     * @return Tổng số tiền đã chi tiêu
     */
    private double getTongChiTieuByMaKH(String maKH, Connection conn) {
        
        // 🔥 ĐÃ SỬA: Đảm bảo chuỗi SQL được nối chính xác và có chứa '?'.
        String sql = "SELECT SUM( " +
                     "    ( (ISNULL(T1.tongMonAn, 0) * 1.13) * (1 - (ISNULL(T1.phanTramGiam, 0) / 100.0)) ) " + // (Tổng món + phí + VAT) * (1 - %giảm)
                     "    - T1.tienCoc " +                                                                // TRỪ ĐI TIỀN CỌC
                     " ) AS tongChiTieuToanBo " +
                     "FROM ( " +
                     "    SELECT " +
                     "        HD.maHD, " +
                     "        HD.tienCoc, " +
                     "        SUM(ISNULL(CTHD.thanhTien, 0)) AS tongMonAn, " +
                     "        MAX(ISNULL(UD.giaTri, 0)) AS phanTramGiam " +
                     "    FROM " +
                     "        HoaDon HD " +
                     "    LEFT JOIN " +
                     "        ChiTietHoaDon CTHD ON HD.maHD = CTHD.maHD " +
                     "    LEFT JOIN " +
                     "        UuDai UD ON HD.maUuDai = UD.maUuDai " +
                     "    WHERE " +
                     "        HD.maKH = ? " + // <-- DẤU ? LÀ THAM SỐ (parameter) 1
                     "        AND HD.ptThanhToan IS NOT NULL " +
                     "    GROUP BY " +
                     "        HD.maHD, HD.tienCoc " +
                     ") AS T1";
        
        double tongChiTieu = 0.0;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            
            // Dòng này gây ra lỗi NẾU dấu '?' ở trên bị thiếu
            ps.setString(1, maKH); 
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    tongChiTieu = rs.getDouble(1); // Lấy cột 1 (tongChiTieuToanBo)
                }
            }
        } catch (SQLException e) {
            // Lỗi của bạn sẽ bị bắt ở đây
            System.err.println("Lỗi khi tính tổng chi tiêu cho " + maKH + ": " + e.getMessage());
        }
        return tongChiTieu;
    }

    /**
     * (Helper) Quy đổi tổng chi tiêu ra hạng thành viên theo quy tắc.
     * 1 điểm = 100.000 VND
     * Member: 0-199 điểm
     * Gold: 200-449 điểm
     * Diamond: 450+ điểm
     */
    private String calculateTierFromSpending(double tongChiTieu) {
        // Quy đổi tổng tiền ra điểm (không lưu điểm)
        int diemTichLuy = (int) (tongChiTieu / 100000.0);

        if (diemTichLuy >= 450) {
            return "Diamond";
        } else if (diemTichLuy >= 200) { // Từ 200 đến 449
            return "Gold";
        } else { // Dưới 200 (0-199)
            return "Member";
        }
    }

    /**
     * (Helper) Cập nhật hạng thành viên mới vào CSDL.
     */
    private void updateTierInDB(String maKH, String newTier, Connection conn) {
        String sql = "UPDATE KhachHang SET thanhVien = ? WHERE maKH = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setNString(1, newTier);
            ps.setString(2, maKH);
            ps.executeUpdate();
            System.out.println("LOG: Cập nhật hạng cho " + maKH + " -> " + newTier);
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật hạng thành viên cho " + maKH + ": " + e.getMessage());
        }
    }
}
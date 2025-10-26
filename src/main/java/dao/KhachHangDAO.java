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
     * Lấy tất cả khách hàng từ CSDL
     */
    public ObservableList<KhachHang> getAllKhachHang() {
        ObservableList<KhachHang> customerList = FXCollections.observableArrayList();
        // 🔥 SỬA SQL: Loại bỏ ngaySinh, chỉ truy vấn các cột có thật trong DB (7 cột)
        String sql = "SELECT maKH, tenKH, soDT, email, ngayDangKy, diaChi, thanhVien FROM KhachHang";

        try (Connection conn = ConnectDB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // Đọc dữ liệu từ ResultSet
                String maKH = rs.getString("maKH");
                String tenKH = rs.getNString("tenKH");
                String soDT = rs.getString("soDT");
                String email = rs.getNString("email");
                
                // Lấy ngayDangKy
                LocalDate ngayDangKy = rs.getDate("ngayDangKy") != null ? rs.getDate("ngayDangKy").toLocalDate() : null;
                
                String diaChi = rs.getNString("diaChi");
                String thanhVien = rs.getNString("thanhVien");

                // 🔥 SỬA CONSTRUCTOR: Dùng constructor 7 tham số mới
                KhachHang kh = new KhachHang(maKH, tenKH, soDT, email, ngayDangKy, diaChi, thanhVien);
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
        // 🔥 SỬA SQL: Chỉ có 7 cột và 7 tham số (?)
        String sql = "INSERT INTO KhachHang (maKH, tenKH, soDT, email, ngayDangKy, diaChi, thanhVien) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, kh.getMaKH());
            ps.setNString(2, kh.getTenKH());
            ps.setString(3, kh.getSoDT());
            ps.setNString(4, kh.getEmail());
            
            // Tham số 5: ngayDangKy
            ps.setDate(5, kh.getNgayDangKy() != null ? Date.valueOf(kh.getNgayDangKy()) : null);
            
            // Tham số 6 & 7: diaChi, thanhVien
            ps.setNString(6, kh.getDiaChi());
            ps.setNString(7, kh.getThanhVien());

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
        // 🔥 SỬA SQL: Loại bỏ ngaySinh
        String sql = "UPDATE KhachHang SET tenKH = ?, soDT = ?, email = ?, ngayDangKy = ?, diaChi = ?, thanhVien = ? " +
                     "WHERE maKH = ?";

        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setNString(1, kh.getTenKH());
            ps.setString(2, kh.getSoDT());
            ps.setNString(3, kh.getEmail());
            
            // Tham số 4: ngayDangKy
            ps.setDate(4, kh.getNgayDangKy() != null ? Date.valueOf(kh.getNgayDangKy()) : null); 
            
            // Tham số 5 & 6: diaChi, thanhVien
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
        // ... (Không thay đổi)
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
        // ... (Không thay đổi)
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
}
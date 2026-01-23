package dao;

import connect.ConnectDB;
import entity.UuDai;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class UuDaiDAO {
    
    // Lấy tất cả ưu đãi
    public List<UuDai> getAllUuDai() {
        List<UuDai> list = new ArrayList<>();
        String sql = "SELECT * FROM UuDai";
        
        try (Connection conn = ConnectDB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                UuDai uuDai = new UuDai(
                    rs.getString("maUuDai"),
                    rs.getString("tenUuDai"),
                    rs.getString("moTa"),
                    rs.getDouble("giaTri"),
                    rs.getDate("ngayBatDau").toLocalDate(),
                    rs.getDate("ngayKetThuc").toLocalDate()
                );
                list.add(uuDai);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách ưu đãi: " + e.getMessage());
            e.printStackTrace();
        }
        
        return list;
    }
    
    // Thêm ưu đãi mới
    public boolean themUuDai(UuDai uuDai) {
        String sql = "INSERT INTO UuDai (maUuDai, tenUuDai, moTa, giaTri, ngayBatDau, ngayKetThuc) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, uuDai.getMaUuDai());
            pstmt.setString(2, uuDai.getTenUuDai());
            pstmt.setString(3, uuDai.getMoTa());
            pstmt.setDouble(4, uuDai.getGiaTri());
            pstmt.setDate(5, Date.valueOf(uuDai.getNgayBatDau()));
            pstmt.setDate(6, Date.valueOf(uuDai.getNgayKetThuc()));
            
            int result = pstmt.executeUpdate();
            return result > 0;
            
        } catch (SQLException e) {
            System.err.println("Lỗi khi thêm ưu đãi: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Cập nhật ưu đãi
    public boolean capNhatUuDai(UuDai uuDai) {
        String sql = "UPDATE UuDai SET tenUuDai = ?, moTa = ?, giaTri = ?, " +
                     "ngayBatDau = ?, ngayKetThuc = ? WHERE maUuDai = ?";
        
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, uuDai.getTenUuDai());
            pstmt.setString(2, uuDai.getMoTa());
            pstmt.setDouble(3, uuDai.getGiaTri());
            pstmt.setDate(4, Date.valueOf(uuDai.getNgayBatDau()));
            pstmt.setDate(5, Date.valueOf(uuDai.getNgayKetThuc()));
            pstmt.setString(6, uuDai.getMaUuDai());
            
            int result = pstmt.executeUpdate();
            return result > 0;
            
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật ưu đãi: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Xóa ưu đãi
    public boolean xoaUuDai(String maUuDai) {
        String sql = "DELETE FROM UuDai WHERE maUuDai = ?";
        
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, maUuDai);
            int result = pstmt.executeUpdate();
            return result > 0;
            
        } catch (SQLException e) {
            System.err.println("Lỗi khi xóa ưu đãi: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    // Tìm ưu đãi theo mã
    public UuDai timUuDaiTheoMa(String maUuDai) {
        String sql = "SELECT * FROM UuDai WHERE maUuDai = ?";
        
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, maUuDai);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new UuDai(
                    rs.getString("maUuDai"),
                    rs.getString("tenUuDai"),
                    rs.getString("moTa"),
                    rs.getDouble("giaTri"),
                    rs.getDate("ngayBatDau").toLocalDate(),
                    rs.getDate("ngayKetThuc").toLocalDate()
                );
            }
            
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm ưu đãi: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    // Lọc ưu đãi theo trạng thái
    public List<UuDai> locUuDaiTheoTrangThai(String trangThai) {
        List<UuDai> allList = getAllUuDai();
        List<UuDai> filteredList = new ArrayList<>();
        
        if (trangThai.equals("Tất cả")) {
            return allList;
        }
        
        for (UuDai ud : allList) {
            if (ud.getTrangThai().equals(trangThai)) {
                filteredList.add(ud);
            }
        }
        
        return filteredList;
    }
    
    // Tự động tạo mã ưu đãi mới
    public String taoMaUuDaiMoi() {
        String sql = "SELECT TOP 1 maUuDai FROM UuDai ORDER BY maUuDai DESC";
        
        try (Connection conn = ConnectDB.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                String maCuoi = rs.getString("maUuDai");
                int soThuTu = Integer.parseInt(maCuoi.substring(2)) + 1;
                return String.format("UD%03d", soThuTu);
            } else {
                return "UD001";
            }
            
        } catch (SQLException e) {
            System.err.println("Lỗi khi tạo mã ưu đãi: " + e.getMessage());
            e.printStackTrace();
            return "UD001";
        }
    }
}
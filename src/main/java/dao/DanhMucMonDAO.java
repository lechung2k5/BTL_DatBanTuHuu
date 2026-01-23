package dao;

import connect.ConnectDB;
import entity.DanhMucMon;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DanhMucMonDAO {

    /**
     * Lấy TẤT CẢ danh mục món ăn.
     * @return Danh sách các DanhMucMon
     */
    public List<DanhMucMon> getAllDanhMuc() {
        List<DanhMucMon> list = new ArrayList<>();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            return list;
        }

        String sql = "SELECT maDM, tenDM FROM DanhMucMon";
        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String maDM = rs.getString("maDM");
                String tenDM = rs.getString("tenDM");
                list.add(new DanhMucMon(maDM, tenDM));
            }
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi lấy danh sách danh mục: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }
    
    /**
     * Lấy maDM từ tenDM. Rất quan trọng khi Lưu/Sửa
     * @param tenDM Tên danh mục (ví dụ: "Khai vị")
     * @return maDM (ví dụ: "DM001") hoặc null nếu không tìm thấy
     */
    public String getMaDMByTenDM(String tenDM) {
        String maDM = null;
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            return null;
        }

        // 🔥 SỬA LỖI: Bỏ ký tự N khỏi câu lệnh SQL.
        // Khi dùng setNString, JDBC sẽ tự thêm tiền tố N cho chuỗi Unicode.
        String sql = "SELECT maDM FROM DanhMucMon WHERE tenDM = ?"; 
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            // Sử dụng setNString cho cột NVARCHAR
            pstmt.setNString(1, tenDM);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    maDM = rs.getString("maDM");
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi tìm mã danh mục: " + e.getMessage());
            e.printStackTrace();
        }
        return maDM;
    }
    
     /**
     * Lấy tenDM từ maDM.
     * @param maDM Mã danh mục (ví dụ: "DM001")
     * @return tenDM (ví dụ: "Khai vị") hoặc null nếu không tìm thấy
     */
    public String getTenDMByMaDM(String maDM) {
        String tenDM = null;
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            return null;
        }

        String sql = "SELECT tenDM FROM DanhMucMon WHERE maDM = ?";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, maDM);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    tenDM = rs.getString("tenDM");
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi tìm tên danh mục: " + e.getMessage());
            e.printStackTrace();
        }
        return tenDM;
    }
}
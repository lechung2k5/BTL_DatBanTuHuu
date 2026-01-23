package dao;

import connect.ConnectDB;
import entity.Ban;
import entity.LoaiBan;
import entity.TrangThaiBan;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class BanDAO {

    /**
     * Lấy TẤT CẢ các bàn trong nhà hàng (không quan tâm trạng thái)
     * @return Danh sách tất cả các Ban
     */
    public List<Ban> getDanhSachBan() {
        List<Ban> dsBan = new ArrayList<>();
        String sql = "SELECT * FROM Ban";
        
        try (Connection con = ConnectDB.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Ban ban = new Ban();
                ban.setMaBan(rs.getString("maBan"));
                ban.setViTri(rs.getString("viTri"));
                ban.setSucChua(rs.getInt("sucChua"));
                
                // Chuyển đổi String từ CSDL -> Enum
                String loaiBanStr = rs.getString("loaiBan");
                ban.setLoaiBan(LoaiBan.fromString(loaiBanStr)); 
                
                String trangThaiStr = rs.getString("trangThai");
             

                dsBan.add(ban);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dsBan;
    }
    
    // (Có thể thêm các hàm khác như getBanTheoMa(String maBan) sau...)
}
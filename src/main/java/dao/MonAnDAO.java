package dao;

import connect.ConnectDB;
import entity.MonAn;

import java.sql.*;
import java.util.ArrayList;

public class MonAnDAO {

    /**
     * Lấy TẤT CẢ món ăn, bao gồm cả tên danh mục (JOIN)
     */
    public ArrayList<MonAn> getAllMonAn() {
        ArrayList<MonAn> dsMonAn = new ArrayList<>();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            return dsMonAn;
        }

        String sql = "SELECT m.maMon, m.tenMon, m.hinhAnh, m.giaBan, m.maDM, d.tenDM " +
                     "FROM MonAn m " +
                     "LEFT JOIN DanhMucMon d ON m.maDM = d.maDM";
        
        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                MonAn mon = extractMonAnFromResultSet(rs);
                dsMonAn.add(mon);
            }
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi lấy danh sách món ăn: " + e.getMessage());
            e.printStackTrace();
        }
        return dsMonAn;
    }
    
    /**
     * Lấy món ăn theo MÃ DANH MỤC (dùng để lọc)
     */
    public ArrayList<MonAn> getMonAnByDanhMuc(String maDanhMuc) {
        ArrayList<MonAn> dsMonAn = new ArrayList<>();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            return dsMonAn;
        }

        String sql = "SELECT m.maMon, m.tenMon, m.hinhAnh, m.giaBan, m.maDM, d.tenDM " +
                     "FROM MonAn m " +
                     "LEFT JOIN DanhMucMon d ON m.maDM = d.maDM " +
                     "WHERE m.maDM = ?";
        
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, maDanhMuc);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    MonAn mon = extractMonAnFromResultSet(rs);
                    dsMonAn.add(mon);
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi lọc món ăn: " + e.getMessage());
            e.printStackTrace();
        }
        return dsMonAn;
    }

    /**
     * Thêm món ăn mới (với hình ảnh)
     */
    public boolean addMonAn(MonAn ma) {
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            return false;
        }

        String sql = "INSERT INTO MonAn (maMon, tenMon, hinhAnh, giaBan, maDM) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, ma.getMaMon());
            pstmt.setString(2, ma.getTenMon());
            
            if (ma.getHinhAnh() != null) {
                pstmt.setBytes(3, ma.getHinhAnh());
            } else {
                pstmt.setNull(3, Types.VARBINARY);
            }
            pstmt.setDouble(4, ma.getGiaBan());
            pstmt.setString(5, ma.getMaDM());

            int n = pstmt.executeUpdate();
            return n > 0;
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi thêm món ăn: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Cập nhật món ăn
     */
    public boolean updateMonAn(MonAn ma) {
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            return false;
        }

        String sql;
        boolean coHinhAnh = (ma.getHinhAnh() != null && ma.getHinhAnh().length > 0);

        if (coHinhAnh) {
            // Cập nhật CÓ hình ảnh
            sql = "UPDATE MonAn SET tenMon = ?, hinhAnh = ?, giaBan = ?, maDM = ? WHERE maMon = ?";
        } else {
            // Cập nhật KHÔNG hình ảnh (giữ ảnh cũ)
            sql = "UPDATE MonAn SET tenMon = ?, giaBan = ?, maDM = ? WHERE maMon = ?";
        }

        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            if (coHinhAnh) {
                pstmt.setString(1, ma.getTenMon());
                pstmt.setBytes(2, ma.getHinhAnh());
                pstmt.setDouble(3, ma.getGiaBan());
                pstmt.setString(4, ma.getMaDM());
                pstmt.setString(5, ma.getMaMon());
            } else {
                pstmt.setString(1, ma.getTenMon());
                pstmt.setDouble(2, ma.getGiaBan());
                pstmt.setString(3, ma.getMaDM());
                pstmt.setString(4, ma.getMaMon());
            }

            int n = pstmt.executeUpdate();
            return n > 0;
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi cập nhật món ăn: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Xóa món ăn
     */
    public boolean deleteMonAn(String maMon) {
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            return false;
        }
        
        String sql = "DELETE FROM MonAn WHERE maMon = ?";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, maMon);

            int n = pstmt.executeUpdate();
            return n > 0;
        } catch (SQLException e) {
            if (e.getErrorCode() == 547) {
                 System.err.println("❌ Lỗi: Không thể xóa món ăn đã có trong hóa đơn.");
            } else {
                 System.err.println("❌ Lỗi khi xóa món ăn: " + e.getMessage());
                 e.printStackTrace();
            }
            return false;
        }
    }

    /**
     * Tạo mã món ăn tiếp theo (ví dụ: MA005 -> MA006)
     */
    public String getNextMaMon() {
        String nextMaMon = "MA001";
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            return nextMaMon;
        }

        String sql = "SELECT TOP 1 maMon FROM MonAn ORDER BY maMon DESC";
        try (Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                String lastMaMon = rs.getString("maMon");
                int lastNum = Integer.parseInt(lastMaMon.substring(2));
                int nextNum = lastNum + 1;
                nextMaMon = "MA" + String.format("%03d", nextNum);
            }
        } catch (Exception e) {
             System.err.println("❌ Lỗi khi lấy mã món ăn tiếp theo: " + e.getMessage());
            e.printStackTrace();
        }
        return nextMaMon;
    }
    
    // Hàm tiện ích để đọc ResultSet
    private MonAn extractMonAnFromResultSet(ResultSet rs) throws SQLException {
        String maMon = rs.getString("maMon");
        String tenMon = rs.getString("tenMon");
        byte[] hinhAnh = rs.getBytes("hinhAnh");
        double giaBan = rs.getDouble("giaBan");
        String maDM = rs.getString("maDM");
        String tenDM = rs.getString("tenDM");

        MonAn mon = new MonAn(maMon, tenMon, hinhAnh, giaBan, maDM);
        mon.setTenDanhMuc(tenDM);
        return mon;
    }

    // ==========================================================
    // === PHẦN MỚI: Thêm hàm tìm kiếm tương đối ===
    // ==========================================================
    /**
     * TÌM KIẾM TƯƠNG ĐỐI: Tìm món ăn theo tên (hỗ trợ Tiếng Việt)
     * @param tenMon Tên món ăn (hoặc một phần của tên)
     * @return Danh sách món ăn khớp
     */
    public ArrayList<MonAn> searchMonAnByName(String tenMon) {
        ArrayList<MonAn> dsMonAn = new ArrayList<>();
        Connection con = ConnectDB.getConnection();
        if (con == null) {
            return dsMonAn;
        }

        // Tìm kiếm tất cả món ăn có tenMon chứa chuỗi tìm kiếm
        // Dùng NVARCHAR (N') và LIKE
        String sql = "SELECT m.maMon, m.tenMon, m.hinhAnh, m.giaBan, m.maDM, d.tenDM " +
                     "FROM MonAn m " +
                     "LEFT JOIN DanhMucMon d ON m.maDM = d.maDM " +
                     "WHERE m.tenMon LIKE ?"; // Dùng N' ngoài PreparedStatement
        
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            // Dùng setNString để hỗ trợ NVARCHAR (Tiếng Việt)
            // và %% để tìm kiếm "contains" (chứa)
            pstmt.setNString(1, "%" + tenMon + "%");
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    // Tái sử dụng hàm extract
                    MonAn mon = extractMonAnFromResultSet(rs);
                    dsMonAn.add(mon);
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi tìm kiếm món ăn: " + e.getMessage());
            e.printStackTrace();
        }
        return dsMonAn;
    }
}
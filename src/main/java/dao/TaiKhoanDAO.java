package dao;

import connect.ConnectDB;
import entity.TaiKhoan;
import entity.NhanVien;
import entity.VaiTro;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TaiKhoanDAO {

    private final NhanVienDAO nhanVienDAO = new NhanVienDAO(); // Cần để lấy thông tin NV

    /**
     * 🔥 HÀM MỚI: Kiểm tra thông tin đăng nhập.
     * @param tenDangNhap Tên đăng nhập (chính là mã NV).
     * @param matKhau Mật khẩu.
     * @return Đối tượng TaiKhoan nếu đăng nhập thành công, null nếu thất bại.
     */
    public TaiKhoan kiemTraDangNhap(String tenDangNhap, String matKhau) {
        // SQL Server mặc định phân biệt chữ hoa/thường tùy collation,
        // nhưng tên đăng nhập (maNV) thường là cố định.
        // Mật khẩu thì NÊN phân biệt hoa/thường.
        // Chú ý: Mật khẩu đang so sánh trực tiếp (không an toàn).
        String sql = "SELECT tenDangNhap, matKhau, vaiTro, maNV FROM TaiKhoan WHERE tenDangNhap = ? AND matKhau = ?";
        TaiKhoan tk = null;

        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, tenDangNhap);
            ps.setString(2, matKhau); // So sánh mật khẩu trực tiếp

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    // Lấy mã NV từ kết quả truy vấn
                    String maNV = rs.getString("maNV");
                    // Lấy thông tin chi tiết Nhân viên
                    NhanVien nv = nhanVienDAO.getNhanVienTheoMa(maNV);

                    // Nếu không tìm thấy thông tin nhân viên -> lỗi dữ liệu
                    if (nv == null) {
                        System.err.println("Lỗi dữ liệu: Tài khoản " + tenDangNhap + " hợp lệ nhưng không tìm thấy Nhân viên " + maNV);
                        return null;
                    }

                    String vaiTroString = rs.getString("vaiTro");
                    VaiTro vaiTro = VaiTro.fromString(vaiTroString); // Chuyển String sang Enum

                    // Tạo đối tượng TaiKhoan trả về
                    tk = new TaiKhoan(
                        rs.getString("tenDangNhap"),
                        null, // Không lưu mật khẩu vào đối tượng sau khi xác thực
                        vaiTro,
                        nv
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace(); // In lỗi ra console để debug
            // Có thể thêm log hoặc ném ngoại lệ ở đây
        }
        return tk; // Trả về null nếu không tìm thấy hoặc có lỗi
    }


    public TaiKhoan layTaiKhoanTheoMaNV(String maNV) {
        String sql = "SELECT tenDangNhap, matKhau, vaiTro, maNV FROM TaiKhoan WHERE maNV = ?";
        TaiKhoan tk = null;
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maNV);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    NhanVien nv = nhanVienDAO.getNhanVienTheoMa(maNV);
                    if (nv == null) return null; // Quan trọng: Kiểm tra nếu NV không tồn tại
                    String vaiTroString = rs.getString("vaiTro");
                    VaiTro vaiTro = VaiTro.fromString(vaiTroString);
                    tk = new TaiKhoan(
                        rs.getString("tenDangNhap"),
                        rs.getString("matKhau"), // Hàm này có thể vẫn cần mật khẩu tùy mục đích
                        vaiTro,
                        nv
                    );
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tk;
    }
    /**
     * 🔥 HÀM MỚI: Kiểm tra xem tài khoản có tồn tại dựa trên tên đăng nhập (mã NV).
     * @param tenDangNhap Tên đăng nhập (mã NV) cần kiểm tra.
     * @return true nếu tài khoản tồn tại, false nếu không.
     */
    public boolean kiemTraTonTaiTaiKhoan(String tenDangNhap) {
        String sql = "SELECT COUNT(*) FROM TaiKhoan WHERE tenDangNhap = ?";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, tenDangNhap);
            try (ResultSet rs = ps.executeQuery()) {
                // Nếu có kết quả và giá trị COUNT(*) > 0 thì tài khoản tồn tại
                if (rs.next() && rs.getInt(1) > 0) {
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // Mặc định là không tồn tại nếu có lỗi hoặc không tìm thấy
    }
    /**
     * 🔥 HÀM MỚI: Cập nhật mật khẩu mới cho tài khoản.
     * @param tenDangNhap Tên đăng nhập (mã NV) của tài khoản cần đổi mật khẩu.
     * @param matKhauMoi Mật khẩu mới (nên được băm trước khi truyền vào).
     * @return true nếu cập nhật thành công, false nếu thất bại.
     */
    public boolean doiMatKhau(String tenDangNhap, String matKhauMoi) {
        // Chú ý: Nên băm (hash) matKhauMoi trước khi lưu vào CSDL
        String sql = "UPDATE TaiKhoan SET matKhau = ? WHERE tenDangNhap = ?";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, matKhauMoi); // Lưu mật khẩu mới (chưa băm)
            ps.setString(2, tenDangNhap);

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0; // Trả về true nếu có ít nhất 1 dòng được cập nhật

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public boolean kiemTraMatKhau(String tenDangNhap, String matKhau) throws SQLException {
        
        // CẢNH BÁO BẢO MẬT: 
        // Logic này giả định bạn đang lưu mật khẩu dạng plaintext (văn bản thuần).
        // Trong thực tế, bạn NÊN mã hóa (hash) mật khẩu khi lưu và kiểm tra.
        
        String sql = "SELECT MatKhau FROM TaiKhoan WHERE TenDangNhap = ?";
        
        // Thay thế 'ConnectDB.getConnection()' bằng cách bạn lấy kết nối
        try (Connection conn = ConnectDB.getConnection(); 
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, tenDangNhap);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String matKhauTuDB = rs.getString("MatKhau");
                    
                    // So sánh mật khẩu (ví dụ đơn giản)
                    return matKhau.equals(matKhauTuDB);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Ném lỗi hoặc trả về false tùy theo logic của bạn
            throw new SQLException("Lỗi khi kiểm tra mật khẩu", e); 
        }
        
        return false; // Trả về false nếu không tìm thấy tài khoản
    }
}
package connect;

import java.sql.Connection;

public class ConnectDBTest {

    public static void main(String[] args) {
        Connection conn = null;
        try {
            // 1. Cố gắng lấy kết nối
            System.out.println("--- Bắt đầu kiểm tra kết nối CSDL SQL Server ---");
            conn = ConnectDB.getConnection();

            // 2. Kiểm tra trạng thái kết nối
            if (conn != null) {
                System.out.println("Kiểm tra thành công! Đối tượng Connection đã được tạo.");
            } else {
                System.out.println("Kiểm tra thất bại! Đối tượng Connection là NULL.");
            }

        } catch (Exception e) {
            System.err.println("Lỗi ngoại lệ trong quá trình kiểm tra: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 3. Đóng kết nối (Được xử lý bên trong ConnectDB.closeConnection())
            ConnectDB.closeConnection();
            System.out.println("--- Kết thúc kiểm tra kết nối ---");
        }
    }
}
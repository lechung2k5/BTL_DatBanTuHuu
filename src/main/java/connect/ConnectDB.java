package connect;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectDB {
    private static Connection connection = null;

    // --- Cấu hình cho SQL Server ---
    private static final String URL = "jdbc:sqlserver://localhost:1433;databaseName=NhaHang;encrypt=false;trustServerCertificate=true;"; 
    private static final String USER = "sa";
    private static final String PASSWORD = "sapassword"; 
    // -------------------------------

    // Hàm lấy kết nối
    public static Connection getConnection() {
        try {
            // Đảm bảo driver SQL Server được tải (Mặc dù JDBC 4.0+ thường tự động)
            // Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver"); 
            
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("✅ Kết nối SQL Server thành công!");
            }
        } catch (SQLException e) {
            System.err.println("❌ Lỗi kết nối CSDL: " + e.getMessage());
        }
        return connection;
    }

    // Hàm đóng kết nối
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("🔒 Đã đóng kết nối CSDL.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
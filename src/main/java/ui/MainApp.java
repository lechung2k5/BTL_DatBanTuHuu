package ui;

import entity.TaiKhoan; // 🔥 Thêm import
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;

import java.io.IOException;

public class MainApp extends Application {

    private Stage primaryStage;
    // 🔥 THÊM BIẾN NÀY ĐỂ LƯU TẠM THÔNG TIN ĐĂNG NHẬP
    private static TaiKhoan loggedInUser = null;

    // 🔥 HÀM MỚI ĐỂ LƯU TÀI KHOẢN SAU KHI ĐĂNG NHẬP
    public static void setLoggedInUser(TaiKhoan user) {
        loggedInUser = user;
    }

    // 🔥 HÀM MỚI (TÙY CHỌN) ĐỂ LẤY THÔNG TIN NGƯỜI DÙNG (nếu cần ở chỗ khác)
    public static TaiKhoan getLoggedInUser() {
        return loggedInUser;
    }


    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setResizable(false);
        gotoLogin();
    }

    public void gotoLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DangNhap.fxml"));
            Parent root = loader.load();
            DangNhap dangNhapController = loader.getController();
            dangNhapController.setMainApp(this);

            Scene scene = new Scene(root);
            // Đảm bảo load CSS đăng nhập
            scene.getStylesheets().add(getClass().getResource("/css/dangNhap.css").toExternalForm());
            primaryStage.setTitle("Đăng nhập");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	public void gotoQuenMatKhau() {
	    try {
	        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/QuenMatKhau.fxml"));
	        Parent root = loader.load();
	        QuenMatKhau quenMatKhauController = loader.getController();
	        quenMatKhauController.setMainApp(this);

	        Scene scene = new Scene(root);
	        // Đảm bảo load CSS quên mật khẩu
	        scene.getStylesheets().add(getClass().getResource("/css/QuenMatKhau.css").toExternalForm());
	        primaryStage.setTitle("Quên mật khẩu");
	        primaryStage.setScene(scene);
	        primaryStage.show();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}

    public void gotoMainScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ManHinhChinh.fxml"));
            Parent root = loader.load();
            ManHinhChinh manHinhChinhController = loader.getController();
            manHinhChinhController.setMainApp(this);

            // 🔥 THÊM DÒNG NÀY: Truyền thông tin người dùng vào ManHinhChinh
            if (loggedInUser != null) {
                manHinhChinhController.setUserInfo(loggedInUser);
            } else {
                System.err.println("Lỗi: Không có thông tin người dùng đăng nhập!");
                 // Optionally handle the case where loggedInUser is null
                // manHinhChinhController.setUserInfo(null); // Or pass null explicitly
            }


            Scene scene = new Scene(root);
            // Đảm bảo load CSS màn hình chính
            scene.getStylesheets().add(getClass().getResource("/css/manHinhChinh.css").toExternalForm());
            scene.getStylesheets().add(getClass().getResource("/css/Dashboard.css").toExternalForm());

            Stage mainStage = new Stage();
            mainStage.setTitle("Quản lý nhà hàng");
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            mainStage.setX(screenBounds.getMinX());
            mainStage.setY(screenBounds.getMinY());
            mainStage.setWidth(screenBounds.getWidth());
            mainStage.setHeight(screenBounds.getHeight());
            mainStage.setResizable(true);

            mainStage.setScene(scene);
            mainStage.show();

            // Đóng cửa sổ đăng nhập
            if (primaryStage != null) {
                primaryStage.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
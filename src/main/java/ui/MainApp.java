package ui;

import entity.TaiKhoan;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Screen;
import javafx.stage.StageStyle; // 👈 THÊM IMPORT NÀY
import javafx.geometry.Rectangle2D;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import java.io.IOException;

public class MainApp extends Application {

    private Stage primaryStage; // Sẽ dùng cho màn hình Đăng nhập
    private Stage preloaderStage; // 👈 THÊM BIẾN NÀY cho Splash Screen
    private Image appIcon;
    private static TaiKhoan loggedInUser = null;

    public static void setLoggedInUser(TaiKhoan user) {
        loggedInUser = user;
    }

    public static TaiKhoan getLoggedInUser() {
        return loggedInUser;
    }


    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setResizable(false);
        
        // 🔥 TẢI ICON TỪ THƯ MỤC RESOURCES
        try {
            // Đảm bảo đường dẫn "/images/logo.png" là chính xác 
            appIcon = new Image(getClass().getResourceAsStream("/images/logo.png"));
            
            // Gán icon cho cửa sổ Đăng nhập (primaryStage)
            primaryStage.getIcons().add(appIcon);
            
        } catch (Exception e) {
            System.err.println("Không thể tải file icon: " + e.getMessage());
        }
        
        showPreloader(); 
    }

    /**
     * 👈 HÀM MỚI: Hiển thị màn hình Preloader (Splash Screen)
     */
    public void showPreloader() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Preloader.fxml"));
            Parent root = loader.load();
            
            PreloaderController controller = loader.getController();
            controller.setMainApp(this);

            Scene scene = new Scene(root);
            
            // 🔥 THAY ĐỔI 1: LÀM NỀN SCENE TRONG SUỐT
            scene.setFill(Color.TRANSPARENT); 
            
            scene.getStylesheets().add(getClass().getResource("/css/preloader.css").toExternalForm());
            
            preloaderStage = new Stage();
            if (appIcon != null) {
                preloaderStage.getIcons().add(appIcon);
            }
            
            preloaderStage.initStyle(StageStyle.TRANSPARENT);
            // 🔥 THAY ĐỔI 2: LÀM NỀN STAGE TRONG SUỐT
            preloaderStage.initStyle(StageStyle.TRANSPARENT); // Thay vì UNDECORATED
            
            preloaderStage.setTitle("Đang tải...");
            preloaderStage.setScene(scene);
            preloaderStage.show();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void gotoLogin() {
        try {
            // 👈 THÊM DÒNG NÀY: Đóng màn hình Preloader khi vào Đăng nhập
            if (preloaderStage != null) {
                preloaderStage.close();
            }

            // Code cũ của bạn giữ nguyên
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DangNhap.fxml"));
            Parent root = loader.load();
            DangNhap dangNhapController = loader.getController();
            dangNhapController.setMainApp(this);

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/dangNhap.css").toExternalForm());
            primaryStage.setTitle("Đăng nhập");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // -----------------------------------------------------------------
    //  CÁC HÀM CÒN LẠI (gotoQuenMatKhau, gotoMainScreen, main)
    //  GIỮ NGUYÊN 100% NHƯ FILE CŨ CỦA BẠN
    // -----------------------------------------------------------------

	public void gotoQuenMatKhau() {
	    try {
	        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/QuenMatKhau.fxml"));
	        Parent root = loader.load();
	        QuenMatKhau quenMatKhauController = loader.getController();
	        quenMatKhauController.setMainApp(this);

	        Scene scene = new Scene(root);
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

            if (loggedInUser != null) {
                manHinhChinhController.setUserInfo(loggedInUser);
            } else {
                System.err.println("Lỗi: Không có thông tin người dùng đăng nhập!");
            }

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/manHinhChinh.css").toExternalForm());
            scene.getStylesheets().add(getClass().getResource("/css/Dashboard.css").toExternalForm());

            Stage mainStage = new Stage();
            if (appIcon != null) {
                mainStage.getIcons().add(appIcon);
            }
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
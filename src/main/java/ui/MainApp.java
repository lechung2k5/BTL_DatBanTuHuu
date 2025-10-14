package ui;

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

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setResizable(false);
        gotoLogin();
    }

    public void gotoLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dangNhap.fxml"));
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

    public void gotoMainScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ManHinhChinh.fxml"));
            Parent root = loader.load();
            ManHinhChinh manHinhChinhController = loader.getController();
            manHinhChinhController.setMainApp(this);

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/manHinhChinh.css").toExternalForm());
            scene.getStylesheets().add(getClass().getResource("/css/Dashboard.css").toExternalForm()); // Load dashboard.css
            
            primaryStage.setTitle("Quản lý nhà hàng");
            
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            primaryStage.setX(screenBounds.getMinX());
            primaryStage.setY(screenBounds.getMinY());
            primaryStage.setWidth(screenBounds.getWidth());
            primaryStage.setHeight(screenBounds.getHeight());
            
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
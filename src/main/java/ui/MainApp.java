package ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Đường dẫn FXML: /fxml/dangNhap.fxml
        URL fxmlUrl = getClass().getResource("/fxml/dangNhap.fxml");
        if (fxmlUrl == null) {
            System.err.println("Không tìm thấy file FXML: /fxml/dangNhap.fxml");
            return;
        }

        Parent root = FXMLLoader.load(fxmlUrl);

        // Đường dẫn CSS: /css/dangNhap.css
        URL cssUrl = getClass().getResource("/css/dangNhap.css");
        if (cssUrl == null) {
            System.err.println("Không tìm thấy file CSS: /css/dangNhap.css");
            return;
        }

        Scene scene = new Scene(root);
        scene.getStylesheets().add(cssUrl.toExternalForm());

        primaryStage.setTitle("Đăng nhập");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
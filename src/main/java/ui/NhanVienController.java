package ui;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import java.io.IOException;
import java.util.Map;

public class NhanVienController {

    @FXML
    private BorderPane contentPane;
    @FXML
    private Button btnDanhSach;
    @FXML
    private Button btnPhanCa;

    private static String currentView = "";

    public static String getCurrentView() {
        return currentView;
    }

    public static void setCurrentView(String view) {
        currentView = view;
    }

    @FXML
    private void initialize() throws IOException {
        handleChuyenSangDanhSach(); // ✅ Mặc định mở tab Danh sách
    }

    /** ✅ Xóa toàn bộ shortcut cũ trước khi load tab mới */
    private void clearShortcuts() {
        if (contentPane.getScene() != null) {
            contentPane.getScene().getAccelerators().clear();
        }
    }

    /** ✅ Shortcut dành cho TAB Danh sách NV */
    private void setupDanhSachShortcuts() {
        var scene = contentPane.getScene();
        if (scene == null) return;

        // ALT + 1 → Danh sách NV
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.DIGIT1, KeyCombination.ALT_DOWN),
                () -> { try { handleChuyenSangDanhSach(); } catch (Exception ignored) {} }
        );

        // ALT + 2 → Phân ca trực
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.DIGIT2, KeyCombination.ALT_DOWN),
                () -> { try { handleChuyenSangPhanCa(); } catch (Exception ignored) {} }
        );
    }

    /** ✅ Shortcut dành cho TAB Phân ca trực */
    private void setupPhanCaShortcuts() {
        var scene = contentPane.getScene();
        if (scene == null) return;

        // ALT + 1 → Danh sách NV
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.DIGIT1, KeyCombination.ALT_DOWN),
                () -> { try { handleChuyenSangDanhSach(); } catch (Exception ignored) {} }
        );

        // ALT + 2 → Phân ca trực
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.DIGIT2, KeyCombination.ALT_DOWN),
                () -> { try { handleChuyenSangPhanCa(); } catch (Exception ignored) {} }
        );

        // ✅ Ctrl + ← → Tuần trước
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.LEFT, KeyCombination.CONTROL_DOWN),
                () -> {
                    if ("PhanCa".equals(currentView)) {
                        ui.PhanCaTrucUI.goPreviousWeek();
                    }
                }
        );

        // ✅ Ctrl + → → Tuần sau
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.RIGHT, KeyCombination.CONTROL_DOWN),
                () -> {
                    if ("PhanCa".equals(currentView)) {
                        ui.PhanCaTrucUI.goNextWeek();
                    }
                }
        );

        // ✅ Ctrl + H → Trở về tuần hiện tại
        scene.getAccelerators().put(
                new KeyCodeCombination(KeyCode.H, KeyCombination.CONTROL_DOWN),
                () -> {
                    if ("PhanCa".equals(currentView)) {
                        ui.PhanCaTrucUI.goCurrentWeek();
                    }
                }
        );
    }

    @FXML
    private void handleChuyenSangDanhSach() throws IOException {
        loadScreen("/fxml/DanhSachNV.fxml");

        clearShortcuts();
        setupDanhSachShortcuts();

        btnDanhSach.getStyleClass().add("tab-button-active");
        btnPhanCa.getStyleClass().remove("tab-button-active");

        setCurrentView("DanhSach");
    }

    @FXML
    private void handleChuyenSangPhanCa() throws IOException {
        loadScreen("/fxml/PhanCaTruc.fxml");

        clearShortcuts();
        setupPhanCaShortcuts();

        btnPhanCa.getStyleClass().add("tab-button-active");
        btnDanhSach.getStyleClass().remove("tab-button-active");

        setCurrentView("PhanCa");
    }

    private void loadScreen(String fxmlPath) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
        contentPane.setCenter(root);
    }
}

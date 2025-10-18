package ui;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

public class ThucDon {

    @FXML private TableView<MenuItem> tableThucDon;
    @FXML private TableColumn<MenuItem, String> colTenMon;
    @FXML private TableColumn<MenuItem, Image> colHinhAnh;
    @FXML private TableColumn<MenuItem, Number> colDonGia;

    @FXML private TextField txtTenMon, txtDonGia, txtSearch;
    @FXML private Label uploadLabel;
    @FXML private ImageView previewImg;

    @FXML private Button btnThem, btnXoa, btnSua, btnLuu;
    @FXML private Button btnLuuForm;

    @FXML private HBox categoryButtonBox;
    @FXML private Button btnKhaiVi, btnNuong, btnLau, btnXaoHap, btnChien, btnDacSan, btnDoUong;

    public void initialize() {
        setupTableColumns();
        loadSampleData();

        txtTenMon.setText("Bánh tráng cuốn");
        txtDonGia.setText("15,000");
        previewImg.setImage(null);

        addCategoryButtonListeners();
        btnKhaiVi.getStyleClass().add("active");

        uploadLabel.setOnMouseClicked(e -> onUploadClicked());

        btnThem.setOnAction(e -> System.out.println("Thêm clicked"));
        btnXoa.setOnAction(e -> System.out.println("Xóa clicked"));
        btnSua.setOnAction(e -> System.out.println("Sửa clicked"));
        btnLuu.setOnAction(e -> System.out.println("Lưu clicked"));
        btnLuuForm.setOnAction(e -> System.out.println("Lưu form clicked"));
    }

    private void setupTableColumns() {
        tableThucDon.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        // *** KEY CHANGE: Added cell factory to apply a specific style class to the name column ***
        colTenMon.setCellValueFactory(new PropertyValueFactory<>("name"));
        colTenMon.setCellFactory(column -> new TableCell<MenuItem, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : item);
                getStyleClass().add("name-cell"); // Apply style class
            }
        });

        colHinhAnh.setCellValueFactory(new PropertyValueFactory<>("image"));
        colHinhAnh.setCellFactory(column -> new TableCell<MenuItem, Image>() {
            private final ImageView imageView = new ImageView();
            {
                // Ensures all images have a consistent size
                imageView.setFitHeight(50); 
                imageView.setFitWidth(80);
                imageView.setPreserveRatio(false); // Set to false to fill the dimensions
                getStyleClass().add("image-cell");
            }
            @Override
            protected void updateItem(Image item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    imageView.setImage(item);
                    setGraphic(imageView);
                }
            }
        });

        // *** KEY CHANGE: Added a specific style class to the price column ***
        colDonGia.setCellValueFactory(new PropertyValueFactory<>("price"));
        colDonGia.setCellFactory(column -> new TableCell<MenuItem, Number>() {
            {
                getStyleClass().add("price-cell"); // Apply style class
            }
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%,.0f", item.doubleValue()));
                }
            }
        });
    }
    
    private void loadSampleData() {
        ObservableList<MenuItem> items = FXCollections.observableArrayList();
        items.add(new MenuItem("Đậu phộng rang muối", "/images/TomSongXotThai.jpg", 20000));
        items.add(new MenuItem("Xoài lắc", "/images/LongLuoc.jpg", 20000));
        items.add(new MenuItem("Nem chua", "/images/NgaoHap.jpg", 20000));
        items.add(new MenuItem("Cá lóc nướng", "/images/CaLocNuong.jpg", 150000));
        items.add(new MenuItem("Ốc nướng mỡ hành", "/images/OcNuongMoHanh.jpg", 80000));
        tableThucDon.setItems(items);
    }

    private void addCategoryButtonListeners() {
        for (javafx.scene.Node node : categoryButtonBox.getChildren()) {
            if (node instanceof Button) {
                node.setOnMouseClicked(this::onCategoryClicked);
            }
        }
    }

    private void onCategoryClicked(MouseEvent evt) {
        Button clicked = (Button) evt.getSource();
        for (javafx.scene.Node node : categoryButtonBox.getChildren()) {
            node.getStyleClass().remove("active");
        }
        clicked.getStyleClass().add("active");
        System.out.println("Filter by: " + clicked.getText());
    }

    private void onUploadClicked() {
        System.out.println("Upload label clicked - implement file chooser here");
    }
    
    public static class MenuItem {
        private final SimpleStringProperty name;
        private final SimpleObjectProperty<Image> image;
        private final SimpleDoubleProperty price;

        public MenuItem(String name, String imagePath, double price) {
            this.name = new SimpleStringProperty(name);
            this.price = new SimpleDoubleProperty(price);
            
            Image loadedImage;
            try {
                loadedImage = new Image(getClass().getResourceAsStream(imagePath));
            } catch (Exception e) {
                System.err.println("Cannot load image: " + imagePath);
                loadedImage = null;
            }
            this.image = new SimpleObjectProperty<>(loadedImage);
        }

        public String getName() { return name.get(); }
        public Image getImage() { return image.get(); }
        public double getPrice() { return price.get(); }

        public SimpleStringProperty nameProperty() { return name; }
        public SimpleObjectProperty<Image> imageProperty() { return image; }
        public SimpleDoubleProperty priceProperty() { return price; }
    }
}
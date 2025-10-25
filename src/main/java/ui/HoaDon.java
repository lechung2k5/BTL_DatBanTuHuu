package ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.layout.HBox;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class HoaDon {

	// (Phần Model giữ nguyên như cũ)
	// ...
	// ---------------------- PHẦN MODEL (DỮ LIỆU) ----------------------
	private String maHoaDon;
	private LocalDate ngayLap;
	private String hinhThucTT;
	private String maKhachHang;
	private String thuNgan;
	private int ban;
	private LocalTime gioVao;
	private LocalTime gioRa;
	private double tongCongMonAn;
	private double phiDichVu;
	private double thueVAT;
	private double tienDatCoc;
	private double khuyenMai;
	private double tongTienThanhToan;

	// Constructors
	public HoaDon() {
		super();
	}

	public HoaDon(String maHoaDon, LocalDate ngayLap, String hinhThucTT, String maKhachHang, String thuNgan, int ban,
			LocalTime gioVao, LocalTime gioRa, double tongCongMonAn, double phiDichVu, double thueVAT,
			double tienDatCoc, double khuyenMai, double tongTienThanhToan) {
		this.maHoaDon = maHoaDon;
		this.ngayLap = ngayLap;
		this.hinhThucTT = hinhThucTT;
		this.maKhachHang = maKhachHang;
		this.thuNgan = thuNgan;
		this.ban = ban;
		this.gioVao = gioVao;
		this.gioRa = gioRa;
		this.tongCongMonAn = tongCongMonAn;
		this.phiDichVu = phiDichVu;
		this.thueVAT = thueVAT;
		this.tienDatCoc = tienDatCoc;
		this.khuyenMai = khuyenMai;
		this.tongTienThanhToan = tongTienThanhToan;
	}

	// Getters
	public String getMaHoaDon() {
		return maHoaDon;
	}

	public LocalDate getNgayLap() {
		return ngayLap;
	}

	public String getHinhThucTT() {
		return hinhThucTT;
	}

	public String getMaKhachHang() {
		return maKhachHang;
	}

	public String getThuNgan() {
		return thuNgan;
	}

	public int getBan() {
		return ban;
	}

	public LocalTime getGioVao() {
		return gioVao;
	}

	public LocalTime getGioRa() {
		return gioRa;
	}

	public double getTongCongMonAn() {
		return tongCongMonAn;
	}

	public double getPhiDichVu() {
		return phiDichVu;
	}

	public double getThueVAT() {
		return thueVAT;
	}

	public double getTienDatCoc() {
		return tienDatCoc;
	}

	public double getKhuyenMai() {
		return khuyenMai;
	}

	public double getTongTienThanhToan() {
		return tongTienThanhToan;
	}

	// ---------------------- PHẦN CONTROLLER (LOGIC GIAO DIỆN)
	// ----------------------

	// === CÁC THÀNH PHẦN GIAO DIỆN ===
	@FXML
	private ComboBox<String> comboFilter;
	@FXML
	private TextField txtSearch;
	@FXML
	private TableView<HoaDon> tableHoaDon;
	@FXML
	private TableColumn<HoaDon, String> colMaHD;
	@FXML
	private TableColumn<HoaDon, String> colNgay;
	@FXML
	private TableColumn<HoaDon, String> colHinhThucThanhToan;
	@FXML
	private TableColumn<HoaDon, String> colSoDienThoaiKH;
	@FXML
	private TableColumn<HoaDon, Double> colTongTien;
	@FXML
	private TableColumn<HoaDon, Void> colXemChiTiet;
	@FXML
	private Button btnXuatExcel;
	@FXML
	private Button btnInHoaDon;
	@FXML
	private TableView<?> tableChiTietHD;

	// CẬP NHẬT: Thay đổi các biến FXML cho các Label giá trị
	@FXML
	private Label lblMaHDValue;
	@FXML
	private Label lblMaKHValue;
	@FXML
	private Label lblNgayValue;
	@FXML
	private Label lblThuNganValue;
	@FXML
	private Label lblBanValue;
	@FXML
	private Label lblGioVaoValue;
	@FXML
	private Label lblGioRaValue;
	@FXML
	private Label lblTongCongMonAnValue;
	@FXML
	private Label lblPhiDichVuValue;
	@FXML
	private Label lblThueVATValue;
	@FXML
	private Label lblTienDatCocValue;
	@FXML
	private Label lblKhuyenMaiValue;
	@FXML
	private Label lblTongTienThanhToanValue;

	private ObservableList<HoaDon> danhSachHoaDon;
	private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

	@FXML
	private void initialize() {
		danhSachHoaDon = FXCollections.observableArrayList(
				new HoaDon("HD00036", LocalDate.of(2025, 10, 26), "Tiền mặt", "0377019955", "Trần Hoàng Nam", 16,
						LocalTime.of(18, 0), LocalTime.of(21, 0), 520000, 26000, 41600, -150000, -130000, 437600),
				new HoaDon("HD00002", LocalDate.of(2025, 10, 15), "Chuyển khoản", "0912345678", "Nguyễn Văn A", 5,
						LocalTime.of(19, 30), LocalTime.of(20, 45), 350000, 17500, 28000, -100000, 0, 295500),
				new HoaDon("HD00003", LocalDate.of(2025, 10, 14), "Tiền mặt", "0978456123", "Lê Thị B", 8,
						LocalTime.of(11, 0), LocalTime.of(12, 15), 150000, 7500, 12000, 0, -20000, 149500));

		colMaHD.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMaHoaDon()));
		colNgay.setCellValueFactory(
				data -> new SimpleStringProperty(data.getValue().getNgayLap().format(dateFormatter)));
		colHinhThucThanhToan.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getHinhThucTT()));
		colSoDienThoaiKH.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMaKhachHang()));
		colTongTien.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getTongTienThanhToan()));

		colTongTien.setCellFactory(column -> new TableCell<>() {
			@Override
			protected void updateItem(Double item, boolean empty) {
				super.updateItem(item, empty);
				setText(empty || item == null ? null : String.format("%,.0f VNĐ", item));
			}
		});

		colXemChiTiet.setCellFactory(param -> new TableCell<>() {
			private final Button btnXem = new Button("Xem");
			private final HBox pane = new HBox(btnXem);
			{
				pane.setAlignment(Pos.CENTER);
				btnXem.getStyleClass().add("view-button");
				btnXem.setOnAction(event -> {
					HoaDon hoaDon = getTableView().getItems().get(getIndex());
					if (hoaDon != null) {
						hienThiThongTinHoaDon(hoaDon);
					}
				});
			}

			@Override
			protected void updateItem(Void item, boolean empty) {
				super.updateItem(item, empty);
				setGraphic(empty ? null : pane);
			}
		});

		tableHoaDon.setItems(danhSachHoaDon);

		if (!danhSachHoaDon.isEmpty()) {
			hienThiThongTinHoaDon(danhSachHoaDon.get(0));
			tableHoaDon.getSelectionModel().selectFirst();
		}

		btnXuatExcel.setOnAction(e -> xuatExcel());
		btnInHoaDon.setOnAction(e -> inHoaDon());
	}

	// CẬP NHẬT: Phương thức này giờ sẽ gán giá trị cho các Label tương ứng
	private void hienThiThongTinHoaDon(HoaDon hoaDon) {
		lblMaHDValue.setText(hoaDon.getMaHoaDon());
		lblMaKHValue.setText(hoaDon.getMaKhachHang());
		lblNgayValue.setText(hoaDon.getNgayLap().format(dateFormatter));
		lblThuNganValue.setText(hoaDon.getThuNgan());
		lblBanValue.setText(String.valueOf(hoaDon.getBan()));
		lblGioVaoValue.setText(hoaDon.getGioVao().format(timeFormatter));
		lblGioRaValue.setText(hoaDon.getGioRa().format(timeFormatter));

		lblTongCongMonAnValue.setText(String.format("%,.0f VNĐ", hoaDon.getTongCongMonAn()));
		lblPhiDichVuValue.setText(String.format("%,.0f VNĐ", hoaDon.getPhiDichVu()));
		lblThueVATValue.setText(String.format("%,.0f VNĐ", hoaDon.getThueVAT()));
		lblTienDatCocValue.setText(String.format("%,.0f VNĐ", hoaDon.getTienDatCoc()));
		lblKhuyenMaiValue.setText(String.format("%,.0f VNĐ", hoaDon.getKhuyenMai()));
		lblTongTienThanhToanValue.setText(String.format("%,.0f VNĐ", hoaDon.getTongTienThanhToan()));
	}

	private void xuatExcel() {
		showAlert("Tính năng xuất Excel đang được phát triển...");
	}

	private void inHoaDon() {
		showAlert("Tính năng in hóa đơn đang được phát triển...");
	}

	private void showAlert(String message) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}

	public void setMaHD(String maHD) {
		// TODO Auto-generated method stub

	}
}
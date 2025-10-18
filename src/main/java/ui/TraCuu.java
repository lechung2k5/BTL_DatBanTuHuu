package ui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.event.ActionEvent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;

public class TraCuu {

	// ========== HÓA ĐƠN ==========
	@FXML
	private ComboBox<String> cboSapXepHD;
	@FXML
	private TextField txtTimKiemHD;
	@FXML
	private TableView<HoaDon> tblHoaDon;
	@FXML
	private TableColumn<HoaDon, String> colMaHD;
	@FXML
	private TableColumn<HoaDon, String> colNgayHD;
	@FXML
	private TableColumn<HoaDon, String> colHinhThuc;
	@FXML
	private TableColumn<HoaDon, String> colSDTKH;
	@FXML
	private TableColumn<HoaDon, String> colTongTien;
	@FXML
	private TableColumn<HoaDon, Void> colXemChiTiet;

	// ========== KHÁCH HÀNG ==========
	@FXML
	private ComboBox<String> cboSapXepKH;
	@FXML
	private TextField txtTimKiemKH;
	@FXML
	private TableView<KhachHang> tblKhachHang;
	@FXML
	private TableColumn<KhachHang, String> colMaKH;
	@FXML
	private TableColumn<KhachHang, String> colHoTen;
	@FXML
	private TableColumn<KhachHang, String> colSDT;
	@FXML
	private TableColumn<KhachHang, String> colDiaChi;
	@FXML
	private TableColumn<KhachHang, String> colEmail;
	@FXML
	private TableColumn<KhachHang, String> colNgayDK;
	@FXML
	private TableColumn<KhachHang, String> colLoaiKH;
	@FXML
	private TableColumn<KhachHang, String> colTongTienHD;
	@FXML
	private TableColumn<KhachHang, Void> colLichSu;

	private ObservableList<HoaDon> danhSachHoaDon;
	private ObservableList<HoaDon> tatCaHoaDon;
	private ObservableList<KhachHang> danhSachKhachHang;
	private ObservableList<KhachHang> tatCaKhachHang;

	@FXML
	public void initialize() {

		// Khởi tạo dữ liệu mẫu cho Hóa đơn
		tatCaHoaDon = FXCollections.observableArrayList(
				new HoaDon("HD001", "22/03/2025", "Tiền mặt", "0377019956", "1.000.000 VNĐ"),
				new HoaDon("HD002", "22/03/2025", "Chuyển khoản", "0377019956", "150,000 VNĐ"),
				new HoaDon("HD003", "22/03/2025", "Chuyển khoản", "0389076543", "200,000 VNĐ"),
				new HoaDon("HD004", "22/03/2025", "Tiền mặt", "0389076543", "180,000 VNĐ"),
				new HoaDon("HD005", "23/03/2025", "Chuyển khoản", "0912345678", "300,000 VNĐ"));

		// Khởi tạo danh sách rỗng (không hiển thị sẵn)
		danhSachHoaDon = FXCollections.observableArrayList();

		// Khởi tạo dữ liệu mẫu cho Khách hàng
		tatCaKhachHang = FXCollections.observableArrayList(
				new KhachHang("KH01", "Nguyễn Văn A", "0389076543", "Quận 1", "nguyenvana@gmail.com", "12/01/2023",
						"VIP", "1,000,000 VNĐ"),
				new KhachHang("KH02", "Nguyễn Văn B", "0377019956", "Quận 2", "nguyenvanb@gmail.com", "15/02/2023",
						"Thường", "500,000 VNĐ"),
				new KhachHang("KH03", "Nguyễn Văn C", "0912345678", "Quận 3", "nguyenvanc@gmail.com", "20/03/2023",
						"VIP", "2,000,000 VNĐ"));

		// Khởi tạo danh sách rỗng (không hiển thị sẵn)
		danhSachKhachHang = FXCollections.observableArrayList();

		// Cấu hình ComboBox
		cboSapXepHD.setItems(FXCollections.observableArrayList("Theo ngày", "Theo tháng", "Theo năm"));

		cboSapXepKH.setItems(FXCollections.observableArrayList("Theo ngày", "Theo tháng", "Theo năm"));

		// ========== Cấu hình bảng Hóa đơn ==========
		colMaHD.setCellValueFactory(new PropertyValueFactory<>("maHD"));
		colNgayHD.setCellValueFactory(new PropertyValueFactory<>("ngay"));
		colHinhThuc.setCellValueFactory(new PropertyValueFactory<>("hinhThuc"));
		colSDTKH.setCellValueFactory(new PropertyValueFactory<>("sdtKH"));
		colTongTien.setCellValueFactory(new PropertyValueFactory<>("tongTien"));

		// Cột Xem chi tiết với nút
		colXemChiTiet.setCellFactory(col -> new TableCell<HoaDon, Void>() {
			private final Button btnXem = new Button("Xem");

			{
				btnXem.getStyleClass().add("view-button");
				btnXem.setOnAction(event -> {
					HoaDon hoaDon = getTableView().getItems().get(getIndex());
					handleXemChiTietHoaDon(hoaDon);
				});
			}

			@Override
			protected void updateItem(Void item, boolean empty) {
				super.updateItem(item, empty);
				if (empty) {
					setGraphic(null);
				} else {
					HBox hbox = new HBox(btnXem);
					hbox.setAlignment(Pos.CENTER);
					setGraphic(hbox);
				}
			}
		});

		tblHoaDon.setItems(danhSachHoaDon);

		// ========== Cấu hình bảng Khách hàng ==========
		colMaKH.setCellValueFactory(new PropertyValueFactory<>("maKH"));
		colHoTen.setCellValueFactory(new PropertyValueFactory<>("hoTen"));
		colSDT.setCellValueFactory(new PropertyValueFactory<>("sdt"));
		colDiaChi.setCellValueFactory(new PropertyValueFactory<>("diaChi"));
		colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
		colNgayDK.setCellValueFactory(new PropertyValueFactory<>("ngayDK"));
		colLoaiKH.setCellValueFactory(new PropertyValueFactory<>("loaiKH"));
		colTongTienHD.setCellValueFactory(new PropertyValueFactory<>("tongTienHD"));

		// Cột Xem lịch sử với nút
		colLichSu.setCellFactory(col -> new TableCell<KhachHang, Void>() {
			private final Button btnXem = new Button("Xem");

			{
				btnXem.getStyleClass().add("view-button");
				btnXem.setOnAction(event -> {
					KhachHang khachHang = getTableView().getItems().get(getIndex());
					handleXemLichSuKhachHang(khachHang);
				});
			}

			@Override
			protected void updateItem(Void item, boolean empty) {
				super.updateItem(item, empty);
				if (empty) {
					setGraphic(null);
				} else {
					HBox hbox = new HBox(btnXem);
					hbox.setAlignment(Pos.CENTER);
					setGraphic(hbox);
				}
			}
		});

		tblKhachHang.setItems(danhSachKhachHang);

		// ========== Thêm listener cho tìm kiếm theo số điện thoại ==========
		txtTimKiemHD.textProperty().addListener((observable, oldValue, newValue) -> {
			timKiemHoaDonTheoSDT(newValue);
		});

		txtTimKiemKH.textProperty().addListener((observable, oldValue, newValue) -> {
			timKiemKhachHangTheoSDT(newValue);
		});
	}

	// ========== Tìm kiếm Hóa đơn theo số điện thoại ==========
	private void timKiemHoaDonTheoSDT(String sdt) {
		// Nếu ô tìm kiếm trống, không hiển thị gì
		if (sdt == null || sdt.trim().isEmpty()) {
			danhSachHoaDon.clear();
			return;
		}

		String keyword = sdt.trim();
		ObservableList<HoaDon> ketQua = FXCollections.observableArrayList();

		// Tìm kiếm chính xác hoặc có chứa số điện thoại
		for (HoaDon hd : tatCaHoaDon) {
			if (hd.getSdtKH().contains(keyword)) {
				ketQua.add(hd);
			}
		}

		danhSachHoaDon.setAll(ketQua);
	}

	// ========== Tìm kiếm Khách hàng theo số điện thoại ==========
	private void timKiemKhachHangTheoSDT(String sdt) {
		// Nếu ô tìm kiếm trống, không hiển thị gì
		if (sdt == null || sdt.trim().isEmpty()) {
			danhSachKhachHang.clear();
			return;
		}

		String keyword = sdt.trim();
		ObservableList<KhachHang> ketQua = FXCollections.observableArrayList();

		// Tìm kiếm chính xác hoặc có chứa số điện thoại
		for (KhachHang kh : tatCaKhachHang) {
			if (kh.getSdt().contains(keyword)) {
				ketQua.add(kh);
			}
		}

		danhSachKhachHang.setAll(ketQua);
	}

	// ========== Xem chi tiết Hóa đơn ==========
	private void handleXemChiTietHoaDon(HoaDon hoaDon) {
		System.out.println("Xem chi tiết hóa đơn: " + hoaDon.getMaHD());
		// TODO: Mở dialog hoặc màn hình chi tiết hóa đơn
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("Chi tiết hóa đơn");
		alert.setHeaderText("Thông tin hóa đơn: " + hoaDon.getMaHD());
		alert.setContentText("Ngày: " + hoaDon.getNgay() + "\n" + "Hình thức: " + hoaDon.getHinhThuc() + "\n"
				+ "SĐT KH: " + hoaDon.getSdtKH() + "\n" + "Tổng tiền: " + hoaDon.getTongTien());
		alert.showAndWait();
	}

	// ========== Xem lịch sử Khách hàng ==========
	private void handleXemLichSuKhachHang(KhachHang khachHang) {
		System.out.println("Xem lịch sử khách hàng: " + khachHang.getMaKH());
		// TODO: Mở dialog hoặc màn hình lịch sử khách hàng
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("Lịch sử khách hàng");
		alert.setHeaderText("Thông tin khách hàng: " + khachHang.getMaKH());
		alert.setContentText("Họ tên: " + khachHang.getHoTen() + "\n" + "SĐT: " + khachHang.getSdt() + "\n"
				+ "Địa chỉ: " + khachHang.getDiaChi() + "\n" + "Email: " + khachHang.getEmail() + "\n" + "Loại KH: "
				+ khachHang.getLoaiKH() + "\n" + "Tổng tiền HD: " + khachHang.getTongTienHD());
		alert.showAndWait();
	}

	// ========== Xuất Excel Hóa đơn ==========
	@FXML
	private void handleXuatExcelHoaDon(ActionEvent event) {
		if (danhSachHoaDon.isEmpty()) {
			Alert alert = new Alert(Alert.AlertType.WARNING);
			alert.setTitle("Cảnh báo");
			alert.setHeaderText("Không có dữ liệu");
			alert.setContentText("Vui lòng tìm kiếm hóa đơn trước khi xuất Excel!");
			alert.showAndWait();
			return;
		}
		System.out.println("Xuất Excel danh sách hóa đơn");
		// TODO: Implement xuất Excel
	}

	// ========== Xuất Excel Khách hàng ==========
	@FXML
	private void handleXuatExcelKhachHang(ActionEvent event) {
		if (danhSachKhachHang.isEmpty()) {
			Alert alert = new Alert(Alert.AlertType.WARNING);
			alert.setTitle("Cảnh báo");
			alert.setHeaderText("Không có dữ liệu");
			alert.setContentText("Vui lòng tìm kiếm khách hàng trước khi xuất Excel!");
			alert.showAndWait();
			return;
		}
		System.out.println("Xuất Excel danh sách khách hàng");
		// TODO: Implement xuất Excel
	}

	// ========== Inner class HoaDon ==========
	public static class HoaDon {
		private final SimpleStringProperty maHD;
		private final SimpleStringProperty ngay;
		private final SimpleStringProperty hinhThuc;
		private final SimpleStringProperty sdtKH;
		private final SimpleStringProperty tongTien;

		public HoaDon(String maHD, String ngay, String hinhThuc, String sdtKH, String tongTien) {
			this.maHD = new SimpleStringProperty(maHD);
			this.ngay = new SimpleStringProperty(ngay);
			this.hinhThuc = new SimpleStringProperty(hinhThuc);
			this.sdtKH = new SimpleStringProperty(sdtKH);
			this.tongTien = new SimpleStringProperty(tongTien);
		}

		public String getMaHD() {
			return maHD.get();
		}

		public void setMaHD(String value) {
			maHD.set(value);
		}

		public SimpleStringProperty maHDProperty() {
			return maHD;
		}

		public String getNgay() {
			return ngay.get();
		}

		public void setNgay(String value) {
			ngay.set(value);
		}

		public SimpleStringProperty ngayProperty() {
			return ngay;
		}

		public String getHinhThuc() {
			return hinhThuc.get();
		}

		public void setHinhThuc(String value) {
			hinhThuc.set(value);
		}

		public SimpleStringProperty hinhThucProperty() {
			return hinhThuc;
		}

		public String getSdtKH() {
			return sdtKH.get();
		}

		public void setSdtKH(String value) {
			sdtKH.set(value);
		}

		public SimpleStringProperty sdtKHProperty() {
			return sdtKH;
		}

		public String getTongTien() {
			return tongTien.get();
		}

		public void setTongTien(String value) {
			tongTien.set(value);
		}

		public SimpleStringProperty tongTienProperty() {
			return tongTien;
		}
	}

	// ========== Inner class KhachHang ==========
	public static class KhachHang {
		private final SimpleStringProperty maKH;
		private final SimpleStringProperty hoTen;
		private final SimpleStringProperty sdt;
		private final SimpleStringProperty diaChi;
		private final SimpleStringProperty email;
		private final SimpleStringProperty ngayDK;
		private final SimpleStringProperty loaiKH;
		private final SimpleStringProperty tongTienHD;

		public KhachHang(String maKH, String hoTen, String sdt, String diaChi, String email, String ngayDK,
				String loaiKH, String tongTienHD) {
			this.maKH = new SimpleStringProperty(maKH);
			this.hoTen = new SimpleStringProperty(hoTen);
			this.sdt = new SimpleStringProperty(sdt);
			this.diaChi = new SimpleStringProperty(diaChi);
			this.email = new SimpleStringProperty(email);
			this.ngayDK = new SimpleStringProperty(ngayDK);
			this.loaiKH = new SimpleStringProperty(loaiKH);
			this.tongTienHD = new SimpleStringProperty(tongTienHD);
		}

		public String getMaKH() {
			return maKH.get();
		}

		public void setMaKH(String value) {
			maKH.set(value);
		}

		public SimpleStringProperty maKHProperty() {
			return maKH;
		}

		public String getHoTen() {
			return hoTen.get();
		}

		public void setHoTen(String value) {
			hoTen.set(value);
		}

		public SimpleStringProperty hoTenProperty() {
			return hoTen;
		}

		public String getSdt() {
			return sdt.get();
		}

		public void setSdt(String value) {
			sdt.set(value);
		}

		public SimpleStringProperty sdtProperty() {
			return sdt;
		}

		public String getDiaChi() {
			return diaChi.get();
		}

		public void setDiaChi(String value) {
			diaChi.set(value);
		}

		public SimpleStringProperty diaChiProperty() {
			return diaChi;
		}

		public String getEmail() {
			return email.get();
		}

		public void setEmail(String value) {
			email.set(value);
		}

		public SimpleStringProperty emailProperty() {
			return email;
		}

		public String getNgayDK() {
			return ngayDK.get();
		}

		public void setNgayDK(String value) {
			ngayDK.set(value);
		}

		public SimpleStringProperty ngayDKProperty() {
			return ngayDK;
		}

		public String getLoaiKH() {
			return loaiKH.get();
		}

		public void setLoaiKH(String value) {
			loaiKH.set(value);
		}

		public SimpleStringProperty loaiKHProperty() {
			return loaiKH;
		}

		public String getTongTienHD() {
			return tongTienHD.get();
		}

		public void setTongTienHD(String value) {
			tongTienHD.set(value);
		}

		public SimpleStringProperty tongTienHDProperty() {
			return tongTienHD;
		}
	}
}
package entity;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * Lớp Entity (Model) đại diện cho một dòng trong bảng Chi Tiết Hóa Đơn. Sử dụng
 * JavaFX Properties để dễ dàng hiển thị lên TableView.
 */
public class ChiTietHoaDon {

	private final SimpleStringProperty tenMon;
	private final SimpleIntegerProperty soLuong;
	private final SimpleDoubleProperty donGia;
	private final SimpleDoubleProperty thanhTien;

	public ChiTietHoaDon(String tenMon, int soLuong, double donGia, double thanhTien) {
		this.tenMon = new SimpleStringProperty(tenMon);
		this.soLuong = new SimpleIntegerProperty(soLuong);
		this.donGia = new SimpleDoubleProperty(donGia);
		this.thanhTien = new SimpleDoubleProperty(thanhTien);
	}

	// --- Getters (JavaFX Style) ---

	public String getTenMon() {
		return tenMon.get();
	}

	public SimpleStringProperty tenMonProperty() {
		return tenMon;
	}

	public int getSoLuong() {
		return soLuong.get();
	}

	public SimpleIntegerProperty soLuongProperty() {
		return soLuong;
	}

	public double getDonGia() {
		return donGia.get();
	}

	public SimpleDoubleProperty donGiaProperty() {
		return donGia;
	}

	public double getThanhTien() {
		return thanhTien.get();
	}

	public SimpleDoubleProperty thanhTienProperty() {
		return thanhTien;
	}
}
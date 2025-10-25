package entity;

public class MonAn {
	private String maMon;
	private String tenMon;
	private byte[] hinhAnh; // Dùng byte[] để lưu dữ liệu VARBINARY
	private double giaBan;
	private String maDM;

	// Trường này không có trong CSDL, nhưng DAO sẽ join để lấy
	private String tenDanhMuc;

	// Constructors
	public MonAn() {
	}

	public MonAn(String maMon, String tenMon, byte[] hinhAnh, double giaBan, String maDM) {
		this.maMon = maMon;
		this.tenMon = tenMon;
		this.hinhAnh = hinhAnh;
		this.giaBan = giaBan;
		this.maDM = maDM;
	}

	// Getters and Setters
	public String getMaMon() {
		return maMon;
	}

	public void setMaMon(String maMon) {
		this.maMon = maMon;
	}

	public String getTenMon() {
		return tenMon;
	}

	public void setTenMon(String tenMon) {
		this.tenMon = tenMon;
	}

	public byte[] getHinhAnh() {
		return hinhAnh;
	}

	public void setHinhAnh(byte[] hinhAnh) {
		this.hinhAnh = hinhAnh;
	}

	public double getGiaBan() {
		return giaBan;
	}

	public void setGiaBan(double giaBan) {
		this.giaBan = giaBan;
	}

	public String getMaDM() {
		return maDM;
	}

	public void setMaDM(String maDM) {
		this.maDM = maDM;
	}

	// Getter/Setter cho trường join
	public String getTenDanhMuc() {
		return tenDanhMuc;
	}

	public void setTenDanhMuc(String tenDanhMuc) {
		this.tenDanhMuc = tenDanhMuc;
	}
}
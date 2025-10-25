package entity;

public class Ban {
	private String maBan;
	private String viTri;
	private int sucChua;
	private LoaiBan loaiBan;
	private TrangThaiBan trangThai;

	public Ban() {
	}

	public Ban(String maBan, String viTri, int sucChua, LoaiBan loaiBan, TrangThaiBan trangThai) {
		this.maBan = maBan;
		this.viTri = viTri;
		this.sucChua = sucChua;
		this.loaiBan = loaiBan;
		this.trangThai = trangThai;
	}

	// Getters and Setters
	public String getMaBan() {
		return maBan;
	}

	public void setMaBan(String maBan) {
		this.maBan = maBan;
	}

	public String getViTri() {
		return viTri;
	}

	public void setViTri(String viTri) {
		this.viTri = viTri;
	}

	public int getSucChua() {
		return sucChua;
	}

	public void setSucChua(int sucChua) {
		this.sucChua = sucChua;
	}

	public LoaiBan getLoaiBan() {
		return loaiBan;
	}

	public void setLoaiBan(LoaiBan loaiBan) {
		this.loaiBan = loaiBan;
	}

	public TrangThaiBan getTrangThai() {
		return trangThai;
	}

	public void setTrangThai(TrangThaiBan trangThai) {
		this.trangThai = trangThai;
	}
}
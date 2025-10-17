package entity;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class NhanVien {
    private final StringProperty maNV;
    private final StringProperty hoTen;
    private final StringProperty sdt;
    private final StringProperty chucVu;
    private final StringProperty matKhau;
    private final StringProperty ngayVaoLam;
    private final StringProperty caLam;
    private final StringProperty trangThai;

    public NhanVien(String maNV, String hoTen, String sdt, String chucVu, String matKhau, String ngayVaoLam, String caLam, String trangThai) {
        this.maNV = new SimpleStringProperty(maNV);
        this.hoTen = new SimpleStringProperty(hoTen);
        this.sdt = new SimpleStringProperty(sdt);
        this.chucVu = new SimpleStringProperty(chucVu);
        this.matKhau = new SimpleStringProperty(matKhau);
        this.ngayVaoLam = new SimpleStringProperty(ngayVaoLam);
        this.caLam = new SimpleStringProperty(caLam);
        this.trangThai = new SimpleStringProperty(trangThai);
    }
    
    // Phương thức getter cho JavaFX TableView
    public String getMaNV() { return maNV.get(); }
    public String getHoTen() { return hoTen.get(); }
    public String getSdt() { return sdt.get(); }
    public String getChucVu() { return chucVu.get(); }
    public String getMatKhau() { return matKhau.get(); }
    public String getNgayVaoLam() { return ngayVaoLam.get(); }
    public String getCaLam() { return caLam.get(); }
    public String getTrangThai() { return trangThai.get(); }
}
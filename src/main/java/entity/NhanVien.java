package entity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class NhanVien {
    // Thuộc tính Entity (Dùng cho DAO và DB)
    private String maNV_entity;
    private String tenNV_entity; // Tên đã được thống nhất
    private String soDT_entity;
    private String email_entity;
    private LocalDate ngaySinh_entity;
    private String diaChi_entity;
    private boolean gioiTinh_entity; // true = Nam, false = Nữ
    private boolean trangThai_entity; // true = Đang làm
    private String caLamYeuThich_entity; // 🔥 THUỘC TÍNH MỚI (Lưu "Sáng", "Chiều"...)

    // Thuộc tính cho JavaFX UI Binding
    private final StringProperty maNV;
    private final StringProperty hoTen; // Tên hiển thị trên UI
    private final StringProperty sdt;
    private final StringProperty email;
    private final StringProperty ngaySinh;
    private final StringProperty diaChi;
    private final StringProperty gioiTinh;
    private final StringProperty trangThai;
    private final StringProperty chucVu;
    private final StringProperty matKhau;
    private final StringProperty caLam; // Vẫn giữ (Dùng cho cột ca làm THỰC TẾ)
    private final StringProperty caLamYeuThich; // 🔥 THUỘC TÍNH MỚI (Dùng cho ComboBox)

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public NhanVien() {
        this.maNV = new SimpleStringProperty();
        this.hoTen = new SimpleStringProperty();
        this.sdt = new SimpleStringProperty();
        this.email = new SimpleStringProperty();
        this.ngaySinh = new SimpleStringProperty();
        this.diaChi = new SimpleStringProperty();
        this.gioiTinh = new SimpleStringProperty();
        this.trangThai = new SimpleStringProperty();
        this.chucVu = new SimpleStringProperty();
        this.matKhau = new SimpleStringProperty();
        this.caLam = new SimpleStringProperty(); // Ca làm thực tế
        this.caLamYeuThich = new SimpleStringProperty(); // 🔥 Ca làm yêu thích
    }

    // Getters & Setters cho UI Properties (phần này sẽ tự động cập nhật Entity)
    public String getMaNV() { return maNV.get(); }
    public void setMaNV(String maNV) { this.maNV.set(maNV); this.maNV_entity = maNV; }
    public StringProperty maNVProperty() { return maNV; }

    public String getHoTen() { return hoTen.get(); }
    public void setHoTen(String hoTen) { this.hoTen.set(hoTen); this.tenNV_entity = hoTen; }
    public StringProperty hoTenProperty() { return hoTen; }

    public String getSdt() { return sdt.get(); }
    public void setSdt(String sdt) { this.sdt.set(sdt); this.soDT_entity = sdt; }
    public StringProperty sdtProperty() { return sdt; }

    public String getEmail() { return email.get(); }
    public void setEmail(String email) { this.email.set(email); this.email_entity = email; }
    public StringProperty emailProperty() { return email; }

    public String getNgaySinh() { return ngaySinh.get(); }
    public void setNgaySinh(String ngaySinhStr) {
        this.ngaySinh.set(ngaySinhStr);
        this.ngaySinh_entity = parseDate(ngaySinhStr);
    }
    public StringProperty ngaySinhProperty() { return ngaySinh; }

    public String getDiaChi() { return diaChi.get(); }
    public void setDiaChi(String diaChi) { this.diaChi.set(diaChi); this.diaChi_entity = diaChi; }
    public StringProperty diaChiProperty() { return diaChi; }

    public String getGioiTinh() { return gioiTinh.get(); }
    public void setGioiTinh(String gioiTinh) {
        this.gioiTinh.set(gioiTinh);
        this.gioiTinh_entity = "Nam".equals(gioiTinh);
    }
    public StringProperty gioiTinhProperty() { return gioiTinh; }

    public String getTrangThai() { return trangThai.get(); }
    public void setTrangThai(String trangThai) {
        this.trangThai.set(trangThai);
        this.trangThai_entity = "Đang làm".equals(trangThai);
    }
    public StringProperty trangThaiProperty() { return trangThai; }

    // Các thuộc tính liên kết từ bảng khác
    public String getChucVu() { return chucVu.get(); }
    public void setChucVu(String chucVu) { this.chucVu.set(chucVu); }
    public StringProperty chucVuProperty() { return chucVu; }

    public String getMatKhau() { return matKhau.get(); }
    public void setMatKhau(String matKhau) { this.matKhau.set(matKhau); }
    public StringProperty matKhauProperty() { return matKhau; }

    // Ca làm (THỰC TẾ - Lấy từ CaTrucDAO)
    public String getCaLam() { return caLam.get(); }
    public void setCaLam(String caLam) { this.caLam.set(caLam); }
    public StringProperty caLamProperty() { return caLam; }

    // 🔥 Ca làm YÊU THÍCH (MỚI - Lấy từ CSDL NhanVien)
    public String getCaLamYeuThich() { return caLamYeuThich.get(); }
    public void setCaLamYeuThich(String caLamYeuThich) {
        this.caLamYeuThich.set(caLamYeuThich);
        this.caLamYeuThich_entity = caLamYeuThich;
    }
    public StringProperty caLamYeuThichProperty() { return caLamYeuThich; }


    // Getters cho DAO sử dụng
    public String getTenNV_entity() { return tenNV_entity; }
    public String getSoDT_entity() { return soDT_entity; }
    public String getEmail_entity() { return email_entity; }
    public LocalDate getNgaySinh_entity() { return ngaySinh_entity; }
    public String getDiaChi_entity() { return diaChi_entity; }
    public boolean getGioiTinh_entity() { return gioiTinh_entity; }
    public boolean getTrangThai_entity() { return trangThai_entity; }
    public String getCaLamYeuThich_entity() { return caLamYeuThich_entity; } // 🔥 Getter MỚI

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        try {
            return LocalDate.parse(dateStr, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
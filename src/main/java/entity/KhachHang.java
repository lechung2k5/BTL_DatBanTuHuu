// KhachHang.java (Đã sửa)
package entity;

import java.time.LocalDate;

public class KhachHang {
    private String maKH;
    private String tenKH;
    private String soDT;
    private String email;
    // 🔥 ĐÃ LOẠI BỎ: private LocalDate ngaySinh; 
    private LocalDate ngayDangKy; // Giữ nguyên tên để khớp với CSDL
    private String diaChi; 
    private String thanhVien; 

    // Constructors
    public KhachHang() {
    }

    // 🔥 Constructor 6 tham số (Thay thế ngaySinh bằng ngayDangKy)
    // Dùng cho DatBanDAO (Chỉ lấy các trường cơ bản)
    public KhachHang(String maKH, String tenKH, String soDT, String email, LocalDate ngayDangKy, String thanhVien) {
        this.maKH = maKH;
        this.tenKH = tenKH;
        this.soDT = soDT;
        this.email = email;
        this.ngayDangKy = ngayDangKy; // 🔥 Vị trí này trước đây là ngaySinh
        this.thanhVien = thanhVien;
    }

    // 🔥 Constructor 8 tham số ĐÃ SỬA thành 7 tham số (Cho KhachHangDAO)
    // Bỏ ngaySinh (1 tham số)
    public KhachHang(String maKH, String tenKH, String soDT, String email, LocalDate ngayDangKy, String diaChi, String thanhVien) {
        this.maKH = maKH;
        this.tenKH = tenKH;
        this.soDT = soDT;
        this.email = email;
        this.ngayDangKy = ngayDangKy; // 🔥 Đã loại bỏ ngaySinh
        this.diaChi = diaChi;
        this.thanhVien = thanhVien;
    }
    
    // Getters and Setters
    public String getMaKH() { return maKH; }
    public void setMaKH(String maKH) { this.maKH = maKH; }
    public String getTenKH() { return tenKH; }
    public void setTenKH(String tenKH) { this.tenKH = tenKH; }
    public String getSoDT() { return soDT; } 
    public void setSoDT(String soDT) { this.soDT = soDT; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    // 🔥 ĐÃ XÓA getNgaySinh() và setNgaySinh()
    /*
    public LocalDate getNgaySinh() { return ngaySinh; }
    public void setNgaySinh(LocalDate ngaySinh) { this.ngaySinh = ngaySinh; }
    */
    
    // Giữ nguyên getter/setter cho ngayDangKy (đã khớp DB)
    public LocalDate getNgayDangKy() { return ngayDangKy; } 
    public void setNgayDangKy(LocalDate ngayDangKy) { this.ngayDangKy = ngayDangKy; }
    
    public String getDiaChi() { return diaChi; } 
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }
    
    public String getThanhVien() { return thanhVien; }
    public void setThanhVien(String thanhVien) { this.thanhVien = thanhVien; }
    
    @Override
    public String toString() {
        return "KhachHang{" +
                "maKH='" + maKH + '\'' +
                ", tenKH='" + tenKH + '\'' +
                ", soDT='" + soDT + '\'' +
                ", ngayDangKy='" + ngayDangKy + '\'' +
                '}';
    }
}
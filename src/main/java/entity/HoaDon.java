package entity;

import java.time.LocalDateTime;

public class HoaDon {
    private String maHD;
    private LocalDateTime ngayLap; // Thêm theo Class Diagram
    private String maUuDai; // Thêm theo Class Diagram
    private String ptThanhToan; // Thêm theo Class Diagram
    private String trangThai;
    private LocalDateTime gioVao;
    private LocalDateTime gioRa; // Thêm theo Class Diagram
    private Double tienCoc; // Thêm theo Class Diagram (Giả định là Double)
    
    private KhachHang khachHang; 
    private Ban ban; 
    // Các entity liên quan (NhanVien, UuDai) có thể được thêm nếu cần ánh xạ đầy đủ

    // Constructors
    public HoaDon() {
    }

    // Constructor đầy đủ (Ví dụ - có thể cần điều chỉnh tùy theo nhu cầu)
    public HoaDon(String maHD, LocalDateTime ngayLap, String maUuDai, String ptThanhToan, String trangThai, 
                  LocalDateTime gioVao, LocalDateTime gioRa, Double tienCoc, KhachHang khachHang, Ban ban) {
        this.maHD = maHD;
        this.ngayLap = ngayLap;
        this.maUuDai = maUuDai;
        this.ptThanhToan = ptThanhToan;
        this.trangThai = trangThai;
        this.gioVao = gioVao;
        this.gioRa = gioRa;
        this.tienCoc = tienCoc;
        this.khachHang = khachHang;
        this.ban = ban;
    }


    // Getters và Setters
    public String getMaHD() { return maHD; }
    public void setMaHD(String maHD) { this.maHD = maHD; }
    
    public LocalDateTime getNgayLap() { return ngayLap; }
    public void setNgayLap(LocalDateTime ngayLap) { this.ngayLap = ngayLap; }

    public String getMaUuDai() { return maUuDai; }
    public void setMaUuDai(String maUuDai) { this.maUuDai = maUuDai; }

    public String getPtThanhToan() { return ptThanhToan; }
    public void setPtThanhToan(String ptThanhToan) { this.ptThanhToan = ptThanhToan; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    
    public LocalDateTime getGioVao() { return gioVao; }
    public void setGioVao(LocalDateTime gioVao) { this.gioVao = gioVao; }
    
    public LocalDateTime getGioRa() { return gioRa; }
    public void setGioRa(LocalDateTime gioRa) { this.gioRa = gioRa; }

    public Double getTienCoc() { return tienCoc; }
    public void setTienCoc(Double tienCoc) { this.tienCoc = tienCoc; }

    public KhachHang getKhachHang() { return khachHang; }
    public void setKhachHang(KhachHang khachHang) { this.khachHang = khachHang; }

    public Ban getBan() { return ban; }
    public void setBan(Ban ban) { this.ban = ban; }
 // Trong file entity/HoaDon.java
    public String getMaKH() {
        // Giả định bạn đã có private KhachHang khachHang; và phương thức getKhachHang()
        return khachHang != null ? khachHang.getMaKH() : null;
    }
}
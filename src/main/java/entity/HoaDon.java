package entity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Lớp Entity (Model) đại diện cho đối tượng Hóa Đơn.
 * Đã cập nhật hoàn chỉnh để đồng bộ với các DAO và Controller.
 */
public class HoaDon {

    private String maHD; // Đổi tên cho khớp DAO
    private LocalDateTime ngayLap;
    private PTTThanhToan hinhThucTT;
    private TrangThaiHoaDon trangThai; // Thuộc tính kiểu Enum
    private String maUuDai;
    private KhachHang khachHang; 
    private String tenNhanVien;  
    private Ban ban;             
    private LocalDateTime gioVao;
    private LocalDateTime gioRa;
    private double tongCongMonAn; 
    private double tienCoc;

    // Các trường tính toán
    private double phiDichVu;
    private double thueVAT;
    private double khuyenMai;
    private double tongTienThanhToan;

    // Constructors
    public HoaDon() {
        // Constructor rỗng để DAO dễ sử dụng setters
    }
    

    // Hàm tính toán tổng tiền (có thể gọi lại khi thêm/bớt món hoặc cập nhật ưu đãi)
    public void calculateTotals() {
        // === Logic tính toán ===
        this.phiDichVu = this.tongCongMonAn * 0.05; // Giả định 5%
        this.thueVAT = this.tongCongMonAn * 0.08;   // Giả định 8%

        // TODO: Cần logic phức tạp hơn để tính khuyến mãi dựa trên maUuDai và tổng tiền món
        this.khuyenMai = 0.0;

        this.tongTienThanhToan = this.tongCongMonAn + this.phiDichVu + this.thueVAT - this.tienCoc - this.khuyenMai;
        if (this.tongTienThanhToan < 0) {
            this.tongTienThanhToan = 0; // Đảm bảo tổng tiền không âm
        }
    }


    // --- Getters ---
    public String getMaHD() { return maHD; }
    public LocalDateTime getNgayLap() { return ngayLap; }
    public PTTThanhToan getHinhThucTT() { return hinhThucTT; }
    public TrangThaiHoaDon getTrangThai() { return trangThai; }
    public String getMaUuDai() { return maUuDai; }
    public KhachHang getKhachHang() { return khachHang; }
    public String getTenNhanVien() { return tenNhanVien; }
    public Ban getBan() { return ban; }
    public LocalDateTime getGioVao() { return gioVao; }
    public LocalDateTime getGioRa() { return gioRa; }
    public double getTongCongMonAn() { return tongCongMonAn; }
    public double getTienCoc() { return tienCoc; }
    public double getPhiDichVu() { return phiDichVu; }
    public double getThueVAT() { return thueVAT; }
    public double getKhuyenMai() { return khuyenMai; }
    public double getTongTienThanhToan() { return tongTienThanhToan; }

    // --- Getters tiện ích cho UI ---
    /** Lấy mã bàn (String) hoặc null nếu không có bàn. */
    public String getMaBan() {
        return (this.ban != null) ? this.ban.getMaBan() : null;
    }
    /** Lấy số điện thoại KH (String) hoặc null nếu không có KH. */
    public String getSoDienThoaiKH() {
        return (this.khachHang != null) ? this.khachHang.getSoDT() : null;
    }


    // --- Setters ---
    public void setMaHD(String maHD) { this.maHD = maHD; }
    public void setNgayLap(LocalDateTime ngayLap) { this.ngayLap = ngayLap; }
    public void setHinhThucTT(PTTThanhToan hinhThucTT) { this.hinhThucTT = hinhThucTT; }
    
    // Setter 1: Nhận Enum (Dùng cho Controller/Logic nội bộ)
    public void setTrangThai(TrangThaiHoaDon trangThai) { this.trangThai = trangThai; }

    // Setter 2: Nhận String (Dùng cho DAO/đọc từ DB)
    public void setTrangThai(String trangThaiDbValue) { 
        // 🔥 FIX LỖI DÒNG 86: Dùng hàm chuyển đổi tĩnh từ Enum
        this.trangThai = TrangThaiHoaDon.fromDbValue(trangThaiDbValue); 
    }

    public void setMaUuDai(String maUuDai) { this.maUuDai = maUuDai; }
    public void setKhachHang(KhachHang khachHang) { this.khachHang = khachHang; }
    public void setTenNhanVien(String tenNhanVien) { this.tenNhanVien = tenNhanVien; }
    public void setBan(Ban ban) { this.ban = ban; }
    public void setGioVao(LocalDateTime gioVao) { this.gioVao = gioVao; }
    public void setGioRa(LocalDateTime gioRa) { this.gioRa = gioRa; }
    public void setTongCongMonAn(double tongCongMonAn) {
        this.tongCongMonAn = tongCongMonAn;
        calculateTotals(); // Tính lại tổng tiền khi tổng món ăn thay đổi
    }
    public void setTienCoc(double tienCoc) {
        this.tienCoc = tienCoc;
        calculateTotals(); // Tính lại tổng tiền khi tiền cọc thay đổi
    }
    // Không cần setters cho các trường tính toán
}
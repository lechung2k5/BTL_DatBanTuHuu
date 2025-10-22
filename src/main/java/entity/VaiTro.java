package entity;

// Enum VaiTro dựa trên CSDL (đã sửa thành tiếng Việt)
public enum VaiTro {
    // 🔥 SỬA LẠI: Giá trị Enum phải khớp 100% với chuỗi trong CSDL
    QUAN_LY("Quản lý"),
    NHAN_VIEN_THU_NGAN("Nhân viên thu ngân");

    private final String tenVaiTro; // Sẽ lưu "Quản lý", "Nhân viên thu ngân"

    VaiTro(String tenVaiTro) {
        this.tenVaiTro = tenVaiTro;
    }

    // Hàm này trả về tên ("Quản lý") để ManHinhChinh hiển thị
    public String getTenVaiTro() {
        return tenVaiTro;
    }

    @Override
    public String toString() {
        return tenVaiTro;
    }

    // Phương thức này BÂY GIỜ SẼ HOẠT ĐỘNG
    public static VaiTro fromString(String text) {
        if (text == null) return null;
        for (VaiTro b : VaiTro.values()) {
            // Sẽ so sánh: "Quản lý" (từ DB) .equalsIgnoreCase("Quản lý" (từ Enum)) -> TRUE
            if (b.tenVaiTro.equalsIgnoreCase(text)) {
                return b;
            }
        }
        // Sẽ không trả về null nữa nếu CSDL khớp
        System.err.println("Không tìm thấy VaiTro khớp với: " + text);
        return null; 
    }
}
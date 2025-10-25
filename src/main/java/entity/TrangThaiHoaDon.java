package entity;

import java.text.Normalizer;
import java.util.regex.Pattern;

/**
 * Enum đại diện cho các Trạng Thái Hóa Đơn.
 */
public enum TrangThaiHoaDon {
    // === Đảm bảo tên hiển thị khớp với ComboBox trong UI ===
    DAT("Dat", "Đã đặt"), 
    DA_THANH_TOAN("DaThanhToan", "Đã thanh toán"),
    DA_HUY("DaHuy", "Đã hủy"), 
    DANG_SU_DUNG("DangSuDung", "Đang phục vụ"), // Đã sửa từ "Đang Sử Dụng" sang "Đang phục vụ"
    HOA_DON_TAM("HoaDonTam", "Hóa đơn tạm"); 
	
    // ========================================================

    private final String dbValue;
    private final String displayName;

    TrangThaiHoaDon(String dbValue, String displayName) {
        this.dbValue = dbValue;
        this.displayName = displayName;
    }

    public String getDbValue() {
        return dbValue;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Tìm Enum tương ứng dựa vào giá trị lưu trong CSDL.
     */
    public static TrangThaiHoaDon fromDbValue(String dbValue) {
        if (dbValue == null) {
            return null;
        }
        String trimmedDbValue = dbValue.trim(); 
        for (TrangThaiHoaDon tt : values()) {
            if (tt.dbValue.equalsIgnoreCase(trimmedDbValue)) {
                return tt; 
            }
        }
        System.err.println("CẢNH BÁO: Không tìm thấy TrangThaiHoaDon cho dbValue: '" + dbValue + "'");
        return null;
    }

    /**
     * Tìm Enum tương ứng dựa vào tên hiển thị.
     * === FIX CUỐI CÙNG: Sử dụng Normalizer để loại bỏ dấu và khoảng trắng, đảm bảo so sánh chính xác ===
     * @param displayName Tên hiển thị (vd: "Đã Thanh Toán", "Đang phục vụ")
     * @return Enum TrangThaiHoaDon tương ứng, hoặc null nếu không tìm thấy.
     */
    public static TrangThaiHoaDon fromDisplayName(String displayName) {
         if (displayName == null) {
            return null;
        }
        
        // Chuẩn hóa chuỗi đầu vào (Loại bỏ dấu, khoảng trắng, và chuyển sang chữ hoa)
        String standardizedInput = standardizeString(displayName);

        for (TrangThaiHoaDon tt : values()) {
            // Chuẩn hóa displayName của Enum 
            String standardizedEnumName = standardizeString(tt.displayName);
            
            if (standardizedEnumName.equals(standardizedInput)) {
                return tt;
            }
        }
        return null; 
    }
    
    /**
     * Helper: Chuẩn hóa chuỗi bằng cách loại bỏ dấu tiếng Việt, khoảng trắng và chuyển thành chữ hoa.
     */
    private static String standardizeString(String input) {
        if (input == null) return "";
        // 1. Chuẩn hóa NFD (phân tách ký tự thành base + dấu)
        String temp = Normalizer.normalize(input, Normalizer.Form.NFD);
        // 2. Loại bỏ các ký tự dấu
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        temp = pattern.matcher(temp).replaceAll("");
        // 3. Loại bỏ khoảng trắng và chuyển thành chữ hoa
        return temp.replaceAll("\\s+", "").toUpperCase();
    }


    @Override
    public String toString() {
        // Mặc định trả về tên hiển thị
        return displayName;
    }
}
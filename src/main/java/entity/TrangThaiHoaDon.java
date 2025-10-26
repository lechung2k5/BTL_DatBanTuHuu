package entity;

import java.text.Normalizer;
import java.util.regex.Pattern;

/**
 * Enum đại diện cho các Trạng Thái Hóa Đơn.
 * 🔥 ĐÃ THÊM: Trạng thái CHO_XAC_NHAN
 */
public enum TrangThaiHoaDon {
    // === Đảm bảo tên hiển thị khớp với ComboBox trong UI ===
    DAT("Dat", "Đã đặt"),
    DA_THANH_TOAN("DaThanhToan", "Đã thanh toán"),
    DA_HUY("DaHuy", "Đã hủy"),
    DANG_SU_DUNG("DangSuDung", "Đang phục vụ"),
    HOA_DON_TAM("HoaDonTam", "Hóa đơn tạm"),
    CHO_XAC_NHAN("ChoXacNhan", "Chờ xác nhận"); // <<< THÊM MỚI

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
        return null; // Hoặc ném Exception nếu muốn chặt chẽ hơn
    }

    /**
     * Tìm Enum tương ứng dựa vào tên hiển thị.
     */
    public static TrangThaiHoaDon fromDisplayName(String displayName) {
         if (displayName == null) {
            return null;
        }
        String standardizedInput = standardizeString(displayName);
        for (TrangThaiHoaDon tt : values()) {
            String standardizedEnumName = standardizeString(tt.displayName);
            if (standardizedEnumName.equals(standardizedInput)) {
                return tt;
            }
        }
         System.err.println("CẢNH BÁO: Không tìm thấy TrangThaiHoaDon cho displayName: '" + displayName + "'");
        return null; // Hoặc ném Exception
    }

    /**
     * Helper: Chuẩn hóa chuỗi.
     */
    private static String standardizeString(String input) {
        if (input == null) return "";
        String temp = Normalizer.normalize(input, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        temp = pattern.matcher(temp).replaceAll("");
        return temp.replaceAll("\\s+", "").toUpperCase();
    }


    @Override
    public String toString() {
        return displayName;
    }
}
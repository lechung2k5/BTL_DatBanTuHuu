package entity;

/**
 * Enum đại diện cho các Trạng Thái Hóa Đơn.
 */
public enum TrangThaiHoaDon {
    DAT("Dat", "Đặt"),                         // Mã trong DB, Tên hiển thị
    DA_THANH_TOAN("DaThanhToan", "Đã Thanh Toán"),
    DA_HUY("DaHuy", "Đã Hủy"), DANG_SU_DUNG("DangSuDung", "Đang Sử Dụng"); // Ví dụ thêm trạng thái Hủy

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
     * @param dbValue Giá trị từ cột trangThai trong DB (vd: "DaThanhToan")
     * @return Enum TrangThaiHoaDon tương ứng, hoặc null nếu không tìm thấy.
     */
    public static TrangThaiHoaDon fromDbValue(String dbValue) {
        if (dbValue == null) {
            return null;
        }
        for (TrangThaiHoaDon tt : values()) {
            if (tt.dbValue.equalsIgnoreCase(dbValue)) {
                return tt;
            }
        }
        return null; // Hoặc ném Exception
    }

     /**
     * Tìm Enum tương ứng dựa vào tên hiển thị.
     * @param displayName Tên hiển thị (vd: "Đã Thanh Toán")
     * @return Enum TrangThaiHoaDon tương ứng, hoặc null nếu không tìm thấy.
     */
    public static TrangThaiHoaDon fromDisplayName(String displayName) {
         if (displayName == null) {
            return null;
        }
        for (TrangThaiHoaDon tt : values()) {
            if (tt.displayName.equalsIgnoreCase(displayName)) {
                return tt;
            }
        }
        return null; // Hoặc ném Exception
    }


    @Override
    public String toString() {
        // Mặc định trả về tên hiển thị
        return displayName;
    }
}
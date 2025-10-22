package entity;

public enum TrangThaiBan {
    TRONG("Trong"),
    DANG_SU_DUNG("DangSuDung"),
    DA_DAT("DaDat");

    private final String dbValue;

    TrangThaiBan(String dbValue) {
        this.dbValue = dbValue;
    }

    @Override
    public String toString() {
        return dbValue;
    }

    /**
     * 🔥 SỬA CHỮA CỐ ĐỊNH LỖI MAPPING ENUM 🔥
     * Xử lý các giá trị từ DB (Trong, DangSuDung, Dat/DaDat)
     */
    public static TrangThaiBan fromString(String text) {
        if (text == null) return TRONG;
        
        String standardizedText = text.toUpperCase().trim();
        
        // Cần ánh xạ chính xác các giá trị từ SQL (DangSuDung, Dat, DaDat)
        switch (standardizedText) {
            case "TRONG":
                return TRONG;
            case "DANGSUDUNG": // Giá trị từ DB
                return DANG_SU_DUNG;
            case "DAT": // Giá trị từ INSERT trong SQL
            case "DADAT": // Giá trị từ ENUM và các bản ghi khác
                return DA_DAT;
            default:
                return TRONG; // Mặc định là TRONG nếu không khớp
        }
    }
}
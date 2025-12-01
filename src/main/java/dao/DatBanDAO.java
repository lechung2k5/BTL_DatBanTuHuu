package dao;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import entity.*; // Import hết entity
import connect.ConnectDB;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import ui.DatBan.MonOrder; // Giả sử MonOrder nằm ở đây
import ui.TachBanPopupController;
import ui.TachBanPopupController.MonTach;

public class DatBanDAO {

    /**
     * Lấy mã hóa đơn tiếp theo (ví dụ: HD011 nếu mã lớn nhất là HD010).
     */
    public String getNextMaHD() {
        // ... (Logic giữ nguyên)
        String maHD = "HD001"; // Giá trị mặc định nếu bảng trống
        String sql = "SELECT MAX(maHD) FROM HoaDon";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                String maxMaHD = rs.getString(1);
                if (maxMaHD != null && maxMaHD.matches("HD\\d{3}")) { // Kiểm tra định dạng HDxxx
                    try {
                        int num = Integer.parseInt(maxMaHD.substring(2)) + 1;
                        maHD = String.format("HD%03d", num);
                    } catch (NumberFormatException e) {
                        System.err.println("Lỗi parse mã HD cuối cùng: " + maxMaHD);
                        // Có thể xử lý bằng cách tạo mã ngẫu nhiên hoặc throw exception
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy mã HD tiếp theo: " + e.getMessage());
            e.printStackTrace();
        }
        return maHD;
    }

    /**
     * Lấy mã khách hàng tiếp theo (ví dụ: KH007 nếu mã lớn nhất là KH006).
     */
    public String getNextMaKH() {
        // ... (Logic giữ nguyên)
        String maKH = "KH001"; // Giá trị mặc định nếu bảng trống
        String sql = "SELECT MAX(maKH) FROM KhachHang";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                String maxMaKH = rs.getString(1);
                 if (maxMaKH != null && maxMaKH.matches("KH\\d{3}")) { // Kiểm tra định dạng KHxxx
                     try {
                        int num = Integer.parseInt(maxMaKH.substring(2)) + 1;
                        maKH = String.format("KH%03d", num);
                     } catch (NumberFormatException e) {
                         System.err.println("Lỗi parse mã KH cuối cùng: " + maxMaKH);
                     }
                }
            }
        } catch (SQLException e) {
             System.err.println("Lỗi khi lấy mã KH tiếp theo: " + e.getMessage());
            e.printStackTrace();
        }
        return maKH;
    }

    /**
     * Tìm khách hàng theo SĐT, nếu không thấy thì tạo mới.
     * @param sdt Số điện thoại cần tìm/tạo.
     * @param tenKH Tên khách hàng (chỉ dùng nếu tạo mới, có thể trống).
     * @return Đối tượng KhachHang tìm thấy hoặc vừa tạo.
     * @throws SQLException Nếu có lỗi CSDL.
     */
     public KhachHang timHoacTaoKhachHang(String sdt, String tenKH) throws SQLException {
        // 1. Tìm kiếm khách hàng theo SĐT
        String selectSql = "SELECT maKH, tenKH, soDT, email, ngayDangKy, thanhVien, diaChi FROM KhachHang WHERE soDT = ?";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement psSelect = con.prepareStatement(selectSql)) {
            psSelect.setString(1, sdt);
            try (ResultSet rs = psSelect.executeQuery()) {
                if (rs.next()) {
                    // Tìm thấy -> Tạo đối tượng KhachHang từ dữ liệu DB
                    LocalDate ngayDangKyFromDB = rs.getDate("ngayDangKy") != null ? rs.getDate("ngayDangKy").toLocalDate() : null;
                    return new KhachHang(
                        rs.getString("maKH"), rs.getString("tenKH"), rs.getString("soDT"),
                        rs.getString("email"), ngayDangKyFromDB, rs.getString("diaChi"),
                        rs.getString("thanhVien")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tìm khách hàng theo SĐT: " + e.getMessage());
            throw e; // Ném lại lỗi để UI xử lý
        }

        // 2. Không tìm thấy -> Tạo khách hàng mới
        try (Connection con = ConnectDB.getConnection()) {
            String newMaKH = getNextMaKH();

            // === SỬA ĐỔI QUAN TRỌNG ===
            // Nếu tenKH là null hoặc rỗng, gán là null. Ngược lại, lấy giá trị đã trim().
            String tenKhachMoi = (tenKH == null || tenKH.trim().isEmpty()) ? null : tenKH.trim();
            // === KẾT THÚC SỬA ĐỔI ===

            LocalDate ngayHienTai = LocalDate.now();
            String insertSql = "INSERT INTO KhachHang (maKH, tenKH, soDT, ngayDangKy, thanhVien) VALUES (?, ?, ?, ?, ?)";

            try (PreparedStatement psInsert = con.prepareStatement(insertSql)) {
                psInsert.setString(1, newMaKH);

                // Chỗ này setNString với giá trị tenKhachMoi (có thể là null hoặc tên người dùng nhập)
                psInsert.setNString(2, tenKhachMoi); 

                psInsert.setString(3, sdt);
                psInsert.setDate(4, java.sql.Date.valueOf(ngayHienTai)); 
                psInsert.setNString(5, "Guest"); 
                psInsert.executeUpdate();

                System.out.println("LOG DAO: Đã tạo khách hàng mới: " + newMaKH + " - " + tenKhachMoi);
                // Trả về đối tượng KhachHang vừa tạo
                return new KhachHang(newMaKH, tenKhachMoi, sdt, null, ngayHienTai, null, "Guest");
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi tạo khách hàng mới: " + e.getMessage());
            throw e; // Ném lại lỗi
        }
    }
     /**
      * Lưu Hóa đơn và Chi tiết Hóa đơn (Đơn đặt hàng/đến quán)
      * === ĐÃ SỬA: Thêm cột maHDGoc ===
      */
     public void luuHoaDonVaChiTiet(HoaDon hoaDon, ObservableList<MonOrder> monOrderList) throws SQLException {
         String maHD = getNextMaHD();
         hoaDon.setMaHD(maHD); // Gán mã HD mới tạo vào đối tượng HoaDon luôn

         // Sửa SQL: Thêm maHDGoc
         String hdSql = "INSERT INTO HoaDon (maHD, ngayLap, trangThai, gioVao, maBan, maKH, tienCoc, maUuDai, ptThanhToan, maNV, maHDGoc) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
         
         try (Connection con = ConnectDB.getConnection();
              PreparedStatement ps = con.prepareStatement(hdSql)) {

             ps.setString(1, maHD);
             ps.setTimestamp(2, hoaDon.getNgayLap() != null ? Timestamp.valueOf(hoaDon.getNgayLap()) : null);
             ps.setString(3, (hoaDon.getTrangThai() != null) ? hoaDon.getTrangThai().getDbValue() : null);
             ps.setTimestamp(4, hoaDon.getGioVao() != null ? Timestamp.valueOf(hoaDon.getGioVao()) : null);
             ps.setString(5, (hoaDon.getBan() != null) ? hoaDon.getBan().getMaBan() : null);
             ps.setString(6, (hoaDon.getKhachHang() != null) ? hoaDon.getKhachHang().getMaKH() : null);
             ps.setDouble(7, hoaDon.getTienCoc());
             ps.setString(8, hoaDon.getMaUuDai());
             ps.setNull(9, Types.NVARCHAR); // ptThanhToan
             ps.setNull(10, Types.VARCHAR); // maNV
             
             // === THÊM MỚI LOGIC maHDGoc ===
             if (hoaDon.getMaHDGoc() != null) {
                 ps.setString(11, hoaDon.getMaHDGoc());
             } else {
                 ps.setNull(11, Types.VARCHAR); // HĐ Gốc sẽ là NULL
             }
             // =============================

             ps.executeUpdate();

             // Lưu Chi tiết Hóa đơn (Chỉ lưu nếu có món)
             if (monOrderList != null && !monOrderList.isEmpty()) {
                  String cthdSql = "INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, thanhTien) VALUES (?, ?, ?, ?)";
                  try (PreparedStatement psCt = con.prepareStatement(cthdSql)) {
                      for (MonOrder order : monOrderList) {
                          psCt.setString(1, maHD);
                          psCt.setString(2, order.getMaMon());
                          psCt.setInt(3, order.getSoLuong());
                          psCt.setDouble(4, order.getDonGia() * order.getSoLuong());
                          psCt.addBatch();
                      }
                      psCt.executeBatch();
                  }
             }
             System.out.println("LOG DAO: Đã lưu thành công hóa đơn " + maHD);
         } catch (SQLException e) {
             System.err.println("Lỗi khi lưu hóa đơn và chi tiết: SQL State: " + e.getSQLState() + ", Error Code: " + e.getErrorCode());
             e.printStackTrace();
             throw e;
         }
     }

    /**
     * Lấy chi tiết món ăn (MonOrder ViewModel) của một hóa đơn cụ thể.
     */
    public ObservableList<ui.DatBan.MonOrder> getChiTietHoaDon(String maHD) {
        // ... (Logic giữ nguyên)
        ObservableList<ui.DatBan.MonOrder> list = FXCollections.observableArrayList();
        if (maHD == null) return list;
        String sql = """
            SELECT cthd.maMon, cthd.soLuong, m.tenMon, m.giaBan
            FROM ChiTietHoaDon cthd
            JOIN MonAn m ON cthd.maMon = m.maMon
            WHERE cthd.maHD = ?
        """;
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maHD);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new ui.DatBan.MonOrder(
                        rs.getString("maMon"), rs.getString("tenMon"),
                        rs.getDouble("giaBan"), rs.getInt("soLuong")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("❌ Lỗi khi tải chi tiết hóa đơn " + maHD + ": " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Lấy TẤT CẢ các bàn và kiểm tra tính khả dụng tại một thời điểm cụ thể.
     * === ĐÃ SỬA LOGIC KIỂM TRA THỜI GIAN VÀ TRẢ VỀ TẤT CẢ BÀN ===
     * Kiểm tra xem thời điểm yêu cầu có giao với "khoảng thời gian bận ước tính"
     * của các hóa đơn khác không (ước tính 4 tiếng phục vụ).
     * @param gioDenKiemTra Thời điểm cần kiểm tra (dưới dạng Timestamp).
     * @return Danh sách Map chứa thông tin Ban và trạng thái isAvailable (true/false).
     */
    public List<Map<String, Object>> getAllBanWithAvailability(Timestamp gioDenKiemTra) {

        List<Map<String, Object>> resultList = new ArrayList<>();
        final int estimatedServiceHours = 4; // Thời gian phục vụ ước tính

        // SQL mới: Lấy TẤT CẢ bàn và dùng CASE để xác định isAvailable
        String sql = """
            SELECT
                b.*,
                CASE
                    WHEN EXISTS (
                        SELECT 1
                        FROM HoaDon h
                        WHERE h.maBan = b.maBan
                          AND h.trangThai IN (?, ?, ?) -- 'Dat', 'DangSuDung', 'HoaDonTam'
                          AND ? >= h.gioVao
                          AND ? < DATEADD(hour, ?, h.gioVao)
                    ) THEN CAST(0 AS BIT) -- Nếu tồn tại HĐ trùng giờ -> Bận (0)
                    ELSE CAST(1 AS BIT) -- Ngược lại -> Trống (1)
                END AS isAvailable
            FROM Ban b
            ORDER BY b.maBan -- Sắp xếp để hiển thị nhất quán
        """;
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // Tham số cho EXISTS
            ps.setString(1, TrangThaiHoaDon.DAT.getDbValue());
            ps.setString(2, TrangThaiHoaDon.DANG_SU_DUNG.getDbValue());
            ps.setString(3, TrangThaiHoaDon.HOA_DON_TAM.getDbValue()); // Thêm HoaDonTam
            ps.setTimestamp(4, gioDenKiemTra);
            ps.setTimestamp(5, gioDenKiemTra);
            ps.setInt(6, estimatedServiceHours);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                // Tạo đối tượng Ban
                Ban ban = new Ban(
                    rs.getString("maBan"),
                    rs.getString("viTri"),
                    rs.getInt("sucChua"),
                    LoaiBan.fromString(rs.getString("loaiBan")),
                    // Lấy trạng thái gốc từ DB, không cần thiết lắm ở đây
                    TrangThaiBan.fromDbValue(rs.getString("trangThai"))
                );
                // Lấy trạng thái khả dụng
                boolean isAvailable = rs.getBoolean("isAvailable");

                // Tạo Map để chứa cả Ban và isAvailable
                Map<String, Object> banInfo = new HashMap<>();
                banInfo.put("ban", ban);
                banInfo.put("isAvailable", isAvailable);
                resultList.add(banInfo);
            }

        } catch (SQLException e) {
             System.err.println("Lỗi khi lấy tất cả bàn và kiểm tra availability: " + e.getMessage());
            e.printStackTrace();
        }
        return resultList;
    }
    /**
     * Lấy tất cả các bàn trong nhà hàng.
     * @return Danh sách tất cả đối tượng Ban.
     */
    public List<Ban> getAllBan() {
        // ... (Logic giữ nguyên)
        List<Ban> list = new ArrayList<>();
        String sql = "SELECT * FROM Ban ORDER BY maBan"; // Sắp xếp để hiển thị nhất quán
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Ban ban = new Ban(
                    rs.getString("maBan"), rs.getString("viTri"), rs.getInt("sucChua"),
                    LoaiBan.fromString(rs.getString("loaiBan")),
                    TrangThaiBan.fromDbValue(rs.getString("trangThai"))
                );
                list.add(ban);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy tất cả bàn: " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Trả về danh sách Hóa đơn Đã Đặt/Đang Sử Dụng/Chờ Xác Nhận trong ngày HOẶC đang hoạt động từ ngày hôm trước.
     * 🔥 ĐÃ SỬA: Thêm trạng thái CHO_XAC_NHAN vào bộ lọc.
     */
    public List<HoaDon> getDsDatBanHomNay(LocalDate date) {
        List<HoaDon> list = new ArrayList<>();
        String sql = """
            SELECT
                hd.maHD, hd.ngayLap, hd.maUuDai, hd.ptThanhToan, hd.trangThai, hd.gioVao, hd.gioRa, hd.tienCoc,
                hd.maKH, kh.tenKH, kh.soDT, kh.email AS khEmail, kh.ngayDangKy AS khNgayDK, kh.thanhVien AS khThanhVien, kh.diaChi AS khDiaChi,
                hd.maBan, b.viTri AS banViTri, b.sucChua, b.loaiBan AS banLoaiBan, b.trangThai AS banTrangThai,
                hd.maNV, n.tenNV, hd.maHDGoc
            FROM HoaDon hd
            LEFT JOIN Ban b ON hd.maBan = b.maBan
            LEFT JOIN KhachHang kh ON hd.maKH = kh.maKH
            LEFT JOIN NhanVien n ON hd.maNV = n.maNV
            WHERE
                hd.trangThai IN (?, ?, ?, ?) -- 'Dat', 'DangSuDung', 'HoaDonTam', 'ChoXacNhan' 🔥 THÊM ?
                AND (
                    CAST(hd.gioVao AS DATE) = ?
                    -- Chỉ lấy HĐ 'DangSuDung'/'HoaDonTam'/'ChoXacNhan' từ ngày hôm trước
                    OR (hd.trangThai IN (?, ?, ?) AND CAST(hd.gioVao AS DATE) < ?) -- 🔥 THÊM ?
                )
             ORDER BY hd.gioVao ASC
        """;

        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // Tham số cho WHERE hd.trangThai IN (...)
            ps.setString(1, TrangThaiHoaDon.DAT.getDbValue());
            ps.setString(2, TrangThaiHoaDon.DANG_SU_DUNG.getDbValue());
            ps.setString(3, TrangThaiHoaDon.HOA_DON_TAM.getDbValue());
            ps.setString(4, TrangThaiHoaDon.CHO_XAC_NHAN.getDbValue()); // 🔥 Tham số mới

            // Tham số cho điều kiện ngày
            ps.setDate(5, java.sql.Date.valueOf(date)); // CAST(hd.gioVao AS DATE) = ?

            // Tham số cho OR (hd.trangThai IN (...) AND CAST(hd.gioVao AS DATE) < ?)
            ps.setString(6, TrangThaiHoaDon.DANG_SU_DUNG.getDbValue());
            ps.setString(7, TrangThaiHoaDon.HOA_DON_TAM.getDbValue());
            ps.setString(8, TrangThaiHoaDon.CHO_XAC_NHAN.getDbValue()); // 🔥 Tham số mới
            ps.setDate(9, java.sql.Date.valueOf(date)); // CAST(hd.gioVao AS DATE) < ?

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                 // ... (Code tạo đối tượng HoaDon giữ nguyên) ...
                HoaDon hoaDon = new HoaDon();
                // ... (set các thuộc tính) ...
                 hoaDon.setMaHD(rs.getString("maHD"));
                hoaDon.setMaUuDai(rs.getString("maUuDai"));
                hoaDon.setTienCoc(rs.getDouble("tienCoc"));
                hoaDon.setTenNhanVien(rs.getString("tenNV"));
                hoaDon.setHinhThucTT(PTTThanhToan.fromDbValue(rs.getString("ptThanhToan")));
                hoaDon.setTrangThai(rs.getString("trangThai")); // Setter nhận String
                Timestamp tsNgayLap = rs.getTimestamp("ngayLap"); hoaDon.setNgayLap( (tsNgayLap != null) ? tsNgayLap.toLocalDateTime() : null );
                Timestamp tsGioVao = rs.getTimestamp("gioVao"); hoaDon.setGioVao( (tsGioVao != null) ? tsGioVao.toLocalDateTime() : null );
                Timestamp tsGioRa = rs.getTimestamp("gioRa"); hoaDon.setGioRa( (tsGioRa != null) ? tsGioRa.toLocalDateTime() : null );
                hoaDon.setMaHDGoc(rs.getString("maHDGoc"));
                 // Khách hàng
                if (rs.getString("maKH") != null) { LocalDate ngayDK = rs.getDate("khNgayDK") != null ? rs.getDate("khNgayDK").toLocalDate() : null; KhachHang kh = new KhachHang( rs.getString("maKH"), rs.getString("tenKH"), rs.getString("soDT"), rs.getString("khEmail"), ngayDK, rs.getString("khDiaChi"), rs.getString("khThanhVien") ); hoaDon.setKhachHang(kh); } else { hoaDon.setKhachHang(null); }
                 // Bàn
                if (rs.getString("maBan") != null) { Ban ban = new Ban( rs.getString("maBan"), rs.getString("banViTri"), rs.getInt("sucChua"), LoaiBan.fromString(rs.getString("banLoaiBan")), TrangThaiBan.fromDbValue(rs.getString("banTrangThai")) ); hoaDon.setBan(ban); } else { hoaDon.setBan(null); }
                 hoaDon.setTongCongMonAn(0); // Tạm thời
                list.add(hoaDon);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách đặt bàn hôm nay: " + e.getMessage());
            e.printStackTrace();
            // return new ArrayList<>(); // Trả về list rỗng nếu lỗi
        }
        return list;
    }
    /**
   /**
     * 🔥 HÀM MỚI: Lấy TẤT CẢ các hóa đơn đang ở trạng thái chờ xử lý (Đang phục vụ, Đã đặt, Hóa đơn tạm, Chờ xác nhận)
     * Sắp xếp theo: Đang phục vụ/Hóa đơn tạm > Chờ xác nhận > Đã đặt, sau đó theo Giờ vào sớm nhất.
     * 🔥 ĐÃ SỬA: Thêm trạng thái CHO_XAC_NHAN vào bộ lọc và sắp xếp.
     */
    public List<HoaDon> getDsHoaDonDangCho() {
        List<HoaDon> list = new ArrayList<>();
        String sql = """
            SELECT
                hd.maHD, hd.ngayLap, hd.maUuDai, hd.ptThanhToan, hd.trangThai, hd.gioVao, hd.gioRa, hd.tienCoc,
                hd.maKH, kh.tenKH, kh.soDT, kh.email AS khEmail, kh.ngayDangKy AS khNgayDK, kh.thanhVien AS khThanhVien, kh.diaChi AS khDiaChi,
                hd.maBan, b.viTri AS banViTri, b.sucChua, b.loaiBan AS banLoaiBan, b.trangThai AS banTrangThai,
                hd.maNV, n.tenNV
            FROM HoaDon hd
            LEFT JOIN Ban b ON hd.maBan = b.maBan
            LEFT JOIN KhachHang kh ON hd.maKH = kh.maKH
            LEFT JOIN NhanVien n ON hd.maNV = n.maNV
            WHERE
                hd.trangThai IN (?, ?, ?, ?) -- 'Dat', 'DangSuDung', 'HoaDonTam', 'ChoXacNhan' 🔥 THÊM ?
            ORDER BY
                -- Ưu tiên 1: Trạng thái
                CASE hd.trangThai
                    WHEN ? THEN 1 -- 'DangSuDung'
                    WHEN ? THEN 1 -- 'HoaDonTam'
                    WHEN ? THEN 2 -- 'ChoXacNhan' 🔥 THÊM MỚI
                    WHEN ? THEN 3 -- 'Dat'       🔥 SỬA THỨ TỰ
                    ELSE 4
                END ASC,
                -- Ưu tiên 2: Giờ vào sớm nhất
                hd.gioVao ASC
        """;

        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // Tham số cho WHERE
            ps.setString(1, TrangThaiHoaDon.DAT.getDbValue());
            ps.setString(2, TrangThaiHoaDon.DANG_SU_DUNG.getDbValue());
            ps.setString(3, TrangThaiHoaDon.HOA_DON_TAM.getDbValue());
            ps.setString(4, TrangThaiHoaDon.CHO_XAC_NHAN.getDbValue()); // 🔥 Tham số mới

            // Tham số cho ORDER BY CASE
            ps.setString(5, TrangThaiHoaDon.DANG_SU_DUNG.getDbValue());
            ps.setString(6, TrangThaiHoaDon.HOA_DON_TAM.getDbValue());
            ps.setString(7, TrangThaiHoaDon.CHO_XAC_NHAN.getDbValue()); // 🔥 Tham số mới
            ps.setString(8, TrangThaiHoaDon.DAT.getDbValue());       // 🔥 Tham số mới

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                // ... (Code tạo đối tượng HoaDon giữ nguyên) ...
                 HoaDon hoaDon = new HoaDon();
                // ... (set các thuộc tính) ...
                 hoaDon.setMaHD(rs.getString("maHD"));
                 hoaDon.setMaUuDai(rs.getString("maUuDai"));
                 hoaDon.setTienCoc(rs.getDouble("tienCoc"));
                 hoaDon.setTenNhanVien(rs.getString("tenNV"));
                 hoaDon.setHinhThucTT(PTTThanhToan.fromDbValue(rs.getString("ptThanhToan")));
                 hoaDon.setTrangThai(rs.getString("trangThai")); // Setter nhận String
                 Timestamp tsNgayLap = rs.getTimestamp("ngayLap"); hoaDon.setNgayLap( (tsNgayLap != null) ? tsNgayLap.toLocalDateTime() : null );
                 Timestamp tsGioVao = rs.getTimestamp("gioVao"); hoaDon.setGioVao( (tsGioVao != null) ? tsGioVao.toLocalDateTime() : null );
                 Timestamp tsGioRa = rs.getTimestamp("gioRa"); hoaDon.setGioRa( (tsGioRa != null) ? tsGioRa.toLocalDateTime() : null );
                 // Khách hàng
                 if (rs.getString("maKH") != null) { LocalDate ngayDK = rs.getDate("khNgayDK") != null ? rs.getDate("khNgayDK").toLocalDate() : null; KhachHang kh = new KhachHang( rs.getString("maKH"), rs.getString("tenKH"), rs.getString("soDT"), rs.getString("khEmail"), ngayDK, rs.getString("khDiaChi"), rs.getString("khThanhVien") ); hoaDon.setKhachHang(kh); } else { hoaDon.setKhachHang(null); }
                 // Bàn
                 if (rs.getString("maBan") != null) { Ban ban = new Ban( rs.getString("maBan"), rs.getString("banViTri"), rs.getInt("sucChua"), LoaiBan.fromString(rs.getString("banLoaiBan")), TrangThaiBan.fromDbValue(rs.getString("banTrangThai")) ); hoaDon.setBan(ban); } else { hoaDon.setBan(null); }
                list.add(hoaDon);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy danh sách hóa đơn đang chờ: " + e.getMessage());
            e.printStackTrace();
            // return new ArrayList<>(); // Trả về list rỗng nếu lỗi
        }
        return list;
    }
 // [Thêm hàm mới này vào file DatBanDAO.java]

    /**
     * 🔥 DAO MỚI: Cập nhật trạng thái Hóa đơn từ "Chờ xác nhận" sang "Đã đặt".
     * Chỉ cập nhật nếu trạng thái hiện tại đúng là "Chờ xác nhận".
     * @param maHD Mã hóa đơn cần xác nhận.
     * @return true nếu cập nhật thành công (1 dòng bị ảnh hưởng), false nếu không.
     * @throws SQLException Nếu có lỗi CSDL.
     */
    public boolean xacNhanTienCoc(String maHD) throws SQLException {
        String sql = "UPDATE HoaDon SET trangThai = ? WHERE maHD = ? AND trangThai = ?";
        int rowsAffected = 0;

        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, TrangThaiHoaDon.DAT.getDbValue()); // Trạng thái mới: Đã đặt
            ps.setString(2, maHD);
            ps.setString(3, TrangThaiHoaDon.CHO_XAC_NHAN.getDbValue()); // Điều kiện: Phải đang chờ xác nhận

            rowsAffected = ps.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("LOG DAO: Đã xác nhận tiền cọc cho HD " + maHD + ", chuyển sang 'Đã đặt'.");
            } else {
                System.out.println("LOG DAO: Không thể xác nhận cọc cho HD " + maHD + " (Có thể không tìm thấy hoặc trạng thái không phải 'Chờ xác nhận').");
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi xác nhận tiền cọc cho HD " + maHD + ": " + e.getMessage());
            throw e; // Ném lỗi để Controller xử lý
        }
        return rowsAffected > 0;
    }
    /**
     * Cập nhật trạng thái của một bàn trong CSDL.
     * @param maBan Mã bàn cần cập nhật.
     * @param trangThaiDbValue Giá trị trạng thái mới (phải là dbValue của enum TrangThaiBan).
     */
    public void capNhatTrangThaiBan(String maBan, String trangThaiDbValue) {
        // ... (Logic giữ nguyên)
        // Kiểm tra đầu vào (tránh null và đảm bảo là dbValue hợp lệ)
        if (maBan == null || trangThaiDbValue == null || TrangThaiBan.fromDbValue(trangThaiDbValue) == null) {
            System.err.println("CẢNH BÁO DAO: Thông tin cập nhật trạng thái bàn không hợp lệ (maBan=" + maBan + ", trangThai=" + trangThaiDbValue + ")");
            return;
        }

        String sql = "UPDATE Ban SET trangThai = ? WHERE maBan = ?";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, trangThaiDbValue);
            ps.setString(2, maBan);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                 System.out.println("LOG DAO: Cập nhật bàn " + maBan + " thành trạng thái '" + trangThaiDbValue + "' thành công.");
            } else {
                 System.err.println("WARNING DAO: Không tìm thấy bàn '" + maBan + "' để cập nhật trạng thái.");
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật trạng thái bàn " + maBan + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Cập nhật thông tin hóa đơn khi thanh toán hoàn tất.
     * @param maHD Mã hóa đơn cần cập nhật.
     * @param ptThanhToan Phương thức thanh toán được sử dụng (Enum PTTThanhToan).
     */
    public void capNhatKhiThanhToan(String maHD, PTTThanhToan ptThanhToan) {
        // ... (Logic giữ nguyên)
        if (maHD == null) {
            System.err.println("ERROR DAO: Không thể cập nhật thanh toán với maHD null.");
            return;
        }
        String trangThaiDbValue = TrangThaiHoaDon.DA_THANH_TOAN.getDbValue();
        String ptThanhToanDbValue = (ptThanhToan != null) ? ptThanhToan.getDbValue() : null;

        // Cập nhật giờ ra là thời điểm hiện tại, trạng thái thành Đã Thanh Toán, và PTTT
        String sql = "UPDATE HoaDon SET gioRa = GETDATE(), trangThai = ?, ptThanhToan = ? WHERE maHD = ?";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, trangThaiDbValue);
            ps.setString(2, ptThanhToanDbValue); // Có thể là null nếu không chọn PTTT
            ps.setString(3, maHD);
            int updatedRows = ps.executeUpdate();
             if (updatedRows > 0) {
                 System.out.println("LOG DAO: Đã cập nhật thanh toán cho HD: " + maHD + " với PTTT: " + (ptThanhToanDbValue != null ? ptThanhToanDbValue : "NULL"));
             } else {
                 System.err.println("WARNING DAO: Không tìm thấy HD '" + maHD + "' để cập nhật thanh toán.");
             }
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật thanh toán cho HD " + maHD + ": " + e.getMessage());
            e.printStackTrace();
            // Cân nhắc ném lại lỗi
            // throw new RuntimeException("Lỗi cập nhật thanh toán", e);
        }
    }
    /**
     * Lấy một hóa đơn duy nhất theo mã.
     * @param maHD Mã hóa đơn.
     * @return Đối tượng HoaDon, hoặc null nếu không tìm thấy.
     */
    public HoaDon getHoaDonByMaHD(String maHD) {
        String sql = """
            SELECT
                hd.maHD, hd.ngayLap, hd.maUuDai, hd.ptThanhToan, hd.trangThai, hd.gioVao, hd.gioRa, hd.tienCoc,
                hd.maKH, kh.tenKH, kh.soDT, kh.email AS khEmail, kh.ngayDangKy AS khNgayDK, kh.thanhVien AS khThanhVien, kh.diaChi AS khDiaChi,
                hd.maBan, b.viTri AS banViTri, b.sucChua, b.loaiBan AS banLoaiBan, b.trangThai AS banTrangThai,
                hd.maNV, n.tenNV, hd.maHDGoc
            FROM HoaDon hd
            LEFT JOIN Ban b ON hd.maBan = b.maBan
            LEFT JOIN KhachHang kh ON hd.maKH = kh.maKH
            LEFT JOIN NhanVien n ON hd.maNV = n.maNV
            WHERE hd.maHD = ?
        """;

        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maHD);
            
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                HoaDon hoaDon = new HoaDon();

                // Thông tin Hóa đơn
                hoaDon.setMaHD(rs.getString("maHD"));
                hoaDon.setMaUuDai(rs.getString("maUuDai"));
                hoaDon.setTienCoc(rs.getDouble("tienCoc"));
                hoaDon.setTenNhanVien(rs.getString("tenNV"));
                // Giả định PTTThanhToan có fromDbValue
                hoaDon.setHinhThucTT(PTTThanhToan.fromDbValue(rs.getString("ptThanhToan"))); 
                hoaDon.setTrangThai(rs.getString("trangThai")); 
                hoaDon.setNgayLap( (rs.getTimestamp("ngayLap") != null) ? rs.getTimestamp("ngayLap").toLocalDateTime() : null );
                hoaDon.setGioVao( (rs.getTimestamp("gioVao") != null) ? rs.getTimestamp("gioVao").toLocalDateTime() : null );
                hoaDon.setGioRa( (rs.getTimestamp("gioRa") != null) ? rs.getTimestamp("gioRa").toLocalDateTime() : null );
                hoaDon.setMaHDGoc(rs.getString("maHDGoc")); // Thêm maHDGoc

                // Thông tin Khách hàng
                if (rs.getString("maKH") != null) {
                    LocalDate ngayDK = rs.getDate("khNgayDK") != null ? rs.getDate("khNgayDK").toLocalDate() : null;
                    KhachHang kh = new KhachHang(
                        rs.getString("maKH"), rs.getString("tenKH"), rs.getString("soDT"),
                        rs.getString("khEmail"), ngayDK, rs.getString("khDiaChi"),
                        rs.getString("khThanhVien")
                    );
                    hoaDon.setKhachHang(kh);
                }

                // Thông tin Bàn
                if (rs.getString("maBan") != null) {
                    Ban ban = new Ban(
                        rs.getString("maBan"), rs.getString("banViTri"), rs.getInt("sucChua"), // sucChua đã được sửa ở lần trước
                        LoaiBan.fromString(rs.getString("banLoaiBan")),
                        TrangThaiBan.fromDbValue(rs.getString("banTrangThai"))
                    );
                    hoaDon.setBan(ban);
                }

                return hoaDon;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy hóa đơn theo mã " + maHD + ": " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    /**
     * 🔥 DAO MỚI: Cập nhật thông tin chính của Hóa đơn (MaKH, TienCoc).
     */
    public void capNhatThongTinHoaDon(String maHD, String maKH, double tienCoc) throws SQLException {
        String sql = "UPDATE HoaDon SET maKH = ?, tienCoc = ? WHERE maHD = ?";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maKH);
            ps.setDouble(2, tienCoc);
            ps.setString(3, maHD);
            ps.executeUpdate();
            System.out.println("LOG DAO: Đã cập nhật thông tin cho HD: " + maHD);
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật thông tin Hóa đơn: " + e.getMessage());
            throw e;
        }
    }

    /**
     * 🔥 DAO MỚI: Cập nhật Chi tiết Hóa đơn (Xóa cũ, chèn mới).
     */
    public void capNhatChiTietHoaDon(String maHD, ObservableList<MonOrder> monOrderList) throws SQLException {
        // Dùng Transaction để đảm bảo an toàn
        Connection con = null;
        try {
            con = ConnectDB.getConnection();
            con.setAutoCommit(false); // Bắt đầu Transaction

            // 1. Xóa chi tiết cũ
            String deleteSql = "DELETE FROM ChiTietHoaDon WHERE maHD = ?";
            try (PreparedStatement psDelete = con.prepareStatement(deleteSql)) {
                psDelete.setString(1, maHD);
                psDelete.executeUpdate();
            }
            
            // 2. Thêm chi tiết mới
            if (monOrderList != null && !monOrderList.isEmpty()) {
                String insertSql = "INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, thanhTien) VALUES (?, ?, ?, ?)";
                try (PreparedStatement psInsert = con.prepareStatement(insertSql)) {
                    for (MonOrder order : monOrderList) {
                        psInsert.setString(1, maHD);
                        psInsert.setString(2, order.getMaMon());
                        psInsert.setInt(3, order.getSoLuong());
                        psInsert.setDouble(4, order.getDonGia() * order.getSoLuong());
                        psInsert.addBatch();
                    }
                    psInsert.executeBatch();
                }
            }
            
            con.commit(); // Hoàn tất Transaction
            System.out.println("LOG DAO: Đã cập nhật CTHD cho HD: " + maHD);
            
        } catch (SQLException e) {
            if (con != null) con.rollback(); // Hoàn tác nếu lỗi
            System.err.println("Lỗi khi cập nhật chi tiết Hóa đơn (Đã rollback): " + e.getMessage());
            throw e;
        } finally {
            if (con != null) {
                 con.setAutoCommit(true);
                 con.close();
            }
        }
    }

    /**
     * 🔥 DAO MỚI: Cập nhật mã bàn mới cho Hóa đơn.
     */
    public void capNhatBanChoHoaDon(String maHD, String maBanMoi) throws SQLException {
        String sql = "UPDATE HoaDon SET maBan = ? WHERE maHD = ?";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maBanMoi);
            ps.setString(2, maHD);
            ps.executeUpdate();
            System.out.println("LOG DAO: Đã đổi bàn cho HD " + maHD + " sang " + maBanMoi);
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật bàn cho Hóa đơn: " + e.getMessage());
            throw e;
        }
    }

    /**
     * 🔥 DAO MỚI: Cập nhật trạng thái Hóa đơn (Hủy/Thanh toán)
     * @param setGioRa true nếu muốn set gioRa = GETDATE() (khi Hủy hoặc Thanh toán)
     */
    public void capNhatTrangThaiHoaDon(String maHD, String trangThaiMoiDbValue, boolean setGioRa) throws SQLException {
        String sql;
        if (setGioRa) {
            sql = "UPDATE HoaDon SET trangThai = ?, gioRa = GETDATE() WHERE maHD = ?";
        } else {
            sql = "UPDATE HoaDon SET trangThai = ? WHERE maHD = ?";
        }
        
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, trangThaiMoiDbValue);
            ps.setString(2, maHD);
            ps.executeUpdate();
            System.out.println("LOG DAO: Đã cập nhật trạng thái HD " + maHD + " sang " + trangThaiMoiDbValue);
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật trạng thái Hóa đơn: " + e.getMessage());
            throw e;
        }
    }
    /**
     * 🔥 DAO MỚI: Cập nhật cả mã bàn và giờ vào mới cho Hóa đơn (dùng khi Đổi Bàn).
     */
    public void capNhatBanVaGioVaoChoHoaDon(String maHD, String maBanMoi, java.sql.Timestamp gioVaoMoi) throws SQLException {
        String sql = "UPDATE HoaDon SET maBan = ?, gioVao = ? WHERE maHD = ?";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maBanMoi);
            ps.setTimestamp(2, gioVaoMoi);
            ps.setString(3, maHD);
            ps.executeUpdate();
            System.out.println("LOG DAO: Đã đổi bàn VÀ giờ vào cho HD " + maHD + " sang " + maBanMoi);
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật bàn và giờ vào cho Hóa đơn: " + e.getMessage());
            throw e;
        }
    }
 // dao.DatBanDAO.java

    /**
     * 🔥 DAO MỚI: Lấy danh sách Hóa Đơn Phụ (HoaDonTam) 
     * dựa trên mã Hóa Đơn Gốc.
     * @param maHDGoc Mã của hóa đơn gốc.
     * @return Danh sách các HoaDon phụ.
     */
    public List<HoaDon> getHoaDonPhuByMaHDGoc(String maHDGoc) {
        List<HoaDon> list = new ArrayList<>();
        // Chỉ lấy các thông tin cơ bản (MaHD, MaBan)
        // === SỬA LỖI GỐC RỄ: CHỈ LỌC HĐ TẠM CHỨ KHÔNG PHẢI HĐ ĐÃ HỦY ===
        String sql = """
            SELECT hd.maHD, hd.maBan, b.viTri AS banViTri, b.sucChua AS banSucChua, b.loaiBan AS banLoaiBan, b.trangThai AS banTrangThai
            FROM HoaDon hd
            LEFT JOIN Ban b ON hd.maBan = b.maBan
            WHERE hd.maHDGoc = ? AND hd.trangThai = 'HoaDonTam'
        """; // <<< LỌC THEO TRẠNG THÁI 'HoaDonTam'

        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            ps.setString(1, maHDGoc);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                HoaDon hd = new HoaDon();
                hd.setMaHD(rs.getString("maHD"));
                hd.setMaHDGoc(maHDGoc); // Thêm maHDGoc
                hd.setTrangThai(TrangThaiHoaDon.HOA_DON_TAM.getDbValue()); // Set trạng thái
                
                // Tải thông tin Bàn
                if (rs.getString("maBan") != null) {
                    Ban ban = new Ban(
                        rs.getString("maBan"), rs.getString("banViTri"), rs.getInt("banSucChua"),
                        LoaiBan.fromString(rs.getString("banLoaiBan")),
                        TrangThaiBan.fromDbValue(rs.getString("banTrangThai"))
                    );
                    hd.setBan(ban);
                }
                list.add(hd);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi lấy hóa đơn phụ cho " + maHDGoc + ": " + e.getMessage());
            e.printStackTrace();
        }
        return list;
    }
    /**
     * 🔥 DAO HELPER: Kiểm tra một bàn cụ thể có trống vào thời điểm nhất định không.
     */
    public boolean isBanAvailableAtTime(String maBan, Timestamp gioDenKiemTra) {
        final int estimatedServiceHours = 4;
        String sql = """
            SELECT COUNT(*)
            FROM HoaDon h
            WHERE h.maBan = ?
              AND h.trangThai IN (?, ?, ?) -- 'Dat', 'DangSuDung', 'HoaDonTam'
              AND ? >= h.gioVao
              AND ? < DATEADD(hour, ?, h.gioVao)
        """;
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, maBan);
            ps.setString(2, TrangThaiHoaDon.DAT.getDbValue());
            ps.setString(3, TrangThaiHoaDon.DANG_SU_DUNG.getDbValue());
            ps.setString(4, TrangThaiHoaDon.HOA_DON_TAM.getDbValue()); // Thêm HoaDonTam
            ps.setTimestamp(5, gioDenKiemTra);
            ps.setTimestamp(6, gioDenKiemTra);
            ps.setInt(7, estimatedServiceHours);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) == 0; // Trả về true nếu count = 0 (không có HĐ nào trùng)
            }
        } catch (SQLException e) {
             System.err.println("Lỗi khi kiểm tra availability cho bàn " + maBan + ": " + e.getMessage());
             e.printStackTrace();
        }
        // Giữ nguyên logic cũ: Nếu có lỗi CSDL, coi như không trống
        return false; 
    }
    /**
     * 🔥 DAO MỚI: Cập nhật số lượng món trong Chi tiết Hóa đơn.
     * Dùng cho nghiệp vụ Tách Bàn (giảm số lượng).
     */
    private void capNhatSoLuongCTHD(Connection con, String maHD, String maMon, int soLuongMoi, double giaBan) throws SQLException {
        // KHÔNG CẦN ConnectDB.getConnection() NỮA
        String sql = "UPDATE ChiTietHoaDon SET soLuong = ?, thanhTien = ? * ? WHERE maHD = ? AND maMon = ?";
        
        // Chỉ dùng PreparedStatement và truyền Connection đã có
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, soLuongMoi);
            ps.setInt(2, soLuongMoi);
            ps.setDouble(3, giaBan);
            ps.setString(4, maHD);
            ps.setString(5, maMon);
            ps.executeUpdate();
            System.out.println("LOG DAO: Cập nhật CTHD (" + maHD + ", " + maMon + ") SL mới: " + soLuongMoi + " trong Transaction.");
        } catch (SQLException e) {
            // Bắt buộc throw để hàm gọi (Transaction) biết lỗi và Rollback
            System.err.println("Lỗi khi cập nhật số lượng CTHD (Helper): " + e.getMessage());
            throw e; 
        }
        // QUAN TRỌNG: Không được gọi con.close() ở đây!
    }

    /**
     * 🔥 DAO MỚI: Thêm chi tiết món ăn mới vào Hóa đơn.
     * Dùng cho nghiệp vụ Tách Bàn (thêm vào HĐ mới).
     */
    private void themChiTietHoaDon(Connection con, String maHD, String maMon, int soLuong, double giaBan) throws SQLException {
        // KHÔNG CẦN ConnectDB.getConnection() NỮA
        String cthdSql = "INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, thanhTien) VALUES (?, ?, ?, ?)";
        
        // Chỉ dùng PreparedStatement và truyền Connection đã có
        try (PreparedStatement psCt = con.prepareStatement(cthdSql)) {
            psCt.setString(1, maHD);
            psCt.setString(2, maMon);
            psCt.setInt(3, soLuong);
            psCt.setDouble(4, soLuong * giaBan);
            psCt.executeUpdate();
            System.out.println("LOG DAO: Đã thêm CTHD (Helper) (" + maHD + ", " + maMon + ") SL: " + soLuong + " trong Transaction.");
        } catch (SQLException e) {
            // Bắt buộc throw để hàm gọi (Transaction) biết lỗi và Rollback
            System.err.println("Lỗi khi thêm chi tiết Hóa đơn mới (Helper): " + e.getMessage());
            throw e; 
        }
        // QUAN TRỌNG: Không được gọi con.close() ở đây!
    }

    /**
     * 🔥 DAO MỚI: Xóa một món cụ thể khỏi Chi tiết Hóa đơn.
     * Dùng cho nghiệp vụ Tách Bàn (khi số lượng còn lại là 0).
     */
    private void xoaChiTietHoaDon(Connection con, String maHD, String maMon) throws SQLException {
        // KHÔNG CẦN ConnectDB.getConnection() NỮA
        String sql = "DELETE FROM ChiTietHoaDon WHERE maHD = ? AND maMon = ?";
        
        // Chỉ dùng PreparedStatement và truyền Connection đã có
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maHD);
            ps.setString(2, maMon);
            ps.executeUpdate();
            System.out.println("LOG DAO: Đã xóa món " + maMon + " khỏi HD " + maHD + " trong Transaction.");
        } catch (SQLException e) {
            // KHÔNG CẦN throw e VÌ TRANSACTION GỐC SẼ XỬ LÝ ROLLBACK
            System.err.println("Lỗi khi xóa chi tiết Hóa đơn (Helper): " + e.getMessage());
            throw e; // Bắt buộc throw để hàm gọi (Transaction) biết lỗi và Rollback
        }
        // QUAN TRỌNG: Không được gọi con.close() ở đây!
    }
    /**
     * 🔥 DAO MỚI: Cập nhật trạng thái của một Hóa Đơn.
     * Dùng để cập nhật trạng thái từ ComboBox trong ChiTietDatBanController.
     * @param maHD Mã hóa đơn cần cập nhật.
     * @param newTrangThai Giá trị trạng thái mới (ví dụ: "Dat", "DangSuDung", "DaThanhToan").
     * @return true nếu cập nhật thành công, false nếu thất bại.
     */
    public boolean updateTrangThaiHoaDon(String maHD, String newTrangThai) {
        String sql = "UPDATE HoaDon SET trangThai = ? WHERE maHD = ?";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, newTrangThai);
            ps.setString(2, maHD);

            int rowsAffected = ps.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("LOG DAO: Đã cập nhật trạng thái HĐ " + maHD + " thành " + newTrangThai);
                return true;
            } else {
                System.err.println("LỖI CSDL: Không tìm thấy Hóa Đơn có mã " + maHD + " để cập nhật trạng thái.");
                return false;
            }

        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật trạng thái Hóa đơn " + maHD + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    /**
     * 🔥 DAO MỚI: Cộng dồn chi tiết món ăn từ một HĐ nguồn vào một HĐ đích (Gộp món).
     * @param maHDDich Mã Hóa đơn Đích (Master)
     * @param maHDNguon Mã Hóa đơn Nguồn (Cần gộp)
     */
    public void congDonChiTietHoaDon(String maHDDich, String maHDNguon) throws SQLException {
        // SQL thực hiện: Cập nhật CTHD đích nếu món đã tồn tại, ngược lại, chèn món mới
        String sql = """
            -- TẠO BẢNG TẠM ĐỂ CHỨA CTHD CỦA HĐ NGUỒN
            SELECT maMon, soLuong, thanhTien INTO #TempCTHD FROM ChiTietHoaDon WHERE maHD = ?;

            -- 1. CẬP NHẬT CÁC MÓN ĐÃ TỒN TẠI (Cộng dồn số lượng và thành tiền)
            UPDATE ChiTietHoaDon
            SET soLuong = CTHD.soLuong + T.soLuong,
                thanhTien = CTHD.thanhTien + T.thanhTien
            FROM ChiTietHoaDon CTHD
            INNER JOIN #TempCTHD T ON CTHD.maMon = T.maMon
            WHERE CTHD.maHD = ?;

            -- 2. CHÈN CÁC MÓN CHƯA TỒN TẠI VÀO HĐ ĐÍCH
            INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, thanhTien)
            SELECT
                ?, T.maMon, T.soLuong, T.thanhTien
            FROM #TempCTHD T
            LEFT JOIN ChiTietHoaDon CTHD ON T.maMon = CTHD.maMon AND CTHD.maHD = ?
            WHERE CTHD.maMon IS NULL;
            
            -- DỌN DẸP BẢNG TẠM
            DROP TABLE #TempCTHD;
        """;

        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            // Tham số cho #TempCTHD
            ps.setString(1, maHDNguon);
            
            // Tham số cho UPDATE
            ps.setString(2, maHDDich);
            
            // Tham số cho INSERT (Lặp lại maHDDich 2 lần)
            ps.setString(3, maHDDich);
            ps.setString(4, maHDDich);

            ps.executeUpdate(); // Chạy toàn bộ khối lệnh
            System.out.println("LOG DAO: Đã gộp món ăn từ HD " + maHDNguon + " vào HD " + maHDDich);

        } catch (SQLException e) {
            System.err.println("Lỗi khi cộng dồn CTHD: " + e.getMessage());
            throw e;
        }
    }

    /**
     * 🔥 DAO MỚI: Xóa Hóa đơn và Chi tiết HĐ của HĐ nguồn sau khi gộp.
     * HÀM NÀY PHẢI ĐƯỢC CHẠY SAU CÙNG.
     */
    public void xoaHoaDonGop(String maHDNguon, String maBanNguon) throws SQLException {
        Connection con = null;
        try {
            con = ConnectDB.getConnection();
            con.setAutoCommit(false);

            // 1. Xóa CTHD Nguồn
            String deleteCTHD = "DELETE FROM ChiTietHoaDon WHERE maHD = ?";
            try (PreparedStatement ps = con.prepareStatement(deleteCTHD)) {
                ps.setString(1, maHDNguon);
                ps.executeUpdate();
            }

            // 2. Xóa HĐ Nguồn
            String deleteHD = "DELETE FROM HoaDon WHERE maHD = ?";
            try (PreparedStatement ps = con.prepareStatement(deleteHD)) {
                ps.setString(1, maHDNguon);
                ps.executeUpdate();
            }

            // 3. Giải phóng bàn
            String updateBan = "UPDATE Ban SET trangThai = ? WHERE maBan = ?";
            try (PreparedStatement ps = con.prepareStatement(updateBan)) {
                ps.setString(1, TrangThaiBan.TRONG.getDbValue());
                ps.setString(2, maBanNguon);
                ps.executeUpdate();
            }
            
            con.commit();
            System.out.println("LOG DAO: Đã hủy HĐ " + maHDNguon + " và giải phóng bàn " + maBanNguon);

        } catch (SQLException e) {
            if (con != null) con.rollback();
            System.err.println("LỖI GỘP BÀN: Rollback giao dịch xóa HĐ nguồn: " + e.getMessage());
            throw e;
        } finally {
            if (con != null) con.setAutoCommit(true);
        }
    }
 // dao.DatBanDAO.java (Chỉ phần hàm thanhToanHoaDon được sửa)

    /**
     * Cập nhật trạng thái Hóa đơn thành ĐÃ THANH TOÁN và lưu PTTT.
     * 🔥 ĐÃ SỬA: Thêm logic XÓA HÓA ĐƠN PHỤ VÀ GIẢI PHÓNG BÀN liên quan.
     */
    public boolean thanhToanHoaDon(String maHD, PTTThanhToan pttt, String maNhanVien) { 
        Connection con = null;
        try {
            con = ConnectDB.getConnection();
            con.setAutoCommit(false); // Bắt đầu giao dịch

            // Lấy mã bàn của HĐ GỐC (cần để cập nhật trạng thái bàn)
            String maBanGoc = null;
            // Lấy danh sách các HĐ phụ liên quan (để xóa)
            List<String> listMaHDPhu = new ArrayList<>();
            listMaHDPhu.add(maHD); // Thêm HĐ gốc vào danh sách kiểm tra
            
            // Tìm các HĐ Phụ (HoaDonTam) liên quan đến HĐ Gốc này
            String sqlFindPhu = "SELECT maHD, maBan FROM HoaDon WHERE maHDGoc = ? AND trangThai = ?";
            try(PreparedStatement psFindPhu = con.prepareStatement(sqlFindPhu)) {
                psFindPhu.setString(1, maHD);
                psFindPhu.setString(2, TrangThaiHoaDon.HOA_DON_TAM.getDbValue());
                ResultSet rs = psFindPhu.executeQuery();
                while(rs.next()) {
                    listMaHDPhu.add(rs.getString("maHD"));
                }
            }
            
            // Lấy maBanGoc (Nếu HĐ Gốc có bàn)
            String sqlFindBanGoc = "SELECT maBan FROM HoaDon WHERE maHD = ?";
            try(PreparedStatement psFindBanGoc = con.prepareStatement(sqlFindBanGoc)) {
                psFindBanGoc.setString(1, maHD);
                ResultSet rs = psFindBanGoc.executeQuery();
                if(rs.next()) {
                    maBanGoc = rs.getString("maBan");
                }
            }
            

            // === 1. XÓA CÁC HÓA ĐƠN PHỤ VÀ CHI TIẾT LIÊN QUAN (HoaDonTam) ===
            // 1.1 Xóa Chi tiết Hóa đơn Phụ
            String sqlDeleteCTHDPhu = "DELETE FROM ChiTietHoaDon WHERE maHD IN (SELECT maHD FROM HoaDon WHERE maHDGoc = ? AND trangThai = ?)";
            try (PreparedStatement psDeleteCTHDPhu = con.prepareStatement(sqlDeleteCTHDPhu)) {
                psDeleteCTHDPhu.setString(1, maHD);
                psDeleteCTHDPhu.setString(2, TrangThaiHoaDon.HOA_DON_TAM.getDbValue());
                psDeleteCTHDPhu.executeUpdate();
            }

            // 1.2 Xóa Hóa đơn Phụ
            String sqlDeleteHDPhu = "DELETE FROM HoaDon WHERE maHDGoc = ? AND trangThai = ?";
            try (PreparedStatement psDeleteHDPhu = con.prepareStatement(sqlDeleteHDPhu)) {
                psDeleteHDPhu.setString(1, maHD);
                psDeleteHDPhu.setString(2, TrangThaiHoaDon.HOA_DON_TAM.getDbValue());
                psDeleteHDPhu.executeUpdate();
            }
            System.out.println("LOG DAO: Đã xóa Hóa đơn Phụ (HoaDonTam) liên quan đến HD Gốc: " + maHD);
            
            // === 2. CẬP NHẬT HÓA ĐƠN GỐC THÀNH ĐÃ THANH TOÁN ===
            String sqlHD = "UPDATE HoaDon SET trangThai = ?, ptThanhToan = ?, gioRa = GETDATE(), maNV = ? WHERE maHD = ?";
            try (PreparedStatement psHD = con.prepareStatement(sqlHD)) {
                psHD.setString(1, TrangThaiHoaDon.DA_THANH_TOAN.getDbValue());
                psHD.setString(2, pttt.getDbValue()); 
                psHD.setString(3, maNhanVien); 
                psHD.setString(4, maHD);
                
                if (psHD.executeUpdate() == 0) {
                    con.rollback();
                    return false;
                }
            }
            
            // === 3. GIẢI PHÓNG TẤT CẢ CÁC BÀN LIÊN QUAN ===
            // Lấy TẤT CẢ các mã bàn đã bị khóa bởi HĐ Gốc (cả HĐ Gốc và HĐ Phụ)
            String sqlBanKeys = "SELECT maBan FROM HoaDon WHERE maHDGoc = ? OR maHD = ?"; 
            Set<String> maBanSet = new HashSet<>();
            try(PreparedStatement psBanKeys = con.prepareStatement(sqlBanKeys)) {
                psBanKeys.setString(1, maHD);
                psBanKeys.setString(2, maHD);
                ResultSet rs = psBanKeys.executeQuery();
                while(rs.next()) {
                    maBanSet.add(rs.getString("maBan"));
                }
            }
            
            // Giải phóng từng bàn
            if (!maBanSet.isEmpty()) {
                String updateBanSql = "UPDATE Ban SET trangThai = ? WHERE maBan = ?";
                try (PreparedStatement psBan = con.prepareStatement(updateBanSql)) {
                    for (String maBan : maBanSet) {
                         if(maBan != null) {
                             psBan.setString(1, TrangThaiBan.TRONG.getDbValue());
                             psBan.setString(2, maBan);
                             psBan.addBatch();
                         }
                    }
                    psBan.executeBatch();
                }
            }

            con.commit(); // Hoàn tất giao dịch
            System.out.println("LOG DAO: Đã thanh toán HĐ " + maHD + ", xóa HĐ Phụ và giải phóng các bàn liên quan.");
            return true;

        } catch (SQLException e) {
            if (con != null) {
                try {
                    con.rollback();
                } catch (SQLException ex) {
                    System.err.println("LỖI ROLLBACK: " + ex.getMessage());
                }
            }
            System.err.println("LỖI THANH TOÁN HĐ " + maHD + ": " + e.getMessage());
            return false;
        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (SQLException ex) {
                    System.err.println("LỖI ĐÓNG KẾT NỐI: " + ex.getMessage());
                }
            }
        }
    }
    /**
     * 🔥 DAO MỚI: XÓA NHIỀU HÓA ĐƠN VÀ CHI TIẾT CỦA CHÚNG (Dùng cho HĐ Phụ)
     * @param maHDs Danh sách mã Hóa đơn cần xóa.
     */
    public void xoaHoaDonVaChiTiet(List<String> maHDs) throws SQLException {
        if (maHDs == null || maHDs.isEmpty()) return;

        // Dùng StringBuilder để xây dựng chuỗi IN (?, ?, ?)
        String placeholders = String.join(",", Collections.nCopies(maHDs.size(), "?"));

        String deleteCTHDSql = "DELETE FROM ChiTietHoaDon WHERE maHD IN (" + placeholders + ")";
        String deleteHDSql = "DELETE FROM HoaDon WHERE maHD IN (" + placeholders + ")";

        Connection con = null;
        try {
            con = ConnectDB.getConnection();
            con.setAutoCommit(false);

            // 1. Xóa Chi tiết Hóa đơn
            try (PreparedStatement ps = con.prepareStatement(deleteCTHDSql)) {
                for (int i = 0; i < maHDs.size(); i++) {
                    ps.setString(i + 1, maHDs.get(i));
                }
                ps.executeUpdate();
            }

            // 2. Xóa Hóa đơn
            try (PreparedStatement ps = con.prepareStatement(deleteHDSql)) {
                for (int i = 0; i < maHDs.size(); i++) {
                    ps.setString(i + 1, maHDs.get(i));
                }
                ps.executeUpdate();
            }

            con.commit();
            System.out.println("LOG DAO: Đã xóa thành công " + maHDs.size() + " Hóa đơn và Chi tiết liên quan.");
        } catch (SQLException e) {
            if (con != null) con.rollback();
            System.err.println("LỖI DAO: Rollback giao dịch xóa nhiều HĐ: " + e.getMessage());
            throw e;
        } finally {
            if (con != null) {
                con.setAutoCommit(true);
                con.close();
            }
        }
    }
 // dao.DatBanDAO.java (Hàm mới cần thêm vào)

    /**
     * 🔥 DAO MỚI: Cập nhật trạng thái của NHIỀU bàn trong CSDL (Dùng cho bulk update).
     * Hàm này khắc phục lỗi không áp dụng được Set<String> cho hàm cũ.
     * @param maBanSet Set chứa Mã bàn cần cập nhật.
     * @param trangThaiDbValue Giá trị trạng thái mới.
     */
    public void capNhatTrangThaiNhieuBan(Set<String> maBanSet, String trangThaiDbValue) throws SQLException {
        if (maBanSet == null || maBanSet.isEmpty() || trangThaiDbValue == null) {
            System.err.println("CẢNH BÁO DAO: Thông tin cập nhật trạng thái nhiều bàn không hợp lệ.");
            return;
        }

        String sql = "UPDATE Ban SET trangThai = ? WHERE maBan = ?";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            
            for (String maBan : maBanSet) {
                 ps.setString(1, trangThaiDbValue);
                 ps.setString(2, maBan);
                 ps.addBatch();
            }
            ps.executeBatch(); // Thực thi batch update
            
            System.out.println("LOG DAO: Đã cập nhật trạng thái cho " + maBanSet.size() + " bàn thành trạng thái '" + trangThaiDbValue + "'.");
        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật trạng thái nhiều bàn: " + e.getMessage());
            throw e;
        }
    }
    /**
     * 🔥 DAO MỚI (TRANSACTION): Thực hiện toàn bộ nghiệp vụ TÁCH MÓN và THANH TOÁN.
     * BẢO ĐẢM TÍNH TOÀN VẸN CSDL (All or Nothing).
     */
    
 // =================================================================
    // CÁC HÀM HELPER NỘI BỘ (NHẬN CONNECTION) CHO TRANSACTION
    // =================================================================

    private void luuHoaDon(Connection con, HoaDon hoaDon) throws SQLException {
        String hdSql = "INSERT INTO HoaDon (maHD, ngayLap, trangThai, gioVao, maBan, maKH, tienCoc, maUuDai, ptThanhToan, maNV, maHDGoc) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
         
         try (PreparedStatement ps = con.prepareStatement(hdSql)) {
             ps.setString(1, hoaDon.getMaHD());
             ps.setTimestamp(2, hoaDon.getNgayLap() != null ? Timestamp.valueOf(hoaDon.getNgayLap()) : null);
             ps.setString(3, (hoaDon.getTrangThai() != null) ? hoaDon.getTrangThai().getDbValue() : null);
             ps.setTimestamp(4, hoaDon.getGioVao() != null ? Timestamp.valueOf(hoaDon.getGioVao()) : null);
             ps.setString(5, (hoaDon.getBan() != null) ? hoaDon.getBan().getMaBan() : null);
             ps.setString(6, (hoaDon.getKhachHang() != null) ? hoaDon.getKhachHang().getMaKH() : null);
             ps.setDouble(7, hoaDon.getTienCoc());
             ps.setString(8, hoaDon.getMaUuDai());
             ps.setNull(9, Types.NVARCHAR);
             ps.setNull(10, Types.VARCHAR);
             if (hoaDon.getMaHDGoc() != null) { ps.setString(11, hoaDon.getMaHDGoc()); } else { ps.setNull(11, Types.VARCHAR); }
             ps.executeUpdate();
             System.out.println("LOG DAO: Đã lưu HĐ (Helper) " + hoaDon.getMaHD());
         }
    }
    
    private void capNhatTrangThaiHoaDon(Connection con, String maHD, String trangThaiMoiDbValue, boolean setGioRa) throws SQLException {
        String sql;
        if (setGioRa) {
            sql = "UPDATE HoaDon SET trangThai = ?, gioRa = GETDATE() WHERE maHD = ?";
        } else {
            sql = "UPDATE HoaDon SET trangThai = ? WHERE maHD = ?";
        }
        
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, trangThaiMoiDbValue);
            ps.setString(2, maHD);
            ps.executeUpdate();
            System.out.println("LOG DAO: Đã cập nhật trạng thái HD (Helper) " + maHD + " sang " + trangThaiMoiDbValue);
        }
    }

    private void thanhToanHoaDon(Connection con, String maHD, PTTThanhToan pttt, String maNhanVien) throws SQLException {
        String sqlHD = "UPDATE HoaDon SET trangThai = ?, ptThanhToan = ?, gioRa = GETDATE(), maNV = ? WHERE maHD = ?";
        try (PreparedStatement psHD = con.prepareStatement(sqlHD)) {
            psHD.setString(1, TrangThaiHoaDon.DA_THANH_TOAN.getDbValue());
            psHD.setString(2, pttt.getDbValue()); 
            psHD.setString(3, maNhanVien); 
            psHD.setString(4, maHD);
            
            if (psHD.executeUpdate() == 0) {
                 throw new SQLException("Không tìm thấy Hóa đơn để thanh toán.");
            }
            System.out.println("LOG DAO: Đã đánh dấu HĐ " + maHD + " là ĐÃ THANH TOÁN (Helper).");
        }
    }

 // dao.DatBanDAO.java (Thêm vào class DatBanDAO)

    /**
     * 🔥 DAO MỚI (TRANSACTION): Thực hiện toàn bộ nghiệp vụ TÁCH MÓN và THANH TOÁN.
     * BẢO ĐẢM TÍNH TOÀN VẸN CSDL (All or Nothing).
     *
     * === ĐÃ SỬA: Thêm tham số 'maUuDai' ===
     */
    public boolean thucHienTachBanVaThanhToan(
            String maHDGoc, 
            ObservableList<TachBanPopupController.MonTach> monTachListSnapshot,
            String maHDToPay, // Mã HĐ sẽ thanh toán (HĐ mới hoặc HĐ gốc)
            PTTThanhToan pttt, 
            String maNhanVien,
            String maUuDai // 🔥 THAM SỐ MỚI
    ) 
    {
        Connection con = null;
        try {
            con = ConnectDB.getConnection();
            con.setAutoCommit(false); // Bắt đầu Transaction

            // --- 1. TẠO VÀ LƯU HÓA ĐƠN MỚI (nếu cần thanh toán HĐ mới) ---
            HoaDon hdMoiDuocTao = null;
            
            // Nếu HĐ cần thanh toán là HĐ MỚI (có mã tạm thời như "TMP...")
            boolean isPayingNewInvoice = maHDToPay.startsWith("TMP"); 
            
            if (isPayingNewInvoice) {
                HoaDon hdGocSnapshot = getHoaDonByMaHD(maHDGoc);
                
                hdMoiDuocTao = new HoaDon();
                hdMoiDuocTao.setNgayLap(LocalDateTime.now());
                hdMoiDuocTao.setGioVao(hdGocSnapshot.getGioVao());
                hdMoiDuocTao.setKhachHang(hdGocSnapshot.getKhachHang());
                hdMoiDuocTao.setBan(null); 
                hdMoiDuocTao.setTienCoc(0); 
                hdMoiDuocTao.setMaHDGoc(maHDGoc);
                hdMoiDuocTao.setTrangThai(TrangThaiHoaDon.HOA_DON_TAM.getDbValue());
                
                // 🔥 GÁN MÃ ƯU ĐÃI CHO HĐ MỚI TRƯỚC KHI LƯU
                hdMoiDuocTao.setMaUuDai(maUuDai); 
                
                // LƯU VÀ GÁN MÃ HD THẬT
                String maHDMoi = getNextMaHD();
                hdMoiDuocTao.setMaHD(maHDMoi);
                luuHoaDon(con, hdMoiDuocTao); // Helper (hàm luuHoaDon phải hỗ trợ lưu maUuDai)
                maHDToPay = maHDMoi; // Cập nhật mã HD cần thanh toán là mã THẬT
            
            } else {
                // 🔥 NẾU THANH TOÁN HĐ GỐC: Cập nhật ưu đãi cho HĐ Gốc
                // (Vì maHDToPay chính là maHDGoc)
                capNhatUuDaiHoaDon(con, maHDGoc, maUuDai); // Gọi helper mới
            }


            // --- 2. CẬP NHẬT CHI TIẾT MÓN ĂN (TÁCH THỰC SỰ) ---
            for (TachBanPopupController.MonTach mon : monTachListSnapshot) {
                // Chỉ xử lý những món có tách (dù là tách 0 món cũng chạy)
                // if (mon.getSoLuongTach() > 0) { // Bỏ điều kiện này để xử lý cả TH thanh toán HĐ gốc
                    
                    double donGia = mon.getDonGia();
                    int slConLai = mon.getSoLuongConLai();
                    int slTach = mon.getSoLuongTach();

                    // Cập nhật CTHD Gốc (Giảm số lượng món)
                    if (slConLai == 0) {
                        xoaChiTietHoaDon(con, maHDGoc, mon.getMaMon()); // Helper
                    } else {
                        // Chỉ cập nhật nếu số lượng gốc khác số lượng còn lại
                        if (slConLai != mon.getSoLuongGoc()) {
                           capNhatSoLuongCTHD(con, maHDGoc, mon.getMaMon(), slConLai, donGia); // Helper
                        }
                    }
                    
                    // Thêm CTHD Mới (Chèn số lượng món đã tách vào HĐ Mới)
                    // Chỉ thêm nếu đang thanh toán HĐ mới VÀ có món được tách
                    if (isPayingNewInvoice && slTach > 0) {
                        themChiTietHoaDon(con, maHDToPay, mon.getMaMon(), slTach, donGia); // Helper
                    }
                // } // Bỏ điều kiện
            }
            
            // --- 3. CẬP NHẬT TRẠNG THÁI HĐ GỐC (Nếu không còn món) ---
            boolean conMonConLai = monTachListSnapshot.stream().anyMatch(m -> m.getSoLuongConLai() > 0);
            if (!conMonConLai) {
                // Nếu HĐ gốc hết món, chuyển thành HĐ Tạm (trừ khi nó đang được thanh toán)
                if (!maHDToPay.equals(maHDGoc)) {
                    capNhatTrangThaiHoaDon(con, maHDGoc, TrangThaiHoaDon.HOA_DON_TAM.getDbValue(), false); // Helper
                }
            }

            // --- 4. THANH TOÁN HÓA ĐƠN THỰC SỰ (HĐ Mới hoặc HĐ Gốc) ---
            thanhToanHoaDon(con, maHDToPay, pttt, maNhanVien); // Helper

            con.commit(); // Hoàn tất Transaction
            return true;

        } catch (SQLException e) {
            if (con != null) {
                try {
                    con.rollback();
                    System.err.println("LỖI TÁCH/THANH TOÁN: Rollback giao dịch. " + e.getMessage());
                } catch (SQLException ex) {
                    System.err.println("LỖI ROLLBACK: " + ex.getMessage());
                }
            }
            System.err.println("LỖI THỰC HIỆN TRANSACTION: " + e.getMessage());
            e.printStackTrace(); // In chi tiết lỗi
            return false;
        } finally {
            if (con != null) {
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    /**
     * 🔥 HÀM HELPER MỚI (NỘI BỘ): Cập nhật mã ưu đãi cho Hóa đơn.
     * Dùng bên trong Transaction.
     */
    private void capNhatUuDaiHoaDon(Connection con, String maHD, String maUuDai) throws SQLException {
        String sql = "UPDATE HoaDon SET maUuDai = ? WHERE maHD = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            if (maUuDai != null && !maUuDai.isEmpty()) {
                ps.setString(1, maUuDai);
            } else {
                // Nếu không chọn KM, set giá trị trong CSDL là NULL
                ps.setNull(1, java.sql.Types.VARCHAR);
            }
            ps.setString(2, maHD);
            ps.executeUpdate();
            System.out.println("LOG DAO (Helper): Đã cập nhật mã ưu đãi " + maUuDai + " cho HD " + maHD);
        } catch (SQLException e) {
            System.err.println("Lỗi helper capNhatUuDaiHoaDon: " + e.getMessage());
            throw e; // Ném lỗi để transaction rollback
        }
    }
    /**
     * CHỈ TÌM KIẾM khách hàng theo SĐT. Không tạo mới.
     * @param sdt Số điện thoại cần tìm.
     * @return Đối tượng KhachHang nếu tìm thấy, ngược lại trả về null.
     */
     public KhachHang timKhachHangBySDT(String sdt) {
        String selectSql = "SELECT maKH, tenKH, soDT, email, ngayDangKy, thanhVien, diaChi FROM KhachHang WHERE soDT = ?";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement psSelect = con.prepareStatement(selectSql)) {
            psSelect.setString(1, sdt);
            try (ResultSet rs = psSelect.executeQuery()) {
                if (rs.next()) {
                    // Tìm thấy -> Tạo đối tượng KhachHang từ dữ liệu DB
                    LocalDate ngayDangKyFromDB = rs.getDate("ngayDangKy") != null ? rs.getDate("ngayDangKy").toLocalDate() : null;
                    return new KhachHang(
                        rs.getString("maKH"), rs.getString("tenKH"), rs.getString("soDT"),
                        rs.getString("email"), ngayDangKyFromDB, rs.getString("diaChi"),
                        rs.getString("thanhVien")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi khi TÌM khách hàng theo SĐT: " + e.getMessage());
            // Không ném lỗi, chỉ trả về null
        }
        // Không tìm thấy
        return null;
    }
  // Trong DatBanDAO.java

     /**
      * 🔥 HÀM MỚI: Lấy danh sách các mã bàn (duy nhất) đang có hóa đơn
      * ở trạng thái "Đang phục vụ".
      * @return List chứa các mã bàn đang phục vụ.
      */
     public List<String> getMaBanDangPhucVu() {
         List<String> maBanList = new ArrayList<>();
         // Lấy DISTINCT maBan từ HoaDon có trạng thái DangSuDung và maBan không null
         String sql = "SELECT DISTINCT maBan FROM HoaDon WHERE trangThai = ? AND maBan IS NOT NULL";

         try (Connection con = ConnectDB.getConnection();
              PreparedStatement ps = con.prepareStatement(sql)) {

             ps.setString(1, TrangThaiHoaDon.DANG_SU_DUNG.getDbValue()); // Dùng Enum để lấy giá trị DB

             try (ResultSet rs = ps.executeQuery()) {
                 while (rs.next()) {
                     maBanList.add(rs.getString("maBan"));
                 }
             }
         } catch (SQLException e) {
             System.err.println("Lỗi khi lấy danh sách mã bàn đang phục vụ: " + e.getMessage());
             e.printStackTrace();
         }
         return maBanList;
     }
  // Trong DatBanDAO.java

     /**
      * 🔥 HÀM MỚI: Đếm số lượng bàn duy nhất đang có hóa đơn
      * ở trạng thái "Đã đặt".
      * @return Số lượng bàn đã đặt trước.
      */
     public int demSoBanDatTruoc() {
         // Đếm DISTINCT maBan từ HoaDon có trạng thái Dat và maBan không null
         String sql = "SELECT COUNT(DISTINCT maBan) FROM HoaDon WHERE trangThai = ? AND maBan IS NOT NULL";
         int count = 0;

         try (Connection con = ConnectDB.getConnection();
              PreparedStatement ps = con.prepareStatement(sql)) {

             ps.setString(1, TrangThaiHoaDon.DAT.getDbValue()); // Dùng Enum để lấy giá trị DB

             try (ResultSet rs = ps.executeQuery()) {
                 if (rs.next()) {
                     count = rs.getInt(1); // Lấy kết quả COUNT
                 }
             }
         } catch (SQLException e) {
             System.err.println("Lỗi khi đếm số bàn đặt trước: " + e.getMessage());
             e.printStackTrace();
         }
         return count;
     }

     /**
      * 🔥 HÀM MỚI: Đếm tổng số bàn có trong nhà hàng.
      * @return Tổng số bàn.
      */
     public int demTongSoBan() {
         String sql = "SELECT COUNT(*) FROM Ban";
         int count = 0;

         try (Connection con = ConnectDB.getConnection();
              PreparedStatement ps = con.prepareStatement(sql);
              ResultSet rs = ps.executeQuery()) {

             if (rs.next()) {
                 count = rs.getInt(1); // Lấy kết quả COUNT
             }
         } catch (SQLException e) {
             System.err.println("Lỗi khi đếm tổng số bàn: " + e.getMessage());
             e.printStackTrace();
         }
         // Trả về 0 nếu có lỗi hoặc không có bàn nào
         return count;
     }
  // Trong DatBanDAO.java

    

     /**
      * 🔥 HÀM MỚI: Lấy danh sách các Hóa đơn (chỉ thông tin cơ bản)
      * đang ở trạng thái "Đang phục vụ" và có gán bàn.
      * @return List các đối tượng HoaDon (chỉ chứa maHD, maBan, trangThai).
      */
     public List<HoaDon> getHoaDonDangPhucVu() {
         List<HoaDon> hoaDonList = new ArrayList<>();
         // Lấy maHD và maBan từ HoaDon có trạng thái DangSuDung và maBan không null
         String sql = "SELECT maHD, maBan FROM HoaDon WHERE trangThai = ? AND maBan IS NOT NULL";

         try (Connection con = ConnectDB.getConnection();
              PreparedStatement ps = con.prepareStatement(sql)) {

             ps.setString(1, TrangThaiHoaDon.DANG_SU_DUNG.getDbValue());

             try (ResultSet rs = ps.executeQuery()) {
                 while (rs.next()) {
                     HoaDon hd = new HoaDon();
                     hd.setMaHD(rs.getString("maHD"));
                     hd.setTrangThai(TrangThaiHoaDon.DANG_SU_DUNG); // Gán trạng thái

                     // Tạo đối tượng Ban tạm thời chỉ với mã bàn
                     Ban ban = new Ban();
                     ban.setMaBan(rs.getString("maBan"));
                     hd.setBan(ban);

                     hoaDonList.add(hd);
                 }
             }
         } catch (SQLException e) {
             System.err.println("Lỗi khi lấy danh sách hóa đơn đang phục vụ: " + e.getMessage());
             e.printStackTrace();
         }
         return hoaDonList;
     }
     public List<Ban> getListBan() {
         return getAllBan();
     }
     /**
      * 🔥 HÀM ĐÃ SỬA LỖI SQL: Lấy danh sách hóa đơn đang ở trạng thái "Đặt trước" (Dat).
      */
     public List<HoaDon> getHoaDonDaDat() {
         List<HoaDon> list = new ArrayList<>();
         // SỬA: Đã đổi 'tenNV' thành 'maNV' để tránh lỗi Invalid column name
         String sql = "SELECT maHD, maBan, gioVao, maNV, maKH FROM HoaDon WHERE trangThai = ? AND maBan IS NOT NULL ORDER BY gioVao";

         try (Connection con = ConnectDB.getConnection();
              PreparedStatement ps = con.prepareStatement(sql)) {

             ps.setString(1, TrangThaiHoaDon.DAT.getDbValue());

             try (ResultSet rs = ps.executeQuery()) {
                 while (rs.next()) {
                     HoaDon hd = new HoaDon();
                     hd.setMaHD(rs.getString("maHD"));
                     hd.setTrangThai(TrangThaiHoaDon.DAT);
                     
                     // Lấy giờ đặt
                     Timestamp ts = rs.getTimestamp("gioVao");
                     if (ts != null) hd.setGioVao(ts.toLocalDateTime());

                     // Tạo đối tượng Ban tạm
                     Ban ban = new Ban();
                     ban.setMaBan(rs.getString("maBan"));
                     hd.setBan(ban);
                     
                     // (Tùy chọn) Lấy mã NV nếu cần
                     if (rs.getString("maNV") != null) {
                         hd.setTenNhanVien(rs.getString("maNV")); // Tạm gán mã vào tên, hoặc bỏ qua
                     }

                     list.add(hd);
                 }
             }
         } catch (SQLException e) {
             e.printStackTrace();
         }
         return list;
     }
}
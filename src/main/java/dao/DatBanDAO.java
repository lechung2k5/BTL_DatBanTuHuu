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

public class DatBanDAO {

	/**
	 * Lấy mã hóa đơn tiếp theo (ví dụ: HD011 nếu mã lớn nhất là HD010).
	 */
	public String getNextMaHD() {
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
	 * 
	 * @param sdt   Số điện thoại cần tìm/tạo.
	 * @param tenKH Tên khách hàng (chỉ dùng nếu tạo mới, có thể trống).
	 * @return Đối tượng KhachHang tìm thấy hoặc vừa tạo.
	 * @throws SQLException Nếu có lỗi CSDL.
	 */
	public KhachHang timHoacTaoKhachHang(String sdt, String tenKH) throws SQLException {
		// 1. Tìm kiếm khách hàng theo SĐT
		String selectSql = "SELECT maKH, tenKH, soDT, email, ngayDangKy, thanhVien, diaChi FROM KhachHang WHERE soDT = ?";
		try (Connection con = ConnectDB.getConnection(); PreparedStatement psSelect = con.prepareStatement(selectSql)) {
			psSelect.setString(1, sdt);
			try (ResultSet rs = psSelect.executeQuery()) {
				if (rs.next()) {
					// Tìm thấy -> Tạo đối tượng KhachHang từ dữ liệu DB
					LocalDate ngayDangKyFromDB = rs.getDate("ngayDangKy") != null
							? rs.getDate("ngayDangKy").toLocalDate()
							: null;
					return new KhachHang(rs.getString("maKH"), rs.getString("tenKH"), rs.getString("soDT"),
							rs.getString("email"), ngayDangKyFromDB, rs.getString("diaChi"), rs.getString("thanhVien"));
				}
			}
		} catch (SQLException e) {
			System.err.println("Lỗi khi tìm khách hàng theo SĐT: " + e.getMessage());
			throw e; // Ném lại lỗi để UI xử lý
		}

		// 2. Không tìm thấy -> Tạo khách hàng mới
		try (Connection con = ConnectDB.getConnection()) {
			String newMaKH = getNextMaKH();
			String tenKhachMoi = (tenKH == null || tenKH.trim().isEmpty()) ? "Khách vãng lai" : tenKH.trim();
			LocalDate ngayHienTai = LocalDate.now();
			String insertSql = "INSERT INTO KhachHang (maKH, tenKH, soDT, ngayDangKy, thanhVien) VALUES (?, ?, ?, ?, ?)";

			try (PreparedStatement psInsert = con.prepareStatement(insertSql)) {
				psInsert.setString(1, newMaKH);
				psInsert.setNString(2, tenKhachMoi); // Dùng setNString cho nvarchar
				psInsert.setString(3, sdt);
				psInsert.setDate(4, java.sql.Date.valueOf(ngayHienTai)); // Chuyển LocalDate sang sql.Date
				psInsert.setNString(5, "Guest"); // Mặc định hạng thành viên
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
	 * Lưu Hóa đơn và Chi tiết Hóa đơn (Đơn đặt hàng/đến quán) Đã cập nhật: Sử dụng
	 * dbValue của enum TrangThaiHoaDon, lấy mã từ entity Ban, KH.
	 */
	public void luuHoaDonVaChiTiet(HoaDon hoaDon, ObservableList<MonOrder> monOrderList) throws SQLException {
		String maHD = getNextMaHD();
		hoaDon.setMaHD(maHD); // Gán mã HD mới tạo vào đối tượng HoaDon luôn

		String hdSql = "INSERT INTO HoaDon (maHD, ngayLap, trangThai, gioVao, maBan, maKH, tienCoc, maUuDai, ptThanhToan, maNV) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(hdSql)) {

			ps.setString(1, maHD);
			ps.setTimestamp(2, hoaDon.getNgayLap() != null ? Timestamp.valueOf(hoaDon.getNgayLap()) : null);

			String trangThaiDbValue = (hoaDon.getTrangThai() != null) ? hoaDon.getTrangThai().getDbValue() : null;
			ps.setString(3, trangThaiDbValue);

			ps.setTimestamp(4, hoaDon.getGioVao() != null ? Timestamp.valueOf(hoaDon.getGioVao()) : null);
			ps.setString(5, (hoaDon.getBan() != null) ? hoaDon.getBan().getMaBan() : null);
			ps.setString(6, (hoaDon.getKhachHang() != null) ? hoaDon.getKhachHang().getMaKH() : null);
			ps.setDouble(7, hoaDon.getTienCoc());
			ps.setString(8, hoaDon.getMaUuDai());

			ps.setNull(9, Types.NVARCHAR); // ptThanhToan null khi mới tạo
			ps.setNull(10, Types.VARCHAR); // maNV null khi mới tạo

			ps.executeUpdate();

			// Lưu Chi tiết Hóa đơn
			if (monOrderList != null && !monOrderList.isEmpty()) {
				String cthdSql = "INSERT INTO ChiTietHoaDon (maHD, maMon, soLuong, thanhTien) VALUES (?, ?, ?, ?)";
				try (PreparedStatement psCt = con.prepareStatement(cthdSql)) {
					for (MonOrder order : monOrderList) {
						psCt.setString(1, maHD);
						psCt.setString(2, order.getMaMon());
						psCt.setInt(3, order.getSoLuong());
						psCt.setDouble(4, order.getThanhTien());
						psCt.addBatch();
					}
					psCt.executeBatch();
				}
			}
			System.out.println("LOG DAO: Đã lưu thành công hóa đơn " + maHD);
		} catch (SQLException e) {
			System.err.println("Lỗi khi lưu hóa đơn và chi tiết: SQL State: " + e.getSQLState() + ", Error Code: "
					+ e.getErrorCode());
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * Lấy chi tiết món ăn (MonOrder ViewModel) của một hóa đơn cụ thể.
	 */
	public ObservableList<ui.DatBan.MonOrder> getChiTietHoaDon(String maHD) {
		ObservableList<ui.DatBan.MonOrder> list = FXCollections.observableArrayList();
		if (maHD == null)
			return list;
		String sql = """
				    SELECT cthd.maMon, cthd.soLuong, m.tenMon, m.giaBan
				    FROM ChiTietHoaDon cthd
				    JOIN MonAn m ON cthd.maMon = m.maMon
				    WHERE cthd.maHD = ?
				""";
		try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setString(1, maHD);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					list.add(new ui.DatBan.MonOrder(rs.getString("maMon"), rs.getString("tenMon"),
							rs.getDouble("giaBan"), rs.getInt("soLuong")));
				}
			}
		} catch (SQLException e) {
			System.err.println("❌ Lỗi khi tải chi tiết hóa đơn " + maHD + ": " + e.getMessage());
			e.printStackTrace();
		}
		return list;
	}

	/**
	 * Lấy danh sách bàn trống tại một thời điểm cụ thể.
	 * 
	 * @param gioDen Thời điểm cần kiểm tra (dưới dạng Timestamp).
	 * @return Danh sách các đối tượng Ban đang trống tại thời điểm đó.
	 */
	public List<Ban> getBanTrongTheoGio(Timestamp gioDen) {
		List<Ban> list = new ArrayList<>();
		// SQL kiểm tra xem bàn có bị trùng lịch đặt ('Dat') hoặc đang sử dụng
		// ('DangSuDung')
		// trong khoảng thời gian bao gồm giờ đến hay không.
		String sql = """
				    SELECT b.*
				    FROM Ban b
				    WHERE b.maBan NOT IN (
				        SELECT h.maBan
				        FROM HoaDon h
				        WHERE
				            h.maBan IS NOT NULL AND
				            h.trangThai IN (?, ?) -- 'Dat', 'DangSuDung'
				            AND (
				                 ? BETWEEN h.gioVao AND ISNULL(h.gioRa, '9999-12-31 23:59:59') -- Kiểm tra xem giờ đến có nằm trong khoảng bận không
				                 -- Hoặc kiểm tra xem khoảng thời gian đặt/sử dụng có bao gồm giờ đến không
				                 -- (Phức tạp hơn và tùy thuộc vào logic nghiệp vụ về thời gian sử dụng ước tính)
				            )
				    ) AND b.trangThai = ? -- Chỉ lấy bàn có trạng thái gốc là 'Trong'
				""";
		try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

			ps.setString(1, TrangThaiHoaDon.DAT.getDbValue());
			ps.setTimestamp(3, gioDen);
			ps.setString(4, TrangThaiBan.TRONG.getDbValue()); // Chỉ kiểm tra những bàn đang 'Trong'

			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				// Tạo đối tượng Ban từ ResultSet
				Ban ban = new Ban(rs.getString("maBan"), rs.getString("viTri"), rs.getInt("sucChua"),
						LoaiBan.fromString(rs.getString("loaiBan")), // Giả định có enum LoaiBan
						TrangThaiBan.fromDbValue(rs.getString("trangThai")) // Dùng fromDbValue
				);
				list.add(ban);
			}
		} catch (SQLException e) {
			System.err.println("Lỗi khi lấy bàn trống theo giờ: " + e.getMessage());
			e.printStackTrace();
		}
		return list;
	}

	/**
	 * Lấy tất cả các bàn trong nhà hàng.
	 * 
	 * @return Danh sách tất cả đối tượng Ban.
	 */
	public List<Ban> getAllBan() {
		List<Ban> list = new ArrayList<>();
		String sql = "SELECT * FROM Ban ORDER BY maBan"; // Sắp xếp để hiển thị nhất quán
		try (Connection con = ConnectDB.getConnection();
				PreparedStatement ps = con.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {
			while (rs.next()) {
				Ban ban = new Ban(rs.getString("maBan"), rs.getString("viTri"), rs.getInt("sucChua"),
						LoaiBan.fromString(rs.getString("loaiBan")),
						TrangThaiBan.fromDbValue(rs.getString("trangThai")));
				list.add(ban);
			}
		} catch (SQLException e) {
			System.err.println("Lỗi khi lấy tất cả bàn: " + e.getMessage());
			e.printStackTrace();
		}
		return list;
	}

	/**
	 * Trả về danh sách Hóa đơn Đã Đặt/Đang Sử Dụng trong ngày HOẶC đang hoạt động
	 * từ ngày hôm trước. Đã cập nhật hoàn chỉnh: Sử dụng Enum và Setters, tạo đối
	 * tượng đầy đủ.
	 */
	public List<HoaDon> getDsDatBanHomNay(LocalDate date) {
		List<HoaDon> list = new ArrayList<>();
		String sql = """
				    SELECT
				        hd.maHD, hd.ngayLap, hd.maUuDai, hd.ptThanhToan, hd.trangThai, hd.gioVao, hd.gioRa, hd.tienCoc,
				        hd.maKH, kh.tenKH, kh.soDT, kh.email AS khEmail, kh.ngayDangKy AS khNgayDK, kh.thanhVien AS khThanhVien, kh.diaChi AS khDiaChi,
				        hd.maBan, b.viTri AS banViTri, b.sucChua AS banSucChua, b.loaiBan AS banLoaiBan, b.trangThai AS banTrangThai,
				        hd.maNV, n.tenNV
				    FROM HoaDon hd
				    LEFT JOIN Ban b ON hd.maBan = b.maBan
				    LEFT JOIN KhachHang kh ON hd.maKH = kh.maKH
				    LEFT JOIN NhanVien n ON hd.maNV = n.maNV
				    WHERE
				        hd.trangThai IN (?, ?) -- 'Dat', 'DangSuDung'
				        AND (
				            CAST(hd.gioVao AS DATE) = ?
				            OR (hd.trangThai = ? AND CAST(hd.gioVao AS DATE) < ?) -- 'DangSuDung'
				        )
				     ORDER BY hd.gioVao ASC
				""";

		try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {

			ps.setString(1, TrangThaiHoaDon.DAT.getDbValue());
			ps.setDate(3, java.sql.Date.valueOf(date));
			ps.setDate(5, java.sql.Date.valueOf(date));

			ResultSet rs = ps.executeQuery();

			while (rs.next()) {
				HoaDon hoaDon = new HoaDon();

				// Thông tin Hóa đơn
				hoaDon.setMaHD(rs.getString("maHD"));
				hoaDon.setMaUuDai(rs.getString("maUuDai"));
				hoaDon.setTienCoc(rs.getDouble("tienCoc"));
				hoaDon.setTenNhanVien(rs.getString("tenNV"));
				hoaDon.setHinhThucTT(PTTThanhToan.fromDbValue(rs.getString("ptThanhToan")));
				hoaDon.setTrangThai(TrangThaiHoaDon.fromDbValue(rs.getString("trangThai")));
				Timestamp tsNgayLap = rs.getTimestamp("ngayLap");
				hoaDon.setNgayLap((tsNgayLap != null) ? tsNgayLap.toLocalDateTime() : null);
				Timestamp tsGioVao = rs.getTimestamp("gioVao");
				hoaDon.setGioVao((tsGioVao != null) ? tsGioVao.toLocalDateTime() : null);
				Timestamp tsGioRa = rs.getTimestamp("gioRa");
				hoaDon.setGioRa((tsGioRa != null) ? tsGioRa.toLocalDateTime() : null);

				// Thông tin Khách hàng
				if (rs.getString("maKH") != null) {
					LocalDate ngayDK = rs.getDate("khNgayDK") != null ? rs.getDate("khNgayDK").toLocalDate() : null;
					KhachHang kh = new KhachHang(rs.getString("maKH"), rs.getString("tenKH"), rs.getString("soDT"),
							rs.getString("khEmail"), ngayDK, rs.getString("khDiaChi"), rs.getString("khThanhVien"));
					hoaDon.setKhachHang(kh);
				} else {
					hoaDon.setKhachHang(null);
				}

				// Thông tin Bàn
				if (rs.getString("maBan") != null) {
					Ban ban = new Ban(rs.getString("maBan"), rs.getString("banViTri"), rs.getInt("banSucChua"),
							LoaiBan.fromString(rs.getString("banLoaiBan")),
							TrangThaiBan.fromDbValue(rs.getString("banTrangThai")));
					hoaDon.setBan(ban);
				} else {
					hoaDon.setBan(null);
				}

				// Tạm thời chưa tính tổng món ăn ở đây
				hoaDon.setTongCongMonAn(0);

				list.add(hoaDon);
			}
		} catch (SQLException e) {
			System.err.println("Lỗi khi lấy danh sách đặt bàn hôm nay: " + e.getMessage());
			e.printStackTrace();
			return new ArrayList<>();
		}
		return list;
	}

	/**
	 * Cập nhật trạng thái của một bàn trong CSDL.
	 * 
	 * @param maBan            Mã bàn cần cập nhật.
	 * @param trangThaiDbValue Giá trị trạng thái mới (phải là dbValue của enum
	 *                         TrangThaiBan).
	 */
	public void capNhatTrangThaiBan(String maBan, String trangThaiDbValue) {
		// Kiểm tra đầu vào (tránh null và đảm bảo là dbValue hợp lệ)
		if (maBan == null || trangThaiDbValue == null || TrangThaiBan.fromDbValue(trangThaiDbValue) == null) {
			System.err.println("CẢNH BÁO DAO: Thông tin cập nhật trạng thái bàn không hợp lệ (maBan=" + maBan
					+ ", trangThai=" + trangThaiDbValue + ")");
			return;
		}

		String sql = "UPDATE Ban SET trangThai = ? WHERE maBan = ?";
		try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setString(1, trangThaiDbValue);
			ps.setString(2, maBan);
			int rowsAffected = ps.executeUpdate();
			if (rowsAffected > 0) {
				System.out.println(
						"LOG DAO: Cập nhật bàn " + maBan + " thành trạng thái '" + trangThaiDbValue + "' thành công.");
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
	 * 
	 * @param maHD        Mã hóa đơn cần cập nhật.
	 * @param ptThanhToan Phương thức thanh toán được sử dụng (Enum PTTThanhToan).
	 */
	public void capNhatKhiThanhToan(String maHD, PTTThanhToan ptThanhToan) {
		if (maHD == null) {
			System.err.println("ERROR DAO: Không thể cập nhật thanh toán với maHD null.");
			return;
		}
		String trangThaiDbValue = TrangThaiHoaDon.DA_THANH_TOAN.getDbValue();
		String ptThanhToanDbValue = (ptThanhToan != null) ? ptThanhToan.getDbValue() : null;

		// Cập nhật giờ ra là thời điểm hiện tại, trạng thái thành Đã Thanh Toán, và
		// PTTT
		String sql = "UPDATE HoaDon SET gioRa = GETDATE(), trangThai = ?, ptThanhToan = ? WHERE maHD = ?";
		try (Connection con = ConnectDB.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
			ps.setString(1, trangThaiDbValue);
			ps.setString(2, ptThanhToanDbValue); // Có thể là null nếu không chọn PTTT
			ps.setString(3, maHD);
			int updatedRows = ps.executeUpdate();
			if (updatedRows > 0) {
				System.out.println("LOG DAO: Đã cập nhật thanh toán cho HD: " + maHD + " với PTTT: "
						+ (ptThanhToanDbValue != null ? ptThanhToanDbValue : "NULL"));
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
}
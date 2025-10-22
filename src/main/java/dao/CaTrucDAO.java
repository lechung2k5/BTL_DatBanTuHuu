package dao;

import connect.ConnectDB;
import entity.CaTruc;
import entity.NhanVien;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CaTrucDAO {

    public List<String> layTatCaMauCaLam() {
        List<String> danhSachCaLam = new ArrayList<>();
        String sql = "SELECT DISTINCT gioBatDau, gioKetThuc FROM CaTruc ORDER BY gioBatDau";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Time gioBatDau = rs.getTime("gioBatDau");
                Time gioKetThuc = rs.getTime("gioKetThuc");
                String caLamFormatted = String.format("Ca (%s - %s)",
                                           gioBatDau.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                                           gioKetThuc.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));
                danhSachCaLam.add(caLamFormatted);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return danhSachCaLam;
    }

    public String layMaCaCuoiCung() {
        String sql = "SELECT TOP 1 maCa FROM CaTruc ORDER BY maCa DESC";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getString("maCa");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean themMoiCaTruc(CaTruc caTruc) {
        String sql = "INSERT INTO CaTruc (maCa, ngay, gioBatDau, gioKetThuc, maNV) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, caTruc.getMaCa());
            ps.setDate(2, Date.valueOf(caTruc.getNgay()));
            ps.setTime(3, Time.valueOf(caTruc.getGioBatDau()));
            ps.setTime(4, Time.valueOf(caTruc.getGioKetThuc()));
            ps.setString(5, caTruc.getNhanVien().getMaNV());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public CaTruc layCaTrucGanNhatCuaNhanVien(String maNV) {
        String sql = "SELECT TOP 1 CT.*, NV.tenNV " + // Lấy thêm tên NV
                     "FROM CaTruc CT JOIN NhanVien NV ON CT.maNV = NV.maNV " +
                     "WHERE CT.maNV = ? ORDER BY ngay DESC, gioBatDau DESC";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maNV);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    NhanVien nv = new NhanVien();
                    nv.setMaNV(maNV);
                    nv.setHoTen(rs.getString("tenNV")); // Gán tên lấy được

                    CaTruc caTruc = new CaTruc();
                    caTruc.setMaCa(rs.getString("maCa"));
                    caTruc.setNgay(rs.getDate("ngay").toLocalDate());
                    caTruc.setGioBatDau(rs.getTime("gioBatDau").toLocalTime());
                    caTruc.setGioKetThuc(rs.getTime("gioKetThuc").toLocalTime());
                    caTruc.setNhanVien(nv);
                    return caTruc;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    public boolean capNhatGioTruc(String maCa, LocalTime gioBatDau, LocalTime gioKetThuc) {
        String sql = "UPDATE CaTruc SET gioBatDau = ?, gioKetThuc = ? WHERE maCa = ?";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setTime(1, Time.valueOf(gioBatDau));
            ps.setTime(2, Time.valueOf(gioKetThuc));
            ps.setString(3, maCa);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 🔥 HÀM GỐC: Lấy tất cả các ca trực trong một tuần (cho màn hình PhanCaTruc).
     * @param ngayDauTuan Ngày thứ Hai của tuần cần lấy dữ liệu.
     * @return Danh sách các đối tượng CaTruc trong tuần đó.
     */
    public List<CaTruc> layCaTrucTrongTuan(LocalDate ngayDauTuan) { // Giữ nguyên hàm gốc
        List<CaTruc> caTrucList = new ArrayList<>();
        LocalDate ngayCuoiTuan = ngayDauTuan.plusDays(6);

        String sql = "SELECT CT.*, NV.tenNV " +
                     "FROM CaTruc CT " +
                     "JOIN NhanVien NV ON CT.maNV = NV.maNV " +
                     "WHERE CT.ngay BETWEEN ? AND ?";

        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(ngayDauTuan));
            ps.setDate(2, Date.valueOf(ngayCuoiTuan));

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    NhanVien nv = new NhanVien();
                    nv.setMaNV(rs.getString("maNV"));
                    nv.setHoTen(rs.getString("tenNV"));

                    CaTruc caTruc = new CaTruc(
                        rs.getString("maCa"),
                        rs.getDate("ngay").toLocalDate(),
                        rs.getTime("gioBatDau").toLocalTime(),
                        rs.getTime("gioKetThuc").toLocalTime(),
                        nv
                    );
                    caTrucList.add(caTruc);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return caTrucList;
    }

    /**
     * 🔥 HÀM MỚI (Đã thêm trước đó): Lấy tất cả các ca trực của MỘT NHÂN VIÊN trong tuần (cho Dashboard).
     * @param ngayDauTuan Ngày thứ Hai của tuần cần lấy dữ liệu.
     * @param maNV Mã nhân viên cần xem lịch.
     * @return Danh sách các đối tượng CaTruc của nhân viên đó trong tuần.
     */
    public List<CaTruc> layCaTrucTrongTuanCuaNV(LocalDate ngayDauTuan, String maNV) { // Giữ lại hàm này cho Dashboard
        List<CaTruc> caTrucList = new ArrayList<>();
        LocalDate ngayCuoiTuan = ngayDauTuan.plusDays(6);

        String sql = "SELECT CT.*, NV.tenNV " +
                     "FROM CaTruc CT " +
                     "JOIN NhanVien NV ON CT.maNV = NV.maNV " +
                     "WHERE CT.ngay BETWEEN ? AND ? AND CT.maNV = ?"; // Lọc theo ngày VÀ mã NV

        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(ngayDauTuan));
            ps.setDate(2, Date.valueOf(ngayCuoiTuan));
            ps.setString(3, maNV);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    NhanVien nv = new NhanVien();
                    nv.setMaNV(rs.getString("maNV"));
                    nv.setHoTen(rs.getString("tenNV"));

                    CaTruc caTruc = new CaTruc(
                        rs.getString("maCa"),
                        rs.getDate("ngay").toLocalDate(),
                        rs.getTime("gioBatDau").toLocalTime(),
                        rs.getTime("gioKetThuc").toLocalTime(),
                        nv
                    );
                    caTrucList.add(caTruc);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return caTrucList;
    }

     public boolean xoaCaTruc(String maCa) {
        String sql = "DELETE FROM CaTruc WHERE maCa = ?";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maCa);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
package dao;

import entity.CaTruc;
import entity.NhanVien;
import connect.ConnectDB;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class NhanVienDAO {
    private final CaTrucDAO caTrucDAO = new CaTrucDAO();

    private NhanVien extractNhanVienFromResultSet(ResultSet rs) throws SQLException {
        NhanVien nv = new NhanVien();
        String maNV = rs.getString("maNV");
        nv.setMaNV(maNV);
        nv.setHoTen(rs.getString("tenNV"));
        nv.setSdt(rs.getString("soDT"));
        nv.setEmail(rs.getString("email"));

        LocalDate ngaySinhDate = rs.getDate("ngaySinh") != null ? rs.getDate("ngaySinh").toLocalDate() : null;
        nv.setNgaySinh(ngaySinhDate != null ? ngaySinhDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : null);

        nv.setDiaChi(rs.getString("diaChi"));
        nv.setGioiTinh(rs.getBoolean("gioiTinh") ? "Nam" : "Nữ");
        nv.setTrangThai(rs.getBoolean("trangThai") ? "Đang làm" : "Nghỉ việc");
        
        nv.setCaLamYeuThich(rs.getString("caLamYeuThich")); // 🔥 ĐỌC CA YÊU THÍCH

        nv.setChucVu(rs.getString("vaiTro"));
        nv.setMatKhau(rs.getString("matKhau"));

        // Lấy ca làm (THỰC TẾ) từ CaTrucDAO
        CaTruc caTrucGanNhat = caTrucDAO.layCaTrucGanNhatCuaNhanVien(maNV);
        if (caTrucGanNhat != null) {
            nv.setCaLam(caTrucGanNhat.toString());
        } else {
            nv.setCaLam("Chưa được xếp ca");
        }

        return nv;
    }

    // Hàm này dùng để lấy full thông tin cho bảng quản lý NV
     public List<NhanVien> layTatCaNhanVien() {
        List<NhanVien> list = new ArrayList<>();
        // 🔥 Thêm cột caLamYeuThich vào SELECT
        String sql = "SELECT NV.*, TK.vaiTro, TK.matKhau, NV.caLamYeuThich " +
                     "FROM NhanVien NV LEFT JOIN TaiKhoan TK ON NV.maNV = TK.maNV ORDER BY NV.maNV";
        try (Connection con = ConnectDB.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(extractNhanVienFromResultSet(rs)); // Dùng hàm extract phức tạp hơn
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }


    // Hàm này chỉ lấy thông tin cơ bản cho việc chọn NV trong Phân Ca
    public List<NhanVien> layTatCaNhanVienHoatDong() {
        List<NhanVien> list = new ArrayList<>();
        // 🔥 Thêm cột caLamYeuThich vào SELECT
        String sql = "SELECT NV.maNV, NV.tenNV, TK.vaiTro, NV.caLamYeuThich " +
                     "FROM NhanVien NV LEFT JOIN TaiKhoan TK ON NV.maNV = TK.maNV " +
                     "WHERE NV.trangThai = 1 ORDER BY NV.tenNV";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                NhanVien nv = new NhanVien();
                nv.setMaNV(rs.getString("maNV"));
                nv.setHoTen(rs.getString("tenNV"));
                nv.setChucVu(rs.getString("vaiTro"));
                nv.setCaLamYeuThich(rs.getString("caLamYeuThich")); // 🔥 LẤY CA YÊU THÍCH
                list.add(nv);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }


    public NhanVien getNhanVienTheoMa(String maNV) {
        // 🔥 Thêm cột caLamYeuThich vào SELECT
         String sql = "SELECT NV.*, TK.vaiTro, TK.matKhau, NV.caLamYeuThich " +
                      "FROM NhanVien NV LEFT JOIN TaiKhoan TK ON NV.maNV = TK.maNV WHERE NV.maNV = ?";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maNV);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return extractNhanVienFromResultSet(rs);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

     public String layMaNVCuoiCung() {
        String sql = "SELECT TOP 1 maNV FROM NhanVien ORDER BY maNV DESC";
        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getString("maNV");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean themNhanVien(NhanVien nv) {
        // 🔥 Thêm cột caLamYeuThich vào INSERT
        String sqlNV = "INSERT INTO NhanVien (maNV, tenNV, soDT, email, ngaySinh, diaChi, gioiTinh, trangThai, caLamYeuThich) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        String sqlTK = "INSERT INTO TaiKhoan (tenDangNhap, matKhau, vaiTro, maNV) VALUES (?, ?, ?, ?)";
        Connection con = null;
        try {
            con = ConnectDB.getConnection();
            con.setAutoCommit(false);

            try (PreparedStatement psNV = con.prepareStatement(sqlNV)) {
                psNV.setString(1, nv.getMaNV());
                psNV.setString(2, nv.getTenNV_entity());
                psNV.setString(3, nv.getSoDT_entity());
                psNV.setString(4, nv.getEmail_entity());
                psNV.setDate(5, nv.getNgaySinh_entity() != null ? Date.valueOf(nv.getNgaySinh_entity()) : null);
                psNV.setString(6, nv.getDiaChi_entity());
                psNV.setBoolean(7, nv.getGioiTinh_entity());
                psNV.setBoolean(8, nv.getTrangThai_entity());
                psNV.setString(9, nv.getCaLamYeuThich_entity()); // 🔥 GÁN CA YÊU THÍCH
                if (psNV.executeUpdate() == 0) throw new SQLException("Thêm Nhân Viên thất bại.");
            }

            try (PreparedStatement psTK = con.prepareStatement(sqlTK)) {
                psTK.setString(1, nv.getMaNV());
                psTK.setString(2, nv.getMatKhau());
                psTK.setString(3, nv.getChucVu());
                psTK.setString(4, nv.getMaNV());
                if (psTK.executeUpdate() == 0) throw new SQLException("Thêm Tài Khoản thất bại.");
            }
            con.commit();
            return true;
        } catch (SQLException e) {
            if (con != null) try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
            return false;
        } finally {
            if (con != null) try { con.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    public boolean capNhatNhanVien(NhanVien nv) {
        // 🔥 Thêm cột caLamYeuThich vào UPDATE
        String sqlNV = "UPDATE NhanVien SET tenNV=?, soDT=?, email=?, ngaySinh=?, diaChi=?, gioiTinh=?, trangThai=?, caLamYeuThich=? WHERE maNV=?";
        String sqlTK = "UPDATE TaiKhoan SET matKhau=?, vaiTro=? WHERE maNV=?";
        Connection con = null;
        try {
            con = ConnectDB.getConnection();
            con.setAutoCommit(false);

            try (PreparedStatement psNV = con.prepareStatement(sqlNV)) {
                psNV.setString(1, nv.getTenNV_entity());
                psNV.setString(2, nv.getSoDT_entity());
                psNV.setString(3, nv.getEmail_entity());
                psNV.setDate(4, nv.getNgaySinh_entity() != null ? Date.valueOf(nv.getNgaySinh_entity()) : null);
                psNV.setString(5, nv.getDiaChi_entity());
                psNV.setBoolean(6, nv.getGioiTinh_entity());
                psNV.setBoolean(7, nv.getTrangThai_entity());
                psNV.setString(8, nv.getCaLamYeuThich_entity()); // 🔥 CẬP NHẬT CA YÊU THÍCH
                psNV.setString(9, nv.getMaNV()); // Mã NV giờ là tham số thứ 9
                psNV.executeUpdate();
            }

            try (PreparedStatement psTK = con.prepareStatement(sqlTK)) {
                psTK.setString(1, nv.getMatKhau());
                psTK.setString(2, nv.getChucVu());
                psTK.setString(3, nv.getMaNV());
                psTK.executeUpdate();
            }
            con.commit();
            return true;
        } catch (SQLException e) {
            if (con != null) try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
            return false;
        } finally {
            if (con != null) try { con.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    // ... (Hàm xoaNhanVien không cần thay đổi)
    public boolean xoaNhanVien(String maNV) {
        String sqlCT = "DELETE FROM CaTruc WHERE maNV=?";
        String sqlTK = "DELETE FROM TaiKhoan WHERE maNV=?";
        String sqlNV = "DELETE FROM NhanVien WHERE maNV=?";
        Connection con = null;
        try {
            con = ConnectDB.getConnection();
            con.setAutoCommit(false);
            try (PreparedStatement psCT = con.prepareStatement(sqlCT)) {
                psCT.setString(1, maNV);
                psCT.executeUpdate();
            }
            try (PreparedStatement psTK = con.prepareStatement(sqlTK)) {
                psTK.setString(1, maNV);
                psTK.executeUpdate();
            }
            try (PreparedStatement psNV = con.prepareStatement(sqlNV)) {
                psNV.setString(1, maNV);
                if (psNV.executeUpdate() == 0) {
                    throw new SQLException("Xóa nhân viên thất bại, không có nhân viên nào với mã: " + maNV);
                }
            }
            con.commit();
            return true;
        } catch (SQLException e) {
            if (con != null) try { con.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            e.printStackTrace();
            return false;
        } finally {
            if (con != null) try { con.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
        }
    }
}
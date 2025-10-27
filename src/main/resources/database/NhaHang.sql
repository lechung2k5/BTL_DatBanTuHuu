
CREATE DATABASE NhaHang;
Go
USE NhaHang;
GO

-- ==========================
-- BẢNG KHÁCH HÀNG (Cấp bậc thành viên: Member, Gold, Diamond)
-- ==========================
CREATE TABLE KhachHang (
    maKH VARCHAR(10) PRIMARY KEY,
    tenKH NVARCHAR(50),
    soDT VARCHAR(15),
    email NVARCHAR(50),
    ngayDangKy DATE,
    diaChi NVARCHAR(100),
    thanhVien NVARCHAR(20) -- Member (0-199), Gold (200-449), Diamond (>=450)
);
GO

INSERT INTO KhachHang VALUES
('KH001', N'Nguyễn Thị Hồng', '0912345678', 'hong.nguyen@example.com', '2024-12-15', N'Quận 1, TP.HCM', N'Member'),
('KH002', N'Lê Văn Nam', '0987654321', 'nam.le@example.com', '2023-07-20', N'Bình Thạnh, TP.HCM', N'Gold'),
('KH003', N'Dương Gia Minh', '0933555777', 'minh.duong@example.com', '2025-01-10', N'Tân Phú, TP.HCM', N'Diamond');
GO

-- ==========================
-- BẢNG ƯU ĐÃI
-- ==========================
CREATE TABLE UuDai (
    maUuDai VARCHAR(10) PRIMARY KEY,
    tenUuDai NVARCHAR(50),
    moTa NVARCHAR(255),
    giaTri DECIMAL(18,2),
    ngayBatDau DATE,
    ngayKetThuc DATE
);
GO

INSERT INTO UuDai VALUES
('UD001', N'Giảm 10% hóa đơn thành viên Gold/Diamond', N'Giảm 10% cho hóa đơn > 200.000 VND (Gold), hoặc 15% cho hóa đơn bất kì (Diamond)', 10, '2025-10-01', '2025-12-31'),
('UD002', N'Tặng món tráng miệng', N'Cho đơn từ 500.000đ trở lên', 0, '2025-09-01', '2025-11-30');
GO

-- ==========================
-- BẢNG DANH MỤC MÓN
-- ==========================
CREATE TABLE DanhMucMon (
    maDM VARCHAR(10) PRIMARY KEY,
    tenDM NVARCHAR(50)
);
GO

INSERT INTO DanhMucMon VALUES
('DM001', N'Món chính'),
('DM002', N'Món khai vị'),
('DM003', N'Trà & Nước giải khát'),
('DM004', N'Tráng miệng');
GO

-- ==========================
-- BẢNG MÓN ĂN
-- ==========================
CREATE TABLE MonAn (
    maMon VARCHAR(10) PRIMARY KEY,
    tenMon NVARCHAR(100),
    hinhAnh VARBINARY(MAX),
    giaBan DECIMAL(18,2),
    maDM VARCHAR(10),
    CONSTRAINT FK_MonAn_DanhMuc FOREIGN KEY (maDM) REFERENCES DanhMucMon(maDM)
);
GO

INSERT INTO MonAn VALUES
('MA001', N'Cơm chiên hải sản', NULL, 55000, 'DM001'),
('MA002', N'Bò lúc lắc khoai tây', NULL, 89000, 'DM001'),
('MA003', N'Súp bí đỏ kem tươi', NULL, 39000, 'DM002'),
('MA004', N'Trà đào cam sả', NULL, 29000, 'DM003'),
('MA005', N'Bánh flan caramel', NULL, 25000, 'DM004');
GO

-- ==========================
-- BẢNG NHÂN VIÊN
-- ==========================
CREATE TABLE NhanVien (
    maNV VARCHAR(10) PRIMARY KEY,
    tenNV NVARCHAR(50),
    soDT VARCHAR(15),
    email NVARCHAR(50),
    ngaySinh DATE,
    diaChi NVARCHAR(100),
    gioiTinh BIT,
    trangThai BIT,
    hinhAnh VARBINARY(MAX)
);
GO

INSERT INTO NhanVien VALUES
('NV001', N'Lê Công Chung', '0901111222', 'chung.le@iuh.edu.vn', '2001-01-15', N'Quận 12, TP.HCM', 1, 1, NULL),
('NV002', N'Lê Hoài Phước Mãi', '0903333444', 'mai.le@iuh.edu.vn', '2002-07-21', N'Gò Vấp, TP.HCM', 0, 1, NULL),
('NV003', N'Trần Hoàng Nam', '0905555666', 'nam.tran@iuh.edu.vn', '2000-12-05', N'Tân Bình, TP.HCM', 1, 1, NULL);
GO

-- ==========================
-- BẢNG TÀI KHOẢN
-- ==========================
CREATE TABLE TaiKhoan (
    tenDangNhap VARCHAR(30) PRIMARY KEY,
    matKhau NVARCHAR(100),
    vaiTro NVARCHAR(20),
    maNV VARCHAR(10),
    CONSTRAINT FK_TaiKhoan_NhanVien FOREIGN KEY (maNV) REFERENCES NhanVien(maNV)
);
GO

INSERT INTO TaiKhoan VALUES
('admin', N'123456', N'QuanLy', 'NV001'),
('cashier01', N'123456', N'NhanVienThuNgan', 'NV002'),
('cashier02', N'123456', N'NhanVienThuNgan', 'NV003');
GO

-- ==========================
-- BẢNG CA TRỰC
-- ==========================
CREATE TABLE CaTruc (
    maCa VARCHAR(10) PRIMARY KEY,
    ngay DATE,
    gioBatDau TIME,
    gioKetThuc TIME,
    maNV VARCHAR(10),
    CONSTRAINT FK_CaTruc_NhanVien FOREIGN KEY (maNV) REFERENCES NhanVien(maNV)
);
GO

INSERT INTO CaTruc VALUES
('CA001', '2025-10-18', '08:00', '14:00', 'NV002'),
('CA002', '2025-10-18', '14:00', '22:00', 'NV003'),
('CA003', '2025-10-19', '08:00', '16:00', 'NV001');
GO

-- ==========================
-- BẢNG BÀN
-- ==========================
CREATE TABLE Ban (
    maBan VARCHAR(10) PRIMARY KEY,
    viTri NVARCHAR(50),
    sucChua INT,
    loaiBan NVARCHAR(20),
    trangThai NVARCHAR(20)
);
GO

INSERT INTO Ban VALUES
('B001', N'Tầng trệt - Gần cửa chính', 4, N'TANG_TRET', N'Trong'),
('B002', N'Tầng 1 - Khu lửng', 10, N'TANG_1', N'DangSuDung'),
('B003', N'Phòng VIP - Sảnh trong', 15, N'PHONG', N'DaDat');
GO

-- ==========================
-- BẢNG HÓA ĐƠN
-- ==========================
CREATE TABLE HoaDon (
    maHD VARCHAR(10) PRIMARY KEY,
    ngayLap DATETIME,
    maUuDai VARCHAR(10) NULL,
    ptThanhToan NVARCHAR(20),
    trangThai NVARCHAR(20),
    gioVao DATETIME,
    gioRa DATETIME NULL,
    maBan VARCHAR(10),
    maNV VARCHAR(10),
    maKH VARCHAR(10) NULL,
    CONSTRAINT FK_HoaDon_UuDai FOREIGN KEY (maUuDai) REFERENCES UuDai(maUuDai),
    CONSTRAINT FK_HoaDon_Ban FOREIGN KEY (maBan) REFERENCES Ban(maBan),
    CONSTRAINT FK_HoaDon_NhanVien FOREIGN KEY (maNV) REFERENCES NhanVien(maNV),
    CONSTRAINT FK_HoaDon_KhachHang FOREIGN KEY (maKH) REFERENCES KhachHang(maKH)
);
GO

INSERT INTO HoaDon VALUES
('HD001', '2025-10-18 11:30', NULL, N'TienMat', N'HoaDonCuoi', '2025-10-18 10:45', '2025-10-18 11:40', 'B001', 'NV002', 'KH001'),
('HD002', '2025-10-18 20:15', 'UD001', N'NganHang', N'HoaDonTam', '2025-10-18 19:30', NULL, 'B002', 'NV003', 'KH002');
GO

-- ==========================
-- BẢNG CHI TIẾT HÓA ĐƠN
-- ==========================
CREATE TABLE ChiTietHoaDon (
    maHD VARCHAR(10),
    maMon VARCHAR(10),
    soLuong INT,
    thanhTien DECIMAL(18,2),
    PRIMARY KEY (maHD, maMon),
    CONSTRAINT FK_CTHD_HoaDon FOREIGN KEY (maHD) REFERENCES HoaDon(maHD),
    CONSTRAINT FK_CTHD_MonAn FOREIGN KEY (maMon) REFERENCES MonAn(maMon)
);
GO

INSERT INTO ChiTietHoaDon VALUES
('HD001', 'MA001', 2, 110000),
('HD001', 'MA004', 2, 58000),
('HD002', 'MA002', 1, 89000),
('HD002', 'MA005', 2, 50000);
GO
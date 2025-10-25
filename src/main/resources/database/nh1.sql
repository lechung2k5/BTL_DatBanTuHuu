USE [master]
GO

/****** Object:  Database [NhaHang]    Script Date: 10/22/2025 4:35:00 PM ******/
-- Ki?m tra n?u DB ?ã t?n t?i thì xóa ?i
IF DB_ID('NhaHang') IS NOT NULL
BEGIN
    ALTER DATABASE [NhaHang] SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE [NhaHang];
END
GO

CREATE DATABASE [NhaHang]
 CONTAINMENT = NONE
 ON  PRIMARY
( NAME = N'NhaHang', FILENAME = N'C:\Program Files\Microsoft SQL Server\MSSQL16.MSSQLSERVER\MSSQL\DATA\NhaHang.mdf' , SIZE = 8192KB , MAXSIZE = UNLIMITED, FILEGROWTH = 65536KB )
 LOG ON
( NAME = N'NhaHang_log', FILENAME = N'C:\Program Files\Microsoft SQL Server\MSSQL16.MSSQLSERVER\MSSQL\DATA\NhaHang_log.ldf' , SIZE = 8192KB , MAXSIZE = 2048GB , FILEGROWTH = 65536KB )
 WITH CATALOG_COLLATION = DATABASE_DEFAULT, LEDGER = OFF
GO

ALTER DATABASE [NhaHang] SET COMPATIBILITY_LEVEL = 160
GO
IF (1 = FULLTEXTSERVICEPROPERTY('IsFullTextInstalled'))
begin
EXEC [NhaHang].[dbo].[sp_fulltext_database] @action = 'enable'
end
GO
ALTER DATABASE [NhaHang] SET ANSI_NULL_DEFAULT OFF
GO
ALTER DATABASE [NhaHang] SET ANSI_NULLS OFF
GO
ALTER DATABASE [NhaHang] SET ANSI_PADDING OFF
GO
ALTER DATABASE [NhaHang] SET ANSI_WARNINGS OFF
GO
ALTER DATABASE [NhaHang] SET ARITHABORT OFF
GO
ALTER DATABASE [NhaHang] SET AUTO_CLOSE OFF
GO
ALTER DATABASE [NhaHang] SET AUTO_SHRINK OFF
GO
ALTER DATABASE [NhaHang] SET AUTO_UPDATE_STATISTICS ON
GO
ALTER DATABASE [NhaHang] SET CURSOR_CLOSE_ON_COMMIT OFF
GO
ALTER DATABASE [NhaHang] SET CURSOR_DEFAULT  GLOBAL
GO
ALTER DATABASE [NhaHang] SET CONCAT_NULL_YIELDS_NULL OFF
GO
ALTER DATABASE [NhaHang] SET NUMERIC_ROUNDABORT OFF
GO
ALTER DATABASE [NhaHang] SET QUOTED_IDENTIFIER OFF
GO
ALTER DATABASE [NhaHang] SET RECURSIVE_TRIGGERS OFF
GO
ALTER DATABASE [NhaHang] SET  ENABLE_BROKER
GO
ALTER DATABASE [NhaHang] SET AUTO_UPDATE_STATISTICS_ASYNC OFF
GO
ALTER DATABASE [NhaHang] SET DATE_CORRELATION_OPTIMIZATION OFF
GO
ALTER DATABASE [NhaHang] SET TRUSTWORTHY OFF
GO
ALTER DATABASE [NhaHang] SET ALLOW_SNAPSHOT_ISOLATION OFF
GO
ALTER DATABASE [NhaHang] SET PARAMETERIZATION SIMPLE
GO
ALTER DATABASE [NhaHang] SET READ_COMMITTED_SNAPSHOT OFF
GO
ALTER DATABASE [NhaHang] SET HONOR_BROKER_PRIORITY OFF
GO
ALTER DATABASE [NhaHang] SET RECOVERY FULL
GO
ALTER DATABASE [NhaHang] SET  MULTI_USER
GO
ALTER DATABASE [NhaHang] SET PAGE_VERIFY CHECKSUM
GO
ALTER DATABASE [NhaHang] SET DB_CHAINING OFF
GO
ALTER DATABASE [NhaHang] SET FILESTREAM( NON_TRANSACTED_ACCESS = OFF )
GO
ALTER DATABASE [NhaHang] SET TARGET_RECOVERY_TIME = 60 SECONDS
GO
ALTER DATABASE [NhaHang] SET DELAYED_DURABILITY = DISABLED
GO
ALTER DATABASE [NhaHang] SET ACCELERATED_DATABASE_RECOVERY = OFF
GO
EXEC sys.sp_db_vardecimal_storage_format N'NhaHang', N'ON'
GO
ALTER DATABASE [NhaHang] SET QUERY_STORE = ON
GO
ALTER DATABASE [NhaHang] SET QUERY_STORE (OPERATION_MODE = READ_WRITE, CLEANUP_POLICY = (STALE_QUERY_THRESHOLD_DAYS = 30), DATA_FLUSH_INTERVAL_SECONDS = 900, INTERVAL_LENGTH_MINUTES = 60, MAX_STORAGE_SIZE_MB = 1000, QUERY_CAPTURE_MODE = AUTO, SIZE_BASED_CLEANUP_MODE = AUTO, MAX_PLANS_PER_QUERY = 200, WAIT_STATS_CAPTURE_MODE = ON)
GO

USE [NhaHang]
GO

-- ----------------------------
-- T?O CÁC B?NG
-- ----------------------------
CREATE TABLE [dbo].[Ban](
	[maBan] [varchar](10) NOT NULL,
	[viTri] [nvarchar](50) NULL,
	[sucChua] [int] NULL,
	[loaiBan] [nvarchar](20) NULL,
	[trangThai] [nvarchar](20) NULL,
PRIMARY KEY CLUSTERED ([maBan] ASC)
) ON [PRIMARY]
GO

CREATE TABLE [dbo].[CaTruc](
	[maCa] [varchar](10) NOT NULL,
	[ngay] [date] NULL,
	[gioBatDau] [time](7) NULL,
	[gioKetThuc] [time](7) NULL,
	[maNV] [varchar](10) NULL,
PRIMARY KEY CLUSTERED ([maCa] ASC)
) ON [PRIMARY]
GO

CREATE TABLE [dbo].[ChiTietHoaDon](
	[maHD] [varchar](10) NOT NULL,
	[maMon] [varchar](10) NOT NULL,
	[soLuong] [int] NULL,
	[thanhTien] [decimal](18, 2) NULL,
PRIMARY KEY CLUSTERED ([maHD] ASC, [maMon] ASC)
) ON [PRIMARY]
GO

CREATE TABLE [dbo].[DanhMucMon](
	[maDM] [varchar](10) NOT NULL,
	[tenDM] [nvarchar](50) NULL,
PRIMARY KEY CLUSTERED ([maDM] ASC)
) ON [PRIMARY]
GO

CREATE TABLE [dbo].[HoaDon](
	[maHD] [varchar](10) NOT NULL,
	[ngayLap] [datetime] NULL,
	[maUuDai] [varchar](10) NULL,
	[ptThanhToan] [nvarchar](20) NULL,
	[trangThai] [nvarchar](20) NULL,
	[gioVao] [datetime] NULL,
	[gioRa] [datetime] NULL,
	[maBan] [varchar](10) NULL,
	[maNV] [varchar](10) NULL,
	[maKH] [varchar](10) NULL,
	[tienCoc] [decimal](18, 2) NULL,
PRIMARY KEY CLUSTERED ([maHD] ASC)
) ON [PRIMARY]
GO

CREATE TABLE [dbo].[KhachHang](
	[maKH] [varchar](10) NOT NULL,
	[tenKH] [nvarchar](50) NULL,
	[soDT] [varchar](15) NULL,
	[email] [nvarchar](50) NULL,
	[ngayDangKy] [date] NULL,
	[thanhVien] [nvarchar](20) NULL,
	[diaChi] [nvarchar](100) NULL,
PRIMARY KEY CLUSTERED ([maKH] ASC)
) ON [PRIMARY]
GO

CREATE TABLE [dbo].[MonAn](
	[maMon] [varchar](10) NOT NULL,
	[tenMon] [nvarchar](100) NULL,
	[hinhAnh] [varbinary](max) NULL,
	[giaBan] [decimal](18, 2) NULL,
	[maDM] [varchar](10) NULL,
PRIMARY KEY CLUSTERED ([maMon] ASC)
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO

CREATE TABLE [dbo].[NhanVien](
	[maNV] [varchar](10) NOT NULL,
	[tenNV] [nvarchar](50) NULL,
	[soDT] [varchar](15) NULL,
	[email] [nvarchar](50) NULL,
	[ngaySinh] [date] NULL,
	[diaChi] [nvarchar](100) NULL,
	[gioiTinh] [bit] NULL,
	[trangThai] [bit] NULL,
	[hinhAnh] [varbinary](max) NULL,
	[caLamYeuThich] [nvarchar](50) NULL,
PRIMARY KEY CLUSTERED ([maNV] ASC)
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO

CREATE TABLE [dbo].[TaiKhoan](
	[tenDangNhap] [varchar](30) NOT NULL,
	[matKhau] [nvarchar](100) NULL,
	[vaiTro] [nvarchar](20) NULL,
	[maNV] [varchar](10) NULL,
PRIMARY KEY CLUSTERED ([tenDangNhap] ASC)
) ON [PRIMARY]
GO

CREATE TABLE [dbo].[UuDai](
	[maUuDai] [varchar](10) NOT NULL,
	[tenUuDai] [nvarchar](50) NULL,
	[moTa] [nvarchar](255) NULL,
	[giaTri] [decimal](18, 2) NULL,
	[ngayBatDau] [date] NULL,
	[ngayKetThuc] [date] NULL,
PRIMARY KEY CLUSTERED ([maUuDai] ASC)
) ON [PRIMARY]
GO

-- ----------------------------
-- D? LI?U B?NG PH?
-- ----------------------------
INSERT [dbo].[Ban] ([maBan], [viTri], [sucChua], [loaiBan], [trangThai]) VALUES (N'B001', N'T?ng tr?t', 4, N'TANG_TRET', N'Trong')
INSERT [dbo].[Ban] ([maBan], [viTri], [sucChua], [loaiBan], [trangThai]) VALUES (N'B002', N'T?ng tr?t', 4, N'TANG_TRET', N'Trong')
INSERT [dbo].[Ban] ([maBan], [viTri], [sucChua], [loaiBan], [trangThai]) VALUES (N'B003', N'T?ng tr?t', 6, N'TANG_TRET', N'Trong')
INSERT [dbo].[Ban] ([maBan], [viTri], [sucChua], [loaiBan], [trangThai]) VALUES (N'B004', N'T?ng tr?t', 4, N'TANG_TRET', N'Trong')
INSERT [dbo].[Ban] ([maBan], [viTri], [sucChua], [loaiBan], [trangThai]) VALUES (N'B005', N'T?ng tr?t', 4, N'TANG_TRET', N'Trong')
INSERT [dbo].[Ban] ([maBan], [viTri], [sucChua], [loaiBan], [trangThai]) VALUES (N'B006', N'T?ng tr?t', 6, N'TANG_TRET', N'Trong')
INSERT [dbo].[Ban] ([maBan], [viTri], [sucChua], [loaiBan], [trangThai]) VALUES (N'B007', N'T?ng tr?t', 4, N'TANG_TRET', N'Trong')
INSERT [dbo].[Ban] ([maBan], [viTri], [sucChua], [loaiBan], [trangThai]) VALUES (N'B008', N'T?ng tr?t', 4, N'TANG_TRET', N'Trong')
INSERT [dbo].[Ban] ([maBan], [viTri], [sucChua], [loaiBan], [trangThai]) VALUES (N'B009', N'T?ng tr?t', 6, N'TANG_TRET', N'Trong')
INSERT [dbo].[Ban] ([maBan], [viTri], [sucChua], [loaiBan], [trangThai]) VALUES (N'B010', N'T?ng tr?t', 4, N'TANG_TRET', N'Trong')
INSERT [dbo].[Ban] ([maBan], [viTri], [sucChua], [loaiBan], [trangThai]) VALUES (N'B011', N'T?ng tr?t', 4, N'TANG_TRET', N'Trong')
INSERT [dbo].[Ban] ([maBan], [viTri], [sucChua], [loaiBan], [trangThai]) VALUES (N'B012', N'T?ng tr?t', 6, N'TANG_TRET', N'Trong')
INSERT [dbo].[Ban] ([maBan], [viTri], [sucChua], [loaiBan], [trangThai]) VALUES (N'B013', N'T?ng tr?t', 4, N'TANG_TRET', N'Trong')
INSERT [dbo].[Ban] ([maBan], [viTri], [sucChua], [loaiBan], [trangThai]) VALUES (N'B014', N'T?ng tr?t', 4, N'TANG_TRET', N'Trong')
INSERT [dbo].[Ban] ([maBan], [viTri], [sucChua], [loaiBan], [trangThai]) VALUES (N'B015', N'T?ng tr?t', 6, N'TANG_TRET', N'Trong')
INSERT [dbo].[Ban] ([maBan], [viTri], [sucChua], [loaiBan], [trangThai]) VALUES (N'B016', N'T?ng tr?t', 4, N'TANG_TRET', N'Trong')
INSERT [dbo].[Ban] ([maBan], [viTri], [sucChua], [loaiBan], [trangThai]) VALUES (N'B017', N'T?ng tr?t', 4, N'TANG_TRET', N'Trong')
INSERT [dbo].[Ban] ([maBan], [viTri], [sucChua], [loaiBan], [trangThai]) VALUES (N'B018', N'T?ng tr?t', 6, N'TANG_TRET', N'Trong')
INSERT [dbo].[Ban] ([maBan], [viTri], [sucChua], [loaiBan], [trangThai]) VALUES (N'B019', N'T?ng tr?t', 4, N'TANG_TRET', N'Trong')
INSERT [dbo].[Ban] ([maBan], [viTri], [sucChua], [loaiBan], [trangThai]) VALUES (N'B020', N'T?ng tr?t', 4, N'TANG_TRET', N'Trong')
INSERT [dbo].[Ban] ([maBan], [viTri], [sucChua], [loaiBan], [trangThai]) VALUES (N'B021', N'T?ng 1', 4, N'TANG_1', N'Trong')
INSERT [dbo].[Ban] ([maBan], [viTri], [sucChua], [loaiBan], [trangThai]) VALUES (N'B022', N'T?ng 1', 4, N'TANG_1', N'Trong')
INSERT [dbo].[Ban] ([maBan], [viTri], [sucChua], [loaiBan], [trangThai]) VALUES (N'B023', N'T?ng 1', 6, N'TANG_1', N'Trong')
INSERT [dbo].[Ban] ([maBan], [viTri], [sucChua], [loaiBan], [trangThai]) VALUES (N'B024', N'T?ng 1', 4, N'TANG_1', N'Trong')
INSERT [dbo].[Ban] ([maBan], [viTri], [sucChua], [loaiBan], [trangThai]) VALUES (N'B025', N'T?ng 1', 4, N'TANG_1', N'Trong')
INSERT [dbo].[Ban] ([maBan], [viTri], [sucChua], [loaiBan], [trangThai]) VALUES (N'B026', N'T?ng 1', 6, N'TANG_1', N'Trong')
INSERT [dbo].[Ban] ([maBan], [viTri], [sucChua], [loaiBan], [trangThai]) VALUES (N'B027', N'T?ng 1', 4, N'TANG_1', N'Trong')
INSERT [dbo].[Ban] ([maBan], [viTri], [sucChua], [loaiBan], [trangThai]) VALUES (N'B028', N'T?ng 1', 4, N'TANG_1', N'Trong')
INSERT [dbo].[Ban] ([maBan], [viTri], [sucChua], [loaiBan], [trangThai]) VALUES (N'B029', N'T?ng 1', 6, N'TANG_1', N'Trong')
INSERT [dbo].[Ban] ([maBan], [viTri], [sucChua], [loaiBan], [trangThai]) VALUES (N'B030', N'T?ng 1', 4, N'TANG_1', N'Trong')
INSERT [dbo].[Ban] ([maBan], [viTri], [sucChua], [loaiBan], [trangThai]) VALUES (N'B031', N'Phòng riêng', 8, N'PHONG', N'Trong')
INSERT [dbo].[Ban] ([maBan], [viTri], [sucChua], [loaiBan], [trangThai]) VALUES (N'B032', N'Phòng riêng', 12, N'PHONG', N'Trong')
INSERT [dbo].[Ban] ([maBan], [viTri], [sucChua], [loaiBan], [trangThai]) VALUES (N'B033', N'Phòng riêng', 8, N'PHONG', N'Trong')
INSERT [dbo].[Ban] ([maBan], [viTri], [sucChua], [loaiBan], [trangThai]) VALUES (N'B034', N'Phòng riêng', 10, N'PHONG', N'Trong')
INSERT [dbo].[Ban] ([maBan], [viTri], [sucChua], [loaiBan], [trangThai]) VALUES (N'B035', N'Phòng riêng', 12, N'PHONG', N'Trong')
INSERT [dbo].[Ban] ([maBan], [viTri], [sucChua], [loaiBan], [trangThai]) VALUES (N'B036', N'Phòng riêng', 8, N'PHONG', N'Trong')
GO
INSERT [dbo].[CaTruc] ([maCa], [ngay], [gioBatDau], [gioKetThuc], [maNV]) VALUES (N'CA001', CAST(N'2025-10-18' AS Date), CAST(N'08:00:00' AS Time), CAST(N'14:00:00' AS Time), N'NV002')
INSERT [dbo].[CaTruc] ([maCa], [ngay], [gioBatDau], [gioKetThuc], [maNV]) VALUES (N'CA002', CAST(N'2025-10-18' AS Date), CAST(N'14:00:00' AS Time), CAST(N'22:00:00' AS Time), N'NV003')
INSERT [dbo].[CaTruc] ([maCa], [ngay], [gioBatDau], [gioKetThuc], [maNV]) VALUES (N'CA003', CAST(N'2025-10-19' AS Date), CAST(N'08:00:00' AS Time), CAST(N'16:00:00' AS Time), N'NV001')
INSERT [dbo].[CaTruc] ([maCa], [ngay], [gioBatDau], [gioKetThuc], [maNV]) VALUES (N'CA041', CAST(N'2025-10-20' AS Date), CAST(N'08:00:00' AS Time), CAST(N'14:00:00' AS Time), N'NV001')
INSERT [dbo].[CaTruc] ([maCa], [ngay], [gioBatDau], [gioKetThuc], [maNV]) VALUES (N'CA042', CAST(N'2025-10-20' AS Date), CAST(N'08:00:00' AS Time), CAST(N'14:00:00' AS Time), N'NV002')
INSERT [dbo].[CaTruc] ([maCa], [ngay], [gioBatDau], [gioKetThuc], [maNV]) VALUES (N'CA043', CAST(N'2025-10-20' AS Date), CAST(N'14:00:00' AS Time), CAST(N'22:00:00' AS Time), N'NV001')
INSERT [dbo].[CaTruc] ([maCa], [ngay], [gioBatDau], [gioKetThuc], [maNV]) VALUES (N'CA044', CAST(N'2025-10-20' AS Date), CAST(N'14:00:00' AS Time), CAST(N'22:00:00' AS Time), N'NV002')
INSERT [dbo].[CaTruc] ([maCa], [ngay], [gioBatDau], [gioKetThuc], [maNV]) VALUES (N'CA045', CAST(N'2025-10-20' AS Date), CAST(N'14:00:00' AS Time), CAST(N'22:00:00' AS Time), N'NV003')
INSERT [dbo].[CaTruc] ([maCa], [ngay], [gioBatDau], [gioKetThuc], [maNV]) VALUES (N'CA046', CAST(N'2025-10-20' AS Date), CAST(N'18:00:00' AS Time), CAST(N'02:00:00' AS Time), N'NV001')
INSERT [dbo].[CaTruc] ([maCa], [ngay], [gioBatDau], [gioKetThuc], [maNV]) VALUES (N'CA047', CAST(N'2025-10-20' AS Date), CAST(N'18:00:00' AS Time), CAST(N'02:00:00' AS Time), N'NV002')
INSERT [dbo].[CaTruc] ([maCa], [ngay], [gioBatDau], [gioKetThuc], [maNV]) VALUES (N'CA048', CAST(N'2025-10-21' AS Date), CAST(N'08:00:00' AS Time), CAST(N'14:00:00' AS Time), N'NV004')
GO
INSERT [dbo].[DanhMucMon] ([maDM], [tenDM]) VALUES (N'DM001', N'Khai v?')
INSERT [dbo].[DanhMucMon] ([maDM], [tenDM]) VALUES (N'DM002', N'N??ng')
INSERT [dbo].[DanhMucMon] ([maDM], [tenDM]) VALUES (N'DM003', N'L?u')
INSERT [dbo].[DanhMucMon] ([maDM], [tenDM]) VALUES (N'DM004', N'Xào/H?p')
INSERT [dbo].[DanhMucMon] ([maDM], [tenDM]) VALUES (N'DM005', N'Chiên')
INSERT [dbo].[DanhMucMon] ([maDM], [tenDM]) VALUES (N'DM006', N'??c s?n')
INSERT [dbo].[DanhMucMon] ([maDM], [tenDM]) VALUES (N'DM007', N'?? u?ng')
GO
INSERT [dbo].[KhachHang] ([maKH], [tenKH], [soDT], [email], [ngayDangKy], [thanhVien], [diaChi]) VALUES (N'KH001', N'Nguy?n Th? H?ng', N'0912345678', N'hong.nguyen@example.com', CAST(N'2024-12-15' AS Date), N'Member', N'Qu?n 1, TP.HCM')
INSERT [dbo].[KhachHang] ([maKH], [tenKH], [soDT], [email], [ngayDangKy], [thanhVien], [diaChi]) VALUES (N'KH002', N'Lê V?n Nam', N'0987654321', N'nam.le@example.com', CAST(N'2023-07-20' AS Date), N'Gold', N'Bình Th?nh, TP.HCM')
INSERT [dbo].[KhachHang] ([maKH], [tenKH], [soDT], [email], [ngayDangKy], [thanhVien], [diaChi]) VALUES (N'KH003', N'D??ng Gia Minh Béo', N'0933555777', N'minh.duong@example.com', CAST(N'2025-01-10' AS Date), N'Diamond', N'Gò V?p, TP.HCM')
INSERT [dbo].[KhachHang] ([maKH], [tenKH], [soDT], [email], [ngayDangKy], [thanhVien], [diaChi]) VALUES (N'KH004', N'Lê Công Chung', N'01234567890', N'chung.le@example.com', CAST(N'2025-10-21' AS Date), N'Member', N'B?ch ??ng, TP.HCM')
INSERT [dbo].[KhachHang] ([maKH], [tenKH], [soDT], [email], [ngayDangKy], [thanhVien], [diaChi]) VALUES (N'KH005', N'Lê Hoài Ph??c Mãi', N'0971166109', N'chung.le@example.com', CAST(N'2025-10-21' AS Date), N'Member', N'An Giang, TP.HCM')
INSERT [dbo].[KhachHang] ([maKH], [tenKH], [soDT], [email], [ngayDangKy], [thanhVien], [diaChi]) VALUES (N'KH006', N'Lê Hoài Ph??c B', N'0971166109', N'chung.le@example.com', CAST(N'2025-10-21' AS Date), N'Member', N'An Giang, TP.HCM')
GO
INSERT [dbo].[MonAn] ([maMon], [tenMon], [hinhAnh], [giaBan], [maDM]) VALUES (N'M001', N'G?i c? h? d?a tôm th?t', NULL, CAST(120000.00 AS Decimal(18, 2)), N'DM001')
INSERT [dbo].[MonAn] ([maMon], [tenMon], [hinhAnh], [giaBan], [maDM]) VALUES (N'M002', N'Ch? giò tôm cua', NULL, CAST(85000.00 AS Decimal(18, 2)), N'DM001')
INSERT [dbo].[MonAn] ([maMon], [tenMon], [hinhAnh], [giaBan], [maDM]) VALUES (N'M003', N'Súp cua th?t gà xé', NULL, CAST(60000.00 AS Decimal(18, 2)), N'DM001')
INSERT [dbo].[MonAn] ([maMon], [tenMon], [hinhAnh], [giaBan], [maDM]) VALUES (N'M004', N'Salad rau tr?n d?u gi?m', NULL, CAST(75000.00 AS Decimal(18, 2)), N'DM001')
INSERT [dbo].[MonAn] ([maMon], [tenMon], [hinhAnh], [giaBan], [maDM]) VALUES (N'M005', N'Bánh ph?ng tôm ?n kèm', NULL, CAST(30000.00 AS Decimal(18, 2)), N'DM001')
INSERT [dbo].[MonAn] ([maMon], [tenMon], [hinhAnh], [giaBan], [maDM]) VALUES (N'M006', N'S??n bò t?ng n??ng s?t BBQ', NULL, CAST(350000.00 AS Decimal(18, 2)), N'DM002')
INSERT [dbo].[MonAn] ([maMon], [tenMon], [hinhAnh], [giaBan], [maDM]) VALUES (N'M007', N'Th?t heo n??ng ri?ng m?', NULL, CAST(180000.00 AS Decimal(18, 2)), N'DM002')
INSERT [dbo].[MonAn] ([maMon], [tenMon], [hinhAnh], [giaBan], [maDM]) VALUES (N'M008', N'Ba ch? heo cu?n n?m kim châm n??ng', NULL, CAST(150000.00 AS Decimal(18, 2)), N'DM002')
INSERT [dbo].[MonAn] ([maMon], [tenMon], [hinhAnh], [giaBan], [maDM]) VALUES (N'M009', N'Tôm sú n??ng mu?i ?t', NULL, CAST(280000.00 AS Decimal(18, 2)), N'DM002')
INSERT [dbo].[MonAn] ([maMon], [tenMon], [hinhAnh], [giaBan], [maDM]) VALUES (N'M010', N'Cá l?ng n??ng mu?i s? ?t', NULL, CAST(240000.00 AS Decimal(18, 2)), N'DM002')
INSERT [dbo].[MonAn] ([maMon], [tenMon], [hinhAnh], [giaBan], [maDM]) VALUES (N'M011', N'L?u m?m mi?n Tây (ph?n l?n)', NULL, CAST(450000.00 AS Decimal(18, 2)), N'DM003')
INSERT [dbo].[MonAn] ([maMon], [tenMon], [hinhAnh], [giaBan], [maDM]) VALUES (N'M012', N'L?u thái h?i s?n chua cay (ph?n nh?)', NULL, CAST(320000.00 AS Decimal(18, 2)), N'DM003')
INSERT [dbo].[MonAn] ([maMon], [tenMon], [hinhAnh], [giaBan], [maDM]) VALUES (N'M013', N'L?u gà lá é', NULL, CAST(380000.00 AS Decimal(18, 2)), N'DM003')
INSERT [dbo].[MonAn] ([maMon], [tenMon], [hinhAnh], [giaBan], [maDM]) VALUES (N'M014', N'Bún t??i ?n kèm l?u', NULL, CAST(20000.00 AS Decimal(18, 2)), N'DM003')
INSERT [dbo].[MonAn] ([maMon], [tenMon], [hinhAnh], [giaBan], [maDM]) VALUES (N'M015', N'Rau mu?ng xào t?i', NULL, CAST(50000.00 AS Decimal(18, 2)), N'DM004')
INSERT [dbo].[MonAn] ([maMon], [tenMon], [hinhAnh], [giaBan], [maDM]) VALUES (N'M016', N'M?c t??i xào sa t?', NULL, CAST(160000.00 AS Decimal(18, 2)), N'DM004')
INSERT [dbo].[MonAn] ([maMon], [tenMon], [hinhAnh], [giaBan], [maDM]) VALUES (N'M017', N'Th?t bò xào bông c?i', NULL, CAST(175000.00 AS Decimal(18, 2)), N'DM004')
INSERT [dbo].[MonAn] ([maMon], [tenMon], [hinhAnh], [giaBan], [maDM]) VALUES (N'M018', N'Gà h?p lá chanh', NULL, CAST(220000.00 AS Decimal(18, 2)), N'DM004')
INSERT [dbo].[MonAn] ([maMon], [tenMon], [hinhAnh], [giaBan], [maDM]) VALUES (N'M019', N'C?m chiên h?i s?n', NULL, CAST(95000.00 AS Decimal(18, 2)), N'DM005')
INSERT [dbo].[MonAn] ([maMon], [tenMon], [hinhAnh], [giaBan], [maDM]) VALUES (N'M020', N'Cá kèo chiên giòn', NULL, CAST(130000.00 AS Decimal(18, 2)), N'DM005')
INSERT [dbo].[MonAn] ([maMon], [tenMon], [hinhAnh], [giaBan], [maDM]) VALUES (N'M021', N'Tr?ng chiên th?t b?m', NULL, CAST(65000.00 AS Decimal(18, 2)), N'DM005')
INSERT [dbo].[MonAn] ([maMon], [tenMon], [hinhAnh], [giaBan], [maDM]) VALUES (N'M022', N'Khoai tây chiên', NULL, CAST(45000.00 AS Decimal(18, 2)), N'DM005')
INSERT [dbo].[MonAn] ([maMon], [tenMon], [hinhAnh], [giaBan], [maDM]) VALUES (N'M023', N'Bún bò Hu? ??c bi?t', NULL, CAST(80000.00 AS Decimal(18, 2)), N'DM006')
INSERT [dbo].[MonAn] ([maMon], [tenMon], [hinhAnh], [giaBan], [maDM]) VALUES (N'M024', N'Ph? bò tái n?m g?u', NULL, CAST(75000.00 AS Decimal(18, 2)), N'DM006')
INSERT [dbo].[MonAn] ([maMon], [tenMon], [hinhAnh], [giaBan], [maDM]) VALUES (N'M025', N'Cá lóc ??ng kho t?', NULL, CAST(190000.00 AS Decimal(18, 2)), N'DM006')
INSERT [dbo].[MonAn] ([maMon], [tenMon], [hinhAnh], [giaBan], [maDM]) VALUES (N'M026', N'N??c su?i Aquafina (chai)', NULL, CAST(15000.00 AS Decimal(18, 2)), N'DM007')
INSERT [dbo].[MonAn] ([maMon], [tenMon], [hinhAnh], [giaBan], [maDM]) VALUES (N'M027', N'Bia Saigon Special (lon)', NULL, CAST(25000.00 AS Decimal(18, 2)), N'DM007')
INSERT [dbo].[MonAn] ([maMon], [tenMon], [hinhAnh], [giaBan], [maDM]) VALUES (N'M028', N'Trà ?á (ly)', NULL, CAST(5000.00 AS Decimal(18, 2)), N'DM007')
GO
INSERT [dbo].[NhanVien] ([maNV], [tenNV], [soDT], [email], [ngaySinh], [diaChi], [gioiTinh], [trangThai], [hinhAnh], [caLamYeuThich]) VALUES (N'NV001', N'Lê Công Chung', N'0901111222', NULL, CAST(N'2001-01-26' AS Date), NULL, 0, 1, NULL, N'Nguyên ngày')
INSERT [dbo].[NhanVien] ([maNV], [tenNV], [soDT], [email], [ngaySinh], [diaChi], [gioiTinh], [trangThai], [hinhAnh], [caLamYeuThich]) VALUES (N'NV002', N'Lê Hoài Ph??c Mãi', N'0903333444', NULL, CAST(N'2002-07-20' AS Date), NULL, 0, 1, NULL, N'Sáng')
INSERT [dbo].[NhanVien] ([maNV], [tenNV], [soDT], [email], [ngaySinh], [diaChi], [gioiTinh], [trangThai], [hinhAnh], [caLamYeuThich]) VALUES (N'NV003', N'Tr?n Hoàng Nam S?n', N'0905555666', NULL, CAST(N'2000-12-05' AS Date), NULL, 0, 1, NULL, N'Chi?u')
INSERT [dbo].[NhanVien] ([maNV], [tenNV], [soDT], [email], [ngaySinh], [diaChi], [gioiTinh], [trangThai], [hinhAnh], [caLamYeuThich]) VALUES (N'NV004', N'D??ng Gia Minh', N'0377777777', NULL, CAST(N'2009-10-14' AS Date), NULL, 0, 1, NULL, N'T?i')
GO
INSERT [dbo].[TaiKhoan] ([tenDangNhap], [matKhau], [vaiTro], [maNV]) VALUES (N'NV001', N'123', N'Qu?n lý', N'NV001')
INSERT [dbo].[TaiKhoan] ([tenDangNhap], [matKhau], [vaiTro], [maNV]) VALUES (N'NV002', N'456', N'Qu?n lý', N'NV002')
INSERT [dbo].[TaiKhoan] ([tenDangNhap], [matKhau], [vaiTro], [maNV]) VALUES (N'NV003', N'123456', N'Nhân viên thu ngân', N'NV003')
INSERT [dbo].[TaiKhoan] ([tenDangNhap], [matKhau], [vaiTro], [maNV]) VALUES (N'NV004', N'123456', N'Nhân viên thu ngân', N'NV004')
GO
INSERT [dbo].[UuDai] ([maUuDai], [tenUuDai], [moTa], [giaTri], [ngayBatDau], [ngayKetThuc]) VALUES (N'UD001', N'Gi?m 10% hóa ??n thành viên Gold/Diamond', N'Gi?m 10% cho hóa ??n > 200.000 VND (Gold), ho?c 15% cho hóa ??n b?t kì (Diamond)', CAST(10.00 AS Decimal(18, 2)), CAST(N'2025-10-01' AS Date), CAST(N'2025-12-31' AS Date))
INSERT [dbo].[UuDai] ([maUuDai], [tenUuDai], [moTa], [giaTri], [ngayBatDau], [ngayKetThuc]) VALUES (N'UD002', N'T?ng món tráng mi?ng', N'Cho ??n t? 500.000? tr? lên', CAST(0.00 AS Decimal(18, 2)), CAST(N'2025-09-01' AS Date), CAST(N'2025-11-30' AS Date))
GO

-- -----------------------------------------------------
-- D? LI?U M?I (?Ã S?A THEO YÊU C?U: Thêm ViDienTu, b?t NULL)
-- -----------------------------------------------------
INSERT [dbo].[HoaDon] ([maHD], [ngayLap], [maUuDai], [ptThanhToan], [trangThai], [gioVao], [gioRa], [maBan], [maNV], [maKH], [tienCoc]) VALUES (N'HD001', CAST(N'2025-10-20T19:30:00.000' AS DateTime), NULL, N'ViDienTu', N'DaThanhToan', CAST(N'2025-10-20T18:00:00.000' AS DateTime), CAST(N'2025-10-20T19:30:00.000' AS DateTime), N'B001', N'NV003', N'KH001', CAST(0.00 AS Decimal(18, 2)))
INSERT [dbo].[HoaDon] ([maHD], [ngayLap], [maUuDai], [ptThanhToan], [trangThai], [gioVao], [gioRa], [maBan], [maNV], [maKH], [tienCoc]) VALUES (N'HD002', CAST(N'2025-10-20T21:00:00.000' AS DateTime), N'UD001', N'NganHang', N'DaThanhToan', CAST(N'2025-10-20T19:30:00.000' AS DateTime), CAST(N'2025-10-20T21:00:00.000' AS DateTime), N'B005', N'NV004', N'KH002', CAST(0.00 AS Decimal(18, 2)))
INSERT [dbo].[HoaDon] ([maHD], [ngayLap], [maUuDai], [ptThanhToan], [trangThai], [gioVao], [gioRa], [maBan], [maNV], [maKH], [tienCoc]) VALUES (N'HD003', CAST(N'2025-10-21T12:15:00.000' AS DateTime), N'UD002', N'ViDienTu', N'DaThanhToan', CAST(N'2025-10-21T11:00:00.000' AS DateTime), CAST(N'2025-10-21T12:15:00.000' AS DateTime), N'B008', N'NV003', N'KH003', CAST(0.00 AS Decimal(18, 2)))
INSERT [dbo].[HoaDon] ([maHD], [ngayLap], [maUuDai], [ptThanhToan], [trangThai], [gioVao], [gioRa], [maBan], [maNV], [maKH], [tienCoc]) VALUES (N'HD004', CAST(N'2025-10-23T20:00:00.000' AS DateTime), NULL, N'TienMat', N'DaThanhToan', CAST(N'2025-10-23T18:00:00.000' AS DateTime), CAST(N'2025-10-23T20:00:00.000' AS DateTime), N'B031', N'NV001', N'KH004', CAST(500000.00 AS Decimal(18, 2)))
INSERT [dbo].[HoaDon] ([maHD], [ngayLap], [maUuDai], [ptThanhToan], [trangThai], [gioVao], [gioRa], [maBan], [maNV], [maKH], [tienCoc]) VALUES (N'HD005', CAST(N'2025-10-21T20:45:00.000' AS DateTime), N'UD002', N'ViDienTu', N'DaThanhToan', CAST(N'2025-10-21T19:00:00.000' AS DateTime), CAST(N'2025-10-21T20:45:00.000' AS DateTime), N'B010', N'NV004', N'KH005', CAST(0.00 AS Decimal(18, 2)))
INSERT [dbo].[HoaDon] ([maHD], [ngayLap], [maUuDai], [ptThanhToan], [trangThai], [gioVao], [gioRa], [maBan], [maNV], [maKH], [tienCoc]) VALUES (N'HD006', CAST(N'2025-10-22T13:00:00.000' AS DateTime), N'UD001', N'TienMat', N'DaThanhToan', CAST(N'2025-10-22T12:00:00.000' AS DateTime), CAST(N'2025-10-22T13:00:00.000' AS DateTime), N'B002', N'NV003', N'KH001', CAST(0.00 AS Decimal(18, 2)))
INSERT [dbo].[HoaDon] ([maHD], [ngayLap], [maUuDai], [ptThanhToan], [trangThai], [gioVao], [gioRa], [maBan], [maNV], [maKH], [tienCoc]) VALUES (N'HD007', CAST(N'2025-10-22T14:30:00.000' AS DateTime), NULL, N'NganHang', N'DaThanhToan', CAST(N'2025-10-22T13:00:00.000' AS DateTime), CAST(N'2025-10-22T14:30:00.000' AS DateTime), N'B021', N'NV003', N'KH002', CAST(0.00 AS Decimal(18, 2)))
INSERT [dbo].[HoaDon] ([maHD], [ngayLap], [maUuDai], [ptThanhToan], [trangThai], [gioVao], [gioRa], [maBan], [maNV], [maKH], [tienCoc]) VALUES (N'HD008', CAST(N'2025-10-24T21:30:00.000' AS DateTime), NULL, N'NganHang', N'DaThanhToan', CAST(N'2025-10-24T19:00:00.000' AS DateTime), CAST(N'2025-10-24T21:30:00.000' AS DateTime), N'B032', N'NV001', N'KH003', CAST(1000000.00 AS Decimal(18, 2)))
INSERT [dbo].[HoaDon] ([maHD], [ngayLap], [maUuDai], [ptThanhToan], [trangThai], [gioVao], [gioRa], [maBan], [maNV], [maKH], [tienCoc]) VALUES (N'HD009', CAST(N'2025-10-22T15:30:00.000' AS DateTime), N'UD001', N'TienMat', N'DaThanhToan', CAST(N'2025-10-22T14:00:00.000' AS DateTime), CAST(N'2025-10-22T15:30:00.000' AS DateTime), N'B003', N'NV004', N'KH004', CAST(0.00 AS Decimal(18, 2)))
INSERT [dbo].[HoaDon] ([maHD], [ngayLap], [maUuDai], [ptThanhToan], [trangThai], [gioVao], [gioRa], [maBan], [maNV], [maKH], [tienCoc]) VALUES (N'HD010', CAST(N'2025-10-22T20:00:00.000' AS DateTime), NULL, N'TienMat', N'DaThanhToan', CAST(N'2025-10-22T19:00:00.000' AS DateTime), CAST(N'2025-10-22T20:00:00.000' AS DateTime), N'B004', N'NV001', N'KH001', CAST(0.00 AS Decimal(18, 2)))
GO

-- ---------------------------------
-- D? LI?U (CHI TI?T HÓA ??N)
-- ---------------------------------
INSERT [dbo].[ChiTietHoaDon] ([maHD], [maMon], [soLuong], [thanhTien]) VALUES (N'HD001', N'M001', 1, CAST(120000.00 AS Decimal(18, 2)))
INSERT [dbo].[ChiTietHoaDon] ([maHD], [maMon], [soLuong], [thanhTien]) VALUES (N'HD001', N'M015', 2, CAST(100000.00 AS Decimal(18, 2)))
INSERT [dbo].[ChiTietHoaDon] ([maHD], [maMon], [soLuong], [thanhTien]) VALUES (N'HD001', N'M027', 4, CAST(100000.00 AS Decimal(18, 2)))
INSERT [dbo].[ChiTietHoaDon] ([maHD], [maMon], [soLuong], [thanhTien]) VALUES (N'HD001', N'M028', 2, CAST(10000.00 AS Decimal(18, 2)))
INSERT [dbo].[ChiTietHoaDon] ([maHD], [maMon], [soLuong], [thanhTien]) VALUES (N'HD002', N'M006', 1, CAST(350000.00 AS Decimal(18, 2)))
INSERT [dbo].[ChiTietHoaDon] ([maHD], [maMon], [soLuong], [thanhTien]) VALUES (N'HD002', N'M019', 1, CAST(95000.00 AS Decimal(18, 2)))
INSERT [dbo].[ChiTietHoaDon] ([maHD], [maMon], [soLuong], [thanhTien]) VALUES (N'HD002', N'M027', 2, CAST(50000.00 AS Decimal(18, 2)))
INSERT [dbo].[ChiTietHoaDon] ([maHD], [maMon], [soLuong], [thanhTien]) VALUES (N'HD003', N'M002', 2, CAST(170000.00 AS Decimal(18, 2)))
INSERT [dbo].[ChiTietHoaDon] ([maHD], [maMon], [soLuong], [thanhTien]) VALUES (N'HD003', N'M028', 3, CAST(15000.00 AS Decimal(18, 2)))
INSERT [dbo].[ChiTietHoaDon] ([maHD], [maMon], [soLuong], [thanhTien]) VALUES (N'HD004', N'M006', 2, CAST(700000.00 AS Decimal(18, 2)))
INSERT [dbo].[ChiTietHoaDon] ([maHD], [maMon], [soLuong], [thanhTien]) VALUES (N'HD004', N'M011', 1, CAST(450000.00 AS Decimal(18, 2)))
INSERT [dbo].[ChiTietHoaDon] ([maHD], [maMon], [soLuong], [thanhTien]) VALUES (N'HD005', N'M012', 1, CAST(320000.00 AS Decimal(18, 2)))
INSERT [dbo].[ChiTietHoaDon] ([maHD], [maMon], [soLuong], [thanhTien]) VALUES (N'HD005', N'M015', 1, CAST(50000.00 AS Decimal(18, 2)))
INSERT [dbo].[ChiTietHoaDon] ([maHD], [maMon], [soLuong], [thanhTien]) VALUES (N'HD005', N'M019', 1, CAST(95000.00 AS Decimal(18, 2)))
INSERT [dbo].[ChiTietHoaDon] ([maHD], [maMon], [soLuong], [thanhTien]) VALUES (N'HD005', N'M027', 5, CAST(125000.00 AS Decimal(18, 2)))
INSERT [dbo].[ChiTietHoaDon] ([maHD], [maMon], [soLuong], [thanhTien]) VALUES (N'HD006', N'M001', 1, CAST(120000.00 AS Decimal(18, 2)))
INSERT [dbo].[ChiTietHoaDon] ([maHD], [maMon], [soLuong], [thanhTien]) VALUES (N'HD006', N'M028', 2, CAST(10000.00 AS Decimal(18, 2)))
INSERT [dbo].[ChiTietHoaDon] ([maHD], [maMon], [soLuong], [thanhTien]) VALUES (N'HD007', N'M006', 2, CAST(700000.00 AS Decimal(18, 2)))
INSERT [dbo].[ChiTietHoaDon] ([maHD], [maMon], [soLuong], [thanhTien]) VALUES (N'HD007', N'M015', 2, CAST(100000.00 AS Decimal(18, 2)))
INSERT [dbo].[ChiTietHoaDon] ([maHD], [maMon], [soLuong], [thanhTien]) VALUES (N'HD007', N'M022', 1, CAST(45000.00 AS Decimal(18, 2)))
INSERT [dbo].[ChiTietHoaDon] ([maHD], [maMon], [soLuong], [thanhTien]) VALUES (N'HD007', N'M027', 8, CAST(200000.00 AS Decimal(18, 2)))
INSERT [dbo].[ChiTietHoaDon] ([maHD], [maMon], [soLuong], [thanhTien]) VALUES (N'HD008', N'M009', 2, CAST(560000.00 AS Decimal(18, 2)))
INSERT [dbo].[ChiTietHoaDon] ([maHD], [maMon], [soLuong], [thanhTien]) VALUES (N'HD008', N'M010', 1, CAST(240000.00 AS Decimal(18, 2)))
INSERT [dbo].[ChiTietHoaDon] ([maHD], [maMon], [soLuong], [thanhTien]) VALUES (N'HD008', N'M027', 10, CAST(250000.00 AS Decimal(18, 2)))
INSERT [dbo].[ChiTietHoaDon] ([maHD], [maMon], [soLuong], [thanhTien]) VALUES (N'HD009', N'M002', 1, CAST(85000.00 AS Decimal(18, 2)))
INSERT [dbo].[ChiTietHoaDon] ([maHD], [maMon], [soLuong], [thanhTien]) VALUES (N'HD009', N'M003', 2, CAST(120000.00 AS Decimal(18, 2)))
INSERT [dbo].[ChiTietHoaDon] ([maHD], [maMon], [soLuong], [thanhTien]) VALUES (N'HD009', N'M019', 1, CAST(95000.00 AS Decimal(18, 2)))
INSERT [dbo].[ChiTietHoaDon] ([maHD], [maMon], [soLuong], [thanhTien]) VALUES (N'HD010', N'M023', 2, CAST(160000.00 AS Decimal(18, 2)))
INSERT [dbo].[ChiTietHoaDon] ([maHD], [maMon], [soLuong], [thanhTien]) VALUES (N'HD010', N'M028', 2, CAST(10000.00 AS Decimal(18, 2)))
GO

-- ---------------------------------
-- D? LI?U B? SUNG (3 HÓA ??N M?I)
-- ---------------------------------

-- Hóa ??n 11: Bàn T?ng tr?t (B006)
INSERT [dbo].[HoaDon] ([maHD], [ngayLap], [maUuDai], [ptThanhToan], [trangThai], [gioVao], [gioRa], [maBan], [maNV], [maKH], [tienCoc]) VALUES (N'HD011', CAST(N'2025-10-25T10:30:00.000' AS DateTime), NULL, N'TienMat', N'DaThanhToan', CAST(N'2025-10-25T09:00:00.000' AS DateTime), CAST(N'2025-10-25T10:30:00.000' AS DateTime), N'B006', N'NV002', N'KH005', CAST(0.00 AS Decimal(18, 2)))
INSERT [dbo].[ChiTietHoaDon] ([maHD], [maMon], [soLuong], [thanhTien]) VALUES (N'HD011', N'M024', 2, CAST(150000.00 AS Decimal(18, 2))) -- Ph? bò
INSERT [dbo].[ChiTietHoaDon] ([maHD], [maMon], [soLuong], [thanhTien]) VALUES (N'HD011', N'M028', 4, CAST(20000.00 AS Decimal(18, 2))) -- Trà ?á

-- Hóa ??n 12: Bàn T?ng 1 (B022)
INSERT [dbo].[HoaDon] ([maHD], [ngayLap], [maUuDai], [ptThanhToan], [trangThai], [gioVao], [gioRa], [maBan], [maNV], [maKH], [tienCoc]) VALUES (N'HD012', CAST(N'2025-10-25T15:00:00.000' AS DateTime), N'UD002', N'NganHang', N'DaThanhToan', CAST(N'2025-10-25T13:30:00.000' AS DateTime), CAST(N'2025-10-25T15:00:00.000' AS DateTime), N'B022', N'NV004', N'KH006', CAST(0.00 AS Decimal(18, 2)))
INSERT [dbo].[ChiTietHoaDon] ([maHD], [maMon], [soLuong], [thanhTien]) VALUES (N'HD012', N'M007', 1, CAST(180000.00 AS Decimal(18, 2))) -- Heo n??ng ri?ng m?
INSERT [dbo].[ChiTietHoaDon] ([maHD], [maMon], [soLuong], [thanhTien]) VALUES (N'HD012', N'M015', 1, CAST(50000.00 AS Decimal(18, 2))) -- Rau mu?ng xào t?i
INSERT [dbo].[ChiTietHoaDon] ([maHD], [maMon], [soLuong], [thanhTien]) VALUES (N'HD012', N'M027', 3, CAST(75000.00 AS Decimal(18, 2))) -- Bia Saigon

-- Hóa ??n 13: Bàn Phòng (B033)
INSERT [dbo].[HoaDon] ([maHD], [ngayLap], [maUuDai], [ptThanhToan], [trangThai], [gioVao], [gioRa], [maBan], [maNV], [maKH], [tienCoc]) VALUES (N'HD013', CAST(N'2025-10-25T21:45:00.000' AS DateTime), N'UD001', N'ViDienTu', N'DaThanhToan', CAST(N'2025-10-25T19:15:00.000' AS DateTime), CAST(N'2025-10-25T21:45:00.000' AS DateTime), N'B033', N'NV001', N'KH003', CAST(500000.00 AS Decimal(18, 2)))
INSERT [dbo].[ChiTietHoaDon] ([maHD], [maMon], [soLuong], [thanhTien]) VALUES (N'HD013', N'M011', 1, CAST(450000.00 AS Decimal(18, 2))) -- L?u m?m
INSERT [dbo].[ChiTietHoaDon] ([maHD], [maMon], [soLuong], [thanhTien]) VALUES (N'HD013', N'M014', 2, CAST(40000.00 AS Decimal(18, 2))) -- Bún ?n kèm
INSERT [dbo].[ChiTietHoaDon] ([maHD], [maMon], [soLuong], [thanhTien]) VALUES (N'HD013', N'M009', 1, CAST(280000.00 AS Decimal(18, 2))) -- Tôm sú n??ng
INSERT [dbo].[ChiTietHoaDon] ([maHD], [maMon], [soLuong], [thanhTien]) VALUES (N'HD013', N'M027', 6, CAST(150000.00 AS Decimal(18, 2))) -- Bia Saigon
GO

-- ----------------------------
-- T?O CÁC RÀNG BU?C KHÓA NGO?I
-- ----------------------------
ALTER TABLE [dbo].[CaTruc]  WITH CHECK ADD  CONSTRAINT [FK_CaTruc_NhanVien] FOREIGN KEY([maNV])
REFERENCES [dbo].[NhanVien] ([maNV])
GO
ALTER TABLE [dbo].[CaTruc] CHECK CONSTRAINT [FK_CaTruc_NhanVien]
GO
ALTER TABLE [dbo].[ChiTietHoaDon]  WITH CHECK ADD  CONSTRAINT [FK_CTHD_HoaDon] FOREIGN KEY([maHD])
REFERENCES [dbo].[HoaDon] ([maHD])
GO
ALTER TABLE [dbo].[ChiTietHoaDon] CHECK CONSTRAINT [FK_CTHD_HoaDon]
GO
ALTER TABLE [dbo].[ChiTietHoaDon]  WITH CHECK ADD  CONSTRAINT [FK_CTHD_MonAn] FOREIGN KEY([maMon])
REFERENCES [dbo].[MonAn] ([maMon])
GO
ALTER TABLE [dbo].[ChiTietHoaDon] CHECK CONSTRAINT [FK_CTHD_MonAn]
GO
ALTER TABLE [dbo].[HoaDon]  WITH CHECK ADD  CONSTRAINT [FK_HoaDon_Ban] FOREIGN KEY([maBan])
REFERENCES [dbo].[Ban] ([maBan])
GO
ALTER TABLE [dbo].[HoaDon] CHECK CONSTRAINT [FK_HoaDon_Ban]
GO
ALTER TABLE [dbo].[HoaDon]  WITH CHECK ADD  CONSTRAINT [FK_HoaDon_KhachHang] FOREIGN KEY([maKH])
REFERENCES [dbo].[KhachHang] ([maKH])
GO
ALTER TABLE [dbo].[HoaDon] CHECK CONSTRAINT [FK_HoaDon_KhachHang]
GO
ALTER TABLE [dbo].[HoaDon]  WITH CHECK ADD  CONSTRAINT [FK_HoaDon_NhanVien] FOREIGN KEY([maNV])
REFERENCES [dbo].[NhanVien] ([maNV])
GO
ALTER TABLE [dbo].[HoaDon] CHECK CONSTRAINT [FK_HoaDon_NhanVien]
GO
ALTER TABLE [dbo].[HoaDon]  WITH CHECK ADD  CONSTRAINT [FK_HoaDon_UuDai] FOREIGN KEY([maUuDai])
REFERENCES [dbo].[UuDai] ([maUuDai])
GO
ALTER TABLE [dbo].[HoaDon] CHECK CONSTRAINT [FK_HoaDon_UuDai]
GO
ALTER TABLE [dbo].[MonAn]  WITH CHECK ADD  CONSTRAINT [FK_MonAn_DanhMuc] FOREIGN KEY([maDM])
REFERENCES [dbo].[DanhMucMon] ([maDM])
GO
ALTER TABLE [Dbo].[MonAn] CHECK CONSTRAINT [FK_MonAn_DanhMuc]
GO
ALTER TABLE [dbo].[TaiKhoan]  WITH CHECK ADD  CONSTRAINT [FK_TaiKhoan_NhanVien] FOREIGN KEY([maNV])
REFERENCES [dbo].[NhanVien] ([maNV])
GO
ALTER TABLE [dbo].[TaiKhoan] CHECK CONSTRAINT [FK_TaiKhoan_NhanVien]
GO
USE [master]
GO
ALTER DATABASE [NhaHang] SET  READ_WRITE
GO
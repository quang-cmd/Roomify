-- SQL Server script duoc sinh tu file UML Class_Diagram_QLyKhachSan.vpp
-- Ghi chu: script da chuan hoa mot so diem de phu hop CSDL quan he,
-- vi du cac thuoc tinh doi tuong/list trong UML duoc chuyen thanh khoa ngoai va bang chi tiet.
SET NOCOUNT ON;

IF DB_ID(N'QuanLyKhachSan') IS NULL CREATE DATABASE QuanLyKhachSan;
GO
USE QuanLyKhachSan;
GO

-- Xoa bang theo thu tu phu thuoc
IF OBJECT_ID('dbo.ThanhToan', 'U') IS NOT NULL DROP TABLE dbo.ThanhToan;
IF OBJECT_ID('dbo.ChiTietDichVu', 'U') IS NOT NULL DROP TABLE dbo.ChiTietDichVu;
IF OBJECT_ID('dbo.ChiTietHoaDon', 'U') IS NOT NULL DROP TABLE dbo.ChiTietHoaDon;
IF OBJECT_ID('dbo.HoaDon', 'U') IS NOT NULL DROP TABLE dbo.HoaDon;
IF OBJECT_ID('dbo.ChiTietDatPhong', 'U') IS NOT NULL DROP TABLE dbo.ChiTietDatPhong;
IF OBJECT_ID('dbo.DatPhong', 'U') IS NOT NULL DROP TABLE dbo.DatPhong;
IF OBJECT_ID('dbo.PhanCongCa', 'U') IS NOT NULL DROP TABLE dbo.PhanCongCa;
IF OBJECT_ID('dbo.Phong', 'U') IS NOT NULL DROP TABLE dbo.Phong;
IF OBJECT_ID('dbo.LoaiPhong', 'U') IS NOT NULL DROP TABLE dbo.LoaiPhong;
IF OBJECT_ID('dbo.KhuyenMai', 'U') IS NOT NULL DROP TABLE dbo.KhuyenMai;
IF OBJECT_ID('dbo.DichVu', 'U') IS NOT NULL DROP TABLE dbo.DichVu;
IF OBJECT_ID('dbo.CaLam', 'U') IS NOT NULL DROP TABLE dbo.CaLam;
IF OBJECT_ID('dbo.KhachHang', 'U') IS NOT NULL DROP TABLE dbo.KhachHang;
IF OBJECT_ID('dbo.NhanVien', 'U') IS NOT NULL DROP TABLE dbo.NhanVien;
IF OBJECT_ID('dbo.TaiKhoan', 'U') IS NOT NULL DROP TABLE dbo.TaiKhoan;
GO

CREATE TABLE TaiKhoan (
    tenDangNhap VARCHAR(50) PRIMARY KEY,
    matKhau NVARCHAR(255) NOT NULL,
    vaiTro VARCHAR(20) NOT NULL CHECK (vaiTro IN ('QuanLy', 'NhanVien')),
    trangThaiTK VARCHAR(20) NOT NULL CHECK (trangThaiTK IN ('DangHoatDong', 'NgungHoatDong'))
);
GO

CREATE TABLE NhanVien (
    maNV CHAR(5) PRIMARY KEY,
    hoTenNV NVARCHAR(100) NOT NULL,
    sdt VARCHAR(15) NOT NULL UNIQUE,
    gioiTinh BIT NOT NULL,
    luong DECIMAL(18,2) NOT NULL CHECK (luong >= 0),
    ngayVao DATETIME2 NOT NULL,
    tenDangNhap VARCHAR(50) NOT NULL UNIQUE,
    CONSTRAINT FK_NhanVien_TaiKhoan FOREIGN KEY (tenDangNhap) REFERENCES TaiKhoan(tenDangNhap)
);
GO

CREATE TABLE KhachHang (
    maKH CHAR(5) PRIMARY KEY,
    hoTenKH NVARCHAR(100) NOT NULL,
    gioiTinh BIT NOT NULL,
    ngaySinh DATETIME2 NOT NULL,
    email VARCHAR(100) NULL UNIQUE,
    sdt VARCHAR(15) NOT NULL UNIQUE,
    CCCD VARCHAR(20) NOT NULL UNIQUE,
    quocTich NVARCHAR(50) NOT NULL,
    diaChi NVARCHAR(200) NULL,
    hangKH VARCHAR(20) NOT NULL CHECK (hangKH IN ('Dong', 'Bac', 'Vang', 'KimCuong')),
    diemTichLuy INT NOT NULL DEFAULT 0 CHECK (diemTichLuy >= 0)
);
GO

CREATE TABLE CaLam (
    maCa CHAR(5) PRIMARY KEY,
    gioBatDau TIME NOT NULL,
    gioKetThuc TIME NOT NULL,
    ghiChu NVARCHAR(200) NULL,
    loaiCa VARCHAR(20) NOT NULL CHECK (loaiCa IN ('CaSang', 'CaChieu', 'CaToi'))
);
GO

CREATE TABLE PhanCongCa (
    maPC CHAR(5) PRIMARY KEY,
    ngay DATE NOT NULL,
    tienMoCa DECIMAL(18,2) NOT NULL DEFAULT 0 CHECK (tienMoCa >= 0),
    tienKetCa DECIMAL(18,2) NOT NULL DEFAULT 0 CHECK (tienKetCa >= 0),
    maNV CHAR(5) NOT NULL,
    maCa CHAR(5) NOT NULL,
    CONSTRAINT FK_PhanCongCa_NhanVien FOREIGN KEY (maNV) REFERENCES NhanVien(maNV),
    CONSTRAINT FK_PhanCongCa_CaLam FOREIGN KEY (maCa) REFERENCES CaLam(maCa)
);
GO

CREATE TABLE LoaiPhong (
    maLoaiPhong CHAR(5) PRIMARY KEY,
    tenLoaiPhong NVARCHAR(100) NOT NULL,
    soLuongPhong INT NOT NULL CHECK (soLuongPhong >= 0),
    giaPhong DECIMAL(18,2) NOT NULL CHECK (giaPhong >= 0),
    sucChuaToiDa INT NOT NULL CHECK (sucChuaToiDa > 0),
    dienTich DECIMAL(10,2) NOT NULL CHECK (dienTich > 0),
    moTa NVARCHAR(255) NULL,
    tienNghi NVARCHAR(255) NULL
);
GO

CREATE TABLE Phong (
    maPhong CHAR(4) PRIMARY KEY,
    tienCoc DECIMAL(18,2) NOT NULL DEFAULT 0 CHECK (tienCoc >= 0),
    maLoaiPhong CHAR(5) NOT NULL,
    tang INT NOT NULL CHECK (tang > 0),
    trangThaiPhong VARCHAR(20) NOT NULL CHECK (trangThaiPhong IN ('DaDat', 'Trong', 'BaoTri', 'DangDon')),
    CONSTRAINT FK_Phong_LoaiPhong FOREIGN KEY (maLoaiPhong) REFERENCES LoaiPhong(maLoaiPhong)
);
GO

CREATE TABLE DichVu (
    maDV CHAR(5) PRIMARY KEY,
    tenDV NVARCHAR(100) NOT NULL,
    donGia DECIMAL(18,2) NOT NULL CHECK (donGia >= 0),
    moTaDV NVARCHAR(255) NULL,
    trangThaiDV VARCHAR(20) NOT NULL CHECK (trangThaiDV IN ('DangHoatDong', 'NgungHoatDong'))
);
GO

CREATE TABLE KhuyenMai (
    maKM CHAR(5) PRIMARY KEY,
    tenKM NVARCHAR(100) NOT NULL,
    dieuKienApDung NVARCHAR(255) NULL,
    loaiKM NVARCHAR(50) NOT NULL,
    giaTriToiDa DECIMAL(18,2) NOT NULL CHECK (giaTriToiDa >= 0),
    tienKhuyenMai DECIMAL(18,2) NOT NULL CHECK (tienKhuyenMai >= 0),
    ngayBatDau DATETIME2 NOT NULL,
    ngayKetThuc DATETIME2 NOT NULL,
    trangThaiKM VARCHAR(20) NOT NULL CHECK (trangThaiKM IN ('SapDienRa', 'DangHoatDong', 'HetHan')),
    CONSTRAINT CK_KhuyenMai_Ngay CHECK (ngayKetThuc > ngayBatDau)
);
GO

CREATE TABLE DatPhong (
    maDatPhong CHAR(5) PRIMARY KEY,
    ngayDat DATETIME2 NOT NULL,
    ngayNhanDuKien DATETIME2 NOT NULL,
    ngayTraDuKien DATETIME2 NOT NULL,
    tienCoc DECIMAL(18,2) NOT NULL DEFAULT 0 CHECK (tienCoc >= 0),
    ghiChu NVARCHAR(255) NULL,
    maKH CHAR(5) NOT NULL,
    maNV CHAR(5) NOT NULL,
    CONSTRAINT FK_DatPhong_KhachHang FOREIGN KEY (maKH) REFERENCES KhachHang(maKH),
    CONSTRAINT FK_DatPhong_NhanVien FOREIGN KEY (maNV) REFERENCES NhanVien(maNV),
    CONSTRAINT CK_DatPhong_Ngay CHECK (ngayTraDuKien > ngayNhanDuKien AND ngayNhanDuKien >= ngayDat)
);
GO

CREATE TABLE ChiTietDatPhong (
    maCTDP CHAR(7) PRIMARY KEY,
    maDatPhong CHAR(5) NOT NULL,
    maPhong CHAR(4) NOT NULL,
    ngayNhanDuKien DATETIME2 NOT NULL,
    ngayTraDuKien DATETIME2 NOT NULL,
    donGiaDat DECIMAL(18,2) NOT NULL CHECK (donGiaDat >= 0),
    soLuongNguoiO INT NOT NULL CHECK (soLuongNguoiO > 0),
    ghiChu NVARCHAR(255) NULL,
    CONSTRAINT FK_CTDatPhong_DatPhong FOREIGN KEY (maDatPhong) REFERENCES DatPhong(maDatPhong),
    CONSTRAINT FK_CTDatPhong_Phong FOREIGN KEY (maPhong) REFERENCES Phong(maPhong),
    CONSTRAINT CK_CTDatPhong_Ngay CHECK (ngayTraDuKien > ngayNhanDuKien)
);
GO

CREATE TABLE HoaDon (
    maHD CHAR(5) PRIMARY KEY,
    ngayLapHD DATETIME2 NOT NULL,
    ngayThanhToan DATETIME2 NULL,
    ghiChu NVARCHAR(255) NULL,
    soLuongNguoiO INT NOT NULL CHECK (soLuongNguoiO > 0),
    tienPhong DECIMAL(18,2) NOT NULL DEFAULT 0 CHECK (tienPhong >= 0),
    tienDichVu DECIMAL(18,2) NOT NULL DEFAULT 0 CHECK (tienDichVu >= 0),
    tienKhuyenMai DECIMAL(18,2) NOT NULL DEFAULT 0 CHECK (tienKhuyenMai >= 0),
    tienThue DECIMAL(18,2) NOT NULL DEFAULT 0 CHECK (tienThue >= 0),
    tongTienThanhToan DECIMAL(18,2) NOT NULL DEFAULT 0 CHECK (tongTienThanhToan >= 0),
    phiDoiPhong DECIMAL(18,2) NOT NULL DEFAULT 0 CHECK (phiDoiPhong >= 0),
    maKM CHAR(5) NULL,
    maKH CHAR(5) NOT NULL,
    maNV CHAR(5) NOT NULL,
    phuongThucTT VARCHAR(20) NOT NULL CHECK (phuongThucTT IN ('TienMat', 'ChuyenKhoan')),
    trangThai VARCHAR(20) NOT NULL CHECK (trangThai IN ('ChuaThanhToan', 'DaThanhToan', 'DaHuy')),
    maDatPhong CHAR(5) NULL,
    CONSTRAINT FK_HoaDon_KhuyenMai FOREIGN KEY (maKM) REFERENCES KhuyenMai(maKM),
    CONSTRAINT FK_HoaDon_KhachHang FOREIGN KEY (maKH) REFERENCES KhachHang(maKH),
    CONSTRAINT FK_HoaDon_NhanVien FOREIGN KEY (maNV) REFERENCES NhanVien(maNV),
    CONSTRAINT FK_HoaDon_DatPhong FOREIGN KEY (maDatPhong) REFERENCES DatPhong(maDatPhong)
);
GO

CREATE TABLE ChiTietHoaDon (
    maCTHD CHAR(8) PRIMARY KEY,
    maHD CHAR(5) NOT NULL,
    maPhong CHAR(4) NOT NULL,
    ngayNhanPhong DATETIME2 NOT NULL,
    ngayTraPhong DATETIME2 NOT NULL,
    soDem INT NOT NULL CHECK (soDem > 0),
    phuThu DECIMAL(18,2) NOT NULL DEFAULT 0 CHECK (phuThu >= 0),
    thanhTien DECIMAL(18,2) NOT NULL CHECK (thanhTien >= 0),
    CONSTRAINT FK_CTHoaDon_HoaDon FOREIGN KEY (maHD) REFERENCES HoaDon(maHD),
    CONSTRAINT FK_CTHoaDon_Phong FOREIGN KEY (maPhong) REFERENCES Phong(maPhong),
    CONSTRAINT CK_CTHoaDon_Ngay CHECK (ngayTraPhong > ngayNhanPhong)
);
GO

CREATE TABLE ChiTietDichVu (
    maCTDV CHAR(8) PRIMARY KEY,
    maHD CHAR(5) NOT NULL,
    maDV CHAR(5) NOT NULL,
    soLuong INT NOT NULL CHECK (soLuong > 0),
    donGia DECIMAL(18,2) NOT NULL CHECK (donGia >= 0),
    thanhTien DECIMAL(18,2) NOT NULL CHECK (thanhTien >= 0),
    ghiChu NVARCHAR(255) NULL,
    CONSTRAINT FK_CTDichVu_HoaDon FOREIGN KEY (maHD) REFERENCES HoaDon(maHD),
    CONSTRAINT FK_CTDichVu_DichVu FOREIGN KEY (maDV) REFERENCES DichVu(maDV)
);
GO

CREATE TABLE ThanhToan (
    maTT CHAR(5) PRIMARY KEY,
    ngayTT DATETIME2 NOT NULL,
    soTienTT DECIMAL(18,2) NOT NULL CHECK (soTienTT >= 0),
    ghiChu NVARCHAR(255) NULL,
    phuongThucTT VARCHAR(20) NOT NULL CHECK (phuongThucTT IN ('TienMat', 'ChuyenKhoan')),
    trangThaiTT VARCHAR(30) NOT NULL CHECK (trangThaiTT IN ('ChoThanhToan', 'ThanhToanThanhCong', 'ThanhToanThatBai', 'DaHuy')),
    maHD CHAR(5) NOT NULL,
    CONSTRAINT FK_ThanhToan_HoaDon FOREIGN KEY (maHD) REFERENCES HoaDon(maHD)
);
GO

-- Indexes
CREATE INDEX IX_PhanCongCa_maNV_ngay ON PhanCongCa(maNV, ngay);
CREATE INDEX IX_Phong_maLoaiPhong ON Phong(maLoaiPhong);
CREATE INDEX IX_DatPhong_maKH ON DatPhong(maKH);
CREATE INDEX IX_DatPhong_maNV ON DatPhong(maNV);
CREATE INDEX IX_CTDP_maDatPhong ON ChiTietDatPhong(maDatPhong);
CREATE INDEX IX_HoaDon_maKH ON HoaDon(maKH);
CREATE INDEX IX_HoaDon_maNV ON HoaDon(maNV);
CREATE INDEX IX_HoaDon_maDatPhong ON HoaDon(maDatPhong);
CREATE INDEX IX_CTHD_maHD ON ChiTietHoaDon(maHD);
CREATE INDEX IX_CTDV_maHD ON ChiTietDichVu(maHD);
CREATE INDEX IX_ThanhToan_maHD ON ThanhToan(maHD);
GO

-- Du lieu mau: TaiKhoan
INSERT INTO TaiKhoan (tenDangNhap, matKhau, vaiTro, trangThaiTK) VALUES
('tk001', N'Hash@001', 'QuanLy', 'DangHoatDong'),
('tk002', N'Hash@002', 'QuanLy', 'DangHoatDong'),
('tk003', N'Hash@003', 'QuanLy', 'DangHoatDong'),
('tk004', N'Hash@004', 'NhanVien', 'DangHoatDong'),
('tk005', N'Hash@005', 'NhanVien', 'DangHoatDong'),
('tk006', N'Hash@006', 'NhanVien', 'DangHoatDong'),
('tk007', N'Hash@007', 'NhanVien', 'DangHoatDong'),
('tk008', N'Hash@008', 'NhanVien', 'DangHoatDong'),
('tk009', N'Hash@009', 'NhanVien', 'DangHoatDong'),
('tk010', N'Hash@010', 'NhanVien', 'DangHoatDong'),
('tk011', N'Hash@011', 'NhanVien', 'DangHoatDong'),
('tk012', N'Hash@012', 'NhanVien', 'DangHoatDong'),
('tk013', N'Hash@013', 'NhanVien', 'DangHoatDong'),
('tk014', N'Hash@014', 'NhanVien', 'DangHoatDong'),
('tk015', N'Hash@015', 'NhanVien', 'NgungHoatDong');
GO

-- Du lieu mau: NhanVien
INSERT INTO NhanVien (maNV, hoTenNV, sdt, gioiTinh, luong, ngayVao, tenDangNhap) VALUES
('NV001', N'Nguyen Minh Anh', '0910000001', 1, 12450000, '2023-02-05 00:00:00', 'tk001'),
('NV002', N'Tran Quoc Bao', '0910000002', 0, 12900000, '2023-03-12 00:00:00', 'tk002'),
('NV003', N'Le Thu Ha', '0910000003', 1, 13350000, '2023-04-16 00:00:00', 'tk003'),
('NV004', N'Pham Gia Huy', '0910000004', 0, 8500000, '2023-05-21 00:00:00', 'tk004'),
('NV005', N'Do Khanh Linh', '0910000005', 1, 8750000, '2023-06-25 00:00:00', 'tk005'),
('NV006', N'Vo Tuan Kiet', '0910000006', 0, 9000000, '2023-07-30 00:00:00', 'tk006'),
('NV007', N'Bui Ngoc Mai', '0910000007', 1, 9250000, '2023-09-03 00:00:00', 'tk007'),
('NV008', N'Dang Hoang Nam', '0910000008', 0, 9500000, '2023-10-08 00:00:00', 'tk008'),
('NV009', N'Hoang Thi Lan', '0910000009', 1, 9750000, '2023-11-12 00:00:00', 'tk009'),
('NV010', N'Phan Duc Long', '0910000010', 0, 10000000, '2023-12-17 00:00:00', 'tk010'),
('NV011', N'Nguyen Thanh Truc', '0910000011', 1, 10250000, '2024-01-21 00:00:00', 'tk011'),
('NV012', N'Trinh Quang Huy', '0910000012', 0, 10500000, '2024-02-25 00:00:00', 'tk012'),
('NV013', N'Ly My Tien', '0910000013', 1, 10750000, '2024-03-31 00:00:00', 'tk013'),
('NV014', N'Doan Bao Chau', '0910000014', 0, 11000000, '2024-05-05 00:00:00', 'tk014'),
('NV015', N'Ngo Duc An', '0910000015', 1, 11250000, '2024-06-09 00:00:00', 'tk015');
GO

-- Du lieu mau: KhachHang
INSERT INTO KhachHang (maKH, hoTenKH, gioiTinh, ngaySinh, email, sdt, CCCD, quocTich, diaChi, hangKH, diemTichLuy) VALUES
('KH001', N'Nguyen Van An', 1, '1991-02-15 00:00:00', 'kh001@example.com', '0820000001', '700000000001', N'Viet Nam', N'Ha Noi', 'Dong', 0),
('KH002', N'Tran Thi Bich', 0, '1992-03-31 00:00:00', 'kh002@example.com', '0820000002', '700000000002', N'Viet Nam', N'Da Nang', 'Dong', 120),
('KH003', N'Le Quoc Cuong', 1, '1993-05-15 00:00:00', 'kh003@example.com', '0820000003', '700000000003', N'Viet Nam', N'Can Tho', 'Dong', 240),
('KH004', N'Pham Thu Dung', 0, '1994-06-29 00:00:00', 'kh004@example.com', '0820000004', '700000000004', N'Viet Nam', N'Nha Trang', 'Dong', 360),
('KH005', N'Vo Minh Duc', 1, '1995-08-13 00:00:00', 'kh005@example.com', '0820000005', '700000000005', N'Viet Nam', N'TP HCM', 'Bac', 480),
('KH006', N'Bui Ngoc Giang', 0, '1996-09-26 00:00:00', 'kh006@example.com', '0820000006', '700000000006', N'Viet Nam', N'Ha Noi', 'Bac', 600),
('KH007', N'Dang Hoai Nam', 1, '1997-11-10 00:00:00', 'kh007@example.com', '0820000007', '700000000007', N'Viet Nam', N'Da Nang', 'Bac', 720),
('KH008', N'Hoang Yen Nhi', 0, '1998-12-25 00:00:00', 'kh008@example.com', '0820000008', '700000000008', N'Viet Nam', N'Can Tho', 'Bac', 840),
('KH009', N'Phan Tuan Khang', 1, '2000-02-08 00:00:00', 'kh009@example.com', '0820000009', '700000000009', N'Viet Nam', N'Nha Trang', 'Vang', 960),
('KH010', N'Ngo Bao Tram', 0, '2001-03-24 00:00:00', 'kh010@example.com', '0820000010', '700000000010', N'Viet Nam', N'TP HCM', 'Vang', 1080),
('KH011', N'Do Huu Phuc', 1, '2002-05-08 00:00:00', 'kh011@example.com', '0820000011', '700000000011', N'Viet Nam', N'Ha Noi', 'Vang', 1200),
('KH012', N'Truong My Linh', 0, '2003-06-22 00:00:00', 'kh012@example.com', '0820000012', '700000000012', N'Viet Nam', N'Da Nang', 'Vang', 1320),
('KH013', N'Ly Gia Han', 1, '2004-08-05 00:00:00', 'kh013@example.com', '0820000013', '700000000013', N'Han Quoc', N'Can Tho', 'KimCuong', 1440),
('KH014', N'Nguyen Quoc Viet', 0, '2005-09-19 00:00:00', 'kh014@example.com', '0820000014', '700000000014', N'Nhat Ban', N'Nha Trang', 'KimCuong', 1560),
('KH015', N'Tran Khanh Vy', 1, '2006-11-03 00:00:00', 'kh015@example.com', '0820000015', '700000000015', N'Singapore', N'TP HCM', 'KimCuong', 1680);
GO

-- Du lieu mau: CaLam
INSERT INTO CaLam (maCa, gioBatDau, gioKetThuc, ghiChu, loaiCa) VALUES
('CA001', '06:00:00', '14:00:00', N'Ca sang thuong', 'CaSang'),
('CA002', '14:00:00', '22:00:00', N'Ca chieu thuong', 'CaChieu'),
('CA003', '22:00:00', '06:00:00', N'Ca toi thuong', 'CaToi'),
('CA004', '05:30:00', '13:30:00', N'Ca sang cuoi tuan', 'CaSang'),
('CA005', '13:30:00', '21:30:00', N'Ca chieu cuoi tuan', 'CaChieu'),
('CA006', '21:30:00', '05:30:00', N'Ca toi cuoi tuan', 'CaToi'),
('CA007', '06:30:00', '14:30:00', N'Ca sang le', 'CaSang'),
('CA008', '14:30:00', '22:30:00', N'Ca chieu le', 'CaChieu'),
('CA009', '22:30:00', '06:30:00', N'Ca toi le', 'CaToi'),
('CA010', '07:00:00', '15:00:00', N'Ca hanh chinh sang', 'CaSang'),
('CA011', '15:00:00', '23:00:00', N'Ca hanh chinh chieu', 'CaChieu'),
('CA012', '23:00:00', '07:00:00', N'Ca hanh chinh toi', 'CaToi'),
('CA013', '06:00:00', '12:00:00', N'Ca ngan sang ho tro', 'CaSang'),
('CA014', '12:00:00', '18:00:00', N'Ca ngan chieu ho tro', 'CaChieu'),
('CA015', '18:00:00', '23:59:59', N'Ca ngan toi ho tro', 'CaToi');
GO

-- Du lieu mau: PhanCongCa
INSERT INTO PhanCongCa (maPC, ngay, tienMoCa, tienKetCa, maNV, maCa) VALUES
('PC001', '2026-04-01 00:00:00', 600000, 2185000, 'NV001', 'CA001'),
('PC002', '2026-04-02 00:00:00', 700000, 2370000, 'NV002', 'CA002'),
('PC003', '2026-04-03 00:00:00', 800000, 2555000, 'NV003', 'CA003'),
('PC004', '2026-04-04 00:00:00', 900000, 2740000, 'NV004', 'CA004'),
('PC005', '2026-04-05 00:00:00', 500000, 2425000, 'NV005', 'CA005'),
('PC006', '2026-04-06 00:00:00', 600000, 2610000, 'NV006', 'CA006'),
('PC007', '2026-04-07 00:00:00', 700000, 2795000, 'NV007', 'CA007'),
('PC008', '2026-04-08 00:00:00', 800000, 2980000, 'NV008', 'CA008'),
('PC009', '2026-04-09 00:00:00', 900000, 3165000, 'NV009', 'CA009'),
('PC010', '2026-04-10 00:00:00', 500000, 2850000, 'NV010', 'CA010'),
('PC011', '2026-04-11 00:00:00', 600000, 3035000, 'NV011', 'CA011'),
('PC012', '2026-04-12 00:00:00', 700000, 3220000, 'NV012', 'CA012'),
('PC013', '2026-04-13 00:00:00', 800000, 3405000, 'NV013', 'CA013'),
('PC014', '2026-04-14 00:00:00', 900000, 3590000, 'NV014', 'CA014'),
('PC015', '2026-04-15 00:00:00', 500000, 3275000, 'NV015', 'CA015');
GO

-- Du lieu mau: LoaiPhong
INSERT INTO LoaiPhong (maLoaiPhong, tenLoaiPhong, soLuongPhong, giaPhong, sucChuaToiDa, dienTich, moTa, tienNghi) VALUES
('LP001', N'Standard Don', 12, 450000, 2, 22, N'Phong tieu chuan giuong don', N'TV, May lanh, Wifi, Nuoc nong'),
('LP002', N'Standard Doi', 10, 520000, 2, 24, N'Phong tieu chuan giuong doi', N'TV, May lanh, Wifi, Tu lanh mini'),
('LP003', N'Superior Don', 8, 580000, 2, 25, N'Phong superior giuong don', N'TV, Ban lam viec, Wifi, View pho'),
('LP004', N'Superior Doi', 8, 650000, 3, 28, N'Phong superior giuong doi', N'TV, Wifi, Ban lam viec, Bon tam'),
('LP005', N'Deluxe Twin', 6, 780000, 3, 32, N'Phong deluxe 2 giuong don', N'TV, Wifi, Mini bar, View dep'),
('LP006', N'Deluxe Double', 6, 820000, 3, 34, N'Phong deluxe giuong doi', N'TV, Wifi, Mini bar, Bon tam'),
('LP007', N'Deluxe City View', 6, 860000, 3, 35, N'Phong deluxe view thanh pho', N'TV, Wifi, View thanh pho, Sofa'),
('LP008', N'Deluxe River View', 5, 920000, 3, 36, N'Phong deluxe view song', N'TV, Wifi, Sofa, Mini bar'),
('LP009', N'Family 4', 5, 1100000, 4, 42, N'Phong gia dinh 4 nguoi', N'2 giuong doi, TV, Wifi, Ban an'),
('LP010', N'Family 6', 4, 1450000, 6, 50, N'Phong gia dinh 6 nguoi', N'3 giuong doi, TV, Wifi, Ban an'),
('LP011', N'Junior Suite', 4, 1550000, 3, 48, N'Suite junior co phong khach nho', N'TV, Wifi, Sofa, Bon tam'),
('LP012', N'Executive Suite', 3, 1850000, 4, 55, N'Suite executive cao cap', N'TV, Wifi, Sofa, May pha cafe'),
('LP013', N'Premier Suite', 3, 2200000, 4, 60, N'Suite premier sang trong', N'TV, Wifi, Sofa, Bon tam nam'),
('LP014', N'VIP Diamond', 2, 2800000, 4, 70, N'Phong VIP cho khach hang cao cap', N'TV, Wifi, Ruou vang, Trai cay'),
('LP015', N'Presidential', 1, 5000000, 6, 110, N'Phong tong thong cao cap nhat', N'Phong khach, bep nho, may pha cafe, jacuzzi');
GO

-- Du lieu mau: Phong
INSERT INTO Phong (maPhong, tienCoc, maLoaiPhong, tang, trangThaiPhong) VALUES
('P101', 100000, 'LP001', 1, 'Trong'),
('P102', 100000, 'LP002', 1, 'Trong'),
('P103', 120000, 'LP003', 1, 'DangDon'),
('P104', 120000, 'LP004', 1, 'Trong'),
('P105', 150000, 'LP005', 1, 'DaDat'),
('P106', 150000, 'LP006', 1, 'Trong'),
('P201', 150000, 'LP007', 2, 'Trong'),
('P202', 180000, 'LP008', 2, 'BaoTri'),
('P203', 200000, 'LP009', 2, 'Trong'),
('P204', 220000, 'LP010', 2, 'DaDat'),
('P205', 250000, 'LP011', 2, 'Trong'),
('P206', 250000, 'LP012', 2, 'Trong'),
('P301', 300000, 'LP013', 3, 'Trong'),
('P302', 350000, 'LP014', 3, 'DaDat'),
('P303', 500000, 'LP015', 3, 'Trong');
GO

-- Du lieu mau: DichVu
INSERT INTO DichVu (maDV, tenDV, donGia, moTaDV, trangThaiDV) VALUES
('DV001', N'Nuoc suoi', 10000, N'Nuoc suoi chai 500ml', 'DangHoatDong'),
('DV002', N'Nuoc ngot', 15000, N'Coca, Pepsi, Sprite lon', 'DangHoatDong'),
('DV003', N'Bia lon', 25000, N'Bia lon phuc vu trong phong', 'DangHoatDong'),
('DV004', N'Mi ly', 30000, N'Mi ly an lien cao cap', 'DangHoatDong'),
('DV005', N'Do an nhanh', 55000, N'Khoai tay, ga ran, snack', 'DangHoatDong'),
('DV006', N'Giat ui', 40000, N'Giat ui theo mon do', 'DangHoatDong'),
('DV007', N'Don phong them', 80000, N'Ve sinh va thay vat dung bo sung', 'DangHoatDong'),
('DV008', N'Dua don san bay', 250000, N'Xe dua don 4 cho trong noi thanh', 'DangHoatDong'),
('DV009', N'Thue xe may', 180000, N'Thue xe may theo ngay', 'DangHoatDong'),
('DV010', N'Thue xe hoi', 950000, N'Thue xe hoi co tai xe theo gio', 'DangHoatDong'),
('DV011', N'An sang buffet', 120000, N'Suat buffet tai nha hang', 'DangHoatDong'),
('DV012', N'Spa thu gian', 450000, N'Goi massage va cham soc co ban', 'DangHoatDong'),
('DV013', N'Phu thu them nguoi', 200000, N'Phu thu moi nguoi vuot suc chua chuan', 'DangHoatDong'),
('DV014', N'Trang tri phong', 350000, N'Trang tri sinh nhat/ky niem', 'DangHoatDong'),
('DV015', N'Nuoc uong mini bar', 90000, N'Combo mini bar trong phong', 'DangHoatDong');
GO

-- Du lieu mau: KhuyenMai
INSERT INTO KhuyenMai (maKM, tenKM, dieuKienApDung, loaiKM, giaTriToiDa, tienKhuyenMai, ngayBatDau, ngayKetThuc, trangThaiKM) VALUES
('KM001', N'Uu dai dat som', N'Dat truoc 7 ngay', N'TheoTien', 1100000, 75000, '2026-01-11 00:00:00', '2026-02-25 00:00:00', 'HetHan'),
('KM002', N'Khach hang than thiet', N'Diem tich luy tu 500', N'TheoPhanTram', 1200000, 100000, '2026-01-21 00:00:00', '2026-03-07 00:00:00', 'HetHan'),
('KM003', N'Combo gia dinh', N'Dat tu 2 phong', N'TheoTien', 1300000, 125000, '2026-01-31 00:00:00', '2026-03-17 00:00:00', 'HetHan'),
('KM004', N'Khuyen mai cuoi tuan', N'Dat vao thu 6-7', N'TheoPhanTram', 1400000, 150000, '2026-02-10 00:00:00', '2026-03-27 00:00:00', 'DangHoatDong'),
('KM005', N'Le 30/4', N'Ap dung dip le', N'TheoTien', 1500000, 175000, '2026-02-20 00:00:00', '2026-04-06 00:00:00', 'DangHoatDong'),
('KM006', N'Giam gia mua he', N'Dat trong thang 6', N'TheoPhanTram', 1600000, 200000, '2026-03-02 00:00:00', '2026-04-16 00:00:00', 'DangHoatDong'),
('KM007', N'Khuyen mai doanh nghiep', N'Cong ty ky hop dong', N'TheoTien', 1700000, 225000, '2026-03-12 00:00:00', '2026-04-26 00:00:00', 'DangHoatDong'),
('KM008', N'Uu dai sinh nhat', N'Khach co ngay sinh trong thang', N'TheoPhanTram', 1800000, 250000, '2026-03-22 00:00:00', '2026-05-06 00:00:00', 'DangHoatDong'),
('KM009', N'Flash sale online', N'Dat qua website', N'TheoTien', 1900000, 275000, '2026-04-01 00:00:00', '2026-05-16 00:00:00', 'DangHoatDong'),
('KM010', N'Combo spa', N'Su dung dich vu spa', N'TheoPhanTram', 2000000, 300000, '2026-04-11 00:00:00', '2026-05-26 00:00:00', 'DangHoatDong'),
('KM011', N'Uu dai buffet', N'Kem buffet sang', N'TheoTien', 2100000, 325000, '2026-04-21 00:00:00', '2026-06-05 00:00:00', 'SapDienRa'),
('KM012', N'Tri an VIP', N'Hang Vang/KimCuong', N'TheoPhanTram', 2200000, 350000, '2026-05-01 00:00:00', '2026-06-15 00:00:00', 'SapDienRa'),
('KM013', N'Khuyen mai cuoi nam', N'Ap dung thang 12', N'TheoTien', 2300000, 375000, '2026-05-11 00:00:00', '2026-06-25 00:00:00', 'SapDienRa'),
('KM014', N'Dat 3 dem tinh 2.5', N'Luu tru tu 3 dem', N'TheoPhanTram', 2400000, 400000, '2026-05-21 00:00:00', '2026-07-05 00:00:00', 'SapDienRa'),
('KM015', N'Voucher doi tac', N'Khach tu doi tac lien ket', N'TheoTien', 2500000, 425000, '2026-05-31 00:00:00', '2026-07-15 00:00:00', 'SapDienRa');
GO

-- Du lieu mau: DatPhong
INSERT INTO DatPhong (maDatPhong, ngayDat, ngayNhanDuKien, ngayTraDuKien, tienCoc, ghiChu, maKH, maNV) VALUES
('DP001', '2026-04-01 10:00:00', '2026-04-03 10:00:00', '2026-04-05 10:00:00', 300000, N'Dat qua website', 'KH001', 'NV003'),
('DP002', '2026-04-02 11:00:00', '2026-04-05 11:00:00', '2026-04-08 11:00:00', 400000, N'Dat qua hotline', 'KH002', 'NV004'),
('DP003', '2026-04-03 12:00:00', '2026-04-07 12:00:00', '2026-04-08 12:00:00', 500000, N'Khach doan nho', 'KH003', 'NV005'),
('DP004', '2026-04-04 09:00:00', '2026-04-09 09:00:00', '2026-04-11 09:00:00', 200000, N'Dat qua le tan', 'KH004', 'NV006'),
('DP005', '2026-04-05 10:00:00', '2026-04-06 10:00:00', '2026-04-09 10:00:00', 300000, N'Dat qua website', 'KH005', 'NV007'),
('DP006', '2026-04-06 11:00:00', '2026-04-08 11:00:00', '2026-04-09 11:00:00', 400000, N'Dat qua hotline', 'KH006', 'NV008'),
('DP007', '2026-04-07 12:00:00', '2026-04-10 12:00:00', '2026-04-12 12:00:00', 500000, N'Khach doan nho', 'KH007', 'NV009'),
('DP008', '2026-04-08 09:00:00', '2026-04-12 09:00:00', '2026-04-15 09:00:00', 200000, N'Dat qua le tan', 'KH008', 'NV010'),
('DP009', '2026-04-09 10:00:00', '2026-04-14 10:00:00', '2026-04-15 10:00:00', 300000, N'Dat qua website', 'KH009', 'NV011'),
('DP010', '2026-04-10 11:00:00', '2026-04-11 11:00:00', '2026-04-13 11:00:00', 400000, N'Dat qua hotline', 'KH010', 'NV012'),
('DP011', '2026-04-11 12:00:00', '2026-04-13 12:00:00', '2026-04-16 12:00:00', 500000, N'Khach doan nho', 'KH011', 'NV013'),
('DP012', '2026-04-12 09:00:00', '2026-04-15 09:00:00', '2026-04-16 09:00:00', 200000, N'Dat qua le tan', 'KH012', 'NV014'),
('DP013', '2026-04-13 10:00:00', '2026-04-17 10:00:00', '2026-04-19 10:00:00', 300000, N'Dat qua website', 'KH013', 'NV015'),
('DP014', '2026-04-14 11:00:00', '2026-04-19 11:00:00', '2026-04-22 11:00:00', 400000, N'Dat qua hotline', 'KH014', 'NV001'),
('DP015', '2026-04-15 12:00:00', '2026-04-16 12:00:00', '2026-04-17 12:00:00', 500000, N'Khach doan nho', 'KH015', 'NV002');
GO

-- Du lieu mau: ChiTietDatPhong
INSERT INTO ChiTietDatPhong (maCTDP, maDatPhong, maPhong, ngayNhanDuKien, ngayTraDuKien, donGiaDat, soLuongNguoiO, ghiChu) VALUES
('CTDP001', 'DP001', 'P101', '2026-04-03 10:00:00', '2026-04-05 10:00:00', 450000, 2, N'Khong'),
('CTDP002', 'DP002', 'P102', '2026-04-05 11:00:00', '2026-04-08 11:00:00', 520000, 2, N'Khong'),
('CTDP003', 'DP003', 'P103', '2026-04-07 12:00:00', '2026-04-08 12:00:00', 580000, 2, N'Giu phong gan thang may'),
('CTDP004', 'DP004', 'P104', '2026-04-09 09:00:00', '2026-04-11 09:00:00', 650000, 1, N'Khong'),
('CTDP005', 'DP005', 'P105', '2026-04-06 10:00:00', '2026-04-09 10:00:00', 780000, 2, N'Khong'),
('CTDP006', 'DP006', 'P106', '2026-04-08 11:00:00', '2026-04-09 11:00:00', 820000, 3, N'Giu phong gan thang may'),
('CTDP007', 'DP007', 'P201', '2026-04-10 12:00:00', '2026-04-12 12:00:00', 860000, 3, N'Khong'),
('CTDP008', 'DP008', 'P202', '2026-04-12 09:00:00', '2026-04-15 09:00:00', 920000, 1, N'Khong'),
('CTDP009', 'DP009', 'P203', '2026-04-14 10:00:00', '2026-04-15 10:00:00', 1100000, 2, N'Giu phong gan thang may'),
('CTDP010', 'DP010', 'P204', '2026-04-11 11:00:00', '2026-04-13 11:00:00', 1450000, 3, N'Khong'),
('CTDP011', 'DP011', 'P205', '2026-04-13 12:00:00', '2026-04-16 12:00:00', 1550000, 3, N'Khong'),
('CTDP012', 'DP012', 'P206', '2026-04-15 09:00:00', '2026-04-16 09:00:00', 1850000, 1, N'Giu phong gan thang may'),
('CTDP013', 'DP013', 'P301', '2026-04-17 10:00:00', '2026-04-19 10:00:00', 2200000, 2, N'Khong'),
('CTDP014', 'DP014', 'P302', '2026-04-19 11:00:00', '2026-04-22 11:00:00', 2800000, 3, N'Khong'),
('CTDP015', 'DP015', 'P303', '2026-04-16 12:00:00', '2026-04-17 12:00:00', 5000000, 4, N'Giu phong gan thang may');
GO

-- Du lieu mau: HoaDon
INSERT INTO HoaDon (maHD, ngayLapHD, ngayThanhToan, ghiChu, soLuongNguoiO, tienPhong, tienDichVu, tienKhuyenMai, tienThue, tongTienThanhToan, phiDoiPhong, maKM, maKH, maNV, phuongThucTT, trangThai, maDatPhong) VALUES
('HD001', '2026-04-05 11:00:00', '2026-04-05 11:45:00', N'Hoa don cho DP001', 2, 900000, 20000, 0, 73600.00, 993600.00, 0, NULL, 'KH001', 'NV005', 'TienMat', 'DaThanhToan', 'DP001'),
('HD002', '2026-04-08 12:00:00', '2026-04-08 12:45:00', N'Hoa don cho DP002', 2, 1560000, 45000, 160500.00, 115560.00, 1560060.00, 0, 'KM004', 'KH002', 'NV006', 'ChuyenKhoan', 'DaThanhToan', 'DP002'),
('HD003', '2026-04-08 13:00:00', '2026-04-08 13:45:00', N'Hoa don cho DP003', 2, 580000, 25000, 175000, 34400.00, 464400.00, 0, 'KM005', 'KH003', 'NV007', 'TienMat', 'DaThanhToan', 'DP003'),
('HD004', '2026-04-11 10:00:00', '2026-04-11 10:45:00', N'Hoa don cho DP004', 1, 1300000, 60000, 136000.00, 101920.00, 1375920.00, 50000, 'KM006', 'KH004', 'NV008', 'ChuyenKhoan', 'DaThanhToan', 'DP004'),
('HD005', '2026-04-09 11:00:00', '2026-04-09 11:45:00', N'Hoa don cho DP005', 2, 2340000, 165000, 0, 200400.00, 2705400.00, 0, NULL, 'KH005', 'NV009', 'TienMat', 'DaThanhToan', 'DP005'),
('HD006', '2026-04-09 12:00:00', NULL, N'Hoa don cho DP006', 3, 820000, 40000, 86000.00, 61920.00, 835920.00, 0, 'KM008', 'KH006', 'NV010', 'ChuyenKhoan', 'ChuaThanhToan', 'DP006'),
('HD007', '2026-04-12 13:00:00', '2026-04-12 13:45:00', N'Hoa don cho DP007', 3, 1720000, 160000, 275000, 128400.00, 1733400.00, 0, 'KM009', 'KH007', 'NV011', 'TienMat', 'DaThanhToan', 'DP007'),
('HD008', '2026-04-15 10:00:00', '2026-04-15 10:45:00', N'Hoa don cho DP008', 1, 2760000, 750000, 0, 280800.00, 3790800.00, 0, NULL, 'KH008', 'NV012', 'ChuyenKhoan', 'DaThanhToan', 'DP008'),
('HD009', '2026-04-15 11:00:00', '2026-04-15 11:45:00', N'Hoa don cho DP009', 2, 1100000, 180000, 325000, 80400.00, 1085400.00, 50000, 'KM011', 'KH009', 'NV013', 'TienMat', 'DaThanhToan', 'DP009'),
('HD010', '2026-04-13 12:00:00', '2026-04-13 12:45:00', N'Hoa don cho DP010', 3, 2900000, 1900000, 480000.00, 345600.00, 4665600.00, 0, 'KM012', 'KH010', 'NV014', 'ChuyenKhoan', 'DaThanhToan', 'DP010'),
('HD011', '2026-04-16 13:00:00', '2026-04-16 13:45:00', N'Hoa don cho DP011', 3, 4650000, 360000, 0, 400800.00, 5410800.00, 0, NULL, 'KH011', 'NV015', 'TienMat', 'DaThanhToan', 'DP011'),
('HD012', '2026-04-16 10:00:00', '2026-04-16 10:45:00', N'Hoa don cho DP012', 1, 1850000, 450000, 230000.00, 165600.00, 2235600.00, 0, 'KM014', 'KH012', 'NV001', 'ChuyenKhoan', 'DaThanhToan', 'DP012'),
('HD013', '2026-04-19 11:00:00', '2026-04-19 11:45:00', N'Hoa don cho DP013', 2, 4400000, 400000, 0, 388000.00, 5238000.00, 50000, NULL, 'KH013', 'NV002', 'TienMat', 'DaThanhToan', 'DP013'),
('HD014', '2026-04-22 12:00:00', NULL, N'Hoa don cho DP014', 3, 8400000, 1050000, 425000, 722000.00, 9747000.00, 0, 'KM015', 'KH014', 'NV003', 'ChuyenKhoan', 'ChuaThanhToan', 'DP014'),
('HD015', '2026-04-17 13:00:00', '2026-04-17 13:45:00', N'Hoa don cho DP015', 4, 5000000, 90000, 0, 407200.00, 5497200.00, 0, NULL, 'KH015', 'NV004', 'TienMat', 'DaThanhToan', 'DP015');
GO

-- Du lieu mau: ChiTietHoaDon
INSERT INTO ChiTietHoaDon (maCTHD, maHD, maPhong, ngayNhanPhong, ngayTraPhong, soDem, phuThu, thanhTien) VALUES
('CTHD001', 'HD001', 'P101', '2026-04-03 10:00:00', '2026-04-05 10:00:00', 2, 0, 900000),
('CTHD002', 'HD002', 'P102', '2026-04-05 11:00:00', '2026-04-08 11:00:00', 3, 0, 1560000),
('CTHD003', 'HD003', 'P103', '2026-04-07 12:00:00', '2026-04-08 12:00:00', 1, 0, 580000),
('CTHD004', 'HD004', 'P104', '2026-04-09 09:00:00', '2026-04-11 09:00:00', 2, 50000, 1300000),
('CTHD005', 'HD005', 'P105', '2026-04-06 10:00:00', '2026-04-09 10:00:00', 3, 0, 2340000),
('CTHD006', 'HD006', 'P106', '2026-04-08 11:00:00', '2026-04-09 11:00:00', 1, 0, 820000),
('CTHD007', 'HD007', 'P201', '2026-04-10 12:00:00', '2026-04-12 12:00:00', 2, 0, 1720000),
('CTHD008', 'HD008', 'P202', '2026-04-12 09:00:00', '2026-04-15 09:00:00', 3, 0, 2760000),
('CTHD009', 'HD009', 'P203', '2026-04-14 10:00:00', '2026-04-15 10:00:00', 1, 50000, 1100000),
('CTHD010', 'HD010', 'P204', '2026-04-11 11:00:00', '2026-04-13 11:00:00', 2, 0, 2900000),
('CTHD011', 'HD011', 'P205', '2026-04-13 12:00:00', '2026-04-16 12:00:00', 3, 0, 4650000),
('CTHD012', 'HD012', 'P206', '2026-04-15 09:00:00', '2026-04-16 09:00:00', 1, 0, 1850000),
('CTHD013', 'HD013', 'P301', '2026-04-17 10:00:00', '2026-04-19 10:00:00', 2, 50000, 4400000),
('CTHD014', 'HD014', 'P302', '2026-04-19 11:00:00', '2026-04-22 11:00:00', 3, 0, 8400000),
('CTHD015', 'HD015', 'P303', '2026-04-16 12:00:00', '2026-04-17 12:00:00', 1, 0, 5000000);
GO

-- Du lieu mau: ChiTietDichVu
INSERT INTO ChiTietDichVu (maCTDV, maHD, maDV, soLuong, donGia, thanhTien, ghiChu) VALUES
('CTDV001', 'HD001', 'DV001', 2, 10000, 20000, N'Su dung trong thoi gian luu tru'),
('CTDV002', 'HD002', 'DV002', 3, 15000, 45000, N'Su dung trong thoi gian luu tru'),
('CTDV003', 'HD003', 'DV003', 1, 25000, 25000, N'Su dung trong thoi gian luu tru'),
('CTDV004', 'HD004', 'DV004', 2, 30000, 60000, N'Su dung trong thoi gian luu tru'),
('CTDV005', 'HD005', 'DV005', 3, 55000, 165000, N'Su dung trong thoi gian luu tru'),
('CTDV006', 'HD006', 'DV006', 1, 40000, 40000, N'Su dung trong thoi gian luu tru'),
('CTDV007', 'HD007', 'DV007', 2, 80000, 160000, N'Su dung trong thoi gian luu tru'),
('CTDV008', 'HD008', 'DV008', 3, 250000, 750000, N'Su dung trong thoi gian luu tru'),
('CTDV009', 'HD009', 'DV009', 1, 180000, 180000, N'Su dung trong thoi gian luu tru'),
('CTDV010', 'HD010', 'DV010', 2, 950000, 1900000, N'Su dung trong thoi gian luu tru'),
('CTDV011', 'HD011', 'DV011', 3, 120000, 360000, N'Su dung trong thoi gian luu tru'),
('CTDV012', 'HD012', 'DV012', 1, 450000, 450000, N'Su dung trong thoi gian luu tru'),
('CTDV013', 'HD013', 'DV013', 2, 200000, 400000, N'Su dung trong thoi gian luu tru'),
('CTDV014', 'HD014', 'DV014', 3, 350000, 1050000, N'Su dung trong thoi gian luu tru'),
('CTDV015', 'HD015', 'DV015', 1, 90000, 90000, N'Su dung trong thoi gian luu tru');
GO

-- Du lieu mau: ThanhToan
INSERT INTO ThanhToan (maTT, ngayTT, soTienTT, ghiChu, phuongThucTT, trangThaiTT, maHD) VALUES
('TT001', '2026-04-05 11:45:00', 993600.00, N'Thanh toan hoa don', 'TienMat', 'ThanhToanThanhCong', 'HD001'),
('TT002', '2026-04-08 12:45:00', 1560060.00, N'Thanh toan hoa don', 'ChuyenKhoan', 'ThanhToanThanhCong', 'HD002'),
('TT003', '2026-04-08 13:45:00', 464400.00, N'Thanh toan hoa don', 'TienMat', 'ThanhToanThanhCong', 'HD003'),
('TT004', '2026-04-11 10:45:00', 1375920.00, N'Thanh toan hoa don', 'ChuyenKhoan', 'ThanhToanThanhCong', 'HD004'),
('TT005', '2026-04-09 11:45:00', 2705400.00, N'Thanh toan hoa don', 'TienMat', 'ThanhToanThanhCong', 'HD005'),
('TT006', '2026-04-09 12:10:00', 0, N'Cho thanh toan', 'ChuyenKhoan', 'ChoThanhToan', 'HD006'),
('TT007', '2026-04-12 13:45:00', 1733400.00, N'Thanh toan hoa don', 'TienMat', 'ThanhToanThanhCong', 'HD007'),
('TT008', '2026-04-15 10:45:00', 3790800.00, N'Thanh toan hoa don', 'ChuyenKhoan', 'ThanhToanThanhCong', 'HD008'),
('TT009', '2026-04-15 11:45:00', 1085400.00, N'Thanh toan hoa don', 'TienMat', 'ThanhToanThanhCong', 'HD009'),
('TT010', '2026-04-13 12:45:00', 4665600.00, N'Thanh toan hoa don', 'ChuyenKhoan', 'ThanhToanThanhCong', 'HD010'),
('TT011', '2026-04-16 13:45:00', 5410800.00, N'Thanh toan hoa don', 'TienMat', 'ThanhToanThanhCong', 'HD011'),
('TT012', '2026-04-16 10:45:00', 2235600.00, N'Thanh toan hoa don', 'ChuyenKhoan', 'ThanhToanThanhCong', 'HD012'),
('TT013', '2026-04-19 11:45:00', 5238000.00, N'Thanh toan hoa don', 'TienMat', 'ThanhToanThanhCong', 'HD013'),
('TT014', '2026-04-22 12:10:00', 0, N'Cho thanh toan', 'ChuyenKhoan', 'ChoThanhToan', 'HD014'),
('TT015', '2026-04-17 13:45:00', 5497200.00, N'Thanh toan hoa don', 'TienMat', 'ThanhToanThanhCong', 'HD015');
GO

-- Kiem tra nhanh
SELECT COUNT(*) AS SoTaiKhoan FROM TaiKhoan;
SELECT COUNT(*) AS SoNhanVien FROM NhanVien;
SELECT COUNT(*) AS SoKhachHang FROM KhachHang;
SELECT COUNT(*) AS SoDatPhong FROM DatPhong;
SELECT COUNT(*) AS SoHoaDon FROM HoaDon;
SELECT COUNT(*) AS SoThanhToan FROM ThanhToan;
GO
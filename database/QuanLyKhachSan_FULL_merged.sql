-- SQL Server script generated from Class_Diagram_QLyKhachSan.vpp
SET NOCOUNT ON;

IF DB_ID(N'QLKhachSan') IS NULL CREATE DATABASE QLKhachSan;
GO
USE QLKhachSan;
GO

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
    ngay DATETIME2 NOT NULL,
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
    tienNghi NVARCHAR(255) NULL,
    tienCocToiThieu AS (giaPhong * 0.3)
);
GO
CREATE TABLE Phong (
    maPhong CHAR(4) PRIMARY KEY,
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
    maCTDP CHAR(8) PRIMARY KEY,
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

-- Du lieu mau: TaiKhoan (20 dong)
INSERT INTO TaiKhoan (tenDangNhap, matKhau, vaiTro, trangThaiTK) VALUES
('tk001', N'Hash@001', 'QuanLy', 'DangHoatDong'),
('tk002', N'Hash@002', 'QuanLy', 'DangHoatDong'),
('tk003', N'Hash@003', 'QuanLy', 'DangHoatDong'),
('tk004', N'Hash@004', 'QuanLy', 'DangHoatDong'),
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
('tk015', N'Hash@015', 'NhanVien', 'DangHoatDong'),
('tk016', N'Hash@016', 'NhanVien', 'DangHoatDong'),
('tk017', N'Hash@017', 'NhanVien', 'DangHoatDong'),
('tk018', N'Hash@018', 'NhanVien', 'DangHoatDong'),
('tk019', N'Hash@019', 'NhanVien', 'NgungHoatDong'),
('tk020', N'Hash@020', 'NhanVien', 'NgungHoatDong');
GO

-- Du lieu mau: NhanVien (20 dong)
INSERT INTO NhanVien (maNV, hoTenNV, sdt, gioiTinh, luong, ngayVao, tenDangNhap) VALUES
('NV001', N'Nguyen Minh Anh', '0910000001', 1, 8850000.00, '2023-02-12 00:00:00', 'tk001'),
('NV002', N'Tran Quoc Bao', '0910000002', 0, 9200000.00, '2023-03-12 00:00:00', 'tk002'),
('NV003', N'Le Thu Ha', '0910000003', 1, 9550000.00, '2023-04-09 00:00:00', 'tk003'),
('NV004', N'Pham Gia Huy', '0910000004', 0, 9900000.00, '2023-05-07 00:00:00', 'tk004'),
('NV005', N'Do Khanh Linh', '0910000005', 1, 10250000.00, '2023-06-04 00:00:00', 'tk005'),
('NV006', N'Vo Tuan Kiet', '0910000006', 0, 10600000.00, '2023-07-02 00:00:00', 'tk006'),
('NV007', N'Bui Ngoc Mai', '0910000007', 1, 10950000.00, '2023-07-30 00:00:00', 'tk007'),
('NV008', N'Dang Hoang Nam', '0910000008', 0, 11300000.00, '2023-08-27 00:00:00', 'tk008'),
('NV009', N'Hoang Thi Lan', '0910000009', 1, 11650000.00, '2023-09-24 00:00:00', 'tk009'),
('NV010', N'Phan Duc Long', '0910000010', 0, 12000000.00, '2023-10-22 00:00:00', 'tk010'),
('NV011', N'Nguyen Thanh Truc', '0910000011', 1, 12350000.00, '2023-11-19 00:00:00', 'tk011'),
('NV012', N'Trinh Quang Huy', '0910000012', 0, 12700000.00, '2023-12-17 00:00:00', 'tk012'),
('NV013', N'Ly My Tien', '0910000013', 1, 13050000.00, '2024-01-14 00:00:00', 'tk013'),
('NV014', N'Doan Bao Chau', '0910000014', 0, 13400000.00, '2024-02-11 00:00:00', 'tk014'),
('NV015', N'Ngo Duc An', '0910000015', 1, 13750000.00, '2024-03-10 00:00:00', 'tk015'),
('NV016', N'Le Bao Ngan', '0910000016', 0, 14100000.00, '2024-04-07 00:00:00', 'tk016'),
('NV017', N'Pham Quoc Huy', '0910000017', 1, 14450000.00, '2024-05-05 00:00:00', 'tk017'),
('NV018', N'Tran Nhat Minh', '0910000018', 0, 14800000.00, '2024-06-02 00:00:00', 'tk018'),
('NV019', N'Bui Thu Trang', '0910000019', 1, 15150000.00, '2024-06-30 00:00:00', 'tk019'),
('NV020', N'Dang Gia Bao', '0910000020', 0, 15500000.00, '2024-07-28 00:00:00', 'tk020');
GO

-- Du lieu mau: KhachHang (20 dong)
INSERT INTO KhachHang (maKH, hoTenKH, gioiTinh, ngaySinh, email, sdt, CCCD, quocTich, diaChi, hangKH, diemTichLuy) VALUES
('KH001', N'Nguyen Van An', 1, '1988-11-30 00:00:00', 'kh001@example.com', '0820000001', '700000000001', N'Viet Nam', N'Ha Noi', 'Dong', 0),
('KH002', N'Tran Thi Bich', 0, '1989-10-26 00:00:00', 'kh002@example.com', '0820000002', '700000000002', N'Viet Nam', N'Da Nang', 'Dong', 85),
('KH003', N'Le Quoc Cuong', 1, '1990-09-21 00:00:00', 'kh003@example.com', '0820000003', '700000000003', N'Viet Nam', N'Can Tho', 'Dong', 170),
('KH004', N'Pham Thu Dung', 0, '1991-08-17 00:00:00', 'kh004@example.com', '0820000004', '700000000004', N'Viet Nam', N'Nha Trang', 'Dong', 255),
('KH005', N'Vo Minh Duc', 1, '1992-07-12 00:00:00', 'kh005@example.com', '0820000005', '700000000005', N'Viet Nam', N'TP HCM', 'Bac', 340),
('KH006', N'Bui Ngoc Giang', 0, '1993-06-07 00:00:00', 'kh006@example.com', '0820000006', '700000000006', N'Viet Nam', N'Ha Noi', 'Bac', 425),
('KH007', N'Dang Hoai Nam', 1, '1994-05-03 00:00:00', 'kh007@example.com', '0820000007', '700000000007', N'Viet Nam', N'Da Nang', 'Bac', 510),
('KH008', N'Hoang Yen Nhi', 0, '1995-03-29 00:00:00', 'kh008@example.com', '0820000008', '700000000008', N'Viet Nam', N'Can Tho', 'Bac', 595),
('KH009', N'Phan Tuan Khang', 1, '1996-02-22 00:00:00', 'kh009@example.com', '0820000009', '700000000009', N'Viet Nam', N'Nha Trang', 'Vang', 680),
('KH010', N'Ngo Bao Tram', 0, '1997-01-17 00:00:00', 'kh010@example.com', '0820000010', '700000000010', N'Viet Nam', N'TP HCM', 'Vang', 765),
('KH011', N'Do Huu Phuc', 1, '1997-12-13 00:00:00', 'kh011@example.com', '0820000011', '700000000011', N'Viet Nam', N'Ha Noi', 'Vang', 850),
('KH012', N'Truong My Linh', 0, '1998-11-08 00:00:00', 'kh012@example.com', '0820000012', '700000000012', N'Viet Nam', N'Da Nang', 'Vang', 935),
('KH013', N'Ly Gia Han', 1, '1999-10-04 00:00:00', 'kh013@example.com', '0820000013', '700000000013', N'Han Quoc', N'Can Tho', 'KimCuong', 1020),
('KH014', N'Nguyen Quoc Viet', 0, '2000-08-29 00:00:00', 'kh014@example.com', '0820000014', '700000000014', N'Nhat Ban', N'Nha Trang', 'KimCuong', 1105),
('KH015', N'Tran Khanh Vy', 1, '2001-07-25 00:00:00', 'kh015@example.com', '0820000015', '700000000015', N'Singapore', N'TP HCM', 'KimCuong', 1190),
('KH016', N'Pham Thanh Son', 0, '2002-06-20 00:00:00', 'kh016@example.com', '0820000016', '700000000016', N'Viet Nam', N'Ha Noi', 'Dong', 1275),
('KH017', N'Le Ngoc Huyen', 1, '2003-05-16 00:00:00', 'kh017@example.com', '0820000017', '700000000017', N'Thai Lan', N'Da Nang', 'Bac', 1360),
('KH018', N'Vo Minh Chau', 0, '2004-04-10 00:00:00', 'kh018@example.com', '0820000018', '700000000018', N'Viet Nam', N'Can Tho', 'Vang', 1445),
('KH019', N'Dang Duc Hieu', 1, '2005-03-06 00:00:00', 'kh019@example.com', '0820000019', '700000000019', N'My', N'Nha Trang', 'Dong', 1530),
('KH020', N'Bui Quynh Anh', 0, '2006-01-30 00:00:00', 'kh020@example.com', '0820000020', '700000000020', N'Viet Nam', N'TP HCM', 'KimCuong', 1615);
GO

-- Du lieu mau: CaLam (20 dong)
INSERT INTO CaLam (maCa, gioBatDau, gioKetThuc, ghiChu, loaiCa) VALUES
('CA001','06:00:00','14:00:00',N'Ca sang thuong','CaSang'),
('CA002','14:00:00','22:00:00',N'Ca chieu thuong','CaChieu'),
('CA003','22:00:00','06:00:00',N'Ca toi thuong','CaToi'),
('CA004','05:30:00','13:30:00',N'Ca sang cuoi tuan','CaSang'),
('CA005','13:30:00','21:30:00',N'Ca chieu cuoi tuan','CaChieu'),
('CA006','21:30:00','05:30:00',N'Ca toi cuoi tuan','CaToi'),
('CA007','06:30:00','14:30:00',N'Ca sang le','CaSang'),
('CA008','14:30:00','22:30:00',N'Ca chieu le','CaChieu'),
('CA009','22:30:00','06:30:00',N'Ca toi le','CaToi'),
('CA010','07:00:00','15:00:00',N'Ca hanh chinh sang','CaSang'),
('CA011','15:00:00','23:00:00',N'Ca hanh chinh chieu','CaChieu'),
('CA012','23:00:00','07:00:00',N'Ca hanh chinh toi','CaToi'),
('CA013','06:00:00','12:00:00',N'Ca ngan sang ho tro','CaSang'),
('CA014','12:00:00','18:00:00',N'Ca ngan chieu ho tro','CaChieu'),
('CA015','18:00:00','23:59:59',N'Ca ngan toi ho tro','CaToi'),
('CA016','08:00:00','16:00:00',N'Ca sang dao tao','CaSang'),
('CA017','16:00:00','23:30:00',N'Ca chieu tang cuong','CaChieu'),
('CA018','00:00:00','08:00:00',N'Ca dem tang cuong','CaToi'),
('CA019','09:00:00','17:00:00',N'Ca hanh chinh dac biet','CaSang'),
('CA020','17:00:00','23:00:00',N'Ca toi su kien','CaToi');
GO

-- Du lieu mau: PhanCongCa (20 dong)
INSERT INTO PhanCongCa (maPC, ngay, tienMoCa, tienKetCa, maNV, maCa) VALUES
('PC001', '2026-04-01 00:00:00', 600000.00, 2285000.00, 'NV001', 'CA001'),
('PC002', '2026-04-02 00:00:00', 700000.00, 2470000.00, 'NV002', 'CA002'),
('PC003', '2026-04-03 00:00:00', 800000.00, 2655000.00, 'NV003', 'CA003'),
('PC004', '2026-04-04 00:00:00', 900000.00, 2840000.00, 'NV004', 'CA004'),
('PC005', '2026-04-05 00:00:00', 500000.00, 2525000.00, 'NV005', 'CA005'),
('PC006', '2026-04-06 00:00:00', 600000.00, 2710000.00, 'NV006', 'CA006'),
('PC007', '2026-04-07 00:00:00', 700000.00, 2895000.00, 'NV007', 'CA007'),
('PC008', '2026-04-08 00:00:00', 800000.00, 3080000.00, 'NV008', 'CA008'),
('PC009', '2026-04-09 00:00:00', 900000.00, 3265000.00, 'NV009', 'CA009'),
('PC010', '2026-04-10 00:00:00', 500000.00, 2950000.00, 'NV010', 'CA010'),
('PC011', '2026-04-11 00:00:00', 600000.00, 3135000.00, 'NV011', 'CA011'),
('PC012', '2026-04-12 00:00:00', 700000.00, 3320000.00, 'NV012', 'CA012'),
('PC013', '2026-04-13 00:00:00', 800000.00, 3505000.00, 'NV013', 'CA013'),
('PC014', '2026-04-14 00:00:00', 900000.00, 3690000.00, 'NV014', 'CA014'),
('PC015', '2026-04-15 00:00:00', 500000.00, 3375000.00, 'NV015', 'CA015'),
('PC016', '2026-04-16 00:00:00', 600000.00, 3560000.00, 'NV016', 'CA016'),
('PC017', '2026-04-17 00:00:00', 700000.00, 3745000.00, 'NV017', 'CA017'),
('PC018', '2026-04-18 00:00:00', 800000.00, 3930000.00, 'NV018', 'CA018'),
('PC019', '2026-04-19 00:00:00', 900000.00, 4115000.00, 'NV019', 'CA019'),
('PC020', '2026-04-20 00:00:00', 500000.00, 3800000.00, 'NV020', 'CA020');
GO

-- Du lieu mau: LoaiPhong (20 dong)
INSERT INTO LoaiPhong (maLoaiPhong, tenLoaiPhong, soLuongPhong, giaPhong, sucChuaToiDa, dienTich, moTa, tienNghi) VALUES
('LP001', N'Standard Don', 12, 510000.00, 2, 22.00, N'Mo ta standard don', N'TV, Wifi, May lanh'),
('LP002', N'Standard Doi', 11, 600000.00, 2, 24.00, N'Mo ta standard doi', N'TV, Wifi, May lanh'),
('LP003', N'Superior Don', 11, 690000.00, 2, 26.00, N'Mo ta superior don', N'TV, Wifi, May lanh'),
('LP004', N'Superior Doi', 10, 780000.00, 2, 28.00, N'Mo ta superior doi', N'TV, Wifi, May lanh'),
('LP005', N'Deluxe Twin', 10, 870000.00, 2, 30.00, N'Mo ta deluxe twin', N'TV, Wifi, May lanh'),
('LP006', N'Deluxe Double', 9, 960000.00, 2, 32.00, N'Mo ta deluxe double', N'TV, Wifi, May lanh'),
('LP007', N'Deluxe City View', 9, 1050000.00, 2, 34.00, N'Mo ta deluxe city view', N'TV, Wifi, May lanh'),
('LP008', N'Deluxe River View', 8, 1140000.00, 4, 36.00, N'Mo ta deluxe river view', N'TV, Wifi, May lanh'),
('LP009', N'Family 4', 8, 1230000.00, 4, 38.00, N'Mo ta family 4', N'TV, Wifi, May lanh'),
('LP010', N'Family 6', 7, 1320000.00, 6, 40.00, N'Mo ta family 6', N'TV, Wifi, May lanh'),
('LP011', N'Junior Suite', 7, 1410000.00, 4, 42.00, N'Mo ta junior suite', N'TV, Wifi, May lanh'),
('LP012', N'Executive Suite', 6, 1500000.00, 4, 44.00, N'Mo ta executive suite', N'TV, Wifi, May lanh'),
('LP013', N'Premier Suite', 6, 1590000.00, 4, 46.00, N'Mo ta premier suite', N'TV, Wifi, May lanh'),
('LP014', N'VIP Diamond', 5, 1680000.00, 4, 48.00, N'Mo ta vip diamond', N'TV, Wifi, May lanh'),
('LP015', N'Presidential', 5, 1770000.00, 6, 50.00, N'Mo ta presidential', N'TV, Wifi, May lanh'),
('LP016', N'Eco Single', 4, 1860000.00, 3, 52.00, N'Mo ta eco single', N'TV, Wifi, May lanh'),
('LP017', N'Eco Double', 4, 1950000.00, 3, 54.00, N'Mo ta eco double', N'TV, Wifi, May lanh'),
('LP018', N'Business Room', 3, 2040000.00, 3, 56.00, N'Mo ta business room', N'TV, Wifi, May lanh'),
('LP019', N'Connecting Room', 3, 2130000.00, 3, 58.00, N'Mo ta connecting room', N'TV, Wifi, May lanh'),
('LP020', N'Honeymoon Suite', 2, 2220000.00, 3, 60.00, N'Mo ta honeymoon suite', N'TV, Wifi, May lanh');
GO

-- Du lieu mau: Phong (20 dong)
INSERT INTO Phong (maPhong, maLoaiPhong, tang, trangThaiPhong) VALUES
('P101', 'LP001', 1, 'Trong'),
('P102', 'LP002', 1, 'Trong'),
('P103', 'LP003', 1, 'DangDon'),
('P104', 'LP004', 1, 'Trong'),
('P105', 'LP005', 1, 'DaDat'),
('P106', 'LP006', 1, 'Trong'),
('P107', 'LP007', 1, 'Trong'),
('P108', 'LP008', 1, 'BaoTri'),
('P109', 'LP009', 1, 'Trong'),
('P201', 'LP010', 2, 'DaDat'),
('P202', 'LP011', 2, 'Trong'),
('P203', 'LP012', 2, 'Trong'),
('P204', 'LP013', 2, 'Trong'),
('P205', 'LP014', 2, 'DaDat'),
('P206', 'LP015', 2, 'Trong'),
('P207', 'LP016', 2, 'Trong'),
('P301', 'LP017', 3, 'DangDon'),
('P302', 'LP018', 3, 'Trong'),
('P303', 'LP019', 3, 'DaDat'),
('P304', 'LP020', 3, 'Trong');
GO

-- Du lieu mau: DichVu (20 dong)
INSERT INTO DichVu (maDV, tenDV, donGia, moTaDV, trangThaiDV) VALUES
('DV001', N'Nuoc suoi', 10000.00, N'Dich vu nuoc suoi', 'DangHoatDong'),
('DV002', N'Nuoc ngot', 15000.00, N'Dich vu nuoc ngot', 'DangHoatDong'),
('DV003', N'Bia lon', 25000.00, N'Dich vu bia lon', 'DangHoatDong'),
('DV004', N'Mi ly', 30000.00, N'Dich vu mi ly', 'DangHoatDong'),
('DV005', N'Do an nhanh', 55000.00, N'Dich vu do an nhanh', 'DangHoatDong'),
('DV006', N'Giat ui', 40000.00, N'Dich vu giat ui', 'DangHoatDong'),
('DV007', N'Don phong them', 80000.00, N'Dich vu don phong them', 'DangHoatDong'),
('DV008', N'Dua don san bay', 250000.00, N'Dich vu dua don san bay', 'DangHoatDong'),
('DV009', N'Thue xe may', 180000.00, N'Dich vu thue xe may', 'DangHoatDong'),
('DV010', N'Thue xe hoi', 950000.00, N'Dich vu thue xe hoi', 'DangHoatDong'),
('DV011', N'An sang buffet', 120000.00, N'Dich vu an sang buffet', 'DangHoatDong'),
('DV012', N'Spa thu gian', 450000.00, N'Dich vu spa thu gian', 'DangHoatDong'),
('DV013', N'Phu thu them nguoi', 200000.00, N'Dich vu phu thu them nguoi', 'DangHoatDong'),
('DV014', N'Trang tri phong', 350000.00, N'Dich vu trang tri phong', 'DangHoatDong'),
('DV015', N'Nuoc uong mini bar', 90000.00, N'Dich vu nuoc uong mini bar', 'DangHoatDong'),
('DV016', N'Cafe pha may', 35000.00, N'Dich vu cafe pha may', 'DangHoatDong'),
('DV017', N'Tra sua', 45000.00, N'Dich vu tra sua', 'DangHoatDong'),
('DV018', N'Giat hap nhanh', 70000.00, N'Dich vu giat hap nhanh', 'DangHoatDong'),
('DV019', N'Goi them khan', 20000.00, N'Dich vu goi them khan', 'DangHoatDong'),
('DV020', N'Trang tri honeymoon', 500000.00, N'Dich vu trang tri honeymoon', 'DangHoatDong');
GO

-- Du lieu mau: KhuyenMai (20 dong)
INSERT INTO KhuyenMai (maKM, tenKM, dieuKienApDung, loaiKM, giaTriToiDa, tienKhuyenMai, ngayBatDau, ngayKetThuc, trangThaiKM) VALUES
('KM001', N'Khuyen mai 1', N'Dieu kien ap dung 1', N'TheoTien', 1100000.00, 75000.00, '2026-01-11 00:00:00', '2026-02-25 00:00:00', 'HetHan'),
('KM002', N'Khuyen mai 2', N'Dieu kien ap dung 2', N'TheoPhanTram', 1200000.00, 100000.00, '2026-01-21 00:00:00', '2026-03-07 00:00:00', 'HetHan'),
('KM003', N'Khuyen mai 3', N'Dieu kien ap dung 3', N'TheoTien', 1300000.00, 125000.00, '2026-01-31 00:00:00', '2026-03-17 00:00:00', 'HetHan'),
('KM004', N'Khuyen mai 4', N'Dieu kien ap dung 4', N'TheoPhanTram', 1400000.00, 150000.00, '2026-02-10 00:00:00', '2026-03-27 00:00:00', 'HetHan'),
('KM005', N'Khuyen mai 5', N'Dieu kien ap dung 5', N'TheoTien', 1500000.00, 175000.00, '2026-02-20 00:00:00', '2026-04-06 00:00:00', 'HetHan'),
('KM006', N'Khuyen mai 6', N'Dieu kien ap dung 6', N'TheoPhanTram', 1600000.00, 200000.00, '2026-03-02 00:00:00', '2026-04-16 00:00:00', 'DangHoatDong'),
('KM007', N'Khuyen mai 7', N'Dieu kien ap dung 7', N'TheoTien', 1700000.00, 225000.00, '2026-03-12 00:00:00', '2026-04-26 00:00:00', 'DangHoatDong'),
('KM008', N'Khuyen mai 8', N'Dieu kien ap dung 8', N'TheoPhanTram', 1800000.00, 250000.00, '2026-03-22 00:00:00', '2026-05-06 00:00:00', 'DangHoatDong'),
('KM009', N'Khuyen mai 9', N'Dieu kien ap dung 9', N'TheoTien', 1900000.00, 275000.00, '2026-04-01 00:00:00', '2026-05-16 00:00:00', 'DangHoatDong'),
('KM010', N'Khuyen mai 10', N'Dieu kien ap dung 10', N'TheoPhanTram', 2000000.00, 300000.00, '2026-04-11 00:00:00', '2026-05-26 00:00:00', 'DangHoatDong'),
('KM011', N'Khuyen mai 11', N'Dieu kien ap dung 11', N'TheoTien', 2100000.00, 325000.00, '2026-04-21 00:00:00', '2026-06-05 00:00:00', 'DangHoatDong'),
('KM012', N'Khuyen mai 12', N'Dieu kien ap dung 12', N'TheoPhanTram', 2200000.00, 350000.00, '2026-05-01 00:00:00', '2026-06-15 00:00:00', 'DangHoatDong'),
('KM013', N'Khuyen mai 13', N'Dieu kien ap dung 13', N'TheoTien', 2300000.00, 375000.00, '2026-05-11 00:00:00', '2026-06-25 00:00:00', 'DangHoatDong'),
('KM014', N'Khuyen mai 14', N'Dieu kien ap dung 14', N'TheoPhanTram', 2400000.00, 400000.00, '2026-05-21 00:00:00', '2026-07-05 00:00:00', 'DangHoatDong'),
('KM015', N'Khuyen mai 15', N'Dieu kien ap dung 15', N'TheoTien', 2500000.00, 425000.00, '2026-05-31 00:00:00', '2026-07-15 00:00:00', 'SapDienRa'),
('KM016', N'Khuyen mai 16', N'Dieu kien ap dung 16', N'TheoPhanTram', 2600000.00, 450000.00, '2026-06-10 00:00:00', '2026-07-25 00:00:00', 'SapDienRa'),
('KM017', N'Khuyen mai 17', N'Dieu kien ap dung 17', N'TheoTien', 2700000.00, 475000.00, '2026-06-20 00:00:00', '2026-08-04 00:00:00', 'SapDienRa'),
('KM018', N'Khuyen mai 18', N'Dieu kien ap dung 18', N'TheoPhanTram', 2800000.00, 500000.00, '2026-06-30 00:00:00', '2026-08-14 00:00:00', 'SapDienRa'),
('KM019', N'Khuyen mai 19', N'Dieu kien ap dung 19', N'TheoTien', 2900000.00, 525000.00, '2026-07-10 00:00:00', '2026-08-24 00:00:00', 'SapDienRa'),
('KM020', N'Khuyen mai 20', N'Dieu kien ap dung 20', N'TheoPhanTram', 3000000.00, 550000.00, '2026-07-20 00:00:00', '2026-09-03 00:00:00', 'SapDienRa');
GO

-- Du lieu mau: DatPhong (20 dong)
INSERT INTO DatPhong (maDatPhong, ngayDat, ngayNhanDuKien, ngayTraDuKien, tienCoc, ghiChu, maKH, maNV) VALUES
('DP001', '2026-04-01 10:00:00', '2026-04-03 10:00:00', '2026-04-05 10:00:00', 95000.00, N'Dat qua hotline', 'KH001', 'NV003'),
('DP002', '2026-04-02 11:00:00', '2026-04-05 11:00:00', '2026-04-08 11:00:00', 110000.00, N'Dat truc tiep le tan', 'KH002', 'NV004'),
('DP003', '2026-04-03 12:00:00', '2026-04-04 12:00:00', '2026-04-08 12:00:00', 125000.00, N'Khach doan nho', 'KH003', 'NV005'),
('DP004', '2026-04-04 09:00:00', '2026-04-06 09:00:00', '2026-04-07 09:00:00', 140000.00, N'Dat qua website', 'KH004', 'NV006'),
('DP005', '2026-04-05 10:00:00', '2026-04-08 10:00:00', '2026-04-10 10:00:00', 155000.00, N'Dat qua hotline', 'KH005', 'NV007'),
('DP006', '2026-04-06 11:00:00', '2026-04-07 11:00:00', '2026-04-10 11:00:00', 170000.00, N'Dat truc tiep le tan', 'KH006', 'NV008'),
('DP007', '2026-04-07 12:00:00', '2026-04-09 12:00:00', '2026-04-13 12:00:00', 185000.00, N'Khach doan nho', 'KH007', 'NV009'),
('DP008', '2026-04-08 09:00:00', '2026-04-11 09:00:00', '2026-04-12 09:00:00', 200000.00, N'Dat qua website', 'KH008', 'NV010'),
('DP009', '2026-04-09 10:00:00', '2026-04-10 10:00:00', '2026-04-12 10:00:00', 215000.00, N'Dat qua hotline', 'KH009', 'NV011'),
('DP010', '2026-04-10 11:00:00', '2026-04-12 11:00:00', '2026-04-15 11:00:00', 230000.00, N'Dat truc tiep le tan', 'KH010', 'NV012'),
('DP011', '2026-04-11 12:00:00', '2026-04-14 12:00:00', '2026-04-18 12:00:00', 245000.00, N'Khach doan nho', 'KH011', 'NV013'),
('DP012', '2026-04-12 09:00:00', '2026-04-13 09:00:00', '2026-04-14 09:00:00', 260000.00, N'Dat qua website', 'KH012', 'NV014'),
('DP013', '2026-04-13 10:00:00', '2026-04-15 10:00:00', '2026-04-17 10:00:00', 275000.00, N'Dat qua hotline', 'KH013', 'NV015'),
('DP014', '2026-04-14 11:00:00', '2026-04-17 11:00:00', '2026-04-20 11:00:00', 290000.00, N'Dat truc tiep le tan', 'KH014', 'NV016'),
('DP015', '2026-04-15 12:00:00', '2026-04-16 12:00:00', '2026-04-20 12:00:00', 305000.00, N'Khach doan nho', 'KH015', 'NV017'),
('DP016', '2026-04-16 09:00:00', '2026-04-18 09:00:00', '2026-04-19 09:00:00', 320000.00, N'Dat qua website', 'KH016', 'NV018'),
('DP017', '2026-04-17 10:00:00', '2026-04-20 10:00:00', '2026-04-22 10:00:00', 335000.00, N'Dat qua hotline', 'KH017', 'NV019'),
('DP018', '2026-04-18 11:00:00', '2026-04-19 11:00:00', '2026-04-22 11:00:00', 350000.00, N'Dat truc tiep le tan', 'KH018', 'NV020'),
('DP019', '2026-04-19 12:00:00', '2026-04-21 12:00:00', '2026-04-25 12:00:00', 365000.00, N'Khach doan nho', 'KH019', 'NV001'),
('DP020', '2026-04-20 09:00:00', '2026-04-23 09:00:00', '2026-04-24 09:00:00', 380000.00, N'Dat qua website', 'KH020', 'NV002');
GO

-- Du lieu mau: ChiTietDatPhong (20 dong)
INSERT INTO ChiTietDatPhong (maCTDP, maDatPhong, maPhong, ngayNhanDuKien, ngayTraDuKien, donGiaDat, soLuongNguoiO, ghiChu) VALUES
('CTDP001', 'DP001', 'P101', '2026-04-03 10:00:00', '2026-04-05 10:00:00', 510000.00, 2, N'Yeu cau phong P101'),
('CTDP002', 'DP002', 'P102', '2026-04-05 11:00:00', '2026-04-08 11:00:00', 600000.00, 2, N'Yeu cau phong P102'),
('CTDP003', 'DP003', 'P103', '2026-04-04 12:00:00', '2026-04-08 12:00:00', 690000.00, 2, N'Yeu cau phong P103'),
('CTDP004', 'DP004', 'P104', '2026-04-06 09:00:00', '2026-04-07 09:00:00', 780000.00, 3, N'Yeu cau phong P104'),
('CTDP005', 'DP005', 'P105', '2026-04-08 10:00:00', '2026-04-10 10:00:00', 870000.00, 3, N'Yeu cau phong P105'),
('CTDP006', 'DP006', 'P106', '2026-04-07 11:00:00', '2026-04-10 11:00:00', 960000.00, 2, N'Yeu cau phong P106'),
('CTDP007', 'DP007', 'P107', '2026-04-09 12:00:00', '2026-04-13 12:00:00', 1050000.00, 2, N'Yeu cau phong P107'),
('CTDP008', 'DP008', 'P108', '2026-04-11 09:00:00', '2026-04-12 09:00:00', 1140000.00, 2, N'Yeu cau phong P108'),
('CTDP009', 'DP009', 'P109', '2026-04-10 10:00:00', '2026-04-12 10:00:00', 1230000.00, 3, N'Yeu cau phong P109'),
('CTDP010', 'DP010', 'P201', '2026-04-12 11:00:00', '2026-04-15 11:00:00', 1320000.00, 4, N'Yeu cau phong P201'),
('CTDP011', 'DP011', 'P202', '2026-04-14 12:00:00', '2026-04-18 12:00:00', 1410000.00, 2, N'Yeu cau phong P202'),
('CTDP012', 'DP012', 'P203', '2026-04-13 09:00:00', '2026-04-14 09:00:00', 1500000.00, 2, N'Yeu cau phong P203'),
('CTDP013', 'DP013', 'P204', '2026-04-15 10:00:00', '2026-04-17 10:00:00', 1590000.00, 2, N'Yeu cau phong P204'),
('CTDP014', 'DP014', 'P205', '2026-04-17 11:00:00', '2026-04-20 11:00:00', 1680000.00, 3, N'Yeu cau phong P205'),
('CTDP015', 'DP015', 'P206', '2026-04-16 12:00:00', '2026-04-20 12:00:00', 1770000.00, 4, N'Yeu cau phong P206'),
('CTDP016', 'DP016', 'P207', '2026-04-18 09:00:00', '2026-04-19 09:00:00', 1860000.00, 2, N'Yeu cau phong P207'),
('CTDP017', 'DP017', 'P301', '2026-04-20 10:00:00', '2026-04-22 10:00:00', 1950000.00, 2, N'Yeu cau phong P301'),
('CTDP018', 'DP018', 'P302', '2026-04-19 11:00:00', '2026-04-22 11:00:00', 2040000.00, 2, N'Yeu cau phong P302'),
('CTDP019', 'DP019', 'P303', '2026-04-21 12:00:00', '2026-04-25 12:00:00', 2130000.00, 3, N'Yeu cau phong P303'),
('CTDP020', 'DP020', 'P304', '2026-04-23 09:00:00', '2026-04-24 09:00:00', 2220000.00, 4, N'Yeu cau phong P304');
GO

-- Du lieu mau: HoaDon (20 dong)
INSERT INTO HoaDon (maHD, ngayLapHD, ngayThanhToan, ghiChu, soLuongNguoiO, tienPhong, tienDichVu, tienKhuyenMai, tienThue, tongTienThanhToan, phiDoiPhong, maKM, maKH, maNV, phuongThucTT, trangThai, maDatPhong) VALUES
('HD001', '2026-04-05 10:00:00', '2026-04-05 11:00:00', N'Hoa don cho DP001', 2, 1020000.00, 20000.00, 0.00, 83200.00, 1123200.00, 0.00, NULL, 'KH001', 'NV003', 'TienMat', 'DaThanhToan', 'DP001'),
('HD002', '2026-04-08 11:00:00', '2026-04-08 12:00:00', N'Hoa don cho DP002', 2, 1800000.00, 45000.00, 100000.00, 139600.00, 1884600.00, 0.00, 'KM002', 'KH002', 'NV004', 'ChuyenKhoan', 'DaThanhToan', 'DP002'),
('HD003', '2026-04-08 12:00:00', '2026-04-08 13:00:00', N'Hoa don cho DP003', 2, 2760000.00, 25000.00, 0.00, 222800.00, 3007800.00, 0.00, NULL, 'KH003', 'NV005', 'TienMat', 'DaThanhToan', 'DP003'),
('HD004', '2026-04-07 09:00:00', '2026-04-07 10:00:00', N'Hoa don cho DP004', 3, 830000.00, 60000.00, 150000.00, 59200.00, 799200.00, 50000.00, 'KM004', 'KH004', 'NV006', 'ChuyenKhoan', 'DaThanhToan', 'DP004'),
('HD005', '2026-04-10 10:00:00', '2026-04-10 11:00:00', N'Hoa don cho DP005', 3, 1740000.00, 165000.00, 0.00, 152400.00, 2057400.00, 0.00, NULL, 'KH005', 'NV007', 'TienMat', 'DaThanhToan', 'DP005'),
('HD006', '2026-04-10 11:00:00', NULL, N'Hoa don cho DP006', 2, 2880000.00, 40000.00, 200000.00, 217600.00, 2937600.00, 0.00, 'KM006', 'KH006', 'NV008', 'ChuyenKhoan', 'ChuaThanhToan', 'DP006'),
('HD007', '2026-04-13 12:00:00', '2026-04-13 13:00:00', N'Hoa don cho DP007', 2, 4200000.00, 160000.00, 0.00, 348800.00, 4708800.00, 0.00, NULL, 'KH007', 'NV009', 'TienMat', 'DaThanhToan', 'DP007'),
('HD008', '2026-04-12 09:00:00', '2026-04-12 10:00:00', N'Hoa don cho DP008', 2, 1190000.00, 750000.00, 250000.00, 135200.00, 1825200.00, 50000.00, 'KM008', 'KH008', 'NV010', 'ChuyenKhoan', 'DaThanhToan', 'DP008'),
('HD009', '2026-04-12 10:00:00', '2026-04-12 11:00:00', N'Hoa don cho DP009', 3, 2460000.00, 180000.00, 0.00, 211200.00, 2851200.00, 0.00, NULL, 'KH009', 'NV011', 'TienMat', 'DaThanhToan', 'DP009'),
('HD010', '2026-04-15 11:00:00', '2026-04-15 12:00:00', N'Hoa don cho DP010', 4, 3960000.00, 1900000.00, 300000.00, 444800.00, 6004800.00, 0.00, 'KM010', 'KH010', 'NV012', 'ChuyenKhoan', 'DaThanhToan', 'DP010'),
('HD011', '2026-04-18 12:00:00', '2026-04-18 13:00:00', N'Hoa don cho DP011', 2, 5640000.00, 360000.00, 0.00, 480000.00, 6480000.00, 0.00, NULL, 'KH011', 'NV013', 'TienMat', 'DaThanhToan', 'DP011'),
('HD012', '2026-04-14 09:00:00', '2026-04-14 10:00:00', N'Hoa don cho DP012', 2, 1550000.00, 450000.00, 350000.00, 132000.00, 1782000.00, 50000.00, 'KM012', 'KH012', 'NV014', 'ChuyenKhoan', 'DaThanhToan', 'DP012'),
('HD013', '2026-04-17 10:00:00', '2026-04-17 11:00:00', N'Hoa don cho DP013', 2, 3180000.00, 400000.00, 0.00, 286400.00, 3866400.00, 0.00, NULL, 'KH013', 'NV015', 'TienMat', 'DaThanhToan', 'DP013'),
('HD014', '2026-04-20 11:00:00', NULL, N'Hoa don cho DP014', 3, 5040000.00, 1050000.00, 400000.00, 455200.00, 6145200.00, 0.00, 'KM014', 'KH014', 'NV016', 'ChuyenKhoan', 'ChuaThanhToan', 'DP014'),
('HD015', '2026-04-20 12:00:00', '2026-04-20 13:00:00', N'Hoa don cho DP015', 4, 7080000.00, 90000.00, 0.00, 573600.00, 7743600.00, 0.00, NULL, 'KH015', 'NV017', 'TienMat', 'DaThanhToan', 'DP015'),
('HD016', '2026-04-19 09:00:00', '2026-04-19 10:00:00', N'Hoa don cho DP016', 2, 1910000.00, 70000.00, 450000.00, 122400.00, 1652400.00, 50000.00, 'KM016', 'KH016', 'NV018', 'ChuyenKhoan', 'DaThanhToan', 'DP016'),
('HD017', '2026-04-22 10:00:00', '2026-04-22 11:00:00', N'Hoa don cho DP017', 2, 3900000.00, 135000.00, 0.00, 322800.00, 4357800.00, 0.00, NULL, 'KH017', 'NV019', 'TienMat', 'DaThanhToan', 'DP017'),
('HD018', '2026-04-22 11:00:00', '2026-04-22 12:00:00', N'Hoa don cho DP018', 2, 6120000.00, 70000.00, 500000.00, 455200.00, 6145200.00, 0.00, 'KM018', 'KH018', 'NV020', 'ChuyenKhoan', 'DaThanhToan', 'DP018'),
('HD019', '2026-04-25 12:00:00', NULL, N'Hoa don cho DP019', 3, 8520000.00, 40000.00, 0.00, 684800.00, 9244800.00, 0.00, NULL, 'KH019', 'NV001', 'TienMat', 'ChuaThanhToan', 'DP019'),
('HD020', '2026-04-24 09:00:00', '2026-04-24 10:00:00', N'Hoa don cho DP020', 4, 2270000.00, 1500000.00, 550000.00, 257600.00, 3477600.00, 50000.00, 'KM020', 'KH020', 'NV002', 'ChuyenKhoan', 'DaThanhToan', 'DP020');
GO

-- Du lieu mau: ChiTietHoaDon (20 dong)
INSERT INTO ChiTietHoaDon (maCTHD, maHD, maPhong, ngayNhanPhong, ngayTraPhong, soDem, phuThu, thanhTien) VALUES
('CTHD001', 'HD001', 'P101', '2026-04-03 10:00:00', '2026-04-05 10:00:00', 2, 0.00, 1020000.00),
('CTHD002', 'HD002', 'P102', '2026-04-05 11:00:00', '2026-04-08 11:00:00', 3, 0.00, 1800000.00),
('CTHD003', 'HD003', 'P103', '2026-04-04 12:00:00', '2026-04-08 12:00:00', 4, 0.00, 2760000.00),
('CTHD004', 'HD004', 'P104', '2026-04-06 09:00:00', '2026-04-07 09:00:00', 1, 50000.00, 830000.00),
('CTHD005', 'HD005', 'P105', '2026-04-08 10:00:00', '2026-04-10 10:00:00', 2, 0.00, 1740000.00),
('CTHD006', 'HD006', 'P106', '2026-04-07 11:00:00', '2026-04-10 11:00:00', 3, 0.00, 2880000.00),
('CTHD007', 'HD007', 'P107', '2026-04-09 12:00:00', '2026-04-13 12:00:00', 4, 0.00, 4200000.00),
('CTHD008', 'HD008', 'P108', '2026-04-11 09:00:00', '2026-04-12 09:00:00', 1, 50000.00, 1190000.00),
('CTHD009', 'HD009', 'P109', '2026-04-10 10:00:00', '2026-04-12 10:00:00', 2, 0.00, 2460000.00),
('CTHD010', 'HD010', 'P201', '2026-04-12 11:00:00', '2026-04-15 11:00:00', 3, 0.00, 3960000.00),
('CTHD011', 'HD011', 'P202', '2026-04-14 12:00:00', '2026-04-18 12:00:00', 4, 0.00, 5640000.00),
('CTHD012', 'HD012', 'P203', '2026-04-13 09:00:00', '2026-04-14 09:00:00', 1, 50000.00, 1550000.00),
('CTHD013', 'HD013', 'P204', '2026-04-15 10:00:00', '2026-04-17 10:00:00', 2, 0.00, 3180000.00),
('CTHD014', 'HD014', 'P205', '2026-04-17 11:00:00', '2026-04-20 11:00:00', 3, 0.00, 5040000.00),
('CTHD015', 'HD015', 'P206', '2026-04-16 12:00:00', '2026-04-20 12:00:00', 4, 0.00, 7080000.00),
('CTHD016', 'HD016', 'P207', '2026-04-18 09:00:00', '2026-04-19 09:00:00', 1, 50000.00, 1910000.00),
('CTHD017', 'HD017', 'P301', '2026-04-20 10:00:00', '2026-04-22 10:00:00', 2, 0.00, 3900000.00),
('CTHD018', 'HD018', 'P302', '2026-04-19 11:00:00', '2026-04-22 11:00:00', 3, 0.00, 6120000.00),
('CTHD019', 'HD019', 'P303', '2026-04-21 12:00:00', '2026-04-25 12:00:00', 4, 0.00, 8520000.00),
('CTHD020', 'HD020', 'P304', '2026-04-23 09:00:00', '2026-04-24 09:00:00', 1, 50000.00, 2270000.00);
GO

-- Du lieu mau: ChiTietDichVu (20 dong)
INSERT INTO ChiTietDichVu (maCTDV, maHD, maDV, soLuong, donGia, thanhTien, ghiChu) VALUES
('CTDV001', 'HD001', 'DV001', 2, 10000.00, 20000.00, N'Su dung trong thoi gian luu tru'),
('CTDV002', 'HD002', 'DV002', 3, 15000.00, 45000.00, N'Su dung trong thoi gian luu tru'),
('CTDV003', 'HD003', 'DV003', 1, 25000.00, 25000.00, N'Su dung trong thoi gian luu tru'),
('CTDV004', 'HD004', 'DV004', 2, 30000.00, 60000.00, N'Su dung trong thoi gian luu tru'),
('CTDV005', 'HD005', 'DV005', 3, 55000.00, 165000.00, N'Su dung trong thoi gian luu tru'),
('CTDV006', 'HD006', 'DV006', 1, 40000.00, 40000.00, N'Su dung trong thoi gian luu tru'),
('CTDV007', 'HD007', 'DV007', 2, 80000.00, 160000.00, N'Su dung trong thoi gian luu tru'),
('CTDV008', 'HD008', 'DV008', 3, 250000.00, 750000.00, N'Su dung trong thoi gian luu tru'),
('CTDV009', 'HD009', 'DV009', 1, 180000.00, 180000.00, N'Su dung trong thoi gian luu tru'),
('CTDV010', 'HD010', 'DV010', 2, 950000.00, 1900000.00, N'Su dung trong thoi gian luu tru'),
('CTDV011', 'HD011', 'DV011', 3, 120000.00, 360000.00, N'Su dung trong thoi gian luu tru'),
('CTDV012', 'HD012', 'DV012', 1, 450000.00, 450000.00, N'Su dung trong thoi gian luu tru'),
('CTDV013', 'HD013', 'DV013', 2, 200000.00, 400000.00, N'Su dung trong thoi gian luu tru'),
('CTDV014', 'HD014', 'DV014', 3, 350000.00, 1050000.00, N'Su dung trong thoi gian luu tru'),
('CTDV015', 'HD015', 'DV015', 1, 90000.00, 90000.00, N'Su dung trong thoi gian luu tru'),
('CTDV016', 'HD016', 'DV016', 2, 35000.00, 70000.00, N'Su dung trong thoi gian luu tru'),
('CTDV017', 'HD017', 'DV017', 3, 45000.00, 135000.00, N'Su dung trong thoi gian luu tru'),
('CTDV018', 'HD018', 'DV018', 1, 70000.00, 70000.00, N'Su dung trong thoi gian luu tru'),
('CTDV019', 'HD019', 'DV019', 2, 20000.00, 40000.00, N'Su dung trong thoi gian luu tru'),
('CTDV020', 'HD020', 'DV020', 3, 500000.00, 1500000.00, N'Su dung trong thoi gian luu tru');
GO

-- Du lieu mau: ThanhToan (20 dong)
INSERT INTO ThanhToan (maTT, ngayTT, soTienTT, ghiChu, phuongThucTT, trangThaiTT, maHD) VALUES
('TT001', '2026-04-05 11:15:00', 1123200.00, N'Thanh toan hoa don', 'TienMat', 'ThanhToanThanhCong', 'HD001'),
('TT002', '2026-04-08 12:15:00', 1884600.00, N'Thanh toan hoa don', 'ChuyenKhoan', 'ThanhToanThanhCong', 'HD002'),
('TT003', '2026-04-08 13:15:00', 3007800.00, N'Thanh toan hoa don', 'TienMat', 'ThanhToanThanhCong', 'HD003'),
('TT004', '2026-04-07 10:15:00', 799200.00, N'Thanh toan hoa don', 'ChuyenKhoan', 'ThanhToanThanhCong', 'HD004'),
('TT005', '2026-04-10 11:15:00', 2057400.00, N'Thanh toan hoa don', 'TienMat', 'ThanhToanThanhCong', 'HD005'),
('TT006', '2026-04-10 12:15:00', 0.00, N'Cho thanh toan', 'ChuyenKhoan', 'ChoThanhToan', 'HD006'),
('TT007', '2026-04-13 13:15:00', 4708800.00, N'Thanh toan hoa don', 'TienMat', 'ThanhToanThanhCong', 'HD007'),
('TT008', '2026-04-12 10:15:00', 1825200.00, N'Thanh toan hoa don', 'ChuyenKhoan', 'ThanhToanThanhCong', 'HD008'),
('TT009', '2026-04-12 11:15:00', 2851200.00, N'Thanh toan hoa don', 'TienMat', 'ThanhToanThanhCong', 'HD009'),
('TT010', '2026-04-15 12:15:00', 6004800.00, N'Thanh toan hoa don', 'ChuyenKhoan', 'ThanhToanThanhCong', 'HD010'),
('TT011', '2026-04-18 13:15:00', 6480000.00, N'Thanh toan hoa don', 'TienMat', 'ThanhToanThanhCong', 'HD011'),
('TT012', '2026-04-14 10:15:00', 1782000.00, N'Thanh toan hoa don', 'ChuyenKhoan', 'ThanhToanThanhCong', 'HD012'),
('TT013', '2026-04-17 11:15:00', 3866400.00, N'Thanh toan hoa don', 'TienMat', 'ThanhToanThanhCong', 'HD013'),
('TT014', '2026-04-20 12:15:00', 0.00, N'Cho thanh toan', 'ChuyenKhoan', 'ChoThanhToan', 'HD014'),
('TT015', '2026-04-20 13:15:00', 7743600.00, N'Thanh toan hoa don', 'TienMat', 'ThanhToanThanhCong', 'HD015'),
('TT016', '2026-04-19 10:15:00', 1652400.00, N'Thanh toan hoa don', 'ChuyenKhoan', 'ThanhToanThanhCong', 'HD016'),
('TT017', '2026-04-22 11:15:00', 4357800.00, N'Thanh toan hoa don', 'TienMat', 'ThanhToanThanhCong', 'HD017'),
('TT018', '2026-04-22 12:15:00', 6145200.00, N'Thanh toan hoa don', 'ChuyenKhoan', 'ThanhToanThanhCong', 'HD018'),
('TT019', '2026-04-25 13:15:00', 0.00, N'Cho thanh toan', 'TienMat', 'ChoThanhToan', 'HD019'),
('TT020', '2026-04-24 10:15:00', 3477600.00, N'Thanh toan hoa don', 'ChuyenKhoan', 'ThanhToanThanhCong', 'HD020');
GO


-- =========================================================
-- BO SUNG 10 MAU HOA DON: DAT 1 LUC 2-3 PHONG
-- 8 CHUA THANH TOAN, 2 DA THANH TOAN
-- DAN TU DP021 - DP030 / HD021 - HD030
-- =========================================================

INSERT INTO DatPhong (maDatPhong, ngayDat, ngayNhanDuKien, ngayTraDuKien, tienCoc, ghiChu, maKH, maNV) VALUES
('DP021', '2026-05-01 09:00:00', '2026-05-03 14:00:00', '2026-05-05 12:00:00', 400000.00, N'Dat 2 phong cung luc cho gia dinh', 'KH001', 'NV005'),
('DP022', '2026-05-02 10:00:00', '2026-05-05 14:00:00', '2026-05-08 12:00:00', 800000.00, N'Dat 3 phong cho nhom ban', 'KH002', 'NV006'),
('DP023', '2026-05-04 08:30:00', '2026-05-06 14:00:00', '2026-05-08 12:00:00', 500000.00, N'Dat 2 phong view dep', 'KH003', 'NV007'),
('DP024', '2026-05-05 11:00:00', '2026-05-08 14:00:00', '2026-05-11 12:00:00', 900000.00, N'Dat 3 phong cho doan cong tac', 'KH004', 'NV008'),
('DP025', '2026-05-07 15:00:00', '2026-05-10 14:00:00', '2026-05-12 12:00:00', 600000.00, N'Dat 2 phong lien nhau', 'KH005', 'NV009'),
('DP026', '2026-05-08 16:00:00', '2026-05-11 14:00:00', '2026-05-14 12:00:00', 1200000.00, N'Dat 3 phong VIP cho gia dinh', 'KH006', 'NV010'),
('DP027', '2026-05-10 09:30:00', '2026-05-13 14:00:00', '2026-05-15 12:00:00', 700000.00, N'Dat 2 phong cho doi tac', 'KH007', 'NV011'),
('DP028', '2026-05-11 13:00:00', '2026-05-14 14:00:00', '2026-05-17 12:00:00', 1300000.00, N'Dat 3 phong cao cap', 'KH008', 'NV012'),
('DP029', '2026-05-15 10:15:00', '2026-05-18 14:00:00', '2026-05-20 12:00:00', 450000.00, N'Dat 2 phong tieu chuan', 'KH009', 'NV013'),
('DP030', '2026-05-16 11:45:00', '2026-05-19 14:00:00', '2026-05-22 12:00:00', 950000.00, N'Dat 3 phong hon hop', 'KH010', 'NV014');
GO

INSERT INTO ChiTietDatPhong (maCTDP, maDatPhong, maPhong, ngayNhanDuKien, ngayTraDuKien, donGiaDat, soLuongNguoiO, ghiChu) VALUES
('CTDP021', 'DP021', 'P101', '2026-05-03 14:00:00', '2026-05-05 12:00:00', 510000.00, 2, N'Phong 1 trong don DP021'),
('CTDP022', 'DP021', 'P102', '2026-05-03 14:00:00', '2026-05-05 12:00:00', 600000.00, 2, N'Phong 2 trong don DP021'),
('CTDP023', 'DP022', 'P103', '2026-05-05 14:00:00', '2026-05-08 12:00:00', 690000.00, 2, N'Phong 1 trong don DP022'),
('CTDP024', 'DP022', 'P104', '2026-05-05 14:00:00', '2026-05-08 12:00:00', 780000.00, 2, N'Phong 2 trong don DP022'),
('CTDP025', 'DP022', 'P105', '2026-05-05 14:00:00', '2026-05-08 12:00:00', 870000.00, 3, N'Phong 3 trong don DP022'),
('CTDP026', 'DP023', 'P106', '2026-05-06 14:00:00', '2026-05-08 12:00:00', 960000.00, 2, N'Phong 1 trong don DP023'),
('CTDP027', 'DP023', 'P107', '2026-05-06 14:00:00', '2026-05-08 12:00:00', 1050000.00, 2, N'Phong 2 trong don DP023'),
('CTDP028', 'DP024', 'P108', '2026-05-08 14:00:00', '2026-05-11 12:00:00', 1140000.00, 2, N'Phong 1 trong don DP024'),
('CTDP029', 'DP024', 'P109', '2026-05-08 14:00:00', '2026-05-11 12:00:00', 1230000.00, 3, N'Phong 2 trong don DP024'),
('CTDP030', 'DP024', 'P201', '2026-05-08 14:00:00', '2026-05-11 12:00:00', 1320000.00, 4, N'Phong 3 trong don DP024'),
('CTDP031', 'DP025', 'P202', '2026-05-10 14:00:00', '2026-05-12 12:00:00', 1410000.00, 2, N'Phong 1 trong don DP025'),
('CTDP032', 'DP025', 'P203', '2026-05-10 14:00:00', '2026-05-12 12:00:00', 1500000.00, 2, N'Phong 2 trong don DP025'),
('CTDP033', 'DP026', 'P204', '2026-05-11 14:00:00', '2026-05-14 12:00:00', 1590000.00, 2, N'Phong 1 trong don DP026'),
('CTDP034', 'DP026', 'P205', '2026-05-11 14:00:00', '2026-05-14 12:00:00', 1680000.00, 3, N'Phong 2 trong don DP026'),
('CTDP035', 'DP026', 'P206', '2026-05-11 14:00:00', '2026-05-14 12:00:00', 1770000.00, 4, N'Phong 3 trong don DP026'),
('CTDP036', 'DP027', 'P207', '2026-05-13 14:00:00', '2026-05-15 12:00:00', 1860000.00, 2, N'Phong 1 trong don DP027'),
('CTDP037', 'DP027', 'P301', '2026-05-13 14:00:00', '2026-05-15 12:00:00', 1950000.00, 2, N'Phong 2 trong don DP027'),
('CTDP038', 'DP028', 'P302', '2026-05-14 14:00:00', '2026-05-17 12:00:00', 2040000.00, 2, N'Phong 1 trong don DP028'),
('CTDP039', 'DP028', 'P303', '2026-05-14 14:00:00', '2026-05-17 12:00:00', 2130000.00, 3, N'Phong 2 trong don DP028'),
('CTDP040', 'DP028', 'P304', '2026-05-14 14:00:00', '2026-05-17 12:00:00', 2220000.00, 3, N'Phong 3 trong don DP028'),
('CTDP041', 'DP029', 'P101', '2026-05-18 14:00:00', '2026-05-20 12:00:00', 510000.00, 2, N'Phong 1 trong don DP029'),
('CTDP042', 'DP029', 'P104', '2026-05-18 14:00:00', '2026-05-20 12:00:00', 780000.00, 2, N'Phong 2 trong don DP029'),
('CTDP043', 'DP030', 'P105', '2026-05-19 14:00:00', '2026-05-22 12:00:00', 870000.00, 2, N'Phong 1 trong don DP030'),
('CTDP044', 'DP030', 'P108', '2026-05-19 14:00:00', '2026-05-22 12:00:00', 1140000.00, 2, N'Phong 2 trong don DP030'),
('CTDP045', 'DP030', 'P202', '2026-05-19 14:00:00', '2026-05-22 12:00:00', 1410000.00, 3, N'Phong 3 trong don DP030');
GO

INSERT INTO HoaDon (maHD, ngayLapHD, ngayThanhToan, ghiChu, soLuongNguoiO, tienPhong, tienDichVu, tienKhuyenMai, tienThue, tongTienThanhToan, phiDoiPhong, maKM, maKH, maNV, phuongThucTT, trangThai, maDatPhong) VALUES
('HD021', '2026-05-05 12:30:00', '2026-05-05 13:00:00', N'Hoa don 2 phong - da thanh toan', 4, 2220000.00, 140000.00, 0.00, 236000.00, 2596000.00, 0.00, NULL, 'KH001', 'NV005', 'TienMat', 'DaThanhToan', 'DP021'),
('HD022', '2026-05-08 12:30:00', NULL, N'Hoa don 3 phong - chua thanh toan', 7, 7020000.00, 370000.00, 225000.00, 716500.00, 7881500.00, 0.00, 'KM007', 'KH002', 'NV006', 'ChuyenKhoan', 'ChuaThanhToan', 'DP022'),
('HD023', '2026-05-08 12:30:00', NULL, N'Hoa don 2 phong - chua thanh toan', 4, 4020000.00, 200000.00, 0.00, 422000.00, 4642000.00, 0.00, NULL, 'KH003', 'NV007', 'TienMat', 'ChuaThanhToan', 'DP023'),
('HD024', '2026-05-11 12:30:00', NULL, N'Hoa don 3 phong - chua thanh toan', 9, 11070000.00, 700000.00, 250000.00, 1152000.00, 12672000.00, 0.00, 'KM008', 'KH004', 'NV008', 'ChuyenKhoan', 'ChuaThanhToan', 'DP024'),
('HD025', '2026-05-12 12:30:00', NULL, N'Hoa don 2 phong - chua thanh toan', 4, 5820000.00, 130000.00, 0.00, 595000.00, 6545000.00, 0.00, NULL, 'KH005', 'NV009', 'TienMat', 'ChuaThanhToan', 'DP025'),
('HD026', '2026-05-14 12:30:00', '2026-05-14 13:15:00', N'Hoa don 3 phong VIP - da thanh toan', 9, 15120000.00, 1400000.00, 300000.00, 1622000.00, 17842000.00, 0.00, 'KM010', 'KH006', 'NV010', 'ChuyenKhoan', 'DaThanhToan', 'DP026'),
('HD027', '2026-05-15 12:30:00', NULL, N'Hoa don 2 phong doi tac - chua thanh toan', 4, 7620000.00, 285000.00, 0.00, 790500.00, 8695500.00, 0.00, NULL, 'KH007', 'NV011', 'TienMat', 'ChuaThanhToan', 'DP027'),
('HD028', '2026-05-17 12:30:00', NULL, N'Hoa don 3 phong cao cap - chua thanh toan', 8, 19170000.00, 520000.00, 325000.00, 1936500.00, 21301500.00, 0.00, 'KM011', 'KH008', 'NV012', 'ChuyenKhoan', 'ChuaThanhToan', 'DP028'),
('HD029', '2026-05-20 12:30:00', NULL, N'Hoa don 2 phong tieu chuan - chua thanh toan', 4, 2580000.00, 100000.00, 0.00, 268000.00, 2948000.00, 0.00, NULL, 'KH009', 'NV013', 'TienMat', 'ChuaThanhToan', 'DP029'),
('HD030', '2026-05-22 12:30:00', NULL, N'Hoa don 3 phong hon hop - chua thanh toan', 7, 10260000.00, 610000.00, 350000.00, 1052000.00, 11572000.00, 0.00, 'KM012', 'KH010', 'NV014', 'ChuyenKhoan', 'ChuaThanhToan', 'DP030');
GO

INSERT INTO ChiTietHoaDon (maCTHD, maHD, maPhong, ngayNhanPhong, ngayTraPhong, soDem, phuThu, thanhTien) VALUES
('CTHD021', 'HD021', 'P101', '2026-05-03 14:00:00', '2026-05-05 12:00:00', 2, 0.00, 1020000.00),
('CTHD022', 'HD021', 'P102', '2026-05-03 14:00:00', '2026-05-05 12:00:00', 2, 0.00, 1200000.00),
('CTHD023', 'HD022', 'P103', '2026-05-05 14:00:00', '2026-05-08 12:00:00', 3, 0.00, 2070000.00),
('CTHD024', 'HD022', 'P104', '2026-05-05 14:00:00', '2026-05-08 12:00:00', 3, 0.00, 2340000.00),
('CTHD025', 'HD022', 'P105', '2026-05-05 14:00:00', '2026-05-08 12:00:00', 3, 0.00, 2610000.00),
('CTHD026', 'HD023', 'P106', '2026-05-06 14:00:00', '2026-05-08 12:00:00', 2, 0.00, 1920000.00),
('CTHD027', 'HD023', 'P107', '2026-05-06 14:00:00', '2026-05-08 12:00:00', 2, 0.00, 2100000.00),
('CTHD028', 'HD024', 'P108', '2026-05-08 14:00:00', '2026-05-11 12:00:00', 3, 0.00, 3420000.00),
('CTHD029', 'HD024', 'P109', '2026-05-08 14:00:00', '2026-05-11 12:00:00', 3, 0.00, 3690000.00),
('CTHD030', 'HD024', 'P201', '2026-05-08 14:00:00', '2026-05-11 12:00:00', 3, 0.00, 3960000.00),
('CTHD031', 'HD025', 'P202', '2026-05-10 14:00:00', '2026-05-12 12:00:00', 2, 0.00, 2820000.00),
('CTHD032', 'HD025', 'P203', '2026-05-10 14:00:00', '2026-05-12 12:00:00', 2, 0.00, 3000000.00),
('CTHD033', 'HD026', 'P204', '2026-05-11 14:00:00', '2026-05-14 12:00:00', 3, 0.00, 4770000.00),
('CTHD034', 'HD026', 'P205', '2026-05-11 14:00:00', '2026-05-14 12:00:00', 3, 0.00, 5040000.00),
('CTHD035', 'HD026', 'P206', '2026-05-11 14:00:00', '2026-05-14 12:00:00', 3, 0.00, 5310000.00),
('CTHD036', 'HD027', 'P207', '2026-05-13 14:00:00', '2026-05-15 12:00:00', 2, 0.00, 3720000.00),
('CTHD037', 'HD027', 'P301', '2026-05-13 14:00:00', '2026-05-15 12:00:00', 2, 0.00, 3900000.00),
('CTHD038', 'HD028', 'P302', '2026-05-14 14:00:00', '2026-05-17 12:00:00', 3, 0.00, 6120000.00),
('CTHD039', 'HD028', 'P303', '2026-05-14 14:00:00', '2026-05-17 12:00:00', 3, 0.00, 6390000.00),
('CTHD040', 'HD028', 'P304', '2026-05-14 14:00:00', '2026-05-17 12:00:00', 3, 0.00, 6660000.00),
('CTHD041', 'HD029', 'P101', '2026-05-18 14:00:00', '2026-05-20 12:00:00', 2, 0.00, 1020000.00),
('CTHD042', 'HD029', 'P104', '2026-05-18 14:00:00', '2026-05-20 12:00:00', 2, 0.00, 1560000.00),
('CTHD043', 'HD030', 'P105', '2026-05-19 14:00:00', '2026-05-22 12:00:00', 3, 0.00, 2610000.00),
('CTHD044', 'HD030', 'P108', '2026-05-19 14:00:00', '2026-05-22 12:00:00', 3, 0.00, 3420000.00),
('CTHD045', 'HD030', 'P202', '2026-05-19 14:00:00', '2026-05-22 12:00:00', 3, 0.00, 4230000.00);
GO

INSERT INTO ChiTietDichVu (maCTDV, maHD, maDV, soLuong, donGia, thanhTien, ghiChu) VALUES
('CTDV021', 'HD021', 'DV011', 1, 120000.00, 120000.00, N'Buffet sang cho don 2 phong'),
('CTDV022', 'HD021', 'DV019', 1, 20000.00, 20000.00, N'Bo sung khan'),
('CTDV023', 'HD022', 'DV008', 1, 250000.00, 250000.00, N'Dua don san bay'),
('CTDV024', 'HD022', 'DV011', 1, 120000.00, 120000.00, N'Buffet sang'),
('CTDV025', 'HD023', 'DV006', 2, 40000.00, 80000.00, N'Giat ui'),
('CTDV026', 'HD023', 'DV011', 1, 120000.00, 120000.00, N'Buffet sang'),
('CTDV027', 'HD024', 'DV008', 1, 250000.00, 250000.00, N'Dua don san bay cho doan'),
('CTDV028', 'HD024', 'DV012', 1, 450000.00, 450000.00, N'Spa thu gian'),
('CTDV029', 'HD025', 'DV011', 1, 120000.00, 120000.00, N'Buffet sang'),
('CTDV030', 'HD025', 'DV001', 1, 10000.00, 10000.00, N'Nuoc suoi'),
('CTDV031', 'HD026', 'DV010', 1, 950000.00, 950000.00, N'Thue xe hoi'),
('CTDV032', 'HD026', 'DV012', 1, 450000.00, 450000.00, N'Spa thu gian'),
('CTDV033', 'HD027', 'DV008', 1, 250000.00, 250000.00, N'Dua don san bay'),
('CTDV034', 'HD027', 'DV016', 1, 35000.00, 35000.00, N'Cafe pha may'),
('CTDV035', 'HD028', 'DV012', 1, 450000.00, 450000.00, N'Spa thu gian'),
('CTDV036', 'HD028', 'DV001', 7, 10000.00, 70000.00, N'Nuoc suoi'),
('CTDV037', 'HD029', 'DV015', 1, 90000.00, 90000.00, N'Mini bar'),
('CTDV038', 'HD029', 'DV001', 1, 10000.00, 10000.00, N'Nuoc suoi'),
('CTDV039', 'HD030', 'DV008', 1, 250000.00, 250000.00, N'Dua don san bay'),
('CTDV040', 'HD030', 'DV014', 1, 350000.00, 350000.00, N'Trang tri phong'),
('CTDV041', 'HD030', 'DV001', 1, 10000.00, 10000.00, N'Nuoc suoi');
GO

INSERT INTO ThanhToan (maTT, ngayTT, soTienTT, ghiChu, phuongThucTT, trangThaiTT, maHD) VALUES
('TT021', '2026-05-05 13:15:00', 2596000.00, N'Thanh toan hoa don', 'TienMat', 'ThanhToanThanhCong', 'HD021'),
('TT022', '2026-05-08 12:45:00', 0.00, N'Cho thanh toan hoa don', 'ChuyenKhoan', 'ChoThanhToan', 'HD022'),
('TT023', '2026-05-08 12:45:00', 0.00, N'Cho thanh toan hoa don', 'TienMat', 'ChoThanhToan', 'HD023'),
('TT024', '2026-05-11 12:45:00', 0.00, N'Cho thanh toan hoa don', 'ChuyenKhoan', 'ChoThanhToan', 'HD024'),
('TT025', '2026-05-12 12:45:00', 0.00, N'Cho thanh toan hoa don', 'TienMat', 'ChoThanhToan', 'HD025'),
('TT026', '2026-05-14 13:30:00', 17842000.00, N'Thanh toan hoa don', 'ChuyenKhoan', 'ThanhToanThanhCong', 'HD026'),
('TT027', '2026-05-15 12:45:00', 0.00, N'Cho thanh toan hoa don', 'TienMat', 'ChoThanhToan', 'HD027'),
('TT028', '2026-05-17 12:45:00', 0.00, N'Cho thanh toan hoa don', 'ChuyenKhoan', 'ChoThanhToan', 'HD028'),
('TT029', '2026-05-20 12:45:00', 0.00, N'Cho thanh toan hoa don', 'TienMat', 'ChoThanhToan', 'HD029'),
('TT030', '2026-05-22 12:45:00', 0.00, N'Cho thanh toan hoa don', 'ChuyenKhoan', 'ChoThanhToan', 'HD030');
GO

-- Kiem tra nhanh so luong du lieu
SELECT 'TaiKhoan' AS TenBang, COUNT(*) AS SoDong FROM TaiKhoan;
SELECT 'NhanVien' AS TenBang, COUNT(*) AS SoDong FROM NhanVien;
SELECT 'KhachHang' AS TenBang, COUNT(*) AS SoDong FROM KhachHang;
SELECT 'CaLam' AS TenBang, COUNT(*) AS SoDong FROM CaLam;
SELECT 'PhanCongCa' AS TenBang, COUNT(*) AS SoDong FROM PhanCongCa;
SELECT 'LoaiPhong' AS TenBang, COUNT(*) AS SoDong FROM LoaiPhong;
SELECT 'Phong' AS TenBang, COUNT(*) AS SoDong FROM Phong;
SELECT 'DichVu' AS TenBang, COUNT(*) AS SoDong FROM DichVu;
SELECT 'KhuyenMai' AS TenBang, COUNT(*) AS SoDong FROM KhuyenMai;
SELECT 'DatPhong' AS TenBang, COUNT(*) AS SoDong FROM DatPhong;
SELECT 'ChiTietDatPhong' AS TenBang, COUNT(*) AS SoDong FROM ChiTietDatPhong;
SELECT 'HoaDon' AS TenBang, COUNT(*) AS SoDong FROM HoaDon;
SELECT 'ChiTietHoaDon' AS TenBang, COUNT(*) AS SoDong FROM ChiTietHoaDon;
SELECT 'ChiTietDichVu' AS TenBang, COUNT(*) AS SoDong FROM ChiTietDichVu;
SELECT 'ThanhToan' AS TenBang, COUNT(*) AS SoDong FROM ThanhToan;
GO
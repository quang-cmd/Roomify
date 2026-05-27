SET NOCOUNT ON;

IF DB_ID(N'QLKhachSan') IS NULL CREATE DATABASE QLKhachSan;
GO
USE QLKhachSan;
GO

-- ===== Drop in dependency order =====
IF OBJECT_ID('dbo.ThanhToan', 'U')      IS NOT NULL DROP TABLE dbo.ThanhToan;
IF OBJECT_ID('dbo.ChiTietDichVu', 'U')  IS NOT NULL DROP TABLE dbo.ChiTietDichVu;
IF OBJECT_ID('dbo.ChiTietHoaDon', 'U')  IS NOT NULL DROP TABLE dbo.ChiTietHoaDon;
IF OBJECT_ID('dbo.HoaDon', 'U')         IS NOT NULL DROP TABLE dbo.HoaDon;
IF OBJECT_ID('dbo.ChiTietKhachO', 'U')  IS NOT NULL DROP TABLE dbo.ChiTietKhachO;
IF OBJECT_ID('dbo.ChiTietDatPhong', 'U')IS NOT NULL DROP TABLE dbo.ChiTietDatPhong;
IF OBJECT_ID('dbo.DatPhong', 'U')       IS NOT NULL DROP TABLE dbo.DatPhong;
IF OBJECT_ID('dbo.PhanCongCa', 'U')     IS NOT NULL DROP TABLE dbo.PhanCongCa;
IF OBJECT_ID('dbo.Phong', 'U')          IS NOT NULL DROP TABLE dbo.Phong;
IF OBJECT_ID('dbo.LoaiPhong', 'U')      IS NOT NULL DROP TABLE dbo.LoaiPhong;
IF OBJECT_ID('dbo.KhuyenMai', 'U')      IS NOT NULL DROP TABLE dbo.KhuyenMai;
IF OBJECT_ID('dbo.DichVu', 'U')         IS NOT NULL DROP TABLE dbo.DichVu;
IF OBJECT_ID('dbo.ChiPhi', 'U')         IS NOT NULL DROP TABLE dbo.ChiPhi;
IF OBJECT_ID('dbo.CaLam', 'U')          IS NOT NULL DROP TABLE dbo.CaLam;
IF OBJECT_ID('dbo.KhachHang', 'U')      IS NOT NULL DROP TABLE dbo.KhachHang;
IF OBJECT_ID('dbo.NhanVien', 'U')       IS NOT NULL DROP TABLE dbo.NhanVien;
IF OBJECT_ID('dbo.TaiKhoan', 'U')       IS NOT NULL DROP TABLE dbo.TaiKhoan;
GO

-- =====================================================================
-- 1. Tai khoan / Nhan vien / Khach hang
-- =====================================================================
CREATE TABLE TaiKhoan (
                          tenDangNhap VARCHAR(50)  PRIMARY KEY,
                          matKhau     NVARCHAR(255) NOT NULL,
                          vaiTro      VARCHAR(20)  NOT NULL CHECK (vaiTro IN ('QuanLy', 'NhanVien')),
                          trangThaiTK VARCHAR(20)  NOT NULL DEFAULT 'DangHoatDong'
                              CHECK (trangThaiTK IN ('DangHoatDong', 'NgungHoatDong'))
);
GO

CREATE TABLE NhanVien (
                          maNV         CHAR(5)        PRIMARY KEY,
                          hoTenNV      NVARCHAR(100)  NOT NULL,
                          sdt          VARCHAR(15)    NOT NULL UNIQUE,
                          gioiTinh     BIT            NOT NULL,
                          luong        DECIMAL(18,2)  NOT NULL CHECK (luong >= 0),
                          ngayVao      DATETIME2      NOT NULL,
                          tenDangNhap  VARCHAR(50)    NOT NULL UNIQUE,
                          CONSTRAINT FK_NhanVien_TaiKhoan FOREIGN KEY (tenDangNhap) REFERENCES TaiKhoan(tenDangNhap)
);
GO

CREATE TABLE KhachHang (
                           maKH        CHAR(5)        PRIMARY KEY,
                           hoTenKH     NVARCHAR(100)  NOT NULL,
                           gioiTinh    BIT            NOT NULL DEFAULT 1,
                           ngaySinh    DATETIME2      NULL,
                           email       VARCHAR(100)   NULL,
                           sdt         VARCHAR(15)    NOT NULL UNIQUE,
                           CCCD        VARCHAR(20)    NOT NULL UNIQUE,
                           quocTich    NVARCHAR(50)   NOT NULL DEFAULT N'Viet Nam',
                           diaChi      NVARCHAR(200)  NULL,
                           hangKH      VARCHAR(20)    NOT NULL DEFAULT 'Dong'
                               CHECK (hangKH IN ('Dong', 'Bac', 'Vang', 'KimCuong')),
                           diemTichLuy INT            NOT NULL DEFAULT 0 CHECK (diemTichLuy >= 0)
);
GO

-- =====================================================================
-- 2. Ca lam viec / Phan cong ca
-- =====================================================================
CREATE TABLE CaLam (
                       maCa       CHAR(5)       PRIMARY KEY,
                       gioBatDau  TIME          NOT NULL,
                       gioKetThuc TIME          NOT NULL,
                       ghiChu     NVARCHAR(200) NULL,
                       loaiCa     VARCHAR(20)   NOT NULL CHECK (loaiCa IN ('CaSang', 'CaChieu', 'CaToi'))
);
GO

CREATE TABLE PhanCongCa (
						maPC           CHAR(5)        PRIMARY KEY,
						ngay           DATETIME2      NOT NULL,
						tienMoCa       DECIMAL(18,2)  NOT NULL DEFAULT 0 CHECK (tienMoCa >= 0),
						tienKetCa      DECIMAL(18,2)  NOT NULL DEFAULT 0 CHECK (tienKetCa >= 0),
						thoiGianMoCa   DATETIME2      NULL,
						thoiGianKetCa  DATETIME2      NULL,
						trangThai      NVARCHAR(20)   NOT NULL DEFAULT N'DaPhanCong'
							CHECK (trangThai IN (N'DaPhanCong', N'DangMo', N'DaKet')),
						maNV           CHAR(5)        NOT NULL,
						maCa           CHAR(5)        NOT NULL,
						CONSTRAINT FK_PhanCongCa_NhanVien FOREIGN KEY (maNV) REFERENCES NhanVien(maNV),
						CONSTRAINT FK_PhanCongCa_CaLam    FOREIGN KEY (maCa) REFERENCES CaLam(maCa)
);
GO

CREATE TABLE ChiPhi (
                        maChiPhi   INT IDENTITY(1,1) PRIMARY KEY,
                        loaiChiPhi NVARCHAR(50)      NOT NULL,
                        tenChiPhi  NVARCHAR(255)     NOT NULL,
                        soTien     DECIMAL(18,0)     NOT NULL CHECK (soTien >= 0),
                        ngayChi    DATETIME2         NOT NULL,
                        ghiChu     NVARCHAR(500)     NULL,
                        maNV       CHAR(5)           NOT NULL,
                        maPC       CHAR(5)           NULL,
                        CONSTRAINT FK_ChiPhi_NhanVien  FOREIGN KEY (maNV) REFERENCES NhanVien(maNV),
                        CONSTRAINT FK_ChiPhi_PhanCongCa FOREIGN KEY (maPC) REFERENCES PhanCongCa(maPC)
);
GO

-- =====================================================================
-- 3. Loai phong / Phong
-- =====================================================================
CREATE TABLE LoaiPhong (
                           maLoaiPhong  CHAR(5)        PRIMARY KEY,
                           tenLoaiPhong NVARCHAR(100)  NOT NULL,
                           soLuongPhong INT            NOT NULL CHECK (soLuongPhong >= 0),
                           giaPhong     DECIMAL(18,2)  NOT NULL CHECK (giaPhong >= 0),
                           sucChuaToiDa INT            NOT NULL CHECK (sucChuaToiDa > 0),
                           soTreEmTD    INT            NOT NULL DEFAULT 0 CHECK (soTreEmTD >= 0),
                           dienTich     DECIMAL(10,2)  NOT NULL CHECK (dienTich > 0),
                           moTa         NVARCHAR(255)  NULL,
                           tienNghi     NVARCHAR(255)  NULL
);
GO

CREATE TABLE Phong (
                       maPhong         CHAR(4)     PRIMARY KEY,
                       maLoaiPhong     CHAR(5)     NOT NULL,
                       tang            INT         NOT NULL CHECK (tang > 0),
                       trangThaiPhong  VARCHAR(20) NOT NULL DEFAULT 'Trong'
                           CHECK (trangThaiPhong IN ('Trong', 'DangSuDung', 'BaoTri')),
                       CONSTRAINT FK_Phong_LoaiPhong FOREIGN KEY (maLoaiPhong) REFERENCES LoaiPhong(maLoaiPhong)
);
GO

-- =====================================================================
-- 4. Dich vu / Khuyen mai
-- =====================================================================
CREATE TABLE DichVu (
                        maDV         CHAR(5)        PRIMARY KEY,
                        tenDV        NVARCHAR(100)  NOT NULL,
                        donGia       DECIMAL(18,2)  NOT NULL CHECK (donGia >= 0),
                        loaiDV       NVARCHAR(50)   NULL,
                        moTaDV       NVARCHAR(255)  NULL,
                        trangThaiDV  VARCHAR(20)    NOT NULL DEFAULT 'DangHoatDong'
                            CHECK (trangThaiDV IN ('DangHoatDong', 'NgungHoatDong'))
);
GO

CREATE TABLE KhuyenMai (
                           maKM            CHAR(5)        PRIMARY KEY,
                           tenKM           NVARCHAR(100)  NOT NULL,
                           dieuKienApDung  DECIMAL(18,2)  NOT NULL CHECK (dieuKienApDung >= 0),
                           loaiKM          VARCHAR(20)    NOT NULL CHECK (loaiKM IN ('TheoTien', 'TheoPhanTram')),
                           giaTriToiDa     DECIMAL(18,2)  NOT NULL CHECK (giaTriToiDa >= 0),
                           tienKhuyenMai   DECIMAL(18,2)  NOT NULL CHECK (tienKhuyenMai >= 0),
                           ngayBatDau      DATETIME2      NOT NULL,
                           ngayKetThuc     DATETIME2      NOT NULL,
                           trangThaiKM     VARCHAR(20)    NOT NULL CHECK (trangThaiKM IN ('SapDienRa', 'DangHoatDong', 'HetHan')),
                           CONSTRAINT CK_KhuyenMai_Ngay CHECK (ngayKetThuc > ngayBatDau)
);
GO

-- =====================================================================
-- 5. Dat phong / Chi tiet dat phong
-- =====================================================================
CREATE TABLE DatPhong (
                          maDatPhong      CHAR(5)        PRIMARY KEY,
                          ngayDat         DATETIME2      NOT NULL,
                          tienCoc         DECIMAL(18,2)  NOT NULL DEFAULT 0 CHECK (tienCoc >= 0),
                          trangThaiDatPhong VARCHAR(20)  NOT NULL DEFAULT 'DaDat'
                              CHECK (trangThaiDatPhong IN ('DaDat', 'DangO', 'DaTra', 'DaHuy')),
                          ghiChu          NVARCHAR(255)  NULL,
                          maKH            CHAR(5)        NOT NULL,
                          maNV            CHAR(5)        NOT NULL,
                          CONSTRAINT FK_DatPhong_KhachHang FOREIGN KEY (maKH) REFERENCES KhachHang(maKH),
                          CONSTRAINT FK_DatPhong_NhanVien  FOREIGN KEY (maNV) REFERENCES NhanVien(maNV)
);
GO

CREATE TABLE ChiTietDatPhong (
                                 maDatPhong     CHAR(5)        NOT NULL,
                                 maPhong        CHAR(4)        NOT NULL,
                                 ngayNhanDuKien DATETIME2      NOT NULL,
                                 ngayTraDuKien  DATETIME2      NOT NULL,
                                 donGiaDat      DECIMAL(18,2)  NOT NULL CHECK (donGiaDat >= 0),
                                 soLuongNguoiO  INT            NOT NULL CHECK (soLuongNguoiO > 0),
                                 ghiChu         NVARCHAR(255)  NULL,
                                 CONSTRAINT PK_ChiTietDatPhong PRIMARY KEY (maDatPhong, maPhong),
                                 CONSTRAINT FK_CTDatPhong_DatPhong FOREIGN KEY (maDatPhong) REFERENCES DatPhong(maDatPhong),
                                 CONSTRAINT FK_CTDatPhong_Phong    FOREIGN KEY (maPhong)    REFERENCES Phong(maPhong),
                                 CONSTRAINT CK_CTDatPhong_Ngay CHECK (ngayTraDuKien > ngayNhanDuKien)
);
GO

CREATE TABLE ChiTietKhachO (
    maDatPhong CHAR(5) NOT NULL,
    maPhong    CHAR(4) NOT NULL,
    hoTen      NVARCHAR(100) NOT NULL,
    cccd       VARCHAR(20) NOT NULL,
    sdt        VARCHAR(15) NULL,
    vaiTro     NVARCHAR(50) DEFAULT N'Khách lưu trú',
    CONSTRAINT PK_ChiTietKhachO PRIMARY KEY (maDatPhong, maPhong, cccd),
    CONSTRAINT FK_CTKhachO_CTDatPhong FOREIGN KEY (maDatPhong, maPhong) REFERENCES ChiTietDatPhong(maDatPhong, maPhong)
);
GO

-- =====================================================================
-- 6. Hoa don / Chi tiet hoa don / Chi tiet dich vu / Thanh toan
-- =====================================================================
CREATE TABLE HoaDon (
                        maHD                CHAR(5)        PRIMARY KEY,
                        ngayLapHD           DATETIME2      NOT NULL,
                        ngayThanhToan       DATETIME2      NULL,
                        ghiChu              NVARCHAR(255)  NULL,
                        soLuongNguoiO       INT            NOT NULL CHECK (soLuongNguoiO >= 0),
                        tienPhong           DECIMAL(18,2)  NOT NULL DEFAULT 0 CHECK (tienPhong >= 0),
                        tienDichVu          DECIMAL(18,2)  NOT NULL DEFAULT 0 CHECK (tienDichVu >= 0),
                        tienKhuyenMai       DECIMAL(18,2)  NOT NULL DEFAULT 0 CHECK (tienKhuyenMai >= 0),
                        tienThue            DECIMAL(18,2)  NOT NULL DEFAULT 0 CHECK (tienThue >= 0),
                        tongTienThanhToan   DECIMAL(18,2)  NOT NULL DEFAULT 0 CHECK (tongTienThanhToan >= 0),
                        phiDoiPhong         DECIMAL(18,2)  NOT NULL DEFAULT 0 CHECK (phiDoiPhong >= 0),
                        maKM                CHAR(5)        NULL,
                        maKH                CHAR(5)        NOT NULL,
                        maNV                CHAR(5)        NOT NULL,
                        phuongThucTT        VARCHAR(20)    NOT NULL CHECK (phuongThucTT IN ('TienMat', 'ChuyenKhoan')),
                        trangThai           VARCHAR(20)    NOT NULL DEFAULT 'ChuaThanhToan'
                            CHECK (trangThai IN ('ChuaThanhToan', 'DaThanhToan', 'DaHuy')),
                        maDatPhong          CHAR(5)        NULL,
                        CONSTRAINT FK_HoaDon_KhuyenMai FOREIGN KEY (maKM)        REFERENCES KhuyenMai(maKM),
                        CONSTRAINT FK_HoaDon_KhachHang FOREIGN KEY (maKH)        REFERENCES KhachHang(maKH),
                        CONSTRAINT FK_HoaDon_NhanVien  FOREIGN KEY (maNV)        REFERENCES NhanVien(maNV),
                        CONSTRAINT FK_HoaDon_DatPhong  FOREIGN KEY (maDatPhong)  REFERENCES DatPhong(maDatPhong)
);
GO

CREATE TABLE ChiTietHoaDon (
                               maHD          CHAR(5)        NOT NULL,
                               maPhong       CHAR(4)        NOT NULL,
                               ngayNhanPhong DATETIME2      NOT NULL,    -- Thoi diem khach thuc su check-in
                               ngayTraPhong  DATETIME2      NOT NULL,    -- Thoi diem du kien tra phong
                               ngayTraThucTe DATETIME2      NULL,        -- NULL = chua check-out
                               soDem         INT            NOT NULL CHECK (soDem > 0),
                               phuThu        DECIMAL(18,2)  NOT NULL DEFAULT 0 CHECK (phuThu >= 0),
                               phiPhat       DECIMAL(18,2)  NOT NULL DEFAULT 0 CHECK (phiPhat >= 0),
                               thanhTien     DECIMAL(18,2)  NOT NULL CHECK (thanhTien >= 0),
                               CONSTRAINT PK_ChiTietHoaDon PRIMARY KEY (maHD, maPhong),
                               CONSTRAINT FK_CTHoaDon_HoaDon FOREIGN KEY (maHD)    REFERENCES HoaDon(maHD),
                               CONSTRAINT FK_CTHoaDon_Phong  FOREIGN KEY (maPhong) REFERENCES Phong(maPhong),
                               CONSTRAINT CK_CTHoaDon_Ngay CHECK (ngayTraPhong > ngayNhanPhong)
);
GO

CREATE TABLE ChiTietDichVu (
                               maCTDV    CHAR(8)        PRIMARY KEY,
                               maHD      CHAR(5)        NOT NULL,
                               maDV      CHAR(5)        NOT NULL,
                               soLuong   INT            NOT NULL CHECK (soLuong > 0),
                               donGia    DECIMAL(18,2)  NOT NULL CHECK (donGia >= 0),
                               thanhTien DECIMAL(18,2)  NOT NULL CHECK (thanhTien >= 0),
                               ghiChu    NVARCHAR(255)  NULL,
                               CONSTRAINT FK_CTDichVu_HoaDon FOREIGN KEY (maHD) REFERENCES HoaDon(maHD),
                               CONSTRAINT FK_CTDichVu_DichVu FOREIGN KEY (maDV) REFERENCES DichVu(maDV)
);
GO

CREATE TABLE ThanhToan (
                           maTT          CHAR(5)        PRIMARY KEY,
                           ngayTT        DATETIME2      NOT NULL,
                           soTienTT      DECIMAL(18,2)  NOT NULL CHECK (soTienTT >= 0),
                           ghiChu        NVARCHAR(255)  NULL,
                           phuongThucTT  VARCHAR(20)    NOT NULL CHECK (phuongThucTT IN ('TienMat', 'ChuyenKhoan')),
                           trangThaiTT   VARCHAR(30)    NOT NULL DEFAULT 'ThanhToanThanhCong'
                               CHECK (trangThaiTT IN ('ChoThanhToan', 'ThanhToanThanhCong', 'ThanhToanThatBai', 'DaHuy')),
                           maHD          CHAR(5)        NOT NULL,
                           maPC          CHAR(5)        NULL,         -- Ca lam thu tien (de doi soat ca)
                           maNV          CHAR(5)        NULL,         -- Le tan thu tien
                           loaiGD        VARCHAR(10)    NOT NULL DEFAULT 'Thu' CONSTRAINT CK_ThanhToan_loaiGD CHECK (loaiGD IN ('Thu', 'HoanTien')),
                           CONSTRAINT FK_ThanhToan_HoaDon     FOREIGN KEY (maHD) REFERENCES HoaDon(maHD),
                           CONSTRAINT FK_ThanhToan_PhanCongCa FOREIGN KEY (maPC) REFERENCES PhanCongCa(maPC),
                           CONSTRAINT FK_ThanhToan_NhanVien   FOREIGN KEY (maNV) REFERENCES NhanVien(maNV)
);
GO

-- =====================================================================
-- 7. Indexes (cho cac query thuong xuyen)
-- =====================================================================
CREATE INDEX IX_PhanCongCa_maNV_ngay ON PhanCongCa(maNV, ngay);
CREATE INDEX IX_Phong_maLoaiPhong    ON Phong(maLoaiPhong);
CREATE INDEX IX_Phong_trangThai      ON Phong(trangThaiPhong);
CREATE INDEX IX_DatPhong_maKH        ON DatPhong(maKH);
CREATE INDEX IX_DatPhong_ngayDat     ON DatPhong(ngayDat);
CREATE INDEX IX_DatPhong_trangThai   ON DatPhong(trangThaiDatPhong);
CREATE INDEX IX_CTDP_maPhong         ON ChiTietDatPhong(maPhong);
CREATE INDEX IX_CTDP_ngayNhan        ON ChiTietDatPhong(ngayNhanDuKien, ngayTraDuKien);
CREATE INDEX IX_HoaDon_maKH          ON HoaDon(maKH);
CREATE INDEX IX_HoaDon_maDatPhong    ON HoaDon(maDatPhong);
CREATE UNIQUE INDEX UX_HoaDon_maDatPhong ON HoaDon(maDatPhong) WHERE maDatPhong IS NOT NULL;
CREATE INDEX IX_HoaDon_trangThai     ON HoaDon(trangThai);
CREATE INDEX IX_CTHD_maPhong         ON ChiTietHoaDon(maPhong);
CREATE INDEX IX_CTHD_ngayNhan        ON ChiTietHoaDon(ngayNhanPhong);
CREATE INDEX IX_CTDV_maHD            ON ChiTietDichVu(maHD);
CREATE INDEX IX_ThanhToan_maHD       ON ThanhToan(maHD);
CREATE INDEX IX_ThanhToan_maPC       ON ThanhToan(maPC);
CREATE INDEX IX_ChiPhi_ngayChi       ON ChiPhi(ngayChi);
CREATE INDEX IX_ChiPhi_loaiChiPhi    ON ChiPhi(loaiChiPhi);
CREATE INDEX IX_ChiPhi_maPC          ON ChiPhi(maPC);

-- Email khong dung lam dinh danh duy nhat vi khach co the dat ho nguoi than.
-- Chi tao index thuong de ho tro tim kiem/tra cuu khi can.
CREATE INDEX IX_KhachHang_email ON KhachHang(email) WHERE email IS NOT NULL;
GO

-- =====================================================================
-- 8. DU LIEU MAU - Anchor demo = 2026-05-27, ngay mai demo = 2026-05-28
-- =====================================================================

-- ----- TaiKhoan + NhanVien (1 quan ly + 4 le tan) -----
INSERT INTO TaiKhoan (tenDangNhap, matKhau, vaiTro, trangThaiTK) VALUES
('admin',  N'Hash@admin',  'QuanLy',   'DangHoatDong'),
('letan1', N'Hash@letan1', 'NhanVien', 'DangHoatDong'),
('letan2', N'Hash@letan2', 'NhanVien', 'DangHoatDong'),
('letan3', N'Hash@letan3', 'NhanVien', 'DangHoatDong'),
('letan4', N'Hash@letan4', 'NhanVien', 'DangHoatDong');
GO

INSERT INTO NhanVien (maNV, hoTenNV, sdt, gioiTinh, luong, ngayVao, tenDangNhap) VALUES
('NV001', N'Nguyen Khanh Luan',  '0901000001', 0, 18000000.00, '2024-01-15', 'admin'),
('NV002', N'Tran Thi Mai Anh',   '0901000002', 1, 10000000.00, '2024-03-01', 'letan1'),
('NV003', N'Le Hoang Phuc',      '0901000003', 0,  9500000.00, '2024-06-10', 'letan2'),
('NV004', N'Pham Thu Trang',     '0901000004', 1,  9000000.00, '2025-01-20', 'letan3'),
('NV005', N'Vo Minh Tuan',       '0901000005', 0,  9000000.00, '2025-09-05', 'letan4');
GO

-- ----- KhachHang (8 khach phu cho ~10 booking) -----
INSERT INTO KhachHang (maKH, hoTenKH, gioiTinh, ngaySinh, email, sdt, CCCD, quocTich, diaChi, hangKH, diemTichLuy) VALUES
('KH001', N'Nguyen Van An',   1, '1990-05-12', 'an.nguyen@example.com',     '0820000001', '079090000001', N'Viet Nam',  N'Ha Noi',     'Vang',     1200),
('KH002', N'Tran Thi Bich',   0, '1992-08-20', 'bich.tran@example.com',     '0820000002', '079092000002', N'Viet Nam',  N'Da Nang',    'Bac',       650),
('KH003', N'Le Quoc Cuong',   1, '1988-11-03', 'cuong.le@example.com',      '0820000003', '079088000003', N'Viet Nam',  N'TP HCM',     'KimCuong', 2200),
('KH004', N'Pham Thu Dung',   0, '1995-02-14', NULL,                        '0820000004', '079095000004', N'Viet Nam',  N'Can Tho',    'Dong',      120),
('KH005', N'Vo Minh Duc',     1, '1985-07-09', 'duc.vo@example.com',        '0820000005', '079085000005', N'Viet Nam',  N'Nha Trang',  'Vang',     1450),
('KH006', N'Bui Ngoc Giang',  0, '1998-12-25', NULL,                        '0820000006', '079098000006', N'Viet Nam',  N'Ha Noi',     'Dong',      300),
('KH007', N'Hoang Yen Nhi',   0, '1993-04-18', 'nhi.hoang@example.com',     '0820000007', '079093000007', N'Han Quoc',  N'TP HCM',     'Bac',       780),
('KH008', N'Phan Tuan Khang', 1, '1987-09-30', 'khang.phan@example.com',    '0820000008', '079087000008', N'Viet Nam',  N'Da Nang',    'KimCuong', 3100);
GO

-- ----- CaLam + PhanCongCa (3 ca chuan, 5 phan cong gan ngay) -----
INSERT INTO CaLam (maCa, gioBatDau, gioKetThuc, ghiChu, loaiCa) VALUES
('CA001', '06:00:00', '14:00:00', N'Ca sang', 'CaSang'),
('CA002', '14:00:00', '22:00:00', N'Ca chieu','CaChieu'),
('CA003', '22:00:00', '06:00:00', N'Ca toi',  'CaToi');
GO

INSERT INTO PhanCongCa (maPC, ngay, tienMoCa, tienKetCa,thoiGianMoCa, thoiGianKetCa, trangThai, maNV, maCa) VALUES
('PC001', '2026-04-23', 500000.00, 3500000.00, '2026-04-23 06:00:00', '2026-04-23 14:00:00', N'DaKet',      'NV002', 'CA001'),
('PC002', '2026-04-23', 500000.00, 4200000.00, '2026-04-23 14:00:00', '2026-04-23 22:00:00', N'DaKet',      'NV003', 'CA002'),
('PC003', '2026-04-24', 500000.00, 3800000.00, '2026-04-24 06:00:00', '2026-04-24 14:00:00', N'DaKet',      'NV004', 'CA001'),
('PC004', '2026-04-24', 500000.00, 4500000.00, '2026-04-24 14:00:00', '2026-04-24 22:00:00', N'DaKet',      'NV005', 'CA002'),
-- ca mau cu da qua ngay demo, khong de DangMo/DaPhanCong de tranh dashboard nhan nham la ca hien tai
('PC005', '2026-04-25', 500000.00, 3000000.00, '2026-04-25 06:00:00', '2026-04-25 14:00:00', N'DaKet',      'NV002', 'CA001'),
('PC006', '2026-04-25', 0.00,      0.00,       NULL,                  NULL,                  N'DaKet',      'NV003', 'CA002'),
('PC007', '2026-04-25', 0.00,      0.00,       NULL,                  NULL,                  N'DaKet',      'NV004', 'CA003');
GO

-- ----- LoaiPhong (6 loai) + Phong (12 phong) -----
INSERT INTO LoaiPhong (maLoaiPhong, tenLoaiPhong, soLuongPhong, giaPhong, sucChuaToiDa, soTreEmTD, dienTich, moTa, tienNghi) VALUES
('LP001', N'Standard',  2,  500000.00, 2, 1, 22.00, N'Phong tieu chuan 1 giuong doi',          N'Wifi, Máy lạnh, TV'),
('LP002', N'Superior',  2,  750000.00, 2, 1, 26.00, N'Phong superior co cua so',                N'Wifi, Máy lạnh, TV, Minibar'),
('LP003', N'Deluxe',    2, 1000000.00, 3, 1, 32.00, N'Phong deluxe huong thanh pho',            N'Wifi, Máy lạnh, TV, Minibar, Bồn tắm'),
('LP004', N'Family',    2, 1500000.00, 4, 2, 42.00, N'Phong gia dinh 2 giuong',                 N'Wifi, Máy lạnh, TV, Minibar, Bồn tắm, Bep nho'),
('LP005', N'Suite',     2, 2200000.00, 4, 2, 55.00, N'Suite cao cap voi phong khach rieng',     N'Wifi, Máy lạnh, TV, Minibar, Bồn tắm, Phong khach'),
('LP006', N'VIP',       2, 3500000.00, 6, 3, 80.00, N'Phong VIP huong bien, view dep nhat',     N'Full tiện nghi, Bồn tắm jacuzzi, Sky bar');
GO

INSERT INTO Phong (maPhong, maLoaiPhong, tang, trangThaiPhong) VALUES
('P101', 'LP001', 1, 'Trong'),
('P102', 'LP001', 1, 'Trong'),
('P103', 'LP002', 1, 'Trong'),
('P104', 'LP002', 1, 'BaoTri'),       -- 1 phong dang bao tri de demo loc
('P201', 'LP003', 2, 'DangSuDung'),   -- khach DP004 dang o
('P202', 'LP003', 2, 'Trong'),
('P203', 'LP004', 2, 'DangSuDung'),   -- khach DP005 dang o
('P204', 'LP004', 2, 'Trong'),
('P301', 'LP005', 3, 'Trong'),
('P302', 'LP005', 3, 'Trong'),
('P401', 'LP006', 4, 'Trong'),
('P402', 'LP006', 4, 'Trong');
GO

-- ----- DichVu (5) + KhuyenMai (3) -----
INSERT INTO DichVu (maDV, tenDV, donGia, loaiDV, moTaDV, trangThaiDV) VALUES
('DV001', N'Ăn sáng buffet',      120000.00, N'Food & Drink', N'Buffet sáng theo người',         'DangHoatDong'),
('DV002', N'Giặt ủi',              60000.00, N'Housekeeping', N'Giặt ủi theo kg',                'DangHoatDong'),
('DV003', N'Dọn phòng thêm',       80000.00, N'Housekeeping', N'Dọn phòng ngoài giờ',            'DangHoatDong'),
('DV004', N'Nước minibar',         30000.00, N'Food & Drink', N'Tính theo số chai tiêu thụ',     'DangHoatDong'),
('DV005', N'Đưa đón sân bay',     250000.00, N'Transport',    N'Xe riêng đưa đón sân bay',       'DangHoatDong');
GO

INSERT INTO KhuyenMai (maKM, tenKM, dieuKienApDung, loaiKM, giaTriToiDa, tienKhuyenMai, ngayBatDau, ngayKetThuc, trangThaiKM) VALUES
('KM001', N'Tet 2026',          15000000.00,  'TheoTien',     500000.00,  500000.00, '2026-01-15','2026-02-28','HetHan'),
('KM002', N'He 2026',           3000000.00,  'TheoPhanTram', 600000.00,  30, '2026-04-01','2026-06-30','DangHoatDong'),
('KM003', N'Trung Thu 2026',    4000000.00,  'TheoTien',     800000.00,  400000.00, '2026-08-15','2026-09-30','SapDienRa');
GO

-- =====================================================================
-- 9. PIPELINE BOOKING (10 bookings phu 5 giai doan)
--   DP001-DP002 : Stage 4 - Hoan tat (da check-out, da thanh toan du)
--   DP003       : Stage 3 - Da check-out, cho thanh toan phan con lai
--   DP004-DP005 : Stage 2 - Dang luu tru (Phong DangSuDung)
--   DP006-DP008 : Stage 1 - Da dat coc, chua check-in
--   DP009       : Stage 5 - Da huy
--   DP010       : Stage 4 dac biet - Thanh toan 100% ngay khi dat
-- =====================================================================

INSERT INTO DatPhong (maDatPhong, ngayDat, tienCoc, trangThaiDatPhong, ghiChu, maKH, maNV) VALUES
-- Stage 4: hoan tat
('DP001', '2026-04-10 09:00',  450000.00, 'DaTra', N'Booking demo da hoan tat',    'KH001', 'NV002'),
('DP002', '2026-04-12 10:00',  675000.00, 'DaTra', N'Booking 2 phong da hoan tat', 'KH002', 'NV003'),
-- Stage 3: da tra phong, chua thanh toan du
('DP003', '2026-04-18 09:00',  450000.00, 'DaTra', N'Khach da tra, no phan con lai','KH004', 'NV004'),
-- Stage 2: dang luu tru
('DP004', '2026-04-20 11:00',  900000.00, 'DangO', N'Khach dang luu tru',          'KH005', 'NV002'),
('DP005', '2026-04-21 14:00', 1350000.00, 'DangO', N'Khach gia dinh dang o',       'KH008', 'NV003'),
-- Stage 1: dat coc, chua check-in
('DP006', '2026-04-23 16:00',  450000.00, 'DaDat', N'Da dat coc - check-in tuong lai','KH003','NV004'),
('DP007', '2026-04-24 09:00', 1500000.00, 'DaDat', N'Dat phong VIP cho ky nghi',    'KH006', 'NV002'),
('DP008', '2026-04-25 10:00',  675000.00, 'DaDat', N'Booking xa - le 30/4',         'KH007', 'NV005'),
-- Stage 5: huy
('DP009', '2026-04-15 11:00',  300000.00, 'DaHuy', N'Khach huy do thay doi lich',   'KH001', 'NV003'),
-- Stage 4 (full pay khong qua coc)
('DP010', '2026-04-19 13:00',       0.00, 'DaTra', N'Walk-in thanh toan 100% ngay', 'KH004', 'NV002');
GO

INSERT INTO ChiTietDatPhong (maDatPhong, maPhong, ngayNhanDuKien, ngayTraDuKien, donGiaDat, soLuongNguoiO, ghiChu) VALUES
-- DP001: 1 Standard
('DP001', 'P101', '2026-04-15 14:00', '2026-04-18 12:00',  500000.00, 2, NULL),
-- DP002: 2 phong (Superior + Deluxe)
('DP002', 'P103', '2026-04-17 14:00', '2026-04-20 12:00',  750000.00, 2, NULL),
('DP002', 'P201', '2026-04-17 14:00', '2026-04-20 12:00', 1000000.00, 3, NULL),
-- DP003: 1 Deluxe
('DP003', 'P202', '2026-04-22 14:00', '2026-04-24 12:00', 1000000.00, 2, NULL),
-- DP004: 1 Deluxe (P201 - DangSuDung)
('DP004', 'P201', '2026-04-23 14:00', '2026-04-27 12:00', 1000000.00, 2, NULL),
-- DP005: 1 Family (P203 - DangSuDung)
('DP005', 'P203', '2026-04-24 14:00', '2026-04-28 12:00', 1500000.00, 4, NULL),
-- DP006: 1 Standard
('DP006', 'P102', '2026-04-27 14:00', '2026-04-30 12:00',  500000.00, 2, NULL),
-- DP007: 1 VIP
('DP007', 'P401', '2026-04-28 14:00', '2026-05-02 12:00', 3500000.00, 4, NULL),
-- DP008: 1 Suite
('DP008', 'P301', '2026-05-05 14:00', '2026-05-08 12:00', 2200000.00, 3, NULL),
-- DP009: 1 Standard (huy)
('DP009', 'P102', '2026-04-22 14:00', '2026-04-25 12:00',  500000.00, 2, NULL),
-- DP010: 1 Superior
('DP010', 'P103', '2026-04-19 14:00', '2026-04-21 12:00',  750000.00, 2, NULL);
GO

INSERT INTO ChiTietKhachO (maDatPhong, maPhong, hoTen, cccd, sdt, vaiTro) VALUES
-- DP004 - Phong P201 co 2 khach (KhachHang 5 la chu)
('DP004', 'P201', N'Vo Minh Duc', '079085000005', '0820000005', N'Người đại diện'),
('DP004', 'P201', N'Le Thu Ha', '079085000099', '0912345678', N'Khách lưu trú'),
-- DP005 - Phong P203 co 4 khach (KhachHang 8 la chu)
('DP005', 'P203', N'Phan Tuan Khang', '079087000008', '0820000008', N'Người đại diện'),
('DP005', 'P203', N'Nguyen Thi Bao', '079087000011', '0901111111', N'Khách lưu trú'),
('DP005', 'P203', N'Phan Tuan Kiet', '079087000022', NULL, N'Khách lưu trú (Trẻ em)'),
('DP005', 'P203', N'Phan Bao Ngoc', '079087000033', NULL, N'Khách lưu trú (Trẻ em)');
GO

-- ----- HoaDon -----
-- Cong thuc: tongTienThanhToan = tienPhong + tienDichVu - tienKhuyenMai + tienThue + phiDoiPhong
-- tienThue = 10% * (tienPhong + tienDichVu - tienKhuyenMai)
INSERT INTO HoaDon (maHD, ngayLapHD, ngayThanhToan, ghiChu, soLuongNguoiO, tienPhong, tienDichVu, tienKhuyenMai, tienThue, tongTienThanhToan, phiDoiPhong, maKM, maKH, maNV, phuongThucTT, trangThai, maDatPhong) VALUES
-- DP001: 3 dem * 500k = 1.5tr + DV 120k - KM 0 + thue 162k = 1.782tr
('HD001', '2026-04-10 09:00', '2026-04-18 12:30', N'Hoan tat',
 2, 1500000.00, 120000.00,      0.00, 162000.00, 1782000.00, 0.00, NULL,    'KH001', 'NV002', 'TienMat',     'DaThanhToan',   'DP001'),
-- DP002: (3*750+3*1000)=5.25tr + DV 60k - KM 300k = 5.01 -> thue 501k
('HD002', '2026-04-12 10:00', '2026-04-20 12:30', N'Hoan tat 2 phong',
 5, 5250000.00,  60000.00, 300000.00, 501000.00, 5511000.00, 0.00, 'KM002', 'KH002', 'NV003', 'ChuyenKhoan', 'DaThanhToan',   'DP002'),
-- DP003: 2 dem * 1000 = 2tr + DV 80k = 2.08, thue 208k -> 2.288tr
('HD003', '2026-04-18 09:00', NULL,                 N'Cho thanh toan phan con lai',
 2, 2000000.00,  80000.00,      0.00, 208000.00, 2288000.00, 0.00, NULL,    'KH004', 'NV004', 'TienMat',     'ChuaThanhToan', 'DP003'),
-- DP004: 4 dem * 1000 = 4tr (chua dung dich vu)
('HD004', '2026-04-20 11:00', NULL,                 N'Khach dang luu tru',
 2, 4000000.00,      0.00,      0.00, 400000.00, 4400000.00, 0.00, NULL,    'KH005', 'NV002', 'TienMat',     'ChuaThanhToan', 'DP004'),
-- DP005: 4 dem * 1500 = 6tr + DV 240k - KM 300k = 5.94 -> thue 594k
('HD005', '2026-04-21 14:00', NULL,                 N'Khach gia dinh',
 4, 6000000.00, 240000.00, 300000.00, 594000.00, 6534000.00, 0.00, 'KM002', 'KH008', 'NV003', 'ChuyenKhoan', 'ChuaThanhToan', 'DP005'),
-- DP006: 3 dem * 500 = 1.5tr -> thue 150k = 1.65tr
('HD006', '2026-04-23 16:00', NULL,                 N'Da dat coc 30%',
 2, 1500000.00,      0.00,      0.00, 150000.00, 1650000.00, 0.00, NULL,    'KH003', 'NV004', 'ChuyenKhoan', 'ChuaThanhToan', 'DP006'),
-- DP007: 4 dem * 3500 = 14tr - KM 600k = 13.4 -> thue 1.34tr
('HD007', '2026-04-24 09:00', NULL,                 N'Da dat coc - VIP',
 4, 14000000.00,     0.00, 600000.00, 1340000.00, 14740000.00, 0.00, 'KM002', 'KH006', 'NV002', 'ChuyenKhoan', 'ChuaThanhToan', 'DP007'),
-- DP008: 3 dem * 2200 = 6.6tr - KM 600k = 6 -> thue 600k = 6.6tr
('HD008', '2026-04-25 10:00', NULL,                 N'Da dat coc - le',
 3, 6600000.00,      0.00, 600000.00, 600000.00, 6600000.00, 0.00, 'KM002', 'KH007', 'NV005', 'TienMat',     'ChuaThanhToan', 'DP008'),
-- DP009: huy - tongTien giu so cu, trangThai = DaHuy
('HD009', '2026-04-15 11:00', NULL,                 N'Da huy',
 2, 1500000.00,      0.00,      0.00, 150000.00, 1650000.00, 0.00, NULL,    'KH001', 'NV003', 'TienMat',     'DaHuy',         'DP009'),
-- DP010: 2 dem * 750 = 1.5tr + DV 30k = 1.53 -> thue 153k
('HD010', '2026-04-19 13:00', '2026-04-19 13:30', N'Walk-in tra full',
 2, 1500000.00,  30000.00,      0.00, 153000.00, 1683000.00, 0.00, NULL,    'KH004', 'NV002', 'TienMat',     'DaThanhToan',   'DP010');
GO

-- ----- ChiTietHoaDon (chi tao cho booking da check-in: DP001-DP005, DP010) -----
INSERT INTO ChiTietHoaDon (maHD, maPhong, ngayNhanPhong, ngayTraPhong, ngayTraThucTe, soDem, phuThu, thanhTien) VALUES
-- DP001 (da tra)
('HD001', 'P101', '2026-04-15 14:30', '2026-04-18 12:00', '2026-04-18 11:45', 3, 0.00, 1500000.00),
-- DP002 (da tra, 2 phong)
('HD002', 'P103', '2026-04-17 14:00', '2026-04-20 12:00', '2026-04-20 11:30', 3, 0.00, 2250000.00),
('HD002', 'P201', '2026-04-17 14:15', '2026-04-20 12:00', '2026-04-20 11:30', 3, 0.00, 3000000.00),
-- DP003 (da tra, cho TT)
('HD003', 'P202', '2026-04-22 14:00', '2026-04-24 12:00', '2026-04-24 11:50', 2, 0.00, 2000000.00),
-- DP004 (dang o, ngayTraThucTe NULL)
('HD004', 'P201', '2026-04-23 14:30', '2026-04-27 12:00', NULL,                4, 0.00, 4000000.00),
-- DP005 (dang o)
('HD005', 'P203', '2026-04-24 14:00', '2026-04-28 12:00', NULL,                4, 0.00, 6000000.00),
-- DP010 (da tra)
('HD010', 'P103', '2026-04-19 14:00', '2026-04-21 12:00', '2026-04-21 11:30', 2, 0.00, 1500000.00);
GO

-- ----- ChiTietDichVu (random - DP1, DP2, DP3, DP5, DP10) -----
INSERT INTO ChiTietDichVu (maCTDV, maHD, maDV, soLuong, donGia, thanhTien, ghiChu) VALUES
('CTDV0001', 'HD001', 'DV001', 1, 120000.00, 120000.00, N'1 buffet sang'),
('CTDV0002', 'HD002', 'DV002', 1,  60000.00,  60000.00, N'Giat ui mot lan'),
('CTDV0003', 'HD003', 'DV003', 1,  80000.00,  80000.00, N'Don phong them'),
('CTDV0004', 'HD005', 'DV001', 2, 120000.00, 240000.00, N'Buffet sang gia dinh'),
('CTDV0005', 'HD010', 'DV004', 1,  30000.00,  30000.00, N'1 chai nuoc minibar');
GO

-- ----- ThanhToan -----
-- Quy tac: moi giao dich tien (coc / phan con lai / thanh toan full) tao 1 record.
-- Booking huy van co record ThanhToan cua cọc da hoan tra (DaHuy).
INSERT INTO ThanhToan (maTT, ngayTT, soTienTT, ghiChu, phuongThucTT, trangThaiTT, maHD, maPC, maNV, loaiGD) VALUES
-- DP001: coc 30% (450k) + phan con lai (1.332tr)
('TT001', '2026-04-10 09:15',  450000.00, N'Dat coc 30%',          'TienMat',     'ThanhToanThanhCong', 'HD001', NULL,    'NV002', 'Thu'),
('TT002', '2026-04-18 12:25', 1332000.00, N'Thanh toan phan con lai','TienMat',   'ThanhToanThanhCong', 'HD001', 'PC001', 'NV002', 'Thu'),
-- DP002: coc 30% + phan con lai
('TT003', '2026-04-12 10:15',  675000.00, N'Dat coc 30%',          'ChuyenKhoan', 'ThanhToanThanhCong', 'HD002', NULL,    'NV003', 'Thu'),
('TT004', '2026-04-20 12:25', 4836000.00, N'Thanh toan phan con lai','ChuyenKhoan','ThanhToanThanhCong', 'HD002', 'PC002', 'NV003', 'Thu'),
-- DP003: chi co coc, chua tra phan con lai
('TT005', '2026-04-18 09:15',  450000.00, N'Dat coc 30%',          'TienMat',     'ThanhToanThanhCong', 'HD003', NULL,    'NV004', 'Thu'),
-- DP004: chi co coc
('TT006', '2026-04-20 11:15',  900000.00, N'Dat coc 30%',          'TienMat',     'ThanhToanThanhCong', 'HD004', NULL,    'NV002', 'Thu'),
-- DP005: chi co coc
('TT007', '2026-04-21 14:15', 1350000.00, N'Dat coc 30%',          'ChuyenKhoan', 'ThanhToanThanhCong', 'HD005', NULL,    'NV003', 'Thu'),
-- DP006-DP008: coc cho future bookings
('TT008', '2026-04-23 16:15',  450000.00, N'Dat coc 30%',          'ChuyenKhoan', 'ThanhToanThanhCong', 'HD006', 'PC002', 'NV004', 'Thu'),
('TT009', '2026-04-24 09:15', 1500000.00, N'Dat coc 30%',          'ChuyenKhoan', 'ThanhToanThanhCong', 'HD007', 'PC003', 'NV002', 'Thu'),
('TT010', '2026-04-25 10:15',  675000.00, N'Dat coc 30%',          'TienMat',     'ThanhToanThanhCong', 'HD008', 'PC005', 'NV005', 'Thu'),
-- DP009: coc da hoan tra (huy)
('TT011', '2026-04-15 11:15',  300000.00, N'Dat coc 30%',          'TienMat',     'ThanhToanThanhCong', 'HD009', NULL,    'NV003', 'Thu'),
('TT012', '2026-04-22 09:00',  300000.00, N'Hoan tien coc do huy', 'TienMat',     'DaHuy',              'HD009', NULL,    'NV003', 'HoanTien'),
-- DP010: thanh toan 100% ngay khi dat
('TT013', '2026-04-19 13:25', 1683000.00, N'Thanh toan 100%',      'TienMat',     'ThanhToanThanhCong', 'HD010', NULL,    'NV002', 'Thu');
GO

-- ----- ChiPhi -----
INSERT INTO ChiPhi (loaiChiPhi, tenChiPhi, soTien, ngayChi, ghiChu, maNV, maPC) VALUES
(N'Dien nuoc', N'Tien dien thang 4',          3200000, '2026-04-25 09:00', N'Thanh toan hoa don dien',       'NV002', 'PC005'),
(N'Dien nuoc', N'Tien nuoc thang 4',          1150000, '2026-04-25 09:10', N'Thanh toan hoa don nuoc',       'NV002', 'PC005'),
(N'Vat tu',    N'Mua khan tam va ga giuong',  2800000, '2026-04-18 15:30', N'Bo sung vat tu phong',          'NV004', NULL),
(N'Vat tu',    N'Mua do dung ve sinh',         950000, '2026-04-20 10:00', N'Nuoc lau san, xa phong, tui rac','NV002', NULL),
(N'Khac',      N'Sua may lanh phong P104',    1800000, '2026-04-22 14:00', N'Bao tri phong dang sua chua',   'NV003', NULL);
GO

-- =====================================================================
-- 9. DEMO SEED BO SUNG - chay demo ngay 2026-05-28
--   - Mo rong len 50 phong.
--   - Bo sung booking cho ngay mai 28/05/2026 de demo Nhan phong.
--   - Bo sung hoa don/thanh toan thang 2, 3, 4 de dashboard/thong ke co du lieu.
-- =====================================================================

UPDATE LoaiPhong SET soLuongPhong = 10 WHERE maLoaiPhong IN ('LP001', 'LP002', 'LP003');
UPDATE LoaiPhong SET soLuongPhong = 8  WHERE maLoaiPhong = 'LP004';
UPDATE LoaiPhong SET soLuongPhong = 7  WHERE maLoaiPhong = 'LP005';
UPDATE LoaiPhong SET soLuongPhong = 5  WHERE maLoaiPhong = 'LP006';

-- Dua cac phong mau cu ve trang thai phu hop voi moc demo hien tai.
UPDATE Phong SET trangThaiPhong = 'Trong' WHERE maPhong IN ('P201', 'P203');
UPDATE Phong SET trangThaiPhong = 'BaoTri' WHERE maPhong = 'P104';

-- Chuan hoa cac booking mau cu da qua ngay tra phong theo moc demo 27/05/2026.
UPDATE DatPhong SET trangThaiDatPhong = 'DaTra' WHERE maDatPhong IN ('DP004', 'DP005');
UPDATE HoaDon SET trangThai = 'DaThanhToan', ngayThanhToan = '2026-04-27 11:50' WHERE maHD = 'HD004';
UPDATE HoaDon SET trangThai = 'DaThanhToan', ngayThanhToan = '2026-04-28 11:40' WHERE maHD = 'HD005';
UPDATE ChiTietHoaDon SET ngayTraThucTe = '2026-04-27 11:50' WHERE maHD = 'HD004';
UPDATE ChiTietHoaDon SET ngayTraThucTe = '2026-04-28 11:40' WHERE maHD = 'HD005';

INSERT INTO Phong (maPhong, maLoaiPhong, tang, trangThaiPhong) VALUES
-- Standard: P101-P102 da co san, bo sung P105-P112
('P105', 'LP001', 1, 'Trong'),
('P106', 'LP001', 1, 'Trong'),
('P107', 'LP001', 1, 'Trong'),
('P108', 'LP001', 1, 'Trong'),
('P109', 'LP001', 1, 'Trong'),
('P110', 'LP001', 1, 'Trong'),
('P111', 'LP001', 1, 'Trong'),
('P112', 'LP001', 1, 'Trong'),
-- Superior: P103-P104 da co san, bo sung P113-P120
('P113', 'LP002', 1, 'Trong'),
('P114', 'LP002', 1, 'Trong'),
('P115', 'LP002', 1, 'Trong'),
('P116', 'LP002', 1, 'Trong'),
('P117', 'LP002', 1, 'Trong'),
('P118', 'LP002', 1, 'Trong'),
('P119', 'LP002', 1, 'Trong'),
('P120', 'LP002', 1, 'Trong'),
-- Deluxe: P201-P202 da co san, bo sung P205-P212
('P205', 'LP003', 2, 'Trong'),
('P206', 'LP003', 2, 'Trong'),
('P207', 'LP003', 2, 'Trong'),
('P208', 'LP003', 2, 'Trong'),
('P209', 'LP003', 2, 'Trong'),
('P210', 'LP003', 2, 'Trong'),
('P211', 'LP003', 2, 'Trong'),
('P212', 'LP003', 2, 'Trong'),
-- Family: P203-P204 da co san, bo sung P213-P218
('P213', 'LP004', 2, 'Trong'),
('P214', 'LP004', 2, 'Trong'),
('P215', 'LP004', 2, 'Trong'),
('P216', 'LP004', 2, 'Trong'),
('P217', 'LP004', 2, 'Trong'),
('P218', 'LP004', 2, 'Trong'),
-- Suite: P301-P302 da co san, bo sung P303-P307
('P303', 'LP005', 3, 'Trong'),
('P304', 'LP005', 3, 'Trong'),
('P305', 'LP005', 3, 'Trong'),
('P306', 'LP005', 3, 'Trong'),
('P307', 'LP005', 3, 'Trong'),
-- VIP: P401-P402 da co san, bo sung P403-P405
('P403', 'LP006', 4, 'Trong'),
('P404', 'LP006', 4, 'Trong'),
('P405', 'LP006', 4, 'BaoTri');
GO

INSERT INTO KhachHang (maKH, hoTenKH, gioiTinh, ngaySinh, email, sdt, CCCD, quocTich, diaChi, hangKH, diemTichLuy) VALUES
('KH009', N'Ngo Thi Hang',       0, '1994-03-22', 'hang.ngo@example.com',      '0820000009', '079094000009', N'Viet Nam', N'Hai Phong',  'Bac',       520),
('KH010', N'Tai Enzo',           1, '1989-10-18', 'enzo.tai@example.com',      '0820000010', '079089000010', N'Y',        N'Ha Noi',     'Vang',     1160),
('KH011', N'Dang Minh Khoa',     1, '1991-07-11', 'khoa.dang@example.com',     '0820000011', '079091000011', N'Viet Nam', N'TP HCM',     'Dong',      180),
('KH012', N'Luu Mai Phuong',     0, '1996-01-09', 'phuong.luu@example.com',    '0820000012', '079096000012', N'Viet Nam', N'Da Lat',     'Bac',       740),
('KH013', N'Nguyen Duc Son',     1, '1986-05-28', 'son.nguyen@example.com',    '0820000013', '079086000013', N'Viet Nam', N'Can Tho',    'Vang',     1300),
('KH014', N'Trinh Bao Chau',     0, '1999-09-17', 'chau.trinh@example.com',    '0820000014', '079099000014', N'Viet Nam', N'Da Nang',    'Dong',       90),
('KH015', N'Ho Minh Quan',       1, '1990-12-02', 'quan.ho@example.com',       '0820000015', '079090000015', N'Viet Nam', N'Ha Noi',     'KimCuong', 2600),
('KH016', N'Pham Ngoc Linh',     0, '1997-06-19', 'linh.pham@example.com',     '0820000016', '079097000016', N'Viet Nam', N'Nha Trang',  'Bac',       610),
('KH017', N'Bui Thanh Nam',      1, '1984-04-04', 'nam.bui@example.com',       '0820000017', '079084000017', N'Viet Nam', N'TP HCM',     'Vang',     1700),
('KH018', N'Do Khanh Vy',        0, '1993-11-25', 'vy.do@example.com',         '0820000018', '079093000018', N'Viet Nam', N'Hue',        'Dong',      220),
('KH019', N'Vu Quang Huy',       1, '1988-02-08', 'huy.vu@example.com',        '0820000019', '079088000019', N'Viet Nam', N'Quang Ninh', 'Bac',       850),
('KH020', N'Nguyen Ngoc Quang',  1, '1995-08-16', 'quang.nguyen@example.com',  '0820000020', '079095000020', N'Viet Nam', N'Ha Noi',     'Vang',     1500);
GO

INSERT INTO PhanCongCa (maPC, ngay, tienMoCa, tienKetCa, thoiGianMoCa, thoiGianKetCa, trangThai, maNV, maCa) VALUES
('PC008', '2026-02-14', 500000.00, 3200000.00, '2026-02-14 06:00:00', '2026-02-14 14:00:00', N'DaKet',      'NV002', 'CA001'),
('PC009', '2026-03-18', 500000.00, 4750000.00, '2026-03-18 14:00:00', '2026-03-18 22:00:00', N'DaKet',      'NV003', 'CA002'),
('PC010', '2026-04-16', 500000.00, 6100000.00, '2026-04-16 06:00:00', '2026-04-16 14:00:00', N'DaKet',      'NV004', 'CA001'),
-- dong tat ca ca truoc moc demo sang 28/05 de dashboard khong nhan nham ca cu
('PC011', '2026-05-27', 500000.00, 1800000.00, '2026-05-27 06:00:00', '2026-05-27 14:00:00', N'DaKet',      'NV002', 'CA001'),
('PC012', '2026-05-27', 500000.00, 2300000.00, '2026-05-27 14:00:00', '2026-05-27 22:00:00', N'DaKet',      'NV003', 'CA002'),
-- ca toi gan nhat truoc ngay demo: dung de test mo ca toi neu can
('PC016', '2026-05-27',      0.00,       0.00, NULL,                  NULL,                  N'DaPhanCong', 'NV004', 'CA003'),
-- 3 ca ngay mai 28/05: sang, chieu, toi
('PC013', '2026-05-28',      0.00,       0.00, NULL,                  NULL,                  N'DaPhanCong', 'NV002', 'CA001'),
('PC014', '2026-05-28',      0.00,       0.00, NULL,                  NULL,                  N'DaPhanCong', 'NV003', 'CA002'),
('PC015', '2026-05-28',      0.00,       0.00, NULL,                  NULL,                  N'DaPhanCong', 'NV004', 'CA003');
GO

-- Booking qua khu: phuc vu demo bieu do doanh thu/lai lo thang 2, 3, 4.
INSERT INTO DatPhong (maDatPhong, ngayDat, tienCoc, trangThaiDatPhong, ghiChu, maKH, maNV) VALUES
('DP011', '2026-02-03 09:10',  330000.00, 'DaTra', N'Demo thang 2 - Standard',   'KH009', 'NV002'),
('DP012', '2026-02-12 10:20', 1980000.00, 'DaTra', N'Demo thang 2 - Suite',      'KH010', 'NV003'),
('DP013', '2026-02-22 15:40', 2310000.00, 'DaTra', N'Demo thang 2 - VIP',        'KH011', 'NV004'),
('DP014', '2026-03-05 08:30',  825000.00, 'DaTra', N'Demo thang 3 - Superior',   'KH012', 'NV002'),
('DP015', '2026-03-16 13:00', 1320000.00, 'DaTra', N'Demo thang 3 - Deluxe',     'KH013', 'NV003'),
('DP016', '2026-03-25 16:15', 1980000.00, 'DaTra', N'Demo thang 3 - Family',     'KH014', 'NV004'),
('DP017', '2026-04-03 11:25',  990000.00, 'DaTra', N'Demo thang 4 - Standard',   'KH015', 'NV002'),
('DP018', '2026-04-11 09:50', 1650000.00, 'DaTra', N'Demo thang 4 - Deluxe',     'KH016', 'NV003'),
('DP019', '2026-04-19 17:10', 2640000.00, 'DaTra', N'Demo thang 4 - Suite',      'KH017', 'NV004'),
('DP020', '2026-05-26 14:30', 1320000.00, 'DangO', N'Demo dang luu tru hien tai','KH018', 'NV002'),
-- Booking ngay mai 28/05/2026: phuc vu demo Nhan phong.
('DP021', '2026-05-27 08:10',  165000.00, 'DaDat', N'Dat coc 30% - nhan phong ngay mai', 'KH009', 'NV002'),
('DP022', '2026-05-27 08:25',  247500.00, 'DaDat', N'Dat coc 30% - nhan phong ngay mai', 'KH010', 'NV003'),
('DP023', '2026-05-27 09:00',  330000.00, 'DaDat', N'Dat coc 30% - nhan phong ngay mai', 'KH011', 'NV004'),
('DP024', '2026-05-27 09:30',  495000.00, 'DaDat', N'Dat coc 30% - gia dinh ngay mai',    'KH012', 'NV002'),
('DP025', '2026-05-27 10:00',  726000.00, 'DaDat', N'Dat coc 30% - suite ngay mai',       'KH013', 'NV003'),
('DP026', '2026-05-27 10:40', 1155000.00, 'DaDat', N'Dat coc 30% - VIP ngay mai',         'KH014', 'NV004'),
-- Booking da hoan tat trong thang 5: giup demo KPI co loi nhuan duong.
('DP027', '2026-05-01 08:45', 4620000.00, 'DaTra', N'Demo thang 5 - VIP 4 dem',      'KH015', 'NV002'),
('DP028', '2026-05-03 10:15', 2178000.00, 'DaTra', N'Demo thang 5 - Suite 3 dem',    'KH016', 'NV003'),
('DP029', '2026-05-06 09:20', 1485000.00, 'DaTra', N'Demo thang 5 - Family 3 dem',   'KH017', 'NV004'),
('DP030', '2026-05-08 14:10', 1320000.00, 'DaTra', N'Demo thang 5 - Deluxe 4 dem',   'KH018', 'NV002'),
('DP031', '2026-05-10 11:30', 3465000.00, 'DaTra', N'Demo thang 5 - VIP 3 dem',      'KH019', 'NV003'),
('DP032', '2026-05-12 13:40', 2904000.00, 'DaTra', N'Demo thang 5 - Suite 4 dem',    'KH020', 'NV004'),
('DP033', '2026-05-15 08:55', 1980000.00, 'DaTra', N'Demo thang 5 - Family 4 dem',   'KH009', 'NV002'),
('DP034', '2026-05-18 10:05', 1237500.00, 'DaTra', N'Demo thang 5 - Superior 5 dem', 'KH010', 'NV003'),
('DP035', '2026-05-20 15:25', 1650000.00, 'DaTra', N'Demo thang 5 - Deluxe 5 dem',   'KH011', 'NV004'),
('DP036', '2026-05-22 16:45', 3465000.00, 'DaTra', N'Demo thang 5 - VIP 3 dem',      'KH012', 'NV002');
GO

INSERT INTO ChiTietDatPhong (maDatPhong, maPhong, ngayNhanDuKien, ngayTraDuKien, donGiaDat, soLuongNguoiO, ghiChu) VALUES
('DP011', 'P105', '2026-02-05 14:00', '2026-02-07 12:00',  500000.00, 2, NULL),
('DP012', 'P301', '2026-02-14 14:00', '2026-02-17 12:00', 2200000.00, 3, NULL),
('DP013', 'P401', '2026-02-24 14:00', '2026-02-26 12:00', 3500000.00, 4, NULL),
('DP014', 'P113', '2026-03-07 14:00', '2026-03-10 12:00',  750000.00, 2, NULL),
('DP015', 'P205', '2026-03-18 14:00', '2026-03-22 12:00', 1000000.00, 2, NULL),
('DP016', 'P213', '2026-03-27 14:00', '2026-03-30 12:00', 1500000.00, 4, NULL),
('DP017', 'P106', '2026-04-05 14:00', '2026-04-08 12:00',  500000.00, 2, NULL),
('DP018', 'P206', '2026-04-13 14:00', '2026-04-18 12:00', 1000000.00, 3, NULL),
('DP019', 'P303', '2026-04-21 14:00', '2026-04-25 12:00', 2200000.00, 3, NULL),
('DP020', 'P207', '2026-05-26 14:00', '2026-05-29 12:00', 1000000.00, 2, N'Dang luu tru de demo tra phong'),
('DP021', 'P101', '2026-05-28 14:00', '2026-05-29 12:00',  500000.00, 2, NULL),
('DP022', 'P103', '2026-05-28 14:00', '2026-05-29 12:00',  750000.00, 2, NULL),
('DP023', 'P205', '2026-05-28 14:00', '2026-05-29 12:00', 1000000.00, 3, NULL),
('DP024', 'P213', '2026-05-28 14:00', '2026-05-29 12:00', 1500000.00, 4, NULL),
('DP025', 'P303', '2026-05-28 14:00', '2026-05-29 12:00', 2200000.00, 3, NULL),
('DP026', 'P401', '2026-05-28 14:00', '2026-05-29 12:00', 3500000.00, 4, NULL),
('DP027', 'P402', '2026-05-02 14:00', '2026-05-06 12:00', 3500000.00, 4, NULL),
('DP028', 'P304', '2026-05-04 14:00', '2026-05-07 12:00', 2200000.00, 3, NULL),
('DP029', 'P214', '2026-05-07 14:00', '2026-05-10 12:00', 1500000.00, 4, NULL),
('DP030', 'P208', '2026-05-09 14:00', '2026-05-13 12:00', 1000000.00, 2, NULL),
('DP031', 'P403', '2026-05-11 14:00', '2026-05-14 12:00', 3500000.00, 4, NULL),
('DP032', 'P305', '2026-05-13 14:00', '2026-05-17 12:00', 2200000.00, 3, NULL),
('DP033', 'P215', '2026-05-16 14:00', '2026-05-20 12:00', 1500000.00, 4, NULL),
('DP034', 'P114', '2026-05-19 14:00', '2026-05-24 12:00',  750000.00, 2, NULL),
('DP035', 'P209', '2026-05-21 14:00', '2026-05-26 12:00', 1000000.00, 3, NULL),
('DP036', 'P404', '2026-05-23 14:00', '2026-05-26 12:00', 3500000.00, 4, NULL);
GO

UPDATE Phong SET trangThaiPhong = 'DangSuDung' WHERE maPhong = 'P207';

INSERT INTO ChiTietKhachO (maDatPhong, maPhong, hoTen, cccd, sdt, vaiTro) VALUES
('DP020', 'P207', N'Do Khanh Vy',       '079093000018', '0820000018', N'Nguoi dai dien'),
('DP020', 'P207', N'Nguyen Thanh Tung', '079093000088', '0918000008', N'Khach luu tru');
GO

INSERT INTO HoaDon (maHD, ngayLapHD, ngayThanhToan, ghiChu, soLuongNguoiO, tienPhong, tienDichVu, tienKhuyenMai, tienThue, tongTienThanhToan, phiDoiPhong, maKM, maKH, maNV, phuongThucTT, trangThai, maDatPhong) VALUES
('HD011', '2026-02-03 09:10', '2026-02-07 11:40', N'Demo paid - Feb Standard', 2, 1000000.00, 100000.00,      0.00, 110000.00, 1210000.00, 0.00, NULL,    'KH009', 'NV002', 'TienMat',     'DaThanhToan',   'DP011'),
('HD012', '2026-02-12 10:20', '2026-02-17 11:30', N'Demo paid - Feb Suite',    3, 6600000.00, 300000.00, 300000.00, 660000.00, 7260000.00, 0.00, 'KM001', 'KH010', 'NV003', 'ChuyenKhoan', 'DaThanhToan',   'DP012'),
('HD013', '2026-02-22 15:40', '2026-02-26 11:20', N'Demo paid - Feb VIP',      4, 7000000.00, 500000.00, 500000.00, 700000.00, 7700000.00, 0.00, 'KM001', 'KH011', 'NV004', 'ChuyenKhoan', 'DaThanhToan',   'DP013'),
('HD014', '2026-03-05 08:30', '2026-03-10 11:45', N'Demo paid - Mar Superior', 2, 2250000.00, 120000.00,      0.00, 237000.00, 2607000.00, 0.00, NULL,    'KH012', 'NV002', 'TienMat',     'DaThanhToan',   'DP014'),
('HD015', '2026-03-16 13:00', '2026-03-22 11:35', N'Demo paid - Mar Deluxe',   2, 4000000.00, 400000.00,      0.00, 440000.00, 4840000.00, 0.00, NULL,    'KH013', 'NV003', 'ChuyenKhoan', 'DaThanhToan',   'DP015'),
('HD016', '2026-03-25 16:15', '2026-03-30 11:50', N'Demo paid - Mar Family',   4, 4500000.00, 280000.00,      0.00, 478000.00, 5258000.00, 0.00, NULL,    'KH014', 'NV004', 'TienMat',     'DaThanhToan',   'DP016'),
('HD017', '2026-04-03 11:25', '2026-04-08 11:25', N'Demo paid - Apr Standard', 2, 1500000.00,  60000.00,      0.00, 156000.00, 1716000.00, 0.00, NULL,    'KH015', 'NV002', 'TienMat',     'DaThanhToan',   'DP017'),
('HD018', '2026-04-11 09:50', '2026-04-18 11:45', N'Demo paid - Apr Deluxe',   3, 5000000.00, 240000.00, 300000.00, 494000.00, 5434000.00, 0.00, 'KM002', 'KH016', 'NV003', 'ChuyenKhoan', 'DaThanhToan',   'DP018'),
('HD019', '2026-04-19 17:10', '2026-04-25 11:40', N'Demo paid - Apr Suite',    3, 8800000.00, 360000.00, 600000.00, 856000.00, 9416000.00, 0.00, 'KM002', 'KH017', 'NV004', 'ChuyenKhoan', 'DaThanhToan',   'DP019'),
('HD020', '2026-05-26 14:30', NULL,                 N'Demo dang luu tru',       2, 3000000.00,      0.00,      0.00, 300000.00, 3300000.00, 0.00, NULL,    'KH018', 'NV002', 'TienMat',     'ChuaThanhToan', 'DP020'),
('HD021', '2026-05-27 08:10', NULL,                 N'Dat coc 30% - ngay mai',  2,  500000.00,      0.00,      0.00,  50000.00,  550000.00, 0.00, NULL,    'KH009', 'NV002', 'TienMat',     'ChuaThanhToan', 'DP021'),
('HD022', '2026-05-27 08:25', NULL,                 N'Dat coc 30% - ngay mai',  2,  750000.00,      0.00,      0.00,  75000.00,  825000.00, 0.00, NULL,    'KH010', 'NV003', 'ChuyenKhoan', 'ChuaThanhToan', 'DP022'),
('HD023', '2026-05-27 09:00', NULL,                 N'Dat coc 30% - ngay mai',  3, 1000000.00,      0.00,      0.00, 100000.00, 1100000.00, 0.00, NULL,    'KH011', 'NV004', 'TienMat',     'ChuaThanhToan', 'DP023'),
('HD024', '2026-05-27 09:30', NULL,                 N'Dat coc 30% - ngay mai',  4, 1500000.00,      0.00,      0.00, 150000.00, 1650000.00, 0.00, NULL,    'KH012', 'NV002', 'TienMat',     'ChuaThanhToan', 'DP024'),
('HD025', '2026-05-27 10:00', NULL,                 N'Dat coc 30% - ngay mai',  3, 2200000.00,      0.00,      0.00, 220000.00, 2420000.00, 0.00, NULL,    'KH013', 'NV003', 'ChuyenKhoan', 'ChuaThanhToan', 'DP025'),
('HD026', '2026-05-27 10:40', NULL,                 N'Dat coc 30% - ngay mai',  4, 3500000.00,      0.00,      0.00, 350000.00, 3850000.00, 0.00, NULL,    'KH014', 'NV004', 'ChuyenKhoan', 'ChuaThanhToan', 'DP026'),
('HD027', '2026-05-01 08:45', '2026-05-06 11:30', N'Demo paid - May VIP',      4, 14000000.00,      0.00,      0.00, 1400000.00, 15400000.00, 0.00, NULL, 'KH015', 'NV002', 'ChuyenKhoan', 'DaThanhToan', 'DP027'),
('HD028', '2026-05-03 10:15', '2026-05-07 11:35', N'Demo paid - May Suite',    3,  6600000.00,      0.00,      0.00,  660000.00,  7260000.00, 0.00, NULL, 'KH016', 'NV003', 'TienMat',     'DaThanhToan', 'DP028'),
('HD029', '2026-05-06 09:20', '2026-05-10 11:40', N'Demo paid - May Family',   4,  4500000.00,      0.00,      0.00,  450000.00,  4950000.00, 0.00, NULL, 'KH017', 'NV004', 'TienMat',     'DaThanhToan', 'DP029'),
('HD030', '2026-05-08 14:10', '2026-05-13 11:45', N'Demo paid - May Deluxe',   2,  4000000.00,      0.00,      0.00,  400000.00,  4400000.00, 0.00, NULL, 'KH018', 'NV002', 'ChuyenKhoan', 'DaThanhToan', 'DP030'),
('HD031', '2026-05-10 11:30', '2026-05-14 11:25', N'Demo paid - May VIP',      4, 10500000.00,      0.00,      0.00, 1050000.00, 11550000.00, 0.00, NULL, 'KH019', 'NV003', 'ChuyenKhoan', 'DaThanhToan', 'DP031'),
('HD032', '2026-05-12 13:40', '2026-05-17 11:50', N'Demo paid - May Suite',    3,  8800000.00,      0.00,      0.00,  880000.00,  9680000.00, 0.00, NULL, 'KH020', 'NV004', 'TienMat',     'DaThanhToan', 'DP032'),
('HD033', '2026-05-15 08:55', '2026-05-20 11:20', N'Demo paid - May Family',   4,  6000000.00,      0.00,      0.00,  600000.00,  6600000.00, 0.00, NULL, 'KH009', 'NV002', 'TienMat',     'DaThanhToan', 'DP033'),
('HD034', '2026-05-18 10:05', '2026-05-24 11:40', N'Demo paid - May Superior', 2,  3750000.00,      0.00,      0.00,  375000.00,  4125000.00, 0.00, NULL, 'KH010', 'NV003', 'ChuyenKhoan', 'DaThanhToan', 'DP034'),
('HD035', '2026-05-20 15:25', '2026-05-26 11:35', N'Demo paid - May Deluxe',   3,  5000000.00,      0.00,      0.00,  500000.00,  5500000.00, 0.00, NULL, 'KH011', 'NV004', 'TienMat',     'DaThanhToan', 'DP035'),
('HD036', '2026-05-22 16:45', '2026-05-26 11:45', N'Demo paid - May VIP',      4, 10500000.00,      0.00,      0.00, 1050000.00, 11550000.00, 0.00, NULL, 'KH012', 'NV002', 'ChuyenKhoan', 'DaThanhToan', 'DP036');
GO

INSERT INTO ChiTietHoaDon (maHD, maPhong, ngayNhanPhong, ngayTraPhong, ngayTraThucTe, soDem, phuThu, thanhTien) VALUES
('HD011', 'P105', '2026-02-05 14:00', '2026-02-07 12:00', '2026-02-07 11:40', 2, 0.00, 1000000.00),
('HD012', 'P301', '2026-02-14 14:10', '2026-02-17 12:00', '2026-02-17 11:30', 3, 0.00, 6600000.00),
('HD013', 'P401', '2026-02-24 14:00', '2026-02-26 12:00', '2026-02-26 11:20', 2, 0.00, 7000000.00),
('HD014', 'P113', '2026-03-07 14:20', '2026-03-10 12:00', '2026-03-10 11:45', 3, 0.00, 2250000.00),
('HD015', 'P205', '2026-03-18 14:05', '2026-03-22 12:00', '2026-03-22 11:35', 4, 0.00, 4000000.00),
('HD016', 'P213', '2026-03-27 14:00', '2026-03-30 12:00', '2026-03-30 11:50', 3, 0.00, 4500000.00),
('HD017', 'P106', '2026-04-05 14:00', '2026-04-08 12:00', '2026-04-08 11:25', 3, 0.00, 1500000.00),
('HD018', 'P206', '2026-04-13 14:00', '2026-04-18 12:00', '2026-04-18 11:45', 5, 0.00, 5000000.00),
('HD019', 'P303', '2026-04-21 14:10', '2026-04-25 12:00', '2026-04-25 11:40', 4, 0.00, 8800000.00),
('HD020', 'P207', '2026-05-26 14:20', '2026-05-29 12:00', NULL,                3, 0.00, 3000000.00),
('HD027', 'P402', '2026-05-02 14:00', '2026-05-06 12:00', '2026-05-06 11:30', 4, 0.00, 14000000.00),
('HD028', 'P304', '2026-05-04 14:00', '2026-05-07 12:00', '2026-05-07 11:35', 3, 0.00,  6600000.00),
('HD029', 'P214', '2026-05-07 14:00', '2026-05-10 12:00', '2026-05-10 11:40', 3, 0.00,  4500000.00),
('HD030', 'P208', '2026-05-09 14:00', '2026-05-13 12:00', '2026-05-13 11:45', 4, 0.00,  4000000.00),
('HD031', 'P403', '2026-05-11 14:00', '2026-05-14 12:00', '2026-05-14 11:25', 3, 0.00, 10500000.00),
('HD032', 'P305', '2026-05-13 14:00', '2026-05-17 12:00', '2026-05-17 11:50', 4, 0.00,  8800000.00),
('HD033', 'P215', '2026-05-16 14:00', '2026-05-20 12:00', '2026-05-20 11:20', 4, 0.00,  6000000.00),
('HD034', 'P114', '2026-05-19 14:00', '2026-05-24 12:00', '2026-05-24 11:40', 5, 0.00,  3750000.00),
('HD035', 'P209', '2026-05-21 14:00', '2026-05-26 12:00', '2026-05-26 11:35', 5, 0.00,  5000000.00),
('HD036', 'P404', '2026-05-23 14:00', '2026-05-26 12:00', '2026-05-26 11:45', 3, 0.00, 10500000.00);
GO

INSERT INTO ChiTietDichVu (maCTDV, maHD, maDV, soLuong, donGia, thanhTien, ghiChu) VALUES
('CTDV0006', 'HD011', 'DV004', 2,  30000.00,  60000.00, N'Minibar'),
('CTDV0007', 'HD011', 'DV002', 1,  60000.00,  60000.00, N'Giat ui'),
('CTDV0008', 'HD012', 'DV005', 1, 250000.00, 250000.00, N'Dua don san bay'),
('CTDV0009', 'HD012', 'DV001', 1, 120000.00, 120000.00, N'Buffet'),
('CTDV0010', 'HD013', 'DV005', 2, 250000.00, 500000.00, N'Dua don VIP'),
('CTDV0011', 'HD014', 'DV001', 1, 120000.00, 120000.00, N'Buffet'),
('CTDV0012', 'HD015', 'DV003', 2,  80000.00, 160000.00, N'Don phong them'),
('CTDV0013', 'HD015', 'DV004', 8,  30000.00, 240000.00, N'Minibar'),
('CTDV0014', 'HD016', 'DV001', 2, 120000.00, 240000.00, N'Buffet gia dinh'),
('CTDV0015', 'HD017', 'DV002', 1,  60000.00,  60000.00, N'Giat ui'),
('CTDV0016', 'HD018', 'DV001', 2, 120000.00, 240000.00, N'Buffet'),
('CTDV0017', 'HD019', 'DV001', 3, 120000.00, 360000.00, N'Buffet suite');
GO

INSERT INTO ThanhToan (maTT, ngayTT, soTienTT, ghiChu, phuongThucTT, trangThaiTT, maHD, maPC, maNV, loaiGD) VALUES
('TT014', '2026-02-03 09:20',  330000.00, N'Dat coc 30%',             'TienMat',     'ThanhToanThanhCong', 'HD011', 'PC008', 'NV002', 'Thu'),
('TT015', '2026-02-07 11:45',  880000.00, N'Thanh toan phan con lai', 'TienMat',     'ThanhToanThanhCong', 'HD011', 'PC008', 'NV002', 'Thu'),
('TT016', '2026-02-12 10:30', 1980000.00, N'Dat coc 30%',             'ChuyenKhoan', 'ThanhToanThanhCong', 'HD012', 'PC008', 'NV003', 'Thu'),
('TT017', '2026-02-17 11:35', 5280000.00, N'Thanh toan phan con lai', 'ChuyenKhoan', 'ThanhToanThanhCong', 'HD012', 'PC008', 'NV003', 'Thu'),
('TT018', '2026-02-22 15:50', 2310000.00, N'Dat coc 30%',             'ChuyenKhoan', 'ThanhToanThanhCong', 'HD013', 'PC008', 'NV004', 'Thu'),
('TT019', '2026-02-26 11:25', 5390000.00, N'Thanh toan phan con lai', 'ChuyenKhoan', 'ThanhToanThanhCong', 'HD013', 'PC008', 'NV004', 'Thu'),
('TT020', '2026-03-05 08:40',  825000.00, N'Dat coc 30%',             'TienMat',     'ThanhToanThanhCong', 'HD014', 'PC009', 'NV002', 'Thu'),
('TT021', '2026-03-10 11:50', 1782000.00, N'Thanh toan phan con lai', 'TienMat',     'ThanhToanThanhCong', 'HD014', 'PC009', 'NV002', 'Thu'),
('TT022', '2026-03-16 13:10', 1320000.00, N'Dat coc 30%',             'ChuyenKhoan', 'ThanhToanThanhCong', 'HD015', 'PC009', 'NV003', 'Thu'),
('TT023', '2026-03-22 11:40', 3520000.00, N'Thanh toan phan con lai', 'ChuyenKhoan', 'ThanhToanThanhCong', 'HD015', 'PC009', 'NV003', 'Thu'),
('TT024', '2026-03-25 16:25', 1980000.00, N'Dat coc 30%',             'TienMat',     'ThanhToanThanhCong', 'HD016', 'PC009', 'NV004', 'Thu'),
('TT025', '2026-03-30 11:55', 3278000.00, N'Thanh toan phan con lai', 'TienMat',     'ThanhToanThanhCong', 'HD016', 'PC009', 'NV004', 'Thu'),
('TT026', '2026-04-03 11:35',  990000.00, N'Dat coc 30%',             'TienMat',     'ThanhToanThanhCong', 'HD017', 'PC010', 'NV002', 'Thu'),
('TT027', '2026-04-08 11:30',  726000.00, N'Thanh toan phan con lai', 'TienMat',     'ThanhToanThanhCong', 'HD017', 'PC010', 'NV002', 'Thu'),
('TT028', '2026-04-11 10:00', 1650000.00, N'Dat coc 30%',             'ChuyenKhoan', 'ThanhToanThanhCong', 'HD018', 'PC010', 'NV003', 'Thu'),
('TT029', '2026-04-18 11:50', 3784000.00, N'Thanh toan phan con lai', 'ChuyenKhoan', 'ThanhToanThanhCong', 'HD018', 'PC010', 'NV003', 'Thu'),
('TT030', '2026-04-19 17:20', 2640000.00, N'Dat coc 30%',             'ChuyenKhoan', 'ThanhToanThanhCong', 'HD019', 'PC010', 'NV004', 'Thu'),
('TT031', '2026-04-25 11:45', 6776000.00, N'Thanh toan phan con lai', 'ChuyenKhoan', 'ThanhToanThanhCong', 'HD019', 'PC010', 'NV004', 'Thu'),
('TT032', '2026-05-26 14:40', 1320000.00, N'Dat coc 30%',             'TienMat',     'ThanhToanThanhCong', 'HD020', NULL, 'NV002', 'Thu'),
('TT033', '2026-05-27 08:15',  165000.00, N'Dat coc 30%',             'TienMat',     'ThanhToanThanhCong', 'HD021', NULL, 'NV002', 'Thu'),
('TT034', '2026-05-27 08:30',  247500.00, N'Dat coc 30%',             'ChuyenKhoan', 'ThanhToanThanhCong', 'HD022', NULL, 'NV003', 'Thu'),
('TT035', '2026-05-27 09:05',  330000.00, N'Dat coc 30%',             'TienMat',     'ThanhToanThanhCong', 'HD023', NULL, 'NV004', 'Thu'),
('TT036', '2026-05-27 09:35',  495000.00, N'Dat coc 30%',             'TienMat',     'ThanhToanThanhCong', 'HD024', NULL, 'NV002', 'Thu'),
('TT037', '2026-05-27 10:05',  726000.00, N'Dat coc 30%',             'ChuyenKhoan', 'ThanhToanThanhCong', 'HD025', NULL, 'NV003', 'Thu'),
('TT038', '2026-05-27 10:45', 1155000.00, N'Dat coc 30%',             'ChuyenKhoan', 'ThanhToanThanhCong', 'HD026', NULL, 'NV004', 'Thu'),
('TT039', '2026-04-27 11:50', 3500000.00, N'Thanh toan phan con lai', 'TienMat',     'ThanhToanThanhCong', 'HD004', 'PC005', 'NV002', 'Thu'),
('TT040', '2026-04-28 11:40', 5184000.00, N'Thanh toan phan con lai', 'ChuyenKhoan', 'ThanhToanThanhCong', 'HD005', 'PC005', 'NV003', 'Thu'),
('TT041', '2026-05-01 08:55',  4620000.00, N'Dat coc 30%',             'ChuyenKhoan', 'ThanhToanThanhCong', 'HD027', NULL, 'NV002', 'Thu'),
('TT042', '2026-05-06 11:35', 10780000.00, N'Thanh toan phan con lai', 'ChuyenKhoan', 'ThanhToanThanhCong', 'HD027', NULL, 'NV002', 'Thu'),
('TT043', '2026-05-03 10:25',  2178000.00, N'Dat coc 30%',             'TienMat',     'ThanhToanThanhCong', 'HD028', NULL, 'NV003', 'Thu'),
('TT044', '2026-05-07 11:40',  5082000.00, N'Thanh toan phan con lai', 'TienMat',     'ThanhToanThanhCong', 'HD028', NULL, 'NV003', 'Thu'),
('TT045', '2026-05-06 09:30',  1485000.00, N'Dat coc 30%',             'TienMat',     'ThanhToanThanhCong', 'HD029', NULL, 'NV004', 'Thu'),
('TT046', '2026-05-10 11:45',  3465000.00, N'Thanh toan phan con lai', 'TienMat',     'ThanhToanThanhCong', 'HD029', NULL, 'NV004', 'Thu'),
('TT047', '2026-05-08 14:20',  1320000.00, N'Dat coc 30%',             'ChuyenKhoan', 'ThanhToanThanhCong', 'HD030', NULL, 'NV002', 'Thu'),
('TT048', '2026-05-13 11:50',  3080000.00, N'Thanh toan phan con lai', 'ChuyenKhoan', 'ThanhToanThanhCong', 'HD030', NULL, 'NV002', 'Thu'),
('TT049', '2026-05-10 11:40',  3465000.00, N'Dat coc 30%',             'ChuyenKhoan', 'ThanhToanThanhCong', 'HD031', NULL, 'NV003', 'Thu'),
('TT050', '2026-05-14 11:30',  8085000.00, N'Thanh toan phan con lai', 'ChuyenKhoan', 'ThanhToanThanhCong', 'HD031', NULL, 'NV003', 'Thu'),
('TT051', '2026-05-12 13:50',  2904000.00, N'Dat coc 30%',             'TienMat',     'ThanhToanThanhCong', 'HD032', NULL, 'NV004', 'Thu'),
('TT052', '2026-05-17 11:55',  6776000.00, N'Thanh toan phan con lai', 'TienMat',     'ThanhToanThanhCong', 'HD032', NULL, 'NV004', 'Thu'),
('TT053', '2026-05-15 09:05',  1980000.00, N'Dat coc 30%',             'TienMat',     'ThanhToanThanhCong', 'HD033', NULL, 'NV002', 'Thu'),
('TT054', '2026-05-20 11:25',  4620000.00, N'Thanh toan phan con lai', 'TienMat',     'ThanhToanThanhCong', 'HD033', NULL, 'NV002', 'Thu'),
('TT055', '2026-05-18 10:15',  1237500.00, N'Dat coc 30%',             'ChuyenKhoan', 'ThanhToanThanhCong', 'HD034', NULL, 'NV003', 'Thu'),
('TT056', '2026-05-24 11:45',  2887500.00, N'Thanh toan phan con lai', 'ChuyenKhoan', 'ThanhToanThanhCong', 'HD034', NULL, 'NV003', 'Thu'),
('TT057', '2026-05-20 15:35',  1650000.00, N'Dat coc 30%',             'TienMat',     'ThanhToanThanhCong', 'HD035', NULL, 'NV004', 'Thu'),
('TT058', '2026-05-26 11:40',  3850000.00, N'Thanh toan phan con lai', 'TienMat',     'ThanhToanThanhCong', 'HD035', NULL, 'NV004', 'Thu'),
('TT059', '2026-05-22 16:55',  3465000.00, N'Dat coc 30%',             'ChuyenKhoan', 'ThanhToanThanhCong', 'HD036', NULL, 'NV002', 'Thu'),
('TT060', '2026-05-26 11:50',  8085000.00, N'Thanh toan phan con lai', 'ChuyenKhoan', 'ThanhToanThanhCong', 'HD036', NULL, 'NV002', 'Thu');
GO

INSERT INTO ChiPhi (loaiChiPhi, tenChiPhi, soTien, ngayChi, ghiChu, maNV, maPC) VALUES
(N'Dien nuoc', N'Tien dien thang 2', 2850000, '2026-02-28 09:00', N'Chi phi van hanh thang 2', 'NV002', 'PC008'),
(N'Vat tu',    N'Bo sung do vai thang 3', 1750000, '2026-03-20 10:30', N'Khan, ga, vo goi', 'NV003', 'PC009'),
(N'Khac',      N'Bao tri thang may thang 4', 2200000, '2026-04-16 15:00', N'Bao tri dinh ky', 'NV004', 'PC010');
GO

-- =====================================================================
-- 10. KIEM TRA NHANH (chay sau khi import)
-- =====================================================================
PRINT '--- So luong rows tung bang ---';
SELECT 'TaiKhoan'        AS Bang, COUNT(*) AS SoDong FROM TaiKhoan
UNION ALL SELECT 'NhanVien',         COUNT(*) FROM NhanVien
UNION ALL SELECT 'KhachHang',        COUNT(*) FROM KhachHang
UNION ALL SELECT 'CaLam',            COUNT(*) FROM CaLam
UNION ALL SELECT 'PhanCongCa',       COUNT(*) FROM PhanCongCa
UNION ALL SELECT 'LoaiPhong',        COUNT(*) FROM LoaiPhong
UNION ALL SELECT 'Phong',            COUNT(*) FROM Phong
UNION ALL SELECT 'DichVu',           COUNT(*) FROM DichVu
UNION ALL SELECT 'KhuyenMai',        COUNT(*) FROM KhuyenMai
UNION ALL SELECT 'DatPhong',         COUNT(*) FROM DatPhong
UNION ALL SELECT 'ChiTietDatPhong',  COUNT(*) FROM ChiTietDatPhong
UNION ALL SELECT 'ChiTietKhachO',    COUNT(*) FROM ChiTietKhachO
UNION ALL SELECT 'ChiPhi',           COUNT(*) FROM ChiPhi
UNION ALL SELECT 'HoaDon',           COUNT(*) FROM HoaDon
UNION ALL SELECT 'ChiTietHoaDon',    COUNT(*) FROM ChiTietHoaDon
UNION ALL SELECT 'ChiTietDichVu',    COUNT(*) FROM ChiTietDichVu
UNION ALL SELECT 'ThanhToan',        COUNT(*) FROM ThanhToan;

PRINT '--- Phan bo trang thai phong ---';
SELECT trangThaiPhong, COUNT(*) AS SoPhong FROM Phong GROUP BY trangThaiPhong;

PRINT '--- Phan bo phong theo loai ---';
SELECT lp.maLoaiPhong, lp.tenLoaiPhong, COUNT(p.maPhong) AS SoPhongThucTe
FROM LoaiPhong lp
LEFT JOIN Phong p ON p.maLoaiPhong = lp.maLoaiPhong
GROUP BY lp.maLoaiPhong, lp.tenLoaiPhong
ORDER BY lp.maLoaiPhong;

PRINT '--- Booking nhan phong ngay mai 28/05/2026 ---';
SELECT dp.maDatPhong, kh.hoTenKH, ctdp.maPhong, lp.tenLoaiPhong, ctdp.ngayNhanDuKien, hd.trangThai AS trangThaiHoaDon
FROM DatPhong dp
JOIN KhachHang kh ON kh.maKH = dp.maKH
JOIN ChiTietDatPhong ctdp ON ctdp.maDatPhong = dp.maDatPhong
JOIN Phong p ON p.maPhong = ctdp.maPhong
JOIN LoaiPhong lp ON lp.maLoaiPhong = p.maLoaiPhong
JOIN HoaDon hd ON hd.maDatPhong = dp.maDatPhong
WHERE CAST(ctdp.ngayNhanDuKien AS DATE) = '2026-05-28'
ORDER BY ctdp.ngayNhanDuKien, ctdp.maPhong;

PRINT '--- Pipeline booking theo trang thai HoaDon ---';
SELECT trangThai, COUNT(*) AS SoHoaDon FROM HoaDon GROUP BY trangThai;

PRINT '--- Demo KPI thang 5/2026: doanh thu - chi phi - loi nhuan ---';
SELECT
    doanhThu = (
        SELECT COALESCE(SUM(CASE WHEN loaiGD = 'HoanTien' THEN -soTienTT ELSE soTienTT END), 0)
        FROM ThanhToan
        WHERE ngayTT >= '2026-05-01' AND ngayTT < '2026-06-01'
          AND trangThaiTT = 'ThanhToanThanhCong'
    ),
    chiPhiThuCong = (
        SELECT COALESCE(SUM(soTien), 0)
        FROM ChiPhi
        WHERE ngayChi >= '2026-05-01' AND ngayChi < '2026-06-01'
    ),
    chiPhiLuong = (
        SELECT COALESCE(SUM(nv.luong), 0)
        FROM NhanVien nv
        JOIN TaiKhoan tk ON nv.tenDangNhap = tk.tenDangNhap
        WHERE tk.trangThaiTK = 'DangHoatDong'
    ),
    loiNhuanDemo = (
        SELECT COALESCE(SUM(CASE WHEN loaiGD = 'HoanTien' THEN -soTienTT ELSE soTienTT END), 0)
        FROM ThanhToan
        WHERE ngayTT >= '2026-05-01' AND ngayTT < '2026-06-01'
          AND trangThaiTT = 'ThanhToanThanhCong'
    )
    - (
        SELECT COALESCE(SUM(soTien), 0)
        FROM ChiPhi
        WHERE ngayChi >= '2026-05-01' AND ngayChi < '2026-06-01'
    )
    - (
        SELECT COALESCE(SUM(nv.luong), 0)
        FROM NhanVien nv
        JOIN TaiKhoan tk ON nv.tenDangNhap = tk.tenDangNhap
        WHERE tk.trangThaiTK = 'DangHoatDong'
    );
GO

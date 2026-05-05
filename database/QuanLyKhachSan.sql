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
IF OBJECT_ID('dbo.ChiTietDatPhong', 'U')IS NOT NULL DROP TABLE dbo.ChiTietDatPhong;
IF OBJECT_ID('dbo.DatPhong', 'U')       IS NOT NULL DROP TABLE dbo.DatPhong;
IF OBJECT_ID('dbo.PhanCongCa', 'U')     IS NOT NULL DROP TABLE dbo.PhanCongCa;
IF OBJECT_ID('dbo.Phong', 'U')          IS NOT NULL DROP TABLE dbo.Phong;
IF OBJECT_ID('dbo.LoaiPhong', 'U')      IS NOT NULL DROP TABLE dbo.LoaiPhong;
IF OBJECT_ID('dbo.KhuyenMai', 'U')      IS NOT NULL DROP TABLE dbo.KhuyenMai;
IF OBJECT_ID('dbo.DichVu', 'U')         IS NOT NULL DROP TABLE dbo.DichVu;
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
    maPC       CHAR(5)       PRIMARY KEY,
    ngay       DATETIME2     NOT NULL,
    tienMoCa   DECIMAL(18,2) NOT NULL DEFAULT 0 CHECK (tienMoCa >= 0),
    tienKetCa  DECIMAL(18,2) NOT NULL DEFAULT 0 CHECK (tienKetCa >= 0),
    maNV       CHAR(5)       NOT NULL,
    maCa       CHAR(5)       NOT NULL,
    CONSTRAINT FK_PhanCongCa_NhanVien FOREIGN KEY (maNV) REFERENCES NhanVien(maNV),
    CONSTRAINT FK_PhanCongCa_CaLam    FOREIGN KEY (maCa) REFERENCES CaLam(maCa)
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
CREATE INDEX IX_CTDP_maPhong         ON ChiTietDatPhong(maPhong);
CREATE INDEX IX_CTDP_ngayNhan        ON ChiTietDatPhong(ngayNhanDuKien, ngayTraDuKien);
CREATE INDEX IX_HoaDon_maKH          ON HoaDon(maKH);
CREATE INDEX IX_HoaDon_maDatPhong    ON HoaDon(maDatPhong);
CREATE INDEX IX_HoaDon_trangThai     ON HoaDon(trangThai);
CREATE INDEX IX_CTHD_maPhong         ON ChiTietHoaDon(maPhong);
CREATE INDEX IX_CTHD_ngayNhan        ON ChiTietHoaDon(ngayNhanPhong);
CREATE INDEX IX_CTDV_maHD            ON ChiTietDichVu(maHD);
CREATE INDEX IX_ThanhToan_maHD       ON ThanhToan(maHD);
CREATE INDEX IX_ThanhToan_maPC       ON ThanhToan(maPC);

-- Filtered unique index: email phai unique khi co gia tri, nhung cho phep nhieu NULL
CREATE UNIQUE INDEX UX_KhachHang_email ON KhachHang(email) WHERE email IS NOT NULL;
GO

-- =====================================================================
-- 8. DU LIEU MAU - Anchor "hom nay" = 2026-04-25
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

INSERT INTO PhanCongCa (maPC, ngay, tienMoCa, tienKetCa, maNV, maCa) VALUES
('PC001', '2026-04-23', 500000.00,  3500000.00, 'NV002', 'CA001'),
('PC002', '2026-04-23', 500000.00,  4200000.00, 'NV003', 'CA002'),
('PC003', '2026-04-24', 500000.00,  3800000.00, 'NV004', 'CA001'),
('PC004', '2026-04-24', 500000.00,  4500000.00, 'NV005', 'CA002'),
('PC005', '2026-04-25', 500000.00,        0.00, 'NV002', 'CA001'); -- ca dang mo
GO

-- ----- LoaiPhong (6 loai) + Phong (12 phong) -----
INSERT INTO LoaiPhong (maLoaiPhong, tenLoaiPhong, soLuongPhong, giaPhong, sucChuaToiDa, dienTich, moTa, tienNghi) VALUES
('LP001', N'Standard',  2,  500000.00, 2, 22.00, N'Phong tieu chuan 1 giuong doi',          N'Wifi, May lanh, TV'),
('LP002', N'Superior',  2,  750000.00, 2, 26.00, N'Phong superior co cua so',                N'Wifi, May lanh, TV, Minibar'),
('LP003', N'Deluxe',    2, 1000000.00, 3, 32.00, N'Phong deluxe huong thanh pho',            N'Wifi, May lanh, TV, Minibar, Bon tam'),
('LP004', N'Family',    2, 1500000.00, 4, 42.00, N'Phong gia dinh 2 giuong',                 N'Wifi, May lanh, TV, Minibar, Bon tam, Bep nho'),
('LP005', N'Suite',     2, 2200000.00, 4, 55.00, N'Suite cao cap voi phong khach rieng',     N'Wifi, May lanh, TV, Minibar, Bon tam, Phong khach'),
('LP006', N'VIP',       2, 3500000.00, 6, 80.00, N'Phong VIP huong bien, view dep nhat',     N'Full tien nghi, Bon tam jacuzzi, Sky bar');
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

INSERT INTO DatPhong (maDatPhong, ngayDat, tienCoc, ghiChu, maKH, maNV) VALUES
-- Stage 4: hoan tat
('DP001', '2026-04-10 09:00',  450000.00, N'Booking demo da hoan tat',    'KH001', 'NV002'),
('DP002', '2026-04-12 10:00',  675000.00, N'Booking 2 phong da hoan tat', 'KH002', 'NV003'),
-- Stage 3: da tra phong, chua thanh toan du
('DP003', '2026-04-18 09:00',  450000.00, N'Khach da tra, no phan con lai','KH004', 'NV004'),
-- Stage 2: dang luu tru
('DP004', '2026-04-20 11:00',  900000.00, N'Khach dang luu tru',          'KH005', 'NV002'),
('DP005', '2026-04-21 14:00', 1350000.00, N'Khach gia dinh dang o',       'KH008', 'NV003'),
-- Stage 1: dat coc, chua check-in
('DP006', '2026-04-23 16:00',  450000.00, N'Da dat coc - check-in tuong lai','KH003','NV004'),
('DP007', '2026-04-24 09:00', 1500000.00, N'Dat phong VIP cho ky nghi',    'KH006', 'NV002'),
('DP008', '2026-04-25 10:00',  675000.00, N'Booking xa - le 30/4',         'KH007', 'NV005'),
-- Stage 5: huy
('DP009', '2026-04-15 11:00',  300000.00, N'Khach huy do thay doi lich',   'KH001', 'NV003'),
-- Stage 4 (full pay khong qua coc)
('DP010', '2026-04-19 13:00',       0.00, N'Walk-in thanh toan 100% ngay', 'KH004', 'NV002');
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
INSERT INTO ThanhToan (maTT, ngayTT, soTienTT, ghiChu, phuongThucTT, trangThaiTT, maHD, maPC, maNV) VALUES
-- DP001: coc 30% (450k) + phan con lai (1.332tr)
('TT001', '2026-04-10 09:15',  450000.00, N'Dat coc 30%',          'TienMat',     'ThanhToanThanhCong', 'HD001', NULL,    'NV002'),
('TT002', '2026-04-18 12:25', 1332000.00, N'Thanh toan phan con lai','TienMat',   'ThanhToanThanhCong', 'HD001', 'PC001', 'NV002'),
-- DP002: coc 30% + phan con lai
('TT003', '2026-04-12 10:15',  675000.00, N'Dat coc 30%',          'ChuyenKhoan', 'ThanhToanThanhCong', 'HD002', NULL,    'NV003'),
('TT004', '2026-04-20 12:25', 4836000.00, N'Thanh toan phan con lai','ChuyenKhoan','ThanhToanThanhCong', 'HD002', 'PC002', 'NV003'),
-- DP003: chi co coc, chua tra phan con lai
('TT005', '2026-04-18 09:15',  450000.00, N'Dat coc 30%',          'TienMat',     'ThanhToanThanhCong', 'HD003', NULL,    'NV004'),
-- DP004: chi co coc
('TT006', '2026-04-20 11:15',  900000.00, N'Dat coc 30%',          'TienMat',     'ThanhToanThanhCong', 'HD004', NULL,    'NV002'),
-- DP005: chi co coc
('TT007', '2026-04-21 14:15', 1350000.00, N'Dat coc 30%',          'ChuyenKhoan', 'ThanhToanThanhCong', 'HD005', NULL,    'NV003'),
-- DP006-DP008: coc cho future bookings
('TT008', '2026-04-23 16:15',  450000.00, N'Dat coc 30%',          'ChuyenKhoan', 'ThanhToanThanhCong', 'HD006', 'PC002', 'NV004'),
('TT009', '2026-04-24 09:15', 1500000.00, N'Dat coc 30%',          'ChuyenKhoan', 'ThanhToanThanhCong', 'HD007', 'PC003', 'NV002'),
('TT010', '2026-04-25 10:15',  675000.00, N'Dat coc 30%',          'TienMat',     'ThanhToanThanhCong', 'HD008', 'PC005', 'NV005'),
-- DP009: coc da hoan tra (huy)
('TT011', '2026-04-15 11:15',  300000.00, N'Dat coc 30%',          'TienMat',     'ThanhToanThanhCong', 'HD009', NULL,    'NV003'),
('TT012', '2026-04-22 09:00',  300000.00, N'Hoan tien coc do huy', 'TienMat',     'DaHuy',              'HD009', NULL,    'NV003'),
-- DP010: thanh toan 100% ngay khi dat
('TT013', '2026-04-19 13:25', 1683000.00, N'Thanh toan 100%',      'TienMat',     'ThanhToanThanhCong', 'HD010', NULL,    'NV002');
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
UNION ALL SELECT 'HoaDon',           COUNT(*) FROM HoaDon
UNION ALL SELECT 'ChiTietHoaDon',    COUNT(*) FROM ChiTietHoaDon
UNION ALL SELECT 'ChiTietDichVu',    COUNT(*) FROM ChiTietDichVu
UNION ALL SELECT 'ThanhToan',        COUNT(*) FROM ThanhToan;

PRINT '--- Phan bo trang thai phong ---';
SELECT trangThaiPhong, COUNT(*) AS SoPhong FROM Phong GROUP BY trangThaiPhong;

PRINT '--- Pipeline booking theo trang thai HoaDon ---';
SELECT trangThai, COUNT(*) AS SoHoaDon FROM HoaDon GROUP BY trangThai;
GO

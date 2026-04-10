# Roomify UI Prototype

## Công nghệ
- Java 21
- Swing
- FlatLaf
- MigLayout

## Cấu trúc dự án

```
src/kqlhotel/
  gui/
    components/        # Custom Swing components
    tabs/              # Login, Shift opening, Main booking screen
    theme/             # Màu sắc và style chung
    AppFrame.java      # UI shell
  main/
    AppLauncher.java   # Entry point
  bus/
  dao/
  entity/
  utils/
lib/
  *.jar                # Thư viện add thủ công
```

## Yêu cầu môi trường
1. IntelliJ IDEA (Community/Ultimate đều được)
2. JDK 21
3. Các thư viện jar trong thư mục `lib/`:
   - `flatlaf-3.5.4.jar`
   - `flatlaf-extras-3.5.4.jar`
   - `miglayout-core-11.4.2.jar`
   - `miglayout-swing-11.4.2.jar`

## Setup trên IntelliJ 
1. Clone project và mở folder `Roomify` bằng IntelliJ.
2. Vào `File > Project Structure`.
3. Ở `Project`:
   - `Project SDK`: chọn JDK 21
   - `Project language level`: 21
4. Ở `Modules > Sources`:
   - Đảm bảo `src` được đánh dấu là `Sources`.
5. Ở `Modules > Dependencies`:
   - Bấm `+ > JARs or Directories...`
   - Chọn toàn bộ file trong `lib/`
   - Scope để `Compile`.
6. Bấm `Apply` rồi `OK`.

## Chạy ứng dụng
1. Mở file `src/kqlhotel/main/AppLauncher.java`.
2. Bấm `Run 'AppLauncher.main()'`.

Luồng màn hình hiện tại:
1. Đăng nhập
2. Kiểm kê tiền đầu ca
3. Vào màn hình chính bên trong

Tài khoản demo:
- Username: `admin`
- Password: `admin123`
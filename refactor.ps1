$src = 'c:\Users\Admin\Roomify\src\kqlhotel'

if (!(Test-Path "$src\bus\service\")) { New-Item -ItemType Directory -Force -Path "$src\bus\service" }

Move-Item -Path "$src\bus\DichVuBus.java" -Destination "$src\bus\service\DichVuBus.java" -Force
Move-Item -Path "$src\bus\KhachHangBus.java" -Destination "$src\bus\customer\KhachHangBus.java" -Force
Move-Item -Path "$src\bus\DoiPhongBus.java" -Destination "$src\bus\booking\DoiPhongBus.java" -Force

Move-Item -Path "$src\dao\DichVuDao.java" -Destination "$src\dao\service\DichVuDao.java" -Force
Move-Item -Path "$src\dao\KhachHangDao.java" -Destination "$src\dao\customer\KhachHangDao.java" -Force
Move-Item -Path "$src\dao\DoiPhongDao.java" -Destination "$src\dao\booking\DoiPhongDao.java" -Force

# Replace package names in moved files
(Get-Content "$src\bus\service\DichVuBus.java") -replace 'package kqlhotel.bus;', 'package kqlhotel.bus.service;' | Set-Content "$src\bus\service\DichVuBus.java"
(Get-Content "$src\bus\customer\KhachHangBus.java") -replace 'package kqlhotel.bus;', 'package kqlhotel.bus.customer;' | Set-Content "$src\bus\customer\KhachHangBus.java"
(Get-Content "$src\bus\booking\DoiPhongBus.java") -replace 'package kqlhotel.bus;', 'package kqlhotel.bus.booking;' | Set-Content "$src\bus\booking\DoiPhongBus.java"

(Get-Content "$src\dao\service\DichVuDao.java") -replace 'package kqlhotel.dao;', 'package kqlhotel.dao.service;' | Set-Content "$src\dao\service\DichVuDao.java"
(Get-Content "$src\dao\customer\KhachHangDao.java") -replace 'package kqlhotel.dao;', 'package kqlhotel.dao.customer;' | Set-Content "$src\dao\customer\KhachHangDao.java"
(Get-Content "$src\dao\booking\DoiPhongDao.java") -replace 'package kqlhotel.dao;', 'package kqlhotel.dao.booking;' | Set-Content "$src\dao\booking\DoiPhongDao.java"

# Replace imports in all files
Get-ChildItem -Path $src -Recurse -Filter *.java | ForEach-Object {
    $content = Get-Content $_.FullName
    $changed = $false
    
    for ($i = 0; $i -lt $content.Length; $i++) {
        if ($content[$i] -match 'kqlhotel\.bus\.DichVuBus') { $content[$i] = $content[$i] -replace 'kqlhotel\.bus\.DichVuBus', 'kqlhotel.bus.service.DichVuBus'; $changed = $true }
        if ($content[$i] -match 'kqlhotel\.bus\.KhachHangBus') { $content[$i] = $content[$i] -replace 'kqlhotel\.bus\.KhachHangBus', 'kqlhotel.bus.customer.KhachHangBus'; $changed = $true }
        if ($content[$i] -match 'kqlhotel\.bus\.DoiPhongBus') { $content[$i] = $content[$i] -replace 'kqlhotel\.bus\.DoiPhongBus', 'kqlhotel.bus.booking.DoiPhongBus'; $changed = $true }
        
        if ($content[$i] -match 'kqlhotel\.dao\.DichVuDao') { $content[$i] = $content[$i] -replace 'kqlhotel\.dao\.DichVuDao', 'kqlhotel.dao.service.DichVuDao'; $changed = $true }
        if ($content[$i] -match 'kqlhotel\.dao\.KhachHangDao') { $content[$i] = $content[$i] -replace 'kqlhotel\.dao\.KhachHangDao', 'kqlhotel.dao.customer.KhachHangDao'; $changed = $true }
        if ($content[$i] -match 'kqlhotel\.dao\.DoiPhongDao') { $content[$i] = $content[$i] -replace 'kqlhotel\.dao\.DoiPhongDao', 'kqlhotel.dao.booking.DoiPhongDao'; $changed = $true }
    }
    
    if ($changed) {
        $content | Set-Content $_.FullName
    }
}

# Khoi dong DuaStore (server + tunnel SePay) cho moi trung dev local.
# Chay: mo PowerShell trong thu muc project, go: .\start-dev.ps1

$cloudflared = "C:\Program Files (x86)\cloudflared\cloudflared.exe"
if (-not (Test-Path $cloudflared)) {
    $onPath = Get-Command cloudflared.exe -ErrorAction SilentlyContinue
    if ($onPath) {
        $cloudflared = $onPath.Source
    } else {
        Write-Host "Khong tim thay cloudflared.exe (da thu: '$cloudflared' va PATH he thong)." -ForegroundColor Red
        Write-Host "Cai dat tai: https://github.com/cloudflare/cloudflared/releases (chon file .exe cho Windows)." -ForegroundColor Red
        exit 1
    }
}
$logFile = "$env:TEMP\cloudflared.log"

Write-Host "== 1. Khoi dong Spring Boot server (nen) ==" -ForegroundColor Cyan
Start-Process -FilePath "cmd.exe" -ArgumentList "/c", "mvnw.cmd spring-boot:run" -WindowStyle Minimized

Write-Host "Doi server len (toi da 90s)..." -ForegroundColor Cyan
$up = $false
for ($i = 0; $i -lt 30; $i++) {
    Start-Sleep -Seconds 3
    try {
        $resp = Invoke-WebRequest -Uri "http://localhost:8080/" -UseBasicParsing -TimeoutSec 3
        if ($resp.StatusCode -eq 200) { $up = $true; break }
    } catch {}
}
if (-not $up) {
    Write-Host "Server chua len sau 90s - kiem tra lai thu cong." -ForegroundColor Red
    exit 1
}
Write-Host "Server da san sang: http://localhost:8080" -ForegroundColor Green

Write-Host "== 2. Khoi dong cloudflared tunnel ==" -ForegroundColor Cyan
if (Test-Path $logFile) { Remove-Item $logFile -Force }
Start-Process -FilePath $cloudflared -ArgumentList "tunnel", "--url", "http://localhost:8080" `
    -RedirectStandardError $logFile -WindowStyle Hidden

Write-Host "Doi tunnel URL..." -ForegroundColor Cyan
$tunnelUrl = $null
for ($i = 0; $i -lt 15; $i++) {
    Start-Sleep -Seconds 2
    if (Test-Path $logFile) {
        $match = Select-String -Path $logFile -Pattern "https://[a-zA-Z0-9.-]*trycloudflare\.com" -ErrorAction SilentlyContinue | Select-Object -First 1
        if ($match) { $tunnelUrl = $match.Matches[0].Value; break }
    }
}

if ($tunnelUrl) {
    Write-Host ""
    Write-Host "===================================================" -ForegroundColor Yellow
    Write-Host " URL cong khai (HTTPS): $tunnelUrl" -ForegroundColor Yellow
    Write-Host " IPN URL cho SePay:     $tunnelUrl/checkout/sepay/ipn" -ForegroundColor Yellow
    Write-Host "===================================================" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "-> Vao my.sepay.vn -> Integrations -> Webhooks -> dan lai IPN URL o tren (URL doi moi lan khoi dong)." -ForegroundColor Cyan
} else {
    Write-Host "Khong lay duoc tunnel URL - xem log tai $logFile" -ForegroundColor Red
}

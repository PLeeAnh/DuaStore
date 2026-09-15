$f = "E:\Study\Class\JavaStudy\Website\DuaStore\DuaStore\src\main\resources\db\seed_data.json"
$c = [System.IO.File]::ReadAllText($f, [System.Text.Encoding]::UTF8)
$c = $c.Replace(',,', ',')
$c = $c.Replace('$pageViews', '')
[System.IO.File]::WriteAllText($f, $c, (New-Object System.Text.UTF8Encoding $false))
try { $j = $c | ConvertFrom-Json; Write-Host "VALID! Tables: $($j.tables.PSObject.Properties.Name.Count)" } catch { Write-Host "INVALID: $($_.Exception.Message)" }

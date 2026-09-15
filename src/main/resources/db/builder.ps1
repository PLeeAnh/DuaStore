$f = "E:\Study\Class\JavaStudy\Website\DuaStore\DuaStore\src\main\resources\db\seed_data.json"
$dir = "E:\Study\Class\JavaStudy\Website\DuaStore\DuaStore\src\main\resources\db\chunks"
$sw = [Diagnostics.Stopwatch]::StartNew()

if(Test-Path $dir){Remove-Item "$dir\*" -Force}
else{New-Item -ItemType Directory -Path $dir -Force | Out-Null}

function J($s){if($s -eq $null){return 'null'};return "`"$([string]$s.Replace('\','\\').Replace('"','\"'))`""}

# Write chunk files
function Out-Chunk($name,$json){Set-Content -Path "$dir\$name.txt" -Value $json -Encoding utf8 -NoNewline}

Write-Host "Generating chunks..."

# HEADER (combined with all table opens)
$hdr = '{"version":"V10","importOrder":["roles","permissions","role_permissions","users","user_roles","user_auth_providers","Categories","Products","ProductVariants","ProductImages","Promotions","Addresses","orders","order_items","order_notes","order_assignments","order_status_logs","admin_action_logs","PostCategories","Posts","Wishlists","banners","store_info","CartItems","Reviews","ReviewImages","ReviewReplies","UserVouchers","SiteSettings","FlashSales","FlashSaleItems","linked_accounts","Notifications","StockMovements","ContactReplies","ProductViews","UserActivityLogs","contact_messages","popup_banners","user_settings","CustomerNotes","CustomerTags","PriceHistory","footer_links","LoyaltyTransactions","LoyaltyBalances","SavedCartItems","PageViews"],"tables":{'
Out-Chunk "00_header" $hdr
Write-Host "  00_header done"

Write-Host "Chunks generated in $($sw.ElapsedMilliseconds)ms"

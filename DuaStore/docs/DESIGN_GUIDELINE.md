# DuaStore Admin — Design Guideline v2

## Design Philosophy

DuaStore Admin là hệ thống quản trị bán đồ thủy tinh cao cấp.

**Mục tiêu thiết kế:**
- Sạch sẽ, sáng, dễ đọc
- Sang trọng nhưng không quá màu mè
- Ưu tiên dữ liệu và thao tác quản trị
- Màu sắc nhẹ, nhiều khoảng trắng
- Không sử dụng hiệu ứng glassmorphism quá mạnh
- Không dùng gradient ở card thông thường
- Chỉ dùng gradient ở Hero Banner (dashboard)

**Lý do:**
Đây là Admin quản lý bán hàng thủy tinh, không phải dashboard fintech hay crypto.
Mọi thiết kế phải phục vụ thao tác nghiệp vụ, không chạy theo xu hướng.

## UX Priorities

Khi có xung đột giữa UI đẹp và nghiệp vụ, **nghiệp vụ luôn được ưu tiên**.

1. **Dữ liệu > Hiệu ứng** — Thông tin quan trọng nhất, animation/glassmorphism chỉ dùng điểm xuyết
2. **Thao tác nhanh > Trang trí** — Admin cần click ít nhất để hoàn thành tác vụ
3. **Nhất quán > Sáng tạo** — Dùng cùng component pattern cho cùng loại dữ liệu
4. **Khả năng đọc > Màu sắc** — Contrast đủ, font rõ, spacing thoáng
5. **Responsive > Pixel Perfect** — Hoạt động tốt trên mọi màn hình hơn là đẹp tuyệt đối ở một kích thước

---

## Color Tokens

| Token | Value | Mục đích |
|---|---|---|
| `--primary` | `#2563EB` | Màu chủ đạo, button, link, active |
| `--primary-light` | `#DBEAFE` | Hover, active background |
| `--secondary` | `#60A5FA` | Accent phụ |
| `--success` | `#16A34A` | Hoàn thành, đã thanh toán |
| `--warning` | `#F59E0B` | Chờ xác nhận, cảnh báo |
| `--danger` | `#DC2626` | Đã hủy, lỗi, xóa |
| `--bg` | `#F6F9FC` | Nền content |
| `--card` | `#FFFFFF` | Nền card, sidebar, header |
| `--text` | `#0F172A` | Chữ chính |
| `--muted` | `#64748B` | Chữ phụ, icon |
| `--border` | `#E2E8F0` | Đường viền |

---

## Typography

| Element | Font | Weight | Size |
|---|---|---|---|
| Logo | `Dancing Script` | 700 | 1.5rem |
| Body | `DM Sans` | 400 | .875rem |
| Heading h4 | `DM Sans` | 700 | 1.5rem |
| Table header | `DM Sans` | 600 | .7rem uppercase |
| Small / muted | `DM Sans` | 400 | .78rem |

### Font Scale
`.65rem` → `.72rem` → `.78rem` → `.82rem` → `.875rem` → `1rem` → `1.25rem` → `1.5rem` → `1.75rem`

---

## Spacing

| Context | Value |
|---|---|
| Card padding | 1.25rem – 1.5rem |
| Table cell | .85rem 1.25rem |
| Section gap (row gutter) | 1.5rem |
| Sidebar nav item padding | .55rem .85rem |
| Header horizontal padding | 1.5rem |

---

## Border Radius

| Component | Radius |
|---|---|
| Card | 20px |
| Button | 10px |
| Input / Select | 12px |
| Badge / Pill | 9999px |
| Sidebar nav link active | 12px |
| Avatar | 50% |
| Icon button | 50% |

---

## Shadow

| Layer | Shadow |
|---|---|
| Card | `0 10px 30px rgba(15,23,42,0.08)` |
| Stat hover | `0 15px 40px rgba(15,23,42,0.12)` |
| Dropdown | `0 10px 30px rgba(15,23,42,0.15)` |
| Button primary hover | `0 4px 12px rgba(37,99,235,0.3)` |

---

## Icon Guideline

Only **Bootstrap Icons**. No Font Awesome.

### Module Icons

| Module | Icon |
|---|---|
| Dashboard | `bi-grid` |
| Dashboard (stat orders) | `bi-bag-check` |
| Dashboard (stat revenue) | `bi-currency-exchange` |
| Dashboard (stat products) | `bi-box-seam` |
| Dashboard (stat customers) | `bi-people` |
| Product | `bi-box-seam` |
| Category | `bi-tags` |
| Order | `bi-receipt` |
| Promotion | `bi-percent` |
| User | `bi-people` |
| Role | `bi-shield-lock` |
| Audit Log | `bi-clock-history` |
| Review | `bi-star` |
| Post | `bi-file-earmark-text` |
| Back to site | `bi-arrow-left-circle` |

### Action Icons

| Action | Icon |
|---|---|
| Add / Create | `bi-plus-lg` |
| Edit | `bi-pencil` |
| Delete | `bi-trash` |
| View / Detail | `bi-eye` |
| Search | `bi-search` |
| Reset filter | `bi-x-lg` |
| Back | `bi-arrow-left` |
| Save | `bi-check-lg` |
| Send | `bi-send` |
| Filter | `bi-funnel` |

### Status Icons (Timeline)

| Event | Icon |
|---|---|
| CREATE_ORDER | `bi-cart-plus` |
| ASSIGN_ADMIN | `bi-person-check` |
| STATUS_CHANGE | `bi-arrow-left-right` |
| CANCEL_ORDER | `bi-x-circle` |
| PAYMENT_CONFIRMED | `bi-credit-card` |

---

## Status Colors (Order)

| Trạng thái | Class | Màu |
|---|---|---|
| Chờ xác nhận | `status-cho-xac-nhan` | warning (#F59E0B) |
| Đã xác nhận | `status-da-xac-nhan` | primary (#2563EB) |
| Đang giao | `status-dang-giao` | cyan (#06B6D4) |
| Đã giao | `status-da-giao` | indigo (#4F46E5) |
| Đã hoàn thành | `status-da-hoan-thanh` | success (#16A34A) |
| Đã hủy | `status-da-huy` | danger (#DC2626) |

**Cách dùng:**
```html
<span class="status-pill status-cho-xac-nhan">Chờ xác nhận</span>
```

## Payment Status Colors

| Trạng thái | Class badge | Màu |
|---|---|---|
| Chưa thanh toán | `badge bg-warning text-dark` | warning |
| Đã thanh toán | `badge bg-success` | success |
| Công nợ | `badge bg-info text-dark` | info (#0EA5E9) |

---

## Status Rules: Order vs Payment

**Order Status và Payment Status là hai trạng thái độc lập.**
UI không được giả định `DA_HOAN_THANH ⇒ DA_THANH_TOAN`.

### Ma trận hợp lệ

| Order | Payment | Ghi chú |
|---|---|---|
| CHO_XAC_NHAN | CHUA_THANH_TOAN | Mới tạo, chưa thanh toán |
| DA_XAC_NHAN | CHUA_THANH_TOAN | Đã xác nhận, chưa thanh toán |
| DANG_GIAO | DA_THANH_TOAN | Đang giao, đã thanh toán |
| DA_GIAO | DA_THANH_TOAN | Đã giao, đã thanh toán |
| DA_HOAN_THANH | DA_THANH_TOAN | Hoàn thành, đã thanh toán |
| DA_HOAN_THANH | CONG_NO | Hoàn thành, công nợ |
| DA_HUY | * | Đã hủy, không quan tâm payment |

### Luồng đặc biệt: Hoàn thành khi chưa thanh toán
1. Admin chọn `DA_HOAN_THANH` + payment là `CHUA_THANH_TOAN`
2. Hệ thống hiển thị modal xác nhận
3. Nếu admin xác nhận → `trangThaiDon = DA_HOAN_THANH`, `trangThaiTT = CONG_NO`
4. Nếu admin hủy → không thay đổi trạng thái

### Luồng thanh toán Online / QR
- Khi khách thanh toán online/QR thành công → `trangThaiTT = DA_THANH_TOAN` tự động
- Timeline ghi nhận `PAYMENT_CONFIRMED`

---

## Empty State Guideline

Dùng khi danh sách rỗng:

```html
<div class="adm-empty">
    <i class="bi bi-inbox adm-empty-icon"></i>
    <h6 class="adm-empty-title">Chưa có dữ liệu</h6>
    <p class="adm-empty-desc">Mô tả ngắn gọn</p>
    <a href="..." class="btn btn-primary btn-sm">Thêm mới</a>
</div>
```

**Thiết kế:**
- Icon lớn (2.5rem), màu muted
- Title `.85rem`, `font-weight: 600`
- Description `.78rem`, màu muted
- CTA button (tuỳ chọn)
- Padding dọc 3rem

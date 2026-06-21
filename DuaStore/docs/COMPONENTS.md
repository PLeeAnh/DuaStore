# DuaStore Admin — Components Catalog

## 1. Layout Components

```css
.adm-wrapper         /* display:flex; min-height:100vh */
.adm-sidebar         /* fixed left, width: var(--sidebar-w) */
.adm-main            /* margin-left: var(--sidebar-w); flex:1 */
.adm-topbar          /* sticky top, height: var(--header-h) */
.adm-content         /* flex:1; padding:1.5rem; background:var(--bg) */
.adm-footer          /* border-top; padding:.75rem 1.5rem; text-align:center */
```

---

## 2. Sidebar Components

| Class | Mô tả |
|---|---|
| `.adm-sidebar` | Fixed left, 280px, white bg, border-right |
| `.adm-sidebar-logo` | Logo + sub-label, border-bottom |
| `.adm-logo` | Logo text, font Dancing Script |
| `.adm-logo-sub` | "Admin Panel", .65rem uppercase muted |
| `.adm-nav` | Scrollable nav container |
| `.adm-nav-section` | Section label uppercase muted |
| `.adm-nav-link` | Nav item: flex, icon + label, 12px radius |
| `.adm-nav-link.active` | Primary light bg, primary text |
| `.adm-nav-link:hover` | Primary light bg |
| `.adm-sidebar-glass` | SVG illustration container |

---

## 3. Header Components

| Class | Mô tả |
|---|---|
| `.adm-topbar-left` | Toggle + breadcrumb |
| `.adm-topbar-right` | Notification + profile |
| `.adm-toggle` | Mobile toggle button |
| `.adm-notif-btn` | Bell icon button, position relative |
| `.adm-notif-badge` | Absolute badge top-right |
| `.adm-profile-wrap` | Relative wrapper |
| `.adm-profile-trigger` | Avatar + name + chevron |
| `.adm-profile-avatar` | 32px circle, primary bg |
| `.adm-profile-name` | .85rem, 500 weight |
| `.adm-profile-dropdown` | Absolute dropdown menu |

---

## 4. Content Components

### Hero Banner (Dashboard only)

```css
.adm-hero                /* gradient bg, border-radius:20px, padding:2rem, min-height:140px */
.adm-hero-content         /* relative z-index:2 */
.adm-hero-decoration      /* absolute right, SVG decoration */
```

### Page Header (CRUD pages)

```css
.adm-page-header          /* display:flex, justify-content:space-between, mb:1.5rem */
.adm-page-header h4       /* font-size:1.25rem, font-weight:700 */
```

### Metric / Stat Cards

```css
.adm-stat                /* card, border-radius:20px, padding:1.25rem, shadow, border */
.adm-stat:hover           /* shadow tăng, translateY(-2px) */
.adm-stat-icon            /* 48x48, border-radius:14px, flex center */
.adm-stat-value           /* 1.75rem, 700 weight */
.adm-stat-label           /* .8rem, muted */
```

### Generic Card

```css
.adm-card                /* card, border-radius:20px, shadow, border, padding:1.5rem */
.adm-card-header          /* flex space-between, .85rem, 600 weight */
.adm-card-body            /* flex:1 */
```

### Search & Filter Bar

```css
.adm-filter-bar           /* card container cho search/filter */
.adm-search-box           /* input search */
.adm-filter-group         /* group of filter fields */
```

### Empty State

```css
.adm-empty                /* text-align:center, padding:3rem */
.adm-empty-icon           /* font-size:2.5rem, color:muted */
.adm-empty-title          /* .85rem, 600 weight, mt:1rem */
.adm-empty-desc           /* .78rem, muted, mt:.25rem */
```

### Tabs

```css
.adm-tab-nav              /* inline-flex, pill container */
.adm-tab                  /* pill button/link */
.adm-tab.active           /* active state */
```

### Timeline

```css
.adm-timeline             /* max-height:400px, overflow-y:auto */
.adm-timeline-item        /* flex, gap:.85rem, pb:1rem, mb:1rem, border-bottom */
.adm-timeline-icon        /* 36x36, rounded-circle, flex center */
.adm-timeline-content     /* flex-grow:1 */
.adm-timeline-header      /* flex justify-between */
.adm-timeline-user        /* .82rem, 600 weight */
.adm-timeline-time        /* .72rem, muted */
.adm-timeline-body        /* mt:.25rem, .82rem */
```

---

## 5. Table Components

```css
.adm-table-wrap           /* card, border-radius:20px, overflow:hidden */
.adm-table-wrap .table th /* sticky top:0, z-index:3, uppercase, .7rem, muted */
.adm-table-wrap .table td /* .875rem, vertical-align:middle */
```

---

## 6. Form Components

```css
.form-control             /* border-radius:12px, border:1.5px solid --border */
.form-control:focus       /* border-color:--primary, box-shadow primary glow */
.form-select              /* same as .form-control */
.form-label               /* .82rem, 500 weight, mb:.35rem */
```

---

## 7. Button Components

| Class | Style |
|---|---|
| `.btn-primary` | Solid primary, 10px radius, hover glow |
| `.btn-outline-primary` | Border primary, hover solid |
| `.btn-outline-secondary` | Border muted |
| `.btn-outline-danger` | Border danger |
| `.btn-icon` | 36x36, 50% radius, icon-only |
| `.btn-sm` | Small size |

---

## 8. Status Badge / Pill

| Class | Dùng cho |
|---|---|
| `.status-pill` | Base class |
| `.status-cho-xac-nhan` | Chờ xác nhận |
| `.status-da-xac-nhan` | Đã xác nhận |
| `.status-dang-giao` | Đang giao |
| `.status-da-giao` | Đã giao |
| `.status-da-hoan-thanh` | Đã hoàn thành |
| `.status-da-huy` | Đã hủy |

```html
<span class="status-pill status-cho-xac-nhan">Chờ xác nhận</span>
```

---

## 9. Progress Bar (Order Detail)

| Class | Mô tả |
|---|---|
| `.progress-step-bar` | Flex container, relative |
| `.step-circle` | 52px circle, z-index:2 |
| `.step-circle.completed` | bg-success |
| `.step-circle.active` | bg-primary + glow ring |
| `.step-circle.pending` | bg-gray (#E2E8F0) |
| `.step-label` | Below circle, .75rem |
| `.step-line` | Absolute connecting line, z-index:1 |
| `.step-line-fill` | bg-success, animated width |

---

## 10. Pagination Components

| Class | Mô tả |
|---|---|
| `.adm-pagination-btn` | Page number, 36x36, 10px radius |
| `.adm-pagination-btn.active` | Primary bg |
| `.adm-pagination-nav-btn` | Prev/next button |

---

## 11. Toast Components

Use Bootstrap toast with custom CSS:

| Class | Mô tả |
|---|---|
| `.toast` | Border-radius 14px, shadow |
| `.toast-header` | Transparent bg |
| `#toastContainer` | Fixed top-right, z-index 9999 |

---

## 12. Modal Components

Use Bootstrap modal with custom CSS:

| Class | Mô tả |
|---|---|
| `.modal-content` | border-radius 20px, no border |
| `.modal-header` | border-bottom |
| `.modal-body` | padding 1.25rem 1.5rem |
| `.modal-footer` | border-top |

---

## 13. Table UX Rules

- **Header luôn sticky**: `position: sticky; top: 0; z-index: 3`
- **Không quá 8 cột trên desktop** — nếu quá, nhóm/gộp cột
- **Mobile phải scroll ngang**: `overflow-x: auto` trên `.adm-table-wrap`
- **Hành động (Sửa/Xóa/Xem) luôn nằm cột cuối cùng**
- **Trạng thái luôn hiển thị bằng badge/pill** — không dùng icon hoặc text trần
- **ID/Mã không dùng màu nổi bật** — chỉ text muted hoặc normal weight
- **Giá tiền căn phải** (`text-end` hoặc `text-right`)
- **Ngày giờ dùng định dạng**: `dd/MM/yyyy HH:mm`

---

## 14. Form UX Rules

- **Label nằm trên input** — không dùng placeholder làm label
- **Field bắt buộc có dấu `*`** trong label
- **Validation message nằm dưới field** — dùng `.form-text` hoặc `.invalid-feedback`
- **Submit button nằm góc phải** của card/form
- **Nút Hủy nằm bên trái nút Lưu** — Cancel → Save
- **Không dùng placeholder thay label** — placeholder chỉ là gợi ý

---

## 15. Empty State Variants

| Module | Icon | Title |
|---|---|---|
| Products | `bi-box-seam` | Chưa có sản phẩm |
| Orders | `bi-receipt` | Chưa có đơn hàng |
| Reviews | `bi-star` | Chưa có đánh giá |
| Users | `bi-people` | Chưa có người dùng |
| Categories | `bi-tags` | Chưa có danh mục |
| Posts | `bi-file-earmark-text` | Chưa có bài viết |
| Promotions | `bi-percent` | Chưa có khuyến mãi |
| Audit Log | `bi-clock-history` | Chưa có nhật ký |
| Roles | `bi-shield-lock` | Chưa có vai trò |
| Generic | `bi-inbox` | Chưa có dữ liệu |

---

## 16. Payment Method Icons

| Method | Icon |
|---|---|
| COD | `bi-cash-stack` |
| CHUYEN_KHOAN / BANK_TRANSFER | `bi-bank` |
| VNPAY | `bi-credit-card` |
| MOMO | `bi-wallet2` |
| ZALOPAY | `bi-wallet2` |
| Other / Unknown | `bi-credit-card` |

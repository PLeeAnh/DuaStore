# DuaStore Admin — Color Themes

## Theme Variables

```css
:root {
  --primary
  --primary-light
  --secondary

  --success     /* KHÔNG ĐỔI — gắn nghiệp vụ đơn hàng */
  --warning     /* KHÔNG ĐỔI — gắn nghiệp vụ đơn hàng */
  --danger      /* KHÔNG ĐỔI — gắn nghiệp vụ đơn hàng */

  --bg
  --card
  --text
  --muted
  --border
}
```

**Lưu ý quan trọng:**
- `--success` (xanh lá) = Đã hoàn thành, Đã thanh toán
- `--warning` (vàng) = Chờ xác nhận, Chưa thanh toán
- `--danger` (đỏ) = Đã hủy, lỗi, xóa

Ba màu này gắn trực tiếp với nghiệp vụ đơn hàng nên **giữ nguyên qua mọi theme**.

---

## Blue (Default)

```css
--primary: #2563EB
--primary-light: #DBEAFE
--secondary: #60A5FA

--bg: #F6F9FC
--card: #FFFFFF
--text: #0F172A
--muted: #64748B
--border: #E2E8F0
```

Giao diện mặc định. Xanh dương primary trung tính, dễ nhìn.

---

## Emerald (Xanh ngọc)

```css
--primary: #059669
--primary-light: #D1FAE5
--secondary: #34D399
```

Cảm giác tự nhiên, sang trọng. Phù hợp concept "thủy tinh xanh".

---

## Purple (Tím)

```css
--primary: #7C3AED
--primary-light: #EDE9FE
--secondary: #A78BFA
```

Cảm giác cao cấp, royal. Phù hợp cửa hàng pha lê tím.

---

## Amber (Cam)

```css
--primary: #D97706
--primary-light: #FEF3C7
--secondary: #FBBF24
```

Cảm giác ấm áp. Phù hợp cửa hàng thủy tinh vàng/pha lê.

---

## Rose (Hồng)

```css
--primary: #E11D48
--primary-light: #FFE4E6
--secondary: #FB7185
```

Cảm giác nhẹ nhàng, nữ tính.

---

## Dark Mode (Reserve)

Chưa có UI toggle, nhưng `[data-theme="dark"]` đã được khai báo sẵn trong `admin-v2.css`:

```css
[data-theme="dark"] {
  --bg: #0F172A;
  --card: #1E293B;
  --text: #F1F5F9;
  --muted: #94A3B8;
  --border: #334155;
}
```

- `--primary`, `--primary-light`, `--secondary` giữ nguyên (vẫn theo theme hiện tại)
- `--success/warning/danger` giữ nguyên (nghiệp vụ)
- Chỉ đổi màu nền, card, text, border

---

## Cách đổi theme

Chỉ cần thay đổi 3 variables trong `admin-v2.css`:

```css
:root {
  --primary: #7C3AED;       /* ← đổi sang Purple */
  --primary-light: #EDE9FE; /* ← đổi theo */
  --secondary: #A78BFA;     /* ← đổi theo */
}
```

Mọi component dùng CSS variables sẽ tự động đổi màu. **Không cần sửa HTML.**

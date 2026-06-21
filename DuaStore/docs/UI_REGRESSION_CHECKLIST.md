# DuaStore Admin — UI Regression Checklist

Sử dụng checklist này **sau mỗi template redesign** để đảm bảo không phát sinh lỗi hồi quy.

---

## Render & Console

- [ ] **Render thành công**: Trang load không lỗi HTTP 500
- [ ] **Không lỗi Thymeleaf**: Không có `TemplateProcessingException` hay lỗi parse
- [ ] **Không lỗi JS Console**: Không có lỗi JavaScript, không có lỗi 404 cho static assets

---

## UI Components

- [ ] **Không lỗi Bootstrap Modal**: Modal mở/đóng được, không bị che
- [ ] **Không lỗi Dropdown**: Profile dropdown, filter dropdown hoạt động
- [ ] **Không lỗi Pagination**: Chuyển trang, active state, prev/next hoạt động
- [ ] **Không lỗi Search**: Input search + submit hoạt động
- [ ] **Không lỗi Filter**: Select filter + submit/reset hoạt động
- [ ] **Không lỗi Form Submit**: POST form redirect đúng trang
- [ ] **Không lỗi Validation Message**: `BindingResult` errors hiển thị
- [ ] **Không lỗi Toast**: `successMsg`/`errorMsg`/`warningMsg` hiển thị dạng toast

---

## Business Logic

- [ ] **Không lỗi Permission**: Trang không báo 403 sai
- [ ] **Không lỗi Empty State**: Danh sách rỗng hiển thị đúng
- [ ] **Không lỗi Order Status**: Status pill hiển thị đúng màu
- [ ] **Không lỗi Payment Status**: Payment badge hiển thị đúng
- [ ] **Không lỗi Progress Bar**: Step hiển thị đúng completed/active/pending
- [ ] **Không lỗi Timeline**: Timeline ASC, icon đúng event type

---

## Cross-Browser & Responsive

- [ ] **Desktop (≥992px)**: Sidebar visible, layout đúng
- [ ] **Tablet (768-991px)**: Sidebar overlay, toggle hoạt động
- [ ] **Mobile (<768px)**: Table horizontal scroll, padding giảm

---

## Theme & Variables

- [ ] **CSS Variables**: Tất cả màu dùng `var(--*)`, không hardcode
- [ ] **Dark Mode**: `[data-theme="dark"]` không làm vỡ layout (nếu test)
- [ ] **Không lỗi màu trạng thái**: Status colors đúng theo guideline

---

## JS Selector Integrity

- [ ] `data-confirm` vẫn hoạt động (confirm dialog khi xóa)
- [ ] `data-toast-msg` vẫn hoạt động (toast notification)
- [ ] `#toastContainer` tồn tại
- [ ] `#adminProfileModal` tồn tại
- [ ] `#adminChangePasswordModal` tồn tại
- [ ] `#admNavToggle` hoạt động (toggle sidebar)
- [ ] `#admProfileTrigger` + `#admProfileDropdown` hoạt động

---

## Kết luận

- [ ] **PASS** — Tất cả mục ở trên đều OK, template sẵn sàng cho phase tiếp theo
- [ ] **FAIL** — Có item lỗi, cần fix trước khi chuyển phase

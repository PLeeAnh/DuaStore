# DuaStore Admin — UI Migration Checklist

Sử dụng checklist này **trước khi redesign** mỗi template để đảm bảo không làm hỏng chức năng hiện có.

---

## Pre-Migration Audit

- [ ] **Layout**: Giữ nguyên `layout:decorate="~{layout/admin/base}"`
- [ ] **Thymeleaf bindings**: Không đổi `th:field`, `th:object`, `th:each`, `th:if`, `th:action`, `th:href`, `th:switch`, `th:case`, `th:replace`, `th:value`, `th:text`
- [ ] **Form action**: Không đổi URL
- [ ] **Form method**: Giữ nguyên POST/GET
- [ ] **Input name/id**: Không đổi tên field backend đang bind
- [ ] **Modal id**: Giữ nguyên (`#adminProfileModal`, `#adminChangePasswordModal`)
- [ ] **JS selectors**: Không đổi id/class/data-attribute đang dùng trong JS
- [ ] **Pagination**: Giữ nguyên `th:replace="~{view/admin/fragments/pagination :: pagination(...)}"`
- [ ] **AJAX endpoint**: Không đổi URL
- [ ] **Permission checks**: Giữ nguyên `th:if="${userPermissions.contains('...')}"`
- [ ] **Toast**: Giữ nguyên `data-toast-msg`, `#toastContainer`
- [ ] **URL params**: Giữ nguyên tham số query

---

## Template-Specific Audit

### Liệt kê các thành phần sau đây trước khi code:

| Thành phần | Giá trị hiện tại | Giữ nguyên? |
|---|---|---|
| Model variables | ... | ✅/❌ |
| Form action | ... | ✅/❌ |
| Input name/id | ... | ✅/❌ |
| JS events | ... | ✅/❌ |
| API calls | ... | ✅/❌ |
| Permission conditions | ... | ✅/❌ |
| Pagination | ... | ✅/❌ |
| Modal triggers | ... | ✅/❌ |
| Toast messages | ... | ✅/❌ |

---

## Post-Migration Test

- [ ] **Render**: Template render thành công, không lỗi Thymeleaf
- [ ] **JS Console**: Không lỗi JavaScript
- [ ] **Modal**: Bootstrap modal hoạt động
- [ ] **Dropdown**: Dropdown menu hoạt động
- [ ] **Pagination**: Chuyển trang hoạt động
- [ ] **Search/Filter**: Tìm kiếm và lọc hoạt động
- [ ] **Form submit**: Submit form thành công
- [ ] **Validation**: Message validation hiển thị đúng
- [ ] **Toast**: Thông báo toast hiển thị
- [ ] **Responsive**: Giao diện hiển thị tốt trên desktop/tablet/mobile
- [ ] **Permission**: Không lỗi 403 ngoài ý muốn
- [ ] **Empty state**: Trang hiển thị đúng khi không có dữ liệu
- [ ] **Dark mode**: CSS variables hoạt động với `[data-theme="dark"]`

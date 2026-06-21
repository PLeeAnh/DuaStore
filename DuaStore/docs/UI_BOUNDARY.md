# DuaStore Admin — UI Boundary Rules

## Frontend được phép sửa

- **HTML structure**: Thay đổi layout, thứ tự, wrapper div, class
- **CSS**: Tất cả style, CSS variables, responsive
- **JS UI interactions**: Sidebar toggle, dropdown, toast, modal show/hide
- **Bootstrap classes**: Thay đổi grid, component classes
- **Icons**: Đổi icon trong Bootstrap Icons
- **Responsive layout**: Breakpoints, mobile menu
- **Thêm class mới**: Không xóa class cũ đang dùng trong JS

## Frontend KHÔNG được sửa

| Thành phần | Lý do |
|---|---|
| **Entity / Model** | Backend data mapping |
| **Repository** | Data access layer |
| **Service** | Business logic |
| **Controller** | Request handling, URL mapping |
| **DTO** | Form data binding |
| **Security** | Authentication, authorization |
| **Validation annotations** | `@NotBlank`, `@Email`, `@Size`... |
| **API URL** | `@PostMapping("/{id}/cap-nhat-trang-thai")`... |
| **Thymeleaf variable names** | `order`, `orders`, `currentPage`, `totalPages`, `statusLogs`... |
| **Form field names** | `trangThaiDon`, `trangThaiTT`, `hoTen`, `email`... |
| **Input id/name** | Backend đang bind vào |
| **JS selector cũ** | `data-confirm`, `data-toast-msg`, `#toastContainer`, `#adminProfileModal`, `#adminChangePasswordModal`, `#admNavToggle`, `#admProfileTrigger`, `#admProfileDropdown` |

## Nếu muốn thay đổi nghiệp vụ

1. Tạo **proposal riêng** mô tả thay đổi
2. Nêu rõ: file nào cần sửa, logic cũ → mới
3. Không tự ý sửa Controller/Service/Repository
4. Proposal phải được approve trước khi implement

## Rule vàng

> Nếu không chắc file đó có thuộc frontend không → KHÔNG SỬA.
> 
> Khi nghi ngờ, hỏi trước.

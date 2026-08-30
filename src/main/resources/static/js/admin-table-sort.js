/**
 * admin-table-sort.js
 * Xử lý sort theo cột cho bảng admin.
 * Sử dụng: thêm class "sortable" vào <th>, data-sort="fieldName" cho cột cần sort.
 * Khi bấm header → cập nhật URL params ?sort=field&dir=asc/desc
 */
(function () {
    'use strict';

    document.addEventListener('DOMContentLoaded', function () {
        var params = new URLSearchParams(window.location.search);
        var currentSort = params.get('sort') || '';
        var currentDir = params.get('dir') || 'asc';

        document.querySelectorAll('th.sortable').forEach(function (th) {
            var field = th.getAttribute('data-sort');
            if (!field) return;

            // Hiển thị icon sort hiện tại
            var icon = document.createElement('i');
            icon.className = 'bi ms-1';
            icon.style.fontSize = '.7rem';
            icon.style.opacity = '.5';
            if (currentSort === field) {
                icon.className += currentDir === 'asc' ? ' bi-arrow-up' : ' bi-arrow-down';
                icon.style.opacity = '1';
                th.style.cursor = 'pointer';
            } else {
                icon.className += ' bi-arrow-down-up';
            }
            th.appendChild(icon);

            // Click handler
            th.style.cursor = 'pointer';
            th.addEventListener('click', function () {
                var newParams = new URLSearchParams(window.location.search);
                if (currentSort === field) {
                    // Đảo chiều
                    newParams.set('dir', currentDir === 'asc' ? 'desc' : 'asc');
                } else {
                    newParams.set('sort', field);
                    newParams.set('dir', 'asc');
                }
                newParams.delete('page'); // Reset về trang 1 khi sort
                window.location.search = newParams.toString();
            });
        });
    });
})();

'use strict';
document.addEventListener('DOMContentLoaded', function () {
    const tbody = document.querySelector('.sortable-banners');
    if (!tbody) return;

    new Sortable(tbody, {
        handle: '.drag-handle',
        animation: 200,
        onEnd: function () {
            const ids = Array.from(tbody.querySelectorAll('tr[data-id]'))
                    .map(function (tr) { return parseInt(tr.getAttribute('data-id'), 10); });

            const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
            const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

            fetch('/admin/banner/api/reorder', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    [csrfHeader]: csrfToken
                },
                body: JSON.stringify({ order: ids })
            })
            .then(function (r) { return r.json(); })
            .then(function (data) {
                if (data.success) {
                    DuaStore.toast.success('Đã sắp xếp lại banner');
                } else {
                    DuaStore.toast.error(data.message || 'Sắp xếp thất bại');
                }
            })
            .catch(function () {
                DuaStore.toast.error('Lỗi kết nối');
            });
        }
    });
});

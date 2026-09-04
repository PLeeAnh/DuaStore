'use strict';
function submitBulkEdit(event) {
    event.preventDefault();
    const form = document.getElementById('bulkEditForm');
    const rows = form.querySelectorAll('tbody tr');
    const data = [];
    rows.forEach(function(row) {
        const idInput = row.querySelector('input[name$=".id"]');
        const versionInput = row.querySelector('input[name$=".version"]');
        const giaInput = row.querySelector('input[name$=".giaBan"]');
        const tonInput = row.querySelector('input[name$=".soLuongTon"]');
        if (idInput && giaInput && tonInput) {
            data.push({
                id: parseInt(idInput.value),
                version: versionInput ? parseInt(versionInput.value) : null,
                giaBan: parseFloat(giaInput.value) || 0,
                soLuongTon: parseInt(tonInput.value) || 0
            });
        }
    });
    if (data.length === 0) return;
    const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
    fetch('/admin/bien-the/api/bulk-save', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            [csrfHeader]: csrfToken
        },
        body: JSON.stringify(data)
    })
    .then(function(r) { return r.json(); })
    .then(function(result) {
        if (result.success) {
            var skipped = result.skippedIds || [];
            if (skipped.length > 0) {
                if (window.dsToast) dsToast('error',
                    'Đã lưu, nhưng ' + skipped.length + ' dòng bị bỏ qua vì dữ liệu đã bị thay đổi bởi thao tác khác (đơn hàng vừa giao/hủy...). Vui lòng tải lại trang để xem số liệu mới nhất: #' + skipped.join(', #'));
            } else if (window.dsToast) {
                dsToast('success', 'Lưu thành công!');
            }
        } else {
            if (window.dsToast) dsToast('error', 'Có lỗi xảy ra khi lưu.');
        }
    })
    .catch(function() { if (window.dsToast) dsToast('error', 'Lỗi kết nối.'); });
    return false;
}

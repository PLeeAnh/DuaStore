'use strict';
function submitBulkEdit(event) {
    event.preventDefault();
    const form = document.getElementById('bulkEditForm');
    const rows = form.querySelectorAll('tbody tr');
    const data = [];
    rows.forEach(function(row) {
        const idInput = row.querySelector('input[name$=".id"]');
        const giaInput = row.querySelector('input[name$=".giaBan"]');
        const tonInput = row.querySelector('input[name$=".soLuongTon"]');
        if (idInput && giaInput && tonInput) {
            data.push({
                id: parseInt(idInput.value),
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
            if (window.dsToast) { dsToast('success', 'Lưu thành công!'); }
            else {
                var tc = document.getElementById('toastContainer');
                if (tc) {
                    var el = document.createElement('div');
                    el.className = 'ds-toast ds-toast-success';
                    el.innerHTML = '<i class="bi bi-check-circle-fill"></i><span>Lưu thành công!</span><button class="ds-toast-close">&times;</button>';
                    tc.appendChild(el);
                    el.querySelector('.ds-toast-close').addEventListener('click', function () { el.remove(); });
                    setTimeout(function () { if (el.parentNode) { el.style.animation = 'ds-toast-fade-out .3s ease forwards'; setTimeout(function () { el.remove(); }, 300); } }, 3000);
                }
            }
        } else {
            alert('Có lỗi xảy ra khi lưu.');
        }
    })
    .catch(function() { alert('Lỗi kết nối.'); });
    return false;
}

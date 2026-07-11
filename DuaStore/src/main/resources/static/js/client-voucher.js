document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('.btn-remove-voucher').forEach(function (btn) {
        btn.addEventListener('click', function (e) {
            e.stopPropagation();
            removeVoucher(this.getAttribute('data-id'));
        });
    });
});

function removeVoucher(voucherId) {
    if (!confirm('X\xf3a voucher n\xe0y?'))
        return;
    var token = document.querySelector('meta[name="_csrf"]')?.content || '';
    var header = document.querySelector('meta[name="_csrf_header"]')?.content || 'X-CSRF-TOKEN';
    fetch('/api/vi-voucher/xoa/' + voucherId, {
        method: 'POST',
        headers: {[header]: token}
    })
            .then(function (r) {
                if (r.status === 401 || r.status === 403) {
                    if (typeof showLoginPopup === 'function')
                        showLoginPopup();
                    return null;
                }
                return r.json();
            })
            .then(function (data) {
                if (!data)
                    return;
                if (data.success) {
                    if (typeof DuaStore !== 'undefined' && DuaStore.toast) { DuaStore.toast.success('Đã xóa voucher'); }
                    setTimeout(function () {
                        location.reload();
                    }, 800);
                } else {
                    if (typeof DuaStore !== 'undefined' && DuaStore.toast) { DuaStore.toast.error(data.message); }
                }
            });
}

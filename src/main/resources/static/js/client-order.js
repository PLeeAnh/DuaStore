function confirmReceived(btn) {
    if (!confirm('Xác nhận bạn đã nhận được hàng?')) return;
    var orderId = btn.getAttribute('data-order-id');
    var token = document.querySelector('meta[name="_csrf"]')?.content || '';
    var header = document.querySelector('meta[name="_csrf_header"]')?.content || 'X-CSRF-TOKEN';
    var headers = {};
    if (token) headers[header] = token;
    fetch('/tai-khoan/don-hang/danh-dau-da-nhan/' + orderId, {
        method: 'POST', headers: headers
    }).then(function (r) {
        if (r.ok) { window.location.reload(); }
        else if (typeof DuaStore !== 'undefined' && DuaStore.toast) { DuaStore.toast.error('Có lỗi xảy ra'); }
    }).catch(function () { if (typeof DuaStore !== 'undefined' && DuaStore.toast) DuaStore.toast.error('Lỗi kết nối'); });
}

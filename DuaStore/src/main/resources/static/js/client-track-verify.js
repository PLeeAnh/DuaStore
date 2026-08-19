function doVerify() {
    var maDon = document.getElementById('inputMaDon').value.trim();
    var phone = document.getElementById('inputPhone').value.trim();
    var btn = document.getElementById('verifyBtn');
    if (!maDon) { if (typeof DuaStore !== 'undefined' && DuaStore.toast) { DuaStore.toast.warning('Vui lòng nhập mã đơn hàng'); } return; }
    if (!phone) { if (typeof DuaStore !== 'undefined' && DuaStore.toast) { DuaStore.toast.warning('Vui lòng nhập SĐT'); } return; }
    btn.disabled = true;
    btn.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span>Đang kiểm tra...';
    var csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
    var csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;
    var headers = {'Content-Type': 'application/x-www-form-urlencoded'};
    if (csrfHeader && csrfToken) headers[csrfHeader] = csrfToken;
    fetch('/tracking/verify', {
        method: 'POST',
        headers: headers,
        body: new URLSearchParams({maDon: maDon, phone: phone})
    }).then(function (r) { return r.json(); })
    .then(function (data) {
        if (data.success) {
            document.getElementById('verifyForm').classList.add('d-none');
            document.getElementById('verifySuccess').classList.remove('d-none');
            window.location.href = data.redirect;
        } else {
            if (typeof DuaStore !== 'undefined' && DuaStore.toast) { DuaStore.toast.error(data.message || 'Không tìm thấy đơn hàng'); }
            btn.disabled = false;
            btn.innerHTML = '<i class="bi bi-search me-1"></i>Tra cứu';
        }
    }).catch(function () {
        if (typeof DuaStore !== 'undefined' && DuaStore.toast) { DuaStore.toast.error('Lỗi kết nối, vui lòng thử lại'); }
        btn.disabled = false;
        btn.innerHTML = '<i class="bi bi-search me-1"></i>Tra cứu';
    });
}

document.addEventListener('DOMContentLoaded', function () {
    document.getElementById('inputMaDon').addEventListener('keydown', function (e) { if (e.key === 'Enter') doVerify(); });
    document.getElementById('inputPhone').addEventListener('keydown', function (e) { if (e.key === 'Enter') doVerify(); });
});

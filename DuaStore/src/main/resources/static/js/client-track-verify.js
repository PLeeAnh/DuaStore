function doVerify() {
    var maDon = document.getElementById('inputMaDon').value.trim();
    var phone = document.getElementById('inputPhone').value.trim();
    var errEl = document.getElementById('verifyError');
    var btn = document.getElementById('verifyBtn');
    if (!maDon) { errEl.textContent = 'Vui lòng nhập mã đơn hàng'; errEl.classList.remove('d-none'); return; }
    if (!phone) { errEl.textContent = 'Vui lòng nhập SĐT'; errEl.classList.remove('d-none'); return; }
    errEl.classList.add('d-none');
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
            errEl.textContent = data.message || 'Không tìm thấy đơn hàng';
            errEl.classList.remove('d-none');
            btn.disabled = false;
            btn.innerHTML = '<i class="bi bi-search me-1"></i>Tra cứu';
        }
    }).catch(function () {
        errEl.textContent = 'Lỗi kết nối, vui lòng thử lại';
        errEl.classList.remove('d-none');
        btn.disabled = false;
        btn.innerHTML = '<i class="bi bi-search me-1"></i>Tra cứu';
    });
}

document.addEventListener('DOMContentLoaded', function () {
    document.getElementById('inputMaDon').addEventListener('keydown', function (e) { if (e.key === 'Enter') doVerify(); });
    document.getElementById('inputPhone').addEventListener('keydown', function (e) { if (e.key === 'Enter') doVerify(); });
});

function sendCode() {
    var email = document.getElementById('regEmail').value.trim();
    if (!email || !email.includes('@')) {
        if (typeof DuaStore !== 'undefined' && DuaStore.toast) { DuaStore.toast.warning('Email không hợp lệ'); }
        return;
    }
    var btn = document.getElementById('sendCodeBtn');
    btn.disabled = true;
    btn.textContent = 'Đang gửi...';

    fetch('/api/auth/send-code', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({email: email})
    }).then(function (r) { return r.json(); })
    .then(function (data) {
        if (data.success) {
            document.getElementById('otpRow').style.display = '';
            var statusTxt = 'Mã đã gửi đến email của bạn';
            if (data.dev_code) { statusTxt += ' (dev: ' + data.dev_code + ')'; }
            document.getElementById('codeStatus').textContent = statusTxt;
            btn.textContent = 'Gửi lại';
            btn.disabled = false;
        } else {
            btn.textContent = 'Thử lại';
            btn.disabled = false;
            var errMsg = data.error || 'Gửi mã thất bại';
            if (typeof DuaStore !== 'undefined' && DuaStore.toast) { DuaStore.toast.error(errMsg); }
        }
    }).catch(function () {
        btn.textContent = 'Thử lại';
        btn.disabled = false;
    });
}

document.getElementById('registerForm').addEventListener('submit', function (e) {
    e.preventDefault();

    var code = document.getElementById('regCode').value.trim();
    var pass = document.querySelector('[name="password"]').value;
    var confirmPass = document.querySelector('[name="confirmPassword"]').value;

    if (!code) { if (typeof DuaStore !== 'undefined' && DuaStore.toast) { DuaStore.toast.warning('Vui lòng nhập mã xác thực'); } return; }
    if (pass !== confirmPass) { if (typeof DuaStore !== 'undefined' && DuaStore.toast) { DuaStore.toast.warning('Mật khẩu xác nhận không khớp'); } return; }

    var btn = document.getElementById('registerBtn');
    btn.disabled = true;
    btn.textContent = 'Đang xử lý...';

    var form = document.getElementById('registerForm');
    fetch('/dang-ky', {
        method: 'POST',
        body: new URLSearchParams(new FormData(form))
    }).then(function (r) {
        if (r.redirected) { window.location.href = r.url; return null; }
        return r.text();
    }).then(function (html) {
        if (html === null) return;
        if (html.includes('is-invalid') || html.includes('alert-danger')) {
            var tmp = document.createElement('div');
            tmp.innerHTML = html;
            var msgs = tmp.querySelectorAll('.invalid-feedback, .alert-danger');
            var errMsgs = [];
            msgs.forEach(function (el) { var t = el.textContent.trim(); if (t) errMsgs.push(t); });
            if (typeof DuaStore !== 'undefined' && DuaStore.toast) { DuaStore.toast.error(errMsgs.length > 0 ? errMsgs.join('; ') : 'Đăng ký thất bại'); }
            btn.disabled = false;
            btn.textContent = 'Đăng ký';
        } else {
            if (typeof DuaStore !== 'undefined' && DuaStore.toast) { DuaStore.toast.success('Đăng ký thành công! Vui lòng đăng nhập.'); }
            document.getElementById('registerForm').reset();
            setTimeout(function () { window.location.href = '/dang-nhap'; }, 2000);
        }
    }).catch(function () {
        if (typeof DuaStore !== 'undefined' && DuaStore.toast) { DuaStore.toast.error('Lỗi kết nối hệ thống'); }
        btn.disabled = false;
        btn.textContent = 'Đăng ký';
    });
});

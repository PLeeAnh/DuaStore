/* =====================================================
 DuaStore — Module: Auth (Login / Register)
 Dependency: api.js, toast.js, utils.js
 ===================================================== */
'use strict';

/* ═══ LOGIN POPUP ═══ */
function showLoginPopup() {
    try {
        var el = document.getElementById('loginModal');
        if (!el) return;
        var modal = bootstrap.Modal.getOrCreateInstance(el);
        modal.show();
    } catch(e) {
        console.error('showLoginPopup error:', e);
        try {
            var fallback = document.getElementById('loginModal');
            if (fallback) {
                fallback.style.display = 'block';
                fallback.classList.add('show');
            }
        } catch(e2) {}
    }
}

/* ═══ REGISTER ═══ */
document.addEventListener('DOMContentLoaded', function () {
    var regModal = document.getElementById('registerModal');
    if (regModal) {
        regModal.addEventListener('show.bs.modal', function () {
            var errDiv = document.getElementById('registerError');
            var okDiv = document.getElementById('registerSuccess');
            var statusEl = document.getElementById('codeStatus');
            var form = document.getElementById('registerForm');
            if (errDiv) errDiv.classList.remove('show');
            if (okDiv) okDiv.classList.remove('show');
            if (statusEl) { statusEl.textContent = ''; statusEl.style.display = 'none'; }
            if (form) form.reset();
            var btn = document.getElementById('regSubmitBtn');
            if (btn) { btn.disabled = false; btn.textContent = 'Đăng ký'; }
        });
    }
});

async function registerSubmit(event) {
    event.preventDefault();
    var form = DuaStore.utils.qs('#registerForm');
    var errDiv = DuaStore.utils.qs('#registerError');
    var okDiv = DuaStore.utils.qs('#registerSuccess');
    errDiv.classList.remove('show');
    okDiv.classList.remove('show');
    var errText = DuaStore.utils.qs('.ds-auth-err-text', errDiv) || DuaStore.utils.qs('span', errDiv);
    var btn = DuaStore.utils.qs('#regSubmitBtn');

    if (DuaStore.utils.qs('[name="password"]', form).value !== DuaStore.utils.qs('[name="confirmPassword"]', form).value) {
        errText.textContent = 'Mật khẩu xác nhận không khớp';
        errDiv.classList.add('show');
        return;
    }

    var hoTen = DuaStore.utils.qs('[name="hoTen"]', form).value;
    if (!hoTen || !hoTen.trim()) {
        errText.textContent = 'Họ tên không được để trống';
        errDiv.classList.add('show');
        return;
    }
    var username = DuaStore.utils.qs('[name="username"]', form).value;
    if (!username || !username.trim()) {
        errText.textContent = 'Tên đăng nhập không được để trống';
        errDiv.classList.add('show');
        return;
    }
    if (username.trim().length < 3) {
        errText.textContent = 'Tên đăng nhập phải có ít nhất 3 ký tự';
        errDiv.classList.add('show');
        return;
    }

    var email = DuaStore.utils.qs('[name="email"]', form).value;
    var code = DuaStore.utils.qs('[name="verificationCode"]', form).value;
    if (!code) {
        errText.textContent = 'Vui lòng nhập mã xác thực';
        errDiv.classList.add('show');
        return;
    }

    btn.disabled = true;
    btn.textContent = 'Đang xử lý...';

    /* ── Verify code via API Layer ── */
    var result = await DuaStore.api.post('/api/auth/verify-code', {email: email, code: code});

    if (!result.ok) {
        errText.textContent = result.message;
        errDiv.classList.add('show');
        btn.disabled = false;
        btn.textContent = 'Đăng ký';
        return;
    }

    if (!result.data.success) {
        errText.textContent = 'Mã xác thực không đúng';
        errDiv.classList.add('show');
        btn.disabled = false;
        btn.textContent = 'Đăng ký';
        return;
    }

    /* ── Submit registration (raw fetch — form-urlencoded, returns HTML) ── */
    // Intentionally kept as raw fetch. Endpoint returns HTML, not JSON API.
    fetch('/dang-ky', {
        method: 'POST',
        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
        body: new URLSearchParams(new FormData(form))
    }).then(function (r) {
        return r.text();
    }).then(function (html) {
        if (html.includes('is-invalid') || html.includes('alert-danger')) {
            var tmp = document.createElement('div');
            tmp.innerHTML = html;
            var msgs = tmp.querySelectorAll('.invalid-feedback, .alert-danger');
            var errMsgs = [];
            msgs.forEach(function (el) {
                var t = el.textContent.trim();
                if (t) errMsgs.push(t);
            });
            errText.innerHTML = errMsgs.length > 0 ? errMsgs.join('<br>') : 'Đăng ký thất bại';
            errDiv.classList.add('show');
            btn.disabled = false;
            btn.textContent = 'Đăng ký';
        } else {
            okDiv.classList.add('show');
            form.reset();
            setTimeout(function () {
                var m = bootstrap.Modal.getInstance(DuaStore.utils.qs('#registerModal'));
                if (m)
                    m.hide();
                showLoginPopup();
            }, 1500);
        }
    }).catch(function () {
        errText.textContent = 'Lỗi kết nối hệ thống';
        errDiv.classList.add('show');
        btn.disabled = false;
        btn.textContent = 'Đăng ký';
    });
}

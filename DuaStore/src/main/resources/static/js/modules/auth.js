/* =====================================================
   DuaStore — Module: Auth (Login / Register)
   Dependency: api.js, toast.js, utils.js
===================================================== */
'use strict';

/* ═══ LOGIN POPUP ═══ */
function showLoginPopup() {
    var modal = new bootstrap.Modal(document.getElementById('loginModal'));
    modal.show();
}

/* ═══ REGISTER ═══ */
async function registerSubmit(event) {
    event.preventDefault();
    var form = DuaStore.utils.qs('#registerForm');
    var errDiv = DuaStore.utils.qs('#registerError');
    var okDiv = DuaStore.utils.qs('#registerSuccess');
    errDiv.classList.remove('show');
    okDiv.classList.remove('show');
    var errText = DuaStore.utils.qs('span', errDiv);
    var btn = DuaStore.utils.qs('#regSubmitBtn');

    if (DuaStore.utils.qs('[name="password"]', form).value !== DuaStore.utils.qs('[name="confirmPassword"]', form).value) {
        errText.textContent = 'Mật khẩu xác nhận không khớp';
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
    var result = await DuaStore.api.post('/api/auth/verify-code', { email: email, code: code });

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
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: new URLSearchParams(new FormData(form))
    }).then(function(r) { return r.text(); }).then(function(html) {
        if (html.includes('is-invalid') || html.includes('alert-danger')) {
            var tmp = document.createElement('div');
            tmp.innerHTML = html;
            var msg = tmp.querySelector('.invalid-feedback') || tmp.querySelector('.alert-danger');
            errText.textContent = msg ? msg.textContent.trim() : 'Đăng ký thất bại';
            errDiv.classList.add('show');
            btn.disabled = false;
            btn.textContent = 'Đăng ký';
        } else {
            okDiv.classList.add('show');
            form.reset();
            setTimeout(function() {
                var m = bootstrap.Modal.getInstance(DuaStore.utils.qs('#registerModal'));
                if (m) m.hide();
                showLoginPopup();
            }, 1500);
        }
    }).catch(function() {
        errText.textContent = 'Lỗi kết nối hệ thống';
        errDiv.classList.add('show');
        btn.disabled = false;
        btn.textContent = 'Đăng ký';
    });
}

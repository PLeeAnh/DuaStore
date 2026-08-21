window.appliedDiscount = parseInt(document.getElementById('discountDisplay').textContent.replace(/[^0-9]/g, '')) || 0;

function copyCode(btn) {
    var code = btn.getAttribute('data-code');
    if (!code) return;
    if (navigator.clipboard) {
        navigator.clipboard.writeText(code).then(function () {
            if (typeof DuaStore !== 'undefined' && DuaStore.toast) { DuaStore.toast.success('Đã copy mã: ' + code); }
        });
    } else {
        var ta = document.createElement('textarea');
        ta.value = code;
        document.body.appendChild(ta);
        ta.select();
        document.execCommand('copy');
        document.body.removeChild(ta);
        if (typeof DuaStore !== 'undefined' && DuaStore.toast) { DuaStore.toast.success('Đã copy mã: ' + code); }
    }
}

function openPromoModal() {
    var modalEl = document.getElementById('promoModal');
    bootstrap.Modal.getOrCreateInstance(modalEl).show();
}
function closePromoModal() {
    var modalEl = document.getElementById('promoModal');
    bootstrap.Modal.getOrCreateInstance(modalEl).hide();
}
function selectPromo(el) {
    var code = el.getAttribute('data-code');
    document.querySelectorAll('#promoModal .promo-item').forEach(function (p) {
        p.classList.remove('promo-selected');
        p.style.borderColor = '';
    });
    el.classList.add('promo-selected');
    el.style.borderColor = 'var(--ds-primary)';
    applyPromoCode(code);
}
function copyCheckoutPromo() {
    var labelEl = document.getElementById('checkoutPromoLabel');
    if (!labelEl) return;
    var code = labelEl.textContent.trim();
    if (!code || code === 'Chọn hoặc nhập mã giảm giá') return;
    navigator.clipboard.writeText(code).then(function () {
        if (typeof DuaStore !== 'undefined' && DuaStore.toast) { DuaStore.toast.success('Đã sao chép mã: ' + code); }
    }).catch(function () {
        if (typeof DuaStore !== 'undefined' && DuaStore.toast) { DuaStore.toast.error('Sao chép thất bại'); }
    });
}
function applyVoucherInput() {
    var input = document.getElementById('promoVoucherInput');
    var code = input.value.trim();
    if (!code) return;
    applyPromoCode(code);
}
function applyPromoCode(code) {
    var labelEl = document.getElementById('checkoutPromoLabel');
    var msgEl = document.getElementById('checkoutPromoMsg');
    var copyBtn = document.getElementById('checkoutCopyPromo');
    var subtotal = parseInt(document.getElementById('rawSubtotal').textContent) || 0;
    var csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
    var csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;
    document.getElementById('checkoutPromoInput').value = code;
    if (labelEl) { labelEl.textContent = code; labelEl.className = 'ds-promo-picker-applied'; }
    if (msgEl) msgEl.innerHTML = '<span class="text-muted">Đang kiểm tra...</span>';
    closePromoModal();
    var headers = {'Content-Type': 'application/json'};
    if (csrfHeader && csrfToken) headers[csrfHeader] = csrfToken;
    fetch('/api/coupon/validate', {
        method: 'POST',
        headers: headers,
        body: JSON.stringify({maCode: code, subtotal})
    }).then(function (r) {
        if (r.status === 403) {
            if (typeof showLoginPopup === 'function') showLoginPopup();
            return null;
        }
        return r.json();
    }).then(function (data) {
        if (!data) return;
        if (data.valid) {
            if (msgEl) msgEl.innerHTML = '<span class="text-primary">&#10003; ' + data.message + '</span>';
            window.appliedDiscount = parseInt(data.discount) || 0;
            document.getElementById('discountDisplay').textContent = '-' + window.appliedDiscount.toLocaleString('vi-VN') + 'đ';
            document.getElementById('discountDisplay').className = 'text-danger';
            if (copyBtn) copyBtn.style.display = '';
        } else {
            if (msgEl) msgEl.innerHTML = '<span class="text-danger">&#10007; ' + data.message + '</span>';
            window.appliedDiscount = 0;
            document.getElementById('discountDisplay').textContent = '0đ';
            document.getElementById('discountDisplay').className = '';
            if (copyBtn) copyBtn.style.display = 'none';
            if (labelEl) { labelEl.textContent = 'Chọn hoặc nhập mã giảm giá'; labelEl.className = ''; }
        }
        updateTotal();
    }).catch(function () {
        if (msgEl) msgEl.innerHTML = '<span class="text-danger">Lỗi kết nối, vui lòng thử lại</span>';
        if (labelEl) { labelEl.textContent = 'Chọn hoặc nhập mã giảm giá'; labelEl.className = ''; }
    });
}

function updatePointsDiscount() {
    var input = document.getElementById('pointsToRedeem');
    var points = parseInt(input.value) || 0;
    var maxPoints = parseInt(input.getAttribute('max')) || 0;
    if (points > maxPoints) { points = maxPoints; input.value = points; }
    if (points < 0) { points = 0; input.value = 0; }
    var rate = parseInt(document.getElementById('rawRedeemRate').textContent) || 100;
    var discount = points * rate;
    window.pointsDiscount = discount;
    document.getElementById('pointsDiscountDisplay').textContent = 'Giảm ' + discount.toLocaleString('vi-VN') + '₫';
    updateTotal();
}

function useMaxPoints() {
    var input = document.getElementById('pointsToRedeem');
    input.value = input.getAttribute('max');
    updatePointsDiscount();
}

function updateTotal() {
    var subtotal = parseInt(document.getElementById('rawSubtotal').textContent.replace(/[^0-9]/g, '')) || 0;
    var fee = parseInt(document.getElementById('shipFeeDisplay').textContent.replace(/[^0-9]/g, '')) || 0;
    var total = subtotal + fee - (window.appliedDiscount || 0) - (window.pointsDiscount || 0);
    document.getElementById('totalDisplay').textContent = (total < 0 ? 0 : total).toLocaleString('vi-VN') + '₫';
}

function initPromoTimers() {
    document.querySelectorAll('.ds-promo-timer').forEach(function (timer) {
        var endStr = timer.getAttribute('data-end');
        if (!endStr) return;
        var endDate = new Date(endStr);
        if (endDate - new Date() <= 0) {
            timer.closest('.promo-item').style.opacity = '.45';
            timer.querySelector('.promo-countdown').textContent = 'Đã kết thúc';
        }
    });
}

document.addEventListener('DOMContentLoaded', function () {
    initPromoTimers();
    window.appliedDiscount = parseInt(document.getElementById('rawDiscount').textContent) || 0;
    window.pointsDiscount = 0;
    if (window.appliedDiscount > 0) {
        var copyBtn = document.getElementById('checkoutCopyPromo');
        if (copyBtn) copyBtn.style.display = '';
    }

    /* ── Idempotency: khoa submit 1 lan, kem idempotencyKey cho checkout ── */
    var keyInput = document.getElementById('checkoutIdempotencyKey');
    if (keyInput) {
        keyInput.value = 'ck-' + (crypto.randomUUID ? crypto.randomUUID() : Date.now() + '-' + Math.random().toString(36).slice(2));
    }
    var submitBtn = document.getElementById('checkoutSubmitBtn');
    var form = document.getElementById('checkoutForm');
    if (submitBtn && form) {
        form.addEventListener('submit', function () {
            submitBtn.disabled = true;
            submitBtn.textContent = 'Đang xử lý...';
        });
    }

    document.querySelectorAll('.btn-address-edit').forEach(function (btn) {
        btn.addEventListener('click', function () {
            editAddress(parseInt(this.getAttribute('data-id')));
        });
    });
    document.querySelectorAll('.btn-address-default').forEach(function (btn) {
        btn.addEventListener('click', function () {
            setDefaultAddress(parseInt(this.getAttribute('data-id')));
        });
    });
    document.querySelectorAll('.btn-address-delete').forEach(function (btn) {
        btn.addEventListener('click', function () {
            deleteAddress(parseInt(this.getAttribute('data-id')));
        });
    });
});

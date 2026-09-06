window.appliedDiscount = parseInt(document.getElementById('discountDisplay').textContent.replace(/[^0-9]/g, '')) || 0;

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

/* ── Voucher picker modal (kieu Shopee) ── */
function formatVoucherDiscount(v) {
    if (v.loaiGiam === 'PHAN_TRAM') {
        var pct = (v.giaTriGiam || 0);
        var txt = 'Giảm ' + pct + '%';
        if (v.giamToiDa) txt += ' (tối đa ' + Math.round(v.giamToiDa).toLocaleString('vi-VN') + '₫)';
        return txt;
    }
    return 'Giảm ' + Math.round(v.giaTriGiam || 0).toLocaleString('vi-VN') + '₫';
}

function renderVoucherCard(v) {
    var minOrder = v.donHangToiThieu && v.donHangToiThieu > 0
        ? 'Đơn từ ' + Math.round(v.donHangToiThieu).toLocaleString('vi-VN') + '₫'
        : 'Không giới hạn đơn tối thiểu';
    var expiry = '';
    if (v.expiredAt) {
        var d = new Date(v.expiredAt);
        expiry = ' · HSD ' + d.toLocaleDateString('vi-VN');
    }
    var stubAmt = v.loaiGiam === 'PHAN_TRAM'
        ? v.giaTriGiam + '%'
        : Math.round(v.giaTriGiam || 0).toLocaleString('vi-VN');
    var stubUnit = v.loaiGiam === 'PHAN_TRAM' ? 'giảm giá' : 'đồng';
    return '<div class="svp-ticket">' +
        '<div class="svp-ticket-stub"><span class="amt">' + stubAmt + '</span><span class="unit">' + stubUnit + '</span></div>' +
        '<div class="svp-ticket-divider"></div>' +
        '<div class="svp-ticket-body">' +
        '<div class="svp-ticket-title">' + (v.tenChuongTrinh || v.maCode) + '</div>' +
        '<span class="svp-ticket-code">' + v.maCode + '</span>' +
        '<div class="svp-ticket-meta">' + minOrder + expiry + '</div>' +
        '</div>' +
        '<div class="svp-ticket-actions">' +
        '<button type="button" class="svp-ticket-apply" onclick="applyVoucherFromModal(\'' + v.maCode + '\')">Áp dụng</button>' +
        '</div>' +
        '</div>';
}

function loadVoucherPickerList() {
    var listEl = document.getElementById('voucherPickerList');
    if (!listEl) return;
    listEl.innerHTML = '<div class="text-center text-muted py-4" id="voucherPickerLoading"><i class="bi bi-arrow-repeat me-1"></i>Đang tải voucher...</div>';
    fetch('/api/vi-voucher/available')
        .then(function (r) { return r.json(); })
        .then(function (vouchers) {
            if (!vouchers || !vouchers.length) {
                listEl.innerHTML = '<div class="svp-ticket-empty">' +
                    '<i class="bi bi-ticket-perforated"></i>' +
                    'Bạn chưa lưu voucher nào<br>' +
                    '<a href="/khuyen-mai" class="small fw-semibold" style="color:#2563eb;">Xem khuyến mãi đang có</a>' +
                    '</div>';
                return;
            }
            listEl.innerHTML = '<div class="shp-voucher-list-label">Voucher của bạn (' + vouchers.length + ')</div>' +
                vouchers.map(renderVoucherCard).join('');
        })
        .catch(function () {
            listEl.innerHTML = '<div class="svp-ticket-empty">Không thể tải danh sách voucher</div>';
        });
}

function applyVoucherFromModal(maCode) {
    maCode = (maCode || '').trim();
    var errorEl = document.getElementById('voucherPickerError');
    if (!maCode) {
        errorEl.textContent = 'Vui lòng nhập hoặc chọn một voucher';
        errorEl.classList.remove('d-none');
        return;
    }
    var subtotal = parseInt(document.getElementById('rawSubtotal').textContent.replace(/[^0-9]/g, '')) || 0;
    errorEl.classList.add('d-none');
    var csrfToken = document.querySelector('meta[name="_csrf"]') ? document.querySelector('meta[name="_csrf"]').getAttribute('content') : null;
    var csrfHeader = document.querySelector('meta[name="_csrf_header"]') ? document.querySelector('meta[name="_csrf_header"]').getAttribute('content') : null;
    var headers = { 'Content-Type': 'application/json' };
    if (csrfToken && csrfHeader) headers[csrfHeader] = csrfToken;
    fetch('/api/coupon/validate', {
        method: 'POST',
        headers: headers,
        body: JSON.stringify({ maCode: maCode, subtotal: subtotal })
    })
        .then(function (r) { return r.json(); })
        .then(function (data) {
            if (!data.valid) {
                errorEl.textContent = data.message || 'Mã voucher không hợp lệ';
                errorEl.classList.remove('d-none');
                return;
            }
            document.getElementById('checkoutPromoInput').value = maCode;
            window.appliedDiscount = Math.round(data.discount || 0);
            document.getElementById('discountDisplay').textContent = '-' + window.appliedDiscount.toLocaleString('vi-VN') + '₫';
            document.getElementById('discountDisplay').classList.add('text-primary');
            updateTotal();
            var modalEl = document.getElementById('voucherPickerModal');
            var modal = bootstrap.Modal.getInstance(modalEl);
            if (modal) modal.hide();
        })
        .catch(function () {
            errorEl.textContent = 'Lỗi kết nối, vui lòng thử lại';
            errorEl.classList.remove('d-none');
        });
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

    /* ── Idempotency: khoa submit 1 lan, kem idempotencyKey cho checkout ── */
    var keyInput = document.getElementById('checkoutIdempotencyKey');
    if (keyInput) {
        keyInput.value = 'ck-' + (crypto.randomUUID ? crypto.randomUUID() : Date.now() + '-' + Math.random().toString(36).slice(2));
    }
    var submitBtn = document.getElementById('checkoutSubmitBtn');
    var form = document.getElementById('checkoutForm');
    if (submitBtn && form) {
        /* Submit handler moved to checkout.js — keep idempotency key init only */
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

    var voucherModalEl = document.getElementById('voucherPickerModal');
    if (voucherModalEl) {
        voucherModalEl.addEventListener('shown.bs.modal', loadVoucherPickerList);
    }
});

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
});

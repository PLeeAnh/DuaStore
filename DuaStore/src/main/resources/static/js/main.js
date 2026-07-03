/* =====================================================
   DuaStore — main.js (Client) — Entry point
   Modules: toast.js, api.js, utils.js, modules/*
===================================================== */
'use strict';

/* ── CSRF: auto-inject token into same-origin non-GET fetch ── */
(function() {
    var token = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
    var header = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
    if (!token || !header) return;
    var orig = window.fetch;
    window.fetch = function(url, opts) {
        opts = opts || {};
        if (!opts.method || opts.method.toUpperCase() === 'GET') return orig.call(this, url, opts);
        var isSameOrigin = typeof url === 'string' && (url.startsWith('/') || new URL(url, location.origin).origin === location.origin);
        if (isSameOrigin) {
            opts.headers = opts.headers || {};
            if (opts.headers instanceof Headers) {
                opts.headers.set(header, token);
            } else {
                opts.headers[header] = token;
            }
        }
        return orig.call(this, url, opts);
    };
})();

document.addEventListener('DOMContentLoaded', function() {

    /* ═══ MOBILE NAV TOGGLE ═══ */
    var toggle = document.getElementById('dsNavToggle');
    var panel = document.getElementById('dsNavPanel');
    var overlay = document.getElementById('dsNavOverlay');

    function openNav() {
        panel.classList.add('open');
        overlay.classList.add('open');
        toggle.classList.add('is-open');
        toggle.setAttribute('aria-label', 'Đóng menu');
        document.body.style.overflow = 'hidden';
    }
    function closeNav() {
        panel.classList.remove('open');
        overlay.classList.remove('open');
        toggle.classList.remove('is-open');
        toggle.setAttribute('aria-label', 'Mở menu');
        document.body.style.overflow = '';
        document.querySelectorAll('.ds-sub-menu.open').forEach(function(el) { el.classList.remove('open'); });
        document.querySelectorAll('.ds-chevron.rotated').forEach(function(el) { el.classList.remove('rotated'); });
    }
    if (toggle && panel) {
        toggle.addEventListener('click', function() { panel.classList.contains('open') ? closeNav() : openNav(); });
    }
    if (overlay) overlay.addEventListener('click', closeNav);

    document.querySelectorAll('.ds-sub-toggle').forEach(function(btn) {
        var menu = btn.nextElementSibling;
        if (menu) {
            btn.addEventListener('click', function(e) {
                e.preventDefault();
                menu.classList.toggle('open');
                var ch = btn.querySelector('.ds-chevron');
                if (ch) ch.classList.toggle('rotated');
            });
        }
    });
    document.querySelectorAll('.ds-nav-panel .ds-nav-link:not(.ds-nav-no-close), .ds-nav-panel .ds-sub-link').forEach(function(link) {
        link.addEventListener('click', function() {
            if (!link.classList.contains('ds-sub-toggle')) setTimeout(closeNav, 200);
        });
    });

    /* ═══ SWIPERS ═══ */
    if (typeof Swiper !== 'undefined') {
        if (document.querySelector('.hero-banner-swiper')) {
            new Swiper('.hero-banner-swiper', {
                loop: true,
                autoplay: { delay: 4000, disableOnInteraction: false },
                pagination: { el: '.hero-banner-swiper .swiper-pagination', clickable: true },
                navigation: { nextEl: '.hero-banner-swiper .swiper-button-next', prevEl: '.hero-banner-swiper .swiper-button-prev' }
            });
        }
        if (document.querySelector('.hero-swiper')) {
            new Swiper('.hero-swiper', {
                loop: true, effect: 'fade',
                autoplay: { delay: 5000, disableOnInteraction: false },
                pagination: { el: '.hero-pagination', clickable: true },
            });
        }
        if (document.querySelector('.testi-swiper')) {
            new Swiper('.testi-swiper', {
                loop: true, autoplay: { delay: 4000, disableOnInteraction: false },
                pagination: { el: '.testi-pagination', clickable: true },
                breakpoints: { 0: { slidesPerView: 1 }, 768: { slidesPerView: 2 }, 1024: { slidesPerView: 3 } }
            });
        }
        if (document.querySelector('.product-swiper')) {
            new Swiper('.product-swiper', {
                loop: true, autoplay: { delay: 5000, disableOnInteraction: false },
                navigation: { nextEl: '.product-next', prevEl: '.product-prev' },
                breakpoints: { 0: { slidesPerView: 1, spaceBetween: 16 }, 576: { slidesPerView: 2, spaceBetween: 16 }, 992: { slidesPerView: 4, spaceBetween: 20 } }
            });
        }
    }

    /* ═══ SCROLLREVEAL ═══ */
    if (typeof ScrollReveal !== 'undefined') {
        var sr = ScrollReveal({ origin: 'bottom', distance: '40px', duration: 800, delay: 200, easing: 'ease-out' });
        sr.reveal('.sr-card', { interval: 200 });
        sr.reveal('.sr-up', {});
        sr.reveal('.sr-left', { origin: 'left', distance: '60px' });
        sr.reveal('.sr-right', { origin: 'right', distance: '60px' });
    }

    /* ═══ PROFILE MENU ═══ */
    var prTrigger = document.getElementById('dsProfileTrigger');
    var prDropdown = document.getElementById('dsProfileDropdown');
    if (prTrigger && prDropdown) {
        prTrigger.addEventListener('click', function(e) {
            e.stopPropagation();
            var isOpen = prDropdown.classList.toggle('open');
            prTrigger.setAttribute('aria-expanded', isOpen);
        });
        document.addEventListener('click', function(e) {
            if (!prTrigger.contains(e.target) && !prDropdown.contains(e.target)) {
                prDropdown.classList.remove('open');
                prTrigger.setAttribute('aria-expanded', 'false');
            }
        });
    }

    /* ═══ FAQ ACCORDION ═══ */
    document.querySelectorAll('.ds-faq-btn').forEach(function(btn) {
        btn.addEventListener('click', function() {
            var body = btn.nextElementSibling;
            var isOpen = btn.getAttribute('aria-expanded') === 'true';
            document.querySelectorAll('.ds-faq-btn').forEach(function(b) {
                b.setAttribute('aria-expanded', 'false');
                b.nextElementSibling.style.maxHeight = '0';
            });
            if (!isOpen) {
                btn.setAttribute('aria-expanded', 'true');
                body.style.maxHeight = body.scrollHeight + 'px';
            }
        });
    });


    /* ═══ PRODUCT CARD ENHANCEMENTS ═══ */
    function getCard(el) { return el.closest('.ds-product-card'); }

    document.addEventListener('click', function(e) {
        const chip = e.target.closest('.ds-variant-chip');
        if (chip && !chip.classList.contains('oos')) {
            e.preventDefault();
            const card = getCard(chip);
            if (!card) return;
            card.querySelectorAll('.ds-variant-chip').forEach(c => c.classList.remove('active'));
            chip.classList.add('active');

            const newPrice = chip.getAttribute('data-price');
            const newStock = parseInt(chip.getAttribute('data-stock')) || 0;
            const stockEl = card.querySelector('.ds-stock-info');
            if (stockEl) {
                if (newStock > 0) {
                    stockEl.textContent = newStock <= 3 ? '⚠ Chỉ còn ' + newStock + ' sản phẩm' : 'Còn lại: ' + newStock + ' sản phẩm';
                    stockEl.className = 'ds-stock-info' + (newStock <= 3 ? ' warning' : '');
                } else {
                    stockEl.textContent = 'Hết hàng';
                    stockEl.className = 'ds-stock-info oos';
                }
                stockEl.style.display = '';
            }
            card.classList.toggle('oos', newStock <= 0);

            const priceBtn = card.querySelector('.ds-card-add-cart');
            if (priceBtn) {
                priceBtn.disabled = (newStock <= 0);
                const amountEl = priceBtn.querySelector('.ds-price-btn-amount');
                if (amountEl) amountEl.textContent = parseInt(newPrice).toLocaleString('vi-VN') + 'đ';
            }
            const qtyVal = card.querySelector('.ds-qty-val');
            if (qtyVal) qtyVal.value = '1';
            const minus = card.querySelector('.ds-qty-minus');
            if (minus) minus.disabled = true;
        }
    });

    document.addEventListener('click', function(e) {
        const btn = e.target.closest('.ds-qty-btn');
        if (!btn) return;
        e.stopPropagation();
        e.preventDefault();
        const card = getCard(btn);
        if (!card) return;
        const qtyEl = card.querySelector('.ds-qty-val');
        let qty = parseInt(qtyEl.value) || 1;
        const activeChip = getActiveVariant(card);
        const maxStock = activeChip ? parseInt(activeChip.getAttribute('data-stock')) || 99 : 99;
        const minus = card.querySelector('.ds-qty-minus');
        const plus = card.querySelector('.ds-qty-plus');

        if (btn.classList.contains('ds-qty-plus')) {
            if (qty < maxStock) qty++;
            else { plus.style.color = '#ef4444'; setTimeout(() => plus.style.color = '', 600); return; }
        } else if (btn.classList.contains('ds-qty-minus') && qty > 1) qty--;

        qtyEl.value = qty;
        minus.disabled = (qty <= 1);
        plus.disabled = (qty >= maxStock);
    });

    /* ═══ FLASH SALE COUNTDOWN ═══ */
    document.querySelectorAll('.ds-flash-timer').forEach(timer => {
        const endStr = timer.getAttribute('data-end');
        if (!endStr) return;
        const endDate = new Date(endStr);
        function tick() {
            const diff = endDate - new Date();
            const span = timer.querySelector('.flash-countdown');
            if (!span) return;
            if (diff <= 0) { span.textContent = 'Đã kết thúc'; timer.style.opacity = '.5'; return; }
            const days = Math.floor(diff / 86400000);
            if (days > 0) {
                const d = endDate.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' });
                span.textContent = 'HSD: ' + d;
                return;
            }
            const h = Math.floor(diff / 3600000);
            const m = Math.floor((diff % 3600000) / 60000);
            const s = Math.floor((diff % 60000) / 1000);
            span.textContent = String(h).padStart(2, '0') + 'h ' + String(m).padStart(2, '0') + 'm ' + String(s).padStart(2, '0') + 's';
        }
        tick();
        setInterval(tick, 1000);
    });

    /* ═══ PROMO COUNTDOWN ═══ */
    document.querySelectorAll('.ds-promo-timer').forEach(timer => {
        const endStr = timer.getAttribute('data-end');
        if (!endStr) return;
        const endDate = new Date(endStr);
        function tick() {
            const diff = endDate - new Date();
            const span = timer.querySelector('.promo-countdown');
            if (!span) return;
            if (diff <= 0) { span.textContent = 'Đã kết thúc'; timer.style.opacity = '.5'; return; }
            const days = Math.floor(diff / 86400000);
            if (days > 0) {
                const d = endDate.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit', year: 'numeric' });
                span.textContent = 'HSD: ' + d;
                return;
            }
            const h = Math.floor(diff / 3600000);
            const m = Math.floor((diff % 3600000) / 60000);
            const s = Math.floor((diff % 60000) / 1000);
            span.textContent = String(h).padStart(2, '0') + 'h ' + String(m).padStart(2, '0') + 'm ' + String(s).padStart(2, '0') + 's';
        }
        tick();
        setInterval(tick, 1000);
    });
});

/* ═══ COPY PROMO CODE ═══ */
function copyPromoCode(btn) {
    const code = btn.getAttribute('data-code');
    if (!code) return;
    navigator.clipboard.writeText(code).then(() => {
        alert('Đã sao chép mã: ' + code);
    }).catch(() => {
        alert('Sao chép thất bại, vui lòng tự copy mã: ' + code);
    });
}

/* ═══ BACK TO TOP ═══ */
var backTopBtn = document.getElementById('backTopBtn');
if (backTopBtn) {
    window.addEventListener('scroll', function() {
        backTopBtn.style.display = window.scrollY > 400 ? 'flex' : 'none';
    }, { passive: true });
}

/* ── Render star rating readonly ── */
document.querySelectorAll('.star-rating-readonly').forEach(function(el) {
    var score = parseInt(el.dataset.score) || 0;
    el.innerHTML = '★'.repeat(score) + '☆'.repeat(5 - score);
});

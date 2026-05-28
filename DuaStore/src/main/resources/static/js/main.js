/* =====================================================
   DuaStore — main.js (Client)
===================================================== */

'use strict';

/* ══════════════════════════════════════════
   EDIT TOGGLE TẠI ĐÂY — Mobile nav toggle (☰ / ✕)
   Sửa openNav/closeNav, icon swap, sub-menu tại đây
══════════════════════════════════════════ */
document.addEventListener('DOMContentLoaded', () => {

    const toggle = document.getElementById('dsNavToggle');
    const panel  = document.getElementById('dsNavPanel');
    const overlay = document.getElementById('dsNavOverlay');
    const icon   = document.getElementById('dsToggleIcon');

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

        // Thu gọn tất cả sub-menu đang mở
        document.querySelectorAll('.ds-sub-menu.open').forEach(el => el.classList.remove('open'));
        document.querySelectorAll('.ds-chevron.rotated').forEach(el => el.classList.remove('rotated'));
    }

    if (toggle && panel && icon) {
        toggle.addEventListener('click', () => {
            if (panel.classList.contains('open')) {
                closeNav();
            } else {
                openNav();
            }
        });
    }
    if (overlay) {
        overlay.addEventListener('click', closeNav);
    }

    /* ── Sub-menu toggle trong panel (giống pattern toggle chính) ── */
    document.querySelectorAll('.ds-sub-toggle').forEach(btn => {
        const menu = btn.nextElementSibling;
        if (btn && menu) {
            btn.addEventListener('click', (e) => {
                e.preventDefault();
                if (menu.classList.contains('open')) {
                    menu.classList.remove('open');
                    btn.querySelector('.ds-chevron')?.classList.remove('rotated');
                } else {
                    menu.classList.add('open');
                    btn.querySelector('.ds-chevron')?.classList.add('rotated');
                }
            });
        }
    });

    /* ── Đóng panel khi click link ── */
    document.querySelectorAll('.ds-nav-panel .ds-nav-link, .ds-nav-panel .ds-sub-link').forEach(link => {
        link.addEventListener('click', () => {
            // Chỉ đóng nếu không phải sub-toggle
            if (!link.classList.contains('ds-sub-toggle')) {
                setTimeout(closeNav, 200);
            }
        });
    });

});
/* ══════════════════════════════════════════
   KẾT THÚC EDIT TOGGLE
══════════════════════════════════════════ */

/* ── SwiperJS Hero Carousel ──
   ★ Cấu hình: fade effect, loop 5s, pagination dots
   ★ Xem HTML trong index.html → cần đúng class .hero-swiper + .hero-pagination */
document.addEventListener('DOMContentLoaded', () => {
    const heroSwiperEl = document.querySelector('.hero-swiper');
    if (heroSwiperEl) {
        new Swiper('.hero-swiper', {
            loop: true,
            effect: 'fade',
            autoplay: { delay: 5000, disableOnInteraction: false },
            pagination: { el: '.hero-pagination', clickable: true },
        });
    }
});

/* ── ScrollReveal ──
   ★ Các class gắn vào HTML:
     .sr-card   → category grid (mỗi card cách 200ms)
     .sr-up     → section từ dưới lên (hero, testimonials, FAQ, product carousel)
     .sr-left   → từ trái sang (dự trữ)
     .sr-right  → từ phải sang (dự trữ)
   ★ Import từ CDN trong base.html dòng 457 */
document.addEventListener('DOMContentLoaded', () => {
    if (typeof ScrollReveal !== 'undefined') {
        const sr = ScrollReveal({
            origin: 'bottom', distance: '40px', duration: 800, delay: 200, easing: 'ease-out'
        });
        sr.reveal('.sr-card', { interval: 200 });
        sr.reveal('.sr-up', {});
        sr.reveal('.sr-left', { origin: 'left', distance: '60px' });
        sr.reveal('.sr-right', { origin: 'right', distance: '60px' });
    }
});

/* ── Back to top button ── */
const backTopBtn = document.getElementById('backTopBtn');
if (backTopBtn) {
    window.addEventListener('scroll', () => {
        backTopBtn.style.display = window.scrollY > 400 ? 'flex' : 'none';
    }, { passive: true });
}

/* ── Auto-dismiss flash alerts sau 5 giây ── */
document.addEventListener('DOMContentLoaded', () => {
    document.querySelectorAll('.alert.alert-dismissible').forEach(el => {
        setTimeout(() => {
            const instance = bootstrap.Alert.getOrCreateInstance(el);
            if (instance) instance.close();
        }, 5000);
    });
});

/* ── Cập nhật số lượng badge giỏ hàng ──
   Gọi hàm này sau AJAX thêm vào giỏ:
   updateCartBadge(newCount);
*/
function updateCartBadge(count) {
    const badge = document.getElementById('cartBadge');
    if (!badge) return;
    if (count <= 0) {
        badge.classList.add('d-none');
        badge.textContent = '0';
    } else {
        badge.classList.remove('d-none');
        badge.textContent = count > 99 ? '99+' : String(count);
    }
}

/* ── AJAX thêm vào giỏ hàng ──
   ★ Button có class .ds-add-cart + data-id (productId) + data-name
   ★ Gửi POST /api/cart/add → backend trả JSON { cartCount: N }
   ★ Thành công: gọi updateCartBadge(N) + hiện "✓ Đã thêm" 2s
   ★ Backend CẦN tạo endpoint POST /api/cart/add */
document.addEventListener('DOMContentLoaded', () => {
    document.querySelectorAll('.ds-add-cart').forEach(btn => {
        btn.addEventListener('click', function() {
            const id = this.dataset.id;
            const name = this.dataset.name;
            const origText = this.textContent;
            this.textContent = '⏳';
            this.disabled = true;

            fetch('/api/cart/add', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ productId: parseInt(id), quantity: 1 })
            })
            .then(r => r.json())
            .then(data => {
                if (data && typeof data.cartCount !== 'undefined') {
                    updateCartBadge(data.cartCount);
                }
                this.textContent = '✓ Đã thêm';
                setTimeout(() => { this.textContent = origText; this.disabled = false; }, 2000);
            })
            .catch(() => {
                this.textContent = '✗ Lỗi';
                setTimeout(() => { this.textContent = origText; this.disabled = false; }, 2000);
            });
        });
    });
});

/* ══════════════════════════════════════════
   THEME TOGGLE (client — inside profile dropdown)
══════════════════════════════════════════ */
(function() {
    function getTheme() { return localStorage.getItem('duastore-theme') || 'light'; }
    function setTheme(theme) {
        document.documentElement.setAttribute('data-theme', theme);
        localStorage.setItem('duastore-theme', theme);
        const isDark = theme === 'dark';
        const icon = document.getElementById('dsProfileThemeIcon');
        const label = document.getElementById('dsProfileThemeLabel');
        if (icon) icon.className = isDark ? 'bi bi-sun' : 'bi bi-moon-stars';
        if (label) label.textContent = isDark ? 'Chế độ sáng' : 'Chế độ tối';
    }
    setTheme(getTheme());
    const btn = document.getElementById('dsProfileThemeToggle');
    if (btn) btn.addEventListener('click', (e) => { e.preventDefault(); setTheme(getTheme() === 'dark' ? 'light' : 'dark'); });
})();

/* ══════════════════════════════════════════
   PROFILE MENU (client)
══════════════════════════════════════════ */
document.addEventListener('DOMContentLoaded', () => {
    const trigger = document.getElementById('dsProfileTrigger');
    const dropdown = document.getElementById('dsProfileDropdown');
    if (trigger && dropdown) {
        trigger.addEventListener('click', (e) => {
            e.stopPropagation();
            const isOpen = dropdown.classList.toggle('open');
            trigger.setAttribute('aria-expanded', isOpen);
        });
        document.addEventListener('click', (e) => {
            if (!trigger.contains(e.target) && !dropdown.contains(e.target)) {
                dropdown.classList.remove('open');
                trigger.setAttribute('aria-expanded', 'false');
            }
        });
    }
});

/* ══════════════════════════════════════════
   FAQ ACCORDION

   ★ Cơ chế: click câu hỏi → mở/đóng bằng max-height animation
     (không dùng Bootstrap collapse vì cần transition mượt)
   ★ Click câu hỏi khác → đóng cái đang mở trước, mở cái mới
   ★ Chevron xoay 180° nhờ CSS: [aria-expanded="true"] i { transform: rotate(180deg) }
══════════════════════════════════════════ */
document.addEventListener('DOMContentLoaded', () => {
    document.querySelectorAll('.ds-faq-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            const body = btn.nextElementSibling;
            const isOpen = btn.getAttribute('aria-expanded') === 'true';
            // close all
            document.querySelectorAll('.ds-faq-btn').forEach(b => {
                b.setAttribute('aria-expanded', 'false');
                b.nextElementSibling.style.maxHeight = '0';
            });
            // open clicked
            if (!isOpen) {
                btn.setAttribute('aria-expanded', 'true');
                body.style.maxHeight = body.scrollHeight + 'px';
            }
        });
    });
});

/* ══════════════════════════════════════════
   TESTIMONIALS SWIPER

   ★ Xem HTML trong index.html section .ds-testi
   ★ breakpoints: mobile 1 → tablet 2 → desktop 3 item / slide
══════════════════════════════════════════ */
document.addEventListener('DOMContentLoaded', () => {
    const el = document.querySelector('.testi-swiper');
    if (el && typeof Swiper !== 'undefined') {
        new Swiper('.testi-swiper', {
            loop: true,
            autoplay: { delay: 4000, disableOnInteraction: false },
            pagination: { el: '.testi-pagination', clickable: true },
            breakpoints: {
                0: { slidesPerView: 1 },
                768: { slidesPerView: 2 },
                1024: { slidesPerView: 3 }
            }
        });
    }
});

/* ══════════════════════════════════════════
   PRODUCT CAROUSEL SWIPER

   ★ Mỗi card sản phẩm chứa nút .ds-add-cart (AJAX)
   ★ breakpoints: mobile 1 → tablet 2 → desktop 4 item / slide
   ★ Xem HTML trong index.html section .ds-product-carousel
══════════════════════════════════════════ */
document.addEventListener('DOMContentLoaded', () => {
    const el = document.querySelector('.product-swiper');
    if (el && typeof Swiper !== 'undefined') {
        new Swiper('.product-swiper', {
            loop: true,
            autoplay: { delay: 5000, disableOnInteraction: false },
            navigation: { nextEl: '.product-next', prevEl: '.product-prev' },
            breakpoints: {
                0: { slidesPerView: 1, spaceBetween: 16 },
                576: { slidesPerView: 2, spaceBetween: 16 },
                992: { slidesPerView: 4, spaceBetween: 20 }
            }
        });
    }
});

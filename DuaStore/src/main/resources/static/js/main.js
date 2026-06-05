/* =====================================================
   DuaStore — main.js (Client)
===================================================== */

'use strict';

/* ══════════════════════════════════════════
   MOBILE NAV TOGGLE (☰ / ✕)
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

    /* ── Sub-menu toggle trong panel ── */
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
            if (!link.classList.contains('ds-sub-toggle')) {
                setTimeout(closeNav, 200);
            }
        });
    });
});


/* ── SwiperJS Hero Carousel ── */
document.addEventListener('DOMContentLoaded', () => {
    const heroSwiperEl = document.querySelector('.hero-swiper');
    if (heroSwiperEl && typeof Swiper !== 'undefined') {
        new Swiper('.hero-swiper', {
            loop: true,
            effect: 'fade',
            autoplay: { delay: 5000, disableOnInteraction: false },
            pagination: { el: '.hero-pagination', clickable: true },
        });
    }
});


/* ── ScrollReveal ── */
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


/* ── Cập nhật số lượng badge giỏ hàng ── */
function updateCartBadge(count) {
    const badge = document.getElementById('cartBadge');
    if (!badge) return;
    count = Number(count) || 0;
    if (count <= 0) {
        badge.classList.add('d-none');
        badge.textContent = '0';
    } else {
        badge.classList.remove('d-none');
        badge.textContent = count > 99 ? '99+' : String(count);
    }
}


/* ══════════════════════════════════════════
   THEME TOGGLE (Client)
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
   PROFILE MENU (Client)
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
══════════════════════════════════════════ */
document.addEventListener('DOMContentLoaded', () => {
    document.querySelectorAll('.ds-faq-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            const body = btn.nextElementSibling;
            const isOpen = btn.getAttribute('aria-expanded') === 'true';
            document.querySelectorAll('.ds-faq-btn').forEach(b => {
                b.setAttribute('aria-expanded', 'false');
                b.nextElementSibling.style.maxHeight = '0';
            });
            if (!isOpen) {
                btn.setAttribute('aria-expanded', 'true');
                body.style.maxHeight = body.scrollHeight + 'px';
            }
        });
    });
});


/* ══════════════════════════════════════════
   TESTIMONIALS SWIPER
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


/* ══════════════════════════════════════════
   POPUP YÊU THÍCH & GIỎ HÀNG
══════════════════════════════════════════ */
// ĐÓNG / MỞ POPUP
function togglePopup(popupId) {
    document.querySelectorAll('.custom-popup').forEach(popup => { 
        if(popup.id !== popupId) popup.style.display = 'none'; 
    });
    const popup = document.getElementById(popupId);
    if(popup) popup.style.display = (popup.style.display === 'block') ? 'none' : 'block';
}

// Click ra ngoài để ẩn popup
document.addEventListener('click', function(event) {
    const btnWishlist = document.getElementById('btn-wishlist-toggle'); 
    const popupWishlist = document.getElementById('wishlist-popup');
    const btnCart = document.getElementById('btn-cart-toggle'); 
    const popupCart = document.getElementById('cart-popup');
    
    if (btnWishlist && popupWishlist && !btnWishlist.contains(event.target) && !popupWishlist.contains(event.target)) {
        popupWishlist.style.display = 'none';
    }
    if (btnCart && popupCart && !btnCart.contains(event.target) && !popupCart.contains(event.target)) {
        popupCart.style.display = 'none';
    }
});


/* ══════════════════════════════════════════
   XỬ LÝ CLICK THẢ TIM BẰNG FETCH API 
   (Hỗ trợ trang chủ + trang chi tiết bốc đúng dữ liệu)
══════════════════════════════════════════ */
function toggleWishlist(btnElement, productId) {
    const icon = btnElement.querySelector('i');
    const container = document.getElementById('wishlist-items-container');
    
    let productName = 'Sản phẩm ' + productId;
    let productPrice = 'Đang cập nhật';
    let productImg = '';

    // Phân biệt Trang chủ và Trang chi tiết
    const card = btnElement.closest('.ds-product-card');
    if (card) {
        const nameEl = card.querySelector('.ds-product-name');
        const priceEl = card.querySelector('.ds-product-price');
        if (nameEl) productName = nameEl.innerText;
        if (priceEl) productPrice = priceEl.innerText;
    } else {
        const detailName = document.querySelector('.product-detail-info h3');
        const detailPrice = document.getElementById('productPrice');
        const detailImg = document.getElementById('mainImage');
        if (detailName) productName = detailName.innerText;
        if (detailPrice) productPrice = detailPrice.innerText;
        if (detailImg) productImg = detailImg.src;
    }

    fetch('/api/wishlist/toggle', { 
        method: 'POST', 
        headers: { 'Content-Type': 'application/json' }, 
        body: JSON.stringify({ productId: productId }) 
    })
    .then(response => response.json())
    .then(data => {
        if(data.success) {
            if (btnElement.classList.contains('active')) {
                // Hủy thích
                btnElement.classList.remove('active'); 
                icon.classList.replace('bi-heart-fill', 'bi-heart');
                const itemToRemove = document.getElementById('wishlist-item-' + productId);
                if(itemToRemove) itemToRemove.remove();
            } else {
                // Thêm thích
                btnElement.classList.add('active'); 
                icon.classList.replace('bi-heart', 'bi-heart-fill');
                
                const emptyMsg = container?.querySelector('.text-muted.text-center');
                if(emptyMsg) emptyMsg.remove();
                
                let imgHtml = `<i class="bi bi-box-seam text-secondary"></i>`;
                if (productImg && productImg.trim() !== '') {
                    imgHtml = `<img src="${productImg}" class="w-100 h-100 object-fit-cover" alt="Ảnh SP">`;
                }

                if (container) {
                    const html = `
                        <div class="popup-item" id="wishlist-item-${productId}">
                            <div style="width: 50px; height: 50px; background: #e5e5e5; border-radius: 4px; margin-right: 15px; display: flex; align-items: center; justify-content: center; overflow: hidden;">
                                ${imgHtml}
                            </div>
                            <div class="popup-item-info">
                                <a href="/san-pham/${productId}">${productName}</a>
                                <div class="text-danger fw-semibold mt-1">${productPrice}</div>
                                <button class="btn btn-sm btn-outline-primary mt-2 w-100" onclick="addToCartFromWishlist(${productId}, null)">
                                    <i class="bi bi-cart-plus"></i> Thêm vào giỏ
                                </button>
                            </div>
                            <button class="btn-delete-item" onclick="removeWishlist(${productId})" title="Xóa"><i class="bi bi-x-circle"></i></button>
                        </div>
                    `;
                    container.insertAdjacentHTML('beforeend', html);
                }
            }
        }
    }).catch(error => console.error("Lỗi yêu thích: ", error));
}

// Xóa Yêu thích trực tiếp từ nút (X) trong popup
function removeWishlist(wishlistId) {
    const item = document.getElementById('wishlist-item-' + wishlistId); 
    if (item) item.remove();
    
    fetch('/api/wishlist/toggle', { 
        method: 'POST', 
        headers: { 'Content-Type': 'application/json' }, 
        body: JSON.stringify({ productId: wishlistId }) 
    });
    
    // Tắt màu đỏ của nút tim ngoài giao diện (nếu đang đứng ở trang có chứa SP đó)
    const btnHearts = document.querySelectorAll(`.btn-wishlist-card[onclick*="toggleWishlist(this, ${wishlistId})"], .btn-detail-wishlist[onclick*="toggleWishlist(this, ${wishlistId})"]`);
    btnHearts.forEach(btnHeart => {
        btnHeart.classList.remove('active'); 
        const icon = btnHeart.querySelector('i');
        if(icon) icon.classList.replace('bi-heart-fill', 'bi-heart');
    });
}


/* ══════════════════════════════════════════
   XỬ LÝ GIỎ HÀNG (Fetch API)
══════════════════════════════════════════ */
function addToCart(productId, variantId, quantity) {
    const container = document.getElementById('cart-items-container');
    const cartPopup = document.getElementById('cart-popup');
    
    const btnAdd = document.querySelector(`.ds-add-cart[data-id="${productId}"]`);
    const productName = btnAdd ? btnAdd.getAttribute('data-name') : 'Sản phẩm ' + productId;
    const card = btnAdd ? btnAdd.closest('.ds-product-card') : null;
    const productPrice = card ? card.querySelector('.ds-product-price').innerText : 'Đang cập nhật';
    
    fetch('/api/cart/add-popup', { 
        method: 'POST', 
        headers: { 'Content-Type': 'application/json' }, 
        body: JSON.stringify({ productId: productId, variantId: variantId, quantity: quantity }) 
    })
    .then(response => response.json())
    .then(data => {
        if(data.success) {
            alert("Đã thêm " + productName + " vào giỏ hàng!");
            if (cartPopup) {
                const wlPopup = document.getElementById('wishlist-popup'); 
                if(wlPopup) wlPopup.style.display = 'none';
                cartPopup.style.display = 'block'; 
                setTimeout(() => { cartPopup.style.display = 'none'; }, 3000);
            }
            // Reload lại trang để đồng bộ HTML popup từ Thymeleaf GlobalControllerAdvice
            setTimeout(() => { window.location.reload(); }, 1000);
        }
    }).catch(error => console.error("Lỗi giỏ hàng: ", error));
}

function addToCartFromWishlist(productId, variantId) { 
    addToCart(productId, variantId, 1); 
}

function removeCartItem(cartItemId) {
    const item = document.getElementById('cart-item-' + cartItemId);
    if (item) item.remove();
    
    fetch('/api/cart/remove-item', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ variantId: cartItemId })
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            window.location.reload(); 
        } else {
            alert("Lỗi hệ thống: " + data.message);
        }
    })
    .catch(error => console.error("Lỗi xóa giỏ hàng:", error));
}
/* =====================================================
   TĂNG GIẢM SỐ LƯỢNG TRỰC TIẾP TRONG POPUP (CỰC MƯỢT)
===================================================== */
function updatePopupQty(variantId, delta) {
    const qtySpan = document.getElementById('popup-qty-' + variantId);
    const priceSpan = document.getElementById('popup-price-' + variantId);
    if (!qtySpan) return;
    
    // 1. Lấy số lượng hiện tại
    let currentQty = parseInt(qtySpan.innerText) || 1;
    let newQty = currentQty + delta;
    
    // 2. Nếu giảm về 0 thì hỏi người dùng có muốn xóa không
    if (newQty < 1) {
        if(confirm('Bạn có chắc chắn muốn xóa sản phẩm này khỏi giỏ hàng?')) {
            removeCartItem(variantId);
        }
        return;
    }

    // 3. Tự động tính tiền và nhảy số ngay lập tức trên màn hình (UX siêu mượt)
    qtySpan.innerText = newQty;
    if (priceSpan) {
        let unitPrice = parseInt(priceSpan.getAttribute('data-price')) || 0;
        let newTotal = unitPrice * newQty;
        priceSpan.innerText = newTotal.toLocaleString('vi-VN') + 'đ';
    }

    // 4. Gọi API cập nhật ngầm xuống Database
    fetch('/api/cart/update', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ variantId: variantId, soLuong: newQty })
    }).then(r => r.json()).then(data => {
        if (data.success) {
            // Cập nhật số lượng trên cái vòng tròn đỏ của Icon Giỏ hàng
            if (typeof updateCartBadge === 'function') updateCartBadge(data.cartCount);
        } else {
            // Nếu có lỗi mạng, trả lại số cũ
            alert(data.message || 'Cập nhật thất bại!');
            qtySpan.innerText = currentQty;
        }
    }).catch(() => {
        alert('Lỗi kết nối hệ thống. Vui lòng thử lại!');
        qtySpan.innerText = currentQty; // Trả lại số cũ nếu rớt mạng
    });
}
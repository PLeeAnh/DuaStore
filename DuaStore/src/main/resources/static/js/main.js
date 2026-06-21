/* =====================================================
   DuaStore — main.js (Client)
===================================================== */
'use strict';

/* ── CSRF: auto-inject token into same-origin non-GET fetch ── */
(function() {
    const token = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
    const header = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
    if (!token || !header) return;
    const orig = window.fetch;
    window.fetch = function(url, opts) {
        opts = opts || {};
        if (!opts.method || opts.method.toUpperCase() === 'GET') return orig.call(this, url, opts);
        const isSameOrigin = typeof url === 'string' && (url.startsWith('/') || new URL(url, location.origin).origin === location.origin);
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
    const toggle = document.getElementById('dsNavToggle');
    const panel = document.getElementById('dsNavPanel');
    const overlay = document.getElementById('dsNavOverlay');

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
        document.querySelectorAll('.ds-sub-menu.open').forEach(el => el.classList.remove('open'));
        document.querySelectorAll('.ds-chevron.rotated').forEach(el => el.classList.remove('rotated'));
    }
    if (toggle && panel) {
        toggle.addEventListener('click', () => panel.classList.contains('open') ? closeNav() : openNav());
    }
    if (overlay) overlay.addEventListener('click', closeNav);

    document.querySelectorAll('.ds-sub-toggle').forEach(btn => {
        const menu = btn.nextElementSibling;
        if (menu) {
            btn.addEventListener('click', e => {
                e.preventDefault();
                menu.classList.toggle('open');
                btn.querySelector('.ds-chevron')?.classList.toggle('rotated');
            });
        }
    });
    document.querySelectorAll('.ds-nav-panel .ds-nav-link:not(.ds-nav-no-close), .ds-nav-panel .ds-sub-link').forEach(link => {
        link.addEventListener('click', () => {
            if (!link.classList.contains('ds-sub-toggle')) setTimeout(closeNav, 200);
        });
    });

    /* ═══ SWIPERS ═══ */
    if (typeof Swiper !== 'undefined') {
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
        const sr = ScrollReveal({ origin: 'bottom', distance: '40px', duration: 800, delay: 200, easing: 'ease-out' });
        sr.reveal('.sr-card', { interval: 200 });
        sr.reveal('.sr-up', {});
        sr.reveal('.sr-left', { origin: 'left', distance: '60px' });
        sr.reveal('.sr-right', { origin: 'right', distance: '60px' });
    }

    /* ═══ PROFILE MENU ═══ */
    const prTrigger = document.getElementById('dsProfileTrigger');
    const prDropdown = document.getElementById('dsProfileDropdown');
    if (prTrigger && prDropdown) {
        prTrigger.addEventListener('click', e => {
            e.stopPropagation();
            prTrigger.setAttribute('aria-expanded', prDropdown.classList.toggle('open'));
        });
        document.addEventListener('click', e => {
            if (!prTrigger.contains(e.target) && !prDropdown.contains(e.target)) {
                prDropdown.classList.remove('open');
                prTrigger.setAttribute('aria-expanded', 'false');
            }
        });
    }

    /* ═══ FAQ ACCORDION ═══ */
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
            if (days > 0) { span.textContent = days + ' days left'; return; }
            const h = Math.floor(diff / 3600000);
            const m = Math.floor((diff % 3600000) / 60000);
            const s = Math.floor((diff % 60000) / 1000);
            span.textContent = String(h).padStart(2, '0') + 'h ' + String(m).padStart(2, '0') + 'm ' + String(s).padStart(2, '0') + 's';
        }
        tick();
        setInterval(tick, 1000);
    });
});

/* ═══ BACK TO TOP ═══ */
const backTopBtn = document.getElementById('backTopBtn');
if (backTopBtn) {
    window.addEventListener('scroll', () => {
        backTopBtn.style.display = window.scrollY > 400 ? 'flex' : 'none';
    }, { passive: true });
}

/* ═══ UPDATE CART BADGE ═══ */
function updateCartBadge(count) {
    const badge = document.getElementById('cartBadge');
    if (!badge) return;

    count = Number(count) || 0;

    if (count <= 0) {
        badge.classList.add('d-none');
        return;
    }

    badge.textContent = count > 99 ? '99+' : String(count);

    const viewed = localStorage.getItem('cartViewed');

    if (!viewed) {
        badge.classList.remove('d-none');
    }
    
}

/* ═══ POPUP TOGGLE ═══ */
// ĐÓNG / MỞ POPUP (Tích hợp tự động ẩn chấm đỏ thông báo)
/* ═══ POPUP TOGGLE ═══ */
function togglePopup(popupId) {
    document.querySelectorAll('.custom-popup').forEach(p => {
        if (p.id !== popupId) p.style.display = 'none';
    });

    const popup = document.getElementById(popupId);

    if (popup) {
        popup.style.display =
            popup.style.display === 'block' ? 'none' : 'block';

        // Wishlist
        if (popupId === 'wishlist-popup' &&
            popup.style.display === 'block') {

            document.getElementById('wishlistBadge')
                ?.classList.add('d-none');
        }

        // Cart
        if (popupId === 'cart-popup' &&
            popup.style.display === 'block') {

            document.getElementById('cartBadge')
                ?.classList.add('d-none');

            localStorage.setItem('cartViewed', 'true');
        }
    }
}

document.addEventListener('click', function(e) {
    // 🛠 QUAN TRỌNG: Ngăn popup đóng khi click vào nút thùng rác (phần tử vừa bị xóa khỏi DOM)
    if (!document.body.contains(e.target)) return;

    ['wishlist', 'cart', 'profile'].forEach(type => {
        const btn = document.getElementById('btn-' + type + '-toggle');
        const popup = document.getElementById(type + '-popup');
        
        // Nếu click ra ngoài cả nút bật và popup thì mới đóng
        if (btn && popup && !btn.contains(e.target) && !popup.contains(e.target)) {
            popup.style.display = 'none';
        }
    });
});
document.addEventListener('click', function(e) {
    ['wishlist', 'cart', 'profile'].forEach(type => {
        const btn = document.getElementById('btn-' + type + '-toggle');
        const popup = document.getElementById(type + '-popup');
        if (btn && popup && !btn.contains(e.target) && !popup.contains(e.target)) popup.style.display = 'none';
    });
});

/* ═══ LOGIN POPUP ═══ */
function showLoginPopup() {
    const modal = new bootstrap.Modal(document.getElementById('loginModal'));
    modal.show();
}

/* ═══ REGISTER ═══ */
function registerSubmit(event) {
    event.preventDefault();
    const form = document.getElementById('registerForm');
    const errDiv = document.getElementById('registerError');
    const okDiv = document.getElementById('registerSuccess');
    errDiv.classList.remove('show');
    okDiv.classList.remove('show');
    const errText = errDiv.querySelector('span');
    const btn = document.getElementById('regSubmitBtn');

    if (form.querySelector('[name="password"]').value !== form.querySelector('[name="confirmPassword"]').value) {
        errText.textContent = 'Mật khẩu xác nhận không khớp';
        errDiv.classList.add('show');
        return false;
    }

    const email = form.querySelector('[name="email"]').value;
    const code = form.querySelector('[name="verificationCode"]').value;
    if (!code) {
        errText.textContent = 'Vui lòng nhập mã xác thực';
        errDiv.classList.add('show');
        return false;
    }

    btn.disabled = true;
    btn.textContent = 'Đang xử lý...';

    fetch('/api/auth/verify-code', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, code })
    }).then(r => r.json()).then(data => {
        if (!data.success) {
            errText.textContent = 'Mã xác thực không đúng';
            errDiv.classList.add('show');
            btn.disabled = false;
            btn.textContent = 'Đăng ký';
            return;
        }
        fetch('/dang-ky', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: new URLSearchParams(new FormData(form))
        }).then(r => r.text()).then(html => {
            if (html.includes('is-invalid') || html.includes('alert-danger')) {
                const tmp = document.createElement('div');
                tmp.innerHTML = html;
                const msg = tmp.querySelector('.invalid-feedback') || tmp.querySelector('.alert-danger');
                errText.textContent = msg ? msg.textContent.trim() : 'Đăng ký thất bại';
                errDiv.classList.add('show');
                btn.disabled = false;
                btn.textContent = 'Đăng ký';
            } else {
                okDiv.classList.add('show');
                form.reset();
                setTimeout(() => {
                    const m = bootstrap.Modal.getInstance(document.getElementById('registerModal'));
                    if (m) m.hide();
                    showLoginPopup();
                }, 1500);
            }
        });
    }).catch(function() {
        errText.textContent = 'Lỗi kết nối hệ thống';
        errDiv.classList.add('show');
        btn.disabled = false;
        btn.textContent = 'Đăng ký';
    });
    return false;
}

/* ═══ WISHLIST ═══ */
/* ═══ WISHLIST ═══ */
function toggleWishlist(btnElement, productId) {
    const icon = btnElement.querySelector('i');
    const container = document.getElementById('wishlist-items-container');
    let productName = 'Sản phẩm ' + productId;
    let productPrice = 'Đang cập nhật';
    let productImg = '';
    const card = btnElement.closest('.ds-product-card');
    
    if (card) {
        const nameEl = card.querySelector('.ds-product-name');
        const priceEl = card.querySelector('.ds-price-btn-amount');
        if (nameEl) productName = nameEl.textContent;
        if (priceEl) productPrice = priceEl.textContent;
    } else {
        const detailName = document.querySelector('.product-detail-info h3');
        const detailPrice = document.getElementById('productPrice');
        const detailImg = document.getElementById('mainImage');
        if (detailName) productName = detailName.innerText;
        if (detailPrice) productPrice = detailPrice.innerText;
        if (detailImg) productImg = detailImg.src;
    }
    
    fetch('/api/wishlist/toggle', {
        method: 'POST', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ productId })
    }).then(r => r.json()).then(data => {
        if (!data.success) {
            if (data.message && data.message.includes('dang nhap')) showLoginPopup();
            return;
        }
        
        if (btnElement.classList.contains('active')) {
            // HỦY YÊU THÍCH
            btnElement.classList.remove('active');
            icon.classList.replace('bi-heart-fill', 'bi-heart');
            
            // Xóa phần tử khỏi danh sách popup
            const item = document.getElementById('wishlist-item-' + productId);
            if (item) item.remove();
            
            // => GỌI HÀM CẬP NHẬT ĐỂ TRỪ SỐ TRÊN BADGE
            refreshWishlistBadgeCount();
            
        } else {
            // THÊM YÊU THÍCH
            btnElement.classList.add('active');
            icon.classList.replace('bi-heart', 'bi-heart-fill');
            const emptyMsg = container?.querySelector('.text-muted.text-center');
            if (emptyMsg) emptyMsg.remove();
            
            const imgHtml = productImg && productImg.trim()
                ? '<img src="' + productImg + '" class="w-100 h-100 object-fit-cover" alt="SP">'
                : '<i class="bi bi-box-seam text-secondary"></i>';
                
            if (container) {
                // Thêm phần tử mới vào danh sách popup
                container.insertAdjacentHTML('beforeend',
                    '<div class="popup-item" id="wishlist-item-' + productId + '">' +
                        '<div style="width:50px;height:50px;background:#e5e5e5;border-radius:4px;margin-right:15px;display:flex;align-items:center;justify-content:center;overflow:hidden;flex-shrink:0;">' + imgHtml + '</div>' +
                        '<div class="popup-item-info">' +
                            '<a href="/san-pham/' + productId + '">' + productName + '</a>' +
                            '<div class="text-danger fw-semibold mt-1">' + productPrice + '</div>' +
                        '</div>' +
                        '<button class="btn-delete-item" onclick="removeWishlist(' + productId + ')" title="Xóa"><i class="bi bi-x-circle"></i></button>' +
                    '</div>');
            }
            
            // => GỌI HÀM CẬP NHẬT ĐỂ CỘNG SỐ TRÊN BADGE VÀ HIỂN THỊ
            refreshWishlistBadgeCount();
        }
    }).catch(error => console.error("Lỗi yêu thích: ", error));
}

/* ═══ WISHLIST (Xóa) ═══ */
function removeWishlist(wishlistId) {
    // Ngăn chặn sự kiện click lan ra ngoài (khóa chặt popup không bị đóng đột ngột)
    if (window.event) { 
        window.event.stopPropagation(); 
        window.event.preventDefault(); 
    }

    // Xóa thẻ HTML của sản phẩm đó khỏi popup
    const item = document.getElementById('wishlist-item-' + wishlistId);
    if (item) item.remove();

    // => GỌI HÀM CẬP NHẬT LẠI SỐ TRÊN ICON NGAY SAU KHI XÓA
    refreshWishlistBadgeCount();

    // Bắn API gọi Server để xóa trong CSDL
    fetch('/api/wishlist/toggle', {
        method: 'POST', 
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ productId: wishlistId })
    }).catch(error => console.error('Lỗi xóa wishlist:', error));

    // Reset lại màu nút trái tim ở ngoài trang web (chuyển từ Đỏ -> Xám)
    document.querySelectorAll('.btn-wishlist-card[onclick*="toggleWishlist(this, ' + wishlistId + ')"], .btn-detail-wishlist[onclick*="toggleWishlist(this, ' + wishlistId + ')"]').forEach(btn => {
        btn.classList.remove('active');
        const ic = btn.querySelector('i');
        if (ic) ic.classList.replace('bi-heart-fill', 'bi-heart');
    });
}

/* ═══ CART ═══ */
function addToCart(productId, variantId, quantity) {
    const card = document.querySelector('.ds-product-card[data-productid="' + productId + '"]');
    const btnAdd = card ? card.querySelector('.ds-card-add-cart') : null;
    fetch('/api/cart/add-popup', {
        method: 'POST', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ productId, variantId, quantity })
    }).then(r => r.json()).then(data => {
        if (data.success) {
            if (btnAdd) btnAdd.classList.add('added');
            if (typeof updateCartBadge === 'function') updateCartBadge(data.cartCount);
            
            // ---> THÊM DÒNG NÀY ĐỂ RENDER ITEM VÀO POPUP <---
            if (card) {
                addCartPopupItem(card, productId, variantId, quantity);
            }
        }
    }).catch(error => console.error("Lỗi giỏ hàng: ", error));
}

function addToCartFromWishlist(productId, variantId) { addToCart(productId, variantId, 1); }

function addToCartFromCard(btn) {
    if (!btn || btn.disabled) return;
    const card = btn.closest('.ds-product-card');
    if (!card) return;
    const productId = btn.getAttribute('data-id');
    const qty = parseInt(card.querySelector('.ds-qty-val').value) || 1;
    const activeChip = card.querySelector('.ds-variant-chip.active')
        || card.querySelector('.ds-variant-chip:not(.oos)')
        || card.querySelector('.ds-variant-chip');
    const variantId = activeChip ? parseInt(activeChip.getAttribute('data-variantid')) : null;
    fetch('/api/cart/add-popup', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ productId: parseInt(productId), variantId, quantity: qty })
    }).then(function(r) { return r.json(); }).then(function(data) {
        if (data.success) {
            btn.classList.add('added');
            if (typeof updateCartBadge === 'function') updateCartBadge(data.cartCount);

            // ---> THÊM DÒNG NÀY ĐỂ RENDER ITEM VÀO POPUP <---
            addCartPopupItem(card, productId, variantId, qty);
        }
    }).catch(function(err) {
        console.error('Lỗi thêm giỏ hàng:', err);
    });
}

/* ═══ CART (Xóa) ═══ */
function removeCartItem(cartItemId) {
    // Ngăn chặn sự kiện click lan ra ngoài (khóa chặt popup)
    if (window.event) { window.event.stopPropagation(); window.event.preventDefault(); }

    const item = document.getElementById('cart-item-' + cartItemId);
    const pid = item ? item.getAttribute('data-productid') : null;
    if (item) item.remove();

    const container = document.getElementById('cart-items-container');
    if (container && container.querySelectorAll('.popup-item').length === 0) {
        container.innerHTML = '<div class="text-center py-4 text-muted"><i class="bi bi-cart-x" style="font-size:2rem;"></i><p class="mt-2 mb-0">Giỏ hàng trống</p></div>';
        const chk = document.querySelector('#cart-popup .mt-2.pt-2');
        if (chk) chk.remove();
    }
    fetch('/api/cart/remove-item', {
        method: 'POST', headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ variantId: cartItemId })
    }).then(r => r.json()).then(data => {
        if (data.success) {
            if (typeof updateCartBadge === 'function') updateCartBadge(data.cartCount);
            if (data.remainingItems === 0 && pid) {
                document.querySelectorAll('.ds-product-card[data-productid="' + pid + '"]').forEach(card => {
                    const b = card.querySelector('.ds-card-add-cart');
                    if (b) b.classList.remove('added');
                });
            }
        } else alert("Lỗi hệ thống: " + data.message);
    }).catch(error => console.error("Lỗi xóa giỏ hàng:", error));
}

/* ═══ UPDATE POPUP QTY (+/- số lượng) ═══ */
function updatePopupQty(variantId, delta) {
    // Ngăn chặn sự kiện click lan ra ngoài (khóa chặt popup)
    if (window.event) { window.event.stopPropagation(); window.event.preventDefault(); }

    const qtySpan = document.getElementById('popup-qty-' + variantId);
    const priceSpan = document.getElementById('popup-price-' + variantId);
    if (!qtySpan) return;
    const cur = parseInt(qtySpan.innerText) || 1;
    const next = cur + delta;

    if (next < 1) {
        if (confirm('Bạn có chắc chắn muốn xóa sản phẩm này khỏi giỏ hàng?')) removeCartItem(variantId);
        return;
    }
    
    // Tạm cập nhật UI trước cho mượt
    qtySpan.innerText = next;
    if (priceSpan) {
        const unit = parseInt(priceSpan.getAttribute('data-price')) || 0;
        priceSpan.innerText = (unit * next).toLocaleString('vi-VN') + 'đ';
    }
    
    fetch('/api/cart/update', {
        method: 'POST', headers: { 'Content-Type': 'application/json' },
        // ✅ ĐÃ SỬA: Thay soLuong thành quantity để khớp với Spring Boot DTO
        body: JSON.stringify({ variantId: variantId, quantity: next }) 
    }).then(r => r.json()).then(data => {
        if (data.success) { 
            if (typeof updateCartBadge === 'function') updateCartBadge(data.cartCount); 
        }
        else { 
            alert(data.message || 'Cập nhật thất bại!'); 
            // Rollback UI nếu server báo lỗi (VD: Không đủ tồn kho)
            qtySpan.innerText = cur; 
            if (priceSpan) {
                const unit = parseInt(priceSpan.getAttribute('data-price')) || 0;
                priceSpan.innerText = (unit * cur).toLocaleString('vi-VN') + 'đ';
            }
        }
    }).catch(err => {
        alert('Lỗi kết nối hệ thống. Vui lòng thử lại!');
        qtySpan.innerText = cur;
        if (priceSpan) {
            const unit = parseInt(priceSpan.getAttribute('data-price')) || 0;
            priceSpan.innerText = (unit * cur).toLocaleString('vi-VN') + 'đ';
        }
        console.error('Lỗi cập nhật SL:', err);
    });
}

/* ═══ HELPERS ═══ */
function getActiveVariant(card) {
    return card.querySelector('.ds-variant-chip.active')
        || card.querySelector('.ds-variant-chip:not(.oos)')
        || card.querySelector('.ds-variant-chip');
}

function addCartPopupItem(card, productId, variantId, qty) {
    const container = document.getElementById('cart-items-container');
    if (!container) return;
    const emptyMsg = container.querySelector('.text-center.text-muted');
    if (emptyMsg) emptyMsg.remove();

    const nameEl = card.querySelector('.ds-product-name-overlay');
    const productName = nameEl ? nameEl.textContent : 'Sản phẩm ' + productId;
    const imgEl = card.querySelector('.ds-product-img-wrap img');
    const imgSrc = imgEl ? imgEl.getAttribute('src') || '' : '';
    const imgHtml = imgSrc
        ? '<img src="' + imgSrc + '" class="w-100 h-100 object-fit-cover" alt="SP">'
        : '<i class="bi bi-box-seam text-secondary"></i>';
    const activeChip = getActiveVariant(card);
    const variantName = activeChip ? activeChip.textContent : 'Mặc định';
    const rawPrice = activeChip ? parseInt(activeChip.getAttribute('data-price')) : 0;
    const priceFmt = rawPrice.toLocaleString('vi-VN') + 'đ';

    const existing = document.getElementById('cart-item-' + variantId);
    if (existing) {
        const qtySpan = existing.querySelector('#popup-qty-' + variantId);
        if (qtySpan) {
            const newQty = parseInt(qtySpan.textContent) + qty;
            qtySpan.textContent = newQty;
            const ps = existing.querySelector('#popup-price-' + variantId);
            if (ps) ps.textContent = (rawPrice * newQty).toLocaleString('vi-VN') + 'đ';
        }
        return;
    }

    const html = '<div class="popup-item d-flex align-items-start mb-3 pb-3 border-bottom" id="cart-item-' + variantId + '" data-productid="' + productId + '">' +
        '<div style="width:50px;height:50px;background:#e5e5e5;border-radius:4px;margin-right:12px;display:flex;align-items:center;justify-content:center;overflow:hidden;flex-shrink:0;">' + imgHtml + '</div>' +
        '<div class="popup-item-info flex-grow-1">' +
            '<a href="/san-pham/' + productId + '" class="text-truncate d-block text-dark fw-semibold" style="max-width:180px;font-size:0.9rem;">' + productName + '</a>' +
            '<div class="small text-muted mb-2" style="font-size:0.8rem;">' + variantName + '</div>' +
            '<div class="d-flex align-items-center">' +
                '<div class="input-group input-group-sm" style="width:85px;">' +
                    '<button class="btn btn-outline-secondary px-2 py-0" onclick="updatePopupQty(' + variantId + ',-1)">-</button>' +
                    '<span class="form-control text-center py-0 px-1" id="popup-qty-' + variantId + '">' + qty + '</span>' +
                    '<button class="btn btn-outline-secondary px-2 py-0" onclick="updatePopupQty(' + variantId + ',1)">+</button>' +
                '</div>' +
                '<span class="text-danger fw-semibold ms-auto popup-item-price" id="popup-price-' + variantId + '" data-price="' + rawPrice + '">' + priceFmt + '</span>' +
            '</div>' +
        '</div>' +
        '<button class="btn-delete-item ms-2 text-muted border-0 bg-transparent" onclick="removeCartItem(' + variantId + ')"><i class="bi bi-trash text-danger"></i></button>' +
    '</div>';
    container.insertAdjacentHTML('beforeend', html);

    if (!document.querySelector('#cart-popup .mt-2.pt-2')) {
        const popup = document.getElementById('cart-popup');
        if (popup) {
            const div = document.createElement('div');
            div.className = 'mt-2 pt-2';
            div.innerHTML = '<a href="/checkout" class="btn btn-danger w-100 fw-semibold py-2">Thanh toán tất cả</a>';
            popup.appendChild(div);
        }
    }
}

/* ── Render star rating readonly ── */
document.querySelectorAll('.star-rating-readonly').forEach(function(el) {
    const score = parseInt(el.dataset.score) || 0;
    el.innerHTML = '★'.repeat(score) + '☆'.repeat(5 - score);
});

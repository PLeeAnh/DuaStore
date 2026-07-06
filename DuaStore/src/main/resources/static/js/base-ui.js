/* ── Auth modal helpers ── */
function togglePass(id, btn) {
var inp = document.getElementById(id);
        var ico = btn.querySelector('i');
        if (inp.type === 'password') { inp.type = 'text'; ico.className = 'bi bi-eye'; }
else { inp.type = 'password'; ico.className = 'bi bi-eye-slash'; }

}
document.addEventListener('DOMContentLoaded', function() {
var profileBtn = document.getElementById('btn-profile-toggle');
        if (profileBtn) {
profileBtn.addEventListener('click', function() {
if (this.dataset.profile === 'true') { togglePopup('profile-popup'); }
else { showLoginPopup(); }
});
        }
});
        function switchModal(from, to) {
        bootstrap.Modal.getInstance(document.querySelector(from))?.hide();
                new bootstrap.Modal(document.querySelector(to)).show();
        }
function showAuthError(el) { if (el) el.classList.add('show'); }
function hideAuthError(el) { if (el) el.classList.remove('show'); }

document.addEventListener('DOMContentLoaded', function() {
/* ── Login error param ── */
var urlParams = new URLSearchParams(window.location.search);
        if (urlParams.has('loginError')) {
var m = new bootstrap.Modal(document.getElementById('loginModal'));
        m.show();
        showAuthError(document.getElementById('loginServerError'));
        }

/* ── Blur validation: show error when field loses focus and empty ── */
document.querySelectorAll('.ds-auth-field input').forEach(function(inp) {
var err = inp.closest('.mb-3')?.querySelector('.ds-auth-error');
        if (!err) return;
        inp.addEventListener('blur', function() {
        if (!inp.value.trim()) showAuthError(err); else hideAuthError(err);
        });
        inp.addEventListener('input', function() {
        if (inp.value.trim()) hideAuthError(err);
        });
        });
        /* ── Login form ── */
        var loginForm = document.querySelector('#loginModal form');
        if (loginForm) {
loginForm.addEventListener('input', function() {
var email = document.getElementById('loginEmail')?.value.trim() || '';
        var pass = document.getElementById('loginPassword')?.value.trim() || '';
        var btn = document.getElementById('loginSubmitBtn');
        var ok = email.length > 0 && pass.length >= 6;
        if (btn) { btn.disabled = !ok; btn.classList.toggle('active', ok); }
});
        }

/* ── Register form ── */
var regForm = document.getElementById('registerForm');
        if (regForm) {
regForm.addEventListener('input', function() {
var email = document.getElementById('regEmail')?.value.trim() || '';
        var pass = document.getElementById('regPassword')?.value.trim() || '';
        var confirm = document.getElementById('regConfirmPass')?.value.trim() || '';
        var terms = document.getElementById('regTerms');
        var btn = document.getElementById('regSubmitBtn');
        var ok = email.includes('@') && pass.length >= 6 && confirm === pass && terms?.checked;
        if (btn) { btn.disabled = !ok; btn.classList.toggle('active', ok); }
});
        document.getElementById('regTerms')?.addEventListener('change', function() { regForm.dispatchEvent(new Event('input')); });
        }

/* ── Forgot form ── */
var forgotForm = document.getElementById('forgotForm');
        if (forgotForm) {
forgotForm.addEventListener('input', function() {
var email = document.getElementById('forgotEmail')?.value.trim() || '';
        var pass = document.getElementById('forgotPass')?.value.trim() || '';
        var confirm = document.getElementById('forgotConfirmPass')?.value.trim() || '';
        var btn = document.getElementById('forgotSubmitBtn');
        var ok = email.includes('@') && pass.length >= 6 && confirm === pass;
        if (btn) { btn.disabled = !ok; btn.classList.toggle('active', ok); }
});
        }

/* ── Send code buttons ── */
document.querySelectorAll('.ds-auth-code-inline').forEach(function(btn) {
btn.addEventListener('click', function() {
var field = this.closest('.ds-auth-field')?.querySelector('input[type="email"]');
        var emailField = field || document.getElementById('regEmail') || document.getElementById('forgotEmail');
        var email = emailField?.value.trim();
        if (!email || !email.includes('@')) {
var errEl = emailField?.closest('.mb-3')?.querySelector('.ds-auth-error');
        if (errEl) { errEl.querySelector('span').textContent = 'Email không hợp lệ'; errEl.classList.add('show'); }
return;
        }
var orig = this.textContent;
        this.disabled = true;
        this.textContent = 'Đang gửi...';
        fetch('/api/auth/send-code', {
        method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email: email })
        }).then(function(r) { return r.json(); }).then(function(data) {
if (data.success) {
btn.textContent = 'Đã gửi';
        setTimeout(function() { btn.textContent = 'Gửi'; btn.disabled = false; }, 5000);
        } else {
btn.textContent = 'Thử lại';
        var errMsg = data.error || 'Gửi mã thất bại';
        var emailErr = emailField?.closest('.mb-3')?.querySelector('.ds-auth-error');
        if (emailErr) { emailErr.querySelector('span').textContent = errMsg; emailErr.classList.add('show'); }
setTimeout(function() { btn.textContent = 'Gửi'; btn.disabled = false; }, 2000);
        }
}).catch(function() {
btn.textContent = 'Lỗi';
        setTimeout(function() { btn.textContent = 'Gửi'; btn.disabled = false; }, 2000);
        });
        });
        });
        /* ── Forgot password submit ── */
        document.getElementById('forgotForm')?.addEventListener('submit', function(e) {
e.preventDefault();
        var email = document.getElementById('forgotEmail')?.value.trim();
        var code = document.getElementById('forgotCode')?.value.trim();
        var pass = document.getElementById('forgotPass')?.value.trim();
        var btn = document.getElementById('forgotSubmitBtn');
        if (!email || !code || !pass) return;
        btn.disabled = true;
        btn.textContent = 'Đang xử lý...';
        fetch('/api/auth/verify-code', {
        method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ email: email, code: code })
        }).then(function(r) { return r.json(); }).then(function(data) {
if (data.success) {
fetch('/quen-mat-khau', {
method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: new URLSearchParams({ email: email, password: pass })
        }).then(function(r) { return r.text(); }).then(function() {
btn.textContent = 'Thành công!';
        setTimeout(function() {
        bootstrap.Modal.getInstance(document.getElementById('forgotPasswordModal'))?.hide();
                showLoginPopup();
        }, 1000);
        });
        } else {
var errEl = document.getElementById('forgotCodeError');
        if (errEl) { errEl.querySelector('span').textContent = 'Mã xác thực không đúng'; errEl.classList.add('show'); }
btn.disabled = false;
        btn.textContent = 'OK';
        }
}).catch(function() {
btn.disabled = false;
        btn.textContent = 'OK';
        });
        });
});
        </script>
        <script>
document.addEventListener('DOMContentLoaded', function() {
// --- 1. LÔ-GÍC GHI NHỚ CHO GIỎ HÀNG ---
        const cartItems = document.querySelectorAll('#cart-items-container .popup-item');
        let currentCartState = [];
        cartItems.forEach(item => {
        const qtyEl = item.querySelector('input[id^="popup-qty-"]');
                const qty = qtyEl ? qtyEl.value : '1';
                currentCartState.push(item.id + '-qty-' + qty);
                });
        let viewedCartState = JSON.parse(localStorage.getItem('viewedCartState') || '[]');
// Badge count is managed by cart.js/wishlist.js, not shown on page load
});

/* ── HÀM BẬT/TẮT POPUP ── */
function togglePopup(popupId) {
        document.querySelectorAll('.custom-popup').forEach(function(popup) {
if (popup.id !== popupId) popup.style.display = 'none';
        });
        var popup = document.getElementById(popupId);
        if (popup) {
var isOpening = (popup.style.display !== 'block');
        popup.style.display = isOpening ? 'block' : 'none';
        if (isOpening) {
if (popupId === 'wishlist-popup') {
var wBadge = document.getElementById('wishlistBadge');
        if (wBadge) wBadge.classList.add('d-none');
        const items = document.querySelectorAll('#wishlist-items-container .popup-item');
        let states = [];
        items.forEach(i => states.push(i.id));
        localStorage.setItem('viewedWishlistState', JSON.stringify(states));
        } else if (popupId === 'cart-popup') {
var cBadge = document.getElementById('cartBadge');
        if (cBadge) cBadge.classList.add('d-none');
        const items = document.querySelectorAll('#cart-items-container .popup-item');
        let states = [];
        items.forEach(item => {
        const inp = item.querySelector('input[id^="popup-qty-"]');
                const qty = inp ? inp.value : '1';
                states.push(item.id + '-qty-' + qty);
                });
        localStorage.setItem('viewedCartState', JSON.stringify(states));
        }
}
}
}

/* ── HÀM TĂNG GIẢM SỐ LƯỢNG ── */
function updatePopupQty(itemId, delta) {
        const qtyInput = document.getElementById('popup-qty-' + itemId);
        const priceSpan = document.getElementById('popup-price-' + itemId);
        if (!qtyInput) return;
        let currentQty = parseInt(qtyInput.value) || 1;
        let newQty = currentQty + delta;
        let stock = parseInt(qtyInput.getAttribute('data-stock')) || 999;
        if (newQty < 1) {
if (confirm('Bạn có chắc chắn muốn xóa sản phẩm này khỏi giỏ hàng?')) {
removeCartItem(itemId);
        }
return;
        }

if (newQty > stock) {
DuaStore.toast.warning('Chỉ còn ' + stock + ' sản phẩm trong kho');
        qtyInput.value = stock;
        newQty = stock;
        }

qtyInput.value = newQty;
        if (priceSpan) {
let unitPrice = parseInt(priceSpan.getAttribute('data-price')) || 0;
        priceSpan.innerText = (unitPrice * newQty).toLocaleString('vi-VN') + '₫';
        }

fetch('/api/cart/update', {
method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ variantId: parseInt(itemId), itemId: parseInt(itemId), soLuong: newQty })
        })
        .then(function(r) {
        if (r.status === 403) {
        if (typeof showLoginPopup === 'function') showLoginPopup();
                qtyInput.value = currentQty;
                return null;
                }
        return r.json();
                })
        .then(data => {
        if (!data) return;
                if (!data.success) {
        DuaStore.toast.error(data.message);
                qtyInput.value = currentQty;
                return;
                }

        const items = document.querySelectorAll('#cart-items-container .popup-item');
                let states = [];
                items.forEach(item => {
                const inp = item.querySelector('input[id^="popup-qty-"]');
                        const q = inp ? inp.value : '1';
                        states.push(item.id + '-qty-' + q);
                });
                localStorage.setItem('viewedCartState', JSON.stringify(states));
                if (typeof updateCartBadge === 'function') updateCartBadge(data.cartCount);
                })
        .catch(err => {
        console.error('Lỗi đồng bộ giỏ hàng:', err);
                qtyInput.value = currentQty;
                });
}

/* ── HÀM NHẬP TRỰC TIẾP SỐ LƯỢNG ── */
/* ── Validate popup qty on manual input ── */
function validatePopupQty(itemId, el) {
        var stock = parseInt(el.getAttribute('data-stock')) || 999;
        var val = parseInt(el.value) || 1;
        if (val < 1) val = 1;
        if (val > stock) { val = stock; DuaStore.toast.warning('Chỉ còn ' + stock + ' sản phẩm'); }
el.value = val;
        fetch('/api/cart/update', {
        method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ variantId: parseInt(itemId), itemId: parseInt(itemId), soLuong: val })
                })
        .then(function(r) {
        if (r.status === 403) { if (typeof showLoginPopup === 'function') showLoginPopup(); return null; }
        return r.json();
                })
        .then(function(data) {
        if (!data) return;
                if (data.success && typeof updateCartBadge === 'function') updateCartBadge(data.cartCount);
                })
        .catch(function() {});
}

function setPopupQty(itemId) {
        const qtyInput = document.getElementById('popup-qty-' + itemId);
        const priceSpan = document.getElementById('popup-price-' + itemId);
        if (!qtyInput) return;
        let val = parseInt(qtyInput.value) || 1;
        let stock = parseInt(qtyInput.getAttribute('data-stock')) || 999;
        if (val < 1) val = 1;
        if (val > stock) {
DuaStore.toast.warning('Chỉ còn ' + stock + ' sản phẩm trong kho');
        val = stock;
        }

qtyInput.value = val;
        if (priceSpan) {
let unitPrice = parseInt(priceSpan.getAttribute('data-price')) || 0;
        priceSpan.innerText = (unitPrice * val).toLocaleString('vi-VN') + '₫';
        }

fetch('/api/cart/update', {
method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ variantId: parseInt(itemId), itemId: parseInt(itemId), soLuong: val })
        })
        .then(function(r) {
        if (r.status === 403) {
        if (typeof showLoginPopup === 'function') showLoginPopup();
                return null;
                }
        return r.json();
                })
        .then(data => {
        if (!data) return;
                if (!data.success) {
        DuaStore.toast.error(data.message);
                return;
                }
        if (typeof updateCartBadge === 'function') updateCartBadge(data.cartCount);
                })
        .catch(err => console.error('Lỗi cập nhật:', err));
}
function refreshWishlistBadgeCount() {
// Đếm số lượng thẻ sản phẩm đang nằm trong popup container
        let currentCount = document.querySelectorAll('#wishlist-items-container .popup-item').length;
        let badge = document.getElementById('wishlistBadge');
        if (!badge) return;
        if (currentCount > 0) {
badge.textContent = currentCount;
        badge.classList.remove('d-none'); // Hiển thị badge che icon
        } else {
badge.classList.add('d-none'); // Ẩn badge nếu không còn sản phẩm nào

// Hiển thị trạng thái trống kèm icon trái tim tan vỡ
        let container = document.getElementById('wishlist-items-container');
        if (container && !container.querySelector('.bi-heartbreak')) {
container.innerHTML = `
        <div class="text-center py-4 text-muted">
            <i class="bi bi-heartbreak" style="font-size: 2rem;"></i>
            <p class="mt-2 mb-0">Chưa có sản phẩm nào</p>
        </div>
    `;
        }
}
}

/* ── THÔNG BÁO ── */
function toggleNotifPopup() {
        togglePopup('notif-popup');
        var badge = document.getElementById('notifBadge');
        if (badge && !badge.classList.contains('d-none')) {
fetch('/api/thong-bao/doc-tat-ca', { method: 'POST' })
        .then(function(r) {
        if (r.status === 403) {
        if (typeof showLoginPopup === 'function') showLoginPopup();
                return null;
                }
        return r.text();
                })
        .then(function(text) {
        if (text === null) return;
                if (text && (text.indexOf('dang nhap') !== - 1 || text.indexOf('đăng nhập') !== - 1)) {
        if (typeof showLoginPopup === 'function') showLoginPopup();
                return;
                }
        badge.classList.add('d-none');
                })
        .catch(function() { badge.classList.add('d-none'); });
        }
}
function markNotifRead(id) {
        fetch('/api/thong-bao/doc/' + id, { method: 'POST' })
        .then(function(r) {
        if (r.status === 403) {
        if (typeof showLoginPopup === 'function') showLoginPopup();
                return null;
                }
        return r.text();
                })
        .then(function(text) {
        if (text === null) return;
                if (text && (text.indexOf('dang nhap') !== - 1 || text.indexOf('đăng nhập') !== - 1)) {
        if (typeof showLoginPopup === 'function') showLoginPopup();
                }
        })
        .catch(function() {});
}
function getNotifIcon(linkType) {
        if (linkType === 'PRODUCT') return '<span class="notif-icon bg-info-subtle text-info flex-shrink-0"><i class="bi bi-box-seam"></i></span>';
        if (linkType === 'PROMOTION') return '<span class="notif-icon bg-warning-subtle text-warning flex-shrink-0"><i class="bi bi-tag"></i></span>';
        if (linkType === 'ORDER') return '<span class="notif-icon bg-info-subtle text-info flex-shrink-0"><i class="bi bi-bag-check"></i></span>';
        return '<span class="notif-icon bg-primary-subtle text-primary flex-shrink-0"><i class="bi bi-megaphone"></i></span>';
}

function pollNotifications() {
        fetch('/api/thong-bao')
        .then(r => r.json())
        .then(data => {
        var badge = document.getElementById('notifBadge');
                if (badge) {
        if (data.count > 0) {
        badge.textContent = data.count;
                badge.classList.remove('d-none');
                } else {
        badge.classList.add('d-none');
                }
        }
        var container = document.querySelector('#notif-popup .mt-2');
                if (!container) return;
                if (!data.notifications || data.notifications.length === 0) {
        container.innerHTML = '<div class="text-center py-4 text-muted"><i class="bi bi-bell-slash" style="font-size: 2rem;"></i><p class="mt-2 mb-0">Chưa có thông báo</p></div>';
                return;
                }
        var html = '';
                data.notifications.forEach(function(n) {
                if (n.linkType) {
                html += '<a href="' + n.linkUrl + '" class="text-decoration-none text-reset d-block notif-item">';
                        html += '<div class="d-flex align-items-start gap-2">';
                        html += getNotifIcon(n.linkType);
                        html += '<div class="flex-grow-1 min-w-0">';
                        html += '<p class="mb-0 notif-text">' + n.content.substring(0, 100) + '</p>';
                        if (n.linkLabel) {
                html += '<div class="mt-1"><span class="notif-link"><span>' + n.linkLabel.substring(0, 35) + '</span><i class="bi bi-arrow-right ms-1"></i></span></div>';
                }
                html += '<small class="notif-time">' + n.time + '</small>';
                        html += '</div></div></a>';
                } else {
                html += '<div class="notif-item"><div class="d-flex align-items-start gap-2">';
                        html += getNotifIcon(null);
                        html += '<div class="flex-grow-1 min-w-0">';
                        html += '<p class="mb-0 notif-text">' + n.content.substring(0, 100) + '</p>';
                        html += '<small class="notif-time">' + n.time + '</small>';
                        html += '</div></div></div>';
                }
                });
                container.innerHTML = html;
                })
        .catch(function() {});
}

setInterval(pollNotifications, 15000);

/* ── Dirty Save Bar ── */
function initDirtyBar() {
        var bar = document.getElementById('dsSaveBar');
        if (!bar) return;
        var forms = document.querySelectorAll('form[data-dirty-bar]');
        if (!forms.length) return;
        var activeForm = null;
        var resetBtn = document.getElementById('dsSaveBarReset');
        var saveBtn = document.getElementById('dsSaveBarSave');
        function getFormData(f) { return new FormData(f); }

function checkDirty(f) {
if (!f._cleanData) { f._cleanData = getFormData(f); f._dirty = false; return; }
var current = getFormData(f);
        var dirty = false;
        var keys = new Set();
        for (var pair of f._cleanData.entries()) keys.add(pair[0]);
        for (var pair of current.entries()) keys.add(pair[0]);
        keys.forEach(function(k) {
        var v1 = f._cleanData.getAll(k).sort().join(',');
                var v2 = current.getAll(k).sort().join(',');
                if (v1 !== v2) dirty = true;
                });
        if (dirty !== f._dirty) { f._dirty = dirty; updateBar(dirty, f); }
}

function updateBar(dirty, f) {
if (dirty) {
activeForm = f;
        bar.style.display = 'flex';
        requestAnimationFrame(function() { bar.classList.add('show'); });
        } else {
bar.classList.remove('show');
        activeForm = null;
        setTimeout(function() { bar.style.display = 'none'; }, 300);
        }
}

function resetDirty(f) {
f.reset();
        setTimeout(function() {
        f._cleanData = getFormData(f);
                f._dirty = false;
                updateBar(false, f);
                }, 50);
        }

forms.forEach(function(f) {
f._cleanData = getFormData(f);
        f._dirty = false;
        f.addEventListener('input', function() { checkDirty(f); });
        f.addEventListener('change', function() { checkDirty(f); });
        });
        if (resetBtn) resetBtn.addEventListener('click', function() {
if (activeForm) resetDirty(activeForm);
        });
        if (saveBtn) saveBtn.addEventListener('click', function() {
if (activeForm) activeForm.requestSubmit();
        });
}

initDirtyBar();

/* ── Edit profile tabs ── */
function switchEpTab(tab, el) {
        document.querySelectorAll('#editProfileModal .nav-link').forEach(function(l) { l.style.color = '#666'; l.style.borderBottomColor = 'transparent'; });
        if (el) { el.style.color = '#2563eb'; el.style.borderBottomColor = '#2563eb'; }
document.getElementById('epActivityContent').classList.toggle('d-none', tab !== 'activity');
        document.getElementById('epWishlistContent').classList.toggle('d-none', tab !== 'wishlist');
        if (tab === 'activity') loadEpActivity();
}

function loadEpActivity() {
        var loading = document.getElementById('epActivityLoading');
        var ordersDiv = document.getElementById('epActivityOrders');
        var reviewsDiv = document.getElementById('epActivityReviews');
        if (!loading || ordersDiv.dataset.loaded) return;
        fetch('/tai-khoan/api/hoat-dong')
        .then(function(r) { return r.json(); })
        .then(function(data) {
        loading.classList.add('d-none');
                if (data.orders && data.orders.length) {
        var html = '<h6 class="fw-bold mb-2" style="font-size:.78rem;color:#999;text-transform:uppercase;">Đơn hàng gần đây</h6>';
                data.orders.forEach(function(o) {
                var statusLabels = {CHO_XAC_NHAN:'Chờ xác nhận', DA_XAC_NHAN:'Đã xác nhận', DANG_GIAO:'Đang giao', DA_GIAO:'Đã giao', DA_HUY:'Đã hủy', DA_HOAN_THANH:'Hoàn thành'};
                        var label = statusLabels[o.trangThaiDon] || o.trangThaiDon;
                        html += '<div class="d-flex align-items-center justify-content-between mb-1 pb-1 border-bottom" style="border-color:#f0f0f0!important;"><div><a href="/tai-khoan/don-hang/' + o.id + '" class="small fw-semibold text-dark text-decoration-none">' + o.maDon + '</a><br><small class="text-muted">' + new Date(o.ngayDat).toLocaleDateString('vi-VN') + '</small></div><span class="small badge bg-light text-dark">' + label + '</span></div>';
                });
                ordersDiv.innerHTML = html;
                ordersDiv.classList.remove('d-none');
                }
        if (data.reviews && data.reviews.length) {
        var html = '<h6 class="fw-bold mb-2 mt-3" style="font-size:.78rem;color:#999;text-transform:uppercase;">Đánh giá gần đây</h6>';
                data.reviews.forEach(function(r) {
                var stars = '★'.repeat(r.danhGia) + '☆'.repeat(5 - r.danhGia);
                        html += '<div class="d-flex align-items-start gap-2 mb-1 pb-1 border-bottom" style="border-color:#f0f0f0!important;"><span class="small text-warning flex-shrink-0">' + stars + '</span><span class="small text-muted flex-grow-1">' + (r.noiDung || '').substring(0, 80) + '</span></div>';
                });
                reviewsDiv.innerHTML = html;
                reviewsDiv.classList.remove('d-none');
                }
        if ((!data.orders || !data.orders.length) && (!data.reviews || !data.reviews.length)) {
        loading.innerHTML = '<div class="text-center py-4 text-muted"><i class="bi bi-inbox" style="font-size:1.5rem;"></i><p class="mt-1 mb-0 small">Chưa có hoạt động</p></div>';
                loading.classList.remove('d-none');
                }
        ordersDiv.dataset.loaded = 'true';
                })
        .catch(function() {
        loading.innerHTML = '<div class="text-center py-4 text-muted small">Không thể tải hoạt động</div>';
                });
}

document.addEventListener('DOMContentLoaded', function() {
        var epModal = document.getElementById('editProfileModal');
        if (epModal) {
epModal.addEventListener('show.bs.modal', function() {
document.getElementById('epActivityOrders').dataset.loaded = '';
        document.getElementById('epActivityOrders').classList.add('d-none');
        document.getElementById('epActivityReviews').classList.add('d-none');
        document.getElementById('epActivityLoading').classList.remove('d-none');
        document.getElementById('epActivityLoading').innerHTML = '<i class="bi bi-arrow-repeat me-1"></i>Đang tải...';
        switchEpTab('activity', document.getElementById('epTabActivity'));
        });
        }
});

/* ── Save nickname from edit profile ── */
function saveNickname() {
        var inp = document.getElementById('epNickname');
        var val = inp ? inp.value.trim() : '';
        var fd = new FormData();
        fd.append('hoTen', document.querySelector('#editProfileModal .fw-bold').textContent);
        fd.append('nickname', val);
        fetch('/tai-khoan/cap-nhat', { method: 'POST', body: fd, redirect: 'follow' })
        .then(function(r) {
        if (r.ok || r.redirected) DuaStore.toast.success('Đã lưu biệt danh');
                else DuaStore.toast.error('Lỗi lưu');
                })
        .catch(function() { DuaStore.toast.error('Lỗi lưu'); });
}

/* ── Status update after AJAX ── */
function applyStatusUI(val) {
        var labels = {ONLINE:'Trực tuyến', AWAY:'Tạm vắng', DND:'Không làm phiền', INVISIBLE:'Ẩn'};
        var map = {ONLINE:'status-online', AWAY:'status-away', DND:'status-dnd', INVISIBLE:'status-invisible'};
        var cls = map[val] || 'status-invisible';
        document.querySelectorAll('.status-dot').forEach(function(d) { d.className = 'status-dot ' + cls; });
        var label = document.getElementById('popupStatusLabel');
        if (label) label.textContent = labels[val] || val;
        var dot = document.querySelector('#profile-popup [class*="border-2"]');
        if (dot) { dot.className = 'position-absolute bottom-0 end-0 border border-2 border-white rounded-circle'; dot.style.background = (val === 'ONLINE' ? '#23a55a' : val === 'AWAY' ? '#f0b232' : val === 'DND' ? '#f23f42' : '#80848e'); }
}

/* ── Settings tabs ── */
document.addEventListener('click', function(e) {
        var btn = e.target.closest('[data-setting-tab]');
        if (!btn) return;
        e.preventDefault();
        document.querySelectorAll('[data-setting-tab]').forEach(function(b) { b.classList.remove('active'); });
        btn.classList.add('active');
        document.querySelectorAll('.settings-tab-content').forEach(function(c) { c.classList.add('d-none'); });
        var target = document.getElementById('settings-' + btn.dataset.settingTab);
        if (target) target.classList.remove('d-none');
});

/* ── Status update ── */
function updateStatus(val) {
        var fd = new FormData();
        fd.append('status', val);
        fetch('/tai-khoan/trang-thai', { method: 'POST', body: fd })
        .then(function(r) { return r.json(); })
        .then(function(data) {
        if (data.success) {
        applyStatusUI(val);
                DuaStore.toast.success('Đã đổi trạng thái');
                }
        });
}

/* ── Avatar upload ── */
function replaceAllAvatars(url) {
        if (!url) return;
        document.querySelectorAll('.ds-profile-avatar, #avatarPreview, #editProfileModal .col-4 .rounded-circle, #avatarUploadModal .rounded-circle').forEach(function(el) {
if (el.tagName === 'IMG') {
el.src = url;
        } else if (el.tagName === 'SPAN' || el.tagName === 'DIV') {
var img = document.createElement('img');
        img.src = url;
        img.alt = 'Avatar';
        img.className = el.className;
        ['rounded-circle', 'object-fit-cover', 'd-inline-flex', 'align-items-center', 'justify-content-center'].forEach(function(c) {
if (el.classList.contains(c)) img.classList.add(c);
        });
        img.style.cssText = el.style.cssText;
        el.parentNode.replaceChild(img, el);
        }
});
}
document.addEventListener('change', function(e) {
        if (e.target.id !== 'avatarInput') return;
        var file = e.target.files[0];
        if (!file) return;
        if (file.size > 5 * 1024 * 1024) { DuaStore.toast.error('File quá lớn, tối đa 5MB'); return; }
if (!['image/jpeg', 'image/png', 'image/webp'].includes(file.type)) { DuaStore.toast.error('Chỉ chấp nhận JPG, PNG, WEBP'); return; }
var canvas = document.createElement('canvas');
        canvas.width = 300; canvas.height = 300;
        var ctx = canvas.getContext('2d');
        var img = new Image();
        img.onload = function() {
        if (img.width < 300 || img.height < 300) { DuaStore.toast.error('Ảnh tối thiểu 300×300px'); return; }
        ctx.drawImage(img, 0, 0, 300, 300);
                canvas.toBlob(function(blob) {
                var fd = new FormData();
                        fd.append('file', blob, 'avatar.jpg');
                        fetch('/tai-khoan/avatar', { method: 'POST', body: fd })
                        .then(function(r) { return r.json(); })
                        .then(function(data) {
                        if (data.success) {
                        replaceAllAvatars(data.avatar);
                                DuaStore.toast.success('Cập nhật ảnh đại diện thành công');
                                bootstrap.Modal.getInstance(document.getElementById('avatarUploadModal'))?.hide();
                        } else {
                        DuaStore.toast.error(data.message);
                        }
                        });
                }, 'image/jpeg', 0.9);
                };
        img.src = URL.createObjectURL(file);
});

document.addEventListener('click', function(e) {
        if (e.target.id !== 'removeAvatarBtn') return;
        if (!confirm('Xóa ảnh đại diện?')) return;
        var fd = new FormData();
        fd.append('remove', 'true');
        fetch('/tai-khoan/avatar', { method: 'POST', body: fd })
        .then(function(r) { return r.json(); })
        .then(function(data) {
        if (data.success) {
        location.reload();
                } else {
        DuaStore.toast.error(data.message);
                }
        });
});

/* ── Linked accounts / Switch account ── */
function loadLinkedAccounts() {
        var container = document.getElementById('linkedAccountsList');
        if (!container) return;
        fetch('/tai-khoan/api/tai-khoan-lien-ket')
        .then(function(r) { return r.json(); })
        .then(function(accounts) {
        if (!accounts || accounts.length === 0) {
        container.innerHTML = '<p class="text-muted small text-center py-3">Chưa có tài khoản liên kết</p>';
                return;
                }
        var html = '';
                accounts.forEach(function(acc) {
                html += '<div class="d-flex align-items-center justify-content-between py-2 border-bottom">';
                        html += '<div><div class="fw-semibold small">' + (acc.hoTen || acc.email) + '</div>';
                        html += '<small class="text-muted">' + (acc.email || '') + '</small></div>';
                        html += '<button class="btn btn-sm btn-outline-primary" onclick="switchToAccount(' + acc.id + ')"><i class="bi bi-arrow-repeat"></i> Chuyển</button>';
                        html += '</div>';
                });
                container.innerHTML = html;
                })
        .catch(function() {
        container.innerHTML = '<p class="text-muted small text-center py-3">Không thể tải danh sách</p>';
                });
}

function switchToAccount(id) {
        fetch('/tai-khoan/chuyen-doi/' + id, { method: 'POST' })
        .then(function(r) { return r.json(); })
        .then(function(data) {
        if (data.success) { location.reload(); }
        else { DuaStore.toast.error(data.message || 'Không thể chuyển đổi'); }
        });
}

document.addEventListener('DOMContentLoaded', function() {
        var switchModal = document.getElementById('switchAccountModal');
        if (switchModal) {
switchModal.addEventListener('show.bs.modal', loadLinkedAccounts);
        }
});

/* ── Link account form ── */
document.addEventListener('submit', function(e) {
        if (e.target.id !== 'linkAccountForm') return;
        e.preventDefault();
        var username = document.getElementById('linkUsername').value.trim();
        var password = document.getElementById('linkPassword').value.trim();
        var errDiv = document.getElementById('linkAccountError');
        if (!username || !password) { errDiv.textContent = 'Vui lòng nhập đầy đủ thông tin'; errDiv.classList.remove('d-none'); return; }
var fd = new FormData();
        fd.append('username', username);
        fd.append('password', password);
        fetch('/tai-khoan/tai-khoan-lien-ket', { method: 'POST', body: fd })
        .then(function(r) { return r.json(); })
        .then(function(data) {
        if (data.success) {
        DuaStore.toast.success('Liên kết tài khoản thành công');
                bootstrap.Modal.getInstance(document.getElementById('linkAccountModal'))?.hide();
                loadLinkedAccounts();
                } else {
        errDiv.textContent = data.message || 'Liên kết thất bại';
                errDiv.classList.remove('d-none');
                }
        });
});

/* ── Logout all devices ── */
function logoutAllDevices() {
        if (!confirm('Đăng xuất khỏi tất cả thiết bị?')) return;
        fetch('/tai-khoan/dang-xuat-tat-ca', { method: 'POST' })
        .then(function(r) { return r.json(); })
        .then(function(data) {
        if (data.success && data.redirect) { window.location.href = data.redirect; }
        else { DuaStore.toast.error('Không thể đăng xuất'); }
        });
}

/* ── Save settings account ── */
function saveSettingsAccount() {
        var fd = new FormData();
        fd.append('hoTen', document.getElementById('sHoTen').value);
        fd.append('nickname', document.getElementById('sNickname').value);
        fd.append('soDienThoai', document.getElementById('sPhone').value);
        fd.append('emailVisible', document.getElementById('sEmailVisible').checked ? 'true' : 'false');
        fd.append('phoneVisible', document.getElementById('sPhoneVisible').checked ? 'true' : 'false');
        fd.append('emailMarketing', document.getElementById('sEmailMarketing').checked ? 'true' : 'false');
        var csrf = document.querySelector('meta[name="_csrf"]');
        if (csrf) fd.append('_csrf', csrf.content);
        fetch('/tai-khoan/cap-nhat', { method: 'POST', body: fd, redirect: 'follow' })
        .then(function(r) {
        if (r.ok || r.redirected) DuaStore.toast.success('Đã lưu thông tin');
                else DuaStore.toast.error('Lỗi lưu');
                })
        .catch(function() { DuaStore.toast.error('Lỗi lưu'); });
}

/* ── Save settings password ── */
function saveSettingsPassword() {
        var oldP = document.getElementById('sOldPassword').value;
        var newP = document.getElementById('sNewPassword').value;
        var confirmP = document.getElementById('sConfirmPassword').value;
        var errDiv = document.getElementById('settingsPassError');
        if (!oldP || !newP || !confirmP) { errDiv.textContent = 'Vui lòng nhập đầy đủ'; errDiv.classList.remove('d-none'); return; }
if (newP.length < 6) { errDiv.textContent = 'Mật khẩu mới tối thiểu 6 ký tự'; errDiv.classList.remove('d-none'); return; }
if (newP !== confirmP) { errDiv.textContent = 'Mật khẩu mới không khớp'; errDiv.classList.remove('d-none'); return; }
var fd = new FormData();
        fd.append('oldPassword', oldP);
        fd.append('newPassword', newP);
        fd.append('confirmPassword', confirmP);
        var csrf = document.querySelector('meta[name="_csrf"]');
        if (csrf) fd.append('_csrf', csrf.content);
        fetch('/tai-khoan/doi-mat-khau', { method: 'POST', body: fd, redirect: 'follow' })
        .then(function(r) {
        if (r.ok || r.redirected) {
        DuaStore.toast.success('Đã đổi mật khẩu');
                document.getElementById('sOldPassword').value = '';
                document.getElementById('sNewPassword').value = '';
                document.getElementById('sConfirmPassword').value = '';
                errDiv.classList.add('d-none');
                } else {
        errDiv.textContent = 'Đổi mật khẩu thất bại';
                errDiv.classList.remove('d-none');
                }
        })
        .catch(function() { errDiv.textContent = 'Lỗi kết nối'; errDiv.classList.remove('d-none'); });
}

/* ── Deactivate account ── */
function deactivateAccount() {
        if (!confirm('Bạn có chắc chắn muốn vô hiệu hóa tài khoản? Hành động này không thể hoàn tác.')) return;
        fetch('/tai-khoan/vo-hieu-hoa', { method: 'POST' })
        .then(function(r) { return r.json(); })
        .then(function(data) {
        if (data.success && data.redirect) { window.location.href = data.redirect; }
        else { DuaStore.toast.error(data.message || 'Không thể vô hiệu hóa'); }
        });
}
</script>

        <div id="dsToast"
     class="position-fixed bottom-0 start-50 translate-middle-x mb-4 px-4 py-2 bg-dark text-white rounded-pill shadow-lg z-3"
     style="transition:opacity .3s;opacity:0;pointer-events:none;z-index:99999;"></div>

        <!-- Save Bar -->
<div id="dsSaveBar" class="ds-save-bar" style="display:none;">
    <div class="ds-save-bar-msg"><i class="bi bi-info-circle-fill"></i> Đừng quên lưu!</div>
    <div class="ds-save-bar-actions">
        <button type="button" class="btn btn-outline-secondary btn-sm" id="dsSaveBarReset">Đặt lại</button>
        <button type="button" class="btn btn-primary btn-sm" id="dsSaveBarSave"><i class="bi bi-floppy me-1"></i>Lưu</button>
    </div>
</div>

<script>
function showToast(msg) {
        var t = document.getElementById('dsToast');
        if (!t) return;
        t.textContent = msg;
        t.style.opacity = '1';
        t.style.pointerEvents = 'auto';
        clearTimeout(t._hide);
        t._hide = setTimeout(function() {
        t.style.opacity = '0';
                t.style.pointerEvents = 'none';
        }, 3000);
}
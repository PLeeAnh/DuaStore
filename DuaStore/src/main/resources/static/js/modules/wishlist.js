/* =====================================================
   DuaStore — Module: Wishlist
===================================================== */
'use strict';

/* ═══ WISHLIST (Toggle) ═══ */
function toggleWishlist(btnElement, productId) {
    var icon = btnElement.querySelector('i');
    var container = document.getElementById('wishlist-items-container');
    var productName = 'Sản phẩm ' + productId;
    var productPrice = 'Đang cập nhật';
    var productImg = '';
    var card = btnElement.closest('.ds-product-card');

    if (card) {
        var nameEl = card.querySelector('.ds-product-name');
        var priceEl = card.querySelector('.ds-price-btn-amount');
        if (nameEl) productName = nameEl.textContent;
        if (priceEl) productPrice = priceEl.textContent;
    } else {
        var detailName = document.querySelector('.product-detail-info h3');
        var detailPrice = document.getElementById('productPrice');
        var detailImg = document.getElementById('mainImage');
        if (detailName) productName = detailName.innerText;
        if (detailPrice) productPrice = detailPrice.innerText;
        if (detailImg) productImg = detailImg.src;
    }

    DuaStore.api.post('/api/wishlist/toggle', { productId: productId }).then(function(result) {
        if (!result.ok) {
            if (result.message && result.message.indexOf('đăng nhập') !== -1) showLoginPopup();
            DuaStore.toast.error(result.message);
            return;
        }
        var data = result.data;
        if (!data.success) {
            if (data.message && data.message.indexOf('dang nhap') !== -1) showLoginPopup();
            return;
        }

        if (btnElement.classList.contains('active')) {
            btnElement.classList.remove('active');
            icon.classList.replace('bi-heart-fill', 'bi-heart');

            var item = document.getElementById('wishlist-item-' + productId);
            if (item) item.remove();

            refreshWishlistBadgeCount();
            DuaStore.toast.success('Đã xóa khỏi yêu thích');

        } else {
            btnElement.classList.add('active');
            icon.classList.replace('bi-heart', 'bi-heart-fill');
            var emptyMsg = container ? container.querySelector('.text-muted.text-center') : null;
            if (emptyMsg) emptyMsg.remove();

            var imgHtml = productImg && productImg.trim()
                ? '<img src="' + productImg + '" class="w-100 h-100 object-fit-cover" alt="SP">'
                : '<i class="bi bi-box-seam text-secondary"></i>';

            if (container) {
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

            refreshWishlistBadgeCount();
            DuaStore.toast.success('Đã thêm vào yêu thích');
        }
    });
}

/* ═══ WISHLIST (Xóa) ═══ */
function removeWishlist(wishlistId) {
    if (window.event) {
        window.event.stopPropagation();
        window.event.preventDefault();
    }

    var item = document.getElementById('wishlist-item-' + wishlistId);
    if (item) item.remove();

    refreshWishlistBadgeCount();

    DuaStore.api.post('/api/wishlist/toggle', { productId: wishlistId }).then(function(result) {
        if (!result.ok) {
            DuaStore.toast.error(result.message);
        }
    });

    document.querySelectorAll('.btn-wishlist-card[onclick*="toggleWishlist(this, ' + wishlistId + ')"], .btn-detail-wishlist[onclick*="toggleWishlist(this, ' + wishlistId + ')"]').forEach(function(btn) {
        btn.classList.remove('active');
        var ic = btn.querySelector('i');
        if (ic) ic.classList.replace('bi-heart-fill', 'bi-heart');
    });
}

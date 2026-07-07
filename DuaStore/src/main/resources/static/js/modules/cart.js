/* =====================================================
 DuaStore — Module: Cart
 ===================================================== */
'use strict';

/* ═══ UPDATE CART BADGE ═══ */
function updateCartBadge(count) {
    var badge = document.getElementById('cartBadge');
    if (!badge)
        return;

    count = Number(count) || 0;

    if (count <= 0) {
        badge.classList.add('d-none');
        return;
    }

    badge.textContent = count > 99 ? '99+' : String(count);
    badge.classList.remove('d-none');
}

/* ═══ POPUP TOGGLE ═══ */
function togglePopup(popupId) {
    document.querySelectorAll('.custom-popup').forEach(function (p) {
        if (p.id !== popupId)
            p.style.display = 'none';
    });

    var popup = document.getElementById(popupId);

    if (popup) {
        popup.style.display =
                popup.style.display === 'block' ? 'none' : 'block';

        if (popupId === 'wishlist-popup' &&
                popup.style.display === 'block') {

            document.getElementById('wishlistBadge')
                    ?.classList.add('d-none');
        }

        if (popupId === 'cart-popup' &&
                popup.style.display === 'block') {

            document.getElementById('cartBadge')
                    ?.classList.add('d-none');

            localStorage.setItem('cartViewed', 'true');
        }
    }
}

document.addEventListener('click', function (e) {
    if (!document.body.contains(e.target))
        return;

    ['wishlist', 'cart', 'profile', 'notif'].forEach(function (type) {
        var btn = document.getElementById('btn-' + type + '-toggle');
        var popup = document.getElementById(type + '-popup');

        if (btn && popup && !btn.contains(e.target) && !popup.contains(e.target)) {
            popup.style.display = 'none';
        }
    });
});

/* ═══ CART (Add to Cart) ═══ */
function addToCart(productId, variantId, quantity) {
    var card = document.querySelector('.ds-product-card[data-productid="' + productId + '"]');
    var btnAdd = card ? card.querySelector('.ds-card-add-cart') : null;
    fetch('/api/cart/add-popup', {
        method: 'POST', headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({productId: productId, variantId: variantId, quantity: quantity})
    }).then(function (r) {
        if (r.status === 403) {
            if (typeof showLoginPopup === 'function')
                showLoginPopup();
            return null;
        }
        return r.json();
    }).then(function (data) {
        if (!data)
            return;
        if (data.success) {
            if (btnAdd)
                btnAdd.classList.add('added');
            if (typeof updateCartBadge === 'function')
                updateCartBadge(data.cartCount);
            if (card) {
                addCartPopupItem(card, productId, variantId, quantity);
            }
            if (typeof DuaStore !== 'undefined' && DuaStore.toast) {
                DuaStore.toast.success('Đã thêm vào giỏ hàng');
            }
        }
    }).catch(function (error) {
        console.error('Lỗi giỏ hàng: ', error);
    });
}

function addToCartFromWishlist(productId, variantId) {
    if (window.event) {
        window.event.stopPropagation();
    }

    fetch('/api/cart/add-popup', {
        method: 'POST', headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({productId: productId, variantId: variantId || null, quantity: 1})
    }).then(function (r) {
        if (r.status === 403) {
            if (typeof showLoginPopup === 'function')
                showLoginPopup();
            return null;
        }
        return r.json();
    }).then(function (data) {
        if (!data)
            return;
        if (data.success) {
            if (typeof updateCartBadge === 'function')
                updateCartBadge(data.cartCount);
            DuaStore.toast.success('Đã thêm vào giỏ hàng');

            var item = document.getElementById('wishlist-item-' + productId);
            var productName = 'Sản phẩm';
            var imgSrc = '';
            var rawPrice = 0;
            if (item) {
                var nameEl = item.querySelector('a');
                var priceEl = item.querySelector('.text-danger');
                var imgEl = item.querySelector('img');
                if (nameEl)
                    productName = nameEl.textContent;
                if (imgEl)
                    imgSrc = imgEl.src;
                if (priceEl)
                    rawPrice = parseInt(priceEl.innerText.replace(/[^\d]/g, '')) || 0;
            }

            var resolvedVariantId = data.variantId || productId;
            var existingEl = document.getElementById('cart-item-' + resolvedVariantId);
            if (existingEl) {
                var qtyInput = existingEl.querySelector('#popup-qty-' + resolvedVariantId);
                if (qtyInput)
                    qtyInput.value = parseInt(qtyInput.value) + 1;
            } else {
                var cartContainer = document.getElementById('cart-items-container');
                if (cartContainer) {
                    var emptyMsg = cartContainer.querySelector('.text-center.text-muted');
                    if (emptyMsg)
                        emptyMsg.remove();
                    var imgHtml = imgSrc ? '<img src="' + imgSrc + '" class="w-100 h-100 object-fit-cover" alt="SP">' : '<i class="bi bi-box-seam text-secondary"></i>';
                    var priceFmt = rawPrice.toLocaleString('vi-VN') + '₫';
                    var html = '<div class="popup-item d-flex align-items-start mb-3 pb-3 border-bottom" id="cart-item-' + resolvedVariantId + '" data-productid="' + productId + '">' +
                            '<div style="width:50px;height:50px;background:#e5e5e5;border-radius:4px;margin-right:12px;display:flex;align-items:center;justify-content:center;overflow:hidden;flex-shrink:0;">' + imgHtml + '</div>' +
                            '<div class="popup-item-info flex-grow-1">' +
                            '<a href="/san-pham/' + productId + '" class="text-truncate d-block text-dark fw-semibold" style="max-width:180px;font-size:0.9rem;">' + productName + '</a>' +
                            '<div class="d-flex align-items-center">' +
                            '<div class="input-group input-group-sm" style="width:85px;">' +
                            '<button class="btn btn-outline-secondary px-2 py-0" onclick="updatePopupQty(' + resolvedVariantId + ',-1)">-</button>' +
                            '<input class="form-control text-center py-0 px-1" type="number" min="1" id="popup-qty-' + resolvedVariantId + '" value="1" />' +
                            '<button class="btn btn-outline-secondary px-2 py-0" onclick="updatePopupQty(' + resolvedVariantId + ',1)">+</button>' +
                            '</div>' +
                            '<span class="text-danger fw-semibold ms-auto popup-item-price" id="popup-price-' + resolvedVariantId + '" data-price="' + rawPrice + '">' + priceFmt + '</span>' +
                            '</div>' +
                            '</div>' +
                            '<button class="btn-delete-item ms-2 text-muted border-0 bg-transparent" onclick="removeCartItem(' + resolvedVariantId + ')"><i class="bi bi-trash text-danger"></i></button>' +
                            '</div>';
                    cartContainer.insertAdjacentHTML('beforeend', html);
                }
            }

            if (!document.querySelector('#cart-popup .mt-2.pt-2')) {
                var popup = document.getElementById('cart-popup');
                if (popup) {
                    var div = document.createElement('div');
                    div.className = 'mt-2 pt-2';
                    div.innerHTML = '<a href="/checkout" class="btn btn-danger w-100 fw-semibold py-2">Thanh toán tất cả</a>';
                    popup.appendChild(div);
                }
            }
        } else {
            if (data.message && data.message.indexOf('dang nhap') !== -1) {
                if (typeof showLoginPopup === 'function')
                    showLoginPopup();
            } else {
                DuaStore.toast.error(data.message || 'Không thể thêm vào giỏ');
            }
        }
    }).catch(function () {
        DuaStore.toast.error('Lỗi kết nối');
    });
}

/* ═══ CART (Add from Card) ═══ */
function addToCartFromCard(btn) {
    if (!btn || btn.disabled)
        return;
    var card = btn.closest('.ds-product-card');
    if (!card)
        return;
    var productId = btn.getAttribute('data-id');
    var qty = parseInt(card.querySelector('.ds-qty-val').value) || 1;
    var activeChip = card.querySelector('.ds-variant-chip.active')
            || card.querySelector('.ds-variant-chip:not(.oos)')
            || card.querySelector('.ds-variant-chip');
    var variantId = activeChip ? parseInt(activeChip.getAttribute('data-variantid')) : null;
    fetch('/api/cart/add-popup', {
        method: 'POST',
        headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({productId: parseInt(productId), variantId: variantId, quantity: qty})
    }).then(function (r) {
        if (r.status === 403) {
            if (typeof showLoginPopup === 'function')
                showLoginPopup();
            return null;
        }
        return r.json();
    }).then(function (data) {
        if (!data)
            return;
        if (data.success) {
            btn.classList.add('added');
            if (typeof updateCartBadge === 'function')
                updateCartBadge(data.cartCount);
            addCartPopupItem(card, productId, variantId, qty);
            if (typeof DuaStore !== 'undefined' && DuaStore.toast) {
                DuaStore.toast.success('Đã thêm vào giỏ hàng');
            }
        } else if (data.message && data.message.indexOf('dang nhap') !== -1) {
            if (typeof showLoginPopup === 'function')
                showLoginPopup();
        } else {
            if (typeof DuaStore !== 'undefined' && DuaStore.toast) {
                DuaStore.toast.error(data.message || 'Không thể thêm vào giỏ');
            }
        }
    }).catch(function (err) {
        console.error('Lỗi thêm giỏ hàng:', err);
        if (typeof DuaStore !== 'undefined' && DuaStore.toast) {
            DuaStore.toast.error('Không thể thêm vào giỏ hàng');
        }
    });
}

/* ═══ CART (Xóa) ═══ */
function removeCartItem(cartItemId) {
    if (window.event) {
        window.event.stopPropagation();
        window.event.preventDefault();
    }

    var item = document.getElementById('cart-item-' + cartItemId);
    var pid = item ? item.getAttribute('data-productid') : null;
    if (item)
        item.remove();

    var container = document.getElementById('cart-items-container');
    if (container && container.querySelectorAll('.popup-item').length === 0) {
        container.innerHTML = '<div class="text-center py-4 text-muted"><i class="bi bi-cart-x" style="font-size:2rem;"></i><p class="mt-2 mb-0">Giỏ hàng trống</p></div>';
        var chk = document.querySelector('#cart-popup .mt-2.pt-2');
        if (chk)
            chk.remove();
    }
    fetch('/api/cart/remove-item', {
        method: 'POST', headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({variantId: cartItemId})
    }).then(function (r) {
        if (r.status === 403) {
            if (typeof showLoginPopup === 'function')
                showLoginPopup();
            return null;
        }
        return r.json();
    }).then(function (data) {
        if (!data)
            return;
        if (data.success) {
            if (typeof updateCartBadge === 'function')
                updateCartBadge(data.cartCount);
            if (data.remainingItems === 0 && pid) {
                document.querySelectorAll('.ds-product-card[data-productid="' + pid + '"]').forEach(function (card) {
                    var b = card.querySelector('.ds-card-add-cart');
                    if (b)
                        b.classList.remove('added');
                });
            }
        } else if (data.message && data.message.indexOf('dang nhap') !== -1) {
            if (typeof showLoginPopup === 'function')
                showLoginPopup();
        } else
            alert('Lỗi hệ thống: ' + data.message);
    }).catch(function (error) {
        console.error('Lỗi xóa giỏ hàng:', error);
    });
}

/* ═══ UPDATE POPUP QTY (+/- số lượng) ═══ */
function updatePopupQty(variantId, delta) {
    if (window.event) {
        window.event.stopPropagation();
        window.event.preventDefault();
    }

    var qtyInput = document.getElementById('popup-qty-' + variantId);
    var priceSpan = document.getElementById('popup-price-' + variantId);
    if (!qtyInput)
        return;
    var cur = parseInt(qtyInput.value) || 1;
    var next = cur + delta;

    if (next < 1) {
        if (confirm('Bạn có chắc chắn muốn xóa sản phẩm này khỏi giỏ hàng?'))
            removeCartItem(variantId);
        return;
    }
    if (next > 99)
        next = 99;

    qtyInput.value = next;
    if (priceSpan) {
        var unit = parseInt(priceSpan.getAttribute('data-price')) || 0;
        priceSpan.innerText = (unit * next).toLocaleString('vi-VN') + '₫';
    }

    fetch('/api/cart/update', {
        method: 'POST', headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({variantId: variantId, quantity: next})
    }).then(function (r) {
        if (r.status === 403) {
            if (typeof showLoginPopup === 'function')
                showLoginPopup();
            return null;
        }
        return r.json();
    }).then(function (data) {
        if (!data)
            return;
        if (data.success) {
            if (typeof updateCartBadge === 'function')
                updateCartBadge(data.cartCount);
        } else if (data.message && data.message.indexOf('dang nhap') !== -1) {
            if (typeof showLoginPopup === 'function')
                showLoginPopup();
            qtyInput.value = cur;
            if (priceSpan) {
                var unit = parseInt(priceSpan.getAttribute('data-price')) || 0;
                priceSpan.innerText = (unit * cur).toLocaleString('vi-VN') + '₫';
            }
        } else {
            alert(data.message || 'Cập nhật thất bại!');
            qtyInput.value = cur;
            if (priceSpan) {
                var unit = parseInt(priceSpan.getAttribute('data-price')) || 0;
                priceSpan.innerText = (unit * cur).toLocaleString('vi-VN') + '₫';
            }
        }
    }).catch(function (err) {
        alert('Lỗi kết nối hệ thống. Vui lòng thử lại!');
        qtyInput.value = cur;
        if (priceSpan) {
            var unit = parseInt(priceSpan.getAttribute('data-price')) || 0;
            priceSpan.innerText = (unit * cur).toLocaleString('vi-VN') + '₫';
        }
        console.error('Lỗi cập nhật SL:', err);
    });
}

/* ═══ SET POPUP QTY (nhập trực tiếp) ═══ */
function setPopupQty(variantId) {
    var qtyInput = document.getElementById('popup-qty-' + variantId);
    if (!qtyInput)
        return;
    var val = parseInt(qtyInput.value) || 1;
    if (val < 1)
        val = 1;
    qtyInput.value = val;
    var priceSpan = document.getElementById('popup-price-' + variantId);
    if (priceSpan) {
        var unit = parseInt(priceSpan.getAttribute('data-price')) || 0;
        priceSpan.innerText = (unit * val).toLocaleString('vi-VN') + '₫';
    }
    fetch('/api/cart/update', {
        method: 'POST', headers: {'Content-Type': 'application/json'},
        body: JSON.stringify({variantId: variantId, quantity: val})
    }).then(function (r) {
        if (r.status === 403) {
            if (typeof showLoginPopup === 'function')
                showLoginPopup();
            return null;
        }
        return r.json();
    }).then(function (data) {
        if (data && data.success && typeof updateCartBadge === 'function')
            updateCartBadge(data.cartCount);
    }).catch(function () {});
}

/* ═══ HELPERS ═══ */
function getActiveVariant(card) {
    return card.querySelector('.ds-variant-chip.active')
            || card.querySelector('.ds-variant-chip:not(.oos)')
            || card.querySelector('.ds-variant-chip');
}

function addCartPopupItem(card, productId, variantId, qty) {
    var container = document.getElementById('cart-items-container');
    if (!container)
        return;
    var emptyMsg = container.querySelector('.text-center.text-muted');
    if (emptyMsg)
        emptyMsg.remove();

    var nameEl = card ? card.querySelector('.ds-product-name-overlay') || card.querySelector('a') : null;
    var productName = nameEl ? nameEl.textContent : 'Sản phẩm ' + productId;
    var imgEl = card ? card.querySelector('.ds-product-img-wrap img') || card.querySelector('img') : null;
    var imgSrc = imgEl ? imgEl.getAttribute('src') || '' : '';
    var imgHtml = imgSrc
            ? '<img src="' + imgSrc + '" class="w-100 h-100 object-fit-cover" alt="SP">'
            : '<i class="bi bi-box-seam text-secondary"></i>';
    var activeChip = card ? getActiveVariant(card) : null;
    var variantName = activeChip ? activeChip.textContent : '';
    if (!variantName && activeChip)
        variantName = activeChip.getAttribute('data-name') || '';
    if (!variantName)
        variantName = 'Mặc định';
    var rawPrice = activeChip ? parseInt(activeChip.getAttribute('data-price')) : 0;
    var priceFmt = rawPrice.toLocaleString('vi-VN') + '₫';

    var existing = document.getElementById('cart-item-' + variantId);
    if (existing) {
        var qtyInput = existing.querySelector('#popup-qty-' + variantId);
        if (qtyInput) {
            var newQty = parseInt(qtyInput.value) + qty;
            qtyInput.value = newQty;
            var ps = existing.querySelector('#popup-price-' + variantId);
            if (ps)
                ps.textContent = (rawPrice * newQty).toLocaleString('vi-VN') + '₫';
        }
        return;
    }

    var html = '<div class="popup-item d-flex align-items-start mb-3 pb-3 border-bottom" id="cart-item-' + variantId + '" data-productid="' + productId + '">' +
            '<div style="width:50px;height:50px;background:#e5e5e5;border-radius:4px;margin-right:12px;display:flex;align-items:center;justify-content:center;overflow:hidden;flex-shrink:0;">' + imgHtml + '</div>' +
            '<div class="popup-item-info flex-grow-1">' +
            '<a href="/san-pham/' + productId + '" class="text-truncate d-block text-dark fw-semibold" style="max-width:180px;font-size:0.9rem;">' + productName + '</a>' +
            '<div class="small text-muted mb-2" style="font-size:0.8rem;">' + variantName + '</div>' +
            '<div class="d-flex align-items-center">' +
            '<div class="input-group input-group-sm" style="width:90px;">' +
            '<button class="btn btn-outline-secondary px-2 py-0" onclick="updatePopupQty(' + variantId + ',-1)">-</button>' +
            '<input class="form-control text-center py-0 px-1" type="number" min="1" id="popup-qty-' + variantId + '" value="' + qty + '" onchange="setPopupQty(' + variantId + ', this)" onfocus="this.select()" style="font-size:0.85rem;" />' +
            '<button class="btn btn-outline-secondary px-2 py-0" onclick="updatePopupQty(' + variantId + ',1)">+</button>' +
            '</div>' +
            '<span class="text-danger fw-semibold ms-auto popup-item-price" id="popup-price-' + variantId + '" data-price="' + rawPrice + '">' + priceFmt + '</span>' +
            '</div>' +
            '</div>' +
            '<button class="btn-delete-item ms-2 text-muted border-0 bg-transparent" onclick="removeCartItem(' + variantId + ')"><i class="bi bi-trash text-danger"></i></button>' +
            '</div>';
    container.insertAdjacentHTML('beforeend', html);

    if (!document.querySelector('#cart-popup .mt-2.pt-2')) {
        var popup = document.getElementById('cart-popup');
        if (popup) {
            var div = document.createElement('div');
            div.className = 'mt-2 pt-2';
            div.innerHTML = '<a href="/checkout" class="btn btn-danger w-100 fw-semibold py-2">Thanh toán tất cả</a>';
            popup.appendChild(div);
        }
    }
    if (typeof togglePopup === 'function')
        togglePopup('cart-popup');
}

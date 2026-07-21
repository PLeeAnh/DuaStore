var _removedItems = {};

function updateQty(variantId, delta) {
    if (typeof DuaStore === 'undefined' || !DuaStore.api || !DuaStore.toast) return;

    var e = event || window.event;
    if (!e || !e.target) return;

    var item = e.target.closest('.ds-cart-item');
    if (!item) return;

    var input = item.querySelector('.ds-qty-val');
    if (!input) return;

    var newQty = parseInt(input.value) + delta;
    if (newQty < 1) return;

    var max = parseInt(input.getAttribute('data-stock')) || 99;
    if (newQty > max) {
        DuaStore.toast.error('Chỉ còn ' + max + ' sản phẩm trong kho');
        return;
    }

    var btns = item.querySelectorAll('.ds-qty-btn');
    btns.forEach(function (b) { b.disabled = true; });

    DuaStore.api.post('/api/cart/update', {
        variantId: variantId,
        soLuong: newQty
    }).then(function (result) {
        btns.forEach(function (b) { b.disabled = false; });

        if (!result.ok) { DuaStore.toast.error(result.message); return; }

        var data = result.data;
        if (!data.success) { DuaStore.toast.error(data.message || 'Cập nhật thất bại'); return; }

        localStorage.setItem('cartViewed', 'true');
        input.value = newQty;
        input.setAttribute('data-max', data.stock || max);

        var unitPriceEl = item.querySelector('.ds-cart-unit-price');
        if (unitPriceEl) {
            var unitPrice = parseInt(unitPriceEl.innerText.replace(/[^\d]/g, '')) || 0;
            var lineTotal = unitPrice * newQty;
            var totalEl = item.querySelector('.ds-cart-total-price');
            if (totalEl) totalEl.innerText = lineTotal.toLocaleString('vi-VN') + '₫';
        }

        if (typeof updateCartBadge === 'function') updateCartBadge(data.cartCount);
        updateSelectedTotal();
        DuaStore.toast.success('Đã cập nhật số lượng');
    });
}

function setQty(variantId, inputEl) {
    if (typeof DuaStore === 'undefined' || !DuaStore.api || !DuaStore.toast) return;

    var max = parseInt(inputEl.getAttribute('data-stock')) || 99;
    var newQty = parseInt(inputEl.value);
    if (isNaN(newQty) || newQty < 1) { newQty = 1; }
    if (newQty > max) { newQty = max; }
    inputEl.value = newQty;

    var item = inputEl.closest('.ds-cart-item');

    DuaStore.api.post('/api/cart/update', {
        variantId: variantId,
        soLuong: newQty
    }).then(function (result) {
        if (!result.ok) { DuaStore.toast.error(result.message); return; }
        var data = result.data;
        if (!data.success) { DuaStore.toast.error(data.message || 'Cập nhật thất bại'); }
        inputEl.value = newQty;

        if (item) {
            var unitPriceEl = item.querySelector('.ds-cart-unit-price');
            if (unitPriceEl) {
                var unitPrice = parseInt(unitPriceEl.innerText.replace(/[^\d]/g, '')) || 0;
                var lineTotal = unitPrice * newQty;
                var totalEl = item.querySelector('.ds-cart-total-price');
                if (totalEl) totalEl.innerText = lineTotal.toLocaleString('vi-VN') + '₫';
            }
        }

        var cartTotal = 0;
        document.querySelectorAll('.ds-cart-total-price').forEach(function (cell) {
            var val = parseInt(cell.innerText.replace(/[^\d]/g, '')) || 0;
            cartTotal += val;
        });
        var formattedTotal = cartTotal.toLocaleString('vi-VN') + '₫';
        var subtotalRow = document.querySelector('.ds-cart-summary-row:first-of-type span:last-child');
        if (subtotalRow) subtotalRow.innerText = formattedTotal;
        var summaryTotal = document.querySelector('.ds-cart-summary-total span:last-child');
        if (summaryTotal) summaryTotal.innerText = formattedTotal;

        updateSelectedTotal();
        if (typeof updateCartBadge === 'function') updateCartBadge(data.cartCount);
        DuaStore.toast.success('Đã cập nhật số lượng');
    });
}

function removeCartItem(id) {
    if (!id) return;
    var itemEl = document.getElementById('cart-item-' + id);
    if (!itemEl) return;

    var name = itemEl.querySelector('.ds-cart-item-name')?.textContent || 'Sản phẩm';
    var variant = itemEl.querySelector('.ds-cart-item-variant')?.textContent || '';
    var price = itemEl.querySelector('.ds-cart-unit-price')?.innerText.replace(/[^\d]/g, '') || '0';
    var qty = itemEl.querySelector('.ds-qty-val')?.value || '1';
    var img = itemEl.querySelector('.ds-cart-image')?.src || '';

    _removedItems[id] = {name: name, variant: variant, price: parseInt(price), qty: parseInt(qty), img: img};

    itemEl.style.opacity = '0.4';
    itemEl.style.pointerEvents = 'none';

    showUndoToast(id, name);

    DuaStore.api.post('/api/cart/remove-item', {variantId: id}).then(function (result) {
        if (!result.ok) {
            itemEl.style.opacity = '1';
            itemEl.style.pointerEvents = 'auto';
            delete _removedItems[id];
            DuaStore.toast.error(result.message);
            return;
        }
        var data = result.data;
        if (data.success) {
            if (typeof updateCartBadge === 'function') updateCartBadge(data.cartCount);
            setTimeout(function () {
                if (_removedItems[id]) {
                    itemEl.remove();
                    delete _removedItems[id];
                    var remaining = document.querySelectorAll('.ds-cart-item');
                    if (remaining.length === 0) window.location.reload();
                    else updateSelectedTotal();
                }
            }, 5000);
        } else {
            itemEl.style.opacity = '1';
            itemEl.style.pointerEvents = 'auto';
            delete _removedItems[id];
            DuaStore.toast.error(data.message || 'Xóa thất bại');
        }
    });
}

function showUndoToast(id, name) {
    var existing = document.querySelector('.ds-undo-toast');
    if (existing) existing.remove();

    var toast = document.createElement('div');
    toast.className = 'ds-undo-toast';
    toast.innerHTML = '<span>Đã xóa <strong>' + name + '</strong></span><button onclick="undoRemove(' + id + ')">Hoàn tác</button>';
    document.body.appendChild(toast);
    setTimeout(function () { toast.remove(); }, 5000);
}

function undoRemove(id) {
    var info = _removedItems[id];
    if (!info) return;
    delete _removedItems[id];

    var toast = document.querySelector('.ds-undo-toast');
    if (toast) toast.remove();

    var itemEl = document.getElementById('cart-item-' + id);
    if (itemEl) {
        itemEl.style.opacity = '1';
        itemEl.style.pointerEvents = 'auto';
    }

    DuaStore.api.post('/api/cart/add', { variantId: id, soLuong: info.qty }).then(function(result) {
        if (result.ok && result.data.success) {
            if (typeof updateCartBadge === 'function') updateCartBadge(result.data.cartCount);
            DuaStore.toast.success('Đã khôi phục ' + info.name);
            updateSelectedTotal();
            if (!itemEl) window.location.reload();
        } else {
            DuaStore.toast.error('Không thể khôi phục sản phẩm');
            if (itemEl) itemEl.remove();
            var remaining = document.querySelectorAll('.ds-cart-item');
            if (remaining.length === 0) window.location.reload();
        }
    });
}

function saveForLater(variantId) {
    if (!variantId) return;
    DuaStore.api.post('/api/cart/save-for-later', {variantId: variantId}).then(function (result) {
        if (!result.ok) { DuaStore.toast.error(result.message); return; }
        var data = result.data;
        if (data.success) {
            if (typeof updateCartBadge === 'function') updateCartBadge(data.cartCount);
            DuaStore.toast.success('Đã lưu để mua sau');
            window.location.reload();
        } else { DuaStore.toast.error('Không thể lưu sản phẩm'); }
    });
}

function moveToCart(savedId) {
    if (!savedId) return;
    DuaStore.api.post('/api/cart/move-to-cart', {savedId: savedId}).then(function (result) {
        if (!result.ok) { DuaStore.toast.error(result.message); return; }
        var data = result.data;
        if (data.success) {
            if (typeof updateCartBadge === 'function') updateCartBadge(data.cartCount);
            DuaStore.toast.success('Đã chuyển vào giỏ hàng');
            window.location.reload();
        } else { DuaStore.toast.error('Sản phẩm đã hết hàng hoặc ngừng bán'); }
    });
}

function removeSaved(savedId) {
    if (!savedId) return;
    DuaStore.api.post('/api/cart/remove-saved', {savedId: savedId}).then(function (result) {
        if (result.ok && result.data.success) {
            var el = document.getElementById('saved-item-' + savedId);
            if (el) el.remove();
            DuaStore.toast.success('Đã xóa khỏi danh sách lưu');
        }
    });
}

function toggleSelectAll(source) {
    document.querySelectorAll('.cart-item-checkbox').forEach(function(cb) {
        cb.checked = source.checked;
    });
    updateSelectedTotal();
}

function updateSelectedTotal() {
    var cartTotal = 0;
    document.querySelectorAll('.cart-item-checkbox:checked').forEach(function(cb) {
        var item = cb.closest('.ds-cart-item');
        if (item) {
            var cell = item.querySelector('.ds-cart-total-price');
            if (cell) {
                var val = parseInt(cell.innerText.replace(/[^\d]/g, '')) || 0;
                cartTotal += val;
            }
        }
    });
    var formattedTotal = cartTotal.toLocaleString('vi-VN') + '₫';
    var subtotalRow = document.querySelector('.ds-cart-summary-row:first-of-type span:last-child');
    if (subtotalRow) subtotalRow.innerText = formattedTotal;
    var summaryTotal = document.querySelector('.ds-cart-summary-total span:last-child');
    if (summaryTotal) summaryTotal.innerText = formattedTotal;
    checkPromoHint(cartTotal);
}

function checkoutSelected() {
    var selected = document.querySelectorAll('.cart-item-checkbox:checked');
    var ids = [];
    selected.forEach(function(cb) { ids.push(cb.getAttribute('data-variant-id')); });
    if (ids.length === 0) {
        if (typeof DuaStore !== 'undefined' && DuaStore.toast) { DuaStore.toast.warning('Vui lòng chọn ít nhất một sản phẩm'); }
        return;
    }
    window.location.href = '/checkout?selected=' + ids.join(',');
}

function checkPromoHint(currentTotal) {
    var hint = document.getElementById('promoHint');
    var text = document.getElementById('promoHintText');
    if (!hint || !text) return;

    var minForFreeShip = 500000;
    var minForPromo = 300000;

    if (currentTotal >= minForFreeShip) { hint.style.display = 'none'; return; }

    var gap = minForFreeShip - currentTotal;
    if (gap > 0 && gap <= 200000) {
        text.textContent = 'Mua thêm ' + gap.toLocaleString('vi-VN') + '₫ để được freeship!';
        hint.style.display = 'flex';
        hint.style.background = '#e8f5e9';
        hint.style.borderColor = '#a5d6a7';
        hint.style.color = '#2e7d32';
    } else if (currentTotal < minForPromo) {
        var promoGap = minForPromo - currentTotal;
        text.textContent = 'Mua thêm ' + promoGap.toLocaleString('vi-VN') + '₫ để nhận ưu đãi!';
        hint.style.display = 'flex';
        hint.style.background = '#fff3cd';
        hint.style.borderColor = '#ffc107';
        hint.style.color = '#856404';
    } else { hint.style.display = 'none'; }
}

document.addEventListener('DOMContentLoaded', function () { updateSelectedTotal(); });

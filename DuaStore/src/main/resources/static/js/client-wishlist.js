function removeWishlist(productId) {
    if (!productId)
        return;
    DuaStore.api.post('/api/wishlist/toggle', {productId: productId}).then(function (result) {
        if (!result.ok) {
            DuaStore.toast.error(result.message);
            return;
        }
        var data = result.data;
        if (data.success) {
            DuaStore.toast.success('\u0110\xe3 x\xf3a kh\u1ecfi danh s\xe1ch y\xeau th\xedch');
            window.location.reload();
        }
    });
}

function addToCartFromWishlist(productId) {
    if (!productId)
        return;
    DuaStore.api.post('/api/cart/add-popup', {productId: productId, quantity: 1}).then(function (result) {
        if (!result.ok) {
            DuaStore.toast.error(result.message);
            return;
        }
        var data = result.data;
        if (data.success) {
            if (typeof updateCartBadge === 'function')
                updateCartBadge(data.cartCount);
            DuaStore.toast.success('\u0110\xe3 th\xeam v\xe0o gi\u1ecf h\xe0ng');
        } else {
            DuaStore.toast.error(data.message || 'Kh\xf4ng th\u1ec3 th\xeam v\xe0o gi\u1ecf');
        }
    });
}

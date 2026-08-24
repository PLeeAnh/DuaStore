/* =====================================================
 DuaStore — Guest Cart
 Khách CHƯA đăng nhập vẫn thêm được vào giỏ hàng (lưu tạm ở localStorage
 CỦA RIÊNG TRÌNH DUYỆT ĐÓ — mỗi khách/trình duyệt có 1 giỏ hàng riêng biệt,
 không thể trùng/lẫn dữ liệu với khách khác vì localStorage không dùng chung).
 Khi đăng nhập xong, giỏ hàng tạm này được gộp (merge) 1 lần vào giỏ hàng
 thật trong DB của tài khoản rồi xóa khỏi localStorage.
 Việc bắt buộc đăng nhập vẫn diễn ra khi khách vào trang giỏ hàng (/gio-hang)
 hoặc thanh toán (/checkout) — chỉ riêng hành động "thêm vào giỏ" là được
 phép làm khi chưa đăng nhập.
 ===================================================== */
'use strict';

window.DuaStore = window.DuaStore || {};

(function () {
    var KEY = 'ds_guest_cart_v1';

    function isLoggedIn() {
        var m = document.querySelector('meta[name="ds-logged-in"]');
        return !!m && m.getAttribute('content') === 'true';
    }

    function readAll() {
        try {
            var raw = localStorage.getItem(KEY);
            var list = raw ? JSON.parse(raw) : [];
            return Array.isArray(list) ? list : [];
        } catch (e) {
            return [];
        }
    }

    function writeAll(list) {
        try {
            localStorage.setItem(KEY, JSON.stringify(list));
        } catch (e) { /* localStorage không khả dụng (chế độ ẩn danh, hết dung lượng...) — bỏ qua an toàn */ }
    }

    function add(variantId, quantity, meta) {
        variantId = parseInt(variantId, 10);
        quantity = parseInt(quantity, 10) || 1;
        if (!variantId) return readAll();
        var list = readAll();
        var existing = null;
        for (var i = 0; i < list.length; i++) {
            if (list[i].variantId === variantId) { existing = list[i]; break; }
        }
        if (existing) {
            existing.quantity = (existing.quantity || 0) + quantity;
            if (meta) Object.assign(existing, meta);
        } else {
            var item = {variantId: variantId, quantity: quantity};
            if (meta) Object.assign(item, meta);
            list.push(item);
        }
        writeAll(list);
        return list;
    }

    function count() {
        return readAll().reduce(function (sum, it) { return sum + (parseInt(it.quantity, 10) || 0); }, 0);
    }

    function clear() {
        try { localStorage.removeItem(KEY); } catch (e) { /* ignore */ }
    }

    /* Gộp giỏ hàng tạm vào giỏ hàng thật ngay sau khi đăng nhập thành công. */
    function merge() {
        var list = readAll();
        if (!list.length) return Promise.resolve(null);
        if (typeof DuaStore === 'undefined' || !DuaStore.api) return Promise.resolve(null);
        var payload = list.map(function (it) { return {variantId: it.variantId, quantity: it.quantity}; });
        return DuaStore.api.post('/api/cart/merge-guest', payload).then(function (result) {
            if (result && result.ok && result.data && result.data.success) {
                clear();
                if (typeof updateCartBadge === 'function') {
                    updateCartBadge(result.data.cartCount, true);
                }
            }
            return result;
        });
    }

    window.DuaStore.guestCart = {
        isLoggedIn: isLoggedIn,
        add: add,
        count: count,
        clear: clear,
        merge: merge,
        readAll: readAll
    };

    document.addEventListener('DOMContentLoaded', function () {
        if (isLoggedIn()) {
            merge();
        } else if (typeof updateCartBadge === 'function') {
            var c = count();
            if (c > 0) updateCartBadge(c, false);
        }
    });
})();

/* =====================================================
 DuaStore — Utility Layer
 DOM shortcuts, debounce, escape helpers
 ===================================================== */
'use strict';

window.DuaStore = window.DuaStore || {};
window.DuaStore.utils = window.DuaStore.utils || {};

(function () {
    window.DuaStore.utils.qs = function (selector, context) {
        return (context || document).querySelector(selector);
    };

    window.DuaStore.utils.qsa = function (selector, context) {
        return (context || document).querySelectorAll(selector);
    };

    window.DuaStore.utils.on = function (el, event, handler, opts) {
        if (el)
            el.addEventListener(event, handler, opts || false);
    };

    window.DuaStore.utils.debounce = function (fn, delay) {
        var timer = null;
        return function () {
            var args = arguments;
            var ctx = this;
            clearTimeout(timer);
            timer = setTimeout(function () {
                fn.apply(ctx, args);
            }, delay || 300);
        };
    };

    window.DuaStore.utils.escapeHtml = function (str) {
        var div = document.createElement('div');
        div.appendChild(document.createTextNode(str));
        return div.innerHTML;
    };

    /* ── Format all .fmt-price elements to VND ── */
    window.DuaStore.utils.formatPrices = function (ctx) {
        var els = (ctx || document).querySelectorAll('.fmt-price');
        for (var i = 0; i < els.length; i++) {
            var num = parseInt(els[i].textContent.replace(/[^0-9]/g, '')) || 0;
            els[i].textContent = num.toLocaleString('vi-VN') + '₫';
        }
    };
})();

/* ── Auto-run formatPrices on DOM ready ── */
document.addEventListener('DOMContentLoaded', function () {
    window.DuaStore.utils.formatPrices();
});

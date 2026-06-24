/* =====================================================
   DuaStore — Utility Layer
   DOM shortcuts, debounce, escape helpers
===================================================== */
'use strict';

window.DuaStore = window.DuaStore || {};
window.DuaStore.utils = window.DuaStore.utils || {};

(function() {
    window.DuaStore.utils.qs = function(selector, context) {
        return (context || document).querySelector(selector);
    };

    window.DuaStore.utils.qsa = function(selector, context) {
        return (context || document).querySelectorAll(selector);
    };

    window.DuaStore.utils.on = function(el, event, handler, opts) {
        if (el) el.addEventListener(event, handler, opts || false);
    };

    window.DuaStore.utils.debounce = function(fn, delay) {
        var timer = null;
        return function() {
            var args = arguments;
            var ctx = this;
            clearTimeout(timer);
            timer = setTimeout(function() {
                fn.apply(ctx, args);
            }, delay || 300);
        };
    };

    window.DuaStore.utils.escapeHtml = function(str) {
        var div = document.createElement('div');
        div.appendChild(document.createTextNode(str));
        return div.innerHTML;
    };
})();

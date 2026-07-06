/* =====================================================
   DuaStore — Toast Manager
   Dependency: style.css (26. TOAST / NOTIFICATION)
===================================================== */
'use strict';

window.DuaStore = window.DuaStore || {};
window.DuaStore.toast = window.DuaStore.toast || {};

(function() {
    var container = null;

    function getContainer() {
        if (!container) {
            container = document.querySelector('.ds-toast-container');
            if (!container) {
                container = document.createElement('div');
                container.className = 'ds-toast-container';
                document.body.appendChild(container);
            }
        }
        return container;
    }

    var icons = {
        success: 'bi-check-circle-fill',
        error:   'bi-x-circle-fill',
        warning: 'bi-exclamation-triangle-fill',
        info:    'bi-info-circle-fill'
    };

    function show(type, message, duration) {
        duration = duration || 3000;
        var c = getContainer();

        var el = document.createElement('div');
        el.className = 'ds-toast ds-toast-' + type;

        var icon = document.createElement('i');
        icon.className = 'bi ' + (icons[type] || icons.info);
        el.appendChild(icon);

        var span = document.createElement('span');
        span.textContent = message;
        el.appendChild(span);

        var closeBtn = document.createElement('button');
        closeBtn.className = 'ds-toast-close';
        closeBtn.innerHTML = '&times;';
        el.appendChild(closeBtn);

        c.appendChild(el);

        var dismissed = false;
        function dismiss() {
            if (dismissed) return;
            dismissed = true;
            el.style.animation = 'ds-toast-fade-out .3s ease forwards';
            el.addEventListener('animationend', function() {
                if (el.parentNode) el.parentNode.removeChild(el);
            });
        }

        closeBtn.addEventListener('click', dismiss);

        if (duration > 0) {
            setTimeout(dismiss, duration);
        }
    }

    window.DuaStore.toast.success = function(msg, d) { show('success', msg, d); };
    window.DuaStore.toast.error   = function(msg, d) { show('error',   msg, d); };
    window.DuaStore.toast.warning = function(msg, d) { show('warning', msg, d); };
    window.DuaStore.toast.info    = function(msg, d) { show('info',    msg, d); };
})();

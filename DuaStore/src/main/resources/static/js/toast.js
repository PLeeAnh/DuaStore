/* =====================================================
 DuaStore — Toast Manager
 Dependency: style.css (26. TOAST / NOTIFICATION)
 ===================================================== */
'use strict';

window.DuaStore = window.DuaStore || {};
window.DuaStore.toast = window.DuaStore.toast || {};

(function () {
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
        error: 'bi-x-circle-fill',
        warning: 'bi-exclamation-triangle-fill',
        info: 'bi-info-circle-fill'
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
            if (dismissed)
                return;
            dismissed = true;
            el.style.animation = 'ds-toast-fade-out .3s ease forwards';
            el.addEventListener('animationend', function () {
                if (el.parentNode)
                    el.parentNode.removeChild(el);
            });
        }

        closeBtn.addEventListener('click', dismiss);

        if (duration > 0) {
            setTimeout(dismiss, duration);
        }
    }

    window.DuaStore.toast.success = function (msg, d) {
        show('success', msg, d);
    };
    window.DuaStore.toast.error = function (msg, d) {
        show('error', msg, d);
    };
    window.DuaStore.toast.warning = function (msg, d) {
        show('warning', msg, d);
    };
    window.DuaStore.toast.info = function (msg, d) {
        show('info', msg, d);
    };
})();

/* ── DuaStore.confirm — Promise-based Bootstrap modal ── */
(function () {
    var confirmEl = null;
    var bsModal = null;
    var pendingReject = null;
    var pendingResolve = null;

    function createConfirmEl() {
        if (confirmEl) return;
        confirmEl = document.createElement('div');
        confirmEl.className = 'modal fade';
        confirmEl.setAttribute('tabindex', '-1');
        confirmEl.setAttribute('aria-hidden', 'true');
        confirmEl.style.zIndex = '1056';
        confirmEl.innerHTML =
            '<div class="modal-dialog modal-dialog-centered modal-sm">' +
            '  <div class="modal-content border-0 shadow-sm" style="border-radius:12px;">' +
            '    <div class="modal-header border-0 pb-0">' +
            '      <h6 class="modal-title fw-bold" id="duastoreConfirmTitle">Xác nhận</h6>' +
            '      <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Đóng"></button>' +
            '    </div>' +
            '    <div class="modal-body pt-2 pb-3">' +
            '      <p class="mb-0 small" id="duastoreConfirmMessage"></p>' +
            '      <div class="form-check mb-0">' +
            '        <input class="form-check-input" type="checkbox" id="duastoreConfirmDontShow">' +
            '        <label class="form-check-label small text-muted" for="duastoreConfirmDontShow">' +
            '          Không hiển thị giao diện này nữa' +
            '        </label>' +
            '      </div>' +
            '    </div>' +
            '    <div class="modal-footer border-0 pt-0 d-flex gap-2 justify-content-center">' +
            '      <button type="button" class="ds-btn ds-btn-outline ds-btn-sm flex-fill" id="duastoreConfirmCancel">Huỷ</button>' +
            '      <button type="button" class="ds-btn ds-btn-fill ds-btn-sm flex-fill" id="duastoreConfirmOk">Đồng ý</button>' +
            '    </div>' +
            '  </div>' +
            '</div>';
        document.body.appendChild(confirmEl);
        if (typeof bootstrap !== 'undefined' && bootstrap.Modal) {
            bsModal = new bootstrap.Modal(confirmEl, { backdrop: 'static', keyboard: false });
        }
        function cleanup(result) {
            if (pendingResolve) { pendingResolve(result); pendingResolve = null; }
            if (bsModal) bsModal.hide();
            if (confirmEl && confirmEl.parentNode) document.body.removeChild(confirmEl);
            confirmEl = null;
            bsModal = null;
        }
        confirmEl.addEventListener('hidden.bs.modal', function () { cleanup(false); });
        document.getElementById('duastoreConfirmCancel').addEventListener('click', function () { cleanup(false); });
        document.getElementById('duastoreConfirmOk').addEventListener('click', function () { 
            var chk = document.getElementById('duastoreConfirmDontShow');
            if (chk && chk.checked) {
                var confirmKey = 'default';
                sessionStorage.setItem('ds_confirm_dont_show_' + confirmKey, 'true');
            }
            cleanup(true); 
        });
    }

    window.DuaStore.confirm = function (message) {
        createConfirmEl();
        document.getElementById('duastoreConfirmMessage').textContent = message;
        
        // Check if user has checked "don't show again" for this message
        var confirmKey = 'default';
        if (sessionStorage.getItem('ds_confirm_dont_show_' + confirmKey) === 'true') {
            return Promise.resolve(true);
        }
        
        // Reset checkbox
        var chk = document.getElementById('duastoreConfirmDontShow');
        if (chk) chk.checked = false;
        
        if (bsModal) bsModal.show();
        return new Promise(function (resolve) { pendingResolve = resolve; });
    };
})();

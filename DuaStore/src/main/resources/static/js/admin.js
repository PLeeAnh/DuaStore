/* =====================================================
 DuaStore — admin.js
 ===================================================== */

'use strict';
(function () {
    const token = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
    const header = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
    if (!token || !header)
        return;
    const orig = window.fetch;
    window.fetch = function (url, opts) {
        opts = opts || {};
        if (!opts.method || opts.method.toUpperCase() === 'GET')
            return orig.call(this, url, opts);
        const isSameOrigin = typeof url === 'string' && (url.startsWith('/') || new URL(url, location.origin).origin === location.origin);
        if (isSameOrigin) {
            opts.headers = opts.headers || {};
            if (opts.headers instanceof Headers) {
                opts.headers.set(header, token);
            } else {
                opts.headers[header] = token;
            }
        }
        return orig.call(this, url, opts);
    };
})();

document.addEventListener('DOMContentLoaded', () => {

    /* ── Sidebar toggle ── */
    const toggle = document.getElementById('admNavToggle');
    const sidebar = document.querySelector('.adm-sidebar');
    const icon = document.getElementById('admToggleIcon');

    if (toggle && sidebar) {
        function isMobile() {
            return window.innerWidth <= 991;
        }

        toggle.addEventListener('click', (e) => {
            e.stopPropagation();
            const isOpen = sidebar.classList.toggle('adm-sidebar-mobile-open');
            toggle.setAttribute('aria-label', isOpen ? 'Đóng menu' : 'Mở menu');
            if (icon) {
                icon.className = isOpen ? 'bi bi-x' : 'bi bi-list';
            }
            if (isMobile()) {
                document.body.style.overflow = isOpen ? 'hidden' : '';
            }
        });

        const mainArea = document.querySelector('.adm-main');
        if (mainArea) {
            mainArea.addEventListener('click', () => {
                if (isMobile() && sidebar.classList.contains('adm-sidebar-mobile-open')) {
                    sidebar.classList.remove('adm-sidebar-mobile-open');
                    document.body.style.overflow = '';
                    if (icon)
                        icon.className = 'bi bi-list';
                }
            });
        }

        window.addEventListener('resize', () => {
            if (!isMobile() && sidebar.classList.contains('adm-sidebar-mobile-open')) {
                sidebar.classList.remove('adm-sidebar-mobile-open');
                document.body.style.overflow = '';
                if (icon)
                    icon.className = 'bi bi-list';
            }
        });
    }

    /* ── Profile menu ── */
    const admTrigger = document.getElementById('admProfileTrigger');
    const admDropdown = document.getElementById('admProfileDropdown');
    if (admTrigger && admDropdown) {
        admTrigger.addEventListener('click', (e) => {
            e.stopPropagation();
            const isOpen = admDropdown.classList.toggle('open');
            admTrigger.setAttribute('aria-expanded', isOpen);
        });
        document.addEventListener('click', (e) => {
            if (!admTrigger.contains(e.target) && !admDropdown.contains(e.target)) {
                admDropdown.classList.remove('open');
                admTrigger.setAttribute('aria-expanded', 'false');
            }
        });
    }

    /* ── Toast notification ── */
    var toastTriggers = document.querySelectorAll('[data-toast-msg]');
    var toastContainer = document.getElementById('toastContainer');
    toastTriggers.forEach(function (el) {
        var msg = el.getAttribute('data-toast-msg');
        var type = el.getAttribute('data-toast-type') || 'success';
        var icons = {success: 'bi-check-circle-fill text-success', error: 'bi-x-circle-fill text-danger', warning: 'bi-exclamation-triangle-fill text-warning'};
        var titles = {success: 'Thành công', error: 'Lỗi', warning: 'Cảnh báo'};
        var toastId = 'toast-' + Date.now() + '-' + Math.random().toString(36).slice(2, 6);
        if (toastContainer) {
            toastContainer.insertAdjacentHTML('beforeend',
                    '<div id="' + toastId + '" class="toast" role="alert" data-bs-delay="4000">' +
                    '<div class="toast-header">' +
                    '<i class="bi ' + (icons[type] || icons.success) + ' me-2"></i>' +
                    '<strong class="me-auto">' + (titles[type] || titles.success) + '</strong>' +
                    '<button type="button" class="btn-close" data-bs-dismiss="toast"></button>' +
                    '</div>' +
                    '<div class="toast-body">' + msg + '</div>' +
                    '</div>');
            var toastEl = document.getElementById(toastId);
            if (toastEl) {
                var toast = bootstrap.Toast.getOrCreateInstance(toastEl);
                toast.show();
                toastEl.addEventListener('hidden.bs.toast', function () {
                    toastEl.remove();
                });
            }
        }
        el.remove();
    });

    /* ── Confirm xóa (Bootstrap Modal) ── */
    let confirmForm = null;
    const confirmModalEl = document.getElementById('confirmModal');
    if (confirmModalEl) {
        const confirmModal = new bootstrap.Modal(confirmModalEl);

        function bindDataConfirm(btn) {
            btn.removeEventListener('click', handleDataConfirm);
            btn.addEventListener('click', handleDataConfirm);
        }
        function handleDataConfirm(e) {
            var btn = e.currentTarget;
            var msg = btn.getAttribute('data-confirm') || 'Xác nhận thực hiện thao tác này?';
            confirmForm = btn.closest('form');
            if (!confirmForm) return;
            e.preventDefault();
            document.getElementById('confirmModalMessage').textContent = msg;
            confirmModal.show();
        }

        // Intercept native confirm() on form submissions → use modal instead
        document.querySelectorAll('form[onsubmit]').forEach(function(form) {
            var original = form.getAttribute('onsubmit');
            if (original && original.includes('confirm(')) {
                form.removeAttribute('onsubmit');
                var btn = form.querySelector('[type="submit"]');
                if (btn) {
                    btn.setAttribute('data-confirm', original.match(/confirm\(['"](.+?)['"]\)/)?.[1] || 'Xác nhận thực hiện?');
                    bindDataConfirm(btn);
                }
            }
        });

        // Bind existing [data-confirm] buttons
        document.querySelectorAll('[data-confirm]').forEach(bindDataConfirm);

        document.getElementById('confirmModalConfirm').addEventListener('click', () => {
            if (confirmForm) {
                confirmForm.submit();
                confirmForm = null;
            }
            confirmModal.hide();
        });
        confirmModalEl.addEventListener('hidden.bs.modal', () => {
            confirmForm = null;
        });
    }

    /* ── Auto-submit search (debounce) ── */
    document.querySelectorAll('[data-autosubmit]').forEach(input => {
        const delay = parseInt(input.getAttribute('data-autosubmit'), 10) || 300;
        let timer;
        input.addEventListener('input', () => {
            clearTimeout(timer);
            timer = setTimeout(() => {
                const form = input.closest('form');
                if (form)
                    form.submit();
            }, delay);
        });

    });

    function copyPromoCode(btn) {
        var code = btn.getAttribute('data-code');
        if (navigator.clipboard) {
            navigator.clipboard.writeText(code).then(function () {
                var orig = btn.textContent;
                btn.textContent = 'Copied!';
                setTimeout(function () {
                    btn.textContent = orig;
                }, 1500);
            });
        } else {
            var ta = document.createElement('textarea');
            ta.value = code;
            document.body.appendChild(ta);
            ta.select();
            document.execCommand('copy');
            document.body.removeChild(ta);
            var orig = btn.textContent;
            btn.textContent = 'Copied!';
            setTimeout(function () {
                btn.textContent = orig;
            }, 1500);
        }
    }
    /* ── Searchable Select (TomSelect) ── */
    document.querySelectorAll('.searchable-select').forEach(el => {
        if (el.tagName !== 'SELECT')
            return;
        const opts = {
            placeholder: el.getAttribute('placeholder') || 'Tìm kiếm...',
            maxOptions: null,
        };
        if (el.hasAttribute('data-create')) {
            opts.create = true;
            opts.createOnBlur = true;
        }
        if (el.hasAttribute('data-autosubmit')) {
            opts.onChange = function () {
                var form = el.closest('form');
                if (form) {
                    form.submit();
                } else {
                    el.dispatchEvent(new Event('change', {bubbles: true}));
                }
            };
        }
        new TomSelect(el, opts);
    });

    /* ── Dirty Save Bar ── */
    function initDirtyBar() {
        var bar = document.getElementById('dsSaveBar');
        if (!bar)
            return;
        var forms = document.querySelectorAll('form[data-dirty-bar]');
        if (!forms.length)
            return;
        var activeForm = null;
        var resetBtn = document.getElementById('dsSaveBarReset');
        var saveBtn = document.getElementById('dsSaveBarSave');
        var fields = 'input, select, textarea';

        function getFormData(f) {
            return new FormData(f);
        }

        function checkDirty(f) {
            if (!f._cleanData) {
                f._cleanData = getFormData(f);
                f._dirty = false;
                return;
            }
            var current = getFormData(f);
            var dirty = false;
            var keys = new Set();
            for (var pair of f._cleanData.entries())
                keys.add(pair[0]);
            for (var pair of current.entries())
                keys.add(pair[0]);
            keys.forEach(function (k) {
                var v1 = f._cleanData.getAll(k).sort().join(',');
                var v2 = current.getAll(k).sort().join(',');
                if (v1 !== v2)
                    dirty = true;
            });
            if (dirty !== f._dirty) {
                f._dirty = dirty;
                updateBar(dirty, f);
            }
        }

        function updateBar(dirty, f) {
            if (dirty) {
                activeForm = f;
                bar.style.display = 'flex';
                requestAnimationFrame(function () {
                    bar.classList.add('show');
                });
            } else {
                bar.classList.remove('show');
                activeForm = null;
                setTimeout(function () {
                    bar.style.display = 'none';
                }, 300);
            }
        }

        function resetDirty(f) {
            f.reset();
            setTimeout(function () {
                f._cleanData = getFormData(f);
                f._dirty = false;
                updateBar(false, f);
            }, 50);
        }

        forms.forEach(function (f) {
            f._cleanData = getFormData(f);
            f._dirty = false;
            f.addEventListener('input', function () {
                checkDirty(f);
            });
            f.addEventListener('change', function () {
                checkDirty(f);
            });
        });

        if (resetBtn)
            resetBtn.addEventListener('click', function () {
                if (activeForm)
                    resetDirty(activeForm);
            });

        if (saveBtn)
            saveBtn.addEventListener('click', function () {
                if (activeForm)
                    activeForm.requestSubmit();
            });
    }

    initDirtyBar();
    const treeRoot = document.querySelector('.category-tree');
    if (treeRoot) {
        treeRoot.addEventListener('click', function (e) {
            const toggle = e.target.closest('.tree-toggle');
            if (toggle && toggle.hasAttribute('data-target')) {
                e.stopPropagation();
                const targetId = toggle.getAttribute('data-target');
                const el = document.getElementById(targetId);
                if (el) {
                    el.classList.toggle('d-none');
                    toggle.classList.toggle('collapsed');
                }
            }
        });
    }

});

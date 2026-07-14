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
            el._tsInit = true;
            opts.onChange = function () {
                if (el._tsInit) { el._tsInit = false; return; }
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

    // ── expose reinit for AJAX navigation ──
    window.__reinitAdminComponents = function () {
        /* Toast */
        document.querySelectorAll('[data-toast-msg]').forEach(function (el) {
            var msg = el.getAttribute('data-toast-msg');
            var type = el.getAttribute('data-toast-type') || 'success';
            var icons = {success: 'bi-check-circle-fill text-success', error: 'bi-x-circle-fill text-danger', warning: 'bi-exclamation-triangle-fill text-warning'};
            var titles = {success: 'Thành công', error: 'Lỗi', warning: 'Cảnh báo'};
            var toastId = 'toast-' + Date.now() + '-' + Math.random().toString(36).slice(2, 6);
            var tc = document.getElementById('toastContainer');
            if (tc) {
                tc.insertAdjacentHTML('beforeend',
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
                    toastEl.addEventListener('hidden.bs.toast', function () { toastEl.remove(); });
                }
            }
            el.remove();
        });

        /* Confirm modal — re-bind [data-confirm] */
        var cmEl = document.getElementById('confirmModal');
        if (cmEl) {
            window.__confirmForm = null;
            document.querySelectorAll('[data-confirm]').forEach(function (btn) {
                btn.onclick = function (e) {
                    e.preventDefault();
                    window.__confirmForm = btn.closest('form');
                    document.getElementById('confirmModalMessage').textContent = btn.getAttribute('data-confirm');
                    if (window.__confirmModal) {
                        window.__confirmModal.show();
                    } else {
                        window.__confirmModal = new bootstrap.Modal(cmEl);
                        window.__confirmModal.show();
                    }
                };
            });
            document.getElementById('confirmModalConfirm').onclick = function () {
                if (window.__confirmForm) { window.__confirmForm.submit(); window.__confirmForm = null; }
                if (window.__confirmModal) window.__confirmModal.hide();
            };
        }

        /* Auto-submit */
        document.querySelectorAll('[data-autosubmit]').forEach(function (input) {
            if (input._reinitBound) return;
            input._reinitBound = true;
            var delay = parseInt(input.getAttribute('data-autosubmit'), 10) || 300;
            var timer;
            input.addEventListener('input', function () {
                clearTimeout(timer);
                timer = setTimeout(function () {
                    var form = input.closest('form');
                    if (form) form.submit();
                }, delay);
            });
        });

        /* TomSelect */
        document.querySelectorAll('.searchable-select').forEach(function (el) {
            if (el.tagName !== 'SELECT' || el.tomselect) return;
            var opts = {
                placeholder: el.getAttribute('placeholder') || 'Tìm kiếm...',
                maxOptions: null,
            };
            if (el.hasAttribute('data-create')) { opts.create = true; opts.createOnBlur = true; }
            if (el.hasAttribute('data-autosubmit')) {
                el._tsInit = true;
                opts.onChange = function () {
                    if (el._tsInit) { el._tsInit = false; return; }
                    var form = el.closest('form');
                    if (form) form.submit();
                    else el.dispatchEvent(new Event('change', {bubbles: true}));
                };
            }
            new TomSelect(el, opts);
        });

        /* Dirty bar (simple: rebind change listeners on [data-dirty-bar] forms) */
        var dsBar = document.getElementById('dsSaveBar');
        if (dsBar) {
            document.querySelectorAll('form[data-dirty-bar]').forEach(function (f) {
                if (f._dsRebound) return;
                f._dsRebound = true;
                f._cleanData = new FormData(f);
                f._dirty = false;
                function getFD(f) { return new FormData(f); }
                function updBar(dirty, f) {
                    if (dirty) {
                        window.__activeForm = f;
                        dsBar.style.display = 'flex';
                        requestAnimationFrame(function () { dsBar.classList.add('show'); });
                    } else {
                        dsBar.classList.remove('show');
                        window.__activeForm = null;
                        setTimeout(function () { dsBar.style.display = 'none'; }, 300);
                    }
                }
                function chkDirty(f) {
                    var cur = getFD(f);
                    var dirty = false;
                    var keys = new Set();
                    for (var pair of f._cleanData.entries()) keys.add(pair[0]);
                    for (var pair of cur.entries()) keys.add(pair[0]);
                    keys.forEach(function (k) {
                        var v1 = f._cleanData.getAll(k).sort().join(',');
                        var v2 = cur.getAll(k).sort().join(',');
                        if (v1 !== v2) dirty = true;
                    });
                    if (dirty !== f._dirty) { f._dirty = dirty; updBar(dirty, f); }
                }
                f.addEventListener('input', function () { chkDirty(f); });
                f.addEventListener('change', function () { chkDirty(f); });
            });
            document.getElementById('dsSaveBarReset').onclick = function () {
                if (window.__activeForm) {
                    window.__activeForm.reset();
                    setTimeout(function () {
                        window.__activeForm._cleanData = new FormData(window.__activeForm);
                        window.__activeForm._dirty = false;
                        updBar(false, window.__activeForm);
                    }, 50);
                }
            };
            document.getElementById('dsSaveBarSave').onclick = function () {
                if (window.__activeForm) window.__activeForm.requestSubmit();
            };
        }
    };
});

/* ══════════════════════════════════════════════════════════════
   AJAX Navigation — sidebar clicks load main content only
   ══════════════════════════════════════════════════════════════ */
(function () {
    var sidebar = document.querySelector('.adm-sidebar');
    var contentArea = document.querySelector('.adm-content');
    if (!sidebar || !contentArea) return;

    function execScripts(container) {
        container.querySelectorAll('script').forEach(function (old) {
            var s = document.createElement('script');
            for (var i = 0; i < old.attributes.length; i++) {
                s.setAttribute(old.attributes[i].name, old.attributes[i].value);
            }
            s.textContent = old.textContent;
            old.parentNode.replaceChild(s, old);
        });
    }

    function getExtraElements(doc) {
        var body = doc.body;
        var extra = [];
        var foundCommon = false;
        for (var i = 0; i < body.children.length; i++) {
            var child = body.children[i];
            if (child.tagName === 'SCRIPT' && child.getAttribute('src')) {
                var src = child.getAttribute('src');
                if (src.indexOf('admin-base.js') !== -1 || src.indexOf('admin.js') !== -1) {
                    foundCommon = true;
                    continue;
                }
            }
            if (foundCommon) {
                var cls = child.className || '';
                var id = child.id || '';
                if (cls.indexOf('adm-footer') !== -1 || id === 'toastContainer' || id === 'dsSaveBar' || id === 'confirmModal' || id === 'adminProfileModal' || id === 'adminChangePasswordModal') continue;
                extra.push(child);
            }
        }
        return extra;
    }

    function loadAdminPage(url, clickedLink) {
        contentArea.style.opacity = '0.4';
        contentArea.style.pointerEvents = 'none';

        fetch(url, { headers: { 'X-Requested-With': 'XMLHttpRequest' } })
            .then(function (r) { if (!r.ok) throw new Error('HTTP ' + r.status); return r.text(); })
            .then(function (html) {
                var doc = new DOMParser().parseFromString(html, 'text/html');

                // Replace main content
                var newContent = doc.querySelector('.adm-content');
                if (newContent) contentArea.innerHTML = newContent.innerHTML;

                // Replace breadcrumb
                var bc = doc.querySelector('.breadcrumb');
                var curBc = document.querySelector('.breadcrumb');
                if (bc && curBc) curBc.innerHTML = bc.innerHTML;

                // Update title
                document.title = doc.title;

                // Update sidebar active state
                var links = sidebar.querySelectorAll('.adm-nav-link');
                for (var j = 0; j < links.length; j++) links[j].classList.remove('active');
                if (clickedLink) {
                    clickedLink.classList.add('active');
                    // Auto-expand parent sub-menu if collapsed
                    var parentSub = clickedLink.closest('.adm-nav-sub');
                    if (parentSub && !parentSub.classList.contains('open')) {
                        parentSub.classList.add('open');
                        var prevHeader = parentSub.previousElementSibling;
                        if (prevHeader && prevHeader.classList.contains('adm-nav-section')) {
                            prevHeader.classList.add('open');
                        }
                    }
                }

                // Push URL to history
                history.pushState({ url: url }, '', url);

                // Remove old extra content, add new extra content
                document.querySelectorAll('[data-ajax-extra]').forEach(function (el) { el.remove(); });
                var extraEls = getExtraElements(doc);
                for (var k = 0; k < extraEls.length; k++) {
                    var clone = extraEls[k].cloneNode(true);
                    clone.setAttribute('data-ajax-extra', 'true');
                    document.body.appendChild(clone);
                }

                // Execute scripts in content area
                execScripts(contentArea);

                // Execute scripts in new extra content
                document.querySelectorAll('[data-ajax-extra] script').forEach(function (old) {
                    var s = document.createElement('script');
                    for (var i = 0; i < old.attributes.length; i++) {
                        s.setAttribute(old.attributes[i].name, old.attributes[i].value);
                    }
                    s.textContent = old.textContent;
                    old.parentNode.replaceChild(s, old);
                });

                // Re-init admin components
                if (window.__reinitAdminComponents) window.__reinitAdminComponents();

                contentArea.style.opacity = '';
                contentArea.style.pointerEvents = '';
            })
            .catch(function (err) {
                console.warn('AJAX load failed, falling back to full navigation:', err);
                window.location.href = url;
            });
    }

    sidebar.addEventListener('click', function (e) {
        var link = e.target.closest('a.adm-nav-link');
        if (!link) return;
        var href = link.getAttribute('href');
        if (!href || href === '#' || href === '' || href.indexOf('http') === 0) return;
        // Only intercept links starting with /admin (excludes "Về trang chủ" → /)
        if (href.indexOf('/admin') !== 0) return;
        // Allow ctrl/meta/middle-click for new tab
        if (e.ctrlKey || e.metaKey || e.button === 1) return;
        e.preventDefault();
        loadAdminPage(href, link);
    });

    window.addEventListener('popstate', function (e) {
        if (e.state && e.state.url) {
            window.location.href = e.state.url;
        }
    });
})();

/* ── Global: copy promo code ── */
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

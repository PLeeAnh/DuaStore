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

    /* ── Global toast helper ── */
    window.dsToast = function (type, msg) {
        var icons = {success: 'bi-check-circle-fill', error: 'bi-x-circle-fill', warning: 'bi-exclamation-triangle-fill', info: 'bi-info-circle-fill'};
        var tc = document.getElementById('toastContainer');
        if (!tc) return;
        var el = document.createElement('div');
        el.className = 'ds-toast ds-toast-' + type;
        el.innerHTML = '<i class="bi ' + (icons[type] || icons.success) + '"></i><span>' + msg + '</span><button class="ds-toast-close">&times;</button>';
        tc.appendChild(el);
        el.querySelector('.ds-toast-close').addEventListener('click', function () { el.remove(); });
        setTimeout(function () {
            if (el.parentNode) {
                el.style.animation = 'ds-toast-fade-out .3s ease forwards';
                setTimeout(function () { el.remove(); }, 300);
            }
        }, 3000);
    };

    /* ── Toast sau reload (sessionStorage) ── */
    window.__dsToastAfterReload = function (msg) {
        try { sessionStorage.setItem('dsPendingToast', msg); } catch (e) { }
    };
    (function () {
        try {
            var pending = sessionStorage.getItem('dsPendingToast');
            if (pending) {
                sessionStorage.removeItem('dsPendingToast');
                dsToast('success', pending);
            }
        } catch (e) { }
    })();

    /* ── Submit form qua AJAX kèm toast mặc định nếu controller không flash ── */
    window.__dsConfirmForm = null;
    function dsSubmitFormWithFeedback(f) {
        if (!f) return;
        window.__dsPendingDefaultToast = f.getAttribute('data-success-msg') || 'Thao tác thành công';
        var fd = new FormData(f);
        var url = f.getAttribute('action');
        var method = f.getAttribute('method') || 'post';
        fetch(url, { method: method.toUpperCase(), body: fd, headers: { 'X-Requested-With': 'XMLHttpRequest' } })
            .then(function (r) {
                if (r.redirected) {
                    var finalUrl = r.url;
                    if (finalUrl && finalUrl !== window.location.href) history.pushState({ url: finalUrl }, '', finalUrl);
                    return r.text().then(window.__handleResponseHtml);
                }
                if (!r.ok) throw new Error('HTTP ' + r.status);
                return r.text();
            })
            .then(function (html) {
                if (html) window.__handleResponseHtml(html);
            })
            .catch(function () {
                window.__dsPendingDefaultToast = null;
                if (typeof dsToast === 'function') dsToast('error', 'Thao tác thất bại, vui lòng thử lại');
            });
    }

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
    toastTriggers.forEach(function (el) {
        var msg = el.getAttribute('data-toast-msg');
        var type = el.getAttribute('data-toast-type') || 'success';
        dsToast(type, msg);
    });

    /* ── Confirm xóa (Bootstrap Modal) ── */
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
            var form = btn.closest('form');
            if (!form) return;
            e.preventDefault();
            
            // Check if user has checked "don't show again" for this specific action
            var confirmKey = btn.getAttribute('data-confirm-key') || 'default';
            var storageKey = 'ds_confirm_dont_show_' + confirmKey;
            if (sessionStorage.getItem('ds_confirm_dont_show_' + confirmKey) === 'true') {
                dsSubmitFormWithFeedback(form); // Auto submit, don't show modal
                return;
            }
            
            window.__dsConfirmForm = form;
            var msgEl = document.getElementById('confirmModalMessage');
            if (msgEl) msgEl.textContent = msg;
            
            // Reset checkbox
            var chk = document.getElementById('confirmModalDontShow');
            if (chk) chk.checked = false;
            
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

        var confirmBtn = document.getElementById('confirmModalConfirm');
        if (confirmBtn) confirmBtn.addEventListener('click', () => {
            var chk = document.getElementById('confirmModalDontShow');
            if (chk && chk.checked) {
                var form = window.__dsConfirmForm;
                if (form) {
                    var btn = form.querySelector('[data-confirm]');
                    var confirmKey = btn?.getAttribute('data-confirm-key') || 'default';
                    sessionStorage.setItem('ds_confirm_dont_show_' + confirmKey, 'true');
                }
            }
            
            if (window.__dsConfirmForm) {
                dsSubmitFormWithFeedback(window.__dsConfirmForm);
                window.__dsConfirmForm = null;
            }
            confirmModal.hide();
        });
        confirmModalEl.addEventListener('hidden.bs.modal', () => {
            window.__dsConfirmForm = null;
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
        if (typeof TomSelect !== 'undefined') new TomSelect(el, opts);
    });

    /* ── Dirty Save Bar ── */
    function initDirtyBar() {
        var bar = document.getElementById('dsSaveBar');
        if (!bar) return;
        var forms = document.querySelectorAll('form[data-dirty-bar]');
        var main = document.querySelector('.adm-main');
        bar._activeForm = null;
        bar._firstForm = null;
        if (!forms.length) {
            bar.style.display = 'none';
            if (main) main.style.paddingBottom = '';
            return;
        }
        var resetBtn = document.getElementById('dsSaveBarReset');
        var saveBtn = document.getElementById('dsSaveBarSave');

        function getFormData(f) { return new FormData(f); }

        function checkDirty(f) {
            if (!f._cleanData) { return; }
            var current = getFormData(f);
            var dirty = false;
            var keys = new Set();
            for (var pair of f._cleanData.entries()) keys.add(pair[0]);
            for (var pair of current.entries()) keys.add(pair[0]);
            keys.forEach(function (k) {
                var v1 = f._cleanData.getAll(k).sort().join(',');
                var v2 = current.getAll(k).sort().join(',');
                if (v1 !== v2) dirty = true;
            });
            if (dirty !== f._dirty) {
                f._dirty = dirty;
                if (dirty) {
                    bar._activeForm = f;
                    bar.style.display = 'flex';
                    if (main) main.style.paddingBottom = '56px';
                } else {
                    bar.style.display = 'none';
                    if (main) main.style.paddingBottom = '';
                }
            }
        }

        function resetDirty(f) {
            f.reset();
            setTimeout(function () {
                f._cleanData = getFormData(f);
                f._dirty = false;
                if (main) main.style.paddingBottom = '';
                bar.style.display = 'none';
            }, 50);
        }

        forms.forEach(function (f) {
            if (f._dirtyInit) return;
            f._dirtyInit = true;
            if (!bar._firstForm) bar._firstForm = f;
            f._cleanData = getFormData(f);
            f._dirty = false;
            f.addEventListener('input', function () { checkDirty(f); });
            f.addEventListener('change', function () { checkDirty(f); });
        });
        if (forms.length === 1) bar._activeForm = forms[0];
        if (!bar._activeForm) bar._activeForm = bar._firstForm;

        // Khi điều hướng AJAX (bấm Rời khỏi / Lưu / nút reset), dsSaveBar là element
        // dùng chung nên phải ẩn ngay ở mỗi lần khởi tạo lại — không ẩn ở đây thì
        // thanh cũ sẽ dính lại trên trang mới.
        bar.style.display = 'none';
        if (main) main.style.paddingBottom = '';

        if (!bar._dirtyInit) {
            bar._dirtyInit = true;

            // Browser autofill (username/password) can fire after the page loads,
            // making a pristine form look dirty. Re-capture the baseline once
            // autofill has settled so the save bar doesn't show prematurely.
            setTimeout(function () {
                forms.forEach(function (f) {
                    f._cleanData = getFormData(f);
                    f._dirty = false;
                });
                bar.style.display = 'none';
                if (main) main.style.paddingBottom = '';
            }, 400);

            if (resetBtn) resetBtn.addEventListener('click', function () {
                // Reset only the main content area via AJAX instead of a full
                // page reload / history.back().
                if (typeof window.__loadAdminPage === 'function') {
                    window.__loadAdminPage(window.location.pathname + window.location.search);
                } else {
                    history.back();
                }
            });

            if (saveBtn) saveBtn.addEventListener('click', function () {
                var f = bar._activeForm || document.querySelector('form[data-dirty-bar]');
                if (!f) return;
                bar.style.display = 'none';
                if (main) main.style.paddingBottom = '';
                if (typeof window.__submitDirtyForm === 'function') {
                    window.__submitDirtyForm(f);
                } else {
                    f.requestSubmit();
                }
            });
        }
    }

    initDirtyBar();

    /* ── Confirm leaving with unsaved changes ── */
    window.__pendingNav = null;
    document.getElementById('unsavedModalConfirm').addEventListener('click', function () {
        var nav = window.__pendingNav;
        window.__pendingNav = null;
        var modal = bootstrap.Modal.getInstance(document.getElementById('unsavedModal'));
        if (modal) modal.hide();
        if (typeof nav === 'function') {
            nav();
        } else if (typeof window.__loadAdminPage === 'function') {
            // No pending navigation: discard changes and reload current main area via AJAX
            window.__loadAdminPage(window.location.pathname + window.location.search);
        }
    });
    document.getElementById('unsavedModal').addEventListener('hidden.bs.modal', function () {
        window.__pendingNav = null;
    });
    window.addEventListener('beforeunload', function (e) {
        var forms = document.querySelectorAll('form[data-dirty-bar]');
        for (var i = 0; i < forms.length; i++) {
            if (forms[i]._dirty) {
                e.preventDefault();
                e.returnValue = '';
                return;
            }
        }
    });

    document.addEventListener('click', function (e) {
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

    // ── expose reinit for AJAX navigation ──
    window.__reinitAdminComponents = function () {
        /* Re-init dirty bar for newly loaded forms */
        initDirtyBar();
        /* Toast */
        document.querySelectorAll('[data-toast-msg]').forEach(function (el) {
            var msg = el.getAttribute('data-toast-msg');
            var type = el.getAttribute('data-toast-type') || 'success';
            dsToast(type, msg);
            el.remove();
        });

        /* Confirm modal — re-bind [data-confirm] */
        var cmEl = document.getElementById('confirmModal');
        if (cmEl) {
            document.querySelectorAll('[data-confirm]').forEach(function (btn) {
                btn.onclick = function (e) {
                    e.preventDefault();
                    var form = btn.closest('form');
                    if (!form) return;
                    window.__dsConfirmForm = form;
                    var msgEl = document.getElementById('confirmModalMessage');
                    if (msgEl) msgEl.textContent = btn.getAttribute('data-confirm');
                    if (window.__confirmModal) {
                        window.__confirmModal.show();
                    } else {
                        window.__confirmModal = new bootstrap.Modal(cmEl);
                        window.__confirmModal.show();
                    }
                };
            });
            var confirmBtn = document.getElementById('confirmModalConfirm');
            if (confirmBtn) confirmBtn.onclick = function () {
                if (window.__dsConfirmForm) {
                    dsSubmitFormWithFeedback(window.__dsConfirmForm);
                    window.__dsConfirmForm = null;
                }
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
            if (typeof TomSelect !== 'undefined') new TomSelect(el, opts);
        });

    };
});

/* ══════════════════════════════════════════════════════════════
   AJAX Navigation — sidebar clicks load main content only
   ══════════════════════════════════════════════════════════════ */
(function () {
    var sidebar = document.querySelector('.adm-sidebar');
    var contentArea = document.querySelector('.adm-content');
    if (!sidebar || !contentArea) return;

    function execScripts(container, scripts) {
        var list = scripts || Array.from(container.querySelectorAll('script'));
        for (var i = 0; i < list.length; i++) {
            try {
                var old = list[i];
                if (!old.parentNode) continue;
                if (old.src) {
                    var s = document.createElement('script');
                    s.src = old.src;
                    old.parentNode.replaceChild(s, old);
                } else {
                    try { (0, eval)(old.textContent); } catch (e2) { /* ignore */ }
                    old.parentNode.removeChild(old);
                }
            } catch (e) { /* ignore script errors from extensions */ }
        }
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
                var contentScripts = Array.from(contentArea.querySelectorAll('script'));

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
                var extraScripts = [];
                for (var k = 0; k < extraEls.length; k++) {
                    var clone = extraEls[k].cloneNode(true);
                    clone.setAttribute('data-ajax-extra', 'true');
                    if (clone.tagName === 'SCRIPT') {
                        extraScripts.push(clone);
                    } else {
                        Array.from(clone.querySelectorAll('script')).forEach(function (s) { extraScripts.push(s); });
                        document.body.appendChild(clone);
                    }
                }

                // Execute scripts in content area
                execScripts(contentArea, contentScripts);

                // Execute scripts in new extra content
                extraScripts.forEach(function (old) {
                    try {
                        if (old.src) {
                            var s = document.createElement('script');
                            s.src = old.src;
                            if (old.parentNode) {
                                old.parentNode.replaceChild(s, old);
                            } else {
                                document.body.appendChild(s);
                            }
                        } else {
                            try { (0, eval)(old.textContent); } catch (e2) { /* ignore */ }
                        }
                    } catch (e) { /* ignore script errors from extensions */ }
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

    window.__loadAdminPage = loadAdminPage;
    window.__handleResponseHtml = function (html) {        var doc = new DOMParser().parseFromString(html, 'text/html');
        var newContent = doc.querySelector('.adm-content');
        if (newContent) {
            contentArea.innerHTML = newContent.innerHTML;
            execScripts(contentArea);
        }
        document.title = doc.title;
        var bc = doc.querySelector('.breadcrumb');
        var curBc = document.querySelector('.breadcrumb');
        if (bc && curBc) curBc.innerHTML = bc.innerHTML;
        var toastFired = false;
        doc.querySelectorAll('[data-toast-msg]').forEach(function (el) {
            var msg = el.getAttribute('data-toast-msg');
            var type = el.getAttribute('data-toast-type') || 'success';
            if (typeof dsToast === 'function') { dsToast(type, msg); toastFired = true; }
        });
        if (window.__dsPendingDefaultToast && !toastFired) {
            if (typeof dsToast === 'function') dsToast('success', window.__dsPendingDefaultToast);
        }
        window.__dsPendingDefaultToast = null;
        if (window.__reinitAdminComponents) window.__reinitAdminComponents();
    };

    window.__submitDirtyForm = function (f) {
        var fd = new FormData(f);
        var url = f.getAttribute('action');
        var method = f.getAttribute('method') || 'post';
        fetch(url, { method: method.toUpperCase(), body: fd, headers: { 'X-Requested-With': 'XMLHttpRequest' } })
            .then(function (r) {
                if (r.redirected) {
                    var finalUrl = r.url;
                    if (finalUrl && finalUrl !== window.location.href) history.pushState({ url: finalUrl }, '', finalUrl);
                    return r.text().then(window.__handleResponseHtml);
                }
                if (!r.ok) throw new Error('HTTP ' + r.status);
                return r.text();
            })
            .then(function (html) {
                if (!html) return;
                window.__handleResponseHtml(html);
            })
            .catch(function () {
                if (typeof dsToast === 'function') dsToast('error', 'Lưu thất bại, vui lòng thử lại');
            });
    };

    function confirmLeavingIfDirty(fn) {
        var forms = document.querySelectorAll('form[data-dirty-bar]');
        for (var i = 0; i < forms.length; i++) {
            if (forms[i]._dirty) {
                window.__pendingNav = fn || null;
                var modalEl = document.getElementById('unsavedModal');
                if (modalEl) {
                    var modal = bootstrap.Modal.getOrCreateInstance(modalEl);
                    modal.show();
                }
                return false;
            }
        }
        return true;
    }

    sidebar.addEventListener('click', function (e) {
        var link = e.target.closest('a.adm-nav-link');
        if (!link) return;
        var href = link.getAttribute('href');
        if (!href || href === '#' || href === '' || href.indexOf('http') === 0) return;
        if (href.indexOf('/admin') !== 0) return;
        if (e.ctrlKey || e.metaKey || e.button === 1) return;
        e.preventDefault();
        if (confirmLeavingIfDirty(function () { loadAdminPage(href, link); })) loadAdminPage(href, link);
    });

    window.addEventListener('popstate', function (e) {
        if (e.state && e.state.url) {
            window.location.href = e.state.url;
        }
    });

    /* ── AJAX pagination: intercept pagination links & size select ── */
    document.addEventListener('click', function (e) {
        var link = e.target.closest('[data-pagination] a[href]');
        if (!link) return;
        var href = link.getAttribute('href');
        if (!href || href === '#' || href.indexOf('/admin') !== 0) return;
        if (e.ctrlKey || e.metaKey || e.button === 1) return;
        e.preventDefault();
        if (confirmLeavingIfDirty(function () { loadAdminPage(href); })) loadAdminPage(href);
    });
    document.addEventListener('change', function (e) {
        var sel = e.target.closest('[data-pagination] select[name="size"]');
        if (!sel) return;
        var form = sel.closest('form');
        if (!form) return;
        var url = form.action;
        if (!url) return;
        var params = new URLSearchParams(new FormData(form));
        if (confirmLeavingIfDirty(function () { loadAdminPage(url + '?' + params.toString()); })) loadAdminPage(url + '?' + params.toString());
    });

    /* ── AJAX tab navigation: load via AJAX instead of full reload ── */
    document.addEventListener('click', function (e) {
        var tab = e.target.closest('.adm-tab-nav a.adm-tab');
        if (!tab) return;
        var href = tab.getAttribute('href');
        if (!href || href === '#' || href.indexOf('/admin') !== 0) return;
        if (e.ctrlKey || e.metaKey || e.button === 1) return;
        e.preventDefault();
        if (confirmLeavingIfDirty(function () { loadAdminPage(href, null); })) loadAdminPage(href, null);
    });
})();

/* ── Submit spinner ── */
document.addEventListener('submit', function(e) {
    var btn = e.target.querySelector('button[type="submit"]');
    if (btn) { btn.disabled = true; btn.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span> Đang xử lý...'; }
});

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

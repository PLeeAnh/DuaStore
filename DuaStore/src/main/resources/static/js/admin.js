/* =====================================================
   DuaStore — admin.js
===================================================== */

'use strict';

document.addEventListener('DOMContentLoaded', () => {

    /* ══════════════════════════════════════════
       EDIT TOGGLE TẠI ĐÂY — Admin sidebar toggle (☰ / ✕)

       ★ Desktop (≥992px): sidebar luôn hiện, toggle ẩn (CSS)
       ★ Mobile (<992px):
          - click toggle → sidebar trượt vào (thêm/remove lớp .adm-sidebar-mobile-open)
          - click .adm-main → đóng sidebar
          - resize lên desktop → tự động đóng sidebar mobile

       ★ CSS: .adm-sidebar-mobile-open trong admin.css dòng 393
    ══════════════════════════════════════════ */
    const toggle = document.getElementById('admNavToggle');
    const sidebar = document.querySelector('.adm-sidebar');
    const icon = document.getElementById('admToggleIcon');

    if (toggle && sidebar) {
        function isMobile() { return window.innerWidth <= 991; }

        toggle.addEventListener('click', (e) => {
            e.stopPropagation();
            const isOpen = sidebar.classList.toggle('adm-sidebar-mobile-open');
            toggle.setAttribute('aria-label', isOpen ? 'Đóng menu' : 'Mở menu');
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
                }
            });
        }

        window.addEventListener('resize', () => {
            if (!isMobile() && sidebar.classList.contains('adm-sidebar-mobile-open')) {
                sidebar.classList.remove('adm-sidebar-mobile-open');
                document.body.style.overflow = '';
            }
        });
    }
    /* ══════════════════════════════════════════
       KẾT THÚC EDIT TOGGLE
    ══════════════════════════════════════════ */

    /* ── Profile menu admin ── */
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

    /* ── Admin theme toggle (inside profile dropdown) ──
       ★ Dùng CHUNG localStorage key 'duastore-theme' với client
         → đổi theme bên admin tự động đồng bộ bên client (và ngược lại)
       ★ CSS: dùng [data-theme="dark"] selector giống client
       ★ Inline script trong <head> base.html (admin) set data-theme trước render */
    function adminGetTheme() { return localStorage.getItem('duastore-theme') || 'light'; }
    function adminSetTheme(theme) {
        document.documentElement.setAttribute('data-theme', theme);
        localStorage.setItem('duastore-theme', theme);
        const isDark = theme === 'dark';
        const icon = document.getElementById('admProfileThemeIcon');
        const label = document.getElementById('admProfileThemeLabel');
        if (icon) icon.className = isDark ? 'bi bi-sun' : 'bi bi-moon-stars';
        if (label) label.textContent = isDark ? 'Chế độ sáng' : 'Chế độ tối';
    }
    adminSetTheme(adminGetTheme());
    const admThemeBtn = document.getElementById('admProfileThemeToggle');
    if (admThemeBtn) admThemeBtn.addEventListener('click', (e) => { e.preventDefault(); adminSetTheme(adminGetTheme() === 'dark' ? 'light' : 'dark'); });

    /* ── Auto-dismiss alerts sau 4 giây ── */
    document.querySelectorAll('.alert.alert-dismissible').forEach(el => {
        setTimeout(() => {
            const instance = bootstrap.Alert.getOrCreateInstance(el);
            if (instance) instance.close();
        }, 4000);
    });

    /* ── Confirm xóa ── */
    document.querySelectorAll('[data-confirm]').forEach(btn => {
        btn.addEventListener('click', e => {
            const msg = btn.getAttribute('data-confirm') || 'Xác nhận thực hiện thao tác này?';
            if (!confirm(msg)) e.preventDefault();
        });
    });

});

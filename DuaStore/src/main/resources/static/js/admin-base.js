'use strict';

function toggleNavSub(id, header) {
    var el = document.getElementById(id);
    if (!el) return;
    el.classList.toggle('open');
    header.classList.toggle('open');
}

function toggleStaffNotifPopup() {
    var popup = document.getElementById('staffNotifPopup');
    if (!popup) return;
    var isVisible = popup.style.display === 'block';
    popup.style.display = isVisible ? 'none' : 'block';
    if (!isVisible) {
        var badge = document.getElementById('staffNotifBadge');
        if (badge && !badge.classList.contains('d-none')) {
            var csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content || '';
            var csrfToken = document.querySelector('meta[name="_csrf"]')?.content || '';
            var headers = { 'Content-Type': 'application/json' };
            if (csrfHeader && csrfToken) headers[csrfHeader] = csrfToken;
            fetch('/admin/thong-bao/api/doc-tat-ca', { method: 'POST', headers: headers })
                .then(function(r) { return r.text(); })
                .then(function() { badge.classList.add('d-none'); })
                .catch(function() { badge.classList.add('d-none'); });
        }
    }
}

function markStaffNotifRead(id) {
    var csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content || '';
    var csrfToken = document.querySelector('meta[name="_csrf"]')?.content || '';
    var headers = { 'Content-Type': 'application/json' };
    if (csrfHeader && csrfToken) headers[csrfHeader] = csrfToken;
    fetch('/admin/thong-bao/api/doc/' + id, { method: 'POST', headers: headers })
        .catch(function() {});
}

document.addEventListener('click', function(e) {
    var popup = document.getElementById('staffNotifPopup');
    var btn = document.getElementById('btnStaffNotifToggle');
    if (popup && btn) {
        if (!popup.contains(e.target) && !btn.contains(e.target)) {
            popup.style.display = 'none';
        }
    }
});

/* ── Sidebar mobile toggle ── */
(function() {
    var sidebar = document.querySelector('.adm-sidebar');
    var backdrop = document.getElementById('admSidebarBackdrop');
    var toggle = document.getElementById('admNavToggle');
    if (!sidebar || !backdrop || !toggle) return;

    function openSidebar() {
        sidebar.classList.add('adm-sidebar-mobile-open');
        backdrop.classList.add('show');
        document.body.style.overflow = 'hidden';
    }

    function closeSidebar() {
        sidebar.classList.remove('adm-sidebar-mobile-open');
        backdrop.classList.remove('show');
        document.body.style.overflow = '';
    }

    toggle.addEventListener('click', function(e) {
        e.stopPropagation();
        if (sidebar.classList.contains('adm-sidebar-mobile-open')) {
            closeSidebar();
        } else {
            openSidebar();
        }
    });

    backdrop.addEventListener('click', closeSidebar);

    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape' && sidebar.classList.contains('adm-sidebar-mobile-open')) {
            closeSidebar();
        }
    });

    window.addEventListener('resize', function() {
        if (window.innerWidth >= 992) {
            closeSidebar();
        }
    });
})();

/* ── Notification polling ── */
(function() {
    const badge = document.getElementById('staffNotifBadge');
    if (badge) {
        setInterval(function() {
            fetch('/admin/thong-bao/api/count')
                .then(r => r.text())
                .then(count => {
                    const num = parseInt(count) || 0;
                    badge.textContent = num;
                    badge.classList.toggle('d-none', num === 0);
                })
                .catch(() => {});
        }, 30000);
    }
/* ── Notification polling ── */
(function() {
    var badge = document.getElementById('staffNotifBadge');
    if (!badge) return;
    setInterval(function() {
        fetch('/admin/thong-bao/api/count')
            .then(function(r) { return r.text(); })
            .then(function(count) {
                var num = parseInt(count) || 0;
                badge.classList.toggle('d-none', num === 0);
            })
            .catch(function() {});
    }, 30000);
})();

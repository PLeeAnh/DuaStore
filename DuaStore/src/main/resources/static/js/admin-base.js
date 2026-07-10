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

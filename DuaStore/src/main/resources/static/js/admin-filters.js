'use strict';

document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('[data-autosubmit]').forEach(function(el) {
        var delay = parseInt(el.getAttribute('data-autosubmit'), 10) || 300;
        var timer;
        function trigger() {
            clearTimeout(timer);
            timer = setTimeout(function () {
                var form = el.closest('form');
                if (form) form.submit();
            }, delay);
        }
        if (el.tagName === 'SELECT') {
            el.addEventListener('change', trigger);
        } else {
            el.addEventListener('input', trigger);
        }
    });
});

// ── category-list filter (form auto-submit) ──
(function () {
    var filterForm = document.getElementById('filterForm');
    if (!filterForm) return;
    var keywordInput = document.getElementById('keywordInput');
    var statusSelect = document.getElementById('statusSelect');
    var timer;

    function submitFilter() {
        clearTimeout(timer);
        timer = setTimeout(function () {
            filterForm.submit();
        }, 400);
    }

    if (keywordInput) keywordInput.addEventListener('input', submitFilter);
    if (statusSelect) statusSelect.addEventListener('change', function () {
        filterForm.submit();
    });
})();

// ── notification-list filter ──
(function () {
    var el = document.getElementById('filterActive');
    var kw = document.getElementById('searchKeyword');
    if (!el && !kw) return;
    function doFilter() {
        var keyword = kw ? kw.value : '';
        var active = el ? el.value : '';
        var params = new URLSearchParams();
        if (keyword)
            params.set('keyword', keyword);
        if (active)
            params.set('isActive', active);
        window.location.href = '/admin/thong-bao?' + params.toString();
    }
    if (el) el.addEventListener('change', doFilter);
    if (kw) {
        var searchTimer;
        kw.addEventListener('input', function () {
            clearTimeout(searchTimer);
            searchTimer = setTimeout(doFilter, 400);
        });
    }
})();

// ── order-list filter ──
// Note: `tatCa` must be defined as a global var via an inline script in the HTML template
// (e.g., <script>var tatCa = [[${tatCa}]];</script>) before this script loads.
(function () {
    var tatCa = window.tatCa || false;
    var qEl = document.getElementById('searchQ');
    var ttEl = document.getElementById('filterTrangThai');
    var tttEl = document.getElementById('filterTrangThaiTT');
    if (!qEl && !ttEl && !tttEl) return;
    function doFilter() {
        var q = qEl ? qEl.value : '';
        var trangThai = ttEl ? ttEl.value : '';
        var trangThaiTT = tttEl ? tttEl.value : '';
        var params = new URLSearchParams();
        if (q)
            params.set('q', q);
        if (trangThai)
            params.set('trangThai', trangThai);
        if (trangThaiTT)
            params.set('trangThaiTT', trangThaiTT);
        params.set('tatCa', tatCa);
        window.location.href = '/admin/don-hang?' + params.toString();
    }
    if (ttEl) ttEl.addEventListener('change', doFilter);
    if (tttEl) tttEl.addEventListener('change', doFilter);
    if (qEl) {
        var searchTimer;
        qEl.addEventListener('input', function () {
            clearTimeout(searchTimer);
            searchTimer = setTimeout(doFilter, 400);
        });
    }
})();

// ── post-list filter ──
(function () {
    var kw = document.getElementById('searchKeyword');
    var st = document.getElementById('filterStatus');
    if (!kw && !st) return;
    function doFilter() {
        var keyword = kw ? kw.value : '';
        var status = st ? st.value : '';
        var params = new URLSearchParams();
        if (keyword)
            params.set('keyword', keyword);
        if (status)
            params.set('trangThai', status);
        window.location.href = '/admin/bai-viet?' + params.toString();
    }
    if (st) st.addEventListener('change', doFilter);
    if (kw) {
        var searchTimer;
        kw.addEventListener('input', function () {
            clearTimeout(searchTimer);
            searchTimer = setTimeout(doFilter, 400);
        });
    }
})();

// ── promotion-list filter ──
(function () {
    var kw = document.getElementById('searchKeyword');
    var ac = document.getElementById('filterActive');
    if (!kw && !ac) return;
    function doFilter() {
        var keyword = kw ? kw.value : '';
        var active = ac ? ac.value : '';
        var params = new URLSearchParams();
        if (keyword)
            params.set('keyword', keyword);
        if (active)
            params.set('isActive', active);
        window.location.href = '/admin/khuyen-mai?' + params.toString();
    }
    if (ac) ac.addEventListener('change', doFilter);
    if (kw) {
        var searchTimer;
        kw.addEventListener('input', function () {
            clearTimeout(searchTimer);
            searchTimer = setTimeout(doFilter, 400);
        });
    }
})();

// ── customer/list filter ──
(function () {
    var kw = document.getElementById('searchKeyword');
    var st = document.getElementById('filterStatus');
    var ct = document.getElementById('filterCity');
    var sp = document.getElementById('filterSpendingTier');
    if (!kw && !st && !ct && !sp) return;
    function doFilter() {
        var keyword = kw ? kw.value : '';
        var status = st ? st.value : '';
        var city = ct ? ct.value : '';
        var spendingTier = sp ? sp.value : '';
        var params = new URLSearchParams();
        if (keyword)
            params.set('keyword', keyword);
        if (status)
            params.set('status', status);
        if (city)
            params.set('city', city);
        if (spendingTier)
            params.set('spendingTier', spendingTier);
        window.location.href = '/admin/khach-hang?' + params.toString();
    }
    if (st) st.addEventListener('change', doFilter);
    if (ct) ct.addEventListener('change', doFilter);
    if (sp) sp.addEventListener('change', doFilter);
    if (kw) {
        var searchTimer;
        kw.addEventListener('input', function () {
            clearTimeout(searchTimer);
            searchTimer = setTimeout(doFilter, 400);
        });
    }
})();

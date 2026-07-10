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
    if (filterForm) {
        var keywordInput = document.getElementById('keywordInput');
        var statusSelect = document.getElementById('statusSelect');
        var timer;

        function submitFilter() {
            clearTimeout(timer);
            timer = setTimeout(function () {
                filterForm.submit();
            }, 400);
        }

        keywordInput.addEventListener('input', submitFilter);
        statusSelect.addEventListener('change', function () {
            filterForm.submit();
        });
    }
})();

// ── notification-list filter ──
(function () {
    function doFilter() {
        var keyword = document.getElementById('searchKeyword').value;
        var active = document.getElementById('filterActive').value;
        var params = new URLSearchParams();
        if (keyword)
            params.set('keyword', keyword);
        if (active)
            params.set('isActive', active);
        window.location.href = '/admin/thong-bao?' + params.toString();
    }
    document.getElementById('filterActive').addEventListener('change', doFilter);
    var searchTimer;
    document.getElementById('searchKeyword').addEventListener('input', function () {
        clearTimeout(searchTimer);
        searchTimer = setTimeout(doFilter, 400);
    });
})();

// ── order-list filter ──
// Note: `tatCa` must be defined as a global var via an inline script in the HTML template
// (e.g., <script>var tatCa = [[${tatCa}]];</script>) before this script loads.
(function () {
    var tatCa = window.tatCa || false;
    function doFilter() {
        var q = document.getElementById('searchQ').value;
        var trangThai = document.getElementById('filterTrangThai').value;
        var trangThaiTT = document.getElementById('filterTrangThaiTT').value;
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
    document.getElementById('filterTrangThai').addEventListener('change', doFilter);
    document.getElementById('filterTrangThaiTT').addEventListener('change', doFilter);
    var searchTimer;
    document.getElementById('searchQ').addEventListener('input', function () {
        clearTimeout(searchTimer);
        searchTimer = setTimeout(doFilter, 400);
    });
})();

// ── post-list filter ──
(function () {
    function doFilter() {
        var keyword = document.getElementById('searchKeyword').value;
        var status = document.getElementById('filterStatus').value;
        var params = new URLSearchParams();
        if (keyword)
            params.set('keyword', keyword);
        if (status)
            params.set('trangThai', status);
        window.location.href = '/admin/bai-viet?' + params.toString();
    }
    document.getElementById('filterStatus').addEventListener('change', doFilter);
    var searchTimer;
    document.getElementById('searchKeyword').addEventListener('input', function () {
        clearTimeout(searchTimer);
        searchTimer = setTimeout(doFilter, 400);
    });
})();

// ── promotion-list filter ──
(function () {
    function doFilter() {
        var keyword = document.getElementById('searchKeyword').value;
        var active = document.getElementById('filterActive').value;
        var params = new URLSearchParams();
        if (keyword)
            params.set('keyword', keyword);
        if (active)
            params.set('isActive', active);
        window.location.href = '/admin/khuyen-mai?' + params.toString();
    }
    document.getElementById('filterActive').addEventListener('change', doFilter);
    var searchTimer;
    document.getElementById('searchKeyword').addEventListener('input', function () {
        clearTimeout(searchTimer);
        searchTimer = setTimeout(doFilter, 400);
    });
})();

// ── customer/list filter ──
(function () {
    function doFilter() {
        var keyword = document.getElementById('searchKeyword').value;
        var status = document.getElementById('filterStatus').value;
        var params = new URLSearchParams();
        if (keyword)
            params.set('keyword', keyword);
        if (status)
            params.set('status', status);
        window.location.href = '/admin/khach-hang?' + params.toString();
    }
    document.getElementById('filterStatus').addEventListener('change', doFilter);
    var searchTimer;
    document.getElementById('searchKeyword').addEventListener('input', function () {
        clearTimeout(searchTimer);
        searchTimer = setTimeout(doFilter, 400);
    });
})();

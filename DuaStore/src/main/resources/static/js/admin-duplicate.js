(function () {
    'use strict';

    var DUPLICATE_CHECK_URL = '/admin/api/kiem-tra-trung';

    var INPUT_IDS = {
        category: 'tenDanhMuc',
        product: 'tenSanPham',
        variant: 'tenBienThe'
    };

    var LABELS = {
        'danh-muc': 'Danh mục',
        'san-pham': 'Sản phẩm',
        'bien-the': 'Biến thể'
    };

    var LINKS = {
        'danh-muc': '/admin/danh-muc/chi-tiet/',
        'san-pham': '/admin/san-pham/chi-tiet/'
    };

    function initDuplicateCheck(type, opts) {
        opts = opts || {};
        var inputId = INPUT_IDS[type];
        if (!inputId) return;
        var input = document.getElementById(inputId);
        if (!input) return;

        var container = document.createElement('div');
        container.className = 'duplicate-warn-container';
        container.style.cssText = 'margin-top:4px;font-size:.82rem;';
        input.parentNode.appendChild(container);

        var lastValue = '';
        var timer;

        input.addEventListener('input', function () {
            var val = input.value.trim();
            if (val === lastValue) return;
            lastValue = val;

            container.innerHTML = '';
            if (val.length < 3) return;

            container.innerHTML = '<span class="text-muted"><i class="bi bi-hourglass-split me-1"></i>Đang kiểm tra...</span>';
            clearTimeout(timer);
            timer = setTimeout(function () {
                var params = 'type=' + encodeURIComponent(type) + '&name=' + encodeURIComponent(val);
                if (opts.excludeId) params += '&excludeId=' + opts.excludeId;
                if (opts.productId) params += '&productId=' + opts.productId;

                fetch(DUPLICATE_CHECK_URL + '?' + params)
                    .then(function (r) { return r.json(); })
                    .then(function (data) {
                        container.innerHTML = '';
                        if (!data.hasDuplicates) {
                            container.innerHTML = '<span class="text-success"><i class="bi bi-check-circle me-1"></i>Không phát hiện trùng lặp</span>';
                            return;
                        }
                        var html = '<div class="alert alert-warning py-2 px-3 mb-0" style="font-size:.82rem;">' +
                            '<i class="bi bi-exclamation-triangle me-1"></i><strong>Có thể trùng lặp:</strong><ul class="mb-0 ps-3 mt-1" style="list-style:none;padding-left:0!important;">';
                        data.matches.forEach(function (m) {
                            var label = LABELS[m.type] || m.type;
                            var link = LINKS[m.type] || '';
                            html += '<li class="mb-1">' +
                                (link ? '<a href="' + link + m.id + '" target="_blank" class="text-decoration-none fw-semibold" style="color:#856404;">' +
                                    '<i class="bi bi-box-arrow-up-right me-1" style="font-size:.7rem;"></i>' + escapeHtml(m.name) +
                                    '</a>' : '<span class="fw-semibold" style="color:#856404;">' + escapeHtml(m.name) + '</span>') +
                                ' <span class="text-muted">(' + label + ')</span>' +
                                ' <span class="badge bg-warning text-dark">' + m.score + '</span>' +
                                (m.path ? '<br><span class="text-muted" style="font-size:.75rem;">' + escapeHtml(m.path) + '</span>' : '') +
                                '</li>';
                        });
                        html += '</ul></div>';
                        container.innerHTML = html;
                    })
                    .catch(function () {
                        container.innerHTML = '';
                    });
            }, 400);
        });
    }

    function escapeHtml(text) {
        var div = document.createElement('div');
        div.appendChild(document.createTextNode(text));
        return div.innerHTML;
    }

    window.initDuplicateCheck = initDuplicateCheck;
})();

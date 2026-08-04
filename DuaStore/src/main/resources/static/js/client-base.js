(function() {
    /* Business hours */
    var hoursEl = document.getElementById('footerBusinessHours');
    if (hoursEl) {
        var raw = hoursEl.dataset.json;
        try {
            if (!raw) throw 'no data';
            var data = JSON.parse(raw);
            var dayNames = {mon:'T2',tue:'T3',wed:'T4',thu:'T5',fri:'T6',sat:'T7',sun:'CN'};
            var order = ['mon','tue','wed','thu','fri','sat','sun'];

            function daySchedule(d) {
                var day = data[d];
                if (!day || !day.open) return 'closed';
                if (day.allDay) return 'allday';
                var seen = {};
                var times = [];
                (day.slots || []).forEach(function(s) {
                    if (!s || !s.open || !s.close) return;
                    if (!/^\d{2}:\d{2}$/.test(s.open) || !/^\d{2}:\d{2}$/.test(s.close)) return;
                    var k = s.open + ' – ' + s.close;
                    if (!seen[k]) { seen[k] = true; times.push(k); }
                });
                return times.length ? times.join(', ') : 'closed';
            }

            var groups = [];
            var cur = null;
            order.forEach(function(d) {
                var label = dayNames[d];
                var sched = daySchedule(d);
                if (cur && cur.sched === sched) {
                    cur.days.push(label);
                } else {
                    cur = { days: [label], sched: sched };
                    groups.push(cur);
                }
            });

            function rangeLabel(days) {
                if (days.length === 1) return days[0];
                return days[0] + ' – ' + days[days.length - 1];
            }

            var parts = groups.map(function(g) {
                var label = rangeLabel(g.days);
                if (g.sched === 'closed') return '<span style="opacity:.55">' + label + ': Đóng cửa</span>';
                if (g.sched === 'allday') return label + ': Mở cả ngày';
                return label + ': ' + g.sched;
            });
            hoursEl.innerHTML = parts.join('<br>') || '<span style="opacity:.55">Chưa cập nhật</span>';
        } catch(e) { hoursEl.style.display = 'none'; }
    }

    /* Image overlay */
    document.querySelectorAll('.ds-footer-card img').forEach(function(img) {
        var parent = img.closest('.ds-footer-card');
        if (!parent) return;
        var container = img.closest('[style*="max-width:300px"]');
        if (!container) return;
        img.style.cursor = 'pointer';
        img.addEventListener('click', function() { showImageOverlay(this.src); });
    });

    document.querySelectorAll('.ds-product-image-wrap img').forEach(function(img) {
        img.style.cursor = 'zoom-in';
        img.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            showImageOverlay(this.src);
        });
    });

    function showImageOverlay(src) {
        var overlay = document.createElement('div');
        overlay.style.cssText = 'position:fixed;top:0;left:0;width:100%;height:100%;background:rgba(0,0,0,.85);z-index:99999;display:flex;align-items:center;justify-content:center;cursor:pointer';
        overlay.onclick = function() { document.body.removeChild(overlay); };
        var full = document.createElement('img');
        full.src = src;
        full.style.cssText = 'max-width:90vw;max-height:90vh;border-radius:8px;box-shadow:0 4px 30px rgba(0,0,0,.5)';
        overlay.appendChild(full);
        document.body.appendChild(overlay);
    }

    /* Footer map */
    var mapEl = document.getElementById('footerMap');
    if (mapEl) {
        function initFooterMap() {
            var lat = parseFloat(mapEl.dataset.lat) || 20.8565;
            var lng = parseFloat(mapEl.dataset.lng) || 106.6756;
            var map = L.map(mapEl, { center: [lat, lng], zoom: 14, zoomControl: true, attributionControl: false, dragging: true, scrollWheelZoom: true });
            L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', { maxZoom: 19 }).addTo(map);
            L.marker([lat, lng]).addTo(map);
        }
        if (typeof L !== 'undefined') {
            initFooterMap();
        } else {
            var cssLink = document.createElement('link');
            cssLink.rel = 'stylesheet';
            cssLink.href = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.css';
            document.head.appendChild(cssLink);
            var script = document.createElement('script');
            script.src = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.js';
            script.onload = initFooterMap;
            document.body.appendChild(script);
        }
    }
})();

/* Search suggestions */
var searchInput = document.getElementById('searchInput');
var searchBox = document.getElementById('searchSuggestions');
if (searchInput && searchBox) {
    var searchTimer;
    searchInput.addEventListener('input', function () {
        clearTimeout(searchTimer);
        var q = this.value.trim();
        if (q.length < 2) { searchBox.classList.add('d-none'); return; }
        searchTimer = setTimeout(function () {
            fetch('/api/products/suggestions?keyword=' + encodeURIComponent(q))
                .then(function (r) { return r.json(); })
                .then(function (data) {
                    searchBox.innerHTML = '';
                    if (!data || !data.length) { searchBox.classList.add('d-none'); return; }
                    var html = '<div class="ds-suggest-header">Gợi ý</div>';
                    data.forEach(function (p) {
                        var img = p.hinhAnhChinh || '/images/no-image.png';
                        html += '<a href="/san-pham/' + p.id + '" class="ds-suggest-item">' +
                            '<img src="' + img + '" class="ds-suggest-img">' +
                            '<span class="ds-suggest-name">' + p.tenSanPham + '</span>' +
                            '</a>';
                    });
                    searchBox.innerHTML = html;
                    searchBox.classList.remove('d-none');
                });
        }, 300);
    });
    document.addEventListener('click', function (e) {
        if (!searchBox.contains(e.target) && e.target !== searchInput) {
            searchBox.classList.add('d-none');
        }
    });
}

document.addEventListener('submit', function(e) {
    var btn = e.target.querySelector('button[type="submit"]');
    if (btn) { btn.disabled = true; btn.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span> Đang xử lý...'; }
});

(function () {
    'use strict';

    var copilotOpen = true;

    window.toggleCopilot = function () {
        copilotOpen = !copilotOpen;
        var body = document.getElementById('copilotBody');
        var arrow = document.getElementById('copilotArrow');
        if (body) body.style.display = copilotOpen ? 'block' : 'none';
        if (arrow) arrow.style.transform = copilotOpen ? 'rotate(0deg)' : 'rotate(-90deg)';
    };

    window.askCopilot = function () {
        var qEl = document.getElementById('copilotQuery');
        var q = qEl ? qEl.value.trim() : '';
        if (!q) return;
        var btn = document.getElementById('copilotBtn');
        var answerDiv = document.getElementById('copilotAnswer');
        var errorDiv = document.getElementById('copilotError');
        if (answerDiv) answerDiv.style.display = 'none';
        if (errorDiv) errorDiv.style.display = 'none';
        if (btn) {
            btn.disabled = true;
            btn.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span>Đang trả lời...';
        }
        var csrfToken = document.querySelector('meta[name="_csrf"]') ? document.querySelector('meta[name="_csrf"]').getAttribute('content') : null;
        var csrfHeader = document.querySelector('meta[name="_csrf_header"]') ? document.querySelector('meta[name="_csrf_header"]').getAttribute('content') : null;
        var headers = { 'Content-Type': 'application/json' };
        if (csrfToken && csrfHeader) headers[csrfHeader] = csrfToken;
        fetch('/admin/phan-tich/api/copilot', {
            method: 'POST',
            headers: headers,
            body: JSON.stringify({ query: q })
        })
        .then(function (r) { return r.json(); })
        .then(function (data) {
            if (answerDiv) {
                answerDiv.textContent = data.answer;
                answerDiv.style.display = 'block';
            }
        })
        .catch(function () {
            if (errorDiv) {
                errorDiv.textContent = 'Lỗi kết nối, vui lòng thử lại.';
                errorDiv.style.display = 'block';
            }
        })
        .finally(function () {
            if (btn) {
                btn.disabled = false;
                btn.innerHTML = '<i class="bi bi-send me-1"></i>Hỏi';
            }
        });
    };

    var chartInstances = [];

    window.initAnalyticsCharts = function (data) {
        if (!data) { console.warn('[analytics] Không có dữ liệu (window.__analyticsData rỗng/null) — chart sẽ không hiện.'); return; }
        if (typeof Chart === 'undefined') { console.error('[analytics] Thư viện Chart.js chưa load được — kiểm tra kết nối mạng/CDN bị chặn.'); return; }
        try {
        // Destroy previously created charts so re-init (AJAX navigation) doesn't
        // throw "Canvas is already in use".
        chartInstances.forEach(function (c) { if (c) c.destroy(); });
        chartInstances = [];

        var colorPalette = ['#2563eb', '#f97316', '#10b981', '#8b5cf6', '#ef4444', '#6b7280'];

        function showEmptyState(canvas, message) {
            if (!canvas || !canvas.parentNode) return;
            var parent = canvas.parentNode;
            canvas.style.display = 'none';
            var empty = document.createElement('div');
            empty.className = 'text-muted small text-center d-flex align-items-center justify-content-center';
            empty.style.height = '100%';
            empty.textContent = message || 'Chưa có dữ liệu';
            parent.appendChild(empty);
        }

        function clearEmptyState(canvas) {
            if (!canvas || !canvas.parentNode) return;
            canvas.style.display = '';
            var parent = canvas.parentNode;
            var empties = parent.querySelectorAll('.text-muted.small.text-center');
            for (var i = 0; i < empties.length; i++) empties[i].parentNode.removeChild(empties[i]);
        }

        var revCanvas = document.getElementById('dailyRevenueChart');
        if (revCanvas && data.dailyRevenue) {
            clearEmptyState(revCanvas);
            var labels = data.dailyRevenue.map(function (d) { return d.date || d.label; });
            var values = data.dailyRevenue.map(function (d) { return parseFloat(d.revenue || d.value) || 0; });
            if (values.some(function (v) { return v > 0; })) {
                chartInstances.push(new Chart(revCanvas, {
                    type: 'line',
                    data: { labels: labels, datasets: [{ label: 'Doanh thu', data: values, borderColor: '#2563eb', backgroundColor: 'rgba(37,99,235,0.1)', fill: true, tension: 0.3 }] },
                    options: { responsive: true, maintainAspectRatio: false, plugins: { legend: { display: false } }, scales: { y: { beginAtZero: true } } }
                }));
            } else {
                showEmptyState(revCanvas);
            }
        }

        var osCanvas = document.getElementById('orderStatusChart');
        if (osCanvas && data.orderStatusCounts) {
            clearEmptyState(osCanvas);
            var osLabels = [], osValues = [], osColors = [];
            var osColorMap = { CHO_XAC_NHAN: '#f97316', DA_XAC_NHAN: '#3b82f6', DANG_GIAO: '#8b5cf6', DA_GIAO: '#10b981', DA_HUY: '#ef4444', DA_HOAN_TIEN: '#6b7280', CHO_LAY_HANG: '#f59e0b' };
            Object.keys(data.orderStatusCounts).forEach(function (key) {
                if (data.orderStatusCounts[key] > 0) { osLabels.push(key); osValues.push(data.orderStatusCounts[key]); osColors.push(osColorMap[key] || '#6b7280'); }
            });
            if (osLabels.length) {
                chartInstances.push(new Chart(osCanvas, {
                    type: 'doughnut',
                    data: { labels: osLabels, datasets: [{ data: osValues, backgroundColor: osColors, borderWidth: 0 }] },
                    options: { responsive: true, maintainAspectRatio: false, plugins: { legend: { position: 'bottom', labels: { boxWidth: 12 } } } }
                }));
            } else {
                showEmptyState(osCanvas);
            }
        }

        var pmCanvas = document.getElementById('paymentMethodChart');
        if (pmCanvas && data.paymentMethodCounts) {
            clearEmptyState(pmCanvas);
            var pmLabels = [], pmValues = [], pmColors = [];
            var ci = 0;
            Object.keys(data.paymentMethodCounts).forEach(function (key) {
                if (data.paymentMethodCounts[key] > 0) { pmLabels.push(key); pmValues.push(data.paymentMethodCounts[key]); pmColors.push(colorPalette[ci % colorPalette.length]); ci++; }
            });
            if (pmLabels.length) {
                chartInstances.push(new Chart(pmCanvas, {
                    type: 'doughnut',
                    data: { labels: pmLabels, datasets: [{ data: pmValues, backgroundColor: pmColors, borderWidth: 0 }] },
                    options: { responsive: true, maintainAspectRatio: false, plugins: { legend: { position: 'bottom', labels: { boxWidth: 12 } } } }
                }));
            } else {
                showEmptyState(pmCanvas);
            }
        }
        } catch (err) {
            console.error('[analytics] Lỗi khi vẽ chart:', err);
        }
    };

    if (window.__analyticsData) {
        window.initAnalyticsCharts(window.__analyticsData);
    } else {
        console.warn('[analytics] window.__analyticsData chưa được gán trước khi admin-analytics.js chạy — có thể do điều hướng AJAX không chạy đúng script trang này.');
    }

    // Chart init khi tab dang an (display:none) co the cho kich thuoc 0;
    // khi tab duoc mo thi resize lai de chart hien dung.
    var tabs = document.querySelectorAll('#analyticsTabs .nav-link');
    for (var ti = 0; ti < tabs.length; ti++) {
        tabs[ti].addEventListener('shown.bs.tab', function () {
            for (var ci = 0; ci < chartInstances.length; ci++) {
                if (chartInstances[ci] && typeof chartInstances[ci].resize === 'function') {
                    chartInstances[ci].resize();
                }
            }
        });
    }
})();

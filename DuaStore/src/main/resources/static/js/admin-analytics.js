'use strict';
let copilotOpen = true;
function toggleCopilot() {
    copilotOpen = !copilotOpen;
    document.getElementById('copilotBody').style.display = copilotOpen ? 'block' : 'none';
    document.getElementById('copilotArrow').style.transform = copilotOpen ? 'rotate(0deg)' : 'rotate(-90deg)';
}
function askCopilot() {
    const q = document.getElementById('copilotQuery').value.trim();
    if (!q) return;
    const btn = document.getElementById('copilotBtn');
    const answerDiv = document.getElementById('copilotAnswer');
    const errorDiv = document.getElementById('copilotError');
    answerDiv.style.display = 'none';
    errorDiv.style.display = 'none';
    btn.disabled = true;
    btn.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span>Đang trả lời...';
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
    const headers = {'Content-Type': 'application/json'};
    if (csrfToken && csrfHeader) headers[csrfHeader] = csrfToken;
    fetch('/admin/phan-tich/api/copilot', {
        method: 'POST',
        headers: headers,
        body: JSON.stringify({query: q})
    })
    .then(r => r.json())
    .then(data => {
        answerDiv.textContent = data.answer;
        answerDiv.style.display = 'block';
    })
    .catch(e => {
        errorDiv.textContent = 'Lỗi kết nối, vui lòng thử lại.';
        errorDiv.style.display = 'block';
    })
    .finally(() => {
        btn.disabled = false;
        btn.innerHTML = '<i class="bi bi-send me-1"></i>Hỏi';
    });
}

function initAnalyticsCharts(data) {
    if (!data || typeof Chart === 'undefined') return;
    var colorPalette = ['#2563eb', '#f97316', '#10b981', '#8b5cf6', '#ef4444', '#6b7280'];

    var revCanvas = document.getElementById('dailyRevenueChart');
    if (revCanvas && data.dailyRevenue) {
        var labels = data.dailyRevenue.map(function(d) { return d.date || d.label; });
        var values = data.dailyRevenue.map(function(d) { return parseFloat(d.revenue || d.value) || 0; });
        new Chart(revCanvas, {
            type: 'line',
            data: { labels: labels, datasets: [{ label: 'Doanh thu', data: values, borderColor: '#2563eb', backgroundColor: 'rgba(37,99,235,0.1)', fill: true, tension: 0.3 }] },
            options: { responsive: true, maintainAspectRatio: false, plugins: { legend: { display: false } }, scales: { y: { beginAtZero: true } } }
        });
    }

    var osCanvas = document.getElementById('orderStatusChart');
    if (osCanvas && data.orderStatusCounts) {
        var osLabels = [], osValues = [], osColors = [];
        var osColorMap = { CHO_XAC_NHAN: '#f97316', DA_XAC_NHAN: '#3b82f6', DANG_GIAO: '#8b5cf6', DA_GIAO: '#10b981', DA_HUY: '#ef4444', DA_HOAN_TIEN: '#6b7280', CHO_LAY_HANG: '#f59e0b' };
        Object.keys(data.orderStatusCounts).forEach(function(key) {
            if (data.orderStatusCounts[key] > 0) { osLabels.push(key); osValues.push(data.orderStatusCounts[key]); osColors.push(osColorMap[key] || '#6b7280'); }
        });
        if (osLabels.length) {
            new Chart(osCanvas, {
                type: 'doughnut',
                data: { labels: osLabels, datasets: [{ data: osValues, backgroundColor: osColors, borderWidth: 0 }] },
                options: { responsive: true, maintainAspectRatio: false, plugins: { legend: { position: 'bottom', labels: { boxWidth: 12 } } } }
            });
        }
    }

    var pmCanvas = document.getElementById('paymentMethodChart');
    if (pmCanvas && data.paymentMethodCounts) {
        var pmLabels = [], pmValues = [], pmColors = [];
        var ci = 0;
        Object.keys(data.paymentMethodCounts).forEach(function(key) {
            if (data.paymentMethodCounts[key] > 0) { pmLabels.push(key); pmValues.push(data.paymentMethodCounts[key]); pmColors.push(colorPalette[ci % colorPalette.length]); ci++; }
        });
        if (pmLabels.length) {
            new Chart(pmCanvas, {
                type: 'doughnut',
                data: { labels: pmLabels, datasets: [{ data: pmValues, backgroundColor: pmColors, borderWidth: 0 }] },
                options: { responsive: true, maintainAspectRatio: false, plugins: { legend: { position: 'bottom', labels: { boxWidth: 12 } } } }
            });
        }
    }
}

if (window.__analyticsData) initAnalyticsCharts(window.__analyticsData);

'use strict';

(function () {
    var labels = [], values = [], prevValues = [];
    document.querySelectorAll('#revenueData span').forEach(function(el) {
        labels.push(el.dataset.label);
        values.push(Number(el.dataset.value));
    });
    document.querySelectorAll('#prevRevenueData span').forEach(function(el) {
        prevValues.push(Number(el.dataset.value));
    });
    if (labels.length) {
        var ctx = document.getElementById('revenueChart');
        if (ctx) {
            new Chart(ctx, {
                type: 'line',
                data: {
                    labels: labels,
                    datasets: [
                        {
                            label: 'Tuần này',
                            data: values,
                            backgroundColor: 'rgba(255, 107, 0, 0.08)',
                            borderColor: 'rgba(255, 107, 0, 0.9)',
                            borderWidth: 2,
                            pointBackgroundColor: 'rgba(255, 107, 0, 1)',
                            pointRadius: 3,
                            tension: 0.3,
                            fill: true
                        },
                        {
                            label: 'Tuần trước',
                            data: prevValues,
                            backgroundColor: 'rgba(148, 112, 219, 0.08)',
                            borderColor: 'rgba(148, 112, 219, 0.9)',
                            borderWidth: 2,
                            pointBackgroundColor: 'rgba(148, 112, 219, 1)',
                            pointRadius: 3,
                            tension: 0.3,
                            fill: true,
                            borderDash: [5, 5]
                        }
                    ]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: { legend: { display: false } },
                    scales: {
                        y: { beginAtZero: true, ticks: { callback: function(v) { return (v/1000).toFixed(0) + 'k'; } } }
                    }
                }
            });
        }
    }

    var pmLabels = [], pmValues = [], pmColors = [];
    var pmColorMap = { 'COD': '#10b981', 'CHUYEN_KHOAN': '#3b82f6', 'VNPAY': '#8b5cf6', 'MOMO': '#f59e0b', 'BANK_TRANSFER': '#6366f1' };
    document.querySelectorAll('#paymentData span').forEach(function(el) {
        pmLabels.push(el.dataset.method);
        pmValues.push(Number(el.dataset.count));
        pmColors.push(pmColorMap[el.dataset.method] || '#6b7280');
    });
    var pmCtx = document.getElementById('paymentChart');
    if (pmCtx && pmLabels.length) {
        new Chart(pmCtx, {
            type: 'pie',
            data: {
                labels: pmLabels,
                datasets: [{ data: pmValues, backgroundColor: pmColors }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { position: 'bottom', labels: { boxWidth: 12, padding: 12, font: { size: 11 } } }
                }
            }
        });
    }

    var mrLabels = [], mrValues = [];
    document.querySelectorAll('#monthlyRevenueData span').forEach(function(el) {
        mrLabels.push(el.dataset.label);
        mrValues.push(Number(el.dataset.value));
    });
    var mrCtx = document.getElementById('monthlyRevenueChart');
    if (mrCtx && mrLabels.length) {
        new Chart(mrCtx, {
            type: 'bar',
            data: {
                labels: mrLabels,
                datasets: [{
                    label: 'Doanh thu',
                    data: mrValues,
                    backgroundColor: 'rgba(255, 107, 0, 0.7)',
                    borderColor: 'rgba(255, 107, 0, 1)',
                    borderWidth: 1,
                    borderRadius: 4
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: { legend: { display: false } },
                scales: {
                    y: { beginAtZero: true, ticks: { callback: function(v) { return v >= 1000000 ? (v / 1000000).toFixed(0) + 'tr' : (v / 1000).toFixed(0) + 'k'; } }, grid: { color: 'rgba(0,0,0,0.05)' } },
                    x: { grid: { display: false } }
                }
            }
        });
    }
})();

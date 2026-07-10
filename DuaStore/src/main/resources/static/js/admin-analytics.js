'use strict';
document.addEventListener('DOMContentLoaded', function () {
    var colors = {
        primary: 'rgba(255, 107, 0, 1)',
        primaryBg: 'rgba(255, 107, 0, 0.15)',
        success: 'rgba(34, 197, 94, 1)',
        warning: 'rgba(234, 179, 8, 1)',
        danger: 'rgba(239, 68, 68, 1)',
        info: 'rgba(59, 130, 246, 1)',
        purple: 'rgba(124, 58, 237, 1)',
        palette: ['#ff6b00', '#22c55e', '#3b82f6', '#eab308', '#ef4444', '#7c3aed', '#06b6d4', '#f97316', '#ec4899', '#14b8a6']
    };
    var data = window.__analyticsData || {};

    var revenueData = data.dailyRevenue;
    if (revenueData && revenueData.length) {
        var labels = revenueData.map(function (d) { return d.date.substring(5); });
        var values = revenueData.map(function (d) { return Number(d.revenue); });
        var ctx = document.getElementById('dailyRevenueChart');
        if (ctx) {
            new Chart(ctx, {
                type: 'line',
                data: {
                    labels: labels,
                    datasets: [{label: 'Doanh thu', data: values, borderColor: colors.primary, backgroundColor: colors.primaryBg, fill: true, tension: 0.35, pointRadius: 4, pointBackgroundColor: '#fff', pointBorderColor: colors.primary, pointBorderWidth: 2, borderWidth: 2}]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {legend: {display: false}},
                    scales: {
                        y: {beginAtZero: true, ticks: {callback: function (v) { return v >= 1000000 ? (v / 1000000).toFixed(0) + 'tr' : (v / 1000).toFixed(0) + 'k'; }}, grid: {color: 'rgba(0,0,0,0.05)'}},
                        x: {grid: {display: false}}
                    }
                }
            });
        }
    }

    var statusData = data.orderStatusCounts;
    if (statusData) {
        var statusLabels = ['Chờ xác nhận', 'Đã xác nhận', 'Đang giao', 'Đã giao', 'Hoàn thành', 'Đã huỷ'];
        var statusKeys = ['CHO_XAC_NHAN', 'DA_XAC_NHAN', 'DANG_GIAO', 'DA_GIAO', 'DA_HOAN_THANH', 'DA_HUY'];
        var statusValues = statusKeys.map(function (k) { return Number(statusData[k] || 0); });
        var statusColors = [colors.warning, colors.info, colors.purple, '#2563eb', colors.success, colors.danger];
        var ctx3 = document.getElementById('orderStatusChart');
        if (ctx3) {
            new Chart(ctx3, {
                type: 'doughnut',
                data: {labels: statusLabels, datasets: [{data: statusValues, backgroundColor: statusColors, borderWidth: 0, hoverOffset: 6}]},
                options: {responsive: true, maintainAspectRatio: false, cutout: '60%', plugins: {legend: {position: 'bottom', labels: {padding: 12, usePointStyle: true}}}}
            });
        }
    }

    var paymentData = data.paymentMethodCounts;
    if (paymentData) {
        var payLabels = Object.keys(paymentData).map(function (k) {
            var map = {'COD': 'COD', 'BANK_TRANSFER': 'Chuyển khoản', 'MOMO': 'MoMo', 'VNPAY': 'VNPay'};
            return map[k] || k;
        });
        var payValues = Object.values(paymentData).map(function (v) { return Number(v); });
        var ctx4 = document.getElementById('paymentMethodChart');
        if (ctx4) {
            new Chart(ctx4, {
                type: 'doughnut',
                data: {labels: payLabels, datasets: [{data: payValues, backgroundColor: colors.palette, borderWidth: 0, hoverOffset: 6}]},
                options: {responsive: true, maintainAspectRatio: false, cutout: '60%', plugins: {legend: {position: 'bottom', labels: {padding: 12, usePointStyle: true}}}}
            });
        }
    }
});

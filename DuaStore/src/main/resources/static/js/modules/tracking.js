/* =====================================================
 DuaStore — Module: Order Tracking
 Dependencies: api.js
 ===================================================== */
'use strict';

window.DuaStore = window.DuaStore || {};
window.DuaStore.tracking = {};

(function () {
    var mapInstance = null;
    var markerStore = null;
    var markerCustomer = null;
    var routeLine = null;
    var pollId = null;
    var currentCode = null;

    var statusColors = {
        'CHO_XAC_NHAN': 'bg-warning',
        'DA_XAC_NHAN': 'bg-primary',
        'DANG_GIAO': 'bg-info',
        'DA_GIAO': 'bg-success',
        'DA_HOAN_THANH': 'bg-success',
        'DA_HUY': 'bg-danger',
        'DA_HOAN_TIEN': 'bg-secondary'
    };

    function init(code) {
        currentCode = code;
        document.getElementById('trackingTitle').textContent = 'Đơn hàng ' + code;
        fetchData(code);
    }

    function fetchData(code) {
        DuaStore.api.get('/tracking/api/data?code=' + encodeURIComponent(code))
            .then(function (result) {
                if (result.ok && result.data.success) {
                    render(result.data.data);
                    document.getElementById('trackingLoading').style.display = 'none';
                    document.getElementById('trackingContent').style.display = '';
                    if (pollId) clearInterval(pollId);
                    pollId = setInterval(function () { pollData(code); }, 30000);
                } else {
                    showError();
                }
            })
            .catch(function () {
                showError();
            });
    }

    function pollData(code) {
        DuaStore.api.get('/tracking/api/data?code=' + encodeURIComponent(code))
            .then(function (result) {
                if (result.ok && result.data.success) {
                    updateData(result.data.data);
                }
            });
    }

    function render(data) {
        updateMap(data);
        renderTimeline(data.timeline || []);
        updateInfo(data);
        updateStatusBadge(data.trangThaiDon, data.trangThaiDonDisplay);
        updateCarrierInfo(data);
    }

    function updateData(data) {
        renderTimeline(data.timeline || []);
        updateStatusBadge(data.trangThaiDon, data.trangThaiDonDisplay);
        updateInfo(data);
        updateCarrierInfo(data);
    }

    function updateMap(data) {
        var mapEl = document.getElementById('trackingMap');
        if (!mapEl) return;
        if (mapInstance) {
            mapInstance.remove();
            mapInstance = null;
        }
        var storeLat = data.storeLat || 20.8565;
        var storeLng = data.storeLng || 106.6756;
        var custLat = data.customerLat || storeLat;
        var custLng = data.customerLng || storeLng;

        mapInstance = L.map(mapEl, {
            center: [(storeLat + custLat) / 2, (storeLng + custLng) / 2],
            zoom: 12,
            zoomControl: true
        });
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            maxZoom: 19,
            attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
        }).addTo(mapInstance);

        var storeIcon = L.divIcon({
            html: '<div style="background:#2563eb;color:#fff;width:32px;height:32px;border-radius:50%;display:flex;align-items:center;justify-content:center;border:3px solid #fff;box-shadow:0 2px 6px rgba(0,0,0,.2);font-size:14px;"><i class="bi bi-shop"></i></div>',
            className: '',
            iconSize: [32, 32],
            iconAnchor: [16, 16]
        });
        markerStore = L.marker([storeLat, storeLng], { icon: storeIcon })
            .addTo(mapInstance)
            .bindPopup('<strong>Cửa hàng DuaStore</strong><br>' + data.customerAddress);

        var custIcon = L.divIcon({
            html: '<div style="background:#10b981;color:#fff;width:32px;height:32px;border-radius:50%;display:flex;align-items:center;justify-content:center;border:3px solid #fff;box-shadow:0 2px 6px rgba(0,0,0,.2);font-size:14px;"><i class="bi bi-geo-alt-fill"></i></div>',
            className: '',
            iconSize: [32, 32],
            iconAnchor: [16, 16]
        });
        markerCustomer = L.marker([custLat, custLng], { icon: custIcon })
            .addTo(mapInstance)
            .bindPopup('<strong>Khách hàng</strong><br>' + (data.snapDiaChi || ''));

        routeLine = L.polyline([[storeLat, storeLng], [custLat, custLng]], {
            color: '#2563eb',
            weight: 3,
            opacity: 0.6,
            dashArray: '8, 8'
        }).addTo(mapInstance);

        var bounds = L.latLngBounds([storeLat, storeLng], [custLat, custLng]);
        mapInstance.fitBounds(bounds, { padding: [40, 40] });

        if (data.trangThaiDon === 'DANG_GIAO') {
            var pulseIcon = L.divIcon({
                html: '<div style="width:16px;height:16px;background:#2563eb;border-radius:50%;border:3px solid #fff;box-shadow:0 0 0 4px rgba(37,99,235,.4);animation:pulse 2s infinite;"></div>',
                className: '',
                iconSize: [16, 16],
                iconAnchor: [8, 8]
            });
            var midLat = (storeLat + custLat) / 2;
            var midLng = (storeLng + custLng) / 2;
            L.marker([midLat, midLng], { icon: pulseIcon })
                .addTo(mapInstance)
                .bindPopup('Đang giao hàng');
        }

        setTimeout(function () { if (mapInstance) mapInstance.invalidateSize(); }, 200);
        setTimeout(function () { if (mapInstance) mapInstance.invalidateSize(); }, 500);
    }

    function renderTimeline(events) {
        var list = document.getElementById('timelineList');
        if (!list) return;
        if (!events || !events.length) {
            list.innerHTML = '<li class="text-muted small py-2">Chưa có cập nhật</li>';
            return;
        }
        list.innerHTML = '';
        events.forEach(function (ev, idx) {
            var isLast = idx === events.length - 1;
            var isCancel = ev.icon === 'CANCEL_ORDER' || ev.icon === 'REFUND_ORDER';
            var dotClass = isCancel ? 'cancel' : ev.active ? 'active' : ev.completed ? 'done' : '';
            var labelClass = isCancel ? 'cancel' : ev.active ? 'active' : ev.completed ? 'done' : '';
            var timeStr = '';
            if (ev.time) {
                var d = new Date(ev.time);
                timeStr = d.toLocaleDateString('vi-VN') + ' ' + d.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
            }
            var li = document.createElement('li');
            li.className = 'tl-item';
            li.innerHTML = '<span class="tl-dot ' + dotClass + '"></span>'
                + '<div class="tl-label ' + labelClass + '">' + escHtml(ev.status || '') + '</div>'
                + (timeStr ? '<div class="tl-time">' + timeStr + '</div>' : '')
                + (ev.description ? '<div class="tl-desc">' + escHtml(ev.description) + '</div>' : '');
            list.appendChild(li);
        });
    }

    function updateInfo(data) {
        setText('infoMaDon', data.maDon || '');
        setText('infoNgayDat', data.ngayDat || '');
        setText('infoNguoiNhan', data.snapTenNguoiNhan || '');
        setText('infoSDT', data.snapSoDienThoai || '');
        setText('infoDiaChi', data.snapDiaChi || '');
        setText('infoPhuongThucTT', data.phuongThucTTDisplay || data.phuongThucTT || '');
        setText('infoTongTien', data.tongThanhToan ? Number(data.tongThanhToan).toLocaleString('vi-VN') + '₫' : '');
        if (data.ghiChu) {
            document.getElementById('ghiChuRow').style.display = '';
            setText('infoGhiChu', data.ghiChu);
        } else {
            document.getElementById('ghiChuRow').style.display = 'none';
        }
    }

    function updateStatusBadge(status, display) {
        var el = document.getElementById('statusBadge');
        if (!el) return;
        el.textContent = display || status || '';
        el.className = 'badge ' + (statusColors[status] || 'bg-secondary');
        if (status === 'DANG_GIAO') el.classList.add('live-badge');
    }

    function updateCarrierInfo(data) {
        var card = document.getElementById('carrierInfoCard');
        if (!card) return;
        if (data.carrierName && data.carrierName !== '') {
            card.style.display = '';
            setText('carrierNameEl', data.shippingCarrier ? data.carrierName + ' (' + data.shippingCarrier + ')' : data.carrierName);
            if (data.maVanDon) {
                setText('maVanDonEl', data.maVanDon);
                document.getElementById('maVanDonEl').style.display = '';
                if (data.carrierTrackingUrl) {
                    document.getElementById('carrierLink').href = data.carrierTrackingUrl;
                    document.getElementById('carrierLinkContainer').style.display = '';
                } else {
                    document.getElementById('carrierLinkContainer').style.display = 'none';
                }
            } else {
                document.getElementById('maVanDonEl').style.display = 'none';
                document.getElementById('carrierLinkContainer').style.display = 'none';
            }
        } else {
            card.style.display = 'none';
        }
    }

    function showError() {
        document.getElementById('trackingLoading').style.display = 'none';
        document.getElementById('trackingContent').style.display = 'none';
        document.getElementById('trackingError').style.display = '';
    }

    function setText(id, val) {
        var el = document.getElementById(id);
        if (el) el.textContent = val || '';
    }

    function escHtml(str) {
        if (!str) return '';
        var div = document.createElement('div');
        div.textContent = str;
        return div.innerHTML;
    }

    window.DuaStore.tracking.init = init;
})();

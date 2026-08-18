'use strict';

/* ── Real-time staff notification via WebSocket ──
 * Server PUSH ngay thong bao moi (don moi, lien he...) toi cac admin dang mo trang.
 * Neu WS khong mo duoc (tường lửa/VPN/proxy), van giu poll 30s trong admin-base.js
 * lam fallback — ung dung khong vo.
 */
(function() {
    var WS_URL = null;
    var socket = null;
    var reconnectDelay = 1000;
    var heartbeatTimer = null;
    var closingByUs = false;
    var connected = false;

    function getBadge() {
        return document.getElementById('staffNotifBadge');
    }

    function showToast(msg, linkHref) {
        var container = document.getElementById('wsNotifToastContainer');
        if (!container) {
            container = document.createElement('div');
            container.id = 'wsNotifToastContainer';
            container.style.cssText = 'position:fixed;bottom:20px;right:20px;z-index:20000;max-width:360px;';
            document.body.appendChild(container);
        }
        var toast = document.createElement('div');
        toast.style.cssText = 'background:#fff;border:1px solid #e0e0e0;border-left:4px solid #0d6efd;'
            + 'border-radius:10px;box-shadow:0 8px 30px rgba(0,0,0,.14);padding:12px 14px;margin-bottom:8px;'
            + 'display:flex;align-items:flex-start;gap:8px;cursor:pointer;';
        toast.setAttribute('role', 'status');
        var icon = document.createElement('span');
        icon.textContent = '🔔';
        icon.style.cssText = 'flex-shrink:0;font-size:16px;';
        var body = document.createElement('div');
        body.style.cssText = 'flex-grow:1;min-width:0;';
        var title = document.createElement('div');
        title.textContent = 'Th\u00f4ng b\u00e1o m\u1edbi';
        title.style.cssText = 'font-weight:700;font-size:13px;color:#333;margin-bottom:3px;';
        var text = document.createElement('div');
        text.textContent = Array.isArray(msg) ? msg.join(' ') : (msg || '');
        text.style.cssText = 'font-size:13px;color:#555;word-break:break-word;';

        body.appendChild(title);
        if (linkHref) {
            var link = document.createElement('a');
            link.href = linkHref;
            link.textContent = 'Xem ngay →';
            link.style.cssText = 'font-size:12px;color:#0d6efd;text-decoration:none;margin-top:3px;display:inline-block;';
            body.appendChild(link);
        }
        body.appendChild(text);
        toast.appendChild(icon);
        toast.appendChild(body);
        container.appendChild(toast);
        var closeBtn = document.createElement('span');
        closeBtn.textContent = '×';
        closeBtn.style.cssText = 'flex-shrink:0;font-weight:700;color:#999;cursor:pointer;';
        closeBtn.addEventListener('click', function() { toast.remove(); });
        toast.appendChild(closeBtn);
        setTimeout(function() {
            toast.style.opacity = '0';
            toast.style.transition = 'opacity .4s';
            setTimeout(function () { toast.remove(); }, 450);
        }, 6000);
        if (linkHref) {
            toast.addEventListener('click', function () { toast.remove(); });
        }
    }

    function bumpBadge() {
        var badge = getBadge();
        if (!badge) return;
        var num = parseInt(badge.textContent, 10);
        if (isNaN(num)) num = 0;
        badge.textContent = num + 1;
        badge.classList.remove('d-none');
    }

    function openSocket() {
        if (connected || (socket && (socket.readyState === WebSocket.OPEN || socket.readyState === WebSocket.CONNECTING))) {
            return;
        }
        if (!WS_URL) {
            WS_URL = (location.protocol === 'https:' ? 'wss://' : 'ws://') + location.host + '/ws/admin/notifications';
        }
        try {
            socket = new WebSocket(WS_URL);
        } catch (e) {
            scheduleReconnect();
            return;
        }

        socket.onopen = function () {
            connected = true;
            reconnectDelay = 1000;
            heartbeatTimer = setInterval(function () {
                if (socket && socket.readyState === WebSocket.OPEN) {
                    socket.send('PING');
                }
            }, 25000);
        };

        socket.onmessage = function (event) {
            var data = null;
            try { data = JSON.parse(event.data); } catch (e) { return; }
            if (data && data.type === 'ADMIN_NOTIFICATION') {
                bumpBadge();
                showToast(data.content, data.linkUrl || null);
            }
        };

        socket.onclose = function () {
            connected = false;
            if (heartbeatTimer) { clearInterval(heartbeatTimer); heartbeatTimer = null; }
            if (!closingByUs) scheduleReconnect();
        };
        socket.onerror = function () {
            try { socket.close(); } catch (e) { /* noop */ }
        };
    }

    function scheduleReconnect() {
        setTimeout(openSocket, reconnectDelay);
        reconnectDelay = Math.min(reconnectDelay * 2, 5000);
    }

    /* API cho admin-base.js poll fallback: bo qua poll khi socket song. */
    window.DuaAdminNotifSocket = {
        connect: openSocket,
        isConnected: function () { return connected && socket && socket.readyState === WebSocket.OPEN; }
    };

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', openSocket);
    } else {
        openSocket();
    }
})();
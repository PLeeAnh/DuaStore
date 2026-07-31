'use strict';

(function () {
    var POPUP_KEY = 'duastore_popup_';

    function shouldShow(data) {
        if (!data.active || !data.imageUrl) return false;

        var mode = data.displayMode;
        var id = data.id;

        if (mode === 'ONCE') {
            return !localStorage.getItem(POPUP_KEY + 'once_' + id);
        }

        if (mode === 'EVERY_VISIT') {
            var sessionKey = POPUP_KEY + 'session_' + id;
            if (sessionStorage.getItem(sessionKey)) return false;
            return true;
        }

        if (mode === 'INTERVAL') {
            var lastKey = POPUP_KEY + 'last_' + id;
            var last = localStorage.getItem(lastKey);
            if (!last) return true;
            var elapsed = (Date.now() - parseInt(last, 10)) / 60000;
            return elapsed >= (data.intervalMinutes || 60);
        }

        return false;
    }

    function markShown(data) {
        var mode = data.displayMode;
        var id = data.id;

        if (mode === 'ONCE') {
            localStorage.setItem(POPUP_KEY + 'once_' + id, '1');
        } else if (mode === 'EVERY_VISIT') {
            sessionStorage.setItem(POPUP_KEY + 'session_' + id, '1');
        } else if (mode === 'INTERVAL') {
            localStorage.setItem(POPUP_KEY + 'last_' + id, String(Date.now()));
        }
    }

    function showPopup(data) {
        var overlay = document.createElement('div');
        overlay.id = 'popupBannerOverlay';
        overlay.style.cssText = 'position:fixed;top:0;left:0;width:100%;height:100%;background:rgba(0,0,0,.55);z-index:10000;display:flex;align-items:center;justify-content:center;animation:fadeIn .3s';

        var wrapper = document.createElement('div');
        wrapper.style.cssText = 'position:relative;max-width:500px;width:90%;animation:popupScale .3s';

        var closeBtn = document.createElement('button');
        closeBtn.innerHTML = '&times;';
        closeBtn.style.cssText = 'position:absolute;top:-12px;right:-12px;width:32px;height:32px;border-radius:50%;border:none;background:#fff;color:#333;font-size:1.3rem;display:flex;align-items:center;justify-content:center;cursor:pointer;box-shadow:0 2px 8px rgba(0,0,0,.2);z-index:1';
        closeBtn.onclick = function () {
            overlay.remove();
            markShown(data);
        };

        var img = document.createElement('img');
        img.src = data.imageUrl;
        img.style.cssText = 'width:100%;border-radius:12px;display:block;cursor:pointer';
        img.onerror = function () { overlay.remove(); };

        if (data.linkUrl) {
            img.onclick = function () {
                markShown(data);
                window.location.href = data.linkUrl;
            };
        } else {
            img.style.cursor = 'default';
            img.onclick = function () {
                overlay.remove();
                markShown(data);
            };
        }

        wrapper.appendChild(closeBtn);
        wrapper.appendChild(img);
        overlay.appendChild(wrapper);
        document.body.appendChild(overlay);

        overlay.addEventListener('click', function (e) {
            if (e.target === overlay) {
                overlay.remove();
                markShown(data);
            }
        });

        document.addEventListener('keydown', function handler(e) {
            if (e.key === 'Escape') {
                overlay.remove();
                markShown(data);
                document.removeEventListener('keydown', handler);
            }
        });
    }

    function injectStyles() {
        var style = document.createElement('style');
        style.textContent = '@keyframes fadeIn{from{opacity:0}to{opacity:1}}@keyframes popupScale{from{transform:scale(.85);opacity:0}to{transform:scale(1);opacity:1}}';
        document.head.appendChild(style);
    }

    function init() {
        fetch('/api/popup-banner/active')
            .then(function (r) { return r.json(); })
            .then(function (data) {
                if (shouldShow(data)) {
                    injectStyles();
                    setTimeout(function () { showPopup(data); }, 800);
                }
            })
            .catch(function () {});
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();

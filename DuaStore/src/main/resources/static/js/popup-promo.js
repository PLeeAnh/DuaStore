(function () {
    'use strict';

    // Chỉ chạy trên trang chủ
    var promoOverlay = document.getElementById('dsPromoPopupOverlay');
    if (!promoOverlay) return;

    var mode = promoOverlay.dataset.mode || 'once';
    var interval = parseInt(promoOverlay.dataset.interval) || 60; // phút

    var STORAGE_KEY_SHOWN = 'dsPromoPopupShown';
    var STORAGE_KEY_TIME = 'dsPromoPopupTime';

    function shouldShow() {
        if (mode === 'always') {
            // Hiện mỗi session (đóng tab mở lại sẽ hiện)
            return !sessionStorage.getItem(STORAGE_KEY_SHOWN);
        }
        if (mode === 'timed') {
            // Hiện lại sau N phút
            var lastShown = localStorage.getItem(STORAGE_KEY_TIME);
            if (!lastShown) return true;
            var elapsed = (Date.now() - parseInt(lastShown)) / 1000 / 60; // phút
            return elapsed >= interval;
        }
        // mode === 'once': Hiện 1 lần duy nhất
        return !localStorage.getItem(STORAGE_KEY_SHOWN);
    }

    function showPopup() {
        promoOverlay.style.display = 'flex';
        document.body.style.overflow = 'hidden';
        // Animation
        requestAnimationFrame(function () {
            promoOverlay.classList.add('ds-promo-popup-visible');
        });
    }

    function hidePopup() {
        promoOverlay.classList.remove('ds-promo-popup-visible');
        promoOverlay.classList.add('ds-promo-popup-hiding');

        setTimeout(function () {
            promoOverlay.style.display = 'none';
            promoOverlay.classList.remove('ds-promo-popup-hiding');
            document.body.style.overflow = '';
        }, 300);

        // Lưu trạng thái đã hiện
        if (mode === 'once') {
            localStorage.setItem(STORAGE_KEY_SHOWN, '1');
        } else if (mode === 'always') {
            sessionStorage.setItem(STORAGE_KEY_SHOWN, '1');
        } else if (mode === 'timed') {
            localStorage.setItem(STORAGE_KEY_TIME, Date.now().toString());
        }
    }

    if (!shouldShow()) return;

    // Hiện popup sau 1.5s
    setTimeout(showPopup, 1500);

    // Nút đóng (X)
    var closeBtn = document.getElementById('dsPromoPopupClose');
    if (closeBtn) {
        closeBtn.addEventListener('click', function (e) {
            e.stopPropagation();
            hidePopup();
        });
    }

    // Click overlay ngoài ảnh để đóng
    promoOverlay.addEventListener('click', function (e) {
        if (e.target === promoOverlay) {
            hidePopup();
        }
    });

    // Đóng bằng phím Escape
    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape' && promoOverlay.style.display === 'flex') {
            hidePopup();
        }
    });
})();

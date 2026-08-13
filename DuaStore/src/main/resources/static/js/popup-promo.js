(function () {
    'use strict';

    var overlay = document.getElementById('dsPromoPopupOverlay');
    if (!overlay) return;

    var mode     = overlay.dataset.mode     || 'once';
    var interval = parseInt(overlay.dataset.interval) || 60;

    var KEY_SHOWN = 'dsPromoShown';
    var KEY_TIME  = 'dsPromoTime';

    function shouldShow() {
        if (mode === 'always') return !sessionStorage.getItem(KEY_SHOWN);
        if (mode === 'timed') {
            var last = localStorage.getItem(KEY_TIME);
            if (!last) return true;
            return (Date.now() - parseInt(last)) / 60000 >= interval;
        }
        return !localStorage.getItem(KEY_SHOWN);
    }

    function show() {
        overlay.style.display = 'flex';
        document.body.style.overflow = 'hidden';
        requestAnimationFrame(function () {
            overlay.classList.add('ds-promo-popup-visible');
        });
    }

    function hide() {
        overlay.classList.remove('ds-promo-popup-visible');
        overlay.classList.add('ds-promo-popup-hiding');
        setTimeout(function () {
            overlay.style.display = 'none';
            overlay.classList.remove('ds-promo-popup-hiding');
            document.body.style.overflow = '';
        }, 300);

        if (mode === 'once')   localStorage.setItem(KEY_SHOWN, '1');
        else if (mode === 'always') sessionStorage.setItem(KEY_SHOWN, '1');
        else if (mode === 'timed')  localStorage.setItem(KEY_TIME, Date.now().toString());
    }

    if (!shouldShow()) return;

    setTimeout(show, 1500);

    var closeBtn = document.getElementById('dsPromoPopupClose');
    if (closeBtn) closeBtn.addEventListener('click', function (e) { e.stopPropagation(); hide(); });
    overlay.addEventListener('click', function (e) { if (e.target === overlay) hide(); });
    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape' && overlay.style.display === 'flex') hide();
    });
})();

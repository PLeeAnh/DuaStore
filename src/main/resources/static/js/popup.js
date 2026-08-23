(function () {
    var shown = sessionStorage.getItem('dsPopupShown');
    if (shown) return;

    var overlay = document.getElementById('dsPopupOverlay');
    if (!overlay) return;

    function showPopup() {
        overlay.style.display = 'flex';
        document.body.style.overflow = 'hidden';
    }

    function hidePopup() {
        overlay.style.display = 'none';
        document.body.style.overflow = '';
        sessionStorage.setItem('dsPopupShown', '1');
    }

    setTimeout(showPopup, 4000);

    document.getElementById('dsPopupClose').addEventListener('click', hidePopup);
    document.getElementById('dsPopupSkip').addEventListener('click', hidePopup);
    overlay.addEventListener('click', function (e) {
        if (e.target === overlay) hidePopup();
    });

    document.getElementById('dsPopupForm').addEventListener('submit', function (e) {
        e.preventDefault();
        var email = this.querySelector('.ds-popup-input').value;
        if (!email) return;
        fetch('/api/subscribe', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
            body: 'email=' + encodeURIComponent(email)
        }).then(function (r) {
            hidePopup();
            if (typeof DuaStore === 'undefined' || !DuaStore.toast) return;
            if (r.ok) {
                DuaStore.toast.success('Đăng ký thành công! Cảm ơn bạn.');
            } else {
                DuaStore.toast.error('Không thể đăng ký. Vui lòng thử lại sau.');
            }
        }).catch(function () {
            hidePopup();
        });
    });
})();

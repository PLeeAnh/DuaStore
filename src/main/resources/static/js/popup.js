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
            var toast = document.getElementById('dsToast');
            if (!toast) return;
            if (r.ok) {
                toast.textContent = 'Đăng ký thành công! Cảm ơn bạn.';
            } else {
                toast.textContent = 'Không thể đăng ký. Vui lòng thử lại sau.';
            }
            toast.style.opacity = '1';
            setTimeout(function () { toast.style.opacity = '0'; }, 3000);
        }).catch(function () {
            hidePopup();
        });
    });
})();

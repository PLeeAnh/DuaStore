(function () {
    function initSlider(el) {
        var viewport = el.querySelector('.ds-slider-viewport');
        var track = el.querySelector('.ds-slider-track');
        var dots = el.querySelector('.ds-slider-dots');
        var wrap = el.querySelector('.ds-slider-wrap');
        var prevBtn = wrap ? wrap.querySelector('.ds-slider-arrow-prev') : null;
        var nextBtn = wrap ? wrap.querySelector('.ds-slider-arrow-next') : null;
        var pageText = el.querySelector('.ds-slider-pagination-text');
        var pgCurrent = pageText ? pageText.querySelector('.ds-pg-current') : null;
        var pgTotal = pageText ? pageText.querySelector('.ds-pg-total') : null;
        if (!viewport || !track) return;
        var items = Array.from(track.children);
        if (items.length === 0) return;
        var currentPage = 0;
        var totalPages = 1;

        function goTo(page) {
            if (page < 0 || page >= totalPages || totalPages <= 1) return;
            currentPage = page;
            track.style.transform = 'translateX(-' + (page * viewport.offsetWidth) + 'px)';
            if (dots) {
                var allDots = dots.querySelectorAll('.ds-slider-dot');
                for (var j = 0; j < allDots.length; j++) { allDots[j].classList.toggle('active', j === page); }
            }
            if (prevBtn) prevBtn.style.display = page > 0 ? 'flex' : 'none';
            if (nextBtn) nextBtn.style.display = page < totalPages - 1 ? 'flex' : 'none';
            if (pgCurrent) pgCurrent.textContent = page + 1;
        }

        function recalc() {
            var vw = viewport.offsetWidth;
            if (!vw) return;
            var gap = 20;
            var iw = items[0].offsetWidth;
            var targetPerPage = parseInt(el.dataset.itemsPerPage) || 0;
            var perPage;
            if (targetPerPage > 0) { perPage = targetPerPage; }
            else if (iw) { perPage = Math.max(1, Math.floor((vw + gap) / (iw + gap))); }
            else { perPage = 1; }
            var basis = 'calc((100% - ' + (gap * (perPage - 1)) + 'px) / ' + perPage + ')';
            for (var k = 0; k < items.length; k++) { items[k].style.flex = '0 0 ' + basis; }
            totalPages = Math.ceil(items.length / perPage);
            el.classList.toggle('ds-slider-single', totalPages <= 1);
            if (dots) dots.innerHTML = '';
            if (totalPages <= 1) {
                track.style.transform = '';
                if (prevBtn) prevBtn.style.display = 'none';
                if (nextBtn) nextBtn.style.display = 'none';
                if (pgCurrent) pgCurrent.textContent = '1';
                if (pgTotal) pgTotal.textContent = '1';
                return;
            }
            if (pgTotal) pgTotal.textContent = totalPages;
            for (var i = 0; i < totalPages; i++) {
                var dot = document.createElement('button');
                dot.className = 'ds-slider-dot' + (i === 0 ? ' active' : '');
                dot.setAttribute('aria-label', 'Trang ' + (i + 1));
                dot.addEventListener('click', function (p) { return function () { goTo(p); }; }(i));
                if (dots) dots.appendChild(dot);
            }
            currentPage = 0;
            goTo(0);
        }

        if (prevBtn) prevBtn.addEventListener('click', function () { goTo(currentPage - 1); });
        if (nextBtn) nextBtn.addEventListener('click', function () { goTo(currentPage + 1); });

        recalc();
        var timer;
        window.addEventListener('resize', function () { clearTimeout(timer); timer = setTimeout(recalc, 200); });
    }
    document.querySelectorAll('.ds-slider-section').forEach(initSlider);
})();

document.addEventListener('DOMContentLoaded', function () {
    var cartItems = document.querySelectorAll('#cart-items-container .popup-item');
    cartItems.forEach(function (item) {
        var qtyEl = item.querySelector('input[id^="popup-qty-"]');
        var qty = qtyEl ? qtyEl.value : '1';
    });
});

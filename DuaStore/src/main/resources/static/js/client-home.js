(function () {
    var browseTabs = document.querySelectorAll('.ds-browse-tab');
    var browseList = document.getElementById('browseList');
    var browsePreview = document.getElementById('browsePreview');
    if (browseTabs.length && browseList && browsePreview) {
        var browseRows = Array.from(browseList.querySelectorAll('.ds-browse-row'));

        browseTabs.forEach(function (tab) {
            tab.addEventListener('click', function () {
                browseTabs.forEach(function (t) { t.classList.remove('active'); });
                tab.classList.add('active');
                var key = tab.dataset.tab;
                browseRows.sort(function (a, b) {
                    if (key === 'fav') return (parseInt(b.dataset.favCount) || 0) - (parseInt(a.dataset.favCount) || 0);
                    if (key === 'sold') return (parseInt(b.dataset.soldCount) || 0) - (parseInt(a.dataset.soldCount) || 0);
                    if (key === 'new') return (b.dataset.daysOld || '').localeCompare(a.dataset.daysOld || '');
                    return 0;
                });
                browseRows.forEach(function (r) { browseList.appendChild(r); });
                if (browseRows.length) activateBrowseRow(browseRows[0]);

                var seeMoreBtns = document.querySelectorAll('.ds-see-more-btn');
                seeMoreBtns.forEach(function (btn) { btn.style.display = btn.dataset.tab === key ? 'block' : 'none'; });
            });
        });

        var activeTab = document.querySelector('.ds-browse-tab.active');
        if (activeTab && browseRows.length) {
            var initKey = activeTab.dataset.tab;
            browseRows.sort(function (a, b) {
                if (initKey === 'fav') return (parseInt(b.dataset.favCount) || 0) - (parseInt(a.dataset.favCount) || 0);
                if (initKey === 'sold') return (parseInt(b.dataset.soldCount) || 0) - (parseInt(a.dataset.soldCount) || 0);
                if (initKey === 'new') return (b.dataset.daysOld || '').localeCompare(a.dataset.daysOld || '');
                return 0;
            });
            browseRows.forEach(function (r) { browseList.appendChild(r); });
            activateBrowseRow(browseRows[0]);
        }

        var isTouch = 'ontouchstart' in window || navigator.maxTouchPoints > 0;
        browseRows.forEach(function (row) {
            row.addEventListener('mouseenter', function () { activateBrowseRow(row); });
            if (isTouch) {
                var tapped = false;
                row.addEventListener('click', function () {
                    if (tapped) {
                        var href = row.dataset.href;
                        if (href) window.location.href = href;
                    } else {
                        tapped = true;
                        activateBrowseRow(row);
                        setTimeout(function () { tapped = false; }, 1500);
                    }
                });
            } else {
                row.addEventListener('click', function () {
                    var href = row.dataset.href;
                    if (href) window.location.href = href;
                });
            }
        });

        function activateBrowseRow(row) {
            browseRows.forEach(function (r) { r.classList.remove('active'); });
            row.classList.add('active');
            document.getElementById('browsePreviewName').textContent = row.dataset.name;

            var link = document.getElementById('browsePreviewLink');
            if (link) link.href = row.dataset.href || '#';

            var thumb = document.querySelector('#browsePreviewThumb img');
            if (thumb) {
                thumb.src = row.dataset.img || '/images/no-image.png';
                thumb.alt = row.dataset.name || '';
            }

            var ratingVal = row.dataset.rating || '';
            var ratingCount = parseInt(row.dataset.ratingCount) || 0;
            var ratingLabel = '';
            if (ratingCount > 0 && ratingVal) {
                var r = parseFloat(ratingVal);
                if (r >= 4.5) ratingLabel = 'Tốt';
                else if (r >= 3.5) ratingLabel = 'Khá';
                else if (r >= 2.5) ratingLabel = 'Bình thường';
                else if (r >= 1.5) ratingLabel = 'Kém';
                else ratingLabel = 'Rất kém';
            }
            document.getElementById('browsePreviewRatingValue').textContent = ratingCount > 0 ? ratingVal : '';
            document.getElementById('browsePreviewRatingLabel').textContent = ratingCount > 0 ? ratingLabel : 'Chưa ai đánh giá';
            document.getElementById('browsePreviewRatingCount').textContent = ratingCount > 0 ? '(' + ratingCount + ')' : '(0)';

            var catTag = document.getElementById('browsePreviewCatTag');
            if (catTag) catTag.textContent = row.dataset.cat || '';

            var container = document.getElementById('browsePreviewImages');
            var galleryStr = row.dataset.gallery || '';
            var gallery = galleryStr ? galleryStr.split(',').filter(function (s) { return s.trim(); }) : [];
            if (gallery.length === 0) gallery = [row.dataset.img || '/images/no-image.png'];
            container.innerHTML = '';
            gallery.slice(0, 6).forEach(function (url) {
                var img = document.createElement('img');
                img.className = 'ds-browse-preview-img';
                img.src = url;
                img.alt = '';
                img.addEventListener('mouseenter', function () { if (thumb) thumb.src = url; });
                container.appendChild(img);
            });
        }
    }
})();

(function () {
    function initSlider(el) {
        var viewport = el.querySelector('.ds-slider-viewport');
        var track = el.querySelector('.ds-slider-track');
        var dots = el.querySelector('.ds-slider-dots');
        var wrap = el.querySelector('.ds-slider-wrap');
        var prevBtn = wrap ? wrap.querySelector('.ds-slider-arrow-prev') : null;
        var nextBtn = wrap ? wrap.querySelector('.ds-slider-arrow-next') : null;
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
            if (dots) dots.innerHTML = '';
            if (totalPages <= 1) {
                track.style.transform = '';
                if (prevBtn) prevBtn.style.display = 'none';
                if (nextBtn) nextBtn.style.display = 'none';
                return;
            }
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

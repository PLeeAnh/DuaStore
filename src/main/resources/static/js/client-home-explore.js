/* ── Infinite scroll: Khám phá sản phẩm ── */
(function () {
    var grid = document.getElementById('dsExploreGrid');
    var loader = document.getElementById('dsExploreLoader');
    var endMsg = document.getElementById('dsExploreEnd');
    if (!grid || !loader || !endMsg) return;

    var page = 0;
    var loading = false;
    var hasNext = true;
    var PAGE_SIZE = 8;

    function renderCard(item) {
        var a = document.createElement('a');
        a.href = '/san-pham/' + item.id;
        a.className = 'ds-explore-card';
        var price = '';
        if (item.giaKhuyenMai) {
            price = '<span class="ds-explore-card-price">' + Number(item.giaKhuyenMai).toLocaleString('vi-VN') + '₫</span>';
            if (item.giaGoc && item.giaGoc > item.giaKhuyenMai) {
                price += '<span class="ds-explore-card-old">' + Number(item.giaGoc).toLocaleString('vi-VN') + '₫</span>';
            }
        } else if (item.giaGoc) {
            price = '<span class="ds-explore-card-price">' + Number(item.giaGoc).toLocaleString('vi-VN') + '₫</span>';
        }
        a.innerHTML =
            '<div class="ds-explore-card-img">' +
                '<img src="' + (item.hinhAnhChinh || item.hinhAnh || '/images/no-image.png') + '" alt="" loading="lazy" />' +
            '</div>' +
            '<div class="ds-explore-card-body">' +
                '<div class="ds-explore-card-name">' + (item.tenSanPham || '') + '</div>' +
                price +
            '</div>';
        return a;
    }

    function loadMore() {
        if (loading || !hasNext) return;
        loading = true;
        loader.style.display = '';

        fetch('/api/products/explore?page=' + page + '&size=' + PAGE_SIZE)
            .then(function (r) { return r.json(); })
            .then(function (data) {
                loader.style.display = 'none';
                loading = false;
                if (!data.items || data.items.length === 0) {
                    hasNext = false;
                    endMsg.style.display = '';
                    endMsg.textContent = 'Bạn đã xem hết sản phẩm rồi!';
                    return;
                }
                data.items.forEach(function (item) {
                    grid.appendChild(renderCard(item));
                });
                page++;
                hasNext = data.hasNext;
                if (!hasNext) {
                    endMsg.style.display = '';
                    endMsg.textContent = 'Bạn đã xem hết sản phẩm rồi!';
                }
            })
            .catch(function () {
                loader.style.display = 'none';
                loading = false;
            });
    }

    /* IntersectionObserver — load when sentinel is visible */
    var sentinel = document.createElement('div');
    sentinel.id = 'dsExploreSentinel';
    sentinel.style.height = '1px';
    grid.parentNode.insertBefore(sentinel, endMsg);

    if ('IntersectionObserver' in window) {
        var obs = new IntersectionObserver(function (entries) {
            if (entries[0].isIntersecting) loadMore();
        }, { rootMargin: '300px' });
        obs.observe(sentinel);
    } else {
        window.addEventListener('scroll', function () {
            if (loading || !hasNext) return;
            var rect = sentinel.getBoundingClientRect();
            if (rect.top < window.innerHeight + 300) loadMore();
        });
    }

    /* Initial load */
    loadMore();
})();

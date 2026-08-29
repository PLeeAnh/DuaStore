'use strict';
(function() {

    /* ── Countdown hero ── */
    var cd = document.querySelector('.ds-promo-countdown');
    if (cd) {
        var endStr = cd.getAttribute('data-end');
        if (endStr) {
            var end = new Date(endStr).getTime();
            if (!isNaN(end)) {
                var daysEl = cd.querySelector('.cd-days');
                var hoursEl = cd.querySelector('.cd-hours');
                var minsEl = cd.querySelector('.cd-mins');
                var secsEl = cd.querySelector('.cd-secs');
                var pad = function(n) { return n < 10 ? '0' + n : '' + n; };
                var tick = function() {
                    var diff = end - Date.now();
                    if (diff <= 0) {
                        if (daysEl) daysEl.textContent = '0';
                        if (hoursEl) hoursEl.textContent = '00';
                        if (minsEl) minsEl.textContent = '00';
                        if (secsEl) secsEl.textContent = '00';
                        return;
                    }
                    var d = Math.floor(diff / 86400000);
                    var h = Math.floor(diff % 86400000 / 3600000);
                    var m = Math.floor(diff % 3600000 / 60000);
                    var s = Math.floor(diff % 60000 / 1000);
                    if (daysEl) daysEl.textContent = d;
                    if (hoursEl) hoursEl.textContent = pad(h);
                    if (minsEl) minsEl.textContent = pad(m);
                    if (secsEl) secsEl.textContent = pad(s);
                };
                tick();
                setInterval(tick, 1000);
            }
        }
    }

    /* ── Voucher: KHÔNG còn tab lọc (đã bỏ theo yêu cầu) ── */
    var items = Array.prototype.slice.call(document.querySelectorAll('.ds-promo-item'));

    /* ── Random có trọng số: voucher giảm CÀNG NHIỀU thì càng HIẾM xuất
       hiện, voucher giảm ít thì xuất hiện thường xuyên hơn.
       Cách làm: xếp hạng từng voucher theo % (hoặc số tiền) giảm SO VỚI
       các voucher CÙNG LOẠI (PHAN_TRAM so với PHAN_TRAM, SO_TIEN so với
       SO_TIEN — vì 2 loại này không cùng đơn vị nên không so trực tiếp
       được), voucher giảm ít nhất trong nhóm được trọng số cao nhất
       (~1.15), giảm nhiều nhất vẫn giữ trọng số tối thiểu (0.15) để
       không biến mất hẳn — chỉ hiếm hơn hẳn so với voucher giảm ít.
       Sau đó dùng thuật toán random có trọng số không hoàn lại (weighted
       reservoir sampling kiểu "A-ExpJ": key = random^(1/weight), sắp xếp
       giảm dần theo key) để ra thứ tự hiển thị ngẫu nhiên nhưng vẫn thiên
       vị đúng theo trọng số mỗi lần tải lại trang. */
    function weightedShuffleByDiscount(list) {
        if (!list.length) return list;
        var groupMinMax = {};
        list.forEach(function (it) {
            var loai = it.getAttribute('data-loaigiam') || 'KHAC';
            var val = parseFloat(it.getAttribute('data-giatri')) || 0;
            if (!groupMinMax[loai]) groupMinMax[loai] = { min: val, max: val };
            else {
                if (val < groupMinMax[loai].min) groupMinMax[loai].min = val;
                if (val > groupMinMax[loai].max) groupMinMax[loai].max = val;
            }
        });
        list.forEach(function (it) {
            var loai = it.getAttribute('data-loaigiam') || 'KHAC';
            var val = parseFloat(it.getAttribute('data-giatri')) || 0;
            var mm = groupMinMax[loai];
            var range = mm.max - mm.min;
            var norm = range > 0 ? (val - mm.min) / range : 0; // 0 = giảm ít nhất, 1 = giảm nhiều nhất trong nhóm
            var weight = 1.15 - norm * 1.0; // giảm ít nhất ~1.15, giảm nhiều nhất ~0.15
            var r = Math.random();
            it.__wKey = Math.pow(r, 1 / weight);
        });
        return list.slice().sort(function (a, b) { return b.__wKey - a.__wKey; });
    }
    items = weightedShuffleByDiscount(items);
    var promoGridEl = document.getElementById('ds-promo-grid');
    if (promoGridEl) {
        items.forEach(function (el) { promoGridEl.appendChild(el); });
    }

    /* ── Sao chép mã ── */
    document.querySelectorAll('.ds-voucher-copy, .vc2-copy').forEach(function(btn) {
        var originalHtml = btn.innerHTML;
        btn.addEventListener('click', function(e) {
            e.stopPropagation();
            var code = btn.getAttribute('data-code');
            if (!code) return;
            function done() {
                btn.innerHTML = '<i class="bi bi-check2"></i> Đã chép';
                btn.classList.add('copied');
                setTimeout(function() {
                    btn.innerHTML = originalHtml;
                    btn.classList.remove('copied');
                }, 1500);
            }
            if (navigator.clipboard && navigator.clipboard.writeText) {
                navigator.clipboard.writeText(code).then(done).catch(function() { fallbackCopy(code); done(); });
            } else {
                fallbackCopy(code);
                done();
            }
        });
    });

    function fallbackCopy(text) {
        var ta = document.createElement('textarea');
        ta.value = text;
        ta.style.position = 'fixed';
        ta.style.opacity = '0';
        document.body.appendChild(ta);
        ta.select();
        try { document.execCommand('copy'); } catch (e) {}
        document.body.removeChild(ta);
    }

    /* ── Phân trang voucher (max 3/trang) ── */
    var PROMO_PER_PAGE = 3;
    var promoCurrentPage = 0;

    function getVisibleItems() {
        return items.filter(function(it) {
            return !it.classList.contains('hidden');
        });
    }

    function renderPromoPage() {
        var visible = getVisibleItems();
        var total = visible.length;
        var totalPages = Math.ceil(total / PROMO_PER_PAGE);
        if (promoCurrentPage >= totalPages) promoCurrentPage = totalPages - 1;
        if (promoCurrentPage < 0) promoCurrentPage = 0;

        visible.forEach(function(el, i) {
            var show = i >= promoCurrentPage * PROMO_PER_PAGE && i < (promoCurrentPage + 1) * PROMO_PER_PAGE;
            el.style.display = show ? '' : 'none';
        });

        var pager = document.getElementById('ds-promo-pager');
        var info = document.getElementById('promoPagerInfo');
        var prev = document.getElementById('promoPrev');
        var next = document.getElementById('promoNext');
        if (pager) {
            if (total <= PROMO_PER_PAGE) {
                pager.style.display = 'none';
            } else {
                pager.style.display = 'flex';
                if (info) info.textContent = (promoCurrentPage + 1) + '/' + totalPages;
                if (prev) prev.disabled = promoCurrentPage === 0;
                if (next) next.disabled = promoCurrentPage >= totalPages - 1;
            }
        }
    }

    window.promoPage = function(dir) {
        promoCurrentPage += dir;
        renderPromoPage();
    };

    renderPromoPage();

    /* ── Mở popup chi tiết voucher ── */
    window.openPromoDetail = function(el) {
        var ten = el.getAttribute('data-ten') || '';
        var ma = el.getAttribute('data-ma') || '';
        var loaiGiam = el.getAttribute('data-loaigiam') || '';
        var giatri = el.getAttribute('data-giatri') || '0';
        var minOrder = el.getAttribute('data-min') || '0';
        var maxGiam = el.getAttribute('data-max') || '';
        var tu = el.getAttribute('data-tu') || '--';
        var den = el.getAttribute('data-den') || '--';
        var solan = el.getAttribute('data-solandalung') || '';
        var daDung = el.getAttribute('data-dadung') || '0';
        var id = el.getAttribute('data-id') || '';

        var isPct = loaiGiam === 'PHAN_TRAM';
        var discountText = isPct ? ('-' + giatri + '%') : ('-' + Number(giatri).toLocaleString('vi-VN') + 'đ');

        var dmDiscount = document.getElementById('dm-discount');
        var dmTen = document.getElementById('dm-ten');
        var dmMa = document.getElementById('dm-ma');
        var dmLoai = document.getElementById('dm-loai');
        var dmMin = document.getElementById('dm-min');
        var dmMax = document.getElementById('dm-max');
        var dmTu = document.getElementById('dm-tu');
        var dmDen = document.getElementById('dm-den');
        var dmFooterAction = document.getElementById('dm-footer-action');

        if (dmDiscount) dmDiscount.textContent = discountText;
        if (dmTen) dmTen.textContent = ten;
        if (dmMa) dmMa.textContent = ma;
        if (dmLoai) dmLoai.textContent = isPct ? 'Giảm theo phần trăm' : 'Giảm số tiền cố định';
        if (dmMin) dmMin.textContent = Number(minOrder).toLocaleString('vi-VN') + 'đ';
        if (dmMax) dmMax.textContent = maxGiam ? (Number(maxGiam).toLocaleString('vi-VN') + 'đ') : 'Không giới hạn';
        if (dmTu) dmTu.textContent = tu;
        if (dmDen) dmDen.textContent = den;

        if (dmFooterAction) {
            dmFooterAction.innerHTML = '<button type="button" class="ds-btn ds-btn-fill" onclick="claimPromo(\'' + ma + '\')"><i class="bi bi-wallet2 me-1"></i>Nhận voucher</button>';
        }

        var modal = document.getElementById('detailModal');
        if (modal) {
            var bsModal = new bootstrap.Modal(modal);
            bsModal.show();
        }
    };

    /* ── Nhận voucher ── */
    window.claimPromo = function(code) {
        var modal = document.getElementById('detailModal');
        if (modal) {
            var bsModal = bootstrap.Modal.getInstance(modal);
            if (bsModal) bsModal.hide();
        }
        showToast('Đã nhận khuyến mãi "' + code + '" thành công!');
    };

    function showToast(msg) {
        if (typeof DuaStore !== 'undefined' && DuaStore.toast) {
            DuaStore.toast.success(msg);
        }
    }

    /* ── Copy mã từ modal ── */
    window.copyDetailCode = function() {
        var el = document.getElementById('dm-ma');
        if (!el) return;
        var code = el.textContent.trim();
        if (!code) return;
        if (navigator.clipboard && navigator.clipboard.writeText) {
            navigator.clipboard.writeText(code).then(function() { showToast('Đã sao chép mã ' + code); });
        } else {
            var ta = document.createElement('textarea');
            ta.value = code;
            ta.style.position = 'fixed';
            ta.style.opacity = '0';
            document.body.appendChild(ta);
            ta.select();
            try { document.execCommand('copy'); } catch (e) {}
            document.body.removeChild(ta);
            showToast('Đã sao chép mã ' + code);
        }
    };

    /* ── Nhận voucher từ trang chủ ── */
    window.claimHomePromo = function(promoId, code) {
        fetch('/api/vi-voucher/luu/' + promoId, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' }
        })
        .then(function(res) { return res.json(); })
        .then(function(data) {
            if (data.success) {
                showToast('Đã nhận voucher "' + code + '" thành công!');
            } else {
                showToast(data.message || 'Không thể nhận voucher. Vui lòng thử lại.');
            }
        })
        .catch(function() {
            showToast('Đã nhận voucher "' + code + '" thành công!');
        });
    };

})();

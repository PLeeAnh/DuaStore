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

    /* ── Tab lọc voucher ── */
    var tabs = document.querySelectorAll('.ds-promo-tab');
    var items = Array.prototype.slice.call(document.querySelectorAll('.ds-promo-item'));
    if (tabs.length && items.length) {
        tabs.forEach(function(tab) {
            tab.addEventListener('click', function() {
                tabs.forEach(function(t) { t.classList.remove('active'); });
                tab.classList.add('active');
                var filter = tab.getAttribute('data-filter');
                if (filter === 'expiring') {
                    var visible = items.filter(function(it) {
                        return !it.classList.contains('hidden');
                    });
                    visible.sort(function(a, b) {
                        return (a.getAttribute('data-end') || '9999-12-31').localeCompare(b.getAttribute('data-end') || '9999-12-31');
                    });
                    visible.forEach(function(el) { el.parentElement.appendChild(el); });
                    return;
                }
                items.forEach(function(it) {
                    var loai = it.getAttribute('data-loai');
                    it.classList.toggle('hidden', filter !== 'all' && loai !== filter);
                });
            });
        });
    }

    /* ── Sao chép mã ── */
    document.querySelectorAll('.vc2-copy').forEach(function(btn) {
        btn.addEventListener('click', function(e) {
            e.stopPropagation();
            var code = btn.getAttribute('data-code');
            if (!code) return;
            function done() {
                btn.textContent = 'Đã chép';
                btn.classList.add('copied');
                setTimeout(function() {
                    btn.textContent = 'Sao chép';
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

    /* ── Tab thay đổi → reset trang ── */
    tabs.forEach(function(tab) {
        tab.addEventListener('click', function() {
            promoCurrentPage = 0;
            setTimeout(renderPromoPage, 50);
        });
    });

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
            dmFooterAction.innerHTML = '<button type="button" class="vc2-get ds-btn-fill" onclick="claimPromo(\'' + ma + '\')">Nhận voucher</button>';
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
        var toast = document.getElementById('dsToast');
        if (!toast) {
            toast = document.createElement('div');
            toast.id = 'dsToast';
            toast.className = 'ds-toast';
            document.body.appendChild(toast);
        }
        toast.textContent = msg;
        toast.classList.add('show');
        setTimeout(function() { toast.classList.remove('show'); }, 3000);
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

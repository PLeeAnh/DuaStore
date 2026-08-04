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
        btn.addEventListener('click', function() {
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
})();

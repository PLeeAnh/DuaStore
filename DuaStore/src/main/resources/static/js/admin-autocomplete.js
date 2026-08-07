'use strict';

/* =====================================================
   DuaStore — DsAutocomplete
   Autocomplete gõ nhanh dùng chung cho các form admin.

   Kích hoạt:
     <input data-ds-autocomplete="/admin/api/search?type=product" data-ds-map="product" />

   Tùy chọn (attribute):
     data-ds-map        = product | variant | customer | order | attribute (default: product)
     data-ds-limit      = số kết quả tối đa (default 7)
     data-ds-open-url   = khi chọn, mở URL này (thay {id} bằng id kết quả)
     data-ds-target     = selector input/select nhận lại giá trị id (mặc định: chính ô đang gõ)
     data-ds-create-url = hiện "+ Thêm mới" khi không có kết quả; bấm mở URL (thay {value})
     data-ds-no-recent  = tắt lịch sử tìm gần đây

   Tính năng: cache 5 phút, AbortController, spinner sau 200ms, phím
   ↑ ↓ Home End Enter Escape, highlight <mark>, ARIA combobox/listbox,
   lịch sử gần đây, empty state, paste tìm ngay.
   ===================================================== */
(function () {
    var CACHE_TTL = 5 * 60 * 1000; // 5 phút
    var cache = new Map();

    function norm(s) {
        if (!s) return '';
        return s.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLowerCase().trim();
    }

    function esc(s) {
        return String(s == null ? '' : s)
            .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;').replace(/'/g, '&#39;');
    }

    function money(n) {
        if (n == null || n === '') return '';
        try {
            return new Intl.NumberFormat('vi-VN').format(Number(n)) + ' đồng';
        } catch (e) {
            return '' + n;
        }
    }

    function parseDate(iso) {
        if (!iso) return '';
        var m = iso.match(/^(\d{4})-(\d{2})-(\d{2})[T ](\d{2}):(\d{2})/);
        return m ? m[3] + '/' + m[2] + '/' + m[1] + ' ' + m[4] + ':' + m[5] : iso;
    }

    function highlight(text, q) {
        var safe = esc(text);
        if (!q) return safe;
        var needle = norm(q);
        var hay = norm(text);
        var found = hay.indexOf(needle);
        if (found < 0) return safe;
        var start = realIndex(text, found);
        var end = realIndex(text, found + needle.length);
        return esc(text.slice(0, start)) + '<mark>' + esc(text.slice(start, end)) + '</mark>' + esc(text.slice(end));
    }

    function realIndex(text, normIdx) {
        var count = 0;
        for (var i = 0; i < text.length; i++) {
            if (count === normIdx) return i;
            count++;
        }
        return text.length;
    }

    var STATUS_TEXT = {
        'CHO_XAC_NHAN': 'Chờ xác nhận', 'DA_XAC_NHAN': 'Đã xác nhận', 'DANG_GIAO': 'Đang giao',
        'DA_GIAO': 'Đã giao', 'DA_HOAN_THANH': 'Hoàn thành', 'DA_HUY': 'Đã hủy',
        'CHUA_THANH_TOAN': 'Chưa TT', 'DA_THANH_TOAN': 'Đã thanh toán',
        'COD': 'COD', 'BANK': 'Chuyển khoản', 'VNPAY': 'VNPay'
    };
    var STATUS_CLASS = {
        'CHO_XAC_NHAN': 'bg-warning text-dark', 'DA_XAC_NHAN': 'bg-info text-dark',
        'DANG_GIAO': 'bg-primary', 'DA_GIAO': 'bg-success', 'DA_HOAN_THANH': 'bg-success',
        'DA_HUY': 'bg-secondary', 'CHUA_THANH_TOAN': 'bg-danger', 'DA_THANH_TOAN': 'bg-success'
    };
    var EMPTY_TEXT = {
        product: 'Không tìm thấy sản phẩm.',
        variant: 'Không tìm thấy biến thể.',
        customer: 'Không tìm thấy khách hàng.',
        order: 'Không tìm thấy đơn hàng.',
        attribute: 'Không tìm thấy giá trị.'
    };

    var ROW_BUILDERS = {
        product: function (items, q) {
            return items.map(function (it) {
                var meta = '';
                if (it.sku) meta += '<span class="ds-ac-sku">' + esc(it.sku) + '</span>';
                if (it.price != null) meta += '<span class="ds-ac-price">' + money(it.price) + '</span>';
                return {
                    id: it.id,
                    disabled: !!it.disabled,
                    img: it.image || null,
                    html: '<div class="ds-ac-name">' + highlight(it.name, q) + '</div>'
                        + '<div class="ds-ac-meta">' + (meta || '&nbsp;') + '</div>',
                    right: it.disabled
                        ? '<span class="ds-ac-badge ds-out">Hết hàng</span>'
                        : '<span class="ds-ac-badge ds-in">Còn ' + (it.stock != null ? it.stock : 0) + '</span>'
                };
            });
        },
        variant: function (items, q) {
            return items.map(function (it) {
                return {
                    id: it.id,
                    disabled: !!it.disabled,
                    img: it.image || null,
                    html: '<div class="ds-ac-name">' + highlight(it.label || it.name, q) + '</div>'
                        + '<div class="ds-ac-meta">' + esc(it.name) + '</div>'
                        + (it.price != null ? '<div class="ds-ac-meta ds-ac-price">' + money(it.price) + '</div>' : ''),
                    right: it.disabled
                        ? '<span class="ds-ac-badge ds-out">Hết hàng</span>'
                        : '<span class="ds-ac-badge ds-in">Còn ' + (it.stock != null ? it.stock : 0) + '</span>'
                };
            });
        },
        customer: function (items, q) {
            return items.map(function (it) {
                var level = '<span class="ds-ac-badge ' + (it.level === 'VIP' ? 'ds-vip' : 'ds-normal') + '">'
                    + esc(it.level || 'NORMAL') + '</span>';
                var pts = it.points != null ? '<span class="ds-ac-badge ds-pts">' + it.points + 'đ</span>' : '';
                return {
                    id: it.id,
                    disabled: false,
                    img: null,
                    html: '<div class="ds-ac-name">' + highlight(it.name, q) + '</div>'
                        + '<div class="ds-ac-meta">' + esc(it.phone || '') + (it.phone && it.email ? ' · ' : '')
                        + esc(it.email || '') + '</div>',
                    right: level + pts
                };
            });
        },
        order: function (items, q) {
            return items.map(function (it) {
                var st = STATUS_TEXT[it.status]
                    ? '<span class="ds-ac-badge ' + (STATUS_CLASS[it.status] || 'bg-secondary') + '">' + esc(STATUS_TEXT[it.status]) + '</span>' : '';
                var pay = it.payment
                    ? '<span class="ds-ac-badge ds-soft">' + esc(STATUS_TEXT[it.payment] || it.payment) + '</span>' : '';
                return {
                    id: it.id,
                    disabled: false,
                    img: null,
                    html: '<div class="ds-ac-name">' + esc(it.code) + '</div>'
                        + '<div class="ds-ac-meta">' + esc(it.buyerName) + ' · ' + esc(it.phone)
                        + ' · ' + esc(parseDate(it.date)) + '</div>'
                        + '<div class="ds-ac-meta ds-ac-total">' + money(it.total) + '</div>',
                    right: st + pay
                };
            });
        },
        attribute: function (items, q) {
            return items.map(function (it) {
                return {
                    id: it.value != null ? it.value : '',
                    disabled: false,
                    img: null,
                    html: '<div class="ds-ac-name">' + highlight(it.value, q) + '</div>',
                    right: '<span class="ds-ac-badge ds-pts">' + (it.count || 0) + ' lần</span>'
                };
            });
        }
    };

    // ── Lịch sử gần đây ──
    function recentKey(url) { return 'ds-ac-recent:' + url; }
    function getRecent(url, n) {
        try {
            var a = JSON.parse(localStorage.getItem(recentKey(url)) || '[]');
            return (Array.isArray(a) ? a : []).slice(0, n);
        } catch (e) { return []; }
    }
    function pushRecent(url, v) {
        try {
            var a = getRecent(url, 9).filter(function (x) { return x !== v; });
            a.unshift(v);
            localStorage.setItem(recentKey(url), JSON.stringify(a.slice(0, 6)));
        } catch (e) {}
    }

    // ── Component ──
    function DsAutocomplete(input) {
        this.input = input;
        this.url = input.getAttribute('data-ds-autocomplete') || '';
        this.map = input.getAttribute('data-ds-map') || 'product';
        this.limit = parseInt(input.getAttribute('data-ds-limit') || '7', 10) || 7;
        this.openUrl = input.getAttribute('data-ds-open-url') || '';
        this.target = input.getAttribute('data-ds-target') || '';
        this.createUrl = input.getAttribute('data-ds-create-url') || '';
        this.useRecent = !input.hasAttribute('data-ds-no-recent');

        this.items = [];
        this.rows = [];
        this.index = -1;
        this.seq = 0;
        this.timer = null;
        this.loadingTimer = null;
        this.abortCtl = null;

        this.buildUI();
        this.bind();
    }

    DsAutocomplete.prototype.buildUI = function () {
        var wrap = document.createElement('div');
        wrap.className = 'ds-ac';
        this.input.parentNode.insertBefore(wrap, this.input);
        wrap.appendChild(this.input);

        this.wrap = wrap;
        this.list = document.createElement('ul');
        this.list.className = 'ds-ac-list';
        this.list.setAttribute('role', 'listbox');
        this.list.setAttribute('hidden', '');
        wrap.appendChild(this.list);

        this.input.setAttribute('autocomplete', 'off');
        this.input.setAttribute('role', 'combobox');
        this.input.setAttribute('aria-autocomplete', 'list');
        this.input.setAttribute('aria-expanded', 'false');
    };

    DsAutocomplete.prototype.bind = function () {
        var self = this;
        this.input.addEventListener('input', function () {
            if (self.timer) clearTimeout(self.timer);
            self.timer = setTimeout(function () { self.search(); }, 250);
        });
        this.input.addEventListener('focus', function () {
            if (self.input.value.trim()) self.search(); else self.showRecent();
        });
        this.input.addEventListener('blur', function () {
            setTimeout(function () { self.close(); }, 140);
        });
        this.input.addEventListener('keydown', function (e) { self.onKey(e); });

        this.list.addEventListener('mousedown', function (e) {
            var li = e.target.closest('.ds-ac-item');
            var cr = e.target.closest('.ds-create');
            var rec = e.target.closest('.ds-recent-item');
            if (li) { e.preventDefault(); self.pick(parseInt(li.getAttribute('data-idx'), 10)); }
            else if (cr) { e.preventDefault(); self.create(); }
            else if (rec) {
                e.preventDefault();
                self.input.value = rec.getAttribute('data-q');
                self.show();
                self.search();
            }
        });

        document.addEventListener('click', function (e) {
            if (!self.wrap.contains(e.target)) self.close();
        });
    };

    DsAutocomplete.prototype.onKey = function (e) {
        var n = this.list.querySelectorAll('.ds-ac-item').length;
        if (e.key === 'ArrowDown') {
            if (this.list.hidden) { this.search(); return; }
            this.setActive(this.index + 1);
            e.preventDefault();
        } else if (e.key === 'ArrowUp') {
            this.setActive(this.index - 1);
            e.preventDefault();
        } else if (e.key === 'Home') {
            this.setActive(0);
            e.preventDefault();
        } else if (e.key === 'End') {
            this.setActive(Math.max(n - 1, 0));
            e.preventDefault();
        } else if (e.key === 'Enter') {
            if (this.index >= 0 && n > 0) { this.pick(this.index); e.preventDefault(); }
            else if (this.list.querySelector('.ds-create')) { this.create(); e.preventDefault(); }
        } else if (e.key === 'Escape') {
            this.close();
        }
    };

    DsAutocomplete.prototype.setActive = function (i) {
        var els = this.list.querySelectorAll('.ds-ac-item');
        var n = els.length;
        if (!n) { this.index = -1; return; }
        if (i < 0) i = 0;
        if (i >= n) i = n - 1;
        this.index = i;
        for (var k = 0; k < n; k++) {
            els[k].classList.toggle('active', k === i);
            els[k].setAttribute('aria-selected', k === i ? 'true' : 'false');
        }
        els[i].scrollIntoView({block: 'nearest'});
    };

    DsAutocomplete.prototype.show = function () {
        this.list.hidden = false;
        this.input.setAttribute('aria-expanded', 'true');
        var hasRows = this.list.querySelector('.ds-ac-item, .ds-empty, .ds-status, .ds-recent-title');
        if (hasRows) this.setActive(this.index >= 0 ? this.index : 0);
    };

    DsAutocomplete.prototype.close = function () {
        this.list.hidden = true;
        this.input.setAttribute('aria-expanded', 'false');
        this.seq++;
        if (this.abortCtl) this.abortCtl.abort();
        if (this.loadingTimer) clearTimeout(this.loadingTimer);
        this.index = -1;
    };

    DsAutocomplete.prototype.search = function () {
        var q = this.input.value.trim();
        if (!q) { this.showRecent(); return; }
        if (!this.url) return;

        var key = this.url + '|' + q + '|' + this.limit;
        var hit = cache.get(key);
        if (hit && hit.expires > Date.now()) { this.render(hit.items, q); return; }

        var seq = ++this.seq;
        if (this.abortCtl) this.abortCtl.abort();
        var ctl = new AbortController();
        this.abortCtl = ctl;

        var sep = this.url.indexOf('?') >= 0 ? '&' : '?';
        var url = this.url + sep + 'q=' + encodeURIComponent(q)
            + '&q2=' + encodeURIComponent(norm(q)) + '&limit=' + this.limit;

        var self = this;
        this.loadingTimer = setTimeout(function () {
            if (seq === self.seq && self.list.hidden) self.renderStatus('Đang tải...');
        }, 200);

        fetch(url, {signal: ctl.signal})
            .then(function (r) {
                if (!r.ok) throw new Error('HTTP ' + r.status);
                return r.json();
            })
            .then(function (data) {
                if (seq !== self.seq) return;
                if (self.loadingTimer) clearTimeout(self.loadingTimer);
                var items = Array.isArray(data) ? data : ((data && data.results) || []);
                cache.set(key, {expires: Date.now() + CACHE_TTL, items: items});
                self.render(items, q);
            })
            .catch(function (err) {
                if (err && err.name === 'AbortError') return;
                if (seq !== self.seq) return;
                if (self.loadingTimer) clearTimeout(self.loadingTimer);
                self.renderStatus('Có lỗi khi tìm kiếm');
            });
    };

    DsAutocomplete.prototype.renderStatus = function (msg) {
        this.list.innerHTML = '<li class="ds-status">' + esc(msg) + '</li>';
        this.show();
    };

    DsAutocomplete.prototype.render = function (items, q) {
        this.list.innerHTML = '';
        this.index = -1;
        this.items = items;
        this.rows = buildRows(this.map, items, q);

        if (!items.length) {
            var empty = document.createElement('div');
            empty.className = 'ds-empty';
            empty.textContent = EMPTY_TEXT[this.map] || 'Không có kết quả.';
            this.list.appendChild(empty);
            if (this.createUrl) {
                var cr = document.createElement('div');
                cr.className = 'ds-create';
                cr.innerHTML = '<i class="bi bi-plus-circle"></i> Thêm mới: <strong>' + esc(q) + '</strong>';
                this.list.appendChild(cr);
            }
            this.show();
            return;
        }

        var self = this;
        this.rows.forEach(function (r, i) {
            var li = document.createElement('li');
            li.className = 'ds-ac-item' + (r.disabled ? ' ds-disabled' : '');
            li.setAttribute('role', 'option');
            li.setAttribute('aria-selected', 'false');
            li.setAttribute('data-idx', i);
            var img = r.img
                ? '<span class="ds-ac-thumb"><img src="' + esc(r.img) + '" alt="" loading="lazy"/></span>'
                : '';
            li.innerHTML = img + '<span class="ds-ac-body">' + r.html + '</span>'
                + '<span class="ds-ac-side">' + (r.right || '') + '</span>';
            li.addEventListener('mouseenter', function () { self.setActive(i); });
            li.addEventListener('mousedown', function (e) {
                e.preventDefault();
                self.pick(i);
            });
            self.list.appendChild(li);
        });
        this.show();
    };

    DsAutocomplete.prototype.showRecent = function () {
        if (!this.useRecent) return;
        var recents = getRecent(this.url, 5);
        if (!recents.length) { this.close(); return; }
        var self = this;
        this.list.innerHTML = '<div class="ds-recent-title">Tìm gần đây</div>';
        recents.forEach(function (q) {
            var li = document.createElement('div');
            li.className = 'ds-recent-item';
            li.setAttribute('data-q', q);
            li.innerHTML = '<i class="bi bi-clock-history"></i>' + esc(q);
            self.list.appendChild(li);
        });
        this.show();
    };

    DsAutocomplete.prototype.pick = function (idx) {
        var r = this.rows && this.rows[idx];
        if (!r) return;
        var it = this.items[idx] || {};
        var id = String(r.id != null ? r.id : (it.id != null ? it.id : ''));
        var label = it.name || it.label || it.value || id;

        this.close();

        if (this.openUrl && id) {
            window.location.href = this.openUrl.replace('{id}', encodeURIComponent(id));
            return;
        }

        var picker = this.input.getAttribute('data-ds-picker');
        if (picker && typeof window[picker] === 'function') {
            window[picker](id, label, it);
            if (this.useRecent) pushRecent(this.url, label);
            return;
        }

        if (this.target) {
            var t = document.querySelector(this.target);
            if (t) {
                if (t.tagName === 'SELECT') {
                    var found = Array.prototype.some.call(t.options, function (o) {
                        return String(o.value) === id;
                    });
                    if (!found) {
                        var opt = document.createElement('option');
                        opt.value = id;
                        opt.textContent = label;
                        t.appendChild(opt);
                    }
                    t.value = id;
                    t.dispatchEvent(new Event('change', {bubbles: true}));
                } else {
                    t.value = id;
                    t.dispatchEvent(new Event('input', {bubbles: true}));
                    t.dispatchEvent(new Event('change', {bubbles: true}));
                }
            }
        }
        this.input.value = label;
        this.input.dispatchEvent(new Event('input', {bubbles: true}));
        if (this.useRecent) pushRecent(this.url, label);
    };

    DsAutocomplete.prototype.create = function () {
        if (!this.createUrl) return;
        var q = this.input.value.trim();
        window.location.href = this.createUrl.replace('{value}', encodeURIComponent(q));
    };

    function buildRows(map, items, q) {
        var fn = ROW_BUILDERS[map] || ROW_BUILDERS.product;
        return fn(items, q);
    }

    // ── Bootstrap ──
    function run() {
        document.querySelectorAll('[data-ds-autocomplete]').forEach(function (el) {
            if (el._dsAc || (el.parentNode && el.parentNode.classList.contains('ds-ac'))) return;
            try {
                el._dsAc = new DsAutocomplete(el);
            } catch (e) {}
        });
    }

    if (typeof window.DsAutocompleteInit === 'undefined') {
        window.DsAutocompleteInit = true;
        if (document.readyState === 'loading') {
            document.addEventListener('DOMContentLoaded', run);
        } else {
            run();
        }
        new MutationObserver(run).observe(document.documentElement, {childList: true, subtree: true});
    }
})();
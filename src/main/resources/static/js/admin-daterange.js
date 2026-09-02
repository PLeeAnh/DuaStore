'use strict';

/* ── Bo loc khoang ngay dang popover (nut kich hoat + presets + tu/den ngay) ──
   Dung chung cho nhieu trang: goi initAdmDateRange(rootId, options) voi:
   options = {
     activePeriod: 'this-month' | null,
     fromDate: 'yyyy-MM-dd', toDate: 'yyyy-MM-dd',
     presets: [{key:'today', label:'Hôm nay'}, ...],
     onPreset: function(periodKey) {},
     onApply: function(fromStr, toStr) {},
     onClear: function() {}
   } */
window.initAdmDateRange = function (rootId, options) {
    var root = document.getElementById(rootId);
    if (!root) return;
    var btn = root.querySelector('.adm-daterange-btn');
    var popover = root.querySelector('.adm-daterange-popover');
    var fromInput = root.querySelector('.adm-daterange-inputs input[data-role="from"]');
    var toInput = root.querySelector('.adm-daterange-inputs input[data-role="to"]');
    var errorEl = root.querySelector('.adm-daterange-error');
    var applyBtn = root.querySelector('.adm-daterange-apply');
    var clearBtn = root.querySelector('.adm-daterange-clear');
    if (!btn || !popover) return;

    function close() {
        popover.style.display = 'none';
        btn.classList.remove('active');
    }
    function open() {
        popover.style.display = 'block';
        btn.classList.add('active');
    }
    btn.addEventListener('click', function (e) {
        e.stopPropagation();
        if (popover.style.display === 'block') close();
        else open();
    });
    popover.addEventListener('click', function (e) {
        e.stopPropagation();
    });
    document.addEventListener('click', close);

    root.querySelectorAll('.adm-daterange-preset').forEach(function (p) {
        p.addEventListener('click', function () {
            close();
            if (options.onPreset) options.onPreset(p.getAttribute('data-period'));
        });
    });

    function validate() {
        var f = fromInput ? fromInput.value : '';
        var t = toInput ? toInput.value : '';
        var bad = f && t && f > t;
        if (fromInput) fromInput.classList.toggle('is-invalid', !!bad);
        if (toInput) toInput.classList.toggle('is-invalid', !!bad);
        if (errorEl) errorEl.style.display = bad ? 'block' : 'none';
        return !bad;
    }
    if (fromInput) fromInput.addEventListener('change', validate);
    if (toInput) toInput.addEventListener('change', validate);

    if (applyBtn) {
        applyBtn.addEventListener('click', function () {
            if (!validate()) return;
            close();
            var f = fromInput ? fromInput.value : '';
            var t = toInput ? toInput.value : '';
            if (options.onApply) options.onApply(f, t);
        });
    }
    if (clearBtn) {
        clearBtn.addEventListener('click', function () {
            close();
            if (fromInput) fromInput.value = '';
            if (toInput) toInput.value = '';
            if (options.onClear) options.onClear();
        });
    }
};

/* Tinh khoang ngay (yyyy-MM-dd) tu ma preset - dung phia client cho trang
   khong co tham so "period" o backend (vi du Don hang chi nhan fromDate/toDate). */
window.admResolvePeriodRange = function (period) {
    function fmt(d) {
        var y = d.getFullYear(), m = String(d.getMonth() + 1).padStart(2, '0'), day = String(d.getDate()).padStart(2, '0');
        return y + '-' + m + '-' + day;
    }
    var today = new Date();
    var from = new Date(today), to = new Date(today);
    switch (period) {
        case 'today':
            break;
        case 'yesterday':
            from.setDate(from.getDate() - 1);
            to.setDate(to.getDate() - 1);
            break;
        case 'last-7':
            from.setDate(from.getDate() - 6);
            break;
        case 'this-week': {
            var day = (today.getDay() + 6) % 7; // 0 = Monday
            from.setDate(today.getDate() - day);
            break;
        }
        case 'this-month':
            from = new Date(today.getFullYear(), today.getMonth(), 1);
            break;
        case 'last-month': {
            var lm = new Date(today.getFullYear(), today.getMonth() - 1, 1);
            from = lm;
            to = new Date(lm.getFullYear(), lm.getMonth() + 1, 0);
            break;
        }
        case 'this-quarter': {
            var q = Math.floor(today.getMonth() / 3);
            from = new Date(today.getFullYear(), q * 3, 1);
            break;
        }
        case 'this-year':
            from = new Date(today.getFullYear(), 0, 1);
            break;
        default:
            from = new Date(today.getFullYear(), today.getMonth(), 1);
    }
    return { from: fmt(from), to: fmt(to) };
};

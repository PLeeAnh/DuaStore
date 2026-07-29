'use strict';
var targetData = {
    '': [],
    'CATEGORY': (window.__promoData.categories || []).map(function (c) {
        return {value: c.id, text: c.tenDanhMuc};
    }),
    'PRODUCT': (window.__promoData.products || []).map(function (p) {
        return {value: p.id, text: p.tenSanPham};
    })
};
var targetIds = window.__promoData.targetIds || '';
var initialSelection = targetIds ? targetIds.split(',').map(function (s) {
    return s.trim();
}).filter(Boolean) : [];
var targetSelect = document.getElementById('targetSelector');
var targetIdsInput = document.getElementById('targetIds');
var targetTypeSelect = document.getElementById('targetTypeSelect');
var ts = new TomSelect(targetSelect, {
    plugins: ['remove_button'],
    maxItems: null,
    onChange: function (values) {
        targetIdsInput.value = values.join(',');
        targetIdsInput.dispatchEvent(new Event('input', {bubbles: true}));
    }
});
function populateTargetOptions() {
    var type = targetTypeSelect.value;
    var items = targetData[type] || [];
    ts.clear();
    ts.clearOptions();
    items.forEach(function (item) {
        ts.addOption(item);
    });
    ts.refreshOptions(false);
    if (type === '' || items.length === 0) {
        ts.disable();
        ts.input.style.minHeight = 'auto';
    } else {
        ts.enable();
        ts.input.style.minHeight = '120px';
        initialSelection.forEach(function (val) {
            var match = items.find(function (i) {
                return String(i.value) === val;
            });
            if (match)
                ts.addItem(match.value);
        });
    }
}

targetTypeSelect.addEventListener('change', function () {
    initialSelection = targetIdsInput.value ? targetIdsInput.value.split(',').map(function (s) {
        return s.trim();
    }).filter(Boolean) : [];
    populateTargetOptions();
});
populateTargetOptions();

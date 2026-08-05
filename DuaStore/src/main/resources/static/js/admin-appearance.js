'use strict';

/* ── Widget toggle visibility ── */
document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('[name="widget_messenger"]').forEach(function (cb) {
        document.getElementById('messengerConfig').style.display = cb.checked ? 'block' : 'none';
        cb.addEventListener('change', function () {
            document.getElementById('messengerConfig').style.display = this.checked ? 'block' : 'none';
        });
    });
    document.querySelectorAll('[name="widget_zalo"]').forEach(function (cb) {
        document.getElementById('zaloConfig').style.display = cb.checked ? 'block' : 'none';
        cb.addEventListener('change', function () {
            document.getElementById('zaloConfig').style.display = this.checked ? 'block' : 'none';
        });
    });
    document.querySelectorAll('[name="widget_call"]').forEach(function (cb) {
        document.getElementById('callConfig').style.display = cb.checked ? 'block' : 'none';
        cb.addEventListener('change', function () {
            document.getElementById('callConfig').style.display = this.checked ? 'block' : 'none';
        });
    });

    // ── Popup Promo ──
    var pActive  = document.getElementById('popupPromoActive');
    var pConfig  = document.getElementById('popupPromoConfig');
    var pMode    = document.getElementById('popupPromoMode');
    var pItvWrap = document.getElementById('popupPromoIntervalWrap');
    var pImg     = document.getElementById('popupPromoImage');
    var pPreview = document.getElementById('popupPromoPreview');
    if (pActive && pConfig) {
        pConfig.style.display = pActive.checked ? 'block' : 'none';
        pActive.addEventListener('change', function () {
            pConfig.style.display = this.checked ? 'block' : 'none';
        });
    }
    if (pMode && pItvWrap) {
        pItvWrap.style.display = pMode.value === 'timed' ? 'block' : 'none';
        pMode.addEventListener('change', function () {
            pItvWrap.style.display = this.value === 'timed' ? 'block' : 'none';
        });
    }
    if (pImg && pPreview) {
        pImg.addEventListener('input', function () {
            var url = this.value.trim();
            pPreview.innerHTML = url
                ? '<img src="' + url + '" style="max-width:100%;max-height:160px;object-fit:contain" alt="Preview" onerror="this.parentElement.innerHTML=\'<span class=text-muted>\u1ea2nh kh\u00f4ng h\u1ee3p l\u1ec7</span>\'">'
                : '<span class="text-muted small">Ch\u01b0a c\u00f3 \u1ea3nh</span>';
        });
    }
});

/* ── Color picker sync ── */
document.querySelectorAll('input[type="color"]').forEach(function (colorInput) {
    colorInput.addEventListener('input', function () {
        var textInput = this.parentElement.querySelector('input[type="text"]');
        if (textInput)
            textInput.value = this.value;
    });
});
document.querySelectorAll('input[name$="_text"]').forEach(function (textInput) {
    textInput.addEventListener('input', function () {
        var colorInput = this.parentElement.querySelector('input[type="color"]');
        if (colorInput)
            colorInput.value = this.value;
    });
});

/* ── Menu sorting ── */
var menuSortable = new Sortable(document.querySelector('.menu-sortable'), {
    handle: '.bi-grip-vertical',
    animation: 150,
    ghostClass: 'sortable-ghost',
    onEnd: renumberMenuItems
});
function renumberMenuItems() {
    var rows = document.querySelectorAll('.menu-sortable tr');
    rows.forEach(function (row, idx) {
        var num = idx + 1;
        row.querySelectorAll('input, textarea').forEach(function (input) {
            var name = input.getAttribute('name');
            if (name)
                input.setAttribute('name', name.replace(/menu_\d+_/, 'menu_' + num + '_'));
        });
    });
}
function removeMenuItem(btn) {
    if (!confirm('Bạn có chắc chắn muốn xóa mục menu này?')) return;
    var row = btn.closest('tr');
    if (document.querySelectorAll('.menu-sortable tr').length <= 1)
        return;
    row.remove();
    renumberMenuItems();
}
function addMenuItem() {
    var tbody = document.querySelector('.menu-sortable');
    var count = tbody.children.length + 1;
    var tr = document.createElement('tr');
    tr.innerHTML = '<td class="text-center"><i class="bi bi-grip-vertical text-muted"></i></td>' +
            '<td><input type="text" name="menu_' + count + '_label" class="form-control form-control-sm" placeholder="Tên menu" /></td>' +
            '<td><input type="url" name="menu_' + count + '_url" class="form-control form-control-sm" placeholder="/" /></td>' +
            '<td class="text-center"><input type="hidden" name="menu_' + count + '_active" value="0" /><input type="checkbox" name="menu_' + count + '_active" value="1" checked /></td>' +
            '<td><button type="button" class="btn btn-sm btn-outline-danger" onclick="removeMenuItem(this)"><i class="bi bi-trash"></i></button></td>';
    tbody.appendChild(tr);
    menuSortable.sort(tbody.children);
}

/* ── Footer columns ── */
var footerColCount = 3;
function addFooterColumn() {
    footerColCount++;
    var container = document.getElementById('footerColumns');
    var div = document.createElement('div');
    div.className = 'card mb-3 footer-col';
    div.innerHTML = '<div class="card-body">' +
            '<div class="d-flex justify-content-between align-items-center mb-2">' +
            '<h6 class="mb-0">Cột ' + footerColCount + '</h6>' +
            '<button type="button" class="btn btn-sm btn-outline-danger" onclick="this.closest(\'.footer-col\').remove()"><i class="bi bi-trash"></i></button>' +
            '</div>' +
            '<div class="row g-2">' +
            '<div class="col-md-4"><input type="text" name="footer_col_' + footerColCount + '_title" class="form-control form-control-sm" placeholder="Tiêu đề" /></div>' +
            '<div class="col-md-8"><textarea name="footer_col_' + footerColCount + '_content" class="form-control form-control-sm" rows="3" placeholder="Nội dung"></textarea></div>' +
            '</div></div></div>';
    container.appendChild(div);
}
function deleteFooterColumn(btn) {
    if (!confirm('Bạn có chắc chắn muốn xóa cột này?')) return;
    btn.closest('.footer-col').remove();
}

/* ── Homepage designer ── */
var hpCounter = 1;

var sectionTypes = [
    {type: 'slider', label: 'Slider', icon: 'bi-images', color: 'secondary', title: 'Hero Banner Slider'},
    {type: 'custom', label: 'Custom', icon: 'bi-code', color: 'secondary', title: 'HTML tùy chỉnh'}
];

function createHomepageSection(type, idx, active) {
    var st = sectionTypes.find(function (t) {
        return t.type === type;
    }) || sectionTypes[1];
    var div = document.createElement('div');
    div.className = 'card mb-2 hp-section' + (idx === 1 ? ' expanded' : '');
    div.innerHTML =
            '<div class="card-body py-2 d-flex align-items-center gap-2 hp-section-header" onclick="toggleHpSection(this)">' +
            '<i class="bi bi-grip-vertical text-muted"></i>' +
            '<span class="badge bg-' + st.color + ' me-1"><i class="bi ' + st.icon + ' me-1"></i>' + st.label + '</span>' +
            '<span class="fw-semibold small">' + st.title + '</span>' +
            '<div class="ms-auto d-flex align-items-center gap-2">' +
            '<input type="hidden" name="hp_' + idx + '_type" value="' + type + '" />' +
            '<input type="hidden" name="hp_' + idx + '_active" value="0" /><input type="checkbox" name="hp_' + idx + '_active" value="1" ' + (active !== false ? 'checked' : '') + ' onclick="event.stopPropagation()" />' +
            '<i class="bi bi-trash text-danger" style="cursor:pointer;font-size:0.85rem" onclick="event.stopPropagation();removeHomepageSection(this)" title="Xoá section"></i>' +
            '<i class="bi bi-chevron-down hp-chevron"></i>' +
            '</div>' +
            '</div>' +
            '<div class="hp-section-body">' +
            '<div class="card-body pt-0 border-top">' +
            '<div class="row g-2 mb-2">' +
            '<div class="col-md-4"><label class="form-label small">Tiêu đề section</label><input type="text" name="hp_' + idx + '_title" class="form-control form-control-sm" placeholder="Tiêu đề" /></div>' +
            '<div class="col-md-3"><label class="form-label small">Layout cột</label>' +
            '<div class="layout-grid" data-target="hp_' + idx + '_layout">' +
            '<div class="layout-option active" data-cols="1" onclick="selectLayout(this)"><div class="col-preview"></div></div>' +
            '<div class="layout-option" data-cols="2" onclick="selectLayout(this)"><div class="col-preview"></div><div class="col-preview"></div></div>' +
            '<div class="layout-option" data-cols="3" onclick="selectLayout(this)"><div class="col-preview"></div><div class="col-preview"></div><div class="col-preview"></div></div>' +
            '<div class="layout-option" data-cols="4" onclick="selectLayout(this)"><div class="col-preview"></div><div class="col-preview"></div><div class="col-preview"></div><div class="col-preview"></div></div>' +
            '<div class="layout-option" data-cols="5" onclick="selectLayout(this)"><div class="col-preview"></div><div class="col-preview"></div><div class="col-preview"></div><div class="col-preview"></div><div class="col-preview"></div></div>' +
            '<div class="layout-option" data-cols="6" onclick="selectLayout(this)"><div class="col-preview"></div><div class="col-preview"></div><div class="col-preview"></div><div class="col-preview"></div><div class="col-preview"></div><div class="col-preview"></div></div>' +
            '<div class="layout-option" data-cols="7" onclick="selectLayout(this)"><div class="col-preview"></div><div class="col-preview"></div><div class="col-preview"></div><div class="col-preview"></div><div class="col-preview"></div><div class="col-preview"></div><div class="col-preview"></div></div>' +
            '<div class="layout-option" data-cols="8" onclick="selectLayout(this)"><div class="col-preview"></div><div class="col-preview"></div><div class="col-preview"></div><div class="col-preview"></div><div class="col-preview"></div><div class="col-preview"></div><div class="col-preview"></div><div class="col-preview"></div></div>' +
            '<div class="layout-option" data-cols="9" onclick="selectLayout(this)"><div class="col-preview"></div><div class="col-preview"></div><div class="col-preview"></div><div class="col-preview"></div><div class="col-preview"></div><div class="col-preview"></div><div class="col-preview"></div><div class="col-preview"></div><div class="col-preview"></div></div>' +
            '<div class="layout-option" data-cols="10" onclick="selectLayout(this)"><div class="col-preview"></div><div class="col-preview"></div><div class="col-preview"></div><div class="col-preview"></div><div class="col-preview"></div><div class="col-preview"></div><div class="col-preview"></div><div class="col-preview"></div><div class="col-preview"></div><div class="col-preview"></div></div>' +
            '</div>' +
            '<input type="hidden" name="hp_' + idx + '_layout" value="1" />' +
            '</div>' +
            '<div class="col-md-3"><label class="form-label small">Giới hạn hiển thị</label><input type="number" name="hp_' + idx + '_limit" class="form-control form-control-sm" value="8" min="1" /></div>' +
            (type === 'products' ? '<div class="col-md-2"><label class="form-label small">Danh mục ID</label><input type="text" name="hp_' + idx + '_category_ids" class="form-control form-control-sm" placeholder="1,2,3" /></div>' : '') +
            (type === 'custom' ? '<div class="col-12"><label class="form-label small">Nội dung HTML</label><textarea name="hp_' + idx + '_html" class="form-control form-control-sm" rows="3"></textarea></div>' : '') +
            '</div>' +
            '</div>' +
            '</div>';
    return div;
}

var defaultSections = ['slider'];
defaultSections.forEach(function (type, i) {
    var idx = i + 1;
    var section = createHomepageSection(type, idx, true);
    document.getElementById('homepageSections').appendChild(section);

    var savedLayout = hpSettings['hp_' + idx + '_layout'];
    var savedTitle = hpSettings['hp_' + idx + '_title'];
    var savedLimit = hpSettings['hp_' + idx + '_limit'];
    var savedActive = hpSettings['hp_' + idx + '_active'];
    var savedCatIds = hpSettings['hp_' + idx + '_category_ids'];

    if (savedTitle && savedTitle !== 'null') {
        var titleInput = section.querySelector('input[name="hp_' + idx + '_title"]');
        if (titleInput)
            titleInput.value = savedTitle;
    }
    if (savedLayout && savedLayout !== 'null' && savedLayout !== '1') {
        var layoutOption = section.querySelector('.layout-option[data-cols="' + savedLayout + '"]');
        if (layoutOption) {
            section.querySelectorAll('.layout-option').forEach(function (o) {
                o.classList.remove('active');
            });
            layoutOption.classList.add('active');
            var hidden = section.querySelector('input[name="hp_' + idx + '_layout"]');
            if (hidden)
                hidden.value = savedLayout;
        }
    }
    if (savedLimit && savedLimit !== 'null') {
        var limitInput = section.querySelector('input[name="hp_' + idx + '_limit"]');
        if (limitInput)
            limitInput.value = savedLimit;
    }
    if (savedActive === '0') {
        var checkbox = section.querySelector('input[name="hp_' + idx + '_active"][type="checkbox"]');
        if (checkbox)
            checkbox.checked = false;
    }
    if (savedCatIds && savedCatIds !== 'null') {
        var catInput = section.querySelector('input[name="hp_' + idx + '_category_ids"]');
        if (catInput)
            catInput.value = savedCatIds;
    }
});

function toggleHpSection(header) {
    var card = header.closest('.hp-section');
    card.classList.toggle('expanded');
    var chevron = card.querySelector('.hp-chevron');
    if (chevron)
        chevron.style.transform = card.classList.contains('expanded') ? 'rotate(180deg)' : '';
}

function selectLayout(el) {
    var grid = el.closest('.layout-grid');
    grid.querySelectorAll('.layout-option').forEach(function (o) {
        o.classList.remove('active');
    });
    el.classList.add('active');
    var hidden = grid.parentElement.querySelector('input[type="hidden"]');
    if (hidden)
        hidden.value = el.dataset.cols;
}

function addHomepageSection() {
    hpCounter++;
    var section = createHomepageSection('custom', hpCounter, true);
    document.getElementById('homepageSections').appendChild(section);
    hpSortable.sort(section);
    var savedTitle = hpSettings['hp_' + hpCounter + '_title'];
    var savedLayout = hpSettings['hp_' + hpCounter + '_layout'];
    var savedLimit = hpSettings['hp_' + hpCounter + '_limit'];
    var savedActive = hpSettings['hp_' + hpCounter + '_active'];
    var savedCatIds = hpSettings['hp_' + hpCounter + '_category_ids'];
    if (savedTitle && savedTitle !== 'null') {
        var ti = section.querySelector('input[name="hp_' + hpCounter + '_title"]');
        if (ti)
            ti.value = savedTitle;
    }
    if (savedLayout && savedLayout !== 'null' && savedLayout !== '1') {
        var lo = section.querySelector('.layout-option[data-cols="' + savedLayout + '"]');
        if (lo) {
            section.querySelectorAll('.layout-option').forEach(function (o) {
                o.classList.remove('active');
            });
            lo.classList.add('active');
            var h = section.querySelector('input[name="hp_' + hpCounter + '_layout"]');
            if (h)
                h.value = savedLayout;
        }
    }
    if (savedLimit && savedLimit !== 'null') {
        var li = section.querySelector('input[name="hp_' + hpCounter + '_limit"]');
        if (li)
            li.value = savedLimit;
    }
    if (savedActive === '0') {
        var cb = section.querySelector('input[name="hp_' + hpCounter + '_active"][type="checkbox"]');
        if (cb)
            cb.checked = false;
    }
    if (savedCatIds && savedCatIds !== 'null') {
        var ci = section.querySelector('input[name="hp_' + hpCounter + '_category_ids"]');
        if (ci)
            ci.value = savedCatIds;
    }
}

function removeHomepageSection(el) {
    if (!confirm('Bạn có chắc chắn muốn xóa section này?')) return;
    var section = el.closest('.hp-section');
    if (document.querySelectorAll('#homepageSections .hp-section').length <= 1)
        return;
    section.remove();
    renumberHomepageSections();
}

var hpSortable = new Sortable(document.getElementById('homepageSections'), {
    handle: '.bi-grip-vertical',
    animation: 150,
    ghostClass: 'sortable-ghost',
    onEnd: renumberHomepageSections
});
function renumberHomepageSections() {
    var sections = document.querySelectorAll('#homepageSections .hp-section');
    sections.forEach(function (section, idx) {
        var num = idx + 1;
        section.querySelectorAll('input, textarea').forEach(function (input) {
            var name = input.getAttribute('name');
            if (name)
                input.setAttribute('name', name.replace(/hp_\d+_/, 'hp_' + num + '_'));
        });
    });
}



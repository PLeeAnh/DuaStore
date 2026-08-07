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

/* Các loại section dùng được cho cả mặc định và khi "Thêm section" */
var hpTypes = {
    'slider': {label: 'Slider', icon: 'bi-images', color: 'secondary', title: 'Hero Banner Slider'},
    'products': {label: 'Sản phẩm', icon: 'bi-box-seam', color: 'primary', title: 'Sản phẩm'},
    'categories': {label: 'Danh mục', icon: 'bi-grid', color: 'success', title: 'Danh mục'},
    'promotions': {label: 'Khuyến mãi', icon: 'bi-tag', color: 'warning', title: 'Khuyến mãi / Voucher'},
    'custom': {label: 'Custom', icon: 'bi-code', color: 'secondary', title: 'HTML tùy chỉnh'}
};

/* Các loại section hiện cho phép THÊM khi thiết kế trang chủ */
var hpAddTypes = ['products', 'categories', 'promotions', 'custom'];

/* Nguồn dữ liệu của section loại Sản phẩm */
var hpProductModes = {
    'featured': 'Sản phẩm nổi bật',
    'newest': 'Sản phẩm mới nhất',
    'best_sold': 'Mua nhiều nhất (bán chạy)',
    'most_liked': 'Thích nhiều nhất (yêu thích)',
    'under_price': 'Dưới một mức giá',
    'price_range': 'Khoảng giá từ–đến',
    'category': 'Theo danh mục'
};

function createHomepageSection(type, idx, active) {
    var st = hpTypes[type] || hpTypes.custom;
    var sv = function (key, def) {
        var v = hpSettings['hp_' + idx + '_' + key];
        return (v !== undefined && v !== null && v !== 'null') ? v : (def !== undefined ? def : '');
    };

    var div = document.createElement('div');
    div.className = 'card mb-2 hp-section' + (idx === 1 ? ' expanded' : '');

    var header = '' +
            '<div class="card-body py-2 d-flex align-items-center gap-2 hp-section-header" onclick="toggleHpSection(this)">' +
            '<i class="bi bi-grip-vertical text-muted"></i>' +
            '<span class="badge bg-' + st.color + ' me-1"><i class="bi ' + st.icon + ' me-1"></i>' + st.label + '</span>' +
            '<span class="fw-semibold small">' + (sv('title', st.title) || st.title) + '</span>' +
            '<div class="ms-auto d-flex align-items-center gap-2">' +
            '<input type="hidden" name="hp_' + idx + '_type" value="' + type + '" />' +
            '<input type="hidden" name="hp_' + idx + '_active" value="0" /><input type="checkbox" name="hp_' + idx + '_active" value="1" ' + (active !== false ? 'checked' : '') + ' onclick="event.stopPropagation()" />' +
            '<i class="bi bi-trash text-danger" style="cursor:pointer;font-size:0.85rem" onclick="event.stopPropagation();removeHomepageSection(this)" title="Xoá section"></i>' +
            '<i class="bi bi-chevron-down hp-chevron"></i>' +
            '</div></div>';

    var body = '';

    if (type === 'slider') {
        body = '' +
            '<div class="card-body pt-0 border-top">' +
            '<p class="text-muted small mb-1">Hero Banner Slider luôn hiển thị đầu trang chủ (không đổi).</p>' +
            '<p class="text-muted small mb-0">Quản lý ảnh banner tại menu <b>Banner</b>. Khi chưa đặt banner, trang chủ hiện banner mặc định.</p>' +
            '</div>';
    } else if (type === 'custom') {
        body = '' +
            '<div class="card-body pt-0 border-top">' +
            '<div class="row g-2 mb-2">' +
            '<div class="col-md-6"><label class="form-label small">Tiêu đề section</label><input type="text" name="hp_' + idx + '_title" class="form-control form-control-sm" value="' + sv('title', '') + '" placeholder="Tiêu đề" /></div>' +
            '</div>' +
            '<div class="row g-2">' +
            '<div class="col-12"><label class="form-label small">Nội dung HTML</label><textarea name="hp_' + idx + '_html" class="form-control form-control-sm font-monospace" rows="4" placeholder="&lt;div&gt;Nội dung tuỳ chỉnh...&lt;/div&gt;">' + sv('html', '') + '</textarea></div>' +
            '</div>' +
            '</div>';
    } else {
        var mode = sv('mode', 'featured');
        var layoutStyle = sv('layout_style', 'grid');
        var layout = sv('layout', '4');
        var limit = sv('limit', '8');
        var maxPrice = sv('max_price', '300000');
        var minPrice = sv('min_price', '0');
        var catIds = sv('category_ids', '');

        var modeOpts = Object.keys(hpProductModes).map(function (m) {
            return '<option value="' + m + '"' + (m === mode ? ' selected' : '') + '>' + hpProductModes[m] + '</option>';
        }).join('');

        body = '' +
            '<div class="card-body pt-0 border-top">' +
            '<div class="row g-2 mb-2">' +
            '<div class="col-md-3"><label class="form-label small">Tiêu đề section</label><input type="text" name="hp_' + idx + '_title" class="form-control form-control-sm" value="' + (sv('title', st.title) || st.title) + '" placeholder="Tiêu đề" /></div>';

        if (type === 'products') {
            body +=
                '<div class="col-md-3"><label class="form-label small">Nguồn dữ liệu</label>' +
                '<select name="hp_' + idx + '_mode" class="form-select form-select-sm hp-mode" onchange="hpModeChanged(this)">' + modeOpts + '</select></div>' +
                '<div class="col-md-3"><label class="form-label small">Kiểu hiển thị</label>' +
                '<select name="hp_' + idx + '_layout_style" class="form-select form-select-sm hp-style">' +
                '<option value="grid"' + (layoutStyle === 'grid' ? ' selected' : '') + '>Lưới (nhiều cột)</option>' +
                '<option value="slider"' + (layoutStyle === 'slider' ? ' selected' : '') + '>Slide ngang</option>' +
                '</select></div>' +
                '<div class="col-md-3"><label class="form-label small">Số cột / trang</label><input type="number" name="hp_' + idx + '_layout" class="form-control form-control-sm" value="' + layout + '" min="2" max="6" /></div>' +
                '<div class="col-md-3"><label class="form-label small">Số sản phẩm hiển thị</label><input type="number" name="hp_' + idx + '_limit" class="form-control form-control-sm" value="' + limit + '" min="1" max="24" /></div>' +
                '<div class="col-md-6 hp-price-fields' + ((mode === 'under_price' || mode === 'price_range') ? '' : ' d-none') + '">' +
                '<label class="form-label small">Khoảng giá (đ)</label>' +
                '<div class="input-group input-group-sm">' +
                '<span class="input-group-text">Từ</span><input type="number" name="hp_' + idx + '_min_price" class="form-control" value="' + minPrice + '" min="0" step="1000" />' +
                '<span class="input-group-text">Đến</span><input type="number" name="hp_' + idx + '_max_price" class="form-control" value="' + maxPrice + '" min="1000" step="1000" />' +
                '</div></div>' +
                '<div class="col-md-6 hp-cat-fields' + (mode === 'category' ? '' : ' d-none') + '">' +
                '<label class="form-label small">Danh mục ID (phẩn tách bằng dấu phẩy)</label>' +
                '<input type="text" name="hp_' + idx + '_category_ids" class="form-control form-control-sm" value="' + catIds + '" placeholder="1,2,3" />' +
                '</div>' +
                '<div class="col-md-4"><label class="form-label small">Link "Xem tất cả" (tuỳ chọn)</label>' +
                '<div class="d-flex gap-2">' +
                '<input type="text" name="hp_' + idx + '_link_label" class="form-control form-control-sm" value="' + sv('link_label', '') + '" placeholder="Xem tất cả" />' +
                '<input type="text" name="hp_' + idx + '_link_url" class="form-control form-control-sm" value="' + sv('link_url', '') + '" placeholder="/san-pham" />' +
                '</div></div>';
        } else if (type === 'categories') {
            body +=
                '<div class="col-md-3"><label class="form-label small">Số cột</label><input type="number" name="hp_' + idx + '_layout" class="form-control form-control-sm" value="' + layout + '" min="2" max="6" /></div>' +
                '<div class="col-md-3"><label class="form-label small">Số danh mục hiển thị</label><input type="number" name="hp_' + idx + '_limit" class="form-control form-control-sm" value="' + limit + '" min="1" max="12" /></div>' +
                '<div class="col-md-3 hp-style-block"><label class="form-label small">Kiểu hiển thị</label>' +
                '<select name="hp_' + idx + '_layout_style" class="form-select form-select-sm hp-style">' +
                '<option value="grid"' + (layoutStyle === 'grid' ? ' selected' : '') + '>Lưới (dạng ô)</option>' +
                '<option value="slider"' + (layoutStyle === 'slider' ? ' selected' : '') + '>Slide ngang</option>' +
                '</select></div>';
        } else if (type === 'promotions') {
            body +=
                '<div class="col-md-3"><label class="form-label small">Số cột</label><input type="number" name="hp_' + idx + '_layout" class="form-control form-control-sm" value="' + layout + '" min="2" max="6" /></div>' +
                '<div class="col-md-3"><label class="form-label small">Số lưu mã hiển thị</label><input type="number" name="hp_' + idx + '_limit" class="form-control form-control-sm" value="' + limit + '" min="1" max="12" /></div>' +
                '<div class="col-md-3"><label class="form-label small">Kiểu hiển thị</label>' +
                '<select name="hp_' + idx + '_layout_style" class="form-select form-select-sm hp-style">' +
                '<option value="grid"' + (layoutStyle === 'grid' ? ' selected' : '') + '>Lưới (dạng ô)</option>' +
                '<option value="slider"' + (layoutStyle === 'slider' ? ' selected' : '') + '>Slide ngang</option>' +
                '</select></div>';
        }

        body += '</div></div>';
    }

    div.innerHTML = header + '<div class="hp-section-body">' + body + '</div>';
    if (active === false) {
        var cbx = div.querySelector('input[name="hp_' + idx + '_active"][type="checkbox"]');
        if (cbx)
            cbx.checked = false;
    }
    return div;
}

/* Giữ tiêu đề trong header đồng bộ khi sửa */
window.addEventListener('DOMContentLoaded', function () {
    document.addEventListener('input', function (e) {
        if (e.target && e.target.name && e.target.name.indexOf('_title') > 0) {
            var card = e.target.closest('.hp-section');
            if (card) {
                var lbl = card.querySelector('.hp-section-header .fw-semibold');
                if (lbl)
                    lbl.textContent = e.target.value || 'Section';
            }
        }
    });
});

/* Hiện/ẩn khối giá & danh mục theo nguồn dữ liệu đã chọn */
function hpModeChanged(sel) {
    var card = sel.closest('.hp-section');
    var mode = sel.value;
    var priceBlock = card.querySelector('.hp-price-fields');
    var catBlock = card.querySelector('.hp-cat-fields');
    if (priceBlock)
        priceBlock.classList.toggle('d-none', !(mode === 'under_price' || mode === 'price_range'));
    if (catBlock)
        catBlock.classList.toggle('d-none', mode !== 'category');
}
window.hpModeChanged = hpModeChanged;

var defaultSections = ['slider'];
defaultSections.forEach(function (type, i) {
    var idx = i + 1;
    hpCounter = Math.max(hpCounter, idx);
    document.getElementById('homepageSections').appendChild(createHomepageSection(type, idx, true));
});

function toggleHpSection(header) {
    var card = header.closest('.hp-section');
    card.classList.toggle('expanded');
    var chevron = card.querySelector('.hp-chevron');
    if (chevron)
        chevron.style.transform = card.classList.contains('expanded') ? 'rotate(180deg)' : '';
}

function addHomepageSection(type) {
    if (!type)
        type = 'custom';
    hpCounter++;
    var section = createHomepageSection(type, hpCounter, true);
    document.getElementById('homepageSections').appendChild(section);
    if (window.hpSortable)
        hpSortable.sort(section);
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



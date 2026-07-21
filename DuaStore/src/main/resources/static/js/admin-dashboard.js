'use strict';
var sectionList = [];
var sectionConfig = {
    'slider':     { icon: 'bi-sliders',       color: 'primary',   label: 'Slider',        desc: 'Banner slider hiển thị đầu trang chủ' },
    'banner':     { icon: 'bi-images',        color: 'success',   label: 'Banner',        desc: 'Banner quảng cáo trang chủ' },
    'featured-products': { icon: 'bi-star',   color: 'warning',   label: 'Sản phẩm nổi bật', desc: 'Sản phẩm được hiển thị nổi bật' },
    'categories': { icon: 'bi-grid-3x3-gap',  color: 'info',      label: 'Danh mục nổi bật', desc: 'Danh mục hiển thị trên trang chủ' },
    'flash-sale': { icon: 'bi-lightning',     color: 'danger',    label: 'Flash Sale',    desc: 'Chương trình flash sale' },
    'vouchers':   { icon: 'bi-ticket-perforated', color: 'success', label: 'Voucher nổi bật', desc: 'Mã giảm giá hiển thị trên trang chủ' },
    'collection': { icon: 'bi-collection',    color: 'secondary', label: 'Bộ sưu tập',    desc: 'Bộ sưu tập sản phẩm đặc biệt' },
    'blog':       { icon: 'bi-newspaper',     color: 'primary',   label: 'Blog nổi bật',  desc: 'Bài viết nổi bật hiển thị trên trang chủ' },
    'popup':      { icon: 'bi-phone',         color: 'dark',      label: 'Popup',         desc: 'Thông báo popup khi truy cập trang' }
};
var sectionLinks = {
    'slider':     '/admin/banner',
    'banner':     '/admin/banner',
    'featured-products': '/admin/san-pham',
    'categories': '/admin/danh-muc',
    'flash-sale': '/admin/khuyen-mai',
    'vouchers':   '/admin/khuyen-mai',
    'collection': '/admin/san-pham',
    'blog':       '/admin/bai-viet',
    'popup':      '/admin/thong-bao'
};

function renderSections(sections) {
    var container = document.getElementById('homepageSectionList');
    container.innerHTML = '';
    sections.forEach(function(s, idx) {
        var cfg = sectionConfig[s.id] || { icon: 'bi-question', color: 'secondary', label: s.id, desc: '' };
        var link = sectionLinks[s.id] || '#';
        var col = document.createElement('div');
        col.className = 'col-md-4 hp-section-card';
        col.dataset.id = s.id;
        col.innerHTML =
            '<div class="card h-100 border-0 shadow-sm">' +
                '<div class="card-body text-center py-4">' +
                    '<div class="mb-3 position-relative">' +
                        '<i class="bi ' + cfg.icon + ' fs-1 text-' + cfg.color + '"></i>' +
                        '<span class="drag-handle position-absolute top-0 start-0"><i class="bi bi-grip-vertical fs-5"></i></span>' +
                    '</div>' +
                    '<h6 class="card-title">' + cfg.label + '</h6>' +
                    '<p class="text-muted small mb-3">' + cfg.desc + '</p>' +
                    '<div class="d-flex justify-content-center gap-2">' +
                        '<a href="' + link + '" class="btn btn-sm btn-outline-' + cfg.color + '"><i class="bi bi-gear me-1"></i>Quản lý</a>' +
                        '<div class="form-check form-switch d-inline-flex align-items-center ms-2">' +
                            '<input class="form-check-input" type="checkbox" role="switch" ' + (s.enabled !== false ? 'checked' : '') + ' onchange="toggleSection(\'' + s.id + '\', this.checked)">' +
                        '</div>' +
                    '</div>' +
                '</div>' +
            '</div>';
        container.appendChild(col);
    });
    document.getElementById('loadingSpinner').style.display = 'none';

    new Sortable(container, {
        handle: '.drag-handle',
        animation: 150,
        ghostClass: 'sortable-ghost',
        chosenClass: 'sortable-chosen',
        dragClass: 'sortable-drag',
        onEnd: function() {
            updateSectionOrder();
        }
    });
}

function updateSectionOrder() {
    var items = document.querySelectorAll('#homepageSectionList > .hp-section-card');
    sectionList = [];
    items.forEach(function(el) {
        var id = el.dataset.id;
        var cb = el.querySelector('input[type="checkbox"]');
        sectionList.push({ id: id, enabled: cb ? cb.checked : true });
    });
}

function toggleSection(id, checked) {
    var found = sectionList.find(function(s) { return s.id === id; });
    if (found) found.enabled = checked;
}

function saveSectionOrder() {
    updateSectionOrder();
    var btn = document.getElementById('btnSaveOrder');
    btn.disabled = true;
    btn.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span>Đang lưu...';

    var csrf = document.querySelector('meta[name="_csrf"]');
    var csrfHeader = document.querySelector('meta[name="_csrf_header"]');
    var headers = { 'Content-Type': 'application/json' };
    if (csrf && csrfHeader) {
        headers[csrfHeader.content] = csrf.content;
    }

    fetch('/admin/api/homepage-layout', {
        method: 'POST',
        headers: headers,
        body: JSON.stringify(sectionList)
    }).then(function(r) { return r.json(); }).then(function(data) {
        if (data.success) {
            showToast('Đã lưu thứ tự các section.', 'success');
        } else {
            showToast('Lưu thất bại.', 'error');
        }
    }).catch(function() {
        showToast('Lỗi kết nối.', 'error');
    }).finally(function() {
        btn.disabled = false;
        btn.innerHTML = '<i class="bi bi-floppy me-1"></i>Lưu thứ tự';
    });
}

function showToast(msg, type) {
    type = type || 'success';
    if (window.dsToast) { dsToast(type, msg); return; }
    var container = document.getElementById('toastContainer');
    if (!container) return;
    var iconMap = {success: 'bi-check-circle-fill', error: 'bi-x-circle-fill', warning: 'bi-exclamation-triangle-fill', info: 'bi-info-circle-fill'};
    var el = document.createElement('div');
    el.className = 'ds-toast ds-toast-' + type;
    el.innerHTML = '<i class="bi ' + (iconMap[type] || iconMap.success) + '"></i><span>' + msg + '</span><button class="ds-toast-close">&times;</button>';
    container.appendChild(el);
    el.querySelector('.ds-toast-close').addEventListener('click', function () { el.remove(); });
    setTimeout(function () {
        if (el.parentNode) {
            el.style.animation = 'ds-toast-fade-out .3s ease forwards';
            setTimeout(function () { el.remove(); }, 300);
        }
    }, 3000);
}

fetch('/admin/api/homepage-layout', {
    headers: { 'Accept': 'application/json' }
}).then(function(r) { return r.json(); }).then(function(data) {
    if (Array.isArray(data)) {
        renderSections(data);
        sectionList = JSON.parse(JSON.stringify(data));
    }
}).catch(function() {
    document.getElementById('loadingSpinner').style.display = 'none';
});

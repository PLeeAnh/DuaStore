/* =====================================================
 DuaStore — Module: Promotion / Voucher
 Dependency: api.js, toast.js, utils.js
 Shared by: index.html, promotion-list.html, wallet.html
 ===================================================== */
'use strict';

var _detailPromoId = null;

document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('.btn-detail').forEach(function (btn) {
        btn.addEventListener('click', function (e) {
            e.stopPropagation();
            openDetailModal(this.getAttribute('data-id'));
        });
    });
    document.querySelectorAll('.btn-save-voucher').forEach(function (btn) {
        btn.addEventListener('click', function (e) {
            e.stopPropagation();
            saveVoucher(this.getAttribute('data-id'));
        });
    });
});

function el(id) {
    return document.getElementById(id);
}

function openDetailModal(promoId) {
    _detailPromoId = promoId;
    fetch('/khuyen-mai/' + promoId)
            .then(function (r) {
                return r.json();
            })
            .then(function (d) {
                if (!d.success) {
                    if (typeof DuaStore !== 'undefined' && DuaStore.toast) { DuaStore.toast.error(d.message || 'Không thể tải chi tiết khuyến mãi'); }
                    return;
                }

                var els = ['dm-discount', 'dm-ten', 'dm-ma', 'dm-loai', 'dm-min', 'dm-max', 'dm-stack', 'dm-target',
                    'dm-tu', 'dm-den', 'dm-targetType', 'dm-product', 'dm-terms', 'dm-related-section',
                    'dm-related-list', 'dm-footer-action', 'detailModal'];
                for (var i = 0; i < els.length; i++) {
                    if (!el(els[i])) {
                        if (typeof DuaStore !== 'undefined' && DuaStore.toast) { DuaStore.toast.error('Lỗi hiển thị: không tìm thấy ' + els[i]); }
                        return;
                    }
                }

                var fmt = function (n) {
                    return parseInt(n).toLocaleString('vi-VN') + 'đ';
                };
                el('dm-discount').textContent = d.loaiGiam === 'PHAN_TRAM'
                        ? 'GIẢM ' + d.giaTriGiam + '%'
                        : 'GIẢM ' + fmt(d.giaTriGiam);
                el('dm-ten').textContent = d.tenChuongTrinh;
                el('dm-ma').textContent = d.maCode;
                el('dm-loai').textContent = d.targetType || 'Toàn bộ sản phẩm';
                el('dm-min').textContent = d.donHangToiThieu > 0 ? fmt(d.donHangToiThieu) : '--';
                el('dm-max').textContent = d.giamToiDa > 0 ? fmt(d.giamToiDa) : '--';
                el('dm-stack').textContent = d.stackable;
                el('dm-target').textContent = 'Tất cả khách hàng';
                el('dm-tu').textContent = d.tuNgay || '--';
                el('dm-den').textContent = d.denNgay || '--';
                el('dm-targetType').textContent = d.targetType || 'Tất cả';
                el('dm-product').textContent = 'Áp dụng hóa đơn chi nhánh';

                var terms = el('dm-terms');
                terms.innerHTML = '<li>Chỉ áp dụng mua sắm trực tuyến hoặc trực tiếp tại cơ sở DuaStore.</li>'
                        + '<li>Voucher không thể kết hợp cùng các chương trình VIP đồng thời ngoài trừ thông báo cụ thể.</li>';
                if (d.giamToiDa > 0)
                    terms.innerHTML += '<li>Hạn mức tối đa áp dụng là ' + fmt(d.giamToiDa) + '.</li>';

                var relSection = el('dm-related-section');
                var relList = el('dm-related-list');
                if (d.related && d.related.length > 0) {
                    relSection.style.display = '';
                    var html = '';
                    d.related.forEach(function (r) {
                        var dt = r.loaiGiam === 'PHAN_TRAM' ? 'Giảm ' + r.giaTriGiam + '%' : 'Giảm ' + fmt(r.giaTriGiam);
                        html += '<span class="related-chip" onclick="openDetailModal(' + r.id + ')" style="cursor:pointer">'
                                + '<span style="color:#2563eb;font-weight:600;">' + dt + '</span> '
                                + '<span style="color:#475569;">' + (r.tenChuongTrinh || '') + '</span></span>';
                    });
                    relList.innerHTML = html;
                } else {
                    relSection.style.display = 'none';
                }

                el('dm-footer-action').innerHTML =
                        '<button class="vc-btn ds-btn-fill" onclick="saveVoucherFromDetail(' + d.id + ')"><i class="bi bi-wallet2 me-1"></i>Nhận</button>';

                if (typeof bootstrap === 'undefined') {
                    if (typeof DuaStore !== 'undefined' && DuaStore.toast) { DuaStore.toast.error('Không thể hiển thị popup: Bootstrap chưa được tải'); }
                    return;
                }
                var modal = new bootstrap.Modal(el('detailModal'));
                modal.show();
            })
            .catch(function (err) {
                if (typeof DuaStore !== 'undefined' && DuaStore.toast) { DuaStore.toast.error('Không thể tải chi tiết khuyến mãi'); }
                console.error(err);
            });
}

function handleUnauthorized(r) {
    if (r.status === 401 || r.status === 403) {
        if (typeof showLoginPopup === 'function')
            showLoginPopup();
        return true;
    }
    return false;
}

function handleVoucherResponse(data, btn) {
    if (!data)
        return;
    if (data.message && data.message.indexOf('dang nhap') !== -1) {
        if (typeof DuaStore !== 'undefined' && DuaStore.toast) { DuaStore.toast.info(data.message); }
        if (typeof showLoginPopup === 'function')
            showLoginPopup();
        if (btn) {
            btn.disabled = false;
            btn.textContent = 'Nhận';
        }
        return;
    }
    if (data.success) {
        if (typeof DuaStore !== 'undefined' && DuaStore.toast) { DuaStore.toast.success(data.message); }
        if (btn) {
            btn.disabled = true;
            btn.style.opacity = '.4';
            btn.style.cursor = 'not-allowed';
        }
        location.reload();
    } else {
        if (typeof DuaStore !== 'undefined' && DuaStore.toast) { DuaStore.toast.error(data.message); }
        if (btn) {
            btn.disabled = false;
            btn.textContent = 'Nhận';
        }
    }
}

function saveVoucherFromDetail(promoId) {
    var btn = document.querySelector('#dm-footer-action .ds-btn-fill');
    btn.disabled = true;
    btn.textContent = 'Đang xử lý...';
    var token = document.querySelector('meta[name="_csrf"]')?.content || '';
    var header = document.querySelector('meta[name="_csrf_header"]')?.content || 'X-CSRF-TOKEN';
    fetch('/api/vi-voucher/luu/' + promoId, {method: 'POST', headers: {[header]: token}})
            .then(function (r) {
                if (handleUnauthorized(r))
                    return null;
                return r.json();
            })
            .then(function (data) {
                handleVoucherResponse(data, btn);
                if (data && data.success) {
                    var modal = bootstrap.Modal.getInstance(document.getElementById('detailModal'));
                    if (modal)
                        modal.hide();
                }
            });
}

function copyDetailCode() {
    var code = document.getElementById('dm-ma').textContent;
    if (!code)
        return;
    if (navigator.clipboard) {
        navigator.clipboard.writeText(code).then(function () {
            if (typeof DuaStore !== 'undefined' && DuaStore.toast) { DuaStore.toast.success('Đã copy mã: ' + code); }
        });
    } else {
        var ta = document.createElement('textarea');
        ta.value = code;
        document.body.appendChild(ta);
        ta.select();
        document.execCommand('copy');
        document.body.removeChild(ta);
        if (typeof DuaStore !== 'undefined' && DuaStore.toast) { DuaStore.toast.success('Đã copy mã: ' + code); }
    }
}

function saveVoucher(promotionId) {
    var btn = document.querySelector('.btn-save-voucher[data-id="' + promotionId + '"]');
    if (btn && btn.disabled)
        return;
    if (btn) {
        btn.disabled = true;
        btn.textContent = 'Đang xử lý...';
    }
    var token = document.querySelector('meta[name="_csrf"]')?.content || '';
    var header = document.querySelector('meta[name="_csrf_header"]')?.content || 'X-CSRF-TOKEN';
    var headers = {'Content-Type': 'application/json'};
    headers[header] = token;
    fetch('/api/vi-voucher/luu/' + promotionId, {method: 'POST', headers: headers})
            .then(function (r) {
                if (handleUnauthorized(r)) {
                    if (btn) {
                        btn.disabled = false;
                        btn.textContent = 'Nhận';
                    }
                    return null;
                }
                return r.json();
            })
            .then(function (data) {
                handleVoucherResponse(data, btn);
            });
}

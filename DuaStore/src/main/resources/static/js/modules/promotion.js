/* =====================================================
   DuaStore — Module: Promotion / Voucher
   Dependency: api.js, toast.js, utils.js
   Shared by: index.html, promotion-list.html, wallet.html
===================================================== */
'use strict';

var _detailPromoId = null;

document.addEventListener('DOMContentLoaded', function() {
    document.querySelectorAll('.btn-detail').forEach(function(btn) {
        btn.addEventListener('click', function(e) {
            e.stopPropagation();
            openDetailModal(this.getAttribute('data-id'));
        });
    });
    document.querySelectorAll('.btn-save-voucher').forEach(function(btn) {
        btn.addEventListener('click', function(e) {
            e.stopPropagation();
            saveVoucher(this.getAttribute('data-id'));
        });
    });
});

function openDetailModal(promoId) {
    _detailPromoId = promoId;
    fetch('/khuyen-mai/' + promoId)
        .then(function(r) { return r.json(); })
        .then(function(d) {
            var fmt = function(n) { return parseInt(n).toLocaleString('vi-VN') + 'đ'; };
            document.getElementById('dm-discount').textContent = d.loaiGiam === 'PHAN_TRAM'
                ? 'GIẢM ' + d.giaTriGiam + '%'
                : 'GIẢM ' + fmt(d.giaTriGiam);
            document.getElementById('dm-ten').textContent = d.tenChuongTrinh;
            document.getElementById('dm-ma').textContent = d.maCode;
            document.getElementById('dm-loai').textContent = d.targetType || 'Toàn bộ sản phẩm';
            document.getElementById('dm-min').textContent = d.donHangToiThieu > 0 ? fmt(d.donHangToiThieu) : '--';
            document.getElementById('dm-max').textContent = d.giamToiDa > 0 ? fmt(d.giamToiDa) : '--';
            document.getElementById('dm-stack').textContent = d.stackable;
            document.getElementById('dm-target').textContent = 'Tất cả khách hàng';
            document.getElementById('dm-tu').textContent = d.tuNgay || '--';
            document.getElementById('dm-den').textContent = d.denNgay || '--';
            document.getElementById('dm-targetType').textContent = d.targetType || 'Tất cả';
            document.getElementById('dm-product').textContent = 'Áp dụng hóa đơn chi nhánh';

            var terms = document.getElementById('dm-terms');
            terms.innerHTML = '<li>Chỉ áp dụng mua sắm trực tuyến hoặc trực tiếp tại cơ sở DuaStore.</li>'
                + '<li>Voucher không thể kết hợp cùng các chương trình VIP đồng thời ngoài trừ thông báo cụ thể.</li>';
            if (d.giamToiDa > 0) terms.innerHTML += '<li>Hạn mức tối đa áp dụng là ' + fmt(d.giamToiDa) + '.</li>';

            var relSection = document.getElementById('dm-related-section');
            var relList = document.getElementById('dm-related-list');
            if (d.related && d.related.length > 0) {
                relSection.style.display = '';
                var html = '';
                d.related.forEach(function(r) {
                    var dt = r.loaiGiam === 'PHAN_TRAM' ? 'Giảm ' + r.giaTriGiam + '%' : 'Giảm ' + fmt(r.giaTriGiam);
                    html += '<span class="related-chip" onclick="openDetailModal(' + r.id + ')" style="cursor:pointer">'
                        + '<span style="color:#2563eb;font-weight:600;">' + dt + '</span> '
                        + '<span style="color:#475569;">' + (r.tenChuongTrinh || '') + '</span></span>';
                });
                relList.innerHTML = html;
            } else {
                relSection.style.display = 'none';
            }

            document.getElementById('dm-footer-action').innerHTML =
                '<button class="vc-btn vc-btn-cta" onclick="saveVoucherFromDetail(' + d.id + ')"><i class="bi bi-wallet2 me-1"></i>Nhận</button>';

            if (typeof bootstrap === 'undefined') {
                showToast('Không thể hiển thị popup: Bootstrap chưa được tải');
                return;
            }
            var modal = new bootstrap.Modal(document.getElementById('detailModal'));
            modal.show();
        })
        .catch(function(err) {
            showToast('Không thể tải chi tiết khuyến mãi');
            console.error(err);
        });
}

function saveVoucherFromDetail(promoId) {
    var token = document.querySelector('meta[name="_csrf"]')?.content || '';
    var header = document.querySelector('meta[name="_csrf_header"]')?.content || 'X-CSRF-TOKEN';
    fetch('/api/vi-voucher/luu/' + promoId, { method: 'POST', headers: { [header]: token } })
        .then(function(r) {
            if (r.status === 403) {
                if (typeof showLoginPopup === 'function') showLoginPopup();
                return null;
            }
            return r.json();
        })
        .then(function(data) {
            if (!data) return;
            showToast(data.message);
            if (data.message && data.message.indexOf('dang nhap') !== -1) {
                if (typeof showLoginPopup === 'function') showLoginPopup();
                return;
            }
            if (data.success) {
                var modal = bootstrap.Modal.getInstance(document.getElementById('detailModal'));
                if (modal) modal.hide();
                location.reload();
            }
        });
}

function copyDetailCode() {
    var code = document.getElementById('dm-ma').textContent;
    if (!code) return;
    if (navigator.clipboard) {
        navigator.clipboard.writeText(code).then(function() { showToast('Đã copy mã: ' + code); });
    } else {
        var ta = document.createElement('textarea');
        ta.value = code;
        document.body.appendChild(ta);
        ta.select();
        document.execCommand('copy');
        document.body.removeChild(ta);
        showToast('Đã copy mã: ' + code);
    }
}

function saveVoucher(promotionId) {
    var token = document.querySelector('meta[name="_csrf"]')?.content || '';
    var header = document.querySelector('meta[name="_csrf_header"]')?.content || 'X-CSRF-TOKEN';
    var headers = { 'Content-Type': 'application/json' };
    headers[header] = token;
    fetch('/api/vi-voucher/luu/' + promotionId, { method: 'POST', headers: headers })
        .then(function(r) {
            if (r.status === 403) {
                if (typeof showLoginPopup === 'function') showLoginPopup();
                return null;
            }
            return r.json();
        })
        .then(function(data) {
            if (!data) return;
            showToast(data.message);
            if (data.message && data.message.indexOf('dang nhap') !== -1) {
                if (typeof showLoginPopup === 'function') showLoginPopup();
                return;
            }
            if (data.success) location.reload();
        });
}

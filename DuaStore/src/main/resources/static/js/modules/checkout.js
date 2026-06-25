/* =====================================================
   DuaStore — Module: Checkout
   Dependencies: api.js, toast.js
===================================================== */
'use strict';

/* ═══ ADDRESS MODAL ═══ */
function openAddressModal() {
    document.getElementById('addressModalList').style.display = 'block';
    document.getElementById('addressModalForm').style.display = 'none';
    document.getElementById('addressModalTitle').textContent = 'Quản lý địa chỉ';
    new bootstrap.Modal(document.getElementById('addressModal')).show();
}
function showAddressForm() {
    document.getElementById('addressModalList').style.display = 'none';
    document.getElementById('addressModalForm').style.display = 'block';
    document.getElementById('addressModalTitle').textContent = 'Thêm địa chỉ mới';
    document.getElementById('modalTenNguoiNhan').value = '';
    document.getElementById('modalSoDienThoai').value = '';
    document.getElementById('modalDiaChiCuThe').value = '';
    document.getElementById('modalPhuongXa').value = '';
    document.getElementById('modalQuanHuyen').value = '';
    document.getElementById('modalTinhThanh').value = '';
    document.getElementById('modalLatitude').value = '';
    document.getElementById('modalLongitude').value = '';
    document.getElementById('modalIsDefault').checked = false;
    document.getElementById('modalMapStatus').textContent = '';
    document.getElementById('modalGoogleMapFrame').src =
        'https://maps.google.com/maps?q=Hai+Phong,Vietnam&output=embed&z=13';
}
function showAddressList() {
    document.getElementById('addressModalList').style.display = 'block';
    document.getElementById('addressModalForm').style.display = 'none';
    document.getElementById('addressModalTitle').textContent = 'Quản lý địa chỉ';
}
function modalSearchMap() {
    var q = document.getElementById('modalDiaChiCuThe').value.trim();
    if (!q) return;
    var encoded = encodeURIComponent(q + ', Việt Nam');
    document.getElementById('modalGoogleMapFrame').src =
        'https://maps.google.com/maps?q=' + encoded + '&output=embed&z=16';
    document.getElementById('modalMapStatus').textContent = 'Đang tra cứu...';
    fetch('https://nominatim.openstreetmap.org/search?q=' + encoded + '&format=json&limit=1')
        .then(function(r) { return r.json(); })
        .then(function(data) {
            if (data && data.length > 0) {
                var loc = data[0];
                document.getElementById('modalLatitude').value = loc.lat;
                document.getElementById('modalLongitude').value = loc.lon;
                var parts = (loc.display_name || q).split(',').map(function(s) { return s.trim(); });
                var len = parts.length;
                if (len >= 1) document.getElementById('modalDiaChiCuThe').value = parts[0];
                if (len >= 2) document.getElementById('modalPhuongXa').value = parts[1];
                if (len >= 3) document.getElementById('modalQuanHuyen').value = parts[2];
                if (len >= 4) document.getElementById('modalTinhThanh').value = parts[len - 1];
                document.getElementById('modalMapStatus').textContent = 'Đã tìm: ' + (loc.display_name || q);
            } else {
                document.getElementById('modalMapStatus').textContent = 'Không tìm thấy địa chỉ';
            }
        })
        .catch(function() {
            document.getElementById('modalMapStatus').textContent = 'Lỗi tra cứu';
        });
}
function modalGetMyLocation() {
    if (!navigator.geolocation) {
        document.getElementById('modalMapStatus').textContent = 'Trình duyệt không hỗ trợ GPS';
        return;
    }
    document.getElementById('modalMapStatus').textContent = 'Đang lấy vị trí...';
    navigator.geolocation.getCurrentPosition(function(pos) {
        var lat = pos.coords.latitude;
        var lng = pos.coords.longitude;
        document.getElementById('modalGoogleMapFrame').src =
            'https://maps.google.com/maps?q=' + lat + ',' + lng + '&output=embed&z=16';
        document.getElementById('modalLatitude').value = lat;
        document.getElementById('modalLongitude').value = lng;
        fetch('https://nominatim.openstreetmap.org/reverse?lat=' + lat + '&lon=' + lng + '&format=json&accept-language=vi')
            .then(function(r) { return r.json(); })
            .then(function(data) {
                var a = data.address || {};
                document.getElementById('modalTinhThanh').value = a.city || a.state || a.town || '';
                document.getElementById('modalQuanHuyen').value = a.suburb || a.county || a.district || '';
                document.getElementById('modalPhuongXa').value = a.quarter || a.neighbourhood || a.village || '';
                document.getElementById('modalDiaChiCuThe').value =
                    (a.house_number ? a.house_number + ' ' : '') + (a.road || '');
                document.getElementById('modalMapStatus').textContent =
                    'Vị trí: ' + (data.display_name || lat + ', ' + lng);
            })
            .catch(function() {
                document.getElementById('modalMapStatus').textContent = 'Không thể lấy thông tin địa chỉ';
            });
    }, function() {
        document.getElementById('modalMapStatus').textContent = 'Không thể lấy vị trí. Kiểm tra quyền GPS.';
    });
}
function modalClearMap() {
    document.getElementById('modalDiaChiCuThe').value = '';
    document.getElementById('modalGoogleMapFrame').src =
        'https://maps.google.com/maps?q=Hai+Phong,Vietnam&output=embed&z=13';
    document.getElementById('modalLatitude').value = '';
    document.getElementById('modalLongitude').value = '';
    document.getElementById('modalMapStatus').textContent = '';
}
function parseAndFillModalAddress(q) {
    var parts = q.split(',').map(function(s) { return s.trim(); });
    var len = parts.length;
    if (len >= 1) document.getElementById('modalDiaChiCuThe').value = parts[0];
    if (len >= 2) document.getElementById('modalPhuongXa').value = parts[1];
    if (len >= 3) document.getElementById('modalQuanHuyen').value = parts[2];
    if (len >= 4) document.getElementById('modalTinhThanh').value = parts[3];
}
function saveAddressFromModal() {
    var ten = document.getElementById('modalTenNguoiNhan').value.trim();
    var sdt = document.getElementById('modalSoDienThoai').value.trim();
    var dc = document.getElementById('modalDiaChiCuThe').value.trim();
    if (!ten) { DuaStore.toast.warning('Vui lòng nhập họ tên'); return; }
    if (!sdt) { DuaStore.toast.warning('Vui lòng nhập SĐT'); return; }
    if (!dc) { DuaStore.toast.warning('Vui lòng nhập địa chỉ'); return; }

    var formData = new URLSearchParams();
    formData.append('tenNguoiNhan', ten);
    formData.append('soDienThoai', sdt);
    formData.append('diaChiCuThe', dc);
    formData.append('phuongXa', document.getElementById('modalPhuongXa').value.trim());
    formData.append('quanHuyen', document.getElementById('modalQuanHuyen').value.trim());
    formData.append('tinhThanh', document.getElementById('modalTinhThanh').value.trim());
    formData.append('latitude', document.getElementById('modalLatitude').value);
    formData.append('longitude', document.getElementById('modalLongitude').value);
    formData.append('isDefault', document.getElementById('modalIsDefault').checked);

    fetch('/address/api/save', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: formData
    })
    .then(function(r) { return r.json(); })
    .then(function(data) {
        if (data.success) {
            location.reload();
        } else {
            DuaStore.toast.error(data.message || 'Lưu thất bại');
        }
    })
    .catch(function() {
        DuaStore.toast.error('Lỗi kết nối');
    });
}
function setDefaultAddress(id) {
    DuaStore.api.post('/address/api/set-default/' + id)
        .then(function(result) {
            if (result.ok && result.data.success) location.reload();
        });
}
function deleteAddress(id) {
    if (!confirm('Xoá địa chỉ này?')) return;
    DuaStore.api.post('/address/api/delete/' + id)
        .then(function(result) {
            if (result.ok && result.data.success) location.reload();
        });
}

/* ═══ SHIPPING ═══ */
function getSelectedAddressId() {
    var sel = document.querySelector('input[name="addressId"]:checked');
    return sel ? sel.value : null;
}

function updateShipFee() {
    var addrId = getSelectedAddressId();
    var method = document.querySelector('input[name="phuongThucGiaoHang"]:checked')?.value || 'SHIP';
    if (!addrId) return;

    function fetchFee(m, cb) {
        DuaStore.api.get('/checkout/shipping-fee?addressId=' + addrId + '&method=' + m)
            .then(function(result) {
                if (result.ok && result.data.success) cb(result.data.fee);
            });
    }

    fetchFee(method, function(fee) {
        document.getElementById('shipFeeDisplay').textContent = fee.toLocaleString('vi-VN') + '₫';
        updateTotal();
    });
    fetchFee('SHIP', function(fee) {
        document.getElementById('shipTTPrice').textContent = fee.toLocaleString('vi-VN') + '₫';
    });
    fetchFee('NHAN_TAI_CONG', function(fee) {
        document.getElementById('shipNhanhPrice').textContent = fee.toLocaleString('vi-VN') + '₫';
    });
}

function updateTotal() {
    var subtotal = parseInt(document.getElementById('rawSubtotal').textContent) || 0;
    var fee = parseInt(document.getElementById('shipFeeDisplay').textContent.replace(/[^0-9]/g, '')) || 0;
    var total = subtotal + fee - window.appliedDiscount;
    document.getElementById('totalDisplay').textContent = (total < 0 ? 0 : total).toLocaleString('vi-VN') + '₫';
}

/* ═══ DOM READY ═══ */
document.addEventListener('DOMContentLoaded', function() {

    /* ── Modal closed → reload ── */
    document.getElementById('addressModal')?.addEventListener('hidden.bs.modal', function() {
        location.reload();
    });

    /* ── Suggestion box: input / keydown / click-away ── */
    var modalDebounceTimer;
    document.addEventListener('input', function(e) {
        if (e.target.id !== 'modalDiaChiCuThe') return;
        clearTimeout(modalDebounceTimer);
        var q = e.target.value.trim();
        var box = document.getElementById('modalSuggestionBox');
        if (!box) return;
        if (q.length < 3) { box.style.display = 'none'; return; }
        modalDebounceTimer = setTimeout(function() {
            fetch('https://nominatim.openstreetmap.org/search?q=' + encodeURIComponent(q + ', Việt Nam') + '&format=json&limit=5')
                .then(function(r) { return r.json(); })
                .then(function(data) {
                    box.innerHTML = '';
                    if (!data || data.length === 0) { box.style.display = 'none'; return; }
                    data.forEach(function(loc) {
                        var item = document.createElement('button');
                        item.type = 'button';
                        item.className = 'list-group-item list-group-item-action py-2 text-start';
                        item.textContent = loc.display_name;
                        item.addEventListener('click', function() {
                            document.getElementById('modalDiaChiCuThe').value = loc.display_name;
                            document.getElementById('modalLatitude').value = loc.lat;
                            document.getElementById('modalLongitude').value = loc.lon;
                            var parts = (loc.display_name || '').split(',').map(function(s) { return s.trim(); });
                            var len = parts.length;
                            document.getElementById('modalDiaChiCuThe').value = len >= 1 ? parts[0] : '';
                            document.getElementById('modalPhuongXa').value = len >= 2 ? parts[1] : '';
                            document.getElementById('modalQuanHuyen').value = len >= 3 ? parts[2] : '';
                            document.getElementById('modalTinhThanh').value = len >= 4 ? parts[len - 1] : '';
                            document.getElementById('modalGoogleMapFrame').src =
                                'https://maps.google.com/maps?q=' + loc.lat + ',' + loc.lon + '&output=embed&z=16';
                            document.getElementById('modalMapStatus').textContent = 'Đã chọn: ' + loc.display_name;
                            box.style.display = 'none';
                        });
                        box.appendChild(item);
                    });
                    box.style.display = 'block';
                });
        }, 300);
    });

    document.addEventListener('keydown', function(e) {
        if (e.target.id === 'modalDiaChiCuThe' && e.key === 'Enter') {
            e.preventDefault();
            modalSearchMap();
        }
    });

    document.addEventListener('click', function(e) {
        var box = document.getElementById('modalSuggestionBox');
        if (!e.target.closest('#modalDiaChiCuThe') && !e.target.closest('#modalSuggestionBox')) {
            if (box) box.style.display = 'none';
        }
    });

    /* ── QR payment confirm ── */
    document.getElementById('qrConfirmBtn')?.addEventListener('click', function() {
        var btn = this;
        btn.disabled = true;
        btn.textContent = 'Đang xử lý...';

        var form = document.getElementById('checkoutForm');
        var formData = new FormData(form);

        fetch('/checkout/api/create', {
            method: 'POST',
            body: new URLSearchParams(formData)
        })
        .then(function(r) { return r.json(); })
        .then(function(data) {
            if (data.success) {
                window.location.href = '/checkout/thanh-cong/' + data.orderId;
            } else {
                DuaStore.toast.error(data.message || 'Đặt hàng thất bại');
                btn.disabled = false;
                btn.textContent = 'Đã thanh toán';
            }
        })
        .catch(function() {
            DuaStore.toast.error('Lỗi kết nối');
            btn.disabled = false;
            btn.textContent = 'Đã thanh toán';
        });
    });

    /* ── Submit guard ── */
    document.getElementById('checkoutForm')?.addEventListener('submit', function(e) {
        var tt = document.querySelector('input[name="phuongThucTT"]:checked');
        if (!tt) return;

        if (tt.value === 'CHUYEN_KHOAN') {
            e.preventDefault();

            var totalEl = document.getElementById('totalDisplay');
            var amount = parseInt(totalEl.textContent.replace(/[^0-9]/g, '')) || 0;

            DuaStore.api.get('/checkout/api/qr-info?amount=' + amount)
                .then(function(result) {
                    if (!result.ok) { DuaStore.toast.error('Không thể tạo mã QR, vui lòng thử lại'); return; }
                    var data = result.data;
                    document.getElementById('qrPaymentImage').src = data.qrUrl;
                    document.getElementById('qrAccountNumber').textContent = data.accountNumber;
                    document.getElementById('qrAccountName').textContent = data.accountName;
                    document.getElementById('qrMaDon').textContent = '';
                    document.getElementById('qrAmount').textContent = amount.toLocaleString('vi-VN') + '₫';
                    new bootstrap.Modal(document.getElementById('qrPaymentModal')).show();
                });
        } else {
            e.preventDefault();

            var btn = document.querySelector('#checkoutForm button[type="submit"]');
            if (btn._submitted) return;
            btn._submitted = true;
            btn.disabled = true;
            btn.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span>Đang xử lý...';

            var form = document.getElementById('checkoutForm');
            var formData = new FormData(form);

            fetch('/checkout/api/create', {
                method: 'POST',
                body: new URLSearchParams(formData)
            })
            .then(function(r) { return r.json(); })
            .then(function(data) {
                if (data.success) {
                    window.location.href = '/checkout/thanh-cong/' + data.orderId;
                } else {
                    DuaStore.toast.error(data.message || 'Đặt hàng thất bại');
                    btn._submitted = false;
                    btn.disabled = false;
                    btn.innerHTML = 'Đặt hàng';
                }
            })
            .catch(function() {
                DuaStore.toast.error('Đặt hàng thất bại, vui lòng thử lại');
                btn._submitted = false;
                btn.disabled = false;
                btn.innerHTML = 'Đặt hàng';
            });
        }
    });

    /* ── Shipping method change ── */
    document.querySelectorAll('input[name="phuongThucGiaoHang"]').forEach(function(el) {
        el.addEventListener('change', function() {
            updateShipFee();
        });
    });

    /* ── Payment method change ── */
    document.querySelectorAll('input[name="phuongThucTT"]').forEach(function(el) {
        el.addEventListener('change', function() {
            document.getElementById('ckInfo').style.display =
                el.value === 'CHUYEN_KHOAN' && el.checked ? 'block' : 'none';
        });
    });

    /* ── Address selection change ── */
    document.querySelectorAll('input[name="addressId"]').forEach(function(el) {
        el.addEventListener('change', function() {
            updateShipFee();
        });
    });

    /* ── Apply promo ── */
    document.getElementById('checkoutApplyPromo')?.addEventListener('click', function() {
        var input = document.getElementById('checkoutPromoInput');
        var msgEl = document.getElementById('checkoutPromoMsg');
        var code = input.value.trim();
        if (!code) { msgEl.innerHTML = '<span class="text-danger">Vui lòng nhập mã</span>'; return; }
        var subtotal = parseInt(document.getElementById('rawSubtotal').textContent) || 0;
        msgEl.innerHTML = '<span class="text-muted">Đang kiểm tra...</span>';
        DuaStore.api.post('/api/coupon/validate', { maCode: code, subtotal })
            .then(function(result) {
                if (!result.ok) { msgEl.innerHTML = '<span class="text-danger">Lỗi kết nối, vui lòng thử lại</span>'; return; }
                var data = result.data;
                if (data.valid) {
                    msgEl.innerHTML = '<span class="text-success">&#10003; ' + data.message + '</span>';
                    window.appliedDiscount = parseInt(data.discount) || 0;
                    document.getElementById('discountDisplay').textContent = '-' + window.appliedDiscount.toLocaleString('vi-VN') + '₫';
                    document.getElementById('discountDisplay').className = 'text-danger';
                } else {
                    msgEl.innerHTML = '<span class="text-danger">&#10007; ' + data.message + '</span>';
                    window.appliedDiscount = 0;
                    document.getElementById('discountDisplay').textContent = '0₫';
                    document.getElementById('discountDisplay').className = '';
                }
                updateTotal();
            });
    });

    updateShipFee();

    /* ── Format all prices ── */
    document.querySelectorAll('.ds-summary-price').forEach(function(el) {
        var num = parseInt(el.textContent.replace(/[^0-9]/g, '')) || 0;
        el.textContent = num.toLocaleString('vi-VN') + '₫';
    });
    var subEl = document.getElementById('subtotalDisplay');
    if (subEl) {
        var subNum = parseInt(subEl.textContent.replace(/[^0-9]/g, '')) || 0;
        subEl.textContent = subNum.toLocaleString('vi-VN') + '₫';
    }
});
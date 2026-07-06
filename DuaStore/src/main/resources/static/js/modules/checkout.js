/* =====================================================
 DuaStore — Module: Checkout
 Dependencies: api.js, toast.js
 ===================================================== */
'use strict';

/* ═══ ADDRESS MODAL ═══ */
var modalProvinces = [];
var modalDistricts = [];
var modalWards = [];
var editingAddressId = null;

function loadModalProvinces() {
    return fetch('/api/location/provinces').then(function (r) {
        return r.json();
    }).then(function (data) {
        modalProvinces = data;
        return data;
    });
}
function loadModalDistricts(provinceCode) {
    return fetch('/api/location/districts?provinceCode=' + provinceCode).then(function (r) {
        return r.json();
    }).then(function (data) {
        modalDistricts = data;
        return data;
    });
}
function loadModalWards(districtCode) {
    return fetch('/api/location/wards?districtCode=' + districtCode).then(function (r) {
        return r.json();
    }).then(function (data) {
        modalWards = data;
        return data;
    });
}

/* ═══ LOCATION COMBOBOX (dropdown with search + checkmark, like a native <select>) ═══ */
var openComboId = null;

function initLocationCombo(comboId, opts) {
    // opts: { getItems, onSelect, placeholder }
    var combo = document.getElementById(comboId);
    if (!combo)
        return;
    combo.__opts = opts;
    var toggle = combo.querySelector('.ds-combo-toggle');
    var search = combo.querySelector('.ds-combo-search-input');

    toggle.addEventListener('click', function (e) {
        e.stopPropagation();
        if (combo.classList.contains('open')) {
            closeCombo(comboId);
            return;
        }
        if (openComboId)
            closeCombo(openComboId);
        var items = opts.getItems();
        if (!items.length) {
            DuaStore.toast.warning('Vui lòng chọn mục phía trên trước');
            return;
        }
        openComboId = comboId;
        combo.classList.add('open');
        search.value = '';
        renderComboList(comboId, '');
        setTimeout(function () {
            search.focus();
        }, 0);
    });

    search.addEventListener('input', function () {
        renderComboList(comboId, this.value);
    });
    search.addEventListener('click', function (e) {
        e.stopPropagation();
    });
}

function renderComboList(comboId, query) {
    var combo = document.getElementById(comboId);
    var opts = combo.__opts;
    var list = combo.querySelector('.ds-combo-list');
    var selectedCode = combo.dataset.selectedCode || '';
    var q = (query || '').toLowerCase().trim();
    var items = opts.getItems().filter(function (x) {
        return x.name.toLowerCase().indexOf(q) !== -1;
    });
    list.innerHTML = '';
    if (!items.length) {
        list.innerHTML = '<div class="ds-combo-empty">Không tìm thấy kết quả</div>';
        return;
    }
    items.forEach(function (item) {
        var row = document.createElement('button');
        row.type = 'button';
        row.className = 'ds-combo-item' + (String(item.code) === String(selectedCode) ? ' selected' : '');
        row.innerHTML = '<span>' + item.name + '</span>' +
                (String(item.code) === String(selectedCode) ? '<i class="bi bi-check-lg"></i>' : '');
        row.addEventListener('click', function (e) {
            e.stopPropagation();
            setComboValue(comboId, item.code, item.name);
            closeCombo(comboId);
            if (opts.onSelect)
                opts.onSelect(item);
        });
        list.appendChild(row);
    });
}

function setComboValue(comboId, code, name) {
    var combo = document.getElementById(comboId);
    combo.dataset.selectedCode = code != null ? code : '';
    combo.querySelector('.ds-combo-label').textContent = name || combo.__opts.placeholder;
    combo.querySelector('.ds-combo-label').classList.toggle('has-value', !!name);
    combo.querySelector('input[type="hidden"]').value = name || '';
}

function resetCombo(comboId) {
    setComboValue(comboId, '', '');
}

function closeCombo(comboId) {
    var combo = document.getElementById(comboId);
    if (combo)
        combo.classList.remove('open');
    if (openComboId === comboId)
        openComboId = null;
}

document.addEventListener('click', function () {
    if (openComboId)
        closeCombo(openComboId);
});
document.addEventListener('keydown', function (e) {
    if (e.key === 'Escape' && openComboId)
        closeCombo(openComboId);
});

function fuzzyFindLocation(list, rawName) {
    if (!rawName)
        return null;
    var needle = rawName.toLowerCase().trim();
    return list.find(function (x) {
        return x.name.toLowerCase() === needle;
    })
            || list.find(function (x) {
                return x.name.toLowerCase().indexOf(needle) !== -1 || needle.indexOf(x.name.toLowerCase()) !== -1;
            })
            || null;
}

function modalSetFromNominatim(tinhThanh, quanHuyen, phuongXa, diaChi) {
    document.getElementById('modalDiaChiCuTheText').value = diaChi || '';
    if (!tinhThanh)
        return;
    var found = fuzzyFindLocation(modalProvinces, tinhThanh);
    if (!found) {
        document.getElementById('modalMapStatus').textContent += ' (không khớp được tỉnh/thành, vui lòng chọn thủ công)';
        return;
    }
    setComboValue('modalTinhThanhCombo', found.code, found.name);
    resetCombo('modalQuanHuyenCombo');
    resetCombo('modalPhuongXaCombo');
    modalDistricts = [];
    modalWards = [];
    loadModalDistricts(found.code).then(function (districts) {
        if (!quanHuyen)
            return;
        var dfound = fuzzyFindLocation(districts, quanHuyen);
        if (!dfound)
            return;
        setComboValue('modalQuanHuyenCombo', dfound.code, dfound.name);
        return loadModalWards(dfound.code).then(function (wards) {
            if (!phuongXa)
                return;
            var wfound = fuzzyFindLocation(wards, phuongXa);
            if (wfound)
                setComboValue('modalPhuongXaCombo', wfound.code, wfound.name);
        });
    });
}

function openAddressModal() {
    document.getElementById('addressModalList').style.display = 'block';
    document.getElementById('addressModalForm').style.display = 'none';
    document.getElementById('addressModalTitle').textContent = 'Quản lý địa chỉ';
    bootstrap.Modal.getOrCreateInstance(document.getElementById('addressModal')).show();
}

function showAddressForm() {
    editingAddressId = null;
    document.getElementById('addressModalList').style.display = 'none';
    document.getElementById('addressModalForm').style.display = 'block';
    document.getElementById('addressModalTitle').textContent = 'Thêm địa chỉ mới';
    document.getElementById('modalTenNguoiNhan').value = '';
    document.getElementById('modalSoDienThoai').value = '';
    document.getElementById('modalDiaChiCuThe').value = '';
    document.getElementById('modalDiaChiCuTheText').value = '';
    resetCombo('modalTinhThanhCombo');
    resetCombo('modalQuanHuyenCombo');
    resetCombo('modalPhuongXaCombo');
    modalDistricts = [];
    modalWards = [];
    document.getElementById('modalLatitude').value = '';
    document.getElementById('modalLongitude').value = '';
    document.getElementById('modalIsDefault').checked = false;
    document.getElementById('modalMapStatus').textContent = '';
    document.getElementById('modalGoogleMapFrame').src =
            'https://maps.google.com/maps?q=Hai+Phong,Vietnam&output=embed&z=13';
    loadModalProvinces();
}

function showAddressList() {
    document.getElementById('addressModalList').style.display = 'block';
    document.getElementById('addressModalForm').style.display = 'none';
    document.getElementById('addressModalTitle').textContent = 'Quản lý địa chỉ';
}

function editAddress(id) {
    editingAddressId = id;
    document.getElementById('addressModalList').style.display = 'none';
    document.getElementById('addressModalForm').style.display = 'block';
    document.getElementById('addressModalTitle').textContent = 'Sửa địa chỉ';
    resetCombo('modalTinhThanhCombo');
    resetCombo('modalQuanHuyenCombo');
    resetCombo('modalPhuongXaCombo');
    var provincesPromise = loadModalProvinces();
    fetch('/address/api/' + id).then(function (r) {
        if (r.status === 403) {
            if (typeof showLoginPopup === 'function')
                showLoginPopup();
            return null;
        }
        return r.json();
    }).then(function (data) {
        if (!data)
            return;
        if (!data.success) {
            DuaStore.toast.error(data.message || 'Không thể tải địa chỉ');
            return;
        }
        document.getElementById('modalTenNguoiNhan').value = data.tenNguoiNhan || '';
        document.getElementById('modalSoDienThoai').value = data.soDienThoai || '';
        document.getElementById('modalDiaChiCuThe').value = data.diaChiCuThe || '';
        document.getElementById('modalDiaChiCuTheText').value = data.diaChiCuThe || '';
        document.getElementById('modalLatitude').value = data.latitude || '';
        document.getElementById('modalLongitude').value = data.longitude || '';
        document.getElementById('modalIsDefault').checked = !!data.isDefault;

        provincesPromise.then(function (provinces) {
            var foundProvince = fuzzyFindLocation(provinces, data.tinhThanh);
            if (!foundProvince)
                return;
            setComboValue('modalTinhThanhCombo', foundProvince.code, foundProvince.name);
            return loadModalDistricts(foundProvince.code).then(function (districts) {
                var foundDistrict = fuzzyFindLocation(districts, data.quanHuyen);
                if (!foundDistrict)
                    return;
                setComboValue('modalQuanHuyenCombo', foundDistrict.code, foundDistrict.name);
                return loadModalWards(foundDistrict.code).then(function (wards) {
                    var foundWard = fuzzyFindLocation(wards, data.phuongXa);
                    if (foundWard)
                        setComboValue('modalPhuongXaCombo', foundWard.code, foundWard.name);
                });
            });
        });
    });
}

function modalSearchMap() {
    var q = document.getElementById('modalDiaChiCuThe').value.trim();
    if (!q)
        return;
    var encoded = encodeURIComponent(q + ', Việt Nam');
    document.getElementById('modalGoogleMapFrame').src =
            'https://maps.google.com/maps?q=' + encoded + '&output=embed&z=16';
    document.getElementById('modalMapStatus').textContent = 'Đang tra cứu...';
    fetch('https://nominatim.openstreetmap.org/search?q=' + encoded + '&format=json&limit=1')
            .then(function (r) {
                return r.json();
            })
            .then(function (data) {
                if (data && data.length > 0) {
                    var loc = data[0];
                    document.getElementById('modalLatitude').value = loc.lat;
                    document.getElementById('modalLongitude').value = loc.lon;
                    var parts = (loc.display_name || q).split(',').map(function (s) {
                        return s.trim();
                    });
                    var len = parts.length;
                    modalSetFromNominatim(
                            len >= 4 ? parts[len - 1] : '',
                            len >= 3 ? parts[len - 2] : '',
                            len >= 2 ? parts[len - 3] : '',
                            parts[0] || ''
                            );
                    document.getElementById('modalMapStatus').textContent = 'Đã tìm: ' + (loc.display_name || q);
                } else {
                    document.getElementById('modalMapStatus').textContent = 'Không tìm thấy địa chỉ';
                }
            })
            .catch(function () {
                document.getElementById('modalMapStatus').textContent = 'Lỗi tra cứu';
            });
}

function modalGetMyLocation() {
    if (!navigator.geolocation) {
        document.getElementById('modalMapStatus').textContent = 'Trình duyệt không hỗ trợ GPS';
        return;
    }
    document.getElementById('modalMapStatus').textContent = 'Đang lấy vị trí...';
    navigator.geolocation.getCurrentPosition(function (pos) {
        var lat = pos.coords.latitude;
        var lng = pos.coords.longitude;
        document.getElementById('modalGoogleMapFrame').src =
                'https://maps.google.com/maps?q=' + lat + ',' + lng + '&output=embed&z=16';
        document.getElementById('modalLatitude').value = lat;
        document.getElementById('modalLongitude').value = lng;
        fetch('https://nominatim.openstreetmap.org/reverse?lat=' + lat + '&lon=' + lng + '&format=json&accept-language=vi')
                .then(function (r) {
                    return r.json();
                })
                .then(function (data) {
                    var a = data.address || {};
                    modalSetFromNominatim(
                            a.city || a.state || a.town || '',
                            a.suburb || a.county || a.district || '',
                            a.quarter || a.neighbourhood || a.village || '',
                            (a.house_number ? a.house_number + ' ' : '') + (a.road || '')
                            );
                    document.getElementById('modalMapStatus').textContent =
                            'Vị trí: ' + (data.display_name || lat + ', ' + lng);
                })
                .catch(function () {
                    document.getElementById('modalMapStatus').textContent = 'Không thể lấy thông tin địa chỉ';
                });
    }, function () {
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
    var parts = q.split(',').map(function (s) {
        return s.trim();
    });
    var len = parts.length;
    modalSetFromNominatim(
            len >= 4 ? parts[len - 1] : '',
            len >= 3 ? parts[len - 2] : '',
            len >= 2 ? parts[len - 3] : '',
            parts[0] || ''
            );
}

function saveAddressFromModal() {
    var ten = document.getElementById('modalTenNguoiNhan').value.trim();
    var sdt = document.getElementById('modalSoDienThoai').value.trim();
    var dc = document.getElementById('modalDiaChiCuTheText').value.trim();
    if (!ten) {
        DuaStore.toast.warning('Vui lòng nhập họ tên');
        return;
    }
    if (!sdt) {
        DuaStore.toast.warning('Vui lòng nhập SĐT');
        return;
    }
    if (!dc) {
        DuaStore.toast.warning('Vui lòng nhập địa chỉ');
        return;
    }
    if (!document.getElementById('modalTinhThanh').value) {
        DuaStore.toast.warning('Vui lòng nhập tỉnh/thành');
        return;
    }
    if (!document.getElementById('modalQuanHuyen').value) {
        DuaStore.toast.warning('Vui lòng nhập quận/huyện');
        return;
    }
    if (!document.getElementById('modalPhuongXa').value) {
        DuaStore.toast.warning('Vui lòng nhập phường/xã');
        return;
    }

    var formData = new URLSearchParams();
    if (editingAddressId)
        formData.append('id', editingAddressId);
    formData.append('tenNguoiNhan', ten);
    formData.append('soDienThoai', sdt);
    formData.append('diaChiCuThe', dc);
    formData.append('phuongXa', document.getElementById('modalPhuongXa').value);
    formData.append('quanHuyen', document.getElementById('modalQuanHuyen').value);
    formData.append('tinhThanh', document.getElementById('modalTinhThanh').value);
    formData.append('latitude', document.getElementById('modalLatitude').value);
    formData.append('longitude', document.getElementById('modalLongitude').value);
    formData.append('isDefault', document.getElementById('modalIsDefault').checked);

    fetch('/address/api/save', {
        method: 'POST',
        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
        body: formData
    })
            .then(function (r) {
                if (r.status === 403) {
                    if (typeof showLoginPopup === 'function')
                        showLoginPopup();
                    return null;
                }
                return r.json();
            })
            .then(function (data) {
                if (!data)
                    return;
                if (data.success) {
                    location.reload();
                } else {
                    DuaStore.toast.error(data.message || 'Lưu thất bại');
                }
            })
            .catch(function () {
                DuaStore.toast.error('Lỗi kết nối');
            });
}

function setDefaultAddress(id) {
    DuaStore.api.post('/address/api/set-default/' + id)
            .then(function (result) {
                if (result.ok && result.data.success)
                    location.reload();
            });
}
function deleteAddress(id) {
    if (!confirm('Xoá địa chỉ này?'))
        return;
    DuaStore.api.post('/address/api/delete/' + id)
            .then(function (result) {
                if (result.ok && result.data.success)
                    location.reload();
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
    if (!addrId)
        return;
    function fetchFee(m, cb) {
        DuaStore.api.get('/checkout/shipping-fee?addressId=' + addrId + '&method=' + m)
                .then(function (result) {
                    if (result.ok && result.data.success)
                        cb(result.data.fee);
                });
    }
    fetchFee(method, function (fee) {
        document.getElementById('shipFeeDisplay').textContent = fee.toLocaleString('vi-VN') + '₫';
        updateTotal();
    });
    fetchFee('SHIP', function (fee) {
        document.getElementById('shipTTPrice').textContent = fee.toLocaleString('en-US') + 'đ';
    });
}

function updateTotal() {
    var subtotal = parseInt(document.getElementById('rawSubtotal').textContent) || 0;
    var fee = parseInt(document.getElementById('shipFeeDisplay').textContent.replace(/[^0-9]/g, '')) || 0;
    var total = subtotal + fee - window.appliedDiscount;
    document.getElementById('totalDisplay').textContent = (total < 0 ? 0 : total).toLocaleString('vi-VN') + '₫';
}

/* ═══ DOM READY ═══ */
document.addEventListener('DOMContentLoaded', function () {
    if (document.querySelector('.ds-checkout-steps')) {
        document.body.classList.add('ds-checkout-page');
    }

    // NOTE: previously this reloaded the whole page on EVERY close of the address
    // modal, even if the user opened it and closed it without doing anything (losing
    // any in-progress checkout state like typed notes). Save/delete/set-default
    // already call location.reload() themselves when something actually changed, so
    // no blanket reload is needed here anymore.

    /* ── Map search (Nominatim) ── */
    var modalDebounceTimer;
    document.addEventListener('input', function (e) {
        if (e.target.id !== 'modalDiaChiCuThe')
            return;
        clearTimeout(modalDebounceTimer);
        var q = e.target.value.trim();
        var box = document.getElementById('modalSuggestionBox');
        if (!box)
            return;
        if (q.length < 3) {
            box.style.display = 'none';
            return;
        }
        modalDebounceTimer = setTimeout(function () {
            fetch('https://nominatim.openstreetmap.org/search?q=' + encodeURIComponent(q + ', Việt Nam') + '&format=json&limit=5')
                    .then(function (r) {
                        return r.json();
                    })
                    .then(function (data) {
                        box.innerHTML = '';
                        if (!data || data.length === 0) {
                            box.style.display = 'none';
                            return;
                        }
                        data.forEach(function (loc) {
                            var item = document.createElement('button');
                            item.type = 'button';
                            item.className = 'list-group-item list-group-item-action py-2 text-start';
                            item.textContent = loc.display_name;
                            item.addEventListener('click', function () {
                                document.getElementById('modalLatitude').value = loc.lat;
                                document.getElementById('modalLongitude').value = loc.lon;
                                var parts = (loc.display_name || '').split(',').map(function (s) {
                                    return s.trim();
                                });
                                var len = parts.length;
                                modalSetFromNominatim(
                                        len >= 4 ? parts[len - 1] : '',
                                        len >= 3 ? parts[len - 2] : '',
                                        len >= 2 ? parts[len - 3] : '',
                                        parts[0] || ''
                                        );
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

    document.addEventListener('keydown', function (e) {
        if (e.target.id === 'modalDiaChiCuThe' && e.key === 'Enter') {
            e.preventDefault();
            modalSearchMap();
        }
    });

    document.addEventListener('click', function (e) {
        var box = document.getElementById('modalSuggestionBox');
        if (!e.target.closest('#modalDiaChiCuThe') && !e.target.closest('#modalSuggestionBox')) {
            if (box)
                box.style.display = 'none';
        }
    });

    /* ── Location comboboxes ── */
    initLocationCombo('modalTinhThanhCombo', {
        getItems: function () {
            return modalProvinces;
        },
        placeholder: 'Chọn tỉnh/thành',
        onSelect: function (item) {
            resetCombo('modalQuanHuyenCombo');
            resetCombo('modalPhuongXaCombo');
            modalDistricts = [];
            modalWards = [];
            loadModalDistricts(item.code);
        }
    });
    initLocationCombo('modalQuanHuyenCombo', {
        getItems: function () {
            return modalDistricts;
        },
        placeholder: 'Chọn quận/huyện',
        onSelect: function (item) {
            resetCombo('modalPhuongXaCombo');
            modalWards = [];
            loadModalWards(item.code);
        }
    });
    initLocationCombo('modalPhuongXaCombo', {
        getItems: function () {
            return modalWards;
        },
        placeholder: 'Chọn phường/xã'
    });

    /* ── QR payment confirm ── */
    document.getElementById('qrConfirmBtn')?.addEventListener('click', function () {
        var btn = this;
        btn.disabled = true;
        btn.textContent = 'Đang xử lý...';
        var form = document.getElementById('checkoutForm');
        var formData = new FormData(form);
        fetch('/checkout/api/create', {method: 'POST', body: new URLSearchParams(formData)})
                .then(function (r) {
                    if (r.status === 403) {
                        if (typeof showLoginPopup === 'function')
                            showLoginPopup();
                        return null;
                    }
                    return r.json();
                })
                .then(function (data) {
                    if (!data) {
                        btn.disabled = false;
                        btn.textContent = 'Đã thanh toán';
                        return;
                    }
                    if (data.success) {
                        window.location.href = '/checkout/thanh-cong/' + data.orderId;
                    } else {
                        DuaStore.toast.error(data.message || 'Đặt hàng thất bại');
                        btn.disabled = false;
                        btn.textContent = 'Đã thanh toán';
                    }
                })
                .catch(function () {
                    DuaStore.toast.error('Lỗi kết nối');
                    btn.disabled = false;
                    btn.textContent = 'Đã thanh toán';
                });
    });

    /* ── Submit guard ── */
    document.getElementById('checkoutForm')?.addEventListener('submit', function (e) {
        var tt = document.querySelector('input[name="phuongThucTT"]:checked');
        if (!tt)
            return;
        if (tt.value === 'CHUYEN_KHOAN') {
            e.preventDefault();
            var totalEl = document.getElementById('totalDisplay');
            var amount = parseInt(totalEl.textContent.replace(/[^0-9]/g, '')) || 0;
            DuaStore.api.get('/checkout/api/qr-info?amount=' + amount)
                    .then(function (result) {
                        if (!result.ok) {
                            DuaStore.toast.error('Không thể tạo mã QR, vui lòng thử lại');
                            return;
                        }
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

            var btn = document.querySelector('#checkoutForm button[type="submit"], button[form="checkoutForm"]');
            if (!btn)
                return;
            if (btn._submitted) {
                e.preventDefault();
                return;
            }
            btn._submitted = true;
            btn.disabled = true;
            btn.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span>Đang xử lý...';

            var form = document.getElementById('checkoutForm');
            var formData = new FormData(form);

            fetch('/checkout/api/create', {
                method: 'POST',
                body: new URLSearchParams(formData)
            })
                    .then(function (r) {
                        if (r.status === 403) {
                            if (typeof showLoginPopup === 'function')
                                showLoginPopup();
                            return null;
                        }
                        return r.json();
                    })
                    .then(function (data) {
                        if (!data) {
                            btn._submitted = false;
                            btn.disabled = false;
                            btn.innerHTML = 'Đặt hàng';
                            return;
                        }
                        if (data.success) {
                            window.location.href = '/checkout/thanh-cong/' + data.orderId;
                        } else {
                            DuaStore.toast.error(data.message || 'Đặt hàng thất bại');
                            btn._submitted = false;
                            btn.disabled = false;
                            btn.innerHTML = 'Đặt hàng';
                        }
                    })
                    .catch(function () {
                        DuaStore.toast.error('Đặt hàng thất bại, vui lòng thử lại');
                        btn._submitted = false;
                        btn.disabled = false;
                        btn.innerHTML = 'Đặt hàng';
                    });
        }
    });

    document.querySelectorAll('input[name="phuongThucGiaoHang"]').forEach(function (el) {
        el.addEventListener('change', function () {
            updateShipFee();
        });
    });
    document.querySelectorAll('input[name="phuongThucTT"]').forEach(function (el) {
        el.addEventListener('change', function () {
            document.getElementById('ckInfo').style.display = el.value === 'CHUYEN_KHOAN' && el.checked ? 'block' : 'none';
        });
    });
    document.querySelectorAll('input[name="addressId"]').forEach(function (el) {
        el.addEventListener('change', function () {
            updateShipFee();
        });
    });

});

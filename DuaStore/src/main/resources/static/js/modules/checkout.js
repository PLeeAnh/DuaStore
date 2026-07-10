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
        // position fixed panel below toggle
        var panel = combo.querySelector('.ds-combo-panel');
        if (panel) {
            var rect = toggle.getBoundingClientRect();
            panel.style.left = rect.left + 'px';
            panel.style.top = (rect.bottom + 4) + 'px';
            panel.style.width = rect.width + 'px';
        }
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

function initCheckoutMap() {
    if (window.checkoutMap) return;
    var mapEl = document.getElementById('checkoutGoogleMap');
    if (!mapEl) return;
    var lat = window.storeLat || 20.8565;
    var lng = window.storeLng || 106.6756;
    window.checkoutMap = L.map(mapEl, { center: [lat, lng], zoom: 13, zoomControl: true });
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', { maxZoom: 19, attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>' }).addTo(window.checkoutMap);
    window.checkoutMarker = L.marker([lat, lng], { draggable: true }).addTo(window.checkoutMap);
    window.checkoutMarker.on('dragend', function () {
        var p = window.checkoutMarker.getLatLng();
        document.getElementById('modalLatitude').value = p.lat.toFixed(6);
        document.getElementById('modalLongitude').value = p.lng.toFixed(6);
    });
    window.checkoutMap.on('click', function (e) {
        window.checkoutMarker.setLatLng(e.latlng);
        document.getElementById('modalLatitude').value = e.latlng.lat.toFixed(6);
        document.getElementById('modalLongitude').value = e.latlng.lng.toFixed(6);
    });
    setTimeout(function () { window.checkoutMap.invalidateSize(); }, 200);
}

function reverseGeocodeForModal(lat, lng) {
    var url = 'https://nominatim.openstreetmap.org/reverse?format=json&lat=' + lat + '&lon=' + lng + '&addressdetails=1&accept-language=vi';
    fetch(url, { headers: { 'User-Agent': 'DuaStore/1.0' } })
        .then(function (r) { return r.json(); })
        .then(function (data) {
            if (!data || data.error) return;
            var comps = data.address || {};
            var city = comps.city || comps.town || comps.county || comps.state_district || '';
            var district = comps.district || '';
            var ward = comps.suburb || comps.neighbourhood || comps.village || '';
            var street = comps.road || comps.path || '';
            document.getElementById('modalDiaChiCuTheText').value = street;
            modalSetFromNominatim(city, district, ward, street);
            document.getElementById('modalMapStatus').textContent = 'Đã tìm: ' + (data.display_name || '');
        });
}

function openAddressModal() {
    document.getElementById('addressModalList').style.display = 'block';
    document.getElementById('addressModalForm').style.display = 'none';
    document.getElementById('addressModalTitle').textContent = 'Quản lý địa chỉ';
    var addBtn = document.getElementById('addressAddBtn');
    if (addBtn) addBtn.style.display = '';
    bootstrap.Modal.getOrCreateInstance(document.getElementById('addressModal')).show();
}

function showAddressForm() {
    editingAddressId = null;
    document.getElementById('addressModalList').style.display = 'none';
    document.getElementById('addressModalForm').style.display = 'block';
    document.getElementById('addressModalTitle').textContent = 'Thêm địa chỉ mới';
    var addBtn = document.getElementById('addressAddBtn');
    if (addBtn) addBtn.style.display = 'none';
    document.getElementById('modalTenNguoiNhan').value = '';
    document.getElementById('modalSoDienThoai').value = '';
    document.getElementById('modalDiaChiCuThe').value = '';
    document.getElementById('modalDiaChiCuTheText').value = '';
    initCheckoutMap();
    resetCombo('modalTinhThanhCombo');
    resetCombo('modalQuanHuyenCombo');
    resetCombo('modalPhuongXaCombo');
    modalDistricts = [];
    modalWards = [];
    document.getElementById('modalLatitude').value = '';
    document.getElementById('modalLongitude').value = '';
    document.getElementById('modalIsDefault').checked = false;
    document.getElementById('modalMapStatus').textContent = '';
    if (window.checkoutMap) {
        var lat = window.storeLat || 20.8565;
        var lng = window.storeLng || 106.6756;
        checkoutMap.setView([lat, lng], 13);
        if (window.checkoutMarker) checkoutMarker.setLatLng([lat, lng]);
    }
    loadModalProvinces();
}

function showAddressList() {
    document.getElementById('addressModalList').style.display = 'block';
    document.getElementById('addressModalForm').style.display = 'none';
    document.getElementById('addressModalTitle').textContent = 'Quản lý địa chỉ';
    var addBtn = document.getElementById('addressAddBtn');
    if (addBtn) addBtn.style.display = '';
}

function editAddress(id) {
    editingAddressId = id;
    document.getElementById('addressModalList').style.display = 'none';
    document.getElementById('addressModalForm').style.display = 'block';
    document.getElementById('addressModalTitle').textContent = 'Sửa địa chỉ';
    var addBtn = document.getElementById('addressAddBtn');
    if (addBtn) addBtn.style.display = 'none';
    initCheckoutMap();
    resetCombo('modalTinhThanhCombo');
    resetCombo('modalQuanHuyenCombo');
    resetCombo('modalPhuongXaCombo');
    var provincesPromise = loadModalProvinces();
    fetch('/address/api/' + id).then(function (r) {
        if (r.status === 401 || r.status === 403) {
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
        var lat = data.latitude;
        var lng = data.longitude;
        document.getElementById('modalLatitude').value = lat || '';
        document.getElementById('modalLongitude').value = lng || '';
        document.getElementById('modalIsDefault').checked = !!data.isDefault;
        if (lat && lng && window.checkoutMap) {
            checkoutMap.setView([lat, lng], 16);
            if (window.checkoutMarker) checkoutMarker.setLatLng([lat, lng]);
        }

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
    if (!q) return;
    document.getElementById('modalMapStatus').textContent = 'Đang tra cứu...';
    var url = 'https://nominatim.openstreetmap.org/search?format=json&q=' + encodeURIComponent(q + ', Việt Nam') + '&limit=5&addressdetails=1&accept-language=vi';
    fetch(url, { headers: { 'User-Agent': 'DuaStore/1.0' } })
        .then(function (r) { return r.json(); })
        .then(function (results) {
            if (results && results.length) {
                var r = results[0];
                var lat = parseFloat(r.lat), lng = parseFloat(r.lon);
                document.getElementById('modalLatitude').value = lat;
                document.getElementById('modalLongitude').value = lng;
                if (window.checkoutMap) { checkoutMap.setView([lat, lng], 16); }
                if (window.checkoutMarker) checkoutMarker.setLatLng([lat, lng]);
                var comps = r.address || {};
                var city = comps.city || comps.town || comps.county || comps.state_district || '';
                var district = comps.district || '';
                var ward = comps.suburb || comps.neighbourhood || comps.village || '';
                var street = comps.road || comps.path || '';
                modalSetFromNominatim(city, district, ward, street);
                document.getElementById('modalMapStatus').textContent = 'Đã tìm: ' + (r.display_name || '');
            } else {
                document.getElementById('modalMapStatus').textContent = 'Không tìm thấy địa chỉ';
            }
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
        document.getElementById('modalLatitude').value = lat;
        document.getElementById('modalLongitude').value = lng;
        if (window.checkoutMap) { checkoutMap.setView([lat, lng], 16); }
        if (window.checkoutMarker) checkoutMarker.setLatLng([lat, lng]);
        reverseGeocodeForModal(lat, lng);
    }, function () {
        document.getElementById('modalMapStatus').textContent = 'Không thể lấy vị trí. Kiểm tra quyền GPS.';
    });
}

function modalClearMap() {
    document.getElementById('modalDiaChiCuThe').value = '';
    document.getElementById('modalLatitude').value = '';
    document.getElementById('modalLongitude').value = '';
    document.getElementById('modalMapStatus').textContent = '';
    if (window.checkoutMap) {
        var lat = window.storeLat || 20.8565;
        var lng = window.storeLng || 106.6756;
        checkoutMap.setView([lat, lng], 13);
        if (window.checkoutMarker) checkoutMarker.setLatLng([lat, lng]);
    }
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

    var tinh = document.getElementById('modalTinhThanh').value;
    var quan = document.getElementById('modalQuanHuyen').value;
    var phuong = document.getElementById('modalPhuongXa').value;
    if (dc === tinh || dc === quan || dc === phuong) {
        DuaStore.toast.warning('Số nhà, đường không được trùng với tên tỉnh/quận/phường');
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
                if (r.status === 401 || r.status === 403) {
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
        var el = document.getElementById('shipTTPrice');
        if (el) el.textContent = fee.toLocaleString('vi-VN') + 'đ';
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

    /* ── Nominatim Autocomplete ── */
    var modalAddrInput = document.getElementById('modalDiaChiCuThe');
    if (modalAddrInput) {
        var suggestionBox = document.getElementById('modalSuggestionBox');
        var searchTimeout;
        modalAddrInput.addEventListener('input', function () {
            clearTimeout(searchTimeout);
            var q = this.value.trim();
            if (q.length < 3) { if (suggestionBox) suggestionBox.style.display = 'none'; return; }
            searchTimeout = setTimeout(function () {
                fetch('https://nominatim.openstreetmap.org/search?format=json&q=' + encodeURIComponent(q + ', Việt Nam') + '&limit=5&addressdetails=1&accept-language=vi', { headers: { 'User-Agent': 'DuaStore/1.0' } })
                    .then(function (r) { return r.json(); })
                    .then(function (results) {
                        if (!suggestionBox) return;
                        suggestionBox.innerHTML = '';
                        if (!results || !results.length) { suggestionBox.style.display = 'none'; return; }
                        results.forEach(function (r) {
                            var a = document.createElement('button');
                            a.type = 'button';
                            a.className = 'list-group-item list-group-item-action';
                            a.textContent = r.display_name;
                            a.addEventListener('click', function () {
                                modalAddrInput.value = r.display_name;
                                suggestionBox.style.display = 'none';
                                var lat = parseFloat(r.lat), lng = parseFloat(r.lon);
                                document.getElementById('modalLatitude').value = lat;
                                document.getElementById('modalLongitude').value = lng;
                                if (window.checkoutMap) { checkoutMap.setView([lat, lng], 16); }
                                if (window.checkoutMarker) checkoutMarker.setLatLng([lat, lng]);
                                var comps = r.address || {};
                                var city = comps.city || comps.town || comps.county || comps.state_district || '';
                                var district = comps.district || '';
                                var ward = comps.suburb || comps.neighbourhood || comps.village || '';
                                var street = comps.road || comps.path || '';
                                modalSetFromNominatim(city, district, ward, street);
                                document.getElementById('modalMapStatus').textContent = 'Đã tìm: ' + r.display_name;
                            });
                            suggestionBox.appendChild(a);
                        });
                        suggestionBox.style.display = '';
                    });
            }, 400);
        });
    }

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
                    if (r.status === 401 || r.status === 403) {
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
                        if (r.status === 401 || r.status === 403) {
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

    var addrModal = document.getElementById('addressModal');
    if (addrModal) {
        addrModal.addEventListener('shown.bs.modal', function () {
            if (window.checkoutMap) setTimeout(function() { window.checkoutMap.invalidateSize(); }, 150);
        });
    }

    /* ── Address selection from modal card click ── */
    document.getElementById('addressModalList')?.addEventListener('click', function (e) {
        var cardBody = e.target.closest('.ds-address-card-body');
        if (!cardBody) return;
        if (e.target.closest('.ds-address-card-actions') || e.target.closest('.ds-kebab-btn')) return;
        var id = cardBody.getAttribute('data-id');
        if (!id) return;
        var radio = document.querySelector('input[name="addressId"][value="' + id + '"]');
        if (radio) {
            radio.checked = true;
            radio.dispatchEvent(new Event('change'));
        }
        document.querySelectorAll('#addressModalList .ds-address-card').forEach(function (c) {
            c.classList.remove('is-selected');
        });
        var card = cardBody.closest('.ds-address-card');
        if (card) card.classList.add('is-selected');
    });

});

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
var modalProvincePromise = null;

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

function stripLocPrefix(name) {
    return name.toLowerCase().replace(/^(thành phố|tỉnh|quận|huyện|phường|xã|thị trấn|thị xã)\s+/g, '').trim();
}

function fuzzyFindLocation(list, rawName) {
    if (!rawName)
        return null;
    var needle = rawName.toLowerCase().trim().normalize('NFC');
    var needleStripped = stripLocPrefix(needle);
    return list.find(function (x) {
        return x.name.toLowerCase().normalize('NFC') === needle;
    })
            || list.find(function (x) {
                return stripLocPrefix(x.name.toLowerCase().normalize('NFC')) === needleStripped;
            })
            || list.find(function (x) {
                var xn = x.name.toLowerCase().normalize('NFC');
                return xn.indexOf(needle) !== -1 || needle.indexOf(xn) !== -1;
            })
            || list.find(function (x) {
                var xn = stripLocPrefix(x.name.toLowerCase().normalize('NFC'));
                return xn.indexOf(needleStripped) !== -1 || needleStripped.indexOf(xn) !== -1;
            })
            || null;
}

function modalSetFromNominatim(tinhThanh, quanHuyen, phuongXa, diaChi, displayName) {
    document.getElementById('modalDiaChiCuTheText').value = diaChi || '';
    if (!tinhThanh && !displayName)
        return;
    if (!modalProvinces.length) {
        document.getElementById('modalMapStatus').textContent += ' (chưa tải được danh sách tỉnh/thành, vui lòng chọn thủ công)';
        return;
    }
    var found = fuzzyFindLocation(modalProvinces, tinhThanh);
    if (!found && displayName) {
        var parts = displayName.split(/[,，]/).map(function(s) { return s.trim().normalize('NFC'); });
        for (var i = parts.length - 1; i >= 0; i--) {
            var fp = fuzzyFindLocation(modalProvinces, parts[i]);
            if (fp) { found = fp; break; }
        }
    }
    if (!found) {
        document.getElementById('modalMapStatus').textContent += ' (không khớp được "' + (tinhThanh || displayName || '') + '", vui lòng chọn thủ công)';
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
    if (window.checkoutMap && typeof window.checkoutMap.setView === 'function') return;
    var mapEl = document.getElementById('checkoutMap');
    if (!mapEl) return;
    try {
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
        setTimeout(function () { if (window.checkoutMap) window.checkoutMap.invalidateSize(); }, 200);
        setTimeout(function () { if (window.checkoutMap) window.checkoutMap.invalidateSize(); }, 500);
    } catch (e) {
        console.warn('Leaflet init failed:', e);
        window.checkoutMap = null;
    }
}

function reverseGeocodeForModal(lat, lng) {
    var url = 'https://nominatim.openstreetmap.org/reverse?format=json&lat=' + lat + '&lon=' + lng + '&addressdetails=1&accept-language=vi';
    fetch(url, { headers: { 'User-Agent': 'DuaStore/1.0' } })
        .then(function (r) { return r.json(); })
        .then(function (data) {
            if (!data || data.error) return;
            var comps = data.address || {};
            var city = comps.state || comps.region || comps.state_district || '';
            if (!city) {
                for (var k in comps) {
                    if (typeof comps[k] === 'string' && /^(thành phố|tỉnh)\s/i.test(comps[k].normalize('NFC'))) { city = comps[k]; break; }
                }
            }
            if (!city && data.display_name) {
                var parts = data.display_name.split(/[,，]/).map(function(s) { return s.trim().normalize('NFC'); });
                for (var i = parts.length - 1; i >= 0; i--) {
                    var p = parts[i].toLowerCase().normalize('NFC');
                    if (p.indexOf('thành phố'.normalize('NFC')) === 0 || p.indexOf('tỉnh'.normalize('NFC')) === 0) { city = parts[i]; break; }
                }
            }
            if (!city) city = comps.city || comps.town || '';
            var district = comps.county || comps.district || comps.city_district || '';
            var ward = comps.suburb || comps.neighbourhood || comps.quarter || comps.village || comps.hamlet || comps.town || comps.city_district || '';
            if (!ward && comps.city && /^(phường|xã|thị trấn)\s/i.test(comps.city)) {
                ward = comps.city;
            }
            var districtFallback = (comps.city && /^(phường|xã|thị trấn|thị xã)\s/i.test(comps.city))
                ? comps.city.replace(/^(phường|xã|thị trấn|thị xã)\s+/i, '').trim() : '';
            if (!district) district = districtFallback;
            var street = comps.house_number ? comps.house_number + ' ' : '';
            street += comps.road || comps.path || comps.pedestrian || comps.residential || comps.quarter || comps.neighbourhood || comps.suburb || comps.hamlet || comps.village || comps.town || '';
            street = street.trim();
            document.getElementById('modalDiaChiCuTheText').value = street;
            var setAddr = function () { modalSetFromNominatim(city, district, ward, street, data.display_name || ''); };
            if (modalProvincePromise) { modalProvincePromise.then(setAddr); } else { setAddr(); }
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
    if (window.checkoutMap && typeof window.checkoutMap.setView === 'function') {
        var lat = window.storeLat || 20.8565;
        var lng = window.storeLng || 106.6756;
        window.checkoutMap.setView([lat, lng], 13);
        if (window.checkoutMarker && typeof window.checkoutMarker.setLatLng === 'function') window.checkoutMarker.setLatLng([lat, lng]);
    }
    modalProvincePromise = loadModalProvinces();
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
            window.checkoutMap.setView([lat, lng], 16);
            if (window.checkoutMarker) window.checkoutMarker.setLatLng([lat, lng]);
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
                if (window.checkoutMap) { window.checkoutMap.setView([lat, lng], 16); }
                if (window.checkoutMarker) window.checkoutMarker.setLatLng([lat, lng]);
                var comps = r.address || {};
                var city = comps.state || comps.region || comps.state_district || '';
                if (!city) {
                    for (var k in comps) {
                        if (typeof comps[k] === 'string' && /^(thành phố|tỉnh)\s/i.test(comps[k].normalize('NFC'))) { city = comps[k]; break; }
                    }
                }
                if (!city && r.display_name) {
                    var parts = r.display_name.split(/[,，]/).map(function(s) { return s.trim().normalize('NFC'); });
                    for (var i = parts.length - 1; i >= 0; i--) {
                        var p = parts[i].toLowerCase().normalize('NFC');
                        if (p.indexOf('thành phố'.normalize('NFC')) === 0 || p.indexOf('tỉnh'.normalize('NFC')) === 0) { city = parts[i]; break; }
                    }
                }
                if (!city) city = comps.city || comps.town || '';
                var district = comps.county || comps.district || comps.city_district || '';
                var ward = comps.suburb || comps.neighbourhood || comps.quarter || comps.village || comps.hamlet || comps.town || comps.city_district || '';
                if (!ward && comps.city && /^(phường|xã|thị trấn)\s/i.test(comps.city)) {
                    ward = comps.city;
                }
                var districtFallback = (comps.city && /^(phường|xã|thị trấn|thị xã)\s/i.test(comps.city))
                    ? comps.city.replace(/^(phường|xã|thị trấn|thị xã)\s+/i, '').trim() : '';
                if (!district) district = districtFallback;
                var street = comps.house_number ? comps.house_number + ' ' : '';
                street += comps.road || comps.path || comps.pedestrian || comps.residential || comps.quarter || comps.neighbourhood || comps.suburb || comps.hamlet || comps.village || comps.town || '';
                street = street.trim();
                var setAddr = function () { modalSetFromNominatim(city, district, ward, street, r.display_name || ''); };
                if (modalProvincePromise) { modalProvincePromise.then(setAddr); } else { setAddr(); }
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
        if (window.checkoutMap && typeof window.checkoutMap.setView === 'function') { window.checkoutMap.setView([lat, lng], 16); }
        if (window.checkoutMarker && typeof window.checkoutMarker.setLatLng === 'function') window.checkoutMarker.setLatLng([lat, lng]);
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
    if (window.checkoutMap && typeof window.checkoutMap.setView === 'function') {
        var lat = window.storeLat || 20.8565;
        var lng = window.storeLng || 106.6756;
        window.checkoutMap.setView([lat, lng], 13);
        if (window.checkoutMarker && typeof window.checkoutMarker.setLatLng === 'function') window.checkoutMarker.setLatLng([lat, lng]);
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

async function saveAddressFromModal() {
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

    function stripLocPrefix(name) {
        return name.replace(/^(thành phố|tỉnh|quận|huyện|phường|xã|thị trấn|thị xã)\s+/i, '').trim().toLowerCase();
    }
    // Block if street address is JUST a location name with no real address
    var dcLower = dc.toLowerCase().trim();
    var selTinhStripped = stripLocPrefix(tinh);
    var selQuanStripped = stripLocPrefix(quan);
    var selPhuongStripped = stripLocPrefix(phuong);
    if (dcLower === selTinhStripped || dcLower === selQuanStripped || dcLower === selPhuongStripped) {
        DuaStore.toast.warning('Vui lòng nhập số nhà, đường cụ thể');
        return;
    }
    // Check if street address contains a DIFFERENT province name (potential fraud)
    if (modalProvinces.length) {
        for (var i = 0; i < modalProvinces.length; i++) {
            var pName = modalProvinces[i].name;
            var pStripped = stripLocPrefix(pName);
            if (pStripped === selTinhStripped) continue;
            if (dcLower.indexOf(pStripped) !== -1) {
                var confirmed = await DuaStore.confirm('Địa chỉ chi tiết có chứa "' + pName + '" nhưng bạn đang chọn "' + tinh + '". Vẫn lưu?');
                if (!confirmed) return;
                break;
            }
        }
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

    DuaStore.api.postForm('/address/api/save', formData)
            .then(function (result) {
                if (!result.ok) {
                    if (result.message) DuaStore.toast.error(result.message);
                    return;
                }
                if (result.data.success) {
                    location.reload();
                } else {
                    DuaStore.toast.error(result.data.message || 'Lưu thất bại');
                }
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

function updateAllQuotes() {
    var addrId = getSelectedAddressId();
    if (!addrId) return;
    var subtotalEl = document.getElementById('rawSubtotal');
    var subtotal = subtotalEl ? parseInt(subtotalEl.textContent) || 0 : 0;
    DuaStore.api.get('/checkout/api/quotes?addressId=' + addrId + '&subtotal=' + subtotal)
            .then(function (result) {
                if (!result.ok || !result.data.success) return;
                var quotes = result.data.quotes || [];
                var selectedCarrier = document.querySelector('input[name="shippingCarrier"]:checked')?.value;
                var firstFee = null;
                var cheapest = null;
                quotes.forEach(function (q) {
                    var el = document.getElementById('carrier' + q.carrierCode + 'Price');
                    if (el) {
                        if (q.fee === 0) {
                            el.textContent = 'Miễn phí';
                            el.className = 'ds-checkout-radio-price text-success';
                        } else {
                            el.textContent = q.fee.toLocaleString('vi-VN') + '₫';
                            el.className = 'ds-checkout-radio-price';
                        }
                    }
                    if (firstFee === null) firstFee = q.fee;
                    if (cheapest === null || q.fee < cheapest.fee) {
                        cheapest = { code: q.carrierCode, fee: q.fee, days: q.deliveryDays };
                    }
                });
                function setShipFeeDisplay(fee) {
                    var el = document.getElementById('shipFeeDisplay');
                    if (!el) return;
                    if (fee === 0) {
                        el.textContent = '✓ Miễn phí';
                        el.className = 'fw-semibold text-success';
                    } else {
                        el.textContent = fee.toLocaleString('vi-VN') + '₫';
                        el.className = 'fw-semibold';
                    }
                }
                // If no carrier selected, auto-select cheapest
                if (!selectedCarrier || !document.querySelector('input[name="shippingCarrier"][value="' + selectedCarrier + '"]')) {
                    if (cheapest) {
                        var radio = document.querySelector('input[name="shippingCarrier"][value="' + cheapest.code + '"]');
                        if (radio) { radio.checked = true; }
                        setShipFeeDisplay(cheapest.fee);
                        updateEstimatedDelivery(cheapest.days);
                    } else if (firstFee !== null) {
                        setShipFeeDisplay(firstFee);
                    }
                } else {
                    var selQuote = quotes.find(function (q) { return q.carrierCode === selectedCarrier; });
                    if (selQuote) {
                        setShipFeeDisplay(selQuote.fee);
                        updateEstimatedDelivery(selQuote.deliveryDays);
                    }
                }
                updateTotal();
            });
}

function updateEstimatedDelivery(days) {
    var today = new Date();
    var minDate = new Date(today); minDate.setDate(today.getDate() + (days || 7));
    var maxDate = new Date(today); maxDate.setDate(today.getDate() + (days || 7) + 2);
    var el = document.getElementById('estimatedDeliveryEl');
    if (el) el.textContent = minDate.toLocaleDateString('vi-VN') + ' – ' + maxDate.toLocaleDateString('vi-VN');
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
                                if (window.checkoutMap) { window.checkoutMap.setView([lat, lng], 16); }
                                if (window.checkoutMarker) window.checkoutMarker.setLatLng([lat, lng]);
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
        var formData = new URLSearchParams(new FormData(form));
        DuaStore.api.postForm('/checkout/api/create', formData)
                .then(function (result) {
                    if (!result.ok || !result.data) {
                        DuaStore.toast.error(result.message || 'Đặt hàng thất bại');
                        btn.disabled = false;
                        btn.textContent = 'Đã thanh toán';
                        return;
                    }
                    if (result.data.success) {
                        if (result.data.redirectUrl) {
                            window.location.href = result.data.redirectUrl;
                        } else {
                            window.location.href = '/checkout/thanh-cong/' + result.data.orderId;
                        }
                    } else {
                        DuaStore.toast.error(result.data.message || 'Đặt hàng thất bại');
                        btn.disabled = false;
                        btn.textContent = 'Đã thanh toán';
                    }
                });
    });

    /* ── Submit guard ── */
    document.getElementById('checkoutForm')?.addEventListener('submit', function (e) {
        var tt = document.querySelector('input[name="phuongThucTT"]:checked');
        if (!tt)
            return;
        e.preventDefault();

        var btn = document.querySelector('#checkoutForm button[type="submit"], button[form="checkoutForm"]');
        if (!btn)
            return;
        if (btn._submitted) {
            return;
        }
        btn._submitted = true;
        btn.disabled = true;
        btn.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span>Đang xử lý...';

        var form = document.getElementById('checkoutForm');
        var formData = new URLSearchParams(new FormData(form));

        DuaStore.api.postForm('/checkout/api/create', formData)
                .then(function (result) {
                    if (!result.ok || !result.data) {
                        DuaStore.toast.error(result.message || 'Đặt hàng thất bại');
                        btn._submitted = false;
                        btn.disabled = false;
                        btn.innerHTML = 'Đặt hàng';
                        return;
                    }
                if (result.data.success) {
                    if (result.data.vnpayUrl) {
                        window.location.href = result.data.vnpayUrl;
                    } else if (result.data.redirectUrl) {
                        window.location.href = result.data.redirectUrl;
                    } else {
                        window.location.href = '/checkout/thanh-cong/' + result.data.orderId;
                    }
                } else {
                    DuaStore.toast.error(result.data.message || 'Đặt hàng thất bại');
                    btn._submitted = false;
                    btn.disabled = false;
                    btn.innerHTML = 'Đặt hàng';
                }
            }).catch(function () {
                DuaStore.toast.error('Lỗi kết nối, vui lòng thử lại');
                btn._submitted = false;
                btn.disabled = false;
                btn.innerHTML = 'Đặt hàng';
            });
    });

    document.querySelectorAll('input[name="shippingCarrier"]').forEach(function (el) {
        el.addEventListener('change', function () {
            updateAllQuotes();
        });
    });
    document.querySelectorAll('input[name="phuongThucTT"]').forEach(function (el) {
        el.addEventListener('change', function () {
            var ckInfoEl = document.getElementById('ckInfo'); if (ckInfoEl) ckInfoEl.style.display = el.value === 'CHUYEN_KHOAN' && el.checked ? 'block' : 'none';
        });
    });
    /* ── Click address card in checkout → select radio + update fee ── */
    document.querySelector('.ds-address-card-list')?.addEventListener('click', function (e) {
        var card = e.target.closest('.ds-address-card');
        if (!card) return;
        if (e.target.closest('.ds-address-card-actions') || e.target.closest('.ds-kebab-btn')) return;
        var radio = card.querySelector('input[name="addressId"]');
        if (!radio) return;
        radio.checked = true;
        radio.dispatchEvent(new Event('change'));
    });

    document.querySelectorAll('input[name="addressId"]').forEach(function (el) {
        el.addEventListener('change', function () {
            updateAllQuotes();
            var label = this.closest('.ds-address-card');
            if (label) {
                document.querySelectorAll('.ds-address-card-list .ds-address-card').forEach(function (c) {
                    c.classList.remove('active');
                });
                label.classList.add('active');
            }
        });
    });

    var addrModal = document.getElementById('addressModal');
    if (addrModal) {
        addrModal.addEventListener('shown.bs.modal', function () {
            if (window.checkoutMap && typeof window.checkoutMap.invalidateSize === 'function') setTimeout(function() { window.checkoutMap.invalidateSize(); }, 150);
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
        closeAddressModal();
    });

    setTimeout(function () {
        if (document.querySelector('input[name="addressId"]:checked')) {
            updateAllQuotes();
        }
    }, 300);
});

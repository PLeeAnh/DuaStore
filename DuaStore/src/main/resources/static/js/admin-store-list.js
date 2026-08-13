'use strict';
var locProvinces = [];
var isEditMode = false;

function loadProvinces() {
    return fetch('/api/location/provinces').then(function (r) {
        return r.json();
    }).then(function (data) {
        locProvinces = data;
    });
}

function loadDistricts(provinceCode) {
    return fetch('/api/location/districts?provinceCode=' + provinceCode).then(function (r) {
        return r.json();
    }).then(function (data) {
        window.locDistricts = data;
    });
}

function loadWards(districtCode) {
    return fetch('/api/location/wards?districtCode=' + districtCode).then(function (r) {
        return r.json();
    }).then(function (data) {
        window.locWards = data;
    });
}

function setupAutocomplete(inputId, suggestId, hiddenId, source, onSelect) {
    var input = document.getElementById(inputId);
    var box = document.getElementById(suggestId);
    var hidden = document.getElementById(hiddenId);
    var timer;
    var lastSelected = '';

    input.addEventListener('input', function () {
        clearTimeout(timer);
        var q = this.value.toLowerCase().trim();
        if (q.length < 1) {
            box.style.display = 'none';
            hidden.value = '';
            lastSelected = '';
            return;
        }
        if (this.value !== lastSelected) {
            hidden.value = '';
        }
        timer = setTimeout(function () {
            var items = source().filter(function (x) {
                return x.name.toLowerCase().indexOf(q) !== -1;
            }).slice(0, 10);
            box.innerHTML = '';
            if (!items.length) {
                box.style.display = 'none';
                return;
            }
            items.forEach(function (item) {
                var btn = document.createElement('button');
                btn.type = 'button';
                btn.className = 'list-group-item list-group-item-action py-1 text-start small';
                btn.textContent = item.name;
                btn.addEventListener('click', function () {
                    input.value = item.name;
                    hidden.value = item.name;
                    lastSelected = item.name;
                    box.style.display = 'none';
                    if (onSelect)
                        onSelect(item);
                });
                box.appendChild(btn);
            });
            box.style.display = 'block';
        }, 200);
    });

    input.addEventListener('blur', function () {
        setTimeout(function () {
            box.style.display = 'none';
            if (input.value !== lastSelected) {
                hidden.value = '';
            }
        }, 200);
    });
    input.addEventListener('focus', function () {
        if (this.value.trim())
            this.dispatchEvent(new Event('input'));
    });
}

function setupModalAutocomplete() {
    setupAutocomplete('editTinhThanhInput', 'editTinhThanhSuggest', 'editTinhThanh',
            function () {
                return locProvinces;
            },
            function (item) {
                document.getElementById('editQuanHuyenInput').value = '';
                document.getElementById('editQuanHuyen').value = '';
                document.getElementById('editPhuongXaInput').value = '';
                document.getElementById('editPhuongXa').value = '';
                window.locDistricts = [];
                window.locWards = [];
                loadDistricts(item.code);
            }
    );

    setupAutocomplete('editQuanHuyenInput', 'editQuanHuyenSuggest', 'editQuanHuyen',
            function () {
                return window.locDistricts || [];
            },
            function (item) {
                document.getElementById('editPhuongXaInput').value = '';
                document.getElementById('editPhuongXa').value = '';
                window.locWards = [];
                loadWards(item.code);
            }
    );

    setupAutocomplete('editPhuongXaInput', 'editPhuongXaSuggest', 'editPhuongXa',
            function () {
                return window.locWards || [];
            },
            null
            );
}

function openAddModal() {
    isEditMode = false;
    document.getElementById('storeModalTitle').textContent = 'Thêm địa chỉ';
    document.getElementById('editStoreId').value = '';
    document.getElementById('editTenCuaHang').value = '';
    document.getElementById('editSoDienThoai').value = '';
    document.getElementById('editSoNha').value = '';
    document.getElementById('editDuong').value = '';
    document.getElementById('editTinhThanhInput').value = '';
    document.getElementById('editTinhThanh').value = '';
    document.getElementById('editQuanHuyenInput').value = '';
    document.getElementById('editQuanHuyen').value = '';
    document.getElementById('editPhuongXaInput').value = '';
    document.getElementById('editPhuongXa').value = '';
    document.getElementById('editEmail').value = '';
    document.getElementById('editLatitude').value = '';
    document.getElementById('editLongitude').value = '';
    document.getElementById('editIsActive').checked = true;
    document.getElementById('editIsDefault').checked = false;
    window.locDistricts = [];
    window.locWards = [];
    new bootstrap.Modal(document.getElementById('storeModal')).show();
}

function openEditModal(id) {
    isEditMode = true;
    document.getElementById('storeModalTitle').textContent = 'Sửa địa chỉ';
    document.getElementById('editStoreId').value = id;
    loadProvinces().then(function () {
        fetch('/admin/dia-chi/api/' + id).then(function (r) {
            return r.json();
        }).then(function (data) {
            document.getElementById('editTenCuaHang').value = data.tenCuaHang || '';
            document.getElementById('editSoDienThoai').value = data.soDienThoai || '';
            document.getElementById('editSoNha').value = data.soNha || '';
            document.getElementById('editDuong').value = data.duong || '';
            document.getElementById('editEmail').value = data.email || '';
            document.getElementById('editLatitude').value = data.latitude || '';
            document.getElementById('editLongitude').value = data.longitude || '';
            document.getElementById('editIsActive').checked = !!data.isActive;
            document.getElementById('editIsDefault').checked = !!data.isDefault;

            document.getElementById('editTinhThanhInput').value = data.tinhThanh || '';
            document.getElementById('editTinhThanh').value = data.tinhThanh || '';
            var foundProvince = locProvinces.find(function (p) {
                return p.name === data.tinhThanh;
            });
            if (foundProvince) {
                loadDistricts(foundProvince.code).then(function () {
                    document.getElementById('editQuanHuyenInput').value = data.quanHuyen || '';
                    document.getElementById('editQuanHuyen').value = data.quanHuyen || '';
                    var foundDistrict = (window.locDistricts || []).find(function (d) {
                        return d.name === data.quanHuyen;
                    });
                    if (foundDistrict) {
                        loadWards(foundDistrict.code).then(function () {
                            document.getElementById('editPhuongXaInput').value = data.phuongXa || '';
                            document.getElementById('editPhuongXa').value = data.phuongXa || '';
                        });
                    } else {
                        window.locWards = [];
                    }
                });
            } else {
                window.locDistricts = [];
                window.locWards = [];
            }
            new bootstrap.Modal(document.getElementById('storeModal')).show();
        });
    });
}

function validateStoreForm() {
    var errors = [];
    if (!document.getElementById('editTenCuaHang').value.trim())
        errors.push('Tên cửa hàng');
    if (!document.getElementById('editTinhThanh').value.trim())
        errors.push('Tỉnh/Thành phố');
    if (!document.getElementById('editQuanHuyen').value.trim())
        errors.push('Quận/Huyện');
    if (!document.getElementById('editPhuongXa').value.trim())
        errors.push('Phường/Xã');

    var lat = document.getElementById('editLatitude').value.trim();
    var lng = document.getElementById('editLongitude').value.trim();
    if (lat && isNaN(parseFloat(lat))) errors.push('Vĩ độ phải là số');
    if (lng && isNaN(parseFloat(lng))) errors.push('Kinh độ phải là số');

    if (errors.length) {
        if (typeof DuaStore !== 'undefined' && DuaStore.toast) {
            DuaStore.toast.error('Vui lòng kiểm tra: ' + errors.join(', '));
        }
        return false;
    }
    return true;
}

function saveStore() {
    if (!validateStoreForm()) return;

    var id = document.getElementById('editStoreId').value;
    var url = id ? '/admin/dia-chi/sua/' + id : '/admin/dia-chi/them-moi';
    var formData = new URLSearchParams();
    if (id)
        formData.append('id', id);
    formData.append('tenCuaHang', document.getElementById('editTenCuaHang').value.trim());
    formData.append('soDienThoai', document.getElementById('editSoDienThoai').value.trim());
    formData.append('soNha', document.getElementById('editSoNha').value.trim());
    formData.append('duong', document.getElementById('editDuong').value.trim());
    formData.append('phuongXa', document.getElementById('editPhuongXa').value);
    formData.append('quanHuyen', document.getElementById('editQuanHuyen').value);
    formData.append('tinhThanh', document.getElementById('editTinhThanh').value);
    formData.append('email', document.getElementById('editEmail').value.trim());
    formData.append('latitude', document.getElementById('editLatitude').value.trim());
    formData.append('longitude', document.getElementById('editLongitude').value.trim());
    formData.append('isActive', document.getElementById('editIsActive').checked ? 'true' : 'false');
    formData.append('isDefault', document.getElementById('editIsDefault').checked ? 'true' : 'false');

    var btn = document.getElementById('saveStoreBtn');
    btn.disabled = true;
    btn.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span>Đang lưu...';

    var token = document.querySelector('meta[name="_csrf"]')?.content || '';
    var header = document.querySelector('meta[name="_csrf_header"]')?.content || 'X-CSRF-TOKEN';
    var headers = {'Content-Type': 'application/x-www-form-urlencoded'};
    if (token) headers[header] = token;

    fetch(url, {
        method: 'POST',
        headers: headers,
        body: formData.toString()
    }).then(function (r) {
        if (r.ok) {
            window.location.href = '/admin/dia-chi';
        } else {
            return r.text().then(function (text) {
                var msg = 'Lỗi khi lưu địa chỉ';
                if (text && text.length < 200) msg = text;
                if (typeof DuaStore !== 'undefined' && DuaStore.toast) { DuaStore.toast.error(msg); }
                btn.disabled = false;
                btn.innerHTML = '<i class="bi bi-check-lg me-1"></i>Lưu';
            });
        }
    }).catch(function () {
        if (typeof DuaStore !== 'undefined' && DuaStore.toast) { DuaStore.toast.error('Lỗi kết nối'); }
        btn.disabled = false;
        btn.innerHTML = '<i class="bi bi-check-lg me-1"></i>Lưu';
    });
}

document.addEventListener('DOMContentLoaded', function () {
    loadProvinces();
    setupModalAutocomplete();
    document.addEventListener('click', function (e) {
        if (!e.target.closest('#editTinhThanhInput, #editTinhThanhSuggest, #editQuanHuyenInput, #editQuanHuyenSuggest, #editPhuongXaInput, #editPhuongXaSuggest')) {
            document.getElementById('editTinhThanhSuggest').style.display = 'none';
            document.getElementById('editQuanHuyenSuggest').style.display = 'none';
            document.getElementById('editPhuongXaSuggest').style.display = 'none';
        }
    });
});

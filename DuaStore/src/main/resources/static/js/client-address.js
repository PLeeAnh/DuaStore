var districtCache = {};
var wardCache = {};

function showAddressModal() {
    document.getElementById('addressModalTitle').textContent = 'Thêm địa chỉ mới';
    document.getElementById('addressForm').reset();
    document.getElementById('addressId').value = '';
    document.getElementById('inputQuan').innerHTML = '<option value="">Chọn quận/huyện</option>';
    document.getElementById('inputPhuong').innerHTML = '<option value="">Chọn phường/xã</option>';
    new bootstrap.Modal('#addressModal').show();
}

function getProvinceCode(provinceName) {
    var opt = document.querySelector('#inputTinh option[value="' + provinceName.replace(/"/g, '\\"') + '"]');
    return opt ? opt.getAttribute('data-code') : '';
}

async function loadDistricts(provinceName) {
    var sel = document.getElementById('inputQuan');
    sel.innerHTML = '<option value="">Đang tải...</option>';
    if (districtCache[provinceName]) {
        sel.innerHTML = '<option value="">Chọn quận/huyện</option>' + districtCache[provinceName].map(d => '<option value="' + d.name + '" data-code="' + d.code + '">' + d.name + '</option>').join('');
        return;
    }
    var code = getProvinceCode(provinceName);
    if (!code) { sel.innerHTML = '<option value="">Chọn quận/huyện</option>'; return; }
    try {
        var resp = await fetch('/api/location/districts?provinceCode=' + encodeURIComponent(code));
        var data = await resp.json();
        districtCache[provinceName] = data;
        sel.innerHTML = '<option value="">Chọn quận/huyện</option>' + data.map(d => '<option value="' + d.name + '" data-code="' + d.code + '">' + d.name + '</option>').join('');
    } catch (e) { sel.innerHTML = '<option value="">Lỗi tải dữ liệu</option>'; }
    document.getElementById('inputPhuong').innerHTML = '<option value="">Chọn phường/xã</option>';
}

async function loadWards(districtName) {
    var sel = document.getElementById('inputPhuong');
    sel.innerHTML = '<option value="">Đang tải...</option>';
    if (wardCache[districtName]) {
        sel.innerHTML = '<option value="">Chọn phường/xã</option>' + wardCache[districtName].map(w => '<option value="' + w.name + '">' + w.name + '</option>').join('');
        return;
    }
    var districtOpt = document.querySelector('#inputQuan option[value="' + districtName.replace(/"/g, '\\"') + '"]');
    var dCode = districtOpt ? districtOpt.getAttribute('data-code') : '';
    if (!dCode) { sel.innerHTML = '<option value="">Chọn phường/xã</option>'; return; }
    try {
        var resp = await fetch('/api/location/wards?districtCode=' + encodeURIComponent(dCode));
        var data = await resp.json();
        wardCache[districtName] = data;
        sel.innerHTML = '<option value="">Chọn phường/xã</option>' + data.map(w => '<option value="' + w.name + '">' + w.name + '</option>').join('');
    } catch (e) { sel.innerHTML = '<option value="">Lỗi tải dữ liệu</option>'; }
}

document.getElementById('addressForm').addEventListener('submit', async function(e) {
    e.preventDefault();
    var formData = new FormData(this);
    var resp = await fetch('/address/api/save', { method: 'POST', body: new URLSearchParams(formData) });
    var result = await resp.json();
    if (result.success) { location.reload(); }
    else { if (typeof DuaStore !== 'undefined' && DuaStore.toast) { DuaStore.toast.error(result.message || 'Lỗi lưu địa chỉ'); } }
});

async function editAddress(id) {
    var resp = await fetch('/address/api/' + id);
    var data = await resp.json();
    if (!data.success) { if (typeof DuaStore !== 'undefined' && DuaStore.toast) { DuaStore.toast.error(data.message); } return; }
    document.getElementById('addressModalTitle').textContent = 'Địa chỉ';
    document.getElementById('addressId').value = data.id;
    document.getElementById('inputTen').value = data.tenNguoiNhan;
    document.getElementById('inputSDT').value = data.soDienThoai;
    document.getElementById('inputTinh').value = data.tinhThanh;
    await loadDistricts(data.tinhThanh);
    document.getElementById('inputQuan').value = data.quanHuyen;
    await loadWards(data.quanHuyen);
    document.getElementById('inputPhuong').value = data.phuongXa;
    document.getElementById('inputDiaChi').value = data.diaChiCuThe;
    document.getElementById('inputDefault').checked = data.isDefault;
    new bootstrap.Modal('#addressModal').show();
}

async function deleteAddress(id) {
    if (!confirm('Xóa địa chỉ này?')) return;
    var resp = await fetch('/address/api/delete/' + id, { method: 'POST' });
    var result = await resp.json();
    if (result.success) location.reload();
    else if (typeof DuaStore !== 'undefined' && DuaStore.toast) { DuaStore.toast.error(result.message || 'Lỗi xóa địa chỉ'); }
}

async function setDefaultAddress(id) {
    var resp = await fetch('/address/api/set-default/' + id, { method: 'POST' });
    var result = await resp.json();
    if (result.success) location.reload();
    else if (typeof DuaStore !== 'undefined' && DuaStore.toast) { DuaStore.toast.error(result.message || 'Lỗi'); }
}

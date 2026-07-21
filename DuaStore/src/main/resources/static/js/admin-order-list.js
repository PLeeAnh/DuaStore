'use strict';
function toggleCheckAll(source) {
    document.querySelectorAll('.order-checkbox').forEach(function (cb) {
        cb.checked = source.checked;
    });
    updateBatchBar();
}

function updateBatchBar() {
    var checked = document.querySelectorAll('.order-checkbox:checked');
    var bar = document.getElementById('batchBar');
    var count = document.getElementById('batchCount');
    if (checked.length > 0) {
        bar.style.display = 'block';
        count.textContent = 'Đã chọn ' + checked.length + ' đơn';
    } else {
        bar.style.display = 'none';
    }
}

function clearBatchSelection() {
    document.querySelectorAll('.order-checkbox').forEach(function (cb) {
        cb.checked = false;
    });
    document.getElementById('checkAll').checked = false;
    updateBatchBar();
}

function batchUpdateStatus() {
    if (!confirm('Xác nhận cập nhật trạng thái hàng loạt?')) return;
    var checked = document.querySelectorAll('.order-checkbox:checked');
    var status = document.getElementById('batchStatus').value;
    if (checked.length === 0) {
        DuaStore.toast.warning('Vui lòng chọn ít nhất một đơn hàng');
        return;
    }
    if (!status) {
        DuaStore.toast.warning('Vui lòng chọn trạng thái');
        return;
    }

    var ids = Array.from(checked).map(function (cb) {
        return parseInt(cb.value, 10);
    });
    var btn = document.getElementById('btnBatchUpdate');
    btn.disabled = true;
    btn.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span>Đang xử lý...';

    var csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
    var csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

    fetch('/admin/don-hang/api/batch-cap-nhat-trang-thai', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            [csrfHeader]: csrfToken
        },
        body: JSON.stringify({ ids: ids, status: status })
    })
    .then(function (r) { return r.json(); })
    .then(function (data) {
        if (data.success) {
            var msg = 'Đã cập nhật ' + data.updated + ' đơn hàng';
            if (data.errors && data.errors.length > 0) {
                msg += '. Lỗi: ' + data.errors.join('; ');
            }
            DuaStore.toast.success(msg);
            setTimeout(function () { location.reload(); }, 1500);
        } else {
            DuaStore.toast.error(data.message || 'Cập nhật thất bại');
        }
    })
    .catch(function () {
        DuaStore.toast.error('Lỗi kết nối');
    })
    .finally(function () {
        btn.disabled = false;
        btn.innerHTML = '<i class="bi bi-check2-circle me-1"></i>Cập nhật';
    });
}

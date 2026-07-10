'use strict';

function toggleAllCheckboxes() {
    var checked = document.getElementById('checkAll').checked;
    document.querySelectorAll('.bulk-check').forEach(function(cb) { cb.checked = checked; });
    updateBulkBar();
}
function updateBulkBar() {
    var checked = document.querySelectorAll('.bulk-check:checked');
    var bar = document.getElementById('bulkActionBar');
    if (checked.length > 0) {
        document.getElementById('bulkSelectedCount').textContent = checked.length;
        bar.style.display = 'flex';
    } else {
        bar.style.display = 'none';
    }
}
function clearBulkSelect() {
    document.querySelectorAll('.bulk-check').forEach(function(cb) { cb.checked = false; });
    document.getElementById('checkAll').checked = false;
    document.getElementById('bulkActionBar').style.display = 'none';
}
function bulkDelete() {
    var checked = document.querySelectorAll('.bulk-check:checked');
    if (checked.length === 0) return;
    if (!confirm('Xóa ' + checked.length + ' sản phẩm đã chọn?')) return;
    document.getElementById('bulkForm').submit();
}

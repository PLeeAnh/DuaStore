function toggleDetail(btn) {
    var id = btn.getAttribute('data-log-id');
    var row = document.getElementById('detail-' + id);
    var icon = btn.querySelector('i');
    if (row.style.display === 'none') {
        row.style.display = 'table-row';
        icon.className = 'bi bi-chevron-up';
    } else {
        row.style.display = 'none';
        icon.className = 'bi bi-chevron-down';
    }
}

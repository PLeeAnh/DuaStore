document.addEventListener('DOMContentLoaded', function() {
    var countEl = document.getElementById('dsFilterCountData');
    if (countEl) {
        var count = parseInt(countEl.dataset.count) || 0;
        var badge = document.getElementById('dsActiveFilterCount');
        if (badge) {
            if (count > 0) { badge.textContent = count; badge.style.display = ''; }
            else { badge.style.display = 'none'; }
        }
    }
    document.querySelectorAll('.pd-filter-more').forEach(function (btn) {
        var labelEl = btn.querySelector('.pd-filter-more-label');
        var originalLabel = labelEl ? labelEl.textContent : '';
        btn.addEventListener('click', function () {
            var list = btn.previousElementSibling;
            if (!list) return;
            var expanded = list.classList.toggle('pd-filter-expanded');
            list.querySelectorAll('.pd-filter-extra').forEach(function (li) {
                li.classList.toggle('d-none', !expanded);
            });
            btn.classList.toggle('pd-filter-more-open', expanded);
            if (labelEl) labelEl.textContent = expanded ? 'Thu gọn' : originalLabel;
        });
    });
});
function toggleMobileFilters() {
    var bar = document.getElementById('dsFilterBar');
    if (bar) bar.classList.toggle('ds-filter-bar-open');
}

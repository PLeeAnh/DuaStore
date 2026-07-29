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
});
function toggleMobileFilters() {
    var bar = document.getElementById('dsFilterBar');
    if (bar) bar.classList.toggle('ds-filter-bar-open');
}

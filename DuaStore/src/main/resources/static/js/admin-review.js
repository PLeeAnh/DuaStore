document.querySelectorAll('.review-admin-stars').forEach(function (el) {
    const score = parseInt(el.dataset.score) || 0;
    el.innerHTML = '\u2605'.repeat(score) + '\u2606'.repeat(5 - score);
});
document.addEventListener('DOMContentLoaded', function () {
    function doFilter() {
        var keyword = document.getElementById('searchKeyword').value;
        var approved = document.getElementById('filterApproved').value;
        var params = new URLSearchParams();
        if (keyword)
            params.set('keyword', keyword);
        if (approved)
            params.set('isApproved', approved);
        window.location.href = '/admin/danh-gia?' + params.toString();
    }
    var approvedEl = document.getElementById('filterApproved');
    if (approvedEl)
        approvedEl.addEventListener('change', doFilter);
    var searchTimer;
    var keywordEl = document.getElementById('searchKeyword');
    if (keywordEl)
        keywordEl.addEventListener('input', function () {
            clearTimeout(searchTimer);
            searchTimer = setTimeout(doFilter, 400);
        });
});

document.addEventListener('click', function (e) {
    if (e.target.classList.contains('module-checkbox')) {
        var module = e.target.getAttribute('data-module');
        document.querySelectorAll('.perm-checkbox[data-module="' + module + '"]').forEach(function (cb) {
            cb.checked = e.target.checked;
        });
    }
    if (e.target.classList.contains('perm-checkbox')) {
        var module = e.target.getAttribute('data-module');
        var all = document.querySelectorAll('.perm-checkbox[data-module="' + module + '"]');
        var checked = document.querySelectorAll('.perm-checkbox[data-module="' + module + '"]:checked');
        var modCb = document.querySelector('.module-checkbox[data-module="' + module + '"]');
        if (modCb) {
            modCb.checked = all.length === checked.length;
            modCb.indeterminate = checked.length > 0 && checked.length < all.length;
        }
    }
});

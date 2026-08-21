(function () {
    document.querySelectorAll('.ds-nav-mega').forEach(function (mega) {
        var items = mega.querySelectorAll('.ds-mega-sidebar-item');
        var groups = mega.querySelectorAll('.ds-mega-product-group');
        function activate(item) {
            items.forEach(function (i) { i.classList.toggle('active', i === item); });
            var target = item ? item.getAttribute('data-target') : null;
            groups.forEach(function (g) { g.classList.toggle('active', !target || g.id === target); });
        }
        items.forEach(function (item) {
            item.addEventListener('mouseenter', function () { activate(item); });
        });
        mega.addEventListener('mouseleave', function () { if (items.length) { activate(items[0]); } });
    });

    var megaLis = document.querySelectorAll('.ds-nav-mega');
    var enterTimer = null;
    var leaveTimer = null;
    var current = null;
    function closeAllMega(except) {
        megaLis.forEach(function (li) {
            if (li !== except) li.classList.remove('ds-mega-open');
        });
    }
    megaLis.forEach(function (li) {
        li.addEventListener('mouseenter', function () {
            clearTimeout(leaveTimer);
            enterTimer = setTimeout(function () {
                closeAllMega(li);
                li.classList.add('ds-mega-open');
                current = li;
            }, 100);
        });
        li.addEventListener('mouseleave', function () {
            clearTimeout(enterTimer);
            leaveTimer = setTimeout(function () {
                closeAllMega(null);
                current = null;
            }, 150);
        });
    });
})();

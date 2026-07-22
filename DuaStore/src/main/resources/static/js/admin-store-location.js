'use strict';
var locProvinces = [];

function loadProvinces() {
    fetch('/api/location/provinces').then(function (r) {
        return r.json();
    }).then(function (data) {
        locProvinces = data;
        var cur = document.getElementById('tinhThanh').value;
        if (cur) {
            document.getElementById('tinhThanhInput').value = cur;
            var found = data.find(function (p) {
                return p.name === cur;
            });
            if (found)
                loadDistricts(found.code);
        }
    });
}

function loadDistricts(provinceCode) {
    fetch('/api/location/districts?provinceCode=' + provinceCode).then(function (r) {
        return r.json();
    }).then(function (data) {
        var cur = document.getElementById('quanHuyen').value;
        if (cur)
            document.getElementById('quanHuyenInput').value = cur;
        window.locDistricts = data;
    });
}

function loadWards(districtCode) {
    fetch('/api/location/wards?districtCode=' + districtCode).then(function (r) {
        return r.json();
    }).then(function (data) {
        var cur = document.getElementById('phuongXa').value;
        if (cur)
            document.getElementById('phuongXaInput').value = cur;
        window.locWards = data;
    });
}

function setupAutocomplete(inputId, suggestId, hiddenId, source, onSelect) {
    var input = document.getElementById(inputId);
    var box = document.getElementById(suggestId);
    var hidden = document.getElementById(hiddenId);
    var timer;

    input.addEventListener('input', function () {
        clearTimeout(timer);
        var q = this.value.toLowerCase().trim();
        if (q.length < 1) {
            box.style.display = 'none';
            hidden.value = '';
            hidden.dispatchEvent(new Event('input', {bubbles: true}));
            return;
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
                    hidden.dispatchEvent(new Event('input', {bubbles: true}));
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
        }, 200);
    });
    input.addEventListener('focus', function () {
        if (this.value.trim())
            this.dispatchEvent(new Event('input'));
    });
}

document.addEventListener('DOMContentLoaded', function () {
    loadProvinces();

    setupAutocomplete('tinhThanhInput', 'tinhThanhSuggest', 'tinhThanh',
            function () {
                return locProvinces;
            },
            function (item) {
                document.getElementById('quanHuyenInput').value = '';
                document.getElementById('quanHuyen').value = '';
                document.getElementById('phuongXaInput').value = '';
                document.getElementById('phuongXa').value = '';
                window.locDistricts = [];
                window.locWards = [];
                loadDistricts(item.code);
            }
    );

    setupAutocomplete('quanHuyenInput', 'quanHuyenSuggest', 'quanHuyen',
            function () {
                return window.locDistricts || [];
            },
            function (item) {
                document.getElementById('phuongXaInput').value = '';
                document.getElementById('phuongXa').value = '';
                window.locWards = [];
                loadWards(item.code);
            }
    );

    setupAutocomplete('phuongXaInput', 'phuongXaSuggest', 'phuongXa',
            function () {
                return window.locWards || [];
            },
            null
            );

    document.addEventListener('click', function (e) {
        if (!e.target.closest('#tinhThanhInput, #tinhThanhSuggest, #quanHuyenInput, #quanHuyenSuggest, #phuongXaInput, #phuongXaSuggest')) {
            document.getElementById('tinhThanhSuggest').style.display = 'none';
            document.getElementById('quanHuyenSuggest').style.display = 'none';
            document.getElementById('phuongXaSuggest').style.display = 'none';
        }
    });
});

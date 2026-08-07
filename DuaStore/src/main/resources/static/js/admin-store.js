'use strict';
(function() {
    var bhInput = document.getElementById('businessHoursInput');
    if (!bhInput) return;
    var bhData = {};
    try { bhData = JSON.parse(bhInput.value || '{}'); } catch(e) { bhData = {}; }

    var allDays = ['mon','tue','wed','thu','fri','sat','sun'];
    var selectedDays = new Set();
    var dayBtns = document.getElementById('bhDayBtns');
    var dayPanel = document.getElementById('bhDayPanel');
    var bhAllDay = document.getElementById('bhAllDay');
    var bhClosed = document.getElementById('bhClosed');
    var bhSlots = document.getElementById('bhSlots');
    var bhAddSlot = document.getElementById('bhAddSlot');
    var bhSelectedLabel = document.getElementById('bhSelectedDays');

    function ensureDay(day) {
        if (!bhData[day]) bhData[day] = {open:false, allDay:false, slots:[]};
    }

    function renderButtons() {
        dayBtns.querySelectorAll('.bh-day-btn').forEach(function(btn) {
            var d = btn.dataset.day;
            btn.classList.toggle('selected', selectedDays.has(d));
        });
    }

    function renderSelectedLabel() {
        if (selectedDays.size === 0) {
            bhSelectedLabel.textContent = '';
            return;
        }
        var names = [];
        selectedDays.forEach(function(d) {
            if (d==='mon') names.push('T2'); else if (d==='tue') names.push('T3');
            else if (d==='wed') names.push('T4'); else if (d==='thu') names.push('T5');
            else if (d==='fri') names.push('T6'); else if (d==='sat') names.push('T7');
            else if (d==='sun') names.push('CN');
        });
        bhSelectedLabel.textContent = 'Áp dụng cho: ' + names.join(', ');
    }

    function getSelectedData() {
        var ref = null;
        selectedDays.forEach(function(d) {
            ensureDay(d);
            if (!ref) ref = JSON.parse(JSON.stringify(bhData[d]));
        });
        return ref;
    }

    function renderSlots() {
        var data = getSelectedData();
        if (!data || selectedDays.size === 0) {
            dayPanel.style.display = 'none';
            return;
        }
        dayPanel.style.display = '';
        bhAllDay.checked = !!data.allDay;
        bhClosed.checked = !data.open;
        bhSlots.innerHTML = '';
        if (data.allDay || !data.open) {
            bhSlots.style.display = 'none';
            bhAddSlot.style.display = 'none';
        } else {
            bhSlots.style.display = '';
            bhAddSlot.style.display = '';
            (data.slots || []).forEach(function(slot, idx) {
                var row = document.createElement('div');
                row.className = 'd-flex align-items-center gap-3 mb-2';
                row.innerHTML =
                    '<div style="flex:1">' +
                        '<label class="small text-muted">Giờ mở cửa</label>' +
                        '<input type="time" class="form-control form-control-sm bh-slot-open time-input" data-idx="' + idx + '" value="' + (slot.open||'') + '" step="60" />' +
                    '</div>' +
                    '<div style="flex:1">' +
                        '<label class="small text-muted">Giờ đóng cửa</label>' +
                        '<input type="time" class="form-control form-control-sm bh-slot-close time-input" data-idx="' + idx + '" value="' + (slot.close||'') + '" step="60" />' +
                    '</div>' +
                    '<button type="button" class="btn btn-sm mt-3 bh-slot-remove" data-idx="' + idx + '" style="color:#dc3545;border:none;font-size:1.1rem" title="Xóa"><i class="bi bi-x-lg"></i></button>';
                bhSlots.appendChild(row);
            });
        }
    }

    function applyToSelected(patch) {
        selectedDays.forEach(function(d) {
            ensureDay(d);
            Object.assign(bhData[d], patch);
        });
        renderButtons();
        renderSelectedLabel();
        renderSlots();
        saveBH();
    }

    dayBtns.addEventListener('click', function(e) {
        var btn = e.target.closest('.bh-day-btn');
        if (!btn) return;
        var d = btn.dataset.day;
        if (selectedDays.has(d)) {
            selectedDays.delete(d);
        } else {
            selectedDays.add(d);
        }
        renderButtons();
        renderSelectedLabel();
        renderSlots();
    });

    bhAllDay.addEventListener('change', function() {
        if (this.checked) {
            selectedDays.forEach(function(d) {
                ensureDay(d);
                bhData[d].allDay = true;
                bhData[d].open = true;
                bhData[d].slots = [];
            });
            bhClosed.checked = false;
        } else {
            selectedDays.forEach(function(d) {
                ensureDay(d);
                bhData[d].allDay = false;
                bhData[d].open = true;
                if (!bhData[d].slots || !bhData[d].slots.length) {
                    bhData[d].slots = [{open:'08:00', close:'19:00'}];
                }
            });
            bhClosed.checked = false;
        }
        renderButtons(); renderSelectedLabel(); renderSlots(); saveBH();
    });

    bhClosed.addEventListener('change', function() {
        if (this.checked) {
            selectedDays.forEach(function(d) {
                ensureDay(d);
                bhData[d].open = false;
                bhData[d].allDay = false;
                bhData[d].slots = [];
            });
            bhAllDay.checked = false;
        } else {
            selectedDays.forEach(function(d) {
                ensureDay(d);
                bhData[d].open = true;
            });
        }
        renderButtons(); renderSelectedLabel(); renderSlots(); saveBH();
    });

    bhAddSlot.addEventListener('click', function() {
        if (selectedDays.size === 0) return;
        var added = false;
        selectedDays.forEach(function(d) {
            ensureDay(d);
            if (!bhData[d].allDay && bhData[d].open) {
                if (!bhData[d].slots) bhData[d].slots = [];
                bhData[d].slots.push({open:'', close:''});
                added = true;
            }
        });
        if (!added) return;
        renderSlots(); saveBH();
    });

    bhSlots.addEventListener('click', function(e) {
        var rmBtn = e.target.closest('.bh-slot-remove');
        if (!rmBtn) return;
        var idx = parseInt(rmBtn.dataset.idx);
        selectedDays.forEach(function(d) {
            ensureDay(d);
            if (bhData[d] && bhData[d].slots && bhData[d].slots[idx]) {
                bhData[d].slots.splice(idx, 1);
            }
        });
        renderSlots(); saveBH();
    });

    bhSlots.addEventListener('input', function(e) {
        var idx = parseInt(e.target.dataset.idx);
        var val = e.target.value;
        var isOpen = e.target.classList.contains('bh-slot-open');
        selectedDays.forEach(function(d) {
            ensureDay(d);
            if (bhData[d] && bhData[d].slots && bhData[d].slots[idx]) {
                if (isOpen) bhData[d].slots[idx].open = val;
                else bhData[d].slots[idx].close = val;
            }
        });
        saveBH();
    });

    function saveBH() {
        var toSave = JSON.parse(JSON.stringify(bhData));
        bhInput.value = JSON.stringify(toSave);
        bhInput.dispatchEvent(new Event('input', {bubbles: true}));
    }

    allDays.forEach(function(d) {
        if (bhData[d] && (bhData[d].open || bhData[d].allDay || (bhData[d].slots && bhData[d].slots.length))) {
            selectedDays.add(d);
        }
    });
    renderButtons();
    renderSelectedLabel();
    renderSlots();

    var storeImagesInput = document.getElementById('storeImagesInput');
    var galleryGrid = document.getElementById('storeGalleryGrid');

    function refreshGallery() {
        var urls = storeImagesInput.value ? storeImagesInput.value.split(',').map(function(s){return s.trim()}).filter(Boolean) : [];
        galleryGrid.innerHTML = '';
        urls.forEach(function(url) {
            var div = document.createElement('div');
            div.className = 'adm-gallery-item store-img-item';
            div.dataset.url = url;
            div.innerHTML = '<img src="' + url + '" alt="Ảnh cửa hàng" />' +
                '<div class="adm-gallery-action">' +
                    '<button type="button" class="btn btn-sm btn-danger btn-icon store-img-remove" style="width:28px;height:28px;font-size:12px"><i class="bi bi-trash"></i></button>' +
                '</div>';
            galleryGrid.appendChild(div);
        });
    }

    galleryGrid.addEventListener('click', function(e) {
        var btn = e.target.closest('.store-img-remove');
        if (!btn) return;
        var item = btn.closest('.store-img-item');
        if (!item) return;
        var url = item.dataset.url;
        var urls = storeImagesInput.value.split(',').map(function(s){return s.trim()}).filter(Boolean);
        urls = urls.filter(function(u){ return u !== url });
        storeImagesInput.value = urls.join(',');
        storeImagesInput.dispatchEvent(new Event('input', {bubbles: true}));
        refreshGallery();
    });

    document.getElementById('storeImagesZone').addEventListener('click', function() {
        document.getElementById('storeImagesFile').click();
    });

    document.getElementById('storeImagesFile').addEventListener('change', function() {
        var files = Array.from(this.files);
        if (!files.length) return;
        var uploaded = [];
        var done = 0;
        files.forEach(function(file) {
            var fd = new FormData();
            fd.append('file', file);
            fetch('/admin/cua-hang/upload', { method: 'POST', body: fd })
                .then(function(r){ return r.json(); })
                .then(function(data){
                    if (data.url) uploaded.push(data.url);
                    done++;
                    if (done === files.length) {
                        var urls = storeImagesInput.value ? storeImagesInput.value.split(',').map(function(s){return s.trim()}).filter(Boolean) : [];
                        urls = urls.concat(uploaded);
                        storeImagesInput.value = urls.join(',');
                        storeImagesInput.dispatchEvent(new Event('input', {bubbles: true}));
                        refreshGallery();
                    }
                })
                .catch(function(){
                    done++;
                    if (done === files.length && uploaded.length) {
                        var urls = storeImagesInput.value ? storeImagesInput.value.split(',').map(function(s){return s.trim()}).filter(Boolean) : [];
                        urls = urls.concat(uploaded);
                        storeImagesInput.value = urls.join(',');
                        storeImagesInput.dispatchEvent(new Event('input', {bubbles: true}));
                        refreshGallery();
                    }
                });
        });
        this.value = '';
    });

    (function waitLeaflet() {
        if (typeof L !== 'undefined') {
            initLeafletMap();
            return;
        }
        setTimeout(waitLeaflet, 100);
    })();
})();

function initLeafletMap() {
    var latInput = document.getElementById('storeLatitude');
    var lngInput = document.getElementById('storeLongitude');
    var addrInput = document.getElementById('storeAddress');
    var lat = parseFloat(latInput?.value) || 20.8565;
    var lng = parseFloat(lngInput?.value) || 106.6756;
    var mapEl = document.getElementById('map');

    var map = L.map(mapEl, { center: [lat, lng], zoom: 15, zoomControl: true });
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', { maxZoom: 19, attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>' }).addTo(map);
    var marker = L.marker([lat, lng], { draggable: true }).addTo(map);

    function reverseGeocode(latlng) {
        var url = 'https://nominatim.openstreetmap.org/reverse?format=json&lat=' + latlng.lat + '&lon=' + latlng.lng + '&addressdetails=1&accept-language=vi';
        fetch(url, { headers: { 'User-Agent': 'DuaStore/1.0' } })
            .then(function(r) { return r.json(); })
            .then(function(data) {
                if (data && data.display_name) {
                    addrInput.value = data.display_name;
                    addrInput.dispatchEvent(new Event('input', {bubbles: true}));
                }
            });
    }

    marker.on('dragend', function() {
        var p = marker.getLatLng();
        latInput.value = p.lat.toFixed(6);
        lngInput.value = p.lng.toFixed(6);
        latInput.dispatchEvent(new Event('input', {bubbles: true}));
        lngInput.dispatchEvent(new Event('input', {bubbles: true}));
        reverseGeocode(p);
    });
    map.on('click', function(e) {
        marker.setLatLng(e.latlng);
        latInput.value = e.latlng.lat.toFixed(6);
        lngInput.value = e.latlng.lng.toFixed(6);
        latInput.dispatchEvent(new Event('input', {bubbles: true}));
        lngInput.dispatchEvent(new Event('input', {bubbles: true}));
        reverseGeocode(e.latlng);
    });

    var searchTimeout;
    addrInput.addEventListener('input', function() {
        clearTimeout(searchTimeout);
        var q = this.value.trim();
        if (q.length < 3) return;
        searchTimeout = setTimeout(function() {
            fetch('https://nominatim.openstreetmap.org/search?format=json&q=' + encodeURIComponent(q + ', Việt Nam') + '&limit=5&accept-language=vi', { headers: { 'User-Agent': 'DuaStore/1.0' } })
                .then(function(r) { return r.json(); })
                .then(function(results) {
                    if (results && results.length) {
                        var r = results[0];
                        var lat = parseFloat(r.lat);
                        var lng = parseFloat(r.lon);
                        map.setView([lat, lng], 15);
                        marker.setLatLng([lat, lng]);
                        latInput.value = lat.toFixed(6);
                        lngInput.value = lng.toFixed(6);
                        latInput.dispatchEvent(new Event('input', {bubbles: true}));
                        lngInput.dispatchEvent(new Event('input', {bubbles: true}));
                    }
                });
        }, 500);
    });

    if (addrInput.value) {
        fetch('https://nominatim.openstreetmap.org/search?format=json&q=' + encodeURIComponent(addrInput.value + ', Việt Nam') + '&limit=1&accept-language=vi', { headers: { 'User-Agent': 'DuaStore/1.0' } })
            .then(function(r) { return r.json(); })
            .then(function(results) {
                if (results && results.length) {
                    var r = results[0];
                    var lat = parseFloat(r.lat);
                    var lng = parseFloat(r.lon);
                    map.setView([lat, lng], 15);
                    marker.setLatLng([lat, lng]);
                    latInput.value = lat.toFixed(6);
                    lngInput.value = lng.toFixed(6);
                    latInput.dispatchEvent(new Event('input', {bubbles: true}));
                    lngInput.dispatchEvent(new Event('input', {bubbles: true}));
                }
            });
    }
}

function initContactMap(lat, lng) {
    var contactMap = L.map('contactMap', { center: [lat, lng], zoom: 16, zoomControl: true });
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', { maxZoom: 19, attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>' }).addTo(contactMap);
    L.marker([lat, lng]).addTo(contactMap).bindPopup('DuaStore');
}

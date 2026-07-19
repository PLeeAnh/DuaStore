function getCsrfHeaders() {
    const headers = {};
    const csrf = document.querySelector('meta[name=_csrf]');
    const header = document.querySelector('meta[name=_csrf_header]');
    if (csrf && header)
        headers[header.content] = csrf.content;
    return headers;
}

function deleteGalleryImage(imageId, btn) {
    const modalEl = document.getElementById('confirmModal');
    if (!modalEl)
        return;
    const modal = bootstrap.Modal.getOrCreateInstance(modalEl);
    document.getElementById('confirmModalMessage').textContent = 'Xo\xe1 \u1ea3nh n\xe0y?';
    const confirmBtn = document.getElementById('confirmModalConfirm');
    const handler = function () {
        fetch('/admin/san-pham/xoa-anh/' + imageId, {method: 'POST', headers: getCsrfHeaders()})
                .then(r => {
                    if (r.ok)
                        btn.closest('.adm-gallery-item').remove();
                });
        modal.hide();
        confirmBtn.removeEventListener('click', handler);
    };
    confirmBtn.addEventListener('click', handler);
    modalEl.addEventListener('hidden.bs.modal', function () {
        confirmBtn.removeEventListener('click', handler);
    }, {once: true});
    modal.show();
}

(function () {
    var imageGrid = document.getElementById('imageGrid');
    if (imageGrid) {
        imageGrid.addEventListener('click', function (e) {
            const card = e.target.closest('[data-href]');
            if (!card)
                return;
            if (e.target.closest('a, button, .dropdown, .adm-image-action, .adm-kebab-btn, label, input, select, textarea'))
                return;
            window.location.href = card.getAttribute('data-href');
        });
    }

    const kpiFilter = document.querySelector('[data-filter]');
    if (kpiFilter) {
        kpiFilter.addEventListener('click', function () {
            const filter = this.getAttribute('data-filter');
            const radio = document.querySelector('input[name=imageFilter][value="' + filter + '"]');
            if (radio) {
                radio.checked = true;
                radio.dispatchEvent(new Event('change'));
            }
        });
    }

    document.querySelectorAll('input[name=imageFilter]').forEach(function (r) {
        r.addEventListener('change', function () {
            const value = this.value;
            document.querySelectorAll('[data-image-count]').forEach(function (card) {
                const count = parseInt(card.getAttribute('data-image-count'), 10);
                if (value === 'all')
                    card.style.display = '';
                else if (value === 'has')
                    card.style.display = count > 0 ? '' : 'none';
                else if (value === 'missing')
                    card.style.display = count === 0 ? '' : 'none';
            });
            document.querySelectorAll('input[name=imageFilter] + .adm-tab').forEach(function (l) {
                l.classList.remove('active');
            });
            this.nextElementSibling.classList.add('active');
        });
    });
})();

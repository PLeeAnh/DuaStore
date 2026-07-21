'use strict';
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
    document.getElementById('confirmModalMessage').textContent = 'Xoá ảnh này?';
    const confirmBtn = document.getElementById('confirmModalConfirm');
    const handler = function () {
        fetch('/admin/san-pham/xoa-anh/' + imageId, {method: 'POST', headers: getCsrfHeaders()})
                .then(r => {
                    if (r.ok)
                        btn.parentElement.remove();
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

function confirmDeleteMainImage(btn) {
    const modalEl = document.getElementById('confirmModal');
    if (!modalEl)
        return;
    const modal = bootstrap.Modal.getOrCreateInstance(modalEl);
    document.getElementById('confirmModalMessage').textContent = 'Xoá ảnh đại diện này?';
    const confirmBtn = document.getElementById('confirmModalConfirm');
    const handler = function () {
        const productId = btn.getAttribute('data-product-id');
        fetch('/admin/san-pham/chi-tiet/' + productId + '/xoa-anh-chinh', {method: 'POST', headers: getCsrfHeaders()})
                .then(r => {
                    if (r.ok) {
                        const card = btn.closest('.adm-card');
                        const img = card.querySelector('.adm-main-image');
                        const empty = card.querySelector('.adm-empty');
                        if (img)
                            img.remove();
                        if (empty)
                            empty.style.display = '';
                        btn.style.display = 'none';
                        const changeBtn = card.querySelector('label .bi-arrow-repeat');
                        if (changeBtn)
                            changeBtn.parentElement.querySelector('span').textContent = 'Thêm ảnh';
                    }
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
    const zone = document.getElementById('galleryUploadZone');
    const input = document.getElementById('galleryInput');
    const preview = document.getElementById('galleryPreview');
    const form = input.closest('form');
    let filesArray = [];

    zone.addEventListener('click', () => input.click());

    input.addEventListener('change', () => {
        if (input.files)
            addFiles(Array.from(input.files));
        input.value = '';
    });

    zone.addEventListener('dragover', (e) => {
        e.preventDefault();
        zone.classList.add('gallery-upload-zone--dragover');
    });
    zone.addEventListener('dragleave', () => {
        zone.classList.remove('gallery-upload-zone--dragover');
    });
    zone.addEventListener('drop', (e) => {
        e.preventDefault();
        zone.classList.remove('gallery-upload-zone--dragover');
        if (e.dataTransfer.files)
            addFiles(Array.from(e.dataTransfer.files));
    });

    function addFiles(newFiles) {
        for (const f of newFiles) {
            if (!f.type.startsWith('image/'))
                continue;
            filesArray.push(f);
        }
        renderPreview();
    }

    window.removeGalleryFile = function (index) {
        filesArray.splice(index, 1);
        renderPreview();
    };

    function renderPreview() {
        preview.innerHTML = '';
        filesArray.forEach((f, i) => {
            const url = URL.createObjectURL(f);
            const div = document.createElement('div');
            div.className = 'gallery-preview-item';
            div.innerHTML =
                    '<img src="' + url + '" alt="" />' +
                    '<button type="button" class="gallery-remove-btn" onclick="removeGalleryFile(' + i + ')">\u2715</button>';
            preview.appendChild(div);
        });
    }

    form.addEventListener('formdata', (e) => {
        for (const f of filesArray)
            e.formData.append('galleryFiles', f);
    });
})();

(function () {
    const input = document.getElementById('mainImageInput');
    const img = document.querySelector('.adm-main-image');
    const empty = document.querySelector('.adm-empty');
    const label = document.getElementById('mainImageLabel');
    const labelSpan = label ? label.querySelector('span') : null;
    let mainImageFile = null;

    if (!input)
        return;

    const wrap = document.querySelector('.adm-main-image-wrap');

    input.addEventListener('change', function () {
        if (this.files && this.files[0]) {
            const file = this.files[0];
            if (!file.type.startsWith('image/'))
                return;
            mainImageFile = file;

            const url = URL.createObjectURL(file);
            if (img) {
                img.src = url;
                img.style.display = '';
            } else if (wrap) {
                const newImg = document.createElement('img');
                newImg.className = 'adm-main-image';
                newImg.src = url;
                newImg.alt = '';
                wrap.insertBefore(newImg, empty);
            }
            if (empty)
                empty.style.display = 'none';

            if (labelSpan)
                labelSpan.textContent = 'Đổi ảnh';
        }
    });

    const form = input.closest('form');
    form.addEventListener('formdata', function (e) {
        if (mainImageFile)
            e.formData.set('hinhAnhFile', mainImageFile);
    });
})();

function generateDescription() {
    const fields = ['tenSanPham', 'danhMucId', 'thuongHieu', 'chatLieu', 'xuatXu', 'kinhLoai', 'mucDichSuDung', 'dungTich'];
    const params = {};
    fields.forEach(f => {
        const el = document.querySelector('[name="' + f + '"]');
        if (el) params[f] = el.value;
    });
    const ta = document.querySelector('[name="moTa"]');
    if (!ta) return;
    const btn = document.querySelector('button[onclick="generateDescription()"]');
    const status = document.getElementById('descStatus');
    btn.disabled = true;
    status.style.display = '';
    status.textContent = 'Đang tạo...';
    fetch('/admin/san-pham/api/tu-dong-mo-ta', {
        method: 'POST',
        headers: Object.assign({'Content-Type': 'application/json'}, getCsrfHeaders()),
        body: JSON.stringify(params)
    })
    .then(r => r.json())
    .then(data => {
        var txt = data.description
            .replace(/<br\s*\/?>/gi, '\n')
            .replace(/<\/(h[1-6]|p|li|ul|ol|div|blockquote|tr|td|th)>/gi, '\n')
            .replace(/<li>/gi, '\u2022 ')
            .replace(/<[^>]*>/g, '')
            .replace(/&nbsp;/g, ' ')
            .replace(/\n{3,}/g, '\n\n')
            .trim();
        ta.value = txt;
        ta.dispatchEvent(new Event('input', {bubbles: true}));
        status.textContent = '\u2705 Đã tạo mô tả tự động';
    })
    .catch(() => {
        status.textContent = '\u274c Lỗi, vui lòng thử lại';
    })
    .finally(() => {
        btn.disabled = false;
        setTimeout(() => { status.style.display = 'none'; }, 3000);
    });
}

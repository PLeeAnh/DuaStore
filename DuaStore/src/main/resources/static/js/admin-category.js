document.addEventListener('DOMContentLoaded', function () {
    var input = document.getElementById('categoryImageInput');
    var preview = document.getElementById('categoryImagePreview');
    var empty = document.getElementById('categoryImageEmpty');
    var btnText = document.getElementById('categoryImageBtnText');
    var deleteBtn = document.getElementById('categoryImageDeleteBtn');

    if (input) {
        input.addEventListener('change', function () {
            var file = this.files[0];
            if (!file)
                return;
            var reader = new FileReader();
            reader.onload = function (e) {
                if (!preview) {
                    var wrap = document.querySelector('.adm-main-image-wrap');
                    if (wrap) {
                        preview = document.createElement('img');
                        preview.id = 'categoryImagePreview';
                        preview.alt = '';
                        preview.style.cssText = 'width:120px;height:120px;object-fit:cover;border-radius:12px';
                        if (empty)
                            empty.style.display = 'none';
                        wrap.insertBefore(preview, empty);
                    }
                }
                if (preview) {
                    preview.src = e.target.result;
                    preview.style.display = 'block';
                    preview.style.opacity = '1';
                }
                if (empty)
                    empty.style.display = 'none';
                btnText.textContent = 'Thay ảnh';
                if (!deleteBtn) {
                    var parent = input.closest('.d-flex');
                    if (parent) {
                        var newBtn = document.createElement('button');
                        newBtn.type = 'button';
                        newBtn.className = 'btn btn-outline-danger btn-sm flex-fill';
                        newBtn.innerHTML = '<i class="bi bi-trash me-1"></i>Xóa ảnh';
                        newBtn.onclick = function () {
                            var modalEl = document.getElementById('confirmModal');
                            if (!modalEl)
                                return;
                            var modal = bootstrap.Modal.getOrCreateInstance(modalEl);
                            document.getElementById('confirmModalMessage').textContent = 'Xoá ảnh danh mục này?';
                            var confirmBtn = document.getElementById('confirmModalConfirm');
                            var handler = function () {
                                if (preview) {
                                    preview.src = '/images/no-image.png';
                                    preview.style.opacity = '0.4';
                                }
                                if (empty)
                                    empty.style.display = '';
                                newBtn.remove();
                                modal.hide();
                                confirmBtn.removeEventListener('click', handler);
                            };
                            confirmBtn.addEventListener('click', handler);
                            modalEl.addEventListener('hidden.bs.modal', function () {
                                confirmBtn.removeEventListener('click', handler);
                            }, {once: true});
                            modal.show();
                        };
                        parent.appendChild(newBtn);
                        deleteBtn = newBtn;
                    }
                }
            };
            reader.readAsDataURL(file);
        });
    }
});

function confirmDeleteCategoryImage(btn) {
    var modalEl = document.getElementById('confirmModal');
    if (!modalEl)
        return;
    var modal = bootstrap.Modal.getOrCreateInstance(modalEl);
    document.getElementById('confirmModalMessage').textContent = 'Xoá ảnh danh mục này?';
    var confirmBtn = document.getElementById('confirmModalConfirm');
    var handler = function () {
        var catId = btn.getAttribute('data-category-id');
        fetch('/admin/danh-muc/' + catId + '/xoa-anh', {method: 'POST'})
                .then(function (r) {
                    if (r.ok) {
                        var preview = document.getElementById('categoryImagePreview');
                        var empty = document.getElementById('categoryImageEmpty');
                        if (preview) {
                            preview.src = '/images/no-image.png';
                            preview.style.opacity = '0.4';
                        }
                        if (empty)
                            empty.style.display = '';
                        btn.remove();
                        var btnText = document.getElementById('categoryImageBtnText');
                        if (btnText)
                            btnText.textContent = 'Thêm ảnh';
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

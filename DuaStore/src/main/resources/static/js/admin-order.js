'use strict';
(function () {
    var orderId = window.orderId;
    var currentStep = window.currentStep;
    var csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content || '';
    var csrfToken = document.querySelector('meta[name="_csrf"]')?.content || '';

    function postStatus(status) {
        var headers = {};
        if (csrfHeader && csrfToken)
            headers[csrfHeader] = csrfToken;
        return fetch('/admin/don-hang/api/' + orderId + '/cap-nhat-trang-thai?trangThai=' + status,
                {method: 'POST', headers: headers}).then(function (r) {
            return r.json();
        });
    }

    function showErrorToast(msg) {
        var el = document.getElementById('errorToast');
        if (!el) {
            el = document.createElement('div');
            el.id = 'errorToast';
            el.className = 'alert alert-danger alert-dismissible fade show position-fixed top-0 start-50 translate-middle-x mt-5';
            el.style.zIndex = '9999';
            document.body.appendChild(el);
        }
        el.innerHTML = '<i class="bi bi-exclamation-triangle me-2"></i>' + msg +
                '<button type="button" class="btn-close ms-3" data-bs-dismiss="alert"></button>';
        el.classList.remove('d-none');
        setTimeout(function () {
            el.classList.add('d-none');
        }, 6000);
    }

    document.querySelectorAll('.timeline-step.clickable').forEach(function (el) {
        el.addEventListener('click', function () {
            var status = this.dataset.status;
            var stepIndex = parseInt(this.dataset.stepindex);
            if (!status)
                return;
            if (stepIndex === currentStep)
                return;
            var node = this;
            node.style.opacity = '0.5';
            postStatus(status).then(function (data) {
                if (data.success) {
                    if (window.__dsToastAfterReload) window.__dsToastAfterReload('Đã cập nhật trạng thái đơn hàng');
                    location.reload();
                } else {
                    if (data.message)
                        showErrorToast(data.message);
                    node.style.opacity = '1';
                }
            }).catch(function () {
                node.style.opacity = '1';
            });
        });
    });

    window.editMaVanDon = function (orderId) {
        document.getElementById('maVanDonText-' + orderId).classList.add('d-none');
        document.getElementById('maVanDonBtn-' + orderId).classList.add('d-none');
        document.getElementById('maVanDonInput-' + orderId).classList.remove('d-none');
        document.getElementById('maVanDonSave-' + orderId).classList.remove('d-none');
        document.getElementById('maVanDonCancel-' + orderId).classList.remove('d-none');
        document.getElementById('maVanDonInput-' + orderId).focus();
    };
    window.saveMaVanDon = function (orderId) {
        var input = document.getElementById('maVanDonInput-' + orderId);
        var val = input.value.trim();
        if (!val) {
            showErrorToast('Mã vận đơn không được để trống');
            return;
        }
        var headers = {};
        var csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content || '';
        var csrfToken = document.querySelector('meta[name="_csrf"]')?.content || '';
        if (csrfHeader && csrfToken)
            headers[csrfHeader] = csrfToken;
        fetch('/admin/don-hang/api/' + orderId + '/cap-nhat-ma-van-don?maVanDon=' + encodeURIComponent(val),
                {method: 'POST', headers: headers})
                .then(function (r) {
                    return r.json();
                })
                .then(function (data) {
                    if (data.success) {
                        if (window.__dsToastAfterReload) window.__dsToastAfterReload('Đã lưu mã vận đơn');
                        location.reload();
                    } else {
                        showErrorToast(data.message || 'Lỗi');
                    }
                });
    };
    window.cancelMaVanDon = function (orderId) {
        document.getElementById('maVanDonText-' + orderId).classList.remove('d-none');
        document.getElementById('maVanDonBtn-' + orderId).classList.remove('d-none');
        document.getElementById('maVanDonInput-' + orderId).classList.add('d-none');
        document.getElementById('maVanDonSave-' + orderId).classList.add('d-none');
        document.getElementById('maVanDonCancel-' + orderId).classList.add('d-none');
    };

    window.openProductQuickView = function (productId) {
        if (!productId)
            return;
        var body = document.getElementById('qvBody');
        body.innerHTML = '<div class="text-center py-4"><div class="spinner-border text-primary" role="status"></div></div>';
        var modal = new bootstrap.Modal(document.getElementById('productQuickViewModal'));
        modal.show();
        fetch('/admin/don-hang/api/san-pham/' + productId + '/quick-view')
                .then(function (r) {
                    return r.json();
                })
                .then(function (data) {
                    if (!data.success) {
                        body.innerHTML = '<div class="alert alert-danger">' + (data.message || 'Lỗi tải dữ liệu') + '</div>';
                        return;
                    }
                    var p = data.product;
                    var html = '<div class="row g-4">';
                    html += '<div class="col-md-5 text-center">';
                    html += p.hinhAnh ? '<img src="' + p.hinhAnh + '" class="img-fluid rounded" style="max-height:300px;object-fit:contain">' : '<i class="bi bi-image text-muted" style="font-size:5rem;"></i>';
                    html += '</div><div class="col-md-7">';
                    html += '<h5 class="fw-bold">' + (p.tenSanPham || '') + '</h5>';
                    html += '<p class="text-muted small mt-2">' + (p.moTa || '') + '</p>';
                    html += '<hr>';
                    if (data.variants && data.variants.length > 0) {
                        html += '<h6 class="fw-semibold">Biến thể</h6>';
                        html += '<div class="table-responsive"><table class="table table-sm table-bordered"><thead><tr><th>Tên</th><th class="text-end">Giá gốc</th><th class="text-end">Giá KM</th><th class="text-center">Tồn kho</th></tr></thead><tbody>';
                        data.variants.forEach(function (v) {
                            html += '<tr>';
                            html += '<td>' + (v.tenBienThe || 'Mặc định') + '</td>';
                            html += '<td class="text-end">' + (v.giaGoc ? Number(v.giaGoc).toLocaleString('vi-VN') + '&#x20AB;' : '-') + '</td>';
                            html += '<td class="text-end">' + (v.giaKhuyenMai ? Number(v.giaKhuyenMai).toLocaleString('vi-VN') + '&#x20AB;' : '-') + '</td>';
                            html += '<td class="text-center">' + (v.soLuongTon != null ? v.soLuongTon : '?') + '</td>';
                            html += '</tr>';
                        });
                        html += '</tbody></table></div>';
                    }
                    html += '</div></div>';
                    body.innerHTML = html;
                })
                .catch(function () {
                    body.innerHTML = '<div class="alert alert-danger">Lỗi kết nối</div>';
                });
    };
})();

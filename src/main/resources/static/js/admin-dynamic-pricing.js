(function () {
    'use strict';

    if (window.__dynamicPricingInited) return;
    window.__dynamicPricingInited = true;

    var CSRF = {
        token: document.querySelector('meta[name="_csrf"]')?.getAttribute('content'),
        header: document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content')
    };

    function csrfFetch(url, opts) {
        opts = opts || {};
        opts.headers = opts.headers || {};
        if (CSRF.token && CSRF.header) {
            opts.headers[CSRF.header] = CSRF.token;
        }
        return fetch(url, opts);
    }

    // ── Variant form: load suggestion below price inputs ──
    function initVariantPricing(variantId) {
        if (!variantId) return;
        var container = document.getElementById('pricingSuggestion');
        if (!container) return;

        container.innerHTML = '<div class="small text-muted mt-1"><i class="bi bi-hourglass-split me-1"></i>AI đang phân tích...</div>';

        csrfFetch('/admin/api/dinh-gia-dong/variant/' + encodeURIComponent(variantId))
            .then(function (r) {
                if (!r.ok) throw new Error('HTTP ' + r.status);
                return r.json();
            })
            .then(function (data) {
                renderVariantSuggestion(data, container);
            })
            .catch(function () {
                container.innerHTML = '';
            });
    }

    function renderVariantSuggestion(data, container) {
        if (!data || !data.actionable) {
            container.innerHTML = '<div class="small text-success mt-1"><i class="bi bi-check-circle me-1"></i>Giá hiện tại đã hợp lý</div>';
            return;
        }

        var action = data.suggestedAction;
        var icon = action === 'INCREASE_PRICE' ? 'bi-arrow-up-circle text-danger' :
                   action === 'OFFER_DISCOUNT' ? 'bi-tag text-warning' : 'bi-check-circle text-success';
        var badge = action === 'INCREASE_PRICE' ? 'bg-danger' :
                    action === 'OFFER_DISCOUNT' ? 'bg-warning text-dark' : 'bg-success';

        var html = '<div class="alert alert-' + (action === 'INCREASE_PRICE' ? 'danger' : action === 'OFFER_DISCOUNT' ? 'warning' : 'success') + ' py-2 px-3 mt-1 mb-0" style="font-size:.82rem">';
        html += '<div class="d-flex align-items-start gap-2">';
        html += '<i class="bi ' + icon + ' flex-shrink-0 mt-1"></i>';
        html += '<div class="flex-grow-1">';
        html += '<span class="badge ' + badge + ' me-2" style="font-size:.7rem">AI</span>';

        if (action === 'INCREASE_PRICE') {
            html += '<strong>Tăng giá gốc</strong> lên <strong class="text-danger">' + formatPrice(data.suggestedGiaGoc) + '</strong>';
            html += '<br/><span class="text-muted">' + escapeHtml(data.reason) + '</span>';
            html += '<div class="mt-1"><button type="button" class="btn btn-sm btn-outline-danger btn-pricing-apply" data-field="giaGoc" data-value="' + data.suggestedGiaGoc + '"><i class="bi bi-check-lg me-1"></i>Áp dụng</button></div>';
        } else if (action === 'OFFER_DISCOUNT') {
            html += '<strong>Giảm giá</strong> còn <strong class="text-warning">' + formatPrice(data.suggestedGiaKhuyenMai) + '</strong>';
            if (data.suggestedDiscountPct) {
                html += ' <span class="badge bg-danger" style="font-size:.7rem">-' + data.suggestedDiscountPct + '%</span>';
            }
            html += '<br/><span class="text-muted">' + escapeHtml(data.reason) + '</span>';
            html += '<div class="mt-1"><button type="button" class="btn btn-sm btn-outline-warning btn-pricing-apply" data-field="giaKhuyenMai" data-value="' + data.suggestedGiaKhuyenMai + '"><i class="bi bi-check-lg me-1"></i>Áp dụng</button></div>';
        }
        html += '<div class="small text-muted mt-1">Độ tin cậy: <span class="fw-semibold">' + data.confidence + '</span>';
        if (data.daysUntilEmpty < 999) {
            html += ' · Tồn: ' + data.currentStock + ' (' + data.daysUntilEmpty + ' ngày)';
        }
        html += ' · Bán: ' + data.salesPerDay + '/ngày</div>';
        html += '</div></div></div>';

        container.innerHTML = html;

        container.querySelectorAll('.btn-pricing-apply').forEach(function (btn) {
            btn.addEventListener('click', function () {
                var field = this.dataset.field;
                var value = this.dataset.value;
                var input = document.getElementById(field === 'giaGoc' ? 'giaGoc' : 'giaKhuyenMai');
                if (input) {
                    input.value = value;
                    input.dispatchEvent(new Event('input', { bubbles: true }));
                }
                this.textContent = 'Đã áp dụng';
                this.classList.remove('btn-outline-danger', 'btn-outline-warning');
                this.classList.add('btn-success');
                this.disabled = true;
            });
        });
    }

    // ── Product detail: load suggestions panel ──
    function initProductPricing(productId) {
        if (!productId) return;
        var container = document.getElementById('pricingSuggestionPanel');
        if (!container) return;

        container.innerHTML = '<div class="text-center py-3"><div class="spinner-border spinner-border-sm text-primary me-2"></div>AI đang phân tích...</div>';

        csrfFetch('/admin/api/dinh-gia-dong/product/' + encodeURIComponent(productId))
            .then(function (r) {
                if (!r.ok) throw new Error('HTTP ' + r.status);
                return r.json();
            })
            .then(function (list) {
                renderProductSuggestions(list, container);
            })
            .catch(function () {
                container.innerHTML = '<div class="text-muted small py-2">Không thể tải đề xuất định giá.</div>';
            });
    }

    function renderProductSuggestions(list, container) {
        if (!list || list.length === 0) {
            container.innerHTML = '<div class="text-muted small py-2">Chưa có đề xuất nào.</div>';
            return;
        }

        var html = '<div class="table-responsive"><table class="table table-sm mb-0" style="font-size:.82rem">';
        html += '<thead><tr><th>Biến thể</th><th>Giá gốc</th><th>Giá KM</th><th>Tồn</th><th>AI đề xuất</th><th></th></tr></thead><tbody>';

        var hasActionable = false;
        list.forEach(function (s) {
            if (s.actionable) hasActionable = true;
            var badge = s.suggestedAction === 'INCREASE_PRICE' ? 'bg-danger' :
                        s.suggestedAction === 'OFFER_DISCOUNT' ? 'bg-warning text-dark' :
                        s.suggestedAction === 'NO_CHANGE' ? 'bg-success' : 'bg-secondary';
            var label = s.suggestedAction === 'INCREASE_PRICE' ? 'Tăng giá' :
                        s.suggestedAction === 'OFFER_DISCOUNT' ? 'Giảm giá' : 'Giữ nguyên';

            html += '<tr>';
            html += '<td class="fw-semibold">' + escapeHtml(s.variantName) + '</td>';
            html += '<td>' + formatPrice(s.currentGiaGoc) + '</td>';
            html += '<td>' + (s.currentGiaKhuyenMai ? formatPrice(s.currentGiaKhuyenMai) : '<span class="text-muted">—</span>') + '</td>';
            html += '<td>' + s.currentStock + '</td>';
            html += '<td>';
            html += '<span class="badge ' + badge + ' me-1" style="font-size:.7rem">' + label + '</span>';
            if (s.suggestedAction !== 'NO_CHANGE') {
                if (s.suggestedGiaGoc && s.suggestedGiaGoc !== s.currentGiaGoc) {
                    html += '<span class="text-danger fw-semibold">' + formatPrice(s.suggestedGiaGoc) + '</span>';
                }
                if (s.suggestedGiaKhuyenMai && s.suggestedGiaKhuyenMai !== s.currentGiaKhuyenMai) {
                    if (s.suggestedGiaGoc && s.suggestedGiaGoc !== s.currentGiaGoc) html += ' / ';
                    html += '<span class="text-warning fw-semibold">' + formatPrice(s.suggestedGiaKhuyenMai) + '</span>';
                }
                if (s.suggestedDiscountPct != null && s.suggestedDiscountPct > 0) {
                    html += ' <span class="badge bg-danger" style="font-size:.65rem">-' + s.suggestedDiscountPct + '%</span>';
                }
                html += '<div class="text-muted" style="font-size:.75rem">' + escapeHtml(s.reason) + '</div>';
            } else {
                html += '<span class="text-muted">' + escapeHtml(s.reason) + '</span>';
            }
            html += '</td>';
            html += '<td>';
            if (s.actionable) {
                html += '<button type="button" class="btn btn-sm btn-outline-primary btn-apply-suggestion" data-variant-id="' + s.variantId + '" data-field="' + (s.suggestedAction === 'INCREASE_PRICE' ? 'giaGoc' : 'giaKhuyenMai') + '" data-value="' + (s.suggestedAction === 'INCREASE_PRICE' ? s.suggestedGiaGoc : s.suggestedGiaKhuyenMai) + '">Áp dụng</button>';
            }
            html += '</td>';
            html += '</tr>';
        });

        html += '</tbody></table></div>';

        if (!hasActionable) {
            html = '<div class="text-success small py-2"><i class="bi bi-check-circle me-1"></i>Tất cả biến thể đã có giá hợp lý.</div>' + html;
        }

        container.innerHTML = html;

        container.querySelectorAll('.btn-apply-suggestion').forEach(function (btn) {
            btn.addEventListener('click', function () {
                var variantId = this.dataset.variantId;
                var field = this.dataset.field;
                var value = this.dataset.value;
                applySuggestion(variantId, field, value, this);
            });
        });
    }

    function applySuggestion(variantId, field, value, btn) {
        btn.disabled = true;
        btn.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span>Đang lưu...';

        csrfFetch('/admin/api/dinh-gia-dong/ap-dung', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ variantId: variantId, field: field, value: value })
        })
        .then(function (r) { return r.json(); })
        .then(function (data) {
            if (data.success) {
                btn.textContent = 'Đã áp dụng';
                btn.className = 'btn btn-sm btn-success';
                var parentRow = btn.closest('tr');
                if (parentRow) {
                    parentRow.style.opacity = '0.6';
                }
                if (window.DuaStore && DuaStore.toast) {
                    DuaStore.toast.success('Đã áp dụng đề xuất định giá.');
                }
            } else {
                btn.disabled = false;
                btn.textContent = 'Áp dụng';
                if (window.DuaStore && DuaStore.toast) {
                    DuaStore.toast.error(data.error || 'Lỗi áp dụng.');
                }
            }
        })
        .catch(function () {
            btn.disabled = false;
            btn.textContent = 'Áp dụng';
            if (window.DuaStore && DuaStore.toast) {
                DuaStore.toast.error('Lỗi kết nối.');
            }
        });
    }

    function formatPrice(v) {
        if (v == null) return '0₫';
        var num = typeof v === 'string' ? parseFloat(v) : v;
        return num.toLocaleString('vi-VN') + '₫';
    }

    function escapeHtml(str) {
        if (!str) return '';
        var div = document.createElement('div');
        div.textContent = str;
        return div.innerHTML;
    }

    // ── Init functions for pages ──
    window.initDynamicPricingVariant = function (variantId) {
        if (document.readyState === 'loading') {
            document.addEventListener('DOMContentLoaded', function () { initVariantPricing(variantId); });
        } else {
            initVariantPricing(variantId);
        }
    };

    window.initDynamicPricingProduct = function (productId) {
        if (document.readyState === 'loading') {
            document.addEventListener('DOMContentLoaded', function () { initProductPricing(productId); });
        } else {
            initProductPricing(productId);
        }
    };
})();

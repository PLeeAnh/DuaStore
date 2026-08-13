/* =====================================================
 DuaStore — API Layer
 Centralized fetch with CSRF, error handling, normalization
 ===================================================== */
'use strict';

window.DuaStore = window.DuaStore || {};
window.DuaStore.api = window.DuaStore.api || {};

(function () {
    var csrfToken = null;
    var csrfHeader = null;

    function initCsrf() {
        if (csrfToken !== null)
            return;
        var tokenMeta = document.querySelector('meta[name="_csrf"]');
        var headerMeta = document.querySelector('meta[name="_csrf_header"]');
        csrfToken = tokenMeta ? tokenMeta.getAttribute('content') : '';
        csrfHeader = headerMeta ? headerMeta.getAttribute('content') : '';
    }

    function getCsrfHeaders() {
        initCsrf();
        if (!csrfToken || !csrfHeader)
            return {};
        var h = {};
        h[csrfHeader] = csrfToken;
        return h;
    }

    var statusMessages = {
        401: 'Vui lòng đăng nhập',
        403: 'Bạn không có quyền thực hiện thao tác này',
        404: 'Không tìm thấy dữ liệu',
        500: 'Lỗi hệ thống'
    };

    function handleResponse(res) {
        var message = statusMessages[res.status] || 'Yêu cầu thất bại';
        if (res.status === 401 || res.status === 403) {
            if (typeof showLoginPopup === 'function')
                showLoginPopup();
        }
        if (res.ok) {
            return res.json().then(function (data) {
                return {ok: true, data: data};
            }).catch(function () {
                return {ok: true, data: null};
            });
        }
        return {ok: false, message: message};
    }

    function networkError() {
        return {ok: false, message: 'Không thể kết nối máy chủ'};
    }

    function request(method, url, data) {
        var opts = {
            method: method,
            headers: {'Content-Type': 'application/json'}
        };

        if (method !== 'GET') {
            var csrf = getCsrfHeaders();
            for (var k in csrf) {
                if (csrf.hasOwnProperty(k))
                    opts.headers[k] = csrf[k];
            }
        }

        if (data !== undefined && data !== null) {
            opts.body = JSON.stringify(data);
        }

        return fetch(url, opts).then(handleResponse).catch(networkError);
    }

    window.DuaStore.api.getCsrfHeaders = function () {
        initCsrf();
        var h = {};
        if (csrfToken && csrfHeader) {
            h[csrfHeader] = csrfToken;
        }
        return h;
    };

    window.DuaStore.api.get = function (url) {
        return request('GET', url);
    };
    window.DuaStore.api.post = function (url, data) {
        return request('POST', url, data);
    };
    window.DuaStore.api.postForm = function (url, formData) {
        var opts = {
            method: 'POST',
            headers: {}
        };
        var csrf = getCsrfHeaders();
        for (var k in csrf) {
            if (csrf.hasOwnProperty(k))
                opts.headers[k] = csrf[k];
        }
        if (formData !== undefined && formData !== null) {
            opts.body = formData;
        }
        return fetch(url, opts).then(handleResponse).catch(networkError);
    };
    window.DuaStore.api.put = function (url, data) {
        return request('PUT', url, data);
    };
    window.DuaStore.api.delete = function (url) {
        return request('DELETE', url);
    };
})();

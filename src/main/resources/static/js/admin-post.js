'use strict';
if (typeof tinymce !== 'undefined') {
    var existing = tinymce.get('noiDung');
    if (existing) existing.destroy();
    var csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
    var csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
    tinymce.init({
        selector: '#noiDung',
        height: 500,
        menubar: true,
        plugins: 'advlist autolink lists link image charmap preview anchor searchreplace visualblocks code fullscreen insertdatetime media table code help wordcount',
        toolbar: 'undo redo | formatselect | bold italic underline strikethrough | forecolor backcolor | alignleft aligncenter alignright alignjustify | bullist numlist outdent indent | removeformat | image link media | table | code fullscreen',
        images_upload_url: '/admin/bai-viet/upload-hinh',
        images_upload_handler: function (blobInfo, success, failure) {
            var formData = new FormData();
            formData.append('file', blobInfo.blob(), blobInfo.filename());
            var headers = { 'X-Requested-With': 'XMLHttpRequest' };
            if (csrfToken && csrfHeader) headers[csrfHeader] = csrfToken;
            fetch('/admin/bai-viet/upload-hinh', {
                method: 'POST',
                body: formData,
                headers: headers
            }).then(function (r) { return r.json(); })
              .then(function (json) {
                  if (json.location) success(json.location);
                  else failure(json.error || 'Upload thất bại');
              }).catch(function () { failure('Lỗi upload'); });
        },
        setup: function (editor) {
            editor.on('change blur', function () {
                editor.save();
            });
        }
    });
}
document.addEventListener('click', function (e) {
    if (e.target && e.target.id === 'dsSaveBarSave') {
        if (typeof tinymce !== 'undefined') tinymce.triggerSave();
    }
}, true);

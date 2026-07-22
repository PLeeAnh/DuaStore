function toggleBankFields() {
    var method = document.getElementById('refundMethod').value;
    var bankFields = document.getElementById('bankFields');
    bankFields.style.display = method === 'CHUYEN_KHOAN' ? 'block' : 'none';
    bankFields.querySelectorAll('input').forEach(function(inp) { inp.required = method === 'CHUYEN_KHOAN'; });
}
document.addEventListener('DOMContentLoaded', toggleBankFields);

function previewRefundImage(input) {
    var preview = document.getElementById('refundImagePreview');
    var img = preview.querySelector('img');
    if (input.files && input.files[0]) {
        var reader = new FileReader();
        reader.onload = function(e) { img.src = e.target.result; preview.classList.remove('d-none'); };
        reader.readAsDataURL(input.files[0]);
    }
}
function clearRefundImage() {
    document.getElementById('refundImageInput').value = '';
    document.getElementById('refundImagePreview').classList.add('d-none');
}

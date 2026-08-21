function toggleBankFields() {
    var method = document.getElementById('refundMethod').value;
    var bankFields = document.getElementById('bankFields');
    bankFields.style.display = method === 'CHUYEN_KHOAN' ? 'block' : 'none';
    bankFields.querySelectorAll('input').forEach(function(inp) { inp.required = method === 'CHUYEN_KHOAN'; });
}

function toggleVideoField() {
    var reason = document.getElementById('refundReason').value;
    var videoField = document.getElementById('videoUnboxingField');
    var requiresVideo = ['LOI_HANG', 'KHONG_DUNG_MO_TA'].includes(reason);
    videoField.style.display = requiresVideo ? 'block' : 'none';
    var videoInput = document.getElementById('videoUnboxingInput');
    videoInput.required = requiresVideo;
}

function onRefundTypeChange() {
    var type = document.getElementById('refundType').value;
    var exchangeFields = document.getElementById('exchangeFields');
    var amountInput = document.querySelector('input[name="soTienHoan"]');
    exchangeFields.style.display = type !== 'HOAN_TIEN' ? 'block' : 'none';
    if (type !== 'HOAN_TIEN') {
        amountInput.value = 0;
        amountInput.readOnly = true;
    } else {
        amountInput.readOnly = false;
    }
}

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

document.addEventListener('DOMContentLoaded', function() {
    toggleBankFields();
    toggleVideoField();
    onRefundTypeChange();

    document.getElementById('refundMethod').addEventListener('change', toggleBankFields);
    document.getElementById('refundReason').addEventListener('change', toggleVideoField);
    document.getElementById('refundType').addEventListener('change', onRefundTypeChange);
});
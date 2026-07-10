'use strict';

document.addEventListener('DOMContentLoaded', function () {
    document.querySelectorAll('[data-toggle-detail]').forEach(function(btn) {
        btn.addEventListener('click', function() {
            var target = document.getElementById(btn.getAttribute('data-toggle-detail'));
            if (target) target.classList.toggle('d-none');
        });
    });

    document.querySelectorAll('.star-rating-input').forEach(function(container) {
        var stars = container.querySelectorAll('i');
        var input = container.querySelector('input[type="hidden"]');
        stars.forEach(function(star) {
            star.addEventListener('click', function() {
                var val = this.getAttribute('data-value');
                if (input) input.value = val;
                stars.forEach(function(s) {
                    s.className = parseInt(s.getAttribute('data-value')) <= parseInt(val)
                        ? 'bi bi-star-fill text-warning'
                        : 'bi bi-star text-muted';
                });
            });
        });
    });

    document.querySelectorAll('.img-preview-input').forEach(function(input) {
        input.addEventListener('change', function() {
            var preview = document.getElementById(input.getAttribute('data-preview'));
            if (preview && input.files && input.files[0]) {
                var reader = new FileReader();
                reader.onload = function(e) { preview.src = e.target.result; };
                reader.readAsDataURL(input.files[0]);
            }
        });
    });
});

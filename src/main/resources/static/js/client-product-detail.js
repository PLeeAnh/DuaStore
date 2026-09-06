if (typeof window.showLoginPopup !== 'function') {
    window.showLoginPopup = function() {
        var el = document.getElementById('loginModal');
        if (el && typeof bootstrap !== 'undefined' && bootstrap.Modal) {
            try { var modal = bootstrap.Modal.getOrCreateInstance(el); modal.show(); return; } catch(e) {}
        }
        window.location.href = '/dang-nhap';
    };
}

document.querySelectorAll('.star-rating-readonly[data-score]').forEach(function (el) {
    var score = parseInt(el.dataset.score) || 0;
    var html = '';
    for (var i = 5; i >= 1; i--) { html += i <= score ? '★' : '☆'; }
    el.textContent = html;
});

var reviewImageGroup = [];
var reviewImageIndex = 0;

function openReviewImage(el) {
    var container = el.parentElement.parentElement;
    var imgs = container.querySelectorAll('img');
    reviewImageGroup = [];
    imgs.forEach(function(img) { reviewImageGroup.push(img.src); });
    reviewImageIndex = Array.from(imgs).indexOf(el);
    showReviewImage();
}

function showReviewImage() {
    var modal = new bootstrap.Modal(document.getElementById('reviewImageModal'));
    document.getElementById('reviewImageLightbox').src = reviewImageGroup[reviewImageIndex];
    document.getElementById('prevImgBtn').style.display = reviewImageGroup.length > 1 ? '' : 'none';
    document.getElementById('nextImgBtn').style.display = reviewImageGroup.length > 1 ? '' : 'none';
    modal.show();
}

function changeReviewImage(dir) {
    reviewImageIndex = (reviewImageIndex + dir + reviewImageGroup.length) % reviewImageGroup.length;
    document.getElementById('reviewImageLightbox').src = reviewImageGroup[reviewImageIndex];
}

function previewReviewImages(input) {
    var container = document.getElementById('reviewImagePreview');
    container.innerHTML = '';
    if (!input.files || !input.files.length) return;
    Array.from(input.files).forEach(function(file) {
        var reader = new FileReader();
        reader.onload = function(e) {
            var wrap = document.createElement('div');
            wrap.className = 'position-relative d-inline-block';
            var img = document.createElement('img');
            img.src = e.target.result;
            img.className = 'rounded border';
            img.style.width = '80px'; img.style.height = '80px'; img.style.objectFit = 'cover';
            wrap.appendChild(img);
            container.appendChild(wrap);
        };
        reader.readAsDataURL(file);
    });
}

(function() {
    var stars = document.querySelectorAll('#starRatingInput .star');
    var input = document.getElementById('danhGiaInput');
    var form = document.getElementById('reviewForm');
    if (!stars.length || !input || !form) return;

    var selected = 0;
    stars.forEach(function(s) {
        s.addEventListener('click', function() {
            selected = parseInt(this.dataset.value);
            input.value = selected;
            stars.forEach(function(x) {
                x.textContent = parseInt(x.dataset.value) <= selected ? '★' : '☆';
                x.classList.toggle('active', parseInt(x.dataset.value) <= selected);
            });
        });
        s.addEventListener('mouseenter', function() {
            var val = parseInt(this.dataset.value);
            stars.forEach(function(x) { x.textContent = parseInt(x.dataset.value) <= val ? '★' : '☆'; });
        });
        s.addEventListener('mouseleave', function() {
            stars.forEach(function(x) { x.textContent = parseInt(x.dataset.value) <= selected ? '★' : '☆'; });
        });
    });

    form.addEventListener('submit', function(e) {
        e.preventDefault();
        if (!selected) {
            if (typeof DuaStore !== 'undefined' && DuaStore.toast) { DuaStore.toast.error('Vui lòng chọn số sao'); }
            return;
        }
        var fd = new FormData(form);
        var btn = form.querySelector('button[type="submit"]');
        btn.disabled = true;
        btn.textContent = 'Đang gửi...';
        fetch(form.action, { method: 'POST', body: fd, headers: { 'X-Requested-With': 'XMLHttpRequest' } })
            .then(function(r) { return r.json(); })
            .then(function(res) {
                if (res.success) {
                    if (typeof DuaStore !== 'undefined' && DuaStore.toast) { DuaStore.toast.success(res.message || 'Gửi đánh giá thành công!'); }
                    form.reset();
                    selected = 0;
                    stars.forEach(function(x) { x.textContent = '☆'; x.classList.remove('active'); });
                    input.value = '';
                    form.style.display = 'none';
                    var msg = document.createElement('div');
                    msg.className = 'alert alert-info mb-4';
                    msg.textContent = 'Bạn đã đánh giá sản phẩm này.';
                    form.parentNode.insertBefore(msg, form.nextSibling);
                } else {
                    if (typeof DuaStore !== 'undefined' && DuaStore.toast) { DuaStore.toast.error(res.message || 'Gửi đánh giá thất bại.'); }
                }
            })
            .catch(function() {
                if (typeof DuaStore !== 'undefined' && DuaStore.toast) { DuaStore.toast.error('Lỗi kết nối.'); }
            })
            .finally(function() {
                btn.disabled = false;
                btn.textContent = 'Gửi đánh giá';
            });
    });
})();

var lightboxImages = [];
var lightboxIndex = 0;
var lightboxEl = null;
var lightboxStartX = 0;
var lightboxStartY = 0;
var lightboxSwiping = false;

document.querySelectorAll('.gallery-thumb').forEach(function(t) { lightboxImages.push(t.dataset.img); });
if (document.getElementById('mainImage')) {
    var mainSrc = document.getElementById('mainImage').src;
    if (mainSrc && lightboxImages.indexOf(mainSrc) === -1) { lightboxImages.unshift(mainSrc); }
}

function openLightbox(idx) {
    lightboxIndex = idx;
    var overlay = document.createElement('div');
    overlay.id = 'dsLightbox';
    overlay.style.cssText = 'position:fixed;top:0;left:0;width:100%;height:100%;background:rgba(0,0,0,.92);z-index:99998;display:flex;align-items:center;justify-content:center;cursor:pointer';
    overlay.onclick = function(e) { if (e.target === overlay) closeLightbox(); };

    var img = document.createElement('img');
    img.id = 'dsLightboxImg';
    img.style.cssText = 'max-width:90vw;max-height:88vh;border-radius:6px;box-shadow:0 4px 40px rgba(0,0,0,.4);cursor:default;user-select:none;-webkit-user-select:none';
    img.ondragstart = function() { return false; };
    img.src = lightboxImages[lightboxIndex] || '';

    var closeBtn = document.createElement('button');
    closeBtn.innerHTML = '<i class="bi bi-x-lg"></i>';
    closeBtn.style.cssText = 'position:fixed;top:16px;right:20px;background:rgba(255,255,255,.15);border:none;color:#fff;font-size:1.4rem;width:44px;height:44px;border-radius:50%;cursor:pointer;z-index:99999;display:flex;align-items:center;justify-content:center;transition:background .2s';
    closeBtn.onmouseenter = function() { this.style.background = 'rgba(255,255,255,.25)'; };
    closeBtn.onmouseleave = function() { this.style.background = 'rgba(255,255,255,.15)'; };
    closeBtn.onclick = closeLightbox;

    var prevBtn = document.createElement('button');
    prevBtn.innerHTML = '<i class="bi bi-chevron-left"></i>';
    prevBtn.style.cssText = 'position:fixed;left:16px;top:50%;transform:translateY(-50%);background:rgba(255,255,255,.1);border:none;color:#fff;font-size:1.8rem;width:48px;height:48px;border-radius:50%;cursor:pointer;z-index:99999;display:' + (lightboxImages.length > 1 ? 'flex' : 'none') + ';align-items:center;justify-content:center;transition:background .2s';
    prevBtn.onmouseenter = function() { this.style.background = 'rgba(255,255,255,.2)'; };
    prevBtn.onmouseleave = function() { this.style.background = 'rgba(255,255,255,.1)'; };
    prevBtn.onclick = function(e) { e.stopPropagation(); navigateLightbox(-1); };

    var nextBtn = document.createElement('button');
    nextBtn.innerHTML = '<i class="bi bi-chevron-right"></i>';
    nextBtn.style.cssText = 'position:fixed;right:16px;top:50%;transform:translateY(-50%);background:rgba(255,255,255,.1);border:none;color:#fff;font-size:1.8rem;width:48px;height:48px;border-radius:50%;cursor:pointer;z-index:99999;display:' + (lightboxImages.length > 1 ? 'flex' : 'none') + ';align-items:center;justify-content:center;transition:background .2s';
    nextBtn.onmouseenter = function() { this.style.background = 'rgba(255,255,255,.2)'; };
    nextBtn.onmouseleave = function() { this.style.background = 'rgba(255,255,255,.1)'; };
    nextBtn.onclick = function(e) { e.stopPropagation(); navigateLightbox(1); };

    var counter = document.createElement('div');
    counter.id = 'dsLightboxCounter';
    counter.style.cssText = 'position:fixed;bottom:20px;left:50%;transform:translateX(-50%);color:rgba(255,255,255,.7);font-size:.85rem;z-index:99999';
    updateLightboxCounter();

    overlay.appendChild(closeBtn);
    overlay.appendChild(prevBtn);
    overlay.appendChild(nextBtn);
    overlay.appendChild(counter);
    overlay.appendChild(img);
    document.body.appendChild(overlay);
    lightboxEl = overlay;
    document.body.style.overflow = 'hidden';

    document.addEventListener('keydown', lightboxKeydown);
    overlay.addEventListener('touchstart', lightboxTouchStart, {passive: true});
    overlay.addEventListener('touchmove', lightboxTouchMove, {passive: true});
    overlay.addEventListener('touchend', lightboxTouchEnd, {passive: true});
    overlay.addEventListener('mousedown', lightboxMouseDown);
    overlay.addEventListener('mousemove', lightboxMouseMove);
    overlay.addEventListener('mouseup', lightboxMouseUp);
    overlay.addEventListener('mouseleave', lightboxMouseUp);
}

function closeLightbox() {
    if (lightboxEl) { document.body.removeChild(lightboxEl); lightboxEl = null; }
    document.removeEventListener('keydown', lightboxKeydown);
    document.body.style.overflow = '';
}

function navigateLightbox(dir) {
    lightboxIndex = (lightboxIndex + dir + lightboxImages.length) % lightboxImages.length;
    var img = document.getElementById('dsLightboxImg');
    if (img) img.src = lightboxImages[lightboxIndex];
    updateLightboxCounter();
}

function updateLightboxCounter() {
    var el = document.getElementById('dsLightboxCounter');
    if (el) el.textContent = (lightboxIndex + 1) + ' / ' + lightboxImages.length;
}

function lightboxKeydown(e) {
    if (e.key === 'Escape') closeLightbox();
    if (e.key === 'ArrowLeft') navigateLightbox(-1);
    if (e.key === 'ArrowRight') navigateLightbox(1);
}

function lightboxTouchStart(e) {
    lightboxStartX = e.touches[0].clientX;
    lightboxStartY = e.touches[0].clientY;
    lightboxSwiping = false;
}

function lightboxTouchMove(e) {
    if (!lightboxStartX) return;
    var dx = e.touches[0].clientX - lightboxStartX;
    var dy = e.touches[0].clientY - lightboxStartY;
    if (Math.abs(dx) > Math.abs(dy) && Math.abs(dx) > 30) { lightboxSwiping = true; }
}

function lightboxTouchEnd(e) {
    if (!lightboxSwiping) return;
    var dx = e.changedTouches[0].clientX - lightboxStartX;
    if (dx > 50) navigateLightbox(-1);
    if (dx < -50) navigateLightbox(1);
    lightboxStartX = 0; lightboxStartY = 0; lightboxSwiping = false;
}

function lightboxMouseDown(e) {
    if (e.target !== document.getElementById('dsLightboxImg')) return;
    lightboxStartX = e.clientX;
    lightboxStartY = e.clientY;
    lightboxSwiping = false;
}

function lightboxMouseMove(e) {
    if (!lightboxStartX) return;
    var dx = e.clientX - lightboxStartX;
    if (Math.abs(dx) > 30) lightboxSwiping = true;
}

function lightboxMouseUp(e) {
    if (lightboxStartX && lightboxSwiping) {
        var dx = e.clientX - lightboxStartX;
        if (dx > 50) navigateLightbox(-1);
        if (dx < -50) navigateLightbox(1);
    }
    lightboxStartX = 0; lightboxStartY = 0; lightboxSwiping = false;
}

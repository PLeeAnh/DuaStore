/* =====================================================
   DuaStore — Module: Product (Card interactions, Flash sale)
===================================================== */
'use strict';

document.addEventListener('DOMContentLoaded', function() {

    /* ═══ PRODUCT CARD ENHANCEMENTS ═══ */
    function getCard(el) { return el.closest('.ds-product-card'); }

    function updateCardFromChip(card, chip) {
        if (!chip || chip.classList.contains('oos')) return;
        card.querySelectorAll('.ds-variant-chip').forEach(function(c) { c.classList.remove('active'); });
        chip.classList.add('active');

        var newPrice = chip.getAttribute('data-price');
        var newStock = parseInt(chip.getAttribute('data-stock')) || 0;
        var stockEl = card.querySelector('.ds-stock-info');
        if (stockEl) {
            if (newStock > 0) {
                stockEl.textContent = newStock <= 3 ? '⚠ Chỉ còn ' + newStock + ' sản phẩm' : 'Còn lại: ' + newStock + ' sản phẩm';
                stockEl.className = 'ds-stock-info' + (newStock <= 3 ? ' warning' : '');
            } else {
                stockEl.textContent = 'Hết hàng';
                stockEl.className = 'ds-stock-info oos';
            }
            stockEl.style.display = '';
        }
        card.classList.toggle('oos', newStock <= 0);

        var priceBtn = card.querySelector('.ds-card-add-cart');
        if (priceBtn) {
            priceBtn.disabled = (newStock <= 0);
            var amountEl = priceBtn.querySelector('.ds-price-btn-amount');
            if (amountEl) amountEl.textContent = parseInt(newPrice).toLocaleString('vi-VN') + '₫';
        }
        var qtyVal = card.querySelector('.ds-qty-val');
        if (qtyVal) qtyVal.value = '1';
        var minus = card.querySelector('.ds-qty-minus');
        if (minus) minus.disabled = true;
    }

    document.addEventListener('click', function(e) {
        var chip = e.target.closest('.ds-variant-chip');
        if (chip && !chip.classList.contains('oos')) {
            e.preventDefault();
            var card = getCard(chip);
            if (!card) return;
            updateCardFromChip(card, chip);
        }
    });

    document.addEventListener('click', function(e) {
        var btn = e.target.closest('.ds-qty-btn');
        if (!btn) return;
        e.preventDefault();
        var card = getCard(btn);
        if (!card) return;
        var qtyEl = card.querySelector('.ds-qty-val');
        var qty = parseInt(qtyEl.value) || 1;
        var activeChip = getActiveVariant(card);
        var maxStock = activeChip ? parseInt(activeChip.getAttribute('data-stock')) || 99 : 99;
        var minus = card.querySelector('.ds-qty-minus');
        var plus = card.querySelector('.ds-qty-plus');

        if (btn.classList.contains('ds-qty-plus')) {
            if (qty < maxStock) qty++;
            else { plus.style.color = '#ef4444'; setTimeout(function() { plus.style.color = ''; }, 600); return; }
        } else if (btn.classList.contains('ds-qty-minus') && qty > 1) qty--;

        qtyEl.value = qty;
        minus.disabled = (qty <= 1);
        plus.disabled = (qty >= maxStock);
    });

    /* ═══ VARIANT GROUP TABS ═══ */
    document.addEventListener('click', function(e) {
        var tab = e.target.closest('.ds-variant-tab');
        if (!tab) return;
        e.preventDefault();
        var card = getCard(tab);
        if (!card) return;
        var group = tab.getAttribute('data-group');
        card.querySelectorAll('.ds-variant-tab').forEach(function(t) { t.classList.remove('active'); });
        tab.classList.add('active');
        card.querySelectorAll('.ds-variant-group').forEach(function(g) {
            g.classList.toggle('active', g.getAttribute('data-group') === group);
        });
    });

    /* ═══ INIT: show first tab and group per card ═══ */
    document.querySelectorAll('.ds-product-card').forEach(function(card) {
        var firstTab = card.querySelector('.ds-variant-tab');
        if (firstTab) firstTab.classList.add('active');
        var firstGroup = card.querySelector('.ds-variant-group');
        if (firstGroup) firstGroup.classList.add('active');
    });

    /* ═══ FLASH SALE COUNTDOWN ═══ */
    document.querySelectorAll('.ds-flash-timer').forEach(function(timer) {
        var endStr = timer.getAttribute('data-end');
        if (!endStr) return;
        var endDate = new Date(endStr);
        function tick() {
            var diff = endDate - new Date();
            var span = timer.querySelector('.flash-countdown');
            if (!span) return;
            if (diff <= 0) { span.textContent = 'Đã kết thúc'; timer.style.opacity = '.5'; return; }
            var days = Math.floor(diff / 86400000);
            if (days > 0) { span.textContent = days + ' days left'; return; }
            var h = Math.floor(diff / 3600000);
            var m = Math.floor((diff % 3600000) / 60000);
            var s = Math.floor((diff % 60000) / 1000);
            span.textContent = String(h).padStart(2, '0') + 'h ' + String(m).padStart(2, '0') + 'm ' + String(s).padStart(2, '0') + 's';
        }
        tick();
        setInterval(tick, 1000);
    });
});

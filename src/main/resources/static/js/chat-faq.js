/* ================================================================
   DuaStore Chat Widget – Câu hỏi thường gặp (FAQ)
================================================================ */
(function () {

  const FAQ = [
    { q: "Giao hàng mất bao lâu?", a: "🚚 DuaStore giao hàng toàn quốc trong <strong>2–3 ngày làm việc</strong>. Miễn phí vận chuyển cho đơn từ <strong>500.000₫</strong>!" },
    { q: "Chính sách đổi trả thế nào?", a: "🔄 Hỗ trợ <strong>đổi trả trong 7 ngày</strong> nếu sản phẩm bị lỗi do nhà sản xuất. Vui lòng giữ nguyên bao bì và liên hệ hotline <strong>0901 234 567</strong>." },
    { q: "Có bộ quà tặng không?", a: "🎁 Có! DuaStore có nhiều <strong>bộ quà tặng thủy tinh cao cấp</strong> phù hợp sinh nhật, cưới hỏi, khai trương. Giá từ <strong>200.000₫ – 2.000.000₫</strong>." },
    { q: "Thanh toán bằng gì?", a: "💳 Chấp nhận: <strong>COD</strong> (tiền mặt khi nhận), <strong>Chuyển khoản</strong> ngân hàng và <strong>QR (VietQR)</strong>. Hoàn toàn an toàn!" },
    { q: "Ly pha lê Bohemia có gì đặc biệt?", a: "✨ Ly pha lê Bohemia nhập từ <strong>Séc</strong> – nổi tiếng với độ trong suốt cao, họa tiết tinh xảo và độ bền vượt trội. Là dòng sản phẩm cao cấp được ưa chuộng nhất!" },
    { q: "Cửa hàng ở đâu?", a: "📍 DuaStore tại <strong>Hải Phòng</strong>. Mở cửa <strong>8:00 – 21:00</strong> tất cả các ngày trong tuần. Hotline: <strong>0901 234 567</strong>." },
    { q: "Bảo quản đồ thủy tinh thế nào?", a: "💎 Nên rửa bằng <strong>nước ấm</strong>, tránh thay đổi nhiệt độ đột ngột. Không dùng máy rửa bát cho sản phẩm pha lê. Để nơi khô ráo, tránh va đập." },
    { q: "Liên hệ tư vấn trực tiếp", a: "📞 Hotline: <strong>0901 234 567</strong><br>✉️ Email: <strong>info@duastore.vn</strong><br>⏰ Làm việc: 8:00 – 21:00 (T2 – CN)" },
  ];

  let isOpen = false;

  function createWidget() {
    const w = document.createElement('div');
    w.id = 'ai-chat-widget';
    w.innerHTML =
      `<button id="chat-toggle-btn" onclick="toggleChat()" title="Hỗ trợ khách hàng">
        <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
          <path d="M8 10h.01M12 10h.01M16 10h.01"/>
        </svg>
        <span id="chat-badge">1</span>
      </button>

      <div id="chat-box">
        <div id="chat-header">
          <div style="display:flex;align-items:center;gap:10px;">
            <div style="width:38px;height:38px;border-radius:50%;background:rgba(255,255,255,.18);display:flex;align-items:center;justify-content:center;flex-shrink:0;">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M12 2a10 10 0 0 1 10 10c0 2.5-1 4.7-2.5 6.3L21 22l-3.7-1.5A10 10 0 1 1 12 2z"/>
                <path d="M8 11h8M11 8v6"/>
              </svg>
            </div>
            <div>
              <div style="font-weight:700;font-size:14px;letter-spacing:.01em;">Chat hỗ trợ</div>
              <div style="font-size:11px;opacity:.8;display:flex;align-items:center;gap:4px;">
                <span style="width:7px;height:7px;border-radius:50%;background:#4caf50;display:inline-block;"></span>
                Đang hoạt động
              </div>
            </div>
          </div>
          <button id="chat-header-close" onclick="toggleChat()" title="Đóng">✕</button>
        </div>

        <div id="chat-messages">
          <div class="msg-bot">
            <div class="msg-bubble">
              👋 Chào bạn! Tôi là trợ lý của DuaStore.<br/>
              <span style="font-size:12px;color:#9ca3af;">Chọn câu hỏi bên dưới để được giải đáp 👇</span>
            </div>
          </div>
        </div>

        <div id="chat-faq">
          <div style="font-size:10px;color:#9ca3af;margin-bottom:8px;font-weight:600;letter-spacing:.04em;text-transform:uppercase;">
            CÂU HỎI THƯỜNG GẶP
          </div>
          <div id="faq-list"></div>
        </div>
      </div>`;
    document.body.appendChild(w);

    const faqList = document.getElementById('faq-list');
    FAQ.forEach((item, i) => {
      const btn = document.createElement('button');
      btn.className = 'faq-btn';
      btn.textContent = item.q;
      btn.onclick = () => showAnswer(i);
      faqList.appendChild(btn);
    });

    setTimeout(() => {
      if (!isOpen) document.getElementById('chat-badge').style.display = 'flex';
    }, 2000);
  }

  window.toggleChat = function () {
    isOpen = !isOpen;
    const box = document.getElementById('chat-box');
    const icon = document.getElementById('chat-toggle-btn');
    const badge = document.getElementById('chat-badge');
    box.style.display = isOpen ? 'flex' : 'none';
    badge.style.display = 'none';
    if (!isOpen) {
      document.getElementById('chat-faq').style.display = 'block';
    }
  };

  window.showAnswer = function (index) {
    const item = FAQ[index];
    const messages = document.getElementById('chat-messages');

    const userDiv = document.createElement('div');
    userDiv.className = 'msg-user';
    userDiv.innerHTML = `<div class="msg-bubble">${item.q}</div>`;
    messages.appendChild(userDiv);

    const typingDiv = document.createElement('div');
    typingDiv.className = 'msg-bot';
    typingDiv.innerHTML = `<div class="msg-bubble">
      <span class="typing-dot"></span>
      <span class="typing-dot"></span>
      <span class="typing-dot"></span>
    </div>`;
    messages.appendChild(typingDiv);
    messages.scrollTop = messages.scrollHeight;

    setTimeout(() => {
      typingDiv.remove();
      const botDiv = document.createElement('div');
      botDiv.className = 'msg-bot';
      botDiv.innerHTML = `<div class="msg-bubble">${item.a}</div>`;
      messages.appendChild(botDiv);

      const moreDiv = document.createElement('div');
      moreDiv.className = 'msg-bot';
      moreDiv.innerHTML = `<button onclick="resetFaq()" class="faq-btn" style="font-size:12px;padding:6px 14px;width:auto;">← Xem câu hỏi khác</button>`;
      messages.appendChild(moreDiv);
      messages.scrollTop = messages.scrollHeight;

      document.getElementById('chat-faq').style.display = 'none';
    }, 600);
  };

  window.resetFaq = function () {
    document.getElementById('chat-faq').style.display = 'block';
    document.getElementById('chat-messages').scrollTop = document.getElementById('chat-messages').scrollHeight;
  };

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', createWidget);
  } else {
    createWidget();
  }
})();

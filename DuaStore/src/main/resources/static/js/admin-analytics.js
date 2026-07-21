'use strict';
let copilotOpen = true;
function toggleCopilot() {
    copilotOpen = !copilotOpen;
    document.getElementById('copilotBody').style.display = copilotOpen ? 'block' : 'none';
    document.getElementById('copilotArrow').style.transform = copilotOpen ? 'rotate(0deg)' : 'rotate(-90deg)';
}
function askCopilot() {
    const q = document.getElementById('copilotQuery').value.trim();
    if (!q) return;
    const btn = document.getElementById('copilotBtn');
    const answerDiv = document.getElementById('copilotAnswer');
    const errorDiv = document.getElementById('copilotError');
    answerDiv.style.display = 'none';
    errorDiv.style.display = 'none';
    btn.disabled = true;
    btn.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span>Đang trả lời...';
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
    const headers = {'Content-Type': 'application/json'};
    if (csrfToken && csrfHeader) headers[csrfHeader] = csrfToken;
    fetch('/admin/phan-tich/api/copilot', {
        method: 'POST',
        headers: headers,
        body: JSON.stringify({query: q})
    })
    .then(r => r.json())
    .then(data => {
        answerDiv.textContent = data.answer;
        answerDiv.style.display = 'block';
    })
    .catch(e => {
        errorDiv.textContent = 'Lỗi kết nối, vui lòng thử lại.';
        errorDiv.style.display = 'block';
    })
    .finally(() => {
        btn.disabled = false;
        btn.innerHTML = '<i class="bi bi-send me-1"></i>Hỏi';
    });
}

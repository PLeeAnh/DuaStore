var customerId = window.__customerId;

function addTag() {
    var input = document.getElementById('newTagInput');
    var tag = input.value.trim();
    if (!tag) return;
    fetch('/admin/khach-hang/' + customerId + '/api/tags', {
        method: 'POST',
        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
        body: 'tag=' + encodeURIComponent(tag)
    }).then(function(r) { return r.json(); }).then(function(d) {
        if (d.error) { if (window.dsToast) dsToast('error', d.error); return; }
        var list = document.getElementById('tagList');
        var span = document.createElement('span');
        span.className = 'badge rounded-pill text-bg-primary d-inline-flex align-items-center gap-1';
        span.style.cssText = 'font-size:0.85rem;padding:6px 12px;';
        span.innerHTML = '<span>' + d.tag + '</span> ' +
            '<a href="javascript:void(0)" class="text-white text-decoration-none ms-1" onclick="removeTag(' + d.id + ')" style="opacity:.7">&times;</a>';
        var empty = list.querySelector('.text-muted');
        if (empty) empty.remove();
        list.appendChild(span);
        input.value = '';
    }).catch(function(err) { console.error(err); });
}
function removeTag(tagId) {
    if (!confirm('Xóa thẻ này?')) return;
    fetch('/admin/khach-hang/' + customerId + '/api/tags/' + tagId, { method: 'DELETE' })
        .then(function(r) { return r.json(); }).then(function() {
            if (window.__dsToastAfterReload) window.__dsToastAfterReload('Đã xóa thẻ');
            location.reload();
        })
        .catch(function(err) { console.error(err); });
}
function addNote() {
    var input = document.getElementById('newNoteInput');
    var severityEl = document.getElementById('newNoteSeverity');
    var content = input.value.trim();
    if (!content) return;
    var severity = severityEl ? severityEl.value : 'INFO';
    fetch('/admin/khach-hang/' + customerId + '/api/notes', {
        method: 'POST',
        headers: {'Content-Type': 'application/x-www-form-urlencoded'},
        body: 'content=' + encodeURIComponent(content) + '&severity=' + encodeURIComponent(severity)
    }).then(function(r) { return r.json(); }).then(function(d) {
        var list = document.getElementById('noteList');
        var div = document.createElement('div');
        var borderClass = d.severity === 'DANGER' ? 'border-danger' : (d.severity === 'WARN' ? 'border-warning' : 'border-primary');
        div.className = 'border-start border-3 ps-3 mb-3 ' + borderClass;
        var badge = d.severity && d.severity !== 'INFO'
            ? ' <span class="badge ' + (d.severity === 'DANGER' ? 'bg-danger' : 'bg-warning text-dark') + '">' + d.severity + '</span>'
            : '';
        div.innerHTML = '<div class="text-muted small">' + d.createdBy + badge + ' - ' + d.createdAt + '</div>' +
            '<div class="mt-1">' + d.content + '</div>' +
            '<a href="javascript:void(0)" class="small text-danger" onclick="deleteNote(' + d.id + ')">Xóa</a>';
        var empty = list.querySelector('.text-muted.py-3');
        if (empty) empty.remove();
        list.prepend(div);
        input.value = '';
        if (severityEl) severityEl.value = 'INFO';
    }).catch(function(err) { console.error(err); });
}
function deleteNote(noteId) {
    if (!confirm('Xóa ghi chú này?')) return;
    fetch('/admin/khach-hang/' + customerId + '/api/notes/' + noteId, { method: 'DELETE' })
        .then(function(r) { return r.json(); }).then(function() {
            if (window.__dsToastAfterReload) window.__dsToastAfterReload('Đã xóa ghi chú');
            location.reload();
        })
        .catch(function(err) { console.error(err); });
}

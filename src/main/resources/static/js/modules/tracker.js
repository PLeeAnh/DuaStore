(function() {
    var sessionId = sessionStorage.getItem('ds_session_id');
    if (!sessionId) {
        sessionId = 's_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9);
        sessionStorage.setItem('ds_session_id', sessionId);
    }

    function track(eventType, data) {
        var payload = Object.assign({ eventType: eventType, pagePath: window.location.pathname }, data || {});
        navigator.sendBeacon('/api/pageview/track', new Blob([JSON.stringify(payload)], {
            type: 'application/json',
            headers: { 'X-Session-Id': sessionId }
        }));
    }

    track('PAGE_VIEW');

    document.addEventListener('click', function(e) {
        var btn = e.target.closest('[data-track]');
        if (btn) {
            track(btn.getAttribute('data-track'), { metadata: btn.getAttribute('data-track-label') || '' });
        }
    });

    window.DuaStore = window.DuaStore || {};
    window.DuaStore.track = track;
})();

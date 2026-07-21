if (window.opener && window.opener !== window) {
    window.opener.location.reload();
    window.close();
} else {
    window.location.href = '/';
}

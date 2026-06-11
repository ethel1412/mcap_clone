document.addEventListener('DOMContentLoaded', function () {
    var refreshBtn = document.getElementById('instituteCaptchaRefreshBtn');
    if (refreshBtn) {
        refreshBtn.addEventListener('click', function () {
            if (typeof instituteCaptcha !== 'undefined') {
                instituteCaptcha.refresh();
            }
        });
    }
});
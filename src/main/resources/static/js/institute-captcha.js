(function (global) {
    'use strict';

    var CAPTCHA_URL = '/captcha/get-captcha';
    var IMG_ID      = 'instituteCaptchaImg';
    var INPUT_ID    = 'instituteCaptchaInput';
    var SECTION_ID  = 'institute-captcha-section';
    var ERROR_ID    = 'instituteCaptchaError';

    function getCsrf() {
        var tokenMeta  = document.querySelector('meta[name="_csrf"]');
        var headerMeta = document.querySelector('meta[name="_csrf_header"]');
        return {
            token:  tokenMeta  ? tokenMeta.getAttribute('content')  : '',
            header: headerMeta ? headerMeta.getAttribute('content') : 'X-CSRF-TOKEN'
        };
    }

    function clearError() {
        var el = document.getElementById(ERROR_ID);
        if (el) {
            el.textContent = '';
            el.classList.add('d-none');
        }
    }

    function showError(message) {
        var el = document.getElementById(ERROR_ID);
        if (el) {
            el.textContent = message;
            el.classList.remove('d-none');
        }
    }

    function load() {
        var csrf  = getCsrf();
        var img   = document.getElementById(IMG_ID);
        var input = document.getElementById(INPUT_ID);

        if (!img) { return; }

        clearError();
        if (input) { input.value = ''; }

        var headers = { 'Content-Type': 'application/x-www-form-urlencoded' };
        headers[csrf.header] = csrf.token;

        fetch(CAPTCHA_URL, { method: 'POST', headers: headers })
            .then(function (res) {
                if (!res.ok) { throw new Error('HTTP ' + res.status); }
                return res.text();
            })
            .then(function (base64) {
                img.src = 'data:image/png;base64,' + base64;
                img.setAttribute('alt', 'CAPTCHA image');
            })
            .catch(function () {
                showError('Unable to load CAPTCHA. Please refresh the page.');
            });
    }

    function showSection() {
        var section = document.getElementById(SECTION_ID);
        if (section) {
            section.classList.remove('d-none');
            load();
        }
    }

    function hideSection() {
        var section = document.getElementById(SECTION_ID);
        if (section) {
            section.classList.add('d-none');
            clearError();
            var input = document.getElementById(INPUT_ID);
            if (input) { input.value = ''; }
        }
    }

    global.instituteCaptcha = {
        load:         load,
        refresh:      load,
        showSection:  showSection,
        hideSection:  hideSection
    };

}(window));

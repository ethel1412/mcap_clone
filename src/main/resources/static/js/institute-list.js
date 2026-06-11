'use strict';

(function () {

    // FIX: was getElementById('institutesTable') — too specific, fails on dashboard.
    // Now checks for the toggle element itself, works on any page.
    if (!document.querySelector('.institute-active-toggle')) return;

    const csrfToken  = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

    if (!csrfToken || !csrfHeader) {
        console.error('CSRF meta tags missing from <head>. Toggle will not work.');
        return;
    }

    document.addEventListener('change', function (e) {
        const toggle = e.target.closest('.institute-active-toggle');
        if (!toggle) return;

        const instituteId   = toggle.dataset.instituteId;
        const intendedState = toggle.checked;

        toggle.disabled = true;

        fetch('/institute/' + instituteId + '/toggle-active', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                [csrfHeader]: csrfToken
            }
        })
        .then(function (res) {
            if (!res.ok) {
                return res.text().then(function (body) {
                    throw new Error('Server error ' + res.status + ': ' + body);
                });
            }
            return res.json();
        })
        .then(function (data) {
            toggle.checked = data.active;
            toggle.title   = data.active ? 'Click to deactivate' : 'Click to activate';
            toggle.setAttribute('aria-checked', String(data.active));
        })
        .catch(function (err) {
            toggle.checked = !intendedState;
            console.error('Toggle failed:', err);
            alert('Could not update institute status. Please try again.\n' + err.message);
        })
        .finally(function () {
            toggle.disabled = false;
        });
    });

})();
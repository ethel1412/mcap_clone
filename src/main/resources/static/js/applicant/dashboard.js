import { showSuccess, showError } from './utils.js'; // CSP FIX: Imported safe utils

document.addEventListener('DOMContentLoaded', function () {
    const successEl = document.getElementById('academicSuccess');
    const errorEl = document.getElementById('academicError');

    if (successEl && successEl.value) {
        showSuccess(successEl.value); // CSP FIX: Removed Swal.fire
    }

    if (errorEl && errorEl.value) {
        showError(errorEl.value); // CSP FIX: Removed Swal.fire
    }

    const sidebar = document.getElementById('sidebarMenu');
    const toggler = document.querySelector('.navbar-toggler');
    const overlay = document.querySelector('.sidebar-overlay');

    if (toggler && sidebar && overlay) {
        const openSidebar = () => {
            sidebar.classList.add('show');
            // CSP FIX: Replaced inline style with Bootstrap classes
            overlay.classList.remove('d-none');
            overlay.classList.add('d-block');
            toggler.setAttribute('aria-expanded', 'true');
        };

        const closeSidebar = () => {
            sidebar.classList.remove('show');
            // CSP FIX: Replaced inline style with Bootstrap classes
            overlay.classList.remove('d-block');
            overlay.classList.add('d-none');
            toggler.setAttribute('aria-expanded', 'false');
        };

        toggler.addEventListener('click', () => {
            if (sidebar.classList.contains('show')) {
                closeSidebar();
            } else {
                openSidebar();
            }
        });

        overlay.addEventListener('click', closeSidebar);

        sidebar.addEventListener('click', (e) => {
            if (e.target.closest('.nav-loader')) {
                setTimeout(closeSidebar, 150);
            }
        });
    }
});
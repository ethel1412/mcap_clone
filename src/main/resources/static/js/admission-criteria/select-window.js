// CSRF Token Setup (safe)
const csrfMeta = document.querySelector('meta[name="_csrf"]');
const csrfHeaderMeta = document.querySelector('meta[name="_csrf_header"]');

const csrfToken = csrfMeta ? csrfMeta.getAttribute('content') : null;
const csrfHeader = csrfHeaderMeta ? csrfHeaderMeta.getAttribute('content') : null;

// Configure Axios only if CSRF meta exists
if (csrfToken && csrfHeader) {
    axios.defaults.headers.common[csrfHeader] = csrfToken;
    axios.defaults.withCredentials = true;
}

// DOM Elements
const loadingState = document.getElementById('loading-state');
const windowsGrid = document.getElementById('windows-grid');
const noWindowsState = document.getElementById('no-windows-state');

// Initialize on Page Load
document.addEventListener('DOMContentLoaded', () => {
    initializePage();
});

// Initialize Page
function initializePage() {
    // Check if we have windows from server-side rendering
    const hasWindows = windowsGrid && windowsGrid.children.length > 0;

    // CSP FIX: Replaced .style.display with .classList
    if (loadingState) loadingState.classList.add('d-none');

    if (hasWindows) {
        if (windowsGrid) windowsGrid.classList.remove('d-none');
        if (noWindowsState) noWindowsState.classList.add('d-none');
    } else {
        if (windowsGrid) windowsGrid.classList.add('d-none');
        if (noWindowsState) noWindowsState.classList.remove('d-none');
    }
}
// Show Toast Notification
function showToast(message, type, position = 'top') {
    const toastId = `toast-${Date.now()}`;
    const containerClass = position === 'top' ? 'toast-container-top' : 'toast-container-bottom';

    let container = document.querySelector(`.${containerClass}`);
    if (!container) {
        container = document.createElement('div');
        container.className = containerClass;
        document.body.appendChild(container);
    }

    const toastHtml = `
        <div id="${toastId}" class="toast custom-toast toast-${type}" role="alert">
            <div class="toast-header">
                <i class="bi bi-${type === 'success' ? 'check-circle-fill' : 'exclamation-triangle-fill'} me-2"></i>
                <strong class="me-auto">${type === 'success' ? 'Success' : 'Error'}</strong>
                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="toast"></button>
            </div>
            <div class="toast-body">${message}</div>
        </div>
    `;

    container.insertAdjacentHTML('beforeend', toastHtml);

    const toastElement = document.getElementById(toastId);
    const bsToast = new bootstrap.Toast(toastElement, {
        autohide: true,
        delay: type === 'error' ? 5000 : 3000
    });

    bsToast.show();

    toastElement.addEventListener('hidden.bs.toast', () => {
        toastElement.remove();
    });
}

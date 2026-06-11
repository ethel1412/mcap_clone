/**
 * CSP-Compliant Utility Functions
 * Replaces SweetAlert2 with native Bootstrap to avoid inline-style violations.
 */

// --- 1. Success Toast (Automatic dismissal) ---
export const showSuccess = (msg) => {
    const toastContainer = document.getElementById('toast-container') || createToastContainer();
    const id = 'toast-' + Date.now();
    
    const html = `
        <div id="${id}" class="toast align-items-center text-white bg-success border-0" role="alert" aria-live="assertive" aria-atomic="true">
            <div class="d-flex">
                <div class="toast-body"><i class="bi bi-check-circle-fill me-2"></i>${msg}</div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button>
            </div>
        </div>`;
    
    toastContainer.insertAdjacentHTML('beforeend', html);
    const toastEl = document.getElementById(id);
    const toast = new bootstrap.Toast(toastEl, { delay: 2000 });
    toast.show();
    toastEl.addEventListener('hidden.bs.toast', () => toastEl.remove());
};

// --- 2. Error/Warning Alerts (Require dismissal) ---
export const showError = (msg) => showAlertModal('Error!', msg, 'text-danger', 'bi-x-circle-fill');
export const showWarning = (msg) => showAlertModal('Heads up!', msg, 'text-warning', 'bi-exclamation-triangle-fill');

// --- 3. Loading Overlay ---
export const showLoading = (msg) => {
    let loader = document.getElementById('global-loader');
    if (!loader) {
        // CSP FIX: Replaced style="z-index: 9999;" with z-max class
        const html = `
            <div id="global-loader" class="position-fixed top-0 start-0 w-100 h-100 d-flex flex-column align-items-center justify-content-center bg-dark bg-opacity-75 z-max">
                <div class="spinner-border text-light mb-2" role="status"></div>
                <div class="text-white fw-bold" id="loader-text">${msg}</div>
            </div>`;
        document.body.insertAdjacentHTML('beforeend', html);
    } else {
        document.getElementById('loader-text').textContent = msg;
        loader.classList.remove('d-none');
    }
};

export const hideLoading = () => {
    const loader = document.getElementById('global-loader');
    if (loader) loader.classList.add('d-none');
};

// --- 4. Confirmation Dialog ---
export const showConfirm = (title, message, confirmText = 'Yes', confirmClass = 'btn-primary') => {
    return new Promise((resolve) => {
        const modalId = 'confirm-modal';
        let modalEl = document.getElementById(modalId);

        if (modalEl) modalEl.remove(); // Clean up old instances

        const html = `
            <div class="modal fade" id="${modalId}" tabindex="-1" aria-hidden="true">
                <div class="modal-dialog modal-dialog-centered">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h5 class="modal-title">${title}</h5>
                            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                        </div>
                        <div class="modal-body">${message}</div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                            <button type="button" id="modal-confirm-btn" class="btn ${confirmClass}">${confirmText}</button>
                        </div>
                    </div>
                </div>
            </div>`;

        document.body.insertAdjacentHTML('beforeend', html);
        modalEl = document.getElementById(modalId);
        const bsModal = new bootstrap.Modal(modalEl);

        document.getElementById('modal-confirm-btn').onclick = () => {
            bsModal.hide();
            resolve({ isConfirmed: true });
        };

        modalEl.addEventListener('hidden.bs.modal', () => {
            resolve({ isConfirmed: false });
            modalEl.remove();
        });

        bsModal.show();
    });
};

// --- Helpers ---
function createToastContainer() {
    // CSP FIX: Replaced style="z-index: 1060;" with z-toast class
    document.body.insertAdjacentHTML('beforeend', '<div id="toast-container" class="toast-container position-fixed bottom-0 end-0 p-3 z-toast"></div>');
    return document.getElementById('toast-container');
}

function showAlertModal(title, msg, textClass, icon) {
    const html = `
        <div class="modal fade" id="alert-modal" tabindex="-1" aria-hidden="true">
            <div class="modal-dialog modal-dialog-centered">
                <div class="modal-content">
                    <div class="modal-header border-0">
                        <h5 class="modal-title ${textClass}"><i class="bi ${icon} me-2"></i>${title}</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body py-0">${msg}</div>
                    <div class="modal-footer border-0">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">OK</button>
                    </div>
                </div>
            </div>
        </div>`;
    const existing = document.getElementById('alert-modal');
    if (existing) existing.remove();
    document.body.insertAdjacentHTML('beforeend', html);
    new bootstrap.Modal(document.getElementById('alert-modal')).show();
}
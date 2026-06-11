import { showError } from './utils.js';

function getModalInstance() {
    const modalEl = document.getElementById('pdfPreviewModal');
    if (!modalEl) {
        showError("Preview modal element not found.");
        return null;
    }
    return bootstrap.Modal.getOrCreateInstance(modalEl);
}

export function showPdfPreviewModal() {
    const previewModal = getModalInstance();
    if (!previewModal) return;

    const applicationId = sessionStorage.getItem('activeApplicationId');
    if (!applicationId) return showError("No active application is selected.");

    const modalTitle = document.getElementById('pdfPreviewModalLabel');
    const modalBody = document.getElementById('pdfPreviewModalBody');
    const downloadBtn = document.getElementById('confirmDownloadBtn');

    if(modalTitle) modalTitle.textContent = 'Application Form Preview';

    // CSP FIX: Use Bootstrap classes for display
    if(downloadBtn) {
        downloadBtn.classList.remove('d-none');
        downloadBtn.classList.add('d-block');
    }

    const pdfUrl = `/applicants/application/print-pdf/${applicationId}`;

    // CSP FIX: Removed inline styles on iframe, used Bootstrap utility classes
    if(modalBody) {
        modalBody.innerHTML = `<iframe src="${pdfUrl}" title="Application Preview" class="w-100 h-100 border-0"></iframe>`;
    }

    const downloadHandler = () => {
	    const newWindow = window.open(pdfUrl, '_blank', 'noopener,noreferrer');
	    if (newWindow) newWindow.opener = null;
	};
    const newDownloadBtn = downloadBtn.cloneNode(true);
    downloadBtn.parentNode.replaceChild(newDownloadBtn, downloadBtn);
    newDownloadBtn.addEventListener('click', downloadHandler);

    previewModal.show();
}

export function previewUploadedDocument(documentId, originalName) {
    const previewModal = getModalInstance();
    if (!previewModal) return;

    if (!documentId) return showError("Document ID is missing.");

    const modalTitle = document.getElementById('pdfPreviewModalLabel');
    const modalBody = document.getElementById('pdfPreviewModalBody');
    const downloadBtn = document.getElementById('confirmDownloadBtn');

    if(modalTitle) modalTitle.textContent = `Preview: ${originalName || 'Document'}`;
    if(modalBody) modalBody.innerHTML = `<div class="d-flex justify-content-center align-items-center h-100"><div class="spinner-border"></div></div>`;

    // CSP FIX: Use Bootstrap class
    if(downloadBtn) {
        downloadBtn.classList.remove('d-block');
        downloadBtn.classList.add('d-none');
    }

    const url = `/applicants/documents/${documentId}`;
    const ext = originalName ? originalName.split('.').pop().toLowerCase() : '';

    if (['jpg', 'jpeg', 'png', 'gif', 'webp'].includes(ext)) {
        // CSP FIX: Removed inline style on img
        if(modalBody) modalBody.innerHTML = `<img src="${url}" alt="Preview of ${originalName}" class="img-fluid w-100 h-100 object-fit-contain mx-auto d-block">`;
    } else if (ext === 'pdf') {
        // CSP FIX: Removed inline style on iframe
        if(modalBody) modalBody.innerHTML = `<iframe src="${url}" title="PDF Preview" class="w-100 h-100 border-0"></iframe>`;
    } else {
        if(modalBody) modalBody.innerHTML = `<div class="text-center p-5"><h4>No preview available for this file type.</h4></div>`;
    }

    previewModal.show();
}

export function previewFile(file) {
    const previewModal = getModalInstance();
    if (!previewModal) return;

    const modalTitle = document.getElementById('pdfPreviewModalLabel');
    const modalBody = document.getElementById('pdfPreviewModalBody');
    const downloadBtn = document.getElementById('confirmDownloadBtn');

    if (modalTitle) modalTitle.textContent = `Staged Preview: ${file.name}`;

    // CSP FIX: Use Bootstrap class
    if (downloadBtn) {
        downloadBtn.classList.remove('d-block');
        downloadBtn.classList.add('d-none');
    }

    if (modalBody) modalBody.innerHTML = '';

    if (file.type.startsWith('image/')) {
        const reader = new FileReader();
        reader.onload = e => {
            // CSP FIX: Removed inline style on img
            if (modalBody) modalBody.innerHTML = `<img src="${e.target.result}" alt="Preview" class="img-fluid w-100 h-100 object-fit-contain mx-auto d-block">`;
        };
        reader.readAsDataURL(file);
    } else if (file.type === 'application/pdf') {
        const modalEl = document.getElementById('pdfPreviewModal');
        const fileUrl = URL.createObjectURL(file);
        // CSP FIX: Removed inline style on iframe
        if (modalBody) modalBody.innerHTML = `<iframe src="${fileUrl}" title="PDF Preview" class="w-100 h-100 border-0"></iframe>`;
        modalEl.addEventListener('hidden.bs.modal', () => URL.revokeObjectURL(fileUrl), { once: true });
    } else {
        if (modalBody) modalBody.innerHTML = `<div class="text-center p-5"><h4>No preview available for this file type.</h4></div>`;
    }

    previewModal.show();
}
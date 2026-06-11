import { showSuccess, showError, showWarning, showLoading, hideLoading, showConfirm } from './utils.js';
import { updateSidebarState } from './applicant.js';
import { previewUploadedDocument } from './pdf.js';

function validateFile(file, input) {
    const minSizeKB = parseInt(input.dataset.minSize, 10);
    const maxSizeKB = parseInt(input.dataset.maxSize, 10);
    const allowedTypes = (input.accept || '').split(',').map(t => t.trim().toLowerCase());
    const fileSizeKB = file.size / 1024;
    const fileExtension = '.' + file.name.split('.').pop().toLowerCase();
    const fileMimeType = file.type.toLowerCase();
    if (!allowedTypes.includes(fileExtension) && !allowedTypes.includes(fileMimeType)) {
        return `Invalid file type. Please select one of: ${input.accept}.`;
    }
    if (fileSizeKB < minSizeKB) {
        return `File is too small. Minimum size is ${minSizeKB} KB.`;
    }
    if (fileSizeKB > maxSizeKB) {
        return `File is too large. Maximum size is ${maxSizeKB} KB.`;
    }
    return null;
}

function renderPreview(file, previewBox) {
    const contentArea = previewBox.querySelector('.preview-content');
    if (!contentArea) return;

    contentArea.innerHTML = '';
    if (file.type.startsWith('image/')) {
        const reader = new FileReader();
        reader.onload = (e) => {
            const img = document.createElement('img');
            img.src = e.target.result;
            img.alt = 'File preview';
            img.className = 'img-fluid h-100 preview-image';
            contentArea.appendChild(img);
        };
        reader.readAsDataURL(file);
    } else {
        contentArea.replaceChildren();

        const wrapper = document.createElement("div");
        wrapper.className = "d-flex flex-column justify-content-center h-100 text-muted";

        const icon = document.createElement("i");
        icon.className = "bi bi-file-earmark-pdf-fill fs-2";

        const nameSpan = document.createElement("span");
        // CSP FIX: Replaced inline style.wordBreak with Bootstrap 'text-break' class
        nameSpan.className = "small text-break";
        nameSpan.textContent = file.name;

        wrapper.appendChild(icon);
        wrapper.appendChild(nameSpan);

        contentArea.appendChild(wrapper);
    }
}

async function uploadFile(file, docType, documentRow) {
    const formData = new FormData();
    formData.append('documentFile', file);
    formData.append('documentType', docType);
    const spinner = documentRow.querySelector('.upload-spinner');
    const label = documentRow.querySelector('.file-upload-label');
    const input = documentRow.querySelector('.document-file-input');
    const previewBox = documentRow.querySelector('.preview-box');

    if (spinner) spinner.classList.add('is-uploading');
    label.classList.add('disabled', 'pe-none'); // CSP FIX
    input.disabled = true;

    try {
        const response = await axios.post('/applicants/documents/upload', formData, {
            headers: { 'Content-Type': 'multipart/form-data' }
        });
        showSuccess(response.data.message || 'File uploaded successfully.');
        label.classList.remove('btn-primary');
        label.classList.add('btn-outline-secondary');

        if (!label.querySelector('.position-absolute')) {
             label.innerHTML += `<span class="position-absolute top-0 start-100 translate-middle p-1 bg-success border border-light rounded-circle" title="Uploaded"><span class="visually-hidden">Uploaded</span></span>`;
        }

        label.querySelector('span:not([class*="position-absolute"])').textContent = 'Change File';
        label.querySelector('i').className = 'bi bi-arrow-repeat me-2';

        const { documentId, fileName } = response.data;
        previewBox.dataset.documentId = documentId;
        previewBox.dataset.originalName = fileName;
        previewBox.classList.add('clickable-preview');

    } catch (err) {
        const errorMsg = err.response?.data?.message || 'Upload failed. Please try again.';
        showError(errorMsg);

        const contentArea = previewBox.querySelector('.preview-content');
        if (contentArea) {
            contentArea.innerHTML = `<span class="preview-placeholder text-muted small d-flex align-items-center justify-content-center h-100"><i class="bi bi-image me-2"></i>Preview</span>`;
        }
        previewBox.classList.remove('clickable-preview');
        delete previewBox.dataset.documentId;
        delete previewBox.dataset.originalName;
    } finally {
        if (spinner) spinner.classList.remove('is-uploading');
        label.classList.remove('disabled', 'pe-none'); // CSP FIX
        input.disabled = false;
        input.value = '';
    }
}

function handleFileSelection(e) {
    const input = e.target;
    if (!input.files || input.files.length === 0) return;
    const file = input.files[0];
    const docType = input.dataset.docType;
    const documentRow = input.closest('.document-row');
    const previewBox = documentRow.querySelector('.preview-box');
    const validationMessageEl = documentRow.querySelector('.validation-message');
    validationMessageEl.textContent = '';
    const validationError = validateFile(file, input);
    if (validationError) {
        showError(validationError);
        validationMessageEl.textContent = validationError;
        input.value = '';
        return;
    }
    renderPreview(file, previewBox);
    uploadFile(file, docType, documentRow);
}

async function finalSaveConfirmation() {
    const applicationId = sessionStorage.getItem('activeApplicationId');
    if (!applicationId) return showError("Cannot determine the current application.");

    const documentRows = document.querySelectorAll('#document-upload-container .document-row');
    const missingDocuments = [];
    documentRows.forEach(row => {
        const previewBox = row.querySelector('.preview-box');
        if (!previewBox || !previewBox.dataset.documentId) {
            const label = row.querySelector('h6').textContent;
            missingDocuments.push(label.trim());
        }
    });
    if (missingDocuments.length > 0) {
        return showError(`You must upload all required documents. Missing: ${missingDocuments.join(', ')}`);
    }

    // CSP FIX: Replaced SweetAlert2 with native showConfirm
    const result = await showConfirm(
        'Finalize Your Documents?',
        'Once finalized, you cannot change your documents.<br/>Please ensure all uploads are correct.',
        'Yes, Finalize & Continue',
        'btn-success'
    );

    if (result.isConfirmed) {
        showLoading('Finalizing documents...');
        try {
            const response = await axios.post(`/applicants/documents/finalize/${applicationId}`);
            updateSidebarState(response.data);
            hideLoading();
            showSuccess("Documents finalized successfully.");

            const finalizeBtn = document.getElementById('final-save-docs-btn');
            if (finalizeBtn) {
                finalizeBtn.disabled = true;
                finalizeBtn.innerHTML = `<i class="bi bi-check-circle-fill me-2"></i> Documents Finalized`;
            }

            document.querySelectorAll('.document-file-input').forEach(input => {
                input.disabled = true;
            });
            document.querySelectorAll('.file-upload-label').forEach(label => {
                // CSP FIX: Replaced inline style with Bootstrap 'pe-none'
                label.classList.add('disabled', 'pe-none');
            });

            const nextLink = document.querySelector('.nav-link[data-url*="payment/show-details"]');
            if (nextLink) nextLink.click();
        } catch (err) {
            hideLoading();
            showError(err.response?.data?.message || 'An unexpected server error occurred.');
        }
    }
}

function initializeDocumentUploadForm() {
    const container = document.getElementById('document-upload-container');
    const finalizeBtn = document.getElementById('final-save-docs-btn');

    if (finalizeBtn && finalizeBtn.disabled) {
        document.querySelectorAll('.document-file-input').forEach(input => {
            input.disabled = true;
        });
        document.querySelectorAll('.file-upload-label').forEach(label => {
            // CSP FIX: Replaced inline style with Bootstrap 'pe-none'
            label.classList.add('disabled', 'pe-none');
        });
    }

    if (container) {
        container.addEventListener('change', (e) => {
            if (e.target.classList.contains('document-file-input')) {
                handleFileSelection(e);
            }
        });
        container.addEventListener('click', (e) => {
            const previewBox = e.target.closest('.clickable-preview');
            if (previewBox) {
                const docId = previewBox.dataset.documentId;
                const originalName = previewBox.dataset.originalName;
                if (docId && originalName) {
                    previewUploadedDocument(docId, originalName);
                }
            }
        });
    }
    if (finalizeBtn) {
        finalizeBtn.addEventListener('click', finalSaveConfirmation);
    }
}

export { initializeDocumentUploadForm };
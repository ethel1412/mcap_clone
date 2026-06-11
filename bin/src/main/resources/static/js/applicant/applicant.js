document.addEventListener('DOMContentLoaded', function () {

    // --- Core DOM Elements ---
    const contentArea = document.getElementById('main-content-area');
    const sidebar = document.getElementById('sidebarMenu');
    const sidebarToggle = document.querySelector('.navbar-toggler');

    // --- Axios CSRF Token Configuration ---
    const token = document.querySelector('meta[name="_csrf"]').content;
    const header = document.querySelector('meta[name="_csrf_header"]').content;
    axios.defaults.headers.common[header] = token;


    
    //  GLOBAL PREVIEW DELEGATION (for both staged & uploaded)  
   
    document.addEventListener('click', function(e) {
        // Staged file preview (no changes here)
        if (e.target.closest('.preview-staged-btn')) {
            e.preventDefault();
            const form = e.target.closest('form.document-upload-form');
            const fileInput = form.querySelector('input[type="file"]');
            if (fileInput.files && fileInput.files.length > 0) {
                previewFile(fileInput.files[0]);
            } else {
                Swal.fire('No File', 'Please select a file first.', 'warning');
            }
            return;
        }

        // ✅ NEW, CORRECTED CODE for uploaded file preview
        if (e.target.closest('.preview-uploaded-btn')) {
            e.preventDefault();
            const btn = e.target.closest('.preview-uploaded-btn');
            const documentId = btn.dataset.documentId; // Read the document ID
            const originalName = btn.dataset.originalName;
            if (documentId && originalName) {
                // Call the function with the document ID instead of the filename
                previewUploadedDocument(documentId, originalName);
            }
        }
    });



    // INITIALIZATION FUNCTIONS (CALLED AFTER FRAGMENT LOADS) 

    function initializeDashboard() {
        const selector = document.getElementById('applicationSelector');
        if (selector) {
            const activeId = sessionStorage.getItem('activeApplicationId');
            if (activeId) selector.value = activeId;
            selector.addEventListener('change', function() {
                const selectedId = this.value;
                if (selectedId) {
                    sessionStorage.setItem('activeApplicationId', selectedId);
                    Swal.fire({
                        icon: 'success',
                        title: 'Application Selected',
                        text: 'You are now working on the ' +
                              this.options[this.selectedIndex].text + ' application.',
                        timer: 2000,
                        showConfirmButton: false
                    });
                } else {
                    sessionStorage.removeItem('activeApplicationId');
                }
            });
        }
    }

    function initializePersonalDetailsForm() {
        const form = document.getElementById('personal-details-form');
        if (form) form.addEventListener('submit', handleAjaxFormSubmit);
    }

    function initializeDynamicAcademicForm() {
        const container = document.getElementById('records-container');
        const addButton = document.getElementById('add-record-btn');
        const removeButton = document.getElementById('remove-record-btn');
        const template = document.getElementById('record-template');
        if (!container || !addButton || !removeButton || !template) return;

        // SMART ADD MORE
        addButton.addEventListener('click', function() {
            const rows = container.querySelectorAll('.record-row');
            const lastRow = rows[rows.length - 1];
            let valid = true;
            lastRow.querySelectorAll('[required]').forEach(input => {
                if (!input.value.trim()) valid = false;
            });
            if (!valid) {
                Swal.fire('Incomplete', 'Fill all required fields before adding.', 'warning');
                return;
            }
            const newRow = template.cloneNode(true);
            newRow.removeAttribute('id');
            newRow.style.display = 'table-row';
            const idx = rows.length;
            newRow.querySelectorAll('input, select, textarea').forEach(input => {
                const name = input.getAttribute('name');

                if (name === 'latestQualificationIndex') {
                    input.value = idx;
                }
                else if (name) {
                    input.setAttribute('name', name.replace(/\[\d+\]/, `[${idx}]`));
                }
            });
            container.appendChild(newRow);
        });

        removeButton.addEventListener('click', function() {
            const rows = container.querySelectorAll('.record-row');
            if (rows.length > 2) rows[rows.length - 1].remove();
            else Swal.fire('Cannot Remove', 'Class X and XII are mandatory.', 'warning');
        });

        const form = document.getElementById('academic-details-form');
        if (form) form.addEventListener('submit', handleAjaxFormSubmit);
    }

    function initializeDocumentUploadForm() {
        // Initialize uploaded-document preview buttons (backward compatibility)
        document.querySelectorAll('.preview-btn').forEach(button => {
            button.addEventListener('click', function() {
                const fn = this.dataset.filename;
                const on = this.dataset.originalFilename;
                if (fn && on) previewUploadedDocument(fn, on);
            });
        });

        document.querySelectorAll('form.document-upload-form').forEach(form => {
            const uploadArea = form.querySelector('.upload-area');
            const stagingArea = form.querySelector('.staging-area');
            const fileInput = form.querySelector('input[type="file"]');
            if (!uploadArea || !stagingArea || !fileInput) return;

            // Show staging on file select
            fileInput.addEventListener('change', function() {
                if (fileInput.files.length > 0) {
                    uploadArea.classList.add('d-none');
                    stagingArea.classList.remove('d-none');
                    const display = stagingArea.querySelector('.file-name-display');
                    if (display) display.textContent = fileInput.files[0].name;
                }
            });

            // Cancel staging
            const cancelBtn = stagingArea.querySelector('.cancel-staged-btn');
            if (cancelBtn) {
                cancelBtn.addEventListener('click', function() {
                    fileInput.value = '';
                    stagingArea.classList.add('d-none');
                    uploadArea.classList.remove('d-none');
                });
            }

            // Form submit
            form.addEventListener('submit', handleDocumentUploadSubmit);
        });
    }

    function initializeProgrammeSelectionForm() {
        const form = document.getElementById('programme-selection-form');
        if (!form) return;
        const instituteSelect = document.getElementById('institute-select');
        const programmesContainer = document.getElementById('programmes-container');
        const applicationId = form.querySelector('input[name="applicationId"]').value;

        instituteSelect.addEventListener('change', () => {
            const inst = instituteSelect.value;
            if (!inst) {
                programmesContainer.innerHTML =
                  '<div class="form-text">Select an institute to load programmes.</div>';
                return;
            }
            programmesContainer.innerHTML = '<div class="form-text">Loading programmes...</div>';
            axios.get('/applicants/programmes-by-institute', {
                params: { instituteId: inst, applicationId }
            })
            .then(res => programmesContainer.innerHTML = res.data)
            .catch(err => {
                programmesContainer.innerHTML =
                  '<div class="form-text text-danger">Failed to load programmes.</div>';
                console.error(err);
            });
        });

        form.addEventListener('submit', e => {
            e.preventDefault();
            const selected = Array.from(
                form.querySelectorAll('input[name="programmeIds"]:checked')
            ).map(cb => cb.value);
            if (!instituteSelect.value) {
                Swal.fire('Incomplete', 'Select an institute.', 'warning');
                return;
            }
            if (!selected.length) {
                Swal.fire('Incomplete', 'Select at least one programme.', 'warning');
                return;
            }
            const payload = {
                applicationId: applicationId.toString(),
                instituteId: instituteSelect.value.toString(),
                programmeIds: selected
            };
            const btn = form.querySelector('button[type="submit"]');
            const orig = btn.innerHTML;
            btn.disabled = true;
            btn.textContent = 'Saving...';
            axios.post('/applicants/programmes/save', payload)
            .then(res => Swal.fire({
                icon: 'success',
                title: 'Success!',
                text: res.data.message || 'Saved.',
                timer: 2000,
                showConfirmButton: false
            }))
            .catch(err => {
                console.error('Save error:', err);
                Swal.fire('Save Failed', 'Error saving programmes.', 'error');
            })
            .finally(() => {
                btn.disabled = false;
                btn.innerHTML = orig;
            });
        });
    }


  
    //   Helper & Utility Functions 
    
    function handleAjaxFormSubmit(event) {
        event.preventDefault();
        const form = event.currentTarget;
        const btn = form.querySelector('button[type="submit"]');
        const orig = btn.innerHTML;
        btn.disabled = true;
        btn.innerHTML = '<span class="spinner-border spinner-border-sm"></span> Saving...';
        axios.post(form.action, new FormData(form))
            .then(() => Swal.fire({
                icon: 'success', title: 'Success!', text: 'Saved.', timer: 2000,
                showConfirmButton: false
            }))
            .catch(err => {
                console.error('Form error:', err);
                Swal.fire('Error', 'Could not save. Try again.', 'error');
            })
            .finally(() => {
                btn.disabled = false;
                btn.innerHTML = orig;
            });
    }

    function handleDocumentUploadSubmit(event) {
        event.preventDefault();
        const form = event.currentTarget;
        const btn = form.querySelector('button[type="submit"]');
        const orig = btn.innerHTML;
        btn.disabled = true;
        btn.innerHTML = '<span class="spinner-border spinner-border-sm"></span> Uploading...';
        axios.post(form.action, new FormData(form), {
            headers: { 'Content-Type': 'multipart/form-data' }
        })
        .then(res => Swal.fire({
            icon: 'success', title: 'Success!', text: res.data.message,
            timer: 1500, showConfirmButton: false
        }).then(() => {
            const activeLink = document.querySelector('.sidebar .nav-link.active');
            if (activeLink) loadFragment(activeLink.getAttribute('data-url'), activeLink);
        }))
        .catch(err => {
            const msg = err.response?.data?.message || 'Upload failed!';
            Swal.fire('Upload Failed', msg, 'error');
        })
        .finally(() => {
            btn.disabled = false;
            btn.innerHTML = orig;
        });
    }

    // Preview for documents already saved in the database
    function previewUploadedDocument(documentId, originalName) {
        const modalEl = document.getElementById('previewModal');
        if (!modalEl) return console.error('Preview modal not found');

        const modal = bootstrap.Modal.getInstance(modalEl) || new bootstrap.Modal(modalEl);
        const title = document.getElementById('previewModalLabel');
        const body = document.getElementById('previewModalBody');

        if (title) title.textContent = `Preview: ${originalName}`;

        // This makes the modal body scrollable
        body.style.overflow = 'auto';

        const url = `/applicants/documents/${documentId}`;
        const ext = originalName.split('.').pop().toLowerCase();

        if (['jpg','jpeg','png'].includes(ext)) {
            // This keeps the image sharp and contained
            body.innerHTML = `<img src="${url}" style="width: 100%; height: auto;" alt="Preview of ${originalName}">`;
        } else { // Handles PDFs or any other non-image type
            body.style.overflow = 'hidden';
            body.innerHTML = `<div class="text-center py-5">
                <i class="bi bi-file-earmark-text" style="font-size:4rem; color:#6c757d;"></i>
                <h5 class="mt-3">${originalName}</h5>
                <p class="text-muted">No preview available for this file type.</p>
            </div>`;
        }

        modal.show();
    }

    // Preview for local files the user has just selected (before uploading)
    function previewFile(file) {
        if (!file || !(file instanceof File)) return;

        const modalEl = document.getElementById('previewModal');
        if (!modalEl) return;

        const modal = bootstrap.Modal.getInstance(modalEl) || new bootstrap.Modal(modalEl);
        const title = document.getElementById('previewModalLabel');
        const body = document.getElementById('previewModalBody');
        const fileName = file.name;
        const fileType = file.type;

        if (title) title.textContent = `Preview: ${fileName}`;
        body.innerHTML = '';

        // This makes the modal body scrollable
        body.style.overflow = 'auto';

        if (fileType.startsWith('image/')) {
            const reader = new FileReader();
            reader.onload = e => {
                // This keeps the image sharp and contained
                body.innerHTML = `<img src="${e.target.result}" style="width: 100%; height: auto;" alt="Preview">`;
            };
            reader.readAsDataURL(file);
        } else {
            body.style.overflow = 'hidden';
            body.innerHTML = `<div class="text-center py-4">
                <i class="bi bi-file-earmark-text" style="font-size:3rem;"></i>
                <h5>${fileName}</h5>
                <p>This file cannot be previewed.</p>
            </div>`;
        }
        modal.show();
    }


    //  Core Logic & Event Listeners                      
    
    const loadFragment = (url, linkElement) => {
        const activeId = sessionStorage.getItem('activeApplicationId');
        if (!url.includes('application-selection') &&
            !url.includes('personal-details') &&
            !activeId) {
            Swal.fire('No Application Selected',
                      'Please select an application first.', 'warning');
            return;
        }
        const ctxUrl = activeId ? `${url}?applicationId=${activeId}` : url;
        contentArea.innerHTML =
          `<div class="d-flex justify-content-center p-5">
             <div class="spinner-border text-primary"></div>
           </div>`;
        axios.get(ctxUrl)
            .then(res => {
                contentArea.innerHTML = res.data;
                document.querySelectorAll('.sidebar .nav-link')
                        .forEach(l => l.classList.remove('active'));
                if (linkElement) linkElement.classList.add('active');
                if (url.includes('application-selection')) initializeDashboard();
                else if (url.includes('personal-details'))
                    initializePersonalDetailsForm();
                else if (url.includes('academic-details'))
                    initializeDynamicAcademicForm();
                else if (url.includes('document-upload'))
                    initializeDocumentUploadForm();
                else if (url.includes('programme-selection'))
                    initializeProgrammeSelectionForm();
            })
            .catch(err => {
                console.error('Load error:', err);
                contentArea.innerHTML =
                  '<div class="alert alert-danger">Failed to load content.</div>';
            });
    };

    document.querySelectorAll('.sidebar .nav-link.nav-loader')
        .forEach(link => {
            link.addEventListener('click', e => {
                e.preventDefault();
                loadFragment(link.getAttribute('data-url'), link);
            });
        });

    if (sidebarToggle) {
        sidebarToggle.addEventListener('click', () => sidebar.classList.toggle('show'));
    }

    const defaultLink = document.querySelector('.nav-link[data-url*="application-selection"]');
    if (defaultLink) loadFragment(defaultLink.getAttribute('data-url'), defaultLink);
});



//    GLOBAL FUNCTIONS                          

function logout() {
    Swal.fire({
        title: 'Are you sure you want to logout?',
        icon: 'warning',
        showCancelButton: true,
        confirmButtonText: 'Yes, logout',
        cancelButtonText: 'Cancel'
    }).then(result => {
        if (result.isConfirmed) {
            const form = document.createElement('form');
            form.method = 'POST';
            form.action = '/logout';
            const csrfInput = document.createElement('input');
            csrfInput.type = 'hidden';
            csrfInput.name = '_csrf';
            csrfInput.value = document.querySelector('meta[name="_csrf"]').content;
            form.appendChild(csrfInput);
            document.body.appendChild(form);
            form.submit();
        }
    });
}

function finalSaveConfirmation() {
    Swal.fire({
        icon: 'info',
        title: 'Step Complete',
        text: 'Your uploaded documents have been saved.',
        confirmButtonText: 'Okay'
    });
}

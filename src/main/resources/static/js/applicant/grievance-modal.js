document.addEventListener('DOMContentLoaded', () => {
    const csrfToken  = document.querySelector('meta[name="_csrf"]')?.content || '';
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content || 'X-CSRF-TOKEN';

    const catSel      = document.getElementById('griev-category');
    const instWrapper = document.getElementById('griev-institute-wrapper');
    const instSel     = document.getElementById('griev-institute');
    const msgArea     = document.getElementById('griev-message');
    const wordCount   = document.getElementById('griev-word-count');
    const wordWarn    = document.getElementById('griev-word-warn');
    const submitBtn   = document.getElementById('griev-submit-btn');
    const cancelBtn   = document.getElementById('griev-cancel-btn');
    const doneBtn     = document.getElementById('griev-done-btn');
    const errBox      = document.getElementById('griev-error');
    const formArea    = document.getElementById('grievance-form-area');
    const successArea = document.getElementById('grievance-success');
    const ticketEl    = document.getElementById('grievance-ticket-code');

    let categoriesData = [];
    let institutesLoaded = false;

    // Initial Load: Fetch DB Categories securely
    fetch('/applicants/grievances/categories', { credentials: 'same-origin' })
        .then(r => r.json())
        .then(data => {
            categoriesData = data;
            catSel.innerHTML = '<option value="">-- Select a category --</option>';
            data.forEach(c => {
                const opt = document.createElement('option');
                opt.value = c.code;
                opt.textContent = c.name;
                catSel.appendChild(opt);
            });
        }).catch(err => console.error("Failed to fetch categories", err));

    const modalEl = document.getElementById('grievanceModal');
    if (modalEl) {
        modalEl.addEventListener('show.bs.modal', () => {
            // Reset form fields
            catSel.value = '';
            instSel.innerHTML = '<option value="">-- Select Institute --</option>';
            instWrapper.classList.add('d-none');
            msgArea.value = '';
            wordCount.textContent = '0 words';
            wordWarn.classList.add('d-none');
            errBox.classList.add('d-none');
            formArea.classList.remove('d-none');
            successArea.classList.add('d-none');
            institutesLoaded = false;

            // FIX: Restore the Submit & Cancel buttons, hide the Done button
            submitBtn.classList.remove('d-none');
            cancelBtn.classList.remove('d-none');
            doneBtn.classList.add('d-none');
            submitBtn.disabled = false;
        });
    }

    if(catSel) {
        catSel.addEventListener('change', function () {
            const selectedCat = categoriesData.find(c => c.code === this.value);
            if (selectedCat && selectedCat.requiresInstitute) {
                instWrapper.classList.remove('d-none');
                if (!institutesLoaded) loadInstitutes();
            } else {
                instWrapper.classList.add('d-none');
            }
        });
    }

    if(msgArea) {
        msgArea.addEventListener('input', () => {
            const words = msgArea.value.trim().split(/\s+/).filter(Boolean).length;
            wordCount.textContent = words + ' word' + (words !== 1 ? 's' : '');
            const over = words > 120;
            wordWarn.classList.toggle('d-none', !over);
            submitBtn.disabled = over;
        });
    }

    function loadInstitutes() {
        fetch('/applicants/grievances/institutes', { credentials: 'same-origin' })
            .then(r => r.json())
            .then(list => {
                list.forEach(i => {
                    const opt = document.createElement('option');
                    opt.value = i.id;
                    opt.textContent = i.name;
                    instSel.appendChild(opt);
                });
                institutesLoaded = true;
            })
            .catch(() => {
                instSel.innerHTML = '<option value="">Failed to load institutes</option>';
            });
    }

    if (submitBtn) {
        submitBtn.addEventListener('click', () => {
            errBox.classList.add('d-none');
            const categoryCode = catSel.value.trim();
            const message  = msgArea.value.trim();
            const instId   = instSel.value;

            const selectedCat = categoriesData.find(c => c.code === categoryCode);

            if (!categoryCode) { showErr('Please select a category.'); return; }
            if (!message)  { showErr('Please enter a message.'); return; }
            if (selectedCat && selectedCat.requiresInstitute && !instId) {
                showErr('Please select the concerned institute.'); return;
            }

            const words = message.split(/\s+/).filter(Boolean).length;
            if (words > 120) { showErr('Message exceeds ~100 words. Please shorten it.'); return; }

            submitBtn.disabled = true;
            submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span>Submitting...';

            const payload = { categoryCode, message };
            if (selectedCat && selectedCat.requiresInstitute) payload.instituteId = instId;

            fetch('/applicants/grievances/submit', {
                method: 'POST',
                credentials: 'same-origin',
                headers: {
                    'Content-Type': 'application/json',
                    [csrfHeader]: csrfToken
                },
                body: JSON.stringify(payload)
            })
            .then(r => r.json())
            .then(data => {
                if (data.error) { showErr(data.error); return; }
                ticketEl.textContent = data.ticketCode;
                formArea.classList.add('d-none');
                successArea.classList.remove('d-none');

                // FIX: Hide Submit & Cancel buttons, reveal the Done button
                submitBtn.classList.add('d-none');
                cancelBtn.classList.add('d-none');
                doneBtn.classList.remove('d-none');
            })
            .catch(() => showErr('Network error. Please try again.'))
            .finally(() => {
                submitBtn.innerHTML = '<i class="bi bi-send me-1"></i>Submit Grievance';
            });
        });
    }

    function showErr(msg) {
        errBox.textContent = msg;
        errBox.classList.remove('d-none');
    }

    // ─────────────────────────────────────────────────────────────────
    //  Applicant: View My Grievances (Dynamic Loading & ARIA Fix)
    // ─────────────────────────────────────────────────────────────────
    const myGrievBtn = document.getElementById('btn-view-my-grievances');
    if (myGrievBtn) {
        myGrievBtn.addEventListener('click', function () {
            // Remove focus from the button immediately to satisfy ARIA rules
            this.blur();

            const mainContent = document.getElementById('main-content-area');
            mainContent.innerHTML = '<div class="text-center mt-5"><div class="spinner-border text-primary"></div></div>';

            // Deselect active sidebar items for UI clarity
            document.querySelectorAll('.sidebar-menu .nav-link').forEach(l => l.classList.remove('active'));

            axios.get('/applicants/fragments/my-grievances')
                .then(res => {
                    mainContent.innerHTML = res.data;

                    // Shift browser focus to the newly loaded content for screen readers
                    mainContent.setAttribute('tabindex', '-1');
                    mainContent.focus();
                    // Clean up the outline so it doesn't look ugly for mouse users
                    mainContent.style.outline = 'none';
                })
                .catch(err => {
                    mainContent.innerHTML = '<div class="alert alert-danger m-3">Failed to load grievances.</div>';
                });
        });
    }
});
import { showSuccess, showError, showLoading, hideLoading, showConfirm } from './utils.js'; // CSP FIX: Added showConfirm
import { updateSidebarState } from './applicant.js';

export function initializeSeatAllotmentPage() {
    const acceptBtn = document.getElementById('accept-allotment-btn');
    const rejectBtn = document.getElementById('reject-allotment-btn');
    const slideUpBtn = document.getElementById('slide-up-allotment-btn');

    if (acceptBtn) acceptBtn.onclick = handleAcceptance;
    if (rejectBtn) rejectBtn.onclick = handleRejection;
    // slide-up-allotment-btn is now inside #slideUpInfoModal footer;
    // it triggers after the user reads the info and clicks confirm
    if (slideUpBtn) slideUpBtn.onclick = handleSlideUp;

    const subjectForm = document.getElementById('subjectPreferenceForm');
    if (subjectForm) {
        initializeSubjectSelection(subjectForm);
    }
}

function getContainerData() {
    const container = document.querySelector('[data-allotment-id]');
    return {
        allotmentId: container?.dataset.allotmentId,
        programmeOfferedId: container?.dataset.programmeOfferedId
    };
}

async function handleAcceptance() {
    const { allotmentId, programmeOfferedId } = getContainerData();
    if (!allotmentId) return showError('Could not identify the allotment. Please refresh.');
    if (!programmeOfferedId) return showError('Could not identify the programme. Please refresh.');

    // CSP FIX: Replaced Swal.fire with native showConfirm
    const result = await showConfirm(
        'Accept This Seat?',
        'You will be shown the seat acceptance fee. Completing payment will confirm your admission.',
        'Yes, View Fee & Accept',
        'btn-success'
    );

    if (result.isConfirmed) {
        window.showSeatFeeModal(allotmentId, programmeOfferedId, false);
    }
}

async function handleRejection() {
    const { allotmentId } = getContainerData();
    if (!allotmentId) return showError('Could not identify the allotment.');

    // CSP FIX: Replaced Swal.fire with native showConfirm
    const result = await showConfirm(
        'Reject This Offer?',
        'You will <strong>permanently lose</strong> this seat. You may be considered for other preferences later.',
        'Yes, Reject',
        'btn-danger'
    );

    if (result.isConfirmed) {
        showLoading("Processing rejection...");
        try {
            await axios.post('/api/applicants/counseling/reject', { allotmentId });
            await showSuccess("Offer Rejected.");

            await refreshSidebarCounseling();
            reloadCurrentFragment();
        } catch (err) {
            hideLoading();
            showError(err.response?.data?.message || 'Failed to reject seat.');
        }
    }
}

function handleSlideUp() {
    // This is called from the "Yes, Slide Up & Pay Fee" button inside #slideUpInfoModal.
    // The modal is already dismissed via data-bs-dismiss="modal" on the button,
    // so we just need to open the fee modal for the slide-up payment.
    const { allotmentId, programmeOfferedId } = getContainerData();
    if (!allotmentId) return showError('Could not identify the allotment. Please refresh.');
    if (!programmeOfferedId) return showError('Could not identify the programme. Please refresh.');

    window.showSeatFeeModal(allotmentId, programmeOfferedId, true);
}

function reloadCurrentFragment() {
    const activeLink = document.querySelector('.nav-link.active.nav-loader');
    if (activeLink) activeLink.click();
    else window.location.reload();
}

export async function refreshSidebarCounseling() {
    try {
        const response = await axios.get('/api/applicants/counseling/overviews');
        const rounds = response.data;
        const menuContainer = document.getElementById('counseling-menu-item');
        const roundsList = document.getElementById('counseling-rounds-list');

        if (!menuContainer || !roundsList) return;

        if (rounds && rounds.length > 0) {
            roundsList.innerHTML = '';

            rounds.forEach(round => {
                const resultUrl = `/applicants/fragments/counseling/result?admissionWindowId=${round.admissionWindowId}`;
                let iconClass = 'bi-hourglass-split text-muted';
                let isLinkDisabled = !round.status || round.status === 'UPCOMING';

                switch (round.status) {
                    case 'ACCEPTED': iconClass = 'bi-check-circle-fill text-success'; break;
                    case 'REJECTED': iconClass = 'bi-x-circle-fill text-danger'; break;
                    case 'SLIDE_UP': iconClass = 'bi-arrow-up-circle-fill text-warning'; break;
                    case 'PENDING': iconClass = 'bi-arrow-right-circle-fill text-warning'; break;
                    case 'PENDING_VERIFICATION': iconClass = 'bi-info-circle-fill text-info'; break;
                    case 'NOT_ALLOTTED': iconClass = 'bi-info-circle-fill text-muted'; break;
                    case 'INSTITUTE_REJECTED': iconClass = 'bi-exclamation-octagon-fill text-danger'; break;
                }

                roundsList.insertAdjacentHTML('beforeend', `
                    <li class="nav-item">
                        <a class="nav-link nav-loader ${isLinkDisabled ? 'disabled' : ''}" href="#" data-url="${resultUrl}" ${isLinkDisabled ? 'aria-disabled="true"' : ''}>
                            <i class="bi ${iconClass}"></i>
                            <span>${escapeHtml(round.stepName)}</span>
                        </a>
                    </li>
                `);

                if ((round.status === 'ACCEPTED' || round.status === 'SLIDE_UP') && round.allotmentId) {
                    const paymentUrl = `/applicants/fragments/counseling/payment-page?allotmentId=${round.allotmentId}`;
                    roundsList.insertAdjacentHTML('beforeend', `
                        <li class="nav-item ps-3">
                            <a class="nav-link nav-loader" href="#" data-url="${paymentUrl}">
                                <i class="bi bi-credit-card-fill text-primary"></i>
                                <span>${round.status === 'SLIDE_UP' ? 'Pay Slide Up Fee' : 'Pay Admission Fee'}</span>
                            </a>
                        </li>
                    `);
                }
            });

            menuContainer.classList.remove('d-none');
        } else {
            menuContainer.classList.add('d-none');
        }
    } catch (error) {
        const menuContainer = document.getElementById('counseling-menu-item');
        if (menuContainer) menuContainer.classList.add('d-none');
    }
}

function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

/**
 * Opens the seat acceptance / slide-up fee modal and fetches the fee breakdown.
 * Called from seat-allotment-result.html via handleAcceptance() and handleSlideUp().
 * @param {string|number} allotmentId
 * @param {string|number} programmeOfferedId
 * @param {boolean} isSlideUp
 */
window.showSeatFeeModal = function (allotmentId, programmeOfferedId, isSlideUp) {
    isSlideUp = isSlideUp || false;

    // Populate CSRF token from meta tag set in parent dashboard.html
    var csrfMeta = document.querySelector('meta[name="_csrf"]');
    var csrfInput = document.getElementById('seatFeeCsrfParam');
    if (csrfMeta && csrfInput) csrfInput.value = csrfMeta.content;

    document.getElementById('seatFeeAllotmentId').value = allotmentId;
    document.getElementById('seatFeeIsSlideUp').value = isSlideUp;

    var body = document.getElementById('seatFeeModalBody');
    var payBtn = document.getElementById('seatFeePayBtn');
    var modalTitle = document.getElementById('seatFeeModalLabel');

    body.innerHTML = '<p class="text-center"><span class="spinner-border spinner-border-sm"></span> Loading fee details...</p>';
    payBtn.disabled = true;
    modalTitle.innerHTML = isSlideUp
        ? '<i class="bi bi-arrow-up-circle me-2"></i>Slide Up Fee'
        : '<i class="bi bi-cash-coin me-2"></i>Seat Acceptance Fee';

    var modal = new bootstrap.Modal(document.getElementById('seatFeeModal'));
    modal.show();

    fetch('/applicants/payment/seat-fee-structure/' + encodeURIComponent(programmeOfferedId), {
        credentials: 'same-origin'
    })
        .then(function (r) { if (!r.ok) throw new Error(); return r.json(); })
        .then(function (data) {
            if (!data || !data.particulars || data.particulars.length === 0) {
                body.innerHTML =
                    '<div class="alert alert-warning mb-0">' +
                    '<i class="bi bi-exclamation-triangle me-2"></i>' +
                    'No fee structure has been configured for this programme. Please contact the institute.' +
                    '</div>';
                payBtn.disabled = true;
                return;
            }

            var div = document.createElement('div');
            var rows = data.particulars.map(function (p) {
                div.textContent = p.particularName;
                var safe = div.innerHTML;
                return '<tr><td>' + safe + '</td>' +
                    '<td class="text-end">&#x20b9; ' + parseFloat(p.amount).toFixed(2) + '</td></tr>';
            }).join('');

            var note = isSlideUp
                ? 'This is your <strong>slide up fee</strong>. Paying this amount holds your current seat while keeping you eligible for higher-preference seats in the next round.'
                : 'This is your <strong>seat acceptance fee</strong>, not the registration fee. Payment is required to confirm your allotted seat.';

            body.innerHTML =
                '<p class="text-muted small mb-2">Review the fee breakdown before proceeding to payment.</p>' +
                '<table class="table table-bordered table-sm mb-2">' +
                '<thead class="table-light"><tr><th>Particulars</th><th class="text-end">Amount</th></tr></thead>' +
                '<tbody>' + rows + '</tbody>' +
                '<tfoot><tr class="fw-bold table-success">' +
                '<td>Total</td>' +
                '<td class="text-end">&#x20b9; ' + parseFloat(data.totalAmount).toFixed(2) + '</td>' +
                '</tr></tfoot>' +
                '</table>' +
                '<div class="alert alert-info py-2 mb-0 small">' +
                '<i class="bi bi-info-circle me-1"></i>' + note +
                '</div>';

            payBtn.disabled = false;
        })
        .catch(function () {
            body.innerHTML =
                '<div class="alert alert-danger mb-0">' +
                '<i class="bi bi-x-circle me-2"></i>' +
                'Could not load fee details. Please try again or contact support.' +
                '</div>';
            payBtn.disabled = true;
        });
};

function initializeSubjectSelection(form) {
    const programmeOfferedId = document.getElementById('programmeOfferedId').value;
    const seatAllotmentId = document.getElementById('seatAllotmentId').value;
    const fixedShift = document.getElementById('fixedShift').value || 'DAY';

    const subjectPoolsContainer = document.getElementById('subjectPoolsContainer');
    const confirmBtn = document.getElementById('confirmSubjectsBtn');

    let choicesInstances = {};
    let currentShiftSubjects = {};
    if(confirmBtn) confirmBtn.disabled = true;

    async function loadInitialState() {
        try {
            const response = await axios.get(`/api/applicants/counseling/get-preferences/${seatAllotmentId}`);
            const savedData = response.data;
            await fetchAndPopulateSubjects(fixedShift, savedData?.preferences);

            if (savedData && savedData.preferences && Object.keys(savedData.preferences).length > 0) {
                confirmBtn.innerHTML = '<i class="bi bi-save2-fill me-2"></i>Update Preferences';
            }
        } catch (error) {
            await fetchAndPopulateSubjects(fixedShift, null);
        }
    }

    async function fetchAndPopulateSubjects(shift, savedPreferences) {
        // CSP FIX: Removed inline style (width/height), used Bootstrap padding class 'p-3'
        subjectPoolsContainer.innerHTML = `
            <div class="text-center py-5">
                <div class="spinner-border text-primary border-3 p-3"></div>
                <p class="mt-3 fw-bold text-muted">Fetching available subjects...</p>
            </div>`;
        confirmBtn.disabled = true;

        try {
            const response = await axios.get(`/api/combinations/for-applicant/${programmeOfferedId}?shift=${shift}`);
            currentShiftSubjects = response.data;
            populateSubjectDropdowns(currentShiftSubjects, savedPreferences);
        } catch (error) {
            console.error('Failed to load subject pools:', error);
            subjectPoolsContainer.innerHTML = '<div class="alert alert-danger shadow-sm"><i class="bi bi-exclamation-triangle-fill me-2"></i> Could not load subjects from the server. Please try refreshing the page.</div>';
        }
    }

    function populateSubjectDropdowns(pools, savedPreferences = null) {
        subjectPoolsContainer.innerHTML = '';
        choicesInstances = {};

        const displayOrder = ['MINOR', 'MULTIDISCIPLINARY', 'SKILL_ENHANCEMENT', 'ABILITY_ENHANCEMENT', 'VALUE_ADDED'];
        const subjectTypeMap = {
            MINOR: 'Minor Subjects', MULTIDISCIPLINARY: 'Multidisciplinary (MDC)',
            ABILITY_ENHANCEMENT: 'Ability Enhancement (AEC)', SKILL_ENHANCEMENT: 'Skill Enhancement (SEC)',
            VALUE_ADDED: 'Value Added (VAC)'
        };

        let hasSubjects = false;

        displayOrder.forEach(type => {
            const subjects = pools[type];
            if (subjects && subjects.length > 0) {
                hasSubjects = true;
                const group = document.createElement('div');
                group.className = 'mb-4 border rounded p-3 bg-white shadow-sm';
                group.innerHTML = `<h5 class="h6 fw-bold text-dark border-bottom pb-2 mb-3">${subjectTypeMap[type]}</h5>`;

                if (type === 'MULTIDISCIPLINARY') {
                    group.innerHTML += `<div class="alert alert-info small py-2 d-flex align-items-start mb-3"><i class="bi bi-info-circle-fill me-2 mt-1"></i> <div><strong>Note:</strong> To be chosen from disciplines other than Major. Students are not allowed to choose an MD course already undergone at the higher secondary level.</div></div>`;
                }

                const select = document.createElement('select');
                select.id = `select-${type}`;
                select.multiple = true;
                select.className = "form-select";
                group.appendChild(select);

                subjectPoolsContainer.appendChild(group);

                const maxItems = Math.min(3, subjects.length);
                const choices = new Choices(select, {
                    removeItemButton: true,
                    maxItemCount: maxItems,
                    placeholder: true,
                    placeholderValue: `Click to select up to ${maxItems} preferences`,
                    classNames: { containerOuter: 'choices border-0' }
                });

                const options = subjects.map(s => ({ value: s.subjectName, label: s.subjectName }));
                choices.setChoices(options, 'value', 'label', false);

                if (savedPreferences && savedPreferences[type]) {
                    const idToNameMap = new Map(subjects.map(s => [s.subjectId, s.subjectName]));
                    const savedNames = savedPreferences[type].map(id => idToNameMap.get(id)).filter(name => name);
                    savedNames.forEach(name => choices.setChoiceByValue(name));
                }

                choicesInstances[type] = choices;
            }
        });

        if (!hasSubjects) {
            subjectPoolsContainer.innerHTML = '<div class="alert alert-warning shadow-sm"><i class="bi bi-exclamation-circle-fill me-2"></i> No subjects have been made available for this shift by the institute.</div>';
            confirmBtn.disabled = true;
        } else {
            confirmBtn.disabled = false;
        }
    }

 	form.addEventListener('submit', async (e) => {
        e.preventDefault();

        const originalContent = confirmBtn.cloneNode(true);

        confirmBtn.disabled = true;
        confirmBtn.replaceChildren();

        const spinner = document.createElement("span");
        spinner.className = "spinner-border spinner-border-sm me-2";

        confirmBtn.appendChild(spinner);
        confirmBtn.appendChild(document.createTextNode(" Processing..."));

        const preferencesPayload = {};
        const masterNameToIdMap = new Map();

        Object.values(currentShiftSubjects)
            .flat()
            .forEach(s => masterNameToIdMap.set(s.subjectName, s.subjectId));

        for (const [type, choices] of Object.entries(choicesInstances)) {
            const selectedNames = choices.getValue(true);

            if (selectedNames && selectedNames.length > 0) {
                preferencesPayload[type] = selectedNames
                    .map(name => masterNameToIdMap.get(name))
                    .filter(id => id);
            }
        }

        const finalPayload = {
            seatAllotmentId: parseInt(seatAllotmentId),
            chosenShift: fixedShift,
            preferences: preferencesPayload
        };

        try {
            await axios.post('/api/applicants/counseling/save-combination-preferences', finalPayload);
            await showSuccess('Your subject preferences have been saved successfully!');

            confirmBtn.replaceChildren();
            const icon = document.createElement("i");
            icon.className = "bi bi-save2-fill me-2";
            confirmBtn.appendChild(icon);
            confirmBtn.appendChild(document.createTextNode("Update Preferences"));

        } catch (error) {
            console.error('Failed to save preferences:', error);
            showError(
                error.response?.data?.message ||
                'An unknown error occurred while saving. Please try again.'
            );
            confirmBtn.replaceChildren(...originalContent.childNodes);

        } finally {
            confirmBtn.disabled = false;
        }
    });

    loadInitialState();
}

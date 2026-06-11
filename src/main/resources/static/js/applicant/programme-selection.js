import { showSuccess, showError, showLoading, hideLoading } from './utils.js';
import { updateSidebarState } from './applicant.js';

export function initializeProgrammeSelection() {
    const form = document.getElementById('master-preference-form');
    if (!form) return;

    const wrapper = document.getElementById('preference-app-wrapper');
    const applicationId = wrapper.dataset.applicationId;
    const isFormLocked = wrapper.dataset.formLocked === 'true';
    const isAllStreams = wrapper.dataset.isAllStreams === 'true';
    const programmeLevel = wrapper.dataset.programmeLevel || 'FYUG';

    const streamSelect = document.getElementById('pref-stream');
    const progSelect = document.getElementById('pref-programme');
    const instSelect = document.getElementById('pref-institute');
    const shiftSelect = document.getElementById('pref-shift');
    const btnAdd = document.getElementById('btn-add-preference');

    const container = document.getElementById('preferences-container');
    const emptyMessage = document.getElementById('empty-list-message');
    const rowTemplate = document.getElementById('row-template');
    const feeDisplay = document.getElementById('total-fee-display');
    const countBadge = document.getElementById('preference-count-badge');

    let currentPrefCount = 0;
    let currentOfferings = [];
    let cachedFlatFee = null; // THE FIX: Cache the flat fee so we don't spam the server

    const setupUI = () => {
        if (isFormLocked) {
            document.getElementById('locked-alert')?.classList.remove('d-none');
        } else {
            document.getElementById('selection-card')?.classList.remove('d-none');
            document.getElementById('action-header')?.classList.remove('d-none');
            document.getElementById('submit-section')?.classList.remove('d-none');
        }
    };

    streamSelect.addEventListener('change', async (e) => {
        const streamId = e.target.value;
        progSelect.innerHTML = '<option value="">-- Choose Programme --</option>';
        instSelect.innerHTML = '<option value="">-- Choose Prog. First --</option>';
        shiftSelect.innerHTML = '<option value="">-- Choose Inst. First --</option>';

        progSelect.disabled = true;
        instSelect.disabled = true;
        shiftSelect.disabled = true;
        btnAdd.disabled = true;
        currentOfferings = [];

        if (!streamId) return;

        progSelect.innerHTML = '<option value="">Loading...</option>';
        try {
            let programmes;
            if (isAllStreams) {
                const res = await axios.get(`/applicants/programmes/by-level/${programmeLevel}`);
                programmes = res.data.filter(p => String(p.streamId) === String(streamId));
            } else {
                const res = await axios.get(`/applicants/programmes/by-stream/${streamId}`);
                programmes = res.data;
            }
            progSelect.innerHTML = '<option value="">-- Choose Programme --</option>';
            programmes.forEach(p => progSelect.add(new Option(p.programmeName, p.programmeId)));
            progSelect.disabled = false;
        } catch (err) {
            showError("Failed to load programmes.");
            progSelect.innerHTML = '<option value="">Error</option>';
        }
    });

    progSelect.addEventListener('change', async (e) => {
        const progId = e.target.value;
        instSelect.innerHTML = '<option value="">-- Choose Institute --</option>';
        shiftSelect.innerHTML = '<option value="">-- Choose Inst. First --</option>';

        instSelect.disabled = true;
        shiftSelect.disabled = true;
        btnAdd.disabled = true;
        currentOfferings = [];

        if (!progId) return;

        instSelect.innerHTML = '<option value="">Loading...</option>';
        try {
            const res = await axios.get(`/applicants/institutes/by-programme/${progId}`);
            currentOfferings = res.data;

            instSelect.innerHTML = '<option value="">-- Choose Institute --</option>';

            const uniqueInstitutes = new Map();
            currentOfferings.forEach(offering => {
                if (!uniqueInstitutes.has(offering.instituteId)) {
                    uniqueInstitutes.set(offering.instituteId, offering.instituteName);
                    instSelect.add(new Option(offering.instituteName, offering.instituteId));
                }
            });

            instSelect.disabled = false;
        } catch (err) {
            showError("Failed to load institutes.");
            instSelect.innerHTML = '<option value="">Error</option>';
        }
    });

    instSelect.addEventListener('change', (e) => {
        const instId = parseInt(e.target.value, 10);
        shiftSelect.innerHTML = '<option value="">-- Choose Shift --</option>';
        shiftSelect.disabled = true;
        btnAdd.disabled = true;

        if (!instId) return;

        const shiftsForInst = currentOfferings.filter(o => o.instituteId === instId);

        shiftsForInst.forEach(offering => {
            const shiftName = offering.shiftDisplayName && offering.shiftDisplayName !== 'Not Applicable'
                ? offering.shiftDisplayName : 'Day';

            shiftSelect.add(new Option(shiftName, offering.programmeOfferedId));
        });

        shiftSelect.disabled = false;
    });

    shiftSelect.addEventListener('change', (e) => {
        btnAdd.disabled = !e.target.value;
    });

    btnAdd.addEventListener('click', () => {
        if (currentPrefCount >= 30) {
            return showError("You can only add a maximum of 30 preferences.");
        }

        const streamId = streamSelect.value;
        const streamText = streamSelect.options[streamSelect.selectedIndex].text;

        const progId = progSelect.value;
        const progText = progSelect.options[progSelect.selectedIndex].text;

        const offeredId = shiftSelect.value;
        const instId = instSelect.value;
        const instText = instSelect.options[instSelect.selectedIndex].text;
        const shiftText = shiftSelect.options[shiftSelect.selectedIndex].text;

        const displayInstText = `${instText} (${shiftText})`;

        let isDuplicate = false;
        container.querySelectorAll('.preference-row').forEach(row => {
            if (row.querySelector('.input-offered').value === offeredId) {
                isDuplicate = true;
            }
        });

        if (isDuplicate) return showError("This Programme and Shift combination is already in your list.");

        addRowToDOM(streamId, streamText, progId, progText, offeredId, displayInstText, instId);

        streamSelect.value = '';
        streamSelect.dispatchEvent(new Event('change'));
        updateListState();
    });

    const addRowToDOM = (streamId, streamText, progId, progText, offeredId, instText, instId) => {
        const clone = rowTemplate.content.cloneNode(true);

        clone.querySelector('.stream-name-display').textContent = streamText;
        clone.querySelector('.stream-name-display').title = streamText;

        clone.querySelector('.prog-name-display').textContent = progText;
        clone.querySelector('.prog-name-display').title = progText;

        clone.querySelector('.inst-name-display').textContent = instText;
        clone.querySelector('.inst-name-display').title = instText;

        clone.querySelector('.input-stream').value = streamId || '';
        clone.querySelector('.input-prog').value = progId || '';
        clone.querySelector('.input-inst').value = instId || '';
        clone.querySelector('.input-offered').value = offeredId;

        if (isFormLocked) {
            clone.querySelector('.action-col').remove();
            const handle = clone.querySelector('.move-handle');
            handle.classList.add('pe-none');
            handle.title = "";
            handle.innerHTML = '<i class="bi bi-lock text-muted fs-5"></i>';
        }

        container.appendChild(clone);
        emptyMessage.classList.add('d-none');
    };

    container.addEventListener('click', (e) => {
        const btn = e.target.closest('.remove-preference-btn');
        if (btn) {
            btn.closest('.preference-group').remove();
            updateListState();
        }
    });

    function updateListState() {
        const rows = container.querySelectorAll('.preference-group');
        currentPrefCount = rows.length;
        countBadge.textContent = `${currentPrefCount} / 30 Added`;

        if (currentPrefCount === 0) {
            emptyMessage.classList.remove('d-none');
        } else {
            emptyMessage.classList.add('d-none');
        }

        rows.forEach((row, index) => {
            const rank = index + 1;
            row.querySelector('.preference-title').textContent = `#${rank}`;
            row.querySelector('.input-order').value = rank;

            row.querySelector('.input-offered').setAttribute('name', `preferences[${index}].programmeOfferedId`);
            row.querySelector('.input-order').setAttribute('name', `preferences[${index}].preferenceOrder`);
        });

        // Trigger dynamic frontend calculation immediately
        calculateTotalFee();
    }

    // THE FIX: Fetch the exact flat fee from the backend instead of multiplying
    async function calculateTotalFee() {
        if (currentPrefCount === 0) {
            feeDisplay.textContent = '₹ 0.00';
            return;
        }

        // If we already fetched the flat fee, use the cached version to prevent UI lag
        if (cachedFlatFee !== null) {
            feeDisplay.textContent = `₹ ${cachedFlatFee.toFixed(2)}`;
            return;
        }

        try {
            feeDisplay.textContent = 'Calculating...';
            const response = await axios.post('/applicants/fees/calculate-total', {
                applicationId: parseInt(applicationId, 10)
            });

            // Cache the response so future row additions are instant
            cachedFlatFee = parseFloat(response.data.totalFee);
            feeDisplay.textContent = `₹ ${cachedFlatFee.toFixed(2)}`;
        } catch (error) {
            console.error("Failed to fetch flat fee from backend", error);
            feeDisplay.textContent = 'Error';
        }
    }

    const initialLoad = async () => {
        setupUI();
        showLoading("Loading your data...");

        try {
            const prefRes = await axios.get(`/applicants/programmes/preferences/${applicationId}`);
            const savedPreferences = prefRes.data || [];

            container.innerHTML = '';

            if (savedPreferences.length > 0) {
                savedPreferences.sort((a, b) => a.preferenceOrder - b.preferenceOrder);

                savedPreferences.forEach(pref => {
                    let instDisplayName = pref.instituteName || 'Unknown Institute';
                    if (pref.shiftDisplayName && pref.shiftDisplayName !== 'Not Applicable') {
                        instDisplayName += ` (${pref.shiftDisplayName})`;
                    } else {
                        instDisplayName += ` (Day)`;
                    }

                    addRowToDOM(
                        pref.streamId,
                        pref.streamName || 'Stream',
                        pref.programmeId,
                        pref.programmeName,
                        pref.programmeOfferedId,
                        instDisplayName,
                        pref.instituteId
                    );
                });
            }

            updateListState();

            if (!isFormLocked && typeof window.Sortable !== 'undefined') {
                new window.Sortable(container, {
                    animation: 150,
                    handle: '.move-handle',
                    ghostClass: 'bg-light',
                    onEnd: updateListState
                });
            }

        } catch (error) {
            console.error("Initialization error:", error);
            showError("Failed to load your preferences.");
        } finally {
            hideLoading();
        }
    };

    form.addEventListener('submit', async (e) => {
        e.preventDefault();

        if (currentPrefCount === 0) {
            return showError("Please add at least one preference before continuing.");
        }

        const btn = document.getElementById('btn-save-preferences');
        const originalContent = btn.cloneNode(true);

        btn.disabled = true;
        btn.replaceChildren();

        const spinner = document.createElement("span");
        spinner.className = "spinner-border spinner-border-sm me-1";

        btn.appendChild(spinner);
        btn.appendChild(document.createTextNode(" Saving..."));

        const payload = { applicationId: applicationId, preferences: [] };

        container.querySelectorAll('.preference-group').forEach((row) => {
            payload.preferences.push({
                programmeOfferedId: parseInt(row.querySelector('.input-offered').value, 10),
                preferenceOrder: parseInt(row.querySelector('.input-order').value, 10)
            });
        });

        try {
            const response = await axios.post(form.action, payload);

            updateSidebarState(response.data);
            await showSuccess('Preferences Saved Successfully!');

            document.querySelector('.nav-link[data-url*="document-upload"]')?.click();

        } catch (error) {
            showError(error.response?.data?.error || "Failed to save preferences.");
        } finally {
            btn.disabled = false;
            btn.replaceChildren(...originalContent.childNodes);
        }
    });

    initialLoad();
}
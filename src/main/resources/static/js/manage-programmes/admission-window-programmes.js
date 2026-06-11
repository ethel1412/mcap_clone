const csrfToken = document
  .querySelector('meta[name="_csrf"]')
  ?.getAttribute('content');

// SecurityConfig CORS allows 'X-XSRF-TOKEN', so always use that header name
// regardless of what _csrf_header meta tag says (which defaults to X-CSRF-TOKEN).
if (csrfToken) {
  axios.defaults.headers.common['X-XSRF-TOKEN'] = csrfToken;
}
axios.defaults.withCredentials = true;

const mainEl = document.querySelector('main[data-admission-window-code]');
const admissionWindowCode = mainEl ? mainEl.getAttribute('data-admission-window-code') : null;
const admissionWindowId = mainEl ? mainEl.getAttribute('data-admission-window-id') : null;

// DOM elements
const programmesTableBody = document.getElementById('window-programmes-body');
const alertBox = document.getElementById('alert-box');
const noProgrammesBox = document.getElementById('no-programmes-box');
let submitModal; // Modal instance

// State variables
let programmeRows = [];
let scheduleStatus = {
  isActive: false,
  startDate: null,
  endDate: null,
  message: '',
  stepName: ''
};

// ==================== INITIALIZATION ====================

document.addEventListener('DOMContentLoaded', () => {
  submitModal = new bootstrap.Modal(document.getElementById('finalSubmitModal'));
  setupUpdateSeatMatrixModal();
  loadProgrammesForWindow();
  loadCuetPreference();

  // Attach Event Listeners securely (CSP Compliant)
  const saveBtn = document.getElementById('saveSeatBtn');
  if (saveBtn) saveBtn.addEventListener('click', saveSeatMatrix);

  const finalSubmitBtn = document.getElementById('finalSubmitBtn');
  if (finalSubmitBtn) finalSubmitBtn.addEventListener('click', initiateFinalSubmit);

  const confirmSubmitBtn = document.getElementById('confirmSubmitBtn');
  if (confirmSubmitBtn) confirmSubmitBtn.addEventListener('click', executeFinalSubmit);

  const selectAllCheckbox = document.getElementById('selectAllCheckbox');
  if (selectAllCheckbox) selectAllCheckbox.addEventListener('change', toggleAllCheckboxes);

  // CUET preference modal wiring
  const instituteCuetToggle = document.getElementById('instituteCuetToggle');
  const instituteCuetLabel = document.getElementById('instituteCuetLabel');
  if (instituteCuetToggle && instituteCuetLabel) {
    instituteCuetToggle.addEventListener('change', function () {
      updateCuetLabel(instituteCuetLabel, this.checked);
    });
  }

  const saveCuetPreferenceBtn = document.getElementById('saveCuetPreferenceBtn');
  if (saveCuetPreferenceBtn) saveCuetPreferenceBtn.addEventListener('click', saveCuetPreference);
});

// ==================== ALERT HELPER ====================

function showAlert(message, type = 'danger', autoHide = true, timeout = 4000) {
  if (!alertBox) return;
  alertBox.innerHTML = `
    <div class="alert alert-${type} alert-dismissible fade show shadow" role="alert">
      ${message}
      <button type="button" class="btn-close" aria-label="Close"></button>
    </div>
  `;
  alertBox.classList.remove('d-none');

  const closeBtn = alertBox.querySelector('.btn-close');
  if (closeBtn) closeBtn.onclick = () => alertBox.classList.add('d-none');
  if (autoHide) setTimeout(() => alertBox.classList.add('d-none'), timeout);
}

// ==================== SCHEDULE STATUS CHECK ====================

async function checkScheduleStatus() {
  if (!admissionWindowCode) return false;
  try {
    const url = `/manage-programmes-data/institute/admission-window/${admissionWindowCode}/schedule-status`;
    const response = await axios.get(url);
    scheduleStatus = response.data;
    return scheduleStatus.isActive;
  } catch (error) {
    scheduleStatus.isActive = false;
    scheduleStatus.message = 'Unable to verify schedule status';
    return false;
  }
}

// ==================== EMPTY STATE HELPER ====================

function updateEmptyState() {
  if (!noProgrammesBox) return;
  if (!programmeRows || programmeRows.length === 0) {
    noProgrammesBox.classList.remove('d-none');
  } else {
    noProgrammesBox.classList.add('d-none');
  }
}

// ==================== DYNAMIC SHIFT TABS ====================

function buildShiftTabs() {
    const table = document.getElementById("shiftTable");
    const tabContainer = document.getElementById("shiftTabsContainer");

    if (!table || !tabContainer) return;

    const rows = table.querySelectorAll("tbody tr.programme-row");

    if (rows.length === 0) {
        tabContainer.replaceChildren();
        return;
    }

    const shifts = new Map();
    rows.forEach(row => {
        const shiftCode = row.getAttribute("data-shift") || "NA";
        const shiftName = row.getAttribute("data-shift-name") || "Not Applicable";
        shifts.set(shiftCode, shiftName);
    });

    if (shifts.size <= 1) {
        tabContainer.replaceChildren();
        rows.forEach(row => row.classList.remove('d-none'));
        return;
    }

    // Create UL
    tabContainer.replaceChildren();
    const ul = document.createElement("ul");
    ul.className = "nav nav-tabs mb-3";
    ul.id = "shiftNavTabs";
    ul.setAttribute("role", "tablist");

    shifts.forEach((name, code) => {
        const li = document.createElement("li");
        li.className = "nav-item";
        li.setAttribute("role", "presentation");

        const button = document.createElement("button");
        button.className = "nav-link fw-bold";
        button.setAttribute("data-bs-toggle", "tab");
        button.setAttribute("data-target-shift", code);
        button.type = "button";
        button.setAttribute("role", "tab");
        button.textContent = name;

        li.appendChild(button);
        ul.appendChild(li);
    });

    tabContainer.appendChild(ul);

    const buttons = tabContainer.querySelectorAll("button.nav-link");

    const filterRows = (targetShift) => {
        rows.forEach(row => {
            if (row.getAttribute("data-shift") === targetShift) {
                row.classList.remove('d-none');
            } else {
                row.classList.add('d-none');
            }
        });
        updateSubmitButtonState();
    };

    buttons.forEach(btn => {
        btn.addEventListener("click", function () {
            buttons.forEach(b => b.classList.remove("active"));
            this.classList.add("active");
            filterRows(this.getAttribute("data-target-shift"));
        });
    });

    let defaultBtn = Array.from(buttons).find(
        b => b.getAttribute("data-target-shift") === "DAY"
    );

    if (!defaultBtn && buttons.length > 0) {
        defaultBtn = buttons[0];
    }

    if (defaultBtn) {
        defaultBtn.classList.add("active");
        filterRows(defaultBtn.getAttribute("data-target-shift"));
    }
}

// ==================== SECURITY ESCAPE ====================

function escapeHTML(str) {
    if (str === null || str === undefined) return '';
    return String(str)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

// ==================== CUET LABEL HELPER ====================

function updateCuetLabel(labelEl, isChecked) {
    if (!labelEl) return;
    if (isChecked) {
        labelEl.textContent = 'CUET';
        labelEl.className = 'form-check-label fw-semibold text-success ms-2';
    } else {
        labelEl.textContent = 'Non-CUET';
        labelEl.className = 'form-check-label fw-semibold text-secondary ms-2';
    }
}

// ==================== INSTITUTE CUET PREFERENCE ====================

async function loadCuetPreference() {
    if (!admissionWindowCode) return;
    try {
        const response = await axios.get(`/manage-programmes-data/institute/admission-window/${admissionWindowCode}/cuet-preference`);
        applyInstitutePreferenceToUI(response.data.wantsCuet, response.data.submitted);
    } catch (err) {
        console.error('Failed to load CUET preference', err);
    }
}

function applyInstitutePreferenceToUI(wantsCuet, submitted) {
    // Update card badge
    const badge = document.getElementById('cuetPreferenceBadge');
    if (badge) {
        badge.className = wantsCuet
            ? 'badge bg-success bg-opacity-10 text-success border border-success fs-6 px-3 py-2'
            : 'badge bg-secondary bg-opacity-10 text-secondary border border-secondary fs-6 px-3 py-2';
        badge.innerHTML = wantsCuet
            ? '<i class="bi bi-check-circle me-1"></i>CUET'
            : '<i class="bi bi-dash-circle me-1"></i>Non-CUET';
    }

    // Show locked badge and disable button if preference is submitted
    const lockedBadge = document.getElementById('cuetPreferenceLockedBadge');
    const changeBtn = document.getElementById('changeCuetPreferenceBtn');
    if (submitted) {
        if (lockedBadge) lockedBadge.classList.remove('d-none');
        if (changeBtn) {
            changeBtn.disabled = true;
            changeBtn.title = 'Preference is locked after Final Submit';
        }
    }

    // Pre-populate modal toggle
    const instituteCuetToggle = document.getElementById('instituteCuetToggle');
    const instituteCuetLabel = document.getElementById('instituteCuetLabel');
    if (instituteCuetToggle) {
        instituteCuetToggle.checked = wantsCuet;
        updateCuetLabel(instituteCuetLabel, wantsCuet);
    }
}

async function saveCuetPreference() {
    const saveBtn = document.getElementById('saveCuetPreferenceBtn');
    const toggle = document.getElementById('instituteCuetToggle');
    if (!saveBtn || !toggle) return;

    const wantsCuet = toggle.checked;

    saveBtn.disabled = true;
    saveBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Saving...';

    try {
        const response = await axios.post(
            `/manage-programmes-data/institute/admission-window/${admissionWindowCode}/cuet-preference`,
            { wantsCuet }
        );

        // Close modal
        const modalEl = document.getElementById('cuetPreferenceModal');
        const modalInstance = bootstrap.Modal.getInstance(modalEl);
        if (modalInstance) modalInstance.hide();

        applyInstitutePreferenceToUI(response.data.wantsCuet, false);
        showAlert('CUET preference saved. All programmes will reflect this preference.', 'success');

        // Re-render programme rows so their badges update
        programmeRows = programmeRows.map(r => ({ ...r, wantsCuet: response.data.wantsCuet }));
        renderProgrammeRows();
    } catch (err) {
        showAlert(err.response?.data?.message || 'Failed to save CUET preference.', 'danger');
    } finally {
        saveBtn.disabled = false;
        saveBtn.innerHTML = '<i class="bi bi-check-circle me-1"></i>Save Preference';
    }
}

// ==================== RENDER PROGRAMME ROWS ====================

function renderProgrammeRows() {
  if (!programmesTableBody) return;
  programmesTableBody.innerHTML = '';

  if (!programmeRows || programmeRows.length === 0) {
    updateEmptyState();
    return;
  }

  programmeRows.forEach((row) => {
    const tr = document.createElement('tr');

    const {
      programmeOfferedId,
      programmeName,
      streamName,
      programmeLevel,
      totalSeats,
      alreadySent,
      canEdit,
      shift,
      shiftDisplayName,
      wantsCuet
    } = row;

    const isLocked = alreadySent;
    const checkboxState = isLocked ? 'disabled' : '';
    const rowClass = isLocked ? 'bg-light opacity-75' : '';

    tr.className = `programme-row ${rowClass}`;
    tr.setAttribute('data-shift', shift || 'NA');
    tr.setAttribute('data-shift-name', shiftDisplayName || 'Not Applicable');

    // 0. CHECKBOX (Created securely via DOM API to bypass CSP inline handler restrictions)
    const cbTd = document.createElement('td');
    cbTd.className = 'text-center';

    const cbInput = document.createElement('input');
    cbInput.type = 'checkbox';
    cbInput.className = 'form-check-input programme-checkbox shadow-sm';
    cbInput.value = programmeOfferedId;
    cbInput.setAttribute('data-has-seats', totalSeats > 0);
    if (isLocked) cbInput.disabled = true;

    cbInput.addEventListener('change', updateSubmitButtonState);

    cbTd.appendChild(cbInput);
    tr.appendChild(cbTd);

    // 1. Name
    const programmeTd = document.createElement('td');
    programmeTd.innerHTML = `<strong>${escapeHTML(programmeName) || 'Programme'}</strong>`;
    tr.appendChild(programmeTd);

    // 2. Stream
    const streamTd = document.createElement('td');
    streamTd.textContent = streamName || '';
    tr.appendChild(streamTd);

    // 3. Level
    const levelTd = document.createElement('td');
    levelTd.textContent = programmeLevel || '';
    tr.appendChild(levelTd);

    // 4. Seats
    const seatsTd = document.createElement('td');
    seatsTd.className = 'text-center fw-bold';
    seatsTd.innerHTML = (totalSeats > 0) ? `<span class="text-primary">${totalSeats}</span>` : `<span class="text-muted">-</span>`;
    tr.appendChild(seatsTd);

    // 5. STATUS COLUMN
    const statusTd = document.createElement('td');
    statusTd.className = 'text-center';

    // CUET preference badge (always shown)
    const cuetBadge = document.createElement('span');
    cuetBadge.className = wantsCuet
        ? 'badge bg-success bg-opacity-10 text-success border border-success me-1'
        : 'badge bg-secondary bg-opacity-10 text-secondary border border-secondary me-1';
    cuetBadge.innerHTML = wantsCuet
        ? '<i class="bi bi-check-circle me-1"></i>CUET'
        : '<i class="bi bi-dash-circle me-1"></i>Non-CUET';

    let approvalBadge;
    if (isLocked) {
        approvalBadge = document.createElement('span');
        approvalBadge.className = 'badge bg-success rounded-pill px-3 shadow-sm';
        approvalBadge.innerHTML = '<i class="bi bi-lock-fill me-1"></i> Submitted';
    } else if (totalSeats > 0) {
        approvalBadge = document.createElement('span');
        approvalBadge.className = 'badge bg-warning text-dark border border-warning rounded-pill px-3 shadow-sm';
        approvalBadge.innerHTML = '<i class="bi bi-pencil-fill me-1"></i> Draft Saved';
    } else {
        approvalBadge = document.createElement('span');
        approvalBadge.className = 'badge bg-secondary rounded-pill px-3 shadow-sm';
        approvalBadge.textContent = 'Pending Entry';
    }

    const statusWrapper = document.createElement('div');
    statusWrapper.className = 'd-flex flex-column align-items-center gap-1';
    statusWrapper.append(approvalBadge, cuetBadge);
    statusTd.appendChild(statusWrapper);
    tr.appendChild(statusTd);

    // 6. ACTIONS COLUMN
    const actionsTd = document.createElement('td');
    actionsTd.className = 'text-center';
    const buttonsEnabled = scheduleStatus.isActive && canEdit !== false && !isLocked;

    if (isLocked) {
        actionsTd.innerHTML = `
            <div class="btn-group btn-group-sm" role="group">
                <button class="btn btn-outline-secondary opacity-75" disabled title="Locked after submission">
                    <i class="bi bi-pencil"></i> Locked
                </button>
                <button class="btn btn-success seats-btn opacity-75"
                    data-programme-id="${programmeOfferedId}"
                    data-programme-name="${escapeHTML(programmeName)}"
                    data-total-seats="${totalSeats || ''}"
                    disabled title="Locked after submission">
                    <i class="bi bi-pie-chart"></i> Locked
                </button>
            </div>
        `;
    } else {
        actionsTd.innerHTML = `
            <div class="btn-group btn-group-sm" role="group">
                <button class="btn btn-warning update-seats-btn shadow-sm"
                    data-programme-id="${programmeOfferedId}"
                    data-programme-name="${escapeHTML(programmeName)}"
                    data-shift-name="${escapeHTML(shiftDisplayName) || ''}"
                    data-total-seats="${totalSeats || ''}"
                    ${!buttonsEnabled ? 'disabled' : ''}
                    data-bs-toggle="modal" data-bs-target="#updateSeatMatrixModal">
                    <i class="bi bi-pencil"></i> Update
                </button>
                <button class="btn btn-success seats-btn shadow-sm"
                    data-programme-id="${programmeOfferedId}"
                    data-programme-name="${escapeHTML(programmeName)}"
                    data-total-seats="${totalSeats || ''}"
                    ${!buttonsEnabled ? 'disabled' : ''}>
                    <i class="bi bi-pie-chart"></i> Seats
                </button>
            </div>
        `;
    }
    tr.appendChild(actionsTd);

    programmesTableBody.appendChild(tr);
  });

  setupSeatsButtonHandlers();
  updateEmptyState();
  buildShiftTabs();

  document.getElementById('selectAllCheckbox').checked = false;
  updateSubmitButtonState();
}

// ==================== FINAL SUBMIT LOGIC ====================

function toggleAllCheckboxes() {
    const selectAll = document.getElementById('selectAllCheckbox').checked;
    // Uses .d-none instead of inline style selector to find visible rows
    const visibleCheckboxes = document.querySelectorAll('tr.programme-row:not(.d-none) .programme-checkbox:not([disabled])');
    visibleCheckboxes.forEach(cb => cb.checked = selectAll);
    updateSubmitButtonState();
}

function updateSubmitButtonState() {
    const finalSubmitBtn = document.getElementById('finalSubmitBtn');
    if (!finalSubmitBtn) return;

    const checkedBoxes = document.querySelectorAll('.programme-checkbox:checked:not([disabled])');
    let allHaveSeats = true;

    checkedBoxes.forEach(cb => {
        if (cb.getAttribute('data-has-seats') === 'false') {
            allHaveSeats = false;
        }
    });

    if (checkedBoxes.length > 0 && allHaveSeats) {
        finalSubmitBtn.disabled = false;
        finalSubmitBtn.innerHTML = `<i class="bi bi-lock-fill me-1"></i> Final Submit (${checkedBoxes.length})`;
    } else {
        finalSubmitBtn.disabled = true;
        if (checkedBoxes.length > 0 && !allHaveSeats) {
            finalSubmitBtn.innerHTML = `<i class="bi bi-exclamation-circle-fill me-1"></i> Missing Seat Entries`;
        } else {
            finalSubmitBtn.innerHTML = `<i class="bi bi-lock-fill me-1"></i> Final Submit Selected`;
        }
    }
}

function initiateFinalSubmit() {
    if(submitModal) submitModal.show();
}

async function executeFinalSubmit() {
    const confirmBtn = document.getElementById('confirmSubmitBtn');
    const checkedBoxes = document.querySelectorAll('.programme-checkbox:checked:not([disabled])');
    const selectedIds = Array.from(checkedBoxes).map(cb => parseInt(cb.value));

    if (selectedIds.length === 0) return;

    confirmBtn.disabled = true;
    confirmBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Locking...';

    try {
        await axios.post(`/manage-programmes-data/institute/admission-window/${admissionWindowCode}/send-for-approval`, {
            programmeOfferedIds: selectedIds
        });

        submitModal.hide();
        showAlert('Seats permanently locked and submitted successfully.', 'success');
        document.getElementById('selectAllCheckbox').checked = false;
        await loadProgrammesForWindow();
    } catch (error) {
        console.error('Submit error:', error);
        showAlert(error.response?.data?.message || 'Failed to submit seats.', 'danger');
    } finally {
        confirmBtn.disabled = false;
        confirmBtn.innerHTML = '<i class="bi bi-check-circle-fill me-2"></i>Yes, Lock & Submit';
    }
}

// ==================== SEATS BUTTON HANDLERS ====================

function setupSeatsButtonHandlers() {
  const seatsBtns = document.querySelectorAll('.seats-btn');
  seatsBtns.forEach((btn) => {
    btn.addEventListener('click', (e) => {
      e.preventDefault();
      if (btn.disabled) return;

      if (!scheduleStatus.isActive) {
        showAlert(`Seat management is currently not available. ${scheduleStatus.message}`, 'warning', true, 5000);
        return;
      }

      const programmeId = btn.getAttribute('data-programme-id');
      const programmeName = btn.getAttribute('data-programme-name');
      const totalSeats = btn.getAttribute('data-total-seats');
      const hasSeatMatrix = totalSeats && parseInt(totalSeats, 10) > 0;

      if (!hasSeatMatrix) {
        showAlert(`Seat matrix not configured for "${programmeName}". Please set total seats before managing reservations.`, 'warning');
      } else {
        if (!admissionWindowCode) return;
		    const safeProgrammeId = encodeURIComponent(String(programmeId));
		    const safeAdmissionWindowCode = encodeURIComponent(String(admissionWindowCode));
		    window.location.href = `/seat-reservations/page/${safeProgrammeId}/${safeAdmissionWindowCode}`;
      }
    });
  });
}

// ==================== DATA LOAD ====================

async function loadProgrammesForWindow() {
  if (!admissionWindowCode) {
    showAlert('Admission window code is missing. Please go back and try again.', 'danger');
    updateEmptyState();
    return;
  }
  try {
    await checkScheduleStatus();
    const url = `/manage-programmes-data/institute/admission-window/${admissionWindowCode}/programmes`;
    const response = await axios.get(url);
    programmeRows = response.data || [];
    renderProgrammeRows();
  } catch (error) {
    showAlert('Failed to load programmes for this admission window. Please try again later.', 'danger');
    programmeRows = [];
    renderProgrammeRows();
  }
}

// ==================== SEAT MATRIX MODAL ====================

function setupUpdateSeatMatrixModal() {
  const modal = document.getElementById('updateSeatMatrixModal');
  if (!modal) return;

  modal.addEventListener('show.bs.modal', (e) => {
    const button = e.relatedTarget;
    if (!button) return;

    if (!scheduleStatus.isActive) {
      e.preventDefault();
      showAlert(`Cannot update seats. ${scheduleStatus.message}`, 'warning', true, 5000);
      return;
    }

    const programmeId = button.getAttribute('data-programme-id');
    const programmeName = button.getAttribute('data-programme-name');
    const shiftName = button.getAttribute('data-shift-name');
    const totalSeats = button.getAttribute('data-total-seats');

    document.getElementById('updateProgrammeId').value = programmeId;
    document.getElementById('updateProgrammeName').value = programmeName;
    document.getElementById('totalSeatsInput').value = totalSeats || '';

    const shiftLabel = document.getElementById('updateProgrammeShiftLabel');
    if (shiftLabel && shiftName && shiftName !== 'Not Applicable') {
        shiftLabel.textContent = `Shift: ${shiftName}`;
    } else if (shiftLabel) {
        shiftLabel.textContent = '';
    }
  });
}

function saveSeatMatrix(event) {
  const programmeId = document.getElementById('updateProgrammeId').value;
  const totalSeats = document.getElementById('totalSeatsInput').value;
  const saveBtn = event.currentTarget;

  if (!programmeId || !totalSeats || totalSeats < 1) {
    showAlert('Please enter valid programme ID and number of seats', 'warning');
    return;
  }

  const originalContent = saveBtn.cloneNode(true);

  saveBtn.disabled = true;
  saveBtn.replaceChildren();

  const spinner = document.createElement("span");
  spinner.className = "spinner-border spinner-border-sm me-2";

  saveBtn.appendChild(spinner);
  saveBtn.appendChild(document.createTextNode("Saving..."));

  axios.post('/seat-matrix/data/assign', {
      programmeOfferedId: parseInt(programmeId, 10),
      admissionWindowId: parseInt(admissionWindowId, 10),
      totalSeats: parseInt(totalSeats, 10),
    })
    .then(() => {
      showAlert('Seat allocation updated successfully. Do not forget to Final Submit when ready!', 'success');

      setTimeout(() => {
        const modalElement = document.getElementById('updateSeatMatrixModal');
        const modalInstance = bootstrap.Modal.getInstance(modalElement);
        if (modalInstance) modalInstance.hide();

        loadProgrammesForWindow();

        saveBtn.disabled = false;
        saveBtn.replaceChildren(...originalContent.childNodes);
      }, 1500);
    })
    .catch((error) => {
      saveBtn.disabled = false;
      saveBtn.replaceChildren(...originalContent.childNodes);

      showAlert(
        error.response?.data?.message || 'Failed to save seat allocation.',
        'danger'
      );
    });
}

/**
 * SECURITY UTILITY: Escapes HTML characters to prevent DOM-based XSS attacks.
 */
function escapeHTML(str) {
    if (!str) return '';
    return String(str)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

function updateSelectedBadges(selectEl) {
    const container = document.getElementById('selectedProgrammesContainer');
    if (!container) return;

    while (container.firstChild) container.removeChild(container.firstChild);

    const selected = Array.from(selectEl.selectedOptions);

    if (selected.length === 0) {
        const msg = document.createElement('div');
        msg.className = 'text-muted small';
        msg.textContent = 'No programmes selected';
        container.appendChild(msg);
        return;
    }

    const countDiv = document.createElement('div');
    countDiv.className = 'mb-1 text-muted small';
    countDiv.textContent = `Selected: ${selected.length}`;
    container.appendChild(countDiv);

    selected.forEach(option => {
        const badge = document.createElement('span');
        badge.className = 'badge bg-primary me-1 mb-1';
        badge.textContent = option.textContent;

        const removeBtn = document.createElement('span');
        removeBtn.textContent = ' ×';
        removeBtn.setAttribute('role', 'button');
        removeBtn.classList.add('text-white', 'ms-1');

        removeBtn.onclick = () => {
            option.selected = false;
            updateSelectedBadges(selectEl);
        };

        badge.appendChild(removeBtn);
        container.appendChild(badge);
    });
}

// ==================== FILTER PROGRAMMES FUNCTION ====================
function filterProgrammes(streamId, programmeLevel, multiselectElement, allProgrammes, preselectedProgrammeIds = []) {

    const programmeSelectionArea = multiselectElement.closest(
        '.programme-selection-area, .add-programme-selection-area, .edit-programme-selection-area'
    );

    const preselectedIds = (preselectedProgrammeIds || []).map(String);
    const selectedValues = Array.from(multiselectElement.selectedOptions).map(opt => String(opt.value));

    multiselectElement.classList.remove('is-invalid');

    // ================= FYUG =================
    if (programmeLevel === 'FYUG') {

        multiselectElement.innerHTML = '';
        multiselectElement.disabled = true;
        multiselectElement.classList.add('bg-light');

        const filtered = allProgrammes.filter(p => String(p.programmeLevel) === 'FYUG');

        filtered.forEach(programme => {
            const option = document.createElement('option');
            option.value = programme.programmeId;
            option.textContent = `${programme.programmeName} (${programme.stream.streamName})`;
            const isPreselected = preselectedIds.includes(String(programme.programmeId));
            option.selected = preselectedIds.length > 0 ? isPreselected : true;
            multiselectElement.appendChild(option);
        });

        if (programmeSelectionArea) {
            programmeSelectionArea.classList.remove('d-none');
            programmeSelectionArea.classList.add('d-block');
        }
        updateSelectedBadges(multiselectElement);
        return;
    }

    // ================= OTHERS =================
    multiselectElement.disabled = false;
    multiselectElement.classList.remove('bg-light');

    if (!programmeLevel) {
        if (programmeSelectionArea) {
            programmeSelectionArea.classList.remove('d-block');
            programmeSelectionArea.classList.add('d-none');
        }
        return;
    }

    // ================= ALL STREAMS =================
    if (!streamId) {
        const filtered = allProgrammes.filter(programme => String(programme.programmeLevel) === String(programmeLevel));
        multiselectElement.innerHTML = '';

        filtered.forEach(programme => {
            const option = document.createElement('option');
            option.value = programme.programmeId;
            option.textContent = `${programme.programmeName} (${programme.stream.streamName})`;
            const idStr = String(programme.programmeId);
            if (preselectedIds.includes(idStr) || selectedValues.includes(idStr)) option.selected = true;
            multiselectElement.appendChild(option);
        });

        if (programmeSelectionArea) {
            programmeSelectionArea.classList.remove('d-none');
            programmeSelectionArea.classList.add('d-block');
        }
        updateSelectedBadges(multiselectElement);
        return;
    }

    // ================= STREAM + LEVEL =================
    const filtered = allProgrammes.filter(programme =>
        String(programme.programmeLevel) === String(programmeLevel) &&
        String(programme.stream.streamId) === String(streamId)
    );

    multiselectElement.innerHTML = '';

    filtered.forEach(programme => {
        const option = document.createElement('option');
        option.value = programme.programmeId;
        option.textContent = `${programme.programmeName} (${programme.stream.streamName})`;
        const idStr = String(programme.programmeId);
        if (preselectedIds.includes(idStr) || selectedValues.includes(idStr)) option.selected = true;
        multiselectElement.appendChild(option);
    });

    if (programmeSelectionArea) {
        programmeSelectionArea.classList.remove('d-none');
        programmeSelectionArea.classList.add('d-block');
    }
    updateSelectedBadges(multiselectElement);
}

// ==================== MAIN INITIALIZATION ====================
function clearProgrammes() {
    const selectEl = document.getElementById('addProgrammeIds');
    const container = document.getElementById('selectedProgrammesContainer');

    if (!selectEl) return;

    Array.from(selectEl.options).forEach(option => option.selected = false);

    if (container) {
        while (container.firstChild) container.removeChild(container.firstChild);
        const msg = document.createElement('div');
        msg.className = 'text-muted small';
        msg.textContent = 'No programmes selected';
        container.appendChild(msg);
    }
    updateSelectedBadges(selectEl);
}

function initAdmissionManagement(data) {
    const { allProgrammesData } = data;
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');

    // ==================== DATATABLE INITIALIZATION ====================
    const existingWindowsTable = $('#existingWindowsTable');
    if (existingWindowsTable.length) {
        try {
            if (!$.fn.DataTable.isDataTable('#existingWindowsTable')) {
                existingWindowsTable.DataTable({
                    responsive: true, paging: true, searching: true, lengthChange: true,
                    info: true, autoWidth: false, order: [[2, 'desc']],
                    columnDefs: [{ orderable: false, searchable: false, targets: -1 }],
                    drawCallback: function () {
                        const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
                        tooltipTriggerList.map(function (tooltipTriggerEl) {
                            if (!bootstrap.Tooltip.getInstance(tooltipTriggerEl)) new bootstrap.Tooltip(tooltipTriggerEl);
                        });
                    }
                });
            } else {
                existingWindowsTable.DataTable().draw();
            }
        } catch (e) {
            console.error('DataTable init failed:', e);
        }
    }

    // ==================== FLATPICKR INITIALIZATION ====================
    try {
        if (typeof flatpickr !== 'undefined') {
            const flatpickrConfig = {
                enableTime: true, dateFormat: "Y-m-d\\TH:i", altInput: true,
                altFormat: "F j, Y - h:i K", time_24hr: false, static: true
            };

            // ✅ Fix: Bind directly to elements to prevent Array wrapping bugs
            ['addStartDate', 'addEndDate', 'editStartDate', 'editEndDate', 'newEndDate'].forEach(id => {
                const el = document.getElementById(id);
                if (el) flatpickr(el, flatpickrConfig);
            });

        } else {
            console.warn("Flatpickr library didn't load. Falling back to native browser calendar.");
        }
    } catch (e) {
        console.error("Calendar initialization failed:", e);
    }

    // ✅ Fix: Safely fetch the picker instance directly from the element
    function setDateSafe(inputId, dateVal) {
        const el = document.getElementById(inputId);
        if (!el) return;
        const picker = el._flatpickr;

        if (picker) {
            picker.setDate(dateVal);
        } else {
            el.value = escapeHTML(dateVal);
        }
    }

    // ✅ Fix: Clean helper to handle Flatpickr invalid styles
    function markInvalid(inputId) {
        const el = document.getElementById(inputId);
        if (!el) return;
        if (el._flatpickr && el._flatpickr.altInput) {
            el._flatpickr.altInput.classList.add('is-invalid');
        } else {
            el.classList.add('is-invalid');
        }
    }

    // ==================== ADD WINDOW MODAL LOGIC ====================
    const addWindowModalEl = document.getElementById('addWindowModal');
    if (addWindowModalEl) {
        const addWindowForm = document.getElementById('addWindowForm');
        const addStreamSelect = document.getElementById('addStream');
        const addProgrammeLevelSelect = document.getElementById('addProgrammeLevel');
        const addProgrammeIdsSelect = document.getElementById('addProgrammeIds');
        const addProgrammeSelectionArea = addProgrammeIdsSelect.closest('.add-programme-selection-area');
        const addSessionSelect = document.getElementById('addSession');
        const addWindowTypeHidden = document.getElementById('addWindowTypeHidden');

        addProgrammeIdsSelect.addEventListener('change', function () { updateSelectedBadges(this); });

        addProgrammeLevelSelect.addEventListener('change', function () {
            if (this.value === 'FYUG') {
                addStreamSelect.value = '';
                addStreamSelect.disabled = true;
            } else {
                addStreamSelect.disabled = false;
            }
        });

        function formatDateTimeLocal(date, hour, minute) {
            if (!(date instanceof Date)) date = new Date(date);
            date.setHours(hour, minute, 0, 0);
            const yyyy = date.getFullYear();
            const mm = String(date.getMonth() + 1).padStart(2, '0');
            const dd = String(date.getDate()).padStart(2, '0');
            const hh = String(date.getHours()).padStart(2, '0');
            const min = String(date.getMinutes()).padStart(2, '0');
            return `${yyyy}-${mm}-${dd}T${hh}:${min}`;
        }

        addWindowModalEl.addEventListener('show.bs.modal', function () {
            const now = new Date();
            const lastDay = new Date(now.getFullYear(), now.getMonth() + 1, 0);
            setDateSafe("addStartDate", formatDateTimeLocal(now, 0, 0));
            setDateSafe("addEndDate", formatDateTimeLocal(lastDay, 23, 59));
        });

        addWindowModalEl.addEventListener('hidden.bs.modal', function () {
            addWindowForm.reset();
            addProgrammeIdsSelect.disabled = false;
            addStreamSelect.disabled = false;
            if (addProgrammeSelectionArea) {
                addProgrammeSelectionArea.classList.remove('d-block');
                addProgrammeSelectionArea.classList.add('d-none');
            }
            addProgrammeIdsSelect.innerHTML = '<option value="" disabled selected>Select Stream and Programme Level first</option>';

            setDateSafe("addStartDate", "");
            setDateSafe("addEndDate", "");

            addWindowForm.querySelectorAll('.is-invalid').forEach(el => el.classList.remove('is-invalid'));
            addWindowForm.querySelectorAll('.invalid-feedback').forEach(el => el.textContent = '');

            const duplicateWarning = document.getElementById('addDuplicateWarning');
            if (duplicateWarning) duplicateWarning.remove();
        });

        const toggleAddProgrammeSelection = () => {
            const radiosContainer = document.getElementById('addScopeStream')?.closest('.mb-3');
            if (radiosContainer) {
                radiosContainer.querySelectorAll('input[name="windowTypeRadio"]').forEach(radio => radio.classList.remove('is-invalid'));
                const feedbackEl = radiosContainer.querySelector('.invalid-feedback');
                if (feedbackEl) feedbackEl.textContent = '';
            }
        };

        document.querySelectorAll('input[name="windowTypeRadio"]').forEach(radio => radio.addEventListener('change', toggleAddProgrammeSelection));

        const filterAddProgrammesOnChange = () => {
            let streamId = addStreamSelect.value || null;
            const programmeLevel = addProgrammeLevelSelect.value;
            const programmeSelectionArea = addProgrammeIdsSelect.closest('.add-programme-selection-area');

            if (programmeLevel === 'FYUG') {
                filterProgrammes('', programmeLevel, addProgrammeIdsSelect, allProgrammesData);
                return;
            }

            if (programmeLevel === 'FYUG') streamId = null;
            else if (!programmeLevel || !streamId) {
                if (programmeSelectionArea) {
                    programmeSelectionArea.classList.remove('d-block');
                    programmeSelectionArea.classList.add('d-none');
                }
                return;
            }
            filterProgrammes(streamId, programmeLevel, addProgrammeIdsSelect, allProgrammesData);
        };

        addStreamSelect.addEventListener('change', filterAddProgrammesOnChange);
        addProgrammeLevelSelect.addEventListener('change', filterAddProgrammesOnChange);

        addWindowForm.addEventListener('submit', function (event) {
            let isValid = true;
            event.preventDefault();

            addWindowForm.querySelectorAll('.is-invalid').forEach(el => el.classList.remove('is-invalid'));
            addWindowForm.querySelectorAll('.invalid-feedback').forEach(el => el.textContent = '');

            addWindowTypeHidden.value = 'STREAM';
            if (!addProgrammeLevelSelect.value) { addProgrammeLevelSelect.classList.add('is-invalid'); isValid = false; }
            if (!addSessionSelect.value) { addSessionSelect.classList.add('is-invalid'); isValid = false; }

            if (!document.getElementById('addStartDate').value) {
                markInvalid('addStartDate');
                isValid = false;
            }
            if (!document.getElementById('addEndDate').value) {
                markInvalid('addEndDate');
                isValid = false;
            }

            if (isValid) {
                if (addProgrammeIdsSelect.selectedOptions.length === 0) {
                    addProgrammeIdsSelect.classList.add('is-invalid');
                    isValid = false;
                }
                this.submit();
            } else {
                const firstInvalid = document.querySelector('.is-invalid');
                if (firstInvalid) firstInvalid.scrollIntoView({ behavior: 'smooth', block: 'center' });
            }
        });
    }

    // ==================== EDIT WINDOW MODAL LOGIC ====================
    const editWindowModalEl = document.getElementById('editWindowModal');
    if (editWindowModalEl) {
        const editWindowForm = document.getElementById('editWindowForm');
        const editAdmissionCodeInput = document.getElementById('editAdmissionCode');
        const editStreamSelect = document.getElementById('editStream');
        const editProgrammeLevelSelect = document.getElementById('editProgrammeLevel');
        const editProgrammeIdsSelect = document.getElementById('editProgrammeIds');
        const editProgrammeSelectionArea = editProgrammeIdsSelect.closest('.edit-programme-selection-area');
        const editSessionSelect = document.getElementById('editSession');
        const editWindowTypeHidden = document.getElementById('editWindowTypeHidden');

        const filterEditProgrammesOnChange = () => {
            let streamId = editStreamSelect.value || null;
            const programmeLevel = editProgrammeLevelSelect.value;
            const programmeSelectionArea = editProgrammeIdsSelect.closest('.edit-programme-selection-area');

            if (programmeLevel === 'FYUG') {
                filterProgrammes('', programmeLevel, editProgrammeIdsSelect, allProgrammesData);
                return;
            }

            if (!programmeLevel) {
                if (programmeSelectionArea) {
                    programmeSelectionArea.classList.remove('d-block');
                    programmeSelectionArea.classList.add('d-none');
                }
                return;
            }
            filterProgrammes(streamId, programmeLevel, editProgrammeIdsSelect, allProgrammesData);
        };

        editStreamSelect.addEventListener('change', filterEditProgrammesOnChange);
        editProgrammeLevelSelect.addEventListener('change', function () {
            if (this.value === 'FYUG') {
                editStreamSelect.value = '';
                editStreamSelect.disabled = true;
            } else {
                editStreamSelect.disabled = false;
            }
            filterEditProgrammesOnChange();
        });

        editWindowForm.addEventListener('submit', function (event) {
            let isValid = true;
            event.preventDefault();

            editWindowForm.querySelectorAll('.is-invalid').forEach(el => el.classList.remove('is-invalid'));

            if (editProgrammeIdsSelect.selectedOptions.length > 0) editWindowTypeHidden.value = 'PROGRAMME';
            else editWindowTypeHidden.value = 'STREAM';

            if (!editProgrammeLevelSelect.value) { editProgrammeLevelSelect.classList.add('is-invalid'); isValid = false; }
            if (!editSessionSelect.value) { editSessionSelect.classList.add('is-invalid'); isValid = false; }

            if (!document.getElementById('editEndDate').value) {
                markInvalid('editEndDate');
                isValid = false;
            }

            if (isValid) this.submit();
        });
    }

    // ==================== EDIT BUTTON HANDLER ====================
    existingWindowsTable.off('click', '.edit-window-btn');
    existingWindowsTable.on('click', '.edit-window-btn', function(event) {
        event.preventDefault();

        const windowCode = escapeHTML(this.dataset.windowCode);
        const editForm = document.getElementById('editWindowForm');
        const editModal = new bootstrap.Modal(document.getElementById('editWindowModal'));

        editForm.reset();
        editForm.querySelectorAll('.is-invalid').forEach(el => el.classList.remove('is-invalid'));

        const editProgrammeSelectionArea = editForm.querySelector('.edit-programme-selection-area');
        if (editProgrammeSelectionArea) {
            editProgrammeSelectionArea.classList.remove('d-block');
            editProgrammeSelectionArea.classList.add('d-none');
        }

        const editProgrammeIdsSelect = editForm.querySelector('#editProgrammeIds');
        if (editProgrammeIdsSelect) editProgrammeIdsSelect.innerHTML = '';
        const duplicateWarning = document.getElementById('editDuplicateWarning');
        if (duplicateWarning) duplicateWarning.remove();

        editForm.action = `/admin/update-admission-window/${windowCode}`;

        axios.get(`/admin/admission-window/${windowCode}`)
            .then(response => {
                const data = response.data;
                editForm.querySelector('#editStream').value = data.streamId ? String(data.streamId) : '';
                editForm.querySelector('#editProgrammeLevel').value = data.programmeLevel || '';
                editForm.querySelector('#editSession').value = data.session || '';

                if (data.programmeIds && data.programmeIds.length > 0) editWindowTypeHidden.value = 'PROGRAMME';
                else editWindowTypeHidden.value = 'STREAM';

                filterProgrammes(data.streamId || null, data.programmeLevel, editForm.querySelector('#editProgrammeIds'), allProgrammesData, data.programmeIds || []);

                setDateSafe("editStartDate", data.startDate);
                setDateSafe("editEndDate", data.endDate);
                editModal.show();
            })
            .catch(error => alert('Failed to load data for editing.'));
    });

    // ==================== EXTEND WINDOW LOGIC ====================
    const extendWindowModalEl = document.getElementById('extendWindowModal');
    if (extendWindowModalEl) {
        const extendWindowModal = new bootstrap.Modal(extendWindowModalEl);
        const extendWindowForm = document.getElementById('extendWindowForm');
        const currentEndDateText = document.getElementById('currentEndDateText');

        existingWindowsTable.off('click', '.extend-window-btn');
        existingWindowsTable.on('click', '.extend-window-btn', function(event) {
            event.preventDefault();

            const windowCode = escapeHTML(this.dataset.windowCode);
            const windowName = this.dataset.windowName;
            const currentEndStr = this.dataset.currentEnd;

            document.getElementById('extendWindowName').textContent = windowName;

            const currentEndDateObj = new Date(currentEndStr.replace(' ', 'T'));
            currentEndDateText.textContent = currentEndDateObj.toLocaleString(undefined, {
                year: 'numeric', month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit'
            });

            const isoString = escapeHTML(currentEndStr.replace(' ', 'T'));

            const extendInput = document.getElementById('newEndDate');
            if (extendInput) {
                const picker = extendInput._flatpickr;
                if (picker) {
                    picker.set('minDate', isoString);
                    picker.setDate(isoString);
                } else {
                    extendInput.min = isoString;
                    extendInput.value = isoString;
                }
            }

            extendWindowForm.action = `/admin/admission-window/${windowCode}/extend`;
            extendWindowForm.querySelectorAll('.is-invalid').forEach(el => el.classList.remove('is-invalid'));
            extendWindowModal.show();
        });

        extendWindowForm.addEventListener('submit', function(event) {
            let isValid = true;
            event.preventDefault();
            extendWindowForm.querySelectorAll('.is-invalid').forEach(el => el.classList.remove('is-invalid'));

            if (!document.getElementById('newEndDate').value) {
                markInvalid('newEndDate');
                isValid = false;
            }
            if (isValid) this.submit();
        });
    }

    // ==================== VIEW/MANAGE PROGRAMMES ====================
    function fetchAndRenderProgrammes(windowCode, windowName) {
        const tbody = document.querySelector('#windowProgrammesTable tbody');
        const noMsg = document.getElementById('noProgrammesMessage');
        const viewWindowNameElement = document.getElementById('viewWindowName');

        if (windowName && viewWindowNameElement) viewWindowNameElement.textContent = windowName;

        tbody.innerHTML = '<tr><td colspan="3" class="text-center p-4"><div class="spinner-border text-primary" role="status"></div></td></tr>';

        noMsg.classList.remove('d-block');
        noMsg.classList.add('d-none');

        axios.get(`/admin/admission-window/${escapeHTML(windowCode)}/programmes`)
            .then(response => {
                const programmes = response.data;
                tbody.innerHTML = '';

                if (programmes.length === 0) {
                    noMsg.classList.remove('d-none');
                    noMsg.classList.add('d-block');
                    tbody.innerHTML = `<tr><td colspan="3" class="text-center">No specific programmes defined. This window applies to ALL programmes of the selected stream/level.</td></tr>`;
                } else {
                    programmes.forEach(programme => {
                        const progId = escapeHTML(programme.admissionWindowProgrammeId || programme.id);
                        const safeProgName = escapeHTML(programme.programmeName);
                        const toggleIcon = programme.active ? 'fas fa-toggle-off' : 'fas fa-toggle-on';
                        const toggleTitle = programme.active ? 'Deactivate' : 'Activate';
                        const toggleClass = programme.active ? 'btn-warning' : 'btn-success';

                        const tr = document.createElement('tr');
                        const tdName = document.createElement('td');
                        tdName.className = 'align-middle';
                        tdName.textContent = safeProgName;

                        const tdStatus = document.createElement('td');
                        tdStatus.className = 'text-center align-middle';
                        const badge = document.createElement('span');
                        badge.className = programme.active ? 'badge bg-success' : 'badge bg-secondary';
                        badge.textContent = programme.active ? 'Active' : 'Inactive';
                        tdStatus.appendChild(badge);

                        const tdAction = document.createElement('td');
                        tdAction.className = 'text-center align-middle';
                        const form = document.createElement('form');
                        form.method = 'post';
                        form.action = `/admin/admission-window/programme/toggle/${progId}`;
                        form.className = 'm-0 d-inline-block';

                        const csrfInput = document.createElement('input');
                        csrfInput.type = 'hidden';
                        csrfInput.name = '_csrf';
                        csrfInput.value = csrfToken;

                        const button = document.createElement('button');
                        button.type = 'submit';
                        button.className = `btn btn-sm ${toggleClass}`;
                        button.title = toggleTitle;

                        const icon = document.createElement('i');
                        icon.className = toggleIcon;

                        button.appendChild(icon);
                        button.appendChild(document.createTextNode(' ' + toggleTitle));
                        form.appendChild(csrfInput);
                        form.appendChild(button);
                        tdAction.appendChild(form);

                        tr.appendChild(tdName);
                        tr.appendChild(tdStatus);
                        tr.appendChild(tdAction);
                        tbody.appendChild(tr);
                    });
                }
            })
            .catch(error => {
                console.error('Error fetching programmes:', error);
                tbody.innerHTML = '<tr><td colspan="3" class="text-center text-danger p-3">Failed to load programme details.</td></tr>';
            });
    }

    existingWindowsTable.off('click', '.view-programmes-btn').on('click', '.view-programmes-btn', function(event) {
        event.preventDefault();
        const windowCode = this.dataset.windowCode;
        const windowName = this.dataset.windowName;
        const viewProgrammesModal = new bootstrap.Modal(document.getElementById('viewProgrammesModal'));
        fetchAndRenderProgrammes(windowCode, windowName);
        viewProgrammesModal.show();
    });

    // ==================== DELETE WINDOW HANDLER ====================
    const deleteWindowModalEl = document.getElementById('deleteWindowModal');
    if (deleteWindowModalEl) {
        const deleteWindowModal = new bootstrap.Modal(deleteWindowModalEl);
        const deleteWindowForm = document.getElementById('deleteWindowForm');

        existingWindowsTable.off('click', '.delete-window-btn').on('click', '.delete-window-btn', function(event){
            event.preventDefault();
            const admissionCode = escapeHTML(this.dataset.windowCode);
            const windowName = this.dataset.windowName;
            document.getElementById('deleteWindowName').textContent = windowName;
            deleteWindowForm.action = `/admin/delete-admission-window/${admissionCode}`;
            deleteWindowModal.show();
        });
    }
}

// ==================== BOOTSTRAP ENTRY POINT ====================
document.addEventListener("DOMContentLoaded", function () {
    // Guard: only run on pages that actually contain the admission management UI.
    // This script is loaded globally via layout.html; without this guard it would
    // call a CONTROLLER-only endpoint on every page, causing 403s for INSTITUTE users.
    if (!document.getElementById('addWindowModal') && !document.getElementById('admission-management-root')) {
        return;
    }
    axios.get('/controller/admissions/programmes/all')
        .then(res => {
            initAdmissionManagement({ allProgrammesData: res.data });
        })
        .catch(err => {
            console.error('Failed to load programmes:', err);
            initAdmissionManagement({ allProgrammesData: [] });
        });
});
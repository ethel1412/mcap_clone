function updateDatesFromSchedule(scheduleSelectElement, startDateInput, endDateInput, schedulesData) {
    const selectedScheduleId = scheduleSelectElement.value;

    // Add debug logs
    console.log("--- updateDatesFromSchedule called ---");
    console.log("Selected Schedule ID from dropdown:", selectedScheduleId);
    console.log("Total schedules loaded (allSchedulesData.length):", schedulesData ? schedulesData.length : 'undefined');

    if (selectedScheduleId) {
        const schedule = schedulesData.find(s => String(s.scheduleId) === String(selectedScheduleId));
        if (schedule) {
            console.log("Found matching schedule:", schedule);
            console.log("Schedule Start Date:", schedule.startDate);
            console.log("Schedule End Date:", schedule.endDate);

            startDateInput.value = formatDateTimeLocal(schedule.startDate);
            endDateInput.value = formatDateTimeLocal(schedule.endDate);
            console.log("Inputs updated to:", startDateInput.value, endDateInput.value);
        } else {
            console.error("Selected schedule data not found in schedulesData for id:", selectedScheduleId);
            startDateInput.value = '';
            endDateInput.value = '';
        }
    } else {
        console.log("No schedule selected (selectedScheduleId is empty). Clearing date inputs.");
        startDateInput.value = '';
        endDateInput.value = '';
    }
    console.log("------------------------------------");
}


function filterProgrammes(streamId, programmeLevel, multiselectElement, allProgrammes, preselectedProgrammeIds = []) {
    multiselectElement.innerHTML = '';
    multiselectElement.classList.remove('is-invalid');
    const feedbackElement = multiselectElement.closest('.mb-3')?.querySelector('.invalid-feedback');
    if (feedbackElement) feedbackElement.textContent = '';

    const programmeSelectionArea = multiselectElement.closest('.programme-selection-area, .add-programme-selection-area, .edit-programme-selection-area');

    if (!streamId || !programmeLevel) {
        if (programmeSelectionArea) programmeSelectionArea.style.display = 'none';
        multiselectElement.innerHTML = '<option value="" disabled selected>Select Stream and Programme Level first</option>';
        return;
    }

    const filtered = allProgrammes.filter(programme =>
        String(programme.stream.streamId) === String(streamId) && String(programme.programmeLevel) === String(programmeLevel)
    );

    if (filtered.length > 0) {
        filtered.forEach(programme => {
            const option = document.createElement('option');
            option.value = programme.programmeId;
            option.textContent = programme.programmeName;
            if (preselectedProgrammeIds.map(String).includes(String(programme.programmeId))) {
                option.selected = true;
            }
            multiselectElement.appendChild(option);
        });
        if (programmeSelectionArea) programmeSelectionArea.style.display = 'block';
        addSingleClickMultiselect(multiselectElement);
    } else {
        const option = document.createElement('option');
        option.value = '';
        option.textContent = 'No programmes found for this stream/level';
        option.disabled = true;
        option.selected = true;
        multiselectElement.appendChild(option);
        if (programmeSelectionArea) programmeSelectionArea.style.display = 'block';
    }
}



function initAdmissionManagement(data) {
    const { allProgrammesData, allSchedulesData } = data; // Destructure the shared data
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content'); // Get token once

    console.log("Admission Management Initializing...");
    console.log("allProgrammesData available:", allProgrammesData.length);
    console.log("allSchedulesData available:", allSchedulesData.length);

    const existingWindowsTable = $('#existingWindowsTable');
    if (existingWindowsTable.length) {
        try {
            existingWindowsTable.DataTable({
                responsive: true,
                paging: true,
                searching: true,
                lengthChange: true,
                info: true,
                autoWidth: false,
                order: [[2, 'desc']], // Assuming Start Date is the 3rd column (index 2)
                columnDefs: [
                    { "orderable": false, "searchable": false, "targets": -1 } // Disable for last column (Actions)
                ],
                "drawCallback": function( settings ) {
                    // Reinitialize tooltips on every draw (e.g., page change)
                    const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
                    tooltipTriggerList.map(function (tooltipTriggerEl) {
                        // Prevent re-init if already initialized by checking instance
                        if (!bootstrap.Tooltip.getInstance(tooltipTriggerEl)) {
                             new bootstrap.Tooltip(tooltipTriggerEl);
                        }
                    });
                }
            });
        } catch (e) {
            console.error("DataTable init failed for existingWindowsTable", e);
        }
    }

        // --- ADD WINDOW MODAL LOGIC ---
    const addWindowModalEl = document.getElementById('addWindowModal');
    if (addWindowModalEl) {
        const addWindowForm = document.getElementById('addWindowForm');
        const addScopeStreamRadio = document.getElementById('addScopeStream');
        const addScopeProgrammeRadio = document.getElementById('addScopeProgramme');
        const addStreamSelect = document.getElementById('addStream');
        const addProgrammeLevelSelect = document.getElementById('addProgrammeLevel');
        const addProgrammeIdsSelect = document.getElementById('addProgrammeIds');
        const addProgrammeSelectionArea = addProgrammeIdsSelect.closest('.add-programme-selection-area');
        const addStartDate = document.getElementById('addStartDate');
        const addEndDate = document.getElementById('addEndDate');
        const addSessionSelect = document.getElementById('addSession');
        const addWindowTypeHidden = document.getElementById('addWindowTypeHidden');

        function formatDateTimeLocal(date, hour, minute) {
            if (!(date instanceof Date)) date = new Date(date);
            date.setHours(hour, minute, 0, 0);
            // Pads to 2-digits
            const yyyy = date.getFullYear();
            const mm = String(date.getMonth() + 1).padStart(2, '0');
            const dd = String(date.getDate()).padStart(2, '0');
            const hh = String(date.getHours()).padStart(2, '0');
            const min = String(date.getMinutes()).padStart(2, '0');
            return `${yyyy}-${mm}-${dd}T${hh}:${min}`;
        }

        document.getElementById('addWindowModal')
            .addEventListener('show.bs.modal', function () {
                const addStartDate = document.getElementById('addStartDate');
                const addEndDate = document.getElementById('addEndDate');

                // Today at 00:00
                const now = new Date();
                addStartDate.value = formatDateTimeLocal(now, 0, 0);

                // Last day of month at 23:59
                const lastDay = new Date(now.getFullYear(), now.getMonth() + 1, 0);
                addEndDate.value = formatDateTimeLocal(lastDay, 23, 59);
            });

        // Reset form on modal hide
        addWindowModalEl.addEventListener('hidden.bs.modal', function () {
            addWindowForm.reset();
            if (addProgrammeSelectionArea) addProgrammeSelectionArea.style.display = 'none';
            addProgrammeIdsSelect.innerHTML = '<option value="" disabled selected>Select Stream and Programme Level first</option>';
            addStartDateInput.value = '';
            addEndDateInput.value = '';
            addWindowTypeHidden.value = '';
            document.querySelectorAll('input[name="windowTypeRadio"]').forEach(radio => radio.checked = false);

            addWindowForm.querySelectorAll('.is-invalid').forEach(el => el.classList.remove('is-invalid'));
            addWindowForm.querySelectorAll('.invalid-feedback').forEach(el => el.textContent = '');
        });

        // Event listeners for windowType radio buttons
        const toggleAddProgrammeSelection = () => {
            const radiosContainer = document.getElementById('addScopeStream')?.closest('.mb-3');
            if (radiosContainer) {
                 radiosContainer.querySelectorAll('input[name="windowTypeRadio"]').forEach(radio => radio.classList.remove('is-invalid'));
                const feedbackEl = radiosContainer.querySelector('.invalid-feedback');
                if (feedbackEl) feedbackEl.textContent = '';
            }

            if (addScopeProgrammeRadio.checked) {
                addWindowTypeHidden.value = 'PROGRAMME';
                const streamId = addStreamSelect.value;
                const programmeLevel = addProgrammeLevelSelect.value;
                filterProgrammes(streamId, programmeLevel, addProgrammeIdsSelect, allProgrammesData);
            } else {
                addWindowTypeHidden.value = 'STREAM';
                if (addProgrammeSelectionArea) addProgrammeSelectionArea.style.display = 'none';
                addProgrammeIdsSelect.innerHTML = '<option value="" disabled selected>Select Stream and Programme Level first</option>';
            }
        };
        document.querySelectorAll('input[name="windowTypeRadio"]').forEach(radio => {
            radio.addEventListener('change', toggleAddProgrammeSelection);
        });

        // Event listeners for stream and programme level changes (for filtering specific programmes)
        const filterAddProgrammesOnChange = () => {
            if (addScopeProgrammeRadio.checked) {
                const streamId = addStreamSelect.value;
                const programmeLevel = addProgrammeLevelSelect.value;
                filterProgrammes(streamId, programmeLevel, addProgrammeIdsSelect, allProgrammesData);
            }
        };
        addStreamSelect.addEventListener('change', filterAddProgrammesOnChange);
        addProgrammeLevelSelect.addEventListener('change', filterAddProgrammesOnChange);

        // Initial state for Add modal (hide programme selection area)
        if (addProgrammeSelectionArea) addProgrammeSelectionArea.style.display = 'none';

        // Custom validation for addWindowForm
        addWindowForm.addEventListener('submit', function (event) {
            let isValid = true;
            event.preventDefault();

            addWindowForm.querySelectorAll('.is-invalid').forEach(el => el.classList.remove('is-invalid'));
            addWindowForm.querySelectorAll('.invalid-feedback').forEach(el => el.textContent = '');

            const selectedWindowTypeRadio = document.querySelector('input[name="windowTypeRadio"]:checked');
            if (!selectedWindowTypeRadio) {
                document.getElementById('addScopeStream')?.classList.add('is-invalid');
                const feedbackEl = document.getElementById('addScopeStream')?.closest('.mb-3')?.querySelector('.invalid-feedback');
                if (feedbackEl) feedbackEl.textContent = "You must select the scope of the window.";
                isValid = false;
            } else {
                addWindowTypeHidden.value = selectedWindowTypeRadio.value;
            }

            if (!addStreamSelect.value) {
                addStreamSelect.classList.add('is-invalid');
                const feedbackEl = addStreamSelect.closest('.mb-3')?.querySelector('.invalid-feedback');
                if (feedbackEl) feedbackEl.textContent = "Stream is required.";
                isValid = false;
            }
            if (!addProgrammeLevelSelect.value) {
                addProgrammeLevelSelect.classList.add('is-invalid');
                const feedbackEl = addProgrammeLevelSelect.closest('.mb-3')?.querySelector('.invalid-feedback');
                if (feedbackEl) feedbackEl.textContent = "Programme Level is required.";
                isValid = false;
            }
            if (!addSessionSelect.value) {
                 addSessionSelect.classList.add('is-invalid');
                 const feedbackEl = addSessionSelect.closest('.mb-3')?.querySelector('.invalid-feedback');
                 if(feedbackEl) feedbackEl.textContent = "Session is required.";
                 isValid = false;
            }

            if (addScopeProgrammeRadio.checked) {
                if (addProgrammeSelectionArea && addProgrammeSelectionArea.style.display !== 'none' && addProgrammeIdsSelect.options.length > 0 && !addProgrammeIdsSelect.options[0].disabled) {
                    if (addProgrammeIdsSelect.selectedOptions.length === 0) {
                        addProgrammeIdsSelect.classList.add('is-invalid');
                        const feedbackEl = addProgrammeIdsSelect.closest('.mb-3')?.querySelector('.invalid-feedback');
                        if (feedbackEl) feedbackEl.textContent = "Please select at least one programme.";
                        isValid = false;
                    }
                }
            }

            if (isValid) {
                this.submit();
            } else {
                const firstInvalid = document.querySelector('.is-invalid');
                if (firstInvalid) {
                    firstInvalid.scrollIntoView({ behavior: 'smooth', block: 'center' });
                }
            }
        });
    }


    // --- EDIT WINDOW MODAL LOGIC ---
    const editWindowModalEl = document.getElementById('editWindowModal');
    if (editWindowModalEl) {
        const editWindowModal = new bootstrap.Modal(editWindowModalEl);
        const editWindowForm = document.getElementById('editWindowForm');

        const editAdmissionIdInput = document.getElementById('editAdmissionId');
        const editScopeStreamRadio = document.getElementById('editScopeStream');
        const editScopeProgrammeRadio = document.getElementById('editScopeProgramme');
        const editStreamSelect = document.getElementById('editStream');
        const editProgrammeLevelSelect = document.getElementById('editProgrammeLevel');
        const editProgrammeIdsSelect = document.getElementById('editProgrammeIds');
        const editProgrammeSelectionArea = editProgrammeIdsSelect.closest('.edit-programme-selection-area');
        const editSessionSelect = document.getElementById('editSession');
        const editScheduleSelect = document.getElementById('editSchedule');
        const editStartDateInput = document.getElementById('editStartDate');
        const editEndDateInput = document.getElementById('editEndDate');
        const editWindowTypeHidden = document.getElementById('editWindowTypeHidden');


        // Event listener for schedule selection
        editScheduleSelect.addEventListener('change', function () {
            updateDatesFromSchedule(editScheduleSelect, editStartDateInput, editEndDateInput, allSchedulesData);
            this.classList.remove('is-invalid');
            const feedbackEl = this.closest('.mb-3')?.querySelector('.invalid-feedback');
            if (feedbackEl) feedbackEl.textContent = '';
        });

        // Event listeners for windowType radio buttons in Edit Modal
        const toggleEditProgrammeSelection = () => {
            const radiosContainer = document.getElementById('editScopeStream')?.closest('.mb-3');
            if (radiosContainer) {
                radiosContainer.querySelectorAll('input[name="windowTypeRadio"]').forEach(radio => radio.classList.remove('is-invalid'));
                const feedbackEl = radiosContainer.querySelector('.invalid-feedback');
                if (feedbackEl) feedbackEl.textContent = '';
            }

            if (editScopeProgrammeRadio.checked) {
                editWindowTypeHidden.value = 'PROGRAMME';
                const streamId = editStreamSelect.value;
                const programmeLevel = editProgrammeLevelSelect.value;
                filterProgrammes(streamId, programmeLevel, editProgrammeIdsSelect, allProgrammesData);
            } else {
                editWindowTypeHidden.value = 'STREAM';
                if (editProgrammeSelectionArea) editProgrammeSelectionArea.style.display = 'none';
                editProgrammeIdsSelect.innerHTML = '<option value="" disabled selected>Select Stream and Programme Level first</option>';
            }
        };
        document.querySelectorAll('#editWindowForm input[name="windowTypeRadio"]').forEach(radio => {
            radio.addEventListener('change', toggleEditProgrammeSelection);
        });

        // Event listeners for stream and programme level changes
        const filterEditProgrammesOnChange = () => {
            if (editScopeProgrammeRadio.checked) {
                const streamId = editStreamSelect.value;
                const programmeLevel = editProgrammeLevelSelect.value;
                filterProgrammes(streamId, programmeLevel, editProgrammeIdsSelect, allProgrammesData);
            }
        };
        editStreamSelect.addEventListener('change', filterEditProgrammesOnChange);
        editProgrammeLevelSelect.addEventListener('change', filterEditProgrammesOnChange);

        // Custom validation for editWindowForm
        editWindowForm.addEventListener('submit', function (event) {
            let isValid = true;
            event.preventDefault();

            editWindowForm.querySelectorAll('.is-invalid').forEach(el => el.classList.remove('is-invalid'));
            editWindowForm.querySelectorAll('.invalid-feedback').forEach(el => el.textContent = '');

            if (!editScheduleSelect.value) {
                editScheduleSelect.classList.add('is-invalid');
                const feedbackEl = editScheduleSelect.closest('.mb-3')?.querySelector('.invalid-feedback');
                if (feedbackEl) feedbackEl.textContent = "Schedule is required.";
                isValid = false;
            }

            const selectedWindowTypeRadio = document.querySelector('#editWindowForm input[name="windowTypeRadio"]:checked');
            if (!selectedWindowTypeRadio) {
                document.getElementById('editScopeStream')?.classList.add('is-invalid');
                const feedbackEl = document.getElementById('editScopeStream')?.closest('.mb-3')?.querySelector('.invalid-feedback');
                if (feedbackEl) feedbackEl.textContent = "You must select the scope of the window.";
                isValid = false;
            } else {
                editWindowTypeHidden.value = selectedWindowTypeRadio.value;
            }

            if (!editStreamSelect.value) {
                editStreamSelect.classList.add('is-invalid');
                const feedbackEl = editStreamSelect.closest('.mb-3')?.querySelector('.invalid-feedback');
                if (feedbackEl) feedbackEl.textContent = "Stream is required.";
                isValid = false;
            }
            if (!editProgrammeLevelSelect.value) {
                editProgrammeLevelSelect.classList.add('is-invalid');
                const feedbackEl = editProgrammeLevelSelect.closest('.mb-3')?.querySelector('.invalid-feedback');
                if (feedbackEl) feedbackEl.textContent = "Programme Level is required.";
                isValid = false;
            }
            if (!editSessionSelect.value) {
                 editSessionSelect.classList.add('is-invalid');
                 const feedbackEl = editSessionSelect.closest('.mb-3')?.querySelector('.invalid-feedback');
                 if(feedbackEl) feedbackEl.textContent = "Session is required.";
                 isValid = false;
            }

            if (editScopeProgrammeRadio.checked) {
                if (editProgrammeSelectionArea && editProgrammeSelectionArea.style.display !== 'none' && editProgrammeIdsSelect.options.length > 0 && !editProgrammeIdsSelect.options[0].disabled) {
                    if (editProgrammeIdsSelect.selectedOptions.length === 0) {
                        editProgrammeIdsSelect.classList.add('is-invalid');
                        const feedbackEl = editProgrammeIdsSelect.closest('.mb-3')?.querySelector('.invalid-feedback');
                        if (feedbackEl) feedbackEl.textContent = "Please select at least one programme.";
                        isValid = false;
                    }
                }
            }

            if (isValid) {
                this.submit();
            } else {
                const firstInvalid = document.querySelector('.is-invalid');
                if (firstInvalid) {
                    firstInvalid.scrollIntoView({ behavior: 'smooth', block: 'center' });
                }
            }
        });
    }

    // --- Core Edit/View Modal Logic  ---
    
    existingWindowsTable.on('click', '.edit-window-btn', function(event) {
        event.preventDefault(); // Prevent default button action if any
        const windowId = this.dataset.windowId;

        const editForm = document.getElementById('editWindowForm');
        const editModal = new bootstrap.Modal(document.getElementById('editWindowModal')); // Re-get modal instance
        
        // Reset form and clear validation messages before loading new data
        editForm.reset();
        editForm.querySelectorAll('.is-invalid').forEach(el => el.classList.remove('is-invalid'));
        editForm.querySelectorAll('.invalid-feedback').forEach(el => el.textContent = '');
        
        const editProgrammeSelectionArea = editForm.querySelector('.edit-programme-selection-area');
        if (editProgrammeSelectionArea) editProgrammeSelectionArea.style.display = 'none';
        const editProgrammeIdsSelect = editForm.querySelector('#editProgrammeIds');
        if (editProgrammeIdsSelect) editProgrammeIdsSelect.innerHTML = '<option value="" disabled selected>Select Stream and Programme Level first</option>';
        document.querySelectorAll('#editWindowForm input[name="windowTypeRadio"]').forEach(radio => radio.checked = false);


        editForm.action = `/admin/update-admission-window/${windowId}`; // Set form action dynamically

        axios.get(`/admin/admission-window/${windowId}`)
            .then(response => {
                const data = response.data;
                
                editForm.querySelector('#editAdmissionId').value = data.admissionId;
                editForm.querySelector('#editStream').value = data.streamId; // Corrected ID usage
                editForm.querySelector('#editProgrammeLevel').value = data.programmeLevel; // Corrected ID usage
                editForm.querySelector('#editSession').value = data.session;
                
                editForm.querySelector('#editSchedule').value = data.scheduleId || ''; // Populate schedule dropdown
                updateDatesFromSchedule(editForm.querySelector('#editSchedule'), editForm.querySelector('#editStartDate'), editForm.querySelector('#editEndDate'), allSchedulesData); // Auto-populate dates

                const hasSpecificProgrammes = data.programmeIds && data.programmeIds.length > 0;
                if (hasSpecificProgrammes) {
                    editForm.querySelector('#editScopeProgramme').checked = true; // Select 'PROGRAMME' radio
                    editForm.querySelector('#editWindowTypeHidden').value = 'PROGRAMME';
                    filterProgrammes(data.streamId, data.programmeLevel, editForm.querySelector('#editProgrammeIds'), allProgrammesData, data.programmeIds); // Filter and pre-select programmes
                } else {
                    editForm.querySelector('#editScopeStream').checked = true; // Select 'STREAM' radio
                    editForm.querySelector('#editWindowTypeHidden').value = 'STREAM';
                    if (editProgrammeSelectionArea) editProgrammeSelectionArea.style.display = 'none';
                    if (editProgrammeIdsSelect) editProgrammeIdsSelect.innerHTML = '<option value="" disabled selected>Select Stream and Programme Level first</option>';
                }
                editModal.show();
            })
            .catch(error => {
                console.error('Error fetching window data for edit:', error);
                showToast('Failed to load data for editing.', 'danger');
            });
    });

    existingWindowsTable.on('click', '.view-programmes-btn', function(event) {
        event.preventDefault(); // Prevent default button action if any
        const windowId = this.dataset.windowId;
        const windowName = this.dataset.windowName;

        const viewProgrammesModal = new bootstrap.Modal(document.getElementById('viewProgrammesModal')); // Re-get modal instance
        const windowProgrammesTableBody = document.querySelector('#windowProgrammesTable tbody');
        const noProgrammesMessage = document.getElementById('noProgrammesMessage');
        const viewWindowNameElement = document.getElementById('viewWindowName');

        if (viewWindowNameElement) viewWindowNameElement.textContent = windowName;
        
        // Show loading spinner while fetching
        if (windowProgrammesTableBody) windowProgrammesTableBody.innerHTML = '<tr><td colspan="3" class="text-center p-4"><div class="spinner-border text-primary" role="status"><span class="visually-hidden">Loading...</span></div></td></tr>';
        if (noProgrammesMessage) noProgrammesMessage.style.display = 'none'; // Hide message initially

        viewProgrammesModal.show(); // Show modal immediately to display loading state

        axios.get(`/admin/admission-window/${windowId}/programmes`)
            .then(response => {
                const programmes = response.data;
                if (windowProgrammesTableBody) windowProgrammesTableBody.innerHTML = ''; // Clear loading

                if (programmes.length === 0) {
                    if (noProgrammesMessage) noProgrammesMessage.style.display = 'block';
                    // Revert to initial message as per your admission-management.html
                    if (windowProgrammesTableBody) windowProgrammesTableBody.innerHTML = `<tr><td colspan="3" class="text-center">No specific programmes defined. This window applies to ALL programmes of the selected stream/level.</td></tr>`;
                } else {
                    if (noProgrammesMessage) noProgrammesMessage.style.display = 'none';
                    const csrfInputHTML = `<input type="hidden" name="_csrf" value="${csrfToken}" />`; // Use the global csrfToken
                    programmes.forEach(programme => {
                        const statusBadge = programme.active ? '<span class="badge bg-success">Active</span>' : '<span class="badge bg-secondary">Inactive</span>';
                        const toggleButtonIcon = programme.active ? 'fas fa-toggle-off' : 'fas fa-toggle-on';
                        const toggleButtonTitle = programme.active ? 'Deactivate' : 'Activate';
                        const toggleButtonClass = programme.active ? 'btn-warning' : 'btn-success';

                        const rowHTML = `
                            <tr>
                                <td>${programme.programmeName}</td>
                                <td class="text-center">${statusBadge}</td>
                                <td class="text-center">
                                    <div class="d-flex justify-content-center">
                                        <form action="/admin/admission-window/programme/toggle/${programme.admissionWindowProgrammeId}" method="post" style="display:inline;" class="me-2">
                                            ${csrfInputHTML}
                                            <button type="submit" class="btn btn-sm ${toggleButtonClass}" title="${toggleButtonTitle}">
                                                <i class="fas ${toggleButtonIcon}"></i> ${toggleButtonTitle}
                                            </button>
                                        </form>
                                        <form action="/admin/admission-window/programme/delete/${programme.admissionWindowProgrammeId}" method="post" style="display:inline;">
                                            ${csrfInputHTML}
                                            <button type="submit" class="btn btn-sm btn-danger" title="Remove Programme" onclick="return confirm('Are you sure you want to remove this programme from the window? This action cannot be undone.');">
                                                <i class="fas fa-trash-alt"></i> Remove
                                            </button>
                                        </form>
                                    </div>
                                </td>
                            </tr>
                        `;
                        if (windowProgrammesTableBody) windowProgrammesTableBody.insertAdjacentHTML('beforeend', rowHTML);
                    });
                }
            })
            .catch(error => {
                console.error('Error fetching programmes for window:', error);
                if (windowProgrammesTableBody) windowProgrammesTableBody.innerHTML = '<tr><td colspan="3" class="text-center text-danger p-3">Failed to load programme details.</td></tr>';
                showToast('Failed to load programme details for this window.', 'danger');
            });
    });

    // --- DELETE WINDOW MODAL LOGIC  ---
    const deleteWindowModalEl = document.getElementById('deleteWindowModal');
    if (deleteWindowModalEl) {
        const deleteWindowModal = new bootstrap.Modal(deleteWindowModalEl);
        const deleteWindowForm = document.getElementById('deleteWindowForm');

        // Use event delegation for delete buttons as well, for consistency with DataTable
        existingWindowsTable.on('click', '.delete-window-btn', function(event) {
            event.preventDefault(); // Prevent default button action if any
            const admissionId = this.dataset.windowId;
            const windowName = this.dataset.windowName;

            document.getElementById('deleteWindowName').textContent = windowName;
            deleteWindowForm.action = `/admin/delete-admission-window/${admissionId}`;
            deleteWindowModal.show();
        });
    }

}
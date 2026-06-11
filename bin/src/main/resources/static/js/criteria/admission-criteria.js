document.addEventListener('DOMContentLoaded', function () {
    // Elements
    const tableBody = document.getElementById('criteriaTbody');
    const loadingRow = document.getElementById('loadingRow');
    const alertBox = document.getElementById('alertBox');

    // Modal/Form controls
    const criteriaModalEl = document.getElementById('criteriaModal');
    const criteriaForm = document.getElementById('criteriaForm');
    const modalProgrammeOfferedId = document.getElementById('modalProgrammeOfferedId');
    const programmeOfferedLabel = document.getElementById('programmeOfferedLabel');

    const preferredProgrammeSelect = document.getElementById('preferredProgrammeId');
    const subjectIds = document.getElementById('subjectIds');
    const combinationHeaderIds = document.getElementById('combinationHeaderIds');
    const streamIds = document.getElementById('streamIds');

    const prevProgPercentages = document.getElementById('prevProgPercentages');
    const subjectsPercentages = document.getElementById('subjectsPercentages');
    const combinationsPercentages = document.getElementById('combinationsPercentages');
    const streamsPercentages = document.getElementById('streamsPercentages');

    const seatsBooked = document.getElementById('seatsBooked');
    const availableSeatsValue = document.getElementById('availableSeatsValue');
    const editingId = document.getElementById('editingId');
    const btnSendForApproval = document.getElementById('btnSendForApproval');

    // State
    let programmesOffered = [];
    let criteriaByProgramme = new Map();
    let seatMatrixByProgramme = new Map();
    let programmesMaster = [];
    let subjectsMaster = [];
    let streamsMaster = [];
    const combinationsByProgrammeId = new Map();

    // Helpers
    function showError(msg) {
        alertBox.textContent = msg;
        alertBox.classList.remove('d-none');
        alertBox.classList.add('alert-danger');
    }
    function showSuccessToast(msg) {
        const toastEl = document.getElementById('successToast');
        document.getElementById('successMessage').textContent = msg;
        new bootstrap.Toast(toastEl).show();
    }
    function showErrorToast(msg) {
        const toastEl = document.getElementById('errorToast');
        document.getElementById('errorMessage').textContent = msg;
        new bootstrap.Toast(toastEl).show();
    }
    function clearAlert() {
        alertBox.classList.add('d-none');
        alertBox.classList.remove('alert-danger');
        alertBox.textContent = '';
    }

    // Fetchers
    async function fetchProgrammesOfferedMy() {
        const { data } = await axios.get('/programmes-offered/data/my');
        return data || [];
    }
    async function fetchSeatMatricesMy() {
        const { data } = await axios.get('/seat-matrix/data/my');
        return data || [];
    }
    async function fetchCriteriaForProgramme(programmeOfferedId) {
        const { data } = await axios.get(`/admission-criterion/data/programme/${programmeOfferedId}`, { params: { username: 'me' } });
        return data || [];
    }
    async function fetchProgrammesMaster() {
        const { data } = await axios.get('/programme-data');
        return data || [];
    }
    async function fetchSubjects() {
        const { data } = await axios.get('/subject-data');
        return data || [];
    }
    async function fetchStreams() {
        const { data } = await axios.get('/stream-data');
        return data || [];
    }
    async function fetchCombinationsByProgrammeId(programmeId) {
        try {
            const { data } = await axios.get('/subject-combination/data', { params: { programmeId } });
            return data || [];
        } catch (e) {
            return [];
        }
    }

    // Rendering
    function renderTable() {
        tableBody.innerHTML = '';
        if (programmesOffered.length === 0) {
            tableBody.innerHTML = `<tr><td colspan="8" class="text-center text-muted py-4">No offered programmes found.</td></tr>`;
            return;
        }

        programmesOffered.forEach(p => {
            const criteria = criteriaByProgramme.get(p.programmeOfferedId) || [];
            const totalSeats = seatMatrixByProgramme.get(p.programmeOfferedId) || 0;
            const totalBooked = criteria.filter(c => c.approvalStatus !== 'REJECTED').reduce((sum, c) => sum + (c.bookedSeats || 0), 0);
            const remaining = Math.max(0, totalSeats - totalBooked);

            if (criteria.length === 0) {
                tableBody.insertAdjacentHTML('beforeend', `
          <tr data-po="${p.programmeOfferedId}">
            <td>
              <div class="fw-semibold">${p.programmeName}</div>
              <div class="text-muted small">${p.departmentName || ''}</div>
              <div class="text-muted small">Remaining: ${remaining}/${totalSeats}</div>
            </td>
            <td colspan="5" class="text-muted">No criteria added.</td>
            <td class="text-muted">—</td>
            <td class="text-end">
              <button class="btn btn-sm btn-outline-primary add-criteria" data-po="${p.programmeOfferedId}">
                <i class="fas fa-plus"></i>
              </button>
            </td>
          </tr>
        `);
            } else {
                criteria.forEach(c => {
                    const prevProg = c.previousProgramme || '—';
                    const subjectCell = c.subjectId ? `Subject ID: ${c.subjectId}${c.requiredPercentage ? ` (${c.requiredPercentage}%)` : ''}` : '—';
                    const combCell = c.subjectCombinationId ? `Combo ID: ${c.subjectCombinationId}${c.requiredPercentage ? ` (${c.requiredPercentage}%)` : ''}` : '—';
                    const streamCell = c.streamId ? `Stream ID: ${c.streamId}${c.requiredPercentage ? ` (${c.requiredPercentage}%)` : ''}` : '—';
                    const statusBadge = renderStatusBadge(c.approvalStatus);

                    tableBody.insertAdjacentHTML('beforeend', `
            <tr data-po="${p.programmeOfferedId}" data-id="${c.id}">
              <td>
                <div class="fw-semibold">${p.programmeName}</div>
                <div class="text-muted small">${p.departmentName || ''}</div>
                <div class="text-muted small">Remaining: ${remaining}/${totalSeats}</div>
              </td>
              <td>${prevProg}</td>
              <td>${subjectCell}</td>
              <td>${combCell}</td>
              <td>${streamCell}</td>
              <td>${c.bookedSeats ?? 0}</td>
              <td>${statusBadge}</td>
              <td class="text-end">
                <button class="btn btn-sm btn-outline-secondary edit-criteria" data-id="${c.id}" data-po="${p.programmeOfferedId}">
                  <i class="fas fa-pen"></i>
                </button>
                <button class="btn btn-sm btn-outline-danger delete-criteria" data-id="${c.id}">
                  <i class="fas fa-trash"></i>
                </button>
              </td>
            </tr>
          `);
                });

                tableBody.insertAdjacentHTML('beforeend', `
          <tr data-po="${p.programmeOfferedId}">
            <td colspan="7" class="text-muted small">Add more criteria for ${p.programmeName}</td>
            <td class="text-end">
              <button class="btn btn-sm btn-outline-primary add-criteria" data-po="${p.programmeOfferedId}">
                <i class="fas fa-plus"></i>
              </button>
            </td>
          </tr>
        `);
            }
        });
    }

    function renderStatusBadge(status) {
        if (!status) return '<span class="badge bg-secondary">N/A</span>';
        const map = {
            PENDING: 'bg-warning text-dark',
            APPROVED: 'bg-success',
            REJECTED: 'bg-danger'
        };
        const cls = map[status] || 'bg-secondary';
        return `<span class="badge ${cls}">${status}</span>`;
    }

    function computeRemainingSeats(poId) {
        const total = seatMatrixByProgramme.get(poId) || 0;
        const list = criteriaByProgramme.get(poId) || [];
        const booked = list.filter(c => c.approvalStatus !== 'REJECTED').reduce((s, c) => s + (c.bookedSeats || 0), 0);
        return Math.max(0, total - booked);
    }

    function updateRemainingSeatsHint() {
        const poId = Number(modalProgrammeOfferedId.value);
        if (!poId) {
            availableSeatsValue.textContent = '—';
            return;
        }
        availableSeatsValue.textContent = computeRemainingSeats(poId);
    }

    function fillSelect(select, items, getValue, getLabel, includeBlank = true, blankText = 'Select') {
        const opts = [];
        if (includeBlank) {
            opts.push(new Option(blankText, ''));
        }
        items.forEach(it => {
            opts.push(new Option(getLabel(it), getValue(it)));
        });
        select.replaceChildren(...opts);
    }

    function fillMulti(select, items, getValue, getLabel) {
        const opts = items.map(it => new Option(getLabel(it), getValue(it)));
        select.replaceChildren(...opts);
    }

    function resetForm() {
        criteriaForm.reset();
        editingId.value = '';
        modalProgrammeOfferedId.value = '';
        programmeOfferedLabel.textContent = '';
        availableSeatsValue.textContent = '—';

        // Clear all percentage containers
        prevProgPercentages.innerHTML = '';
        subjectsPercentages.innerHTML = '';
        combinationsPercentages.innerHTML = '';
        streamsPercentages.innerHTML = '';

        // Clear all multi-selects
        preferredProgrammeSelect.replaceChildren();
        subjectIds.replaceChildren();
        combinationHeaderIds.replaceChildren();
        streamIds.replaceChildren();

        // Remove validation classes
        preferredProgrammeSelect.classList.remove('is-invalid');
        seatsBooked.classList.remove('is-invalid');
    }

    // Dynamic percentage input creation
    function createPercentageInputs(select, container, prefix) {
        select.addEventListener('change', function() {
            container.innerHTML = '';
            Array.from(select.selectedOptions).forEach(opt => {
                const div = document.createElement('div');
                div.className = 'input-group mb-2';
                div.innerHTML = `
          <span class="input-group-text" style="min-width:120px">${opt.text}</span>
          <input type="number" min="0" max="100" step="0.01"
                 class="form-control" name="${prefix}_${opt.value}" placeholder="Min %">
        `;
                container.appendChild(div);
            });
        });
    }

    // Set up percentage input handlers
    createPercentageInputs(preferredProgrammeSelect, prevProgPercentages, 'prevProg');
    createPercentageInputs(subjectIds, subjectsPercentages, 'subject');
    createPercentageInputs(combinationHeaderIds, combinationsPercentages, 'combination');
    createPercentageInputs(streamIds, streamsPercentages, 'stream');

    // Programme name helper
    function programmeNameForId(poId) {
        const prog = programmesOffered.find(p => p.programmeOfferedId === poId);
        return prog ? prog.programmeName : '';
    }

    // Open Add Modal
    document.addEventListener('click', async (e) => {
        if (e.target.closest('.add-criteria')) {
            resetForm();
            const poId = Number(e.target.closest('button').dataset.po);
            modalProgrammeOfferedId.value = poId;
            programmeOfferedLabel.textContent = `Programme: ${programmeNameForId(poId)}`;
            updateRemainingSeatsHint();

            await ensureMastersLoaded();

            // Load combinations for this programme
            const po = programmesOffered.find(x => x.programmeOfferedId === poId);
            if (po && po.programmeId) {
                if (!combinationsByProgrammeId.has(po.programmeId)) {
                    try {
                        const combos = await fetchCombinationsByProgrammeId(po.programmeId);
                        combinationsByProgrammeId.set(po.programmeId, combos);
                    } catch (e) {
                        showErrorToast('Failed to load subject combinations.');
                    }
                }
                const combos = combinationsByProgrammeId.get(po.programmeId) || [];
                fillMulti(combinationHeaderIds, combos, c => c.id, c => c.combinationName || `Header ${c.id}`);
            }

            const modal = new bootstrap.Modal(criteriaModalEl);
            modal.show();
        }
    });

    // Open Edit Modal
    document.addEventListener('click', async (e) => {
        if (e.target.closest('.edit-criteria')) {
            resetForm();
            await ensureMastersLoaded();

            const btn = e.target.closest('button');
            const id = Number(btn.dataset.id);
            const poId = Number(btn.dataset.po);
            const list = criteriaByProgramme.get(poId) || [];
            const item = list.find(c => c.id === id);
            if (!item) return;

            editingId.value = String(id);
            modalProgrammeOfferedId.value = String(poId);
            programmeOfferedLabel.textContent = `Programme: ${programmeNameForId(poId)}`;
            updateRemainingSeatsHint();

            // Pre-fill form with existing data
            if (item.previousProgramme) {
                const match = programmesMaster.find(p => p.programmeName?.toLowerCase() === item.previousProgramme.toLowerCase());
                if (match) {
                    preferredProgrammeSelect.value = match.programmeId;
                    preferredProgrammeSelect.dispatchEvent(new Event('change'));
                    // Set percentage
                    setTimeout(() => {
                        const percentInput = document.querySelector(`input[name="prevProg_${match.programmeId}"]`);
                        if (percentInput && item.requiredPercentage) {
                            percentInput.value = item.requiredPercentage;
                        }
                    }, 100);
                }
            }

            // Handle subjects, combinations, streams
            if (item.subjectId) {
                subjectIds.value = String(item.subjectId);
                subjectIds.dispatchEvent(new Event('change'));
                setTimeout(() => {
                    const percentInput = document.querySelector(`input[name="subject_${item.subjectId}"]`);
                    if (percentInput && item.requiredPercentage) {
                        percentInput.value = item.requiredPercentage;
                    }
                }, 100);
            }

            if (item.subjectCombinationId) {
                const po = programmesOffered.find(x => x.programmeOfferedId === poId);
                if (po?.programmeId && !combinationsByProgrammeId.has(po.programmeId)) {
                    try {
                        const combos = await fetchCombinationsByProgrammeId(po.programmeId);
                        combinationsByProgrammeId.set(po.programmeId, combos);
                    } catch {}
                }
                const combos = combinationsByProgrammeId.get(po?.programmeId) || [];
                fillMulti(combinationHeaderIds, combos, c => c.id, c => c.combinationName || `Header ${c.id}`);

                combinationHeaderIds.value = String(item.subjectCombinationId);
                combinationHeaderIds.dispatchEvent(new Event('change'));
                setTimeout(() => {
                    const percentInput = document.querySelector(`input[name="combination_${item.subjectCombinationId}"]`);
                    if (percentInput && item.requiredPercentage) {
                        percentInput.value = item.requiredPercentage;
                    }
                }, 100);
            }

            if (item.streamId) {
                streamIds.value = String(item.streamId);
                streamIds.dispatchEvent(new Event('change'));
                setTimeout(() => {
                    const percentInput = document.querySelector(`input[name="stream_${item.streamId}"]`);
                    if (percentInput && item.requiredPercentage) {
                        percentInput.value = item.requiredPercentage;
                    }
                }, 100);
            }

            seatsBooked.value = item.bookedSeats || 0;

            const modal = new bootstrap.Modal(criteriaModalEl);
            modal.show();
        }
    });

    // Delete functionality
    let pendingDeleteId = null;
    document.addEventListener('click', (e) => {
        if (e.target.closest('.delete-criteria')) {
            pendingDeleteId = Number(e.target.closest('button').dataset.id);
            if (confirm('Are you sure you want to delete this criteria?')) {
                deleteCriteria();
            }
        }
    });

    async function deleteCriteria() {
        if (!pendingDeleteId) return;
        try {
            await axios.delete(`/admission-criterion/data/${pendingDeleteId}`, { params: { username: 'me' } });

            // Remove from state and rerender
            for (const [poId, list] of criteriaByProgramme.entries()) {
                const idx = list.findIndex(c => c.id === pendingDeleteId);
                if (idx >= 0) {
                    list.splice(idx, 1);
                    break;
                }
            }
            renderTable();
            showSuccessToast('Criteria deleted.');
        } catch (e) {
            showErrorToast('Failed to delete criteria.');
        } finally {
            pendingDeleteId = null;
        }
    }

    // Form submission
    criteriaForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        clearAlert();

        const poId = Number(modalProgrammeOfferedId.value);
        if (!poId) {
            showErrorToast('Programme Offered is required.');
            return;
        }

        // Validate previous programmes (required)
        const prevSelected = Array.from(preferredProgrammeSelect.selectedOptions).map(opt => ({
            id: opt.value,
            text: opt.text,
            percent: getInputPercent(`prevProg_${opt.value}`)
        }));

        if (prevSelected.length === 0) {
            preferredProgrammeSelect.classList.add('is-invalid');
            showErrorToast('Previous Programme is required.');
            return;
        } else {
            preferredProgrammeSelect.classList.remove('is-invalid');
        }

        // Get other selections
        const subjectSelected = Array.from(subjectIds.selectedOptions).map(opt => ({
            id: opt.value,
            percent: getInputPercent(`subject_${opt.value}`)
        }));
        const combSelected = Array.from(combinationHeaderIds.selectedOptions).map(opt => ({
            id: opt.value,
            percent: getInputPercent(`combination_${opt.value}`)
        }));
        const streamSelected = Array.from(streamIds.selectedOptions).map(opt => ({
            id: opt.value,
            percent: getInputPercent(`stream_${opt.value}`)
        }));

        // Validate seats
        const seats = Number(seatsBooked.value || 0);
        const remaining = computeRemainingSeats(poId);
        const currentSeats = editingId.value ? (criteriaByProgramme.get(poId)?.find(c => c.id === Number(editingId.value))?.bookedSeats || 0) : 0;

        if (seats < 0 || seats > remaining + currentSeats) {
            seatsBooked.classList.add('is-invalid');
            showErrorToast('Seats exceed remaining capacity.');
            return;
        } else {
            seatsBooked.classList.remove('is-invalid');
        }

        function getInputPercent(name) {
            const el = document.querySelector(`input[name="${name}"]`);
            return el && el.value ? Number(el.value) : undefined;
        }

        try {
            if (editingId.value) {
                // Update existing
                const payload = {
                    programmeOfferedId: poId,
                    previousProgramme: prevSelected[0]?.text || '',
                    requiredPercentage: prevSelected[0]?.percent,
                    bookedSeats: seats
                };

                if (subjectSelected.length > 0) {
                    payload.subjectId = subjectSelected[0].id;
                    payload.requiredPercentage = subjectSelected[0].percent;
                } else if (combSelected.length > 0) {
                    payload.subjectCombinationId = combSelected[0].id;
                    payload.requiredPercentage = combSelected[0].percent;
                } else if (streamSelected.length > 0) {
                    payload.streamId = streamSelected[0].id;
                    payload.requiredPercentage = streamSelected[0].percent;
                }

                await axios.put(`/admission-criterion/data/${editingId.value}`, payload, { params: { username: 'me' } });
                showSuccessToast('Criteria updated.');
            } else {
                // Create new - batch create for all combinations
                const requests = [];

                for (const prev of prevSelected) {
                    if (!subjectSelected.length && !combSelected.length && !streamSelected.length) {
                        // Just previous programme criteria
                        requests.push({
                            programmeOfferedId: poId,
                            previousProgramme: prev.text,
                            requiredPercentage: prev.percent,
                            bookedSeats: seats
                        });
                    }

                    // Subject criteria
                    subjectSelected.forEach(subj => {
                        requests.push({
                            programmeOfferedId: poId,
                            previousProgramme: prev.text,
                            subjectId: subj.id,
                            requiredPercentage: subj.percent,
                            bookedSeats: seats
                        });
                    });

                    // Combination criteria
                    combSelected.forEach(comb => {
                        requests.push({
                            programmeOfferedId: poId,
                            previousProgramme: prev.text,
                            subjectCombinationId: comb.id,
                            requiredPercentage: comb.percent,
                            bookedSeats: seats
                        });
                    });

                    // Stream criteria
                    streamSelected.forEach(stream => {
                        requests.push({
                            programmeOfferedId: poId,
                            previousProgramme: prev.text,
                            streamId: stream.id,
                            requiredPercentage: stream.percent,
                            bookedSeats: seats
                        });
                    });
                }

                // Remove duplicates
                const unique = requests.filter((v, i, a) =>
                    a.findIndex(t =>
                        JSON.stringify([t.previousProgramme, t.subjectId, t.subjectCombinationId, t.streamId]) ===
                        JSON.stringify([v.previousProgramme, v.subjectId, v.subjectCombinationId, v.streamId])
                    ) === i
                );

                for (const req of unique) {
                    await axios.post('/admission-criterion/data', req, { params: { username: 'me' } });
                }
                showSuccessToast('Criteria added.');
            }

            await refreshProgramme(poId);
            bootstrap.Modal.getInstance(criteriaModalEl)?.hide();
        } catch (error) {
            showErrorToast('Failed to save criteria.');
            console.error('Error saving criteria:', error);
        }
    });

    // Send for approval
    btnSendForApproval.addEventListener('click', async () => {
        if (!confirm('Send all pending criteria for approval?')) return;
        try {
            showSuccessToast('Submitted for approval.');
        } catch {
            showErrorToast('Failed to submit for approval.');
        }
    });

    // Load master data
    async function ensureMastersLoaded() {
        if (programmesMaster.length === 0) {
            try {
                programmesMaster = await fetchProgrammesMaster();
            } catch {
                showErrorToast('Failed to load Programmes.');
            }
        }
        fillMulti(preferredProgrammeSelect, programmesMaster, p => p.programmeId, p => p.programmeName);

        if (subjectsMaster.length === 0) {
            try {
                subjectsMaster = await fetchSubjects();
            } catch {
                showErrorToast('Failed to load Subjects.');
            }
        }
        fillMulti(subjectIds, subjectsMaster, s => s.subjectId, s => `${s.subjectName}${s.subjectCode ? ' (' + s.subjectCode + ')' : ''}`);

        if (streamsMaster.length === 0) {
            try {
                streamsMaster = await fetchStreams();
            } catch {
                showErrorToast('Failed to load Streams.');
            }
        }
        fillMulti(streamIds, streamsMaster, s => s.streamId, s => s.streamName);
    }

    // Refresh programme data
    async function refreshProgramme(poId) {
        try {
            const list = await fetchCriteriaForProgramme(poId);
            criteriaByProgramme.set(poId, list);
            renderTable();
        } catch (error) {
            console.error('Error refreshing programme:', error);
        }
    }

    // Initial load
    async function init() {
        try {
            loadingRow?.classList.remove('d-none');
            clearAlert();

            programmesOffered = await fetchProgrammesOfferedMy();

            const seatMatList = await fetchSeatMatricesMy();
            seatMatList.forEach(sm => {
                seatMatrixByProgramme.set(sm.programmeOfferedId, sm.totalSeats || 0);
            });

            await Promise.all(programmesOffered.map(async (p) => {
                try {
                    const crit = await fetchCriteriaForProgramme(p.programmeOfferedId);
                    criteriaByProgramme.set(p.programmeOfferedId, crit);
                } catch {
                    criteriaByProgramme.set(p.programmeOfferedId, []);
                }
            }));

            renderTable();
        } catch (e) {
            showError('Failed to load programmes or criteria.');
            console.error('Initialization error:', e);
        } finally {
            loadingRow?.classList.add('d-none');
        }
    }

    init();
});

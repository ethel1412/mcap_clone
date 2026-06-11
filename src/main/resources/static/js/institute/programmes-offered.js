const ProgrammesModule = (() => {

    // --- CSRF & API Helpers ---
    const getCsrfHeaders = () => {
        const token = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
        const header = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
        return (token && header) ? { [header]: token } : {};
    };

    const Api = {
        get: (url) => axios.get(url).then(r => r.data),
        post: (url, data) => axios.post(url, data, { headers: { 'Content-Type': 'application/json', ...getCsrfHeaders() } }),
        put: (url, data) => axios.put(url, data, { headers: { 'Content-Type': 'application/json', ...getCsrfHeaders() } }),
        delete: (url) => axios.delete(url, { headers: { ...getCsrfHeaders() } })
    };

    // --- State Management ---
    const state = {
        programmesOffered: [],      // Original unfiltered list
        filteredProgrammes: [],     // List currently shown
        pendingRequests: [],
        departments: [],
        streams: [],
        allProgrammes: [],          // Master system list
        availableProgrammes: [],    // Master list filtered by search
        activeProgrammeSet: new Set(), // Track what shifts are in use (e.g. "12_MORNING")
        editing: null,
        deleting: null,
        isReloading: false,
        isLocked: false              // Timeline Lock State
    };

    const els = {};
    const modals = { add: null, request: null, edit: null, delete: null };

    // --- Utility Functions ---
    function $id(id) {
        return document.getElementById(id);
    }

    function showToast(type, message) {
        const toastEl = $id(type === 'success' ? 'successToast' : 'errorToast');
        if (toastEl) {
            $id(type === 'success' ? 'successMessage' : 'errorMessage').textContent = message;
            new bootstrap.Toast(toastEl).show();
        }
    }

    function reloadPageSafely() {
        if (state.isReloading) return;
        state.isReloading = true;
        setTimeout(() => window.location.reload(), 100);
    }
    
    function enableShiftCheckboxes() {
    document.querySelectorAll('#shiftCheckboxGroup input[type="checkbox"]')
        .forEach(cb => cb.disabled = false);
}

    // --- Initialization ---
    function init() {
        // Cache DOM elements
        [
            'programmesTableBody', 'pendingTableBody', 'totalCount', 'alertBox',
            'departmentSelect', 'editDepartmentSelect', 'requestDepartmentSelect',
            'shiftSelect', 'editShiftSelect', 'submitBtn', 'scheduleLockBanner',
            'addProgrammeBtnMain', 'streamSelect', 'programmeCheckboxContainer',
            'addProgrammeForm', 'editProgrammeForm', 'requestNewForm', 'addProgrammeModal',
            'requestNewModal', 'editProgrammeModal', 'deleteConfirmModal', 'switchToRequestModal',
            'pendingCountBadge', 'confirmDeleteBtn', 'filterSearch', 'filterLevel', 'filterStream'
        ].forEach(id => els[id] = $id(id));

        // Bind Form Events
        if (els.addProgrammeForm) els.addProgrammeForm.addEventListener('submit', handleAddExisting);
        if (els.editProgrammeForm) els.editProgrammeForm.addEventListener('submit', handleEdit);
        if (els.requestNewForm) els.requestNewForm.addEventListener('submit', handleRequestSubmit);
        if (els.confirmDeleteBtn) els.confirmDeleteBtn.addEventListener('click', confirmDelete);

        // Bind Filter Events
        if (els.filterSearch) els.filterSearch.addEventListener('input', applyFilters);
        if (els.filterLevel) els.filterLevel.addEventListener('change', applyFilters);
        if (els.filterStream) els.filterStream.addEventListener('change', applyFilters);
        if (els.departmentSelect) {
		    els.departmentSelect.addEventListener('change', handleDepartmentChange);
		}
		
		if (els.addProgrammeBtnMain) {
		    els.addProgrammeBtnMain.addEventListener('click', () => {
		        modals.add.show();
		        setProgrammeMessage("Select a Department First"); // important for your data
		    });
		}

        // Form Logic
        document.querySelectorAll('#shiftCheckboxGroup input[type="checkbox"]')
	    .forEach(cb => {
	        cb.disabled = true;
	        cb.checked = false;
	    });
	    
	    document.addEventListener('change', function (e) {
		    if (e.target.matches('#shiftCheckboxGroup input[type="checkbox"]')) {
		        const checked = document.querySelectorAll('#shiftCheckboxGroup input[type="checkbox"]:checked');
		        els.submitBtn.disabled = checked.length === 0;
		    }
		});

        if (els.switchToRequestModal) {
            els.switchToRequestModal.addEventListener('click', (e) => {
                e.preventDefault();
                switchModals('add', 'request');
            });
        }
        
        if (els.requestNewModal) {
		    const backBtn = els.requestNewModal.querySelector('.btn-secondary');
		    if (backBtn) {
		        backBtn.addEventListener('click', () => {
		            switchModals('request', 'add');
		        });
		    }
		}

        // Initialize Modals
        if (els.addProgrammeModal) {
            modals.add = new bootstrap.Modal(els.addProgrammeModal);
            //els.addProgrammeModal.addEventListener('show.bs.modal', loadMasterProgrammesList);
        }
        if (els.requestNewModal) modals.request = new bootstrap.Modal(els.requestNewModal);
        if (els.editProgrammeModal) modals.edit = new bootstrap.Modal(els.editProgrammeModal);
        if (els.deleteConfirmModal) modals.delete = new bootstrap.Modal(els.deleteConfirmModal);

        reloadAll();
    }

    function switchModals(from, to) {
	    const fromEl = modals[from]?._element;
	    const toEl = modals[to]?._element;
	
	    if (!fromEl || !toEl) return;
	
	    fromEl.addEventListener('hidden.bs.modal', function handler() {
	        fromEl.removeEventListener('hidden.bs.modal', handler);
	        modals[to].show();
	    });
	
	    modals[from].hide();
	}
	
	
    // --- Schedule Timeline Check ---
    /*async function checkGlobalLock() {
        try {
            const windows = await Api.get('/manage-programmes-data/institute/windows');
            state.isLocked = !windows.some(w => w.scheduleStatus === 'OPEN');
        } catch (e) {
            state.isLocked = true;
        }

        if (state.isLocked) {
            if(els.scheduleLockBanner) els.scheduleLockBanner.classList.remove('d-none');
            if(els.addProgrammeBtnMain) els.addProgrammeBtnMain.disabled = true;
        }
    }*/

    // --- Load Data ---
	// ONLY CHANGED PARTS — rest of your file stays SAME

// ================= FIX 1: reloadAll =================
async function reloadAll() {
    // 🔒 SAFE LOADING
    const tr = document.createElement("tr");
    const td = document.createElement("td");
    td.colSpan = 5;
    td.className = "text-center py-5";

    const spinner = document.createElement("div");
    spinner.className = "spinner-border text-primary";

    td.appendChild(spinner);
    tr.appendChild(td);
    els.programmesTableBody.replaceChildren(tr);

    try {
        const [offered, pending, departments, streams] = await Promise.all([
            Api.get('/programmes-offered/data/my'),
            Api.get('/programme-requests/my').catch(() => []),
            Api.get('/institute-departments/data/my'),
            Api.get('/stream-data').catch(() => [])
        ]);

        state.programmesOffered = offered || [];
        state.filteredProgrammes = state.programmesOffered;
        state.pendingRequests = pending || [];
        state.departments = departments || [];
        state.streams = streams || [];

        state.activeProgrammeSet = new Set(
            state.programmesOffered.map(p => `${p.programmeId}_${p.shift || 'NA'}`)
        );
		const badge = document.getElementById('pendingCountBadge');

		if (badge) {
		    const count = state.pendingRequests.filter(r => r.status === 'PENDING').length;
		
		    badge.textContent = count;
		
		    if (count > 0) {
                badge.classList.remove('d-none');
            } else {
                badge.classList.add('d-none');
            }
		}
        renderActiveTable();
        renderPendingTable();
        renderDepartments();
        renderStreams();

    } catch {
        const errTr = document.createElement("tr");
        const errTd = document.createElement("td");
        errTd.colSpan = 5;
        errTd.className = "text-danger text-center py-4";
        errTd.textContent = "Failed to load data. Please refresh.";

        errTr.appendChild(errTd);
        els.programmesTableBody.replaceChildren(errTr);
    }
}

    // --- Filters ---
    function applyFilters() {
        const term = els.filterSearch.value.toLowerCase();
        const level = els.filterLevel.value;
        const streamId = els.filterStream.value;

        state.filteredProgrammes = state.programmesOffered.filter(p => {
            return (p.programmeName.toLowerCase().includes(term)) &&
                   (level === '' || p.programmeLevel === level) &&
                   (streamId === '' || p.streamId.toString() === streamId);
        });

        renderActiveTable();
    }

    function clearFilters() {
        els.filterSearch.value = '';
        els.filterLevel.value = '';
        els.filterStream.value = '';
        applyFilters();
    }

    // --- Dynamic Shift Tabs Logic ---
	function buildShiftTabs() {
    const table = document.getElementById("shiftTable");
    const tabContainer = document.getElementById("shiftTabsContainer");
    if (!table || !tabContainer) return;

    const rows = table.querySelectorAll("tbody tr.programme-row");
    const shifts = new Map();

    rows.forEach(row => {
        shifts.set(
            row.getAttribute("data-shift") || "NA",
            row.getAttribute("data-shift-name") || "Not Applicable"
        );
    });

    // Clear safely
    tabContainer.replaceChildren();

    if (shifts.size <= 1) {
        tabContainer.classList.add("d-none");
        rows.forEach(row => row.classList.remove("d-none"));
        return;
    }

    tabContainer.classList.remove("d-none");

    const ul = document.createElement("ul");
    ul.className = "nav nav-tabs";

    const buttons = [];

    shifts.forEach((name, code) => {
        const li = document.createElement("li");
        li.className = "nav-item";

        const btn = document.createElement("button");
        btn.className = "nav-link";
        btn.type = "button";
        btn.textContent = name;
        btn.dataset.targetShift = code;

        li.appendChild(btn);
        ul.appendChild(li);
        buttons.push(btn);
    });

    tabContainer.appendChild(ul);

    const filterRows = (targetShift) => {
        let count = 1;
        rows.forEach(row => {
            if (row.getAttribute("data-shift") === targetShift) {
                row.classList.remove("d-none");
                const slCell = row.querySelector("td:first-child");
                if (slCell) slCell.textContent = count++;
            } else {
                row.classList.add("d-none");
            }
        });
    };

    buttons.forEach(btn => {
        btn.addEventListener("click", function () {
            buttons.forEach(b => b.classList.remove("active"));
            this.classList.add("active");
            filterRows(this.dataset.targetShift);
        });
    });

    const defaultBtn =
        buttons.find(b => b.dataset.targetShift === "DAY") || buttons[0];

    if (defaultBtn) {
        defaultBtn.classList.add("active");
        filterRows(defaultBtn.dataset.targetShift);
    }
}

    // --- Table Rendering ---
	// ================= FIX 2: renderActiveTable =================
	function renderActiveTable() {
	    const programmes = state.filteredProgrammes || [];
	
	    if (programmes.length === 0) {
	        const tr = document.createElement("tr");
	        const td = document.createElement("td");
	        td.colSpan = 7;
	        td.className = "text-center py-5 text-muted";
	        td.textContent = "No Programmes Match Filters";
	
	        tr.appendChild(td);
	        els.programmesTableBody.replaceChildren(tr);
	
	        if (els.totalCount) els.totalCount.textContent = '0 programmes';
	        if (els.alertBox && state.programmesOffered.length === 0)
	            els.alertBox.classList.remove('d-none');
	        return;
	    }
	
	    if (els.alertBox) els.alertBox.classList.add('d-none');
	    if (els.totalCount) els.totalCount.textContent = `${programmes.length} active`;
	
	    const rows = programmes.map((p, i) => {
	        const tr = document.createElement("tr");
	        tr.className = "programme-row";
	        tr.dataset.shift = p.shift || "NA";
	        tr.dataset.shiftName = p.shiftDisplayName || "Not Applicable";
	
	        const td1 = document.createElement("td");
	        td1.className = "text-center";
	        td1.textContent = i + 1;
	
	        const td2 = document.createElement("td");
	        const nameDiv = document.createElement("div");
	        nameDiv.className = "fw-bold text-dark programme-name";
	        nameDiv.textContent = p.programmeName;
	
	        const shiftDiv = document.createElement("div");
	        shiftDiv.className = "small text-muted";
	        shiftDiv.textContent = "Shift: " + p.shiftDisplayName;
	
	        td2.append(nameDiv, shiftDiv);
	
	        const td3 = document.createElement("td");
	        const dept = document.createElement("span");
	        dept.className = "badge bg-light text-dark";
	        dept.textContent = p.departmentName;
	        td3.appendChild(dept);
	
	        const td4 = document.createElement("td");
	        const level = document.createElement("span");
	        level.className = "badge bg-info text-dark me-1";
	        level.textContent = p.programmeLevel || "N/A";
	        td4.appendChild(level);
	
	        const td5 = document.createElement("td");
	        const stream = document.createElement("span");
	        stream.className = "badge bg-secondary";
	        stream.textContent = p.streamName || "N/A";
	        td5.appendChild(stream);
	
	        const td6 = document.createElement("td");
	        td6.className = "text-center";
	
	        const link = document.createElement("a");
	        link.href = `/institute/programmes/combinations/manage?programmeOfferedId=${p.programmeOfferedId}`;
	        link.className = "btn btn-sm btn-outline-success rounded-pill px-3";
	
	        const icon = document.createElement("i");
	        icon.className = "fas fa-layer-group me-1";
	
	        link.append(icon, document.createTextNode(" Subjects"));
	        td6.appendChild(link);
	
	        const td7 = document.createElement("td");
	        td7.className = "text-center pe-4";
	
	        const editBtn = document.createElement("button");
	        editBtn.className = "btn btn-sm btn-outline-primary me-1 border-0";
	        editBtn.disabled = state.isLocked;
	
	        const editIcon = document.createElement("i");
	        editIcon.className = "fas fa-pen";
	        editBtn.appendChild(editIcon);
	
	        editBtn.addEventListener('click', () => {
	            ProgrammesModule.openEdit(
	                p.programmeOfferedId,
	                p.programmeName,
	                p.instituteDepartmentId,
	                p.shift
	                )
	            });
	
	        const delBtn = document.createElement("button");
	        delBtn.className = "btn btn-sm btn-outline-danger border-0";
	        delBtn.disabled = state.isLocked;
	
	        const delIcon = document.createElement("i");
	        delIcon.className = "fas fa-trash";
	        delBtn.appendChild(delIcon);
	
	        delBtn.addEventListener('click', () => {
	            ProgrammesModule.openDelete(
	                p.programmeOfferedId,
	                p.programmeName,
	                p.shift,
	                p.shiftDisplayName
	            )
	            });
	
	        td7.append(editBtn, delBtn);
	
	        tr.append(td1, td2, td3, td4, td5, td6, td7);
	        return tr;
	    });
	
	    els.programmesTableBody.replaceChildren(...rows);
	
	    buildShiftTabs(); // ✅ untouched
	}

	// ================= FIX 3: renderPendingTable =================
	function renderPendingTable() {
	    const requests = state.pendingRequests || [];
	
	    if (requests.length === 0) {
	        const tr = document.createElement("tr");
	        const td = document.createElement("td");
	        td.colSpan = 5;
	        td.className = "text-center py-4 text-muted";
	        td.textContent = "No requests found.";
	
	        tr.appendChild(td);
	        els.pendingTableBody.replaceChildren(tr);
	        return;
	    }
	
	    const rows = requests.map(r => {
	        const tr = document.createElement("tr");
	
	        const td1 = document.createElement("td");
	        td1.className = "ps-4 fw-bold";
	        td1.textContent = r.programmeName;
	
	        const td2 = document.createElement("td");
	        td2.textContent = r.programmeLevel;
	
	        const td3 = document.createElement("td");
	        td3.textContent = r.streamName;
	
	        const td4 = document.createElement("td");
	        const badge = document.createElement("span");
	
	        if (r.status === 'APPROVED') badge.className = "badge bg-success";
	        else if (r.status === 'REJECTED') badge.className = "badge bg-danger";
	        else badge.className = "badge bg-warning text-dark";
	
	        badge.textContent = r.status;
	        td4.appendChild(badge);
	
	        const td5 = document.createElement("td");
	        td5.className = "small text-muted";
	        td5.textContent = r.createdAt
	            ? new Date(r.createdAt).toLocaleDateString()
	            : "N/A";
	
	        tr.append(td1, td2, td3, td4, td5);
	        return tr;
	    });
	
	    els.pendingTableBody.replaceChildren(...rows);
	}

  	function renderDepartments() {
	    const targets = [
	        els.departmentSelect,
	        els.editDepartmentSelect,
	        els.requestDepartmentSelect
	    ];
	
	    targets.forEach(select => {
	        if (!select) return;
	
	        select.replaceChildren();
	
	        const placeholder = document.createElement("option");
	        placeholder.value = "";
	        placeholder.textContent = "Select a department...";
	        select.appendChild(placeholder);
	
	        state.departments
	            .filter(d => d.active)
	            .forEach(d => {
	                const opt = document.createElement("option");
	                opt.value = d.instituteDepartmentId;
	                opt.textContent = d.departmentName;
	                select.appendChild(opt);
	            });
	    });
	}

    function renderStreams() {
	    if (els.streamSelect) {
	        els.streamSelect.replaceChildren();
	
	        const placeholder = document.createElement("option");
	        placeholder.value = "";
	        placeholder.textContent = "Select Stream...";
	        els.streamSelect.appendChild(placeholder);
	
	        state.streams.forEach(s => {
	            const opt = document.createElement("option");
	            opt.value = s.streamId;
	            opt.textContent = s.streamName;
	            els.streamSelect.appendChild(opt);
	        });
	    }
	
	    if (els.filterStream) {
	        els.filterStream.replaceChildren();
	
	        const all = document.createElement("option");
	        all.value = "";
	        all.textContent = "All Streams";
	        els.filterStream.appendChild(all);
	
	        state.streams.forEach(s => {
	            const opt = document.createElement("option");
	            opt.value = s.streamId;
	            opt.textContent = s.streamName;
	            els.filterStream.appendChild(opt);
	        });
	    }
	}

    // --- Master Programme Selection & Shifts ---
    async function loadMasterProgrammesList() {
        document.querySelectorAll('#shiftCheckboxGroup input[type="checkbox"]')
	    .forEach(cb => {
	        cb.disabled = true;
	        cb.checked = false;
	    });
	    
        if (els.submitBtn) els.submitBtn.disabled = true;

        if (state.allProgrammes.length === 0) {
            setProgrammeMessage("Loading programmes...");
            try {
                state.allProgrammes = await Api.get('/programme-data') || [];
                state.availableProgrammes = state.allProgrammes;
                setupSearch();
            } catch (e) {
                return;
            }
        }
        renderRadios(state.availableProgrammes);
    }

    function renderRadios(list) {
	    const container = els.programmeCheckboxContainer;
	    if (!container) return;
	
	    container.replaceChildren();
	
	    if (list.length === 0) {
	        const div = document.createElement("div");
	        div.className = "text-muted text-center py-3";
	        div.textContent = "No programmes match your search.";
	        container.appendChild(div);
	        return;
	    }
	
	    const nodes = list.map(p => {
	        const div = document.createElement("div");
	        div.className = "form-check py-1 border-bottom";
	
	        const input = document.createElement("input");
	        input.className = "form-check-input programme-checkbox";
	        input.type = "checkbox";
	        input.value = p.programmeId;
	
	        const label = document.createElement("label");
	        label.className = "form-check-label w-100 cursor-pointer";
	        label.textContent = `${p.programmeName} (${p.programmeLevel})`;
	
	        div.append(input, label);
	
	        input.addEventListener("change", handleProgrammeSelection);
	
	        return div;
	    });
	
	    container.replaceChildren(...nodes);
	}

    function setupSearch() {
        const input = $id('programmeSearchInput');
        // Remove old listeners cleanly by cloning node
        const newInput = input.cloneNode(true);
        input.parentNode.replaceChild(newInput, input);

        newInput.addEventListener('input', (e) => {
            const term = e.target.value.toLowerCase();
            state.availableProgrammes = state.allProgrammes.filter(p => p.programmeName.toLowerCase().includes(term));
            renderRadios(state.availableProgrammes);
            els.shiftSelect.disabled = true;
            els.submitBtn.disabled = true;
        });
    }

    function handleProgrammeSelection(e) {
        const progId = e.target.value;
        enableShiftCheckboxes();
        els.submitBtn.disabled = true;

        let availCount = 0;
        const checkboxes = document.querySelectorAll('#shiftCheckboxGroup input[type="checkbox"]');
		
		checkboxes.forEach(cb => {
		    const shift = cb.value;
		
		    if (state.activeProgrammeSet.has(`${progId}_${shift}`)) {
		        cb.disabled = true;
		        cb.checked = false;
		    } else {
		        cb.disabled = false;
		        availCount++;
		    }
		});

        const help = document.getElementById('shiftHelpText');
		if (help) {
		    help.replaceChildren();
		
		    if (availCount === 0) {
		        const span = document.createElement("span");
		        span.className = "text-danger fw-bold";
		        span.textContent = "All shifts assigned for this programme!";
		        help.appendChild(span);
		    }
		}
    }

    // --- Form Submissions ---
    async function handleAddExisting(e) {
        e.preventDefault();
        const fd = new FormData(e.target);
        const selectedProgrammes = Array.from(
    	document.querySelectorAll('.programme-checkbox:checked')).map(cb => Number(cb.value));
        if (!fd.get('instituteDepartmentId') || selectedProgrammes.length === 0 || !fd.getAll('shift[]').length) return;
        try {
            els.submitBtn.disabled = true;
            await Api.post('/programmes-offered/data', {
				instituteDepartmentId: Number(fd.get('instituteDepartmentId')),
                programmeIds: selectedProgrammes,
                shift: fd.getAll('shift[]')
            });
            showToast('success', `Programme added successfully!`);
            reloadPageSafely();
        } catch (err) {
            showToast('error', 'Failed to add programme.');
            els.submitBtn.disabled = false;
        }
    }

    async function handleRequestSubmit(e) {
        e.preventDefault();
        if (!e.target.checkValidity()) {
            return e.target.classList.add('was-validated');
        }

        const btn = e.target.querySelector('button[type="submit"]');
        btn.disabled = true;

        try {
            const fd = new FormData(e.target);
            await Api.post('/programme-requests/submit', {
                programmeName: fd.get('programmeName'),
                programmeLevel: fd.get('programmeLevel'),
                streamId: Number(fd.get('streamId')),
                instituteDepartmentId: Number(fd.get('instituteDepartmentId'))
            });
            showToast('success', 'Request submitted successfully!');
            reloadPageSafely();
        } catch (err) {
            showToast('error', 'Failed to submit request.');
            btn.disabled = false;
        }
    }

    async function handleEdit(evt) {
        evt.preventDefault();
        if (!state.editing || !evt.target.checkValidity()) return;

        try {
            evt.target.querySelector('button[type=submit]').disabled = true;
            const fd = new FormData(evt.target);
            await Api.put(`/programmes-offered/data/${state.editing.id}`, {
                instituteDepartmentId: Number(fd.get('instituteDepartmentId')),
                programmeIds: [Number(state.editing.progId)],
                shift: fd.getAll('shift[]')
            });
            reloadPageSafely();
        } catch (err) {
            showToast('error', 'Failed to update details.');
        }
    }

  	async function confirmDelete() {
	    if (!state.deleting?.id) return;
	
	    if (state.isLocked) return;
	    state.isLocked = true;
	
	    try {
	        els.confirmDeleteBtn.disabled = true;
	        const selectedOption = document.querySelector('input[name="deleteOption"]:checked')?.value || 'single';
	
	        await Api.delete(`/programmes-offered/data/${state.deleting.id}?shiftType=${selectedOption}`);
	
	        showToast(
	            'success',
	            selectedOption === 'all'
	                ? 'All shifts deleted successfully.'
	                : 'Shift deleted successfully.'
	        );
	
	        reloadPageSafely();
	
	    } catch (err) {
	        showToast('error', 'Failed to delete.');
	        els.confirmDeleteBtn.disabled = false;
	        state.isLocked = false;
	    }
	}
	
	//Helper Message 
	
	function setProgrammeMessage(text, type = "muted") {
	    const container = els.programmeCheckboxContainer;
	    if (!container) return;
	
	    container.replaceChildren();
	
	    const div = document.createElement("div");
	    div.className = `text-center py-3 text-${type}`;
	    div.textContent = text;
	
	    container.appendChild(div);
	}
	
	//Populate Programme's List
	
	async function handleDepartmentChange(e) {
    const deptId = e.target.value;

    // Reset UI
    setProgrammeMessage("Loading programmes...");

    document.querySelectorAll('#shiftCheckboxGroup input[type="checkbox"]')
        .forEach(cb => {
            cb.disabled = true;
            cb.checked = false;
        });

    if (els.submitBtn) els.submitBtn.disabled = true;

    if (!deptId) {
        setProgrammeMessage("Select a Department First");
        return;
    }

    try {
        const programmes = await Api.get(`/programme-data?departmentId=${deptId}`) || [];
   		state.availableProgrammes = programmes;

        renderRadios(programmes);

    } catch (err) {
        setProgrammeMessage("Failed to load programmes.", "danger");
    }
}

    // Reveal public methods
    return {
        init,
        clearFilters,

		openEdit: async (id, name, deptId) => {
		    if (state.isLocked) return;
		
		    const list = await Api.get(`/programmes-offered/data/${id}`);
		    if (!Array.isArray(list) || list.length === 0) return;
		
		    const first = list[0];
		    state.editing = { id, progId: first.programmeId };
		
		    $id('editProgrammeName').textContent = name;
		    els.editDepartmentSelect.value = deptId || '';
		    const shifts = list.map(item => String(item.shift).toUpperCase());

		    document.querySelectorAll('#shiftCheckboxGroup input[type="checkbox"]').forEach(cb => {
		        cb.disabled = false;
		        cb.checked = shifts.includes(cb.value.toUpperCase()); 
		    });
		
		    modals.edit.show();
		},

        openDelete: (id, name, shift, shiftName) => {
		    if (state.isLocked) return;		
		    state.deleting = { id, shift };
		    $id('programmeToDelete').textContent = name;
		    $id('deleteShiftName').textContent = shiftName || shift;
		    modals.delete.show();
		}
    };
})();

document.addEventListener('DOMContentLoaded', ProgrammesModule.init);
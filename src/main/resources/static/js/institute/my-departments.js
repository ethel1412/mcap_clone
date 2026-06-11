const DepartmentsModule = (() => {

    // --- CSRF & API Helpers ---
    const getCsrfHeaders = () => {
        const token = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
        const header = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
        return (token && header) ? { [header]: token } : {};
    };

    const Api = {
        get: (url) => axios.get(url).then(r => r.data),
        post: (url, data) => axios.post(url, data, { headers: { ...getCsrfHeaders() } }),
        put: (url, data) => axios.put(url, data, { headers: { ...getCsrfHeaders() } }),
        delete: (url) => axios.delete(url, { headers: { ...getCsrfHeaders() } })
    };

    // --- State Management ---
    const state = {
        myDepartments: [],
        filteredDepartments: [],
        pendingRequests: [],
        allDepartments: [],
        editing: null,
        deleting: null,
        isLocked: false
    };

    // --- DOM Elements & Modals ---
    const els = {};
    const modals = { add: null, request: null, edit: null, delete: null };

    function $id(id) {
        return document.getElementById(id);
    }

    // --- Initialization ---
    function init() {
        // Cache DOM elements
        ['departmentsTableBody', 'pendingTableBody', 'totalCount', 'pendingBadge', 'alertBox',
         'departmentSelect', 'selectedDeptCode', 'addDepartmentForm', 'requestDepartmentForm', 'editDepartmentForm',
         'addDepartmentModal', 'requestDepartmentModal', 'editDepartmentModal', 'deleteConfirmModal',
         'scheduleLockBanner', 'addDeptBtnMain', 'filterSearchDept'
        ].forEach(id => els[id] = $id(id));

        // Initialize Modals
        if(els.addDepartmentModal) modals.add = new bootstrap.Modal(els.addDepartmentModal);
        if(els.requestDepartmentModal) modals.request = new bootstrap.Modal(els.requestDepartmentModal);
        if(els.editDepartmentModal) modals.edit = new bootstrap.Modal(els.editDepartmentModal);
        if(els.deleteConfirmModal) modals.delete = new bootstrap.Modal(els.deleteConfirmModal);

        // Bind Form Events
        if (els.addDepartmentForm) els.addDepartmentForm.addEventListener('submit', handleAddExistingSubmit);
        if (els.requestDepartmentForm) els.requestDepartmentForm.addEventListener('submit', handleRequestNewSubmit);
        if (els.editDepartmentForm) els.editDepartmentForm.addEventListener('submit', handleEditSubmit);
        if ($id('confirmDeleteBtn')) $id('confirmDeleteBtn').addEventListener('click', confirmDelete);

        // Bind Search Filter
        if (els.filterSearchDept) els.filterSearchDept.addEventListener('input', applyFilters);

        // THE FIX: Listen for Dropdown Changes to display the specific Department Code
        if (els.departmentSelect) {
            els.departmentSelect.addEventListener('change', (e) => {
                const opt = e.target.options[e.target.selectedIndex];
                const code = opt ? opt.getAttribute('data-code') : '';

                if (code && els.selectedDeptCode) {
                    els.selectedDeptCode.replaceChildren();					
					const icon = document.createElement("i");
					icon.className = "fas fa-tag me-1";					
					const text = document.createTextNode(" Department Code: ");					
					const span = document.createElement("span");
					span.textContent = code;					
					els.selectedDeptCode.append(icon, text, span);
					els.selectedDeptCode.classList.remove('d-none');
                } else if (els.selectedDeptCode) {
                    els.selectedDeptCode.classList.add('d-none');
                }
            });
        }
        
          const addBtn = document.getElementById('addDeptBtnMain');
		  if (addBtn) {
		    addBtn.addEventListener('click', () => {
		      DepartmentsModule.openAddModal();
		    });
		  }
		
		  const requestLink = document.getElementById('requestDeptLink');
		  if (requestLink) {
		    requestLink.addEventListener('click', (e) => {
		      e.preventDefault();
		      DepartmentsModule.switchToRequestModal();
		    });
		  }
		
		  const backBtn = document.getElementById('backToAddDeptBtn');
		  if (backBtn) {
		    backBtn.addEventListener('click', () => {
		      DepartmentsModule.openAddModal();
		    });
		  }

        reloadAll();
    }

    // --- Load Data ---
    async function reloadAll() {
        showLoading();

        try {
            const [my, pending, all] = await Promise.all([
                Api.get('/institute-departments/data/my'),
                Api.get('/department-requests/my'),
                Api.get('/institute-departments/data/departments')
            ]);

            state.myDepartments = my || [];
            state.filteredDepartments = state.myDepartments;
            state.pendingRequests = pending || [];
            state.allDepartments = all || [];

            renderActiveTable();
            renderPendingTable();
            renderDropdown();

        } catch (err) {
            if(els.departmentsTableBody) {
                const tr = document.createElement("tr");
				const td = document.createElement("td");
				
				td.colSpan = 6;
				td.className = "text-center text-danger py-4";
				td.textContent = "Failed to load data.";
				
				tr.appendChild(td);
				els.departmentsTableBody.replaceChildren(tr);
            }
        }
    }

    function showLoading() {
	    if (!els.departmentsTableBody) return;
	    els.departmentsTableBody.replaceChildren();
	
	    const tr = document.createElement("tr");
	
	    const td = document.createElement("td");
	    td.colSpan = 6;
	    td.className = "text-center py-5";
	
	    const spinner = document.createElement("div");
	    spinner.className = "spinner-border text-primary";
	
	    td.appendChild(spinner);
	    tr.appendChild(td);
	
	    els.departmentsTableBody.appendChild(tr);
	}
	
    // --- Filters ---
    function applyFilters() {
        const term = els.filterSearchDept.value.toLowerCase();
        state.filteredDepartments = state.myDepartments.filter(d =>
            d.departmentName.toLowerCase().includes(term) ||
            (d.departmentCode && d.departmentCode.toLowerCase().includes(term))
        );
        renderActiveTable();
    }

    // --- Table Rendering ---
    function renderActiveTable() {
        const list = state.filteredDepartments;

        if (list.length === 0) {
        	const tr = document.createElement("tr");
			const td = document.createElement("td");		
			td.colSpan = 6;
			td.className = "text-center py-5 text-muted";
			td.textContent = "No departments found.";			
			tr.appendChild(td);
			els.departmentsTableBody.replaceChildren(tr);
            if (els.totalCount) els.totalCount.textContent = '0 departments';
            if (els.alertBox && state.myDepartments.length === 0) els.alertBox.classList.remove('d-none');
            return;
        }

        if(els.alertBox) els.alertBox.classList.add('d-none');
        if(els.totalCount) els.totalCount.textContent = `${list.length} department${list.length !== 1 ? 's' : ''}`;

        els.departmentsTableBody.replaceChildren();
		(list || []).forEach((dept, i) => {
		    const tr = document.createElement("tr");
		    const td1 = document.createElement("td");
		    td1.className = "text-center";
		    td1.textContent = i + 1;
		    const td2 = document.createElement("td");
		    td2.className = "fw-bold text-primary";
		    td2.textContent = dept.departmentName || "";
		    
		    const td3 = document.createElement("td");
		    const codeBadge = document.createElement("span");
		    codeBadge.className = "badge bg-light text-dark border";
		    codeBadge.textContent = dept.departmentCode || "-";
		    td3.appendChild(codeBadge);
		
		    const td4 = document.createElement("td");
		    td4.className = "small";
		
		    if (dept.hodName) {
		        const div = document.createElement("div");
		        const icon = document.createElement("i");
		        icon.className = "fas fa-user text-muted me-1";
		        div.append(icon, document.createTextNode(dept.hodName));
		        td4.appendChild(div);
		    }
		
		    if (dept.email) {
		        const div = document.createElement("div");
		        const icon = document.createElement("i");
		        icon.className = "fas fa-envelope text-muted me-1";
		        div.append(icon, document.createTextNode(dept.email));
		        td4.appendChild(div);
		    }
		
		    if (dept.phone) {
		        const div = document.createElement("div");
		        const icon = document.createElement("i");
		        icon.className = "fas fa-phone text-muted me-1";
		        div.append(icon, document.createTextNode(dept.phone));
		        td4.appendChild(div);
		    }
		

		    const td5 = document.createElement("td");
		    td5.className = "text-center";
		
		    const statusBadge = document.createElement("span");
		    statusBadge.className = `badge ${dept.active ? "bg-success" : "bg-secondary"}`;
		    statusBadge.textContent = dept.active ? "Active" : "Inactive";
		
		    td5.appendChild(statusBadge);

		    const td6 = document.createElement("td");
		    td6.className = "text-center";
		
		    // Edit button
		    const editBtn = document.createElement("button");
		    editBtn.className = "btn btn-sm btn-outline-primary me-1";
		    editBtn.disabled = state.isLocked;
		
		    const editIcon = document.createElement("i");
		    editIcon.className = "fas fa-pen";
		    editBtn.appendChild(editIcon);
		
		    editBtn.addEventListener("click", () => {
		        DepartmentsModule.openEditModal(dept.instituteDepartmentId);
		    });
		
		    // Delete button
		    const delBtn = document.createElement("button");
		    delBtn.className = "btn btn-sm btn-outline-danger";
		    delBtn.disabled = state.isLocked;
		
		    const delIcon = document.createElement("i");
		    delIcon.className = "fas fa-trash";
		    delBtn.appendChild(delIcon);
		
		    delBtn.addEventListener("click", () => {
		        DepartmentsModule.openDeleteModal(
		            dept.instituteDepartmentId,
		            dept.departmentName || ""
		        );
		    });
		
		    td6.append(editBtn, delBtn);
		
		    // ===== Append row =====
		    tr.append(td1, td2, td3, td4, td5, td6);
		    els.departmentsTableBody.appendChild(tr);
		});
    }

    function renderPendingTable() {
        const list = state.pendingRequests;

        if(els.pendingBadge) {
            const pendingCount = list.filter(r => r.status === 'PENDING').length;
            els.pendingBadge.textContent = pendingCount;
            pendingCount > 0 ? els.pendingBadge.classList.remove('d-none') : els.pendingBadge.classList.add('d-none');
        }

        if (list.length === 0) {
            const tr = document.createElement("tr");
			const td = document.createElement("td");			
			td.colSpan = 6;
			td.className = "text-center py-5 text-muted";
			td.textContent = "No requests found.";			
			tr.appendChild(td);
			els.pendingTableBody.replaceChildren(tr);
            return;
        }

       	els.pendingTableBody.replaceChildren();
		(list || []).forEach((req, i) => {
		    const tr = document.createElement("tr");

		    const td1 = document.createElement("td");
		    td1.className = "text-center";
		    td1.textContent = i + 1;
		

		    const td2 = document.createElement("td");
		    td2.className = "fw-bold";
		    td2.textContent = req.departmentName || "";

		    const td3 = document.createElement("td");
		    td3.textContent = req.departmentCode || "-";
		

		    const td4 = document.createElement("td");
		    td4.textContent = req.hodName || "-";

		    const td5 = document.createElement("td");
		    td5.className = "small text-muted";
		    td5.textContent = req.createdAt
		        ? new Date(req.createdAt).toLocaleDateString()
		        : "-";

		    const td6 = document.createElement("td");
		    td6.className = "text-center";
		
		    const badge = document.createElement("span");
		
		    if (req.status === "APPROVED") {
		        badge.className = "badge bg-success";
		        badge.textContent = "Approved";
		    } else if (req.status === "REJECTED") {
		        badge.className = "badge bg-danger";
		        badge.textContent = "Rejected";
		    } else {
		        badge.className = "badge bg-warning text-dark";
		        badge.textContent = "Pending";
		    }
		
		    td6.appendChild(badge);
		
		    // ===== Append row =====
		    tr.append(td1, td2, td3, td4, td5, td6);
		    els.pendingTableBody.appendChild(tr);
		});
    }

    function renderDropdown() {
        const assignedIds = state.myDepartments.map(d => d.departmentId);
        const pendingNames = state.pendingRequests.map(r => r.departmentName.toLowerCase());

        const available = state.allDepartments.filter(d =>
            !assignedIds.includes(d.departmentId) &&
            !pendingNames.includes(d.departmentName.toLowerCase())
        );

       els.departmentSelect.replaceChildren();

if (available.length === 0) {
    const opt = document.createElement("option");
    opt.value = "";
    opt.textContent = "All available departments added";

    els.departmentSelect.appendChild(opt);
    els.departmentSelect.disabled = true;

} else {
    els.departmentSelect.disabled = false;

    // Default option
    const defaultOpt = document.createElement("option");
    defaultOpt.value = "";
    defaultOpt.textContent = "-- Select Department --";
    els.departmentSelect.appendChild(defaultOpt);

    // Sorted options (same logic)
    available
        .sort((a, b) => a.departmentName.localeCompare(b.departmentName))
        .forEach(d => {
            const opt = document.createElement("option");

            opt.value = d.departmentId;
            opt.setAttribute("data-code", d.departmentCode || "");

            // Same label logic as before
            opt.textContent = d.departmentCode
                ? `${d.departmentName} (${d.departmentCode})`
                : d.departmentName;

            els.departmentSelect.appendChild(opt);
        });
}
    }

    // --- Actions & Modals ---
    function openAddModal() {
        if(modals.request) modals.request.hide();
        els.addDepartmentForm.reset();

        // Hide the dynamic code label when opening a fresh modal
        if (els.selectedDeptCode) els.selectedDeptCode.classList.add('d-none');

        if(modals.add && !state.isLocked) modals.add.show();
    }

    function switchToRequestModal() {
        if(modals.add) modals.add.hide();
        els.requestDepartmentForm.reset();
        if(modals.request && !state.isLocked) modals.request.show();
    }

    async function handleAddExistingSubmit(e) {
        e.preventDefault();
        const fd = new FormData(e.target);
        try {
            await Api.post('/institute-departments/data', {
                departmentId: fd.get('departmentId'),
                hodName: fd.get('hodName'),
                email: fd.get('email'),
                phone: fd.get('phone'),
                active: fd.get('active') === 'true'
            });
            showToast('success', 'Added successfully.');
            modals.add.hide();
            reloadAll();
        } catch (err) {
            showToast('error', 'Failed to add.');
        }
    }

    async function handleRequestNewSubmit(e) {
        e.preventDefault();
        const fd = new FormData(e.target);
        try {
            await Api.post('/department-requests/submit', {
                departmentName: fd.get('newDepartmentName'),
                departmentCode: fd.get('newDepartmentCode'),
                hodName: fd.get('hodName'),
                email: fd.get('email'),
                phone: fd.get('phone')
            });
            showToast('success', 'Request sent.');
            modals.request.hide();
            reloadAll();
        } catch (err) {
		    const msg = err?.response?.data?.message || 'Failed to request.';
		    showToast('error', msg);
		}
    }

    function openEditModal(id) {
        if (state.isLocked) return;
        state.editing = state.myDepartments.find(d => d.instituteDepartmentId === id);

        // THE FIX: Also show the Code alongside the Department Name when Editing
        const codeDisplay = state.editing.departmentCode ? ` (${state.editing.departmentCode})` : '';
        $id('editDepartmentName').value = state.editing.departmentName + codeDisplay;

        $id('editHodName').value = state.editing.hodName || '';
        $id('editEmail').value = state.editing.email || '';
        $id('editPhone').value = state.editing.phone || '';
        $id('editActive').value = String(state.editing.active);

        modals.edit.show();
    }

    async function handleEditSubmit(e) {
        e.preventDefault();
        const fd = new FormData(e.target);
        try {
            await Api.put(`/institute-departments/data/${state.editing.instituteDepartmentId}`, {
                hodName: fd.get('hodName'),
                email: fd.get('email'),
                phone: fd.get('phone'),
                active: fd.get('active') === 'true'
            });
            showToast('success', 'Updated.');
            modals.edit.hide();
            reloadAll();
        } catch(err) {
            showToast('error', 'Update failed.');
        }
    }

    function openDeleteModal(id, name) {
        if (state.isLocked) return;
        state.deleting = id;
        $id('departmentToDelete').textContent = name;
        modals.delete.show();
    }

    async function confirmDelete() {
        try {
            await Api.delete(`/institute-departments/data/${state.deleting}`);
            modals.delete.hide();
            reloadAll();
        } catch(err) {
            showToast('error', 'Remove failed. Department may be in use.');
        }
    }

    function showToast(type, msg) {
        const el = $id(type === 'success' ? 'successToast' : 'errorToast');
        const body = $id(type === 'success' ? 'successMessage' : 'errorMessage');
        if(el && body) {
            body.textContent = msg;
            new bootstrap.Toast(el).show();
        }
    }

    // Reveal public methods
    return {
        init,
        openAddModal,
        switchToRequestModal,
        openEditModal,
        openDeleteModal
    };
})();

document.addEventListener('DOMContentLoaded', DepartmentsModule.init);
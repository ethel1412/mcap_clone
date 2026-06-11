const SeatApprovals = (() => {

    // --- State Management ---
    let currentAdmissionId = null;

    // --- DOM Elements ---
    const els = {
        sectionWindows: document.getElementById('window-selection-section'),
        sectionTable: document.getElementById('approval-section'),
        windowsGrid: document.getElementById('windows-grid'),
        windowsLoading: document.getElementById('windows-loading'),
        noWindows: document.getElementById('no-windows'),
        tbody: document.getElementById('approvalTableBody'),
        backBtn: document.getElementById('btnBackToWindows'),
        windowTitle: document.getElementById('selectedWindowName'),
        breadcrumb: document.getElementById('breadcrumbCurrent'),
        searchInput: document.getElementById('tableSearchInput'),
        noSearchResults: document.getElementById('noSearchResults'),
        previewModal: new bootstrap.Modal(document.getElementById('previewModal')),
        resList: document.getElementById('reservationList'),
        toast: new bootstrap.Toast(document.getElementById('statusToast')),
        toastMsg: document.getElementById('toastMessage'),
        toastEl: document.getElementById('statusToast')
    };

    // --- Helpers ---

    const getCsrfHeaders = () => {
        const token = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
        const header = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
        return (token && header) ? { [header]: token } : {};
    };

    function formatDate(dateString) {
        if (!dateString || dateString === 'N/A') return 'N/A';
        const date = new Date(dateString);
        return date.toLocaleDateString('en-GB', {
            day: '2-digit',
            month: 'short',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit',
            hour12: true
        });
    }

    function init() {
        loadWindows();
    }

    // --- Window Selection Logic ---

    async function loadWindows() {
        try {
            const res = await axios.get('/controller/seat-approvals/windows');
            renderWindows(res.data || []);
        } catch (e) {
            console.error(e);
            els.windowsLoading.style.display = 'none';
            els.noWindows.style.display = 'block';
            els.noWindows.textContent = 'Failed to load admission windows.';
        }
    }

    function renderWindows(windows) {
        els.windowsLoading.style.display = 'none';

        if (windows.length === 0) {
            els.noWindows.style.display = 'block';
            els.noWindows.textContent = "No active admission windows found.";
            return;
        }

        els.windowsGrid.style.display = 'flex';
    	els.windowsGrid.replaceChildren();

		windows.forEach(w => {
		
		    let btnClass = 'btn-secondary';
		    let btnText = '';
		    let cardOpacity = '';
		    let clickable = false;
		
		    let scheduleBadgeText = '';
		    let scheduleBadgeClass = '';
		    let scheduleIconClass = '';
		
		    if (w.scheduleStatus === 'OPEN') {
		        scheduleBadgeText = 'Approvals Open';
		        scheduleBadgeClass = 'success';
		        scheduleIconClass = 'fa-unlock';
		        btnClass = 'btn-primary';
		        btnText = 'Manage Approvals';
		        clickable = true;
		
		    } else if (w.scheduleStatus === 'UPCOMING') {
		        scheduleBadgeText = 'Starts Soon';
		        scheduleBadgeClass = 'warning';
		        scheduleIconClass = 'fa-clock';
		        btnText = `Opens: ${formatDate(w.scheduleStart)}`;
		
		    } else if (w.scheduleStatus === 'CLOSED') {
		        scheduleBadgeText = 'Approvals Closed';
		        scheduleBadgeClass = 'danger';
		        scheduleIconClass = 'fa-lock';
		        btnText = 'Approval Period Ended';
		        cardOpacity = 'opacity-75';
		
		    } else {
		        scheduleBadgeText = 'Not Scheduled';
		        scheduleBadgeClass = 'secondary';
		        btnText = 'Schedule Not Set';
		        cardOpacity = 'opacity-75';
		    }
		
		    const col = document.createElement("div");
		    col.className = "col-md-6 col-lg-4";
		
		    const card = document.createElement("div");
		    card.className = `card h-100 shadow-sm admission-card ${cardOpacity}`;
		
		    const body = document.createElement("div");
		    body.className = "card-body";
		
		    // header
		    const header = document.createElement("div");
		    header.className = "d-flex justify-content-between align-items-center mb-3";
		
		    const activeBadge = document.createElement("span");
		    activeBadge.className = "badge bg-success";
		    activeBadge.textContent = "Window Active";
		
		    const pendingBadge = document.createElement("span");
		    if (w.pendingCount > 0) {
		        pendingBadge.className = "badge bg-danger rounded-pill";
		        pendingBadge.textContent = `${w.pendingCount} Pending`;
		    } else {
		        pendingBadge.className = "badge bg-light text-muted border";
		        pendingBadge.textContent = "All Clear";
		    }
		
		    header.append(activeBadge, pendingBadge);
		
		    // title
		    const title = document.createElement("h5");
		    title.className = "card-title fw-bold text-dark mb-3";
		    title.textContent = w.name;
		
		    // schedule box
		    const box = document.createElement("div");
		    box.className = "bg-light p-3 rounded mb-3 border";
		
		    const top = document.createElement("div");
		    top.className = "d-flex justify-content-between align-items-center mb-2";
		
		    const small = document.createElement("small");
		    small.className = "text-uppercase fw-bold text-muted";
		    small.textContent = "Approval Schedule";
		
		    const badge = document.createElement("span");
		    badge.className = `badge bg-${scheduleBadgeClass} bg-opacity-10 text-${scheduleBadgeClass} border border-${scheduleBadgeClass}`;
		    badge.textContent = scheduleBadgeText;
		
		    top.append(small, badge);
		
		    const start = document.createElement("div");
		    start.className = "small";
		    start.textContent = `Start: ${formatDate(w.scheduleStart)}`;
		
		    const end = document.createElement("div");
		    end.className = "small";
		    end.textContent = `End: ${formatDate(w.scheduleEnd)}`;
		
		    box.append(top, start, end);
		
		    // button
		    const btn = document.createElement("button");
		    btn.className = `btn ${btnClass} btn-sm w-100 py-2 fw-bold`;
		    btn.textContent = btnText;
		
		    if (!clickable) {
		        btn.disabled = true;
		    } else {
		        btn.addEventListener("click", () => {
		            SeatApprovals.selectWindow(w.admissionId, w.name);
		        });
		    }
		
		    body.append(header, title, box, btn);
		    card.appendChild(body);
		    col.appendChild(card);
		
		    els.windowsGrid.appendChild(col);
		});
    }

    // --- Window Navigation ---

    async function selectWindow(id, name) {
        try {
            const res = await axios.get(`/controller/seat-approvals/check-schedule/${id}`);
            if (!res.data.active) {
                showToast(res.data.message, 'warning');
                return;
            }
            currentAdmissionId = id;
            els.windowTitle.textContent = name;
            els.breadcrumb.textContent = 'Approvals for: ' + name;
            els.searchInput.value = '';
            els.noSearchResults.classList.add('d-none');
            els.sectionWindows.style.display = 'none';
            els.sectionTable.classList.remove('d-none');
            els.backBtn.classList.remove('d-none');
            loadTableData(id);
        } catch (e) {
            showToast(e.response?.data?.message || 'Error checking status', 'danger');
        }
    }

    function showWindows() {
        currentAdmissionId = null;
        els.breadcrumb.textContent = 'Select Admission Window';
        els.sectionTable.classList.add('d-none');
        els.backBtn.classList.add('d-none');
        els.sectionWindows.style.display = 'block';
        const tr = document.createElement("tr");
		const td = document.createElement("td");
		td.colSpan = 5;
		td.className = "text-center py-4";
		td.textContent = "Loading...";
		tr.appendChild(td);
		els.tbody.replaceChildren(tr);
        // Reload windows to update counts
        loadWindows();
    }

    // --- Table Data & Rendering ---

    async function loadTableData(admissionId) {
       // loading
		const tr = document.createElement("tr");
		const td = document.createElement("td");
		td.colSpan = 5;
		td.className = "text-center py-4";
		
		const spinner = document.createElement("div");
		spinner.className = "spinner-border text-primary";
		
		td.appendChild(spinner);
		tr.appendChild(td);
		els.tbody.replaceChildren(tr);
		
       	try {
            const res = await axios.get(`/controller/seat-approvals/all?admissionId=${admissionId}`);
            renderTable(res.data || []);
        } catch (e) {
			const tr = document.createElement("tr");
			const td = document.createElement("td");
			td.colSpan = 5;
			td.className = "text-center text-danger";
			td.textContent = "Failed to load data.";
			tr.appendChild(td);
			els.tbody.replaceChildren(tr);
        }
    }

  	function renderTable(data) {
	    els.tbody.replaceChildren();
	
	    if (data.length === 0) {
	        const tr = document.createElement("tr");
	        const td = document.createElement("td");
	        td.colSpan = 5;
	        td.className = "text-center py-5 text-muted";
	        td.textContent = "No Seat Allocations Found";
	        tr.appendChild(td);
	        els.tbody.appendChild(tr);
	        return;
	    }
	
	    data.forEach(row => {
	        const tr = document.createElement("tr");
	        tr.id = `row-${row.seatMatrixId}`;
	        tr.className = "data-row";
	
	        const td1 = document.createElement("td");
	        td1.className = "fw-bold text-primary approval-institute-name";
	        td1.textContent = row.instituteName;
	
	        const td2 = document.createElement("td");
	        td2.textContent = row.programmeName;

	        const td3 = document.createElement("td");
	        td3.textContent = row.streamName;
	
	        const td4 = document.createElement("td");
	        td4.className = "text-center";
	        td4.textContent = row.totalSeats;
	
	        const td5 = document.createElement("td");
	        td5.className = "text-center";
	        td5.id = `action-${row.seatMatrixId}`;
	
	        const status = row.status ? row.status.toUpperCase() : 'PENDING';
	
	        if (status === 'APPROVED') {
	            const span = document.createElement("span");
	            span.className = "badge bg-success";
	            span.textContent = "Approved";
	            td5.appendChild(span);
	
	        } else if (status === 'REJECTED') {
	            const span = document.createElement("span");
	            span.className = "badge bg-danger";
	            span.textContent = "Rejected";
	            td5.appendChild(span);
	
	        } else {
	            const btnView = document.createElement("button");
	            btnView.className = "btn btn-sm btn-info me-1";
	            btnView.title = "View";
	            const eyeIcon = document.createElement("i");
				eyeIcon.className = "fas fa-eye";
				btnView.appendChild(eyeIcon);
	            btnView.addEventListener("click", () => {
	                SeatApprovals.preview(row.programmeOfferedId, row.programmeName, row.totalSeats);
	            });
	
	            const btnApprove = document.createElement("button");
	            btnApprove.className = "btn btn-sm btn-success me-1";
	            btnApprove.title = "Approve";
	            const checkIcon = document.createElement("i");
				checkIcon.className = "fas fa-check";
				btnApprove.appendChild(checkIcon);
	            btnApprove.addEventListener("click", () => SeatApprovals.approve(row.seatMatrixId));
	
	            const btnReject = document.createElement("button");
	            btnReject.className = "btn btn-sm btn-danger";
	            btnReject.title = "Reject";
	            const crossIcon = document.createElement("i");
				crossIcon.className = "fas fa-times";
				btnReject.appendChild(crossIcon);
	            btnReject.addEventListener("click", () => SeatApprovals.reject(row.seatMatrixId));
	
	            td5.append(btnView, btnApprove, btnReject);
	        }
	
	        tr.append(td1, td2, td3, td4, td5);
	        els.tbody.appendChild(tr);
    	});
	}

    function filterTable() {
        const filter = els.searchInput.value.toLowerCase();
        const rows = els.tbody.getElementsByClassName('data-row');
        let hasVisible = false;

        Array.from(rows).forEach(row => {
            const text = row.innerText.toLowerCase();
            if (text.includes(filter)) {
                row.style.display = '';
                hasVisible = true;
            } else {
                row.style.display = 'none';
            }
        });

        if (!hasVisible && rows.length > 0) {
            els.noSearchResults.classList.remove('d-none');
        } else {
            els.noSearchResults.classList.add('d-none');
        }
    }

    // --- Actions ---

    async function preview(poId, progName, total) {
    document.getElementById('previewProgrammeName').textContent = progName;
    document.getElementById('previewTotalSeats').textContent = total;

    // Loading state
    els.resList.replaceChildren();
    const loadingLi = document.createElement("li");
    loadingLi.className = "list-group-item text-center";
    loadingLi.textContent = "Loading breakdown...";
    els.resList.appendChild(loadingLi);

    els.previewModal.show();

    try {
        const res = await axios.get(`/controller/seat-approvals/preview/${poId}`);
        const reservations = res.data || [];
        let totalReserved = 0;

        // Clear loading
        els.resList.replaceChildren();

        if (reservations.length === 0) {
            const li = document.createElement("li");
            li.className = "list-group-item text-muted text-center";

            const icon = document.createElement("i");
            icon.className = "fas fa-info-circle me-2";

            li.append(icon, document.createTextNode("No specific reservations. All seats are Open/General."));
            els.resList.appendChild(li);

        } else {
            reservations.forEach(r => {
                totalReserved += r.seatCount;

                const li = document.createElement("li");
                li.className = "list-group-item d-flex justify-content-between align-items-center";

                const name = document.createElement("span");
                name.textContent = r.categoryName || r.categoryCode;

                const badge = document.createElement("span");
                badge.className = "badge bg-secondary rounded-pill";
                badge.textContent = r.seatCount;

                li.append(name, badge);
                els.resList.appendChild(li);
            });
        }

        document.getElementById('previewTotalReserved').textContent = totalReserved;

    } catch (e) {
        els.resList.replaceChildren();

        const li = document.createElement("li");
        li.className = "list-group-item text-danger";
        li.textContent = "Failed to load details";

        els.resList.appendChild(li);
    }
}

    async function approve(id) {
        if (!confirm('Approve this allocation?')) return;
        try {
            await axios.post(`/controller/seat-approvals/${id}/approve`, {}, {
                headers: getCsrfHeaders()
            });
            const el = document.getElementById(`action-${id}`);
			el.replaceChildren();
			
			const span = document.createElement("span");
			span.className = "badge bg-success";
			
			const icon = document.createElement("i");
			icon.className = "fas fa-check me-1";
			
			span.append(icon, document.createTextNode(" Approved"));
			el.appendChild(span);
            showToast('Approved');
        } catch (e) {
            showToast('Failed', 'danger');
        }
    }

    async function reject(id) {
        const r = prompt("Reason:");
        if (!r) return;

        const fd = new FormData();
        fd.append('reason', r);

        try {
            await axios.post(`/controller/seat-approvals/${id}/reject`, fd, {
                headers: getCsrfHeaders()
            });
            const el = document.getElementById(`action-${id}`);
			el.replaceChildren();
			
			const span = document.createElement("span");
			span.className = "badge bg-danger";
			
			const icon = document.createElement("i");
			icon.className = "fas fa-times me-1";
			
			span.append(icon, document.createTextNode(" Rejected"));
			el.appendChild(span);
            showToast('Rejected', 'warning');
        } catch (e) {
            showToast('Failed', 'danger');
        }
    }

    function showToast(msg, type = 'success') {
        els.toastMsg.textContent = msg;
        els.toastEl.className = `toast align-items-center text-white border-0 bg-${type}`;
        els.toast.show();
    }

    // --- Public API ---
    return {
        init,
        loadWindows,
        selectWindow,
        showWindows,
        filterTable,
        preview,
        approve,
        reject
    };
})();

// Initialize on Load
document.addEventListener('DOMContentLoaded', SeatApprovals.init);
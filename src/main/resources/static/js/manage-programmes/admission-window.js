document.addEventListener('DOMContentLoaded', () => {
    AdmissionWindow.init();
});

const AdmissionWindow = (() => {
    
    // ===== CSRF Token Setup =====
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');

    if (csrfToken && csrfHeader) {
        axios.defaults.headers.common[csrfHeader] = csrfToken;
    }
    axios.defaults.withCredentials = true;

    // ===== DOM Elements =====
    const els = {
        grid: document.getElementById('windows-grid'),
        loading: document.getElementById('windows-loading'),
        noWindows: document.getElementById('no-windows'),
        alertBox: document.getElementById('alert-box')
    };

    function init() {
        loadInstituteWindows();
    }

    function showLoading() {
        // CSP FIX: Replaced .style with .classList
        if (els.loading) els.loading.classList.remove('d-none');
        if (els.grid) els.grid.classList.add('d-none');
        if (els.noWindows) els.noWindows.classList.add('d-none');
    }

    function hideLoading() {
        if (els.loading) els.loading.classList.add('d-none');
    }

    function showAlert(message, type = 'danger') {
        if (!els.alertBox) return;
        els.alertBox.innerHTML = `
            <div class="alert alert-${type} alert-dismissible fade show shadow-sm" role="alert">
                <i class="bi bi-exclamation-triangle-fill me-2"></i>${message}
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        `;
    }

    function formatDate(dateStr) {
        if (!dateStr || dateStr === 'N/A') return 'N/A';
        const d = new Date(dateStr);
        if (Number.isNaN(d.getTime())) return dateStr;
        return d.toLocaleDateString('en-IN', {
            day: '2-digit', month: 'short', year: 'numeric',
            hour: '2-digit', minute: '2-digit', hour12: true
        });
    }

    async function loadInstituteWindows() {
        showLoading();
        try {
            const response = await axios.get('/manage-programmes-data/institute/windows');
            const windows = response.data || [];
            renderWindowCards(windows);
        } catch (error) {
            console.error('Error loading windows', error);
            hideLoading();
            showAlert('Failed to load admission windows. Please try again later.');
            if (els.noWindows) {
                els.noWindows.classList.remove('d-none');
                els.noWindows.innerHTML = 'Error loading data.';
            }
        }
    }

    function renderWindowCards(windows) {
        hideLoading();

        if (!windows || windows.length === 0) {
            if (els.noWindows) els.noWindows.classList.remove('d-none');
            return;
        }

        if (els.grid) {
            els.grid.classList.remove('d-none');
 			els.grid.replaceChildren();

			windows.forEach(w => {

			    let statusBadgeEl;
			    let btnClass = 'btn-secondary';
			    let btnDisabled = true;
			    let btnText = '';
			    let cardOpacity = '';

			    // --- STATUS LOGIC ---
			    if (w.scheduleStatus === 'OPEN') {
			        statusBadgeEl = document.createElement("span");
			        statusBadgeEl.className = "badge bg-success bg-opacity-10 text-success border border-success";
			        const icon = document.createElement("i");
			        icon.className = "bi bi-unlock-fill me-1";
			        statusBadgeEl.append(icon, document.createTextNode(" Data Entry Open"));
			        btnClass = 'btn-primary';
			        btnDisabled = false;
			        btnText = "Configure Seat Matrix";
			    } else if (w.scheduleStatus === 'UPCOMING') {
			        statusBadgeEl = document.createElement("span");
			        statusBadgeEl.className = "badge bg-warning bg-opacity-10 text-warning border border-warning";
			        const icon = document.createElement("i");
			        icon.className = "bi bi-clock-history me-1";
			        statusBadgeEl.append(icon, document.createTextNode(" Locked (Starts Soon)"));
			        btnText = `Opens: ${formatDate(w.scheduleStart)}`;
			    } else if (w.scheduleStatus === 'CLOSED') {
			        statusBadgeEl = document.createElement("span");
			        statusBadgeEl.className = "badge bg-danger bg-opacity-10 text-danger border border-danger";
			        const icon = document.createElement("i");
			        icon.className = "bi bi-lock-fill me-1";
			        statusBadgeEl.append(icon, document.createTextNode(" Locked (Closed)"));
			        btnText = 'Deadline Passed';
			        cardOpacity = 'opacity-75';
			    } else {
			        statusBadgeEl = document.createElement("span");
			        statusBadgeEl.className = "badge bg-secondary bg-opacity-10 text-secondary border border-secondary";
			        statusBadgeEl.textContent = "Not Scheduled";
			        btnText = 'Awaiting Controller Setup';
			        cardOpacity = 'opacity-75';
			    }

			    // --- STRUCTURE ---
			    const col = document.createElement("div");
			    col.className = "col-12 col-md-6 col-lg-4";

			    const card = document.createElement("div");
			    card.className = `card h-100 shadow-sm admission-card ${cardOpacity}`;

			    const body = document.createElement("div");
			    body.className = "card-body";

			    // Header badge
			    const header = document.createElement("div");
			    header.className = "d-flex justify-content-between align-items-center mb-3";

			    const headerBadge = document.createElement("span");
			    headerBadge.className = "badge bg-primary bg-opacity-10 text-primary px-3 py-1 rounded-pill";

			    const headerIcon = document.createElement("i");
			    headerIcon.className = "bi bi-building me-1";

			    headerBadge.append(headerIcon, document.createTextNode(" Admission Window"));
			    header.appendChild(headerBadge);

			    // Title
			    const title = document.createElement("h5");
			    title.className = "card-title fw-bold text-dark mb-1";
			    title.textContent = w.streamName;

			    // Subtitle
			    const sub = document.createElement("p");
			    sub.className = "text-muted small fw-semibold mb-3";
			    sub.textContent = `${w.programmeLevel} • Session ${w.session || 'N/A'}`;

			    // Info box
			    const box = document.createElement("div");
			    box.className = "p-3 bg-light border rounded mb-3";

			    const boxHeader = document.createElement("div");
			    boxHeader.className = "d-flex justify-content-between align-items-center mb-3";

			    const phase = document.createElement("span");
                // CSP FIX: Replaced .style.fontSize with fs-0-85rem
			    phase.className = "text-dark fw-bold fs-0-85rem";
			    phase.textContent = "Institute Setup Phase";

			    boxHeader.append(phase, statusBadgeEl);

			    // Dates
			    const openRow = document.createElement("div");
			    openRow.className = "d-flex align-items-center mb-2 small";

			    const openLabel = document.createElement("span");
                // CSP FIX: Replaced .style.width & .style.fontSize with classes
			    openLabel.className = "text-muted fw-bold text-uppercase w-50px fs-0-7rem";
			    openLabel.textContent = "Opens";

			    const openVal = document.createElement("span");
			    openVal.className = "text-dark fw-semibold ms-2";
			    openVal.textContent = formatDate(w.scheduleStart);

			    openRow.append(openLabel, openVal);

			    const closeRow = document.createElement("div");
			    closeRow.className = "d-flex align-items-center small";

			    const closeLabel = document.createElement("span");
                // CSP FIX: Replaced .style.width & .style.fontSize with classes
			    closeLabel.className = "text-muted fw-bold text-uppercase w-50px fs-0-7rem";
			    closeLabel.textContent = "Closes";
			
			    const closeVal = document.createElement("span");
			    closeVal.className = "text-dark fw-semibold ms-2";
			    closeVal.textContent = formatDate(w.scheduleEnd);
			
			    closeRow.append(closeLabel, closeVal);
			
			    box.append(boxHeader, openRow, closeRow);
			
			    // Button
			    const btn = document.createElement("button");
			    btn.type = "button";
			    btn.className = `btn ${btnClass} shadow-sm rounded-pill w-100 py-2 fw-bold`;
			
			    if (btnDisabled) {
			        btn.disabled = true;
			        btn.textContent = btnText;
			    } else {
			        const icon = document.createElement("i");
			        icon.className = "bi bi-sliders2-vertical me-2";
			        btn.append(icon, document.createTextNode(" " + btnText));
			
			        btn.addEventListener("click", () => {
			            window.location.href = `/admission-window/institute/${w.admissionCode}/programmes`;
			        });
			    }
			
			    // Assemble
			    body.append(header, title, sub, box, btn);
			    card.appendChild(body);
			    col.appendChild(card);
			    els.grid.appendChild(col);
			});
        }
    }

    return { init };
})();
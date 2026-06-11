document.addEventListener("DOMContentLoaded", async () => {
    "use strict";

    // --- DOM Elements ---
    const prospectusUrlInput = document.getElementById('prospectusUrlInput');
    const saveProspectusBtn = document.getElementById('saveProspectusBtn');
    const modalEl = document.getElementById('prospectusModal');

    const addLinkBtn = document.getElementById('addLinkBtn');
    const editLinkGroup = document.getElementById('editLinkGroup');
    const viewLinkBtn = document.getElementById('viewLinkBtn');
    const editLinkBtn = document.getElementById('editLinkBtn');

    // CSRF Setup
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;
    const csrfToken = document.querySelector('meta[name="_csrf"]').content;
    axios.defaults.headers.common[csrfHeader] = csrfToken;

    async function loadDashboard() {
        try {
            const res = await axios.get("/api/institute/dashboard-data");
            const { institute, stats } = res.data;

            // 1. Populate UI
            document.getElementById('ui-inst-name').textContent = institute.instituteName;
            document.getElementById('ui-inst-status').textContent = institute.status;
            document.getElementById('ui-inst-year').textContent = institute.yearEstablished;
            document.getElementById('ui-inst-aishe').textContent = institute.aisheid || 'N/A';

            document.getElementById('stat-prog').textContent = stats.totalProgrammes || 0;
            document.getElementById('stat-app').textContent = stats.totalApplicants || 0;
            document.getElementById('stat-adm').textContent = stats.admissionsAccepted || 0;

            if (stats.totalApplicants > 0) {
                const rate = (stats.admissionsAccepted * 100.0) / stats.totalApplicants;
                document.getElementById('stat-rate').textContent = rate.toFixed(1) + "%";
            } else {
                document.getElementById('stat-rate').textContent = "0%";
            }

            document.getElementById('ui-inst-head').textContent = institute.institutionHeadDetails || 'N/A';

            if(document.getElementById('ui-inst-email')) {
                document.getElementById('ui-inst-email').textContent = institute.institutionOfficialEmailId;
                document.getElementById('ui-inst-email').href = "mailto:" + institute.institutionOfficialEmailId;
            }

            if(document.getElementById('ui-inst-phone')) {
                document.getElementById('ui-inst-phone').textContent = institute.institutionOfficialContactNumber;
                document.getElementById('ui-inst-phone').href = "tel:" + institute.institutionOfficialContactNumber;
            }

            if(document.getElementById('ui-inst-web') && institute.institutionWebsite) {
                const webEl = document.getElementById('ui-inst-web');
                webEl.textContent = institute.institutionWebsite;
                webEl.href = institute.institutionWebsite.startsWith('http')
                               ? institute.institutionWebsite
                               : 'https://' + institute.institutionWebsite;
            }

            // 2. Render Buttons
            renderProspectusActions(institute.prospectusUrl);

            // 3. Show UI (FIXED FOR CSP: Use CSS classes instead of .style)
            document.getElementById('loading-spinner').classList.add('d-none');
            document.getElementById('main-dashboard-content').classList.remove('d-none');

        } catch (err) {
            console.error("Failed to load dashboard data", err);
        }
    }

    function renderProspectusActions(rawUrl) {
        let isValidUrl = false;
        let cleanUrl = "";

        if (rawUrl && typeof rawUrl === 'string') {
            cleanUrl = rawUrl.trim();
            if (cleanUrl !== "" && cleanUrl.toLowerCase() !== "null" && cleanUrl.startsWith("http")) {
                isValidUrl = true;
            }
        }

        if (!isValidUrl) {
            // FIXED FOR CSP: Use classList
            addLinkBtn.classList.remove('d-none');
            editLinkGroup.classList.add('d-none');

            addLinkBtn.onclick = () => { prospectusUrlInput.value = ''; };
        } else {
            // FIXED FOR CSP: Use classList
            addLinkBtn.classList.add('d-none');
            editLinkGroup.classList.remove('d-none');

            viewLinkBtn.href = cleanUrl;
            editLinkBtn.onclick = () => { prospectusUrlInput.value = cleanUrl; };
        }
    }

    // Modal Save Action
    saveProspectusBtn.onclick = async (e) => {
        e.preventDefault();
        const urlValue = prospectusUrlInput.value.trim();

        if (!urlValue) {
            alert("Please enter a URL.");
            return;
        }

        try {
            saveProspectusBtn.disabled = true;
            saveProspectusBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Saving...';

            await axios.post("/api/institute/prospectus/save", { prospectusUrl: urlValue });

            try {
                const modalInstance = bootstrap.Modal.getInstance(modalEl);
                if (modalInstance) modalInstance.hide();
            } catch(error) {
                // FIXED FOR CSP: Use classList
                modalEl.classList.remove('show');
                modalEl.classList.add('d-none');
                document.body.classList.remove('modal-open');
                document.querySelector('.modal-backdrop')?.remove();
            }

            await loadDashboard();

        } catch (err) {
            console.error("Error saving prospectus link", err);
            alert("Error saving link. Please ensure it starts with http:// or https://");
        } finally {
            saveProspectusBtn.disabled = false;
            saveProspectusBtn.innerHTML = 'Save Link';
        }
    };

    // Initial Load
    loadDashboard();
});
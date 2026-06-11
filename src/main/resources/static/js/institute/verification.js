document.addEventListener('DOMContentLoaded', function () {
    console.log("Verification Dashboard Loaded");

    // --- 1. Configuration & State ---
    const PAGE_SIZE = 10;
    const state = {
        PENDING: { page: 0, statuses: 'PENDING_VERIFICATION', tbody: 'pending-table-body', pag: 'pending-pagination', stats: 'stats-pending' },
        VERIFIED: { page: 0, statuses: 'PENDING,ACCEPTED', tbody: 'verified-table-body', pag: 'verified-pagination', stats: 'stats-verified' },
        REJECTED: { page: 0, statuses: 'INSTITUTE_REJECTED,REJECTED', tbody: 'rejected-table-body', pag: 'rejected-pagination', stats: 'stats-rejected' }
    };

    let currentAllotmentId = null;

    // --- 2. Modal Initialization ---
    const rejectionModal = new bootstrap.Modal(document.getElementById('rejectionModal'));
    const verificationModal = new bootstrap.Modal(document.getElementById('verificationConfirmModal')); // Native Bootstrap Modal
    const docListModal = new bootstrap.Modal(document.getElementById('documentReviewModal'));
    const viewerModal = new bootstrap.Modal(document.getElementById('documentViewerModal'));

    const fullResImage = document.getElementById('full-res-image');
    const pdfFrame = document.getElementById('pdf-viewer-frame');
    const imgContainer = document.getElementById('image-viewer-container');
    const viewerTitle = document.getElementById('viewer-filename');

    // --- 3. Data Loading Logic ---
    const loadData = async (tabKey, page = 0) => {
        const config = state[tabKey];
        config.page = page;

        const tbody = document.getElementById(config.tbody);
        const pagDiv = document.getElementById(config.pag);
        const statsEl = document.getElementById(config.stats);

        // PROGRAMME FILTER: Get selected value
        const programmeFilter = document.getElementById('programmeFilter');
        const selectedProgId = programmeFilter ? programmeFilter.value : "";

        tbody.replaceChildren();
        const tr = document.createElement("tr");
        const td = document.createElement("td");
        td.colSpan = 5;
        td.className = "text-center py-4";

        const spinner = document.createElement("div");
        spinner.className = "spinner-border text-primary spinner-border-sm";

        td.appendChild(spinner);
        tr.appendChild(td);
        tbody.appendChild(tr);

        try {
            // Prepare request parameters with dynamic programme filter
            const requestParams = { page: page, size: PAGE_SIZE, statuses: config.statuses };
            if (selectedProgId) {
                requestParams.programmeId = selectedProgId;
            }

            const res = await axios.get(`/api/institute/allotments/paged`, {
                params: requestParams
            });

            const paged = res.data;
            renderTable(tbody, paged.data, tabKey);
            renderPagination(pagDiv, paged, tabKey);
            if (statsEl) statsEl.textContent = paged.totalElements;

        } catch (err) {
            console.error("Fetch Error:", err);
            tbody.replaceChildren();
            const trErr = document.createElement("tr");
            const tdErr = document.createElement("td");
            tdErr.colSpan = 5;
            tdErr.className = "text-center text-danger";
            tdErr.textContent = "Failed to load data.";

            trErr.appendChild(tdErr);
            tbody.appendChild(trErr);
        }
    };

    const renderTable = (tbody, items, tabKey) => {
        tbody.replaceChildren();

        if (!items || items.length === 0) {
            const tr = document.createElement("tr");
            const td = document.createElement("td");
            td.colSpan = 5;
            td.className = "text-center py-5 text-muted";
            td.textContent = `No ${tabKey.toLowerCase()} records found.`;
            tr.appendChild(td);
            tbody.appendChild(tr);
            return;
        }

        items.forEach((item, idx) => {
            const rowNum = (state[tabKey].page * PAGE_SIZE) + (idx + 1);

            const tr = document.createElement("tr");

            // 1. Index
            const td1 = document.createElement("td");
            td1.className = "text-muted small";
            td1.textContent = rowNum;

            // 2. Applicant Info
            const td2 = document.createElement("td");
            const nameDiv = document.createElement("div");
            nameDiv.className = "fw-bold text-dark";
            nameDiv.textContent = item.applicantName;

            const small = document.createElement("small");
            small.className = "text-muted";
            small.textContent = item.applicationNo;

            td2.append(nameDiv, small);

            // 3. Programme & Phase
            const td3 = document.createElement("td");
            const prog = document.createElement("div");
            prog.textContent = item.programmeName;

            const phase = document.createElement("small");
            phase.className = "text-info";
            phase.textContent = item.roundAndPhase;

            td3.append(prog, phase);

            // 4. Status / Remarks
            const td4 = document.createElement("td");

            if (tabKey === 'REJECTED') {
                const span = document.createElement("span");
                span.className = "text-danger small fw-bold";
                span.textContent = item.remarks || "No Reason";
                td4.appendChild(span);
            } else {
                const span = document.createElement("span");
                span.className = "badge bg-secondary";
                span.textContent = item.allottedCategory || "OPEN";
                td4.appendChild(span);
            }

            // 5. ACTIONS
            const td5 = document.createElement("td");
            td5.className = "text-center";

            if (tabKey === 'PENDING') {
                const group = document.createElement("div");
                group.className = "btn-group";

                const btnView = createBtn("btn-outline-primary", "bi bi-eye-fill", item);
                btnView.classList.add("view-docs-btn");

                const btnVerify = createBtn("btn-outline-success", "bi bi-check-circle-fill", item);
                btnVerify.classList.add("verify-btn");

                const btnReject = createBtn("btn-outline-danger", "bi bi-x-circle-fill", item);
                btnReject.classList.add("reject-btn");

                group.append(btnView, btnVerify, btnReject);
                td5.appendChild(group);

            } else {
                const btn = createBtn("btn-light text-primary", "bi bi-eye-fill", item);
                btn.classList.add("view-docs-btn");
                td5.appendChild(btn);
            }

            tr.append(td1, td2, td3, td4, td5);
            tbody.appendChild(tr);
        });
    };

    function createBtn(className, iconClass, item) {
        const btn = document.createElement("button");
        btn.className = `btn btn-sm ${className} border-0`;
        btn.dataset.id = item.allotmentId;
        btn.dataset.name = item.applicantName;

        const icon = document.createElement("i");
        icon.className = iconClass;

        btn.appendChild(icon);
        return btn;
    }

    const renderPagination = (container, paged, tabKey) => {
        container.replaceChildren();

        if (paged.totalPages <= 1) return;

        const wrapper = document.createElement("div");
        wrapper.className = "d-flex justify-content-between align-items-center p-3 border-top bg-light";

        const info = document.createElement("span");
        info.className = "small text-muted";
        info.textContent = `Showing page ${paged.page + 1} of ${paged.totalPages}`;

        const nav = document.createElement("nav");
        const ul = document.createElement("ul");
        ul.className = "pagination pagination-sm mb-0";

        const prev = createPageBtn("Previous", paged.page === 0, paged.page - 1, tabKey);
        const next = createPageBtn("Next", paged.last, paged.page + 1, tabKey);

        ul.append(prev, next);
        nav.appendChild(ul);
        wrapper.append(info, nav);

        container.appendChild(wrapper);
    };

    function createPageBtn(text, disabled, page, tabKey) {
        const li = document.createElement("li");
        li.className = `page-item ${disabled ? 'disabled' : ''}`;

        const btn = document.createElement("button");
        btn.className = "page-link";
        btn.textContent = text;
        btn.dataset.page = page;
        btn.dataset.tab = tabKey;

        li.appendChild(btn);
        return li;
    }

    // --- 4. Event Delegation ---
    document.addEventListener('click', async (e) => {
        const btn = e.target.closest('button');
        if (!btn) return;

        const id = btn.dataset.id || btn.dataset.allotmentId;
        const name = btn.dataset.name || btn.dataset.applicantName;

        // Pagination Clicks
        if (btn.classList.contains('page-link')) {
            const tab = btn.dataset.tab;
            const page = parseInt(btn.dataset.page);
            loadData(tab, page);
        }

        // Action: View Docs
        if (btn.classList.contains('view-docs-btn')) {
            viewDocuments(id, name);
        }

        // Action: Verify (Open Bootstrap Modal instead of SweetAlert)
        if (btn.classList.contains('verify-btn')) {
            currentAllotmentId = id;
            document.getElementById('verify-applicant-name').textContent = name;
            verificationModal.show();
        }

        // Action: Reject (Open Bootstrap Modal)
        if (btn.classList.contains('reject-btn')) {
            currentAllotmentId = id;
            document.getElementById('reject-applicant-name').textContent = name;
            document.getElementById('rejectionRemarks').value = '';
            rejectionModal.show();
        }
    });

    // --- FILTER CHANGE LISTENER ---
    const programmeFilterElement = document.getElementById('programmeFilter');
    if (programmeFilterElement) {
        programmeFilterElement.addEventListener('change', () => {
            // Reset all pagination when filter changes
            state.PENDING.page = 0;
            state.VERIFIED.page = 0;
            state.REJECTED.page = 0;

            // Find currently active tab and reload
            const activeTabBtn = document.querySelector('.nav-link.active');
            if (activeTabBtn) {
                const key = activeTabBtn.id.replace('tab-', '').replace('-btn', '').toUpperCase();
                loadData(key, 0);
            }
        });
    }

    // --- 5. Support Functions ---

    // Native fallback alert
    const showAlert = (title, message) => {
        alert(title + ": " + message);
    };

    const performAction = async (id, payload) => {
        const token = document.querySelector('meta[name="_csrf"]')?.content;
        const header = document.querySelector('meta[name="_csrf_header"]')?.content;

        try {
            await axios.post(`/api/institute/allotments/${id}/verify`, payload, {
                headers: { [header]: token }
            });
            showAlert('Success', 'Action recorded successfully.');
            rejectionModal.hide();
            verificationModal.hide();

            // Refresh current views
            loadData('PENDING', state.PENDING.page);
            loadData('VERIFIED', 0);
            loadData('REJECTED', 0);
        } catch (err) {
            showAlert('Error', 'Action failed to process.');
        }
    };

    const viewDocuments = async (id, name) => {
        document.getElementById('review-applicant-name').textContent = name;
        const body = document.getElementById('documentReviewBody');
        body.replaceChildren();
        const wrapper = document.createElement("div");
        wrapper.className = "text-center p-5";

        const spinner = document.createElement("div");
        spinner.className = "spinner-border";

        wrapper.appendChild(spinner);
        body.appendChild(wrapper);
        docListModal.show();

        try {
            const res = await axios.get(`/api/institute/allotments/${id}/document-review`);

            body.replaceChildren();

            const parser = new DOMParser();
            const doc = parser.parseFromString(res.data, "text/html");
            doc.querySelectorAll("script, iframe, object, embed").forEach(el => el.remove());
            doc.querySelectorAll("*").forEach(el => {
                [...el.attributes].forEach(attr => {
                    if (attr.name.startsWith("on")) {
                        el.removeAttribute(attr.name);
                    }
                });
            });

            Array.from(doc.body.childNodes).forEach(node => {
                body.appendChild(node);
            });

            body.querySelectorAll('.clickable-preview').forEach(el => {
                el.addEventListener('click', () => {
                    const docId = el.dataset.documentId;
                    const isImg = el.querySelector('img') !== null;

                    viewerTitle.textContent = "Document Viewer";

                    if (isImg) {
                        fullResImage.src = `/applicants/documents/${docId}`;
                        imgContainer.classList.remove('d-none');
                        pdfFrame.classList.add('d-none');
                    } else {
                        pdfFrame.src = `/applicants/documents/${docId}`;
                        pdfFrame.classList.remove('d-none');
                        imgContainer.classList.add('d-none');
                    }

                    viewerModal.show();
                });
            });

        } catch (err) {
            body.replaceChildren();
            const div = document.createElement("div");
            div.className = "alert alert-danger";
            div.textContent = "Failed to load docs.";
            body.appendChild(div);
        }
    };

    // Confirm Rejection Button Event
    document.getElementById('confirm-rejection-btn').addEventListener('click', () => {
        const remarks = document.getElementById('rejectionRemarks').value.trim();
        if (!remarks) return showAlert('Wait', 'Please provide a reason for rejection.');
        performAction(currentAllotmentId, { status: 'INSTITUTE_REJECTED', remarks: remarks });
    });

    // Confirm Verification Button Event
    document.getElementById('confirm-verify-btn').addEventListener('click', () => {
        performAction(currentAllotmentId, { status: 'PENDING', remarks: 'Verified by Institute' });
    });

    // Tab switch listener
    document.querySelectorAll('button[data-bs-toggle="tab"]').forEach(btn => {
        btn.addEventListener('shown.bs.tab', (e) => {
            const key = e.target.id.replace('tab-', '').replace('-btn', '').toUpperCase();
            loadData(key, 0);
        });
    });

    // Global Stats Card Click Support
    document.querySelectorAll('.stats-card').forEach(card => {
        card.addEventListener('click', () => {
            const btnId = card.dataset.tabId;
            document.getElementById(btnId).click();
        });
    });

    // Initial Load
    loadData('PENDING', 0);
});
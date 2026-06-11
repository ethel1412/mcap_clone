document.addEventListener("DOMContentLoaded", async function() {
    const wrapper = document.getElementById('summary-app-wrapper');
    if (!wrapper) return; // Guard clause if JS runs on the wrong page

    // CHANGED: Grab the windowCode from the new dataset property
    const winCode = wrapper.dataset.windowCode;
    const progId = wrapper.dataset.programmeId;
    const round = wrapper.dataset.roundType;
    const phase = wrapper.dataset.phaseNo;

    // Setup Back Button
    // CHANGED: admissionWindowId replaced by admissionWindowCode
    document.getElementById('btn-back-manage').href = `/controller/admissions/manage?admissionWindowCode=${winCode}&roundType=${round}&phaseNo=${phase}`;

    const tbody = document.getElementById('summary-table-body');
    const rowTpl = document.getElementById('summary-row-template');
    const emptyTpl = document.getElementById('empty-row-template');
    const tabsContainer = document.getElementById('shift-tabs-container');

    let allOfferings = [];

    try {
        // CHANGED: API endpoint query parameter from windowId to windowCode
        const res = await axios.get(`/seat-allotment-data/api/admin/allotments/summary?windowCode=${winCode}&programmeId=${progId}&roundType=${round}&phaseNo=${phase}`);
        allOfferings = res.data;

        if (allOfferings.length > 0) {
            document.getElementById('display-programme-name').textContent = allOfferings[0].programmeName;
        } else {
            document.getElementById('display-programme-name').textContent = "Programme";
            tbody.appendChild(emptyTpl.content.cloneNode(true));
            return;
        }

        // 1. Find unique shifts
        const shifts = [...new Set(allOfferings.map(o => o.shiftName || 'Day'))];

        // 2. Generate Tabs
        let tabsHtml = '<ul class="nav nav-tabs" role="tablist">';
        shifts.forEach((shift, index) => {
            const activeClass = index === 0 ? 'active' : '';
            tabsHtml += `
                <li class="nav-item" role="presentation">
                    <button class="nav-link ${activeClass} fw-bold text-uppercase px-4 py-3 shift-tab-btn"
                            data-target-shift="${shift}">
                        <i class="bi bi-clock-history me-1"></i> ${shift} SHIFT
                    </button>
                </li>
            `;
        });
        tabsHtml += '</ul>';
        tabsContainer.innerHTML = tabsHtml;

        // 3. Render Table Function
        const renderTable = (selectedShift) => {
            tbody.innerHTML = '';
            const filtered = allOfferings.filter(o => (o.shiftName || 'Day') === selectedShift);

            if (filtered.length === 0) {
                tbody.appendChild(emptyTpl.content.cloneNode(true));
                return;
            }

            filtered.forEach((off, idx) => {
                const clone = rowTpl.content.cloneNode(true);
                clone.querySelector('.row-index').textContent = idx + 1;
                clone.querySelector('.col-institute').textContent = off.instituteName;
                clone.querySelector('.col-shift').textContent = off.shiftName || 'Day';
                clone.querySelector('.col-total').textContent = off.totalSeats;
                clone.querySelector('.col-reserved').textContent = off.reservedSeats;
                clone.querySelector('.col-open').textContent = off.openSeats;
                clone.querySelector('.col-allotted').textContent = off.allottedCount;
                clone.querySelector('.col-unfilled').textContent = off.unfilledSeats;

                // CHANGED: Build the URL using winCode
                clone.querySelector('.btn-view-applicants').href =
                    `/seat-allotment/page/window/${winCode}/programme-offered/${off.programmeOfferedId}?roundType=${round}&phaseNo=${phase}`;

                tbody.appendChild(clone);
            });
        };

        // Initialize first tab
        renderTable(shifts[0]);

        // 4. Attach Tab Events
        tabsContainer.querySelectorAll('.shift-tab-btn').forEach(btn => {
            btn.addEventListener('click', function() {
                tabsContainer.querySelectorAll('.shift-tab-btn').forEach(b => b.classList.remove('active'));
                this.classList.add('active');
                renderTable(this.getAttribute('data-target-shift'));
            });
        });

    } catch (err) {
        console.error("Failed to load summary", err);
        tbody.innerHTML = '<tr><td colspan="9" class="text-center text-danger py-4">Error loading data. Check console.</td></tr>';
    }
});
document.addEventListener("DOMContentLoaded", async function() {
    const wrapper = document.getElementById('applicants-app-wrapper');
    if (!wrapper) return;

    // CHANGED: Grab the windowCode from the new dataset property
    const winCode = wrapper.dataset.windowCode;
    const poId = wrapper.dataset.poId;
    const round = wrapper.dataset.roundType;
    const phase = wrapper.dataset.phaseNo;

    const tbody = document.getElementById('applicants-table-body');
    const rowTpl = document.getElementById('applicant-row-template');
    const emptyTpl = document.getElementById('empty-applicants-template');

    try {
        // CHANGED: API endpoint query parameter from windowId to windowCode
        const res = await axios.get(`/seat-allotment-data/api/admin/allotments/candidates?windowCode=${winCode}&roundType=${round}&phaseNo=${phase}&programmeOfferedId=${poId}`);
        const candidates = res.data;

        document.getElementById('total-count-display').textContent = candidates.length;

        if (!candidates || candidates.length === 0) {
            tbody.appendChild(emptyTpl.content.cloneNode(true));
            return;
        }

        // Populate headers using the first candidate's data
        const first = candidates[0];
        document.getElementById('header-prog-name').textContent = first.programmeName || 'Programme';
        document.getElementById('header-inst-name').textContent = first.instituteName || 'Institute';
        document.getElementById('header-shift-badge').textContent = (first.shiftName || 'Day') + ' Shift';

        // Set up the back button dynamically
        if (first.programmeId) {
            const btnBack = document.getElementById('btn-back-summary');
            // CHANGED: Build the return URL using winCode
            btnBack.href = `/seat-allotment/page/window/${winCode}/programme/${first.programmeId}?roundType=${round}&phaseNo=${phase}`;
            btnBack.classList.remove('disabled');
        }

        // Render rows
        candidates.forEach((cand, idx) => {
            const clone = rowTpl.content.cloneNode(true);
            clone.querySelector('.row-index').textContent = idx + 1;
            clone.querySelector('.col-name').textContent = cand.applicantName;
            clone.querySelector('.col-reg-no').textContent = cand.registrationNumber || 'N/A';
            clone.querySelector('.col-reservation').textContent = cand.reservationUsed || 'OPEN';

            const statusBadge = clone.querySelector('.col-status');
            const status = cand.allotmentStatus || 'PENDING';
            statusBadge.textContent = status;

            // Apply styling based on status
            if (status === 'ACCEPTED' || status === 'PENDING_VERIFICATION') {
                statusBadge.classList.add('bg-success');
            } else if (status === 'REJECTED' || status === 'INSTITUTE_REJECTED') {
                statusBadge.classList.add('bg-danger');
            } else {
                statusBadge.classList.add('bg-warning', 'text-dark');
            }

            tbody.appendChild(clone);
        });

    } catch (err) {
        console.error("Failed to load applicants", err);
        tbody.innerHTML = '<tr><td colspan="5" class="text-center text-danger py-4">Error loading data. Check console.</td></tr>';
    }
});
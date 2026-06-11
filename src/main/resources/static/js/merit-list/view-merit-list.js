// Merit List View JavaScript
const csrfToken  = document.querySelector('meta[name="_csrf"]')?.content;
const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

// Shared URL params
const params = new URLSearchParams(window.location.search);
// CHANGED: Grab admissionWindowCode instead of admissionWindowId
const admissionWindowCode = params.get('admissionWindowCode');
const streamId = params.get('streamId');
const programmeId = params.get('programmeId');
const programmeLevel = params.get('programmeLevel'); // "UG" or "PG"

document.addEventListener('DOMContentLoaded', () => {
    initializePage();
    initCuetTab();
    initNonCuetTab();
    loadCuetPreferences();
    loadNonCuetPreferences();
    loadProgrammeEligibilityList(); // Programme eligibility grid
    initCriteriaButton();           //  criteria view button
});

function initializePage() {
    setupPrintButton();
    highlightTopRanks();
    highlightTieBreakerRows();
}

// Setup Print Button
function setupPrintButton() {
    const printBtn = document.getElementById('printBtn');
    if (printBtn) {
        printBtn.addEventListener('click', () => window.print());
    }
}

// Highlight Top 3 Ranks
function highlightTopRanks() {
    const rows = document.querySelectorAll('tbody tr');
    rows.forEach((row) => {
        const rankCell = row.querySelector('.rank-column');
        if (!rankCell) return;

        const rank = parseInt(rankCell.textContent);
        if (rank === 1) {
            row.classList.add('table-warning');
            rankCell.textContent = '🥇 ' + rank;
        } else if (rank === 2) {
            row.classList.add('table-info');
            rankCell.textContent = '🥇 ' + rank;
        } else if (rank === 3) {
            row.classList.add('table-success');
            rankCell.textContent = '🥇 ' + rank;
        }
    });
}

// Highlight rows where tie-breakers were applied
function highlightTieBreakerRows() {
    const rows = document.querySelectorAll('tbody tr');
    rows.forEach((row) => {
        const tbCell = row.querySelector('.tie-breaker-cell .badge');
        if (tbCell && tbCell.textContent.trim()) {
            row.classList.add('table-active');
        }
    });
}

// ================= VIEW ADMISSION CRITERIA (NEW) ===================

function initCriteriaButton() {
    const btn = document.getElementById('btn-view-criteria');
    // CHANGED: Check admissionWindowCode
    if (!btn || !admissionWindowCode || !programmeId) return;

    btn.addEventListener('click', () => {
        loadAdmissionCriteriaForCurrentProgramme();
    });
}

async function loadAdmissionCriteriaForCurrentProgramme() {
    const loading = document.getElementById('criteria-loading');
    const content = document.getElementById('criteria-content');
    const errorBox = document.getElementById('criteria-error');
    const tbList = document.getElementById('crit-tb-list');
    const tbEmpty = document.getElementById('crit-tb-empty');

    if (!loading || !content || !errorBox) {
        return;
    }

    loading.style.display = 'block';
    content.style.display = 'none';
    errorBox.style.display = 'none';

    if (tbList) {
        tbList.style.display = 'none';
        tbList.replaceChildren();
    }
    if (tbEmpty) {
        tbEmpty.style.display = 'block';
    }

    try {
        const level = (programmeLevel || 'UG').toUpperCase();

        // Correct base path + segment from AdmissionCriteriaDataController
        const endpoint = level === 'UG'
            ? '/admission-criteria/data/ug'
            : '/admission-criteria/data/pg';

        // CHANGED: Pass admissionWindowCode to API
        const url = `${endpoint}?admissionWindowCode=${encodeURIComponent(admissionWindowCode)}`
                  + `&programmeId=${encodeURIComponent(programmeId)}`;


        const resp = await fetch(url);
        if (!resp.ok) {
            throw new Error('HTTP ' + resp.status);
        }
        const crit = await resp.json();
        if (!crit) {
            throw new Error('No criteria configured');
        }

        // Basic info
        const progNameEl = document.getElementById('criteria-programme-name');
        const levelEl = document.getElementById('criteria-level');
        if (progNameEl) progNameEl.textContent = crit.programmeName || 'Programme';
        if (levelEl) levelEl.textContent = crit.programmeLevel || level;

        function safeParseJsonArray(v) {
            if (!v) return [];
            if (Array.isArray(v)) return v;
            if (typeof v === 'string') {
                try { return JSON.parse(v); } catch (e) { return []; }
            }
            return [];
        }

        const subjectsList = document.getElementById('subjects-list');
 		if (subjectsList) {
    const withEntranceSubjects = safeParseJsonArray(
        crit.cuetMeritSubjectsJson ?? crit.cuetMeritSubjects ?? crit.withEntranceSubjects
    );

    const withoutEntranceSubjects = safeParseJsonArray(
        crit.nonCuetMeritSubjectsJson ?? crit.nonCuetMeritSubjects ?? crit.withoutEntranceSubjects
    );

    subjectsList.replaceChildren();

    const createSection = (title, arr, badgeClass) => {
        const wrapper = document.createElement("div");
        wrapper.className = "mb-2";

        const heading = document.createElement("div");
        heading.className = "fw-semibold mb-1";
        heading.textContent = title;

        wrapper.appendChild(heading);

        arr.forEach(s => {
            const badge = document.createElement("span");
            badge.className = `badge ${badgeClass} me-1 mb-1`;
            badge.textContent = s;
            wrapper.appendChild(badge);
        });

        return wrapper;
    };

    if (withEntranceSubjects.length === 0 && withoutEntranceSubjects.length === 0) {
        const div = document.createElement("div");
        div.className = "text-muted";
        div.textContent = "No subjects configured.";
        subjectsList.appendChild(div);
    } else {
        if (withEntranceSubjects.length) {
            subjectsList.appendChild(
                createSection("WITH_ENTRANCE (CUET)", withEntranceSubjects, "bg-info text-dark")
            );
        }

        if (withoutEntranceSubjects.length) {
            subjectsList.appendChild(
                createSection("WITHOUT_ENTRANCE (Board/Class XII)", withoutEntranceSubjects, "bg-warning text-dark")
            );
        }
    }
}

        const activeBadge = document.getElementById('crit-active-badge');
        if (activeBadge) {
            if (crit.active) {
                activeBadge.className = 'badge bg-success';
                activeBadge.textContent = 'Active';
            } else {
                activeBadge.className = 'badge bg-secondary';
                activeBadge.textContent = 'Inactive';
            }
        }

        // Tie-breaker config
        if (Array.isArray(crit.tiebreakerConfig) && crit.tiebreakerConfig.length > 0 && tbList) {
            if (tbEmpty) tbEmpty.style.display = 'none';
            tbList.style.display = 'block';

            crit.tiebreakerConfig.forEach((rule, idx) => {
                const li = document.createElement('li');
                li.className = 'list-group-item d-flex justify-content-between align-items-center';
                const span = document.createElement("span");
				const strong = document.createElement("strong");
				strong.textContent = (idx + 1) + ". ";
				
				span.appendChild(strong);
				
				span.appendChild(document.createTextNode("Field: "));
				
				const code1 = document.createElement("code");
				code1.textContent = rule.field;
				
				span.appendChild(code1);
				span.appendChild(document.createTextNode(" | Order: "));
				
				const code2 = document.createElement("code");
				code2.textContent = rule.priority ?? (idx + 1);
				
				span.appendChild(code2);
				
				li.appendChild(span);
                tbList.appendChild(li);
            });
        }

        loading.style.display = 'none';
        content.style.display = 'block';
    } catch (err) {
        console.error('Failed to load admission criteria', err);
        loading.style.display = 'none';
        errorBox.style.display = 'block';
    }
}

// ================= CUET TAB ===================

function initCuetTab() {
    const btn = document.getElementById('btn-generate-cuet');
    // CHANGED: Check admissionWindowCode
    if (!btn || !admissionWindowCode || !programmeId) return;

    // CHANGED: Passed admissionWindowCode instead of ID
    const checkUrl = programmeLevel === 'UG'
        ? `/merit-list/data/check/ug?admissionWindowCode=${admissionWindowCode}&programmeId=${programmeId}`
        : `/merit-list/data/check/pg?admissionWindowCode=${admissionWindowCode}&programmeId=${programmeId}`;

    fetch(checkUrl)
        .then(r => r.json())
        .then(data => {
            const status = document.getElementById('status-cuet');
            if (data.canGenerate) btn.removeAttribute('disabled');
            else btn.setAttribute('disabled', 'disabled');
            if (status) {
                status.textContent = data.hasMeritList
                    ? 'Merit list already generated.'
                    : 'Merit list not generated yet.';
            }
        })
        .catch(() => {
            const status = document.getElementById('status-cuet');
            if (status) status.textContent = 'Failed to load CUET summary.';
        });

    btn.addEventListener('click', () => {
        btn.setAttribute('disabled', 'disabled');
        const originalText = btn.textContent;
		btn.textContent = 'Generating...';

        const generateUrl = programmeLevel === 'UG'
            ? '/merit-list/data/generate/ug'
            : '/merit-list/data/generate/pg';

        // CHANGED: Passed admissionWindowCode in body
        const body =
            `admissionWindowCode=${encodeURIComponent(admissionWindowCode)}&programmeId=${encodeURIComponent(programmeId)}`;

            fetch(generateUrl, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                    [csrfHeader]: csrfToken
                },
                body
            })

            .then(r => r.json())
            .then(() => {
                const status = document.getElementById('status-cuet');
                if (status) status.textContent = 'Merit list generated successfully. Reloading...';
              setTimeout(() => window.location.reload(), 1000);
            })
            .catch(() => {
                const status = document.getElementById('status-cuet');
                if (status) status.textContent = 'Failed to generate merit list.';
                btn.textContent = originalText;
                btn.removeAttribute('disabled');
            });
    });
}


// ================= NON‑CUET TAB ===================

function initNonCuetTab() {
    const btn = document.getElementById('btn-generate-noncuet');
    // CHANGED: Check admissionWindowCode
    if (!btn || !admissionWindowCode || !programmeId) return;

    // CHANGED: Passed admissionWindowCode instead of ID
    const checkUrl = programmeLevel === 'UG'
        ? `/merit-list/data/check/ug?admissionWindowCode=${admissionWindowCode}&programmeId=${programmeId}`
        : `/merit-list/data/check/pg?admissionWindowCode=${admissionWindowCode}&programmeId=${programmeId}`;

    fetch(checkUrl)
        .then(r => r.json())
        .then(data => {
            const status = document.getElementById('status-noncuet');
            if (data.canGenerate) btn.removeAttribute('disabled');
            else btn.setAttribute('disabled', 'disabled');
            if (status) {
                status.textContent = data.hasMeritList
                    ? 'Merit list already generated.'
                    : 'Merit list not generated yet.';
            }
        })
        .catch(() => {
            const status = document.getElementById('status-noncuet');
            if (status) status.textContent = 'Failed to load Non‑CUET summary.';
        });

    	btn.addEventListener('click', () => {
        btn.setAttribute('disabled', 'disabled');
        const originalText = btn.textContent;
		btn.textContent = 'Generating...';

        const generateUrl = programmeLevel === 'UG'
            ? '/merit-list/data/generate/ug'
            : '/merit-list/data/generate/pg';

        // CHANGED: Passed admissionWindowCode in body
        const body =
            `admissionWindowCode=${encodeURIComponent(admissionWindowCode)}&programmeId=${encodeURIComponent(programmeId)}`;

        fetch(generateUrl, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
                [csrfHeader]: csrfToken
            },
            body
        })

            .then(r => r.json())
            .then(() => {
                const status = document.getElementById('status-noncuet');
                if (status) status.textContent = 'Merit list generated successfully. Reloading...';
                setTimeout(() => window.location.reload(), 1000);
            })
            .catch(() => {
                const status = document.getElementById('status-noncuet');
                if (status) status.textContent = 'Failed to generate merit list.';
                btn.textContent = originalText;
                btn.removeAttribute('disabled');
            });
    });
}


// ================= LOAD CUET PREFERENCES (PROGRAMME‑ONLY) ===================

function loadCuetPreferences() {
    // CHANGED: Check admissionWindowCode
    if (!admissionWindowCode || !programmeId) return;

    const tbody = document.getElementById('cuet-preference-tbody');
    const summary = document.getElementById('cuet-preference-summary');
    const badge = document.getElementById('cuet-count-badge');

    if (!tbody) return;

    // CHANGED: Passed admissionWindowCode
    const url = `/merit-list/data/preferences/programme`
        + `?admissionWindowCode=${admissionWindowCode}`
        + `&programmeId=${programmeId}`
        + `&applicantType=WITH_ENTRANCE`;

    fetch(url)
        .then(r => r.json())
        .then(data => {
            tbody.replaceChildren();

            if (!Array.isArray(data) || data.length === 0) {
               tbody.replaceChildren();
				
				const tr = document.createElement("tr");
				const td = document.createElement("td");
				
				td.colSpan = 7;
				td.className = "text-center text-muted py-4";
				
				const icon = document.createElement("i");
				icon.className = "bi bi-inbox";
				icon.style.fontSize = "3rem";
				icon.style.opacity = "0.3";
				
				const p = document.createElement("p");
				p.className = "mt-2 mb-0";
				p.textContent = "No applicants have selected this programme yet.";
				
				td.append(icon, p);
				tr.appendChild(td);
				tbody.appendChild(tr);
                if (badge) badge.textContent = '0';
                return;
            }

            let eligible = 0;
            let ineligible = 0;

            data.forEach((applicant, index) => {
                if (applicant.isEligible) eligible++;
                else ineligible++;

                const row = document.createElement('tr');
				// index
				const td1 = document.createElement('td');
				td1.className = "text-center";
				td1.textContent = index + 1;
				
				// application no
				const td2 = document.createElement('td');
				td2.textContent = applicant.applicationNo;
				
				// name
				const td3 = document.createElement('td');
				td3.textContent = applicant.applicantName;
				
				// preference
				const td4 = document.createElement('td');
				const prefBadge = document.createElement('span');
				prefBadge.className = "badge bg-secondary";
				prefBadge.textContent = getPreferenceLabel(applicant.preferenceOrder);
				td4.appendChild(prefBadge);
				
				// programme
				const td5 = document.createElement('td');
				td5.textContent = applicant.programmeName;
				
				// eligibility
				const td6 = document.createElement('td');
				const statusBadge = document.createElement('span');
				
				if (applicant.isEligible) {
				    statusBadge.className = "badge bg-success";
				    statusBadge.textContent = "Eligible";
				} else {
				    statusBadge.className = "badge bg-danger";
				    statusBadge.textContent = "Not Eligible";
				}
				
				td6.appendChild(statusBadge);
				
				// empty column
				const td7 = document.createElement('td');
				
				row.append(td1, td2, td3, td4, td5, td6, td7);
				tbody.appendChild(row);
            });

            const totalComplete = data.filter(a => a.isEligible !== null && a.isEligible !== undefined).length;
            const eligibilityRate = totalComplete > 0 ? Math.round((eligible / totalComplete) * 100) : 0;

            document.getElementById('cuet-total-complete').textContent = totalComplete;
            document.getElementById('cuet-eligible').textContent = eligible;
            document.getElementById('cuet-eligibility-rate').textContent = eligibilityRate;

            document.getElementById('cuet-pref-total').textContent = data.length;
            document.getElementById('cuet-pref-eligible').textContent = eligible;
            document.getElementById('cuet-pref-ineligible').textContent = ineligible;
            if (summary) summary.style.display = 'block';

            if (badge) badge.textContent = data.length;
        })
        .catch(err => {
            console.error('Failed to load CUET preferences:', err);
            tbody.replaceChildren();
			const tr = document.createElement("tr");
			const td = document.createElement("td");
			
			td.colSpan = 7;
			td.className = "text-center text-muted py-4";
			td.textContent = "No applicants have selected this programme yet.";
			
			tr.appendChild(td);
			tbody.appendChild(tr);
            if (badge) badge.textContent = '!';
        });
}

// ================= LOAD NON‑CUET PREFERENCES (PROGRAMME‑ONLY) ===================

function loadNonCuetPreferences() {

    if (!admissionWindowCode || !programmeId) return;

    const tbody = document.getElementById('noncuet-preference-tbody');
    const summary = document.getElementById('noncuet-preference-summary');
    const badge = document.getElementById('noncuet-count-badge');

    if (!tbody) return;

    const url = `/merit-list/data/preferences/programme`
        + `?admissionWindowCode=${admissionWindowCode}`
        + `&programmeId=${programmeId}`
        + `&applicantType=WITHOUT_ENTRANCE`;

    fetch(url)
        .then(r => r.json())
        .then(data => {

            // ✅ ALWAYS CLEAR FIRST
            tbody.replaceChildren();

            if (!Array.isArray(data) || data.length === 0) {

                const tr = document.createElement("tr");
                const td = document.createElement("td");

                td.colSpan = 7;
                td.className = "text-center text-muted py-4";

                const icon = document.createElement("i");
                icon.className = "bi bi-inbox";
                icon.style.fontSize = "3rem";
                icon.style.opacity = "0.3";

                const p = document.createElement("p");
                p.className = "mt-2 mb-0";
                p.textContent = "No applicants have selected this programme yet.";

                td.append(icon, p);
                tr.appendChild(td);
                tbody.appendChild(tr);

                if (badge) badge.textContent = '0';
                return;
            }

            let eligible = 0;
            let ineligible = 0;

            data.forEach((applicant, index) => {

                if (applicant.isEligible) eligible++;
                else ineligible++;

                // ✅ FIXED (only once)
                const row = document.createElement('tr');

                const td1 = document.createElement('td');
                td1.className = "text-center";
                td1.textContent = index + 1;

                const td2 = document.createElement('td');
                td2.textContent = applicant.applicationNo;

                const td3 = document.createElement('td');
                td3.textContent = applicant.applicantName;

                const td4 = document.createElement('td');
                const prefBadge = document.createElement('span');
                prefBadge.className = "badge bg-secondary";
                prefBadge.textContent = getPreferenceLabel(applicant.preferenceOrder);
                td4.appendChild(prefBadge);

                const td5 = document.createElement('td');
                td5.textContent = applicant.programmeName;

                const td6 = document.createElement('td');
                const statusBadge = document.createElement('span');

                if (applicant.isEligible) {
                    statusBadge.className = "badge bg-success";
                    statusBadge.textContent = "Eligible";
                } else {
                    statusBadge.className = "badge bg-danger";
                    statusBadge.textContent = "Not Eligible";
                }

                td6.appendChild(statusBadge);

                const td7 = document.createElement('td');

                row.append(td1, td2, td3, td4, td5, td6, td7);
                tbody.appendChild(row);
            });

            const totalComplete = data.filter(a =>
                a.isEligible !== null && a.isEligible !== undefined
            ).length;

            const eligibilityRate =
                totalComplete > 0 ? Math.round((eligible / totalComplete) * 100) : 0;

            document.getElementById('noncuet-total-complete').textContent = totalComplete;
            document.getElementById('noncuet-eligible').textContent = eligible;
            document.getElementById('noncuet-eligibility-rate').textContent = eligibilityRate;

            document.getElementById('noncuet-pref-total').textContent = data.length;
            document.getElementById('noncuet-pref-eligible').textContent = eligible;
            document.getElementById('noncuet-pref-ineligible').textContent = ineligible;

            if (summary) summary.style.display = 'block';
            if (badge) badge.textContent = data.length;
        })
        .catch(err => {

            console.error('Failed to load Non-CUET preferences:', err);

            tbody.replaceChildren();

            const tr = document.createElement("tr");
            const td = document.createElement("td");

            td.colSpan = 7;
            td.className = "text-center text-danger py-4";
            td.textContent = "Failed to load applicant preferences. Please try again.";

            tr.appendChild(td);
            tbody.appendChild(tr);

            if (badge) badge.textContent = '!';
        });
}

// ================= PROGRAMME ELIGIBILITY LIST (GRID) ===================

function loadProgrammeEligibilityList() {
    // CHANGED: Check admissionWindowCode
    if (!admissionWindowCode || !programmeId) return;

    const tbody = document.getElementById('eligibility-list-tbody');
    const summary = document.getElementById('eligibility-list-summary');
    const badge = document.getElementById('eligibility-count-badge');

    if (!tbody) return;

    // For now, mirror CUET applicant type; adjust if you want WITHOUT_ENTRANCE etc.
    const applicantType = 'WITH_ENTRANCE';

    // CHANGED: Passed admissionWindowCode instead of ID
    const url = `/eligibility/data/programme/list`
        + `?admissionWindowCode=${encodeURIComponent(admissionWindowCode)}`
        + `&programmeId=${encodeURIComponent(programmeId)}`
        + `&applicantType=${encodeURIComponent(applicantType)}`;

    fetch(url)
        .then(r => r.json())
        .then(data => {
            tbody.replaceChildren();

            if (!Array.isArray(data) || data.length === 0) {
                const tr = document.createElement("tr");
				const td = document.createElement("td");
				
				td.colSpan = 6;
				td.className = "text-center text-muted py-4";
				
				const icon = document.createElement("i");
				icon.className = "bi bi-inbox";
				icon.style.fontSize = "3rem";
				icon.style.opacity = "0.3";
				
				const p = document.createElement("p");
				p.className = "mt-2 mb-0";
				p.textContent = "No eligibility results available for this programme yet.";
				
				td.append(icon, p);
				tr.appendChild(td);
				tbody.appendChild(tr);
                if (summary) {
                    summary.textContent = 'Total completed: 0, Eligible: 0 (0% eligibility rate)';
                }
                if (badge) badge.textContent = '0';
                return;
            }

            let eligibleCount = 0;

            data.forEach((row, index) => {
		
		    const isEligibleText = (row.eligibilityStatus || '').toLowerCase();
		
		    if (isEligibleText.startsWith('eligible')) {
		        eligibleCount++;
		    }
		
		    const tr = document.createElement('tr');
		
		    // index
		    const td1 = document.createElement('td');
		    td1.className = "text-center";
		    td1.textContent = index + 1;
		
		    // application no
		    const td2 = document.createElement('td');
		    td2.textContent = row.applicationNo ?? '';
		
		    // name
		    const td3 = document.createElement('td');
		    td3.textContent = row.applicantName ?? '';
		
		    // preference
		    const td4 = document.createElement('td');
		    const badge = document.createElement('span');
		    badge.className = "badge bg-secondary";
		    badge.textContent = getPreferenceLabel(row.preferenceOrder);
		    td4.appendChild(badge);
		
		    // programme
		    const td5 = document.createElement('td');
		    td5.textContent = row.programmeName ?? '';
		
		    // eligibility
		    const td6 = document.createElement('td');
		    const status = document.createElement('span');
		
		    if (isEligibleText.startsWith('eligible')) {
		        status.className = "badge bg-success";
		        status.textContent = "Eligible";
		    } else {
		        status.className = "badge bg-danger";
		        status.textContent = "Not Eligible";
		    }
		
		    td6.appendChild(status);
		
		    tr.append(td1, td2, td3, td4, td5, td6);
		    tbody.appendChild(tr);
		});

            const total = data.length;
            const pct = total > 0 ? Math.round((eligibleCount / total) * 100) : 0;

            if (summary) {
                summary.textContent =
                    `Total completed: ${total}, Eligible: ${eligibleCount} (${pct}% eligibility rate)`;
            }
            if (badge) {
                badge.textContent = String(total);
            }
        })
        .catch(err => {
            console.error('Failed to load programme eligibility list:', err);
            	tbody.replaceChildren();

				const tr = document.createElement("tr");
				const td = document.createElement("td");
				
				td.colSpan = 6;
				td.className = "text-center text-danger py-4";
				
				const icon = document.createElement("i");
				icon.className = "bi bi-exclamation-triangle";
				icon.style.fontSize = "2rem";
				
				const p = document.createElement("p");
				p.className = "mt-2 mb-0";
				p.textContent = "Failed to load eligibility list. Please try again.";
				
				td.append(icon, p);
				tr.appendChild(td);
				tbody.appendChild(tr);
            if (summary) {
                summary.textContent = 'Failed to load eligibility summary.';
            }
            if (badge) badge.textContent = '!';
        });
}

// ================= Helpers ===================

function getPreferenceLabel(order) {
    const labels = ['1st Choice', '2nd Choice', '3rd Choice', '4th Choice', '5th Choice'];
    return labels[order - 1] || `${order}th Choice`;
}
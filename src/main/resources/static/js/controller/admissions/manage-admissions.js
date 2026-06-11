/* static/js/controller/admissions/manage-admissions.js */
(function () {
  const JS_VERSION = "manage-admissions.js v11.0-strict-csp-compliant";
  console.log(JS_VERSION);

  const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
  const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

  const admissionWindowCode = document.getElementById('admissionWindowCode')?.value;
  const programmeLevel = (document.getElementById('programmeLevelHidden')?.value || 'UG').toUpperCase();

  // Get the route of the currently active tab
  function getRoundPhaseState() {
    const rt = (document.getElementById('currentRoundType')?.value || 'CUET').toUpperCase();
    const ph = parseInt(document.getElementById('currentPhaseNo')?.value || '1', 10);
    return { roundType: rt, phaseNo: Number.isFinite(ph) ? ph : 1 };
  }

  document.addEventListener('DOMContentLoaded', async () => {
    wireUnifiedButtons();
    wireStreamFilters(); // CSP FIX: Moved from inline HTML script to JS
    const { roundType, phaseNo } = getRoundPhaseState();
    await refreshUIState(roundType, phaseNo);
  });

  window.addEventListener('pageshow', async () => {
    const { roundType, phaseNo } = getRoundPhaseState();
    await refreshUIState(roundType, phaseNo);
  });

  // ==========================================================
  // WIRING
  // ==========================================================
  function wireUnifiedButtons() {
    const processBtn = document.getElementById('btn-unified-process');
    const nextPhaseBtn = document.getElementById('btn-unified-next-phase');
    const goNextRoundBtn = document.getElementById('btn-unified-go-next-round');

    if (processBtn) {
      processBtn.addEventListener('click', async () => {
        const { roundType, phaseNo } = getRoundPhaseState();
        const displayRt = roundType === 'NON_CUET' ? 'Non-CUET' : 'CUET';

        if (!confirm(`Run Admissions Process (Generate Merit -> Publish -> Seat Allotment) for ${displayRt} Phase ${phaseNo}?`)) return;

        // Strictly run for the active tab only
        await runFullAdmissionProcess(processBtn, roundType, phaseNo);
        await refreshUIState(roundType, phaseNo);
      });
    }

    if (nextPhaseBtn) {
      nextPhaseBtn.addEventListener('click', () => {
        const nextPh = nextPhaseBtn.dataset.nextPhase;
        const { roundType } = getRoundPhaseState();

        const safeAdmissionWindowCode = encodeURIComponent(String(admissionWindowCode));
        const safeRoundType = encodeURIComponent(String(roundType));
        const safeNextPh = encodeURIComponent(String(nextPh));

        window.location.href = `/controller/admissions/manage?admissionWindowCode=${safeAdmissionWindowCode}&roundType=${safeRoundType}&phaseNo=${safeNextPh}`;
      });
    }

    if (goNextRoundBtn) {
      goNextRoundBtn.addEventListener('click', () => {
        const safeAdmissionWindowCode = encodeURIComponent(String(admissionWindowCode));
        window.location.href = `/controller/admissions/manage?admissionWindowCode=${safeAdmissionWindowCode}&roundType=NON_CUET&phaseNo=1`;
      });
    }
  }

  // CSP FIX: Moved inline stream filter logic here
  function wireStreamFilters() {
    const filterBtns = document.querySelectorAll('.stream-filter-btn');
    if (!filterBtns.length) return;

    filterBtns.forEach(btn => {
        btn.addEventListener('click', function () {
            document.querySelectorAll('.stream-filter-btn').forEach(b => {
                b.classList.remove('active', 'btn-primary');
                b.classList.add('btn-outline-secondary');
            });
            this.classList.add('active', 'btn-primary');
            this.classList.remove('btn-outline-secondary');

            const selected = this.dataset.stream;
            const rows = document.querySelectorAll('#unifiedProgrammeTable tbody tr[data-stream-id]');
            let visible = 0;

            rows.forEach(row => {
                const match = selected === 'all' || String(row.dataset.streamId) === String(selected);
                // CSP FIX: Use classes instead of style.display
                if (match) {
                    row.classList.remove('d-none');
                    visible++;
                } else {
                    row.classList.add('d-none');
                }
            });

            const noRow = document.getElementById('no-programmes-row');
            if (noRow) {
                if (visible === 0) noRow.classList.remove('d-none');
                else noRow.classList.add('d-none');
            }
        });
    });
  }

  // ==========================================================
  // STRICT SINGLE-ROUTE ENGINE
  // ==========================================================
  async function runFullAdmissionProcess(btn, roundType, phaseNo) {
    const rows = getProgrammeRows();
    if (rows.length === 0) return showAlert('warning', 'No programmes found in table.');

    const originalContent = btn.cloneNode(true);
    btn.disabled = true;

    const displayRt = roundType === 'NON_CUET' ? 'Non-CUET' : 'CUET';
    let generated = 0, noEligible = 0, failed = 0;

    // STEP 1 & 2: Generate AND Publish (For the ACTIVE tab only)
    for (let i = 0; i < rows.length; i++) {
        const pId = rows[i].programmeId;

        // Skip hidden rows (filtered out)
        if (rows[i].actionDiv.closest('tr').classList.contains('d-none')) continue;

        btn.replaceChildren();

        const spinnerBtn = document.createElement("span");
        spinnerBtn.className = "spinner-border spinner-border-sm me-2";

        btn.appendChild(spinnerBtn);
        btn.appendChild(
            document.createTextNode(` Gen ${displayRt} (${i + 1}/${rows.length})...`)
        );
        rows[i].actionDiv.replaceChildren();

        const spinnerDiv = document.createElement("div");
        spinnerDiv.className = "spinner-border spinner-border-sm text-primary";

        const text = document.createElement("span");
        text.className = "small text-primary";
        text.textContent = `Gen ${displayRt}...`;

        rows[i].actionDiv.append(spinnerDiv, text);

        const result = await runMeritListApi(pId, roundType, phaseNo, rows[i].actionDiv, displayRt);

        if (result.ok) {
            if (result.noEligible) {
                noEligible++;
                const span = document.createElement("span");
                span.className = "badge bg-light text-secondary border";
                span.textContent = "No eligible candidates";

                rows[i].actionDiv.replaceChildren(span);
            } else {
                generated++;
                renderMeritLinkOnly(rows[i].actionDiv, pId, roundType, phaseNo);
            }
        } else {
            failed++;
            const span = document.createElement("span");
            span.className = "badge bg-light text-secondary border";
            span.textContent = "Failed";

            rows[i].actionDiv.replaceChildren(span);
        }
    }

    if (generated === 0) {
      btn.disabled = false;
      btn.replaceChildren(...originalContent.childNodes);
      return showAlert('warning', `Merit generation complete (No Eligible: ${noEligible}). Skipped Seat Allotment due to 0 valid merit lists for ${displayRt}.`);
    }

    // STEP 3: Run Seat Allotment (For the ACTIVE tab only)
    btn.replaceChildren();

    const spinner = document.createElement("span");
    spinner.className = "spinner-border spinner-border-sm me-2";

    btn.appendChild(spinner);
    btn.appendChild(document.createTextNode(` Allotting ${displayRt}...`));
    try {
        const url = `/seat-allotment-data/window/${admissionWindowCode}/run?roundType=${roundType}&phaseNo=${phaseNo}`;
        const res = await fetch(url, { method: 'POST', headers: { ...(csrfHeader ? { [csrfHeader]: csrfToken } : {}) }});

        if (res.ok) {
            sessionStorage.setItem(`allotment_run_${admissionWindowCode}_${roundType}_${phaseNo}`, 'true');
            if (failed > 0) {
                showAlert('warning', `Process finished, but ${failed} programme(s) failed to generate. See table for details.`);
            } else {
                showAlert('success', `Admissions Process completed successfully for ${displayRt} Phase ${phaseNo}.`);
            }
        } else {
            showAlert('danger', `Seat Allotment for ${displayRt} encountered an error. Check server logs.`);
        }
    } catch {
        showAlert('danger', `Network error occurred during ${displayRt} Seat Allotment.`);
    }

    btn.disabled = false;
    btn.replaceChildren(...originalContent.childNodes);
  }

  // ==========================================================
  // API CALL: GENERATE + AUTO-PUBLISH
  // ==========================================================
  async function runMeritListApi(pId, rt, phaseNo, actionDiv, displayRt) {
    try {
      const url = `/merit-list/data/generate/${programmeLevel === 'PG' ? 'pg' : 'ug'}?admissionWindowCode=${admissionWindowCode}&programmeId=${pId}&roundType=${rt}&phaseNo=${phaseNo}`;
      const res = await fetch(url, { method: 'POST', headers: { ...(csrfHeader ? { [csrfHeader]: csrfToken } : {}) }});
      const data = await res.json().catch(()=>null);

      if (res.ok) {
        if (data && data.status === 'NO_ELIGIBLE') return { ok: true, noEligible: true };

        const mlId = data?.meritListId;
        if (mlId) {
            if (actionDiv) {
               actionDiv.replaceChildren();
                const spinner = document.createElement("div");
                spinner.className = "spinner-border spinner-border-sm text-success";

                const text = document.createElement("span");
                text.className = "small text-success";
                text.textContent = `Pub ${displayRt}...`;

                actionDiv.append(spinner, text);
            }
            await fetch(`/merit-list/data/${mlId}/publish`, {
                method: 'PUT',
                headers: { ...(csrfHeader ? { [csrfHeader]: csrfToken } : {}) }
            });
        }
        return { ok: true };
      }

      if (res.status === 400 && data && (data.message || '').toLowerCase().includes('no eligible')) return { ok: true, noEligible: true };
      return { ok: false };
    } catch { return { ok: false }; }
  }

  // ==========================================================
  // UI STATE MANAGER
  // ==========================================================
  async function refreshUIState(roundType, phaseNo) {
    const rows = getProgrammeRows();
    if (rows.length === 0) return;

    let allotmentExecuted = false;
    let canAdvance = false;
    let nextPhaseNum = null;
    let canStartNonCuet = false;

    try {
        const url = `/seat-allotment-data/window/${admissionWindowCode}/summary?roundType=${roundType}&phaseNo=${phaseNo}`;
        const sumRes = await fetch(url);
        if (sumRes.ok) {
            const sumData = await sumRes.json();
            const totAllotted = Number(sumData?.totalAllotted || 0);

            canAdvance = sumData?.canGenerateNextPhase || false;
            nextPhaseNum = sumData?.nextPhaseNumber;
            canStartNonCuet = sumData?.canStartNonCuet || false;

            const sessionFlag = sessionStorage.getItem(`allotment_run_${admissionWindowCode}_${roundType}_${phaseNo}`);
            allotmentExecuted = (totAllotted > 0) || canAdvance || canStartNonCuet || sumData?.isCompleted === true || sessionFlag === 'true';
        }
    } catch(e) {}

    for (const row of rows) {
      try {
        const checkUrl = `/merit-list/data/check/${programmeLevel === 'PG' ? 'pg' : 'ug'}?admissionWindowCode=${admissionWindowCode}&programmeId=${row.programmeId}&roundType=${roundType}&phaseNo=${phaseNo}`;
        const checkRes = await fetch(checkUrl);
        const mData = await checkRes.json();

        const isReady = mData.hasMeritList || mData.canGenerate === false;

        if (!isReady) {
            const span = document.createElement("span");
            span.className = "text-muted small";

            const icon = document.createElement("i");
            icon.className = "bi bi-clock me-1";

            span.append(icon, document.createTextNode(" Pending"));
            row.actionDiv.replaceChildren(span);
        } else if (!mData.hasMeritList && mData.canGenerate === false) {
            const span = document.createElement("span");
            span.className = "badge bg-light text-secondary border";
            span.textContent = "No eligible candidates";

            row.actionDiv.replaceChildren(span);
        } else if (!allotmentExecuted) {
            renderMeritLinkOnly(row.actionDiv, row.programmeId, roundType, phaseNo);
        } else {
            renderBothLinks(row.actionDiv, row.programmeId, roundType, phaseNo);
        }
      } catch(e) {
         const span = document.createElement("span");
        span.className = "text-danger small";
        span.textContent = "Error";

        row.actionDiv.replaceChildren(span);
      }
    }

    updateTopButtons(allotmentExecuted, canAdvance, nextPhaseNum, canStartNonCuet, rows);
  }

  function updateTopButtons(allotmentExecuted, canAdvance, nextPhaseNum, canStartNonCuet, rows) {
    const processBtn = document.getElementById('btn-unified-process');
    const nextPhaseBtn = document.getElementById('btn-unified-next-phase');
    const goNextRoundBtn = document.getElementById('btn-unified-go-next-round');

    let allEmpty = true;
    let needsProcessing = false;

    for (const row of rows) {
        const html = row.actionDiv.innerHTML;

        if (!html.includes('No eligible candidates')) {
            allEmpty = false;
        }

        if (html.includes('Pending') ||
            html.includes('Failed') ||
            html.includes('Error') ||
            (html.includes('View Merit List') && !html.includes('Allotments'))) {
            needsProcessing = true;
        }
    }

    if (processBtn) {
        processBtn.classList.remove('d-none', 'btn-primary', 'btn-outline-primary');

        if (allEmpty) {
            processBtn.classList.add('d-none');
        } else if (needsProcessing) {
            processBtn.classList.add('btn-primary');
            processBtn.replaceChildren();
            const icon = document.createElement("i");
            icon.className = "bi bi-play-circle-fill me-1";

            processBtn.append(icon, document.createTextNode(" Run Admission Process"));
        } else {
            processBtn.classList.add('btn-outline-primary');
            processBtn.replaceChildren();
            const icon = document.createElement("i");
            icon.className = "bi bi-arrow-clockwise me-1";

            processBtn.append(icon, document.createTextNode(" Re-Run Process"));
        }
    }

    if (nextPhaseBtn) {
        if (canAdvance) {
            nextPhaseBtn.classList.remove('d-none');
            nextPhaseBtn.dataset.nextPhase = nextPhaseNum;
        } else {
            nextPhaseBtn.classList.add('d-none');
        }
    }

    if (goNextRoundBtn) {
        if (canStartNonCuet) {
            goNextRoundBtn.classList.remove('d-none');
        } else {
            goNextRoundBtn.classList.add('d-none');
        }
    }
  }

  // ==========================================================
  // HTML RENDER HELPERS
  // ==========================================================
  function getProgrammeRows() {
    return Array.from(document.querySelectorAll('.js-phase-action')).map(div => ({
        actionDiv: div,
        programmeId: String(div.dataset.programmeId || '').trim()
    }));
  }

  function renderMeritLinkOnly(div, pId, rt, ph) {
    div.replaceChildren();
    const a = document.createElement("a");
    a.className = "btn btn-outline-primary btn-sm shadow-sm";
    a.href = `/controller/admissions/merit-list?admissionWindowCode=${admissionWindowCode}&programmeId=${pId}&roundType=${rt}&phaseNo=${ph}`;

    const icon = document.createElement("i");
    icon.className = "bi bi-eye";

    a.append(icon, document.createTextNode(" View Merit List"));
    div.appendChild(a);
  }

  function renderBothLinks(div, pId, rt, ph) {
    div.replaceChildren();

    const wrapper = document.createElement("div");
    wrapper.className = "d-flex gap-2";

    const a1 = document.createElement("a");
    a1.className = "btn btn-outline-primary btn-sm shadow-sm";
    a1.href = `/controller/admissions/merit-list?admissionWindowCode=${admissionWindowCode}&programmeId=${pId}&roundType=${rt}&phaseNo=${ph}`;

    const icon1 = document.createElement("i");
    icon1.className = "bi bi-card-list";
    a1.append(icon1, document.createTextNode(" Merit List"));

    const a2 = document.createElement("a");
    a2.className = "btn btn-outline-success btn-sm shadow-sm";
    a2.href = `/seat-allotment/page/window/${admissionWindowCode}/programme/${pId}?roundType=${rt}&phaseNo=${ph}`;

    const icon2 = document.createElement("i");
    icon2.className = "bi bi-diagram-3";
    a2.append(icon2, document.createTextNode(" Allotments"));

    wrapper.append(a1, a2);
    div.appendChild(wrapper);
  }

  function showAlert(type, message) {
    const container = document.getElementById('alertContainer');
    if (!container) return;
    const div = document.createElement('div');
    div.className = `alert alert-${type} alert-dismissible fade show shadow-sm`;
    const text = document.createTextNode(message + " ");
    const btnClose = document.createElement("button");
    btnClose.type = "button";
    btnClose.className = "btn-close";
    btnClose.setAttribute("data-bs-dismiss", "alert");

    div.append(text, btnClose);
    container.prepend(div);
  }

})();
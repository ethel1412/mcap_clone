/* static/js/controller/admissions/merit-list.js */
(function () {

  const getAdmissionWindowCode = () => document.getElementById('admissionWindowCode')?.value;
  const getProgrammeId = () => document.getElementById('programmeId')?.value;
  const getStreamId = () => document.getElementById('streamId')?.value;
  const getRoundType = () => document.getElementById('roundType')?.value;
  const getPhaseNo = () => document.getElementById('phaseNo')?.value;
  const getProgrammeLevel = () => (document.getElementById('programmeLevelHidden')?.value || 'UG').toUpperCase();

  const tbody = document.getElementById('meritTbody');
  const stateContainer = document.getElementById('stateContainer');
  const statusBadge = document.getElementById('statusBadge');

  let currentMeritId = null;
  let currentMeritEntries = [];

  const pageSize = 10;

  document.addEventListener('DOMContentLoaded', () => { loadPage(); });

  function levelSegment() { return getProgrammeLevel() === 'PG' ? 'pg' : 'ug'; }

  async function loadPage() {
    currentMeritId = null;
    currentMeritEntries = [];

    if (tbody) tbody.replaceChildren();
    if (statusBadge) statusBadge.textContent = 'Refreshing...';

    const admissionWindowCode = getAdmissionWindowCode();
    const programmeId = getProgrammeId();

    if (!admissionWindowCode || !programmeId) return showState('error', 'Missing Parameters');

    showState('loading', 'Fetching merit list...');

    let url = `/merit-list/data/for-round-phase/${levelSegment()}?admissionWindowCode=${admissionWindowCode}&programmeId=${programmeId}&roundType=${getRoundType()}&phaseNo=${getPhaseNo()}`;
    if (levelSegment() === 'ug') url += `&streamId=${getStreamId()}`;

    try {
      const res = await fetch(url);
      const meta = await safeJson(res);

      if (!res.ok) return showState('error', 'Server Error');

      const status = String(meta?.status || '').toUpperCase();
      if (status === 'NO_ELIGIBLE') return showState('empty', 'No eligible candidates found.');
      if (status === 'NOT_GENERATED') return showState('empty', 'Merit list has not been generated yet.');
      if (status !== 'FOUND' || !meta?.meritListId) return showState('error', 'Merit list unavailable.');

      currentMeritId = meta.meritListId;
      await loadMeritListById(meta.meritListId, 0);

    } catch (e) {
      console.error(e);
      showState('error', 'Network Error');
    }
  }

  async function loadMeritListById(id, page = 0) {
    try {
      const res = await fetch(`/merit-list/data/${encodeURIComponent(id)}?page=${page}&size=${pageSize}`);
      const pagedData = await safeJson(res);

      if (!res.ok || !pagedData) return showState('error', 'Failed to load entries');

      currentMeritEntries = pagedData.data || [];
      if (currentMeritEntries.length === 0) return showState('empty', 'Merit list is empty');

      renderTable(currentMeritEntries);
      renderPagination(pagedData);

      if (statusBadge) {
        statusBadge.className = 'badge bg-success';
        statusBadge.textContent = `Published · Total ${pagedData.totalElements} Candidates`;
      }

    } catch (e) {
      console.error(e);
      showState('error', 'Data Parsing Error');
    }
  }

  window.loadPageData = function(pageIndex) {
    if (currentMeritId) loadMeritListById(currentMeritId, pageIndex);
  };

  function renderPagination(pagedData) {
    const container = document.getElementById('paginationControls');
    if (!container) return;

    container.replaceChildren();

    if (pagedData.totalPages <= 1) return;

    function createItem(label, page, disabled, active) {
      const li = document.createElement("li");
      li.className = `page-item ${disabled ? 'disabled' : ''} ${active ? 'active' : ''}`;

      const a = document.createElement("a");
      a.className = "page-link";
      a.href = "javascript:void(0)";
      a.textContent = label;

      if (!disabled) {
        a.addEventListener("click", () => loadPageData(page));
      }

      li.appendChild(a);
      return li;
    }

    container.appendChild(createItem("Previous", pagedData.page - 1, pagedData.page === 0, false));

    for (let i = 0; i < pagedData.totalPages; i++) {
      container.appendChild(createItem(i + 1, i, false, pagedData.page === i));
    }

    container.appendChild(createItem("Next", pagedData.page + 1, pagedData.last, false));
  }

  window.downloadPdf = function() {
      if (!currentMeritId) return alert("Merit list ID not found.");

      const safeMeritId = encodeURIComponent(String(currentMeritId));

      window.location.href = `/merit-list/data/${safeMeritId}/export/pdf`;
  };

  window.showMeritRules = function() {
    const modalBody = document.getElementById('rulesModalBody');
    if (!modalBody) return;

    modalBody.replaceChildren();

    if (!currentMeritEntries.length) return alert("No merit data loaded yet.");

    const rulesMap = new Map();

    currentMeritEntries.forEach(e => {
      if (e.selectionCriteria && !rulesMap.has(e.selectionCriteria)) {
        rulesMap.set(e.selectionCriteria, e.ruleDescription || e.selectionCriteria);
      }
    });

    if (!rulesMap.size) {
      const div = document.createElement("div");
      div.className = "text-center py-3 text-muted";
      div.textContent = "No specific rules found for this list.";
      modalBody.appendChild(div);
    } else {

      const info = document.createElement("p");
      info.className = "text-muted mb-3";
      info.textContent = "The following logic was applied to rank the candidates:";
      modalBody.appendChild(info);

      const list = document.createElement("div");
      list.className = "list-group shadow-sm";

      rulesMap.forEach((details, shortName) => {
        const item = document.createElement("div");
        item.className = "list-group-item py-3";

        const title = document.createElement("h6");
        title.className = "fw-bold text-primary";
        title.textContent = shortName;

        const desc = document.createElement("p");
        desc.className = "small text-secondary";
        desc.textContent = details;

        item.append(title, desc);
        list.appendChild(item);
      });

      modalBody.appendChild(list);
    }

    new bootstrap.Modal(document.getElementById('rulesModal')).show();
  };

  function renderTable(entries) {
    // CSP FIX: Replaced .style.display with .classList
    if (stateContainer) stateContainer.classList.add('d-none');
    if (tbody) tbody.replaceChildren();

    entries.forEach(e => {
      const tr = document.createElement("tr");

      const tdRank = document.createElement("td");
      tdRank.textContent = e.rank || '-';

      const tdApp = document.createElement("td");
      tdApp.textContent = e.applicationNo || '';

      const tdName = document.createElement("td");
      tdName.textContent = e.applicantName || '';

      const tdCat = document.createElement("td");
      tdCat.textContent = e.category || 'General';

      const tdScore = document.createElement("td");
      tdScore.className = "text-end";
      tdScore.textContent = e.meritScore != null ? Number(e.meritScore).toFixed(2) : '0.00';

      tr.append(tdRank, tdApp, tdName, tdCat, tdScore);
      tbody.appendChild(tr);
    });
  }

  function showState(type, msg) {
    if (tbody) tbody.replaceChildren();

    if (stateContainer) {
      // CSP FIX: Replaced .style.display with .classList
      stateContainer.classList.remove('d-none');
      stateContainer.replaceChildren();

      const div = document.createElement("div");
      div.className = "text-muted";

      const h = document.createElement("h5");
      h.textContent = msg;

      div.appendChild(h);
      stateContainer.appendChild(div);
    }

    // SYNTAX FIX: This is now correctly inside the function
    const paginationControls = document.getElementById('paginationControls');
    if (paginationControls) paginationControls.replaceChildren();
  }

  async function safeJson(res) {
    try { return await res.json(); } catch { return null; }
  }

})();
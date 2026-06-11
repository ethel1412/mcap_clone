const EligibilityConfig = (() => {
  // CHANGED: Use code instead of id
  let currentWindowCode = null;
  let currentProgrammeId = null;

  // Section 1
  let relaxationRules = [];

  // Section 2 (multiple CUET conditions)
  let cuetConditions = [];
  // each: { subjectNames: "Paper1, Paper2", cuetMinScore: null|number, cuetMinScoreMode: "AVERAGE"|"INDIVIDUAL" }

  // Section 3 (one condition = one rule)
  let routes = []; // [ [ {subjectNames, minPercentage} ], ... ]

  // Merit (two separate lists)
  let cuetMeritRuleSets = []; // [{ optionIndex:number, ruleIndex:0, label?:string }]
  let nonCuetMeritRuleSets = []; // [{ optionIndex:number, ruleIndex:0, label?:string }]

  let masterData = {
    subjects: [],
    qualifications: [],
    cuetSubjects: [],
    castes: [],
  };

  const API_BASE = "/api/data/eligibility";

  const els = {
    viewWindows: document.getElementById("view-windows"),
    viewProgrammes: document.getElementById("view-programmes"),
    viewConfig: document.getElementById("view-config"),
    windowsGrid: document.getElementById("windows-grid"),
    windowsLoading: document.getElementById("windows-loading"),
    progTableBody: document.getElementById("programmeTableBody"),
    selectedWindowName: document.getElementById("selectedWindowName"),
    configProgName: document.getElementById("configProgrammeName"),
    routesContainer: document.getElementById("routes-container"),
    baseQualificationId: document.getElementById("baseQualificationId"),
    minOverallPercentage: document.getElementById("minOverallPercentage"),
    relaxationsContainer: document.getElementById("category-relaxations-container"),
    cuetReqToggle: document.getElementById("cuetReqToggle"),
    cuetInputs: document.getElementById("cuet-inputs"),
    cuetConditionsContainer: document.getElementById("cuet-conditions-container"),
    cuetMeritSourceButtons: document.getElementById("cuet-merit-source-buttons"),
    cuetMeritRuleSetsContainer: document.getElementById("cuet-merit-rulesets-container"),
    nonCuetMeritSourceButtons: document.getElementById("noncuet-merit-source-buttons"),
    nonCuetMeritRuleSetsContainer: document.getElementById("noncuet-merit-rulesets-container"),
    saveBtn: document.getElementById("btnSaveConfig"),
    backBtn: document.getElementById("btnBack"),
    stepWindows: document.getElementById("step-windows"),
    stepProgrammes: document.getElementById("step-programmes"),
    stepConfig: document.getElementById("step-config"),
  };

  function closeAllDropdownPanels() {
    document.querySelectorAll(".subject-multiselect-panel").forEach((p) => p.classList.add("d-none"));
  }

  function init() {
    updateProgress("windows");
    loadMasterData();
    loadWindows();

    document.addEventListener("click", function (e) {
      const insideAny = e.target.closest(".subject-multiselect-wrapper") || e.target.closest(".cuet-box") || e.target.closest(".condition-card") || e.target.closest(".merit-panel-wrap");
      if (!insideAny) closeAllDropdownPanels();
    });
  }

  async function loadMasterData() {
    try {
      const [subjRes, qualRes, casteRes] = await Promise.all([
        axios.get(`${API_BASE}/master/subjects`),
        axios.get(`${API_BASE}/master/qualifications`),
        axios.get(`${API_BASE}/master/castes`),
      ]);
      masterData.subjects = subjRes.data || [];
      masterData.qualifications = qualRes.data || [];
      masterData.castes = casteRes.data || [];
      renderQualificationDropdown();
    } catch (e) {
      showToast("Failed to load reference data.", "danger");
    }
  }

  async function loadWindows() {
    if (els.windowsLoading) els.windowsLoading.classList.remove("d-none");
    try {
      const res = await axios.get(`${API_BASE}/windows`);
      renderWindows(res.data || []);
    } finally {
      if (els.windowsLoading) els.windowsLoading.classList.add("d-none");
    }
  }

function renderWindows(windows) {
  if (!els.windowsGrid) return;

  els.windowsGrid.replaceChildren();

  if (!windows.length) {
    const div = document.createElement("div");
    div.className = "col-12 text-center text-muted";
    div.textContent = "No admission windows found.";
    els.windowsGrid.appendChild(div);
    return;
  }

  windows.forEach((w) => {
    const status = w.scheduleStatus;
    const isOpen = status === "OPEN";

    const startStr = new Date(w.startDate).toLocaleDateString("en-IN", {
      day: "2-digit", month: "short", year: "numeric"
    });
    const endStr = new Date(w.endDate).toLocaleDateString("en-IN", {
      day: "2-digit", month: "short", year: "numeric"
    });

    let badgeClass = "bg-success";
    let btnClass = "btn-primary";
    let btnText = "Configure Rules";
    let statusText = "Currently Open";

    if (status === "UPCOMING") {
      badgeClass = "bg-warning text-dark";
      btnClass = "btn-light";
      btnText = "Not Started";
      statusText = "Starts on " + startStr;
    } else if (status === "CLOSED") {
      badgeClass = "bg-danger";
      btnClass = "btn-light text-muted";
      btnText = "Window Closed";
      statusText = "Closed on " + endStr;
    }

    // --- Layout ---
    const col = document.createElement("div");
    col.className = "col-md-4";

    const card = document.createElement("div");
    card.className = `card h-100 border shadow-sm ${!isOpen ? "bg-light opacity-75" : ""}`;

    const body = document.createElement("div");
    body.className = "card-body p-3 d-flex flex-column";

    // Header row
    const header = document.createElement("div");
    header.className = "d-flex justify-content-between align-items-start mb-2";

    const badge = document.createElement("span");
    badge.className = `badge ${badgeClass} x-small`;
    badge.textContent = status;

    const pending = document.createElement("span");
    pending.className = "x-small fw-bold text-muted";
    pending.textContent = `${w.pendingCount} Pending`;

    header.append(badge, pending);

    // Title
    const title = document.createElement("h6");
    title.className = "fw-bold x-small mb-3 text-navy";
    title.textContent = w.name;

    // Footer
    const footer = document.createElement("div");
    footer.className = "mt-auto border-top pt-2";

    const datesRow = document.createElement("div");
    datesRow.className = "d-flex justify-content-between mb-2";

    const startDiv = document.createElement("div");
    startDiv.className = "text-start";

    const startLabel = document.createElement("p");
    startLabel.className = "mb-0 text-muted";
    startLabel.style.fontSize = "10px";
    startLabel.textContent = "Start Date";

    const startVal = document.createElement("p");
    startVal.className = "mb-0 fw-bold x-small";
    startVal.textContent = startStr;

    startDiv.append(startLabel, startVal);

    const endDiv = document.createElement("div");
    endDiv.className = "text-end";

    const endLabel = document.createElement("p");
    endLabel.className = "mb-0 text-muted";
    endLabel.style.fontSize = "10px";
    endLabel.textContent = "End Date";

    const endVal = document.createElement("p");
    endVal.className = "mb-0 fw-bold x-small";
    endVal.textContent = endStr;

    endDiv.append(endLabel, endVal);

    datesRow.append(startDiv, endDiv);

    // Status text
    const statusP = document.createElement("p");
    statusP.className = `text-center x-small mb-2 ${isOpen ? "text-success fw-bold" : "text-muted"}`;

    const icon = document.createElement("i");
    icon.className = "fas fa-clock me-1";

    statusP.append(icon, document.createTextNode(" " + statusText));

    // Button
    const btn = document.createElement("button");
    btn.className = `btn btn-xs ${btnClass} w-100 fw-bold shadow-sm`;
    btn.textContent = btnText;

    if (!isOpen) {
      btn.disabled = true;
    } else {
      btn.addEventListener("click", () => {
        EligibilityConfig.selectWindow(w.admissionCode, w.name);
      });
    }

    // Assemble
    footer.append(datesRow, statusP, btn);
    body.append(header, title, footer);
    card.appendChild(body);
    col.appendChild(card);
    els.windowsGrid.appendChild(col);
  });
}

  // CHANGED: signature to accept code
  async function selectWindow(code, name) {
    currentWindowCode = code;

    if (els.viewWindows) els.viewWindows.classList.add("d-none");
    if (els.viewProgrammes) els.viewProgrammes.classList.remove("d-none");
    if (els.backBtn) els.backBtn.classList.remove("d-none");
    if (els.selectedWindowName) els.selectedWindowName.textContent = name;

    updateProgress("programmes");
    refreshProgrammeList();
  }

  async function refreshProgrammeList() {
    // CHANGED: API call via code
    if (!currentWindowCode) return;
    const res = await axios.get(`${API_BASE}/programmes-by-window/${currentWindowCode}`);
    renderProgrammes(res.data || []);
  }

  function renderProgrammes(progs) {
    if (!els.progTableBody) return;
    els.progTableBody.innerHTML = (progs || []).map((p) => `
        <tr>
          <td class="ps-3 fw-bold">${escapeHtml(p.programmeName)}</td>
          <td>${escapeHtml(p.streamName)}</td>
          <td class="text-center">
            <span class="badge ${p.hasCriteria ? "bg-success" : "bg-warning"} x-small shadow-sm px-3">
              ${p.hasCriteria ? "Done" : "Pending"}
            </span>
          </td>
          <td class="text-center">
            <button class="btn btn-xs ${p.hasCriteria ? "btn-outline-primary" : "btn-primary"} fw-bold"
                    onclick="EligibilityConfig.selectProgramme(${p.programmeId}, '${escapeQuotes(p.programmeName)}', '${escapeQuotes(p.programmeLevel)}')">
              ${p.hasCriteria ? "Edit Rules" : "Set Rules"}
            </button>
          </td>
        </tr>
      `).join("");
  }

  async function selectProgramme(id, name, level) {
    currentProgrammeId = id;

    if (els.viewProgrammes) els.viewProgrammes.classList.add("d-none");
    if (els.viewConfig) els.viewConfig.classList.remove("d-none");
    if (els.configProgName) els.configProgName.textContent = name;

    updateProgress("config");
    resetForm();
    await loadCuetSubjectsByLevel(level);

    try {
      // CHANGED: pass code to backend
      const res = await axios.get(`${API_BASE}/config`, {
        params: { admissionWindowCode: currentWindowCode, programmeId: currentProgrammeId },
      });
      if (res.status === 200 && res.data) populateForm(res.data);
    } catch (e) {
      // no existing configuration
    }

    normalizeRoutesToSingleRulePerCondition();
    normalizeCuetConditions();

    renderRelaxationRows();
    renderRoutes();
    renderCuetConditions();

    renderCuetMeritSourceButtons();
    renderCuetMeritRuleSets();
    renderNonCuetMeritSourceButtons();
    renderNonCuetMeritRuleSets();
  }

  function populateForm(data) {
    if (els.baseQualificationId) els.baseQualificationId.value = data.baseQualificationId || "";
    if (els.minOverallPercentage) els.minOverallPercentage.value = data.minOverallPercentage ?? "";

    relaxationRules = Array.isArray(data.categoryRelaxations) ? data.categoryRelaxations : [];

    if (els.cuetReqToggle) els.cuetReqToggle.checked = !!data.cuetRequired;
    toggleCuet(!!data.cuetRequired);

    cuetConditions = [];
    routes = [];
    cuetMeritRuleSets = [];
    nonCuetMeritRuleSets = [];

    const ruleSets = Array.isArray(data.ruleSets) ? data.ruleSets : [];

    ruleSets.forEach((rs) => {
      const reqs = Array.isArray(rs.subjectRequirements) ? rs.subjectRequirements : [];
      reqs.forEach((req) => {
        const src = (req.scoreSource || "").toString().toUpperCase();
        const subjectsList = Array.isArray(req.subjectNames) ? req.subjectNames : [];
        const subjectNamesStr = subjectsList.map((s) => (s || "").toString().trim()).filter(Boolean).join(", ");

        if (src === "CUET") {
          cuetConditions.push({
            subjectNames: subjectNamesStr,
            cuetMinScore: null,
            cuetMinScoreMode: "AVERAGE"
          });
        } else if (src === "NON_CUET" || src === "NONCUET") {
          routes.push({
            subjectNames: subjectNamesStr,
            minScore: req.minScore ?? 0
          });
        }
      });
    });

    normalizeCuetConditions();
    normalizeRoutesToSingleRulePerCondition();

    const mergedMerit = Array.isArray(data.meritRuleSets) ? data.meritRuleSets : [];

    cuetMeritRuleSets = mergedMerit
      .filter((m) => (m.sourceType || "").toString().toUpperCase() === "CUET")
      .map((m) => ({
        optionIndex: typeof m.optionIndex === "number" ? m.optionIndex : 0,
        ruleIndex: 0,
        label: m.label || null
      }));

    nonCuetMeritRuleSets = mergedMerit
      .filter((m) => {
        const t = (m.sourceType || "").toString().toUpperCase();
        return t === "NON_CUET" || t === "NONCUET";
      })
      .map((m) => ({
        optionIndex: typeof m.optionIndex === "number" ? m.optionIndex : 0,
        ruleIndex: typeof m.ruleIndex === "number" ? m.ruleIndex : 0,
        label: m.label || null
      }));

    renderRelaxationRows();
    renderRoutes();
    renderCuetConditions();
    renderCuetMeritSourceButtons();
    renderCuetMeritRuleSets();
    renderNonCuetMeritSourceButtons();
    renderNonCuetMeritRuleSets();
  }

  function resetForm() {
    if (els.baseQualificationId) els.baseQualificationId.value = "";
    if (els.minOverallPercentage) els.minOverallPercentage.value = "";
    if (els.cuetReqToggle) els.cuetReqToggle.checked = false;
    toggleCuet(false);
    routes = [];
    relaxationRules = [];
    cuetConditions = [];
    cuetMeritRuleSets = [];
    nonCuetMeritRuleSets = [];

    renderRelaxationRows();
    renderRoutes();
    renderCuetConditions();
    renderCuetMeritSourceButtons();
    renderCuetMeritRuleSets();
    renderNonCuetMeritSourceButtons();
    renderNonCuetMeritRuleSets();
  }

  // -------------------- CUET conditions --------------------
  function normalizeCuetConditions() {
    if (!Array.isArray(cuetConditions)) cuetConditions = [];
    cuetConditions = cuetConditions.map((c) => ({
      subjectNames: c?.subjectNames ?? "",
      cuetMinScore: c?.cuetMinScore === "" || c?.cuetMinScore === undefined ? null : c.cuetMinScore,
      cuetMinScoreMode: (c?.cuetMinScoreMode ?? "AVERAGE").toUpperCase(),
    }));
  }

  function addCuetCondition() {
    cuetConditions.push({ subjectNames: "", cuetMinScore: null, cuetMinScoreMode: "AVERAGE", });
    renderCuetConditions();
    renderCuetMeritSourceButtons();
  }

  function removeCuetCondition(idx) {
    cuetConditions.splice(idx, 1);
    cuetMeritRuleSets = (cuetMeritRuleSets || []).filter((m) => m.optionIndex !== idx);
    cuetMeritRuleSets.forEach((m) => { if (typeof m.optionIndex === "number" && m.optionIndex > idx) m.optionIndex -= 1; });
    renderCuetConditions();
    renderCuetMeritSourceButtons();
    renderCuetMeritRuleSets();
  }

  function toggleCuet(on) {
    if (els.cuetInputs) els.cuetInputs.classList.toggle("d-none", !on);
    if (!on) { cuetConditions = []; cuetMeritRuleSets = []; }
    else { if (!cuetConditions.length) { cuetConditions = [ { subjectNames: "", cuetMinScore: null, cuetMinScoreMode: "AVERAGE" } ]; } }
    renderCuetConditions();
    renderCuetMeritSourceButtons();
    renderCuetMeritRuleSets();
  }

  function getCuetSelectedSubjects(idx) {
    normalizeCuetConditions();
    const raw = cuetConditions[idx]?.subjectNames || "";
    return raw.split(",").map((s) => s.trim()).filter(Boolean);
  }

  function setCuetSelectedSubjects(idx, arr) {
    const clean = (arr || []).map((s) => (s || "").trim()).filter(Boolean);
    cuetConditions[idx].subjectNames = clean.join(", ");
  }

  function updateCuetCondition(idx, field, value) {
    normalizeCuetConditions();
    if (!cuetConditions[idx]) return;
    if (field === "cuetMinScore") {
      const v = (value ?? "").toString().trim();
      const n = v === "" ? null : parseInt(v, 10);
      cuetConditions[idx].cuetMinScore = Number.isNaN(n) ? null : n;
      renderCuetMeritSourceButtons();
      renderCuetMeritRuleSets();
      return;
    }
    if (field === "cuetMinScoreMode") {
      cuetConditions[idx].cuetMinScoreMode = (value || "AVERAGE").toUpperCase();
      renderCuetMeritSourceButtons();
      renderCuetMeritRuleSets();
      return;
    }
  }

  function toggleCuetSubjectDropdown(idx) {
    closeAllDropdownPanels();
    const panel = document.getElementById(`cuet-panel-${idx}`);
    if (panel) panel.classList.toggle("d-none");
  }

  function filterCuetSubjects(val, idx) {
    const t = (val || "").toUpperCase();
    const container = document.getElementById(`cuet-checkbox-container-${idx}`);
    if (!container) return;
    const items = container.querySelectorAll(".cuet-subject-item");
    items.forEach((item) => {
      const text = item.textContent.toUpperCase();
      if (text.includes(t)) { item.classList.remove("d-none"); item.classList.add("d-flex"); }
      else { item.classList.add("d-none"); item.classList.remove("d-flex"); }
    });
  }

  function handleCuetCheckboxChange(idx, cb) {
    normalizeCuetConditions();
    if (!cuetConditions[idx]) return;
    let subjects = getCuetSelectedSubjects(idx);
    if (cb.checked) { if (!subjects.includes(cb.value)) subjects.push(cb.value); }
    else { subjects = subjects.filter((s) => s !== cb.value); }
    setCuetSelectedSubjects(idx, subjects);
    cuetConditions[idx].cuetMinScoreMode = subjects.length > 1 ? "AVERAGE" : "INDIVIDUAL";
    renderCuetConditions();
    renderCuetMeritSourceButtons();
    renderCuetMeritRuleSets();
  }

  function removeCuetChip(name, idx) {
    normalizeCuetConditions();
    if (!cuetConditions[idx]) return;
    let subjects = getCuetSelectedSubjects(idx);
    subjects = subjects.filter((s) => s !== name);
    setCuetSelectedSubjects(idx, subjects);
    cuetConditions[idx].cuetMinScoreMode = subjects.length > 1 ? "AVERAGE" : "INDIVIDUAL";
    renderCuetConditions();
    renderCuetMeritSourceButtons();
    renderCuetMeritRuleSets();
  }

  function renderCuetConditions() {
    if (!els.cuetConditionsContainer) return;
    const cuetEnabled = !!(els.cuetReqToggle && els.cuetReqToggle.checked);
    if (!cuetEnabled) { els.cuetConditionsContainer.innerHTML = ""; return; }
    normalizeCuetConditions();
    if (!cuetConditions.length) {
      els.cuetConditionsContainer.innerHTML = `
        <div class="alert alert-light border p-3 x-small text-center mb-2">
          No CUET eligibility conditions added yet.
          <div class="text-muted mt-1">Click "Add eligibility CUET condition" to start.</div>
        </div>`;
      return;
    }
    els.cuetConditionsContainer.innerHTML = ""; 
		cuetConditions.forEach((c, idx) => {
		    const selected = getCuetSelectedSubjects(idx);
		    const mode = (c.cuetMinScoreMode || "AVERAGE").toUpperCase();
		    const modeLabel = selected.length >= 2
		        ? "Average of selected subjects"
		        : "Single subject (minimum required)";
		    const minLabel = selected.length >= 2
		        ? "Minimum Average % Required"
		        : "Minimum % Required";
		
		    const card = document.createElement("div");
		    card.className = "condition-card mb-3 cuet-box";
		
		    // Header
		    const header = document.createElement("div");
		    header.className = "d-flex justify-content-between align-items-center mb-2";
		
		    const titleWrap = document.createElement("div");
		
		    const rule = document.createElement("div");
		    rule.className = "fw-bold x-small text-navy text-uppercase";
		    rule.textContent = `Rule ${idx + 1}`;
		
		    const sub = document.createElement("div");
		    sub.className = "x-small text-muted";
		    sub.textContent = modeLabel;
		
		    titleWrap.append(rule, sub);
		
		    const delBtn = document.createElement("button");
		    delBtn.type = "button";
		    delBtn.className = "btn btn-xs btn-outline-danger";
		    delBtn.title = "Delete rule";
		    const icon = document.createElement("i");
			icon.className = "fas fa-trash";
			delBtn.appendChild(icon); // static icon is fine
		    delBtn.addEventListener("click", () => {
		        EligibilityConfig.removeCuetCondition(idx);
		    });
		
		    header.append(titleWrap, delBtn);
		
		    // Row
		    const row = document.createElement("div");
		    row.className = "row g-2 align-items-end";
		
		    // ===== Subjects column =====
		    const col1 = document.createElement("div");
		    col1.className = "col-md-7";
		
		    const label = document.createElement("label");
		    label.className = "gov-label";
		    label.textContent = "Required CUET Subjects";
		
		    const chips = document.createElement("div");
		    chips.className = "mb-1 d-flex flex-wrap";
		    chips.innerHTML = renderChips(
			  (c.subjectNames || "").replace(/,\s*/g, ","),
			  "removeCuetChip",
			  idx,
			  0
			);
		
		    const wrapper = document.createElement("div");
		    wrapper.className = "subject-multiselect-wrapper";
		
		    const dropdownHeader = document.createElement("div");
		    dropdownHeader.className = "gov-dropdown-header py-1 rounded";
		    dropdownHeader.innerHTML = `<span class="x-small text-muted">Click to select papers...</span>
		                                <i class="fas fa-search x-small text-primary"></i>`;
		    dropdownHeader.addEventListener("click", () => {
		        EligibilityConfig.toggleCuetSubjectDropdown(idx);
		    });
		
		    const panel = document.createElement("div");
		    panel.className = "subject-multiselect-panel d-none shadow border p-2";
		    panel.id = `cuet-panel-${idx}`;
		    panel.style.cssText = "background:white; position:absolute; z-index:100; width:100%; max-height:240px; overflow-y:auto;";
		
		    // Search input
		    const inputGroup = document.createElement("div");
		    inputGroup.className = "input-group input-group-sm mb-2";
		
		    const search = document.createElement("input");
		    search.type = "text";
		    search.className = "form-control";
		    search.placeholder = "Search paper...";
		    search.addEventListener("input", (e) => {
		        EligibilityConfig.filterCuetSubjects(e.target.value, idx);
		    });
		
		    const closeBtn = document.createElement("button");
		    closeBtn.className = "btn btn-outline-secondary";
		    closeBtn.type = "button";
		    closeBtn.innerHTML = `<i class="fas fa-times"></i>`;
		    closeBtn.addEventListener("click", () => {
		        EligibilityConfig.clearAndCloseCuetDropdown(idx);
		    });
		
		    inputGroup.append(search, closeBtn);
		
		    const checkboxContainer = document.createElement("div");
		    checkboxContainer.className = "subject-checkbox-container";
		    checkboxContainer.id = `cuet-checkbox-container-${idx}`;
		
		    (masterData.cuetSubjects || []).forEach((s) => {
		        const name = s.displayName;
		
		        const label = document.createElement("label");
		        label.className = "d-flex align-items-center mb-1 x-small cuet-subject-item";
		
		        const checkbox = document.createElement("input");
		        checkbox.type = "checkbox";
		        checkbox.value = name;
		        checkbox.className = "me-2";
		        checkbox.checked = selected.includes(name);
		
		        checkbox.addEventListener("change", function () {
		            EligibilityConfig.handleCuetCheckboxChange(idx, this);
		        });
		
		        const span = document.createElement("span");
		        span.textContent = name;
		
		        label.append(checkbox, span);
		        checkboxContainer.appendChild(label);
		    });
		
		    panel.append(inputGroup, checkboxContainer);
		    wrapper.append(dropdownHeader, panel);
		
		    col1.append(label, chips, wrapper);
		
		    // ===== Min score =====
		    const col2 = document.createElement("div");
		    col2.className = "col-md-3";
		
		    const minLbl = document.createElement("label");
		    minLbl.className = "gov-label";
		    minLbl.textContent = `${minLabel} (optional)`;
		
		    const input = document.createElement("input");
		    input.type = "number";
		    input.className = "form-control form-control-sm";
		    input.value = c.cuetMinScore ?? "";
		    input.placeholder = "e.g., 120";
		
		    input.addEventListener("input", (e) => {
		        EligibilityConfig.updateCuetCondition(idx, "cuetMinScore", e.target.value);
		    });
		
		    col2.append(minLbl, input);
		
		    // ===== Mode select =====
		    const col3 = document.createElement("div");
		    col3.className = "col-md-2";
		
		    const modeLbl = document.createElement("label");
		    modeLbl.className = "gov-label";
		    modeLbl.textContent = "Scoring mode";
		
		    const select = document.createElement("select");
		    select.className = "form-select form-select-sm";
		
		    ["AVERAGE", "INDIVIDUAL"].forEach(val => {
		        const opt = document.createElement("option");
		        opt.value = val;
		        opt.textContent = val.charAt(0) + val.slice(1).toLowerCase();
		        if (mode === val) opt.selected = true;
		        select.appendChild(opt);
		    });
		
		    select.addEventListener("change", (e) => {
		        EligibilityConfig.updateCuetCondition(idx, "cuetMinScoreMode", e.target.value);
		    });
		
		    col3.append(modeLbl, select);
		
		    row.append(col1, col2, col3);
		
		    card.append(header, row);
		    els.cuetConditionsContainer.appendChild(card);
		});
  }

  function clearAndClosePathDropdown(rIdx, sIdx) {
    const input = document.getElementById(`path-filter-${rIdx}-0`);
    if (input) { input.value = ""; filterPathSubjects("", rIdx, sIdx); }
    const panel = document.getElementById(`panel-${rIdx}-0`);
    if (panel) panel.classList.add("d-none");
  }

  function clearAndCloseCuetDropdown(idx) {
    const input = document.getElementById(`cuet-filter-${idx}`);
    if (input) { input.value = ""; filterCuetSubjects("", idx); }
    const panel = document.getElementById(`cuet-panel-${idx}`);
    if (panel) panel.classList.add("d-none");
  }

  // -------------------- Merit UI (split) --------------------
  function cuetMeritLabel(optionIndex) {
    const c = cuetConditions?.[optionIndex];
    const subs = (c?.subjectNames || "").split(",").map((s) => s.trim()).filter(Boolean);
    const minScoreTxt = c?.cuetMinScore !== null && c?.cuetMinScore !== "" ? `, MinScore: ${c.cuetMinScore}` : "";
    const modeTxt = `, Mode: ${(c?.cuetMinScoreMode || "AVERAGE").toUpperCase()}`;
    return `Rule ${optionIndex + 1} (CUET): ${subs.length ? subs.join(", ") : "No papers selected"}${minScoreTxt}${modeTxt}`;
  }

  function nonCuetMeritLabel(optionIndex) {
    normalizeRoutesToSingleRulePerCondition();
    const req = routes?.[optionIndex];
    const subs = req?.subjectNames ? req.subjectNames.split(",").map((s) => s.trim()).filter(Boolean) : [];
    const minTxt = req?.minScore ?? 0;
    return `Rule ${optionIndex + 1} (NON-CUET): ${subs.length ? subs.join(", ") : "No subjects"} (Min: ${minTxt})`;
  }

  function isCuetMeritSelected(optionIndex) { return (cuetMeritRuleSets || []).some((x) => x.optionIndex === optionIndex); }
  function isNonCuetMeritSelected(optionIndex) { return (nonCuetMeritRuleSets || []).some((x) => x.optionIndex === optionIndex); }

  function refreshCuetMeritLabels() { cuetMeritRuleSets = (cuetMeritRuleSets || []).map((m) => ({ ...m, label: cuetMeritLabel(m.optionIndex), })); }
  function refreshNonCuetMeritLabels() { nonCuetMeritRuleSets = (nonCuetMeritRuleSets || []).map((m) => ({ ...m, label: nonCuetMeritLabel(m.optionIndex), })); }

  function addCuetMeritRuleSet(optionIndex) {
    if (!(els.cuetReqToggle && els.cuetReqToggle.checked)) { showToast("Enable CUET first to add CUET merit rules.", "warning"); return; }
    if (isCuetMeritSelected(optionIndex)) return;
    cuetMeritRuleSets.push({ optionIndex, ruleIndex: 0, label: cuetMeritLabel(optionIndex), });
    renderCuetMeritRuleSets();
    renderCuetMeritSourceButtons();
  }

  function addNonCuetMeritRuleSet(optionIndex) {
    if (isNonCuetMeritSelected(optionIndex)) return;
    nonCuetMeritRuleSets.push({ optionIndex, ruleIndex: 0, label: nonCuetMeritLabel(optionIndex), });
    renderNonCuetMeritRuleSets();
    renderNonCuetMeritSourceButtons();
  }

  function removeCuetMeritRuleSet(index) { cuetMeritRuleSets.splice(index, 1); renderCuetMeritRuleSets(); renderCuetMeritSourceButtons(); }
  function removeNonCuetMeritRuleSet(index) { nonCuetMeritRuleSets.splice(index, 1); renderNonCuetMeritRuleSets(); renderNonCuetMeritSourceButtons(); }

  function moveCuetMeritRuleSet(index, dir) {
    const j = index + dir;
    if (j < 0 || j >= cuetMeritRuleSets.length) return;
    const tmp = cuetMeritRuleSets[index];
    cuetMeritRuleSets[index] = cuetMeritRuleSets[j];
    cuetMeritRuleSets[j] = tmp;
    renderCuetMeritRuleSets();
  }

  function moveNonCuetMeritRuleSet(index, dir) {
    const j = index + dir;
    if (j < 0 || j >= nonCuetMeritRuleSets.length) return;
    const tmp = nonCuetMeritRuleSets[index];
    nonCuetMeritRuleSets[index] = nonCuetMeritRuleSets[j];
    nonCuetMeritRuleSets[j] = tmp;
    renderNonCuetMeritRuleSets();
  }

  function renderCuetMeritSourceButtons() {
    if (!els.cuetMeritSourceButtons) return;
    const cuetEnabled = !!(els.cuetReqToggle && els.cuetReqToggle.checked);
    if (!cuetEnabled) { els.cuetMeritSourceButtons.innerHTML = `<div class="x-small text-muted">Note: Enable CUET to view CUET merit sources.</div>`; return; }
    if (!cuetConditions.length) { els.cuetMeritSourceButtons.innerHTML = `<div class="x-small text-muted">Add at least one CUET condition.</div>`; return; }
    
    els.cuetMeritSourceButtons.innerHTML = ""; // safe clear
		cuetConditions.forEach((_, i) => {
		  const exists = isCuetMeritSelected(i);
		  const title = cuetMeritLabel(i); // no need to escape when using properties
		
		  const btn = document.createElement("button");
		  btn.type = "button";
		  btn.className = `btn btn-xs ${exists ? "btn-success" : "btn-outline-primary"} fw-bold`;
		  btn.textContent = `${exists ? "Added" : "Add"} Rule ${i + 1}`;
		  btn.title = title;
		
		  if (exists) {
		    btn.disabled = true;
		  } else {
		    btn.addEventListener("click", () => {
		      EligibilityConfig.addCuetMeritRuleSet(i);
		    });
		  }
		
		  els.cuetMeritSourceButtons.appendChild(btn);
		});
  }

  function renderNonCuetMeritSourceButtons() {
    if (!els.nonCuetMeritSourceButtons) return;
    normalizeRoutesToSingleRulePerCondition();
    if (!routes.length) { els.nonCuetMeritSourceButtons.innerHTML = `<div class="x-small text-muted">Note: Add at least one NON-CUET eligibility condition to view sources.</div>`; return; }
    els.nonCuetMeritSourceButtons.innerHTML = ""; // safe clear
	routes.forEach((_, i) => {
	    const exists = isNonCuetMeritSelected(i);
	    const title = nonCuetMeritLabel(i); // no need to escape
	
	    const btn = document.createElement("button");
	    btn.type = "button";
	    btn.className = `btn btn-xs ${exists ? "btn-success" : "btn-outline-primary"} fw-bold`;
	    btn.textContent = `${exists ? "Added" : "Add"} Rule ${i + 1}`;
	    btn.title = title;
	
	    if (exists) {
	        btn.disabled = true;
	    } else {
	        btn.addEventListener("click", () => {
	            EligibilityConfig.addNonCuetMeritRuleSet(i);
	        });
	    }
	
	    els.nonCuetMeritSourceButtons.appendChild(btn);
	});
  }

  function renderCuetMeritRuleSets() {
    if (!els.cuetMeritRuleSetsContainer) return;
    refreshCuetMeritLabels();
    if (!cuetMeritRuleSets.length) { els.cuetMeritRuleSetsContainer.innerHTML = `<div class="alert alert-light border p-2 x-small text-center mb-0">No CUET merit rules selected yet.</div>`; return; }
    els.cuetMeritRuleSetsContainer.replaceChildren(); // safer than innerHTML=""
	cuetMeritRuleSets.forEach((m, idx) => {
	    const wrapper = document.createElement("div");
	    wrapper.className = "d-flex justify-content-between align-items-center border rounded p-2 mb-2";
	
	    // ===== LEFT =====
	    const left = document.createElement("div");
	    left.className = "me-2";
	
	    const title = document.createElement("div");
	    title.className = "fw-bold x-small";
	    title.textContent = `RuleSet ${idx + 1}`;
	
	    const subtitle = document.createElement("div");
	    subtitle.className = "x-small text-muted";
	    subtitle.textContent = m.label || cuetMeritLabel(m.optionIndex);
	
	    left.append(title, subtitle);
	
	    // ===== RIGHT BUTTONS =====
	    const right = document.createElement("div");
	    right.className = "d-flex gap-1";
	
	    // ↑ button
	    const upBtn = document.createElement("button");
	    upBtn.type = "button";
	    upBtn.className = "btn btn-xs btn-outline-secondary";
	    upBtn.textContent = "↑";
	    upBtn.addEventListener("click", () => {
	        EligibilityConfig.moveCuetMeritRuleSet(idx, -1);
	    });
	
	    // ↓ button
	    const downBtn = document.createElement("button");
	    downBtn.type = "button";
	    downBtn.className = "btn btn-xs btn-outline-secondary";
	    downBtn.textContent = "↓";
	    downBtn.addEventListener("click", () => {
	        EligibilityConfig.moveCuetMeritRuleSet(idx, 1);
	    });
	
	    // delete button
	    const delBtn = document.createElement("button");
	    delBtn.type = "button";
	    delBtn.className = "btn btn-xs btn-outline-danger";
	    delBtn.title = "Delete rule";
	
	    const icon = document.createElement("i");
	    icon.className = "fas fa-trash";
	
	    delBtn.appendChild(icon);
	
	    delBtn.addEventListener("click", () => {
	        EligibilityConfig.removeCuetMeritRuleSet(idx);
	    });
	
	    right.append(upBtn, downBtn, delBtn);
	
	    wrapper.append(left, right);
	
	    els.cuetMeritRuleSetsContainer.appendChild(wrapper);
	});
  }

  function renderNonCuetMeritRuleSets() {
    if (!els.nonCuetMeritRuleSetsContainer) return;
    refreshNonCuetMeritLabels();
    if (!nonCuetMeritRuleSets.length) { els.nonCuetMeritRuleSetsContainer.innerHTML = `<div class="alert alert-light border p-2 x-small text-center mb-0">No Non-CUET merit rules selected yet.</div>`; return; }
    els.nonCuetMeritRuleSetsContainer.replaceChildren();

	nonCuetMeritRuleSets.forEach((m, idx) => {
	    const wrapper = document.createElement("div");
	    wrapper.className = "d-flex justify-content-between align-items-center border rounded p-2 mb-2";
	    const left = document.createElement("div");
	    left.className = "me-2";
	
	    const title = document.createElement("div");
	    title.className = "fw-bold x-small";
	    title.textContent = `RuleSet ${idx + 1}`;
	
	    const subtitle = document.createElement("div");
	    subtitle.className = "x-small text-muted";
	    subtitle.textContent = m.label || nonCuetMeritLabel(m.optionIndex);
	
	    left.append(title, subtitle);
	    const right = document.createElement("div");
	    right.className = "d-flex gap-1";
	
	    const upBtn = document.createElement("button");
	    upBtn.type = "button";
	    upBtn.className = "btn btn-xs btn-outline-secondary";
	    upBtn.textContent = "↑";
	    upBtn.addEventListener("click", () => {
	        EligibilityConfig.moveNonCuetMeritRuleSet(idx, -1);
	    });

	    const downBtn = document.createElement("button");
	    downBtn.type = "button";
	    downBtn.className = "btn btn-xs btn-outline-secondary";
	    downBtn.textContent = "↓";
	    downBtn.addEventListener("click", () => {
	        EligibilityConfig.moveNonCuetMeritRuleSet(idx, 1);
	    });
	
	    const delBtn = document.createElement("button");
	    delBtn.type = "button";
	    delBtn.className = "btn btn-xs btn-outline-danger";
	    delBtn.title = "Delete rule";
	
	    const icon = document.createElement("i");
	    icon.className = "fas fa-trash";
	
	    delBtn.appendChild(icon);
	
	    delBtn.addEventListener("click", () => {
	        EligibilityConfig.removeNonCuetMeritRuleSet(idx); // ✅ corrected
	    });
	
	    right.append(upBtn, downBtn, delBtn);
	    wrapper.append(left, right);
	
	    els.nonCuetMeritRuleSetsContainer.appendChild(wrapper);
	});
  }

  // -------------------- Relaxations --------------------
  function getUsedCategoryCodes(exceptIndex) { return (relaxationRules || []).map((r, i) => i === exceptIndex ? null : (r?.categoryCode || "").trim()).filter(Boolean); }
  function getSortedCastes() {
    return [...(masterData.castes || [])].sort((a, b) => {
      const ap = typeof a.priority === "number" ? a.priority : 999;
      const bp = typeof b.priority === "number" ? b.priority : 999;
      if (ap !== bp) return ap - bp;
      return (a.displayName || "").localeCompare(b.displayName || "");
    });
  }

  function renderRelaxationRows() {
    if (!els.relaxationsContainer) return;
    if (!relaxationRules.length) { els.relaxationsContainer.innerHTML = `<div class="col-12"><div class="alert alert-light border p-2 x-small text-center mb-0">No category relaxations added.</div></div>`; return; }
    const castes = getSortedCastes();
    els.relaxationsContainer.innerHTML = (relaxationRules || []).map((rule, idx) => {
        const used = new Set(getUsedCategoryCodes(idx));
        const selectedCode = (rule?.categoryCode || "").trim();
        const optionsHtml = castes.filter((c) => {
            const code = (c.categoryCode || "").trim();
            if (!code) return false;
            if (code === selectedCode) return true;
            return !used.has(code);
          }).map((c) => {
            const code = (c.categoryCode || "").trim();
            const isSelected = code === selectedCode;
            return `<option value="${escapeHtml(code)}" ${isSelected ? "selected" : ""}>${escapeHtml(c.displayName)}</option>`;
          }).join("");
        return `
        <div class="col-12">
          <div class="relax-row">
            <div class="row g-2 align-items-end">
              <div class="col-md-8"><label class="gov-label mb-1">Category</label><select class="form-select form-select-sm" onchange="EligibilityConfig.updateRelaxation(${idx}, 'categoryCode', this.value)"><option value="">Select category</option>${optionsHtml}</select></div>
              <div class="col-md-3"><label class="gov-label mb-1">Min %</label><div class="input-group input-group-sm"><input type="number" class="form-control text-center" value="${rule.relaxationValue ?? 0}" onchange="EligibilityConfig.updateRelaxation(${idx}, 'relaxationValue', this.value)"><span class="input-group-text x-small">%</span></div></div>
              <div class="col-md-1 text-end"><button class="btn btn-xs btn-outline-danger" title="Remove" onclick="EligibilityConfig.removeRelaxationRow(${idx})"><i class="fas fa-trash"></i></button></div>
            </div>
          </div>
        </div>`;
      }).join("");
  }

  function addRelaxationRow() {
    const used = new Set((relaxationRules || []).map((r) => (r?.categoryCode || "").trim()).filter(Boolean));
    const next = getSortedCastes().find((c) => !used.has((c.categoryCode || "").trim()));
    relaxationRules.push({ categoryCode: next ? next.categoryCode : "", relaxationValue: 0, });
    renderRelaxationRows();
  }

  function removeRelaxationRow(idx) { relaxationRules.splice(idx, 1); renderRelaxationRows(); }
  function updateRelaxation(idx, f, v) { if (!relaxationRules[idx]) return; relaxationRules[idx][f] = v; renderRelaxationRows(); }

  // -------------------- Non-CUET conditions --------------------
  function normalizeRoutesToSingleRulePerCondition() {
    if (!Array.isArray(routes)) routes = [];
    const out = [];
    routes.forEach((x) => {
      if (x && !Array.isArray(x) && typeof x === "object") { out.push({ subjectNames: x.subjectNames || "", minScore: x.minScore != null ? x.minScore : (x.minPercentage != null ? x.minPercentage : 0) }); return; }
      if (Array.isArray(x)) { const first = x[0]; if (first && typeof first === "object") { out.push({ subjectNames: first.subjectNames || "", minScore: first.minScore != null ? first.minScore : (first.minPercentage != null ? first.minPercentage : 0) }); } }
    });
    routes = out;
  }

  function addCondition() { routes.push([{ subjectNames: "", minPercentage: 0 }]); renderRoutes(); renderNonCuetMeritSourceButtons(); }

  function removeCondition(idx) {
    routes.splice(idx, 1);
    nonCuetMeritRuleSets = (nonCuetMeritRuleSets || []).filter((m) => m.optionIndex !== idx);
    nonCuetMeritRuleSets.forEach((m) => { if (typeof m.optionIndex === "number" && m.optionIndex > idx) m.optionIndex -= 1; });
    renderRoutes(); renderNonCuetMeritSourceButtons(); renderNonCuetMeritRuleSets();
  }

  function renderRoutes() {
    normalizeRoutesToSingleRulePerCondition();
    if (!els.routesContainer) return;
    if (!routes.length) { els.routesContainer.innerHTML = `<div class="alert alert-light border p-3 x-small text-center mb-2">No eligibility conditions added yet.<div class="text-muted mt-1">Click Add eligibility condition to start.</div></div>`; return; }
	els.routesContainer.innerHTML = ""; // safe clear
		routes.forEach((req, cIdx) => {
		    const subjects = req.subjectNames
		        ? req.subjectNames.split(",").map(s => s.trim()).filter(Boolean)
		        : [];
		
		    const modeLabel = subjects.length > 1
		        ? "Average of selected subjects"
		        : "Single subject minimum required";
		
		    const minLabel = subjects.length > 1
		        ? "Minimum Average % Required"
		        : "Minimum % Required";
		
		    const card = document.createElement("div");
		    card.className = "condition-card mb-3";
		
		    // ===== HEADER =====
		    const header = document.createElement("div");
		    header.className = "d-flex justify-content-between align-items-center mb-2";
		
		    const left = document.createElement("div");
		
		    const rule = document.createElement("div");
		    rule.className = "fw-bold x-small text-navy text-uppercase";
		    rule.textContent = `Rule ${cIdx + 1}`;
		
		    const sub = document.createElement("div");
		    sub.className = "x-small text-muted";
		    sub.textContent = modeLabel;
		
		    left.append(rule, sub);
		
		    const right = document.createElement("div");
		    const removeBtn = document.createElement("button");
		    removeBtn.className = "btn btn-xs btn-outline-danger";
		    removeBtn.textContent = "Remove";
		    removeBtn.addEventListener("click", () => {
		        EligibilityConfig.removeCondition(cIdx);
		    });
		
		    right.appendChild(removeBtn);
		    header.append(left, right);
		
		    // ===== ROW =====
		    const row = document.createElement("div");
		    row.className = "row g-2 align-items-end";
		
		    // ===== SUBJECT COLUMN =====
		    const col1 = document.createElement("div");
		    col1.className = "col-md-7";
		
		    const label = document.createElement("label");
		    label.className = "gov-label";
		    label.textContent = "Required NON-CUET SUBJECTS";
		
		    const chips = document.createElement("div");
		    chips.className = "mb-1 d-flex flex-wrap";
		
		    // ⚠️ IMPORTANT: if renderChips returns string → still unsafe
		    chips.innerHTML = renderChips(req.subjectNames, "removePathSubject", cIdx, 0);
		
		    const wrapper = document.createElement("div");
		    wrapper.className = "subject-multiselect-wrapper";
		
		    const dropdownHeader = document.createElement("div");
		    dropdownHeader.className = "gov-dropdown-header py-1 rounded";
		    dropdownHeader.innerHTML = `<span class="x-small text-muted">Add/Search subjects...</span><i class="fas fa-search x-small text-primary"></i>`;
		
		    dropdownHeader.addEventListener("click", () => {
		        EligibilityConfig.togglePathDropdown(cIdx, 0);
		    });
		
		    const panel = document.createElement("div");
		    panel.className = "subject-multiselect-panel d-none shadow border";
		    panel.id = `panel-${cIdx}-0`;
		    panel.style.cssText = "max-height:220px; overflow-y:auto;";
		
		    const inner = document.createElement("div");
		    inner.className = "p-2";
		
		    (masterData.subjects || []).forEach(sub => {
		        const item = document.createElement("label");
		        item.className = "d-flex align-items-center mb-1 x-small path-subject-item";
		
		        const checkbox = document.createElement("input");
		        checkbox.type = "checkbox";
		        checkbox.value = sub.subjectName;
		        checkbox.className = "me-2";
		        checkbox.checked = subjects.includes(sub.subjectName);
		
		        checkbox.addEventListener("change", function () {
		            EligibilityConfig.handlePathCheck(cIdx, 0, this);
		        });
		
		        const span = document.createElement("span");
		        span.textContent = sub.subjectName;
		
		        item.append(checkbox, span);
		        inner.appendChild(item);
		    });
		
		    panel.appendChild(inner);
		    wrapper.append(dropdownHeader, panel);
		
		    col1.append(label, chips, wrapper);
		
		    // ===== MIN SCORE =====
		    const col2 = document.createElement("div");
		    col2.className = "col-md-5";
		
		    const minLbl = document.createElement("label");
		    minLbl.className = "gov-label";
		    minLbl.textContent = minLabel;
		
		    const inputGroup = document.createElement("div");
		    inputGroup.className = "input-group input-group-sm";
		
		    const input = document.createElement("input");
		    input.type = "number";
		    input.className = "form-control";
		    input.value = req.minScore ?? 0;
		
		    input.addEventListener("change", (e) => {
		        EligibilityConfig.updateReq(cIdx, 0, "minScore", e.target.value);
		    });
		
		    const span = document.createElement("span");
		    span.className = "input-group-text x-small";
		    span.textContent = "%";
		
		    inputGroup.append(input, span);
		    col2.append(minLbl, inputGroup);
		
		    row.append(col1, col2);
		    card.append(header, row);
		
		    els.routesContainer.appendChild(card);
		});
  }

  function handlePathCheck(rIdx, sIdx, cb) {
    normalizeRoutesToSingleRulePerCondition();
    if (!routes[rIdx]) return;
    let subjects = routes[rIdx].subjectNames ? routes[rIdx].subjectNames.split(",").map((s) => s.trim()).filter(Boolean) : [];
    if (cb.checked) { if (!subjects.includes(cb.value)) subjects.push(cb.value); }
    else { subjects = subjects.filter((s) => s !== cb.value); }
    routes[rIdx].subjectNames = subjects.join(", ");
    renderRoutes(); renderNonCuetMeritSourceButtons(); renderNonCuetMeritRuleSets();
  }

  function removePathSubject(name, rIdx, sIdx) {
    normalizeRoutesToSingleRulePerCondition();
    if (!routes[rIdx]) return;
    let subjects = routes[rIdx].subjectNames ? routes[rIdx].subjectNames.split(",").map((s) => s.trim()).filter(Boolean) : [];
    subjects = subjects.filter((s) => s !== name);
    routes[rIdx].subjectNames = subjects.join(", ");
    renderRoutes(); renderNonCuetMeritSourceButtons(); renderNonCuetMeritRuleSets();
  }

  function togglePathDropdown(r, s) { closeAllDropdownPanels(); const panel = document.getElementById(`panel-${r}-0`); if (panel) panel.classList.remove("d-none"); }

  function filterPathSubjects(val, rIdx, sIdx) {
    const t = (val || "").toUpperCase();
    const panel = document.getElementById(`panel-${rIdx}-0`);
    if (!panel) return;
    const items = panel.querySelectorAll(".path-subject-item");
    items.forEach((item) => {
      const text = item.textContent.toUpperCase();
      if (text.includes(t)) { item.classList.remove("d-none"); item.classList.add("d-flex"); }
      else { item.classList.add("d-none"); item.classList.remove("d-flex"); }
    });
  }

  function updateReq(ri, si, f, v) {
    normalizeRoutesToSingleRulePerCondition();
    if (!routes[ri]) return;
    if (f === "minPercentage" || f === "minScore") {
      const raw = (v ?? "").toString().trim();
      const n = raw === "" ? 0 : parseFloat(raw);
      routes[ri].minScore = Number.isFinite(n) ? n : 0;
    } else if (f === "subjectNames") { routes[ri].subjectNames = (v ?? "").toString(); }
    else { routes[ri][f] = v; }
    renderNonCuetMeritSourceButtons(); renderNonCuetMeritRuleSets();
  }

  // -------------------- Save --------------------
  function saveConfiguration() {
    const isCuet = !!(els.cuetReqToggle && els.cuetReqToggle.checked);
    normalizeRoutesToSingleRulePerCondition();
    normalizeCuetConditions();

    const payload = {
      // CHANGED: Use admissionCode property
      admissionCode: currentWindowCode,
      programmeId: currentProgrammeId,
      baseQualificationId: els.baseQualificationId?.value ? parseInt(els.baseQualificationId.value, 10) : null,
      minOverallPercentage: parseFloat(els.minOverallPercentage?.value || "0"),
      categoryRelaxations: relaxationRules.filter((r) => r && (r.categoryCode || "").trim()),
      cuetRequired: isCuet,
      ruleSets: [],
      meritRuleSets: [],
      tiebreakerConfig: null
    };

    if (!payload.baseQualificationId) { return showToast("Qualification Required", "warning"); }

    try {
      if (isCuet) {
        if (!cuetConditions.length) { throw new Error("Add at least one CUET eligibility condition."); }
        cuetConditions.forEach((c, idx) => {
          const subjects = (c?.subjectNames || "").split(",").map((s) => s.trim()).filter(Boolean);
          if (!subjects.length) { throw new Error(`CUET Rule ${idx + 1}: Please select at least 1 paper.`); }
          const raw = (c?.cuetMinScore ?? "").toString().trim();
          const cuetScore = raw === "" ? null : Number(raw);
          if (cuetScore !== null && (!Number.isFinite(cuetScore) || cuetScore <= 0)) { throw new Error(`CUET Rule ${idx + 1}: Minimum score must be > 0 if provided.`); }
          payload.ruleSets.push({
            description: `CUET Rule ${idx + 1}`,
            subjectRequirements: [{ subjectNames: subjects, minScore: cuetScore, calculationType: subjects.length > 1 ? "AGGREGATE_AVERAGE" : "INDIVIDUAL_SUBJECT", scoreSource: "CUET" }]
          });
        });
      }

      normalizeRoutesToSingleRulePerCondition();
      if (routes.length) {
        routes.forEach((req, idx) => {
          const subjects = (req?.subjectNames || "").split(",").map((s) => s.trim()).filter(Boolean);
          if (!subjects.length) { throw new Error(`NON-CUET Rule ${idx + 1}: Please select at least 1 subject.`); }
          const minPct = parseFloat((req?.minScore ?? "0").toString());
          if (!Number.isFinite(minPct) || minPct <= 0) { throw new Error(`NON-CUET Rule ${idx + 1}: Minimum percentage must be > 0.`); }
          payload.ruleSets.push({
            description: `NON-CUET Rule ${idx + 1}`,
            subjectRequirements: [{ subjectNames: subjects, minScore: minPct, calculationType: subjects.length > 1 ? "AGGREGATE_AVERAGE" : "INDIVIDUAL_SUBJECT", scoreSource: "NON_CUET" }]
          });
        });
      }
    } catch (e) {
      return showToast(e.message || "Invalid input", "warning");
    }

    if (!payload.ruleSets.length) { return showToast("Add at least one eligibility condition (CUET or NON-CUET).", "warning"); }

    const cleanCuetMerit = isCuet ? cuetMeritRuleSets.map((m) => ({ sourceType: "CUET", optionIndex: typeof m.optionIndex === "number" ? m.optionIndex : 0, ruleIndex: 0, label: m.label || null })).filter((m) => m.optionIndex >= 0 && m.optionIndex < cuetConditions.length) : [];
    const cleanNonCuetMerit = nonCuetMeritRuleSets.map((m) => ({ sourceType: "NON_CUET", optionIndex: typeof m.optionIndex === "number" ? m.optionIndex : 0, ruleIndex: typeof m.ruleIndex === "number" ? m.ruleIndex : 0, label: m.label || null })).filter((m) => m.optionIndex >= 0 && m.optionIndex < routes.length);
    payload.meritRuleSets = [...cleanCuetMerit, ...cleanNonCuetMerit];

    if (els.saveBtn) els.saveBtn.disabled = true;

    axios
      .post(`${API_BASE}/save`, payload)
      .then(() => { showToast("Rules Saved Successfully", "success"); return refreshProgrammeList(); })
      .then(() => goBack())
      .catch((e) => { console.error(e); showToast("Save error. Please check inputs.", "danger"); })
      .finally(() => { if (els.saveBtn) els.saveBtn.disabled = false; });
  }

  function renderChips(subjectString, onRemoveFn, rIdx, sIdx) {
    if (!subjectString) return `<span class="text-muted x-small">None Selected</span>`;
    return subjectString.split(",").map((s) => s.trim()).filter(Boolean).map((s) => `<span class="subject-chip">${escapeHtml(s)}<i class="fas fa-times ms-2 cursor-pointer text-danger" onclick="EligibilityConfig.${onRemoveFn}('${escapeQuotes(s)}', ${rIdx}, ${sIdx})"></i></span>`).join("");
  }

  function escapeQuotes(s) { return (s || "").replace(/'/g, "\\'").replace(/"/g, "&quot;"); }
  function escapeHtml(s) { return (s || "").replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;").replace(/'/g, "&#039;"); }

  return {
    init, selectWindow, selectProgramme, goBack,
    toggleCuet, addCuetCondition, removeCuetCondition, toggleCuetSubjectDropdown, handleCuetCheckboxChange, removeCuetChip, filterCuetSubjects, updateCuetCondition,
    addCuetMeritRuleSet, removeCuetMeritRuleSet, moveCuetMeritRuleSet, renderCuetMeritSourceButtons, renderCuetMeritRuleSets,
    addNonCuetMeritRuleSet, removeNonCuetMeritRuleSet, moveNonCuetMeritRuleSet, renderNonCuetMeritSourceButtons, renderNonCuetMeritRuleSets,
    addRelaxationRow, removeRelaxationRow, updateRelaxation,
    addCondition, removeCondition, handlePathCheck, removePathSubject, togglePathDropdown, filterPathSubjects, updateReq,
    clearAndClosePathDropdown, clearAndCloseCuetDropdown,
    saveConfiguration,
  };
})();

document.addEventListener("DOMContentLoaded", EligibilityConfig.init);
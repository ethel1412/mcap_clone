/* eligibility-core.js */
(function () {
  "use strict";

  window.EligibilityConfig = window.EligibilityConfig || {};

  const S = (window.EligibilityConfigState = window.EligibilityConfigState || {
    // CHANGED: currentWindowId to currentWindowCode
    currentWindowCode: null,
    currentProgrammeId: null,
    programmeLevel: "UG",
    relaxationRules: [],
    cuetConditions: [],
    routes: [],
    cuetMeritRuleSets: [],
    nonCuetMeritRuleSets: [],

    // --- TIEBREAKER STATE ---
    tiebreakerConfig: [],
    tiebreakerSortableInstance: null,
    tiebreakerOptions: [
        { value: "CLASS_12_MARKS", label: "Class XII Percentage" },
        { value: "ENTRANCE_EXAM_SCORE", label: "Entrance Exam Score" },
        { value: "UG_DEGREE_MARKS", label: "UG Degree Percentage" },
        { value: "COMMUNITY_CATEGORY", label: "Community Category Priority" },
        { value: "DOB_OLDER", label: "Date of Birth (Older first)" }
    ],

    masterData: { subjects: [], qualifications: [], cuetSubjects: [], castes: [] },
    API_BASE: "/api/data/eligibility",
    els: {},
    choicesInstances: {}
  });

  function cacheEls() {
    S.els = {
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
      nonCuetReqToggle: document.getElementById("nonCuetReqToggle"),
      noncuetInputs: document.getElementById("noncuet-inputs"),
      saveBtn: document.getElementById("btnSaveConfig"),
      backBtn: document.getElementById("btnBack"),
      stepWindows: document.getElementById("step-windows"),
      stepProgrammes: document.getElementById("step-programmes"),
      stepConfig: document.getElementById("step-config"),
    };
  }

  function showToast(m, t) {
    const msgEl = document.getElementById("toastMessage");
    const toastEl = document.getElementById("statusToast");
    if (!msgEl || !toastEl) return;
    msgEl.textContent = m || "";
    const type = (t || "primary").toString().trim().toLowerCase();
    const extra = type === "warning" ? " text-dark" : "";
    toastEl.className = `toast align-items-center text-white border-0 bg-${type}${extra}`;
    new bootstrap.Toast(toastEl).show();
  }

  function escapeHtml(s) {
    return (s ?? "").toString().replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;").replace(/'/g, "&#039;");
  }

  function normalizeCuetMaster() {
    const raw = Array.isArray(S.masterData.cuetSubjects) ? S.masterData.cuetSubjects : [];
    S.masterData.cuetSubjects = raw.map((p, i) => {
      const displayName = (p?.displayName || p?.paperName || p?.name || "").toString().trim();
      const paperCode = (p?.paperCode || p?.code || "").toString().trim() || displayName;
      return {
        ...p,
        paperCode: paperCode || `P${String(i + 1).padStart(2, "0")}`,
        displayName: displayName || paperCode || `Paper ${i + 1}`,
        spec: (p?.spec || "").toString().trim().toUpperCase()
      };
    });
  }

  function updateProgress(stage) {
    const { els } = S;
    const steps = [els.stepWindows, els.stepProgrammes, els.stepConfig].filter(Boolean);
    steps.forEach((s) => s.classList.remove("active"));
    if (stage === "windows" && els.stepWindows) els.stepWindows.classList.add("active");
    if (stage === "programmes" && els.stepProgrammes) els.stepProgrammes.classList.add("active");
    if (stage === "config" && els.stepConfig) els.stepConfig.classList.add("active");
  }

  function goBack() {
    const { els } = S;
    if (els.viewConfig && !els.viewConfig.classList.contains("d-none")) {
      els.viewConfig.classList.add("d-none");
      if (els.viewProgrammes) els.viewProgrammes.classList.remove("d-none");
      updateProgress("programmes");
      return;
    }
    if (els.viewProgrammes) els.viewProgrammes.classList.add("d-none");
    if (els.viewWindows) els.viewWindows.classList.remove("d-none");
    if (els.backBtn) els.backBtn.classList.add("d-none");
    updateProgress("windows");
  }

  async function loadMasterData() {
    try {
      const [subjRes, qualRes, casteRes] = await Promise.all([
        axios.get(`${S.API_BASE}/master/subjects`),
        axios.get(`${S.API_BASE}/master/qualifications`),
        axios.get(`${S.API_BASE}/master/castes`),
      ]);
      S.masterData.subjects = subjRes.data || [];
      S.masterData.qualifications = qualRes.data || [];
      S.masterData.castes = casteRes.data || [];
      renderQualificationDropdown();
    } catch (e) {
      showToast("Failed to load reference data.", "danger");
    }
  }

  function renderQualificationDropdown() {
    const { els } = S;
    if (!els.baseQualificationId) return;

    const targetLevel = (S.programmeLevel || "UG").toUpperCase();

    const sortedQuals = [...(S.masterData.qualifications || [])].sort((a, b) => {
        const aLevel = (a.programmeLevel || a.level || "").toString().toUpperCase();
        const bLevel = (b.programmeLevel || b.level || "").toString().toUpperCase();

        if (aLevel === targetLevel && bLevel !== targetLevel) return -1;
        if (aLevel !== targetLevel && bLevel === targetLevel) return 1;
        return (a.name || "").localeCompare(b.name || "");
    });

    const options = sortedQuals.map(q => ({
        value: q.id, label: q.name
    }));

    if(S.choicesInstances['baseQualification']) {
        S.choicesInstances['baseQualification'].destroy();
    }

    S.choicesInstances['baseQualification'] = new Choices(els.baseQualificationId, {
        searchEnabled: true,
        placeholderValue: 'Search Qualifications...',
        itemSelectText: '',
        shouldSort: false
    });

    S.choicesInstances['baseQualification'].setChoices(options, 'value', 'label', true);
  }

  async function loadCuetSubjectsByLevel(level) {
    try {
      const res = await axios.get(`${S.API_BASE}/master/cuet-subjects`, { params: { level } });
      S.masterData.cuetSubjects = res.data || [];
    } catch (e) {
      S.masterData.cuetSubjects = [];
    }
    normalizeCuetMaster();
    if (typeof EligibilityConfig.renderCuetConditions === "function") EligibilityConfig.renderCuetConditions();
  }

  async function loadWindows() {
    const { els } = S;
    if (els.windowsLoading) els.windowsLoading.classList.remove("d-none");
    try {
      const res = await axios.get(`${S.API_BASE}/windows`);
      renderWindows(res.data || []);
    } finally {
      if (els.windowsLoading) els.windowsLoading.classList.add("d-none");
    }
  }

  function renderWindows(windows) {
    const { els } = S;
    if (!els.windowsGrid) return;
    if (!windows.length) {
      els.windowsGrid.innerHTML = `<div class="col-12 text-center text-muted">No admission windows found.</div>`;
      return;
    }
   
    els.windowsGrid.replaceChildren();
	(windows || []).forEach((w) => {
	    const status = (w.scheduleStatus || "").toString().toUpperCase();
	    const isOpen = status === "OPEN";

	    const col = document.createElement("div");
	    col.className = "col-md-4";

	    const card = document.createElement("div");
	    card.className = `card h-100 border shadow-sm ${!isOpen ? "bg-light opacity-75" : ""}`;

	    const body = document.createElement("div");
	    body.className = "card-body p-3 d-flex flex-column";

	    const top = document.createElement("div");
	    top.className = "d-flex justify-content-between align-items-start mb-2";
	
	    const badge = document.createElement("span");
	    badge.className = `badge ${isOpen ? "bg-success" : "bg-warning text-dark"}`;
	    badge.textContent = status;
	
	    top.appendChild(badge);

	    const title = document.createElement("h6");
	    title.className = "fw-bold mb-3 text-navy";
	    title.textContent = w.name || "";

	    const footer = document.createElement("div");
	    footer.className = "mt-auto pt-2";
	
	    const btn = document.createElement("button");
	    btn.type = "button";
	    btn.className = `btn btn-sm ${isOpen ? "btn-primary" : "btn-light text-muted"} w-100 fw-bold shadow-sm`;
	    btn.textContent = "Configure Rules";
	
	    if (!isOpen) {
	        btn.disabled = true;
	    } else {
	        btn.addEventListener("click", () => {
	            // no escaping needed; not injecting into HTML
	            EligibilityConfig.selectWindow(
	                String(w.admissionCode ?? ""),
	                String(w.name ?? "")
	            );
	        });
	    }
	
	    footer.appendChild(btn);
	    body.append(top, title, footer);
	    card.appendChild(body);
	    col.appendChild(card);
	
	    els.windowsGrid.appendChild(col);
	});
  }

  // CHANGED: Expect code instead of id
  async function selectWindow(code, name) {
    const { els } = S;
    // CHANGED: Assign code
    S.currentWindowCode = code;
    if (els.viewWindows) els.viewWindows.classList.add("d-none");
    if (els.viewProgrammes) els.viewProgrammes.classList.remove("d-none");
    if (els.backBtn) els.backBtn.classList.remove("d-none");
    if (els.selectedWindowName) els.selectedWindowName.textContent = name || "";
    updateProgress("programmes");
    await refreshProgrammeList();
  }

  async function refreshProgrammeList() {
    // CHANGED: Use S.currentWindowCode
    if (!S.currentWindowCode) return;
    const res = await axios.get(`${S.API_BASE}/programmes-by-window/${S.currentWindowCode}`);
    renderProgrammes(res.data || []);
  }

  function renderProgrammes(progs) {
    const { els } = S;
    if (!els.progTableBody) return;
  	els.progTableBody.replaceChildren();
	
	(progs || []).forEach((p) => {
	
	  const tr = document.createElement("tr");
	
	  // Programme Name
	  const td1 = document.createElement("td");
	  td1.className = "ps-4 fw-bold text-dark";
	  td1.textContent = p.programmeName || "";
	
	  // Stream
	  const td2 = document.createElement("td");
	  td2.textContent = p.streamName || "";
	
	  // Status
	  const td3 = document.createElement("td");
	  td3.className = "text-center";
	
	  const badge = document.createElement("span");
	  badge.className = `badge ${p.hasCriteria ? "bg-success" : "bg-warning text-dark"} shadow-sm px-3 py-2 rounded-pill`;
	  badge.textContent = p.hasCriteria ? "Rules Set" : "Pending";
	
	  td3.appendChild(badge);
	
	  // Action
	  const td4 = document.createElement("td");
	  td4.className = "text-center";
	
	  const btn = document.createElement("button");
	  btn.className = `btn btn-sm ${p.hasCriteria ? "btn-outline-primary" : "btn-primary"} fw-bold px-3 rounded-pill`;
	  btn.textContent = p.hasCriteria ? "Edit Rules" : "Set Rules";
	
	  // ✅ NO onclick → safe event listener
	  btn.addEventListener("click", () => {
	    window.EligibilityConfig.selectProgramme(
	      p.programmeId,
	      p.programmeName || "",
	      p.programmeLevel || "UG"
	    );
	  });
	
	  td4.appendChild(btn);
	
	  // Append all
	  tr.append(td1, td2, td3, td4);
	  els.progTableBody.appendChild(tr);
	});
  }

  async function selectProgramme(id, name, level) {
    const { els } = S;
    S.currentProgrammeId = id;
    S.programmeLevel = (level || "UG").toString().trim().toUpperCase();

    renderQualificationDropdown();

    if (els.viewProgrammes) els.viewProgrammes.classList.add("d-none");
    if (els.viewConfig) els.viewConfig.classList.remove("d-none");
    if (els.configProgName) els.configProgName.textContent = name || "";

    updateProgress("config");
    resetForm();
    await loadCuetSubjectsByLevel(S.programmeLevel);

    try {
      // CHANGED: Pass admissionWindowCode
      const res = await axios.get(`${S.API_BASE}/config`, {
        params: { admissionWindowCode: S.currentWindowCode, programmeId: S.currentProgrammeId },
      });
      if (res.status === 200 && res.data) populateForm(res.data);
    } catch (e) {}

    if (typeof EligibilityConfig.normalizeRoutesToSingleRulePerCondition === "function") EligibilityConfig.normalizeRoutesToSingleRulePerCondition();
    if (typeof EligibilityConfig.normalizeCuetConditions === "function") EligibilityConfig.normalizeCuetConditions();
    if (typeof EligibilityConfig.renderRelaxationRows === "function") EligibilityConfig.renderRelaxationRows();
    if (typeof EligibilityConfig.renderRoutes === "function") EligibilityConfig.renderRoutes();
    if (typeof EligibilityConfig.renderCuetConditions === "function") EligibilityConfig.renderCuetConditions();

    if (window.EligibilityPreview) EligibilityPreview.updateStudentPreview();
  }

  function populateForm(data) {
    const { els } = S;

    if (S.choicesInstances['baseQualification'] && data.baseQualificationId) {
        S.choicesInstances['baseQualification'].setChoiceByValue(data.baseQualificationId);
    }

    if (els.minOverallPercentage) els.minOverallPercentage.value = data.minOverallPercentage ?? "";

    S.relaxationRules = Array.isArray(data.categoryRelaxations) ? data.categoryRelaxations : [];

    try {
        S.tiebreakerConfig = data.tiebreakerConfig ? JSON.parse(data.tiebreakerConfig) : [];
    } catch (e) {
        S.tiebreakerConfig = [];
    }

    const cuetRequired = !!data.cuetRequired;
    if (els.cuetReqToggle) els.cuetReqToggle.checked = cuetRequired;
    if (typeof EligibilityConfig.toggleCuet === "function") EligibilityConfig.toggleCuet(cuetRequired);

    S.cuetConditions = [];
    S.routes = [];
    S.cuetMeritRuleSets = [];
    S.nonCuetMeritRuleSets = [];

    const rsList = Array.isArray(data.ruleSets) && data.ruleSets.length ? data.ruleSets : (Array.isArray(data.alternativeRuleSets) ? data.alternativeRuleSets : []);

    if (Array.isArray(rsList) && rsList.length) {
      const cuet = [];
      const noncuet = [];
      rsList.forEach((rs) => {
        const reqs = Array.isArray(rs.subjectRequirements) ? rs.subjectRequirements : [];
        reqs.forEach((req) => {
          const src = (req.scoreSource || "").toString().toUpperCase();
          const subjects = Array.isArray(req.subjectNames) ? req.subjectNames.map((x) => (x ?? "").toString().trim()).filter(Boolean) : (req.subjectNames || "").toString().split(",").map((s) => s.trim()).filter(Boolean);
          const minScore = req.minScore ?? req.minPercentage ?? null;

          if (src === "CUET") {
            cuet.push({
              subjectNames: subjects.join(","),
              cuetMinScore: minScore,
              cuetMinScoreMode: (req.calculationType || "").toString().toUpperCase() === "INDIVIDUAL_SUBJECT" ? "INDIVIDUAL" : "AVERAGE",
            });
          } else if (src === "NON_CUET" || src === "NONCUET") {
            noncuet.push([{ subjectNames: subjects.join(", "), minPercentage: minScore ?? 0 }]);
          }
        });
      });
      S.cuetConditions = cuet;
      S.routes = noncuet;
    }

    if (Array.isArray(data.meritRuleSets)) {
        const merged = data.meritRuleSets || [];

        S.cuetMeritRuleSets = merged.filter((m) => (m.sourceType || "").toString().toUpperCase() === "CUET").map(m => {
            const includedCodes = Array.isArray(m.meritSubjects) ? m.meritSubjects : [];
            const optionIndex = typeof m.optionIndex === "number" ? m.optionIndex : 0;

            const cond = S.cuetConditions[optionIndex];
            const totalCodes = cond && cond.subjectNames ? cond.subjectNames.split(',').map(s=>s.trim()).filter(Boolean) : [];
            const ignoredCodes = totalCodes.filter(c => !includedCodes.includes(c));

            const ignoreSpecsSet = new Set();
            ignoredCodes.forEach(code => {
                const paper = S.masterData.cuetSubjects.find(p => p.paperCode === code || p.displayName === code);
                if (paper && paper.spec) ignoreSpecsSet.add(paper.spec.toUpperCase());
            });

            return {
                optionIndex: optionIndex,
                ruleIndex: 0,
                label: m.label || null,
                ignoreSpecs: Array.from(ignoreSpecsSet),
                derivedCodesCsv: includedCodes.join(","),
            };
        });

        S.nonCuetMeritRuleSets = merged.filter((m) => {
            const t = (m.sourceType || "").toString().toUpperCase();
            return t === "NON_CUET" || t === "NONCUET" || t === "NON-CUET";
        }).map(m => {
            const includedSubjects = Array.isArray(m.meritSubjects) ? m.meritSubjects : [];
            const optionIndex = typeof m.optionIndex === "number" ? m.optionIndex : 0;

            const route = S.routes[optionIndex] && S.routes[optionIndex][0];
            const totalSubjects = route && route.subjectNames ? route.subjectNames.split(',').map(s => s.trim()).filter(Boolean) : [];
            const ignoreSubjects = totalSubjects.filter(s => !includedSubjects.includes(s));

            return {
                optionIndex: optionIndex,
                ruleIndex: 0,
                label: m.label || null,
                ignoreSubjects: ignoreSubjects,
                derivedSubjectsCsv: includedSubjects.join(", "),
            };
        });
    }

    if (S.routes.length > 0) {
        if (els.nonCuetReqToggle) els.nonCuetReqToggle.checked = true;
        if (typeof EligibilityConfig.toggleNonCuet === "function") EligibilityConfig.toggleNonCuet(true);
    }
  }

  function resetForm() {
    const { els } = S;
    if (S.choicesInstances['baseQualification']) {
        S.choicesInstances['baseQualification'].setChoiceByValue("");
    }
    if (els.minOverallPercentage) els.minOverallPercentage.value = "";
    if (els.cuetReqToggle) els.cuetReqToggle.checked = false;
    if (els.nonCuetReqToggle) els.nonCuetReqToggle.checked = false;

    if (typeof EligibilityConfig.toggleCuet === "function") EligibilityConfig.toggleCuet(false);
    if (typeof EligibilityConfig.toggleNonCuet === "function") EligibilityConfig.toggleNonCuet(false);

    S.routes = [];
    S.relaxationRules = [];
    S.cuetConditions = [];
    S.cuetMeritRuleSets = [];
    S.nonCuetMeritRuleSets = [];
    S.tiebreakerConfig = [];
  }

  function openTieBreakerModal() {
      renderTieBreakerSortable();
      bootstrap.Modal.getOrCreateInstance(document.getElementById("tieBreakerModal")).show();
  }

  function renderTieBreakerSortable() {
      const container = document.getElementById("tiebreaker-sortable-list");
      if (!container) return;

      const enabledFields = S.tiebreakerConfig.map(tb => tb.field);
      const sortedOptions = [];

      S.tiebreakerConfig.forEach(tb => {
          const opt = S.tiebreakerOptions.find(o => o.value === tb.field);
          if (opt) sortedOptions.push({ ...opt, enabled: true });
      });

      S.tiebreakerOptions.forEach(opt => {
          if (!enabledFields.includes(opt.value)) {
              sortedOptions.push({ ...opt, enabled: false });
          }
      });

      container.replaceChildren();
		(sortedOptions || []).forEach((opt) => {
		    const item = document.createElement("div");
		    item.className = "list-group-item d-flex align-items-center border mb-2 rounded shadow-sm bg-white";
		    item.dataset.value = opt.value;
		
		    // ===== Drag Handle =====
		    const handle = document.createElement("i");
		    handle.className = "fas fa-grip-vertical text-muted me-3 cursor-grab handle";
            handle.classList.add("fs-1-2rem", "p-5px");

		    // ===== Switch Wrapper =====
		    const switchWrapper = document.createElement("div");
		    switchWrapper.className = "form-check form-switch mb-0 flex-grow-1";

		    // ===== Toggle Input =====
		    const input = document.createElement("input");
		    input.className = "form-check-input tb-toggle";
		    input.type = "checkbox";
		    input.id = `tb_${opt.value}`;
		    input.checked = !!opt.enabled;
		    input.classList.add("w-35px", "h-18px");

		    // ===== Label =====
		    const label = document.createElement("label");
		    label.className = "form-check-label fw-bold text-navy ms-2";
		    label.setAttribute("for", input.id);
		    label.classList.add("fs-0-95rem");
		    label.textContent = opt.label;
		
		    // ===== Assemble =====
		    switchWrapper.append(input, label);
		    item.append(handle, switchWrapper);
		
		    container.appendChild(item);
		});

      if (S.tiebreakerSortableInstance) {
          S.tiebreakerSortableInstance.destroy();
      }

      if (typeof Sortable !== 'undefined') {
          S.tiebreakerSortableInstance = new Sortable(container, {
              handle: '.handle',
              animation: 150,
              ghostClass: 'sortable-ghost'
          });
      } else {
          console.error("SortableJS is not loaded!");
      }
  }

  function applyTieBreakerConfig() {
      const container = document.getElementById("tiebreaker-sortable-list");
      if (!container) return;

      const items = Array.from(container.children);
      const newConfig = [];
      let priority = 1;

      items.forEach(item => {
          const value = item.getAttribute("data-value");
          const isChecked = item.querySelector(".tb-toggle").checked;

          if (isChecked) {
              newConfig.push({ field: value, priority: priority++ });
          }
      });

      S.tiebreakerConfig = newConfig;
      bootstrap.Modal.getOrCreateInstance(document.getElementById("tieBreakerModal")).hide();

      if(window.EligibilityPreview) EligibilityPreview.updateStudentPreview();
  }

  function init() {
    cacheEls();
    updateProgress("windows");
    loadMasterData();
    loadWindows();
    
     // Back button
  S.els.backBtn?.addEventListener("click", goBack);

  // Add Relaxation (your button fix)
  document.getElementById("btnAddRelaxation")
    ?.addEventListener("click", () => {
      if (typeof EligibilityConfig.addRelaxationRow === "function") {
        EligibilityConfig.addRelaxationRow();
      }
    });

  // CUET toggle
  S.els.cuetReqToggle?.addEventListener("change", (e) => {
    if (typeof EligibilityConfig.toggleCuet === "function") {
      EligibilityConfig.toggleCuet(e.target.checked);
    }
  });

  // NON-CUET toggle
  S.els.nonCuetReqToggle?.addEventListener("change", (e) => {
    if (typeof EligibilityConfig.toggleNonCuet === "function") {
      EligibilityConfig.toggleNonCuet(e.target.checked);
    }
  });

  // Qualification change
  S.els.baseQualificationId?.addEventListener("change", () => {
    if (window.EligibilityPreview) {
      EligibilityPreview.updateStudentPreview();
    }
  });

  // Marks input
  S.els.minOverallPercentage?.addEventListener("input", () => {
    if (window.EligibilityPreview) {
      EligibilityPreview.updateStudentPreview();
    }
  });

  // Save button
  S.els.saveBtn?.addEventListener("click", () => {
    if (typeof EligibilityConfig.saveConfiguration === "function") {
      EligibilityConfig.saveConfiguration();
    }
  });

  // Tie-breaker button
  document.getElementById("btnTieBreaker")
    ?.addEventListener("click", openTieBreakerModal);
  }
  
  document.getElementById("btnAddCuet")
  ?.addEventListener("click", () => {
    if (typeof EligibilityConfig.addCuetCondition === "function") {
      EligibilityConfig.addCuetCondition();
    } else {
      console.error("addCuetCondition not found");
    }
  });
  
  document.getElementById("btnCuetSave")
  ?.addEventListener("click", () => {
    if (typeof EligibilityConfig.applyCuetMeritConfig === "function") {
      EligibilityConfig.applyCuetMeritConfig();
    } else {
      console.error("applyCuetMeritConfig not found");
    }
  });
  
  document.getElementById("btnTieBreakerApply")
  ?.addEventListener("click", () => {
    if (typeof EligibilityConfig.applyTieBreakerConfig === "function") {
      EligibilityConfig.applyTieBreakerConfig();
    } else {
      console.error("applyTieBreakerConfig not found");
    }
  });

  Object.assign(window.EligibilityConfig, {
    init, selectWindow, selectProgramme, goBack, showToast, escapeHtml, refreshProgrammeList, loadCuetSubjectsByLevel, normalizeCuetMaster, updateProgress,
    openTieBreakerModal, applyTieBreakerConfig
  });

  document.addEventListener("DOMContentLoaded", window.EligibilityConfig.init);
})();
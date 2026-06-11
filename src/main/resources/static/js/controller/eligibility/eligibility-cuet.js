/* eligibility-cuet.js */
(function () {
  "use strict";

  const S = window.EligibilityConfigState;
  const C = window.EligibilityConfig;

  S._cuetMeritModal = S._cuetMeritModal || { optionIndex: null, applied: false };

  function normalizeCuetConditions() {
    if (!Array.isArray(S.cuetConditions)) S.cuetConditions = [];
    S.cuetConditions = S.cuetConditions.map((c) => ({
      subjectNames: (c?.subjectNames ?? "").toString(),
      cuetMinScore: c?.cuetMinScore === "" || c?.cuetMinScore === undefined ? null : c.cuetMinScore,
      cuetMinScoreMode: (c?.cuetMinScoreMode ?? "AVERAGE").toUpperCase(),
    }));
  }

  function groupCuetPapersByDomain() {
    const map = new Map();
    (S.masterData.cuetSubjects || []).forEach((p) => {
      const domain = ((p?.domainName || "OTHER") + "").trim() || "OTHER";
      if (!map.has(domain)) map.set(domain, []);
      map.get(domain).push(p);
    });
    return { domains: Array.from(map.keys()).sort(), map };
  }

  function toggleCuet(on) {
    const inputs = document.getElementById("cuet-inputs");
    if (inputs) {
        if(on) inputs.classList.remove("d-none");
        else inputs.classList.add("d-none");
    }

    if (!on) {
        S.cuetConditions = [];
        S.cuetMeritRuleSets = [];
    } else if (!S.cuetConditions.length) {
        S.cuetConditions = [{ subjectNames: "", cuetMinScore: null, cuetMinScoreMode: "AVERAGE" }];
    }
    renderCuetConditions();
    if(window.EligibilityPreview) EligibilityPreview.updateStudentPreview();
  }

  function addCuetCondition() {
    normalizeCuetConditions();
    S.cuetConditions.push({ subjectNames: "", cuetMinScore: null, cuetMinScoreMode: "AVERAGE" });
    renderCuetConditions();
    if(window.EligibilityPreview) EligibilityPreview.updateStudentPreview();
  }

  function removeCuetCondition(idx) {
    normalizeCuetConditions();
    S.cuetConditions.splice(idx, 1);
    S.cuetMeritRuleSets = (S.cuetMeritRuleSets || []).filter((m) => m.optionIndex !== idx).map(m => { if(m.optionIndex > idx) m.optionIndex--; return m; });
    renderCuetConditions();
    if(window.EligibilityPreview) EligibilityPreview.updateStudentPreview();
  }

  function updateCuetCondition(idx, field, value) {
    if (!S.cuetConditions[idx]) return;
    if (field === "cuetMinScore") {
      const n = parseInt(value, 10);
      S.cuetConditions[idx].cuetMinScore = Number.isNaN(n) ? null : n;
    } else if (field === "cuetMinScoreMode") {
      S.cuetConditions[idx].cuetMinScoreMode = (value || "AVERAGE").toUpperCase();
    }
    if(window.EligibilityPreview) EligibilityPreview.updateStudentPreview();
  }

  function toggleCuetUseInMerit(idx, checked) {
    S.cuetMeritRuleSets = S.cuetMeritRuleSets || [];
    if (!checked) {
        S.cuetMeritRuleSets = S.cuetMeritRuleSets.filter(m => m.optionIndex !== idx);
    } else {
        if(!S.cuetMeritRuleSets.find(m => m.optionIndex === idx)) {
            S.cuetMeritRuleSets.push({ optionIndex: idx, ignoreSpecs: [] });
        }
    }
    renderCuetConditions();

    // --> FIX: Triggers preview update when checking/unchecking CUET Merit toggle
    if(window.EligibilityPreview) EligibilityPreview.updateStudentPreview();
  }

  function openCuetMeritConfigModal(optionIndex) {
    S._cuetMeritModal.optionIndex = optionIndex;
    const m = (S.cuetMeritRuleSets || []).find(x => x.optionIndex === optionIndex) || { ignoreSpecs: [] };
    const isPG = ((S.programmeLevel || "UG") + "").toUpperCase() === "PG";

    document.getElementById("cuetIgnoreCommonWrap")?.classList.toggle("d-none", !isPG);
    document.getElementById("cuetIgnoreLanguage").checked = m.ignoreSpecs.includes("LANGUAGE");
    document.getElementById("cuetIgnoreAptitude").checked = m.ignoreSpecs.includes("APTITUDE");
    if(isPG) document.getElementById("cuetIgnoreCommon").checked = m.ignoreSpecs.includes("COMMON");

    const modalEl = document.getElementById("cuetMeritConfigModal");
    bootstrap.Modal.getOrCreateInstance(modalEl).show();
  }

  function applyCuetMeritConfig() {
    const idx = S._cuetMeritModal.optionIndex;
    if (idx === null) return;
    let m = S.cuetMeritRuleSets.find(x => x.optionIndex === idx);
    if (!m) { m = { optionIndex: idx, ignoreSpecs: [] }; S.cuetMeritRuleSets.push(m); }

    const isPG = ((S.programmeLevel || "UG") + "").toUpperCase() === "PG";
    m.ignoreSpecs = [];
    if (document.getElementById("cuetIgnoreLanguage").checked) m.ignoreSpecs.push("LANGUAGE");
    if (document.getElementById("cuetIgnoreAptitude").checked) m.ignoreSpecs.push("APTITUDE");
    if (isPG && document.getElementById("cuetIgnoreCommon").checked) m.ignoreSpecs.push("COMMON");

    const modalEl = document.getElementById("cuetMeritConfigModal");
    bootstrap.Modal.getOrCreateInstance(modalEl).hide();

    // --> FIX: Triggers preview update when applying advanced merit settings
    if(window.EligibilityPreview) EligibilityPreview.updateStudentPreview();
  }

  function renderCuetConditions() {
    const container = document.getElementById("cuet-conditions-container");
    if (!container) return;
    if (!document.getElementById("cuetReqToggle")?.checked) { container.innerHTML = ""; return; }

    normalizeCuetConditions();
    if (!S.cuetConditions.length) {
      container.innerHTML = `<div class="alert alert-light border text-center small text-muted">Click "Add Rule" below to define CUET requirements.</div>`;
      return;
    }

	 container.replaceChildren();
		(S.cuetConditions || []).forEach((c, idx) => {
		    const mode = (c.cuetMinScoreMode || "AVERAGE").toUpperCase();
		    const isMerit = (S.cuetMeritRuleSets || []).find(m => m.optionIndex === idx);
		
		    // ===== CARD =====
		    const card = document.createElement("div");
		    card.className = "card border border-primary mb-3 shadow-sm condition-card";
		
		    // ===== HEADER =====
		    const header = document.createElement("div");
		    header.className = "card-header bg-white d-flex justify-content-between align-items-center py-2";
		
		    const title = document.createElement("h6");
		    title.className = "fw-bold text-primary mb-0 small";
		    title.textContent = `CUET Rule ${idx + 1}`;
		
		    const removeBtn = document.createElement("button");
		    removeBtn.className = "btn btn-sm text-danger border-0";
		    removeBtn.append(
		        Object.assign(document.createElement("i"), { className: "fas fa-trash" }),
		        document.createTextNode(" Remove")
		    );
		    removeBtn.addEventListener("click", () => {
		        EligibilityConfig.removeCuetCondition(idx);
		    });
		
		    header.append(title, removeBtn);
		
		    // ===== BODY =====
		    const body = document.createElement("div");
		    body.className = "card-body p-3 bg-light";
		
		    const row = document.createElement("div");
		    row.className = "row g-3";
		
		    // ===== SUBJECT SELECT =====
		    const col1 = document.createElement("div");
		    col1.className = "col-md-12";
		
		    const label1 = document.createElement("label");
		    label1.className = "fw-bold small text-navy mb-2";
		    label1.textContent = "Select Required Subjects ";
		
		    const star = document.createElement("span");
		    star.className = "text-danger";
		    star.textContent = "*";
		    label1.appendChild(star);
		
		    const select = document.createElement("select");
		    select.id = `cuet-select-${idx}`;
		    select.multiple = true;
		    select.className = "form-select";
		
		    col1.append(label1, select);
		
		    // ===== MODE =====
		    const col2 = document.createElement("div");
		    col2.className = "col-md-6";
		
		    const label2 = document.createElement("label");
		    label2.className = "fw-bold small text-navy mb-1";
		    label2.textContent = "How should we check their score?";
		
		    const modeSelect = document.createElement("select");
		    modeSelect.id = `cuet-mode-${idx}`;
		    modeSelect.className = "form-select form-select-sm";
		
		    const opt1 = new Option("Average of all selected subjects", "AVERAGE", false, mode === "AVERAGE");
		    const opt2 = new Option("Must pass each subject individually", "INDIVIDUAL", false, mode === "INDIVIDUAL");
		
		    modeSelect.append(opt1, opt2);
		
		    modeSelect.addEventListener("change", (e) => {
		        EligibilityConfig.updateCuetCondition(idx, 'cuetMinScoreMode', e.target.value);
		    });
		
		    col2.append(label2, modeSelect);
		
		    // ===== SCORE =====
		    const col3 = document.createElement("div");
		    col3.className = "col-md-6";
		
		    const label3 = document.createElement("label");
		    label3.className = "fw-bold small text-navy mb-1";
		    label3.textContent = "Minimum Score Required (Optional)";
		
		    const input = document.createElement("input");
		    input.type = "number";
		    input.className = "form-control form-control-sm";
		    input.value = c.cuetMinScore ?? "";
		    input.placeholder = "e.g., 120";
		
		    input.addEventListener("input", (e) => {
		        EligibilityConfig.updateCuetCondition(idx, 'cuetMinScore', e.target.value);
		    });
		
		    col3.append(label3, input);
		
		    // ===== MERIT SECTION =====
		    const col4 = document.createElement("div");
		    col4.className = "col-12 mt-3 pt-3 border-top";
		
		    const flex = document.createElement("div");
		    flex.className = "d-flex align-items-center justify-content-between";
		
		    const switchWrap = document.createElement("div");
		    switchWrap.className = "form-check form-switch mb-0";
		
		    const toggle = document.createElement("input");
		    toggle.type = "checkbox";
		    toggle.className = "form-check-input";
		    toggle.id = `meritToggleCuet${idx}`;
		    toggle.checked = !!isMerit;
		
		    toggle.addEventListener("change", (e) => {
		        EligibilityConfig.toggleCuetUseInMerit(idx, e.target.checked);
		    });
		
		    const toggleLabel = document.createElement("label");
		    toggleLabel.className = "form-check-label fw-bold text-navy ms-1";
		    toggleLabel.setAttribute("for", toggle.id);
		    toggleLabel.textContent = "☑️ Use these subjects to calculate final Merit Rank";
		
		    switchWrap.append(toggle, toggleLabel);
		
		    flex.appendChild(switchWrap);
		
		    // ===== ADVANCED BUTTON (conditional) =====
		    if (isMerit) {
		        const advBtn = document.createElement("button");
		        advBtn.type = "button";
		        advBtn.className = "btn btn-xs btn-outline-secondary";
		
		        const icon = document.createElement("i");
		        icon.className = "fas fa-cog me-1";
		
		        advBtn.append(icon, document.createTextNode(" Advanced Merit Settings"));
		
		        advBtn.addEventListener("click", () => {
		            EligibilityConfig.openCuetMeritConfig(idx);
		        });
		
		        flex.appendChild(advBtn);
		    }
		
		    col4.appendChild(flex);
		
		    // ===== ASSEMBLE =====
		    row.append(col1, col2, col3, col4);
		    body.appendChild(row);
		    card.append(header, body);
		
		    container.appendChild(card);
		});

      const { domains, map } = groupCuetPapersByDomain();

      S.cuetConditions.forEach((c, idx) => {
          const el = document.getElementById(`cuet-select-${idx}`);
          const selectedSubjects = (c.subjectNames || "").split(",").map(s => s.trim());

          const groupedOptions = domains.map(domain => {
              return {
                  label: domain,
                  id: domain,
                  choices: map.get(domain).map(sub => ({
                      value: sub.paperCode || sub.displayName,
                      label: `${sub.displayName} (${sub.paperCode || 'N/A'})`,
                      selected: selectedSubjects.includes(sub.paperCode || sub.displayName)
                  }))
              };
          });

          if(S.choicesInstances[`cuet_${idx}`]) S.choicesInstances[`cuet_${idx}`].destroy();

          const choice = new Choices(el, {
              removeItemButton: true,
              searchPlaceholderValue: "Search by subject name or code...",
              placeholder: true,
              placeholderValue: "👆 Click here to search and select CUET papers...",
              itemSelectText: "",
              renderChoiceLimit: -1,
              shouldSort: false
          });

          choice.setChoices(groupedOptions, 'value', 'label', true);

          el.addEventListener('change', () => {
              const selectedVals = choice.getValue(true);
              S.cuetConditions[idx].subjectNames = selectedVals.join(',');

              const modeSelect = document.getElementById(`cuet-mode-${idx}`);
              if (selectedVals.length > 1) {
                  S.cuetConditions[idx].cuetMinScoreMode = 'AVERAGE';
                  if(modeSelect) modeSelect.value = 'AVERAGE';
              } else {
                  S.cuetConditions[idx].cuetMinScoreMode = 'INDIVIDUAL';
                  if(modeSelect) modeSelect.value = 'INDIVIDUAL';
              }
              if(window.EligibilityPreview) EligibilityPreview.updateStudentPreview();
          });
      });
  }

  Object.assign(window.EligibilityConfig, {
    normalizeCuetConditions, toggleCuet, addCuetCondition, removeCuetCondition, updateCuetCondition, renderCuetConditions,
    toggleCuetUseInMerit, openCuetMeritConfig: openCuetMeritConfigModal, applyCuetMeritConfig
  });
})();
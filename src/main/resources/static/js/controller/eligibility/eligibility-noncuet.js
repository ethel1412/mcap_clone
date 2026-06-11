/* eligibility-noncuet.js */
(function () {
  "use strict";

  const S = window.EligibilityConfigState;
  const C = window.EligibilityConfig;

  S._noncuetMeritModal = S._noncuetMeritModal || { optionIndex: null, applied: false };

  function getUsedCategoryCodes(exceptIndex) {
    return (S.relaxationRules || []).map((r, i) => (i === exceptIndex ? null : (r?.categoryCode || "").trim())).filter(Boolean);
  }

  function getSortedCastes() {
    return [...(S.masterData.castes || [])].sort((a, b) => {
      const ap = typeof a.priority === "number" ? a.priority : 999;
      const bp = typeof b.priority === "number" ? b.priority : 999;
      return ap !== bp ? ap - bp : (a.displayName || "").localeCompare(b.displayName || "");
    });
  }

  function renderRelaxationRows() {
    const { els } = S;
    if (!els.relaxationsContainer) return;
    if (!S.relaxationRules.length) { els.relaxationsContainer.innerHTML = `<div class="col-12 text-muted small fst-italic">No exceptions added.</div>`; return; }

    const castes = getSortedCastes();
     els.relaxationsContainer.replaceChildren();
		(S.relaxationRules || []).forEach((rule, idx) => {
		    const used = new Set(getUsedCategoryCodes(idx));
		    const selectedCode = (rule?.categoryCode || "").trim();
		
		    // ===== ROW WRAPPER =====
		    const outer = document.createElement("div");
		    outer.className = "col-12 border-bottom pb-2 mb-2";
		
		    const row = document.createElement("div");
		    row.className = "row align-items-center";
		
		    // ===== COL 1: SELECT =====
		    const col1 = document.createElement("div");
		    col1.className = "col-md-7";
		
		    const select = document.createElement("select");
		    select.className = "form-select form-select-sm";
		
		    // Default option
		    const defaultOpt = document.createElement("option");
		    defaultOpt.value = "";
		    defaultOpt.textContent = "Select Category";
		    select.appendChild(defaultOpt);
		
		    // Options (same filtering logic)
		    castes
		        .filter((c) => {
		            const code = (c.categoryCode || "").trim();
		            return code === selectedCode || !used.has(code);
		        })
		        .forEach((c) => {
		            const opt = document.createElement("option");
		            opt.value = c.categoryCode || "";
		            opt.textContent = c.displayName || "";
		
		            if ((c.categoryCode || "") === selectedCode) {
		                opt.selected = true;
		            }
		
		            select.appendChild(opt);
		        });
		
		    select.addEventListener("change", (e) => {
		        EligibilityConfig.updateRelaxation(idx, 'categoryCode', e.target.value);
		    });
		
		    col1.appendChild(select);
		
		    // ===== COL 2: INPUT =====
		    const col2 = document.createElement("div");
		    col2.className = "col-md-4";
		
		    const group = document.createElement("div");
		    group.className = "input-group input-group-sm";
		
		    const input = document.createElement("input");
		    input.type = "number";
		    input.className = "form-control text-center";
		    input.value = rule.relaxationValue ?? 0;
		
		    input.addEventListener("change", (e) => {
		        EligibilityConfig.updateRelaxation(idx, 'relaxationValue', e.target.value);
		    });
		
		    const percent = document.createElement("span");
		    percent.className = "input-group-text";
		    percent.textContent = "%";
		
		    group.append(input, percent);
		    col2.appendChild(group);
		
		    // ===== COL 3: REMOVE BUTTON =====
		    const col3 = document.createElement("div");
		    col3.className = "col-md-1 text-end";
		
		    const btn = document.createElement("button");
		    btn.className = "btn btn-sm text-danger border-0";
		
		    const icon = document.createElement("i");
		    icon.className = "fas fa-times";
		
		    btn.appendChild(icon);
		
		    btn.addEventListener("click", () => {
		        EligibilityConfig.removeRelaxationRow(idx);
		    });
		
		    col3.appendChild(btn);
		
		    // ===== ASSEMBLE =====
		    row.append(col1, col2, col3);
		    outer.appendChild(row);
		
		    els.relaxationsContainer.appendChild(outer);
		});
  }

  function addRelaxationRow() {
    const used = new Set((S.relaxationRules || []).map((r) => (r?.categoryCode || "").trim()).filter(Boolean));
    const next = getSortedCastes().find((c) => !used.has((c.categoryCode || "").trim()));
    S.relaxationRules.push({ categoryCode: next ? next.categoryCode : "", relaxationValue: 0 });
    renderRelaxationRows();
    if(window.EligibilityPreview) EligibilityPreview.updateStudentPreview();
  }

  function removeRelaxationRow(idx) {
      S.relaxationRules.splice(idx, 1);
      renderRelaxationRows();
      if(window.EligibilityPreview) EligibilityPreview.updateStudentPreview();
  }

  function updateRelaxation(idx, f, v) {
      if (S.relaxationRules[idx]) { S.relaxationRules[idx][f] = v; renderRelaxationRows(); }
      if(window.EligibilityPreview) EligibilityPreview.updateStudentPreview();
  }

  function normalizeRoutesToSingleRulePerCondition() {
    if (!Array.isArray(S.routes)) S.routes = [];
    const flat = [];
    S.routes.forEach((reqs) => {
      if (!Array.isArray(reqs) || !reqs.length) return;
      reqs.forEach((r) => flat.push([{ subjectNames: r?.subjectNames ?? "", minPercentage: r?.minPercentage ?? r?.minScore ?? 0 }]));
    });
    S.routes = flat;
  }

  function toggleNonCuet(on) {
    const inputs = document.getElementById("noncuet-inputs");
    if (inputs) {
        if(on) inputs.classList.remove("d-none");
        else inputs.classList.add("d-none");
    }

    if (!on) { S.routes = []; S.nonCuetMeritRuleSets = []; }
    else if (!S.routes.length) S.routes = [[{ subjectNames: "", minPercentage: 0 }]];
    renderRoutes();
    if(window.EligibilityPreview) EligibilityPreview.updateStudentPreview();
  }

  function addCondition() {
    normalizeRoutesToSingleRulePerCondition();
    S.routes.push([{ subjectNames: "", minPercentage: 0 }]);
    renderRoutes();
    if(window.EligibilityPreview) EligibilityPreview.updateStudentPreview();
  }

  function removeCondition(idx) {
    normalizeRoutesToSingleRulePerCondition();
    S.routes.splice(idx, 1);
    S.nonCuetMeritRuleSets = (S.nonCuetMeritRuleSets || []).filter((m) => m.optionIndex !== idx).map(m => { if(m.optionIndex > idx) m.optionIndex--; return m;});
    renderRoutes();
    if(window.EligibilityPreview) EligibilityPreview.updateStudentPreview();
  }

  function toggleNonCuetUseInMerit(idx, checked) {
    S.nonCuetMeritRuleSets = S.nonCuetMeritRuleSets || [];
    if (!checked) {
        S.nonCuetMeritRuleSets = S.nonCuetMeritRuleSets.filter(m => m.optionIndex !== idx);
    } else {
        if(!S.nonCuetMeritRuleSets.find(m => m.optionIndex === idx)) {
            S.nonCuetMeritRuleSets.push({ optionIndex: idx, ignoreSubjects: [] });
        }
    }
    renderRoutes();
    if(window.EligibilityPreview) EligibilityPreview.updateStudentPreview();
  }

  function openNonCuetMeritConfigModal(optionIndex) {
    const idx = parseInt(optionIndex, 10);
    S._noncuetMeritModal.optionIndex = idx;

    const ruleSet = (S.nonCuetMeritRuleSets || []).find(x => x.optionIndex === idx) || { ignoreSubjects: [] };
    const ignoreList = Array.isArray(ruleSet.ignoreSubjects) ? ruleSet.ignoreSubjects : [];

    let subjects = [];
    if (S.routes && S.routes[idx] && S.routes[idx][0] && S.routes[idx][0].subjectNames) {
        subjects = S.routes[idx][0].subjectNames.split(",").map(s => s.trim()).filter(Boolean);
    }

    const listWrap = document.getElementById("nonCuetIgnoreList");
    if (listWrap) {
        if (subjects.length > 0) {
            	listWrap.replaceChildren();
				(subjects || []).forEach((s, i) => {
				    const wrapper = document.createElement("div");
				    wrapper.className = "form-check mb-2";
				
				    // ===== Checkbox =====
				    const input = document.createElement("input");
				    input.type = "checkbox";
				    input.className = "form-check-input nc-ignore-cb";
				    input.id = `nc_ig_${idx}_${i}`;
				    input.value = s;
				    input.checked = ignoreList.includes(s);
				
				    // ===== Label =====
				    const label = document.createElement("label");
				    label.className = "form-check-label small fw-bold";
				    label.setAttribute("for", input.id);
				    label.textContent = s;
				
				    // ===== Assemble =====
				    wrapper.append(input, label);
				    listWrap.appendChild(wrapper);
				});
        } else {
            listWrap.innerHTML = `<div class="text-muted small">No subjects selected for this rule yet.</div>`;
        }

        listWrap.querySelectorAll('.nc-ignore-cb').forEach(cb => {
            cb.addEventListener('change', function() {
                updateNonCuetModalPreview(subjects);
            });
        });
    }

    updateNonCuetModalPreview(subjects);

    const modalEl = document.getElementById("nonCuetMeritConfigModal");
    if (modalEl) {
        bootstrap.Modal.getOrCreateInstance(modalEl).show();
    }
  }

  function updateNonCuetModalPreview(allSubjects) {
      const previewEl = document.getElementById("nonCuetMeritPreviewIncluded");
      if (!previewEl) return;

      const listWrap = document.getElementById("nonCuetIgnoreList");
      const ignored = listWrap ? Array.from(listWrap.querySelectorAll('.nc-ignore-cb:checked')).map(cb => cb.value) : [];

      const included = allSubjects.filter(s => !ignored.includes(s));

      if (included.length === 0) {
          previewEl.innerHTML = `<span class="text-danger fw-bold">No subjects included (Ranking impossible).</span>`;
      } else {
          previewEl.textContent = included.join(", ");
      }
  }

  function applyNonCuetMeritConfig() {
    const idx = S._noncuetMeritModal.optionIndex;
    if (idx === null || idx === undefined) return;

    let m = S.nonCuetMeritRuleSets.find(x => x.optionIndex === idx);
    if (!m) {
        m = { optionIndex: idx, ignoreSubjects: [] };
        S.nonCuetMeritRuleSets.push(m);
    }

    const listWrap = document.getElementById("nonCuetIgnoreList");
    const checked = listWrap ? Array.from(listWrap.querySelectorAll('.nc-ignore-cb:checked')).map(cb => cb.value) : [];
    m.ignoreSubjects = checked;

    const modalEl = document.getElementById("nonCuetMeritConfigModal");
    if(modalEl) bootstrap.Modal.getOrCreateInstance(modalEl).hide();

    if(window.EligibilityPreview) EligibilityPreview.updateStudentPreview();
  }

  function renderRoutes() {
    normalizeRoutesToSingleRulePerCondition();
    const container = document.getElementById("routes-container");
    if (!container) return;
    if (!document.getElementById("nonCuetReqToggle")?.checked) { container.innerHTML = ""; return; }

    if (!S.routes.length) { container.innerHTML = `<div class="alert alert-light border text-center small text-muted">Click "Add Rule" below to define Direct Admission requirements.</div>`; return; }

	container.replaceChildren();
	
	(S.routes || []).forEach((reqs, cIdx) => {
	    const req = reqs[0];
	    const isMerit = (S.nonCuetMeritRuleSets || []).find(m => m.optionIndex === cIdx);
	
	    // ===== CARD =====
	    const card = document.createElement("div");
	    card.className = "card border border-info mb-3 shadow-sm condition-card";
	
	    // ===== HEADER =====
	    const header = document.createElement("div");
	    header.className = "card-header bg-white d-flex justify-content-between align-items-center py-2";
	
	    const title = document.createElement("h6");
	    title.className = "fw-bold text-info mb-0 small";
	    title.textContent = `Direct Admission Rule ${cIdx + 1}`;
	
	    const removeBtn = document.createElement("button");
	    removeBtn.className = "btn btn-sm text-danger border-0";
	    removeBtn.append(
	        Object.assign(document.createElement("i"), { className: "fas fa-trash" }),
	        document.createTextNode(" Remove")
	    );
	    removeBtn.addEventListener("click", () => {
	        EligibilityConfig.removeCondition(cIdx);
	    });
	
	    header.append(title, removeBtn);
	
	    // ===== BODY =====
	    const body = document.createElement("div");
	    body.className = "card-body p-3 bg-light";
	
	    const row = document.createElement("div");
	    row.className = "row g-3";
	
	    // ===== SUBJECT SELECT =====
	    const col1 = document.createElement("div");
	    col1.className = "col-md-8";
	
	    const label1 = document.createElement("label");
	    label1.className = "fw-bold small text-navy mb-2";
	    label1.textContent = "Required Subjects ";
	
	    const star = document.createElement("span");
	    star.className = "text-danger";
	    star.textContent = "*";
	    label1.appendChild(star);
	
	    const select = document.createElement("select");
	    select.id = `noncuet-select-${cIdx}`;
	    select.multiple = true;
	    select.className = "form-select";
	
	    col1.append(label1, select);
	
	    // ===== MIN % =====
	    const col2 = document.createElement("div");
	    col2.className = "col-md-4";
	
	    const label2 = document.createElement("label");
	    label2.className = "fw-bold small text-navy mb-1";
	    label2.textContent = "Minimum Percentage Required";
	
	    const inputGroup = document.createElement("div");
	    inputGroup.className = "input-group input-group-sm";
	
	    const input = document.createElement("input");
	    input.type = "number";
	    input.className = "form-control";
	    input.value = req.minPercentage ?? 0;
	
	    input.addEventListener("change", (e) => {
	        EligibilityConfig.updateReq(cIdx, 0, 'minPercentage', e.target.value);
	    });
	
	    const percent = document.createElement("span");
	    percent.className = "input-group-text";
	    percent.textContent = "%";
	
	    inputGroup.append(input, percent);
	    col2.append(label2, inputGroup);
	
	    // ===== MERIT SECTION =====
	    const col3 = document.createElement("div");
	    col3.className = "col-12 mt-3 pt-3 border-top";
	
	    const flex = document.createElement("div");
	    flex.className = "d-flex align-items-center justify-content-between";
	
	    const switchWrap = document.createElement("div");
	    switchWrap.className = "form-check form-switch mb-0";
	
	    const toggle = document.createElement("input");
	    toggle.type = "checkbox";
	    toggle.className = "form-check-input";
	    toggle.id = `meritToggleNonCuet${cIdx}`;
	    toggle.checked = !!isMerit;
	
	    toggle.addEventListener("change", (e) => {
	        EligibilityConfig.toggleNonCuetUseInMerit(cIdx, e.target.checked);
	    });
	
	    const toggleLabel = document.createElement("label");
	    toggleLabel.className = "form-check-label fw-bold text-navy ms-1";
	    toggleLabel.setAttribute("for", toggle.id);
	    toggleLabel.textContent = "☑️ Use these subjects to calculate final Merit Rank";
	
	    switchWrap.append(toggle, toggleLabel);
	    flex.appendChild(switchWrap);
	
	    // ===== ADVANCED BUTTON =====
	    if (isMerit) {
	        const advBtn = document.createElement("button");
	        advBtn.type = "button";
	        advBtn.className = "btn btn-xs btn-outline-secondary";
	
	        const icon = document.createElement("i");
	        icon.className = "fas fa-cog me-1";
	
	        advBtn.append(icon, document.createTextNode(" Advanced Merit Settings"));
	
	        advBtn.addEventListener("click", () => {
	            EligibilityConfig.openNonCuetMeritConfig(cIdx);
	        });
	
	        flex.appendChild(advBtn);
	    }
	
	    col3.appendChild(flex);
	
	    // ===== ASSEMBLE =====
	    row.append(col1, col2, col3);
	    body.appendChild(row);
	    card.append(header, body);
	
	    container.appendChild(card);
	});

      S.routes.forEach((reqs, cIdx) => {
          const req = reqs[0];
          const el = document.getElementById(`noncuet-select-${cIdx}`);
          const selectedSubjects = (req.subjectNames || "").split(",").map(s => s.trim());

          const options = S.masterData.subjects.map(sub => ({
              value: sub.subjectName,
              label: sub.subjectName,
              selected: selectedSubjects.includes(sub.subjectName)
          }));

          if(S.choicesInstances[`noncuet_${cIdx}`]) S.choicesInstances[`noncuet_${cIdx}`].destroy();

          const choice = new Choices(el, {
              removeItemButton: true,
              searchPlaceholderValue: "Search subjects...",
              placeholder: true,
              placeholderValue: "👆 Click here to search and select past subjects...",
              itemSelectText: "",
              renderChoiceLimit: -1,
              shouldSort: false
          });

          choice.setChoices(options, 'value', 'label', true);

          el.addEventListener('change', () => {
              const selectedVals = choice.getValue(true);
              S.routes[cIdx][0].subjectNames = selectedVals.join(',');
              if(window.EligibilityPreview) EligibilityPreview.updateStudentPreview();
          });
      });
  }

  function updateReq(ri, si, f, v) {
      if(S.routes[ri]) S.routes[ri][0][f] = Number.isFinite(Number(v)) ? Number(v) : 0;
      if(window.EligibilityPreview) EligibilityPreview.updateStudentPreview();
  }

  Object.assign(window.EligibilityConfig, {
    renderRelaxationRows, addRelaxationRow, removeRelaxationRow, updateRelaxation, toggleNonCuet, normalizeRoutesToSingleRulePerCondition, addCondition, removeCondition, renderRoutes, updateReq,
    toggleNonCuetUseInMerit, openNonCuetMeritConfig: openNonCuetMeritConfigModal, applyNonCuetMeritConfig
  });
})();
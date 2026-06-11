/* eligibility-merit-save.js */
(function () {
  "use strict";
  
  const S = window.EligibilityConfigState;
  const C = window.EligibilityConfig;

  // -------------------- SAVE OPERATION --------------------
  function saveConfiguration() {
    const isCuet = !!(S.els.cuetReqToggle && S.els.cuetReqToggle.checked);
    const isNonCuet = !!(S.els.nonCuetReqToggle && S.els.nonCuetReqToggle.checked);

    if (typeof C.normalizeRoutesToSingleRulePerCondition === "function") C.normalizeRoutesToSingleRulePerCondition();
    if (typeof C.normalizeCuetConditions === "function") C.normalizeCuetConditions();

    const payload = {
      // CHANGED: Use admissionCode to match the backend DTO precisely
      admissionCode: S.currentWindowCode,
      programmeId: S.currentProgrammeId,
      baseQualificationId: S.els.baseQualificationId?.value ? parseInt(S.els.baseQualificationId.value, 10) : null,
      minOverallPercentage: parseFloat(S.els.minOverallPercentage?.value || "0"),
      categoryRelaxations: (S.relaxationRules || []).filter((r) => r && (r.categoryCode || "").trim()),
      cuetRequired: isCuet,
      ruleSets: [],
      meritRuleSets: [],
      tiebreakerConfig: S.tiebreakerConfig && S.tiebreakerConfig.length > 0 ? JSON.stringify(S.tiebreakerConfig) : "",
    };

    if (!payload.baseQualificationId) return C.showToast("Minimum Qualification is required", "warning");
    if (!isCuet && !isNonCuet) return C.showToast("Please enable at least one Admission Route", "warning");

    try {
      // -------- Build CUET ruleSets --------
      if (isCuet) {
        if (!(S.cuetConditions || []).length) throw new Error("Add at least one CUET eligibility condition.");
        S.cuetConditions.forEach((c, idx) => {
          const codes = (c?.subjectNames || "").split(",").map((s) => s.trim()).filter(Boolean);
          if (!codes.length) throw new Error(`CUET Rule ${idx + 1}: Please select at least 1 paper.`);
          const cuetScore = c.cuetMinScore ? Number(c.cuetMinScore) : null;

          payload.ruleSets.push({
            description: `CUET Rule ${idx + 1}`,
            subjectRequirements: [{
                subjectNames: codes,
                minScore: cuetScore,
                calculationType: codes.length > 1 && c.cuetMinScoreMode === "AVERAGE" ? "AGGREGATE_AVERAGE" : "INDIVIDUAL_SUBJECT",
                scoreSource: "CUET"
            }]
          });
        });
      }

      // -------- Build NON-CUET ruleSets --------
      if (isNonCuet) {
        if (!(S.routes || []).length) throw new Error("Add at least one Direct Admission condition.");
        S.routes.forEach((reqs, idx) => {
          const r = reqs[0];
          const subjects = (r?.subjectNames || "").split(",").map((s) => s.trim()).filter(Boolean);
          if (!subjects.length) throw new Error(`Direct Admission Rule ${idx + 1}: Please select at least 1 subject.`);

          payload.ruleSets.push({
            description: `NON-CUET Rule ${idx + 1}`,
            subjectRequirements: [{
                subjectNames: subjects,
                minScore: r.minPercentage || r.minScore || 0,
                calculationType: subjects.length > 1 ? "AGGREGATE_AVERAGE" : "INDIVIDUAL_SUBJECT",
                scoreSource: "NON_CUET"
            }]
          });
        });
      }
    } catch (e) {
      return C.showToast(e?.message || "Invalid input", "warning");
    }

    // -------- Build Merit ruleSets --------
    let meritPriority = 0;
    const merged = [];

    if (isCuet && S.cuetMeritRuleSets) {
        S.cuetMeritRuleSets.sort((a,b) => a.optionIndex - b.optionIndex).forEach(m => {
            const rule = S.cuetConditions[m.optionIndex];
            if(!rule) return;
            const allCodes = rule.subjectNames ? rule.subjectNames.split(",").map(s => s.trim()).filter(Boolean) : [];
            const ignoreSpecs = new Set(m.ignoreSpecs || []);

            const explicitCodes = allCodes.filter(code => {
                const paper = S.masterData.cuetSubjects.find(p => p.paperCode === code || p.displayName === code);
                const spec = (paper && paper.spec) ? paper.spec.toUpperCase() : "";
                return !spec || !ignoreSpecs.has(spec);
            });

            merged.push({
                sourceType: "CUET",
                optionIndex: m.optionIndex,
                ruleIndex: meritPriority++,
                label: `CUET Rule ${m.optionIndex + 1}`,
                meritSubjects: explicitCodes
            });
        });
    }

    if (isNonCuet && S.nonCuetMeritRuleSets) {
        S.nonCuetMeritRuleSets.sort((a,b) => a.optionIndex - b.optionIndex).forEach(m => {
            const rule = S.routes[m.optionIndex]?.[0];
            if(!rule) return;
            const allSubjects = rule.subjectNames ? rule.subjectNames.split(",").map(s => s.trim()).filter(Boolean) : [];
            const explicitIgnored = Array.isArray(m.ignoreSubjects) ? m.ignoreSubjects : [];

            const finalSubjects = allSubjects.filter(s => !explicitIgnored.includes(s));

            merged.push({
                sourceType: "NON_CUET",
                optionIndex: m.optionIndex,
                ruleIndex: meritPriority++,
                label: `NON-CUET Rule ${m.optionIndex + 1}`,
                meritSubjects: finalSubjects
            });
        });
    }

    payload.meritRuleSets = merged;

    if (!payload.meritRuleSets.length) {
        return C.showToast("You must select at least one rule to generate the Merit Rank.", "warning");
    }

    if (S.els.saveBtn) S.els.saveBtn.disabled = true;
    const csrfTokenMeta = document.querySelector('meta[name="_csrf"]');
	const csrfHeaderMeta = document.querySelector('meta[name="_csrf_header"]');
	
	let headers = {};
	
	if (csrfTokenMeta && csrfHeaderMeta) {
	  headers[csrfHeaderMeta.getAttribute("content")] = csrfTokenMeta.getAttribute("content");
	}

    axios.post(`${S.API_BASE}/save`, payload, { headers })
      .then(() => {
        C.showToast("Rules Saved Successfully", "success");
        return C.refreshProgrammeList().then(() => C.goBack());
      })
      .catch((e) => {
        C.showToast("Save error. Please check inputs.", "danger");
      })
      .finally(() => {
        if (S.els.saveBtn) S.els.saveBtn.disabled = false;
      });
  }

  Object.assign(window.EligibilityConfig, { saveConfiguration });
})();
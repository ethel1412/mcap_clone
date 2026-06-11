/* eligibility-preview.js */
(function () {
  "use strict";

  const S = window.EligibilityConfigState;

  function getCuetNameWithoutCode(code) {
      const cleanCode = code.trim();
      const sub = (S.masterData.cuetSubjects || []).find(s => s.paperCode === cleanCode || s.displayName === cleanCode);
      return sub ? sub.displayName : cleanCode;
  }

  const EligibilityPreview = {
   	updateStudentPreview: function () {
	    const container = document.getElementById("studentPreviewContainer");
	    if (!container) return;
	
	    container.replaceChildren();
	
	    const ul = document.createElement("ul");
	    ul.className = "list-group list-group-flush border-0";
	
	    /* ================= 1. EDUCATION ================= */
	
	    const qualSelect = document.getElementById("baseQualificationId");
	    const qualName = qualSelect && qualSelect.value
	        ? (qualSelect.options[qualSelect.selectedIndex]?.text || "[Qualification]")
	        : "[Qualification]";
	    const minMarks = document.getElementById("minOverallPercentage")?.value || "[X]";
	
	    const liEdu = document.createElement("li");
	    liEdu.className = "list-group-item bg-transparent px-0 pt-0 pb-3 mb-3 border-bottom border-light";
	
	    const wrap = document.createElement("div");
	    wrap.className = "d-flex align-items-start";
	
	    const iconBox = document.createElement("div");
	    // CSP FIX: Replaced inline style with class
	    iconBox.className = "bg-primary text-white rounded-circle d-flex align-items-center justify-content-center me-3 shadow-sm icon-box-32";

	    const icon = document.createElement("i");
	    icon.className = "fas fa-graduation-cap";
	    iconBox.appendChild(icon);

	    const content = document.createElement("div");

	    const h6 = document.createElement("h6");
	    h6.className = "fw-bold mb-1";
	    h6.textContent = "Education Required";

	    const p = document.createElement("p");
	    p.className = "mb-0 text-secondary";
	    p.append("You must have passed ");
	    const s1 = document.createElement("strong");
	    s1.textContent = qualName;
	    const s2 = document.createElement("strong");
	    s2.textContent = `${minMarks}%`;
	    p.append(s1, " with a minimum of ", s2, " overall.");

	    content.append(h6, p);

	    // Relaxations
	    if (S.relaxationRules?.length) {
	        const relaxDiv = document.createElement("div");
	        relaxDiv.className = "mt-2 p-2 bg-white rounded border small";

	        const infoIcon = document.createElement("i");
	        infoIcon.className = "fas fa-info-circle text-primary me-1";
	        relaxDiv.append(infoIcon, document.createTextNode("Relaxations: "));

	        S.relaxationRules.forEach((r, i) => {
	            const strong = document.createElement("strong");
	            strong.textContent = r.categoryCode;
	            relaxDiv.append(strong, `: ${r.relaxationValue}%`);
	            if (i < S.relaxationRules.length - 1) relaxDiv.append(", ");
	        });

	        content.appendChild(relaxDiv);
	    }

	    wrap.append(iconBox, content);
	    liEdu.appendChild(wrap);
	    ul.appendChild(liEdu);

	    /* ================= 2. ROUTES ================= */

	    const isCuet = document.getElementById("cuetReqToggle")?.checked;
	    const isNonCuet = document.getElementById("nonCuetReqToggle")?.checked;

	    if (!isCuet && !isNonCuet) {
	        const warn = document.createElement("div");
	        warn.className = "alert alert-warning small border-0";

	        const warnIcon = document.createElement("i");
	        warnIcon.className = "fas fa-exclamation-triangle me-2";

	        warn.append(warnIcon, "Please enable an Admission Route (CUET or Direct) to see preview.");
	        ul.appendChild(warn);
	    }

	    /* ================= CUET ================= */

	    if (isCuet) {
	        const li = document.createElement("li");
	        li.className = "list-group-item bg-transparent px-0 py-3 mb-3 border-bottom border-light";

	        const wrap2 = document.createElement("div");
	        wrap2.className = "d-flex align-items-start";

	        const iconBox2 = document.createElement("div");
	        // CSP FIX: Replaced inline style with class
	        iconBox2.className = "bg-success text-white rounded-circle d-flex align-items-center justify-content-center me-3 shadow-sm icon-box-32";
	        iconBox2.appendChild(Object.assign(document.createElement("i"), { className: "fas fa-file-signature" }));
	
	        const content2 = document.createElement("div");
	
	        const h = document.createElement("h6");
	        h.className = "fw-bold mb-2";
	        h.textContent = "Route 1: Entrance Exam (CUET)";
	
	        const list = document.createElement("ul");
	        list.className = "list-unstyled small text-secondary mb-0";
	
	        if (S.cuetConditions?.length) {
	            S.cuetConditions.forEach((c, idx) => {
	                const liRule = document.createElement("li");
	                liRule.className = "mb-2";
	
	                const checkIcon = document.createElement("i");
	                checkIcon.className = "fas fa-check text-success me-1";
	
	                const subjects = c.subjectNames
	                    ? c.subjectNames.split(",").map(getCuetNameWithoutCode).join(" and ")
	                    : "[Subjects]";
	
	                const mode = c.cuetMinScoreMode === "AVERAGE"
	                    ? "an average score"
	                    : "a minimum score in each";
	
	                const score = c.cuetMinScore ? ` of at least ${c.cuetMinScore}` : "";
	
	                liRule.append(checkIcon, `Take `, document.createTextNode(subjects),
	                    ` (requiring ${mode}${score})`);
	
	                list.appendChild(liRule);
	
	                if (idx < S.cuetConditions.length - 1) {
	                    const orWrap = document.createElement("div");
	                    orWrap.className = "my-1";
	
	                    const badge = document.createElement("span");
	                    badge.className = "badge bg-light text-muted border px-2 py-1";
	                    badge.textContent = "OR";
	
	                    orWrap.appendChild(badge);
	                    list.appendChild(orWrap);
	                }
	            });
	        } else {
	            const err = document.createElement("li");
	            err.className = "text-danger";
	            err.textContent = "No CUET rules defined yet.";
	            list.appendChild(err);
	        }
	
	        content2.append(h, list);
	        wrap2.append(iconBox2, content2);
	        li.appendChild(wrap2);
	        ul.appendChild(li);
	    }
	
	    /* ================= DIRECT ================= */
	
	    if (isNonCuet) {
	        const li = document.createElement("li");
	        li.className = "list-group-item bg-transparent px-0 py-3 border-bottom border-light mb-3";
	
	        const list = document.createElement("ul");
	
	        if (S.routes?.length) {
	            S.routes.forEach((reqs, idx) => {
	                const r = reqs[0];
	
	                const liRule = document.createElement("li");
	
	                const subjects = r.subjectNames ? r.subjectNames.split(",").join(" and ") : "[Subjects]";
	                const percent = r.minPercentage || r.minScore || 0;
	
	                liRule.textContent = `Have studied ${subjects} and scored at least ${percent}%`;
	                list.appendChild(liRule);
	
	                if (idx < S.routes.length - 1) {
	                    const orDiv = document.createElement("div");
	                    orDiv.textContent = "OR";
	                    list.appendChild(orDiv);
	                }
	            });
	        }
	
	        li.appendChild(list);
	        ul.appendChild(li);
	    }
	
	    /* ================= FINAL ================= */
	
	    container.appendChild(ul);
	}
  };

  window.EligibilityPreview = EligibilityPreview;
})();
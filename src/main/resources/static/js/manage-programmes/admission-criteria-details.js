document.addEventListener('DOMContentLoaded', function(){
    // USE Thymeleaf to set these server-side!
    const context = window.PROGRAMME_CONTEXT || {
        programmeId: "1",
        programmeType: "UG", // UG or PG
        programmeName: "B.Sc Physics",
        stream: "SCIENCE" // For UG only
    };
    document.getElementById('programmeTypeDisplay').textContent = context.programmeType;
    document.getElementById('programmeNameDisplay').textContent = context.programmeName;
    if (context.programmeType === 'UG') {
        document.getElementById('programmeStreamDisplay').textContent = context.stream;
        document.getElementById('streamField').classList.remove('d-none');
    }

    // Simulated DB (replace with backend on integration)
    let criteriaList = [];

	function renderTable() {
	    const tbody = document.getElementById('criteriaTableBody');
	    tbody.replaceChildren();
	
	    if (!criteriaList.length) {
	        const tr = document.createElement("tr");
	        const td = document.createElement("td");
	        td.colSpan = 5;
	        td.className = "text-center text-muted";
	        td.textContent = "No criteria set.";
	        tr.appendChild(td);
	        tbody.appendChild(tr);
	        return;
	    }
	
	    criteriaList.forEach((row, idx) => {
	        const tr = document.createElement("tr");
	
	        const td1 = document.createElement("td");
	        td1.textContent = row.prevQualification;
	
	        const td2 = document.createElement("td");
	        td2.textContent = row.preferred;
	
	        const td3 = document.createElement("td");
	        td3.textContent = row.min;
	
	        const td4 = document.createElement("td");
	        td4.textContent = row.scoreType || '-';
	
	        const td5 = document.createElement("td");
	
	        const editBtn = document.createElement("button");
	        editBtn.type = "button";
	        editBtn.className = "btn btn-sm btn-primary me-2";
	        editBtn.dataset.action = "edit";
	        editBtn.dataset.idx = idx;
	        editBtn.textContent = "Edit";
	
	        const deleteBtn = document.createElement("button");
	        deleteBtn.type = "button";
	        deleteBtn.className = "btn btn-sm btn-danger";
	        deleteBtn.dataset.action = "delete";
	        deleteBtn.dataset.idx = idx;
	        deleteBtn.textContent = "Delete";
	
	        td5.append(editBtn, deleteBtn);
	        tr.append(td1, td2, td3, td4, td5);
	
	        tbody.appendChild(tr);
	    });
	}

    function getUGModalFields(stream, existing){
        // --- UG STREAM LOGIC ---
        // "Science": fixed prev, fixed preferred (readonly)
        // "Commerce": fixed prev, preferred=radio (Science/Commerce/Both)
        // "Arts": fixed prev, preferred=radio (Arts/Science/Commerce/Any Two/All)
        // "Engineering": prev=dropdown (Class XII or Diploma), preferred=checkbox (Science/Diploma), JEE opt
        let prevQual, preferredUI, scoreOpts="";
        let minVal = existing?.min||"";
        let scoreTypeOpt = existing?.scoreType||"";
        const checked = v=>existing&&existing.preferred===v?'checked':'';
        if(stream==="SCIENCE"){
            prevQual =`<input type="text" class="form-control-plaintext" name="prevQualification" readonly value="Class XII">`;
            preferredUI = `<input type="text" class="form-control-plaintext" name="preferred" readonly value="Science">`;
            scoreOpts = `<div class="form-check mt-2"><input class="form-check-input" type="checkbox" name="scoreType" value="CUET" id="cuet" ${scoreTypeOpt==='CUET'?"checked":""}><label class="form-check-label" for="cuet">Add CUET % requirement</label></div>`;
        }
        else if(stream==="COMMERCE"){
            prevQual =`<input type="text" class="form-control-plaintext" name="prevQualification" readonly value="Class XII">`;
            preferredUI = [ 'Science', 'Commerce', 'Both' ].map( v=>`
                <div class="form-check form-check-inline">
                  <input class="form-check-input" type="radio" name="preferred" value="${v}" id="ps-${v}" ${checked(v)}>
                  <label class="form-check-label" for="ps-${v}">${v}</label>
                </div>
            `).join('');
            scoreOpts = `<div class="form-check mt-2"><input class="form-check-input" type="checkbox" name="scoreType" value="CUET" id="cuet" ${scoreTypeOpt==='CUET'?"checked":""}><label class="form-check-label" for="cuet">Add CUET % requirement</label></div>`;
        }
        else if(stream==="ARTS"){
            prevQual =`<input type="text" class="form-control-plaintext" name="prevQualification" readonly value="Class XII">`;
            preferredUI = [ 'Arts', 'Science', 'Commerce', 'Any Two', 'All' ].map( v=>`
                <div class="form-check form-check-inline">
                  <input class="form-check-input" type="radio" name="preferred" value="${v}" id="ps-${v}" ${checked(v)}>
                  <label class="form-check-label" for="ps-${v}">${v}</label>
                </div>
            `).join('');
            scoreOpts = `<div class="form-check mt-2"><input class="form-check-input" type="checkbox" name="scoreType" value="CUET" id="cuet" ${scoreTypeOpt==='CUET'?"checked":""}><label class="form-check-label" for="cuet">Add CUET % requirement</label></div>`;
        }
        else if(stream==="ENGINEERING"){
            prevQual = `<select name="prevQualification" class="form-select">${['Class XII','Diploma'].map(q=>
                `<option ${existing?.prevQualification===q?'selected':''}>${q}</option>`).join('')}</select>`;
            preferredUI = [
                `<div class="form-check form-check-inline">
                  <input class="form-check-input" type="checkbox" name="preferred" value="Science" id="ps-Science" ${existing?.preferred?.includes('Science')?'checked':''}>
                  <label class="form-check-label" for="ps-Science">Science</label>
                </div>`,
                `<div class="form-check form-check-inline">
                  <input class="form-check-input" type="checkbox" name="preferred" value="Diploma" id="ps-Diploma" ${existing?.preferred?.includes('Diploma')?'checked':''}>
                  <label class="form-check-label" for="ps-Diploma">Diploma</label>
                </div>`
            ].join('');
            scoreOpts = `<div class="form-check mt-2"><input class="form-check-input" type="checkbox" name="scoreType" value="JEE" id="jee" ${scoreTypeOpt==='JEE'?'checked':''}><label class="form-check-label" for="jee">Add JEE % requirement</label></div>`;
        }
        return `
            <div class="mb-3">
              <label class="form-label">Previous Qualification</label>
              ${prevQual}
            </div>
            <div class="mb-3">
              <label class="form-label">Preferred Stream(s)</label>
              ${preferredUI}
            </div>
            <div class="mb-3">
              <label class="form-label">Minimum % Required</label>
              <input class="form-control" name="min" type="number" min="0" max="100" required value="${minVal}">
            </div>
            ${scoreOpts}
        `;
    }

    function getPGModalFields(existing){
        // --- PG LOGIC ----
        let prevQuals = [context.programmeName.replace(/^(M|B)\.[A-Z]*\s/, 'B.')];    // E.g. for M.Sc Chemistry -> B.Sc Chemistry
        let minVal = existing?.min||"";
        let prevList = [ `<div class="form-check"><input class="form-check-input" name="prevQualification" id="pq-main" type="radio" value="${prevQuals[0]}" ${existing?.prevQualification===prevQuals[0]?'checked':'checked'}><label class="form-check-label" for="pq-main">${prevQuals[0]}</label></div>` ];
        // Plus "Add another" row possibility (simulate)
        prevList.push(...["Other Programme 1","Other Programme 2"].map((p,i)=>
            `<div class="form-check"><input class="form-check-input" name="prevQualification" id="pq${i+2}" type="radio" value="${p}" ${existing?.prevQualification===p?'checked':''}><label class="form-check-label" for="pq${i+2}">${p}</label></div>`
        ));
        let scoreOpts = `<div class="form-check mt-2"><input class="form-check-input" type="checkbox" name="scoreType" value="CUET" id="cuet" ${existing?.scoreType==='CUET'?"checked":""}><label class="form-check-label" for="cuet">Add CUET % requirement</label></div>
                         <div class="form-check mt-0"><input class="form-check-input" type="checkbox" name="scoreType" value="GATE" id="gate" ${existing?.scoreType==='GATE'?"checked":""}><label class="form-check-label" for="gate">Add GATE score requirement</label></div>`;
        return `
            <div class="mb-3">
              <label class="form-label">Previous Qualification</label>
              ${prevList.join('')}
            </div>
            <div class="mb-3">
              <label class="form-label">Preferred Programme</label>
              <input type="text" class="form-control" name="preferred" required value="${existing?.preferred||""}">
            </div>
            <div class="mb-3">
              <label class="form-label">Minimum % Required</label>
              <input class="form-control" name="min" type="number" min="0" max="100" required value="${minVal}">
            </div>
            ${scoreOpts}
        `;
    }

    // --- MODAL OPEN/EDIT/ADD ----
    const modal = new bootstrap.Modal(document.getElementById('criterionModal'));
    let editIdx = -1;
    document.getElementById('addCriterionBtn').onclick = function(){
        editIdx = -1;
        showModal();
    };
    document.getElementById('criteriaTableBody').onclick = function(e){
        let btn = e.target.closest('button[data-action]');
        if(!btn) return;
        let idx = Number(btn.getAttribute('data-idx'));
        if(btn.dataset.action === 'edit'){
            editIdx = idx;
            showModal(criteriaList[idx]);
        }
        if(btn.dataset.action === 'delete'){
            criteriaList.splice(idx, 1);
            renderTable();
        }
    };

    function showModal(existing){
        let inner;
        if(context.programmeType==='UG'){
            inner=getUGModalFields(context.stream,existing);
        } else {
            inner=getPGModalFields(existing);
        }
        const container = document.getElementById('modalBodyContent');
		container.replaceChildren();
		
		const parser = new DOMParser();
		const doc = parser.parseFromString(inner, "text/html");
		
		doc.querySelectorAll("script, iframe, object, embed").forEach(el => el.remove());
		doc.querySelectorAll("*").forEach(el => {
		    Array.from(el.attributes).forEach(attr => {
		        if (attr.name.startsWith("on")) {
		            el.removeAttribute(attr.name);
		        }
		    });
		});
		
		// append
		Array.from(doc.body.childNodes).forEach(node => {
		    container.appendChild(node);
		});
        modal.show();
    }

    // ADD/EDIT SUBMIT
    document.getElementById('criterionForm').onsubmit = function(ev){
        ev.preventDefault();
        let fd = new FormData(ev.target);

        let row = {
            prevQualification: fd.get('prevQualification'),
            preferred: fd.getAll('preferred').join(', ') || fd.get('preferred'), // radios (UG)/text (PG)
            min: fd.get('min'),
            scoreType: fd.getAll('scoreType').join(', ')
        };
        if(editIdx>=0){
            criteriaList[editIdx] = row;
        }else{
            criteriaList.push(row);
        }
        modal.hide();
        renderTable();
    };

    renderTable();
});

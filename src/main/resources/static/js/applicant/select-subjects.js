import { showSuccess, showError, showLoading, hideLoading } from './utils.js';

function initializeSelectSubjects() {
    const form = document.getElementById('subjectPreferenceForm');
    if (!form) return;

    const programmeOfferedId = document.getElementById('programmeOfferedId').value;
    const seatAllotmentId = document.getElementById('seatAllotmentId').value;
    const shiftRadios = form.querySelectorAll('input[name="chosenShift"]');
    const subjectSelectionContainer = document.getElementById('subjectSelectionContainer');
    const subjectPoolsContainer = document.getElementById('subjectPoolsContainer');
    const confirmBtn = document.getElementById('confirmSubjectsBtn');

    let choicesInstances = {};
    if(confirmBtn) confirmBtn.disabled = true;

    let currentShiftSubjects = {};

    async function loadInitialState() {
        try {
            const response = await axios.get(`/api/applicants/counseling/get-preferences/${seatAllotmentId}`);
            const savedData = response.data;
            if (savedData && savedData.chosenShift) {
                const shiftRadio = form.querySelector(`input[name="chosenShift"][value="${savedData.chosenShift}"]`);
                if (shiftRadio) {
                    shiftRadio.checked = true;
                    await handleShiftSelection(savedData.chosenShift, savedData.preferences);
                    
                    // Change button text to indicate an update rather than a first-time save
                    confirmBtn.replaceChildren();
					const icon = document.createElement("i");
					icon.className = "bi bi-save2-fill me-2";
					
					confirmBtn.appendChild(icon);
					confirmBtn.appendChild(document.createTextNode("Update Preferences"));
                }
            }
        } catch (error) {
            console.warn('Could not load saved preferences. This might be a first-time setup.', error);
        }
    }

    shiftRadios.forEach(radio => {
        radio.addEventListener('change', (event) => {
            handleShiftSelection(event.target.value);
            
            // UI Polish: Smooth scroll down to the newly revealed subject section
            setTimeout(() => {
                subjectSelectionContainer.scrollIntoView({ behavior: 'smooth', block: 'start' });
            }, 100);
        });
    });
    
    form.addEventListener('submit', handlePreferenceSubmission);

    async function handleShiftSelection(selectedShift, savedPreferences = null) {
        subjectSelectionContainer.classList.remove('d-none');
        subjectPoolsContainer.replaceChildren();

		const wrapper = document.createElement("div");
		wrapper.className = "text-center py-5";
		
		const spinner = document.createElement("div");
		spinner.className = "spinner-border text-primary border-3";
		spinner.style.width = "3rem";
		spinner.style.height = "3rem";
		
		const text = document.createElement("p");
		text.className = "mt-3 fw-bold text-muted";
		text.textContent = "Fetching available subjects...";
		
		wrapper.append(spinner, text);
		subjectPoolsContainer.appendChild(wrapper);
        confirmBtn.disabled = true;
        
        try {
            const response = await axios.get(`/api/combinations/for-applicant/${programmeOfferedId}?shift=${selectedShift}`);
            currentShiftSubjects = response.data; 
            populateSubjectDropdowns(currentShiftSubjects, savedPreferences);
        } catch (error) {
            console.error('Failed to load subject pools:', error);
            subjectPoolsContainer.replaceChildren();

			const alert = document.createElement("div");
			alert.className = "alert alert-danger shadow-sm";
			
			const icon = document.createElement("i");
			icon.className = "bi bi-exclamation-triangle-fill me-2";
			
			alert.append(icon, document.createTextNode(" Could not load subjects from the server. Please try refreshing the page."));
			subjectPoolsContainer.appendChild(alert);
        }
    }

    function populateSubjectDropdowns(pools, savedPreferences = null) {
        subjectPoolsContainer.replaceChildren();
        choicesInstances = {};
        
        const displayOrder = ['MINOR', 'MULTIDISCIPLINARY', 'SKILL_ENHANCEMENT', 'ABILITY_ENHANCEMENT', 'VALUE_ADDED'];
        const subjectTypeMap = {
            MINOR: 'Minor Subjects',
            MULTIDISCIPLINARY: 'Multidisciplinary (MDC) Subjects',
            ABILITY_ENHANCEMENT: 'Ability Enhancement (AEC) Subjects',
            SKILL_ENHANCEMENT: 'Skill Enhancement (SEC) Subjects',
            VALUE_ADDED: 'Value Added (VAC) Subjects'
        };

        let hasSubjects = false;
        
        displayOrder.forEach(type => {
            const subjects = pools[type];
            if (subjects && subjects.length > 0) {
                hasSubjects = true;
                const friendlyName = subjectTypeMap[type] || type;
                
                const group = document.createElement('div');
                group.className = 'mb-4 border rounded p-3 bg-white shadow-sm';
                
                const heading = document.createElement("h5");
				heading.className = "h6 fw-bold text-dark border-bottom pb-2 mb-3";
				heading.textContent = friendlyName;
				
				group.appendChild(heading);
				
                if (type === 'MULTIDISCIPLINARY') {
                    const comment = document.createElement('div');
                    comment.className = 'alert alert-info small py-2 d-flex align-items-start mb-3'; 
                    const icon = document.createElement("i");
					icon.className = "bi bi-info-circle-fill me-2 mt-1";
					
					const textWrap = document.createElement("div");
					
					const strong = document.createElement("strong");
					strong.textContent = "Note: ";
					
					const text = document.createTextNode(
					  "To be chosen from disciplines other than Major. Students are not allowed to choose an MD course already undergone at the higher secondary level (12th class). The MD course opted shall not be repeated in any semester."
					);
					
					textWrap.append(strong, text);
					comment.append(icon, textWrap);
                    group.appendChild(comment);
                }
                
                const select = document.createElement('select');
                select.id = `select-${type}`;
                select.multiple = true;
                select.className = "form-select";
                group.appendChild(select);
                
                subjectPoolsContainer.appendChild(group);

                const maxItems = Math.min(3, subjects.length);
                const choices = new Choices(select, {
                    removeItemButton: true,
                    maxItemCount: maxItems,
                    placeholder: true,
                    placeholderValue: `Click to select up to ${maxItems} preferences`,
                    classNames: { containerOuter: 'choices border-0' } // Remove double borders
                });

                const options = subjects.map(s => ({
                    value: s.subjectName,
                    label: s.subjectName
                }));
                choices.setChoices(options, 'value', 'label', false);

                if (savedPreferences && savedPreferences[type]) {
                    const idToNameMap = new Map(subjects.map(s => [s.subjectId, s.subjectName]));
                    const savedNames = savedPreferences[type].map(id => idToNameMap.get(id)).filter(name => name);
                    savedNames.forEach(name => {
                        choices.setChoiceByValue(name);
                    });
                }

                choicesInstances[type] = choices;
            }
        });

        if (!hasSubjects) {
            subjectPoolsContainer.replaceChildren();
			const alert = document.createElement("div");
			alert.className = "alert alert-warning shadow-sm";
			
			const icon = document.createElement("i");
			icon.className = "bi bi-exclamation-circle-fill me-2";
			
			alert.append(icon, document.createTextNode(" No subjects have been made available for this shift by the institute."));
			subjectPoolsContainer.appendChild(alert);
            confirmBtn.disabled = true;
        } else {
            confirmBtn.disabled = false;
        }
    }

    async function handlePreferenceSubmission(event) {
        event.preventDefault();
        const originalContent = confirmBtn.cloneNode(true);
		confirmBtn.disabled = true;
		confirmBtn.replaceChildren();
		
		const spinner = document.createElement("span");
		spinner.className = "spinner-border spinner-border-sm me-2";
		
		confirmBtn.appendChild(spinner);
		confirmBtn.appendChild(document.createTextNode(" Processing..."));
        const chosenShift = form.querySelector('input[name="chosenShift"]:checked')?.value;
        
        if (!chosenShift) {
            showError('Please select a shift before submitting.');
            confirmBtn.disabled = false;
            confirmBtn.replaceChildren(...originalContent.childNodes);
            return;
        }

        const preferencesPayload = {};
        const masterNameToIdMap = new Map();
        Object.values(currentShiftSubjects).flat().forEach(s => {
            masterNameToIdMap.set(s.subjectName, s.subjectId);
        });

        for (const [type, choices] of Object.entries(choicesInstances)) {
            const selectedNames = choices.getValue(true);
            if (selectedNames && selectedNames.length > 0) {
                preferencesPayload[type] = selectedNames.map(name => masterNameToIdMap.get(name)).filter(id => id);
            }
        }

        const finalPayload = {
            seatAllotmentId: parseInt(seatAllotmentId),
            chosenShift: chosenShift,
            preferences: preferencesPayload
        };
        
        try {
            await axios.post('/api/applicants/counseling/save-combination-preferences', finalPayload);
            await showSuccess('Your subject preferences have been saved successfully!');
            setTimeout(() => {
                window.location.href = '/applicants/dashboard';
            }, 1500);
        } catch (error) {
            console.error('Failed to save preferences:', error);
            showError(error.response?.data?.message || 'An unknown error occurred while saving. Please try again.');
            confirmBtn.disabled = false;
            confirmBtn.replaceChildren(...originalContent.childNodes);
        }
    }

    loadInitialState();
}

export { initializeSelectSubjects };
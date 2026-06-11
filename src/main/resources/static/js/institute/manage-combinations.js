const CombinationsModule = (() => {

    // --- CSRF & API Helpers ---
    const getCsrfHeaders = () => {
        const token = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
        const header = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
        return (token && header) ? { [header]: token } : {};
    };

    const Api = {
        get: (url) => axios.get(url).then(r => r.data),
        post: (url, data) => axios.post(url, data, { headers: { 'Content-Type': 'application/json', ...getCsrfHeaders() } })
    };

    // --- State Management ---
    const state = {
        programmeOfferedId: null,
        shiftCode: 'NA',
        isLocked: false,
        allStreamSubjects: [],
        choicesInstances: {}
    };

    // Maps backend subject categories to our HTML select IDs
    const subjectTypeMap = {
    'minor-subjects': 'MINOR',   // ✅ FIX HERE
    'mdc-subjects': 'MDC',
    'aec-subjects': 'AEC',
    'sec-subjects': 'SEC',
    'vac-subjects': 'VAC'
};

    // --- Utility Functions ---
    function $id(id) { 
        return document.getElementById(id); 
    }

    function showToast(type, message) {
        const toastEl = $id(type === 'success' ? 'successToast' : 'errorToast');
        if (toastEl) {
            $id(type === 'success' ? 'successMessage' : 'errorMessage').textContent = message;
            new bootstrap.Toast(toastEl).show();
        }
    }

    // --- Initialization ---
    async function init() {
        // 1. Get Context from URL Parameters
        const urlParams = new URLSearchParams(window.location.search);
        
        // Fallback to dataset if URL params are missing
        const mainEl = document.querySelector('main');
        state.programmeOfferedId = urlParams.get('programmeOfferedId') || (mainEl ? mainEl.getAttribute('data-programme-offered-id') : null);
        state.shiftCode = urlParams.get('shiftCode') || 'NA';
        
        // Populate Header Text
        const pName = urlParams.get('programmeName');
        const dName = urlParams.get('departmentName');
        const sName = urlParams.get('shiftName');

        if (pName && $id('displayProgName')) $id('displayProgName').textContent = pName;
        if (dName && $id('displayDeptName')) $id('displayDeptName').textContent = dName;
        if (sName && $id('displayShiftName')) $id('displayShiftName').textContent = sName;

        // Bind Submit Event
        const form = $id('combinationsForm');
        if (form) form.addEventListener('submit', handleSave);

       // await checkGlobalLock();
        await loadData();
    }

    // --- Global Timeline Lock ---
    /*async function checkGlobalLock() {
        try {
            const windows = await Api.get('/manage-programmes-data/institute/windows');
            state.isLocked = !windows.some(w => w.scheduleStatus === 'OPEN');
        } catch (e) { 
            state.isLocked = true; 
        }

        if (state.isLocked) {
            const lockBanner = $id('scheduleLockBanner');
            if (lockBanner) lockBanner.classList.remove('d-none');
            
            const saveBtn = $id('saveBtn');
			if (saveBtn) {
			    saveBtn.disabled = true;
			    saveBtn.replaceChildren();
			    const icon = document.createElement("i");
			    icon.className = "fas fa-lock me-2";
			    const text = document.createTextNode("Locked");
			    saveBtn.append(icon, text);
			}
        }
    }*/

    // --- Load API Data ---
    async function loadData() {
        try {
            // Fetch ALL subjects for this stream AND the saved combinations
            const [subjectsResponse, savedSubjectsResponse] = await Promise.all([
                Api.get(`/subject-data/for-programme-stream/${state.programmeOfferedId}`),
                Api.get(`/api/combinations/by-programme-offered/${state.programmeOfferedId}`).catch(() => ({}))
            ]);

            state.allStreamSubjects = subjectsResponse || [];
            const allSavedCombinations = savedSubjectsResponse || {};
            
            // Extract ONLY the subjects saved for the currently selected shift
            const savedShiftSubjects = allSavedCombinations[state.shiftCode] || {};

            // Map backend subjects into the structure Choices.js expects (using ID as value)
            
			// helper
			const mapToOption = (subject) => ({
			    value: subject.subjectId,
			    label: `${subject.subjectName} (${subject.subjectCode || 'N/A'})`
			});

			// filtered options
			const minorOptions = state.allStreamSubjects.filter(s => s.subjectType === 'MINOR').map(mapToOption);
			const mdcOptions   = state.allStreamSubjects.filter(s => s.subjectType === 'MDC').map(mapToOption);
			const aecOptions   = state.allStreamSubjects.filter(s => s.subjectType === 'AEC').map(mapToOption);
			const secOptions   = state.allStreamSubjects.filter(s => s.subjectType === 'SEC').map(mapToOption);
			const vacOptions   = state.allStreamSubjects.filter(s => s.subjectType === 'VAC').map(mapToOption);
			
			// use filtered
			initChoice('minor-subjects', minorOptions, getSavedIds(savedShiftSubjects, subjectTypeMap['minor-subjects']));
			initChoice('mdc-subjects', mdcOptions, getSavedIds(savedShiftSubjects, subjectTypeMap['mdc-subjects']));
			initChoice('aec-subjects', aecOptions, getSavedIds(savedShiftSubjects, subjectTypeMap['aec-subjects']));
			initChoice('sec-subjects', secOptions, getSavedIds(savedShiftSubjects, subjectTypeMap['sec-subjects']));
			initChoice('vac-subjects', vacOptions, getSavedIds(savedShiftSubjects, subjectTypeMap['vac-subjects']));
			          
            // Reveal UI
            const loadingState = $id('loadingState');
            const mainContent = $id('mainContent');
            if (loadingState) loadingState.classList.add('d-none');
            if (mainContent) mainContent.classList.remove('d-none');

        } catch (error) {
            console.error("Initialization failed:", error);
            const loadingState = $id('loadingState');
            if (loadingState) {
                loadingState.innerHTML = '<div class="alert alert-danger shadow-sm"><i class="fas fa-exclamation-triangle me-2"></i>Failed to load required data. Please refresh the page.</div>';
            }
        }
    }

    // Helper: Extracts an array of IDs from the backend's saved response
    function getSavedIds(savedShiftSubjects, categoryKey) {
        const categoryArray = savedShiftSubjects[categoryKey] || [];
        return categoryArray.map(subject => subject.subjectId);
    }

    // --- Initialize Choices.js ---
    function initChoice(elementId, options, selectedIds = []) {
        const el = $id(elementId);
        if (!el) return;
        
        // Mark items as selected if their ID exists in the saved database array
        const mappedOptions = options.map(opt => ({
            ...opt,
            selected: selectedIds.includes(opt.value)
        }));

        state.choicesInstances[elementId] = new Choices(el, {
            removeItemButton: !state.isLocked, // Hide remove 'x' if timeline is locked
            searchResultLimit: 5,
            renderChoiceLimit: 20,
            shouldSort: false,
            placeholderValue: 'Search and select subjects...',
            itemSelectText: 'Press to select'
        });

        state.choicesInstances[elementId].setChoices(mappedOptions, 'value', 'label', true);

        // Completely disable interaction if timeline is locked
        if (state.isLocked) {
            state.choicesInstances[elementId].disable();
        }
    }

    // --- Form Submission ---
 	async function handleSave(e) {
    e.preventDefault();
    if (state.isLocked) return;

    const btn = $id('saveBtn');

    // ✅ Store original safely (no innerHTML)
    if (!btn._originalContent) {
       btn._originalContent = Array.from(btn.childNodes).map(n => n.cloneNode(true));
    }

    btn.disabled = true;
    btn.replaceChildren();

    // ✅ Spinner
    const spinner = document.createElement("span");
    spinner.className = "spinner-border spinner-border-sm me-2";

    // ✅ Text
    const text = document.createTextNode("Saving...");

    btn.append(spinner, text);

    const getIds = (elementId) => {
        const instance = state.choicesInstances[elementId];
        if (!instance) return [];
        return instance.getValue(true).map(Number);
    };

    const payload = {
        programmeOfferedId: parseInt(state.programmeOfferedId),
        shift: state.shiftCode,
        minorSubjectIds: getIds('minor-subjects'),
        mdcSubjectIds: getIds('mdc-subjects'),
        aecSubjectIds: getIds('aec-subjects'),
        secSubjectIds: getIds('sec-subjects'),
        vacSubjectIds: getIds('vac-subjects')
    };

    try {
        await Api.post('/api/combinations', payload);

        showToast('success', `Successfully saved subjects for ${state.shiftCode === 'NA' ? 'this' : state.shiftCode} shift!`);

        setTimeout(() => {
            btn.disabled = false;
            btn.replaceChildren(...btn._originalContent);
        }, 1000);

    } catch (error) {
        console.error('Failed to save combinations', error);

        showToast('error', error.response?.data?.message || 'Error saving subjects. Please try again.');

        btn.disabled = false;
        btn.replaceChildren(...btn._originalContent);
    }
}

    return { init };
})();

document.addEventListener('DOMContentLoaded', CombinationsModule.init);
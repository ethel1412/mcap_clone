import { handleAjaxFormSubmit } from './applicant.js';

function initializeAcademicDetailsForm() {
    const form = document.getElementById('academic-details-form');
    if (!form) return;

    const dataHolder = document.getElementById('academic-data-holder');
    const allSubjects = (dataHolder?.dataset.allSubjects) ? JSON.parse(dataHolder.dataset.allSubjects) : [];

    const fragmentRoot = document.getElementById('academic-details-fragment');
    if (!fragmentRoot) return;
    const cuetPapers = JSON.parse(fragmentRoot?.dataset.cuetPapers || '[]');
    const MAX_CUET_SUBJECTS = 6;
    const addCuetSubjectBtn = document.getElementById('add-cuet-subject-btn');
    const cuetSubjectsContainer = document.getElementById('cuet-subjects-container');
    const latestQualSelect = form.querySelector('.latest-qualification-row .qualification-level-select');
    const subjectMarksContent = document.getElementById('subject-marks-content');
    const addSubjectBtn = document.getElementById('add-subject-btn');

    const createCuetSubjectDropdown = (selectElement) => {
        if (!selectElement || typeof Choices === 'undefined' || selectElement.choices) return null;

        selectElement.innerHTML = '<option value="">-- Select CUET Paper --</option>';
        cuetPapers.forEach(paper => {
            const option = new Option(paper.paperName, paper.paperCode);
            selectElement.add(option);
        });

        const choicesInstance = new Choices(selectElement, {
            itemSelectText: '',
            searchPlaceholderValue: 'Search CUET papers...',
            shouldSort: false,
            placeholder: true
        });

        selectElement.addEventListener('change', (e) => {
            const selectedCode = e.detail ? e.detail.value : selectElement.value;
            const paper = cuetPapers.find(p => p.paperCode === selectedCode);
            const row = selectElement.closest('.cuet-subject-row');
            const hiddenNameInput = row.querySelector('.cuet-subject-name-hidden');

            if (paper && hiddenNameInput) {
                hiddenNameInput.value = paper.paperName;
            }
        });

        return choicesInstance;
    };

    const createComboBox = (selectElement) => {
        if (!selectElement || typeof Choices === 'undefined' || selectElement.choices) return null;

        return new Choices(selectElement, {
            removeItemButton: false,
            searchEnabled: true,
            searchPlaceholderValue: 'Search subjects...',
            itemSelectText: '',
        });
    };

    const updateCuetAddButtonState = () => {
        if (!addCuetSubjectBtn || !cuetSubjectsContainer) return;
        const currentCount = cuetSubjectsContainer.querySelectorAll('.cuet-subject-row').length;
        addCuetSubjectBtn.disabled = currentCount >= MAX_CUET_SUBJECTS;
    };

    const updatePercentage = (row) => {
        const marksObtainedInput = row.querySelector('.marks-obtained-input');
        const totalMarksInput = row.querySelector('.total-marks-input');
        const percentageDisplay = row.querySelector('.percentage-display');
        if (!marksObtainedInput || !totalMarksInput || !percentageDisplay) return;

        const obtained = parseFloat(marksObtainedInput.value);
        const total = parseFloat(totalMarksInput.value);

        marksObtainedInput.classList.remove('is-invalid');
        totalMarksInput.classList.remove('is-invalid');

        if (!isNaN(obtained) && !isNaN(total) && total > 0) {
            if (obtained > total) {
                marksObtainedInput.classList.add('is-invalid');
                percentageDisplay.value = 'Error';
            } else {
                percentageDisplay.value = ((obtained / total) * 100).toFixed(2) + '%';
            }
        } else {
            percentageDisplay.value = '';
        }
    };

    const handleStreamFieldVisibility = (qualificationSelect) => {
        const row = qualificationSelect.closest('.record-row');
        if (!row) return;

        const streamSelect = row.querySelector('.stream-major-select');
        if (!streamSelect) return;

        const selectedValue = qualificationSelect.value.toLowerCase();
        const streamIsEnabled = ['class xi', 'class xii', 'diploma', 'bachelor', 'master'].some(term => selectedValue.includes(term));

        streamSelect.disabled = !streamIsEnabled;

        if (!streamIsEnabled) {
            streamSelect.value = '';
        }
        const streamInput = row.querySelector('.stream-major-input');
        if (streamInput) streamInput.classList.add('d-none');
    };

    const enforceInputConstraints = (event) => {
        // Updated to include .cuet-percentile-input
        if (!event.target.matches('input[type="number"], .marks-obtained-input, .total-marks-input, .overall-percentage-input, .cuet-percentile-input')) return;

        const input = event.target;
        const max = parseFloat(input.getAttribute('max'));
        const min = parseFloat(input.getAttribute('min'));
        let value = parseFloat(input.value);

        if (isNaN(value)) return;

        // Custom percentile bounds handling
        if (input.classList.contains('cuet-percentile-input')) {
            if (value > 100) input.value = 100;
            if (value < 0) input.value = 0;
            return;
        }

        if (!isNaN(max) && value > max) {
            input.value = max;
        }
        if (!isNaN(min) && value < min) {
            input.value = min;
        }
    };

    const toggleSubjectMarksSection = () => {
        if (!latestQualSelect || !subjectMarksContent) return;
        const selectedQualification = latestQualSelect.value;
        const isEligible = selectedQualification && ['Class XII', 'Diploma', 'Bachelor', 'Master'].some(term => selectedQualification.includes(term));

        subjectMarksContent.classList.toggle('d-none', !isEligible);
        if (addSubjectBtn) addSubjectBtn.disabled = !isEligible;
    };

    const addRowFromTemplate = (containerId, templateId, rowSelector) => {
        const container = document.getElementById(containerId);
        const template = document.getElementById(templateId);

        if (!container || !template) {
            console.error(`Template ('${templateId}') or container ('${containerId}') not found!`);
            return null;
        }

        const newIndex = container.querySelectorAll(rowSelector).length;
        const newRow = template.cloneNode(true);
        newRow.removeAttribute('id');

        newRow.querySelectorAll('input, select, textarea').forEach(input => {
            if (input.name) {
                input.name = input.name.replace(/\[__INDEX__\]/g, `[${newIndex}]`);
            }
            if (input.id) {
                const newId = input.id.replace('__INDEX__', newIndex);
                const oldId = input.id;
                input.id = newId;
                const label = newRow.querySelector(`label[for="${oldId}"]`);
                if (label) label.setAttribute('for', newId);
            }

            // Ensure numeric inputs/percentiles are cleared for the new row
            if (input.type === 'number' || input.classList.contains('cuet-percentile-input')) {
                input.value = '';
            }

            input.disabled = false;
        });
        container.appendChild(newRow);
        return newRow;
    };

    const setupEligibilityToggle = () => {
        const eligibilityDetails = document.getElementById('eligibilityDetails');
        if (!eligibilityDetails) return;

        const toggleButton = eligibilityDetails.closest('.alert').querySelector('button[data-bs-toggle="collapse"]');
        if (!toggleButton) return;

        eligibilityDetails.addEventListener('show.bs.collapse', () => {
            toggleButton.querySelector('.show-text').classList.add('d-none');
            toggleButton.querySelector('.hide-text').classList.remove('d-none');
        });

        eligibilityDetails.addEventListener('hide.bs.collapse', () => {
            toggleButton.querySelector('.show-text').classList.remove('d-none');
            toggleButton.querySelector('.hide-text').classList.add('d-none');
        });
    };

    form.addEventListener('submit', handleAjaxFormSubmit);

    if (latestQualSelect) {
        latestQualSelect.addEventListener('change', toggleSubjectMarksSection);
    }

    form.addEventListener('click', (e) => {
        const button = e.target.closest('button');
        if (!button) return;

        if (button.id === 'add-cuet-subject-btn') {
            const currentCount = cuetSubjectsContainer.querySelectorAll('.cuet-subject-row').length;
            if (currentCount < MAX_CUET_SUBJECTS) {
                const newRow = addRowFromTemplate('cuet-subjects-container', 'cuet-subject-template', '.cuet-subject-row');
                if (newRow) {
                    const select = newRow.querySelector('.cuet-paper-select');
                    createCuetSubjectDropdown(select);
                    updateCuetAddButtonState();
                }
            }
        }

        if (button.id === 'add-subject-btn') {
            const newRow = addRowFromTemplate('subject-marks-container', 'subject-row-template', '.subject-mark-row');
            if (newRow) createComboBox(newRow.querySelector('.subject-select'));
        }

        if (button.id === 'add-record-btn') {
            const newRow = addRowFromTemplate('past-records-container', 'record-template', '.record-row');
            if(newRow) newRow.querySelector('.qualification-level-select').addEventListener('change', (e) => handleStreamFieldVisibility(e.target));
        }

        if (button.classList.contains('remove-subject-btn')) button.closest('.subject-mark-row').remove();
        if (button.classList.contains('remove-record-btn')) button.closest('.record-row').remove();
        if (button.classList.contains('remove-cuet-subject-btn')) {
            button.closest('.cuet-subject-row').remove();
            updateCuetAddButtonState();
        }
    });

    form.addEventListener('input', (e) => {
        enforceInputConstraints(e);
        if (e.target.matches('.marks-obtained-input, .total-marks-input')) {
            updatePercentage(e.target.closest('.subject-mark-row'));
        }
    });

    form.querySelectorAll('.qualification-level-select').forEach(select => {
        select.addEventListener('change', (e) => handleStreamFieldVisibility(e.target));
    });

    ['provideJeeScores', 'provideCuetScores', 'provideNetScores', 'provideGateScores'].forEach(id => {
        const checkbox = document.getElementById(id);
        const sectionId = id.replace('provide', '').replace('Scores', '').toLowerCase() + '-score-section';
        const section = document.getElementById(sectionId);

        if (checkbox && section) {
            checkbox.addEventListener('change', () => {
                section.classList.toggle('d-none', !checkbox.checked);
            });
        }
    });

    document.querySelectorAll('#cuet-subjects-container .cuet-paper-select').forEach(select => {
        const savedCode = select.value;
        const choicesInstance = createCuetSubjectDropdown(select);
        if (savedCode && choicesInstance) {
            choicesInstance.setChoiceByValue(savedCode);
        }
    });

    toggleSubjectMarksSection();
    setupEligibilityToggle();
    updateCuetAddButtonState();

    document.querySelectorAll('#subject-marks-container .subject-select').forEach(select => {
        createComboBox(select);
    });

    document.querySelectorAll('#past-records-container .qualification-level-select').forEach(handleStreamFieldVisibility);

    document.querySelectorAll('#subject-marks-container .subject-mark-row').forEach(row => {
        updatePercentage(row);
    });
}

export { initializeAcademicDetailsForm };
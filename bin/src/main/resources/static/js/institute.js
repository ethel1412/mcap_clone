document.addEventListener('DOMContentLoaded', function () {

    // 1. Initialize Bootstrap Tooltips
    const tooltipTriggerList = document.querySelectorAll('[data-bs-toggle="tooltip"]');
    tooltipTriggerList.forEach(function (tooltipTriggerEl) {
        new bootstrap.Tooltip(tooltipTriggerEl);
    });
    
    // 2. Logic for State -> District dropdowns
    const stateDropdown = document.getElementById('stateDropdown');
    const districtDropdown = document.getElementById('districtDropdown');
    const blockDropdown = document.getElementById('blockDropdown');

    if (stateDropdown) {
        stateDropdown.addEventListener('change', function () {
            const stateCode = this.value;
            districtDropdown.innerHTML = '<option value="">Loading...</option>';
            blockDropdown.innerHTML = '<option value="">Select Block</option>'; 
            
            if (!stateCode) {
                districtDropdown.innerHTML = '<option value="">Select District</option>';
                return;
            }

            axios.get(`/master/get-list-districts/${stateCode}`)
                .then(response => {
                    const districts = response.data;
                    let optionsHTML = '<option value="">Select District</option>';
                    districts.forEach(d => {
                        optionsHTML += `<option value="${d.districtCode}">${d.districtName}</option>`;
                    });
                    districtDropdown.innerHTML = optionsHTML;
                })
                .catch(error => {
                    console.error('Error fetching districts:', error);
                    districtDropdown.innerHTML = '<option value="">Error loading districts</option>';
                });
        });
    }

    // 3. Logic for District -> Block dropdowns
    if (districtDropdown) {
        districtDropdown.addEventListener('change', function () {
            const districtCode = this.value;
            blockDropdown.innerHTML = '<option value="">Loading...</option>';
            
            if (!districtCode) {
                blockDropdown.innerHTML = '<option value="">Select Block</option>';
                return;
            }
            
            axios.get(`/master/get-list-blocks/${districtCode}`)
                .then(response => {
                    const blocks = response.data;
                    let optionsHTML = '<option value="">Select Block</option>';
                    blocks.forEach(b => {
                        optionsHTML += `<option value="${b.blockCode}">${b.blockName}</option>`;
                    });
                    blockDropdown.innerHTML = optionsHTML;
                })
                .catch(error => {
                    console.error('Error fetching blocks:', error);
                    blockDropdown.innerHTML = '<option value="">Error loading blocks</option>';
                });
        });
    }

    // 4. UX Improvement
    const instituteForm = document.getElementById('instituteForm');
    if (instituteForm) {
        const fieldsToMonitor = instituteForm.querySelectorAll('input, select, textarea');
        fieldsToMonitor.forEach(field => {
            const eventType = (field.tagName.toLowerCase() === 'select') ? 'change' : 'input';
            
            field.addEventListener(eventType, function () {
                // Check if there is an error to clear
                if (this.classList.contains('is-invalid')) {
                    // Remove the red border
                    this.classList.remove('is-invalid');
    
                    
                    const errorDiv = this.parentElement.querySelector('.js-error-message');
                    
                    if (errorDiv) {
                        errorDiv.textContent = '';
                    }
                }
            });
        });
    }
});
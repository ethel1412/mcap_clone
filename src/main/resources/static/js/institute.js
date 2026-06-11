document.addEventListener('DOMContentLoaded', function () {

    const csrfToken = document.querySelector('meta[name="_csrf"]').content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

    axios.defaults.headers.common[csrfHeader] = csrfToken;

    // 1. Initialize Bootstrap Tooltips
    const tooltipTriggerList = document.querySelectorAll('[data-bs-toggle="tooltip"]');
    tooltipTriggerList.forEach(function (el) {
        new bootstrap.Tooltip(el);
    });

    // ===== Helper: set single option =====
    function setSingleOption(select, text) {
        select.replaceChildren();
        const opt = document.createElement("option");
        opt.value = "";
        opt.textContent = text;
        select.appendChild(opt);
    }

    // ===== Helper: populate options =====
    function populateOptions(select, list, valueKey, labelKey, defaultText) {
        select.replaceChildren();

        const defaultOpt = document.createElement("option");
        defaultOpt.value = "";
        defaultOpt.textContent = defaultText;
        select.appendChild(defaultOpt);

        list.forEach(item => {
            const opt = document.createElement("option");
            opt.value = item[valueKey];
            opt.textContent = item[labelKey];
            select.appendChild(opt);
        });
    }

    // 2. State -> District
    const stateDropdown = document.getElementById('stateDropdown');
    const districtDropdown = document.getElementById('districtDropdown');
    const blockDropdown = document.getElementById('blockDropdown');

    if (stateDropdown) {
        stateDropdown.addEventListener('change', function () {
            const stateCode = this.value;

            setSingleOption(districtDropdown, "Loading...");
            setSingleOption(blockDropdown, "Select Block");

            if (!stateCode) {
                setSingleOption(districtDropdown, "Select District");
                return;
            }

            axios.get(`/master/get-list-districts/${stateCode}`)
                .then(response => {
                    const districts = response.data;
                    populateOptions(
                        districtDropdown,
                        districts,
                        "districtCode",
                        "districtName",
                        "Select District"
                    );
                })
                .catch(error => {
                    console.error('Error fetching districts:', error);
                    setSingleOption(districtDropdown, "Error loading districts");
                });
        });
    }

    // 3. District -> Block
    if (districtDropdown) {
        districtDropdown.addEventListener('change', function () {
            const districtCode = this.value;

            setSingleOption(blockDropdown, "Loading...");

            if (!districtCode) {
                setSingleOption(blockDropdown, "Select Block");
                return;
            }

            axios.get(`/master/get-list-blocks/${districtCode}`)
                .then(response => {
                    const blocks = response.data;
                    populateOptions(
                        blockDropdown,
                        blocks,
                        "blockCode",
                        "blockName",
                        "Select Block"
                    );
                })
                .catch(error => {
                    console.error('Error fetching blocks:', error);
                    setSingleOption(blockDropdown, "Error loading blocks");
                });
        });
    }

    // 4. UX Improvement (unchanged)
    const instituteForm = document.getElementById('instituteForm');
    if (instituteForm) {
        const fieldsToMonitor = instituteForm.querySelectorAll('input, select, textarea');

        fieldsToMonitor.forEach(field => {
            const eventType = (field.tagName.toLowerCase() === 'select') ? 'change' : 'input';

            field.addEventListener(eventType, function () {
                if (this.classList.contains('is-invalid')) {
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
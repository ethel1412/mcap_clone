import { showSuccess, showError, showWarning, showLoading, hideLoading } from './utils.js';
import { updateSidebarState } from './applicant.js';
import { handleAjaxFormSubmit } from './applicant.js';

function initializePersonalDetailsForm() {
    const form = document.getElementById('personal-details-form');
    if (!form) return;

    // --- DOM Element Cache ---
    const sameAsPermanentCheckbox = document.getElementById('sameAsPermanent');
    const commAddressFields = {
        addressLine1: document.getElementById('commAddress1'),
        addressLine2: document.getElementById('commAddress2'),
        stateCode:    document.getElementById('commState'),
        districtCode: document.getElementById('commDistrict'),
        townVillage:  document.getElementById('commTownVillage'),
        pincode:      document.getElementById('commPincode')
    };
    const permAddressFields = {
        addressLine1: document.getElementById('permAddress1'),
        addressLine2: document.getElementById('permAddress2'),
        stateCode:    document.getElementById('permState'),
        districtCode: document.getElementById('permDistrict'),
        townVillage:  document.getElementById('permTownVillage'),
        pincode:      document.getElementById('permPincode')
    };
    const fatherNameInput = document.getElementById('fatherName');
    const motherNameInput = document.getElementById('motherName');
    const guardianNameInput = document.getElementById('guardianName');

    // Function to dynamically update a dropdown (e.g., districts based on state)
    	const updateDropdown = async (url, targetDropdown) => {
	    if (!targetDropdown) return;

	    // show loading
	    targetDropdown.replaceChildren();
	    const loadingOpt = document.createElement("option");
	    loadingOpt.value = "";
	    loadingOpt.textContent = "Loading...";
	    targetDropdown.appendChild(loadingOpt);
	    targetDropdown.disabled = true;

	    try {
	        const resp = await axios.get(url);

	        targetDropdown.replaceChildren();

	        // default option
	        const defaultOpt = document.createElement("option");
	        defaultOpt.value = "";
	        defaultOpt.textContent = "-- Select --";
	        targetDropdown.appendChild(defaultOpt);

	        // IMPORTANT: resp.data is HTML string → parse safely
	        const parser = new DOMParser();
	        const doc = parser.parseFromString(`<select>${resp.data}</select>`, "text/html");

	        const options = doc.querySelectorAll("option");
	        options.forEach(opt => {
	            const newOpt = document.createElement("option");
	            newOpt.value = opt.value;
	            newOpt.textContent = opt.textContent;
	            targetDropdown.appendChild(newOpt);
	        });

	        targetDropdown.disabled = false;

	    } catch (err) {
	        console.error('Failed to fetch dropdown data:', err);

	        targetDropdown.replaceChildren();
	        const errorOpt = document.createElement("option");
	        errorOpt.value = "";
	        errorOpt.textContent = "-- Error --";
	        targetDropdown.appendChild(errorOpt);
	    }
	};

    // Logic for the "Same as Permanent Address" checkbox
    const handleSameAsPermanent = async () => {
        const checked = sameAsPermanentCheckbox.checked;
        if (!checked) {
            Object.values(commAddressFields).forEach(field => { if (field) field.value = ''; });
            commAddressFields.districtCode.innerHTML = '<option value="">-- Select State First --</option>';
            return;
        }

        Object.keys(permAddressFields).forEach(key => {
            if (commAddressFields[key] && permAddressFields[key]) {
                commAddressFields[key].value = permAddressFields[key].value;
            }
        });

        const stateToSet = permAddressFields.stateCode.value;
        if (stateToSet) {
            await updateDropdown(`/applicants/districts?stateCode=${stateToSet}`, commAddressFields.districtCode);
            commAddressFields.districtCode.value = permAddressFields.districtCode.value;
        }
    };

    // --- Event Listeners ---
    form.addEventListener('submit', handleAjaxFormSubmit);

    if (sameAsPermanentCheckbox) {
        sameAsPermanentCheckbox.addEventListener('change', handleSameAsPermanent);
    }

    form.querySelectorAll('.state-dropdown').forEach(stateSelect => {
        stateSelect.addEventListener('change', function () {
            const stateCode = this.value;
            const districtSelect = this.closest('.row, .table-responsive').querySelector('.district-dropdown');
            if (stateCode && districtSelect) {
                updateDropdown(`/applicants/districts?stateCode=${stateCode}`, districtSelect);
            } else if(districtSelect) {
                districtSelect.innerHTML = '<option value="">-- Select State First --</option>';
            }
        });
    });


    // --- Initial State Setup ---
    const preloadPromises = [];

    document.querySelectorAll('.state-dropdown').forEach((stateSelect) => {
        if(stateSelect.value) {
            const districtSelect = stateSelect.closest('.row, .table-responsive').querySelector('.district-dropdown');
            const savedDistrictCode = districtSelect.dataset.savedValue;
            if (districtSelect) {
                 // Add the promise to our array
                 const promise = updateDropdown(`/applicants/districts?stateCode=${stateSelect.value}`, districtSelect)
                     .then(() => {
                         if (savedDistrictCode) {
                             districtSelect.value = savedDistrictCode;
                         }
                     });
                 preloadPromises.push(promise);
            }
        }
    });

    Promise.all(preloadPromises).then(() => {
        try {
            const areSame =
                permAddressFields.addressLine1.value === commAddressFields.addressLine1.value &&
                permAddressFields.addressLine2.value === commAddressFields.addressLine2.value &&
                permAddressFields.pincode.value      === commAddressFields.pincode.value &&
                permAddressFields.stateCode.value    === commAddressFields.stateCode.value &&
                permAddressFields.districtCode.value === commAddressFields.districtCode.value &&
                permAddressFields.townVillage.value  === commAddressFields.townVillage.value;

            if (areSame && permAddressFields.addressLine1.value !== '') {
                 sameAsPermanentCheckbox.checked = true;
            }
        } catch (e) {
            console.warn("Could not check if addresses are the same.", e);
        }
    });

}

// Export the main initializer function to be called from applicant.js
export { initializePersonalDetailsForm };
// Global data that needs to be shared across modules 
let allProgrammesData = [];
let allSchedulesData = [];

document.addEventListener('DOMContentLoaded', function () {
    // --- 1. SETUP AXIOS WITH CSRF TOKEN ---
    const token = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
    const header = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
    if (token && header) {
        axios.defaults.headers.common[header] = token;
    } else {
        console.warn("CSRF token or header not found. Axios requests might fail.");
    }

    
    // Using Promise.all to fetch both programmes and schedules concurrently
    Promise.all([
        axios.get('/admin/api/programmes'),
    ])
    .then(responses => {
        allProgrammesData = responses[0].data;
        console.log("All programmes loaded:", allProgrammesData.length);

        // --- 2. START THE INITIALIZATION PROCESS AFTER DATA IS LOADED ---
        initializeDashboardComponents();
    })
    .catch(error => {
        console.error("Failed to load initial data (programmes/schedules):", error);
        // showToast is in utils.js
        if (typeof showToast !== 'undefined') {
            showToast("Failed to load essential data. Some features may not work.", "danger");
        } else {
            console.error("showToast function not found. Please ensure utils.js is loaded.");
        }
        // Still attempt to initialize other components even if data load fails
        initializeDashboardComponents();
    });
});


function initializeDashboardComponents() {
    // --- General UI Initializations ---
    const profileTrigger = document.getElementById('showProfileModal');
    if (profileTrigger) {
        profileTrigger.addEventListener('click', function () {
            var profileModal = new bootstrap.Modal(document.getElementById('profileModal'));
            profileModal.show();
        });
    }
    // Bootstrap Tooltips
    new bootstrap.Tooltip(document.body, { selector: '[data-bs-toggle="tooltip"]' });

    // Sidebar Toggle
    const menuToggle = document.getElementById('menu-toggle');
    if (menuToggle) {
        menuToggle.addEventListener('click', (e) => {
            e.preventDefault();
            document.getElementById('wrapper').classList.toggle('toggled');
        });
    }

    // --- Module Specific Initializations ---

    // Initialize Sidebar Menu (from sidebar.js)
    if (typeof buildSidebarMenu !== 'undefined') {
        buildSidebarMenu();
    } else {
        console.warn("buildSidebarMenu function not found. Sidebar may not be initialized. Ensure sidebar.js is loaded.");
    }

    // Initialize Institute Management features (from institute-management.js)
    if (typeof initInstituteManagement !== 'undefined') {
        initInstituteManagement();
    } else {
        console.warn("initInstituteManagement function not found. Institute features may not be initialized. Ensure institute-management.js is loaded.");
    }

    // Initialize Admission Management features (from admission-management.js)
    
    if (typeof initAdmissionManagement !== 'undefined') {
        initAdmissionManagement({
            allProgrammesData: allProgrammesData,
            allSchedulesData: allSchedulesData
        });
    } else {
        console.warn("initAdmissionManagement function not found. Admission features may not be initialized. Ensure admission-management.js is loaded.");
    }
}
import { showSuccess, showError, showWarning, showLoading, hideLoading, showConfirm } from './utils.js';
import { initializePersonalDetailsForm } from './personal-details.js';
import { initializeAcademicDetailsForm } from './academic-details.js';
import { initializeProgrammeSelection } from './programme-selection.js';
import { initializeDocumentUploadForm } from './document-upload.js';
import { initializePaymentForm } from './payment.js';
import { showPdfPreviewModal } from './pdf.js';
import { initializeSeatAllotmentPage, refreshSidebarCounseling } from './seat-allotment.js';
import { initializeSelectSubjects } from './select-subjects.js';

export function updateSidebarState(status) {
    if (!status) return;

    const applicationSubmenu = document.getElementById('applicationSubmenu');
    const printLink = document.getElementById('printApplicationLink');
    if (!applicationSubmenu || !printLink) return;

    const workflow = {
        'personal-details': status.personalDetailsComplete,
        'academic-details': status.academicDetailsComplete,
        'programme-selection': status.programmeSelectionComplete,
        'document-upload': status.documentsUploadComplete,
        'payment/show-details': status.paymentComplete
    };

    let enableNext = true;
    for (const stepKey in workflow) {
        const link = applicationSubmenu.querySelector(`a[data-url*="${stepKey}"]`);
        if (link) {
            const isComplete = workflow[stepKey];

            if (enableNext) {
                link.classList.remove('disabled');
                link.removeAttribute('aria-disabled');
            } else {
                link.classList.add('disabled');
                link.setAttribute('aria-disabled', 'true');
            }

            let icon = link.querySelector('.sidebar-status-icon');

            if (!icon) {
                icon = document.createElement('i');
                link.prepend(icon);
            }

            if (isComplete) {
                icon.className = 'sidebar-status-icon bi bi-check-circle-fill text-success me-2';
            } else {
                icon.className = 'sidebar-status-icon bi bi-circle text-muted opacity-50 me-2';
            }

            if (enableNext && !isComplete) {
                enableNext = false;
            }
        }
    }

    if (status.paymentComplete) {
        printLink.classList.remove('disabled');
        printLink.removeAttribute('aria-disabled');
    } else {
        printLink.classList.add('disabled');
        printLink.setAttribute('aria-disabled', 'true');
    }
}

export function handleAjaxFormSubmit(event) {
    event.preventDefault();
    const form = event.currentTarget;

    if (form.id === 'preference-form') {
        return;
    }

    form.classList.add('user-interacted');

    if (!form.checkValidity()) {
        showError('Please fill in all required fields correctly.');
        return;
    }

    const btn = form.querySelector('button[type="submit"]');
    if (!btn) return;
    const originalContent = Array.from(btn.childNodes).map(n => n.cloneNode(true));

    btn.disabled = true;
    btn.replaceChildren();

    const spinner = document.createElement("span");
    spinner.className = "spinner-border spinner-border-sm";

    const text = document.createTextNode(" Saving...");

    btn.append(spinner, text);

    const formData = new FormData(form);
    const data = new URLSearchParams(formData);

    axios.post(form.action, data)
        .then(async (res) => {
            const status = res.data;
            updateSidebarState(status);

            const nextStepMap = {
                'personal-details-form': '/applicants/fragments/academic-details',
                'academic-details-form': '/applicants/fragments/programme-selection',
            };

            const nextStepUrl = nextStepMap[form.id];

            if (nextStepUrl) {
                const nextLink = document.querySelector(`.nav-link[data-url="${nextStepUrl}"]`);
                if (nextLink) {
                    await showSuccess('Saved successfully. Loading next step...');
                    nextLink.click();
                }
            } else {
                await showSuccess('Saved successfully.');
            }
        })
        .catch(err => {
            const serverMessage = err.response?.data?.message;
            const finalMessage = serverMessage || 'Could not save your data. Please check all fields for errors and try again.';
            showError(finalMessage);
        })
        .finally(() => {
            btn.disabled = false;
            btn.replaceChildren(...originalContent);
        });
}

document.addEventListener('DOMContentLoaded', function () {
    const contentArea = document.getElementById('main-content-area');
    const token = document.querySelector('meta[name="_csrf"]').content;
    const header = document.querySelector('meta[name="_csrf_header"]').content;
    axios.defaults.headers.common[header] = token;

    const loadFragment = async (url, linkElement) => {
        let activeId = sessionStorage.getItem('activeApplicationId');
        if (!url.includes('/dashboard') && !activeId) {
            return showWarning('Please select an application from your dashboard to continue.');
        }
        let ctxUrl = url + (activeId && !url.includes('/dashboard') ? (url.includes('?') ? '&' : '?') + `applicationId=${activeId}` : '');

        contentArea.replaceChildren();
		const wrapper = document.createElement("div");
		wrapper.className = "d-flex justify-content-center p-5";

		const spinner = document.createElement("div");
        // CSP FIX: Removed inline styles, relied on Bootstrap padding classes
		spinner.className = "spinner-border text-primary p-4";

		wrapper.appendChild(spinner);
		contentArea.appendChild(wrapper);
        try {
            const res = await axios.get(ctxUrl);
            contentArea.replaceChildren();
			contentArea.insertAdjacentHTML('afterbegin', res.data);

            document.querySelectorAll('.sidebar .nav-link').forEach(l => l.classList.remove('active'));
            const baseUrl = url.split('?')[0];
            const matchingSidebarLink = document.querySelector(`.sidebar .nav-link[data-url^="${baseUrl}"]`);

            if (matchingSidebarLink) {
                matchingSidebarLink.classList.add('active');
                const parentSubMenu = matchingSidebarLink.closest('.collapse');
                if (parentSubMenu) {
                    document.querySelectorAll('.sidebar .collapse.show').forEach(openSubMenu => {
                        if (openSubMenu !== parentSubMenu) {
                            if (typeof bootstrap !== 'undefined' && bootstrap.Collapse) {
                                const bsCollapse = bootstrap.Collapse.getInstance(openSubMenu);
                                if (bsCollapse) bsCollapse.hide();
                            } else {
                                openSubMenu.classList.remove('show');
                            }
                            const trigger = document.querySelector(`[data-bs-target="#${openSubMenu.id}"]`);
                            if (trigger) {
                                trigger.setAttribute('aria-expanded', 'false');
                                trigger.classList.add('collapsed');
                            }
                        }
                    });

                    if (!parentSubMenu.classList.contains('show')) {
                        if (typeof bootstrap !== 'undefined' && bootstrap.Collapse) {
                            const bsCollapse = bootstrap.Collapse.getOrCreateInstance(parentSubMenu);
                            bsCollapse.show();
                        } else {
                            parentSubMenu.classList.add('show');
                        }
                    }

                    const parentMenuTrigger = document.querySelector(`[data-bs-target="#${parentSubMenu.id}"]`);
                    if (parentMenuTrigger) {
                        parentMenuTrigger.classList.add('active');
                        parentMenuTrigger.setAttribute('aria-expanded', 'true');
                        parentMenuTrigger.classList.remove('collapsed');
                    }
                }
            } else if (linkElement && linkElement.classList.contains('nav-link')) {
                linkElement.classList.add('active');
            }

            if (url.includes('/dashboard')) {
                const overviewCard = contentArea.querySelector('[data-active-id]');
                if (overviewCard && overviewCard.dataset.activeId) {
                    const activeIdFromServer = overviewCard.dataset.activeId;
                    sessionStorage.setItem('activeApplicationId', activeIdFromServer);
                }
            }

            const activeIdForStatus = sessionStorage.getItem('activeApplicationId');

            if (activeIdForStatus) {
                try {
                    const statusResponse = await axios.get(`/applicants/application-status/${activeIdForStatus}`);
                    updateSidebarState(statusResponse.data);
                } catch (statusError) {
                    console.error("Could not fetch application status.", statusError);
                    updateSidebarState(null);
                }
            } else {
                updateSidebarState(null);
            }

            if (url.includes('/personal-details'))    initializePersonalDetailsForm();
            if (url.includes('/academic-details'))    initializeAcademicDetailsForm();
            if (url.includes('/programme-selection')) initializeProgrammeSelection();
            if (url.includes('/document-upload'))     initializeDocumentUploadForm();
            if (url.includes('/payment/show-details')) initializePaymentForm();
            if (url.includes('/counseling/result'))   initializeSeatAllotmentPage();
            if (url.includes('/applicants/select-subjects')) initializeSelectSubjects();

        } catch (err) {
            console.error('Fragment Load Error:', err);
            contentArea.innerHTML = `<div class="alert alert-danger m-3">Failed to load content. Please refresh the page and try again.</div>`;
        }
    };

    const handleApplicationSwitch = async (button) => {
        const appId = button.dataset.applicationId;
        showLoading('Switching active application...');
        try {
            await axios.post('/applicants/select-active-application', { applicationId: appId });
            sessionStorage.setItem('activeApplicationId', appId);
            initialLoad();
            hideLoading();
        } catch (error) {
            showError("Could not switch the application due to a server error.");
        }
    };

    const initialLoad = () => {
        const sidebar = document.getElementById('sidebarMenu');
        if (sidebar && sidebar.dataset.activeId) {
            sessionStorage.setItem('activeApplicationId', sidebar.dataset.activeId);
        }

        const dashboardLink = document.querySelector('.sidebar .nav-link[data-url*="/dashboard"]');
        if (dashboardLink) {
            loadFragment(dashboardLink.getAttribute('data-url'), dashboardLink);
        }
        refreshSidebarCounseling();
    };

    document.body.addEventListener('click', (event) => {
        const navLoader = event.target.closest('.nav-loader');
        const logoutButton = event.target.closest('#logoutButton');
        const printLink = event.target.closest('#printApplicationLink');
        const switchAppButton = event.target.closest('.switch-app-btn');

        if (navLoader && !navLoader.classList.contains('disabled')) {
            event.preventDefault();
            const url = navLoader.getAttribute('data-url');
            if (url) loadFragment(url, navLoader);
        }
        if (logoutButton) {
            event.preventDefault();
            logout();
        }
        if (printLink && !printLink.classList.contains('disabled')) {
            event.preventDefault();
            showPdfPreviewModal();
        }
        if (switchAppButton) {
            event.preventDefault();
            handleApplicationSwitch(switchAppButton);
        }
    });

    initialLoad();
});

function logout() {
    // CSP FIX: Replaced Swal.fire with showConfirm
    showConfirm('Are you sure you want to logout?', 'You will need to log in again to access your application.', 'Yes, logout', 'btn-danger').then(result => {
        if (result.isConfirmed) {
            sessionStorage.removeItem('activeApplicationId');
            const form = document.createElement('form');
            form.method = 'POST';
            form.action = '/logout';

            const csrfToken = document.querySelector('meta[name="_csrf"]').content;
            const csrfParameter = document.querySelector('meta[name="_csrf_parameter"]').content;

            const csrfInput = document.createElement('input');
            csrfInput.type = 'hidden';
            csrfInput.name = csrfParameter;
            csrfInput.value = csrfToken;
            form.appendChild(csrfInput);

            document.body.appendChild(form);
            form.submit();
        }
    });
}
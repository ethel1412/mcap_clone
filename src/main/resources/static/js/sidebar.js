function buildSidebarMenu() {
    const menuContainer = document.getElementById('sidebar-menu-container');
    if (!menuContainer) {
        console.warn("Sidebar menu container (#sidebar-menu-container) not found. Skipping menu build.");
        return;
    }

    axios.get('/menu')
        .then(response => {
            const menuItems = response.data;

            menuContainer.replaceChildren();

            const accordion = document.createElement("div");
            accordion.className = "accordion";
            accordion.id = "sidebarAccordion";

            menuItems.forEach(item => {
                const hasChildren = item.children && item.children.length > 0;

                if (hasChildren) {
                    const submenuId = `submenu-${item.menuId}`;

                    // Parent link
                    const parentLink = document.createElement("a");
                    parentLink.href = `#${submenuId}`;
                    parentLink.setAttribute("data-bs-toggle", "collapse");
                    parentLink.setAttribute("data-bs-parent", "#sidebarAccordion");
                    parentLink.setAttribute("aria-expanded", "false");
                    parentLink.className = "list-group-item list-group-item-action d-flex align-items-center";

                    const parentIcon = document.createElement("i");
                    parentIcon.className = `${item.iconClass || 'fas fa-folder'} fa-fw me-3`;

                    const parentText = document.createElement("span");
                    parentText.className = "sidebar-text";
                    parentText.textContent = item.name;

                    const arrow = document.createElement("i");
                    arrow.className = "fas fa-chevron-down sidebar-arrow ms-auto";

                    parentLink.append(parentIcon, parentText, arrow);

                    // Submenu container
                    const submenu = document.createElement("div");
                    submenu.className = "collapse sidebar-submenu";
                    submenu.id = submenuId;
                    submenu.setAttribute("data-bs-parent", "#sidebarAccordion");

                    // Children
                    item.children.forEach(child => {
                        const childLink = document.createElement("a");
                        childLink.href = child.url;
                        childLink.setAttribute("data-page-url", child.url);
                        childLink.className = "list-group-item list-group-item-action sidebar-link-item d-flex align-items-center";

                        const icon = document.createElement("i");
                        icon.className = `${child.iconClass || 'fas fa-arrow-right'} fa-fw me-3`;

                        const text = document.createElement("span");
                        text.className = "sidebar-text";
                        text.textContent = child.name;

                        const badge = document.createElement("span");
                        badge.className = "badge bg-danger ms-auto notification-badge";
                        badge.style.display = "none";
                        badge.textContent = "0";

                        childLink.append(icon, text, badge);
                        submenu.appendChild(childLink);
                    });

                    accordion.append(parentLink, submenu);

                } else if (item.url) {
                    const link = document.createElement("a");
                    link.href = item.url;
                    link.setAttribute("data-page-url", item.url);
                    link.className = "list-group-item list-group-item-action sidebar-link-item d-flex align-items-center";

                    const icon = document.createElement("i");
                    icon.className = `${item.iconClass || 'fas fa-arrow-right'} fa-fw me-3`;

                    const text = document.createElement("span");
                    text.className = "sidebar-text";
                    text.textContent = item.name;

                    const badge = document.createElement("span");
                    badge.className = "badge bg-danger ms-auto notification-badge";
                    badge.style.display = "none";
                    badge.textContent = "0";

                    link.append(icon, text, badge);
                    accordion.appendChild(link);
                }
            });

            menuContainer.appendChild(accordion);

            highlightActiveMenuItem();

            document.dispatchEvent(new Event('sidebarMenuBuilt'));
        })
        .catch(error => {
            console.error('Failed to load menu:', error);

            menuContainer.replaceChildren();

            const div = document.createElement("div");
            div.className = "list-group-item text-danger p-3";
            div.textContent = "Error loading navigation menu. Please check backend /menu endpoint.";

            menuContainer.appendChild(div);
        });
}

function highlightActiveMenuItem() {
    const currentPath = window.location.pathname;
    const links = document.querySelectorAll('#sidebar-menu-container a');
    let bestMatch = null;

    links.forEach(link => {
        const linkPath = link.getAttribute('href');
        if (linkPath && !linkPath.startsWith('#') && currentPath.startsWith(linkPath)) {
            if (!bestMatch || linkPath.length > bestMatch.getAttribute('href').length) {
                bestMatch = link;
            }
        }
    });

    if (bestMatch) {
        bestMatch.classList.add('active');

        const parentSubmenu = bestMatch.closest('.sidebar-submenu');
        if (parentSubmenu) {
            const parentTrigger = document.querySelector(`a[href="#${parentSubmenu.id}"]`);
            if (parentTrigger) {
                parentTrigger.classList.add('active');
                parentTrigger.setAttribute('aria-expanded', 'true');
                parentSubmenu.classList.add('show');
            }
        }
    }

    document.querySelectorAll('[data-bs-toggle="collapse"]').forEach(function (toggle) {
        let icon = toggle.querySelector('.fa-chevron-down');
        let targetId = toggle.getAttribute('href') || toggle.getAttribute('data-bs-target');
        let submenu = document.querySelector(targetId);

        if (submenu && submenu.classList.contains('show')) {
            if (icon) icon.classList.add('rotate');
        }

        toggle.addEventListener('click', function () {
            setTimeout(function () {
                if (submenu && icon) {
                    if (submenu.classList.contains('show')) {
                        icon.classList.add('rotate');
                    } else {
                        icon.classList.remove('rotate');
                    }
                }
            }, 200);
        });
    });
}

document.addEventListener('DOMContentLoaded', () => {
    buildSidebarMenu();

    const showProfileModalLink = document.getElementById('showProfileModal');
    if (showProfileModalLink) {
        showProfileModalLink.addEventListener('click', (e) => {
            e.preventDefault();
            e.stopPropagation();
            new bootstrap.Modal(document.getElementById('profileModal')).show();
        });
    }

    const menuToggleBtn = document.getElementById('menu-toggle');
    if (menuToggleBtn) {
        menuToggleBtn.addEventListener('click', (e) => {
            e.preventDefault();
            e.stopPropagation();
            document.getElementById('wrapper')?.classList.toggle('toggled');
        });
    }

    const profileModal = document.getElementById('profileModal');
    if (profileModal) {
        profileModal.addEventListener('hidden.bs.modal', () => {
            window.location.reload();
        });
    }
});
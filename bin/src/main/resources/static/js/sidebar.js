
function buildSidebarMenu() {
    const menuContainer = document.getElementById('sidebar-menu-container');
    if (!menuContainer) {
        console.warn("Sidebar menu container (#sidebar-menu-container) not found. Skipping menu build.");
        return;
    }

    axios.get('/menu')
        .then(response => {
            const menuItems = response.data;
            let menuHtml = '';

            menuHtml += `<div class="accordion" id="sidebarAccordion">`;

            menuItems.forEach(item => {
                const hasChildren = item.children && item.children.length > 0;

                if (hasChildren) {
                    const submenuId = `submenu-${item.menuId}`;

                    menuHtml += `
                        <a href="#${submenuId}"
                           data-bs-toggle="collapse"
                           data-bs-parent="#sidebarAccordion"
                           aria-expanded="false"
                           class="list-group-item list-group-item-action d-flex align-items-center">
                            <i class="${item.iconClass || 'fas fa-folder'} fa-fw me-3"></i>
                            <span class="sidebar-text">${item.name}</span>
                            <i class="fas fa-chevron-down sidebar-arrow ms-auto"></i>
                        </a>
                    `;

                    menuHtml += `<div class="collapse sidebar-submenu" id="${submenuId}" data-bs-parent="#sidebarAccordion">`;

                    item.children.forEach(child => {
                        menuHtml += `
                            <a href="${child.url}" class="list-group-item list-group-item-action">
                            <i class="${child.iconClass || 'fas fa-arrow-right'} fa-fw me-3"></i>
                                <span class="sidebar-text">${child.name}</span>
                            </a>`;
                    });

                    menuHtml += `</div>`;

                } else if (item.url) {
                    menuHtml += `
                        <a href="${item.url}" class="list-group-item list-group-item-action d-flex align-items-center">
                            <i class="${item.iconClass || 'fas fa-arrow-right'} fa-fw me-3"></i>
                            <span class="sidebar-text">${item.name}</span>
                        </a>`;
                }
            });

            menuHtml += `</div>`;

            menuContainer.innerHTML = menuHtml;
            highlightActiveMenuItem();
        })
        .catch(error => {
            console.error('Failed to load menu:', error);
            if (menuContainer) {
                menuContainer.innerHTML = '<div class="list-group-item text-danger p-3">Error loading navigation menu. Please check backend /menu endpoint.</div>';
            }
        });
}


function highlightActiveMenuItem() {
    const currentPath = window.location.pathname;
    const links = document.querySelectorAll('#sidebar-menu-container a');
    let bestMatch = null;

    links.forEach(link => {
        const linkPath = link.getAttribute('href');
       
        if (linkPath && !linkPath.startsWith('#') && currentPath.startsWith(linkPath)) {
            // Find the longest matching path for more specific highlighting
            if (!bestMatch || linkPath.length > bestMatch.getAttribute('href').length) {
                bestMatch = link;
            }
        }
    });

    if (bestMatch) {
        bestMatch.classList.add('active');

        // If the active item is in a submenu, ensure its parent accordion is open
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

    // Initialize accordion arrow rotation
    document.querySelectorAll('[data-bs-toggle="collapse"]').forEach(function (toggle) {
        let icon = toggle.querySelector('.fa-chevron-down');
        let targetId = toggle.getAttribute('href') || toggle.getAttribute('data-bs-target');
        let submenu = document.querySelector(targetId);

        // Initial check for 'show' class on load
        if (submenu && submenu.classList.contains('show')) {
            if (icon) icon.classList.add('rotate');
        }

        toggle.addEventListener('click', function () {
            // Using setTimeout to allow Bootstrap's collapse animation to start/finish
            setTimeout(function () {
                if (submenu && icon) {
                    if (submenu.classList.contains('show')) {
                        icon.classList.add('rotate');
                    } else {
                        icon.classList.remove('rotate');
                    }
                }
            }, 200); // Small delay to match collapse transition
        });
    });
}
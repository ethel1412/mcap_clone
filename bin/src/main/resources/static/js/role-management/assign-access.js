axios.defaults.withCredentials = true;

// --- DOM Elements ---
const roleSelect = document.getElementById("roleSelect");
const menuTreeContainer = document.getElementById("menuTreeContainer");
const saveAccessBtn = document.getElementById("saveAccessBtn");
const messageArea = document.getElementById("messageArea");

// --- Event Listeners ---
document.addEventListener("DOMContentLoaded", () => {
    loadRoles();
    roleSelect.addEventListener("change", loadMenusForRole);
    saveAccessBtn.addEventListener("click", saveAccess);
});


function showLoading(message = "Loading menus...") {
    menuTreeContainer.innerHTML = `
        <div class="d-flex justify-content-center align-items-center h-100 text-muted">
            <div class="spinner-border text-warning me-2" role="status">
                <span class="visually-hidden">Loading...</span>
            </div>
            ${message}
        </div>`;
    saveAccessBtn.disabled = true;
}


function showMessage(text, type = 'success') {
    const alertClass = `alert-${type}`;
    messageArea.innerHTML = `<div class="alert ${alertClass} alert-dismissible fade show" role="alert">
        ${text}
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    </div>`;
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

async function loadRoles() {
    try {
        const res = await axios.get("/role-data/roles");
        res.data.forEach(role => {
            const opt = document.createElement("option");
            opt.value = role.roleId;
            opt.textContent = role.roleName;
            roleSelect.appendChild(opt);
        });
    } catch (err) {
        console.error("Error loading roles", err);
        showMessage("Could not load roles. The server might be down.", "danger");
    }
}


async function loadMenusForRole() {
    const roleId = roleSelect.value;

    if (!roleId) {
        menuTreeContainer.innerHTML = `<div class="d-flex justify-content-center align-items-center h-100 text-muted">Select a role to load its menus...</div>`;
        saveAccessBtn.disabled = true;
        return;
    }

    showLoading();

    showLoading();

    try {
        const [menusRes, assignedRes] = await Promise.all([
            axios.get("/role-data/menus"),
            axios.get(`/role-data/role-menu-access/${roleId}`)
        ]);

        const allMenus = menusRes.data;
        const assignedIds = new Set(assignedRes.data.map(m => m.menuId));
        const menuTree = buildMenuTree(allMenus);
        renderMenuTree(menuTree, assignedIds);
        saveAccessBtn.disabled = false;
    } catch (err) {
        console.error(err);
        menuTreeContainer.innerHTML = `<div class="text-danger p-3">Error loading menu access. Please check the browser console for details.</div>`;
        showMessage("An error occurred while loading menu data.", "danger");
    }
}

function buildMenuTree(menus) {
    const menuMap = new Map(menus.map(menu => [menu.menuId, { ...menu, children: [] }]));
    const tree = [];

    for (const menu of menuMap.values()) {
        if (menu.parentId && menuMap.has(menu.parentId)) {
            menuMap.get(menu.parentId).children.push(menu);
        } else {
            tree.push(menu);
        }
    }
    return tree;
}

function renderMenuTree(menuTree, assignedIds) {
    if (menuTree.length === 0) {
        menuTreeContainer.innerHTML = '<div class="text-muted p-3">No menus have been configured in the system.</div>';
        return;
    }
    const html = menuTree.map(node => renderMenuNode(node, assignedIds)).join('');
    menuTreeContainer.innerHTML = `<div class="menu-tree">${html}</div>`;

    menuTreeContainer.querySelectorAll('input[type="checkbox"]').forEach(checkbox => {
        checkbox.addEventListener('change', handleCheckboxChange);
    });
}

function renderMenuNode(node, assignedIds) {
    const isChecked = assignedIds.has(node.menuId) ? 'checked' : '';
    const childrenHtml = node.children.length > 0
        ? `<div class="menu-node-children">${node.children.map(child => renderMenuNode(child, assignedIds)).join('')}</div>`
        : '';
    
    return `
        <div class="menu-node">
            <div class="form-check">
                <input class="form-check-input" type="checkbox" value="${node.menuId}" id="menu-${node.menuId}" ${isChecked}>
                <label class="form-check-label" for="menu-${node.menuId}">
                    ${node.name} 
                </label>
            </div>
            ${childrenHtml}
        </div>
    `;
}

/**
 * Handles the cascading check/uncheck logic for parent-child menus.
 */
function handleCheckboxChange(event) {
    const checkbox = event.target;
    const isChecked = checkbox.checked;
    const parentNode = checkbox.closest('.menu-node');
    const childCheckboxes = parentNode.querySelectorAll('.menu-node-children input[type="checkbox"]');
    childCheckboxes.forEach(child => child.checked = isChecked);
}

/**
 * Saves the currently selected menu access configuration for the role.
 */
async function saveAccess() {
    const roleId = roleSelect.value;
    if (!roleId) return;

    const checkedIds = [...menuTreeContainer.querySelectorAll("input[type=checkbox]:checked")]
        .map(cb => parseInt(cb.value));

    const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;
    const headers = csrfToken ? { [csrfHeader]: csrfToken } : {};
    
    saveAccessBtn.disabled = true;
    saveAccessBtn.innerHTML = `<span class="spinner-border spinner-border-sm" role="status" aria-hidden="true"></span> Saving...`;

    try {
        await axios.post(`/role-data/role-menu-access/${roleId}`, checkedIds, { headers });
        showMessage("Access permissions updated successfully!", "success");
    } catch (err) {
        console.error(err);
        showMessage("Failed to update access. An error occurred.", "danger");
    } finally {
        saveAccessBtn.disabled = false;
        saveAccessBtn.innerHTML = `<i class="fas fa-save me-2"></i>Save Changes`;
    }
}
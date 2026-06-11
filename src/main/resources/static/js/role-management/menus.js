axios.defaults.withCredentials = true;

document.addEventListener("DOMContentLoaded", () => {
    loadMenus();
    document.getElementById("addMenuForm").addEventListener("submit", submitAddMenu);
});

async function loadMenus() {
    const tbody = document.getElementById("menusBody");

    // Loading state
    tbody.replaceChildren();
    const trLoad = document.createElement("tr");
    const tdLoad = document.createElement("td");
    tdLoad.colSpan = 4;
    tdLoad.className = "text-center";
    tdLoad.textContent = "Loading...";
    trLoad.appendChild(tdLoad);
    tbody.appendChild(trLoad);

    try {
        const res = await axios.get("/role-data/menus");

        tbody.replaceChildren();

        res.data.forEach((menu, idx) => {
            const tr = document.createElement("tr");

            const td1 = document.createElement("td");
            td1.textContent = idx + 1;

            const td2 = document.createElement("td");
            td2.textContent = menu.menuId || "-";

            const td3 = document.createElement("td");
            td3.textContent = menu.name;

            const td4 = document.createElement("td");

            if (menu.assignedRoles && menu.assignedRoles.length) {
                menu.assignedRoles.forEach(r => {
                    const span = document.createElement("span");
                    span.className = "badge bg-primary me-1";
                    span.textContent = r.roleName;
                    td4.appendChild(span);
                });
            } else {
                const span = document.createElement("span");
                span.className = "text-muted";
                span.textContent = "No roles assigned";
                td4.appendChild(span);
            }

            tr.append(td1, td2, td3, td4);
            tbody.appendChild(tr);
        });

    } catch (err) {
        console.error("Error loading menus:", err);

        tbody.replaceChildren();

        const tr = document.createElement("tr");
        const td = document.createElement("td");
        td.colSpan = 4;
        td.className = "text-danger text-center";
        td.textContent = "Error loading menus";
        tr.appendChild(td);
        tbody.appendChild(tr);
    }
}

async function submitAddMenu(e) {
    e.preventDefault();

    const name = document.getElementById("menuNameInput").value.trim();
    const iconClass = document.getElementById("iconInput").value.trim();
    const orderIndex = parseInt(document.getElementById("orderInput").value) || 0;
    const parentMenuId = document.getElementById("parentMenuSelect").value || null;
    const active = document.getElementById("activeCheck").checked;

    const errorDiv = document.getElementById("addMenuError");

    if (!name) {
        errorDiv.textContent = "Menu name is required.";
        errorDiv.classList.remove("d-none");
        return;
    }
    errorDiv.classList.add("d-none");

    const csrfToken = document.querySelector('meta[name="_csrf"]').content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

    try {
        await axios.post("/role-data/menus",
            {
                menuName: name,
                iconClass,
                orderIndex,
                active,
                parentMenuId: parentMenuId ? parseInt(parentMenuId) : null
            },
            { headers: { [csrfHeader]: csrfToken } }
        );

        bootstrap.Modal.getInstance(document.getElementById("addMenuModal")).hide();
        e.target.reset();
        loadMenus();
    } catch (err) {
        console.error("Error adding menu:", err);
        errorDiv.textContent = err.response?.data?.message || "Failed to add menu.";
        errorDiv.classList.remove("d-none");
    }
}

async function populateParentMenuSelect() {
    try {
        const res = await axios.get("/role-data/menus");
        const select = document.getElementById("parentMenuSelect");

        select.replaceChildren();

        const defaultOpt = document.createElement("option");
        defaultOpt.value = "";
        defaultOpt.textContent = "-- None (Top Level) --";
        select.appendChild(defaultOpt);

        res.data.forEach(menu => {
            const opt = document.createElement("option");
            opt.value = menu.menuId;
            opt.textContent = menu.name;
            select.appendChild(opt);
        });

    } catch (e) {
        console.error("Failed to load parent menus", e);
    }
}

async function loadIcons() {
    const container = document.getElementById("iconPickerContainer");
    const searchBox = document.getElementById("iconSearch");

    try {
        const res = await fetch("/data/icons.json");
        const icons = await res.json();

        let allIcons = Object.keys(icons);

        function renderIcons(filter = "") {
            container.replaceChildren();

            const filtered = allIcons
                .filter(name => name.includes(filter.toLowerCase()))
                .slice(0, 100);

            filtered.forEach(name => {
                const iconClass = `fa-solid fa-${name}`;

                const btn = document.createElement("button");
                btn.type = "button";
                btn.className = "btn btn-outline-secondary icon-option";
                btn.dataset.icon = iconClass;

                // icon
                const icon = document.createElement("i");
                icon.className = iconClass;
                btn.appendChild(icon);

                btn.addEventListener("click", () => {
                    document.querySelectorAll(".icon-option")
                        .forEach(b => b.classList.remove("btn-success"));

                    btn.classList.add("btn-success");
                    document.getElementById("iconInput").value = iconClass;
                });

                container.appendChild(btn);
            });
        }

        renderIcons();

        searchBox.addEventListener("input", e => renderIcons(e.target.value));

    } catch (err) {
        console.error("Failed to load icons:", err);

        container.replaceChildren();

        const div = document.createElement("div");
        div.className = "text-danger";
        div.textContent = "Error loading icons";

        container.appendChild(div);
    }
}

// call this when modal opens
document.getElementById("addMenuModal").addEventListener("shown.bs.modal", loadIcons);
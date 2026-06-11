document.addEventListener("DOMContentLoaded", function() {
    const table = document.getElementById("shiftTable");
    const tabContainer = document.getElementById("shiftTabsContainer");

    if (!table || !tabContainer) return;

    const rows = table.querySelectorAll("tbody tr.programme-row");
    if (rows.length === 0) return;

    // 1. Find all unique shifts
    const shifts = new Map();
    rows.forEach(row => {
        const shiftCode = row.getAttribute("data-shift") || "NA";
        const shiftName = row.getAttribute("data-shift-name") || "Not Applicable";
        shifts.set(shiftCode, shiftName);
    });

    // 2. If only NA exists, skip
    if (shifts.size <= 1 && shifts.has("NA")) return;

    // ===== 3. Build Tabs (NO innerHTML) =====
    tabContainer.replaceChildren();

    const ul = document.createElement("ul");
    ul.className = "nav nav-tabs mb-3";
    ul.id = "shiftNavTabs";
    ul.setAttribute("role", "tablist");

    // ===== All Tab =====
    const liAll = document.createElement("li");
    liAll.className = "nav-item";
    liAll.setAttribute("role", "presentation");

    const btnAll = document.createElement("button");
    btnAll.className = "nav-link active fw-bold";
    btnAll.setAttribute("data-bs-toggle", "tab");
    btnAll.setAttribute("data-target-shift", "ALL");
    btnAll.type = "button";
    btnAll.setAttribute("role", "tab");

    const icon = document.createElement("i");
    icon.className = "fas fa-layer-group me-1";

    btnAll.append(icon, document.createTextNode(" All Shifts"));
    liAll.appendChild(btnAll);
    ul.appendChild(liAll);

    // ===== Shift Tabs =====
    shifts.forEach((name, code) => {
        const li = document.createElement("li");
        li.className = "nav-item";
        li.setAttribute("role", "presentation");

        const btn = document.createElement("button");
        btn.className = "nav-link fw-bold";
        btn.setAttribute("data-bs-toggle", "tab");
        btn.setAttribute("data-target-shift", code);
        btn.type = "button";
        btn.setAttribute("role", "tab");

        btn.textContent = name;

        li.appendChild(btn);
        ul.appendChild(li);
    });

    tabContainer.appendChild(ul);

    // ===== 4. Click Logic (UNCHANGED) =====
    const buttons = tabContainer.querySelectorAll("button.nav-link");

    buttons.forEach(btn => {
        btn.addEventListener("click", function() {
            buttons.forEach(b => b.classList.remove("active"));
            this.classList.add("active");

            const targetShift = this.getAttribute("data-target-shift");

            rows.forEach(row => {
                if (targetShift === "ALL" || row.getAttribute("data-shift") === targetShift) {
                    row.style.display = "";
                } else {
                    row.style.display = "none";
                }
            });
        });
    });
});
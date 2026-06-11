document.addEventListener("DOMContentLoaded", function () {

    const modalEl = document.getElementById('detailsModal');
    if (!modalEl) return; // ✅ safety

    const modal = bootstrap.Modal.getOrCreateInstance(modalEl);
    const modalTitle = document.getElementById("modalTitle");
    const modalContent = document.getElementById("modalContent");

    let isLoading = false;

    function clearContent() {
        modalContent.innerHTML = "";
    }

    function renderList(list) {
        clearContent();

        if (!list || list.length === 0) {
            modalContent.innerHTML = `<span class="text-muted">No data found</span>`;
            return;
        }

        const ul = document.createElement("ul");
        ul.className = "list-group w-100";

        list.forEach((item, index) => {
            const li = document.createElement("li");
            li.className = "list-group-item";
            li.innerHTML = `<strong>${index + 1}.</strong> ${item}`;
            ul.appendChild(li);
        });

        modalContent.appendChild(ul);
    }

    document.addEventListener("click", function (e) {

        const deptEl = e.target.closest(".view-departments");
        const progEl = e.target.closest(".view-programmes");

        if (!deptEl && !progEl) return;

        e.preventDefault();

        if (isLoading) return;
        isLoading = true;

        const el = deptEl || progEl;
        const id = el.dataset.id;

        if (!id) {
            console.error("Missing data-id");
            isLoading = false;
            return;
        }

        const isDept = !!deptEl;

        const endpoint = isDept ? "departments" : "programmes-with-shift";
        const title = isDept ? "Departments" : "Programmes & Shifts";

        modalTitle.textContent = title;
        clearContent();

        modalContent.innerHTML = `
            <div class="text-center w-100 py-3">
                <div class="spinner-border text-primary"></div>
            </div>
        `;

        modal.show();

        fetch(`/mis/${id}/${endpoint}`)
            .then(res => {
                if (!res.ok) throw new Error("Fetch failed");
                return res.json();
            })
            .then(renderList)
            .catch(err => {
                console.error(err);
                modalContent.innerHTML = `<span class="text-danger">Failed to load data</span>`;
            })
            .finally(() => {
                isLoading = false;
            });
    });

});
document.addEventListener('DOMContentLoaded', function () {

    const stateDropdown = document.getElementById('stateDropdown');
    const districtDropdown = document.getElementById('districtDropdown');
    const blockDropdown = document.getElementById('blockDropdown');
    
    if (!stateDropdown || !districtDropdown || !blockDropdown) {
        console.error("Dropdowns not found ❌");
        return;
    }

    // ================= STATE CHANGE =================
    stateDropdown.addEventListener('change', function () {

        const stateCode = this.value;
        console.log("State changed:", stateCode); // 🔥 debug

        districtDropdown.innerHTML = '<option>Loading...</option>';
        blockDropdown.innerHTML = '<option>-- Select Block --</option>';

        if (!stateCode) {
            districtDropdown.innerHTML = '<option>-- Select District --</option>';
            return;
        }

        axios.get('/master/get-list-districts/' + stateCode)
            .then(res => {
                console.log("District API:", res.data); // 🔥 debug

                let html = '<option value="">-- Select District --</option>';

                res.data.forEach(d => {
                    html += `<option value="${d.districtCode}">${d.districtName}</option>`;
                });

                districtDropdown.innerHTML = html;
            })
            .catch(err => {
                console.error("District error:", err);
            });
    });

    // ================= DISTRICT CHANGE =================
    districtDropdown.addEventListener('change', function () {

        const districtCode = this.value;
        blockDropdown.innerHTML = '<option>Loading...</option>';

        if (!districtCode) {
            blockDropdown.innerHTML = '<option>-- Select Block --</option>';
            return;
        }

        axios.get('/master/get-list-blocks/' + districtCode)
            .then(res => {

                let html = '<option value="">-- Select Block --</option>';

                res.data.forEach(b => {
                    html += `<option value="${b.blockCode}">${b.blockName}</option>`;
                });

                blockDropdown.innerHTML = html;
            })
            .catch(err => {
                alert("Block error:", err);
            });
    });

});
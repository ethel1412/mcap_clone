export const Pagination = (() => {
    let currentPage = 0;
    let totalPages = 0;
    let pageSize = 10;
    let onPageChangeCallback = null;

    function init({ onPageChange, initialPageSize = 10 }) {
        pageSize = initialPageSize;
        onPageChangeCallback = onPageChange;

        const prevBtn = document.getElementById('prevBtn');
        const nextBtn = document.getElementById('nextBtn');

        if (!prevBtn || !nextBtn) {
            console.warn('⚠️ Pagination controls not found in DOM.');
            return;
        }

        prevBtn.addEventListener('click', () => changePage(currentPage - 1));
        nextBtn.addEventListener('click', () => changePage(currentPage + 1));
    }

    function changePage(page) {
        if (page >= 0 && page < totalPages) {
            currentPage = page;
            if (typeof onPageChangeCallback === 'function') {
                onPageChangeCallback(currentPage, pageSize);
            }
        }
    }

    function update({ pageNumber, totalPageCount }) {
        currentPage = pageNumber;
        totalPages = totalPageCount;

        const pageInfo = document.getElementById('pageInfo');
        const prevBtn = document.getElementById('prevBtn');
        const nextBtn = document.getElementById('nextBtn');

        if (pageInfo) pageInfo.textContent = `Page ${currentPage + 1} of ${totalPages || 1}`;
        if (prevBtn) prevBtn.disabled = currentPage <= 0;
        if (nextBtn) nextBtn.disabled = currentPage >= totalPages - 1;
    }

    return {
        init,
        update
    };
})();

/**
 * Institute View Applicants - DataTable Initialization
 */
(function() {
    'use strict';

    /**
     * Initialize DataTable when DOM is ready
     */
    function initApplicantsTable() {
        const table = $('#applicantsTable');
        
        // Exit silently if we are on a page that doesn't have this table
        if (table.length === 0) {
            return;
        }

        if (!$.fn.DataTable) {
            return;
        }

        table.DataTable({
            pageLength: 10,
            lengthMenu: [[10, 25, 50, 100, -1], [10, 25, 50, 100, "All"]],
            order: [[1, "asc"]], // Sort by Application No.

            language: {
                search: "Search applicants:",
                lengthMenu: "Show _MENU_ applicants per page",
                info: "Showing _START_ to _END_ of _TOTAL_ applicants",
                infoEmpty: "No applicants found",
                infoFiltered: "(filtered from _MAX_ total applicants)",
                zeroRecords: "No matching applicants found",
                paginate: {
                    first: "First",
                    last: "Last",
                    next: "Next",
                    previous: "Previous"
                }
            },

            responsive: true,
            autoWidth: false,

            // Bootstrap 5 styling layout
            dom: '<"row"<"col-sm-12 col-md-6"l><"col-sm-12 col-md-6"f>>' +
                 '<"row"<"col-sm-12"tr>>' +
                 '<"row"<"col-sm-12 col-md-5"i><"col-sm-12 col-md-7"p>>',

            // Column definitions
            columnDefs: [
                {
                    targets: 0,
                    orderable: false,
                    searchable: false,
                    // Dynamic serial number
                    render: function (data, type, row, meta) {
                        if (type === 'display') {
                            return meta.row + meta.settings._iDisplayStart + 1;
                        }
                        return data;
                    }
                }
            ],

            // Re-render serial numbers after every draw (sort, page change, filter)
            drawCallback: function(settings) {
                const api = this.api();
                const startIndex = api.page.info().start;

                api.column(0, { page: 'current' }).nodes().each(function(cell, i) {
                    cell.textContent = startIndex + i + 1;
                });
            }
        });
    }

    // Initialize when document is ready
    if (typeof $ !== 'undefined') {
        $(document).ready(function() {
            initApplicantsTable();
        });
    }

})();
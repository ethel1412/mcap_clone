// Dashboard Statistics and Activity Management
let dashboardRefreshInterval;

/**
 * Load dashboard statistics from backend API
 */
function loadDashboardStats() {
    axios.get('/admin/api/dashboard-stats')
        .then(response => {
            const stats = response.data;
            renderStatsCards(stats);
            updateLastRefreshedTime();
        })
        .catch(error => {
            console.error('Failed to load dashboard stats:', error);
            if (typeof showToast !== 'undefined') {
                showToast('Failed to load dashboard statistics', 'danger');
            }
            const container = document.getElementById('stats-container');
			if (!container) return;			
			container.replaceChildren();
			
			const div = document.createElement("div");
			div.className = "col-12 text-center text-danger py-5";
			
			const icon = document.createElement("i");
			icon.className = "fas fa-exclamation-triangle fa-3x mb-3";
			
			const p = document.createElement("p");
			p.textContent = "Failed to load dashboard statistics";
			
			const btn = document.createElement("button");
			btn.className = "btn btn-primary btn-sm";
			
			const btnIcon = document.createElement("i");
			btnIcon.className = "fas fa-redo me-1";
			
			btn.append(btnIcon, document.createTextNode("Retry"));
			btn.addEventListener("click", loadDashboardStats);
			
			div.append(icon, p, btn);
			container.appendChild(div);
        });
}

/**
 * Render statistics cards with dynamic data
 */
function renderStatsCards(stats) {
    const statsHtml = `
        <div class="col-xl-3 col-md-6 mb-4">
            <div class="card stat-card bg-gradient-primary shadow h-100">
                <div class="card-body">
                    <div class="stat-icon">
                        <i class="fas fa-university"></i>
                    </div>
                    <div class="stat-number">${stats.totalInstitutes || 0}</div>
                    <div class="stat-label">Total Institutes</div>
                </div>
                <div class="card-footer">
                    <a href="/admin/institutes-list" class="text-white text-decoration-none">
                        View All <i class="fas fa-arrow-right ms-1"></i>
                    </a>
                </div>
            </div>
        </div>

        <div class="col-xl-3 col-md-6 mb-4">
            <div class="card stat-card bg-gradient-warning shadow h-100">
                <div class="card-body">
                    <div class="stat-icon">
                        <i class="fas fa-clock"></i>
                    </div>
                    <div class="stat-number">${stats.pendingInstitutes || 0}</div>
                    <div class="stat-label">Pending Approvals</div>
                </div>
                <div class="card-footer">
                    <a href="/admin/institutes-list?status=PENDING" class="text-white text-decoration-none">
                        Review Now <i class="fas fa-arrow-right ms-1"></i>
                    </a>
                </div>
            </div>
        </div>

        <div class="col-xl-3 col-md-6 mb-4">
            <div class="card stat-card bg-gradient-success shadow h-100">
                <div class="card-body">
                    <div class="stat-icon">
                        <i class="fas fa-check-circle"></i>
                    </div>
                    <div class="stat-number">${stats.acceptedInstitutes || 0}</div>
                    <div class="stat-label">Accepted Institutes</div>
                </div>
                <div class="card-footer">
                    <a href="/admin/institutes-list?status=ACCEPTED" class="text-white text-decoration-none">
                        View All <i class="fas fa-arrow-right ms-1"></i>
                    </a>
                </div>
            </div>
        </div>

        <div class="col-xl-3 col-md-6 mb-4">
            <div class="card stat-card bg-gradient-danger shadow h-100">
                <div class="card-body">
                    <div class="stat-icon">
                        <i class="fas fa-times-circle"></i>
                    </div>
                    <div class="stat-number">${stats.rejectedInstitutes || 0}</div>
                    <div class="stat-label">Rejected Institutes</div>
                </div>
                <div class="card-footer">
                    <a href="/admin/institutes-list?status=REJECTED" class="text-white text-decoration-none">
                        View All <i class="fas fa-arrow-right ms-1"></i>
                    </a>
                </div>
            </div>
        </div>

        <div class="col-xl-3 col-md-6 mb-4">
            <div class="card stat-card bg-gradient-info shadow h-100">
                <div class="card-body">
                    <div class="stat-icon">
                        <i class="fas fa-edit"></i>
                    </div>
                    <div class="stat-number">${stats.correctionRequiredInstitutes || 0}</div>
                    <div class="stat-label">Correction Required</div>
                </div>
                <div class="card-footer">
                    <a href="/admin/institutes-list?status=CORRECTION_REQUIRED" class="text-white text-decoration-none">
                        Review <i class="fas fa-arrow-right ms-1"></i>
                    </a>
                </div>
            </div>
        </div>

        <div class="col-xl-3 col-md-6 mb-4">
            <div class="card stat-card bg-gradient-primary shadow h-100">
                <div class="card-body">
                    <div class="stat-icon">
                        <i class="fas fa-graduation-cap"></i>
                    </div>
                    <div class="stat-number">${stats.totalProgrammes || 0}</div>
                    <div class="stat-label">Total Programmes</div>
                </div>
                <div class="card-footer">
                    <a href="/admin/manage-programmes" class="text-white text-decoration-none">
                        Manage <i class="fas fa-arrow-right ms-1"></i>
                    </a>
                </div>
            </div>
        </div>

        <div class="col-xl-3 col-md-6 mb-4">
            <div class="card stat-card bg-gradient-success shadow h-100">
                <div class="card-body">
                    <div class="stat-icon">
                        <i class="fas fa-door-open"></i>
                    </div>
                    <div class="stat-number">${stats.activeAdmissionWindows || 0}</div>
                    <div class="stat-label">Active Admission Windows</div>
                </div>
                <div class="card-footer">
                    <a href="/admin/admission-management" class="text-white text-decoration-none">
                        View <i class="fas fa-arrow-right ms-1"></i>
                    </a>
                </div>
            </div>
        </div>

        <div class="col-xl-3 col-md-6 mb-4">
            <div class="card stat-card bg-gradient-secondary shadow h-100">
                <div class="card-body">
                    <div class="stat-icon">
                        <i class="fas fa-users"></i>
                    </div>
                    <div class="stat-number">${stats.totalUsers || 0}</div>
                    <div class="stat-label">System Users</div>
                </div>
                <div class="card-footer">
                    <a href="/user-management/list" class="text-white text-decoration-none">
                        Manage <i class="fas fa-arrow-right ms-1"></i>
                    </a>
                </div>
            </div>
        </div>
    `;

    const container = document.getElementById('stats-container');
    if (!container) return;

    container.replaceChildren();

    const temp = document.createElement("div");
    const parser = new DOMParser();
	const doc = parser.parseFromString(statsHtml, "text/html");
	
	Array.from(doc.body.childNodes).forEach(node => {
	    container.appendChild(node);
	});

    while (temp.firstChild) {
        container.appendChild(temp.firstChild);
    }
}

/**
 * Load recent activity from backend API
 */
function loadRecentActivity() {
    axios.get('/admin/api/recent-activity')
        .then(response => {
            const activities = response.data;
            renderRecentActivity(activities);
            $('#activity-count').text(activities.length);
        })
        .catch(error => {
            console.error('Failed to load recent activity:', error);
             const tbody = document.getElementById('activity-tbody');
			tbody.replaceChildren();
			
			const tr = document.createElement("tr");
			const td = document.createElement("td");
			
			td.colSpan = 4;
			td.className = "text-center text-danger py-3";
			
			const icon = document.createElement("i");
			icon.className = "fas fa-exclamation-triangle me-2";
			
			td.append(icon, document.createTextNode(" Failed to load recent activity"));
			tr.appendChild(td);
			tbody.appendChild(tr);
            $('#activity-count').text('0');
        });
}

/**
 * Render recent activity table rows
 */
function renderRecentActivity(activities) {
    if (activities.length === 0) {
        const tbody = document.getElementById('activity-tbody');
		tbody.replaceChildren();
		
		const tr = document.createElement("tr");
		const td = document.createElement("td");
		
		td.colSpan = 4;
		td.className = "text-center text-muted py-4";
		
		const icon = document.createElement("i");
		icon.className = "fas fa-inbox me-2";
		
		td.append(icon, document.createTextNode(" No recent activity"));
		tr.appendChild(td);
		tbody.appendChild(tr);
        return;
    }

    const tbody = document.getElementById('activity-tbody');
	tbody.replaceChildren();
	
	activities.forEach(activity => {
	    const tr = document.createElement("tr");
	
	    const td1 = document.createElement("td");
	    const small = document.createElement("small");
	    small.className = "text-muted";
	    small.textContent = formatDateTime(activity.time);
	    td1.appendChild(small);
	
	    const td2 = document.createElement("td");
	    td2.textContent = activity.user;
	
	    const td3 = document.createElement("td");
	    td3.textContent = activity.action;
	
	    const td4 = document.createElement("td");
	    const badge = document.createElement("span");
	    badge.className = `badge ${activity.statusClass}`;
	    badge.textContent = activity.status;
	
	    td4.appendChild(badge);
	
	    tr.append(td1, td2, td3, td4);
	    tbody.appendChild(tr);
	});
}

/**
 * Format date/time for display with relative time
 */
function formatDateTime(dateTime) {
    if (!dateTime) return 'Unknown';
    
    const date = new Date(dateTime);
    const now = new Date();
    const diffMs = now - date;
    const diffMins = Math.floor(diffMs / 60000);
    
    if (diffMins < 1) return 'Just now';
    if (diffMins < 60) return `${diffMins} min${diffMins > 1 ? 's' : ''} ago`;
    
    const diffHours = Math.floor(diffMins / 60);
    if (diffHours < 24) return `${diffHours} hour${diffHours > 1 ? 's' : ''} ago`;
    
    // For older dates, show formatted date
    return date.toLocaleString('en-IN', {
        day: '2-digit',
        month: 'short',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    });
}

/**
 * Update last refreshed timestamp
 */
function updateLastRefreshedTime() {
    const now = new Date();
    const timeStr = now.toLocaleTimeString('en-IN', {
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
    });
    $('#last-updated').text(`Last updated: ${timeStr}`);
}

/**
 * Escape HTML to prevent XSS attacks
 */
function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

/**
 * Refresh dashboard data manually
 */
function refreshDashboard() {
    $('#refresh-dashboard').find('i').addClass('fa-spin');
    
    loadDashboardStats();
    loadRecentActivity();
    
    setTimeout(() => {
        $('#refresh-dashboard').find('i').removeClass('fa-spin');
        if (typeof showToast !== 'undefined') {
            showToast('Dashboard refreshed successfully', 'success');
        }
    }, 1000);
}

// Initialize Dashboard - Only run on dashboard page
$(document).ready(function() {
    // Check if we're on the dashboard page
    if (window.location.pathname.includes('/admin/dashboard')) {
        console.log("Initializing admin dashboard...");
        
        // Initial load
        loadDashboardStats();
        loadRecentActivity();
        
        // Auto-refresh every 30 seconds
        dashboardRefreshInterval = setInterval(function() {
            console.log("Auto-refreshing dashboard data...");
            loadDashboardStats();
            loadRecentActivity();
        }, 30000); // 30 seconds
        
        // Manual refresh button
        $('#refresh-dashboard').on('click', function(e) {
            e.preventDefault();
            refreshDashboard();
        });
        
        // Clear interval when navigating away
        $(window).on('beforeunload', function() {
            if (dashboardRefreshInterval) {
                clearInterval(dashboardRefreshInterval);
                console.log("Dashboard auto-refresh cleared");
            }
        });
        
        console.log("Admin dashboard initialized successfully");
    }
});

// Export functions for potential external use
window.dashboardFunctions = {
    loadDashboardStats: loadDashboardStats,
    loadRecentActivity: loadRecentActivity,
    refreshDashboard: refreshDashboard
};

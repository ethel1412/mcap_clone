const GlobalNotifications = (() => {

    const getCsrfHeaders = () => {
        const token = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
        const header = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
        return (token && header) ? {
             [header]: token 
        } : {};
    };

    /*number of new notifications since last visit */
    function getLastVisitTime() {
        const stored = localStorage.getItem('notifications_last_visit');
        return stored ? new Date(stored) : null;
    }

    /*configuration mapping menu URLs to API endpoints */
    const notificationConfig = {
        // Seat Matrix Approvals (Controller)
        '/controller/seat-approvals': '/controller/seat-approvals/count-pending',
        '/seat-approvals': '/controller/seat-approvals/count-pending',

        // Programme Approvals (Controller)
        '/controller/approvals/programmes': '/programme-requests/admin/count-pending',
        '/programme-requests/admin': '/programme-requests/admin/count-pending',

        // Department Approvals (Controller)
        '/controller/approvals/departments': '/department-requests/controller/count-pending',

        // Institute Side - Programme Requests
        '/programmes-offered/page': '/programme-requests/my/count-pending',

        // Institute Approvals (Controller)
        '/admin/institutes-list': '/admin/count-pending-institutes',
        '/institutes-list': '/admin/count-pending-institutes',
        
        // Institute Schedule Notifications 
        '/institute-notifications': 'SPECIAL_NOTIFICATIONS',
        
        // My Departments (Institute)
        '/institute-departments/page/my': '/department-requests/my/count-pending'
    };

    /**
     * Initialize all sidebar badges
     */
    async function init() {
        const links = document.querySelectorAll('.sidebar-link-item');

        links.forEach(link => {
            const menuUrl = link.getAttribute('data-page-url');

            if (notificationConfig[menuUrl]) {
                const badge = link.querySelector('.notification-badge');
                
                if (badge) {
                    const apiUrl = notificationConfig[menuUrl];
                    
                    // Special handling for schedule notifications
                    if (apiUrl === 'SPECIAL_NOTIFICATIONS') {
                        fetchNotificationsCount(badge);
                    } else {
                        fetchCount(apiUrl, badge);
                    }
                }
            }
        });
    }

    /**
     * Special function for schedule notifications with localStorage tracking
     */
    async function fetchNotificationsCount(badgeElement) {
        try {
            const response = await axios.get('/api/notifications', { 
                headers: getCsrfHeaders() 
            });
            
            const notifications = response.data;
            const lastVisit = getLastVisitTime();
            
            let newCount = 0;
            
            if (!lastVisit) {
                // Never visited, show all notifications
                newCount = notifications.length;
            } else {
                // Count notifications created AFTER last visit
                newCount = notifications.filter(n => {
                    const notifDate = new Date(n.startDate);
                    return notifDate > lastVisit;
                }).length;
            }

            console.log(`[Notifications] Total: ${notifications.length}, New: ${newCount}, Last visit: ${lastVisit}`);

            if (newCount > 0) {
                badgeElement.textContent = newCount > 99 ? '99+' : newCount;
                badgeElement.style.display = 'inline-block';
                badgeElement.classList.add('animate__animated', 'animate__fadeIn');
            } else {
                badgeElement.style.display = 'none';
            }
        } catch (error) {
            if (error.response && error.response.status !== 403) {
                console.warn('[Notification] Failed to fetch notifications', error);
            }
        }
    }

    /**
     * Regular function for other badges (approvals, etc.)
     */
    async function fetchCount(apiUrl, badgeElement) {
        try {
            const response = await axios.get(apiUrl, { 
                headers: getCsrfHeaders() 
            });
            
            const count = Number(response.data);

            if (count > 0) {
                badgeElement.textContent = count > 99 ? '99+' : count;
                badgeElement.style.display = 'inline-block';
                badgeElement.classList.add('animate__animated', 'animate__fadeIn');
            } else {
                badgeElement.style.display = 'none';
            }
        } catch (error) {
            if (error.response && error.response.status !== 403) {
                console.warn(`[Notification] Failed to fetch ${apiUrl}`, error);
            }
        }
    }

    return { 
        init, 
        refresh: init 
    };
})();

// Initialize when sidebar is built
document.addEventListener('sidebarMenuBuilt', GlobalNotifications.init);

// Fallback initialization
setTimeout(() => {
    if (document.querySelectorAll('.sidebar-link-item').length > 0) {
        GlobalNotifications.init();
    }
}, 1000);

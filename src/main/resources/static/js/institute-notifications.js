/**
 * Institute Notifications System - For Sidebar Link
 */
const InstituteNotifications = (() => {

    let notifications = [];

    const getCsrfHeaders = () => {
        const token = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
        const header = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
        return (token && header) ? { [header]: token } : {};
    };

    function updateLastVisitTime() {
        localStorage.setItem('notifications_last_visit', new Date().toISOString());
    }

    function init() {
        if (document.getElementById('notificationTabsContent')) {
            updateLastVisitTime();
            initFullPage();
        }
    }

    function initFullPage() {
        loadNotificationsForPage();
    }

    async function loadNotificationsForPage() {
        try {
            const response = await axios.get('/api/notifications', {
                headers: getCsrfHeaders()
            });

            notifications = response.data;
            renderAllTabs();
            updateTabBadges();
        } catch (error) {
            console.error('[Notifications] Error loading page:', error);
            showPageError();
        }
    }

    function renderAllTabs() {
        const urgent = notifications.filter(n => n.status === 'ENDING_SOON');
        const active = notifications.filter(n => n.status === 'ACTIVE' || n.status === 'ENDING_SOON');
        const upcoming = notifications.filter(n => n.status === 'UPCOMING');

        renderTab('allNotificationsList', notifications);
        renderTab('urgentNotificationsList', urgent);
        renderTab('activeNotificationsList', active);
        renderTab('upcomingNotificationsList', upcoming);
    }

    function renderTab(containerId, list) {
        const container = document.getElementById(containerId);
        if (!container) return;

        container.replaceChildren();

        if (list.length === 0) {
            const template = document.getElementById('emptyStateTemplate');
            if (template) {
                container.appendChild(template.content.cloneNode(true));
            }
            return;
        }

        list.forEach(n => container.appendChild(createCardElement(n)));
    }

    function createCardElement(notif) {
        const statusClass = getStatusCardClass(notif.status);
        const icon = getStatusIcon(notif.status);
        const statusText = formatStatusText(notif.status);
        const startDate = formatDate(notif.startDate);
        const endDate = formatDate(notif.endDate);
        const daysText = getTimeRemainingText(notif.daysRemaining);
        const timeClass = notif.daysRemaining <= 3 ? 'critical' : '';

        const card = document.createElement("div");
        card.className = `notification-card ${statusClass}`;

        const flex = document.createElement("div");
        flex.className = "d-flex gap-3";

        const iconWrap = document.createElement("div");
        iconWrap.className = `card-icon ${statusClass}`;
        const iconEl = document.createElement("i");
        iconEl.className = `fa-solid ${icon}`;
        iconWrap.appendChild(iconEl);

        const content = document.createElement("div");
        content.className = "flex-grow-1";

        const header = document.createElement("div");
        header.className = "d-flex justify-content-between align-items-start mb-2";

        const title = document.createElement("h5");
        title.className = "mb-0";
        title.textContent = notif.stepName;

        const badge = document.createElement("span");
        badge.className = `status-badge ${statusClass}`;
        badge.textContent = statusText;

        header.append(title, badge);

        const desc = document.createElement("p");
        desc.className = "text-muted mb-0";
        desc.textContent = notif.description;

        const meta = document.createElement("div");
        meta.className = "meta-info";

        const start = document.createElement("div");
        start.className = "meta-item";
        const startIcon = document.createElement("i");
        startIcon.className = "fa-solid fa-calendar-day text-success";
        const startText = document.createElement("span");
        startText.textContent = `Start: ${startDate}`;
        start.append(startIcon, startText);

        const end = document.createElement("div");
        end.className = "meta-item";
        const endIcon = document.createElement("i");
        endIcon.className = "fa-solid fa-calendar-xmark text-danger";
        const endText = document.createElement("span");
        endText.textContent = `End: ${endDate}`;
        end.append(endIcon, endText);

        meta.append(start, end);

        const time = document.createElement("div");
        time.className = `time-badge ${timeClass}`;
        const timeIcon = document.createElement("i");
        timeIcon.className = "fa-solid fa-clock";
        const timeText = document.createElement("span");
        timeText.textContent = daysText;
        time.append(timeIcon, timeText);

        content.append(header, desc, meta, time);
        flex.append(iconWrap, content);
        card.appendChild(flex);

        return card;
    }

    function updateTabBadges() {
        const urgent = notifications.filter(n => n.status === 'ENDING_SOON').length;
        const active = notifications.filter(n => n.status === 'ACTIVE' || n.status === 'ENDING_SOON').length;
        const upcoming = notifications.filter(n => n.status === 'UPCOMING').length;

        setBadge('allCount', notifications.length);
        setBadge('urgentCount', urgent);
        setBadge('activeCount', active);
        setBadge('upcomingCount', upcoming);

        setBadge('statAllCount', notifications.length);
        setBadge('statUrgentCount', urgent);
        setBadge('statActiveCount', active);
        setBadge('statUpcomingCount', upcoming);
    }

    function showPageError() {
        ['allNotificationsList', 'urgentNotificationsList', 'activeNotificationsList', 'upcomingNotificationsList']
            .forEach(id => {
                const container = document.getElementById(id);
                if (!container) return;

                container.replaceChildren();

                const wrap = document.createElement("div");
                wrap.className = "text-center py-5";

                const icon = document.createElement("i");
                icon.className = "fa-solid fa-circle-exclamation fa-4x text-danger mb-3";

                const title = document.createElement("h5");
                title.textContent = "Failed to load notifications";

                const btn = document.createElement("button");
                btn.className = "btn btn-primary mt-3";

                const btnIcon = document.createElement("i");
                btnIcon.className = "fa-solid fa-arrows-rotate me-1";

                btn.append(btnIcon, document.createTextNode("Retry"));
                btn.addEventListener("click", () => location.reload());

                wrap.append(icon, title, btn);
                container.appendChild(wrap);
            });
    }

    function getStatusBadgeClass(status) {
        const map = { 'ENDING_SOON': 'danger', 'ACTIVE': 'success', 'UPCOMING': 'info' };
        return map[status] || 'secondary';
    }

    function getStatusCardClass(status) {
        const map = { 'ENDING_SOON': 'urgent', 'ACTIVE': 'active', 'UPCOMING': 'upcoming' };
        return map[status] || 'upcoming';
    }

    function getStatusIcon(status) {
        const map = {
            'ENDING_SOON': 'fa-triangle-exclamation',
            'ACTIVE': 'fa-circle-check',
            'UPCOMING': 'fa-clock'
        };
        return map[status] || 'fa-bell';
    }

    function formatStatusText(status) {
        return status.replace('_', ' ');
    }

    function getTimeRemainingText(days) {
        if (days < 0) return 'Expired';
        if (days === 0) return 'Ends Today!';
        if (days === 1) return '1 Day Remaining';
        return `${days} Days Remaining`;
    }

    function formatDate(dateString) {
        const date = new Date(dateString);
        return date.toLocaleString('en-IN', {
            day: '2-digit',
            month: 'short',
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit',
            hour12: true
        });
    }

    function setBadge(id, count) {
        const el = document.getElementById(id);
        if (el) el.textContent = count;
    }

    return { init };
})();

document.addEventListener('DOMContentLoaded', () => {
    InstituteNotifications.init();
});
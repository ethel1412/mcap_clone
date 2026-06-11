-- System Security - Users, Roles, Menus, and Permissions
-- Generated from data.sql
-- Date: 2024-09-23

-- INSERT THE ROLES
INSERT INTO mcap.role (role_id, role_name) VALUES
                                               ('4', 'ADMIN'),
                                               ('2', 'INST_ADMIN'),
                                               ('3','INSTITUTE'),
                                               ('1', 'CONTROLLER'),
                                               ('5', 'APPLICANT');

-- Set menu count - Resets the sequence for the menu_id to ensure proper auto-incrementing
SELECT setval(
               pg_get_serial_sequence('mcap.menu','menu_id'),
               COALESCE((SELECT MAX(menu_id) FROM mcap.menu),0) + 1,
               false
       );

-- MENU - Top-Level Items (no parent_menu_id)
INSERT INTO mcap.menu (menu_id, menu_name, icon_class, order_index, is_active, parent_menu_id) VALUES
 (1, 'Dashboard', 'fas fa-tachometer-alt', 10, TRUE, NULL),
(2, 'Institute Registration', 'fas fa-university', 20, TRUE, NULL),
(7, 'Schedule Management', 'fas solid fa-calendar-days', 50, TRUE, NULL),
(10, 'Admission Management', 'fas fa-user-graduate', 30, TRUE, NULL),
(20, 'System Administration', 'fas fa-cogs', 40, TRUE, NULL),
(24, 'Institute Management', NULL, 60, TRUE, NULL),
(28, 'Controller Dashboard', 'fas fa-tachometer-alt', 1, TRUE, NULL),
(29, 'Manage Admissions', NULL, 100, TRUE, NULL),
(36, 'Notifications', 'fas fa-bell', 1, TRUE, NULL),
(40, 'Allotment Verification', 'fas fa-person-check-fill', 11, TRUE, 10);

-- MENU - "Schedule Management" Children (parent_menu_id = 7)
INSERT INTO mcap.menu (menu_id, menu_name, icon_class, order_index, is_active, parent_menu_id) VALUES
(8, 'Prepare Schedule', 'fas fa-plus', 2, TRUE, 7),
(9, 'View Schedules', 'fas fa-calendar-days', 3, TRUE, 7),
(30, 'Create Schedule', 'fas fa-layer-group', 100, TRUE, NULL);

-- MENU - "Admission Process" Children (parent_menu_id = 10)
INSERT INTO mcap.menu (menu_id, menu_name, icon_class, order_index, is_active, parent_menu_id) VALUES
(11, 'Manage Windows', 'fas fa-calendar-alt', 1, TRUE, 10),
(12, 'Manage Seats', 'fas fa-book', 2, TRUE, 10),
(13, 'View Applications', 'fas fa-folder-open', 8, TRUE, 10),
(14, 'Admission Process', 'fas fa-trophy', 7, TRUE, 10),
 (16, 'Admission Reports', 'fas fa-chart-pie', 9, TRUE, 10),
 (31, 'Registration Fee ', 'fa fa-credit-card', 5, FALSE, 10),
 (32, 'Programme Approvals', 'fas fa-check-double', 3, TRUE, 10),
 (33, 'Seat Matrix Approvals', 'fas fa-chair', 6, TRUE, 10),
 (34, 'Department Approvals', 'fas fa-building', 2, TRUE, 10),
 (35, 'Eligibility Rules', 'fas fa-clipboard-check', 4, TRUE, 10);

-- MENU - "System Administration" Children (parent_menu_id = 20)
INSERT INTO mcap.menu (menu_id, menu_name, icon_class, order_index, is_active, parent_menu_id) VALUES
(21, 'User Management', 'fas fa-users', 1, TRUE, 20),
(22, 'Role Management', 'fas fa-user-shield', 2, TRUE, 20),
(23, 'Access Management', 'fas fa-key', 3, TRUE, 20);

-- MENU - "Institute Management" Children (parent_menu_id = 24)
INSERT INTO mcap.menu (menu_id, menu_name, icon_class, order_index, is_active, parent_menu_id) VALUES
(26, 'Departments', NULL, 2, TRUE, 24),
(27, 'Programme', NULL, 3, TRUE, 24);

INSERT INTO mcap.menu 
(menu_id, menu_name, icon_class, order_index, is_active, parent_menu_id) 
VALUES 
(50, 'Account', 'fas fa-user-cog', 6, TRUE, null);

INSERT INTO mcap.menu 
(menu_id, menu_name, icon_class, order_index, is_active, parent_menu_id) 
VALUES
(51, 'Profile', 'fas fa-user', 1, TRUE, 50),
(52, 'Change Password', 'fas fa-key', 2, TRUE, 50);


INSERT INTO mcap.page_url 
(url_code, is_public, method, page_url, menu_id) 
VALUES
(60, FALSE, 'GET', '/institute/profile', 51),
(61, FALSE, 'GET', '/profile/change-password', 52);

-- Account + submenus for INSTITUTE
INSERT INTO mcap.role_menu (role_id, menu_id) VALUES
('3', 50),  -- Account
('3', 51),  -- Profile
('3', 52);  -- Change Password

-- (15, 'Seat Allocation', 'fas fa-chair', 5, TRUE, 24);

-- PAGE URLs - Mapping menu items to their respective URLs
INSERT INTO mcap.page_url (url_code, is_public, method, page_url, menu_id) VALUES
(1, FALSE, 'GET', '/admin/dashboard', 1),
(2, FALSE, 'GET', '/admin/institutes-list', 2),
(7, FALSE, 'GET', '/schedule-management/create', 8),
(8, FALSE, 'GET', '/schedule-management/list', 9),
(11, FALSE, 'GET', '/admin/admission-management', 11),
(12, FALSE, 'GET', '/admission-window/institute', 12),
(13, FALSE, 'GET', '/institute/view-applications', 13),
(14, FALSE, 'GET', '/admission-criteria/page/select-window', 14),
-- (15, FALSE, 'GET', '/seat-matrix/page/home', 15),
(16, FALSE, 'GET', '/admin/reports', 16),
(21, FALSE, 'GET', '/user-management/dashboard', 21),
(22, FALSE, 'GET', '/role-management/dashboard', 22),
(23, FALSE, 'GET', '/role-management/assign-access', 23),
(25, FALSE, 'GET', '/institute-departments/page/my', 26),
(26, FALSE, 'GET', '/programmes-offered/page', 27),
(27, FALSE, 'GET', '/control-panel/dashboard',28),
(28, FALSE, 'GET', '/schedule-management/step-templates',30),
(29,FALSE,'GET','/institute-registration-fee',31),
(32, FALSE, 'GET', '/controller/approvals/programmes', 32),
(33, FALSE, 'GET', '/controller/seat-approvals', 33),
(34, FALSE, 'GET', '/controller/approvals/departments', 34),
(35, FALSE, 'GET', '/admin/eligibility/configure', 35),
(36, FALSE, 'GET', '/institute-notifications', 36),
(40, FALSE, 'GET', '/institute/verification-dashboard', 40);

-- ROLE-MENU MAPPING - Grant 'ADMIN' (role_id = 1) access to all menus
INSERT INTO mcap.role_menu (role_id, menu_id) VALUES
('4', 1),
('1', 2),
('1', 7),
('1', 8),
('1', 9),
('1', 10),
('1', 11),
-- ('1', 13),
('1', 14),
-- ('1', 15),
('1', 16),
('1', 32),
-- ('1', 33),
('1', 34),
('1', 35);

-- ROLE-MENU MAPPING - Grant 'INST_ADMIN' (role_id = 2) access to Dashboard and Admission Process section
INSERT INTO mcap.role_menu (role_id, menu_id) VALUES
('2', 1),
('2', 10),
('2', 12),
('2', 13),
-- ('2', 14),
-- ('2', 15),
('2', 16),
('2', 24),
('2', 36),
('2', 40);

-- ROLE-MENU MAPPING - Grant 'INSTITUTE' (role_id = 3) access to specific menus
INSERT INTO mcap.role_menu (role_id, menu_id) VALUES
('3', 1),
('3', 10),
('3', 12),
('3', 13),
-- ('3', 14),
-- ('3', 15),
('3', 16),
('3', 24),
('3', 26),
('3', 27),
('3', 31),
('3', 36),
('3', 40);

INSERT INTO mcap.role_menu (role_id, menu_id) VALUES
('1', 28),
('4', 20),
('4', 21),
('4', 22),
('4', 23),
('4', 30);

-- USERS
-- INSERT INTO mcap.user (
--     user_id,
--     user_code,
--     username,
--     account_non_expired,
--     account_non_locked,
--     credentials_non_expired,
--     date_joined,
--     enabled,
--     is_superuser,
--     password,
--     org_owner_type,
--     role_id,
--     password_change_required
-- ) VALUES (
--              1,
--              '8d88548d-a2d7-4a83-ad26-a1aee5f05231',
--              'admin1',
--              TRUE,
--              TRUE,
--              TRUE,
--              '2024-04-09 14:28:14.845',
--              TRUE,
--              TRUE,
--              '$2a$10$CX2Fw/AfE1SHQfptUVVB/OrBOVtv0h8E87XDzPCbQ3zTEXywrJEuu',
--              'GOVT_DEPARTMENT',
--              '1',
--              FALSE
--          );
INSERT INTO mcap.user (
    account_non_expired, account_non_locked, credentials_non_expired, enabled, is_superuser, org_owner_id,
    password_change_required, user_id, date_joined, user_code, org_owner_type, password, role_id, temp_plaintext_password, username
) VALUES
    (TRUE, TRUE, TRUE, TRUE, TRUE, NULL, FALSE, 1, '2024-04-09 14:28:14.845', '8d88548d-a2d7-4a83-ad26-a1aee5f05231', 'GOVT_DEPARTMENT', '$2a$10$CX2Fw/AfE1SHQfptUVVB/OrBOVtv0h8E87XDzPCbQ3zTEXywrJEuu', '4 ', NULL, 'admin2'),
    (TRUE, TRUE, TRUE, TRUE, FALSE, NULL, FALSE, 2, '2025-09-23 13:20:19.934835', '2e5bca97-638d-464d-87eb-fccaabeef332', 'INSTITUTE', '$2a$10$acgM0X18wIpe77YEqaP1qexkdiY35UizF0nRyeo5LKhXoYnhXzqsK', '1 ', NULL, 'controller2');

-- Reset user sequence
SELECT setval('mcap.user_user_id_seq', (SELECT MAX(user_id) FROM mcap.user));

-- Reset menu sequence
SELECT setval(
               pg_get_serial_sequence('mcap.menu','menu_id'),
               COALESCE((SELECT MAX(menu_id) FROM mcap.menu),0) + 1,
               false
       );
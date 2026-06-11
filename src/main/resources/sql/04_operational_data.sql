-- Operational Data - Affiliation types, Management types, and operational setup
-- Generated from data.sql
-- Date: 2024-09-23

-- DROP TABLES

DROP TABLE IF EXISTS mcap.login_activities CASCADE;
DROP TABLE IF EXISTS mcap.programmes CASCADE;
DROP TABLE IF EXISTS mcap.programmes_offered CASCADE;
DROP TABLE IF EXISTS mcap.blocks CASCADE;


-- RESET SEQUENCES
SELECT setval(pg_get_serial_sequence('mcap.address', 'address_id'), COALESCE((SELECT MAX(address_id) FROM mcap.address), 0) + 1, false);
SELECT setval(pg_get_serial_sequence('mcap.institute', 'institute_id'), COALESCE((SELECT MAX(institute_id) FROM mcap.institute), 0) + 1, false);
SELECT setval(pg_get_serial_sequence('mcap.department', 'department_id'), COALESCE((SELECT MAX(department_id) FROM mcap.department), 0) + 1, false);
SELECT setval(pg_get_serial_sequence('mcap.admission_window', 'admission_id'), COALESCE((SELECT MAX(admission_id) FROM mcap.admission_window), 0) + 1, false);
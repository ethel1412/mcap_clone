-- ===============================
-- 1. Admission window (if needed)
-- ===============================
INSERT INTO mcap.admissionwindow
    (admission_id, is_active, stream_id, end_date, schedule_id, start_date, programme_level, session)
VALUES
    (
        1,                               -- admissionid
        TRUE,                            -- isactive
        101,                             -- streamid for UG Science
        TIMESTAMP '2026-01-30 23:59:00', -- enddate
        NULL,                            -- scheduleid
        TIMESTAMP '2025-11-27 00:00:00', -- startdate
        'UG',                            -- programmelevel
        '2025-2026'                      -- session
    )
ON CONFLICT (admissionid) DO NOTHING;    -- avoid duplicate if it already exists

-----------------------------------------------------------------------
---MAKE SURE PROGRAMME_ID AND PROGRAMME_OFFERED_ID EXIST IN DATABASE___
-----------------------------------------------------------------------

INSERT INTO mcap.applicant (
    applicant_id, applicant_no,
    first_name, middle_name, last_name,
    country_phone_code, phone_number,
    date_of_birth, email,
    category_code
) VALUES
('5e8d9f4b-7c5b-4e0f-8e5a-8a0e5cba8b69','APPLMESC202500100','Test1','', 'Student','+91','9876105532','2006-07-16','test1@example.com','GEN'),
('0a2f9b01-1c2a-4b70-bd84-8a6c2b3a4a27','APPLMESC202500101','Test2','', 'Student','+91','9876522078','2006-09-20','test2@example.com','OBC'),
('c0e9e2d8-5a6c-4a42-a2b7-8e5f6c0a9b0b','APPLMESC202500003','Test3','', 'Student','+91','9876377700','2006-03-01','test3@example.com','SC'),
('e3f0b4e3-9d66-4e32-8d11-b4f0cbb5c8da','APPLMESC202500004','Test4','', 'Student','+91','9876173840','2006-11-17','test4@example.com','ST'),
('f5c1b0a8-4d3a-4c2d-9b0c-7e2f3c4b5d6e','APPLMESC202500005','Test5','', 'Student','+91','9876641498','2006-01-24','test5@example.com','GEN'),
('b7e9d6c5-1a2b-4c3d-9e0f-1a2b3c4d5e6f','APPLMESC202500006','Test6','', 'Student','+91','9876678719','2006-05-02','test6@example.com','OBC'),
('d8f1e2c3-b4a5-4d6e-8f9a-0b1c2d3e4f5a','APPLMESC202500007','Test7','', 'Student','+91','9876699322','2006-02-11','test7@example.com','SC'),
('a1b2c3d4-e5f6-4a7b-8c9d-0e1f2a3b4c5d','APPLMESC202500008','Test8','', 'Student','+91','9876543980','2006-08-25','test8@example.com','ST'),
('19b7e33d-9d6b-4f8b-9d62-ddef121af8b0','APPLMESC202500009','Test9','', 'Student','+91','9876620932','2006-10-05','test9@example.com','GEN'),
('8b35f2b2-5bfe-4fe7-b83f-ad3cb38febb8','APPLMESC202500010','Test10','', 'Student','+91','9876570071','2006-06-03','test10@example.com','OBC'),
('c8b1b6a0-8c56-4d5a-9b56-a5f6f0e3c4c8','APPLMESC202500011','Test11','', 'Student','+91','9876298771','2006-04-12','test11@example.com','SC'),
('0c2d4e6f-8091-4b33-a099-9f46a2e4c6a1','APPLMESC202500012','Test12','', 'Student','+91','9876680651','2006-09-09','test12@example.com','ST'),
('1e3f5a7b-9c2d-4e6f-8a0b-1c2d3e4f5a6b','APPLMESC202500013','Test13','', 'Student','+91','9876269551','2006-02-23','test13@example.com','GEN'),
('2f4a6c8e-0b1c-4d5e-9f0a-1b2c3d4e5f6a','APPLMESC202500014','Test14','', 'Student','+91','9876701922','2006-12-14','test14@example.com','OBC'),
('3a5c7e9f-1b2c-4d3e-8f0a-2b3c4d5e6f7a','APPLMESC202500015','Test15','', 'Student','+91','9876712870','2006-07-08','test15@example.com','SC'),
('4b6d8f0a-2c3d-4e5f-9a0b-3c4d5e6f7a8b','APPLMESC202500016','Test16','', 'Student','+91','9876118844','2006-01-05','test16@example.com','ST'),
('5c7e9f1b-3d4e-4f5a-8b0c-4d5e6f7a8b9c','APPLMESC202500017','Test17','', 'Student','+91','9876809688','2006-03-18','test17@example.com','GEN'),
('6d8f0a2c-4e5f-4a6b-9c0d-5e6f7a8b9c0d','APPLMESC202500018','Test18','', 'Student','+91','9876143137','2006-05-27','test18@example.com','OBC'),
('7e9f1b3d-5f6a-4b7c-8d0e-6f7a8b9c0d1e','APPLMESC202500019','Test19','', 'Student','+91','9876368990','2006-10-21','test19@example.com','SC'),
('8f0a2c4e-6a7b-4c8d-9e0f-7a8b9c0d1e2f','APPLMESC202500020','Test20','', 'Student','+91','9876145163','2006-08-02','test20@example.com','ST');

INSERT INTO mcap.application (
    application_id,
    applicant_id,
    admission_id,
    application_no,
    applicant_type,
    application_status,
    academic_details_complete,
    personal_details_complete,
    programme_selection_complete,
    payment_complete,
    is_documents_finalized,
    application_date
) VALUES
-- 1–10: WITH_ENTRANCE
(1001,'5e8d9f4b-7c5b-4e0f-8e5a-8a0e5cba8b69',1,'SCIE202520260001','WITH_ENTRANCE','COMPLETE',true,true,true,true,true,now()),
(1002,'0a2f9b01-1c2a-4b70-bd84-8a6c2b3a4a27',1,'SCIE202520260002','WITH_ENTRANCE','COMPLETE',true,true,true,true,true,now()),
(1003,'c0e9e2d8-5a6c-4a42-a2b7-8e5f6c0a9b0b',1,'SCIE202520260003','WITH_ENTRANCE','COMPLETE',true,true,true,true,true,now()),
(1004,'e3f0b4e3-9d66-4e32-8d11-b4f0cbb5c8da',1,'SCIE202520260004','WITH_ENTRANCE','COMPLETE',true,true,true,true,true,now()),
(1005,'f5c1b0a8-4d3a-4c2d-9b0c-7e2f3c4b5d6e',1,'SCIE202520260005','WITH_ENTRANCE','COMPLETE',true,true,true,true,true,now()),
(1006,'b7e9d6c5-1a2b-4c3d-9e0f-1a2b3c4d5e6f',1,'SCIE202520260006','WITH_ENTRANCE','COMPLETE',true,true,true,true,true,now()),
(1007,'d8f1e2c3-b4a5-4d6e-8f9a-0b1c2d3e4f5a',1,'SCIE202520260007','WITH_ENTRANCE','COMPLETE',true,true,true,true,true,now()),
(1008,'a1b2c3d4-e5f6-4a7b-8c9d-0e1f2a3b4c5d',1,'SCIE202520260008','WITH_ENTRANCE','COMPLETE',true,true,true,true,true,now()),
(1009,'19b7e33d-9d6b-4f8b-9d62-ddef121af8b0',1,'SCIE202520260009','WITH_ENTRANCE','COMPLETE',true,true,true,true,true,now()),
(1010,'8b35f2b2-5bfe-4fe7-b83f-ad3cb38febb8',1,'SCIE202520260010','WITH_ENTRANCE','COMPLETE',true,true,true,true,true,now()),
-- 11–20: WITHOUT_ENTRANCE
(1011,'c8b1b6a0-8c56-4d5a-9b56-a5f6f0e3c4c8',1,'SCIE202520260011','WITHOUT_ENTRANCE','COMPLETE',true,true,true,true,true,now()),
(1012,'0c2d4e6f-8091-4b33-a099-9f46a2e4c6a1',1,'SCIE202520260012','WITHOUT_ENTRANCE','COMPLETE',true,true,true,true,true,now()),
(1013,'1e3f5a7b-9c2d-4e6f-8a0b-1c2d3e4f5a6b',1,'SCIE202520260013','WITHOUT_ENTRANCE','COMPLETE',true,true,true,true,true,now()),
(1014,'2f4a6c8e-0b1c-4d5e-9f0a-1b2c3d4e5f6a',1,'SCIE202520260014','WITHOUT_ENTRANCE','COMPLETE',true,true,true,true,true,now()),
(1015,'3a5c7e9f-1b2c-4d3e-8f0a-2b3c4d5e6f7a',1,'SCIE202520260015','WITHOUT_ENTRANCE','COMPLETE',true,true,true,true,true,now()),
(1016,'4b6d8f0a-2c3d-4e5f-9a0b-3c4d5e6f7a8b',1,'SCIE202520260016','WITHOUT_ENTRANCE','COMPLETE',true,true,true,true,true,now()),
(1017,'5c7e9f1b-3d4e-4f5a-8b0c-4d5e6f7a8b9c',1,'SCIE202520260017','WITHOUT_ENTRANCE','COMPLETE',true,true,true,true,true,now()),
(1018,'6d8f0a2c-4e5f-4a6b-9c0d-5e6f7a8b9c0d',1,'SCIE202520260018','WITHOUT_ENTRANCE','COMPLETE',true,true,true,true,true,now()),
(1019,'7e9f1b3d-5f6a-4b7c-8d0e-6f7a8b9c0d1e',1,'SCIE202520260019','WITHOUT_ENTRANCE','COMPLETE',true,true,true,true,true,now()),
(1020,'8f0a2c4e-6a7b-4c8d-9e0f-7a8b9c0d1e2f',1,'SCIE202520260020','WITHOUT_ENTRANCE','COMPLETE',true,true,true,true,true,now());

INSERT INTO mcap.academic_record (
    id, applicant_id, stream_id,
    qualification_level, percentage,
    latest_qualification, date_of_passing,
    board_or_university, school_or_college
) VALUES
(2001,'5e8d9f4b-7c5b-4e0f-8e5a-8a0e5cba8b69',101,'Class XII or Equivalent (Science)',88.50,true,'2024-03-31','CBSE','School 1'),
(2002,'0a2f9b01-1c2a-4b70-bd84-8a6c2b3a4a27',101,'Class XII or Equivalent (Science)',92.00,true,'2024-03-31','CBSE','School 2'),
(2003,'c0e9e2d8-5a6c-4a42-a2b7-8e5f6c0a9b0b',101,'Class XII or Equivalent (Science)',75.30,true,'2024-03-31','CBSE','School 3'),
(2004,'e3f0b4e3-9d66-4e32-8d11-b4f0cbb5c8da',101,'Class XII or Equivalent (Science)',81.75,true,'2024-03-31','CBSE','School 4'),
(2005,'f5c1b0a8-4d3a-4c2d-9b0c-7e2f3c4b5d6e',101,'Class XII or Equivalent (Science)',69.40,true,'2024-03-31','CBSE','School 5'),
(2006,'b7e9d6c5-1a2b-4c3d-9e0f-1a2b3c4d5e6f',101,'Class XII or Equivalent (Science)',95.10,true,'2024-03-31','CBSE','School 6'),
(2007,'d8f1e2c3-b4a5-4d6e-8f9a-0b1c2d3e4f5a',101,'Class XII or Equivalent (Science)',78.25,true,'2024-03-31','CBSE','School 7'),
(2008,'a1b2c3d4-e5f6-4a7b-8c9d-0e1f2a3b4c5d',101,'Class XII or Equivalent (Science)',84.90,true,'2024-03-31','CBSE','School 8'),
(2009,'19b7e33d-9d6b-4f8b-9d62-ddef121af8b0',101,'Class XII or Equivalent (Science)',90.60,true,'2024-03-31','CBSE','School 9'),
(2010,'8b35f2b2-5bfe-4fe7-b83f-ad3cb38febb8',101,'Class XII or Equivalent (Science)',73.15,true,'2024-03-31','CBSE','School 10'),
(2011,'c8b1b6a0-8c56-4d5a-9b56-a5f6f0e3c4c8',101,'Class XII or Equivalent (Science)',82.30,true,'2024-03-31','CBSE','School 11'),
(2012,'0c2d4e6f-8091-4b33-a099-9f46a2e4c6a1',101,'Class XII or Equivalent (Science)',67.80,true,'2024-03-31','CBSE','School 12'),
(2013,'1e3f5a7b-9c2d-4e6f-8a0b-1c2d3e4f5a6b',101,'Class XII or Equivalent (Science)',96.20,true,'2024-03-31','CBSE','School 13'),
(2014,'2f4a6c8e-0b1c-4d5e-9f0a-1b2c3d4e5f6a',101,'Class XII or Equivalent (Science)',71.45,true,'2024-03-31','CBSE','School 14'),
(2015,'3a5c7e9f-1b2c-4d3e-8f0a-2b3c4d5e6f7a',101,'Class XII or Equivalent (Science)',88.95,true,'2024-03-31','CBSE','School 15'),
(2016,'4b6d8f0a-2c3d-4e5f-9a0b-3c4d5e6f7a8b',101,'Class XII or Equivalent (Science)',63.50,true,'2024-03-31','CBSE','School 16'),
(2017,'5c7e9f1b-3d4e-4f5a-8b0c-4d5e6f7a8b9c',101,'Class XII or Equivalent (Science)',92.80,true,'2024-03-31','CBSE','School 17'),
(2018,'6d8f0a2c-4e5f-4a6b-9c0d-5e6f7a8b9c0d',101,'Class XII or Equivalent (Science)',85.10,true,'2024-03-31','CBSE','School 18'),
(2019,'7e9f1b3d-5f6a-4b7c-8d0e-6f7a8b9c0d1e',101,'Class XII or Equivalent (Science)',79.75,true,'2024-03-31','CBSE','School 19'),
(2020,'8f0a2c4e-6a7b-4c8d-9e0f-7a8b9c0d1e2f',101,'Class XII or Equivalent (Science)',94.30,true,'2024-03-31','CBSE','School 20');

INSERT INTO mcap.cuet_score (
    id, applicant_id, overall_percentile, year_of_exam, application_number
) VALUES
(3001,'5e8d9f4b-7c5b-4e0f-8e5a-8a0e5cba8b69',96.50,2025,'CUET2025-0001'),
(3002,'0a2f9b01-1c2a-4b70-bd84-8a6c2b3a4a27',78.40,2025,'CUET2025-0002'),
(3003,'c0e9e2d8-5a6c-4a42-a2b7-8e5f6c0a9b0b',88.20,2025,'CUET2025-0003'),
(3004,'e3f0b4e3-9d66-4e32-8d11-b4f0cbb5c8da',67.10,2025,'CUET2025-0004'),
(3005,'f5c1b0a8-4d3a-4c2d-9b0c-7e2f3c4b5d6e',91.75,2025,'CUET2025-0005'),
(3006,'b7e9d6c5-1a2b-4c3d-9e0f-1a2b3c4d5e6f',55.30,2025,'CUET2025-0006'),
(3007,'d8f1e2c3-b4a5-4d6e-8f9a-0b1c2d3e4f5a',82.65,2025,'CUET2025-0007'),
(3008,'a1b2c3d4-e5f6-4a7b-8c9d-0e1f2a3b4c5d',73.90,2025,'CUET2025-0008'),
(3009,'19b7e33d-9d6b-4f8b-9d62-ddef121af8b0',89.45,2025,'CUET2025-0009'),
(3010,'8b35f2b2-5bfe-4fe7-b83f-ad3cb38febb8',60.25,2025,'CUET2025-0010');


INSERT INTO mcap.applicant_programme_preference (
    id, application_id, programme_id, institute_id, preference_order, is_active
) VALUES
(5001,1001,3,1,1,true),
(5002,1002,3,1,1,true),
(5003,1003,3,1,1,true),
(5004,1004,3,1,1,true),
(5005,1005,3,1,1,true),
(5006,1006,3,1,1,true),
(5007,1007,3,1,1,true),
(5008,1008,3,1,1,true),
(5009,1009,3,1,1,true),
(5010,1010,3,1,1,true),
(5011,1011,3,1,1,true),
(5012,1012,3,1,1,true),
(5013,1013,3,1,1,true),
(5014,1014,3,1,1,true),
(5015,1015,3,1,1,true),
(5016,1016,3,1,1,true),
(5017,1017,3,1,1,true),
(5018,1018,3,1,1,true),
(5019,1019,3,1,1,true),
(5020,1020,3,1,1,true);

INSERT INTO mcap.eligibility_result (
    eligibility_result_id,
    is_eligible,
    programme_offered_id,
    application_id,
    calculated_at,
    rejection_reason
) VALUES
(4001,true,3,1001,now(),NULL),
(4002,true,3,1002,now(),NULL),
(4003,true,3,1003,now(),NULL),
(4004,true,3,1004,now(),NULL),
(4005,true,3,1005,now(),NULL),
(4006,true,3,1006,now(),NULL),
(4007,true,3,1007,now(),NULL),
(4008,true,3,1008,now(),NULL),
(4009,true,3,1009,now(),NULL),
(4010,true,3,1010,now(),NULL),
(4011,true,3,1011,now(),NULL),
(4012,true,3,1012,now(),NULL),
(4013,true,3,1013,now(),NULL),
(4014,true,3,1014,now(),NULL),
(4015,true,3,1015,now(),NULL),
(4016,true,3,1016,now(),NULL),
(4017,true,3,1017,now(),NULL),
(4018,true,3,1018,now(),NULL),
(4019,true,3,1019,now(),NULL),
(4020,true,3,1020,now(),NULL);
-- Academic Structure - Streams, Programmes, Departments, and Subjects
-- Generated from data.sql
-- Date: 2024-09-23

--Stream

INSERT INTO mcap.stream (stream_id, stream_name) VALUES
                                                     (101, 'Science'),
                                                     (102, 'Commerce'),
                                                     (103, 'Arts/Humanities'),
                                                     (104, 'Vocational/Technical'),
                                                     (105, 'Engineering & Technology'),
                                                     (106, 'Management'),
                                                     (107, 'Fine Arts');


--Programme


-- Science Stream (stream_id 101)
INSERT INTO mcap.programme (programme_name, programme_level, stream_id) VALUES
                                                                   ('B.Sc. Physics', 'UG', 101),
                                                                   ('B.Sc. Chemistry', 'UG', 101),
                                                                   ('B.Sc. Mathematics', 'UG', 101),
                                                                   ('B.Sc. Biology', 'UG', 101),
                                                                   ('B.Sc. Biotechnology', 'UG', 101),
                                                                   ('B.Pharm.', 'UG', 101),
                                                                   ('B.Sc. Nursing', 'UG', 101),
                                                                   ('M.Sc. Physics', 'PG', 101),
                                                                   ('M.Sc. Chemistry', 'PG', 101),
                                                                   ('M.Sc. Mathematics', 'PG', 101),
                                                                   ('M.Sc. Biotechnology', 'PG', 101),
                                                                   ('PhD in Physics', 'PHD', 101),
                                                                   ('PhD in Chemistry', 'PHD', 101);


-- Commerce Stream (stream_id 102)
INSERT INTO mcap.programme (programme_name, programme_level, stream_id) VALUES
                                                                   ('B.Com.', 'UG', 102),
                                                                   ('B.Com. (Honors)', 'UG', 102),
                                                                   ('BBA', 'UG', 102),
                                                                   ('M.Com.', 'PG', 102),
                                                                   ('PhD in Commerce', 'PHD', 102);


-- Arts/Humanities Stream (stream_id 103)
INSERT INTO mcap.programme (programme_name, programme_level, stream_id) VALUES
                                                                   ('B.A. English', 'UG', 103),
                                                                   ('B.A. History', 'UG', 103),
                                                                   ('B.A. Political Science', 'UG', 103),
                                                                   ('B.A. Sociology', 'UG', 103),
                                                                   ('Bachelor of Fine Arts (BFA)', 'UG', 103),
                                                                   ('M.A. Political Science', 'PG', 103),
                                                                   ('M.A. Sociology', 'PG', 103),
                                                                   ('Master of Fine Arts (MFA)', 'PG', 103),
                                                                   ('PhD in History', 'PHD', 103),
                                                                   ('PhD in Fine Arts', 'PHD', 103);


-- Vocational/Technical Stream (stream_id 104)
INSERT INTO mcap.programme (programme_name, programme_level, stream_id) VALUES
                                                                   ('Diploma in Computer Applications', 'DIPLOMA', 104),
                                                                   ('BCA', 'UG', 104),
                                                                   ('MCA', 'PG', 104),
                                                                   ('Diploma in Mechanical Engineering', 'DIPLOMA', 104),
                                                                   ('Diploma in Electrical Engineering', 'DIPLOMA', 104);


-- Engineering & Technology Stream (stream_id 105)
INSERT INTO mcap.programme (programme_name, programme_level, stream_id) VALUES
                                                                   ('Diploma in Civil Engineering', 'DIPLOMA', 105),
                                                                   ('Diploma in Mechanical Engineering', 'DIPLOMA', 105),
                                                                   ('B.Tech. Computer Science', 'UG', 105),
                                                                   ('B.Tech. Mechanical Engineering', 'UG', 105),
                                                                   ('B.Tech. Civil Engineering', 'UG', 105),
                                                                   ('B.Tech. Electrical Engineering', 'UG', 105),
                                                                   ('B.Tech. Electronics and Communication Engineering', 'UG', 105),
                                                                   ('B.Tech. Artificial Intelligence and Machine Learning', 'UG', 105),
                                                                   ('M.Tech. Data Science', 'PG', 105),
                                                                   ('M.Tech. Mechanical Engineering', 'PG', 105),
                                                                   ('PhD in Mechanical Engineering', 'PHD', 105);


-- Management Stream (stream_id 106)
INSERT INTO mcap.programme (programme_name, programme_level, stream_id) VALUES
                                                                   ('BBA', 'UG', 106),
                                                                   ('MBA', 'PG', 106),
                                                                   ('PhD in Management', 'PHD', 106);


-- Fine Arts Stream (stream_id 107)
INSERT INTO mcap.programme (programme_name, programme_level, stream_id) VALUES
                                                                   ('Bachelor of Fine Arts', 'UG', 107),
                                                                   ('Master of Fine Arts', 'PG', 107),
                                                                   ('PhD in Fine Arts', 'PHD', 107),
                                                                   ('Diploma in Graphic Design', 'DIPLOMA', 107),
                                                                   ('Diploma in Photography', 'DIPLOMA', 107);


-- Insert Data into Department Table
INSERT INTO mcap.department (department_name) VALUES
                                                  ('Physics Department'),
                                                  ('Chemistry Department'),
                                                  ('Mathematics Department'),
                                                  ('Biology Department'),
                                                  ('Biotechnology Department'),
                                                  ('Pharmaceutical Sciences Department'),
                                                  ('Nursing Department'),
                                                  ('Commerce Department'),
                                                  ('Accountancy Department'),
                                                  ('English Department'),
                                                  ('History Department'),
                                                  ('Political Science Department'),
                                                  ('Sociology Department'),
                                                  ('Fine Arts Department'),
                                                  ('Graphic Design Department'),
                                                  ('Photography Department'),
                                                  ('Computer Applications Department'),
                                                  ('Mechanical Engineering Department'),
                                                  ('Electrical Engineering Department'),
                                                  ('Civil Engineering Department'),
                                                  ('Computer Science Engineering Department'),
                                                  ('Data Science Department'),
                                                  ('Artificial Intelligence Department'),
                                                  ('Management Department'),
                                                  ('Digital Marketing Department');


-- Physics
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Physics', 'PHY101');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Applied Physics', 'PHY102');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Nuclear Physics', 'PHY201');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Quantum Physics', 'PHY301');


-- Chemistry
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Chemistry', 'CHE101');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Organic Chemistry', 'CHE201');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Inorganic Chemistry', 'CHE202');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Physical Chemistry', 'CHE203');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Analytical Chemistry', 'CHE301');


-- Biology
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Biology', 'BIO101');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Botany', 'BOT101');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Zoology', 'ZOO101');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Microbiology', 'MIC101');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Biochemistry', 'BIC201');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Genetics', 'GEN201');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Molecular Biology', 'MOL301');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Cell Biology', 'CEL201');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Ecology', 'ECO201');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Biotechnology', 'BIT301');


-- Mathematics
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Mathematics', 'MAT101');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Calculus', 'MAT201');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Linear Algebra', 'MAT202');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Differential Equations', 'MAT301');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Statistics', 'STA101');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Probability Theory', 'STA201');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Discrete Mathematics', 'MAT302');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Number Theory', 'MAT401');

INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Computer Science', 'CSE101');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Programming in C', 'CSE102');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Programming in Java', 'CSE201');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Programming in Python', 'CSE202');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Data Structures', 'CSE203');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Algorithms', 'CSE301');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Database Management Systems', 'CSE302');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Computer Networks', 'CSE303');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Operating Systems', 'CSE304');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Software Engineering', 'CSE305');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Web Development', 'CSE306');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Mobile App Development', 'CSE307');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Artificial Intelligence', 'CSE401');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Machine Learning', 'CSE402');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Data Mining', 'CSE403');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Cyber Security', 'CSE404');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Digital Image Processing', 'CSE405');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Computer Graphics', 'CSE406');


-- Mechanical Engineering
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Engineering Mechanics', 'MEC101');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Thermodynamics', 'MEC201');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Fluid Mechanics', 'MEC202');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Heat Transfer', 'MEC301');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Machine Design', 'MEC302');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Manufacturing Processes', 'MEC303');


-- Electrical Engineering
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Circuit Theory', 'EEE101');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Digital Electronics', 'EEE201');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Analog Electronics', 'EEE202');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Power Systems', 'EEE301');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Control Systems', 'EEE302');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Microprocessors', 'EEE303');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Signal Processing', 'EEE401');


-- Civil Engineering
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Structural Engineering', 'CIV201');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Geotechnical Engineering', 'CIV202');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Transportation Engineering', 'CIV301');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Environmental Engineering', 'CIV302');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Water Resources Engineering', 'CIV303');

INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('English Literature', 'ENG101');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('English Language', 'ENG102');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Creative Writing', 'ENG201');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Linguistics', 'ENG301');

INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('History', 'HIS101');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Ancient History', 'HIS201');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Modern History', 'HIS202');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('World History', 'HIS301');

INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Philosophy', 'PHI101');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Ethics', 'PHI201');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Logic', 'PHI202');

INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Psychology', 'PSY101');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Social Psychology', 'PSY201');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Cognitive Psychology', 'PSY301');

INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Sociology', 'SOC101');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Anthropology', 'ANT101');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Political Science', 'POL101');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('International Relations', 'POL201');

INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Accounting', 'ACC101');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Financial Accounting', 'ACC201');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Management Accounting', 'ACC202');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Cost Accounting', 'ACC301');

INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Business Studies', 'BUS101');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Business Management', 'BUS201');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Human Resource Management', 'BUS301');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Marketing Management', 'BUS302');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Financial Management', 'BUS303');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Operations Management', 'BUS304');

INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Economics', 'ECO101');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Microeconomics', 'ECO201');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Macroeconomics', 'ECO202');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('International Economics', 'ECO301');

INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Banking', 'BNK101');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Insurance', 'INS101');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Taxation', 'TAX101');

INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Hindi', 'HIN101');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Sanskrit', 'SAN101');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Bengali', 'BEN101');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Tamil', 'TAM101');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Telugu', 'TEL101');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Marathi', 'MAR101');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Gujarati', 'GUJ101');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Punjabi', 'PUN101');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Assamese', 'ASS101');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Oriya', 'ORI101');

INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('French', 'FRE101');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('German', 'GER101');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Spanish', 'SPA101');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Chinese', 'CHI101');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Japanese', 'JAP101');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Arabic', 'ARA101');

INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Anatomy', 'ANA101');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Physiology', 'PHY101');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Pathology', 'PAT201');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Pharmacology', 'PHA201');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Medicine', 'MED301');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Surgery', 'SUR301');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Pediatrics', 'PED301');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Gynecology', 'GYN301');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Orthopedics', 'ORT301');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Cardiology', 'CAR401');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Dermatology', 'DER401');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Psychiatry', 'PSY401');

INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Nursing', 'NUR101');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Pharmacy', 'PHA101');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Physiotherapy', 'PHT101');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Medical Laboratory Technology', 'MLT101');

INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Drawing', 'ART101');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Painting', 'ART102');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Sculpture', 'ART201');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Graphics Design', 'ART301');

INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Music Theory', 'MUS101');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Vocal Music', 'MUS102');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Instrumental Music', 'MUS201');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Classical Dance', 'DAN101');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Folk Dance', 'DAN102');

INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Mass Communication', 'MAS101');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Journalism', 'JOU201');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Film Studies', 'FIL201');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Photography', 'PHO101');

INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Environmental Studies', 'ENV101');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Computer Applications', 'CMP101');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('General Knowledge', 'GEN101');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Current Affairs', 'CUR101');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Reasoning', 'REA101');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Quantitative Aptitude', 'QUA101');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Verbal Ability', 'VER101');
INSERT INTO mcap.subject (subject_name, subject_code) VALUES ('Logical Reasoning', 'LOG101');




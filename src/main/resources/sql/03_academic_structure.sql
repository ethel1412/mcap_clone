-- Academic Structure - Streams, Programmes, Departments, and Subjects
-- Generated from data.sql
-- Date: 2024-09-23

--Stream

INSERT INTO mcap.stream (stream_id, stream_name) VALUES
(101, 'Arts / Humanities'),
(102, 'Science'),
(103, 'Commerce'),
(104, 'Engineering & Technology'),
(106, 'ALL STREAMS'),
(105, 'Computer Applications');


-- Departments

INSERT INTO mcap.department (department_name, department_code) VALUES
('History Department', 'HIS'),
('Political Science Department', 'PSC'),
('Sociology Department', 'SOC'),
('Philosophy Department', 'PHI'),
('Psychology Department', 'PSY'),
('Economics Department', 'ECO'),
('English Department', 'ENG'),
('Hindi Department', 'HIN'),
('Khasi Department', 'KHA'),
('Geography Department', 'GEO'),
('Public Administration Department', 'PAD'),
('Social Work Department', 'SWK'),
('Physics Department', 'PHY'),
('Chemistry Department', 'CHE'),
('Mathematics Department', 'MAT'),
('Botany Department', 'BOT'),
('Zoology Department', 'ZOO'),
('Statistics Department', 'STA'),
('Computer Department', 'COD'),
('Biotechnology Department', 'BIO'),
('Microbiology Department', 'MIC'),
('Environmental Science Department', 'ENV'),
('Geology Department', 'GLY'),
('Commerce Department', 'COM'),
('Physiology Department', 'PHYD'),
('Biochemistry Department', 'BCH'),
('Education Department', 'EDU'),
('Home Science Department', 'HSD');

--Programme

INSERT INTO mcap.programme (programme_name, programme_level, stream_id, department_id) VALUES

-- ================= ARTS / HUMANITIES (101) =================
('Major in History','FYUG',101,(SELECT department_id FROM mcap.department WHERE department_code='HIS')),
('Major in Political Science','FYUG',101,(SELECT department_id FROM mcap.department WHERE department_code='PSC')),
('Major in Sociology','FYUG',101,(SELECT department_id FROM mcap.department WHERE department_code='SOC')),
('Major in Philosophy','FYUG',101,(SELECT department_id FROM mcap.department WHERE department_code='PHI')),
('Major in Psychology','FYUG',101,(SELECT department_id FROM mcap.department WHERE department_code='PSY')),
('Major in Economics','FYUG',101,(SELECT department_id FROM mcap.department WHERE department_code='ECO')),
('Major in English','FYUG',101,(SELECT department_id FROM mcap.department WHERE department_code='ENG')),
('Major in Hindi','FYUG',101,(SELECT department_id FROM mcap.department WHERE department_code='HIN')),
('Major in Khasi','FYUG',101,(SELECT department_id FROM mcap.department WHERE department_code='KHA')),

-- ================= SCIENCE (102) =================
('Major in Physics','FYUG',102,(SELECT department_id FROM mcap.department WHERE department_code='PHY')),
('Major in Chemistry','FYUG',102,(SELECT department_id FROM mcap.department WHERE department_code='CHE')),
('Major in Mathematics','FYUG',102,(SELECT department_id FROM mcap.department WHERE department_code='MAT')),
('Major in Botany','FYUG',102,(SELECT department_id FROM mcap.department WHERE department_code='BOT')),
('Major in Zoology','FYUG',102,(SELECT department_id FROM mcap.department WHERE department_code='ZOO')),
('Major in Statistics','FYUG',102,(SELECT department_id FROM mcap.department WHERE department_code='STA')),
('Major in Computer Science','FYUG',102,(SELECT department_id FROM mcap.department WHERE department_code='COD')),
('Major in Biotechnology','FYUG',102,(SELECT department_id FROM mcap.department WHERE department_code='BIO')),
('Major in Microbiology','FYUG',102,(SELECT department_id FROM mcap.department WHERE department_code='MIC')),
('Major in Environmental Science','FYUG',102,(SELECT department_id FROM mcap.department WHERE department_code='ENV')),
('Major in Geology','FYUG',102,(SELECT department_id FROM mcap.department WHERE department_code='GLY')),

-- ================= COMMERCE (103) =================
('Major in Commerce','FYUG',103,(SELECT department_id FROM mcap.department WHERE department_code='COM')),
('Major in Accounting & Finance','FYUG',103,(SELECT department_id FROM mcap.department WHERE department_code='COM')),
('Major in Business Economics','FYUG',103,(SELECT department_id FROM mcap.department WHERE department_code='COM')),
('Major in Taxation','FYUG',103,(SELECT department_id FROM mcap.department WHERE department_code='COM')),
('Major in Banking & Insurance','FYUG',103,(SELECT department_id FROM mcap.department WHERE department_code='COM')),
('Major in Business Analytics','FYUG',103,(SELECT department_id FROM mcap.department WHERE department_code='COM')),

-- ================= EDUCATION (101) =================
('Major in Education','FYUG',101,(SELECT department_id FROM mcap.department WHERE department_code='EDU')),

-- ================= COMPUTER APPLICATIONS =================
('Major in Computer Applications)','FYUG',105,(SELECT department_id FROM mcap.department WHERE department_code='COD')),

-- ================= DATA / STATISTICS =================
('Major in Statistics','FYUG',102,(SELECT department_id FROM mcap.department WHERE department_code='STA'));

INSERT INTO mcap.programme (programme_name, programme_level, stream_id, department_id) VALUES

-- Geography
('Major in Geography','FYUG',101,(SELECT department_id FROM mcap.department WHERE department_code='GEO')),

-- Public Administration
('Major in Public Administration','FYUG',101,(SELECT department_id FROM mcap.department WHERE department_code='PAD')),

-- Social Work
('Major in Social Work','FYUG',101,(SELECT department_id FROM mcap.department WHERE department_code='SWK')),

-- Physiology (Science)
('Major in Physiology','FYUG',102,(SELECT department_id FROM mcap.department WHERE department_code='PHYD')),

-- Biochemistry (Science)
('Major in Biochemistry','FYUG',102,(SELECT department_id FROM mcap.department WHERE department_code='BCH')),

-- Home Science
('Major in Home Science','FYUG',102,(SELECT department_id FROM mcap.department WHERE department_code='HSD'));
                                                 
-- Class XII
-- =========================
-- LANGUAGES
-- =========================
INSERT INTO mcap.subject (subject_name, subject_code, subject_type) VALUES
('English Core', 'GEN-ENG', 'GENERAL'),
('English Elective', 'GEN-ENG-E', 'GENERAL'),
('Hindi Core', 'GEN-HIN', 'GENERAL'),
('Hindi Elective', 'GEN-HIN-E', 'GENERAL'),
('Sanskrit Core', 'GEN-SAN', 'GENERAL'),
('Sanskrit Elective', 'GEN-SAN-E', 'GENERAL'),
('Bengali', 'GEN-BEN', 'GENERAL'),
('Assamese', 'GEN-ASM', 'GENERAL'),
('Gujarati', 'GEN-GUJ', 'GENERAL'),
('Punjabi', 'GEN-PUN', 'GENERAL'),
('Tamil', 'GEN-TAM', 'GENERAL'),
('Telugu', 'GEN-TEL', 'GENERAL'),
('Malayalam', 'GEN-MAL', 'GENERAL'),
('Kannada', 'GEN-KAN', 'GENERAL'),
('Odia', 'GEN-ODI', 'GENERAL'),
('Urdu', 'GEN-URD', 'GENERAL');

-- =========================
-- CORE / ACADEMIC SUBJECTS
-- =========================
INSERT INTO mcap.subject (subject_name, subject_code, subject_type) VALUES
('Physics', 'GEN-PHY', 'GENERAL'),
('Chemistry', 'GEN-CHE', 'GENERAL'),
('Mathematics', 'GEN-MAT', 'GENERAL'),
('Biology', 'GEN-BIO', 'GENERAL'),
('Accountancy', 'GEN-ACC', 'GENERAL'),
('Business Studies', 'GEN-BST', 'GENERAL'),
('Economics', 'GEN-ECO', 'GENERAL'),
('History', 'GEN-HIS', 'GENERAL'),
('Geography', 'GEN-GEO', 'GENERAL'),
('Political Science', 'GEN-POL', 'GENERAL'),
('Sociology', 'GEN-SOC', 'GENERAL'),
('Psychology', 'GEN-PSY', 'GENERAL'),
('Legal Studies', 'GEN-LAW', 'GENERAL'),
('Philosophy', 'GEN-PHI', 'GENERAL');

-- =========================
-- APPLIED / OPTIONAL
-- =========================
INSERT INTO mcap.subject (subject_name, subject_code, subject_type) VALUES
('Computer Science', 'GEN-CS', 'GENERAL'),
('Informatics Practices', 'GEN-IP', 'GENERAL'),
('Biotechnology', 'GEN-BT', 'GENERAL'),
('Engineering Graphics', 'GEN-EG', 'GENERAL'),
('Entrepreneurship', 'GEN-ENT', 'GENERAL'),
('Home Science', 'GEN-HSC', 'GENERAL'),
('Physical Education', 'GEN-PE', 'GENERAL'),
('Fine Arts', 'GEN-ART', 'GENERAL'),
('Applied Mathematics', 'GEN-AMAT', 'GENERAL');

-- =========================
-- SKILL SUBJECTS (IMPORTANT ONES)
-- =========================
INSERT INTO mcap.subject (subject_name, subject_code, subject_type) VALUES
('Information Technology', 'GEN-IT', 'GENERAL'),
('Retail', 'GEN-RET', 'GENERAL'),
('Banking', 'GEN-BNK', 'GENERAL'),
('Marketing', 'GEN-MKT', 'GENERAL'),
('Tourism', 'GEN-TOUR', 'GENERAL'),
('Mass Media Studies', 'GEN-MMS', 'GENERAL'),
('Fashion Studies', 'GEN-FASH', 'GENERAL'),
('Food Production', 'GEN-FOOD', 'GENERAL'),
('Automotive', 'GEN-AUTO', 'GENERAL'),
('Healthcare', 'GEN-HLTH', 'GENERAL'),

-- MINOR Courses

-- ECONOMICS
('Economics', 'MIN-ECO', 'MINOR'),

-- EDUCATION
('Education', 'MIN-EDU', 'MINOR'),

-- ENGLISH
('English', 'MIN-ENG', 'MINOR'),

-- GARO
('Garo', 'MIN-GARO', 'MINOR'),

-- HISTORY
('History', 'MIN-HIS', 'MINOR'),

-- PHILOSOPHY
('Philosophy', 'MIN-PHI', 'MINOR'),

-- Political Science
('Political Science', 'MIN-POL', 'MINOR'),

-- ENGLISH
('English', 'MIN-ENG', 'MINOR'),

-- EDUCATION
('Education', 'MIN-EDU', 'MINOR');

-- MDC Courses
INSERT INTO mcap.subject (subject_name, subject_code, subject_type) VALUES
('Culture and Society', 'MDC_111', 'MDC'),
('Fundamentals of Computer Systems', 'MDC_112', 'MDC'),
('Fundamental of Lifelong Learning', 'MDC_113', 'MDC'),
('Introductory Life Sciences', 'MDC_114', 'MDC'),
('Mathematics in Daily Life', 'MDC_115', 'MDC'),
('Introduction to Indian Constitution', 'MDC_116', 'MDC');


-- SEC Courses
INSERT INTO mcap.subject (subject_name, subject_code, subject_type) VALUES
('Motivation', 'SEC_131', 'SEC'),
('Public Speaking', 'SEC_133', 'SEC'),
('Team Building', 'SEC_134', 'SEC'),
('Life Skills and Education', 'SEC_135', 'SEC');

-- AEC Courses
INSERT INTO mcap.subject (subject_name, subject_code, subject_type) VALUES
('Alternative English', 'AEC_120', 'AEC'),
('MIL-I', 'AEC_121', 'AEC'),
('Communicative English - I','AEC_122','AEC'),
('Ka Kylla Ktien bad Ka Literature Khasi','AEC_123','AEC');


-- VAC Courses
INSERT INTO mcap.subject (subject_name, subject_code, subject_type) VALUES
('Environment Studies', 'VAC_140', 'VAC');


INSERT INTO mcap.qualification (name, level, is_active) VALUES
-- SCHOOL LEVEL
('Class IX', 'SCHOOL', true),
('Class VII', 'SCHOOL', true),
('Class VIII', 'SCHOOL', true),
('Class X/MATRICULATION/ICSE or Equivalent', 'SCHOOL', true),
('Class XII or Equivalent (Arts)', 'SCHOOL', true),
('Class XII or Equivalent (Commerce)', 'SCHOOL', true),
('Class XII or Equivalent (Science)', 'SCHOOL', true),
('CLASS XII VOCATIONAL', 'SCHOOL', true),

-- DIPLOMA LEVEL
('Diploma Ayush Nursing and Pharmacy', 'DIPLOMA', true),
('Diploma In Ophthalmic Technician', 'DIPLOMA', true),
('Diploma course in Couple and Family therapy', 'DIPLOMA', true),
('Diploma in Anaesthesiology (DA)', 'DIPLOMA', true),
('Diploma in Banking Administration (D.B.A)', 'DIPLOMA', true),
('Diploma in Blood Transfusion', 'DIPLOMA', true),
('Diploma in Child Health (DCH)', 'DIPLOMA', true),
('Diploma in Cinematography', 'DIPLOMA', true),
('Diploma in Clinical Pathology (DCP)', 'DIPLOMA', true),
('Diploma in Coaching', 'DIPLOMA', true),
('Diploma in Community Medicine/Public Health', 'DIPLOMA', true),
('Diploma in Computer Applications', 'DIPLOMA', true),
('Diploma in Counselling Psychology', 'DIPLOMA', true),
('Diploma in Dance', 'DIPLOMA', true),
('Diploma in Dental Hygiene', 'DIPLOMA', true),
('Diploma in Dental Mechanic/Dental Technician', 'DIPLOMA', true),
('Diploma in Dermatology, Venerology & Leprosy (DDVL)', 'DIPLOMA', true),
('Diploma in Diabetology (D.Diab.)', 'DIPLOMA', true),
('Diploma in Dialysis technologists', 'DIPLOMA', true),
('Diploma in Draftsmanship', 'DIPLOMA', true),
('Diploma in ECG Technology', 'DIPLOMA', true),
('Diploma in Education', 'DIPLOMA', true),
('Diploma in Education (D.B.Ed)', 'DIPLOMA', true),
('Diploma in Electro Encephalograph Technology', 'DIPLOMA', true),
('Diploma in Elementary Education [D.El.Ed]', 'DIPLOMA', true),
('Diploma in Engineering/Technology', 'DIPLOMA', true),
('Diploma in Finance and Accounts', 'DIPLOMA', true),
('Diploma in Fire And Safety Engineering', 'DIPLOMA', true),
('Diploma in Forensic Medicine (D.F.M.)', 'DIPLOMA', true),
('Diploma in Gynecology and Obstetrics', 'DIPLOMA', true),
('Diploma in Haemodialysis Technology', 'DIPLOMA', true),
('Diploma in Health Administration (D.H.A.)', 'DIPLOMA', true),
('Diploma in Hemodialysis Technology', 'DIPLOMA', true),
('Diploma in Hindi Translation', 'DIPLOMA', true),
('Diploma in Hospital Administration', 'DIPLOMA', true),
('Diploma in Hotel & Tourism Management', 'DIPLOMA', true),
('Diploma in Laryngology and Otology (DLO)', 'DIPLOMA', true),
('Diploma in Medical Laboratory Technology', 'DIPLOMA', true),
('Diploma in Medical Imaging Technology', 'DIPLOMA', true),
('Diploma in Medical Physics', 'DIPLOMA', true),
('Diploma in Medical Radio Diagnosis (DMRD)', 'DIPLOMA', true),
('Diploma in Medical Radio Therapy (DMRT)', 'DIPLOMA', true),
('Diploma in Medical Record Technology', 'DIPLOMA', true),
('Diploma in Microbiology (D. Micro)', 'DIPLOMA', true),
('Diploma in Multipurpose Health Worker', 'DIPLOMA', true),
('Diploma in Music', 'DIPLOMA', true),
('Diploma in Obstetrics and Gynaecology (DGO)', 'DIPLOMA', true),
('Diploma in Occupational Health (D. O.H.)', 'DIPLOMA', true),
('Diploma in Operation Theatre Technology', 'DIPLOMA', true),
('Diploma in Ophthalmology', 'DIPLOMA', true),
('Diploma in Ophthalmology (DO)', 'DIPLOMA', true),
('Diploma in Orthopaedics (D:Orth.)', 'DIPLOMA', true),
('Diploma in Oto Rhino Laryngology (DLO)', 'DIPLOMA', true),
('Diploma in Pharmacy', 'DIPLOMA', true),
('Diploma in Photography', 'DIPLOMA', true),
('Diploma in Physical Education (D.P.Ed )', 'DIPLOMA', true),
('Diploma in Physical Medicine & Rehabilitation (D.Phy.Med.)', 'DIPLOMA', true),
('Diploma in Primary Education (D.P.E)', 'DIPLOMA', true),
('Diploma in Printing Technologies', 'DIPLOMA', true),
('Diploma in Printing Technology', 'DIPLOMA', true),
('Diploma in Prosthetics & Orthotics', 'DIPLOMA', true),
('Diploma in Psychiatry', 'DIPLOMA', true),
('Diploma in Psychological Medicine (DPM)', 'DIPLOMA', true),
('Diploma in Public Health (DPH)', 'DIPLOMA', true),
('Diploma in Public Health and Sanitation', 'DIPLOMA', true),
('Diploma in Radio Imaging Technology', 'DIPLOMA', true),
('Diploma in Rural Development', 'DIPLOMA', true),
('Diploma in Special Education', 'DIPLOMA', true),
('Diploma in Special Education (D.S.E)', 'DIPLOMA', true),
('Diploma in Sports Coaching', 'DIPLOMA', true),
('Diploma in Sports Management', 'DIPLOMA', true),
('Diploma in Teacher Education (D.T.Ed)', 'DIPLOMA', true),
('Diploma in Technical Education (D.T.E)', 'DIPLOMA', true),
('Diploma in Theology', 'DIPLOMA', true),
('Diploma in Trauma, Emergency and Disaster Management', 'DIPLOMA', true),
('Diploma in Tuberculosis & Chest Diseases (DTCD)', 'DIPLOMA', true),
('Diploma in X Ray And Electrocardiography Technology', 'DIPLOMA', true),
('Diplomate of National Board (DNB)', 'DIPLOMA', true),
('Diplomate of National Board (DNB).', 'DIPLOMA', true);



-- ============================================================
-- CUET Paper Master Seed (UG + PG) - idempotent (PostgreSQL)
-- Target: mcap.cuet_paper
-- Columns: id,is_active,paper_code,paper_name,programme_level,sort_order,spec,domain_name
-- ============================================================

INSERT INTO mcap.cuet_paper
(is_active, paper_code, paper_name, programme_level, sort_order, spec, domain_name)
VALUES

-- =========================
-- UG : SECTION I (LANGUAGE)
-- =========================
(true,'101','English','FYUG',  1,'LANGUAGE','LANGUAGE'),
(true,'102','Hindi','FYUG',    2,'LANGUAGE','LANGUAGE'),
(true,'103','Assamese','FYUG', 3,'LANGUAGE','LANGUAGE'),
(true,'104','Bengali','FYUG',  4,'LANGUAGE','LANGUAGE'),
(true,'105','Gujarati','FYUG', 5,'LANGUAGE','LANGUAGE'),
(true,'106','Kannada','FYUG',  6,'LANGUAGE','LANGUAGE'),
(true,'107','Malayalam','FYUG',7,'LANGUAGE','LANGUAGE'),
(true,'108','Marathi','FYUG',  8,'LANGUAGE','LANGUAGE'),
(true,'109','Odia','FYUG',     9,'LANGUAGE','LANGUAGE'),
(true,'110','Punjabi','FYUG', 10,'LANGUAGE','LANGUAGE'),
(true,'111','Tamil','FYUG',   11,'LANGUAGE','LANGUAGE'),
(true,'112','Telugu','FYUG',  12,'LANGUAGE','LANGUAGE'),
(true,'113','Urdu','FYUG',    13,'LANGUAGE','LANGUAGE'),

-- =========================
-- UG : SECTION II (DOMAIN)
-- =========================
(true,'301','Accountancy / Book-Keeping','FYUG', 14,'DOMAIN','DOMAIN'),
(true,'302','Agriculture','FYUG',               15,'DOMAIN','DOMAIN'),
(true,'303','Anthropology','FYUG',              16,'DOMAIN','DOMAIN'),
(true,'304','Biology/ Biological Science/ Biotechnology /Biochemistry','FYUG', 17,'DOMAIN','DOMAIN'),
(true,'305','Business Studies','FYUG',          18,'DOMAIN','DOMAIN'),
(true,'306','Chemistry','FYUG',                 19,'DOMAIN','DOMAIN'),
(true,'307','Environmental Science','FYUG',     20,'DOMAIN','DOMAIN'),
(true,'308','Computer Science / Information Practices','FYUG', 21,'DOMAIN','DOMAIN'),
(true,'309','Economics / Business Economics','FYUG', 22,'DOMAIN','DOMAIN'),
(true,'312','Fine Arts/Visual Arts/Commercial Arts','FYUG',    23,'DOMAIN','DOMAIN'),
(true,'313','Geography / Geology','FYUG',       24,'DOMAIN','DOMAIN'),
(true,'314','History','FYUG',                   25,'DOMAIN','DOMAIN'),
(true,'315','Home Science','FYUG',              26,'DOMAIN','DOMAIN'),
(true,'316','Knowledge Tradition-Practices in India','FYUG',   27,'DOMAIN','DOMAIN'),
(true,'318','Mass Media / Mass Communication','FYUG',          28,'DOMAIN','DOMAIN'),
(true,'319','Mathematics / Applied Mathematics','FYUG',        29,'DOMAIN','DOMAIN'),
(true,'320','Performing Arts - (Dance, Drama and Music)','FYUG',30,'DOMAIN','DOMAIN'),
(true,'321','Physical Education (Yoga, Sports)','FYUG',        31,'DOMAIN','DOMAIN'),
(true,'322','Physics','FYUG',                   32,'DOMAIN','DOMAIN'),
(true,'323','Political Science','FYUG',         33,'DOMAIN','DOMAIN'),
(true,'324','Psychology','FYUG',                34,'DOMAIN','DOMAIN'),
(true,'325','Sanskrit','FYUG',                  35,'DOMAIN','DOMAIN'),
(true,'326','Sociology','FYUG',                 36,'DOMAIN','DOMAIN'),

-- =========================
-- UG : SECTION III (APTITUDE)
-- =========================
(true,'501','General Aptitude Test','FYUG',     37,'APTITUDE','APTITUDE'),

-- ============================================================
-- PG : COMMON (COQP*) -> spec COMMON, domain_name COMMON
-- ============================================================
(true,'COQP01','Agribusiness Management','PG',  1,'COMMON','COMMON'),
(true,'COQP02','Applied Geography and Geoinformatics','PG',  2,'COMMON','COMMON'),
(true,'COQP03','B.Ed.','PG',  3,'COMMON','COMMON'),
(true,'COQP04','B.Ed. Humanities and Social Science','PG',  4,'COMMON','COMMON'),
(true,'COQP05','B.Ed. Languages','PG',  5,'COMMON','COMMON'),
(true,'COQP06','B.Ed. Science','PG',  6,'COMMON','COMMON'),
(true,'COQP07','B.Ed. Mathematics','PG',  7,'COMMON','COMMON'),
(true,'COQP08','Commerce','PG',  8,'COMMON','COMMON'),
(true,'COQP09','Disaster Studies','PG',  9,'COMMON','COMMON'),
(true,'COQP10','Economics','PG', 10,'COMMON','COMMON'),
(true,'COQP11','General Paper','PG', 11,'COMMON','COMMON'),
(true,'COQP12','General Paper (Metc.)','PG', 12,'COMMON','COMMON'),
(true,'COQP13','Library & Information Science','PG', 13,'COMMON','COMMON'),
(true,'COQP14','Law (LLM)','PG', 14,'COMMON','COMMON'),
(true,'COQP15','Master of Education','PG', 15,'COMMON','COMMON'),
(true,'COQP16','M.A. Education','PG', 16,'COMMON','COMMON'),
(true,'COQP17','Mass Communication and Journalism','PG', 17,'COMMON','COMMON'),
(true,'COQP18','Physical Education','PG', 18,'COMMON','COMMON'),
(true,'COQP19','Public Health','PG', 19,'COMMON','COMMON'),
(true,'COQP20','Sports Physiology etc.','PG', 20,'COMMON','COMMON'),
(true,'COQP21','Yoga','PG', 21,'COMMON','COMMON'),
(true,'COQP22','Hospital Management','PG', 22,'COMMON','COMMON'),

-- ============================================================
-- PG : SCIENCE (SCQP*) -> spec DOMAIN, domain_name SCIENCE
-- ============================================================
(true,'SCQP01','Agricultural Science (SCQP01)','PG', 101,'DOMAIN','SCIENCE'),
(true,'SCQP02','Agro-forestry (SCQP02)','PG', 102,'DOMAIN','SCIENCE'),
(true,'SCQP03','Applied Microbiology (SCQP03)','PG', 103,'DOMAIN','SCIENCE'),
(true,'SCQP04','Architecture and Planning (SCQP04)','PG', 104,'DOMAIN','SCIENCE'),
(true,'SCQP05','Biochemistry (SCQP05)','PG', 105,'DOMAIN','SCIENCE'),
(true,'SCQP06','Bio-Informatics (SCQP06)','PG', 106,'DOMAIN','SCIENCE'),
(true,'SCQP07','Botany (SCQP07)','PG', 107,'DOMAIN','SCIENCE'),
(true,'SCQP08','Chemistry (SCQP08)','PG', 108,'DOMAIN','SCIENCE'),
(true,'SCQP09','Computer Science and Information Technology (SCQP09)','PG', 109,'DOMAIN','SCIENCE'),
(true,'SCQP10','Criminology (SCQP10)','PG', 110,'DOMAIN','SCIENCE'),
(true,'SCQP11','Environmental Science (SCQP11)','PG', 111,'DOMAIN','SCIENCE'),
(true,'SCQP12','Food Science and Technology (SCQP12)','PG', 112,'DOMAIN','SCIENCE'),
(true,'SCQP13','Forensic Science (SCQP13)','PG', 113,'DOMAIN','SCIENCE'),
(true,'SCQP14','Geology, Earth Science (SCQP14)','PG', 114,'DOMAIN','SCIENCE'),
(true,'SCQP15','Geophysics (SCQP15)','PG', 115,'DOMAIN','SCIENCE'),
(true,'SCQP16','Horticulture (SCQP16)','PG', 116,'DOMAIN','SCIENCE'),
(true,'SCQP17','Life Sciences (SCQP17)','PG', 117,'DOMAIN','SCIENCE'),
(true,'SCQP18','Material Science (SCQP18)','PG', 118,'DOMAIN','SCIENCE'),
(true,'SCQP19','Mathematics (SCQP19)','PG', 119,'DOMAIN','SCIENCE'),
(true,'SCQP20','Medical Laboratory Technology (SCQP20)','PG', 120,'DOMAIN','SCIENCE'),
(true,'SCQP21','MPT_Master in Respiratory Theory (MRT) (SCQP21)','PG', 121,'DOMAIN','SCIENCE'),
(true,'SCQP22','Nanoscience Integrative Biosciences (SCQP22)','PG', 122,'DOMAIN','SCIENCE'),
(true,'SCQP23','Pharmacy (SCQP23)','PG', 123,'DOMAIN','SCIENCE'),
(true,'SCQP24','Physics (SCQP24)','PG', 124,'DOMAIN','SCIENCE'),
(true,'SCQP25','Plant Biotechnology (SCQP25)','PG', 125,'DOMAIN','SCIENCE'),
(true,'SCQP26','Soil Science – Soil & Water Conservation (SCQP26)','PG', 126,'DOMAIN','SCIENCE'),
(true,'SCQP27','Statistics (SCQP27)','PG', 127,'DOMAIN','SCIENCE'),
(true,'SCQP28','Zoology (SCQP28)','PG', 128,'DOMAIN','SCIENCE'),
(true,'SCQP29','Atmospheric Science (SCQP29)','PG', 129,'DOMAIN','SCIENCE'),
(true,'SCQP30','Animal Science (Poultry) (SCQP30)','PG', 130,'DOMAIN','SCIENCE'),

-- ============================================================
-- PG : M.TECH (MTQP*) -> spec DOMAIN, domain_name M_TECH
-- ============================================================
(true,'MTQP01','Chemical, Thermal and Polymer Engineering (MTQP01)','PG', 201,'DOMAIN','M_TECH'),
(true,'MTQP02','Civil, Structural and Transport Engineering (MTQP02)','PG', 202,'DOMAIN','M_TECH'),
(true,'MTQP03','Dairy Technology (MTQP03)','PG', 203,'DOMAIN','M_TECH'),
(true,'MTQP04','Data Science, Artificial Intelligence, Cyber Security etc. (MTQP04)','PG', 204,'DOMAIN','M_TECH'),
(true,'MTQP05','Electronics, Communication and Information Engineering (MTQP05)','PG', 205,'DOMAIN','M_TECH'),
(true,'MTQP06','Food Engineering and Technology (MTQP06)','PG', 206,'DOMAIN','M_TECH'),
(true,'MTQP07','Mechanical Engineering (MTQP07)','PG', 207,'DOMAIN','M_TECH'),
(true,'MTQP08','Nanoscience (MTQP08)','PG', 208,'DOMAIN','M_TECH'),
(true,'MTQP09','Nanoelectronics (MTQP09)','PG', 209,'DOMAIN','M_TECH'),
(true,'MTQP10','Electrical, Power and Energy Engineering (MTQP10)','PG', 210,'DOMAIN','M_TECH'),
(true,'MTQP11','Water Engineering & Management (MTQP11)','PG', 211,'DOMAIN','M_TECH'),
(true,'MTQP12','Textile Engineering (MTQP12)','PG', 212,'DOMAIN','M_TECH'),

-- ============================================================
-- PG : LANGUAGE (LAQP*) -> spec LANGUAGE, domain_name LANGUAGE
-- ============================================================
(true,'LAQP01','English','PG', 301,'LANGUAGE','LANGUAGE'),
(true,'LAQP02','Hindi','PG', 302,'LANGUAGE','LANGUAGE'),
(true,'LAQP03','Sanskrit','PG', 303,'LANGUAGE','LANGUAGE'),
(true,'LAQP04','Linguistics','PG', 304,'LANGUAGE','LANGUAGE'),
(true,'LAQP05','Arabic','PG', 305,'LANGUAGE','LANGUAGE'),
(true,'LAQP06','Assamese','PG', 306,'LANGUAGE','LANGUAGE'),
(true,'LAQP07','Bengali','PG', 307,'LANGUAGE','LANGUAGE'),
(true,'LAQP08','Bhutia','PG', 308,'LANGUAGE','LANGUAGE'),
(true,'LAQP09','Chinese','PG', 309,'LANGUAGE','LANGUAGE'),
(true,'LAQP10','French','PG', 310,'LANGUAGE','LANGUAGE'),
(true,'LAQP11','Garo','PG', 311,'LANGUAGE','LANGUAGE'),
(true,'LAQP12','German','PG', 312,'LANGUAGE','LANGUAGE'),
(true,'LAQP13','Gujarati','PG', 313,'LANGUAGE','LANGUAGE'),
(true,'LAQP14','Spanish (Hispanic)','PG', 314,'LANGUAGE','LANGUAGE'),
(true,'LAQP15','Japanese','PG', 315,'LANGUAGE','LANGUAGE'),
(true,'LAQP16','Kannada','PG', 316,'LANGUAGE','LANGUAGE'),
(true,'LAQP17','Kashmiri','PG', 317,'LANGUAGE','LANGUAGE'),
(true,'LAQP18','Khasi','PG', 318,'LANGUAGE','LANGUAGE'),
(true,'LAQP19','Kokborok','PG', 319,'LANGUAGE','LANGUAGE'),
(true,'LAQP20','Korean','PG', 320,'LANGUAGE','LANGUAGE'),
(true,'LAQP21','Lepcha','PG', 321,'LANGUAGE','LANGUAGE'),
(true,'LAQP22','Limbu','PG', 322,'LANGUAGE','LANGUAGE'),
(true,'LAQP23','Malayalam','PG', 323,'LANGUAGE','LANGUAGE'),
(true,'LAQP24','Manipuri','PG', 324,'LANGUAGE','LANGUAGE'),
(true,'LAQP25','Marathi','PG', 325,'LANGUAGE','LANGUAGE'),
(true,'LAQP26','Nepali','PG', 326,'LANGUAGE','LANGUAGE'),
(true,'LAQP27','Odia','PG', 327,'LANGUAGE','LANGUAGE'),
(true,'LAQP28','Pali','PG', 328,'LANGUAGE','LANGUAGE'),
(true,'LAQP29','Pashto','PG', 329,'LANGUAGE','LANGUAGE'),
(true,'LAQP30','Persian','PG', 330,'LANGUAGE','LANGUAGE'),
(true,'LAQP31','Prakrit','PG', 331,'LANGUAGE','LANGUAGE'),
(true,'LAQP32','Prayojanmoolak','PG', 332,'LANGUAGE','LANGUAGE'),
(true,'LAQP33','Punjabi','PG', 333,'LANGUAGE','LANGUAGE'),
(true,'LAQP34','Russian','PG', 334,'LANGUAGE','LANGUAGE'),
(true,'LAQP35','Tamil','PG', 335,'LANGUAGE','LANGUAGE'),
(true,'LAQP36','Telugu','PG', 336,'LANGUAGE','LANGUAGE'),
(true,'LAQP37','Urdu','PG', 337,'LANGUAGE','LANGUAGE'),
(true,'LAQP38','Urdu Journalism','PG', 338,'LANGUAGE','LANGUAGE'),
(true,'LAQP39','Indo-Tibetan','PG', 339,'LANGUAGE','LANGUAGE'),
(true,'LAQP40','Santali','PG', 340,'LANGUAGE','LANGUAGE'),
(true,'LAQP41','Italian','PG', 341,'LANGUAGE','LANGUAGE'),

-- ============================================================
-- PG : HUMANITIES (HUQP*) -> spec DOMAIN, domain_name HUMANITIES
-- ============================================================
(true,'HUQP01','Ancient Indian History, Culture & Architecture','PG', 401,'DOMAIN','HUMANITIES'),
(true,'HUQP02','Anthropology','PG', 402,'DOMAIN','HUMANITIES'),
(true,'HUQP03','Applied Arts','PG', 403,'DOMAIN','HUMANITIES'),
(true,'HUQP04','Art and Aesthetics','PG', 404,'DOMAIN','HUMANITIES'),
(true,'HUQP05','Dance','PG', 405,'DOMAIN','HUMANITIES'),
(true,'HUQP06','Development and Labour Studies','PG', 406,'DOMAIN','HUMANITIES'),
(true,'HUQP07','Fine Arts','PG', 407,'DOMAIN','HUMANITIES'),
(true,'HUQP08','Geography','PG', 408,'DOMAIN','HUMANITIES'),
(true,'HUQP09','History','PG', 409,'DOMAIN','HUMANITIES'),
(true,'HUQP10','History of Art','PG', 410,'DOMAIN','HUMANITIES'),
(true,'HUQP11','Home Science','PG', 411,'DOMAIN','HUMANITIES'),
(true,'HUQP12','Karnatak Music (Vocal-Instrumental)','PG', 412,'DOMAIN','HUMANITIES'),
(true,'HUQP13','Museology','PG', 413,'DOMAIN','HUMANITIES'),
(true,'HUQP14','Hindustani Music (Vocal-Instrumental)','PG', 414,'DOMAIN','HUMANITIES'),
(true,'HUQP15','Painting','PG', 415,'DOMAIN','HUMANITIES'),
(true,'HUQP16','Philosophy','PG', 416,'DOMAIN','HUMANITIES'),
(true,'HUQP17','Plastic Arts','PG', 417,'DOMAIN','HUMANITIES'),
(true,'HUQP18','Political Science','PG', 418,'DOMAIN','HUMANITIES'),
(true,'HUQP19','Pottery and Ceramic','PG', 419,'DOMAIN','HUMANITIES'),
(true,'HUQP20','Psychology','PG', 420,'DOMAIN','HUMANITIES'),
(true,'HUQP21','Social Work','PG', 421,'DOMAIN','HUMANITIES'),
(true,'HUQP22','Sociology','PG', 422,'DOMAIN','HUMANITIES'),
(true,'HUQP23','Textile Design','PG', 423,'DOMAIN','HUMANITIES'),
(true,'HUQP24','Theatre','PG', 424,'DOMAIN','HUMANITIES'),
(true,'HUQP25','Music – Percussion','PG', 425,'DOMAIN','HUMANITIES'),
(true,'HUQP26','Rabindra Sangit','PG', 426,'DOMAIN','HUMANITIES'),

-- ============================================================
-- PG : ACHARYA (ACQP*) -> spec DOMAIN, domain_name ACHARYA
-- ============================================================
(true,'ACQP01','Shiksha Shastri (B.Ed.)','PG', 501,'DOMAIN','ACHARYA'),
(true,'ACQP02','Shiksha Acharya (M.Ed.)','PG', 502,'DOMAIN','ACHARYA'),
(true,'ACQP03','Agama','PG', 503,'DOMAIN','ACHARYA'),
(true,'ACQP04','Baudha Darshan or Buddhist Studies (Trilingual)','PG', 504,'DOMAIN','ACHARYA'),
(true,'ACQP05','Dharmashastra, Vastu & Paurohitya','PG', 505,'DOMAIN','ACHARYA'),
(true,'ACQP06','Dharm Vijnan','PG', 506,'DOMAIN','ACHARYA'),
(true,'ACQP07','Dharmashastra','PG', 507,'DOMAIN','ACHARYA'),
(true,'ACQP08','Hindu Studies (Bilingual)','PG', 508,'DOMAIN','ACHARYA'),
(true,'ACQP09','Indian Knowledge System (Trilingual)','PG', 509,'DOMAIN','ACHARYA'),
(true,'ACQP10','Jain Darshan','PG', 510,'DOMAIN','ACHARYA'),
(true,'ACQP11','Jyotish – Falit','PG', 511,'DOMAIN','ACHARYA'),
(true,'ACQP12','Jyotish – Ganit','PG', 512,'DOMAIN','ACHARYA'),
(true,'ACQP13','Krishna Yajurveda','PG', 513,'DOMAIN','ACHARYA'),
(true,'ACQP14','Nyaya Vaisheshika','PG', 514,'DOMAIN','ACHARYA'),
(true,'ACQP15','Phalit Jyotish and Siddhant Jyotish','PG', 515,'DOMAIN','ACHARYA'),
(true,'ACQP16','Puranetihasa','PG', 516,'DOMAIN','ACHARYA'),
(true,'ACQP17','Rigveda','PG', 517,'DOMAIN','ACHARYA'),
(true,'ACQP18','Sahitya','PG', 518,'DOMAIN','ACHARYA'),
(true,'ACQP19','Sahitya (Alankara and Kavya Varga)','PG', 519,'DOMAIN','ACHARYA'),
(true,'ACQP20','Samveda','PG', 520,'DOMAIN','ACHARYA'),
(true,'ACQP21','Shukla Yajurveda','PG', 521,'DOMAIN','ACHARYA'),
(true,'ACQP22','Veda Etc.','PG', 522,'DOMAIN','ACHARYA'),
(true,'ACQP23','Vedanta','PG', 523,'DOMAIN','ACHARYA'),
(true,'ACQP24','Vedanta, Sarvadarshan, Mimansa, Nyaya Etc.','PG', 524,'DOMAIN','ACHARYA'),
(true,'ACQP25','Vyakaran','PG', 525,'DOMAIN','ACHARYA'),
(true,'ACQP26','Vyakarana and Shabdabodha System','PG', 526,'DOMAIN','ACHARYA')

ON CONFLICT (programme_level, paper_code)
DO UPDATE SET
  is_active   = EXCLUDED.is_active,
  paper_name  = EXCLUDED.paper_name,
  sort_order  = EXCLUDED.sort_order,
  spec        = EXCLUDED.spec,
  domain_name = EXCLUDED.domain_name;

INSERT INTO mcap.programme (programme_name, programme_level, stream_id, department_id) VALUES

-- Geography
('BA Geography','UG',101,(SELECT department_id FROM mcap.department WHERE department_code='GEO')),

-- Public Administration
('BA Public Administration','UG',101,(SELECT department_id FROM mcap.department WHERE department_code='PAD')),

-- Social Work
('BA Social Work','UG',101,(SELECT department_id FROM mcap.department WHERE department_code='SWK')),

-- Physiology (Science)
('BSc Physiology','UG',102,(SELECT department_id FROM mcap.department WHERE department_code='PHYD')),

-- Biochemistry (Science)
('BSc Biochemistry','UG',102,(SELECT department_id FROM mcap.department WHERE department_code='BCH')),

-- Home Science (can be Arts or Science, choosing Science here)
('BSc Home Science','UG',102,(SELECT department_id FROM mcap.department WHERE department_code='HSD'));
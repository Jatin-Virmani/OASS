
-- ============================================================================
-- OASS demo data. Run ONCE, after schema.sql, on an empty database:
--     mysql -u jatin -p oass < db/seed.sql
-- Every demo account has the password: pass123
-- Password rule used by the app: password_hash = SHA-256 hex of (salt + password)
-- All dates are relative to the moment you RUN this file (NOW() is evaluated once).
-- Deadlines such as "due in 2 hours" pass in real time, so before a demo on a later day reset the data:
--     DROP DATABASE oass; then run schema.sql and seed.sql again.
-- Seed submissions have no physical file on disk (stored_name is NULL).
-- Plagiarism results are NOT seeded: press "Run check" on the Schema Normalization
-- assignment and the engine computes them.
-- ============================================================================
USE oass;

-- ---------------------------------------------------------------------------
-- Academic structure
-- ---------------------------------------------------------------------------
INSERT INTO sections (course, name, batch_year, semester)
VALUES ('B.Tech CSE', 'CSE-5D', 2024, 5);

SET @sec = LAST_INSERT_ID();

INSERT INTO subjects (code, name) VALUES
                                      ('CS501', 'Design and Analysis of Algorithms'),
                                      ('CS502', 'Operating Systems'),
                                      ('CS503', 'Database Management Systems'),
                                      ('CS504', 'Computer Networks'),
                                      ('CS505', 'Advanced Java');

SET @s_daa  = (SELECT id FROM subjects WHERE code = 'CS501');
SET @s_os   = (SELECT id FROM subjects WHERE code = 'CS502');
SET @s_dbms = (SELECT id FROM subjects WHERE code = 'CS503');
SET @s_cn   = (SELECT id FROM subjects WHERE code = 'CS504');
SET @s_java = (SELECT id FROM subjects WHERE code = 'CS505');

-- ---------------------------------------------------------------------------
-- People: 4 teachers (one still PENDING), Jatin + 39 fictional students
-- ---------------------------------------------------------------------------
CREATE TEMPORARY TABLE tmp_students (
    n INT,
    name VARCHAR(100)
);

INSERT INTO tmp_students (n, name) VALUES
                                       (1,'Aarav Sharma'),(2,'Vivaan Gupta'),(3,'Aditya Verma'),(4,'Arjun Singh'),(5,'Reyansh Mehta'),
                                       (6,'Krish Malhotra'),(7,'Ishaan Kapoor'),(8,'Kabir Joshi'),(9,'Rahul Sharma'),(10,'Aman Khanna'),
                                       (11,'Karan Bhatia'),(12,'Neha Arora'),(13,'Priya Nair'),(14,'Simran Kaur'),(15,'Rohan Chopra'),
                                       (16,'Sahil Bansal'),(17,'Aditi Rao'),(18,'Ananya Iyer'),(19,'Diya Menon'),(20,'Isha Agarwal'),
                                       (21,'Kavya Reddy'),(22,'Meera Pillai'),(23,'Nisha Yadav'),(24,'Pooja Tiwari'),(25,'Riya Saxena'),
                                       (26,'Sneha Kulkarni'),(27,'Tanvi Deshmukh'),(28,'Varun Thakur'),(29,'Yash Rathore'),(30,'Harsh Pandey'),
                                       (31,'Mohit Chauhan'),(32,'Nikhil Dubey'),(33,'Pranav Mishra'),(34,'Sanjay Bose'),(35,'Tushar Jain'),
                                       (36,'Utkarsh Sinha'),(37,'Vikas Rawat'),(38,'Zoya Siddiqui'),(39,'Lakshya Goel');

-- Corrected user insertion: generate salt first, then calculate password hash.
INSERT INTO users (name, email, salt, password_hash, role, status)
SELECT
    name,
    email,
    salt,
    SHA2(CONCAT(salt, 'pass123'), 256),
    role,
    status
FROM (
         SELECT
             base.*,
             SHA2(CONCAT(email, 'OASS_DEMO_SALT'), 256) AS salt
         FROM (
                  SELECT
                      'Dr. Ranjana Jain' AS name,
                      'teacher@mru.edu' AS email,
                      'TEACHER' AS role,
                      'ACTIVE' AS status

                  UNION ALL SELECT
                                'Prof. Rajiv Malhotra',
                                'rajiv.malhotra@mru.edu',
                                'TEACHER',
                                'ACTIVE'

                  UNION ALL SELECT
                                'Dr. Sunita Rao',
                                'sunita.rao@mru.edu',
                                'TEACHER',
                                'ACTIVE'

                  UNION ALL SELECT
                                'Dr. Amit Sharma',
                                'amit.sharma@mru.edu',
                                'TEACHER',
                                'PENDING'

                  UNION ALL SELECT
                                'Jatin Virmani',
                                'jatin@mru.edu',
                                'STUDENT',
                                'ACTIVE'

                  UNION ALL
                  SELECT
                      name,
                      CONCAT(LOWER(REPLACE(name, ' ', '.')), '@mru.edu'),
                      'STUDENT',
                      'ACTIVE'
                  FROM tmp_students
              ) AS base
     ) AS with_salt;

INSERT INTO teachers (user_id, employee_id, department)
SELECT
    id,
    CASE email
        WHEN 'teacher@mru.edu'        THEN 'MRU-T-101'
        WHEN 'rajiv.malhotra@mru.edu' THEN 'MRU-T-102'
        WHEN 'sunita.rao@mru.edu'     THEN 'MRU-T-103'
        ELSE 'MRU-T-104'
        END,
    'Computer Science and Engineering'
FROM users
WHERE role = 'TEACHER';

INSERT INTO students (user_id, roll_no, section_id)
SELECT
    u.id,
    CONCAT('2K24CSUN', LPAD(1000 + t.n, 5, '0')),
    @sec
FROM tmp_students t
         JOIN users u
              ON u.email = CONCAT(LOWER(REPLACE(t.name, ' ', '.')), '@mru.edu');

INSERT INTO students (user_id, roll_no, section_id)
SELECT id, '2K24CSUN01099', @sec
FROM users
WHERE email = 'jatin@mru.edu';

SET @t1 = (SELECT id FROM users WHERE email = 'teacher@mru.edu');
SET @t2 = (SELECT id FROM users WHERE email = 'rajiv.malhotra@mru.edu');
SET @t3 = (SELECT id FROM users WHERE email = 'sunita.rao@mru.edu');

INSERT INTO teaching (teacher_id, subject_id, section_id) VALUES
                                                              (@t1, @s_dbms, @sec),
                                                              (@t1, @s_java, @sec),
                                                              (@t2, @s_os, @sec),
                                                              (@t2, @s_cn, @sec),
                                                              (@t3, @s_daa, @sec);

-- ---------------------------------------------------------------------------
-- Assignments (A1..A6) with rubrics
-- ---------------------------------------------------------------------------

-- A1: past, closed, late allowed (Jatin submitted late)
INSERT INTO assignments (
    teacher_id, subject_id, section_id, title, description, max_marks,
    status, deadline, allow_late, late_penalty_pct_per_day,
    max_late_penalty_pct, hard_cutoff, plagiarism_enabled, blind_grading
)
VALUES (
           @t1, @s_java, @sec, 'Java Collections Lab',
           'Implement a student record manager using ArrayList, HashMap and TreeSet. Submit the source and a short report.',
           30, 'CLOSED', NOW() - INTERVAL 10 DAY, 1, 5, 20,
           NOW() - INTERVAL 5 DAY, 0, 0
       );

SET @a1 = LAST_INSERT_ID();

-- A2: past, closed, plagiarism demo, blind grading
INSERT INTO assignments (
    teacher_id, subject_id, section_id, title, description, max_marks,
    status, deadline, allow_late, plagiarism_enabled,
    plagiarism_threshold, blind_grading
)
VALUES (
           @t1, @s_dbms, @sec, 'Mini Project Milestone 2: Schema Normalization',
           'Normalise your project schema up to BCNF. Explain every decomposition step in a short report.',
           50, 'CLOSED', NOW() - INTERVAL 2 DAY, 0, 1, 70, 1
       );

SET @a2 = LAST_INSERT_ID();

-- A3: open, due in 42 hours, late allowed for 2 days
INSERT INTO assignments (
    teacher_id, subject_id, section_id, title, description, max_marks,
    status, deadline, allow_late, late_penalty_pct_per_day,
    max_late_penalty_pct, hard_cutoff
)
VALUES (
           @t2, @s_os, @sec, 'Lab 6: Memory Management and Paging Simulation',
           'Simulate FIFO and LRU page replacement for a given reference string and compare the page faults.',
           20, 'PUBLISHED', NOW() + INTERVAL 42 HOUR, 1, 10, 30,
           NOW() + INTERVAL 90 HOUR
       );

SET @a3 = LAST_INSERT_ID();

-- A4: open, due in 2 hours 14 minutes
INSERT INTO assignments (
    teacher_id, subject_id, section_id, title, description,
    max_marks, status, deadline, allow_late
)
VALUES (
           @t1, @s_java, @sec, 'Servlet and JSP Mini App',
           'Build a small MVC web app with servlets, JSP and JSTL. Submit the project as a zip with a short README.',
           40, 'PUBLISHED', NOW() + INTERVAL 134 MINUTE, 0
       );

SET @a4 = LAST_INSERT_ID();

-- A5: open, due in 4 days
INSERT INTO assignments (
    teacher_id, subject_id, section_id, title, description,
    max_marks, status, deadline, allow_late
)
VALUES (
           @t2, @s_cn, @sec, 'TCP Flow Control and Wireshark Packet Analysis',
           'Capture a TCP session in Wireshark and explain the sliding window behaviour you observe.',
           30, 'PUBLISHED', NOW() + INTERVAL 4 DAY, 0
       );

SET @a5 = LAST_INSERT_ID();

-- A6: draft, not visible to students yet
INSERT INTO assignments (
    teacher_id, subject_id, section_id, title, description,
    max_marks, status, deadline, allow_late
)
VALUES (
           @t3, @s_daa, @sec, 'Dynamic Programming and Greedy Algorithms',
           'Solve the given problem set and analyse the time complexity of each solution.',
           25, 'DRAFT', NOW() + INTERVAL 10 DAY, 0
       );

SET @a6 = LAST_INSERT_ID();

INSERT INTO rubric_criteria (assignment_id, name, max_marks, sort_order) VALUES
                                                                             (@a1, 'Correctness', 15, 1),
                                                                             (@a1, 'Code Quality', 10, 2),
                                                                             (@a1, 'Documentation', 5, 3),
                                                                             (@a2, 'Normalization Steps', 20, 1),
                                                                             (@a2, 'Schema Design', 20, 2),
                                                                             (@a2, 'Explanation', 10, 3),
                                                                             (@a3, 'Correctness', 10, 1),
                                                                             (@a3, 'Analysis', 6, 2),
                                                                             (@a3, 'Report', 4, 3),
                                                                             (@a4, 'Functionality', 20, 1),
                                                                             (@a4, 'Code Quality', 12, 2),
                                                                             (@a4, 'UI and Report', 8, 3),
                                                                             (@a5, 'Analysis', 15, 1),
                                                                             (@a5, 'Evidence', 10, 2),
                                                                             (@a5, 'Conclusions', 5, 3),
                                                                             (@a6, 'Correctness', 15, 1),
                                                                             (@a6, 'Complexity Analysis', 5, 2),
                                                                             (@a6, 'Clarity', 5, 3);

-- ---------------------------------------------------------------------------
-- Question bank and questions attached to two assignments
-- ---------------------------------------------------------------------------
INSERT INTO questions (
    teacher_id, subject_id, question_type, question_text,
    marks, difficulty, topic
) VALUES
      (@t1, @s_dbms, 'DESCRIPTIVE', 'Explain first, second and third normal form with an example for each.', 10, 'MEDIUM', 'Normalization'),
      (@t1, @s_dbms, 'DESCRIPTIVE', 'Decompose a given relation into BCNF and show that the decomposition is lossless.', 15, 'HARD', 'Normalization'),
      (@t1, @s_dbms, 'SHORT', 'Define a super key and a candidate key.', 5, 'EASY', 'Keys'),
      (@t1, @s_java, 'DESCRIPTIVE', 'Explain the servlet life cycle and the role of the init and destroy methods.', 10, 'MEDIUM', 'Servlets'),
      (@t1, @s_java, 'SHORT', 'What is the difference between forward and redirect?', 5, 'EASY', 'Servlets'),
      (@t1, @s_java, 'ESSAY', 'Describe the MVC pattern and show how it maps to servlets and JSP.', 15, 'MEDIUM', 'MVC');

INSERT INTO assignment_questions (assignment_id, question_id)
SELECT @a2, id
FROM questions
WHERE topic = 'Normalization';

INSERT INTO assignment_questions (assignment_id, question_id)
SELECT @a4, id
FROM questions
WHERE topic IN ('Servlets', 'MVC');

-- ---------------------------------------------------------------------------
-- Submissions (one row per upload attempt)
-- ---------------------------------------------------------------------------
SET @txt_rahul = 'Normalization is the process of organising the tables of a relational database so that redundancy is reduced and data integrity is improved. In first normal form every column holds atomic values and each row is unique. In second normal form every non key attribute must depend on the whole primary key and not on a part of it. In third normal form there must be no transitive dependency between non key attributes. We applied these steps to the student course enrollment schema and decomposed it into four smaller tables with proper foreign keys.';

SET @txt_aman = 'Normalization is the process of organizing the tables of a relational database so that redundancy is reduced and data integrity is improved. In first normal form every column holds atomic values and each row is unique. In second normal form every non key attribute must depend on the whole primary key and not on a part of it. In third normal form there must be no transitive dependency between non key attributes. We applied these steps to the student course registration schema and split it into four smaller tables with proper foreign keys.';

SET @txt_karan = 'Normalization is the process of organising the tables of a relational database so that redundancy is reduced and data integrity is improved. In first normal form every column holds atomic values and each row is unique. In second normal form every non key attribute must depend on the whole primary key and not on a part of it. Third normal form removes any transitive dependency between non key attributes. We applied these steps to the student course enrollment schema and decomposed it into four smaller tables with proper foreign keys.';

SET @txt_neha = 'Boyce Codd normal form is a stricter version of third normal form. A relation is in BCNF when for every functional dependency the left side is a super key. We checked our library schema and found a dependency where the determinant was not a super key. We decomposed the table to remove the anomaly while keeping the data lossless.';

SET @txt_priya = 'Boyce Codd normal form is a stricter version of third normal form. A relation is in BCNF when for every functional dependency the left side is a super key. We checked our hospital schema and found a dependency where the determinant was not a super key. We then split the table to remove the anomaly and keep the data lossless.';

SET @txt_aarav = 'Our project stores books, members and loans for a college library. We drew an entity relationship diagram first and then converted every entity into a table. Keys were chosen carefully and repeated author details were moved to their own table so that updates happen in one place only.';

SET @txt_vivaan = 'The hotel booking schema originally kept guest address and room price in the same table. By separating guests, rooms and bookings we removed update anomalies and the number of null values dropped sharply, which also made queries simpler to write.';

SET @txt_aditya = 'For the online shopping database we listed all functional dependencies and computed the candidate keys using attribute closure. The orders table violated third normal form because the customer city depended on the customer id, so we moved it into a customer table.';

SET @txt_arjun = 'We studied lossless join decomposition using a small employee department example. Splitting the table on the department id preserved every row after the natural join, which proves the decomposition did not lose any information.';

SET @txt_jatin1 = 'Draft: we normalised the student attendance database to first normal form by removing repeating groups. Second and third normal form are still to be done.';

SET @txt_jatin2 = 'Normalization of the student attendance database was done in three steps. We removed repeating groups, then moved subject details that depended only on the subject code into a separate table, and finally removed the transitive link between teacher and department.';

CREATE TEMPORARY TABLE tmp_a (
    k VARCHAR(3),
    aid INT
);

INSERT INTO tmp_a VALUES
                      ('A1', @a1),
                      ('A2', @a2),
                      ('A3', @a3),
                      ('A4', @a4),
                      ('A5', @a5),
                      ('A6', @a6);

-- hrs = hours BEFORE the assignment deadline (negative = after the deadline)
CREATE TEMPORARY TABLE tmp_sub (
    k VARCHAR(3),
    email VARCHAR(120),
    v INT,
    hrs INT,
    status VARCHAR(10),
    reason VARCHAR(255),
    txt LONGTEXT
);

INSERT INTO tmp_sub (k, email, v, hrs, status, reason, txt) VALUES
                                                                -- A1 Java Collections Lab: Jatin is LATE (48 hours after the deadline)
                                                                ('A1', 'jatin@mru.edu', 1, -48, 'ACCEPTED', NULL, NULL),
                                                                ('A1', 'aarav.sharma@mru.edu', 1, 5, 'ACCEPTED', NULL, NULL),
                                                                ('A1', 'vivaan.gupta@mru.edu', 1, 10, 'ACCEPTED', NULL, NULL),
                                                                ('A1', 'rahul.sharma@mru.edu', 1, 3, 'ACCEPTED', NULL, NULL),

                                                                -- A2 Schema Normalization (plagiarism demo)
                                                                ('A2', 'jatin@mru.edu', 1, 30, 'ACCEPTED', NULL, @txt_jatin1),
                                                                ('A2', 'jatin@mru.edu', 2, 6, 'ACCEPTED', NULL, @txt_jatin2),
                                                                ('A2', 'rahul.sharma@mru.edu', 1, 10, 'ACCEPTED', NULL, @txt_rahul),
                                                                ('A2', 'aman.khanna@mru.edu', 1, 9, 'ACCEPTED', NULL, @txt_aman),
                                                                ('A2', 'karan.bhatia@mru.edu', 1, 8, 'ACCEPTED', NULL, @txt_karan),
                                                                ('A2', 'neha.arora@mru.edu', 1, 12, 'ACCEPTED', NULL, @txt_neha),
                                                                ('A2', 'priya.nair@mru.edu', 1, 11, 'ACCEPTED', NULL, @txt_priya),
                                                                ('A2', 'aarav.sharma@mru.edu', 1, 20, 'ACCEPTED', NULL, @txt_aarav),
                                                                ('A2', 'vivaan.gupta@mru.edu', 1, 22, 'ACCEPTED', NULL, @txt_vivaan),
                                                                ('A2', 'aditya.verma@mru.edu', 1, 15, 'ACCEPTED', NULL, @txt_aditya),
                                                                ('A2', 'arjun.singh@mru.edu', 1, 18, 'ACCEPTED', NULL, @txt_arjun),
                                                                ('A2', 'reyansh.mehta@mru.edu', 1, 14, 'ACCEPTED', NULL, NULL),
                                                                ('A2', 'krish.malhotra@mru.edu', 1, 13, 'REJECTED', 'File too large (max 10 MB)', NULL),

                                                                -- A3 and A4 (open): a few early submitters
                                                                ('A3', 'aman.khanna@mru.edu', 1, 50, 'ACCEPTED', NULL, NULL),
                                                                ('A4', 'aarav.sharma@mru.edu', 1, 200, 'ACCEPTED', NULL, NULL),
                                                                ('A4', 'vivaan.gupta@mru.edu', 1, 190, 'ACCEPTED', NULL, NULL),
                                                                ('A4', 'rahul.sharma@mru.edu', 1, 170, 'ACCEPTED', NULL, NULL);

INSERT INTO submissions (
    assignment_id, student_id, version_no, upload_status,
    original_name, stored_name, size_bytes, sha256, receipt_no,
    submitted_at, is_late, rejected_reason, text_content
)
SELECT
    aid,
    student_id,
    v,
    status,
    original_name,
    NULL,
    size_bytes,
    sha,
    IF(sha IS NULL, NULL, CONCAT('R-', aid, '-', student_id, '-v', v, '-', LEFT(sha, 8))),
    submitted_at,
    IF(submitted_at > deadline, 1, 0),
    reason,
    txt
FROM (
         SELECT
             a.id AS aid,
             a.deadline,
             u.id AS student_id,
             t.v,
             t.status,
             t.reason,
             t.txt,
             CONCAT(st.roll_no, '_', t.k, '_v', t.v, '.pdf') AS original_name,
             IF(t.status = 'ACCEPTED', GREATEST(LENGTH(IFNULL(t.txt, '')) * 300, 51200), 0) AS size_bytes,
             IF(t.status = 'ACCEPTED', SHA2(CONCAT(u.email, ':', t.k, ':', t.v, ':', IFNULL(t.txt, '')), 256), NULL) AS sha,
             a.deadline - INTERVAL t.hrs HOUR AS submitted_at
         FROM tmp_sub t
             JOIN tmp_a m ON m.k = t.k
             JOIN assignments a ON a.id = m.aid
             JOIN users u ON u.email = t.email
             JOIN students st ON st.user_id = u.id
     ) AS x;

-- ---------------------------------------------------------------------------
-- Evaluations (scores per rubric criterion 1, 2, 3)
-- ---------------------------------------------------------------------------
CREATE TEMPORARY TABLE tmp_eval (
    k VARCHAR(3),
    email VARCHAR(120),
    v INT,
    s1 DECIMAL(6,2),
    s2 DECIMAL(6,2),
    s3 DECIMAL(6,2),
    penalty_pct DECIMAL(5,2),
    status VARCHAR(10),
    days_ago INT,
    feedback VARCHAR(255)
);

INSERT INTO tmp_eval VALUES
                         ('A1', 'jatin@mru.edu', 1, 14, 7, 3, 10, 'PUBLISHED', 8, 'Good logic. A 10% late penalty was applied.'),
                         ('A1', 'aarav.sharma@mru.edu', 1, 14, 8, 4, 0, 'PUBLISHED', 8, 'Clean code and a clear report.'),
                         ('A1', 'vivaan.gupta@mru.edu', 1, 12, 7, 3, 0, 'PUBLISHED', 8, 'Works well. Add more comments.'),
                         ('A1', 'rahul.sharma@mru.edu', 1, 13, 8, 4, 0, 'PUBLISHED', 8, 'Good work.'),
                         ('A2', 'jatin@mru.edu', 2, 17, 18, 7, 0, 'PUBLISHED', 1, 'Good work. Improve the BCNF explanation.'),
                         ('A2', 'aarav.sharma@mru.edu', 1, 16, 17, 7, 0, 'PUBLISHED', 1, 'Solid decomposition.'),
                         ('A2', 'vivaan.gupta@mru.edu', 1, 15, 15, 6, 0, 'FINALIZED', 1, 'Explain the lossless join check.'),
                         ('A2', 'aditya.verma@mru.edu', 1, 12, 12, 6, 0, 'DRAFT', 0, NULL);

INSERT INTO evaluations (
    submission_id, total, late_penalty, final_marks, feedback,
    status, evaluated_by, evaluated_at, published_at
)
SELECT
    s.id,
    te.s1 + te.s2 + te.s3,
    ROUND((te.s1 + te.s2 + te.s3) * te.penalty_pct / 100, 2),
    (te.s1 + te.s2 + te.s3) - ROUND((te.s1 + te.s2 + te.s3) * te.penalty_pct / 100, 2),
    te.feedback,
    te.status,
    a.teacher_id,
    NOW() - INTERVAL te.days_ago DAY,
    IF(te.status = 'PUBLISHED', NOW() - INTERVAL te.days_ago DAY, NULL)
FROM tmp_eval te
    JOIN tmp_a m ON m.k = te.k
    JOIN assignments a ON a.id = m.aid
    JOIN users u ON u.email = te.email
    JOIN submissions s
    ON s.assignment_id = a.id
    AND s.student_id = u.id
    AND s.version_no = te.v;

INSERT INTO evaluation_scores (evaluation_id, criterion_id, marks)
SELECT
    e.id,
    rc.id,
    CASE rc.sort_order
        WHEN 1 THEN te.s1
        WHEN 2 THEN te.s2
        ELSE te.s3
        END
FROM tmp_eval te
         JOIN tmp_a m ON m.k = te.k
         JOIN users u ON u.email = te.email
         JOIN submissions s
              ON s.assignment_id = m.aid
                  AND s.student_id = u.id
                  AND s.version_no = te.v
         JOIN evaluations e ON e.submission_id = s.id
         JOIN rubric_criteria rc ON rc.assignment_id = m.aid;

-- ---------------------------------------------------------------------------
-- Requests, extensions, announcements, notifications, audit log
-- ---------------------------------------------------------------------------

-- Jatin asks for a regrade of the published Schema Normalization result
INSERT INTO regrade_requests (evaluation_id, student_id, reason)
SELECT
    e.id,
    u.id,
    'Please re-check the marks for the BCNF explanation.'
FROM evaluations e
         JOIN submissions s
              ON s.id = e.submission_id
                  AND s.assignment_id = @a2
         JOIN users u
              ON u.id = s.student_id
                  AND u.email = 'jatin@mru.edu';

-- One extension request waiting for the teacher, one already approved
INSERT INTO extensions (assignment_id, student_id, new_deadline, reason, status)
SELECT
    @a3,
    u.id,
    NOW() + INTERVAL 66 HOUR,
    'University network outage',
    'REQUESTED'
FROM users u
WHERE u.email = 'karan.bhatia@mru.edu';

INSERT INTO extensions (
    assignment_id, student_id, new_deadline, reason,
    status, decided_by, decided_at
)
SELECT
    @a5,
    u.id,
    NOW() + INTERVAL 5 DAY,
    'Medical appointment',
    'APPROVED',
    @t2,
    NOW() - INTERVAL 3 HOUR
FROM users u
WHERE u.email = 'aman.khanna@mru.edu';

INSERT INTO announcements (teacher_id, section_id, title, body, created_at) VALUES
                                                                                (@t1, @sec, 'Viva schedule', 'Mini project viva will be held next week. The timetable will be shared on Monday.', NOW() - INTERVAL 1 DAY),
                                                                                (@t1, @sec, 'Servlet and JSP Mini App', 'Remember to include a README with the steps to run your project.', NOW() - INTERVAL 6 HOUR),
                                                                                (@t2, @sec, 'Lab 6 clarification', 'Use the reference string given in the lab manual, not the one from the lecture.', NOW() - INTERVAL 2 DAY);

SET @jatin = (SELECT id FROM users WHERE email = 'jatin@mru.edu');

INSERT INTO notifications (
    user_id, title, message, link, is_read, created_at
) VALUES
      (@jatin, 'Assignment due in 2 hours', 'Servlet and JSP Mini App is due in 2 hours.', CONCAT('/student/assignment/view?id=', @a4), 0, NOW() - INTERVAL 5 MINUTE),
      (@jatin, 'Result published', 'Your result for Schema Normalization is published.', '/student/results', 0, NOW() - INTERVAL 1 DAY),
      (@jatin, 'New assignment', 'TCP Flow Control and Wireshark Packet Analysis was created.', CONCAT('/student/assignment/view?id=', @a5), 0, NOW() - INTERVAL 2 DAY),
      (@jatin, 'Late penalty applied', 'A 10% late penalty was applied to Java Collections Lab.', '/student/results', 1, NOW() - INTERVAL 8 DAY),
      (@jatin, 'Submission received', 'Your submission for Schema Normalization (v2) was received.', '/student/submissions', 1, NOW() - INTERVAL 2 DAY);

INSERT INTO notifications (user_id, title, message, link, is_read) VALUES
                                                                       (@t1, 'New submissions', 'Servlet and JSP Mini App has 3 new submissions.', '/teacher/submissions', 0),
                                                                       (@t2, 'Extension request', 'Karan Bhatia requested an extension for Lab 6.', '/teacher/requests', 0);

INSERT INTO audit_log (
    ts, user_id, role, action, entity, old_value,
    new_value, reason, ip
) VALUES
      (NOW() - INTERVAL 3 HOUR, @jatin, 'STUDENT', 'LOGIN', NULL, NULL, NULL, NULL, '192.168.1.21'),
      (NOW() - INTERVAL 2 DAY, @jatin, 'STUDENT', 'SUBMIT', CONCAT('assignment:', @a2), NULL, 'v2', NULL, '192.168.1.21'),
      (NOW() - INTERVAL 1 DAY, @t1, 'TEACHER', 'MARKS_PUBLISHED', CONCAT('assignment:', @a2), NULL, 'published', NULL, '192.168.1.15'),
      (NOW() - INTERVAL 20 HOUR, @t1, 'TEACHER', 'MARKS_CHANGED', CONCAT('assignment:', @a2), '40', '42', 'Re-checked Q2', '192.168.1.15'),
      (NOW() - INTERVAL 3 HOUR, @t2, 'TEACHER', 'EXTENSION_APPROVED', CONCAT('assignment:', @a5), NULL, '+1 day', 'Medical appointment', '192.168.1.17');

-- ---------------------------------------------------------------------------
-- Cleanup temporary tables
-- ---------------------------------------------------------------------------
DROP TEMPORARY TABLE tmp_eval;
DROP TEMPORARY TABLE tmp_sub;
DROP TEMPORARY TABLE tmp_a;
DROP TEMPORARY TABLE tmp_students;

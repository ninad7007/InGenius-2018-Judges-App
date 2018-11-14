DROP VIEW IF EXISTS myScoreView;
DROP VIEW IF EXISTS myTeamView;
DROP TABLE IF EXISTS score;
DROP TABLE IF EXISTS team;
DROP TABLE IF EXISTS judge;
DROP TABLE IF EXISTS domain;

CREATE TABLE domain (
    Domain_id INT PRIMARY KEY AUTO_INCREMENT,
    Domain_name VARCHAR(50) NOT NULL
);

CREATE TABLE judge (
    Judge_id INT PRIMARY KEY AUTO_INCREMENT,
    Judge_name VARCHAR(50) NOT NULL,
    Domain_id INT NOT NULL,
    FOREIGN KEY (Domain_id) REFERENCES domain (Domain_id) ON DELETE CASCADE
);

CREATE TABLE team (
    Team_id INT PRIMARY KEY AUTO_INCREMENT,
    Team_name VARCHAR(50) NOT NULL,
    Seminar_hall INT NOT NULL,
    Domain_one_id INT NOT NULL,
    Domain_two_id INT NOT NULL,
    Primary_judge_id INT NOT NULL,
    Secondary_judge_id INT NOT NULL,
    FOREIGN KEY (Domain_one_id) REFERENCES domain (Domain_id) ON DELETE CASCADE,
    FOREIGN KEY (Domain_two_id) REFERENCES domain (Domain_id) ON DELETE CASCADE,
    FOREIGN KEY (Primary_judge_id) REFERENCES judge (Judge_id) ON DELETE CASCADE,
    FOREIGN KEY (Secondary_judge_id) REFERENCES judge (Judge_id) ON DELETE CASCADE
);

CREATE TABLE score (
    Judge_id INT NOT NULL,
    Team_id INT NOT NULL,
    PRIMARY KEY(Judge_id, Team_id),    
    FOREIGN KEY (Judge_id) REFERENCES judge (Judge_id) ON DELETE CASCADE,
    FOREIGN KEY (Team_id) REFERENCES team (Team_id) ON DELETE CASCADE,
    criteria_one_score INT NOT NULL,
    criteria_two_score INT NOT NULL,
    criteria_three_score INT NOT NULL,
    criteria_four_score INT NOT NULL
);

-- VIEWS 
CREATE VIEW myTeamView AS 
SELECT t.Team_name AS Team_name,
    t.Team_id AS Team_id,
    t.Seminar_hall AS Seminar_hall,
    j1.Judge_name AS Primary_judge_name,
    t.Primary_judge_id AS Primary_judge_id,
    j2.Judge_name AS Secondary_judge_name,
    t.Secondary_judge_id AS Secondary_judge_id,
    CASE
		WHEN s1.criteria_one_score IS NULL THEN "NO"
        ELSE "YES"
	END AS Primary_judged,
    CASE 
		WHEN s2.criteria_one_score IS NULL THEN "NO"
        ELSE "YES"
	END AS Secondary_judged
FROM (((team t JOIN judge j1 ON t.Primary_judge_id = j1.Judge_id) 
JOIN judge j2 ON t.Secondary_judge_id = j2.Judge_id)
LEFT JOIN score s1 ON s1.Judge_id = t.Primary_judge_id AND s1.Team_id = t.Team_id)
LEFT JOIN score s2 ON s2.Judge_id = t.Secondary_judge_id AND s2.Team_id = t.Team_id
ORDER BY t.Seminar_hall, t.Team_name;


CREATE VIEW myScoreView AS 
SELECT s.Judge_id AS Judge_id, 
    s.Team_id AS Team_id,
    criteria_one_score AS p1,
    criteria_two_score AS p2,
    criteria_three_score AS p3,
    criteria_four_score AS p4,
    CASE
        WHEN t.Domain_two_id = t.Domain_one_id THEN "3"
        WHEN j.Domain_id = t.Domain_one_id THEN "1"
        WHEN j.Domain_id = t.Domain_two_id THEN "2"
    END AS Domain_preference
FROM (score s NATURAL JOIN (judge j NATURAL JOIN team t));

-- insertion commands
INSERT INTO domain (Domain_name) VALUES 
("Application/Web Development"), 
("Machine Learning/Data Science/A.I."),
("Electronics/IOT");

-- selection commands

-- team + domains
select t.Team_name, d1.Domain_name as primary_domain, d2.Domain_name as secondary_domain
from ((team t JOIN domain d1 ON d1.Domain_id = t.Domain_one_id) JOIN domain d2 on d2.Domain_id = t.Domain_two_id)
order by t.Team_name;

-- domain stats
select count(*) as strength, d1.Domain_name as primary_domain, d2.Domain_name as secondary_domain
from ((team t JOIN domain d1 ON d1.Domain_id = t.Domain_one_id) JOIN domain d2 on d2.Domain_id = t.Domain_two_id)
GROUP BY d1.Domain_id, d2.Domain_id;

-- teams Excel sheet creator
select Team_name, Seminar_hall, d1.Domain_name, d2.Domain_name, Primary_judge_id, Secondary_judge_id 
from (team JOIN domain d1 on d1.Domain_id = Domain_one_id)
JOIN domain d2 on d2.Domain_id = Domain_two_id  
order by Team_id;


-- morning judges
INSERT INTO judge (Judge_name, Domain_id) VALUES
("app_j1", 1),
("app_j2", 1),
("app_j3", 1),
("app_j4", 1),
("ml_j1", 2),
("ml_j2", 2),
("ml_j3", 2),
("ml_j4", 2),
("iot_j1", 3),  
("iot_j2", 3),
("iot_j3", 3);
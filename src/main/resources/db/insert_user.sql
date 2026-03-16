DROP TABLE IF EXISTS users;
CREATE TABLE users(id SERIAL PRIMARY KEY, username VARCHAR(50), login VARCHAR(50), password_hash VARCHAR(50));

INSERT INTO users (username, login, password_hash) VALUES ('test1', 'test1', 'pass1');
INSERT INTO users (username, login, password_hash) VALUES ('test2', 'test2', 'pass2');
INSERT INTO users (username, login, password_hash) VALUES ('test3', 'test3', 'pass3');
INSERT INTO users (username, login, password_hash) VALUES ('test4', 'test4', 'pass4');
INSERT INTO users (username, login, password_hash) VALUES ('test5', 'test5', 'pass5');
INSERT INTO users (username, login, password_hash) VALUES ('test6', 'test6', 'pass6');
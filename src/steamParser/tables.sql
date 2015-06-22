DROP TABLE IF EXISTS games;

CREATE TABLE IF NOT EXISTS games (
	appid INT PRIMARY KEY,
	name TEXT NOT NULL,
	positive INT,
	negative INT
);
DROP TABLE IF EXISTS games;

CREATE TABLE IF NOT EXISTS games (
	appid INT PRIMARY KEY,
	name TEXT NOT NULL,
	positive INT,
	negative INT,
--	5 digits with 2 decimal points
	rating NUMERIC(5, 2)
);

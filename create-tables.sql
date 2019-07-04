CREATE TABLE PlayerData IF NOT EXISTS (UUID VARCHAR(36) PRIMARY KEY NOT NULL, LastSeenName VARCHAR(16) NOT NULL, Emeralds INT NOT NULL, LongestKillStreak INT NOT NULL);
CREATE TABLE SeasonStats IF NOT EXISTS (StatsID INT PRIMARY KEY AUTO_INCREMENT, Kills SMALLINT NOT NULL, Deaths SMALLINT NOT NULL, Monuments SMALLINT NOT NULL, Wins SMALLINT NOT NULL, Losses SMALLINT NOT NULL, PlayTimeWon INT NOT NULL, PlayTimeLost INT NOT NULL, SupplyDropsOpened SMALLINT NOT NULL);
CREATE TABLE PDSS IF NOT EXISTS (UUID VARCHAR(36), Season TINYINT, StatsID INT, FOREIGN KEY (StatsID) REFERENCES SeasonStats(StatsID));

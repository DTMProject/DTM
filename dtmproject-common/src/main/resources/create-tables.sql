CREATE TABLE IF NOT EXISTS PlayerData
(
   UUID VARCHAR (36) NOT NULL,
   LastSeenName VARCHAR (16) NOT NULL,
   Prefix VARCHAR (100),
   Nick VARCHAR (100),
   Emeralds INT NOT NULL DEFAULT 0,
   KillStreak INT NOT NULL DEFAULT 0,
   EloRating INT NOT NULL DEFAULT 1000,
   PRIMARY KEY (UUID)
);
CREATE TABLE IF NOT EXISTS SeasonStats
(
   UUID VARCHAR (36) NOT NULL,
   Season TINYINT NOT NULL,
   Kills SMALLINT NOT NULL DEFAULT 0,
   Deaths SMALLINT NOT NULL DEFAULT 0,
   MonumentsDestroyed SMALLINT NOT NULL DEFAULT 0,
   Wins SMALLINT NOT NULL DEFAULT 0,
   Losses SMALLINT NOT NULL DEFAULT 0,
   PlayTimeWon INT NOT NULL DEFAULT 0,
   PlayTimeLost INT NOT NULL DEFAULT 0,
   LongestKillStreak INT NOT NULL DEFAULT 0,
   FOREIGN KEY (UUID) REFERENCES PlayerData (UUID),
   PRIMARY KEY
   (
      UUID,
      Season
   )
);
CREATE TABLE IF NOT EXISTS Kits
(
   KitID INTEGER NOT NULL AUTO_INCREMENT,
   Contents BLOB UNIQUE NOT NULL PRIMARY KEY (KitID)
)
CREATE TABLE IF NOT EXISTS Maps
(
   MapID VARCHAR (50) NOT NULL,
   DisplayName VARCHAR (50) NOT NULL,
   LobbyX DECIMAL,
   LobbyY DECIMAL,
   LobbyZ DECIMAL,
   LobbyYaw DECIMAL,
   LobbyPitch DECIMAL,
   Ticks INTEGER NOT NULL DEFAULT 0,
   KitID INTEGER NOT NULL DEFAULT 0,
   PRIMARY KEY (MapID),
   FOREIGN KEY (KitID) REFERENCES Kits (KitID)
);
CREATE TABLE IF NOT EXISTS Teams
(
   MapID VARCHAR (50) NOT NULL,
   TeamID VARCHAR (50) NOT NULL,
   DisplayName VARCHAR (50) NOT NULL,
   TeamColor VARCHAR NOT NULL,
   SpawnX DECIMAL,
   SpawnY DECIMAL,
   SpawnZ DECIMAL,
   SpawnYaw DECIMAL,
   SpawnPitch DECIMAL,
   PRIMARY KEY
   (
      MapID,
      TeamID
   ),
   FOREIGN KEY (MapID) REFERENCES Maps (MapID),
);
CREATE TABLE IF NOT EXISTS Monuments
(
   MapID VARCHAR (50) NOT NULL,
   TeamID VARCHAR (50) NOT NULL,
   Position VARCHAR (4) NOT NULL,
   CustomName VARCHAR (50) NOT NULL,
   LocationX DECIMAL NOT NULL,
   LocationY DECIMAL NOT NULL,
   LocationZ DECIMAL NOT NULL,
   LocationYaw DECIMAL NOT NULL,
   LocationPitch DECIMAL NOT NULL,
   PRIMARY KEY
   (
      MapID,
      TeamID,
      Position
   ),
   FOREIGN KEY (MapID) REFERENCES Maps (MapID),
   FOREIGN KEY (TeamID) REFERENCES Teams (TeamID)
);
/* Need to switch of FK check for MySQL since there are crosswise FK references */
SET FOREIGN_KEY_CHECKS = 0;;

CREATE TABLE IF NOT EXISTS Game (
  gameID int NOT NULL UNIQUE AUTO_INCREMENT,

  name varchar(255),

  phase tinyint,
  step tinyint,
  currentPlayer tinyint NULL,
    boardName varchar(255),

  PRIMARY KEY (gameID),
  FOREIGN KEY (gameID, currentPlayer) REFERENCES Player(gameID, playerID)
);;

CREATE TABLE IF NOT EXISTS Player (
  gameID int NOT NULL,
  playerID tinyint NOT NULL,

  name varchar(255),
  colour varchar(31),

  positionX int,
  positionY int,
  heading tinyint,

  PRIMARY KEY (gameID, playerID),
  FOREIGN KEY (gameID) REFERENCES Game(gameID)
);;

CREATE TABLE IF NOT EXISTS Cards (
    gameID int NOT NULL,
    playerID tinyint NOT NULL,

    hand0 int,
    hand1 int,
    hand2 int,
    hand3 int,
    hand4 int,
    hand5 int,
    hand6 int,
    hand7 int,
    register0 int,
    register1 int,
    register2 int,
    register3 int,
    register4 int,

    PRIMARY KEY (gameID, playerID),
    FOREIGN KEY (gameID) REFERENCES Game(gameID),
    FOREIGN KEY (gameID, playerID) REFERENCES Player(gameID, playerID)
    );;

SET FOREIGN_KEY_CHECKS = 1;;


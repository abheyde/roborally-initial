/*
 *  This file is part of the initial project provided for the
 *  course "Project in Software Development (02362)" held at
 *  DTU Compute at the Technical University of Denmark.
 *
 *  Copyright (C) 2019, 2020: Ekkart Kindler, ekki@dtu.dk
 *
 *  This software is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; version 2 of the License.
 *
 *  This project is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this project; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
package dk.dtu.compute.se.pisd.roborally.dal;

import dk.dtu.compute.se.pisd.roborally.fileaccess.LoadBoard;
import dk.dtu.compute.se.pisd.roborally.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This class creates all the data for the database
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 * @author Jens Lindegaard, s205343@student.dtu.dk
 * @author Alexander Bak Heyde, s193576@student.dtu.dk
 * @author Andreas Krone Reichl, s205362@student.dtu.dk
 * @author Andreas Borg Kristensen, s205338@student.dtu.dk
 * @author Kim Michael Randgaard, s205341@student.dtu.dk
 *
 */
class Repository implements IRepository {
	
	private static final String GAME_GAMEID = "gameID";

	private static final String GAME_NAME = "name";
	
	private static final String GAME_CURRENTPLAYER = "currentPlayer";

	private static final String GAME_PHASE = "phase";

	private static final String GAME_STEP = "step";
	
	private static final String PLAYER_PLAYERID = "playerID";
	
	private static final String PLAYER_NAME = "name";

	private static final String PLAYER_COLOUR = "colour";
	
	private static final String PLAYER_GAMEID = "gameID";
	
	private static final String PLAYER_POSITION_X = "positionX";

	private static final String PLAYER_POSITION_Y = "positionY";

	private static final String PLAYER_HEADING = "heading";

	private static final String CARD_GAMEID = "gameID";
	private static final String CARD_PLAYERID = "playerID";

	private static final String CARD_HAND0 = "hand0";
	private static final String CARD_HAND1 = "hand1";
	private static final String CARD_HAND2 = "hand2";
	private static final String CARD_HAND3 = "hand3";
	private static final String CARD_HAND4 = "hand4";
	private static final String CARD_HAND5 = "hand5";
	private static final String CARD_HAND6 = "hand6";
	private static final String CARD_HAND7 = "hand7";

	private static final String CARD_REGISTER0 = "register0";
	private static final String CARD_REGISTER1 = "register1";
	private static final String CARD_REGISTER2 = "register2";
	private static final String CARD_REGISTER3 = "register3";
	private static final String CARD_REGISTER4 = "register4";

	private static final String BORD_NAME = "boardName";

	private static final String CHECKPOINT = "checkpoint";

	private Connector connector;
	
	Repository(Connector connector){
		this.connector = connector;
	}


	/**
	 * This creates our game ind the database.
	 * @param game
	 * @return if succes thrue else we get an error
	 */
	@Override
	public boolean createGameInDB(Board game) {
		if (game.getGameId() == null) {
			Connection connection = connector.getConnection();
			try {
				connection.setAutoCommit(false);

				PreparedStatement ps = getInsertGameStatementRGK();
				// TODO: the name should eventually set by the user
				//       for the game and should be then used 
				//       game.getName();
				//ps.setNull(0, game.getGameId());
				ps.setString(1, "Date: " +  new Date()); // instead of name
				ps.setInt(2, game.getPlayerNumber(game.getCurrentPlayer()));
				ps.setInt(3, game.getPhase().ordinal());
				ps.setInt(4, game.getStep());
				ps.setString(5, game.boardName);

				// If you have a foreign key constraint for current players,
				// the check would need to be temporarily disabled, since
				// MySQL does not have a per transaction validation, but
				// validates on a per row basis.
				Statement statement = connection.createStatement();
				statement.execute("SET foreign_key_checks = 0");
				
				int affectedRows = ps.executeUpdate();
				ResultSet generatedKeys = ps.getGeneratedKeys();
				if (affectedRows == 1 && generatedKeys.next()) {
					game.setGameId(generatedKeys.getInt(1));
				}
				generatedKeys.close();
				
				// Enable foreign key constraint check again:
				statement.execute("SET foreign_key_checks = 1");
				statement.close();

				createPlayersInDB(game);
				/* TOODO this method needs to be implemented first
				createCardFieldsInDB(game);
				 */
				createCardFieldsInDB(game);
				// since current player is a foreign key, it can oly be
				// inserted after the players are created, since MySQL does
				// not have a per transaction validation, but validates on
				// a per row basis.
				ps = getSelectGameStatementU();
				ps.setInt(1, game.getGameId());

				ResultSet rs = ps.executeQuery();
				if (rs.next()) {
					rs.updateInt(GAME_CURRENTPLAYER, game.getPlayerNumber(game.getCurrentPlayer()));
					rs.updateRow();
				} else {
					// TODO error handling
				}
				rs.close();

				connection.commit();
				connection.setAutoCommit(true);
				return true;
			} catch (SQLException e) {
				// TODO error handling
				e.printStackTrace();
				System.err.println("Some DB error");
				
				try {
					connection.rollback();
					connection.setAutoCommit(true);
				} catch (SQLException e1) {
					// TODO error handling
					e1.printStackTrace();
				}
			}
		} else {
			System.err.println("Game cannot be created in DB, since it has a game id already!");
		}
		return false;
	}
		
	@Override
	public boolean updateGameInDB(Board game) {
		assert game.getGameId() != null;
		
		Connection connection = connector.getConnection();
		try {
			connection.setAutoCommit(false);

			PreparedStatement ps = getSelectGameStatementU();
			ps.setInt(1, game.getGameId());
			
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				rs.updateInt(GAME_CURRENTPLAYER, game.getPlayerNumber(game.getCurrentPlayer()));
				rs.updateInt(GAME_PHASE, game.getPhase().ordinal());
				rs.updateInt(GAME_STEP, game.getStep());
				rs.updateRow();
			} else {
				// TODO error handling
			}
			rs.close();

			updatePlayersInDB(game);
			updateCardFieldsInDB(game);
			/* TOODO this method needs to be implemented first
			updateCardFieldsInDB(game);
			*/

            connection.commit();
            connection.setAutoCommit(true);
			return true;
		} catch (SQLException e) {
			// TODO error handling
			e.printStackTrace();
			System.err.println("Some DB error");
			
			try {
				connection.rollback();
				connection.setAutoCommit(true);
			} catch (SQLException e1) {
				// TODO error handling
				e1.printStackTrace();
			}
		}

		return false;
	}
	
	@Override
	public Board loadGameFromDB(int id) {
		Board game;
		try {
			// TODO here, we could actually use a simpler statement
			//      which is not updatable, but reuse the one from
			//      above for the pupose
			PreparedStatement ps = getSelectGameStatementU();
			ps.setInt(1, id);
			
			ResultSet rs = ps.executeQuery();
			int playerNo = -1;
			if (rs.next()) {
				// TODO the width and height could eventually come from the database
				// int width = AppController.BOARD_WIDTH;
				// int height = AppController.BOARD_HEIGHT;
				// game = new Board(width,height);
				// TODO and we should also store the used game board in the database
				//      for now, we use the default game board
				game = LoadBoard.loadBoard(rs.getString(BORD_NAME));
				if (game == null) {
					return null;
				}
				playerNo = rs.getInt(GAME_CURRENTPLAYER);
				// TODO currently we do not set the games name (needs to be added)
				game.setPhase(Phase.values()[rs.getInt(GAME_PHASE)]);
				game.setStep(rs.getInt(GAME_STEP));
			} else {
				// TODO error handling
				return null;
			}
			rs.close();

			game.setGameId(id);			
			loadPlayersFromDB(game);

			if (playerNo >= 0 && playerNo < game.getPlayersNumber()) {
				game.setCurrentPlayer(game.getPlayer(playerNo));
			} else {
				// TODO  error handling
				return null;
			}

			/* TOODO this method needs to be implemented first
			loadCardFieldsFromDB(game);
			*/
			loadCardFieldsFromDB(game);
			return game;
		} catch (SQLException e) {
			// TODO error handling
			e.printStackTrace();
			System.err.println("Some DB error");
		}
		return null;
	}
	
	@Override
	public List<GameInDB> getGames() {
		// TODO when there many games in the DB, fetching all available games
		//      from the DB is a bit extreme; eventually there should a
		//      methods that can filter the returned games in order to
		//      reduce the number of the returned games.
		List<GameInDB> result = new ArrayList<>();
		try {
			PreparedStatement ps = getSelectGameIdsStatement();
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				int id = rs.getInt(GAME_GAMEID);
				String name = rs.getString(GAME_NAME);
				result.add(new GameInDB(id,name));
			}
			rs.close();
		} catch (SQLException e) {
			// TODO proper error handling
			e.printStackTrace();
		}
		return result;		
	}

	private int commandToInt(CommandCard command) {
		int temp;
		if(command == null)	{
			temp = 0;
		}
		else {
			switch (command.command) {
				case FORWARD:
					temp = 1;
					break;
				case RIGHT:
					temp = 2;
					break;
				case LEFT:
					temp = 3;
					break;
				case FAST_FORWARD:
					temp = 4;
					break;
				case SUPER_FAST_FORWARD:
					temp = 5;
					break;
				case TURN_180:
					temp = 6;
					break;
				case TURN_180_FORWARD:
					temp = 7;
					break;
				case OPTION_LEFT_RIGHT:
					temp = 8;
					break;

				default:
					temp = 0;
			}
		}
		return temp;
	}

	private Command intToCommand(int commandIndex)
	{
		Command temp = null;

		switch (commandIndex) {
			case 1:
				temp = Command.FORWARD;
				break;
			case 2:
				temp = Command.RIGHT;
				break;
			case 3:
				temp = Command.LEFT;
				break;
			case 4:
				temp = Command.FAST_FORWARD;
				break;
			case 5:
				temp = Command.SUPER_FAST_FORWARD;
				break;
			case 6:
				temp = Command.TURN_180;
				break;
			case 7:
				temp = Command.TURN_180_FORWARD;
				break;
			case 8:
				temp = Command.OPTION_LEFT_RIGHT;
				break;
			default:
				// DO NOTHING (for now)
		}
		return temp;
	}


	/**
	 * This creats our card fields in the database
	 * @param game
	 * @throws SQLException
	 */
	private void createCardFieldsInDB(Board game) throws SQLException {
		PreparedStatement ps = getSelectCardsStatementU();
		ps.setInt(1, game.getGameId());

		ResultSet rs = ps.executeQuery();
		for(int i = 0; i < game.getPlayersNumber(); i++) {
			Player player = game.getPlayer(i);
			System.out.println(player);
			rs.moveToInsertRow();
			rs.updateInt(CARD_GAMEID, game.getGameId());
			rs.updateInt(CARD_PLAYERID, i);

			rs.updateInt(CARD_HAND0, commandToInt(player.getCardField(0).getCard()));
			rs.updateInt(CARD_HAND1, commandToInt(player.getCardField(1).getCard()));
			rs.updateInt(CARD_HAND2, commandToInt(player.getCardField(2).getCard()));
			rs.updateInt(CARD_HAND3, commandToInt(player.getCardField(3).getCard()));
			rs.updateInt(CARD_HAND4, commandToInt(player.getCardField(4).getCard()));
			rs.updateInt(CARD_HAND5, commandToInt(player.getCardField(5).getCard()));
			rs.updateInt(CARD_HAND6, commandToInt(player.getCardField(6).getCard()));
			rs.updateInt(CARD_HAND7, commandToInt(player.getCardField(7).getCard()));

			rs.updateInt(CARD_REGISTER0, commandToInt(player.getProgramField(0).getCard()));
			rs.updateInt(CARD_REGISTER1, commandToInt(player.getProgramField(1).getCard()));
			rs.updateInt(CARD_REGISTER2, commandToInt(player.getProgramField(2).getCard()));
			rs.updateInt(CARD_REGISTER3, commandToInt(player.getProgramField(3).getCard()));
			rs.updateInt(CARD_REGISTER4, commandToInt(player.getProgramField(4).getCard()));

			rs.insertRow();
		}
		rs.close();
	}


	/**
	 * This loads our crads from the database
	 * @param game
	 * @throws SQLException
	 */
	private void loadCardFieldsFromDB(Board game) throws SQLException {
		PreparedStatement ps = getSelectCardsASCStatement();
		ps.setInt(1, game.getGameId());

		ResultSet rs = ps.executeQuery();

		int i = 0;
		while (rs.next()) {
			int playerId = rs.getInt(CARD_PLAYERID);

			if (i == playerId) {
				if (rs.getInt(CARD_HAND0) != 0)
					game.getPlayer(i).getCardField(0).setCard(new CommandCard(intToCommand(rs.getInt(CARD_HAND0))));
				if (rs.getInt(CARD_HAND1) != 0)
					game.getPlayer(i).getCardField(1).setCard(new CommandCard(intToCommand(rs.getInt(CARD_HAND1))));
				if (rs.getInt(CARD_HAND2) != 0)
					game.getPlayer(i).getCardField(2).setCard(new CommandCard(intToCommand(rs.getInt(CARD_HAND2))));
				if (rs.getInt(CARD_HAND3) != 0)
					game.getPlayer(i).getCardField(3).setCard(new CommandCard(intToCommand(rs.getInt(CARD_HAND3))));
				if (rs.getInt(CARD_HAND4) != 0)
					game.getPlayer(i).getCardField(4).setCard(new CommandCard(intToCommand(rs.getInt(CARD_HAND4))));
				if (rs.getInt(CARD_HAND5) != 0)
					game.getPlayer(i).getCardField(5).setCard(new CommandCard(intToCommand(rs.getInt(CARD_HAND5))));
				if (rs.getInt(CARD_HAND6) != 0)
					game.getPlayer(i).getCardField(6).setCard(new CommandCard(intToCommand(rs.getInt(CARD_HAND6))));
				if (rs.getInt(CARD_HAND7) != 0)
					game.getPlayer(i).getCardField(7).setCard(new CommandCard(intToCommand(rs.getInt(CARD_HAND7))));

				if (rs.getInt(CARD_REGISTER0) != 0)
					game.getPlayer(i).getProgramField(0).setCard(new CommandCard(intToCommand(rs.getInt(CARD_REGISTER0))));
				if (rs.getInt(CARD_REGISTER1) != 0)
					game.getPlayer(i).getProgramField(1).setCard(new CommandCard(intToCommand(rs.getInt(CARD_REGISTER1))));
				if (rs.getInt(CARD_REGISTER2) != 0)
					game.getPlayer(i).getProgramField(2).setCard(new CommandCard(intToCommand(rs.getInt(CARD_REGISTER2))));
				if (rs.getInt(CARD_REGISTER3) != 0)
					game.getPlayer(i).getProgramField(3).setCard(new CommandCard(intToCommand(rs.getInt(CARD_REGISTER3))));
				if (rs.getInt(CARD_REGISTER4) != 0)
					game.getPlayer(i).getProgramField(4).setCard(new CommandCard(intToCommand(rs.getInt(CARD_REGISTER4))));
			} else {
				// TODO error handling
				System.err.println("Player with id " + i +" not found in the Card table");
			}
			i++;
		}
		rs.close();
	}

	private void updateCardFieldsInDB(Board game) throws SQLException {
		PreparedStatement ps = getSelectCardsStatementU();
		ps.setInt(1, game.getGameId());

		ResultSet rs = ps.executeQuery();
		while (rs.next()) {

			int playerId = rs.getInt(CARD_PLAYERID);
			Player player = game.getPlayer(playerId);

			rs.updateInt(CARD_HAND0, commandToInt(player.getCardField(0).getCard()));
			rs.updateInt(CARD_HAND1, commandToInt(player.getCardField(1).getCard()));
			rs.updateInt(CARD_HAND2, commandToInt(player.getCardField(2).getCard()));
			rs.updateInt(CARD_HAND3, commandToInt(player.getCardField(3).getCard()));
			rs.updateInt(CARD_HAND4, commandToInt(player.getCardField(4).getCard()));
			rs.updateInt(CARD_HAND5, commandToInt(player.getCardField(5).getCard()));
			rs.updateInt(CARD_HAND6, commandToInt(player.getCardField(6).getCard()));
			rs.updateInt(CARD_HAND7, commandToInt(player.getCardField(7).getCard()));

			rs.updateInt(CARD_REGISTER0, commandToInt(player.getProgramField(0).getCard()));
			rs.updateInt(CARD_REGISTER1, commandToInt(player.getProgramField(1).getCard()));
			rs.updateInt(CARD_REGISTER2, commandToInt(player.getProgramField(2).getCard()));
			rs.updateInt(CARD_REGISTER3, commandToInt(player.getProgramField(3).getCard()));
			rs.updateInt(CARD_REGISTER4, commandToInt(player.getProgramField(4).getCard()));
			rs.updateRow();
		}
		rs.close();

		// TODO error handling/consistency check: check whether all players were updated
	}


	/**
	 * This creates our player in the database
	 * @param game
	 * @throws SQLException
	 */
	private void createPlayersInDB(Board game) throws SQLException {
		// TODO code should be more defensive
		PreparedStatement ps = getSelectPlayersStatementU();
		ps.setInt(1, game.getGameId());
		
		ResultSet rs = ps.executeQuery();
		for (int i = 0; i < game.getPlayersNumber(); i++) {
			Player player = game.getPlayer(i);
			rs.moveToInsertRow();
			rs.updateInt(PLAYER_GAMEID, game.getGameId());
			rs.updateInt(PLAYER_PLAYERID, i);
			rs.updateString(PLAYER_NAME, player.getName());
			rs.updateString(PLAYER_COLOUR, player.getColor());
			rs.updateInt(PLAYER_POSITION_X, player.getSpace().x);
			rs.updateInt(PLAYER_POSITION_Y, player.getSpace().y);
			rs.updateInt(PLAYER_HEADING, player.getHeading().ordinal());
			rs.updateInt(CHECKPOINT, player.getCheckPoint());
			rs.insertRow();
		}

		rs.close();
	}

	/**
	 * This loads our player form the database
	 * @param game
	 * @throws SQLException
	 */
	private void loadPlayersFromDB(Board game) throws SQLException {
		PreparedStatement ps = getSelectPlayersASCStatement();
		ps.setInt(1, game.getGameId());
		
		ResultSet rs = ps.executeQuery();
		int i = 0;
		while (rs.next()) {
			int playerId = rs.getInt(PLAYER_PLAYERID);
			if (i++ == playerId) {
				// TODO this should be more defensive
				String name = rs.getString(PLAYER_NAME);
				String colour = rs.getString(PLAYER_COLOUR);
				Player player = new Player(game, colour ,name);
				game.addPlayer(player);
				
				int x = rs.getInt(PLAYER_POSITION_X);
				int y = rs.getInt(PLAYER_POSITION_Y);
				player.setSpace(game.getSpace(x,y));
				int heading = rs.getInt(PLAYER_HEADING);
				player.setHeading(Heading.values()[heading]);
				int checkpoint = rs.getInt(CHECKPOINT);
				player.setCheckPoint(checkpoint);

				// TODO  should also load players program and hand here
			} else {
				// TODO error handling
				System.err.println("Game in DB does not have a player with id " + i +"!");
			}
		}
		rs.close();
	}
	
	private void updatePlayersInDB(Board game) throws SQLException {
		PreparedStatement ps = getSelectPlayersStatementU();
		ps.setInt(1, game.getGameId());
		
		ResultSet rs = ps.executeQuery();
		while (rs.next()) {
			int playerId = rs.getInt(PLAYER_PLAYERID);
			// TODO should be more defensive
			Player player = game.getPlayer(playerId);
			// rs.updateString(PLAYER_NAME, player.getName()); // not needed: player's names does not change
			rs.updateInt(PLAYER_POSITION_X, player.getSpace().x);
			rs.updateInt(PLAYER_POSITION_Y, player.getSpace().y);
			rs.updateInt(PLAYER_HEADING, player.getHeading().ordinal());
			// TODO error handling
			// TODO take care of case when number of players changes, etc
			rs.updateRow();
		}
		rs.close();
		
		// TODO error handling/consistency check: check whether all players were updated
	}

	private static final String SQL_INSERT_GAME =
			"INSERT INTO Game(name, currentPlayer, phase, step, boardName) VALUES (?, ?, ?, ?, ?)";

	private PreparedStatement insert_game_stmt = null;

	private PreparedStatement getInsertGameStatementRGK() {
		if (insert_game_stmt == null) {
			Connection connection = connector.getConnection();
			try {
				insert_game_stmt = connection.prepareStatement(
						SQL_INSERT_GAME,
						Statement.RETURN_GENERATED_KEYS);
			} catch (SQLException e) {
				// TODO error handling
				e.printStackTrace();
			}
		}
		return insert_game_stmt;
	}

	private static final String SQL_SELECT_GAME =
			"SELECT * FROM Game WHERE gameID = ?";
	
	private PreparedStatement select_game_stmt = null;
	
	private PreparedStatement getSelectGameStatementU() {
		if (select_game_stmt == null) {
			Connection connection = connector.getConnection();
			try {
				select_game_stmt = connection.prepareStatement(
						SQL_SELECT_GAME,
						ResultSet.TYPE_FORWARD_ONLY,
					    ResultSet.CONCUR_UPDATABLE);
			} catch (SQLException e) {
				// TODO error handling
				e.printStackTrace();
			}
		}
		return select_game_stmt;
	}
		
	private static final String SQL_SELECT_PLAYERS =
			"SELECT * FROM Player WHERE gameID = ?";

	private PreparedStatement select_players_stmt = null;

	private PreparedStatement getSelectPlayersStatementU() {
		if (select_players_stmt == null) {
			Connection connection = connector.getConnection();
			try {
				select_players_stmt = connection.prepareStatement(
						SQL_SELECT_PLAYERS,
						ResultSet.TYPE_FORWARD_ONLY,
						ResultSet.CONCUR_UPDATABLE);
			} catch (SQLException e) {
				// TODO error handling
				e.printStackTrace();
			}
		}
		return select_players_stmt;
	}

	private static final String SQL_SELECT_PLAYERS_ASC =
			"SELECT * FROM Player WHERE gameID = ? ORDER BY playerID ASC";
	
	private PreparedStatement select_players_asc_stmt = null;
	
	private PreparedStatement getSelectPlayersASCStatement() {
		if (select_players_asc_stmt == null) {
			Connection connection = connector.getConnection();
			try {
				// This statement does not need to be updatable
				select_players_asc_stmt = connection.prepareStatement(
						SQL_SELECT_PLAYERS_ASC);
			} catch (SQLException e) {
				// TODO error handling
				e.printStackTrace();
			}
		}
		return select_players_asc_stmt;
	}
	
	private static final String SQL_SELECT_GAMES =
			"SELECT gameID, name FROM Game";
	
	private PreparedStatement select_games_stmt = null;
	
	private PreparedStatement getSelectGameIdsStatement() {
		if (select_games_stmt == null) {
			Connection connection = connector.getConnection();
			try {
				select_games_stmt = connection.prepareStatement(
						SQL_SELECT_GAMES);
			} catch (SQLException e) {
				// TODO error handling
				e.printStackTrace();
			}
		}
		return select_games_stmt;
	}

	private static final String SQL_SELECT_CARDS =
			"SELECT * FROM Cards WHERE gameID = ?";

	private PreparedStatement select_cards_stmt = null;

	private PreparedStatement getSelectCardsStatementU() {
		if(select_cards_stmt == null) {
			Connection connection = connector.getConnection();
			try {
				select_cards_stmt = connection.prepareStatement(
						SQL_SELECT_CARDS,
						ResultSet.TYPE_FORWARD_ONLY,
						ResultSet.CONCUR_UPDATABLE);
			} catch (SQLException e) {
				// TODO error handling
				e.printStackTrace();
			}
		}
		return select_cards_stmt;
	}

	private static final String SQL_SELECT_CARDS_ASC =
			"SELECT * FROM Cards WHERE gameID = ? ORDER BY playerID ASC";

	private PreparedStatement select_cards_asc_stmt = null;

	private PreparedStatement getSelectCardsASCStatement() {
		if (select_cards_asc_stmt == null) {
			Connection connection = connector.getConnection();
			try {
				// This statement does not need to be updatable
				select_cards_asc_stmt = connection.prepareStatement(
						SQL_SELECT_CARDS_ASC);
			} catch (SQLException e) {
				// TODO error handling
				e.printStackTrace();
			}
		}
		return select_cards_asc_stmt;
	}

}

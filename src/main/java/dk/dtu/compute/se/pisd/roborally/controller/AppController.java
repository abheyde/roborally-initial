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
package dk.dtu.compute.se.pisd.roborally.controller;

import dk.dtu.compute.se.pisd.designpatterns.observer.Observer;
import dk.dtu.compute.se.pisd.designpatterns.observer.Subject;

import dk.dtu.compute.se.pisd.roborally.RoboRally;

import dk.dtu.compute.se.pisd.roborally.fileaccess.LoadBoard;
import dk.dtu.compute.se.pisd.roborally.fileaccess.model.BoardTemplate;
import dk.dtu.compute.se.pisd.roborally.dal.GameInDB;
import dk.dtu.compute.se.pisd.roborally.dal.IRepository;
import dk.dtu.compute.se.pisd.roborally.dal.RepositoryAccess;
import dk.dtu.compute.se.pisd.roborally.model.Board;
import dk.dtu.compute.se.pisd.roborally.model.Player;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceDialog;
import org.jetbrains.annotations.NotNull;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * This class controllers the App, for newGame, saveGame, loadGame, stopGame and exit
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 */
public class AppController implements Observer {

    final private List<Integer> PLAYER_NUMBER_OPTIONS = Arrays.asList(2, 3, 4, 5, 6);
    final private List<String> PLAYER_COLORS = Arrays.asList("red", "green", "blue", "orange", "grey", "magenta");
    final private List<Integer> BOARDOPTIONS = Arrays.asList(1, 2, 3);
    final private RoboRally roboRally;

    private GameController gameController;

    public AppController(@NotNull RoboRally roboRally) {
        this.roboRally = roboRally;
    }

    public void newGame() {
        ChoiceDialog<Integer> dialog = new ChoiceDialog<>(PLAYER_NUMBER_OPTIONS.get(0), PLAYER_NUMBER_OPTIONS);
        dialog.setTitle("Player number");
        dialog.setHeaderText("Select number of players");
        Optional<Integer> result = dialog.showAndWait();

        ChoiceDialog<Integer> dialog2 = new ChoiceDialog<>(BOARDOPTIONS.get(0), BOARDOPTIONS);
        dialog2.setTitle("Board");
        dialog2.setHeaderText("Select Board");
        Optional<Integer> result2 = dialog2.showAndWait();
        String BoardSelect;
        switch (result2.get()) {
            case 1:
                BoardSelect = "board1";
                break;
            case 2:
                BoardSelect = "board2";
                break;
            default:
                BoardSelect = "defaultboard";
                break;

        }


        if (result.isPresent()) {
            if (gameController != null) {
                // The UI should not allow this, but in case this happens anyway.
                // give the user the option to save the game or abort this operation!
                if (!stopGame()) {
                    return;
                }
            }

            if (BoardSelect == "board1") {
                Board board = LoadBoard.loadBoard("board1");
                gameController = new GameController(board);
                gameController.board.boardName = BoardSelect;
                int no = result.get();
                for (int i = 0; i < no; i++) {
                    Player player = new Player(board, PLAYER_COLORS.get(i), "Player " + (i + 1));
                    board.addPlayer(player);
                    player.setSpace(board.getSpace(i % board.width, i));
                }
            } else if (BoardSelect == "board2"){
                Board board1 = LoadBoard.loadBoard("board2");
                gameController = new GameController(board1);
                gameController.board.boardName = BoardSelect;
                int no1 = result.get();
                for (int i = 0; i < no1; i++) {
                    Player player = new Player(board1, PLAYER_COLORS.get(i), "Player " + (i + 1));
                    board1.addPlayer(player);
                    player.setSpace(board1.getSpace(i % board1.width, i));
                }
            } else {
                Board board2 = LoadBoard.loadBoard("defaultboard");
                gameController = new GameController(board2);
                gameController.board.boardName = BoardSelect;
                int no1 = result.get();
                for (int i = 0; i < no1; i++) {
                    Player player = new Player(board2, PLAYER_COLORS.get(i), "Player " + (i + 1));
                    board2.addPlayer(player);
                    player.setSpace(board2.getSpace(i % board2.width, i));
                }
            }

//            // This creates a new game, with a specific board
//            Board board = LoadBoard.loadBoard("board1");
//            gameController = new GameController(board);
//            int no = result.get();
//            for (int i = 0; i < no; i++) {
//                Player player = new Player(board, PLAYER_COLORS.get(i), "Player " + (i + 1));
//                board.addPlayer(player);
//                player.setSpace(board.getSpace(i % board.width, i));
//            }

            // XXX: V2
            // board.setCurrentPlayer(board.getPlayer(0));
            gameController.startProgrammingPhase();

            roboRally.createBoardView(gameController);
        }
    }
//Mangler @
    public void saveGame() {
        IRepository repository = RepositoryAccess.getRepository();
        if (gameController.board.getGameId() == null) {
            repository.createGameInDB(gameController.board);
            System.out.println("New save, Current ID =" + gameController.board.getGameId());
        } else {
            repository.updateGameInDB(gameController.board);
            System.out.println("new updated ID = " + gameController.board.getGameId());
        }
    }

    public void loadGame() {
        IRepository repository = RepositoryAccess.getRepository();
        List<GameInDB> savedGames = repository.getGames();

        List<Integer> ids = new ArrayList<>();
        for (GameInDB tempGame : savedGames) {
            ids.add(tempGame.id);
        }

        ChoiceDialog<Integer> dialog = new ChoiceDialog<>(ids.get(0), ids);
        dialog.setTitle("Select Load File");
        dialog.setHeaderText("Load Game");
        Optional<Integer> result = dialog.showAndWait();

        System.out.println("Game Chosen = " + result.get());
        for (Integer k : ids) {
            System.out.println("Game ID:" + k);
        }
        //load saved game
        Board loadedBoard = repository.loadGameFromDB(result.get());

        //start loaded game
        gameController = new GameController(loadedBoard);
        roboRally.createBoardView(gameController);

    }


    /**
     * Stop playing the current game, giving the user the option to save
     * the game or to cancel stopping the game. The method returns true
     * if the game was successfully stopped (with or without saving the
     * game); returns false, if the current game was not stopped. In case
     * there is no current game, false is returned.
     *
     * @return true if the current game was stopped, false otherwise
     */
    public boolean stopGame() {
        if (gameController != null) {

            // here we save the game (without asking the user).
            //saveGame();

            gameController = null;
            roboRally.createBoardView(null);
            return true;
        }
        return false;
    }

    public void exit() {
        if (gameController != null) {
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("Exit RoboRally?");
            alert.setContentText("Are you sure you want to exit RoboRally?");
            Optional<ButtonType> result = alert.showAndWait();

            if (!result.isPresent() || result.get() != ButtonType.OK) {
                return; // return without exiting the application
            }
        }

        // If the user did not cancel, the RoboRally application will exit
        // after the option to save the game
        if (gameController == null || stopGame()) {
            Platform.exit();
        }
    }

   /*public void winCondition() {
        if (gameController != null) {
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle("WINNER!");
            alert.setContentText("YOU ARE A REAL ROBOKING");
            Optional<ButtonType> result = alert.show();

            if (!result.isPresent() || result.get() != ButtonType.OK) {
                return;
            }
        }
    }*/

        public boolean isGameRunning () {
            return gameController != null;
        }


        @Override
        public void update (Subject subject){
            // XXX do nothing for now
        }

    }
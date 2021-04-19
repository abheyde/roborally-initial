package dk.dtu.compute.se.pisd.roborally.controller;

import dk.dtu.compute.se.pisd.roborally.fileaccess.LoadBoard;
import dk.dtu.compute.se.pisd.roborally.model.Board;
import dk.dtu.compute.se.pisd.roborally.model.Heading;
import dk.dtu.compute.se.pisd.roborally.model.Player;
import dk.dtu.compute.se.pisd.roborally.model.Space;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GameControllerTest {

    private GameController gameController;

    @BeforeEach
    void setUp() {
        Board board = LoadBoard.loadBoard("board1");
        gameController = new GameController(board);
        for (int i = 0; i < 6; i++) {
            Player player = new Player(board, null, "Player " + i);
            board.addPlayer(player);
            player.setSpace(board.getSpace(i, i));
            player.setHeading(Heading.values()[i % Heading.values().length]);
        }
        board.setCurrentPlayer(board.getPlayer(0));
    }

    @AfterEach
    void tearDown() {

        gameController = null;
    }

//    @Test
//    void someTest() {
//        Board board = gameController.board;
//
//        Player player = board.getCurrentPlayer();
//        gameController.moveCurrentPlayerToSpace(board.getSpace(0, 4));
//
//        Assertions.assertEquals(player, board.getSpace(0, 4).getPlayer(), "Player " + player.getName() + " should beSpace (0,4)!");
//    }

    @Test
    void moveForward() {
        Board board = gameController.board;
        Player player = board.getPlayer(0);
        Player player1 = board.getPlayer(1);
        player1.setSpace(board.getSpace(0,1));
        gameController.moveForward(player);

        Assertions.assertEquals(player, board.getSpace(0, 1).getPlayer(), "Player " + player.getName() + " should be Space (0,1)!");
        Assertions.assertEquals(player1, board.getSpace(0, 2).getPlayer(), "Player " + player.getName() + " should be Space (0,2)!");
    }

    @Test
    void fastForward() {
        Board board = gameController.board;
        Player player = board.getCurrentPlayer();
        gameController.fastForward(player);

        Assertions.assertEquals(player, board.getSpace(0,2).getPlayer(),"Player" + player.getName() + " should be Space (0,2)!");
    }

    @Test
    void superfastForward(){
        Board board = gameController.board;
        Player player = board.getCurrentPlayer();
        gameController.superfastForward(player);

        Assertions.assertEquals(player, board.getSpace(0,3).getPlayer(),"Player" + player.getName() + " should be Space (0,3)!");
    }

    @Test
    void turn180(){
        Board board = gameController.board;
        Player player = board.getCurrentPlayer();
        gameController.turn180(player);

        Assertions.assertEquals(player.getHeading(), Heading.NORTH, player.getName() + "should be facing North!");
    }

    @Test
    void turn180Forward(){
        Board board = gameController.board;
        Player player = board.getCurrentPlayer();
        gameController.turn180Forward(player);

        Assertions.assertEquals(player.getHeading(),
                Heading.NORTH, player.getName() + "should be facing North!");
        Assertions.assertEquals(player,
                board.getSpace(0,9).getPlayer(),
                "Player " + player.getName() + " should be Space (0,11)!");
    }
    @Test
    void turnRight() {
        Board board = gameController.board;
        Player player = board.getCurrentPlayer();
        gameController.turnRight(player);

        Assertions.assertEquals(player.getHeading(), Heading.WEST, player.getName() + " should be facing West!");
    }

    @Test
    void turnLeft() {
        Board board = gameController.board;
        Player player = board.getCurrentPlayer();
        gameController.turnLeft(player);

        Assertions.assertEquals(player.getHeading(), Heading.EAST, player.getName() + " should be facing West!");
    }
    @Test
    void walls() {
        Board board = gameController.board;
        Player player = board.getCurrentPlayer();
        player.setSpace(board.getSpace(1,1));
        gameController.moveForward(player);

        Assertions.assertEquals(player, board.getSpace(1, 1).getPlayer(), "Player " + player.getName() + " should be Space (1,1)!");
    }
}
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

import dk.dtu.compute.se.pisd.roborally.RoboRally;
import dk.dtu.compute.se.pisd.roborally.model.*;
import dk.dtu.compute.se.pisd.roborally.view.RoboRallyMenuBar;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import jdk.nashorn.internal.runtime.AllocationStrategy;
import jdk.nashorn.internal.runtime.options.Option;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * This class controls the game.
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 * @author Jens Lindegaard, s205343@student.dtu.dk
 * @author Alexander Bak Heyde, s193576@studnet.dut.dk
 * @author Andreas Krone
 * @author Andreas Borg
 * @author Kim Randgaard
 */
public class GameController {

    final public Board board;

    public boolean winner = false;

    public GameController(@NotNull Board board) {
        this.board = board;
    }

//    /**
//     * This is just some dummy controller operation to make a simple move to see something
//     * happening on the board. This method should eventually be deleted!
//     *
//     * @param space the space to which the current player should move
//     */
//    public void moveCurrentPlayerToSpace(@NotNull Space space)  {
    // TODO Assignment V1: method should be implemented by the students:
    //   - the current player should be moved to the given space
    //     (if it is free()
    //   - and the current player should be set to the player
    //     following the current player
    //   - the counter of moves in the game should be increased by one
    //     if the player is moved
//
//        Player current = board.getCurrentPlayer();
//
//        if(space.getPlayer() == null)
//            current.setSpace(space);
//        else return;
//
//        int number = board.getPlayerNumber(current);
//        board.setCurrentPlayer(board.getPlayer(number + 1));
//        Player next = board.getPlayer((number + 1) % board.getPlayersNumber());
//        board.setCurrentPlayer(next);
//
//        board.setCounter(board.getCounter() + 1);
//    }

    // XXX: V2
    public void startProgrammingPhase() {
        board.setPhase(Phase.PROGRAMMING);
        board.setCurrentPlayer(board.getPlayer(0));
        board.setStep(0);

        for (int i = 0; i < board.getPlayersNumber(); i++) {
            Player player = board.getPlayer(i);
            if (player != null) {
                for (int j = 0; j < Player.NO_REGISTERS; j++) {
                    CommandCardField field = player.getProgramField(j);
                    field.setCard(null);
                    field.setVisible(true);
                }
                for (int j = 0; j < Player.NO_CARDS; j++) {
                    CommandCardField field = player.getCardField(j);
                    field.setCard(generateRandomCommandCard());
                    field.setVisible(true);
                }
            }
        }
    }

    // XXX: V2
    private CommandCard generateRandomCommandCard() {
        Command[] commands = Command.values();
        int random = (int) (Math.random() * commands.length);
        return new CommandCard(commands[random]);
    }

    // XXX: V2
    public void finishProgrammingPhase() {
        makeProgramFieldsInvisible();
        makeProgramFieldsVisible(0);
        board.setPhase(Phase.ACTIVATION);
        board.setCurrentPlayer(board.getPlayer(0));
        board.setStep(0);
    }

    // XXX: V2
    private void makeProgramFieldsVisible(int register) {
        if (register >= 0 && register < Player.NO_REGISTERS) {
            for (int i = 0; i < board.getPlayersNumber(); i++) {
                Player player = board.getPlayer(i);
                CommandCardField field = player.getProgramField(register);
                field.setVisible(true);
            }
        }
    }

    // XXX: V2
    private void makeProgramFieldsInvisible() {
        for (int i = 0; i < board.getPlayersNumber(); i++) {
            Player player = board.getPlayer(i);
            for (int j = 0; j < Player.NO_REGISTERS; j++) {
                CommandCardField field = player.getProgramField(j);
                field.setVisible(false);
            }
        }
    }

    // XXX: V2
    public void executePrograms() {
        board.setStepMode(false);
        continuePrograms();
    }

    // XXX: V2
    public void executeStep() {
        board.setStepMode(true);
        continuePrograms();
    }

    // XXX: V2
    private void continuePrograms() {
        do {
            executeNextStep();
        } while (board.getPhase() == Phase.ACTIVATION && !board.isStepMode());
    }

    // XXX: V2
    private void executeNextStep() {
        Player currentPlayer = board.getCurrentPlayer();
        if ((board.getPhase() == Phase.ACTIVATION || (board.getPhase() == Phase.PLAYER_INTERACTION && board.getUserChoice() != null))
                && currentPlayer != null) {
            int step = board.getStep();
            if (step >= 0 && step < Player.NO_REGISTERS) {
                Command userChoice = board.getUserChoice();
                if (userChoice != null) {
                    board.setUserChoice(null);
                    board.setPhase(Phase.ACTIVATION);
                    executeCommand(currentPlayer, userChoice);
                } else {
                    CommandCard card = currentPlayer.getProgramField(step).getCard();
                    if (card != null) {
                        Command command = card.command;
                        if (command.isInteractive()) {
                            board.setPhase(Phase.PLAYER_INTERACTION);
                            return;
                        }
                        executeCommand(currentPlayer, command);
                    }
                }
                int nextPlayerNumber = board.getPlayerNumber(currentPlayer) + 1;
                if (nextPlayerNumber < board.getPlayersNumber()) {
                    board.setCurrentPlayer(board.getPlayer(nextPlayerNumber));
                } else {
                    for (Player player : board.getPlayers()) {
                        for (FieldAction action : player.getSpace().getActions()) {
                            action.doAction(this, player.getSpace());
                        }
                        if (winner)
                            break;
                    }

                    step++;
                    if (step < Player.NO_REGISTERS) {
                        makeProgramFieldsVisible(step);
                        board.setStep(step);
                        board.setCurrentPlayer(board.getPlayer(0));
                    } else {
                        startProgrammingPhase();
                    }
                }
            } else {
                // this should not happen
                assert false;
            }
        } else {
            // this should not happen
            assert false;
        }
    }

    public void executeCommandOptionAndContinue(@NotNull Command option) {
        assert board.getPhase() == Phase.PLAYER_INTERACTION;
        assert board.getCurrentPlayer() != null;
        board.setUserChoice(option);
        continuePrograms();
    }

    private void executeCommand(@NotNull Player player, Command command) {
        if (player != null && player.board == board && command != null) {
            // XXX This is a very simplistic way of dealing with some basic cards and
            //     their execution. This should eventually be done in a more elegant way
            //     (this concerns the way cards are modelled as well as the way they are executed).

            switch (command) {
                case FORWARD:
                    this.moveForward(player);
                    break;
                case RIGHT:
                    this.turnRight(player);
                    break;
                case LEFT:
                    this.turnLeft(player);
                    break;
                case FAST_FORWARD:
                    this.fastForward(player);
                    break;
                case SUPER_FAST_FORWARD:
                    this.superfastForward(player);
                    break;
                case TURN_180:
                    this.turn180(player);
                    break;
                case TURN_180_FORWARD:
                    this.turn180Forward(player);
                    break;
                default:
                    // DO NOTHING (for now)
            }
        }
    }

    // TODO Assignment V2
    public void moveForward(@NotNull Player player) {
        if (player.board == board) {
            Space space = player.getSpace();
            Heading heading = player.getHeading();

            Space target = board.getNeighbour(space, heading);
            if (target != null) {
                try {
                    moveToSpace(player, target, heading);
                } catch (ImpossibleMoveException e) {
                    // we don't do anything here  for now; we just catch the
                    // exception so that we do no pass it on to the caller
                    // (which would be very bad style).
                }
            }
        }
    }

    // TODO Assignment V2
    public void fastForward(@NotNull Player player) {
        moveForward(player);
        moveForward(player);
    }

    // Assignment A3
    public void superfastForward(@NotNull Player player) {
        moveForward(player);
        moveForward(player);
        moveForward(player);
    }

    // Assignment A3
    public void turn180(@NotNull Player player) {
        turnRight(player);
        turnRight(player);
    }

    // Assigment A3
    public void turn180Forward(@NotNull Player player) {
        turn180(player);
        moveForward(player);
    }

    // TODO Assignment V2
    public void turnRight(@NotNull Player player) {
        if (player != null && player.board == board) {
            player.setHeading(player.getHeading().next());
        }

    }

    // TODO Assignment V2
    public void turnLeft(@NotNull Player player) {
        if (player != null && player.board == board) {
            player.setHeading(player.getHeading().prev());
        }

    }

    public boolean moveCards(@NotNull CommandCardField source, @NotNull CommandCardField target) {
        CommandCard sourceCard = source.getCard();
        CommandCard targetCard = target.getCard();
        if (sourceCard != null && targetCard == null) {
            target.setCard(sourceCard);
            source.setCard(null);
            return true;
        } else {
            return false;
        }
    }

    // XXX Assignment A3
    void moveToSpace(@NotNull Player player, @NotNull Space space, @NotNull Heading heading) throws ImpossibleMoveException {
        assert board.getNeighbour(player.getSpace(), heading) == space; // make sure the move to here is possible in principle
        Player other = space.getPlayer();
        if (other != null) {
            Space target = board.getNeighbour(space, heading);
            if (target != null) {
                // XXX Note that there might be additional problems with
                //     infinite recursion here (in some special cases)!
                //     We will come back to that!
                moveToSpace(other, target, heading);

                // Note that we do NOT embed the above statement in a try catch block, since
                // the thrown exception is supposed to be passed on to the caller

                assert space.getPlayer() == null : "Space to move to is not free: " + space; // make sure target is free now
            } else {
                throw new ImpossibleMoveException(player, space, heading);
            }
        }
        player.setSpace(space);
    }

    // Assignment A3
    class ImpossibleMoveException extends Exception {

        private Player player;
        private Space space;
        private Heading heading;

        public ImpossibleMoveException(Player player, Space space, Heading heading) {
            super("Move impossible");
            this.player = player;
            this.space = space;
            this.heading = heading;

            // Checks if the move is possible
        }
    }

    /**
     * This method is used to determine who won the game.
     * @param player
     */

    public void playerWon(Player player) {
        Alert winning = new Alert(Alert.AlertType.CONFIRMATION);
        winning.setTitle("WINNER WINNER CHICKEN DINNER!");
        String string = ("PLAYER " + player.getColor() + " YOU ARE A REAL ROBOKING!");
        winning.setContentText(string);
        this.winner = true;
        winning.showAndWait();

        Alert save = new Alert(Alert.AlertType.INFORMATION);
        save.setTitle("SAVE GAME?");
        save.setContentText("IF YOU WOULD LIKE TO SAVE THE GAME THEN PLEASE DO SO AFTER THIS MESSAGE");
        save.showAndWait();
    }

    /**
     * A method called when no corresponding controller operation is implemented yet. This
     * should eventually be removed.
     */
    public void notImplemented() {
        // XXX just for now to indicate that the actual method is not yet implemented
        assert false;
    }

}
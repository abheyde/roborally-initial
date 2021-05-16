package dk.dtu.compute.se.pisd.roborally.model;

import dk.dtu.compute.se.pisd.roborally.controller.FieldAction;
import dk.dtu.compute.se.pisd.roborally.controller.GameController;

/**
 * This class creates the checkpoint that we use to determine will win the game.
 * @author Jens Lindegaard, s205343@student.dtu.dk
 * @author Alexander Bak Heyde, s193576@student.dtu.dk
 * @author Andreas Krone Reichl, s205362@student.dtu.dk
 * @author Andreas Borg Kristensen, s205338@student.dtu.dk
 * @author Kim Michael Randgaard, s205341@student.dtu.dk
 */
public class Checkpoint extends FieldAction {

    public final int number;

    public Checkpoint(int number) {
        this.number = number;
    }

    @Override
    public boolean doAction(GameController gameController, Space space) {
        Player player = space.getPlayer();
        if (player != null) {
            player.setCheckPoint(this.number);
            if (player.getCheckPoint() >= gameController.board.getCheckpoints()) {
                gameController.playerWon(player);
            }
        }
        return true;
    }
}

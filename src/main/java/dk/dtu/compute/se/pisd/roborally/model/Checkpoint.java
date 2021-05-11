package dk.dtu.compute.se.pisd.roborally.model;

import dk.dtu.compute.se.pisd.roborally.controller.FieldAction;
import dk.dtu.compute.se.pisd.roborally.controller.GameController;

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

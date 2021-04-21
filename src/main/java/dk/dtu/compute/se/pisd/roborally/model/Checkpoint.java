package dk.dtu.compute.se.pisd.roborally.model;

import dk.dtu.compute.se.pisd.roborally.controller.FieldAction;
import dk.dtu.compute.se.pisd.roborally.controller.GameController;

public class Checkpoint extends FieldAction {

    public int number;

    public Checkpoint(int number) {
        this.number = number;
    }

    @Override
    public boolean doAction(GameController gameController, Space space) {
        Player player = space.getPlayer();
        if (player != null) {
            player.setCheckPoints(this.number);
            if (player.getCheckPoints() >= gameController.board.getCheckpoints().size()) {
                gameController.playerWon(player);
            }
        }
        return true;
    }
}

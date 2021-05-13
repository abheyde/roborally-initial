package dk.dtu.compute.se.pisd.roborally.model;

import dk.dtu.compute.se.pisd.roborally.controller.FieldAction;
import dk.dtu.compute.se.pisd.roborally.controller.GameController;


/**
 * This class is where we create the gear.
 *
 * @author Jens Lindegaard, s205343@student.dtu.dk
 * @author Alexander Bak Heyde, s193576@studnet.dut.dk
 * @author Andreas Krone
 * @author Andreas Borg
 * @author Kim Randgaard
 *
 */

enum Rotation {
    LEFT, RIGHT
}

public class Gear extends FieldAction {
    public Rotation rotation;

    public Gear (Rotation rotation) {
        this.rotation = rotation;
    }


    /**
     * Here the gear rotates a player either left or right.
     * @param gameController the gameController of the respective game
     * @param space the space this action should be executed for
     * @return
     */
    @Override
    public boolean doAction (GameController gameController, Space space) {

        Player player = space.getPlayer();

        switch (rotation) {
            case LEFT:
                gameController.turnLeft(player);
                break;

            case RIGHT:
                gameController.turnRight(player);
                break;
        }
        return true;
    }
}

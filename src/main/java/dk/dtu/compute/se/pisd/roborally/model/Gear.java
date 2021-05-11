package dk.dtu.compute.se.pisd.roborally.model;

import dk.dtu.compute.se.pisd.roborally.controller.FieldAction;
import dk.dtu.compute.se.pisd.roborally.controller.GameController;


/**
 * ...
 *
 * @author @author Alexander Bak Heyde, s193576@studnet.dtu.dk
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

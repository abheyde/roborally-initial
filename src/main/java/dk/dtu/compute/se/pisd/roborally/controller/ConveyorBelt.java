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

import dk.dtu.compute.se.pisd.roborally.model.Board;
import dk.dtu.compute.se.pisd.roborally.model.Heading;
import dk.dtu.compute.se.pisd.roborally.model.Player;
import dk.dtu.compute.se.pisd.roborally.model.Space;
import org.jetbrains.annotations.NotNull;

/**
 * This class is making the conveyorbelt.
 * We know that is should be under the model package, but got an error when trying to refactor
 * @author Ekkart Kindler, ekki@dtu.dk
 * @author Andreas Krone Reichl, s205362@student.dtu.dk
 */
public class ConveyorBelt extends FieldAction {

    private Heading heading;

    public Heading getHeading() {
        return heading;
    }

    public void setHeading(Heading heading) {
        this.heading = heading;
    }
    @Override
    public boolean doAction(@NotNull GameController gameController, @NotNull Space space) {
        Player currentPlayer = space.getPlayer();
        Space neighbourSpace = space.getNeighbourSpace(this.heading);

        currentPlayer.setHeading(this.heading);

        if (neighbourSpace.getPlayer() != null) {
            return false;
        } else {

            currentPlayer.setSpace(neighbourSpace);

            for (FieldAction action : neighbourSpace.actions) {
                if (action instanceof ConveyorBelt && ((ConveyorBelt) action).getHeading() != this.heading.opposite()) {
                    action.doAction(gameController, currentPlayer.getSpace());
                }
            }

            return true;
        }

    }
}

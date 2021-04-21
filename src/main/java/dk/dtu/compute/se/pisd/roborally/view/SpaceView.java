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
package dk.dtu.compute.se.pisd.roborally.view;

import dk.dtu.compute.se.pisd.designpatterns.observer.Subject;
import dk.dtu.compute.se.pisd.roborally.controller.ConveyorBelt;
import dk.dtu.compute.se.pisd.roborally.controller.FieldAction;
import dk.dtu.compute.se.pisd.roborally.model.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeLineCap;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.css.Rect;

/**
 * ...
 *
 * @author Ekkart Kindler, ekki@dtu.dk
 * @author Alexander Bak Heyde, s3576@student.dtu.dk
 * @author Jens Lindegaard, s205343@student.dtu.dk
 * @author Krone
 */
public class SpaceView extends StackPane implements ViewObserver {

    final public static int SPACE_HEIGHT = 75; // 60; // 75;
    final public static int SPACE_WIDTH = 75;  // 60; // 75;

    public final Space space;


    public SpaceView(@NotNull Space space) {
        this.space = space;

        // XXX the following styling should better be done with styles
        this.setPrefWidth(SPACE_WIDTH);
        this.setMinWidth(SPACE_WIDTH);
        this.setMaxWidth(SPACE_WIDTH);

        this.setPrefHeight(SPACE_HEIGHT);
        this.setMinHeight(SPACE_HEIGHT);
        this.setMaxHeight(SPACE_HEIGHT);



        if ((space.x + space.y) % 2 == 0) {
            this.setStyle("-fx-background-color: white;");
        } else {
            this.setStyle("-fx-background-color: black;");
        }
        // updatePlayer();

        // This space view should listen to changes of the space
        space.attach(this);
        update(space);
    }

    private void updatePlayer() {

        Player player = space.getPlayer();
        if (player != null) {
            Polygon arrow = new Polygon(0.0, 0.0,
                    10.0, 20.0,
                    20.0, 0.0);
            try {
                arrow.setFill(Color.valueOf(player.getColor()));
            } catch (Exception e) {
                arrow.setFill(Color.MEDIUMPURPLE);
            }

            arrow.setRotate((90 * player.getHeading().ordinal()) % 360);
            this.getChildren().add(arrow);
        }
    }

    public void updateWalls(){
        Space space = this.space;
        if (space != null && !space.getWalls().isEmpty()) {
            for (Heading wall : space.getWalls()) {

                Canvas canvas = new Canvas(SPACE_WIDTH, SPACE_HEIGHT);
                GraphicsContext gc = canvas.getGraphicsContext2D();
                gc.setStroke(Color.RED);
                gc.setLineWidth(5);
                gc.setLineCap(StrokeLineCap.ROUND);
                this.getChildren().add(canvas);

                switch(wall) {
                    case SOUTH:
                        gc.strokeLine(SPACE_HEIGHT-2, 73, 0, 73);
                        break;

                    case WEST:
                        gc.strokeLine( 2, SPACE_HEIGHT-2, 2, 2);
                        break;

                    case NORTH:
                        gc.strokeLine(2,2,SPACE_HEIGHT-2,2);
                        break;

                    case EAST:
                        gc.strokeLine(73,0,73,SPACE_HEIGHT-2);
                        break;
                }
            }
        }
    }

    private void updateBelt() {
        ConveyorBelt belt = space.getConveyorBelt();
        if (belt != null){
            Polygon fig = new Polygon(0.0,0.0,60.0,0.0,30.0,60.0);

            try {
                fig.setFill(Color.LIGHTPINK);
            } catch (Exception e) {
                fig.setFill(Color.CHOCOLATE);
            }

            fig.setRotate((90 * belt.getHeading().ordinal()) % 360);
            this.getChildren().add(fig);
        }

    }

private void updateGears() {
    for (FieldAction action : space.actions) {
        if (action instanceof Gear) {

            Rectangle fig1 = new Rectangle(50,50);

            try {
                fig1.setFill(Color.GREENYELLOW);
            } catch (Exception e) {
                fig1.setFill(Color.GREY);
            }

            this.getChildren().add(fig1);
        }
    }
}

    private void updateCheckpoints() {
        for (FieldAction action : space.actions) {
            if (action instanceof Checkpoint) {

                Polygon fig2 = new Polygon(0.0, 0.0, 40.0, 0.0, 40.0, 40.0);

                try {
                    fig2.setFill(Color.TURQUOISE);
                } catch (Exception e) {
                    fig2.setFill(Color.MEDIUMORCHID);
                }

                this.getChildren().add(fig2);
            }
        }
    }

    @Override
    public void updateView(Subject subject) {
        if (subject == this.space) {
            this.getChildren().clear();

            updateBelt();
            updateWalls();
            updateGears();
            updateCheckpoints();
            updatePlayer();
        }
    }
}

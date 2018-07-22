package org.astormofminds.icfpc2018.solver;

import org.astormofminds.icfpc2018.model.*;
import org.astormofminds.icfpc2018.solver.exceptions.SolverNotInitializedException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class MultiVoid implements Solver {

    private Matrix currentMatrix = null;
    private List<Command> result;
    private int posx = 0;
    private int posy = 0;
    private int posz = 0;
    private boolean highHarmonic;

    @Override
    public boolean initAssemble(Matrix matrix) {
        return false;
    }

    @Override
    public boolean initDeconstruct(Matrix matrix) {
        currentMatrix = matrix;
        return true;
    }

    @Override
    public boolean initReconstruct(Matrix source, Matrix target) {
        return false;
    }

    @Override
    public List<Command> getCompleteTrace() {
        if (currentMatrix == null) throw new SolverNotInitializedException();
        int resolution = currentMatrix.getResolution();
        result = new ArrayList<>(2 * resolution * resolution * resolution);
        if (resolution < 3) {
            result.add(Command.HALT);
            return result;
        }

        Region box = currentMatrix.getBoundingBox();
        int xmin = box.getMinX();
        int zmin = box.getMinZ();
        int xmax = box.getMaxX();
        int ymax = box.getMaxY();
        int zmax = box.getMaxZ();

        //move to starting point
        while (posx < xmin) {
            int steps = Math.min(xmin - posx, 15);
            result.add(Command.sMove(Difference.of(steps, 0, 0)));
            posx += steps;
        }
        //move one higher up, because we eat from above
        while (posy < ymax + 1) {
            int steps = Math.min(ymax + 1 - posy, 15);
            result.add(Command.sMove(Difference.of(0, steps, 0)));
            posy += steps;
        }
        while (posz < zmin) {
            int steps = Math.min(zmin - posz, 15);
            result.add(Command.sMove(Difference.of(0, 0, steps)));
            posz += steps;
        }

        highHarmonic = false;

        //void all layers
        boolean moveAway = true;
        boolean moveRight = true;
        for (int y = ymax + 1; y > 0; y--) {
            if (moveAway) {
                for (int z = zmin; z <= zmax; z++) {
                    if (moveRight) {
                        voidRowToRight(xmin, xmax, y, z);
                    } else {
                        voidRowToLeft(xmin, xmax, y, z);
                    }
                    moveRight = !moveRight;
                    //move one away, unless it is the last run through
                    if (z < zmax) {
                        moveFar();
                    }
                }
            } else {
                for (int z = zmax; z >= zmin; z--) {
                    if (moveRight) {
                        voidRowToRight(xmin, xmax, y, z);
                    } else {
                        voidRowToLeft(xmin, xmax, y, z);
                    }
                    moveRight = !moveRight;
                    //move one here, unless it is the last run through
                    if (z > zmin) {
                        moveNear();
                    }
                }
            }
            // move one up, unless it is the last run through
            if (y > 1) {
                moveDown();
            }
            moveAway = !moveAway;
        }

        //return home
        while (posx > 0) {
            int steps = Math.min(15, posx);
            result.add(Command.sMove(Difference.of(-steps, 0, 0)));
            posx -= steps;
        }
        while (posz > 0) {
            int steps = Math.min(15, posz);
            result.add(Command.sMove(Difference.of(0, 0, -steps)));
            posz -= steps;
        }
        while (posy > 0) {
            int steps = Math.min(15, posy);
            result.add(Command.sMove(Difference.of(0, -steps, 0)));
            posy -= steps;
        }

        //end finally, stop
        result.add(Command.HALT);

        //optimize movement
        Optimizer.removeMoves(result);
        Optimizer.combineStraightMoves(result);

        return result;
    }

    private void voidRowToLeft(int xmin, int xmax, int y, int z) {
        for (int x = xmax; x >= xmin; x--) {
            voidIfRequired(x, y, z);
            //move left, unless it is the last run through
            if (x > xmin) {
                moveLeft();
            }
        }
    }

    private void voidRowToRight(int xmin, int xmax, int y, int z) {
        for (int x = xmin; x <= xmax; x++) {
            voidIfRequired(x, y, z);
            //move to right, unless it is the last run through
            if (x < xmax) {
                moveRight();
            }
        }
    }

    private void voidIfRequired(int x, int y, int z) {
        Coordinate underUs = Coordinate.of(x, y - 1, z);
        if (currentMatrix.isFull(underUs)) {
            Set<Coordinate> toVoid = new HashSet<>();
            toVoid.add(underUs);
            if (x> 0 && y > 0) {
                Coordinate downLeft = Coordinate.of(x - 1, y - 1, z);
                if (currentMatrix.isFull(downLeft)) toVoid.add(downLeft);
            }
            if (y > 0 && x < currentMatrix.getResolution() - 1) {
                Coordinate downRight = Coordinate.of(x + 1, y - 1, z);
                if (currentMatrix.isFull(downRight)) toVoid.add(downRight);
            }
            if (y > 0 && z < currentMatrix.getResolution() - 1) {
                Coordinate downFar = Coordinate.of(x, y - 1, z + 1);
                if (currentMatrix.isFull(downFar)) toVoid.add(downFar);
            }
            if (y > 0 && z > 0) {
                Coordinate downNear = Coordinate.of(x, y - 1, z - 1);
                if (currentMatrix.isFull(downNear)) toVoid.add(downNear);
            }

            for (Coordinate current : toVoid) {
                currentMatrix.unfill(current);
                boolean flipDown = false;
                boolean flipUp = false;
                if (highHarmonic) {
                    //check whether we are all grounded and can flip down after this fill
                    if (currentMatrix.allGrounded()) {
                        flipDown = true;
                    }
                } else {
                    if (!currentMatrix.allGrounded()) {
                        flipUp = true;
                    }
                }
                if (flipUp) {
                    result.add(Command.FLIP);
                    highHarmonic = true;
                }
                result.add(Command.void_(Difference.of(current.getX() - x, current.getY() - y, current.getZ() - z)));
                if (flipDown) {
                    result.add(Command.FLIP);
                    highHarmonic = false;
                }
            }
        }
    }

    private void moveDown() {
        result.add(Command.DOWN);
        posy--;
    }

    private void moveFar() {
        result.add(Command.FAR);
        posz++;
    }

    private void moveNear() {
        result.add(Command.NEAR);
        posz--;
    }

    private void moveLeft() {
        result.add(Command.LEFT);
        posx--;
    }

    private void moveRight() {
        result.add(Command.RIGHT);
        posx++;
    }
}

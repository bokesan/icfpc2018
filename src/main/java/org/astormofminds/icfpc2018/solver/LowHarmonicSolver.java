package org.astormofminds.icfpc2018.solver;

import org.astormofminds.icfpc2018.model.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LowHarmonicSolver implements Solver {

    protected Matrix targetMatrix = null;
    protected Matrix currentMatrix = null;
    protected List<Command> result;
    private int posx = 0;
    private int posy = 0;
    private int posz = 0;
    protected boolean highHarmonic;
    protected Set<Coordinate> floating;

    @Override
    public boolean init(Matrix matrix) {
        this.targetMatrix = matrix;
        return true;
    }

    @Override
    public List<Command> getCompleteTrace() {
        if (targetMatrix == null) throw new SolverNotInitializedException();
        int resolution = targetMatrix.getResolution();
        result = new ArrayList<>(2 * resolution * resolution * resolution);
        if (resolution < 3) {
            result.add(Command.HALT);
            return result;
        }

        Region box = targetMatrix.getBoundingBox();
        int xmin = Math.min(box.getC1().getX(), box.getC2().getX());
        int ymin = Math.min(box.getC1().getY(), box.getC2().getY());
        int zmin = Math.min(box.getC1().getZ(), box.getC2().getZ());
        int xmax = Math.max(box.getC1().getX(), box.getC2().getX());
        int ymax = Math.max(box.getC1().getY(), box.getC2().getY());
        int zmax = Math.max(box.getC1().getZ(), box.getC2().getZ());

        //move to starting point
        for (int x = 0; x < xmin; x++) {
            moveRight();
        }
        //move one higher up, because we build from over the voxel
        for (int y = 0; y < ymin + 1; y++) {
            moveUp();
        }
        for (int z = 0; z < zmin; z++) {
            moveFar();
        }

        currentMatrix = new Matrix(targetMatrix.getResolution());
        highHarmonic = false;
        floating = new HashSet<>();

        //print all layers
        for (int y = ymin + 1; y < ymax + 2; y++) {
            //on each layer, move from near to far on odd layers, omitting the last row
            if ((y - ymin) % 2 == 1) {
                for (int z = zmin; z <= zmax; z++) {
                    //in each row, move right on odd rows and left in even ones
                    if ((z - zmin) % 2 == 0) {
                        //odd row - move right
                        fillRowToRight(xmin, xmax, y, z);
                    } else {
                        //even row, move left
                        fillRowToLeft(xmin, xmax, y, z);
                    }
                    //move one away, unless it is the last run through
                    if (z < zmax) {
                        moveFar();
                    }
                }
                // move one up, unless it is the last run through
                if (y < ymax + 1) {
                    moveUp();
                }
            } else {
                // move from far to near on even layers
                for (int z = zmax; z >= zmin; z--) {
                    //in each row, move right on even rows and left in odd ones
                    if ((z - zmin) % 2 == 1) {
                        //odd row - move right
                        fillRowToRight(xmin, xmax, y, z);
                    } else {
                        //even row, move left
                        fillRowToLeft(xmin, xmax, y, z);
                    }
                    //move one here, unless it is the last run through
                    if (z > zmin) {
                        moveNear();
                    }
                }
                // move one up, unless it is the last run through
                if (y < ymax + 1) {
                    moveUp();
                }
            }
        }

        //return home
        while (posz > 0) moveNear();
        while (posx > 0) moveLeft();
        while (posy > 0) moveDown();

        //end finally, stop
        result.add(Command.HALT);

        //optimize movement
        Optimizer.removeMoves(result);
        Optimizer.combineStraightMoves(result);

        return result;
    }

    private void fillRowToLeft(int xmin, int xmax, int y, int z) {
        for (int x = xmax; x >= xmin; x--) {
            fillIfRequired(y, z, x);
            //move left, unless it is the last run through
            if (x > xmin) {
                moveLeft();
            }
        }
    }

    private void fillRowToRight(int xmin, int xmax, int y, int z) {
        for (int x = xmin; x <= xmax; x++) {
            fillIfRequired(y, z, x);
            //move to right, unless it is the last run through
            if (x < xmax) {
                moveRight();
            }
        }
    }

    protected void fillIfRequired(int y, int z, int x) {
        Coordinate underUs = Coordinate.of(x, y - 1, z);
        if (targetMatrix.get(underUs) == VoxelState.FULL) {
            currentMatrix.fill(underUs);
            boolean flipDown = false;
            boolean flipUp = false;
            if (highHarmonic) {
                //check whether we are all grounded and can flip down after this fill
                if (currentMatrix.isGrounded(underUs)) {
                    flipDown = true;
                    for (Coordinate c : floating) {
                        if (!currentMatrix.isGrounded(c)) {
                            flipDown = false;
                            break;
                        }
                    }
                } else {
                    floating.add(underUs);
                }
            } else {
                //check whether we fill ungrounded and need to flip up before this fill
                if (!currentMatrix.isGrounded(underUs)) {
                    flipUp = true;
                }
            }
            if (flipUp) {
                result.add(Command.FLIP);
                highHarmonic = true;
                floating.add(underUs);
            }
            result.add(Command.fill(Difference.of(0, -1, 0)));
            if (flipDown) {
                result.add(Command.FLIP);
                highHarmonic = false;
                floating = new HashSet<>();
            }
        }
    }

    private void moveDown() {
        result.add(Command.DOWN);
        posy--;
    }

    private void moveUp() {
        result.add(Command.UP);
        posy++;
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

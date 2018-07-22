package org.astormofminds.icfpc2018.solver;

import org.astormofminds.icfpc2018.model.*;
import org.astormofminds.icfpc2018.solver.exceptions.SolverNotInitializedException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Swarm60Solver implements Solver {

    private Matrix targetMatrix = null;
    private Matrix currentMatrix = null;
    protected List<Command> result;
    private int posx = 0;
    private int posy = 0;
    private int posz = 0;
    private boolean harmonicHigh;
    private boolean switchOff;
    private Set<Coordinate> fresh;
    private Set<Coordinate> floating;

    @Override
    public boolean initAssemble(Matrix matrix) {
        this.targetMatrix = matrix;
        return true;
    }

    @Override
    public boolean initDeconstruct(Matrix matrix) {
        return false;
    }

    @Override
    public boolean initReconstruct(Matrix source, Matrix target) {
        return false;
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
        int xmin = box.getMinX();
        int ymin = box.getMinY();
        int zmin = box.getMinZ();
        int xmax = box.getMaxX();
        int ymax = box.getMaxY();
        int zmax = box.getMaxZ();

        //lets start by only caring about those where we can get away with one long line of bots
        if (xmax - xmin > 120) {
            result.add(Command.HALT);
            return result;
        }

        //move to starting point and one further right - we cover more ground
        for (int x = 0; x <= xmin; x++) {
            moveRight();
        }
        //move one higher up, because we build from over the voxel
        for (int y = 0; y < ymin + 1; y++) {
            moveUp(1);
        }
        for (int z = 0; z < zmin; z++) {
            moveFar(1);
        }

        currentMatrix = new Matrix(targetMatrix.getResolution());
        harmonicHigh = false;
        switchOff = false;
        fresh = new HashSet<>();
        floating = new HashSet<>();

        //spawn bots
        int numBotsToSpawn = (xmax - xmin + 1) / 3;
        //if we cover it perfectly, we subtract on for the existing bot
        if ((xmax - xmin + 1) % 3 == 0) numBotsToSpawn--;
        if (numBotsToSpawn > 39) {
            result.add(Command.HALT);
            return result;
        }

        for (int i = 1; i <= numBotsToSpawn; i++) {
            for (int j = 1; j < i; j++) {
                //let the left bots wait while we spawn at the right
                addWait();
            }
            //spawn a new one
            result.add(Command.fission(Difference.of(1, 0, 0), 39 - i));

            //let the left ones wait while the new one moves right
            for (int j = 0; j < i; j++) {
                //let the left bots wait while we spawn at the right
                addWait();
            }
            result.add(Command.sMove(Difference.of(2, 0, 0)));
        }
        int numBots = numBotsToSpawn + 1;

        //print all layers
        for (int y = ymin + 1; y < ymax + 2; y++) {
            //on each layer, move from near to far on odd layers, omitting the last row
            if ((y - ymin) % 2 == 1) {
                for (int z = zmin; z <= zmax; z++) {
                    allFillBelow(numBots);
                    //move one away, unless it is the last run through
                    if (z < zmax) {
                        moveFar(numBots);
                    }
                }
                // move one up, unless it is the last run through
                if (y < ymax + 1) {
                    moveUp(numBots);
                }
            } else {
                // move from far to near on even layers
                for (int z = zmax; z >= zmin; z--) {
                    allFillBelow(numBots);
                    //move one here, unless it is the last run through
                    if (z > zmin) {
                        moveNear(numBots);
                    }
                }
                // move one up, unless it is the last run through
                if (y < ymax + 1) {
                    moveUp(numBots);
                }
            }
        }

        //merge all bots through fusion
        for (int i = numBots; i > 1; i--) {
            //let the left ones wait while the right one moves left
            for (int j = 1; j < i; j++) {
                //let the left bots wait while we move the right one
                addWait();
            }
            result.add(Command.sMove(Difference.of(-2, 0, 0)));

            for (int j = 2; j < i; j++) {
                //let the left bots wait while we fusion at the right
                addWait();
            }
            //combine two
            result.add(Command.fusionP(Difference.of(1, 0, 0)));
            result.add(Command.fusionS(Difference.of(-1, 0, 0)));
        }

        //return home
        while (posz > 0) moveNear(1);
        while (posy > 0) moveDown(1);
        while (posx > 0) moveLeft();
        //end finally, stop
        result.add(Command.HALT);

        return result;
    }

    private void addWait() {
        if (switchOff) {
            result.add(Command.FLIP);
            switchOff = false;
            harmonicHigh = false;
        } else {
            result.add(Command.WAIT);
        }
    }

    private void allFillBelow(int numBots) {
        for (int i = 0; i < numBots; i++) {
            int x = posx + i * 3;
            fillIfRequiredLeft(x - 1, posy - 1, posz);
        }
        checkHarmonic(numBots);
        for (int i = 0; i < numBots; i++) {
            int x = posx + i * 3;
            fillIfRequiredCenter(x, posy - 1, posz);
        }
        checkHarmonic(numBots);
        for (int i = 0; i < numBots; i++) {
            int x = posx + i * 3;
            fillIfRequiredRight(x + 1, posy - 1, posz);
        }
        checkHarmonic(numBots);
    }

    private void checkHarmonic(int numbots) {
        Set<Coordinate> floats = new HashSet<>();
        for (Coordinate c : fresh) {
            if (!currentMatrix.isGrounded(c)) floats.add(c);
        }
        for (Coordinate c : floating) {
            if (!currentMatrix.isGrounded(c)) floats.add(c);
        }
        floating.clear();
        fresh.clear();
        floating.addAll(floats);

        if (floating.isEmpty()) {
            if (harmonicHigh) switchOff = true;
        } else {
            switchOff = false;
            if (!harmonicHigh) {
                harmonicHigh = true;
                // we look back, but not further than the tick before this
                for (int i = result.size() - 1; i >= result.size() - (2 * numbots); i--) {
                    if (result.get(i).equals(Command.WAIT)) {
                        result.remove(i);
                        result.add(i, Command.FLIP);
                        return;
                    }
                }
                // nothing found to replace - we inject a waiting + flipping round before the current tick
                int index = result.size() - numbots;
                for (int i = 1; i < numbots; i++) {
                    result.add(index, Command.WAIT);
                }
                result.add(index, Command.FLIP);
            }
        }
    }

    private void fillIfRequiredLeft(int x, int y, int z) {
        Coordinate toFill = Coordinate.of(x, y , z);
        if (targetMatrix.get(toFill) == VoxelState.FULL && currentMatrix.get(toFill) == VoxelState.VOID) {
            currentMatrix.fill(toFill);
            fresh.add(toFill);
            result.add(Command.fill(Difference.of(-1, -1, 0)));
        } else {
            addWait();
        }
    }

    private void fillIfRequiredCenter(int x, int y, int z) {
        Coordinate toFill = Coordinate.of(x, y , z);
        if (targetMatrix.get(toFill) == VoxelState.FULL && currentMatrix.get(toFill) == VoxelState.VOID) {
            currentMatrix.fill(toFill);
            fresh.add(toFill);
            result.add(Command.fill(Difference.of(0, -1, 0)));
        } else {
            addWait();
        }
    }

    private void fillIfRequiredRight(int x, int y, int z) {
        Coordinate toFill = Coordinate.of(x, y , z);
        if (targetMatrix.get(toFill) == VoxelState.FULL && currentMatrix.get(toFill) == VoxelState.VOID) {
            currentMatrix.fill(toFill);
            fresh.add(toFill);
            result.add(Command.fill(Difference.of(1, -1, 0)));
        } else {
            addWait();
        }
    }

    private void moveDown(int numbots) {
        for (int i = 0; i < numbots; i++) {
            result.add(Command.DOWN);
        }
        posy--;
    }

    private void moveUp(int numbots) {
        for (int i = 0; i < numbots; i++) {
            result.add(Command.UP);
        }
        posy++;
    }

    private void moveFar(int numbots) {
        for (int i = 0; i < numbots; i++) {
            result.add(Command.FAR);
        }
        posz++;
    }

    private void moveNear(int numbots) {
        for (int i = 0; i < numbots; i++) {
            result.add(Command.NEAR);
        }
        posz--;
    }

    private void moveRight() {
        result.add(Command.RIGHT);
        posx++;
    }

    private void moveLeft() {
        result.add(Command.LEFT);
        posx--;
    }
}

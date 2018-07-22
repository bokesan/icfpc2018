package org.astormofminds.icfpc2018.solver;

import org.astormofminds.icfpc2018.model.*;

import java.util.ArrayList;
import java.util.List;

public class EatingSwarm implements Solver {

    private Matrix currentMatrix = null;
    protected List<Command> result;
    private int posx = 0;
    private int posy = 0;
    private int posz = 0;
    private boolean harmonicHigh;
    private boolean switchOff;
    private boolean hasEaten = false;

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

        //lets start by only caring about those where we can get away with one long line of bots
        if (xmax - xmin > 120) {
            result.add(Command.HALT);
            return result;
        }

        //move to starting point and one further right - we cover more ground
        while (posx < xmin + 1) {
            int steps = Math.min(xmin + 1 - posx, 15);
            result.add(Command.sMove(Difference.of(steps, 0, 0)));
            posx += steps;
        }
        //move one lower up, because we eat from the inside
        while (posy < ymax - 1) {
            int steps = Math.min(ymax - 1 - posy, 15);
            result.add(Command.sMove(Difference.of(0, steps, 0)));
            posy += steps;
        }
        while (posz < zmin - 1) {
            int steps = Math.min(zmin - 1 - posz, 15);
            result.add(Command.sMove(Difference.of(0, 0, steps)));
            posz += steps;
        }

        harmonicHigh = false;
        switchOff = false;

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

        //eat all layers starting by going in far direction
        boolean moveAway = true;
        for (int y = ymax - 1; y >= -1; y -= 3) {
            if (y == -1) y = 0;
            if (moveAway) {
                for (int z = zmin -1; z <= zmax + 1; z++) {
                    eatAll(numBots, 1);
                    //move one away, unless it is the last run through
                    if (z < zmax + 1) {
                        moveFar(numBots);
                    }
                }
            } else {
                for (int z = zmax + 1; z >= zmin - 1; z--) {
                    eatAll(numBots, -1);
                    //move one here, unless it is the last run through
                    if (z > zmin - 1) {
                        moveNear(numBots);
                    }
                }
            }
            // move 3 down, unless fast run or ground too near
            if (y > 2) {
                moveDown(numBots, 3);
            } else if (y == 2) {
                moveDown(numBots, 2);
            }
            //change direction
            moveAway = !moveAway;
            //if we can swith off high harmonic and have not yet, do it now
            if (switchOff) {
                switchOff = false;
                harmonicHigh = false;
                result.add(Command.FLIP);
                for (int i = 1; i < numBots; i++) {
                    result.add(Command.WAIT);
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
        while (posx > 0) {
            int steps = Math.min(15, posx);
            result.add(Command.sMove(Difference.of(-steps, 0, 0)));
            posx -= steps;
        }
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

    private void eatAll(int numBots, int zEat) {
        //fields above
        for (int i = 0; i < numBots; i++) {
            int x = posx + i * 3;
            voidIfRequired(x - 1, posy + 1, posz, -1, 1, 0);
        }
        checkHarmonic(numBots);
        for (int i = 0; i < numBots; i++) {
            int x = posx + i * 3;
            voidIfRequired(x, posy + 1, posz, 0, 1, 0);
        }
        checkHarmonic(numBots);
        for (int i = 0; i < numBots; i++) {
            int x = posx + i * 3;
            voidIfRequired(x + 1, posy + 1, posz, 1, 1, 0);
        }
        checkHarmonic(numBots);

        //eat fields before us
        for (int i = 0; i < numBots; i++) {
            int x = posx + i * 3;
            voidIfRequired(x - 1, posy, posz, -1, 0, 0);
        }
        checkHarmonic(numBots);
        for (int i = 0; i < numBots; i++) {
            int x = posx + i * 3;
            voidIfRequired(x, posy + 1, posz + zEat, 0, 1, zEat);
        }
        checkHarmonic(numBots);
        for (int i = 0; i < numBots; i++) {
            int x = posx + i * 3;
            voidIfRequired(x, posy, posz + zEat, 0, 0, zEat);
        }
        checkHarmonic(numBots);
        for (int i = 0; i < numBots; i++) {
            int x = posx + i * 3;
            voidIfRequired(x, posy - 1, posz + zEat, 0, -1, zEat);
        }
        checkHarmonic(numBots);
        for (int i = 0; i < numBots; i++) {
            int x = posx + i * 3;
            voidIfRequired(x + 1, posy, posz, 1, 0, 0);
        }
        checkHarmonic(numBots);

        //eat fields below
        for (int i = 0; i < numBots; i++) {
            int x = posx + i * 3;
            voidIfRequired(x - 1, posy - 1, posz, -1, -1, 0);
        }
        checkHarmonic(numBots);
        for (int i = 0; i < numBots; i++) {
            int x = posx + i * 3;
            voidIfRequired(x, posy - 1, posz, 0, -1, 0);
        }
        checkHarmonic(numBots);
        for (int i = 0; i < numBots; i++) {
            int x = posx + i * 3;
            voidIfRequired(x + 1, posy - 1, posz, 1, -1, 0);
        }
        checkHarmonic(numBots);
    }

    private void checkHarmonic(int numbots) {
        if (!hasEaten) return;

        if (currentMatrix.allGrounded()) {
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

    private void voidIfRequired(int x, int y, int z, int divx, int divy, int divz) {
        if (!(x >= 0 && y >= 0 && z >= 0 &&
                x < currentMatrix.getResolution() &&
                y < currentMatrix.getResolution() &&
                z < currentMatrix.getResolution())) {
            addWait();
            return;
        }
        Coordinate toEat = Coordinate.of(x, y , z);
        if (currentMatrix.get(toEat) == VoxelState.FULL) {
            currentMatrix.unfill(toEat);
            result.add(Command.void_(Difference.of(divx, divy, divz)));
            hasEaten = true;
        } else {
            addWait();
        }
    }

    private void moveDown(int numbots, int steps) {
        for (int i = 0; i < numbots; i++) {
            result.add(Command.sMove(Difference.of(0, -steps, 0)));
        }
        posy-=steps;
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
}

package org.astormofminds.icfpc2018.solver;

import org.astormofminds.icfpc2018.model.*;
import org.astormofminds.icfpc2018.solver.exceptions.SolverNotInitializedException;
import org.astormofminds.icfpc2018.solver.exceptions.WrongNumberOfBotsException;

import java.util.ArrayList;
import java.util.List;

class Reconstructor implements Solver {

    private Matrix targetMatrix = null;
    private Matrix currentMatrix = null;
    protected List<Command> result;
    private int posx = 0;
    private int posy = 0;
    private int posz = 0;
    private boolean harmonicHigh;
    private boolean switchOff;

    @Override
    public boolean initAssemble(Matrix matrix) {
        return false;
    }

    @Override
    public boolean initDeconstruct(Matrix matrix) {
        return false;
    }

    @Override
    public boolean initReconstruct(Matrix source, Matrix target) {
        currentMatrix = source;
        targetMatrix = target;
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

        Region targetRegion = targetMatrix.getBoundingBox();
        Region sourceRegion = currentMatrix.getBoundingBox();
        int xmin = Math.min(sourceRegion.getMinX(), targetRegion.getMinX());
        int ymin = Math.min(sourceRegion.getMinY(), targetRegion.getMinY());
        int zmin = Math.min(sourceRegion.getMinZ(), targetRegion.getMinZ());
        int xmax = Math.max(sourceRegion.getMaxX(), targetRegion.getMaxX());
        int ymax = Math.max(sourceRegion.getMaxY(), targetRegion.getMaxY());
        int zmax = Math.max(sourceRegion.getMaxZ(), targetRegion.getMaxZ());

        int totalBots = (xmax - xmin + 1) / 3;
        if ((xmax - xmin + 1) % 3 != 0) totalBots++;
        int sweeps = totalBots / 40;
        if (totalBots % 40 != 0) sweeps++;
        int numBots = Math.min(40, totalBots);

        //move to starting point and one further right - we cover more ground
        while (posx < xmin + 1) {
            int steps = Math.min(xmin + 1 - posx, 15);
            result.add(Command.sMove(Difference.of(steps, 0, 0)));
            posx += steps;
        }
        //move one higher up, because we build from over the voxel
        while (posy <= ymin) {
            int steps = Math.min(ymin + 1 - posy, 15);
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
        int numBotsToSpawn = numBots - 1;
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



        for (int i = 0; i < sweeps; i++) {
            correctAllLayers(ymin, zmin, ymax, zmax, numBots);
            if (i + 1 < sweeps) {
                numBots = moveBotsToNewStart(zmin, numBots);
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

        return result;
    }

    private int moveBotsToNewStart(int zmin, int numBots) {
        int newX = posx + ((numBots) * 3);
        while (newX + ((numBots - 1) * 3) > targetMatrix.getResolution() - 1) {
            fuseOneBot(numBots);
            numBots--;
        }
        while (posx < newX) {
            int distance = Math.min(2, newX - posx);
            for (int i = 0; i < numBots; i++) {
                result.add(Command.sMove(Difference.ofX(distance)));
            }
            posx += distance;
        }
        while (posz > zmin) {
            int distance = Math.min(15, posz - zmin);
            for (int i = 0; i < numBots; i++) {
                result.add(Command.sMove(Difference.ofZ(-distance)));
            }
            posz -= distance;
        }
        while (posy > 1) {
            int distance = Math.min(15, posy - 1);
            for (int i = 0; i < numBots; i++) {
                result.add(Command.sMove(Difference.ofY(-distance)));
            }
            posy -= distance;
        }
        return numBots;
    }

    private void fuseOneBot(int numBots) {
        for (int i = 1; i < numBots; i++) {
            //let the left bots wait while we move the right one
            addWait();
        }
        result.add(Command.sMove(Difference.of(-2, 0, 0)));

        for (int i = 2; i < numBots; i++) {
            //let the left bots wait while we fusion at the right
            addWait();
        }
        //combine two
        result.add(Command.fusionP(Difference.of(1, 0, 0)));
        result.add(Command.fusionS(Difference.of(-1, 0, 0)));
    }

    private void correctAllLayers(int ymin, int zmin, int ymax, int zmax, int numBots) {
        //correct all layers
        boolean moveAway = true;
        for (int y = ymin + 1; y < ymax + 2; y += 3) {
            int issuedCommands = result.size();
            if (moveAway) {
                for (int z = zmin -1; z <= zmax + 1; z++) {
                    fixAll(numBots, -1);
                    //move one away, unless it is the last run through
                    if (z < zmax + 1) {
                        moveFar(numBots);
                    }
                }
                // move one up, unless it is the last run through
                if (y < ymax - 1) {
                    moveUp(numBots, 3);
                }
            } else {
                for (int z = zmax + 1; z >= zmin - 1; z--) {
                    fixAll(numBots, 1);
                    //move one here, unless it is the last run through
                    if (z > zmin - 1) {
                        moveNear(numBots);
                    }
                }
                // move 3 up, unless it is the last run through
                if (y < ymax - 1) {
                    moveUp(numBots, 3);
                }
            }
            moveAway = !moveAway;
            //optimize moves
            if ((result.size() - issuedCommands) % numBots != 0) {
                //something was messed up, there should be an equal number of commands for each bot
                throw new WrongNumberOfBotsException();
            }
            int steps = (result.size() - issuedCommands) / numBots;
            Optimizer.optimizeBotMoves(steps, numBots, result);
            //if we can switch off high harmonic and have not yet, do it now
            if (switchOff) {
                switchOff = false;
                harmonicHigh = false;
                result.add(Command.FLIP);
                for (int i = 1; i < numBots; i++) {
                    result.add(Command.WAIT);
                }
            }
        }
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

    private void fixAll(int numBots, int zFill) {
        int extraSteps = 0;
        //flip fields below
        for (int i = 0; i < numBots; i++) {
            int x = posx + i * 3;
            flipIfRequired(x - 1, posy - 1, posz, -1, -1, 0, false);
        }
        extraSteps += checkHarmonic(numBots);
        for (int i = 0; i < numBots; i++) {
            int x = posx + i * 3;
            flipIfRequired(x, posy - 1, posz, 0, -1, 0, false);
        }
        extraSteps += checkHarmonic(numBots);
        for (int i = 0; i < numBots; i++) {
            int x = posx + i * 3;
            flipIfRequired(x + 1, posy - 1, posz, 1, -1, 0, false);
        }
        extraSteps += checkHarmonic(numBots);
        for (int i = 0; i < numBots; i++) {
            int x = posx + i * 3;
            flipIfRequired(x, posy - 1, posz - 1, 0, -1, -1, false);
        }
        extraSteps += checkHarmonic(numBots);
        for (int i = 0; i < numBots; i++) {
            int x = posx + i * 3;
            flipIfRequired(x, posy - 1, posz + 1, 0, -1, 1, false);
        }
        extraSteps += checkHarmonic(numBots);

        //flip fields same level
        for (int i = 0; i < numBots; i++) {
            int x = posx + i * 3;
            flipIfRequired(x - 1, posy, posz, -1, 0, 0, false);
        }
        extraSteps += checkHarmonic(numBots);
        for (int i = 0; i < numBots; i++) {
            int x = posx + i * 3;
            flipIfRequired(x - 1, posy, posz - 1, -1, 0, -1, false);
        }
        extraSteps += checkHarmonic(numBots);
        for (int i = 0; i < numBots; i++) {
            int x = posx + i * 3;
            flipIfRequired(x - 1, posy, posz + 1, -1, 0, 1, false);
        }
        extraSteps += checkHarmonic(numBots);
        for (int i = 0; i < numBots; i++) {
            int x = posx + i * 3;
            flipIfRequired(x, posy, posz + zFill, 0, 0, zFill, false);
        }
        extraSteps += checkHarmonic(numBots);
        for (int i = 0; i < numBots; i++) {
            int x = posx + i * 3;
            flipIfRequired(x, posy, posz - zFill, 0, 0, -zFill, true);
        }
        extraSteps += checkHarmonic(numBots);
        for (int i = 0; i < numBots; i++) {
            int x = posx + i * 3;
            flipIfRequired(x + 1, posy, posz, 1, 0, 0, false);
        }
        extraSteps += checkHarmonic(numBots);
        for (int i = 0; i < numBots; i++) {
            int x = posx + i * 3;
            flipIfRequired(x + 1, posy, posz - 1, 1, 0, -1, false);
        }
        extraSteps += checkHarmonic(numBots);
        for (int i = 0; i < numBots; i++) {
            int x = posx + i * 3;
            flipIfRequired(x + 1, posy, posz + 1, 1, 0, 1, false);
        }
        extraSteps += checkHarmonic(numBots);

        //flip fields above
        for (int i = 0; i < numBots; i++) {
            int x = posx + i * 3;
            flipIfRequired(x - 1, posy + 1, posz, -1, 1, 0, false);
        }
        extraSteps += checkHarmonic(numBots);
        for (int i = 0; i < numBots; i++) {
            int x = posx + i * 3;
            flipIfRequired(x, posy + 1, posz, 0, 1, 0, false);
        }
        extraSteps += checkHarmonic(numBots);
        for (int i = 0; i < numBots; i++) {
            int x = posx + i * 3;
            flipIfRequired(x + 1, posy + 1, posz, 1, 1, 0, false);
        }
        extraSteps += checkHarmonic(numBots);
        for (int i = 0; i < numBots; i++) {
            int x = posx + i * 3;
            flipIfRequired(x, posy + 1, posz - 1, 0, 1, -1, false);
        }
        extraSteps += checkHarmonic(numBots);
        for (int i = 0; i < numBots; i++) {
            int x = posx + i * 3;
            flipIfRequired(x, posy + 1, posz + 1, 0, 1, 1, false);
        }
        extraSteps += checkHarmonic(numBots);

        //optimize waits
        Optimizer.optimizeBotWaits(18 + extraSteps, numBots, result);
    }

    private int checkHarmonic(int numbots) {
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
                        return 1;
                    }
                }
                // nothing found to replace - we inject a waiting + flipping round before the current tick
                int index = result.size() - numbots;
                for (int i = 1; i < numbots; i++) {
                    result.add(index, Command.WAIT);
                }
                result.add(index, Command.FLIP);
                return 1;
            }
        }
        return 0;
    }

    private void flipIfRequired(int x, int y, int z, int divx, int divy, int divz, boolean forceDelete) {
        if (!(x >= 0 && y >= 0 && z >= 0 &&
                x < targetMatrix.getResolution() &&
                y < targetMatrix.getResolution() &&
                z < targetMatrix.getResolution())) {
            addWait();
            return;
        }
        Coordinate toCheck = Coordinate.of(x, y , z);
        if (currentMatrix.isFull(toCheck) && (forceDelete || !targetMatrix.isFull(toCheck))) {
            //we have to delete
            currentMatrix.unfill(toCheck);
            result.add(Command.void_(Difference.of(divx, divy, divz)));
        } else if (!currentMatrix.isFull(toCheck) && !forceDelete && targetMatrix.isFull(toCheck)) {
            //we have to create
            currentMatrix.fill(toCheck);
            result.add(Command.fill(Difference.of(divx, divy, divz)));
        } else {
            addWait();
        }
    }

    private void moveUp(int numbots, int steps) {
        for (int i = 0; i < numbots; i++) {
            result.add(Command.sMove(Difference.of(0, steps, 0)));
        }
        posy+=steps;
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

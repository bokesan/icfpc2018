package org.astormofminds.icfpc2018.solver;

import org.astormofminds.icfpc2018.model.*;
import org.astormofminds.icfpc2018.solver.exceptions.SolverNotInitializedException;
import org.astormofminds.icfpc2018.solver.exceptions.WrongNumberOfBotsException;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

class ArchSolver implements Solver {

    private Matrix currentMatrix = null;
    protected List<Command> result;
    private int posx = 0;
    private int posy = 0;
    private int posz = 0;

    @Override
    public boolean initAssemble(Matrix matrix) {
        this.currentMatrix = matrix;
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
        if (currentMatrix == null) throw new SolverNotInitializedException();
        int resolution = currentMatrix.getResolution();
        result = new ArrayList<>(2 * resolution * resolution * resolution);
        if (resolution < 3) {
            result.add(Command.HALT);
            return result;
        }

        Region box = currentMatrix.getBoundingBox();
        int xmin = box.getMinX();
        int ymin = box.getMinY();
        int zmin = box.getMinZ();
        int xmax = box.getMaxX();
        int ymax = box.getMaxY();
        int zmax = box.getMaxZ();


        //move to starting point and one further right
        while (posx < 30) {
            int steps = Math.min(30 - posx, 15);
            result.add(Command.sMove(Difference.of(steps, 0, 0)));
            posx += steps;
        }
        //move one higher up, because we build from over the voxel
        while (posy < 40) {
            int steps = Math.min(60 - posy, 15);
            result.add(Command.sMove(Difference.of(0, steps, 0)));
            posy += steps;
        }
        while (posz < 30) {
            int steps = Math.min(30 - posz, 15);
            result.add(Command.sMove(Difference.of(0, 0, steps)));
            posz += steps;
        }


        result.add(Command.FLIP);
        //spawn bots
        int numBotsToSpawn = 9;
        for (int i = 1; i <= numBotsToSpawn; i++) {
            for (int j = 1; j < i; j++) {
                //let the left bots wait while we spawn at the right
                result.add(Command.WAIT);
            }
            //spawn a new one
            result.add(Command.fission(Difference.of(1, 0, 0), 39 - i));
            //let the left ones wait while the new one moves right
            for (int j = 0; j < i; j++) {
                //let the left bots wait while we spawn at the right
                result.add(Command.WAIT);
            }
            result.add(Command.sMove(Difference.of(5, 0, 0)));
        }

        moveFar(10);
        moveFar(10);
        moveUp(10);
        wait(3);
        moveFar(4);
        wait(3);
        wait(3);
        moveUp(4);
        wait(3);
        moveFar(3);
        moveUp(4);
        moveFar(3);
        moveUp(3);
        moveFar(4);
        moveUp(3);

        for (int i = 0; i < 10; i++) {
            clouds(10, 1);
            moveUp(10);
            moveFar(10);
            wait(1); moveUp(8); wait(1);
            moveFar(10);
            wait(2); moveFar(6); wait(2);
            moveFar(10);
            moveUp(3); clouds(4, 1); moveUp(3);
            moveFar(10);
        }

        for (int i = 0; i < 5; i++) {
            moveFar(2); wait(1); moveUp(4); wait(1); moveFar(2);
            moveUp(1); clouds(2, 1); moveUp(4); clouds(2, 1); moveUp(1);
            moveUp(2); wait(1); moveUp(4); wait(1); moveUp(2);
            moveFar(2); wait(1); moveUp(4); wait(1); moveFar(2);
            moveFar(2); clouds(2, 1); moveUp(2); clouds(2, 1); moveFar(2);
            moveUp(10);
        }

        for (int i = 0; i < 5; i++) {
            moveLeft(2); moveUp(1); wait(4); moveUp(1); moveRight(2);
            moveLeft(2); moveUp(1); clouds(4, -1); moveUp(1); moveRight(2);
            moveUp(3); wait(4); moveUp(3);
            moveLeft(3); moveUp(4); moveRight(3);
            clouds(2, -1); moveUp(1); wait(4); moveUp(1); clouds(2, -1);
        }

        for (int i = 0; i < 4; i++) {
            wait(3); moveUp(4); wait(3);
            moveNear(10);
            moveLeft(3); moveUp(1); wait(2); moveUp(1); moveRight(3);
            moveUp(3); moveLeft(1); moveNear(2); moveRight(1); moveUp(3);
            moveNear(2); moveUp(2);moveNear(2); moveUp(2); moveNear(2);
        }

        for (int i = 0; i < 3; i++) {
            moveLeft(1); moveUp(1); moveLeft(2); moveNear(2); moveRight(2); moveUp(1); moveRight(1);
            moveLeft(1); moveUp(1); moveLeft(1); moveNear(1); moveUp(2); moveNear(1); moveRight(1); moveUp(1); moveRight(1);
            clouds(4, -1); moveUp(2); clouds(4, -1);
            moveNear(10);
            moveNear(10);
            moveLeft(1); moveUp(1); moveLeft(1); moveNear(1); moveUp(2); moveNear(1); moveRight(1); moveUp(1); moveRight(1);
        }


        moveNear(10);
        moveNear(10);
        moveNear(10);
        moveNear(10);
        explode(10);


        return result;
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

    private void wait(int numbots) {
        for (int i = 0; i < numbots; i++) {
            result.add(Command.WAIT);
        }
    }

    private void moveLeft(int numbots) {
        for (int i = 0; i < numbots; i++) {
            result.add(Command.LEFT);
        }
    }

    private void moveRight(int numbots) {
        for (int i = 0; i < numbots; i++) {
            result.add(Command.RIGHT);
        }
    }

    private void explode(int numbots) {
        for (int i = 0; i < numbots; i++) {
            result.add(Command.fill(Difference.ofX(-1)));
        }
        for (int i = 0; i < numbots; i++) {
            result.add(Command.fill(Difference.ofY(1)));
        }
        for (int i = 0; i < numbots; i++) {
            result.add(Command.fill(Difference.ofX(1)));
        }
        for (int i = 0; i < numbots; i++) {
            result.add(Command.fill(Difference.ofY(-1)));
        }
    }

    private void clouds(int numbots, int reverse) {
        for (int i = 0; i < numbots; i++) {
            int z = -1 * reverse;
            int x = 0;
            int y = 0;
            int rand = new Random().nextInt();
            if (rand % 3 == 0) {
                x++;
            } else if (rand % 3 == 1) {
                x--;
            } else {
                y--;
            }
            result.add(Command.fill(Difference.of(x, y, z)));
        }
    }
}

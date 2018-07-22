package org.astormofminds.icfpc2018.solver;

import org.astormofminds.icfpc2018.model.*;
import org.astormofminds.icfpc2018.solver.exceptions.SolverNotInitializedException;

import java.util.ArrayList;
import java.util.List;

class Zapper implements Solver {

    private Matrix currentMatrix = null;
    protected List<Command> result;
    private int posx = 0;
    private int posy = 0;
    private int posz = 0;

    @Override
    public boolean initAssemble(Matrix matrix) {
        return false;
    }

    @Override
    public boolean initDeconstruct(Matrix matrix) {
        currentMatrix = matrix;
        Region box = currentMatrix.getBoundingBox();
        int xmin = box.getMinX();
        int zmin = box.getMinZ();
        int xmax = box.getMaxX();
        int ymax = box.getMaxY();
        int zmax = box.getMaxZ();

        //we can only zap a region max 30*30*30
        return !(xmax - xmin > 30 || ymax > 30 || zmax - zmin > 30);
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

        //we can only zap a region max 30*30*30
        if (xmax - xmin > 30 || ymax > 30 || zmax -zmin > 30) {
            result.add(Command.HALT);
            return result;
        }

        //move to starting position
        while (posx < xmin - 1) {
            int steps = Math.min(xmin - 1 - posx, 15);
            result.add(Command.sMove(Difference.ofX(steps)));
            posx += steps;
        }
        while (posz < zmin - 1) {
            int steps = Math.min(zmin - 1 - posz, 15);
            result.add(Command.sMove(Difference.ofZ(steps)));
            posz += steps;
        }


        //spawn first bot
        result.add(Command.fission(Difference.ofX(1), 20));
        //move first bot into position
        int x1 = xmin;
        while (x1 < xmax + 1) {
            result.add(Command.WAIT);
            int steps = Math.min(15, xmax + 1 - x1);
            result.add(Command.sMove(Difference.ofX(steps)));
            x1 += steps;
        }
        //spawn two new bots on the ground
        result.add(Command.fission(Difference.ofZ(1), 10));
        result.add(Command.fission(Difference.ofZ(1), 10));
        //move new
        int z1 = zmin;
        while (z1 < zmax - 1) {
            result.add(Command.WAIT);
            result.add(Command.WAIT);
            int steps = Math.min(15, zmax - 1 - z1);
            result.add(Command.sMove(Difference.ofZ(steps)));
            result.add(Command.sMove(Difference.ofZ(steps)));
            z1 += steps;
        }
        //spawn four new bots above
        result.add(Command.fission(Difference.ofY(1), 5));
        result.add(Command.fission(Difference.ofY(1), 5));
        result.add(Command.fission(Difference.ofY(1), 5));
        result.add(Command.fission(Difference.ofY(1), 5));
        //move bots 5 - 8
        int y1 = 1;
        while (y1 < ymax) {
            int steps = Math.min(15, ymax - y1);
            result.add(Command.WAIT);
            result.add(Command.WAIT);
            result.add(Command.WAIT);
            result.add(Command.sMove(Difference.ofY(steps)));
            result.add(Command.sMove(Difference.ofY(steps)));
            result.add(Command.WAIT);
            result.add(Command.sMove(Difference.ofY(steps)));
            result.add(Command.sMove(Difference.ofY(steps)));
            y1 += steps;
        }

        // ZAP the structure
        int xdif = xmax - xmin;
        int zdif = zmax - zmin;
        result.add(Command.gVoid(Difference.of(1, 0, 1), Difference.of(xdif, ymax, zdif)));         //1
        result.add(Command.gVoid(Difference.of(-1, 0, 1), Difference.of(-xdif, ymax, zdif)));       //2
        result.add(Command.gVoid(Difference.of(-1, 0, 1), Difference.of(-xdif, ymax, -zdif)));     //4
        result.add(Command.gVoid(Difference.of(-1, 0, 1), Difference.of(-xdif, -ymax, -zdif)));    //8
        result.add(Command.gVoid(Difference.of(-1, 0, 1), Difference.of(-xdif, -ymax, zdif)));      //6
        result.add(Command.gVoid(Difference.of(1, 0, 1), Difference.of(xdif, ymax, -zdif)));       //3
        result.add(Command.gVoid(Difference.of(1, 0, 1), Difference.of(xdif, -ymax, -zdif)));      //7
        result.add(Command.gVoid(Difference.of(1, 0, 1), Difference.of(xdif, -ymax, zdif)));        //5

        //merge bots again
        while (y1 > 1) {
            int steps = Math.min(15, y1 - 1);
            result.add(Command.WAIT);
            result.add(Command.WAIT);
            result.add(Command.WAIT);
            result.add(Command.sMove(Difference.ofY(-steps)));
            result.add(Command.sMove(Difference.ofY(-steps)));
            result.add(Command.WAIT);
            result.add(Command.sMove(Difference.ofY(-steps)));
            result.add(Command.sMove(Difference.ofY(-steps)));
            y1 -= steps;
        }
        result.add(Command.fusionP(Difference.ofY(1)));
        result.add(Command.fusionP(Difference.ofY(1)));
        result.add(Command.fusionP(Difference.ofY(1)));
        result.add(Command.fusionS(Difference.ofY(-1)));
        result.add(Command.fusionS(Difference.ofY(-1)));
        result.add(Command.fusionP(Difference.ofY(1)));
        result.add(Command.fusionS(Difference.ofY(-1)));
        result.add(Command.fusionS(Difference.ofY(-1)));
        while (z1 > zmin) {
            result.add(Command.WAIT);
            result.add(Command.WAIT);
            int steps = Math.min(15, z1 - zmin);
            result.add(Command.sMove(Difference.ofZ(-steps)));
            result.add(Command.sMove(Difference.ofZ(-steps)));
            z1 -= steps;
        }
        result.add(Command.fusionP(Difference.ofZ(1)));
        result.add(Command.fusionP(Difference.ofZ(1)));
        result.add(Command.fusionS(Difference.ofZ(-1)));
        result.add(Command.fusionS(Difference.ofZ(-1)));
        while (x1 > xmin) {
            result.add(Command.WAIT);
            int steps = Math.min(15, x1 - xmin);
            result.add(Command.sMove(Difference.ofX(-steps)));
            x1 -= steps;
        }
        result.add(Command.fusionP(Difference.ofX(1)));
        result.add(Command.fusionS(Difference.ofX(-1)));

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
}

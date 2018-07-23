package org.astormofminds.icfpc2018.solver;

import org.astormofminds.icfpc2018.model.Command;
import org.astormofminds.icfpc2018.model.Difference;
import org.astormofminds.icfpc2018.model.Matrix;
import org.astormofminds.icfpc2018.model.Region;
import org.astormofminds.icfpc2018.solver.exceptions.SolverNotInitializedException;

import java.util.ArrayList;
import java.util.List;

class ZZapper implements Solver {

    private Matrix currentMatrix = null;
    protected List<Command> result;
    private int posx = 0;
    private int posy = 0;
    private int posz = 0;
    private int poshighx = 0;
    private int poshighy = 0;
    private int poshighz = 0;

    @Override
    public boolean initAssemble(Matrix matrix) {
        return false;
    }

    @Override
    public boolean initDeconstruct(Matrix matrix) {
        currentMatrix = matrix;
        Region box = currentMatrix.getBoundingBox();
        int zmin = box.getMinZ();
        int zmax = box.getMaxZ();

        return !(zmax - zmin > 30);
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

        if (zmax - zmin > 30) {
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
        int ytarget = Math.max(0, ymax - 30);
        while (posy < ytarget) {
            int steps = Math.min(ytarget - posy, 15);
            result.add(Command.sMove(Difference.ofY(steps)));
            posy += steps;
        }
        poshighx = posx + 1;
        poshighy = posy + 1;
        poshighz = posz + 1;


        //spawn first bot
        result.add(Command.fission(Difference.ofZ(1), 20));
        //move first bot into position
        while (poshighz < zmax + 1) {
            result.add(Command.WAIT);
            int steps = Math.min(15, zmax + 1 - poshighz);
            result.add(Command.sMove(Difference.ofZ(steps)));
            poshighz += steps;
        }
        //spawn two new bots on the ground
        result.add(Command.fission(Difference.ofX(1), 10));
        result.add(Command.fission(Difference.ofX(1), 10));
        //move new
        int xtarget = Math.min(xmax - 1, xmin + 30);
        while (poshighx < xtarget) {
            result.add(Command.WAIT);
            result.add(Command.WAIT);
            int steps = Math.min(15, xtarget - poshighx);
            result.add(Command.sMove(Difference.ofX(steps)));
            result.add(Command.sMove(Difference.ofX(steps)));
            poshighx += steps;
        }
        //spawn four new bots above
        result.add(Command.fission(Difference.ofY(1), 5));
        result.add(Command.fission(Difference.ofY(1), 5));
        result.add(Command.fission(Difference.ofY(1), 5));
        result.add(Command.fission(Difference.ofY(1), 5));
        //move bots 5 - 8
        while (poshighy < ymax) {
            int steps = Math.min(15, ymax - poshighy);
            result.add(Command.WAIT);
            result.add(Command.WAIT);
            result.add(Command.WAIT);
            result.add(Command.sMove(Difference.ofY(steps)));
            result.add(Command.sMove(Difference.ofY(steps)));
            result.add(Command.WAIT);
            result.add(Command.sMove(Difference.ofY(steps)));
            result.add(Command.sMove(Difference.ofY(steps)));
            poshighy += steps;
        }

        // ZAP the structure
        int xdif = Math.min(xmax - xmin, 30);
        int ydif = Math.min(ymax, 30);
        int zdif = zmax - zmin;
        zapIt(xdif, ydif, zdif);

        boolean moveRightWards = true;
        while (currentMatrix.numFilled() > 0) {
            if (moveRightWards) {
                if (posx + 30 >= xmax - 1) {
                    if (posy == 0) break;
                    movedown();
                    moveRightWards = false;
                } else {
                    moveRight(xmax);
                }
            } else {
                if (posx < xmin) {
                    if (posy == 0) break;
                    movedown();
                    moveRightWards = true;
                } else {
                    moveLeft(xmin);
                }
            }
            zapIt(xdif, ydif, zdif);
        }

        //merge bots again
        while (poshighy > 1 + posy) {
            int steps = Math.min(15, poshighy - (1 + posy));
            result.add(Command.WAIT);
            result.add(Command.WAIT);
            result.add(Command.WAIT);
            result.add(Command.sMove(Difference.ofY(-steps)));
            result.add(Command.sMove(Difference.ofY(-steps)));
            result.add(Command.WAIT);
            result.add(Command.sMove(Difference.ofY(-steps)));
            result.add(Command.sMove(Difference.ofY(-steps)));
            poshighy -= steps;
        }
        result.add(Command.fusionP(Difference.ofY(1)));
        result.add(Command.fusionP(Difference.ofY(1)));
        result.add(Command.fusionP(Difference.ofY(1)));
        result.add(Command.fusionS(Difference.ofY(-1)));
        result.add(Command.fusionS(Difference.ofY(-1)));
        result.add(Command.fusionP(Difference.ofY(1)));
        result.add(Command.fusionS(Difference.ofY(-1)));
        result.add(Command.fusionS(Difference.ofY(-1)));
        while (poshighx > posx + 1) {
            result.add(Command.WAIT);
            result.add(Command.WAIT);
            int steps = Math.min(15, poshighx - (posx + 1));
            result.add(Command.sMove(Difference.ofX(-steps)));
            result.add(Command.sMove(Difference.ofX(-steps)));
            poshighx -= steps;
        }
        result.add(Command.fusionP(Difference.ofX(1)));
        result.add(Command.fusionP(Difference.ofX(1)));
        result.add(Command.fusionS(Difference.ofX(-1)));
        result.add(Command.fusionS(Difference.ofX(-1)));
        while (poshighz > posz + 1) {
            result.add(Command.WAIT);
            int steps = Math.min(15, poshighz - (posz + 1));
            result.add(Command.sMove(Difference.ofZ(-steps)));
            poshighz -= steps;
        }
        result.add(Command.fusionP(Difference.ofZ(1)));
        result.add(Command.fusionS(Difference.ofZ(-1)));

        //return home
        while (posx > 0) {
            int steps = Math.min(15, posx);
            result.add(Command.sMove(Difference.ofX(-steps)));
            posx -= steps;
        }
        while (posy > 0) {
            int steps = Math.min(15, posy);
            result.add(Command.sMove(Difference.of(0, -steps, 0)));
            posy -= steps;
        }
        while (posz > 0) {
            int steps = Math.min(15, posz);
            result.add(Command.sMove(Difference.ofZ(-steps)));
            posz -= steps;
        }
        //end finally, stop
        result.add(Command.HALT);

        return result;
    }

    private void moveLeft(int xmin) {
        int xtarget = Math.max(xmin, posx - 30);
        while (posx > xtarget) {
            int steps = Math.min(15, posx - xtarget);
            result.add(Command.sMove(Difference.ofX(-steps)));
            result.add(Command.sMove(Difference.ofX(-steps)));
            result.add(Command.sMove(Difference.ofX(-steps)));
            result.add(Command.sMove(Difference.ofX(-steps)));
            result.add(Command.sMove(Difference.ofX(-steps)));
            result.add(Command.sMove(Difference.ofX(-steps)));
            result.add(Command.sMove(Difference.ofX(-steps)));
            result.add(Command.sMove(Difference.ofX(-steps)));
            posx -= steps;
            poshighx -= steps;
        }
    }

    private void moveRight(int xmax) {
        int xtarget = Math.min(xmax - 30, posx + 30);
        while (posx < xtarget) {
            int steps = Math.min(15, xtarget - posx);
            result.add(Command.sMove(Difference.ofX(steps)));
            result.add(Command.sMove(Difference.ofX(steps)));
            result.add(Command.sMove(Difference.ofX(steps)));
            result.add(Command.sMove(Difference.ofX(steps)));
            result.add(Command.sMove(Difference.ofX(steps)));
            result.add(Command.sMove(Difference.ofX(steps)));
            result.add(Command.sMove(Difference.ofX(steps)));
            result.add(Command.sMove(Difference.ofX(steps)));
            posx += steps;
            poshighx += steps;
        }
    }

    private void movedown() {
        int ytarget = Math.max(0, posy - 30);
        while (posy > ytarget) {
            int steps = Math.min(15, posy - ytarget);
            result.add(Command.sMove(Difference.ofY(-steps)));
            result.add(Command.sMove(Difference.ofY(-steps)));
            result.add(Command.sMove(Difference.ofY(-steps)));
            result.add(Command.sMove(Difference.ofY(-steps)));
            result.add(Command.sMove(Difference.ofY(-steps)));
            result.add(Command.sMove(Difference.ofY(-steps)));
            result.add(Command.sMove(Difference.ofY(-steps)));
            result.add(Command.sMove(Difference.ofY(-steps)));
            posy -= steps;
            poshighy -= steps;
        }
    }

    private void zapIt(int xdif, int ydif, int zdif) {
        result.add(Command.gVoid(Difference.of(1, 0, 1), Difference.of(xdif, ydif, zdif)));         //1
        result.add(Command.gVoid(Difference.of(1, 0, -1), Difference.of(xdif, ydif, -zdif)));       //2
        result.add(Command.gVoid(Difference.of(1, 0, -1), Difference.of(-xdif, ydif, -zdif)));     //4
        result.add(Command.gVoid(Difference.of(1, 0, -1), Difference.of(-xdif, -ydif, -zdif)));    //8
        result.add(Command.gVoid(Difference.of(1, 0, -1), Difference.of(xdif, -ydif, -zdif)));      //6
        result.add(Command.gVoid(Difference.of(1, 0, 1), Difference.of(-xdif, ydif, zdif)));       //3
        result.add(Command.gVoid(Difference.of(1, 0, 1), Difference.of(-xdif, -ydif, zdif)));      //7
        result.add(Command.gVoid(Difference.of(1, 0, 1), Difference.of(xdif, -ydif, zdif)));        //5
    }
}

package org.astormofminds.icfpc2018.solver;

import org.astormofminds.icfpc2018.model.Command;
import org.astormofminds.icfpc2018.model.Difference;
import org.astormofminds.icfpc2018.model.Matrix;

import java.util.ArrayList;
import java.util.List;

class TwinSolver implements Solver {

    private Matrix target;

    @Override
    public boolean initAssemble(Matrix matrix) {
        this.target = matrix;
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
        List<Command> result = new ArrayList<>();
        //split the matrix in half
        int midX = target.getResolution() / 2;
        Matrix leftMatrix = target.getHalf(0, midX - 1);
        Matrix rightMatrix = target.getHalf(midX - 1, target.getResolution() -1);
        //fork the bot, move the second on to its starting point
        result.add(Command.fission(Difference.of(1, 0, 0), 10));
        //move second bot to its starting position, bot 1 waits
        int x = 1;
        while(x < midX - 1) {
            result.add(Command.WAIT);
            int distance = Math.min(15, midX - 1 - x);
            result.add(Command.sMove(Difference.of(distance, 0, 0)));
            x += distance;
        }
        //let both bots solve their half
        LHMultiFiller leftSolver = new LHMultiFiller();
        leftSolver.initAssemble(leftMatrix);
        List<Command> leftCommands = leftSolver.getCompleteTrace();
        LHMultiFiller rightSolver = new LHMultiFiller();
        rightSolver.initAssemble(rightMatrix);
        List<Command> rightCommands = rightSolver.getCompleteTrace();
        //merge commands, making sure flips get replaced by wait where necessary
        boolean leftHigh = false;
        boolean rightHigh = false;
        boolean combinedHigh = false;
        for (int i = 0; i < Math.max(leftCommands.size(), rightCommands.size()); i++) {
            Command leftC = Command.WAIT;
            Command rightC = Command.WAIT;
            if (i < leftCommands.size()) leftC = leftCommands.get(i);
            if (i < rightCommands.size()) rightC = rightCommands.get(i);
            //bots cannot halt, happens after fusion in the end
            if (leftC.equals(Command.HALT)) leftC = Command.WAIT;
            if (rightC.equals(Command.HALT)) rightC = Command.WAIT;
            if (leftC.equals(Command.WAIT) && rightC.equals(Command.WAIT)) continue;
            //check for flip status
            if (leftC.equals(Command.FLIP) || rightC.equals(Command.FLIP)) {
                //make sure one side doesnt flip to low while the other needs high
                if (leftC.equals(Command.FLIP)) leftHigh = !leftHigh;
                if (rightC.equals(Command.FLIP)) rightHigh = !rightHigh;
                boolean oldState = combinedHigh;
                //if either needs high state, we need it
                combinedHigh = leftHigh || rightHigh;
                if (oldState != combinedHigh) {
                    //we do need a single flip
                    result.add(leftC);
                    if (leftC.equals(Command.FLIP)) {
                        //right cannot flip as well
                        if (rightC.equals(Command.FLIP)) {
                            result.add(Command.WAIT);
                        } else {
                            result.add(rightC);
                        }
                    } else {
                        result.add(rightC);
                    }
                } else {
                    //we dont want a flip
                    if (leftC.equals(Command.FLIP)) {
                        if (!rightC.equals(Command.FLIP)) {
                            //only left wanted to flip, replace by wait
                            result.add(Command.WAIT);
                            result.add(rightC);
                        } //otherwise both wanted to flip, we ignore
                    } else {
                        //right has the flip, replace it by wait
                        result.add(leftC);
                        result.add(Command.WAIT);
                    }
                }
            } else {
                result.add(leftC);
                result.add(rightC);
            }
        }
        //move second bot back
        x = midX - 1;
        while(x > 1) {
            if (combinedHigh) {
                combinedHigh = false;
                result.add(Command.FLIP);
            } else {
                result.add(Command.WAIT);
            }
            int distance = Math.min(15, x - 1);
            result.add(Command.sMove(Difference.of(-distance, 0, 0)));
            x -= distance;
        }
        //fusion bots and halt
        result.add(Command.fusionP(Difference.of(1, 0, 0)));
        result.add(Command.fusionS(Difference.of(-1, 0, 0)));
        result.add(Command.HALT);
        return result;
    }
}

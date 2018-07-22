package org.astormofminds.icfpc2018.solver.old;

import org.astormofminds.icfpc2018.model.Command;
import org.astormofminds.icfpc2018.solver.Optimizer;

import java.util.List;

public class OptimizedSbSolver extends StupidBoundingSolver {

    @Override
    public List<Command> getCompleteTrace() {
        List<Command> result = super.getCompleteTrace();
        Optimizer.removeMoves(result);
        Optimizer.combineStraightMoves(result);
        Optimizer.moveFlipsInward(result);
        return result;
    }
}

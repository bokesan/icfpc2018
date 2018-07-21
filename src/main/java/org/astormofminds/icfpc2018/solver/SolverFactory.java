package org.astormofminds.icfpc2018.solver;

import org.astormofminds.icfpc2018.solver.chris.LayerSolver;

public class SolverFactory {

    public static Solver byName(String name) {
        switch (name) {
            case "stupid":  return new StupidSolver();
            case "stupidbounding":  return new StupidBoundingSolver();
            case "optimizing": return new OptimizedSbSolver();
            case "lowharm": return new LowHarmonicSolver();
            case "lhmulti": return new LHMultiFiller();
            case "moveto": return new LayerSolver();
            default: throw new SolverNotFoundException();
        }
    }
}

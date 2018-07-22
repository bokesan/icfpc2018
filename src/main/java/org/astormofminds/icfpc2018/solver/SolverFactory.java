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
            case "twin": return new TwinSolver();
            case "swarm": return new Swarm60Solver();
            case "surround": return new SurroundSwarmSolver();
            case "eatswarm": return new EatingSwarm();
            case "zapper": return new Zapper();
            default: throw new SolverNotFoundException();
        }
    }
}

package org.astormofminds.icfpc2018.solver;

public class SolverFactory {

    public static Solver byName(String name) {
        switch (name) {
            case "stupid":  return new StupidSolver();
            case "stupidbounding":  return new StupidBoundingSolver();
            case "optimizing": return new OptimizedSbSolver();
            default: throw new SolverNotFoundException();
        }


    }
}

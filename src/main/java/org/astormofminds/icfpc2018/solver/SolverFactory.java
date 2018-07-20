package org.astormofminds.icfpc2018.solver;

public class SolverFactory {

    public static Solver byName(String name) {
        switch (name) {
            case "stupid":  return new StupidSolver();
            default: throw new SolverNotFoundException();
        }


    }
}

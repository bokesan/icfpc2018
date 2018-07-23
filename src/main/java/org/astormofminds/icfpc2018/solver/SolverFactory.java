package org.astormofminds.icfpc2018.solver;

import org.astormofminds.icfpc2018.solver.exceptions.SolverNotFoundException;

public class SolverFactory {

    public static Solver byName(String name) {
        switch (name) {
            case "lhmulti": return new LHMultiFiller();
            case "twin": return new TwinSolver();
            case "swarm2": return new Swarm2();
            case "surround2": return new Surround2();
            case "eatswarm2": return new Eating2();
            case "zapper": return new Zapper();
            case "reconstructor": return new Reconstructor();
            case "multivoid": return new MultiVoid();
            default:
                String[] solvers = name.split("\\+");
                if (solvers.length == 2) {
                    return new CombinedSolver(byName(solvers[0]), byName(solvers[1]));
                }
                throw new SolverNotFoundException();
        }
    }
}

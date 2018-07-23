package org.astormofminds.icfpc2018.solver;

import org.astormofminds.icfpc2018.solver.exceptions.SolverNotFoundException;

public class SolverFactory {

    public static Solver byName(String name) {
        switch (name) {
            case "lhmulti": return new LHMultiFiller();
            case "twin": return new TwinSolver();
            case "swarm2": return new Swarm2();
            case "swarm3": return new Swarm3();
            case "surround2": return new Surround2();
            case "eatswarm2": return new Eating2();
            case "eatswarm3": return new Eating3();
            case "zapper": return new Zapper();
            case "xzapper": return new XZapper();
            case "zzapper": return new ZZapper();
            case "reconstructor": return new Reconstructor();
            case "multivoid": return new MultiVoid();
            case "stupidmultivoid": return new StupidMultiVoid();
            default:
                String[] solvers = name.split("\\+");
                if (solvers.length == 2) {
                    return new CombinedSolver(byName(solvers[0]), byName(solvers[1]));
                }
                throw new SolverNotFoundException();
        }
    }
}

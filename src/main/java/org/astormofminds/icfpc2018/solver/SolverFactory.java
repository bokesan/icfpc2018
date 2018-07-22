package org.astormofminds.icfpc2018.solver;

import org.astormofminds.icfpc2018.solver.chris.LayerSolver;
import org.astormofminds.icfpc2018.solver.exceptions.SolverNotFoundException;

public class SolverFactory {

    public static Solver byName(String name) {
        switch (name) {
            case "lhmulti": return new LHMultiFiller();
            case "moveto": return new LayerSolver();
            case "twin": return new TwinSolver();
            case "swarm": return new Swarm60Solver();
            case "swarm2": return new Swarm2();
            case "surround": return new SurroundSwarmSolver();
            case "surround2": return new Surround2();
            case "eatswarm": return new EatingSwarm();
            case "eatswarm2": return new Eating2();
            case "zapper": return new Zapper();
            default: throw new SolverNotFoundException();
        }
    }
}

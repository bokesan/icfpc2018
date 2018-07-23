package org.astormofminds.icfpc2018.solver;

import org.astormofminds.icfpc2018.model.Command;
import org.astormofminds.icfpc2018.model.Matrix;

import java.util.List;

class CombinedSolver implements Solver {

    private final Solver destructor;
    private final Solver assembler;

    public CombinedSolver(Solver destructor, Solver assembler) {
        this.destructor = destructor;
        this.assembler = assembler;
    }

    @Override
    public boolean initAssemble(Matrix matrix) {
        return false;
    }

    @Override
    public boolean initDeconstruct(Matrix matrix) {
        return false;
    }

    @Override
    public boolean initReconstruct(Matrix source, Matrix target) {
        return destructor.initDeconstruct(source) && assembler.initAssemble(target);
    }

    @Override
    public List<Command> getCompleteTrace() {
        List<Command> trace = destructor.getCompleteTrace();
        trace.remove(trace.size() - 1);
        trace.addAll(assembler.getCompleteTrace());
        return trace;
    }
}

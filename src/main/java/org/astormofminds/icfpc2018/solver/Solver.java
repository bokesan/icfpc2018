package org.astormofminds.icfpc2018.solver;

import org.astormofminds.icfpc2018.model.Command;
import org.astormofminds.icfpc2018.model.Matrix;

import java.util.List;

public interface Solver {

    boolean initAssemble(Matrix matrix);

    boolean initDeconstruct(Matrix matrix);

    boolean initReconstruct(Matrix source, Matrix target);

    List<Command> getCompleteTrace();
}

package org.astormofminds.icfpc2018.solver;

import org.astormofminds.icfpc2018.model.Command;
import org.astormofminds.icfpc2018.model.Matrix;

import java.util.List;

public interface Solver {

    boolean init(Matrix matrix);

    List<Command> getCompleteTrace();
}

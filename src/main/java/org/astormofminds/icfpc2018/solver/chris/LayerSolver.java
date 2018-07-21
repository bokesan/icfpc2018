package org.astormofminds.icfpc2018.solver.chris;

import org.astormofminds.icfpc2018.model.*;
import org.astormofminds.icfpc2018.solver.Solver;

import java.util.ArrayList;
import java.util.List;

/**
 * Solver that builds up the object layer by layer
 */
public class LayerSolver implements Solver {

    private Matrix target;
    private final List<Command> trace = new ArrayList<>();
    private HarmonicsState harmonics = HarmonicsState.LOW;
    private Matrix state;
    private Coordinate pos;

    @Override
    public boolean init(Matrix matrix) {
        this.target = matrix;
        this.trace.clear();
        this.state = new Matrix(target.getResolution());
        this.pos = Coordinate.ORIGIN;
        return true;
    }

    @Override
    public List<Command> getCompleteTrace() {
        Region box = target.getBoundingBox();
        int top = box.getC2().getY();
        int back = box.getC2().getZ();
        int right = box.getC2().getX();
        for (int y = 0; y <= top; y++) {
            for (int z = box.getC1().getZ(); z <= back; z++) {
                for (int x = box.getC1().getX(); x <= right; x++) {
                    Coordinate c = Coordinate.of(x, y, z);
                    if (target.isFull(c)) {
                        moveTo(c.above());
                        if (harmonics == HarmonicsState.LOW && !state.isGrounded(c)) {
                            emit(Command.FLIP);
                        }
                        emit(Command.fill(Difference.ofY(-1)));
                    }
                }
            }
        }
        if (harmonics == HarmonicsState.HIGH) {
            emit(Command.FLIP);
        }
        moveTo(Coordinate.ORIGIN);
        emit(Command.HALT);
        return trace;
    }

    /**
     * Add command to trace and update state.
     * @param cmd
     */
    private void emit(Command cmd) {
        trace.add(cmd);
        switch (cmd.getOp()) {
            case SMOVE:
                pos = pos.plus(cmd.getD1());
                break;
            case LMOVE:
                pos = pos.plus(cmd.getD1()).plus(cmd.getD2());
                break;
            case FILL:
                state.fill(pos.plus(cmd.getD1()));
                break;
            case HALT:
            case WAIT:
                break;
            case FLIP:
                if (harmonics == HarmonicsState.LOW) {
                    harmonics = HarmonicsState.HIGH;
                } else {
                    harmonics = HarmonicsState.LOW;
                }
                break;
            default:
                throw new AssertionError("op not implemented: " + cmd.getOp());
        }
    }

    private void moveTo(Coordinate c) {
        Difference d = Difference.between(pos, c);
        if (Math.abs(d.getDy()) > 0) {
            emit(Command.sMove(Difference.ofY(Math.min(15, Math.abs(d.getDy())) * Integer.signum(d.getDy()))));
            moveTo(c);
        }
        else if (Math.abs(d.getDx()) > 5) {
            emit(Command.sMove(Difference.ofX(Math.min(15, Math.abs(d.getDx())) * Integer.signum(d.getDx()))));
            moveTo(c);
        }
        else if (Math.abs(d.getDz()) > 5) {
            emit(Command.sMove(Difference.ofZ(Math.min(15, Math.abs(d.getDz())) * Integer.signum(d.getDz()))));
            moveTo(c);
        }
        else if (!d.isZero()) {
            if (d.getDx() == 0) {
                emit(Command.sMove(Difference.ofZ(d.getDz())));
            } else if (d.getDz() == 0) {
                emit(Command.sMove(Difference.ofX(d.getDx())));
            } else {
                emit(Command.lMove(Difference.ofX(d.getDx()), Difference.ofZ(d.getDz())));
            }
        }
    }
}

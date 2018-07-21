package org.astormofminds.icfpc2018.model;

import java.io.IOException;
import java.io.OutputStream;

public class Command {

    public static final Command HALT = new Command(Op.HALT);
    public static final Command WAIT = new Command(Op.WAIT);
    public static final Command FLIP = new Command(Op.FLIP);

    public static final Command RIGHT = Command.sMove(Difference.of(1, 0, 0));
    public static final Command LEFT = Command.sMove(Difference.of(-1, 0, 0));
    public static final Command UP = Command.sMove(Difference.of(0, 1, 0));
    public static final Command DOWN = Command.sMove(Difference.of(0, -1, 0));
    public static final Command FAR = Command.sMove(Difference.of(0, 0, 1));
    public static final Command NEAR = Command.sMove(Difference.of(0, 0, -1));

    public enum Op {
        HALT,
        WAIT,
        FLIP,
        SMOVE,
        LMOVE,
        FISSION,
        FILL,
        FUSIONP,
        FUSIONS
    }

    private final Op op;
    private final Difference d1;
    private final Difference d2;
    private final int m;

    private Command(Op op) {
        this.op = op;
        this.d1 = null;
        this.d2 = null;
        this.m = 0;
    }

    private Command(Op op, Difference d1, Difference d2, int m) {
        this.op = op;
        this.d1 = d1;
        this.d2 = d2;
        this.m = m;
    }

    public static Command sMove(Difference lld) {
        if (!lld.isLongLinear()) {
            throw new IllegalArgumentException("not an lld: " + lld);
        }
        return new Command(Op.SMOVE, lld, null, 0);
    }

    public static Command lMove(Difference sld1, Difference sld2) {
        if (!sld1.isShortLinear()) {
            throw new IllegalArgumentException("not a sld: " + sld1);
        }
        if (!sld2.isShortLinear()) {
            throw new IllegalArgumentException("not a sld: " + sld2);
        }
        return new Command(Op.LMOVE, sld1, sld2, 0);
    }

    public static Command fission(Difference nd, int m) {
        if (!nd.isNear()) {
            throw new IllegalArgumentException("not near: " + nd);
        }
        if (m < 0) {
            throw new IllegalArgumentException("m must be non-negative: " + m);
        }
        return new Command(Op.FISSION, nd, null, m);
    }

    public static Command fill(Difference nd) {
        if (!nd.isNear()) {
            throw new IllegalArgumentException("not near: " + nd);
        }
        return new Command(Op.FILL, nd, null, 0);
    }

    public static Command fusionP(Difference nd) {
        if (!nd.isNear()) {
            throw new IllegalArgumentException("not near: " + nd);
        }
        return new Command(Op.FUSIONP, nd, null, 0);
    }

    public static Command fusionS(Difference nd) {
        if (!nd.isNear()) {
            throw new IllegalArgumentException("not near: " + nd);
        }
        return new Command(Op.FUSIONS, nd, null, 0);
    }

    public Op getOp() {
        return op;
    }

    public Difference getD1() {
        return d1;
    }

    public Difference getD2() {
        return d2;
    }

    public int getM() {
        return m;
    }

    public Coordinate getFusionPosition(Coordinate c) {
        switch (op) {
            case FUSIONP:
            case FUSIONS:
                return c.plus(d1);
            default:
                return null;
        }
    }

    public String toString() {
        switch (op) {
            case HALT: return "Halt";
            case WAIT: return "Wait";
            case FLIP: return "Flip";
            case SMOVE: return "SMove " + d1;
            case LMOVE: return "LMove " + d1 + " " + d2;
            case FISSION: return "Fission " + d1 + " " + m;
            case FILL: return "Fill " + d1;
            case FUSIONP: return "FusionP " + d1;
            case FUSIONS: return "FusionS " + d1;
            default:
                throw new AssertionError();
        }
    }

    public void encode(OutputStream out) throws IOException {
        switch (op) {
            case HALT: out.write(255); break;
            case WAIT: out.write(254); break;
            case FLIP: out.write(253); break;
            case SMOVE:
                out.write((d1.axis() << 4) | 0b0100);
                out.write(d1.delta() + 15);
                break;
            case LMOVE:
                out.write((d1.axis() << 4) | (d2.axis() << 6) | 0b1100);
                out.write((d1.delta() + 5) | ((d2.delta() + 5) << 4));
                break;
            case FUSIONP:
                out.write((encodeNd(d1) << 3) | 0b111);
                break;
            case FUSIONS:
                out.write((encodeNd(d1) << 3) | 0b110);
                break;
            case FISSION:
                out.write((encodeNd(d1) << 3) | 0b101);
                out.write(m);
                break;
            case FILL:
                out.write((encodeNd(d1) << 3) | 0b011);
                break;
            default:
                throw new AssertionError();
        }
    }

    private int encodeNd(Difference nd) {
        return 9 * (nd.dx + 1) + 3 * (nd.dy + 1) + (nd.dz + 1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Command command = (Command) o;

        if (m != command.m) return false;
        if (op != command.op) return false;
        if (d1 != null ? !d1.equals(command.d1) : command.d1 != null) return false;
        return d2 != null ? d2.equals(command.d2) : command.d2 == null;
    }

    @Override
    public int hashCode() {
        int result = op.hashCode();
        result = 31 * result + (d1 != null ? d1.hashCode() : 0);
        result = 31 * result + (d2 != null ? d2.hashCode() : 0);
        result = 31 * result + m;
        return result;
    }
}

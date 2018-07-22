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
        FUSIONS,
        VOID,
        GFILL,
        GVOID
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
        mustBeLongLinear(lld);
        return new Command(Op.SMOVE, lld, null, 0);
    }

    public static Command lMove(Difference sld1, Difference sld2) {
        mustBeShortLinear(sld1);
        mustBeShortLinear(sld2);
        return new Command(Op.LMOVE, sld1, sld2, 0);
    }

    public static Command fission(Difference nd, int m) {
        mustBeNear(nd);
        if (m < 0 || m > 255) {
            throw new IllegalArgumentException("m out of range: " + m);
        }
        return new Command(Op.FISSION, nd, null, m);
    }

    public static Command fill(Difference nd) {
        mustBeNear(nd);
        return new Command(Op.FILL, nd, null, 0);
    }

    public static Command fusionP(Difference nd) {
        mustBeNear(nd);
        return new Command(Op.FUSIONP, nd, null, 0);
    }

    public static Command fusionS(Difference nd) {
        mustBeNear(nd);
        return new Command(Op.FUSIONS, nd, null, 0);
    }

    public static Command void_(Difference nd) {
        mustBeNear(nd);
        return new Command(Op.VOID, nd, null, 0);
    }

    public static Command gFill(Difference nd, Difference fd) {
        mustBeNear(nd);
        mustBeFar(fd);
        return new Command(Op.GFILL, nd, fd, 0);
    }

    public static Command gVoid(Difference nd, Difference fd) {
        mustBeNear(nd);
        mustBeFar(fd);
        return new Command(Op.GVOID, nd, fd, 0);
    }

    private static void mustBeShortLinear(Difference d) {
        if (!d.isShortLinear()) {
            throw new IllegalArgumentException("short linear difference required, but got " + d);
        }
    }

    private static void mustBeLongLinear(Difference d) {
        if (!d.isLongLinear()) {
            throw new IllegalArgumentException("long linear difference required, but got " + d);
        }
    }

    private static void mustBeNear(Difference d) {
        if (!d.isNear()) {
            throw new IllegalArgumentException("near difference required, but got " + d);
        }
    }

    private static void mustBeFar(Difference d) {
        if (!d.isNear()) {
            throw new IllegalArgumentException("far difference required, but got " + d);
        }
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

    /**
     * Return the position of the fusion primary.
     */
    public Coordinate getFusionPosition(Coordinate c) {
        switch (op) {
            case FUSIONP:
                return c;
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
            case VOID: return "Void " + d1;
            case GFILL: return "GFill " + d1 + " " + d2;
            case GVOID: return "GVoid " + d1 + " " + d2;
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
            case VOID:
                out.write((encodeNd(d1) << 3) | 0b010);
                break;
            case GFILL:
                out.write((encodeNd(d1) << 3) | 0b001);
                writeFarDistance(out, d2);
                break;
            case GVOID:
                out.write(encodeNd(d1) << 3);
                writeFarDistance(out, d2);
                break;
            default:
                throw new AssertionError();
        }
    }

    private static int encodeNd(Difference nd) {
        return 9 * (nd.getDx() + 1) + 3 * (nd.getDy() + 1) + (nd.getDz() + 1);
    }

    private static void writeFarDistance(OutputStream out, Difference d) throws IOException {
        out.write(d.getDx() + 30);
        out.write(d.getDy() + 30);
        out.write(d.getDz() + 30);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Command other = (Command) o;
        if (op != other.op) return false;
        switch (op) {
            case HALT:
            case WAIT:
            case FLIP:
                return true;
            case FISSION:
                return m == other.m && d1.equals(other.d1);
            case FILL:
            case VOID:
            case SMOVE:
            case FUSIONP:
            case FUSIONS:
                return d1.equals(other.d1);
            case LMOVE:
            case GFILL:
            case GVOID:
                return d1.equals(other.d1) && d2.equals(other.d2);
            default:
                throw new AssertionError();
        }
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

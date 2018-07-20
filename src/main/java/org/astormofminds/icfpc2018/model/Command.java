package org.astormofminds.icfpc2018.model;

public class Command {

    public static final Command HALT = new Command(Op.HALT);
    public static final Command WAIT = new Command(Op.WAIT);
    public static final Command FLIP = new Command(Op.FLIP);

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

}

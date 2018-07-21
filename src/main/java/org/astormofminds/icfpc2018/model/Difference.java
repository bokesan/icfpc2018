package org.astormofminds.icfpc2018.model;

/**
 * Coordinate difference.
 */
public class Difference {

    private final int dx;
    private final int dy;
    private final int dz;

    private Difference(int dx, int dy, int dz) {
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
    }

    public static Difference of(int dx, int dy, int dz) {
        return new Difference(dx, dy, dz);
    }

    public static Difference ofX(int dx) {
        return of(dx, 0, 0);
    }

    public static Difference ofY(int dy) {
        return of(0, dy, 0);
    }

    public static Difference ofZ(int dz) {
        return of(0, 0, dz);
    }

    public static Difference between(Coordinate c1, Coordinate c) {
        return of(c.getX() - c1.getX(), c.getY() - c1.getY(), c.getZ() - c1.getZ());
    }

    public boolean isZero() {
        return dx == 0 && dy == 0 && dz == 0;
    }

    /**
     * Manhattan length.
     */
    public int mlen() {
        return Math.abs(dx) + Math.abs(dy) + Math.abs(dz);
    }

    /**
     * Chessboard length.
     */
    public int clen() {
        return Math.max(Math.abs(dx), Math.max(Math.abs(dy), Math.abs(dz)));
    }

    /**
     * Get Axis of linear difference. 1 = x, 2 = y, 3 = z.
     */
    public int axis() {
        if (dx != 0) return 1;
        if (dy != 0) return 2;
        return 3;
    }

    /**
     * Get delta of linear difference.
     */
    public int delta() {
        if (dx != 0) return dx;
        if (dy != 0) return dy;
        return dz;
    }

    public boolean isLinear() {
        return (dx != 0 && dy == 0 && dz == 0) ||
               (dx == 0 && dy != 0 && dz == 0) ||
               (dx == 0 && dy == 0 && dz != 0);
    }

    public boolean isShortLinear() {
        return isLinear() && mlen() <= 5;
    }

    public boolean isLongLinear() {
        return isLinear() && mlen() <= 15;
    }

    public boolean isNear() {
        // TODO: optimize
        int m = mlen();
        return 0 < m && m <= 2 && clen() == 1;
    }

    @Override
    public String toString() {
        return "<" + dx + ", " + dy + ", " + dz + ">";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Difference that = (Difference) o;

        if (dx != that.dx) return false;
        if (dy != that.dy) return false;
        return dz == that.dz;
    }

    @Override
    public int hashCode() {
        int result = dx;
        result = 31 * result + dy;
        result = 31 * result + dz;
        return result;
    }

    public int getDx() {
        return dx;
    }

    public int getDy() {
        return dy;
    }

    public int getDz() {
        return dz;
    }
}

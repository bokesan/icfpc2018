package org.astormofminds.icfpc2018.model;

/**
 * Coordinate difference.
 */
public class Difference {

    public final int dx;
    public final int dy;
    public final int dz;

    private Difference(int dx, int dy, int dz) {
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
    }

    public static Difference of(int dx, int dy, int dz) {
        return new Difference(dx, dy, dz);
    }

    public static Difference between(Coordinate c, Coordinate c1) {
        return of(c.x - c1.x, c.y - c1.y, c.z - c1.z);
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
}

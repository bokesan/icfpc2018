package org.astormofminds.icfpc2018.model;

public class Coordinate {

    public static final Coordinate ORIGIN = new Coordinate(0, 0, 0);

    public final int x;
    public final int y;
    public final int z;

    private Coordinate(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static Coordinate of(int x, int y, int z) {
        return new Coordinate(x, y, z);
    }

    public Coordinate plus(Difference d) {
        return of(x + d.dx, y + d.dy, z + d.dz);
    }

    public Coordinate minus(Difference d) {
        return of(x - d.dx, y - d.dy, z - d.dz);
    }

    public boolean isAdjacentTo(Coordinate c) {
        return (x == c.x && y == c.y && Math.abs(z - c.z) == 1) ||
               (x == c.x && Math.abs(y - c.y) == 1 && z == c.z) ||
               (Math.abs(x - c.x) == 1 && z == c.z && y == c.y);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ", " + z + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Coordinate that = (Coordinate) o;

        if (x != that.x) return false;
        if (y != that.y) return false;
        return z == that.z;
    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        result = 31 * result + z;
        return result;
    }
}

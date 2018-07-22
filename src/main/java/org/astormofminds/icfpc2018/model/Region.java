package org.astormofminds.icfpc2018.model;

public class Region {

    private final Coordinate c1;
    private final Coordinate c2;

    private Region(Coordinate c1, Coordinate c2) {
        this.c1 = c1;
        this.c2 = c2;
    }

    public static Region of(Coordinate c1, Coordinate c2) {
        return new Region(c1, c2);
    }

    public int dim() {
        int n = 0;
        if (c1.getX() != c2.getX()) n++;
        if (c1.getY() != c2.getY()) n++;
        if (c1.getZ() != c2.getZ()) n++;
        return n;
    }

    public String toString() {
        return "[" + c1 + ", " + c2 + "]";
    }

    public Coordinate getC1() {
        return c1;
    }

    public Coordinate getC2() {
        return c2;
    }

    public boolean contains(Coordinate c) {
        return Math.min(c1.getX(), c2.getX()) <= c.getX()
                && c.getX() <= Math.max(c1.getX(), c2.getX())
                && Math.min(c1.getY(), c2.getY()) <= c.getY()
                && c.getY() <= Math.max(c1.getY(), c2.getY())
                && Math.min(c1.getZ(), c2.getZ()) <= c.getZ()
                && c.getZ() <= Math.max(c1.getZ(), c2.getZ());
    }

    // TODO: equals and hashCode
}

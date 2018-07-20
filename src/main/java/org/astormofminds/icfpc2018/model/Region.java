package org.astormofminds.icfpc2018.model;

public class Region {

    public final Coordinate c1;
    public final Coordinate c2;

    private Region(Coordinate c1, Coordinate c2) {
        this.c1 = c1;
        this.c2 = c2;
    }

    public static Region of(Coordinate c1, Coordinate c2) {
        return new Region(c1, c2);
    }

    public int dim() {
        int n = 0;
        if (c1.x != c2.x) n++;
        if (c1.y != c2.y) n++;
        if (c1.z != c2.z) n++;
        return n;
    }

    public String toString() {
        return "[" + c1 + ", " + c2 + "]";
    }

    // TODO: equals and hashCode
}

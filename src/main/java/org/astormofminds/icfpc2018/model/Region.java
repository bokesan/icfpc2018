package org.astormofminds.icfpc2018.model;

public class Region {

    private final Coordinate lbn;
    private final Coordinate rtf;

    private Region(Coordinate c1, Coordinate c2) {
        if (c1.getX() > c2.getX() || c1.getY() > c2.getY() || c1.getZ() > c2.getZ()) {
            throw new IllegalArgumentException("region invariant violated: " + c1 + ", " + c2);
        }
        this.lbn = c1;
        this.rtf = c2;
    }

    public static Region of(Coordinate c1, Coordinate c2) {
        int minX = Math.min(c1.getX(), c2.getX());
        int minY = Math.min(c1.getY(), c2.getY());
        int minZ = Math.min(c1.getZ(), c2.getZ());
        Coordinate lbn = Coordinate.of(minX, minY, minZ);
        int maxX = Math.max(c1.getX(), c2.getX());
        int maxY = Math.max(c1.getY(), c2.getY());
        int maxZ = Math.max(c1.getZ(), c2.getZ());
        Coordinate rtf = Coordinate.of(maxX, maxY, maxZ);
        return new Region(lbn, rtf);
    }

    public int dim() {
        int n = 0;
        if (lbn.getX() != rtf.getX()) n++;
        if (lbn.getY() != rtf.getY()) n++;
        if (lbn.getZ() != rtf.getZ()) n++;
        return n;
    }

    @Override
    public String toString() {
        return "[" + lbn + ", " + rtf + "]";
    }

    /**
     * Get the left-bottom-near-most coordinate.
     */
    public Coordinate getLbn() {
        return lbn;
    }

    /**
     * Get the right-top-far-most coordinate.
     */
    public Coordinate getRtf() {
        return rtf;
    }

    /**
     * @deprecated replaced by {@link #getLbn()}
     */
    @Deprecated
    public Coordinate getC1() {
        return lbn;
    }

    /**
     * @deprecated replaced by {@link #getRtf()}
     */
    @Deprecated
    public Coordinate getC2() {
        return rtf;
    }

    public int getMinX() {
        return lbn.getX();
    }

    public int getMinY() {
        return lbn.getY();
    }

    public int getMinZ() {
        return lbn.getZ();
    }

    public int getMaxX() {
        return rtf.getX();
    }

    public int getMaxY() {
        return rtf.getY();
    }

    public int getMaxZ() {
        return rtf.getZ();
    }

    public boolean contains(Coordinate c) {
        return getMinX() <= c.getX() && c.getX() <= getMaxX()
            && getMinY() <= c.getY() && c.getY() <= getMaxY()
            && getMinZ() <= c.getZ() && c.getZ() <= getMaxZ();
    }

    /**
     * Is this coordinate valid in a given resolution?
     */
    public boolean isValid(int resolution) {
        return getLbn().isValid(resolution) && getRtf().isValid(resolution);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Region other = (Region) o;
        return lbn.equals(other.lbn) && rtf.equals(other.rtf);
    }

    @Override
    public int hashCode() {
        return 31 * lbn.hashCode() + rtf.hashCode();
    }
}

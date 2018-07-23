package org.astormofminds.icfpc2018.model;

import java.lang.ref.Reference;
import java.sql.Ref;
import java.util.*;
import java.util.stream.Stream;

public class Matrix {

    private final int resolution;
    private final BitSet voxels;
    private final BitSet grounded;
    private boolean trackGrounded = true;

    public Matrix(int resolution) {
        this.resolution = resolution;
        this.voxels = new BitSet(resolution * resolution * resolution);
        this.grounded = new BitSet();
    }

    public Matrix(Matrix source) {
        this.resolution = source.getResolution();
        this.voxels = (BitSet) source.voxels.clone();
        this.grounded = (BitSet) source.grounded.clone();
    }

    public void setTrackGrounded(boolean track) {
        if (track && !trackGrounded) {
            recomputeGrounded();
        }
        trackGrounded = track;
    }

    public int getResolution() {
        return resolution;
    }

    private void check(Coordinate c) {
        if (!c.isValid(resolution))
            throw new IllegalArgumentException("coordinate out of bounds (r=" + resolution + "): " + c);
    }

    private int index(int x, int y, int z) {
        return resolution * resolution * x + resolution * y + z;
    }

    private int indexBelow(int i) {
        return i - resolution;
    }

    private int indexAbove(int i) {
        return i + resolution;
    }

    private int indexBehind(int i) {
        return i + 1;
    }

    private int indexBefore(int i) {
        return i - 1;
    }

    private int indexLeft(int i) {
        return i - resolution * resolution;
    }

    private int indexRight(int i) {
        return i + resolution * resolution;
    }

    private Coordinate toCoordinate(int index) {
        int z = index % resolution;
        int i = index / resolution;
        int y = i % resolution;
        int x = i / resolution;
        return Coordinate.of(x, y, z);
    }

    private int index(Coordinate c) {
        return index(c.getX(), c.getY(), c.getZ());
    }

    private boolean isFull(int x, int y, int z) {
        return voxels.get(index(x, y, z));
    }

    public boolean isFull(Coordinate c) {
        if (!isValid(c)) {
            throw new IllegalArgumentException("invalid coordinate: " + c);
        }
        return voxels.get(index(c));
    }

    public VoxelState get(Coordinate c) {
        return isFull(c) ? VoxelState.FULL : VoxelState.VOID;
    }

    /**
     * Set the voxel at c to FULL.
     * @return {@code true} is the old state of the voxel was VOID,
     * {@code false} if it was already FULL.
     */
    public boolean fill(Coordinate c) {
        // FIXME: should be isValidForFill, but "twin" solver needs this
        if (!isValid(c)) {
            throw new ExecutionException("coordinate not valid for fill: " + c);
        }
        return fillUnsafe(c);
    }

    private boolean fillUnsafe(Coordinate c) {
        int i = index(c);
        if (voxels.get(i)) {
            return false;
        } else {
            voxels.set(i);
            if (trackGrounded)
                postFill(c);
            return true;
        }
    }

    /**
     * Set the voxel at c to VOID.
     * @return {@code true} if the old state of the voxel was FULL,
     * {@code false} if it was already VOID.
     */
    public boolean unfill(Coordinate c) {
        if (!isValid(c)) {
            throw new ExecutionException("coordinate not valid for fill: " + c);
        }
        int i = index(c);
        if (!voxels.get(i)) {
            return false;
        } else {
            voxels.clear(i);
            if (trackGrounded) {
                // postClear(c);
                recomputeGrounded();
            }
            return true;
        }
    }

    /**
     * After filling a voxel, we have to check if it immediately becomes grounded.
     * That is the case if and only if y == or or a neigboring voxel is grounded.
     *
     * If it does become grounded, we then have to recursively ground any neighboring
     * ungrounded voxels.
     */
    private void postFill(Coordinate c) {
        boolean g = (c.getY() == 0
                     || isGrounded(c.below())
                     || isGrounded(c.above())
                     || isGrounded(c.left())
                     || isGrounded(c.right())
                     || isGrounded(c.before())
                     || isGrounded(c.behind()));
        if (g) {
            grounded.set(index(c));
            ground(c.below());
            ground(c.above());
            ground(c.before());
            ground(c.behind());
            ground(c.left());
            ground(c.right());
        }
    }

    private void ground(Coordinate c) {
        while (isValidForFill(c)) {
            int index = index(c);
            if (!voxels.get(index))
                break;
            if (grounded.get(index))
                break;
            grounded.set(index);
            if (c.getY() > 0) {
                ground(c.below());
                ground(c.before());
                ground(c.behind());
                ground(c.left());
                ground(c.right());
            }
            c = c.above();
        }
    }

    /**
     * After clearing a grounded voxel, we must check if neighboring voxel become ungrounded, too.
     * I'm too tired to do this efficiently.
     */
    private void postClear(Coordinate c) {
        updateGrounded(c.above());
        updateGrounded(c.behind());
        updateGrounded(c.before());
        updateGrounded(c.left());
        updateGrounded(c.right());
        updateGrounded(c.below());
    }

    /**
     * Recompute grounded info for all voxels.
     */
    private void recomputeGrounded() {
        grounded.clear();

        // set bottom
        for (int x = 1; x < resolution - 1; x++) {
            for (int z = 1; z < resolution - 1; z++) {
                int i = index(x, 0, z);
                if (voxels.get(i))
                    grounded.set(i);
            }
        }

        // now ground upwards in a linear way
        for (int y = 1; y < resolution - 1; y++) {
            for (int x = 1; x < resolution - 1; x++) {
                for (int z = 1; z < resolution - 1; z++) {
                    int i = index(x, y, z);
                    if (voxels.get(i)
                        && (grounded.get(indexBelow(i))
                            || grounded.get(indexLeft(i))
                            || grounded.get(indexBefore(i))))
                    {
                        grounded.set(i);
                    }
                }
            }
        }

        // and finally check still ungrounded ones for groundedness
        BitSet clone = (BitSet) voxels.clone();
        clone.andNot(grounded);
        clone.stream().forEach(i -> {
            if (grounded.get(indexAbove(i))
                || grounded.get(indexBelow(i))
                || grounded.get(indexLeft(i))
                || grounded.get(indexRight(i))
                || grounded.get(indexBefore(i))
                || grounded.get(indexBehind(i)))
            {
                ground(toCoordinate(i));
            }
        });
    }

    public boolean isGrounded(Coordinate c) {
        return isValidForFill(c) && grounded.get(index(c));
    }

    public int numFilled() {
        return voxels.cardinality();
    }

    public boolean allGrounded() {
        return grounded.cardinality() == voxels.cardinality();
    }

    /**
     * Is this coordinate valid wrt resolution?
     */
    public boolean isValid(Coordinate c) {
        return c.isValid(resolution);
    }

    /**
     * Is this coordinate valid for fill wrt resolution?
     */
    public boolean isValidForFill(Coordinate c) {
        return c.isValidForFill(resolution);
    }

    /**
     * Get all full voxel coordinates.
     */
    public Stream<Coordinate> filled() {
        return voxels.stream().mapToObj(this::toCoordinate);
    }

    /**
     * Get the minimal region covering all filled voxels
     */
    public Region getBoundingBox() {
        int xmin = resolution, ymin = resolution, zmin = resolution;
        int xmax = 0, ymax = 0, zmax = 0;
        for (int x = 1; x < resolution - 1; x++) {
            for (int y = 0; y < resolution - 1; y++) {
                for (int z = 1; z < resolution - 1; z++) {
                    if (isFull(x, y, z)) {
                        xmin = Math.min(xmin, x);
                        ymin = Math.min(ymin, y);
                        zmin = Math.min(zmin, z);
                        xmax = Math.max(xmax, x);
                        ymax = Math.max(ymax, y);
                        zmax = Math.max(zmax, z);
                    }
                }
            }
        }
        if (xmax == 0) {
            return null;
        }
        return Region.of(Coordinate.of(xmin, ymin, zmin), Coordinate.of(xmax, ymax, zmax));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Matrix matrix = (Matrix) o;

        if (resolution != matrix.resolution) return false;
        return voxels.equals(matrix.voxels);
    }

    @Override
    public int hashCode() {
        int result = resolution;
        result = 31 * result + voxels.hashCode();
        return result;
    }

    public String toString() {
        return "Matrix{r=" + getResolution() + ", filled=" + numFilled() + "}";
    }

    public Matrix getHalf(int minX, int maxX) {
        Matrix matrix = new Matrix(resolution);
        for (int x = minX + 1; x <= maxX; x++) {
            for (int y = 0; y < resolution; y++) {
                for (int z = 0; z < resolution; z++) {
                    if (isFull(x, y, z)) matrix.fillUnsafe(Coordinate.of(x - minX, y, z));
                }
            }
        }
        return matrix;
    }

    public boolean contains(Coordinate c) {
        return !(c.getX() < 0 || c.getX() >= resolution
                || c.getY() < 0 || c.getY() >= resolution
                || c.getZ() < 0 || c.getZ() >= resolution);
    }


    private static class BooleanRef {
        boolean value = false;
    }

    private void updateGrounded(Coordinate c) {
        if (isValid(c) && isFull(c)) {
        /*
        1) Initialize all vertices as not visited.
        2) Do following for every vertex 'v'.
          (a) If 'v' is not visited before, call DFSUtil(v)
          (b) Print new line character

       DFSUtil(v)
         1) Mark 'v' as visited.
         2) Print 'v'
         3) Do following for every adjacent 'u' of 'v'.
            If 'u' is not visited, then recursively call DFSUtil(u)
         */
            BitSet visited = new BitSet(voxels.length());
            BooleanRef anyY0 = new BooleanRef();
            getComponent1(visited, anyY0, c);
            if (anyY0.value) {
                // at least one on base, so all are grounded
                grounded.or(visited);
            } else {
                // not grounded
                grounded.and(visited);
            }
        }
    }

    private void getComponent1(BitSet visited, BooleanRef anyY0, Coordinate c) {
        if (isValid(c) && isFull(c) && !visited.get(index(c))) {
            visited.set(index(c));
            if (c.getY() == 0) {
                anyY0.value = true;
            }
            getComponent1(visited, anyY0, c.above());
            getComponent1(visited, anyY0, c.below());
            getComponent1(visited, anyY0, c.left());
            getComponent1(visited, anyY0, c.right());
            getComponent1(visited, anyY0, c.before());
            getComponent1(visited, anyY0, c.behind());
        }
    }

}

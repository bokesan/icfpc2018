package org.astormofminds.icfpc2018.model;

import java.util.*;
import java.util.stream.Stream;

public class Matrix {

    private final int resolution;
    private final BitSet voxels;

    public Matrix(int resolution) {
        this.resolution = resolution;
        this.voxels = new BitSet(resolution * resolution * resolution);
    }

    public int getResolution() {
        return resolution;
    }

    private void check(Coordinate c) {
        if (c.getX() < 0 || c.getX() >= resolution
            || c.getY() < 0 || c.getY() >= resolution
            || c.getZ() < 0 || c.getZ() >= resolution)
            throw new IllegalArgumentException("coordinate out of bounds (r=" + resolution + "): " + c);
    }

    private int index(int x, int y, int z) {
        return resolution * resolution * x + resolution * y + z;
    }

    private Coordinate toCoordinate(int index) {
        int z = index % resolution;
        int i = index / resolution;
        int y = i % resolution;
        int x = i / resolution;
        return Coordinate.of(x, y, z);
    }

    private int index(Coordinate c) {
        check(c);
        return index(c.getX(), c.getY(), c.getZ());
    }

    private boolean isFull(int x, int y, int z) {
        return voxels.get(index(x, y, z));
    }

    public boolean isFull(Coordinate c) {
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
        int i = index(c);
        if (voxels.get(i)) {
            return false;
        } else {
            voxels.set(i);
            return true;
        }
    }

    /**
     * Set the voxel at c to VOID.
     * @return {@code true} if the old state of the voxel was FULL,
     * {@code false} if it was already VOID.
     */
    public boolean unfill(Coordinate c) {
        int i = index(c);
        if (!voxels.get(i)) {
            return false;
        } else {
            voxels.clear(i);
            return true;
        }
    }

    public int numFilled() {
        return voxels.cardinality();
    }

    public boolean isValid(Coordinate c) {
        return c.getX() >= 0 && c.getX() < resolution
                && c.getY() >= 0 && c.getY() < resolution
                && c.getZ() >= 0 && c.getZ() < resolution;
    }

    public boolean isGrounded(Coordinate c) {
        BitSet visited = new BitSet(resolution * resolution * (resolution-1));
        Deque<Coordinate> stack = new ArrayDeque<>(50);
        stack.push(c);
        while (!stack.isEmpty()) {
            Coordinate c1 = stack.pop();
            if (c1.getY() == 0) {
                return true;
            }
            int i = index(c1);
            if (!visited.get(i)) {
                visited.set(i);
                tryNeighbor(stack, c1.above());
                tryNeighbor(stack, c1.left());
                tryNeighbor(stack, c1.before());
                tryNeighbor(stack, c1.right());
                tryNeighbor(stack, c1.behind());
                tryNeighbor(stack, c1.below());
            }
        }
        return false;
    }

    private void tryNeighbor(Deque<Coordinate> stack, Coordinate voxel) {
        if (isValid(voxel) && isFull(voxel)) {
            stack.push(voxel);
        }
    }

    /**
     * Get all full voxel coordinates.
     */
    public Stream<Coordinate> filled() {
        return voxels.stream().mapToObj(this::toCoordinate);
    }

    /**
     * Get full voxels on given y-plane.
     */
    public Stream<Coordinate> filled(int y) {
        Stream.Builder<Coordinate> builder = Stream.builder();
        for (int x = 1; x < resolution - 1; x++) {
            for (int z = 1; z < resolution - 1; z++) {
                if (isFull(x, 0, z)) {
                    builder.accept(Coordinate.of(x, 0, z));
                }
            }
        }
        return builder.build();
    }

    /**
     * Get the minimal region covering all filled voxels
     */
    public Region getBoundingBox() {
        int xmin = resolution, ymin = resolution, zmin = resolution;
        int xmax = 0, ymax = 0, zmax = 0;
        for (int x = 0; x < resolution; x++) {
            for (int y = 0; y < resolution - 1; y++) {
                for (int z = 0; z < resolution; z++) {
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
        for (int x = minX; x <= maxX; x++) {
            for (int y = 0; y < resolution; y++) {
                for (int z = 0; z < resolution; z++) {
                    if (isFull(x, y, z)) matrix.fill(Coordinate.of(x - minX, y, z));
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
}

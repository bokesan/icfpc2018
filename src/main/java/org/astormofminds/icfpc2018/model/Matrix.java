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
        if (c.x < 0 || c.x >= resolution
            || c.y < 0 || c.y >= resolution
            || c.z < 0 || c.z >= resolution)
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
        return index(c.x, c.y, c.z);
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

    public void fill(Coordinate c) {
        voxels.set(index(c));
    }

    public int numFilled() {
        return voxels.cardinality();
    }

    public boolean isValid(Coordinate c) {
        return c.x >= 0 && c.x < resolution
                && c.y >= 0 && c.y < resolution
                && c.z >= 0 && c.z < resolution;
    }

    public boolean isGrounded(Coordinate c) {
        if (get(c) != VoxelState.FULL) {
            throw new IllegalArgumentException("VOID voxel: " + c);
        }
        Set<Coordinate> visited = new HashSet<>();
        Stack<Coordinate> stack = new Stack<>();
        stack.push(c);
        while (!stack.empty()) {
            Coordinate c1 = stack.pop();
            if (c1.y == 0) {
                return true;
            }
            if (visited.add(c1)) {
                tryNeighbor(stack, c1.below());
                tryNeighbor(stack, c1.left());
                tryNeighbor(stack, c1.before());
                tryNeighbor(stack, c1.right());
                tryNeighbor(stack, c1.behind());
                tryNeighbor(stack, c1.above());
            }
        }
        return false;
    }

    private void tryNeighbor(Stack<Coordinate> stack, Coordinate voxel) {
        if (isValid(voxel) && isFull(voxel)) {
            stack.push(voxel);
        }
    }

    /**
     * Get all full voxel coordinates.
     */
    public Stream<Coordinate> filled() {
        Stream.Builder<Coordinate> builder = Stream.builder();
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
        return Region.of(Coordinate.of(xmin, ymin, zmin), Coordinate.of(xmax, ymax, zmax));
    }
}

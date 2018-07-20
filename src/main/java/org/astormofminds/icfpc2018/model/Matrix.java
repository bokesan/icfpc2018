package org.astormofminds.icfpc2018.model;

import java.util.BitSet;

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

    private int index(Coordinate c) {
        check(c);
        return resolution * resolution * c.x
                + resolution * c.y
                + c.z;
    }

    public VoxelState get(Coordinate c) {
        return voxels.get(index(c)) ? VoxelState.FULL : VoxelState.VOID;
    }

    public void fill(Coordinate c) {
        voxels.set(index(c));
    }

    public int numFilled() {
        return voxels.cardinality();
    }

    public boolean isGrounded(Coordinate c) {
        // TODO
        throw new AssertionError("not implemented");
    }
}

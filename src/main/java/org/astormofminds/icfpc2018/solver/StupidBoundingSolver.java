package org.astormofminds.icfpc2018.solver;

import org.astormofminds.icfpc2018.model.*;

import java.util.ArrayList;
import java.util.List;

public class StupidBoundingSolver implements Solver {

    private Matrix matrix = null;

    @Override
    public boolean init(Matrix matrix) {
        this.matrix = matrix;
        return true;
    }

    @Override
    public List<Command> getCompleteTrace() {
        if (matrix == null) throw new SolverNotInitializedException();
        int resolution = matrix.getResolution();
        List<Command> result = new ArrayList<>(2 * resolution * resolution * resolution);
        if (resolution < 3) {
            result.add(Command.HALT);
            return result;
        }

        Region box = matrix.getBoundingBox();
        int xmin = Math.min(box.c1.x, box.c2.x);
        int ymin = Math.min(box.c1.y, box.c2.y);
        int zmin = Math.min(box.c1.z, box.c2.z);
        int xmax = Math.max(box.c1.x, box.c2.x);
        int ymax = Math.max(box.c1.y, box.c2.y);
        int zmax = Math.max(box.c1.z, box.c2.z);



        //move to starting point
        for (int x = 0; x < xmin; x++) {
            result.add(Command.RIGHT);
        }
        //move one higher up, because we build from over the voxel
        for (int y = 0; y < ymin + 1; y++) {
            result.add(Command.UP);
        }
        for (int z = 0; z < zmin; z++) {
            result.add(Command.FAR);
        }

        //flip to high energy
        result.add(Command.FLIP);

        //print all layers
        for (int y = ymin + 1; y < ymax + 2; y++) {

            //on each layer, move from near to far on odd layers, omitting the last row
            if ((y - ymin) % 2 == 1) {
                for (int z = zmin; z <= zmax; z++) {

                    //in each row, move right on odd rows and left in even ones
                    if ((z - zmin) % 2 == 0) {
                        //odd row - move right
                        for (int x = xmin; x <= xmax; x++) {
                            Coordinate underUs = Coordinate.of(x, y - 1, z);
                            if (matrix.get(underUs) == VoxelState.FULL) {
                                result.add(Command.fill(Difference.of(0, -1, 0)));
                            }
                            //move to right, unless it is the last run through
                            if (x < xmax) {
                                result.add(Command.RIGHT);
                            }
                        }
                    } else {
                        //even row, move left
                        for (int x = xmax; x >= xmin; x--) {
                            Coordinate underUs = Coordinate.of(x, y - 1, z);
                            if (matrix.get(underUs) == VoxelState.FULL) {
                                result.add(Command.fill(Difference.of(0, -1, 0)));
                            }
                            //move left, unless it is the last run through
                            if (x > xmin) {
                                result.add(Command.LEFT);
                            }
                        }
                    }

                    //move one away, unless it is the last run through
                    if (z < zmax) {
                        result.add(Command.FAR);
                    }
                }

                // move one up, unless it is the last run through
                if (y < resolution - 1) {
                    result.add(Command.UP);
                }
            } else {
                // move from far to near on even layers
                for (int z = zmax; z >= zmin; z--) {

                    //in each row, move right on even rows and left in odd ones
                    if ((z - zmin) % 2 == 1) {
                        //odd row - move right
                        for (int x = xmin; x <= xmax; x++) {
                            Coordinate underUs = Coordinate.of(x, y - 1, z);
                            if (matrix.get(underUs) == VoxelState.FULL) {
                                result.add(Command.fill(Difference.of(0, -1, 0)));
                            }
                            //move to right, unless it is the last run through
                            if (x < xmax) {
                                result.add(Command.RIGHT);
                            }
                        }
                    } else {
                        //even row, move left
                        for (int x = xmax; x >= xmin; x--) {
                            Coordinate underUs = Coordinate.of(x, y - 1, z);
                            if (matrix.get(underUs) == VoxelState.FULL) {
                                result.add(Command.fill(Difference.of(0, -1, 0)));
                            }
                            //move left, unless it is the last run through
                            if (x > xmin) {
                                result.add(Command.LEFT);
                            }
                        }
                    }

                    //move one here, unless it is the last run through
                    if (z > zmin) {
                        result.add(Command.NEAR);
                    }
                }

                // move one up, unless it is the last run through
                if (y < ymax + 1) {
                    result.add(Command.UP);
                }
            }

        }

        //return home - first disable high energy
        result.add(Command.FLIP);

        //move to nearest row
        if ((ymax - ymin) % 2 == 0) {
            for (int z = zmax; z > 0; z--) {
                result.add(Command.NEAR);
            }
        } else {
            for (int z = zmin; z > 0; z--) {
                result.add(Command.NEAR);
            }
        }
        //move all the way down
        for (int y = ymax + 1; y > 0; y--) {
            result.add(Command.DOWN);
        }
        //move back to left
        if ((zmax - zmin) % 2 == 0) {
            for (int x = xmax; x > 0; x--) {
                result.add(Command.LEFT);
            }
        } else {
            for (int x = xmin; x > 0; x--) {
                result.add(Command.LEFT);
            }
        }

        //end finally, stop
        result.add(Command.HALT);

        return result;
    }
}

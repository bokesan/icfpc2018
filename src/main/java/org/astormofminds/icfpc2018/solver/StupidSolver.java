package org.astormofminds.icfpc2018.solver;

import org.astormofminds.icfpc2018.model.*;

import java.util.ArrayList;
import java.util.List;

public class StupidSolver implements Solver {

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

        //flip to high energy
        result.add(Command.FLIP);
        //move +1 in all directions to get above the printable area
        result.add(Command.UP);
        result.add(Command.RIGHT);
        result.add(Command.FAR);

        //print all layers
        for (int y = 1; y < resolution; y++) {

            //on each layer, move from near to far on odd layers, omitting the last row
            if (y % 2 == 1) {
                for (int z = 1; z < resolution - 1; z++) {

                    //in each row, move right on odd rows and left in even ones
                    if (z % 2 == 1) {
                        //odd row - move right
                        for (int x = 1; x < resolution - 1; x++) {
                            Coordinate underUs = Coordinate.of(x, y - 1, z);
                            if (matrix.get(underUs) == VoxelState.FULL) {
                                result.add(Command.fill(Difference.of(0, -1, 0)));
                            }
                            //move to right, unless it is the last run through
                            if (x < resolution -2) {
                                result.add(Command.RIGHT);
                            }
                        }
                    } else {
                        //even row, move left
                        for (int x = resolution - 2; x > 0; x--) {
                            Coordinate underUs = Coordinate.of(x, y - 1, z);
                            if (matrix.get(underUs) == VoxelState.FULL) {
                                result.add(Command.fill(Difference.of(0, -1, 0)));
                            }
                            //move left, unless it is the last run through
                            if (x > 1) {
                                result.add(Command.LEFT);
                            }
                        }
                    }

                    //move one away, unless it is the last run through
                    if (z < resolution - 2) {
                        result.add(Command.FAR);
                    }
                }

                // move one up, unless it is the last run through
                if (y < resolution - 1) {
                    result.add(Command.UP);
                }
            } else {
                // move from far to near on even layers
                for (int z = resolution - 2; z > 0; z--) {

                    //in each row, move right on even rows and left in odd ones
                    if (z % 2 == 0) {
                        //odd row - move right
                        for (int x = 1; x < resolution - 1; x++) {
                            Coordinate underUs = Coordinate.of(x, y - 1, z);
                            if (matrix.get(underUs) == VoxelState.FULL) {
                                result.add(Command.fill(Difference.of(0, -1, 0)));
                            }
                            //move to right, unless it is the last run through
                            if (x < resolution -2) {
                                result.add(Command.RIGHT);
                            }
                        }
                    } else {
                        //even row, move left
                        for (int x = resolution - 2; x > 0; x--) {
                            Coordinate underUs = Coordinate.of(x, y - 1, z);
                            if (matrix.get(underUs) == VoxelState.FULL) {
                                result.add(Command.fill(Difference.of(0, -1, 0)));
                            }
                            //move left, unless it is the last run through
                            if (x > 1) {
                                result.add(Command.LEFT);
                            }
                        }
                    }

                    //move one here, unless it is the last run through
                    if (z > 1) {
                        result.add(Command.NEAR);
                    }
                }

                // move one up, unless it is the last run through
                if (y < resolution - 1) {
                    result.add(Command.UP);
                }
            }

        }

        //return home - first disable high energy
        result.add(Command.FLIP);
        //move to nearest row
        if (resolution % 2 == 1) {
            result.add(Command.NEAR);
        } else {
            for (int z = resolution -2; z > 0; z--) {
                result.add(Command.NEAR);
            }
        }
        //move all the way down
        for (int y = resolution - 1; y > 0; y--) {
            result.add(Command.DOWN);
        }
        //move back to left - either one step if the resolution is even or all the way if it is odd
        if (resolution % 2 == 0) {
            result.add(Command.LEFT);
        } else {
            for (int x = resolution -2; x > 0; x--) {
                result.add(Command.LEFT);
            }
        }

        //end finally, stop
        result.add(Command.HALT);

        return result;
    }
}

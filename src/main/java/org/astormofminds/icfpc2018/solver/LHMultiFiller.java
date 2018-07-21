package org.astormofminds.icfpc2018.solver;

import org.astormofminds.icfpc2018.model.*;

import java.util.HashSet;
import java.util.Set;

public class LHMultiFiller extends LowHarmonicSolver {

    @Override
    protected void fillIfRequired(int y, int z, int x) {
        Coordinate underUs = Coordinate.of(x, y - 1, z);
        if (targetMatrix.get(underUs) == VoxelState.FULL && currentMatrix.get(underUs) == VoxelState.VOID) {
            Set<Coordinate> toFill = new HashSet<>();
            toFill.add(underUs);
            if (x> 0 && y > 0) {
                Coordinate downLeft = Coordinate.of(x - 1, y - 1, z);
                if (targetMatrix.contains(downLeft) && targetMatrix.get(downLeft) == VoxelState.FULL && currentMatrix.get(downLeft) == VoxelState.VOID) toFill.add(downLeft);
            }
            if (y > 0) {
                Coordinate downRight = Coordinate.of(x + 1, y - 1, z);
                if (targetMatrix.contains(downRight) && targetMatrix.get(downRight) == VoxelState.FULL && currentMatrix.get(downRight) == VoxelState.VOID) toFill.add(downRight);
                Coordinate downFar = Coordinate.of(x, y - 1, z + 1);
                if (targetMatrix.contains(downFar) && targetMatrix.get(downFar) == VoxelState.FULL && currentMatrix.get(downFar) == VoxelState.VOID) toFill.add(downFar);
            }
            if (y > 0 && z > 0) {
                Coordinate downNear = Coordinate.of(x, y - 1, z - 1);
                if (targetMatrix.contains(downNear) && targetMatrix.get(downNear) == VoxelState.FULL && currentMatrix.get(downNear) == VoxelState.VOID) toFill.add(downNear);
            }

            for (Coordinate current : toFill) {
                currentMatrix.fill(current);
                boolean flipDown = false;
                boolean flipUp = false;
                if (highHarmonic) {
                    //check whether we are all grounded and can flip down after this fill
                    if (currentMatrix.isGrounded(current)) {
                        flipDown = true;
                        for (Coordinate c : floating) {
                            if (!currentMatrix.isGrounded(c)) {
                                flipDown = false;
                                break;
                            }
                        }
                    } else {
                        floating.add(current);
                    }
                } else {
                    //check whether we fill ungrounded and need to flip up before this fill
                    if (!currentMatrix.isGrounded(current)) {
                        //we only want this if it is the voxel right below
                        //we do not go into high harmonic to fill neighbours
                        if (current.equals(underUs)) {
                            flipUp = true;
                        } else {
                            currentMatrix.unfill(current);
                            continue;
                        }
                    }
                }
                if (flipUp) {
                    result.add(Command.FLIP);
                    highHarmonic = true;
                    floating.add(current);
                }
                result.add(Command.fill(Difference.of(current.getX() - x, current.getY() - y, current.getZ() - z)));
                if (flipDown) {
                    result.add(Command.FLIP);
                    highHarmonic = false;
                    floating = new HashSet<>();
                }
            }

        }
    }
}

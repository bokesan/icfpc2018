package org.astormofminds.icfpc2018.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class State {

    private long energy;
    private HarmonicsState harmonics;
    private final Matrix matrix;
    private final Set<Nanobot> bots = new TreeSet<>();
    private final List<Command> trace = new LinkedList<>();

    public State(int resolution) {
        matrix = new Matrix(resolution);
    }

    public int getResolution() {
        return matrix.getResolution();
    }

    public void fill(Coordinate c) {
        matrix.fill(c);
    }

    public boolean isWellFormed() {
        if (harmonics == HarmonicsState.LOW) {
            if (!matrix.filled().allMatch(matrix::isGrounded)) {
                return false;
            }
        }
        if (bots.stream().anyMatch(b -> matrix.isFull(b.getPos()))) {
            return false;
        }
        // TODO: more conditions
        return true;
    }


    public String toString() {
        return "State{energy=" + energy
                + ", harmonics=" + harmonics
                + ", resolution=" + getResolution()
                + ", filled=" + matrix.numFilled()
                + ", bots=" + bots.size()
                + ", trace=" + trace.size()
                + "}";
    }
}

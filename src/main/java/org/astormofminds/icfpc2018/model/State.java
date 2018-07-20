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


}

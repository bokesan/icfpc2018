package org.astormofminds.icfpc2018.model;

import java.util.HashSet;
import java.util.Set;

public class Nanobot {

    private final int bid;
    private Coordinate pos;
    private final Set<Integer> seeds;

    private Nanobot(int bid) {
        this.bid = bid;
        this.pos = Coordinate.ORIGIN;
        this.seeds = new HashSet<>();
    }

    public static Nanobot initial() {
        Nanobot bot = new Nanobot(1);
        for (int seed = 2; seed <= 20; seed++) {
            bot.seeds.add(seed);
        }
        return bot;
    }

}

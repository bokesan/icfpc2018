package org.astormofminds.icfpc2018.model;

import java.util.Collections;
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

    public int getBid() {
        return bid;
    }

    public Coordinate getPos() {
        return pos;
    }

    public Set<Integer> getSeeds() {
        return Collections.unmodifiableSet(seeds);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Nanobot nanobot = (Nanobot) o;

        return bid == nanobot.bid;
    }

    @Override
    public int hashCode() {
        return bid;
    }
}

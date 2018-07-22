package org.astormofminds.icfpc2018.model;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

public class Nanobot implements Comparable<Nanobot> {

    private final int bid;
    private Coordinate pos;
    private final TreeSet<Integer> seeds;

    private Nanobot(int bid) {
        this.bid = bid;
        this.pos = Coordinate.origin();
        this.seeds = new TreeSet<>();
    }

    /**
     * Initial bot for main division.
     */
    public static Nanobot initial() {
        Nanobot bot = new Nanobot(1);
        for (int seed = 2; seed <= 40; seed++) {
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

    public void setPos(Coordinate c) {
        pos = c;
    }

    public Set<Integer> getSeeds() {
        return Collections.unmodifiableSet(seeds);
    }

    public Nanobot fissure(Coordinate c, int m) {
        assert !seeds.isEmpty();
        Nanobot b1 = new Nanobot(seeds.pollFirst());
        b1.pos = c;
        for (int i = 0; i < m; i++) {
            b1.seeds.add(seeds.pollFirst());
        }
        return b1;
    }

    public void fuseWith(Nanobot b1) {
        seeds.add(b1.bid);
        seeds.addAll(b1.seeds);
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

    @Override
    public int compareTo(Nanobot o) {
        return Integer.compare(bid, o.bid);
    }

    @Override
    public String toString() {
        return "bot " + bid + " " + pos;
    }
}

package org.astormofminds.icfpc2018.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class State {

    private final Logger logger = LoggerFactory.getLogger(State.class);

    private long energy = 0;
    private HarmonicsState harmonics = HarmonicsState.LOW;
    private final Matrix matrix;
    private final Set<Nanobot> bots;
    private final LinkedList<Command> trace = new LinkedList<>();
    private int steps = 0;

    public State(int resolution, Collection<Command> trace) {
        matrix = new Matrix(resolution);
        this.trace.addAll(trace);
        bots = new TreeSet<>();
        Nanobot bot1 = Nanobot.initial();
        bots.add(bot1);
    }

    public int getResolution() {
        return matrix.getResolution();
    }

    public boolean isValidFinalState(Matrix target) {
        return harmonics == HarmonicsState.LOW
                && matrix.equals(target)
                && bots.isEmpty()
                && trace.isEmpty();
    }

    private int getSize() {
        int r = getResolution();
        return r * r * r;
    }

    public void fill(Coordinate c) {
        matrix.fill(c);
    }


    /**
     * Perform one time step.
     * @return {@code true} if there are active bots after the step and
     * {@code false} if the system is halted after the time step.
     */
    public boolean timeStep() {
        if ((steps & 8191) == 0) {
            logger.info("steps: " + steps);
            if (!isWellFormed()) {
                throw new ExecutionException("not well-formed");
            }
        }
        if (bots.isEmpty()) {
            throw new ExecutionException("system is halted");
        }
        Stream.Builder<BotCommand> botCommands = Stream.builder();
        for (Nanobot bot : bots) {
            BotCommand bc = new BotCommand(bot, trace.removeFirst());
            botCommands.add(bc);
        }
        Collection<List<BotCommand>> groups = botCommands.build()
                .collect(Collectors.groupingBy(GroupKey::new))
                .values();

        energy += ((harmonics == HarmonicsState.LOW) ? 3L : 30L) * getSize();
        energy += 20L * bots.size();
        for (List<BotCommand> group : groups) {
            BotCommand bc = group.get(0);
            Nanobot bot = bc.bot;
            Coordinate c = bot.getPos();
            Command cmd = bc.command;
            switch (cmd.getOp()) {
                case HALT:
                    if (!c.isOrigin()) {
                        throw new ExecutionException("halting bot not at origin: " + c);
                    }
                    if (bots.size() != 1) {
                        throw new ExecutionException("halting bot must be last bot: " + bots.size());
                    }
                    if (harmonics != HarmonicsState.LOW) {
                        throw new ExecutionException("harmonics mut be LOW at halt: " + harmonics);
                    }
                    bots.clear();
                    steps++;
                    return false;
                case WAIT:
                    break;
                case FLIP:
                    if (harmonics == HarmonicsState.LOW) {
                        harmonics = HarmonicsState.HIGH;
                    } else {
                        harmonics = HarmonicsState.LOW;
                    }
                    break;
                case SMOVE:
                    bot.setPos(c.plus(cmd.getD1()));
                    energy += 2L * cmd.getD1().mlen();
                    break;
                case LMOVE:
                    bot.setPos(c.plus(cmd.getD1()).plus(cmd.getD2()));
                    energy += 2L * (cmd.getD1().mlen() + 2 + cmd.getD2().mlen());
                    break;
                case FILL:
                    Coordinate c1 = c.plus(cmd.getD1());
                    if (matrix.fill(c1)) {
                        energy += 12;
                    } else {
                        energy += 6;
                    }
                    break;
                case FISSION:
                    Difference nd = cmd.getD1();
                    int m = cmd.getM();
                    int n = bot.getSeeds().size();
                    if (n == 0 || n <= m) {
                        throw new ExecutionException("Fission: too few seeds");
                    }
                    c1 = c.plus(nd);
                    bots.add(bot.fissure(c1, m));
                    energy += 24;
                    break;
                case FUSIONP:
                case FUSIONS:
                    if (group.size() != 2) {
                        throw new ExecutionException("invalid fusion group size: " + group.size());
                    }
                    BotCommand bcs;
                    if (cmd.getOp() == Command.Op.FUSIONP) {
                        bcs = group.get(1);
                    } else {
                        bcs = bc;
                        bc = group.get(1);
                        bot = bc.bot;
                    }
                    bots.remove(bcs.bot);
                    bot.fuseWith(bcs.bot);
                    energy -= 24;
                    break;
            }
        }
        steps++;
        return true;
    }

    private static class BotCommand {
        final Nanobot bot;
        final Command command;

        BotCommand(Nanobot bot, Command command) {
            this.bot = bot;
            this.command = command;
        }
    }

    private static class GroupKey {
        private final Nanobot single;
        private final Coordinate fusion;

        public GroupKey(BotCommand bc) {
            Command cmd = bc.command;
            fusion = cmd.getFusionPosition(bc.bot.getPos());
            if (fusion != null) {
                single = null;
            } else {
                single = bc.bot;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            GroupKey other = (GroupKey) o;

            if (single != null) {
                return single.equals(other.single);
            } else {
                return fusion.equals(other.fusion);
            }
        }

        @Override
        public int hashCode() {
            if (single != null) {
                return single.hashCode();
            } else {
                return fusion.hashCode();
            }
        }
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
        if (bots.stream().anyMatch(b -> bots.stream().anyMatch(b1 -> b.getPos().equals(b1.getPos()) && !b.equals(b1)))) {
            // bot positions not disjunct
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
                + ", steps=" + steps
                + "}";
    }
}

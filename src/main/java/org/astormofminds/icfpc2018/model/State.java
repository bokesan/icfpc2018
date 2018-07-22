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

    public State(Collection<Command> trace, Matrix matrix) {
        this.matrix = matrix;
        this.trace.addAll(trace);
        bots = new TreeSet<>();
        Nanobot bot1 = Nanobot.initial();
        bots.add(bot1);
    }

    public Matrix getMatrix() {
        return matrix;
    }

    public long getEnergy() {
        return energy;
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

    public boolean isMatrixEmpty() {
        return matrix.numFilled() == 0;
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
        if ((steps & 1023) == 0) {
            logger.debug("steps: {}", steps);
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
            // logger.info("step: {} bot: {} command: {}", steps, bot, cmd);
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
                    if (!matrix.isValid(bot.getPos())) {
                        throw new ExecutionException(cmd + ": bot moved out of matrix: " + bot.getPos() + " [steps=" + steps + "]");
                    }
                    energy += 2L * cmd.getD1().mlen();
                    break;
                case LMOVE:
                    bot.setPos(c.plus(cmd.getD1()).plus(cmd.getD2()));
                    if (!matrix.isValid(bot.getPos())) {
                        throw new ExecutionException(cmd + ": bot moved out of matrix: " + bot.getPos());
                    }
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
                case VOID:
                    c1 = c.plus(cmd.getD1());
                    if (matrix.unfill(c1)) {
                        energy -= 12;
                    } else {
                        energy += 3;
                    }
                    break;
                case FISSION:
                    Difference nd = cmd.getD1();
                    int m = cmd.getM();
                    int n = bot.getSeeds().size();
                    if (n == 0 || n <= m) {
                        throw new ExecutionException(cmd + ": too few seeds");
                    }
                    c1 = c.plus(nd);
                    if (!matrix.isValid(c1)) {
                        throw new ExecutionException(cmd + ": bot fissured out of matrix: " + bot.getPos());
                    }
                    bots.add(bot.fissure(c1, m));
                    energy += 24;
                    break;
                case FUSIONP:
                case FUSIONS:
                    if (group.size() != 2) {
                        throw new ExecutionException(cmd + ": invalid fusion group size (bots=" + bots.size() + "): " + group.size());
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
                case GFILL:
                case GVOID:
                    c1 = c.plus(cmd.getD1());
                    Region region = Region.of(c1, c1.plus(cmd.getD2()));
                    if (group.size() != (1 << region.dim())) {
                        throw new ExecutionException(cmd + ": GFill/GVoid mismatch: region "
                                + region + ", dim " + region.dim()
                                + ", but group size " + group.size()
                                + " (expected " + (1 << region.dim()) + ")");
                    }
                    if (group.stream().anyMatch(x -> x.command.getOp() != cmd.getOp())) {
                        throw new ExecutionException(cmd + ": mixture of GFill and GVoid");
                    }
                    if (!region.isValid(getResolution())) {
                        throw new ExecutionException(cmd + ": invalid region: " + region);
                    }
                    if (group.stream().anyMatch(bc1 -> region.contains(bc1.bot.getPos()))) {
                        throw new ExecutionException(cmd + ": bot inside region");
                    }
                    if (group.stream().map(x -> x.bot.getPos().plus(x.command.getD1()))
                            .collect(Collectors.toSet()).size() < group.size())
                    {
                        throw new ExecutionException("multiple bots on region edge");
                    }
                    region.coordinates().forEach(c2 -> {
                        if (cmd.getOp() == Command.Op.GFILL) {
                            if (matrix.fill(c2)) {
                                energy += 12;
                            } else {
                                energy += 6;
                            }
                        } else {
                            if (matrix.unfill(c2)) {
                                energy -= 12;
                            } else {
                                energy += 3;
                            }
                        }
                    });
                    break;
                default:
                    throw new AssertionError();
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

        public String toString() {
            return bot + ": " + command;
        }
    }

    private static class GroupKey {
        private final Nanobot single;
        private final Coordinate fusion;
        private final Region region;

        public GroupKey(BotCommand bc) {
            Nanobot bot = bc.bot;
            Command cmd = bc.command;
            switch (cmd.getOp()) {
                case FUSIONP:
                case FUSIONS:
                    fusion = cmd.getFusionPosition(bc.bot.getPos());
                    single = null;
                    region = null;
                    break;
                case GFILL:
                case GVOID:
                    Coordinate c1 = bot.getPos().plus(cmd.getD1());
                    Coordinate c2 = c1.plus(cmd.getD2());
                    region = Region.of(c1, c2);
                    single = null;
                    fusion = null;
                    break;
                default:
                    single = bot;
                    fusion = null;
                    region = null;
                    break;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            GroupKey other = (GroupKey) o;
            if (single != null) {
                return single.equals(other.single);
            } else if (fusion != null) {
                return fusion.equals(other.fusion);
            } else {
                return region.equals(other.region);
            }
        }

        @Override
        public int hashCode() {
            if (single != null) {
                return single.hashCode();
            } else if (fusion != null) {
                return fusion.hashCode();
            } else {
                return region.hashCode();
            }
        }
    }


    public boolean isWellFormed() {
        if (harmonics == HarmonicsState.LOW) {
            if (!matrix.allGrounded()) {
                logger.info("not all grounded");
                return false;
            }
        }
        if (bots.stream().anyMatch(b -> matrix.isFull(b.getPos()))) {
            logger.info("bot at full");
            return false;
        }
        if (bots.stream().anyMatch(b -> bots.stream().anyMatch(b1 -> b.getPos().equals(b1.getPos()) && !b.equals(b1)))) {
            // bot positions not disjunct
            logger.info("bot positions not disjunct");
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

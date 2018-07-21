package org.astormofminds.icfpc2018.solver;

import org.astormofminds.icfpc2018.model.Command;
import org.astormofminds.icfpc2018.model.Difference;

import java.util.ArrayList;
import java.util.List;

public class Optimizer {

    //this all just works based on the stupid / stupid bounding approach where single steps and layering are used
    //todo: all these methods are probably super inefficient and could be made faster by being smarter

    public static void removeMoves(List<Command> cmds) {
        int end = cmds.size();
        int i = end - 3;
        while (i >= 0) {
            Command now = cmds.get(i);
            Command next = cmds.get(i + 1);
            Command afterNext = cmds.get(i + 2);
            if (now.getOp() == Command.Op.SMOVE &&
                    afterNext.getOp() == Command.Op.SMOVE &&
                    (now.equals(Command.UP) && afterNext.equals(Command.DOWN) ||
                            now.equals(Command.DOWN) && afterNext.equals(Command.UP) ||
                            now.equals(Command.LEFT) && afterNext.equals(Command.RIGHT) ||
                            now.equals(Command.RIGHT) && afterNext.equals(Command.LEFT) ||
                            now.equals(Command.FAR) && afterNext.equals(Command.NEAR) ||
                            now.equals(Command.NEAR) && afterNext.equals(Command.FAR))
                    && next.getOp() != Command.Op.FILL)
            {
                cmds.remove(i+2);
                cmds.remove(i);
                end -= 2;
                i = Math.min(i, end - 3);
            } else {
                i--;
            }
        }
    }

    public static void combineStraightMoves(List<Command> commands) {
        boolean updated = true;
        int start = 0;
        while (updated) {
            updated = false;
            Command current = null;
            int streak = 1;
            for (int i = start; i < commands.size(); i++) {
                if (commands.get(i).equals(current)) {
                    streak++;
                    if (streak == 15) {
                        Command newCommand;
                        if (current.equals(Command.DOWN)) {
                            newCommand = Command.sMove(Difference.of(0, - streak, 0));
                        } else if (current.equals(Command.UP)) {
                            newCommand = Command.sMove(Difference.of(0, streak, 0));
                        } else if (current.equals(Command.LEFT)) {
                            newCommand = Command.sMove(Difference.of(- streak, 0, 0));
                        } else if (current.equals(Command.RIGHT)) {
                            newCommand = Command.sMove(Difference.of(streak, 0, 0));
                        } else if (current.equals(Command.FAR)) {
                            newCommand = Command.sMove(Difference.of(0, 0, streak));
                        } else if (current.equals(Command.NEAR)) {
                            newCommand = Command.sMove(Difference.of(0, 0, - streak));
                        } else {
                            current = null;
                            streak = 1;
                            continue;
                        }
                        for (int index = i - streak + 1; index <= i; index++) {
                            commands.remove(i - streak + 1);
                        }
                        commands.add(i - streak + 1, newCommand);
                        start = Math.max(i - streak + 1, 0);
                        updated = true;
                        break;
                    }
                } else {
                    if (streak > 1) {
                        Command newCommand;
                        if (current.equals(Command.DOWN)) {
                            newCommand = Command.sMove(Difference.of(0, - streak, 0));
                        } else if (current.equals(Command.UP)) {
                            newCommand = Command.sMove(Difference.of(0, streak, 0));
                        } else if (current.equals(Command.LEFT)) {
                            newCommand = Command.sMove(Difference.of(- streak, 0, 0));
                        } else if (current.equals(Command.RIGHT)) {
                            newCommand = Command.sMove(Difference.of(streak, 0, 0));
                        } else if (current.equals(Command.FAR)) {
                            newCommand = Command.sMove(Difference.of(0, 0, streak));
                        } else if (current.equals(Command.NEAR)) {
                            newCommand = Command.sMove(Difference.of(0, 0, - streak));
                        } else {
                            current = commands.get(i);
                            streak = 1;
                            continue;
                        }
                        for (int index = i - streak; index < i; index++) {
                            commands.remove(i - streak);
                        }
                        commands.add(i - streak, newCommand);
                        start = Math.max(i - streak, 0);
                        updated = true;
                        break;
                    }
                    current = commands.get(i);
                    streak = 1;
                }
            }
        }
    }

    public static void moveFlipsInward(List<Command> commands) {
        int index = 0;
        int firstFlip = -1;
        int lastFlip = commands.size() + 1;
        int firstFill = -1;
        int lastFill = commands.size() + 1;
        for (Command command : commands) {
            if (command.getOp() == Command.Op.FLIP) {
                if (firstFlip == -1) {
                    firstFlip = index;
                } else {
                    lastFlip = index;
                }
            }
            if (command.getOp() == Command.Op.FILL) {
                if (firstFill == -1) {
                    firstFill = index;
                } else {
                    lastFill = index;
                }
            }
            index++;
        }

        if (firstFlip > firstFill || lastFill > lastFlip ||
                firstFlip == -1 || lastFlip == commands.size() + 1 ||
                firstFill == -1 || lastFill == commands.size() + 1) {
            System.out.println("Flip: " + firstFlip + "/" + lastFlip + " - Fill: " + firstFill + "/" + lastFill);
            throw new BrokenFillAndFlipOrderException();
        }
        commands.add(firstFill, Command.FLIP);
        commands.remove(firstFlip);
        commands.remove(lastFlip);
        //last flip can happen before last fill
        commands.add(lastFill, Command.FLIP);
    }

}

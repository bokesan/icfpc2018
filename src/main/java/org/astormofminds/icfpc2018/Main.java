package org.astormofminds.icfpc2018;

import org.astormofminds.icfpc2018.io.Binary;
import org.astormofminds.icfpc2018.model.Command;
import org.astormofminds.icfpc2018.model.ExecutionException;
import org.astormofminds.icfpc2018.model.Matrix;
import org.astormofminds.icfpc2018.model.State;
import org.astormofminds.icfpc2018.solver.Solver;
import org.astormofminds.icfpc2018.solver.SolverFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private static final String prog = "asof";

    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            usage(0);
        }
        switch (args[0]) {
            case "help":
                usage((args.length == 1) ? 0 : 1);
                break;
            case "check":
                if (args.length != 2) {
                    usage(1);
                }
                check(args[1]);
                break;
            case "exec":
                if (args.length != 3) {
                    usage(1);
                }
                exec(args[1], args[2]);
                break;
            case "solve":
                if (args.length != 4) {
                    usage(1);
                }
                solve(args[1], args[2], args[3]);
                break;
            case "solveAll":
                if (args.length != 5) {
                    usage(1);
                }
                solveAll(args[1], args[2], args[3], args[4].split(","));
                break;
            default:
                usage(1);
        }
    }

    private static void solve(String modelFile, String traceFile, String solverName) throws IOException {
        Solver solver = SolverFactory.byName(solverName);
        Matrix model = Binary.readModel(new FileInputStream(modelFile));
        if (modelFile.contains("_tgt.mdl")) {
            solver.initAssemble(model);
        } else if (modelFile.contains("_src.mdl")) {
            solver.initDeconstruct(model);
        } else {
            throw new UnsupportedOperationException("Cannot process model " + modelFile);
        }
        List<Command> trace = solver.getCompleteTrace();
        Binary.writeTrace(traceFile, trace);
    }

    private static void solveAll(String problemFolder, String problemPrefix, String traceFolder, String[] solverNames) throws IOException {
        long startTime = System.nanoTime();
        File targetDir = new File(problemFolder);
        File[] targets = targetDir.listFiles((dir, name) -> name.startsWith(problemPrefix) && name.endsWith(".mdl"));
        List<File> targetFiles = Arrays.asList(targets);
        System.out.print("ID;R;default");
        for (String solver : solverNames) {
            System.out.print(";" + solver);
        }
        System.out.println(";bestSolver;bestEnergy");
        targetFiles.parallelStream()
                .map(t -> {
                    try {
                        return solveProblem(traceFolder, solverNames, t);
                    } catch (IOException e) {
                        return t.getName() + ": " + e;
                    }
                })
                .forEach(r -> System.out.println(r));
        long elapsedTime = System.nanoTime() - startTime;
        System.out.format("Elapsed time: %.3f seconds.%n", elapsedTime / 1.0e9);
    }

    private static String solveProblem(String traceFolder, String[] solverNames, File target) throws IOException {
        String id = target.getName();
        id = id.substring(0, id.length() - 8);
        String traceFile = traceFolder + "/" + id + ".nbt";
        try (InputStream in = new BufferedInputStream(new FileInputStream(target));
             InputStream tin = new BufferedInputStream(new FileInputStream(traceFile))
        ) {
            Matrix model = Binary.readModel(in);
            List<Command> defaultTrace = Binary.readTrace(tin);
            State dfltResult = execute(model, defaultTrace);
            if (dfltResult == null) {
                return(id + ": invalid default trace.");
            } else {
                long bestEnergy = dfltResult.getEnergy();
                String bestSolver = "default";
                List<Command> bestTrace = null;
                String r = String.format("%s;%d;%d", id, model.getResolution(), bestEnergy);
                for (String solverName : solverNames) {
                    Solver solver = SolverFactory.byName(solverName);
                    solver.initAssemble(model);
                    List<Command> trace = solver.getCompleteTrace();
                    State ownResult = execute(model, trace);
                    if (ownResult == null) {
                        r += ";invalid";
                    } else {
                        r += String.format(";%d", ownResult.getEnergy());
                        if (ownResult.getEnergy() < bestEnergy) {
                            bestEnergy = ownResult.getEnergy();
                            bestSolver = solverName;
                            bestTrace = trace;
                        }
                    }
                }
                if (bestTrace != null) {
                    Binary.writeTrace("out/" + id + ".nbt", bestTrace);
                }
                r += ";" + bestSolver + ";" + bestEnergy;
                return r;
            }
        }
    }

    private static void usage(int exitCode) {
        System.out.println("usage:");
        System.out.println("  help                  show this help text");
        System.out.println("  check <file>          decode file and show content");
        System.out.println("  exec <model> <trace>  execute trace on model");
        System.out.println("  checkAll <problemsFolder> <prefix> <tracesFolder> solver1,solver2,...  run all solvers on all problems strating with prefix");
        System.exit(exitCode);
    }

    private static void check(String file) throws IOException {
        if (file.endsWith(".mdl")) {
            checkModel(file);
        } else if (file.endsWith(".nbt")) {
            checkTrace(file);
        } else {
            throw new AssertionError("file type not yet supported: " + file);
        }
    }

    private static void checkModel(String file) throws IOException {
        try (InputStream in = new FileInputStream(file)) {
            Matrix model = Binary.readModel(in);
            System.out.println("Model successfully read: " + model);
        }
    }


    private static final int EXCERPT_SIZE = 10;

    private static void checkTrace(String file) throws IOException {
        try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
            List<Command> trace = Binary.readTrace(in);
            System.out.println("Trace successfully read:");
            int n = trace.size();
            int head;
            int tail;
            if (n <= 2 * EXCERPT_SIZE) {
                head = n;
                tail = 0;
            } else {
                head = EXCERPT_SIZE;
                tail = n - EXCERPT_SIZE;
            }
            for (int i = 0; i < head; i++) {
                System.out.format("%5d: %s%n", i, trace.get(i));
            }
            if (tail > 0) {
                System.out.println("...");
                for (int i = tail; i < n; i++) {
                    System.out.format("%5d: %s%n", i, trace.get(i));
                }
            }
            System.out.format("Total number of commands: %d%n", n);
            trace.stream()
                 .collect(Collectors.groupingBy(Command::getOp, Collectors.counting()))
                 .entrySet()
                 .stream()
                 .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                 .forEach(e -> System.out.format("%10d %s%n", e.getValue(), e.getKey()));
        }
    }

    private static void exec(String modelFile, String traceFile) throws IOException {
        try (InputStream ms = new BufferedInputStream(new FileInputStream(modelFile));
             InputStream ts = new BufferedInputStream(new FileInputStream(traceFile))) {
            logger.info("loading target model...");
            Matrix model = Binary.readModel(ms);
            logger.info("loading trace...");
            List<Command> trace = Binary.readTrace(ts);
            State state = new State(model.getResolution(), trace);
            long t0 = System.nanoTime();
            for (;;) {
                if (!state.timeStep()) {
                    break;
                }
            }
            long elapsed = System.nanoTime() - t0;
            if (state.isValidFinalState(model)) {
                System.out.println("Trace correct.");
            } else {
                System.out.println("Trace invalid.");
            }
            System.out.println("Finale state: " + state);
            System.out.format("Elapsed time: %.1f ms%n", elapsed / 1.0e6);
        }
    }

    private static State execute(Matrix target, List<Command> trace) {
        try {
            State state = new State(target.getResolution(), trace);
            for (; ; ) {
                if (!state.timeStep()) {
                    break;
                }
            }
            if (state.isValidFinalState(target)) {
                return state;
            } else {
                return null;
            }
        } catch (ExecutionException ex) {
            logger.error("Execution exception: {}", ex.getMessage());
            return null;
        }
    }
}

package org.astormofminds.icfpc2018;

import org.astormofminds.icfpc2018.io.Binary;
import org.astormofminds.icfpc2018.model.*;
import org.astormofminds.icfpc2018.solver.Solver;
import org.astormofminds.icfpc2018.solver.SolverFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    private static final boolean PARALLEL = false;
    private static final boolean TEST_DEFAULT_TRACE = false;
    private static final boolean STOP_ON_ERROR = true;

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
        if (!PARALLEL) {
            Arrays.sort(targets);
        }
        System.out.print("ID;R");
        if (TEST_DEFAULT_TRACE) {
            System.out.print(";default");
        }
        for (String solver : solverNames) {
            System.out.print(";" + solver);
        }
        System.out.println(";bestSolver;bestEnergy");
        Stream<File> fileStream = Arrays.stream(targets);
        if (PARALLEL) {
            fileStream = fileStream.parallel();
        }
        fileStream.map(t -> {
                    try {
                        return solveProblem(traceFolder, solverNames, t);
                    } catch (IOException e) {
                        logger.error("IO Exception", e);
                        return t.getName() + ": " + e;
                    }
                })
                .forEach(r -> System.out.println(r));
        long elapsedTime = System.nanoTime() - startTime;
        System.out.format("Elapsed time: %.3f seconds.%n", elapsedTime / 1.0e9);
    }

    enum ProblemMode {
        ASSEMBLE,
        DESTRUCT,
        RECONSTRUCT;

        static ProblemMode of(char c) {
            switch (c) {
                case 'A': return ASSEMBLE;
                case 'D': return DESTRUCT;
                case 'R': return RECONSTRUCT;
                default:
                    throw new IllegalArgumentException("invalid mode: " + c);
            }
        }
    }

    private static String solveProblem(String traceFolder, String[] solverNames, File target) throws IOException {
        String id = target.getName();
        id = id.substring(0, id.length() - 8);
        ProblemMode mode = ProblemMode.of(id.charAt(1));
        String traceFile = traceFolder + "/" + id + ".nbt";
        try (InputStream in = new BufferedInputStream(new FileInputStream(target));
             InputStream tin = new BufferedInputStream(new FileInputStream(traceFile))
        ) {
            Matrix model = Binary.readModel(in);
            long bestEnergy = Long.MAX_VALUE;
            String bestSolver = "-";
            if (TEST_DEFAULT_TRACE) {
                List<Command> defaultTrace = Binary.readTrace(tin);
                State dfltResult = execute(id, "default", mode, model, defaultTrace);
                if (dfltResult == null) {
                    return id + ": invalid default trace";
                }
                bestEnergy = dfltResult.getEnergy();
                bestSolver = "default";
            }
            List<Command> bestTrace = null;
            String r = String.format("%s;%d", id, model.getResolution());
            if (TEST_DEFAULT_TRACE) {
                r += String.format(";%d", bestEnergy);
            }
            for (String solverName : solverNames) {
                long startTime = System.nanoTime();
                Solver solver = SolverFactory.byName(solverName);
                List<Command> trace = null;
                switch (mode) {
                    case ASSEMBLE:
                        if (solver.initAssemble(new Matrix(model))) {
                            trace = solver.getCompleteTrace();
                        }
                        break;
                    case DESTRUCT:
                        if (solver.initDeconstruct(new Matrix(model))) {
                            trace = solver.getCompleteTrace();
                        }
                        break;
                    default:
                        throw new AssertionError("not implemented");
                }
                State result;
                if (trace == null) {
                    result = null;
                    logger.error("generating trace failed for {} with solver: {}", id, solverName);
                } else {
                    long elapsed = System.nanoTime() - startTime;
                    logger.info("generate trace for {} with solver {}: {}s", id, solverName,
                            String.format("%.3f", elapsed / 1.0e9));
                    result = execute(id, solverName, mode, model, trace);
                }
                if (result == null) {
                    if (STOP_ON_ERROR) {
                        System.exit(1);
                    }
                    r += ";invalid";
                } else {
                    r += String.format(";%d", result.getEnergy());
                    if (result.getEnergy() < bestEnergy) {
                        bestEnergy = result.getEnergy();
                        bestSolver = solverName;
                        bestTrace = trace;
                    }
                }
            }
            if (bestTrace != null) {
                Binary.writeTrace("out/" + id + ".nbt", bestTrace);
                r += ";" + bestSolver + ";" + bestEnergy;
            } else {
                r += ";INVALID;0";
            }
            return r;

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
            String id = new File(modelFile).getName().substring(0, 5);
            ProblemMode mode = ProblemMode.of(id.charAt(1));
            logger.info("loading target model...");
            Matrix model = Binary.readModel(ms);
            logger.info("loading trace...");
            List<Command> trace = Binary.readTrace(ts);
            State state = execute(id, "-", mode, model, trace);
            if (state != null) {
                System.out.println("Trace correct.");
            } else {
                System.out.println("Trace invalid.");
            }
            System.out.println("Finale state: " + state);
        }
    }

    private static State execute(String problemId, String solver, ProblemMode mode, Matrix model, List<Command> trace) {
        long startTime = System.nanoTime();
        try {
            State state;
            switch (mode) {
                case ASSEMBLE:
                    state = new State(trace, new Matrix(model.getResolution()));
                    break;
                case DESTRUCT:
                    state = new State(trace, new Matrix(model));
                    break;
                default:
                    throw new AssertionError();
            }
            while (state.timeStep())
                ;
            boolean validResult = true;
            switch (mode) {
                case ASSEMBLE:
                    if (state.getHarmonics() == HarmonicsState.LOW && !state.getMatrix().allGrounded()) {
                        logger.error("Not all grounded in low harmonics");
                        validResult = false;
                    }
                    if (!state.getBots().isEmpty()) {
                        logger.error("bots not empty");
                        validResult = false;
                    }
                    if (!state.getTrace().isEmpty()) {
                        logger.error("trace not empty");
                        validResult = false;
                    }
                    if (!state.getMatrix().equals(model)) {
                        Set<Coordinate> ts = model.filled().collect(Collectors.toSet());
                        Set<Coordinate> rs = state.getMatrix().filled().collect(Collectors.toSet());
                        logger.error("target model mismatch. Model {}, result {}", ts.size(), rs.size());
                        validResult = false;
                    }
                    break;
                case DESTRUCT:
                    int remaining = state.getMatrix().numFilled();
                    if (remaining != 0) {
                        validResult = false;
                        logger.error("matrix not empty. Solver {}, filled: {}", solver, remaining);
                    }
                    break;
                default:
                    throw new AssertionError("not implemented");
            }
            return validResult ? state : null;
        } catch (ExecutionException ex) {
            logger.error("Execution exception in problem " + problemId + " with solver '" + solver + "'", ex);
            return null;
        } finally {
            long elapsed = System.nanoTime() - startTime;
            logger.info("exec {} with solver '{}': {}s", problemId, solver, String.format("%.3f", elapsed / 1.0e9));
        }
    }
}

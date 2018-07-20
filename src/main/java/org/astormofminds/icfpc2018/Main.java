package org.astormofminds.icfpc2018;

import org.astormofminds.icfpc2018.io.Binary;
import org.astormofminds.icfpc2018.model.Command;
import org.astormofminds.icfpc2018.model.Matrix;
import org.astormofminds.icfpc2018.model.State;
import org.astormofminds.icfpc2018.solver.Solver;
import org.astormofminds.icfpc2018.solver.SolverFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class Main {

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
            default:
                usage(1);
        }
    }

    private static void solve(String modelFile, String traceFile, String solverName) throws IOException {
        Solver solver = SolverFactory.byName(solverName);
        Matrix model = Binary.readModel(new FileInputStream(modelFile));
        solver.init(model);
        List<Command> trace = solver.getCompleteTrace();
        Binary.writeTrace(traceFile, trace);
    }

    private static void usage(int exitCode) {
        System.out.println("usage:");
        System.out.println("  asof help                  show this help text");
        System.out.println("  asof check <file>          decode file and show content");
        System.out.println("  asof exec <model> <trace>  execute trace on model");
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
            System.out.println("Model successfully read");
        }
    }


    private static final int EXCERT_SIZE = 10;

    private static void checkTrace(String file) throws IOException {
        try (InputStream in = new FileInputStream(file)) {
            List<Command> trace = Binary.readTrace(in);
            System.out.println("Trace successfully read:");
            int n = trace.size();
            int head;
            int tail;
            if (n <= 2 * EXCERT_SIZE) {
                head = n;
                tail = 0;
            } else {
                head = EXCERT_SIZE;
                tail = n - EXCERT_SIZE;
            }
            for (int i = 0; i < head; i++) {
                System.out.format("%5d: %s\n", i, trace.get(i));
            }
            if (tail > 0) {
                System.out.println("...");
                for (int i = tail; i < n; i++) {
                    System.out.format("%5d: %s\n", i, trace.get(i));
                }
            }
        }
    }

    private static void exec(String modelFile, String traceFile) {
        throw new AssertionError("not yet implmented");
    }

}

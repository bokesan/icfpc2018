package org.astormofminds.icfpc2018.io;

import org.astormofminds.icfpc2018.model.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Binary {

    public static Matrix readModel(InputStream in) throws IOException {
        int resolution = in.read();
        if (resolution <= 0 || resolution > 250) {
            throw new IOException("invalid resolution (corrupt model file?): " + resolution);
        }
        Matrix matrix = new Matrix(resolution);
        BitInputStream bits = new BitInputStream(in);
        for (int x = 0; x < resolution; x++) {
            for (int y = 0; y < resolution; y++) {
                for (int z = 0; z < resolution; z++) {
                    if (bits.nextBit()) {
                        matrix.fill(Coordinate.of(x, y, z));
                    }
                }
            }
        }
        return matrix;
    }

    public static List<Command> readTrace(InputStream in) throws IOException {
        List<Command> trace = new ArrayList<>(10000);
        for (;;) {
            int b = in.read();
            if (b < 0) {
                break;
            }
            Command cmd;

            if (b == 0b11111111) {
                cmd = Command.HALT;
            }
            else if (b == 0b11111110) {
                cmd = Command.WAIT;
            }
            else if (b == 0b11111101) {
                cmd = Command.FLIP;
            }
            else if ((b & 0b11001111) == 0b00000100) {
                int axis = (b & 0b110000) >>> 4;
                int b2 = in.read();
                cmd = Command.sMove(decodeLld(axis, b2));
            }
            else if ((b & 0b1111) == 0b1100) {
                int axis1 = (b & 0b110000) >>> 4;
                int axis2 = (b & 0b11000000) >>> 6;
                int b2 = in.read();
                Difference sld1 = decodeSld(axis1, b2 & 0b1111);
                Difference sld2 = decodeSld(axis2, b2 >>> 4);
                cmd = Command.lMove(sld1, sld2);
            }
            else {
                Difference nd = decodeNd(b >>> 3);
                switch (b & 0b111) {
                    case 0b000: cmd = Command.gVoid(nd, readFarDistance(in)); break;
                    case 0b001: cmd = Command.gFill(nd, readFarDistance(in)); break;
                    case 0b010: cmd = Command.void_(nd); break;
                    case 0b011: cmd = Command.fill(nd); break;
                    case 0b101: cmd = Command.fission(nd, in.read()); break;
                    case 0b110: cmd = Command.fusionS(nd); break;
                    case 0b111: cmd = Command.fusionP(nd); break;
                    default:
                        throw new IOException("invalid command encoding: " + Integer.toBinaryString(b));
                }
            }
            trace.add(cmd);
        }
        return trace;
    }

    public static void writeTrace(String filename, Iterable<Command> trace) throws IOException {
        if (!filename.endsWith(".nbt")) {
            throw new IllegalArgumentException("invalid trace file name (should end with '.nbt'): " + filename);
        }
        try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(filename))) {
            writeTrace(out, trace);
        }
    }

    public static void writeTrace(OutputStream out, Iterable<Command> trace) throws IOException {
        for (Command cmd : trace) {
            cmd.encode(out);
        }
    }

    private static Difference decodeNd(int i) {
        int dz = (i % 3) - 1;
        i /= 3;
        int dy = (i % 3) - 1;
        i /= 3;
        int dx = i - 1;
        return Difference.of(dx, dy, dz);
    }

    private static Difference decodeSld(int axis, int i) throws IOException {
        return decodeDifference(axis, i - 5);
    }

    private static Difference decodeLld(int axis, int i) throws IOException {
        return decodeDifference(axis, i - 15);
    }

    private static Difference decodeDifference(int axis, int d) throws IOException {
        switch (axis) {
            case 1: return Difference.of(d, 0, 0);
            case 2: return Difference.of(0, d, 0);
            case 3: return Difference.of(0, 0, d);
            default:
                throw new IOException("invalid axis: " + axis);
        }
    }

    private static Difference readFarDistance(InputStream in) throws IOException {
        int dx = in.read() - 30;
        int dy = in.read() - 30;
        int dz = in.read() - 30;
        return Difference.of(dx, dy, dz);
    }

}

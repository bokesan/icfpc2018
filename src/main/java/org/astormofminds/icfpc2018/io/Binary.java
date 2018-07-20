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
        List<Command> trace = new ArrayList<>();
        for (;;) {
            int b = in.read();
            if (b < 0) {
                break;
            }
            Command command;

            if (b == 0b11111111) {
                command = Command.HALT;
            }
            else if (b == 0b11111110) {
                command = Command.WAIT;
            }
            else if (b == 0b11111101) {
                command = Command.FLIP;
            }
            else if ((b & 0b11001111) == 0b00000100) {
                int axis = (b & 0b110000) >>> 4;
                int b2 = in.read();
                command = Command.sMove(decodeLld(axis, b2));
            }
            else if ((b & 0b1111) == 0b1100) {
                int axis1 = (b & 0b110000) >>> 4;
                int axis2 = (b & 0b11000000) >>> 6;
                int b2 = in.read();
                Difference sld1 = decodeSld(axis1, b2 & 0b1111);
                Difference sld2 = decodeSld(axis2, b2 >>> 4);
                command = Command.lMove(sld1, sld2);
            }
            else if ((b & 0b111) == 0b111) {
                command = Command.fusionP(decodeNd(b >>> 3));
            }
            else if ((b & 0b111) == 0b110) {
                command = Command.fusionS(decodeNd(b >>> 3));
            }
            else if ((b & 0b111) == 0b101) {
                Difference nd = decodeNd(b >>> 3);
                int b2 = in.read();
                command = Command.fission(nd, b2);
            }
            else if ((b & 0b111) == 0b011) {
                Difference nd = decodeNd(b >>> 3);
                command = Command.fill(nd);
            }
            else {
                throw new IOException("invalid command encoding: " + Integer.toBinaryString(b));
            }
            trace.add(command);
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

}

package org.astormofminds.icfpc2018.io;

import org.astormofminds.icfpc2018.model.Coordinate;
import org.astormofminds.icfpc2018.model.State;

import java.io.IOException;
import java.io.InputStream;

public class Binary {

    public static State readModel(InputStream in) throws IOException {
        int resolution = in.read();
        if (resolution <= 0 || resolution > 250) {
            throw new IOException("invalid resolution (corrupt model file?): " + resolution);
        }
        State state = new State(resolution);
        BitInputStream bits = new BitInputStream(in);
        for (int x = 0; x < resolution; x++) {
            for (int y = 0; y < resolution; y++) {
                for (int z = 0; z < resolution; z++) {
                    if (bits.nextBit()) {
                        state.fill(Coordinate.of(x, y, z));
                    }
                }
            }
        }
        return state;
    }

}

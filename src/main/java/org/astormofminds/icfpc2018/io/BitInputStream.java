package org.astormofminds.icfpc2018.io;

import java.io.IOException;
import java.io.InputStream;

public class BitInputStream {

    private final InputStream in;

    private int currentByte;
    private int mask;

    public BitInputStream(InputStream in) {
        this.in = in;
        this.mask = 128;
    }

    public boolean nextBit() throws IOException {
        mask <<= 1;
        if (mask > 128) {
            currentByte = in.read();
            if (currentByte < 0) {
                throw new IOException("end of file reached");
            }
            mask = 1;
        }
        return (currentByte & mask) != 0;
    }

}

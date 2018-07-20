package org.astormofminds.icfpc2018.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DifferenceTests {

    @Test
    public void testMlen() {
        Difference d = Difference.of(2, 3, 4);
        assertEquals(9, d.mlen());
        d = Difference.of(2, -3, 4);
        assertEquals(9, d.mlen());
    }

    @Test
    public void testClen() {
        Difference d = Difference.of(2, 3, 4);
        assertEquals(4, d.clen());
        d = Difference.of(2, 3, -4);
        assertEquals(4, d.clen());
    }
}

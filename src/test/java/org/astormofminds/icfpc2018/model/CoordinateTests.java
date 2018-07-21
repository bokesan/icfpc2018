package org.astormofminds.icfpc2018.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CoordinateTests {

    @Test
    public void testIsAdjacentTo() {
        Coordinate c1 = Coordinate.of(3, 3, 3);
        assertTrue(c1.isAdjacentTo(Coordinate.of(3, 3, 4)));
        assertTrue(c1.isAdjacentTo(Coordinate.of(3, 3, 2)));
        assertTrue(c1.isAdjacentTo(Coordinate.of(3, 4, 3)));
        assertTrue(c1.isAdjacentTo(Coordinate.of(3, 2, 3)));
        assertTrue(c1.isAdjacentTo(Coordinate.of(4, 3, 3)));
        assertTrue(c1.isAdjacentTo(Coordinate.of(2, 3, 3)));
        assertFalse(c1.isAdjacentTo(Coordinate.of(1, 3, 3)));
    }

    @Test
    public void notAdjacantToSelf() {
        Coordinate c1 = Coordinate.of(3, 3, 3);
        assertFalse(c1.isAdjacentTo(c1));
    }

    @Test
    public void testPlus() {
        Coordinate c = Coordinate.of(10, 20, 30);
        Difference d = Difference.of(5, -5, 7);
        assertEquals(Coordinate.of(15, 15, 37), c.plus(d));
    }

    @Test
    public void testPlusLarge() {
        Coordinate c = Coordinate.of(0, 249, 22);
        Difference d = Difference.of(249, -249, 7);
        assertEquals(Coordinate.of(249, 0, 29), c.plus(d));
    }
}

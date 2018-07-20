package org.astormofminds.icfpc2018.model;

import org.junit.Test;

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

}

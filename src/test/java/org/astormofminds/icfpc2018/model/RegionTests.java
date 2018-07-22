package org.astormofminds.icfpc2018.model;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RegionTests {

    @Test
    public void testNormalization() {
        Coordinate c1 = Coordinate.of(10, 20, 10);
        Coordinate c2 = Coordinate.of(20, 10, 20);
        Region region = Region.of(c1, c2);
        assertEquals(Coordinate.of(10, 10, 10), region.getLbn());
        assertEquals(Coordinate.of(20, 20, 20), region.getRtf());
    }

    @Test
    public void testContains() {
        Coordinate c1 = Coordinate.of(10, 20, 20);
        Coordinate c2 = Coordinate.of(20, 10, 10);
        Region region = Region.of(c1, c2);
        assertTrue(region.contains(Coordinate.of(15, 15, 15)));
        assertTrue(region.contains(Coordinate.of(10, 10, 10)));
        assertTrue(region.contains(Coordinate.of(20, 20, 20)));
        assertTrue(region.contains(Coordinate.of(10, 10, 20)));

        assertFalse(region.contains(Coordinate.of(9, 12, 12)));
        assertFalse(region.contains(Coordinate.of(12, 9, 12)));
        assertFalse(region.contains(Coordinate.of(12, 12, 9)));
        assertFalse(region.contains(Coordinate.of(21, 16, 16)));
        assertFalse(region.contains(Coordinate.of(16, 21, 16)));
        assertFalse(region.contains(Coordinate.of(16, 16, 21)));
        assertFalse(region.contains(Coordinate.ORIGIN));
    }

    @Test
    public void testContainsInvalid() {
        Region region = Region.of(Coordinate.ORIGIN, Coordinate.of(1,1,1));
        assertFalse(region.contains(Coordinate.of(-1, 0, 0)));
        assertFalse(region.contains(Coordinate.of(0, 0, 251)));
    }

    @Test
    public void testIsInvalid() {
        Coordinate c1 = Coordinate.of(10, 20, 20);
        Coordinate c2 = Coordinate.of(20, 10, 10);
        Region region = Region.of(c1, c2);

        assertTrue(region.isValid(30));
        assertTrue(region.isValid(21));
        assertFalse(region.isValid(20));
        assertFalse(region.isValid(12));
        assertFalse(region.isValid(1));
        assertFalse(region.isValid(0));
        assertFalse(region.isValid(-1));
    }
}

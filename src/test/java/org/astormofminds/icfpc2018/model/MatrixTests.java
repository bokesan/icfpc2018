package org.astormofminds.icfpc2018.model;

import org.junit.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class MatrixTests {

    @Test
    public void testNumFilled() {
        Matrix m = new Matrix(10);
        m.fill(Coordinate.of(1, 1, 1));
        m.fill(Coordinate.of(1, 2, 1));
        m.fill(Coordinate.of(6, 4, 9));
        assertEquals(3, m.numFilled());
    }

    @Test
    public void testFilled() {
        Coordinate c1 = Coordinate.of(1, 1, 1);
        Coordinate c2 = Coordinate.of(5, 1, 7);
        Coordinate c3 = Coordinate.of(3, 0, 4);
        Matrix m = new Matrix(10);
        m.fill(c1);
        m.fill(c2);
        m.fill(c3);
        Set<Coordinate> filled = m.filled().collect(Collectors.toSet());
        assertEquals(3, filled.size());
        assertTrue(filled.contains(c1));
        assertTrue(filled.contains(c2));
        assertTrue(filled.contains(c3));
    }

    @Test
    public void testVoxelState() {
        Coordinate c1 = Coordinate.of(1, 1, 1);
        Coordinate c2 = Coordinate.of(5, 1, 7);
        Coordinate c3 = Coordinate.of(3, 0, 4);
        Coordinate c4 = Coordinate.of(3, 0, 3);
        Matrix m = new Matrix(10);
        m.fill(c1);
        m.fill(c2);
        m.fill(c3);
        assertEquals(VoxelState.FULL, m.get(c1));
        assertEquals(VoxelState.FULL, m.get(c2));
        assertEquals(VoxelState.FULL, m.get(c3));
        assertEquals(VoxelState.VOID, m.get(c4));
    }

    @Test
    public void testGrounded() {
        Coordinate c1 = Coordinate.of(1, 0, 1);
        Coordinate c2 = Coordinate.of(1, 1, 1);
        Coordinate c3 = Coordinate.of(2, 1, 1);
        Coordinate c4 = Coordinate.of(3, 7, 3);
        Matrix m = new Matrix(10);
        m.fill(c1);
        m.fill(c2);
        m.fill(c3);
        m.fill(c4);
        assertTrue(m.isGrounded(c1));
        assertTrue(m.isGrounded(c2));
        assertTrue(m.isGrounded(c3));
        assertFalse(m.isGrounded(c4));
    }

}

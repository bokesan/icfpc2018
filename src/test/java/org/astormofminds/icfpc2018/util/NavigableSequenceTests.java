package org.astormofminds.icfpc2018.util;

import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class NavigableSequenceTests {

    @Test
    public void testDelete() {
        ArrayList<String> lyst = Lists.newArrayList("A", "B", "C", "D", "E");
        NavigableSequence<String> seq = new NavigableSequence<>(lyst);
        assertEquals(5, seq.size());
        int current = seq.getIndex(2);
        current = seq.remove(current);
        assertEquals(4, seq.size());
        assertEquals("D", seq.fetch(current));
        current = seq.prev(current);
        assertEquals("B", seq.fetch(current));
        seq.compact();
        assertEquals(4, lyst.size());
        assertEquals("A", lyst.get(0));
        assertEquals("B", lyst.get(1));
        assertEquals("D", lyst.get(2));
        assertEquals("E", lyst.get(3));
    }

    @Test
    public void testDeleteFirst() {
        ArrayList<String> lyst = Lists.newArrayList("A", "B", "C", "D", "E");
        NavigableSequence<String> seq = new NavigableSequence<>(lyst);
        assertEquals(5, seq.size());
        int current = seq.getIndex(0);
        current = seq.remove(current);
        assertEquals(1, current);
        assertEquals(4, seq.size());
        assertEquals("B", seq.fetch(current));
        seq.compact();
        assertEquals(4, lyst.size());
        assertEquals("B", lyst.get(0));
        assertEquals("C", lyst.get(1));
        assertEquals("D", lyst.get(2));
        assertEquals("E", lyst.get(3));
    }

    @Test
    public void testDeleteLast() {
        ArrayList<String> lyst = Lists.newArrayList("A", "B", "C", "D", "E");
        NavigableSequence<String> seq = new NavigableSequence<>(lyst);
        assertEquals(5, seq.size());
        int current = seq.getIndex(4);
        current = seq.remove(current);
        assertEquals(3, current);
        assertEquals(4, seq.size());
        assertEquals("D", seq.fetch(current));
        seq.compact();
        assertEquals(4, lyst.size());
        assertEquals("A", lyst.get(0));
        assertEquals("B", lyst.get(1));
        assertEquals("C", lyst.get(2));
        assertEquals("D", lyst.get(3));
    }

}

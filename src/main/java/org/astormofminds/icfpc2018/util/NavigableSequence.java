package org.astormofminds.icfpc2018.util;

import java.util.ArrayList;

/**
 * A navigation layer over an ArrayList. Only deleting or replacing items is possible, but adding is not.
 * You should not modify the ArrayList in any way while using methods of this class.
 * You must call compact when finished.
 */
public class NavigableSequence<T> {

    private final int[] nextPos;
    private final int[] prevPos;
    private final ArrayList<T> items;

    private int size;
    private int firstIndex;

    public NavigableSequence(ArrayList<T> elems) {
        size = elems.size();
        items = elems;
        nextPos = new int[size];
        prevPos = new int[size];
        int n = size - 1;
        for (int i = 1; i < n; i++) {
            nextPos[i] = i + 1;
            prevPos[i] = i - 1;
        }
        nextPos[0] = 1;
        nextPos[n] = -1;
        prevPos[0] = -1;
        prevPos[n] = n - 1;
        firstIndex = 0;
    }

    public int size() {
        return size;
    }

    public T fetch(int index) {
        return items.get(index);
    }

    public T put(int index, T value) {
        return items.set(index, value);
    }

    public int remove(int index) {
        size--;
        int p = prevPos[index];
        int s = nextPos[index];
        if (p < 0) {
            // this was the first item
            firstIndex = s;
        } else {
            nextPos[p] = s;
        }
        if (s >= 0) {
            prevPos[s] = p;
        }
        return Math.max(p, s);
    }

    /**
     * Get the index of the n'th item.
     */
    public int getIndex(int n) {
        if (n < 0 || n >= size) {
            throw new IndexOutOfBoundsException("size=" + size + ", index=" + n);
        }
        int index = firstIndex;
        for (int i = 0; i < n; i++) {
            index = nextPos[index];
        }
        return index;
    }

    public int forward(int index, int n) {
        for (int i = 0; i < n && index >= 0; i++) {
            index = nextPos[index];
        }
        return index;
    }

    public int back(int index, int n) {
        for (int i = 0; i < n && index >= 0; i++) {
            index = prevPos[index];
        }
        return index;
    }

    public int next(int index) {
        return nextPos[index];
    }

    public int prev(int index) {
        return prevPos[index];
    }

    public boolean isFirst(int index) {
        return index == firstIndex;
    }

    public boolean isLast(int index) {
        return nextPos[index] < 0;
    }

    public ArrayList<T> compact() {
        int oldSize = items.size();
        if (size < oldSize) {
            int index = firstIndex;
            for (int i = 0; i < size; i++) {
                if (index > i) {
                    items.set(i, items.get(index));
                }
                index = nextPos[index];
            }
            for (int i = oldSize - 1; i >= size; i--) {
                items.remove(i);
            }
        }
        return items;
    }
}

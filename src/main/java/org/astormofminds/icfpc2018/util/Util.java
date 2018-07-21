package org.astormofminds.icfpc2018.util;

public class Util {

    public static boolean odd(int n) {
        return (n & 1) == 1;
    }

    public static boolean even(int n) {
        return (n & 1) == 0;
    }
}

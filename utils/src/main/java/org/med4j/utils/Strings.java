package org.med4j.utils;

public class Strings {
    private Strings() {}

    public static boolean isEmpty(String s) {
        return s == null || s.length() == 0;
    }

    public static String zeros(int n) {
        return repeat('0', n);
    }

    private static String repeat(char value, int n) {
        return new String(new char[n]).replace("\0", String.valueOf(value));
    }
}

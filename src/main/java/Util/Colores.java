package Util;

public class Colores {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    public static void redWrite(String s, int trans) { //monitor
        System.out.printf(ANSI_RED + "%s %s t:%d\n" + ANSI_RESET, s, Thread.currentThread().getName(), trans);
    }

    public static void blueWrite(String s, int trans) { //rdp
        System.out.printf(ANSI_BLUE + "%s %s t:%d\n" + ANSI_RESET, s, Thread.currentThread().getName(), trans);
    }

    public static void yellowWrite(String s, int trans) {
        System.out.printf(ANSI_YELLOW + "%s %s t:%d\n" + ANSI_RESET, s, Thread.currentThread().getName(), trans);
    }

    public static void greenWrite(String s, int trans) {
        System.out.printf(ANSI_GREEN + "%s %s t:%d\n" + ANSI_RESET, s, Thread.currentThread().getName(), trans);
    }

    public static void cianWrite(String s, int trans) { //rdp
        System.out.printf(ANSI_CYAN + "%s %s t:%d\n" + ANSI_RESET, s, Thread.currentThread().getName(), trans);
    }
}

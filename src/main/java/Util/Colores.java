package Util;

import RedDePetri.Transicion;

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

    public static void purpleWrite(String s, Transicion transicion) {
        System.out.printf(ANSI_PURPLE + "%s %s t:%d\n" + ANSI_PURPLE, s, Thread.currentThread().getName(), transicion.getPosicion());
    }
    public static void redWrite(String s, Transicion transicion) { //monitor
        System.out.printf(ANSI_RED + "%s %s t:%d\n" + ANSI_RESET, s, Thread.currentThread().getName(), transicion.getPosicion());
    }

    public static void redWrite(String s, Transicion transicion, long time) { //monitor
        System.out.printf(ANSI_RED + "%s %s t:%d time:%d\n" + ANSI_RESET, s, Thread.currentThread().getName(), transicion.getPosicion(), System.currentTimeMillis() - time);
    }

    public static void blueWrite(String s, Transicion transicion) {
        System.out.printf(ANSI_BLUE + "%s %s t:%d\n" + ANSI_RESET, s, Thread.currentThread().getName(), transicion.getPosicion());
    }

    public static void blueWrite(String s, Transicion transicion, long time) {
        System.out.printf(ANSI_BLUE + "%s %s t:%d time:%d\n" + ANSI_RESET, s, Thread.currentThread().getName(), transicion.getPosicion(), System.currentTimeMillis() - time);
    }

    public static void yellowWrite(String s, Transicion transicion) {
        System.out.printf(ANSI_YELLOW + "%s %s t:%d\n" + ANSI_RESET, s, Thread.currentThread().getName(), transicion.getPosicion());
    }

    public static void yellowWrite(String s, Transicion transicion, long time) {
        System.out.printf(ANSI_YELLOW + "%s %s t:%d time:%d\n" + ANSI_RESET, s, Thread.currentThread().getName(), transicion.getPosicion(), System.currentTimeMillis() - time);
    }

    public static void greenWrite(String s, Transicion transicion) {
        System.out.printf(ANSI_GREEN + "%s %s t:%d\n" + ANSI_RESET, s, Thread.currentThread().getName(), transicion.getPosicion());
    }

    public static void greenWrite(String s, Transicion transicion, long time) {
        System.out.printf(ANSI_GREEN + "%s %s t:%d time:%d\n" + ANSI_RESET, s, Thread.currentThread().getName(), transicion.getPosicion(), System.currentTimeMillis() - time);

    }

    public static void cianWrite(String s, Transicion transicion) {
        System.out.printf(ANSI_CYAN + "%s %s t:%d\n" + ANSI_RESET, s, Thread.currentThread().getName(), transicion.getPosicion());
    }

    public static void cianWrite(String s, Transicion transicion, long time) {
        System.out.printf(ANSI_CYAN + "%s %s t:%d time:%d\n" + ANSI_RESET, s, Thread.currentThread().getName(), transicion.getPosicion(), System.currentTimeMillis() - time);
    }
}

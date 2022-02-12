package Util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;


public class Log {
    FileWriter fw;
    BufferedWriter bw;
    static PrintWriter pw;

    public Log(String filepath) {
        try {
            fw = new FileWriter(filepath, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        bw = new BufferedWriter(fw);
        pw = new PrintWriter(bw);
    }

    public synchronized static void write(char str) {
        pw.print(str);
        pw.flush();
    }

    public static void close() {
        pw.close();
    }
}


package Util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;


public class Log {
    private FileWriter fw;
    private BufferedWriter bw;
    private PrintWriter pw;

    public Log(String filepath) {
        try {
            this.fw = new FileWriter(filepath, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.bw = new BufferedWriter(fw);
        this.pw = new PrintWriter(bw);
    }

    public synchronized void write(String str) {
        pw.print(str);
        pw.flush();
    }

    public void close() {
        pw.close();
    }
}


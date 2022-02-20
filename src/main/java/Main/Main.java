package Main;

import Monitor.*;
import RedDePetri.RedDePetri;
import Util.Log;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {

        //todo implementar el numero de hilos
        Operaciones.setCantidadHilos(8);
        String mji = "src/main/resources/inicial.csv";
        String I = "src/main/resources/incidencia.csv";
        String H = "src/main/resources/inhibidor.csv";
        String T = "src/main/resources/tInvariantes.csv";
        String tiempos = "src/main/resources/tiempos.csv";
        String filepathLog = "src/main/resources/log";

        Log log = new Log(filepathLog);


        RedDePetri redDePetri = new RedDePetri(mji, I, H, tiempos, T);
        Monitor monitor = new Monitor(redDePetri, log);


        Boolean[] arr0 = {true, false, false, false, false, false, false, false, false, false};//T0
        Boolean[] arr1 = {false, true, false, true, false, false, false, false, false, false};//T1-T3
        Boolean[] arr2 = {false, false, true, false, true, false, false, false, false, false};//T2-T4
        Boolean[] arr3 = {false, false, false, false, false, true, false, false, false, false};//T5
        Boolean[] arr4 = {false, false, false, false, false, false, true, true, true, true};//T6-T7-T8-T9

        Thread[] hilo = new Thread[8];


        Runnable runnable1 = new Hilo(redDePetri, monitor, arr0);
        hilo[0] = new Thread(runnable1, "hilo_0");

        Runnable runnable2 = new Hilo(redDePetri, monitor, arr1);
        hilo[1] = new Thread(runnable2, "hilo_1");

        Runnable runnable3 = new Hilo(redDePetri, monitor, arr2);
        hilo[2] = new Thread(runnable3, "hilo_2");

        Runnable runnable4 = new Hilo(redDePetri, monitor, arr3);
        hilo[3] = new Thread(runnable4, "hilo_3");

        Runnable runnable5 = new Hilo(redDePetri, monitor, arr4);
        hilo[4] = new Thread(runnable5, "hilo_4");

        Runnable runnable6 = new Hilo(redDePetri, monitor, arr4);
        hilo[5] = new Thread(runnable6, "hilo_5");

        Runnable runnable7 = new Hilo(redDePetri, monitor, arr4);
        hilo[6] = new Thread(runnable7, "hilo_6");

        Runnable runnable8 = new Hilo(redDePetri, monitor, arr0);
        hilo[7] = new Thread(runnable8, "hilo_0.1");

        for (int i = 0; i < hilo.length; i++) {
            hilo[i].start();
        }
        try {
            for (Thread thread : hilo) thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        log.close();
    }
}
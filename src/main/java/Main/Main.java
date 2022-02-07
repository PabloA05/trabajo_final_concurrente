package Main;

import Monitor.*;
import RedDePetri.RedDePetri;

public class Main {
    public static void main(String[] args) {

        //todo implementar el numero de hilos
        Operaciones.setCantidadHilos(1);
        String mji = "src/main/resources/inicial.csv";
        String I = "src/main/resources/incidencia.csv";
        String H = "src/main/resources/inhibidor.csv";
        RedDePetri redDePetri = new RedDePetri(mji, I, H);
        Monitor monitor = new Monitor(redDePetri);


        Boolean[] arr0 = {true, true, false, true, false, true, false, false, false, false};
        Boolean[] arr1 = {true, false, true, false, true, true, false, false, false, false};
        Boolean[] arr2 = {true, true, false, true, false, true, false, false, false, false};
        Boolean[] arr3 = {true, false, true, false, true, true, false, false, false, false};
        Boolean[] arr4 = {false, false, false, false, false, false, true, true, true, true};
        Boolean[] arr5 = {false, false, false, false, false, false, true, true, true, true};
        Boolean[] arr6 = {false, false, false, false, false, false, true, true, true, true};


        Thread[] hilo = new Thread[7];


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

        Runnable runnable6 = new Hilo(redDePetri, monitor, arr5);
        hilo[5] = new Thread(runnable6, "hilo_5");

        Runnable runnable7 = new Hilo(redDePetri, monitor, arr6);
        hilo[6] = new Thread(runnable7, "hilo_6");

        for (int i = 0; i < hilo.length; i++) {
            hilo[i].start();
        }
        try {
            for (Thread thread : hilo) thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
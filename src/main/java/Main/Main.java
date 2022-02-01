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





        Boolean[] arr1 = {true, false, false, false, false, false, false, false, false, false};
        Boolean[] arr2 = {false, true, false, false, false, false, false, false, false, false};
        Boolean[] arr3 = {false, false, true, false, false, false, false, false, false, false};
        Boolean[] arr4 = {false, false, false, true, false, false, false, false, false, false};
        Boolean[] arr5 = {false, false, false, false, true, false, false, false, false, false};
        Boolean[] arr6 = {false, false, false, false, false, true, false, false, false, false};
        Boolean[] arr7 = {false, false, false, false, false, false, true, false, false, false};
        Boolean[] arr8 = {false, false, false, false, false, false, false, true, false, false};
        Boolean[] arr9 = {false, false, false, false, false, false, false, false, true, false};
        Boolean[] arr10 = {false, false, false, false, false, false, false, false, false, true};


        Thread[] hilo = new Thread[10];


        Runnable runnable1 = new Hilo(redDePetri, monitor, arr1);
        hilo[0] = new Thread(runnable1,"hilo_0");
        hilo[0].start();
        Runnable runnable2 = new Hilo(redDePetri, monitor, arr2);
        hilo[1] = new Thread(runnable2,"hilo_1");
        hilo[1].start();
        Runnable runnable3 = new Hilo(redDePetri, monitor, arr3);
        hilo[2] = new Thread(runnable3,"hilo_3");
        hilo[2].start();
        Runnable runnable4 = new Hilo(redDePetri, monitor, arr4);
        hilo[3] = new Thread(runnable4,"hilo_4");
        hilo[3].start();
        Runnable runnable5 = new Hilo(redDePetri, monitor, arr5);
        hilo[4] = new Thread(runnable5,"hilo_5");
        hilo[4].start();
        Runnable runnable6 = new Hilo(redDePetri, monitor, arr6);
        hilo[5] = new Thread(runnable6,"hilo_6");
        hilo[5].start();
        Runnable runnable7 = new Hilo(redDePetri, monitor, arr7);
        hilo[6] = new Thread(runnable7,"hilo_7");
        hilo[6].start();
        Runnable runnable8 = new Hilo(redDePetri, monitor, arr8);
        hilo[7] = new Thread(runnable8,"hilo_8");
        hilo[7].start();
        Runnable runnable9 = new Hilo(redDePetri, monitor, arr9);
        hilo[8] = new Thread(runnable9,"hilo_9");
        hilo[8].start();
        Runnable runnable10 = new Hilo(redDePetri, monitor, arr10);
        hilo[9] = new Thread(runnable10,"hilo_10");
        hilo[9].start();


    }
}
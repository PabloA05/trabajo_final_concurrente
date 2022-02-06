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





        Boolean[] arr1 = {true, false, false, false, false, false, false, false, false, false};//T1
        Boolean[] arr2 = {false, true, false, true, false, false, false, false, false, false};//T2-T4
        Boolean[] arr3 = {false, false, true, false, true, false, false, false, false, false};//T3-T5
        Boolean[] arr4 = {false, false, false, false, false, true, false, false, false, false};//T6
        Boolean[] arr5 = {false, false, false, false, false, false, true, true, true, true};//T7-T8-T9-T10


        Thread[] hilo = new Thread[6];


        Runnable runnable1 = new Hilo(redDePetri, monitor, arr1);
        hilo[0] = new Thread(runnable1,"hilo_1");
        hilo[0].start();
        Runnable runnable2 = new Hilo(redDePetri, monitor, arr2);
        hilo[1] = new Thread(runnable2,"hilo_2");
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
        Runnable runnable6 = new Hilo(redDePetri, monitor, arr5);
        hilo[5] = new Thread(runnable5,"hilo_6");
        hilo[5].start();

    }
}
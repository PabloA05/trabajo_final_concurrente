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

        Boolean[] arr2 = {true, true, false, true, true, true, true, true, true, true};

        Boolean[] arr1 = {false, false, true, false, false, false, false, false, false, false};


        Thread[] hilo = new Thread[2];
        Runnable runnable = new Hilo(redDePetri, monitor, arr1);
        hilo[0] = new Thread(runnable);
        hilo[0].start();
        Runnable runnable1 = new Hilo(redDePetri, monitor, arr2);
        hilo[1] = new Thread(runnable1);
        hilo[1].start();

    }
}
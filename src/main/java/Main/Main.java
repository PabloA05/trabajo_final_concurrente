package Main;

import Monitor.*;
import RedDePetri.RedDePetri;

public class Main {
    public static void main(String[] args) {

        //todo implementar el numero de hilos
        Operaciones.setCantidadHilos(10);
        String mji = "src/main/resources/inicial.csv";
        String I = "src/main/resources/incidencia.csv";
        RedDePetri redDePetri = new RedDePetri(mji, I);
        //
        Monitor monitor = new Monitor(redDePetri);
    }
}
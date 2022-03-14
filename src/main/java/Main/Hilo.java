package Main;

import Monitor.Monitor;
import RedDePetri.RedDePetri;
import RedDePetri.Transicion;
import Util.Colores;

public class Hilo implements Runnable {

    RedDePetri rdp;
    Monitor monitor;
    Transicion[] transiciones;
    Boolean[] secuencia = new Boolean[10];

    public Hilo(RedDePetri rdp, Monitor monitor, Boolean[] secuencia) {
        this.rdp = rdp;
        this.monitor = monitor;
        transiciones = rdp.getTransiciones().clone();
        this.secuencia = secuencia;
    }

    @Override
    public void run() {

        while (monitor.getCondicion()) {
            for (int i = 0; i < transiciones.length; i++) {
                if (!monitor.getCondicion()) {
                    break;
                }
                if (secuencia[i]) {
                    monitor.disparaTransicion(transiciones[i]);
                }
            }

        }
        System.out.printf(Colores.ANSI_RED + "SALIO :%s\n" + Colores.ANSI_RESET, Thread.currentThread().getName());
    }
}
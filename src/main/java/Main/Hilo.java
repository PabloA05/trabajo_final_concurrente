package Main;

import Monitor.Monitor;
import RedDePetri.RedDePetri;
import RedDePetri.Transicion;

public class Hilo implements Runnable {

    RedDePetri rdp;
    Monitor monitor;
    final Transicion[] transiciones;
    Boolean[] secuencia = new Boolean[10];

    public Hilo(RedDePetri rdp, Monitor monitor, Boolean[] secuencia) {
        this.rdp = rdp;
        this.monitor = monitor;
        transiciones = rdp.getTransiciones().clone();
        this.secuencia = secuencia;
    }

    @Override
    public void run() {
        int k = 10000;
        while (k > 0) {
            for (int i = 0; i < transiciones.length; i++) {
                if (secuencia[i]) {
                    System.out.printf("transiocon %d %s\n", i,Thread.currentThread().getName());
                    monitor.disparaTransicion(transiciones[i]);
                }
            }
            k--;
        }
        System.out.println("Salio: " + Thread.currentThread().getId());
    }
}
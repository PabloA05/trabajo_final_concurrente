package Main;

import Monitor.Monitor;
import RedDePetri.RedDePetri;
import RedDePetri.Transicion;

public class Hilo implements Runnable {

    private RedDePetri rdp;
    private Monitor monitor;
    private Transicion[] transiciones;
    private Boolean[] secuencia = new Boolean[10];

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
                if(!monitor.getCondicion()){break;}
                if (secuencia[i]) {
                    monitor.disparaTransicion(transiciones[i]);
                }
            }

        }
    }
}
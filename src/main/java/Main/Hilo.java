package Main;

import Monitor.Monitor;
import RedDePetri.RedDePetri;
import RedDePetri.Transicion;

public class Hilo implements Runnable{

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
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int k=1000;
        while(true){
            for(int i=0;i<transiciones.length;i++){
                if(secuencia[i]){
                    monitor.disparaTransicion(transiciones[i]);
                }
            }
            k--;
        }
       // System.out.println("Salio: "+Thread.currentThread().getName());
    }
}
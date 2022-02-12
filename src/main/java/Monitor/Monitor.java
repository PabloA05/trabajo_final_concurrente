package Monitor;

import RedDePetri.RedDePetri;
import RedDePetri.Transicion;
import Util.Log;

import java.sql.SQLOutput;
import java.util.concurrent.Semaphore;

public class Monitor {

    private static Semaphore semaforoMonitor;
    //private boolean k;
    private RedDePetri redDePetri;
    private Colas[] cola;
    private Politica politica = new Politica(true);
    private static int disparos = 0;


    public Monitor(RedDePetri rdp) {
        semaforoMonitor = new Semaphore(1, true);
        //k = false;
        redDePetri = rdp;
        cola = new Colas[redDePetri.getCantTransisiones()];
        for (int i = 0; i < redDePetri.getCantTransisiones(); i++) {
            cola[i] = new Colas(); //Inicialización de colas.
        }
    }

    private Boolean[] quienesEstan() {
        Boolean[] Vc = new Boolean[cola.length];
        for (int i = 0; i < cola.length; i++) {
            Vc[i] = !cola[i].isEmpty();
        }
        return Vc;
    }

    public void disparaTransicion(Transicion transicion) {
        // k = true;
        while (true) {//todo hace falta la k????


            acquireMonitor();

            boolean k = true;
            //System.out.print("Hilo: "+Thread.currentThread().getId()+" entro al monitor con transicion "+transicion.getPosicion()+"\n");
            k = this.redDePetri.disparar(transicion);


            if (k) {
                System.out.printf("disparo ->%d %s\n", disparos++, Thread.currentThread().getName());
                Boolean[] Vs = this.redDePetri.getSensibilizadasExtendido();
                //Operaciones.printVectorEx(Vs);
                Boolean[] Vc = quienesEstan();
                //Operaciones.printVectorColas(Vc);
                Boolean[] m = new Boolean[Vs.length];
                m = Operaciones.andVector(Vs, Vc); //todo ver si se puede simplificar
                // cantidadDisparada(redDePetri);
                if (Operaciones.comprobarUnos(m)) {
                    try {
                        if (semaforoMonitor.availablePermits() != 0) {
                            System.out.printf("valor del semaforo %d\n", semaforoMonitor.availablePermits());
                            System.exit(1);

                        }
                        Transicion transicionADisparar = politica.cualDisparo(m, redDePetri);

                        cola[transicionADisparar.getPosicion()].release();
                    } catch (IndexOutOfBoundsException e) {
                        e.printStackTrace();
                    }
                    break;
                } else {
                    k = false;
                    break;
                }
            } else {
                releaseMonitor();
                cola[transicion.getPosicion()].acquire();
            }

        }
        //cantidadDisparada(redDePetri);

        releaseMonitor();
        Log.write(transicion.getId());
    }

    public static synchronized void acquireMonitor() {
        try {
            semaforoMonitor.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static synchronized void releaseMonitor() {
        semaforoMonitor.release();
    }

    public void cantidadDisparada(RedDePetri redDePetri) {
        Transicion[] transiciones;
        transiciones = redDePetri.getTransiciones().clone();
        for (int i = 0; i < redDePetri.getCantTransisiones(); i++) {
            System.out.println("La transicion: " + (transiciones[i].getPosicion() + 1) + " se disparo: " + transiciones[i].getCantidadDisparada());
        }
    }

}


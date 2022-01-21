package Monitor;

import RedDePetri.RedDePetri;
import RedDePetri.Transicion;

import java.util.concurrent.Semaphore;

public class Monitor {

    private static Semaphore semaforoMonitor;
    private boolean k;
    private RedDePetri redDePetri;
    private Colas[] cola;
    private Politica politica = new Politica(false);

    public Monitor(RedDePetri rdp) {
        semaforoMonitor = new Semaphore(1, true);
        k = false;
        redDePetri = rdp;
        cola = new Colas[redDePetri.getCantTransisiones()];
        for (int i = 0; i < redDePetri.getCantTransisiones(); i++) {
            cola[i] = new Colas(); //Inicialización de colas.
        }
    }

    private boolean[] quienesEstan() {
        boolean[] Vc = new boolean[cola.length];
        for (int i = 0; i < cola.length; i++) {
            Vc[i] = !cola[i].isEmpty();
        }
        return Vc;
    }

    public void disparaTransicion(Transicion transicion) {
        k = true;
        while (k) {//todo hace falta la k????
            acquireMonitor();
            //System.out.print("Hilo: "+Thread.currentThread().getId()+" entro al monitor con transicion "+transicion.getPosicion()+"\n");
            k = this.redDePetri.disparar(transicion);
            System.out.println("valor de k:"+k);
            if (k) {
                boolean[] Vs = this.redDePetri.getSensibilizadas();
                boolean[] Vc = quienesEstan();
                boolean[] m = new boolean[Vs.length];
                m = Operaciones.andVector(Vs, Vc); //todo ver si se puede simplificar

                if (Operaciones.comprobarUnos(m)) {
                    try{
                        Transicion transicionADisparar = politica.cualDisparo(m,redDePetri);
                        cola[transicionADisparar.getPosicion()].release();
                    }
                    catch(IndexOutOfBoundsException e){
                        e.printStackTrace();
                    }
                    //releaseMonitor();
                } else {
                    k = false;
                    //releaseMonitor();
                }
                break;

            } else {
                releaseMonitor();
                cola[transicion.getPosicion()].acquire();
            }
        }
        releaseMonitor();
    }

    public static void acquireMonitor() {
        try {
            semaforoMonitor.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void releaseMonitor() {
        semaforoMonitor.release();
    }
}


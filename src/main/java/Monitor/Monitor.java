package Monitor;

import RedDePetri.RedDePetri;
import RedDePetri.Transicion;

import java.sql.SQLOutput;
import java.util.concurrent.Semaphore;

public class Monitor {

    private static Semaphore semaforoMonitor;
    private boolean k;
    private RedDePetri redDePetri;
    private Colas[] cola;
    private Politica politica = new Politica(true);


    public Monitor(RedDePetri rdp) {
        semaforoMonitor = new Semaphore(1, true);
        k = false;
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
        k = true;
        while (k) {//todo hace falta la k????

            try{
                semaforoMonitor.acquire(); //Adquiero acceso al monitor.
            }
            catch(InterruptedException e){
                e.printStackTrace();
                return;
            }
            //System.out.print("Hilo: "+Thread.currentThread().getId()+" entro al monitor con transicion "+transicion.getPosicion()+"\n");
            k = this.redDePetri.disparar(transicion);
            System.out.println("valor de k:"+k);
            if (k) {
                Boolean[] Vs = this.redDePetri.getSensibilizadasExtendido();
                Operaciones.printVectorEx(Vs);
                Boolean[] Vc = quienesEstan();
                Operaciones.printVectorColas(Vc);
                Boolean[] m = new Boolean[Vs.length];
                m = Operaciones.andVector(Vs, Vc); //todo ver si se puede simplificar
                cantidadDisparada(redDePetri);
                if (Operaciones.comprobarUnos(m)) {
                    try{
                        Transicion transicionADisparar = politica.cualDisparo(m,redDePetri);
                        cola[transicionADisparar.getPosicion()].release();
                    }
                    catch(IndexOutOfBoundsException e){
                        e.printStackTrace();
                    }
                    semaforoMonitor.release();
                    return;

                }
                else {
                    k = false;
                    semaforoMonitor.release();
                    return;

                }


            } else {
                semaforoMonitor.release();
                cola[transicion.getPosicion()].acquire();
            }
        }
        semaforoMonitor.release();
    }

    public static synchronized void acquireMonitor() {
        System.out.println(Thread.currentThread().getId());
        try {
            semaforoMonitor.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static synchronized void releaseMonitor() {
        semaforoMonitor.release();
    }

    public void cantidadDisparada (RedDePetri redDePetri){
        Transicion[] transiciones;
        transiciones = redDePetri.getTransiciones();
        for(int i=0;i<redDePetri.getCantTransisiones();i++){
            System.out.println("La transicion: "+(transiciones[i].getPosicion()+1)+" se disparo: "+transiciones[i].getCantidadDisparada());
        }
    }

}


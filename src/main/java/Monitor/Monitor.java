package Monitor;

import RedDePetri.RedDePetri;
import RedDePetri.Transicion;
import Util.Log;

import java.sql.SQLOutput;
import java.util.concurrent.Semaphore;

public class Monitor {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

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

            try {
                semaforoMonitor.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            boolean k = true;
            //System.out.print("Hilo: "+Thread.currentThread().getId()+" entro al monitor con transicion "+transicion.getPosicion()+"\n");
            k = this.redDePetri.disparar(transicion);


            if (k) {
                System.out.printf(ANSI_BLUE + "Disparo transicion: %d %s\n" + ANSI_RESET, transicion.getPosicion(), Thread.currentThread().getName());
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
                semaforoMonitor.release();
                System.out.printf(ANSI_RED + "entro en cola t:%d %s\n" + ANSI_RESET, transicion.getPosicion(), Thread.currentThread().getName());
                cola[transicion.getPosicion()].acquire();
                System.out.printf(ANSI_GREEN + "salio de cola t:%d %s\n" + ANSI_RESET, transicion.getPosicion(), Thread.currentThread().getName());
            }

        }
        //cantidadDisparada(redDePetri);

        Log.write(transicion.getId());
        semaforoMonitor.release();
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

    public void cantidadDisparada(RedDePetri redDePetri) {
        Transicion[] transiciones;
        transiciones = redDePetri.getTransiciones().clone();
        for (int i = 0; i < redDePetri.getCantTransisiones(); i++) {
            System.out.println("La transicion: " + (transiciones[i].getPosicion() + 1) + " se disparo: " + transiciones[i].getCantidadDisparada());
        }
    }

}


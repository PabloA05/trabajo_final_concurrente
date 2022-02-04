package Monitor;

import RedDePetri.RedDePetri;
import RedDePetri.Transicion;

import java.util.Objects;
import java.util.concurrent.Semaphore;

public class Monitor {

    private static Semaphore semaforoMonitor;
    private boolean k;
    private RedDePetri redDePetri;
    private Colas[] cola;
    private Politica politica = new Politica(true);
    Object token;

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";


    public Monitor(RedDePetri rdp) {
        semaforoMonitor = new Semaphore(1, true);
        k = false;
        redDePetri = rdp;
        cola = new Colas[redDePetri.getCantTransisiones()];
        for (int i = 0; i < redDePetri.getCantTransisiones(); i++) {
            cola[i] = new Colas(); //Inicialización de colas.
        }
//        Transicion temp[] = rdp.getTransiciones();
//        for (int i = 0; i < temp.length; i++) {
//            cola[i].transicion = temp[i];
//        }


    }

    class flag_colas {
        public boolean flag;
        public int veces;
    }

    ;


    private Boolean[] quienesEstan() {
        Boolean[] Vc = new Boolean[cola.length];
        for (int i = 0; i < cola.length; i++) {
            Vc[i] = !cola[i].isEmpty();
        }
        return Vc;
    }

    public void disparaTransicion(Transicion transicion) {
        k = true;
        flag_colas log = new flag_colas();
        while (k) {//todo hace falta la k????
            acquireMonitor();

            if (log.flag) {
                System.out.printf(ANSI_RED + "!!!! entro de vuelta al monitor cantidad:%d transi:%d %s valor de k:%b\n" +
                                ANSI_RESET
                        , log.veces, transicion.getPosicion(), Thread.currentThread().getName(), k);
            } else {
                System.out.print(ANSI_RED + ">>>>>>>>>>>>>>>>>>>>> "
                        + Thread.currentThread().getName() + " entro al monitor con transicion " + transicion.getPosicion() + " valor de k:" + k + ANSI_RESET + "\n");

            }

            k = this.redDePetri.disparar(transicion);
            if (k) {
                System.out.print(ANSI_RED + "Monitor disparo " + Thread.currentThread().getName() +
                        " entro al monitor con transicion " + transicion.getPosicion() + ANSI_RESET);
                if (log.flag) {
                    System.out.printf(ANSI_RED + "^^^^^^^^^^^^ habia entrado %d veces" + ANSI_RESET, log.veces);
                }
                System.out.println();
            }
            // System.out.println("valor de k:"+k);
            if (k) {
                Boolean[] Vs = this.redDePetri.getSensibilizadasExtendido().clone();

                Boolean[] Vc = quienesEstan();
                Boolean[] m = new Boolean[Vs.length];
                m = Operaciones.andVector(Vs, Vc); //todo ver si se puede simplificar
                //cantidadDisparada(redDePetri);

                if (Operaciones.comprobarUnos(m)) {
                    try {

                        Transicion ttemp = politica.cualDisparo(m, redDePetri);
                        System.out.printf(ANSI_GREEN + "transision %d metida por %s [politica]\n" + ANSI_RESET, ttemp.getPosicion(), Thread.currentThread().getName());

                        cola[ttemp.getPosicion()].release();
                    } catch (IndexOutOfBoundsException e) {
                        e.printStackTrace();
                    }
                    releaseMonitor();
                    return;
                } else {
                    k = false;
                }

                break;

            } else {
                releaseMonitor();
                System.out.printf(ANSI_PURPLE + "\n>>>>>>>>>>> posicion a meter colas %d - %s\n" + ANSI_RESET, transicion.getPosicion(), Thread.currentThread().getName());
                cola[transicion.getPosicion()].acquire();
                System.out.printf(ANSI_PURPLE + "<<<<<<<<<<<<<<< sale transion  colas %d - %s\n" + ANSI_RESET, transicion.getPosicion(), Thread.currentThread().getName());
                log.flag = true;
                log.veces++;
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

    public void cantidadDisparada(RedDePetri redDePetri) {
        Transicion[] transiciones;
        transiciones = redDePetri.getTransiciones().clone();
        for (int i = 0; i < redDePetri.getCantTransisiones(); i++) {
            System.out.println("La transicion: " + (transiciones[i].getPosicion() + 1) + " se disparo: " + transiciones[i].getCantidadDisparada());
        }
    }

}


package Monitor;

import RedDePetri.RedDePetri;
import RedDePetri.Transicion;
import Util.Log;

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
    private Politica politica = new Politica(2);
    private static int disparos = 0;
    private Log log;

    public Monitor(RedDePetri rdp, Log log) {
        this.log = log;
        semaforoMonitor = new Semaphore(1, true);
        //k = false;
        redDePetri = rdp;
        cola = new Colas[redDePetri.getCantTransiciones()];
        for (int i = 0; i < redDePetri.getCantTransiciones(); i++) {
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
        // acquireMonitor();

        while (true) {//todo hace falta la k????
            acquireMonitor();
            boolean k = true;
            System.out.print(ANSI_YELLOW + "Hilo: " + Thread.currentThread().getId() + " entro al monitor con transicion " + transicion.getPosicion() + " " + Thread.currentThread().getName() + ANSI_RESET + "\n");
            k = this.redDePetri.disparar(transicion);


            if (k) {
                System.out.printf(ANSI_BLUE + "Disparo transicion: %d %s\n" + ANSI_RESET, transicion.getPosicion(), Thread.currentThread().getName());
                Boolean[] Vs = this.redDePetri.getSensibilizadasExtendido();
                System.out.println("-----vs ----");
                Operaciones.printB(Vs);
                System.out.println("---------");

                System.out.println("-----vc ----");
                Boolean[] Vc = quienesEstan();
                Operaciones.printB(Vc);
                System.out.println("---------");

                Boolean[] m = new Boolean[Vs.length];
                m = Operaciones.andVector(Vs, Vc); //todo ver si se puede simplificar
                //  cantidadDisparada(redDePetri);
                if (Operaciones.comprobarUnos(m)) {
                    try {
                        if (semaforoMonitor.availablePermits() != 0) {
                            System.out.printf("valor del semaforo %d\n", semaforoMonitor.availablePermits());
                            System.exit(1);

                        }
                        Transicion transicionADisparar = politica.cualDisparo(m, redDePetri);

                        System.out.printf("%s t:%d despertar:%d\n", Thread.currentThread().getName(), transicion.getPosicion(), transicionADisparar.getPosicion());
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
                System.out.printf(ANSI_RED + "entro en cola t:%d %s\n" + ANSI_RESET, transicion.getPosicion(), Thread.currentThread().getName());
                cola[transicion.getPosicion()].acquire();
                System.out.printf(ANSI_GREEN + "salio de cola t:%d %s\n" + ANSI_RESET, transicion.getPosicion(), Thread.currentThread().getName());
            }

        }
        cantidadDisparada(redDePetri);

        log.write(transicion.getId());
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
        System.out.println("Invariante 1: " + transiciones[3].getId() + " se disparo: " + transiciones[3].getCantidadDisparada());
        System.out.println("Invariante 2: " + transiciones[4].getId() + " se disparo: " + transiciones[4].getCantidadDisparada());
        System.out.println("Invariante 3: " + transiciones[9].getId() + " se disparo: " + transiciones[9].getCantidadDisparada());
    }

}


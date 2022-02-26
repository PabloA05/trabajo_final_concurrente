package Monitor;

import RedDePetri.RedDePetri;
import RedDePetri.Transicion;
import Util.Log;
import Util.Operaciones;
import Util.Colores;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class Monitor {

    public static ArrayList<int[]> datos;
    private static Semaphore semaforoMonitor;
    //private boolean k;
    private RedDePetri redDePetri;
    private Colas[] cola;
    private Politica politica = new Politica(2);
    private static int disparos = 0;
    private boolean condicion;
    private boolean flag;
    private int contador;
    private long cuenta = 0;
    private final int cantidadDeInvariantesADisparar;
    private int relacionDeMuestra;
    private Log log;
    public Monitor(RedDePetri rdp,Log log ,int cantidadDeInvariantesADisparar) {

            this.log = log;
            semaforoMonitor = new Semaphore(1, true);
            //k = false;
            redDePetri = rdp;
            cola = new Colas[redDePetri.getCantTransiciones()];
            for (int i = 0; i < redDePetri.getCantTransiciones(); i++) {
                cola[i] = new Colas(); //Inicialización de colas.
            }
            condicion = true;
            //flag = true;
            contador = 0;
            datos = new ArrayList<int[]>();
            cuenta = System.currentTimeMillis();
            this.cantidadDeInvariantesADisparar = cantidadDeInvariantesADisparar;
            relacionDeMuestra = cantidadDeInvariantesADisparar / 10;
        }

        private Boolean[] quienesEstan () {
            Boolean[] Vc = new Boolean[cola.length];
            for (int i = 0; i < cola.length; i++) {
                Vc[i] = !cola[i].isEmpty();
            }
            return Vc;
        }

        public void disparaTransicion (Transicion transicion){
            // k = true;
            // acquireMonitor();

            while (true) {//todo hace falta la k????
                acquireMonitor();
                boolean k = true;
                System.out.print(Colores.ANSI_YELLOW + "Hilo: " + Thread.currentThread().getId() +
                        " entro al monitor con transicion " + transicion.getPosicion() + " " + Thread.currentThread().getName() + Colores.ANSI_RESET + "\n");
                k = this.redDePetri.disparar(transicion);
                if (!condicion) {
                    break;
                }

                if (k) {
                  System.out.printf(Colores.ANSI_BLUE + "Disparo transicion: %d %s\n" +Colores. ANSI_RESET, transicion.getPosicion(), Thread.currentThread().getName());
                    Boolean[] Vs = this.redDePetri.getSensibilizadasEx();
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
                    if (!condicion) {
                        break;
                    }
                    System.out.printf(Colores.ANSI_RED + "entro en cola t:%d %s\n" +Colores.ANSI_RESET, transicion.getPosicion(), Thread.currentThread().getName());
                    cola[transicion.getPosicion()].acquire();
                    System.out.printf(Colores.ANSI_GREEN + "salio de cola t:%d %s\n" + Colores.ANSI_RESET, transicion.getPosicion(), Thread.currentThread().getName());
                }

            }

            log.write(transicion.getId());
            contador++;
            if (contador >= relacionDeMuestra) {
                agregarDato(redDePetri.getTransiciones()[3].getCantidadDisparada(), redDePetri.getTransiciones()[4].getCantidadDisparada(), redDePetri.getTransiciones()[9].getCantidadDisparada());
                contador = 0;
            }

            int aux = 0;

            if (condicion) {
                setCondicion();
            }

            if (!condicion && flag) {
                flag = false;

                for (int i = 0; i < cola.length; i++) {
                    while (cola[i].get() > 0) {
                        cola[i].release();
                    }
                }

            }
            releaseMonitor();
        }

        public static void acquireMonitor () {
            try {
                semaforoMonitor.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public boolean getCondicion () {
            return condicion;
        }

        public void setCondicion () {
            Transicion[] transiciones;
            transiciones = redDePetri.getTransiciones().clone();
            int suma = 0;
            suma = transiciones[3].getCantidadDisparada() + transiciones[4].getCantidadDisparada() + transiciones[9].getCantidadDisparada();

            if (suma >= cantidadDeInvariantesADisparar) {
                condicion = false;
                flag = true;
                for (int i = 0; i < cola.length; i++) {
                    System.out.println("Cola[" + i + "]: " + cola[i].get());
                }
                System.out.println("Es condicion false");
            }

        }

        public static void releaseMonitor () {
            semaforoMonitor.release();
        }

        public void cantidadDisparada (RedDePetri redDePetri){
            Transicion[] transiciones;
            transiciones = redDePetri.getTransiciones().clone();
            System.out.println("Invariante 1: " + transiciones[3].getId() + " se disparo: " + transiciones[3].getCantidadDisparada());
            System.out.println("Invariante 2: " + transiciones[4].getId() + " se disparo: " + transiciones[4].getCantidadDisparada());
            System.out.println("Invariante 3: " + transiciones[9].getId() + " se disparo: " + transiciones[9].getCantidadDisparada());
        }

        public void agregarDato ( int a, int b, int c){
            System.out.println("Agrego dato");
            long actual = System.currentTimeMillis() - cuenta;

            System.out.println("Actual: " + actual);
            datos.add((new int[]{a, b, c, (int) actual}));
        }


    }


package Monitor;

import RedDePetri.RedDePetri;
import RedDePetri.Transicion;
import Util.Colores;
import Util.Log;
import Util.Operaciones;
import RedDePetri.State;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;


public class Monitor {

    public static ArrayList<int[]> datos;
    private static Semaphore semaforoMonitor;
    private RedDePetri redDePetri;
    private Colas[] cola;
    private Politica politica = new Politica(2);
    private boolean condicion;
    private boolean flag;
    private int contador;
    private long cuenta;
    private final int cantidadDeInvariantesADisparar;
    private int relacionDeMuestra;
    private Log log;


    public class Police {

        private boolean colas;
        private int colasTransicion;

        public Police() {
            colas = false;
            colasTransicion = -1;
        }

        public void patrolling(Transicion transicion) {
            if (colas) {
                if (transicion.getPosicion() != colasTransicion) {
                    if (colasTransicion == -1) {
                        Colores.redWrite("Colas -1", transicion);
                        System.exit(1);
                    }

                    Colores.redWrite("Fallo", transicion);
                    System.out.printf(Colores.ANSI_RED + "transicion que se deberia disparar t:%d\n" + Colores.ANSI_RESET, colasTransicion);
                    System.exit(1);
                }
                resetColas();
            }

        }

        public void setColas(Transicion transicion) {
            this.colas = true;
            colasTransicion = transicion.getPosicion();
        }

        public void resetColas() {
            this.colas = false;
            colasTransicion = -1;
        }
    }

    Police police;

    public Monitor(RedDePetri rdp, Log log, int cantidadDeInvariantesADisparar) {

        this.log = log;
        semaforoMonitor = new Semaphore(1, true);
        redDePetri = rdp;
        cola = new Colas[redDePetri.getCantTransiciones()];
        for (int i = 0; i < redDePetri.getCantTransiciones(); i++) {
            cola[i] = new Colas(); //Inicialización de colas.
        }
        condicion = true;
        contador = 0;
        datos = new ArrayList<int[]>();
        cuenta = System.currentTimeMillis();
        this.cantidadDeInvariantesADisparar = cantidadDeInvariantesADisparar;
        relacionDeMuestra = cantidadDeInvariantesADisparar / 10;
        //police = new Police();
    }

    private Boolean[] quienesEstan() {
        Boolean[] Vc = new Boolean[cola.length];
        for (int i = 0; i < cola.length; i++) {
            Vc[i] = !(cola[i].get() == 0);
        }
        return Vc;
    }


    public void disparaTransicion1(Transicion transicion) {
        //System.out.printf("valor del semaforo %d %s\n", semaforoMonitor.availablePermits(), Thread.currentThread().getName());
        acquireMon();
        // Colores.cianWrite("entro al monitor", transicion);

        while (true) {
            if (!condicion) {
                releaseMon();
                break;
            }
            boolean k = this.redDePetri.disparar(transicion, semaforoMonitor);
            if (k) {
                //Colores.redWrite("disparo", transicion);
                //police.patrolling(transicion);
                update_condition(transicion.getId());
                if (!condicion) {
                    releaseMon();
                    break;
                }
                Boolean[] Vs = this.redDePetri.getSensibilizadasEx();

                Boolean[] Vc = quienesEstan();
                // System.out.println("---------------------- Vector sensibilizado -----------------------");
                //Operaciones.printB(Vs);
                //    System.out.println("---------------------- Vector colas -----------------------");
                //Operaciones.printB(Vc);
                Boolean[] m = Operaciones.andVector(Vs, Vc); //todo ver si se puede simplificar
                //  System.out.println("---------------------- Vector m -----------------------");
                //  Operaciones.printB(m);
//                System.out.println("---------------------- disparos -----------------------");
//                cantidadDisparada(redDePetri);
                if (Operaciones.comprobarUnos(m)) {
                    if (semaforoMonitor.availablePermits() != 0) {
                        System.out.printf("valor del semaforo %d\n", semaforoMonitor.availablePermits());
                        System.exit(1);
                    }
                    Transicion transicionADisparar = politica.cualDisparo(m, redDePetri);
                    //police.setColas(transicionADisparar);
                    // Colores.yellowWrite("politica despertó de las colas", transicionADisparar);
                    cola[transicionADisparar.getPosicion()].release();

                } else {
                    //   Colores.redWrite("solto el monitor", transicion);
                    releaseMon();
                }
                break;
            } else {
                if (!condicion) {
                    releaseMon();
                    break;
                }
                //Colores.blueWrite("Entro en las colas", transicion);
                cola[transicion.getPosicion()].increment();
                semaforoMonitor.release();
                cola[transicion.getPosicion()].acquire();
                //Colores.blueWrite("Se fue de las colas", transicion);
            }
        }
        //   Colores.cianWrite("se fue del monitor", transicion);
    }

    public void disparaTransicion(Transicion transicion) {
        acquireMon();
        Colores.redWrite("entro al monitor", transicion);

        State state = null;
        while (state != State.FIRE) {
            state = redDePetri.disparar(transicion);
            switch (state) {
                case FIRE: {
                    Colores.greenWrite(">>>Disparo", transicion);
                    update_condition(transicion.getId());
                    if (!condicion) {
                        releaseMon();
                        break;
                    }
                    Boolean[] Vs = this.redDePetri.getSensibilizadasEx();
                    Boolean[] Vc = quienesEstan();
                    System.out.printf("-------------- Vector sensibilizado %s t:%d------------\n" , Thread.currentThread().getName() , transicion.getPosicion());
                    Operaciones.printB(Vs);
                        System.out.printf("---------------------- Vector colas %s t:%d------------\n" , Thread.currentThread().getName() , transicion.getPosicion());
                    Operaciones.printB(Vc);
                    Boolean[] m = Operaciones.andVector(Vs, Vc); //todo ver si se puede simplificar
                      System.out.printf("---------------------- Vector m %s t:%d------------\n" , Thread.currentThread().getName() , transicion.getPosicion());
                      Operaciones.printB(m);
//                System.out.println("---------------------- disparos -----------------------");
//                cantidadDisparada(redDePetri);
                    if (Operaciones.comprobarUnos(m)) {
                        if (semaforoMonitor.availablePermits() != 0) {
                            System.out.printf("valor del semaforo %d\n", semaforoMonitor.availablePermits());
                            System.exit(1);
                        }
                        Transicion transicionADisparar = politica.cualDisparo(m, redDePetri);
                        //police.setColas(transicionADisparar);
                        System.out.printf(Colores.ANSI_YELLOW + "politica - El %s desperto la t:%d\n" + Colores.ANSI_RESET,  Thread.currentThread().getName(), transicionADisparar.getPosicion());

                        cola[transicionADisparar.getPosicion()].release();

                    } else {
                        Colores.redWrite("solto el monitor", transicion);
                        releaseMon();
                    }
                    break;
                }
                case NO_FIRE: {
                    //todo chequar si los hilos que se fueron a las colas y que se sensibilizan pueden dormir
                    Colores.blueWrite("no disparo entro a colas", transicion);
                    cola[transicion.getPosicion()].increment();
                    releaseMon();
                    cola[transicion.getPosicion()].acquire();
                    Colores.blueWrite("salio de colas", transicion);
                    break;
                }
                case SLEEP: {
                    Colores.cianWrite("se fue a dormir", transicion);

                    releaseMon();
                    //todo ver si les gusta asi. Capaz que pedir de esa forma el time stamp y el alpha no esta bien
                    sleep_thread(transicion);
                    acquireMon();
                    Colores.cianWrite("salio de dormir", transicion);
                    break;
                }
                case AFTER: {
                    Colores.redWrite("Disparo despues beta",transicion);
                    System.exit(1);
                }
                default: {
                    Colores.redWrite("Error disparo", transicion);
                    System.exit(1);
                }

            }
        }
    }

    private void update_condition(String id) {
        if (condicion) {
            log.write(id);
        }
        contador++;
        if (contador >= relacionDeMuestra) {
            agregarDato(redDePetri.getTransiciones()[3].getCantidadDisparada(), redDePetri.getTransiciones()[4].getCantidadDisparada(), redDePetri.getTransiciones()[9].getCantidadDisparada());
            contador = 0;
        }
        if (condicion) {
            setCondicion();
        }
        if (!condicion && flag) {

            for (int i = 0; i < cola.length; i++) {
                while (cola[i].get() > 0) {
                    cola[i].release();
                }
            }
        }
    }

    private void sleep_thread(Transicion transicion) {
        long sleepTime = redDePetri.timeStamp(transicion) + redDePetri.alpha(transicion) - System.currentTimeMillis();
        if (sleepTime < 0) {
            return;
        }
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void acquireMon() {
        ///   System.out.println("acquireMon " + Thread.currentThread().getName());
        try {
            semaforoMonitor.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void releaseMon() {

        semaforoMonitor.release();
        //     System.out.printf("releaseMon despues permisos %d %s\n", semaforoMonitor.availablePermits(), Thread.currentThread().getName());
    }

    public static void acquireMonitor() {
        try {
            semaforoMonitor.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public boolean getCondicion() {
        return condicion;
    }

    private void setCondicion() {
        Transicion[] transiciones;
        transiciones = redDePetri.getTransiciones().clone();
        int suma = 0;
        suma = transiciones[3].getCantidadDisparada() + transiciones[4].getCantidadDisparada() + transiciones[9].getCantidadDisparada();

        if (suma >= cantidadDeInvariantesADisparar) {
            condicion = false;
            flag = true;
        }
    }

    public static void releaseMonitor() {
        semaforoMonitor.release();
        System.out.println("releaseMonitor desde rdp permisos " + semaforoMonitor.availablePermits() + " " + Thread.currentThread().getName());
    }

    public void cantidadDisparada(RedDePetri redDePetri) {
        Transicion[] transiciones;
        transiciones = redDePetri.getTransiciones().clone();
        System.out.println(transiciones[3].getCantidadDisparada() + " " + transiciones[4].getCantidadDisparada() + " " + transiciones[9].getCantidadDisparada());
    }

    private void agregarDato(int a, int b, int c) {
        long actual = System.currentTimeMillis() - cuenta;
        datos.add((new int[]{a, b, c, (int) actual}));
    }

    public void printInvariantes(String path) {
        Log invariantes = new Log(path);
        int[] aux;
        for (int i = 0; i < datos.size(); i++) {
            aux = Monitor.datos.get(i);
            invariantes.write(aux[0] + "," + aux[1] + "," + aux[2] + "," + aux[3] / 1000.0 + "\n");
        }
    }
}
package Monitor;

import RedDePetri.RedDePetri;
import RedDePetri.Transicion;
import Util.Colores;
import Util.Log;
import Util.Operaciones;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;


public class Monitor {

    public static ArrayList<int[]> datos;
    private static Semaphore semaforoMonitor;
    private RedDePetri redDePetri;
    //private Colas[] cola;
    private Politica politica = new Politica(2);
    private boolean condicion;
    private boolean flag;
    private int contador;
    private long cuenta;
    private final int cantidadDeInvariantesADisparar;
    private int relacionDeMuestra;
    private Log log;

    private Cola2[] cola2;
    private long time = System.currentTimeMillis();

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
        // cola = new Colas[redDePetri.getCantTransiciones()];
        cola2 = new Cola2[redDePetri.getCantTransiciones()];
        for (int i = 0; i < redDePetri.getCantTransiciones(); i++) {
            // cola[i] = new Colas(); //Inicialización de colas.
            cola2[i] = new Cola2();
        }
        condicion = true;
        contador = 0;
        datos = new ArrayList<int[]>();
        cuenta = System.currentTimeMillis();
        this.cantidadDeInvariantesADisparar = cantidadDeInvariantesADisparar;
        relacionDeMuestra = cantidadDeInvariantesADisparar / 10;
        //police = new Police();
    }

//    private Boolean[] quienesEstan() {
//        Boolean[] Vc = new Boolean[cola.length];
//        for (int i = 0; i < cola.length; i++) {
//            Vc[i] = !(cola[i].get() == 0);
//        }
//        return Vc;
//    }

    private Boolean[] quienesEstan2() {
        Boolean[] Vc = new Boolean[cola2.length];
        for (int i = 0; i < cola2.length; i++) {
            Vc[i] = cola2[i].isNotEmpty();
        }
        return Vc;
    }

    public void disparaTransicion(Transicion transicion) {

        acquireMonitor();
        Colores.redWrite("entro al monitor", transicion);
        if (!condicion) {
            releaseMon();
            return;
        }
        long estado = -9999;
        while (estado != -1) {
            if (!condicion) {
                releaseMon();
                return;
            }
            estado = redDePetri.disparar(transicion);
            Colores.greenWrite("estado " + estado, transicion);

            if (estado == -1) {
                update_condition(transicion.getId());
                checkPolitica(transicion);
            } else if (estado > 0) {
                semaforoMonitor.release();
                sleep_thread(transicion, estado);
                acquireMonitor();
            } else if (estado == -2) {
                cola2[transicion.getPosicion()].increment();
                semaforoMonitor.release();
                Colores.blueWrite("se va a colas", transicion);
                cola2[transicion.getPosicion()].acquire();
//                cola[transicion.getPosicion()].increment();
//                semaforoMonitor.release();
//                cola[transicion.getPosicion()].acquire();
                Colores.blueWrite("sale de colas", transicion);
            } else {
                Colores.redWrite("Error, Disparo despues beta", transicion);
                System.exit(1);
            }
        }
    }

    private void checkPolitica(Transicion transicion) {
        Boolean[] Vs = this.redDePetri.getSensibilizadas();
        Boolean[] Vc = quienesEstan2();
        System.out.printf("-------------- Vector sensibilizado %s t:%d ------------\n", Thread.currentThread().getName(), transicion.getPosicion());
        Operaciones.printB(Vs);
        System.out.printf("---------------------- Vector colas %s t:%d ------------\n", Thread.currentThread().getName(), transicion.getPosicion());
        Operaciones.printB(Vc);
        Boolean[] m = Operaciones.andVector(Vs, Vc); //todo ver si se puede simplificar
        System.out.printf("---------------------- Vector m %s t:%d -------------\n", Thread.currentThread().getName(), transicion.getPosicion());
        Operaciones.printB(m);
        if (Operaciones.comprobarUnos(m)) {
            if (semaforoMonitor.availablePermits() != 0) {
                System.out.printf("Error, valor del semaforo %d %s t:%d - politica\n", semaforoMonitor.availablePermits(), Thread.currentThread().getName(), transicion.getPosicion());
                System.exit(1);
            }
            Transicion transicionADisparar = politica.cualDisparo(m, redDePetri);
            //police.setColas(transicionADisparar);
            System.out.printf(Colores.ANSI_YELLOW + "politica - El %s desperto la t:%d  tok:%d- " + Colores.ANSI_RESET, Thread.currentThread().getName(), transicionADisparar.getPosicion(), semaforoMonitor.availablePermits());

            cola2[transicionADisparar.getPosicion()].release();
        } else {
            if (semaforoMonitor.availablePermits() != 0) {
                System.out.printf("Error, valor del semaforo %d %s t:%d - solto el monitor\n", semaforoMonitor.availablePermits(), Thread.currentThread().getName(), transicion.getPosicion());
                System.exit(1);
            }
            Colores.redWrite("solto el semaforo -  monitor - disparo y no hay nada que despertar", transicion);
            releaseMon();
        }
    }

    //todo sacar el objeto transicion
    private void sleep_thread(Transicion transicion, long tiempo) {
        Colores.purpleWrite("--- sleepTime:" + tiempo, transicion);
        try {
            Thread.sleep(tiempo);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

//
//    public void disparaTransicion(Transicion transicion) {
//        Colores.redWrite("antes de acquireMon tokens semaforo:" + semaforoMonitor.availablePermits(), transicion);
//
//        acquireMon();
//        if (!condicion) {
//            releaseMon();
//            return;
//        }
//        Colores.redWrite("entro al monitor id:" + String.valueOf(Thread.currentThread().getId()), transicion);
//
//        State state = null;
//        while (state != State.FIRE) {
//            state = redDePetri.disparar(transicion);
//            Colores.greenWrite("estado " + state.name(), transicion);
//            if (!condicion) {
//                releaseMon();
//                break;
//            }
//            switch (state) {
//                case FIRE: {
//                    Colores.greenWrite(">>>Disparo", transicion);
//                    update_condition(transicion.getId());
//                    if (!condicion) {
//                        releaseMon();
//                        break;
//                    }
//                    Boolean[] Vs = this.redDePetri.getSensibilizadasEx();
//                    Boolean[] Vc = quienesEstan();
//                    System.out.printf("-------------- Vector sensibilizado %s t:%d ------------\n", Thread.currentThread().getName(), transicion.getPosicion());
//                    Operaciones.printB(Vs);
//                    System.out.printf("---------------------- Vector colas %s t:%d ------------\n", Thread.currentThread().getName(), transicion.getPosicion());
//                    Operaciones.printB(Vc);
//                    Boolean[] m = Operaciones.andVector(Vs, Vc); //todo ver si se puede simplificar
//                    System.out.printf("---------------------- Vector m %s t:%d -------------\n", Thread.currentThread().getName(), transicion.getPosicion());
//                    Operaciones.printB(m);
////                System.out.println("---------------------- disparos -----------------------");
////                cantidadDisparada(redDePetri);
//                    if (Operaciones.comprobarUnos(m)) {
//                        if (semaforoMonitor.availablePermits() != 0) {
//                            System.out.printf("Error, valor del semaforo %d %s t:%d - politica\n", semaforoMonitor.availablePermits(), Thread.currentThread().getName(), transicion.getPosicion());
//                            System.exit(1);
//                        }
//                        Transicion transicionADisparar = politica.cualDisparo(m, redDePetri);
//                        //police.setColas(transicionADisparar);
//                        System.out.printf(Colores.ANSI_YELLOW + "politica - El %s desperto la t:%d - " + Colores.ANSI_RESET, Thread.currentThread().getName(), transicionADisparar.getPosicion());
//
//                        cola[transicionADisparar.getPosicion()].release();
//                        if (!condicion) {
//                            releaseMon();
//                            break;
//                        }
//
//                    } else {
//
//                        if (semaforoMonitor.availablePermits() != 0) {
//                            System.out.printf("Error, valor del semaforo %d %s t:%d - solto el monitor\n", semaforoMonitor.availablePermits(), Thread.currentThread().getName(), transicion.getPosicion());
//                            System.exit(1);
//                        }
//                        Colores.redWrite("solto el semaforo -  monitor - disparo y no hay nada que despertar", transicion);
//                        releaseMon();
//                    }
//                    break;
//                }
//                case NO_FIRE: {
//                    if (!condicion) {
//                        releaseMon();
//                        break;
//                    }
//                    //todo chequar si los hilos que se fueron a las colas y que se sensibilizan pueden dormir
//                    Colores.blueWrite("no disparo entro a colas", transicion);
//                    cola[transicion.getPosicion()].increment();
//                    if (semaforoMonitor.availablePermits() != 0) {
//                        System.out.printf("Error, valor del semaforo %d %s t:%d - no disparo entro a colas\n", semaforoMonitor.availablePermits(), Thread.currentThread().getName(), transicion.getPosicion());
//                        System.exit(1);
//                    }
//                    Colores.redWrite("solto el semaforo -  monitor - no disparo y se fue a colas", transicion);
//                    releaseMon();
//                    cola[transicion.getPosicion()].acquire();
//                    Colores.blueWrite("salio de colas", transicion);
//                    if (!condicion) {
//                        releaseMon();
//                        state = State.FIRE;
//                    }
//                    break;
//                }
//                case SLEEP: {
//                    if (!condicion) {
//                        releaseMon();
//                        break;
//                    }
//                    Colores.cianWrite("se fue a dormir", transicion);
//                    if (semaforoMonitor.availablePermits() != 0) {
//                        System.out.printf("Error, valor del semaforo %d %s t:%d- se fue a dormir\n", semaforoMonitor.availablePermits(), Thread.currentThread().getName(), transicion.getPosicion());
//                        System.exit(1);
//                    }
//                    Colores.redWrite("solto el semaforo -  monitor - duerme", transicion);
//
//                    releaseMon();
////                    if (semaforoMonitor.availablePermits() != 1) {
////                        System.out.printf("Error, sleep valor del semaforo %d %s t:%d - no disparo entro a colas\n", semaforoMonitor.availablePermits(), Thread.currentThread().getName(), transicion.getPosicion());
////                        System.exit(1);
////                    }
//                    //todo ver si les gusta asi. Capaz que pedir de esa forma el time stamp y el alpha no esta bien
//                    sleep_thread(transicion);
//                    acquireMon();
//                    Colores.cianWrite("salio de dormir", transicion);
//                    if (!condicion) {
//                        releaseMon();
//                        state = State.FIRE;
//                    }
//
//                    break;
//                }
//                case AFTER: {
//                    Colores.redWrite("Error,Disparo despues beta", transicion);
//                    System.exit(1);
//                }
//                default: {
//                    Colores.redWrite("Error disparo", transicion);
//                    System.exit(1);
//                }
//
//            }
//        }
//        Colores.redWrite("salio del monitor tokens semaforo: " + semaforoMonitor.availablePermits() + " condicion:" + condicion, transicion);
//    }

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

            for (int i = 0; i < cola2.length; i++) {
                while (cola2[i].isNotEmpty()) {
                    System.out.printf("t:%d ", i);
                    cola2[i].release();
                }
//                while (cola[i].get() > 0) {
//                    System.out.printf("t:%d ", i);
//                    cola[i].release();
//                }
            }
        }
    }

//    private void sleep_thread(Transicion transicion) {
//        long sleepTime = redDePetri.timeToSleep(transicion) + 1;
//        Colores.purpleWrite("--- sleepTime:" + sleepTime, transicion);
//        if (sleepTime < 0) {
//            return;
//        }
//        try {
//            Thread.sleep(sleepTime);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }

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

    public void acquireMonitor() {
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

        System.out.printf("cantidad disparada de invariantes %d\n", suma);
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

//    public void disparaTransicion(Transicion transicion) {
//        acquireMon();
//        Colores.redWrite("entro al monitor", transicion, time);
//
//        State state = null;
//        while (state != State.FIRE) {
//            state = redDePetri.disparar(transicion);
//            switch (state) {
//                case FIRE: {
//                    Colores.greenWrite(">>>Disparo", transicion, time);
//                    update_condition(transicion.getId());
//                    if (!condicion) {
//                        releaseMon();
//                        break;
//                    }
//                    Boolean[] Vs = this.redDePetri.getSensibilizadasEx();
//                    Boolean[] Vc = quienesEstan();
//                    System.out.printf("-------------- Vector sensibilizado %s t:%d time:%d------------\n", Thread.currentThread().getName(), transicion.getPosicion(), System.currentTimeMillis() - time);
//                    Operaciones.printB(Vs);
//                    System.out.printf("---------------------- Vector colas %s t:%d time:%d------------\n", Thread.currentThread().getName(), transicion.getPosicion(), System.currentTimeMillis() - time);
//                    Operaciones.printB(Vc);
//                    Boolean[] m = Operaciones.andVector(Vs, Vc); //todo ver si se puede simplificar
//                    System.out.printf("---------------------- Vector m %s t:%d time:%d------------\n", Thread.currentThread().getName(), transicion.getPosicion(), System.currentTimeMillis() - time);
//                    Operaciones.printB(m);
////                System.out.println("---------------------- disparos -----------------------");
////                cantidadDisparada(redDePetri);
//                    if (Operaciones.comprobarUnos(m)) {
//                        if (semaforoMonitor.availablePermits() != 0) {
//                            System.out.printf("valor del semaforo %d\n", semaforoMonitor.availablePermits());
//                            System.exit(1);
//                        }
//                        Transicion transicionADisparar = politica.cualDisparo(m, redDePetri);
//                        //police.setColas(transicionADisparar);
//                        System.out.printf(Colores.ANSI_YELLOW + "politica - El %s desperto la t:%d time:%d\n"
//                                + Colores.ANSI_RESET, Thread.currentThread().getName(), transicionADisparar.getPosicion(), System.currentTimeMillis() - time);
//
//                        cola[transicionADisparar.getPosicion()].release();
//
//                    } else {
//                        Colores.redWrite("solto el monitor", transicion, time);
//                        releaseMon();
//                    }
//                    break;
//                }
//                case NO_FIRE: {
//                    //todo chequar si los hilos que se fueron a las colas y que se sensibilizan pueden dormir
////                    Colores.blueWrite("no disparo entro a colas", transicion);
//                    cola[transicion.getPosicion()].increment();
//                    releaseMon();
//                    cola[transicion.getPosicion()].acquire();
//                    Colores.blueWrite("salio de colas", transicion, time);
//                    break;
//                }
//                case SLEEP: {
//                    Colores.cianWrite("se fue a dormir", transicion, time);
//
//                    releaseMon();
//                    //todo ver si les gusta asi. Capaz que pedir de esa forma el time stamp y el alpha no esta bien
//                    sleep_thread(transicion);
//                    acquireMon();
//                    Colores.cianWrite("salio de dormir", transicion, time);
//                    break;
//                }
//                case AFTER: {
//                    Colores.redWrite("Disparo despues beta", transicion, time);
//                    System.exit(1);
//                }
//                default: {
//                    Colores.redWrite("Error disparo", transicion, time);
//                    System.exit(1);
//                }
//
//            }
//        }
//    }
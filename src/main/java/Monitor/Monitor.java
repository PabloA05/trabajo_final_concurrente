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
            colasTransicion=-1;
        }

        public void patrolling(Transicion transicion) {
            if (colas) {
                if(transicion.getPosicion()!=colasTransicion){
                    if(colasTransicion==-1){
                        Colores.redWrite("Colas -1",transicion);
                        System.exit(1);
                    }

                    Colores.redWrite("Fallo",transicion);
                    System.out.printf(Colores.ANSI_RED+"transicion que se deberia disparar t:%d\n"+Colores.ANSI_RESET,colasTransicion);
                    System.exit(1);
                }
                resetColas();
            }

        }

        public void setColas(Transicion transicion) {
            this.colas = true;
            colasTransicion=transicion.getPosicion();
        }
        public void resetColas(){
            this.colas=false;
            colasTransicion=-1;
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

    public void disparaTransicion(Transicion transicion) {
        System.out.printf("valor del semaforo %d %s\n", semaforoMonitor.availablePermits(), Thread.currentThread().getName());
        acquireMon();
        Colores.cianWrite("entro al monitor", transicion);

        while (true) {
            if (!condicion) {
                releaseMon();
                break;
            }
            boolean k = this.redDePetri.disparar(transicion, semaforoMonitor);
            if (k) {
                Colores.redWrite("disparo", transicion);
                //police.patrolling(transicion);
                update_condition(transicion.getId());
                if (!condicion) {
                    releaseMon();
                    break;
                }
                Boolean[] Vs = this.redDePetri.getSensibilizadasEx();

                Boolean[] Vc = quienesEstan();
                System.out.println("---------------------- Vector sensibilizado -----------------------");
                Operaciones.printB(Vs);
                System.out.println("---------------------- Vector colas -----------------------");
                Operaciones.printB(Vc);
                Boolean[] m = Operaciones.andVector(Vs, Vc); //todo ver si se puede simplificar
                System.out.println("---------------------- Vector m -----------------------");
                Operaciones.printB(m);
                if (Operaciones.comprobarUnos(m)) {
                    if (semaforoMonitor.availablePermits() != 0) {
                        System.out.printf("valor del semaforo %d\n", semaforoMonitor.availablePermits());
                        System.exit(1);
                    }
                    Transicion transicionADisparar = politica.cualDisparo(m, redDePetri);
                    //police.setColas(transicionADisparar);
                    Colores.yellowWrite("politica despertó de las colas", transicionADisparar);
                    cola[transicionADisparar.getPosicion()].release();

                } else {
                    Colores.redWrite("solto el monitor", transicion);
                    releaseMon();
                }
                break;
            } else {
                if (!condicion) {
                    releaseMon();
                    break;
                }
                Colores.blueWrite("Entro en las colas", transicion);
                cola[transicion.getPosicion()].increment();
                cola[transicion.getPosicion()].acquire(semaforoMonitor);
                Colores.blueWrite("Se fue de las colas", transicion);
            }
        }
        Colores.cianWrite("se fue del monitor", transicion);
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

    private void acquireMon() {
        System.out.println("acquireMon " + Thread.currentThread().getName());
        try {
            semaforoMonitor.acquire();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void releaseMon() {

        semaforoMonitor.release();
        System.out.printf("releaseMon despues permisos %d %s\n" , semaforoMonitor.availablePermits(),Thread.currentThread().getName());
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
        System.out.println("releaseMonitor desde rdp permisos " + semaforoMonitor.availablePermits() + " "+Thread.currentThread().getName());
    }

    public void cantidadDisparada(RedDePetri redDePetri) {
        Transicion[] transiciones;
        transiciones = redDePetri.getTransiciones().clone();
        System.out.println("Invariante 1: " + transiciones[3].getId() + " se disparo: " + transiciones[3].getCantidadDisparada());
        System.out.println("Invariante 2: " + transiciones[4].getId() + " se disparo: " + transiciones[4].getCantidadDisparada());
        System.out.println("Invariante 3: " + transiciones[9].getId() + " se disparo: " + transiciones[9].getCantidadDisparada());
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


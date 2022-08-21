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
    }

    private Boolean[] quienesEstan() {
        Boolean[] Vc = new Boolean[cola.length];
        for (int i = 0; i < cola.length; i++) {
            Vc[i] = !(cola[i].get() == 0);
        }
        return Vc;
    }

    public void disparaTransicion(Transicion transicion) {
        acquireMonitor();
        while (true) {
            if (!condicion) {
                semaforoMonitor.release();
                break;
            }
            boolean k = this.redDePetri.disparar(transicion);
            if (k) {
                Colores.redWrite("disparo", transicion.getPosicion());
                update_condition(transicion.getId());
                if (!condicion) {
                    semaforoMonitor.release();
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
                    Colores.yellowWrite("despertó de las colas", transicionADisparar.getPosicion());
                    cola[transicionADisparar.getPosicion()].release();

                } else {
                    Colores.redWrite("solto el monitor", transicion.getPosicion());
                    semaforoMonitor.release();
                }
                break;
            } else {
                if (!condicion) {
                    semaforoMonitor.release();
                    break;
                }
                Colores.blueWrite("Entro en las colas", transicion.getPosicion());
                cola[transicion.getPosicion()].increment();
                semaforoMonitor.release();
                cola[transicion.getPosicion()].acquire();
                Colores.blueWrite("Se fue a las colas", transicion.getPosicion());
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


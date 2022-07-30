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
    private RedDePetri redDePetri;
    private Cola[] cola;
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
        cola = new Cola[redDePetri.getCantTransiciones()];
        for (int i = 0; i < redDePetri.getCantTransiciones(); i++) {
            cola[i] = new Cola(); //Inicialización de colas.
        }
        condicion = true;
        contador = 0;
        datos = new ArrayList<int[]>();
        cuenta = System.currentTimeMillis();
        this.cantidadDeInvariantesADisparar = cantidadDeInvariantesADisparar;
        relacionDeMuestra = cantidadDeInvariantesADisparar / 10;
    }

    public void disparaTransicion(Transicion transicion) {
        while (true) {
            acquireMonitor();
            Colores.yellowWrite("tomo monitor", transicion.getPosicion());
            boolean k = true;
            k = this.redDePetri.disparar(transicion);
            if (!condicion) {
                break;
            }

            if (k) {
                Colores.redWrite("Disparo", transicion.getPosicion());

                Boolean[] Vs = this.redDePetri.getSensibilizadasEx();
                System.out.println("---------------------VS ---------------------");
                Operaciones.printB(Vs);
                System.out.println("------------------- VC-----------------------");

                Boolean[] Vc = quienesEstan();
                Operaciones.printB(Vc);

                Boolean[] m = Operaciones.andVector(Vs, Vc); //todo ver si se puede simplificar
                if (Operaciones.comprobarUnos(m)) {
                    if (semaforoMonitor.availablePermits() != 0) {
                        System.out.printf(">> valor del semaforo: %d %s\n", semaforoMonitor.availablePermits(),Thread.currentThread().getName());
                        System.exit(1);
                    }
                    Transicion transicionADisparar = politica.cualDisparo(m, redDePetri);
                    Colores.redWrite("suelto monitor", transicion.getPosicion());
                    Colores.redWrite("elijo t:",transicionADisparar.getPosicion());
                    releaseMonitor();
                    cola[transicionADisparar.getPosicion()].release();
                    break;
                }
                semaforoMonitor.release();
                break;
            } else {
                semaforoMonitor.release();
                Colores.greenWrite("entro a cola", transicion.getPosicion());
                cola[transicion.getPosicion()].acquire();
            }
        }
        condicionUpdate(transicion.getId());

        Colores.cianWrite("salio! Disparos:" + contador, transicion.getPosicion());
    }

    private synchronized void condicionUpdate(String transicion) {
        contador++;
        if (condicion) {
            log.write(transicion);
        }

        if (contador >= relacionDeMuestra) {
            agregarDato(redDePetri.getTransiciones()[3].getCantidadDisparada(), redDePetri.getTransiciones()[4].getCantidadDisparada(), redDePetri.getTransiciones()[9].getCantidadDisparada());
            contador = 0;
        }

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
    }

    private Boolean[] quienesEstan() {
        Boolean[] Vc = new Boolean[cola.length];
        for (int i = 0; i < cola.length; i++) {
            Vc[i] = !(cola[i].get() == 0);
        }
        return Vc;
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


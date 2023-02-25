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
    private Semaphore semaforoMonitor;
    private RedDePetri redDePetri;
    private Politica politica = new Politica(2);
    private boolean condicion;
    private boolean flag;
    private int contador;
    private long cuenta;
    private final int cantidadDeInvariantesADisparar;
    private int relacionDeMuestra;
    private Log log;
    private Cola[] cola;
    private long time = System.currentTimeMillis();

    public Monitor(RedDePetri rdp, Log log, int cantidadDeInvariantesADisparar) {
        this.log = log;
        semaforoMonitor = new Semaphore(1, true);
        redDePetri = rdp;
        cola = new Cola[redDePetri.getCantTransiciones()];
        for (int i = 0; i < redDePetri.getCantTransiciones(); i++) {
            cola[i] = new Cola();
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
            Vc[i] = cola[i].isNotEmpty();
        }
        return Vc;
    }

    public void disparaTransicion(Transicion transicion) {
        acquireMonitor();
        if (!condicion) {
            releaseMonitor();
            return;
        }
        long estado = -9999;
        while (estado != -1) {
            if (!condicion) {
                releaseMonitor();
                return;
            }
            estado = redDePetri.disparar(transicion);
            if (estado == -1) {
                update_condition(transicion.getId());
                checkPolitica(transicion);
            } else if (estado > 0) {
                semaforoMonitor.release();
                sleep_thread(estado);
                acquireMonitor();
            } else if (estado == -2) {
                cola[transicion.getPosicion()].increment();
                semaforoMonitor.release();
                cola[transicion.getPosicion()].acquire();
                if (!condicion) {
                    return;
                }
            } else {
                Colores.redWrite("Error, Disparo despues beta", transicion);
                System.exit(1);
            }
        }
    }

    private void checkPolitica(Transicion transicion) {
        Boolean[] m = Operaciones.andVector(this.redDePetri.getSensibilizadas(), quienesEstan()); //todo ver si se puede simplificar
        if (Operaciones.comprobarUnos(m)) {
            Transicion transicionADisparar = politica.cualDisparo(m, redDePetri);
            cola[transicionADisparar.getPosicion()].release();
        } else {
            if (semaforoMonitor.availablePermits() != 0) {
                System.out.printf("Error, valor del semaforo %d %s t:%d - solto el monitor\n", semaforoMonitor.availablePermits(), Thread.currentThread().getName(), transicion.getPosicion());
                System.exit(1);
            }
            releaseMonitor();
        }
    }

    private void sleep_thread(long tiempo) {

        try {
            Thread.sleep(tiempo);
        } catch (InterruptedException e) {
            e.printStackTrace();
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
                while (cola[i].isNotEmpty()) {
                    cola[i].release();
                }
            }
        }
    }

    private void releaseMonitor() {
        semaforoMonitor.release();
    }

    private void acquireMonitor() {
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
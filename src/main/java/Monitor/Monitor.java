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

    public Monitor(RedDePetri rdp) {
        semaforoMonitor = new Semaphore(1, true);
        k = false;
        redDePetri = rdp;
        cola = new Colas[redDePetri.getCantTransisiones()];
        for (int i = 0; i < redDePetri.getCantTransisiones(); i++) {
            cola[i] = new Colas(); //Inicialización de colas.
        }
        Transicion temp[] = rdp.getTransiciones();
        for (int i = 0; i < temp.length; i++) {
            cola[i].transicion = temp[i];
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
        k = true;
        while (k) {//todo hace falta la k????
            acquireMonitor();

            System.out.print(">>>>>>>>>>>>>>>>>>>>> Hilo: " + Thread.currentThread().getName() + " entro al monitor con transicion " + transicion.getPosicion() + "\n");

            k = this.redDePetri.disparar(transicion);
            // System.out.println("valor de k:"+k);
            if (k) {
                Boolean[] Vs = this.redDePetri.getSensibilizadasExtendido();
                Boolean[] Vc = quienesEstan();
                Boolean[] m = new Boolean[Vs.length];
                m = Operaciones.andVector(Vs, Vc); //todo ver si se puede simplificar
                //cantidadDisparada(redDePetri);
                for (int i = 0; i < m.length; i++) {

                }
                if (Operaciones.comprobarUnos(m)) {
                    try {
                        int transicionaDisparar = 0;
//                        transicion= politica.cualDisparo(m, redDePetri);
                        for (int i = 0; i < m.length; i++) {
                            if (m[i]) {
                                transicionaDisparar = i;
                            }

                        }
                        cola[transicion.getPosicion()].release();
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
                System.out.printf("\n>>>>>>>>>>> posicion a meter colas %d - %s\n", transicion.getPosicion(), Thread.currentThread().getName());
                transicion = cola[transicion.getPosicion()].acquire();
                System.out.printf("<<<<<<<<<<<<<<< sale transion  colas %d - %s\n", transicion.getPosicion(), Thread.currentThread().getName());
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
        transiciones = redDePetri.getTransiciones();
        for (int i = 0; i < redDePetri.getCantTransisiones(); i++) {
            System.out.println("La transicion: " + (transiciones[i].getPosicion() + 1) + " se disparo: " + transiciones[i].getCantidadDisparada());
        }
    }

}


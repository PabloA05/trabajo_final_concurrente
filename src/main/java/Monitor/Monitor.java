package Monitor;

import RedDePetri.RedDePetri;
import RedDePetri.Transicion;
import Util.Log;

import java.util.ArrayList;
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
    private long cuenta =0;
    public Monitor(RedDePetri rdp) {
        semaforoMonitor = new Semaphore(1, true);
        //k = false;
        redDePetri = rdp;
        cola = new Colas[redDePetri.getCantTransiciones()];
        for (int i = 0; i < redDePetri.getCantTransiciones(); i++) {
            cola[i] = new Colas(); //Inicialización de colas.
        }
        condicion = true;
        //flag = true;
        contador =0;
        datos = new ArrayList<int[]>();
        cuenta = System.currentTimeMillis();
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
        while (condicion) {//todo hace falta la k????
            acquireMonitor();
            boolean k = true;
            System.out.print(ANSI_YELLOW + "Hilo: " + Thread.currentThread().getId() + " entro al monitor con transicion " + transicion.getPosicion() + " " + Thread.currentThread().getName() + ANSI_RESET + "\n");
            k = this.redDePetri.disparar(transicion);
            if(!condicion){
                break;
            }

            if (k) {
                System.out.printf(ANSI_BLUE + "Disparo transicion: %d %s\n" + ANSI_RESET, transicion.getPosicion(), Thread.currentThread().getName());
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

                if (Operaciones.comprobarUnos(m)) {
                    try {
                        if (semaforoMonitor.availablePermits() != 0) {
                            System.out.printf("valor del semaforo %d\n", semaforoMonitor.availablePermits());
                            //System.exit(1);

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
                if(!condicion){
                    break;
                }
                cola[transicion.getPosicion()].acquire();
                System.out.printf(ANSI_GREEN + "salio de cola t:%d %s\n" + ANSI_RESET, transicion.getPosicion(), Thread.currentThread().getName());
            }

        }

        Log.write(transicion.getId());
        contador++;
        if(contador>=10){
            agregarDato(redDePetri.getTransiciones()[3].getCantidadDisparada(),redDePetri.getTransiciones()[4].getCantidadDisparada(),redDePetri.getTransiciones()[9].getCantidadDisparada());
            contador=0;
        }

        int aux=0;
        setCondicion();
        if(!condicion && flag){
            flag = false;
            for(int i=0;i<redDePetri.getCantTransiciones();i++){
                if (!cola[i].isEmpty()) {
                    while(!cola[i].isEmpty()) {
                        aux++;
                        System.out.println("AUX: "+aux+"Name Thread:" + Thread.currentThread().getName());
                        cola[i].release();}
                }
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

    public boolean getCondicion(){
        return condicion;
    }

    public void setCondicion(){
        Transicion[] transiciones;
        transiciones = redDePetri.getTransiciones().clone();
        int suma = 0;
        suma = transiciones[3].getCantidadDisparada() + transiciones[4].getCantidadDisparada() + transiciones[9].getCantidadDisparada();
        if(suma>=50){
            condicion = false;
            flag = true;
            System.out.println("Es condicion false");
        }

    }

    public static void releaseMonitor() {
        semaforoMonitor.release();
    }

    public void cantidadDisparada(RedDePetri redDePetri) {
        Transicion[] transiciones;
        transiciones = redDePetri.getTransiciones().clone();
    }

    public void agregarDato(int a, int b, int c) {
        System.out.println("Agrego dato");
        long actual = System.currentTimeMillis() - cuenta;

        System.out.println("Actual: " + actual);
        datos.add((new int[]{a, b, c, (int) actual}));
    }


}


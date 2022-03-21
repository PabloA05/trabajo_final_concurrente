package RedDePetri;

import Monitor.Monitor;
import Util.Operaciones;

import java.util.ArrayList;
import java.util.Arrays;

public class RedDePetri {

    int[][] incidencia;
    int[][] tInvariantes;
    final int[][] inhibidor;
    // private int[][] intervalos_tiempo; //matriz de intervalos de tiempo
    final int[] mki; //marca inicial. columna. NO VARIA
    private int[] vectorDeEstado; //la marca actual
    private SensibilizadasConTiempo[] transicionesConTiempo;
    private Boolean[] sensibilizadasEx;
    private Boolean[] Q;
    private Boolean[] Z;
    private Boolean[] B;
    int[] mj_1;// la siguiente
    //private int[] e; //vector de transiciones sensibilizadas
    int[] ex; //vector de sensibilizado extendido
    //private int[] z; //Vector de transiciones des-sensibilizadas por tiempo
    //  private boolean k = false;
    private boolean[] VectorSensibilazadas;
    private Transicion[] transiciones;
    private ArrayList<ArrayList<Integer>> pInvariantes;
    //private Boolean[] sensibilizadasEx;

    public RedDePetri(String mji, String I, String h, String t, String T) {


        //  e_semaphore = new Semaphore(1, true);//no se  si lo voy a usar

        this.incidencia = Operaciones.matriz2d(I);
        this.vectorDeEstado = Operaciones.vector(mji);
        this.inhibidor = Operaciones.matriz2d(h);
        this.tInvariantes = Operaciones.matriz2d(T);
        pInvariantes = Operaciones.setPinvariantes("src/main/resources/pInvariantes.csv");
        this.mki = vectorDeEstado.clone(); //marca inicial
        /*sensibilizadas = new Boolean[getCantTransisiones()];
        for (int i = 0; i < getCantTransisiones(); i++) {
            sensibilizadas[i] = false;
        }
        sensibilizadasEx = new Boolean[getCantTransisiones()];
        for (int i = 0; i < getCantTransisiones(); i++) {
            sensibilizadasEx[i] = false;
        }*/
        int[][] tiempos = Operaciones.matriz2d(t);
        this.transicionesConTiempo = new SensibilizadasConTiempo[getCantTransiciones()];
        for (int i = 0; i < transicionesConTiempo.length; i++) {
            transicionesConTiempo[i] = new SensibilizadasConTiempo((long) tiempos[0][i], (long) tiempos[1][i]);
        }
        transiciones = new Transicion[getCantTransiciones()];
        for (int i = 0; i < getCantTransiciones(); i++) {
            transiciones[i] = new Transicion("T" + i, i, transicionesConTiempo[i].esTemporal());
        }

        Boolean[] temp = new Boolean[transiciones.length];
        Arrays.fill(temp, false);
        actualizaSensibilizadasExtendido();
        setNuevoTimeStamp();

    }

    public int[][] gettInvariantes() {
        return tInvariantes;
    }

    public Boolean[] getVectorE() {
        Boolean[] sensibilizadas = new Boolean[getCantTransiciones()];
        for (int i = 0; i < getCantTransiciones(); i++) {
            try {
                sensibilizadas[i] = esDisparoValido(marcadoSiguiente(vectorDeEstado, i));
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Error en getSensibilizadas()");
            }
        }
        return sensibilizadas;
    }
//todo completar

    public boolean disparar(Transicion transicion) {//todo para transiciones inmediatas
        boolean k = false;
        boolean esperando;
        boolean ventana;
        // if (estaSensibilizado(transicion.getPosicion())) {
        while (sensibilizadasEx[transicion.getPosicion()] && !transicionesConTiempo[transicion.getPosicion()].isEsperando()) {

            ventana = transicionesConTiempo[transicion.getPosicion()].testVentanaTiempo();
            esperando = transicionesConTiempo[transicion.getPosicion()].isEsperando();

            if (ventana) {
                if (!esperando) {
                    k = true;
                    break;
                }
            } else {
                boolean antes = antesDeLaVentana(transicion.getPosicion());
                //System.out.printf("timestamp:%d t:%d %s\n", transicionesConTiempo[transicion.getPosicion()].getTimeStamp(), transicion.getPosicion(), Thread.currentThread().getName());
                Monitor.releaseMonitor();

//                System.out.printf("esperando %b %s t:%d \n", transicionesConTiempo[transicion.getPosicion()].isEsperando(), Thread.currentThread().getName(), transicion.getPosicion());
//                if (transicionesConTiempo[transicion.getPosicion()].isEsperando()) {
//                    System.out.printf("?? %s esp_id:%d hilo_id:%d\n", Thread.currentThread().getName(), id, Thread.currentThread().getId());
//                }

                boolean pudoDormir = true;
                if (antes && !esperando) {
//                    if (transicionesConTiempo[transicion.getPosicion()].isEsperando()) {
//                        System.out.printf("fallo %s t:%d esp:%b esp_id:%d hilo_id:%d\n", Thread.currentThread().getName(), transicion.getPosicion(),
//                                transicionesConTiempo[transicion.getPosicion()].isEsperando(),
//                                transicionesConTiempo[transicion.getPosicion()].getId(),
//                                Thread.currentThread().getId());
//                        System.exit(1);
//                    }
                    // System.out.printf(">>> entro sleep transicion:%d %s\n", transicion.getPosicion(), Thread.currentThread().getName());
                    transicionesConTiempo[transicion.getPosicion()].setEsperando();
                    transicionesConTiempo[transicion.getPosicion()].setId(Thread.currentThread().getId());
                    pudoDormir = sleepThread(transicion.getPosicion());
                    //   System.out.printf("<<< salio del sleep %s\n", Thread.currentThread().getName());

                } else if (!esperando) {
                    System.out.printf("mayor que beta %s t:%d esp:%b\n",
                            Thread.currentThread().getName(), transicion.getPosicion(), transicionesConTiempo[transicion.getPosicion()].isEsperando());
                    System.exit(1);
                }
                Monitor.acquireMonitor();


                if (sensibilizadasEx[transicion.getPosicion()] && pudoDormir) {
//                    System.out.printf("entro aca ts:%d esp:%b id_esp:%d id_h:%d\n", transicionesConTiempo[transicion.getPosicion()].getTimeStamp(), transicionesConTiempo[transicion.getPosicion()].isEsperando(),
//                            transicionesConTiempo[transicion.getPosicion()].getId(), Thread.currentThread().getId());
                    ventana = transicionesConTiempo[transicion.getPosicion()].testVentanaTiempo();
                    if (ventana) {
                        if ((transicionesConTiempo[transicion.getPosicion()].isEsperando()
                                && (transicionesConTiempo[transicion.getPosicion()].getId() == Thread.currentThread().getId()))) {
                            k = true;
                            break;
                        }
                    }
//                    System.out.printf("****%s t:%d test:%b esp:%b esp_id:%d hilo_id:%d\n", Thread.currentThread().getName(), transicion.getPosicion(), ventana,
//                            transicionesConTiempo[transicion.getPosicion()].isEsperando(),
//                            transicionesConTiempo[transicion.getPosicion()].getId(),
//                            Thread.currentThread().getId());
                }
//                System.out.printf("salio else transicion:%d %s k:%b t:%d\n",
//                        transicion.getPosicion(), Thread.currentThread().getName(), k, transicion.getPosicion());

            }
            //   System.out.printf("salio transicion:%d %s k:%b t:%d v:%b\n", transicion.getPosicion(), Thread.currentThread().getName(), k, transicion.getPosicion(), ventana);

        }


        if (k) {

            transicionesConTiempo[transicion.getPosicion()].resetTimestamp();
            // Operaciones.printVector(vectorDeEstado);
            verificarPInvariantes();
            vectorDeEstado = marcadoSiguiente(vectorDeEstado, transicion.getPosicion());
            actualizaSensibilizadasExtendido();
            setNuevoTimeStamp();
            //Operaciones.printB(getSensibilizadasExtendido());
            transicion.incrementoDisparo();
        }
        if (!k &&
                transicionesConTiempo[transicion.getPosicion()].isEsperando() &&
                transicionesConTiempo[transicion.getPosicion()].getId() == Thread.currentThread().getId()) {

            //   if (!k && transicion.isTemportizada() && transicionesConTiempo[transicion.getPosicion()].isEsperando()) {
            transicionesConTiempo[transicion.getPosicion()].resetEsperando();
        }
        return k;
//        boolean k = false;
//        if (this.getSensibilizadasExtendido()[transicion.getPosicion()]) {
//            Operaciones.printVector(vectorDeEstado);
//
//            vectorDeEstado = marcadoSiguiente(vectorDeEstado, transicion.getPosicion());
//            transicion.incrementoDisparo();
//
//            return true;
//        } else {
//            return false;
//        }
    }

    public Boolean[] getSensibilizadasEx() {
        return sensibilizadasEx;
    }

    private boolean sePuedeDispara(Transicion transicion) {
        return (sensibilizadasEx[transicion.getPosicion()] &&
                transicionesConTiempo[transicion.getPosicion()].testVentanaTiempo());
    }

    private void sincronizar(Transicion t) {

        System.out.println("La transicion: " + (t.getPosicion() + 1) + " en el tiempo: " + System.currentTimeMillis() / 1000);
        Operaciones.printVector(vectorDeEstado);

    }

    private void setNuevoTimeStamp() {
        for (int i = 0; i < transicionesConTiempo.length; i++) {
            if (sensibilizadasEx[i] && transiciones[i].isTemportizada()) {///

                transicionesConTiempo[i].nuevoTimeStamp();
            }

        }
    }

    public void setTransiciones(Transicion[] trans) {
        this.transiciones = trans;
    }


    private boolean sleepThread(int posicion) { //todo no se si esta bien
        long sleepTime = transicionesConTiempo[posicion].getTimeStamp() + transicionesConTiempo[posicion].getAlpha() - System.currentTimeMillis();
        if (sleepTime < 0) {
            return false;
        }
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return true;
    }


    private boolean antesDeLaVentana(int posicion) {
        long actual = System.currentTimeMillis();
//        System.out.printf("t:%d %s start:%d alpha:%d beta:%d actual:%d suma:%d resta:%d\n", posicion,
//                Thread.currentThread().getName(),
//                transicionesConTiempo[posicion].getTimeStamp(),
//                transicionesConTiempo[posicion].getAlpha(),
//                transicionesConTiempo[posicion].getBeta(),
//                actual,
//                (transicionesConTiempo[posicion].getTimeStamp() + transicionesConTiempo[posicion].getAlpha()),
//                (transicionesConTiempo[posicion].getTimeStamp() - actual));
        return (transicionesConTiempo[posicion].getTimeStamp() + transicionesConTiempo[posicion].getAlpha() > actual);
    }


    public int[] getVectorDeEstado() {
        return vectorDeEstado;
    }

    public int getCantTransiciones() {
        return incidencia[0].length;
    }

    public int getCantPlazas() {
        return incidencia.length;
    }

    public int[][] getInhibidor() {
        return inhibidor;
    }

    public void calculoDeVectorEstado(Transicion transicion) {
        vectorDeEstado = marcadoSiguiente(vectorDeEstado, transicion.getPosicion());

    }

/*    public void calculoDeVectorEstado(Transicion transicion) {
        for (int i = 0; i < vectorDeEstado.length; i++) {
            vectorDeEstado[i] += incidencia[i][transicion.getPosicion()];
        }
    }*/

    public int[] getColumna() {

        return new int[0];
    }


    public int[][] getIncidencia() {
        return incidencia;
    }

    public void verificarPInvariantes() {
        int suma = 0;

        for (int i = 0; i < pInvariantes.size(); i++) {
            ArrayList<Integer> a = new ArrayList<>();
            a = pInvariantes.get(i);
            suma = 0;
            for (int j = 0; j < a.size() - 1; j++) {
                int aux = a.get(j) - 1;
                suma += vectorDeEstado[a.get(j) - 1];
            }
            if (suma != a.get(a.size() - 1)) {
                System.out.println("No se cumple el invariante " + i + " de plaza");
                System.exit(1);
            }
        }
    }

    public boolean esDisparoValido(int[] marcado_siguiente) throws NullPointerException {

        for (int j : marcado_siguiente) {
            if (j < 0) {
                return false;
            }
        }
        return true;
    }

    public Transicion[] getTransiciones() {
        return transiciones;
    }

    public boolean esTemporizada(int a) {
        //return a == 5 || a == 6 || a == 9 || a==11 || a==2 ;
        return false;
    }

    public int[] marcadoSiguiente(int[] old, int position) {
        /*calcularVectorB();
        Operaciones.printVector(vectorDeEstado);
        System.out.print("entro>>>>>>>>>>>>>>>>>>>>>>>\n");*/
        int[] temp = new int[old.length];
        for (int i = 0; i < temp.length; i++) {
            temp[i] = old[i] + incidencia[i][position];
        }
        //Operaciones.printVector(temp);
        //System.out.print("salio<<<<<<<<<<<<<<<<<<<<<<<<<<<\n");


        return temp;
    }


    public Boolean[] getVectorQ() {
        Boolean[] vectorQ = new Boolean[getVectorDeEstado().length];

        for (int i = 0; i < getCantPlazas(); i++) {
            vectorQ[i] = vectorDeEstado[i] != 0;
        }
        return vectorQ;
    }

    public Boolean[] getVectorB() {

        Boolean[] vectorB = new Boolean[getCantTransiciones()];
        int[][] inhibidorTranspuesta = Operaciones.transpuesta(this.inhibidor);
        vectorB = Operaciones.productoMatrizVectorBoolean(inhibidorTranspuesta, this.getVectorQ());
        for (int i = 0; i < vectorB.length; i++) {
            vectorB[i] = !vectorB[i];
        }

        B = vectorB;

        return this.B.clone();

    }

    public void actualizaSensibilizadasExtendido() {

        sensibilizadasEx = Operaciones.andVector(getVectorE(), getVectorB());

        /*if(sensibilizadasEx[0] || sensibilizadasEx[6]){
            for (int i = 0; i < getCantTransiciones(); i++) {
                if(!(i == 0 || i==6)){
                    sensibilizadasEx[i] = false;
                }
            }
        }*/

    }

    public void setVectorDeEstado(int[] vector) {
        this.vectorDeEstado = vector;
    }

}
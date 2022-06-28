package RedDePetri;

import Monitor.Monitor;
import Util.Operaciones;

import java.util.ArrayList;

public class RedDePetri {

    int[][] incidencia;
    int[][] tInvariantes;
    final int[][] inhibidor;
    // private int[][] intervalos_tiempo; //matriz de intervalos de tiempo
    final int[] mki; //marca inicial. columna. NO VARIA
    private int[] vectorDeEstado; //la marca actual
    private final SensibilizadasConTiempo[] transicionesConTiempo;
    private Boolean[] sensibilizadasEx;

    private final Transicion[] transiciones;
    private final ArrayList<ArrayList<Integer>> pInvariantes;
    private final ArrayList<Integer> soloInmediatas;
    private final Boolean activoLogicaInmediata;

    public RedDePetri(String mji, String I, String h, String t, String T, String Pinv) {


        //  e_semaphore = new Semaphore(1, true);//no se  si lo voy a usar

        this.incidencia = Operaciones.matriz2d(I);
        this.vectorDeEstado = Operaciones.vector(mji);
        this.inhibidor = Operaciones.matriz2d(h);
        this.tInvariantes = Operaciones.matriz2d(T);
        pInvariantes = Operaciones.setPinvariantes(Pinv);
        this.mki = vectorDeEstado.clone(); //marca inicial

        int[][] tiempos = Operaciones.transpuesta(Operaciones.matriz2d(t));
        this.transicionesConTiempo = new SensibilizadasConTiempo[getCantTransiciones()];
        for (int i = 0; i < transicionesConTiempo.length; i++) {
            transicionesConTiempo[i] = new SensibilizadasConTiempo((long) tiempos[0][i], (long) tiempos[1][i]);
        }
        transiciones = new Transicion[getCantTransiciones()];
        for (int i = 0; i < getCantTransiciones(); i++) {
            transiciones[i] = new Transicion("T" + i, i, transicionesConTiempo[i].esTemporal());
        }
        soloInmediatas = getsoloInmediatas();
        activoLogicaInmediata = true;
        actualizaSensibilizadasExtendido();
    }

    public int[][] gettInvariantes() {
        return tInvariantes;
    }

    public Boolean[] getVectorE() {
        Boolean[] sensibilizadas = new Boolean[getCantTransiciones()];
        for (int i = 0; i < getCantTransiciones(); i++) {
            sensibilizadas[i] = esDisparoValido(marcadoSiguiente(vectorDeEstado, i));
        }
        return sensibilizadas;
    }

    public boolean disparar(Transicion transicion) {
        boolean k = false;
        boolean esperando = false;
        boolean ventana;
        // if (estaSensibilizado(transicion.getPosicion())) {
        while (sensibilizadasEx[transicion.getPosicion()] && !transicionesConTiempo[transicion.getPosicion()].isEsperando()
                || sensibilizadasEx[transicion.getPosicion()] && transicionesConTiempo[transicion.getPosicion()].isEsperando()
                && Thread.currentThread().getId() == transicionesConTiempo[transicion.getPosicion()].getId()) {

            if (!transicion.isTemporizada()) {
                k = true;
                break;
            }
            ventana = transicionesConTiempo[transicion.getPosicion()].testVentanaTiempo();
            esperando = transicionesConTiempo[transicion.getPosicion()].isEsperando();

            if (ventana) {
                if (!esperando || transicionesConTiempo[transicion.getPosicion()].isEsperando()
                        && Thread.currentThread().getId() == transicionesConTiempo[transicion.getPosicion()].getId()) {
                    k = true;
                    break;
                }
            } else if (!esperando) {
                boolean antes = antesDeLaVentana(transicion.getPosicion());
                // System.out.printf("timestamp:%d t:%d %s\n", transicionesConTiempo[transicion.getPosicion()].getTimeStamp(), transicion.getPosicion(), Thread.currentThread().getName());


//                System.out.printf("esperando %b %s t:%d \n", transicionesConTiempo[transicion.getPosicion()].isEsperando(), Thread.currentThread().getName(), transicion.getPosicion());
//                if (transicionesConTiempo[transicion.getPosicion()].isEsperando()) {
//                    System.out.printf("?? %s esp_id:%d hilo_id:%d\n", Thread.currentThread().getName(), id, Thread.currentThread().getId());
//                }


                if (antes) {
//                    if (transicionesConTiempo[transicion.getPosicion()].isEsperando()) {
//                        System.out.printf("fallo %s t:%d esp:%b esp_id:%d hilo_id:%d\n", Thread.currentThread().getName(), transicion.getPosicion(),
//                                transicionesConTiempo[transicion.getPosicion()].isEsperando(),
//                                transicionesConTiempo[transicion.getPosicion()].getId(),
//                                Thread.currentThread().getId());
//                        System.exit(1);
//                    }
                    // System.out.printf(">>> entro sleep transicion:%d %s\n", transicion.getPosicion(), Thread.currentThread().getName());
                    transicionesConTiempo[transicion.getPosicion()].setEsperando();
                    Monitor.releaseMonitor();

                    esperando = true;
                    // transicionesConTiempo[transicion.getPosicion()].setId(Thread.currentThread().getId());
                    sleepThread(transicion.getPosicion());
                    // System.out.printf(Colores.ANSI_YELLOW + "<<< salio del sleep %s\n" + Colores.ANSI_RESET, Thread.currentThread().getName());

                } else {
                    System.out.printf("mayor que beta %s t:%d esp:%b\n",
                            Thread.currentThread().getName(), transicion.getPosicion(), transicionesConTiempo[transicion.getPosicion()].isEsperando());
                    System.exit(1);
                }
                Monitor.acquireMonitor();
//                System.out.printf("tomo monitor transicion:%d %s k:%b t:%d\n",
//                        transicion.getPosicion(), Thread.currentThread().getName(), k, transicion.getPosicion());

//                if (sensibilizadasEx[transicion.getPosicion()] && pudoDormir) {
//                    System.out.printf("pudo dormir ts:%d esp:%b id_esp:%d id_h:%d\n", transicionesConTiempo[transicion.getPosicion()].getTimeStamp(), transicionesConTiempo[transicion.getPosicion()].isEsperando(),
//                            transicionesConTiempo[transicion.getPosicion()].getId(), Thread.currentThread().getId());
//                    ventana = transicionesConTiempo[transicion.getPosicion()].testVentanaTiempo();
//                    if (ventana) {
//                        if ((transicionesConTiempo[transicion.getPosicion()].isEsperando()
//                                && (transicionesConTiempo[transicion.getPosicion()].getId() == Thread.currentThread().getId()))) {
//                            k = true;
//                            break;
//                        }
//                    }
////                    System.out.printf("****%s t:%d test:%b esp:%b esp_id:%d hilo_id:%d\n", Thread.currentThread().getName(), transicion.getPosicion(), ventana,
////                            transicionesConTiempo[transicion.getPosicion()].isEsperando(),
////                            transicionesConTiempo[transicion.getPosicion()].getId(),
////                            Thread.currentThread().getId());
//                }


            }
            //   System.out.printf(Colores.ANSI_RED + "salio transicion:%d %s k:%b t:%d v:%b\n" + Colores.ANSI_RESET, transicion.getPosicion(), Thread.currentThread().getName(), k, transicion.getPosicion(), ventana);

        }


        if (k) {


            if (transicion.isTemporizada()) {
                transicionesConTiempo[transicion.getPosicion()].resetTimestamp();
            }

            // Operaciones.printVector(vectorDeEstado);
            verificarPInvariantes();
            vectorDeEstado = marcadoSiguiente(vectorDeEstado, transicion.getPosicion());
            actualizaSensibilizadasExtendido();
            // setNuevoTimeStamp(transicionesAnteriores);
            //Operaciones.printB(getSensibilizadasExtendido());
            transicion.incrementoDisparo();
        } else if (esperando && Thread.currentThread().getId() == transicionesConTiempo[transicion.getPosicion()].getId()) {
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


    private void sleepThread(int posicion) {
        long sleepTime = transicionesConTiempo[posicion].getTimeStamp() + transicionesConTiempo[posicion].getAlpha() - System.currentTimeMillis();
        if (sleepTime < 0) {
            return;
        }
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
        return (transicionesConTiempo[posicion].getTimeStamp() + transicionesConTiempo[posicion].getAlpha() >= actual);
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


    public void verificarPInvariantes() {
        int suma;

        for (int i = 0; i < pInvariantes.size(); i++) {
            ArrayList<Integer> a;
            a = pInvariantes.get(i);
            suma = 0;
            for (int j = 0; j < a.size() - 1; j++) {
                suma += vectorDeEstado[a.get(j) - 1];
            }
            if (suma != a.get(a.size() - 1)) {
                System.out.println("No se cumple el invariante " + i + " de plaza");
                System.exit(1);
            }
        }
    }

    public boolean esDisparoValido(int[] marcado_siguiente) {

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

        Boolean[] vectorB;
        int[][] inhibidorTranspuesta = Operaciones.transpuesta(this.inhibidor);
        vectorB = Operaciones.productoMatrizVectorBoolean(inhibidorTranspuesta, this.getVectorQ());
        for (int i = 0; i < vectorB.length; i++) {
            vectorB[i] = !vectorB[i];
        }

        return vectorB;

    }

    public void actualizaSensibilizadasExtendido() {

        sensibilizadasEx = Operaciones.andVector(getVectorE(), getVectorB());
        long timeStamp = System.currentTimeMillis();
        for (int i = 0; i < transicionesConTiempo.length; i++) {
            if (sensibilizadasEx[i] && transiciones[i].isTemporizada()) {///
                transicionesConTiempo[i].nuevoTimeStamp(timeStamp);
            }
        }

        if (activoLogicaInmediata) {
            for (int i = 0; i < soloInmediatas.size(); i++) {
                if (sensibilizadasEx[soloInmediatas.get(i)]) {
                    for (int j = 0; j < sensibilizadasEx.length; j++) {
                        if (sensibilizadasEx[j] && transiciones[j].isTemporizada()) {
                            sensibilizadasEx[j] = false;
                        }
                    }
                    break;
                }
            }
        }

    }

    public ArrayList<Integer> getsoloInmediatas() {

        ArrayList<Integer> soloInmediatas = new ArrayList<>();

        for (int i = 0; i < getCantTransiciones(); i++) {
            if (!transiciones[i].isTemporizada()) {
                soloInmediatas.add(i);
            }
        }
        return soloInmediatas;
    }

}
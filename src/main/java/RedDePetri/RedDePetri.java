package RedDePetri;

import Util.Colores;
import Util.Operaciones;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class RedDePetri {

    private final int[][] incidencia;
    private final int[][] tInvariantes;
    private final int[][] inhibidor;
    private int[] vectorDeEstado; //la marca actual
    private final SensibilizadasConTiempo[] transicionesConTiempo;
    private Boolean[] sensibilizadasEx;
    private final Transicion[] transiciones;
    private final ArrayList<ArrayList<Integer>> pInvariantes;
    private final ArrayList<Integer> soloInmediatas;
    private final Boolean activoLogicaInmediata;


    public RedDePetri(String mji, String I, String h, String t, String T, String Pinv) {
        this.incidencia = Operaciones.matriz2d(I);
        this.vectorDeEstado = Operaciones.vector(mji);
        this.inhibidor = Operaciones.matriz2d(h);
        this.tInvariantes = Operaciones.matriz2d(T);
        this.pInvariantes = Operaciones.setPinvariantes(Pinv);
        int[][] tiempos = Operaciones.transpuesta(Operaciones.matriz2d(t));
        this.transicionesConTiempo = new SensibilizadasConTiempo[getCantTransiciones()];
        for (int i = 0; i < transicionesConTiempo.length; i++) {
            this.transicionesConTiempo[i] = new SensibilizadasConTiempo((long) tiempos[0][i], (long) tiempos[1][i]);
        }
        this.transiciones = new Transicion[getCantTransiciones()];
        for (int i = 0; i < getCantTransiciones(); i++) {
            this.transiciones[i] = new Transicion("T" + i, i, transicionesConTiempo[i].esTemporal());
        }
        this.soloInmediatas = getsoloInmediatas();
        this.activoLogicaInmediata = true;
        Boolean[] temp = new Boolean[getCantTransiciones()];
        actualizaSensibilizadasExtendido(temp);
    }

    public State disparar(Transicion transicion) {
        boolean k = false;
        if (!transicion.isTemporizada()) {
            k = true;
        } else {
            for (int i = 0; i < soloInmediatas.size(); i++) {
                if (sensibilizadasEx[soloInmediatas.get(i)]) {
                    return State.NO_FIRE;
                }
            }
        }//todo solo para temporales, y no esta esparando el
        if (transicionesConTiempo[transicion.getPosicion()].testVentanaTiempo() && !transicionesConTiempo[transicion.getPosicion()].isEsperando2()) {
            if (antesDeLaVentana(transicion.getPosicion())) {
                transicionesConTiempo[transicion.getPosicion()].setEsperando();
                return State.SLEEP;
            }
            k = true;
        }
        if (k) {
            Colores.blueWrite("pudo disparar", transicion);
            if (transicion.isTemporizada()) {
                transicionesConTiempo[transicion.getPosicion()].resetEsperando();
            }
            verificarPInvariantes();
            Boolean[] tempSensibilizadas = sensibilizadasEx;
            vectorDeEstado = marcadoSiguiente(vectorDeEstado, transicion.getPosicion());
            actualizaSensibilizadasExtendido(tempSensibilizadas);
            transicion.incrementoDisparo();
            return State.FIRE;
        }// todo ver si el esparando esta bien
        return State.NO_FIRE;
    }

    public boolean disparar(Transicion transicion, Semaphore semaforoMonitor) {
        boolean k = false;
        boolean esperando = false;
        boolean ventana = false;

        if (transicion.isTemporizada()) {
            for (int i = 0; i < soloInmediatas.size(); i++) {
                if (sensibilizadasEx[soloInmediatas.get(i)]) {
                    return false;
                }
            }
        }

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
                k = true;
                break;
            } else if (!esperando) {
                boolean antes = antesDeLaVentana(transicion.getPosicion());
                if (antes) {
                    transicionesConTiempo[transicion.getPosicion()].setEsperando();

                    semaforoMonitor.release();
                    esperando = true;
                    sleepThread(transicion.getPosicion());
                } else {
                    System.out.printf("mayor que beta %s t:%d esp:%b\n",
                            Thread.currentThread().getName(), transicion.getPosicion(), transicionesConTiempo[transicion.getPosicion()].isEsperando());
                    System.exit(1);
                }
                try {
                    semaforoMonitor.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }
        if (k) {
            if (transicion.isTemporizada()) {
                transicionesConTiempo[transicion.getPosicion()].resetEsperando();
            }
            verificarPInvariantes();
            Boolean[] tempSensibilizadas = sensibilizadasEx;
            vectorDeEstado = marcadoSiguiente(vectorDeEstado, transicion.getPosicion());
            actualizaSensibilizadasExtendido(tempSensibilizadas);
            transicion.incrementoDisparo();
        } else if (esperando && Thread.currentThread().getId() == transicionesConTiempo[transicion.getPosicion()].getId()) {
            transicionesConTiempo[transicion.getPosicion()].resetEsperando();
        }
        return k;
    }

    public Boolean[] getSensibilizadasEx() {
        return sensibilizadasEx;
    }

    public void sleepThread(int posicion) {
        //    System.out.printf("se fue a dormir %s t:%d\n" , Thread.currentThread().getName(), posicion);
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
        return (transicionesConTiempo[posicion].getTimeStamp() + transicionesConTiempo[posicion].getAlpha() >= actual);
    }

    private int[] getVectorDeEstado() {
        return vectorDeEstado;
    }

    public int getCantTransiciones() {
        return incidencia[0].length;
    }

    private int getCantPlazas() {
        return incidencia.length;
    }

    private void verificarPInvariantes() {
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

    private boolean esDisparoValido(int[] marcado_siguiente) {

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

    private int[] marcadoSiguiente(int[] old, int position) {
        int[] temp = new int[old.length];
        for (int i = 0; i < temp.length; i++) {
            temp[i] = old[i] + incidencia[i][position];
        }
        return temp;
    }


    private Boolean[] getVectorQ() {
        Boolean[] vectorQ = new Boolean[getVectorDeEstado().length];

        for (int i = 0; i < getCantPlazas(); i++) {
            vectorQ[i] = vectorDeEstado[i] != 0;
        }
        return vectorQ;
    }

    private Boolean[] getVectorB() {

        Boolean[] vectorB;
        int[][] inhibidorTranspuesta = Operaciones.transpuesta(this.inhibidor);
        vectorB = Operaciones.productoMatrizVectorBoolean(inhibidorTranspuesta, this.getVectorQ());
        for (int i = 0; i < vectorB.length; i++) {
            vectorB[i] = !vectorB[i];
        }

        return vectorB;

    }

    private Boolean[] getVectorE() {
        Boolean[] sensibilizadas = new Boolean[getCantTransiciones()];
        for (int i = 0; i < getCantTransiciones(); i++) {
            sensibilizadas[i] = esDisparoValido(marcadoSiguiente(vectorDeEstado, i));
        }
        return sensibilizadas;
    }

    private Boolean[] getVectorZ() {
        Boolean[] vectorZ = new Boolean[getCantTransiciones()];
        System.out.println("---------------------- Vector e  rdp -----------------------");
        Operaciones.printB(getVectorE());
        System.out.println("---------------------- Vector b  rdp -----------------------");
        Operaciones.printB(getVectorB());

        Boolean sensi[] = Operaciones.andVector(getVectorE(), getVectorB());
        System.out.println("---------------------- Vector sensi  rdp -----------------------");
        Operaciones.printB(sensi);

        for (int i = 0; i < getCantTransiciones(); i++) {
            vectorZ[i] = transicionesConTiempo[i].testVentanaTiempo() && sensi[i];
        }
        System.out.println("---------------------- Vector z  rdp -----------------------");
        Operaciones.printB(vectorZ);
        printTimeStamp();
        return vectorZ;
    }

    public int[][] gettInvariantes() {
        return tInvariantes;
    }

    private void actualizaSensibilizadasExtendido(Boolean[] tempSensibilizadas) {
        sensibilizadasEx =  getVectorZ();

        nuevoTimeStamp(tempSensibilizadas);
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

    private void nuevoTimeStamp(Boolean[] tempSensibilizadas) {
        //todo este rompe todo
        long timeStamp = System.currentTimeMillis();
        for (int i = 0; i < transicionesConTiempo.length; i++) {
            if (sensibilizadasEx[i] && transiciones[i].isTemporizada()) {///
                if (!tempSensibilizadas[i]) {
                    transicionesConTiempo[i].nuevoTimeStamp(timeStamp);
                }
            }
        }
    }

    private void printTimeStamp() {
        for (int i = 0; i < transicionesConTiempo.length; i++) {
            System.out.printf("%d ", transicionesConTiempo[i].getTimeStamp());
        }
        System.out.println();
    }

    private ArrayList<Integer> getsoloInmediatas() {

        ArrayList<Integer> soloInmediatas = new ArrayList<>();
        for (int i = 0; i < getCantTransiciones(); i++) {
            if (!transiciones[i].isTemporizada()) {
                soloInmediatas.add(i);
            }
        }
        return soloInmediatas;
    }

}
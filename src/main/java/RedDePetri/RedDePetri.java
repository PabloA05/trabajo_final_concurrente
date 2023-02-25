package RedDePetri;

import Util.Colores;
import Util.Operaciones;

import java.util.ArrayList;
import java.util.Arrays;
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

    private Boolean[] vectorEandB;

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
        this.vectorEandB = new Boolean[getCantTransiciones()]; // todo no hace falta creo
        for (int i = 0; i < getCantTransiciones(); i++) {
            this.transiciones[i] = new Transicion("T" + i, i, transicionesConTiempo[i].esTemporal());
        }
        this.soloInmediatas = getsoloInmediatas();
        this.activoLogicaInmediata = true;
        var temp = new Boolean[getCantTransiciones()];
        Arrays.fill(temp, Boolean.FALSE);

        setVectorEandB();
        nuevoTimeStamp(temp);
        verificarPInvariantes();
    }
    public void setVectorDeEstado(int[] vectorDeEstado) {
        this.vectorDeEstado = vectorDeEstado;
    }

    public Boolean[] getVectorEandB() {
        return vectorEandB;
    }

    private void setVectorEandB() {
        vectorEandB = getvectorEandB();
    }

    public void setVectorEandB(Boolean[] vectorEandB) {
        this.vectorEandB = vectorEandB;
    }


    public long disparar(Transicion transicion) {
        long result = -999;
        long tiempoActual = System.currentTimeMillis();
        boolean esperando = transicionesConTiempo[transicion.getPosicion()].isEsperando();

        if (vectorEandB[transicion.getPosicion()]) {
            if (transicion.isTemporizada()) {
                if (checkInmediatas()) {
                    return -2;
                }
                boolean ventana = transicionesConTiempo[transicion.getPosicion()].testVentanaTiempo(tiempoActual);
                boolean antes = antesDeLaVentana(transicion.getPosicion(), tiempoActual);
                if (ventana) {
                    if (!esperando || esperando && Thread.currentThread().getId() == transicionesConTiempo[transicion.getPosicion()].getId()) {
                        result = -1;
                    } else {
                        result = -2;
                    }
                } else if (antes) {
                    if (esperando && Thread.currentThread().getId() != transicionesConTiempo[transicion.getPosicion()].getId()) {
                        result = -2;
                    } else {

                        transicionesConTiempo[transicion.getPosicion()].setEsperando();
                        result = timeToSleep(transicion, tiempoActual);
                    }
                } else {
                    return result; //despues de la ventana
                }
            } else {
                result = -1;
            }
        } else {
            result = -2;
        }
        if (result == -1) {
            if (transicion.isTemporizada()) {
                transicionesConTiempo[transicion.getPosicion()].resetEsperando();
            }
            var vectorAntes = vectorEandB.clone();
            vectorDeEstado = marcadoSiguiente(vectorDeEstado, transicion.getPosicion());
            transicion.incrementoDisparo();
            setVectorEandB();
            nuevoTimeStamp(vectorAntes);
            verificarPInvariantes();
        } else if (esperando && Thread.currentThread().getId() == transicionesConTiempo[transicion.getPosicion()].getId()) {
            transicionesConTiempo[transicion.getPosicion()].resetEsperando();
        }// todo ver si el esparando esta bien y se resetea cuando espero y no disparo
        return result;
    }

    private boolean checkInmediatas() {
        for (int i = 0; i < soloInmediatas.size(); i++) {
            if (vectorEandB[soloInmediatas.get(i)]) { //todo si esta en las colas la transicion inmediata probablemente no se despierte y se trabe el programa
                // creo que se puede arreglar eligiendo bien el hilo antes en la politica
                return true;
            }
        }
        return false;
    }

    public Boolean[] getSensibilizadas() {
        if (checkInmediatas()) {
            Boolean[] temp;
            temp = new Boolean[vectorEandB.length];
            Arrays.fill(temp, Boolean.FALSE);
            for (Integer soloInmediata : soloInmediatas) {
                if (vectorEandB[soloInmediata]) {
                    temp[soloInmediata] = Boolean.TRUE;
                }
            }
            return temp;
        } else {
            return vectorEandB;
        }
    }

    private boolean antesDeLaVentana(int posicion, long actual) {
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
                System.out.println("Error, No se cumple el invariante " + i + " de plaza");
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
        Boolean[] vectorE = new Boolean[getCantTransiciones()];
        for (int i = 0; i < getCantTransiciones(); i++) {
            vectorE[i] = esDisparoValido(marcadoSiguiente(vectorDeEstado, i));
        }
        return vectorE;
    }

    private Boolean[] getvectorEandB() {
        return Operaciones.andVector(getVectorE(), getVectorB());
    }

    private long timeToSleep(Transicion transicion, long actual) {
        return transicionesConTiempo[transicion.getPosicion()].getTimeStamp() + transicionesConTiempo[transicion.getPosicion()].getAlpha() - actual;
    }

    public int[][] gettInvariantes() {
        return tInvariantes;
    }

    private void nuevoTimeStamp(Boolean[] tempSensibilizadas) {
        long timeStamp = System.currentTimeMillis();
        for (int i = 0; i < transicionesConTiempo.length; i++) {
            if (vectorEandB[i] && transiciones[i].isTemporizada()) {
                if (!tempSensibilizadas[i]) {
                    transicionesConTiempo[i].nuevoTimeStamp(timeStamp);
                }
            }
        }
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
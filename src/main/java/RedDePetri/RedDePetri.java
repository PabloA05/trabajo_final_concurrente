package RedDePetri;

import Monitor.Monitor;
import Monitor.Operaciones;

public class RedDePetri {
    int[][] incidencia;
    // private int[][] intervalos_tiempo; //matriz de intervalos de tiempo
    final int[] mki; //marca inicial. columna. NO VARIA
    private int[] vectorDeEstado; //la marca actual
    private SensibilizadasConTiempo[] transicionesConTiempo;
    private boolean[] sensibilizadas;
    int[] mj_1;// la siguiente
    //private int[] e; //vector de transiciones sensibilizadas
    int[] ex; //vector de sensibilizado extendido
    //private int[] z; //Vector de transiciones des-sensibilizadas por tiempo
    private boolean k = false;
    private boolean antes = false;
    private boolean[] VectorSensibilazadas;
    ;

    public RedDePetri(String mji, String I) {


        //  e_semaphore = new Semaphore(1, true);//no se  si lo voy a usar

        this.incidencia = Operaciones.matriz2d(I);

        this.vectorDeEstado = Operaciones.vector(mji);
        this.mki = vectorDeEstado; //marca inicial
        this.transicionesConTiempo = new SensibilizadasConTiempo[getCantTransisiones()];

    }

    public boolean[] getSensibilizadas() {
        return sensibilizadas;
    }

    public boolean disparar(Transicion transicion) {//todo para transiciones inmediatas
        k = false;
        antes = false;
        if (estaSensibilizado(transicion.getPosicion())) {
            boolean ventana = transicionesConTiempo[transicion.getPosicion()].testVentanaTiempo();
            if (ventana) {
                if (!transicionesConTiempo[transicion.getPosicion()].isEsperando()) {
                    transicionesConTiempo[transicion.getPosicion()].setNuevoTimeStamp();
                    k = true;
                }
            } else {
                antes = antesDeLaVentana(transicion.getPosicion());
                Monitor.releaseMonitor();
                if (antes) {
                    setEsperando(transicion.getPosicion());
                    sleepThread(transicion.getPosicion());
                }
                Monitor.acquireMonitor();
            }
        }
        if (k){
            calculoDeVectorEstado(transicion);
            actualiceSensibilizadoT();
        }
        return k;
    }

    private void sleepThread(int posicion) { //todo no se si esta bien
        long sleepTime = transicionesConTiempo[posicion].getStartTime() + transicionesConTiempo[posicion].getAlpha() - System.currentTimeMillis();
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void setEsperando(int posicion) {

    }

    private boolean antesDeLaVentana(int posicion) {
        return false;
    }

    public void actualiceSensibilizadoT() {
        for (int i = 0; i < getCantTransisiones(); i++) {
            try {
                if (esDisparoValido(Operaciones.marcadoSiguiente(vectorDeEstado, i, incidencia))) {
                    sensibilizadas[i] = true;
                } else sensibilizadas[i] = false;
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Error en getSensibilizadas()");
            }
        }
    }

    public boolean estaSensibilizado(int posicion) {
        return sensibilizadas[posicion];
    }



    public int getCantTransisiones() {
        return incidencia[0].length;
    }

    public void calculoDeVectorEstado(Transicion transicion) {
        vectorDeEstado = Operaciones.marcadoSiguiente(vectorDeEstado, transicion.getPosicion(), incidencia);
    }

    public int[] getColumna() {

        return new int[0];
    }


    public int[][] getIncidencia() {
        return incidencia;
    }


    public boolean esDisparoValido(int[] marcado_siguiente) throws NullPointerException {

        if (marcado_siguiente == null) {
            throw new NullPointerException("Marcado null.");
        }
        for (int j : marcado_siguiente) {
            if (j < 0) {
                return false;
            }
        }
        return true;
    }
}

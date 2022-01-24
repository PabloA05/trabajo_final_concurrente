package RedDePetri;

import Monitor.Operaciones;

public class RedDePetri {

    int[][] incidencia;
    final int[][] inhibidor;
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
    private boolean[] VectorSensibilazadas;
    private Transicion[] transiciones;
    private int[] vectorQ;
    private int[] vectorB;

    public RedDePetri(String mji, String I, String h) {


        //  e_semaphore = new Semaphore(1, true);//no se  si lo voy a usar

        this.incidencia = Operaciones.matriz2d(I);
        this.vectorDeEstado = Operaciones.vector(mji);
        this.inhibidor = Operaciones.matriz2d(h);
        this.mki = vectorDeEstado; //marca inicial
        vectorQ = new int[vectorDeEstado.length];
        sensibilizadas = new boolean[getCantTransisiones()];
        for (int i = 0; i < getCantTransisiones(); i++) {
            sensibilizadas[i] = false;
        }
        this.transicionesConTiempo = new SensibilizadasConTiempo[getCantTransisiones()];
        transiciones = new Transicion[getCantTransisiones()];
        for (int i = 0; i < getCantTransisiones(); i++) {
            transiciones[i] = new Transicion((char) (97 + i), i, esTemporizada(i));
        }
        calcularVectorB();

        actualiceSensibilizadoT();
    }

    public boolean[] getSensibilizadas() {

        return sensibilizadas;
    }


    public boolean disparar(Transicion transicion) {//todo para transiciones inmediatas
        /*   k = true;
         *//*if (estaSensibilizado(transicion.getPosicion())) {

            transiciones[transicion.getPosicion()].incrementoDisparo();

            boolean ventana = transicionesConTiempo[transicion.getPosicion()].testVentanaTiempo();
            if (ventana) {
                if (!transicionesConTiempo[transicion.getPosicion()].isEsperando() ||
                        ( transicionesConTiempo[transicion.getPosicion()].isEsperando()
                                && (transicionesConTiempo[transicion.getPosicion()].getId()==Thread.currentThread().getId()))) {
                    setNuevoTimeStamp(); //todo no esta bien
                    k = true;
                }
            } else {
                Monitor.releaseMonitor();
                if (antesDeLaVentana(transicion.getPosicion())) {
                    transicionesConTiempo[transicion.getPosicion()].setEsperando();
                    sleepThread(transicion.getPosicion());
                }
                Monitor.acquireMonitor();
            }
        }*//*
        if (k){
            calculoDeVectorEstado(transicion);
            actualiceSensibilizadoT();
        }
        return k;*/
        k = false;
        if (estaSensibilizado(transicion.getPosicion())) {
            k = true;
        } else {
            k = false;
        }
        if (k) {
            calculoDeVectorEstado(transicion);
            actualiceSensibilizadoT();
        }
        return k;
    }

    private void setNuevoTimeStamp() {
        for (int i = 0; i < transicionesConTiempo.length; i++) {
            if (sensibilizadas[i]) {
                transicionesConTiempo[i].nuevoTimeStamp();
            }
        }
    }

    private void sleepThread(int posicion) { //todo no se si esta bien
        long sleepTime = transicionesConTiempo[posicion].getStartTime() + transicionesConTiempo[posicion].getAlpha() - System.currentTimeMillis();
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    private boolean antesDeLaVentana(int posicion) {
        return (transicionesConTiempo[posicion].getStartTime() + transicionesConTiempo[posicion].getAlpha() - System.currentTimeMillis() < 0);
    }

    public void actualiceSensibilizadoT() {
        for (int i = 0; i < getCantTransisiones(); i++) {
            try {
                sensibilizadas[i] = esDisparoValido(marcadoSiguiente(vectorDeEstado, i));
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
        vectorDeEstado = marcadoSiguiente(vectorDeEstado, transicion.getPosicion());

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
        System.out.print("vectorB\n");
        calcularVectorB();

        Operaciones.printVector(vectorB);
        int[]ex=Operaciones.multiplyWithForLoops(incidencia,vectorB);
        int[] temp = new int[old.length];
        for (int i = 0; i < temp.length; i++) {
            temp[i] = old[i] + ex[i];
        }
        calcularVectorB();
        Operaciones.printVector(temp);
        System.out.print("salio<<<<<<<<<<<<<<<<<<<<<<<<<<<\n");


        return temp;
    }

    private void calcularVectorB() {

        for (int i = 0; i < vectorQ.length; i++) {
            if (vectorDeEstado[i] != 0) {
                vectorQ[i] = 0;
            } else {
                vectorQ[i] = 1;
            }
        }
        vectorB = Operaciones.multiplyWithForLoops(inhibidor, vectorQ);
    }
}
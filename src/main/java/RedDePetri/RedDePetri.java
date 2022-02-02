package RedDePetri;

import Monitor.Operaciones;

import java.text.SimpleDateFormat;
import java.util.Date;

public class RedDePetri {

    int[][] incidencia;
    final int[][] inhibidor;
    // private int[][] intervalos_tiempo; //matriz de intervalos de tiempo
    final int[] mki; //marca inicial. columna. NO VARIA
    private int[] vectorDeEstado; //la marca actual
    private SensibilizadasConTiempo[] transicionesConTiempo;
    private Boolean[] sensibilizadas;
    int[] mj_1;// la siguiente
    //private int[] e; //vector de transiciones sensibilizadas
    int[] ex; //vector de sensibilizado extendido
    //private int[] z; //Vector de transiciones des-sensibilizadas por tiempo
    private boolean k = false;
    private boolean[] VectorSensibilazadas;
    private Transicion[] transiciones;
    private Boolean[] sensibilizadasEx;


    public RedDePetri(String mji, String I, String h) {


        //  e_semaphore = new Semaphore(1, true);//no se  si lo voy a usar

        this.incidencia = Operaciones.matriz2d(I);
        this.vectorDeEstado = Operaciones.vector(mji);
        this.inhibidor = Operaciones.matriz2d(h);
        this.mki = vectorDeEstado; //marca inicial
        sensibilizadas = new Boolean[getCantTransisiones()];
        for (int i = 0; i < getCantTransisiones(); i++) {
            sensibilizadas[i] = false;
        }
        sensibilizadasEx = new Boolean[getCantTransisiones()];
        for (int i = 0; i < getCantTransisiones(); i++) {
            sensibilizadasEx[i] = false;
        }
        this.transicionesConTiempo = new SensibilizadasConTiempo[getCantTransisiones()];
        transiciones = new Transicion[getCantTransisiones()];
        for (int i = 0; i < getCantTransisiones(); i++) {
            transiciones[i] = new Transicion((char) (97 + i), i, esTemporizada(i));
        }


        //actualiceSensibilizadoT();
    }

    public Boolean[] getVectorE() {
        for (int i = 0; i < getCantTransisiones(); i++) {
            try {
                sensibilizadas[i] = esDisparoValido(marcadoSiguiente(vectorDeEstado, i));
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Error en getSensibilizadas()");
            }
        }
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
        if (getSensibilizadasExtendido()[transicion.getPosicion()]) {
            k = true;
        } else {
            k = false;
        }
        if (k) {
            calculoDeVectorEstado(transicion);


            sincronizar(transicion);
            transicion.incrementoDisparo();
            //actualiceSensibilizadoT();
        }
        return k;
    }

    private void sincronizar(Transicion t) {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();
        System.out.println("La transicion: "+(t.getPosicion()+1)+" en el tiempo: "+System.currentTimeMillis()/1000);
        Operaciones.printVector(vectorDeEstado);

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



    public boolean estaSensibilizado(int posicion) {
        return sensibilizadasEx[posicion];
    }

    public int[] getVectorDeEstado(){
        return vectorDeEstado;
    }

    public int getCantTransisiones() {
        return incidencia[0].length;
    }

    public int getCantPlazas(){
        return incidencia.length;
    }

    public int[][] getInhibidor(){
        return inhibidor;
    }

    public void calculoDeVectorEstado( Transicion transicion) {
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
        int[] temp = new int[old.length];
        for (int i = 0; i < temp.length; i++) {
            temp[i] = old[i] + incidencia[i][position];
        }
        //Operaciones.printVector(temp);
        //System.out.print("salio<<<<<<<<<<<<<<<<<<<<<<<<<<<\n");


        return temp;
    }


    public Boolean[] getVectorQ(){
        Boolean[] vectorQ = new Boolean[getVectorDeEstado().length];

        for(int i=0; i<getCantPlazas(); i++){
            vectorQ[i] = vectorDeEstado[i] != 0;
        }
        return vectorQ;
    }

    public Boolean[] getVectorB(){

        Boolean[] vectorB = new Boolean[getCantTransisiones()];
        int[][] inhibidorTranspuesta = Operaciones.transpuesta(inhibidor);
        vectorB = Operaciones.productoMatrizVectorBoolean(inhibidorTranspuesta,getVectorQ());
        for(int i=0; i<vectorB.length;i++){
            vectorB[i]=!vectorB[i];
        }
        return vectorB;

    }

    public Boolean[] getSensibilizadasExtendido(){
        sensibilizadasEx = Operaciones.andVector(getVectorB(), getVectorE());
        return sensibilizadasEx;

    }

}
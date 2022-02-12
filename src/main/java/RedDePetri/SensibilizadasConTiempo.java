package RedDePetri;


public class SensibilizadasConTiempo {
    final private long alpha;
    final private long beta;
    private long id;
    //   private boolean flag;// todo id y flag hay que implementarlo
    private long startTime;
    private boolean esperando;

    SensibilizadasConTiempo(int alpha, int beta) {
        this.alpha = alpha;
        this.beta = beta;
        //  this.flag = false;
        this.startTime = -1;
        this.id = -999999;
        this.esperando = false;
    }

    public boolean testVentanaTiempo() {
        long ahora = System.currentTimeMillis();
        return ((ahora - startTime) >= alpha) && ((ahora - startTime) < beta);

    }

    public void nuevoTimeStamp() {



        this.startTime = System.currentTimeMillis();
        this.id = Thread.currentThread().getId();
    }

    public boolean isEsperando() {
        return esperando;
    }

    public void setEsperando() {
        this.esperando = true;
        id = Thread.currentThread().getId();
    }

    public long getAlpha() {
        return alpha;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getId() {
        return id;
    }

    public boolean esInmediata() {
        return !(alpha < 0 && beta < 0);
    }

    public void resetTimestamp() {
        this.startTime = -1;
        this.id = -999999;
    }

    public void setNuevoTimeStamp() {



    }
}

package RedDePetri;


import java.util.concurrent.atomic.AtomicBoolean;

public class SensibilizadasConTiempo {
    private long alpha;
    private long beta;
    private long id;
    //   private boolean flag;// todo id y flag hay que implementarlo
    private long startTime;
    //  private boolean esperando;
    private AtomicBoolean esperando;

    public long getBeta() {
        return beta;
    }

    SensibilizadasConTiempo(long alpha, long beta) {
        this.alpha = alpha;
        this.beta = beta;
        //  this.flag = false;
        this.startTime = -1;
        this.id = -999999;
        this.esperando = new AtomicBoolean(false);
    }

//    public boolean testVentanaTiempo() {
//        long ahora = System.currentTimeMillis();
//        return ((ahora - this.startTime) >= this.alpha) && ((ahora - this.startTime) < this.beta &&
//                !this.esperando.get() || this.esperando.get() && Thread.currentThread().getId() == this.id);
//
//    }

    public boolean testVentanaTiempo() {
        long ahora = System.currentTimeMillis();
        return ((ahora - startTime) >= alpha) && ((ahora - startTime) < beta);

    }

    public void nuevoTimeStamp() {
        this.startTime = System.currentTimeMillis();
        this.id = Thread.currentThread().getId();
    }

    public boolean isEsperando() {
        return esperando.get();
    }

    public void setEsperando() {
        if (this.esperando.get()) {
            System.out.println("esperando error");
            System.exit(1);
        }
        this.esperando.set(true);
        this.id = Thread.currentThread().getId();
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
}

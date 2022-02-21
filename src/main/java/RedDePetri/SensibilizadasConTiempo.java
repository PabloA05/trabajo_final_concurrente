package RedDePetri;


import java.util.concurrent.atomic.AtomicBoolean;

public class SensibilizadasConTiempo {
    final private long alpha;
    final private long beta;
    private long id;
    private long timeStamp;
    private AtomicBoolean esperando;

    public long getBeta() {
        return beta;
    }

    public void setId(long id) {
        this.id = id;
    }

    SensibilizadasConTiempo(long alpha, long beta) {
        this.alpha = alpha;
        this.beta = beta;
        this.timeStamp = -1;
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
       // System.out.printf("test de ventana:%b %s\n", ((ahora - timeStamp) >= alpha) && ((ahora - timeStamp) < beta), Thread.currentThread().getName());
        return ((ahora - timeStamp) >= alpha) && ((ahora - timeStamp) < beta);
    }

    public void nuevoTimeStamp() {
        this.timeStamp = System.currentTimeMillis();
        this.id = -999999;
        this.esperando.set(false);
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
    }

    public long getAlpha() {
        return alpha;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public long getId() {
        return id;
    }

    public boolean esTemporal() {
        return !(alpha < 0 && beta < 0);
    }

    public void resetTimestamp() {
        this.timeStamp = -1;
        this.id = -999999;
        this.esperando.set(false);
    }
}

package RedDePetri;


import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class SensibilizadasConTiempo {
    final private long alpha;
    final private long beta;
    private AtomicLong id;
    private long timeStamp;
    private AtomicBoolean esperando;

    public long getBeta() {
        return beta;
    }


    SensibilizadasConTiempo(long alpha, long beta) {
        this.alpha = alpha;
        this.beta = beta;
        this.timeStamp = -1;
        this.id = new AtomicLong(-999999);
        this.esperando = new AtomicBoolean(false);
    }

    public void setId(long id) {
        this.id.set(id);
    }


    public boolean testVentanaTiempo() {
        long ahora = System.currentTimeMillis();
        return ((ahora - timeStamp) >= alpha) && ((ahora - timeStamp) < beta);
    }

    public void nuevoTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
        this.id.set(-999999);
        this.esperando.set(false); //todo probablemente este demas
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
        return id.get();
    }

    public boolean esTemporal() {
        return !(alpha < 0 && beta < 0);
    }

    public void resetEsperando() {
        this.esperando.set(false);
    }

    public void resetTimestamp() {
        this.timeStamp = -1;
        this.id.set(-999999);

    }
}

package RedDePetri;
import Util.Colores;

public class SensibilizadasConTiempo {
    final private long alpha;
    final private long beta;
    private long id;
    private long timeStamp;
    private boolean esperando;

    public SensibilizadasConTiempo(long alpha, long beta) {
        this.alpha = alpha;
        this.beta = beta;
        this.timeStamp = -1;
        this.id = -999999;
        this.esperando = false;
    }

    public boolean testVentanaTiempo() {
        long ahora = System.currentTimeMillis();
        return ((ahora - timeStamp) >= alpha) && ((ahora - timeStamp) < beta);
    }

    public void nuevoTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
        this.id = -999999;
        this.esperando = false;
    }

    public boolean isEsperando() {
        return esperando;
    }

    public void setEsperando() {
        if (this.esperando) {
            System.out.printf(Colores.ANSI_RED + "esperando error h:%s id_H:%s esp:%b id:%d>>\n", Thread.currentThread().getName(), Thread.currentThread().getId(), esperando, id);
            System.exit(1);
        }
        this.esperando = true;
        this.id = Thread.currentThread().getId();
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
    }

    public void resetEsperando() {
        this.id = -999999;
        this.esperando = false;
    }
}

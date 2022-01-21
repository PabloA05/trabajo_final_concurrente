package Monitor;
import java.util.concurrent.locks.Lock;


public class Colas {

    private int hilosEnCola;

    public Colas() {
        this.hilosEnCola = 0;
    }

    public synchronized void acquire() {
        hilosEnCola++;
        try {
            System.out.print("Hilo: "+Thread.currentThread().getId()+" entro cola\n");
            wait(); //El hilo entra a la cola, sumando la cantidad de hilos en cola
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            hilosEnCola--; //Cuando sale, resta la cantidad de hilos
        }
    }

    public  void release() {
        System.out.print("sale\n");
        notify();
        hilosEnCola--;
    }

    public boolean isEmpty() {
        return hilosEnCola == 0;
    }


}
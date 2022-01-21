package Monitor;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class Colas {

    private int hilosEnCola;
    Lock lock;

    public Colas() {
        this.hilosEnCola = 0;
        lock = new ReentrantLock();
    }

    public synchronized void acquire() {
        lock.lock();
        hilosEnCola++;
        lock.unlock();
        try {
            // System.out.print("Hilo: "+Thread.currentThread().getId()+" entro cola\n");
            wait(); //El hilo entra a la cola, sumando la cantidad de hilos en cola
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            hilosEnCola--; //Cuando sale, resta la cantidad de hilos
        }
    }

    public synchronized void release() {
        System.out.print("sale\n");
        notify();
        lock.lock();
        hilosEnCola--;
        lock.unlock();
    }

    public boolean isEmpty() {
        return hilosEnCola == 0;
    }


}
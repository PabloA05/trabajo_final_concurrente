package Monitor;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class Colas {

    private int hilosEnCola;
    private final ReentrantReadWriteLock lock;

    public Colas() {
        this.hilosEnCola = 0;
        lock = new ReentrantReadWriteLock(true);
    }

    public synchronized void acquire() {
        lock.writeLock();
        hilosEnCola++;
        lock.writeLock().unlock();
        try {
            // System.out.print("Hilo: "+Thread.currentThread().getId()+" entro cola\n");
            wait(); //El hilo entra a la cola, sumando la cantidad de hilos en cola
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.writeLock();
            hilosEnCola--; //Cuando sale, resta la cantidad de hilos
            lock.writeLock().unlock();
        }
    }

    public synchronized void release() {
        System.out.print("salio de cola\n");
        notify();
        lock.writeLock();
        hilosEnCola--;
        lock.writeLock().unlock();
    }

    public boolean isEmpty() {
        lock.readLock();
        boolean temp = hilosEnCola == 0;
        lock.readLock().unlock();
        return temp;
    }


}
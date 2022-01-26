package Monitor;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class Colas {

    private int hilosEnCola;
    private ReadWriteLock rwLock;
    Lock writeLock;
    Lock readLock;

    public Colas() {
        this.hilosEnCola = 0;
        this.rwLock = new ReentrantReadWriteLock(true);
        this.writeLock = rwLock.writeLock();
        this.readLock = rwLock.readLock();
    }

    public synchronized void acquire() { //todo fijarse si hacen falta los locks y synchronized
        writeLock.lock();
        try {
            hilosEnCola++;
        } finally {
            writeLock.unlock();
        }
        try {
            // System.out.print("Hilo: "+Thread.currentThread().getId()+" entro cola\n");
            wait(); //El hilo entra a la cola, sumando la cantidad de hilos en cola
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            writeLock.lock();
            try {
                hilosEnCola--;
            } finally {
                writeLock.unlock();
            }
        }
    }

    public void release() {
        System.out.print("salio de cola\n");
        notify();
        writeLock.lock();
        try {
            hilosEnCola--;
        } finally {
            writeLock.unlock();
        }
    }


    public boolean isEmpty() {
        readLock.lock();
        try {
            return (hilosEnCola == 0);
        } finally {
            readLock.unlock();
        }
    }
}


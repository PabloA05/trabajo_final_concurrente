package Monitor;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Colas {
    private AtomicInteger hilosCola;
    private int hilosEnCola;
    private ReadWriteLock rwLock;
    Lock writeLock;
    Lock readLock;

    public Colas() {
        this.hilosEnCola = 0;
        this.rwLock = new ReentrantReadWriteLock(true);
        this.writeLock = rwLock.writeLock();
        this.readLock = rwLock.readLock();
        hilosCola = new AtomicInteger(0);

    }

    public synchronized void acquire() { //todo fijarse si hacen falta los locks y synchronized
        int a = hilosCola.incrementAndGet();
        System.out.println("cola en hilos:" + a +" "+ Thread.currentThread().getName());
        try {
            // System.out.print("Hilo: "+Thread.currentThread().getId()+" entro cola\n");
            wait(); //El hilo entra a la cola, sumando la cantidad de hilos en cola
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public void release() {
        try {
            notify();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        System.out.println("salio " + hilosCola.decrementAndGet());
    }


    public boolean isEmpty() {

        return (hilosCola.intValue() == 0);

    }
}


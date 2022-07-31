package Monitor;

import Util.Colores;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class Cola {
    private int hilosCola;
    private ReadWriteLock rwLock;
    private Lock writeLock;
    private Lock readLock;


    public Cola() {
        this.hilosCola = 0;
        this.rwLock = new ReentrantReadWriteLock(true);
        this.writeLock = rwLock.writeLock();
        this.readLock = rwLock.readLock();

    }

    public void acquire() {

        increment();

        if (get() < 0) {
            System.out.printf("hilosCola [%d] menor que cero", get());
            System.exit(1);
        }

        synchronized (this) {
            try {
                wait(); //El hilo entra a la cola, sumando la cantidad de hilos en cola
            } catch (InterruptedException e) {
                e.printStackTrace();
                System.out.println("hilo en cola");
                System.exit(1);
            }
        }
    }

    private void increment() {
        writeLock.lock();
        hilosCola++;
        writeLock.unlock();
    }

    private void decrement() {
        writeLock.lock();
        hilosCola--;
        writeLock.unlock();
    }

    public void release() {
        if (get() < 0) {
            System.out.printf("hilosCola [%d] menor que cero\n", get());
            System.exit(1);
        }
        synchronized (this) {
            try {
                notify();
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }

        }
        decrement();
        System.out.printf("entro notify %s\n", Thread.currentThread().getName());

        System.out.printf(Colores.ANSI_PURPLE + "%s hilos en cola:%d\n" + Colores.ANSI_RESET, Thread.currentThread().getName(), hilosCola);

        System.out.printf("salio de cola %s\n", Thread.currentThread().getName());
    }

    public int get() {
        try {
            readLock.lock();
            return hilosCola;

        } finally {
            readLock.unlock();
        }
    }


    public boolean isEmpty() {

        return (get() == 0);

    }
}
package Monitor;

import Util.Colores;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


public class Colas {
    private int hilosCola;

    public Colas() {
        this.hilosCola = 0;
    }

    public synchronized void acquire() {

        if (hilosCola < 0) {
            System.out.printf("hilosCola [%d] menor que cero", hilosCola);
            System.exit(1);
        }

        try {
            increment();
            Monitor.releaseMonitor();
            wait(); //El hilo entra a la cola, sumando la cantidad de hilos en cola
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("hilo en cola");
            System.exit(1);
        } finally {
            decrement();
        }
    }

    private void increment() {
        hilosCola++;
    }

    private void decrement() {
        hilosCola--;
    }

    public synchronized void release() {
        //   System.out.printf("entro notify %s\n", Thread.currentThread().getName());
        try {
            notify();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        //System.out.printf(ANSI_PURPLE + "%s hilos en cola:%d\n" + ANSI_RESET, Thread.currentThread().getName(), hilosCola);
        //System.out.printf("salio %d - %s\n", a, Thread.currentThread().getName());
    }

    public int get() {
        return hilosCola;
    }
}
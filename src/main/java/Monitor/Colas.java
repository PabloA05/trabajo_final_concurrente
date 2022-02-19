package Monitor;

import RedDePetri.Transicion;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Colas {
    private int hilosCola;
    private int hilosEnCola;
    private ReadWriteLock rwLock;
    Lock writeLock;
    Lock readLock;
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    public Colas() {
        this.hilosEnCola = 0;
        this.rwLock = new ReentrantReadWriteLock(true);
        this.writeLock = rwLock.writeLock();
        this.readLock = rwLock.readLock();

    }

    public synchronized void acquire() { //todo fijarse si hacen falta los locks y synchronized

        increment();
        // System.out.println(ANSI_CYAN+"elementos en cola :" + a + " " + Thread.currentThread().getName()+ ANSI_RESET );
//        if (a < 0 || a>4) {
//            System.out.printf(ANSI_CYAN+"Valor > %d < de INT mal! %s >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n"+ANSI_RESET ,a , Thread.currentThread().getName());
//            System.exit(1);
//        }
        try {
            System.out.print(ANSI_CYAN + Thread.currentThread().getName() + " cola cant: " + hilosCola + "\n" + ANSI_RESET);

            Monitor.releaseMonitor();
            wait(); //El hilo entra a la cola, sumando la cantidad de hilos en cola
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.out.println("hilo en cola");
            System.exit(1);
        }
        finally {
            decrement();
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

    public synchronized void release() {
        //   System.out.printf("entro notify %s\n", Thread.currentThread().getName());
        try {
            notify();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        System.out.printf(ANSI_PURPLE + "%s hilos en cola:%d\n" + ANSI_RESET, Thread.currentThread().getName(), hilosEnCola);

        //System.out.printf("salio %d - %s\n", a, Thread.currentThread().getName());
    }

    private int get() {
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
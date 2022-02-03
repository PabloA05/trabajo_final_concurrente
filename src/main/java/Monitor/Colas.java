package Monitor;

import RedDePetri.Transicion;

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

    public Transicion transicion;
    public Colas() {
        this.hilosEnCola = 0;
        this.rwLock = new ReentrantReadWriteLock(true);
        this.writeLock = rwLock.writeLock();
        this.readLock = rwLock.readLock();
        hilosCola = new AtomicInteger(0);
    }

    public synchronized Transicion acquire() { //todo fijarse si hacen falta los locks y synchronized
        int a = hilosCola.incrementAndGet();
      //  System.out.println("elementos en cola :" + a + " " + Thread.currentThread().getName() );
        if (a < 0 || a>1) {
            System.out.printf("%d -- Valor de INT mal! %s >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n" ,a , Thread.currentThread().getName());
        }
        try {
            // System.out.print("Hilo: "+Thread.currentThread().getId()+" entro cola\n");

            wait(); //El hilo entra a la cola, sumando la cantidad de hilos en cola
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return transicion;
    }

    public synchronized void release() {
     //   System.out.printf("entro notify %s\n", Thread.currentThread().getName());
        try {
            notify();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        int a = hilosCola.decrementAndGet();
        //System.out.printf("salio %d - %s\n", a, Thread.currentThread().getName());
    }


    public boolean isEmpty() {

        return (hilosCola.intValue() == 0);

    }
}


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
        this.hilosEnCola++;
        try{
//			System.out.println("Llgueeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee");
            super.wait(); //El hilo entra a la cola, sumando la cantidad de hilos en cola
        }catch(InterruptedException e){
            //e.printStackTrace();
        }
        this.hilosEnCola--; //Cuando sale, resta la cantidad de hilos
    }

    public synchronized void release() {
        super.notify();
    }

    public boolean isEmpty() {
        return hilosEnCola == 0;
    }


}

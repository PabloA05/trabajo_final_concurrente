package Monitor;

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
        }
    }

    private void increment() {
        hilosCola++;
    }

    private void decrement() {
        hilosCola--;
    }

    public synchronized void release() {
        try {
            notify();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            decrement();
        }
    }

    public int get() {
        return hilosCola;
    }
}
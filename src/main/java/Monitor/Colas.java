package Monitor;


public class Colas {

    private int hilos_en_cola;

    public Colas(){
        this.hilos_en_cola=0;
    }

    public void acquire() {
        try {
            super.wait();
            hilos_en_cola++;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void release(){
        super.notify();
        hilos_en_cola--;
    }

    public boolean isEmpty(){
        return hilos_en_cola == 0;
    }



}

package Monitor;


public class Colas {

    private int hilos_en_cola;

    public Colas(){
        this.hilos_en_cola=0;
    }
    public void acquire() {
        //todo hacer
    }

    public void release(){
        //todo hacer
    }

    public boolean isEmpty(){
        return hilos_en_cola == 0;
    }
}

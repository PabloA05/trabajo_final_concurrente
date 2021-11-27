package Monitor;


public class Colas {

    private int hilosEnCola;

    public Colas(){
        this.hilosEnCola =0;
    }

    public void acquire() {
        hilosEnCola++;
        try{
            wait(); //El hilo entra a la cola, sumando la cantidad de hilos en cola
        }catch(InterruptedException e){
            e.printStackTrace();
        }
        finally {
            hilosEnCola--; //Cuando sale, resta la cantidad de hilos
        }
    }

    public void release(){
        notify();
        hilosEnCola--;
    }

    public boolean isEmpty(){
        return hilosEnCola == 0;
    }



}

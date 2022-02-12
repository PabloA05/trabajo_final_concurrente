package RedDePetri;

public class Transicion {
    private char id;//todo pasar a string?
    private boolean temporizada;
    private int posicion;
    private int cantidadDisparada;

    public Transicion(char id, int posicion, boolean temporizada){
        this.id = id;
        this.posicion = posicion;
        this.temporizada = temporizada;
        cantidadDisparada = 0;
    }
//return (!(a == 0 || a == 6)) ;
    public char getId (){
        return id;
    }

    public boolean isTemportizada(){
        return temporizada;
    }

    public int getPosicion(){
        return posicion;
    }

    public int getCantidadDisparada() {return cantidadDisparada;}

    public void incrementoDisparo(){
        cantidadDisparada++;
    }
}

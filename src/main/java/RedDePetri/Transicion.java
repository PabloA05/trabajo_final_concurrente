package RedDePetri;

public class Transicion {
    private char id;
    private boolean temporizada;
    private int posicion;

    public Transicion(char id, int posicion, boolean temporizada){ //todo controlar que no sea nulo
        this.id = id;
        this.posicion = posicion;
        this.temporizada = temporizada;
    }

    public char getId (){
        return id;
    }

    public boolean getTemportizada(){
        return temporizada;
    }

    public int getPosicion(){
        return posicion;
    }

}

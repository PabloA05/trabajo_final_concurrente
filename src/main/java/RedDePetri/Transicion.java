package RedDePetri;

public class Transicion {
    private String id;
    private boolean temporizada;
    private int posicion;
    private int cantidadDisparada;

    public Transicion(String id, int posicion, boolean temporizada) {
        this.id = id;
        this.posicion = posicion;
        this.temporizada = temporizada;
        cantidadDisparada = 0;
    }

    public String getId() {
        return id;
    }

    public boolean isTemporizada() {
        return temporizada;
    }

    public int getPosicion() {
        return posicion;
    }

    public int getCantidadDisparada() {
        return cantidadDisparada;
    }

    public void incrementoDisparo() {
        cantidadDisparada++;
    }
}

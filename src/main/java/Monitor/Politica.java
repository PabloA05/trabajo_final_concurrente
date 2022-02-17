package Monitor;

import RedDePetri.Transicion;
import RedDePetri.RedDePetri;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;

public class Politica {

    private int modo;

    class Temp {

        int posicion;
        int cantidad;

        int getCantidad(){
            return cantidad;
        }

    }
    Temp[] temp = new Temp[3];

    //modo = 0 → Dispara aleatoriamente
    //modo = 1 → Dispara la transición menos disparada
    //modo = 2 → Dispara la menos disparada del invariante menos disparado

    public Politica(int modo) {
        this.modo = modo;
        temp[0] = new Temp();
        temp[1] = new Temp();
        temp[2] = new Temp();
    }

    public Transicion cualDisparo(Boolean[] m, RedDePetri rdp) {

        Transicion[] transiciones = rdp.getTransiciones().clone();
        int[][] tInvariantes = rdp.gettInvariantes().clone();

        if(modo==2){

            temp[0].cantidad = transiciones[3].getCantidadDisparada();
            temp[1].cantidad = transiciones[4].getCantidadDisparada();
            temp[2].cantidad = transiciones[9].getCantidadDisparada();

            for (int i = 0; i < tInvariantes.length; i++) {
                temp[i].posicion = i;
            }
            Arrays.sort(temp, Comparator.comparingInt(Temp::getCantidad));

            Arrays.sort(transiciones, Comparator.comparingInt(Transicion::getCantidadDisparada));
            HashMap<Integer,int[]> tInvariantemap = new HashMap<Integer,int[]>();

            for (int i = 0; i < tInvariantes.length; i++) {
                tInvariantemap.put(i,tInvariantes[i]);
                //balance.put(aux[i],i);
            }
            for (int k = 0; k < tInvariantes.length; k++) {
                for (int i = 0; i < transiciones.length; i++) {
                    int[] temp1 = tInvariantemap.get(temp[k].posicion);
                    if (m[transiciones[i].getPosicion()] && temp1[transiciones[i].getPosicion()] == 1) {
                        return transiciones[i];
                    }
                }
            }
        }

        if (modo==1) {
            Arrays.sort(transiciones, Comparator.comparingInt(Transicion::getCantidadDisparada));
            for (Transicion transicion : transiciones) {
                if (m[transicion.getPosicion()]) {
                    System.out.println("La politica decide por: T"+(transicion.getPosicion()+1));
                    return transicion;
                }
            }
        }

        // Política desactivada o modo distinto de 1 y 2.
        int random;
        ArrayList<Integer> list = new ArrayList<Integer>();
        for (int i = 0; i < m.length; i++) {
            if (m[i]) {
                list.add(i);
            }
        }
        random = (int) (Math.random() * (list.size()));
        return transiciones[list.get(random)];

    }

    public void cambiarPolitica(int modo) {
        this.modo = modo;
    }

}
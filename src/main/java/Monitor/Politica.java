package Monitor;

import RedDePetri.Transicion;
import RedDePetri.RedDePetri;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class Politica {

    private boolean politica;

    //tipo = false → Dispara aleatoriamente
    //tipo = true → Dispara la menos disparada

    public Politica(boolean politica) {
        this.politica = politica;
    }

    public Transicion cualDisparo(Boolean[] m, RedDePetri rdp) {

        Transicion[] transiciones = rdp.getTransiciones().clone();
        Arrays.sort(transiciones, Comparator.comparingInt(Transicion::getCantidadDisparada));
//        for (int i = 0; i < m.length; i++) {
//            System.out.printf(" vector m %d %b | ", i, m[i]);
//        }
//        System.out.println();

//        for (int i = 0; i < transiciones.length; i++) {
//            System.out.printf("transisiones %d %d \n",transiciones[i].getPosicion(), transiciones[i].getCantidadDisparada());
//        }
        if (politica) {
            // System.out.println("Entro a polica true");
            for (Transicion transicion : transiciones) {
                if (m[transicion.getPosicion()]) {
           //         System.out.println("La politica decide por: T"+(transicion.getPosicion()+1));
                    return transicion;
                }
            }
        }
        // System.out.println("Entro a polica false");
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

    public void cambiarPolitica(boolean politica) {
        this.politica = politica;
    }

}
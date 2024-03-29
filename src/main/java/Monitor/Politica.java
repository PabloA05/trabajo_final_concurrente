package Monitor;

import RedDePetri.Transicion;
import RedDePetri.RedDePetri;
import Util.Colores;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class Politica {
    private int modo;

    class InvariantesMap {
        private int posicion;
        private int cantidad;

        public int getCantidad() {
            return cantidad;
        }
    }

    private InvariantesMap[] invariantesMap = new InvariantesMap[3];

    //modo = 0 → Dispara aleatoriamente
    //modo = 1 → Dispara la transición menos disparada
    //modo = 2 → Dispara la menos disparada del invariante menos disparado

    public Politica(int modo) {
        this.modo = modo;
        invariantesMap[0] = new InvariantesMap();
        invariantesMap[1] = new InvariantesMap();
        invariantesMap[2] = new InvariantesMap();
    }

    public Transicion cualDisparo(Boolean[] m, RedDePetri rdp) {

        Transicion[] transiciones = rdp.getTransiciones().clone();
        int[][] tInvariantes = rdp.gettInvariantes().clone();

        if (modo == 2) {
            Arrays.sort(transiciones, Comparator.comparingInt(Transicion::getCantidadDisparada));

            for (int i = 0; i < transiciones.length; i++) {
                if (transiciones[i].getPosicion() == 3) {
                    invariantesMap[0].cantidad = transiciones[i].getCantidadDisparada();

                } else if (transiciones[i].getPosicion() == 4) {
                    invariantesMap[1].cantidad = transiciones[i].getCantidadDisparada();
                } else if (transiciones[i].getPosicion() == 9) {
                    invariantesMap[2].cantidad = transiciones[i].getCantidadDisparada();
                }
            }
            
            for (int i = 0; i < tInvariantes.length; i++) {
                invariantesMap[i].posicion = i;
            }
            Arrays.sort(invariantesMap, Comparator.comparingInt(InvariantesMap::getCantidad));

            for (int k = 0; k < tInvariantes.length; k++) {
                int[] invarianteMenosDisparado = tInvariantes[invariantesMap[k].posicion];
                for (int i = 0; i < transiciones.length; i++) {
                    if (m[transiciones[i].getPosicion()] && invarianteMenosDisparado[transiciones[i].getPosicion()] == 1) {
                        return transiciones[i];
                    }
                }
            }
        }

        if (modo == 1) {
            for (Transicion transicion : transiciones) {
                if (m[transicion.getPosicion()]) {
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


    private void cambiarPolitica(int modo) {
        this.modo = modo;
    }

}

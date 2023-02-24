package Monitor;

import RedDePetri.Transicion;
import RedDePetri.RedDePetri;

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
        boolean flagInmediatas = false;

        for (int i = 0; i < transiciones.length; i++) {
            if (m[i] && (!transiciones[i].isTemporizada())) {
                flagInmediatas = true;
                break;
            }
        }
        Arrays.sort(transiciones, Comparator.comparingInt(Transicion::getCantidadDisparada));

        if (modo == 2) {
            invariantesMap[0].cantidad = transiciones[3].getCantidadDisparada();
            invariantesMap[1].cantidad = transiciones[4].getCantidadDisparada();
            invariantesMap[2].cantidad = transiciones[9].getCantidadDisparada();

            for (int i = 0; i < tInvariantes.length; i++) {
                invariantesMap[i].posicion = i;
            }
            Arrays.sort(invariantesMap, Comparator.comparingInt(InvariantesMap::getCantidad));

            for (int k = 0; k < tInvariantes.length; k++) {
                int[] invarianteMenosDisparado = tInvariantes[invariantesMap[k].posicion];
                for (int i = 0; i < transiciones.length; i++) {
                    if (m[transiciones[i].getPosicion()] && invarianteMenosDisparado[transiciones[i].getPosicion()] == 1) {
                        if (flagInmediatas && !transiciones[i].isTemporizada()) {
                            return transiciones[i];
                        } else if (!flagInmediatas) {
                            return transiciones[i];
                        }
                    }
                }
            }
        }

        if (modo == 1) {
            for (Transicion transicion : transiciones) {
                if (m[transicion.getPosicion()]) {
                    if (flagInmediatas && !transicion.isTemporizada()) {
                        return transicion;
                    } else if (!flagInmediatas) {
                        return transicion;
                    }
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
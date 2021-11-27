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

    public Politica(){
        politica =true;
    }

    public Transicion cualDisparo(boolean[] m, RedDePetri rdp){

        Transicion[] transiciones = rdp.getTransiciones();
        Arrays.sort(transiciones, Comparator.comparingInt(Transicion::getCantidadDisparada));

        if(politica){
            for (Transicion transicion : transiciones) {
                if (m[transicion.getPosicion()]) {
                    return transicion;
                }
            }
        }

        int random;
        ArrayList<Integer> list = new ArrayList<Integer>();
        for(int i=0;i< m.length;i++){
            if(m[i]){
                list.add(i);
            }
        }
        random =(int) (Math.random() * (list.size()));
        return transiciones[list.get(random)];

    }

    public void cambiarPolitica(boolean politica){
        this.politica =politica;
    }

}

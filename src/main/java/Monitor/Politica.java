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
        for(int i=0;i< transiciones.length;i++){
            //System.out.println("La transicion: "+transiciones[i].getPosicion()+" tiene: "+transiciones[i].getCantidadDisparada()+" disparos "+Thread.currentThread().getName() );
        }
        System.out.println("\n");
        Arrays.sort(transiciones, Comparator.comparingInt(Transicion::getCantidadDisparada));
        for(int i=0;i< transiciones.length;i++){
       //     System.out.println("La transicion: "+transiciones[i].getPosicion()+" tiene: "+transiciones[i].getCantidadDisparada()+" disparos "+Thread.currentThread().getName() );
        }
        System.out.println("\n");

        for (int i = 0; i < m.length; i++) {
          //  System.out.printf("%b\n",m[i]);

        }
        if (politica) {

            for (int i = 0; i < rdp.getCantTransisiones(); i++) {
                if (m[transiciones[i].getPosicion()]) {
                 //   System.out.printf("*******************************retorna %d\n", transiciones[i].getPosicion());
                    return transiciones[i];
                }
            }

        }



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
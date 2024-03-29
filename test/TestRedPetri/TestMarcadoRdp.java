package TestRedPetri;

import Util.Operaciones;
import RedDePetri.Transicion;
import RedDePetri.RedDePetri;
import Util.Log;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class TestMarcadoRdp {
    static RedDePetri redDePetri;
    static Transicion[] arrTransiciones;
    static int[][] stateMarks;
    static String mji = "src/main/resources/inicial.csv";
    static String I = "src/main/resources/incidencia.csv";
    static String H = "src/main/resources/inhibidor.csv";
    static String B = "src/main/resources/bincidencia.csv";
    static String F = "src/main/resources/fincidencia.csv";
    static String S = "src/main/resources/estadosPosibles.csv"; //no sirve
    static String tiempos = "src/main/resources/tiempos.csv";
    static String T = "src/main/resources/tInvariantes.csv";
    static String Pinv = "src/main/resources/pInvariantes.csv";
    int[] fire = {0, 1, 6, 0, 7, 3, 5, 1, 6, 8, 7, 6, 9, 8, 9, 0, 7, 3, 1, 8, 9, 0, 5, 3, 2, 0, 5, 4, 5, 2, 0, 4, 1, 0, 5, 3, 5, 1, 0, 3}; //termina en S03
    int[] state3 = {1, 0, 0, 1, 0, 0, 0, 0, 1, 0, 1, 1, 3};
    int[] transiciones;

    @BeforeClass
    public static void setRedDePetri() {
        redDePetri = new RedDePetri(mji, I, H, tiempos, T, Pinv);
        arrTransiciones = new Transicion[10];

        char alp = 'a';
        for (int i = 0; i < arrTransiciones.length; i++) {
            arrTransiciones[i] = new Transicion("T" + alp++, i, false);
        }
    }

    @Before
    public void setVector() {
//        redDePetri.setVectorDeEstado(new int[]{0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 3, 3});
//        transiciones = redDePetri.getVectorDeEstado();
//        new Log("src/main/resources/log");
    }

//    @Test
//    public void TestMarcadoVectorDeEstado() {
//        //boolean mark = false; //no sirve
////        for (int i = 0; i < transicionesToFire.length; i++) { //no funciona
////            mark = false;
////            redDePetri.calculoDeVectorEstado(arrTransiciones[transicionesToFire[i]]);
////            transiciones = redDePetri.getVectorDeEstado();
////            for (int j = 0; j < stateMarks[0].length; j++) {
////                if (Arrays.equals(transiciones, stateMarks[j])) {
////                    mark = true;
////                    break;
////                }
////            }
////            assertTrue(mark);
////        for (int i = 0; i < fire.length; i++) {
////            redDePetri.calculoDeVectorEstado(arrTransiciones[fire[i]]);
////            transiciones = redDePetri.getVectorDeEstado();
////        }
//        Assert.assertArrayEquals(transiciones, state3);
//    }

//    @Test
//    public void TestActualizarSensibilizadas() {
//        int[] f_sen = {0, 1, 6, 0, 7};
//        boolean[] sensi_S5 = {false, false, false, true, false, false, false, false, false, false};
////        for (int i = 0; i < f_sen.length; i++) {
////            redDePetri.calculoDeVectorEstado(arrTransiciones[f_sen[i]]);
////        }
//        Assert.assertArrayEquals(redDePetri.getVectorDeEstado(), new int[]{1, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 1, 2});
//        //  Assert.assertArrayEquals(redDePetri.getVectorDeEstado(), new int[]{1, 0, 0, 1, 0, 1, 0, 0, 0, 0, 1, 1, 2});
//
//        //  redDePetri.actualiceSensibilizadoT();
//
//        System.out.println("sensiblizadas");
//        for (int i = 0; i < 10; i++) {
//            //   System.out.printf("%b ", redDePetri.getSensibilizada()[i]);
//        }
//        System.out.println();
//        //   Assert.assertEquals(sensi_S5.length, redDePetri.getSensibilizada().length);
//
//        //Assert.assertArrayEquals(redDePetri.getSensibilizada(), sensi_S5); //todo descomentar
//    }

    @Test
    public void testFire() {
        int[] f_sen = {0, 1, 6, 0, 7};
        boolean[] sensi5 = {false, false, false, true, false, false, false, false, false, false};

        for (int i = 0; i < f_sen.length; i++) {
            redDePetri.disparar(arrTransiciones[f_sen[i]]);
        }
        Boolean[] temp = redDePetri.getSensibilizadas();
        System.out.println("antes");
        Operaciones.printBoolean(sensi5);
        System.out.println("despues");
        for (int i = 0; i < sensi5.length; i++) {
            System.out.printf("%b ", temp[i]);
        }
        System.out.println();
    }

    @Test
    public void testWrite() {
        Log log = new Log("log");
        test("aaaaa", log);
        test("bbbbb", log);
        log.close();
    }

    @Test
    public void testGetSensibilizados() { // anda solo cuando t0 y t6 son inmediatas

        Boolean[] arr0 = {true, false, false, false, true, false, false, false, false, false};//T0
        Boolean[] resultado = {true, false, false, false, false, false, false, false, false, false};//T0

        redDePetri.setVectorEandB(arr0);
        var cambiado = redDePetri.getSensibilizadas();
        System.out.printf("-------------- resultado Vector sensibilizado  main ------------ \n");
        Operaciones.printB(cambiado);
        Assert.assertArrayEquals(cambiado, resultado); //todo descomentar
    }

    @Test
    public void testEstado() {
             /*
        -------------- rdp antes de modif Vector sensibilizado  hilo_1 ------------
        * * * 3 * * 6 * 8 *
        -------------- Vector sensibilizado hilo_1 t:1 ------------
        * * * 3 * * 6 * 8 *
        ---------------------- Vector colas hilo_1 t:1 ------------
        0 * 2 * * * 6 * 8 *
        ---------------------- Vector m hilo_1 t:1 -------------
        * * * * * * 6 * 8 *
        * */

        int[] temp_estado = {0, 1, 0, 0, 0, 1, 0, 1, 0, 1, 0, 2, 2};
        redDePetri.setVectorDeEstado(temp_estado);
        Boolean[] arr0 = {false, false, false, true, false, false, true, false, true, false};//T0
        Boolean[] resultado = {false, false, false, false, false, false, true, false, false, false};//T0

        redDePetri.setVectorEandB(arr0);
        var cambiado = redDePetri.getSensibilizadas();
        System.out.printf("-------------- resultado Vector sensibilizado  main ------------ \n");
        Operaciones.printB(cambiado);
        Assert.assertArrayEquals(cambiado, resultado); //todo descomentar

    }

    @Test
    public void testVentana() {
        long alpha = 2;
        long beta = 10;
        long ahora = 124;
        long timeStamp = 123;
        System.out.println((ahora - timeStamp));
        System.out.println(((ahora - timeStamp)));
        boolean ventana = ((ahora - timeStamp) >= alpha) && ((ahora - timeStamp) < beta);
        Assert.assertTrue(ventana);
    }

    public void test(String str, Log log) {
        log.write(str);
    }
}

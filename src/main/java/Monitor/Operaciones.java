package Monitor;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Operaciones {

    private static int CANTIDAD; //hilos
    private static Scanner INPUT_STREAM;

    public static synchronized  boolean[] andVector(boolean[] lista1, boolean[] lista2) {

        boolean[] resultado = new boolean[lista1.length];
        for(int i = 0; i < lista1.length; i++) {
            resultado[i] =(lista1[i] == lista2[i]);
        }
        return resultado;

    }


    public static synchronized boolean comprobarUnos(boolean[] lista){
        for (boolean b : lista) {
            if (b) {
                return true;
            }
        }
        return false;
    }



    public static synchronized int[][] productoMatrices(int[][] a, int[][] b) throws IllegalArgumentException {

        int[][] c = new int[a.length][b[0].length]; //inicializo c
        //se comprueba si las matrices se pueden multiplicar
        if (a[0].length == b.length) {
            for (int i = 0; i < a.length; i++) {
                for (int j = 0; j < b[0].length; j++) {
                    for (int k = 0; k < a[0].length; k++) {
                        //se multiplica la matriz
                        c[i][j] += a[i][k] * b[k][j];
                    }
                }
            }
        } else {
            System.out.print("Columas de matriz 1: \n");
            System.out.println(a[0].length);
            System.out.print("Filas de matriz 2: \n");
            System.out.println(b.length);
            throw new IllegalArgumentException("Las matrices no cumplen con las condiciones para poder efectuar la multiplicacion"); //si no se cumple la condicion tira IllegalArgumentException
        }
        return c;
    }


    public static synchronized int[][] sumaMatrices(int[][] a, int[][] b) throws IllegalArgumentException {
        int[][] c = new int[a.length][a[0].length]; //inicializo c con mismos tamanios
        if ((a[0].length == b[0].length) && (a.length == b.length)) { //compruebo que a y b sean del mismo tamanio
            for (int x = 0; x < a.length; x++) { //recorro en un for y sumo los elementos de las matrices
                for (int y = 0; y < a[x].length; y++) {
                    c[x][y] = a[x][y] + b[x][y];
                }
            }
        } else {
            throw new IllegalArgumentException("Matrices de diferentes tamanios"); //si no se cumple la condicion tira IllegalArgumentException
        }
        return c;
    }


    /**
     * Metodo transpuesta. Realiza la transpuesta de una matriz de datos tipo int.
     *
     * @param a Matriz a transponer
     * @return int[][] Matriz transpuesta
     */
    public static synchronized int[][] transpuesta(int[][] a) {
        int[][] c = new int[a[0].length][a.length];
        for (int fila = 0; fila < a.length; fila++) {
            for (int columna = 0; columna < a[0].length; columna++) {
                c[columna][fila] = a[fila][columna];
            }
        }

        return c;
    }


    public static String outPut(String filename) {
        String output = "";

        try {
            File file = new File(filename);
            INPUT_STREAM = new Scanner(file);
            int i = 0;
            while (INPUT_STREAM.hasNext()) {

                String line = INPUT_STREAM.next();
                String[] values = line.split(",");
                for (String value : values) {
                    output += value;
                }
                i++;
            }
            INPUT_STREAM.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return output;
    }

    public static int[][] matriz2d(String fileName) {
        ArrayList<ArrayList<Integer>> a = new ArrayList<ArrayList<Integer>>();
        try {
            File file = new File(fileName);
            INPUT_STREAM = new Scanner(file);
            int i = 0;
            while (INPUT_STREAM.hasNext()) {
                a.add(new ArrayList<Integer>());
                String line = INPUT_STREAM.next();
                String[] values = line.split(",");
                for (int j = 0; j < values.length; j++) {
                    a.get(i).add(Integer.valueOf(values[j]));
                }
                i++;
            }
            INPUT_STREAM.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return a.stream().map(u -> u.stream().mapToInt(i -> i).toArray()).toArray(int[][]::new);
    }

    /**
     * Este metodo se usa para cargar un vector.
     *
     * @param fileName the file name
     * @return the int [ ]
     */
    public static int[] vector(String fileName) {
        ArrayList<Integer> a = new ArrayList<Integer>();
        try {
            File file = new File(fileName);
            INPUT_STREAM = new Scanner(file);
            while (INPUT_STREAM.hasNext()) {

                String line = INPUT_STREAM.next();
                String[] values = line.split(",");
                for (String value : values) {
                    a.add(Integer.valueOf(value));
                }
            }
            INPUT_STREAM.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return a.stream().mapToInt(i -> i).toArray();
    }

    public static void setCantidadHilos(int i) {
        CANTIDAD = i;
    }

    public static int getCantidadHilos() {
        return CANTIDAD;
    }

    public static void prinThisMatrix(int[][] matrix) {
        for (int[] ints : matrix) {
            for (int anInt : ints) {
                System.out.print(anInt + " ");
            }
            System.out.println();
        }
    }

    public static void printVector(int[] vector) {
        for (int value : vector) {
            System.out.print(value);
        }
        System.out.println();
    }

}

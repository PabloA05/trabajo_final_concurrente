package Util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class Operaciones {

    private static int CANTIDAD; //hilos
    private static Scanner INPUT_STREAM;

    public static Boolean[] andVector(Boolean[] lista1, Boolean[] lista2) {

        Boolean[] resultado = new Boolean[lista1.length];
        for (int i = 0; i < lista1.length; i++) {
            resultado[i] = lista1[i] && lista2[i];
        }
        return resultado;

    }


    public static Boolean comprobarUnos(Boolean[] lista) {
        for (boolean b : lista) {
            if (b) {
                return true;
            }
        }
        return false;
    }


    public static int[][] productoMatrices(int[][] a, int[][] b) throws IllegalArgumentException {

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

    public static int[] multiplyWithForLoops(int[][] matrix, int[] vector) {
        int rows = matrix.length;
        int columns = matrix[0].length;

        int[] result = new int[rows];

        for (int row = 0; row < rows; row++) {
            int sum = 0;
            for (int column = 0; column < columns; column++) {
                sum += matrix[row][column] * vector[column];
            }
            result[row] = sum;
        }
        return result;
    }

    public static void printBoolean(boolean[] boo) {
        for (int i = 0; i < boo.length; i++) {
            System.out.printf("%b ", boo[i]);
        }
        System.out.println();
    }


    public static Boolean[] productoMatrizVectorBoolean(int[][] matriz, Boolean[] vector) throws IllegalArgumentException {

        Boolean[] resultado = new Boolean[matriz.length];
        if (matriz[0].length == vector.length) {
            for (int i = 0; i < matriz.length; i++) {
                for (int j = 0; j < matriz[0].length; j++) {
                    if (matriz[i][j] != 0 && vector[j]) {
                        resultado[i] = true;
                        break;
                    } else resultado[i] = false;
                }
            }
        } else {
            throw new IllegalArgumentException("Matrices de diferentes tamanios");
        }
        return resultado;
    }

    public static int[][] sumaMatrices(int[][] a, int[][] b) throws IllegalArgumentException {
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
    public static int[][] transpuesta(int[][] a) {
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

    public static ArrayList<ArrayList<Integer>> setPinvariantes(String fileName) {

        ArrayList<ArrayList<Integer>> pInvariantes = new ArrayList<ArrayList<Integer>>();
        String numero;
        try {
            File file = new File(fileName);
            INPUT_STREAM = new Scanner(file);
            int i = 0;
            boolean flag = false;
            pInvariantes.add(new ArrayList<Integer>());
            while (INPUT_STREAM.hasNext()) {
                String line = INPUT_STREAM.next();
                if (line.contains("M")) {
                    numero = line.substring(line.indexOf("P") + 1, line.indexOf(")"));
                    pInvariantes.get(i).add(Integer.valueOf(numero));
                    //System.out.println(numero);
                }
                if (line.matches("[+-]?\\d*(\\.\\d+)?") && !line.equals("+")) {
                    pInvariantes.get(i).add(Integer.valueOf(line));
                }
                if (flag) {
                    i++;
                    pInvariantes.add(new ArrayList<Integer>());
                    flag = false;
                }
                if (line.equals("=")) {
                    flag = true;
                }

            }
            INPUT_STREAM.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        pInvariantes.remove(pInvariantes.size() - 1);

        return pInvariantes;
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
        for (int i = 0; i < vector.length; i++) {
            System.out.printf("%d", vector[i]);
            if (i + 1 != vector.length) {
                System.out.print(" ,");
            }
        }
        System.out.println();
    }

    public static void printB(Boolean[] boo) {
        for (int i = 0; i < boo.length; i++) {
            if (boo[i]) {
                System.out.printf("%d ", i);
            } else {
                System.out.print("0 ");
            }
        }
        System.out.println();
    }

    public static void printVectorEx(Boolean[] vector) {
        for (int i = 0; i < vector.length; i++) {
            if (vector[i]) {
                System.out.println("La transición: " + (i) + " esta sesibilizada");
            } else {
                System.out.println("La transición: " + (i) + " no esta sesibilizada");
            }
        }
        System.out.println();
    }

    public static void printVectorColas(Boolean[] vector) {
        for (int i = 0; i < vector.length; i++) {
            if (vector[i]) {
                System.out.println("La transición: " + (i + 1) + " tiene en Cola");
            } else {
                System.out.println("La transición: " + (i + 1) + " no tiene en Cola");
            }
        }
        System.out.println();
    }
}
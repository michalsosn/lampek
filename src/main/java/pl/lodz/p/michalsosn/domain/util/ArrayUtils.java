package pl.lodz.p.michalsosn.domain.util;

import java.util.Arrays;

/**
 * @author Michał Sośnicki
 */
public final class ArrayUtils {

    private ArrayUtils() {
    }

    public static int[][] copy2d(int[][] array) {
        int height = array.length;
        int[][] newArray = new int[height][];
        for (int y = 0; y < height; y++) {
            newArray[y] = Arrays.copyOf(array[y], array[y].length);
        }
        return newArray;
    }

    public static double[][] copy2d(double[][] array) {
        int height = array.length;
        double[][] newArray = new double[height][];
        for (int y = 0; y < height; y++) {
            newArray[y] = Arrays.copyOf(array[y], array[y].length);
        }
        return newArray;
    }

    public static boolean isEmpty(double[][] array) {
        int height = array.length;
        if (height == 0) {
            return true;
        }
        for (double[] row : array) {
            if (row.length != 0) {
                return false;
            }
        }
        return true;
    }

    public static String limitedToString(byte[] array, int limit) {
        if (array.length < limit) {
            return Arrays.toString(array);
        } else {
            StringBuilder stringBuilder = new StringBuilder("[");
            for (int i = 0; i < limit - 1; i++) {
                stringBuilder.append(array[i]).append(", ");
            }
            stringBuilder.append(array[limit - 1]).append("...]");
            return stringBuilder.toString();
        }
    }

    public static String limitedToString(int[] array, int limit) {
        if (array.length < limit) {
            return Arrays.toString(array);
        } else {
            StringBuilder stringBuilder = new StringBuilder("[");
            for (int i = 0; i < limit - 1; i++) {
                stringBuilder.append(array[i]).append(", ");
            }
            stringBuilder.append(array[limit - 1]).append("...]");
            return stringBuilder.toString();
        }
    }

    public static String limitedToString(double[] array, int limit) {
        if (array.length < limit) {
            return Arrays.toString(array);
        } else {
            StringBuilder stringBuilder = new StringBuilder("[");
            for (int i = 0; i < limit - 1; i++) {
                stringBuilder.append(array[i]).append(", ");
            }
            stringBuilder.append(array[limit - 1]).append("...]");
            return stringBuilder.toString();
        }
    }

    public static <T> String limitedToString(T[] array, int limit) {
        if (array.length < limit) {
            return Arrays.toString(array);
        } else {
            StringBuilder stringBuilder = new StringBuilder("[");
            for (int i = 0; i < limit - 1; i++) {
                stringBuilder.append(array[i]).append(", ");
            }
            stringBuilder.append(array[limit - 1]).append("...]");
            return stringBuilder.toString();
        }
    }
}

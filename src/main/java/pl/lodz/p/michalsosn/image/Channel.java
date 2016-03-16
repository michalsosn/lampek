package pl.lodz.p.michalsosn.image;

import pl.lodz.p.michalsosn.util.Lift;

import java.util.function.IntUnaryOperator;

/**
 * An array made of one of primary colors of an image.
 * Objects of this class are immutable.
 * The array is rectangular, all rows are of equal length.
 * @author Michał Sośnicki
 */
public interface Channel<T> extends Size2d, Lift<IntUnaryOperator, T> {

    int getValue(int x, int y);

    /**
     * Returns a 2d array containing pixel values from this Channel.
     * The array is safe to modify and use, it won't change the Channel.
     * @return a 2d array of pixel values from this Channel.
     */
    int[][] getValues();

    void copyTo(int x, int[] buffer, int dest);

}

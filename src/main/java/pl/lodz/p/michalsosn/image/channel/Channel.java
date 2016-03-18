package pl.lodz.p.michalsosn.image.channel;

import pl.lodz.p.michalsosn.image.Size2d;
import pl.lodz.p.michalsosn.util.Lift;

import java.util.function.IntUnaryOperator;
import java.util.stream.IntStream;

/**
 * An array made of one of primary colors of an image.
 * Objects of this class are immutable.
 * The array is rectangular, all rows are of equal length.
 * @author Michał Sośnicki
 */
public interface Channel extends Size2d, Lift<IntUnaryOperator, Channel> {

    int getValue(int y, int x);

    default IntStream values() {
        return IntStream.range(0, getHeight()).flatMap(y ->
                IntStream.range(0, getWidth()).map(x -> getValue(y, x))
        );
    }

    /**
     * Returns a 2d array containing pixel values from this Channel.
     * The array is safe to modify and use, it won't change the Channel.
     * @return a 2d array of pixel values from this Channel.
     */
    int[][] copyValues();

    void copyTo(int y, int[] buffer, int dest);

    <T> T accept(ChannelVisitor<T> visitor);

    Channel toLazy();

    Channel toStrict();

}

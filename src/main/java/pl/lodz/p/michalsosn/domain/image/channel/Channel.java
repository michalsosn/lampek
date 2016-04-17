package pl.lodz.p.michalsosn.domain.image.channel;

import pl.lodz.p.michalsosn.domain.image.Size2d;
import pl.lodz.p.michalsosn.domain.Lift;

import java.util.function.IntBinaryOperator;
import java.util.function.IntUnaryOperator;
import java.util.stream.IntStream;

/**
 * A single color channel.
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

    Channel toLazy();

    Channel toStrict();

    Channel constructSimilar(int height, int width,
                             IntBinaryOperator valueFunction);

    default Channel constructSimilar(IntBinaryOperator valueFunction) {
        return constructSimilar(getHeight(), getWidth(), valueFunction);
    }

    default Channel constructConst(int height, int width, int value) {
        return constructSimilar(height, width, (y, x) -> value);
    }

}

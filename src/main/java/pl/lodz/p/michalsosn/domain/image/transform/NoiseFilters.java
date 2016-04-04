package pl.lodz.p.michalsosn.domain.image.transform;

import pl.lodz.p.michalsosn.domain.image.channel.Channel;

import java.util.Arrays;
import java.util.function.IntBinaryOperator;
import java.util.function.UnaryOperator;

import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * @author Michał Sośnicki
 */
public final class NoiseFilters {

    private NoiseFilters() {
    }

    public static UnaryOperator<Channel> arithmeticMean(int range) {
        if (range < 0) {
            throw new IllegalArgumentException(
                    range + " must not be negative."
            );
        }

        return channel -> {
            int heightLim = channel.getHeight() - 1;
            int widthLim = channel.getWidth() - 1;

            int rangeLength = (1 + 2 * range) * (1 + 2 * range); // 1, 9, 25...

            IntBinaryOperator meanFunction = (y, x) -> {
                int sum = 0;
                for (int i = -range; i <= range; i++) {
                    for (int j = -range; j <= range; j++) {
                        sum += channel.getValue(max(0, min(heightLim, y + i)),
                                               (max(0, min(widthLim, x + j))));
                    }
                }
                return sum / rangeLength;
            };

            return channel.constructSimilar(heightLim + 1, widthLim + 1,
                                            meanFunction);
        };
    }

    public static UnaryOperator<Channel> median(int range) {
        if (range < 0) {
            throw new IllegalArgumentException(
                    range + " must not be negative."
            );
        }

        return channel -> {
            int heightLim = channel.getHeight() - 1;
            int widthLim = channel.getWidth() - 1;

            int rangeLength = (1 + 2 * range) * (1 + 2 * range);
            int[] buffer = new int[rangeLength];

            IntBinaryOperator medianFunction = (y, x) -> {
                int index = 0;
                for (int i = -range; i <= range; i++) {
                    for (int j = -range; j <= range; j++) {
                        buffer[index++] = channel.getValue(
                                max(0, min(heightLim, y + i)),
                                max(0, min(widthLim, x + j))
                        );
                    }
                }
                Arrays.sort(buffer);
                return buffer[rangeLength / 2];
            };

            return channel.constructSimilar(heightLim + 1, widthLim + 1,
                                            medianFunction);
        };
    }

}

package pl.lodz.p.michalsosn.image.transform;

import pl.lodz.p.michalsosn.image.BufferChannel;
import pl.lodz.p.michalsosn.image.Channel;

import java.util.Arrays;
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
            throw new IllegalArgumentException(range + " must not be negative.");
        }

        return channel -> {
            int height = channel.getHeight();
            int width = channel.getWidth();

            int[][] values = new int[height][width];

            int rangeLength = (1 + 2 * range) * (1 + 2 * range); // 1, 9, 25, 49...
            for (int x = 0; x < height; x++) {
                for (int y = 0; y < width; y++) {
                    int sum = 0;
                    for (int i = -range; i <= range; i++) {
                        for (int j = -range; j <= range; j++) {
                            sum += values[max(0, min(height, x + i))]
                                         [max(0, min(width , y + j))];
                        }
                    }
                    values[x][y] = sum / rangeLength;
                }
            }

            return new BufferChannel(values);
        };
    }

    public static UnaryOperator<Channel> median(int range) {
        if (range < 0) {
            throw new IllegalArgumentException(range + " must not be negative.");
        }

        return channel -> {
            int height = channel.getHeight();
            int width = channel.getWidth();

            int[][] values = new int[height][width];

            int rangeLength = (1 + 2 * range) * (1 + 2 * range);
            int[] buffer = new int[rangeLength];
            for (int x = 0; x < height; x++) {
                for (int y = 0; y < width; y++) {
                    int index = 0;
                    for (int i = -range; i <= range; i++) {
                        for (int j = -range; j <= range; j++) {
                            buffer[index++] = values[max(0, min(height, x + i))]
                                                    [max(0, min(width , y + j))];
                        }
                    }
                    Arrays.sort(buffer);
                    int median = buffer[rangeLength/2];
                    values[x][y] = median;
                }
            }

            return new BufferChannel(values);
        };
    }

}

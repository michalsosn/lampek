package pl.lodz.p.michalsosn.domain.image.transform;

import pl.lodz.p.michalsosn.domain.image.channel.BufferChannel;
import pl.lodz.p.michalsosn.domain.image.channel.Channel;
import pl.lodz.p.michalsosn.domain.image.channel.Image;

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
                                                max(0, min(widthLim, x + j)));
                    }
                }
                return Math.round((float) sum / rangeLength);
            };

            return channel.constructSimilar(heightLim + 1, widthLim + 1,
                                            meanFunction);
        };
    }

    public static UnaryOperator<Channel>
    arithmeticMeanRunning(int range) {
        if (range < 0) {
            throw new IllegalArgumentException(
                    range + " must not be negative."
            );
        }

        return channel -> {
            int height = channel.getHeight();
            int width = channel.getWidth();
            int heightLim = height - 1;
            int widthLim = width - 1;

            int[][] values = new int[height][width];

            int rangeLength = (1 + 2 * range) * (1 + 2 * range); // 1, 9, 25...

            int sum = 0;
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (x == 0) {
                        sum = 0;
                        for (int i = -range; i <= range; i++) {
                            for (int j = -range; j <= range; j++) {
                                sum += channel.getValue(
                                        max(0, min(heightLim, y + i)),
                                        max(0, min(widthLim, x + j)));
                            }
                        }
                    } else {
                        for (int i = -range; i <= range; i++) {
                            sum -= channel.getValue(
                                    max(0, min(heightLim, y + i)),
                                    max(0, min(widthLim, x - range - 1)));
                            sum += channel.getValue(
                                    max(0, min(heightLim, y + i)),
                                    max(0, min(widthLim, x + range)));
                        }
                    }
                    values[y][x] = Math.round((float) sum / rangeLength);
                }
            }

            return new BufferChannel(values);
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

    public static UnaryOperator<Channel>
    medianRunning(int range) {
        if (range < 0) {
            throw new IllegalArgumentException(
                    range + " must not be negative."
            );
        }

        return channel -> {
            int height = channel.getHeight();
            int width = channel.getWidth();
            int heightLim = height - 1;
            int widthLim = width - 1;

            int[][] values = new int[height][width];

            int rangeLength = (1 + 2 * range) * (1 + 2 * range); // 1, 9, 25...
            int medianPos = (rangeLength + 1) / 2;
            int[] histogram = new int[Image.MAX_VALUE + 1];

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (x == 0) {
                        Arrays.fill(histogram, 0);
                        for (int i = -range; i <= range; i++) {
                            for (int j = -range; j <= range; j++) {
                                int value = channel.getValue(
                                        max(0, min(heightLim, y + i)),
                                        max(0, min(widthLim, x + j)));
                                histogram[value] += 1;
                            }
                        }
                    } else {
                        for (int i = -range; i <= range; i++) {
                            int removed =  channel.getValue(
                                    max(0, min(heightLim, y + i)),
                                    max(0, min(widthLim, x - range - 1)));
                            histogram[removed] -= 1;
                            int added = channel.getValue(
                                    max(0, min(heightLim, y + i)),
                                    max(0, min(widthLim, x + range)));
                            histogram[added] += 1;
                        }
                    }
                    int count = 0;
                    int v = 0;
                    while (count < medianPos) {
                        count += histogram[v++];
                    }
                    values[y][x] = v - 1;
                }
            }

            return new BufferChannel(values);
        };
    }

}

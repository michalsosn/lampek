package pl.lodz.p.michalsosn.domain.image.transform;

import pl.lodz.p.michalsosn.domain.image.channel.Channel;

import java.util.Arrays;
import java.util.function.IntBinaryOperator;
import java.util.function.UnaryOperator;

/**
 * @author Michał Sośnicki
 */
public final class ChannelOps {

    private static final Kernel[] KIRSCH_KERNELS = Arrays.stream(
            new double[][][]{
                    {{ 5,  5,  5}, {-3, 0, -3}, {-3, -3, -3}},
                    {{-3,  5,  5}, {-3, 0,  5}, {-3, -3, -3}},
                    {{-3, -3,  5}, {-3, 0,  5}, {-3, -3,  5}},
                    {{-3, -3, -3}, {-3, 0,  5}, {-3,  5,  5}},
                    {{-3, -3, -3}, {-3, 0, -3}, { 5,  5,  5}},
                    {{-3, -3, -3}, { 5, 0, -3}, { 5,  5, -3}},
                    {{ 5, -3, -3}, { 5, 0, -3}, { 5, -3, -3}},
                    {{ 5,  5, -3}, { 5, 0, -3}, {-3, -3, -3}}
            }
    ).map(Kernel::normalized).toArray(Kernel[]::new);

    private ChannelOps() {
    }

    public static UnaryOperator<Channel> convolution(Kernel kernel) {
        return channel -> {
            if (channel.getSize() == 0) {
                throw new IllegalArgumentException("Channel has zero size.");
            }

            return channel.constructSimilar(
                    kernel.getHeight() + channel.getHeight() - 1,
                    kernel.getWidth() + channel.getWidth() - 1,
                    convolveSingle(channel, kernel)
            );
        };
    }

    public static UnaryOperator<Channel> kirschOperator() {
        return channel -> {
            int height = channel.getHeight();
            int width = channel.getWidth();

            IntBinaryOperator kirschFunction = (y, x) ->
                Arrays.stream(KIRSCH_KERNELS)
                    .mapToInt(kernel ->
                            convolveSingle(channel, kernel).applyAsInt(y, x)
                    )
                    .max().getAsInt();

            return channel.constructSimilar(height, width, kirschFunction);
        };
    }

    private static IntBinaryOperator convolveSingle(Channel channel,
                                                    Kernel kernel) {
        int channelHeight = channel.getHeight();
        int channelWidth = channel.getWidth();
        int kernelHeight = kernel.getHeight();
        int kernelWidth = kernel.getWidth();

        return (y, x) -> {
            double result = 0;
            int fromY = Math.max(y - kernelHeight + 1, 0);
            int toY = Math.min(y + 1, channelHeight);
            int fromX = Math.max(x - kernelWidth + 1, 0);
            int toX = Math.min(x + 1, channelWidth);
            for (int itY = fromY; itY < toY; itY++) {
                for (int itX = fromX; itX < toX; itX++) {
                    result += channel.getValue(itY, itX)
                            * kernel.getValue(y - itY, x - itX);
                }
            }
            return (int) Math.round(result + kernel.getShift());
        };
    }


}

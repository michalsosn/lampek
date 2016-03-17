package pl.lodz.p.michalsosn.image.transform;

import pl.lodz.p.michalsosn.image.channel.Channel;
import pl.lodz.p.michalsosn.image.channel.Channels;

import java.util.function.IntBinaryOperator;
import java.util.function.UnaryOperator;

/**
 * @author Michał Sośnicki
 */
public final class ChannelOps {

    private ChannelOps() {
    }

    public static UnaryOperator<Channel> convolution(Channel kernel) {
        if (kernel.getSize() == 0) {
            throw new IllegalArgumentException("Kernel has zero size.");
        }
        int kernelHeight = kernel.getHeight();
        int kernelWidth = kernel.getWidth();

        return channel -> {
            if (channel.getSize() == 0) {
                throw new IllegalArgumentException("Channel has zero size.");
            }
            int channelHeight = channel.getHeight();
            int channelWidth = channel.getWidth();

            IntBinaryOperator convolutionFunction = (y, x) -> {
                double result = 0;
                int fromY = Math.max(y - kernelHeight + 1, 0);
                int toY = Math.min(y + 1, channelHeight);
                int fromX = Math.max(x - kernelWidth + 1, 0);
                int toX = Math.min(x + 1, channelWidth);
                for (int itY = fromY; itY < toY; itY++) {
                    for (int itX = fromX; itX < toX; itX++) {
                        result += channel.getValue(itY, itX) * kernel.getValue(y - itY, x - itX);
                    }
                }
                return (int) result;
            };

            return Channels.constructOfType(channel,
                    kernelHeight + channelHeight - 1,
                    kernelWidth + channelWidth - 1,
                    convolutionFunction
            );
        };
    }

}

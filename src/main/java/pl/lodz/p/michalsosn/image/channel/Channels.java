package pl.lodz.p.michalsosn.image.channel;

import java.util.function.Function;
import java.util.function.IntBinaryOperator;

/**
 * @author Michał Sośnicki
 */
public final class Channels {

    private Channels() {
    }

    public static Channel constructOfType(Channel pattern, int height, int width, IntBinaryOperator valueFunction) {
        Function<Channel, Channel> constructBufferChannel = bufferChannel -> {
            int[][] newValues = new int[height][width];
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    newValues[y][x] = valueFunction.applyAsInt(y, x);
                }
            }
            return new BufferChannel(newValues);
        };

        return pattern.accept(ChannelVisitor.channelVisitor(
                constructBufferChannel,
                lazyChannel -> new LazyChannel(height, width, valueFunction),
                constructBufferChannel
        ));
    }

    public static Channel constOfType(Channel pattern, int height, int width, int value) {
        return pattern.accept(ChannelVisitor.channelVisitor(
                bufferChannel -> new ConstChannel(height, width, value),
                lazyChannel -> new LazyChannel(height, width, (y, x) -> value),
                constChannel -> new ConstChannel(height, width, value)
        ));
    }

}

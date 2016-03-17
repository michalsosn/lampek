package pl.lodz.p.michalsosn.image.channel;

import java.util.function.Function;

/**
 * @author Michał Sośnicki
 */
public interface ChannelVisitor<T> {

    T visit(BufferChannel bufferChannel);

    T visit(LazyChannel lazyChannel);

    T visit(ConstChannel constChannel);

    static <T> ChannelVisitor<T> channelVisitor(Function<? super BufferChannel, ? extends T> bufferConsumer,
                                                Function<? super LazyChannel, ? extends T> lazyConsumer,
                                                Function<? super ConstChannel, ? extends T> constConsumer) {
        return new ChannelVisitor<T>() {
            @Override
            public T visit(BufferChannel bufferChannel) {
                return bufferConsumer.apply(bufferChannel);
            }

            @Override
            public T visit(LazyChannel lazyChannel) {
                return lazyConsumer.apply(lazyChannel);
            }

            @Override
            public T visit(ConstChannel constChannel) {
                return constConsumer.apply(constChannel);
            }
        };
    }
}

package pl.lodz.p.michalsosn.domain.util;

/**
 * @author Michał Sośnicki
 */
@FunctionalInterface
public interface IntBiConsumer {

    void accept(int a, int b);

    default IntBiConsumer andThen(IntBiConsumer after) {
        return (a, b) -> {
            accept(a, b);
            after.accept(a, b);
        };
    }

}

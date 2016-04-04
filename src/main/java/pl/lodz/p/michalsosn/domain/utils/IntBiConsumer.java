package pl.lodz.p.michalsosn.domain.utils;

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

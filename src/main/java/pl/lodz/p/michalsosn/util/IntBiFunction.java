package pl.lodz.p.michalsosn.util;

import java.util.function.Function;

/**
 * @author Michał Sośnicki
 */
@FunctionalInterface
public interface IntBiFunction<T> {

    T apply(int a, int b);

    default <U> IntBiFunction<? extends U> andThen(
            Function<? super T, ? extends U> after
    ) {
        return (a, b) -> after.apply(apply(a, b));
    }

}

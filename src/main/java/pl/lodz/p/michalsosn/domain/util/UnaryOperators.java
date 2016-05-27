package pl.lodz.p.michalsosn.domain.util;

import java.util.function.UnaryOperator;

/**
 * @author Michał Sośnicki
 */
public final class UnaryOperators {
    private UnaryOperators() {
    }

    public static <T> UnaryOperator<T> compose(UnaryOperator<T> second,
                                               UnaryOperator<T> first) {
        return item -> second.apply(first.apply(item));
    }

}

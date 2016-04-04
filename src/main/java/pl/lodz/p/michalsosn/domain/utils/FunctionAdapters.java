package pl.lodz.p.michalsosn.domain.utils;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author Michał Sośnicki
 */
public final class FunctionAdapters {

    public static <T, R> Function<T, R> toFunction(Consumer<T> consumer) {
        return argument -> {
            consumer.accept(argument);
            return null;
        };
    }

    public static <T, R> Function<T, R> toFunction(Supplier<R> supplier) {
        return argument -> supplier.get();
    }

    public static <T, R> Consumer<T> toConsumer(Function<T, R> function) {
        return function::apply;
    }

    public static <T, R> Consumer<T> toConsumer(Supplier<R> supplier) {
        return argument -> supplier.get();
    }

    public static <T, R> Supplier<R> toSupplier(Function<T, R> function) {
        return () -> function.apply(null);
    }

    public static <T, R> Supplier<R> toSupplier(Consumer<T> consumer) {
        return () -> {
            consumer.accept(null);
            return null;
        };
    }

}

package pl.lodz.p.michalsosn.domain.image.transform.segmentation;

import java.util.function.BinaryOperator;
import java.util.stream.BaseStream;
import java.util.stream.DoubleStream;

/**
 * @author Michał Sośnicki
 */
public final class ClassifiedRegionPair<T, S extends BaseStream<T, S>>
        implements Region<T, S>  {
    private final Region<T, S> first;
    private final Region<T, S> second;
    private final BinaryOperator<S> merger;

    public ClassifiedRegionPair(Region<T, S> first, Region<T, S> second,
                                BinaryOperator<S> merger) {
        this.first = first;
        this.second = second;
        this.merger = merger;
    }

    public static ClassifiedRegionPair<Double, DoubleStream> of(
            ClassifiedRegionPair<Double, DoubleStream> first,
            ClassifiedRegionPair<Double, DoubleStream> second
    ) {
        return new ClassifiedRegionPair<>(first, second, DoubleStream::concat);
    }

    @Override
    public S values() {
        return merger.apply(first.values(), second.values());
    }
}


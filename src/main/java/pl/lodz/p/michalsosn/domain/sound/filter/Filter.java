package pl.lodz.p.michalsosn.domain.sound.filter;

import pl.lodz.p.michalsosn.domain.Lift;
import pl.lodz.p.michalsosn.domain.sound.Size1d;
import pl.lodz.p.michalsosn.domain.sound.TimeRange;

import java.util.function.DoubleUnaryOperator;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

/**
 * Both negative length and positive length are greater or equal to zero.
 * Length equals negative length plus positive length.
 * @author Michał Sośnicki
 */
public interface Filter extends Size1d, Lift<DoubleUnaryOperator, Filter> {

    double getValue(int sample);

    DoubleStream values();

    int getNegativeLength();

    default int getPositiveLength() {
        return getLength() - getNegativeLength();
    }

    default boolean isCausal() {
        return getNegativeLength() == 0;
    }

    TimeRange getSamplingTime();

    @Override
    default IntStream stream() {
        return IntStream.range(-getNegativeLength(), getPositiveLength());
    }

}

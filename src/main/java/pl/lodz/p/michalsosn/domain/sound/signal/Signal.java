package pl.lodz.p.michalsosn.domain.sound.signal;

import pl.lodz.p.michalsosn.domain.Lift;
import pl.lodz.p.michalsosn.domain.sound.Size1d;
import pl.lodz.p.michalsosn.domain.sound.TimeRange;

import java.util.function.DoubleUnaryOperator;
import java.util.stream.DoubleStream;

/**
 * @author Michał Sośnicki
 */
public interface Signal extends Size1d, Lift<DoubleUnaryOperator, Signal> {

    double getValue(int sample);

    DoubleStream values();

    TimeRange getSamplingTime();

}

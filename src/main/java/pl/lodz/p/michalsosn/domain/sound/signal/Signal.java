package pl.lodz.p.michalsosn.domain.sound.signal;

import pl.lodz.p.michalsosn.domain.Lift;
import pl.lodz.p.michalsosn.domain.sound.Size1d;
import pl.lodz.p.michalsosn.domain.sound.TimeRange;

import java.util.function.LongUnaryOperator;
import java.util.stream.LongStream;

/**
 * @author Michał Sośnicki
 */
public interface Signal extends Size1d, Lift<LongUnaryOperator, Signal> {

    long getValue(int sample);

    LongStream values();

    TimeRange getSamplingTime();

}

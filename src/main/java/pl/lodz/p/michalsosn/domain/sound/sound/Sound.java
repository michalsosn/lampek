package pl.lodz.p.michalsosn.domain.sound.sound;

import pl.lodz.p.michalsosn.domain.Lift;
import pl.lodz.p.michalsosn.domain.sound.Size1d;
import pl.lodz.p.michalsosn.domain.sound.TimeRange;

import java.util.function.IntUnaryOperator;
import java.util.stream.IntStream;

/**
 * @author Michał Sośnicki
 */
public interface Sound extends Size1d, Lift<IntUnaryOperator, Sound> {

    int MIN_VALUE = -256 * 256 / 2;
    int MAX_VALUE = 256 * 256 / 2 - 1;
    int MID_VALUE = 0; //(MAX_VALUE - MIN_VALUE) / 2;

    int getValue(int sample);

    IntStream values();

    TimeRange getSamplingTime();

}

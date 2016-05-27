package pl.lodz.p.michalsosn.domain.sound.transform;

import org.junit.Test;
import pl.lodz.p.michalsosn.domain.sound.TimeRange;
import pl.lodz.p.michalsosn.domain.sound.filter.BufferFilter;
import pl.lodz.p.michalsosn.domain.sound.filter.Filter;
import pl.lodz.p.michalsosn.domain.sound.signal.BufferSignal;
import pl.lodz.p.michalsosn.domain.sound.signal.Signal;

import static org.hamcrest.MatcherAssert.assertThat;
import static pl.lodz.p.michalsosn.TestUtils.DELTA;
import static pl.lodz.p.michalsosn.TestUtils.arrayCloseTo;
import static pl.lodz.p.michalsosn.domain.sound.transform.Conversions.toSignal;
import static pl.lodz.p.michalsosn.domain.sound.transform.Extensions.delaySignal;
import static pl.lodz.p.michalsosn.domain.sound.transform.Extensions.rotateLeftSignal;
import static pl.lodz.p.michalsosn.domain.sound.transform.Extensions.zeroPadFilter;

/**
 * @author Michał Sośnicki
 */
public class ExtensionsTest {

    @Test
    public void testCausalPad() throws Exception {
        // given
        final TimeRange time = TimeRange.ofDuration(1.0);
        final Filter filter = new BufferFilter(new double[] {1, 3, 5, 3, 2}, 0, time);

        // when
        final Signal result = toSignal(zeroPadFilter(8).apply(filter));

        // then
        assertThat(result.values().boxed().toArray(Double[]::new),
                   arrayCloseTo(DELTA, 1, 3, 5, 3, 2, 0, 0, 0));
    }

    @Test
    public void testNonCausalPadWhenOdd() throws Exception {
        // given
        final TimeRange time = TimeRange.ofDuration(1.0);
        final Filter filter = new BufferFilter(new double[] {1, 3, 5, 3, 2}, 2, time);

        // when
        final Signal result = toSignal(zeroPadFilter(8).apply(filter));

        // then
        assertThat(result.values().boxed().toArray(Double[]::new),
                   arrayCloseTo(DELTA, 5, 3, 2, 0, 0, 0, 1, 3));
    }

    @Test
    public void testNonCausalPadWhenEven() throws Exception {
        // given
        final TimeRange time = TimeRange.ofDuration(1.0);
        final Filter filter = new BufferFilter(new double[] {1, 3, 5, 3, 2, 6}, 3, time);

        // when
        final Signal result = toSignal(zeroPadFilter(9).apply(filter));

        // then
        assertThat(result.values().boxed().toArray(Double[]::new),
                   arrayCloseTo(DELTA, 3, 2, 6, 0, 0, 0, 1, 3, 5));
    }

    @Test
    public void testNonCausalWhenSameLength() throws Exception {
        // given
        final TimeRange time = TimeRange.ofDuration(1.0);
        final Filter filter = new BufferFilter(new double[] {1, 3, 5, 3, 2, 6}, 3, time);

        // when
        final Signal result = toSignal(zeroPadFilter(filter.getLength()).apply(filter));

        // then
        assertThat(result.values().boxed().toArray(Double[]::new),
                   arrayCloseTo(DELTA, 3, 2, 6, 1, 3, 5));
    }

    @Test
    public void testDelay() throws Exception {
        // given
        final TimeRange time = TimeRange.ofDuration(1.0);
        final Signal signal = new BufferSignal(new double[] {1, 2, 3}, time);

        // when
        final Signal result = delaySignal(4).apply(signal);

        // then
        assertThat(result.values().boxed().toArray(Double[]::new),
                arrayCloseTo(DELTA, 0, 0, 0, 0, 1, 2, 3));
    }

    @Test
    public void testRotateLeft() throws Exception {
        // given
        final TimeRange time = TimeRange.ofDuration(1.0);
        final Signal signal = new BufferSignal(new double[] {1, 2, 3, 4, 5}, time);

        // when
        final Signal result = rotateLeftSignal(2).apply(signal);

        // then
        assertThat(result.values().boxed().toArray(Double[]::new),
                arrayCloseTo(DELTA, 3, 4, 5, 1, 2));
    }

}
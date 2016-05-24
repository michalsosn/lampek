package pl.lodz.p.michalsosn.domain.sound.transform;

import org.junit.Rule;
import org.junit.Test;
import pl.lodz.p.michalsosn.RandomRule;
import pl.lodz.p.michalsosn.Repeat;
import pl.lodz.p.michalsosn.RepeatRule;
import pl.lodz.p.michalsosn.TestUtils;
import pl.lodz.p.michalsosn.domain.sound.TimeRange;
import pl.lodz.p.michalsosn.domain.sound.signal.LazySignal;
import pl.lodz.p.michalsosn.domain.sound.signal.Signal;
import pl.lodz.p.michalsosn.domain.sound.sound.BufferSound;
import pl.lodz.p.michalsosn.domain.sound.sound.Sound;

import java.util.Random;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static pl.lodz.p.michalsosn.domain.sound.transform.Extensions.shortenSignal;
import static pl.lodz.p.michalsosn.domain.sound.transform.Extensions.zeroPadSignal;

/**
 * @author Michał Sośnicki
 */
public class WindowsTest {

    @Rule
    public RandomRule randomRule = new RandomRule();

    @Rule
    public RepeatRule repeatRule = new RepeatRule();

    @Test
    @Repeat(1000)
    public void testOverlapAddOdd() throws Exception {
        final Random random = randomRule.getRandom();
        final int hopSize = random.nextInt(10) + 1;
        testOverlapAdd(hopSize, hopSize * 2 + 1);
    }

    @Test
    @Repeat(1000)
    public void testOverlapAddEven() throws Exception {
        final Random random = randomRule.getRandom();
        final int hopSize = random.nextInt(10) + 1;
        testOverlapAdd(hopSize, hopSize * 2);
    }

    private void testOverlapAdd(int hopSize, int windowLength) throws Exception {
        // given
        final Random random = randomRule.getRandom();
        final int signalLength = random.nextInt(999) + 1;

        final TimeRange timeRange = TimeRange.ofDuration(1.0);
        final Sound sound = new BufferSound(random.ints(
                signalLength, Sound.MIN_VALUE, Sound.MAX_VALUE
        ).toArray(), timeRange);
        final Signal signal = LazySignal.of(sound);
        final Signal paddedSignal = zeroPadSignal(
                signalLength + 2 * windowLength - 2, windowLength - 1
        ).apply(signal);

        // when
        final Signal result = Windows.overlapAdd(
                windowLength, hopSize, Windows.hannSignal()
        ).andThen(shortenSignal(windowLength - 1, signalLength)).apply(paddedSignal);

        // then
        assertThat(result.values().boxed().toArray(Double[]::new),
                is(TestUtils.arrayCloseTo(TestUtils.DELTA, signal.values().toArray())));
    }
}
package pl.lodz.p.michalsosn.domain.sound.transform;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import pl.lodz.p.michalsosn.RandomRule;
import pl.lodz.p.michalsosn.Repeat;
import pl.lodz.p.michalsosn.RepeatRule;
import pl.lodz.p.michalsosn.domain.sound.TimeRange;
import pl.lodz.p.michalsosn.domain.sound.signal.LazySignal;
import pl.lodz.p.michalsosn.domain.sound.signal.Signal;
import pl.lodz.p.michalsosn.domain.sound.sound.BufferSound;
import pl.lodz.p.michalsosn.domain.sound.sound.Sound;

import java.util.Arrays;
import java.util.Collection;
import java.util.Random;
import java.util.function.UnaryOperator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.IsCloseTo.closeTo;
import static pl.lodz.p.michalsosn.TestUtils.DELTA;
import static pl.lodz.p.michalsosn.TestUtils.arrayCloseTo;
import static pl.lodz.p.michalsosn.domain.sound.transform.Extensions.delaySignal;
import static pl.lodz.p.michalsosn.domain.sound.transform.Extensions.shortenSignal;
import static pl.lodz.p.michalsosn.domain.sound.transform.Extensions.zeroPadSignal;

/**
 * @author Michał Sośnicki
 */
@RunWith(Parameterized.class)
public class WindowsTest {

    private UnaryOperator<Signal> signalWindow;

    @Rule
    public RandomRule randomRule = new RandomRule();

    @Rule
    public RepeatRule repeatRule = new RepeatRule();

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { Windows.hannSignal() },
                { Windows.hammingSignal() }
        });
    }

    public WindowsTest(UnaryOperator<Signal> signalWindow) {
        this.signalWindow = signalWindow;
    }

    @Test
    public void windowsAreAlmostSymmetric() throws Exception {
        final TimeRange timeRange = TimeRange.ofDuration(1.0);

        for (int length = 1; length < 100; ++length) {
            // given
            final Signal signal = new LazySignal(i -> 1, length, timeRange);

            // when
            final Signal result = signalWindow.apply(signal);

            // then
            System.out.println(Arrays.toString(result.values().toArray()));
            final int middle = length / 2;
            for (int i = 0; i < middle; ++i) {
                assertThat(result.getValue(middle - i), closeTo(result.getValue(middle + i), 1e-5));
            }
        }
    }

    @Test
    @Repeat(1000)
    public void windowsAreOLAPWhenLengthOdd() throws Exception {
        final Random random = randomRule.getRandom();
        final int hopSize = random.nextInt(10) + 1;
        testOverlapAdd(hopSize, hopSize * 2 + 1);
    }

    @Test
    @Repeat(1000)
    public void windowsAreOLAPWhenLengthEven() throws Exception {
        final Random random = randomRule.getRandom();
        final int hopSize = random.nextInt(10) + 1;
        testOverlapAdd(hopSize, hopSize * 2);
    }

    private void testOverlapAdd(int hopSize, int windowLength) throws Exception {
        // given
        final Random random = randomRule.getRandom();
        final int signalLength = random.nextInt(999) + 1;
        System.out.println("hopSize: " + hopSize + ", windowLength: " + windowLength + ", signalLength: " + signalLength);

        final TimeRange timeRange = TimeRange.ofDuration(1.0);
        final Sound sound = new BufferSound(random.ints(
                signalLength, 9, 10 ///Sound.MIN_VALUE, Sound.MAX_VALUE
        ).toArray(), timeRange);
        final Signal signal = Conversions.toSignal(sound);
        final Signal paddedSignal = zeroPadSignal(signalLength + 2 * windowLength - 2)
                .compose(delaySignal(windowLength - 1)).apply(signal);

        // when
        final Signal result = Windows.overlapAdd(
                windowLength, hopSize, signalWindow
        ).andThen(shortenSignal(windowLength - 1, signalLength)).apply(paddedSignal);

        // then
        double changeSearch = Double.NaN;
        for (int i = 0; i < signalLength; ++i) {
            if (signal.getValue(i) != 0) {
                changeSearch = signal.getValue(i) / result.getValue(i);
                break;
            }
        }
        final double change = changeSearch;
        assertThat(result.values().mapToObj(n -> n * change).toArray(Double[]::new),
                is(arrayCloseTo(DELTA, signal.values().toArray())));
    }
}
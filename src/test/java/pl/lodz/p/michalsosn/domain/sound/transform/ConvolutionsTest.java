package pl.lodz.p.michalsosn.domain.sound.transform;

import org.junit.Rule;
import org.junit.Test;
import pl.lodz.p.michalsosn.RandomRule;
import pl.lodz.p.michalsosn.Repeat;
import pl.lodz.p.michalsosn.RepeatRule;
import pl.lodz.p.michalsosn.domain.sound.TimeRange;
import pl.lodz.p.michalsosn.domain.sound.signal.BufferSignal;
import pl.lodz.p.michalsosn.domain.sound.signal.Signal;
import pl.lodz.p.michalsosn.domain.sound.sound.BufferSound;
import pl.lodz.p.michalsosn.domain.sound.sound.Sound;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static pl.lodz.p.michalsosn.TestUtils.DELTA;
import static pl.lodz.p.michalsosn.TestUtils.arrayCloseTo;

/**
 * @author Michał Sośnicki
 */
public class ConvolutionsTest {

    @Rule
    public RepeatRule repeatRule = new RepeatRule();

    @Rule
    public RandomRule randomRule = new RandomRule();

    private static final List<BiFunction<Signal, Sound, Signal>> convolutionsSafe =
            Arrays.asList(
                    (s1, s2) -> Convolutions.convolveLinearTime(s1).apply(s2),
                    (s1, s2) -> Convolutions.convolveLinearOverlapAdd(
                            Windows.hannSignal(), 5, 2, s1
                    ).apply(s2),
                    (s1, s2) -> Convolutions.convolveLinearOverlapAdd(
                            Windows.hannSignal(), 4, 2, s1
                    ).apply(s2),
                    (s1, s2) -> Convolutions.convolveLinearOverlapAdd(
                            Windows.hannSignal(), 3, 1, s1
                    ).apply(s2),
                    (s1, s2) -> Convolutions.convolveLinearOverlapAdd(
                            UnaryOperator.identity(), 1, 1, s1
                    ).apply(s2),
                    (s1, s2) -> Convolutions.convolveLinearOverlapAdd(
                            UnaryOperator.identity(), 2, 2, s1
                    ).apply(s2),
                    (s1, s2) -> Convolutions.convolveLinearOverlapAdd(
                            UnaryOperator.identity(), 3, 3, s1
                    ).apply(s2)
            );

    private static final List<BiFunction<Signal, Sound, Signal>> convolutions;
    static {
        final List<BiFunction<Signal, Sound, Signal>> moreConvolutions
                = new ArrayList<>(convolutionsSafe);
        moreConvolutions.add(
                (s1, s2) -> Convolutions.convolveLinearFrequency(s1).apply(s2)
        );
        convolutions = Collections.unmodifiableList(moreConvolutions);
    }

    @Test
    public void testHomework1() throws Exception {
        // given
        TimeRange timeRange = TimeRange.ofDuration(1.0);
        Signal first = new BufferSignal(new double[] {1, 2, -1, 1}, timeRange);
        Sound second = new BufferSound(new int[] {2, 1, 1, 3}, timeRange);

        // when
        final Stream<Double[]> results = convolutionsSafe.stream()
                .map(convolution -> convolution.apply(first, second))
                .map(result -> result.values().boxed().toArray(Double[]::new));

        // then
        results.forEach(result ->
                assertThat(result, is(arrayCloseTo(DELTA, 2, 5, 1, 6, 6, -2, 3)))
        );
    }

    @Test
    public void testHomework2() throws Exception {
        // given
        TimeRange timeRange = TimeRange.ofDuration(1.0);
        Signal first = new BufferSignal(new double[] {1, 2, -1}, timeRange);
        Sound second = new BufferSound(new int[] {2, 1, 1, 3}, timeRange);

        // when
        final Stream<Double[]> results = convolutionsSafe.stream()
                .map(convolution -> convolution.apply(first, second))
                .map(result -> result.values().boxed().toArray(Double[]::new));

        // then
        results.forEach(result ->
                assertThat(result, is(arrayCloseTo(DELTA, 2, 5, 1, 4, 5, -3)))
        );
    }

    @Test
    @Repeat(20)
    public void testAlgorithmsEquivalent() throws Exception {
        // given
        final Random random = randomRule.getRandom();
        final int length = (1 << 10) + 1;
        final int firstLength = random.nextInt(length);
        final int secondLength = length - firstLength;
        final double[] firstValues
                = random.doubles(firstLength, Sound.MIN_VALUE, Sound.MAX_VALUE).toArray();
        final int[] secondValues
                = random.ints(secondLength, Sound.MIN_VALUE, Sound.MAX_VALUE).toArray();

        TimeRange timeRange = TimeRange.ofDuration(1.0);
        Signal first = new BufferSignal(firstValues, timeRange);
        Sound second = new BufferSound(secondValues, timeRange);

        // when
        final Stream<Double[]> results = convolutions.stream()
                .map(convolution -> convolution.apply(first, second))
                .map(result -> result.values().boxed().toArray(Double[]::new));

        // then
        results.reduce((prev, current) -> {
            assertThat(current, arrayCloseTo(10e-4, prev));
            return current;
        });
    }
}
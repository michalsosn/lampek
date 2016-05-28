package pl.lodz.p.michalsosn.domain.sound.transform;

import org.junit.Rule;
import org.junit.Test;
import pl.lodz.p.michalsosn.RandomRule;
import pl.lodz.p.michalsosn.Repeat;
import pl.lodz.p.michalsosn.RepeatRule;
import pl.lodz.p.michalsosn.domain.sound.TimeRange;
import pl.lodz.p.michalsosn.domain.sound.filter.BufferFilter;
import pl.lodz.p.michalsosn.domain.sound.filter.Filter;
import pl.lodz.p.michalsosn.domain.sound.signal.Signal;
import pl.lodz.p.michalsosn.domain.sound.sound.BufferSound;
import pl.lodz.p.michalsosn.domain.sound.sound.Sound;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static pl.lodz.p.michalsosn.TestUtils.DELTA;
import static pl.lodz.p.michalsosn.TestUtils.arrayCloseTo;
import static pl.lodz.p.michalsosn.domain.Lift.lift;

/**
 * @author Michał Sośnicki
 */
public class ConvolutionsTest {

    @Rule
    public RepeatRule repeatRule = new RepeatRule();

    @Rule
    public RandomRule randomRule = new RandomRule();

    private static final List<BiFunction<Filter, Sound, Signal>> convolutions =
            Arrays.asList(
                    (s1, s2) -> Convolutions.convolveLinearTime(s1).apply(s2),
                    (s1, s2) -> Convolutions.convolveLinearFrequency(s1).apply(s2),
                    (s1, s2) -> Convolutions.convolveLinearOverlapAdd(
                            Windows.Window.HANN, 1, 1, s1
                    ).apply(s2),
                    (s1, s2) -> Convolutions.convolveLinearOverlapAdd(
                            Windows.Window.HANN, 2, 1, s1
                    ).apply(s2),
                    (s1, s2) -> Convolutions.convolveLinearOverlapAdd(
                            Windows.Window.HANN, 3, 1, s1
                    ).apply(s2),
                    (s1, s2) -> Convolutions.convolveLinearOverlapAdd(
                            Windows.Window.HANN, 4, 2, s1
                    ).apply(s2),
                    (s1, s2) -> Convolutions.convolveLinearOverlapAdd(
                            Windows.Window.HANN, 5, 2, s1
                    ).apply(s2),
                    (s1, s2) -> Convolutions.convolveLinearOverlapAdd(
                            Windows.Window.HANN, 21, 10, s1
                    ).apply(s2),
                    (s1, s2) -> Convolutions.convolveLinearOverlapAdd(
                            Windows.Window.HAMMING, 3, 1, s1
                    ).andThen(lift(p -> p * 0.9287465636377145)).apply(s2),
                    (s1, s2) -> Convolutions.convolveLinearOverlapAdd(
                            Windows.Window.HAMMING, 8, 4, s1
                    ).andThen(lift(p -> p * 0.9287465636377145)).apply(s2),
                    (s1, s2) -> Convolutions.convolveLinearOverlapAdd(
                            Windows.Window.HAMMING, 15, 7, s1
                    ).andThen(lift(p -> p * 0.9287465636377145)).apply(s2),
                    (s1, s2) -> Convolutions.convolveLinearOverlapAdd(
                            Windows.Window.RECTANGULAR, 1, 1, s1
                    ).apply(s2),
                    (s1, s2) -> Convolutions.convolveLinearOverlapAdd(
                            Windows.Window.RECTANGULAR, 4, 4, s1
                    ).apply(s2),
                    (s1, s2) -> Convolutions.convolveLinearOverlapAdd(
                            Windows.Window.RECTANGULAR, 9, 9, s1
                    ).apply(s2)
            );


    @Test
    public void givesCorrectResult1() throws Exception {
        // given
        TimeRange timeRange = TimeRange.ofDuration(1.0);
        Filter first = new BufferFilter(new double[] {1, 2, -1, 1}, 0, timeRange);
        Sound second = new BufferSound(new int[] {2, 1, 1, 3}, timeRange);

        // when
        final Stream<Double[]> results = convolutions.stream()
                .map(convolution -> convolution.apply(first, second))
                .map(result -> result.values().boxed().toArray(Double[]::new));

        // then
        results.forEach(result -> {
            System.out.println(Arrays.toString(result));
            assertThat(result, is(arrayCloseTo(DELTA, 2, 5, 1, 6, 6, -2, 3)));
        });
    }

    @Test
    public void givesCorrectResult2() throws Exception {
        // given
        TimeRange timeRange = TimeRange.ofDuration(1.0);
        Filter first = new BufferFilter(new double[] {1, 2, -1}, 0, timeRange);
        Sound second = new BufferSound(new int[] {2, 1, 1, 3}, timeRange);

        // when
        final Stream<Double[]> results = convolutions.stream()
                .map(convolution -> convolution.apply(first, second))
                .map(result -> result.values().boxed().toArray(Double[]::new));

        // then
        results.forEach(result ->
                assertThat(result, is(arrayCloseTo(DELTA, 2, 5, 1, 4, 5, -3)))
        );
    }

    @Test
    public void givesCorrectResultWhenNonCausal1() throws Exception {
        // given
        TimeRange timeRange = TimeRange.ofDuration(1.0);
        Filter first = new BufferFilter(new double[] {1, 2, -1, 1}, 2, timeRange);
        Sound second = new BufferSound(new int[] {2, 1, 1, 3}, timeRange);

        // when
        final Stream<Double[]> results = convolutions.stream()
                .map(convolution -> convolution.apply(first, second))
                .map(result -> result.values().boxed().toArray(Double[]::new));

        // then
        results.forEach(result ->
            assertThat(result, is(arrayCloseTo(DELTA, 1, 6, 6, -2, 3)))
        );
    }

    @Test
    public void givesCorrectResultWhenNonCausal2() throws Exception {
        // given
        TimeRange timeRange = TimeRange.ofDuration(1.0);
        Filter first = new BufferFilter(new double[] {0, 0, 1, 2, -1}, 2, timeRange);
        Sound second = new BufferSound(new int[] {2, 1, 1, 3}, timeRange);

        // when
        final Stream<Double[]> results = convolutions.stream()
                .map(convolution -> convolution.apply(first, second))
                .map(result -> result.values().boxed().toArray(Double[]::new));

        // then
        results.forEach(result ->
            assertThat(result, is(arrayCloseTo(DELTA, 2, 5, 1, 4, 5, -3)))
        );
    }

    @Test
    @Repeat(200)
    public void algorithmsAreEquivalentCausal() throws Exception {
        // given
        final Random random = randomRule.getRandom();
        final int firstLength = random.nextInt(100) + 1;
        final int secondLength = random.nextInt(100) + 1;
        final double[] firstValues
                = random.doubles(firstLength, Sound.MIN_VALUE, Sound.MAX_VALUE).toArray();
        final int[] secondValues
                = random.ints(secondLength, Sound.MIN_VALUE, Sound.MAX_VALUE).toArray();

        TimeRange timeRange = TimeRange.ofDuration(1.0);
        Filter first = new BufferFilter(firstValues, 0, timeRange);
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

    @Test
    @Repeat(300)
    public void algorithmsAreEquivalentNonCausal() throws Exception {
        // given
        final Random random = randomRule.getRandom();
        final int firstLength = random.nextInt(100) + 1;
        final int secondLength = random.nextInt(100) + 1;
        final double[] firstValues
                = random.doubles(firstLength, Sound.MIN_VALUE, Sound.MAX_VALUE).toArray();
        final int[] secondValues
                = random.ints(secondLength, Sound.MIN_VALUE, Sound.MAX_VALUE).toArray();

        TimeRange timeRange = TimeRange.ofDuration(1.0);
        Filter first = new BufferFilter(firstValues, random.nextInt(firstLength), timeRange);
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
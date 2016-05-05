package pl.lodz.p.michalsosn.domain.sound.transform;

import org.junit.Test;
import pl.lodz.p.michalsosn.TestUtils;
import pl.lodz.p.michalsosn.domain.sound.TimeRange;
import pl.lodz.p.michalsosn.domain.sound.signal.Signal;
import pl.lodz.p.michalsosn.domain.sound.sound.BufferSound;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static pl.lodz.p.michalsosn.domain.sound.transform.Correlations.*;

/**
 * @author Michał Sośnicki
 */
public class CorrelationsTest {

    @Test
    public void testAutocorrelateCyclicMethodsEqual() throws Exception {
        TimeRange timeRange = TimeRange.ofDuration(1.0);
        BufferSound sound = new BufferSound(new int[] {1, 5, -1, 1}, timeRange);

        Signal result1 = autocorrelateCyclicNaive().apply(sound);
        Signal result2 = autocorrelateCyclicWienerKhinchin(false).apply(sound);
//        Signal result3 = BasicFrequencyAnalysis.cepstrum().apply(sound);

        System.out.println(result1);
        System.out.println(result2);

        assertThat(result1, is(result2));
    }


    @Test
    public void testAutocorrelateLinearMethodsEqual() throws Exception {
        TimeRange timeRange = TimeRange.ofDuration(1.0);
        BufferSound sound = new BufferSound(new int[] {1, 5, -1, 1}, timeRange);

        Signal result1 = autocorrelateLinearNaive().apply(sound);
        Signal result2 = autocorrelateLinearWienerKhinchin(false).apply(sound);

        System.out.println(Arrays.toString(result1.values().toArray()));
        System.out.println(Arrays.toString(result2.values().toArray()));

        assertThat(result1.values().boxed().toArray(Double[]::new),
                is(TestUtils.arrayCloseTo(TestUtils.DELTA, result2.values().toArray())));
    }

}
package pl.lodz.p.michalsosn.domain.sound.transform;

import org.junit.Test;
import pl.lodz.p.michalsosn.domain.sound.TimeRange;
import pl.lodz.p.michalsosn.domain.sound.signal.Signal;
import pl.lodz.p.michalsosn.domain.sound.sound.BufferSound;

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
        Signal result2 = autocorrelateCyclic().apply(sound);

        assertThat(result1, is(result2));
    }

}
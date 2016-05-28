package pl.lodz.p.michalsosn.domain.sound.transform;

import org.junit.Test;
import pl.lodz.p.michalsosn.domain.sound.filter.Filter;
import pl.lodz.p.michalsosn.domain.sound.sound.Sound;
import pl.lodz.p.michalsosn.io.ResourceSet;
import pl.lodz.p.michalsosn.io.SoundIO;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static pl.lodz.p.michalsosn.domain.sound.transform.Convolutions.convolveLinearOverlapAdd;
import static pl.lodz.p.michalsosn.domain.sound.transform.Equalizers.*;

/**
 * @author Michał Sośnicki
 */
public class EqualizersTest {

    @Test
    public void testEqualize() throws Exception {
        try (Stream<Path> resources = ResourceSet.listResources(ResourceSet.CHIRP)) {
            // given
            final Sound sound = SoundIO.readSound(resources.findAny().get());
            final Filter[] filterBase = filterBase(
                    sound.getSamplingTime(), 4096, Windows.Window.HAMMING, 20, 12,
                    DoubleStream.generate(() -> 0.0).limit(10).toArray()
            );

            // when
            Instant before = Instant.now();

            final Sound result1 = equalizeSequentially(filterBase, filter -> convolveLinearOverlapAdd(
                    Windows.Window.RECTANGULAR, 2048, 2048, filter
            )).apply(sound);

            Instant middle = Instant.now();

            final Sound result2 = convolveLinearOverlapAdd(
                    Windows.Window.RECTANGULAR, 2048, 2048, joinBase(filterBase, 2048),
                    filterBase[0].getNegativeLength(), filterBase[0].getPositiveLength()
            ).andThen(Conversions::toSound).apply(sound);

            Instant after = Instant.now();

            // then
            System.out.println(Duration.between(before, middle) + " and " + Duration.between(middle, after));

            System.out.println(result1);
            System.out.println(result2);
            assertThat(result1.values().toArray(), is(result2.values().toArray()));
        }
    }

}
package pl.lodz.p.michalsosn.domain.sound.transform;

import org.junit.Test;
import pl.lodz.p.michalsosn.domain.sound.sound.Sound;
import pl.lodz.p.michalsosn.io.ResourceSet;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Stream;

import static pl.lodz.p.michalsosn.domain.sound.transform.BasicFrequencyAnalysis.findByAutocorrelation;
import static pl.lodz.p.michalsosn.domain.sound.transform.BasicFrequencyAnalysis.findByCepstrum;
import static pl.lodz.p.michalsosn.domain.sound.transform.Windows.hann;
import static pl.lodz.p.michalsosn.io.SoundIO.readSound;

/**
 * @author Michał Sośnicki
 */
public class BasicFrequencyAnalysisTest {

    private static final double DEFAULT_THRESHOLD = 0.9;

    @Test
    public void findBasicFrequency() throws Exception {
        try (Stream<Path> paths = ResourceSet.listResources(ResourceSet.SOUNDS)) {
            paths.forEach(path -> {
                try {
                    int pathLength = path.getNameCount();
                    String fileName = path.subpath(pathLength - 2, pathLength).toString();

                    Sound sound = SampleOps.shorten(8192, 512).apply(readSound(path));
                    double result1 = findByAutocorrelation(DEFAULT_THRESHOLD)
                            .apply(sound)
                            .getPitch().orElse(Double.NaN);
                    double result2 = findByAutocorrelation(DEFAULT_THRESHOLD)
                            .compose(hann())
                            .apply(sound)
                            .getPitch().orElse(Double.NaN);
                    double result3 = findByCepstrum(DEFAULT_THRESHOLD)
                            .apply(sound)
                            .getPitch().orElse(Double.NaN);
                    double result4 = findByCepstrum(DEFAULT_THRESHOLD)
                            .compose(hann())
                            .apply(sound)
                            .getPitch().orElse(Double.NaN);

                    if (Arrays.asList(result2, result3, result4).stream()
                            .anyMatch(result -> result != result1)) {
                        System.out.print("\\rowfont{\\bf} ");
                    }
                    System.out.printf("%s & %.2f & %.2f & %.2f & %.2f\n\\\\\n",
                                      fileName, result1, result2, result3, result4);
                } catch (Exception ex) {
                    throw new AssertionError(ex);
                }
            });
        }

    }

}
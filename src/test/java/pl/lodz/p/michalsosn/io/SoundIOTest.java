package pl.lodz.p.michalsosn.io;

import org.junit.Test;
import pl.lodz.p.michalsosn.domain.sound.sound.Sound;

import java.nio.file.Path;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static pl.lodz.p.michalsosn.io.SoundIO.*;

/**
 * @author Michał Sośnicki
 */
public class SoundIOTest {

    @Test
    public void testPlaySound() throws Exception {
        try (Stream<Path> paths = ResourceSet.listResources(ResourceSet.SOUNDS)) {
            paths.forEach(path -> {
                try {
                    System.out.println("Test playing: " + path);
                    Sound sound = readSound(path);
                    play(sound);
                } catch (Exception ex) {
                    throw new AssertionError(ex);
                }
            });
        }
    }

    @Test
    public void testReadWriteSoundFile() throws Exception {
        try (Stream<Path> paths = ResourceSet.listResources(ResourceSet.SOUNDS)) {
            paths.forEach(path -> {
                try {
                    System.out.println("Test writing: " + path);
                    Sound sound = readSound(path);
                    System.out.println(sound);

                    Path writePath = ResourceSet.tempResource();
//                    Path writePath = Paths.get("/home/michal/dupa.wav");
                    writeSound(sound, writePath, AudioType.WAVE);

                    Sound recovered = readSound(writePath);

                    assertThat(recovered, is(sound));
                } catch (Exception ex) {
                    throw new AssertionError(ex);
                }
            });
        }
    }

    @Test
    public void testReadWriteSoundByteArray() throws Exception {
        try (Stream<Path> paths = ResourceSet.listResources(ResourceSet.SOUNDS)) {
            paths.forEach(path -> {
                try {
                    System.out.println("Test writing: " + path);
                    Sound sound = readSound(path);
                    System.out.println(sound);

                    byte[] bytes = writeSound(sound, AudioType.WAVE);

                    Sound recovered = readSound(bytes);

                    assertThat(recovered, is(sound));
                } catch (Exception ex) {
                    throw new AssertionError(ex);
                }
            });
        }
    }

}
package pl.lodz.p.michalsosn.domain.image.transform;

import org.junit.Test;
import pl.lodz.p.michalsosn.domain.image.channel.BufferChannel;
import pl.lodz.p.michalsosn.domain.image.channel.Channel;
import pl.lodz.p.michalsosn.domain.image.channel.Image;
import pl.lodz.p.michalsosn.io.BufferedImageIO;
import pl.lodz.p.michalsosn.io.ImageSet;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static pl.lodz.p.michalsosn.domain.Lift.lift;

/**
 * @author Michał Sośnicki
 */
public class ChannelOpsTest {

    @Test
    public void testConvolution2d() throws Exception {
        Channel first = new BufferChannel(new int[][]{
                {50, 100, 50}, {100, 150, 100}, {100, 100, 100}
        });
        Kernel second = Kernel.normalized(new double[][]{
                {1, 2}, {1, -1}
        });

        int[][] convolution = ChannelOps.convolution(second, false).apply(first).copyValues();

        int[][] expected = {{61, 91, 101, 71}, {81, 131, 121, 81}, {91, 121, 101, 71}, {71, 51, 51, 31}};

        assertThat(convolution, is(expected));
    }

    @Test
    public void testConvolution2dReversed() throws Exception {
        Channel first = new BufferChannel(new int[][]{
                {100, 150}, {100, 0}
        });
        Kernel second = Kernel.normalized(new double[][]{
                {1, 2, 1}, {2, 3, 2}, {2, 2, 2}
        });

        int[][] convolution = ChannelOps.convolution(second, false).apply(first).copyValues();

        int[][] expected = {{6, 21, 24, 9}, {18, 47, 44, 18}, {24, 47, 41, 18}, {12, 12, 12, 0}};

        assertThat(convolution, is(expected));
    }

    @Test
    public void testConvolution1dHomework1() throws Exception {
        Channel first = new BufferChannel(new int[][]{ {1, 2, -1, 1} });
        Kernel second = Kernel.unsafe(new double[][]{ {2, 1, 1, 3} });

        int[][] convolution = ChannelOps.convolution(second, false).apply(first).copyValues();

        int[][] expected = { {2, 5, 1, 6, 6, -2, 3} };

        assertThat(convolution, is(expected));
    }

    @Test
    public void testConvolution1dHomework2() throws Exception {
        Channel first = new BufferChannel(new int[][]{ {1, 2, -1} });
        Kernel second = Kernel.unsafe(new double[][]{ {2, 1, 1, 3} });

        int[][] convolution = ChannelOps.convolution(second, false).apply(first).copyValues();

        int[][] expected = { {2, 5, 1, 4, 5, -3} };

        assertThat(convolution, is(expected));
    }

    @Test
    public void testConvolution1dHomework1Vertical() throws Exception {
        Channel first = new BufferChannel(new int[][]{ {1}, {2}, {-1}, {1} });
        Kernel second = Kernel.unsafe(new double[][]{ {2}, {1}, {1}, {3} });

        int[][] convolution = ChannelOps.convolution(second, false).apply(first).copyValues();

        int[][] expected = { {2}, {5}, {1}, {6}, {6}, {-2}, {3} };

        assertThat(convolution, is(expected));
    }

    @Test
    public void testOpsDontCrash() throws Exception {
        List<UnaryOperator<Image>> channelOperations = Arrays.asList(
                lift(ChannelOps.convolution(Kernel.normalized(new double[][]{{1, 1}, {1, 1}}), false)),
                lift(ChannelOps.kirschOperator())
        );

        try (Stream<Path> paths = ImageSet.listImages(ImageSet.ALL)) {
            paths.forEach(path -> {
                try {
                    Image image = BufferedImageIO.readImage(path);
                    channelOperations.forEach(operation -> operation.apply(image));
                } catch (IOException ex) {
                    throw new AssertionError("IO operation failed", ex);
                }
            });
        }
    }

}
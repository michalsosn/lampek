package pl.lodz.p.michalsosn.image.transform;

import org.junit.Test;
import pl.lodz.p.michalsosn.image.channel.BufferChannel;
import pl.lodz.p.michalsosn.image.channel.Channel;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author Michał Sośnicki
 */
public class ChannelOpsTest {

    @Test
    public void testConvolution2d() throws Exception {
        Channel first = new BufferChannel(new int[][]{
                {1, 2, 1}, {2, 3, 2}, {2, 2, 2}
        });
        Channel second = new BufferChannel(new int[][]{
                {1, 2}, {1, -1}
        });

        int[][] convolution = ChannelOps.convolution(second).apply(first).copyValues();

        int[][] expected = {{1, 4, 5, 2}, {3, 8, 7, 3}, {4, 7, 5, 2}, {2, 0, 0, -2}};

        assertThat(convolution, is(expected));
    }

    @Test
    public void testConvolution2dCommutative() throws Exception {
        Channel first = new BufferChannel(new int[][]{
                {1, 2}, {1, -1}
        });
        Channel second = new BufferChannel(new int[][]{
                {1, 2, 1}, {2, 3, 2}, {2, 2, 2}
        });

        int[][] convolution = ChannelOps.convolution(second).apply(first).copyValues();

        int[][] expected = {{1, 4, 5, 2}, {3, 8, 7, 3}, {4, 7, 5, 2}, {2, 0, 0, -2}};

        assertThat(convolution, is(expected));
    }

    @Test
    public void testConvolution1dHomework1() throws Exception {
        Channel first = new BufferChannel(new int[][]{ {1, 2, -1, 1} });
        Channel second = new BufferChannel(new int[][]{ {2, 1, 1, 3} });

        int[][] convolution = ChannelOps.convolution(second).apply(first).copyValues();

        int[][] expected = { {2, 5, 1, 6, 6, -2, 3} };

        assertThat(convolution, is(expected));
    }

    @Test
    public void testConvolution1dHomework2() throws Exception {
        Channel first = new BufferChannel(new int[][]{ {1, 2, -1} });
        Channel second = new BufferChannel(new int[][]{ {2, 1, 1, 3} });

        int[][] convolution = ChannelOps.convolution(second).apply(first).copyValues();

        int[][] expected = { {2, 5, 1, 4, 5, -3} };

        assertThat(convolution, is(expected));
    }

    @Test
    public void testConvolution1dHomework1Vertical() throws Exception {
        Channel first = new BufferChannel(new int[][]{ {1}, {2}, {-1}, {1} });
        Channel second = new BufferChannel(new int[][]{ {2}, {1}, {1}, {3} });

        int[][] convolution = ChannelOps.convolution(second).apply(first).copyValues();

        int[][] expected = { {2}, {5}, {1}, {6}, {6}, {-2}, {3} };

        assertThat(convolution, is(expected));
    }
}
package pl.lodz.p.michalsosn.domain.image.transform.segmentation;

import pl.lodz.p.michalsosn.domain.Lift;
import pl.lodz.p.michalsosn.domain.image.Size2d;
import pl.lodz.p.michalsosn.domain.image.channel.Channel;

import java.util.Arrays;
import java.util.function.IntBinaryOperator;
import java.util.function.UnaryOperator;

/**
 * @author Michał Sośnicki
 */
public final class Mask implements Size2d, Lift<UnaryOperator<Boolean>, Mask> {

    private final boolean[][] mask;

    public Mask(boolean[][] mask) {
        int height = mask.length;
        if (height != 0) {
            int width = mask[0].length;
            for (int y = 1; y < height; y++) {
                if (mask[y].length != width) {
                    throw new IllegalArgumentException(
                            "Mask rows must have equal lengths"
                    );
                }
            }
        }

        this.mask = mask;
    }

    @Override
    public int getHeight() {
        return mask.length;
    }

    @Override
    public int getWidth() {
        if (mask.length == 0) {
            return 0;
        }
        return mask[0].length;
    }

    public boolean isMasked(int y, int x) {
        return mask[y][x];
    }

    public boolean[][] copyMask() {
        int height = getHeight();
        boolean[][] newMask = new boolean[height][];
        for (int y = 0; y < height; y++) {
            newMask[y] = Arrays.copyOf(mask[y], mask[y].length);
        }
        return newMask;
    }

    public UnaryOperator<Channel> toOperator() {
        return channel -> {
            if (!isEqualSize(channel)) {
                throw new IllegalArgumentException("Mask and channel differ in size");
            }

            IntBinaryOperator maskFunction = (y, x) ->
                    mask[y][x] ? channel.getValue(y, x) : 0;

            return channel.constructSimilar(getHeight(), getWidth(), maskFunction);
        };
    }

    public int maskedCount() {
        int height = getHeight();
        int width = getWidth();
        int count = 0;
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                if (mask[y][x]) {
                    count += 1;
                }
            }
        }
        return count;
    }

    @Override
    public Mask map(UnaryOperator<Boolean> maskMapper) {
        int height = getHeight();
        int width = getWidth();

        boolean[][] newMask = new boolean[height][width];

        forEach((y, x) ->
                newMask[y][x] = maskMapper.apply(mask[y][x])
        );

        return new Mask(newMask);
    }
}

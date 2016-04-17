package pl.lodz.p.michalsosn.domain.image.transform.segmentation;

import pl.lodz.p.michalsosn.domain.image.Size2d;
import pl.lodz.p.michalsosn.domain.util.IntBiConsumer;

import java.util.stream.BaseStream;

/**
 * @author Michał Sośnicki
 */
public interface SplitRegion<T, S extends BaseStream<T, S>> extends Region<T, S>, Size2d {
    SplitRegion<T, S> subRegion(SubRegion subRegion);
    int getX();
    int getY();

    default void forEach(IntBiConsumer consumer) {
        int height = getHeight();
        int width = getWidth();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                consumer.accept(getY() + y, getX() + x);
            }
        }
    }
}


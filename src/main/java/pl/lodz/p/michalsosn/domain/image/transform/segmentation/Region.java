package pl.lodz.p.michalsosn.domain.image.transform.segmentation;

import java.util.stream.BaseStream;

/**
 * @author Michał Sośnicki
 */
public interface Region<T, S extends BaseStream<T, S>> {
    S values();
}

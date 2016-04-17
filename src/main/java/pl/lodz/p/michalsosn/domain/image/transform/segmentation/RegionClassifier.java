package pl.lodz.p.michalsosn.domain.image.transform.segmentation;

import java.util.stream.BaseStream;

/**
 * @author Michał Sośnicki
 */
public interface RegionClassifier<T, S extends BaseStream<T, S>> {
    boolean checkUniform(Region<T, S> region);
}

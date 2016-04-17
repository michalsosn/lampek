package pl.lodz.p.michalsosn.domain.image.transform.segmentation;

import java.util.DoubleSummaryStatistics;
import java.util.stream.DoubleStream;

/**
 * @author Michał Sośnicki
 */
public final class DoubleMaxRangeClassifier
        implements RegionClassifier<Double, DoubleStream> {
    private final int maxRange;

    public DoubleMaxRangeClassifier(int maxRange) {
        this.maxRange = maxRange;
    }

    @Override
    public boolean checkUniform(Region<Double, DoubleStream> region) {
        DoubleSummaryStatistics statistics = region.values().summaryStatistics();
        return statistics.getMax() - statistics.getMin() <= maxRange;
    }
}


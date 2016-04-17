package pl.lodz.p.michalsosn.domain.image.transform.segmentation;

import java.util.OptionalDouble;
import java.util.stream.DoubleStream;

/**
 * @author Michał Sośnicki
 */
public final class DoubleMaxStdDevClassifier
        implements RegionClassifier<Double, DoubleStream> {
    private final double maxStdDev;

    public DoubleMaxStdDevClassifier(double maxStdDev) {
        this.maxStdDev = maxStdDev;
    }

    @Override
    public boolean checkUniform(Region<Double, DoubleStream> region) {
        OptionalDouble optionalAverage = region.values().average();
        if (!optionalAverage.isPresent()) {
            return true;
        }
        double average = optionalAverage.getAsDouble();
        double avgOfSquares = region.values().map(v -> v * v).average().getAsDouble();
        double stdDev = Math.sqrt(avgOfSquares - average * average);

        return stdDev <= maxStdDev;
    }
}



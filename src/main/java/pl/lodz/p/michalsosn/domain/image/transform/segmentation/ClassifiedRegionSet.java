package pl.lodz.p.michalsosn.domain.image.transform.segmentation;

import pl.lodz.p.michalsosn.domain.util.Record;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.stream.BaseStream;

/**
 * @author Michał Sośnicki
 */
public final class ClassifiedRegionSet<T, S extends BaseStream<T, S>>
        implements Region<T, S>  {

    private final SplitRegion<T, S> region;
    private final BinaryOperator<S> merger;
    private Record<Set<SplitRegion<T, S>>> classRegions;

    public ClassifiedRegionSet(SplitRegion<T, S> region, BinaryOperator<S> merger) {
        this.region = region;
        this.merger = merger;
        this.classRegions = new Record<>(new HashSet<>());
        this.classRegions.get().add(region);
    }

    @Override
    public S values() {
        return classRegions.get().stream().map(Region::values).reduce(merger).get();
    }

    public static <T, S extends BaseStream<T, S>> void merge(
            ClassifiedRegionSet<T, S> first, ClassifiedRegionSet<T, S> second
    ) {
        Record.merge(first.classRegions, second.classRegions,
                (setA, setB) -> {
                    setA.addAll(setB);
                    return setA;
                }
        );
    }

    public SplitRegion<T, S> getRegion() {
        return region;
    }

    public Record<Set<SplitRegion<T, S>>> getClassRegions() {
        return classRegions;
    }
}


package pl.lodz.p.michalsosn.domain.util;

import java.util.function.BinaryOperator;

/**
 * @author Michał Sośnicki
 */
public final class Record<T> {

    // invariant: value == null || target == null
    private Record<T> target;
    private T value;

    public Record(T value) {
        this.value = value;
    }

    public T get() {
        return resolve().value;
    }

    public void set(T value) {
        resolve().value = value;
    }

    public static <T> void merge(Record<T> first, Record<T> second,
                                 BinaryOperator<T> merger) {
        Record<T> firstResolved = first.resolve();
        Record<T> secondResolved = second.resolve();
        if (firstResolved != secondResolved) {
            firstResolved.value = merger.apply(firstResolved.value,
                                               secondResolved.value);
            secondResolved.target = firstResolved;
            secondResolved.value = null;
        }
    }

    public static <T> boolean same(Record<T> first, Record<T> second) {
        return first.resolve() == second.resolve();
    }

    private Record<T> resolve() {
        if (target != null) {
            target = target.resolve();
            return target;
        }
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Record<?> record = (Record<?>) o;

        return resolve() == record.resolve();
    }

    @Override
    public int hashCode() {
        T currentValue = resolve().get();
        return currentValue != null ? currentValue.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Record{"
             + "target=" + target
             + ", value=" + value
             + '}';
    }

}

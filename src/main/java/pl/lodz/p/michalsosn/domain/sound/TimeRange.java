package pl.lodz.p.michalsosn.domain.sound;

/**
 * @author Michał Sośnicki
 */
public final class TimeRange {

    private final double duration;

    private TimeRange(double duration) {
        this.duration = duration;
    }

    public static TimeRange ofDuration(double duration) {
        return new TimeRange(duration);
    }

    public static TimeRange ofFrequency(double frequency) {
        return new TimeRange(1.0 / frequency);
    }

    public double getDuration() {
        return duration;
    }

    public double getFrequency() {
        return 1.0 / duration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TimeRange timeRange = (TimeRange) o;

        return Double.compare(timeRange.duration, duration) == 0;

    }

    @Override
    public int hashCode() {
        long temp = Double.doubleToLongBits(duration);
        return (int) (temp ^ (temp >>> 32));
    }

    @Override
    public String toString() {
        return "TimeRange{"
             + "duration=" + duration
             + '}';
    }
}

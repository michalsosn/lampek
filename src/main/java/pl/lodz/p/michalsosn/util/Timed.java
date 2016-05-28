package pl.lodz.p.michalsosn.util;

import java.time.Duration;
import java.time.Instant;

/**
 * @author Michał Sośnicki
 */
public final class Timed implements Runnable {

    private final Runnable runnable;
    private Instant before;
    private Instant after;

    public Timed(Runnable runnable) {
        this.runnable = runnable;
    }

    @Override
    public void run() {
        before = Instant.now();
        runnable.run();
        after = Instant.now();
    }

    public Instant getBefore() {
        return before;
    }

    public Instant getAfter() {
        return after;
    }

    public Duration getBetween() {
        return Duration.between(before, after);
    }

    public static Duration timed(Runnable runnable) {
        Instant before = Instant.now();
        runnable.run();
        Instant after = Instant.now();
        return Duration.between(before, after);
    }

}

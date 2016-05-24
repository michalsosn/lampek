package pl.lodz.p.michalsosn;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.Random;

/**
 * @author Michał Sośnicki
 */
public class RandomRule implements TestRule {

    private static class RandomStatement extends Statement {

        private final Statement statement;
        private final long seed;

        public RandomStatement(Statement statement, long seed) {
            this.statement = statement;
            this.seed = seed;
        }

        @Override
        public void evaluate() throws Throwable {
            try {
                statement.evaluate();
            } catch (Throwable t) {
                System.err.println("Test failed with seed: " + seed);
                throw t;
            }
        }

    }

    private Random random;

    @Override
    public Statement apply(Statement statement, Description description) {
        Seed seedAnnotation = description.getAnnotation(Seed.class);
        long seed;
        if (seedAnnotation != null) {
            seed = seedAnnotation.value();
        } else {
            seed = new Random().nextLong();
        }
        random = new Random(seed);
        return new RandomStatement(statement, seed);
    }

    public Random getRandom() {
        return random;
    }

}

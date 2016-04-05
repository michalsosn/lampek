package pl.lodz.p.michalsosn.domain.utils;

import java.util.function.Consumer;

/**
 * @author Michał Sośnicki
 */
@FunctionalInterface
public interface AudaciousConsumer<T> {

    void acceptAudaciously(T t) throws Exception;

    final class AudaciousConsumerAdapter<T> implements AudaciousConsumer<T>,
                                                       Consumer<T> {

        private final AudaciousConsumer<T> behaviour;
        private Exception exception;

        public AudaciousConsumerAdapter(AudaciousConsumer<T> behaviour) {
            this.behaviour = behaviour;
        }

        @Override
        public void acceptAudaciously(T t) throws Exception {
            behaviour.acceptAudaciously(t);
        }

        @Override
        public void accept(T t) {
            try {
                acceptAudaciously(t);
            } catch (Exception ex) {
                exception = ex;
            }
        }

        public Exception getException() {
            return exception;
        }

        public void rethrow() throws Exception {
            if (exception != null) {
                throw exception;
            }
        }
    }

}

package pl.lodz.p.michalsosn.rest.support;

/**
 * @author Michał Sośnicki
 */
public class OperationStatusAttachment<T> {
    private final boolean done;
    private final boolean failed;
    private final String type;
    private final T payload;

    public OperationStatusAttachment(boolean done, boolean failed,
                                     String type, T payload) {
        this.done = done;
        this.failed = failed;
        this.type = type;
        this.payload = payload;
    }

    public boolean isDone() {
        return done;
    }

    public boolean isFailed() {
        return failed;
    }

    public String getType() {
        return type;
    }

    public T getPayload() {
        return payload;
    }
}


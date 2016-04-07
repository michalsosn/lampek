package pl.lodz.p.michalsosn.rest.support;

/**
 * @author Michał Sośnicki
 */
public class OperationSummaryAttachment<T> {
    private final boolean done;
    private final boolean failed;
    private final String type;
    private final String description;
    private final T payload;

    public OperationSummaryAttachment(boolean done, boolean failed,
                                      String type, String description,
                                      T payload) {
        this.done = done;
        this.failed = failed;
        this.type = type;
        this.description = description;
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

    public String getDescription() {
        return description;
    }

    public T getPayload() {
        return payload;
    }
}


package pl.lodz.p.michalsosn.rest.support;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.hateoas.ResourceSupport;
import pl.lodz.p.michalsosn.rest.OperationRestController;
import pl.lodz.p.michalsosn.rest.ResultRestController;
import pl.lodz.p.michalsosn.specification.OperationRequest;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

/**
 * @author Michał Sośnicki
 */
public class OperationRequestSupport extends ResourceSupport {

    private final OperationRequest operationRequest;
    private final long operationId;
    private final boolean done;
    private final boolean failed;

    public OperationRequestSupport(String username,
            OperationStatusAttachment<OperationRequest> operationRequest,
            String processName, long operationId
    ) {
        this.operationRequest = operationRequest.getPayload();
        this.operationId = operationId;
        this.done = operationRequest.isDone();
        this.failed = operationRequest.isFailed();

        add(linkTo(methodOn(OperationRestController.class)
                .retrieveRequest(username, processName, operationId))
                .withSelfRel());
        add(linkTo(methodOn(OperationRestController.class)
                .listOperations(username, processName))
                .withRel("operations"));
        add(linkTo(methodOn(ResultRestController.class)
                .listResults(username, processName, operationId))
                .withRel("results"));
    }

    public OperationRequest getOperationRequest() {
        return operationRequest;
    }

    @JsonProperty("id")
    public long getOperationId() {
        return operationId;
    }

    public boolean isDone() {
        return done;
    }

    public boolean isFailed() {
        return failed;
    }
}

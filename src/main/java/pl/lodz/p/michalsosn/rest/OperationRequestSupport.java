package pl.lodz.p.michalsosn.rest;

import org.springframework.hateoas.ResourceSupport;
import pl.lodz.p.michalsosn.entities.specification.OperationRequest;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

/**
 * @author Michał Sośnicki
 */
public class OperationRequestSupport extends ResourceSupport {

    private final OperationRequest operationRequest;
    private final boolean done;
    private final boolean failed;

    public OperationRequestSupport(
            OperationStatusAttachment<OperationRequest> operationRequest,
            String processName, long operationId
    ) {
        this.operationRequest = operationRequest.getPayload();
        this.done = operationRequest.isDone();
        this.failed = operationRequest.isFailed();

        add(linkTo(methodOn(OperationRestController.class)
                .retrieveRequest(processName, operationId, null))
                .withSelfRel());
        add(linkTo(methodOn(OperationRestController.class)
                .listOperations(processName, null))
                .withRel("operations"));
        add(linkTo(methodOn(ResultRestController.class)
                .listResults(processName, operationId, null))
                .withRel("results"));
    }

    public OperationRequest getOperationRequest() {
        return operationRequest;
    }

    public boolean isDone() {
        return done;
    }

    public boolean isFailed() {
        return failed;
    }
}

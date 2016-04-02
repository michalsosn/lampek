package pl.lodz.p.michalsosn.rest;

import org.springframework.hateoas.ResourceSupport;
import pl.lodz.p.michalsosn.service.OperationService.OperationStatusAttachment;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

/**
 * @author Michał Sośnicki
 */
public class OperationListSupport extends ResourceSupport {

    private List<OperationIdSupport> idList;

    public static class OperationIdSupport extends ResourceSupport {
        private final long operationId;
        private final boolean done;
        private final boolean failed;

        public OperationIdSupport(OperationStatusAttachment<Long> operationId,
                                  String processName) {
            this.operationId = operationId.getPayload();
            this.done = operationId.isDone();
            this.failed = operationId.isFailed();
            add(linkTo(methodOn(OperationRestController.class)
                    .retrieveRequest(processName, this.operationId, null))
                    .withSelfRel());
            add(linkTo(methodOn(ProcessRestController.class)
                    .getProcessEntity(processName, null))
                    .withRel("process"));
        }

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

    public OperationListSupport(List<OperationStatusAttachment<Long>> idList,
                                String processName) {
        this.idList = idList.stream()
                .map(id -> new OperationIdSupport(id, processName))
                .collect(Collectors.toList());
        add(linkTo(methodOn(OperationRestController.class)
                .listOperations(processName, null))
                .withSelfRel());
    }

    public List<OperationIdSupport> getIdList() {
        return idList;
    }
}

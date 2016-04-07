package pl.lodz.p.michalsosn.rest.support;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.hateoas.ResourceSupport;
import pl.lodz.p.michalsosn.rest.OperationRestController;
import pl.lodz.p.michalsosn.rest.ProcessRestController;

import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

/**
 * @author Michał Sośnicki
 */
public class OperationListSupport extends ResourceSupport {

    private List<OperationIdSupport> idList;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class OperationIdSupport extends ResourceSupport {
        private final long operationId;
        private final boolean done;
        private final boolean failed;
        private final String type;
        private final String description;

        public OperationIdSupport(String username,
                                  OperationSummaryAttachment<Long> operationId,
                                  String processName) {
            this.operationId = operationId.getPayload();
            this.done = operationId.isDone();
            this.failed = operationId.isFailed();
            this.type = operationId.getType();
            this.description = operationId.getDescription();
            add(linkTo(methodOn(OperationRestController.class)
                    .retrieveRequest(username, processName, this.operationId))
                    .withSelfRel());
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

        public String getType() {
            return type;
        }

        public String getDescription() {
            return description;
        }
    }

    public OperationListSupport(String username,
                                List<OperationSummaryAttachment<Long>> idList,
                                String processName) {
        this.idList = idList.stream()
                .map(id -> new OperationIdSupport(username, id, processName))
                .collect(Collectors.toList());
        add(linkTo(methodOn(OperationRestController.class)
                .listOperations(username, processName))
                .withSelfRel());
        add(linkTo(methodOn(ProcessRestController.class)
                .getProcessEntity(username, processName))
                .withRel("process"));
    }

    public List<OperationIdSupport> getIdList() {
        return idList;
    }
}

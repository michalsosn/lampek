package pl.lodz.p.michalsosn.rest.support;

import org.springframework.hateoas.ResourceSupport;
import pl.lodz.p.michalsosn.entities.ResultEntity;
import pl.lodz.p.michalsosn.rest.OperationRestController;
import pl.lodz.p.michalsosn.rest.ResultRestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

/**
 * @author Michał Sośnicki
 */
public class ResultListSupport extends ResourceSupport {

    private final boolean done;
    private final boolean failed;
    private final String type;
    private final List<ResultEntitySupport> resultList;

    public ResultListSupport(String username,
            OperationStatusAttachment<Map<String, ResultEntity>> results,
            String processName, long operationId
    ) {
        this.done = results.isDone();
        this.failed = results.isFailed();
        this.type = results.getType();
        this.resultList = new ArrayList<>();
        results.getPayload().forEach((role, result) ->
                resultList.add(new ResultEntitySupport(
                        username, role, result, processName, operationId
                ))
        );
        add(linkTo(methodOn(ResultRestController.class)
                .listResults(username, processName, operationId))
                .withSelfRel());
        add(linkTo(methodOn(OperationRestController.class)
                .retrieveRequest(username, processName, operationId))
                .withRel("operation"));
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

    public List<ResultEntitySupport> getResultList() {
        return resultList;
    }
}


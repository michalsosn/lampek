package pl.lodz.p.michalsosn.rest.support;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.hateoas.ResourceSupport;
import pl.lodz.p.michalsosn.entities.ResultEntity;
import pl.lodz.p.michalsosn.entities.ValueType;
import pl.lodz.p.michalsosn.rest.OperationRestController;
import pl.lodz.p.michalsosn.rest.ResultRestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;
import static pl.lodz.p.michalsosn.entities.ResultEntity.DoubleResultEntity;
import static pl.lodz.p.michalsosn.entities.ResultEntity.HistogramResultEntity;

/**
 * @author Michał Sośnicki
 */
public class ResultListSupport extends ResourceSupport {

    private final boolean done;
    private final boolean failed;
    private final String type;
    private final List<ResultEntitySupport> resultList;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ResultEntitySupport extends ResourceSupport {
        private final String role;
        private final ValueType type;
        private final Object value;

        public ResultEntitySupport(String username,
                                   String role, ResultEntity result,
                                   String processName, long operationId) {
            this.role = role;
            this.type = result.getType();

            switch (result.getType()) {
                case DOUBLE:
                    value = ((DoubleResultEntity) result).getValue();
                    break;
                case HISTOGRAM:
                    value = ((HistogramResultEntity) result).getHistogram();
                    break;
                case IMAGE:
                    value = null;
                    try {
                        add(linkTo(methodOn(ResultRestController.class)
                                .getResultAsPng(username, processName,
                                                operationId, role))
                                .withRel("image"));
                    } catch (IOException e) {
                        throw new IllegalStateException(
                                "it should never happen", e
                        );
                    }
                    break;
                default:
                    throw new IllegalArgumentException(
                            "Not supported value type"
                    );
            }

            add(linkTo(methodOn(ResultRestController.class)
                    .listResults(username, processName, operationId))
                    .withSelfRel());
        }

        public String getRole() {
            return role;
        }

        public ValueType getType() {
            return type;
        }

        public Object getValue() {
            return value;
        }
    }

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


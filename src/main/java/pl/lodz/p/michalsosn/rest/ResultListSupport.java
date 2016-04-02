package pl.lodz.p.michalsosn.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.hateoas.ResourceSupport;
import pl.lodz.p.michalsosn.entities.ResultEntity;
import pl.lodz.p.michalsosn.entities.ValueType;

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

    private final List<ResultEntitySupport> resultList;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ResultEntitySupport extends ResourceSupport {
        private final String role;
        private final ValueType type;
        private final Object value;

        public ResultEntitySupport(String role, ResultEntity result,
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
                                .getResultAsPng(processName, operationId,
                                                role, null))
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
                    .listResults(processName, operationId, null))
                    .withSelfRel());
            add(linkTo(methodOn(OperationRestController.class)
                    .retrieveRequest(processName, operationId, null))
                    .withRel("operation"));
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

    public ResultListSupport(Map<String, ResultEntity> results,
                                String processName, long operationId) {
        this.resultList = new ArrayList<>();
        results.forEach((role, result) ->
                resultList.add(new ResultEntitySupport(
                        role, result, processName, operationId
                ))
        );
        add(linkTo(methodOn(ResultRestController.class)
                .listResults(processName, operationId, null))
                .withSelfRel());
    }

    public List<ResultEntitySupport> getResultList() {
        return resultList;
    }
}


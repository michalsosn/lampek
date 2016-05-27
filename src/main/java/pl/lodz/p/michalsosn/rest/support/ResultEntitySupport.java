package pl.lodz.p.michalsosn.rest.support;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.hateoas.ResourceSupport;
import pl.lodz.p.michalsosn.entities.ResultEntity;
import pl.lodz.p.michalsosn.entities.ResultType;
import pl.lodz.p.michalsosn.rest.ResultRestController;

import java.io.IOException;
import java.util.Arrays;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;
import static pl.lodz.p.michalsosn.entities.ResultEntity.*;

/**
 * @author Michał Sośnicki
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResultEntitySupport extends ResourceSupport {
    private final String role;
    private final ResultType type;
    private final Object value;

    public ResultEntitySupport(
            String username, String role, ResultEntity result,
            String processName, long operationId
    ) {
        this.role = role;
        this.type = result.getType();

        try {
            switch (result.getType()) {
                case DOUBLE:
                    value = ((DoubleResultEntity) result).getValue();
                    break;
                case INTEGER:
                    value = ((IntegerResultEntity) result).getValue();
                    break;
                case IMAGE_HISTOGRAM:
                    value = ((ImageHistogramResultEntity) result).getHistogram();
                    break;
                case IMAGE:
                case IMAGE_SPECTRUM:
                case IMAGE_MASK:
                    value = null;
                    add(linkTo(methodOn(ResultRestController.class)
                            .getResultAsPng(username, processName, operationId, role))
                            .withRel("image"));
                    break;
                case SOUND:
                    value = new SoundChartPack(
                            (SoundResultEntity) result
                    );
                    add(linkTo(methodOn(ResultRestController.class)
                            .getResultAsWave(username, processName, operationId, role))
                            .withRel("sound"));
                    break;
                case SOUND_SPECTRUM:
                    value = new SoundSpectrumChartPack(
                            (SoundSpectrumResultEntity) result
                    );
                    break;
                case SIGNAL:
                    value = new SignalChartPack(
                            (SignalResultEntity) result
                    );
                    break;
                case SOUND_FILTER:
                    value = new SoundFilterChartPack(
                            (SoundFilterResultEntity) result
                    );
                    break;
                case NOTE_SEQUENCE:
                    value = Arrays.stream(
                            ((NoteSequenceResultEntity) result).getNotes()
                    ).map(NoteInfo::new).toArray();
                    break;
                default:
                    throw new IllegalArgumentException(
                            "Not supported value type"
                    );
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        add(linkTo(methodOn(ResultRestController.class)
                .listResults(username, processName, operationId))
                .withSelfRel());
    }

    public String getRole() {
        return role;
    }

    public ResultType getType() {
        return type;
    }

    public Object getValue() {
        return value;
    }
}



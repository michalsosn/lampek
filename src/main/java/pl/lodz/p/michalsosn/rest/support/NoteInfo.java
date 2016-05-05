package pl.lodz.p.michalsosn.rest.support;

import com.fasterxml.jackson.annotation.JsonInclude;
import pl.lodz.p.michalsosn.domain.sound.transform.Note;

/**
 * @author Michał Sośnicki
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NoteInfo {

    private final Double pitch;
    private final Integer amplitudeStart;
    private final Integer amplitudeEnd;
    private final double duration;

    public NoteInfo(Note note) {
        if (note.getPitch().isPresent()) {
            this.pitch = note.getPitch().getAsDouble();
            this.amplitudeStart = note.getAmplitudeStart().getAsInt();
            this.amplitudeEnd = note.getAmplitudeEnd().getAsInt();
        } else {
            this.pitch = null;
            this.amplitudeStart = null;
            this.amplitudeEnd = null;
        }
        this.duration = note.getDuration();
    }

    public Double getPitch() {
        return pitch;
    }

    public Integer getAmplitudeStart() {
        return amplitudeStart;
    }

    public Integer getAmplitudeEnd() {
        return amplitudeEnd;
    }

    public double getDuration() {
        return duration;
    }
}

package pl.lodz.p.michalsosn.rest.support;

import org.springframework.hateoas.ResourceSupport;
import pl.lodz.p.michalsosn.entities.SoundEntity;
import pl.lodz.p.michalsosn.rest.SoundRestController;

import java.io.IOException;
import java.time.Instant;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

/**
 * @author Michał Sośnicki
 */
public class SoundEntitySupport extends ResourceSupport {

    private final String name;
    private final Instant modificationTime;
    private final String account;

    public SoundEntitySupport(String username, SoundEntity soundEntity) {
        this.name = soundEntity.getName();
        this.modificationTime = soundEntity.getModificationTime();
        this.account = soundEntity.getAccount().getUsername();
        add(linkTo(methodOn(SoundRestController.class)
                .getSoundEntity(username, name))
                .withSelfRel());
        try {
            add(linkTo(methodOn(SoundRestController.class)
                    .getSoundAsWave(username, name))
                    .withRel("sound"));
        } catch (IOException e) {
            throw new IllegalStateException(
                    "it should never happen", e
            );
        }
        add(linkTo(methodOn(SoundRestController.class)
                .listSounds(username, null, null))
                .withRel("sounds"));
    }

    public String getName() {
        return name;
    }

    public Instant getModificationTime() {
        return modificationTime;
    }

    public String getAccount() {
        return account;
    }
}

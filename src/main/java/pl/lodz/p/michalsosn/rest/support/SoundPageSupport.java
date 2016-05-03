package pl.lodz.p.michalsosn.rest.support;

import org.springframework.data.domain.Page;
import org.springframework.hateoas.ResourceSupport;
import pl.lodz.p.michalsosn.entities.SoundEntity;
import pl.lodz.p.michalsosn.rest.SoundRestController;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

/**
 * @author Michał Sośnicki
 */
public class SoundPageSupport extends ResourceSupport {

    private final List<SoundItemSupport> soundList;
    private final int pageCount;

    public static class SoundItemSupport extends ResourceSupport {
        private final String name;
        private final Instant modificationTime;

        public SoundItemSupport(String username, SoundEntity sound) {
            this.name = sound.getName();
            this.modificationTime = sound.getModificationTime();
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
        }

        public String getName() {
            return name;
        }

        public Instant getModificationTime() {
            return modificationTime;
        }
    }

    public SoundPageSupport(String username, Page<SoundEntity> soundPage) {
        this.soundList = soundPage
                .map(sound -> new SoundItemSupport(username, sound))
                .getContent();
        this.pageCount = soundPage.getTotalPages();
        if (soundPage.hasPrevious()) {
            add(linkTo(methodOn(SoundRestController.class)
                    .listSounds(username, soundPage.getNumber() - 1,
                            soundPage.getSize()))
                    .withRel("previous"));
        }
        add(linkTo(methodOn(SoundRestController.class)
                .listSounds(username, soundPage.getNumber(), soundPage.getSize()))
                .withSelfRel());
        if (soundPage.hasNext()) {
            add(linkTo(methodOn(SoundRestController.class)
                    .listSounds(username, soundPage.getNumber() + 1,
                                soundPage.getSize()))
                    .withRel("next"));
        }
    }

    public List<SoundItemSupport> getSoundList() {
        return soundList;
    }

    public int getPageCount() {
        return pageCount;
    }
}

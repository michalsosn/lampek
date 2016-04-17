package pl.lodz.p.michalsosn.rest.support;

import org.springframework.hateoas.ResourceSupport;
import pl.lodz.p.michalsosn.entities.ImageEntity;
import pl.lodz.p.michalsosn.rest.ImageRestController;

import java.io.IOException;
import java.time.Instant;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

/**
 * @author Michał Sośnicki
 */
public class ImageEntitySupport extends ResourceSupport {

    private final String name;
    private final Instant modificationTime;
    private final String account;

    public ImageEntitySupport(String username, ImageEntity imageEntity) {
        this.name = imageEntity.getName();
        this.modificationTime = imageEntity.getModificationTime();
        this.account = imageEntity.getAccount().getUsername();
        add(linkTo(methodOn(ImageRestController.class)
                .getImageEntity(username, name))
                .withSelfRel());
        try {
            add(linkTo(methodOn(ImageRestController.class)
                    .getImageAsPng(username, name))
                    .withRel("image"));
        } catch (IOException e) {
            throw new IllegalStateException(
                    "it should never happen", e
            );
        }
        add(linkTo(methodOn(ImageRestController.class)
                .listImages(username, null, null))
                .withRel("images"));
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

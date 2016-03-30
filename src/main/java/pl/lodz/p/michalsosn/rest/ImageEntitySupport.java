package pl.lodz.p.michalsosn.rest;

import org.springframework.hateoas.ResourceSupport;
import pl.lodz.p.michalsosn.entities.ImageEntity;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

/**
 * @author Michał Sośnicki
 */
public class ImageEntitySupport extends ResourceSupport {

    private final String name;
    private final String account;

    public ImageEntitySupport(ImageEntity imageEntity) {
        this.name = imageEntity.getName();
        this.account = imageEntity.getAccount().getUsername();
        add(linkTo(methodOn(ImageRestController.class)
                .getImageEntity(name, null))
                .withSelfRel());
        add(linkTo(methodOn(ImageRestController.class)
                .listImages(null, null, null))
                .withRel("images"));
    }

    public String getName() {
        return name;
    }

    public String getAccount() {
        return account;
    }
}

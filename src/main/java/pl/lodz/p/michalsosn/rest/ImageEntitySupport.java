package pl.lodz.p.michalsosn.rest;

import org.springframework.hateoas.ResourceSupport;
import pl.lodz.p.michalsosn.entities.ImageEntity;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

/**
 * @author Michał Sośnicki
 */
public class ImageEntitySupport extends ResourceSupport {

    private ImageEntity imageEntity;

    public ImageEntitySupport(ImageEntity imageEntity) {
        this.imageEntity = imageEntity;
        String name = imageEntity.getName();
        add(linkTo(methodOn(ImageRestController.class)
                .getImageEntity(name, null))
                .withSelfRel());
        add(linkTo(methodOn(ImageRestController.class)
                .listImages(0, null))
                .withRel("images"));
    }

}

package pl.lodz.p.michalsosn.rest;

import org.springframework.data.domain.Page;
import org.springframework.hateoas.ResourceSupport;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

/**
 * @author Michał Sośnicki
 */
public class ImagePageSupport extends ResourceSupport {

    private final Page<ImageNameSupport> namePage;

    public static class ImageNameSupport extends ResourceSupport {
        private final String name;

        public ImageNameSupport(String name) {
            this.name = name;
            add(linkTo(methodOn(ImageRestController.class).getImageEntity(name, null)).withSelfRel());
        }

        public String getName() {
            return name;
        }
    }

    public ImagePageSupport(Page<String> namePage) {
        this.namePage = namePage.map(ImageNameSupport::new);
        add(linkTo(methodOn(ImageRestController.class).listImages(namePage.getNumber(), null)).withSelfRel());
        if (namePage.hasNext()) {
            add(linkTo(methodOn(ImageRestController.class)
                    .listImages(namePage.getNumber() + 1, null))
                    .withRel("next"));
        }
    }

    public Page<ImageNameSupport> getNamePage() {
        return namePage;
    }
}

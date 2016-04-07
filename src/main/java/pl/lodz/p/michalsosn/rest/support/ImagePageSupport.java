package pl.lodz.p.michalsosn.rest.support;

import org.springframework.data.domain.Page;
import org.springframework.hateoas.ResourceSupport;
import pl.lodz.p.michalsosn.rest.ImageRestController;

import java.io.IOException;
import java.util.List;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

/**
 * @author Michał Sośnicki
 */
public class ImagePageSupport extends ResourceSupport {

    private final List<ImageNameSupport> nameList;
    private final int pageCount;

    public static class ImageNameSupport extends ResourceSupport {
        private final String name;

        public ImageNameSupport(String username, String name) {
            this.name = name;
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
        }

        public String getName() {
            return name;
        }
    }

    public ImagePageSupport(String username, Page<String> namePage) {
        this.nameList = namePage
                .map(name -> new ImageNameSupport(username, name))
                .getContent();
        this.pageCount = namePage.getTotalPages();
        if (namePage.hasPrevious()) {
            add(linkTo(methodOn(ImageRestController.class)
                    .listImages(username, namePage.getNumber() - 1,
                            namePage.getSize()))
                    .withRel("previous"));
        }
        add(linkTo(methodOn(ImageRestController.class)
                .listImages(username, namePage.getNumber(), namePage.getSize()))
                .withSelfRel());
        if (namePage.hasNext()) {
            add(linkTo(methodOn(ImageRestController.class)
                    .listImages(username, namePage.getNumber() + 1,
                                namePage.getSize()))
                    .withRel("next"));
        }
    }

    public List<ImageNameSupport> getNameList() {
        return nameList;
    }

    public int getPageCount() {
        return pageCount;
    }
}

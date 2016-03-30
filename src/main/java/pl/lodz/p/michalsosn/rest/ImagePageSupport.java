package pl.lodz.p.michalsosn.rest;

import org.springframework.data.domain.Page;
import org.springframework.hateoas.ResourceSupport;

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

        public ImageNameSupport(String name) {
            this.name = name;
            add(linkTo(methodOn(ImageRestController.class)
                    .getImageEntity(name, null))
                    .withSelfRel());
        }

        public String getName() {
            return name;
        }
    }

    public ImagePageSupport(Page<String> namePage) {
        this.nameList = namePage.map(ImageNameSupport::new).getContent();
        this.pageCount = namePage.getTotalPages();
        add(linkTo(methodOn(ImageRestController.class)
                .listImages(namePage.getNumber(), namePage.getSize(), null))
                .withSelfRel());
        if (namePage.hasNext()) {
            add(linkTo(methodOn(ImageRestController.class)
                    .listImages(namePage.getNumber() + 1,
                                namePage.getSize(), null))
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

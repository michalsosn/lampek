package pl.lodz.p.michalsosn.rest.support;

import org.springframework.data.domain.Page;
import org.springframework.hateoas.ResourceSupport;
import pl.lodz.p.michalsosn.entities.ImageEntity;
import pl.lodz.p.michalsosn.rest.ImageRestController;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

/**
 * @author Michał Sośnicki
 */
public class ImagePageSupport extends ResourceSupport {

    private final List<ImageItemSupport> imageList;
    private final int pageCount;

    public static class ImageItemSupport extends ResourceSupport {
        private final String name;
        private final Instant modificationTime;

        public ImageItemSupport(String username, ImageEntity image) {
            this.name = image.getName();
            this.modificationTime = image.getModificationTime();
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

        public Instant getModificationTime() {
            return modificationTime;
        }
    }

    public ImagePageSupport(String username, Page<ImageEntity> imagePage) {
        this.imageList = imagePage
                .map(image -> new ImageItemSupport(username, image))
                .getContent();
        this.pageCount = imagePage.getTotalPages();
        if (imagePage.hasPrevious()) {
            add(linkTo(methodOn(ImageRestController.class)
                    .listImages(username, imagePage.getNumber() - 1,
                            imagePage.getSize()))
                    .withRel("previous"));
        }
        add(linkTo(methodOn(ImageRestController.class)
                .listImages(username, imagePage.getNumber(), imagePage.getSize()))
                .withSelfRel());
        if (imagePage.hasNext()) {
            add(linkTo(methodOn(ImageRestController.class)
                    .listImages(username, imagePage.getNumber() + 1,
                                imagePage.getSize()))
                    .withRel("next"));
        }
    }

    public List<ImageItemSupport> getImageList() {
        return imageList;
    }

    public int getPageCount() {
        return pageCount;
    }
}

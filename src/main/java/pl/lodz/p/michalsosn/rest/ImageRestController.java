package pl.lodz.p.michalsosn.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.lodz.p.michalsosn.entities.ImageEntity;
import pl.lodz.p.michalsosn.rest.support.ImageEntitySupport;
import pl.lodz.p.michalsosn.rest.support.ImagePageSupport;
import pl.lodz.p.michalsosn.service.ImageService;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Michał Sośnicki
 */
@RestController
@RequestMapping("/user/{username}/image")
public class ImageRestController {

    @Autowired
    private ImageService imageService;

    @RequestMapping(method = RequestMethod.GET)
    public ImagePageSupport listImages(
            @PathVariable String username,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size
    ) {
        Page<String> namePage
                = imageService.listImageNames(username, page, size);
        return new ImagePageSupport(username, namePage);
    }

    @RequestMapping(path = "/{name}", method = RequestMethod.GET)
    public ImageEntitySupport getImageEntity(
            @PathVariable String username,
            @PathVariable String name
    ) {
        ImageEntity imageEntity = imageService.getImageEntity(username, name);
        return new ImageEntitySupport(username, imageEntity);
    }

    @RequestMapping(path = "/{name}", method = RequestMethod.GET,
                    produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getImageAsPng(
            @PathVariable String username,
            @PathVariable String name
    ) throws IOException {
        byte[] imageData = imageService.getImageAsPng(username, name);
        return new ResponseEntity<>(imageData, HttpStatus.OK);
    }

    @RequestMapping(path = "/{name}", method = RequestMethod.PUT)
    public ResponseEntity replaceImage(
            @PathVariable String username,
            @PathVariable String name,
            @RequestParam("file") MultipartFile image
    ) throws IOException {
        InputStream imageStream = image.getInputStream();

        boolean found = imageService.replaceImage(username, name, imageStream);

        return ResponseEntity.status(
                found ? HttpStatus.NO_CONTENT : HttpStatus.CREATED
        ).build();
    }

    @RequestMapping(path = "/{name}", method = RequestMethod.DELETE)
    public ResponseEntity deleteImage(
            @PathVariable String username,
            @PathVariable String name
    ) {
        imageService.deleteImage(username, name);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

}

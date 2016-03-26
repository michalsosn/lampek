package pl.lodz.p.michalsosn.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.lodz.p.michalsosn.entities.ImageEntity;
import pl.lodz.p.michalsosn.service.ImageService;

import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;

/**
 * @author Michał Sośnicki
 */
@RestController
@RequestMapping("/images")
public class ImageRestController {

    private static final int PAGE_SIZE = 10;

    @Autowired
    private ImageService imageService;

    @RequestMapping(method = RequestMethod.GET)
    public ImagePageSupport listImages(
            @RequestParam(name = "page",defaultValue = "0") int page,
            Principal principal
    ) {
        String username = principal.getName();
        Page<String> namePage
                = imageService.listImageNames(username, page, PAGE_SIZE);
        return new ImagePageSupport(namePage);
    }

    @RequestMapping(method = RequestMethod.POST)
    public ImageEntitySupport uploadImage(
            @RequestParam("name") String name,
            @RequestParam("file") MultipartFile image,
            Principal principal
    ) throws IOException {
        String username = principal.getName();
        InputStream imageStream = image.getInputStream();
        ImageEntity imageEntity
                = imageService.uploadImage(username, name, imageStream);
        return new ImageEntitySupport(imageEntity);
    }

    @RequestMapping(path = "/{name}", method = RequestMethod.GET)
    public ImageEntitySupport getImageEntity(
            @PathVariable String name, Principal principal
    ) {
        String username = principal.getName();
        ImageEntity imageEntity = imageService.getImageEntity(username, name);
        return new ImageEntitySupport(imageEntity);
    }

    @RequestMapping(path = "/{name}", method = RequestMethod.GET,
                    produces = MediaType.IMAGE_PNG_VALUE) // + ".png" ?
    public ResponseEntity<byte[]> getImageAsPng(
            @PathVariable String name, Principal principal
    ) throws IOException {
        String username = principal.getName();
        byte[] imageData = imageService.getImageAsPng(username, name);
        return new ResponseEntity<>(imageData, HttpStatus.OK);
    }

    @RequestMapping(path = "/{name}", method = RequestMethod.PUT)
    public ResponseEntity replaceImage(
            @PathVariable String name,
            @RequestParam("file") MultipartFile image,
            Principal principal
    ) throws IOException {
        String username = principal.getName();
        InputStream imageStream = image.getInputStream();
        imageService.uploadImage(username, name, imageStream);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @RequestMapping(path = "/{name}", method = RequestMethod.DELETE)
    public ResponseEntity deleteImage(
            @PathVariable String name, Principal principal
    ) {
        String username = principal.getName();
        imageService.deleteImage(username, name);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

}

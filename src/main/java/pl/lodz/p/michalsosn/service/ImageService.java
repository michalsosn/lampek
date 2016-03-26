package pl.lodz.p.michalsosn.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.michalsosn.entities.AccountEntity;
import pl.lodz.p.michalsosn.entities.ImageEntity;
import pl.lodz.p.michalsosn.repository.AccountRepository;
import pl.lodz.p.michalsosn.repository.ImageRepository;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Michał Sośnicki
 */
@Service
@Transactional
public class ImageService {

    private final Logger log = LoggerFactory.getLogger(ImageService.class);

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private ImageRepository imageRepository;

    public ImageService() {
    }

    public Page<String> listImageNames(String username, int page,int size) {
        Pageable pageable = new PageRequest(page, size,
                                            Sort.Direction.ASC, "name");
        return imageRepository
                .findByAccountUsername(username, pageable)
                .map(ImageEntity::getName);
    }

    public ImageEntity getImageEntity(String username, String name) {
        return imageRepository
                .findByAccountUsernameAndName(username, name)
                .get();
    }

    public byte[] getImageAsPng(String username, String name)
            throws IOException {
        ImageEntity image = imageRepository
                .findByAccountUsernameAndName(username, name)
                .get();
        return image.getData();
    }

    public ImageEntity uploadImage(String username, String name,
                                   InputStream imageStream) throws IOException {
        AccountEntity account
                = accountRepository.findByUsername(username).get();
        BufferedImage bufferedImage = ImageIO.read(imageStream);
        ImageEntity imageEntity = new ImageEntity(name, bufferedImage, account);
        imageEntity = imageRepository.save(imageEntity);
        log.info("Image {} uploaded by {}.", name, username);
        return imageEntity;
    }

    public void deleteImage(String username, String name) {
        imageRepository.deleteByAccountUsernameAndName(username, name);
        log.info("Image {} deleted by {}.", name, username);
    }

}

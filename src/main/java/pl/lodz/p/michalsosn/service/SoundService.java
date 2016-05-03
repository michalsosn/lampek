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
import pl.lodz.p.michalsosn.entities.SoundEntity;
import pl.lodz.p.michalsosn.io.SoundIO;
import pl.lodz.p.michalsosn.repository.AccountRepository;
import pl.lodz.p.michalsosn.repository.SoundRepository;
import pl.lodz.p.michalsosn.security.OwnerOnly;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

/**
 * @author Michał Sośnicki
 */
@Service
@Transactional
public class SoundService {

    private final Logger log = LoggerFactory.getLogger(SoundService.class);

    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private SoundRepository soundRepository;

    public SoundService() {
    }

    public Page<SoundEntity> listSounds(String username, int page, int size) {
        Pageable pageRequest = new PageRequest(
                page, size, Sort.Direction.DESC, "modificationTime"
        );
        return soundRepository.findByAccountUsername(username, pageRequest);
    }

    public SoundEntity getSoundEntity(String username, String name) {
        return soundRepository
                .findByAccountUsernameAndName(username, name)
                .get();
    }

    public byte[] getSoundAsWave(String username, String name) throws IOException {
        SoundEntity sound = soundRepository
                .findByAccountUsernameAndName(username, name)
                .get();
        return sound.getData();
    }

    public byte[] getSoundAsFlac(String username, String name) throws IOException {
        SoundEntity sound = soundRepository
                .findByAccountUsernameAndName(username, name)
                .get();
        return SoundIO.convertAudio(sound.getData(), SoundIO.AudioType.FLAC);
    }

    @OwnerOnly
    public boolean replaceSound(
            String username, String name, InputStream soundStream
    ) throws IOException {
        AccountEntity account = accountRepository.findByUsername(username).get();

        Optional<SoundEntity> replaced = soundRepository
                .findByAccountUsernameAndName(username, name);
        boolean found = replaced.isPresent();

        SoundEntity soundEntity = replaced.orElseGet(SoundEntity::new);
        soundEntity.setName(name);
        soundEntity.setDataWithConversion(soundStream);
        soundEntity.setAccount(account);
        soundRepository.save(soundEntity);

        log.info("Sound {} {} by {}.",
                name, found ? "replaced" : "uploaded", username
        );
        return found;
    }

    @OwnerOnly
    public void deleteSound(String username, String name) {
        soundRepository.deleteByAccountUsernameAndName(username, name);
        log.info("Sound {} deleted by {}.", name, username);
    }

}

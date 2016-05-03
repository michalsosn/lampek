package pl.lodz.p.michalsosn.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pl.lodz.p.michalsosn.entities.SoundEntity;
import pl.lodz.p.michalsosn.rest.support.SoundEntitySupport;
import pl.lodz.p.michalsosn.rest.support.SoundPageSupport;
import pl.lodz.p.michalsosn.service.SoundService;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author Michał Sośnicki
 */
@RestController
@RequestMapping("/user/{username}/sound")
public class SoundRestController {

    @Autowired
    private SoundService soundService;

    @RequestMapping(method = RequestMethod.GET)
    public SoundPageSupport listSounds(
            @PathVariable String username,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size
    ) {
        Page<SoundEntity> soundPage
                = soundService.listSounds(username, page, size);
        return new SoundPageSupport(username, soundPage);
    }

    @RequestMapping(path = "/{name}", method = RequestMethod.GET)
    public SoundEntitySupport getSoundEntity(
            @PathVariable String username,
            @PathVariable String name
    ) {
        SoundEntity soundEntity = soundService.getSoundEntity(username, name);
        return new SoundEntitySupport(username, soundEntity);
    }

    @RequestMapping(path = "/{name}", method = RequestMethod.GET,
                    produces = "audio/x-wav")
    public ResponseEntity<byte[]> getSoundAsWave(
            @PathVariable String username,
            @PathVariable String name
    ) throws IOException {
        byte[] soundData = soundService.getSoundAsWave(username, name);
        return new ResponseEntity<>(soundData, HttpStatus.OK);
    }

    @RequestMapping(path = "/{name}", method = RequestMethod.GET,
                    produces = "audio/flac")
    public ResponseEntity<byte[]> getSoundAsFlac(
            @PathVariable String username,
            @PathVariable String name
    ) throws IOException {
        byte[] soundData = soundService.getSoundAsFlac(username, name);
        return new ResponseEntity<>(soundData, HttpStatus.OK);
    }

    @RequestMapping(path = "/{name}", method = RequestMethod.PUT)
    public ResponseEntity replaceSound(
            @PathVariable String username,
            @PathVariable String name,
            @RequestParam("file") MultipartFile sound
    ) throws IOException {
        InputStream soundStream = sound.getInputStream();

        boolean found = soundService.replaceSound(username, name, soundStream);

        return ResponseEntity.status(
                found ? HttpStatus.NO_CONTENT : HttpStatus.CREATED
        ).build();
    }

    @RequestMapping(path = "/{name}", method = RequestMethod.DELETE)
    public ResponseEntity deleteSound(
            @PathVariable String username,
            @PathVariable String name
    ) {
        soundService.deleteSound(username, name);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

}

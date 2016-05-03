package pl.lodz.p.michalsosn.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.michalsosn.entities.ResultEntity;
import pl.lodz.p.michalsosn.rest.support.OperationStatusAttachment;
import pl.lodz.p.michalsosn.rest.support.ResultListSupport;
import pl.lodz.p.michalsosn.service.ResultService;

import java.io.IOException;
import java.util.Map;

/**
 * @author Michał Sośnicki
 */
@RestController
@RequestMapping("/user/{username}/process/{process}"
              + "/operation/{operation}/result")
public class ResultRestController {

    @Autowired
    private ResultService resultService;

    @RequestMapping(method = RequestMethod.GET)
    public ResultListSupport listResults(
            @PathVariable String username,
            @PathVariable("process") String processName,
            @PathVariable("operation") long operationId
    ) {
        OperationStatusAttachment<Map<String, ResultEntity>> results
                = resultService.listResults(
                username, processName, operationId
        );
        return new ResultListSupport(username, results,
                                     processName, operationId);
    }

    @RequestMapping(path = "/{result}", method = RequestMethod.GET,
                    produces = MediaType.APPLICATION_JSON_VALUE)
    public Object getResultAsChart(
            @PathVariable String username,
            @PathVariable("process") String processName,
            @PathVariable("operation") long operationId,
            @PathVariable("result") String resultName,
            @RequestParam(name = "start", required = false) Double start,
            @RequestParam(name = "end", required = false) Double end
    ) throws IOException {
        return resultService.getResultAsChart(
                username, processName, operationId, resultName, start, end
        );
    }

    @RequestMapping(path = "/{result}", method = RequestMethod.GET,
                    produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getResultAsPng(
            @PathVariable String username,
            @PathVariable("process") String processName,
            @PathVariable("operation") long operationId,
            @PathVariable("result") String resultName
    ) throws IOException {
        byte[] imageData = resultService.getResultAsPng(
                username, processName, operationId, resultName
        );
        return new ResponseEntity<>(imageData, HttpStatus.OK);
    }

    @RequestMapping(path = "/{result}", method = RequestMethod.GET,
                    produces = "audio/x-wav")
    public ResponseEntity<byte[]> getResultAsWave(
            @PathVariable String username,
            @PathVariable("process") String processName,
            @PathVariable("operation") long operationId,
            @PathVariable("result") String resultName
    ) throws IOException {
        byte[] soundData = resultService.getResultAsWave(
                username, processName, operationId, resultName
        );
        return new ResponseEntity<>(soundData, HttpStatus.OK);
    }

    @RequestMapping(path = "/{result}", method = RequestMethod.GET,
                    produces = "audio/flac")
    public ResponseEntity<byte[]> getResultAsFlac(
            @PathVariable String username,
            @PathVariable("process") String processName,
            @PathVariable("operation") long operationId,
            @PathVariable("result") String resultName
    ) throws IOException {
        byte[] soundData = resultService.getResultAsFlac(
                username, processName, operationId, resultName
        );
        return new ResponseEntity<>(soundData, HttpStatus.OK);
    }

}

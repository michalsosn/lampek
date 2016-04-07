package pl.lodz.p.michalsosn.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
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

}

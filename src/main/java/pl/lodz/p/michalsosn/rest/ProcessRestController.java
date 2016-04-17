package pl.lodz.p.michalsosn.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.michalsosn.entities.ProcessEntity;
import pl.lodz.p.michalsosn.rest.support.ProcessEntitySupport;
import pl.lodz.p.michalsosn.rest.support.ProcessPageSupport;
import pl.lodz.p.michalsosn.service.OperationService;
import pl.lodz.p.michalsosn.service.ProcessService;
import pl.lodz.p.michalsosn.specification.OperationRequest;

import java.io.IOException;

import static pl.lodz.p.michalsosn.service.ProcessService.ReplaceResult;

/**
 * @author Michał Sośnicki
 */
@RestController
@RequestMapping("/user/{username}/process")
public class ProcessRestController {

    @Autowired
    private ProcessService processService;
    @Autowired
    private OperationService operationService;

    @RequestMapping(method = RequestMethod.GET)
    public ProcessPageSupport listProcesses(
            @PathVariable String username,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size
    ) {
            Page<ProcessEntity> namePage
                    = processService.listProcesses(username, page, size);
            return new ProcessPageSupport(username, namePage);
    }

    @RequestMapping(path = "/{name}", method = RequestMethod.GET)
    public ProcessEntitySupport getProcessEntity(
            @PathVariable String username,
            @PathVariable String name
    ) {
        ProcessEntity processEntity
                = processService.getProcessEntity(username, name);
        return new ProcessEntitySupport(processEntity);
    }

    @RequestMapping(path = "/{name}", method = RequestMethod.PUT)
    public ResponseEntity replaceProcess(
            @PathVariable String username,
            @PathVariable String name,
            @RequestBody OperationRequest.ImageRootRequest imageRootRequest
    ) throws IOException {
        ReplaceResult replaceResult
                = processService.replaceProcess(username, name,
                                                imageRootRequest);
        operationService.submitOperation(replaceResult.getId());

        return ResponseEntity.status(
            replaceResult.isFound() ? HttpStatus.NO_CONTENT : HttpStatus.CREATED
        ).build();
    }

    @RequestMapping(path = "/{name}", method = RequestMethod.DELETE)
    public ResponseEntity deleteProcess(
            @PathVariable String username,
            @PathVariable String name
    ) {
        processService.deleteProcess(username, name);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

}

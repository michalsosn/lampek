package pl.lodz.p.michalsosn.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.michalsosn.entities.ProcessEntity;
import pl.lodz.p.michalsosn.entities.specification.OperationRequest;
import pl.lodz.p.michalsosn.service.OperationService;
import pl.lodz.p.michalsosn.service.ProcessService;

import java.io.IOException;
import java.security.Principal;

import static pl.lodz.p.michalsosn.service.ProcessService.ReplaceResult;

/**
 * @author Michał Sośnicki
 */
@RestController
@RequestMapping("/processes")
public class ProcessRestController {

    @Autowired
    private ProcessService processService;
    @Autowired
    private OperationService operationService;

    @RequestMapping(path = "/specification", method = RequestMethod.GET)
    public SpecificationSupport getSpecification(
    ) {
        return new SpecificationSupport();
    }

    @RequestMapping(method = RequestMethod.GET)
    public ProcessPageSupport listProcesses(
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam(name = "size", defaultValue = "10") Integer size,
            Principal principal
    ) {
        String username = principal.getName();
        Page<String> namePage
                = processService.listProcessNames(username, page, size);
        return new ProcessPageSupport(namePage);
    }

    @RequestMapping(path = "/{name}", method = RequestMethod.GET)
    public ProcessEntitySupport getProcessEntity(
            @PathVariable String name, Principal principal
    ) {
        String username = principal.getName();
        ProcessEntity processEntity
                = processService.getProcessEntity(username, name);
        return new ProcessEntitySupport(processEntity);
    }

    @RequestMapping(path = "/{name}", method = RequestMethod.PUT)
    public ResponseEntity replaceProcess(
            @PathVariable String name,
            @RequestBody OperationRequest.StartRequest startRequest,
            Principal principal
    ) throws IOException {
        String username = principal.getName();

        ReplaceResult replaceResult
                = processService.replaceProcess(username, name, startRequest);
        operationService.submitOperation(replaceResult.getId());

        return ResponseEntity.status(
            replaceResult.isFound() ? HttpStatus.NO_CONTENT : HttpStatus.CREATED
        ).build();
    }

    @RequestMapping(path = "/{name}", method = RequestMethod.DELETE)
    public ResponseEntity deleteProcess(
            @PathVariable String name, Principal principal
    ) {
        String username = principal.getName();
        processService.deleteProcess(username, name);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

}

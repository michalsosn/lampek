package pl.lodz.p.michalsosn.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.michalsosn.rest.support.OperationListSupport;
import pl.lodz.p.michalsosn.rest.support.OperationRequestSupport;
import pl.lodz.p.michalsosn.rest.support.OperationStatusAttachment;
import pl.lodz.p.michalsosn.rest.support.OperationSummaryAttachment;
import pl.lodz.p.michalsosn.service.OperationService;
import pl.lodz.p.michalsosn.specification.OperationRequest;

import java.util.List;

/**
 * @author Michał Sośnicki
 */
@RestController
@RequestMapping("/user/{username}/process/{process}/operation")
public class OperationRestController {

    @Autowired
    private OperationService operationService;

    @RequestMapping(method = RequestMethod.GET)
    public OperationListSupport listOperations(
            @PathVariable String username,
            @PathVariable("process") String processName
    ) {
        List<OperationSummaryAttachment<Long>> idList
                = operationService.listOperationIds(username, processName);
        return new OperationListSupport(username, idList, processName);
    }

    @RequestMapping(path = "/{operation}", method = RequestMethod.GET)
    public OperationRequestSupport retrieveRequest(
            @PathVariable String username,
            @PathVariable("process") String processName,
            @PathVariable("operation") long operationId
    ) {
        OperationStatusAttachment<OperationRequest> operationRequest
                = operationService.retrieveRequest(username, processName,
                                                   operationId);
        return new OperationRequestSupport(username,
                operationRequest, processName, operationId
        );
    }

    @RequestMapping(path = "/{operation}", method = RequestMethod.POST)
    public OperationRequestSupport acceptRequest(
            @PathVariable String username,
            @PathVariable("process") String processName,
            @PathVariable("operation") long operationId,
            @RequestParam(defaultValue = "false") boolean replace,
            @RequestBody OperationRequest operationRequest
    ) {
        long newId;
        if (replace) {
            newId = operationService.replaceOperation(
                    username, processName, operationId, operationRequest
            );
        } else {
            newId = operationService.acceptRequest(
                    username, processName, operationId, operationRequest
            );
        }
        operationService.submitOperation(newId);
        return new OperationRequestSupport(username,
                new OperationStatusAttachment<>(
                        false, false,
                        operationRequest.getSpecification().getType(),
                        operationRequest
                ), processName, newId
        );
    }

    @RequestMapping(path = "/{operation}", method = RequestMethod.DELETE)
    public void deleteOperation(
            @PathVariable String username,
            @PathVariable("process") String processName,
            @PathVariable("operation") long operationId
    ) {
        operationService.deleteOperation(
                username, processName, operationId
        ).ifPresent(id ->
            operationService.submitOperation(id)
        );
    }

}

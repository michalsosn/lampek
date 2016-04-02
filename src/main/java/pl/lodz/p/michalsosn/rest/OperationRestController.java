package pl.lodz.p.michalsosn.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import pl.lodz.p.michalsosn.entities.specification.OperationRequest;
import pl.lodz.p.michalsosn.service.OperationService;
import pl.lodz.p.michalsosn.service.OperationService.OperationStatusAttachment;

import java.security.Principal;
import java.util.List;

/**
 * @author Michał Sośnicki
 */
@RestController
@RequestMapping("/processes/{process}/operations")
public class OperationRestController {

    @Autowired
    private OperationService operationService;

    @RequestMapping(method = RequestMethod.GET)
    public OperationListSupport listOperations(
            @PathVariable("process") String processName,
            Principal principal
    ) {
        String username = principal.getName();
        List<OperationStatusAttachment<Long>> idList
                = operationService.listOperationIds(username, processName);
        return new OperationListSupport(idList, processName);
    }

    @RequestMapping(path = "/{operation}", method = RequestMethod.GET)
    public OperationRequestSupport retrieveRequest(
            @PathVariable("process") String processName,
            @PathVariable("operation") long operationId,
            Principal principal
    ) {
        String username = principal.getName();
        OperationStatusAttachment<OperationRequest> operationRequest
                = operationService.retrieveRequest(username, processName,
                                                   operationId);
        return new OperationRequestSupport(
                operationRequest, processName, operationId
        );
    }

    @RequestMapping(path = "/{operation}", method = RequestMethod.POST)
    public long acceptRequest(
            @PathVariable("process") String processName,
            @PathVariable("operation") long parentId,
            @RequestBody OperationRequest operationRequest,
            Principal principal
    ) {
        String username = principal.getName();
        long id = operationService.acceptRequest(
            username, processName, parentId, operationRequest
        );
        operationService.submitOperation(id);
        return id;
    }

    @RequestMapping(path = "/{operation}", method = RequestMethod.PUT)
    public long replaceOperation(
            @PathVariable("process") String processName,
            @PathVariable("operation") long parentId,
            @RequestBody OperationRequest operationRequest,
            Principal principal
    ) {
        String username = principal.getName();
        long id = operationService.replaceOperation(
                username, processName, parentId, operationRequest
        );
        operationService.submitOperation(id);
        return id; // TODO it should be joined to POST!
    }

    @RequestMapping(path = "/{operation}", method = RequestMethod.DELETE)
    public void deleteOperation(
            @PathVariable("process") String processName,
            @PathVariable("operation") long parentId,
            Principal principal
    ) {
        String username = principal.getName();
        operationService.deleteOperation(
                username, processName, parentId
        );
    }

}

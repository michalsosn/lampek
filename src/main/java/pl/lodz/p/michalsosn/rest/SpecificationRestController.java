package pl.lodz.p.michalsosn.rest;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

/**
 * @author Michał Sośnicki
 */
@RestController
@RequestMapping("/processes/{process}/specifications")
public class SpecificationRestController {

    @RequestMapping(method = RequestMethod.GET)
    public SpecificationSupport getSpecifications(
            @PathVariable String process, Principal principal
    ) {
        return new SpecificationSupport(process);
    }

}

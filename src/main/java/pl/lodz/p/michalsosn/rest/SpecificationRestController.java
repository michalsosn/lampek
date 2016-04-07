package pl.lodz.p.michalsosn.rest;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import pl.lodz.p.michalsosn.rest.support.SpecificationSupport;

/**
 * @author Michał Sośnicki
 */
@RestController
@RequestMapping("/user/{username}/process/{process}/specification")
public class SpecificationRestController {

    @RequestMapping(method = RequestMethod.GET)
    public SpecificationSupport getSpecifications(
            @PathVariable String username,
            @PathVariable String process
    ) {
        return new SpecificationSupport(username, process);
    }

}

package pl.lodz.p.michalsosn.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.lodz.p.michalsosn.service.AccountService;

/**
 * @author Michał Sośnicki
 */
@RestController
public class AccountRestController {

    @Autowired
    private AccountService accountService;

    @RequestMapping(path = "/register", method = RequestMethod.POST)
    public void registerAccount(@RequestParam("username") String username,
                                @RequestParam("password") String password) {
        accountService.registerAccount(username, password);
    }

}

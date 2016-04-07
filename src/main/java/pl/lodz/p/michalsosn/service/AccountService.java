package pl.lodz.p.michalsosn.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.lodz.p.michalsosn.entities.AccountEntity;
import pl.lodz.p.michalsosn.repository.AccountRepository;

/**
 * @author Michał Sośnicki
 */
@Service
@Transactional
public class AccountService {

    private final Logger log = LoggerFactory.getLogger(AccountService.class);

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AccountRepository accountRepository;

    public AccountService() {
    }

    public void registerAccount(String username, String password) {
        if (accountRepository.findByUsername(username).isPresent()) {
            throw new IllegalArgumentException(
                    "User " + username + " already exists"
            );
        }
        validatePassword(password);
        String encoded = passwordEncoder.encode(password);
        AccountEntity account = new AccountEntity(username, encoded);
        accountRepository.save(account);
        log.info("Account {} registered", username);
    }

    private void validatePassword(String password) {
        if (password == null) {
            throw new IllegalArgumentException("Password is not specified");
        }
        if (password.length() < 6) {
            throw new IllegalArgumentException("Password shorter than 6");
        }
        if (password.length() > 127) {
            throw new IllegalArgumentException("Password longer than 127");
        }
    }

}

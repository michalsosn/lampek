package pl.lodz.p.michalsosn.security;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;


/**
 * @author Michał Sośnicki
 */
@Aspect
@Component
public class CheckOwnerAspect {

    private final Logger log = LoggerFactory.getLogger(CheckOwnerAspect.class);

    @Pointcut("@annotation(pl.lodz.p.michalsosn.security.OwnerOnly)")
    private void checkOwnerMethod() { }

    @Pointcut("@within(pl.lodz.p.michalsosn.security.OwnerOnly)")
    private void checkOwnerType() { }

    @Pointcut("(checkOwnerMethod() || checkOwnerType()) && args(username,..)")
    private void checkOwnerWithArg(String username) { }

    @Before(value = "checkOwnerWithArg(username)",
            argNames = "joinPoint, username")
    public void logMethodAccessBefore(
            JoinPoint joinPoint, String username
    ) {
        log.debug("Checking if the logged user is " + username);
        Object principal = SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            String authorizedName = ((UserDetails)principal).getUsername();
            if (!authorizedName.equals(username)) {
                throw new ForbiddenException(String.format(
                        "This resource is private to user %s, %s can't access it",
                        username, authorizedName
                ));
            }
        } else {
            throw new NotAuthenticatedException(
                    "Anonymous users can't access this resource"
            );
        }
    }

}

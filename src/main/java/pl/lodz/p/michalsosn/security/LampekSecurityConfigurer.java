package pl.lodz.p.michalsosn.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.sql.DataSource;

/**
 * @author Michał Sośnicki
 */
@Configuration
@EnableWebSecurity
@EnableAspectJAutoProxy
public class LampekSecurityConfigurer extends WebSecurityConfigurerAdapter {

    @Autowired
    private DataSource dataSource;
    @Autowired
    private RestAuthenticationEntryPoint authenticationEntryPoint;
    @Autowired
    private RestAuthenticationFailureHandler authenticationFailureHandler;
    @Autowired
    private RestAuthenticationSuccessHandler authenticationSuccessHandler;

    public LampekSecurityConfigurer() {
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder builder)
            throws Exception {
        builder.jdbcAuthentication().dataSource(dataSource)
                .passwordEncoder(passwordEncoder())
                .usersByUsernameQuery(
                        "SELECT username, password, TRUE "
                      + "FROM account WHERE username = ?"
                )
                .authoritiesByUsernameQuery(
                        "SELECT username,'USER' "
                      + "FROM account WHERE username = ?"
                );
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
//        http.authorizeRequests().antMatchers("/images/**").authenticated();
//        http.authorizeRequests().antMatchers("/processes/**").authenticated();
        http.authorizeRequests().anyRequest().permitAll();
        http.csrf().disable();
        http.exceptionHandling()
                .authenticationEntryPoint(authenticationEntryPoint);
        http.formLogin()
                .loginPage("/user/login")
                .successHandler(authenticationSuccessHandler)
                .failureHandler(authenticationFailureHandler);
//        http.rememberMe()
        http.logout()
                .logoutUrl("/user/logout");
    }

}

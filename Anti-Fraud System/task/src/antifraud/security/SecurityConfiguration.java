package antifraud.security;

import antifraud.AntiFraudApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.authorizeRequests().mvcMatchers("/test", "/actuator/shutdown").permitAll().mvcMatchers(HttpMethod.POST, "/api/auth/user").permitAll().mvcMatchers(HttpMethod.GET, "/api/auth/list").hasAnyRole("ADMINISTRATOR", "SUPPORT").mvcMatchers(HttpMethod.DELETE, "/api/auth/user/**").hasRole("ADMINISTRATOR").mvcMatchers(HttpMethod.POST, "/api/antifraud/transaction").hasRole("MERCHANT").mvcMatchers(HttpMethod.PUT, "/api/antifraud/transaction").hasRole("SUPPORT").mvcMatchers("/api/antifraud/suspicious-ip/**").hasRole("SUPPORT").mvcMatchers("/api/antifraud/stolencard").hasRole("SUPPORT").mvcMatchers("/api/antifraud/history/**").hasRole("SUPPORT").mvcMatchers(HttpMethod.PUT, "/api/auth/access").hasRole("ADMINISTRATOR").mvcMatchers(HttpMethod.PUT, "/api/auth/role").hasRole("ADMINISTRATOR").and().exceptionHandling().authenticationEntryPoint(new AntiFraudApplication.RestAuthenticationEntryPoint()).and().csrf().disable().headers().frameOptions().disable().and().httpBasic(Customizer.withDefaults());

        return httpSecurity.build();
    }

    @Bean
    public AdminSecurity setAdminSecurity() {
        return AdminSecurity.builder().operation("UNLOCK").role("ADMINISTRATOR").build();
    }


}
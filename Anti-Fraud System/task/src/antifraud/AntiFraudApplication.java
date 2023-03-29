package antifraud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@SpringBootApplication
public class AntiFraudApplication {


    public enum TransactionState {
        ALLOWED, MANUAL_PROCESSING, PROHIBITED, INVALID
    }

    public enum Region {
        EAP, ECA, HIC, LAC, MENA, SA, SSA
    }

    @Bean
    public static PasswordEncoder getEncoder() {
        return new BCryptPasswordEncoder();
    }


    public static class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

        @Override
        public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
        }


        @ExceptionHandler(value = {AccessDeniedException.class})
        public void commence(HttpServletRequest request, HttpServletResponse response,
                             AccessDeniedException accessDeniedException) throws IOException {
            // 403
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Authorization Failed : " + accessDeniedException.getMessage());
        }

        @ExceptionHandler(value = {Exception.class})
        public void commence(HttpServletRequest request, HttpServletResponse response,
                             Exception exception) throws IOException {
            // 500
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal Server Error : " + exception.getMessage());
        }
    }


    public static void main(String[] args) {
        SpringApplication.run(AntiFraudApplication.class, args);

    }
}
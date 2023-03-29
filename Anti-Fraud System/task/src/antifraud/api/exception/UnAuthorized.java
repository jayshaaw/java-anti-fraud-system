package antifraud.api.exception;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
@AllArgsConstructor
public class UnAuthorized extends RuntimeException {
    private String message;
}

package antifraud.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class UnAuthorized extends RuntimeException {
    public UnAuthorized(String message) {
        super(message);
    }
}

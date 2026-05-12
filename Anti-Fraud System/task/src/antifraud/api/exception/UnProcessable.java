package antifraud.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class UnProcessable extends RuntimeException {
    public UnProcessable(String message) {
        super(message);
    }
}

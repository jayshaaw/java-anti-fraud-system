package antifraud.api.exception;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
@AllArgsConstructor
public class UnProcessable extends RuntimeException{
    private String message;
}

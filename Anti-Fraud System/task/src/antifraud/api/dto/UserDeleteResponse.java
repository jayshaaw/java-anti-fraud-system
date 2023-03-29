package antifraud.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserDeleteResponse {
    private String username;
    private final String status;
}

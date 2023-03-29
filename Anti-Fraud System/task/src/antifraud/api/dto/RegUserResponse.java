package antifraud.api.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegUserResponse {

    private Long id;

    private String name;

    private String username;

    private String role;

}

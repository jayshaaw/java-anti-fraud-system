package antifraud.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserListResponse {

    private Long id;

    private String name;

    private String username;

    private String role;


}

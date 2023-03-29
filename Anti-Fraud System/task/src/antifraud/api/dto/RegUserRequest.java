package antifraud.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegUserRequest {

    @NotNull
    private String name;

    @NotNull
    private String username;

    @NotNull
    private String password;


}

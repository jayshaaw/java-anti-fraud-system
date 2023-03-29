package antifraud.security;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AdminSecurity {
    private String operation;
    private String role;
}
